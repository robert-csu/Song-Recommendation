/*
 * Song Simlarity purpose:
 *  to find the similarity between songs using Euclidean Distance and the Z-Rating of each user
 *      - to find ED and Z-Rating: found mean and Standard Div for each user
 *  Print the similarity between pairs of songs
 */

import java.io.*;
import java.util.*;
import java.lang.*;


public class SongSimilarity implements mainInterface{
    // TreeMap of all songs and the amount of time they are rated
    private TreeMap<String,Integer> allSongs;
    // TreeMap of all users and the amount of times they rated
    private TreeMap<String,Integer> allUsers;
    // List that hold the continual stream of ratings
    // users -> list of all ratings
    private TreeMap<String, List<Integer>> usersIndividualRatings;
    // users -> (Song, rating)
    private TreeMap<String, TreeMap<String,Integer>> UsersSongRatings = new TreeMap<>();
    // TreeMap of cooperative Users so we can have a clean data set
    private TreeMap<String, TreeMap<String, Integer>> cooperativeUsers;
    // TreeMap of cooperative songs so we have a clean data set
    private TreeMap<String, Integer> cooperativeSongs;

    // user -> (Song, Z-Rating)
    private TreeMap<String, TreeMap<String, Double>> userZRatingPerSong; 

    public SongSimilarity(){
        this.allSongs = new TreeMap<>();
        this.allUsers = new TreeMap<>();
        this.usersIndividualRatings = new TreeMap<>();
        this.UsersSongRatings = new TreeMap<>();
        this.cooperativeUsers = new TreeMap<>();
        this.cooperativeSongs = new TreeMap<>();
    }

    public void cooperativeCalculations(){
        this.cooperativeUsers = UserAnalysis.findingCooperativeUsers(usersIndividualRatings, UsersSongRatings);
        if(cooperativeUsers.size() < 2){System.err.print("Error: There are too little cooperative users in this data."); return;}
        this.cooperativeSongs = UserAnalysis.findingCooperativeSongs(cooperativeUsers);
        if(cooperativeSongs.isEmpty()){System.err.print("Error: There are no cooperative users in the data."); return;}
    }

    public void zRatingCalculations(){
        this.userZRatingPerSong = zRatingUsers(cooperativeUsers);
    }





    // Follows the implementation in the mainInterface
    public void acceptData(String song, String user, int rating){
        usersIndividualRatings.computeIfAbsent(user, i -> new ArrayList<>()).add(rating);

        UsersSongRatings.computeIfAbsent(user, i -> new TreeMap<>()).put(song,rating);

        allSongs.computeIfPresent(song, (key,value)->value + 1);
        allSongs.putIfAbsent(song, 1);

        allUsers.computeIfPresent(user, (key,value)->value + 1);
        allUsers.putIfAbsent(user, 1);
    }

    // Follows the implementation in the mainInterface
    public void writeCSV(String outputFile){
        cooperativeCalculations();
        zRatingCalculations();
        TreeMap<String, Double> euclideanDistance = euclideanDistance(userZRatingPerSong, cooperativeSongs);
        try(FileWriter writer = new FileWriter(outputFile)){
            writer.write("name1,name2,similarity\n");
            for(String song : euclideanDistance.keySet()){
                writer.write(song + "," + euclideanDistance.get(song) + "\n");
            }
        }
        catch(Exception e){
            System.err.println("Error: unable to write to: " + outputFile);
            return;
        }
    }

    /// Finding the users mean:
    /// for each user -> added the rating of each song to a total, divide my the size of cooperative songs -> put in the TreeMap and return
    public static TreeMap<String, Double> findingUserMean(TreeMap<String, TreeMap<String, Integer>> cooperativeUsers){
        TreeMap<String, Double> userMean = new TreeMap<>();
        if(cooperativeUsers.isEmpty()){System.err.print("Error: There are no cooperative users in this data."); return new TreeMap<>();}
        for(String user : cooperativeUsers.keySet()){
            TreeMap<String, Integer> songs = cooperativeUsers.get(user);
            double userTotal = 0.0;

            for(String song: songs.keySet()){
                userTotal += songs.get(song);
            }

            double mean = userTotal/songs.size();

            userMean.put(user, mean);
        }
        return userMean;
    }

    /// Using the mean to find the Standard Div of each user:
    /// for each user -> get the users mean by calling findUserMean -> find the sumSquaredDiff -> use sumSquaredDiff to find Standard Div -> put mean in index 0, stdDiv in index 1, return
    public static TreeMap<String, List<Double>> userMeanStd(TreeMap<String, TreeMap<String,Integer>> cooperativeUsers){
        TreeMap<String, List<Double>> userMeanStd = new TreeMap<>();
        for(String user : cooperativeUsers.keySet()){
            List<Double> meanStd = new ArrayList<>();
            TreeMap<String, Double> userMean = findingUserMean(cooperativeUsers);
            double mean = userMean.get(user);
            double sumSquaredDiff = 0.0;
            double standardDiv = 0.0;
            TreeMap<String, Integer> songs = cooperativeUsers.get(user);

            for(String song : songs.keySet()){
                int rating = songs.get(song);
                sumSquaredDiff += Math.pow(rating - mean, 2);
            }
            standardDiv = Math.sqrt(sumSquaredDiff/songs.size());

            meanStd.add(mean);
            meanStd.add(standardDiv);

            userMeanStd.put(user, meanStd);   
        }
        return userMeanStd;
    }

    /// Find the Z-Rating for each song, using userMeanStd
    /// for each user -> get the mean and stdDiv from userMeanStd -> calculated Z-Rating per song -> put user -> (song, z-rating) into map, return
    public static TreeMap<String, TreeMap<String, Double>> zRatingUsers(TreeMap<String, TreeMap<String, Integer>> cooperativeUsers){
        TreeMap<String, TreeMap<String,Double>> userZRatingPerSong = new TreeMap<>();
        TreeMap<String, List<Double>> userMeanStd = userMeanStd(cooperativeUsers);
        for(String user : userMeanStd.keySet()){
            TreeMap<String, Double> songZRating = new TreeMap<>();
            List<Double> meanStd = userMeanStd.get(user);
            double mean = meanStd.get(0);
            double standardDiv = meanStd.get(1);

            TreeMap<String, Integer> songs = cooperativeUsers.get(user);
            for(String song : songs.keySet()){
                int rating = songs.get(song);
                double zRating = (rating - mean) / standardDiv;

                songZRating.put(song, zRating);
            }
            userZRatingPerSong.put(user, songZRating);
        }
        return userZRatingPerSong;
    }

    /// Find the linear span of cooperative songs - every unique combination in the set
    /// for each song in cooperativeSongs -> get a second song from cooperativeSongs -> if the songs arent the same, and the songs havent been combined together, add to the List as a combo -> return the list
    public static List<String> linearSpan(TreeMap<String, Integer> cooperativeSongs){
        List<String> linearSpanOfAllCoopSongs = new ArrayList<>();
        if(cooperativeSongs.isEmpty()){System.err.print("Error: There are no cooperative users in the data."); return new ArrayList<>();}

        for(String song : cooperativeSongs.keySet()){
            for(String song2 : cooperativeSongs.keySet()){
                if(!song.equals(song2) && !linearSpanOfAllCoopSongs.contains(song+","+song2) && !linearSpanOfAllCoopSongs.contains(song2+","+song)){
                    linearSpanOfAllCoopSongs.add(song+","+song2);
                }
                else{continue;}
            }
        }
        return linearSpanOfAllCoopSongs;
    }

    
    public static TreeMap<String, Double> euclideanDistance(TreeMap<String, TreeMap<String, Double>> userZRatingPerSong, TreeMap<String, Integer> cooperativeSongs){
        TreeMap<String, Double> euclideanDistancePerPair = new TreeMap<>();
        List<String> linearSpanOfAllCoopSongs = linearSpan(cooperativeSongs);
        for(String song : linearSpanOfAllCoopSongs){
            List<String> splitSongs = splitString(song); String song1 = splitSongs.get(0); String song2 = splitSongs.get(1);

            List<Double> differencesForAllPairs = checkValidPairsDiff(userZRatingPerSong, song1, song2);
            Double euclideanDist = calculateEclideanDistance(differencesForAllPairs);
            euclideanDistancePerPair.put(song, euclideanDist);
        }
        return euclideanDistancePerPair;
    }

    public static List<Double> checkValidPairsDiff(TreeMap<String, TreeMap<String, Double>> userZRatingPerSong, String song1, String song2){
        List<Double> differencesForAllPairs = new ArrayList<>();
        for(String user : userZRatingPerSong.keySet()){
            Double song1ZRating = userZRatingPerSong.get(user).get(song1);
            Double song2ZRating = userZRatingPerSong.get(user).get(song2);

            if(song1ZRating == null || song2ZRating == null){continue;}
            if(Double.isNaN(song1ZRating) || Double.isNaN(song2ZRating)){continue;}

            differencesForAllPairs.add(song1ZRating - song2ZRating);
        }
        return differencesForAllPairs;
    }

    public static Double calculateEclideanDistance(List<Double> difference){
        if(difference.isEmpty()){return Double.NaN;}
        Double sumOfSqrDiff = 0.0;
        for(Double diff : difference){
            sumOfSqrDiff += Math.pow(diff, 2);
        }

        return Math.sqrt(sumOfSqrDiff);
    }

    /// Spliting springs 😛😛😛
    /// take a string -> split using delimiter -> put song1 in index1, put song2 in index2
    public static List<String> splitString(String song1song2){
        String[] splitSongsComponents = new String[3];
        List<String> splitSongs = new ArrayList<>();
        String delimiter = ",";
        splitSongsComponents = song1song2.split(delimiter,-1);
        for(int i = 0; i<splitSongsComponents.length; i++){
            splitSongs.add(splitSongsComponents[i]);
        }
        return splitSongs;
    }

















    public TreeMap<String,Integer> getAllSongs(){return allSongs;}
    public TreeMap<String,Integer> getAllUsers(){return allUsers;}
    public TreeMap<String, List<Integer>> getUsersIndividualRatings(){return usersIndividualRatings;}
    public TreeMap<String, TreeMap<String,Integer>> getUsersSongRatings(){return UsersSongRatings;}
    public TreeMap<String, TreeMap<String, Integer>> getCooperativeUsers(){return cooperativeUsers;}
    public TreeMap<String, Integer> getCooperativeSongs(){return cooperativeSongs;}



}

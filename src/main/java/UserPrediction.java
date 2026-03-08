
import java.io.*;
import java.util.*;
import java.lang.*;


public class UserPrediction implements mainInterface {
    private TreeMap<String,Integer> allSongs;
    private TreeMap<String,Integer> allUsers;
    private TreeMap<String, List<Integer>> usersIndividualRatings;
    private TreeMap<String, TreeMap<String,Integer>> UsersSongRatings = new TreeMap<>();
    private TreeMap<String, TreeMap<String, Integer>> cooperativeUsers;
    private TreeMap<String, Integer> cooperativeSongs;
    private TreeMap<String, List<Double>> userMeanStd;
    private TreeMap<String, TreeMap<String, Double>> userZRatingPerSong;


    public UserPrediction(){
        this.allSongs = new TreeMap<>();
        this.allUsers = new TreeMap<>();
        this.usersIndividualRatings = new TreeMap<>();
        this.UsersSongRatings = new TreeMap<>();
    }
    public void cooperativeCalculations(){
        this.cooperativeUsers = UserAnalysis.findingCooperativeUsers(usersIndividualRatings, UsersSongRatings);
        if(cooperativeUsers.size() < 2){System.err.print("Error: There are too little cooperative users in this data."); return;}
        this.cooperativeSongs = UserAnalysis.findingCooperativeSongs(cooperativeUsers);
        if(cooperativeSongs.isEmpty()){System.err.print("Error: There are no cooperative users in the data."); return;}
    }
    public void zRatingCalculations(){
        this.userZRatingPerSong = SongSimilarity.zRatingUsers(cooperativeUsers);
    }
    public void meanStdCalculations(){
        this.userMeanStd = SongSimilarity.userMeanStd(cooperativeUsers);
    }


    public void acceptData(String song, String user, int rating){
        usersIndividualRatings.computeIfAbsent(user, i -> new ArrayList<>()).add(rating);

        UsersSongRatings.computeIfAbsent(user, i -> new TreeMap<>()).put(song,rating);

        allSongs.computeIfPresent(song, (key,value)->value + 1);
        allSongs.putIfAbsent(song, 1);

        allUsers.computeIfPresent(user, (key,value)->value + 1);
        allUsers.putIfAbsent(user, 1);
    }

    public void writeCSV(String outputFile){
        cooperativeCalculations();
        zRatingCalculations();
        meanStdCalculations();
        if(noEmptyScore(cooperativeUsers, cooperativeSongs)){System.err.print("Error: no empty scores to predict"); return;}
        TreeMap<String, TreeMap<String, Number>> userPrediction = userPred(cooperativeUsers, cooperativeSongs);
        try(FileWriter writer = new FileWriter(outputFile)){
            writer.write("song,user,predicted rating\n");
            TreeMap<String, TreeMap<String, Number>> formattedUserPrediction = formatingOutput(userPrediction);
            for(String song : formattedUserPrediction.keySet()){
                TreeMap<String, Number> songUserRatings = formattedUserPrediction.get(song);
                for(String user : songUserRatings.keySet()){
                    Number rating = songUserRatings.get(user);
                    writer.write(song + "," + user + "," + rating + "\n");
                }
            }
        }
        catch(Exception e){
            System.err.println("Error: unable to write to: " + outputFile);
            return;
        }
    }


    public TreeMap<String, TreeMap<String, Number>> formatingOutput(TreeMap<String, TreeMap<String, Number>> userPrediction){
        TreeMap<String, TreeMap<String, Number>> formatted = new TreeMap<>();
        for(String user : userPrediction.keySet()){
            TreeMap<String, Number> singleUserSongs = userPrediction.get(user);
            for(String song : singleUserSongs.keySet()){
                formatted.computeIfAbsent(song, i -> new TreeMap<>()).put(user, userPrediction.get(user).get(song));
            }
        }
        return formatted;
    }

    public TreeMap<String, TreeMap<String,Double>> userSim(TreeMap<String, TreeMap<String, Integer>> cooperativeUsers, TreeMap<String, Integer> cooperativeSongs){
        TreeMap<String, TreeMap<String,Double>> userSimilarity = new TreeMap<>();
        for(String user : cooperativeUsers.keySet()){
            TreeMap<String, Double> individualUserSim = new TreeMap<>();
            for(String user2: cooperativeUsers.keySet()){
                if(user.equals(user2)){continue;}
                String fullUsers = user+","+user2;
                individualUserSim.put(user2, euclideanDistance(fullUsers, cooperativeSongs, cooperativeUsers));
            }
            userSimilarity.put(user, individualUserSim);
        }
        return userSimilarity;     
    }

    public Double euclideanDistance(String fullUsers, TreeMap<String, Integer> cooperativeSongs, TreeMap<String, TreeMap<String, Integer>> cooperativeUsers){
        List<String> users = SongSimilarity.splitString(fullUsers); String user = users.get(0); String user2 = users.get(1);
        Integer commonSongs = 0;
        Double preSqrtTotal = 0.0;
        for(String song: cooperativeSongs.keySet()){
            Integer user1Rating = cooperativeUsers.get(user).get(song);
            Integer user2Rating = cooperativeUsers.get(user2).get(song);
            if(checkNaN(user1Rating, user2Rating)){
                commonSongs++;
                preSqrtTotal += Math.pow(user1Rating - user2Rating, 2);
            }
        }
        if(commonSongs == 0){return Double.NaN;}
        return Math.sqrt(preSqrtTotal);
    }

    public Boolean checkNaN(Integer user1Rating, Integer user2Rating){
        if(user1Rating != null && user2Rating != null){return true;}
        return false;
    }


    public TreeMap<String, TreeMap<String, Number>> userPred(TreeMap<String, TreeMap<String, Integer>> cooperativeUsers, TreeMap<String, Integer> cooperativeSongs){
        this.cooperativeUsers = cooperativeUsers;
        this.cooperativeSongs = cooperativeSongs;
        this.userZRatingPerSong = SongSimilarity.zRatingUsers(cooperativeUsers);
        this.userMeanStd = SongSimilarity.userMeanStd(cooperativeUsers);
        TreeMap<String, TreeMap<String, Number>> userPrediction = new TreeMap<>();
        for(String user : cooperativeUsers.keySet()){
            TreeMap<String, Integer> userSongandRating = cooperativeUsers.get(user);
            TreeMap<String, Number> predictedSongsandRating = new TreeMap<>();
            TreeMap<String, TreeMap<String,Double>> userSimilarity = userSim(cooperativeUsers, cooperativeSongs);
            for(String song : cooperativeSongs.keySet()){
                if(userCheckSong(song, userSongandRating)){continue;}
                String similarUser = mostSimilarUser(userSimilarity, user, song);
                if(similarUserCheck(similarUser)){ predictedSongsandRating.put(song, Double.NaN); continue;}
                List<TreeMap<?,?>> mapsForPred = new ArrayList<>(); mapsForPred.add(userMeanStd); mapsForPred.add(userZRatingPerSong);
                String songUserSimUser = song+","+user+","+similarUser;
                predictedSongsandRating.put(song, predictedRating(mapsForPred, songUserSimUser));
            }
            userPrediction.put(user, predictedSongsandRating);
        }
        return userPrediction;
    }

    public Boolean similarUserCheck(String similarUser){
        if(similarUser.equals("")){ return true;}
        return false;
    }

    public Boolean userCheckSong(String song, TreeMap<String, Integer> userSongandRating){
        if(userSongandRating.get(song)!= null){ return true;}
        return false;
    }

    public String mostSimilarUser(TreeMap<String, TreeMap<String,Double>> userSimilarity, String user, String song){
        TreeMap<String,Double> comparedUsers = userSimilarity.get(user);
        String lowestSimilarity = "";
        Double largestNum = Double.MAX_VALUE;
        for(String user2 : comparedUsers.keySet()){
            if(comparedUsers.get(user2) < largestNum){
                if(noSimilarSong(user2, song)){continue;}
                largestNum = comparedUsers.get(user2);
                lowestSimilarity = user2;
            }
        }
        return lowestSimilarity;
    }

    public Boolean noSimilarSong(String user2, String song){
        Integer user2Rating = cooperativeUsers.get(user2).get(song);
        if(user2Rating == null){ return true;}
        return false;
    }
    public Integer predictedRating(List<TreeMap<?,?>> mapsForPred, String songUser1User2){
        @SuppressWarnings("unchecked")
        TreeMap<String, List<Double>> userMeanStd = (TreeMap<String, List<Double>>)mapsForPred.get(0);
        @SuppressWarnings("unchecked")
        TreeMap<String, TreeMap<String, Double>> userZRatingPerSong = (TreeMap<String, TreeMap<String, Double>>)mapsForPred.get(1);
        List<String> songUsers = SongSimilarity.splitString(songUser1User2); String song = songUsers.get(0); String user1 = songUsers.get(1); String user2 = songUsers.get(2);
        Double predicted = userZRatingPerSong.get(user2).get(song) * userMeanStd.get(user1).get(1) + userMeanStd.get(user1).get(0);
        Integer normalizedPred = normPrediction(predicted);

        return normalizedPred;
    }

    public Integer normPrediction(Double predicted){
        if(predicted < 1){ return 1;}
        if(predicted > 5){ return 5;}
        return (int) Math.round(predicted);
    }

    public boolean noEmptyScore(TreeMap<String, TreeMap<String, Integer>> cooperativeUsers, TreeMap<String, Integer> cooperativeSongs){
        for(String song : cooperativeSongs.keySet()){
            for(String user : cooperativeUsers.keySet()){
                if(cooperativeUsers.get(user).get(song) == null){ return false;}
            }
        }
        return true;
    }







    public TreeMap<String,Integer> getAllSongs(){return allSongs;}
    public TreeMap<String,Integer> getAllUsers(){return allUsers;}
    public TreeMap<String, List<Integer>> getUsersIndividualRatings(){return usersIndividualRatings;}
    public TreeMap<String, TreeMap<String,Integer>> getUsersSongRatings(){return UsersSongRatings;}
    public TreeMap<String, TreeMap<String, Integer>> getCooperativeUsers(){return cooperativeUsers;}
    public TreeMap<String, Integer> getCooperativeSongs(){return cooperativeSongs;}
    public TreeMap<String, List<Double>> getUserMeanStd(){return userMeanStd;}
    public TreeMap<String, TreeMap<String, Double>> getUserZRatingPerSong(){return userZRatingPerSong;}

}

/*
 * User Analysis purpose:
 *      find cooperative user and cooperative song from the data set
 */



import java.io.*;
import java.util.*;
import java.lang.*;


public class UserAnalysis implements mainInterface{
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


    public UserAnalysis(){
        this.allSongs = new TreeMap<>();
        this.allUsers = new TreeMap<>();
        this.usersIndividualRatings = new TreeMap<>();
        this.UsersSongRatings = new TreeMap<>();
        this.cooperativeUsers = new TreeMap<>();
        this.cooperativeSongs = new TreeMap<>();
    }

    public void cooperativeCalculations(){
        this.cooperativeUsers = findingCooperativeUsers(usersIndividualRatings, UsersSongRatings);
        if(cooperativeUsers.size() < 2){System.err.print("Error: There are too little cooperative users in this data."); return;}
        this.cooperativeSongs = findingCooperativeSongs(cooperativeUsers);
        if(cooperativeSongs.isEmpty()){System.err.print("Error: There are no cooperative users in the data."); return;}
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

        try(FileWriter writer = new FileWriter(outputFile)){
            writer.write("username,song,rating\n");
            writeCSVHelper(cooperativeUsers, cooperativeSongs, writer);

        }
        catch(Exception e){
            System.err.println("Error: unable to write to: " + outputFile);
            return;
        }
    }

    public static void writeCSVHelper(TreeMap<String, TreeMap<String, Integer>> cooperativeUsers, TreeMap<String, Integer> cooperativeSongs, FileWriter writer) throws IOException{
        for(String coopUser : cooperativeUsers.keySet()){
            Map<String, Integer> tempUserSongs = cooperativeUsers.get(coopUser);
            if(tempUserSongs == null){continue;}
            for(String songs : cooperativeSongs.keySet()){
                if(!tempUserSongs.containsKey(songs)){
                    writer.write(coopUser + "," + songs + "," + Double.NaN + "\n");
                }
                else{
                    writer.write(coopUser + "," + songs + "," + tempUserSongs.get(songs) + "\n");
                }
            }
        }
    }

    /// Finding Cooperative Users
    /// for each user -> if the user rated more than once and rated with 2 difference score -> put in the map, returns
    public static TreeMap<String, TreeMap<String, Integer>> findingCooperativeUsers(TreeMap<String, List<Integer>> usersIndividualRatings, TreeMap<String, TreeMap<String,Integer>> UsersSongRatings){
        TreeMap<String, TreeMap<String, Integer>> cooperativeUsers = new TreeMap<>();
        for(String users : usersIndividualRatings.keySet()){
            List<Integer> tempUserHold = usersIndividualRatings.get(users);
            if(tempUserHold != null && tempUserHold.size() > 1 && tempUserHold.stream().distinct().count() > 1){
                // pulls all songs from user in UserSongRatings
                cooperativeUsers.computeIfAbsent(users, i -> UsersSongRatings.get(users));
            }
        }
        return cooperativeUsers;
    }

    /// Finding Cooperative Songs
    /// for each user in coopUsers -> added all the songs that the cooperative user has rated into the map, return
    public static TreeMap<String, Integer> findingCooperativeSongs(TreeMap<String, TreeMap<String, Integer>> cooperativeUsers){
        TreeMap<String, Integer> cooperativeSongs = new TreeMap<>();    
        for(String allCoopUsers : cooperativeUsers.keySet()){
            TreeMap<String, Integer> tempSongHold = cooperativeUsers.get(allCoopUsers);
            for(String coopSongs : tempSongHold.keySet()){
                cooperativeSongs.computeIfPresent(coopSongs, (key,value)->value + 1);
                cooperativeSongs.putIfAbsent(coopSongs, 1);
            }
        }
        return cooperativeSongs;
    }


    public TreeMap<String,Integer> getAllSongs(){return allSongs;}
    public TreeMap<String,Integer> getAllUsers(){return allUsers;}
    public TreeMap<String, List<Integer>> getUsersIndividualRatings(){return usersIndividualRatings;}
    public TreeMap<String, TreeMap<String,Integer>> getUsersSongRatings(){return UsersSongRatings;}
    public TreeMap<String, TreeMap<String, Integer>> getCooperativeUsers(){return cooperativeUsers;}
    public TreeMap<String, Integer> getCooperativeSongs(){return cooperativeSongs;}


}

/*
 * Song Stats purpose:
 *  use welfordOnlineAlgorithm to consistantly update the users mean and stdDiv
 */


import java.io.*;
import java.util.*;
import java.lang.*;


public class SongStats implements mainInterface{
    // TreeMap of all songs and the amount of time they are rated
    private TreeMap<String,Integer> allSongs;
    // TreeMap of all users and the amount of times they rated
    private TreeMap<String,Integer> allUsers;
    // TreeMap of songs and the updates Mean_M2_standardDiv
    private TreeMap<String,double[]> songsAndRatings;

    public SongStats(){
        this.allSongs = new TreeMap<>();
        this.allUsers = new TreeMap<>();
        this.songsAndRatings = new TreeMap<>();
        
    }

    // Follows the implementation in the mainInterface
    public void acceptData(String song, String user, int rating){
        allSongs.computeIfPresent(song, (key,value)->value + 1);
        allSongs.putIfAbsent(song, 1);

        allUsers.computeIfPresent(user, (key,value)->value + 1);
        allUsers.putIfAbsent(user, 1);
    
        double[] Mean_M2_StandardDiv = songsAndRatings.get(song);
        if(Mean_M2_StandardDiv == null){ Mean_M2_StandardDiv = new double[]{0.0,0.0,0.0};}

        double[] updatedMean_M2_StandardDiv = welfordOnlineAlgorithm(rating, allSongs.get(song), Arrays.copyOfRange(Mean_M2_StandardDiv, 0, 2));
        songsAndRatings.putIfAbsent(song, updatedMean_M2_StandardDiv);
        songsAndRatings.put(song, updatedMean_M2_StandardDiv);
    }

    // Follows the implementation in the mainInterface
    public void writeCSV(String outputFile){
        try(FileWriter writer = new FileWriter(outputFile)){
            writer.write("song,number of ratings,mean,standard deviation\n");
            for(String songs:allSongs.keySet()){
                writer.write(songs + "," + allSongs.get(songs) + "," + songsAndRatings.get(songs)[0] + "," + songsAndRatings.get(songs)[2] + "\n" );

            }
        }
        catch(Exception e){
            System.err.println("Error: unable to write to: " + outputFile);
            return;
        }
    }

    // Pseudocode from Wikipeadia page
    public static double[] welfordOnlineAlgorithm(int newRating, int count, double[] welfordStats){
        double mean = welfordStats[0];
        double m2 = welfordStats[1];
        double delta = newRating - mean;
        mean += delta/count;
        double delta2 = newRating - mean;
        m2 += delta * delta2;
        double standardDiv = Math.sqrt(m2/(count));

        return new double[] {mean, m2, standardDiv};
    }


    public TreeMap<String,Integer> getAllSongs(){return allSongs;}
    public TreeMap<String,Integer> getAllUsers(){return allUsers;}
    public TreeMap<String,double[]> getSongsAndRatings(){return songsAndRatings;}
}

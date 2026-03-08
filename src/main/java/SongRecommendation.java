import java.io.*;
import java.util.*;
import java.lang.*;



public class SongRecommendation implements mainInterface{
    private TreeMap<String,Integer> allSongs;
    private TreeMap<String,Integer> allUsers;
    private TreeMap<String, List<Integer>> usersIndividualRatings;
    private TreeMap<String, TreeMap<String,Integer>> UsersSongRatings = new TreeMap<>();
    private TreeMap<String, TreeMap<String, Integer>> cooperativeUsers;
    private TreeMap<String, Integer> cooperativeSongs;
    private TreeMap<String, TreeMap<String, Number>> songsUserRating;
    private List<String> kSongs;


    public SongRecommendation(){
        this.allSongs = new TreeMap<>();
        this.allUsers = new TreeMap<>();
        this.usersIndividualRatings = new TreeMap<>();
        this.UsersSongRatings = new TreeMap<>();
    }
    public void cooperativeCalculations(){
        this.cooperativeUsers = UserAnalysis.findingCooperativeUsers(usersIndividualRatings, UsersSongRatings);
        if(cooperativeUsers.size() < 2 || cooperativeUsers.isEmpty()){System.err.print("Error: There are too little cooperative users in this data."); return;}
        this.cooperativeSongs = UserAnalysis.findingCooperativeSongs(cooperativeUsers);
        if(cooperativeSongs.size() <= 2 || cooperativeSongs.isEmpty()){System.err.print("Error: There are not enough songs avaliable in the data."); return;}
    }
    public void songUserRatingCalculations(){
        cooperativeCalculations();
        UserPrediction temp = new UserPrediction();
        TreeMap<String, TreeMap<String, Number>> userPrediction = temp.userPred(this.cooperativeUsers, this.cooperativeSongs);
        this.songsUserRating = temp.formatingOutput(userPrediction);

    }

    public void kSongs(String[] args){
        this.kSongs = new ArrayList<>();
        for(int i = 3; i<args.length; i++){
            kSongs.add(args[i]);
        }
        checkKSongs(kSongs);
    }

    public void checkKSongs(List<String> kSongs){
        if(kSongs == null || kSongs.isEmpty()){
            System.err.println("Error: no user selection");
            return;
        }
        long distinctSongs = kSongs.stream().distinct().count();
        if(distinctSongs != kSongs.size()){
            System.err.println("Error: Duplicate requested song recommendation."); return;
        }

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
        try(FileWriter writer = new FileWriter(outputFile)){
            writer.write("user choice,recommendation\n");

            if( kSongs == null || kSongs.isEmpty()){
                System.err.println("Error: no songs selected to find recommendations."); return;
            }
            cooperativeCalculations();
            songUserRatingCalculations();
            kMeansClustering temp = new kMeansClustering();
            TreeMap<String, List<String>> clusters = temp.calculationKMeansClustering(songsUserRating, cooperativeUsers, kSongs);
            removeKSongs(clusters, kSongs);

            int indexOfKSong = 0;
            for(String clust : clusters.keySet()){
                List<String> recommendedSongsFromClust = clusters.get(clust);
                for(String recommend : recommendedSongsFromClust){
                    writer.write(kSongs.get(indexOfKSong) + "," + recommend + "\n");
                }
                indexOfKSong++;
            }

        }
        catch(Exception e){
            System.err.println("Error: unable to write to: " + outputFile);
            return;
        }


    } 

    public void removeKSongs(TreeMap<String, List<String>> clusters, List<String> kSongs){
        for(String clust : clusters.keySet()){
            List<String> clustSongs = clusters.get(clust);
            Iterator<String> interator = clustSongs.iterator();
            while(interator.hasNext()){
                String song = interator.next();
                if(isKSong(song, kSongs)){
                    interator.remove();
                }
            }
        }
    }

    public boolean isKSong(String song, List<String> kSong){
        for(String songs: kSong){
            if(song.equals(songs)){
                return true;
            }
        }
        return false;
    }



    public TreeMap<String,Integer> getAllSongs(){return allSongs;}
    public TreeMap<String,Integer> getAllUsers(){return allUsers;}
    public TreeMap<String, List<Integer>> getUsersIndividualRatings(){return usersIndividualRatings;}
    public TreeMap<String, TreeMap<String,Integer>> getUsersSongRatings(){return UsersSongRatings;}
    public TreeMap<String, TreeMap<String, Integer>> getCooperativeUsers(){return cooperativeUsers;}
    public TreeMap<String, Integer> getCooperativeSongs(){return cooperativeSongs;}
    public TreeMap<String,TreeMap<String,Number>> getSongUserRating(){return songsUserRating;}
    public List<String> getKSongs(){return kSongs;}
}
import java.io.*;
import java.util.*;
import java.lang.*;

public class ProximityPlaylist implements mainInterface{
    private TreeMap<String,Integer> allSongs;
    private TreeMap<String,Integer> allUsers;
    private TreeMap<String, List<Integer>> usersIndividualRatings;
    private TreeMap<String, TreeMap<String,Integer>> UsersSongRatings = new TreeMap<>();
    private TreeMap<String, TreeMap<String, Integer>> cooperativeUsers;
    private TreeMap<String, Integer> cooperativeSongs;
    private TreeMap<String, TreeMap<String, Number>> songsUserRating;
    private List<String> kSongs;
    private TreeMap<String, TreeMap<String, Double>>  userZRatingPerSong;


    public ProximityPlaylist(){
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
    public void zRatingCalculations(){
        TreeMap<String, TreeMap<String, Integer>> userZRatingSong = transposeZRatingData(cooperativeUsers);
        TreeMap<String, TreeMap<String, Double>> songZRatings = SongSimilarity.zRatingUsers(userZRatingSong);
        this.userZRatingPerSong = transposeZRatingsToUsers(songZRatings);
    }

    public TreeMap<String, TreeMap<String, Double>> transposeZRatingsToUsers(TreeMap<String, TreeMap<String, Double>> songZRating){
        TreeMap<String, TreeMap<String, Double>> tranposedData = new TreeMap<>();
        for(String song : songZRating.keySet()){
            for(String user : songZRating.get(song).keySet()){
                Double zRating = songZRating.get(song).get(user);
                tranposedData.computeIfAbsent(user, i -> new TreeMap<>()).put(song, zRating);
            }
        }
        return tranposedData;
    }

    public TreeMap<String, TreeMap<String, Integer>> transposeZRatingData(TreeMap<String, TreeMap<String, Integer>> userZRatingPerSong){
        TreeMap<String, TreeMap<String, Integer>> tranposedData = new TreeMap<>();
        for(String user : userZRatingPerSong.keySet()){
            for(String song : userZRatingPerSong.get(user).keySet()){
                Integer zRating = userZRatingPerSong.get(user).get(song);
                tranposedData.computeIfAbsent(song, i -> new TreeMap<>()).put(user, zRating);
            }
        }
        return tranposedData;
    }

    public void kSongs(String[] args){
        this.kSongs = new ArrayList<>();
        for(int i = 4; i<args.length; i++){
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
            if( kSongs == null || kSongs.isEmpty()){
                System.err.println("Error: no songs selected to find recommendations."); return;
            }
            cooperativeCalculations();
            songUserRatingCalculations();
            zRatingCalculations();
            kMeansClustering temp = new kMeansClustering();
            TreeMap<String, List<String>> clusters = temp.calculationKMeansClustering(songsUserRating, cooperativeUsers, kSongs);
            zRatingCalculations();

            TreeMap<Double, String> recommendedPlaylist = playlistCalculation(clusters, kSongs);

            if(recommendedPlaylist.keySet().size() > 20){ moreThan20SongsCSV(recommendedPlaylist, writer);}
            else{
                for(Double value : recommendedPlaylist.descendingKeySet()){
                    writer.write(recommendedPlaylist.get(value) + "\n");
                }
            }

        }
        catch(Exception e){
            e.printStackTrace();
            System.err.println("Error: unable to write to: " + outputFile);
            return;
        }
    }

    public void moreThan20SongsCSV(TreeMap<Double, String> recommendedPlaylist, FileWriter writer) throws IOException{
        int songsWritten = 0;

        for(Double value : recommendedPlaylist.descendingKeySet()){
            while(checkSongsWritten(songsWritten)){
                writer.write(recommendedPlaylist.get(value) + "\n");
                songsWritten++;
            }
        }
    }

    public boolean checkSongsWritten(int songsWritten){
        return songsWritten < 21;
    }

    public boolean isKSong(String song, List<String> kSong){
        for(String songs: kSong){
            if(song.equals(songs)){
                return true;
            }
        }
        return false;
    }

    public TreeMap<Double, String> playlistCalculation(TreeMap<String, List<String>> clusters, List<String> kSongs){
        TreeMap<Double, String> playlist = new TreeMap<>(); 

        TreeMap<String, Double> unsortedPlaylist = new TreeMap<>();
        for(String song : kSongs){
            List<String> clustSongs = findCluster(song, clusters);
            for(String cSong : clustSongs){
                if(song.equals(cSong) || kSongs.contains(cSong)){continue;}
                Double dist = 1.0/euclideanDist(cSong, song);

                Double old = unsortedPlaylist.getOrDefault(cSong, 0.0);
                Double best = Math.max(dist, old);
                unsortedPlaylist.put(cSong, best);
            }

        } 
        playlist = sortPlaylist(unsortedPlaylist);
        return playlist;
    }

    public List<String> findCluster(String song, TreeMap<String, List<String>> cluster){
        for(String clust : cluster.keySet()){
            List<String> clusterSong = cluster.get(clust);
            if(clusterSong.contains(song)){return clusterSong;}
            else{continue;}
        }

        System.err.println("Error: requested song does not exist in the data."); return null;
    }

    public Double euclideanDist(String cSong, String song){
        Double sum = 0.0;
        for(String user : userZRatingPerSong.keySet()){
            Double cSongZRating = userZRatingPerSong.get(user).get(cSong);
            Double songZRating = userZRatingPerSong.get(user).get(song);

            sum += (cSongZRating - songZRating) * (cSongZRating - songZRating);
        }
        return Math.sqrt(sum);
    }


    public TreeMap<Double, String> sortPlaylist(TreeMap<String, Double> unsortedPlaylist){
        TreeMap<Double, String> sortedPlaylist = new TreeMap<>();

        //https://docs.oracle.com/javase/8/docs/api/java/util/Map.Entry.html
        for (Map.Entry<String, Double> song : unsortedPlaylist.entrySet()) {
            sortedPlaylist.put(song.getValue(), song.getKey());
        }

        return sortedPlaylist;
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

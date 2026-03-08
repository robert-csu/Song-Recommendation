import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class kMeansClustering {

    private TreeMap<String, TreeMap<String, Integer>> cooperativeUsers;
    private TreeMap<String, Integer> cooperativeSongs;
    private TreeMap<String, TreeMap<String, Double>>  userZRatingPerSong;


    public TreeMap<String, List<String>> calculationKMeansClustering(TreeMap<String, TreeMap<String, Number>> songsUserRating, TreeMap<String, TreeMap<String, Integer>> cooperativeUsers, List<String> kSongs){
        this.cooperativeUsers = cooperativeUsers;
        this.cooperativeSongs = UserAnalysis.findingCooperativeSongs(cooperativeUsers);
        this.fillingUnpredictedRatings(songsUserRating,cooperativeUsers,cooperativeSongs);
        zRatingCalculations();

        return this.kClustering(songsUserRating, userZRatingPerSong, kSongs);
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

    public TreeMap<String, List<String>> kClustering(TreeMap<String, TreeMap<String, Number>> songsUserRating, TreeMap<String, TreeMap<String, Double>> userZRatingPerSong, List<String> kSongs){
        TreeMap<String, List<String>> clusters = new TreeMap<String, List<String>>();
        clusters = kMeansCluster(songsUserRating, this.cooperativeUsers, kSongs);

        return clusters;
    }

   public void addingData(TreeMap<String, TreeMap<String, Number>> songUserRating, TreeMap<String, TreeMap<String, Integer>> cooperativeUsers, TreeMap<String, Integer> cooperativeSongs){
        for(String song : cooperativeSongs.keySet()){
            TreeMap<String, Number> userRatings = songUserRating.computeIfAbsent(song, i -> new TreeMap<>());
            for(String user : cooperativeUsers.keySet()){
                if(!userRatings.containsKey(user)){
                    if(!cooperativeUsers.get(user).containsKey(song)){
                        userRatings.put(user, Double.NaN);
                    }
                    else{
                        userRatings.put(user, cooperativeUsers.get(user).get(song));
                    }
                }
                
            }
        }
    }

    public void fillingUnpredictedRatings(TreeMap<String, TreeMap<String, Number>> songUserRating, TreeMap<String, TreeMap<String, Integer>> cooperativeUsers, TreeMap<String, Integer> cooperativeSongs){
        addingData(songUserRating, cooperativeUsers, cooperativeSongs);
        for(String song : songUserRating.keySet()){
            TreeMap<String, Number> usersRatings = songUserRating.get(song);
            for(String user : usersRatings.keySet()){
                TreeMap<String, Integer> songRatings = cooperativeUsers.get(user);
                if(!checkNaN(songUserRating.get(song).get(user))){continue;}
                Integer newRating = predictedRating(song+","+user, songUserRating, cooperativeUsers);
                usersRatings.put(user, newRating);
                songRatings.put(song, newRating);
            }
        }

        for(String songs: songUserRating.keySet()){
            for(String users : songUserRating.get(songs).keySet()){
                Number rating = songUserRating.get(songs).get(users);
                if(!checkNaN(rating)){
                    cooperativeUsers.get(users).put(songs, rating.intValue());
                }
            }
        }
    }

    public Boolean checkNaN(Number rating){
        return Double.isNaN(rating.doubleValue());
    }

    public Integer predictedRating(String songUser, TreeMap<String, TreeMap<String, Number>> songUserRating, TreeMap<String, TreeMap<String, Integer>> cooperativeUsers){
        List<String> songUserSplit = SongSimilarity.splitString(songUser); String song = songUserSplit.get(0); String user = songUserSplit.get(1);
        Double songMean = songMeanCalculation(song, cooperativeUsers);
        double numSongRating = songNumCount(song, cooperativeUsers);
        Double userMean = userMeanCalculations(user, cooperativeUsers);
        double numUserRating = cooperativeUsers.get(user).size();

        int unpredicted = (int)Math.round((songMean * numSongRating + userMean * numUserRating) / (numSongRating + numUserRating));
        return (Integer) unpredicted;
    }

    public Double songMeanCalculation(String song, TreeMap<String, TreeMap<String, Integer>> cooperativeUsers){
        int total = 0;
        int count = 0;
        for(String user: cooperativeUsers.keySet()){
            if(cooperativeUsers.get(user).containsKey(song)){
                total += cooperativeUsers.get(user).get(song);
                count++;
            }
        }
        return total/ (double)count;
    }

    public double songNumCount(String song, TreeMap<String, TreeMap<String, Integer>> cooperativeUser){
        int count = 0;
        for(String user : cooperativeUser.keySet()){
            if(cooperativeUser.get(user).containsKey(song)){
                count++;
            }
        }
        return (double) count;
    }

    public Double userMeanCalculations(String user, TreeMap<String, TreeMap<String, Integer>> cooperativeUsers){
        int total = 0;
        for(String song : cooperativeUsers.get(user).keySet()){
            total += cooperativeUsers.get(user).get(song);
        }
        return total/ (double)cooperativeUsers.get(user).size();
    }

    public TreeMap<String, List<String>> kMeansCluster(TreeMap<String, TreeMap<String, Number>> songUserRating, TreeMap<String, TreeMap<String, Integer>> cooperativeUsers, List<String> kSongs){
        TreeMap<String, List<Double>> centroid = centroidCalculation(this.userZRatingPerSong, kSongs);
        TreeMap<String, List<String>> centroidCluster = clusterCalculation(centroid,songUserRating,this.userZRatingPerSong);
        return centroidCluster;
    }

    public TreeMap<String, List<String>> clusterCalculation(TreeMap<String, List<Double>> centroid, TreeMap<String, TreeMap<String, Number>> songUserRating, TreeMap<String, TreeMap<String, Double>> userZRatingPerSong){
        int iterations = 10;
        boolean convergence = false;
        TreeMap<String, List<Double>> songZRating = songZRatingCalculation(songUserRating);
        TreeMap<String, List<String>> cluster = clusterAssignment(centroid, songZRating);
        while(iterations != 0 && !convergence){
            TreeMap<String, List<Double>> newCentroid = new TreeMap<>();
            for(String cent : centroid.keySet()){
                List<Double> newIndividualCentroidValue = centroidRecalculation(cent, cluster, this.userZRatingPerSong);
                newCentroid.put(cent, newIndividualCentroidValue);
            }
            convergence = checkConvergence(centroid, newCentroid);
            centroid = newCentroid;
            cluster = clusterAssignment(centroid, songZRating);
            iterations--; 

        }
        return cluster;

    }
    
    public boolean checkConvergence(TreeMap<String, List<Double>> centroid, TreeMap<String, List<Double>> newCentroid){
        return newCentroid.equals(centroid);
    }
    
    public List<Double> centroidRecalculation(String centroid, TreeMap<String, List<String>> cluster, TreeMap<String, TreeMap<String, Double>> userZRatingPerSong){
        String clusterNumber = centroid.replace("centroid", "");
        List<String> clusterSongs = cluster.get("cluster"+clusterNumber);
        List<Double> newCentroid = new ArrayList<Double>();
        for(String user : userZRatingPerSong.keySet()){
            Double userZRatingTotal = 0.0;
            int songCount = 0;
            for(String song : clusterSongs){
                userZRatingTotal += userZRatingPerSong.get(user).get(song);
                songCount++;
            }
            newCentroid.add(userZRatingTotal/songCount);
        }
        return newCentroid;
    }

    public TreeMap<String, List<String>> clusterAssignment(TreeMap<String, List<Double>> centroid, TreeMap<String, List<Double>> songZRating){
        TreeMap<String, List<String>> cluster = clusterConstruction(centroid);
        for(String song : songZRating.keySet()){
            String closestCluster = euclideanDistSongCentroid(song, centroid, songZRating);
            cluster.get(closestCluster).add(song);
        }
        return cluster;
    }

    public String euclideanDistSongCentroid(String song, TreeMap<String, List<Double>> centroid, TreeMap<String, List<Double>> songZRating){
        List<Double> euclideanDistPerCentroid = new ArrayList<>();
        List<Double> songZRatings = songZRating.get(song);
        for(String cent : centroid.keySet()){
            List<Double> centroidValues = centroid.get(cent);
            Double euclideanValue = euclideanDistCalc(songZRatings, centroidValues);
            euclideanDistPerCentroid.add(euclideanValue);
        }
        int clusterNumber = findIndexOfMinEuclideanDist(euclideanDistPerCentroid); 
        return "cluster"+(clusterNumber+1);
    }

    public int findIndexOfMinEuclideanDist(List<Double> euclideanDistPerCentroid){
        int minIndex = 0;
        for(int i = 0; i<euclideanDistPerCentroid.size(); i++){
            if(euclideanDistPerCentroid.get(i)< euclideanDistPerCentroid.get(minIndex)){
                minIndex = i;
            }
        }
        return minIndex;
    }

    public Double euclideanDistCalc(List<Double> songZRatings, List<Double> centroidValue){
        Double sumSquared = 0.0;
        for(int i = 0; i<songZRatings.size(); i++){
            Double songMinCent = songZRatings.get(i) - centroidValue.get(i);
            sumSquared += Math.pow(songMinCent, 2);
        }
        return Math.sqrt(sumSquared);
    }

    public TreeMap<String, List<String>> clusterConstruction(TreeMap<String, List<Double>> centroid){
        TreeMap<String, List<String>> cluster = new TreeMap<>();
        int clusterNum = 1;
        for(int i = 0; i < centroid.size(); i++){
            cluster.computeIfAbsent("cluster"+clusterNum, k -> new ArrayList<>());
            clusterNum++;
        }
        return cluster;
    }

    public TreeMap<String, List<Double>> songZRatingCalculation(TreeMap<String, TreeMap<String, Number>> songUserRating){
        TreeMap<String, List<Double>> songZRating = new TreeMap<>();
        for(String song : songUserRating.keySet()){
            songZRating.put(song, findZRatingsPerSong(song, userZRatingPerSong));
        }
        return songZRating;
    }

    public TreeMap<String, List<Double>> centroidCalculation(TreeMap<String, TreeMap<String, Double>> userZRatingPerSong, List<String> kSongs){
        TreeMap<String, List<Double>> centroid = new TreeMap<>();
        int centroidNum = 1;
        for(String song : kSongs){
            List<Double> zRatingPerSong = findZRatingsPerSong(song, userZRatingPerSong);
            checkZRatingPerSong(zRatingPerSong);
            centroid.put("centroid"+centroidNum, zRatingPerSong);
            centroidNum++;
        }
        return centroid;
    }

    public void checkZRatingPerSong(List<Double> zRatingPerSong){
        if(zRatingPerSong.isEmpty() || zRatingPerSong.contains(Double.NaN)){
            System.err.println("Error: Selected song does not exist in the data set."); return;
        }
    }

    public Double zRatingAverage(List<Double> zRatingsPerSong){
        double total = 0.0;
        for(Double rating : zRatingsPerSong){
            total += rating;
        }
        return total/zRatingsPerSong.size();
    }

    public List<Double> findZRatingsPerSong(String song, TreeMap<String, TreeMap<String, Double>> userZRatingPerSong){
        List<Double> zScores = new ArrayList<>();
        for(String user : userZRatingPerSong.keySet()){
            Double score = userZRatingPerSong.get(user).get(song);
            if(score != null){
                zScores.add(score);
            }
        }
        return zScores;
    }


    public TreeMap<String, TreeMap<String, Integer>> getCooperativeUsers(){return cooperativeUsers;}
    public TreeMap<String, Integer> getCooperativeSongs(){return cooperativeSongs;}
    public TreeMap<String, TreeMap<String, Double>> getUserZRatingPerSong(){return userZRatingPerSong;}


}
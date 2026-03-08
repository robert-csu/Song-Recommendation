import java.io.*;
import java.util.*;
import java.lang.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class TestCs214Project {
    @TempDir
    Path tempDir;
    // Example of a JUnit5 test (this test can be removed)


    // CS214PROJECT TEST

    @Test
    public void testTooLittleArgs(){
        String[] args = {};

        PrintStream originalError = System.err;
        ByteArrayOutputStream actualError = new ByteArrayOutputStream();
        try{
            System.setErr(new PrintStream(actualError, true));
            Cs214Project.main(args);
        }
        finally{
            System.setErr(originalError);
        }
        String e = actualError.toString();
        
        assertEquals("Error: To little arguments, 2 or more is required", e);
    }

    @Test 
    public void inputFileNotACSV(){
        String[] args = {"inFile.txt", "outFile.csv"};

        PrintStream originalError = System.err;
        ByteArrayOutputStream actualError = new ByteArrayOutputStream();
        try{
            System.setErr(new PrintStream(actualError, true));
            Cs214Project.main(args);
        }
        finally{
            System.setErr(originalError);
        }
        String e = actualError.toString();
        
        assertEquals("Error: file type is incorrect- need csv for both input and output files:inFile.txt outFile.csv", e);
    }

    @Test
    public void outputFileNotACSV(){
        String[] args = {"inFile.csv", "outFile.txt"};

        PrintStream originalError = System.err;
        ByteArrayOutputStream actualError = new ByteArrayOutputStream();
        try{
            System.setErr(new PrintStream(actualError, true));
            Cs214Project.main(args);
        }
        finally{
            System.setErr(originalError);
        }
        String e = actualError.toString();
        
        assertEquals("Error: file type is incorrect- need csv for both input and output files:inFile.csv outFile.txt", e);
    }

    @Test
    public void userAnalysisCheckTest(){
        String[] args = {"inFile.csv", "outFile.csv", "-w"};

        PrintStream originalError = System.err;
        ByteArrayOutputStream actualError = new ByteArrayOutputStream();
        try{
            System.setErr(new PrintStream(actualError, true));
            Cs214Project.main(args);
        }
        finally{
            System.setErr(originalError);
        }
        String e = actualError.toString();
        
        assertEquals("Error: -w does not equal -a, -u, -p", e);
    }


    // CSVIO TESTS

    @Test
    public void emptyCSV_CsvIO()throws Exception{
        Path emptyFile = Files.createFile(tempDir.resolve("empty.csv"));

        mainInterface temp = new SongStats();

        PrintStream originalError = System.err;
        ByteArrayOutputStream actualError = new ByteArrayOutputStream();
        try{
            System.setErr(new PrintStream(actualError, true));
            CsvIO.readCSV(emptyFile.toString(), temp);
        }
        finally{
            System.setErr(originalError);
        }
        String e = actualError.toString();

        assertEquals("Error: Requires a csv with 3 fields.", e);
    }


    @Test
    public void notInt_CsvIO()throws Exception{
        Path inFile = Files.createFile(tempDir.resolve("in.csv"));

        Files.write(inFile, List.of("Bohemian Rhapsody,charlie,4",
                                    "Sweet Home Alabama,alex,4",
                                    "All Star,alex,NaN",
                                    "All Star,charlie,2",
                                    "Sweet Home Alabama,charlie,3",
                                    "All Star,cameron,5"));

        

        mainInterface temp = new SongStats();
        PrintStream originalError = System.err;
        ByteArrayOutputStream actualError = new ByteArrayOutputStream();
        try{
            System.setErr(new PrintStream(actualError, true));
            CsvIO.readCSV(inFile.toString(), temp);
        }
        finally{
            System.setErr(originalError);
        }
        String e = actualError.toString();

        assertEquals("Error: this rating is not an integer. NaN", e);
    }


    @Test
    public void notIntinRange_CsvIO()throws Exception{
        Path inFile = Files.createFile(tempDir.resolve("in.csv"));

        Files.write(inFile, List.of("Bohemian Rhapsody,charlie,4",
                                    "Sweet Home Alabama,alex,4",
                                    "All Star,alex,7"));

        
        
        mainInterface temp = new SongStats();
        PrintStream originalError = System.err;
        ByteArrayOutputStream actualError = new ByteArrayOutputStream();
        try{
            System.setErr(new PrintStream(actualError, true));
            CsvIO.readCSV(inFile.toString(), temp);
        }
        finally{
            System.setErr(originalError);
        }
        String e = actualError.toString();


        assertEquals("Error: ratings are out of scope {1-5} : 7", e);
    }


    @Test
    public void tooManyCSVFields_CsvIO()throws Exception{
        Path inFile = Files.createFile(tempDir.resolve("in.csv"));

        Files.write(inFile, List.of("Bohemian Rhapsody,charlie,4",
                                    "Sweet Home Alabama,alex,4",
                                    "All Star,alex,7,8"));

        

        mainInterface temp = new SongStats();
        PrintStream originalError = System.err;
        ByteArrayOutputStream actualError = new ByteArrayOutputStream();
        try{
            System.setErr(new PrintStream(actualError, true));
            CsvIO.readCSV(inFile.toString(), temp);
        }
        finally{
            System.setErr(originalError);
        }
        String e = actualError.toString();

        assertEquals("Error: Requires a csv with 3 fields.", e);
    }

    @Test
    public void nullCSVFields_CsvIO()throws Exception{
        Path inFile = Files.createFile(tempDir.resolve("in.csv"));

        Files.write(inFile, List.of("Bohemian Rhapsody,charlie,4",
                                    ",,4",
                                    "All Star,alex,1"));

        

        mainInterface temp = new SongStats();
        PrintStream originalError = System.err;
        ByteArrayOutputStream actualError = new ByteArrayOutputStream();
        try{
            System.setErr(new PrintStream(actualError, true));
            CsvIO.readCSV(inFile.toString(), temp);
        }
        finally{
            System.setErr(originalError);
        }
        String e = actualError.toString();

        assertEquals("Error: No data avaliable in the file.", e);
    }

    // SONG STATS TESTS
    @Test
    public void testAlgorithm_SongStat(){
        double[] test1MeanM2 ={0.0, 0.0};
        double[] test1 = SongStats.welfordOnlineAlgorithm(4, 1, test1MeanM2);
        double[] test2MeanM2 = {test1[0], test1[1]};
        double[] test2 = SongStats.welfordOnlineAlgorithm(2, 2, test2MeanM2);
        double[] test3MeanM2 = {test2[0], test2[1]};
        double[] test3 = SongStats.welfordOnlineAlgorithm(5, 3, test3MeanM2);

        assertEquals(3.666, test3[0], 1e-3);
        assertEquals(4.666, test3[1], 1e-3);
    }

    

    @Test 
    public void testWriteCSVHeader_SongStats(){
        SongStats temp = new SongStats();
        Path outFile = tempDir.resolve("out.csv");

        temp.acceptData("song1", "user1", 3);
        temp.acceptData("song2", "user2", 2);
        temp.acceptData("song2", "user1", 5);

        temp.writeCSV(outFile.toString());

        try(BufferedReader reader = new BufferedReader(new FileReader(outFile.toFile()))){
            String tempLineHold = reader.readLine();
            assertEquals("song,number of ratings,mean,standard deviation", tempLineHold);
        }
        catch(IOException e){
            return;
        }

    }

    @Test
    public void testWriteCSVContent_SongStats(){
        SongStats temp = new SongStats();
        Path outFile = tempDir.resolve("out.csv");

        temp.acceptData("song1", "user1", 3);
        temp.acceptData("song2", "user2", 2);
        temp.acceptData("song2", "user1", 5);

        temp.writeCSV(outFile.toString());


        try(BufferedReader reader = new BufferedReader(new FileReader(outFile.toFile()))){
            String delimiter = ",";
            String tempLineHold;
            String[] tempDataHold = new String[4];
            reader.readLine();
            tempLineHold = reader.readLine();

            tempDataHold = tempLineHold.split(delimiter, -1);

            assertEquals("song1", tempDataHold[0]);
            assertEquals("0.0", tempDataHold[3]);
        }
        catch(IOException e){
            return;
        }
    }

    @Test 
    public void testSingleUserMultipleRatings_SongStats(){
        SongStats temp = new SongStats();

        temp.acceptData("song1", "user1", 3);
        temp.acceptData("song2", "user1", 2);
        temp.acceptData("song2", "user1", 5);

        assertEquals(3, temp.getAllUsers().get("user1"));
    }

    // USER ANAYLSIS TESTS

    @Test
    public void acceptData_UserAnalysis(){
        UserAnalysis temp = new UserAnalysis();

        String song = "song1";
        String user = "user1";
        int rating = 5;

        temp.acceptData(song, user, rating);
        List<Integer> userStats = temp.getUsersIndividualRatings().get(user);

        assertEquals(1, temp.getAllSongs().get(song));
        assertNotNull(userStats);
        assertEquals(1, temp.getAllUsers().size());
    }

    @Test
    public void testSameSong_UserAnalysis(){
        UserAnalysis temp = new UserAnalysis();

        String song = "song1";
        String user = "user1";
        String user2 = "user2";

        temp.acceptData(song, user, 5);
        temp.acceptData(song, user2, 2);
        List<Integer> userStats = temp.getUsersIndividualRatings().get(user);

        assertEquals(2, temp.getAllSongs().get(song).intValue());
        assertNotNull(userStats);
    }

    @Test 
    public void testCooperativeUsers_UserAnalysis(){
        UserAnalysis temp = new UserAnalysis();
        TreeMap<String, List<Integer>> usersIndividualRatings = new TreeMap<>();
        TreeMap<String, TreeMap<String,Integer>> UsersSongRatings = new TreeMap<>();
        TreeMap<String, TreeMap<String, Integer>> cooperativeUsers = new TreeMap<>();

        List<Integer> user1 = new ArrayList<>();
        user1.add(5);
        user1.add(2);
        usersIndividualRatings.put("user1", user1);

        List<Integer> user2 = new ArrayList<>();
        user2.add(4);
        usersIndividualRatings.put("user2", user2);

        TreeMap<String, Integer> user1_map = new TreeMap<>();
        user1_map.put("Song1", 5);
        user1_map.put("Song2", 2);
        UsersSongRatings.put("user1", user1_map);

        TreeMap<String, Integer> user2_map = new TreeMap<>();
        user2_map.put("Song1", 4);
        UsersSongRatings.put("user2", user2_map);

        cooperativeUsers = temp.findingCooperativeUsers(usersIndividualRatings, UsersSongRatings);

        assertEquals(1, cooperativeUsers.size());
        TreeMap<String, Integer> tempMap = cooperativeUsers.get("user1");
        assertEquals(2, tempMap.size());
    }

    @Test 
    public void testCooperativeSongs_UserAnalysis(){
        UserAnalysis temp = new UserAnalysis();
        TreeMap<String, TreeMap<String, Integer>> cooperativeUsers = new TreeMap<>();
        TreeMap<String, Integer> cooperativeSongs = new TreeMap<>();

        TreeMap<String, Integer> user1 = new TreeMap<>();
        user1.put("Song1", 5);
        user1.put("Song2", 3);
        cooperativeUsers.put("user1", user1);

        TreeMap<String,Integer> user2 = new TreeMap<>();
        user2.put("Song1", 2);
        user2.put("Song3", 4);
        cooperativeUsers.put("user2", user2);

        cooperativeSongs = temp.findingCooperativeSongs(cooperativeUsers);

        assertEquals(3, cooperativeSongs.size());
        assertEquals(2, cooperativeSongs.get("Song1"));
    }

    @Test 
    public void testWriteCSVHeader_UserAnalysis(){
        UserAnalysis temp = new UserAnalysis();
        Path outFile = tempDir.resolve("out.csv");

        temp.acceptData("song1", "user1", 3);
        temp.acceptData("song2", "user2", 2);
        temp.acceptData("song2", "user1", 5);
        temp.acceptData("song1", "user2", 1);

        temp.writeCSV(outFile.toString());

        try(BufferedReader reader = new BufferedReader(new FileReader(outFile.toFile()))){
            String tempLineHold = reader.readLine();
            tempLineHold = reader.readLine();
            assertEquals("user1,song1,3", tempLineHold);
            tempLineHold = reader.readLine();
            assertEquals("user1,song2,5", tempLineHold);
        }
        catch(IOException e){
            return;
        }
    }

    // SONG SIMILARITY TESTS

    @Test 
    public void testAcceptData_SongSim(){
        SongSimilarity temp = new SongSimilarity();

        String song = "song1";
        String user = "user1";
        int rating = 5;

        temp.acceptData(song, user, rating);
        List<Integer> userStats = temp.getUsersIndividualRatings().get(user);

        assertEquals(1, temp.getAllSongs().get(song));
        assertNotNull(userStats);
        assertEquals(1, temp.getAllUsers().size());

    }

    @Test
    public void testSameSong_SongSim(){
        SongSimilarity temp = new SongSimilarity();

        String song = "song1";
        String user = "user1";
        String user2 = "user2";

        temp.acceptData(song, user, 5);
        temp.acceptData(song, user2, 2);
        List<Integer> userStats = temp.getUsersIndividualRatings().get(user);

        assertEquals(2, temp.getAllSongs().get(song).intValue());
        assertNotNull(userStats);
    }

    @Test
    public void testZRatingUsers_SongSim(){
        TreeMap<String, TreeMap<String, Integer>> cooperativeUsers = new TreeMap<>();

        TreeMap<String, Integer> user1Songs = new TreeMap<>();
        user1Songs.put("song1", 4);
        user1Songs.put("song2", 3);
        cooperativeUsers.put("user1", user1Songs);

        TreeMap<String, Integer> user2Songs = new TreeMap<>();
        user2Songs.put("song1", 5);
        user2Songs.put("song2", 1);
        cooperativeUsers.put("user2", user2Songs);

        TreeMap<String, TreeMap<String, Double>> userZRatingPerSong = SongSimilarity.zRatingUsers(cooperativeUsers);

        TreeMap<String,Double> user1Stats = userZRatingPerSong.get("user1");
        assertEquals(1, user1Stats.get("song1"));
        assertEquals(-1, user1Stats.get("song2"));

        TreeMap<String, Double> user2Stats = userZRatingPerSong.get("user2");
        assertEquals(1, user2Stats.get("song1"));
        assertEquals(-1, user2Stats.get("song2"));
    }

    @Test
    public void testUserMeanStd_SongSim(){
        TreeMap<String, TreeMap<String, Integer>> cooperativeUsers = new TreeMap<>();

        TreeMap<String, Integer> user1Songs = new TreeMap<>();
        user1Songs.put("song1", 4);
        user1Songs.put("song2", 3);
        cooperativeUsers.put("user1", user1Songs);

        TreeMap<String, Integer> user2Songs = new TreeMap<>();
        user2Songs.put("song1", 5);
        user2Songs.put("song2", 1);
        cooperativeUsers.put("user2", user2Songs);

        TreeMap<String, List<Double>> userMeansStd = SongSimilarity.userMeanStd(cooperativeUsers);

        List<Double> user1Stats = userMeansStd.get("user1");
        assertEquals(3.5, user1Stats.get(0));
        assertEquals(0.5, user1Stats.get(1));

        List<Double> user2Stats = userMeansStd.get("user2");
        assertEquals(3.0, user2Stats.get(0));
        assertEquals(2.0, user2Stats.get(1));
    }

    @Test
    public void testFindUserMean_SongSim(){
        TreeMap<String, TreeMap<String, Integer>> cooperativeUsers = new TreeMap<>();

        TreeMap<String, Integer> user1Songs = new TreeMap<>();
        user1Songs.put("song1", 4);
        user1Songs.put("song2", 3);
        cooperativeUsers.put("user1", user1Songs);

        TreeMap<String, Integer> user2Songs = new TreeMap<>();
        user2Songs.put("song1", 5);
        user2Songs.put("song2", 1);
        cooperativeUsers.put("user2", user2Songs);

        TreeMap<String, Double> userMeans = SongSimilarity.findingUserMean(cooperativeUsers);

        assertEquals(3.5, userMeans.get("user1"));
        assertEquals(3, userMeans.get("user2"));
    }

    @Test
    public void testLinearSpanEmpty_SongSim(){
        TreeMap<String, Integer> cooperativeSongs = new TreeMap<>();
        List<String> linearSpanOfAllCoopSongs = SongSimilarity.linearSpan(cooperativeSongs);
        assertEquals(0, linearSpanOfAllCoopSongs.size());

    }

    @Test
    public void testLinearSpanOneSong_SongSim(){
        TreeMap<String, Integer> cooperativeSongs = new TreeMap<>();
        cooperativeSongs.put("song1", 1);
        List<String> linearSpanOfAllCoopSongs = SongSimilarity.linearSpan(cooperativeSongs);
        assertEquals(0, linearSpanOfAllCoopSongs.size());
    }

    @Test
    public void testLinearSpan_SongSim(){
        TreeMap<String, Integer> cooperativeSongs = new TreeMap<>();
        cooperativeSongs.put("song1", 1);
        cooperativeSongs.put("song2", 3);
        cooperativeSongs.put("song3", 1);
        List<String> linearSpanOfAllCoopSongs = SongSimilarity.linearSpan(cooperativeSongs);
        assertEquals(3, linearSpanOfAllCoopSongs.size());
    }

    @Test
    public void testEuclideanDistEmpty_SongSim(){
        TreeMap<String, TreeMap<String, Double>> userZRatingPerSong = new TreeMap<>();
        TreeMap<String, Integer> cooperativeSongs = new TreeMap<>();
        TreeMap<String, Double> euclideanDistancePerPair = SongSimilarity.euclideanDistance(userZRatingPerSong, cooperativeSongs);
        assertEquals(0, euclideanDistancePerPair.size());
    }

    @Test
    public void testEuclideanDistOneSong_SongSim(){
        TreeMap<String, TreeMap<String, Double>> userZRatingPerSong = new TreeMap<>();
        TreeMap<String, Double> zRating = new TreeMap<>();
        zRating.put("song1", 1.0);
        userZRatingPerSong.put("user1", zRating);
        TreeMap<String, Integer> cooperativeSongs = new TreeMap<>();
        cooperativeSongs.put("song1", 1);
        TreeMap<String, Double> euclideanDistancePerPair = SongSimilarity.euclideanDistance(userZRatingPerSong, cooperativeSongs);

        assertEquals(0, euclideanDistancePerPair.size());
    }

    @Test
    public void testEuclideanDistMultiSong_SongSim(){
        TreeMap<String, TreeMap<String, Double>> userZRatingPerSong = new TreeMap<>();
        TreeMap<String, Double> zRating = new TreeMap<>();
        zRating.put("song1", 1.0);
        zRating.put("song2", 3.0);
        userZRatingPerSong.put("user1", zRating);
        TreeMap<String, Integer> cooperativeSongs = new TreeMap<>();
        cooperativeSongs.put("song1", 1);
        cooperativeSongs.put("song2", 1);
        TreeMap<String, Double> euclideanDistancePerPair = SongSimilarity.euclideanDistance(userZRatingPerSong, cooperativeSongs);

        assertEquals(2.0, euclideanDistancePerPair.get("song1,song2"));
    }

    @Test
    public void testEuclideanDistNAN_SongSim(){
        TreeMap<String, TreeMap<String, Double>> userZRatingPerSong = new TreeMap<>();
        TreeMap<String, Double> zRatingUser1 = new TreeMap<>();
        zRatingUser1.put("song1", 1.0);
        userZRatingPerSong.put("user1", zRatingUser1);
        TreeMap<String, Double> zRatingUser2 = new TreeMap<>();
        zRatingUser2.put("song2", 3.0);
        TreeMap<String, Integer> cooperativeSongs = new TreeMap<>();
        cooperativeSongs.put("song1", 1);
        cooperativeSongs.put("song2", 1);
        TreeMap<String, Double> euclideanDistancePerPair = SongSimilarity.euclideanDistance(userZRatingPerSong, cooperativeSongs);

        assertEquals(Double.NaN, euclideanDistancePerPair.get("song1,song2"));
    }

    @Test
    public void testSplitString_SongSim(){
        String song1song2 = "Devil in a New Dress,Pokerface";
        List<String> tempList = SongSimilarity.splitString(song1song2);

        assertEquals("Devil in a New Dress", tempList.get(0));
        assertEquals("Pokerface", tempList.get(1));
    }

    @Test
    public void testWriteCSVHeader_SongSim(){
        SongSimilarity temp = new SongSimilarity();
        Path outFile = tempDir.resolve("out.csv");

        temp.acceptData("song1", "user1", 3);
        temp.acceptData("song2", "user2", 2);
        temp.acceptData("song2", "user1", 5);

        temp.writeCSV(outFile.toString());

        try(BufferedReader reader = new BufferedReader(new FileReader(outFile.toFile()))){
            String tempLineHold = reader.readLine();
            assertEquals("name1,name2,similarity", tempLineHold);
        }
        catch(IOException e){
            return;
        }
    }


    // USER PREDICTION

    @Test 
    public void testAcceptData_UserPrediction(){
        UserPrediction temp = new UserPrediction();

        String song = "song1";
        String user = "user1";
        int rating = 5;

        temp.acceptData(song, user, rating);
        List<Integer> userStats = temp.getUsersIndividualRatings().get(user);

        assertEquals(1, temp.getAllSongs().get(song));
        assertNotNull(userStats);
        assertEquals(1, temp.getAllUsers().size());
    }

    @Test 
    public void testNormPrediction_UserPrediction(){
        UserPrediction temp = new UserPrediction();
        Double lowRating = 0.5;
        Double highRating = 7.0;
        Double midRating = 3.14;

        Integer lowRatingResult = temp.normPrediction(lowRating);
        assertEquals(lowRatingResult, 1);
        Integer highRatingResult = temp.normPrediction(highRating);
        assertEquals(highRatingResult, 5);
        Integer midRatingResult = temp.normPrediction(midRating);
        assertEquals(midRatingResult, 3);
    }


    @Test 
    public void testSimilarUserCheck_UserPrediction(){
        UserPrediction temp = new UserPrediction();
        String similarUser = "";
        boolean testResult = temp.similarUserCheck(similarUser);
        assertTrue(testResult);

        String similarUser2 = "user1";
        boolean testResult2 = temp.similarUserCheck(similarUser2);
        assertFalse(testResult2);
    }

    @Test
    public void testCheckNaN_UserPrediction(){
        UserPrediction temp = new UserPrediction();
        assertTrue(temp.checkNaN(2,5));
        assertFalse(temp.checkNaN(2,null));
        assertFalse(temp.checkNaN(null,5));
    }


    @Test
    public void testEuclideanDistance_UserPrediction(){
        UserPrediction temp = new UserPrediction();

        TreeMap<String, Integer> coopSongs = new TreeMap<>();
        coopSongs.put("song1", 1);
        coopSongs.put("song2", 1);
        coopSongs.put("song3", 1);

        TreeMap<String, TreeMap<String, Integer>> coopUsers = new TreeMap<>();

        TreeMap<String,Integer> user1 = new TreeMap<>();
        user1.put("song1", 5);
        user1.put("song2", 3);
        user1.put("song3", 2);

        TreeMap<String, Integer> user2 = new TreeMap<>();
        user2.put("song1", 1);
        user2.put("song2", 4);
        user2.put("song3", 2);

        coopUsers.put("user1", user1);
        coopUsers.put("user2", user2);

        Double normalEuclideanDistance = temp.euclideanDistance("user1,user2", coopSongs, coopUsers);
        assertEquals(Math.sqrt(17), normalEuclideanDistance);


        TreeMap<String, Integer> user3 = new TreeMap<>();
        user3.put("song1", 3);
        user3.put("song2", null);
        user3.put("song3", 4);

        TreeMap<String, Integer> user4 = new TreeMap<>();
        user4.put("song1", null);
        user4.put("song2", 1);
        user4.put("song3", 5);

        coopUsers.put("user3", user3);
        coopUsers.put("user4", user4);

        Double oneSongEuclideanDistance = temp.euclideanDistance("user3,user4", coopSongs, coopUsers);
        assertEquals(Math.sqrt(1), oneSongEuclideanDistance);


        TreeMap<String,Integer> user5 = new TreeMap<>();
        user5.put("song1", null);
        user5.put("song2", null);
        user5.put("song3", null);

        coopUsers.put("user5", user5);

        Double noSimSongsEuclideanDistance = temp.euclideanDistance("user1,user5", coopSongs, coopUsers);
        assertTrue(noSimSongsEuclideanDistance.isNaN());


        Double sameSongsEuclideanDistance = temp.euclideanDistance("user1,user1", coopSongs, coopUsers);
        assertEquals(0, sameSongsEuclideanDistance);
    }

    @Test
    public void testMostSimUser_UserPrediction(){
        UserPrediction temp = new UserPrediction();

        TreeMap<String, TreeMap<String, Double>> userSims = new TreeMap<>();
        TreeMap<String, Double> individualSimularity = new TreeMap<>();
        individualSimularity.put("user2", 2.5);
        individualSimularity.put("user3", 5.0);

        userSims.put("user1", individualSimularity);

        temp.acceptData("song1", "user2", 4);
        temp.acceptData("song2", "user2", 1);
        temp.acceptData("song2", "user3", 3);
        temp.acceptData("song3", "user3", 5);
        temp.cooperativeCalculations();

        String similarUser = temp.mostSimilarUser(userSims, "user1", "song1");
        assertEquals("user2", similarUser);
    }

    @Test
    public void testPredictedRating_UserPrediction(){
        UserPrediction temp = new UserPrediction();

        TreeMap<String, List<Double>> userMeanStd = new TreeMap<>();
        userMeanStd.put("user1", Arrays.asList(5.0, 1.0));


        TreeMap<String,TreeMap<String, Double>> userZRating = new TreeMap<>();
        TreeMap<String,Double> user2ZRating = new TreeMap<>();
        user2ZRating.put("song2", 1.0);
        userZRating.put("user2", user2ZRating);

        List<TreeMap<?,?>> genericMap = new ArrayList<>();
        genericMap.add(userMeanStd);
        genericMap.add(userZRating);


        int predictedRating = temp.predictedRating(genericMap, "song2,user1,user2");
        assertEquals(5, predictedRating);
    }

    // SongRecommendation Tests

    @Test 
    public void testAcceptData_SongRec(){
        SongRecommendation temp = new SongRecommendation();

        String song = "song1";
        String user = "user1";
        int rating = 5;

        temp.acceptData(song, user, rating);
        List<Integer> userStats = temp.getUsersIndividualRatings().get(user);

        assertEquals(1, temp.getAllSongs().get(song));
        assertNotNull(userStats);
        assertEquals(1, temp.getAllUsers().size());
    }

    @Test
    public void testWriteCSVHeader_SongRec(){
        SongRecommendation temp = new SongRecommendation();
        Path outFile = tempDir.resolve("out.csv");

        temp.acceptData("song1", "user1", 3);
        temp.acceptData("song2", "user1", 2);
        temp.acceptData("song3", "user1", 5);
        temp.acceptData("song1", "user2", 1);
        temp.acceptData("song2", "user2", 3);


        temp.writeCSV(outFile.toString());

        try(BufferedReader reader = new BufferedReader(new FileReader(outFile.toFile()))){
            String tempLineHold = reader.readLine();
            assertEquals("user choice,recommendation", tempLineHold);
        }
        catch(IOException e){
            return;
        }
    }

    @Test
    public void testKSongs_SongRec(){
        SongRecommendation temp = new SongRecommendation();

        String[] args = {"inputFile", "outputFile", "-r", "song1", "song2", "song3"};
        temp.kSongs(args);
        List<String> kSongs = temp.getKSongs();
        int kSongSize = kSongs.size();
        assertEquals(kSongSize, 3);
    }

    @Test 
    public void testIsKSong_SongRec(){
        SongRecommendation temp = new SongRecommendation();

        String song = "song1";
        List<String> kSongs =  Arrays.asList("song1", "song2", "song3");

        boolean result = temp.isKSong(song, kSongs);

        assertTrue(result);
    }

    @Test
    public void testFullPassThrough_SongRec(){
        SongRecommendation temp = new SongRecommendation();
        Path outFile = tempDir.resolve("output.csv");
        String[] args = {"inputFile", "outputFile", "-r", "song3", "song5", "song6"};
        temp.kSongs(args);

        temp.acceptData("song1", "user1", 2);
        temp.acceptData("song2", "user1", 3);
        temp.acceptData("song4", "user1", 4);
        temp.acceptData("song1", "user2", 2);
        temp.acceptData("song2", "user2", 3);
        temp.acceptData("song4", "user2", 5);
        temp.acceptData("song1", "user3", 4);
        temp.acceptData("song3", "user3", 5);
        temp.acceptData("song4", "user3", 4);
        temp.acceptData("song5", "user3", 5);
        temp.acceptData("song1", "user4", 1);
        temp.acceptData("song3", "user4", 2);
        temp.acceptData("song4", "user4", 1);
        temp.acceptData("song5", "user5", 3);
        temp.acceptData("song6", "user5", 2);

         temp.writeCSV(outFile.toString());

        try(BufferedReader reader = new BufferedReader(new FileReader(outFile.toFile()))){
            String tempLineHold = reader.readLine();
            assertEquals("user choice,recommendation", tempLineHold);
            tempLineHold = reader.readLine();
            assertEquals("song3,song4", tempLineHold);
            tempLineHold = reader.readLine();
            assertEquals("song6,song1", tempLineHold);
            tempLineHold = reader.readLine();
            assertEquals("song6,song2", tempLineHold);
        }
        catch(IOException e){
            return;
        }

       


    }







           
}
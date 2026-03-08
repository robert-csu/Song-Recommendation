/*
 * cs214Project purpose:
 *      its like the brain of the operation
 *      tells which class main will be assigned to
 */


import java.io.*;
import java.lang.*;

public class Cs214Project {
    private static String inputFile;
    private static String outputFile;

    public static void main(String[] args) {
        if(!checkArgs(args)){return;}
        mainInterface main = null;
        inputFile = args[0]; outputFile = args[1];
        if(!endWithCSV(inputFile, outputFile)){System.err.print("Error: file type is incorrect- need csv for both input and output files:" + inputFile + " " +outputFile); return;}

        main = getMainApplication(args, main);

        if(main == null){return;}
        CsvIO.readCSV(inputFile, main);
        main.writeCSV(outputFile);

    }

    public static boolean checkArgs(String[] args){
        if(args.length == 0){
            System.err.print("Error: To little arguments, 2 or more is required"); return false;
        }
        return true;
    }

    public static mainInterface getMainApplication(String[] args, mainInterface main){
        if(args.length == 2){
            main =  new SongStats();
        }
        else if(args.length == 3){
            main = threeArgSelection(args[2], main);
        }
        else if(args.length > 3){
            if(songRecommendationCheck(args[2])){
                main = new SongRecommendation(); ((SongRecommendation)main).kSongs(args);
            }
            else if(proximityPlaylistCheck(args[2])){
                main = new ProximityPlaylist(); ((ProximityPlaylist) main).kSongs(args);checkKSongs(args, main);
            }
            
        }

        return main;
    }

    public static mainInterface threeArgSelection(String arg, mainInterface main){
        if(userAnalysisCheck(arg)){main = new UserAnalysis();}
        else if(songSimilarityCheck(arg)){main = new SongSimilarity();}
        else if(userPredictionCheck(arg)){main = new UserPrediction();}
        else if(songRecommendationCheck(arg)){System.err.println("Error: No song selected");return null;}
        else{System.err.print("Error: " + arg + " does not equal -a, -u, -p"); return null;}

        return main;
    }


    // Both files must have a .csv tail so we can write to it
    public static boolean endWithCSV(String inputFile, String outputFile){
        if(inputFile.endsWith(".csv") && outputFile.endsWith(".csv")){
            return true;
        }
        return false;
    }

    // Checks arg 3 to see if it is userAnalysis problem
    public static boolean userAnalysisCheck(String check){
        if(check.equals("-a")){
            return true;
        }
        return false;
    }

    // Checks arg 3 to see if it is a SongSim problem
    public static boolean songSimilarityCheck(String check){
        if(check.equals("-u")){
            return true;
        }
        return false;
    }

    public static boolean userPredictionCheck(String check){
        if(check.equals("-p")){
            return true;
        }
        return false;
    }

    public static boolean songRecommendationCheck(String check)
    {
        if(check.equals("-r")){
            return true;
        }
        return false;
    }

    public static boolean proximityPlaylistCheck(String check){
        if(check.equals("-s")){
            return true;
        }
        return false;
    }

    public static boolean checkKSongs(String[] args, mainInterface main){
        int numOfKSong = Integer.parseInt(args[3]);
        int mainKSongs = ((ProximityPlaylist) main).getKSongs().size();

        if(numOfKSong != mainKSongs){
            System.err.println("Error: incorrect input of songs.");
            return false;
        }
        return true;
    }
}
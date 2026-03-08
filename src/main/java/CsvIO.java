/*
 * CsvIO purpose:
 *      every assignment I need to read from a csv with hella error handeling checks
 *      therefore I created this class to do just that
 *      it returns per line, so if the csv line is valid, it tells main to accept the data
 */


import java.io.*;
import java.lang.*;
import java.nio.file.*;

public class CsvIO {

    // call an .acceptData method (SongStat class || UserAnaylsis class)
    public static void readCSV(String inputFile, mainInterface main){
        try(BufferedReader reader = new BufferedReader(new FileReader(inputFile))){
            if(emptyFileCheck(inputFile)){System.err.print("Error: Requires a csv with 3 fields.");return;}
            Files.lines(Paths.get(inputFile)).forEach(line -> processingData(line, main));
        }
        catch(IOException e){
            System.err.println("Error: unable to read from: " + inputFile);
            return;
        }

    }

    public static void processingData(String line, mainInterface main){
        String delimiter = ",";
        String user; String song; int rating;
        String[] tempDataHold = line.split(delimiter, -1);
        if(!threeFieldsCheck(tempDataHold)){System.err.print("Error: Requires a csv with 3 fields.");return;}
        song = tempDataHold[0]; user = tempDataHold[1]; 
        if(nullFieldsCheck(song, user)){System.err.print("Error: No data avaliable in the file.");return;}
        if(!integerCheck(tempDataHold[2])){return;}
        rating = Integer.parseInt(tempDataHold[2]);
        if(!ratingRangeCheck(rating)){System.err.print("Error: ratings are out of scope {1-5} : " + rating);return;}
        main.acceptData(song, user, rating);
    }

    // checks if the file is empty
    public static boolean emptyFileCheck(String inputFile){
        if(new File(inputFile).length() == 0){
            return true;
        }
        return false;

    }

    // check if there are 3 fields in the csv line
    public static boolean threeFieldsCheck(String[] fields){
        if(fields == null || fields.length != 3){
            return false;
        }
        return true;
    }

    // checks if any of the fields are null
    public static boolean nullFieldsCheck(String song, String user){
        if(song == null || song.trim().isEmpty() || user == null || user.trim().isEmpty()){
            return true;
        }
        return false;
    }

    // checks if the rating is an integer
    public static boolean integerCheck(String rawRating){
        try{
            Integer.parseInt(rawRating);
            return true;
        }
        catch(NumberFormatException e){
            System.err.print("Error: this rating is not an integer. " + rawRating);
            return false;
        }
    }

    // check if the rating is in range of 1-5
    public static boolean ratingRangeCheck(int rating){
        if(rating < 1 || rating > 5){
            return false;
        }
        return true;
    }
    
    
}
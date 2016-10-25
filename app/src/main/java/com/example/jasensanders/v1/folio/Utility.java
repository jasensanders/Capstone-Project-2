package com.example.jasensanders.v1.folio;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.example.jasensanders.v1.folio.database.DataContract;

import java.util.ArrayList;

/**
 * Created by Jasen Sanders on 002,03/02/16.
 */
public class Utility {

    static public void setPreference(Context c, String key, String value){
        SharedPreferences settings = c.getSharedPreferences(MainActivity.MAIN_PREFS,0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.commit();
    }

    static public String getPreference(Context c, String key, String deFault){
        SharedPreferences settings = c.getSharedPreferences(MainActivity.MAIN_PREFS, 0);

        return settings.getString(key, deFault);
    }
    //Checks for internet connectivity
    static public boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
    //Checks if the String is numeric
    static public boolean isNum(String s) {
        int len = s.length();
        for (int i = 0; i < len; ++i) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    //Checks if  the string is numeric and the right length for a UPC barcode.
    static public boolean isValid(String s) {
        if(isNum(s)){
            //s.length() == 10 || s.length() == 13
            if(s.length() ==12){
                return true;
            }
        }
        return false;
    }

    static public String[] parseProductName(String productName){
        String name = "";
        String format = "";

        //Strips Product name from String
        if(productName.contains(" (")) {
            String[] tempParens = productName.split(" \\(");
            name = tempParens[0];
        }
        if(productName.contains(" [")){
            String[] tempParens = productName.split(" \\[");
            name = tempParens[0];
        }

        //Finds all formats in productName string and appends to format string
        String[] match = {"Blu-ray", "DVD", "Digital Copy", "Digital HD"};
        //Dumps all matches into arraylist
        ArrayList<String> matched = new ArrayList<>();
        for(int i = 0; i < match.length; i++){
            if(productName.contains(match[i])){
                matched.add(match[i]);
            }
        }
        //Turns arraylist into String with dashes adding to format String
        for(int i = 0; i < matched.size(); i++) {
            if (i == matched.size() - 1) {
                format = format + matched.get(i);
            } else {
                format = format + matched.get(i) + " - ";
            }
        }


        return new String[]{name, format};
    }

    public static String stringArrayToString(String[] inString){
        StringBuilder result = new StringBuilder();
        for(String add: inString){
            result.append(add + ", ");
        }

        return result.toString();
    }

    public static String dateToYear(String date){
        String[] temp;
        try{
            temp = date.split("-");
            return temp[0];
        }catch (NullPointerException e){
            Log.e("Utility class","Error splitting year");
            return "XXXX";
        }

    }

    public static String addDateToYear(String date){
        String[] temp;
        try{
            temp = date.split(", ");
            return temp[1];
        }catch (NullPointerException e){
            Log.e("Utility class","Error splitting addDate year");
            return "XXXX";
        }

    }

     public static String normalizeRating(String rating){
        final int standard = 5;
        int len = rating.length();
        String pad = "";
        int diff = standard - len;
        if(diff <= 0){
            return rating;
        }else{
            for(int i=0; i<diff; i++){
                pad = pad + " ";
            }
            return rating + pad;
        }

    }

    public static ContentValues makeRowValues(String[] input){

        //        FullRow = new String[]{TMDB_MOVIE_ID, UPC_CODE, MOVIE_THUMB, POSTER_URL,
//                ART_IMAGE_URL, BARCODE_URL, TITLE, RELEASE_DATE, RUNTIME, ADD_DATE, FORMATS, STORE,
//                NOTES, STATUS, RATING, SYNOPSIS, TRAILERS, GENRES};

        ContentValues insertMovie = new ContentValues();
        insertMovie.put(DataContract.MovieEntry.COLUMN_MOVIE_ID, input[0]);
        insertMovie.put(DataContract.MovieEntry.COLUMN_UPC,input[1]);
        insertMovie.put(DataContract.MovieEntry.COLUMN_THUMB,input[2]);
        insertMovie.put(DataContract.MovieEntry.COLUMN_M_POSTERURL, input[3]);
        insertMovie.put(DataContract.MovieEntry.COLUMN_DISC_ART,input[4]);
        insertMovie.put(DataContract.MovieEntry.COLUMN_BARCODE,input[5]);
        insertMovie.put(DataContract.MovieEntry.COLUMN_M_TITLE, input[6]);
        insertMovie.put(DataContract.MovieEntry.COLUMN_M_DATE, input[7]);
        insertMovie.put(DataContract.MovieEntry.COLUMN_RUNTIME,input[8]);
        insertMovie.put(DataContract.MovieEntry.COLUMN_ADD_DATE,input[9]);
        insertMovie.put(DataContract.MovieEntry.COLUMN_FORMATS,input[10]);
        insertMovie.put(DataContract.MovieEntry.COLUMN_STORE, input[11]);
        insertMovie.put(DataContract.MovieEntry.COLUMN_NOTES, input[12]);
        insertMovie.put(DataContract.MovieEntry.COLUMN_STATUS, input[13]);
        insertMovie.put(DataContract.MovieEntry.COLUMN_RATING, input[14]);
        insertMovie.put(DataContract.MovieEntry.COLUMN_M_SYNOPSIS, input[15]);
        insertMovie.put(DataContract.MovieEntry.COLUMN_M_TRAILERS, input[16]);
        insertMovie.put(DataContract.MovieEntry.COLUMN_M_GENRE, input[17]);
        return insertMovie;
    }

    public static ContentValues makeUpdateValues(String store, String notes, String status){
        ContentValues updateMovie = new ContentValues();
        updateMovie.put(DataContract.MovieEntry.COLUMN_STORE, store);
        updateMovie.put(DataContract.MovieEntry.COLUMN_NOTES, notes);
        if(status != null) {
            updateMovie.put(DataContract.MovieEntry.COLUMN_STATUS, status);
        }
        return updateMovie;
    }
}

package com.example.jasensanders.v1.folio.database;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Jasen Sanders on 10/5/2016.
 */

public class DataContract {

    public static final String CONTENT_AUTHORITY ="com.example.jasensanders.v1.folio";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //Primitive paths
    public static final String PATH_MOVIES = "movie";
    public static final String PATH_OWNED_MOVIES = "movie/owned";
    public static final String PATH_WISH_LIST = "movie/wish";
    public static final String PATH_SEARCH = "movie/search";

    //Segments
    public static final String SEG_OWNED = "owned";
    public static final String SEG_WISHLIST = "wish";
    public static final String SEG_SEARCH = "search";
    public static final String SEG_UPC = "upc";
    public static final String SEG_MID = "mid";
    public static final String SEG_TITLE = "title";
    public static final String SEG_YEAR = "year";

    //Full Paths
    public static final String PATH_OWNED_MOVIES_UPC = "movie/owned/upc";
    public static final String PATH_OWMNED_MOVIES_MID = "movie/owned/mid";

    public static final String PATH_WISH_LIST_UPC = "movie/wish/upc";
    public static final String PATH_WISH_LIST_MID = "movie/wish/mid";

    public static final String PATH_SEARCH_UPC = "movie/search/upc";
    public static final String PATH_SEARCH_TITLE = "movie/search/title";
    public static final String PATH_SEARCH_YEAR = "movie/search/year";

    //Status Flags
    public static final String STATUS_OWNED_MOVIES = "OWNED";
    public static final String STATUS_WISH_LIST = "WISH";

    //Projection full row
    public static final String[] MOVIE_COLUMNS = {
            MovieEntry._ID,
            MovieEntry.COLUMN_MOVIE_ID,
            MovieEntry.COLUMN_UPC,
            MovieEntry.COLUMN_THUMB,
            MovieEntry.COLUMN_M_POSTERURL,
            MovieEntry.COLUMN_DISC_ART,
            MovieEntry.COLUMN_BARCODE,
            MovieEntry.COLUMN_M_TITLE,
            MovieEntry.COLUMN_M_DATE,
            MovieEntry.COLUMN_RUNTIME,
            MovieEntry.COLUMN_ADD_DATE,
            MovieEntry.COLUMN_FORMATS,
            MovieEntry.COLUMN_STORE,
            MovieEntry.COLUMN_NOTES,
            MovieEntry.COLUMN_STATUS,
            MovieEntry.COLUMN_RATING,
            MovieEntry.COLUMN_M_SYNOPSIS,
            MovieEntry.COLUMN_M_TRAILERS,
            MovieEntry.COLUMN_M_GENRE
    };

    public static final int COL_ID = 0;
    public static final int COL_MOVIE_ID = 1;
    public static final int COL_UPC = 2;
    public static final int COL_THUMB = 3;
    public static final int COL_POSTER = 4;
    public static final int COL_DISC_ART = 5;
    public static final int COL_BARCODE = 6;
    public static final int COL_TITLE = 7;
    public static final int COL_DATE = 8;
    public static final int COL_RUNTIME = 9;
    public static final int COL_ADD_DATE = 10;
    public static final int COL_FORMATS = 11;
    public static final int COL_STORE = 12;
    public static final int COL_NOTES = 13;
    public static final int COL_STATUS = 14;
    public static final int COL_RATING = 15;
    public static final int COL_SYNOPSIS = 16;
    public static final int COL_TRAILERS = 17;
    public static final int COL_GENRES = 18;

    public static final class MovieEntry implements BaseColumns {
        public static final String TABLE_NAME = "movies";

        // The MOVIE_ID setting string is what will be sent to themoviedatabase.org
        // as the review and trailers.

        //TMDB movie ID number as a string
        public static final String COLUMN_MOVIE_ID = "MOVIE_ID";
        //String UPC code for disc
        public static final String COLUMN_UPC = "UPC";
        //Url for the thumb image
        public static final String COLUMN_THUMB = "M_THUMB";
        //Url for the poster image
        public static final String COLUMN_M_POSTERURL = "M_POSTERURL";
        //Url Movie disc package art
        public static final String COLUMN_DISC_ART = "U_DISC_ART";
        //Url for the upc barcode image
        public static final String COLUMN_BARCODE = "U_BARCODE";
        //String title of movie
        public static final String COLUMN_M_TITLE = "M_TITLE";
        //Movie Release Date String in the format yyyy-mm-dd
        public static final String COLUMN_M_DATE = "M_DATE";
        //String movie runtime in min
        public static final String COLUMN_RUNTIME = "M_RUNTIME";
        //Date Movie Was added to database as a string
        public static final String COLUMN_ADD_DATE = "ADD_DATE";
        //Movie formats included
        public static final String COLUMN_FORMATS = "U_FORMATS";
        //String Store Movie was purchased from (user input)
        public static final String COLUMN_STORE = "STORE";
        //String of notes about movie 140 char long (user input)
        public static final String COLUMN_NOTES = "NOTES";
        //String status of movie either WISH or OWN
        public static final String COLUMN_STATUS = "STATUS";
        //Movie Rating ie. "PG-13"
        public static final String COLUMN_RATING = "M_RATING";
        //String of the Short synopsis
        public static final String COLUMN_M_SYNOPSIS = "M_SYNOPSIS";
        //String of Trailer youtube urls. comma separated.
        public static final String COLUMN_M_TRAILERS = "M_TRAILERS";
        //String of movie/tv genres. comma separated.
        public static final String COLUMN_M_GENRE = "M_GENRE";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        //Build URI based on row_id of movie in database
        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
        //Build URI based on MOVIE_ID for Owned
        public static Uri buildMovieIdUriOwned(String Movie_Id) {

            return CONTENT_URI.buildUpon()
                    .appendPath(SEG_OWNED)
                    .appendPath(SEG_MID)
                    .appendPath(Movie_Id).build();
        }
        //Build URI based on MOVIE_ID for Wish List
        public static Uri buildMovieIdUriWish(String Movie_Id) {

            return CONTENT_URI.buildUpon()
                    .appendPath(SEG_WISHLIST)
                    .appendPath(SEG_MID)
                    .appendPath(Movie_Id).build();
        }
        //Build URI based on UPC for Owned
        public static Uri buildUPCUriOwned(String Upc_Code){
            return CONTENT_URI.buildUpon()
                    .appendPath(SEG_OWNED)
                    .appendPath(SEG_UPC)
                    .appendPath(Upc_Code).build();
        }

        //Build URI based on UPC for Wish List
        public static Uri buildUPCUriWish(String Upc_Code){
            return CONTENT_URI.buildUpon()
                    .appendPath(SEG_WISHLIST)
                    .appendPath(SEG_UPC)
                    .appendPath(Upc_Code).build();
        }
        //Build Uri for all wish list items
        public static Uri buildUriWishAll(){
            return CONTENT_URI.buildUpon()
                    .appendPath(SEG_WISHLIST)
                    .build();
        }
        //Build Uri for all owned Items
        public static Uri buildUriOwnedAll(){
            return CONTENT_URI.buildUpon()
                    .appendPath(SEG_OWNED)
                    .build();
        }
        public static String getMovieIdFromUri(Uri uri) {
            return uri.getLastPathSegment();
        }

        public static String getUpcFromUri(Uri uri){return uri.getLastPathSegment();}


    }
}

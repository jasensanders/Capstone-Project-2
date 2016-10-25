package com.example.jasensanders.v1.folio.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.jasensanders.v1.folio.database.DataContract.MovieEntry;

/**
 * Created by Jasen Sanders on 10/5/2016.
 */

public class FolioDBHelper extends SQLiteOpenHelper{
    private static final int DATABASE_VERSION =1;

    static final String DATABASE_NAME = "folio.db";

    public FolioDBHelper (Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    //INTEGER NOT NULL,

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_FAVORITES_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID + " INTEGER PRIMARY KEY," +
                MovieEntry.COLUMN_MOVIE_ID + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_UPC + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_THUMB + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_M_POSTERURL + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_DISC_ART + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_BARCODE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_M_TITLE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_M_DATE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_RUNTIME + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_ADD_DATE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_FORMATS + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_STORE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_NOTES + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_STATUS + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_RATING + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_M_SYNOPSIS + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_M_TRAILERS + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_M_GENRE + " TEXT NOT NULL, " +
                "UNIQUE (" + MovieEntry.COLUMN_UPC +") ON CONFLICT REPLACE"+
                " );";
        db.execSQL(SQL_CREATE_FAVORITES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

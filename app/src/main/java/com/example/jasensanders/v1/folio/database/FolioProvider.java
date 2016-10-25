package com.example.jasensanders.v1.folio.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;


/**
 * Created by Jasen Sanders on 10/5/2016.
 */

public class FolioProvider extends ContentProvider {

    public static final String ACTION_DATA_UPDATED =
            "com.example.jasensanders.v1.folio.ACTION_DATA_UPDATED";

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private FolioDBHelper mOpenHelper;
    //For selecting all Movies in the DataBase and Deleting/Inserting/Updating all
    //This is the flag used for insertion into movie table
    static final int MOVIE_ALL = 100;
    //For Selecting one Movie with possibly multiple items by TMDB movieID code owned list
    static final int MOVIE_BY_MID_OWNED = 101;
    //For Selecting one item by upc code in owned list
    static final int MOVIE_BY_UPC_OWNED = 102;
    //For Selecting one Movie with possibly multiple items by TMDB movieID code wish list
    static final int MOVIE_BY_MID_WISH = 103;
    //For Selecting one movie by upc code in wish list
    static final int MOVIE_BY_UPC_WISH = 104;
    //For selecting all WISH_LIST movies
    static final int MOV_WISH_LIST_ALL = 105;
    //For selecting all OWNED movies in DataBase
    static final int MOV_OWNED_ALL = 106;


    private static final SQLiteQueryBuilder fQueryBuilder;

    static{
        fQueryBuilder = new SQLiteQueryBuilder();

        //Set The Tables!!
        fQueryBuilder.setTables(
                DataContract.MovieEntry.TABLE_NAME );
    }

    //Selection of individual row by Movie ID according to theMovieDataBase.org
    private static final String fMovieIdSelectionByStatus =
            DataContract.MovieEntry.TABLE_NAME+
                    "." + DataContract.MovieEntry.COLUMN_MOVIE_ID + " = ? " +" AND "
                    + DataContract.MovieEntry.COLUMN_STATUS + " = ? ";
    private static final String fAllSelection = null;
    //Selection of a single Item by UPC code
    private static final String fUpcMoviesSelection = DataContract.MovieEntry.TABLE_NAME +
            "." + DataContract.MovieEntry.COLUMN_UPC + " = ? ";
    //Selection of all Items by Status OWNED or WISH
    private static final String fMoviesByStatus = DataContract.MovieEntry.TABLE_NAME +
            "." + DataContract.MovieEntry.COLUMN_STATUS + " = ? ";



    private Cursor getMovieByIdByStatus(Uri uri, String[] projection, String sortOrder) {
        String MovieId = DataContract.MovieEntry.getMovieIdFromUri(uri);
        //base/authority/movie/status(owned or wish)/mid/# This gets the status.
        String status = uri.getPathSegments().get(1).toUpperCase();

        //By which Column?
        String selection = fMovieIdSelectionByStatus;
        //Which items specifically
        String[] selectionArgs = new String[]{MovieId, status};

        return fQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getMoviesByUpc(Uri uri, String[] projection, String sortOrder){
        String upc = DataContract.MovieEntry.getUpcFromUri(uri);

        //By which Column?
        String selection = fUpcMoviesSelection;

        //Which Item specifically
        String[] selectionArgs = new String[]{upc};

        return fQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }

    private Cursor getAllOwnedMovies(Uri uri, String[] projection, String sortOrder){
        String selection = fMoviesByStatus;
        String[] selectionArgs = new String[]{DataContract.STATUS_OWNED_MOVIES};
        return fQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }

    private Cursor getWishList(Uri uri, String[] projection, String sortOrder){
        String selection = fMoviesByStatus;
        String[] selectionArgs = new String[]{DataContract.STATUS_WISH_LIST};
        return fQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }

    private Cursor getAll(Uri uri, String[] projection, String sortOrder) {

        return fQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                fAllSelection,
                null,
                null,
                null,
                sortOrder
        );
    }

    //URI matcher for states above
    static UriMatcher buildUriMatcher() {
        //Setup the Matcher
        final UriMatcher fURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DataContract.CONTENT_AUTHORITY;

        //Add Match possibilities for the states defined above
        fURIMatcher.addURI(authority,DataContract.PATH_MOVIES,MOVIE_ALL );
        fURIMatcher.addURI(authority,DataContract.PATH_WISH_LIST, MOV_WISH_LIST_ALL);
        fURIMatcher.addURI(authority,DataContract.PATH_OWNED_MOVIES,MOV_OWNED_ALL);
        fURIMatcher.addURI(authority,DataContract.PATH_OWMNED_MOVIES_MID + "/#",MOVIE_BY_MID_OWNED );
        fURIMatcher.addURI(authority,DataContract.PATH_WISH_LIST_MID + "/#",MOVIE_BY_MID_WISH );
        fURIMatcher.addURI(authority, DataContract.PATH_OWNED_MOVIES_UPC + "/#",MOVIE_BY_UPC_OWNED);
        fURIMatcher.addURI(authority, DataContract.PATH_WISH_LIST_UPC + "/#",MOVIE_BY_UPC_WISH);


        return fURIMatcher;
    }


    //Create a new FolioDbHelper for later use in onCreate
    @Override
    public boolean onCreate() {
        mOpenHelper = new FolioDBHelper(getContext());
        return true;
    }


    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {

            case MOVIE_BY_UPC_OWNED:
                return DataContract.MovieEntry.CONTENT_ITEM_TYPE;
            case MOVIE_BY_UPC_WISH:
                return DataContract.MovieEntry.CONTENT_ITEM_TYPE;
            case MOVIE_BY_MID_OWNED:
                return DataContract.MovieEntry.CONTENT_TYPE;
            case MOVIE_BY_MID_WISH:
                return DataContract.MovieEntry.CONTENT_TYPE;
            case MOV_OWNED_ALL:
                return DataContract.MovieEntry.CONTENT_TYPE;
            case MOV_WISH_LIST_ALL:
                return DataContract.MovieEntry.CONTENT_TYPE;
            case MOVIE_ALL:
                return DataContract.MovieEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "movies/owned/mid/#"
            case MOVIE_BY_MID_OWNED:
            {
                retCursor = getMovieByIdByStatus(uri, projection, sortOrder);
                break;
            }
            //"movies/wish/mid/#"
            case MOVIE_BY_MID_WISH:
            {
                retCursor = getMovieByIdByStatus(uri, projection, sortOrder);
                break;
            }
            case MOVIE_BY_UPC_OWNED:
            {
                retCursor = getMoviesByUpc(uri, projection, sortOrder);
                break;
            }
            case MOVIE_BY_UPC_WISH:
            {
                retCursor = getMoviesByUpc(uri, projection, sortOrder);
                break;
            }
            case MOV_OWNED_ALL:
            {
                retCursor = getAllOwnedMovies(uri, projection, sortOrder);
                break;
            }
            case MOV_WISH_LIST_ALL: {
                retCursor = getWishList(uri, projection, sortOrder);
                break;
            }
            // "movies"
            case MOVIE_ALL: {
                retCursor = getAll(uri, projection, sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    /*
        Student: Add the ability to insert Locations to the implementation of this function.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {

            case MOVIE_ALL: {
                long _id = db.insert(DataContract.MovieEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = DataContract.MovieEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri+ String.valueOf(_id));
                break;

            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        updateWidgets();
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Student: Start by getting a writable database
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsDeleted;

        // Student: Use the uriMatcher to match the WEATHER and LOCATION URI's we are going to
        // handle.  If it doesn't match these, throw an UnsupportedOperationException.
        final int match = sUriMatcher.match(uri);
        switch (match){
            case MOVIE_BY_UPC_OWNED:
                rowsDeleted = db.delete(
                        DataContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MOVIE_BY_UPC_WISH:
                rowsDeleted = db.delete(DataContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MOV_OWNED_ALL:
                rowsDeleted = db.delete(
                        DataContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MOV_WISH_LIST_ALL:
                rowsDeleted = db.delete(
                        DataContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }
        //Notify content resolver and any listeners
        if(selection == null||rowsDeleted!=0){
            getContext().getContentResolver().notifyChange(uri, null);
            updateWidgets();
        }

        //rows deleted
        return rowsDeleted;
    }


    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Student: This is a lot like the delete function.  We return the number of rows impacted
        // by the update.
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case MOVIE_BY_UPC_OWNED:
                rowsUpdated = db.update(DataContract.MovieEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case MOVIE_BY_UPC_WISH:
                rowsUpdated = db.update(DataContract.MovieEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            updateWidgets();
        }
        return rowsUpdated;
    }
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIE_ALL:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(DataContract.MovieEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                updateWidgets();
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    private void updateWidgets() {
        Context context = getContext();
        // Setting the package ensures that only components in our app will receive the broadcast
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
                .setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }


}

package com.example.jasensanders.v1.folio.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.jasensanders.v1.folio.AddNewActivity;
import com.example.jasensanders.v1.folio.R;
import com.example.jasensanders.v1.folio.Utility;
import com.example.jasensanders.v1.folio.database.DataContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.Calendar;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class FetchMovieDataService extends IntentService {

    private final String LOG_TAG = FetchMovieDataService.class.getSimpleName();

    // IntentService can perform
    private static final String FETCH_DETAILS = "com.example.jasensanders.v1.folio.services.action.FETCH_DETAILS";

    //Intent Service params
    private static final String UPC_PARAM = "com.example.jasensanders.v1.folio.services.extra.UPC_PARAM";




    public FetchMovieDataService() {
        super("FetchMovieDataService");
    }

    /**
     * Starts this service to perform action with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */

    public static void startActionFetchData(Context context, String upc_param) {
        Intent intent = new Intent(context, FetchMovieDataService.class);
        intent.setAction(FETCH_DETAILS);
        intent.putExtra(UPC_PARAM, upc_param);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (FETCH_DETAILS.equals(action)) {
                final String upc_code = intent.getStringExtra(UPC_PARAM);

                handleActionFetchDetails(upc_code);
            }
        }
    }

    /**
     * Handle action in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFetchDetails(String upc) {
        //This String array MUST BE in the same order as MovieEntry columns!!
        //We do not include the _ID column because it is auto-generated, so 18 (Not 19) in length
        String[] FullRow = new String[18];

        //Variables to hold row data
        String TMDB_MOVIE_ID;
        String UPC_CODE;
        String MOVIE_THUMB;
        String POSTER_URL;
        String ART_IMAGE_URL;
        String BARCODE_URL;
        String TITLE;
        String RELEASE_DATE;
        String RUNTIME = "";
        String ADD_DATE;
        String FORMATS;
        String STORE = "";
        String NOTES = "";
        //Default is you own the movie, can be changed in AddNewActivity
        String STATUS = DataContract.STATUS_OWNED_MOVIES;
        String RATING = "";
        String SYNOPSIS;
        String TRAILERS = "";
        String GENRES;


        //Verify input and restrict input to Movies only for alpha version
        if(upc == null ||upc.length() != 12){
            Log.e(LOG_TAG, "UPC is null or invalid, nothing will be added to Database");
            return;}

        //Add current Data to Variables
        UPC_CODE = upc;
        BARCODE_URL = "http://www.searchupc.com/drawupc.aspx?q=" + UPC_CODE;

        DateFormat df = DateFormat.getDateInstance();
        ADD_DATE = df.format(Calendar.getInstance().getTime());



        //Get UPC data from SearchUPC.com database
        String upcJson = requestUPCdata(upc);
        String TMDBmovieJson;
        String[] TMDBmovieData;
        //If request successful attempt to parse and add to variables
        if(upcJson != null) {
            String[] upcResults = parseUPCdataJson(upcJson);
            if(upcResults != null) {
                ART_IMAGE_URL = upcResults[1];
                String[] nameAndFormat = Utility.parseProductName(upcResults[0]);
                TITLE = nameAndFormat[0];
                FORMATS = nameAndFormat[1];
                TMDBmovieJson = requestMovieData(nameAndFormat[0]);
                if(TMDBmovieJson != null){
                    TMDBmovieData = parseTMDBdataJson(TMDBmovieJson);

                    if(TMDBmovieData != null){
                        TMDB_MOVIE_ID = TMDBmovieData[0];
                        MOVIE_THUMB = TMDBmovieData[1];
                        POSTER_URL = TMDBmovieData[2];
                        RELEASE_DATE = TMDBmovieData[4];
                        SYNOPSIS = TMDBmovieData[5];
                        GENRES = TMDBmovieData[6];

                        String DetailsJson = requestDetailsData(TMDB_MOVIE_ID);
                        if(DetailsJson != null){
                            String runtime = parseTMDBdetailsJson(DetailsJson);
                            if(runtime != null){
                                RUNTIME = runtime;
                            }

                        }
                        String TrailerJson = requestTrailerData(TMDB_MOVIE_ID);
                        if(TrailerJson != null){
                            String trailers = parseTMDBtrailerJson(TrailerJson);
                            if(trailers != null){
                                TRAILERS = trailers;
                            }
                        }
                        String ReleaseJson = requestReleaseData(TMDB_MOVIE_ID);
                        if(ReleaseJson != null){
                            String rating = parseTMDBreleaseJson(ReleaseJson);
                            if(rating != null){
                                RATING = Utility.normalizeRating(rating);
                            }
                        }
                        FullRow = new String[]{TMDB_MOVIE_ID, UPC_CODE, MOVIE_THUMB, POSTER_URL,
                        ART_IMAGE_URL, BARCODE_URL, TITLE, RELEASE_DATE, RUNTIME, ADD_DATE, FORMATS, STORE,
                        NOTES, STATUS, RATING, SYNOPSIS, TRAILERS, GENRES};

                        sendDataBack(FullRow);
                    }
                }else{
                    //Not a Movie that was scanned, or not in the Database
                    //Message to user
                    sendApologies("Sorry, that is not in our database.", upc);
                    return;

                }

            }
        }

    }



    private String requestUPCdata(String upc){

        if(upc == null){return null;}

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String upcJsonStr = null;

        try{
            final String SEARCHUPC_BASE_URL = "http://www.searchupc.com/handlers/upcsearch.ashx?";
            final String REQUEST_TYPE = "request_type";
            final String TYPE_JSON = "3";
            final String ACCESS_TOKEN = "access_token";
            final String UPC_KEY = getResources().getString(R.string.search_upc_key);
            final String UPC = "upc";

            Uri builtUri = Uri.parse(SEARCHUPC_BASE_URL).buildUpon()
                    .appendQueryParameter(REQUEST_TYPE, TYPE_JSON)
                    .appendQueryParameter(ACCESS_TOKEN, UPC_KEY)
                    .appendQueryParameter(UPC, upc)
                    .build();


            URL url = new URL(builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            upcJsonStr = buffer.toString();
        }catch (IOException e){
            Log.e(LOG_TAG, "IO Error UPC Request", e);
            return null;
        }finally {
            if(urlConnection != null){
                urlConnection.disconnect();
            }
            if(reader != null){
                try{
                    reader.close();
                }catch (final IOException f){
                    Log.e(LOG_TAG, "Error Closing Stream UPC Request", f);
                }
            }
        }
        return upcJsonStr;
    }

    private String requestMovieData(String movieName){
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String MovieJsonStr = null;


        try {
            // Construct the URL for the TMDB query
            final String TMDB_BASE_URL =
                    "https://api.themoviedb.org/3/search/movie?";
            final String APP_KEY = "api_key";
            final String TMDB_KEY = getResources().getString(R.string.tmdb_key);
            final String LANG_PARAM = "language";
            final String US_LANG = "en-US";
            final String QUERY_PARAM = "query";

            Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                    .appendQueryParameter(APP_KEY, TMDB_KEY)
                    .appendQueryParameter(LANG_PARAM,US_LANG)
                    .appendQueryParameter(QUERY_PARAM, movieName)
                    .build();

            URL url = new URL(builtUri.toString());

            // Create the request to TMDB, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            MovieJsonStr = buffer.toString();

        } catch (IOException e) {
            Log.e(LOG_TAG, "IO Error TMDB data request", e);
            // If the code didn't successfully get the Movie data, there's no point in attempting
            // to parse it.
            return null;
        } finally{
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream TMDB data request", e);
                }
            }
        }

        return MovieJsonStr;
    }

    private String requestDetailsData(String TMDBmovieID){
        String DetailsJsonStr;

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        try{
            final String TMDB_KEY = getResources().getString(R.string.tmdb_key);
            final String TMDB_BASE_URL =
                    "http://api.themoviedb.org/3/movie/";
            final String LANGUAGE = "en-US";
            final String QUERY_LANGUAGE = "language";
            final String APP_KEY = "api_key";

            Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                    .appendEncodedPath(TMDBmovieID)
                    .appendQueryParameter(APP_KEY, TMDB_KEY)
                    .appendQueryParameter(QUERY_LANGUAGE, LANGUAGE)
                    .build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging. Were not doing this right now cause it will confuse the reader.
                //For now we will just append the line. (Logging the result instead of appending "\n")
                buffer.append(line);
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            DetailsJsonStr = buffer.toString();
            return DetailsJsonStr;
            //Log.v(LOG_TAG, DetailsJsonStr);
        }catch(IOException i){
            Log.e(LOG_TAG, "IO Error TMDB details request", i);
            return null;
        }
    }

    private String requestTrailerData(String TMDBMovieID ){
        String TrailerJsonStr;

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        try{
            final String TMDB_KEY = getResources().getString(R.string.tmdb_key);
            final String TMDB_BASE_URL =
                    "http://api.themoviedb.org/3/movie/";
            final String QUERY_PARAM = "videos";
            final String APP_KEY = "api_key";

            Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                    .appendEncodedPath(TMDBMovieID)
                    .appendEncodedPath(QUERY_PARAM)
                    .appendQueryParameter(APP_KEY, TMDB_KEY)
                    .build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStreamT = urlConnection.getInputStream();
            StringBuffer bufferT = new StringBuffer();
            if (inputStreamT == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStreamT));

            String lineT;
            while ((lineT = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging. Were not doing this right now cause it will confuse the reader.
                //For now we will just append the line. (Logging the result instead of appending "\n")
                bufferT.append(lineT);
            }

            if (bufferT.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            TrailerJsonStr = bufferT.toString();
            return TrailerJsonStr;
            //Log.v(LOG_TAG, TrailerJsonStr);
        }catch(IOException i){
            Log.e(LOG_TAG, "IO Error TMDB trailers request", i);
            return null;
        }

    }

    private String requestReleaseData(String TMDBmovieID){
        String ReleaseJsonStr;

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        try{
            final String TMDB_KEY = getResources().getString(R.string.tmdb_key);
            final String TMDB_BASE_URL =
                    "http://api.themoviedb.org/3/movie/";
            final String QUERY_PARAM = "release_dates";
            final String APP_KEY = "api_key";

            Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                    .appendEncodedPath(TMDBmovieID)
                    .appendEncodedPath(QUERY_PARAM)
                    .appendQueryParameter(APP_KEY, TMDB_KEY)
                    .build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStreamT = urlConnection.getInputStream();
            StringBuffer bufferT = new StringBuffer();
            if (inputStreamT == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStreamT));

            String lineT;
            while ((lineT = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging. Were not doing this right now cause it will confuse the reader.
                //For now we will just append the line. (Logging the result instead of appending "\n")
                bufferT.append(lineT);
            }

            if (bufferT.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            ReleaseJsonStr = bufferT.toString();
            return ReleaseJsonStr;
            //Log.v(LOG_TAG, ReleaseJsonStr);
        }catch(IOException i){
            Log.e(LOG_TAG, "IO Error TMDB Release Dates request", i);
            return null;
        }

    }



    private String[] parseUPCdataJson(String jsonString){
        final String ROOT = "0";
        final String PRODUCTNAME = "productname";
        final String IMAGEURL = "imageurl";

        try {
            JSONObject upcJson = new JSONObject(jsonString);
            JSONObject root = upcJson.getJSONObject(ROOT);
            String productName = root.getString(PRODUCTNAME);
            String imageUrl = root.getString(IMAGEURL);
            return new String[]{productName, imageUrl};
        }catch(JSONException j){
            Log.e(LOG_TAG, "Error Parsing UPC JSON", j );
            return null;
        }

    }

    private String[] parseTMDBdataJson(String movieJsonStr){

        // These are the names of the JSON objects that need to be extracted.
        final String TMDB_LIST = "results";
        final String TMDB_TITLE = "original_title";
        final String TMDB_IMAGE = "poster_path";
        final String TMDB_DATE = "release_date";
        final String TMDB_SYNOPSIS = "overview";
        final String TMDB_GENRES = "genre_ids";
        final String TMDB_ID = "id";
        final String TMDB_RESULTS_TOTAL = "total_results";

        String thumbBaseUrl = "http://image.tmdb.org/t/p/w92/";
        String posterBaseUrl = "http://image.tmdb.org/t/p/w185/";

        try {
            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(TMDB_LIST);

            //If there are no results, then there is nothing to parse
            int totalResults = movieJson.getInt(TMDB_RESULTS_TOTAL);
            if(totalResults == 0 || movieArray.length() == 0){return null;}

            // Strings to hold the data"
            String id, thumb, poster, title, date, synopsis, genre;

            // Get the JSON object representing the Movie
            JSONObject tmdb_movie = movieArray.getJSONObject(0);

            //Get the genres and turn into String CSV
            JSONArray genres = tmdb_movie.getJSONArray(TMDB_GENRES);
            genre = "";
            for (int i = 0; i < genres.length(); i++) {
                //If its the last one, don't add the comma
                if (i == genres.length() - 1) {
                    genre = genre + String.valueOf(genres.get(i));
                } else {
                    genre = genre + String.valueOf(genres.get(i)) + ", ";
                }
            }

            //Get the rest of the data
            id = tmdb_movie.getString(TMDB_ID);
            thumb = thumbBaseUrl + tmdb_movie.getString(TMDB_IMAGE);
            poster = posterBaseUrl + tmdb_movie.getString(TMDB_IMAGE);
            title = tmdb_movie.getString(TMDB_TITLE);
            date = tmdb_movie.getString(TMDB_DATE);
            synopsis = tmdb_movie.getString(TMDB_SYNOPSIS);

            return new String[]{id, thumb, poster, title, date, synopsis, genre};
        }catch (JSONException j){
            Log.e(LOG_TAG, "Error Parsing TMDB data JSON", j );
            return null;
        }

    }

    private String parseTMDBreleaseJson(String jsonString){
        final String RESULTS = "results";
        final String ISO_STANDARD = "iso_3166_1";
        final String COUNTRY = "US";
        final String RELEASE_DATES = "release_dates";
        final String TYPE = "type";
        final int TYPE_THEATRICAL = 3;
        final String MPAA_CERT = "certification";
        try {
            JSONObject root = new JSONObject(jsonString);
            JSONArray rootArray = root.getJSONArray(RESULTS);
            for(int i =0; i<rootArray.length(); i++){
                JSONObject node = rootArray.getJSONObject(i);
                if(node.getString(ISO_STANDARD).contentEquals(COUNTRY)){
                    JSONArray nodeArray = node.getJSONArray(RELEASE_DATES);
                    for(int j =0; j<nodeArray.length(); j++){
                        JSONObject last = nodeArray.getJSONObject(j);
                        if(last.getInt(TYPE) == TYPE_THEATRICAL){
                            return last.getString(MPAA_CERT);
                    }
                    }
                }
            }
            return null;
        }catch(JSONException j){
            Log.e(LOG_TAG, "Error parsing TMDB release JSON", j);
            return null;
        }
    }

    private String parseTMDBdetailsJson(String jsonString){
        if(jsonString == null){return null;}
        final String RUNTIME = "runtime";
        try{
            JSONObject root = new JSONObject(jsonString);
            int runtime = root.getInt(RUNTIME);
            return String.valueOf(runtime) + " min";
        }catch(JSONException j){
            Log.e(LOG_TAG, "Error parsing TMDB details JSON", j);
            return null;
        }
    }

    private String parseTMDBtrailerJson(String jsonStr){
        String output = "";
        if(jsonStr==null){return null;}
        try{

            StringBuffer bufferPT = new StringBuffer();
            final String TRAILER_LIST = "results";
            final String TRAILER_KEY = "key";
            final String TRAILER_BASE_URL = "https://www.youtube.com/watch?v=";
            final String SEPARATOR = ",";
            JSONObject result = new JSONObject(jsonStr);
            JSONArray list = result.getJSONArray(TRAILER_LIST);
            for (int i = 0; i < list.length(); i++) {
                JSONObject tmdb_trailer = list.getJSONObject(i);
                String youtubeKey = tmdb_trailer.getString(TRAILER_KEY);
                if (i == list.length() - 1) {
                    bufferPT.append(TRAILER_BASE_URL);
                    bufferPT.append(youtubeKey);
                } else {
                    bufferPT.append(TRAILER_BASE_URL);
                    bufferPT.append(youtubeKey);
                    bufferPT.append(SEPARATOR);
                }

            }
            output = bufferPT.toString();
            Log.v(LOG_TAG, "Parsed: "+output);

        }catch(JSONException e){
            Log.e(LOG_TAG,"Error parsing TMDB trailers JSON", e);
            return null;
        }
        return output;

    }

    private void sendApologies(String message_to_user, String upc_string){
        String TMDB_MOVIE_ID = "";
        String UPC_CODE = upc_string;
        String MOVIE_THUMB = "";
        String POSTER_URL = "";
        String ART_IMAGE_URL = "";
        String BARCODE_URL = "";
        String TITLE = message_to_user;
        String RELEASE_DATE = "";
        String RUNTIME = "";
        String ADD_DATE = "";
        String FORMATS = "";
        String STORE = "";
        String NOTES = "";
        String STATUS = "";
        String RATING = "";
        String SYNOPSIS = "";
        String TRAILERS = "";
        String GENRES = "";

        sendDataBack(new String[]{TMDB_MOVIE_ID, UPC_CODE, MOVIE_THUMB, POSTER_URL,
                ART_IMAGE_URL, BARCODE_URL, TITLE, RELEASE_DATE, RUNTIME, ADD_DATE, FORMATS, STORE,
                NOTES, STATUS, RATING, SYNOPSIS, TRAILERS, GENRES});


    }

    private void sendDataBack(String[] movieData){
        Intent messageIntent = new Intent(AddNewActivity.SERVICE_EVENT);
        messageIntent.putExtra(AddNewActivity.SERVICE_KEY,movieData);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageIntent);
    }


}

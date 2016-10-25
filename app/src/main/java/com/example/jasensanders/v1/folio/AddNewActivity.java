package com.example.jasensanders.v1.folio;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.jasensanders.v1.folio.barcode.BarcodeActivity;
import com.example.jasensanders.v1.folio.database.DataContract;
import com.example.jasensanders.v1.folio.services.FetchMovieDataService;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

public class AddNewActivity extends AppCompatActivity {
    private static final String LOG_TAG = AddNewActivity.class.getSimpleName();
    private static final String CURRENT_UPC_KEY = "UPC_CURRENT";
    private static final String CURRENT_STATUS_KEY = "STATUS_CURRENT";
    private static final String CURRENT_STORE_KEY = "STORE_CURRENT";
    private static final String CURRENT_NOTES_KEY = "NOTES_CURRENT";

    private BroadcastReceiver messageReceiver;
    private String[] movie;
    private String currentUPC;
    private String STATUS = DataContract.STATUS_OWNED_MOVIES;
    private EditText inputText;
    private TextView error;
    private TextView title;
    private ImageView artImage;
    private TextView releaseDate;
    private TextView rating;
    private TextView formats;
    private TextView synopsis;
    private EditText store;
    private EditText notes;
    private LinearLayout trailerScroll;
    private LinearLayout detailView;
    private LinearLayout buttonLayout;
    private CheckBox favButton;
    private String[] TrailerArray;
    private View divider;
    private View userDivider;




    public static final String SERVICE_EVENT = "SERVICE_EVENT";
    public static final String SERVICE_KEY = "SERVICE_EXTRA";

    private static final int RC_BARCODE_CAPTURE = 9001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Initialize view references
        setContentView(R.layout.activity_add_new);
        initializeViews();

        //If app still in session, Restore State after rotation
        if(savedInstanceState != null){
            String recover = savedInstanceState.getString(CURRENT_UPC_KEY);
            currentUPC = recover;
            if(currentUPC == null || currentUPC.length()== 0){hideViews();}
            else {
                STATUS = savedInstanceState.getString(CURRENT_STATUS_KEY);
                store.setText(savedInstanceState.getString(CURRENT_STORE_KEY));
                notes.setText(savedInstanceState.getString(CURRENT_NOTES_KEY));
                inputText.setText(currentUPC);
            }

        }else {
            //otherwise hide all views and wait for user input
            hideViews();
        }
        inputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String upc = s.toString();
                currentUPC = upc;
                if(upc.length() != 12){
                    //inputText.setText("");
                    return;
                }
                FetchMovieDataService.startActionFetchData(getApplicationContext(), upc);


            }
        });
        //TODO set content descriptions that are static in strings xml file
        //inputText.setContentDescription();



        messageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter(SERVICE_EVENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, filter);


    }

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringArrayExtra(SERVICE_KEY) != null) {
                //Report to user with results
                movie = intent.getStringArrayExtra(SERVICE_KEY);
                //We got Data! Lets show what we got!!!
                showViews();
                inflateViews(intent.getStringArrayExtra(SERVICE_KEY));
                //resetInputText();


            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeActivity.BarcodeObject);
                    //Update the EditText
                    inputText.setText(barcode.displayValue);

                    //LOG ALL THE THINGS!!!!
                    Log.d(LOG_TAG, "Barcode read: " + barcode.displayValue);
                } else {
                    //Tell the user the Scan Failed, then log event.
                    ((TextView) findViewById(R.id.error)).setText(R.string.barcode_failure);
                    ((TextView) findViewById(R.id.error)).setVisibility(View.VISIBLE);
                    Log.d(LOG_TAG, "No barcode captured, intent data is null");
                }
            } else {
                //Tell the user the Activity Failed
                ((TextView) findViewById(R.id.error)).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.error)).setText(String.format(getString(R.string.barcode_error),
                        CommonStatusCodes.getStatusCodeString(resultCode)));

            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current state
        savedInstanceState.putString(CURRENT_UPC_KEY, currentUPC);
        savedInstanceState.putString(CURRENT_STATUS_KEY, STATUS);
        savedInstanceState.putString(CURRENT_STORE_KEY, store.getText().toString());
        savedInstanceState.putString(CURRENT_NOTES_KEY, notes.getText().toString());

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    public void LaunchBarcodeScanner(View view){

        Intent intent = new Intent(this, BarcodeActivity.class);
        intent.putExtra(BarcodeActivity.AutoFocus, true);
        intent.putExtra(BarcodeActivity.UseFlash, false);
        startActivityForResult(intent, RC_BARCODE_CAPTURE);

    }

    public void SetWishOwnStatus(View view){
        CheckBox input = (CheckBox) view;


            if(STATUS.contentEquals(DataContract.STATUS_OWNED_MOVIES) && input.isChecked()) {
                STATUS = DataContract.STATUS_WISH_LIST;
                movie[13] = STATUS;
                Toast.makeText(this, STATUS, Toast.LENGTH_LONG).show();
            }else{
                STATUS = DataContract.STATUS_OWNED_MOVIES;
                movie[13] = STATUS;
                input.setChecked(false);
                Toast.makeText(this, STATUS, Toast.LENGTH_LONG).show();

            }


    }

    public void SaveToDataBase(View view){
        ContentValues insert = Utility.makeRowValues(movie);
        Uri returned = getContentResolver().insert(DataContract.MovieEntry.CONTENT_URI, insert);
        //TODO: Clear Views
        resetStoreAndNotes();
        clearTrailerViews();
        hideViews();
        if(returned != null) {
            Toast.makeText(getApplicationContext(), movie[6] + " Saved to Folio", Toast.LENGTH_LONG).show();
        }

    }

    public void ClearFields(View view){
        resetInputText();
        currentUPC = null;
        resetStoreAndNotes();
        hideViews();
        clearTrailerViews();
    }

    private void initializeViews(){
        //Input Area
        error = (TextView) findViewById(R.id.error);
        error.setVisibility(View.GONE);
        inputText = (EditText) findViewById(R.id.upc);

        //Movie Details Area
        detailView = (LinearLayout) findViewById(R.id.details);
        title = (TextView) findViewById(R.id.movieTitle);
        artImage = (ImageView) findViewById(R.id.artView);
        releaseDate = (TextView) findViewById(R.id.releaseDate);
        rating = (TextView) findViewById(R.id.rating);
        formats = (TextView) findViewById(R.id.formats);
        favButton = (CheckBox) findViewById(R.id.FavButton);
        synopsis = (TextView) findViewById(R.id.synopsis);
        store = (EditText) findViewById(R.id.store);
        notes = (EditText) findViewById(R.id.notes);
        trailerScroll = (LinearLayout) findViewById(R.id.trailer_scroll);
        divider = (View)findViewById(R.id.divider);
        userDivider = (View)findViewById(R.id.userDivider);
        buttonLayout = (LinearLayout)findViewById(R.id.button_layout);

    }

    private void inflateViews(String[] data){
        //Reminder of how array is loaded
//        FullRow = new String[]{TMDB_MOVIE_ID, UPC_CODE, MOVIE_THUMB, POSTER_URL,
//                ART_IMAGE_URL, BARCODE_URL, TITLE, RELEASE_DATE, RUNTIME, ADD_DATE, FORMATS, STORE,
//                NOTES, STATUS, RATING, SYNOPSIS, TRAILERS, GENRES};
        title.setText(data[6]);
        Glide.with(getApplicationContext()).load(data[4]).into(artImage);
        releaseDate.setText(Utility.dateToYear(data[7]));
        rating.setText(data[14]);
        formats.setText(data[10]);
        String overview = String.format(getResources().getString(R.string.overview), data[15]);
        synopsis.setText(overview);

        addTrailers(trailerScroll, data[16]);
        //Log.v(LOG_TAG, "inflateViews Called");

        //ALLy content descriptions for dynamic content
        String description =data[6] + " " + data[10] + " relesaed "+data[7]+ " " + "rated " + data[14];
        detailView.setContentDescription(description);
        synopsis.setContentDescription("Movie overview " + synopsis.getText());
        artImage.setContentDescription(data[6] + " Movie Art");



    }

    private void clearTrailerViews(){
        trailerScroll.removeAllViews();
        trailerScroll.invalidate();
    }

    private void hideViews(){
        detailView.setVisibility(View.GONE);
        title.setVisibility(View.INVISIBLE);
        artImage.setVisibility(View.INVISIBLE);
        releaseDate.setVisibility(View.INVISIBLE);
        rating.setVisibility(View.INVISIBLE);
        formats.setVisibility(View.INVISIBLE);
        favButton.setVisibility(View.INVISIBLE);
        synopsis.setVisibility(View.INVISIBLE);
        trailerScroll.setVisibility(View.INVISIBLE);
        divider.setVisibility(View.INVISIBLE);
        store.setVisibility(View.INVISIBLE);
        notes.setVisibility(View.INVISIBLE);
        userDivider.setVisibility(View.INVISIBLE);
        buttonLayout.setVisibility(View.INVISIBLE);
    }

    private void showViews(){
        detailView.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        artImage.setVisibility(View.VISIBLE);
        releaseDate.setVisibility(View.VISIBLE);
        rating.setVisibility(View.VISIBLE);
        formats.setVisibility(View.VISIBLE);
        favButton.setVisibility(View.VISIBLE);
        synopsis.setVisibility(View.VISIBLE);
        trailerScroll.setVisibility(View.VISIBLE);
        divider.setVisibility(View.VISIBLE);
        store.setVisibility(View.VISIBLE);
        notes.setVisibility(View.VISIBLE);
        userDivider.setVisibility(View.VISIBLE);
        buttonLayout.setVisibility(View.VISIBLE);
    }

    private void resetInputText(){
        inputText.setText("");
    }

    private void resetStoreAndNotes(){
        store.setText("");
        notes.setText("");
    }

    private void addTrailers(LinearLayout view, String trailers){
        LayoutInflater vi = getLayoutInflater();
        final String[] tempTrail;
        //Log.v("AddNew: ", trailers);
        int i = 0;
        try{
            tempTrail = trailers.split(",");
            if(tempTrail != null || tempTrail.length > 0) {
                TrailerArray = tempTrail;

                for(String url: tempTrail){
                    View v =  vi.inflate(R.layout.trailer_tile, view, false);
                    TextView listText = (TextView) v.findViewById(R.id.list_item_trailer_text);
                    String text = "Play Trailer number"+ String.valueOf(i+1);
                    //ALLy Content description for trailers
                    v.setContentDescription(text);
                    v.setFocusable(true);
                    listText.setText(text);
                    final Uri trailerUrl = Uri.parse(url);
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent intent = new Intent(Intent.ACTION_VIEW, trailerUrl);
                            if (intent.resolveActivity(getPackageManager()) != null) {
                                startActivity(intent);
                            }

                        }
                    });
                    view.addView(v, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    i++;
                    Log.v("AddNew: "+ String.valueOf(i), text + " : "+ trailerUrl);


                }
                return;
            }

        }catch (NullPointerException e){
            Log.e(LOG_TAG, "Error splitting and Adding Trailers", e);
            return;
        }

    }
}



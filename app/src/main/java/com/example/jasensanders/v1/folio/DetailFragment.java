package com.example.jasensanders.v1.folio;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.jasensanders.v1.folio.database.DataContract;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    public static final String DETAIL_URI = "URI";
    private static final int DETAIL_LOADER = 0;
    private static final String DETAIL_CURRENT_STORE = "DETAIL_CURRENT_STORE";
    private static final String DETAIL_CURRENT_NOTES = "DETAIL_CURRENT_NOTES";

    private ShareActionProvider mShareActionProvider;
    private Uri mParam1;
    private Context mContext = getActivity();
    private LayoutInflater mInflater;
    private Resources rs;
    private String STATUS;
    private String StateSavedStore;
    private String StateSavedNotes;

    private View rootView;
    private TextView error;
    private TextView title;
    private ImageView posterImage;
    private ImageView barcodeImage;
    private TextView releaseDate;
    private TextView rating_runtime;
    private TextView formats;
    private TextView synopsis;
    private EditText store;
    private EditText notes;
    private LinearLayout trailerScroll;
    private LinearLayout detailView;
    private CheckBox favButton;
    private Button DeleteButton;
    private Button SaveButton;
    private String[] TrailerArray;
    private String DetailMovieShare;





    public DetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1. A URI containing the upc of an item to display
     * @return A new instance of fragment DetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DetailFragment newInstance(Uri param1) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(DETAIL_URI, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        rs = getResources();
        //If we were launched by fragment transaction, get arguments
        if (getArguments() != null) {

            mParam1 = getArguments().getParcelable(DETAIL_URI);

        }
        //Otherwise we were launched by intent, get data
        else{

            Intent intent = getActivity().getIntent();
            mParam1 = intent.getData();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            // Restore last state.
            StateSavedStore = savedInstanceState.getString(DETAIL_CURRENT_STORE);
            StateSavedNotes = savedInstanceState.getString(DETAIL_CURRENT_NOTES);
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mInflater = inflater;
        // Inflate the layout for this fragment
         rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        initializeViews();
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detail_fragment_menu, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareIntent(DetailMovieShare));
        }
    }

    private Intent createShareIntent(String movieDesc) {
        if(movieDesc != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, movieDesc);
            return shareIntent;
        }else{
            String placeHolder = getString(R.string.placeholder_share);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, placeHolder);
            return shareIntent;
        }
    }



    private void initializeViews(){
        //Input Area
        error = (TextView) rootView.findViewById(R.id.error);
        error.setVisibility(View.GONE);


        //Movie Details Area
        detailView = (LinearLayout) rootView.findViewById(R.id.details);
        title = (TextView) rootView.findViewById(R.id.movieTitle);
        posterImage = (ImageView) rootView.findViewById(R.id.posterView);
        barcodeImage = (ImageView) rootView.findViewById(R.id.upcBarcodeImage);
        releaseDate = (TextView) rootView.findViewById(R.id.releaseDate);
        rating_runtime = (TextView) rootView.findViewById(R.id.rating_runtime);
        formats = (TextView) rootView.findViewById(R.id.formats);
        favButton = (CheckBox) rootView.findViewById(R.id.FavButton);
        synopsis = (TextView) rootView.findViewById(R.id.synopsis);
        store = (EditText) rootView.findViewById(R.id.store);
        notes = (EditText) rootView.findViewById(R.id.notes);
        trailerScroll = (LinearLayout) rootView.findViewById(R.id.trailer_scroll);
        DeleteButton = (Button) rootView.findViewById(R.id.delete_button);
        SaveButton = (Button) rootView.findViewById(R.id.save_button);



    }

    private void inflateViews(Cursor row){
        //Reminder of how array is loaded
//        FullRow = new String[]{TMDB_MOVIE_ID, UPC_CODE, MOVIE_THUMB, POSTER_URL,
//                ART_IMAGE_URL, BARCODE_URL, TITLE, RELEASE_DATE, RUNTIME, ADD_DATE, FORMATS, STORE,
//                NOTES, STATUS, RATING, SYNOPSIS, TRAILERS, GENRES};

        // If onCreateOptionsMenu has already happened, we need to update the share intent now.
        if (mShareActionProvider != null) {

            DetailMovieShare = rs.getString(R.string.detail_movie_desc, row.getString(DataContract.COL_TITLE),
                    row.getString(DataContract.COL_FORMATS), row.getString(DataContract.COL_DATE),
                    row.getString(DataContract.COL_RATING), row.getString(DataContract.COL_UPC));
            //Log.v(LOG_TAG, DetailMovieShare);

            mShareActionProvider.setShareIntent(createShareIntent(DetailMovieShare));
        }

        title.setText(row.getString(DataContract.COL_TITLE));
        Glide.with(this).load(row.getString(DataContract.COL_POSTER)).fitCenter().into(posterImage);
        Glide.with(this).load(row.getString(DataContract.COL_BARCODE)).fitCenter().into(barcodeImage);
        releaseDate.setText(Utility.dateToYear(row.getString(DataContract.COL_DATE)));
        String ratingRuntime = String.format(rs.getString(R.string.rating_runtime),
                row.getString(DataContract.COL_RATING), row.getString(DataContract.COL_RUNTIME) );
        rating_runtime.setText(ratingRuntime);
        formats.setText(row.getString(DataContract.COL_FORMATS));
        String overview = String.format(rs.getString(R.string.overview), row.getString(DataContract.COL_SYNOPSIS));
        synopsis.setText(overview);

        //Display the favbutton according to STATUS
        if(row.getString(DataContract.COL_STATUS).contentEquals(DataContract.STATUS_OWNED_MOVIES)){
            favButton.setVisibility(View.INVISIBLE);
        }

        //If this is a wish list item, then set the checkbox accordingly.
        STATUS = row.getString(DataContract.COL_STATUS);
        if(row.getString(DataContract.COL_STATUS).contentEquals(DataContract.STATUS_WISH_LIST)&& !favButton.isChecked()){

            favButton.setChecked(true);
            favButton.setEnabled(false);
        }
        //Restore from Saved State if necessary.
        if(StateSavedStore != null ){
            store.setText(StateSavedStore);
        }else {
            store.setText(row.getString(DataContract.COL_STORE));
        }
        if(StateSavedNotes != null){
            notes.setText(StateSavedNotes);
        }else {
            notes.setText(row.getString(DataContract.COL_NOTES));
        }

        addTrailers(trailerScroll, row.getString(DataContract.COL_TRAILERS));


        //ALLy content descriptions for dynamic content
        String description =rs.getString(R.string.detail_view_description,
                row.getString(DataContract.COL_TITLE), row.getString(DataContract.COL_FORMATS), row.getString(DataContract.COL_DATE),
                row.getString(DataContract.COL_RATING));
        detailView.setContentDescription(description);
        synopsis.setContentDescription(overview);
        String artDesc = rs.getString(R.string.poster_description, row.getString(DataContract.COL_TITLE));
        posterImage.setContentDescription(artDesc);
        String barcodeDesc = rs.getString(R.string.barcode_description, row.getString(DataContract.COL_UPC));
        barcodeImage.setContentDescription(barcodeDesc);

        //Setup Delete and Save Buttons for Owned View and WishList View
        final String CurrentUPC = row.getString(DataContract.COL_UPC);
        DeleteButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                final String DeleteStatus = STATUS;
                Uri deleteMovie;
                //Determine from which list we are deleting
                if(DeleteStatus.contentEquals(DataContract.STATUS_OWNED_MOVIES)){
                     deleteMovie = DataContract.MovieEntry.buildUPCUriOwned(CurrentUPC);
                }else{
                    deleteMovie = DataContract.MovieEntry.buildUPCUriWish(CurrentUPC);
                }
                //Attempt delete
                int rowsDeleted = getActivity().getContentResolver().delete(deleteMovie,
                        DataContract.MovieEntry.COLUMN_UPC + " = ?",
                        new String[]{CurrentUPC});
                //Notify User
                if(rowsDeleted == 1){
                    Toast.makeText(getActivity(), "Movie removed from Folio.", Toast.LENGTH_SHORT).show();
                }

            }
        });
        SaveButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                String Store = store.getText().toString();
                String Notes = notes.getText().toString();

                final String UpdateStatus = STATUS;
                Uri updateMovie;
                ContentValues update;
                int rowsUpdated;
                //Determine from which list we are updating
                if(UpdateStatus.contentEquals(DataContract.STATUS_OWNED_MOVIES)){
                    update = Utility.makeUpdateValues(Store, Notes, null);
                    updateMovie = DataContract.MovieEntry.buildUPCUriOwned(CurrentUPC);
                    //Attempt update
                    rowsUpdated = getActivity().getContentResolver().update(updateMovie,update,
                            DataContract.MovieEntry.COLUMN_UPC + " = ?",
                            new String[]{CurrentUPC});
                }else{
                    update = Utility.makeUpdateValues(Store, Notes, UpdateStatus);
                    updateMovie = DataContract.MovieEntry.buildUPCUriWish(CurrentUPC);
                    //Attempt update
                    rowsUpdated =getActivity().getContentResolver().update(updateMovie, update,
                            DataContract.MovieEntry.COLUMN_UPC + " = ?",
                            new String[]{CurrentUPC});
                }

                if(rowsUpdated == 1){
                    Toast.makeText(getActivity(), "Updates saved to Folio.", Toast.LENGTH_SHORT).show();
                }

            }
        });




    }
    private void addTrailers(LinearLayout view, String trailers){

        if(view.getChildCount() > 0){ view.removeAllViews();}

        final String[] tempTrail;
        //Log.v("AddNew: ", trailers);
        int i = 0;
        try{
            tempTrail = trailers.split(",");
            if(tempTrail != null || tempTrail.length > 0) {
                TrailerArray = tempTrail;

                for(String url: tempTrail){
                    View v =  mInflater.inflate(R.layout.trailer_tile, view, false);
                    TextView listText = (TextView) v.findViewById(R.id.list_item_trailer_text);
                    String text = rs.getString(R.string.trailer_play_description, String.valueOf(i+1));
                    //ALLy Content description for trailers
                    v.setContentDescription(text);
                    v.setFocusable(true);
                    listText.setText(text);
                    final Uri trailerUrl = Uri.parse(url);
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent intent = new Intent(Intent.ACTION_VIEW, trailerUrl);
                            if (intent.resolveActivity(mContext.getPackageManager()) != null) {
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(DETAIL_CURRENT_STORE, store.getText().toString());
        outState.putString(DETAIL_CURRENT_NOTES, notes.getText().toString());
    }

    public void ClearChanges(View view){
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);


    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                mParam1,
                DataContract.MOVIE_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data.moveToFirst()) {
            if(data!= null) {
                inflateViews(data);
            }
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }
}

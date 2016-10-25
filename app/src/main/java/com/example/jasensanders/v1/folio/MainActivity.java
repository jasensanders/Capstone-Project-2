package com.example.jasensanders.v1.folio;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.jasensanders.v1.folio.database.DataContract;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.Thing;


public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, NavigationView.OnNavigationItemSelectedListener {
    public static final String MAIN_PREFS = "MAIN_PREFS";
    private static final String SORT_ORDER_STATE = "SORT";
    private static final String LIST_VIEW_STATE = "VIEW";
    private static final String VIEW_LABEL_STATE = "VIEW_LABEL";
    private RecyclerView itemList;
    private String sortOrder;
    private Uri providerCall;
    private TextView viewLabel;
    private String ViewLabel;
    public Activity activity = this;

    private MovieListAdapter movieListAdapter;

    private static final String[] MOVIE_COLUMNS = {
            DataContract.MovieEntry.COLUMN_UPC,
            DataContract.MovieEntry.COLUMN_DISC_ART,
            DataContract.MovieEntry.COLUMN_M_TITLE,
            DataContract.MovieEntry.COLUMN_ADD_DATE,
            DataContract.MovieEntry.COLUMN_FORMATS,
            DataContract.MovieEntry.COLUMN_STATUS,
            DataContract.MovieEntry.COLUMN_RATING,
    };

    private static final int FOLIO_LOADER = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Start Tracking events
        ((FolioApplication) getApplication()).startTracking();
        //If the app is still in session, Restore State after rotation.
        if (savedInstanceState != null) {
            sortOrder = savedInstanceState.getString(SORT_ORDER_STATE);
            providerCall = Uri.parse(savedInstanceState.getString(LIST_VIEW_STATE));
            ViewLabel = savedInstanceState.getString(VIEW_LABEL_STATE);
        } else {
            //Otherwise set view defaults
            //TODO Settings Activity to set defaults. For now they are set here.
            sortOrder = DataContract.MovieEntry.COLUMN_ADD_DATE + " DESC";
            providerCall = DataContract.MovieEntry.buildUriOwnedAll();
            ViewLabel = getString(R.string.movie_view);

        }
        //if(Build.VERSION_CODES.LOLLIPOP <= android.os.Build.VERSION.SDK_INT)
        AdView mAdView = (AdView) findViewById(R.id.adView);
        // Create an ad request. Check logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("F18A92219F2A59F1")  //My Nexus 5x Test Phone
                .build();
        mAdView.loadAd(adRequest);


        //Initialize recycler view
        itemList = (RecyclerView) findViewById(R.id.content_list);

        //Setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        viewLabel = (TextView) findViewById(R.id.viewLabel);
        viewLabel.setText(ViewLabel);

        //Setup Floating Action Button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(activity).toBundle();
                startActivity(new Intent(getApplicationContext(), AddNewActivity.class), bundle);
            }
        });

        //Setup Nav Drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        getLoaderManager().initLoader(FOLIO_LOADER, null, this);


    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current state
        savedInstanceState.putString(SORT_ORDER_STATE, sortOrder);
        savedInstanceState.putString(LIST_VIEW_STATE, providerCall.toString());
        savedInstanceState.putString(VIEW_LABEL_STATE, ViewLabel);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //public void setLaunchAnimation() {

   // }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_search_retail:
                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle();
                Intent SearchRetailIntent = new Intent(this, RetailerSearchActivity.class);
                startActivity(SearchRetailIntent, bundle);
            case R.id.nav_all_movies:
                providerCall = DataContract.MovieEntry.buildUriOwnedAll();
                getLoaderManager().restartLoader(FOLIO_LOADER, null, this);
                String movieLabel = getString(R.string.movie_view);
                ViewLabel = movieLabel;
                viewLabel.setText(ViewLabel);
                break;
            case R.id.nav_wish_list:
                providerCall = DataContract.MovieEntry.buildUriWishAll();
                getLoaderManager().restartLoader(FOLIO_LOADER, null, this);
                String wishListLabel = getString(R.string.wish_list_view);
                ViewLabel = wishListLabel;
                viewLabel.setText(ViewLabel);
                break;
            case R.id.nav_title_asc:
                //call provider and refresh
                sortOrder = DataContract.MovieEntry.COLUMN_M_TITLE + " ASC";
                getLoaderManager().restartLoader(FOLIO_LOADER, null, this);
                break;
            case R.id.nav_title_desc:
                //call provider and refresh
                sortOrder = DataContract.MovieEntry.COLUMN_M_TITLE + " DESC";
                getLoaderManager().restartLoader(FOLIO_LOADER, null, this);
                break;
            case R.id.nav_date_add_desc:
                //call provider and refresh
                sortOrder = DataContract.MovieEntry.COLUMN_ADD_DATE + " DESC";
                getLoaderManager().restartLoader(FOLIO_LOADER, null, this);
                break;
            case R.id.nav_date_add_asc:
                //call provider ans refresh
                sortOrder = DataContract.MovieEntry.COLUMN_ADD_DATE + " ASC";
                getLoaderManager().restartLoader(FOLIO_LOADER, null, this);
                break;

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        //Get the sort order

        //Build the correct uri

        //return the cursor call
        return new CursorLoader(this,
                providerCall,
                MOVIE_COLUMNS,
                null,
                null,
                sortOrder);


    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            movieListAdapter = new MovieListAdapter(this);
            movieListAdapter.swapCursor(data);
            itemList.setAdapter(movieListAdapter);
            LinearLayoutManager list = new LinearLayoutManager(this);
            list.setOrientation(LinearLayoutManager.VERTICAL);
            itemList.setLayoutManager(list);
            itemList.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL, R.drawable.line_divider));
        } else {
            itemList.setAdapter(null);
            itemList.removeAllViews();

        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        movieListAdapter.swapCursor(null);

    }

    public void updateUI() {
        sortOrder = DataContract.MovieEntry.COLUMN_ADD_DATE + " DESC";
        providerCall = DataContract.MovieEntry.buildUriOwnedAll();
        getLoaderManager().initLoader(FOLIO_LOADER, null, this);

    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }


}

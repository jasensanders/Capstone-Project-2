package com.example.jasensanders.v1.folio;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.jasensanders.v1.folio.barcode.BarcodeActivity;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

public class RetailerSearchActivity extends AppCompatActivity {
    private static final String LOG_TAG = RetailerSearchActivity.class.getSimpleName();

    //State Variable keys
    private static final String SEARCH_CURRENT_UPC = "SEARCH_CURRENT_UPC";
    private static final String SEARCH_CURRENT_TITLE = "SEARCH_CURRENT_TITLE";
    private static final String SEARCH_CURRENT_QUERY = "SEARCH_CURRENT_QUERY";

    //Search Strings
    private static final String SEARCH_AMAZON = "https://www.amazon.com/s?url=search-alias%3Daps&field-keywords=";
    private static final String SEARCH_TARGET = "http://www.target.com/s?searchTerm=";
    private static final String SEARCH_WALMART = "https://www.walmart.com/search/?query=";
    private static final String SEARCH_EBAY = "http://www.ebay.com/sch/i.html?_from=R40&_sacat=0&_nkw=";

    //Barcode flag
    private static final int RC_BARCODE_CAPTURE = 9001;

    //Views
    private EditText UpcInput;
    private EditText TitleInput;

    //Query to share
    private String QUERY;
    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retailer_search);
        initializeViews();

        //If App is still in session, then restore state data
        if(savedInstanceState != null){
            String restoreUpc = savedInstanceState.getString(SEARCH_CURRENT_UPC);
            if(restoreUpc != null){
                UpcInput.setText(restoreUpc);
            }
            String restoreTitle = savedInstanceState.getString(SEARCH_CURRENT_TITLE);
            if(restoreTitle != null){
                TitleInput.setText(restoreTitle);
            }
            QUERY = savedInstanceState.getString(SEARCH_CURRENT_QUERY);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_retail, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareIntent(null));
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeActivity.BarcodeObject);
                    //Update the EditText
                    UpcInput.setText(barcode.displayValue);

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
        savedInstanceState.putString(SEARCH_CURRENT_UPC, UpcInput.getText().toString());
        savedInstanceState.putString(SEARCH_CURRENT_TITLE,TitleInput.getText().toString());
        savedInstanceState.putString(SEARCH_CURRENT_QUERY, QUERY);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }


    public void SearchAmazon(View view){
        String query = launchTermPriority();
        if(query != null){
            //update Query Saved State
            QUERY = SEARCH_AMAZON + query;

            //update share intent
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareIntent(QUERY));
            }

            //Start the intent
            Intent startSomething = createLaunchIntent(SEARCH_AMAZON, query);
            if(startSomething.resolveActivity(getPackageManager()) != null){
                startActivity(startSomething);
            }
        }

    }
    public void SearchTarget(View view){
        String query = launchTermPriority();
        if(query != null){
            //update Query Saved State
            QUERY = SEARCH_TARGET + query;

            //update share intent
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareIntent(QUERY));
            }

            //Start the intent
            Intent startSomething = createLaunchIntent(SEARCH_TARGET, query);
            if(startSomething.resolveActivity(getPackageManager()) != null){
                startActivity(startSomething);
            }
        }

    }
    public void SearchWalmart(View view){
        String query = launchTermPriority();
        if(query != null){
            //update Query Saved State
            QUERY = SEARCH_WALMART + query;

            //update share intent
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareIntent(QUERY));
            }

            //Start the intent
            Intent startSomething = createLaunchIntent(SEARCH_WALMART, query);
            if(startSomething.resolveActivity(getPackageManager()) != null){
                startActivity(startSomething);
            }
        }

    }
    public void SearchEbay(View view){
        String query = launchTermPriority();
        if(query != null){
            //update Query Saved State
            QUERY = SEARCH_EBAY + query;

            //update share intent
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareIntent(QUERY));
            }

            //Start the intent
            Intent startSomething = createLaunchIntent(SEARCH_EBAY, query);
            if(startSomething.resolveActivity(getPackageManager()) != null){
                startActivity(startSomething);
            }
        }

    }

    public void LaunchBarcodeScanner(View view){

        Intent intent = new Intent(this, BarcodeActivity.class);
        intent.putExtra(BarcodeActivity.AutoFocus, true);
        intent.putExtra(BarcodeActivity.UseFlash, false);
        startActivityForResult(intent, RC_BARCODE_CAPTURE);

    }

    private String launchTermPriority(){
        String upcText = UpcInput.getText().toString();
        String titleText = TitleInput.getText().toString();
        if(upcText.length() == 12){
            return upcText;
        }else{
            if(titleText.length() > 0){
                return titleText.replace(" ", "+");
            }
            return null;
        }
    }
    private void initializeViews(){

        UpcInput = (EditText) findViewById(R.id.upc_input);
        TitleInput = (EditText) findViewById(R.id.title_input);

    }

    private Intent createLaunchIntent(String url, String searchTerm){
        String query = url+searchTerm;
        Intent result = new Intent(Intent.ACTION_VIEW, Uri.parse(query));
        result.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return result;
    }

    private Intent createShareIntent(String queryUrl) {
        if(queryUrl != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, queryUrl);
            return shareIntent;
        }else{
            String placeHolder = SEARCH_AMAZON + launchTermPriority();
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, placeHolder);
            return shareIntent;
        }
    }

    private void clearViews(){
        UpcInput.setText("");
        TitleInput.setText("");
    }
}

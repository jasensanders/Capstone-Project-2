package com.example.jasensanders.v1.folio;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.jasensanders.v1.folio.database.DataContract;

/**
 * Created by Jasen Sanders on 10/17/2016.
 */

public class MovieListAdapter extends RecyclerView.Adapter<MovieListAdapter.ViewHolder>  {

    private Cursor mCursor;
    final private Context mContext;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView artImage;
        public final TextView title;
        public final TextView formats;
        public final TextView addDate;
        public final TextView rating;

        //Set references for all the views
        public ViewHolder(View view){
            super(view);

            artImage = (ImageView) view.findViewById(R.id.thumbnail);
            title = (TextView) view.findViewById(R.id.headline_title);
            formats = (TextView) view.findViewById(R.id.byline_formats);
            addDate = (TextView)  view.findViewById(R.id.date_added);
            rating = (TextView) view.findViewById(R.id.rated);
            view.setOnClickListener(this);

        }
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            String upc = mCursor.getString(0);
            String status = mCursor.getString(5);
            Uri send;

            if(status.contentEquals(DataContract.STATUS_OWNED_MOVIES)){
                send = DataContract.MovieEntry.buildUPCUriOwned(upc);
            }else{
                send = DataContract.MovieEntry.buildUPCUriWish(upc);
            }

            if(((AppCompatActivity) mContext).findViewById(R.id.detail_container) != null){
                Bundle args = new Bundle();
                args.putParcelable(DetailFragment.DETAIL_URI, send);
                DetailFragment fragment = DetailFragment.newInstance(send);
                ((AppCompatActivity)mContext).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detail_container, fragment)
                        .commit();

            }else {
                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation((AppCompatActivity)mContext).toBundle();

                Intent DetailIntent = new Intent(mContext, DetailActivity.class)
                        .setData(send);
                mContext.startActivity(DetailIntent, bundle);
            }

        }

    }

    public MovieListAdapter(Context context){
        mContext = context;
    }

    @Override
    public MovieListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        //Inflate the new view
        if(parent instanceof RecyclerView){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            view.setFocusable(true);
            return new ViewHolder(view);
        }else{
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(final MovieListAdapter.ViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        //Load the imageView
        String artUrl = mCursor.getString(mCursor.getColumnIndex(DataContract.MovieEntry.COLUMN_DISC_ART));
        if(Utility.isNetworkAvailable(mContext)){
            Glide.with(mContext).load(artUrl).into(holder.artImage);
        }

        //Get data from cursor
        String title = mCursor.getString(mCursor.getColumnIndex(DataContract.MovieEntry.COLUMN_M_TITLE));
        String formats = mCursor.getString(mCursor.getColumnIndex(DataContract.MovieEntry.COLUMN_FORMATS));
        String date = mCursor.getString(mCursor.getColumnIndex(DataContract.MovieEntry.COLUMN_ADD_DATE));
        String addDate = Utility.addDateToYear(date);
        String rating = mCursor.getString(mCursor.getColumnIndex(DataContract.MovieEntry.COLUMN_RATING));

        //ALLy content descriptions
        String description = title + " " + formats + " " + "Rated "+ rating + "added "+date;
        holder.itemView.setContentDescription(description);

        //Set data into views
        holder.title.setText(title);
        holder.formats.setText(formats);
        holder.addDate.setText(addDate);
        holder.rating.setText(rating);

    }

    @Override
    public int getItemCount() {
        if ( null == mCursor ) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        this.notifyDataSetChanged();

    }

    public Cursor getCursor() {
        return mCursor;
    }


}

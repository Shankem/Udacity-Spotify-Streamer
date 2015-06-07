package com.growingcoder.spotifystreamer;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Used to display an artist in a recycler view.
 * @author Pierce
 * @since 6/7/2015.
 */
public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ViewHolder> {
    private List<Artist> mArtists;

    public ArtistAdapter(List<Artist> artists) {
        mArtists = artists;
    }

    @Override
    public ArtistAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_artist, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public int getItemCount() {
        return mArtists.size();
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mName.setText(mArtists.get(position).getName());
        //TODO picasso image load
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mName;
        public ImageView mThumbnail;

        public ViewHolder(View v) {
            super(v);
            mName = (TextView) v.findViewById(R.id.layout_item_artist_textview_name);
            mThumbnail = (ImageView) v.findViewById(R.id.layout_item_artist_imageview_thumbnail);
        }
    }
}

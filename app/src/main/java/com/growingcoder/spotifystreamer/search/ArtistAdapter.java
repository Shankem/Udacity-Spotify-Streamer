package com.growingcoder.spotifystreamer.search;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.growingcoder.spotifystreamer.R;
import com.growingcoder.spotifystreamer.core.OnRecyclerItemClickListener;
import com.growingcoder.spotifystreamer.core.SpotifyStreamerApp;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Used to display an artist in a recycler view.
 * @author Pierce
 * @since 6/7/2015.
 */
public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ViewHolder> {

    private List<SpotifyArtist> mArtists;
    private OnRecyclerItemClickListener mItemClickListener = null;
    private ArtistComparator mArtistComparator = new ArtistComparator();

    public ArtistAdapter() {
        mArtists = new ArrayList<SpotifyArtist>();
    }

    public List<SpotifyArtist> getArtists() {
        return mArtists;
    }

    public void setArtists(List<SpotifyArtist> artists) {
        mArtists = artists;
        Collections.sort(mArtists, mArtistComparator);
    }

    public void setJSONArtists(List<JSONObject> jsonArtists) {
        List<SpotifyArtist> artists = new ArrayList<SpotifyArtist>();
        for (JSONObject jsonArtist : jsonArtists) {
            artists.add(new SpotifyArtist(jsonArtist));
        }
        setArtists(artists);
    }

    public void setItemClickListener(OnRecyclerItemClickListener listener) {
        mItemClickListener = listener;
    }

    @Override
    public ArtistAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_artist, parent, false);
        ViewHolder vh = new ViewHolder(v, mItemClickListener);
        return vh;
    }

    @Override
    public int getItemCount() {
        return mArtists.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SpotifyArtist artist = mArtists.get(position);
        holder.mName.setText(artist.getName());
        Picasso.with(SpotifyStreamerApp.getApp())
                .load(artist.getThumbnailUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.mThumbnail);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView mName;
        public ImageView mThumbnail;

        private OnRecyclerItemClickListener mListener;

        public ViewHolder(View v, OnRecyclerItemClickListener listener) {
            super(v);
            mListener = listener;
            mName = (TextView) v.findViewById(R.id.layout_item_artist_textview_name);
            mThumbnail = (ImageView) v.findViewById(R.id.layout_item_artist_imageview_thumbnail);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClick(v, getAdapterPosition());
            }
        }
    }

    /**
     * Comparator used for sorting the artists by name.
     */
    private class ArtistComparator implements Comparator<SpotifyArtist> {
        @Override
        public int compare(SpotifyArtist lhs, SpotifyArtist rhs) {
            return lhs.getName().compareToIgnoreCase(rhs.getName());
        }
    }
}

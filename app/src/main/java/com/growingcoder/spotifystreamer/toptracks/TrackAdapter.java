package com.growingcoder.spotifystreamer.toptracks;

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
 * Used to display a track in a recycler view.
 * @author Pierce
 * @since 6/7/2015.
 */
public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.ViewHolder> {

    private List<SpotifyTrack> mTracks;
    private OnRecyclerItemClickListener mItemClickListener = null;
    private TrackComparator mTrackComparator = new TrackComparator();

    public TrackAdapter() {
        mTracks = new ArrayList<SpotifyTrack>();
    }

    public void setTracks(List<SpotifyTrack> tracks) {
        mTracks = tracks;
        Collections.sort(mTracks, mTrackComparator);
    }

    public List<SpotifyTrack> getTracks() {
        return mTracks;
    }

    public void setJSONTracks(List<JSONObject> jsonTracks) {
        List<SpotifyTrack> tracks = new ArrayList<SpotifyTrack>();
        for (JSONObject jsonTrack : jsonTracks) {
            tracks.add(new SpotifyTrack(jsonTrack));
        }
        setTracks(tracks);
    }

    public void setItemClickListener(OnRecyclerItemClickListener listener) {
        mItemClickListener = listener;
    }

    @Override
    public TrackAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_track, parent, false);
        ViewHolder vh = new ViewHolder(v, mItemClickListener);
        return vh;
    }

    @Override
    public int getItemCount() {
        return mTracks.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SpotifyTrack track = mTracks.get(position);
        holder.mName.setText(track.getName());
        holder.mAlbum.setText(track.getAlbumName());
        Picasso.with(SpotifyStreamerApp.getApp())
                .load(track.getThumbnailUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.mThumbnail);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView mName;
        public TextView mAlbum;
        public ImageView mThumbnail;

        private OnRecyclerItemClickListener mListener;

        public ViewHolder(View v, OnRecyclerItemClickListener listener) {
            super(v);
            mListener = listener;
            mName = (TextView) v.findViewById(R.id.layout_item_track_textview_name);
            mAlbum = (TextView) v.findViewById(R.id.layout_item_track_textview_albumname);
            mThumbnail = (ImageView) v.findViewById(R.id.layout_item_track_imageview_thumbnail);
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
     * Comparator used for sorting the tracks by name.
     */
    private class TrackComparator implements Comparator<SpotifyTrack> {
        @Override
        public int compare(SpotifyTrack lhs, SpotifyTrack rhs) {
            return lhs.getName().compareToIgnoreCase(rhs.getName());
        }
    }
}

package com.growingcoder.spotifystreamer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Fragment to display a search field for artists and return the results of said search.
 * @author Pierce
 * @since 6/7/2015.
 */
public class ArtistSearchFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Toolbar mToolbar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_artist_search, container, false);

        mToolbar = (Toolbar) v.findViewById(R.id.main_toolbar);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.main_recyclerview_artists);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(SpotifyStreamerApp.getApp());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ArtistAdapter(new ArrayList<Artist>());
        mRecyclerView.setAdapter(mAdapter);

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
    }
}

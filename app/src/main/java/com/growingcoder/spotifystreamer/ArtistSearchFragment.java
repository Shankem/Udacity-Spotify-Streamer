package com.growingcoder.spotifystreamer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Fragment to display a search field for artists and return the results of said search.
 * @author Pierce
 * @since 6/7/2015.
 */
public class ArtistSearchFragment extends Fragment {

    private SpotifyService mSpotifyService;

    private RecyclerView mRecyclerView;
    private ArtistAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Toolbar mToolbar;

    private Callback<ArtistsPager> mArtistsCallback = new Callback<ArtistsPager>() {
        @Override
        public void success(ArtistsPager artistsPager, Response response) {
            mAdapter.setArtists(artistsPager.artists.items);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void failure(RetrofitError error) {
            //TODO toast error
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSpotifyService = new SpotifyApi().getService();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_artist_search, container, false);

        mToolbar = (Toolbar) v.findViewById(R.id.fragment_artist_search_toolbar);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_artist_search_recyclerview_artists);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(SpotifyStreamerApp.getApp());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ArtistAdapter();
        mRecyclerView.setAdapter(mAdapter);

        TextView search = (TextView) v.findViewById(R.id.fragment_artist_search_edittext_search);
        search.addTextChangedListener(new SearchWatcher());

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
    }

    private class SearchWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            mSpotifyService.searchArtists(s.toString(), mArtistsCallback);
        }
    }
}

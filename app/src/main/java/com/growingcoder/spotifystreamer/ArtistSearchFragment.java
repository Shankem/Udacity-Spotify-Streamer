package com.growingcoder.spotifystreamer;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Fragment to display a search field for artists and return the results of said search.
 *
 * @author Pierce
 * @since 6/7/2015.
 */
public class ArtistSearchFragment extends BaseFragment {

    private static final long SEARCH_DELAY = 500l;

    private SpotifyService mSpotifyService;

    private RecyclerView mRecyclerView;
    private ArtistAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Callback<ArtistsPager> mArtistsCallback = new Callback<ArtistsPager>() {
        @Override
        public void success(ArtistsPager artistsPager, Response response) {
            mAdapter.setArtists(artistsPager.artists.items);
            postEvent(new BusManager.ArtistSearchEvent());
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

    private class SearchWatcher implements TextWatcher {

        private Handler mHandler = new Handler();
        private SearchRunnable mSearchRunnable = new SearchRunnable();

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(final Editable s) {
            mHandler.removeCallbacks(mSearchRunnable);
            mSearchRunnable.setSearchText(s.toString());
            mHandler.postDelayed(mSearchRunnable, SEARCH_DELAY);
        }
    }

    /**
     * Runnable to do the search that we can delay and cancel. Otherwise we'll
     * fire off a request for each character typed.
     */
    private class SearchRunnable implements Runnable {
        private String mSearchText = "";

        public void setSearchText(String text) {
            mSearchText = text;
        }

        @Override
        public void run() {
            mSpotifyService.searchArtists(mSearchText, mArtistsCallback);
        }
    }

    @Subscribe
    public void searchFinished(BusManager.ArtistSearchEvent event) {
        mAdapter.notifyDataSetChanged();
    }
}

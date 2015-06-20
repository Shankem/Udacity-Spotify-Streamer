package com.growingcoder.spotifystreamer.search;

import android.content.Intent;
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
import android.widget.EditText;

import com.growingcoder.spotifystreamer.R;
import com.growingcoder.spotifystreamer.core.BaseFragment;
import com.growingcoder.spotifystreamer.core.BusManager;
import com.growingcoder.spotifystreamer.core.OnRecyclerItemClickListener;
import com.growingcoder.spotifystreamer.core.SpotifyStreamerApp;
import com.growingcoder.spotifystreamer.core.Util;
import com.growingcoder.spotifystreamer.toptracks.TopTracksActivity;
import com.growingcoder.spotifystreamer.toptracks.TopTracksFragment;
import com.squareup.otto.Subscribe;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
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
    private TopTracksFragment mTopTracksFragment = null;

    private RecyclerView mRecyclerView;
    private ArtistAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private View mProgressBar;
    private View mEmptyTextView;

    private int mNumRequests = 0;
    private String mLastSearch = "";

    // If we were searching and then found a cached result, we should ignore any results that come in after
    private boolean mIsSearching = false;

    private Callback<ArtistsPager> mArtistsCallback = new Callback<ArtistsPager>() {
        @Override
        public void success(ArtistsPager artistsPager, Response response) {
            mNumRequests--;

            /* If we made multiple requests we should just ignore previous ones.
            * This is because the user only cares about the current request.
            * Retrofit has no way of cancelling requests, this feature may come in the next version (2.0)
            * but in the current version (1.9) it does not exist.
            */
            if (mNumRequests == 0 && mIsSearching) {
                mIsSearching = false;
                List<SpotifyArtist> artists = new ArrayList<SpotifyArtist>();
                for (Artist artist : artistsPager.artists.items) {
                    artists.add(new SpotifyArtist(artist));
                }
                Util.cacheData(SpotifyArtist.class.getName() + "-" + mLastSearch, artists);
                mAdapter.setArtists(artists);
                postEvent(new BusManager.ArtistSearchEvent());
            }
        }

        @Override
        public void failure(RetrofitError error) {
            mNumRequests--;
            if (mNumRequests == 0 && mIsSearching) {
                mIsSearching = false;
                mAdapter.setArtists(new ArrayList<SpotifyArtist>());
                postEvent(new BusManager.ArtistSearchEvent());
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSpotifyService = new SpotifyApi().getService();
        mAdapter = new ArtistAdapter();
        mAdapter.setItemClickListener(new ArtistClickListener());

        if (mTopTracksFragment != null) {
            mAdapter.setSelectable(true);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_artist_search, container, false);

        mProgressBar = v.findViewById(R.id.fragment_artist_search_progressbar);
        mEmptyTextView = v.findViewById(R.id.fragment_artist_search_textview_empty);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_artist_search_recyclerview_artists);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(SpotifyStreamerApp.getApp());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        EditText search = (EditText) v.findViewById(R.id.fragment_artist_search_edittext_search);
        search.addTextChangedListener(new SearchWatcher());

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //TODO save the list of data and the selected index
    }

    public void setTopTracksFragment(TopTracksFragment fragment) {
        mTopTracksFragment = fragment;
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
            if (mTopTracksFragment != null && !s.toString().equals(mLastSearch) && mTopTracksFragment.getView() != null) {
                mTopTracksFragment.getView().setVisibility(View.INVISIBLE);
                mAdapter.setSelectedPosition(-1);
            }

            mLastSearch = s.toString();
            if ("".equals(mLastSearch)) {
                mIsSearching = false;
                mAdapter.setArtists(new ArrayList<SpotifyArtist>());
                postEvent(new BusManager.ArtistSearchEvent());
            } else {
                List<JSONObject> artists = Util.getCachedData(SpotifyArtist.class.getName() + "-" + mLastSearch);
                if (artists == null) {
                    mIsSearching = true;
                    refreshState(true);

                    mHandler.removeCallbacks(mSearchRunnable);
                    mNumRequests += mSearchRunnable.isRunning() ? 0 : 1;
                    mSearchRunnable.setIsRunning(true);
                    mSearchRunnable.setSearchText(mLastSearch);
                    mHandler.postDelayed(mSearchRunnable, SEARCH_DELAY);
                } else {
                    mIsSearching = false;
                    mAdapter.setJSONArtists(artists);
                    postEvent(new BusManager.ArtistSearchEvent());
                }
            }
        }
    }

    private class ArtistClickListener implements OnRecyclerItemClickListener {
        @Override
        public void onItemClick(View v, int position) {
            SpotifyArtist artist = mAdapter.getArtists().get(position);

            if (mTopTracksFragment == null) {
                Intent intent = new Intent(getActivity(), TopTracksActivity.class);
                intent.putExtra(TopTracksFragment.KEY_BUNDLE_ARTIST_ID, artist.getId());
                intent.putExtra(TopTracksFragment.KEY_BUNDLE_ARTIST_NAME, artist.getName());
                startActivity(intent);
            } else {
                //TODO set selection state
                mTopTracksFragment.setArtist(artist.getId());
                if (mTopTracksFragment.getView() != null) {
                    mTopTracksFragment.getView().setVisibility(View.VISIBLE);
                }
            }
        }
    }

    /**
     * Runnable to do the search that we can delay and cancel. Otherwise we'll
     * fire off a request for each character typed.
     */
    private class SearchRunnable implements Runnable {
        private String mSearchText = "";
        private boolean mIsRunning = false;

        public void setSearchText(String text) {
            mSearchText = text;
        }

        @Override
        public void run() {
            mIsRunning = false;
            mSpotifyService.searchArtists(mSearchText, mArtistsCallback);
        }

        public void setIsRunning(boolean isRunning) {
            mIsRunning = isRunning;
        }

        public boolean isRunning() {
            return mIsRunning;
        }
    }

    private void refreshState(boolean loading) {
        int count = mAdapter.getItemCount();

        mProgressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        mEmptyTextView.setVisibility(loading || count > 0 ? View.GONE : View.VISIBLE);
        mRecyclerView.setVisibility(loading || count == 0 ? View.GONE : View.VISIBLE);
    }

    @Subscribe
    public void searchFinished(BusManager.ArtistSearchEvent event) {
        mAdapter.notifyDataSetChanged();
        refreshState(false);
    }
}

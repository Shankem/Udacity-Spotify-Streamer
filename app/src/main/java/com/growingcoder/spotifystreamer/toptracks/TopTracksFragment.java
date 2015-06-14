package com.growingcoder.spotifystreamer.toptracks;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.growingcoder.spotifystreamer.R;
import com.growingcoder.spotifystreamer.core.BaseFragment;
import com.growingcoder.spotifystreamer.core.BusManager;
import com.growingcoder.spotifystreamer.core.OnRecyclerItemClickListener;
import com.growingcoder.spotifystreamer.core.SpotifyStreamerApp;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Fragment used to display an artist's top tracks.
 *
 * @author Pierce
 * @since 6/13/2015.
 */
public class TopTracksFragment extends BaseFragment {

    public static String KEY_BUNDLE_ARTIST_ID = "BUNDLE_ARTIST_ID";
    public static String KEY_BUNDLE_ARTIST_NAME = "BUNDLE_ARTIST_NAME";

    private static String QUERY_KEY_COUNTRY = "country";

    private SpotifyService mSpotifyService;

    private RecyclerView mRecyclerView;
    private TrackAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ProgressBar mProgressBar;
    private View mEmptyTextView;

    private String mArtistId;

    private Callback<Tracks> mTracksCallback = new Callback<Tracks>() {
        @Override
        public void success(Tracks tracks, Response response) {
            List<SpotifyTrack> tracksList = new ArrayList<SpotifyTrack>();
            for (Track track : tracks.tracks) {
                tracksList.add(new SpotifyTrack(track));
            }
            mAdapter.setTracks(tracksList);
            postEvent(new BusManager.TracksLoadedEvent());
        }

        @Override
        public void failure(RetrofitError error) {
            mAdapter.setTracks(new ArrayList<SpotifyTrack>());
            postEvent(new BusManager.TracksLoadedEvent());
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSpotifyService = new SpotifyApi().getService();
        mAdapter = new TrackAdapter();
        mAdapter.setItemClickListener(new OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Toast.makeText(SpotifyStreamerApp.getApp(), "Implement me in part 2!!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_toptracks, container, false);

        mProgressBar = (ProgressBar) v.findViewById(R.id.fragment_toptracks_search_progressbar);
        mEmptyTextView = v.findViewById(R.id.fragment_toptracks_textview_empty);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_toptracks_recyclerview_tracks);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(SpotifyStreamerApp.getApp());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            setArtist(args.getString(KEY_BUNDLE_ARTIST_ID));
        }
    }

    public void setArtist(String id) {
        //TODO check if this is in the cache and just load from there
        mArtistId = id;
        refreshState(true);

        String locale = getResources().getConfiguration().locale.getCountry();
        Map<String, Object> queryMap = new HashMap<String, Object>();
        queryMap.put(QUERY_KEY_COUNTRY, locale);
        mSpotifyService.getArtistTopTrack(mArtistId, queryMap, mTracksCallback);
    }

    private void refreshState(boolean loading) {
        int count = mAdapter.getItemCount();

        mProgressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        mEmptyTextView.setVisibility(loading || count > 0 ? View.GONE : View.VISIBLE);
        mRecyclerView.setVisibility(loading || count == 0 ? View.GONE : View.VISIBLE);
    }

    @Subscribe
    public void tracksLoaded(BusManager.TracksLoadedEvent event) {
        mAdapter.notifyDataSetChanged();
        refreshState(false);
    }
}

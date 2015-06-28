package com.growingcoder.spotifystreamer.player;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.growingcoder.spotifystreamer.R;
import com.growingcoder.spotifystreamer.core.BusManager;
import com.growingcoder.spotifystreamer.core.EventBridge;
import com.growingcoder.spotifystreamer.core.SpotifyStreamerApp;
import com.growingcoder.spotifystreamer.core.Util;
import com.growingcoder.spotifystreamer.toptracks.SpotifyTrack;
import com.growingcoder.spotifystreamer.toptracks.TopTracksFragment;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to display a music player.
 *
 * @author Pierce
 * @since 6/20/2015.
 */
public class PlayerFragment extends DialogFragment implements EventBridge.LifeCycleState {

    public static final String KEY_BUNDLE_PLAYLIST_POSITION = "BUNDLE_PLAYLIST_POSITION";

    private static final long TIME_UPDATE_DELAY = 200;

    private EventBridge mEventBridge;
    private boolean mAllowsUIChanges = false;
    private SpotifyPlayerService mPlayerService;
    private int mSongPosition = 0;
    private String mArtistName = "";
    private Handler mTimeUpdateHandler;

    private View mContainer;
    private TextView mCurrentTime;
    private TextView mEndTime;
    private TextView mAlbumName;
    private TextView mArtistNameView;
    private TextView mSongName;
    private ImageView mAlbumArt;
    private ImageView mPlayButton;
    private SeekBar mSeekBar;
    private ProgressBar mProgressBar;

    private Runnable mUpdateTimeRunnable = new Runnable() {
        public void run() {
            postEvent(new BusManager.SongUpdatedEvent());
            mTimeUpdateHandler.postDelayed(this, TIME_UPDATE_DELAY);
        }
    };

    private ServiceConnection mPlayerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mPlayerService = ((SpotifyPlayerService.SpotifyPlayerBinder)service).getService();

            //TODO if we're looking at an already playing song we'll have to flag it so we skip all of this
            mPlayerService.pauseCurrentSong();

            List<SpotifyTrack> playlist = new ArrayList<SpotifyTrack>();
            for (JSONObject jsonTrack : Util.getCachedData(SpotifyPlayerService.KEY_PREFS_PLAYLIST)) {
                playlist.add(new SpotifyTrack(jsonTrack));
            }

            mPlayerService.setPlayList(playlist);
            mPlayerService.setSongAtPosition(mSongPosition);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mPlayerService = null;
        }
    };

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mEventBridge = new EventBridge(this);
        mTimeUpdateHandler = new Handler();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_player, container, false);

        ControlClickListener controlClickListener = new ControlClickListener();
        mPlayButton = (ImageView) v.findViewById(R.id.fragment_player_play);
        mPlayButton.setOnClickListener(controlClickListener);
        v.findViewById(R.id.fragment_player_next).setOnClickListener(controlClickListener);
        v.findViewById(R.id.fragment_player_previous).setOnClickListener(controlClickListener);

        mCurrentTime = (TextView) v.findViewById(R.id.fragment_player_textview_current_time);
        mEndTime = (TextView) v.findViewById(R.id.fragment_player_textview_end_time);
        mAlbumName = (TextView) v.findViewById(R.id.fragment_player_textview_album);
        mArtistNameView = (TextView) v.findViewById(R.id.fragment_player_textview_artist);
        mSongName = (TextView) v.findViewById(R.id.fragment_player_textview_song);
        mAlbumArt = (ImageView) v.findViewById(R.id.fragment_player_imageview_album);
        mSeekBar = (SeekBar) v.findViewById(R.id.fragment_player_seekbar);
        mProgressBar = (ProgressBar) v.findViewById(R.id.fragment_player_progressbar);
        mContainer = v.findViewById(R.id.fragment_player_container);

        mSeekBar.setOnSeekBarChangeListener(new SeekListener());

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        mAllowsUIChanges = true;
        BusManager.getBus().register(this);
        mEventBridge.consumeEvents();
        setupTrack();
    }

    @Override
    public void onPause() {
        super.onPause();
        mAllowsUIChanges = false;
        BusManager.getBus().unregister(this);
        mTimeUpdateHandler.removeCallbacks(mUpdateTimeRunnable);
    }

    @Override
    public void onStop() {
        super.onStop();
        SpotifyStreamerApp.getApp().unbindService(mPlayerServiceConnection);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            mSongPosition = args.getInt(KEY_BUNDLE_PLAYLIST_POSITION);
            mArtistName = args.getString(TopTracksFragment.KEY_BUNDLE_ARTIST_NAME);
        }
        Util.startPlayerService(mPlayerServiceConnection);

        //TODO handle rotation mid-song? (image is too big and it shouldn't restart)
        //TODO handle tablet too
    }

    private void setupTrack() {
        if (mPlayerService != null) {
            SpotifyTrack track = mPlayerService.getCurrentTrack();
            Picasso.with(SpotifyStreamerApp.getApp())
                    .load(track.getPlayerImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(mAlbumArt);
            mAlbumName.setText(track.getAlbumName());
            mSongName.setText(track.getName());
            mArtistNameView.setText(mArtistName);

            if (mPlayerService.isPlaying()) {
                mPlayButton.setImageResource(R.drawable.ic_pause_black);
            } else {
                mPlayButton.setImageResource(R.drawable.ic_play_arrow_black);
            }


            int endTime = mPlayerService.getEndTime();
            mEndTime.setText(Util.getFormattedTime(endTime));
            mSeekBar.setMax(endTime);

            mTimeUpdateHandler.removeCallbacks(mUpdateTimeRunnable);
            mTimeUpdateHandler.post(mUpdateTimeRunnable);

            showLoading(false);
        }
    }

    private void updateTime() {
        int time = mPlayerService.getCurrentTime();
        mCurrentTime.setText(Util.getFormattedTime(time));
        mSeekBar.setProgress(time);
    }

    private void showLoading(boolean loading) {
        mContainer.setVisibility(loading ? View.GONE : View.VISIBLE);
        mProgressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    @Subscribe
    public void songUpdated(BusManager.SongUpdatedEvent event) {
        updateTime();
    }

    @Subscribe
    public void songChanged(BusManager.SongChangedEvent event) {
        setupTrack();
    }

    @Subscribe
    public void songLoading(BusManager.SongLoadingEvent event) {
        showLoading(true);
    }

    @Override
    public boolean allowsUIChanges() {
        return mAllowsUIChanges;
    }

    /**
     * Convenience method to post an event to the bridge.
     */
    protected final void postEvent(Object event) {
        mEventBridge.post(event);
    }

    private class ControlClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (mPlayerService != null && v instanceof ImageView) {
                ImageView imageView = (ImageView) v;
                switch (v.getId()) {
                    case R.id.fragment_player_previous:
                        mPlayerService.playPreviousSong();
                        break;
                    case R.id.fragment_player_play:
                        if (mPlayerService.isPlaying()) {
                            imageView.setImageResource(R.drawable.ic_play_arrow_black);
                            mPlayerService.pauseCurrentSong();
                        } else {
                            imageView.setImageResource(R.drawable.ic_pause_black);
                            mPlayerService.resumeCurrentSong();
                        }
                        break;
                    case R.id.fragment_player_next:
                        mPlayerService.playNextSong();
                        break;
                }

            }
        }
    }

    /**
     * Listener to handle scrubbing/seeking through the song.
     */
    private class SeekListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser && mPlayerService != null) {
                mPlayerService.seekTo(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }
}

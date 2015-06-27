package com.growingcoder.spotifystreamer.player;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

import kaaes.spotify.webapi.android.models.Track;

/**
 * Used to display a music player.
 *
 * @author Pierce
 * @since 6/20/2015.
 */
public class PlayerFragment extends DialogFragment implements EventBridge.LifeCycleState {

    public static String KEY_BUNDLE_PLAYLIST_POSITION = "BUNDLE_PLAYLIST_POSITION";

    private EventBridge mEventBridge;
    private boolean mAllowsUIChanges = false;
    private SpotifyPlayerService mPlayerService;
    private int mSongPosition = 0;
    private String mArtistName = "";

    private TextView mCurrentTime;
    private TextView mEndTime;
    private TextView mAlbumName;
    private TextView mArtistNameView;
    private TextView mSongName;
    private ImageView mAlbumArt;
    private SeekBar mSeekBar;

    private ServiceConnection mPlayerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mPlayerService = ((SpotifyPlayerService.SpotifyPlayerBinder)service).getService();
            mPlayerService.pauseCurrentSong();
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
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_player, container, false);

        ControlClickListener controlClickListener = new ControlClickListener();
        v.findViewById(R.id.fragment_player_play).setOnClickListener(controlClickListener);
        v.findViewById(R.id.fragment_player_next).setOnClickListener(controlClickListener);
        v.findViewById(R.id.fragment_player_previous).setOnClickListener(controlClickListener);

        mCurrentTime = (TextView) v.findViewById(R.id.fragment_player_textview_current_time);
        mEndTime = (TextView) v.findViewById(R.id.fragment_player_textview_end_time);
        mAlbumName = (TextView) v.findViewById(R.id.fragment_player_textview_album);
        mArtistNameView = (TextView) v.findViewById(R.id.fragment_player_textview_artist);
        mSongName = (TextView) v.findViewById(R.id.fragment_player_textview_song);
        mAlbumArt = (ImageView) v.findViewById(R.id.fragment_player_imageview_album);
        mSeekBar = (SeekBar) v.findViewById(R.id.fragment_player_seekbar);


        //TODO set the current time text (and end time)

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        mAllowsUIChanges = true;
        BusManager.getBus().register(this);
        mEventBridge.consumeEvents();

        //TODO update track, may need to get the current track from the service
    }

    @Override
    public void onPause() {
        super.onPause();
        mAllowsUIChanges = false;
        BusManager.getBus().unregister(this);
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

        //TODO handle rotation mid-song?
    }

    private void setupTrack() {
        if (mPlayerService != null) {
            SpotifyTrack track = mPlayerService.getCurrentTrack();Track t;
            Picasso.with(SpotifyStreamerApp.getApp())
                    .load(track.getPlayerImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(mAlbumArt);
            mAlbumName.setText(track.getAlbumName());
            mSongName.setText(track.getName());
            mArtistNameView.setText(mArtistName);
        }
    }

    @Subscribe
    public void songChanged(BusManager.SongChangedEvent event) {
        setupTrack();
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
}

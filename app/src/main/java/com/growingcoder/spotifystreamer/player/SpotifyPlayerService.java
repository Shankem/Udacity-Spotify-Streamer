package com.growingcoder.spotifystreamer.player;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.growingcoder.spotifystreamer.core.Util;
import com.growingcoder.spotifystreamer.toptracks.SpotifyTrack;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service used to handle playing music and communicating with the UI to update the current playing song.
 *
 * @author Pierce
 * @since 6/21/2015.
 */
public class SpotifyPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener {

    public static String KEY_PREFS_PLAYLIST = "PREFS_PLAYLIST";

    private List<SpotifyTrack> mTracks;
    private int mPlayListPosition = 0;
    private MediaPlayer mPlayer;
    private WifiManager.WifiLock mWifiLock;
    private IBinder mBinder = new SpotifyPlayerBinder();
    private boolean mIsPlaying;

    @Override
    public void onCreate() {
        super.onCreate();
        mPlayListPosition = 0;
        mTracks = new ArrayList<SpotifyTrack>();
        for (JSONObject jsonTrack : Util.getCachedData(KEY_PREFS_PLAYLIST)) {
            mTracks.add(new SpotifyTrack(jsonTrack));
        }

        setupPlayer();
    }

    /**
     * Initialize the player and get necessary wakelocks.
     */
    private void setupPlayer() {
        mPlayer = new MediaPlayer();
        mPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);

        mWifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, getClass().getName());
        mWifiLock.acquire();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWifiLock.release();
    }

    @Override
    public void onCompletion(MediaPlayer player) {
        //TODO
    }

    @Override
    public boolean onError(MediaPlayer player, int what, int extra) {
        mPlayer.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer player) {
        if (mIsPlaying) {
            player.start();
        }
    }

    public void playPreviousSong() {
        mPlayListPosition = (mPlayListPosition - 1) % mTracks.size();
        playCurrentSong();
    }

    public void playNextSong() {
        mPlayListPosition = (mPlayListPosition + 1) % mTracks.size();
        playCurrentSong();
    }

    public void setSongAtPosition(int position) {
        mPlayListPosition = position;
        playCurrentSong();
    }

    private void playCurrentSong() {
        mPlayer.reset();
        try {
            //TODO test with bad URL
            mPlayer.setDataSource(mTracks.get(mPlayListPosition).getPreviewUrl());
        } catch (IOException e) {
            Log.e(getClass().getName(), "Error occurred setting next song.", e);
        }
        mPlayer.prepareAsync();
    }

    public void resumeCurrentSong() {
        mPlayer.start();
        mIsPlaying = true;
        //TODO
    }

    public void pauseCurrentSong() {
        mPlayer.pause();
        mIsPlaying = false;
        //TODO
    }

    public void scrub() {
        //TODO skip to a point in the song
    }

    public boolean isPlaying() {
        return mIsPlaying;
    }

    public class SpotifyPlayerBinder extends Binder {
        SpotifyPlayerService getService() {
            return SpotifyPlayerService.this;
        }
    }
}

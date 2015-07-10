package com.growingcoder.spotifystreamer.player;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.growingcoder.spotifystreamer.R;
import com.growingcoder.spotifystreamer.core.BusManager;
import com.growingcoder.spotifystreamer.core.EventBridge;
import com.growingcoder.spotifystreamer.core.SpotifyStreamerApp;
import com.growingcoder.spotifystreamer.toptracks.SpotifyTrack;
import com.squareup.picasso.Picasso;

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
        MediaPlayer.OnErrorListener, EventBridge.LifeCycleState {

    public static String KEY_PREFS_PLAYLIST = "PREFS_PLAYLIST";

    private static final String ACTION_STOP = "ACTION_STOP";
    private static final String ACTION_PAUSE = "ACTION_PAUSE";
    private static final String ACTION_PLAY = "ACTION_PLAY";
    private static final String ACTION_NEXT = "ACTION_NEXT";
    private static final String ACTION_PREVIOUS = "ACTION_PREVIOUS";

    private static final int PLAYER_NOTIFICATION_ID = 1;

    private List<SpotifyTrack> mTracks;
    private int mPlayListPosition = -1;
    private MediaPlayer mPlayer;
    private WifiManager.WifiLock mWifiLock;
    private IBinder mBinder = new SpotifyPlayerBinder();
    private boolean mAllowsUIChanges = false;
    private EventBridge mEventBridge;

    @Override
    public void onCreate() {
        super.onCreate();
        mEventBridge = new EventBridge(this);
        mAllowsUIChanges = true;
        mTracks = new ArrayList<SpotifyTrack>();

        setupPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int result = super.onStartCommand(intent, flags, startId);
        String action = intent.getAction();
        if (ACTION_STOP.equals(action)) {
            pauseCurrentSong(false);
        } else if (ACTION_PAUSE.equals(action)) {
            pauseCurrentSong();
        } else if (ACTION_PLAY.equals(action)) {
            resumeCurrentSong();
        } else if (ACTION_NEXT.equals(action)) {
            playNextSong();
        } else if (ACTION_PREVIOUS.equals(action)) {
            playPreviousSong();
        }

        return result;
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
        mAllowsUIChanges = false;

        //TODO dismiss the notification
    }

    @Override
    public void onCompletion(MediaPlayer player) {
        playNextSong();
    }

    @Override
    public boolean onError(MediaPlayer player, int what, int extra) {
        mPlayer.reset();
        Toast.makeText(SpotifyStreamerApp.getApp(), R.string.song_load_error, Toast.LENGTH_SHORT).show();
        mEventBridge.post(new BusManager.SongChangedEvent());

        NotificationManager notificationManager = (NotificationManager) SpotifyStreamerApp.getApp().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(PLAYER_NOTIFICATION_ID);
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer player) {
        if (!player.isPlaying()) {
            player.start();
            mEventBridge.post(new BusManager.SongChangedEvent());
            showNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
        }
    }

    public void setPlayList(List<SpotifyTrack> playList) {
        mTracks = playList;
    }

    public void playPreviousSong() {
        mPlayListPosition = (mPlayListPosition - 1) % mTracks.size();
        if (mPlayListPosition < 0) {
            mPlayListPosition = mTracks.size() - 1;
        }
        playCurrentSong();
    }

    public void playNextSong() {
        mPlayListPosition = (mPlayListPosition + 1) % mTracks.size();
        playCurrentSong();
    }

    public void setSongAtPosition(int position) {
        mPlayListPosition = mTracks.size() > position ? position : 0;
        playCurrentSong();
    }

    private void playCurrentSong() {
        mPlayer.reset();
        if (mTracks.size() == 0) {
            return;
        }

        mEventBridge.post(new BusManager.SongLoadingEvent());
        String url = mTracks.get(mPlayListPosition).getPreviewUrl();
        if (url == null) {
            onError(mPlayer, 0, 0);
        } else {
            try {
                mPlayer.setDataSource(url);
            } catch (IOException e) {
                Log.e(getClass().getName(), "Error occurred setting next song.", e);
            }
            mPlayer.prepareAsync();
        }

    }

    public void resumeCurrentSong() {
        if (!mPlayer.isPlaying()) {
            mPlayer.start();
            showNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
        }
    }

    public void pauseCurrentSong() {
        pauseCurrentSong(true);
    }

    public void pauseCurrentSong(boolean showNotification) {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();

            if (showNotification) {
                showNotification(generateAction(android.R.drawable.ic_media_play, "Play", ACTION_PLAY));
            }
        }
    }

    //TODO remove hard-coded titles

    //TODO to hide and show on lockscreen
    //TODO http://stackoverflow.com/questions/26932457/lollipop-notification-setvisibility-does-not-work

    private NotificationCompat.Action generateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(SpotifyStreamerApp.getApp(), SpotifyPlayerService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(SpotifyStreamerApp.getApp(), 1, intent, 0);
        return new NotificationCompat.Action.Builder(icon, title, pendingIntent).build();
    }

    private void showNotification(NotificationCompat.Action action) {
        NotificationCompat.MediaStyle style = new NotificationCompat.MediaStyle();

        Intent intent = new Intent(SpotifyStreamerApp.getApp(), SpotifyPlayerService.class);
        intent.setAction(ACTION_STOP);
        PendingIntent pendingIntent = PendingIntent.getService(SpotifyStreamerApp.getApp(), 1, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(SpotifyStreamerApp.getApp());
        builder.setSmallIcon(R.mipmap.ic_launcher);

        Bitmap largeIcon;

        try {
            largeIcon = Picasso.with(this).load(getCurrentTrack().getThumbnailUrl()).placeholder(android.R.drawable.ic_menu_gallery).get();
        } catch (Exception e) {
            largeIcon = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_menu_gallery);
        }

        builder.setLargeIcon(largeIcon);
        builder.setDeleteIntent(pendingIntent);
        builder.setStyle(style);
        builder.setContentTitle("Title here");
        builder.setContentText("Context here");

        builder.addAction(generateAction(android.R.drawable.ic_media_previous, "Previous", ACTION_PREVIOUS));
        builder.addAction(action);
        builder.addAction(generateAction(android.R.drawable.ic_media_next, "Next", ACTION_NEXT));

        NotificationManager notificationManager = (NotificationManager) SpotifyStreamerApp.getApp().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(PLAYER_NOTIFICATION_ID, builder.build());
    }

    public int getEndTime() {
        return mPlayer.getDuration();
    }

    public int getCurrentTime() {
        return mPlayer.getCurrentPosition();
    }

    public void seekTo(int ms) {
        mPlayer.seekTo(ms);
    }

    public boolean isPlaying() {
        return mPlayer.isPlaying();
    }

    /**
     * Check if the service was already started normally.
     */
    public boolean isOn() {
        return mPlayListPosition != -1;
    }

    public SpotifyTrack getCurrentTrack() {
        return mTracks.size() == 0 ? null : mTracks.get(mPlayListPosition);
    }

    @Override
    public boolean allowsUIChanges() {
        return mAllowsUIChanges;
    }

    public class SpotifyPlayerBinder extends Binder {
        public SpotifyPlayerService getService() {
            return SpotifyPlayerService.this;
        }
    }
}

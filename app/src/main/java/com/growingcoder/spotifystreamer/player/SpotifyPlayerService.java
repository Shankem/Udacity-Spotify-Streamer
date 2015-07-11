package com.growingcoder.spotifystreamer.player;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.growingcoder.spotifystreamer.R;
import com.growingcoder.spotifystreamer.core.BusManager;
import com.growingcoder.spotifystreamer.core.EventBridge;
import com.growingcoder.spotifystreamer.core.SpotifyStreamerApp;
import com.growingcoder.spotifystreamer.search.MainActivity;
import com.growingcoder.spotifystreamer.toptracks.SpotifyTrack;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Service used to handle playing music and communicating with the UI to update the current playing song.
 *
 * @author Pierce
 * @since 6/21/2015.
 */
public class SpotifyPlayerService extends IntentService implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener, EventBridge.LifeCycleState, Target {

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
    private String mLoadingArt = null;
    private NotificationCompat.Action mLastAction;
    private Bitmap mCurrentArt = null;

    public SpotifyPlayerService() {
        super(SpotifyPlayerService.class.getName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mEventBridge = new EventBridge(this);
        mAllowsUIChanges = true;
        mTracks = new ArrayList<SpotifyTrack>();

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
    protected void onHandleIntent(Intent intent) {
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
        removeNotification();
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

        removeNotification();
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer player) {
        if (!player.isPlaying()) {
            mCurrentArt = null;
            player.start();
            mEventBridge.post(new BusManager.SongChangedEvent());
            showNotification(generateAction(android.R.drawable.ic_media_pause, getString(R.string.pause), ACTION_PAUSE));
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
            showNotification(generateAction(android.R.drawable.ic_media_pause, getString(R.string.pause), ACTION_PAUSE));
        }
    }

    public void pauseCurrentSong() {
        pauseCurrentSong(true);
    }

    public void pauseCurrentSong(boolean showNotification) {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();

            if (showNotification) {
                showNotification(generateAction(android.R.drawable.ic_media_play, getString(R.string.play), ACTION_PLAY));
            } else {
                mLoadingArt = null;
                mCurrentArt = null;
                mLastAction = null;
            }
        }
    }

    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) SpotifyStreamerApp.getApp().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(PLAYER_NOTIFICATION_ID);
        mLastAction = null;
        mCurrentArt = null;
        mLoadingArt = null;
    }

    private NotificationCompat.Action generateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(SpotifyStreamerApp.getApp(), SpotifyPlayerService.class);
        intent.setAction(intentAction);
        int requestCode = (int) new Date().getTime();
        PendingIntent pendingIntent = PendingIntent.getService(SpotifyStreamerApp.getApp(), requestCode, intent, 0);
        return new NotificationCompat.Action.Builder(icon, title, pendingIntent).build();
    }

    private void showNotification(final NotificationCompat.Action action) {
        showNotification(action, mCurrentArt);
    }

    private void showNotification(NotificationCompat.Action action, Bitmap art) {
        mCurrentArt = art;
        mLastAction = action;
        SpotifyTrack track = getCurrentTrack();

        if (track == null) {
            removeNotification();
            return;
        }

        NotificationCompat.MediaStyle style = new NotificationCompat.MediaStyle();

        Intent intent = new Intent(SpotifyStreamerApp.getApp(), SpotifyPlayerService.class);
        intent.setAction(ACTION_STOP);
        PendingIntent pendingIntent = PendingIntent.getService(SpotifyStreamerApp.getApp(), 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(SpotifyStreamerApp.getApp());
        builder.setSmallIcon(R.mipmap.ic_launcher);

        Bitmap largeIcon = art != null ? art : BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_menu_gallery);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SpotifyStreamerApp.getApp());
        boolean showOnLockScreen = preferences.getBoolean(MainActivity.KEY_SHOW_LOCKSCREEN, true);

        builder.setLargeIcon(largeIcon);
        builder.setDeleteIntent(pendingIntent);
        builder.setStyle(style);
        builder.setContentTitle(track.getName());
        builder.setContentText(track.getArtistName());
        builder.setVisibility(showOnLockScreen ? NotificationCompat.VISIBILITY_PUBLIC : NotificationCompat.VISIBILITY_SECRET);

        builder.addAction(generateAction(android.R.drawable.ic_media_previous, getString(R.string.previous), ACTION_PREVIOUS));
        builder.addAction(action);
        builder.addAction(generateAction(android.R.drawable.ic_media_next, getString(R.string.next), ACTION_NEXT));

        NotificationManager notificationManager = (NotificationManager) SpotifyStreamerApp.getApp().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(PLAYER_NOTIFICATION_ID, builder.build());

        if (art == null) {
            mLoadingArt = track.getThumbnailUrl();
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Picasso.with(SpotifyPlayerService.this)
                            .load(mLoadingArt)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .into(SpotifyPlayerService.this);
                }
            });
        }
    }

    public void refreshNotification() {
        if (mLastAction != null) {
            showNotification(mLastAction);
        }
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

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        SpotifyTrack track = getCurrentTrack();
        if (track != null && mLoadingArt != null && mLoadingArt.equals(track.getThumbnailUrl())) {
            showNotification(mLastAction, bitmap);
        }
    }
    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
    }
    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
    }

    public class SpotifyPlayerBinder extends Binder {
        public SpotifyPlayerService getService() {
            return SpotifyPlayerService.this;
        }
    }
}

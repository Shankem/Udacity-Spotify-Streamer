package com.growingcoder.spotifystreamer.search;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.growingcoder.spotifystreamer.R;
import com.growingcoder.spotifystreamer.core.BaseActivity;
import com.growingcoder.spotifystreamer.core.BusManager;
import com.growingcoder.spotifystreamer.core.SpotifyStreamerApp;
import com.growingcoder.spotifystreamer.core.Util;
import com.growingcoder.spotifystreamer.player.PlayerActivity;
import com.growingcoder.spotifystreamer.player.PlayerFragment;
import com.growingcoder.spotifystreamer.player.SpotifyPlayerService;
import com.growingcoder.spotifystreamer.toptracks.TopTracksFragment;
import com.squareup.otto.Subscribe;

import java.util.Locale;

/**
 * Displays a search field to search for artists on Spotify. It returns results in a list if there are any.
 *
 * @author Pierce
 * @since 6/7/2015.
 */
public class MainActivity extends BaseActivity {

    public static final String KEY_COUNTRY = "KEY_COUNTRY";
    public static final String KEY_SHOW_NOTIFICATION = "KEY_SHOW_NOTIFICATION";
    private static final String STATE_SUBTITLE = "STATE_SUBTITLE ";

    private Toolbar mToolbar;
    private AlertDialog mDialog;
    private boolean mIsTablet = false;
    private MenuItem mShowNotificationMenu;
    private MenuItem mNowPlayingMenu;
    private MenuItem mShareMenu;
    private SpotifyPlayerService mPlayerService;
    private ShareActionProvider mShareActionProvider;

    private ServiceConnection mPlayerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mPlayerService = ((SpotifyPlayerService.SpotifyPlayerBinder)service).getService();
            if (mNowPlayingMenu != null) {
                mNowPlayingMenu.setVisible(mPlayerService.isOn());
            }
            if (mShareMenu != null) {
                mShareMenu.setVisible(mPlayerService.isOn());
            }

            if (!mPlayerService.isOn()) {
                mPlayerService.stopSelf();
            } else {
                setupShareIntent();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mPlayerService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);

        if (savedInstanceState != null) {
            TopTracksFragment topTracksFragment = (TopTracksFragment) getSupportFragmentManager()
                    .findFragmentByTag(TopTracksFragment.class.getName());
            if (topTracksFragment != null) {
                ArtistSearchFragment artistFragment = (ArtistSearchFragment) getSupportFragmentManager()
                        .findFragmentByTag(ArtistSearchFragment.class.getName());
                artistFragment.setTopTracksFragment(topTracksFragment);
            }

            if (getSupportActionBar() != null) {
                getSupportActionBar().setSubtitle(savedInstanceState.getString(STATE_SUBTITLE));
            }
            return;
        }

        ArtistSearchFragment artistFragment = new ArtistSearchFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.main_fragment_container, artistFragment,
                ArtistSearchFragment.class.getName()).commit();

        // If we're running on tablet then show the detail fragment here
        if (findViewById(R.id.main_fragment_detail_container) != null) {
            TopTracksFragment topTracksFragment = new TopTracksFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.main_fragment_detail_container, topTracksFragment,
                    TopTracksFragment.class.getName()).commit();
            artistFragment.setTopTracksFragment(topTracksFragment);
            mIsTablet = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPlayerService != null) {
            if (mNowPlayingMenu != null) {
                mNowPlayingMenu.setVisible(mPlayerService.isOn());
            }
            if (mShareMenu != null) {
                mShareMenu.setVisible(mPlayerService.isOn());
            }
        }

        if (mPlayerService == null) {
            Util.startPlayerService(mPlayerServiceConnection);
        } else {
            setupShareIntent();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (getSupportActionBar() != null) {
            CharSequence subtitle = getSupportActionBar().getSubtitle();
            if (subtitle != null) {
                outState.putString(STATE_SUBTITLE, subtitle.toString());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        mNowPlayingMenu =  menu.findItem(R.id.now_playing);
        mNowPlayingMenu.setVisible(mPlayerService != null && mPlayerService.isOn());

        mShareMenu =  menu.findItem(R.id.share_current_song);
        mShareMenu.setVisible(mPlayerService != null && mPlayerService.isOn());

        mShowNotificationMenu = menu.findItem(R.id.show_lockscreen);
        updateShowNotificationMenu();

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(mShareMenu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.country:
                showCountryPicker();
                return true;
            case R.id.now_playing:
                showNowPlaying();
                return true;
            case R.id.show_lockscreen:
                showNotification();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showCountryPicker() {
        String currentCountry = Util.getCurrentCountry();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.country_dialog_title);

        final String[] countries = Locale.getISOCountries();
        int counter = 0;
        int selection = -1;
        for (String country : countries) {
            if (selection == - 1 && country.equals(currentCountry)) {
                selection = counter;
            }
            counter++;
        }

        builder.setSingleChoiceItems(countries, selection,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SpotifyStreamerApp.getApp());
                        preferences.edit().putString(KEY_COUNTRY, countries[item]).apply();
                        dialog.dismiss();
                    }
                });

        mDialog = builder.create();
        mDialog.show();
    }

    private void updateShowNotificationMenu() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SpotifyStreamerApp.getApp());
        boolean showNotification = preferences.getBoolean(KEY_SHOW_NOTIFICATION, true);
        if (showNotification) {
            mShowNotificationMenu.setTitle(R.string.hide_notificaton);
        } else {
            mShowNotificationMenu.setTitle(R.string.show_notificaton);
        }
    }

    private void showNotification() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SpotifyStreamerApp.getApp());
        boolean showNotification = preferences.getBoolean(KEY_SHOW_NOTIFICATION, true);
        preferences.edit().putBoolean(KEY_SHOW_NOTIFICATION, !showNotification).apply();
        if (showNotification) {
            mShowNotificationMenu.setTitle(R.string.show_notificaton);
        } else {
            mShowNotificationMenu.setTitle(R.string.hide_notificaton);
        }

        if (mPlayerService != null) {
            mPlayerService.refreshNotification();
        }
    }

    private void showNowPlaying() {
        if (mIsTablet) {
            PlayerFragment player = new PlayerFragment();
            player.show(getSupportFragmentManager(), PlayerFragment.class.getName());
        } else {
            Intent intent = new Intent(this, PlayerActivity.class);
            startActivity(intent);
        }
    }

    private void setupShareIntent() {
        if (mShareActionProvider != null && mPlayerService != null && mPlayerService.getCurrentTrack() != null) {
            String url = mPlayerService.getCurrentTrack().getPreviewUrl();

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_default_subject));
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_default_message, url));

            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Subscribe
    public void songChanged(BusManager.SongChangedEvent event) {
        mNowPlayingMenu.setVisible(true);
        mShareMenu.setVisible(true);

        if (mPlayerService == null) {
            Util.startPlayerService(mPlayerServiceConnection);
        } else {
            setupShareIntent();
        }
    }
}





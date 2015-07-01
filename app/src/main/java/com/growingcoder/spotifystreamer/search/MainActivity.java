package com.growingcoder.spotifystreamer.search;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.growingcoder.spotifystreamer.R;
import com.growingcoder.spotifystreamer.core.BaseActivity;
import com.growingcoder.spotifystreamer.toptracks.TopTracksFragment;

/**
 * Displays a search field to search for artists on Spotify. It returns results in a list if there are any.
 *
 * @author Pierce
 * @since 6/7/2015.
 */
public class MainActivity extends BaseActivity {

    private static final String STATE_SUBTITLE = "STATE_SUBTITLE ";

    private Toolbar mToolbar;

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

            if(getSupportActionBar() != null) {
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
}





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

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);

        if (savedInstanceState != null) {
            return;
        }

        ArtistSearchFragment artistFragment = new ArtistSearchFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.main_fragment_container, artistFragment).commit();

        // If we're running on tablet then show the detail fragment here
        if (findViewById(R.id.main_fragment_detail_container) != null) {
            TopTracksFragment topTracksFragment = new TopTracksFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.main_fragment_detail_container, topTracksFragment).commit();
            artistFragment.setTopTracksFragment(topTracksFragment);
        }

        //TODO need to set the toolbar subtitle dynamically for the top tracks and then back to blank

        //TODO selected state for list item if we're in a tablet (might be able to do in layout?)
        // Make it saved in the saved instance state, also change the list data to be parcelable
        // and make it so that the text watcher does nothing when restoring state

        //TODO need to scroll back to the selection on rotation

        //TODO clear out the top tracks fragment if we type any text (maybe just set it to hidden)
    }
}





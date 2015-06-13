package com.growingcoder.spotifystreamer;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

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

        //TODO setup progressbar
    }
}





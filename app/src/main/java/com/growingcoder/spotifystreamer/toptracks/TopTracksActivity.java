package com.growingcoder.spotifystreamer.toptracks;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.growingcoder.spotifystreamer.R;
import com.growingcoder.spotifystreamer.core.BaseActivity;

/**
 * Activity used to display the top tracks for an artist.
 *
 * @author Pierce
 * @since 6/13/2015.
 */
public class TopTracksActivity extends BaseActivity {

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toptracks);

        mToolbar = (Toolbar) findViewById(R.id.toptracks_toolbar);
        mToolbar.setTitle(R.string.top_tracks_title);

        Bundle extras = getIntent().getExtras();
        String artistName = extras.getString(TopTracksFragment.KEY_BUNDLE_ARTIST_NAME);
        if (artistName != null) {
            mToolbar.setSubtitle(artistName);
        }

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        if (savedInstanceState != null) {
            return;
        }

        TopTracksFragment topTracksFragment = new TopTracksFragment();
        topTracksFragment.setArguments(extras);
        getSupportFragmentManager().beginTransaction().add(R.id.toptracks_fragment_container, topTracksFragment).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // This is the id for the back button on the toolbar
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

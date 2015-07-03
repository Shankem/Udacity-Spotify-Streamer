package com.growingcoder.spotifystreamer.search;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.growingcoder.spotifystreamer.R;
import com.growingcoder.spotifystreamer.core.BaseActivity;
import com.growingcoder.spotifystreamer.toptracks.TopTracksFragment;

import java.util.Locale;

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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.country:
                showCountryPicker();
                //TODO show dialog, handle rotation?
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showCountryPicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.country_dialog_title);

        final Locale[] locales = Locale.getAvailableLocales();
        CharSequence[] countries = new CharSequence[locales.length];
        int counter = 0;
        for (Locale locale : locales) {
            countries[counter] = locale.getDisplayCountry();
            counter++;
        }

        //TODO save and set checked item instead of -1

        builder.setSingleChoiceItems(countries, -1,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        locales[item].getCountry();
                        dialog.dismiss();
                    }
                });

        AlertDialog countryDialog = builder.create();
        countryDialog.show();
    }
}





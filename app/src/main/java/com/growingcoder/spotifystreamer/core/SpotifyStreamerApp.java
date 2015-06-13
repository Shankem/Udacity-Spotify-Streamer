package com.growingcoder.spotifystreamer.core;

import android.app.Application;

/**
 * Convenience application class used to initialize any values used globally.
 * @author Pierce
 * @since 6/7/2015.
 */
public class SpotifyStreamerApp extends Application {

    private static SpotifyStreamerApp sApp;

    @Override
    public void onCreate() {
        super.onCreate();
        sApp = this;
    }

    public static SpotifyStreamerApp getApp() {
        return sApp;
    }
}

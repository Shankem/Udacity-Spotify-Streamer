package com.growingcoder.spotifystreamer.core;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Image;

/**
 * Utility methods to be used statically.
 *
 * @author Pierce
 * @since 6/13/2015.
 */
public final class Util {
    private Util() {

    }

    /**
     * Used to get an image from the list closest to the specified size.
     */
    public static String getImageWithSize(List<Image> images, int pixelSize) {
        String url = null;
        if (images != null && images.size() > 0) {
            int lastImageSize = Integer.MAX_VALUE;
            for (Image image : images) {
                if (lastImageSize > image.width && image.width > pixelSize) {
                    lastImageSize = image.width;
                    url = image.url;
                }
            }

            // In case we couldn't find an appropriately sized image, just get the biggest one
            if(url == null) {
                url = images.get(0).url;
            }
        }

        // Picasso will crash if you provide a blank URL so we convert it to null
        if (url != null && "".equals(url.trim())) {
            url = null;
        }
        return url;
    }

    /**
     * Used to store data in shared preferences as JSON.
     */
    public static void cacheData(String key, List<? extends SpotifyJSONObject> data) {
        JSONArray array = new JSONArray();
        for (SpotifyJSONObject jsonObject : data) {
            array.put(jsonObject);
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SpotifyStreamerApp.getApp());
        preferences.edit().putString(key, array.toString()).apply();
    }

    /**
     * Used to retrieve data from shared preferences stored as JSON.
     */
    public static List<JSONObject> getCachedData(String key) {
        List<JSONObject> results = null;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SpotifyStreamerApp.getApp());
        String data = preferences.getString(key, null);

        if (data != null) {
            results = new ArrayList<JSONObject>();
            try {
                JSONArray array = new JSONArray(data);
                for (int i = 0; i < array.length(); i++) {
                    results.add(array.getJSONObject(i));
                }
            } catch (JSONException e) {
                Log.w(Util.class.getName(), "Error getting cached data", e);
            }
        }

        return results;
    }
}

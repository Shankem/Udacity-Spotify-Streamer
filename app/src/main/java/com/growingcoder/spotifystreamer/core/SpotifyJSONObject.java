package com.growingcoder.spotifystreamer.core;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Extension of the JSONObject class to add some convenience methods.
 *
 * @author Pierce
 * @since 6/14/2015.
 */
public class SpotifyJSONObject extends JSONObject {

    @Override
    public String getString(String name) {
        String result = null;
        try {
            result = super.getString(name);
        } catch (JSONException e) {
            Log.w(SpotifyJSONObject.class.getName(), "Error getting JSON String", e);
        }
        return result;
    }

    @Override
    public JSONObject put(String name, Object value) {
        try {
            super.put(name, value);
        } catch (JSONException e) {
            Log.w(SpotifyJSONObject.class.getName(), "Error setting JSON value", e);
        }
        return this;
    }
}

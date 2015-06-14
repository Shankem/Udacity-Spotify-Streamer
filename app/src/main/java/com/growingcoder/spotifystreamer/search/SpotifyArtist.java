package com.growingcoder.spotifystreamer.search;

import com.growingcoder.spotifystreamer.R;
import com.growingcoder.spotifystreamer.core.SpotifyJSONObject;
import com.growingcoder.spotifystreamer.core.SpotifyStreamerApp;
import com.growingcoder.spotifystreamer.core.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;

/**
 * Local model to store an artist from Spotify which can be saved locally.
 *
 * @author Pierce
 * @since 6/14/2015.
 */
public class SpotifyArtist extends SpotifyJSONObject{

    private static final String KEY_THUMBNAIL = "thumbnail";
    private static final String KEY_NAME = "name";
    private static final String KEY_ID = "id";

    public SpotifyArtist(Artist artist) {
        List<Image> images = artist.images;
        int thumbnailSize = SpotifyStreamerApp.getApp().getResources().getDimensionPixelSize(R.dimen.thumbnail_size);
        String url = Util.getImageWithSize(images, thumbnailSize);
        setThumbnailUrl(url);
        setName(artist.name);
        setId(artist.id);
    }

    public SpotifyArtist(JSONObject artist) {
        try {
            setThumbnailUrl(artist.getString(KEY_THUMBNAIL));
        } catch (JSONException e) {
        }
        try {
            setName(artist.getString(KEY_NAME));
        } catch (JSONException e) {
        }
        try {
            setId(artist.getString(KEY_ID));
        } catch (JSONException e) {
        }
    }

    public String getThumbnailUrl() {
        return getString(KEY_THUMBNAIL);
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        put(KEY_THUMBNAIL, thumbnailUrl);
    }

    public String getName() {
        return getString(KEY_NAME);
    }

    public void setName(String name) {
        put(KEY_NAME, name);
    }

    public String getId() {
        return getString(KEY_ID);
    }

    public void setId(String id) {
        put(KEY_ID, id);
    }
}

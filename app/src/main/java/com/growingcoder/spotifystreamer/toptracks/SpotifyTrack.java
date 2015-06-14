package com.growingcoder.spotifystreamer.toptracks;

import com.growingcoder.spotifystreamer.R;
import com.growingcoder.spotifystreamer.core.SpotifyJSONObject;
import com.growingcoder.spotifystreamer.core.SpotifyStreamerApp;
import com.growingcoder.spotifystreamer.core.Util;

import java.util.List;

import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Local model to store a track from Spotify which can be saved locally.
 *
 * @author Pierce
 * @since 6/14/2015.
 */
public class SpotifyTrack extends SpotifyJSONObject{

    private static final String KEY_THUMBNAIL = "thumbnail";
    private static final String KEY_NAME = "name";
    private static final String KEY_ALBUM_NAME = "album_name";
    private static final String KEY_ID = "id";

    public SpotifyTrack(Track track) {
        List<Image> images = track.album.images;
        int thumbnailSize = SpotifyStreamerApp.getApp().getResources().getDimensionPixelSize(R.dimen.thumbnail_size);
        String url = Util.getImageWithSize(images, thumbnailSize);
        setThumbnailUrl(url);
        setName(track.name);
        setAlbumName(track.album.name);
        setId(track.id);
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

    public String getAlbumName() {
        return getString(KEY_ALBUM_NAME);
    }

    public void setAlbumName(String albumName) {
        put(KEY_ALBUM_NAME, albumName);
    }

    public String getId() {
        return getString(KEY_ID);
    }

    public void setId(String id) {
        put(KEY_ID, id);
    }
}

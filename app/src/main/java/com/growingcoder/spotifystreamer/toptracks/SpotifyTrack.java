package com.growingcoder.spotifystreamer.toptracks;

import com.growingcoder.spotifystreamer.R;
import com.growingcoder.spotifystreamer.core.SpotifyJSONObject;
import com.growingcoder.spotifystreamer.core.SpotifyStreamerApp;
import com.growingcoder.spotifystreamer.core.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Local model to store a track from Spotify which can be saved locally.
 *
 * @author Pierce
 * @since 6/14/2015.
 */
public class SpotifyTrack extends SpotifyJSONObject{

    private static final String UNKNOWN_ARTIST = "Unknown Artist";

    private static final String KEY_THUMBNAIL = "thumbnail";
    private static final String KEY_NAME = "name";
    private static final String KEY_ALBUM_NAME = "album_name";
    private static final String KEY_ID = "id";
    private static final String KEY_PLAYER_IMAGE = "player_image";
    private static final String KEY_PREVIEW = "preview";
    private static final String KEY_ARTIST_NAME = "artist_name";

    public SpotifyTrack(Track track) {
        List<Image> images = track.album.images;
        int thumbnailSize = SpotifyStreamerApp.getApp().getResources().getDimensionPixelSize(R.dimen.thumbnail_size);
        int playerImageSize = SpotifyStreamerApp.getApp().getResources().getDimensionPixelSize(R.dimen.player_image_size);
        String thumbnailUrl = Util.getImageWithSize(images, thumbnailSize);
        String playerImageUrl = Util.getImageWithSize(images, playerImageSize);
        setThumbnailUrl(thumbnailUrl);
        setPlayerImageUrl(playerImageUrl);
        setPreviewUrl(track.preview_url);
        setName(track.name);
        setAlbumName(track.album.name);
        setArtistName(getArtistName(track));
        setId(track.id);
    }

    private String getArtistName(Track track) {
        List<ArtistSimple> artists = track.artists;
        String artist = UNKNOWN_ARTIST;
        if (artists != null && artists.size() > 0) {
            artist = artists.get(0).name;
        }

        return artist;
    }

    public SpotifyTrack(JSONObject track) {
        try {
            setThumbnailUrl(track.getString(KEY_THUMBNAIL));
        } catch (JSONException e) {
        }
        try {
            setPlayerImageUrl(track.getString(KEY_PLAYER_IMAGE));
        } catch (JSONException e) {
        }
        try {
            setPreviewUrl(track.getString(KEY_PREVIEW));
        } catch (JSONException e) {
        }
        try {
            setName(track.getString(KEY_NAME));
        } catch (JSONException e) {
        }
        try {
            setAlbumName(track.getString(KEY_ALBUM_NAME));
        } catch (JSONException e) {
        }
        try {
            setArtistName(track.getString(KEY_ARTIST_NAME));
        } catch (JSONException e) {
        }
        try {
            setId(track.getString(KEY_ID));
        } catch (JSONException e) {
        }
    }

    public String getThumbnailUrl() {
        return getString(KEY_THUMBNAIL);
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        put(KEY_THUMBNAIL, thumbnailUrl);
    }

    public String getPlayerImageUrl() {
        return getString(KEY_PLAYER_IMAGE);
    }

    public void setPlayerImageUrl(String playerImageUrl) {
        put(KEY_PLAYER_IMAGE, playerImageUrl);
    }

    public String getPreviewUrl() {
        return getString(KEY_PREVIEW);
    }

    public void setPreviewUrl(String previewUrl) {
        put(KEY_PREVIEW, previewUrl);
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

    public String getArtistName() {
        return getString(KEY_ARTIST_NAME);
    }

    public void setArtistName(String artistName) {
        put(KEY_ARTIST_NAME, artistName);
    }

    public String getId() {
        return getString(KEY_ID);
    }

    public void setId(String id) {
        put(KEY_ID, id);
    }
}

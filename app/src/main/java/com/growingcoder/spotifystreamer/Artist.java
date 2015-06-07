package com.growingcoder.spotifystreamer;

/**
 * Model to represent an artist from Spotify.
 * @author Pierce
 * @since 6/7/2015.
 */
public class Artist {
    private long mId;
    private String mName;
    private String mImageURL;

    public Artist(long mId, String mName, String mImageURL) {
        this.mId = mId;
        this.mName = mName;
        this.mImageURL = mImageURL;
    }

    public long getId() {
        return mId;
    }

    public void setId(long mId) {
        this.mId = mId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getImageURL() {
        return mImageURL;
    }

    public void setImageURL(String mImageURL) {
        this.mImageURL = mImageURL;
    }
}

package com.growingcoder.spotifystreamer.core;

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
        return url;
    }
}

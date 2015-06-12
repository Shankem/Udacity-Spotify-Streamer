package com.growingcoder.spotifystreamer;

import com.squareup.otto.Bus;

/**
 * Singleton to manage the Otto Bus, as recommended by Square.
 *
 * @author Pierce
 * @since 15-06-12.
 */
public final class BusManager {
    private static Bus sBus;

    private BusManager() {

    }

    public static Bus getBus() {
        if (sBus == null) {
            sBus = new Bus();
        }
        return sBus;
    }

    public static class ArtistSearchEvent {

    }

}

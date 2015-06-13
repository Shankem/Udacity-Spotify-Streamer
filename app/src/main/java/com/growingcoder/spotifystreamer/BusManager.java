package com.growingcoder.spotifystreamer;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

/**
 * Singleton to manage the Otto Bus, as recommended by Square.
 *
 * @author Pierce
 * @since 15-06-12.
 */
public final class BusManager {
    private static AndroidBus sBus;

    private BusManager() {

    }

    public static Bus getBus() {
        if (sBus == null) {
            sBus = new AndroidBus();
        }
        return sBus;
    }

    public static class ArtistSearchEvent {

    }

    /**
     * This extension is just to make sure we can post events from any thread.
     * This solution was found here: https://github.com/square/otto/issues/38
     * recommended by the developer of Otto.
     */
    private static class AndroidBus extends Bus {
        private final Handler mHandler = new Handler(Looper.getMainLooper());

        @Override
        public void post(final Object event) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                super.post(event);
            } else {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        AndroidBus.super.post(event);
                    }
                });
            }
        }
    }

}

package com.growingcoder.spotifystreamer;

import com.squareup.otto.Bus;

import java.util.ArrayList;

/**
 * Used to send events to the Otto event bus, but stores them in case the app is backgrounded so we can consume them later.
 *
 * @author Pierce
 * @since 15-06-12.
 */
public class EventBridge {

    private LifeCycleState mState;
    private ArrayList<Object> mEventList = new ArrayList<Object>();

    /**
     * Simple interface to check if the activity/fragment is resumed.
     *
     * @author Pierce
     */
    public interface LifeCycleState {

        /**
         * Used to let the EventBridge know about the state of the activity/fragment that posts to the bridge.
         */
        boolean allowsUIChanges();

    }

    public EventBridge(final LifeCycleState state) {
        mState = state;
    }

    /**
     * Post event to registered subscribers or saves it to be consumed when the lifecycle is resumed.
     */
    public void post(final Object event) {
        if (mState.allowsUIChanges()) {
            BusManager.getBus().post(event);
        } else {
            mEventList.add(event);
        }
    }

    /**
     * Posts all stored events and clears out the stored events.
     */
    public void consumeEvents() {
        Bus bus = BusManager.getBus();
        for (Object event : mEventList) {
            bus.post(event);
        }
        mEventList.clear();
    }
}

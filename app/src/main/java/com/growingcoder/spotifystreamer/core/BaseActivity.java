package com.growingcoder.spotifystreamer.core;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Common functionality for activities to inherit.
 *
 * @author Pierce
 * @since 15-06-12.
 */
public class BaseActivity extends AppCompatActivity implements EventBridge.LifeCycleState {

    private EventBridge mEventBridge;
    private boolean mAllowsUIChanges = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEventBridge = new EventBridge(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mAllowsUIChanges = true;
        BusManager.getBus().register(this);
        mEventBridge.consumeEvents();
    }

    @Override
    public void onPause() {
        super.onPause();
        mAllowsUIChanges = false;
        BusManager.getBus().unregister(this);
    }

    @Override
    public boolean allowsUIChanges() {
        return mAllowsUIChanges;
    }

    /**
     * Convenience method to post an event to the bridge.
     */
    protected final void postEvent(Object event) {
        mEventBridge.post(event);
    }
}

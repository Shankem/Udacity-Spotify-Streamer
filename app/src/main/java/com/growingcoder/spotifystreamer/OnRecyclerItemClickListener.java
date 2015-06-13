package com.growingcoder.spotifystreamer;

import android.view.View;

/**
 * Interface used to trigger a callback when you click on an item in a recycler view.
 *
 * @author Pierce
 * @since 6/13/2015.
 */
public interface OnRecyclerItemClickListener {

    void onItemClick(View v, int position);
}

package com.collinguarino.nowmanager;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ListView;

/**
 * Created by Collin on 11/22/13.
 *
 * This adds a slight bounce effect on the listview when scrolling to the top or bottom.
 * I'd like to turn this into a "Pull down to add a new event" trigger eventually.
 *
 */
public class OverScrollListView extends ListView {

    private final String TAG = "ListView";

    public OverScrollListView(Context context) {
        super(context);
        setOverScrollMode(OVER_SCROLL_ALWAYS);
        init();
    }

    public OverScrollListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setOverScrollMode(OVER_SCROLL_ALWAYS); // WAS "ALWAYS", trying this out
    }

    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX,
                                   int scrollY, int scrollRangeX, int scrollRangeY,
                                   int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {

        return super.overScrollBy(0, deltaY, 0, scrollY, 0, scrollRangeY, 0,
                30, isTouchEvent);

    }

    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX,
                                  boolean clampedY) {

        Log.v(TAG, "scrollX:" + scrollX + " scrollY:" + scrollY + " clampedX:"
                + clampedX + " clampedY:" + clampedX);

        /*Main main = new Main();
        main.createNewTimeCard(false);*/

        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);

    }

}

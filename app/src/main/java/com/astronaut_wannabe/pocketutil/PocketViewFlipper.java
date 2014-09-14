package com.astronaut_wannabe.pocketutil;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.AdapterViewFlipper;

/**
 * Created by ***REMOVED*** on 9/11/14.
 */
public class PocketViewFlipper extends AdapterViewFlipper {
    private float initialX;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = ev.getX();
                return true;
            case MotionEvent.ACTION_UP:
                float finalX = ev.getX();
                if (initialX > finalX) {
                    Log.d(VIEW_LOG_TAG,"left swipe");
                    showNext();
                } else {
                    Log.d(VIEW_LOG_TAG,"right swipe");
                    showNext();
                }
                return true;
            default:
                return super.onTouchEvent(ev);
        }
    }

    public PocketViewFlipper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}

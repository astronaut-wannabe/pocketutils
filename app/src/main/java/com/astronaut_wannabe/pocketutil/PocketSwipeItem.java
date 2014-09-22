package com.astronaut_wannabe.pocketutil;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class PocketSwipeItem extends LinearLayout {
    private static final String LOG_TAG = PocketSwipeItem.class.getSimpleName();

    private PocketSwipeCallbacks mCallbacks;
    private float mStartX;

    public PocketSwipeItem(Context context) {
        super(context);
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.list_item_pocket, this, true);
        mCallbacks = new PocketSwipeCallbacks() {
            @Override
            public void onLeftSwipe() {
                //noop
            }

            @Override
            public void onRightSwipe() {
                //noop
            }
        };
    }

    public PocketSwipeItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.list_item_pocket, this, true);
    }

    public PocketSwipeItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater.from(context).inflate(R.layout.list_item_pocket, this, true);
    }

    public void setCallbacks(PocketSwipeCallbacks callbacks){
        mCallbacks = callbacks;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        final int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mStartX = event.getX();
                return true;
            case MotionEvent.ACTION_UP:
                if (isSwipedLeft(event)) {
                    mCallbacks.onLeftSwipe();
                    return true;
                } else {
                    mCallbacks.onRightSwipe();
                    return true;
                }
            default:
                return super.onTouchEvent(event);
        }
    }


    private boolean isSwipedLeft(final MotionEvent event){
        return mStartX > event.getX();
    }

    public static interface PocketSwipeCallbacks{
        public void onLeftSwipe();
        public void onRightSwipe();
    }
}

package com.astronaut_wannabe.pocketutil;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class PocketSwipeItem extends RelativeLayout {
    private static final String LOG_TAG = PocketSwipeItem.class.getSimpleName();

    private PocketSwipeCallbacks mCallbacks;
    private float mStartX;

    public PocketSwipeItem(Context context) {
        super(context);
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

            @Override
            public void onTap() {

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
                if(isTapped(event)){
                    mCallbacks.onTap();
                    return true;
                }
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
        return mStartX > event.getX() + 10;
    }
    private boolean isTapped(final MotionEvent event){
        final float endX = event.getX();
        return endX <= mStartX + 10 && endX >= mStartX - 10;
    }

    public static interface PocketSwipeCallbacks{
        public void onLeftSwipe();
        public void onRightSwipe();
        public void onTap();
    }
}

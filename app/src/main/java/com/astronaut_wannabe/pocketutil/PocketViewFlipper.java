package com.astronaut_wannabe.pocketutil;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.AdapterViewFlipper;
import android.widget.Toast;

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
                Toast.makeText(getContext(), "Action up: x="+finalX,Toast.LENGTH_SHORT).show();
                if (initialX > finalX) {
                    Toast.makeText(getContext(), "Left = Delete: x="+initialX,Toast.LENGTH_SHORT).show();
                    showNext();
                } else {
                    Toast.makeText(getContext(), "Right = Save: x="+initialX,Toast.LENGTH_SHORT).show();
                    showNext();
                }
                return true;
            default:
                return super.onTouchEvent(ev);
        }
    }

    @Override
    public void setOnItemClickListener(OnItemClickListener listener) {
        Toast.makeText(getContext(), "setOnItemClickListener",Toast.LENGTH_SHORT).show();
        super.setOnItemClickListener(listener);
    }

    @Override
    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        Toast.makeText(getContext(), "setOnItemSelectedListener",Toast.LENGTH_SHORT).show();
        super.setOnItemSelectedListener(listener);
    }

    public PocketViewFlipper(Context context, AttributeSet attrs) {
        super(context, attrs);


    }
}

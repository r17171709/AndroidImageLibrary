package com.renyu.imagelibrary.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

public class RecordView extends AppCompatImageView {
    private OnGestureListener mOnGestureListener;

    public RecordView(Context context) {
        super(context);
    }

    public RecordView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RecordView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mOnGestureListener != null) {
                down = true;
                mOnGestureListener.onDown();
            }
        }
    };

    public boolean isDown() {
        return down;
    }

    private boolean down;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                ViewGroup parent = (ViewGroup) getParent();
                parent.requestDisallowInterceptTouchEvent(true);
                mHandler.sendEmptyMessageDelayed(0, 500);
                break;
            case MotionEvent.ACTION_MOVE:

                break;
            case MotionEvent.ACTION_UP:
                ViewGroup parent1 = (ViewGroup) getParent();
                parent1.requestDisallowInterceptTouchEvent(false);
                if (mOnGestureListener != null) {
                    mOnGestureListener.onUp();
                }
                initState();
                break;
        }
        return true;
    }

    public void initState() {
        down = false;
        mHandler.removeMessages(0);
    }

    public void setOnGestureListener(OnGestureListener listener) {
        this.mOnGestureListener = listener;
    }

    public interface OnGestureListener {
        void onDown();

        void onUp();
    }
}

package com.bandlink.air.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.webkit.WebView;

public class GpsWebview extends WebView {

	public GpsWebview(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	private int mLastY;
	private VelocityTracker mVelocityTracker;
	private int velocityY = 0;
	private int dis = 0;

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mLastY = (int) event.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			int y = (int) event.getY();
			dis = mLastY - y;
			mLastY = y;
			break;
		case MotionEvent.ACTION_UP:

			mVelocityTracker.computeCurrentVelocity(1000);
			int velocityY = (int) mVelocityTracker.getYVelocity();
			mVelocityTracker.recycle();
			mVelocityTracker = null;

			break;
		default:
			break;
		}

		return false;

	}

}

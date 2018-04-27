package com.bandlink.air.view;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/***
 * 重写SwipeRefreshLayout的OnTouch事件，如果子view已经滚动则将事件交给子view 否则让SwipeRefreshLayout操作
 * 
 * @author Kevin
 * 
 */
public class SwipeLayout extends SwipeRefreshLayout {
	// 子View
	ViewGroup viewGroup;

	public SwipeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// this.set
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean onTouchEvent(MotionEvent arg0) {
		// TODO Auto-generated method stub
		if (viewGroup != null && viewGroup.getScrollY() > 1) {
			this.setEnabled(false);
			return super.onTouchEvent(arg0);
		} else {
			// 让SwipeRefreshLayout处理本次事件
			return super.onTouchEvent(arg0);
		}

	}

	public ViewGroup getViewGroup() {
		return viewGroup;
	}

	public void setViewGroup(ViewGroup viewGroup) {
		this.viewGroup = viewGroup;
		this.viewGroup.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (SwipeLayout.this.viewGroup.getScrollY() <= 1) {
					SwipeLayout.this.setEnabled(true);
				}
				return false;
			}
		});
	}

}

package com.bandlink.air.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;

import com.bandlink.air.R;
import com.bandlink.air.bluetooth.protocol.Parser;
import com.nineoldandroids.animation.ArgbEvaluator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;

/***
 * Air 项目中支持变色的FragmentActivity都应继承的FragmentActivity基类
 * 
 * @author Kevin
 * 
 */
public abstract class LovefitFragmentActivity extends FragmentActivity {
	private SharedPreferences share;
	Dbutils db;
	private View mainView;
	private int currentColor;
	private int curStage = -1;

	/***
	 * 获得当前配色
	 * 
	 * @return argb32位颜色
	 */
	public int getCurrentColor() {
		return currentColor;
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		unregisterReceiver(receiver);
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		IntentFilter filter = new IntentFilter();
		filter.addAction(Parser.ACTION_STEP_CHANGED);
		filter.addAction("MilinkStep");
		registerReceiver(receiver, filter);
		share = getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		db = new Dbutils(share.getInt("UID", -1), this);
		Object[] steprs = db.getSpBasicData(MyDate.getFileName());// 0 step,1
																	// distance,2
		int step = 0;
		try {
			step = Integer.valueOf(steprs[0].toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		if (mainView != null) {
			if (step >= 10000) {
				mainView.setBackgroundColor(getResources().getColor(
						R.color.bg_10_12k));
				currentColor = getResources().getColor(R.color.bg_10_12k);
			} else if (step < 10000 && step >= 8000) {
				mainView.setBackgroundColor(getResources().getColor(
						R.color.bg_8_10k));
				currentColor = getResources().getColor(R.color.bg_8_10k);
			} else if (step < 8000 && step >= 6000) {
				mainView.setBackgroundColor(getResources().getColor(
						R.color.bg_6_8k));
				currentColor = getResources().getColor(R.color.bg_6_8k);
			} else if (step < 6000 && step >= 4000) {
				mainView.setBackgroundColor(getResources().getColor(
						R.color.bg_4_6k));
				currentColor = getResources().getColor(R.color.bg_4_6k);
			} else if (step < 4000 && step >= 2000) {
				mainView.setBackgroundColor(getResources().getColor(
						R.color.bg_2_4k));
				currentColor = getResources().getColor(R.color.bg_2_4k);
			} else {

				mainView.setBackgroundColor(getResources().getColor(
						R.color.bg_0_2k));
				currentColor = getResources().getColor(R.color.bg_0_2k);
			}
			LovefitSlidingActivity.changeBarColor(this,currentColor,mainView);
			OnColorChanged(currentColor);
		}
		super.onResume();
	}

	BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if (action.equals("MilinkStep")) {
				// 软件记步的步数变化
				int device = intent.getIntExtra("device", 0);
				if (device == 1) {
					int steps = intent.getIntExtra("step", 0);
					int cal = intent.getIntExtra("cal", 0);
					int dis = intent.getIntExtra("dis", 0);
					// OnStepChanged(steps, action);
					change(mainView, steps);
				}
			} else if (action.equals(Parser.ACTION_STEP_CHANGED)) {
				// air返回的步数
				int steps = (int) intent.getLongExtra("steps", 0);
				if(steps!=-1){
					change(mainView, steps);
				}
				// OnStepChanged(steps, action);
			}
		}
	};

	@Override
	public void setContentView(int res) {
		// TODO Auto-generated method stub
		mainView = LayoutInflater.from(this).inflate(res, null);
		changeBG(mainView);
		super.setContentView(mainView);
	}

	@Override
	public void setContentView(View view) {
		// TODO Auto-generated method stub
		mainView = view;
		changeBG(mainView);
		super.setContentView(mainView);
	}

	public View getMainView() {
		return mainView;
	}

	void changeBG(View view) {
		share = getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		db = new Dbutils(share.getInt("UID", -1), this);
		Object[] steprs = db.getSpBasicData(MyDate.getFileName());// 0 step,1
																	// distance,2
		int step = 0;
		try {
			step = Integer.valueOf(steprs[0].toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		change(view, step);
	}

	void change(View view, int step) {
		ValueAnimator va = null;

		if (step >= 10000) {
			if (curStage == 5) {
				return;
			}
			curStage = 5;
			va = ObjectAnimator.ofObject(view, "backgroundColor",
					new ArgbEvaluator(), currentColor,
					getResources().getColor(R.color.bg_10_12k));
			if (currentColor == 0) {
				va.setDuration(0);
			} else {
				va.setDuration(2400);
			}
			currentColor = getResources().getColor(R.color.bg_10_12k);
		} else if (step < 10000 && step >= 8000) {
			if (curStage == 4) {
				return;
			}
			curStage = 4;
			va = ObjectAnimator.ofObject(view, "backgroundColor",
					new ArgbEvaluator(), currentColor,
					getResources().getColor(R.color.bg_8_10k));
			if (currentColor == 0) {
				va.setDuration(0);
			} else {
				va.setDuration(2400);
			}
			currentColor = getResources().getColor(R.color.bg_8_10k);
		} else if (step < 8000 && step >= 6000) {
			if (curStage == 3) {
				return;
			}
			curStage = 3;
			va = ObjectAnimator.ofObject(view, "backgroundColor",
					new ArgbEvaluator(), currentColor,
					getResources().getColor(R.color.bg_6_8k));
			if (currentColor == 0) {
				va.setDuration(0);
			} else {
				va.setDuration(2400);
			}
			currentColor = getResources().getColor(R.color.bg_6_8k);
		} else if (step < 6000 && step >= 4000) {
			if (curStage == 2) {
				return;
			}
			curStage = 2;
			va = ObjectAnimator.ofObject(view, "backgroundColor",
					new ArgbEvaluator(), currentColor,
					getResources().getColor(R.color.bg_4_6k));
			if (currentColor == 0) {
				va.setDuration(0);
			} else {
				va.setDuration(2400);
			}
			currentColor = getResources().getColor(R.color.bg_4_6k);
		} else if (step < 4000 && step >= 2000) {
			if (curStage == 1) {
				return;
			}
			curStage = 1;
			va = ObjectAnimator.ofObject(view, "backgroundColor",
					new ArgbEvaluator(), currentColor,
					getResources().getColor(R.color.bg_2_4k));
			if (currentColor == 0) {
				va.setDuration(0);
			} else {
				va.setDuration(2400);
			}
			currentColor = getResources().getColor(R.color.bg_2_4k);
		} else {
			if (curStage == 0) {
				return;
			}
			curStage = 0;
			if (currentColor != 0) {
				va = ObjectAnimator.ofObject(view, "backgroundColor",
						new ArgbEvaluator(), currentColor, getResources()
								.getColor(R.color.bg_0_2k));
				currentColor = getResources().getColor(R.color.bg_0_2k);
				va.setDuration(2400);
			} else {
				view.setBackgroundColor(getResources()
						.getColor(R.color.bg_0_2k));
				currentColor = getResources().getColor(R.color.bg_0_2k);
			}

		}
		if (va != null) {
			try {
				va.addUpdateListener(new AnimatorUpdateListener() {

					@Override
					public void onAnimationUpdate(ValueAnimator animation) {
						// TODO Auto-generated method stub
						Integer cVal = (Integer) animation.getAnimatedValue();
						LovefitSlidingActivity.changeBarColor(
								LovefitFragmentActivity.this, cVal,mainView);
					}
				});
				va.start();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		LovefitSlidingActivity.changeBarColor(this,currentColor,mainView);
		OnColorChanged(currentColor);
	}

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		//arg0 = null;
		 
		LovefitSlidingActivity.changeBarColor(this,currentColor,mainView);
		super.onCreate(arg0);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
//		mainView.setVisibility(View.GONE);
//		mainView = null;
		super.onDestroy();
	}
 
	// 似乎一般的activity不需要实现这个抽象方法
	// /***
	// * 在从air 或 软件计步 得到的步数后，应该做的事
	// * @param step 步数
	// * @param action 标识来源 是发送广播的action
	// */
	public abstract void OnColorChanged(int color);
}

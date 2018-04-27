package com.bandlink.air.util;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.bandlink.air.AirHelp;
import com.bandlink.air.GpsSportActivity;
import com.bandlink.air.NewFriendActivity;
import com.bandlink.air.R;
import com.bandlink.air.SlideMainActivity;
import com.bandlink.air.bluetooth.protocol.Parser;
import com.bandlink.air.pingan.PinganRegisterActivity;
import com.bandlink.air.simple.InitUserData;
import com.bandlink.air.simple.SetTarget;
import com.bandlink.air.slidemenu.SlidingFragmentActivity;
import com.bandlink.air.user.LoginActivity;
import com.bandlink.air.user.Register;
import com.nineoldandroids.animation.ArgbEvaluator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;

/***
 * Air 项目中支持变色的SlidingFragmentActivity都应继承的Activity基类
 * 
 * @author Kevin
 * 
 */
public abstract class LovefitSlidingActivity extends SlidingFragmentActivity {
	private SharedPreferences share;
	Dbutils db;
	private View mainView;
	private CallFragment call;
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

	public CallFragment getCallBack() {
		return call;
	}

	@Override
	public void onAttachFragment(Fragment fragment) {
		// TODO Auto-generated method stub
		try {
			if (fragment instanceof CallFragment)
				call = (CallFragment) fragment;
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
		super.onAttachFragment(fragment);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		IntentFilter filter = new IntentFilter();
		filter.addAction(Parser.ACTION_STEP_CHANGED);
		filter.addAction("MilinkStep");
		filter.addAction(Parser.ACTION_STEP0805);
		registerReceiver(receiver, filter);
		share = getSharedPreferences(SharePreUtils.APP_ACTION, Context.MODE_PRIVATE);
		db = new Dbutils(share.getInt("UID", -1), this);
		Object[] steprs = db.getSpBasicData(MyDate.getFileName());// 0 step,1
																	// distance,2
		int step = 0;
		try {
			step = Integer.valueOf(steprs[0].toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		if (mainView != null) {
			if (step >= 10000) {
				mainView.setBackgroundColor(getResources().getColor(R.color.bg_10_12k));
				currentColor = getResources().getColor(R.color.bg_10_12k);
			} else if (step < 10000 && step >= 8000) {
				mainView.setBackgroundColor(getResources().getColor(R.color.bg_8_10k));
				currentColor = getResources().getColor(R.color.bg_8_10k);
			} else if (step < 8000 && step >= 6000) {
				mainView.setBackgroundColor(getResources().getColor(R.color.bg_6_8k));
				currentColor = getResources().getColor(R.color.bg_6_8k);
			} else if (step < 6000 && step >= 4000) {
				mainView.setBackgroundColor(getResources().getColor(R.color.bg_4_6k));
				currentColor = getResources().getColor(R.color.bg_4_6k);
			} else if (step < 4000 && step >= 2000) {
				mainView.setBackgroundColor(getResources().getColor(R.color.bg_2_4k));
				currentColor = getResources().getColor(R.color.bg_2_4k);
			} else {

				mainView.setBackgroundColor(getResources().getColor(R.color.bg_0_2k));
				currentColor = getResources().getColor(R.color.bg_0_2k);
			}
			LovefitSlidingActivity.changeBarColor(this, currentColor, mainView);
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
					OnStepChanged(steps, dis, cal, action);
					change(mainView, steps);
				}
			} else if (action.equals(Parser.ACTION_STEP_CHANGED)) {
				// air返回的步数
				int steps = (int) intent.getLongExtra("steps", 0);
				int dis = (int) intent.getLongExtra("dis", 0);
				int cal = (int) intent.getLongExtra("cal", 0);
				OnStepChanged(steps, dis, cal, action);
				if (steps != -1) {
					change(mainView, steps);
				}
			} else if (Parser.ACTION_STEP0805.equals(action)) {
				int steps = (int) intent.getLongExtra("step", 0);
				int dis = (int) intent.getLongExtra("dis", 0);
				int cal = (int) intent.getLongExtra("cal", 0);
				OnStepChanged(steps, dis, cal, action);
				if (steps != -1) {
					change(mainView, steps);
				}
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
		share = getSharedPreferences(SharePreUtils.APP_ACTION, Context.MODE_PRIVATE);
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		// 被回收后进入oncreate，会尝试以此bundle去恢复，导致叠加。所以丢弃系统保存的状态，相当于重新打开。
		savedInstanceState = null;

		changeBarColor(this, currentColor, mainView);
		super.onCreate(savedInstanceState);
	}

	public static void changeBarColor(Activity c, int x, View view) {
		Window window = c.getWindow();

		int colors = x & 0x00ffffff + 0xff000000;
		// float[] hsv = new float[3];
		// Color.colorToHSV(colors, hsv);
		// hsv[2] -= 30;
		// colors = Color.HSVToColor(hsv);

		int r = Color.red(colors) - 10;
		int g = Color.green(colors) - 10;
		int b = Color.blue(colors) - 10;
		if (r > 255) {
			r = 255;
		} else if (r < 0) {
			r = 0;
		}

		if (g > 255) {
			g = 255;
		} else if (g < 0) {
			g = 0;
		}

		if (b > 255) {
			b = 255;
		} else if (b < 0) {
			b = 0;
		}
		colors = Color.rgb(r, g, b);
		if (Build.VERSION.SDK_INT >= 21) {

			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(colors);
			// 添加上面2行代码之后在有虚拟按键的手机上，如nexus5，底部ui被遮挡
			if (Build.MODEL.contains("Nexus") && view != null
					&& (c instanceof LovefitSlidingActivity || c instanceof SlideMainActivity)) {
				SystemBarTintManager mTintManager = new SystemBarTintManager(c);
				view.setPadding(0, 0, 0, mTintManager.getConfig().getNavigationBarHeight());
			}
			return;
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

			SystemBarTintManager mTintManager = new SystemBarTintManager(c);
			mTintManager.setStatusBarTintEnabled(true);
			mTintManager.setNavigationBarTintEnabled(true);

			mTintManager.setStatusBarTintColor(colors);
			if (view != null) {
				if (c instanceof AirHelp || c instanceof LovefitActivity || c instanceof LoginActivity
						|| c instanceof Register || c instanceof GpsSportActivity || c instanceof PinganRegisterActivity
						|| c instanceof SetTarget || c instanceof InitUserData || c instanceof NewFriendActivity) {
					view.setPadding(0, mTintManager.getConfig().getStatusBarHeight(), 0, 0);
				}
			}

		}

	}

	int old = 0xff5f93ef;

	void change(View view, int step) {
		ValueAnimator va = null;

		if (currentColor != 0) {
			old = currentColor;
		}
		if (step >= 10000) {
			if (curStage == 5) {
				return;
			}
			curStage = 5;
			va = ObjectAnimator.ofObject(view, "backgroundColor", new ArgbEvaluator(), currentColor,
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
			va = ObjectAnimator.ofObject(view, "backgroundColor", new ArgbEvaluator(), currentColor,
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
			va = ObjectAnimator.ofObject(view, "backgroundColor", new ArgbEvaluator(), currentColor,
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
			va = ObjectAnimator.ofObject(view, "backgroundColor", new ArgbEvaluator(), currentColor,
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
			va = ObjectAnimator.ofObject(view, "backgroundColor", new ArgbEvaluator(), currentColor,
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
				va = ObjectAnimator.ofObject(view, "backgroundColor", new ArgbEvaluator(), currentColor,
						getResources().getColor(R.color.bg_0_2k));
				currentColor = getResources().getColor(R.color.bg_0_2k);
				va.setDuration(2400);
			} else {
				view.setBackgroundColor(getResources().getColor(R.color.bg_0_2k));
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
						LovefitSlidingActivity.changeBarColor(LovefitSlidingActivity.this, cVal, mainView);
					}
				});
				va.start();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		OnColorChanged(currentColor);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		// mainView.setVisibility(View.GONE);
		// mainView = null;
		super.onDestroy();
	}

	/***
	 * 在从air 或 软件计步 得到的步数后，应该做的事
	 * 
	 * @param step
	 *            步数
	 * @param action
	 *            标识来源 是发送广播的action
	 */
	public abstract void OnStepChanged(int step, int dis, int cal, String action);

	/**
	 * 背景换了
	 * 
	 * @param color
	 */
	public abstract void OnColorChanged(int color);

	/***
	 * 用于activity与其fragment通信
	 * 
	 * @author Kevin
	 * 
	 */
	public interface CallFragment {
		public void callFragment(int step, int dis, int cal, String action);

		/***
		 * 暂时是为了引导的一些控制和simple交互
		 * 
		 * @param code
		 *            指令
		 */
		public void makeOrder(int code);
	}
}

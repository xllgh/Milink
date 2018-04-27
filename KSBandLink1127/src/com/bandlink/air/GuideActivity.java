package com.bandlink.air;

import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bandlink.air.ble.BluetoothLeService;
import com.bandlink.air.ble.LogUtil;
import com.bandlink.air.simple.SyncQQHealth;
import com.bandlink.air.user.LoginActivity;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.MyDate;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.SystemBarTintManager;
import com.bandlink.air.util.Util;
import com.milink.android.lovewalk.bluetooth.service.StepService;
import com.nineoldandroids.animation.ArgbEvaluator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;

public class GuideActivity extends Activity {

	private ViewPager viewPager;

	private ViewPagerAdapter vpAdapter;

	private ArrayList<View> views;

	private LinearLayout linearLayout;

	private static final int[] pics = { R.drawable.guide1, R.drawable.guide2,
			R.drawable.guide3 };
	private String[] msg;
	private ImageView[] points;
	private int currentIndex;
	SharedPreferences preferences;
	View vs;
	Map<String, SoftReference<Bitmap>> imageCache;
	String openid = null, accesstoken = null, accesstokenexpiretime = null;

	private boolean CheckIntent() {
		// 检查是否其他安装程序启动自己
		Intent intent1 = this.getIntent();
		String action = intent1.getAction();

		int i = intent1.getFlags();
		int result = i & Intent.FLAG_ACTIVITY_NEW_TASK;
		result = i & Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED;
		Bundle mBundle = intent1.getExtras();
		if (mBundle != null && mBundle.containsKey("accesstoken")
				&& mBundle.containsKey("openid")) {// Bundle[{fling_action_key=2,
													// accesstoken=D3BC7E6AF997D9A4DD051EA436360451,
													// openid=39CEC48174CE7B0A3F2CDE5D6E7ECF32,
													// leftViewText=计步,
													// fling_code_key=48805039,
													// from=qqhealth, type=walk,
													// accesstokenexpiretime=1456444800}]
			openid = mBundle.getString("openid");
			accesstoken = mBundle.getString("accesstoken");
			accesstokenexpiretime = mBundle.getString("accesstokenexpiretime");
		} else {
			accesstoken = null;
			openid = null;
		}
		boolean isfind = false;
		if (!action.equals(Intent.ACTION_MAIN)) {
			isfind = true;
		} else if (result == 0) {
			isfind = true;
		} else {

		}
		return isfind;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Window window = this.getWindow();
		// window.setBackgroundDrawable(getWallpaper());
		if (Build.VERSION.SDK_INT >= 21) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(0x20000000);

		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

			SystemBarTintManager mTintManager = new SystemBarTintManager(this);
			mTintManager.setStatusBarTintEnabled(true);
			mTintManager.setNavigationBarTintEnabled(true);
			mTintManager.setStatusBarTintColor(0x20000000);
		}
		LogUtil.e("--进入闪屏时间--"
				+ new SimpleDateFormat("yyyy-MM-dd-HH：mm：ss-SSS")
						.format(new Date()));

		msg = new String[] { getString(R.string.guide1),
				getString(R.string.guide2), getString(R.string.guide3),
				getString(R.string.guide4) };
		if (CheckIntent()) {
			 Bundle bun = getIntent().getExtras();
			 this.finish();
			 Intent intent = new Intent(GuideActivity.this,
			 GuideActivity.class);
			 if(bun!=null){
			 intent.putExtras(bun);
			 }
			 intent.setAction(Intent.ACTION_MAIN);
			 intent.addCategory(Intent.CATEGORY_LAUNCHER);
			 intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			 intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			 this.startActivity(intent); 
			 return;
		}
		preferences = getApplicationContext().getSharedPreferences(
				SharePreUtils.APP_ACTION, Context.MODE_PRIVATE);
		Dbutils db = new Dbutils(preferences.getInt("UID", -1), this);

		Object device = db.getUserDeivce();
		if (device != null) {
			StartMilinkService(this);
		} else {
			if (preferences.getInt("UID", -2) <= -2) {

			} else {
				Toast.makeText(this, "您还有绑定手环设备，绑定成功即可同步至QQ健康", 3 * 1000)
						.show();
				;
			}

		}
		// 呈现欢迎图片 延时一秒载入引导页
		// ImageView i = new ImageView(this);
		// i.setScaleType(ScaleType.FIT_XY);
		// i.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
		// LayoutParams.MATCH_PARENT));
		// // i.setBackgroundResource(R.drawable.back);
		// // i.setImageResource(R.drawable.welcome);
		// i.setBackgroundResource(R.drawable.splash);
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);
		Bundle mBundle = getIntent().getExtras();
		if (mBundle != null && mBundle.containsKey("accesstoken")
				&& mBundle.containsKey("openid")) {
			toMainPage();
			return;
		}
		View mView = LayoutInflater.from(this).inflate(R.layout.splashlayout,
				null);
		mView.setBackgroundResource(R.drawable.bg_login);
		mView.findViewById(R.id.quality).startAnimation(
				AnimationUtils.loadAnimation(this, R.anim.splash_text));
		mView.findViewById(R.id.line).startAnimation(
				AnimationUtils.loadAnimation(this, R.anim.alpha_0_to_1));
		mView.findViewById(R.id.fit).startAnimation(
				AnimationUtils.loadAnimation(this, R.anim.splash_text_fit));
		mView.findViewById(R.id.life).startAnimation(
				AnimationUtils.loadAnimation(this, R.anim.alpha_0_to_08));
		setContentView(mView);
		handler.sendEmptyMessageDelayed(0, 2000);
		// startActivity(new Intent(this,SyncQQHealth.class));
	}

	private void StartMilinkService(Context context) {
		// 获取设备类型
		SharedPreferences share = context.getSharedPreferences(
				SharePreUtils.APP_ACTION, Context.MODE_PRIVATE);
		Dbutils db = new Dbutils(share.getInt("UID", -1), context);
		int device = db.getUserDeivceType();
		// 启动相应的服务
		// 0->蓝牙 1-> 3g 2->手机 3->ble 4->ant 5->air

		Intent intentblue = new Intent(context, BluetoothLeService.class);
		Intent intentstep = new Intent(context, StepService.class);
		Intent intentdct = new Intent("MilinkConfig");
		intentdct.putExtra("command", 2);
		if (SharePreUtils.getInstance(this).getMainMode() != 3) {
			switch (device) {

			case 5:// air device
				if (!context.getPackageManager().hasSystemFeature(
						PackageManager.FEATURE_BLUETOOTH_LE)) {

					return;
				}
				LogUtil.e("---BootstartBLE---"
						+ new SimpleDateFormat("yyyy-MM-dd-:HH:mm:ss:SSS")
								.format(new Date()));
				context.sendBroadcast(intentdct);
				context.stopService(intentstep);

				String address = db.getBTDeivceAddress();
				if (address != null && address.length() == 17) {
					LogUtil.e("---闪屏读蓝牙地址广播连接---"
							+ address
							+ "--"
							+ new SimpleDateFormat("yyyy-MM-dd-HH: mm: ss-SSS")
									.format(new Date()));
					// intent.putExtra("name", name);
					intentblue.putExtra("address", address);
					// 1 普通模式 2是固件升级
					intentblue.putExtra("command", 1);
					intentblue.putExtra("scanflag", 1);
					intentblue.putExtra("accesstoken", accesstoken);
					intentblue.putExtra("openid", openid);
					intentblue.putExtra("accesstokenexpiretime",
							accesstokenexpiretime);
					context.startService(intentblue);

					MyLog.v("BootReceiver", "Send Start BLE Service");
				}
				break;
			case 2:
				context.startService(intentstep);
				context.stopService(intentblue);
				break;
			default:
				try {
					// 注销软计步的监听器\
					context.sendBroadcast(intentdct);
					// 停止软计步服务
					context.stopService(intentstep);
					// 停止蓝牙服务
					context.stopService(intentblue);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}

				break;
			}
		}

	}

	View mainView;
	/***
	 * 线程通信
	 */
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case 0:
				// 引导
				boolean isfirst = preferences.getBoolean("isfirstload", false);

				if (!isfirst) {
					LogUtil.e(
							"TEST",
							"get message 0"
									+ Util.getTimeMMStringFormat("HH:mm:ss:SSS"));
					toMainPage();
				} else {
					getWindow().setFlags(
							WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
							WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
					mainView = getLayoutInflater().inflate(R.layout.guide_main,
							null);
					setContentView(mainView); 

					initView();
					initData();
				}

				break;
			case 1:
				break;
			case 2:
				getWindow().setFlags(
						WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
						WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
				mainView = getLayoutInflater().inflate(R.layout.guide_main,
						null);
				setContentView(mainView);

				initView();
				initData();
				break;
			case 3:
				GuideActivity.this.finish();
				break;
			}

			super.handleMessage(msg);
		}

	};

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	/**
	 */
	private void initView() {
		views = new ArrayList<View>();

		viewPager = (ViewPager) findViewById(R.id.guide_viewpager);

		vpAdapter = new ViewPagerAdapter(views);
		imageCache = new HashMap<String, SoftReference<Bitmap>>();
	}

	private void initData() {
		LayoutInflater inflater = getLayoutInflater();
		vs = LayoutInflater.from(this).inflate(R.layout.guide_item_5, null);
		for (int i = 0; i < pics.length; i++) {
			InputStream is = getResources().openRawResource(pics[i]);
			Bitmap mBitmap = BitmapFactory.decodeStream(is);
			// 软引用的Bitmap对象
			SoftReference<Bitmap> bitmapcache = new SoftReference<Bitmap>(
					mBitmap);
			// 添加该对象到Map中使其缓存
			imageCache.put(i + "", bitmapcache);

			SoftReference<Bitmap> bitmapcache_ = imageCache.get(i + "");
			// 取出Bitmap对象，如果由于内存不足Bitmap被回收，将取得空
			Bitmap bitmap_ = bitmapcache_.get();

			// TextView text = (TextView) v.findViewById(R.id.text);
			// text.setText(msg[i]);
			if (i == pics.length - 1) {
				((ImageView) vs.findViewById(R.id.img)).setImageBitmap(bitmap_);
				((Button) vs.findViewById(R.id.btn))
						.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								toMainPage();
							}
						});
				views.add(vs);
			} else {
				ImageView image = new ImageView(this);
				image.setImageBitmap(bitmap_);
				views.add(image);
			}

		}
		// views.add(vs);
		viewPager.setAdapter(vpAdapter);
		viewPager.setOnPageChangeListener(new pageListener());

		initPoint();
	}

	void toMainPage() {
		try {
			// unregisterReceiver(receiver);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Intent intent = new Intent();
		if (preferences.getInt("UID", -2) <= -2) {
			intent.setClass(this, LoginActivity.class);
			intent.putExtra("value", "re");
			intent.putExtra("accesstoken", accesstoken);
			intent.putExtra("openid", openid);
			intent.putExtra("accesstokenexpiretime", accesstokenexpiretime);
		} else {
			intent.putExtra("fromguide", true);
			intent.setClass(this, SlideMainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_CLEAR_TASK);
			if (accesstoken != null) {
				Dbutils db = new Dbutils(preferences.getInt("UID", -1), this);
				Object[] steprs = db.getSpBasicData(MyDate.getFileName());
				SyncQQHealth.SyncQQToday(this, accesstoken, openid,
						Integer.valueOf(steprs[0].toString()),
						Integer.valueOf(steprs[1].toString()),
						(int) (Integer.valueOf(steprs[0].toString()) / 2.5f),
						Calendar.getInstance().getTimeInMillis() / 1000,
						Integer.valueOf(steprs[2].toString()),true);
			}

		}

		LogUtil.e("TEST",
				"start intent" + Util.getTimeMMStringFormat("HH:mm:ss:SSS"));
		startActivity(intent);

		preferences.edit().putBoolean("isfirstload", false).commit();
		handler.sendEmptyMessageDelayed(3, 1000);
	}

	/**
	 * 初始化底部小点
	 */
	private void initPoint() {
		linearLayout = (LinearLayout) findViewById(R.id.ll);

		points = new ImageView[pics.length];
		for (int i = 0; i < pics.length; i++) {
			points[i] = (ImageView) linearLayout.getChildAt(i);
			points[i].setEnabled(true);
			points[i].setOnClickListener(new pointListener());
			points[i].setTag(i);
		}
		currentIndex = 0;
		points[currentIndex].setEnabled(false);
	}

	boolean needTest = true;
	private ImageView phone;
	private Button ignore;
	private TextView msg1, msg2, title;

	private class pageListener implements OnPageChangeListener {

		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub

		}

		int curcolor = 0xff5e93ef;

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			ValueAnimator va = null;
			switch (arg0) {
			case 0:
				va = ObjectAnimator.ofObject(mainView, "backgroundColor",
						new ArgbEvaluator(), curcolor,
						getResources().getColor(R.color.bg_4_6k));
				curcolor = getResources().getColor(R.color.bg_4_6k);
				break;
			case 1:
				va = ObjectAnimator.ofObject(mainView, "backgroundColor",
						new ArgbEvaluator(), curcolor,
						getResources().getColor(R.color.bg_6_8k));
				curcolor = getResources().getColor(R.color.bg_6_8k);
				break;
			case 2:
				va = ObjectAnimator.ofObject(mainView, "backgroundColor",
						new ArgbEvaluator(), curcolor,
						getResources().getColor(R.color.bg_10_12k));
				curcolor = getResources().getColor(R.color.bg_10_12k);
				break;
			}
			if (va != null) {
				va.setDuration(2000);
				va.start();
			}

			if (arg0 == 3) {
				linearLayout.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void onPageSelected(int position) {

			setCurDot(position);
		}

	}

	int testStep = 0;
	long testStart = 0;
	BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_SCREEN_ON)) {
				screenDown = false;
				if (testStep >= 1) {
					title.setText(R.string.check_result_ok);
					ignore.setText(R.string.finish);

				} else {
					if (System.currentTimeMillis() - testStart > 5000) {
						title.setText(R.string.check_result_bad);
						ignore.setText(R.string.finish);
					} else {
						title.setText(R.string.check_result_timeshort);
					}
				}
				try {
					// unregisterReceiver(receiver);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
				screenDown = true;
				// 开启服务，
				mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
				mSensor = mSensorManager
						.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

				testStart = System.currentTimeMillis();

			}
		}
	};
	SensorManager mSensorManager;
	Sensor mSensor;
	boolean screenDown = false;
	boolean hadVibrator = false;

	private class pointListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			int position = (Integer) v.getTag();
			setCurView(position);
			setCurDot(position);
		}

	}

	private void setCurView(int position) {
		if (position < 0 || position >= pics.length) {
			return;
		}
		viewPager.setCurrentItem(position);
	}

	private void setCurDot(int positon) {
		if (positon < 0 || positon > pics.length - 1 || currentIndex == positon) {
			return;
		}
		points[positon].setEnabled(false);
		points[currentIndex].setEnabled(true);

		currentIndex = positon;
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	class ViewPagerAdapter extends PagerAdapter {

		private ArrayList<View> views;

		public ViewPagerAdapter(ArrayList<View> views) {
			this.views = views;
		}

		@Override
		public int getCount() {
			if (views != null) {
				return views.size();
			}
			return 0;
		}

		@Override
		public Object instantiateItem(View view, int position) {

			((ViewPager) view).addView(views.get(position), 0);

			return views.get(position);
		}

		@Override
		public boolean isViewFromObject(View view, Object arg1) {
			return (view == arg1);
		}

		@Override
		public void destroyItem(View view, int position, Object arg2) {
			((ViewPager) view).removeView(views.get(position));
		}
	}

}
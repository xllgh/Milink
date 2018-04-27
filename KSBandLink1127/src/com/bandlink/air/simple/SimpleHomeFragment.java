package com.bandlink.air.simple;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bandlink.air.DataChartActivity;
import com.bandlink.air.MilinkApplication;
import com.bandlink.air.MyLog;
import com.bandlink.air.R;
import com.bandlink.air.SlideMainActivity;
import com.bandlink.air.SportsDevice;
import com.bandlink.air.ble.AirScaleService;
import com.bandlink.air.ble.AirTemperatureService;
import com.bandlink.air.ble.BluetoothLeService;
import com.bandlink.air.ble.Converter;
import com.bandlink.air.ble.LogUtil;
import com.bandlink.air.bluetooth.protocol.Parser;
import com.bandlink.air.club.SleepActivity;
import com.bandlink.air.simple.NewProgress.OnNewProgressDown;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.LovefitSlidingActivity.CallFragment;
import com.bandlink.air.util.MainInterface;
import com.bandlink.air.util.MyDate;
import com.bandlink.air.util.PrintScreen;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Util;
import com.bandlink.air.view.NoRegisterDialog;
import com.bandlink.air.view.WeightRuler;
import com.bandlink.air.wxapi.WXEntryActivity;
import com.milink.android.airscale.FormulaConverter;
import com.milink.android.airscale.ScaleData;
import com.milink.android.airscale.Tools;
import com.nineoldandroids.animation.ObjectAnimator;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

@SuppressLint("NewApi")
public class SimpleHomeFragment extends Fragment implements OnClickListener, OnNewProgressDown, CallFragment {

	private View mView;
	private BG point;
	private NewProgress progress;
	private MainInterface minterface;
	private RelativeLayout mainBoard;
	private LinearLayout stepBoard;
	private LinearLayout weightBoard;
	private ActionbarSettings actionbar;
	private Dbutils db;
	private int uid;
	private SharedPreferences share;
	private int devicetype;
	View pageone;
	LinearLayout border;
	private TextView tv1, tv2, tv3, unit1, unit2, unit3;
	LinearLayout l1, l2;
	RelativeLayout l3;
	private boolean isPause = true;
	private int count = 0;
	private ProgressBar loading;
	private TextView tips;
	public static final String ACTION_GET0804 = "com.air.android.getsleep";

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		minterface = (MainInterface) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		super.onCreate(savedInstanceState);
	}

	int awakeIndex = 0;
	int sleepIndex = 0;
	int aweakTimes = 0, deep_time = 0, active_time = 0, light_time = 0;
	int realTo, realFrom;

	boolean oncreate = false;
	public Bitmap shareScreen = null;
	float tSize;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		// LogUtil.e("TEST",
		// "simple start"+Util.getTimeMMStringFormat("HH:mm:ss:SSS"));
		Bundle data = getArguments();
		// 因为程序启动的时候启动服务去读ble数据了，如果在oncreate去读
		// 会导致0802的时候crc校验失败。所以只有当在侧菜单进入此页才去去
		if (data != null && data.containsKey("oncreate")) {
			oncreate = data.getBoolean("oncreate", false);
		} else {
			oncreate = false;
		}
		share = getActivity().getSharedPreferences(SharePreUtils.APP_ACTION, Context.MODE_PRIVATE);
		LogUtil.e("--首页时间--" + new SimpleDateFormat("yyyy-MM-dd-HH：mm：ss-SSS").format(new Date()));
		mView = inflater.inflate(R.layout.ac_anim, null);
		mView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				switchMode(mView);
			}
		});
		mView.findViewById(R.id.share_border).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				pop = initPopupWindow();
				pop.showAsDropDown(mView.findViewById(R.id.t1));
			}
		});
		tips = (TextView) mView.findViewById(R.id.tips);
		// 主面板容器
		loading = (ProgressBar) mView.findViewById(R.id.loading);
		mainBoard = (RelativeLayout) mView.findViewById(R.id.t2);
		mainBoard.setOnClickListener(this);

		actionbar = new ActionbarSettings(mView, new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (currentDate.equals(MyDate.getFileName())) {
					// AllRankActivity a = new AllRankActivity();
					// FragmentTransaction ft = getFragmentManager()
					// .beginTransaction();
					// //
					// setCustomAnimations()必须位于replace()之前，否则效果不起所中。它的两个参数分别为enter，exit的效果。系统目前提供两个效果，分别为android.R.animator.fade_in和android.R.animator.fade_out
					// ft.setCustomAnimations(R.anim.hold, R.anim.fade);
					// ft.replace(R.id.main_frame, a);
					// ft.commit();
					Intent i = new Intent(getActivity(), RankActivity.class);
					// startActivity(i);
					// getActivity().overridePendingTransition(R.anim.hold,
					// R.anim.fade);
					actionbar.setRightVisible(false);
				} else {
					pushData(currentDate = MyDate.getFileName(), 1);
					count = 0;

					((TextView) mainBoard.findViewById(R.id.date)).setText(getString(R.string.today_tem));
					actionbar.setRightVisible(false);
					actionbar.setTopRightIcon(R.drawable.friend);
				}

			}
		}, minterface);
		actionbar.setRightVisible(false);
		actionbar.setTopRightIcon(R.drawable.friend);
		// 下方详情框
		border = (LinearLayout) mView.findViewById(R.id.border);
		// 详情页1
		pageone = getActivity().getLayoutInflater().inflate(R.layout.pagerone, null);
		// step.setTypeface(Typeface.createFromAsset(getResources().getAssets(),
		// "font/Courier.ttf"));
		// step_aim.setTypeface(Typeface.createFromAsset(getResources()
		// .getAssets(), "font/Courier.ttf"));
		// 控件各项数据
		border.addView(pageone);
		tv1 = (TextView) border.findViewById(R.id.border_1_txt);
		tv2 = (TextView) border.findViewById(R.id.border_2_txt);
		tv3 = (TextView) border.findViewById(R.id.border_3_txt);

		unit1 = (TextView) border.findViewById(R.id.border_1_name);
		unit2 = (TextView) border.findViewById(R.id.border_2_name);
		unit3 = (TextView) border.findViewById(R.id.border_3_name);
		l1 = (LinearLayout) border.findViewById(R.id.li1);
		l2 = (LinearLayout) border.findViewById(R.id.li2);
		l3 = (RelativeLayout) border.findViewById(R.id.li3);
		tSize = tv3.getTextSize();
		l1.setOnClickListener(this);
		l2.setOnClickListener(this);
		l3.setOnClickListener(this);
		// 步数模型默认
		stepBoard = (LinearLayout) (ViewGroup) getActivity().getLayoutInflater().inflate(R.layout.step_board, null);
		point = (BG) stepBoard.findViewById(R.id.small_point);
		progress = (NewProgress) stepBoard.findViewById(R.id.progress);
		progress.setOnNewProgressDown(this);
		mainBoard.addView(stepBoard);
		mainBoard.getLocationInWindow(pos);
		currentDate = MyDate.getFileName();
		actionbar.setTitle(getString(R.string.sport));
		// 脚图标点击
		mainBoard.findViewById(R.id.step_icon).setOnClickListener(SimpleHomeFragment.this);

		// LogUtil.e("TEST",
		// "simple over"+Util.getTimeMMStringFormat("HH:mm:ss:SSS"));
		return mView;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		stopAirScaleService();
		getActivity().unregisterReceiver(airReceiver);
		super.onDestroy();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		oncreate = false;
		isPause = true;
		Intent intent = new Intent(BluetoothLeService.ACTION_AIR_CONTROL);
		intent.putExtra("task", 5);
		intent.putExtra("args", 1);
		getActivity().sendBroadcast(intent);
		super.onPause();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		isPause = false;
		IntentFilter filter = new IntentFilter();
		filter.addAction(Parser.ACTION_GET_HEARTRATE);
		// filter.addAction("MilinkStep");
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(ACTION_GET0804);
		filter.addAction(Parser.ACTION_STEP0805);

		filter.addAction(BluetoothLeService.ACTION_AIR_RSSI_STATUS);
		filter.addAction(AirScaleService.ACTION_BLE_AIRSCALE_DATA);
		filter.addAction(ACTION_START);
		filter.addAction(AirTemperatureService.ACTION_BLE_AIRTempr_DATA);

		getActivity().registerReceiver(airReceiver, filter);
		share = getActivity().getSharedPreferences(SharePreUtils.APP_ACTION, Context.MODE_PRIVATE);

		weightBoard = (LinearLayout) (ViewGroup) getActivity().getLayoutInflater().inflate(R.layout.weight_board, null);
		// 刷新数据
		db = new Dbutils(share.getInt("UID", -1), getActivity());
		devicetype = db.getUserDeivceType();
		if (oncreate)
			loading.setVisibility(View.VISIBLE);
		if (currentDate == null) {
			currentDate = MyDate.getFileName();
		}
		if (devicetype == 5 && Build.VERSION.SDK_INT >= 18 && mode == NewProgress.STEPMODE) {
			getUserData(currentDate, false);
			if (oncreate) {
				sendRefreshAir();
			}

		} else if (mode == NewProgress.WEIGHTMODE) {
			getUserData(currentDate, true);
		} else {
			loadData(currentDate);
		}
		String s = currentDate;
		if (s.equals(MyDate.getYesterDay())) {
			s = getString(R.string.yesterday);
		} else if (s.equals(Util.getBeforeAfterDate(MyDate.getFileName(), -2).toString())) {
			s = getString(R.string.beforeyesterday);
		} else if (s.equals(MyDate.getFileName())) {
			s = getString(R.string.today_tem);
		}
		TextView t = ((TextView) mainBoard.findViewById(R.id.date));
		t.setTypeface(MilinkApplication.NumberFace);
		t.setText(s);

		mView.findViewById(R.id.ach).setVisibility(View.GONE);
		mView.findViewById(R.id.achBtn).setVisibility(View.GONE);
		mView.findViewById(R.id.achBtn).setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					Intent intent = new Intent(getActivity(), AchievementActivity.class);
					intent.putExtra("date", currentDate);
					// startActivity(intent);
					// getActivity().overridePendingTransition(
					// R.anim.rise, R.anim.fall);
					return true;
				} else {
					return false;
				}
			}
		});
		mView.findViewById(R.id.ach).startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.redown));
		super.onResume();
	}

	void startAirScaleService() {

		// byte[] ba1 = new byte[]{0x55,(byte) 0xAA,(byte) 0x84,(byte)
		// 0xD8,0x02,(byte) 0x9F,0x01,(byte) 0xFB};
		// final Intent intent = new
		// Intent(AirScaleService.ACTION_BLE_AIRSCALE_DATA);
		// intent.putExtra("data", ba1);
		// getActivity().sendBroadcast(intent);

		Intent intentScale = new Intent(getActivity().getApplicationContext(), AirScaleService.class);
		getActivity().startService(intentScale);
	}

	public static final String ACTION_START = "com.milink.air.start";
	private BroadcastReceiver airReceiver = new BroadcastReceiver() {

		@SuppressLint("NewApi")
		@Override
		public void onReceive(Context arg0, final Intent intent) {
			// 如果显示的不是今日的则不要显示在界面上
			if (!currentDate.equals(MyDate.getFileName())) {
				return;
			}
			final String action = intent.getAction();
			if (action.equals(Parser.ACTION_GET_HEARTRATE)) {
				if (intent.getIntExtra("type", 0) == 1) {
					refreshHandler.obtainMessage(5).sendToTarget();
					;
				} else {
					refreshHandler.obtainMessage(4, intent.getIntExtra("hr", 0) + "").sendToTarget();
					;
				}

			}
			if (action.equals(AirTemperatureService.ACTION_BLE_AIRTempr_DATA)) {
				getActivity().runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						byte[] bb = intent.getByteArrayExtra("data");
						String s = String.format("%.2f", ((bb[0] << 8) + bb[1]) / 100f);
						((TextView) mainBoard.getChildAt(0).findViewById(R.id.step)).setText(s);
					}
				});
			}
			// 得到0804计算睡眠
			if (action.equals(ACTION_GET0804)) {
				if (isPause) {
					return;
				}
				if (mode == NewProgress.STEPMODE) {
					getUserData(currentDate, false);
				}
				// 实时刷新
				Intent intent2 = new Intent(BluetoothLeService.ACTION_AIR_CONTROL);
				intent2.putExtra("task", 5);
				intent2.putExtra("args", 0);
				getActivity().sendBroadcast(intent2);

			}
			if (action.equals(ACTION_START)) {
				if (isPause) {
					return;
				}
				// 实时刷新
				Intent intent2 = new Intent(BluetoothLeService.ACTION_AIR_CONTROL);
				intent2.putExtra("task", 5);
				intent2.putExtra("args", 0);
				getActivity().sendBroadcast(intent2);
			}
			// if (Parser.ACTION_STEP0805.equals(action)
			// && mode == NewProgress.STEPMODE) {
			//
			// ((TextView) mainBoard.getChildAt(0).findViewById(R.id.step))
			// .setText(intent.getLongExtra("step", 0) + "");
			// tv1.setText(String.format("%.1f",
			// intent.getDoubleExtra("dis", 0)));
			// tv2.setText(String.format("%.1f",
			// intent.getDoubleExtra("cal", 0)));
			//
			// }
			// 接受屏幕状态，决定是否刷新界面
			if (action.equals(Intent.ACTION_SCREEN_OFF)) {
				oncreate = false;
			}
			if (action.equals(Intent.ACTION_SCREEN_ON)) {
				oncreate = true;
			}
			if (action.equals(AirScaleService.ACTION_BLE_AIRSCALE_DATA) && mode == NewProgress.WEIGHTMODE) {
				try {
					LogUtil.e("***get weight from broadcast", true);
					byte[] ba1 = intent.getByteArrayExtra("data");
					// if(ba1.length == 1 && ba1[0] == (byte)0x00)
					// {
					// stopAirScaleService();
					// return;
					// }
					boolean bbb = Tools.checkBufferSum(ba1);
					// bbb=true;
					if (bbb) {

						ScaleData ss = Tools.buffer2ScaleData(ba1);
						MyLog.v("pp11", ss.getWeight() + "/" + ss.getResistance());

						MyLog.v("jjj00", "data: " + ba1.length + ": " + Converter.byteArrayToHexString(ba1) + "/"
								+ ss.getWeight() + "/" + ss.getResistance() + "/" + ss.getType());
						float weight = ss.getWeight();
						int res = ss.getResistance();
						String strs = "";
						for (byte x : ba1) {
							strs += Converter.byteToHexString(x) + "-";
						}
						Log.e("---", strs);
						if (ba1[2] == (byte) 0x82 && !isPause) {
							((TextView) mainBoard.getChildAt(0).findViewById(R.id.step))
									.setText(String.format("%1$.1f", weight));
							float temp = Float.valueOf(String.format("%1$.1f", weight)) - standardW;
							String str = "";
							if (weight == 0) {
								str = getString(R.string.connect_waitfortest);

							} else {
								if (temp < 0) {
									// 比标准轻
									str = String.format(getString(R.string.weight_less), Math.abs(temp));
								} else if (temp > 0) {
									str = String.format(getString(R.string.weight_more), Math.abs(temp));
								}

							}
							((TextView) mainBoard.getChildAt(0).findViewById(R.id.step_aim)).setText(str);
						} else if (ba1[2] == (byte) 0x83) {
							((TextView) mainBoard.getChildAt(0).findViewById(R.id.step_aim))
									.setText(R.string.connect_waitforcalc);

						} else if (ba1[2] == (byte) 0x84) {
							stopAirScaleService();
							float height = 0;
							// male 1, female 2
							int sex = 0;
							int age = 20;

							Object[] data;

							data = db.getUserProfile();

							try {
								if (data != null) {

									Double ddd = (Double) data[5];
									height = Float.parseFloat(ddd.toString());
									if ((int) (height) == 0) {
										height = 170;
									}
									sex = 1;
									try {
										sex = (Integer) data[4];
									} catch (Exception e) {
										sex = 1;
									}
								} else {
									height = 170;
									sex = 1;
								}

							} catch (Exception ee) {
								// TODO: handle exception
								MyLog.v("aa", ee.toString());
							}

							sex -= 1;
							// change to male 1, female 0
							sex = 1 - sex;

							int year = share.getInt("year", 1990);
							int month = share.getInt("month", 1);
							age = Calendar.getInstance().get(Calendar.YEAR) - year;
							if (age < 0)
								age = 20;

							calcWeight(height, age, sex, res);
						}

					}

				} catch (Exception e) {
					// TODO: handle exception
					MyLog.v("pp0", e.toString());
				}
			}
		}

	};

	void calcWeight(float height, int age, int sex, int res) {
		double bmi = weight * 10000 / (height * height);
		if (bmi < 0 || bmi > 120) {
			bmi = 0;
		}
		double fat = FormulaConverter.getFat(weight, height, age, res, sex);
		double water = FormulaConverter.getWater((float) fat, res, sex);
		double muscle = FormulaConverter.getMuscle(weight, height, age, sex);
		double bone = FormulaConverter.getBone(weight, height, age, sex);

		if (res == 0) {
			fat = water = muscle = bone = 0;
		}
		if (fat < 0 || water < 0 || muscle < 0 || bone < 0) {
			fat = water = muscle = bone = 0;
			((TextView) mainBoard.getChildAt(0).findViewById(R.id.step_aim)).setText(R.string.slim_height_age_error);
		}
		String time = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		db.SaveWgBasicDataFull(weight, bmi, fat, water, muscle, bone, 1, time, time);
		uploadWeight(1, currentDate, fat, water, muscle, weight + "", bone, bmi);
		Message m = new Message();
		m.what = 3;
		refreshHandler.sendMessage(m);
	}

	private void showSlimDialog() {
		ProgressDialog slimDialog = ProgressDialog.show(Util.getThemeContext(getActivity()), null,
				getString(R.string.scanning));
		ImageView im = new ImageView(getActivity());
		slimDialog.setView(im);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(getActivity(), DataChartActivity.class);
		switch (v.getId()) {
		case R.id.step_icon * 10:
			if (mode == NewProgress.STEPMODE) {

				// 步数 距离 卡路里 体重 BMI
				intent.putExtra("type", 0);
				intent.putExtra("nowtime", currentDate);
				startActivity(intent);
			} else if (mode == NewProgress.WEIGHTMODE) {
				// 步数 距离 卡路里 体重 BMI
				intent.putExtra("type", 3);
				intent.putExtra("nowtime", currentDate);
				startActivity(intent);
			}
			break;
		case R.id.li1 * 10:
			if (mode == NewProgress.STEPMODE) {
				// 步数 距离 卡路里 体重 BMI
				intent.putExtra("type", 1);
				intent.putExtra("nowtime", currentDate);
				startActivity(intent);
			}
			break;

		case R.id.li2 * 10:
			if (mode == NewProgress.STEPMODE) {
				intent.putExtra("type", 2);
				intent.putExtra("nowtime", currentDate);
				startActivity(intent);
			}
			break;

		case R.id.li3:
			((SlideMainActivity) getActivity()).switchContent(new SleepActivity());
			break;
		}
	}

	int mode = 0;
	boolean inswitch = false;

	void mainBardAnim(final View v) {
		if (loading != null) {
			loading.setVisibility(View.GONE);
		}
		View pageOld = mainBoard.getChildAt(0);
		Animation dismiss = AnimationUtils.loadAnimation(getActivity(), R.anim.simple_dismiss);
		// RotateAnimation dismiss2 = new RotateAnimation(
		// border.getWidth() / 2.0f, border.getHeight() / 2.0f,
		// RotateAnimation.ROTATE_DECREASE);
		UpAnimation dismiss2 = new UpAnimation(border.getWidth() / 2.0f, border.getHeight() / 2.0f,
				RotateAnimation.ROTATE_DECREASE);
		dismiss.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				// mainBoard.setClickable(false);
				inswitch = true;
				v.setEnabled(false);
				// progress.setEnabled(false);

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				// mainBoard.addView(child);
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				mainBoard.removeAllViews();
				switch (mode) {
				case NewProgress.WEIGHTMODE:
					mainBoard.addView(weightBoard);
					mode = NewProgress.WEIGHTMODE;
					l1.setEnabled(false);
					l2.setEnabled(false);
					// l3.setEnabled(false);
					break;
				case NewProgress.STEPMODE:
					mode = NewProgress.STEPMODE;
					mainBoard.addView(stepBoard);
					l1.setEnabled(true);
					l2.setEnabled(true);
					l3.setEnabled(true);
					break;
				}
				mainBoard.findViewById(R.id.step_icon).setOnClickListener(SimpleHomeFragment.this);
				mainBoard.getChildAt(0)
						.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.simple_appera));
				NewProgress n = (NewProgress) mainBoard.getChildAt(0).findViewById(R.id.progress);
				BG b = (BG) mainBoard.getChildAt(0).findViewById(R.id.small_point);
				inswitch = false;
				// b.setMode(mode);显示体重
				// n.setMode(mode);
				// ObjectAnimator o = ObjectAnimator.ofFloat(n, "progress",
				// pro);
				// o.setDuration(400);
				// o.setInterpolator(new AccelerateDecelerateInterpolator());
				// o.start();
				v.setEnabled(true);
				// progress.setEnabled(false);
				n.setOnNewProgressDown(SimpleHomeFragment.this);
				String s = currentDate;
				if (s.equals(MyDate.getYesterDay())) {
					s = getString(R.string.yesterday);
				} else if (s.equals(Util.getBeforeAfterDate(MyDate.getFileName(), -2).toString())) {
					s = getString(R.string.beforeyesterday);
				} else if (s.equals(MyDate.getFileName())) {
					s = getString(R.string.today_tem);
				}
				TextView t = ((TextView) mainBoard.findViewById(R.id.date));
				t.setTypeface(MilinkApplication.NumberFace);
				t.setText(s);
				getUserData(currentDate, true);
			}
		});

		pageOld.clearAnimation();
		pageOld.startAnimation(dismiss);
		// dismiss2.setInterpolatedTimeListener(new InterpolatedTimeListener() {
		//
		// @Override
		// public void interpolatedTime(float interpolatedTime) {
		// // TODO Auto-generated method stub
		// if (enableRefresh && interpolatedTime > 0.5f) {
		//
		// // switch (mode) {
		// // case NewProgress.STEPMODE:
		// // iv1.setImageResource(R.drawable.steps2x);
		// // iv2.setImageResource(R.drawable.kcal2x);
		// // iv3.setImageResource(R.drawable.sleep2x);
		// // break;
		// // case NewProgress.WEIGHTMODE:
		// // iv1.setImageResource(R.drawable.steps2x);
		// // iv2.setImageResource(R.drawable.waterpercent);
		// // iv3.setImageResource(R.drawable.sleep2x);
		// // break;
		// // }
		//
		// enableRefresh = false;
		// }
		//
		// }
		//
		// @Override
		// public void animationEnd(float interpolatedTime) {
		// // TODO Auto-generated method stub
		// enableRefresh = true;
		// }
		// });
		border.getChildAt(0).clearAnimation();
		border.getChildAt(0).startAnimation(dismiss2);
	}

	boolean enableRefresh = true;
	final int[] pos = new int[2];
	private String currentDate;

	@Override
	public void onProgressDowning(float x) {
		// TODO Auto-generated method stub
		System.out.println("onProgressDowning:" + pos[1]);
		tips.setVisibility(View.VISIBLE);

		if (devicetype == 2 && mode == NewProgress.STEPMODE) {
			tips.setText(R.string.goon_toupstep);
		} else if (devicetype == 5 && mode == NewProgress.STEPMODE) {
			if (copyThat) {
				tips.setText(R.string.goon_torefresh);
			} else {
				tips.setText(R.string.refresh_too);
			}

		} else if (mode == NewProgress.WEIGHTMODE) {
			tips.setText(R.string.goon_toupw);
		} else if (devicetype != 0 && mode == NewProgress.STEPMODE) {
			tips.setText(R.string.goon_torefresh);
		} else if (devicetype == 0) {
			tips.setText(R.string.set_nodevice);
		}
		ObjectAnimator o = ObjectAnimator.ofFloat(mainBoard, "y",
				(pos[1] + x) > NewProgress.getDownDis() ? NewProgress.getDownDis() : (pos[1] + x));
		o.setDuration(0);
		o.start();
	}

	@Override
	public void onProgressHadDown(float x) {
		// TODO Auto-generated method stub
		tips.setVisibility(View.GONE);
		loading.setVisibility(View.VISIBLE);
		System.out.println("onProgressHadDown:" + pos[1]);
		ObjectAnimator o = ObjectAnimator.ofFloat(mainBoard, "y", pos[1]);
		o.setInterpolator(new AccelerateDecelerateInterpolator());
		o.setDuration(300);
		o.start();
		// mainBoard.scrollTo(0, 0);
		db = new Dbutils(share.getInt("UID", -1), getActivity());
		if (mode == NewProgress.WEIGHTMODE) {
			Intent intent = new Intent(BluetoothLeService.ACTION_AIR_CONTROL);
			intent.putExtra("task", 806);
			intent.putExtra("args", 1);
			getActivity().sendBroadcast(intent);

			// int type;
			// Object[] object = db.getUserDeivce();
			// try {
			// if (object == null) {
			// type = 0;
			// } else {
			// type = (Integer) object[4];
			// }
			// } catch (Exception e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// type = 0;
			// }
			// switch (type) {
			// case 0:
			// Intent intent4 = new Intent();
			// intent4.setClass(getActivity(), WeightRuler.class);
			// intent4.putExtra("startEdit1", "");
			// intent4.putExtra("title",
			// getString(R.string.weight_ruler_title3));
			// double wei = db.getWeightRecentlyBasicData();
			// if (wei == 0) {
			// try {
			// wei = Double.valueOf(db.getUserProfile()[6].toString());
			// } catch (Exception e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// }
			// intent4.putExtra("start_w", weight == 0 ? wei + "" : weight
			// + "");
			// intent4.putExtra("pppp", "pp");
			// startActivityForResult(intent4, 4);
			// break;
			// case 1:
			// stopAirScaleService();
			// startAirScaleService();
			// // showSlimDialog();
			// break;
			//
			// default:
			// break;
			// }

		} else {
			if (!currentDate.equals(MyDate.getFileName())) {
				getStepFromNet(currentDate, false);
			} else {
				int dtype = db.getUserDeivceType();
				// /1flame 2soft 4ant 5air
				if (dtype == 1 || dtype == -1 || dtype == 4 || dtype == 2) {
					refreshUserData(dtype);
				} else if (dtype == 5) {
					if (copyThat) {
						LogUtil.e("--refresh--", true);
						Intent intent = new Intent(BluetoothLeService.ACTION_AIR_CONTROL);
						intent.putExtra("task", 5);
						intent.putExtra("args", 1);
						getActivity().sendBroadcast(intent);
						sendRefreshAir();
					} else {
						if (loading != null) {
							loading.setVisibility(View.GONE);
						}
					}
				} else if (dtype == 0 && !share.getBoolean("no_bind_tips", false)) {
					Context con = Util.getThemeContext(getActivity());
					AlertDialog.Builder ab = new AlertDialog.Builder(con);
					ab.setMessage(R.string.target_nobind);
					ab.setTitle(R.string.warning);
					LinearLayout linear = new LinearLayout(getActivity());
					linear.setOrientation(LinearLayout.HORIZONTAL);
					TextView r = new TextView(getActivity());
					r.setText(new StringBuffer().append('\t').append('\t').toString());
					final CheckBox box = new CheckBox(con);
					box.setText(getString(R.string.donotshowme));
					box.setTextColor(Color.BLACK);
					linear.addView(r);
					linear.addView(box);
					ab.setNegativeButton(R.string.set_bindingdevice, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							Intent in = new Intent(getActivity(), SportsDevice.class);
							startActivity(in);
						}
					});
					ab.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							if (box.isChecked()) {
								share.edit().putBoolean("no_bind_tips", true).commit();
							}
						}
					});
					ab.setView(linear);
					ab.create().show();
					if (loading != null) {
						loading.setVisibility(View.GONE);
					}
				} else {
					if (loading != null) {
						loading.setVisibility(View.GONE);
					}
				}
			}

		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == 4 && data != null) {
			loading.setVisibility(View.INVISIBLE);
			String w = data.getStringExtra("startEdit");
			String time = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
			double we = Double.parseDouble(w);
			Object[] user = db.getUserProfile();
			double height = 1.7;
			try {
				height = Double.parseDouble(user[5].toString()) / 100;
			} catch (Exception e) {

			}
			double bmi = we / (height * height);
			if (bmi < 0 || bmi > 120) {
				bmi = 0;
			}
			uploadWeight(0, currentDate, 0, 0, 0, we + "", 0, bmi);
			db.SaveWgBasicData(we, bmi, 1, currentDate, time);

		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/***
	 * 上传体重数据
	 * 
	 * @param type
	 *            0是手动输入 1是体重秤
	 * @param date
	 *            时间
	 * @param fat
	 *            脂肪含量 1时有意义
	 * @param water
	 *            水分
	 * @param muscle
	 *            肌肉
	 * @param weight
	 *            体重
	 */
	public void uploadWeight(final int type, final String date, final double fat, final double water,
			final double muscle, final String weight, final double bone, final double bmi) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("session", share.getString("session_id", null));
				params.put("weight", weight);
				params.put("fat", fat + "");
				params.put("water", water + "");
				params.put("muscle", muscle + "");
				params.put("bone", bone + "");
				params.put("time", date);
				params.put("type", type + "");
				params.put("bmi", bmi + "");

				try {
					String result = HttpUtlis.getRequest(HttpUtlis.BASE_URL + "/data/upLoadWgData", params);
					System.out.println(result);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	@Override
	public void onProgressCancle(float x) {
		// TODO Auto-generated method stub
		tips.setVisibility(View.GONE);
		System.out.println("onProgressHadDown:" + pos[1]);
		ObjectAnimator o = ObjectAnimator.ofFloat(mainBoard, "y", pos[1]);
		o.setDuration((int) (300 * (x / 300f)));
		o.start();
		// mainBoard.scrollTo(0, 0);
	}

	@Override
	public void onProgressAchieve(float x) {
		// TODO Auto-generated method stub
		if (devicetype == 2 && mode == NewProgress.STEPMODE) {
			tips.setText(R.string.release_to_upload);
		} else if (devicetype == 5 && mode == NewProgress.STEPMODE) {
			if (copyThat) {
				tips.setText(R.string.release_to_download);
			} else {
				tips.setText(R.string.refresh_too);
			}
		} else if (mode == NewProgress.WEIGHTMODE) {
			tips.setText(R.string.release_to_upload_weight);
		} else if (devicetype == 0) {
			tips.setText(R.string.set_nodevice);
		} else {
			tips.setText(R.string.release_to_download);
		}
		ObjectAnimator o = ObjectAnimator.ofFloat(mainBoard, "y",
				(pos[1] + x) > NewProgress.getDownDis() ? NewProgress.getDownDis() : (pos[1] + x));
		o.setDuration(0);
		o.start();
	}

	@Override
	public void onProgressClick(float x, View v) {
		// TODO Auto-generated method stub
		// Intent intent = new Intent(getActivity(), DataChartActivity.class);
		//
		// if (mode == NewProgress.STEPMODE) {
		//
		// // 步数 距离 卡路里 体重 BMI
		// intent.putExtra("type", 0);
		// intent.putExtra("nowtime", currentDate);
		// startActivity(intent);
		// } else if (mode == NewProgress.WEIGHTMODE) {
		// // 步数 距离 卡路里 体重 BMI
		// intent.putExtra("type", 3);
		// intent.putExtra("nowtime", currentDate);
		// startActivity(intent);
		// }
		switchMode(v);
	}

	void switchMode(View v) {
		if (!inswitch) {
			tips.setVisibility(View.GONE);
			ObjectAnimator o = ObjectAnimator.ofFloat(mainBoard, "y", pos[1]);
			o.setDuration(Math.abs((int) (300 * (300 / 300f))));
			o.start();
			switch (mode) {
			case NewProgress.STEPMODE:
				border.setVisibility(View.INVISIBLE);
				mode = NewProgress.WEIGHTMODE;
				actionbar.setTitle(getString(R.string.heart));

				break;
			case NewProgress.WEIGHTMODE:
				border.setVisibility(View.VISIBLE);
				stopAirScaleService();
				mode = NewProgress.STEPMODE;
				actionbar.setTitle(getString(R.string.sport));

				break;
			}
			mainBardAnim(v);
		}

	}

	void uploadSteps() {
		int type = db.getUserDeivceType();
		if (type == 0 || type == 2 || type == 5) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {

						Object[] steprs = db.getSpBasicData(MyDate.getFileName());
						String url = String.format(
								"%s/data/upLoadSpData/session/%s/step/%s/calorie/%s/distance/%s/time/%s/type/2",
								HttpUtlis.BASE_URL, share.getString("session_id", "lovefit"), steprs[0].toString(),
								steprs[2].toString(), steprs[1].toString(), MyDate.getFileName());

						String str = HttpUtlis.getRequest(url, null);
						// handler.sendEmptyMessage(1);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}).start();

		} else {
			// handler.sendEmptyMessage(1);
		}
	}

	@Override
	public void onOtherEvent(int d) {
		// TODO Auto-generated method stub
		tips.setVisibility(View.GONE);
		switch (d) {
		case 1:// 向左 如果是今天就显示排名如果是历史就向前滑一天
			if (currentDate.equals(MyDate.getFileName())) {
				// if (share.getInt("ISMEMBER", 0) == 0) {
				// NoRegisterDialog dd = new NoRegisterDialog(getActivity(),
				// R.string.no_register, R.string.no_register_content);
				// dd.show();
				// } else {
				// // 15-01-06
				// uploadSteps();
				// startActivity(new Intent(getActivity(), RankActivity.class));
				// getActivity().overridePendingTransition(R.anim.hold,
				// R.anim.fade);
				// // AllRankActivity a = new AllRankActivity();
				// // FragmentTransaction ft = getFragmentManager()
				// // .beginTransaction();
				// // //
				// //
				// setCustomAnimations()必须位于replace()之前，否则效果不起所中。它的两个参数分别为enter，exit的效果。系统目前提供两个效果，分别为android.R.animator.fade_in和android.R.animator.fade_out
				// // ft.setCustomAnimations(R.anim.hold, R.anim.fade);
				// // ft.replace(R.id.main_frame, a);
				// // ft.commit();
				// }
			} else {
				currentDate = Util.getBeforeAfterDate(currentDate, 1).toString();
				pushData(currentDate, 1);
				count--;
				if (count >= 1) {
					actionbar.setRightVisible(true);
					actionbar.setTopRightIcon(R.drawable.goback);
				} else if (count == 0) {
					actionbar.setRightVisible(false);
					actionbar.setTopRightIcon(R.drawable.friend);
				}
			}
			break;
		case 2:// 向右
			currentDate = Util.getBeforeAfterDate(currentDate, -1).toString();
			pushData(currentDate, -1);
			count++;
			if (count >= 1) {
				actionbar.setRightVisible(true);
				actionbar.setTopRightIcon(R.drawable.goback);
			} else if (count == 0) {
				actionbar.setRightVisible(false);
				actionbar.setTopRightIcon(R.drawable.friend);
			}
			break;
		}
		String s = currentDate;
		if (s.equals(MyDate.getYesterDay())) {
			s = getString(R.string.yesterday);
		} else if (s.equals(Util.getBeforeAfterDate(MyDate.getFileName(), -2).toString())) {
			s = getString(R.string.beforeyesterday);
		} else if (s.equals(MyDate.getFileName())) {
			s = getString(R.string.today_tem);
		}
		TextView t = ((TextView) mainBoard.findViewById(R.id.date));
		t.setTypeface(MilinkApplication.NumberFace);
		t.setText(s);

		ObjectAnimator o = ObjectAnimator.ofFloat(mainBoard, "y", pos[1]);
		o.setDuration(0);
		o.start();
	}

	private void pushData(String day, int anim) {
		// TODO Auto-generated method stub
		((ViewGroup) mView).getChildAt(1).startAnimation(
				AnimationUtils.loadAnimation(getActivity(), anim == -1 ? R.anim.show_left : R.anim.show_right));
		((ViewGroup) mView).findViewById(R.id.t2).startAnimation(
				AnimationUtils.loadAnimation(getActivity(), anim == -1 ? R.anim.show_left : R.anim.show_right));
		((ViewGroup) mView).findViewById(R.id.border).startAnimation(
				AnimationUtils.loadAnimation(getActivity(), anim == -1 ? R.anim.show_left : R.anim.show_right));
		((ViewGroup) mView).findViewById(R.id.point_).startAnimation(AnimationUtils.loadAnimation(getActivity(),
				anim == -1 ? R.anim.simple_point_left : R.anim.simple_point_right));

		loadData(day);

	}

	Timer timer;

	void sendRefreshAir() {
		String address = db.getBTDeivceAddress();
		if (address != null && address.length() == 17) {
			copyThat = false;
			// 获取air数据
			timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					System.out.println("copy that");
					refreshHandler.obtainMessage(2).sendToTarget();
				}
			}, 12 * 1000);
			new Timer().schedule(new TimerTask() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					copyThat = true;
				}
			}, 10 * 1000);
			Intent intent = new Intent(getActivity(), BluetoothLeService.class);
			// intent.putExtra("name", name);
			intent.putExtra("address", address);
			// 1 普通模式 2是固件升级
			intent.putExtra("command", 1);
			intent.putExtra("scanflag", 1);
			LogUtil.e("--startBLE/刷新数据开始服务时间--" + new SimpleDateFormat("yyyy-MM-dd-HH：mm：ss-SSS").format(new Date()));
			getActivity().startService(intent);
			MyLog.d("BLE", "Refresh air data");

		} else {
			loadData(currentDate);
		}
	}

	private void loadData(String s) {

		try {
			uid = share.getInt("UID", -1);
			db = new Dbutils(share.getInt("UID", -1), getActivity());
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			Date cur = format.parse(s);
			String today = format.format(new Date());
			Date t = format.parse(today);
			// 大于七天的数据要注册
			if ((t.getTime() - cur.getTime()) > 7 * 60 * 60 * 24 * 1000 && share.getInt("ISMEMBER", 0) == 0) {
				NoRegisterDialog d = new NoRegisterDialog(getActivity(), R.string.no_register,
						R.string.no_register_content);
				d.show();
			} else if (db.checkSpBasicData(s) == -1) {// 检查数据库有没有数据
				devicetype = db.getUserDeivceType();
				if (devicetype == 1 || devicetype == 4) {// 从网路取数据(3g和ant)
					stepHandler.obtainMessage(3, s).sendToTarget();
				} else if (devicetype == 5) {
					if (!db.hasDownload(s)) {
						stepHandler.obtainMessage(4, s).sendToTarget();
					} else {
						// 从数据库加载
						stepHandler.obtainMessage(0, s).sendToTarget();
					}

				} else { // 蓝牙和手机计步直接从数据库读取
					stepHandler.obtainMessage(0, s).sendToTarget();
				}
			} else {
				// 从数据库加载

				stepHandler.obtainMessage(0, s).sendToTarget();
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			MyLog.e("ondatechage", e.toString());
			e.printStackTrace();
			loading.setVisibility(View.INVISIBLE);
		}
	};

	/***
	 * 0 从数据库获取数据 ,1 网络错误,3 从网络取数据
	 */
	Handler stepHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			try {
				loading.setVisibility(View.INVISIBLE);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			switch (msg.what) {
			case 0:
				getUserData(msg.obj.toString(), true);
				break;
			case 1:
				Toast.makeText(getActivity(), R.string.network_erro, // 网络错误
						Toast.LENGTH_SHORT).show();
				break;
			case 2:
				getUserData(msg.obj.toString(), true);
				break;
			case 3:
				getStepFromNet((String) msg.obj, false);// 从网络取数据
				break;
			case 4:
				getStepFromNet((String) msg.obj, true);// Air从网络取数据
				break;

			}
			super.handleMessage(msg);
		}

	};

	private void getStepFromNet(final String cutTime, final boolean isAir) {

		new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					System.out.println("----" + System.currentTimeMillis());
					Map<String, String> args = new HashMap<String, String>();
					args.put("session", share.getString("session_id", null));

					args.put("end", cutTime);
					args.put("begin", Util.getBeforeAfterDate(cutTime, -7).toString());
					String result1 = HttpUtlis.getRequest(HttpUtlis.BASE_URL + "/data/getstep", args);
					System.out.println("----" + System.currentTimeMillis() + result1);
					if (isAir) {
						// 当天已取过该天
						SQLiteDatabase base = db.beginTransaction();
						try {
							for (int i = 0; i >= -7; i--) {
								db.downloaded(cutTime, Util.getBeforeAfterDate(cutTime, i).toString());
							}
							db.setTransactionSuccessful(base);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} finally {
							db.endTransaction(base);
						}
					}
					JSONObject content = new JSONObject(result1);
					SQLiteDatabase base = db.beginTransaction();
					try {
						if (content.getString("content").length() < 5)
							return;
						JSONArray jsonArray = content.getJSONArray("content");
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject jsobj = jsonArray.getJSONObject(i);
							if (jsobj.getString("time").equals(MyDate.getFileName())) {
								Intent inte = new Intent(Parser.ACTION_STEP_CHANGED);
								inte.putExtra("steps", Long.valueOf(jsobj.getInt("step")));
								getActivity().sendBroadcast(inte);
							}
							db.SaveSpBasicData(jsobj.getInt("step"), jsobj.getInt("distance"), jsobj.getInt("calorie"),
									0, jsobj.getString("time"), new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

						}
						db.setTransactionSuccessful(base);
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						db.endTransaction(base);
						Message msg = new Message();
						msg.what = 0;// 从数据库取数据
						msg.obj = cutTime;
						stepHandler.sendMessage(msg);
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

					// Message msg = new Message();
					// msg.what = 2;// 网络不通
					// stepHandler.sendMessage(msg);

					Message msg = new Message();
					msg.obj = cutTime;
					msg.what = 0;// 从数据库取数据
					stepHandler.sendMessage(msg);

				}
			}
		}).start();
	}

	private float weightPro;
	private float weight;
	private String weightText;
	private float step_aim;
	private float standardW = 55.0f;
	private int step_cur;

	/**
	 * 获得db数据
	 * 
	 * @param day
	 *            日期
	 * @param isAnim
	 *            圆环是否有动画效果
	 */
	private void getUserData(String day, boolean isAnim) {
		if (isPause) {
			return;
		}
		if (loading.getVisibility() == View.VISIBLE && mode == NewProgress.WEIGHTMODE) {
			loading.setVisibility(View.INVISIBLE);
		}
		try {
			db = new Dbutils(share.getInt("UID", -1), getActivity());
			Object[] target = db.getUserTarget();
			Object[] device = db.getUserDeivce();
			Object[] steprs = db.getSpBasicData(day);// 0 step,1 distance,2
			// Object[] weightrs = null;
			Object[] user = null;
			// weightrs = db.getWgBasicData(day);
			// if (weightrs == null) {
			// try {
			// getWeightFromNet(day);
			// } catch (Exception e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// }
			user = db.getUserProfile();
			// weightrs = (weightrs == null) ? new Object[] { 0.0, 0.0, 0, 0, 0,
			// 0 }
			// : weightrs;
			steprs = (steprs == null) ? new Object[] { 0, 0, 0 } : steprs;
			String yes = Util.getBeforeAfterDate(currentDate, -1).toString();
			String score = db.getSleepDetailScore(yes);
			if (score == null && !MyDate.getYesterDay().equals(yes)) {
				try {
					getSleepFromNet(yes);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				standardW = 0.9f * (Float.valueOf(user[5].toString()) - 105);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			standardW = standardW < 0 ? 0 : standardW;
			share.edit().putFloat("standard", standardW).commit();
			// weight = Float.valueOf(weightrs[0].toString());

			float temp = (weight <= 0 ? standardW : weight) - standardW;
			weightPro = Math.abs(temp) / standardW;
			weightText = "等待测量心率";
			if (weight <= 0f) {
				weightPro = 1;

			} else {

				// weightText = String.format(
				// temp < 0 ? getString(R.string.weight_less)
				// : getString(R.string.weight_more), String
				// .format("%1$.1f", Math.abs(temp)));
			}
			String d1 = "0", d2 = "0", d3 = "0";
			step_aim = Float.valueOf(target[3].toString());
			step_aim = (step_aim == 0 ? 10000 : step_aim);
			step_cur = Integer.valueOf(steprs[0].toString());
			float pro = 0f;
			TextView curStep = (TextView) mainBoard.getChildAt(0).findViewById(R.id.step);
			TextView curAim = ((TextView) mainBoard.getChildAt(0).findViewById(R.id.step_aim));
			curStep.setTypeface(MilinkApplication.NumberFace);
			curAim.setTypeface(MilinkApplication.NumberFace);
			switch (mode) {
			case NewProgress.STEPMODE:
				pro = Float.valueOf((steprs[0].toString())) / step_aim;
				curStep.setText(step_cur + "");
				curAim.setText("of " + '\t' + Integer.valueOf(target[3].toString()) + "");
				d1 = String.format("%.1f", Double.valueOf(steprs[1].toString()));
				d2 = String.format("%.1f", (Float.valueOf(steprs[2].toString()) / 1000f)) + "";
				String str = null;
				try {
					str = db.getSleepMsg(yes);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if (str == null || str.length() == 0) {
					d3 = score == null ? "-/-" : score;
					tv3.setTextSize(24);
					(border.findViewById(R.id.sleep_unit)).setVisibility(View.VISIBLE);
				} else {
					d3 = str;
					tv3.setTextSize(14);
					(border.findViewById(R.id.sleep_unit)).setVisibility(View.GONE);
				}

				unit1.setText(getString(R.string.unit_distance));
				unit2.setText(getString(R.string.unit_cal));
				unit3.setText(getString(R.string.sleep));

				break;
			case NewProgress.WEIGHTMODE:
				tv3.setTextSize(24);
				unit3.setVisibility(View.VISIBLE);
				pro = (1 - weightPro) < 0 ? 0 : (1 - weightPro);
				curStep.setText(String.format("%.1f", weight));
				curAim.setText(weightText);
				// weight,bmi,fat,water,muscle,bone
				// d1 = weightrs[2].toString().equals("0") ? "-/-"
				// : String.format("%1$.1f",
				// Float.valueOf(weightrs[2].toString()));
				// d2 = weightrs[3].toString().equals("0") ? "-/-"
				// : String.format("%1$.1f",
				// Float.valueOf(weightrs[3].toString()));
				// d3 = weightrs[4].toString().equals("0") ? "-/-"
				// : String.format("%1$.1f",
				// Float.valueOf(weightrs[4].toString()));
				// unit1.setText(getString(R.string.weight_fat));
				// unit2.setText(getString(R.string.weight_water));
				// unit3.setText(getString(R.string.weight_muscle));
				(border.findViewById(R.id.sleep_unit)).setVisibility(View.GONE);
				break;
			}
			try {
				ObjectAnimator o = ObjectAnimator
						.ofFloat(((NewProgress) mainBoard.getChildAt(0).findViewById(R.id.progress)), "progress", pro);
				o.setInterpolator(new AccelerateDecelerateInterpolator());
				if (isAnim) {
					o.setDuration(700);
				} else {
					o.setDuration(0);
				}

				o.start();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			tv1.setTypeface(MilinkApplication.NumberFace);
			tv2.setTypeface(MilinkApplication.NumberFace);
			tv3.setTypeface(MilinkApplication.NumberFace);
			tv1.setText(d1);
			tv2.setText(d2);
			tv3.setText(d3);
		} catch (Exception e) {
			// showException(e, getActivity());
			e.printStackTrace();
		}
	}

	public static void showException(Exception e, Context context) {
		AlertDialog.Builder ab = new AlertDialog.Builder(context);
		ab.setTitle(e.getClass().getName());
		String msg = "";
		for (StackTraceElement mStackTraceElement : e.getStackTrace()) {
			msg += mStackTraceElement.getClassName() + "\n" + mStackTraceElement.getMethodName() + '\n' + "at line:"
					+ mStackTraceElement.getLineNumber() + '\n' + e.getMessage();
			break;
		}
		ab.setMessage(msg);
		ab.show();
	}

	private void getSleepFromNet(final String string) {
		// TODO Auto-generated method stub
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Map<String, String> args = new HashMap<String, String>();
				args.put("session", share.getString("session_id", null));

				args.put("time", string);
				try {
					String result1 = HttpUtlis.getRequest(HttpUtlis.BASE_URL + "/data/getsleep", args);
					JSONObject js = new JSONObject(result1).getJSONObject("content");
					{
						try {
							String str = js.getString("blob");
							String[] x = str.split("_");
							db.saveSleepDetail(js.getString("time"), js.getString("stime"), js.getString("etime"),
									js.getString("wake"), js.getString("light"), js.getString("deep"),
									js.getString("score"), js.getString("total"), x);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							db.saveSleepDetail(string, "0", "0", "0", "0", "0", "0", "0", new byte[] { 0 });
							// db.saveSleepMsg(string,
							// getString(R.string.nodata));
							e.printStackTrace();
						}
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					db.saveSleepDetail(string, "0", "0", "0", "0", "0", "0", "0", new byte[] { 0 });
					// db.saveSleepMsg(string, getString(R.string.nodata));
					e.printStackTrace();
				} catch (Exception e1) {
					// TODO: handle exception
				}
			}

		}).start();
	}

	private void getWeightFromNet(final String date) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("session", share.getString("session_id", null));
				params.put("end", date);
				params.put("begin", Util.getBeforeAfterDate(date, -7).toString());
				try {
					String result = HttpUtlis.getRequest(HttpUtlis.BASE_URL + "/data/getWeight", params);
					JSONArray arr = new JSONObject(result).getJSONArray("content");
					// 先都存0
					for (int i = 0; i > -8; i--) {
						db.SaveWgBasicDataFull(0, 0, 0, 0, 0, 0, 0, Util.getBeforeAfterDate(date, i).toString(),
								MyDate.getFileName());
					}
					// 在实际根据取得的存储
					for (int i = 0; i < arr.length(); i++) {
						JSONObject obj = arr.getJSONObject(i);
						// {"id":"6","uid":"93181","bmi":"0","weight":"51.1","muscle":"0","water":"0","fat":"0","type":"0","time":"2015-02-25"}
						db.SaveWgBasicDataFull(obj.getDouble("weight"), obj.getDouble("bmi"), obj.getDouble("fat"),
								obj.getDouble("water"), obj.getDouble("muscle"), obj.getDouble("bone"),
								obj.getInt("type"), obj.getString("time"), MyDate.getFileName());
					}
					System.out.println(result);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	String session_id;

	private void refreshUserData(final int dtype) {
		uid = share.getInt("UID", -1);
		db = new Dbutils(uid, getActivity());
		if (uid > 0) {
			if (dtype == 2) {
				// 软记步下拉上传步数
				new Thread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						try {
							Object[] steprs = db.getSpBasicData(MyDate.getFileName());// 0
																						// step,1
																						// distance,2
																						// cal
							if (steprs == null || steprs.length < 3) {
								return;
							}
							String url = String.format(
									"%s/data/upLoadSpData/session/%s/step/%s/calorie/%s/distance/%s/time/%s/type/2",
									HttpUtlis.BASE_URL, share.getString("session_id", "lovefit"), steprs[0].toString(),
									steprs[2].toString(), steprs[1].toString(), MyDate.getFileName());

							String str = HttpUtlis.getRequest(url, null);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				}).start();
			} else if (dtype == 0) {
				if (loading != null) {
					loading.setVisibility(View.GONE);
				}
				return;
			}
			new Thread(new Runnable() {
				@Override
				public void run() {

					try {
						Map<String, String> args = new HashMap<String, String>();
						session_id = share.getString("session_id", "");
						args.put("session", session_id);
						String result = HttpUtlis.getRequest(HttpUtlis.BASE_URL + "/user/getUserData", args);
						JSONObject object = new JSONObject(result);
						if (object.getInt("status") == 0) {
							JSONObject tarCon = new JSONObject(new JSONObject(result).get("target").toString());
							db = new Dbutils(share.getInt("UID", -1), getActivity());
							db.InitTarget(Integer.parseInt(tarCon.getString("type")),
									Double.parseDouble(tarCon.getString("weight_bef")),
									Double.parseDouble(tarCon.getString("weight_end")),
									Integer.parseInt(tarCon.getString("step")),
									Integer.parseInt(tarCon.getString("sleepmode")),
									Integer.parseInt(tarCon.getString("weightmode")),
									Integer.parseInt(tarCon.getString("bmimode")));

							JSONObject Info = new JSONObject(new JSONObject(result).get("content").toString());

							db.UpdateUserInfo(Info.getString("nickname"), Integer.parseInt(Info.getString("sex")),
									Double.parseDouble(Info.getString("height")),
									Double.parseDouble(Info.getString("weight")));

							JSONObject device = new JSONObject(new JSONObject(result).get("device").toString());

							db.InitDeivce(Integer.parseInt(device.getString("devicetype")), 0,
									device.getString("deviceid"), "");

						}

						if (dtype != 2) {
							Map<String, String> args1 = new HashMap<String, String>();
							args1.put("session", share.getString("session_id", null));
							args1.put("end", currentDate);
							args1.put("begin", Util.getBeforeAfterDate(currentDate, -7).toString());
							String result1 = HttpUtlis.getRequest(HttpUtlis.BASE_URL + "/data/getstep", args1);

							JSONObject content = new JSONObject(result1);
							SQLiteDatabase base = db.beginTransaction();
							try {
								if (content.getString("content").length() < 5)
									return;
								JSONArray jsonArray = content.getJSONArray("content");
								for (int i = 0; i < jsonArray.length(); i++) {
									JSONObject jsobj = jsonArray.getJSONObject(i);
									db.SaveSpBasicData(jsobj.getInt("step"), jsobj.getInt("distance"),
											jsobj.getInt("calorie"), 0, jsobj.getString("time"),
											new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

								}
								db.setTransactionSuccessful(base);

							} catch (Exception e) {
								e.printStackTrace();
							} finally {
								db.endTransaction(base);
								refreshHandler.sendEmptyMessage(0);
							}

						} else {
							refreshHandler.sendEmptyMessage(0);
						}

					} catch (Exception e) {
						// TODO Auto-generated catch block
						refreshHandler.sendEmptyMessage(0);
						e.printStackTrace(System.err);
					}

				}
			}).start();

		} else {
			refreshHandler.sendEmptyMessage(0);
		}
	}

	private Handler refreshHandler = new Handler() {
		public void dispatchMessage(Message msg) {
			try {
				loading.setVisibility(View.INVISIBLE);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			switch (msg.what) {
			case 0:

				getUserData(currentDate, true);
				// 刷新完用户数据，是否需要开启服务
				Intent intent = new Intent();
				intent.setAction("milinkStartService");
				intent.putExtra("update", false);
				if (getActivity() != null) {
					getActivity().sendBroadcast(intent);
				}

				break;
			case 1:

				getUserData(currentDate, true);
				break;
			case 2:
				// 12秒内没收到广播
				getUserData(currentDate, true);
				// 可能是由于蓝牙出了问题，做一些事情来补救
				// if (!isPause) {
				// Intent intents = new
				// Intent(BluetoothLeService.ACTION_AIR_CONTROL);
				// intents.putExtra("task", 7);
				// getActivity().sendBroadcast(intents);
				// }
				break;

			// for scale
			case 3:
				getUserData(currentDate, true);
				break;
			case 4:
				if (mode == NewProgress.STEPMODE) {
					switchMode(mView);
				}
				((TextView) mainBoard.getChildAt(0).findViewById(R.id.step)).setText(msg.obj.toString());
				((TextView) mainBoard.getChildAt(0).findViewById(R.id.step_aim)).setText("正在测量");

				NewProgress mm = ((NewProgress) mainBoard.getChildAt(0).findViewById(R.id.progress));
				ObjectAnimator o = ObjectAnimator.ofFloat(mm, "progress", Integer.valueOf(msg.obj.toString()) / 120f);
				o.setInterpolator(new AccelerateDecelerateInterpolator());

				if (mm.getProgress() > 0) {
					o.setDuration(0);
				} else {
					o.setDuration(700);
				}

				o.start();

				break;
			case 5:

				break;
			default:
				break;
			}

		}

	};

	void stopAirScaleService() {
		Intent intentScale = new Intent(getActivity().getApplicationContext(), AirScaleService.class);
		getActivity().stopService(intentScale);
	}

	// 0是不分享 1是朋友圈2是好友
	private IWXAPI wxApi;
	String textTitle = "";
	int shareFlag = 0;
	PopupWindow pop;
	String url = "http://air.lovefit.com/share/";

	// actionbar右侧按钮
	private PopupWindow initPopupWindow() {
		uid = share.getInt("UID", -1);
		wxApi = WXAPIFactory.createWXAPI(getActivity().getApplicationContext(), WXEntryActivity.AppId);
		wxApi.registerApp(WXEntryActivity.AppId);
		// showShare(false, null, false);
		url = "http://air.lovefit.com/share/";
		if (step_cur >= 2000 && step_cur < 4000) {
			textTitle = String.format(getString(R.string.share_2to4k), step_cur + "");
			url += "2000/" + (new Random().nextInt(3) + 1) + ".jpg";
		} else if (step_cur >= 4000 && step_cur < 6000) {
			textTitle = String.format(getString(R.string.share_4to6k), step_cur + "");
			url += "4000/" + (new Random().nextInt(2) + 1) + ".jpg";
		} else if (step_cur >= 6000 && step_cur < 8000) {
			textTitle = String.format(getString(R.string.share_6to8k), step_cur + "");
			url += "6000/" + (new Random().nextInt(2) + 1) + ".jpg";
		} else if (step_cur >= 8000 && step_cur < 10000) {
			textTitle = String.format(getString(R.string.share_8to10k), step_cur + "");
			url += "8000/" + (new Random().nextInt(2) + 1) + ".jpg";
		} else if (step_cur >= 10000 && step_cur < 12000) {
			textTitle = String.format(getString(R.string.share_10to12k), step_cur + "");
			url += "10000/" + (new Random().nextInt(2) + 1) + ".jpg";
		} else if (step_cur >= 12000) {
			textTitle = String.format(getString(R.string.share_over12k), step_cur + "");
			url += "12000/" + (new Random().nextInt(1) + 1) + ".jpg";
		} else {
			textTitle = String.format(getString(R.string.share_0to2k), step_cur + "");
			url += "0/" + (new Random().nextInt(2) + 1) + ".jpg";
		}
		View fa = getActivity().getLayoutInflater().inflate(R.layout.pop_wechat_share, null);
		pop = new PopupWindow(fa, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true);

		final ImageView wechat_f = (ImageView) pop.getContentView().findViewById(R.id.wechat);
		final ImageView wechat = (ImageView) pop.getContentView().findViewById(R.id.wechat_f);
		final ImageView lovefit_share = (ImageView) pop.getContentView().findViewById(R.id.lovefit_share);

		wechat_f.setVisibility(View.GONE);
		lovefit_share.setVisibility(View.GONE);
		lovefit_share.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (step_cur < 100) {
					Toast.makeText(getActivity(), getString(R.string.share_little), Toast.LENGTH_SHORT).show();
				} else {
					shareToLovefit();
				}

				pop.dismiss();
			}
		});
		wechat.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				shareFlag = 2;
				wechatShare(shareFlag);
				pop.dismiss();

			}
		});
		wechat_f.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				shareFlag = 1;
				wechatShare(shareFlag);
				pop.dismiss();

			}
		});
		fa.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				pop.dismiss();
			}
		});
		wechat.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.left_in2));

		wechat_f.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.left_in));
		wechat_f.setVisibility(View.VISIBLE);
		lovefit_share.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.left_in3));

		// lovefit_share.setVisibility(View.VISIBLE);

		pop.setBackgroundDrawable(new BitmapDrawable());
		pop.setOutsideTouchable(true);
		pop.setTouchable(true);

		return pop;
	}

	private void shareToLovefit() {
		AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
		ab.setTitle(R.string.warning);
		ab.setMessage(R.string.share);
		ab.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				shareFlag = 3;
				wechatShare(shareFlag);
				Toast.makeText(getActivity(), R.string.check_later, Toast.LENGTH_LONG).show();
			}
		});
		ab.setNegativeButton(R.string.cancel, null);
		ab.create().show();

	}

	private void wechatShare(int flag) {
		if (flag == 0) {
			return;
		}
		shareScreen = PrintScreen.takeScreenShot(getActivity());
		PrintScreen.savePic(shareScreen,
				Environment.getExternalStorageDirectory().getPath() + HttpUtlis.CACHE_Folder + "/a.png");
		if (flag == 3) {
			String pa = Environment.getExternalStorageDirectory().getPath() + HttpUtlis.CACHE_Folder + "/a.png";
			sendFeed(pa, getActivity(), uid, false);
			return;
		}
		WXWebpageObject webpage = new WXWebpageObject();
		webpage.webpageUrl = "http://a.app.qq.com/o/simple.jsp?pkgname=com.milink.android.air";
		WXMediaMessage msg = new WXMediaMessage(webpage);
		msg.title = "Bandlink";
		msg.description = "Bandlink";
		Matrix ma = new Matrix();
		ma.postScale(0.3f, 0.3f);
		msg.setThumbImage(Bitmap.createBitmap(shareScreen, 0, shareScreen.getHeight() / 7, shareScreen.getWidth(),
				shareScreen.getHeight() / 2, ma, false));
		WXImageObject imgObj = new WXImageObject();
		imgObj.setImagePath(Environment.getExternalStorageDirectory().getPath() + HttpUtlis.CACHE_Folder + "/a.png");
		msg.mediaObject = imgObj;
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = String.valueOf(System.currentTimeMillis());
		req.message = msg;
		req.scene = flag == 2 ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
		wxApi.sendReq(req);

	}

	public static void sendFeed(final String imageUris, final Context context, final int uid, final boolean run) {
		new Thread(new Runnable() {
			public void run() {
				try {
					String urlStr = HttpUtlis.UPLOADIMAGE + uid;
					LinkedList<String> lis = new LinkedList<String>();
					lis.add(imageUris.toString());
					String res = HttpUtlis.postFeedImg(context, urlStr, lis);
					JSONObject json = new JSONObject(res);
					if (json.getInt("status") == 0) {
						JSONArray arr = json.getJSONArray("content");
						if (arr.length() > 0) {
							JSONObject temp = (JSONObject) arr.get(0);
							// temp.getString("attach_id")
							ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
							params.add(new BasicNameValuePair("uid", uid + ""));
							params.add(new BasicNameValuePair("from", "11"));
							params.add(new BasicNameValuePair("clubid", "0"));
							params.add(new BasicNameValuePair("type", "postimage"));
							params.add(new BasicNameValuePair("attach_id", temp.getString("attach_id")));
							params.add(new BasicNameValuePair("witch", "1"));
							// params.add(new BasicNameValuePair("calc", "1"));
							params.add(new BasicNameValuePair("run", run ? "1" : "0"));
							String send = HttpUtlis.SendFeed(HttpUtlis.POSTFEED, params);
						}

					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	private boolean copyThat = true;

	@SuppressLint("NewApi")
	@Override
	public void callFragment(int steps, int dis, int cal, String action) {
		// TODO Auto-generated method stub
		if (action.equals(Parser.ACTION_STEP_CHANGED)) {
			LogUtil.e("--广播收到0803的时间-" + new SimpleDateFormat("yyyy-MM-dd-HH：mm：ss-SSS").format(new Date()));
			if (timer != null) {
				timer.cancel();
			}
			if (!isPause && currentDate.equals(MyDate.getFileName())) {
				loading.setVisibility(View.INVISIBLE);
				refreshHandler.obtainMessage(1).sendToTarget();
			}

		}
		if (Parser.ACTION_STEP0805.equals(action) && mode == NewProgress.STEPMODE) {

			if (currentDate.equals(MyDate.getFileName())) {
				((TextView) mainBoard.getChildAt(0).findViewById(R.id.step)).setText(steps + "");
				tv1.setText(String.format("%.1f", (float) dis / 10f));
				tv2.setText(String.format("%.1f", (float) cal / 1000f));
			}

		}
		if (action.equals("MilinkStep")) {

			if (!isPause && mode == NewProgress.STEPMODE && currentDate.equals(MyDate.getFileName())) {
				if (Build.VERSION.SDK_INT >= 11 && point.getScaleX() == 1f) {
					point.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.simple_bg_expand));
				}
				NewProgress n = (NewProgress) mainBoard.findViewById(R.id.progress);
				n.setProgress(steps / step_aim);
				((TextView) mainBoard.getChildAt(0).findViewById(R.id.step)).setText(steps + "");
				((TextView) mainBoard.getChildAt(0).findViewById(R.id.step_aim))
						.setText("of " + '\t' + (int) step_aim + "");
				tv1.setText(dis / 100 + "");
				tv2.setText(String.format("%1$.1f", cal / 10f) + "");
			}
		}
	}

	@Override
	public void makeOrder(int code) {
		// TODO Auto-generated method stub
		switch (code) {
		case 100:
			tips.setVisibility(View.GONE);
			ObjectAnimator o = ObjectAnimator.ofFloat(mainBoard, "y", pos[1]);
			o.setDuration(0);
			o.start();
			switch (mode) {
			case NewProgress.STEPMODE:
				mode = NewProgress.WEIGHTMODE;
				actionbar.setTitle(getString(R.string.heart));

				break;
			case NewProgress.WEIGHTMODE:
				mode = NewProgress.STEPMODE;
				actionbar.setTitle(getString(R.string.sport));

				break;
			}
			mainBardAnim(mainBoard.findViewById(R.id.progress));
			break;
		case 101:
			pageone.setBackgroundResource(R.drawable.corner_border_red);
			pageone.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.re_alpha));
			break;
		case 102:
			pageone.setBackgroundDrawable(new BitmapDrawable());
			pageone.clearAnimation();
			break;
		case 104:
			mainBoard.findViewById(R.id.step_icon).setBackgroundResource(R.drawable.corner_border_red);
			mainBoard.findViewById(R.id.step_icon)
					.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.re_alpha));
			break;
		case 105:
			mainBoard.findViewById(R.id.step_icon).setBackgroundDrawable(new BitmapDrawable());
			mainBoard.findViewById(R.id.step_icon).clearAnimation();
			break;
		}
	}

}

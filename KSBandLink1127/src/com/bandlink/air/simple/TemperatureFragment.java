package com.bandlink.air.simple;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bandlink.air.MilinkApplication;
import com.bandlink.air.R;
import com.bandlink.air.ble.AirTemperatureService;
import com.bandlink.air.simple.NewProgress.OnNewProgressDown;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.MainInterface;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Temperature;
import com.bandlink.air.util.Util;
import com.bandlink.air.view.MultiDirectionSlidingDrawer;
import com.bandlink.air.view.MultiDirectionSlidingDrawer.OnDrawerOpenListener;
import com.nineoldandroids.animation.ArgbEvaluator;
import com.nineoldandroids.animation.ObjectAnimator;

public class TemperatureFragment extends Fragment implements OnClickListener {
	View mView;

	private MainInterface inf;
	private ActionbarSettings actionBar;
	private NewProgress progress;
	private TextView temperature, time, date;

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		if (activity instanceof MainInterface) {
			inf = (MainInterface) activity;

		}
		super.onAttach(activity);
	}

	void startTempService() {
		dialog = ProgressDialog.show(Util.getThemeContext(getActivity()), null,
				getString(R.string.scanning), true, true);
		Intent intentScale = new Intent(getActivity().getApplicationContext(),
				AirTemperatureService.class);
		getActivity().startService(intentScale);
	}

	void stopTempService() {
		Intent intentScale = new Intent(getActivity().getApplicationContext(),
				AirTemperatureService.class);
		getActivity().stopService(intentScale);
	}

	float hhh = 0f;
	float hight = 0;
	private BroadcastReceiver tempeRe = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, final Intent intent) {
			// TODO Auto-generated method stub
			final String action = intent.getAction();
			if (action.equals(AirTemperatureService.ACTION_BLE_AIRTempr_DATA)) {
				onTest = true;

				getActivity().runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						float bb = intent.getFloatExtra("data", 0f);

						if (hight == 0) {
							hight = bb;
						} else if (bb > hight) {
							hight = bb;
						}
						date.setText(hight + "℃");
						String s = String.format("%.2f", bb);
						temperature.setText(s);
						testing.setVisibility(View.VISIBLE);
						btn.setText(R.string.stop_test);
						int batt = intent.getIntExtra("batt", 0);
						float pp = batt / 100f;
						if (hhh == 0) {
							hhh = batt;
							ObjectAnimator o = ObjectAnimator.ofFloat(progress,
									"progress", pp);
							o.setDuration(300);
							o.start();
						} else if (hhh < batt) {
							hhh = batt;
							progress.setProgress(pp);
						} else {
							progress.setProgress(pp);
						}
						int color = (Integer) new ArgbEvaluator().evaluate(pp,
								0xffFF8800, 0xffffffff);
						// 我们不希望有透明度
						color = (color & 0x00ffffff) + 0xff000000;
						progress.setCircleColor(color);
					}
				});
			} else if (action
					.equals(AirTemperatureService.ACTION_DEVICE_AIRTempr)) {
				// 已链接
				if (intent.getStringExtra("address") != null) {
					if (dialog != null && dialog.isShowing()) {
						dialog.dismiss();
					}
					Toast.makeText(
							getActivity(),
							getString(R.string.notification_air_content_connected),
							Toast.LENGTH_LONG).show();
				} else {
					if (dialog != null && dialog.isShowing()) {
						dialog.dismiss();
					}
					// showDisconnect();
					Toast.makeText(getActivity(), "连接暂时中断。", Toast.LENGTH_LONG)
							.show();
				}

			}
		}
	};
	int[] pos = new int[2];
	View mainBoard;
	ListView listview;
	SimpleAdapter simAdapter;
	ArrayList<HashMap<String, String>> data;
	Dbutils db;
	SharePreUtils spUtil;
	MultiDirectionSlidingDrawer drawer;
	LinearLayout border_alarm;
	private TextView alarmText, normal;
	private ImageView icon;
	private boolean onTest = false;
	private Button btn;
	private ProgressDialog dialog;

	void showDisconnect() {
		AlertDialog.Builder ab = new AlertDialog.Builder(
				Util.getThemeContext(getActivity()));
		ab.setTitle(R.string.warning);
		ab.setMessage(R.string.a_friend);
	}

	ProgressBar testing;
	ImageView handleBtn;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mView = inflater.inflate(R.layout.fragment_temperature, null);
		testing = (ProgressBar) mView.findViewById(R.id.loading);
		spUtil = SharePreUtils.getInstance(getActivity());
		db = new Dbutils(spUtil.getUid(), getActivity());
		progress = (NewProgress) mView.findViewById(R.id.progress);
		progress.setMode(NewProgress.TEMPMODE);
		BG bg = (BG) mView.findViewById(R.id.small_point);
		bg.setMode(NewProgress.TEMPMODE);
		drawer = (MultiDirectionSlidingDrawer) mView.findViewById(R.id.drawer);
		// handleBtn =(ImageView)mView.findViewById(R.id.handle);
		// handleBtn.setBackgroundColor(0x5f93ef);
		border_alarm = (LinearLayout) mView.findViewById(R.id.border_alarm);
		border_alarm.setOnClickListener(this);
		actionBar = new ActionbarSettings(mView, new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// shezhi
				showSet();
			}

		}, inf);
		actionBar.setTitle(R.string.temperature);
		actionBar.setTopRightIcon(R.drawable.ic_setting);
		icon = (ImageView) mView.findViewById(R.id.step_icon);
		icon.setImageResource(R.drawable.temperature);
		temperature = (TextView) mView.findViewById(R.id.step);
		date = (TextView) mView.findViewById(R.id.date);
		time = (TextView) mView.findViewById(R.id.step_aim);
		time.setText(R.string.hight_tempe);
		date.setText("-/-");
		normal = (TextView) mView.findViewById(R.id.normal);
		alarmText = (TextView) mView.findViewById(R.id.alarmtempe);
		alarmText.setTypeface(MilinkApplication.NumberFace);
		normal.setTypeface(MilinkApplication.NumberFace);
		alarmText.setText(spUtil.getAlarmTempe() + "℃");
		temperature.setTypeface(MilinkApplication.NumberFace);
		time.setTypeface(MilinkApplication.NumberFace);
		date.setTypeface(MilinkApplication.NumberFace);
		// date.setText(MyDate.getFileName());
		btn = (Button) mView.findViewById(R.id.btn);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (onTest) {
					AlertDialog.Builder ab = new AlertDialog.Builder(Util
							.getThemeContext(getActivity()));
					ab.setTitle(R.string.warning);
					ab.setMessage(R.string.stop_test);
					ab.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									btn.setText(R.string.start_test);
									stopTempService();
									testing.setVisibility(View.INVISIBLE);
									onTest = false;
								}
							});
					ab.setNegativeButton(R.string.cancel, null);
					ab.create().show();
				} else {
					// testing.setVisibility(View.VISIBLE);
					btn.setText(R.string.start_test);
					startTempService();
				}
			}
		});

		IntentFilter inff = new IntentFilter();
		inff.addAction(AirTemperatureService.ACTION_BLE_AIRTempr_DATA);
		inff.addAction(AirTemperatureService.ACTION_DEVICE_AIRTempr);
		getActivity().registerReceiver(tempeRe, inff);
		mainBoard = (View) mView.findViewById(R.id.main);
		mainBoard.getLocationInWindow(pos);
		listview = (ListView) mView.findViewById(R.id.listview);

		data = new ArrayList<HashMap<String, String>>();
		getData();
		simAdapter = new SimpleAdapter(getActivity(), data,
				R.layout.tempe_item, new String[] { "month", "date", "max" },
				new int[] { R.id.text1, R.id.text2, R.id.text3 });
		listview.setAdapter(simAdapter);
		drawer.setOnDrawerOpenListener(new OnDrawerOpenListener() {

			@Override
			public void onDrawerOpened() {
				// TODO Auto-generated method stub
				getData();
				simAdapter.notifyDataSetChanged();
			}
		});
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				HashMap<String, String> map = (HashMap<String, String>) parent
						.getAdapter().getItem(position);
				Intent i = new Intent(getActivity(), TempeDetailActivity.class);
				i.putExtra("id", map.get("time"));
				startActivity(i);
			}
		});

		progress.setOnNewProgressDown(new OnNewProgressDown() {

			@Override
			public void onProgressHadDown(float x) {
				// TODO Auto-generated method stub
				startTempService();
				ObjectAnimator o = ObjectAnimator.ofFloat(mainBoard, "y",
						pos[1]);
				o.setInterpolator(new AccelerateDecelerateInterpolator());
				o.setDuration(300);
				o.start();
			}

			@Override
			public void onProgressDowning(float x) {
				// TODO Auto-generated method stub
				ObjectAnimator o = ObjectAnimator.ofFloat(
						mainBoard,
						"y",
						(pos[1] + x) > NewProgress.getDownDis() ? NewProgress
								.getDownDis() : (pos[1] + x));
				o.setDuration(0);
				o.start();
			}

			@Override
			public void onProgressClick(float x, View v) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressCancle(float x) {
				// TODO Auto-generated method stub
				ObjectAnimator o = ObjectAnimator.ofFloat(mainBoard, "y",
						pos[1]);
				o.setDuration((int) (300 * (x / 300f)));
				o.start();
			}

			@Override
			public void onProgressAchieve(float x) {
				// TODO Auto-generated method stub
				ObjectAnimator o = ObjectAnimator.ofFloat(
						mainBoard,
						"y",
						(pos[1] + x) > NewProgress.getDownDis() ? NewProgress
								.getDownDis() : (pos[1] + x));
				o.setDuration(0);
				o.start();
			}

			@Override
			public void onOtherEvent(int d) {
				// TODO Auto-generated method stub

			}
		});
	//	LogUtil.e("TTTT", get("2015-06-11")[0]+"-"+get("2015-06-11")[1]+"-"+get("2015-06-11")[2]+"-"+get("2015-06-11")[3]);
		return mView;
	}



	String[] items;

	private void showSet() {
		// TODO Auto-generated method stub
		// items = getResources().getStringArray(R.array.)
		AlertDialog.Builder ab = new AlertDialog.Builder(
				Util.getThemeContext(getActivity()));
		ab.setItems(items, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub

			}
		});
		ab.setNegativeButton(R.string.cancel, null);
		ab.show();
	}

	void getData() {
		SimpleDateFormat sd = new SimpleDateFormat("dd日HH:mm:ss");
		data.clear();
		for (Temperature t : db.getTemperature(null)) {
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(t.getTime());
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("month", (cal.get(Calendar.MONTH) + 1) + "月");
			map.put("date", sd.format(cal.getTime()));
			map.put("max", t.getMaxTempe() + "");
			map.put("id", t.getId() + "");
			map.put("time", t.getTime() + "");
			data.add(map);
		}
	};

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		getActivity().unregisterReceiver(tempeRe);
		super.onDestroy();
	}

	public void stepIconClick(View view) {
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.border_alarm:
			showInputDialog();
			break;
		}
	};

	void showInputDialog() {
		Context context = Util.getThemeContext(getActivity());
		AlertDialog.Builder ab = new AlertDialog.Builder(context);
		final EditText e = new EditText(context);
		e.setInputType(EditorInfo.TYPE_NUMBER_FLAG_DECIMAL);
		e.setText(spUtil.getAlarmTempe() + "");
		ab.setView(e);
		ab.setTitle(R.string.set_alarm_tempe);
		ab.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						String va = e.getEditableText().toString();
						try {
							float a = Float.valueOf(va);
							spUtil.putAlarmTempe(a);
							alarmText.setText(spUtil.getAlarmTempe() + "℃");
							progress.postInvalidate();
						} catch (NumberFormatException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
							Toast.makeText(getActivity(),
									getString(R.string.input_error),
									Toast.LENGTH_SHORT).show();
						}

					}
				});
		ab.setNegativeButton(R.string.cancel, null);
		ab.create().show();
	}

}

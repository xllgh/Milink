package com.bandlink.air.simple;

import java.util.Calendar;

import com.bandlink.air.AirPreferenceActivity;
import com.bandlink.air.R;
import com.bandlink.air.ble.BluetoothLeService;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.MainInterface;
import com.bandlink.air.util.Util;
import com.bandlink.air.view.TimePicker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class AlarmFragment extends Fragment implements OnClickListener, OnCheckedChangeListener {

	private MainInterface mainin;
	private View mView;
	private ActionbarSettings action;
	private int device_status = 0;
	private RelativeLayout toogle_novoice1_border, // 无声闹铃开关1
			toogle_novoice2_border;// 无声闹铃开关1
	private ToggleButton toogle_novoice1, // 无声闹铃开关1
			toogle_novoice2;// 无声闹铃开关1
	private TextView set_novoice_time1, // 无声闹铃1时间设定
			set_novoice_time2; // 无声闹铃2时间设定;
	RelativeLayout set_novoice_time1_week_border, set_novoice_time2_week_border, set_novoice_time1_border,
			set_novoice_time2_border;
	TextView set_novoice_time1_week, set_novoice_time2_week;
	private LinearLayout linear_voice1, linear_voice2;
	private BroadcastReceiver airReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			// TODO Auto-generated method stub

			final String action = intent.getAction();

			if (action.equals(BluetoothLeService.ACTION_AIR_RSSI_STATUS)) {
				device_status = intent.getIntExtra("status", 0);
			}

			if (action.equals(BluetoothLeService.ACTION_BLE_CONFIG_READ_RESPONSE)) {
				int write = intent.getIntExtra("write", -2);
				if (write != -2) {
					if (write == 7) {
						if (dialog != null && dialog.isShowing()) {
							Toast.makeText(getActivity(), R.string.finish, Toast.LENGTH_LONG).show();
							dialog.dismiss();
						}
					}
				}
				// Toast.makeText(AirPreferenceActivity.this, "on packet good!",
				// Toast.LENGTH_SHORT).show();
				final int type = intent.getIntExtra("type", -1);
				final byte[] ba;
				switch (type) {

				// alarm
				case 7:
					getActivity().runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							if (dialog != null && dialog.isShowing()) {
								dialog.dismiss();
								dialog = null;
							}
						}
					});
					ba = intent.getByteArrayExtra("value");
					try {

						if (ba[0] == 1) {
							toogle_novoice1.setChecked(true);
						} else {
							toogle_novoice1.setChecked(false);
						}
						set_novoice_time1.setText(
								AirPreferenceActivity.turnNum(ba[1]) + ":" + AirPreferenceActivity.turnNum(ba[2]));

						if (ba[3] == 1) {
							toogle_novoice2.setChecked(true);
						} else {
							toogle_novoice2.setChecked(false);
						}
						set_novoice_time2.setText(
								AirPreferenceActivity.turnNum(ba[4]) + ":" + AirPreferenceActivity.turnNum(ba[5]));

					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				default:
					break;
				}

			}
		}

	};

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		mainin = (MainInterface) activity;
		super.onAttach(activity);
	}

	ProgressDialog dialog;

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		dialog = Util.initProgressDialog(getActivity(), true, getString(R.string.data_wait), null);
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothLeService.ACTION_AIR_RSSI_STATUS);
		filter.addAction(BluetoothLeService.ACTION_BLE_CONFIG_READ_RESPONSE);
		getActivity().registerReceiver(airReceiver, filter);
		airReadconfig();
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
					dialog = null;
					Toast.makeText(getActivity(), getString(R.string.deleteerror), Toast.LENGTH_SHORT).show();
				}

			}

		}, 10 * 1000);
		super.onResume();
	}

	void airReadconfig() {
		Intent intent = new Intent();
		intent.setAction(BluetoothLeService.ACTION_BLE_CONFIG_CMD);
		// 只读闹钟
		intent.putExtra("type", 90);
		intent.putExtra("param", new int[] { 0 });
		getActivity().sendBroadcast(intent);
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		getActivity().unregisterReceiver(airReceiver);
		super.onStop();
	}

	private SharedPreferences sharePre;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mView = inflater.inflate(R.layout.fragment_alarm, null);
		sharePre = getActivity().getSharedPreferences("airqst", Context.MODE_MULTI_PROCESS);
		set_novoice_time1_week_border = (RelativeLayout) mView.findViewById(R.id.set_novoice_time1_week_border);
		set_novoice_time1 = (TextView) mView.findViewById(R.id.set_novoice_time1);
		set_novoice_time2 = (TextView) mView.findViewById(R.id.set_novoice_time2);
		set_novoice_time1_week = (TextView) mView.findViewById(R.id.set_novoice_time1_week);
		set_novoice_time2_week_border = (RelativeLayout) mView.findViewById(R.id.set_novoice_time2_week_border);
		set_novoice_time2_week = (TextView) mView.findViewById(R.id.set_novoice_time2_week);
		linear_voice1 = (LinearLayout) mView.findViewById(R.id.linear_voice1);
		linear_voice2 = (LinearLayout) mView.findViewById(R.id.linear_voice2);
		set_novoice_time1_week.setText(getWeek(1));
		set_novoice_time1_week_border.setOnClickListener(this);
		set_novoice_time1_week.setOnClickListener(this);
		set_novoice_time2_week.setText(getWeek(2));
		set_novoice_time2_week_border.setOnClickListener(this);
		set_novoice_time2_week.setOnClickListener(this);
		toogle_novoice1_border = (RelativeLayout) mView.findViewById(R.id.toogle_novoice1_border);
		toogle_novoice2_border = (RelativeLayout) mView.findViewById(R.id.toogle_novoice2_border);
		set_novoice_time1_border = (RelativeLayout) mView.findViewById(R.id.set_novoice_time1_border);
		set_novoice_time2_border = (RelativeLayout) mView.findViewById(R.id.set_novoice_time2_border);
		set_novoice_time1_border.setOnClickListener(this);
		set_novoice_time2_border.setOnClickListener(this);
		toogle_novoice1_border.setOnClickListener(this);
		toogle_novoice2_border.setOnClickListener(this);
		toogle_novoice1 = (ToggleButton) mView.findViewById(R.id.toogle_novoice1);
		toogle_novoice2 = (ToggleButton) mView.findViewById(R.id.toogle_novoice2);
		toogle_novoice1.setOnCheckedChangeListener(this);
		toogle_novoice2.setOnCheckedChangeListener(this);
		// 闹钟1
		boolean bool_alarm1 = sharePre.getBoolean("set_alarm1", false);
		if (bool_alarm1) {
			toogle_novoice1.setChecked(true);
		} else {
			toogle_novoice1.setChecked(false);
		}
		for (int i = 1; i < linear_voice1.getChildCount(); i++) {
			changeEnable(linear_voice1.getChildAt(i), bool_alarm1);
		}
		// 闹钟2
		boolean bool_alarm2 = sharePre.getBoolean("set_alarm2", false);
		if (bool_alarm2) {
			toogle_novoice2.setChecked(true);
		} else {
			toogle_novoice2.setChecked(false);
		}
		for (int i = 1; i < linear_voice2.getChildCount(); i++) {
			changeEnable(linear_voice2.getChildAt(i), bool_alarm2);
		}
		action = new ActionbarSettings(mView, mainin);
		action.setTitle(getResources().getStringArray(R.array.menu)[4]);
		action.setRightVisible(false);
		Button save = (Button) mView.findViewById(R.id.submit);
		save.setOnClickListener(this);
		return mView;
	}

	void changeEnable(View view, boolean isChecked) {
		if (view instanceof ViewGroup) {
			// 由于添加时间段的时间监听在RelativeLayout上，所以在关闭功能时需要劫持响应
			if (isChecked) {
				view.setEnabled(true);
			} else {
				view.setEnabled(false);
			}
			// 如果子元素还是容器则递归以下
			ViewGroup v = (ViewGroup) view;

			for (int i = 0; i < v.getChildCount(); i++) {
				changeEnable(v.getChildAt(i), isChecked);
			}
		} else if (view instanceof TextView) {// 如果是TextView则改变字体颜色，取消可操作，下同理
			if (!isChecked) {
				((TextView) view).setTextColor(getResources().getColor(R.color.white_0_8));
				((TextView) view).setEnabled(false);
			} else {
				((TextView) view).setTextColor(getResources().getColor(R.color.white));
				((TextView) view).setEnabled(true);
			}

		} else if (view instanceof CheckBox) {
			if (!isChecked) {
				((CheckBox) view).setEnabled(false);
			} else {
				((CheckBox) view).setEnabled(true);
			}

		} else if (view instanceof Button) {
			if (!isChecked) {
				((Button) view).setClickable(false);
			} else {
				((Button) view).setClickable(true);
			}
		}

	}

	private String getWeek(int index) {
		String str = sharePre.getString("week" + index, "0-1-1-1-1-1-0");
		String[] arr = str.split("-");
		String[] w = getResources().getStringArray(R.array.week);
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].equals("1")) {
				sb.append(w[i]);
				sb.append('\t');
			}
		}
		if (str.equals("0-1-1-1-1-1-0")) {
			sb = new StringBuffer(getString(R.string.workday));
		} else if (str.equals("1-1-1-1-1-1-1")) {
			sb = new StringBuffer(getString(R.string.everyday));
		}
		return sb.toString().length() == 0 ? getString(R.string.none) : sb.toString();
	}

	boolean[] bool;

	private void showWeekSelector(final int index) {
		try {
			String data = sharePre.getString("week" + index, "0-1-1-1-1-1-0");
			String[] week = data.split("-");
			bool = new boolean[week.length];
			for (int i = 0; i < week.length; i++) {
				bool[i] = week[i].equals("1") ? true : false;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			bool = new boolean[] { false, true, true, true, true, true, false };
			e.printStackTrace();
		}
		AlertDialog.Builder ab = new AlertDialog.Builder(Util.getThemeContext(getActivity()));
		ab.setTitle(R.string.week);
		ab.setMultiChoiceItems(getResources().getStringArray(R.array.week), bool, new OnMultiChoiceClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				// TODO Auto-generated method stub
				bool[which] = isChecked;
			}
		});
		ab.setNegativeButton(R.string.can, null);
		ab.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				StringBuffer sb = new StringBuffer();
				for (int j = 0; j < bool.length; j++) {
					sb.append(bool[j] ? "1" : "0");
					if (j != bool.length - 1) {
						sb.append("-");
					}
				}
				sharePre.edit().putString("week" + index, sb.toString()).commit();
				if (index == 1) {
					set_novoice_time1_week.setText(getWeek(index));
				} else {
					set_novoice_time2_week.setText(getWeek(index));
				}
			}
		});
		ab.create().show();

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.submit:
			if (device_status == 1) {
				commitAlarm();
				
			} else {
				Toast.makeText(getActivity(), R.string.device_off, Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.set_novoice_time1_week_border:
		case R.id.set_novoice_time1_week:
			showWeekSelector(1);
			break;
		case R.id.set_novoice_time2_week_border:
		case R.id.set_novoice_time2_week:
			showWeekSelector(2);
			break;
		case R.id.set_novoice_time1_border:
			String[] time = new String[2];

			int hour = 8;
			int min = 30;
			try {
				time = set_novoice_time1.getText().toString().split(":");
				hour = Integer.parseInt(time[0]);
				min = Integer.parseInt(time[1]);
			} catch (Exception e) {
				e.printStackTrace();
			}
			View vie = LayoutInflater.from(new ContextThemeWrapper(getActivity(), R.style.AiTheme_Light))
					.inflate(R.layout.set_timepicker, null);
			vie.findViewById(R.id.container).setOnClickListener(null);
			final TimePicker t = (TimePicker) vie.findViewById(R.id.time_start);
			Calendar calendar = Calendar.getInstance();
			t.setIs24Hour(true);
			calendar.set(Calendar.HOUR_OF_DAY, hour);
			calendar.set(Calendar.MINUTE, min);
			t.setCalendar(calendar);
			AlertDialog.Builder aDailog = new AlertDialog.Builder(Util.getThemeContext(getActivity()));
			aDailog.setTitle(R.string.select_time);
			aDailog.setView(vie);
			aDailog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					String s = AirPreferenceActivity.turnNum(t.getHourOfDay()) + ":"
							+ AirPreferenceActivity.turnNum(t.getMinute());
					set_novoice_time1.setText(s);
					sharePre.edit().putString("set_alarm1_time", s).commit();
					sharePre.edit().putInt("set_alarm1_hour", t.getHourOfDay()).commit();
					sharePre.edit().putInt("set_alarm1_minute", t.getMinute()).commit();
				}
			});

			aDailog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.cancel();
				}
			});
			aDailog.show();
			break;
		case R.id.set_novoice_time2_border:
			String[] time2 = new String[2];

			int hour2 = 8;
			int min2 = 30;
			try {
				time2 = set_novoice_time2.getText().toString().split(":");
				hour2 = Integer.parseInt(time2[0]);
				min2 = Integer.parseInt(time2[1]);
			} catch (Exception e) {
				e.printStackTrace();
			}

			View vie2 = LayoutInflater.from(new ContextThemeWrapper(getActivity(), R.style.AiTheme_Light))
					.inflate(R.layout.set_timepicker, null);
			vie2.findViewById(R.id.container).setOnClickListener(null);
			final TimePicker t2 = (TimePicker) vie2.findViewById(R.id.time_start);
			t2.setIs24Hour(true);
			Calendar calendar2 = Calendar.getInstance();

			calendar2.set(Calendar.HOUR_OF_DAY, hour2);
			calendar2.set(Calendar.MINUTE, min2);
			t2.setCalendar(calendar2);

			AlertDialog.Builder aDailog1 = new AlertDialog.Builder(Util.getThemeContext(getActivity()));
			aDailog1.setTitle(R.string.select_time);
			aDailog1.setView(vie2);
			aDailog1.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					String s = AirPreferenceActivity.turnNum(t2.getHourOfDay()) + ":"
							+ AirPreferenceActivity.turnNum(t2.getMinute());
					set_novoice_time2.setText(s);
					sharePre.edit().putString("set_alarm2_time", s).commit();
					sharePre.edit().putInt("set_alarm2_hour", t2.getHourOfDay()).commit();
					sharePre.edit().putInt("set_alarm2_minute", t2.getMinute()).commit();
				}
			});

			aDailog1.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.cancel();
				}
			});
			aDailog1.show();

			break;
		case R.id.toogle_novoice1_border:
			CompoundButton toggle = AirPreferenceActivity.getToggleButtonFromRelativeLayout((ViewGroup) v);
			if (toggle != null)
				onCheckedChanged(toggle, !toggle.isChecked());
			break;
		case R.id.toogle_novoice2_border:
			CompoundButton toggle1 = AirPreferenceActivity.getToggleButtonFromRelativeLayout((ViewGroup) v);
			if (toggle1 != null)
				onCheckedChanged(toggle1, !toggle1.isChecked());
			break;
		default:
			break;
		}
	}

	/**
	 * 检测无声闹铃设置状态，生成长度为6的byte数组{0[1],x,x,0[1],x,x} 0、3位表示闹钟开启状态后面两位时、分
	 */
	private void commitAlarm() {
		dialog = Util.initProgressDialog(getActivity(), true, getString(R.string.data_wait), null);
		// 闹钟是否开启
		int switch1 = 0, switch2 = 0;
		if (toogle_novoice1.isChecked()) {
			switch1 = 1;
		}
		if (toogle_novoice2.isChecked()) {
			switch2 = 1;
		}
		int[] args = new int[6];
		try {
			String[] str = set_novoice_time1.getText().toString().split(":");
			args[0] = switch1;
			args[1] = Integer.parseInt(str[0]);
			args[2] = Integer.parseInt(str[1]);

		} catch (Exception e) {
			args[3] = 0;
			args[4] = 8;
			args[5] = 30;
			e.printStackTrace();
		}
		try {
			String[] str2 = set_novoice_time2.getText().toString().split(":");
			args[3] = switch2;
			args[4] = Integer.parseInt(str2[0]);
			args[5] = Integer.parseInt(str2[1]);

		} catch (Exception e) {
			args[3] = 0;
			args[4] = 8;
			args[5] = 30;
			e.printStackTrace();
		}
		airSetAlarm(args);
	}

	void airSetAlarm(int[] args) {
		Intent intent = new Intent();
		intent.setAction(BluetoothLeService.ACTION_BLE_CONFIG_CMD);
		intent.putExtra("type", 7);
		intent.putExtra("param", args);
		intent.putExtra("w1", sharePre.getString("week1", "0-1-1-1-1-1-0"));
		intent.putExtra("w2", sharePre.getString("week2", "0-1-1-1-1-1-0"));
		getActivity().sendBroadcast(intent);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub

		switch (buttonView.getId()) {

		case R.id.toogle_novoice1:
			buttonView.setChecked(isChecked);
			for (int i = 1; i < linear_voice1.getChildCount(); i++) {
				changeEnable(linear_voice1.getChildAt(i), isChecked);
			}
			sharePre.edit().putBoolean("set_alarm1", isChecked).commit();
			break;
		case R.id.toogle_novoice2:
			buttonView.setChecked(isChecked);
			for (int i = 1; i < linear_voice2.getChildCount(); i++) {
				changeEnable(linear_voice2.getChildAt(i), isChecked);
			}
			sharePre.edit().putBoolean("set_alarm2", isChecked).commit();
			break;
		}
	}
}

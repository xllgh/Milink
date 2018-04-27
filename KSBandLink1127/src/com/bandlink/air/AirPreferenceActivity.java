package com.bandlink.air;

import java.io.File;
import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bandlink.air.ble.BluetoothLeService;
import com.bandlink.air.bluetooth.protocol.Parser;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.FileDownUtils;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.NotificationUtils;
import com.bandlink.air.util.Util;
import com.bandlink.air.util.FileDownUtils.OnHexDownloadListener;
import com.bandlink.air.view.BadgeView;
import com.bandlink.air.view.NewSeekBar;
import com.bandlink.air.view.NumberPicker;
import com.bandlink.air.view.TimePicker;
import com.bandlink.air.view.TimePicker.OnTimeChanged;
import com.umeng.analytics.MobclickAgent;

public class AirPreferenceActivity extends LovefitActivity
		implements OnCheckedChangeListener, OnClickListener, OnSharedPreferenceChangeListener {

	PopupWindow p;
	// 用于选择铃声后作相应的判断标记
	private static final int REQUEST_CODE_PICK_RINGTONE = 1;
	private static final int RESULTCODE_CHOOSE_RING = 10;
	private static final int RESULT_COCE_FILE_DOWN = 2;

	// 设备连接状态
	private TextView tv_device_status;
	private TextView tv_device_rssi;
	private TextView tv_device_battery;
	private RelativeLayout layout_device_name;// 设备名称整栏
	private RelativeLayout layout_device_status;// 设备名称整栏
	private TextView tv_device_name;// 设备名称
	private int device_status = 0;
	// 即时RSSI
	private int device_rssi_c = 0;
	// 平均RSSI
	private int device_rssi_a = 0;
	private int device_battery = 0;
	RelativeLayout set_checkbox_callphone_border;
	// 保存铃声的Uri的字符串形式
	private String mRingtoneUri = null;
	private ToggleButton toogle_novoice1, // 无声闹铃开关1
			toogle_novoice2, // 无声闹铃开关1
			toogle_remind, // 智能提醒开关
			toogle_antilost, // 防丢开关
			toogle_sleep, // 睡眠监测开关
			tog_app_notification; // App通知开关
	private SharedPreferences sharePre;
	private LinearLayout linear_voice1, linear_voice2, linear_remind, linear_antilost, linear_sleep, linear_lost_time,
			linear_airnew, linear_setting, linear_callphone, linear_delay, linear_app_notification_border;

	private Button btn_addtime, btn_update, btn_off, set_checkupdate;
	RelativeLayout air_new;
	private CheckBox callphone, wechatremind, // 呼叫手机
			callremind, // 来电提醒
			msgremind;// 短信提醒

	private TextView set_sleep_time, // 睡眠监测时间设定
			set_safe_distance, // 安全距离设定
			set_warning_ring, // 防丢报警铃声设定
			set_novoice_time1, // 无声闹铃1时间设定
			set_novoice_time2, // 无声闹铃2时间设定
			cur_version, // 当前软件版本
			hh, htext, line, ishide, switch_debug, morrow, sys_set; // 检测更新

	private String set_warning_ring_path;// 报警铃声的路径/url

	private String currentVision, newVision, newVersionText;
	private ProgressDialog progressDialog;
	private ProgressDialog mProgressDialog;
	private final int MAX_PROGRESS = 100;
	private int mProgress;
	private Handler mProgressHandler;
	private String filenameString;

	private final int FindNewVersion = 1;
	private final int FindFinish = 2;
	private final int FindError = 3;
	private RelativeLayout toogle_novoice1_border, // 无声闹铃开关1
			toogle_novoice2_border, // 无声闹铃开关1
			toogle_remind_border, // 智能提醒开关
			toogle_antilost_border, // 防丢开关
			toogle_sleep_border; // 睡眠监测开关
	private Context con;
	private TextView update_anyway;
	private BroadcastReceiver airReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			// TODO Auto-generated method stub

			final String action = intent.getAction();

			if (action.equals(BluetoothLeService.ACTION_AIR_RSSI_STATUS)) {
				device_status = intent.getIntExtra("status", 0);
				device_rssi_c = intent.getIntExtra("rssi_c", 0);
				device_rssi_a = intent.getIntExtra("rssi_a", 0);
				device_battery = intent.getIntExtra("device_battery", 0);
				if (tv_device_status != null) {
					if (device_status == 0) {
						tv_device_status.setText(getString(R.string.device_status_off));
						air_new.setClickable(false);
						ishide.setTextColor(getResources().getColor(R.color.white_0_8));
					} else {
						air_new.setClickable(true);
						ishide.setTextColor(getResources().getColor(R.color.white));
						tv_device_status.setText(getString(R.string.device_status_on));
					}
				}

				if (tv_device_rssi != null) {
					if (device_status == 0) {
						tv_device_rssi.setText("-/-");
					} else {
						tv_device_rssi.setText(device_rssi_c + "/" + device_rssi_a);
					}
				}

				if (tv_device_battery != null) {
					if (device_status == 0) {
						tv_device_battery.setText("--%");

					} else {
						tv_device_battery.setText(device_battery + "%");
					}
				}
			}

			if (action.equals(BluetoothLeService.ACTION_BLE_CONFIG_READ_RESPONSE)) {
				bNotRead = false;
				// Toast.makeText(AirPreferenceActivity.this, "on packet good!",
				// Toast.LENGTH_SHORT).show();
				int write = intent.getIntExtra("write", -2);
				if (write != -2) {
					if (write == 13 || write == 16) {
						if (dialog != null && dialog.isShowing()) {
							dialog.dismiss();
							Toast.makeText(AirPreferenceActivity.this, getString(R.string.finish), Toast.LENGTH_SHORT)
									.show();
						}
					}
				}
				final int type = intent.getIntExtra("type", -1);
				final byte[] ba;
				switch (type) {
				// vibrate_en
				case 5:
					ba = intent.getByteArrayExtra("value");
					boolean vibrate_en = ((ba[0] & 0xff) == 0x01);

					break;

				// alarm
				case 7:
					ba = intent.getByteArrayExtra("value");
					try {

						if (ba[0] == 1) {
							toogle_novoice1.setChecked(true);
						} else {
							toogle_novoice1.setChecked(false);
						}
						set_novoice_time1.setText(turnNum(ba[1]) + ":" + turnNum(ba[2]));

						if (ba[3] == 1) {
							toogle_novoice2.setChecked(true);
						} else {
							toogle_novoice2.setChecked(false);
						}
						set_novoice_time2.setText(turnNum(ba[4]) + ":" + turnNum(ba[5]));

					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				// name
				case 20:
					final String name = intent.getStringExtra("value");
					if (tv_device_name != null) {
						tv_device_name.setText(name);
					}
					break;
				// soft_version
				case 22:
					final String ver = intent.getStringExtra("value");
					if (tv_device_name != null) {
						cur_version.setText(ver);
					}
					break;

				default:
					break;
				}

			}
		}

	};
	private OnClickListener lsr = new OnClickListener() {

		@Override
		public void onClick(View v) {
			AlertDialog.Builder ab = new AlertDialog.Builder(con);
			ab.setMessage(R.string.abandon);
			ab.setTitle(R.string.warning);
			ab.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					AirPreferenceActivity.this.finish();
				}
			});
			ab.setNegativeButton(R.string.can, null);
			if (settingChanged) {
				ab.create().show();
			} else {
				AirPreferenceActivity.this.finish();
			}
		}
	};
	boolean bNotRead = true;
	private CheckBox ble_open, // 设备振动
			auto_reconnect;// 自动重连
	private int mode;
	private String version;
	RelativeLayout toogle_liftwrist_border, airlanguge_border;
	ToggleButton toogle_liftwrist;
	TextView airlanguge;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setTheme(R.style.holo_notitle_notitlecontent);
		setContentView(R.layout.setting_device_preference);
		sharePre = getSharedPreferences("air", MODE_MULTI_PROCESS);
		sharePre.registerOnSharedPreferenceChangeListener(this);
		// 语言抬腕
		toogle_liftwrist_border = (RelativeLayout) findViewById(R.id.toogle_liftwrist_border);
		airlanguge_border = (RelativeLayout) findViewById(R.id.airlanguge_border);
		toogle_liftwrist = (ToggleButton) findViewById(R.id.toogle_liftwrist);
		airlanguge = (TextView) findViewById(R.id.airlanguge);
		// toogle_liftwrist_border.setOnClickListener(this);
		airlanguge_border.setOnClickListener(this);

		// 初始化语言
		if (sharePre.getInt("lan", 1) == 0) {
			airlanguge.setText(R.string.language_en);
		} else if (sharePre.getInt("lan", 1) == 1) {
			airlanguge.setText(R.string.language_zh);
		} else {
			// 2
			airlanguge.setText(R.string.language_auto);
		}
		// 初始化抬腕
		if (sharePre.getInt("liftwrist", 1) != 1) {
			toogle_liftwrist.setChecked(false);
		} else {
			toogle_liftwrist.setChecked(true);
		}
		toogle_liftwrist.setOnCheckedChangeListener(this);
		// end
		con = Util.getThemeContext(this);
		tv_device_status = (TextView) findViewById(R.id.tv_device_status);
		tv_device_rssi = (TextView) findViewById(R.id.tv_device_rssi);
		tv_device_battery = (TextView) findViewById(R.id.tv_device_battery);
		auto_reconnect = (CheckBox) findViewById(R.id.set_checkbox_auto_connect);
		ble_open = (CheckBox) findViewById(R.id.set_checkbox_ble_open);
		auto_reconnect.setOnCheckedChangeListener(this);
		ble_open.setOnCheckedChangeListener(this);
		set_checkbox_callphone_border = (RelativeLayout) findViewById(R.id.set_checkbox_callphone_border);
		set_checkbox_callphone_border.setOnClickListener(this);

		if (sharePre.getBoolean("air_auto_connect", true)) {
			auto_reconnect.setChecked(true);
		} else {
			auto_reconnect.setChecked(false);
		}
		ActionbarSettings actionbar = new ActionbarSettings(this, lsr, new OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder ab = new AlertDialog.Builder(con);
				ab.setTitle(R.string.warning);
				ab.setMessage(getString(R.string.save_mode) + "?");
				ab.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						// 提交无声闹
						// commitAlarm();
						sharePre.edit().putInt("airmode", mode).commit();
						switch (mode) {
						case 0:
							commitMode1();
							switchMode(0);
							break;
						case 1:
							commitMode2();
							Editor editor = sharePre.edit();
							editor.putBoolean("set_isantilost", false).commit();
							break;
						case 2:
							commitMode3();
							break;
						}
						cancleNotification();
						AirPreferenceActivity.this.finish();
					}
				});
				ab.setPositiveButton(R.string.cancel, null);
				ab.show();
			}
		});
		actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
		actionbar.setTitle(R.string.setting_preference);
		actionbar.setTopRightIcon(R.drawable.complete);
		initViews();
		mode = getIntent().getIntExtra("mode", 0);
		version = getIntent().getStringExtra("version");
		findViewById(R.id.go_auth).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
				startActivity(intent);
			}
		});
		findViewById(R.id.go_manager).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(AirPreferenceActivity.this, com.bandlink.air.NotificationManager.class);
				startActivity(intent);
			}
		});

		switch (mode) {
		case 0:
			findViewById(R.id.air3).setVisibility(View.GONE);
			linear_antilost.setVisibility(View.GONE);
			linear_airnew.setVisibility(View.GONE);
			linear_remind.setVisibility(View.GONE);
			sys_set.setVisibility(View.GONE);
			linear_setting.setVisibility(View.GONE);
			linear_delay.setVisibility(View.GONE);
			linear_callphone.setVisibility(View.GONE);
			break;
		case 1:
			if (true) {
				findViewById(R.id.air3).setVisibility(View.VISIBLE);
			} /*
				 * else { findViewById(R.id.air3).setVisibility(View.GONE); if
				 * (version != null && version.contains("AirII")) {
				 * set_checkbox_remindmm_border.setVisibility(View.VISIBLE); }
				 * else { set_checkbox_remindmm_border.setVisibility(View.GONE);
				 * } }
				 */

			linear_delay.setVisibility(View.GONE);
			linear_antilost.setVisibility(View.GONE);
			linear_airnew.setVisibility(View.GONE);
			// linear_callphone.setVisibility(View.GONE);
			// linear_remind.setVisibility(View.GONE);
			// sys_set.setVisibility(View.GONE);
			// linear_setting.setVisibility(View.GONE);
			break;
		case 2:
			if (true) {
				findViewById(R.id.air3).setVisibility(View.VISIBLE);
			}
			break;
		}
		mProgressHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (mProgress >= MAX_PROGRESS) {
					mProgressDialog.dismiss();
				} else {
					mProgress++;
					mProgressDialog.incrementProgressBy(1);
					mProgressHandler.sendEmptyMessageDelayed(0, 100);
				}
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothLeService.ACTION_AIR_RSSI_STATUS);
		filter.addAction(BluetoothLeService.ACTION_BLE_CONFIG_READ_RESPONSE);
		registerReceiver(airReceiver, filter);

		bNotRead = true;
		// airReadconfigTwice();
		airRssiReportSwitch(1);
	}

	void switchMode(int m) {
		boolean b = m == 2 ? true : false;
		Editor editor = sharePre.edit();
		editor.putBoolean("air_open_bt", b).putBoolean("air_auto_connect", m == 1 ? true : b)
				.putBoolean("set_isantilost", b).putBoolean("set_isremind", m == 1 ? true : b)
				.putBoolean("set_isremind_call", m == 1 ? true : b).putBoolean("set_isremind_msg", m == 1 ? true : b)
				.putBoolean("set_find_phone", true).commit();
		if (m == 2) {
			airSetAntiLost(new int[] { 1 });
		} else {
			airSetAntiLost(new int[] { 0 });
		}
		if (m != 0) {
			airSetVibrate(true);
		}
	}

	void cancleNotification() {
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(NotificationUtils.FoundNotfication);
		nm.cancel(NotificationUtils.BlueNotfication);
		nm.cancel(NotificationUtils.LostNotfication);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		MobclickAgent.onPause(this);
		super.onPause();

	}

	@Override
	protected void onResume() {
		Intent i = new Intent(BluetoothLeService.ACTION_AIR_CONTROL);
		i.putExtra("task", 6);
		sendBroadcast(i);
		if (set_checkupdate != null && hh != null) {
			set_checkupdate.setVisibility(View.VISIBLE);
			htext.setVisibility(View.GONE);
			btn_update.setVisibility(View.GONE);
			hh.setVisibility(View.GONE);
			// update_anyway.setVisibility(View.GONE);
			sharePre = getSharedPreferences("air", MODE_MULTI_PROCESS);
			cur_version.setText(sharePre.getString("soft_version", ""));

		}

		MobclickAgent.onResume(this);
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		airRssiReportSwitch(0);
		unregisterReceiver(airReceiver);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		if (mProgressHandler != null) {
			mProgressHandler.removeCallbacksAndMessages(null);
		}
		if (checkhandler != null) {
			checkhandler.removeCallbacksAndMessages(null);
		}
		super.onStop();
	}

	/***
	 * 改变view及其子view的可用性
	 * 
	 * @param view
	 *            需要改变的view
	 * @param isChecked
	 *            更改为是否可用
	 */
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
		morrow.setTextColor(getResources().getColor(R.color.white_0_8));
		sleep_tips.setTextColor(getResources().getColor(R.color.white_0_8));
		autorize.setTextColor(getResources().getColor(R.color.white_0_8));
	}

	void commitMode3() {
		commitMode2();
		// 延时
		sharePre.edit().putInt("air_delay", delay).commit();
		Intent intent = new Intent();
		intent.putExtra("delay", delay);
		intent.setAction(BluetoothLeService.ACTION_PREF_CHANGED);
		sendBroadcast(intent);
		// 防丢
		sharePre.edit().putBoolean("set_isantilost", toogle_antilost.isChecked()).commit();
		// if (toogle_antilost.isChecked()) {
		// airSetAntiLost(new int[] { 1 });
		// } else {
		// airSetAntiLost(new int[] { 0 });
		// }
		// 防丢时间
		for (int i = 0; i < linear_lost_time.getChildCount(); i++) {
			String s = "" + ((TextView) (linear_lost_time.getChildAt(i).findViewById(R.id.ttt).findViewById(R.id.time)))
					.getText();
			String[] date = s.split("~");
			String[] date1 = date[0].split(":");
			String[] date2 = date[1].split(":");

			sharePre.edit().putString("set_antilost_time" + i, s).commit();
			sharePre.edit().putInt("set_antilost_time" + i + "_h1", Integer.valueOf(date1[0])).commit();
			sharePre.edit().putInt("set_antilost_time" + i + "_m1", Integer.valueOf(date1[1])).commit();
			sharePre.edit().putInt("set_antilost_time" + i + "_h2", Integer.valueOf(date2[0])).commit();
			sharePre.edit().putInt("set_antilost_time" + i + "_m2", Integer.valueOf(date2[1])).commit();
		}

	}

	void commitMode2() {
		commitMode1();
		// 智能提醒
		sharePre.edit().putBoolean("set_isremind", toogle_remind.isChecked()).commit();
		sharePre.edit().putBoolean("set_isremind_call", callremind.isChecked()).commit();
		sharePre.edit().putBoolean("set_isremind_msg", msgremind.isChecked()).commit();
		// 呼叫手机
		sharePre.edit().putBoolean("set_find_phone", callphone.isChecked()).commit();
		sharePre.edit().putBoolean("set_isremind_wechat", wechatremind.isChecked()).commit();
		// 自动重连
		sharePre.edit().putBoolean("air_auto_connect", auto_reconnect.isChecked()).commit();
		// 常开蓝牙
		sharePre.edit().putBoolean("air_open_bt", ble_open.isChecked()).commit();

	}

	void commitMode1() {
		// 睡眠
		sharePre.edit().putBoolean("set_issleep", toogle_sleep.isChecked()).putBoolean("set_find_phone", false)
				.commit();
		sharePre.edit().putString("set_sleep_time", set_sleep_time.getText() + "").commit();
	}

	private boolean settingChanged = false;

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		if (inited)
			settingChanged = true;
		switch (buttonView.getId()) {
		case R.id.toogle_liftwrist:
			if (device_status != 0) {
				buttonView.setChecked(isChecked);
				sharePre.edit().putInt("liftwrist", isChecked ? 1 : 0).commit();
				commitLiftWrist();
			} else {
				buttonView.setChecked(!isChecked);
				Toast.makeText(getApplicationContext(), getString(R.string.set_antilost_disconnect), Toast.LENGTH_SHORT)
						.show();
			}

			break;
		case R.id.set_checkbox_ble_open:
			ble_open.setChecked(isChecked);

			break;
		case R.id.set_checkbox_auto_connect:
			buttonView.setChecked(isChecked);

			break;
		case R.id.toogle_antilost:
			if (device_status != 0) {
				buttonView.setChecked(isChecked);
				sharePre.edit().putBoolean("set_isantilost", isChecked).commit();
				changeEnable(findViewById(R.id.set_warning_ring_border), isChecked);
				commitLost(isChecked);
			} else {
				buttonView.setChecked(!isChecked);
				Toast.makeText(getApplicationContext(), getString(R.string.set_antilost_disconnect), Toast.LENGTH_SHORT)
						.show();
			}

			break;
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
		case R.id.tog_app_notification:
			buttonView.setChecked(isChecked);
			for (int i = 1; i < linear_app_notification_border.getChildCount(); i++) {
				changeEnable(linear_app_notification_border.getChildAt(i), isChecked);
			}
			sharePre.edit().putBoolean("app_notification", isChecked).commit();
			break;
		case R.id.toogle_remind:
			buttonView.setChecked(isChecked);
			for (int i = 1; i < linear_remind.getChildCount(); i++) {
				changeEnable(linear_remind.getChildAt(i), isChecked);
			}

			break;
		case R.id.toogle_sleep:
			buttonView.setChecked(isChecked);
			for (int i = 1; i < linear_sleep.getChildCount(); i++) {
				changeEnable(linear_sleep.getChildAt(i), isChecked);
			}
			break;
		case R.id.set_checkbox_callphone: // 呼叫手机
			buttonView.setChecked(isChecked);
			break;
		case R.id.set_checkbox_remindcall:
			buttonView.setChecked(isChecked);
			sharePre.edit().putBoolean("set_isremind_call", isChecked).commit();

			break;
		case R.id.set_checkbox_remindmsg:
			buttonView.setChecked(isChecked);
			sharePre.edit().putBoolean("set_isremind_msg", isChecked).commit();
		case R.id.set_checkbox_remindmm:
			buttonView.setChecked(isChecked);
			sharePre.edit().putBoolean("set_isremind_wechat", isChecked).commit();
			break;

		}
	}

	private void commitLost(boolean isChecked) {
		// TODO Auto-generated method stub
		dialog = Util.initProgressDialog(this, true, getString(R.string.data_wait), null);
		Intent intent = new Intent();
		intent.setAction(BluetoothLeService.ACTION_BLE_CONFIG_CMD);
		intent.putExtra("type", 88);
		intent.putExtra("param", new int[] { isChecked ? 1 : 0 });
		sendBroadcast(intent);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data == null) {
			return;
		}
		switch (requestCode) {
		case REQUEST_CODE_PICK_RINGTONE:
			Uri pickedUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
			handleRingtonePicked(pickedUri);
			break;
		case RESULTCODE_CHOOSE_RING:

			// 将返回的uri转为path，方便后续利用
			Uri uri = data.getData();
			Cursor cursor = getContentResolver().query(uri, null, null, null, null);

			// 第一行第二列保存路径strRingPath
			cursor.moveToFirst();
			mRingtoneUri = cursor.getString(1);
			set_warning_ring.setText(new File(mRingtoneUri).getName());
			sharePre.edit().putString("set_warning_ring", new File(mRingtoneUri).getName()).commit();
			cursor.close();
			sharePre.edit().putString("set_warning_ring_path", mRingtoneUri).commit();
			break;

		}

	}

	private void handleRingtonePicked(Uri pickedUri) {
		if (pickedUri == null || RingtoneManager.isDefault(pickedUri)) {
			mRingtoneUri = null;
		} else {
			Cursor cursor = getContentResolver().query(pickedUri, null, null, null, null);

			// 第一行第二列保存路径strRingPath
			cursor.moveToFirst();
			mRingtoneUri = cursor.getString(1);
			// mRingtoneUri = pickedUri.toString();
		}
		// get ringtone name and you can save mRingtoneUri for database.
		if (mRingtoneUri != null) {
			String nameString = RingtoneManager.getRingtone(this, pickedUri).getTitle(this);
			set_warning_ring.setText(nameString);
			sharePre.edit().putString("set_warning_ring", nameString).commit();
			sharePre.edit().putString("set_warning_ring_path", mRingtoneUri).commit();
		} else {
			set_warning_ring.setText(getString(R.string.voice));
		}
		// ContentValues values = new ContentValues();
		// values.put(Contacts.CUSTOM_RINGTONE, mRingtoneUri);
		// //mContactId mean contacts id
		// getContentResolver().update(Contacts.CONTENT_URI, values,
		// Contacts._ID + " = " + mContactId, null);
	}

	int hh_counter = 0;
	int switch_debug_c = 0;
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
		AlertDialog.Builder ab = new AlertDialog.Builder(con);
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

	private ProgressDialog dialog;

	void commitLiftWrist() {
		dialog = Util.initProgressDialog(this, true, getString(R.string.data_wait), null);
		int lan2 = sharePre.getInt("lan", 1);
		if (lan2 == 2) {
			String lan = getResources().getConfiguration().locale.getCountry();

			if (lan.equals("CN")) {
				lan2 = 1;
			} else if (lan.equals("TW")) {

			} else if (lan.equals("UK")) {
				lan2 = 0;
			} else if (lan.equals("US")) {
				lan2 = 0;
			}
		}
		Intent intent = new Intent();
		intent.setAction(BluetoothLeService.ACTION_BLE_CONFIG_CMD);
		intent.putExtra("type", 89);
		intent.putExtra("param", new int[] { 1, lan2, sharePre.getInt("liftwrist", 1) });
		sendBroadcast(intent);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() != R.id.air_new && inited) {
			settingChanged = true;
		}
		switch (v.getId()) {
		case R.id.set_novoice_time1_week_border:
		case R.id.set_novoice_time1_week:
			showWeekSelector(1);
			break;
		case R.id.set_novoice_time2_week_border:
		case R.id.set_novoice_time2_week:
			showWeekSelector(2);
			break;
		case R.id.toogle_liftwrist_border:
			onCheckedChanged(toogle_liftwrist, !toogle_liftwrist.isChecked());
			break;
		case R.id.airlanguge_border:
			if (device_status != 0) {
				AlertDialog.Builder aba = new AlertDialog.Builder(con);
				aba.setTitle(R.string.air_language);
				final String[] str = new String[] { getString(R.string.language_zh), getString(R.string.language_en),
						getString(R.string.language_auto) };
				aba.setItems(str, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						// 0是英文 1是中文
						if (which < 2) {
							sharePre.edit().putInt("lan", 1 - which).commit();
							airlanguge.setText(str[which]);
						} else {
							sharePre.edit().putInt("lan", 2).commit();
							airlanguge.setText(str[which]);
						}
						commitLiftWrist();
					}
				});
				aba.create().show();
			} else {
				Toast.makeText(getApplicationContext(), getString(R.string.set_antilost_disconnect), Toast.LENGTH_SHORT)
						.show();
			}

			break;
		case R.id.autorize:
			Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
			startActivity(intent);
			break;
		case R.id.set_checkbox_callphone_border:
			CompoundButton toggle5 = getToggleButtonFromRelativeLayout((ViewGroup) v);
			if (toggle5 != null)
				onCheckedChanged(toggle5, !toggle5.isChecked());
			break;
		case R.id.update_anyway:
			if (device_status == 1) {
				if (HttpUtlis.checkAir(currentVision, newVision) >= 0) {
					dialog = Util.initProgressDialog(Util.getThemeContext(this), true, getString(R.string.data_wait),
							null);
					checkAirVersion(newVision);
				} else {
					Toast.makeText(this, getString(R.string.no_hex_enable), Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(this, getString(R.string.device_off), Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.hh:
			hh_counter++;
			if (hh_counter > 10) {
				btn_update.setVisibility(View.VISIBLE);
				btn_update.setEnabled(true);
				btn_update.setText(R.string.updown);

			}
			break;
		case R.id.switch_debug:
			switch_debug_c++;
			if (switch_debug_c >= 10) {
				boolean isdebug = sharePre.getBoolean("isdebug", false);
				isdebug = !isdebug;
				sharePre.edit().putBoolean("isdebug", isdebug).commit();
				if (isdebug) {
					Toast.makeText(AirPreferenceActivity.this, "已开启Air调试模式，请至首页，下拉刷新一次", Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(AirPreferenceActivity.this, "已关闭Air调试模式，请至首页，下拉刷新一次", Toast.LENGTH_LONG).show();
				}
				switch_debug_c = 0;
			}
			break;
		case R.id.linear_connection_status:

			airManualConnect(1 - device_status);
			if (device_status == 1)
				device_status = 0;
			if (tv_device_status != null) {
				if (device_status == 0) {
					tv_device_status.setText(getString(R.string.device_status_off));
					tv_device_rssi.setText("-/-");
				}
			}

			break;
		case R.id.linear_name:
			showNameInput();
			break;
		case R.id.set_prefe_btn_turnoff: // turn off device
			AlertDialog.Builder ab = new AlertDialog.Builder(con);
			ab.setTitle(R.string.turnoff);
			ab.setMessage(R.string.turnoffmsg);
			ab.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					airSetShutdown();
				}
			});
			ab.setPositiveButton(R.string.can, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}
			});
			ab.create().show();
			break;
		case R.id.set_prefe_btn_update: // update
			// 打开新的窗口来升级
			// filenameString = "Air v2.1.5.hex";
			// ShowDownProgress(filenameString);
			ShowDownProgress(newVision + ".hex");
			break;
		case R.id.set_prefe_btn_addtime: // 添加时间段
			int x = 0;
			for (int i = 0; i < 2; i++) {
				if (sharePre.contains("set_antilost_time" + i)
						&& !sharePre.getString("set_antilost_time" + i, "").equals("")) {
					x++;
				}
			}
			if (x >= 2 || linear_lost_time.getChildCount() >= 2) {
				System.out.println((x >= 2) + "" + (linear_lost_time.getChildCount() >= 2));
				return;
			}
			addTime();
			break;
		case R.id.set_sleep_time_border:
			timesPicker(v, -1);

			break;
		case R.id.set_safe_distance_border:

			showDistance(v);

			break;
		case R.id.set_warning_ring_border:
			show();
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
			View vie = LayoutInflater.from(this).inflate(R.layout.set_timepicker, null);
			vie.findViewById(R.id.container).setOnClickListener(null);
			final TimePicker t = (TimePicker) vie.findViewById(R.id.time_start);
			Calendar calendar = Calendar.getInstance();
			t.setIs24Hour(true);
			calendar.set(Calendar.HOUR_OF_DAY, hour);
			calendar.set(Calendar.MINUTE, min);
			t.setCalendar(calendar);
			AlertDialog.Builder aDailog = new AlertDialog.Builder(con);
			aDailog.setTitle(R.string.select_time);
			aDailog.setView(vie);
			aDailog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					String s = turnNum(t.getHourOfDay()) + ":" + turnNum(t.getMinute());
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

			View vie2 = LayoutInflater.from(this).inflate(R.layout.set_timepicker, null);
			vie2.findViewById(R.id.container).setOnClickListener(null);
			final TimePicker t2 = (TimePicker) vie2.findViewById(R.id.time_start);
			t2.setIs24Hour(true);
			Calendar calendar2 = Calendar.getInstance();

			calendar2.set(Calendar.HOUR_OF_DAY, hour2);
			calendar2.set(Calendar.MINUTE, min2);
			t2.setCalendar(calendar2);

			AlertDialog.Builder aDailog1 = new AlertDialog.Builder(con);
			aDailog1.setTitle(R.string.select_time);
			aDailog1.setView(vie2);
			aDailog1.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					String s = turnNum(t2.getHourOfDay()) + ":" + turnNum(t2.getMinute());
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
		case R.id.set_checkupdate:
			getLastestVersion();
			break;
		case R.id.toogle_novoice1_border:
			CompoundButton toggle = getToggleButtonFromRelativeLayout((ViewGroup) v);
			if (toggle != null)
				onCheckedChanged(toggle, !toggle.isChecked());
			break;
		case R.id.toogle_novoice2_border:
			CompoundButton toggle1 = getToggleButtonFromRelativeLayout((ViewGroup) v);
			if (toggle1 != null)
				onCheckedChanged(toggle1, !toggle1.isChecked());
			break;
		case R.id.toogle_remind_border:
			CompoundButton toggle2 = getToggleButtonFromRelativeLayout((ViewGroup) v);
			if (toggle2 != null)
				onCheckedChanged(toggle2, !toggle2.isChecked());
			break;
		case R.id.toogle_antilost_border:
			if (device_status != 0) {
				// CompoundButton toggle3 =
				// getToggleButtonFromRelativeLayout((ViewGroup) v);
				// if (toggle3 != null)
				// onCheckedChanged(toggle3, !toggle3.isChecked());
				toogle_antilost.setChecked(!toogle_antilost.isChecked());
			} else {
				Toast.makeText(getApplicationContext(), getString(R.string.set_antilost_disconnect), Toast.LENGTH_SHORT)
						.show();
			}
			break;
		case R.id.toogle_sleep_border:
			CompoundButton toggle4 = getToggleButtonFromRelativeLayout((ViewGroup) v);
			if (toggle4 != null)
				onCheckedChanged(toggle4, !toggle4.isChecked());
			break;

		case R.id.set_checkbox_connect_border:
			CompoundButton toggle7 = getToggleButtonFromRelativeLayout((ViewGroup) v);
			if (toggle7 != null)
				onCheckedChanged(toggle7, !toggle7.isChecked());
			break;
		case R.id.set_checkbox_remindcall_border:
			CompoundButton toggle6 = getToggleButtonFromRelativeLayout((ViewGroup) v);
			if (toggle6 != null)
				onCheckedChanged(toggle6, !toggle6.isChecked());
			break;
		case R.id.set_checkbox_remindmsg_border:
			CompoundButton toggle8 = getToggleButtonFromRelativeLayout((ViewGroup) v);
			if (toggle8 != null)
				onCheckedChanged(toggle8, !toggle8.isChecked());
			break;
		case R.id.set_checkbox_remindmm_border:
			CompoundButton toggle9 = getToggleButtonFromRelativeLayout((ViewGroup) v);
			if (toggle9 != null)
				onCheckedChanged(toggle9, !toggle9.isChecked());
			break;
		case R.id.air_new:
			Intent ii = new Intent(AirPreferenceActivity.this, AirNewExperience.class);
			startActivity(ii);
			break;

		default:
			break;
		}
	}

	private void checkAirVersion(String newVision) {
		// TODO Auto-generated method stub
		if (newVision == null || newVision.length() == 0) {
			Toast.makeText(this, getString(R.string.deleteerror), Toast.LENGTH_SHORT).show();
		}
		String filepath = AirPreferenceActivity.this.getFilesDir().toString() + "/air";
		FileDownUtils fileDownUtils = new FileDownUtils(new OnHexDownloadListener() {

			@Override
			public void OnHexProgress(float pre) {
				// TODO Auto-generated method stub
			}

			@Override
			public void OnHexDownloaded(String name) {
				// TODO Auto-generated method stub
				if (name != null) {
					Intent intent = new Intent(BluetoothLeService.ACTION_AIR_CONTROL);
					intent.putExtra("task", 4);
					intent.putExtra("filename", "Air.hex");
					intent.putExtra("version", name);
					AirPreferenceActivity.this.sendBroadcast(intent);
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							if (dialog != null && dialog.isShowing()) {
								dialog.dismiss();
							}
						}
					});
				}
			}
		}, filepath, Parser.httppath + newVision + ".hex");
		fileDownUtils.start();

		// appShare.edit().putString("lastest_air", newVision).commit();
	}

	/**
	 * 检测无声闹铃设置状态，生成长度为6的byte数组{0[1],x,x,0[1],x,x} 0、3位表示闹钟开启状态后面两位时、分
	 */
	private void commitAlarm() {
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

	/***
	 * 获得横框中的togglebutton / checkbox
	 * 
	 * @param group
	 * @return
	 */
	public static CompoundButton getToggleButtonFromRelativeLayout(ViewGroup group) {
		for (int i = 0; i < group.getChildCount(); i++) {
			if (group.getChildAt(i) instanceof CompoundButton) {
				return (CompoundButton) group.getChildAt(i);
			} else if (group.getChildAt(i) instanceof ViewGroup) {
				return getToggleButtonFromRelativeLayout((ViewGroup) group.getChildAt(i));
			}
		}
		return null;

	}

	boolean ok = false;

	/***
	 * 显示时间段选择的控件
	 * 
	 * @param v
	 *            标识由哪个View触发
	 * @param index
	 *            如果是-1表示存储的是睡眠时间，不是-1则是防丢时间值的索引
	 */
	void timesPicker(final View v, final int index) {
		View vv = LayoutInflater.from(this).inflate(R.layout.set_timespicker, null);
		vv.findViewById(R.id.container).setOnClickListener(null);
		final TimePicker t1 = (TimePicker) vv.findViewById(R.id.time_start);
		final TimePicker t2 = (TimePicker) vv.findViewById(R.id.time_end);
		final TextView tips = (TextView) vv.findViewById(R.id.time_tips);
		t1.setIs24Hour(true);
		t2.setIs24Hour(true);
		Calendar calendar1 = Calendar.getInstance();
		Calendar calendar2 = Calendar.getInstance();

		if (v.getId() == R.id.set_sleep_time_border) {
			String s = sharePre.getString("set_sleep_time", "22:00~07:00");
			String[] s1 = s.split("~");
			String[] time1 = s1[0].split(":");
			String[] time2 = s1[1].split(":");
			calendar1.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time1[0]));
			calendar1.set(Calendar.MINUTE, Integer.parseInt(time1[1]));
			calendar2.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time2[0]));
			calendar2.set(Calendar.MINUTE, Integer.parseInt(time2[1]));
		} else {
			calendar1.set(Calendar.HOUR_OF_DAY, 7);
			calendar1.set(Calendar.MINUTE, 00);
			calendar2.set(Calendar.HOUR_OF_DAY, 22);
			calendar2.set(Calendar.MINUTE, 00);
		}
		t1.setCalendar(calendar1);
		t2.setCalendar(calendar2);
		t1.setOnTimeChanged(new OnTimeChanged() {

			@Override
			public void onTimeChanged(int hour, int min) {
				// TODO Auto-generated method stub
				if (hour == 12 && min > 0) {
					hour = 13;
				}
				if (hour - t2.getHourOfDay() > 0 && hour < 12
						|| hour - t2.getHourOfDay() > 0 && t2.getHourOfDay() > 12) {
					ok = false;
					tips.setText(getString(R.string.sleep_mistake));

				} else {
					ok = true;
					tips.setText("");
				}
			}
		});
		t2.setOnTimeChanged(new OnTimeChanged() {

			@Override
			public void onTimeChanged(int hour, int min) {
				// TODO Auto-generated method stub
				if (hour == 12 && min > 0) {
					hour = 13;
				}
				if (hour - t1.getHourOfDay() < 0 && hour > 12
						|| hour - t1.getHourOfDay() < 0 && t1.getHourOfDay() < 12) {
					tips.setText(getString(R.string.sleep_mistake));
					ok = false;
				} else {
					ok = true;
					tips.setText("");
				}
			}
		});

		AlertDialog.Builder aDailog = new AlertDialog.Builder(con);
		aDailog.setTitle(R.string.select_times);
		aDailog.setView(vv);
		aDailog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				int b_h = t1.getHourOfDay();
				int b_m = t1.getMinute();
				int e_h = t2.getHourOfDay();
				int e_m = t2.getMinute();

				String s1 = turnNum(b_h) + ":" + turnNum(b_m);
				String s2 = turnNum(e_h) + ":" + turnNum(e_m);
				if (index != -1) {
					if (Integer.parseInt(s1.replace(":", "")) - Integer.parseInt(s2.replace(":", "")) > 0
							&& index != -1) {

						Toast.makeText(AirPreferenceActivity.this, getString(R.string.time_mistake), Toast.LENGTH_SHORT)
								.show();
						return;
					}
				}

				if (v instanceof TextView) {

					((TextView) v).setText(s1 + "~" + s2);
				} else if (v instanceof ViewGroup) {
					if (ok) {
						((TextView) ((ViewGroup) v).getChildAt(3)).setText(s1 + "~" + s2);
						// sharePre.edit()
						// .putString("set_sleep_time",
						// s1 + "~" + s2).commit();
					} else {
						((TextView) ((ViewGroup) v).getChildAt(3))
								.setTextColor(getResources().getColor(R.color.warning));
						((TextView) ((ViewGroup) v).getChildAt(3)).setText(s1 + "~" + s2);
					}

				}
				// morrow.setText(add);
				// morrow.setTextColor(getResources().getColor(R.color.white_0_8));

				if (index != -1) {
					sharePre.edit().putString("set_antilost_time" + index, s1 + "~" + s2).commit();
					sharePre.edit().putInt("set_antilost_time" + index + "_h1", b_h).commit();
					sharePre.edit().putInt("set_antilost_time" + index + "_m1", b_m).commit();
					sharePre.edit().putInt("set_antilost_time" + index + "_h2", e_h).commit();
					sharePre.edit().putInt("set_antilost_time" + index + "_m2", e_m).commit();

				}
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
	}

	/***
	 * 9 -> 09
	 * 
	 * @param i
	 * @return
	 */
	public static String turnNum(int i) {
		String s;
		if (i < 10)
			s = "0" + i;
		else
			s = i + "";
		return s;
	}

	private void delectTime(int index, int count) {
		System.out.println("click:" + index + ";count:" + count);
		for (int i = index; i < count; i++) {

			if (index + 1 >= count) {
				System.out.println("delete" + i);
				sharePre.edit().remove("set_antilost_time" + i).commit();
				return;
			} else {
				sharePre.edit()
						.putString("set_antilost_time" + i, sharePre.getString("set_antilost_time" + (i + 1), ""))
						.commit();
			}
		}
	}

	/***
	 * 安全距离选择
	 */
	private void showDistance(final View view) {
		// 给vie设置点击事件 用以实现popupwindow周边变暗并可 点击周边消失的功能
		View vie = LayoutInflater.from(this).inflate(R.layout.set_distancepicker, null);
		final NumberPicker n = (NumberPicker) vie.findViewById(R.id.time_start);
		// 截获vie上的click事件
		vie.findViewById(R.id.container).setOnClickListener(null);
		n.setMaxValue(2);
		n.setMinValue(1);
		int vv = sharePre.getInt("set_lost_distance", 2);
		n.setValue(vv);

		AlertDialog.Builder aDailog = new AlertDialog.Builder(con);
		aDailog.setTitle(R.string.divice_preference_distance);
		aDailog.setView(vie);
		aDailog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				set_safe_distance.setText(n.getValue() + getString(R.string.unit_distance));
				sharePre.edit().putInt("set_lost_distance", n.getValue()).commit();
				// MilinkApplication.air_lost_distance = n.getValue();
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
	}

	/***
	 * 显示铃声选择的dialog
	 */
	void show() {
		final CharSequence[] items = { getString(R.string.ring_from_sys), getString(R.string.ring_from_file) };

		AlertDialog dlg = new AlertDialog.Builder(con).setTitle(getString(R.string.ring_select))
				.setItems(items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						if (item == 0) {
							Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
							// Allow user to pick 'Default'
							intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
							// Show only ringtones
							intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
							// Don't show 'Silent'
							intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);

							Uri ringtoneUri;
							if (mRingtoneUri != null) {
								ringtoneUri = Uri.parse(mRingtoneUri);
							} else {
								// Otherwise pick default ringtone Uri so that
								// something is
								// selected.
								ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
							}

							// Put checkmark next to the current ringtone for
							// this contact
							intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, ringtoneUri);

							// Launch!
							// startActivityForResult(intent,
							// REQUEST_CODE_PICK_RINGTONE);
							startActivityForResult(intent, REQUEST_CODE_PICK_RINGTONE);

						} else if (item == 1) {
							Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
							intent.setType("audio/*");

							startActivityForResult(intent, RESULTCODE_CHOOSE_RING);
						}
					}
				}).create();
		dlg.setCanceledOnTouchOutside(true);
		dlg.show();
	}

	/***
	 * 在布局上添加一条设置防丢的时间段，并设置其监听
	 */
	void addTime() {
		View view = LayoutInflater.from(AirPreferenceActivity.this).inflate(R.layout.item_setting_antilost_time, null);

		linear_lost_time.addView(view);

		for (int i = 0; i < linear_lost_time.getChildCount(); i++) {
			linear_lost_time.getChildAt(i).findViewById(R.id.ttt).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) { // 添加的时间段后，设置时间事件

					// linear_lost_time.indexOfChild(v) 获得点击条目的index
					final TextView t = (TextView) v.findViewById(R.id.time);
					timesPicker(t, linear_lost_time.indexOfChild(v));

				}
			});
			linear_lost_time.getChildAt(i).findViewById(R.id.ttt).setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					// TODO Auto-generated method stub
					int index = linear_lost_time.indexOfChild(v);
					if (index > 0) {
						delectTime(index, linear_lost_time.getChildCount());
						linear_lost_time.removeViewAt(index);
					} else {
						Toast.makeText(getApplicationContext(), getApplication().getString(R.string.longtoucherror),
								Toast.LENGTH_LONG).show();
					}
					return true;
				}
			});
		}
	}

	public ProgressDialog initProgressDialog() {
		ProgressDialog progressDialog = ProgressDialog.show(this, getString(R.string.data_wait),
				getString(R.string.data_getting), true);
		progressDialog.setCancelable(true);

		LayoutInflater inflater = LayoutInflater.from(this);
		View v = inflater.inflate(R.layout.loading, null);
		TextView tvMsg = (TextView) v.findViewById(R.id.tvTitle);

		if (tvMsg != null) {
			tvMsg.setText(getString(R.string.data_getting));
		}
		progressDialog.setContentView(v);
		progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub

			}
		});

		return progressDialog;
	}

	TextView sleep_tips, autorize;
	RelativeLayout set_checkbox_connect_border, set_novoice_time1_week_border, set_novoice_time2_week_border;
	TextView set_novoice_time1_week, set_novoice_time2_week;
	NewSeekBar seekbar;
	int delay;

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

	RelativeLayout set_checkbox_remindmm_border;

	void initViews() {
		autorize = (TextView) findViewById(R.id.autorize);
		autorize.setOnClickListener(this);
		set_novoice_time1_week_border = (RelativeLayout) findViewById(R.id.set_novoice_time1_week_border);
		set_novoice_time1_week = (TextView) findViewById(R.id.set_novoice_time1_week);
		set_novoice_time2_week_border = (RelativeLayout) findViewById(R.id.set_novoice_time2_week_border);
		set_novoice_time2_week = (TextView) findViewById(R.id.set_novoice_time2_week);

		set_novoice_time1_week.setText(getWeek(1));
		set_novoice_time1_week_border.setOnClickListener(this);
		set_novoice_time1_week.setOnClickListener(this);
		set_novoice_time2_week.setText(getWeek(2));
		set_novoice_time2_week_border.setOnClickListener(this);
		set_novoice_time2_week.setOnClickListener(this);
		linear_callphone = (LinearLayout) findViewById(R.id.linear_callphone);
		linear_setting = (LinearLayout) findViewById(R.id.linear_setting);
		linear_delay = (LinearLayout) findViewById(R.id.linear_delay);
		sys_set = (TextView) findViewById(R.id.sys_set);
		seekbar = (NewSeekBar) findViewById(R.id.seekbar);
		delay = sharePre.getInt("air_delay", 10);
		seekbar.setNumberThumb(delay);
		seekbar.setProgress((int) (((delay - 3) / 57f) * 100));
		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				if (inited)
					settingChanged = true;
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				// TODO Auto-generated method stub
				delay = (int) (57 * (progress / 100f)) + 3;
				seekbar.setNumberThumb(delay);
			}
		});
		sleep_tips = (TextView) findViewById(R.id.sleep_tips);
		RelativeLayout set_checkbox_remindcall_border = (RelativeLayout) findViewById(
				R.id.set_checkbox_remindcall_border);
		RelativeLayout set_checkbox_remindmsg_border = (RelativeLayout) findViewById(
				R.id.set_checkbox_remindmsg_border);
		set_checkbox_remindmm_border = (RelativeLayout) findViewById(R.id.set_checkbox_remindmm_border);
		set_checkbox_connect_border = (RelativeLayout) findViewById(R.id.set_checkbox_connect_border);
		set_checkbox_remindmm_border.setOnClickListener(this);
		set_checkbox_remindcall_border.setOnClickListener(this);
		set_checkbox_remindmsg_border.setOnClickListener(this);
		set_checkbox_connect_border.setOnClickListener(this);

		layout_device_name = (RelativeLayout) findViewById(R.id.linear_name);
		layout_device_status = (RelativeLayout) findViewById(R.id.linear_connection_status);
		layout_device_name.setOnClickListener(this);
		tv_device_name = (TextView) findViewById(R.id.tv_device_name);
		toogle_novoice1_border = (RelativeLayout) findViewById(R.id.toogle_novoice1_border);
		toogle_novoice2_border = (RelativeLayout) findViewById(R.id.toogle_novoice2_border);
		toogle_remind_border = (RelativeLayout) findViewById(R.id.toogle_remind_border);
		toogle_antilost_border = (RelativeLayout) findViewById(R.id.toogle_antilost_border);
		tog_app_notification = (ToggleButton) findViewById(R.id.tog_app_notification);
		toogle_sleep_border = (RelativeLayout) findViewById(R.id.toogle_sleep_border);
		toogle_novoice1_border.setOnClickListener(this);
		toogle_novoice2_border.setOnClickListener(this);
		toogle_remind_border.setOnClickListener(this);
		toogle_antilost_border.setOnClickListener(this);
		toogle_sleep_border.setOnClickListener(this);
		toogle_novoice1 = (ToggleButton) findViewById(R.id.toogle_novoice1);
		toogle_novoice2 = (ToggleButton) findViewById(R.id.toogle_novoice2);
		toogle_remind = (ToggleButton) findViewById(R.id.toogle_remind);
		toogle_antilost = (ToggleButton) findViewById(R.id.toogle_antilost);
		toogle_sleep = (ToggleButton) findViewById(R.id.toogle_sleep);
		linear_voice1 = (LinearLayout) findViewById(R.id.linear_voice1);
		linear_voice2 = (LinearLayout) findViewById(R.id.linear_voice2);
		linear_remind = (LinearLayout) findViewById(R.id.linear_remind);
		linear_sleep = (LinearLayout) findViewById(R.id.linear_sleep);
		linear_antilost = (LinearLayout) findViewById(R.id.linear_antilost);
		linear_airnew = (LinearLayout) findViewById(R.id.linear_airnew);
		linear_lost_time = (LinearLayout) findViewById(R.id.linear_lost_time);

		btn_addtime = (Button) findViewById(R.id.set_prefe_btn_addtime);
		btn_update = (Button) findViewById(R.id.set_prefe_btn_update);
		btn_off = (Button) findViewById(R.id.set_prefe_btn_turnoff);
		hh = (TextView) findViewById(R.id.hh);
		switch_debug = (TextView) findViewById(R.id.switch_debug);
		switch_debug.setOnClickListener(this);
		htext = (TextView) findViewById(R.id.htext);
		callremind = (CheckBox) findViewById(R.id.set_checkbox_remindcall);
		msgremind = (CheckBox) findViewById(R.id.set_checkbox_remindmsg);
		callphone = (CheckBox) findViewById(R.id.set_checkbox_callphone);
		wechatremind = (CheckBox) findViewById(R.id.set_checkbox_remindmm);
		wechatremind.setOnCheckedChangeListener(this);
		air_new = (RelativeLayout) findViewById(R.id.air_new);
		linear_app_notification_border = (LinearLayout) findViewById(R.id.linear_app_notification_border);
		ishide = (TextView) findViewById(R.id.ishide);
		air_new.setOnClickListener(this);

		morrow = (TextView) findViewById(R.id.morrow);
		line = (TextView) findViewById(R.id.line);
		set_sleep_time = (TextView) findViewById(R.id.set_sleep_time);
		set_safe_distance = (TextView) findViewById(R.id.set_safe_distance);
		set_warning_ring = (TextView) findViewById(R.id.set_warning_ring);
		set_novoice_time1 = (TextView) findViewById(R.id.set_novoice_time1);
		set_novoice_time2 = (TextView) findViewById(R.id.set_novoice_time2);
		set_checkupdate = (Button) findViewById(R.id.set_checkupdate);
		cur_version = (TextView) findViewById(R.id.cur_version);
		RelativeLayout set_sleep_time_border = (RelativeLayout) findViewById(R.id.set_sleep_time_border);
		set_sleep_time_border.setOnClickListener(this);
		RelativeLayout set_safe_distance_border = (RelativeLayout) findViewById(R.id.set_safe_distance_border);
		set_safe_distance_border.setOnClickListener(this);
		// set_sleep_time.setOnClickListener(this);
		// set_safe_distance.setOnClickListener(this);
		RelativeLayout set_warning_ring_border = (RelativeLayout) findViewById(R.id.set_warning_ring_border);
		set_warning_ring_border.setOnClickListener(this);
		RelativeLayout set_novoice_time1_border = (RelativeLayout) findViewById(R.id.set_novoice_time1_border);
		set_novoice_time1_border.setOnClickListener(this);
		RelativeLayout set_novoice_time2_border = (RelativeLayout) findViewById(R.id.set_novoice_time2_border);
		set_novoice_time2_border.setOnClickListener(this);
		update_anyway = (TextView) findViewById(R.id.update_anyway);
		// update_anyway.setVisibility(View.GONE);
		update_anyway.setOnClickListener(this);
		// set_warning_ring.setOnClickListener(this);
		// set_novoice_time1.setOnClickListener(this);
		// set_novoice_time2.setOnClickListener(this);
		set_checkupdate.setOnClickListener(this);
		hh.setOnClickListener(this);

		// mshen add for manual connect air
		layout_device_status.setOnClickListener(this);
		boolean air_open_bt = sharePre.getBoolean("air_open_bt", false);
		ble_open.setChecked(air_open_bt);
		// 初始化toogle，checkbox存储值
		// 呼叫手机
		boolean bool_find_phone = sharePre.getBoolean("set_find_phone", true);
		if (bool_find_phone) {
			callphone.setChecked(true);
		} else {
			callphone.setChecked(false);

		}
		// 微信通知
		boolean bool_wechat = sharePre.getBoolean("set_isremind_wechat", false);
		if (bool_wechat) {
			wechatremind.setChecked(true);
		} else {
			wechatremind.setChecked(false);

		}
		// 睡眠开启
		boolean bool_is_sleep = sharePre.getBoolean("set_issleep", true);
		if (bool_is_sleep) {
			toogle_sleep.setChecked(true);
		} else {
			toogle_sleep.setChecked(false);
		}
		for (int i = 1; i < linear_sleep.getChildCount(); i++) {
			changeEnable(linear_sleep.getChildAt(i), bool_is_sleep);
		}

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
		// 闹钟2
		boolean app_notification = sharePre.getBoolean("app_notification", true);
		tog_app_notification.setChecked(app_notification);

		for (int i = 1; i < linear_app_notification_border.getChildCount(); i++) {
			changeEnable(linear_app_notification_border.getChildAt(i), app_notification);
		}
		// 智能提醒
		boolean bool_remind = sharePre.getBoolean("set_isremind", true);
		if (bool_remind) {
			toogle_remind.setChecked(true);
			if (sharePre.getBoolean("set_isremind_call", true)) {
				callremind.setChecked(true);
			} else {
				callremind.setChecked(false);
			}
			if (sharePre.getBoolean("set_isremind_msg", true)) {
				msgremind.setChecked(true);
			} else {
				msgremind.setChecked(false);
			}

		} else {

			if (sharePre.getBoolean("set_isremind_call", false)) {
				callremind.setChecked(true);
			} else {
				callremind.setChecked(false);
			}
			if (sharePre.getBoolean("set_isremind_msg", false)) {
				msgremind.setChecked(true);
			} else {
				msgremind.setChecked(false);
			}
			toogle_remind.setChecked(false);
		}
		for (int i = 1; i < linear_remind.getChildCount(); i++) {
			changeEnable(linear_remind.getChildAt(i), bool_remind);
		}
		// 初始化 文本存储值
		// 睡眠时间
		set_sleep_time.setText(sharePre.getString("set_sleep_time", "22:00~07:30"));
		// 防丢时间
		if (!sharePre.contains("set_antilost_time0")) {
			sharePre.edit().putString("set_antilost_time0", "08:00~20:00").commit();
			sharePre.edit().putInt("set_antilost_time0_h1", 8).commit();
			sharePre.edit().putInt("set_antilost_time0_m1", 0).commit();
			sharePre.edit().putInt("set_antilost_time0_h2", 20).commit();
			sharePre.edit().putInt("set_antilost_time0_m2", 0).commit();
		}

		for (int i = 0; i < 2; i++) {
			if (sharePre.contains("set_antilost_time" + i)
					&& !sharePre.getString("set_antilost_time" + i, "").equals("")) {
				View view = LayoutInflater.from(AirPreferenceActivity.this).inflate(R.layout.item_setting_antilost_time,
						null);
				linear_lost_time.addView(view);
				((TextView) (view.findViewById(R.id.time))).setText(sharePre.getString("set_antilost_time" + i, ""));
				view.findViewById(R.id.ttt).setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						final TextView t = (TextView) v.findViewById(R.id.time);
						timesPicker(t, linear_lost_time.indexOfChild(v));
					}
				});
				view.findViewById(R.id.ttt).setOnLongClickListener(new OnLongClickListener() {

					@Override
					public boolean onLongClick(View v) {
						// TODO Auto-generated method stub

						int index = linear_lost_time.indexOfChild(v);
						if (index > 0) {
							delectTime(index, linear_lost_time.getChildCount());
							linear_lost_time.removeViewAt(index);
						} else {
							Toast.makeText(getApplicationContext(), getApplication().getString(R.string.longtoucherror),
									Toast.LENGTH_LONG).show();
						}
						return true;
					}
				});
			}
		}
		// 防丢 放在时间段添加完之后因为添加后要修改其不可点击
		boolean bool_anti_lost = sharePre.getBoolean("set_isantilost", false);
		if (bool_anti_lost) {
			toogle_antilost.setChecked(true);
		} else {
			toogle_antilost.setChecked(false);
		}
		for (int i = 1; i < linear_antilost.getChildCount(); i++) {
			changeEnable(linear_antilost.getChildAt(i), bool_anti_lost);
		}
		// 报警铃声
		set_warning_ring.setText(sharePre.getString("set_warning_ring", "default"));
		set_warning_ring_path = sharePre.getString("set_warning_ring_path", "media/internal/audio/media/1");
		// 闹钟1
		set_novoice_time1.setText(sharePre.getString("set_alarm1_time", "07:00"));
		// 闹钟2
		set_novoice_time2.setText(sharePre.getString("set_alarm2_time", "07:00"));
		// 防丢距离
		set_safe_distance.setText(sharePre.getInt("set_lost_distance", 2) + getString(R.string.unit_distance));

		// 软件版本
		cur_version.setText("Air v0.1");
		if (cur_version != null) {
			cur_version.setText(sharePre.getString("soft_version", ""));
		}

		// 禁止点击
		btn_update.setEnabled(false);

		callremind.setOnCheckedChangeListener(this);

		msgremind.setOnCheckedChangeListener(this);
		callphone.setOnCheckedChangeListener(this);
		toogle_novoice1.setOnCheckedChangeListener(this);
		tog_app_notification.setOnCheckedChangeListener(this);
		toogle_novoice2.setOnCheckedChangeListener(this);
		toogle_remind.setOnCheckedChangeListener(this);
		toogle_antilost.setOnCheckedChangeListener(this);
		toogle_sleep.setOnCheckedChangeListener(this);
		btn_addtime.setOnClickListener(this);
		btn_update.setOnClickListener(this);
		btn_off.setOnClickListener(this);
		inited = true;
	}

	boolean inited = false;

	private void ShowDownProgress(String filename) {
		Intent intent = new Intent(this, FileDownDialog.class);
		intent.putExtra("filename", filename);
		startActivity(intent);
	}

	Handler checkhandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case 1:
				try {
					if (progressDialog != null && progressDialog.isShowing()) {
						progressDialog.dismiss();
					}
					// findViewById(R.id.progress).setVisibility(View.GONE);
					currentVision = sharePre.getString("soft_version", sharePre.getString("soft_version", ""));
					// currentVision = "agaaag";
					currentVision = (String) cur_version.getText();

					if (currentVision.toUpperCase().contains("AIRIII")) {
						newVision = ((JSONObject) (msg.obj)).optString("verNameIII");
						newVersionText = ((JSONObject) (msg.obj)).optString("textIII", "");
					} else if (currentVision.toUpperCase().contains("AIRII")) {
						newVision = ((JSONObject) (msg.obj)).optString("verNameII");
						newVersionText = ((JSONObject) (msg.obj)).optString("textII", "");
					} else if (currentVision.toUpperCase().contains("AIR")) {
						newVision = ((JSONObject) (msg.obj)).optString("verName");
						newVersionText = ((JSONObject) (msg.obj)).optString("text", "");
					}
					if (HttpUtlis.checkAir(currentVision, newVision) != 1) {
						hh.setVisibility(View.VISIBLE);
						hh.setText(R.string.move_new_version);
						set_checkupdate.setVisibility(View.GONE);
						// appShare.edit().putBoolean("airupdate",
						// false).commit();
					} else {
						set_checkupdate.setVisibility(View.GONE);
						hh.setVisibility(View.VISIBLE);
						hh.setText(getString(R.string.lastest_v) + newVision);
						htext.setVisibility(View.VISIBLE);
						htext.setText(newVersionText);
						btn_update.setVisibility(View.VISIBLE);
						btn_update.setEnabled(true);
						btn_update.setText(R.string.updown);
					}
					Object o = set_checkupdate.getTag();
					if (o != null && o instanceof BadgeView) {
						((BadgeView) o).setBadgeCount(0);
					}
					update_anyway.setVisibility(View.VISIBLE);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;

			default:
				break;
			}
		}
	};

	/***
	 * 显示修改设备名称对话框
	 */
	private void showNameInput() {
		AlertDialog.Builder ab = new AlertDialog.Builder(con);
		final EditText e = new EditText(con);
		e.setTextColor(getResources().getColor(R.color.blue_ui));
		e.setText(tv_device_name.getText());

		ab.setTitle(R.string.change_device_name);
		ab.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				String name = e.getText().toString();
				tv_device_name.setText(name);
				// 硬件上的信息
				sharePre.edit().putString("device_name", name);
				airSetName(name);
			}
		});
		ab.setNeutralButton(R.string.can, null);
		LinearLayout l = new LinearLayout(con);
		l.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		e.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		l.setPadding(20, 30, 20, 10);
		l.addView(e);
		ab.setView(l);
		ab.setCancelable(false);
		ab.create().show();
	}

	public void getLastestVersion() {
		progressDialog = initProgressDialog();
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				String result = HttpUtlis.queryStringForPost(HttpUtlis.UPDATE_AIR);

				try {
					result = new String(result.getBytes("iso8859-1"), "utf-8");
					JSONObject js = new JSONObject(result);
					checkhandler.obtainMessage(1, js).sendToTarget();
				} catch (Exception e) {
					e.printStackTrace();
					if (progressDialog != null && progressDialog.isShowing()) {
						progressDialog.dismiss();
					}
				}
			}
		}).start();
	}

	// added by mshen
	// type = 0 to disconnect, type=1 to connect if not connected yet
	void airManualConnect(int type) {
		Intent intent;
		switch (type) {
		case 0:
			Toast.makeText(getApplicationContext(), getString(R.string.air_setting_manual_disconnect),
					Toast.LENGTH_SHORT).show();
			intent = new Intent();
			intent.setAction(BluetoothLeService.ACTION_BLE_MANAGE_CMD);
			intent.putExtra("type", 0);
			sendBroadcast(intent);
			break;
		case 1:

			Dbutils db = new Dbutils(this);
			String address = db.getBTDeivceAddress();
			if (address == null || address.length() != 17) {
				Toast.makeText(getApplicationContext(), getString(R.string.no_air_address), Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getApplicationContext(), getString(R.string.air_setting_manual_connect),
						Toast.LENGTH_SHORT).show();
				intent = new Intent(this, BluetoothLeService.class);
				intent.putExtra("address", address);
				// 1 普通模式 2是固件升级
				intent.putExtra("command", 1);
				intent.putExtra("scanflag", 1);
				startService(intent);
			}

			break;
		default:
			break;
		}
	}

	void airRssiReportSwitch(int type) {
		Intent intent;
		switch (type) {
		// start
		case 1:
			sharePre.edit().putBoolean("air_report_rssi", true).commit();
			// ((MilinkApplication)getApplication()).setbReportRssi(true);
			// MilinkApplication.bReportRssi = true;
			// intent = new Intent();
			// intent.setAction(BluetoothLeService.ACTION_BLE_MANAGE_CMD);
			// intent.putExtra("type", 2);
			// sendBroadcast(intent);
			break;
		// stop
		case 0:
			sharePre.edit().putBoolean("air_report_rssi", false).commit();
			// ((MilinkApplication)getApplication()).setbReportRssi(false);
			// MilinkApplication.bReportRssi = true;
			// intent = new Intent();
			// intent.setAction(BluetoothLeService.ACTION_BLE_MANAGE_CMD);
			// intent.putExtra("type", 3);
			// sendBroadcast(intent);
			break;
		default:
			break;
		}
	}

	void airReadconfigTwice() {
		Toast.makeText(getApplicationContext(), getString(R.string.air_setting_reading), Toast.LENGTH_SHORT).show();
		airReadconfig();
		/*
		 * new Handler().postDelayed(new Runnable() {
		 * 
		 * @Override public void run() { if (bNotRead) { //
		 * Toast.makeText(AirPreferenceActivity.this, // "on packet good!",
		 * Toast.LENGTH_SHORT).show(); airReadconfig(); } } }, 4000);
		 */
	}

	void airReadconfig() {
		Intent intent = new Intent();
		intent.setAction(BluetoothLeService.ACTION_BLE_CONFIG_CMD);
		intent.putExtra("type", 0);
		intent.putExtra("param", new int[] { 0 });
		sendBroadcast(intent);
	}

	void airSetVibrate(boolean bb) {
		Intent intent = new Intent();
		intent.setAction(BluetoothLeService.ACTION_BLE_CONFIG_CMD);
		intent.putExtra("type", 5);
		intent.putExtra("param", new int[] { 0 });
		intent.putExtra("en", bb);
		sendBroadcast(intent);

	}

	void airSetAlarm(int[] args) {
		Intent intent = new Intent();
		intent.setAction(BluetoothLeService.ACTION_BLE_CONFIG_CMD);
		intent.putExtra("type", 7);
		intent.putExtra("param", args);
		intent.putExtra("w1", sharePre.getString("week1", "0-1-1-1-1-1-0"));
		intent.putExtra("w2", sharePre.getString("week2", "0-1-1-1-1-1-0"));
		sendBroadcast(intent);

	}

	void airSetAntiLost(int[] args) {
		Intent intent = new Intent();
		intent.setAction(BluetoothLeService.ACTION_BLE_CONFIG_CMD);
		intent.putExtra("type", 88);
		intent.putExtra("param", args);
		sendBroadcast(intent);

	}

	void airSetName(String name) {
		Intent intent = new Intent();
		intent.setAction(BluetoothLeService.ACTION_BLE_CONFIG_CMD);
		intent.putExtra("type", 20);
		intent.putExtra("param", new int[] { 0 });
		intent.putExtra("name", name);
		sendBroadcast(intent);
	}

	void airSetShutdown() {
		Intent intent = new Intent();
		intent.setAction(BluetoothLeService.ACTION_BLE_CONFIG_CMD);
		intent.putExtra("type", 99);
		intent.putExtra("param", new int[] { 0 });
		sendBroadcast(intent);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		// TODO Auto-generated method stub
		if (key.equalsIgnoreCase("air_report_rssi") || key.equalsIgnoreCase("set_lost_distance")
				|| key.equalsIgnoreCase("set_isremind") || key.equalsIgnoreCase("set_isremind_call")
				|| key.equalsIgnoreCase("air_delay") || key.equalsIgnoreCase("set_isremind_msg")) {
			Intent intent = new Intent();
			intent.setAction(BluetoothLeService.ACTION_PREF_CHANGED);
			sendBroadcast(intent);
		}
	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub

	}
}
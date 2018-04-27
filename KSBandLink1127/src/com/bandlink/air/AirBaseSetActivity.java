package com.bandlink.air;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bandlink.air.ble.BluetoothLeService;
import com.bandlink.air.bluetooth.protocol.Parser;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.FileDownUtils;
import com.bandlink.air.util.FileDownUtils.OnHexDownloadListener;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.NotificationUtils;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Util;
import com.bandlink.air.view.BadgeView;

public class AirBaseSetActivity extends LovefitActivity implements
		OnClickListener, OnCheckedChangeListener {
	private CheckBox mode1, mode2, mode3;
	private Button set_checkupdate, btn_update, btn_turnoff;
	private ProgressDialog progressDialog;
	private TextView update_anyway, htext, hh, cur_version, tv_device_status,
			tv_device_rssi, tv_device_battery;
	private SharedPreferences sharePre, appShare;
	private String currentVision, newVision, newVersionText;
	private int device_status = 0;
	// 即时RSSI
	private int device_rssi_c = 0;
	// 平均RSSI
	private int device_rssi_a = 0;
	private int device_battery = 0;
	int hh_counter = 0;
	private Context con;
	private RelativeLayout linear_connection_status, mode1_border,
			mode2_border, mode3_border;
	// 0是一般模式开计步睡眠 1是通知模式开来电通知，短信通知 2是高级 都开，呼叫主人 防丢
	private int AirMode = 0;
	ActionbarSettings actionBar;
	int switch_debug_c = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.set_air_base);
		findViewById(R.id.img_device).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				switch_debug_c++;
				if (switch_debug_c >= 10) {
					boolean isdebug = sharePre.getBoolean("isdebug", false);
					isdebug = !isdebug;
					sharePre.edit().putBoolean("isdebug", isdebug).commit();
					if (isdebug) {
						Toast.makeText(AirBaseSetActivity.this,
								"已开启Air调试模式，请至首页，下拉刷新一次", Toast.LENGTH_LONG)
								.show();
					} else {
						Toast.makeText(AirBaseSetActivity.this,
								"已关闭Air调试模式，请至首页，下拉刷新一次", Toast.LENGTH_LONG)
								.show();
					}
					switch_debug_c = 0;
				}
			}
		});
		appShare = getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		sharePre = getSharedPreferences("air", MODE_MULTI_PROCESS);

		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothLeService.ACTION_AIR_RSSI_STATUS);
		filter.addAction(BluetoothLeService.ACTION_BLE_CONFIG_READ_RESPONSE);

		sharePre.registerOnSharedPreferenceChangeListener(perListener);
		actionBar = new ActionbarSettings(this, new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				AirBaseSetActivity.this.finish();
			}
		}, new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(AirBaseSetActivity.this,
						AirHelp.class);
				startActivity(intent);
			}
		});
		actionBar.setTopLeftIcon(R.drawable.ic_top_arrow);
		actionBar.setTopRightIcon(R.drawable.help);
		actionBar.setTitle(R.string.baseinfo);
		actionBar.setRightVisible(false);
		registerReceiver(airReceiver, filter);
		con = Util.getThemeContext(this);

	}

	OnSharedPreferenceChangeListener perListener = new OnSharedPreferenceChangeListener() {

		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			// TODO Auto-generated method stub
			if (key.equals("airmode")) {
				// Toast.makeText(getApplicationContext(),
				// ""+sharedPreferences.getInt(key, 000),
				// Toast.LENGTH_LONG).show();
				Intent intent = new Intent();
				intent.putExtra("mode", sharedPreferences.getInt(key, 1));
				intent.setAction(BluetoothLeService.ACTION_PREF_CHANGED);
				sendBroadcast(intent);
			}
		}
	};
	private String tempVersion = null;
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
						tv_device_status
								.setText(getString(R.string.device_status_off));

					} else {

						tv_device_status
								.setText(getString(R.string.device_status_on));

					}
				}

				if (tv_device_rssi != null) {
					if (device_status == 0) {
						tv_device_rssi.setText("-/-");
					} else {
						tv_device_rssi.setText(device_rssi_c + "/"
								+ device_rssi_a);
					}
				}

				if (tv_device_battery != null) {
					if (device_status == 0) {
						tv_device_battery.setText("--%");

					} else {
						tv_device_battery.setText(device_battery + "%");
					}
				}
				tv_device_status.setEnabled(true);
				findViewById(R.id.progress2).setVisibility(View.GONE);
			}

			if (action
					.equals(BluetoothLeService.ACTION_BLE_CONFIG_READ_RESPONSE)) {

				final int type = intent.getIntExtra("type", -1);
				final byte[] ba;
				switch (type) {
				case 22:
					final String ver = intent.getStringExtra("value");
					if (cur_version != null) {
						tempVersion = ver;
						cur_version.setText(ver);
						System.out.println(ver);
						checkhandler.obtainMessage(2).sendToTarget();
					}
					break;

				default:
					break;
				}
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

			}
		}

	};

	void airSetAntiLost(int[] args) {
		Intent intent = new Intent();
		intent.setAction(BluetoothLeService.ACTION_BLE_CONFIG_CMD);
		intent.putExtra("type", 88);
		intent.putExtra("param", args);
		sendBroadcast(intent);

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		initViews();
		if (sharePre.getString("soft_version", null) == null) {
			airReadconfig();
		} else {
			tempVersion = sharePre.getString("soft_version", "");
			cur_version.setText(sharePre.getString("soft_version", ""));
			System.out.println(sharePre.getString("soft_version", ""));
			checkhandler.obtainMessage(2).sendToTarget();
		}

		if (set_checkupdate != null && hh != null) {
			set_checkupdate.setVisibility(View.GONE);
			htext.setVisibility(View.GONE);
			btn_update.setVisibility(View.GONE);
			hh.setVisibility(View.GONE);
			update_anyway.setVisibility(View.GONE);
			sharePre = getSharedPreferences("air", MODE_MULTI_PROCESS);
			cur_version.setText(sharePre.getString("soft_version",
					sharePre.getString("soft_version", "")));

		}
		device_status = 0;
		if (getIntent().getBooleanExtra("connect", false)) {
			tv_device_status.setText(getString(R.string.device_status_on));
			findViewById(R.id.progress2).setVisibility(View.GONE);
		} else {
			tv_device_status.setText(getString(R.string.device_status_off));
			findViewById(R.id.progress2).setVisibility(View.VISIBLE);
		}
		tv_device_rssi.setText("-/-");
		tv_device_battery.setText("--%");
		int deviceImg = R.drawable.setting_air, deviceName = R.string.setting_device_lovefitAir;
		boolean b2 = appShare.getBoolean("airupdate", false);
		ImageView ksimage = (ImageView) findViewById(R.id.img_device);
		TextView proname = (TextView) findViewById(R.id.proname);
		SlideMainActivity.connectName = appShare.getString("connectName", "");
		if (SlideMainActivity.connectName.toUpperCase().contains("AIRIII")) {
			if (appShare.getBoolean("isKs", false)) {
				deviceName = R.string.setting_device_lovefitKs;
				deviceImg = R.drawable.kuaishua;
			} else {
				deviceName = R.string.setting_device_air3;
				deviceImg = R.drawable.air3;
			}
		} else if (SlideMainActivity.connectName.toUpperCase()
				.contains("AIRII")) {
			deviceName = R.string.setting_device_air2;
			deviceImg = R.drawable.setting_air;
		} else if (SlideMainActivity.connectName.toUpperCase().contains(
				"COOLBAND")) {
			deviceName = R.string.setting_device_coolband;
			deviceImg = R.drawable.coolband;
		} else if (SlideMainActivity.connectName.toUpperCase()
				.contains("AIRIV")) {
			deviceName = R.string.setting_device_air4;
			deviceImg = R.drawable.air3;
		} else if (SlideMainActivity.connectName.toUpperCase().contains(
				"AIRMSG")) {
			deviceName = R.string.setting_device_airmsg;
			deviceImg = R.drawable.air3;
		} else if (SlideMainActivity.connectName.toUpperCase().contains("AIR")) {
			deviceName = R.string.setting_device_lovefitAir;
			deviceImg = R.drawable.setting_air;
		} else {
			{
				deviceName = R.string.setting_device_lovefitAir;
				deviceImg = R.drawable.setting_air;
			}
		}
		ksimage.setImageResource(deviceImg);
		proname.setText(deviceName);
		int count = 0;
		Dbutils db = new Dbutils(appShare.getInt("UID", -1), this);
		if (b2 && db.getUserDeivceType() == 5) {
			count = 1;
			BadgeView b = new BadgeView(this);
			b.setTargetView(set_checkupdate);
			b.setBadgeGravity(Gravity.TOP | Gravity.RIGHT);
			set_checkupdate.setTag(b);
			b.setBadgeCount(count);
		} else {
			Object o = set_checkupdate.getTag();
			if (o != null && o instanceof BadgeView) {
				((BadgeView) o).setBadgeCount(0);
			}
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		boolean on = sharePre.getBoolean("set_isantilost", false);
		airSetAntiLost(new int[] { on ? 1 : 0 });
		super.onPause();
	}

	RelativeLayout toogle_liftwrist_border, airlanguge_border;
	ToggleButton toogle_liftwrist;
	TextView airlanguge;

	private void initViews() {
		toogle_liftwrist_border = (RelativeLayout) findViewById(R.id.toogle_liftwrist_border);
		airlanguge_border = (RelativeLayout) findViewById(R.id.airlanguge_border);
		toogle_liftwrist = (ToggleButton) findViewById(R.id.toogle_liftwrist);
		airlanguge = (TextView) findViewById(R.id.airlanguge);
		toogle_liftwrist_border.setOnClickListener(this);
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
		mode1 = (CheckBox) findViewById(R.id.mode1);
		mode2 = (CheckBox) findViewById(R.id.mode2);
		mode3 = (CheckBox) findViewById(R.id.mode3);
		mode1.setOnClickListener(this);
		mode2.setOnClickListener(this);
		mode3.setOnClickListener(this);
		set_checkupdate = (Button) findViewById(R.id.set_checkupdate);
		set_checkupdate.setOnClickListener(this);
		update_anyway = (TextView) findViewById(R.id.update_anyway);
		update_anyway.setVisibility(View.GONE);
		update_anyway.setOnClickListener(this);
		btn_update = (Button) findViewById(R.id.set_prefe_btn_update);
		btn_update.setEnabled(false);
		btn_update.setOnClickListener(this);
		hh = (TextView) findViewById(R.id.hh);
		htext = (TextView) findViewById(R.id.htext);
		cur_version = (TextView) findViewById(R.id.cur_version);
		cur_version.setVisibility(View.GONE);
		tv_device_status = (TextView) findViewById(R.id.tv_device_status);
		tv_device_rssi = (TextView) findViewById(R.id.tv_device_rssi);
		tv_device_battery = (TextView) findViewById(R.id.tv_device_battery);
		linear_connection_status = (RelativeLayout) findViewById(R.id.linear_connection_status);
		linear_connection_status.setOnClickListener(this);
		btn_turnoff = (Button) findViewById(R.id.set_prefe_btn_turnoff);
		btn_turnoff.setOnClickListener(this);
		mode1_border = (RelativeLayout) findViewById(R.id.mode1_border);
		mode2_border = (RelativeLayout) findViewById(R.id.mode2_border);
		mode3_border = (RelativeLayout) findViewById(R.id.mode3_border);
		mode1_border.setOnClickListener(this);
		mode2_border.setOnClickListener(this);
		mode3_border.setOnClickListener(this);
		switch (sharePre.getInt("airmode", 1)) {
		case 0:
			mode1.setChecked(true);
			mode2.setChecked(false);
			mode3.setChecked(false);
			break;
		case 1:
			mode1.setChecked(false);
			mode2.setChecked(true);
			mode3.setChecked(false);
			break;
		case 2:
			mode1.setChecked(false);
			mode2.setChecked(false);
			mode3.setChecked(true);
			break;
		}
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
					findViewById(R.id.progress).setVisibility(View.GONE);
					currentVision = sharePre.getString("soft_version",
							sharePre.getString("soft_version", ""));
					// currentVision = "agaaag";
					currentVision = (String) cur_version.getText();

					if (currentVision.toUpperCase().contains("AIRIII")) {
						newVision = ((JSONObject) (msg.obj))
								.optString("verNameIII");
						newVersionText = ((JSONObject) (msg.obj)).optString(
								"textIII", "");
					} else if (currentVision.toUpperCase().contains("AIRII")) {
						newVision = ((JSONObject) (msg.obj))
								.optString("verNameII");
						newVersionText = ((JSONObject) (msg.obj)).optString(
								"textII", "");
					} else if (currentVision.toUpperCase().contains("AIR")) {
						newVision = ((JSONObject) (msg.obj))
								.optString("verName");
						newVersionText = ((JSONObject) (msg.obj)).optString(
								"text", "");
					}
					if (HttpUtlis.checkAir(currentVision, newVision) != 1) {
						hh.setVisibility(View.VISIBLE);
						hh.setText(R.string.move_new_version);
						set_checkupdate.setVisibility(View.GONE);
						appShare.edit().putBoolean("airupdate", false).commit();
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
			case 2:
				cur_version.setVisibility(View.VISIBLE);
				set_checkupdate.setVisibility(View.VISIBLE);
				getLastestVersion();
				break;
			default:
				break;
			}
		}
	};

	public void getLastestVersion() {
		// progressDialog = initProgressDialog();
		findViewById(R.id.progress).setVisibility(View.VISIBLE);
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				String result = HttpUtlis
						.queryStringForPost(HttpUtlis.UPDATE_AIR);

				try {
					result = new String(result.getBytes("iso8859-1"), "utf-8");
					JSONObject js = new JSONObject(result);
					checkhandler.obtainMessage(1, js).sendToTarget();
				} catch (Exception e) {
					e.printStackTrace();
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							findViewById(R.id.progress)
									.setVisibility(View.GONE);
							if (progressDialog != null
									&& progressDialog.isShowing()) {
								progressDialog.dismiss();
							}
						}
					});
				}
			}
		}).start();
	}

	private void ShowDownProgress(String filename) {
		Intent intent = new Intent(this, FileDownDialog.class);
		intent.putExtra("filename", filename);
		startActivity(intent);
	}

	private void checkAirVersion(String newVision) {
		// TODO Auto-generated method stub
		if (newVision == null || newVision.length() == 0) {
			Toast.makeText(this, getString(R.string.deleteerror),
					Toast.LENGTH_SHORT).show();
		}
		String filepath = AirBaseSetActivity.this.getFilesDir().toString()
				+ "/air";
		FileDownUtils fileDownUtils = new FileDownUtils(
				new OnHexDownloadListener() {

					@Override
					public void OnHexProgress(float pre) {
						// TODO Auto-generated method stub
					}

					@Override
					public void OnHexDownloaded(String name) {
						// TODO Auto-generated method stub
						if (name != null) {
							Intent intent = new Intent(
									BluetoothLeService.ACTION_AIR_CONTROL);
							intent.putExtra("task", 4);
							intent.putExtra("filename", "Air.hex");
							intent.putExtra("version", name);
							AirBaseSetActivity.this.sendBroadcast(intent);
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

		appShare.edit().putString("lastest_air", newVision).commit();
	}

	ProgressDialog dialog;

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.airlanguge_border:
			if (device_status != 0) {
				AlertDialog.Builder aba = new AlertDialog.Builder(con);
				aba.setTitle(R.string.air_language);
				final String[] str = new String[] {
						getString(R.string.language_zh),
						getString(R.string.language_en),
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
				Toast.makeText(getApplicationContext(),
						getString(R.string.set_antilost_disconnect),
						Toast.LENGTH_SHORT).show();
			}

			break;
		case R.id.toogle_liftwrist_border:
			onCheckedChanged(toogle_liftwrist, !toogle_liftwrist.isChecked());
			break;
		case R.id.set_prefe_btn_update: // update
			// 打开新的窗口来升级
			// filenameString = "Air v2.1.5.hex";
			// ShowDownProgress(filenameString);
			// ShowDownProgress(newVision + ".hex");
			dialog = Util.initProgressDialog(Util.getThemeContext(this), true,
					getString(R.string.data_wait), null);
			checkAirVersion(newVision);
			break;
		case R.id.set_checkupdate:
			getLastestVersion();
			break;
		case R.id.update_anyway:
			if (device_status == 1) {
				// if (!sharePre.getString("soft_version",
				// "").contains("2.2.7")) {
				// if (newVision != null && newVision.length() > 0)
				// ShowDownProgress(newVision + ".hex");
				// } else {
				// Toast.makeText(this, getString(R.string.not_release),
				// Toast.LENGTH_SHORT).show();
				// }
				if (HttpUtlis.checkAir(currentVision, newVision) >= 0) {
					dialog = Util.initProgressDialog(
							Util.getThemeContext(this), true,
							getString(R.string.data_wait), null);
					checkAirVersion(newVision);
				} else {
					Toast.makeText(this, getString(R.string.no_hex_enable),
							Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(this, getString(R.string.device_off),
						Toast.LENGTH_SHORT).show();
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
		case R.id.set_prefe_btn_turnoff: // turn off device
			AlertDialog.Builder ab = new AlertDialog.Builder(con);
			ab.setTitle(R.string.turnoff);
			ab.setMessage(R.string.turnoffmsg);
			ab.setNegativeButton(R.string.ok,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							airSetShutdown();
						}
					});
			ab.setPositiveButton(R.string.can,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							dialog.dismiss();
						}
					});
			ab.create().show();
			break;
		case R.id.linear_connection_status:
			if (device_status != 0) {
				tv_device_status.setText(getString(R.string.device_status_on));
				break;
			}
			airManualConnect(1 - device_status);
			tv_device_status.setEnabled(false);
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					runOnUiThread(new Runnable() {
						public void run() {
							tv_device_status.setEnabled(true);
						}
					});
				}
			}, 10 * 1000);
			// if (device_status == 1)
			// device_status = 0;
			// if (tv_device_status != null) {
			// if (device_status == 0) {
			// tv_device_status
			// .setText(getString(R.string.device_status_off));
			// tv_device_rssi.setText("-/-");
			// }
			// }

			break;
		case R.id.mode1_border:
			currentVision = sharePre.getString("soft_version",
					sharePre.getString("soft_version", ""));
			Intent mIntent1 = new Intent(AirBaseSetActivity.this,
					AirPreferenceActivity.class);
			mIntent1.putExtra("mode", 0);
			mIntent1.putExtra("version", tempVersion == null ? currentVision
					: tempVersion);
			startActivity(mIntent1);
			break;
		case R.id.mode2_border:
			currentVision = sharePre.getString("soft_version",
					sharePre.getString("soft_version", ""));
			Intent mIntent2 = new Intent(AirBaseSetActivity.this,
					AirPreferenceActivity.class);
			mIntent2.putExtra("mode", 1);
			mIntent2.putExtra("version", tempVersion == null ? currentVision
					: tempVersion);
			startActivity(mIntent2);
			break;
		case R.id.mode3_border:
			currentVision = sharePre.getString("soft_version",
					sharePre.getString("soft_version", ""));
			Intent mIntent = new Intent(AirBaseSetActivity.this,
					AirPreferenceActivity.class);
			mIntent.putExtra("mode", 2);
			mIntent.putExtra("version", tempVersion == null ? currentVision
					: tempVersion);
			startActivity(mIntent);
			break;
		case R.id.mode1:

			if (device_status == 0) {
				mode1.setChecked(false);
				Toast.makeText(getApplicationContext(),
						getString(R.string.set_antilost_disconnect),
						Toast.LENGTH_LONG).show();
				break;
			}

			cancleNotification();
			AirMode = 0;
			sharePre.edit().putInt("airmode", 0).commit();
			switchMode(0);
			mode1.setChecked(true);
			mode2.setChecked(false);
			mode3.setChecked(false);

			break;
		case R.id.mode2:

			if (device_status == 0) {
				mode2.setChecked(false);
				Toast.makeText(getApplicationContext(),
						getString(R.string.set_antilost_disconnect),
						Toast.LENGTH_LONG).show();
				break;
			}
			cancleNotification();
			AirMode = 1;
			switchMode(1);
			sharePre.edit().putInt("airmode", 1).commit();
			mode2.setChecked(true);
			mode1.setChecked(false);
			mode3.setChecked(false);
			break;
		case R.id.mode3:

			if (device_status == 0) {
				mode3.setChecked(false);
				Toast.makeText(getApplicationContext(),
						getString(R.string.set_antilost_disconnect),
						Toast.LENGTH_LONG).show();
				break;
			}
			cancleNotification();

			AirMode = 2;
			switchMode(2);
			sharePre.edit().putInt("airmode", 2).commit();
			mode3.setChecked(true);
			mode1.setChecked(false);
			mode2.setChecked(false);

			break;

		}

	}

	void airSetVibrate(boolean bb) {
		Intent intent = new Intent();
		intent.setAction(BluetoothLeService.ACTION_BLE_CONFIG_CMD);
		intent.putExtra("type", 5);
		intent.putExtra("param", new int[] { 0 });
		intent.putExtra("en", bb);
		sendBroadcast(intent);

	}

	void cancleNotification() {
		nm.cancel(NotificationUtils.FoundNotfication);
		nm.cancel(NotificationUtils.BlueNotfication);
		nm.cancel(NotificationUtils.LostNotfication);
	}

	/***
	 * 
	 * @param m
	 *            模式 0是普通 1是通知 2是高级
	 */
	void switchMode(int m) {
		boolean b = m == 2 ? true : false;
		Editor editor = sharePre.edit();
		editor.putBoolean("air_open_bt", b)
				.putBoolean("air_auto_connect", m == 1 ? true : b)
				.putBoolean("set_isantilost", b)
				.putBoolean("set_isremind", m == 1 ? true : b)
				.putBoolean("set_isremind_call", m == 1 ? true : b)
				.putBoolean("set_isremind_msg", m == 1 ? true : b)
				.putBoolean("set_find_phone", m == 1 ? true : b).commit();
		if (m == 2) {
			airSetAntiLost(new int[] { 1 });
		} else {
			airSetAntiLost(new int[] { 0 });
		}
		// if (m != 0) {
		// airSetVibrate(true);
		// } else {
		// airSetVibrate(false);
		// }
	}

	NotificationManager nm;

	void airReadconfig() {
		Intent intent = new Intent();
		intent.setAction(BluetoothLeService.ACTION_BLE_CONFIG_CMD);
		intent.putExtra("type", 0);
		intent.putExtra("param", new int[] { 0 });
		sendBroadcast(intent);
	}

	// added by mshen
	// type = 0 to disconnect, type=1 to connect if not connected yet
	void airManualConnect(int type) {
		Intent intent;
		switch (type) {
		case 0:
			Toast.makeText(getApplicationContext(),
					getString(R.string.air_setting_manual_disconnect),
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
				Toast.makeText(getApplicationContext(),
						getString(R.string.no_air_address), Toast.LENGTH_SHORT)
						.show();
			} else {
				Toast.makeText(getApplicationContext(),
						getString(R.string.air_setting_manual_connect),
						Toast.LENGTH_SHORT).show();
				intent = new Intent(this, BluetoothLeService.class);
				intent.putExtra("address", address);
				// 1 普通模式 2是固件升级
				intent.putExtra("command", 1);
				// 重新扫描连接
				intent.putExtra("scanflag", 4);
				startService(intent);
			}

			break;
		default:
			break;
		}
	}

	void airSetShutdown() {
		Intent intent = new Intent();
		intent.setAction(BluetoothLeService.ACTION_BLE_CONFIG_CMD);
		intent.putExtra("type", 99);
		intent.putExtra("param", new int[] { 0 });
		sendBroadcast(intent);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		unregisterReceiver(airReceiver);
		super.onDestroy();
	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		switch (buttonView.getId()) {
		case R.id.toogle_liftwrist:
			if (device_status != 0) {
				buttonView.setChecked(isChecked);
				sharePre.edit().putInt("liftwrist", isChecked ? 1 : 0).commit();
				commitLiftWrist();
			} else {
				buttonView.setChecked(!isChecked);
				Toast.makeText(getApplicationContext(),
						getString(R.string.set_antilost_disconnect),
						Toast.LENGTH_SHORT).show();
			}

			break;
		}

	}

	void commitLiftWrist() {
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
		intent.putExtra("param",
				new int[] { 1, lan2, sharePre.getInt("liftwrist", 1) });
		sendBroadcast(intent);
	}
}
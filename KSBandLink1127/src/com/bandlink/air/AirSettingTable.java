package com.bandlink.air;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Cap;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bandlink.air.ble.BluetoothLeService;
import com.bandlink.air.bluetooth.protocol.Parser;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.FileDownUtils;
import com.bandlink.air.util.FileDownUtils.OnHexDownloadListener;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Util;
import com.bandlink.air.view.NewSeekBar;

public class AirSettingTable extends LovefitActivity implements
		OnClickListener, OnLongClickListener {

	private TextView name_batt, name_rssi, pro_name, version, checkupdate,
			close, connectStatus;
	private int width;
	private SharedPreferences sharePre, appShare;
	private int currentItem = -1;
	private int curAlarm1, curAlarm2, curLost, curHand, curLan, curWechat,
			curApp;
	private boolean bool_find_phone, bool_sms, bool_call, bool_connect,
			bool_openbt;
	private ActionbarSettings actionBar;
	private GridLayout gridlayout;
	private ImageView icon;
	private int curBar = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setContentView(R.layout.activity_table_set);
		setTheme(R.style.AppTheme);
		IntentFilter filter = new IntentFilter();
		actionBar = new ActionbarSettings(this, new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				AirSettingTable.this.finish();
			}
		}, null);
		actionBar.setTopLeftIcon(R.drawable.ic_top_arrow);
		actionBar.setTitle(R.string.setting_preference);
		filter.addAction(BluetoothLeService.ACTION_AIR_RSSI_STATUS);
		filter.addAction(BluetoothLeService.ACTION_BLE_CONFIG_READ_RESPONSE);
		registerReceiver(airReceiver, filter);
		sharePre = getSharedPreferences("air", MODE_MULTI_PROCESS);
		appShare = getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);

		gridlayout = (GridLayout) findViewById(R.id.gridlayout);
		// 布局中padding2.5 * 2
		width = (int) ((getResources().getDisplayMetrics().widthPixels - Util
				.dp2px(this, 5)) / (float) gridlayout.getColumnCount());
		name_rssi = (TextView) findViewById(R.id.rssi);
		icon = (ImageView) findViewById(R.id.icon);
		name_batt = (TextView) findViewById(R.id.battery);
		pro_name = (TextView) findViewById(R.id.pro_name);
		version = (TextView) findViewById(R.id.version);
		checkupdate = (TextView) findViewById(R.id.check);
		connectStatus = (TextView) findViewById(R.id.connect);
		findViewById(R.id.checkupdatenow).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						// 检测更新
						checkByUser = true;
						getLastestVersion();
					}
				});
		findViewById(R.id.temp2).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(AirSettingTable.this, AirHelp.class));
			}
		});
		close = (TextView) findViewById(R.id.close);
		findViewById(R.id.turnoffnow).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showOff();
			}
		});
		initPanel(gridlayout);

		super.onCreate(savedInstanceState);
	}

	public Bitmap getCroppedBitmap(Context ContextRef, float progress,
			boolean connect) {

		Matrix m = new Matrix();

		int w = (int) (ContextRef.getResources()
				.getDimensionPixelSize(R.dimen.notification_step));
		int h = w;
		Bitmap output = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG));
		final Paint paint = new Paint();
		paint.setTextSize(ContextRef.getResources().getDimensionPixelSize(
				R.dimen.text_small));
		paint.setStrokeWidth(1);
		paint.setColor(this.getResources().getColor(R.color.text_dark));
		paint.setStrokeCap(Cap.ROUND);
		paint.setStyle(Paint.Style.STROKE);
		paint.setAntiAlias(true);
		paint.setTextAlign(Align.CENTER);
		// paint.setFakeBoldText(true);
		Paint paint2 = new Paint();
		paint.setColor(currentColor);
		canvas.drawArc(
				new RectF(w * (0.3f), h * (0.3f), w * (0.7f), h * (0.7f)),
				-90f, 360, false, paint);
		paint.setStrokeWidth(3);
		if (connect) {
			paint.setColor(currentColor);
			paint2.setColor(currentColor);
		} else {
			paint.setColor(Color.parseColor("#ff6000"));
			paint2.setColor(Color.parseColor("#ff6000"));
		}

		canvas.drawArc(
				new RectF(w * (0.3f), h * (0.3f), w * (0.7f), h * (0.7f)),
				-90f, 360 * progress, false, paint);
		paint.setStrokeCap(Cap.SQUARE);

		paint2.setAntiAlias(true);
		paint2.setTypeface(MilinkApplication.NumberFace);

		paint2.setStyle(Paint.Style.FILL);
		paint2.setTextSize(24);
		float f = paint2.measureText(Math.round(progress * 100) + "");
		canvas.drawText(Math.round(progress * 100) + "", (w * 0.45f)
				- (f * 0.45f), h * 0.55f, paint2);
		paint2.setTextSize(18);
		canvas.drawText("%", (w * 0.5f) + (f * 0.45f), h * 0.55f, paint2);
		return Bitmap.createBitmap(output, 0, 0, output.getWidth(),
				output.getHeight(), m, true);
	}

	private void initPanel(GridLayout gridlayout) {
		gridlayout.removeAllViews();
		// addItemView(16);
		// addItemView(17);
		for (int i = 1; i < 11; i++) {
			addItemView(i);
		}

		// addItemView(18);
		// itemName[18].setText(R.string.turnoff);
		// itemIcon[18].setImageResource(R.drawable.device_off);
		if (sharePre.getBoolean("set_isremind_wechat", true)) {
			curWechat = 1;
		} else {
			curWechat = 0;
		}
		if (sharePre.getBoolean("app_notification", true)) {
			curApp = 1;
		} else {
			curApp = 0;
		}
		// ///////////////////////////初始化数据
		itemName[1].setText(String.format(getString(R.string.alarm_set),
				SlideMainActivity.alarm1Value));
		itemName[2].setText(String.format(getString(R.string.alarm_set),
				SlideMainActivity.alarm2Value));
		if (SlideMainActivity.device_status == 1) {
			connectStatus.setText(R.string.device_status_on);
		} else {
			connectStatus.setText(R.string.device_status_off);
		}
		// itemName[16].setText(getString(R.string.batt_set));
		if (SlideMainActivity.device_battery != curBar) {
			curBar = SlideMainActivity.device_battery;
			// itemIcon[16].setImageBitmap(getCroppedBitmap(this, curBar / 100f,
			// (curBar / 100f) < 0.15f ? false : true));
			name_batt.setText(curBar + "%");
		}
		if (SlideMainActivity.alarm1) {
			itemIcon[1].setImageResource(R.drawable.alarm_on);
			curAlarm1 = 1;
		} else {
			itemIcon[1].setImageResource(R.drawable.alarm_off);
			curAlarm1 = 0;
		}

		if (SlideMainActivity.alarm2) {
			itemIcon[2].setImageResource(R.drawable.alarm_on);
			curAlarm2 = 1;
		} else {
			itemIcon[2].setImageResource(R.drawable.alarm_off);
			curAlarm2 = 0;
		}
		if (SlideMainActivity.device_status == 0) {
			// icon_rssi.setImageResource(R.drawable.rssi_off);
			// itemName[17].setText(String.format(getString(R.string.rssi_set),
			// "-/-"));
			name_rssi.setText("-/-");
		} else {
			// icon_rssi.setImageResource(R.drawable.rssi_on);
			// itemName[17].setText(String.format(getString(R.string.rssi_set),
			// SlideMainActivity.device_rssi_c + "/"
			// + SlideMainActivity.device_rssi_a));
			name_rssi.setText(SlideMainActivity.device_rssi_c + "/"
					+ SlideMainActivity.device_rssi_a);
		}
		// ///////////初始化语言
		if (SlideMainActivity.setting != null) {
			// 根据SlideMainActivity的数据初始化界面配置，也就是从手环读取的。
			initSetIntime();
			needReLoad = false;
		} else {
			// 进入页面时并未读到，加载本地存的配置
			needReLoad = true;

		}

		// 呼叫手机在本地配置
		itemName[5].setText(R.string.divice_preference_call);
		bool_find_phone = sharePre.getBoolean("set_find_phone", true);
		if (bool_find_phone) {
			itemIcon[5].setImageResource(R.drawable.phone_on);
		} else {
			itemIcon[5].setImageResource(R.drawable.phone_off);
		}

		// 来电提醒在本地

		itemName[10].setText(R.string.divice_preference_reconnect);
		bool_connect = sharePre.getBoolean("air_auto_connect", true);
		if (bool_connect) {
			itemIcon[10].setImageResource(R.drawable.connect_on);
		} else {
			itemIcon[10].setImageResource(R.drawable.connect_off);
		}
		itemName[7].setText(R.string.divice_preference_callremind);
		bool_call = sharePre.getBoolean("set_isremind_call", true);
		if (bool_call) {
			itemIcon[7].setImageResource(R.drawable.call_on);
		} else {
			itemIcon[7].setImageResource(R.drawable.call_off);
		}

		itemName[8].setText(R.string.divice_preference_msgremind);
		bool_sms = sharePre.getBoolean("set_isremind_msg", true);
		if (bool_sms) {
			itemIcon[8].setImageResource(R.drawable.sms_on);
		} else {
			itemIcon[8].setImageResource(R.drawable.sms_off);
		}

		itemName[9].setText(R.string.ble_open);
		bool_openbt = sharePre.getBoolean("air_open_bt", false);

		if (bool_openbt) {
			itemIcon[9].setImageResource(R.drawable.bt_on);
		} else {
			itemIcon[9].setImageResource(R.drawable.bt_off);
		}
		// ///////////
		tempWeek1 = sharePre.getString("week1", "0-1-1-1-1-1-0");
		tempWeek2 = sharePre.getString("week2", "0-1-1-1-1-1-0");

	}

	private void initSetIntime() {
		// TODO Auto-generated method stub
		version.setText(String.format(getString(R.string.ver_set),
				SlideMainActivity.currentVersion));

		// itemIcon[11].setImageResource(R.drawable.hardware_on);
		itemIcon[4].setImageResource(R.drawable.lan_on);
		if (2 == (sharePre.getInt("lan", 1))) {
			itemName[4].setText(R.string.language_auto);
			curLan = 2;
		} else if ("0".equals(SlideMainActivity.setting.get("lan").toString())) {
			itemName[4].setText(R.string.language_en);
			curLan = 0;
		} else if ("1".equals(SlideMainActivity.setting.get("lan").toString())) {
			itemName[4].setText(R.string.language_zh);
			curLan = 1;
		}
		itemName[3].setText(R.string.handup);
		if ("0".equals(SlideMainActivity.setting.get("hand").toString())) {
			// 抬手显示关
			itemIcon[3].setImageResource(R.drawable.hand_off);
			curHand = 0;
		} else {
			itemIcon[3].setImageResource(R.drawable.hand_on);
			curHand = 1;
		}
		itemName[6].setText(R.string.divice_preference_antilost);
		if ("0".equals(SlideMainActivity.setting.get("lost").toString())) {
			itemIcon[6].setImageResource(R.drawable.lost_off);
			curLost = 0;
		} else {
			itemIcon[6].setImageResource(R.drawable.lost_on);
			curLost = 1;
		}
		if (!TextUtils.isEmpty(SlideMainActivity.currentVersion)) {
			if (true
					|| SlideMainActivity.currentVersion.toUpperCase().contains(
							"AIRIII") && appShare.getBoolean("isKs", false)) {
				// 快刷
				pro_name.setText("快刷手环");
				icon.setImageResource(R.drawable.kuaishua);
				addItemView(13);
				itemName[13].setText(R.string.app_notification_manager);
				itemIcon[13].setImageResource(sharePre.getBoolean(
						"app_notification", true) ? R.drawable.app_on
						: R.drawable.app_off);
			} else if (SlideMainActivity.currentVersion.toUpperCase().contains(
					"AIRIII")) {
				// Air messager
				pro_name.setText("Air Messenger");
				icon.setImageResource(R.drawable.kuaishua);
				addItemView(13);
				itemName[13].setText(R.string.app_notification_manager);
			} else if (SlideMainActivity.currentVersion.toUpperCase().contains(
					"AIRII")) {
				// airII
				pro_name.setText("Air II 智能手表");
				addItemView(12);
				itemName[12].setText(R.string.divice_preference_mmremind);
				itemIcon[12].setImageResource(sharePre.getBoolean(
						"set_isremind_wechat", true) ? R.drawable.wechat_on
						: R.drawable.wechat_off);
			} else if (SlideMainActivity.currentVersion.toUpperCase().contains(
					"AIR")) {
				// I
				pro_name.setText("Air 智能手表");

			}
			// check update
			getLastestVersion();
		}
	}

	public void getLastestVersion() {
		if (checkByUser) {
			progress = Util.initProgressDialog(AirSettingTable.this, true,
					getString(R.string.data_wait), null);
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				String result = HttpUtlis
						.queryStringForPost(HttpUtlis.UPDATE_AIR);

				try {
					result = new String(result.getBytes("iso8859-1"), "utf-8");
					JSONObject js = new JSONObject(result);
					handler.obtainMessage(3, js).sendToTarget();
				} catch (Exception e) {
					e.printStackTrace();
					handler.obtainMessage(4).sendToTarget();
				}
			}
		}).start();
	}

	private TextView[] itemName = new TextView[20];
	private ImageView[] itemIcon = new ImageView[20];
	private ImageView[] itemFlag = new ImageView[20];

	void addItemView(int index) {

		View bt = LayoutInflater.from(this).inflate(R.layout.item_table, null);
		LayoutParams lp = new LayoutParams(width, width);
		bt.setLayoutParams(lp);
		itemIcon[index] = (ImageView) bt.findViewById(R.id.icon);
		itemName[index] = (TextView) bt.findViewById(R.id.name);
		itemFlag[index] = (ImageView) bt.findViewById(R.id.flag);
		bt.setOnLongClickListener(this);
		bt.setOnClickListener(this);
		bt.setTag(index);
		gridlayout.addView(bt);

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		airReadconfig(this);
		super.onResume();
	}

	public static void airReadconfig(Context c) {
		Intent intent = new Intent();
		intent.setAction(BluetoothLeService.ACTION_BLE_CONFIG_CMD);
		intent.putExtra("type", 0);
		intent.putExtra("param", new int[] { 0 });
		c.sendBroadcast(intent);
	}

	boolean checkByUser = false;
	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				// 用于更新场强
				if (needReLoad && SlideMainActivity.setting != null) {
					try {
						initSetIntime();
						needReLoad = false;
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				// itemName[16].setText(String.format(
				// getString(R.string.batt_set),
				// SlideMainActivity.device_battery)
				// + "%");
				if (SlideMainActivity.device_battery != curBar) {
					curBar = SlideMainActivity.device_battery;
					// itemIcon[16].setImageBitmap(getCroppedBitmap(
					// AirSettingTable.this, curBar / 100f,
					// (curBar / 100f) < 0.15f ? false : true));
					name_batt.setText(curBar + "%");
				}
				if (SlideMainActivity.device_status == 0) {
					// icon_rssi.setImageResource(R.drawable.rssi_off);
					// itemName[17].setText(String.format(
					// getString(R.string.rssi_set), "-/-"));
					name_rssi.setText(SlideMainActivity.device_rssi_c + "/"
							+ SlideMainActivity.device_rssi_a);
					connectStatus.setText(R.string.device_status_off);
				} else {
					// icon_rssi.setImageResource(R.drawable.rssi_on);
					// itemName[17].setText(String.format(
					// getString(R.string.rssi_set),
					// SlideMainActivity.device_rssi_c + "/"
					// + SlideMainActivity.device_rssi_a));
					name_rssi.setText(SlideMainActivity.device_rssi_c + "/"
							+ SlideMainActivity.device_rssi_a);
					connectStatus.setText(R.string.device_status_on);
				}

				break;
			case 1:
				if (SlideMainActivity.alarm1) {
					itemIcon[1].setImageResource(R.drawable.alarm_on);
				} else {
					itemIcon[1].setImageResource(R.drawable.alarm_off);
				}
				if (SlideMainActivity.alarm2) {
					itemIcon[2].setImageResource(R.drawable.alarm_on);
				} else {
					itemIcon[2].setImageResource(R.drawable.alarm_off);
				}
				itemName[1].setText(String.format(
						getString(R.string.alarm_set),
						SlideMainActivity.alarm1Value));
				itemName[2].setText(String.format(
						getString(R.string.alarm_set),
						SlideMainActivity.alarm2Value));
				break;
			case 2:
				// itemName[16].setText(String.format(
				// getString(R.string.batt_set),
				// SlideMainActivity.device_battery)
				// + "%");
				if (SlideMainActivity.device_battery != curBar) {
					curBar = SlideMainActivity.device_battery;
					// itemIcon[16].setImageBitmap(getCroppedBitmap(
					// AirSettingTable.this, curBar / 100f,
					// (curBar / 100f) < 0.15f ? false : true));
					name_batt.setText(curBar + "%");
				}
				break;
			case 3:
				try {
					if (progress != null && progress.isShowing()) {
						progress.dismiss();
					}
					String newVision=null, newVersionText=null;
					if (SlideMainActivity.currentVersion.toUpperCase()
							.contains("AIRIII")) {
						newVision = ((JSONObject) (msg.obj))
								.optString("verNameIII");
						newVersionText = ((JSONObject) (msg.obj)).optString(
								"textIII", "");
					} else if (SlideMainActivity.currentVersion.toUpperCase()
							.contains("AIRII")) {
						newVision = ((JSONObject) (msg.obj))
								.optString("verNameII");
						newVersionText = ((JSONObject) (msg.obj)).optString(
								"textII", "");
					} else if(SlideMainActivity.currentVersion.toUpperCase()
							.contains("AIR")){
						newVision = ((JSONObject) (msg.obj))
								.optString("verName");
						newVersionText = ((JSONObject) (msg.obj)).optString(
								"text", "");
					}
					if (HttpUtlis.checkAir(SlideMainActivity.currentVersion,
							newVision) != 1) {
						// 已是最新
						if (checkByUser) {
							showAirVersion(newVision, null);
							checkByUser = false;
						}
					} else {
						//
						if (checkByUser) {
							showAirVersion(newVision, newVersionText);
							if (itemFlag[11] != null) {
								itemFlag[11].setVisibility(View.VISIBLE);
							}
							checkByUser = false;
						}
						if (itemFlag[11] != null) {
							itemFlag[11].setImageResource(R.drawable.version);
							itemFlag[11].setVisibility(View.VISIBLE);
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case 4:
				if (progress != null && progress.isShowing()) {
					progress.dismiss();
				}
				break;

			case 5:
				// 配置写入4秒 取消
				if (progress != null && progress.isShowing()) {
					progress.dismiss();
				}
				break;
			}
		};
	};

	void showAirVersion(final String version, String msg) {
		AlertDialog.Builder ab = new AlertDialog.Builder(
				new ContextThemeWrapper(AirSettingTable.this,
						R.style.Theme_AppCompat_Light));
		if (msg != null) {
			ab.setTitle(String.format(getString(R.string.update_set), version));
			ab.setMessage(msg);
			ab.setPositiveButton(R.string.ok,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							checkAirVersion(version);
						}
					});
		} else {
			ab.setTitle(R.string.check_upgrade);
			ab.setMessage(R.string.move_new_version);
		}
		ab.setNegativeButton(R.string.cancel, null);
		ab.create().show();
	}

	private void checkAirVersion(String newVision) {
		// TODO Auto-generated method stub
		if (newVision == null || newVision.length() == 0) {
			Toast.makeText(this, getString(R.string.deleteerror),
					Toast.LENGTH_SHORT).show();
		}
		String filepath = AirSettingTable.this.getFilesDir().toString()
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
							AirSettingTable.this.sendBroadcast(intent);
							handler.obtainMessage(4).sendToTarget();
						}
					}
				}, filepath, Parser.httppath + newVision + ".hex");
		fileDownUtils.start();

		appShare.edit().putString("lastest_air", newVision).commit();
	}

	private boolean needReLoad = false;
	private BroadcastReceiver airReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			// TODO Auto-generated method stub

			final String action = intent.getAction();

			if (action.equals(BluetoothLeService.ACTION_AIR_RSSI_STATUS)) {
				handler.sendEmptyMessage(0);
			}

			if (action
					.equals(BluetoothLeService.ACTION_BLE_CONFIG_READ_RESPONSE)) {

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
					handler.sendEmptyMessage(1);
					break;
				// name
				case 20:
					final String name = intent.getStringExtra("value");

					break;
				// soft_version
				case 22:
					final String ver = intent.getStringExtra("value");

					break;

				default:
					break;
				}
				final int write = intent.getIntExtra("write", -1);
				if (write != -1) {
					handler.removeMessages(5);
				}
				// 写入成功
				switch (write) {
				// alarm
				case 7:
					// 闹钟 1 2
					if (progress != null && progress.isShowing()) {
						progress.dismiss();
					}
					if (currentItem == 1) {
						curAlarm1 = 1 - curAlarm1;
						SlideMainActivity.alarm1 = (curAlarm1 == 1);
						itemIcon[1]
								.setImageResource(curAlarm1 == 1 ? R.drawable.alarm_on
										: R.drawable.alarm_off);
						currentItem = -1;
					} else if (currentItem == 2) {
						curAlarm2 = 1 - curAlarm2;
						itemIcon[2]
								.setImageResource(curAlarm2 == 1 ? R.drawable.alarm_on
										: R.drawable.alarm_off);
						SlideMainActivity.alarm2 = (curAlarm2 == 1);
						currentItem = -1;
					}
					if (tempAlarm1 != null) {
						SlideMainActivity.alarm1Value = tempAlarm1;
						tempAlarm1 = null;
						itemName[1].setText(String.format(
								getString(R.string.alarm_set),
								SlideMainActivity.alarm1Value));
					}
					if (tempAlarm2 != null) {
						SlideMainActivity.alarm2Value = tempAlarm2;
						tempAlarm2 = null;
						itemName[2].setText(String.format(
								getString(R.string.alarm_set),
								SlideMainActivity.alarm2Value));
					}
					sharePre.edit().putString("week1", tempWeek1).commit();
					sharePre.edit().putString("week2", tempWeek1).commit();
					// name.
					break;
				// name
				case 13:
					// 显示
					if (progress != null && progress.isShowing()) {
						progress.dismiss();
					}
					if (currentItem == 3) {
						curHand = 1 - curHand;
						itemIcon[3]
								.setImageResource(curHand == 1 ? R.drawable.hand_on
										: R.drawable.hand_off);

					} else if (currentItem == 4) {
						curLan = 1 - curLan;
					}
					break;
				case 16:
					// 防丢
					if (progress != null && progress.isShowing()) {
						progress.dismiss();
					}
					if (currentItem == 6) {
						curLost = 1 - curLost;
						sharePre.edit()
								.putBoolean("set_isantilost", curLost == 1)
								.commit();
						itemIcon[6]
								.setImageResource(curLost == 1 ? R.drawable.lost_on
										: R.drawable.lost_off);
						currentItem = -1;
					}
					break;
				case 19:
					// 防丢延时
					if (progress != null && progress.isShowing()) {
						progress.dismiss();
					}
					sharePre.edit().putInt("air_delay", delay).commit();
					break;
				default:
					break;
				}

			}
		}

	};

	protected void onDestroy() {
		unregisterReceiver(airReceiver);
		super.onDestroy();
	}

	ProgressDialog progress;

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getTag() != null) {
			if (SlideMainActivity.device_status == 0) {
				Toast.makeText(AirSettingTable.this,
						getString(R.string.device_status_off),
						Toast.LENGTH_SHORT).show();
				return;
			}
			int id = Integer.valueOf(v.getTag().toString());
			currentItem = id;
			switch (id) {
			case 1:
				// 闹钟1
				showProgress(R.string.data_wait);
				commitAlarm(1 - curAlarm1, curAlarm2,
						SlideMainActivity.alarm1Value,
						SlideMainActivity.alarm2Value);
				break;
			case 2:
				// 闹钟1
				showProgress(R.string.data_wait);
				commitAlarm(curAlarm1, 1 - curAlarm2,
						SlideMainActivity.alarm1Value,
						SlideMainActivity.alarm2Value);
				break;
			case 3:
				// 设置抬腕显示
				showProgress(R.string.data_wait);

				setAirDisplay(sharePre.getInt("lan", 1), 1 - curHand);
				break;
			case 4:
				setLan();
				break;
			case 5:
				// 呼叫手机
				sharePre.edit()
						.putBoolean("set_find_phone",
								bool_find_phone = !bool_find_phone).commit();

				itemIcon[5]
						.setImageResource(bool_find_phone ? R.drawable.phone_on
								: R.drawable.phone_off);
				break;
			case 6:

				// 手机防丢
				showProgress(R.string.data_wait);
				airSetAntiLost(new int[] { 1 - curLost });
				break;
			case 7:
				// 来电提醒
				sharePre.edit()
						.putBoolean("set_isremind_call", bool_call = !bool_call)
						.commit();
				itemIcon[7].setImageResource(bool_call ? R.drawable.call_on
						: R.drawable.call_off);
				break;
			case 8:
				// 短信提醒
				sharePre.edit()
						.putBoolean("set_isremind_msg", bool_sms = !bool_sms)
						.commit();
				itemIcon[8].setImageResource(bool_sms ? R.drawable.sms_on
						: R.drawable.sms_off);

				break;
			case 9:
				sharePre.edit()
						.putBoolean("air_open_bt", bool_openbt = !bool_openbt)
						.commit();
				itemIcon[9].setImageResource(bool_openbt ? R.drawable.bt_on
						: R.drawable.bt_off);
				break;
			case 10:
				sharePre.edit()
						.putBoolean("air_auto_connect",
								bool_connect = !bool_connect).commit();

				itemIcon[10]
						.setImageResource(bool_connect ? R.drawable.connect_on
								: R.drawable.connect_off);

				break;
			case 11:
				// 检测更新
				getLastestVersion();
				break;
			case 12:
				sharePre.edit()
						.putBoolean("set_isremind_wechat",
								(curWechat = 1 - curWechat) == 1).commit();
				itemIcon[12]
						.setImageResource((curWechat == 1) ? R.drawable.wechat_on
								: R.drawable.wechat_off);
				break;
			case 13:
				sharePre.edit()
						.putBoolean("app_notification",
								(curApp = 1 - curApp) == 1).commit();
				itemIcon[13].setImageResource((curApp == 1) ? R.drawable.app_on
						: R.drawable.app_off);
				break;
			case 18:
				// 关闭设备
				showOff();
				break;
			}

		}
	}

	void showOff() {
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle(R.string.turnoff);
		ab.setMessage(R.string.turnoffmsg);
		ab.setNegativeButton(R.string.cancel, null);
		ab.setPositiveButton(R.string.turnoff,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						airSetShutdown();
					}
				});
		ab.create().show();
	}

	void airSetShutdown() {
		Intent intent = new Intent();
		intent.setAction(BluetoothLeService.ACTION_BLE_CONFIG_CMD);
		intent.putExtra("type", 99);
		intent.putExtra("param", new int[] { 0 });
		sendBroadcast(intent);
	}

	void airSetAntiLost(int[] args) {
		Intent intent = new Intent();
		intent.setAction(BluetoothLeService.ACTION_BLE_CONFIG_CMD);
		intent.putExtra("type", 88);
		intent.putExtra("param", args);
		sendBroadcast(intent);

	}

	void setLan() {
		AlertDialog.Builder aba = new AlertDialog.Builder(AirSettingTable.this);
		aba.setTitle(R.string.air_language);
		final String[] str = new String[] { getString(R.string.language_zh),
				getString(R.string.language_en),
				getString(R.string.language_auto) };
		aba.setItems(str, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				// 0是英文 1是中文
				int temp = 1;
				if (which < 2) {
					temp = 1 - which;
					sharePre.edit().putInt("lan", temp).commit();
					// airlanguge.setText(str[which]);
				} else {
					temp = 2;
					sharePre.edit().putInt("lan", temp).commit();
					// airlanguge.setText(str[which]);
				}
				showProgress(R.string.data_wait);
				setAirDisplay(temp, curHand);
			}
		});
		aba.create().show();
	}

	void showProgress(int resid) {
		progress = Util.initProgressDialog(this, true, getString(resid), null);

		handler.sendEmptyMessageDelayed(5, 4000);
	}

	// 显示设置 抬腕、语言
	void setAirDisplay(int lan2, int hand) {

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
		intent.putExtra("param", new int[] { 1, lan2, hand });
		sendBroadcast(intent);
	}

	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		if (v.getTag() != null) {
			int id = Integer.valueOf(v.getTag().toString());
			switch (id) {
			case 1:
				showAlarm(1);
				break;
			case 2:
				showAlarm(2);
				break;
			case 3:
				showMsgDialog("打开时，当用户抬手看时间时，1秒后手表会自动亮屏（抬手的姿势会影响判断哦）");
				break;
			case 4:
				showMsgDialog("这里是指手环屏幕显示语言支持简体中文、英文");
				break;
			case 5:
				showMsgDialog("在手环上触发呼叫手机，手机会有响铃，帮助您寻找手机");
				break;
			case 6:
				showLostDelay();
				break;
			case 7:
				showMsgDialog("来电时，手环会收到提醒");
				break;
			case 8:
				showMsgDialog("来短信时，手环会收到提醒");
				break;
			case 9:
				showMsgDialog("勾选时，如果蓝牙被关闭了，则会自动打开蓝牙（需要开放权限）");
				break;
			case 10:

				break;
			case 11:
				break;
			case 13:
				//
				Intent intent = new Intent(AirSettingTable.this,
						com.bandlink.air.NotificationManager.class);
				startActivity(intent);
				break;
			}

		}
		return false;
	};

	void showMsgDialog(String str) {
		AlertDialog.Builder ab = new AlertDialog.Builder(AirSettingTable.this);
		ab.setMessage(str);
		ab.setPositiveButton(R.string.isee, null);
		ab.create().show();
	}

	/**
	 * 检测无声闹铃设置状态，生成长度为6的byte数组{0[1],x,x,0[1],x,x} 0、3位表示闹钟开启状态后面两位时、分
	 */
	private void commitAlarm(int a1, int a2, String v1, String v2) {
		// 闹钟是否开启

		int[] args = new int[6];
		try {
			String[] str = v1.split(":");
			args[0] = a1;
			args[1] = Integer.parseInt(str[0]);
			args[2] = Integer.parseInt(str[1]);

		} catch (Exception e) {
			args[3] = 0;
			args[4] = 8;
			args[5] = 30;
			e.printStackTrace();
		}
		try {
			String[] str2 = v2.split(":");
			args[3] = a2;
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
		intent.putExtra("w1", tempWeek1);
		intent.putExtra("w2", tempWeek2);
		sendBroadcast(intent);

	}

	boolean[] bool;
	private String tempAlarm1, tempAlarm2, tempWeek1, tempWeek2;

	private void showWeekSelector(final int index, final TextView view) {
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
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle(R.string.week);
		ab.setMultiChoiceItems(getResources().getStringArray(R.array.week),
				bool, new OnMultiChoiceClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which,
							boolean isChecked) {
						// TODO Auto-generated method stub
						bool[which] = isChecked;
					}
				});
		ab.setNegativeButton(R.string.can, null);
		ab.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {

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

						if (index == 1) {
							tempWeek1 = sb.toString();
							view.setText(getWeek(tempWeek1));
						} else {
							tempWeek2 = sb.toString();
							view.setText(getWeek(tempWeek2));
						}
					}
				});
		ab.create().show();

	}

	void showAlarm(final int index) {
		AlertDialog.Builder ab = new AlertDialog.Builder(AirSettingTable.this);
		View view = LayoutInflater.from(AirSettingTable.this).inflate(
				R.layout.alarm_set_dialog, null);
		final TimePicker picker = (TimePicker) view
				.findViewById(R.id.timepicker);
		final TextView week = (TextView) view.findViewById(R.id.week);
		week.setText(getWeek(index));
		week.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showWeekSelector(index, week);
			}
		});
		picker.setIs24HourView(true);
		ab.setView(view);
		ab.setNegativeButton(R.string.cancel, null);
		ab.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						showProgress(R.string.data_wait);
						String str = SlideMainActivity.turnNum(picker
								.getCurrentHour())
								+ ":"
								+ SlideMainActivity.turnNum(picker
										.getCurrentMinute());
						if (index == 1) {
							tempAlarm1 = str;
							commitAlarm(curAlarm1, curAlarm2, str,
									SlideMainActivity.alarm2Value);
						} else if (index == 2) {
							tempAlarm2 = str;
							commitAlarm(curAlarm1, curAlarm2,
									SlideMainActivity.alarm1Value, str);
						}
					}
				});
		ab.create().show();
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
		return sb.toString().length() == 0 ? getString(R.string.none) : sb
				.toString();
	}

	private String getWeek(String value) {
		String str = value;
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
		return sb.toString().length() == 0 ? getString(R.string.none) : sb
				.toString();
	}

	private int delay;

	private void showLostDelay() {
		AlertDialog.Builder ab = new AlertDialog.Builder(AirSettingTable.this);
		View view = LayoutInflater.from(AirSettingTable.this).inflate(
				R.layout.lost_delay, null);
		final NewSeekBar seekbar = (NewSeekBar) view.findViewById(R.id.seekbar);
		delay = sharePre.getInt("air_delay", 10);
		seekbar.setNumberThumb(delay);
		seekbar.setProgress((int) (((delay - 3) / 57f) * 100));
		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				delay = (int) (57 * (progress / 100f)) + 3;
				seekbar.setNumberThumb(delay);

			}
		});
		view.setBackgroundColor(currentColor);
		ab.setView(view);
		ab.setNegativeButton(R.string.cancel, null);
		ab.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						sendLostDelay();
					}
				});
		ab.create().show();
	}

	void sendLostDelay() {
		Intent intent = new Intent();
		intent.putExtra("delay", delay);
		sharePre.edit().putInt("air_delay", delay).commit();
		intent.setAction(BluetoothLeService.ACTION_PREF_CHANGED);
		sendBroadcast(intent);
	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub

	}
}

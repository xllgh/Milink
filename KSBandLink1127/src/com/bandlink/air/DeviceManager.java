package com.bandlink.air;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONObject;

import com.bandlink.air.ble.BluetoothLeService;
import com.bandlink.air.pingan.PinganRegisterActivity;
import com.bandlink.air.user.LoginActivity;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.MyDate;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Util;
import com.bandlink.air.view.BadgeView;
import com.bandlink.air.view.NoRegisterDialog;
import com.milink.android.lovewalk.bluetooth.service.StepService;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DeviceManager extends LovefitActivity {
	private LinearLayout container;
	private int uid;
	private String user;
	private Dbutils dbutil;
	int deviceName, deviceImg, deviceTypeName;
	int deviceNameW, deviceImgW, deviceTypeNameW;
	String deviceCode;
	private SharedPreferences share, sharePre;
	private int ismember;
	private ProgressDialog progressDialog;
	private NoRegisterDialog d;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_manager);

		ActionbarSettings actionbar = new ActionbarSettings(this, new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				DeviceManager.this.finish();
			}
		}, null);
		actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
		actionbar.setTitle(R.string.setting_device_manager);
		container = (LinearLayout) findViewById(R.id.manager_device_container);
		share = getSharedPreferences(SharePreUtils.APP_ACTION, Context.MODE_MULTI_PROCESS);
		sharePre = getSharedPreferences(SharePreUtils.AIR_ACTION, MODE_MULTI_PROCESS);
		user = share.getString("USERNAME", "lovefit");
		uid = share.getInt("UID", -1);
		ismember = share.getInt("ISMEMBER", 0);
		if (share.getInt("ISMEMBER", 0) == 0) {
			d = new NoRegisterDialog(DeviceManager.this, R.string.no_register, R.string.no_register_content);
			d.show();
		}
		dbutil = new Dbutils(uid, DeviceManager.this);
		// _id,uid,sp_type,spdid,wg_type,wgdid
		Object[] device = dbutil.getUserDeivce();
		if (device == null || device[3].toString().length() < 5) {
			// progressDialog = initProgressDialog();
			getDevice();
		} else {
			try {
				if (device[3].toString().contains(":")) {
					dbutil.setDeviceSpType(5);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			initViews();
		}
	}

	/***
	 * 请求用户绑定的设备信息
	 */
	private void getDevice() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Map<String, String> map = new HashMap<String, String>();
				map.put("session", share.getString("session_id", ""));

				try {
					String result = HttpUtlis.getRequest(HttpUtlis.BASE_URL + "/user/getUserDevice", map);
					JSONObject js = new JSONObject(result);
					if (js.getString("message").equals("ok")) {
						JSONObject devJson = js.getJSONObject("content");

						try {
							dbutil.InitDeivce(Integer.parseInt(devJson.getString("devicetype")),
									dbutil.getDeviceWgType(), devJson.getString("deviceid"), "");
						} catch (Exception e) {
							e.printStackTrace();
						}
						hand.sendEmptyMessage(1);
					} else {

					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					hand.sendEmptyMessage(1);
					e.printStackTrace();

				}
			}
		}).start();
	}

	Handler hand = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case 0:
				if (progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				dbutil.setDeviceSpID("0");
				dbutil.setDeviceSpType(0);
				container.removeAllViews();
				initViews();
				break;
			case 1:
				if (progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				initViews();
				break;
			case 2:
				if (msg.obj != null) {

				}
				// initViews();
				break;
			case 3:
				Toast.makeText(DeviceManager.this, R.string.check_network, Toast.LENGTH_SHORT).show();
				break;
			}
			super.handleMessage(msg);
		}

	};
	int weightDes;

	/***
	 * 初始化界面，根据设备类型加载
	 */
	private void initViews() {
		Object[] obj = dbutil.getUserDeivce();
		// _id,uid,sp_type,spdid,wg_type,wgdid
		int sp_type = -1;
		int we_type = -1;
		deviceTypeName = R.string.setting_device_sportsdevice;
		try {
			deviceCode = obj[3].toString();
			// sp_type = Integer.parseInt(obj[2].toString());
			sp_type = dbutil.getUserDeivceType();

		} catch (Exception e) {
			deviceCode = "";

		}
		try {
			we_type = Integer.valueOf(obj[4].toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			we_type = -1;
		}
		switch (sp_type) {
		case 4:
			deviceName = R.string.setting_device_lovefitAnt;
			deviceImg = R.drawable.setting_ant;
			break;
		case 1:
			deviceName = R.string.setting_device_lovefitflame;
			deviceImg = R.drawable.setting_flame;
			break;
		case 5:
			SlideMainActivity.connectName = share.getString("connectName", "");
			if (SlideMainActivity.connectName.toUpperCase().contains("AIRIII")) {
				if (share.getBoolean("isKs", false)) {
					deviceName = R.string.setting_device_lovefitKs;
					deviceImg = R.drawable.kuaishua;
				} else {
					deviceName = R.string.setting_device_air3;
					deviceImg = R.drawable.air3;
				}

			} else if (SlideMainActivity.connectName.toUpperCase().contains("AIRII")) {
				deviceName = R.string.setting_device_air2;
				deviceImg = R.drawable.setting_air;
			} else if (SlideMainActivity.connectName.toUpperCase().contains("COOLBAND")) {
				deviceName = R.string.setting_device_coolband;
				deviceImg = R.drawable.coolband;
			} else if (SlideMainActivity.connectName.toUpperCase().contains("AIRIV")) {
				deviceName = R.string.setting_device_air4;
				deviceImg = R.drawable.air3;
			} else if (SlideMainActivity.connectName.toUpperCase().contains("AIRMSG")) {
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

			break;
		case 2:
			deviceName = R.string.soft_step;
			deviceImg = R.drawable.ic_launcher;
			break;
		case 0:
			break;
		default:
			break;

		}
		// 如果类型为0,也显示未绑定
		if (sp_type == 2) {
			addSportSoftView();
		} else if (deviceCode.equals("") || sp_type == 0) {
			addSportUnBindView();
		} else {
			addSportBindView(sp_type);
		}
		// 体重产品，暂无
		deviceTypeName = R.string.setting_device_weightdevice;
		switch (we_type) {
		case 3:
			// deviceName = R.string.setting_device_lovefitAnt;
			// deviceImg = R.drawable.setting_ant;
			// addWeightBindView();
			break;
		case -1:
			// addWeightUnBindView();
			break;
		case 1:
			deviceNameW = R.string.lovefitslim;
			deviceImgW = R.drawable.mall_color;
			weightDes = R.string.lovefitslim_msg;
			// addWeightBindView();
			break;
		case 0:
			deviceNameW = R.string.soft_weight;
			deviceImgW = R.drawable.ic_launcher;
			weightDes = R.string.soft_weight_msg;
			// addWeightBindView();
		default:

			break;

		}
	}

	boolean isPause = true;

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		isPause = true;
		unregisterReceiver(airReceiver);
		super.onPause();
	}

	/***
	 * 界面添加已绑定的运动设备
	 * 
	 */
	void addSportSoftView() {
		if (isPause) {
			return;
		}
		container.removeAllViews();
		View v2 = LayoutInflater.from(DeviceManager.this).inflate(R.layout.device_item_binding, null);
		// 设备icon
		ImageView i = (ImageView) v2.findViewById(R.id.img_set_device);
		i.setImageResource(deviceImg);
		ImageView list_arrow = (ImageView) v2.findViewById(R.id.list_arrow);
		if (getString(deviceName).contains("Air")) {
			list_arrow.setVisibility(View.VISIBLE);
		}
		// 类型名称
		TextView type = (TextView) v2.findViewById(R.id.typename);
		type.setText(deviceTypeName);
		// 设备的编码
		LinearLayout ly = (LinearLayout) v2.findViewById(R.id.linear_code);
		ly.setVisibility(View.GONE);
		// 设备名称
		final TextView name = (TextView) v2.findViewById(R.id.name_set_device);
		name.setText(deviceName);
		// 设备简介
		TextView preference = (TextView) v2.findViewById(R.id.describe_set_device);
		preference.setText(getString(R.string.soft_step_describe));
		v2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 如果是Air，可以经行参数设置
				if (name.getText().toString().contains("Air")) {
					Intent intent = new Intent(DeviceManager.this, AirBaseSetActivity.class);
					intent.putExtra("version", version);
					intent.putExtra("connect", isConnect);
					startActivity(intent);
				}
			}
		});
		// 更换设备按钮
		Button change = (Button) v2.findViewById(R.id.change_device);

		change.setText(getString(R.string.unbind_device));

		change.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (ismember == 0) {
					NoRegisterDialog d = new NoRegisterDialog(DeviceManager.this, R.string.no_register,
							R.string.no_register_content);
					d.show();
				} else {
					unBindSPDevice(2);
				}

			}
		});
		container.addView(v2);
	}

	void addSportBindView(final int sp_type) {
		if (isPause) {
			return;
		}
		container.removeAllViews();
		View v2 = LayoutInflater.from(DeviceManager.this).inflate(R.layout.device_item_binding, null);
		// 设备icon
		ImageView i = (ImageView) v2.findViewById(R.id.img_set_device);
		ImageView list_arrow = (ImageView) v2.findViewById(R.id.list_arrow);
		i.setImageResource(deviceImg);
		// 类型名称
		TextView type = (TextView) v2.findViewById(R.id.typename);
		type.setText(deviceTypeName);
		// 设备的编码
		TextView code = (TextView) v2.findViewById(R.id.code);
		code.setText(deviceCode + "");
		// 设备名称
		final TextView name = (TextView) v2.findViewById(R.id.name_set_device);
		name.setText(deviceName);
		if (sp_type == 5) {
			name.setTag("5");
		} else {
			name.setTag(null);
		}
		TextView preference = (TextView) v2.findViewById(R.id.describe_set_device);
		// 设备简介
		if (sp_type == 5) {
			list_arrow.setVisibility(View.VISIBLE);
			boolean b2 = share.getBoolean("airupdate", false);
			int count = 0;
			Dbutils db = new Dbutils(share.getInt("UID", -1), DeviceManager.this);
			if (b2 && db.getUserDeivceType() == 5) {
				count = 1;
				BadgeView b = new BadgeView(DeviceManager.this);
				b.setTargetView(preference);
				b.setBadgeGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
				b.setBadgeCount(count);
				preference.setTag(b);
			} else {
				Object o = preference.getTag();
				if (o != null && o instanceof BadgeView) {
					((BadgeView) o).setBadgeCount(0);
				}
			}

		}

		v2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 如果是Air，可以经行参数设置
				if (name.getTag() != null) {
					Intent intent = new Intent(DeviceManager.this, AirPreferenceActivity.class);
					intent.putExtra("version", version);
					intent.putExtra("connect", isConnect);
					intent.putExtra("mode", 2);
					startActivity(intent);
				}

			}
		});
		// 更换设备按钮
		Button change = (Button) v2.findViewById(R.id.change_device);

		change.setText(getString(R.string.unbind_device));

		change.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (ismember == 0) {
					NoRegisterDialog d = new NoRegisterDialog(DeviceManager.this, R.string.no_register,
							R.string.no_register_content);
					d.show();
				} else {

					unBindSPDevice(sp_type);

				}

			}
		});
		container.addView(v2);
	}

	// 添加绑定的体重设备界面，暂无
	void addWeightBindView() {
		if (isPause) {
			return;
		}
		// container.removeAllViews();
		View v2 = LayoutInflater.from(DeviceManager.this).inflate(R.layout.device_item_binding, null);
		final ImageView i = (ImageView) v2.findViewById(R.id.img_set_device);
		i.setImageResource(deviceImgW);
		TextView type = (TextView) v2.findViewById(R.id.typename);
		v2.findViewById(R.id.linear_code).setVisibility(View.GONE);
		type.setText(deviceTypeName);
		TextView code = (TextView) v2.findViewById(R.id.code);
		code.setVisibility(View.GONE);
		code.setText(deviceCode);
		final TextView name = (TextView) v2.findViewById(R.id.name_set_device);
		name.setText(deviceNameW);
		final TextView preference = (TextView) v2.findViewById(R.id.describe_set_device);
		preference.setText(weightDes);
		v2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

			}
		});

		Button change = (Button) v2.findViewById(R.id.change_device);

		change.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// startActivity(new Intent(DeviceManager.this,
				// SportsDevice.class));
				Context con = Util.getThemeContext(DeviceManager.this);
				AlertDialog.Builder ab = new AlertDialog.Builder(con);
				ab.setTitle(R.string.weight_source);
				ab.setItems(getResources().getStringArray(R.array.spinnerweight),
						new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						switch (which) {
						case 0:
							name.setText(R.string.soft_weight);
							preference.setText(R.string.soft_weight_msg);
							i.setImageResource(R.drawable.ic_launcher);
							dbutil.setDeviceWgType(which);
							break;
						case 1:
							name.setText(R.string.lovefitslim);
							preference.setText(R.string.lovefitslim_msg);
							i.setImageResource(R.drawable.mall_color);
							dbutil.setDeviceWgType(which);
							break;
						}
					}
				});
				ab.setNegativeButton(R.string.can, null);
				ab.create().show();

			}
		});
		container.addView(v2);
	}

	// 当用户没有绑定任何运动设备时，启用此方法，添加绑定入口
	void addSportUnBindView() {
		if (isPause) {
			return;
		}
		try {
			container.removeAllViews();
			View v1 = LayoutInflater.from(DeviceManager.this).inflate(R.layout.device_item_unbinding, null);
			Button btn_binding = (Button) v1.findViewById(R.id.btn_binding);
			TextView type = (TextView) v1.findViewById(R.id.typename);
			type.setText(deviceTypeName);
			btn_binding.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (ismember == 0) {
						NoRegisterDialog d = new NoRegisterDialog(DeviceManager.this, R.string.no_register,
								R.string.no_register_content);
						d.show();
					} else {
						startActivity(new Intent(DeviceManager.this, DeviceSettingBindingBLE.class));
					}
				}
			});
			container.addView(v1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 当用户没有绑定任何体重设备时，启用此方法，添加绑定入口
	void addWeightUnBindView() {
		if (isPause) {
			return;
		}
		View v3 = LayoutInflater.from(DeviceManager.this).inflate(R.layout.device_item_unbinding, null);
		Button btn_binding = (Button) v3.findViewById(R.id.btn_binding);
		TextView type = (TextView) v3.findViewById(R.id.typename);
		type.setText(deviceTypeName);
		btn_binding.setEnabled(false);
		btn_binding.setTextColor(getResources().getColor(R.color.white_0_8));
		// btn_binding.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		//
		// Intent(DeviceManager.this, SportsDevice.class));
		// }
		// });
		container.addView(v3);
	}

	void unBindSPDevice(final int sp_type) {
		Context c = Util.getThemeContext(DeviceManager.this);
		AlertDialog.Builder ab = new AlertDialog.Builder(c);
		ab.setTitle(getString(R.string.warning));
		ab.setMessage(getString(R.string.unbind_device) + "?");
		ab.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if (sp_type == 2) {
					switch (sp_type) {
					case 5:
						sharePre.edit().remove("soft_version").commit();
						dbutil.delSleepDetail(MyDate.getYesterDay());
						SlideMainActivity.currentVersion = null;
						stopService(new Intent(DeviceManager.this, BluetoothLeService.class));
						break;
					case 2:
						stopService(new Intent(DeviceManager.this, StepService.class));
						break;
					}
					dbutil.setDeviceSpID("0");
					dbutil.setDeviceSpType(0);
					// progressDialog = initProgressDialog();
					container.removeAllViews();
					initViews();
					clearDeviceType();
				} else if (sp_type == 1 || sp_type == 4 || sp_type == 5) {
					progressDialog = Util.initProgressDialog(DeviceManager.this, true, getString(R.string.unbinding),
							null);
					share.edit().putBoolean("isKs", false).commit();
					// 改侧菜单
					sendBroadcast(new Intent(BluetoothLeService.ACTION_BLE_BIND_DEVICE_CHANGED));
					if (sp_type == 5 && deviceCode != null && deviceCode.length() > 5) {
						dbutil.delSleepDetail(MyDate.getYesterDay());
						stopService(new Intent(DeviceManager.this, BluetoothLeService.class));
						reSetDevice(deviceCode, sp_type);

					} else if (sp_type == 5) {
						dbutil.setDeviceSpID("0");
						dbutil.setDeviceSpType(0);
						// progressDialog = initProgressDialog();
						container.removeAllViews();
						initViews();
						clearDeviceType();
					} else {
						reSetDevice(deviceCode, sp_type);
					}
				}
			}
		});
		ab.setNegativeButton(getString(R.string.cancel), null);
		ab.create().show();
	}

	private boolean isConnect = false;
	private String version = "";
	private BroadcastReceiver airReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			// TODO Auto-generated method stub

			final String action = intent.getAction();

			if (action.equals(BluetoothLeService.ACTION_AIR_RSSI_STATUS)) {
				isConnect = intent.getIntExtra("status", 0) == 0 ? false : true;
			}

			if (action.equals(BluetoothLeService.ACTION_BLE_CONFIG_READ_RESPONSE)) {

				final int type = intent.getIntExtra("type", -1);

				switch (type) {
				case 22:
					version = intent.getStringExtra("value");

					break;

				default:
					break;
				}
			}
		}

	};

	private void reSetDevice(final String did, final int sp) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				// 接口 0->蓝牙 ，1-》flame， 2-》手机， 3->ble, 4-ant ， 5-》air
				// app选项中 0-》手机，1-》air，2-》flame，3-》ant
				Map<String, String> device = new HashMap<String, String>();
				device.put("session", share.getString("session_id", ""));
				device.put("devicetype", "0");
				device.put("deviceid", did);
				device.put("devicepwd", "0");
				try {
					String result = HttpUtlis.getRequest(HttpUtlis.BASE_URL + "/user/reSetUserDevice", device);
					JSONObject js = new JSONObject(result);
					if (js.getString("status").equals("0")) {// 成功
						if (share.getBoolean("ispa", false)) {
							String url = PinganRegisterActivity.PINGAN_BASE_URL
									+ "/open/appsvr/health/partner/bind/device/0330000?access_token="
									+ share.getString("pingan_token", "");
							String ur2 = "&partyNo=" + share.getString("vnumber", "")
									+ "&partnerCode=0330000&deviceType=" + sp;
							String res = HttpUtlis.doDelete(url + ur2);
							System.out.println(res);
						}
						Message m = new Message();
						m.obj = did;
						m.what = 0;
						m.arg1 = 0;
						// 保存
						dbutil.setDeviceSpID("");
						dbutil.setDeviceSpType(0);
						hand.sendMessage(m);
					} else if (js.getString("status").equals("2")) {
						String session = null;
						try {
							session = SlideMainActivity.getSession(share);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (session != null) {
							reSetDevice(did, sp);
						} else {
							startActivity(new Intent(DeviceManager.this, LoginActivity.class));
							finish();
							android.os.Process.killProcess(android.os.Process.myPid());
							Toast.makeText(getApplicationContext(), getString(R.string.getsessionerror),
									Toast.LENGTH_LONG).show();
						}
					} else {
						hand.sendEmptyMessage(3);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					if (e instanceof UnknownHostException) {
						hand.sendEmptyMessage(3);
					} else {
						hand.sendEmptyMessage(3);
					}

					e.printStackTrace();
				}
			}
		}).start();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		isPause = false;
		ismember = share.getInt("ISMEMBER", 0);
		String u = share.getString("USERNAME", "lovefit");
		if (!u.equals(user) && ismember == 1) {
			// if (ismember == 1) {
			dbutil = new Dbutils(share.getInt("UID", -1), DeviceManager.this);
			// progressDialog = initProgressDialog();
			getDevice();
		}
		initViews();

		if (!dbutil.hasNotificationPermission()) {
			showPermission();
		}
		IntentFilter inf = new IntentFilter();
		inf.addAction(BluetoothLeService.ACTION_AIR_RSSI_STATUS);
		inf.addAction(BluetoothLeService.ACTION_BLE_CONFIG_READ_RESPONSE);
		registerReceiver(airReceiver, inf);
		super.onResume();
	}

	private void showPermission() {
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle(R.string.warning);
		ab.setMessage(R.string.permission_notification);
		ab.setNegativeButton(R.string.cancel, null);
		ab.setPositiveButton(R.string.toauth, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
				startActivity(intent);
				dialog.dismiss();
			}
		});
		ab.setNeutralButton(R.string.donotshowme, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dbutil.saveNotificationPermission(true);
			}
		});
		ab.create().show();
	}

	public void clearDeviceType() {
		new Thread() {
			public void run() {
				String urlStr = HttpUtlis.BASE_URL + "/user/setUserDeviceType";
				Map<String, String> map = new LinkedHashMap<String, String>();
				map.put("session", share.getString("session_id", ""));
				// 设备类型为 AIR
				// 接口 0->蓝牙 ，1-》flame， 2-》手机， 3->ble, 4-ant ， 5-》air
				map.put("devicetype", "0");
				try {
					String result = HttpUtlis.getRequest(urlStr, map);
					JSONObject json = new JSONObject(result);
					if (json.getInt("status") == 0) {
						hand.obtainMessage(2, null).sendToTarget();

					} else {
						hand.obtainMessage(2, "").sendToTarget();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
					hand.obtainMessage(2, "").sendToTarget();
				}
				super.run();

			}

		}.start();

	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub

	}

}

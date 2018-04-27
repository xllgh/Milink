package com.bandlink.air;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Timer;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bandlink.air.ble.BluetoothLeService;
import com.bandlink.air.ble.LogUtil;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.MApplication;
import com.bandlink.air.util.SharePreUtils;
import com.milink.android.lovewalk.bluetooth.service.StepService;

@SuppressLint("NewApi")
public class DeviceSettingBindingBLE extends LovefitActivity {

	private String type;

	private String strScanStart;
	private String strScanStop;

	private String user;
	private String user_session;
	private int uid;
	private int devicetype;
	private Dbutils dbutil;
	private SharedPreferences share;

	private BluetoothAdapter mBluetoothAdapter;
	private boolean mScanning;
	private Handler mHandler;
	private static final int REQUEST_ENABLE_BT = 1;
	// Stops scanning after 10 seconds.
	private static final long SCAN_PERIOD = 10000;
	private String from;
	private ImageView startBtn;
	private TextView back, startText;
	private ArrayList<AirDevice> mLeDevices = new ArrayList<AirDevice>();
	private BroadcastReceiver airReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			// TODO Auto-generated method stub
			String address = intent.getStringExtra("address");
			if (address != null) {
				uploadBinding(address);
			}

		}

	};

	private Handler myHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case 1:
				// success
				Context context = getApplicationContext();

				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
				// 保存绑定的设备
				// 接口 0->蓝牙 ，1-》flame， 2-》手机， 3->ble, 4-ant ， 5-》air
				// app选项中 0-》手机，1-》air，2-》flame，3-》ant
				Dbutils dbutil = new Dbutils(DeviceSettingBindingBLE.this);
				dbutil.setDeviceSpID(msg.obj.toString());
				dbutil.setDeviceSpType(5);
				dbutil.InitDeivce(5, 0, msg.obj.toString(), "");
				share = getSharedPreferences(SharePreUtils.APP_ACTION,
						Context.MODE_MULTI_PROCESS);
				if (share != null) {
					share.edit().putString("soft_version", "").commit();
				}
				// if(device == 2)
				// {
				// 停止软件计步
				// Intent intent1 = new Intent();
				// intent1.setAction("milinkStartService");// 用户检查用户类型,打开serivce
				// sendBroadcast(intent1);
				// }
				Intent intentdct = new Intent("MilinkConfig");
				intentdct.putExtra("command", 2);
				DeviceSettingBindingBLE.this.sendBroadcast(intentdct);

				Intent i = new Intent(DeviceSettingBindingBLE.this,
						StepService.class);
				DeviceSettingBindingBLE.this.stopService(i);
				// 读一次air信息 如rom版本 ，设备id等
				// airReadconfig();
				if (from != null) {
					if (from.equals("init")) {
						startActivity(new Intent(DeviceSettingBindingBLE.this,
								SlideMainActivity.class));
						for (Activity ac : MApplication.getInstance().getList()) {
							try {
								ac.finish();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					} else {
						startActivity(new Intent(DeviceSettingBindingBLE.this,
								SlideMainActivity.class));
					}

				}
				DeviceSettingBindingBLE.this.finish();
				break;
			case 2:
				// time out
				handleError("绑定失败");
				StopBleService();
				break;
			case 3:
				// no device found
				handleError("没有发现相关设备");
				StopBleService();
				break;
			case 4:
				// low rssi
				handleError("设备信号较弱，请拿近再试");
				StopBleService();
				break;
			case 5:
				// upload error
				handleError("绑定失败请稍后再试");
				StopBleService();
				break;
			case 6:
				handleError("请检查网络链接");
				StopBleService();
				break;
			case 8:
				handleError(msg.obj.toString());
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}

	};

	private void handleError(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		if (startBtn != null) {

			back.clearAnimation();

			startBtn.setEnabled(true);
			startText.setTextColor(getResources().getColor(R.color.white_0_8));
			startText.setText(R.string.scanandbind);
			// startBtn.setImageResource(resId)

		}

	}

	private Button btnList, btnAQ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		from = getIntent().getStringExtra("from");
		setContentView(R.layout.setting_device_binding_bt);

		mHandler = new Handler();
		mScanning = false;
		if (!getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT)
					.show();
			finish();
		}

		// Initializes a Bluetooth adapter. For API level 18 and above, get a
		// reference to
		// BluetoothAdapter through BluetoothManager.
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		// Checks if Bluetooth is supported on the device.
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.error_bluetooth_not_supported,
					Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		mBluetoothAdapter.enable();
		if (!FriendFragment.isNetworkConnected(this)) {
			Toast.makeText(this, getString(R.string.failedifnetdismiss),
					Toast.LENGTH_LONG).show();
		}
		strScanStart = getString(R.string.setting_device_bt_scan);
		strScanStop = getString(R.string.setting_device_bt_scan_stop);

		ActionbarSettings actionbar = new ActionbarSettings(this,
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						DeviceSettingBindingBLE.this.finish();
					}
				}, null);
		actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
		actionbar.setTitle(R.string.setting_device_sportsdevice);

		initUser();
		startBtn = (ImageView) findViewById(R.id.startBtn);
		back = (TextView) findViewById(R.id.bg);
		startText = (TextView) findViewById(R.id.startText);
		btnList = (Button) findViewById(R.id.btnl);
		btnAQ = (Button) findViewById(R.id.btnaq);
		btnList.setVisibility(View.VISIBLE);
		btnAQ.setVisibility(View.VISIBLE);
		btnList.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mBluetoothAdapter.stopLeScan(mLeScanCallback);
				mScanning = false;
				mBluetoothAdapter = null;
				startActivity(new Intent(DeviceSettingBindingBLE.this,
						BleListActivity.class));
				DeviceSettingBindingBLE.this.finish();
			}
		});
		btnAQ.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(DeviceSettingBindingBLE.this,
						AirHelp.class));
			}
		});
		startBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startText.setTextColor(getResources().getColor(R.color.white));
				scanDevice();
				back.startAnimation(AnimationUtils.loadAnimation(
						DeviceSettingBindingBLE.this, R.anim.anim_air_scan));
				startBtn.setEnabled(false);
			}
		});
		IntentFilter filter_dynamic = new IntentFilter();
		filter_dynamic.addAction(BluetoothLeService.ACTION_DEVICE_AIR);
		registerReceiver(airReceiver, filter_dynamic);
		// 在此页面防丢不报警
		Intent intent = new Intent();
		intent.setAction(BluetoothLeService.ACTION_AIR_CONTROL);
		intent.putExtra("task", 2);
		sendBroadcast(intent);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		intent.setAction(BluetoothLeService.ACTION_AIR_CONTROL);
		intent.putExtra("task", 3);
		sendBroadcast(intent);
		if (timer != null) {
			timer.cancel();
		}
		myHandler.removeCallbacksAndMessages(null);
		unregisterReceiver(airReceiver);
		super.onDestroy();
	}

	private void StopBleService() {
		Intent intent = new Intent(this, BluetoothLeService.class);
		stopService(intent);
	}

	private void StartBleService(String name, String address) {
		Intent intent = new Intent(this, BluetoothLeService.class);
		intent.putExtra("name", name);
		SlideMainActivity.connectName = name;
		intent.putExtra("address", address);
		share.edit().putString("connectName", name).commit();
		// 1 普通模式 2是固件升级
		intent.putExtra("command", 1);
		intent.putExtra("scanflag", 0);
		startService(intent);
	}

	void airReadconfig() {
		Intent intent = new Intent();
		intent.setAction(BluetoothLeService.ACTION_BLE_CONFIG_CMD);
		intent.putExtra("type", 0);
		intent.putExtra("param", new int[] { 0 });
		sendBroadcast(intent);
	}

	private void initUser() {
		if (user == null) {
			share = getSharedPreferences(SharePreUtils.APP_ACTION,
					Context.MODE_MULTI_PROCESS);
			user = share.getString("USERNAME", "lovefit");
			uid = share.getInt("UID", -1);
			user_session = share.getString("session_id", "");
			dbutil = new Dbutils(uid, this);
		}
	}

	ProgressDialog dialog;
	int i = 9;
	Timer timer;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		System.out.println("requestCode:" + requestCode + ";resultCode:"
				+ resultCode);
		if (resultCode == 20) {
			String str = data.getStringExtra("did");
			Intent i = new Intent(this, InputWindowActivity.class);
			i.putExtra("type", devicetype);
			i.putExtra(
					"did",
					str.substring(str.indexOf("did=") + 4,
							str.indexOf("&passwd")));
			i.putExtra("pwd",
					str.substring(str.indexOf("wd=") + 3, str.length()));
			startActivity(i);

		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	void scanDevice() {
		mLeDevices.clear();

		if (!mBluetoothAdapter.isEnabled()) {
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}

		// 先停止服务
		StopBleService();
		startText.setText(R.string.scanning);
		scanLeDevice(true);

	}

	private void scanLeDevice(final boolean enable) {
		if (enable) {
			// Stops scanning after a pre-defined scan period.
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mScanning = false;
					try {
						mBluetoothAdapter.stopLeScan(mLeScanCallback);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					invalidateOptionsMenu();
					if (mLeDevices.size() <= 0) {
						myHandler.sendEmptyMessage(3);
						return;
					}

					final AirDevice airdevice = mLeDevices.get(0);
					if (airdevice == null) {
						myHandler.sendEmptyMessage(3);
						return;
					}

					if (airdevice.rssi < -60) {
						myHandler.sendEmptyMessage(4);
						return;
					}
					// final Intent intent = new Intent(this,
					// DeviceControlActivity.class);
					// intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME,
					// device.getName());
					// intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS,
					// device.getAddress());
					final BluetoothDevice device = airdevice.device;
					if (mScanning) {
						mBluetoothAdapter.stopLeScan(mLeScanCallback);
						mScanning = false;
					}
					startText.setText(R.string.device_bind_binding);
					myHandler.sendEmptyMessageDelayed(2, 30000);
					// 启动BLE服务
					LogUtil.e("--绑定设备启动ble服务--"
							+ new SimpleDateFormat("yyyy-MM-dd-HH：mm：ss-SSS")
									.format(new Date()));
					StartBleService(device.getName(), device.getAddress());
				}
			}, SCAN_PERIOD);

			mScanning = true;
			mBluetoothAdapter.startLeScan(mLeScanCallback);
		} else {
			mScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		}
		invalidateOptionsMenu();
	}

	public void addDevice(AirDevice device) {
		// if(!mLeDevices.contains(device)) {
		// mLeDevices.add(device);
		// }
		boolean isfind = false;
		for (int i = 0; i < mLeDevices.size(); i++) {
			if (mLeDevices.get(i).device.getAddress().equals(
					device.device.getAddress())) {
				mLeDevices.get(i).rssi = device.rssi > 0 ? -device.rssi
						: device.rssi;
				mLeDevices.get(i).device = device.device;
				isfind = true;
			}
		}
		if (!isfind) {
			if (device.rssi > 0) {
				device.rssi = 0 - device.rssi;
			}
			mLeDevices.add(device);
		}

		if (!mLeDevices.isEmpty()) {
			Collections.sort(mLeDevices, new Comparator<AirDevice>() {
				@Override
				public int compare(AirDevice air1, AirDevice air2) {

					// 根据字段"LEVEL"排序
					return (air2.rssi - air1.rssi);
				}
			});
		}
	}

	// Device scan callback.
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, final int rssiint,
				byte[] scanRecord) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					AirDevice airDevice = new AirDevice(device, rssiint);
					if (device != null && device.getName() != null
							&& device.getName().contains("Air")) {
						addDevice(airDevice);
					}
					// mLeDeviceListAdapter.notifyDataSetChanged();
				}
			});
		}
	};

	/***
	 * 上传绑定数据
	 * 
	 * @param id
	 *            设备id
	 * @param code
	 *            对应密码
	 */
	private void uploadBinding(final String id) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				// 接口 0->蓝牙 ，1-》flame， 2-》手机， 3->ble, 4-ant ， 5-》air
				// app选项中 0-》手机，1-》air，2-》flame，3-》ant
				Map<String, String> device = new HashMap<String, String>();
				device.put("session", share.getString("session_id", ""));
				device.put("devicetype", 5 + "");
				device.put("deviceid", id);
				try {
					String result = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/user/setAirDevice", device);
					JSONObject js = new JSONObject(result);
// http://test-api.pingan.com.cn/open/appsvr/health/partner/bind/device/0330000?access_token=94CE3BC5AEAE4266988B204B4E5F98F9

					if (js.getInt("status") == 0) {
						if (share.getBoolean("ispa", false)||true) {
							String url = "http://test-api.pingan.com.cn:20080/open/appsvr/health/partner/bind/device/0330000?access_token="
								+ share.getString("pingan_token", "FE9924ACD747413CA72FB024D4FAB337");
							//String url = PinganRegisterActivity.PINGAN_BASE_URL
							//	+ "/open/appsvr/health/partner/bind/device/0330000?access_token="
							//		+ share.getString("pingan_token", "");
							System.out.println(url);
							HashMap<String, String> map = new HashMap<String, String>();
							// o.add(new BasicNameValuePair("partyNo",
							// "007538031055"));
							// o.add(new BasicNameValuePair("partnerCode",
							// "0330000"));
							// o.add(new BasicNameValuePair("deviceType", "3"));
							map.put("partyNo", share.getString("vnumber", "006645187708"));
							map.put("partnerCode", "0330000");
							map.put("deviceType", "" + 3);
							map.put("access_token", "FE9924ACD747413CA72FB024D4FAB337");
							String res = HttpUtlis.postData(url,
									map);
							try {
								JSONObject obj = new JSONObject(res);
								if (res == null
										|| !res.contains("\"returnCode\":\"00\"")) {

									myHandler.obtainMessage(8,
											obj.getString("message"))
											.sendToTarget();
								}
							} catch (Exception e) {
								// TODO: handle exception
								// myHandler.obtainMessage(8,"").sendToTarget();
							}

						}
						myHandler.obtainMessage(1, id).sendToTarget();
						// myHandler.obtainMessage(1, id).sendToTarget();
					} else {
						myHandler.obtainMessage(5, null).sendToTarget();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					myHandler.sendEmptyMessage(6);
					e.printStackTrace();
				}
			}
		}).start();

	}

	public void setDeviceType() {
		new Thread() {
			public void run() {
				String urlStr = HttpUtlis.BASE_URL + "/user/setUserDeviceType";
				Map<String, String> map = new LinkedHashMap<String, String>();
				map.put("session", user_session);
				// 设备类型为 AIR
				// 接口 0->蓝牙 ，1-》flame， 2-》手机， 3->ble, 4-ant ， 5-》air
				map.put("devicetype", "5");
				try {
					String result = HttpUtlis.getRequest(urlStr, map);
					JSONObject json = new JSONObject(result);
					if (json.getInt("status") == 0) {
						myHandler.obtainMessage(1, null).sendToTarget();
					} else {
						myHandler.obtainMessage(5, null).sendToTarget();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
					myHandler.obtainMessage(5, null).sendToTarget();
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

class AirDevice {

	public BluetoothDevice device;
	public int rssi;
	public String name;

	public AirDevice(BluetoothDevice d, int r) {
		device = d;
		rssi = r;
	}
}

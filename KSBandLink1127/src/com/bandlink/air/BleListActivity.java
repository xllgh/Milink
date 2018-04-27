package com.bandlink.air;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bandlink.air.ble.BluetoothLeService;
import com.bandlink.air.ble.LogUtil;
import com.bandlink.air.ble.MilinkGattAttributes;
import com.bandlink.air.pingan.PinganRegisterActivity;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Util;
import com.milink.android.lovewalk.bluetooth.service.StepService;

@SuppressLint("NewApi")
public class BleListActivity extends LovefitActivity {
	private TextView name, line;
	private Button btn_scan, btn_input;
	private String type;
	private ListView listView;

	private String strScanStart;
	private String strScanStop;

	private ImageView img;

	private String user;
	private String user_session;
	private int uid;
	private int devicetype;
	private Dbutils dbutil;
	private SharedPreferences share, sharePre;

	private LeDeviceListAdapter mLeDeviceListAdapter;
	private BluetoothAdapter mBluetoothAdapter;
	private boolean mScanning;
	private Handler mHandler;
	private static final int REQUEST_ENABLE_BT = 1;
	// Stops scanning after 10 seconds.
	private static final long SCAN_PERIOD = 10000;
	private String from;
	private BroadcastReceiver airReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			// TODO Auto-generated method stub
			String address = intent.getStringExtra("address");
			if (address != null) {
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}

				// 保存绑定的设备
				// 接口 0->蓝牙 ，1-》flame， 2-》手机， 3->ble, 4-ant ， 5-》air
				// app选项中 0-》手机，1-》air，2-》flame，3-》ant
				Dbutils dbutil = new Dbutils(BleListActivity.this);
				dbutil.setDeviceSpID(address);
				dbutil.setDeviceSpType(5);
				share = getSharedPreferences(SharePreUtils.APP_ACTION,
						Context.MODE_MULTI_PROCESS);
				sharePre = getSharedPreferences(SharePreUtils.AIR_ACTION,
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
				arg0.sendBroadcast(intentdct);

				Intent i = new Intent(arg0, StepService.class);
				arg0.stopService(i);
				// 读一次air信息 如rom版本 ，设备id等
				airReadconfig();
				if (from != null) {
					startActivity(new Intent(BleListActivity.this,
							SlideMainActivity.class));
				} 
				BleListActivity.this.finish();
				// 上传
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
				Context context = getApplicationContext();
				Toast.makeText(
						context,
						getApplication().getString(
								R.string.upload_device_success),
						Toast.LENGTH_LONG).show();

				break;
			case 2:
				if (btn_scan != null)
					btn_scan.setEnabled(true);
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}

	};
	BluetoothManager bluetoothManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		from = getIntent().getStringExtra("from");
		setContentView(R.layout.ac_blelist);
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
		bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		// Checks if Bluetooth is supported on the device.
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.error_bluetooth_not_supported,
					Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		strScanStart = getString(R.string.setting_device_bt_scan);
		strScanStop = getString(R.string.setting_device_bt_scan_stop);

		ActionbarSettings actionbar = new ActionbarSettings(this,
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						BleListActivity.this.finish();
					}
				}, null);
		actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
		actionbar.setTitle(R.string.setting_device_sportsdevice);
		initViews();
		initUser();

		mLeDeviceListAdapter = new LeDeviceListAdapter();
		listView = (ListView) findViewById(R.id.listView1);
		if (listView != null) {
			listView.setAdapter(mLeDeviceListAdapter);

			listView.setOnItemClickListener(new ItemClick());
		}

		IntentFilter filter_dynamic = new IntentFilter();
		filter_dynamic.addAction(BluetoothLeService.ACTION_DEVICE_AIR);
		registerReceiver(airReceiver, filter_dynamic);

		Intent intent = new Intent();
		intent.setAction(BluetoothLeService.ACTION_AIR_CONTROL);
		intent.putExtra("task", 2);
		sendBroadcast(intent);
		// 10 -21 立即扫描
		btn_scan.setEnabled(false);
		scanDevice();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub

		hand.removeCallbacksAndMessages(null);
		Intent intent = new Intent();
		intent.setAction(BluetoothLeService.ACTION_AIR_CONTROL);
		intent.putExtra("task", 3);
		sendBroadcast(intent);
		super.onDestroy();
		unregisterReceiver(airReceiver);
	}

	private class ItemClick implements AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long id) {
			// TODO Auto-generated method stub
			final AirDevice airdevice = mLeDeviceListAdapter
					.getDevice(position);
			if (airdevice == null)
				return;
			Toast.makeText(
					BleListActivity.this,
					getString(R.string.air_connecting_device) + "  "
							+ airdevice.device.getAddress(), Toast.LENGTH_SHORT)
					.show();
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
			dialog = Util.initProgressDialog(BleListActivity.this, true,
					getString(R.string.data_wait), null);
			// 启动BLE服务
			LogUtil.e("--绑定设备启动ble服务--"
					+ new SimpleDateFormat("yyyy-MM-dd-HH：mm：ss-SSS")
							.format(new Date()));
			StartBleService(device.getName(), device.getAddress());

		}

	}

	private void StopBleService() {
		Intent intent = new Intent(this, BluetoothLeService.class);
		stopService(intent);
	}

	private void StartBleService(String name, String address) {
		Intent intent = new Intent(this, BluetoothLeService.class);
		intent.putExtra("name", name);
		intent.putExtra("address", address);
		SlideMainActivity.connectName = name;
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
	Handler hand = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if (msg.what == -1) {
				dialog.dismiss();
				StopBleService();
			}
			super.handleMessage(msg);
		}

	};

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

	private void initViews() {
		name = (TextView) findViewById(R.id.set_binding_name);
		img = (ImageView) findViewById(R.id.set_device_img);

		btn_input = (Button) findViewById(R.id.set_btn_flame);
		btn_input.setVisibility(View.GONE);
		line = (TextView) findViewById(R.id.line);
		btn_scan = (Button) findViewById(R.id.set_btn_scan);
		btn_scan.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 先检查蓝牙是否打开
				((Button) v).setEnabled(false);
				scanDevice();
			}
		});

		devicetype = 5;
		name.setText(R.string.setting_device_band);
		//img.setImageResource(R.drawable.airs);

	}

	void scanDevice() {
		myHandler.sendEmptyMessageDelayed(2, 3000);
		if (!mBluetoothAdapter.isEnabled()) {
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}
		line.setVisibility(View.VISIBLE);
		// 先停止服务
		StopBleService();

		// 扫描设备
		if (mScanning) {
			// 停止
			scanLeDevice(false);
			btn_scan.setText(strScanStart);
		} else {
			// 开始
			mLeDeviceListAdapter.clear();
			scanLeDevice(true);
			btn_scan.setText(strScanStop);
		}
	}

	private void scanLeDevice(final boolean enable) {
		if (enable) {
			List<BluetoothDevice> devices = bluetoothManager
					.getConnectedDevices(BluetoothProfile.GATT);
			for (BluetoothDevice mBluetoothDevice : devices) {
				if ((mBluetoothDevice.getName() + "").contains(share.getString(
						"air_address", "-1"))) {
					Toast.makeText(BleListActivity.this,
							getString(R.string.bind_but_connect),
							Toast.LENGTH_LONG).show();
					break;
				}
			}
			// Stops scanning after a pre-defined scan period.
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mScanning = false;
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
					btn_scan.setText(strScanStart);
					invalidateOptionsMenu();
				}
			}, SCAN_PERIOD);

			mBluetoothAdapter.startLeScan(mLeScanCallback);

			mScanning = true;
			// mBluetoothAdapter.getBluetoothLeScanner().startScan(filters,
			// settings, callback);

		} else {
			mScanning = false;

			mBluetoothAdapter.stopLeScan(mLeScanCallback);

		}
		invalidateOptionsMenu();
	}

	void stopFor21() {
		if (mScanCallback != null && mScanCallback instanceof ScanCallback) {
			BluetoothLeScanner mBluetoothLeScanner = mBluetoothAdapter
					.getBluetoothLeScanner();
			mBluetoothLeScanner.stopScan((ScanCallback) mScanCallback);
		}

	}

	Object mScanCallback;

	void scanFor21() {
		BluetoothLeScanner mBluetoothLeScanner = mBluetoothAdapter
				.getBluetoothLeScanner();
		mScanCallback = new ScanCallback() {
			@Override
			public void onBatchScanResults(List<ScanResult> results) {
				// TODO Auto-generated method stub
				for (ScanResult res : results) {
					System.out.println(res.getDevice().getAddress() + "#"
							+ res.getRssi());

					// AirDevice airDevice = new AirDevice(
					// res.getDevice(), res.getRssi());
					// mLeDeviceListAdapter.addDevice(airDevice);
					// mLeDeviceListAdapter.notifyDataSetChanged();
				}
				super.onBatchScanResults(results);
			}

			@Override
			public void onScanResult(int callbackType, ScanResult result) {
				// TODO Auto-generated method stub
				System.out.println(result.getDevice().getAddress() + "#"
						+ result.getRssi());
				super.onScanResult(callbackType, result);
			}

			@Override
			public void onScanFailed(int errorCode) {
				// TODO Auto-generated method stub
				System.out.println("#######" + errorCode);
				super.onScanFailed(errorCode);
			}
		};
		List<ScanFilter> bleScanFilters = new ArrayList<>();
		bleScanFilters.add(new ScanFilter.Builder().setServiceUuid(
				ParcelUuid.fromString(MilinkGattAttributes.AIR_DATA_SERVICE))
				.build());
		// 当应用程序运行在前台时，建议使用这种模式。SCAN_MODE_LOW_LATENCY
		ScanSettings bleScanSettings = new ScanSettings.Builder().setScanMode(
				ScanSettings.SCAN_MODE_LOW_LATENCY).build();
		mBluetoothLeScanner.startScan(bleScanFilters, bleScanSettings,
				(ScanCallback) mScanCallback);
	}

	// Adapter for holding devices found through scanning.
	private class LeDeviceListAdapter extends BaseAdapter {
		private ArrayList<AirDevice> mLeDevices;
		private LayoutInflater mInflator;

		public LeDeviceListAdapter() {
			super();
			mLeDevices = new ArrayList<AirDevice>();
			mInflator = BleListActivity.this.getLayoutInflater();
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

		public AirDevice getDevice(int position) {
			return mLeDevices.get(position);
		}

		public void clear() {
			mLeDevices.clear();
		}

		@Override
		public int getCount() {
			return mLeDevices.size();
		}

		@Override
		public Object getItem(int i) {
			return mLeDevices.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			ViewHolder viewHolder;
			// General ListView optimization code.
			if (view == null) {
				view = mInflator.inflate(R.layout.listitem_device, null);
				viewHolder = new ViewHolder();
				viewHolder.deviceAddress = (TextView) view
						.findViewById(R.id.device_address);
				viewHolder.deviceName = (TextView) view
						.findViewById(R.id.device_name);
				viewHolder.deviceRssi = (TextView) view
						.findViewById(R.id.device_rssi);
				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}

			AirDevice device = mLeDevices.get(i);
			final String deviceName = device.device.getName();
			if (deviceName != null && deviceName.length() > 0)
				viewHolder.deviceName.setText(deviceName);
			else
				viewHolder.deviceName.setText(R.string.unknown_device);
			viewHolder.deviceAddress.setText(device.device.getAddress());
			viewHolder.deviceRssi.setText(device.rssi + "");

			return view;
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
					mLeDeviceListAdapter.addDevice(airDevice);
					mLeDeviceListAdapter.notifyDataSetChanged();
				}
			});
		}
	};

	static class ViewHolder {
		TextView deviceName;
		TextView deviceAddress;
		TextView deviceRssi;
	}

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

					if (js.getInt("status") == 0) {
						if (share.getBoolean("ispa", false)) {
							String url = PinganRegisterActivity.PINGAN_BASE_URL
									+ "/open/appsvr/health/partner/bind/device/0330000?access_token="
									+ share.getString("pingan_token", "");
							System.out.println(url);
							HashMap<String, String> map = new HashMap<String, String>();
							// o.add(new BasicNameValuePair("partyNo",
							// "007538031055"));
							// o.add(new BasicNameValuePair("partnerCode",
							// "0330000"));
							// o.add(new BasicNameValuePair("deviceType", "3"));
							map.put("partyNo", share.getString("vnumber", ""));
							map.put("partnerCode", "0330000");
							map.put("deviceType", "" + 3);
							String res = HttpUtlis.getRequestForPostJSON(url,
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

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub

	}

}

package com.bandlink.air.ble;

import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import com.bandlink.air.MyLog;
import com.bandlink.air.bluetooth.protocol.LEOutPutStream;
import com.bandlink.air.bluetooth.protocol.LeFileHex;
import com.bandlink.air.bluetooth.protocol.Parser;
import com.bandlink.air.util.Dbutils;

@SuppressLint("NewApi")
public class AirScaleService extends Service {

	private final static String TAG = "AIR_Slim";
	private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";

	private MessageBrocast msgBrocast;
	private String address;

	// 用户参数
	private SharedPreferences sharePre;
	private volatile boolean set_isantilost;
	private volatile boolean set_isnovoice;
	// 寻找手机
	public volatile boolean set_isfindephone;
	// 来电，短信 总开关
	private volatile boolean set_isremind;
	private volatile boolean set_isremind_call;
	private volatile boolean set_isremind_msg;
	//
	private volatile boolean isOnCalling;
	boolean bActiveDisconnect = false;

	public static boolean device_connected = false;
	public static int device_battery = 100;

	// just for lost

	private BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothGattService mBluetoothGattService;
	private String mBluetoothDeviceAddress;
	public BluetoothGatt mBluetoothGatt;
	private int mConnectionState = STATE_DISCONNECTED;

	private static final int REQUEST_ENABLE_BT = 1;

	private static final int STATE_DISCONNECTED = 0;
	private static final int STATE_CONNECTING = 1;
	private static final int STATE_CONNECTED = 2;

	private LEOutPutStream outStream;
	private BluetoothGattDescriptor descriptor;
	public Parser parser;
	// private ReadRssiAndData rssidata;

	public final static String ACTION_DEVICE_AIRSCALE = "LovefitAir.ACTION_DEVICE_AIRSCALE";

	public static final String ACTION_BLE_AIRSCALE_CMD = "LovefitAir.MANAGE_AIRSCALE";
	public static final String ACTION_BLE_AIRSCALE_DATA = "LovefitAir.DATA_AIRSCALE";

	public final static UUID UUID_AIRSCALE_DATA = UUID
			.fromString(MilinkGattAttributes.AIRSCALE_DATA);

	public final static UUID UUID_AIRSCALE_DATA_SERVICE = UUID
			.fromString(MilinkGattAttributes.AIRSCALE_DATA_SERVICE);

	// private volatile ReadRssi rssi;

	private volatile int index = 0;
	private LeFileHex leFileHex;

	boolean bStep2 = false;
	boolean bStep3 = false;

	// 连接状态检查
	private Timer _watchdogScanTimer;
	private int counter_scan = 0;
	private Handler mHandler;
	private boolean mScanning;
	private static final long SCAN_PERIOD = 60000;
	// Implements callback methods for GATT events that the app cares about. For
	// example,
	// connection change and services discovered.
	public static boolean isdebug = false;
	public String session;
	private int rssi = 0;
	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
				int newState) {
			if (bLogAbc)
				MyLog.v("abc", "onConnectionStateChange,status:" + status
						+ ",newState:" + newState);

			if (newState == BluetoothProfile.STATE_CONNECTED) {
				// intentAction = ACTION_GATT_CONNECTED;
				// broadcastUpdate(intentAction);
				LogUtil.e(TAG, "onConnectionStateChange:connect");
				bActiveDisconnect = false;
				mConnectionState = STATE_CONNECTED;

				if (mBluetoothGatt.discoverServices()) {
					MyLog.i(TAG, "Attempting to start service discovery:true");
				} else {
					MyLog.i(TAG, "Attempting to start service discovery:false");
				}

			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				LogUtil.e(TAG, "onConnectionStateChange:disconnect");
				mConnectionState = STATE_DISCONNECTED;

				if (mBluetoothGatt != null) {
					mBluetoothGatt.close();
					mBluetoothGatt = null;
				}

				// showAirConnectionStatusNotification(false);
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				// broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
				LogUtil.e(TAG, "onServicesDiscovered");

				// scale
				mBluetoothGattService = gatt
						.getService(UUID_AIRSCALE_DATA_SERVICE);
				if (mBluetoothGattService != null) {
					// 发出设备绑定成功通知
					broadcastUpdate(ACTION_DEVICE_AIRSCALE, gatt.getDevice()
							.getAddress());
					// 使能通知
					EnableScale();
				}
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				MyLog.v(TAG, "onCharacteristicRead");
			}
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {
			LogUtil.e(TAG, "onCharacteristicChanged");
			if (UUID_AIRSCALE_DATA.equals(characteristic.getUuid())) {
				LogUtil.e(TAG, "onCharacteristicChanged get UUID_AIRSCALE_DATA");
				byte[] ba1 = characteristic.getValue();
				final Intent intent = new Intent(ACTION_BLE_AIRSCALE_DATA);
				intent.putExtra("data", ba1);
				sendBroadcast(intent);

			}

		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			// TODO Auto-generated method stub
			if (bLogAbc)
				MyLog.v("abc", "onCharacteristicWrite ");
			byte[] tmp = null;
			super.onCharacteristicWrite(gatt, characteristic, status);

		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt,
				BluetoothGattDescriptor descriptor, int status) {
			// TODO Auto-generated method stub
			super.onDescriptorWrite(gatt, descriptor, status);

		}

	};

	// 设备扫描回调
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, final int rssiint,
				byte[] scanRecord) {

			String nn = device.getName();
			if (nn.toUpperCase().contains("AIRSCALE")) {
				LogUtil.e(TAG, "get slim device");
				address = device.getAddress();

				if (mScanning) {
					mScanning = false;
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
					// 2.连接设备
					mHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							// if(DEVICE_MODE == 1)
							{
								connect(address);
							}
						}
					}, 1000);
				}
			}
		}
	};

	// rssi timer

	// come from lebu
	Timer mFindTimer = null;

	public void find2connect() {
		try {
			mScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		find2connectWait();
	}

	public void find2connectWait() {

		try {

			if (mLeScanCallback != null && mScanning == false) {
				mScanning = true;
				// disconnectLast_all();
				TimerTask task = new TimerTask() {
					public void run() {
						// execute the task
						mScanning = false;
						try {
							mBluetoothAdapter.stopLeScan(mLeScanCallback);
							if (bLogAbc)
								MyLog.v("abc", "TimerTask ");
						} catch (Exception e) {
							// TODO Auto-generated catch block
							// e.printStackTrace();
						}
					}
				};
				try {
					if (mFindTimer != null) {
						mFindTimer.cancel();
						stopSelf();
					}
					mFindTimer = new Timer();
					mFindTimer.schedule(task, SCAN_PERIOD);
				} catch (Exception e) {
					// TODO: handle exception
				}

				/*
				 * mHandler.postDelayed(new Runnable() {
				 * 
				 * @Override public void run() { mScanning = false; try {
				 * mBluetoothAdapter.stopLeScan(mLeScanCallback); } catch
				 * (Exception e) { // TODO Auto-generated catch block
				 * //e.printStackTrace(); } } }, SCAN_PERIOD);
				 */
				LogUtil.e(TAG, "start scan");
				mBluetoothAdapter.startLeScan(mLeScanCallback);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void broadcastUpdate(final String action) {
		final Intent intent = new Intent(action);
		sendBroadcast(intent);
	}

	private void broadcastUpdate(final String action, String Address) {
		final Intent intent = new Intent(action);
		intent.putExtra("address", Address);
		sendBroadcast(intent);
	}

	private void broadcastUpdate(final String action, int step) {
		final Intent intent = new Intent(action);
		intent.putExtra("step", step);
		sendBroadcast(intent);
	}

	private void broadcastUpdate(final String action, int cur, int all) {
		final Intent intent = new Intent(action);
		intent.putExtra("cur", cur);
		intent.putExtra("all", all);
		sendBroadcast(intent);
	}

	long lastParseTime;

	public class LocalBinder extends Binder {
		public AirScaleService getService() {
			return AirScaleService.this;
		}
	}

	static boolean bLogfile = true;
	static final boolean bLogAbc = false;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();

		LogUtil.e(TAG, "oncreate");
		if (mHandler == null) {
			mHandler = new Handler();
		}
		if (mBluetoothManager == null) {
			mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager == null) {
				MyLog.e(TAG, "Unable to initialize BluetoothManager.");
			}
		}

		mBluetoothAdapter = mBluetoothManager.getAdapter();

		IntentFilter filter = new IntentFilter(ACTION_BLE_AIRSCALE_CMD);

		filter.setPriority(2147483647);
		if (msgBrocast == null) {
			msgBrocast = new MessageBrocast();
		}
		registerReceiver(msgBrocast, filter);

		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// if(DEVICE_MODE == 1)
				{
					LogUtil.e(TAG, "find2connect");
					find2connect();
				}
			}
		}, 1000);

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		LogUtil.e("AIR_SLIM", "ondestroy");

		// disconnect();
		disconnectLast_all();
		if (mHandler != null) {
			mHandler.removeCallbacksAndMessages(null);
		}

		MyLog.e(TAG, "LeService is onDestroy");

		try {
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		} catch (Exception e) {
			// TODO: handle exception
		}

		// 关闭GATT
		if (mBluetoothGatt != null) {
			mBluetoothGatt.close();
			mBluetoothGatt = null;
			mBluetoothGattService = null;
		}
		if (mBluetoothAdapter != null) {
			mBluetoothAdapter = null;
		}
		unregisterReceiver(msgBrocast);

		super.onDestroy();
	}

	Dbutils db;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		// String name = intent.getStringExtra("name");
		flags = Service.START_REDELIVER_INTENT;
		LogUtil.setDefaultFilePath(getBaseContext());
		LogUtil.setLogFilePath("log12/");
		LogUtil.setSyns(true);
		db = new Dbutils(this);
		bLogfile = isdebug = getSharedPreferences("air",
				Context.MODE_MULTI_PROCESS).getBoolean("isdebug", false);
		outStream.bLogFile = bLogfile;
		if (intent != null) {

			int scanflag = 0;

			// address="EE:FE:66:7A:84:95";
			// 11 JSUT FOR GET DATA FROM AIR
			address = intent.getStringExtra("address");

			if (address == null) {
				return super.onStartCommand(intent, flags, startId);
			}

			if (address.length() != 17) {
				return super.onStartCommand(intent, flags, startId);
			}

			if (address != null) {

				// 连接设备
				if (!initialize()) {
					MyLog.e(TAG, "Unable to initialize Bluetooth");
				}

				scanflag = intent.getIntExtra("scanflag", 0);
				switch (scanflag) {
				case 0:
					connect(address);
					break;
				case 1:
					// connect(address);
					if (mConnectionState == STATE_CONNECTED) {
						if (parser != null) {
							parser.Start(5);
						} else {
							find2connect();
						}
					} else {
						find2connect();
					}
				case 3:
					try {
						if (mConnectionState == STATE_CONNECTED) {
							parser.requestData();
						} else {
							find2connect();
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				default:
					break;
				}

			} else {
				MyLog.e(TAG, "Address is null");
			}
			MyLog.d(TAG, "LeService is onStartCommand, is not null");

		} else {
			// 进程被KILL,那么重新启动服务，INTENT IS NULL
			MyLog.e(TAG, "LeService is onStartCommand, is null");
		}

		return super.onStartCommand(intent, flags, startId);

		// return Service.START_REDELIVER_INTENT;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// After using a given device, you should make sure that
		// BluetoothGatt.close() is called
		// such that resources are cleaned up properly. In this particular
		// example, close() is
		// invoked when the UI is disconnected from the Service.
		close();
		return super.onUnbind(intent);
	}

	private final IBinder mBinder = new LocalBinder();

	/**
	 * Initializes a reference to the local Bluetooth adapter.
	 * 
	 * @return Return true if the initialization is successful.
	 */
	public boolean initialize() {
		// For API level 18 and above, get a reference to BluetoothAdapter
		// through
		// BluetoothManager.
		if (mBluetoothManager == null) {
			mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager == null) {
				MyLog.e(TAG, "Unable to initialize BluetoothManager.");
				return false;
			}
		}

		mBluetoothAdapter = mBluetoothManager.getAdapter();
		if (mBluetoothAdapter == null) {
			MyLog.e(TAG, "Unable to obtain a BluetoothAdapter.");
			return false;
		}

		if (!mBluetoothAdapter.isEnabled()) {
			if (!mBluetoothAdapter.isEnabled()) {
				mBluetoothAdapter.enable();
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return true;
	}

	/**
	 * Connects to the GATT server hosted on the Bluetooth LE device.
	 * 
	 * @param address
	 *            The device address of the destination device.
	 * 
	 * @return Return true if the connection is initiated successfully. The
	 *         connection result is reported asynchronously through the
	 *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 *         callback.
	 */

	public boolean connect(final String address) {
		if (mBluetoothAdapter == null || address == null) {
			MyLog.w(TAG,
					"BluetoothAdapter not initialized or unspecified address.");
			return false;
		}

		final BluetoothDevice device = BluetoothAdapter.getDefaultAdapter()
				.getRemoteDevice(address);
		if (device == null) {
			MyLog.w(TAG, "Device not found.  Unable to connect.");
			return false;
		}
		// We want to directly connect to the device, so we are setting the
		// autoConnect
		// parameter to false.
		if (bLogfile)
			LogUtil.i(Calendar.getInstance().getTime().toLocaleString()
					+ "newconnect");
		mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
		if (bLogAbc)
			MyLog.v("abc", "device.connectGatt ");
		// mBluetoothGatt.discoverServices();
		LogUtil.e(TAG, "Trying to create a new connection.");
		mBluetoothDeviceAddress = address;
		mConnectionState = STATE_CONNECTING;

		return true;
	}

	/**
	 * Disconnects an existing connection or cancel a pending connection. The
	 * disconnection result is reported asynchronously through the
	 * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 * callback.
	 */
	public void disconnect() {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			MyLog.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.disconnect();
	}

	/**
	 * After using a given BLE device, the app must call this method to ensure
	 * resources are released properly.
	 */
	public void close() {
		if (mBluetoothGatt == null) {
			return;
		}
		mBluetoothGatt.close();
		mBluetoothGatt = null;
	}

	/**
	 * Request a read on a given {@code BluetoothGattCharacteristic}. The read
	 * result is reported asynchronously through the
	 * {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
	 * callback.
	 * 
	 * @param characteristic
	 *            The characteristic to read from.
	 */
	public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			MyLog.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.readCharacteristic(characteristic);
	}

	/**
	 * Enables or disables notification on a give characteristic.
	 * 
	 * @param characteristic
	 *            Characteristic to act on.
	 * @param enabled
	 *            If true, enable notification. False otherwise.
	 */
	public void setCharacteristicNotification(
			BluetoothGattCharacteristic characteristic, boolean enabled) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			MyLog.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
	}

	/**
	 * Retrieves a list of supported GATT services on the connected device. This
	 * should be invoked only after {@code BluetoothGatt#discoverServices()}
	 * completes successfully.
	 * 
	 * @return A {@code List} of supported services.
	 */
	public List<BluetoothGattService> getSupportedGattServices() {
		if (mBluetoothGatt == null)
			return null;

		return mBluetoothGatt.getServices();
	}

	// 连接体重秤设备DATA
	private void EnableScale() {
		BluetoothGattCharacteristic characteristic;
		characteristic = mBluetoothGattService
				.getCharacteristic(UUID_AIRSCALE_DATA);
		boolean enabled = true;
		if (characteristic != null) {

			mBluetoothGatt.setCharacteristicNotification(characteristic,
					enabled);

			List<BluetoothGattDescriptor> descriptorl = characteristic
					.getDescriptors();
			descriptor = descriptorl.get(0);
			if (descriptor != null) {
				descriptor
						.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
				mBluetoothGatt.writeDescriptor(descriptor);
				LogUtil.e(TAG, "Scale des is send");
			}
		}
	}

	private class MessageBrocast extends BroadcastReceiver {

		@Override
		public void onReceive(final Context context, Intent intent) {
			// System.out.println("receiver message --->>>>");
			// abortBroadcast();
			final String action = intent.getAction();

			if (ACTION_BLE_AIRSCALE_CMD.equals(action)) {
				int type = intent.getIntExtra("type", 0);
				switch (type) {
				case 0:
					bActiveDisconnect = true;
					disconnectLast_all();
					break;
				case 1:
					find2connect();
					break;

				default:
					break;
				}
			}
		}

	}

	private void disconnectLast_all() {

		if (address != null) {
			final BluetoothDevice device = BluetoothAdapter.getDefaultAdapter()
					.getRemoteDevice(address);
			if (device != null) {
				do_disconnectall(device);
				// mDevice = null;
			}
		}
	}

	public void do_disconnectall(BluetoothDevice device) {
		try {
			do_disconnect(device);
			if (mBluetoothGatt != null) {
				mBluetoothGatt.disconnect();
				mBluetoothGatt.close();
				mBluetoothGatt = null;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	public void do_disconnect(BluetoothDevice device) {
		mConnectionState = STATE_DISCONNECTED;
		parser = null;
	}

}

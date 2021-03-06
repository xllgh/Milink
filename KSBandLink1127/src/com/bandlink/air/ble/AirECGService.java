package com.bandlink.air.ble;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.view.WindowManager;

import com.bandlink.air.R;
import com.bandlink.air.bluetooth.protocol.LEOutPutStream;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.MyDate;
import com.bandlink.air.util.MyMediaPlayer;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Util;

@SuppressLint("NewApi")
public class AirECGService extends Service {

	private final static String TAG = "AirECGService";

	private String address;

	// just for lost

	private BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothGattService mBluetoothGattService;
	public BluetoothGatt mBluetoothGatt;
	private int mConnectionState = STATE_DISCONNECTED;

	private static final int REQUEST_ENABLE_BT = 1;

	private static final int STATE_DISCONNECTED = 0;
	private static final int STATE_CONNECTING = 1;
	private static final int STATE_CONNECTED = 2;

	private LEOutPutStream outStream;
	private BluetoothGattDescriptor descriptor;

	public final static String ACTION_DEVICE_AIRTempr = "LovefitAir.ACTION_DEVICE_AIRTempr";

	public static final String ACTION_BLE_AIRECG_DATA = "LovefitAir.DATA_AIRSCALE.ECG";

	public final static UUID UUID_AIRC_DATA = UUID
			.fromString(MilinkGattAttributes.AIRC_DATA);

	public final static UUID AIRC_DATA_CHARA_CONNECT = UUID
			.fromString(MilinkGattAttributes.AIRC_DATA_CHARA_CONNECT);

	public final static UUID UUID_AIRC_DATA_SERVICE = UUID
			.fromString(MilinkGattAttributes.AIRC_DATA_SERVICE);

	// public final static UUID UUID_AIRC_DATA_DES = UUID
	// .fromString(MilinkGattAttributes.AIRC_DATA_DES);

	private Handler mHandler;
	private boolean mScanning;
	private static final long SCAN_PERIOD = 60000;
	// Implements callback methods for GATT events that the app cares about. For
	// example,
	// connection change and services discovered.
	public static boolean isdebug = false;
	public String session;
	private float maxTempe = 0f;
	private ArrayList<Byte> tempeList;
	private long starttime;
	private MyMediaPlayer mediaPlayer;
	private boolean onAlarm = false, onDialogShow = false;
	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
				int newState) {
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				// intentAction = ACTION_GATT_CONNECTED;
				// broadcastUpdate(intentAction);
				LogUtil.e(TAG, "onConnectionStateChange:connect");
				mConnectionState = STATE_CONNECTED;

				if (mBluetoothGatt.discoverServices()) {
					LogUtil.e(TAG, " discovery:true");
				} else {
					LogUtil.e(TAG, " discovery:false");
				}
				if (tempeList != null) {
					tempeList.clear();
				}
				starttime = Calendar.getInstance().getTimeInMillis();
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				LogUtil.e(TAG, "onConnectionStateChange:disconnect");
				mConnectionState = STATE_DISCONNECTED;

				if (mBluetoothGatt != null) {
					mBluetoothGatt.close();
					mBluetoothGatt = null;
				}
				if (tempeList != null && tempeList.size() > 0) {
					db.saveTemperature(MyDate.getFileName(), starttime,
							maxTempe, tempeList);
					tempeList.clear();
				}
				connect(address);
				// showAirConnectionStatusNotification(false);
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				// broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
				// for (BluetoothGattService bb : gatt.getServices()) {
				// LogUtil.e(TAG, "-->:" + bb.getUuid().toString());
				// for (BluetoothGattCharacteristic cc : bb
				// .getCharacteristics()) {
				// LogUtil.e(TAG, "---->:" + cc.getUuid().toString());
				//
				// for (BluetoothGattDescriptor dd : cc.getDescriptors()) {
				// LogUtil.e(TAG, "------>:" + dd.getUuid().toString());
				// }
				// }
				// }
				{

					// scale
					mBluetoothGattService = gatt
							.getService(UUID_AIRC_DATA_SERVICE);
					if (mBluetoothGattService != null) {
						// 发出设备绑定成功通知
						broadcastUpdate(ACTION_DEVICE_AIRTempr, gatt
								.getDevice().getAddress());
						// 链接确认
						// EnableScale();
						// connectConfirm();
						EnableScale();
					} else {
						LogUtil.e(TAG, "can not find:" + UUID_AIRC_DATA_SERVICE);
					}

				}
			} else {
				LogUtil.e(TAG, "BluetoothGatt:failed");
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {

			LogUtil.e(
					TAG,
					"onCharacteristicRead:"
							+ Converter.byteArrayToHexString(characteristic
									.getValue()));

		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {

			if (UUID_AIRC_DATA.equals(characteristic.getUuid())) {

				byte[] ba1 = characteristic.getValue();
				// LogUtil.e("--onCharacteristicChanged->" +
				// Converter.byteArrayToHexString(ba1));
				Intent intent = new Intent(ACTION_BLE_AIRECG_DATA);
				intent.putExtra("data", ba1);
				sendBroadcast(intent);
				// if(ba1.length==20){
				// System.arraycopy(ba1, 0, dst, index*20, ba1.length);
				// index++;
				// }else{
				// System.arraycopy(ba1, 0, dst, index*20, ba1.length);
				// index =0;
				//
				//
				// }
				// if (ba1 != null && ba1.length == 2) {
				// gatt.readRemoteRssi();
				// LogUtil.e(TAG,
				// "onCharacteristicChanged get UUID_AIRSCALE_DATA"
				// + Converter.byteArrayToHexString(ba1));
				// final Intent intent = new Intent(ACTION_BLE_AIRTempr_DATA);
				// tempeList.add(ba1[0]);
				// tempeList.add(ba1[1]);
				// float x = ((ba1[0] << 8) + ba1[1]) / 100f;
				// if (x > maxTempe) {
				// maxTempe = x;
				// }
				// if (x > shareUtil.getAlarmTempe()) {
				// // AudioManager mAudioManager = (AudioManager)
				// // getSystemService(Context.AUDIO_SERVICE);
				// // int currentVolume = mAudioManager
				// // .getStreamVolume(AudioManager.STREAM_MUSIC);
				// // mAudioManager
				// // .setStreamVolume(
				// // AudioManager.STREAM_MUSIC,
				// // Math.round(mAudioManager
				// // .getStreamMaxVolume(AudioManager.STREAM_MUSIC) * 2f /
				// // 3f),
				// // 1);
				// // showAirFoundNotification();
				// if (!onAlarm) {
				// onAlarm = true;
				// mediaPlayer = new MyMediaPlayer(AirECGService.this,
				// false);
				// mediaPlayer.setOnRingOverListener(new OnRingOver() {
				//
				// @Override
				// public void onRringOver() {
				// // TODO Auto-generated method stub
				// onAlarm = false;
				// }
				// });
				// mediaPlayer.start();
				// }
				// if (!onDialogShow) {
				// onDialogShow = true;
				// handler.obtainMessage(0).sendToTarget();
				// }
				//
				// }
				// intent.putExtra("data", x);
				// sendBroadcast(intent);
				// }

			}
			// connectConfirm();
			// BluetoothGattDescriptor descriptor = characteristic
			// .getDescriptor(UUID_AIRC_DATA_DES);
			// if (descriptor != null) {
			// LogUtil.e(TAG, "enable 2902 try keep connect");
			// descriptor
			// .setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			// mBluetoothGatt.writeDescriptor(descriptor);
			// }

		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			// TODO Auto-generated method stub

			byte[] a = characteristic.getValue();
			LogUtil.e(
					TAG,
					"onCharacteristicWrite :"
							+ Converter.byteArrayToHexString(a));
			if (a != null && a.length == 1 && a[0] == 0x10) {
				// EnableScale();
			}
			super.onCharacteristicWrite(gatt, characteristic, status);

		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt,
				BluetoothGattDescriptor descriptor, int status) {
			// TODO Auto-generated method stub
			byte[] a = descriptor.getValue();
			LogUtil.e(TAG,
					"onDescriptorWrite :" + Converter.byteArrayToHexString(a));

			super.onDescriptorWrite(gatt, descriptor, status);

		}

	};
	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 0) {
				show();
			}
		};
	};

	void show() {

		AlertDialog.Builder builder = new AlertDialog.Builder(
				Util.getThemeContext(AirECGService.this));
		builder.setTitle(R.string.warning);
		builder.setMessage(R.string.hight_tempe_warning);

		builder.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						onDialogShow = true;
						onAlarm = true;
						if (mediaPlayer != null) {
							try {
								mediaPlayer.Stop();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				});
		builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO Auto-generated method stub
				onDialogShow = false;
			}
		});
		AlertDialog dialog = builder.create();
		dialog.getWindow().setType(
				(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
		dialog.show();

	}

	// 设备扫描回调
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, final int rssiint,
				byte[] scanRecord) {

			String nn = device.getName();
			LogUtil.e(TAG, nn + "--->" + device.getAddress());
			if (nn.toUpperCase().contains("LSD")
					&& nn.toUpperCase().contains("BLE")) {
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

	long rssi_calltime, rssi_readtime;

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
								LogUtil.e(TAG, "TimerTask ");
						} catch (Exception e) {
							// TODO Auto-generated catch block
							// e.printStackTrace();
						}
					}
				};
				try {
					if (mFindTimer != null) {
						mFindTimer.cancel();
						// stopSelf();
					}
					mFindTimer = new Timer();
					mFindTimer.schedule(task, SCAN_PERIOD);
				} catch (Exception e) {
					// TODO: handle exception
				}
				mBluetoothAdapter.startLeScan(mLeScanCallback);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void broadcastUpdate(final String action, String Address) {
		final Intent intent = new Intent(action);
		intent.putExtra("address", Address);
		sendBroadcast(intent);
	}

	public class LocalBinder extends Binder {
		public AirECGService getService() {
			return AirECGService.this;
		}
	}

	static boolean bLogfile = true;
	static final boolean bLogAbc = false;
	private SharePreUtils shareUtil;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();

		LogUtil.e(TAG, "oncreate");

		if (mBluetoothManager == null) {
			mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager == null) {
				LogUtil.e(TAG, "Unable to initialize BluetoothManager.");
			}
		}

		mBluetoothAdapter = mBluetoothManager.getAdapter();

		shareUtil = SharePreUtils.getInstance(this);
		// startKeeper();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		// disconnect();
		if (tempeList != null && tempeList.size() > 0) {
			db.saveTemperature(MyDate.getFileName(), starttime, maxTempe,
					tempeList);
			tempeList.clear();
		}
		disconnectLast_all();
		if (mHandler != null) {
			mHandler.removeCallbacksAndMessages(null);
		}

		LogUtil.e(TAG, "LeService is onDestroy");

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
		tempeList = new ArrayList<Byte>();
		db = new Dbutils(this);
		bLogfile = isdebug = getSharedPreferences("air",
				Context.MODE_MULTI_PROCESS).getBoolean("isdebug", false);
		outStream.bLogFile = bLogfile;

		if (mHandler == null) {
			mHandler = new Handler();
		}
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
		onAlarm = false;
		onDialogShow = false;
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
				LogUtil.e(TAG, "Unable to initialize BluetoothManager.");
				return false;
			}
		}

		mBluetoothAdapter = mBluetoothManager.getAdapter();
		if (mBluetoothAdapter == null) {
			LogUtil.e(TAG, "Unable to obtain a BluetoothAdapter.");
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
			LogUtil.e(TAG,
					"BluetoothAdapter not initialized or unspecified address.");
			return false;
		}

		final BluetoothDevice device = BluetoothAdapter.getDefaultAdapter()
				.getRemoteDevice(address);
		if (device == null) {
			LogUtil.e(TAG, "Device not found.  Unable to connect.");
			return false;
		}
		// We want to directly connect to the device, so we are setting the
		// autoConnect
		// parameter to false.
		if (bLogfile)
			LogUtil.e(Calendar.getInstance().getTime().toLocaleString()
					+ "newconnect");
		mBluetoothGatt = device.connectGatt(this, false, mGattCallback);

		LogUtil.e(TAG, "device.connectGatt ");
		// mBluetoothGatt.discoverServices();
		LogUtil.e(TAG, "Trying to create a new connection.");
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
			LogUtil.e(TAG, "BluetoothAdapter not initialized");
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
			LogUtil.e(TAG, "BluetoothAdapter not initialized");
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
			LogUtil.e(TAG, "BluetoothAdapter not initialized");
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
				.getCharacteristic(UUID_AIRC_DATA);
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
				LogUtil.e(TAG, "enable fff2");
			}
		}
	}

	Timer timerKeep;
	int count = 0;

	private void startKeeper() {
		timerKeep = new Timer();
		timerKeep.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (mConnectionState == STATE_DISCONNECTED) {
					count++;
					if (count > 20) {
						find2connect();
						count = 0;
					}
				}
			}
		}, 1000, 1000);
	}

	private void connectConfirm() {
		BluetoothGattCharacteristic characteristic;
		characteristic = mBluetoothGattService
				.getCharacteristic(AIRC_DATA_CHARA_CONNECT);
		boolean enabled = true;
		if (characteristic != null) {

			mBluetoothGatt.setCharacteristicNotification(characteristic,
					enabled);
			characteristic.setValue(new byte[] { 0x10 });
			mBluetoothGatt.writeCharacteristic(characteristic);
		} else {
			LogUtil.e(TAG, "can not find:" + AIRC_DATA_CHARA_CONNECT);
		}
	}

	private void disconnectLast_all() {
		if (bLogAbc)
			LogUtil.e(TAG, "disconnectLast_all ");
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
	}

}

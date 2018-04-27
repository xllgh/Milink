package com.bandlink.air.ble;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;
import com.bandlink.air.AirPreferenceActivity;
import com.bandlink.air.BuildConfig;
import com.bandlink.air.FileUpdateActivity;
import com.bandlink.air.MilinkApplication;
import com.bandlink.air.MyLog;
import com.bandlink.air.R;
import com.bandlink.air.bluetooth.protocol.LEOutPutStream;
import com.bandlink.air.bluetooth.protocol.LeFileHex;
import com.bandlink.air.bluetooth.protocol.Parser;
import com.bandlink.air.card.NJTransactionDetail;
import com.bandlink.air.card.NetworkingOperator;
import com.bandlink.air.card.ProcessResult;
import com.bandlink.air.simple.SimpleHomeFragment;
import com.bandlink.air.util.DbContract.NANJING_CARD_ARGS;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.MyDate;
import com.bandlink.air.util.MyMediaPlayer;
import com.bandlink.air.util.MyMediaPlayer.OnRingOver;
import com.bandlink.air.util.NotificationUtils;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Util;

@SuppressLint("NewApi")
public class BluetoothLeService extends Service implements OnRingOver {

	private final static String TAG = "AIRBLE";
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
	private volatile boolean set_isremind_call;
	private volatile boolean set_isremind_msg;
	//
	private volatile boolean isOnCalling;
	boolean bActiveDisconnect = false;

	public static boolean device_connected = false;
	public static int device_battery = 100;

	// just for lost
	private MyMediaPlayer mediaPlayer;
	Vibrator mVibrator = null;
	public volatile static boolean onLostAlarm = false;
	public volatile static boolean isOnFinding;
	boolean bRssiReport = false;
	private volatile int losttype = 0;
	private volatile int losttype_last = 0;
	private volatile float rssi_ave = 0f;
	private volatile int rssilog_counter = 0;
	private volatile int lost_distance;
	private static int rssi_lost_thres1 = -90;
	private static final int rssi_filter_rank = 8;
	private int rssi_filter_counter = 0;
	private int rssi_filter_i = 0;
	private int[] rssi_filter = new int[rssi_filter_rank];

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

	private static final int STATE_FW_START = 0;
	private static final int STATE_FW_RECEIVE_INIT = 1;
	private static final int STATE_FW_RECEIVE_FW = 2;
	private static final int STATE_FW_RECEIVE_FW_2 = 22;
	private static final int STATE_FW_RECEIVE_FW_3 = 23;
	private static final int STATE_FW_VALIDATE = 3;
	private static final int STATE_FW_VALIDATE_2 = 32;
	private static final int STATE_FW_ACTIVATE_N_RESET = 4;
	private static final int STATE_FW_SYS_RESET = 5;
	private static final int STATE_FW_IMAGE_SIZE_REQ = 6;
	private static final int STATE_FW_PKT_RCPT_NOTIF_REQ = 7;
	private static final int STATE_FW_RESPONSE = 8;
	private static final int STATE_FW_PKT_RCPT_NOTIF = 9;
	private static int STATE_FW;
	// 1 普通模式 2升级模式
	private static int DEVICE_MODE;
	public static boolean isCon = false;
	private LEOutPutStream outStream;
	private BluetoothGattDescriptor descriptor;
	public Parser parser;
	// private ReadRssiAndData rssidata;
	public final static String PACKETNAME = "com.bandlink.air.ble.";
	public final static String ACTION_GATT_CONNECTED = PACKETNAME + "LovefitAir.ACTION_GATT_CONNECTED";
	public final static String ACTION_GATT_DISCONNECTED = PACKETNAME + "ACTION_GATT_DISCONNECTED";
	public final static String ACTION_GATT_SERVICES_DISCOVERED = PACKETNAME
			+ "LovefitAir.ACTION_GATT_SERVICES_DISCOVERED";
	public final static String ACTION_DATA_AVAILABLE = PACKETNAME + "LovefitAir.ACTION_DATA_AVAILABLE";
	public final static String ACTION_DEVICE_AIR = PACKETNAME + "LovefitAir.ACTION_DEVICE_AIR";
	public final static String ACTION_BT_REBOOT = PACKETNAME + "LovefitAir.ACTION_BT_REBOOT";
	public final static String ACTION_FW_PROGRESS = PACKETNAME + "LovefitAir.ACTION_FW_PROGRESS";
	public final static String ACTION_FW_STEPS = PACKETNAME + "LovefitAir.ACTION_FW_STEPS";
	public final static String EXTRA_DATA = PACKETNAME + "LovefitAir.EXTRA_DATA";
	// nfc card
	public final static String ACTION_BLE_CARD = PACKETNAME + "LovefitAir.NFC.CARD";
	public final static String ACTION_BLE_CARD_REVEIVER = PACKETNAME + "LovefitAir.NFC.CARD.rec";
	public final static String ACTION_BLE_CARD_READ = PACKETNAME + "LovefitAir.NFC.CARD.READ";
	public final static String ACTION_BLE_CARD_TURNON = PACKETNAME + "LovefitAir.NFC.CARD.turnon";
	public final static String ACTION_BLE_CARD_UPDATE = PACKETNAME + "LovefitAir.NFC.CARD.update";
	public final static String ACTION_BLE_BIND_DEVICE_CHANGED = PACKETNAME + "LovefitAir.bind.changed";

	public static final String ACTION_BLE_MANAGE_CMD = PACKETNAME + "LovefitAir.MANAGE";
	public final static String ACTION_BT_CONFIG_READ = PACKETNAME + "LovefitAir.ACTION_BT_CONFIG_READ";
	public final static String ACTION_BT_CONFIG_WRITE = PACKETNAME + "LovefitAir.ACTION_BT_CONFIG_WRITE";
	public static final String ACTION_BLE_CONFIG_CMD = PACKETNAME + "LovefitAir.CONFIG_WRITE";
	public static final String ACTION_BLE_CONFIG_READ_RESPONSE = PACKETNAME + "LovefitAir.CONFIG_READ_RESPONSE";
	public static final String ACTION_BLE_DEVICE_ALARM = PACKETNAME + "LovefitAir.DEVICE_ALARM";

	public static final String ACTION_AIR_YAOYAO = PACKETNAME + "LovefitAir.AIR_YAOYAO";
	public static final String ACTION_CAMERA_PICTURE = PACKETNAME + "LovefitAir.CAMERA_PICTURE";

	public static final String ACTION_PREF_CHANGED = PACKETNAME + "LovefitAir.PREF_CHANGED";
	public static final String ACTION_AIR_CONNECTION_STATUS = PACKETNAME + "LovefitAir.AIR_CONNECTION_STATUS";
	public static final String ACTION_AIR_RSSI_STATUS = PACKETNAME + "LovefitAir.AIR_RSSI_STATUS";
	public static final String ACTION_AIR_CONTROL = PACKETNAME + "com.air.ctrl";
	public final static UUID UUID_AIR_DATA_SERVICE = UUID.fromString(MilinkGattAttributes.AIR_DATA_SERVICE);
	public final static UUID UUID_AIR_DATA_F1 = UUID.fromString(MilinkGattAttributes.AIR_DATA_F1);
	public final static UUID UUID_AIR_DATA_F2 = UUID.fromString(MilinkGattAttributes.AIR_DATA_F2);

	public final static UUID UUID_AIR_FW_SERVICE = UUID.fromString(MilinkGattAttributes.AIR_FW_SERVICE);
	public final static UUID UUID_AIR_FW_31 = UUID.fromString(MilinkGattAttributes.AIR_FW_31);
	public final static UUID UUID_AIR_FW_32 = UUID.fromString(MilinkGattAttributes.AIR_FW_32);
	// 公交读卡
	public final static UUID UUID_PAY_SERVICE = UUID.fromString("0000ffE0-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_PAY_FFE2 = UUID.fromString("0000ffE2-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_PAY_FFE1 = UUID.fromString("0000ffE1-0000-1000-8000-00805f9b34fb");

	private BluetoothGattService mCardGattService;
	// private volatile ReadRssi rssi;

	private volatile int index = 0, bytei = 0;
	private LeFileHex leFileHex;

	boolean bStep2 = false;
	boolean bStep3 = false;

	// 连接状态检查
	private Timer _watchdogScanTimer;
	private int counter_scan = 0;
	private Handler mHandler;
	private boolean mScanning;
	private static final long SCAN_PERIOD = 40000;
	// Implements callback methods for GATT events that the app cares about. For
	// example,
	// connection change and services discovered.
	public static boolean isdebug = false;
	public String session;
	private int rssi = 0;
	private boolean hasFW = false;
	BluetoothGatt sGatt = null;
	PowerManager pm = null;
	PowerManager.WakeLock wl = null;
	StringBuffer recordNj = new StringBuffer();

	public static int getValueFromLSBHex(String hexData) {
		return Converter.getIntFromHexString(hexData.substring(0, 8), ByteOrder.LITTLE_ENDIAN);
	}

	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override
		public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
			// TODO Auto-generated method stub
			super.onReadRemoteRssi(gatt, rssi, status);
			// mBluetoothGatt = gatt;
			BluetoothLeService.this.rssi = rssi;
			rssi_readtime = System.currentTimeMillis();
			if (DEVICE_MODE == 1) {
				if (bLogfile)
					LogUtil.i(
							Calendar.getInstance().getTime().toLocaleString() + "onrssi:  " + rssi + "    " + rssi_ave);

				// initLostDistance();
				// lost_distance = MilinkApplication.air_lost_distance;
				// MyLog.v(TAG, "aaaa " +
				// Calendar.getInstance().getTime().toLocaleString() + " : " +
				// rssi_readtime + "/" + rssi_calltime + "/" + rssi);
				/*
				 * if (lost_distance < 4) { rssi_lost_thres1 = -91; } else if
				 * (lost_distance < 7) { rssi_lost_thres1 = -94; } else {
				 * rssi_lost_thres1 = -98; }
				 */
				if (lost_distance < 2) {
					rssi_lost_thres1 = -96;
				} else {
					rssi_lost_thres1 = -118;
				}
				if (rssi_filter_counter < rssi_filter_rank)
					rssi_filter_counter++;
				rssi_filter[rssi_filter_i] = rssi;
				rssi_filter_i++;
				if (rssi_filter_i >= rssi_filter_rank)
					rssi_filter_i = 0;
				int rssi_ave_sum = 0;
				for (int i = 0; i < rssi_filter_rank; i++)
					rssi_ave_sum += rssi_filter[i];
				rssi_ave = rssi_ave_sum / rssi_filter_rank;

				losttype = 0;
				if (rssi_filter_counter < rssi_filter_rank) {
					if (rssi_filter_counter > 3) {
						if (rssi_filter[rssi_filter_i] < rssi_lost_thres1
								&& rssi_filter[rssi_filter_i - 1] < rssi_lost_thres1
								&& rssi_filter[rssi_filter_i - 2] < rssi_lost_thres1) {
							losttype = 1;
						}
					}
				} else {
					if (rssi_ave < rssi_lost_thres1) {
						losttype = 1;
					}

					if (losttype_last != 0) {
						// 设备进入范围
						if ((rssi > rssi_lost_thres1 || losttype == 0))
							onDeviceIn();
					}

				}

				if (lost_distance < 2 && (losttype != 0 && losttype_last == 0)) {
					// 连接断开，触发防丢
					onDeviceLost();
					if (db == null) {
						db = new Dbutils(BluetoothLeService.this);
					}
					int l = db.getAirlostTimes(MyDate.getFileName());
					l++;
					db.setAirRecorderlost(l, MyDate.getFileName());
				}
				// if(MilinkApplication.bReportRssi)
				// if(((MilinkApplication)getApplication()).getbReportRssi())
				// if(bRssiReport)
				{
					if (!bActiveDisconnect) {
						Intent intent = new Intent(ACTION_AIR_RSSI_STATUS);
						intent.putExtra("rssi_c", rssi);
						intent.putExtra("rssi_a", (int) rssi_ave);

						intent.putExtra("status", 1);
						intent.putExtra("device_battery", device_battery);
						sendBroadcast(intent);
					}
					// MyLog.v(TAG, "RSSI:" + rssi + ";STATUS:" + status);
				}
			}
		}

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			String intentAction;
			rssi_readtime = 0;
			System.out.println("---->" + newState);
			if (bLogfile)
				LogUtil.e(Calendar.getInstance().getTime().toLocaleString() + "onConnectionStateChange:  " + newState);

			if (bLogAbc)
				MyLog.e("abc", "onConnectionStateChange,status:" + status + ",newState:" + newState);
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				if (bLogfile)
					LogUtil.e("---连上设备---" + Util.getTimeMMString());
				// intentAction = ACTION_GATT_CONNECTED;
				// broadcastUpdate(intentAction);
				// //连接
				// Intent intent = new Intent(ACTION_AIR_RSSI_STATUS);
				// intent.putExtra("rssi_c", rssi);
				// intent.putExtra("rssi_a", (int) rssi_ave);
				//
				// intent.putExtra("status", 1);
				// intent.putExtra("device_battery", device_battery);
				// sendBroadcast(intent);

				bActiveDisconnect = false;
				isCon = true;
				mConnectionState = STATE_CONNECTED;
				onDeviceLostCancle();

				auto_connection_wait_counter = 120;
				MyLog.d(TAG, "Connected to GATT server.");
				// Attempts to discover services after successful connection.
				// MyLog.d(TAG, "Attempting to start service discovery:");
				MyLog.v("log1016", "discover Services 当前模式:" + DEVICE_MODE);
				if (mBluetoothGatt.discoverServices()) {
					MyLog.i(TAG, "Attempting to start service discovery:true");
				} else {
					MyLog.i(TAG, "Attempting to start service discovery:false");
				}
				// 发出设备绑定成功通知
				broadcastUpdate(ACTION_DEVICE_AIR, gatt.getDevice().getAddress());
				/*
				 * if(rssi!=null) { rssi.StopThread(); } rssi=new
				 * ReadRssi(gatt); rssi.start();
				 */
				// 用用显示连接状态
				Parser.updatenotification(BluetoothLeService.this, (float) 0, (int) -1, (int) 0, (int) 0, true);

			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

				mConnectionState = STATE_DISCONNECTED;
				MyLog.e(TAG, "Disconnected from GATT server.");
				// 这边需要判断下模式，是否升级结束的关闭
				// intentAction = ACTION_GATT_DISCONNECTED;
				// broadcastUpdate(intentAction);
				sharePre.edit().remove("soft_version").commit();
				isCon = false;
				if (STATE_FW_VALIDATE_2 == STATE_FW) {
					// 升级完成，关闭蓝牙
					// 新的版直接第五步
					if (!hasFW) {
						broadcastUpdate(ACTION_FW_STEPS, 4);
						disableBT(false);
					} else {
						broadcastUpdate(ACTION_FW_STEPS, 5);
					}
				} else {
					if (mBluetoothGatt != null) {
						mBluetoothGatt.close();
						mBluetoothGatt = null;
					}
					Intent intent = new Intent(ACTION_AIR_RSSI_STATUS);

					intent.putExtra("status", 0);
					sendBroadcast(intent);

					if (mConnectionState == STATE_DISCONNECTED && DEVICE_MODE == 1 && !bActiveDisconnect
							&& address != null) {
						// connect(address);
						// find2connect();
						auto_connection_wait_counter = 0;
						auto_connection_counter = 60;
					}

					if (DEVICE_MODE == 1) {
						if (!bActiveDisconnect) {
							// 连接断开，触发防丢
							// onDeviceLost();
						}
						// 读取信号强度
						// if (rssidata != null) {
						// rssidata.isConnected = false;
						// }
					}
					bActiveDisconnect = false;

					if (DEVICE_MODE == 2) {
						// connect(address);
						if (afterSwitch) {
							find2connect();
							afterSwitch = false;
						}

					}
					// 断开时，是通知模式则立即刷新通知
					Parser.updatenotification(BluetoothLeService.this, (float) 0, (int) -1, (int) 0, (int) 0, false);
				}
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			sGatt = gatt;
			// 标记在device_mode为1时是否有升级服务
			if (mBluetoothGattService == null) {
				mBluetoothGattService = gatt.getService(UUID_AIR_FW_SERVICE);

				if (mBluetoothGattService != null && DEVICE_MODE == 1) {
					// 新版本有升级服务
					hasFW = true;
					sharePre.edit().putBoolean("hasfw", true).commit();
				} else if (DEVICE_MODE == 1) {
					hasFW = false;
					sharePre.edit().putBoolean("hasfw", false).commit();
				}
			}
			if (status == BluetoothGatt.GATT_SUCCESS) {
				// broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
				if (DEVICE_MODE == 1) {
					// 接收到服务，检查是否有lovefit 服务，如果有表示是lovefit 设备
					mBluetoothGattService = gatt.getService(UUID_AIR_DATA_SERVICE);
					mCardGattService = gatt.getService(UUID_PAY_SERVICE);

					if (mCardGattService != null) {
						// 是快刷
						enableFFE2();
						appShare.edit().putBoolean("isKs", true).commit();
						// 修改菜单
						Intent intent = new Intent(ACTION_BLE_BIND_DEVICE_CHANGED);
						intent.putExtra("isKs", true);
						sendBroadcast(intent);
					} else if (mBluetoothGattService != null) {
						EnableFFF2();
						appShare.edit().putBoolean("isKs", false).commit();
						// for lost
						rssi_filter_counter = 0;
						rssi_filter_i = 0;
					}

					if (mBluetoothGattService == null) {
						mBluetoothGattService = gatt.getService(UUID_AIR_FW_SERVICE);

						if (mBluetoothGattService != null) {
							// 发出设备绑定成功通知
							broadcastUpdate(ACTION_DEVICE_AIR, gatt.getDevice().getAddress());
							// MyLog.i(TAG, "onServicesDiscovered 1530");
						}
						mBluetoothGattService = null;

					}
				} else {

					// if(!bStep2)
					{
						bStep2 = true;
						mBluetoothGattService = gatt.getService(UUID_AIR_DATA_SERVICE);
						// tt
						if (mBluetoothGattService != null) {
							// 发出设备绑定成功通知
							broadcastUpdate(ACTION_DEVICE_AIR, gatt.getDevice().getAddress());
							// 连接设备

							EnableFFF2();

							// MyLog.i(TAG,
							// "mode 2 onServicesDiscovered EnableFFF2");
							// tt
							return;

						} else {
							// MyLog.e(TAG,"mode 2 FFF0 onServicesDiscovered Not
							// SUCCESS,The status is: "
							// + status);
						}
					}
					// if(!bStep3)
					{
						bStep3 = true;
						mBluetoothGattService = gatt.getService(UUID_AIR_FW_SERVICE);
						if (mBluetoothGattService != null) {
							// 升级固件
							Enabel1531();
							// MyLog.i(TAG,
							// "mode 2 onServicesDiscovered Enable1531");
						} else {
							// MyLog.e(TAG,"1530 onServicesDiscovered Not
							// SUCCESS,The status is: "+
							// status);
						}
					}
				}
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
				MyLog.v(TAG, "onCharacteristicRead");
			}
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
			if (bLogAbc)
				MyLog.v("abc", "onCharacteristicChanged ");
			if (UUID_AIR_FW_31.equals(characteristic.getUuid())) {
				BluetoothGattCharacteristic fwc;
				byte[] data = characteristic.getValue();
				if (data.length > 3) {
					if (BuildConfig.DEBUG) {
						Log.e("UPDATE",
								"get:" + data[0] + "/" + data[1] + "/" + data[2] + "/" + data[3] + "/" + data[4]);
					}
				} else if (BuildConfig.DEBUG) {
					Log.e("UPDATE", "get:" + data[0] + "/" + data[1] + "/" + data[2]);
				}
				// MyLog.v("jjj", "byte get: " + data.length + ": " +
				// Converter.byteArrayToHexString(data));
				if (data.length == 3) {

					if ((data[0] == 0x10) && (data[1] == 0x03) && (data[2] == 0x01)) {
						// 升级文件发送完成，下面发送校验
						if (BuildConfig.DEBUG) {
							Log.e("UPDATE", "发送校验 ");
						}
						STATE_FW = STATE_FW_VALIDATE;
						characteristic.setValue(new byte[] { 4 });
						gatt.writeCharacteristic(characteristic);
						return;
					}

					if ((data[0] == 0x10) && (data[1] == 0x04) && (data[2] == 0x01)) {
						// 校验成功，发送升级
						if (BuildConfig.DEBUG) {
							Log.e("UPDATE", "校验完成");
						}

						// 发送5完成升级，否则停在100%
						appShare.edit().putBoolean("airupdate", false).commit();
						STATE_FW = STATE_FW_VALIDATE_2;
						characteristic.setValue(new byte[] { 5 });
						gatt.writeCharacteristic(characteristic);
						broadcastUpdate(ACTION_FW_STEPS, 5);
						if (BuildConfig.DEBUG) {
							Log.e("UPDATE", "最后一步");
						}
						// 升级完成清除cache
						if (!hasFW) {
							refreshDeviceCache(mBluetoothGatt, false);
						}

						new Timer().schedule(new TimerTask() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								if (BuildConfig.DEBUG) {
									Log.e("UPDATE", "重启服务");
								}
								Intent i = new Intent(BluetoothLeService.this, BleScanService.class);
								i.putExtra("address", address);
								i.putExtra("order", true);
								startService(i);
							}
						}, 3000);
						return;

					}
					if ((data[0] == 0x10) && (data[1] == 0x04) && (data[2] == 0x05)) {
						// 校验失败
						if (BuildConfig.DEBUG) {
							Log.e("UPDATE", "校验失败");
						}
						// STATE_FW = 0;
						// DEVICE_MODE = 1;
						broadcastUpdate(ACTION_FW_STEPS, 13);
						// Context con = getApplicationContext();
						// if(Build.VERSION.SDK_INT>=11){
						// con = new ContextThemeWrapper(con,
						// android.R.style.Theme_Holo_Light);
						// }
						// AlertDialog.Builder ab = new
						// AlertDialog.Builder(con);
						// ab.setTitle(R.string.warning);
						// ab.setMessage(R.string.air_fw_step_13);
						// ab.setPositiveButton(R.string.close, null);
						// ab.create().show();
						return;

					}
				}
				MyLog.v(TAG, "onCharacteristicChanged");
				switch (STATE_FW) {
				case STATE_FW_START:
					if (!((data[0] == 0x10) && (data[1] == 0x01) && (data[2] == 0x01))) {
						break;
					}
					STATE_FW = STATE_FW_RECEIVE_INIT;
					// MyLog.v("jjj", "new start: ");
					if (BuildConfig.DEBUG) {
						Log.e("UPDATE", "启动确认，收到长度，写入2，进入初始化");
					}
					characteristic.setValue(new byte[] { 2 });
					gatt.writeCharacteristic(characteristic);
					break;
				case STATE_FW_RECEIVE_INIT:

					// if(!updateHasInit){
					// if(BuildConfig.DEBUG){
					// Log.e("UPDATE", "没有初始化？再来一次");
					// }
					// STATE_FW = STATE_FW_RECEIVE_INIT;
					// // MyLog.v("jjj", "new start: ");
					// characteristic.setValue(new byte[] { 2 });
					// gatt.writeCharacteristic(characteristic);
					// break;
					// }else{
					// updateHasInit = false;
					// }
					if ((data[0] == 0x10) && (data[1] == 0x02) && (data[2] == 0x01)) {
						if (BuildConfig.DEBUG) {
							Log.e("UPDATE", "初始化确认写3，控制设备进入接收有效数据进行upgrade状态");
						}
						STATE_FW = STATE_FW_RECEIVE_FW;
						index = 0;
						bytei = 0;
						characteristic.setValue(new byte[] { 3 });
						gatt.writeCharacteristic(characteristic);
					}
					break;
				case STATE_FW_RECEIVE_FW_3:

					if (data[0] != 0x11)
						break;
					if (BuildConfig.DEBUG) {
						Log.e("UPDATE", "传输确认");
					}
					fwc = mBluetoothGatt.getService(UUID_AIR_FW_SERVICE).getCharacteristic(UUID_AIR_FW_32);
					byte[] tmp = null;
					while (index < LeFileHex.buffer.length) {
						byte[] data1 = LeFileHex.buffer[index];

						if (data1[3] == 0) {
							MyLog.v(TAG, "FW INDEX:" + index);
							index++;
							tmp = new byte[data1[0]];
							bytei += tmp.length;
							System.arraycopy(data1, 4, tmp, 0, data1[0]);

							break;
						} else {
							MyLog.v(TAG, "FW INDEX IS EMPTY:" + index);
							index++;
							continue;
						}
					}
					if (tmp != null) {
						fwc.setValue(tmp);
						mBluetoothGatt.writeCharacteristic(fwc);
						MyLog.v("jjj", index + "/" + uuu + " ,4 tmp: " + tmp.length + ": "
								+ Converter.byteArrayToHexString(tmp));

					}
				// if (index % 10 == 0)
				{
					broadcastUpdate(ACTION_FW_PROGRESS, bytei, LeFileHex.datalen);
				}
					uuu = uuuMax - 1;
					break;
				case STATE_FW_VALIDATE:

					break;
				default:
					break;
				}
			}
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			// TODO Auto-generated method stub
			byte[] tmp = null;
			super.onCharacteristicWrite(gatt, characteristic, status);
			BluetoothGattCharacteristic fwc;
			if (UUID_PAY_FFE1.equals(characteristic.getUuid())) {
				if (output7816 != null) {
					output7816.ContinueSend();
					if (bLogfile) {
						LogUtil.e("XXXX", "7816 write");
					}
					return;
				}
			}
			if (UUID_AIR_FW_31.equals(characteristic.getUuid())) {
				switch (STATE_FW) {
				case STATE_FW_START:
					if (BuildConfig.DEBUG) {
						Log.e("UPDATE", "启动，写入文件长度");
					}
					fwc = mBluetoothGatt.getService(UUID_AIR_FW_SERVICE).getCharacteristic(UUID_AIR_FW_32);
					fwc.setValue(LeFileHex.byteslen);
					mBluetoothGatt.writeCharacteristic(fwc);
					break;
				case STATE_FW_RECEIVE_INIT:
					if (BuildConfig.DEBUG) {
						Log.e("UPDATE", "初始化写入后，写入文件crc：" + LeFileHex.crc);
					}
					fwc = mBluetoothGatt.getService(UUID_AIR_FW_SERVICE).getCharacteristic(UUID_AIR_FW_32);
					fwc.setValue(LeFileHex.bytescrc);
					mBluetoothGatt.writeCharacteristic(fwc);
					break;
				case STATE_FW_RECEIVE_FW:

					// 发送
					if (BuildConfig.DEBUG) {
						Log.e("UPDATE", "发送写08每8字节回一个ack:" + (byte) uuuMax);
					}
					STATE_FW = STATE_FW_RECEIVE_FW_2;
					characteristic.setValue(new byte[] { 8, (byte) uuuMax, 0 });
					mBluetoothGatt.writeCharacteristic(characteristic);
					break;
				case STATE_FW_RECEIVE_FW_2:
					// 下面是直接发送
					if (BuildConfig.DEBUG) {
						Log.e("UPDATE", "发送" + (byte) uuuMax);
					}
					STATE_FW = STATE_FW_RECEIVE_FW_3;
					fwc = mBluetoothGatt.getService(UUID_AIR_FW_SERVICE).getCharacteristic(UUID_AIR_FW_32);

					while (index < LeFileHex.buffer.length) {
						byte[] data = LeFileHex.buffer[index];

						if (data[3] == 0) {
							// MyLog.v(TAG, "FW INDEX:"+index);
							index++;
							tmp = new byte[data[0]];
							bytei += tmp.length;
							System.arraycopy(data, 4, tmp, 0, data[0]);
							break;
						} else {
							// MyLog.e(TAG, "FW INDEX IS EMPTY:" + index);
							index++;
							continue;
						}
					}
					// MyLog.v("jjj", "byte write: " + index);
					fwc.setValue(tmp);
					mBluetoothGatt.writeCharacteristic(fwc);
					MyLog.v("jjj",
							index + "/" + uuu + " ,0 tmp: " + tmp.length + ": " + Converter.byteArrayToHexString(tmp));
					broadcastUpdate(ACTION_FW_STEPS, 3);
					uuu = uuuMax - 1;
					break;

				case STATE_FW_VALIDATE_2:
					// 升级完成
					// MyLog.e(TAG, "FW send upgrade ok");
					break;
				default:
					break;
				}

			} else if (UUID_AIR_DATA_F1.equals(characteristic.getUuid())) {
				// 是否还有数据需要发送
				if (DEVICE_MODE == 1) {
					if (outStream != null) {
						outStream.ContinueSend();
					}
				} else {

					// 升级文件
					/*
					 * if (mBluetoothAdapter != null) {
					 * broadcastUpdate(ACTION_FW_STEPS, 2); if (mBluetoothGatt
					 * != null) { mBluetoothGatt.close(); mBluetoothGatt = null;
					 * } try { Thread.sleep(1000); } catch (InterruptedException
					 * e) { // TODO Auto-generated catch block
					 * e.printStackTrace(); } disableBT(); // MyLog.d(TAG,
					 * "发送数据，关闭蓝牙"); }
					 */

				}

			} else if (UUID_AIR_FW_32.equals(characteristic.getUuid())) {
				switch (STATE_FW) {
				case STATE_FW_RECEIVE_FW_3:
					if (uuu > 0) {
						fwc = mBluetoothGatt.getService(UUID_AIR_FW_SERVICE).getCharacteristic(UUID_AIR_FW_32);

						while (index < LeFileHex.buffer.length) {
							byte[] data1 = LeFileHex.buffer[index];
							if (data1[3] == 0) {
								MyLog.v(TAG, "FW INDEX:" + index);
								index++;
								tmp = new byte[data1[0]];
								bytei += tmp.length;
								System.arraycopy(data1, 4, tmp, 0, data1[0]);

								break;
							} else {
								MyLog.v(TAG, "FW INDEX IS EMPTY:" + index);
								index++;
								continue;
							}
						}
						/*
						 * try { Thread.sleep(300); } catch
						 * (InterruptedException e) { // TODO Auto-generated
						 * catch block e.printStackTrace(); } MyLog.v("jjj",
						 * "byte write: " + index + "/ " + tmp.length);
						 */
						if (tmp != null) {
							fwc.setValue(tmp);
							mBluetoothGatt.writeCharacteristic(fwc);
							MyLog.v("jjj", index + "/" + uuu + " ,2 tmp: " + tmp.length + ": "
									+ Converter.byteArrayToHexString(tmp));
						}
						// if (index % 10 == 0)
						{
							broadcastUpdate(ACTION_FW_PROGRESS, bytei, LeFileHex.datalen);
						}
						uuu--;
					}
					break;

				default:
					break;
				}
			}
			/*
			 * else if(UUID_AIR_FW_32.equals(characteristic.getUuid())) {
			 * 
			 * }
			 */
		}

		int uuuMax = 8;// 8
		int uuu = 0;

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
			// TODO Auto-generated method stub
			super.onDescriptorWrite(gatt, descriptor, status);
			if (UUID_PAY_FFE2.equals(descriptor.getCharacteristic().getUuid())) {
				// 卡片使能成功
				// 查询开机
				// writePacket(composeCardPacket(new byte[] { (byte) 0xf5,
				// (byte) 0x5f, (byte) 0x02, (byte) 0xa1, (byte) 0x00,
				// (byte) 0xf6, (byte) 0x6f }));
				// update7816();
				BluetoothGattCharacteristic characFFE1 = mCardGattService.getCharacteristic(UUID_PAY_FFE1);
				if (characFFE1 != null) {
					output7816 = new LEOutPutStream(mBluetoothGatt, characFFE1, false);
				}
				if (bLogfile) {
					Log.e("xxxx", "卡片使能成功");
				}
				if (mBluetoothGattService != null) {
					// 使能air
					EnableFFF2();
				}
				return;
			}
			if (STATE_FW == STATE_FW_START) {
				// 升级
				BluetoothGattCharacteristic characteristic = gatt.getService(UUID_AIR_FW_SERVICE)
						.getCharacteristic(UUID_AIR_FW_31);
				if (BuildConfig.DEBUG) {
					Log.e("UPDATE", "写1准备启动");
				}
				characteristic.setValue(new byte[] { 1 });
				mBluetoothGatt.writeCharacteristic(characteristic);
			} else {
				// 普通通讯
				BluetoothGattCharacteristic characteristic2 = mBluetoothGattService.getCharacteristic(UUID_AIR_DATA_F1);
				outStream = new LEOutPutStream(mBluetoothGatt, characteristic2, bLogfile);
				parser = new Parser(outStream, BluetoothLeService.this);
				if (parser != null) {

					if (DEVICE_MODE == 1) {
						// 设置防丢
						parser.setLost(set_isantilost ? 1 : 0);
						parser.Start(5);
						if (bLogfile) {
							Log.e("xxxx", "AIR使能成功");
						}

					} else if (DEVICE_MODE == 2) {
						updateStart(gatt);
					}

				}
			}

		}

	};
	LEOutPutStream output7816;

	void writePacket(byte[] data) {

		if (outStream.bWriting) {
			Message msg = new Message();
			msg.obj = data;
			msg.what = 0;
			handler.sendMessageDelayed(msg, 2500);
			return;
		}
		if (output7816 != null) {
			if (bLogfile) {
				Log.e("SEND" + output7816.getCharacteristicUUID(), Converter.byteArrayToHexString(data));
			}
			try {
				output7816.write(data, 0, data.length);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Message msg = new Message();
			msg.obj = data;
			msg.what = 0;
			handler.sendMessageDelayed(msg, 2500);
		} else {
			Toast.makeText(getApplicationContext(), "没有相关服务，请正确连接设备", Toast.LENGTH_LONG).show();
		}

	}

	byte[] composeCardPacket(byte[] data) {
		byte[] all = new byte[data.length + 5];
		all[0] = (byte) 0xf1;
		all[1] = (byte) 0x1f;
		all[2] = (byte) data.length;
		System.arraycopy(data, 0, all, 3, data.length);
		all[all.length - 2] = (byte) 0xf2;
		all[all.length - 1] = (byte) 0x2f;
		return data;
	}

	void readCard(int who) {

		if (mCardGattService != null) {
			String str = "f11f0800a40000023f0000f22f";

			if (str != null) {
				writePacket(strToBytes(str));
			}
			// String[] x = { "00A4040000", "00A4040000", "00A4040000" };
			// writePacket(composePackets(x, false));

		} else {
			Toast.makeText(getApplicationContext(), "没有相关服务，请正确连接设备", Toast.LENGTH_LONG).show();
		}
	}

	public static byte[] strToBytes(String src) {

		if (null == src || 0 == src.length() || 0 != src.length() % 2) {
			return null;
		}

		byte[] arrRes = new byte[src.length() / 2];
		StringBuffer sBuff = new StringBuffer(src);

		int i = 0;
		String sTmp = null;
		while (i < sBuff.length() - 1) {
			sTmp = src.substring(i, i + 2);
			arrRes[i / 2] = (byte) Integer.parseInt(sTmp, 16);
			i += 2;
		}

		return arrRes;
	}

	// 使能读卡ffe2
	void enableFFE2() {
		if (mCardGattService != null) {
			BluetoothGattCharacteristic characteristic;
			characteristic = mCardGattService.getCharacteristic(UUID_PAY_FFE2);
			boolean enabled = true;
			if (characteristic != null) {

				mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
				List<BluetoothGattDescriptor> descriptorl = characteristic.getDescriptors();
				BluetoothGattDescriptor descriptor = descriptorl.get(0);
				if (descriptor != null) {
					descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
					mBluetoothGatt.writeDescriptor(descriptor);
				}
			}
		} else {
			Toast.makeText(getApplicationContext(), "没有相关服务，请正确连接设备", Toast.LENGTH_LONG).show();
		}

	}

	void updateStart(BluetoothGatt gatt) {
		if (parser != null) {
			if (bLogAbc)
				MyLog.v("abc", "parser.Start(6) ");
			try {
				mScanning = false;
				if (bLogfile)
					LogUtil.e(TAG, "stopLeScan-> updateStart");
				mBluetoothAdapter.stopLeScan(mLeScanCallback);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}

			parser.Start(6);
			afterSwitch = true;
			// broadcastUpdate(ACTION_FW_STEPS, 2);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// uu
			/*
			 * if (mBluetoothGatt != null) { mBluetoothGatt.close();
			 * mBluetoothGatt = null; }
			 */
			// 如果有升级服务
			if (gatt.getService(UUID_AIR_FW_SERVICE) != null) {
				mBluetoothGattService = gatt.getService(UUID_AIR_FW_SERVICE);
				Enabel1531();
			} else {
				// refreshDeviceCache(gatt, true);
				disableBT(true);
			}
			// if (hasFW && gatt !=null) {
			// bStep3 = true;
			// mBluetoothGattService = gatt.getService(UUID_AIR_FW_SERVICE);
			// if (mBluetoothGattService != null) {
			// // 升级固件
			// Enabel1531();
			// // MyLog.i(TAG,
			// // "mode 2 onServicesDiscovered Enable1531");
			// } else {
			// //
			// MyLog.e(TAG,"1530 onServicesDiscovered Not SUCCESS,The status is:
			// "+
			// // status);
			// refreshDeviceCache(gatt);
			// }
			// } else if(!refreshDeviceCache(gatt)) {
			// if(!bStep2){
			// disableBT(true);
			// }else{
			// // 升级固件
			// Enabel1531();
			// }
			//
			// }

		}
	}

	private boolean refreshDeviceCache(BluetoothGatt gatt, boolean discover) {

		try {
			BluetoothGatt localBluetoothGatt = gatt;
			Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
			if (localMethod != null) {
				boolean bool = ((Boolean) localMethod.invoke(localBluetoothGatt, new Object[0])).booleanValue();
				if (discover) {
					gatt.discoverServices();
				}
				return bool;
			}
		} catch (Exception localException) {
			Log.e(TAG, "An exception occured while refreshing device");
		}
		return false;
	}

	// 设备扫描回调
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, final int rssiint, byte[] scanRecord) {
			/*
			 * runOnUiThread(new Runnable() {
			 * 
			 * @Override public void run() { AirDevice airDevice= new
			 * AirDevice(device,rssiint);
			 * mLeDeviceListAdapter.addDevice(airDevice);
			 * mLeDeviceListAdapter.notifyDataSetChanged(); } });
			 */
			// if(bLogfile)LogUtil.e("---搜索到---" + device.getAddress());
			if (device.getAddress().equals(address)) {
				if (bLogfile)
					LogUtil.e("---找到设备---" + Util.getTimeMMString());
				// 找到设备
				// 1.停止服务
				if (mScanning) {
					mScanning = false;
					if (mFindTimer != null) {
						mFindTimer.cancel();
					}
					if (bLogfile)
						LogUtil.e(TAG, "stopLeScan-> onLeScan:find");
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
					if (mBluetoothGatt != null) {
						mBluetoothGatt.disconnect();
						mBluetoothGatt.close();
					}
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
				} else {
					mScanning = true;
				}

			}
		}
	};

	long rssi_calltime, rssi_readtime;

	// rssi timer

	private Timer _watchdogConnectionTimer;
	int counter_onehour = 0;
	int counter_onemin = 0;
	// 10-11 默认报警延时30秒
	int lostcount = 0;
	boolean hasClose = false;

	public void startWatchdogTimer() {
		counter_onehour = 0;
		rssi_readtime = rssi_calltime = System.currentTimeMillis();
		if (_watchdogConnectionTimer != null) {
			_watchdogConnectionTimer.cancel();
		}
		_watchdogConnectionTimer = new Timer();
		_watchdogConnectionTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				counter_onemin++;
				if (counter_onemin >= 120) {
					counter_onemin = 0;
					initOftenUsedPref(BluetoothLeService.this, false);
				}
				counter_onehour++;
				long sec = System.currentTimeMillis() - rssi_readtime;
				if (counter_onehour % 5 == 0) {
					if (bLogfile)
						LogUtil.e("count:" + auto_connection_counter + "--" + "RSSI_TIME:" + sec + "--"
								+ "STATE_CONNECTED：" + (mConnectionState == STATE_CONNECTED) + "--DEVICE_MODE："
								+ DEVICE_MODE + "---" + Util.getTimeMMString());
				}
				if (mConnectionState == STATE_CONNECTED) {
					isCon = true;
					// 10-11 改为4小时读一次
					if (counter_onehour >= 60 * 60 * 2) {
						counter_onehour = 0;
						if (parser != null) {
							parser.Start(5);
							if (bLogfile)
								LogUtil.e(counter_onehour + "send");
						}

					}
					// 9.19
					// if (lost_delay == 30 && temp < 300) {
					// lost_delay = 60;
					// }
					//
					// temp++;
					// if (temp == 300) {
					// lost_delay = 30;
					// if (parser != null) {
					// parser.SendLostDelay((byte) 30);
					// }
					// }
					try {

						if (mBluetoothGatt != null) {
							// MyLog.v(TAG, "bbbbb " +
							// Calendar.getInstance().getTime().toLocaleString());

							rssi_calltime = System.currentTimeMillis();
							// if(Math.abs(rssi_calltime - rssi_readtime) >
							// 3000)
							// {
							// onDeviceLost();
							// }
							// 都每两秒读吧

							if (btClose && DEVICE_MODE == 1 && rssi_readtime != 0 && sec > 20 * 1000
									&& sec <= 30 * 1000) {
								// 上一次得到rssi在30s内读过并在20秒前丢失 说明 已经deadobject了
								if (bLogfile)
									LogUtil.e("记录的链接状态是连接，但是20前rssi丢失aa" + Util.getTimeMMString());
								if (bLogfile)
									LogUtil.e("start Process:BleHolder------------");
								Intent i = new Intent(BluetoothLeService.this, BleScanService.class);
								i.putExtra("address", address);
								i.putExtra("order", true);
								startService(i);
								btClose = false;
							}
							if (counter_onehour % 3 == 0) {
								if (!mBluetoothGatt.readRemoteRssi()) {
									mConnectionState = STATE_DISCONNECTED;
									if (BuildConfig.DEBUG) {
										if (bLogfile)
											LogUtil.e("*************ReadrssiError**************");
									}
								}
							}

						} else {
							rssi_readtime = rssi_calltime = System.currentTimeMillis();
						}
					} catch (Exception e) {
						// TODO: handle exception
					}

					// int ms =Integer.parseInt(new
					// SimpleDateFormat("HHmmss").format(new Date()));
					// if(parser!=null){
					// if(ms>sStart && !hasClose){
					// parser.setVibrateEn(false);
					// hasClose = true;
					// }
					// if(!isInSleep()){
					// parser.setVibrateEn(true);
					// }
					// }
				} else {
					// 10-11 场强在强于-85时发生的断开 报警延时一律60秒，
					// 但这时无法向air回写，如果之前是30秒 那么air的延时还是30 app为60
					// if (rssi > -85 && airDelay != 60) {
					// airDelay = 60;
					// }
					isCon = false;
					if (DEVICE_MODE == 1) {

						if (auto_connection_wait_counter < airDelay) {
							auto_connection_wait_counter++;
						} else if (auto_connection_wait_counter == airDelay && !bActiveDisconnect) {
							auto_connection_wait_counter++;
							onDeviceLost();
							// 存丢失统计
							if (db == null) {
								db = new Dbutils(BluetoothLeService.this);
							}
							int l = db.getAirlostTimes(MyDate.getFileName());
							l++;
							db.setAirRecorderlost(l, MyDate.getFileName());
							// 开始报警后立马跑一次find2connect ?
							// auto_connection_counter = 60;
						}
						auto_connection_counter++;
						if (auto_connection_counter >= 3000) {
							auto_connection_counter = 0;
							boolean autoconnect = sharePre.getBoolean("air_auto_connect", true);
							if (autoconnect) {
								find2connectWait();
							}
						}
						if (auto_connection_counter >= 60) {
							if (bLogfile)
								LogUtil.e("计数60" + "---" + Util.getTimeMMString());
							auto_connection_counter = 0;
							if (sharePre == null) {
								sharePre = getSharedPreferences("air", MODE_MULTI_PROCESS);
							}
							boolean autoconnect = sharePre.getBoolean("air_auto_connect", true);
							if (autoconnect) {
								if (bLogfile)
									LogUtil.e("计数60,且自动连接，开始find2connect" + "---" + Util.getTimeMMString());
								find2connectWait();
							}
						}
					}
				}
			}

		}, 1000, 1000);
	}

	public void cancelWatchdogTimer() {
		if (_watchdogConnectionTimer != null) {
			_watchdogConnectionTimer.cancel();
			_watchdogConnectionTimer = null;
		}

	}

	// 设备自动检查
	int auto_connection_counter = 0;
	int auto_connection_wait_counter = 120;

	public void cancelWatchdogTimer_scan() {
		if (_watchdogScanTimer != null) {
			_watchdogScanTimer.cancel();
			_watchdogScanTimer = null;
		}

	}

	// come from lebu
	Timer mFindTimer = null, scanErrorTimer = null;

	public void find2connect() {
		try {
			if (mConnectionState != STATE_CONNECTED) {
				// List<BluetoothDevice> devices = mBluetoothManager
				// .getConnectedDevices(BluetoothProfile.GATT);
				// for (BluetoothDevice device : devices) {
				// if (device.getAddress().equals(address)) {
				// System.out.println("#########已经连上了##########");
				// // connect(device.getAddress());
				// return;
				// }
				// }
			}
			mScanning = false;
			if (bLogfile)
				LogUtil.e(TAG, "stopLeScan-> find2connect");
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		find2connectWait();
	}

	@SuppressWarnings("deprecation")
	public void find2connectWait() {
		/*
		 * List<BluetoothDevic
		 * 
		 * e> lstConnected; BluetoothManager manager = (BluetoothManager)
		 * getSystemService(Context.BLUETOOTH_SERVICE); lstConnected =
		 * manager.getConnectedDevices(BluetoothProfile.GATT);
		 * 
		 * for(BluetoothDevice dd:lstConnected) { if
		 * (dd.getAddress().equals(address)) { mBluetoothGatt =
		 * dd.connectGatt(this, false, mGattCallback); return; } }
		 */
		/*
		 * if(bLogfile)
		 * LogUtil.i(Calendar.getInstance().getTime().toLocaleString() +
		 * "MiBLEService.connectLast"); lstConnected = ((BluetoothManager)
		 * getSystemService
		 * (Context.BLUETOOTH_SERVICE)).getConnectedDevices(BluetoothGatt.GATT);
		 * db_test db1 = new db_test(); // get all devices ArrayList<String[]>
		 * ss1 = db1.getDevices(0, DeviceWrapper.DEVICE_TYPE_BLE);
		 * 
		 * if(ss1.size() > 0) { String lastDeviceAddr = ss1.get(ss1.size() -
		 * 1)[0];
		 * 
		 * if (lastDeviceAddr != null) { //
		 * Toast.makeText(BluetoothService.this, //
		 * getString(R.string.trans_bt_service_reopen), //
		 * Toast.LENGTH_SHORT).show(); MyLog.d(TAG, lastDeviceAddr); try {
		 * BluetoothDevice dd =
		 * BluetoothAdapter.getDefaultAdapter().getRemoteDevice(lastDeviceAddr);
		 * connectTry(dd,true); } catch (Exception e) { // TODO: handle
		 * exception MyLog.v("aaa", "invalid bluetooth address"); } } }
		 */
		if (bLogAbc)
			MyLog.v("abc", "find2connect");
		try {
			if (address == null) {
				if (sharePre == null) {
					sharePre = getSharedPreferences("air", MODE_MULTI_PROCESS);
				}
				address = sharePre.getString("air_address", null);
			}
			if (mLeScanCallback != null && mScanning == false) {
				mScanning = true;
				// disconnectLast_all();
				TimerTask task = new TimerTask() {
					public void run() {
						// execute the task
						try {
							if (bLogfile) {
								LogUtil.e("---扫描满40s---" + Util.getTimeMMString());
							}

							mScanning = false;
							if (bLogfile) {
								LogUtil.e(TAG, "stopLeScan-> sacn for 40s");
							}
							mBluetoothAdapter.stopLeScan(mLeScanCallback);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							// e.printStackTrace();
						}
					}
				};
				try {
					if (mFindTimer != null) {
						mFindTimer.cancel();
					}
					mFindTimer = new Timer();
					mFindTimer.schedule(task, SCAN_PERIOD);
					if (bLogfile) {
						LogUtil.e("---开始扫描find2connect---" + Util.getTimeMMString());
					}

				} catch (Exception e) {
					// TODO: handle exception
				}

				if (mBluetoothAdapter == null) {
					if (mBluetoothManager == null) {
						mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
						if (mBluetoothManager == null) {
							MyLog.e(TAG, "Unable to initialize BluetoothManager.");
							return;
						}
					}

					mBluetoothAdapter = mBluetoothManager.getAdapter();
					if (mBluetoothAdapter == null) {
						MyLog.e(TAG, "Unable to obtain a BluetoothAdapter.");
						return;
					}
				}
				boolean startRes = mBluetoothAdapter.startLeScan(mLeScanCallback);
				if (!startRes) {
					if (BuildConfig.DEBUG) {
						// 但是很多时候成功开始扫描也会返回false.....
						if (bLogfile)
							LogUtil.e("*************StartLeScanError**************" + Util.getTimeMMString());
					}
					if (scanErrorTimer != null) {
						scanErrorTimer.cancel();
						scanErrorTimer = null;
					}
					// scanErrorTimer = new Timer();
					// scanErrorTimer.schedule(new TimerTask() {
					//
					// @Override
					// public void run() {
					// // TODO Auto-generated method stub
					// //扫描返回false 且5s没有扫描到任何设备，重启服务
					// Intent i = new Intent(BluetoothLeService.this,
					// BleScanService.class);
					// i.putExtra("address", address);
					// i.putExtra("order", true);
					// startService(i);
					// }
					// }, 5*1000);

				}
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
		// intent.putExtra("has", hasFW);

		sendBroadcast(intent);
	}

	private void broadcastUpdate(final String action, int cur, int all) {
		final Intent intent = new Intent(action);
		intent.putExtra("cur", cur);
		intent.putExtra("all", all);
		sendBroadcast(intent);
	}

	// 是否是第二部重启蓝牙时
	void disableBT(boolean step2) {
		try {
			if (step2) {
				broadcastUpdate(ACTION_FW_STEPS, 2);
			}
			if (bLogAbc)
				MyLog.v("abc", "disableBT ");
			disconnectLast_all();
			mConnectionState = STATE_DISCONNECTED;
			Intent intent = new Intent(ACTION_AIR_RSSI_STATUS);
			// 连接断开
			intent.putExtra("status", 0);
			sendBroadcast(intent);

			if (mBluetoothAdapter != null) {
				mBluetoothAdapter.disable();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
		final Intent intent = new Intent(action);

		// This is special handling for the Heart Rate Measurement profile. Data
		// parsing is
		// carried out as per profile specifications:
		// http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
		if (UUID_AIR_FW_31.equals(characteristic.getUuid())) {
			final byte[] data = characteristic.getValue();
			if (data != null && data.length > 0) {
				final StringBuilder stringBuilder = new StringBuilder(data.length);
				for (byte byteChar : data)
					stringBuilder.append(String.format("%02X ", byteChar));
				intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());

				if (data.length == 3) {
					// MyLog.e(TAG, "Received 1531:" +
					// stringBuilder.toString());
				}
			}

		} else if (UUID_AIR_FW_32.equals(characteristic.getUuid())) {// 写升级数据返回

			final byte[] data = characteristic.getValue();
			if (data != null && data.length > 0) {
				final StringBuilder stringBuilder = new StringBuilder(data.length);
				for (byte byteChar : data)
					stringBuilder.append(String.format("%02X ", byteChar));
				intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());

				// MyLog.e("Test", "Received 1532:" + stringBuilder.toString());
			}
		} else if (UUID_PAY_FFE2.equals(characteristic.getUuid())) {
			byte[] xx = characteristic.getValue();
			if (xx != null) {
				resendcount = 0;
				handler.removeMessages(0);
				if (bLogfile) {
					LogUtil.e("xxxxxx", "ACK:" + Converter.byteArrayToHexString(xx) + '\n');
				}
				getArray(xx, xx.length);

			}

		} else {
			// For all other profiles, writes the data formatted in HEX.
			final byte[] data = characteristic.getValue();
			if (data != null && data.length > 0) {
				final StringBuilder stringBuilder = new StringBuilder(data.length);
				for (byte byteChar : data)
					stringBuilder.append(String.format("%02X ", byteChar));
				intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());

				// MyLog.e(TAG, "Received data:" + stringBuilder.toString());
			}
			if (parser != null) {
				long nowTime = System.currentTimeMillis();
				if (Math.abs(lastParseTime - nowTime) > 10000) {
					parser.buf_Len = 0;
					parser._isFindLen = false;
				}
				lastParseTime = System.currentTimeMillis();

				// A7-B8-00-01-00-00-00-14-08-05-00-00-04-CB-00-00-02-DE-BB-21

				if (data.length == 20 && (data[8]) == 8 && (data[9]) == 5) {
					if (((data[10] & 0xff) & (data[11] & 0xff) & (data[12] & 0xff) & (data[13] & 0xff)
							& (data[14] & 0xff) & (data[15] & 0xff) & (data[16] & 0xff) & (data[17] & 0xff)) == 0xff) {
						sendBroadcast(new Intent(SimpleHomeFragment.ACTION_START));
					} else {
						if (bLogfile)
							LogUtil.e("<===" + Converter.byteArrayToHexString(data));
						parser.onPacket0805(data);
					}

				} else {
					if (data.length == 12 && (data[8]) == 4 && (data[9]) == 0 && (data[0]) == 0xa7) {
						if (reFindTimer != null) {
							reFindTimer.cancel();
							reFindTimer = null;
						}
						try {
							mBluetoothAdapter.stopLeScan(mLeScanCallback);
							mScanning = false;
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					parser.getArray(data, data.length);
				}

			}
		}
		// sendBroadcast(intent);
	}

	byte[] buffer = new byte[1024];
	byte[] packet = new byte[1024];
	public int buf_Len, packet_Len;

	public void getArray(byte[] value, int len) {

		if (buffer != null) {
			synchronized (buffer) {
				try {
					// buf_Len 已有
					if (buf_Len >= buffer.length) {
						buf_Len = 0;
					} else {
						System.arraycopy(value, 0, buffer, buf_Len, len);

						buf_Len += len;
						Check();
					}
				} catch (Exception e) {
					// TODO: handle exception
					buf_Len = 0;
					e.printStackTrace();

				}
			}
		}
	}

	boolean _isFindLen = false;
	LinkedList<String> tasklist;

	private void Check() {
		// MyLog.i("rrrr",
		// "check: " + Converter.byteArrayToHexString(buffer, 0, buf_Len));
		if (!_isFindLen) {
			if (buf_Len >= 3) {
				// packet_Len此包长度

				packet_Len = (buffer[2] & 0xff) + 5;
				_isFindLen = true;
			}
		}

		if (_isFindLen && buf_Len >= packet_Len) {

			{
				ParserData();
			}

		}
	}

	int temp0817 = 0;
	int resendcount = 0;
	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				if (msg.obj == null) {
					break;
				}
				if (resendcount++ >= 10) {
					resendcount = 0;
					break;
				}

				byte[] bt = (byte[]) msg.obj;
				if (Converter.byteArrayToHexString(bt).toUpperCase().equals("F55F02A001F66F")) {
					writePacket(bt);
				}

				break;
			case 12:
				byte[] xx = (byte[]) msg.obj;
				writePacket(xx);
				break;
			case 222:
				if (++temp0817 == 30) {
					Intent i1 = new Intent(ACTION_BLE_CARD);
					i1.putExtra("text", "(2/2)进度：" + "100%,升级结束###" + '\n');
					i1.putExtra("text2", "升级结束！！！" + '\n');
					sendBroadcast(i1);
					writePacket(strToBytes("F55F02A000F66F"));
				} else {
					writePacket(strToBytes("0A0A"));
					handler.sendEmptyMessageDelayed(222, 6 * 1000);
				}
				break;
			case 7:
				find2connect();
				break;
			}
		};
	};
	int cardIndex = 0;
	String HexUid = "01BA58FA6397ED", CardChallenge, F01, F14, F15, F05;
	String card;

	private byte[] composeMulti(String content, boolean isHex) {
		// F77F XXXX F1 10 07
		// 0D00A4040007D27600008501000009905A000003010000000D90BD0000070F000000000000000D90BD00000701000000000000000D90BD00000705000000000000000D90BD0000070E0000000000000007900A0000010100F88F
		int len = content.length() / 2;
		String str = "F77F" + Integer.toHexString((len >> 8) & 0xff) + Integer.toHexString((len) & 0xff) + "F11007"
				+ content + "F88F";
		return strToBytes(str);

	}

	private byte[] composePackets(String[] packets, boolean isHex) {
		StringBuffer par = new StringBuffer();
		for (String pt : packets) {
			par.append(fillZero(Integer.toHexString(pt.length() / 2)));
			par.append(pt);
		}
		int len = (par.length() / 2) + 3;
		String lenPer = Integer.toHexString((len >> 8) & 0xff);

		String str = "F77F" + fillZero(lenPer) + Integer.toHexString((len) & 0xff) + "F1" + (isHex ? "02" : "01")
				+ fillZero(Integer.toHexString(packets.length)) + par + "F88F";
		return strToBytes(str);
	}

	/***
	 * 一位则高位补零,2位则不操作
	 * 
	 * @param str
	 * @return
	 */
	private String fillZero(String str) {
		if (str != null && str.length() == 1) {
			str = "0" + str;
		} else if (str == null || str.length() == 0) {
			str = "00";
		} else {
			return str;
		}
		return str;
	}

	// 表示将要发哪个指令
	// -1 默认 ，1 选择hexuid应用,2读hexuid,3选择读取卡号的应用,4,5A命令，5 bd01 ,6 bd05,7bd0e,8bd0f
	private int current = -1;
	private String cardCode;

	private void sendNetCode() {
		new Thread(new Runnable() {
			public void run() {

				String code = getNjCheck(HexUid, CardChallenge, F01, F14, F15, F05);
				if (code == null || code.length() < 5) {
					writePacket(strToBytes("F55F02A000F66F"));
					Intent i3 = new Intent(ACTION_BLE_CARD);
					i3.putExtra("code", "得到指令Null错误:" + code + '\n');
					sendBroadcast(i3);
					db.removeNanjingArgs(address);
					Log.e("XX", "CODE NULL");
					return;
				}

				byte[] codeBt = new byte[code.length() / 2];

				for (int i = 0; i < code.length(); i += 2) {
					codeBt[i / 2] = (byte) (Dbutils.charToByte(code.charAt(i)) << 4
							| Dbutils.charToByte(code.charAt(i + 1)));

				}
				byte[] xx = new byte[codeBt.length + 5];
				xx[0] = (byte) 0xf1;
				xx[1] = (byte) 0x1f;
				xx[2] = (byte) codeBt.length;
				System.arraycopy(codeBt, 0, xx, 3, codeBt.length);
				xx[xx.length - 2] = (byte) 0xf2;
				xx[xx.length - 1] = (byte) 0x2f;
				writePacket(xx);// 90AF000010A871E698007B8FFD4B6F6CED55EADD8200
								// 90AF0000107DB2F1439B8F2974AE2903813D9F60E600

				current = 10;
			}
		}).start();
	}

	private void ParserData() {
		buf_Len = 0;
		_isFindLen = false;
		System.arraycopy(buffer, 0, packet, 0, packet_Len);
		byte[] bt = new byte[packet_Len];
		System.arraycopy(buffer, 0, bt, 0, packet_Len);
		if (bLogfile) {
			LogUtil.e("Copy", Converter.byteArrayToHexString(bt));
		}
		if ((packet[0] & 0xff) == 0xf5 && (packet[1] & 0xff) == 0x5f) {
			switch (packet[3] & 0xff) {
			case 0xa0:
				// 开关回调
				if ((packet[4] & 0xff) == 0x00) {
					// 关机回调
					Intent intent = new Intent(ACTION_BLE_CARD);
					intent.putExtra("off", "");
					sendBroadcast(intent);
				} else if ((packet[4] & 0xff) == 0x01) {

					if (current == 0) {
						boolean needRead = true;
						if (nanJingArgs != null && nanJingArgs.size() >= 4) {
							HexUid = nanJingArgs.get(NANJING_CARD_ARGS.HEXUID);
							F01 = nanJingArgs.get(NANJING_CARD_ARGS.BD01);
							F05 = nanJingArgs.get(NANJING_CARD_ARGS.BD05);
							F14 = nanJingArgs.get(NANJING_CARD_ARGS.BD0E);
							F15 = nanJingArgs.get(NANJING_CARD_ARGS.BD0F);
							cardCode = nanJingArgs.get(NANJING_CARD_ARGS.CARD);
							CardChallenge = nanJingArgs.get(NANJING_CARD_ARGS.CHENGEID);
							Intent intent = new Intent(ACTION_BLE_CARD);
							intent.putExtra("card", cardCode);
							sendBroadcast(intent);
							needRead = false;
						}
						if (needRead) {
							// 读取hexuid
							writePacket(strToBytes("f11f0d00a4040008a000000003000000f22f"));
							current = 1;
						} else {
							// 跳过读hexuid , bd
							// 选择读余额
							writePacket(strToBytes("F11F0D00A4040007D276000085010000F22F"));
							current = 20;
						}
					} else if (current == 100) {
						update7816();
					}
					// 开机回调

				}
				break;
			case 0xa1:
				// 查询
				if ((packet[4] & 0x0f) == 00) {
					// 当前关

				} else if ((packet[4] & 0x0f) == 01) {

				}

				break;
			}
			return;
		}
		// 选择应用回调
		switch (current) {
		case 1:
			// 选择读hexui的应用
			if ((packet[packet_Len - 4] & 0xff) == 0x90 && (packet[packet_Len - 3] & 0xff) == 0x00) {
				writePacket(strToBytes("f11f0580ca004600f22f"));
				current = 2;
			} else {
				writePacket(strToBytes("f55f02a000f66f"));
			}
			break;
		case 2:
			// F1 1F 0B 46 07 0C 67 B8 BA 09 D1 70 90 00 F2 2F
			if ((packet[packet_Len - 4] & 0xff) == 0x90 && (packet[packet_Len - 3] & 0xff) == 0x00) {
				byte[] hex = new byte[7];
				System.arraycopy(bt, bt.length - 11, hex, 0, 7);
				HexUid = Converter.byteArrayToHexString(hex);
				writePacket(strToBytes("F11F0D00A4040007D276000085010000F22F"));
				current = 3;
				Log.e("XX", "收到HEX,发选择应用");
				//
			} else {
				writePacket(strToBytes("f55f02a000f66f"));
			}
			break;
		case 3:
			// 选择读卡应用的返回
			if ((packet[packet_Len - 4] & 0xff) == 0x90 && (packet[packet_Len - 3] & 0xff) == 0x00) {
				// 5A命令
				writePacket(strToBytes("F11F08905A000003010000F22F")); // F11F09905A00000301000000F22F
				current = 4;
				Log.e("XX", "收到选择,发5A");
			} else {
				writePacket(strToBytes("f55f02a000f66f"));
			}
			break;
		case 4:
			if ((packet[packet_Len - 4] & 0xff) == 0x91 && (packet[packet_Len - 3] & 0xff) == 0x00) {
				Log.e("XX", "收到5A,发BD01");
				writePacket(strToBytes("F11F0c90BD00000701000000000000F22F")); // F11F0D90BD0000070100000000000000F22F
				current = 5;
			} else {
				writePacket(strToBytes("f55f02a000f66f"));
			}
			break;
		case 5:
			// 收到bd01
			if ((packet[packet_Len - 4] & 0xff) == 0x91 && (packet[packet_Len - 3] & 0xff) == 0x00) {
				int len = packet[2] & 0xff;
				byte[] tmp = new byte[len - 3];
				System.arraycopy(packet, 3 + 3, tmp, 0, tmp.length);
				F01 = Converter.byteArrayToHexString(tmp);
				Log.e("XX", "收到BD01:" + F01 + ",发BD05");
				current = 6;
				writePacket(strToBytes("F11F0c90BD00000705000000000000F22F")); // F11F0D90BD0000070500000000000000F22F
				// 发送bd05

			} else {
				writePacket(strToBytes("f55f02a000f66f"));
			}
			break;
		case 6:
			// 收到bd05 发bd0e
			if ((packet[packet_Len - 4] & 0xff) == 0x91 && (packet[packet_Len - 3] & 0xff) == 0x00) {
				int len = packet[2] & 0xff;
				byte[] tmp = new byte[len - 3];
				System.arraycopy(packet, 3 + 3, tmp, 0, tmp.length);
				F05 = Converter.byteArrayToHexString(tmp);
				writePacket(strToBytes("F11F0c90BD0000070E000000000000F22F")); // F11F0D90BD0000070E00000000000000F22F
				current = 7;
				Log.e("XX", "收到BD05:" + F05 + ",发BD14");
			} else {
				writePacket(strToBytes("f55f02a000f66f"));
			}
			break;
		case 7:
			// 收到0e 发0f
			if ((packet[packet_Len - 4] & 0xff) == 0x91 && (packet[packet_Len - 3] & 0xff) == 0x00) {
				// BD01
				int len = packet[2] & 0xff;
				byte[] tmp = new byte[len - 3];
				System.arraycopy(packet, 3 + 3, tmp, 0, tmp.length);
				F14 = Converter.byteArrayToHexString(tmp);
				writePacket(strToBytes("F11F0c90BD0000070F000000000000F22F")); // F11F0D90BD0000070F00000000000000F22F
				current = 8;
				Log.e("XX", "收到BD14:" + F14 + ",发BD15");
			} else {
				writePacket(strToBytes("f55f02a000f66f"));
			}
			break;
		case 8:
			// 收到0f
			if ((packet[packet_Len - 4] & 0xff) == 0x91 && (packet[packet_Len - 3] & 0xff) == 0x00) {
				int len = packet[2] & 0xff;
				byte[] tmp = new byte[len - 3];
				System.arraycopy(packet, 3 + 3, tmp, 0, tmp.length);
				F15 = Converter.byteArrayToHexString(tmp);
				// 发0a
				writePacket(strToBytes("f11f06900A00000101f22f")); // f11f07900A0000010100f22f
				current = 9;
				Log.e("XX", "收到BD15:" + F15 + ",发900A");
				StringBuffer sb = new StringBuffer();

				sb.append("版本:" + F15.substring(0, 4) + '\n');

				sb.append("城市代码:" + F15.substring(4, 8) + '\n');

				sb.append("发卡方代码:" + F15.substring(8, 12) + '\n');

				sb.append("卡内号:" + F15.substring(12, 20) + '\n');

				sb.append("卡认证码:" + F15.substring(22, 30) + '\n');

				sb.append("发行日期:" + F15.substring(30, 38) + '\n');

				sb.append("发卡操作员编号:" + F15.substring(38, 42) + '\n');

				sb.append("发卡设备编号:" + F15.substring(42, 46) + '\n');
				sb.append("卡号:" + F15.substring(46, 62) + '\n');
				Intent intent = new Intent(ACTION_BLE_CARD);
				cardCode = F15.substring(46, 62);
				intent.putExtra("card", cardCode);
				sendBroadcast(intent);
				if (bLogfile) {
					Log.e("XXX", sb.toString());
				}
			} else {
				writePacket(strToBytes("f55f02a000f66f"));
			}
			break;
		case 9:
			// 收到0a

			if ((packet[packet_Len - 4] & 0xff) == 0x91 && (packet[packet_Len - 3] & 0xff) == 0xAF) {
				// BD01
				int len = packet[2] & 0xff;
				byte[] tmp = new byte[len - 3];
				System.arraycopy(packet, 3 + 3, tmp, 0, tmp.length);
				CardChallenge = Converter.byteArrayToHexString(tmp);
				Log.e("XX", "收到CardChallenge:" + CardChallenge + ",发900A");
				db.saveNanjingArgs(HexUid, CardChallenge, F01, F05, F14, F15, address, cardCode);
				sendNetCode();
			} else {
				writePacket(strToBytes("f55f02a000f66f"));
			}
			break;
		case 10:
			if ((packet[packet_Len - 4] & 0xff) == 0x91 && (packet[packet_Len - 3] & 0xff) == 0x00) {

				writePacket(strToBytes("F11F06907C00000100F22F")); // F11F07907C0000010000F22F
				current = 11;
			} else {
				writePacket(strToBytes("f55f02a000f66f"));
			}
			break;
		case 11:
			if ((packet[packet_Len - 4] & 0xff) == 0x91 && (packet[packet_Len - 3] & 0xff) == 0x00) {

				int len = packet[2] & 0xff;
				byte[] tmp = new byte[len - 3];
				System.arraycopy(packet, 3 + 3, tmp, 0, tmp.length);
				String str = Converter.byteArrayToHexString(tmp);
				int money = getValueFromLSBHex(str);
				Intent intent = new Intent(ACTION_BLE_CARD);
				intent.putExtra("money", money);
				sendBroadcast(intent);
				// writePacket(strToBytes("f55f02a000f66f"));
				writePacket(strToBytes("F11F0D90bb0000070400000000000000F22F"));
				current = 12;
				recordNj = new StringBuffer();
			} else {
				writePacket(strToBytes("f55f02a000f66f"));
			}

			break;
		case 12:
			int len = packet[2] & 0xff;
			byte[] tmp = new byte[len - 3];
			System.arraycopy(packet, 3 + 3, tmp, 0, tmp.length);
			String tmpResult = Converter.byteArrayToHexString(tmp);
			if (tmpResult.endsWith("9000") || tmpResult.endsWith("9100") || tmpResult.endsWith("91AF")) {
				recordNj.append(tmpResult.substring(0, tmpResult.length() - 4));
			}
			if (tmpResult.endsWith("91AF")) {
				writePacket(strToBytes("F11F0590AF000000F22F"));
				current = 12;
			} else {
				writePacket(strToBytes("f55f02a000f66f"));
				parserRecord();
			}
			break;
		case 13:

			break;
		case 20:
			if ((packet[packet_Len - 4] & 0xff) == 0x90 && (packet[packet_Len - 3] & 0xff) == 0x00) {
				writePacket(strToBytes("F11F08905A000003010000F22F"));
				current = 21;
			} else {
				writePacket(strToBytes("f55f02a000f66f"));
			}
			break;
		case 21:
			if ((packet[packet_Len - 4] & 0xff) == 0x91 && (packet[packet_Len - 3] & 0xff) == 0x00) {
				// 读余额
				writePacket(strToBytes("f11f06900A00000101f22f"));
				current = 22;
			} else {
				writePacket(strToBytes("f55f02a000f66f"));
			}
			break;
		case 22:
			if ((packet[packet_Len - 4] & 0xff) == 0x91 && (packet[packet_Len - 3] & 0xff) == 0xAF) {
				int len2 = packet[2] & 0xff;
				byte[] tmp2 = new byte[len2 - 3];
				System.arraycopy(packet, 3 + 3, tmp2, 0, tmp2.length);
				CardChallenge = Converter.byteArrayToHexString(tmp2);
				sendNetCode();
			} else {
				writePacket(strToBytes("f55f02a000f66f"));
			}
			break;
		case 101:
			// 因为default为return
			break;
		default:
			return;
		}
		if ((packet[0] & 0xff) == 0xf3 && (packet[1] & 0xff) == 0x3f) {
			switch (packet[3] & 0xff) {
			case 0x01:
				switch (packet[4] & 0xff) {
				case 0x61:

					// 请求升级成功开始发数据
					Log.e("UPDATE", "请求升级成功开始发数据");
					cardIndex = 0;
					int len = 128;
					if (HexAll.length - cardIndex < 128) {
						len = HexAll.length - cardIndex;
					}
					byte[] tmp = new byte[len];
					try {
						System.arraycopy(HexAll, cardIndex, tmp, 0, tmp.length);
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
					cardIndex += len;
					writePacket(composePktUpdate(tmp));

					break;

				case 0x32:
					// 结束升级成功

					Log.e("UPDATE", "数据传输完成！！！等待烧录");
					cardIndex = 0;
					break;
				case 0x33:

					Log.e("UPDATE", "检验失败！！！");
					cardIndex = 0;
					break;
				case 0x34:

					Log.e("UPDATE", "检验成功！！！等待烧录");
					cardIndex = 0;
					break;
				}
				break;
			case 0x00:
				if ((packet[4] & 0xff) == 0x66) {
					// 发送确认，下一组
					if (cardIndex < HexAll.length) {
						int len = 128;
						if (HexAll.length - cardIndex < 128) {
							len = HexAll.length - cardIndex;
						}
						byte[] tmp = new byte[len];
						System.arraycopy(HexAll, cardIndex, tmp, 0, tmp.length);
						cardIndex += len;

						float x = ((float) cardIndex / (float) HexAll.length);
						Log.e("UPDATE", "第：" + cardIndex + "条");

						writePacket(composePktUpdate(tmp));
						if (x == 1) {
							handler.sendEmptyMessageDelayed(222, 6 * 1000);
						}

					} else {
						// writePacket(composeCompleteUpdate());
						// 发送校验
						writePacket(strToBytes("F33F030131" + Converter.byteToHexString(checker) + "F44F"));
						Log.e("UPDATE", "发送校验 ！！！");
					}

				} else if ((packet[4] & 0xff) == 0x65) {
					current = -1;

					Log.e("UPDATE", "升级结束！！！");
					writePacket(strToBytes("F55F02A000F66F"));
				}
				break;
			}

		}
	}

	long lastParseTime;

	public class LocalBinder extends Binder {
		public BluetoothLeService getService() {
			return BluetoothLeService.this;
		}
	}

	void parserRecord() {
		ArrayList<NJTransactionDetail> transactionsArrayList = new ArrayList<NJTransactionDetail>();

		String data2 = recordNj.toString();
		int offset = 0;
		while (offset + 32 <= data2.length()) {
			transactionsArrayList.add(0, new NJTransactionDetail(data2.substring(offset, offset + 32)));
			offset += 32;
		}
		ArrayList<HashMap<String, String>> rec = new ArrayList<HashMap<String, String>>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		for (NJTransactionDetail nj : transactionsArrayList) {
			HashMap<String, String> mp = new HashMap<String, String>();

			mp.put("TransDate", "日期：" + sdf.format(nj.TransDate));
			mp.put("TransValue", "金额：" + String.format("%.1f", nj.TransValue / 100f));
			mp.put("Verification", nj.TransTypeDescription + "");
			// mp.put("TransTypeDescription", nj.TransTypeDescription + "");
			// mp.put("TransType", nj.TransType + "");
			// mp.put("TerminalCode", nj.TerminalCode + "");
			// mp.put("Reservation", nj.Reservation + "");
			// mp.put("CityID", nj.CityID + "");
			rec.add(mp);
		}
		Intent intent = new Intent(ACTION_BLE_CARD);
		intent.putExtra("record", rec);
		sendBroadcast(intent);
	}

	// play lost
	public void onDeviceLost() {

		if (isdebug) {
			LogUtil.i(Calendar.getInstance().getTime().toLocaleString() + "onDeviceLost: " + losttype + "/"
					+ losttype_last + "/" + rssi_ave + "/" + rssi_lost_thres1);
			String ss = rssi_filter_counter + ": ";
			for (int i = 0; i < rssi_filter_rank; i++) {
				ss += rssi_filter[i] + "/";
			}
			LogUtil.i(ss);
			LogUtil.i("是否防丢：" + set_isantilost);

		}
		// showAirConnectionStatusNotification(false);
		if (bLogfile)
			LogUtil.e("---------------------->onlost");
		Parser.updatenotification(BluetoothLeService.this, (float) 0, (int) -1, (int) 0, (int) 0, false);
		// 如果正在通话，刚不触发
		if (isOnCalling) {
			return;
		}

		// 检查防丢是否开启
		initUserConfig();
		if (set_isantilost) {
			if (CheckIsInLostTime() || true) {
				// on lost
				losttype_last = 1;
				showAirLostNotification();
				if (!onLostAlarm) {
					if (mediaPlayer == null) {
						mediaPlayer = new MyMediaPlayer(this, true);
						mediaPlayer.setOnRingOverListener(this);
						mediaPlayer.start();

					} else {
						mediaPlayer = new MyMediaPlayer(this, true);
						mediaPlayer.start();
					}
				}

				// 发送内容给设备
				if (parser != null) {
					// 10-11 断开后不再发丟失
					// parser.SendOnDeviceOut();
				}

			}
		}
	}

	public void showAirLostNotification() {
		NotificationUtils not = new NotificationUtils(this, NotificationUtils.LostNotfication);

		not.showNotification(getString(R.string.notification_lost_title), getString(R.string.notification_lost_content),
				AirPreferenceActivity.class, false);

	}

	public void showAirLostNotificationCancel() {
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(NotificationUtils.LostNotfication);
	}

	public void onDeviceIn() {
		// 检查防丢是否开启
		initUserConfig();
		showAirLostNotificationCancel();
		if (set_isantilost) {
			// 发送内容给设备
			// if (parser != null) {
			// parser.SendOnDeviceIn();
			// }
			losttype_last = 0;
			if (mediaPlayer != null) {
				mediaPlayer.Stop();

			}
		}
	}

	// play lost cancle
	public void onDeviceLostCancle() {
		if (mediaPlayer != null) {
			mediaPlayer.Stop();
			mediaPlayer = null;
		}
		showAirLostNotificationCancel();
	}

	public static boolean bLogfile = false;
	static final boolean bLogAbc = false;

	// private int airSetMode = 1;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		LogUtil.e("--BLE服务oncreate时间--" + Util.getTimeMMString());
		addCleanTask();
		// pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		// wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
		// wl.acquire();
		lastParseTime = System.currentTimeMillis();
		if (bLogfile)
			LogUtil.e("--BLE服务oncreate时间--" + Util.getTimeMMString());
		DEVICE_MODE = 1;
		// ttt
		// initOftenUsedPref(this);
		device_connected = false;
		device_battery = 100;
		// if (sharePre == null) {
		sharePre = getSharedPreferences("air", MODE_MULTI_PROCESS);

		// sharePre.registerOnSharedPreferenceChangeListener(this);
		// }
		// if (address == null) {
		// if (sharePre == null) {
		// sharePre = getSharedPreferences("air", MODE_MULTI_PROCESS);
		// }
		address = sharePre.getString("air_address", null);
		// }

		if (bLogfile) {
			LogUtil.setDefaultFilePath(getBaseContext());
			LogUtil.setLogFilePath("log12/");
			LogUtil.setSyns(true);
			// LogUtil.startNewLog();
		}
		if (bLogfile)
			LogUtil.e("LovefitAir.oncreate-" + address + "--" + Util.getTimeMMString());
		// Oncreate次数
		if (db == null) {
			db = new Dbutils(BluetoothLeService.this);
		}
		int c = db.getAirOnCreate(MyDate.getFileName());
		c++;
		db.setAirRecorderCreate(c, MyDate.getFileName());
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

		createPhoneListener();

		IntentFilter filter = new IntentFilter(ACTION);
		filter.addAction(ACTION_BT_REBOOT);
		filter.addAction("android.intent.action.LOCALE_CHANGED");
		filter.addAction(ACTION_BLE_MANAGE_CMD);
		filter.addAction(ACTION_BT_CONFIG_READ);
		filter.addAction(ACTION_BT_CONFIG_WRITE);
		filter.addAction(ACTION_BLE_CONFIG_CMD);
		filter.addAction(ACTION_BLE_DEVICE_ALARM);
		filter.addAction("android.bluetooth.device.action.PAIRING_REQUEST");
		// 用于自动结束遥控

		filter.addAction(ACTION_AIR_CONTROL);

		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

		filter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
		filter.addAction(ACTION_PREF_CHANGED);
		filter.addAction(ACTION_BLE_CARD_READ);
		filter.addAction(ACTION_BLE_CARD_TURNON);
		filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		filter.addAction(ACTION_BLE_CARD_UPDATE);
		filter.addAction(AirNotificationListener.ACTION_AIR_NOTIFICATION);
		filter.addAction("android.intent.action.SERVICE_STATE");
		// filter.addAction("android.intent.action.PHONE_STATE");
		filter.setPriority(Integer.MAX_VALUE);
		if (msgBrocast == null) {
			msgBrocast = new MessageBrocast();
		}
		registerReceiver(msgBrocast, filter);
		// rssi and data get
		// if (rssidata == null) {
		// rssidata = new ReadRssiAndData(this);
		// rssidata.start();
		// }

		initUserConfig();

		// 看门狗
		try {
			startWatchdogTimer();
			if (bLogfile)
				LogUtil.e("Watchdog.start-" + "--" + Util.getTimeMMString());
			// startWatchdogTimer_scan();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 启动日志保存
		// LogcatHelper.getInstance(this).start();
		// MyLog.e(TAG, "LeService is onCreate");

		// 11 26 开启前台服务，前台服务需要notification

		startForeground(NotificationUtils.BlueNotfication,
				Parser.updatenotification(BluetoothLeService.this, (float) 0, (int) 0, (int) 0, (int) 0, false));

		mSettingsContentObserver = new SettingsContentObserver(this, new Handler());
		getApplicationContext().getContentResolver()
				.registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, mSettingsContentObserver);

	}

	/*
	 * public void onSharedPreferenceChanged(SharedPreferences
	 * sharedPreferences, String key) { // Let's do something a preference value
	 * changes if (key.equals("set_lost_distance")) { lost_distance =
	 * sharedPreferences.getInt("set_lost_distance", 8); } if
	 * (key.equals("air_report_rssi")) { bRssiReport =
	 * sharedPreferences.getBoolean("air_report_rssi", false); } }
	 */
	SettingsContentObserver mSettingsContentObserver = null;

	private void initUserConfig() {
		if (sharePre == null) {
			sharePre = getSharedPreferences("air", MODE_MULTI_PROCESS);
		}

		set_isantilost = sharePre.getBoolean("set_isantilost", false);
		set_isnovoice = sharePre.getBoolean("set_isnovoice", true);
		// 寻找手机
		set_isfindephone = sharePre.getBoolean("set_isnovoice", false);
		// 来电，短信 总开关
		set_isremind_call = sharePre.getBoolean("set_isremind_call", true);
		set_isremind_msg = sharePre.getBoolean("set_isremind_msg", true);
		// sharePre = null;
	}

	boolean openBt = false, set_find_phone = true;
	private int airDelay;

	SharedPreferences appShare;
	private String airVersion;

	private void initOftenUsedPref(Context cc, boolean broadcast) {
		// set_safe_distance
		SharedPreferences ss = cc.getSharedPreferences("air", MODE_MULTI_PROCESS);
		appShare = cc.getSharedPreferences(SharePreUtils.APP_ACTION, Context.MODE_MULTI_PROCESS);
		session = appShare.getString("session_id", "lovefit");
		airVersion = ss.getString("soft_version", "");
		lost_distance = ss.getInt("set_lost_distance", 4);
		openBt =  false;
		bRssiReport = ss.getBoolean("air_report_rssi", false);
		// 来电，短信 总开关
		set_isremind_call = ss.getBoolean("set_isremind_call", true);
		set_isremind_msg = ss.getBoolean("set_isremind_msg", true);
		set_isantilost = ss.getBoolean("set_isantilost", false);
		set_find_phone = ss.getBoolean("set_find_phone", true);
		// 延时
		airDelay = 1;
		// 配置改变，刷新通知
		if (broadcast) {

			if (bLogfile)
				LogUtil.e("---------------------->onInitoften");
			Notification notification = Parser.updatenotification(BluetoothLeService.this, (float) 0, (int) -1, (int) 0,
					(int) 0, isCon);
			this.startForeground(NotificationUtils.BlueNotfication, notification);

		}

		// sharePre = null;
	}

	private void addCleanTask() {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		Intent intent = new Intent(MilinkApplication.ACTION_ZERO);
		setRepeatTasks(c.getTimeInMillis(), 24 * 60 * 60 * 1000, intent);
	}

	private void setRepeatTasks(long triggerTime, long interval, Intent intent) {

		AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		// 系统中如果有这个pendingIntent 取消
		PendingIntent pending = PendingIntent.getBroadcast(this, 88, intent, PendingIntent.FLAG_NO_CREATE);
		if (pending != null) {
			am.cancel(pending);
		}
		pending = PendingIntent.getBroadcast(this, 88, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		am.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime, interval, pending);

	}

	// 获取防丢的时间段
	private boolean CheckIsInLostTime() {
		int b_h1 = 0;
		int b_m1 = 0;
		int e_h1 = 0;
		int e_m1 = 0;

		int b_h2 = 0;
		int b_m2 = 0;
		int e_h2 = 0;
		int e_m2 = 0;

		boolean isSecond = false;

		if (sharePre == null) {
			sharePre = getSharedPreferences("air", MODE_MULTI_PROCESS);
		}
		b_h1 = sharePre.getInt("set_antilost_time0_h1", 0);
		b_m1 = sharePre.getInt("set_antilost_time0_m1", 0);
		e_h1 = sharePre.getInt("set_antilost_time0_h2", 0);
		e_m1 = sharePre.getInt("set_antilost_time0_m2", 0);

		if (sharePre.contains("set_antilost_time1_h1")) {
			isSecond = true;
			b_h2 = sharePre.getInt("set_antilost_time1_h1", 0);
			b_m2 = sharePre.getInt("set_antilost_time1_m1", 0);
			e_h2 = sharePre.getInt("set_antilost_time1_h2", 0);
			e_m2 = sharePre.getInt("set_antilost_time1_m2", 0);
		}

		if (b_h1 == e_h1 && b_m1 == e_m1) {
			return true;
		}

		if (isSecond) {
			if (b_h2 == e_h2 && b_m2 == e_m2) {
				return true;
			}
		}

		// 获取当前时间
		Time time = new Time();
		time.setToNow();
		int nowm = time.hour * 60 + time.minute, bm = b_h1 * 60 + b_m1, em = e_h1 * 60 + e_m1;
		if (nowm >= bm && nowm <= em) {
			return true;
		}

		// if (time.hour >= b_h1 && time.hour <= e_h1) {
		// if(time.hour == b_h1 && time.hour == e_h1){
		// if (time.minute >= b_m1 && time.minute <= e_m1) {
		// return true;
		// }
		// }else if(time.hour == b_h1){
		// if (time.minute >= b_m1){
		// return true;
		// }
		// }else if(time.hour == e_h1){
		// if (time.minute <= e_m1){
		// return true;
		// }
		// }else{
		// return true;
		// }
		// }

		if (isSecond) {
			int nowm2 = time.hour * 60 + time.minute, bm2 = b_h2 * 60 + b_m2, em2 = e_h2 * 60 + e_m2;
			if (nowm2 >= bm2 && nowm2 <= em2) {
				return true;
			}
			// if (time.hour >= b_h2 && time.hour <= e_h2) {
			// if(time.hour == b_h2 && time.hour == e_h2){
			// if (time.minute >= b_m2 && time.minute <= e_m2) {
			// return true;
			// }
			// }else if(time.hour == b_h2){
			// if (time.minute >= b_m2){
			// return true;
			// }
			// }else if(time.hour == e_h2){
			// if (time.minute <= e_m2){
			// return true;
			// }
			// }
			// }
		}

		return false;
	}

	private void unpairDevice(BluetoothDevice device) {
		try {
			Method m = device.getClass().getMethod("removeBond", (Class[]) null);
			m.invoke(device, (Object[]) null);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}

	@Override
	public void onDestroy() {
		// 关闭GATT
		if (mBluetoothGatt != null) {
			mBluetoothGatt.close();
			mBluetoothGatt = null;
			mBluetoothGattService = null;
		}
		// 退出看门狗
		cancelWatchdogTimer();
		sharePre.edit().remove("soft_version").commit();
		try {
			if (bLogfile)
				LogUtil.e(TAG, "stopLeScan - > destroy");
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		} catch (Exception e) {
			// TODO: handle exception
		}
		if (mBluetoothAdapter != null) {
			mBluetoothAdapter = null;
		}
		// TODO Auto-generated method stub
		if (mHandler != null) {
			mHandler.removeCallbacksAndMessages(null);
		}
		if (mFindTimer != null) {
			mFindTimer.cancel();
		}
		rssi_readtime = 0;
		bActiveDisconnect = true;
		if (bLogfile)
			LogUtil.e("LeService is onDestroy");
		// if (wl != null) {
		// wl.release();
		// }

		// cancelWatchdogTimer_scan();

		// try {
		// rssidata.StopThread();
		// rssidata = null;
		// } catch (Exception e) {
		// TODO: handle exception
		// }

		parser = null;

		unregisterReceiver(msgBrocast);
		// 停止日志保存
		// LogcatHelper.getInstance(this).stop();

		// showAirConnectionStatusNotification(false);
		// Parser.updatenotification(BluetoothLeService.this,(float) 0,
		// (int) -1, (int) 0,
		// (int) 0,false);

		stopForeground(true);
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(NotificationUtils.BlueNotfication);
		super.onDestroy();
	}

	Dbutils db;
	boolean afterSwitch = false;
	Timer reFindTimer;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		// String name = intent.getStringExtra("name");
		btClose = false;
		initOftenUsedPref(this, false);

		flags = Service.START_REDELIVER_INTENT;

		LogUtil.e("--BLE onstartcommand --" + Util.getTimeMMString());
		LogUtil.setDefaultFilePath(getBaseContext());
		LogUtil.setLogFilePath("log12/");
		LogUtil.setSyns(true);
		db = new Dbutils(this);
		bLogfile = isdebug = getSharedPreferences("air", Context.MODE_MULTI_PROCESS).getBoolean("isdebug", false);
		outStream.bLogFile = bLogfile;

		MyLog.e("abc", "onStartCommand ");
		if (intent != null) {

			int scanflag = 0;
			DEVICE_MODE = intent.getIntExtra("command", 1);
			String openid = intent.getStringExtra("openid");
			String accesstoken = intent.getStringExtra("accesstoken");
			// address="EE:FE:66:7A:84:95";
			// 11 JSUT FOR GET DATA FROM AIR

			if (DEVICE_MODE != 11) {
				address = intent.getStringExtra("address");
			} else {
				DEVICE_MODE = 1;
			}

			if (address == null) {
				return super.onStartCommand(intent, flags, startId);
			}

			if (address.length() != 17) {
				return super.onStartCommand(intent, flags, startId);
			}

			if (DEVICE_MODE == 2) {
				String filnameString = intent.getStringExtra("filename");
				broadcastUpdate(ACTION_FW_STEPS, 1);
				// loadfile
				if (filnameString != null && leFileHex == null) {
					if (bLogfile)
						LogUtil.e("是新的air?--->" + hasFW);
					leFileHex = new LeFileHex(this, filnameString, hasFW);
					if (leFileHex.filestatus == 1) {
						// 文件OK
						// MyLog.d(TAG, "file loas ok");
					} else {
						// 文件error
						// MyLog.d(TAG, "file loas error");
					}
				}

				index = 0;
				bytei = 0;
				if (BuildConfig.DEBUG) {
					Log.e("UPDATE", "DFU Start!");
				}
				if (mBluetoothGatt != null) {
					if (false && !mBluetoothGatt.connect()) {
						mBluetoothGatt.close();
						mBluetoothGatt = null;
						MyLog.d(TAG, "close Gatt on Start Command");
					}
				}
			}

			if (address != null) {

				// 连接设备
				if (!initialize()) {
					MyLog.e(TAG, "Unable to initialize Bluetooth");
				}

				if (DEVICE_MODE == 1) {
					scanflag = intent.getIntExtra("scanflag", 0);
					if (bLogfile)
						LogUtil.e("scanflag--->" + scanflag);
					switch (scanflag) {
					case 0:
						connect(address);
						break;
					case 1:
						// connect(address);
						if (mConnectionState == STATE_CONNECTED) {
							if (parser != null) {
								parser.Start(5);
								reFindTimer = new Timer();
								reFindTimer.schedule(new TimerTask() {

									@Override
									public void run() {
										// TODO Auto-generated method stub
										// 10秒内没有收到0804
										find2connect();
									}
								}, 5 * 1000);
							} else {
								if (mBluetoothGatt != null) {
									try {
										mBluetoothGatt.disconnect();
										mBluetoothGatt.close();
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
								if (bLogfile)
									LogUtil.e("start Scan--->onstartcommand:parser null" + Util.getTimeMMString());
								find2connect();
							}
						} else {
							if (bLogfile)
								LogUtil.e("start Scan--->onstartcommand:no connect" + Util.getTimeMMString());
							find2connect();
						}
						break;
					case 3:
						try {
							if (mConnectionState == STATE_CONNECTED) {
								parser.Start(5);
								;
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						break;
					case 4:
						mConnectionState = STATE_DISCONNECTED;
						// 重新扫描连接
						if (bLogfile)
							LogUtil.e("start Scan--->onstartcommand:click reconnect" + Util.getTimeMMString());
						find2connect();
						break;
					default:
						break;
					}
				} else if (DEVICE_MODE == 2) {
					bStep2 = false;
					bStep3 = false;

					try {
						if (bLogfile)
							LogUtil.e(TAG, "stopLeScan-> onstartcommand");
						mBluetoothAdapter.stopLeScan(mLeScanCallback);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						// e.printStackTrace();
					}
					scanflag = intent.getIntExtra("scanflag", 0);

					switch (scanflag) {
					case 0:
						if (mConnectionState == STATE_CONNECTED && parser != null) {
							// parser.Start(6);
							if (bLogfile)
								LogUtil.e("-----------------开始");
							updateStart(mBluetoothGatt);

						} else {
							disconnectLast_all();
							find2connect();
						}
						break;
					case 1:
						if (!hasFW) {
							disconnectLast_all();
							find2connect();
						} else {
							updateStart(mBluetoothGatt);
						}

						break;
					case 2:
						if (!hasFW) {
							disableBT(false);
							updateStart(mBluetoothGatt);
						}

						break;
					default:
						break;
					}

					// connect(address);
					// disableBT();
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
		SharedPreferences ss = getSharedPreferences("air", MODE_MULTI_PROCESS);
		openBt = false;
		if (!mBluetoothAdapter.isEnabled()) {
			if (!mBluetoothAdapter.isEnabled()) {
				mBluetoothAdapter.enable();
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (false) {
				// 提示打开蓝牙
				if (!mBluetoothAdapter.isEnabled()) {
					Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
					enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(enableBtIntent);
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
			MyLog.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
			return false;
		}

		/*
		 * // Previously connected device. Try to reconnect. if
		 * (mBluetoothDeviceAddress != null &&
		 * address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null) {
		 * MyLog.d(TAG,
		 * "Trying to use an existing mBluetoothGatt for connection."); for (int
		 * i = 0; i < 1; i++) { if (bLogfile)
		 * LogUtil.i(Calendar.getInstance().getTime().toLocaleString() +
		 * "reconnect: " + "i=" + i); if (mBluetoothGatt.connect()) { //
		 * mConnectionState = STATE_CONNECTING; // reconnect then if (bLogfile)
		 * LogUtil.i(Calendar.getInstance().getTime() .toLocaleString() +
		 * "reconnect: connect ok "); if (mBluetoothGatt.discoverServices()) {
		 * MyLog.i(TAG, "reconnct:Attempting to start service discovery:true");
		 * if (bLogfile) LogUtil.i(Calendar.getInstance().getTime()
		 * .toLocaleString() + "reconnect: discoverServices ok "); return true;
		 * } else { MyLog.i(TAG,
		 * "reconnct:Attempting to start service discovery:false"); if
		 * (bLogfile) LogUtil.i(Calendar.getInstance().getTime()
		 * .toLocaleString() + "reconnect: discoverServices failed ");
		 * 
		 * } } try { Thread.sleep(1000); } catch (InterruptedException e) { //
		 * TODO Auto-generated catch block e.printStackTrace(); } } }
		 */

		/*
		 * disconnectLast_all(); try { Thread.sleep(3000); } catch
		 * (InterruptedException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */

		// final BluetoothDevice device = mBluetoothAdapter
		// .getRemoteDevice(address);
		final BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
		if (device == null) {
			MyLog.w(TAG, "Device not found.  Unable to connect.");
			return false;
		}
		// We want to directly connect to the device, so we are setting the
		// autoConnect
		// parameter to false.
		if (bLogfile)
			LogUtil.i(Calendar.getInstance().getTime().toLocaleString() + "newconnect");
		mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
		if (mBluetoothGatt == null) {
			// refreshDeviceCache(mBluetoothGatt, true);
			return false;
		}

		if (bLogAbc)
			MyLog.v("abc", "device.connectGatt ");
		// mBluetoothGatt.discoverServices();
		MyLog.d(TAG, "Trying to create a new connection.");
		mBluetoothDeviceAddress = address;
		mConnectionState = STATE_CONNECTING;

		if (sharePre == null) {
			sharePre = getSharedPreferences("air", MODE_MULTI_PROCESS);
		}
		sharePre.edit().putString("air_address", address).commit();
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
			Log.e("UPDATE", "disconnect by code");
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
	public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
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

	// 连接设备DATA
	private void EnableFFF2() {
		BluetoothGattCharacteristic characteristic;
		characteristic = mBluetoothGattService.getCharacteristic(UUID_AIR_DATA_F2);
		boolean enabled = true;
		if (characteristic != null) {
			STATE_FW = -1;
			mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

			List<BluetoothGattDescriptor> descriptorl = characteristic.getDescriptors();
			descriptor = descriptorl.get(0);
			if (descriptor != null) {
				descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
				mBluetoothGatt.writeDescriptor(descriptor);
				MyLog.v(TAG, "FW f2 des is send");
			}
		}
	}

	// 连接设备更新固件
	private void Enabel1531() {
		BluetoothGattCharacteristic characteristic;
		characteristic = mBluetoothGattService.getCharacteristic(UUID_AIR_FW_31);

		boolean enabled = true;
		mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

		STATE_FW = STATE_FW_START;
		List<BluetoothGattDescriptor> descriptorl = characteristic.getDescriptors();
		descriptor = descriptorl.get(0);
		if (descriptor != null) {
			descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			mBluetoothGatt.writeDescriptor(descriptor);
			MyLog.v(TAG, "FW 1531 des is send");
		}

	}

	OnePhoneStateListener mOnePhoneStateListener;

	public void createPhoneListener() {
		mOnePhoneStateListener = new OnePhoneStateListener();
		TelephonyManager telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		telephony.listen(mOnePhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		Object obj = getSystemService("phone2");
		if (obj != null) {
			TelephonyManager telM2 = (TelephonyManager) obj;
			telM2.listen(mOnePhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		}

	}

	String nameString;
	int callStatus = TelephonyManager.CALL_STATE_OFFHOOK;

	class OnePhoneStateListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			// MyLog.i(TAG, "[Listener]电话号码:"+incomingNumber);
			if (DEVICE_MODE == 2) {
				return;
			}
			initUserConfig();

			callStatus = state;
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:
				isOnCalling = false;

				// 向设备发送来电提醒
				if (set_isremind_call) {
					if (parser != null) {
						nameString = getContactNameFromPhoneBook(incomingNumber);
						if (true) {
							if (TextUtils.isEmpty(nameString)) {
								nameString = incomingNumber;
							}
							parser.SendNotification(nameString, "呼入来电", true);
						} else {
							parser.SendCallIncome("");

							if (nameString != null && !nameString.equals(incomingNumber)) {
								// 如果找到
								byte[] b = parser.SendCallIncome(nameString);
							} else {
								// 如果没找到
								parser.ComposeCallWithNumber(incomingNumber);
							}
						}
					}
				}
				break;
			case TelephonyManager.CALL_STATE_IDLE:
				isOnCalling = false;
				// 发送 电话挂断
				if (set_isremind_call) {
					if (parser != null) {
						// MyLog.i(TAG, "[Listener]电话挂断发送:"+incomingNumber);
						parser.SendCallEnd();
					}
				}
				if (isdebug && incomingNumber != null) {
					if (bLogfile) {
						LogUtil.e(Calendar.getInstance().getTime().toLocaleString() + "---挂断" + incomingNumber);
					}
				}
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				// MyLog.e(TAG, "[Listener]通话中:"+incomingNumber);
				LogUtil.i(Calendar.getInstance().getTime().toLocaleString() + "phone.state:  offhook");

				isOnCalling = true;
				// 发送 电话挂断
				if (set_isremind_call) {
					if (parser != null) {
						parser.SendCallEnd();
					}
				}
				break;
			}
			super.onCallStateChanged(state, incomingNumber);
		}
	}

	// 根据手机号查找用户姓名
	public String getContactNameFromPhoneBook(String phoneNum) {

		String contactName = null;

		try {
			String[] projection = { ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup.NUMBER };
			Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNum));
			Cursor pCur = this.getContentResolver().query(uri, projection, null, null, null);
			if (pCur.moveToFirst()) {
				contactName = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
				pCur.close();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			if (isdebug) {
				if (bLogfile)
					LogUtil.e("---------查询联系人异常----------");
				if (bLogfile)
					LogUtil.e(e);
				if (bLogfile)
					LogUtil.e("----------------------------");
			}
			e.printStackTrace();
		}
		return contactName;
	}

	private boolean btClose = false;

	static public boolean createBond(Class btClass, BluetoothDevice btDevice) throws Exception {
		Method createBondMethod = btClass.getMethod("createBond");
		Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
		return returnValue.booleanValue();
	}

	static public boolean cancelPairingUserInput(Class btClass, BluetoothDevice btDevice) throws Exception {
		Method createBondMethod = btClass.getMethod("cancelPairingUserInput");
		Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
		return returnValue.booleanValue();
	}

	static public boolean setPairingConfirmation(Class btClass, BluetoothDevice btDevice, boolean boo)
			throws Exception {
		Method createBondMethod = btClass.getMethod("setPairingConfirmation");
		Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice, boo);
		return returnValue.booleanValue();
	}

	private HashMap<String, String> nanJingArgs;

	private class MessageBrocast extends BroadcastReceiver {

		@Override
		public void onReceive(final Context context, Intent intent) {
			// System.out.println("receiver message --->>>>");
			// abortBroadcast();
			final String action = intent.getAction();
			if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
				switch (intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, 1000)) {
				case BluetoothDevice.BOND_BONDED:
					if (bLogfile) {
						LogUtil.e("---------自动配对成功----------");
					}

					break;
				case BluetoothDevice.BOND_BONDING:
					if (bLogfile) {
						LogUtil.e("---------开始自动配对----------");
					}
					break;
				case BluetoothDevice.BOND_NONE:
					if (bLogfile) {
						LogUtil.e("---------未配对----------");
					}
					break;
				}
			}
			if ("android.intent.action.SERVICE_STATE".equals(action)) {
				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					int airState = bundle.getInt("state");

					Log.e(TAG, "飞行模式状态 1为开启状态，0为关闭状态 airState==" + airState);
					switch (airState) {
					case 0: // 飞行模式关闭成功状态
						// handler.sendEmptyMessageDelayed(7, 2000);
						find2connectWait();
						break;
					case 1: // 飞行模式 关闭过程

						break;
					case 3: // 飞行模式开启成功

						break;
					}
				}
			}
			if ("android.bluetooth.device.action.PAIRING_REQUEST".equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (true) {

					boolean pair = false;
					if (Build.VERSION.SDK_INT >= 19) {
						device.setPairingConfirmation(true);
						pair = device.createBond();
						// device.
					} else {
						try {
							setPairingConfirmation(BluetoothDevice.class, device, true);
							pair = createBond(BluetoothDevice.class, device);
							cancelPairingUserInput(BluetoothDevice.class, device);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					{
						abortBroadcast();
					}
				}
			}
			// 150619 读卡号命令
			if (action.equals(ACTION_BLE_CARD_READ)) {
				nanJingArgs = db.getNanJingArgs(address);
				current = 0;
				writePacket(strToBytes("F55F02A001F66F"));
			} else if (action.equals(ACTION_BLE_CARD_TURNON)) {
				// 开机先使能
				HexUid = intent.getStringExtra("hexuid");
				Intent i1 = new Intent(ACTION_BLE_CARD);
				i1.putExtra("text2", "设置的HexUid:" + HexUid + '\n');
				sendBroadcast(i1);
				enableFFE2();
				writePacket(strToBytes("F55F02A100F66F"));

			} else if (action.equals(ACTION_BLE_CARD_UPDATE)) {

				writePacket(strToBytes("F55F02A001F66F"));
				current = 100;
				// enableFFE2();

			}
			// 15-01-06 其他平台通知air
			if (action.equals(AirNotificationListener.ACTION_AIR_NOTIFICATION)) {
				int packet = intent.getIntExtra("Platform", -1);
				switch (packet) {
				case AirNotificationListener.NOTIFICATION_WECHAT:
					if (parser != null) {
						// AirII 微信提醒
						parser.SendMsgIncome(2);
					}
					break;
				case -1:
					// 应用通知提醒
					String title = intent.getStringExtra("title");
					String msg = intent.getStringExtra("msg");
					if (parser != null) {
						parser.SendNotification(title, msg, false);
					}
					break;
				}
			}
			if (action.equals("android.intent.action.LOCALE_CHANGED")) {
				if (parser != null) {
					// intent.putExtra("param", new int[] { 1,
					// sharePre.getInt("lan", 1),
					// sharePre.getInt("liftwrist", 1) });
					SharedPreferences sh2 = context.getSharedPreferences("air", MODE_MULTI_PROCESS);
					String lan = getResources().getConfiguration().locale.getCountry();
					int iLan = sh2.getInt("lan", 1);
					if (iLan == 2) {
						if (lan.equals("CN")) {
							iLan = 1;
						} else if (lan.equals("TW")) {

						} else if (lan.equals("UK")) {
							iLan = 0;
						} else if (lan.equals("US")) {
							iLan = 0;
						}
						parser.setAirDisplay(1, iLan, sh2.getInt("liftwrist", 1));
					}
				}
			}
			if (action.equals(ACTION_PREF_CHANGED)) {
				int temp = intent.getIntExtra("delay", 0);
				if (temp != 0) {
					// 设置延时
					if (parser != null) {
						airDelay = temp;
						parser.SendLostDelay((byte) 1);
					}
				}
				initOftenUsedPref(context, true);

			}

			if (action.equals(ACTION)) {
				initUserConfig();
				if (set_isremind_msg) {
					if (parser != null) {
						parser.SendMsgIncome(1);
						if (true) {

							Bundle bundle = intent.getExtras();
							Object messages[] = (Object[]) bundle.get("pdus");
							String allMessage = "";
							String number = "";
							if (messages != null && messages.length > 0) {
								SmsMessage smsMessage[] = new SmsMessage[messages.length];
								for (int n = 0; n < smsMessage.length; n++) {
									smsMessage[n] = SmsMessage.createFromPdu((byte[]) messages[n]);
								}

								for (SmsMessage message : smsMessage) {

									if (number.equals("")) {
										number = message.getOriginatingAddress();// 得到发件人号码
										allMessage = message.getMessageBody();
									} else if (number.equals(message.getOriginatingAddress())) {
										allMessage += message.getMessageBody();// 得到短信内容
									}
								}
								String title = "";
								title = getContactNameFromPhoneBook(number);
								Intent i = new Intent(AirNotificationListener.ACTION_AIR_NOTIFICATION);
								// 提取验证码
								if (allMessage.contains("验证码") || allMessage.contains("校验码")
										|| allMessage.contains("口令")) {
									String code = AirNotificationListener.parserCheckCode(allMessage);

									if (code != null) {
										allMessage = code;
										title = "验证码";
									}
								}
								if (TextUtils.isEmpty(title)) {
									title = number;
								}
								if (title == null || title.isEmpty() || allMessage == null || allMessage.isEmpty()) {
									return;
								}
								i.putExtra("title", title);
								i.putExtra("msg", allMessage);
								sendBroadcast(i);
							}
						}

						// airVersion
					}
				}
			}

			if (action.equals(ACTION_AIR_CONTROL)) {

				int task = intent.getIntExtra("task", 1);
				switch (task) {
				case 0:
					if (parser != null)
						parser.Start(5);
					break;
				case 1:
					// mHandler.removeCallbacksAndMessages(null);
					mHandler.postDelayed(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub

							SharedPreferences sh2 = context.getSharedPreferences("air", MODE_MULTI_PROCESS);
							sh2.edit().putInt("air_yaoyao_binding_choose", 0).commit();
							parser.setConfigYaoyaoAction(0);
						}
					}, 6 * 10000);
					break;
				case 2:
					// 当进入devicebindBLE 设置为3目的是不报警 不妨丢
					DEVICE_MODE = 3;
					break;
				case 3:
					// 退出 设回1
					DEVICE_MODE = 1;
					// watchdog 3000 立即重连
					auto_connection_counter = 3000;
					break;
				// 2015-01-05 hex下载完成通知
				case 4:
					Intent intents = new Intent(BluetoothLeService.this, FileUpdateActivity.class);
					intents.putExtra("filename", "Air.hex");
					intents.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intents.putExtra("version", intent.getStringExtra("version"));
					BluetoothLeService.this.startActivity(intents);
					break;
				case 5:
					if (parser != null) {
						parser.SendAirHz(intent.getIntExtra("args", 1));
					}
					break;
				case 6:
					Intent ii = new Intent(ACTION_AIR_RSSI_STATUS);
					ii.putExtra("rssi_c", rssi);
					ii.putExtra("rssi_a", (int) rssi_ave);
					ii.putExtra("status", isCon ? 1 : 0);
					ii.putExtra("device_battery", device_battery);
					sendBroadcast(ii);
					break;
				case 7:
					// 重新开启
					if (bLogfile)
						LogUtil.e("-----copy ! restart now" + Util.getTimeMMString());
					if (mBluetoothManager == null) {
						mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
						if (mBluetoothManager == null) {
							MyLog.e(TAG, "Unable to initialize BluetoothManager.");
						}
					}
					mBluetoothAdapter = mBluetoothManager.getAdapter();
					mBluetoothAdapter.enable();
					find2connect();
					break;
				case 806:
					// 读取心率
					if (parser != null) {
						parser.readHr();
					}
					break;
				}
			}

			if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				String stateExtra = BluetoothAdapter.EXTRA_STATE;
				int state = intent.getIntExtra(stateExtra, -1);
				switch (state) {
				case BluetoothAdapter.STATE_TURNING_ON:
					// MyLog.d(TAG, "蓝牙开始开启");

					break;
				case BluetoothAdapter.STATE_ON:
					MyLog.v("log1016", "蓝牙开启");
					if (bLogAbc)
						MyLog.v("abc", "BluetoothAdapter.STATE_ON");
					// MyLog.d(TAG, "蓝牙已经开启");
					if (address != null) {

						if (DEVICE_MODE == 2) {
							if (mBluetoothGatt != null) {
								// uu
								mBluetoothGatt.disconnect();
								mBluetoothGatt.close();
								mBluetoothGatt = null;
							}
							// connect(address);
							if (bLogAbc)
								MyLog.v("abc", "BluetoothAdapter.STATE_ON  find2connect");

							try {
								Thread.sleep(2000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							find2connect();
						}

						if (DEVICE_MODE == 1) {
							find2connect();
						}

						// MyLog.d(TAG, "重启后连接设备"+address);
					} else {
						// MyLog.d(TAG, "重启后连接地址为空"+address);
					}
					break;
				case BluetoothAdapter.STATE_TURNING_OFF:
					// MyLog.d(TAG, "蓝牙开始关闭");
					break;
				case BluetoothAdapter.STATE_OFF:
					MyLog.v("log1016", "蓝牙关闭");
					if (DEVICE_MODE == 2) {
						if (STATE_FW_VALIDATE_2 == STATE_FW) {
							// 升级完成，切换状态
							// 切换模式，进入正常数据模式
							STATE_FW = -1;
							DEVICE_MODE = 1;
							// MyLog.d(TAG, "蓝牙关闭，开始睡眠");
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							// MyLog.d(TAG, "蓝牙关闭，睡眠结束");
							broadcastUpdate(ACTION_FW_STEPS, 5);
						}
						if (BluetoothLeService.this.mBluetoothAdapter != null) {
							// MyLog.d(TAG, "蓝牙已经关闭，开启蓝牙中");
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							BluetoothLeService.this.mBluetoothAdapter.enable();
							if (bLogAbc)
								MyLog.v("abc", "BluetoothLeService.this.mBluetoothAdapter.enable()");
						}
					} else {
						if (isdebug) {
							if (bLogfile)
								LogUtil.e(Calendar.getInstance().getTime().toLocaleString() + "BluetoothClip is OFF");
						}
						SharedPreferences ss = context.getSharedPreferences("air", MODE_MULTI_PROCESS);
						openBt = false;
						btClose = true;
						// 自动打开蓝牙
						if (bLogfile)
							LogUtil.e("保持蓝牙常开：" + openBt);
						if (openBt) {
							if (mBluetoothManager == null) {
								mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
								if (mBluetoothManager == null) {
									MyLog.e(TAG, "Unable to initialize BluetoothManager.");
								}
							}
							mBluetoothAdapter = mBluetoothManager.getAdapter();
							if (mBluetoothAdapter.enable()) {
								if (bLogfile)
									LogUtil.e("自动打开蓝牙--" + Util.getTimeMMString());
							}
							find2connect();
						}

						// if(bLogfile)LogUtil.e("start
						// BleScanHolder------------");
						// openBt = ss.getBoolean("air_open_bt", false);
						// Intent i = new Intent(BluetoothLeService.this,
						// BleScanService.class);
						// i.putExtra("address", address);
						// i.putExtra("order", true);
						// startService(i);
					}
					break;
				}
			}
			if (ACTION_BLE_MANAGE_CMD.equals(action)) {
				int type = intent.getIntExtra("type", 0);
				switch (type) {
				case 0:
					bActiveDisconnect = true;
					disconnectLast_all();
					break;
				case 1:
					disconnectLast_all();
					find2connect();
					break;
				case 2:
					bRssiReport = true;
					break;
				case 3:
					bRssiReport = false;
					break;

				case 20:
					if (parser != null) {
						String nameString = "abcd";
						LogUtil.i(Calendar.getInstance().getTime().toLocaleString() + "phone.state:  ringing "
								+ nameString);
						parser.SendCallIncome(nameString);
					}
					break;

				case 21:
					if (parser != null) {
						// MyLog.i(TAG, "[Listener]电话挂断发送:"+incomingNumber);
						parser.SendCallEnd();
					}
					break;

				default:
					break;
				}
			}
			if (action.equals(ACTION_BT_CONFIG_READ)) {

			}

			if (action.equals(ACTION_BT_CONFIG_WRITE)) {

			}
			if (action.equals(ACTION_BLE_DEVICE_ALARM)) {
				int type = intent.getIntExtra("type", 3);
				MyLog.v("eueu", "alarm " + type);
				switch (type) {
				// end incoming call
				case 3:
					myEndCall();
					break;
				// boss find phone
				case 4:
					onFindPhone(context);
					break;
				// remote control
				case 5:
					SharedPreferences sh1 = context.getSharedPreferences("air", MODE_MULTI_PROCESS);
					int yaoyao_binding_choose = sh1.getInt("air_yaoyao_binding_choose", 1);

					// int yaoyao_binding_choose =
					// sharePre.getInt("air_yaoyao_binding_choose", 1);
					switch (yaoyao_binding_choose) {
					case 0:
						// do nothing
						sendBroadcast(new Intent(ACTION_AIR_YAOYAO));
						break;

					case 1:
						// take picture
						sendBroadcast(new Intent(ACTION_CAMERA_PICTURE));
						break;

					case 2:
						// play sound & vibrate
						if (mediaPlayer == null) {
							mediaPlayer = new MyMediaPlayer(BluetoothLeService.this, true);
							mediaPlayer.start();

						} else {
							mediaPlayer = new MyMediaPlayer(BluetoothLeService.this, true);
							mediaPlayer.start();
						}

						mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

						// long[] pattern = {50, 2000, 500, 2000,500,2000}; //
						// OFF/ON/OFF/ON......
						long[] pattern = { 50, 800, 200, 800, 200, 800, 200, 800, 200, 800, 200, 800, 200, 800, 200,
								800, 200, 800, 200, 800, 200, 800, 200, 800, 200, 800, 200, 400, 200, 400, 200, 400,
								200, 400, 200, 400, 200, 400, 200, 400, 200, 400, 200, 400, 200, 400, 200, 400 }; // OFF/ON/OFF/ON......
						mVibrator.vibrate(pattern, -1);

						break;

					default:
						break;
					}
					break;
				default:
					break;
				}

			}

			if (action.equals(ACTION_BLE_CONFIG_CMD)) {
				int type = intent.getIntExtra("type", 0);
				int[] param = intent.getIntArrayExtra("param");
				String w1 = intent.getStringExtra("w1");
				String w2 = intent.getStringExtra("w2");
				MyLog.v(TAG, "ACTION_BLE_CONFIG_CMD: " + action.toString());
				if (parser != null) {
					try {
						switch (type) {
						case 0:
							parser.readConfig();
							break;
						// vibrate_en
						case 5:
							boolean bb = intent.getBooleanExtra("en", false);
							parser.setVibrateEn(bb);
							break;
						// alarm
						case 7:
							parser.setAlarm(param, w1, w2);
							break;

						// vibrate alarm
						case 11:

							int[] vibrate_params;
							switch (param[0]) {
							// once
							case 0:
								vibrate_params = new int[] { 1, 1, 1, 100, 0 };
								break;
							// liric1
							case 1:
								vibrate_params = new int[] { 1, 1, 10, 40, 20, 40, 20, 40, 20, 40, 20, 40, 20, 100, 30,
										100, 30, 100, 30, 100, 30, 100, 30, };
								break;
							default:
								vibrate_params = new int[] { param[1], 0, 1, 100, 1 };
								break;

							}
							parser.setConfigVibrateAction(vibrate_params);
							break;

						case 12:
							parser.setConfigYaoyaoAction(param[0]);
							break;

						// name
						case 20:
							String nn = intent.getStringExtra("name");
							parser.setName(nn);
							break;

						case 99:
							parser.setConfigShutdown();
							break;
						case 88:
							// 设置air防丢
							parser.setIsLost(param[0]);
							break;
						case 89:
							// 设置air抬腕显示
							parser.setAirDisplay(param[0], param[1], param[2]);
							break;
						case 90:
							parser.readAlarm();
							break;
						default:
							break;
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	byte[] group;
	byte[] HexAll;
	byte checker = 0;

	private void update7816() {
		try {

			File in = new File(
					Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "project.hex");
			if (!in.exists()) {
				Toast.makeText(getApplicationContext(), "缺少Hex文件", 1500).show();
				return;
			}
			FileInputStream fileInputStream = new FileInputStream(in);
			BufferedReader buf = new BufferedReader(new InputStreamReader(fileInputStream));

			String str = null;
			ArrayList<Byte> upData = new ArrayList<Byte>();
			while (true) {
				str = buf.readLine();
				if (str == null) {

					break;
				} else {
					byte[] temp = ParserHex(str);
					if (temp[3] == 0) {
						group = new byte[temp[0]];
						System.arraycopy(temp, 4, group, 0, temp[0]);
						for (byte bit : group) {
							upData.add(bit);
						}

					}

					// upData.add(str);
				}
			}
			HexAll = new byte[upData.size()];
			Byte[] x = new Byte[upData.size()];
			x = upData.toArray(x);
			HexAll = toPrimitives(x);
			x = null;
			buf.close();
			checker(HexAll);
			writePacket(composeStartUpdate());
			current = 101;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void checker(final byte[] alls) {
		new Thread(new Runnable() {
			public void run() {
				checker = 0;
				for (byte bt : alls) {
					checker ^= bt;
				}
				Intent i1 = new Intent(ACTION_BLE_CARD);
				i1.putExtra("text2", "校验为" + "" + Converter.byteToHexString(checker) + "" + '\n');
				sendBroadcast(i1);
			}
		}).start();
	}

	byte[] toPrimitives(Byte[] oBytes) {
		byte[] bytes = new byte[oBytes.length];

		for (int i = 0; i < oBytes.length; i++) {
			bytes[i] = oBytes[i];
		}

		return bytes;
	}

	private byte[] composeStartUpdate() {
		// F3 3F 02 01 73 F4 4F
		byte[] data = new byte[7];
		int i = 0;
		data[i++] = (byte) 0xf3;
		data[i++] = (byte) 0x3f;
		data[i++] = (byte) 0x02;
		data[i++] = (byte) 0x01;
		data[i++] = (byte) 0x73;
		data[i++] = (byte) 0xf4;
		data[i++] = (byte) 0x4f;
		return data;
	}

	private byte[] composeCompleteUpdate() {
		// F3 3F 02 01 73 F4 4F
		byte[] data = new byte[7];
		int i = 0;
		data[i++] = (byte) 0xf3;
		data[i++] = (byte) 0x3f;
		data[i++] = (byte) 0x02;
		data[i++] = (byte) 0x01;
		data[i++] = (byte) 0x31;
		data[i++] = (byte) 0xf4;
		data[i++] = (byte) 0x4f;
		return data;
	}

	private byte[] composePktUpdate(byte[] sub) {
		// F3 3F 13 00 xx xx xx xx xx xx xx xx xx xx xx xx xx xx xx xx F4 4F
		byte[] data = new byte[sub.length + 6];
		data[0] = (byte) 0xf3;
		data[1] = (byte) 0x3f;
		data[2] = (byte) (sub.length + 1);
		data[3] = (byte) 0x00;
		System.arraycopy(sub, 0, data, 4, sub.length);
		data[data.length - 1] = (byte) 0x4f;
		data[data.length - 2] = (byte) 0xf4;
		return data;
	}

	private void disconnectLast_all() {
		rssi_readtime = 0;
		sharePre.edit().remove("soft_version").commit();
		if (bLogAbc)
			Log.e("UPDATE", "disconnectLast_all bycode");
		final BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
		if (device != null) {
			do_disconnectall(device);
			// mDevice = null;
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

	public class SettingsContentObserver extends ContentObserver {
		int previousVolume;
		Context context;

		public SettingsContentObserver(Context c, Handler handler) {
			super(handler);
			context = c;

			AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
			previousVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
		}

		@Override
		public boolean deliverSelfNotifications() {
			return super.deliverSelfNotifications();
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);

			AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
			int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);

			int delta = previousVolume - currentVolume;

			onDeviceLostCancle();
			try {
				if (mVibrator != null)
					mVibrator.cancel();

			} catch (Exception e) {
				// TODO: handle exception
			}
			if (delta > 0) {
				previousVolume = currentVolume;
			} else if (delta < 0) {
				previousVolume = currentVolume;
			}
		}
	}

	private void endCall2() throws Exception {
		if (isdebug) {
			if (bLogfile)
				LogUtil.e("endcall2-start");
		}
		TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		Class tmClazz = tm.getClass();
		Method getITelephonyMethod = tmClazz.getDeclaredMethod("getITelephony", null);
		getITelephonyMethod.setAccessible(true);
		ITelephony iTelephony = (ITelephony) getITelephonyMethod.invoke(tm, null);
		iTelephony.endCall();
		if (isdebug) {
			if (bLogfile)
				LogUtil.e("endcall2-end");
		}
	}

	private void myEndCall() {
		// MyLog.e("TAG", "挂断电话");
		try {
			MyLog.v("eueu", "myEndCall ");
			Method method = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);

			IBinder binder = (IBinder) method.invoke(null, new Object[] { TELEPHONY_SERVICE });

			ITelephony telephony = ITelephony.Stub.asInterface(binder);

			telephony.endCall();
			// telephony.answerRingingCall();
			if (callStatus == TelephonyManager.CALL_STATE_RINGING) {
				endCall2();
			}
			if (isdebug) {
				if (bLogfile)
					LogUtil.e("-------双击挂断无异常---------");
				if (bLogfile)
					LogUtil.e(Calendar.getInstance().getTime().toLocaleString() + '\n' + "手机厂商：" + Build.MANUFACTURER
							+ '\n' + "手机型号:" + Build.MODEL);
				if (bLogfile)
					LogUtil.e("--------看下面电话状态---------");
			}
		} catch (NoSuchMethodException e) {
			MyLog.v("eueu", "NoSuchMethodException ", e);
			if (isdebug) {
				if (bLogfile)
					LogUtil.e("-------双击挂断失效---------");
				if (bLogfile)
					LogUtil.e(Calendar.getInstance().getTime().toLocaleString() + '\n' + "手机厂商：" + Build.MANUFACTURER
							+ '\n' + "手机型号:" + Build.MODEL);
				if (bLogfile)
					LogUtil.e(e);
				if (bLogfile)
					LogUtil.e("------------------------");
			}
		} catch (ClassNotFoundException e) {
			MyLog.v("eueu", "ClassNotFoundException ", e);
			if (isdebug) {
				if (bLogfile)
					LogUtil.e("-------双击挂断失效---------");
				if (bLogfile)
					LogUtil.e(Calendar.getInstance().getTime().toLocaleString() + '\n' + "手机厂商：" + Build.MANUFACTURER
							+ '\n' + "手机型号:" + Build.MODEL);
				if (bLogfile)
					LogUtil.e(e);
				if (bLogfile)
					LogUtil.e("------------------------");
			}
		} catch (Exception e) {
			MyLog.v("eueu", "Exception ", e);
			try {
				// MyLog.e(TAG, "for version 4.1 or larger");
				Intent intent = new Intent("android.intent.action.MEDIA_BUTTON");
				KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK);
				intent.putExtra("android.intent.extra.KEY_EVENT", keyEvent);
				sendOrderedBroadcast(intent, "android.permission.CALL_PRIVILEGED");
				MyLog.v("eueu", "try2 ", e);
			} catch (Exception e2) {
				if (isdebug) {
					if (bLogfile)
						LogUtil.e("-------双击挂断失效---------");
					if (bLogfile)
						LogUtil.e(Calendar.getInstance().getTime().toLocaleString() + '\n' + "手机厂商："
								+ Build.MANUFACTURER + '\n' + "手机型号:" + Build.MODEL);
					if (bLogfile)
						LogUtil.e(e2);
					if (bLogfile)
						LogUtil.e("------------------------");
				}
				MyLog.v("eueu", "Exception 2", e);
				MyLog.d(TAG, "", e2);
				Intent meidaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
				KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK);
				meidaButtonIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
				sendOrderedBroadcast(meidaButtonIntent, null);

			}
		}
	}

	private void onFindPhone(Context context) {
		// MODE_MULTI_PROCESS is 4
		initOftenUsedPref(context, false);
		if (set_find_phone && !isOnCalling) {
			if (!isOnFinding) {
				if (mediaPlayer != null) {
					try {
						mediaPlayer.Stop();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				mediaPlayer = new MyMediaPlayer(this, false);
				mediaPlayer.start();
				showAirFoundNotification();

				AlertDialog.Builder builder = new AlertDialog.Builder(Util.getThemeContext(this));
				builder.setTitle(R.string.notification_found_title);
				builder.setMessage(R.string.notification_found_content);

				builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
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
				AlertDialog dialog = builder.create();
				dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
				dialog.show();
			}
		}
	}

	public void showAirFoundNotification() {
		NotificationUtils not = new NotificationUtils(this, NotificationUtils.FoundNotfication);
		not.showNotification(getString(R.string.notification_found_title),
				getString(R.string.notification_found_content), AirPreferenceActivity.class, false);
	}

	private byte[] ParserHex(String str) {
		int len = (str.length() - 1) / 2;
		byte[] data = new byte[len];
		// char[] aa=new char[2];
		String ss1 = str.substring(1, str.length());
		char[] cc = ss1.toCharArray();
		data = LeFileHex.decodeHex(cc);
		// data=HexString2Bytes(ss1);
		/*
		 * for(int i=0;i<len;i++) { //aa[0] = str.charAt(1+i*2); //aa[1] =
		 * str.charAt(2+i*2); //String jj = String.valueOf(aa);
		 * //data[i]=(byte)Integer.parseInt(jj,16);
		 * data[i]=(byte)Integer.parseInt(str.substring(1+i*2, 3+i*2), 16);
		 * //data[i]=str.substring(1+i*2, 3+i*2).getBytes()[0];
		 * //data[i]=(byte)Integer.parseInt("08", 16); //data[i]=(byte)(0xaa); }
		 */
		return data;
	}

	public static String getNjCheck(String hexUid, String cardChallenge, String F01, String F14, String F15,
			String F05) {
		NetworkingOperator networkingOperator = NetworkingOperator.getSharedOperator();
		networkingOperator.openConnection();

		try {
			HashMap<String, String> queryJsonData = new HashMap<String, String>();
			queryJsonData.put("F05", F05);
			queryJsonData.put("F15", F15);
			queryJsonData.put("F14", F14);
			queryJsonData.put("F01", F01);
			queryJsonData.put("KA", "0");
			ProcessResult pr = networkingOperator.queryPaymentAuth(hexUid, F05, F15, F14, F01, cardChallenge);
			networkingOperator.shutdownConnection();
			if (!pr.hasError()) {
				return (String) pr.getData().get("apdu");
			} else {
				return String.valueOf(pr.getStatus());
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public void onRringOver() {
		// TODO Auto-generated method stub
		losttype = 0;
	}

}
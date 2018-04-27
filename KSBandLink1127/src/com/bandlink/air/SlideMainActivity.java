package com.bandlink.air;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bandlink.air.ble.AirTemperatureService;
import com.bandlink.air.ble.BluetoothLeService;
import com.bandlink.air.ble.LogUtil;
import com.bandlink.air.club.InitPerson;
import com.bandlink.air.club.SleepActivity;
import com.bandlink.air.gps.GPSEntity;
import com.bandlink.air.gps.GPSPointEntity;
import com.bandlink.air.gps.GPSUploadThread;
import com.bandlink.air.gps.RunNowFragment;
import com.bandlink.air.simple.AlarmFragment;
import com.bandlink.air.simple.AllRankActivity;
import com.bandlink.air.simple.CardFragment;
import com.bandlink.air.simple.HeartFragment;
import com.bandlink.air.simple.RankActivity;
import com.bandlink.air.simple.SimpleHomeFragment;
import com.bandlink.air.simple.SyncQQHealth;
import com.bandlink.air.simple.TemperatureFragment;
import com.bandlink.air.slidemenu.SlidingMenu;
import com.bandlink.air.update.UpdateActivity;
import com.bandlink.air.user.LoginActivity;
import com.bandlink.air.util.Base64Coder;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.LovefitSlidingActivity;
import com.bandlink.air.util.MainInterface;
import com.bandlink.air.util.MyDate;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Util;
import com.bandlink.air.view.NoRegisterDialog;
import com.umeng.analytics.MobclickAgent;

public class SlideMainActivity extends LovefitSlidingActivity implements
		MainInterface {

	private int item = 0;
	private int uid = -1;

	private long firstime = 0;
	public static boolean isGpsRunning = false;
	public static boolean bCenter = true;

	private String time;
	private SharedPreferences preferences;
	private String TAG = "SlideMainActivity";
	private Context mContext;
	private Dbutils db;
	private String session_id = "";
	private String session_time = "";
	private String tuser = null;
	private String tpwd = null;
	private int tuid = -1;
	private int ismember = -1;
	private String lovefitid;
	SharedPreferences settings;
	SharedPreferences airPreference;
	private String str;
	private View mView;
	SharePreUtils spUtils;
	public static String connectName = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		mContext = Util.getThemeContext(this);
		mView = getLayoutInflater().inflate(R.layout.main_frame, null);
		setContentView(mView);
		preferences = getApplicationContext().getSharedPreferences(
				SharePreUtils.APP_ACTION, Context.MODE_PRIVATE);
		airPreference = getSharedPreferences("air", Context.MODE_MULTI_PROCESS);
		airSetMode = airPreference.getInt("airmode", 1);
		uid = preferences.getInt("UID", -1);
		spUtils = SharePreUtils.getInstance(this);
		db = new Dbutils(this);
		InitUser();
		if (spUtils.getMainMode() == 3) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.main_frame, new TemperatureFragment()).commit();
		} else {
			SimpleHomeFragment ss = new SimpleHomeFragment();
			Bundle args = new Bundle();
			args.putBoolean("oncreate",
					!getIntent().getBooleanExtra("fromguide", false));
			ss.setArguments(args);
			getSupportFragmentManager().beginTransaction()
					.add(R.id.main_frame, ss).commit();
		}

		initSlidingMenu();

		str = new SimpleDateFormat("yyyyMMdd").format(new Date());

		IntentFilter filter = new IntentFilter(
				BluetoothLeService.ACTION_AIR_YAOYAO);
		filter.addAction(BluetoothLeService.ACTION_CAMERA_PICTURE);
		filter.addAction(SyncQQHealth.ACTION_QQHEALTH);
		filter.addAction(BluetoothLeService.ACTION_PREF_CHANGED);
		filter.addAction(BluetoothLeService.ACTION_BLE_BIND_DEVICE_CHANGED);
		filter.addAction(BluetoothLeService.ACTION_AIR_RSSI_STATUS);
		filter.addAction(BluetoothLeService.ACTION_BLE_CONFIG_READ_RESPONSE);
		registerReceiver(airReceiver, filter);
		LogUtil.e("TEST",
				"sliding over" + Util.getTimeMMStringFormat("HH:mm:ss:SSS"));
	}

	@SuppressLint("NewApi")
	void updateDialog(String msg, final int type) {

		int check_app = preferences.getInt("app_check_date", 0);
		int check_air = preferences.getInt("air_check_date", 0);

		if (Integer.parseInt(str) <= check_app && type == 1) {
			return;
		}
		if (Integer.parseInt(str) <= check_air && type == 2) {
			return;
		}

		AlertDialog.Builder ab = new AlertDialog.Builder(mContext);
		final Intent i = new Intent();
		String title;
		if (type == 1) {
			i.setClass(SlideMainActivity.this, UpdateActivity.class);
			title = getString(R.string.app_update_enable);
			preferences.edit().putInt("app_check_date", Integer.parseInt(str))
					.commit();
			ab.setIcon(R.drawable.ic_launcher);
			String url = preferences.getString("appupdate_url", null);
			if (url != null) {
				NotificationManager mNotificationManager = (NotificationManager) mContext
						.getSystemService(Context.NOTIFICATION_SERVICE);
				Uri uri = Uri.parse(url);
				// 通过Uri获得编辑框里的//地址，加上http://是为了用户输入时可以不要输入
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				android.app.Notification notification = new NotificationCompat.Builder(
						mContext)
						.setSmallIcon(R.drawable.ic_launcher)
						.setContentInfo("")
						.setContentTitle("Lovefit版本升级")
						// .setContentText()
						.setSubText("点击此处下载")
						.setAutoCancel(true)
						.setDefaults(android.app.Notification.DEFAULT_ALL)
						.setContentIntent(
								PendingIntent.getActivity(mContext, 111,
										intent, Intent.FLAG_ACTIVITY_NEW_TASK))
						.build();
				notification.sound = null;
				notification.vibrate = null;
				mNotificationManager.notify(156, notification);
			}
		} else {
			if (Build.VERSION.SDK_INT < 18) {
				return;
			}
			ab.setIcon(R.drawable.air_small);
			i.setClass(SlideMainActivity.this, AirPreferenceActivity.class);
			title = getString(R.string.app_update_enable_air);
			preferences.edit().putInt("air_check_date", Integer.parseInt(str))
					.commit();
		}
		ab.setTitle(title);
		ab.setMessage(msg);
		ab.setNegativeButton(R.string.upgrade_upgrading,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						startActivity(i);
					}
				});
		ab.setPositiveButton(R.string.tell_me_later,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				});
		if (android.os.Build.VERSION.SDK_INT >= 18 && type == 1) {
			// 在4.3以下不执行下段，故忽略检查
			ab.setOnDismissListener(new DialogInterface.OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					// TODO Auto-generated method stub
					if (preferences.getBoolean("airupdate", false)) {
						updateDialog(
								preferences.getString("airupdate_context", ""),
								2);
					}
				}
			});
		}
		ab.show();

	}

	/*
	 * public void getLastestVersion(final String ver) { MyLog.e(TAG, "now:" +
	 * ver); // 只有在4.3 及已经绑定air时 进行检查更新 try { if (db.getUserDeivceType() != 5 ||
	 * Build.VERSION.SDK_INT < 18) { return; } } catch (Exception e) {
	 * e.printStackTrace(); return; } new Thread(new Runnable() {
	 * 
	 * @Override public void run() { // TODO Auto-generated method stub String
	 * result = HttpUtlis .queryStringForPost(HttpUtlis.UPDATE_AIR);
	 * 
	 * try { result = new String(result.getBytes("iso8859-1"), "utf-8");
	 * JSONObject js = new JSONObject(result); String newVersion =
	 * js.getString("verName"); String newVersionText = js.getString("text");
	 * 
	 * if (HttpUtlis.checkAir(ver, newVersion)) { handler.obtainMessage(4,
	 * newVersionText).sendToTarget(); }
	 * preferences.edit().putString("lastest_air", newVersion) .commit(); }
	 * catch (Exception e) { e.printStackTrace();
	 * 
	 * } } }).start(); }
	 */

	public void InitUser() {
		String user = preferences.getString("USERNAME", null);
		String pwd = preferences.getString("PASSWORD", null);
		uid = preferences.getInt("UID", -1);
		if (user == null || pwd == null || uid == -1) {
			new Thread() {
				public void run() {
					String urlStr = HttpUtlis.BASE_URL + "/user/getTempUser";
					String result = null;
					try {
						Map<String, String> params = new HashMap<String, String>();
						System.out.println("试用用户的标号" + getMyUUID());
						params.put("mobileid", getMyUUID());
						params.put("from", "1");
						MyLog.e("试用开始", Calendar.getInstance().getTime()
								.toString());
						result = HttpUtlis.getRequest(urlStr, params);
						MyLog.e("试用结束", Calendar.getInstance().getTime()
								.toString());
						if (result != null) {
							JSONObject json, ob = null;
							int code = -1;
							json = new JSONObject(result);
							ob = json.getJSONObject("content");
							code = Integer.parseInt(json.getString("status"));
							if (code == 0) {
								tuid = Integer.parseInt(ob.getString("uid"));
								session_id = ob.getString("session_id");
								session_time = ob.getString("session_time");
								tuser = ob.getString("username");
								tpwd = ob.getString("password");
								ismember = ob.getInt("ismember");
								lovefitid = ob.getString("lovefitid");
								int sex = Integer.parseInt(ob.getString("sex"));
								int height = Integer.parseInt(ob
										.getString("height"));
								int weight = Integer.parseInt(ob
										.getString("weight"));
								JSONObject accobj = json
										.getJSONObject("account");
								String email = accobj.getString("email");
								String mobile = accobj.getString("mobile");

								db = new Dbutils(tuid, SlideMainActivity.this);
								db.InitUser(tuid, "", ismember, sex, height,
										weight, lovefitid, email, mobile,
										ob.has("birth") ? ob.getString("birth")
												: "", "");

								JSONObject tarCon = json
										.getJSONObject("target");
								db.InitTarget(Integer.parseInt(tarCon
										.getString("type")), Double
										.parseDouble(tarCon
												.getString("weight_bef")),
										Double.parseDouble(tarCon
												.getString("weight_end")),
										Integer.parseInt(tarCon
												.getString("step")),
										Integer.parseInt(tarCon
												.getString("sleepmode")),
										Integer.parseInt(tarCon
												.getString("weightmode")),
										Integer.parseInt(tarCon
												.getString("bmimode")));

								JSONObject device = json
										.getJSONObject("device");

								db.InitDeivce(Integer.parseInt(device
										.getString("devicetype")), 0, device
										.getString("deviceid"), "");
								handler.sendEmptyMessage(1);
							}

						} else {
							handler.sendEmptyMessage(2);
						}

					} catch (Exception e) {
						// TODO Auto-generated catch block
						uid = -1;
						preferences.edit().putInt("UID", uid).commit();

						preferences.edit().putString("session_id", null)
								.commit();
						preferences.edit().putString("session_time", null)
								.commit();
						preferences.edit().putString("USERNAME", null).commit();
						preferences.edit().putString("PASSWORD", null).commit();
						preferences.edit().putInt("ISMEMBER", 0).commit();

						airPreference.edit().putInt("UID", uid).commit();
						airPreference.edit().putString("USERNAME", null)
								.commit();
						airPreference.edit().putString("PASSWORD", null)
								.commit();
						airPreference.edit().putInt("ISMEMBER", 0).commit();

						handler.sendEmptyMessage(2);
						e.printStackTrace();
					}
				}

			}.start();
		} else {
			// LogUtil.e("---Slide读取设备类型发送开服务广播---"+new
			// SimpleDateFormat("yyyy-MM-dd-HH: mm: ss-SSS").format(new
			// Date()));
			// Object device = db.getUserDeivce();
			// if (device != null) {
			// Intent intent = new Intent("milinkStartService");
			// sendBroadcast(intent);
			// }
		}

	}

	// 试用的handler
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				Toast.makeText(SlideMainActivity.this, R.string.tryfirst,
						Toast.LENGTH_LONG).show();
				break;
			case 1:
				airPreference.edit().putInt("UID", tuid).commit();
				airPreference.edit().putString("USERNAME", tuser).commit();
				airPreference.edit().putString("PASSWORD", tpwd).commit();
				airPreference.edit().putInt("ISMEMBER", ismember).commit();

				preferences.edit().putInt("UID", tuid).commit();
				preferences.edit().putString("session_id", session_id).commit();
				preferences.edit().putString("session_time", session_time)
						.commit();
				preferences.edit().putString("USERNAME", tuser).commit();
				preferences.edit().putString("PASSWORD", tpwd).commit();
				preferences.edit().putInt("ISMEMBER", ismember).commit();
				uid = preferences.getInt("UID", -1);

				ismember = preferences.getInt("ISMEMBER", 0);
				if (ismember == 0) {
					Toast.makeText(SlideMainActivity.this,
							getString(R.string.trial), Toast.LENGTH_SHORT)
							.show();
				}
				MyLog.e("temper", "milinkStartService");

				Intent intent = new Intent();
				intent.setAction("milinkStartService");// 用户检查用户类型,打开serivce
				intent.putExtra("update", true);
				sendBroadcast(intent);
				if (preferences.getInt("ISMEMBER", 0) == 0) {
					Intent i = new Intent(SlideMainActivity.this,
							InitPerson.class);
					i.putExtra("flag", 0);// 0表示使用用户
					startActivity(i);
				}
				break;
			case 2:// 试用的时候没有联网
				db = new Dbutils(preferences.getInt("UID", -1),
						SlideMainActivity.this);
				db.InitTarget(0, 70.0, 50.0, 10000, 0, 1, 1);
				int year = preferences.getInt("year", 1990);
				int month = preferences.getInt("month", 1);
				db.InitUser(uid, "", 0, 0, 170, 50.0f, "", "", "", year
						+ getString(R.string.year) + (month)
						+ getString(R.string.month), "");
				db.InitDeivce(2, 0, "", "");
				Toast.makeText(SlideMainActivity.this,
						getString(R.string.network_erro), Toast.LENGTH_SHORT)
						.show();

				break;
			case 3:// 更新用户数据成功
				if (preferences.getInt("ISMEMBER", 0) == 0) {
					Toast.makeText(SlideMainActivity.this,
							getString(R.string.trial), Toast.LENGTH_SHORT)
							.show();
				}
				MyLog.e("更新用户数据", "milinkStartService");

				Intent intent1 = new Intent();
				intent1.setAction("milinkStartService");// 用户检查用户类型,打开serivce
				intent1.putExtra("update", true);
				sendBroadcast(intent1);

				break;
			case 4:
				// updateDialog(msg.obj.toString(), 2);
				break;
			case 11:
				preferences.edit().putBoolean("airupdate", true).commit();
				preferences.edit()
						.putString("airupdate_context", msg.obj.toString())
						.commit();
				break;
			case 12:
				// air最新
				preferences.edit().putBoolean("airupdate", false).commit();
				break;

			}
		}
	};

	ConnectivityManager cm;

	public boolean isNetworkConnected() {
		try {
			cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		NetworkInfo ni = cm.getActiveNetworkInfo();
		return ni != null && ni.isConnectedOrConnecting();
	}

	private void InitUserData() {
		uid = preferences.getInt("UID", -1);
		db = new Dbutils(this);
		if (uid != -1) {
			getUserInfoFromNet();
		}

	}

	public void getUserInfoFromNet() {

		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Map<String, String> args = new HashMap<String, String>();
					args.put("session", preferences.getString("session_id", ""));
					String result = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/user/getUserData", args);

					JSONObject object = new JSONObject(result);
					if (object.getInt("status") == 0) {
						JSONObject tarCon = new JSONObject(new JSONObject(
								result).get("target").toString());
						db = new Dbutils(preferences.getInt("UID", -1),
								SlideMainActivity.this);
						db.InitTarget(
								Integer.parseInt(tarCon.getString("type")),
								Double.parseDouble(tarCon
										.getString("weight_bef")),
								Double.parseDouble(tarCon
										.getString("weight_end")),
								Integer.parseInt(tarCon.getString("step")),
								Integer.parseInt(tarCon.getString("sleepmode")),
								Integer.parseInt(tarCon.getString("weightmode")),
								Integer.parseInt(tarCon.getString("bmimode")));

						JSONObject Info = new JSONObject(new JSONObject(result)
								.get("content").toString());

						db.UpdateUserInfo(Info.getString("nickname"),
								Integer.parseInt(Info.getString("sex")),
								Double.parseDouble(Info.getString("height")),
								Double.parseDouble(Info.getString("weight")));

						JSONObject device = new JSONObject(new JSONObject(
								result).get("device").toString());

						db.InitDeivce(Integer.parseInt(device
								.getString("devicetype")), 0, device
								.getString("deviceid"), "");

					}

					handler.sendEmptyMessage(3);
				} catch (Exception e) {
					// TODO Auto-generated catch block

					e.printStackTrace(System.err);

				}

			}
		}).start();

	}

	private String getMyUUID() {

		final TelephonyManager tm = (TelephonyManager) getBaseContext()
				.getSystemService(Context.TELEPHONY_SERVICE);
		final String tmDevice, tmSerial, tmPhone, androidId;
		tmDevice = "" + tm.getDeviceId();
		tmSerial = "" + tm.getSimSerialNumber();
		androidId = ""
				+ android.provider.Settings.Secure.getString(
						getContentResolver(),
						android.provider.Settings.Secure.ANDROID_ID);

		UUID deviceUuid = new UUID(androidId.hashCode(),
				((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
		String uniqueId = deviceUuid.toString();
		return uniqueId;
	}

	public void checkSession() {

		if (preferences.getInt("UID", -1) > 0) {
			time = new SimpleDateFormat("yyyyMMdd").format(new Date());
			String session_time = preferences.getString("session_time", null);
			if (session_time != null && !session_time.equals(time)) {
				getSession();
				InitUserData();
				new Thread(new Runnable() {
					public void run() {
						upOpen();
					}
				}).start();
			} else {

			}
		}
	}

	void upOpen() {
		String now = Util.getTimeMMStringFormat("yyyyMMdd");
		if (!db.getAirInfoNeedUpLoad(MyDate.getFileName())) {
			return;
		}
		if (Integer.valueOf(preferences.getString("open_date", "0")) >= Integer
				.valueOf(now)) {
			return;

		}
		HashMap<String, String> args2 = new HashMap<String, String>();
		args2.put("session", preferences.getString("session_id", ""));
		args2.put("deviceid", "0");
		args2.put("time", MyDate.getFileName());
		args2.put("battery", "0");
		args2.put("alert", "0");
		args2.put("ble", "0");
		args2.put("screen", "0");
		args2.put("vibrate", "0");
		// try {
		// //String s =
		// HttpUtlis.getRequest(HttpUtlis.BASE_URL+"/user/setUserAirDevice",
		// args);
		// //System.out.println(s);
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		try {
			String s = HttpUtlis.getRequest(HttpUtlis.BASE_URL
					+ "/user/uploadDeviceInfo", args2);
			if (s.contains("ok")) {
				preferences.edit().putString("open_date", now).commit();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static String getSession(SharedPreferences preferences)
			throws Exception {
		Map<String, String> logoargs = new HashMap<String, String>();
		logoargs.put("user", preferences.getString("USERNAME", ""));
		if (preferences.getString("USERNAME", "").startsWith("SinaWeibo_")
				|| preferences.getString("USERNAME", "").startsWith("QZone_")) {
			logoargs.put("pwd", LoginActivity.MD5("333333"));
		} else {
			logoargs.put("pwd", preferences.getString("PASSWORD", ""));
		}

		String s = HttpUtlis.getRequest(HttpUtlis.BASE_URL
				+ "/user/"
				+ (preferences.getBoolean("ispa", false) ? "getloginPa"
						: "getlogin"), logoargs);
		// LogUtil.e(s);
		JSONObject status = new JSONObject(s);
		if (status.getInt("status") == 0) {
			JSONObject js = new JSONObject(
					new JSONObject(s).getString("content"));
			String session_id = js.getString("session_id");
			String time = js.getString("session_time");
			preferences.edit().putString("session_id", session_id).commit();
			preferences.edit().putString("session_time", time).commit();
			return session_id;
		}
		return null;
	}

	void getSession() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Map<String, String> logoargs = new HashMap<String, String>();

				String s = "";
				try {
					if (preferences.getString("qq_openid", null) != null
							&& !preferences.getBoolean("ispa", false)) {
						logoargs.put("source", "QZone");
						logoargs.put("sid",
								preferences.getString("qq_openid", null));
						s = HttpUtlis.getRequest(HttpUtlis.BASE_URL
								+ "/user/getLoginThirdParty", logoargs);
					} else {
						logoargs.put("user",
								preferences.getString("USERNAME", ""));
						if (preferences.getString("USERNAME", "").startsWith(
								"SinaWeibo_")
								|| preferences.getString("USERNAME", "")
										.startsWith("QZone_")) {
							logoargs.put("pwd", LoginActivity.MD5("333333"));
						} else {
							logoargs.put("pwd",
									preferences.getString("PASSWORD", ""));
						}
						s = HttpUtlis.getRequest(
								HttpUtlis.BASE_URL
										+ "/user/"
										+ (preferences
												.getBoolean("ispa", false) ? "getloginPa"
												: "getlogin"), logoargs);
					}

					// LogUtil.e(s);
					JSONObject status = new JSONObject(s);
					// {"status":0,"message":"ok","content":{"uid":"96","session_id":"144656640096","session_time":"20151104","ismember":1,"username":"mshen2","password":"d93e7a977b98afa192b3cfef2cad9140","nickname":"mshen2","lovefitid":"22894825"},"account":{"mobile":"","email":"shenmeng_boya@163.com"},"bind":[{"id":"1485","uid":"96","openid":"E276EA87BC8C6E892DAC6403C9BBF33E","source":"QZone","bind":"1","updatetime":"1446552970","inserttime":"1446552970"}]}
					if (status.getInt("status") == 0) {
						JSONObject js = new JSONObject(new JSONObject(s)
								.getString("content"));
						String session_id = js.getString("session_id");
						String time = js.getString("session_time");
						preferences.edit().putString("session_id", session_id)
								.commit();
						preferences.edit().putString("session_time", time)
								.commit();
						if (status.has("bind")) {
							JSONArray arr = status.getJSONArray("bind");
							// "bind":[{"id":"1485","uid":"96","openid":"E276EA87BC8C6E892DAC6403C9BBF33E","source":"QZone","bind":"1","updatetime":"1446552970","inserttime":"1446552970"}]
							for (int i = 0; i < arr.length(); i++) {
								JSONObject obj = arr.getJSONObject(i);
								if (obj.has("openid")) {
									preferences
											.edit()
											.putString("qq_openid",
													obj.getString("openid"))
											.commit();
								}
							}
						}

						upOpen();
					} else {
						// preferences
						// .edit()
						// .putString(
						// "session_time",
						// new SimpleDateFormat("yyyyMMdd")
						// .format(new Date())).commit();
						Toast.makeText(getApplicationContext(),
								getString(R.string.getsessionerror),
								Toast.LENGTH_LONG).show();
						Intent intent = new Intent(SlideMainActivity.this,
								LoginActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
						startActivity(intent);
						SlideMainActivity.this.finish();
						android.os.Process.killProcess(android.os.Process
								.myPid());

					}
				} catch (Exception e) {
					preferences
							.edit()
							.putString(
									"session_time",
									new SimpleDateFormat("yyyyMMdd")
											.format(new Date())).commit();
					Intent intent = new Intent(SlideMainActivity.this,
							LoginActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
					startActivity(intent);
					SlideMainActivity.this.finish();
				}
			}
		}).start();

	}

	MenuFragment menu;

	public void initSlidingMenu() {

		setBehindContentView(R.layout.menu_frame);
		menu = new MenuFragment();
		Bundle bundle = new Bundle();
		bundle.putInt("item", item);
		menu.setArguments(bundle);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.menu_frame, menu).commit();
		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);

	}

	public void refreshMenu(boolean isKs) {
		if (menu != null) {
			menu.loadMenu(SharePreUtils.getInstance(this).getMainMode(),
					isKs ? 2 : 1);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	OnSharedPreferenceChangeListener lis = new OnSharedPreferenceChangeListener() {

		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			// TODO Auto-generated method stub
			if (key.equals("soft_version")) {
				String ver = sharedPreferences.getString("soft_version", null);
				if (ver != null) {
					getLastestVersion(ver);
				}
			}
		}
	};

	@Override
	protected void onResume() {
		if (airPreference != null) {
			airPreference.registerOnSharedPreferenceChangeListener(lis);
		}

		if (isNetworkConnected()) {
			checkSession();
			if (preferences.getInt("ISMEMBER", 0) != 0) {
				checkGpsupLoad();
			}
			if (preferences.getBoolean("appupdate", false)) {
				updateDialog(preferences.getString("appupdate_context", ""), 1);
			}
		}
		SlideMainActivity.connectName = preferences
				.getString("connectName", "");
		MobclickAgent.onResume(this);
		super.onResume();
	}

	public void getLastestVersion(final String ver) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				String result = HttpUtlis
						.queryStringForPost(HttpUtlis.UPDATE_AIR);

				try {
					result = new String(result.getBytes("iso8859-1"), "utf-8");
					JSONObject js = new JSONObject(result);
					String newVersion = null;
					String newVersionText = null;
					if (ver.toUpperCase().contains("AIRIII")) {
						newVersion = js.getString("verNameIII");
						newVersionText = js.getString("textIII");
					} else if (ver.toUpperCase().contains("AIRII")) {
						newVersion = js.getString("verNameII");
						newVersionText = js.getString("textII");
					} else if (ver.toUpperCase().contains("AIR")) {
						newVersion = js.getString("verName");
						newVersionText = js.getString("text");
					}
					if (HttpUtlis.checkAir(ver, newVersion) > 0) {
						handler.obtainMessage(11, newVersionText)
								.sendToTarget();
					} else {
						handler.obtainMessage(12).sendToTarget();
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}).start();
	}

	private void checkGpsupLoad() {
		new Thread() {
			public synchronized void run() {
				try {
					db = new Dbutils(SlideMainActivity.this);
					ArrayList<Integer> ids = db.getUpLoadGPSIds();
					if (ids != null) {
						for (int i = 0; i < ids.size(); i++) {
							ArrayList<GPSEntity> Tlist = db
									.getGPSTrackCursorById(ids.get(i));
							ArrayList<GPSPointEntity> Plist = db
									.getGPSPointCursor(ids.get(i));
							if (Tlist.get(0).distance < 100) {
								continue;
							}
							GPSUploadThread gpsup = GPSUploadThread
									.getInstance();
							gpsup.setPostEntity(
									preferences.getString("session_id", ""),
									SlideMainActivity.this, Tlist, Plist);
							gpsup.Write2DatUpload(ids.get(i));
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}.start();

	}

	@Override
	protected void onPause() {
		MobclickAgent.onPause(this);
		airPreference.unregisterOnSharedPreferenceChangeListener(lis);
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(airReceiver);

		super.onDestroy();
	}

	@Override
	public void setContentView(int id) {
		// TODO Auto-generated method stub
		super.setContentView(id);
	}

	void showModeSwitch() {
		AlertDialog.Builder ab = new AlertDialog.Builder(
				Util.getThemeContext(this));
		ab.setTitle("切换模式");
		ab.setItems(new String[] { "运动健康模式", "体温检测模式", "心电检测" },
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						switch (which) {
						case 0:
							if (spUtils.getMainMode() == 3) {
								stopMyService(AirTemperatureService.class);
								spUtils.putMainMode(0);
								SlideMainActivity.this.onToggle();
								SimpleHomeFragment s = new SimpleHomeFragment();
								Bundle args = new Bundle();
								args.putBoolean("oncreate", true);
								s.setArguments(args);
								switchContent(s);
								monModeClick.onModeClick(0);
							}
							break;
						case 1:
							if (spUtils.getMainMode() == 0) {
								stopMyService(BluetoothLeService.class);
								spUtils.putMainMode(3);
								SlideMainActivity.this.onToggle();
								switchContent(new TemperatureFragment());
								monModeClick.onModeClick(3);
							}

							break;
						case 2:
							startActivity(new Intent(SlideMainActivity.this,
									HeartFragment.class));

							break;
						}
					}
				});
		ab.setNegativeButton(R.string.cancel, null);
		ab.create().show();
	}

	void stopMyService(Class classname) {
		try {
			Intent i = new Intent(this, classname);
			stopService(i);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onNavigationDrawerImageClick(int id) {
		// TODO Auto-generated method stub
		switch (id) {
		case -4:
			showModeSwitch();
			break;
		case -1:
			this.onToggle();
			startActivity(new Intent(SlideMainActivity.this,
					PersonalSettings.class));
			break;
		case -2:
			this.onToggle();
			startActivity(new Intent(SlideMainActivity.this,
					ExpandActivity.class));
			break;
		case R.drawable.ic_home:
			if (spUtils.getMainMode() == 3) {
				this.onToggle();
				switchContent(new TemperatureFragment());
			} else {
				this.onToggle();
				SimpleHomeFragment s = new SimpleHomeFragment();
				Bundle args = new Bundle();
				args.putBoolean("oncreate", true);
				s.setArguments(args);
				switchContent(s);
			}
			break;
		case R.drawable.clock2:
			this.onToggle(); 
			switchContent(new AlarmFragment());
			break;
		case R.drawable.running:
			this.onToggle();
			switchContent(new DataChartActivity());
			break;
		case R.drawable.sleep_icon:
			this.onToggle();
			switchContent(new SleepActivity());
			break;
		case R.drawable.shadow2:
			this.onToggle();
			switchContent(new RankActivity());
			break;
		case R.drawable.ic_club:
			this.onToggle();
			switchContent(new ClubFragment());
			break;
		case R.drawable.heartrate_dark:
			this.onToggle();
			switchContent(new HrChartActivity());
			break;
		case R.drawable.ic_card:
			this.onToggle();
			switchContent(new CardFragment());
			break;
		case R.drawable.ic_device:
			this.onToggle();
			//switchContent(new DeviceManager());
			break;
		case R.drawable.ic_setting:
			this.onToggle();
			switchContent(new SettingsFragment());
			break;
		}
	}

	@Override
	public void onToggle() {
		// TODO Auto-generated method stub
		MenuFragment menu = (MenuFragment) getSupportFragmentManager()
				.findFragmentById(R.id.menu_frame);
		menu.updateUserInfo();
		this.toggle();

	}

	@Override
	public void removeFragment() {
		// TODO Auto-generated method stub
		FragmentManager fm = getSupportFragmentManager();
		Fragment fragment = fm.findFragmentById(R.id.main_frame);
		if (fragment != null)
			fm.beginTransaction().remove(fragment).commit();
	}

	public void switchContent(Fragment fragment) {
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.main_frame, fragment).commit();
		getSlidingMenu().showContent();
	}

	private int airSetMode = 1;
	boolean a = false;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (curFragment instanceof ExpandActivity
				&& event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			ExpandActivity x = (ExpandActivity) curFragment;
			if (x.webview.canGoBack()) {
				x.webview.goBack();
				x.action.setTopLeftIcon(R.drawable.ic_top_left);
			} else {
				handleBack();
			}
			return true;
		} else {
			return false;
		}

	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub

		if (a) {
			a = false;
			return true;
		}
		if (curFragment instanceof ExpandActivity) {

			a = true;
			return this.onKeyDown(event.getKeyCode(), event);
		}
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && curFragment != null
				&& curFragment instanceof AllRankActivity) {
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.main_frame, new SimpleHomeFragment())
					.commit();
			a = true;
			return super.dispatchKeyEvent(event);
		}
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_UP) {
			handleBack();
			return true;
		}
		return super.dispatchKeyEvent(event);

	}

	void handleBack() {
		if (!isMenuShow()) {
			this.toggle();
			long secondtime = System.currentTimeMillis();
			if (secondtime - firstime > 2000) {
				firstime = System.currentTimeMillis();
				Toast.makeText(this, R.string.again_exit, Toast.LENGTH_SHORT)
						.show();

			} else {

				if (airSetMode == 0) {
					try {
						Intent i = new Intent(this, BluetoothLeService.class);
						stopService(i);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					// back back 不会走主fragment的Onpause方法？？
					Intent intent = new Intent(
							BluetoothLeService.ACTION_AIR_CONTROL);
					intent.putExtra("task", 5);
					intent.putExtra("args", 1);
					sendBroadcast(intent);
				}
				android.os.Process.killProcess(android.os.Process.myPid());
			}
		} else {
			long secondtime = System.currentTimeMillis();
			if (secondtime - firstime > 2000) {
				this.toggle();
			} else {

				if (airSetMode == 0) {
					try {
						Intent i = new Intent(this, BluetoothLeService.class);
						stopService(i);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					Intent intent = new Intent(
							BluetoothLeService.ACTION_AIR_CONTROL);
					intent.putExtra("task", 5);
					intent.putExtra("args", 1);
					sendBroadcast(intent);
				}
				android.os.Process.killProcess(android.os.Process.myPid());
			}
		}
	}

	// /
	Camera myCamera = null;

	@SuppressLint("NewApi")
	public void takePictureNoPreview(Context context) {
		// open back facing camera by default
		try {
			if (Build.VERSION.SDK_INT >= 9) {
				myCamera = Camera.open(CameraInfo.CAMERA_FACING_BACK);
			} else {
				myCamera = Camera.open();
			}

			if (myCamera != null) {
				try {
					// set camera parameters if you want to
					// ...

					// here, the unused surface view and holder
					SurfaceView dummy = new SurfaceView(context);

					myCamera.setPreviewDisplay(dummy.getHolder());
					/* 构建Camera.Parameters对相机的参数进行设置 */
					// Camera.Parameters parameters = myCamera.getParameters();
					// /* 设置拍照的图片格式 */
					//
					// /* 设置Preview(预览)的尺寸 */
					// int jjj = parameters.getJpegQuality();
					// parameters.setJpegQuality(50);
					// Size ss = parameters.getPictureSize();
					// // parameters.setPictureSize(640, 480);
					// List<Size> jj = parameters.getSupportedPictureSizes();
					// ss = jj.get(0);
					// parameters.setPictureSize(ss.width, ss.height);
					// // parameters.setPreviewSize(dummy.);
					// parameters.setPictureFormat(ImageFormat.JPEG);
					// parameters.setRotation(90);
					// myCamera.setParameters(parameters);
					// myCamera.autoFocus(autoFocusCallback);

					myCamera.startPreview();
					myCamera.autoFocus(autoFocusCallback);
					// myCamera.takePicture(null, null, getJpegCallback());

				} finally {
					// myCamera.
					// myCamera.release();
					// myCamera.close();
				}

			} else {
				// booo, failed!
			}
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.v("aa", e.toString());
			if (myCamera != null) {
				myCamera.setPreviewCallback(null);
				myCamera.stopPreview();
				// myCamera.cancelAutoFocus();
				myCamera.release();
			}
		}

	}

	Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {

		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			// TODO Auto-generated method stub
			myCamera.takePicture(null, null, getJpegCallback());
		}
	};

	private PictureCallback getJpegCallback() {
		PictureCallback jpeg = new PictureCallback() {
			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				FileOutputStream fos;
				try {
					// String jj=
					// Environment.getExternalStorageDirectory().getPath() +
					// File.separator +
					// "log11/a1_"+Calendar.getInstance().getTime().toString()+
					// ".jpeg";
					camera.startPreview();
					String strPath = Environment
							.getExternalStoragePublicDirectory(
									Environment.DIRECTORY_DCIM).toString()
							+ File.separator + "Lovefit_AIR";
					File ff = new File(strPath);
					if (!ff.exists()) {
						ff.mkdir();
					}
					String strFile = strPath + File.separator
							+ Calendar.getInstance().getTime().toString()
							+ ".jpeg";
					;
					fos = new FileOutputStream(strFile);
					fos.write(data);
					fos.close();

					Toast.makeText(
							getBaseContext(),
							getResources().getString(
									R.string.air_yaoyao_picture_msg1),
							Toast.LENGTH_SHORT).show();
					inTakeing = false;
				} catch (IOException e) {
					// do something about it
				} finally {
					if (myCamera != null) {
						myCamera.setPreviewCallback(null);
						myCamera.stopPreview();
						// myCamera.cancelAutoFocus();
						myCamera.release();
					}
					myCamera = null;
				}
			}
		};
		return jpeg;
	}

	AlertDialog tokenAlertDialog;

	private void showTokenInvalid() {
		if (tokenAlertDialog != null && tokenAlertDialog.isShowing()) {
			return;
		}
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle(R.string.warning);
		ab.setMessage(R.string.qqtokenerror);
		ab.setPositiveButton(R.string.toauth,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						NotificationManager mNotificationManager = (NotificationManager) SlideMainActivity.this
								.getSystemService(Context.NOTIFICATION_SERVICE);
						mNotificationManager.cancel(10010);
						Intent intent1 = new Intent(SlideMainActivity.this,
								SyncQQHealth.class);
						intent1.putExtra("auth", 1);
						startActivity(intent1);
					}
				});
		ab.setNegativeButton(R.string.cancel, null);
		tokenAlertDialog = ab.create();
		tokenAlertDialog.show();
	}

	boolean inTakeing = false;
	public static HashMap<String, Object> setting;
	private BroadcastReceiver airReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			// TODO Auto-generated method stub
			final String action = intent.getAction();
			if (action.equals(BluetoothLeService.ACTION_AIR_YAOYAO)) {
				Toast.makeText(getBaseContext(),
						getResources().getString(R.string.air_yaoyao_msg1),
						Toast.LENGTH_SHORT).show();
			}
			if (action.equals(SyncQQHealth.ACTION_QQHEALTH)) {
				if (intent.getIntExtra("type", 0) == 1) {
					NotificationCompat.Builder nb;
					Intent intent1 = new Intent(arg0, SyncQQHealth.class);
					intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
							| Intent.FLAG_ACTIVITY_NEW_TASK);
					intent1.putExtra("step", intent.getIntExtra("step", 0));
					intent1.putExtra("distance",
							intent.getIntExtra("distance", 0));
					intent1.putExtra("duration",
							intent.getIntExtra("duration", 0));
					intent1.putExtra("time", intent.getLongExtra("time", 0));
					intent1.putExtra("calories",
							intent.getIntExtra("calories", 0));
					PendingIntent contentIntent = PendingIntent
							.getActivity(arg0, 0, intent1,
									PendingIntent.FLAG_UPDATE_CURRENT);
					nb = new NotificationCompat.Builder(arg0)

					.setSmallIcon(R.drawable.ic_launcher)
							.setContentTitle(getString(R.string.warning))
							.setContentText("QQ认证已经过期，点击此处重新授权")
							.setWhen(System.currentTimeMillis())
							.setContentIntent(contentIntent)
							.setAutoCancel(true);
					NotificationManager mNotificationManager = (NotificationManager) arg0
							.getSystemService(Context.NOTIFICATION_SERVICE);
					mNotificationManager.notify(10010, nb.build());
					showTokenInvalid();
				}
				if (intent.getBooleanExtra("fromQQ", false)) {
					AlertDialog.Builder ab = new AlertDialog.Builder(
							SlideMainActivity.this);
					//ab.setTitle("提示");
					ab.setMessage("Yes! 数据同步成功！");
					ab.setPositiveButton("留在Lovefit", null);
					ab.setNegativeButton("返回QQ健康",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									String uri = "http://jiankang.qq.com/?_wv=2163715&_bid=233";
									if (SyncQQHealth.isPackageExists(
											SlideMainActivity.this,
											"com.tencent.mobileqq")) {
										uri = "mqqapi://forward/url?src_type=web&version=1&url_prefix="
												+ Base64Coder.encodeString(uri);
									}
									Intent intent = new Intent(
											Intent.ACTION_VIEW, Uri.parse(uri));
									startActivity(intent);
								}
							});
					ab.create().show();
				}
			}

			if (action.equals(BluetoothLeService.ACTION_CAMERA_PICTURE)) {
				if (!inTakeing) {
					takePictureNoPreview(SlideMainActivity.this);
					inTakeing = true;
				}
			}

			if (action.equals(BluetoothLeService.ACTION_PREF_CHANGED)) {
				airSetMode = airPreference.getInt("airmode", 1);
			}
			if (action
					.equals(BluetoothLeService.ACTION_BLE_BIND_DEVICE_CHANGED)) {

				refreshMenu(intent.getBooleanExtra("isKs", false));
			}
			if (action.equals(BluetoothLeService.ACTION_AIR_RSSI_STATUS)) {
				device_status = intent.getIntExtra("status", 0);
				device_rssi_c = intent.getIntExtra("rssi_c", 0);
				device_rssi_a = intent.getIntExtra("rssi_a", 0);
				device_battery = intent.getIntExtra("device_battery", 0);

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
					try {

						if (ba[0] == 1) {
							alarm1 = true;
						} else {
							alarm1 = false;
						}
						alarm1Value = (turnNum(ba[1]) + ":" + turnNum(ba[2]));

						if (ba[3] == 1) {
							alarm2 = true;
						} else {
							alarm2 = false;
						}
						alarm2Value = turnNum(ba[4]) + ":" + turnNum(ba[5]);

					} catch (Exception e) {
						e.printStackTrace();
					}

					break;
				// name
				case 20:
					final String name = intent.getStringExtra("value");

					break;
				// soft_version
				case 22:
					final String ver = intent.getStringExtra("value");
					currentVersion = ver;
					setting = (HashMap<String, Object>) intent
							.getSerializableExtra("setting");
					airPreference.edit()
							.putString("soft_version", currentVersion).commit();
					break;

				default:
					break;
				}

			}
		}

	};
	public static int device_status, device_rssi_c, device_rssi_a,
			device_battery;
	public static boolean alarm1, alarm2;
	public static String alarm1Value, alarm2Value, currentVersion;

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

	int guide_flag = 0;
	Fragment curFragment;

	@Override
	public void onAttachFragment(Fragment fragment) {
		// TODO Auto-generated method stub
		this.curFragment = fragment;
		a = false;
		if (fragment instanceof MenuFragment) {
			monModeClick = (onModeClick) fragment;
		}
		super.onAttachFragment(fragment);
	}

	CallFragment call;

	void showGuide() {
		call = getCallBack();
		final View guidView = LayoutInflater.from(this).inflate(
				R.layout.guide_simplehome, null);
		final PopupWindow pop = new PopupWindow(guidView,
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		final ImageView ivLeft = (ImageView) guidView.findViewById(R.id.left);
		final ImageView ivDown = (ImageView) guidView.findViewById(R.id.down);
		final ImageView ivRight = (ImageView) guidView.findViewById(R.id.right);
		final TextView tvRight = (TextView) guidView
				.findViewById(R.id.right_text);
		final TextView itemtv = (TextView) guidView.findViewById(R.id.itemtv);
		final ImageView ivClick = (ImageView) guidView.findViewById(R.id.click);
		final TextView tvClick = (TextView) guidView
				.findViewById(R.id.click_text);
		final TextView tvleft = (TextView) guidView
				.findViewById(R.id.left_text);
		final TextView tvdown = (TextView) guidView
				.findViewById(R.id.down_text);
		final LinearLayout last = (LinearLayout) guidView
				.findViewById(R.id.last);
		final Button select = (Button) guidView.findViewById(R.id.select);
		final Button cancel = (Button) guidView.findViewById(R.id.cancel);
		ivDown.setVisibility(View.VISIBLE);
		tvdown.setVisibility(View.VISIBLE);
		ivDown.startAnimation(AnimationUtils.loadAnimation(this,
				R.anim.next_anim));
		guidView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				guide_flag++;
				switch (guide_flag) {
				case 1:
					// 隐藏第一步
					ivDown.clearAnimation();
					ivDown.setVisibility(View.INVISIBLE);
					tvdown.setVisibility(View.INVISIBLE);
					// 开始第二步
					ivRight.setVisibility(View.VISIBLE);
					tvRight.setVisibility(View.VISIBLE);
					ivRight.startAnimation(AnimationUtils.loadAnimation(
							SlideMainActivity.this, R.anim.swipe_to_left));
					break;
				case 2:
					// 隐藏第二步
					ivRight.clearAnimation();
					tvRight.setVisibility(View.INVISIBLE);
					ivRight.setVisibility(View.INVISIBLE);
					// 开始第三步
					ivLeft.setVisibility(View.VISIBLE);
					tvleft.setVisibility(View.VISIBLE);
					ivLeft.startAnimation(AnimationUtils.loadAnimation(
							SlideMainActivity.this, R.anim.swipe_to_right));
					break;
				case 3:
					// 隐藏第三步
					ivLeft.clearAnimation();
					tvleft.setVisibility(View.INVISIBLE);
					ivLeft.setVisibility(View.INVISIBLE);
					// 开始第四步
					call.makeOrder(101);
					itemtv.setVisibility(View.VISIBLE);
					break;
				case 4:
					// 隐藏第三步
					itemtv.setVisibility(View.INVISIBLE);
					call.makeOrder(102);
					// 开始第四步
					ivClick.setVisibility(View.VISIBLE);
					tvClick.setVisibility(View.VISIBLE);
					ivClick.startAnimation(AnimationUtils.loadAnimation(
							SlideMainActivity.this, R.anim.swipe_click));
					break;
				case 5:
					pop.dismiss();
					// guidView.setClickable(false);
					// //切换为体重
					// call.makeOrder(100);
					// //隐藏第四步
					// ivClick.clearAnimation();
					// ivClick.setVisibility(View.INVISIBLE);
					// tvClick.setVisibility(View.INVISIBLE);
					//
					// new Handler().postDelayed(new Runnable() {
					//
					// @Override
					// public void run() {
					// // TODO Auto-generated method stub
					// //点击图标查看体重详细
					// guidView.setClickable(true);
					// call.makeOrder(104);
					// tvClick.setText("点击此图标，查看体重详细数据。");
					// tvClick.setVisibility(View.VISIBLE);
					// }
					// }, 800);

					break;
				case 6:
					call.makeOrder(105);
					tvClick.setVisibility(View.INVISIBLE);
					last.setVisibility(View.VISIBLE);
					select.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub

							if (preferences.getInt("ISMEMBER", 0) == 0) {
								NoRegisterDialog dd = new NoRegisterDialog(
										SlideMainActivity.this,
										R.string.no_register,
										R.string.no_register_content);
								dd.show();
							} else {
								pop.dismiss();
								startActivity(new Intent(
										SlideMainActivity.this,
										SportsDevice.class));

							}
						}
					});
					cancel.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							pop.dismiss();
						}
					});
					break;
				case 7:
					// pop.dismiss();
					break;
				}
			}
		});
		pop.showAtLocation(mView, Gravity.CENTER, 0, 0);
	}

	boolean guide_first = true;

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		guide_first = preferences.getBoolean("isfirstloads", true);
		if (guide_first) {
			guide_first = false;
			preferences.edit().putBoolean("isfirstloads", false).commit();
			// showGuide();
		}
		super.onWindowFocusChanged(hasFocus);
	}

	@Override
	public void OnStepChanged(int steps, int dis, int cal, String action) {
		// TODO Auto-generated method stub
		CallFragment call = getCallBack();
		if (call != null) {
			call.callFragment(steps, dis, cal, action);
		}
	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub

	}

	onModeClick monModeClick;

}

interface onModeClick {
	void onModeClick(int mode);
}

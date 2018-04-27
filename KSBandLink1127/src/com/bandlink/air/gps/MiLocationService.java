package com.bandlink.air.gps;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.view.WindowManager;

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.bandlink.air.GpsSportActivity;
import com.bandlink.air.R;
import com.bandlink.air.ble.LogUtil;
import com.bandlink.air.gps.MyLocationListerner.changeListener;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.NotificationUtils;
import com.bandlink.air.util.SharePreUtils;

public class MiLocationService extends Service implements changeListener {
	/**
	 * 
	 * 0 起始点 1 画路径 2 终点 -1 精度不够
	 * **/
	int isFristLoc = 0;// 是否首次定位
	private MyBinder myBinder = new MyBinder();
	public static int LocNotification = 111;
	private NotificationManager mNM;
	private Dbutils db;
	private String user;
	private int uid;
	private MyLocationListerner myListener = null;
	private LocationClient locClient;
	private static SimpleDateFormat sf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	public int m_runId;
	public int points;
	private static String TAG = "LocationService";
	private LatLng lastPoint = null;
	private UpdateUIListener updateListener;

	public interface UpdateUIListener {
		public void onchangeUI(int index, BDLocation locData);
	}

	public void setListener(UpdateUIListener listener) {
		updateListener = listener;
	}

	public class MyBinder extends Binder {
		public MiLocationService getService() {
			return MiLocationService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub]
		handleCommand(intent);
		return myBinder;
	}

	private void getUserProfile() {
		SharedPreferences share = getSharedPreferences(
				SharePreUtils.APP_ACTION, Context.MODE_PRIVATE);
		bLogfile = share.getBoolean("isdebug", false);
		bLogfile = true;
		user = share.getString("USERNAME", "lovefit");
		uid = share.getInt("UID", -1);

		db = new Dbutils(uid, this);

	}

	public void onCreate() {

		if (bLogfile) {
			LogUtil.setDefaultFilePath(getBaseContext());
			LogUtil.setLogFilePath("log12gps/");
			LogUtil.setSyns(true);
			// LogUtil.startNewLog();
			LogUtil.e(Calendar.getInstance().getTime().toLocaleString()
					+ "LovefitAir.oncreate");
		}

		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		getUserProfile();
		String time = getCurTime();
		int timeint = getUnixTime(time);
		GPSEntity g = new GPSEntity();
		g.time = time;
		g.steps = 0;
		g.calorie = 0;
		g.distance = 0;
		g.durance = 0;
		g.points = 0;
		g.setId(timeint);// 以时间戳作为tracker的id
		m_runId = timeint;
		points = 0;
		db = new Dbutils(this);
		db.saveGpsTrack(g);
		lastPoint = new LatLng(0, 0);

		myListener = new MyLocationListerner(this, false);// 是否使用模拟
		// 将该server的优先级设置为Activity一个级别
		// setForeground(true);
		// startForeground(id, notification);
		locClient = new LocationClient(this);
		locClient.registerLocationListener(myListener);
		setLocationOption();
		locClient.start();

	}

	// 设置相关参数
	private void setLocationOption() {
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		// option.setScanSpan(5000);// 5秒去请求一次
		option.setScanSpan(1000);// 1秒去请求一次
		// option.setScanSpan(10000);// 5秒去请求一次
		locClient.setLocOption(option);
	}

	private void setLocationOptionSpeed(int interval) {
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		// option.setScanSpan(5000);// 5秒去请求一次
		option.setScanSpan(interval);// 5秒去请求一次
		locClient.setLocOption(option);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		handleCommand(intent);
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	public static String getCurTime() {
		Calendar c = Calendar.getInstance();
		String str = sf.format(c.getTime());
		return str;
	}

	public static int getUnixTime(String time) {
		int timeint = 0;
		try {
			Long a = sf.parse(time).getTime() / 1000;
			timeint = a.intValue();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return timeint;
	}

	private int numMessages;

	void handleCommand(Intent intent) {

		Intent intent1 = new Intent(this, GpsSportActivity.class);
		// intent1.setAction(Intent.ACTION_MAIN);
		// intent1.addCategory(Intent.CATEGORY_LAUNCHER);
		intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				intent1, PendingIntent.FLAG_UPDATE_CURRENT);
		numMessages = 0;
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(getString(R.string.gps_service_run))
				.setContentText(getString(R.string.gps_service_run))
				.setWhen(System.currentTimeMillis())
				.setContentIntent(contentIntent).setAutoCancel(true);
		startForeground(NotificationUtils.GPSOrStepNotfication,
				builder.getNotification());

	}

	@Override
	public void onStart(Intent intent, int startId) {
		handleCommand(intent);
	}

	Date lastDate = null;
	Date nowDate = null;
	private double distance = 0;
	private double deltaDistance = 0;
	int iAccuracyBigThres = 1000;
	int iAccuracyThres = 32;
	// int iAccuracyBigThres = 3000;
	// int iAccuracyThres = 2000;
	private int validCheckCounter = 0;

	static boolean bLogfile = true;

	@Override
	public void onchange(BDLocation locData) {
		// TODO Auto-generated method stub
		try {
			if (bLogfile) {
				LogUtil.i(Calendar.getInstance().getTime().toLocaleString()
						+ ":   " + locData.getRadius() + "/ "
						+ locData.getSpeed() + "/" + locData.getLongitude()
						+ "/" + locData.getLatitude());
			}
			LogUtil.e(
					TAG,
					"gpsnow: " + locData.getRadius() + "/ "
							+ locData.getSpeed());
			LogUtil.e(TAG, "get a geopoint...");
			if (locData.getLatitude() > 0 && locData.getLongitude() > 0) {
				try {
					// cc.latitude += 0.001;
					// cc.longitude+= 0.00001;
					// locData = cc;
					LatLng _gp = null;
					Date d2 = new Date();
					int deltaSeconds = 0;
					float ss = (float) 0.0;
					// MyLog.v(TAG, "gpsnow: " + locData.accuracy + "/ " +
					// locData.speed);
					// MyLog.e(TAG, "get a geopoint...");
					if (locData.getRadius() <= iAccuracyBigThres) {
						switch (isFristLoc) {
						case 0:
							isFristLoc++;
							validCheckCounter = 0;
							updateListener.onchangeUI(-1, locData);
							System.out.println("gps waiting");
							break;
						case 1:
							if (locData.getRadius() <= iAccuracyThres) {
								_gp = new LatLng(locData.getLatitude(),
										locData.getLongitude());
								System.out.println("gps waiting"
										+ validCheckCounter);
								if (validCheckCounter < 4) {
									validCheckCounter++;
								} else {
									isFristLoc = 2;
									updateListener.onchangeUI(0, locData);
									insertStartPoint(_gp);
									lastPoint = _gp;
									lastDate = d2;
								}

							} else {
								// 仅用作更新gps强度
								updateListener.onchangeUI(-1, locData);
							}

							break;
						case 2:
							if (locData.getRadius() > iAccuracyThres) {
								validCheckCounter = 0;
								System.out.println("gps wake"
										+ validCheckCounter);
								updateListener.onchangeUI(-1, locData);
								break;
							}
							// 突然间信号丢失 恢复后的第一个点往往存在偏差
							if (++validCheckCounter < 2) {
								updateListener.onchangeUI(-1, locData);
								break;
							}
							System.out
									.println("gps strong" + validCheckCounter);
							_gp = new LatLng(locData.getLatitude(),
									locData.getLongitude());
							// deltaDistance = GetDistance(lastPoint, _gp);
							deltaDistance = DistanceUtil.getDistance(lastPoint,
									_gp);
							// d2 = new Date();
							deltaSeconds = (int) ((d2.getTime() - lastDate
									.getTime()) / 1000);

							if (deltaSeconds > 0) {
								// 每小时多少千米
								ss = (float) (deltaDistance / deltaSeconds * 18 / 5f);

								if (bLogfile) {
									LogUtil.i(Calendar.getInstance().getTime()
											.toLocaleString()
											+ ":   "
											+ deltaDistance
											+ "/"
											+ deltaSeconds + "/" + ss);
								}
								// 人体极限
								if (ss >= 0 && ss < 30) {
									insertPoint(_gp, ss, locData.getRadius());
									locData.setSpeed(ss);
									setLocationOptionSpeed(2000);
									// setLocationOptionSpeed(20000);

									updateListener.onchangeUI(1, locData);
									lastPoint = _gp;
									lastDate = d2;
								} else {
									// 仅作gps信号强度更新
									updateListener.onchangeUI(-3, locData);
								}
							}
							break;
						default:

							updateListener.onchangeUI(-3, locData);
							break;

						}
					} else {
						// 信号无
						updateListener.onchangeUI(-3, locData);
					}

				} catch (Exception e) {
					LogUtil.e(TAG, "onchange exception...");
					e.printStackTrace();
				}

			}

		} catch (Exception e) {
			// TODO: handle exception
			StringBuffer sb = new StringBuffer();
			for (StackTraceElement x : e.getStackTrace()) {
				sb.append("===" + x.getClassName() + x.getLineNumber() + "==="
						+ x.getMethodName() + "===" + x.getFileName());
				LogUtil.e(TAG, "===" + x.getClassName() + x.getLineNumber()
						+ "===" + x.getMethodName() + "===" + x.getFileName());
			}
			LogUtil.e(TAG, e.getMessage());
			AlertDialog.Builder ab = new AlertDialog.Builder(
					MiLocationService.this);
			ab.setTitle("ERROR");
			ab.setMessage(sb);
			AlertDialog dialog = ab.create();
			dialog.getWindow().setType(
					(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
			dialog.show();

		}
	}

	private double ConvertDegreeToRadians(double degrees) {
		return (Math.PI / 180) * degrees;
	}

	// 往数据库中插入第一个点，表示运动开始
	public void insertStartPoint(LatLng _gp) {
		String time = getCurTime();
		GPSPointEntity info = new GPSPointEntity();
		info.setIdd(m_runId);
		info.setLatitude(_gp.latitude);
		info.setLongitude((double) _gp.longitude);
		info.setDateTime(time);
		db.saveGpsPoints(info);
		points++;
	}

	public void insertPoint(LatLng _gp, double speed, double accuracy) {
		GPSPointEntity info = new GPSPointEntity();
		info.setIdd(m_runId);
		info.setLatitude(_gp.latitude);
		info.setLongitude((double) _gp.longitude);
		info.setSpeed(speed);
		info.setAccuracy(accuracy);
		String time = getCurTime();
		info.setDateTime(time);
		db.saveGpsPoints(info);
		points++;

	}

	public void onDestroy() {
		isFristLoc = 0;
		locClient.stop();
		stopForegroundCompat(LocNotification);
	}

	void stopForegroundCompat(int id) {
		stopForeground(true);
	}

}

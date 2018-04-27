package com.milink.android.lovewalk.bluetooth.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build.VERSION_CODES;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.bandlink.air.MyLog;
import com.bandlink.air.R;
import com.bandlink.air.SlideMainActivity;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.NotificationUtils;
import com.bandlink.air.util.SessionUpdate;

public class StepService extends Service {

	private Sensor mSensor;
	private SensorManager mSensorManager;
	private StepDetector mStepDetector;

	private Dbutils db;

	public static int step;
	public static int cal;
	public static double weight;
	public static int step_length;

	private int cur_step;
	private int cur_cal;
	private int cru_dis;
	public static String username;
	public static boolean isRun;
	SharedPreferences share;
	private int ss, cc;
	private Typeface tf;
	// upload data
	// private UploadMyData upThread;

	private BroadcastReceiver milinkReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			int command = intent.getIntExtra("command", 0);
			if (command == 2) {
				// stop
				SessionUpdate.isUpload = false;
				unRegister();
			} else if (command == 3) {
				MyLog.e("stepservicecommad3", isRun + "");
				if (!isRun) {
					step = intent.getIntExtra("step", 0);
					cal = intent.getIntExtra("calorie", 0);
					weight = intent.getDoubleExtra("weight", 60.0);
					step_length = intent.getIntExtra("step_length", 60);
					String temp = "0.0";
					try {
						temp = db.getUserProfile()[5].toString();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					step_length = Math.round(0.4f * Float.valueOf(temp));
					step_length = step_length == 0 ? 60 : step_length;
					username = intent.getStringExtra("username");
					if (mStepDetector != null) {
						mStepDetector.setConfig(step, cal, weight, step_length,
								username);
					}

					registerDetector();
					MyLog.e("stepregisterdetector", "nowstep:0");
					Intent intent1 = new Intent("MilinkStepInit");
					intent1.putExtra("nowstep", 0);
					intent1.putExtra("nowcal", 0);
					sendBroadcast(intent1);
				} else {
					Intent intent1 = new Intent("MilinkStepInit");
					intent1.putExtra("nowstep", StepDetector.getNowStep());
					intent1.putExtra("nowcal", StepDetector.getNowCal());
					sendBroadcast(intent1);
				}
			}
		}

	};

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		tf = Typeface
				.createFromAsset(getAssets(), "font/AvenirLTStd-Light.otf");
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		share = getSharedPreferences("air", Context.MODE_MULTI_PROCESS);
		db = new Dbutils(share.getInt("UID", -1), this);
		mStepDetector = new StepDetector(this, db);

		// upThread=new UploadMyData(dbhHelper);
		// upThread.start();
		MyLog.v("stepservice oncreate", "开始");
		IntentFilter filter_dynamic = new IntentFilter();
		filter_dynamic.addAction("MilinkConfig");
		filter_dynamic.addAction("STEPRESET");
		checkTime();
		registerReceiver(milinkReceiver, filter_dynamic);
	}

	private void checkTime() {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		Intent intent = new Intent("StepReset");
		MyLog.v("开始时间",
				new SimpleDateFormat("yyyy-MM-dd").format(new Date(c
						.getTimeInMillis() + 24 * 60 * 60 * 1000)));
		setRepeatTasks(c.getTimeInMillis() + 24 * 60 * 60 * 1000,
				24 * 60 * 60 * 1000, intent);
		Intent intent1 = new Intent("StepUpload");
		setRepeatTasks(c.getTimeInMillis() + 60 * 60 * 1000,
				1 * 60 * 60 * 1000, intent1);
	}

	int i = 0;

	private void setRepeatTasks(long triggerTime, long interval, Intent intent) {

		AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		// 系统中如果有这个pendingIntent 取消
		PendingIntent pending = PendingIntent.getBroadcast(this, i, intent,
				PendingIntent.FLAG_NO_CREATE);
		if (pending != null) {
			am.cancel(pending);
		}
		// pending = PendingIntent.getBroadcast(this, 0, intent, 0);
		pending = PendingIntent.getBroadcast(this, i, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		// am.set(AlarmManager.RTC_WAKEUP, triggerTime, pending);
		am.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime, interval, pending);
		i++;

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		db = new Dbutils(this);
		int deviceType = db.getUserDeivceType();
		if (deviceType == 2
				|| (intent != null && intent.getBooleanExtra("test", false))) {
			if (mSensor == null) {
				// 没有这个传感器
				Intent intent1 = new Intent("MilinkStep");
				intent1.putExtra("device", 0);
				sendBroadcast(intent);
			} else {
				if (!isRun) {
					MyLog.v("软计步服务开始", isRun + "");
					// checkTime();
					setStepDetector();
				}

			}

		}
		return START_STICKY;
	}

	public void setStepDetector() {
		// settings = getSharedPreferences("milinkStep", Context.MODE_PRIVATE);
		db = new Dbutils(this);
		ss = 0;
		cc = 0;
		try {
			Calendar dd = Calendar.getInstance();
			// String strdate = settings.getString("steps_savetime",
			// "1980-01-01");
			Object[] object = db.getSoftStep();
			String strdate = (String) object[0];
			MyLog.v("strdate", strdate + "==");
			Date date = new SimpleDateFormat("yyyy-MM-dd").parse(strdate);
			Calendar ddd;
			ddd = Calendar.getInstance();
			ddd.setTime(date);

			// MyLog.v("time1现在",
			// dd.get(Calendar.YEAR)+"=="+(dd.get(Calendar.MONTH)+1)+"=="+dd.get(Calendar.DATE)+dd.getTime().toString());
			// MyLog.v("time2存储",
			// ddd.get(Calendar.YEAR)+"=="+(ddd.get(Calendar.MONTH)+1)+"=="+ddd.get(Calendar.DATE)+ddd.getTime().toString());
			if (dd.get(Calendar.YEAR) == ddd.get(Calendar.YEAR)
					&& (dd.get(Calendar.MONTH) + 1) == (ddd.get(Calendar.MONTH) + 1)
					&& dd.get(Calendar.DATE) == ddd.get(Calendar.DATE)) {
				// ss = settings.getInt("steps_today", 0);
				// cc = settings.getInt("cal_today", 0);
				ss = (Integer) object[1];
				cc = (Integer) object[2];
			}
		} catch (Exception e) {
			ss = 0;
			cc = 0;
			e.printStackTrace();
		}
		step = ss;
		cal = cc;
		step_length = 60;
		try {
			step_length = Math
					.round(0.4f * Float.valueOf(db.getUserProfile()[5]
							.toString()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Object[] profile = db.getUserProfile();
		if (profile != null) {
			weight = (Double) profile[6];
			if (weight < 30) {
				weight = 60;
			}
		}
		if (mStepDetector != null) {
			mStepDetector.setConfig(step, cal, weight, step_length, username);
		}
		registerDetector();

	}

	void handleCommand() {

		Intent intent1 = new Intent(this, SlideMainActivity.class);
		// intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent1.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

		// intent1.setAction(Intent.ACTION_MAIN);
		// intent1.addCategory(Intent.CATEGORY_LAUNCHER);
		// intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
		// | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				intent1, PendingIntent.FLAG_UPDATE_CURRENT);
		Notification notification = new NotificationCompat.Builder(this)
				.setDefaults(Notification.DEFAULT_ALL)
				.setSmallIcon(R.drawable.ic_launcher)
			//	.setContentTitle(getString(R.string.soft_step))
			//	.setContentText(getString(R.string.step) + "：" + step)
				.setContentIntent(contentIntent)
				.setWhen(System.currentTimeMillis()).build();

		db = new Dbutils(this);
		int deviceType = db.getUserDeivceType();
		if (deviceType == 2) {
			startForeground(NotificationUtils.GPSOrStepNotfication,
					notification);
		} else {
			// NotificationManager mNotificationManager = (NotificationManager)
			// getSystemService(Context.NOTIFICATION_SERVICE);
			// mNotificationManager.notify(NotificationUtils.GPSOrStepNotfication,
			// notification);

		}

	}

	void handleCommand2() {
		Intent intent1 = new Intent(this, SlideMainActivity.class);
		intent1.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

		// intent1.setAction(Intent.ACTION_MAIN);
		// intent1.addCategory(Intent.CATEGORY_LAUNCHER);
		// intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
		// | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				intent1, PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationManager mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(NotificationUtils.BlueNotfication);
		RemoteViews rvs = new RemoteViews(this.getPackageName(),
				R.layout.step_notification);

		rvs.setImageViewBitmap(R.id.icon, getCroppedBitmap(0));
		rvs.setTextViewText(R.id.step, step + "");
		rvs.setTextViewText(R.id.kcal, 0 + "");
		rvs.setTextViewText(R.id.dis, 0 + "");
		// Sets an ID for the notification, so it can be updated
		NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_launcher)
				//.setContentTitle(this.getString(R.string.soft_step))
				//.setContentText(this.getString(R.string.step) + "：" + step)
				.setContentIntent(contentIntent)
				.setWhen(System.currentTimeMillis()).setContent(rvs);
		Notification notification = mNotifyBuilder.build();

		db = new Dbutils(this);
		int deviceType = db.getUserDeivceType();
		if (deviceType == 2) {
			startForeground(NotificationUtils.GPSOrStepNotfication,
					notification);
		} else {
			// NotificationManager mNotificationManager = (NotificationManager)
			// getSystemService(Context.NOTIFICATION_SERVICE);
			// mNotificationManager.notify(NotificationUtils.GPSOrStepNotfication,
			// notification);

		}

	}

	public Bitmap getCroppedBitmap(float progress) {

		Matrix m = new Matrix();

		int w = (int) (getResources()
				.getDimensionPixelSize(R.dimen.notification_step));
		int h = w;
		Bitmap output = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG));
		final Paint paint = new Paint();
		paint.setTextSize(getResources().getDimensionPixelSize(
				R.dimen.text_small));
		paint.setStrokeWidth(5);
		paint.setColor(Color.parseColor("#ffffff"));
		paint.setStrokeCap(Cap.ROUND);
		paint.setStyle(Paint.Style.STROKE);
		paint.setAntiAlias(true);
		paint.setTextAlign(Align.CENTER);
		// paint.setFakeBoldText(true);

		paint.setColor(Color.parseColor("#33ffffff"));
		canvas.drawArc(
				new RectF(w * (0.1f), h * (0.1f), w * (0.9f), h * (0.9f)),
				-90f, 360, false, paint);
		paint.setStrokeWidth(8);
		paint.setColor(Color.parseColor("#5F93EF"));
		canvas.drawArc(
				new RectF(w * (0.1f), h * (0.1f), w * (0.9f), h * (0.9f)),
				-90f, 360 * progress, false, paint);
		paint.setStrokeCap(Cap.SQUARE);
		Paint paint2 = new Paint();
		paint2.setTypeface(tf);
		paint2.setColor(Color.parseColor("#ffffff"));
		paint2.setStyle(Paint.Style.FILL);
		paint2.setTextSize(getResources().getDimension(R.dimen.text_middle));
		float f = paint2.measureText(Math.round(progress * 100) + "");
		canvas.drawText(Math.round(progress * 100) + "", (w * 0.5f)
				- (f * 0.5f), h * 0.6f, paint2);
		paint2.setTextSize(18);
		canvas.drawText("%", (w * 0.5f) + (f * 0.5f), h * 0.6f, paint2);
		return Bitmap.createBitmap(output, 0, 0, output.getWidth(),
				output.getHeight(), m, true);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		unregisterReceiver(milinkReceiver);
		unRegister();
		SessionUpdate.isUpload = false;
		isRun = false;
		MyLog.v("destory", isRun + "");
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	private void registerDetector() {
		boolean isok;
		  
        if (android.os.Build.VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD)
			isok = mSensorManager.registerListener(mStepDetector, mSensor,
					50000);
		else {
			isok = mSensorManager.registerListener(mStepDetector, mSensor,
					SensorManager.SENSOR_DELAY_FASTEST);
		}

		if (isok) {
			// 注册成功，启动计步服务成功
			isRun = true;
			MyLog.v("registerdetectro ok", isRun + "");

		} else {
			// 注册失败，启动计步服务失败
			isRun = false;
			MyLog.v("registerdetectro erro", isRun + "");
		}
		handleCommand2();
	}

	private void unRegister() {
		if (mSensorManager != null) {
			mSensorManager.unregisterListener(mStepDetector);
		}
		isRun = false;
		MyLog.v("unregisterdetector", isRun + "");
	}

	public void updateData(int s, int c, int d) {
		cur_step = s;
		cur_cal = c;
		cru_dis = d;
	}

}

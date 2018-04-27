package com.milink.android.lovewalk.bluetooth.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.support.v4.app.NotificationCompat;
import android.text.format.Time;
import android.widget.RemoteViews;

import com.bandlink.air.MyLog;
import com.bandlink.air.R;
import com.bandlink.air.SlideMainActivity;
import com.bandlink.air.bluetooth.protocol.Parser;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.NotificationUtils;

public class StepDetector implements SensorEventListener {

	static {
		System.loadLibrary("transjni");
	}

	public native int[] transJNI(int[] ii);

	public volatile static int step;
	private volatile static int step1;
	private int dis;
	private int step_inc;
	private static int step_length;
	private volatile int data_g = 0;
	private volatile int tickcount = 0;

	private static double weight;
	private static double calorie;
	private double calorie_tmp1;
	private double calorie_tmp2;
	private double calorie_inc;

	private Context context;
	private Time time;
	private static String username;

	private Dbutils db;
	private SharedPreferences settings;
	private Typeface tf;
	// private UploadMyData upThread;
	private StepDisplayer mStepDisplayer5m;
	private Parser parser;

	public StepDetector(Context c, Dbutils d) {
		context = c;
		step = 0;
		calorie = 0.0;
		dis = 0;
		db = d;
		time = new Time();
		mStepDisplayer5m = new StepDisplayer();
		parser = new Parser(null, c);
		tf = Typeface.createFromAsset(c.getAssets(),
				"font/AvenirLTStd-Light.otf");
	}

	public static void setConfig(int ste, double c, double w, int s, String u) {
		step = ste;
		step1 = step;
		calorie = c;
		weight = w;
		username = u;
		step_length = s;
		MyLog.e("stepconfig", step + "");
		MyLog.e("calconfig", c + "");
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		synchronized (this) {
			data_g = 0;
			for (int i = 0; i < 3; i++) {
				data_g += (event.values[i] / 9.8 * 64)
						* (event.values[i] / 9.8 * 64);
			}
			checkTime();
			step_process(data_g);
		}

	}

	int[][] daydata = new int[24][12];

	private void step_process(int data_g) {
		int[] stepvalue = transJNI(new int[] { 0, data_g });

		tickcount++;

		if (stepvalue != null && stepvalue.length == 4) {
			step_inc = stepvalue[0];
			// step = stepvalue[1];
			// calorie_tmp1 = stepvalue[2] * weight / 10000.0;
			// calorie_inc = calorie_tmp1 - calorie_tmp2;
			// calorie_tmp2 = calorie_tmp1;

			if (step_inc > 0) {
				// 发送广播
				// 0.0017 * tmpValue * stepData->stepWeight * tmpValue
				step += step_inc;
				// calorie += step_inc * step_inc * 0.0017 * weight;
				// calorie = calorie + calorie_inc;
				// calorie = calorie_tmp1;

				dis = step * step_length;
				Object[] objs = null;
				if (db != null) {
					// db.SaveHttpStep(getTime(), username, step+"", calorie+"",
					// dis+"", "0");
					// MyLog.e("MILINKSTEP","Save Step Success");
					// db.getDataForUpload();
					objs = db.getUserTarget();
				}
				if (context != null) {
					Intent intent = new Intent("MilinkStep");
					intent.putExtra("device", 1);
					intent.putExtra("step", step);
					intent.putExtra("cal", (int) calorie);
					intent.putExtra("dis", dis / 1);
					intent.putExtra("date", getTime());
					context.sendBroadcast(intent);
					mStepDisplayer5m.onStep(step_inc, (int) (calorie * 100),
							dis);

					Dbutils db = new Dbutils(context);
					int dtype = db.getUserDeivceType();
					if (dtype == 2) {
						db.SaveSpBasicData(step, (int) (dis / 100.0),
								(int) (calorie * 100), 2, getTime(), "");// 软计步得到的是百卡，*100//
																			// 转为卡存入数据库
						// settings = context.getSharedPreferences("milinkStep",
						// Context.MODE_PRIVATE);
						// SharedPreferences.Editor editor = settings.edit();
						String strdate = (new SimpleDateFormat("yyyy-MM-dd"))
								.format(Calendar.getInstance().getTime());
						// editor.putString("steps_savetime", strdate);
						// editor.putInt("steps_today", step);
						// editor.putInt("cal_today", (int) calorie);// 记录软计步
						// editor.commit();

						db.saveSoftStep(strdate, step, (int) calorie);
						Date dd = new Date();
						int hourindex = dd.getHours();
						int mm = dd.getMinutes();
						int inhourindex = mm / 5;
						int ss = mStepDisplayer5m.getSteps();
						if (mm == 0) {
							for (int i = 0; i < 12; i++)
								daydata[hourindex][i] = 0;
						}
						if (mm % 5 == 0) {
							mStepDisplayer5m.setSteps(0, 0);
						}

						daydata[hourindex][inhourindex] = ss;
						parser.onPacket0802Fake(dd, hourindex,
								daydata[hourindex]);
					}
					if (objs != null) {
						try {
							updatenotification(((float) step / Float
									.valueOf(objs[3].toString())));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							updatenotification(-1f);
						}
					} else {
						updatenotification(-1f);
					}

					MyLog.e("broadstep", step + "");
					MyLog.e("broadcal", calorie + "");

					Date dd = new Date();
					int hourindex = dd.getHours();
					int mm = dd.getMinutes();
					int inhourindex = mm / 5;
					int ss = step;
					if (mm == 0) {
						if (hourindex == 0) {
							ss = 0;
						}
						for (int i = 0; i < 12; i++)
							daydata[hourindex][i] = 0;
					}
					if (mm % 5 == 0) {
						ss = 0;
					}

					daydata[hourindex][inhourindex] = ss;
					//
					// SharedPreferences setting = getSharedPreferences(
					// "com.milink.android.lovewalk_preferences",
					// Context.MODE_PRIVATE);
					// int device_mode = setting.getInt("DeviceType", 1);
					// if (device_mode == DeviceWrapper.DEVICE_TYPE_SOFT) {
					// parser.onPacket0802Fake(dd, hourindex,
					// daydata[hourindex]);
					// }

				}
			}

			if (tickcount >= 40) {
				tickcount = 0;
				// MyLog.e("MILINKSTEP",String.format("step:%1$s;step1:%2$s",
				// step,step1));
				step1 = step - step1;
				calorie += step1 * step1 * 0.0017 * weight;
				step1 = step;
			}
		}
		return;
	}

	void updatenotification(float per) {
		Intent intent1 = new Intent(context, SlideMainActivity.class);
		intent1.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

		// intent1.setAction(Intent.ACTION_MAIN);
		// intent1.addCategory(Intent.CATEGORY_LAUNCHER);
		// intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
		// | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				intent1, PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		RemoteViews rvs = new RemoteViews(context.getPackageName(),
				R.layout.step_notification);

		if (per != -1) {
			rvs.setImageViewBitmap(R.id.icon, getCroppedBitmap(per));
		} else {
			rvs.setImageViewResource(R.id.icon, R.drawable.ic_launcher);
		}
		mNotificationManager.cancel(NotificationUtils.BlueNotfication);
		rvs.setTextViewText(R.id.step, step + "");
		rvs.setTextViewText(R.id.kcal, String.format("%.1f", (calorie / 10f)));
		rvs.setTextViewText(R.id.dis, String.format("%.1f", dis / 100000f));
		// Sets an ID for the notification, so it can be updated
		NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(
				context).setSmallIcon(R.drawable.ic_launcher)
				//.setContentTitle(context.getString(R.string.soft_step))
				//.setContentText(context.getString(R.string.step) + "：" + step)
				.setContentIntent(contentIntent)
				.setWhen(System.currentTimeMillis()).setContent(rvs);
		mNotificationManager.notify(NotificationUtils.GPSOrStepNotfication,
				mNotifyBuilder.build());

	}

	public Bitmap getCroppedBitmap(float progress) {

		Matrix m = new Matrix();

		int w = (int) (context.getResources()
				.getDimensionPixelSize(R.dimen.notification_step));
		int h = w;
		Bitmap output = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG));
		final Paint paint = new Paint();
		paint.setTextSize(context.getResources().getDimensionPixelSize(
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
		paint.setColor(Color.parseColor("#ffffff"));
		canvas.drawArc(
				new RectF(w * (0.1f), h * (0.1f), w * (0.9f), h * (0.9f)),
				-90f, 360 * progress, false, paint);
		paint.setStrokeCap(Cap.SQUARE);
		Paint paint2 = new Paint();
		paint2.setTypeface(tf);
		paint2.setColor(Color.parseColor("#ffffff"));
		paint2.setStyle(Paint.Style.FILL);
		paint2.setTextSize(context.getResources().getDimension(
				R.dimen.text_middle));
		float f = paint2.measureText(Math.round(progress * 100) + "");
		canvas.drawText(Math.round(progress * 100) + "", (w * 0.5f)
				- (f * 0.5f), h * 0.6f, paint2);
		paint2.setTextSize(18);
		canvas.drawText("%", (w * 0.5f) + (f * 0.5f), h * 0.6f, paint2);
		return Bitmap.createBitmap(output, 0, 0, output.getWidth(),
				output.getHeight(), m, true);
	}

	private void checkTime() {
		// 检查是否是 0时0分0秒
		// time.setToNow();
		// if (time.hour == 0 && time.minute == 0 && time.second == 0) {
		// step = 0;
		// dis = 0;
		// calorie = 0;
		// // if(upThread==null)
		// // {
		// // upThread = new UploadMyData(db);
		// // upThread.start();
		// // }
		//
		// } else if (time.hour == 0 && time.minute == 0) {
		// // upThread = null;
		// }
	}

	public static int getNowStep() {
		MyLog.e("getnowStep", step + "");
		return step;
	}

	public static int getNowCal() {
		return (int) calorie;

	}

	private String getTime() {
		time.setToNow();
		String str_time = String.format("%1$04d-%2$02d-%3$02d", time.year,
				time.month + 1, time.monthDay);
		return str_time;
	}

}

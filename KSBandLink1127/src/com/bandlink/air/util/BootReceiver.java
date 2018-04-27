package com.bandlink.air.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.support.v4.app.NotificationCompat;
import android.view.Gravity;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.bandlink.air.MilinkApplication;
import com.bandlink.air.MyLog;
import com.bandlink.air.R;
import com.bandlink.air.SlideMainActivity;
import com.bandlink.air.ble.BluetoothLeService;
import com.bandlink.air.ble.LogUtil;
import com.bandlink.air.bluetooth.protocol.Parser;
import com.milink.android.lovewalk.bluetooth.service.StepService;

public class BootReceiver extends BroadcastReceiver {
	private SharedPreferences preferences;
	private Typeface tf;
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (intent.getAction().equals("milinkStartService")) {// 根据设备类型打开关闭相应service
			LogUtil.e("---Boot收到广播---"+new SimpleDateFormat("yyyy-MM-dd-HH: mm: ss-SSS").format(new Date()));
			StartMilinkService(context,true);

		} else if (intent.getAction().equals("MilinkStep")) {// 开启软计步缺少传感器
			int device = intent.getIntExtra("device", 1);
			if (device == 0) {
				Toast ta = Toast.makeText(context, R.string.soft_step_nosensor,
						Toast.LENGTH_SHORT);
				ta.setGravity(Gravity.CENTER, 0, 0);
				ta.show();
			}
		} else if (intent.getAction().equals("ACTION_UPLOADGPS")) {// 上传gps轨迹成功

			int code = intent.getExtras().getInt("code");
			int id = intent.getExtras().getInt("id");
			if (id > 0 && code == 0) {
				Dbutils db = new Dbutils(context);
				db.UpdateGPSUpLoad(id, 1);
				 
			}

		} else if (intent.getAction().equals(
				"android.intent.action.BOOT_COMPLETED")) { 
			SharedPreferences airPre = context
					.getSharedPreferences(
							"air",
							Context.MODE_MULTI_PROCESS);
			StartMilinkService(context,airPre.getInt("airmode", 1)!=0);
		} else if (intent.getAction().equals("SportReport")) {
			preferences = context.getSharedPreferences(
					SharePreUtils.APP_ACTION,
					Context.MODE_PRIVATE);
			if (preferences.getInt("UID", -2) != -2) {
				showReport(context);
			}

		}else if(intent.getAction().equals(MilinkApplication.ACTION_ZERO)){
			int tid =  new Dbutils(context).getUserDeivceType();
			if(tid==2){
				
				tf = Typeface.createFromAsset(context.getResources().getAssets(), "font/AvenirLTStd-Light.otf");
				updatenotification(0,context);
			}else if(tid ==5){
				LogUtil.e("---------------------->onboot");
				Parser.updatenotification(context, 0, 0, 0, 0, BluetoothLeService.device_connected);
			}
		}
	}
	void updatenotification(float per,Context context) {
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
		RemoteViews rvs = new RemoteViews(context.getPackageName(), R.layout.step_notification);
		 
		if(per!=-1){
			rvs.setImageViewBitmap(R.id.icon, getCroppedBitmap(per,context));
		}else{
			rvs.setImageViewResource(R.id.icon,R.drawable.ic_launcher);
		}
		mNotificationManager.cancel(NotificationUtils.BlueNotfication);
		rvs.setTextViewText(R.id.step, 0+"");
		rvs.setTextViewText(R.id.kcal, String.format("%.1f", (0/10f))  );
		rvs.setTextViewText(R.id.dis, String.format("%.1f", 0/100000f) ); 
		// Sets an ID for the notification, so it can be updated
		NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(
				context).setSmallIcon(R.drawable.ic_launcher)
			//	.setContentTitle(context.getString(R.string.soft_step))
			//	.setContentText(context.getString(R.string.step) + "：" + 0)
				.setContentIntent(contentIntent)
				.setWhen(System.currentTimeMillis()).setContent(rvs);
		mNotificationManager.notify(NotificationUtils.GPSOrStepNotfication,
				mNotifyBuilder.build());

	}
	public Bitmap getCroppedBitmap(float progress,Context context) {

		Matrix m = new Matrix(); 

		 
		int w = (int)(context.getResources().getDimensionPixelSize(R.dimen.notification_step));
		int h = w;
		Bitmap output = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG));
		final Paint paint = new Paint(); 
		paint.setTextSize(context.getResources().getDimensionPixelSize(R.dimen.text_small));
		paint.setStrokeWidth(5);
		paint.setColor(Color.parseColor("#ffffff"));
		paint.setStrokeCap(Cap.ROUND);
		paint.setStyle(Paint.Style.STROKE); 
		paint.setAntiAlias(true);
		paint.setTextAlign(Align.CENTER);
		// paint.setFakeBoldText(true); 
		 
		
		paint.setColor(Color.parseColor("#33ffffff"));
		canvas.drawArc(new RectF(w*(0.1f), h*(0.1f), w*(0.9f), h*(0.9f)), -90f, 360 , false, paint);
		paint.setStrokeWidth(8);
		paint.setColor(Color.parseColor("#5F93EF"));
		canvas.drawArc(new RectF(w*(0.1f), h*(0.1f), w*(0.9f), h*(0.9f)), -90f, 360 * progress, false, paint);
		paint.setStrokeCap(Cap.SQUARE);
		Paint paint2 = new Paint();
		paint2.setTypeface(tf);
		paint2.setColor(Color.parseColor("#ffffff"));
		paint2.setStyle(Paint.Style.FILL);
		paint2.setTextSize(context.getResources().getDimension(R.dimen.text_middle)); 
		float f= paint2.measureText(Math.round(progress*100)+"");
		canvas.drawText(Math.round(progress*100)+"", (w * 0.5f)-(f*0.5f), h * 0.6f, paint2); 
		paint2.setTextSize(18);
		canvas.drawText("%", (w * 0.5f)+(f*0.5f), h * 0.6f, paint2); 
		return Bitmap.createBitmap(output, 0, 0, output.getWidth(),
				output.getHeight(), m, true);
	}
/***
 * 
 * @param context
 * @param connectAir 是否连接air,在开机启动时，如果是模式1，不连air
 */
	private void StartMilinkService(Context context,boolean connectAir) {
		// 获取设备类型
		SharedPreferences share = context
				.getSharedPreferences(
						SharePreUtils.APP_ACTION,
						Context.MODE_PRIVATE); 
		Dbutils db = new Dbutils(share.getInt("UID", -1), context);
		int device = db.getUserDeivceType();
		// 启动相应的服务
		// 0->蓝牙 1-> 3g 2->手机 3->ble 4->ant 5->air
	
		Intent intentblue = new Intent(context, BluetoothLeService.class);
		Intent intentstep = new Intent(context, StepService.class);
		Intent intentdct = new Intent("MilinkConfig");
		intentdct.putExtra("command", 2);

		switch (device) {

		case 5:// air device
			if (!context.getPackageManager().hasSystemFeature(
					PackageManager.FEATURE_BLUETOOTH_LE)) {

				return;
			}
			LogUtil.e("---BootstartBLE---"+new SimpleDateFormat("yyyy-MM-dd-:HH:mm:ss:SSS").format(new Date()));
			context.sendBroadcast(intentdct);
			context.stopService(intentstep);

			String address = db.getBTDeivceAddress();
			if (address != null && address.length() == 17 && connectAir) {
				// intent.putExtra("name", name);
				intentblue.putExtra("address", address);
				// 1 普通模式 2是固件升级
				intentblue.putExtra("command", 1);
				intentblue.putExtra("scanflag", 1);
				context.startService(intentblue);

				MyLog.v("BootReceiver", "Send Start BLE Service");
			}
			break;
		case 2:
			context.startService(intentstep);
			context.stopService(intentblue);
			break;
		default:
			try {
				// 注销软计步的监听器\
				context.sendBroadcast(intentdct);
				// 停止软计步服务
				context.stopService(intentstep);
				// 停止蓝牙服务
				context.stopService(intentblue);
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}

			break;
		}
	}

	private void showReport(Context context) {
		if(preferences==null){
			preferences = context.getSharedPreferences(
					SharePreUtils.APP_ACTION,
					Context.MODE_PRIVATE);
		}
		if(preferences.getInt("ISMEMBER", 0) == 0){
			return;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String today = sdf.format(new Date());
		Dbutils db = new Dbutils(preferences.getInt("UID", -1), context);
		Object[] target = db.getUserTarget();

		Object[] steps = db.getSpBasicData(sdf.format(Util
				.getBeforeAfterDate(today, -1)));
		// 显示步数
		if (steps != null && target != null) {
			try {
				int c = calculatePercent((Integer) target[3],
						(Integer) steps[0]);
				String s1 = "";
				if (c >= 80) {
					s1 = context.getString(R.string.yesterday_step)
							+ ((Integer) steps[0])
							+ context.getString(R.string.unit_step) + ","
							+ context.getString(R.string.finish_precent) + c
							+ "% " + context.getString(R.string.keep_status);
				} else {
					s1 = context.getString(R.string.yesterday_step)
							+ ((Integer) steps[0])
							+ context.getString(R.string.unit_step) + ","
							+ context.getString(R.string.finish_precent) + c
							+ "% " + context.getString(R.string.lazy);
				}
				showSportsNotification(s1, context);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (steps == null) {
			String s1 = context.getString(R.string.yesterday_step) + "0"
					+ context.getString(R.string.unit_step) + ","
					+ context.getString(R.string.finish_precent) + 0 + "% "
					+ context.getString(R.string.lazy);
			showSportsNotification(s1, context);
		}

	}

	private void showSportsNotification(String tip, Context context) {

		String today = new SimpleDateFormat("yyyy-MM-dd").format(Calendar
				.getInstance().getTime());

		if (!today.equals(preferences.getString("sporttime", ""))
				&& !tip.equals("")) {

			NotificationUtils not = new NotificationUtils(context,
					NotificationUtils.SportsNotfication);
			not.showCancelableNotification(
					context.getString(R.string.notification_sports), tip,
					SlideMainActivity.class);
			
			preferences.edit().putString("sporttime", today).commit();
		}

	}

	private int calculatePercent(int target, int current) {
		int percent = 0;
		if (current > target) {
			// percent =100;
			percent = (int) (((float) current / target) * 100);
		} else if (target == 0) {
			percent = 0;
		} else {
			percent = (int) (((float) current / target) * 100);
		}
		return percent;
	}

}

package com.milink.android.lovewalk.bluetooth.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.text.format.Time;

import com.bandlink.air.MyLog;
import com.bandlink.air.R;
import com.bandlink.air.SlideMainActivity;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.NotificationUtils;
import com.bandlink.air.util.SessionUpdate;
import com.bandlink.air.util.UploadData;

public class MilinkReceiver extends BroadcastReceiver {

	SharedPreferences share;
	Dbutils db;

	@Override
	public void onReceive(Context context, Intent intent) {
		MyLog.v("闹了",intent.getAction());
		share = context.getSharedPreferences("air", Context.MODE_MULTI_PROCESS);
		db = new Dbutils(share.getInt("UID", -1), context);
		if (intent.getAction().equals("StepReset")) {
			 
			StepService.step = 0;
			StepService.cal = 0;
			MyLog.v("计步器清零啦", "=========");
			int step = StepDetector.getNowStep();
			int calorie = StepDetector.getNowCal();
			int dis = step * StepService.step_length;
			db = new Dbutils(share.getInt("UID", -1), context);
			if(db.getUserDeivceType()==2){
				db.SaveSpBasicData(step, (int) (dis / 100.0),
						(int) (calorie * 100), 2, getTime(), "");// 软计步得到的是百卡，*100/
				Calendar calendar = Calendar.getInstance();
				calendar.roll(java.util.Calendar.DAY_OF_YEAR, -1);
				SimpleDateFormat sformat = new SimpleDateFormat("yyyy-MM-dd");
				String time = sformat.format(calendar.getTime());
				MyLog.v("stringtime", time);
				String strdate = sformat.format(Calendar.getInstance().getTime());
				db.saveSoftStep(strdate, 0, (int) 0);
				//updatenotification(context, 0);
				StepDetector.setConfig(0, 0, StepService.weight,
						StepService.step_length, StepService.username);
			}
			
		} else if (intent.getAction().equals("StepUpload")) {
			if (share.getInt("ISMEMBER", 0) != 0) {
				SessionUpdate update = SessionUpdate.getInstance(context);
				update.checkSession();
				MyLog.e("service_process----update", SessionUpdate.isUpload + "");
				if (SessionUpdate.isUpload) {
					UploadData upload = new UploadData(2, context,-1);
					upload.start();
				}
			}

		}

	}

	private String getTime() {
		Time time = new Time();
		time.setToNow();
		String str_time = String.format("%1$04d-%2$02d-%3$02d", time.year,
				time.month + 1, time.monthDay);
		return str_time;
	}

  void updatenotification(Context context, int step) {
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
		// Sets an ID for the notification, so it can be updated
		NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(
				context).setSmallIcon(R.drawable.ic_launcher)
			//	.setContentTitle(context.getString(R.string.soft_step))
			//	.setContentText(context.getString(R.string.step) + "：" + step)
				.setContentIntent(contentIntent)
				.setWhen(System.currentTimeMillis());
		mNotificationManager.notify(NotificationUtils.GPSOrStepNotfication,
				mNotifyBuilder.build());

	}
}

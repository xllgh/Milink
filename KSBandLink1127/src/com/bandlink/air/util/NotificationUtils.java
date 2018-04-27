package com.bandlink.air.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.bandlink.air.R;

public class NotificationUtils {
	private int notificationid = -1;
	private Context context;
	public static int BlueNotfication = 99;
	public static int GPSOrStepNotfication = 9;
	public static int SportsNotfication = 10;
	public static int PowerNotfication = 11;
	public static int LostNotfication = 12;
	public static int FoundNotfication = 13;

	//
	// public static int FriendNotfication=10;
	//
	// public static int FriendNotfication=10;
	//
	// public static int FriendNotfication=10;

	public NotificationUtils(Context context) {
		this.context = context;
	}

	public NotificationUtils(Context context, int nid) {
		this.context = context;
		notificationid = nid;
	}

	public void showNotification(String title, String content, Class activity,
			boolean sound) {
		NotificationCompat.Builder notification;
		if (activity != null) {
			Intent intent1 = new Intent(context, activity);
			intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_NEW_TASK);
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
					intent1, PendingIntent.FLAG_UPDATE_CURRENT);
			notification = new NotificationCompat.Builder(context)

			.setSmallIcon(R.drawable.ic_launcher).setContentTitle(title)
					.setContentText(content)
					.setWhen(System.currentTimeMillis())
					.setContentIntent(contentIntent);

		} else {
			notification = new NotificationCompat.Builder(context)

			.setSmallIcon(R.drawable.ic_launcher).setContentTitle(title)
					.setContentText(content)
					.setWhen(System.currentTimeMillis());
		}
		if (sound) {
			notification.setDefaults(Notification.DEFAULT_ALL);
		}
		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(notificationid, notification.build());

	}

	public void showCancelableNotification(String title, String content,
			Class activity) {
		Notification notification;
		if (activity != null) {
			Intent intent1 = new Intent(context, activity);
			intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_NEW_TASK);
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
					intent1, PendingIntent.FLAG_UPDATE_CURRENT);
			notification = new NotificationCompat.Builder(context)
					.setDefaults(Notification.DEFAULT_ALL).setAutoCancel(true)
					.setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle(title).setContentText(content)
					.setWhen(System.currentTimeMillis())
					.setContentIntent(contentIntent).build();

		} else {
			notification = new NotificationCompat.Builder(context)
					.setDefaults(Notification.DEFAULT_ALL)
					.setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle(title).setContentText(content)
					.setWhen(System.currentTimeMillis()).build();
		}

		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(notificationid, notification);

	}

}

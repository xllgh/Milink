package com.bandlink.air.ble;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.bandlink.air.R;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.SharePreUtils;

/***
 * 
 * @author Kevin
 * 
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class AirNotificationListener extends NotificationListenerService {
	SharedPreferences sharePre;
	public static final String ACTION_AIR_NOTIFICATION = "com.air.android.notificationlis";
	public static final String ACTION_AIR_ENABLELIST_CHANGED = "com.air.android.enable.changed";
	public static final int NOTIFICATION_WECHAT = 1001;
	public static final int NOTIFICATION_QQ = 1002;
	private Dbutils dbUtils;
	private ArrayList<String> list;

	@Override
	public void onCreate() {
		sharePre = getSharedPreferences(SharePreUtils.AIR_ACTION, MODE_MULTI_PROCESS);
		dbUtils = new Dbutils(SharePreUtils.getInstance(this).getUid(), this);
		list = dbUtils.getEnableList();
		IntentFilter inf = new IntentFilter();
		inf.addAction(ACTION_AIR_ENABLELIST_CHANGED);
		registerReceiver(EnableListReceiver, inf);
		dbUtils.saveNotificationPermission(true);
		super.onCreate();
	}

	BroadcastReceiver EnableListReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (ACTION_AIR_ENABLELIST_CHANGED.equals(intent.getAction())) {
				list = dbUtils.getEnableList();
			}
		}
	};

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		handler.removeCallbacksAndMessages(null);
		unregisterReceiver(EnableListReceiver);
		dbUtils.saveNotificationPermission(false);
		super.onDestroy();
	}

	@Override
	public void onNotificationPosted(StatusBarNotification sbn) {
		String version = sharePre.getString("soft_version", "");

		if (!sharePre.getBoolean("app_notification", true)) {
			return;
		}

		if (sbn.getId() != tempId) {
			tempId = sbn.getId();

		} else {
			return;
		}

		Message msg = handler.obtainMessage();
		msg.what = sbn.getId();
		msg.obj = sbn;
		handler.sendMessageDelayed(msg, 500);

	}

	int tempId = -1;
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			tempId = -1;

			StatusBarNotification sbn = (StatusBarNotification) msg.obj;
			Notification mNotification = sbn.getNotification();
			String pkg = sbn.getPackageName();
			Bundle extras = mNotification.extras;
			String title = extras.getString("android.title");
			String msgs = extras.getString("android.text");
			if (title == null && msgs == null) {
				return;
			}
			if (getString(R.string.upgrade_downloading).equals(title)) {
				return;
			}

			if (msgs == null || msgs.isEmpty()) {
				msgs = title;
				try {
					PackageInfo info = getPackageManager().getPackageInfo(pkg, 0);
					title = info.applicationInfo.loadLabel(getPackageManager()).toString();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (title == null || title.isEmpty()) {
				try {
					PackageInfo info = getPackageManager().getPackageInfo(pkg, 0);
					title = info.applicationInfo.loadLabel(getPackageManager()).toString();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			Intent i = new Intent(ACTION_AIR_NOTIFICATION);

			if (list == null || !list.contains(pkg)) {
				return;
			}
			if (title == null || title.isEmpty() || msgs == null || msgs.isEmpty()) {
				return;
			}
			i.putExtra("packet", pkg == null ? "" : pkg);
			i.putExtra("title", title);

			i.putExtra("msg", msgs);

			sendBroadcast(i);

			super.handleMessage(msg);
		};
	};

	/***
	 * 提取验证码 连续6位 字母或数字组合
	 * 
	 * @param msg
	 * @return
	 */
	public static String parserCheckCode(String msg) {
		if (msg == null || msg.length() <= 6) {
			return null;
		}
		Pattern p = Pattern.compile("(?<![a-zA-Z0-9])([a-zA-Z0-9]{6})(?![a-zA-Z0-9])");
		Matcher m = p.matcher(msg);
		ArrayList<String> str = new ArrayList<String>();
		while (m.find()) {
			str.add(m.group(1));
		}
		if (str.size() > 1) {
			Pattern p2 = Pattern.compile("(?<![0-9])([0-9]{6})(?![0-9])");
			Matcher m2 = p2.matcher(msg);
			if (m2.find()) {
				return m2.group(1);
			} else {
				return str.get(0);
			}
		} else if (str.size() == 1) {
			return str.get(0);
		} else {
			return null;
		}
	}

	@Override
	public void onNotificationRemoved(StatusBarNotification sbn) {
		tempId = -1;
		System.out.println("remove " + sbn.getPackageName() + " tempId set -1");
		handler.removeMessages(sbn.getId());
		Notification mNotification = sbn.getNotification();
		Bundle extras = mNotification.extras;
		String title = extras.getString("android.title");
		String msgs = extras.getString("android.text");
		Log.e("REMOVE", "" + sbn.getPackageName() + "--" + sbn.getId());
	}
}

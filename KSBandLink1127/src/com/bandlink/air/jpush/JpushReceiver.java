package com.bandlink.air.jpush;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.BigTextStyle;
import cn.jpush.android.api.JPushInterface;

import com.bandlink.air.MatchDetailActivity;
import com.bandlink.air.MyLog;
import com.bandlink.air.R;
import com.bandlink.air.friend.friendInviteActivity;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.SharePreUtils;

/**
 * 自定义接收器
 * 
 * 如果不定义这个 Receiver，则： 1) 默认用户会打开主界面 2) 接收不到自定义消息
 */
public class JpushReceiver extends BroadcastReceiver {
	private static final String TAG = "JPush";
	private String FriendType = "friend";
	SharedPreferences share;

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		MyLog.d(TAG, "[JpushReceiver] onReceive - " + intent.getAction()
				+ ", extras: " + printBundle(bundle));
		if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {// 获取唯一id
			final String regId = bundle
					.getString(JPushInterface.EXTRA_REGISTRATION_ID);
			share = context.getSharedPreferences(
					SharePreUtils.APP_ACTION,
					Context.MODE_PRIVATE);
			String lsjpushid = share.getString("JPUSHID", "");
			if (!lsjpushid.equals(regId)
					&& !share.getString("session_id", "").equals("")) {
				new Thread() {
					public void run() {
						String urlStr = HttpUtlis.BASE_URL + "/user/addJpush";
						Map<String, String> map = new HashMap<String, String>();
						map.put("jpushid", regId);
						map.put("session", share.getString("session_id", ""));
						try {
							String result = HttpUtlis.getRequest(urlStr, map);
							if (result != null) {
								JSONObject jsonObject;
								jsonObject = new JSONObject(result);
								int code = Integer.parseInt(jsonObject
										.getString("status"));
								if (code == 0) {
									share.edit().putString("JPUSHID", regId)
											.commit();
								}

							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}.start();

			}
			share.edit().putString("JPUSHID", regId).commit();
			// send the Registration Id to your server...

		} else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent// 接受自定义消息
				.getAction())) {
			MyLog.d(TAG,
					"[JpushReceiver] 接收到推送下来的自定义消息: "
							+ bundle.getString(JPushInterface.EXTRA_MESSAGE));
			processCustomMessage(context, bundle);

		} else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent // 接收api的广播
				.getAction())) {
			MyLog.d(TAG, "[JpushReceiver] 接收到推送下来的通知");
			int notifactionId = bundle
					.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
			MyLog.d(TAG, "[JpushReceiver] 接收到推送下来的通知的ID: " + notifactionId);
			//processCustomMessage(context, bundle);
		} else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent // 打开通知
				.getAction())) {
			MyLog.d(TAG, "[JpushReceiver] 用户点击打开了通知");

			JPushInterface.reportNotificationOpened(context,
					bundle.getString(JPushInterface.EXTRA_MSG_ID));
			openNotification(context, bundle);
			// 打开自定义的Activity
			// Intent i = new Intent(context, TestActivity.class);
			// i.putExtras(bundle);
			// i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			// context.startActivity(i);

		} else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent
				.getAction())) {
			MyLog.d(TAG,
					"[JpushReceiver] 用户收到到RICH PUSH CALLBACK: "
							+ bundle.getString(JPushInterface.EXTRA_EXTRA));
			// 在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity，
			// 打开一个网页等..

		} else if (JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent
				.getAction())) {
			boolean connected = intent.getBooleanExtra(
					JPushInterface.EXTRA_CONNECTION_CHANGE, false);
			MyLog.e(TAG, "[JpushReceiver]" + intent.getAction()
					+ " connected state change to " + connected);
		} else {
			MyLog.d(TAG,
					"[JpushReceiver] Unhandled intent - " + intent.getAction());
		}
	}

	// 打印所有的 intent extra 数据
	private static String printBundle(Bundle bundle) {
		StringBuilder sb = new StringBuilder();
		for (String key : bundle.keySet()) {
			if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
				sb.append("\nkey:" + key + ", value:" + bundle.getInt(key));
			} else if (key.equals(JPushInterface.EXTRA_CONNECTION_CHANGE)) {
				sb.append("\nkey:" + key + ", value:" + bundle.getBoolean(key));
			} else {
				sb.append("\nkey:" + key + ", value:" + bundle.getString(key));
			}
		}
		return sb.toString();
	}

	private void openNotification(Context context, Bundle bundle) {
		String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);

		String myValue = "";
		try {
			JSONObject extrasJson = new JSONObject(extras);
			myValue = extrasJson.optString("activity");
		} catch (Exception e) {
			return;
		}
		if (FriendType.equals(myValue)) {
			Intent mIntent = new Intent(context, friendInviteActivity.class);
			mIntent.putExtras(bundle);
			mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(mIntent);
		}
		try {
			JSONObject extrasJson = new JSONObject(extras);
			if (extrasJson.getString("type").equals("url")) {
				Uri uri = Uri
						.parse("http://" + extrasJson.getString("content"));
				// 通过Uri获得编辑框里的//地址，加上http://是为了用户输入时可以不要输入
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				// 建立Intent对象，传入uri
				context.startActivity(intent);
			} else if(extrasJson.getString("type").equals("match")){
				//用于邀请好友参加竞赛的推送接受
				Intent mIntent = new Intent(context, MatchDetailActivity.class);
				mIntent.putExtras(bundle);
				mIntent.putExtra("id", Integer.valueOf(extrasJson.getString("id")));
				mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(mIntent); 
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// send msg to MainActivity
	private void processCustomMessage(Context context, Bundle bundle) {
		// if (MainActivity.isForeground) {
		// String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
		// String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
		// Intent msgIntent = new Intent(MainActivity.MESSAGE_RECEIVED_ACTION);
		// msgIntent.putExtra(MainActivity.KEY_MESSAGE, message);
		// if (!ExampleUtil.isEmpty(extras)) {
		// try {
		// JSONObject extraJson = new JSONObject(extras);
		// if (null != extraJson && extraJson.length() > 0) {
		// msgIntent.putExtra(MainActivity.KEY_EXTRAS, extras);
		// }
		// } catch (JSONException e) {
		//
		// }
		//
		// }
		// context.sendBroadcast(msgIntent);
		// }
		try {
			JSONObject o = new JSONObject(bundle.getString("cn.jpush.android.EXTRA"));
			if (o.has("type")
					&& o.getString("type").equals("match")) {
				NotificationManager mNotificationManager = (NotificationManager) context
						.getSystemService(Context.NOTIFICATION_SERVICE);
				Intent ttt = new Intent(context,MatchDetailActivity.class);
				ttt.putExtra("id", o.getInt("id"));
				ttt.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				 NotificationCompat.BigTextStyle textStyle = new BigTextStyle();
			     textStyle
			             .setBigContentTitle(o.has("title")?o.getString("title"):"")
			             .setSummaryText(o.has("msg")?o.getString("msg"):"")
			             .bigText(o.has("content")?o.getString("content"):"");
			     android.app.Notification notification = new NotificationCompat.Builder(context)
			             .setSmallIcon(R.drawable.ic_launcher)
			             .setTicker(o.has("title")?o.getString("title"):"").setContentInfo("Lovefit")
			             .setContentTitle("ContentTitle").setContentText("ContentText")
			             .setStyle(textStyle)
			             .setAutoCancel(true).setDefaults(android.app.Notification.DEFAULT_ALL).setContentIntent(PendingIntent.getActivity(context, (int)(Math.random()*100), ttt, 0))
			             .build();
			     mNotificationManager.notify(156, notification);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}

package com.bandlink.air.jpush;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.bandlink.air.R;

public class Jpush {
	// demo App defined in resources/jpush-api.conf
	private static final String appKey = "8d37b56328005603f36f6386";
	private static final String masterSecret = "778857daee9b632d8e5cbd74";
	private static Context mcontext;

	public Jpush(Context context) {
		mcontext = context;
	}

	public static void sendFriendMessage(final String content,
			final String jpushid) {
		new Thread() {
			public void run() {
				JPushClient jpushClient = new JPushClient(masterSecret, appKey,
						3);
				// For push, all you need do is to build PushPayload object.
				try {
					Map map = new HashMap<String, String>();
					map.put("activity", "friend");
					PushResult result = jpushClient
							.sendAndroidIOSNotificationWithRegistrationID(
									mcontext.getString(R.string.friendinvite),
									content, map, jpushid);

					// PushResult result = jpushClient
					// .sendAndroidIOSNotificationWithRegistrationID(
					// mcontext.getString(R.string.friendinvite),
					// content, map, jpushid);

					// PushPayload payload =
					// jpushClient.buildPushObject_ios_tagAnd_alertWithExtrasAndMessage(mcontext.getString(R.string.friendinvite),
					// content, map, jpushid);
					// PushResult result= jpushClient.sendPush(payload);
				} catch (APIConnectionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (APIRequestException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}.start();

	}
	/***
	 * 2015-04-16 发送JPush通知
	 * @param content 文字内容
	 * @param jpushid 对方jpushid
	 * @param map 附加参数
	 */
	public static void sendMessage(final String content,
			final String jpushid,final HashMap<String, String> map) {
		new Thread() {
			public void run() {
				JPushClient jpushClient = new JPushClient(masterSecret, appKey,
						3);
				// For push, all you need do is to build PushPayload object.
				try {
				 
					PushResult result = jpushClient
							.sendAndroidIOSNotificationWithRegistrationID(
									mcontext.getString(R.string.friendinvite),
									content, map, jpushid);
					
					// PushResult result = jpushClient
					// .sendAndroidIOSNotificationWithRegistrationID(
					// mcontext.getString(R.string.friendinvite),
					// content, map, jpushid);
					
					// PushPayload payload =
					// jpushClient.buildPushObject_ios_tagAnd_alertWithExtrasAndMessage(mcontext.getString(R.string.friendinvite),
					// content, map, jpushid);
					// PushResult result= jpushClient.sendPush(payload);
				} catch (APIConnectionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (APIRequestException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}.start();
		
	}
	public static void sendJpushMessage(final String content,
			final String jpushid,final Class className,final Context con) {
		new Thread() {
			public void run() {
				JPushClient jpushClient = new JPushClient(masterSecret, appKey,
						3);
				// For push, all you need do is to build PushPayload object.
				try {
					HashMap<String, String> map = new HashMap<String, String>();
					if(className!=null){
						map.put("activity", className.getName());
					}
					PushResult result = jpushClient
							.sendAndroidIOSNotificationWithRegistrationID(
									con.getString(R.string.app_name),
									content, map, jpushid);

					// PushResult result = jpushClient
					// .sendAndroidIOSNotificationWithRegistrationID(
					// mcontext.getString(R.string.friendinvite),
					// content, map, jpushid);

					// PushPayload payload =
					// jpushClient.buildPushObject_ios_tagAnd_alertWithExtrasAndMessage(mcontext.getString(R.string.friendinvite),
					// content, map, jpushid);
					// PushResult result= jpushClient.sendPush(payload);
				} catch (APIConnectionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (APIRequestException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}.start();

	}

}

package com.bandlink.air.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class SessionUpdate {
	private static SessionUpdate sessionInstance;
	private static Context mcontext;
	private SharedPreferences preferences,share;
	public static Boolean isUpload = false;

	public static SessionUpdate getInstance(Context context) {
		mcontext = context;
		if (sessionInstance == null) {

			sessionInstance = new SessionUpdate();

		}
		return sessionInstance;

	}

	private ConnectivityManager cm;

	private boolean isNetworkConnected() {
		try {
			cm = (ConnectivityManager) mcontext
					.getSystemService(Context.CONNECTIVITY_SERVICE);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		NetworkInfo ni = cm.getActiveNetworkInfo();
		return ni != null && ni.isConnectedOrConnecting();
	}

	public void checkSession() {
		preferences = mcontext.getSharedPreferences("air",
				Context.MODE_MULTI_PROCESS);
		share = mcontext.getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
//		MyLog.e("service_process----username", preferences.getString("USERNAME", ""));
//		MyLog.e("service_process-------password", preferences.getString("PASSWORD", ""));
//		MyLog.e("service_process-----------uid", preferences.getInt("UID", -1)+"");
//		MyLog.e("service_process------------session_time", preferences.getString("session_time", ""));
//		MyLog.e("service_process-------------session_id", preferences.getString("session_id", ""));
		String session_time = preferences.getString("session_time", null);
		if (preferences.getInt("UID", -1) > 0 && isNetworkConnected()) {
			String time = new SimpleDateFormat("yyyyMMdd").format(new Date());
			if (session_time != null) {
				if (!session_time.equals(time)) {
					 getSession();
				} else {
					isUpload = true;
				}
			} else {
				 getSession();
			}
		} else {
			isUpload = false;
		}

	}

	private void getSession() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Map<String, String> logoargs = new HashMap<String, String>();
				logoargs.put("user", preferences.getString("USERNAME", ""));
				logoargs.put("pwd", preferences.getString("PASSWORD", ""));
				String s;
				try {
					s = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/user/"+(share.getBoolean("ispa", false)?"getloginPa":"getlogin"), logoargs);
					JSONObject status = new JSONObject(s);
					if (status.getInt("status") == 0) {
						JSONObject js = new JSONObject(
								new JSONObject(s).getString("content"));
						String session_id = js.getString("session_id");
						String time = js.getString("session_time");
						preferences.edit().putString("session_id", session_id)
								.commit();
						preferences.edit().putString("session_time", time)
								.commit();
						
//						MyLog.v("service_ok----username", preferences.getString("USERNAME", ""));
//						MyLog.v("service_ok-------password", preferences.getString("PASSWORD", ""));
//						MyLog.v("service_ok-----------uid", preferences.getInt("UID", -1)+"");
//						MyLog.v("service_ok------------session_time", preferences.getString("session_time", ""));
//						MyLog.v("service_ok-------------session_id", preferences.getString("session_id", ""));
						
						isUpload = true;
					} else {
						isUpload = false;

					}
				} catch (Exception e) {
					isUpload = false;
				}
			}
		}).start();
	}

}

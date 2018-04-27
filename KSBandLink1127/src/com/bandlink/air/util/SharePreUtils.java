package com.bandlink.air.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
 
public class SharePreUtils {
	private static final int SHARE_APP = 0;

	private static final int SHARE_AIR = 1;

	private static SharedPreferences shareApp, shareAir;

	private static SharePreUtils shareIntance;

	public final static String APP_ACTION = "com.baindlnk.lovewalk.preferences";

	public final static String AIR_ACTION = "baindlnk.air";

	private Editor appEdit, airEdit;

	public static final String MAIN_MODE = "main_mode";

	public static final String AlarmTempe = "AlarmTempe";

	private SharePreUtils(Context context) {

		shareApp = context.getSharedPreferences(APP_ACTION,
				Context.MODE_PRIVATE);
		appEdit = shareApp.edit();

		shareAir = context.getSharedPreferences(AIR_ACTION,
				Context.MODE_MULTI_PROCESS);
		airEdit = shareAir.edit();

	};

	public Editor getAppEditor() {
		return shareApp.edit();
	}

	public Editor getAirEditor() {
		return shareAir.edit();
	}
	public SharedPreferences getApp() {
		return shareApp;
	}
	
	public SharedPreferences getAir() {
		return shareAir;
	}

	public static SharePreUtils getInstance(Context context) {

		if (shareApp == null) {
			shareIntance = new SharePreUtils(context);
			return shareIntance;
		} else {
			return shareIntance;
		}

	}

	public int getMainMode() {
		if (shareApp != null) {
			return shareApp.getInt(MAIN_MODE, 0);
		} else {
			return 0;
		}
	}

	public int getUid() {
		if (shareApp != null) {
			return shareApp.getInt("UID", -1);
		} else {
			return -1;
		}
	}

	public String getNickName() {
		if (shareApp != null) {
			return shareApp.getString("NICKNAME", null);
		} else {
			return null;
		}
	}

	public void putMainMode(int value) {
		if (appEdit != null) {
			appEdit.putInt(MAIN_MODE, value).commit();
		}
	}

	public void putAlarmTempe(float value) {
		if (appEdit != null) {
			appEdit.putFloat(AlarmTempe, value).commit();
		}
	}

	public float getAlarmTempe() {
		if (shareApp != null) {
			return shareApp.getFloat(AlarmTempe, 38.5f);
		} else {
			return 38.5f;
		}
	}
}

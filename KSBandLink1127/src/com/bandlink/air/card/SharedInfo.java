package com.bandlink.air.card;

import java.util.Random;
import java.util.UUID;

import android.content.Context;
import android.telephony.TelephonyManager;

public class SharedInfo {
	public static String iccid;
	public static String msisdn;
	public static String imei;
	public static String imsi;
	public static String userMobile = "18812345678";
	private static boolean _getInfoDone = false;
	
	public static void getDeviceInfo(Context ctx) {
		TelephonyManager tm = (TelephonyManager)ctx.getSystemService(Context.TELEPHONY_SERVICE);
		SharedInfo.iccid = tm.getSimSerialNumber();
		SharedInfo.msisdn = tm.getLine1Number();
		SharedInfo.imei = tm.getDeviceId();
		SharedInfo.imsi = tm.getSubscriberId();
		
		if (imei == null || imei == "") {
			UUID uuid = UUID.randomUUID();
			imei = String.format("F%015d", uuid.getMostSignificantBits()).substring(0, 16);
		}
		
		while (imei.length() < 16) {
			imei = "0" + imei;
		}
		
		_getInfoDone = true;
	}
}

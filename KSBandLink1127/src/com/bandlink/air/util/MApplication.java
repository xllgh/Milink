package com.bandlink.air.util;

import java.util.LinkedList;

import com.bandlink.air.MyLog;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

public class MApplication{
	private static MApplication ma;
	private static LinkedList<Activity> list = new LinkedList<Activity>();

	private MApplication() {

	}

	public static MApplication getInstance() {
		if (ma == null) {
			ma = new MApplication();
		}
		return ma;
	}
	public void remove(Activity activity){
		if(list.contains(activity)){
			list.remove(activity);
		}
	}
	public void addActivity(Activity activity) {
		list.add(activity);
		MyLog.w("T", "加了一个后" + list.size());
	}

	public void clearAll() { 
		list.clear();
		MyLog.w("T", "clear之后" + list.size());
	}

	public LinkedList<Activity> getList() {
		return list;
	}
}

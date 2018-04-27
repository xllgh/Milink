package com.bandlink.air.gps;

import java.util.Date;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.bandlink.air.MyLog;
import com.bandlink.air.gps.MyLocationListerner.changeListener;

public class MiLocationServiceOnce extends Service implements changeListener {
	/**
	 * 
	 * @description 0 起始点 1 画路径 2 终点 -1 精度不够
	 * @author pmx
	 * @date 2013-8-17
	 * @version 1.0.0
	 * @since 1.0
	 */
	public interface UpdateUIListenerOnce {
		public void onchangeUI(int index, BDLocation locData);
	}

	public int m_runId;
	public int points;

	int locFlag = 0;// 是否首次定位
	private UpdateUIListenerOnce m_listener;
	private SharedPreferences m_settingPre;
	private static String TAG = "LocationService";
	private MyLocationListerner myListener = null;
	private LocationClient mLocClient;
	private LatLng lastPoint = null;

	private MyBinder myBinder = new MyBinder();

	// /

	public class MyBinder extends Binder {
		public MiLocationServiceOnce getService() {
			return MiLocationServiceOnce.this;
		}
	}

	public IBinder onBind(Intent intent) {
		return myBinder;
	}

	public void onCreate() {

		lastPoint = new LatLng(0, 0);

		myListener = new MyLocationListerner(this, false);
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		setLocationOption();
		mLocClient.start();

	}

	// 设置相关参数
	private void setLocationOption() {
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		// option.setScanSpan(5000);// 5秒去请求一次
		option.setScanSpan(1000);// 5秒去请求一次
		mLocClient.setLocOption(option);
	}

	Date lastDate = null;
	Date nowDate = null;

	int iAccuracyBigThres = 1000;
	int iAccuracyThres = 30;

	// int iAccuracyBigThres = 3000;
	// int iAccuracyThres = 2000;

	@Override
	public void onchange(BDLocation locData) {

		if (locData.getLatitude() > 0 && locData.getLongitude() > 0) {
			try {
				if (locData.getRadius() <= iAccuracyBigThres) {
					if (m_listener != null) {
						m_listener.onchangeUI(-2, locData);
					}

				}
			} catch (Exception e) {
				MyLog.e(TAG, "onchange exception...");
				e.printStackTrace();
			}

		}

	}

	public void setListener(UpdateUIListenerOnce listener) {
		m_listener = listener;
	}

	public void onDestroy() {
		locFlag = 0;
		mLocClient.stop();
		MyLog.v("once", "once finish");
		// startService(new Intent(LocationService.this, BootServer.class));
		// stopForegroundCompat(R.string.gps_service_started);
	}

}

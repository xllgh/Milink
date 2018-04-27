package com.bandlink.air.gps;

import org.json.JSONArray;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.bandlink.air.util.HttpUtlis;

public class MyLocationListerner implements BDLocationListener {

	private static String TAG = "MyLocationListenner";
	// private GeoPoint gp = null;
	private changeListener listener;
	BDLocation locData = new BDLocation();
	boolean bEmulate = false;
	double sss = 18.0;
	double test_longitude = 118.1;
	double test_latitude = 32.1;
	double test_inc = 0.001;

	public MyLocationListerner(changeListener lis, boolean debug) {
		this.listener = lis;
		// 测试数据
		if (debug) {
			getRoute();
		}
		bEmulate = debug;
		if (bEmulate) {
			test_longitude = 118.1;
			test_latitude = 32.1;
		}
	}

	public interface changeListener {
		public void onchange(BDLocation locData);
	}

	@Override
	public void onReceiveLocation(BDLocation location) {
		// TODO Auto-generated method stub

		if (location == null)
			return;
		int locType = location.getLocType();
		if (locType == BDLocation.TypeGpsLocation
				|| locType == BDLocation.TypeOffLineLocation
				|| locType == BDLocation.TypeNetWorkLocation
				|| locType == BDLocation.TypeOffLineLocationNetworkFail) {
			locData.setLatitude(location.getLatitude());
			locData.setLongitude(location.getLongitude());
			// 如果不显示定位精度圈，将accuracy赋值为0即可
			locData.setRadius(location.getRadius());
			locData.setSpeed(location.getSpeed());

			if (bEmulate) {
				// test_latitude += test_inc;
				// test_longitude += test_inc;
				// locData.latitude = test_latitude;
				// locData.longitude = test_longitude;
				// locData.accuracy = 5;
				// locData.speed = (float) sss;
				// sss += 0.1;
				// 真实模拟数据
				if (js != null) {
					if (index >= js.length()) {
						return;
					}
					locData.setLatitude(js.optJSONObject(index).optDouble(
							"latitude"));
					locData.setLongitude(js.optJSONObject(index).optDouble(
							"longitude"));
					locData.setRadius(js.optJSONObject(index)
							.optInt("accuracy"));
					;
					locData.setSpeed((float) js.optJSONObject(index).optDouble(
							"speed"));
					;
					index++;
				} else {
					return;
				}
			} else {

			}
			System.out.println(locData.getLatitude() + ","
					+ locData.getLongitude());
			listener.onchange(locData);
		}

	}

	int index = 0;
	JSONArray js;

	public void getRoute() {
		new Thread(new Runnable() {
			public void run() {
				try {
					String result = HttpUtlis
							.getRequest(
									"http://air.lovefit.com/index.php/home/expand/getRoute4Test/tid/60685",
									null);
					js = new JSONArray(result);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}).start();
	}

}

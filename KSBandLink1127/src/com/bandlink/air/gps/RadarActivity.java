package com.bandlink.air.gps;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapTouchListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.radar.RadarNearbyResult;
import com.baidu.mapapi.radar.RadarNearbySearchOption;
import com.baidu.mapapi.radar.RadarSearchError;
import com.baidu.mapapi.radar.RadarSearchListener;
import com.baidu.mapapi.radar.RadarSearchManager;
import com.bandlink.air.R;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Util;

public class RadarActivity extends LovefitActivity implements
		RadarSearchListener {

	private int pageIndex = 0;
	private BaiduMap mBaiduMap;
	private MapView mapView;
	private double la, lo;
	private ActionbarSettings actionBar;
	private ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setContentView(R.layout.activity_radar);
		actionBar = new ActionbarSettings(this, new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				RadarActivity.this.finish();
			}
		}, new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				AlertDialog.Builder ab = new AlertDialog.Builder(
						RadarActivity.this);
				ab.setTitle(R.string.warning);
				ab.setMessage("清除位置并退出");
				ab.setPositiveButton(R.string.exit,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								dialog = Util.initProgressDialog(
										RadarActivity.this, true,
										getString(R.string.data_wait), null);
								RadarSearchManager.getInstance()
										.clearUserInfo();
								SharePreUtils.getInstance(RadarActivity.this)
										.getAppEditor()
										.putBoolean("sharelocation", false)
										.commit();
							}
						});
				ab.setNegativeButton(R.string.cancel, null);
				ab.create().show();
			}
		});
		actionBar.setTopLeftIcon(R.drawable.ic_top_arrow);
		actionBar.setTitle("发现跑友");
		actionBar.setTopRightIcon(R.drawable.result_error);
		mapView = (MapView) findViewById(R.id.bmapsView);
		mBaiduMap = mapView.getMap();
		mapView.showZoomControls(false);
		// 周边雷达设置监听
		RadarSearchManager.getInstance().addNearbyInfoListener(this);
		// 周边雷达设置用户，id为空默认是设备标识
		RadarSearchManager.getInstance().setUserID(
				SharePreUtils.getInstance(this).getUid() + getPackageName());
		pageIndex = 0;
		lo = getIntent().getDoubleExtra("lo", 0);
		la = getIntent().getDoubleExtra("la", 0);
		searchRequest(pageIndex);
		mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker arg0) {
				// TODO Auto-generated method stub

				TextView tv = new TextView(RadarActivity.this);
				tv.setBackgroundResource(R.drawable.map_data_bg);
				tv.setText(arg0.getExtraInfo().getString("des", "路人甲"));
				tv.setTextColor(Color.WHITE);
				Options op = new Options();
				op.inJustDecodeBounds = true;
				Bitmap size = BitmapFactory.decodeResource(getResources(),
						R.drawable.icon_en);
				InfoWindow i = new InfoWindow(tv, arg0.getPosition(), 0 - size
						.getHeight());
				size.recycle();
				mBaiduMap.showInfoWindow(i);

				return true;
			}
		});
		mBaiduMap.setOnMapTouchListener(new OnMapTouchListener() {

			@Override
			public void onTouch(MotionEvent arg0) {
				// TODO Auto-generated method stub
				mBaiduMap.hideInfoWindow();
			}
		});
		super.onCreate(savedInstanceState);
	}

	/**
	 * 清除自己当前的信息
	 * 
	 * @param v
	 */
	public void clearInfoClick(View v) {
		RadarSearchManager.getInstance().clearUserInfo();
	}

	/**
	 * 查找周边的人
	 * 
	 * @param v
	 */
	public void searchNearby(View v) {

		pageIndex = 0;
		searchRequest(pageIndex);
	}

	/**
	 * 上一页
	 * 
	 * @param v
	 */
	public void preClick(View v) {
		if (pageIndex < 1) {
			return;
		}
		// 上一页
		pageIndex--;
		searchRequest(pageIndex);
	}

	/**
	 * 下一页
	 * 
	 * @param v
	 */
	public void nextClick(View v) {
		if (pageIndex >= totalPage - 1) {
			return;
		}
		// 下一页
		pageIndex++;
		searchRequest(pageIndex);
	}

	private int curPage, totalPage;

	private void searchRequest(int index) {
		curPage = 0;
		totalPage = 0;

		RadarNearbySearchOption option = new RadarNearbySearchOption()
				.centerPt(new LatLng(la, lo)).pageNum(pageIndex)
				.radius(20 * 1000);
		RadarSearchManager.getInstance().nearbyInfoRequest(option);

	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onGetClearInfoState(RadarSearchError arg0) {
		// TODO Auto-generated method stub
		if (arg0 == RadarSearchError.RADAR_NO_ERROR) {
			if (dialog != null && dialog.isShowing()) {
				dialog.dismiss();
			}
			RadarActivity.this.finish();
		}
	}

	@Override
	public void onGetNearbyInfoList(RadarNearbyResult arg0,
			RadarSearchError error) {
		// TODO Auto-generated method stub
		if (error == RadarSearchError.RADAR_NO_ERROR) {
			parseResultToMap(arg0);
		}

	}

	@Override
	public void onGetUploadState(RadarSearchError arg0) {
		// TODO Auto-generated method stub

	}

	BitmapDescriptor mBitmapDescriptor;

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub

		RadarSearchManager.getInstance().clearUserInfo();
		RadarSearchManager.getInstance().destroy();
		mapView.onDestroy();
		super.onDestroy();
		if (mBitmapDescriptor != null) {
			mBitmapDescriptor.recycle();
		}
	}

	/**
	 * 更新结果地图
	 * 
	 * @param res
	 */
	public void parseResultToMap(RadarNearbyResult res) {
		mBaiduMap.clear();
		mBitmapDescriptor = BitmapDescriptorFactory
				.fromResource(R.drawable.icon_point);
		LatLngBounds.Builder bounds = new LatLngBounds.Builder();
		if (res != null && res.infoList != null && res.infoList.size() > 0) {
			for (int i = 0; i < res.infoList.size(); i++) {
				// res.infoList.get(i).userID

				MarkerOptions option = new MarkerOptions()
						.icon(mBitmapDescriptor)
						.position(res.infoList.get(i).pt).title((i + 1) + "");
				Bundle des = new Bundle();
				if (res.infoList.get(i).comments == null
						|| res.infoList.get(i).comments.equals("")) {
					des.putString("des", "没有备注");
				} else {
					des.putString("des", res.infoList.get(i).comments);
				}
				bounds.include(res.infoList.get(i).pt);
				option.extraInfo(des);

				mBaiduMap.addOverlay(option);
				// mBitmapDescriptor.recycle();
			}
			mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(bounds
					.build().getCenter()));

		}

	}
}

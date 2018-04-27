package com.bandlink.air.gps;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapLoadedCallback;
import com.baidu.mapapi.map.BaiduMap.SnapshotReadyCallback;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.utils.DistanceUtil;
import com.bandlink.air.MyLog;
import com.bandlink.air.R;
import com.bandlink.air.WaterImage;
import com.bandlink.air.simple.SimpleHomeFragment;
import com.bandlink.air.user.LoginActivity;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.MediaUtils;
import com.bandlink.air.util.PrintScreen;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.wxapi.WXEntryActivity;
import com.umeng.socialize.bean.CustomPlatform;
import com.umeng.socialize.bean.MultiStatus;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners.MulStatusListener;
import com.umeng.socialize.controller.listener.SocializeListeners.OnSnsPlatformClickListener;
import com.umeng.socialize.controller.listener.SocializeListeners.SnsPostListener;
import com.umeng.socialize.media.QQShareContent;
import com.umeng.socialize.media.QZoneShareContent;
import com.umeng.socialize.media.SinaShareContent;
import com.umeng.socialize.media.TencentWbShareContent;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.umeng.socialize.sso.TencentWBSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;

public class GPSHistoryDetailActivity extends LovefitActivity {
	private static final String TAG = "GpsSportActivity";
	private Overlay graphicsOverlay = null;
	// TextView mTextView_stepnum;
	TextView mTextView_distance;
	TextView mTextView_cal;
	TextView mTextView_duration, msg_day;
	private ImageView ivFullscreen;
	LinearLayout buttonlayout;

	private ImageView m_btnImg;
	private MapView mMapView;
	private int zoomLevel;
	private RelativeLayout gpsmap;
	private ImageView gpsimage;
	private LinearLayout result;
	final UMSocialService mController = UMServiceFactory
			.getUMSocialService("com.umeng.share");
	/**
	 * /** 定位SDK监听函数
	 */

	private List<GPSItem> mList = new ArrayList<GPSItem>();

	// 配置文件
	private SharedPreferences m_settingPre;
	Dbutils db;
	int trackid;
	private int stepsNow, calNow;
	private boolean isFull;
	private LinearLayout mapother;
	String strdistance, duration, strwaterdis, resultpath, startime;
	Double calDouble;
	double distance;
	long totalTime; 
	private BaiduMap mBaiduMap;
	private ImageButton showMap;
	private int uid;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		trackid = bundle.getInt("iid");
		db = new Dbutils(GPSHistoryDetailActivity.this);
		ArrayList<GPSEntity> tracker = db.getGPSTrackCursorById(trackid);
		GPSEntity gps = tracker.get(0);
		stepsNow = gps.steps;
		calDouble = gps.calorie;
		calNow = calDouble.intValue();
		distance = gps.distance;
		totalTime = gps.durance;
		startime = gps.time;

		setContentView(R.layout.gps_history_detail);

		result = (LinearLayout) findViewById(R.id.result);
		gpsimage = (ImageView) findViewById(R.id.gpsimage);
		gpsmap = (RelativeLayout) findViewById(R.id.gpsmap);

		/* 建立MapView对象 */
		mMapView = (MapView) findViewById(R.id.bmapsView);
		mMapView.showZoomControls(false);
		mBaiduMap = mMapView.getMap();

		mBaiduMap.setOnMapLoadedCallback(new OnMapLoadedCallback() {

			@Override
			public void onMapLoaded() {
				// TODO Auto-generated method stub

			}
		});
		getListener();
		mapother = (LinearLayout) findViewById(R.id.layout_map_other);
		// mTextView_stepnum = (TextView) findViewById(R.id.text_step_funrun);
		mTextView_distance = (TextView) findViewById(R.id.text_distance_funrun);
		showMap = (ImageButton) findViewById(R.id.showMap);
		showMap.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				drawCell();
			}
		});
		mTextView_duration = (TextView) findViewById(R.id.text_clock_funrun);
		msg_day = (TextView) findViewById(R.id.msg_day);
		mTextView_cal = (TextView) findViewById(R.id.text_cal_funrun);
		Typeface tf = Typeface.createFromAsset(getAssets(),
				"font/AvenirLTStd-Light.otf");
		mTextView_distance.setTypeface(tf);
		mTextView_duration.setTypeface(tf);
		mTextView_cal.setTypeface(tf);
		TextView hint = (TextView) findViewById(R.id.gpshint);
		hint.setText(startime);
		ivFullscreen = (ImageView) findViewById(R.id.ivFullscreen);

		// mTextView_stepnum.setText(stepsNow + "");
		mTextView_cal.setText(String.format("%.1f", calDouble));
		updateBoardDistance(distance, 0);
		updateBoardTime(totalTime);
		/* 设定预设的放大层级 */
		zoomLevel = 17;
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(zoomLevel));
		m_settingPre = getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		uid = m_settingPre.getInt("UID", -1);
		db = new Dbutils(uid, this);
		result = (LinearLayout) findViewById(R.id.result);
		initSpan();
		ActionbarSettings actionbar = new ActionbarSettings(this,
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						GPSHistoryDetailActivity.this.finish();
					}
				}, new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						System.out.println("111111");
						showShareDialog();
					}
				});
		actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
		actionbar.setTitle(R.string.hi_detail);
		actionbar.setTopRightIcon(R.drawable.ic_share);

		Cursor cursor = db.getGPSPointCursor2(trackid);
		if (cursor != null && cursor.getCount() > 0) {
			Bitmap bit = BitmapFactory.decodeResource(getResources(),
					R.drawable.color_line);
			int w = bit.getWidth();
			colorSet = new int[w];
			for (int i = 0; i < w; i++) {
				colorSet[i] = bit.getPixel(i, 0);
			}
			drawHistory();
		}

	}

	public void myFull(View view) {
		isFull = !isFull;
		if (isFull)
			mapother.setVisibility(View.GONE);
		else
			mapother.setVisibility(View.VISIBLE);

	}

	final double nwLat_max = -90;
	final double nwLng_max = 180;
	final double seLat_max = 90;
	final double seLng_max = -180;

	double nwLat = nwLat_max;
	double nwLng = nwLng_max;
	double seLat = seLat_max;
	double seLng = seLng_max;

	void fitSpan2() {
		if (nwLat != nwLat_max) {
			LatLng center = new LatLng((nwLat + seLat) / 2, (nwLng + seLng) / 2);
			// add padding in each direction
			int spanLatDelta = (int) (Math.abs(nwLat - seLat) * 0.6 * 100000);
			int spanLngDelta = (int) (Math.abs(seLng - nwLng) * 0.6 * 100000);
			MyLog.v("sss", "fitspan" + " " + nwLat + " " + nwLng + " " + seLat
					+ " " + seLng + " " + spanLatDelta + " " + spanLngDelta);
			// fit map to points
			// mMapController.setCenter(center);
			// mMapController.zoomToSpan(spanLatDelta, spanLngDelta);
			// mMapView.refresh();
			LatLngBounds.Builder border = new LatLngBounds.Builder();
			border.include(new LatLng(nwLat, nwLng));
			border.include(new LatLng(seLat, seLng));

			mBaiduMap.setMapStatus(MapStatusUpdateFactory
					.newLatLngBounds(border.build()));
		}
	}

	Overlay cell;

	public void drawCell() {
		final UiSettings ui = mBaiduMap.getUiSettings();
		View zoomctrl = findViewById(R.id.zoomctrl);
		if (cell != null) {
			cell.remove();
			cell = null;
			ui.setZoomGesturesEnabled(true);
			ui.setScrollGesturesEnabled(true);
			ui.setRotateGesturesEnabled(true);
			zoomctrl.setVisibility(View.VISIBLE);
			return;
		}
		zoomctrl.setVisibility(View.GONE);
		// fitSpan2();
		new Handler().postDelayed(new Runnable() {
			public void run() {
				ui.setZoomGesturesEnabled(false);
				ui.setScrollGesturesEnabled(false);
				ui.setRotateGesturesEnabled(false);
				List<LatLng> pts = new ArrayList<LatLng>();
				pts.add(new LatLng(
						mBaiduMap.getMapStatus().bound.southwest.latitude,
						mBaiduMap.getMapStatus().bound.northeast.longitude));
				pts.add(mBaiduMap.getMapStatus().bound.northeast);

				pts.add(new LatLng(
						mBaiduMap.getMapStatus().bound.northeast.latitude,
						mBaiduMap.getMapStatus().bound.southwest.longitude));

				pts.add(mBaiduMap.getMapStatus().bound.southwest);

				OverlayOptions ooPolygon = new PolygonOptions().points(pts)
						.stroke(new Stroke(5, 0xffeee3de))
						.fillColor(0xffeee3de).zIndex(4);
				cell = mBaiduMap.addOverlay(ooPolygon);
			}
		}, 0);
	}

	private void setCenterPoint(LatLng ll) {
		MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
		mBaiduMap.animateMapStatus(u);
	}

	public void myzoomin(View v) {

		mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomIn());
	}

	public void myzoomout(View v) {

		mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomOut());
	}

	void gotoStart() {
		if (mList != null && mList.size() > 0) {
			// mMapController.animateTo(mList.get(0).getGp());
		}

	}

	private LinkedList<GPSItem> gList = new LinkedList<GPSItem>();

	void initSpan() {
		nwLat = -90;
		nwLng = 180;
		seLat = 90;
		seLng = -180;
	}

	ArrayList<LatLng> intMil = new ArrayList<LatLng>();
	double intval = 50;
	int standard = 1000;

	void drawHistory() {
		double distance = 0;
		mBaiduMap.clear();
		Cursor cursor = db.getGPSPointCursor2(trackid);
		double spAvg = 0;
		if (cursor != null) {
			while (cursor.moveToNext()) {

				double lati = cursor.getDouble(3);
				double longti = cursor.getDouble(4);
				double speed = cursor.getDouble(5);
				float accuracy = cursor.getFloat(5);
				if (accuracy >= 32 || speed > 30) {
					continue;
				}
				LatLng gp = new LatLng(lati, longti);

				spAvg += speed;
				if (speed <= speedSlow) {
					speedSlow = speed;
				}
				if (speed >= speedFast) {
					speedFast = speed;
				}

				if (gList.size() > 0) {
					distance += DistanceUtil
							.getDistance(gList.getLast().gp, gp);
					double abs = Math.abs((distance) - standard);
					if (intval > abs) {
						// 找到新的点的绝对值小
						intval = abs;
					} else {
						// 找到最小的了
						if (intval < 50) {
							intMil.add(gList.getLast().gp);
							standard += 1000;
							intval = 50;
						}
					}

				}

				gList.add(new GPSItem(speed, gp, accuracy));
				nwLat = Math.max(nwLat, gp.latitude);// 西北经度最大
				nwLng = Math.min(nwLng, gp.longitude);// 西北的纬度最小
				seLat = Math.min(seLat, gp.latitude);//
				seLng = Math.max(seLng, gp.longitude);

			}
			cursor.close();
		}
		spAvg = (double) spAvg / (double) gList.size();
		// GPSItem last = realPoint.getFirst();
		// for (int i = 1; i < realPoint.size(); i++) {
		// // double x1 = last.getGp().latitude;
		// // double x = x1;
		// // double x2 = realPoint.get(i).getGp().latitude;
		// // double y1 = last.getGp().longitude;
		// // double y2 = realPoint.get(i).getGp().longitude;
		// // double intrval = (x2 - x1) / 10f;
		// // double sp = (last.getSpeed() - realPoint.get(i).getSpeed()) / 30;
		//
		// gList.add(last);
		// // while((x+=intrval)<x2){
		// // gList.add(new GPSItem(sp+=last.speed, new LatLng(x,
		// // ((x-x1)*(y2-y1)/(x2-x1))+y1), last.radius));
		// // }
		// //last = realPoint.get(i);
		// }
		updateBoardDistance(distance, spAvg);
		drawPoint(gList.getFirst().getGp(), R.drawable.icon_st);

		LinkedList<GPSItem> pos = new LinkedList<GPSItem>();
		for (int i = 0; i < gList.size(); i++) {

			pos.add(gList.get(i));
			if (i > 0 && i % 2 == 0) {
				drawLine(pos);
			}
		}
		drawPoint(gList.getLast().getGp(), R.drawable.icon_en);
		new Handler().postDelayed(new Runnable() {
			public void run() {
				fitSpan2();
			}
		}, 300);

		try {
			drawMiles();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void drawMiles() {
		View view = LayoutInflater.from(this).inflate(R.layout.gpspointview,
				null);
		for (LatLng gp : intMil) {
			TextView t = (TextView) view.findViewById(R.id.text);
			t.setText((intMil.indexOf(gp) + 1) + "");
			MarkerOptions ooPolyline = new MarkerOptions().zIndex(7)
					.icon(BitmapDescriptorFactory.fromView(view)).position(gp);
			// mBaiduMap.addOverlay(ooPolyline);
			mBaiduMap.addOverlay(ooPolyline);
		}
	}

	double speedFast = 0;
	double speedSlow = 100;

	void drawPoint(LatLng _gp, int resId) {

		MarkerOptions ooPolyline = new MarkerOptions().zIndex(6)
				.icon(BitmapDescriptorFactory.fromResource(resId))
				.position(_gp);
		// mBaiduMap.addOverlay(ooPolyline);
		mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(_gp));

		mBaiduMap.addOverlay(ooPolyline);
		if (resId == R.drawable.icon_en) {

			// 结束
			TextView tv = new TextView(GPSHistoryDetailActivity.this);
			tv.setBackgroundResource(R.drawable.map_data_bg);
			tv.setText(String.format("%.1f",
					(Double.valueOf(strdistance) / 1000f))
					+ getString(R.string.unit_distance_km)
					+ "\t"
					+ (totalTime / 60) + "′" + (totalTime % 60) + "″");
			tv.setTextColor(Color.WHITE);
			Options op = new Options();
			op.inJustDecodeBounds = true;
			Bitmap size = BitmapFactory.decodeResource(getResources(),
					R.drawable.icon_en);
			InfoWindow i = new InfoWindow(tv, _gp, 0 - size.getHeight());
			size.recycle();
			mBaiduMap.showInfoWindow(i);
		}

	}

	LinkedList<LatLng> temp = new LinkedList<>();
	public int slowColor = 0xff03C841;
	public int fastColor = 0xffFF682B;
	int startColor;
	int lastColor;

	void drawLine(LinkedList<GPSItem> points) {

		LinkedList<GPSItem> point = new LinkedList<GPSItem>();
		distance += DistanceUtil.getDistance(points.getFirst().getGp(), points
				.getLast().getGp());

		point.addAll(points);
		points.clear();
		points.push(point.getLast());
		// System.out.println("SPEED："+point.getFirst().getSpeed());
		int index = Math.round(colorSet.length
				* (float) (point.getFirst().getSpeed() + point.getLast()
						.getSpeed()) * 0.5f / 20f);
		if (index >= colorSet.length - 1) {
			// index = colorSet.length -1;
			startColor = lastColor;
		} else {
			startColor = colorSet[index];
			lastColor = startColor;
		}

		temp.clear();
		for (GPSItem po : point) {
			temp.add(po.getGp());
		}

		OverlayOptions ooPolyline = new PolylineOptions().width(6)
				.color(startColor).points(temp).dottedLine(false).zIndex(5);
		// mBaiduMap.addOverlay(ooPolyline);

		mBaiduMap.addOverlay(ooPolyline);

	}

	// private OverlayItem mCurItem = null;
	// private MyOverlay mOverlay = null;
	// private ArrayList<OverlayItem> mItems = null;
	//
	// public class MyOverlay extends ItemizedOverlay {
	//
	// public MyOverlay(Drawable defaultMarker, MapView mapView) {
	// super(defaultMarker, mapView);
	// }
	//
	// @Override
	// public boolean onTap(int index) {
	// OverlayItem item = getItem(index);
	// mCurItem = item;
	// return true;
	// }
	//
	// @Override
	// public boolean onTap(GeoPoint pt, MapView mMapView) {
	// return false;
	// }
	//
	// }

	void updateBoardTime(long totalTime) {
		Integer seconds = (int) (totalTime % 60);
		Integer minutes = (int) ((totalTime / 60) % 60);
		Integer hours = (int) (totalTime / 3600);
		duration = String.format("%1$02d:%2$02d:%3$02d", hours, minutes,
				seconds);
		Pattern p = Pattern.compile("-([0-9]{2}-[0-9]{2}) ([0-9]{2})");
		Matcher m = p.matcher(startime);
		if (m.find()) {
			mTextView_duration.setText(m.group(1));

			String str = "";
			int temp = Integer.valueOf(m.group(2));

			if (temp <= 7 && temp >= 5) {
				str = getResources().getString(R.string.matinal);
			} else if (temp >= 8 && temp <= 10) {
				str =getResources().getString(R.string.am);
			} else if (temp >= 11 && temp <= 14) {
				str = getResources().getString(R.string.noon);
			} else if (temp > 14 && temp <= 17) {
				str =getResources().getString(R.string.pm);
			} else if (temp > 17 && temp <= 19) {
				str = getResources().getString(R.string.banwan);
			} else if (temp >= 20 && temp <= 23) {
				str = getResources().getString(R.string.yewan);
			} else {
				str = getResources().getString(R.string.shengye);
			}

			msg_day.setText(str);
		}

	}

	void updateBoardDistance(double distance, double speed) {

		strdistance = String.format("%1$.1f", distance);
		strwaterdis = String.format("%1$.1f", distance / 1000);
		mTextView_distance.setText(String.format("%.1fKM/h", speed));
	}

	int[] colorSet;

	void initBoard() {
		// mTextView_stepnum.setText("0");
		mTextView_cal.setText("0");
		mTextView_distance.setText("0");
		mTextView_duration.setText("00:00:00");

	}

	// public Graphic drawPoint(GeoPoint _gp, int nf) {
	// // 构建线
	// Geometry pointGeometry = new Geometry();
	//
	// pointGeometry.setPoint(_gp, 10);
	// // 设定样式
	// Symbol pointSymbol = new Symbol();
	// Symbol.Color pointColor = pointSymbol.new Color();
	// if (nf == 1) {
	// pointColor.green = 255;
	// pointColor.red = 0;
	// } else if (nf == 0) {
	// pointColor.blue = 0;
	// pointColor.red = 255;
	// }
	// pointColor.blue = 0;
	// pointColor.alpha = 255;
	// pointSymbol.setLineSymbol(pointColor, 10);
	// // 生成Graphic对象
	// Graphic lineGraphic = new Graphic(pointGeometry, pointSymbol);
	// return lineGraphic;
	// }

	// 1，如果有2个点，则画2点
	// 2，如果大于2个点，则最多画3个点
	private int colorchange = 30;

	// public Graphic drawLine() {
	// // 构建线
	// Geometry lineGeometry = new Geometry();
	// // 设定折线点坐标
	// GeoPoint[] linePoints = null;
	//
	// // linePoints = new GeoPoint[gList.size()];
	// // for (int i = 0; i < gList.size(); i++) {
	// // linePoints[i] = gList.get(i).getGp();
	// // }
	//
	// if (gList.size() < 3) {
	// linePoints = new GeoPoint[gList.size()];
	// for (int i = 0; i < gList.size(); i++)
	// linePoints[i] = gList.get(i).getGp();
	// } else {
	// linePoints = new GeoPoint[3];
	// linePoints[0] = gList.get(gList.size() - 3).getGp();
	// linePoints[1] = gList.get(gList.size() - 2).getGp();
	// linePoints[2] = gList.get(gList.size() - 1).getGp();
	// }
	//
	// lineGeometry.setPolyLine(linePoints);
	// // 设定样式
	// Symbol lineSymbol = new Symbol();
	// Symbol.Color lineColor = lineSymbol.new Color();
	//
	// // 颜色blue一直为0
	// // red为speed%255
	// // green为speed、255
	//
	// // m_curSpeed [0,255];
	//
	// // lineColor.red = 0;
	// // lineColor.green = 0;
	// // lineColor.blue = 200;
	// // lineColor.alpha = 255;
	//
	// float ss = (float) gList.get(2).speed;
	// int r = 0, g = 0, b = 0;
	// float factor = (float) ss / 20;
	// factor = 1 - factor;
	// if (factor >= 1.0) {
	// factor = 1.0f;
	// } else if (factor < 0) {
	// factor = 0f;
	// }
	// int rgb = getTrafficlightColor(factor);
	// r = (rgb & 0xff0000) >> 16;
	// g = (rgb & 0xff00) >> 8;
	// b = (rgb & 0xff);
	//
	// lineColor.red = r;
	// lineColor.green = g;
	// lineColor.blue = b;
	// lineColor.alpha = 220;
	//
	// lineSymbol.setLineSymbol(lineColor, 5);
	// // 生成Graphic对象
	// Graphic lineGraphic = new Graphic(lineGeometry, lineSymbol);
	// return lineGraphic;
	//
	// }

	int getTrafficlightColor(double value) {
		return android.graphics.Color.HSVToColor(new float[] {
				(float) value * 120f, 1f, 1f });
	}

	/* format移动距离的method */
	public String format(double num) {
		NumberFormat formatter = new DecimalFormat("###");
		String s = formatter.format(num);
		return s;
	}

	@Override
	protected void onPause() {
		/**
		 * MapView的生命周期与Activity同步，当activity挂起时需调用MapView.onPause()
		 */
		mMapView.onPause();
		super.onPause();
	}

	boolean isSatelite = false;

	public void changeMaptype(View view) {
		if (isSatelite) {
			mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
			isSatelite = false;
		} else {
			mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
			isSatelite = true;
		}

	}

	private void getListener() {

		// mMapListener = new MKMapViewListener() {
		// @Override
		// public void onGetCurrentMap(Bitmap bitmap) {
		// try {
		// System.out.println("sssssssssssss");
		// // TODO Auto-generated method stub
		// final MediaUtils utils = new MediaUtils(
		// GPSHistoryDetailActivity.this);
		// String mapath = utils.GpsMapSnapshot(bitmap);
		// final Bitmap bb = BitmapFactory.decodeFile(mapath);
		// gpsimage.setImageBitmap(bb);
		// gpsimage.setVisibility(View.VISIBLE);
		// gpsmap.setVisibility(View.GONE);
		//
		// setShareContent(bitmap);
		// mController.openShare(GPSHistoryDetailActivity.this, false);
		// // new Handler().postDelayed(new Runnable() {
		// // @Override
		// // public void run() {
		// // // TODO Auto-generated method stub
		// // // showShare(false, null, false);
		// // setShareContent();
		// // mController.openShare(
		// // GPSHistoryDetailActivity.this, false);
		// // }
		// // }, 10);
		// } catch (Exception e) {
		// // TODO: handle exception
		// MyLog.e("gps", e.toString());
		// }
		//
		// }
		//
		// @Override
		// public void onMapAnimationFinish() {
		// // TODO Auto-generated method stub
		//
		// }
		//
		// @Override
		// public void onClickMapPoi(MapPoi arg0) {
		// // TODO Auto-generated method stub
		//
		// }
		//
		// @Override
		// public void onMapMoveFinish() {
		// // TODO Auto-generated method stub
		//
		// }
		//
		// @Override
		// public void onMapLoadFinish() {
		// // TODO Auto-generated method stub
		//
		// }
		//
		// };

	}

	public void saveMyBitmap(Bitmap bitmap) {
		String strPath = Environment.getExternalStorageDirectory().toString()
				+ "/milink/waterimage/";
		OutputStream fos = null;
		File f = new File(strPath, "waterimage.jpg");

		if (f.exists()) {
			f.delete();
		}
		try {
			FileOutputStream out = new FileOutputStream(f);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.flush();
			out.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	int color = 0x5f93ef;

	public String layoutscreenpng(Bitmap bm) {

		File path = new File(Environment.getExternalStorageDirectory()
				.getPath() + HttpUtlis.TEMP_Folder + "/gps", "gpsresult.png");
		if (path.exists())
			path.delete();
		try {
			FileOutputStream out = new FileOutputStream(path);
			bm.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return path.getAbsolutePath();
	}

	private void setShareContent(Bitmap bb) { 
		// MediaUtils utils = new MediaUtils(this);
		Bitmap fa = PrintScreen.takeScreenShot(this);
		Canvas canvas = new Canvas(fa);
		canvas.drawBitmap(bb, 0, (fa.getHeight() / 2f) - (bb.getHeight() / 2f),
				new Paint());
		Paint paint = new Paint();
		paint.setColor(color);
		paint.setTextAlign(Align.CENTER);
		// Rect rect = new Rect(0, 0, fa.getWidth(),
		// (int) ((fa.getHeight() / 2f) - (bb.getHeight() / 2f)));
		// canvas.drawRect(rect, paint);
		// paint.setColor(Color.parseColor("#ffffff"));
		// paint.setStyle(Paint.Style.FILL);
		// paint.setTextSize(getResources().getDimension(R.dimen.text_large));
		// canvas.drawText(getString(R.string.gps_hint4), fa.getWidth() / 2f
		// ,
		// (rect.height() /
		// 2f)+getResources().getDimension(R.dimen.text_large)/2, paint);
		// System.out.println("ttttttttttttttt");
		gpsimage.setVisibility(View.GONE);
		mMapView.setVisibility(View.VISIBLE);
		// 设置分享内容
		mController.setShareContent(getString(R.string.gps_hint4));
		resultpath = layoutscreenpng(Bitmap.createBitmap(fa));
		UMImage ui = new UMImage(this, resultpath);
		// 设置分享图片, 参数2为图片的url地址

		// oks.setImagePath(Environment.getExternalStorageDirectory()
		// .toString() + "/milink/waterimage/waterimage.jpg");

		mController.setShareMedia(ui);

		// 设置分享图片，参数2为本地图片的资源引用
		mController.setShareMedia(new UMImage(this, R.drawable.ic_launcher));

		CustomPlatform customPlatform = new CustomPlatform("contacts",
				getString(R.string.app_name), R.drawable.ic_launcher);
		customPlatform.mClickListener = new OnSnsPlatformClickListener() {
			@Override
			public void onClick(Context context, SocializeEntity entity,
					SnsPostListener listener) {
				// Intent i = new Intent();
				// i.putExtra("imagePath", resultpath);
				// i.putExtra("tid", 3);
				// i.putExtra("witch", "1");
				// i.setClass(GPSHistoryDetailActivity.this,
				// ShareEditActivity.class);
				// startActivity(i);
				shareToLovefit();
			}
		};
		mController.getConfig().addCustomPlatform(customPlatform);

		// QQ
		UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler(this, LoginActivity.QQ_APP_ID,
				LoginActivity.QQ_APP_KEY);
		qqSsoHandler.setTargetUrl("http://www.lovefit.com");
		QQShareContent qq = new QQShareContent();
		// 设置title
		qq.setShareContent(getString(R.string.gps_hint4));
		qq.setTargetUrl("http://www.lovefit.com");
		qq.setTitle(getString(R.string.app_name));
		qq.setShareImage(ui);
		mController.setShareMedia(qq);
		qqSsoHandler.addToSocialSDK();

		// QZONE
		QZoneSsoHandler qZoneSsoHandler = new QZoneSsoHandler(this,
				LoginActivity.QQ_APP_ID, LoginActivity.QQ_APP_KEY);
		qZoneSsoHandler.setTargetUrl("http://www.lovefit.com");
		QZoneShareContent qz = new QZoneShareContent();
		// 设置title
		qz.setShareContent(getString(R.string.gps_hint4));
		qz.setTargetUrl("http://www.lovefit.com");
		qz.setTitle(getString(R.string.app_name));
		qz.setShareImage(ui);
		mController.setShareMedia(qz);
		qZoneSsoHandler.addToSocialSDK();
		// 微信
		UMWXHandler wxHandler = new UMWXHandler(this, WXEntryActivity.AppId,
				WXEntryActivity.AppSecret);
		wxHandler.setTargetUrl("http://www.lovefit.com");
		// 设置微信好友分享内容
		WeiXinShareContent weixinContent = new WeiXinShareContent();
		// 设置title
		weixinContent.setTargetUrl("http://www.lovefit.com");
		// weixinContent.setShareContent(getString(R.string.gps_hint4));
		weixinContent.setTitle(getString(R.string.app_name));
		weixinContent.setShareImage(ui);

		mController.setShareMedia(weixinContent);
		wxHandler.addToSocialSDK();

		// 微信朋友圈
		UMWXHandler wxCircleHandler = new UMWXHandler(this,
				WXEntryActivity.AppId, WXEntryActivity.AppSecret);
		wxCircleHandler.setTargetUrl("http://www.lovefit.com");
		// 设置微信好友分享内容
		CircleShareContent cs = new CircleShareContent();
		// cs.setShareContent(getString(R.string.gps_hint4));
		// 设置title
		cs.setTargetUrl("http://www.lovefit.com");
		cs.setTitle(getString(R.string.app_name));
		cs.setShareImage(ui);

		mController.setShareMedia(cs);
		wxCircleHandler.setToCircle(true);
		wxCircleHandler.addToSocialSDK();
		// 新浪微博
		mController.getConfig().setSsoHandler(new SinaSsoHandler());
		mController.getConfig().setSinaCallbackUrl(
				"http://sns.whalecloud.com/sina2/callback");
		SinaShareContent si = new SinaShareContent(); // 设置title
		si.setTargetUrl("http://www.lovefit.com");
		si.setShareContent(getString(R.string.gps_hint4));
		si.setTitle(getString(R.string.app_name));
		si.setShareImage(ui);
		mController.setShareMedia(si);
		// 腾讯微博
		mController.getConfig().setSsoHandler(new TencentWBSsoHandler());
		TencentWbShareContent tb = new TencentWbShareContent(); // 设置title
		tb.setTargetUrl("http://www.lovefit.com");
		tb.setShareContent(getString(R.string.gps_hint4));
		tb.setTitle(getString(R.string.app_name));
		tb.setShareImage(ui);
		mController.setShareMedia(tb);
		SHARE_MEDIA[] platforms = new SHARE_MEDIA[] { SHARE_MEDIA.WEIXIN,
				SHARE_MEDIA.WEIXIN_CIRCLE, SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE,
				SHARE_MEDIA.SINA, SHARE_MEDIA.TENCENT };

		mController.postShareMulti(GPSHistoryDetailActivity.this,
				new MulStatusListener() {

					@Override
					public void onStart() {
					}

					@Override
					public void onComplete(MultiStatus multiStatus, int st,
							SocializeEntity entity) {
						// String showText = "分享结果：" + multiStatus.toString();

					}
				}, platforms);
	}

	private void shareToLovefit() {
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle(R.string.warning);
		ab.setMessage(R.string.share);
		
		ab.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						SimpleHomeFragment.sendFeed(resultpath,
								GPSHistoryDetailActivity.this, uid,true);
						Toast.makeText(GPSHistoryDetailActivity.this,
								R.string.check_later, Toast.LENGTH_LONG).show();
					}
				});
		ab.setNegativeButton(R.string.cancel, null);
		ab.create().show();

	}

	@Override
	protected void onResume() {
		/**
		 * MapView的生命周期与Activity同步，当activity恢复时需调用MapView.onResume()
		 */
		mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onDestroy() {

		// MyLog.e(TAG, "destory save runId:" + m_runId);

		// stopService(new Intent(GpsSportActivity.this, BootServer.class));
		super.onDestroy();
	}

	public void shareGps(View view) {
		// mMapView.getCurrentMap();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// mMapView.onSaveInstanceState(outState);
		super.onSaveInstanceState(outState);

	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		// mMapView.onRestoreInstanceState(savedInstanceState);
	}

	public void showShareDialog() {
		String[] items = getResources().getStringArray(R.array.shareitem);
		Context context = this;
		if (Build.VERSION.SDK_INT >= 11) {
			context = new ContextThemeWrapper(this,
					android.R.style.Theme_Holo_Light);
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.gps_share);
		builder.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if (which == 0) {
					// mMapView.getCurrentMap();
					mBaiduMap.snapshotScope(null, new SnapshotReadyCallback() {

						@Override
						public void onSnapshotReady(Bitmap bitmap) {
							// TODO Auto-generated method stub
							try {

								final MediaUtils utils = new MediaUtils(
										GPSHistoryDetailActivity.this);
								String mapath = utils.GpsMapSnapshot(bitmap);
								final Bitmap bb = BitmapFactory
										.decodeFile(mapath);
								gpsimage.setImageBitmap(bb);
								gpsimage.setVisibility(View.VISIBLE);
								mMapView.setVisibility(View.GONE);
								gpsmap.findViewById(R.id.showctrl)
										.setVisibility(View.GONE);
								gpsmap.findViewById(R.id.zoomctrl)
										.setVisibility(View.GONE);
								setShareContent(bitmap);
								mController.openShare(
										GPSHistoryDetailActivity.this, false);
								// new Handler().postDelayed(new Runnable() {
								// @Override
								// public void run() {
								// // TODO Auto-generated method stub
								// // showShare(false, null, false);
								// setShareContent();
								// mController.openShare(
								// GPSHistoryDetailActivity.this, false);
								// }
								// }, 10);
							} catch (Exception e) {
								// TODO: handle exception
								MyLog.e("gps", e.toString());
							}

						}
					});

				} else {
					Intent intent = new Intent(GPSHistoryDetailActivity.this,
							WaterImage.class);
					intent.putExtra("steps", "" + stepsNow);
					intent.putExtra("distance", strwaterdis);
					intent.putExtra("calorie", calDouble);
					Integer seconds = (int) (totalTime % 60);
					Integer minutes = (int) ((totalTime / 60) % 60);
					Integer hours = (int) (totalTime / 3600);
					intent.putExtra("min_s", String.format("%1$02d:%2$02d",
							hours * 60 + minutes, seconds));
					startActivity(intent);
				}
			}
		});
		builder.setNegativeButton(getString(R.string.close),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface d1, int which) {
						// TODO Auto-generated method stub
						d1.dismiss();
					}
				});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub
		this.color = color;
	}
}

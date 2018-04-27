package com.bandlink.air.gps;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapLoadedCallback;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.OverlayManager;
import com.bandlink.air.GpsSportActivity;
import com.bandlink.air.MilinkApplication;
import com.bandlink.air.MyLog;
import com.bandlink.air.R;
import com.bandlink.air.gps.MiLocationServiceOnce.UpdateUIListenerOnce;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.LovefitSlidingActivity;
import com.bandlink.air.util.MainInterface;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Util;
import com.umeng.analytics.MobclickAgent;

public class RunNowFragment extends Fragment implements MKOfflineMapListener,
		OnClickListener {

	private Boolean isCentering;
	private boolean bResumed;
	private View mView;
	private ActionbarSettings actionBar;
	private MainInterface interf;
	private MapView mMapView;
	private int zoomlevel;
	private SharedPreferences m_settingPre;
	private final static String TAG = "RunNowFragment";
	private Dbutils db;
	private TextView distanceCount, kcalCount, durationCount, timesCount;
	private ImageView btnVoice, btnLocation, signl;
	private Button btnStart;
	private ArrayList<GPSEntity> allTrack;
	private LovefitSlidingActivity activity;
	private BaiduMap mBaiduMap;

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		interf = (MainInterface) activity;
		this.activity = (LovefitSlidingActivity) activity;
		super.onAttach(activity);
	}

	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		MilinkApplication app = (MilinkApplication) this.getActivity()
				.getApplication();

		mView = inflater.inflate(R.layout.ac_runnow, null);
		actionBar = new ActionbarSettings(mView, new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 历史记录
				startActivity(new Intent(getActivity(),
						GpsHistoryActivity.class));
			}
		}, interf);
		actionBar.setTopRightIcon(R.drawable.histroy);
		return mView;
	}

	@Override
	public void onGetOfflineMapState(int arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	public Bitmap getCroppedBitmap(String per) {

		Matrix m = new Matrix();
		m.postScale(0.8f, 0.8f);

		Bitmap src = BitmapFactory.decodeResource(getResources(),
				R.drawable.border_date);
		int w = src.getWidth();
		int h = src.getWidth();
		Bitmap output = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG));
		final Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setTextSize(getResources().getDimension(R.dimen.text_large_s));
		paint.setColor(activity.getCurrentColor());
		paint.setTextAlign(Align.CENTER);
		// paint.setFakeBoldText(true);
		canvas.drawBitmap(src, 0, 0, paint);
		canvas.drawText(per + "", w * 0.5f, h * 0.75f, paint);

		return Bitmap.createBitmap(output, 0, 0, output.getWidth(),
				output.getHeight(), m, true);
	}

	private ImageView date_img;

	private void initViews(View v) {
		v.findViewById(R.id.border_top).setBackgroundColor(
				activity.getCurrentColor());
		distanceCount = (TextView) v.findViewById(R.id.distance_count);
		Typeface tf = Typeface.createFromAsset(getActivity().getAssets(),
				"font/AvenirLTStd-Light.otf");
		distanceCount.setTypeface(tf);
		kcalCount = (TextView) v.findViewById(R.id.kcal);
		durationCount = (TextView) v.findViewById(R.id.duration);
		timesCount = (TextView) v.findViewById(R.id.times);
		btnVoice = (ImageView) v.findViewById(R.id.voice_ctrl);
		btnLocation = (ImageView) v.findViewById(R.id.location_ctrl);
		signl = (ImageView) v.findViewById(R.id.signl_ctrl);
		date_img = (ImageView) v.findViewById(R.id.date_img);
		// signl.setVisibility(View.INVISIBLE);
		btnStart = (Button) v.findViewById(R.id.start);
		btnVoice.setOnClickListener(this);
		btnStart.setOnClickListener(this);
		btnStart.setBackgroundColor(activity.getCurrentColor());
		btnLocation.setOnClickListener(this);

	}

	@Override
	public void onResume() {
		bResumed = true;

		actionBar.setTitle(getResources().getStringArray(R.array.menu)[2]);
		boolean is = GpsSportActivity.isOPen(getActivity());
		if (!is) {
			Context con = Util.getThemeContext(getActivity());
			AlertDialog.Builder ab = new AlertDialog.Builder(con);
			ab.setTitle(R.string.warning);
			ab.setMessage(R.string.gps_location);
			ab.setNegativeButton(R.string.can,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							dialog.dismiss();
						}
					});
			ab.setPositiveButton(R.string.ok,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							setGPS(getActivity());
						}
					});
			ab.create().show();
		}
		isCentering = false;

		MKOfflineMap mOffline = new MKOfflineMap();

		mMapView = (MapView) mView.findViewById(R.id.bmapsView);
		mMapView.setVisibility(View.VISIBLE);

		mOffline.init(null);

		// runtype = (TextView) findViewById(R.id.btn_runtype_funrun);
		// voicetype = (TextView) findViewById(R.id.btn_voice_funrun);
		zoomlevel = 19;
		initViews(mView);
		
		mMapView.onResume();
		mBaiduMap = mMapView.getMap(); 
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(zoomlevel));
		 
		mMapView.showZoomControls(false);
		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true); 
		mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
				LocationMode.FOLLOWING, true, null));
		mBaiduMap.setOnMapLoadedCallback(new OnMapLoadedCallback() {

			@Override
			public void onMapLoaded() {
				// TODO Auto-generated method stub
				myOverlayManager = new RunOverManager(mBaiduMap); 
			}
		});
		db = new Dbutils(getActivity());
		m_settingPre = getActivity().getSharedPreferences(
				SharePreUtils.APP_ACTION, Context.MODE_PRIVATE);

		try {
			Dbutils db = new Dbutils(getActivity());
			LatLng gg = db.getLastPoint();
			if (gg != null) {
				setCenterPoint(gg);
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		startLocationOnce();

		times = 0;
		distance = 0;
		duration = 0;
		allTrack = db.getAllGPSTrack();
		// for (GPSEntity gps : allTrack) {
		// times = allTrack.size();
		// distance += gps.distance;
		// kcal += gps.calorie;
		// duration += gps.durance;
		// }
		 
		for(GPSEntity mGPSEntity:allTrack){
			if(mGPSEntity.distance>=100){
				distance += mGPSEntity.distance;
				lastDate = mGPSEntity.time;
				duration += mGPSEntity.durance;
				break;
			}
		}
		distanceCount.setText(String.format("%1$.1f", distance / 1000.0f) + "");
		kcalCount.setText(lastDate + "");
		durationCount.setText(transform(duration / 3600) + ":"
				+ transform((duration % 3600) / 60) + ":"
				+ transform((duration % 3600) % 60));
		// timesCount.setText(times + "");
		String temp = "31";
		try {
			temp = lastDate.split("-")[2].substring(0, 2);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		date_img.setImageBitmap(getCroppedBitmap(temp));
		MobclickAgent.onResume(getActivity());
		super.onResume();

	}

	public final void setGPS(Context context) {
		Toast.makeText(context, getResources().getString(R.string.gps_turn_on),
				Toast.LENGTH_SHORT).show();
		Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		context.startActivity(myIntent);
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		bResumed = false;
		mMapView.onPause();

		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				mMapView.setVisibility(View.INVISIBLE);
			}
		}, 100);
		endLocationOnce();
		super.onPause();
	}

	String transform(int t) {
		if (t <= 9) {
			return "0" + t;
		} else {
			return t + "";
		}
	}

	private LatLng lastPoint;
	private double distance;
	private String lastDate = "0000-00-00";
	private int times, duration;
	private MiLocationServiceOnce loconceService;
	private ServiceConnection connOnce = new ServiceConnection() {
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			loconceService = ((MiLocationServiceOnce.MyBinder) arg1)
					.getService();
			loconceService.setListener(new UpdateUIListenerOnce() {

				@Override
				public void onchangeUI(int index, BDLocation locData) {
					// TODO Auto-generated method stub
					// 只移动到中心(milocationserviceonce 用于初次定位)
					if (locData.getRadius() <= 0) {
						signl.setImageResource(R.drawable.running_gps_none);
					} else if (0 < locData.getRadius()
							&& locData.getRadius() <= 20) {
						signl.setImageResource(R.drawable.running_gps_strong);
					} else if (20 < locData.getRadius()
							&& locData.getRadius() < 70) {
						signl.setImageResource(R.drawable.running_gps_general);
					} else {
						signl.setImageResource(R.drawable.running_gps_weak);
					}
					lastPoint = new LatLng(locData.getLatitude(), locData
							.getLongitude());
					setCenterPoint(lastPoint);
					MyLocationData mlocData = new MyLocationData.Builder()
							.accuracy(locData.getRadius())
							// 此处设置开发者获取到的方向信息，顺时针0-360
							.direction(100).latitude(locData.getLatitude())
							.longitude(locData.getLongitude()).build();
					mBaiduMap.setMyLocationData(mlocData);
					db = new Dbutils(getActivity());
					db.setLastPoint(lastPoint.latitude, lastPoint.longitude);
					endLocationOnce();
				}
			});

		}

		public void onServiceDisconnected(ComponentName name) {
			loconceService = null;
			MyLog.e(TAG, "dis connected...");
		}
	};
	RunOverManager myOverlayManager;
	private class RunOverManager extends OverlayManager {

		public RunOverManager(BaiduMap arg0) {
			super(arg0);
			// TODO Auto-generated constructor stub
		}

		@Override
		public boolean onMarkerClick(Marker arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onPolylineClick(Polyline arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public List<OverlayOptions> getOverlayOptions() {
			// TODO Auto-generated method stub
			return null;
		}

	}

	public void startLocationOnce() {
		synchronized (isCentering) {
			try {
				Intent intent = new Intent(getActivity(),
						MiLocationServiceOnce.class);
				getActivity().getApplicationContext().bindService(intent,
						connOnce, Context.BIND_AUTO_CREATE);
				// bindService(intent, conn, BIND_AUTO_CREATE);

			} catch (Exception e) {
				MyLog.e(TAG, "startLocation exception..." + e.getMessage());
			}
			isCentering = true;
		}

	}

	private void setCenterPoint(LatLng ll) {
		MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
		mBaiduMap.animateMapStatus(u);
	}

	public void endLocationOnce() {
		synchronized (isCentering) {
			// if(connOnce != null)
			if (isCentering) {
				try {
					getActivity().getApplicationContext().unbindService(
							connOnce);
				} catch (Exception e) {
					// TODO: handle exception
					int ii = 10;
					ii++;
				}
				isCentering = false;

			}
		}
		// connOnce = null;
		// unbindService(conn);
	}

	private boolean isVoice = true;

	public void switchVoice() {
		isVoice = !isVoice;
		if (isVoice) {
			btnVoice.setImageDrawable(getResources().getDrawable(
					R.drawable.volum01));

		} else {
			btnVoice.setImageDrawable(getResources().getDrawable(
					R.drawable.volum02));
			// Drawable nav_up = getResources().getDrawable(R.drawable.volume2);
			// nav_up.setBounds(0, 0, nav_up.getMinimumWidth(),
			// nav_up.getMinimumHeight());
			// voicetype.setCompoundDrawables(null, null, nav_up, null);
		}
		btnVoice.invalidate();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.location_ctrl:
			if (lastPoint != null) {
				setCenterPoint(lastPoint);
			}

			break;
		case R.id.voice_ctrl:
			switchVoice();
			break;
		case R.id.start:
			Intent intent = new Intent(getActivity(), GPSMoniterActivity.class);
			intent.putExtra("isVoice", isVoice);
			startActivity(intent);
			break;
		}
	}
}

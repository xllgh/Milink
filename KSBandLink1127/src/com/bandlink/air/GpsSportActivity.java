package com.bandlink.air;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.radar.RadarNearbyInfo;
import com.baidu.mapapi.radar.RadarNearbyResult;
import com.baidu.mapapi.radar.RadarNearbySearchOption;
import com.baidu.mapapi.radar.RadarSearchError;
import com.baidu.mapapi.radar.RadarSearchListener;
import com.baidu.mapapi.radar.RadarSearchManager;
import com.baidu.mapapi.radar.RadarUploadInfo;
import com.baidu.mapapi.utils.DistanceUtil;
import com.bandlink.air.club.BorderImageView;
import com.bandlink.air.gps.GPSEntity;
import com.bandlink.air.gps.GPSHistoryDetailActivity;
import com.bandlink.air.gps.GPSItem;
import com.bandlink.air.gps.GPSPointEntity;
import com.bandlink.air.gps.GPSUploadThread;
import com.bandlink.air.gps.GpsHistoryActivity;
import com.bandlink.air.gps.MiLocationService;
import com.bandlink.air.gps.MiLocationService.UpdateUIListener;
import com.bandlink.air.gps.MiLocationServiceOnce;
import com.bandlink.air.gps.MiLocationServiceOnce.UpdateUIListenerOnce;
import com.bandlink.air.gps.RadarActivity;
import com.bandlink.air.gps.SpeedNode;
import com.bandlink.air.gps.StepsNode;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.NotificationUtils;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Util;
import com.bandlink.air.util.WebViewHelper;
import com.bandlink.air.view.MultiDirectionSlidingDrawer;
import com.bandlink.air.view.VerticalSlidingView;
import com.bandlink.air.voice.MiSpeech;
import com.bandlink.air.voice.MiSpeechOneKmPool;
import com.bandlink.air.voice.MiSpeechPause;
import com.bandlink.air.voice.MiSpeechResume;
import com.bandlink.air.voice.MiSpeechStart;
import com.bandlink.air.voice.MiSpeechStop;
import com.bandlink.air.voice.MiSpeechSummary;
import com.milink.android.lovewalk.bluetooth.service.StepService;
import com.nineoldandroids.animation.ArgbEvaluator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;

public class GpsSportActivity extends FragmentActivity implements
		UpdateUIListener, UpdateUIListenerOnce, MKOfflineMapListener {
	private MapView mMapView;
	private MiLocationService locService;
	private MiLocationServiceOnce loconceService;
	private static final String TAG = "GpsSportActivity";
	boolean isVoice = false;
	boolean isRun = false;
	boolean isPause = false;
	boolean isSat = false;
	private int zoomlevel;

	private LatLng lastPoint = null;
	private SharedPreferences m_settingPre;
	private Date lastDate = null;
	private int distanceFlag = 0;
	private String user;
	private int uid;
	// private TextView runtype, voicetype;
	private ImageView vol;
	private Dbutils db;
	int[] startColor;
	int[] endColor;
	private Timer mTimer;
	float rr, gg, bb;
	private double weight;
	private BaiduMap mBaiduMap;

	public static String gps_starttime = "";

	public static final String ACTION1S = "com.milink.android.lovewalk.TIMER1S";
	public static final String ACTION_RUN_START = "com.milink.android.lovewalk.RUN_START";
	public static final String ACTION_RUN_PAUSE = "com.milink.android.lovewalk.RUN_PAUSE";
	public static final String ACTION_RUN_RESUME = "com.milink.android.lovewalk.RUN_RESUME";
	public static final String ACTION_RUN_STOP = "com.milink.android.lovewalk.RUN_STOP";

	public static final String ACTION_RUN_STEPS = "com.milink.android.lovewalk.RUN_STEPS";
	public static final String ACTION_RUN_DISTANCE = "com.milink.android.lovewalk.RUN_DISTANCE";
	public static final String ACTION_RUN_SPEED = "com.milink.android.lovewalk.RUN_SPEED";
	public int deviceType;
	public static final String EXTRA_TAB = "tab";
	public static List<SpeedNode> arraySpeed = new ArrayList<SpeedNode>();
	public static List<StepsNode> arraySteps = new ArrayList<StepsNode>();
	private VerticalSlidingView mSlideView;
	private MultiDirectionSlidingDrawer mDrawer;
	private View[] mViews = new View[2];
	private ImageView ivAlwaysFull;
	private ImageView ivAlwaysCenter;
	private ImageView gps_singnal_image;// gps 信号
	private ServiceConnection loconn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			locService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			locService = ((MiLocationService.MyBinder) service).getService();
			locService.setListener(GpsSportActivity.this);
			MyLog.e(TAG, "getServiced");
		}
	};

	private ServiceConnection connOnce = new ServiceConnection() {
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			loconceService = ((MiLocationServiceOnce.MyBinder) arg1)
					.getService();
			loconceService.setListener(GpsSportActivity.this);

		}

		public void onServiceDisconnected(ComponentName name) {
			loconceService = null;
			MyLog.e(TAG, "dis connected...");
		}
	};

	void getPersionInfo() {
		new Thread(new Runnable() {
			public void run() {
				try {

					String res = HttpUtlis
							.queryStringForGet("http://air.lovefit.com/index.php/home/Dongtai/getMyInfo/uid/"
									+ uid);
					JSONObject obj = new JSONObject(res);
					if (obj.getInt("status") == 0) {
						obj = obj.getJSONObject("content");
						userComment = "【"
								+ obj.getString("name")
								+ "】"+getResources().getString(R.string.all_km)+"："
								+ String.format("%.1f", Integer.valueOf(obj
										.getString("distance")) / 1000f) + getResources().getString(R.string.km);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
				}
			}
		}).start();
	}

	void startTimer() {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		setRepeatTasks(c.getTimeInMillis(), 1 * 1 * 1000);

		Calendar c1 = Calendar.getInstance();
		gps_starttime = c1.get(Calendar.YEAR) + "-"
				+ (c1.get(Calendar.MONTH) + 1) + "-"
				+ c1.get(Calendar.DAY_OF_MONTH) + "-"
				+ c1.get(Calendar.HOUR_OF_DAY) + "-" + c1.get(Calendar.MINUTE)
				+ "-" + "0";
	}

	private void setRepeatTasks(long triggerTime, long interval) {

		mTimer = new Timer();
		// 寤舵椂2绉�闂撮殧10鍒嗛挓
		mTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (!isPause) {
					// sendBroadcast(new Intent(ACTION1S));
					totalTime++;
					if (totalTime % 5 == 0) {
						updateView1(speed);
						speed = 0;
					}
					if (totalTime % 60 == 0) {
						if (steps60s_start == -1) {
							steps60s_start = steps_now;
						}

						updateView3();
						steps60s_start = steps_now;
					}
					if (bResumed) {
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								updateBoardTime();
							}
						});

					}

				}
			}
		}, 0 * 1000, 1 * 1000);
	}

	void cancelRepeat() {
		if (mTimer != null) {
			synchronized (mTimer) {
				mTimer.cancel();
				mTimer = null;
			}
		}
	}

	public void myzoomin(View v) {
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomIn());
	}

	public void myzoomout(View v) {
		// mMapController.zoomOut();
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomOut());
	}

	public void myCenter(View view) {
		setCenterPoint(lastPoint);

	}

	private boolean initFlag = true;
	private BroadcastReceiver milinkReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals("MilinkStep")) {
				if (isRun) {
					int device = intent.getIntExtra("device", 0);
					if (device == 1 && !initFlag) {
						int steps = intent.getIntExtra("step", 0);
						int cal = intent.getIntExtra("cal", 0);
						MyLog.d("receiveStep", steps + "");

						// MyLog.e("receiveCal", cal + "");
						if (stepsLast >= 0) {
							stepsDelta = steps - stepsLast;
							calDelta = cal - calLast;
							MyLog.d("stepDelta", stepsDelta + "");
							// MyLog.e("calLast", calLast + "");
							// MyLog.e("calDelta", calDelta + "");
							stepsLast = steps;
							calLast = cal;
							stepsReal += stepsDelta;
							MyLog.d("stepsReal", stepsReal + "");
							calReal += calDelta;
							if (!isPause) {
								updateBoardSteps();
							}

							if (steps60s_start == -1) {
								steps60s_start = steps;
							}

							steps_now = steps;
						}
					}
				}

			} else if (intent.getAction().equals("MilinkStepInit")) {
				stepsLast = intent.getIntExtra("nowstep", 0);
				calLast = intent.getIntExtra("nowcal", 0);
				steps60s_start = stepsLast;
				MyLog.d("receiveNowStep", stepsLast + "");
				initFlag = false;
				// MyLog.e("receiveNowCal", calLast + "");
			}
		}

	};

	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		setContentView(R.layout.gps);
		getPersionInfo();
		mSharePreUtils = SharePreUtils.getInstance(this);
		context = Util.getThemeContext(this);
		gps_singnal_image = (ImageView) findViewById(R.id.gps_singnal_image);
		isCentering = false;
		cancelRepeat();
		mDrawer = (MultiDirectionSlidingDrawer) findViewById(R.id.drawer);
		mSlideView = (VerticalSlidingView) findViewById(R.id.verticalsliding_view);
		mSlideView.setOnPageScrollListener(new MyPageScrollListener());
		mSlideView
				.setPageEndListener(new VerticalSlidingView.OnPageEndListener() {

					@Override
					public void onPageEnd() {
						// TODO Auto-generated method stub
						mDrawer.animateClose();
					}
				});

		MKOfflineMap mOffline = new MKOfflineMap();
		mMapView = (MapView) findViewById(R.id.bmapsView);
		mBaiduMap = mMapView.getMap();
		mOffline.init(this);
		vol = (ImageView) findViewById(R.id.volum);
		zoomlevel = 19;
		// mMapController.setZoom(zoomlevel);
		mBaiduMap.setMyLocationEnabled(true);
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(zoomlevel));
		mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
				LocationMode.NORMAL, true, null));
		mMapView.showZoomControls(false);
		double cLat = 39.945;
		double cLon = 116.404;
		LatLng p = new LatLng(cLat, cLon);
		setCenterPoint(p);

		arraySpeed.clear();
		arraySteps.clear();
		isAlwaysFull = true;
		isAlwaysCenter = true;

		m_settingPre = getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		user = m_settingPre.getString("USERNAME", "lovefit");
		uid = m_settingPre.getInt("UID", -1);

		db = new Dbutils(uid, this);
		Object[] profile = db.getUserProfile();
		if (profile == null) {
			weight = 60.0;
		} else {
			weight = (Double) profile[6];
			if (weight < 30) {
				weight = 60.0;
			}
		}

		deviceType = db.getUserDeivceType();
		// Editor edit = m_settingPre.edit();
		// edit.putInt("activity_flag", 1);
		// edit.commit();
		// int runflag = m_settingPre.getInt("runflag", -1);
		// MyLog.e(TAG, "runflag : " + runflag);
		startColor = new int[] { 255, 0, 0 };
		endColor = new int[] { 0, 255, 0 };
		rr = startColor[0] - endColor[0];
		gg = startColor[1] - endColor[1];
		bb = startColor[2] - endColor[2];
		registerReceiver();
		Intent intents = new Intent(this, StepService.class);
		startService(intents);
		initViews();
		createPhoneListener();
		initStepChart();
		initSpeedChart();
		Bundle bundle = getIntent().getExtras();
		isVoice = bundle.getBoolean("isVoice", true);
		ivAlwaysFull = (ImageView) findViewById(R.id.iv_always_full);
		ivAlwaysCenter = (ImageView) findViewById(R.id.iv_always_center);
		isAlwaysFull = true;
		ivAlwaysFull.setImageResource(R.drawable.map_always_full_pressed);
		isAlwaysCenter = true;
		ivAlwaysCenter.setImageResource(R.drawable.map_always_center_pressed);
		radarborder = (LinearLayout) findViewById(R.id.radarborder);
		radarborder.setVisibility(View.GONE);
		switchStart();

		super.onCreate(savedInstanceState);
	}

	PopupWindow pop;
	TextView ride, run, walk;

	final double nwLat_max = -90;
	final double nwLng_max = 180;
	final double seLat_max = 90;
	final double seLng_max = -180;

	double nwLat = nwLat_max;
	double nwLng = nwLng_max;
	double seLat = seLat_max;
	double seLng = seLng_max;

	void initSpan() {
		nwLat = -90;
		nwLng = 180;
		seLat = 90;
		seLng = -180;
	}

	boolean isAlwaysFull = false;
	boolean isAlwaysCenter = true;
	// 周边雷达相关
	RadarNearbyResult listResult = null;
	ListView mResultListView = null;
	// RadarResultListAdapter mResultListAdapter = null;
	private String userID = "";
	private String userComment = "";
	private boolean uploadAuto = false;
	LinearLayout radarborder;

	private void getRadar(LatLng point) {
		if (point == null) {
			return;
		}
		// 周边雷达设置监听
		RadarSearchManager.getInstance().addNearbyInfoListener(
				new RadarSearchListener() {

					@Override
					public void onGetUploadState(RadarSearchError error) {
						// TODO Auto-generated method stub
						System.out.println(error);
					}

					@Override
					public void onGetNearbyInfoList(RadarNearbyResult arg0,
							RadarSearchError error) {
						// TODO Auto-generated method stub
						ArrayList<String> uids = new ArrayList<String>();
						if (error == RadarSearchError.RADAR_NO_ERROR) {
							for (RadarNearbyInfo info : arg0.infoList) {
								String uid = info.userID;
								if (uid.contains(getPackageName())) {
									uids.add(uid.replace(getPackageName(), ""));
								}
								if (uids.size() == 2) {
									break;
								}

							}
							if (uids.size() < 1) {

								BorderImageView imageview = (BorderImageView) radarborder
										.findViewById(R.id.lbs1);
								BorderImageView imageview2 = (BorderImageView) radarborder
										.findViewById(R.id.lbs2);
								imageview2.setVisibility(View.GONE);
								imageview.setImageResource(R.drawable.avatar);
							} else {
								if (uids.size() == 1) {
									BorderImageView imageview = (BorderImageView) radarborder
											.findViewById(R.id.lbs1);
									BorderImageView imageview2 = (BorderImageView) radarborder
											.findViewById(R.id.lbs2);
									imageview2.setVisibility(View.GONE);
									ImageLoader.getInstance().displayImage(
											HttpUtlis.AVATAR_URL
													+ HttpUtlis
															.getAvatarUrl(uids
																	.get(0)),
											imageview,
											MilinkApplication.getListOptions());

								} else {
									BorderImageView imageview = (BorderImageView) radarborder
											.findViewById(R.id.lbs1);
									BorderImageView imageview2 = (BorderImageView) radarborder
											.findViewById(R.id.lbs2);

									ImageLoader.getInstance().displayImage(
											HttpUtlis.AVATAR_URL
													+ HttpUtlis
															.getAvatarUrl(uids
																	.get(0)),
											imageview,
											MilinkApplication.getListOptions());
									ImageLoader.getInstance().displayImage(
											HttpUtlis.AVATAR_URL
													+ HttpUtlis
															.getAvatarUrl(uids
																	.get(1)),
											imageview2,
											MilinkApplication.getListOptions());
								}

							}
						} else {
							Toast.makeText(getApplicationContext(),
									"查找失败" + error.toString(),
									Toast.LENGTH_LONG).show();
						}
						RadarSearchManager.getInstance()
								.removeNearbyInfoListener(this);

					}

					@Override
					public void onGetClearInfoState(RadarSearchError error) {
						// TODO Auto-generated method stub

					}
				});
		// 周边雷达设置用户，id为空默认是设备标识
		RadarSearchManager.getInstance().setUserID(
				SharePreUtils.getInstance(this).getUid() + getPackageName());

		RadarNearbySearchOption option = new RadarNearbySearchOption()
				.centerPt(point).pageNum(0).radius(20 * 1000);
		RadarSearchManager.getInstance().nearbyInfoRequest(option);
	}

	SharePreUtils mSharePreUtils;

	public void radarClick(View view) {
		if (mSharePreUtils.getApp().getBoolean("sharelocation", false)) {
			if (lastPoint == null) {
				Toast.makeText(getApplicationContext(), "定位失败，请稍等",
						Toast.LENGTH_LONG).show();
				return;
			}
			// 周边雷达设置用户，id为空默认是设备标识
			RadarSearchManager.getInstance().setUserID(
					SharePreUtils.getInstance(GpsSportActivity.this).getUid()
							+ getPackageName());
			RadarUploadInfo info = new RadarUploadInfo();
			info.comments = userComment;
			info.pt = lastPoint;
			RadarSearchManager.getInstance().uploadInfoRequest(info);
			Intent i = new Intent(GpsSportActivity.this, RadarActivity.class);
			i.putExtra("la", lastPoint.latitude);
			i.putExtra("lo", lastPoint.longitude);
			startActivity(i);
			return;
		}
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle(R.string.warning);
		ab.setMessage(R.string.warn);
		ab.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						// 周边雷达设置用户，id为空默认是设备标识
						mSharePreUtils.getAppEditor()
								.putBoolean("sharelocation", true).commit();
						RadarSearchManager.getInstance().setUserID(
								SharePreUtils
										.getInstance(GpsSportActivity.this)
										.getUid()
										+ getPackageName());
						RadarUploadInfo info = new RadarUploadInfo();
						info.comments = userComment;
						info.pt = lastPoint;
						RadarSearchManager.getInstance()
								.uploadInfoRequest(info);
						Intent i = new Intent(GpsSportActivity.this,
								RadarActivity.class);
						startActivity(i);
					}
				});
		ab.create().show();
	}

	private synchronized void setRoute(BDLocation locData) {
		// MyLog.v("ppp", "setRoute: ");
		LatLng curPoint = new LatLng(locData.getLatitude(),
				locData.getLongitude());
		setCenterPoint(curPoint);

		nwLat = Math.max(nwLat, curPoint.latitude);
		nwLng = Math.min(nwLng, curPoint.longitude);
		seLat = Math.min(seLat, curPoint.latitude);
		seLng = Math.max(seLng, curPoint.longitude);

		if (points.size() <= 1) {

			return;
		}

		if (isAlwaysCenter) {
			setCenterPoint(lastPoint);
		}

		if (isAlwaysFull) {
			fitSpan();
		}
		// mBaiduMap.setMyLocationConfigeration(arg0)

	}

	private void setStartPoint(LatLng _gp) {
		// 起点
		drawPoint(_gp, R.drawable.icon_st);
	}

	void drawPoint(LatLng _gp, int resId) {

		MarkerOptions ooPolyline = new MarkerOptions().zIndex(6)
				.icon(BitmapDescriptorFactory.fromResource(resId))
				.position(_gp);
		mBaiduMap.addOverlay(ooPolyline);
	}

	ArgbEvaluator argbE;

	public void switchVoice(View view) {
		isVoice = !isVoice;
		// MyLog.v("voice", isVoice + "");
		if (isVoice) {
			vol.setImageDrawable(getResources().getDrawable(R.drawable.volum01));

		} else {
			vol.setImageDrawable(getResources().getDrawable(R.drawable.volum02));
		}
		vol.invalidate();
	}

	public void my_alwaysfull(View v) {
		switchAlwaysFull();
	}

	public void my_alwayscenter(View v) {
		switchAlwaysCenter();
	}

	public void animateCenter(View view) {
		setCenterPoint(lastPoint);
	}

	boolean isSa = false;

	public void changeMaptype(View view) {
		if (isSa) {
			isSa = false;
			mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
		} else {
			mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
			isSa = true;
		}
	}

	void switchAlwaysFull() {
		isAlwaysFull = !isAlwaysFull;
		if (isAlwaysFull) {
			ivAlwaysFull.setImageResource(R.drawable.map_always_full_pressed);
		} else {
			ivAlwaysFull.setImageResource(R.drawable.map_always_full_normal);
		}
	}

	void switchAlwaysCenter() {
		isAlwaysCenter = !isAlwaysCenter;
		if (isAlwaysCenter) {
			ivAlwaysCenter
					.setImageResource(R.drawable.map_always_center_pressed);
		} else {
			ivAlwaysCenter
					.setImageResource(R.drawable.map_always_center_normal);
		}
	}

	private void cancleNotificaction() {
		NotificationManager manager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.cancel(MiLocationService.LocNotification);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// RadarSearchManager.getInstance().clearUserInfo();
		RadarSearchManager.getInstance().destroy();
		try {
			if (isRun)
				stopRun();

			// if (ss != null) {
			// ss.stop();
			// }
			SlideMainActivity.bCenter = true;
			// Editor edit = m_settingPre.edit();
			// edit.putInt("activity_flag", 0);
			// edit.commit();
			cancleNotificaction();
			if (milinkReceiver != null)
				unregisterReceiver(milinkReceiver);

			GpsSportActivity.this.finish();
		} catch (Exception e) {
			// TODO: handle exception
			GpsSportActivity.this.finish();
			e.printStackTrace();
		}
		if (mMapView != null) {
			mMapView.onDestroy();
		}

	}

	boolean bResumed = false;

	@Override
	protected void onResume() {
		bResumed = true;
		// 鍦╝ctivity鎵цonResume鏃舵墽琛宮MapView. onResume ()锛屽疄鐜板湴鍥剧敓鍛藉懆鏈熺鐞�

		mMapView.onResume();
		mMapView.setVisibility(View.VISIBLE);
		if (SlideMainActivity.bCenter) {
			SlideMainActivity.bCenter = false;
			if (!isRun) {
				m_settingPre = getSharedPreferences(SharePreUtils.APP_ACTION,
						Context.MODE_PRIVATE);
				double last_longitude = 116.200000, last_latitude = 39.500000;
				try {
					Dbutils db = new Dbutils(GpsSportActivity.this);
					LatLng gg = db.getLastPoint();
					if (gg != null) {
						setCenterPoint(gg);
					}

				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}

				startLocationOnce();
			}
		}
		if (mDrawer.isOpened() && isRun) {
			if (verticalPage == 0) {
				updateBoardSteps();
			} else if (verticalPage == 1 && lststeplistcount != steplistcount) {
				buildStepChart();
				lststeplistcount = steplistcount;
			}
		}

		MobclickAgent.onResume(this);
		super.onResume();
	}

	public void startLocationOnce() {
		synchronized (isCentering) {
			try {
				Intent intent = new Intent(GpsSportActivity.this,
						MiLocationServiceOnce.class);
				this.getApplicationContext().bindService(intent, connOnce,
						BIND_AUTO_CREATE);
				// bindService(intent, conn, BIND_AUTO_CREATE);
				MyLog.d("gpsloconce", "====start====");
			} catch (Exception e) {
				// MyLog.e(TAG, "startLocation exception..." + e.getMessage());
			}
			isCentering = true;
		}

	}

	@Override
	protected void onPause() {
		bResumed = false;
		MobclickAgent.onPause(this);
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

	boolean isStart = false;
	private LinkedList<GPSItem> points = new LinkedList<GPSItem>();
	// 标记当前GPS设置
	int cur = 0;

	@Override
	public void onchangeUI(int index, BDLocation locData) {
		// TODO Auto-generated method stub
		if (index == -1) {
			if (cur != 10) {
				gps_singnal_image.setImageResource(R.anim.gps_signal);
				AnimationDrawable aa = (AnimationDrawable) gps_singnal_image
						.getDrawable();
				aa.start();
				cur = 10;
			}
		} else if (!isPause && locData != null) {
			if (locData.hasRadius()) {
				float r = locData.getRadius();
				if (r > 100) {
					if (cur != 0) {
						gps_singnal_image
								.setImageResource(R.drawable.gps_none_dr);
						cur = 0;
					}
				} else if (r > 50) {
					if (cur != 1) {
						gps_singnal_image
								.setImageResource(R.drawable.gps_poor_dr);
						cur = 1;
					}
				} else if (r > 35) {
					if (cur != 2) {
						gps_singnal_image
								.setImageResource(R.drawable.gps_fair_dr);
						cur = 2;
					}

				} else {
					if (cur != 3) {
						gps_singnal_image
								.setImageResource(R.drawable.gps_good_dr);
						cur = 3;
					}

				}
			} else {
				if (cur != 4) {
					gps_singnal_image.setImageResource(R.drawable.gps_none_dr);
					cur = 4;
				}

			}
		}
		if (locData.getLatitude() > 0 && locData.getLongitude() > 0) {
			LatLng tmpPt = null;
			Date d2 = new Date();
			float deltaSeconds = 0;
			float ss = (float) 0.0;
			Editor editor = m_settingPre.edit();
			switch (index) {
			case -2:
				lastPoint = new LatLng(locData.getLatitude(),
						locData.getLongitude());
				// MyLog.e("GPSONCE", lastPoint.getLatitudeE6() + "====="
				// + lastPoint.getLongitudeE6());
				tmpPt = new LatLng(locData.getLatitude(),
						locData.getLongitude());
				// setCenterPoint(lastPoint);
				// mMapController.animateTo(lastPoint);
				db = new Dbutils(GpsSportActivity.this);
				db.setLastPoint(lastPoint.latitude, lastPoint.longitude);
				endLocationOnce();

				break;
			case -1:
				lastPoint = new LatLng(locData.getLatitude(),
						locData.getLongitude());
				lastDate = new Date();
				setCenterPoint(lastPoint);
				deltaDistance = 111.2345678;
				radarborder.setVisibility(View.GONE);
				break;
			case 0:
				// 定位开始
				if (!isStart) {
					startTimer();
					isStart = true;
				}
				// cancleNotificaction();
				tmpPt = new LatLng(locData.getLatitude(),
						locData.getLongitude());
				lastDate = d2;
				lastPoint = tmpPt;
				radarborder.setVisibility(View.GONE);
				if (mSharePreUtils.getApp().getBoolean("sharelocation", false)) {
					// 周边雷达设置用户，id为空默认是设备标识
					RadarSearchManager.getInstance().setUserID(
							SharePreUtils.getInstance(GpsSportActivity.this)
									.getUid() + getPackageName());
					RadarUploadInfo info = new RadarUploadInfo();
					info.comments = userComment;
					info.pt = lastPoint;
					RadarSearchManager.getInstance().uploadInfoRequest(info);
					// getRadar(tmpPt);
				} else {
					BorderImageView imageview = new BorderImageView(
							GpsSportActivity.this, 2, Color.GRAY, 55);
					imageview.setImageResource(R.drawable.btn_add_pic_normal);
					radarborder.addView(imageview);
				}
				distance = 0;
				setCenterPoint(lastPoint);
				// editor.putInt("runflag", 0);
				// editor.putInt("run_disdance", 0);
				// editor.commit();
				setStartPoint(lastPoint);
				points.add(new GPSItem(locData.getSpeed(), lastPoint, locData
						.getRadius()));
				break;
			case 1:

				// cancleNotificaction();
				tmpPt = new LatLng(locData.getLatitude(),
						locData.getLongitude());
				// deltaDistance = DistanceUtil.getDistance(lastPoint, tmpPt);
				deltaDistance = DistanceUtil.getDistance(lastPoint, tmpPt);
				deltaSeconds = ((d2.getTime() - lastDate.getTime()) / 1000);
				speed = locData.getSpeed();
				// Intent ii = new Intent(ACTION_RUN_SPEED);
				// ii.putExtra("speed", speed);
				// sendBroadcast(ii);
				points.add(new GPSItem(locData.getSpeed(), tmpPt, locData
						.getRadius()));
				if (points.size() == 4) {
					drawLine(locData.getDirection());
				}
				// if (BuildConfig.DEBUG) {
				// Toast.makeText(getApplicationContext(),
				// locData.latitude + "----" + locData.longitude, 100)
				// .show();
				// }
				// setRoute(locData);
				distance += deltaDistance;

				// ii = new Intent(ACTION_RUN_DISTANCE);
				// ii.putExtra("distance", distance);
				// sendBroadcast(ii);
				updateBoardDistance();
				if (distanceFlag != (int) (distance / 1000)) {
					playVoice(soundtype_onekm);
					distanceFlag = (int) (distance / 1000);
					// MyLog.v(TAG, "voicenow: " + distance + "/ " +
					// distanceFlag);
				}
				// mTextView.setText("GPS淇″彿鑹ソ "
				// + String.format("%1$.2f", locData.speed));

				// editor.putInt("run_disdance", (int) distance);
				// editor.commit();
				lastPoint = tmpPt;
				lastDate = new Date();

				break;

			default:
				// mTextView.setText("GPS淇″彿涓嶈壇锛岃鎮ㄧЩ鍔ㄥ埌寮�様鍦颁互鑾峰彇鏇村噯纭殑GPS浣嶇疆淇℃伅");
				// showNotificaction();
				break;
			}
			// MyLog.e(TAG, "onchangeUI message...");
		}

	}

	ArrayList<LatLng> temp = new ArrayList<>();;

	void drawLine(float d) {

		LinkedList<GPSItem> point = new LinkedList<GPSItem>();
		point.addAll(points);
		temp.clear();
		for (GPSItem po : point) {
			temp.add(po.getGp());
		}
		points.clear();
		points.push(point.getLast());
		OverlayOptions ooPolyline = new PolylineOptions().width(6)
				.color(0xff5f93ef).points(temp).dottedLine(false).zIndex(5);

		mBaiduMap.addOverlay(ooPolyline);

		if (isAlwaysCenter & (!isAlwaysFull)) {
			setCenterPoint(lastPoint);
		}
		nwLat = Math.max(nwLat, lastPoint.latitude);
		nwLng = Math.min(nwLng, lastPoint.longitude);
		seLat = Math.min(seLat, lastPoint.latitude);
		seLng = Math.max(seLng, lastPoint.longitude);
		if (isAlwaysFull) {
			fitSpan();
		}

		MyLocationData mlocData = new MyLocationData.Builder()
				.accuracy(point.getLast().getRadius())
				// 此处设置开发者获取到的方向信息，顺时针0-360
				.direction(d).latitude(point.getLast().getGp().latitude)
				.longitude(point.getLast().getGp().longitude).build();
		mBaiduMap.setMyLocationData(mlocData);

	}

	public static int stepsReal;
	public static int calReal;
	int stepsLast, stepsDelta;
	int calLast, calDelta;
	public static int totalTime;
	private int totalTime_lastonekm;
	public static double distance = 0;
	private double deltaDistance = 0;
	private double speed = 0;

	int steps_now = 0;
	int steps60s_start = 0;

	void startRun() {
		initFlag = true;
		boolean is = GpsSportActivity.isOPen(GpsSportActivity.this);
		if (!is) {
			Context con = GpsSportActivity.this;
			if (Build.VERSION.SDK_INT >= 11) {
				con = new ContextThemeWrapper(con,
						android.R.style.Theme_Holo_Light);
			}
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
							setGPS(GpsSportActivity.this);
						}
					});
			ab.create().show();
		}

		playVoice(soundtype_start);

		initSpan();

		isRun = true;
		stepsReal = 0;
		calReal = 0;
		distance = 0;
		speed = 0;
		gps_starttime = "";
		points.clear();
		// GPSFragment1.first = true;
		// GPSFragment3.first = true;
		// new Handler().postDelayed(new Runnable() {
		//
		// @Override
		// public void run() {
		// // TODO Auto-generated method stub
		// initSpeedChart();
		// initStepChart();
		//
		// }
		// }, 1000);

		stepfirst = true;
		speedfirst = true;
		arraySpeed.clear();
		arraySteps.clear();

		startLocation();
		totalTime = 0;
		totalTime_lastonekm = 0;
		// sendBroadcast(new Intent(ACTION_RUN_START));
		updateBoardTime();
		updateBoardSteps();
		updateBoardDistance();
		// TODO Auto-generated method stub
		Intent intent = new Intent("MilinkConfig");
		intent.putExtra("command", 3);
		intent.putExtra("step", 0);
		intent.putExtra("calorie", 0);
		intent.putExtra("weight", weight);
		intent.putExtra("step_length", 70);
		// username
		intent.putExtra("username", "user");
		sendBroadcast(intent);

	}

	void stopRun() {
		// gpsStart.setText(R.string.gpsstart);
		initFlag = true;
		if (deviceType != 2) {
			Intent intent = new Intent("MilinkConfig");
			intent.putExtra("command", 2);
			sendBroadcast(intent);
			NotificationManager manager = (NotificationManager) getApplicationContext()
					.getSystemService(Context.NOTIFICATION_SERVICE);
			manager.cancel(NotificationUtils.GPSOrStepNotfication);
		}
		cancelRepeat();
		// graphicsOverlay = null;
		isRun = false;
		isPause = false;
		// gpsPause.setText(R.string.pause);
		/* 缁堟鐢昏矾绾跨殑鏈哄埗 */
		// clearMyLoc();
		// MainUIActivity.isGpsRunning = isRun;
		/* 鐢荤粓鐐� */
		setEndPoint();

		sendBroadcast(new Intent(ACTION_RUN_STOP));

		GPSEntity g = new GPSEntity();
		g.time = "";
		g.steps = stepsReal;
		g.calorie = calReal / 10;
		if (g.calorie < 1) {
			g.calorie = ((distance * 100f / 65f) / 100) * 3;
		}
		g.distance = distance;
		g.durance = (int) totalTime;
		g.points = locService.points;
		db = new Dbutils(this);
		db.updateGPSTrack(locService.m_runId, g);
		db.insertGPSUpLoad(locService.m_runId, 0);

		// gpsHint.setText(R.string.start_sport);
		fitSpan();

		if (!gps_starttime.equals("")) {
			updateView1(speed);
			updateView3();
		}

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				playVoice(soundtype_stop);
				try {
					Thread.sleep(600);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				playVoice(soundtype_summary);
			}
		}).start();

		// TODO Auto-generated method stub
		// if (m_settingPre.getInt("ISMEMBER", 0) == 1) {

		// new Handler().postDelayed(new Runnable() {
		//
		// @Override
		// public void run() {
		// // TODO Auto-generated method stub
		// mMapView.getCurrentMap();
		// }
		// }, 300);

		if (distance > 100) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					// mMapView.getCurrentMap();
					try {
						ArrayList<GPSEntity> Tlist = db
								.getGPSTrackCursorById(locService.m_runId);
						ArrayList<GPSPointEntity> Plist = db
								.getGPSPointCursor(locService.m_runId);
						GPSUploadThread gpsup = GPSUploadThread.getInstance();
						gpsup.setPostEntity(
								m_settingPre.getString("session_id", ""),
								GpsSportActivity.this, Tlist, Plist);
						gpsup.Write2DatUpload(locService.m_runId);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();
		}
		distance = 0;
		distanceFlag = 0;
		speed = 0;
		Cursor cursor = db.getGPSPointCursor2(locService.m_runId);
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				Intent intent = new Intent(GpsSportActivity.this,
						GPSHistoryDetailActivity.class);
				intent.putExtra("iid", locService.m_runId);
				startActivity(intent);
				GpsSportActivity.this.finish();

			} else {
				Intent intent = new Intent(GpsSportActivity.this,
						GPSHistoryDetailActivity.class);
				intent.putExtra("iid", locService.m_runId);
				startActivity(intent);
				GpsSportActivity.this.finish();
				// GpsSportActivity.this.finish();
			}
		}

		endLocation();
	}

	void registerReceiver() {
		try {
			IntentFilter filter_dynamic = new IntentFilter();
			filter_dynamic.addAction("MilinkStep");
			filter_dynamic.addAction("MilinkStepInit");
			registerReceiver(milinkReceiver, filter_dynamic);
		} catch (Exception e) {
			MyLog.d("main", e.toString());
		}
	}

	private void initBoard() {
		steps_show = 0;
		duration_show = 0;
		cal_show = 0;
		distance_show = 0;
		gpsCal.setText("0");
		gpsStep.setText("0");
		gpsDis.setText("0");
		gpsClock.setText("00:00");
	}

	void updateBoardTime() {
		if (mDrawer.isOpened() && bResumed) {
			GpsSportActivity.duration_show = (int) totalTime;
			Integer seconds = (int) (totalTime % 60);
			Integer minutes = (int) ((totalTime / 60) % 60);
			Integer hours = (int) (totalTime / 3600);
			String duration = String.format("%1$02d:%2$02d:%3$02d", hours,
					minutes, seconds);
			gpsClock.setText(duration);
		}
	}

	void updateBoardSteps() {
		if (mDrawer.isOpened() && bResumed) {
			GpsSportActivity.steps_show = GpsSportActivity.stepsReal;
			GpsSportActivity.cal_show = (int) Math
					.round(GpsSportActivity.calReal / 10);
			gpsStep.setText("" + GpsSportActivity.steps_show);
			gpsCal.setText("" + GpsSportActivity.cal_show);
			Log.e("updateboardCal", GpsSportActivity.cal_show + "");
			Log.e("updateboardStep", GpsSportActivity.steps_show + "");
		}
	}

	void updateBoardDistance() {
		if (mDrawer.isOpened() && bResumed) {
			GpsSportActivity.distance_show = (int) GpsSportActivity.distance;
			String strdistance = String.format("%1$.1f",
					GpsSportActivity.distance);
			gpsCal.setText(String.format("%.1f",
					((distance_show * 100f / 65f) / 100) * 3));
			gpsDis.setText(strdistance);
		}
	}

	void addspeed(Double ss1) {
		arraySpeed.add(new SpeedNode(ss1, new Date()));
	}

	void updateView1(double ss) {
		addspeed(ss);
		if (mDrawer.isOpened() && bResumed && verticalPage == 1)
			buildSpeedChart();
	}

	void addsteps(int ss1) {
		arraySteps.add(new StepsNode(ss1, new Date()));
		steplistcount = arraySteps.size();
	}

	double ddtemp = 0;

	void updateView3() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				addsteps((int) Math.round(distance - ddtemp));
				ddtemp = distance;
				if (mDrawer.isOpened() && bResumed && verticalPage == 1) {
					buildStepChart();
				} else if (totalTime % 300 == 0) {
					buildStepChart();
					buildSpeedChart();
					updateBoardSteps();
				}
			}
		});

	}

	public void initSpeedChart() {

		helpspeed.loadUrls(new String[] { "showSpeedChartSingle('[]','"
				+ getString(R.string.speed) + "','')" });
	}

	public void buildSpeedChart() {
		int count = GpsSportActivity.arraySpeed.size();
		JSONArray yValues = new JSONArray();
		for (int i = 0; i < count; i++)
			yValues.put(GpsSportActivity.arraySpeed.get(i).speed);

		helpspeed.loadUrls(new String[] { "updateSpeedChart('"
				+ yValues.toString() + "','" + getString(R.string.speed)
				+ "','" + GpsSportActivity.gps_starttime + "'," + speedfirst
				+ ")" });
		if (yValues.length() > 1) {
			speedfirst = false;
		}
	}

	public void buildStepChart() {
		int count = GpsSportActivity.arraySteps.size();
		JSONArray xValues = new JSONArray();
		JSONArray yValues = new JSONArray();
		for (int i = 0; i < GpsSportActivity.arraySteps.size(); i++) {// 鑷畾涔墄杞存爣绛�
			String aa;
			Date dd = GpsSportActivity.arraySteps.get(i).date;
			aa = (new SimpleDateFormat("HH:mm")).format(dd);
			xValues.put(aa);
		}

		for (int i = 0; i < count; i++)
			yValues.put(GpsSportActivity.arraySteps.get(i).steps);

		helpstep.loadUrls(new String[] { "updateStepChart('"
				+ yValues.toString() + "','" + getString(R.string.step_mintue)
				+ "','" + GpsSportActivity.gps_starttime + "'," + stepfirst
				+ ")" });
		if (yValues.length() > 1) {
			stepfirst = false;
		}

	}

	public void initStepChart() {
		helpstep.loadUrls(new String[] { "showStepChartSingle('[]','"
				+ getString(R.string.step_mintue) + "','')" });
	}

	public void switchPause() {
		if (isRun) {
			if (isPause) {
				gps_start.setVisibility(View.GONE);
				gps_pause.setVisibility(View.VISIBLE);
				gps_stop.setVisibility(View.GONE);
				playVoice(soundtype_resume);
			} else {
				gps_start.setVisibility(View.VISIBLE);
				gps_pause.setVisibility(View.GONE);
				gps_stop.setVisibility(View.VISIBLE);
				playVoice(soundtype_pause);
			}
			isPause = !isPause;
		}
	}

	public static final boolean isOPen(final Context context) {
		LocationManager locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		boolean gps = locationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);

		return gps;
	}

	public final void setGPS(Context context) {
		Toast.makeText(context, getResources().getString(R.string.gps_turn_on),
				Toast.LENGTH_SHORT).show();
		Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		context.startActivity(myIntent);
	}

	public void startLocation() {
		try {
			Intent intent = new Intent(GpsSportActivity.this,
					MiLocationService.class);
			this.getApplicationContext().bindService(intent, loconn,
					BIND_AUTO_CREATE);
			MyLog.d("gpsstartloc", "========");
		} catch (Exception e) {
			MyLog.e(TAG, "startLocation exception..." + e.getMessage());
		}
	}

	public void endLocation() {
		this.getApplicationContext().unbindService(loconn);
		MyLog.d("gpsendloc", "========");
		// unbindService(conn);
	}

	/* 璁惧畾缁堢偣鐨刴ethod */
	private void setEndPoint() {
		// cancleNotificaction();
		// clearMyLoc();
		Editor edit = m_settingPre.edit();
		edit.putInt("runflag", 1);
		// edit.putInt("runid", m_runId);
		edit.commit();
		// lastPoint = new LatLng(32045500, 118826000);
		if (lastPoint == null)
			return;
		drawPoint(lastPoint, R.drawable.icon_en);

	}

	// private void cancleNotificaction() {
	// NotificationManager manager = (NotificationManager) this
	// .getSystemService(Context.NOTIFICATION_SERVICE);
	// manager.cancel(MiLocationService.LocNotification);
	// }

	// double a = 0.00001;

	private void setCenterPoint(LatLng pos) {
		// clearMyLoc();
		MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(pos);
		mBaiduMap.animateMapStatus(u);
	}

	Boolean isCentering = false;

	public void endLocationOnce() {
		synchronized (isCentering) {
			// if(connOnce != null)
			if (isCentering) {
				try {
					this.getApplicationContext().unbindService(connOnce);
				} catch (Exception e) {
					// TODO: handle exception
					int ii = 10;
					ii++;
				}
				isCentering = false;
				MyLog.d("gpsloconce", "====end====");
			}
		}
		// connOnce = null;
		// unbindService(conn);
	}

	public void switchStart() {
		if (!isRun) {
			gps_start.setVisibility(View.GONE);
			gps_pause.setVisibility(View.VISIBLE);
			gps_stop.setVisibility(View.GONE);
			startRun();
		} else {
			AlertDialog.Builder ab = new AlertDialog.Builder(context);
			ab.setTitle(R.string.warning);
			ab.setMessage(distance <= 100 ? R.string.gps_less_dis
					: R.string.gps_finish);
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
							dialog.dismiss();
							stopRun();
						}
					});
			ab.create().show();
		}
		MyLog.v("isRun", isRun + "");
	}

	void fitSpan() {
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

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub

		if (keyCode == KeyEvent.KEYCODE_BACK) {

			// TODO Auto-generated method stub
			if (isRun) {
				Context con = GpsSportActivity.this;
				if (Build.VERSION.SDK_INT >= 11) {
					con = new ContextThemeWrapper(con,
							android.R.style.Theme_Holo_Light);
				}
				AlertDialog.Builder ab = new AlertDialog.Builder(con);
				ab.setTitle(R.string.warning);
				ab.setMessage(R.string.gps_finish);
				ab.setNegativeButton(R.string.can,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								dialog.dismiss();
							}
						});
				ab.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								dialog.dismiss();
								stopRun();
							}
						});
				ab.create().show();

			}

			SlideMainActivity.bCenter = true;
			// Editor edit = m_settingPre.edit();
			// edit.putInt("activity_flag", 0);
			// edit.commit();
			// cancleNotificaction();
			try {
				if (milinkReceiver != null)
					unregisterReceiver(milinkReceiver);

			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}

			// mMapView.onDestroy();

		}
		return super.onKeyDown(keyCode, event);
	}

	private boolean isOnCalling = false;

	public void createPhoneListener() {
		TelephonyManager telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		telephony.listen(new OnePhoneStateListener(),
				PhoneStateListener.LISTEN_CALL_STATE);

	}

	class OnePhoneStateListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			// MyLog.i(TAG, "[Listener]閻絻鐦介崣椋庣垳:"+incomingNumber);

			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:
				isOnCalling = true;
				try {
					if (a1 != null)
						a1.stop();
				} catch (Exception e) {
					// TODO: handle exception
				}
				break;
			case TelephonyManager.CALL_STATE_IDLE:
				isOnCalling = false;
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				// MyLog.e(TAG, "[Listener]闁俺鐦芥稉锟�+incomingNumber);
				isOnCalling = true;
				try {
					if (a1 != null)
						a1.stop();
				} catch (Exception e) {
					// TODO: handle exception
				}
				break;
			}
			super.onCallStateChanged(state, incomingNumber);
		}
	}

	public static final int soundtype_start = 0;
	public static final int soundtype_pause = 1;
	public static final int soundtype_resume = 2;
	public static final int soundtype_stop = 3;
	public static final int soundtype_onekm = 4;
	public static final int soundtype_summary = 5;

	static public int steps_show;
	static public int distance_show;
	static public int duration_show;
	static public int cal_show;
	MiSpeech a1;
	Object voiceLock = new Object();
	MiSpeechSummary ss;

	void playVoice(final int type) {
		if (!isOnCalling) {
			if (isVoice) {
				synchronized (voiceLock) {

					new Thread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub

							switch (type) {
							case soundtype_start:
								a1 = new MiSpeechStart(GpsSportActivity.this);
								a1.start();
								break;
							case soundtype_pause:
								a1 = new MiSpeechPause(GpsSportActivity.this);
								a1.start();
								break;
							case soundtype_resume:
								a1 = new MiSpeechResume(GpsSportActivity.this);
								a1.start();
								break;
							case soundtype_stop:
								a1 = new MiSpeechStop(GpsSportActivity.this);
								a1.start();
								break;
							case soundtype_onekm:
								int ttt = (int) (totalTime - totalTime_lastonekm);
								int ddd = (int) (distance / 1000);
								// ttt=123456;
								totalTime_lastonekm = totalTime;
								MiSpeechOneKmPool kk = new MiSpeechOneKmPool(
										GpsSportActivity.this, ttt, ddd);
								try {
									kk.play();
								} catch (InterruptedException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								break;
							case soundtype_summary:
								ss = new MiSpeechSummary(GpsSportActivity.this,
										steps_show, duration_show,
										distance_show, cal_show);
								try {
									ss.play();
								} catch (InterruptedException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								break;
							default:
								break;
							}
						}
					}).start();
				}
			}
		}
	}

	private int verticalPage = 0;
	private int steplistcount = 0;
	private int lststeplistcount = 0;

	class MyPageScrollListener implements
			VerticalSlidingView.OnPageScrollListener {

		@Override
		public void onPageChanged(int position) {

			verticalPage = position;
			if (lststeplistcount != steplistcount && position == 1 && isRun) {
				buildStepChart();
				lststeplistcount = steplistcount;
			}

			if (position == 0 && isRun) {
				updateBoardSteps();
			}

		}

	}

	private ImageView gps_start, gps_pause, gps_stop;
	private TextView gpsClock, gpsStep, gpsCal, gpsDis;
	private WebView webstep, webspeed;
	private WebViewHelper helpstep, helpspeed;
	private static Boolean stepfirst;
	private static Boolean speedfirst;

	private void initViews() {
		LayoutInflater inflater = LayoutInflater.from(this);
		View layout1 = inflater.inflate(R.layout.gps_layout1, null);
		gps_start = (ImageView) layout1.findViewById(R.id.start);
		gps_stop = (ImageView) layout1.findViewById(R.id.stop);
		gps_pause = (ImageView) layout1.findViewById(R.id.pause);
		gpsClock = (TextView) layout1.findViewById(R.id.gpsClock);
		gpsStep = (TextView) layout1.findViewById(R.id.gpsStep);

		gpsCal = (TextView) layout1.findViewById(R.id.gpsCal);
		gpsDis = (TextView) layout1.findViewById(R.id.gpsDis);
		gpsClock.setTypeface(MilinkApplication.NumberFace);
		gpsDis.setTypeface(MilinkApplication.NumberFace);
		gpsCal.setTypeface(MilinkApplication.NumberFace);
		gpsStep.setTypeface(MilinkApplication.NumberFace);
		gps_start.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (!isRun) {
					switchStart();
				} else {
					switchPause();
				}

			}
		});

		gps_stop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				switchStart();

			}
		});

		gps_pause.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				switchPause();

			}
		});
		mViews[0] = layout1;
		View layout2 = inflater.inflate(R.layout.gps_layout2, null);
		webstep = (WebView) layout2.findViewById(R.id.webstep);
		webspeed = (WebView) layout2.findViewById(R.id.webspeed);

		webstep.setFocusable(false);
		webspeed.setFocusable(false);
		helpstep = new WebViewHelper(this, webstep,
				"file:///android_asset/chart/gpshigh.html");
		helpstep.initWebview();
		helpstep.setBgTransparent();

		helpspeed = new WebViewHelper(this, webspeed,
				"file:///android_asset/chart/gpshigh2.html");
		helpspeed.initWebview();
		helpspeed.setBgTransparent();

		mViews[1] = layout2;

		ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
		mSlideView.addView(mViews[0], lp);
		mSlideView.addView(mViews[1], lp);
		stepfirst = true;
		speedfirst = true;
		mDrawer.setOnDrawerCloseListener(new MultiDirectionSlidingDrawer.OnDrawerCloseListener() {

			@Override
			public void onDrawerClosed() {
				// TODO Auto-generated method stub
				ImageView handle = (ImageView) (mDrawer.getHandle()
						.findViewById(R.id.img));
				handle.setImageResource(R.drawable.arrow_down);

			}
		});

		mDrawer.setOnDrawerOpenListener(new MultiDirectionSlidingDrawer.OnDrawerOpenListener() {

			@Override
			public void onDrawerOpened() {
				// TODO Auto-generated method stub
				ImageView handle = (ImageView) (mDrawer.getHandle()
						.findViewById(R.id.img));
				handle.setImageResource(R.drawable.arrow_up);
			}
		});

		mDrawer.open();
	}

	private VelocityTracker mVelocityTracker;
	private int mLastY;
	private int mStartYPosition;
	private int mEndYPosition;
	private View.OnTouchListener webtouch = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			if (mVelocityTracker == null) {
				mVelocityTracker = VelocityTracker.obtain();
			}
			mVelocityTracker.addMovement(event);
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mLastY = (int) event.getY();
				break;
			case MotionEvent.ACTION_MOVE:
				int y = (int) event.getY();
				int distance = mLastY - y;
				mLastY = y;
				if (Math.abs(distance) > 300) {
					return false;
				}
				break;
			case MotionEvent.ACTION_UP:
				mVelocityTracker.computeCurrentVelocity(1000);
				int velocityY = (int) mVelocityTracker.getYVelocity();
				mVelocityTracker.recycle();
				mVelocityTracker = null;

				if (Math.abs(velocityY) >= 600) {
					// return false;
				} else {
					// return true;
				}
				break;
			default:
				break;
			}

			return false;

		}
	};

	private PopupWindow initPopupWindow() {
		View fa = getLayoutInflater().inflate(R.layout.pop_club_operate, null);
		final PopupWindow pop = new PopupWindow(fa, LayoutParams.WRAP_CONTENT,
				getResources().getDimensionPixelSize(R.dimen.icon_large_x),
				true);
		Button create = (Button) fa.findViewById(R.id.add_guanzhu);
		Button update = (Button) fa.findViewById(R.id.a_friend);
		Button manager = (Button) fa.findViewById(R.id.ignore);
		Button invite = (Button) fa.findViewById(R.id.invite);
		TextView line1 = (TextView) fa.findViewById(R.id.l1);
		TextView line2 = (TextView) fa.findViewById(R.id.t2);
		TextView line3 = (TextView) fa.findViewById(R.id.t3);
		create.setText(R.string.gps_sport_history);
		line1.setVisibility(View.GONE);
		line2.setVisibility(View.GONE);
		line3.setVisibility(View.GONE);
		update.setVisibility(View.GONE);
		manager.setVisibility(View.GONE);
		invite.setVisibility(View.GONE);
		create.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				pop.dismiss();
				startActivity(new Intent(GpsSportActivity.this,
						GpsHistoryActivity.class));
			}
		});

		pop.setBackgroundDrawable(new BitmapDrawable());
		pop.setOutsideTouchable(true);
		pop.setTouchable(true);
		return pop;
	}

	@Override
	public void onGetOfflineMapState(int arg0, int arg1) {
		// TODO Auto-generated method stub

	}

}

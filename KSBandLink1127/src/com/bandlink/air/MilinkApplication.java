package com.bandlink.air;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import cn.jpush.android.api.JPushInterface;

import com.baidu.mapapi.SDKInitializer;
import com.bandlink.air.ble.BleScanService;
import com.bandlink.air.pingan.PinganRegisterActivity;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.HttpRequest;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Util;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.FIFOLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.StorageUtils;

public class MilinkApplication extends Application {
	private static final String TAG = "JPush";
	private static MilinkApplication mInstance = null;
	SharedPreferences preferences;
	public static int step;
	public static final String ACTION_ZERO = "com.milink.android.tozero";
	public static Typeface NumberFace, lineFont;
	public static String fontPath;
	public Dbutils dbutil;

	@Override
	public void onCreate() {
		NumberFace = Typeface.createFromAsset(getAssets(),
				"font/AvenirLTStd-Light.otf");
		MyLog.d(TAG, "[MilinkApplication] onCreate");
		// checkFont(this);
		super.onCreate();
		SDKInitializer.initialize(this);
		JPushInterface.setDebugMode(true); // 设置开启日志,发布时请关闭日志
		JPushInterface.init(this); // 初始化 JPush
		mInstance = this;
		initEngineManager(this);
		preferences = getApplicationContext().getSharedPreferences(
				SharePreUtils.APP_ACTION, Context.MODE_PRIVATE);
		dbutil = new Dbutils(preferences.getInt("UID", -1), this);
		SharedPreferences airPreference = getSharedPreferences("air",
				Context.MODE_MULTI_PROCESS);

		if (airPreference.getInt("UID", -1) == -1) {
			airPreference.edit().putInt("UID", preferences.getInt("UID", -1))
					.commit();
		}
		if (airPreference.getString("USERNAME", "").equals("")) {
			airPreference
					.edit()
					.putString("USERNAME",
							preferences.getString("USERNAME", "")).commit();
		}

		if (airPreference.getString("PASSWORD", "").equals("")) {
			airPreference
					.edit()
					.putString("PASSWORD",
							preferences.getString("PASSWORD", "")).commit();
		}

		if (airPreference.getInt("ISMEMBER", -2) == -2) {
			airPreference.edit()
					.putInt("ISMEMBER", preferences.getInt("ISMEMBER", 0))
					.commit();
		}

		showSportReport();
		if (!preferences.getString("session_id", "").equals("")) {
			uploadMobileInfo();
			if (dbutil.getUserDeivceType() != 5) {
				getDevice();
			}
		}
		initImageLoader(this);
		startService(new Intent(this, BleScanService.class));

		// addCleanTask();
		// test() ;
		// test3();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		System.out.println(newConfig);
		super.onConfigurationChanged(newConfig);
	}

	// 给丢失的会员 补步数
	void test3() {
		new Thread(new Runnable() {
			public void run() {
				for (int i = 0; i > -30; i--) {
					int step = 6160;
					double x = Math.random();
					if (x < 0.25) {
						step = (int) (10000 + Math.random() * 8000);
					} else if (x < 0.4) {
						step = (int) (10000 + Math.random() * 5000);
					} else {
						step = (int) (10000 + Math.random() * 2000);
					}
					String url = String
							.format("%s/data/upLoadSpDataByUid/uid/%s/step/%d/calorie/%d/distance/%s/time/%s/type/2",
									HttpUtlis.BASE_URL, "194363", step,
									step * 30, Math.round(step * 0.65f),
									Util.getBeforeAfterDate("2015-09-30", i));

					String str = HttpRequest.sendGet(url);
					System.out.println(str);
				}
			}
		}).start();
	}

	void test() {
		new Thread(new Runnable() {
			public void run() {
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("access_token", "A5BB0E8E3D884E47801D84817180DBE2");
				params.put("partyNo", "010209964372");
				params.put("partnerCode", "0330000");
				params.put("partnerMemberNo", "乐步施海屏");
				try {
					String res = HttpUtlis
							.getRequestForPost(
									PinganRegisterActivity.PINGAN_BASE_URL
											+ "/open/appsvr/health/partner/bind/account/0330000?access_token=A5BB0E8E3D884E47801D84817180DBE2"
											+ "", params);
					System.out.println(res);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	void test2() {
		new Thread(new Runnable() {
			public void run() {
				String url = PinganRegisterActivity.PINGAN_BASE_URL
						+ "/open/appsvr/health/partner/bind/device/0330000?access_token="
						+ "A5BB0E8E3D884E47801D84817180DBE2";
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("partyNo", "010209964372");
				map.put("partnerCode", "0330000");
				// 2 flame
				map.put("deviceType", "2");
				try {
					String res = HttpUtlis.getRequestForPost(url, map);
					System.out.println(res);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	/***
	 * 请求用户绑定的设备信息
	 */
	private void getDevice() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Map<String, String> map = new HashMap<String, String>();
				map.put("session", preferences.getString("session_id", ""));

				try {
					String result = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/user/getUserDevice", map);
					JSONObject js = new JSONObject(result);
					if (js.getString("message").equals("ok")) {
						JSONObject devJson = js.getJSONObject("content");

						try {
							dbutil.InitDeivce(Integer.parseInt(devJson
									.getString("devicetype")), dbutil
									.getDeviceWgType(), devJson
									.getString("deviceid"), "");
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {

					}
				} catch (Exception e) {
					// TODO Auto-generated cat
					e.printStackTrace();

				}
			}
		}).start();
	}

	void initImageLoader(Context context) {
		File cacheDir = StorageUtils.getOwnCacheDirectory(this,
				HttpUtlis.CACHE_Folder);
		// 创建配置ImageLoader(所有的选项都是可选的,只使用那些你真的想定制)，这个可以设定在APPLACATION里面，设置为全局的配置参数
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				context)
				// max width, max height，即保存的每个缓存文件的最大长宽
				// 线程池内加载的数量
				.threadPoolSize(5)
				// 线程优先级
				.threadPriority(Thread.NORM_PRIORITY - 2)
				/*
				 * When you display an image in a small ImageView and later you
				 * try to display this image (from identical URI) in a larger
				 * ImageView so decoded image of bigger size will be cached in
				 * memory as a previous decoded image of smaller size. So the
				 * default behavior is to allow to cache multiple sizes of one
				 * image in memory. You can deny it by calling this method: so
				 * when some image will be cached in memory then previous cached
				 * size of this image (if it exists) will be removed from memory
				 * cache before.
				 */
				.denyCacheImageMultipleSizesInMemory()

				// You can pass your own memory cache
				// implementation你可以通过自己的内存缓存实现
				.memoryCache(new FIFOLimitedMemoryCache(50 * 1024 * 1024))
				// .memoryCacheSize(2 * 1024 * 1024)
				// 硬盘缓存50MB
				.diskCacheSize(50 * 1024 * 1024)
				// 将保存的时候的URI名称用MD5
				.diskCacheFileNameGenerator(new Md5FileNameGenerator())
				// 加密
				// .diskCacheFileNameGenerator(new HashCodeFileNameGenerator())
				// 将保存的时候的URI名称用HASHCODE加密
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.diskCacheFileCount(5000) // 缓存的File数量
				.diskCache(new UnlimitedDiscCache(cacheDir))// 自定义缓存路径
				// .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
				// .imageDownloader(new BaseImageDownloader(context, 5 * 1000,
				// 30 * 1000)) // connectTimeout (5 s), readTimeout (30 s)超时时间
				// .writeDebugLogs() // Remove for release app
				.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);// 全局初始化此配置
		ImageLoader.getInstance().clearDiskCache();
	}

	public static DisplayImageOptions getListOptions() {
		DisplayImageOptions options = new DisplayImageOptions.Builder()
		// 设置图片在下载期间显示的图片
				.showImageOnLoading(R.drawable.avatar)
				// 设置图片Uri为空或是错误的时候显示的图片
				.showImageForEmptyUri(R.drawable.avatar)
				// 设置图片加载/解码过程中错误时候显示的图片
				.showImageOnFail(R.drawable.avatar)
				// 设置下载的图片是否缓存在内存中
				.cacheInMemory(false)
				// 设置下载的图片是否缓存在SD卡中
				.cacheOnDisk(true)
				// 保留Exif信息
				.considerExifParams(true)
				// 设置图片以如何的编码方式显示
				.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
				// 设置图片的解码类型
				// .bitmapConfig(Bitmap.Config.RGB_565)
				// .decodingOptions(android.graphics.BitmapFactory.Options
				// decodingOptions)//设置图片的解码配置
				// .considerExifParams(true)
				// 设置图片下载前的延迟
				// .delayBeforeLoading(100)// int
				// delayInMillis为你设置的延迟时间
				// 设置图片加入缓存前，对bitmap进行设置
				// .preProcessor(BitmapProcessor preProcessor)
				// .resetViewBeforeLoading(true)// 设置图片在下载前是否重置，复位
				// .displayer(new RoundedBitmapDisplayer(20))//是否设置为圆角，弧度为多少
				// .displayer(new FadeInBitmapDisplayer(0))// 淡入
				.build();
		return options;
	}

	private void showSportReport() {

		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		c.set(Calendar.HOUR_OF_DAY, 9);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		Intent intent = new Intent("SportReport");
		setRepeatTasks(c.getTimeInMillis(), 24 * 60 * 60 * 1000, intent);

	}

	private void addCleanTask() {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		Intent intent = new Intent(ACTION_ZERO);
		setRepeatTasks(c.getTimeInMillis(), 24 * 60 * 60 * 1000, intent);
	}

	private void setRepeatTasks(long triggerTime, long interval, Intent intent) {

		AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		// 系统中如果有这个pendingIntent 取消
		PendingIntent pending = PendingIntent.getBroadcast(this, 88, intent,
				PendingIntent.FLAG_NO_CREATE);
		if (pending != null) {
			am.cancel(pending);
		}
		pending = PendingIntent.getBroadcast(this, 88, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		am.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime, interval, pending);

	}

	public void initEngineManager(Context context) {
		// if (mBMapManager == null) {
		// mBMapManager = new BMapManager(context);
		// }
		//
		// if (!mBMapManager.init(null)) {
		// //
		// Toast.makeText(MiApplication.getInstance().getApplicationContext(),
		// // "BMapManager  初始化错误!", Toast.LENGTH_LONG).show();
		// mBMapManager = null;
		// }

		/*
		 * if (!mBMapManager.init(strKey,new MyGeneralListener())) {
		 * //Toast.makeText(MiApplication.getInstance().getApplicationContext(),
		 * //"BMapManager  初始化错误!", Toast.LENGTH_LONG).show(); mBMapManager =
		 * null; }
		 */
	}

	public void uploadMobileInfo() {
		final String mobileinfo = android.os.Build.MODEL;
		final String sys = android.os.Build.VERSION.SDK;
		final String app = getAppVersionName(this);
		new Thread() {
			public void run() {
				// 这个版本是记录推广下载量的，记录一个。升级时也不会丢失
				// preferences.edit().putString("avd", "_").commit();
				Map<String, String> args = new HashMap<String, String>();
				DisplayMetrics dm = getResources().getDisplayMetrics();
				args.put("session", preferences.getString("session_id", ""));
				args.put("app", app + preferences.getString("avd", ""));
				args.put("info", mobileinfo + "_" + dm.toString());
				args.put("sys", sys);
				String result;
				try {
					result = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/user/setUserAirDevice", args);
					JSONObject object = new JSONObject(result);
					if (object.getInt("status") == 0) {

					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}.start();
	}
	
	private String getAppVersionName(Context context) {
		String versionName = "";
		try {
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(
					"com.milink.android.air", 0);
			versionName = packageInfo.versionName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return versionName;
	}

	public static MilinkApplication getInstance() {
		return mInstance;
	}

}

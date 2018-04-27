package com.bandlink.air.simple;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import cn.jpush.android.service.PushService;

import com.bandlink.air.R;
import com.bandlink.air.ble.BluetoothLeService;
import com.bandlink.air.user.LoginActivity;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.Base64Coder;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.MyDate;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Util;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners.UMAuthListener;
import com.umeng.socialize.controller.listener.SocializeListeners.UMDataListener;
import com.umeng.socialize.exception.SocializeException;
import com.umeng.socialize.sso.QZoneSsoHandler;

public class SyncQQHealth extends LovefitActivity {
	private ActionbarSettings actionbar;

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub
		changeBgColor();
	}

	private SharedPreferences share;
	private TextView lasttime, name, status;
	public static String appId = "1101968121";
	public static String appKey = "QoeKCspZFMB7LEp5";
	private Button startsync, toqq;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setContentView(R.layout.activity_sync_qq);
		share = this.getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		actionbar = new ActionbarSettings(this, new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				SyncQQHealth.this.finish();
			}
		}, null);

		IntentFilter inf = new IntentFilter();
		inf.addAction(ACTION_QQHEALTH_LOGIN);
		inf.addAction(ACTION_QQHEALTH);
		QZoneSsoHandler qZoneSsoHandler = new QZoneSsoHandler(this, appId,
				appKey);
		qZoneSsoHandler.addToSocialSDK();
		registerReceiver(receiver, inf);
		actionbar.setTitle(R.string.sync_qq);
		actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
		name = (TextView) findViewById(R.id.name);
		lasttime = (TextView) findViewById(R.id.lasttime);
		startsync = (Button) findViewById(R.id.startsync);
		toqq = (Button) findViewById(R.id.toqq);
		status = (TextView) findViewById(R.id.bind);

		toqq.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String uri = "http://jiankang.qq.com/?_wv=2163715&_bid=233";
				if (isPackageExists(SyncQQHealth.this, "com.tencent.mobileqq")) {
					uri = "mqqapi://forward/url?src_type=web&version=1&url_prefix="
							+ Base64Coder.encodeString(uri);
				}
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
				startActivity(intent);
			}
		});

		status.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (share.getString("qq_token", null) != null) {
					// 解绑
					// bindAccount(share.getString("session_id", ""),
					// share.getString("qq_openid", "xxxx"), null, false);

					showUnbind();
				} else {
					auth(SyncQQHealth.this, getIntent().getIntExtra("step", 0),
							getIntent().getIntExtra("distance", 0), getIntent()
									.getIntExtra("duration", 0), getIntent()
									.getLongExtra("time", 0), getIntent()
									.getIntExtra("calories", 0));
				}
			}
		});
		if (getIntent().getIntExtra("auth", 0) == 1) {
			status.setText(R.string.toauth);
			name.setText(getString(R.string.nobind));

			auth(this, getIntent().getIntExtra("step", 0), getIntent()
					.getIntExtra("distance", 0),
					getIntent().getIntExtra("duration", 0), getIntent()
							.getLongExtra("time", 0),
					getIntent().getIntExtra("calories", 0));
		} else {
			if (share.getString("qq_name", null) != null) {
				name.setText(share.getString("qq_name",
						getString(R.string.nobind)));
				status.setText(R.string.hasbind);
			} else {
				status.setText(R.string.toauth);
				name.setText(getString(R.string.nobind));

				auth(this, getIntent().getIntExtra("step", 0), getIntent()
						.getIntExtra("distance", 0),
						getIntent().getIntExtra("duration", 0), getIntent()
								.getLongExtra("time", 0), getIntent()
								.getIntExtra("calories", 0));
			}
		}

		lasttime.setText(share.getString("qq_last", getString(R.string.nosync)));
		changeBgColor();
		startsync.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Dbutils db = new Dbutils(share.getInt("UID", -1),
						SyncQQHealth.this);
				Object[] steprs = db.getSpBasicData(MyDate.getFileName());
				String access_token = share.getString("qq_token", null);
				if (access_token != null && steprs != null
						&& steprs.length == 3) {
					dialog = Util.initProgressDialog(SyncQQHealth.this, true,
							getString(R.string.data_wait), null);
					SyncQQToday(
							SyncQQHealth.this,
							access_token,
							share.getString("qq_openid", ""),
							Integer.valueOf(steprs[0].toString()),
							Integer.valueOf(steprs[1].toString()),
							(int) (Integer.valueOf(steprs[0].toString()) / 2.5f),
							Calendar.getInstance().getTimeInMillis() / 1000,
							Integer.valueOf(steprs[2].toString()),false);
				} else if(steprs != null
						&& steprs.length == 3){
					auth(SyncQQHealth.this,
							Integer.valueOf(steprs[0].toString()),
							Integer.valueOf(steprs[1].toString()),
							(int) (Integer.valueOf(steprs[0].toString()) / 2.5f),
							Calendar.getInstance().getTimeInMillis() / 1000,
							Integer.valueOf(steprs[2].toString()));
				}else{
					Toast.makeText(SyncQQHealth.this, "暂时没有步数", Toast.LENGTH_LONG).show();
				}
			}
		});
		super.onCreate(savedInstanceState);
	}

	public static boolean isPackageExists(Context context, String targetPackage) {
		List<ApplicationInfo> packages;
		PackageManager pm;
		pm = context.getPackageManager();
		packages = pm.getInstalledApplications(0);
		for (ApplicationInfo packageInfo : packages) {

			if (packageInfo.packageName.equals(targetPackage)) {
				return true;
			}
		}
		return false;
	}

	void showUnbind() {
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle(R.string.warning);
		ab.setTitle(R.string.unbind_account);
		ab.setNegativeButton(R.string.cancel, null);
		ab.setPositiveButton(R.string.unbind_alert,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						new Thread(new Runnable() {
							public void run() {
								try {
									HashMap<String, String> params = new HashMap<String, String>();
									params.put("session",
											share.getString("session_id", ""));
									params.put("bind", "2");
									String res = HttpUtlis.getRequest(
											HttpUtlis.BASE_URL
													+ "/user/bindPlatform",
											params);
									JSONObject obj = new JSONObject(res);
									if (obj.has("status")) {
										switch (obj.getInt("status")) {
										case 0:
										case 5:
											share.edit().remove("qq_name")
													.remove("qq_token")
													.remove("qq_openid")
													.commit();
											handler.sendEmptyMessage(5);
											break;
										case 1:
											break;
										}
									}
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}).start();
					}
				});
		ab.create().show();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(receiver);

	}

	void changeBgColor() {
		if (startsync == null || toqq == null) {
			return;
		}
		int color1 = getResources().getColor(R.color.bg_0_2k);
		int color2 = getResources().getColor(R.color.bg_2_4k);
		int color3 = getResources().getColor(R.color.bg_4_6k);
		int color4 = getResources().getColor(R.color.bg_6_8k);
		int color5 = getResources().getColor(R.color.bg_8_10k);
		int color6 = getResources().getColor(R.color.bg_10_12k);
		int resId1 = R.drawable.selector_btn_qq_health;
		int resId2 = R.drawable.selector_btn_qq_health_sync;
		if (color2 == currentColor) {
			resId1 = R.drawable.selector_btn_qq_health2_4;
			resId2 = R.drawable.selector_btn_qq_health_sync2_4;
		} else if (color3 == currentColor) {
			resId1 = R.drawable.selector_btn_qq_health4_6;
			resId2 = R.drawable.selector_btn_qq_health_sync4_6;
		} else if (color4 == currentColor) {
			resId1 = R.drawable.selector_btn_qq_health6_8;
			resId2 = R.drawable.selector_btn_qq_health_sync6_8;
		} else if (color5 == currentColor) {
			resId1 = R.drawable.selector_btn_qq_health8_10;
			resId2 = R.drawable.selector_btn_qq_health_sync8_10;
		} else if (color6 == currentColor) {
			resId1 = R.drawable.selector_btn_qq_health10_12;
			resId2 = R.drawable.selector_btn_qq_health_sync10_12;
		}
		startsync.setBackgroundResource(resId2);
		startsync.setTextColor(currentColor);
		toqq.setBackgroundResource(resId1);
	}

	UMSocialService mController = UMServiceFactory
			.getUMSocialService("com.umeng.share");

	private void auth(final Context context, final int steps,
			final int distance, final int duration, final long time,
			final int calories) {
		login(SHARE_MEDIA.QZONE, context, steps, distance, duration, time,
				calories);
	}

	private void login(final SHARE_MEDIA platform, final Context context,
			final int steps, final int distance, final int duration,
			final long time, final int calories) {
		mController.doOauthVerify(this, platform, new UMAuthListener() {

			@Override
			public void onStart(SHARE_MEDIA platform) {
			}

			@Override
			public void onError(SocializeException e, SHARE_MEDIA platform) {
			}

			@Override
			public void onComplete(Bundle value, SHARE_MEDIA platform) {
				String uid = value.getString("uid");
				if (!TextUtils.isEmpty(uid)) {

					if (steps > 0) {
						SyncQQToday(context, value.getString("access_token"),
								value.getString("openid"), steps, distance,
								duration, time, calories,false);
					}

					bindAccount(share.getString("session_id", ""),
							value.getString("openid"), value, true);

				} else {
					Toast.makeText(SyncQQHealth.this, "授权失败...",
							Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onCancel(SHARE_MEDIA platform) {

			}
		});
	}

	private void getUserInfo(final SHARE_MEDIA platform) {
		mController.getPlatformInfo(SyncQQHealth.this, platform,
				new UMDataListener() {

					@Override
					public void onStart() {
						// thirdHandle.obtainMessage(3).sendToTarget();
					}

					@Override
					public void onComplete(int status, Map<String, Object> info) {
						if (info != null) {
							Bundle bundle = new Bundle();
							bundle.putString("sname",
									(String) info.get("screen_name"));
							share.edit()
									.putString("source_nick",
											(String) info.get("screen_name"))
									.commit();
							bundle.putString("photo",
									(String) info.get("profile_image_url"));
							bundle.putString("sid", info.get("uid") + "");
							if (platform.name().equals("QZONE")) {
								bundle.putString("source", "QZone");
								share.edit()
										.putString("qq_token",
												info.get("access_token") + "")
										.putString("qq_openid",
												info.get("openid") + "")
										.putString("qq_name",
												info.get("screen_name") + "")
										.commit();
								// 刷新UI
								sendBroadcast(new Intent(ACTION_QQHEALTH_LOGIN));
							} else {
								return;
							}

						} else {

						}
					}
				});
	}

	void bindAccount(final String session, final String openid,
			final Bundle value, final boolean bind) {
		new Thread(new Runnable() {
			public void run() {
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("session", session);
				params.put("openid", openid);
				params.put("source", "QZone");
				if (!bind) {
					params.put("bind", "2");
				}
				try {
					String res = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/user/bindPlatform", params);
					JSONObject obj = new JSONObject(res);
					switch (obj.optInt("status")) {
					case 6:
					case 0:
						if (bind && value != null) {
							getUserInfo(SHARE_MEDIA.QZONE);
							share.edit()
									.putString("qq_name",
											value.getString("name"))
									.putString("qq_token",
											value.getString("access_token"))
									.putString("qq_openid",
											value.getString("openid")).commit();
						} else {
							handler.sendEmptyMessage(7);
						}
						break;
					case 1:
						break;
					case 2:
						break;
					case 3:
						// 第三方帐号已被绑定
						handler.sendEmptyMessage(3);
						break;
					case 4:
						// lovefit已有绑定
						break;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	void showMsgDialog(String resid) {
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setMessage(resid);
		ab.setNegativeButton(R.string.isee, null);
		ab.setPositiveButton(R.string.login_now,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

						try {
							SyncQQHealth.this
									.stopService(new Intent(SyncQQHealth.this,
											BluetoothLeService.class));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						try {
							SyncQQHealth.this.stopService(new Intent(
									SyncQQHealth.this, PushService.class));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						SyncQQHealth.this
								.getSharedPreferences(SharePreUtils.APP_ACTION,
										Activity.MODE_PRIVATE).edit().clear()
								.commit();
						Intent in = new Intent();
						in.setClass(SyncQQHealth.this, LoginActivity.class);
						in.putExtra("value", "re");
						startActivity(in);
						SyncQQHealth.this.finish();
					}
				});
		ab.create().show();
	}

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 3:
				showMsgDialog("该QQ帐号已有绑定信息，可直接登录");
				break;
			case 1:
				break;
			case 2:
				break;
			case 0:
				break;
			case 5:
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}

				share.edit().remove("qq_name").remove("qq_token")
						.remove("qq_openid").commit();

				status.setText(R.string.toauth);
				name.setText(getString(R.string.nobind));
				Toast.makeText(SyncQQHealth.this, "解绑完成,不再自动同步QQ健康数据",
						Toast.LENGTH_LONG).show();
				break;
			case 6:
				// 绑定关系已存在

				break;
			case 7:
				// UI

				break;
			}
		};
	};
	public static String ACTION_QQHEALTH = BluetoothLeService.PACKETNAME
			+ "QQHEALTH";
	public static String ACTION_QQHEALTH_LOGIN = BluetoothLeService.PACKETNAME
			+ "QQHEALTH_LOGIN";
	public static int tryCount = 0;
	ProgressDialog dialog;
	BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals(ACTION_QQHEALTH)
					&& intent.getIntExtra("type", 0) == 0) {
				// 同步成功
				runOnUiThread(new Runnable() {
					public void run() {
						if (dialog != null && dialog.isShowing()) {
							dialog.dismiss();
						}
						if (lasttime != null) {
							lasttime.setText(share.getString("qq_last",
									getString(R.string.nosync)));
						}

					}
				});
			} else if (intent.getAction().equals(ACTION_QQHEALTH_LOGIN)) {
				name.setText(share.getString("qq_name",
						getString(R.string.nobind)));
				status.setText(R.string.hasbind);
			}
		}
	};

	public static void SyncQQToday(final Context context,
			final String access_token, final String openid, final int step,
			final int distance, final int duration, final long time,
			final int calories,final boolean value) {
		final SharedPreferences share = context.getSharedPreferences(
				SharePreUtils.APP_ACTION, Context.MODE_PRIVATE);
		String lastdate = share.getString("qq_last", "2015-10-10 23:59:59");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		if(access_token==null){
			return;
		}
		long len = 0;
		try {
			len = Integer.valueOf(lastdate.substring(0, 10).trim()
					.replace("-", ""));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (len >= (Integer.valueOf(sdf.format(new Date())))
				&& step <= share.getInt("qq_last_step", 0)) {
			// System.out.println("已是最新" + share.getInt("qq_last_step", 0));
			// return;
		}
		System.out.println("QQ START");
		new Thread(new Runnable() {
			public void run() {

				// Dbutils db = new Dbutils(share.getInt("UID", -1), context);
				// Object[] steprs = db.getSpBasicData(MyDate.getFileName());
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("access_token", access_token);
				params.put("oauth_consumer_key", appId);
				params.put("openid", openid);
				params.put("pf", "qzone");
				params.put("distance", distance + "");
				params.put("steps", step + "");
				params.put("duration", "" + duration);
				params.put("calories", calories + "");
				params.put("time", time + "");
				try {
					// {oauth_consumer_key=1101968121, calories=21500,
					// steps=7706, time=1446521615,
					// access_token=D3BC7E6AF997D9A4DD051EA436360451,
					// distance=4623, pf=qzone, duration=3082,
					// openid=39CEC48174CE7B0A3F2CDE5D6E7ECF32}
					String res = HttpUtlis.postData(
							"https://openmobile.qq.com/v3/health/report_steps",
							params);
					JSONObject obj = new JSONObject(res);

					switch (obj.getInt("ret")) {
					case 0:
						// ok
						tryCount = 0;
						share.edit().putInt("qq_last_step", step).commit();
						share.edit()
								.putString(
										"qq_last",
										new SimpleDateFormat(
												"yyyy-MM-dd HH:mm:ss")
												.format(Calendar.getInstance()
														.getTime())).commit();
						Intent mIntent= new Intent(ACTION_QQHEALTH);
						mIntent.putExtra("fromQQ", value);
						context.sendBroadcast(mIntent);
						break;
					case -10002:
						// Token Invalid
						share.edit().remove("qq_name").remove("qq_token")
								.remove("qq_openid").commit();
						Intent intent = new Intent(ACTION_QQHEALTH);
						intent.putExtra("step", step);
						intent.putExtra("type", 1);
						intent.putExtra("distance", distance);
						intent.putExtra("duration", duration);
						intent.putExtra("time", time);
						intent.putExtra("calories", calories);
						context.sendBroadcast(intent);

						break;
					case -10005:
						// QQ 健康中心系统繁忙
						// auth(context, step, distance, duration, time,
						// calories);
						if (tryCount++ < 10) {
							SyncQQToday(context, access_token, openid, step,
									distance, duration, time, calories,false);
						} else {
							tryCount = 0;
							return;
						}

						break;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					tryCount = 0;
					e.printStackTrace();
				}
			}
		}).start();

	}

}

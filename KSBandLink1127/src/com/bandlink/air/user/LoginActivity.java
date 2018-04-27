package com.bandlink.air.user;

import java.net.URL;
import java.security.MessageDigest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.jpush.android.api.JPushInterface;

import com.bandlink.air.MilinkApplication;
import com.bandlink.air.R;
import com.bandlink.air.SlideMainActivity;
import com.bandlink.air.pingan.PinganRegisterActivity;
import com.bandlink.air.simple.SyncQQHealth;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.LovefitSlidingActivity;
import com.bandlink.air.util.MApplication;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Util;
import com.tencent.connect.UserInfo;
import com.tencent.connect.auth.QQToken;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners.UMAuthListener;
import com.umeng.socialize.controller.listener.SocializeListeners.UMDataListener;
import com.umeng.socialize.exception.SocializeException;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.sso.UMSsoHandler;

public class LoginActivity extends Activity implements OnClickListener {
	private ImageView btn1, btn2, btn3;
	private Button btn_register, btn_login, btn_trial;
	private EditText account, pwd;
	private String str_acc, str_pwd, result, nickname, lovefitid, email,
			mobile, birth, vnumber;
	String session_id, sessiontag, session_time;
	int code, uid, ismember;
	// TextView tv_cancel, tv_register;
	Button tv_forget_pwd;
	String source, sid, sname, username;
	String str_pwd_or;
	String ValueFrom = "1";
	private long firstime = 0;
	SharedPreferences share;
	private Dbutils db;
	ProgressDialog progressdialog;
	private String jpushid, intentValue;
	private SharedPreferences airPreference;
	UMSocialService mController = UMServiceFactory
			.getUMSocialService("com.umeng.share");
	ImageView btn_login_pingan;
	String Platform;
	RelativeLayout temp;

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		MApplication.getInstance().remove(this);
		super.onDestroy();
	}

	TextView user_read;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View v = LayoutInflater.from(this).inflate(R.layout.login, null);
		LovefitSlidingActivity.changeBarColor(this, 0xff17B4EB, v);
		setContentView(v);
		Platform = getIntent().getStringExtra("Platform");
		temp = (RelativeLayout) findViewById(R.id.temp);
		user_read = (TextView) findViewById(R.id.user_read);
		if (Platform != null) {
			user_read.setVisibility(View.VISIBLE);
		} else {
			user_read.setVisibility(View.GONE);
		}
		ActionbarSettings actionbar = new ActionbarSettings(this,
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						if (Platform != null) {
							LoginActivity.this.finish();
						} else {
							exit();
						}
					}
				}, new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						if (Platform != null
								&& Platform.toLowerCase().equals("pa")) {
							Intent si = new Intent(LoginActivity.this,
									PinganRegisterActivity.class);
							startActivity(si);
							LoginActivity.this.finish();
						} else {
							if (intentValue != null) {
								Intent i = new Intent();
								i.setClass(LoginActivity.this, Register.class);
								i.putExtra("value", "re");
								startActivity(i);
								LoginActivity.this.finish();
							} else {
								Intent si = new Intent(LoginActivity.this,
										Register.class);
								startActivity(si);
								LoginActivity.this.finish();
							}
						}
					}
				});
		actionbar.setRightText(R.string.register);
		actionbar.setTitle(R.string.login);
		if (Platform != null) {
			findViewById(R.id.otherl).setVisibility(View.GONE);
			actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
		} else {
			actionbar.setTopLeftIcon(R.drawable.result_error);
		}

		actionbar.setRightVisible(false);

		user_read.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				AlertDialog.Builder ab = new AlertDialog.Builder(Util
						.getThemeContext(LoginActivity.this));
				String str = "1、此系统为lovefit与平安健行天下合作推出的运动积分项目。\n2、此系统只服务于平安健行天下会员，以超便捷地绑定lovefit账号。\n3、绑定成功后您可以随时自行选择更换lovefit计步设备，而无需再进行其他操作。\n4、新用户，请先注册后再登陆使用。\n5、对于已经使用lovefit产品的老用户，请返回lovefit界面登录即可。";
				ab.setMessage(str);
				ab.setPositiveButton(R.string.isee, null);
				ab.create().show();
			}
		});
		btn_login_pingan = (ImageView) findViewById(R.id.btn_login_pingan);
		btn_login_pingan.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(LoginActivity.this, LoginActivity.class);
				i.putExtra("Platform", "pa");
				startActivity(i);
				MApplication.getInstance().addActivity(LoginActivity.this);
			}
		});
		share = getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		airPreference = getSharedPreferences("air", Context.MODE_MULTI_PROCESS);
		initViews();
		if (Platform != null && Platform.toLowerCase().equals("pa")) {
			temp.setVisibility(View.GONE);
			ImageView i = (ImageView) findViewById(R.id.tid_logo);
			i.setImageResource(R.drawable.pingan);
		}
		uid = share.getInt("UID", -1);

		db = new Dbutils(uid, this);
		mController.getConfig().removePlatform(SHARE_MEDIA.RENREN,
				SHARE_MEDIA.DOUBAN);
		configPlatforms();
		try {
			if (getIntent().getStringExtra("accesstoken") != null
					&& getIntent().getStringExtra("openid") != null) {
				Tencent mTencent = Tencent.createInstance(SyncQQHealth.appId,
						getApplicationContext());

				QQToken qqToken = mTencent.getQQToken();
				long time = Long.parseLong(getIntent().getStringExtra(
						"accesstokenexpiretime"));
				qqToken.setAccessToken(getIntent()
						.getStringExtra("accesstoken"),
						(int) (time - (new Date().getTime() / 1000)) + "");
				qqToken.setOpenId(getIntent().getStringExtra("openid"));
				UserInfo info = new UserInfo(getApplicationContext(), qqToken);
				info.getUserInfo(new IUiListener() {

					@Override
					public void onError(UiError arg0) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onComplete(Object arg0) {
						// TODO Auto-generated method stub
						System.out.println("hahha");
						// {"ret":0,"msg":"","is_lost":0,"nickname":"风雨同舟","gender":"男","province":"江苏","city":"南京","figureurl":"http:\/\/qzapp.qlogo.cn\/qzapp\/1101968121\/39CEC48174CE7B0A3F2CDE5D6E7ECF32\/30","figureurl_1":"http:\/\/qzapp.qlogo.cn\/qzapp\/1101968121\/39CEC48174CE7B0A3F2CDE5D6E7ECF32\/50","figureurl_2":"http:\/\/qzapp.qlogo.cn\/qzapp\/1101968121\/39CEC48174CE7B0A3F2CDE5D6E7ECF32\/100","figureurl_qq_1":"http:\/\/q.qlogo.cn\/qqapp\/1101968121\/39CEC48174CE7B0A3F2CDE5D6E7ECF32\/40","figureurl_qq_2":"http:\/\/q.qlogo.cn\/qqapp\/1101968121\/39CEC48174CE7B0A3F2CDE5D6E7ECF32\/100","is_yellow_vip":"0","vip":"0","yellow_vip_level":"0","level":"0","is_yellow_year_vip":"0"}
						try {
							JSONObject obj = new JSONObject(arg0.toString());
							autoLoginQQ(obj.getString("nickname"), obj
									.getString("figureurl_2"), getIntent()
									.getStringExtra("accesstoken"), getIntent()
									.getStringExtra("openid")); 
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}

					@Override
					public void onCancel() {
						// TODO Auto-generated method stub

					}
				});

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void autoLoginQQ(String nick, String img, String token, String openid) {
		Bundle bundle = new Bundle();
		bundle.putString("sname", nick);
		share.edit().putString("source_nick", nick).commit();
		bundle.putString("photo", img);
		bundle.putString("sid", openid);
		bundle.putString("source", "QZone");
		share.edit().putString("qq_token", token).commit();
		share.edit().putString("qq_openid", openid).commit();
		share.edit().putString("qq_name", nick).commit();

		thirdHandle.obtainMessage(1, bundle).sendToTarget();
	}

	private void configPlatforms() {
		// 添加新浪SSO授权
		mController.getConfig().setSinaCallbackUrl(
				"http://sns.whalecloud.com/sina2/callback");
		mController.getConfig().setSsoHandler(new SinaSsoHandler());

		// 添加QQ、QZone平台
		addQQQZonePlatform();
	}
public static final String QQ_APP_ID = "1104604453";
public static final String QQ_APP_KEY = "FM2B4L2H6UPlXtli";
	private void addQQQZonePlatform() {
		 
		// 添加QQ支持, 并且设置QQ分享内容的target url
		UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler(this, QQ_APP_ID, QQ_APP_KEY);
		qqSsoHandler.setTargetUrl("http://www.umeng.com/social");
		qqSsoHandler.addToSocialSDK();
		// 添加QZone平台
		QZoneSsoHandler qZoneSsoHandler = new QZoneSsoHandler(this, QQ_APP_ID,
				QQ_APP_KEY);
		qZoneSsoHandler.addToSocialSDK();
	}

	private void login(final SHARE_MEDIA platform) {
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
					getUserInfo(platform);
				} else {
					Toast.makeText(LoginActivity.this, "授权失败...",
							Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onCancel(SHARE_MEDIA platform) {

			}
		});
	}

	private void getUserInfo(final SHARE_MEDIA platform) {
		mController.getPlatformInfo(LoginActivity.this, platform,
				new UMDataListener() {

					@Override
					public void onStart() {
						thirdHandle.obtainMessage(3).sendToTarget();
					}

					@Override
					public void onComplete(int status, Map<String, Object> info) {
						if (info != null) {

							StringBuilder sb = new StringBuilder();
							Set<String> keys = info.keySet();
							for (String key : keys) {
								sb.append(key + "=" + info.get(key).toString()
										+ "\r\n");
							}
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
							if (platform.name().equals("SINA")) {
								bundle.putString("source", "SinaWeibo_");
							} else if (platform.name().equals("QZONE")) {
								bundle.putString("source", "QZone");
								share.edit()
										.putString("qq_token",
												info.get("access_token") + "")
										.commit();
								share.edit()
										.putString("qq_openid",
												info.get("openid") + "")
										.commit();
								share.edit()
										.putString("qq_name",
												info.get("screen_name") + "")
										.commit();
							} else {
								return;
							}

							thirdHandle.obtainMessage(1, bundle).sendToTarget();
						} else {
							thirdHandle.obtainMessage(2).sendToTarget();
						}
					}
				});
	}

	void initViews() {
		account = (EditText) findViewById(R.id.user);
		if (Platform != null) {
			account.setHint(R.string.pingan_account_login);
		}
		pwd = (EditText) findViewById(R.id.pwd);
		btn1 = (ImageView) findViewById(R.id.btn1_layout);
		btn2 = (ImageView) findViewById(R.id.btn2_layout);
		btn3 = (ImageView) findViewById(R.id.btn3_layout);
		btn_login = (Button) findViewById(R.id.btn_login);
		btn_register = (Button) findViewById(R.id.btn_register);
		// btn_register.startAnimation(AnimationUtils.loadAnimation(this,
		// R.anim.scale_0_to_1));
		btn_trial = (Button) findViewById(R.id.btn_trial);
		btn_register.setOnClickListener(this);
		btn_login.setOnClickListener(this);
		btn_trial.setOnClickListener(this);
		// btn1.setImageResource(R.drawable.qq);
		// btn2.setImageResource(R.drawable.weibo);
		// btn3.setImageResource(R.drawable.logo_wechat);
		// btn1.setText(R.string.qq);
		// btn2.setText(R.string.weibo);
		// btn3.setText(R.string.wechat_login);
		btn1.setOnClickListener(this);
		btn2.setOnClickListener(this);
		btn3.setOnClickListener(this);
		// tv_cancel = (TextView) findViewById(R.id.txt_return);
		// tv_register = (TextView) findViewById(R.id.txt_register);
		tv_forget_pwd = (Button) findViewById(R.id.txt_forget_psd);
		// tv_cancel.setOnClickListener(this);
		// tv_register.setOnClickListener(this);
		tv_forget_pwd.setOnClickListener(this);
		Intent getValue = getIntent();
		try {
			intentValue = getValue.getExtras().getString("value");
			if (intentValue != null) {
				// tv_cancel.setText(R.string.exit);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		btn_trial.setVisibility(View.GONE);
		if (share.getInt("UID", -2) <= -2) {
			// btn_trial.setVisibility(View.VISIBLE);
		} else {
			// btn_trial.setText("继续试用");
		}
		// if (account.isFocused()) {
		// account.setHint("");
		// }
		// if (pwd.isFocused()) {
		// pwd.setHint("");
		// }
		account.setOnFocusChangeListener(new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (Platform != null) {
					return;
				}
				if (hasFocus) {// 获得焦点
					// 在这里可以对获得焦点进行处理
					account.setHint("");
				} else {// 失去焦点
						// 在这里可以对输入的文本内容进行有效的验证
					if (account.getText().toString().trim().length() < 1) {
						account.setHint(R.string.acc);
					}
				}
			}
		});
		pwd.setOnFocusChangeListener(new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {// 获得焦点
					// 在这里可以对获得焦点进行处理
					pwd.setHint("");
				} else {// 失去焦点
						// 在这里可以对输入的文本内容进行有效的验证
					if (pwd.getText().toString().trim().length() < 1) {
						pwd.setHint(R.string.psd);
					}
				}
			}
		});
		try {
			String hintname = airPreference.getString("USERNAME", str_acc);
			if (hintname.startsWith("temp_") || hintname.startsWith("QZone_")
					|| hintname.startsWith("SinaWeibo_")) {

			} else {
				if (airPreference.getString("isPutLogin", "").equals("123")) {
					if (Platform != null) {
						// pa
						if ((hintname.startsWith("0") && hintname.length() == 12)
								|| (hintname.startsWith("1") && hintname
										.length() == 11)) {
							account.setText(hintname);
							pwd.setText(airPreference.getString("PASSWORD_OR",
									str_pwd_or));
						}
					} else {
						if (!hintname.startsWith("0")
								&& hintname.length() != 12) {
							account.setText(hintname);
							pwd.setText(airPreference.getString("PASSWORD_OR",
									str_pwd_or));
						}
					}

				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_login:
			btn_login();
			break;
		case R.id.btn_register:
			if (Platform != null && Platform.toLowerCase().equals("pa")) {
				Intent si = new Intent(this, PinganRegisterActivity.class);
				startActivity(si);
				this.finish();
			} else {
				if (intentValue != null) {
					Intent i = new Intent();
					i.setClass(this, Register.class);
					i.putExtra("value", "re");
					startActivity(i);
					this.finish();
				} else {
					Intent si = new Intent(this, Register.class);
					startActivity(si);
					this.finish();
				}
			}
			break;
		case R.id.btn_trial:

			Intent intent = new Intent(this, SlideMainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intent);
			this.finish();

			break;
		case R.id.btn1_layout:
			Toast.makeText(this, getString(R.string.getqqlogin),
					Toast.LENGTH_SHORT).show();
			login(SHARE_MEDIA.QZONE);
			break;

		case R.id.btn2_layout:
			Toast.makeText(this, getString(R.string.data_wait),
					Toast.LENGTH_SHORT).show();
			login(SHARE_MEDIA.SINA);
			break;
		case R.id.btn3_layout:
			// Platform plat3 = ShareSDK.getPlatform(this, Wechat.NAME);
			// plat3.setPlatformActionListener(this);
			// plat3.authorize();
			break;
		// case R.id.txt_return:
		// if (intentValue != null) {
		// exit();
		// } else {
		// this.finish();
		// }
		// break;
		// case R.id.txt_register:
		// if (intentValue != null) {
		// Intent ii = new Intent();
		// ii.setClass(this, Register.class);
		// ii.putExtra("value", "re");
		// startActivity(ii);
		// this.finish();
		// } else {
		// Intent sa = new Intent(this, Register.class);
		// startActivity(sa);
		// this.finish();
		// }
		// break;
		case R.id.txt_forget_psd:
			Uri uri = Uri
					.parse("http://www.lovefit.com/do.php?ac=lostpwdmobile");
			startActivity(new Intent(Intent.ACTION_VIEW, uri));
			break;
		default:
			break;
		}
	}

	// public boolean isMobileNO(String mobiles) {
	// Pattern p = Pattern
	// .compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
	// Matcher m = p.matcher(mobiles);
	// return m.matches();
	// }

	// 判断email格式是否正确
	public boolean isEmail(String email) {
		String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
		Pattern p = Pattern.compile(str);
		Matcher m = p.matcher(email);

		return m.matches();
	}

	public void btn_login() {
		str_acc = account.getText().toString().trim();
		str_pwd = pwd.getText().toString().trim();
		if (str_acc != null && str_acc.length() > 1 && str_pwd != null
				&& pwd.length() >= 6 && pwd.length() <= 25) {
			progressdialog = Util.initProgressDialog(this, true,
					getString(R.string.logining), null);
			jpushid = share.getString("JPUSHID", null);
			if (jpushid == null || jpushid.equals("")) {
				jpushid = JPushInterface.getRegistrationID(this);
			}

			Thread loginThread = new Thread(new LoginThread());
			loginThread.start();
		} else if (str_acc == null || str_acc.length() <= 1) {
			Toast.makeText(this, getString(R.string.input_mistake),
					Toast.LENGTH_SHORT).show();
		} else if (isEmail(str_acc) || (!(str_acc.substring(0, 1).equals("1")))) {
			Toast.makeText(this, R.string.user_email_phone_error,
					Toast.LENGTH_SHORT).show();
		} else if (pwd.length() < 6 && pwd.length() > 25) {
			Toast.makeText(this, getString(R.string.accmanage_pwd_erro),
					Toast.LENGTH_SHORT).show();
		}
	}

	public ProgressDialog initProgressDialog() {
		ProgressDialog progressDialog = ProgressDialog.show(this,
				getString(R.string.data_wait), getString(R.string.logining),
				true);
		progressDialog.setCancelable(true);

		LayoutInflater inflater = LayoutInflater.from(this
				.getApplicationContext());
		View v = inflater.inflate(R.layout.loading, null);// 寰楀埌鍔犺浇view
		TextView tvMsg = (TextView) v.findViewById(R.id.tvTitle);

		if (tvMsg != null) {
			tvMsg.setText(getString(R.string.logining));
		}
		progressDialog.setContentView(v);
		progressDialog
				.setOnCancelListener(new DialogInterface.OnCancelListener() {

					@Override
					public void onCancel(DialogInterface dialog) {
						// TODO Auto-generated method stub

					}
				});

		return progressDialog;
	}

	public static String MD5(String str) {

		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (Exception e)

		{
			e.printStackTrace();
			return "";
		}

		char[] charArray = str.toCharArray();
		byte[] byteArray = new byte[charArray.length];
		for (int i = 0; i < charArray.length; i++) {
			byteArray[i] = (byte) charArray[i];
		}

		byte[] md5Bytes = md5.digest(byteArray);
		StringBuffer hexValue = new StringBuffer();
		for (int i = 0; i < md5Bytes.length; i++) {
			int val = ((int) md5Bytes[i]) & 0xff;
			if (val < 16) {
				hexValue.append("0");
			}
			hexValue.append(Integer.toHexString(val));
		}
		return hexValue.toString();

	}

	// Handler
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				saveToPreferences();
				getUserInfoFromNet();
				if (!share.getString("session_id", "").equals("")) {
					MilinkApplication app = (MilinkApplication) LoginActivity.this
							.getApplication();
					app.uploadMobileInfo();
				}

				break;
			case 1:
				if (progressdialog != null && progressdialog.isShowing())
					progressdialog.dismiss();
				Toast.makeText(LoginActivity.this,
						getString(R.string.not_user), Toast.LENGTH_SHORT)
						.show();
				break;
			case 2:
				if (progressdialog != null && progressdialog.isShowing())
					progressdialog.dismiss();
				Toast.makeText(LoginActivity.this,
						getString(R.string.pwd_mistake), Toast.LENGTH_SHORT)
						.show();
				break;
			case 3:
				if (progressdialog != null && progressdialog.isShowing())
					progressdialog.dismiss();
				Toast.makeText(LoginActivity.this, "error", 1).show();
				break;
			case 4:
				if (progressdialog != null && progressdialog.isShowing())
					progressdialog.dismiss();
				Toast.makeText(getApplicationContext(),
						getString(R.string.network_erro), 1).show();
				break;
			case 5:
				if (progressdialog != null && progressdialog.isShowing())
					progressdialog.dismiss();
				Toast.makeText(getApplicationContext(),
						getString(R.string.login_susseful), Toast.LENGTH_SHORT)
						.show();
				if (intentValue != null) {
					Intent intent = new Intent();
					airPreference.edit().putString("isPutLogin", "123")
							.commit();
					startActivity(intent.setClass(LoginActivity.this,
							SlideMainActivity.class));
					((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
							.hideSoftInputFromWindow(getCurrentFocus()
									.getWindowToken(),
									InputMethodManager.HIDE_NOT_ALWAYS);
					try {
						for (Activity a : MApplication.getInstance().getList()) {
							a.finish();
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					finish();
				} else {
					finish();
				}

				break;
			}
		}
	};

	class LoginThread implements Runnable {
		@Override
		public void run() {
			String urlStr = HttpUtlis.BASE_URL + "/user/"
					+ (Platform == null ? "getlogin" : "getloginPa");
			str_pwd_or = str_pwd;
			str_pwd = MD5(str_pwd);
			Map<String, String> map = new HashMap<String, String>();
			map.put("user", str_acc);
			map.put("pwd", str_pwd);
			if (jpushid != null && !jpushid.equals("")) {
				map.put("jpushid", jpushid);
			}

			Message msg = handler.obtainMessage();
			try {
				result = HttpUtlis.getRequest(urlStr, map);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				msg.what = 4;
				handler.sendMessage(msg);
				e.printStackTrace();

			}
			if (result != null) {
				JSONObject jsonObject, obj, accobj;
				try {
					jsonObject = new JSONObject(result);
					code = Integer.parseInt(jsonObject.getString("status"));

					if (code == 0) {
						obj = jsonObject.getJSONObject("content");
						uid = obj.getInt("uid");
						session_id = obj.getString("session_id");
						session_time = obj.getString("session_time");
						nickname = obj.getString("nickname");
						ismember = obj.getInt("ismember");
						lovefitid = obj.getString("lovefitid");
						username = obj.getString("username");
						vnumber = "";
						if (obj.has("vnumber")) {
							vnumber = obj.getString("vnumber");
						}
						if (vnumber.length() == 0) {
							share.edit().putBoolean("ispa", false).commit();
						}
						// birth=obj.getString("birth");
						accobj = jsonObject.getJSONObject("account");
						email = accobj.getString("email");
						mobile = accobj.getString("mobile");

						msg.what = 0;
						if (jsonObject.has("bind")) {
							JSONArray arr = jsonObject.getJSONArray("bind");
							// "bind":[{"id":"1485","uid":"96","openid":"E276EA87BC8C6E892DAC6403C9BBF33E","source":"QZone","bind":"1","updatetime":"1446552970","inserttime":"1446552970"}]
							for (int i = 0; i < arr.length(); i++) {
								JSONObject objs = arr.getJSONObject(i);
								if (obj.has("openid")) {
									share.edit()
											.putString("qq_openid",
													objs.getString("openid"))
											.commit();
								}
							}
						}

						handler.sendMessage(msg);
					} else if (code == 1) {
						msg.what = 1;
						handler.sendMessage(msg);

					} else if (code == 2) {
						msg.what = 2;
						handler.sendMessage(msg);
					} else {
						msg.what = 3;
						handler.sendMessage(msg);

					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					msg.what = 3;
					handler.sendMessage(msg);
					e.printStackTrace();
				}

			}
		}

	}

	public void saveToPreferences() {
		// TODO Auto-generated method stub
		airPreference.edit().putInt("UID", uid).commit();
		airPreference.edit().putString("USERNAME", str_acc).commit();
		airPreference.edit().putString("PASSWORD", str_pwd).commit();
		airPreference.edit().putString("PASSWORD_OR", str_pwd_or).commit();
		airPreference.edit().putInt("ISMEMBER", ismember).commit();
		SharedPreferences.Editor editor = share.edit();
		editor.putInt("UID", uid);
		editor.putString("NICKNAME", nickname);
		editor.putString("USERNAME", str_acc);
		editor.putString("PASSWORD", str_pwd);
		editor.putInt("ISMEMBER", ismember);
		editor.putBoolean("isfirstload", false);
		editor.putString("JPUSHID", jpushid);
		editor.putString("session_id", session_id);
		editor.putString("session_time", session_time);
		editor.commit();
		uid = share.getInt("UID", -1);
		db = new Dbutils(uid, this);
		int year = share.getInt("year", 1990);
		int month = share.getInt("month", 1);
		db.InitUser(uid, nickname, ismember, 0, 170, 60.0, lovefitid, email,
				mobile, year + getString(R.string.year) + (month)
						+ getString(R.string.month), "");
		if (vnumber != null && vnumber.length() > 0) {
			share.edit().putBoolean("ispa", true).commit();
			share.edit().putString("vnumber", vnumber).commit();
			intentValue = "";
		} else {
			share.edit().putString("vnumber", null).commit();
			share.edit().putBoolean("ispa", false).commit();
		}
	}

	public void getUserInfoFromNet() {
		// progressdialog = initProgressDialog();
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Map<String, String> args = new HashMap<String, String>();
					args.put("session", share.getString("session_id", ""));
					String result = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/user/getUserData", args);
					JSONObject object = new JSONObject(result);
					if (object.getInt("status") == 0) {
						JSONObject tarCon = new JSONObject(new JSONObject(
								result).get("target").toString());
						db = new Dbutils(share.getInt("UID", -1),
								LoginActivity.this);
						db.InitTarget(
								Integer.parseInt(tarCon.getString("type")),
								Double.parseDouble(tarCon
										.getString("weight_bef")),
								Double.parseDouble(tarCon
										.getString("weight_end")),
								Integer.parseInt(tarCon.getString("step")),
								Integer.parseInt(tarCon.getString("sleepmode")),
								Integer.parseInt(tarCon.getString("weightmode")),
								Integer.parseInt(tarCon.getString("bmimode")));

						JSONObject Info = new JSONObject(new JSONObject(result)
								.get("content").toString());

						db.UpdateUserInfo(Info.getString("nickname"),
								Integer.parseInt(Info.getString("sex")),
								Double.parseDouble(Info.getString("height")),
								Double.parseDouble(Info.getString("weight")),
								username);

						JSONObject device = new JSONObject(new JSONObject(
								result).get("device").toString());

						db.InitDeivce(Integer.parseInt(device
								.getString("devicetype")), 0, device
								.getString("deviceid"), "");

						Intent intent = new Intent();
						intent.setAction("milinkStartService");
						intent.putExtra("update", true);
						sendBroadcast(intent);
						handler.obtainMessage(5).sendToTarget();
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block

					e.printStackTrace(System.err);

				}

			}
		}).start();

	}

	// public void getUserInfoFromNet() {
	//
	// new Thread(new Runnable() {
	// @Override
	// public void run() {
	// // TODO Auto-generated method stub
	// try {
	// Map<String, String> targetargs = new HashMap<String, String>();
	// targetargs.put("session",
	// share.getString("session_id", ""));
	// String target = HttpUtlis.getRequest(HttpUtlis.BASE_URL
	// + "/user/getUserTarget", targetargs);
	//
	// String userInfo = HttpUtlis.getRequest(HttpUtlis.BASE_URL
	// + "/user/getUserInfo", targetargs);
	//
	// String deviceInfo = HttpUtlis.getRequest(HttpUtlis.BASE_URL
	// + "/user/getUserDevice", targetargs);
	//
	// JSONObject tarStatus = new JSONObject(target);
	// if (tarStatus.getInt("status") == 0) {
	// JSONObject tarCon = new JSONObject(new JSONObject(
	// target).get("content").toString());
	// db.InitTarget(tarCon.getInt("type"), Double
	// .parseDouble(tarCon.getString("weight_bef")),
	// Double.parseDouble(tarCon
	// .getString("weight_end")), tarCon
	// .getInt("step"), tarCon
	// .getInt("sleepmode"), tarCon
	// .getInt("weightmode"), tarCon
	// .getInt("bmimode"));
	// }
	//
	// JSONObject InfoStatus = new JSONObject(userInfo);
	// if (InfoStatus.getInt("status") == 0) {
	// JSONObject Info = new JSONObject(new JSONObject(
	// userInfo).get("content").toString());
	//
	// db.UpdateUserInfo(Info.getString("nickname"),
	// Info.getInt("sex"), Info.getDouble("height"),
	// Info.getDouble("weight"), "");
	// }
	//
	// JSONObject device = new JSONObject(deviceInfo);
	// if (device.getInt("status") == 0) {
	// JSONObject Info = new JSONObject(new JSONObject(
	// deviceInfo).get("content").toString());
	// db.InitDeivce(
	// Integer.parseInt(Info.getString("devicetype")),
	// 0, Info.getString("deviceid"), "");
	// }
	// Intent intent=new Intent();
	// intent.setAction("milinkStartService");
	// intent.putExtra("updateStep", true);
	// sendBroadcast(intent);
	//
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	//
	// e.printStackTrace(System.err);
	//
	// }
	//
	// }
	// }).start();
	//
	// }
	public ProgressDialog creatDialog(String title, String message) {
		ProgressDialog dialog = ProgressDialog.show(this, title, message);

		dialog.setCancelable(true);

		LayoutInflater inflater = LayoutInflater.from(this
				.getApplicationContext());
		View v = inflater.inflate(R.layout.loading, null);
		TextView tvMsg = (TextView) v.findViewById(R.id.tvTitle);

		if (tvMsg != null) {
			tvMsg.setText(message);
		}
		dialog.setContentView(v);
		return dialog;
	}

	private Handler thirdHandle = new Handler() {

		public void handleMessage(Message msg) {

			switch (msg.what) {
			case 1: {
				Bundle plat = (Bundle) msg.obj;
				source = plat.getString("source");
				sid = plat.getString("sid");
				final String photo = plat.getString("photo");
				sname = plat.getString("sname");
				progressdialog = Util.initProgressDialog(LoginActivity.this,
						true, getString(R.string.logining), null);
				jpushid = share.getString("JPUSHID", null);
				if (jpushid == null || jpushid.equals("")) {
					jpushid = JPushInterface
							.getRegistrationID(LoginActivity.this);
				}
				new Thread(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						String urlStr = HttpUtlis.BASE_URL
								+ "/user/getLoginThirdParty";
						Map<String, String> map = new HashMap<String, String>();
						map.put("source", source);
						map.put("sid", sid);
						map.put("sname", sname);
						map.put("from", ValueFrom);
						if (jpushid != null && !jpushid.equals(""))
							map.put("jpushid", jpushid);
						try {
							result = HttpUtlis.getRequest(urlStr, map);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							thirdHandle.obtainMessage(2).sendToTarget();
						}
						if (result != null) {

							JSONObject jsonObject, obj, accobj;
							try {
								jsonObject = new JSONObject(result);

								code = Integer.parseInt(jsonObject
										.getString("status"));
								if (code == 0) {
									obj = jsonObject.getJSONObject("content");
									accobj = jsonObject
											.getJSONObject("account");

									uid = obj.getInt("uid");
									submitAvatar(photo);
									session_id = obj.getString("session_id");
									ismember = obj.getInt("ismember");
									session_time = obj
											.getString("session_time");
									nickname = obj.getString("nickname");
									str_acc = obj.getString("username");
									str_pwd = obj.getString("password");
									lovefitid = obj.getString("lovefitid");
									email = accobj.getString("email");
									mobile = accobj.getString("mobile");
									handler.obtainMessage(0).sendToTarget();
								} else {
									handler.obtainMessage(3).sendToTarget();
								}

							} catch (Exception e) {
								// TODO Auto-generated catch block
								handler.obtainMessage(3).sendToTarget();
								e.printStackTrace();
							}

						}
					}

				}).start();
			}
				break;
			case 2:
				if (progressdialog != null && progressdialog.isShowing()) {
					progressdialog.dismiss();
				}
				Toast.makeText(getApplicationContext(), "获取用户资料失败",
						Toast.LENGTH_SHORT).show();
				break;
			case 3:
				Toast.makeText(getApplicationContext(), "请稍等",
						Toast.LENGTH_SHORT).show();
				break;
			}

		}
	};

	public void submitAvatar(final String photo) {
		new Thread() {
			public void run() {
				try {
					// progressdialog = initProgressDialog();
					Bitmap bitmap = BitmapFactory.decodeStream(new URL(photo)
							.openStream());
					String result = HttpUtlis
							.uploadAvatar(
									HttpUtlis.UPAVATAR_URL
											+ share.getString("session_id", ""),
									bitmap);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}.start();

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		MobclickAgent.onResume(this);
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		MobclickAgent.onPause(this);
		super.onPause();
	}

	public void exit() {
		Context c = this;
		if (Build.VERSION.SDK_INT >= 11) {
			c = new ContextThemeWrapper(c, android.R.style.Theme_Holo_Light);
		}
		AlertDialog.Builder aDailog = new AlertDialog.Builder(c);
		aDailog.setCancelable(false);
		aDailog.setTitle("Exit?");
		aDailog.setMessage(getString(R.string.sure_exit));
		aDailog.setPositiveButton(getString(R.string.exit),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
						android.os.Process.killProcess(android.os.Process
								.myPid());
					}
				});

		aDailog.setNegativeButton(getString(R.string.can),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.cancel();
					}
				});
		aDailog.show();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (Platform != null) {
				LoginActivity.this.finish();
			} else {
				long secondtime = System.currentTimeMillis();
				if (secondtime - firstime > 3000) {
					Toast.makeText(this, getString(R.string.again_exit),
							Toast.LENGTH_SHORT).show();
					firstime = System.currentTimeMillis();
					return true;
				} else {
					exit();
				}
			}

		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		/** 使用SSO授权必须添加如下代码 */
		UMSsoHandler ssoHandler = mController.getConfig().getSsoHandler(
				requestCode);
		if (ssoHandler != null) {
			ssoHandler.authorizeCallBack(requestCode, resultCode, data);
		}
	}
}

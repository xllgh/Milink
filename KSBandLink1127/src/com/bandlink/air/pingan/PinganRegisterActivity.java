package com.bandlink.air.pingan;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.jpush.android.api.JPushInterface;

import com.bandlink.air.MilinkApplication;
import com.bandlink.air.R;
import com.bandlink.air.simple.InitUserData;
import com.bandlink.air.user.LoginActivity;
import com.bandlink.air.user.Register;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.LovefitSlidingActivity;
import com.bandlink.air.util.MApplication;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Util;

public class PinganRegisterActivity extends Activity implements OnClickListener {
	public final static String PINGAN_CLIEND_SECRET = "DYW8U38n";
	public final static String PINGAN_GRANT_TYPE = "client_credentials";
	public final static String PINGAN_CLIEND_ID = "P_LOVEFITAPP";
	public static final String PINGAN_BASE_URL = "https://api.pingan.com.cn";

	private Button getCode, btn_trial, regist;
	private EditText phone, account, passwd, code;
	private SharedPreferences sp, airPreference;
	private ProgressBar pro;
	private boolean hasSent = false;
	private RelativeLayout vborder;
	private ImageView resultAcc;
	private TextView have_account;
	private CheckBox checkBox;
	ActionbarSettings actionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View v = LayoutInflater.from(this).inflate(R.layout.register, null);
		LovefitSlidingActivity.changeBarColor(this, 0xff17B4EB, v);
		setContentView(v);
		sp = getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_MULTI_PROCESS);
		airPreference = getSharedPreferences("air", Context.MODE_MULTI_PROCESS);
		initViews();
		vborder = (RelativeLayout) findViewById(R.id.vborder);
		vborder.setVisibility(View.VISIBLE);
		pro.setVisibility(View.GONE);
		actionBar = new ActionbarSettings(this, new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				PinganRegisterActivity.this.finish();
			}
		}, null);
		actionBar.setTitle(R.string.pingan_login);
		actionBar.setTopLeftIcon(R.drawable.ic_top_arrow);
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					String[] token = getAccessToken();
					if (token != null) {
						handler.obtainMessage(
								0,
								Integer.valueOf(token[1])
										+ (int) (new Date().getTime() / (1000 * 60)),
								0, token[0]).sendToTarget();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}).start();
		super.onCreate(savedInstanceState);
	}

	ImageView tid_logo;
	TextView user_pro;

	private void initViews() {
		user_pro = (TextView) findViewById(R.id.user_pro);
		user_pro.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Uri uri = Uri.parse("http://www.lovefit.com/agreement.html");
				startActivity(new Intent(Intent.ACTION_VIEW, uri));
			}
		});
		tid_logo = (ImageView) findViewById(R.id.tid_logo);
		tid_logo.setImageResource(R.drawable.pingan);
		regist = (Button) findViewById(R.id.register);
		regist.setOnClickListener(this);
		checkBox = (CheckBox) findViewById(R.id.checkBox);
		resultAcc = (ImageView) findViewById(R.id.result);
		resultAcc.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (account.getTag() == null) {
					account.setText("");
				}
			}
		});
		btn_trial = (Button) findViewById(R.id.btn_trial);
		btn_trial.setVisibility(View.GONE);
		pro = (ProgressBar) findViewById(R.id.loading);
		getCode = (Button) findViewById(R.id.send);
		phone = (EditText) findViewById(R.id.account);
		account = (EditText) findViewById(R.id.vaccount);
		code = (EditText) findViewById(R.id.code);
		passwd = (EditText) findViewById(R.id.pwd);
		getCode.setVisibility(View.VISIBLE);
		code.setVisibility(View.VISIBLE);
		getCode.setOnClickListener(this);
		passwd.setHint(R.string.passwords);
		have_account = (TextView) findViewById(R.id.have_account);
		have_account.setOnClickListener(this);
		// code.setBackgroundResource(R.drawable.shape_round_border_white);
		// phone.setBackgroundResource(R.drawable.shape_leftround_edit);
		account.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				// 用户输入平安帐号后校验合法性
				if (v.getId() == R.id.vaccount && !hasFocus
						&& account.getText().length() > 0) {

					account.setTag(null);
					resultAcc.setVisibility(View.GONE);
					checkPingan(account.getText().toString());

				}
			}
		});
	}

	/***
	 * 校验平安号是否可用
	 * 
	 * @param vnumber
	 */
	private void checkPingan(final String vnumber) {
		pro.setVisibility(View.VISIBLE);

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					String result = HttpUtlis
							.getRequest(
									PINGAN_BASE_URL
											+ "/open/appsvr/health/member/authority/userIdentify/"
											+ vnumber + "?access_token="
											+ sp.getString("pingan_token", "")
											+ "", null);

					if (result.contains("\"returnCode\":\"00\"")) {
						handler.obtainMessage(1).sendToTarget();
					} else {
						handler.obtainMessage(2).sendToTarget();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					handler.obtainMessage(33).sendToTarget();
				}
			}
		}).start();

	}

	public void saveToPreferences() {
		// TODO Auto-generated method stub
		airPreference.edit().putInt("UID", Integer.parseInt(uid)).commit();
		airPreference.edit().putString("USERNAME", str_acc).commit();
		airPreference.edit().putString("PASSWORD", str_pwd).commit();
		airPreference.edit().putString("PASSWORD_OR", str_pwd_or).commit();
		airPreference.edit().putInt("ISMEMBER", ismember).commit();

		SharedPreferences.Editor editor = sp.edit();
		int u = Integer.parseInt(uid);
		editor.putInt("UID", u);
		editor.putString("USERNAME", str_acc);
		editor.putString("PASSWORD", str_pwd);
		editor.putString("NICKNAME", str_acc);
		editor.putInt("ISMEMBER", ismember);
		editor.putString("session_id", session_id);
		editor.putString("session_time", session_time);
		editor.putString("JPUSHID", jpushid);
		editor.commit();
	}

	int count = 60;
	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				sp.edit().putString("pingan_token", msg.obj.toString())
						.commit();
				sp.edit().putInt("expires_in", msg.arg1).commit();

				break;
			case 1:
				// 认证成功
				if (pro != null) {
					pro.setVisibility(View.GONE);
				}
				resultAcc.setImageResource(R.drawable.result_ok);
				resultAcc.setVisibility(View.VISIBLE);
				account.setTag("1");
				break;
			case 2:
				// 认证失败
				if (pro != null) {
					pro.setVisibility(View.GONE);
				}
				resultAcc.setImageResource(R.drawable.result_error2);
				resultAcc.setVisibility(View.VISIBLE);
				break;
			case 3:
				// 其他错误
				if (pro != null) {
					pro.setVisibility(View.GONE);
				}
				getCode.setEnabled(true);
				Toast.makeText(getApplicationContext(),
						R.string.getsmsauth_error, Toast.LENGTH_SHORT).show();
				break;
			case 4:
				getCode.setEnabled(false);
				// 发送验证码
				String code = msg.obj.toString();
				sp.edit().putString("smsauth", code).commit();
				Toast.makeText(getApplicationContext(),
						getString(R.string.getsmsauth_ok), Toast.LENGTH_SHORT)
						.show();
				break;
			case 5:
				count = 60;
				Toast.makeText(getApplicationContext(),
						R.string.getsmsauth_error, Toast.LENGTH_SHORT).show();
				sp.edit().putString("smsauth", null).commit();
				break;
			case reg_msg_ok:
				if (progressdialog != null && progressdialog.isShowing())
					progressdialog.dismiss();
				saveToPreferences();
				if (!sp.getString("session_id", "").equals("")) {
					MilinkApplication app = (MilinkApplication) PinganRegisterActivity.this
							.getApplication();
					app.uploadMobileInfo();
				}
				Intent intent = new Intent();
				intent.setClass(PinganRegisterActivity.this, InitUserData.class);
				intent.putExtra("value", "re");
				startActivity(intent);
				try {
					for (Activity a : MApplication.getInstance().getList()) {
						a.finish();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				Toast.makeText(getApplicationContext(),
						getString(R.string.register_ok), Toast.LENGTH_SHORT)
						.show();
				// 绑定

				break;
			case send_code_error:

				sp.edit().putString("smsauth", null).commit();
				String number = phone.getText().toString();

				if (number.length() == 11 && number.startsWith("1")) {
					hasSent = true;
					getCodeByPhone(number);
				}
				break;
			case send_code_count:
				count--;
				if (count <= 0) {
					getCode.setText(R.string.getsmsauth);
					getCode.setEnabled(true);
					count = 60;
					handler.removeMessages(send_code_count);
				} else {
					getCode.setEnabled(false);
					getCode.setText(count + "");
					handler.removeMessages(send_code_count);
					handler.sendEmptyMessageDelayed(send_code_count, 1000);
				}

				break;
			case 11:
				if (progressdialog != null && progressdialog.isShowing())
					progressdialog.dismiss();
				Toast.makeText(PinganRegisterActivity.this,
						getString(R.string.no_param), Toast.LENGTH_SHORT)
						.show();
				break;
			case 12:
				if (progressdialog != null && progressdialog.isShowing())
					progressdialog.dismiss();
				Toast.makeText(PinganRegisterActivity.this,
						getString(R.string.wrong_username), Toast.LENGTH_SHORT)
						.show();
				break;
			case 13:
				if (progressdialog != null && progressdialog.isShowing())
					progressdialog.dismiss();
				Toast.makeText(PinganRegisterActivity.this,
						getString(R.string.user_exist), Toast.LENGTH_SHORT)
						.show();
				break;
			case 14:
				if (progressdialog != null && progressdialog.isShowing())
					progressdialog.dismiss();
				Toast.makeText(PinganRegisterActivity.this,
						getString(R.string.register_erro), Toast.LENGTH_SHORT)
						.show();
				break;
			case 15:

				break;
			case 16:
				if (progressdialog != null && progressdialog.isShowing())
					progressdialog.dismiss();
				Toast.makeText(PinganRegisterActivity.this,
						getString(R.string.network_erro), Toast.LENGTH_SHORT)
						.show();
				break;
			case 19:
				if (progressdialog != null && progressdialog.isShowing())
					progressdialog.dismiss();
				Toast.makeText(PinganRegisterActivity.this,
						getString(R.string.vname_exist), Toast.LENGTH_SHORT)
						.show();
				break;
			case 20:
				if (progressdialog != null && progressdialog.isShowing())
					progressdialog.dismiss();
				// 平安身份绑定失败
				Toast.makeText(PinganRegisterActivity.this,
						"身份信息绑定到平安失败，请稍后在几分钟重试", Toast.LENGTH_LONG).show();
				break;
			case 21:
//access_token?
				break;
			case 33:
				if (progressdialog != null && progressdialog.isShowing())
					progressdialog.dismiss();
				Toast.makeText(PinganRegisterActivity.this, "服务器/网络异常",
						Toast.LENGTH_SHORT).show();
				break;
			}
		};
	};

	public static String[] getAccessToken() {
		String[] token = new String[2];
		// TODO Auto-generated method stub

		try {
			String result = HttpUtlis.getRequest(PINGAN_BASE_URL
					+ "/oauth/oauth2/access_token?client_id="
					+ PINGAN_CLIEND_ID + "&grant_type=" + PINGAN_GRANT_TYPE
					+ "&client_secret=" + PINGAN_CLIEND_SECRET, null);
			JSONObject js = new JSONObject(result);
			if (js.has("ret") && js.getString("ret").equals("0")) {
				if (js.getJSONObject("data").has("access_token")) {
					token[0] = js.getJSONObject("data").getString(
							"access_token");
					token[1] = js.getJSONObject("data").getString("expires_in");
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return token;
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	int x = 0;

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.send:

			String number = phone.getText().toString();
			if (number.length() >= 11) {
				getCodeByPhone(number);
			} else {
				Toast.makeText(this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.register:
			btnRegister();
			break;
		case R.id.have_account:
			Intent sa = new Intent(this, LoginActivity.class);
			sa.putExtra("Platform", "pa");
			startActivity(sa);
			this.finish();
			break;
		}
	}

	public final int send_code_error = 9;
	public final int send_code_count = 10;
	public final int reg_msg_ok = 8;
	private Dbutils db;
	int i = 0;

	private void getCodeByPhone(final String num) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				i++;
				HashMap<String, String> args = new HashMap<String, String>();
				args.put("mobile", num);
				try {
					String result = HttpUtlis.getRequest(
							HttpUtlis.BASE_URL + "/user"
									+ (hasSent ? "/resendcode" : "/sendcode"),
							args);
					JSONObject obje = new JSONObject(result);
					if (obje.getInt("status") == 0) {
						hasSent = true;
						String code = obje.getString("content");
						if (code.length() > 0) {
							handler.obtainMessage(4, code).sendToTarget();
						} else {
							if (i <= 3) {
								handler.obtainMessage(send_code_error)
										.sendToTarget();
							} else {
								handler.obtainMessage(5).sendToTarget();
							}
						}
						handler.obtainMessage(send_code_count).sendToTarget();
					} else {
						handler.obtainMessage(3).sendToTarget();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					handler.obtainMessage(3).sendToTarget();
					e.printStackTrace();
				}
			}
		}).start();
	}

	ProgressDialog progressdialog;
	private String str_acc, str_pwd, phonenumber, jpushid;
	String uid, session_id, session_time, lovefitid, email, mobile, str_pwd_or;
	private int ismember;

	public void btnRegister() {
		if (!checkBox.isChecked()) {
			Toast.makeText(this, getString(R.string.user_pro_error),
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (account.getTag() == null) {
			Toast.makeText(PinganRegisterActivity.this,
					getString(R.string.vname_error), Toast.LENGTH_LONG).show();
			return;
		}
		if (!code.getText().toString().equals(sp.getString("smsauth", "-"))) {
			Toast.makeText(PinganRegisterActivity.this,
					getString(R.string.sms_auth_error), Toast.LENGTH_LONG)
					.show();
			return;
		}
		str_acc = account.getText().toString();
		str_pwd = passwd.getText().toString();
		phonenumber = phone.getText().toString();
		str_acc = str_acc.trim();
		if (str_acc.length() > 0 && str_acc != null) {
			Pattern p = Pattern.compile("[0-9]*");
			Matcher m = p.matcher(str_acc);
			if (str_acc != null && (str_acc.length() > 0)
					&& str_acc.length() <= 40 && str_acc.length() >= 3
					&& str_pwd != null && str_pwd.length() >= 6
					&& str_pwd.length() <= 25 && checkBox.isChecked()) {
				jpushid = sp.getString("JPUSHID", null);
				if (jpushid == null || jpushid.equals("")) {
					jpushid = JPushInterface.getRegistrationID(this);
				}
				progressdialog = Util.initProgressDialog(this, false,
						getString(R.string.registering), null);
				new Thread(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						String urlStr = HttpUtlis.BASE_URL
								+ "/user/getRegisterPa";
						str_pwd_or = str_pwd;
						str_pwd = LoginActivity.MD5(str_pwd);
						Map<String, String> map = new HashMap<String, String>();
						map.put("user", phonenumber);
						map.put("vname", str_acc);
						map.put("pwd", str_pwd); 
						map.put("from", "1");
						map.put("nick", str_acc);
						map.put("token", sp.getString("pingan_token", ""));
						map.put("uid", sp.getInt("UID", -1) + "");
						if (jpushid != null && !jpushid.equals(""))
							map.put("jpushid", jpushid);
						try {
							String result = HttpUtlis.getRequest(urlStr, map);
							if (result != null) {
								JSONObject json, ob, tarCon, device, accobj;
								json = new JSONObject(result);
								int rescode = json.getInt("status");
								if (rescode == 0) {
									ob = json.getJSONObject("content");
									tarCon = json.getJSONObject("target");
									device = json.getJSONObject("device");
									accobj = json.getJSONObject("account");
									uid = ob.getString("uid");
									session_id = ob.getString("session_id");
									session_time = ob.getString("session_time");
									ismember = ob.getInt("ismember");
									lovefitid = ob.getString("lovefitid");
									email = accobj.getString("email");
									mobile = accobj.getString("mobile");
									if (ob.has("vnumber")) {
										String vnumber = ob
												.getString("vnumber");
										sp.edit().putString("vnumber", vnumber)
												.commit();
									}
									sp.edit().putString("vnumber", str_acc)
											.commit();
									sp.edit().putBoolean("ispa", true).commit();
									int sex = Integer.parseInt(ob
											.getString("sex"));
									int height = Integer.parseInt(ob
											.getString("height"));
									int weight = Integer.parseInt(ob
											.getString("weight"));

									int year = sp.getInt("year", 1990);
									int month = sp.getInt("month", 1);
									db = new Dbutils(Integer.parseInt(uid),
											PinganRegisterActivity.this);
									db.InitUser(
											Integer.parseInt(uid),
											str_acc,
											ismember,
											sex,
											height,
											weight,
											lovefitid,
											email,
											mobile,
											year + getString(R.string.year)
													+ (month)
													+ getString(R.string.month),
											ob.getString("username"));

									db.InitTarget(Integer.parseInt(tarCon
											.getString("type")), Double
											.parseDouble(tarCon
													.getString("weight_bef")),
											Double.parseDouble(tarCon
													.getString("weight_end")),
											Integer.parseInt(tarCon
													.getString("step")),
											Integer.parseInt(tarCon
													.getString("sleepmode")),
											Integer.parseInt(tarCon
													.getString("weightmode")),
											Integer.parseInt(tarCon
													.getString("bmimode")));

									db.InitDeivce(Integer.parseInt(device
											.getString("devicetype")), 0,
											device.getString("deviceid"), "");
//									// 调平安接口
//									HashMap<String, String> params = new HashMap<String, String>();
//									params.put("access_token",
//											sp.getString("pingan_token", ""));
//									params.put("partyNo", str_acc);
//									params.put("partnerCode", "0330000");
//									params.put("partnerMemberNo",
//											ob.getString("username"));
//									String res = HttpUtlis
//											.getRequestForPost(
//													PINGAN_BASE_URL
//															+ "/open/appsvr/health/partner/bind/account/0330000?access_token="
//															+ sp.getString(
//																	"pingan_token",
//																	""), params);
//
//									if (!res.contains("\"returnCode\":\"00\"")) {
//										// 同步平安失败。
//										System.out.println(res);
//										handler.obtainMessage(20)
//												.sendToTarget();
//									}
									int ptype = -1;
									if (device.has("deviceid")) {
										if (device.getString("deviceid")
												.length() > 0
												&& device.getString("deviceid")
														.startsWith("7288")) {
											ptype = 2;
										} else if (device.getString("deviceid")
												.length() > 0) {
											ptype = 1;
										}
									}
									if (ptype > 0) {
										String url = PinganRegisterActivity.PINGAN_BASE_URL
												+ "/open/appsvr/health/partner/bind/device/0330000?access_token="
												+ sp.getString("pingan_token",
														"");
										System.out.println(url);
										HashMap<String, String> map2 = new HashMap<String, String>();

										map2.put("partyNo",
												sp.getString("vnumber", ""));
										map2.put("partnerCode", "0330000");
										map2.put("deviceType", "" + ptype);
										String res2 = HttpUtlis
												.getRequestForPost(url, map2);
										System.out.println(res2);
									}
									handler.sendEmptyMessage(reg_msg_ok);
								} else {
									if(rescode + 10 == 21){
//										String token = getAccessToken()[0];
//										String result2 = HttpUtlis.getRequest(urlStr, map);
										handler.sendEmptyMessage(10 + 10);
									}else{
										handler.sendEmptyMessage(rescode + 10);
									}
									
								}
							} else {
								handler.sendEmptyMessage(33);
							}
						} catch (UnknownHostException e) {
							handler.sendEmptyMessage(6 + 10);
						} catch (Exception e) {
							// TODO Auto-generated catch block\
							handler.sendEmptyMessage(4 + 10);
							e.printStackTrace();
						}
					}

				}).start();

			} else if (!checkBox.isChecked()) {
				Toast.makeText(this, getString(R.string.user_pro_error),
						Toast.LENGTH_SHORT).show();
			} else if (str_acc.length() > 40 || str_acc.length() < 3) {
				Toast.makeText(this, getString(R.string.input_mistake),
						Toast.LENGTH_SHORT).show();
			} else if (str_acc.length() <= 40 && str_acc.length() >= 3) {
				if ((!(str_acc.startsWith("1"))) || str_acc.length() != 11
						|| (Register.isEmail(str_acc))) {
					Toast.makeText(this, R.string.user_email_phone_error,
							Toast.LENGTH_SHORT).show();
				} else if (str_pwd == null || str_pwd.length() < 6
						|| str_pwd.length() > 25) {
					Toast.makeText(this,
							getString(R.string.accmanage_pwd_erro),
							Toast.LENGTH_SHORT).show();
				}
			}
		} else if (!checkBox.isChecked()) {
			Toast.makeText(this, getString(R.string.user_pro_error),
					Toast.LENGTH_SHORT).show();
		} else if (str_acc.length() > 40 || str_acc.length() < 3) {
			Toast.makeText(this, getString(R.string.input_mistake),
					Toast.LENGTH_SHORT).show();
		} else if (str_acc.length() <= 40 && str_acc.length() >= 3) {
			if ((!(str_acc.startsWith("1"))) || str_acc.length() != 11
					|| (Register.isEmail(str_acc))) {
				Toast.makeText(this, R.string.user_email_phone_error,
						Toast.LENGTH_SHORT).show();
			} else if (str_pwd == null || str_pwd.length() < 6
					|| str_pwd.length() > 25) {
				Toast.makeText(this, getString(R.string.accmanage_pwd_erro),
						Toast.LENGTH_SHORT).show();
			}
		}

	}

}

package com.bandlink.air.user;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import cn.jpush.android.api.JPushInterface;

import com.bandlink.air.MilinkApplication;
import com.bandlink.air.R;
import com.bandlink.air.SlideMainActivity;
import com.bandlink.air.simple.InitUserData;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.LovefitSlidingActivity;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Util;
import com.umeng.analytics.MobclickAgent;

public class Register extends Activity implements OnClickListener {

	TextView tv_usercontrol, tv_now_login;
	Button btn_register1, btn_trial;
	EditText user1, pwd1;
	String str_acc, str_pwd, result, lovefitid, mobile, email;
	String ValueFrom = "1", str_pwd_or;
	String uid, session_id;
	int code;
	private Dbutils db;
	SharedPreferences share;
	private ProgressDialog progressdialog;
	private int ismember;
	private String session_time, per;
	private String jpushid, intentValue;
	CheckBox checkBox;
	private long firstime = 0;
	private SharedPreferences airPreference;
	private Button sendCode;
	private EditText codeEdit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		View v = LayoutInflater.from(this).inflate(R.layout.register, null);
		LovefitSlidingActivity.changeBarColor(this, 0xff17B4EB, v);
		setContentView(v);
		ActionbarSettings ac = new ActionbarSettings(this,
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						startActivity(new Intent(Register.this,
								LoginActivity.class));
						Register.this.finish();
					}
				}, null);
		ac.setTitle(R.string.register);
		ac.setTopLeftIcon(R.drawable.ic_top_arrow);
		// ac.setRightText(R.string.login);
		share = getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		airPreference = getSharedPreferences("air", Context.MODE_MULTI_PROCESS);
		// tv_cancel = (TextView) findViewById(R.id.txt_return);
		tv_usercontrol = (TextView) findViewById(R.id.user_pro);
		// tv_login = (TextView) findViewById(R.id.txt_login);
		tv_now_login = (TextView) findViewById(R.id.have_account);
		checkBox = (CheckBox) findViewById(R.id.checkBox);
		tv_now_login.setOnClickListener(this);
		// tv_cancel.setOnClickListener(this);
		tv_usercontrol.setOnClickListener(this);
		// tv_login.setOnClickListener(this);
		user1 = (EditText) findViewById(R.id.account);
		pwd1 = (EditText) findViewById(R.id.pwd);
		codeEdit = (EditText) findViewById(R.id.code);
		btn_register1 = (Button) findViewById(R.id.register);
		btn_register1.setOnClickListener(this);
		btn_trial = (Button) findViewById(R.id.btn_trial);
		sendCode = (Button) findViewById(R.id.send);
		sendCode.setOnClickListener(this);
		//sendCode.setVisibility(View.VISIBLE);
		//codeEdit.setVisibility(View.VISIBLE);
		btn_trial.setOnClickListener(this);
		uid = share.getInt("UID", -1) + "";
		if (share.getInt("UID", -2) <= -2) {
			btn_trial.setVisibility(View.GONE);
		} else {
			btn_trial.setVisibility(View.GONE);
		}

		jpushid = share.getString("JPUSHID", null);
		if (jpushid == null) {
			jpushid = JPushInterface.getRegistrationID(this);
		}
		user1.setOnFocusChangeListener(new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {// 获得焦点
					// 在这里可以对获得焦点进行处理
					user1.setHint("");
				} else {// 失去焦点
						// 在这里可以对输入的文本内容进行有效的验证
					if (user1.getText().toString().trim().length() < 1) {
						user1.setHint(R.string.acc);
					}
				}
			}
		});
		pwd1.setOnFocusChangeListener(new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {// 获得焦点
					// 在这里可以对获得焦点进行处理
					pwd1.setHint("");
				} else {// 失去焦点
						// 在这里可以对输入的文本内容进行有效的验证
					if (pwd1.getText().toString().trim().length() < 1) {
						pwd1.setHint(R.string.psd);
					}
				}
			}
		});
		Intent getValue = getIntent();
		try {
			intentValue = getValue.getExtras().getString("value");
			per = getValue.getExtras().getString("per");
			if (intentValue != null) {
				// tv_cancel.setText(R.string.exit);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	int count = 60;
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				if (progressdialog != null && progressdialog.isShowing())
					progressdialog.dismiss();
				saveToPreferences();
				if (!share.getString("session_id", "").equals("")) {
					MilinkApplication app = (MilinkApplication) Register.this
							.getApplication();
					app.uploadMobileInfo();
				}
				Intent intent = new Intent();
				if (intentValue != null) {
					intent.putExtra("iti", 1);
				}
				if (per != null) {
					finish();
				} else {
					intent.setClass(Register.this, InitUserData.class);
					intent.putExtra("value", "re");
					startActivity(intent);
					finish();
				}

				Toast.makeText(getApplicationContext(),
						getString(R.string.register_ok), Toast.LENGTH_SHORT)
						.show();
				break;
			case 1:
				if (progressdialog != null && progressdialog.isShowing())
					progressdialog.dismiss();
				Toast.makeText(Register.this, getString(R.string.no_param),
						Toast.LENGTH_SHORT).show();
				break;
			case 2:
				if (progressdialog != null && progressdialog.isShowing())
					progressdialog.dismiss();
				Toast.makeText(Register.this,
						getString(R.string.wrong_username), Toast.LENGTH_SHORT)
						.show();
				break;
			case 3:
				if (progressdialog != null && progressdialog.isShowing())
					progressdialog.dismiss();
				Toast.makeText(Register.this, getString(R.string.user_exist),
						Toast.LENGTH_SHORT).show();
				break;
			case 4:
				if (progressdialog != null && progressdialog.isShowing())
					progressdialog.dismiss();
				Toast.makeText(Register.this,
						getString(R.string.register_erro), Toast.LENGTH_SHORT)
						.show();
				break;
			case 5:

				break;
			case 6:
				if (progressdialog != null && progressdialog.isShowing())
					progressdialog.dismiss();
				Toast.makeText(Register.this, getString(R.string.network_erro),
						Toast.LENGTH_SHORT).show();
				break;
			case 7:
				sendCode.setEnabled(false);
				// 发送验证码
				String code = msg.obj.toString();
				share.edit().putString("smsauth", code).commit();
				Toast.makeText(getApplicationContext(),
						getString(R.string.getsmsauth_ok), Toast.LENGTH_SHORT)
						.show();
				break;
			case 8:
				share.edit().putString("smsauth", null).commit();
				String number = user1.getText().toString();

				if (number.length() == 11 && number.startsWith("1")) {
					hasSent = true;
					getCodeByPhone(number);
				}
				break;
			case 9:
				count--;
				if (count <= 0) {
					sendCode.setText(R.string.getsmsauth);
					sendCode.setEnabled(true);
					count = 60;
					handler.removeMessages(9);
				} else {
					sendCode.setEnabled(false);
					sendCode.setText(count + "");
					handler.removeMessages(9);
					handler.sendEmptyMessageDelayed(9, 1000);
				}

				break;
			case 10:
				// 其他错误

				sendCode.setEnabled(true);
				Toast.makeText(getApplicationContext(),
						R.string.getsmsauth_error, Toast.LENGTH_SHORT).show();
				break;
			case 11:
				count = 60;
				Toast.makeText(getApplicationContext(),
						R.string.getsmsauth_error, Toast.LENGTH_SHORT).show();
				share.edit().putString("smsauth", null).commit();
				break;
			}
		}
	};

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

	public void btnRegister() {

		str_acc = user1.getText().toString();
		str_pwd = pwd1.getText().toString();
		str_acc = str_acc.trim();
		if (!checkBox.isChecked()) {
			Toast.makeText(this, getString(R.string.user_pro_error),
					Toast.LENGTH_SHORT).show();
			return;
		}

		if (!codeEdit.getText().toString()
				.equals(share.getString("smsauth", "-"))) {
//			Toast.makeText(Register.this, getString(R.string.sms_auth_error),
//					Toast.LENGTH_LONG).show();
//			return;
		}
		if (str_acc.length() > 0 && str_acc != null) {
			Pattern p = Pattern.compile("[0-9]*");
			Matcher m = p.matcher(str_acc);
			if (((str_acc.length() > 0) || isEmail(str_acc)) && str_acc != null
					&& str_acc.length() <= 40 && str_acc.length() >= 3
					&& str_pwd != null && str_pwd.length() >= 6
					&& str_pwd.length() <= 25 && checkBox.isChecked()) {
				jpushid = share.getString("JPUSHID", null);
				if (jpushid == null || jpushid.equals("")) {
					jpushid = JPushInterface.getRegistrationID(this);
				}
				progressdialog = Util.initProgressDialog(Register.this, true,
						getString(R.string.data_wait), null);
				new Thread(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						String urlStr = HttpUtlis.BASE_URL
								+ "/user/getRegister";
						str_pwd_or = str_pwd;
						str_pwd = LoginActivity.MD5(str_pwd);
						Map<String, String> map = new HashMap<String, String>();
						map.put("user", str_acc);
						map.put("pwd", str_pwd);
						map.put("from", ValueFrom);
						map.put("nick", str_acc);
						map.put("uid", share.getInt("UID", -1) + "");
						if (jpushid != null && !jpushid.equals(""))
							map.put("jpushid", jpushid);

						Message msg = handler.obtainMessage();
						try {
							result = HttpUtlis.getRequest(urlStr, map);
							if (result != null) {
								JSONObject json, ob, tarCon, device, accobj;
								json = new JSONObject(result);
								code = Integer.parseInt(json
										.getString("status"));
								if (code == 0) {
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
									int sex = Integer.parseInt(ob
											.getString("sex"));
									int height = Integer.parseInt(ob
											.getString("height"));
									int weight = Integer.parseInt(ob
											.getString("weight"));

									int year = share.getInt("year", 1990);
									int month = share.getInt("month", 1);
									db = new Dbutils(Integer.parseInt(uid),
											Register.this);
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
									msg.what = 0;
									handler.sendMessage(msg);
								} else if (code == 1) {
									msg.what = 1;
									handler.sendMessage(msg);

								} else if (code == 2) {
									msg.what = 2;
									handler.sendMessage(msg);
								} else if (code == 3) {
									msg.what = 3;
									handler.sendMessage(msg);
								} else {
									msg.what = 4;
									handler.sendMessage(msg);
								}
							}
						} catch (UnknownHostException e) {
							msg.what = 6;
							handler.sendMessage(msg);
						} catch (Exception e) {
							// TODO Auto-generated catch block\
							msg.what = 4;
							handler.sendMessage(msg);
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
						|| (isEmail(str_acc))) {
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
					|| (isEmail(str_acc))) {
				Toast.makeText(this, R.string.user_email_phone_error,
						Toast.LENGTH_SHORT).show();
			} else if (str_pwd == null || str_pwd.length() < 6
					|| str_pwd.length() > 25) {
				Toast.makeText(this, getString(R.string.accmanage_pwd_erro),
						Toast.LENGTH_SHORT).show();
			}
		}

	}

	public void saveToPreferences() {
		// TODO Auto-generated method stub
		airPreference.edit().putInt("UID", Integer.parseInt(uid)).commit();
		airPreference.edit().putString("USERNAME", str_acc).commit();
		airPreference.edit().putString("PASSWORD", str_pwd).commit();
		airPreference.edit().putString("PASSWORD_OR", str_pwd_or).commit();
		airPreference.edit().putInt("ISMEMBER", ismember).commit();

		SharedPreferences.Editor editor = share.edit();
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

	// public boolean isMobileNO(String mobiles) {
	// Pattern p = Pattern
	// .compile("^((13[0-9])|(17[0678])|(14[57])|(15[^4,\\D])|(18[0-9]))\\d{8}$");
	// Matcher m = p.matcher(mobiles);
	// return m.matches();
	// }

	// 判断email格式是否正确
	public static boolean isEmail(String email) {
		String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
		Pattern p = Pattern.compile(str);
		Matcher m = p.matcher(email);
		return m.matches();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			long secondtime = System.currentTimeMillis();
			try {
				if (intentValue != null && intentValue.equals("re")) {
					Intent mIntent = new Intent(this, LoginActivity.class);
					mIntent.putExtra("value", "re");
					startActivity(mIntent);
					this.finish();
					return true;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (secondtime - firstime > 3000) {
				Toast.makeText(this, getString(R.string.again_exit),
						Toast.LENGTH_SHORT).show();
				firstime = System.currentTimeMillis();
				return true;
			} else {
				exit();
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	public void exit() {
		Context c = this;
		if (Build.VERSION.SDK_INT >= 11) {
			c = new ContextThemeWrapper(c, android.R.style.Theme_Holo_Light);
		}
		AlertDialog.Builder aDailog = new AlertDialog.Builder(c);
		aDailog.setCancelable(false);
		aDailog.setTitle("Exit？");
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

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		// case R.id.txt_return:
		// if (intentValue != null) {
		// exit();
		// } else {
		// this.finish();
		// }
		// break;
		// case R.id.txt_login:
		// if (intentValue != null) {
		// Intent in = new Intent();
		// in.setClass(this, LoginActivity.class);
		// in.putExtra("value", "re");
		// startActivity(in);
		// this.finish();
		// } else {
		// Intent si = new Intent(this, LoginActivity.class);
		// startActivity(si);
		// this.finish();
		// }
		// break;
		case R.id.user_pro:
			Uri uri = Uri.parse("http://www.lovefit.com/agreement.html");
			startActivity(new Intent(Intent.ACTION_VIEW, uri));
			break;
		case R.id.have_account:
			if (intentValue != null) {
				Intent iin = new Intent();
				iin.setClass(this, LoginActivity.class);
				iin.putExtra("value", "re");
				startActivity(iin);
				this.finish();
			} else {
				Intent sa = new Intent(this, LoginActivity.class);
				startActivity(sa);
				this.finish();
			}
			break;
		case R.id.register:
			btnRegister();
			break;
		case R.id.btn_trial:
			Intent intent = new Intent(this, SlideMainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intent);
			this.finish();
			break;
		case R.id.send:
			String phone = user1.getText().toString();
			if (phone != null && phone.length() == 11 && phone.startsWith("1")) {
				getCodeByPhone(phone);
			} else {
				Toast.makeText(Register.this,
						getString(R.string.accmanage_tel_erro),
						Toast.LENGTH_SHORT).show();
			}

			break;
		default:
			break;
		}
	}

	int i = 0;
	boolean hasSent = false;

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
							handler.obtainMessage(7, code).sendToTarget();
						} else {
							if (i <= 3) {
								handler.obtainMessage(8).sendToTarget();
							} else {
								handler.obtainMessage(11).sendToTarget();
							}
						}
						handler.obtainMessage(9).sendToTarget();
					} else {
						handler.obtainMessage(10).sendToTarget();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					handler.obtainMessage(10).sendToTarget();
					e.printStackTrace();
				}
			}
		}).start();
	}
}

package com.bandlink.air;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bandlink.air.pingan.PinganRegisterActivity;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.MApplication;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Util;

public class DeviceSettingBinding extends LovefitActivity {
	private TextView name;
	private Button btn_scan, btn_input;
	private String type;

	private ImageView img;

	private String user;
	private int uid;
	private int devicetype;
	private Dbutils dbutil;
	private SharedPreferences share;
	private String from;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_device_binding);
		from = getIntent().getStringExtra("from");
		ActionbarSettings actionbar = new ActionbarSettings(this,
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						DeviceSettingBinding.this.finish();
					}
				}, null);
		actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
		actionbar.setTitle(R.string.setting_device_sportsdevice);
		initViews();
		initUser();
	}

	private void initUser() {
		if (user == null) {
			share = getSharedPreferences(SharePreUtils.APP_ACTION,
					Context.MODE_PRIVATE);
			user = share.getString("USERNAME", "lovefit");
			uid = share.getInt("UID", -1);
			dbutil = new Dbutils(uid, this);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		System.out.println("requestCode:" + requestCode + ";resultCode:"
				+ resultCode);
		if (resultCode == 20) {
			String str = data.getStringExtra("did");
			// Intent i = new Intent(this, InputWindowActivity.class);
			// i.putExtra("type", devicetype);
			// i.putExtra(
			// "did",
			// str.substring(str.indexOf("did=") + 4,
			// str.indexOf("&passwd")));
			// i.putExtra("pwd",
			// str.substring(str.indexOf("wd=") + 3, str.length()));
			// startActivity(i);
			// MApplication.getInstance().addActivity(DeviceSettingBinding.this);
			if (str.contains("did") && str.contains("passwd")) {
				String s = str.substring(str.indexOf("did=") + 4,
						str.indexOf("&passwd"));
				String s2 = str.substring(str.indexOf("wd=") + 3, str.length());
				if (s.startsWith("7288") && devicetype == 1) {
					if (!s.equals("") && !s2.equals("")) {
						if (checkBinding(s, s2)) {
							progressDialog = Util.initProgressDialog(
									DeviceSettingBinding.this, true,
									getString(R.string.data_wait), null);
							uploadBinding(s, s2);

						}

					} else {
						Toast.makeText(DeviceSettingBinding.this,
								R.string.device_bind_null, Toast.LENGTH_SHORT)
								.show();
					}
				} else {
					Toast.makeText(DeviceSettingBinding.this, "不是flame设备",
							Toast.LENGTH_SHORT).show();
				}

			}

		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void initViews() {
		name = (TextView) findViewById(R.id.set_binding_name);
		img = (ImageView) findViewById(R.id.set_device_img);
		ImageView logo = (ImageView) findViewById(R.id.img);
		btn_input = (Button) findViewById(R.id.set_btn_flame);

		btn_scan = (Button) findViewById(R.id.set_btn_scan);

		btn_scan.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent sacnintent = new Intent(DeviceSettingBinding.this,
						DeviceBindScanActivity.class);
				sacnintent.putExtra("from", 2);
				if (from != null) {
					sacnintent.putExtra("from", "init");
				}
				startActivityForResult(sacnintent, 100);
			}
		});
		btn_input.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// Intent i = new Intent(DeviceSettingBinding.this,
				// InputWindowActivity.class);
				// MApplication.getInstance().addActivity(DeviceSettingBinding.this);
				// i.putExtra("type", devicetype);
				// if(from!=null){
				// i.putExtra("from", "init");
				// }
				// startActivity(i);
				showInputDialog();
			}

		});
		Intent intent = getIntent();
		type = intent.getExtras().getString("name");
		if (intent != null && type != null) {

			if (type.contains("Flame")) {
				btn_scan.setText(R.string.setting_device_scan_2);
				name.setText(R.string.setting_device_lovefitflame);
				img.setImageResource(R.drawable.setting_flame);
				logo.setImageResource(R.drawable.flame_bind_text);
				devicetype = 1;
			} else if (type.contains("Ant")) {
				name.setText(R.string.setting_device_lovefitAnt);
				img.setImageResource(R.drawable.setting_ant);
				devicetype = 4;
				logo.setImageResource(R.drawable.ant_bind_text);
				btn_scan.setVisibility(View.GONE);
			} else if (type.contains("Air")) {
				devicetype = 5;
				name.setText(R.string.setting_device_lovefitAir);
				img.setImageResource(R.drawable.setting_air);
				// logo.setImageResource(R.drawable.air_bind_text);
			}
		}

	}

	private void showInputDialog() {
		Context context = Util.getThemeContext(this);
		AlertDialog.Builder ab = new AlertDialog.Builder(context);
		ab.setTitle(R.string.set_bindingdevice);
		View view = LayoutInflater.from(context).inflate(
				R.layout.layout_in_dialog, null);
		final EditText code = (EditText) view.findViewById(R.id.edittext1);
		final EditText pwd = (EditText) view.findViewById(R.id.edittext2);
		ab.setView(view);
		ab.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						String s = code.getText().toString();
						String s2 = pwd.getText().toString();
						if (!s.equals("") && !s2.equals("")) {
							if (checkBinding(s, s2)) {
								progressDialog = Util.initProgressDialog(
										DeviceSettingBinding.this, true,
										getString(R.string.data_wait), null);
								uploadBinding(s, s2);

							}

						} else {
							Toast.makeText(DeviceSettingBinding.this,
									R.string.device_bind_null,
									Toast.LENGTH_SHORT).show();
						}
					}
				});
		ab.setNegativeButton(R.string.can, null);
		ab.create().show();
	}

	private ProgressDialog progressDialog;
	Handler hand = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case 0:
				// 存至数据库
				if (progressDialog != null) {
					progressDialog.dismiss();
				}
				dbutil.setDeviceSpID((String) msg.obj);
				dbutil.setDeviceSpType(msg.arg1);

				if (from != null) {
					startActivity(new Intent(DeviceSettingBinding.this,
							SlideMainActivity.class));
				}
				DeviceSettingBinding.this.finish();
				try {
					MApplication.getInstance().getList().get(0).finish();
					MApplication.getInstance().clearAll();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case 1:
				if (progressDialog != null) {
					progressDialog.dismiss();
				}
				Toast.makeText(DeviceSettingBinding.this,
						getString(R.string.device_error), 1).show();

				break;
			case 2:
				if (progressDialog != null) {
					progressDialog.dismiss();
				}
				Toast.makeText(DeviceSettingBinding.this,
						getString(R.string.network_erro), 1).show();
				break;
			case 3:
				showMsg("抱歉！设备提交异常，请稍后再试");
				break;
			case 4:
				showMsg("抱歉！帐号提交异常，请稍后再试");
				break;
			}
			super.handleMessage(msg);
		}

	};

	void showMsg(String msg) {
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle(R.string.warning);
		ab.setMessage(msg);
		ab.setPositiveButton(R.string.ok, null);
		ab.show();
	}

	/***
	 * 上传绑定数据
	 * 
	 * @param id
	 *            设备id
	 * @param code
	 *            对应密码
	 */
	private void uploadBinding(final String id, final String code) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				// 接口 0->蓝牙 ，1-》flame， 2-》手机， 3->ble, 4-ant ， 5-》air
				//  
				Map<String, String> device = new HashMap<String, String>();
				device.put("session", share.getString("session_id", ""));
				device.put("devicetype", devicetype + "");
				device.put("deviceid", id);
				device.put("devicepwd", code);
				// if (share.getBoolean("ispa", false)) {
				// device.put("ispa", "1");
				// }
				try {
					String result = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/user/setUserDevice2", device);
					JSONObject js = new JSONObject(result);
					if (js.getString("status").equals("0")) {// 成功
						Message m = new Message();
						m.obj = id;
						m.what = 0;
						m.arg1 = devicetype;
						int ptype;
						if (devicetype == 1) {
							ptype = 2;
						} else {
							ptype = 1;
						}
						if (share.getBoolean("ispa", false)) {
							String token = share.getString("pingan_token", "");
							String vname = share.getString("vnumber", "");
							String username = js.getString("content");
							String res = bindDevicePa(token, vname, ptype);
							// {"ret":"0","msg":"","requestId":"null","data":"{"returnCode":"01","message":"无效或没有绑定账号，请确认！"}"}

							if (res.contains("returnCode\":\"00")) {
								hand.sendMessage(m);
							} else if (res.contains("returnCode\":\"01")) {
								if (username != null && username.length() > 0) {
									String retr = bindAccountPa(token, vname,
											username);
									if (retr.contains("returnCode\":\"00")) {
										String res2 = bindDevicePa(token,
												vname, ptype);
										if (res2.contains("returnCode\":\"01")) {
											// 绑定帐号成功 绑定设备error
											hand.sendEmptyMessage(3);
											return;
										}
									} else {
										// 绑定帐号 error
										hand.sendEmptyMessage(4);
										return;
									}
								} else {
									// 没有用户名？
								}
							}
							System.out.println(res);

							hand.sendMessage(m);
						} else {
							// 保存
							hand.sendMessage(m);
						}

					} else if (js.getString("status").equals("3")) {
						hand.sendEmptyMessage(1);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					hand.sendEmptyMessage(2);
					e.printStackTrace();
				}
			}
		}).start();

	}

	public static String bindDevicePa(String token, String vname, int ptype)
			throws Exception {
		String url = PinganRegisterActivity.PINGAN_BASE_URL
				+ "/open/appsvr/health/partner/bind/device/0330000?access_token="
				+ token;
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("partyNo", vname);
		map.put("partnerCode", "0330000");
		map.put("deviceType", "" + ptype);
		String res = HttpUtlis.getRequestForPost(url, map);
		return res;
	}

	public static String bindAccountPa(String token, String vname,
			String account) throws Exception {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("access_token", token);
		params.put("partyNo", vname);
		params.put("partnerCode", "0330000");
		params.put("partnerMemberNo", account);
		String res = HttpUtlis
				.getRequestForPost(
						PinganRegisterActivity.PINGAN_BASE_URL
								+ "/open/appsvr/health/partner/bind/account/0330000?access_token="
								+ token, params);
		return res;
	}

	private boolean checkBinding(String did, String pass) {
		Pattern p1, p2;
		Matcher m1, m2;
		p1 = Pattern.compile("^\\d{4}[0-9.a-z,A-Z]\\d{10}$");
		p2 = Pattern.compile("^\\d{6}$");
		m1 = p1.matcher(did);
		m2 = p2.matcher(pass);
		Toast msg;
		if (!m1.matches()) {
			msg = Toast.makeText(this, R.string.device_bind_didformat,
					Toast.LENGTH_SHORT);
			msg.setGravity(Gravity.CENTER, 0, 0);
			msg.show();
			return false;
		} else if (!m2.matches()) {
			msg = Toast.makeText(this, R.string.device_bind_pwdformat,
					Toast.LENGTH_SHORT);
			msg.setGravity(Gravity.CENTER, 0, 0);
			msg.show();
			return false;
		}

		return true;
	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub

	}
}

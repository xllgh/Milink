package com.bandlink.air;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import android.app.Activity;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.MApplication;
import com.bandlink.air.util.SharePreUtils;

public class InputWindowActivity extends Activity {
	private TextView can, ok;
	private EditText edit_code, edit_pwd;
	private SharedPreferences share;
	private Dbutils dbutil;
	private String user;
	private int uid;
	private int devicetype;
	private ProgressDialog progressDialog;
	private String from;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setContentView(R.layout.pop_setting);
		share = getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		devicetype = getIntent().getIntExtra("type", 0);
from = getIntent().getStringExtra("from");
		user = share.getString("USERNAME", "lovefit");
		uid = share.getInt("UID", -1);
		dbutil = new Dbutils(uid, this);
		ok = (TextView) findViewById(R.id.btn_ok);
		can = (TextView) findViewById(R.id.btn_can);
		edit_code = (EditText) findViewById(R.id.input_code);
		edit_pwd = (EditText) findViewById(R.id.input_pwd);
		if (getIntent().getStringExtra("did") != null
				&& getIntent().getStringExtra("did").length() > 5) {
			edit_code.setText(getIntent().getStringExtra("did"));
			edit_pwd.setText(getIntent().getStringExtra("pwd"));
		}
		ok.setBackgroundColor(getResources().getColor(R.color.blue_ui));
		ok.setTextColor(getResources().getColor(R.color.white));
		ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				can.setBackgroundColor(getResources().getColor(
						android.R.color.transparent));
				ok.setBackgroundColor(getResources().getColor(R.color.blue_ui));
				ok.setTextColor(getResources().getColor(R.color.white));
				can.setTextColor(getResources().getColor(R.color.blue_ui));

				String s = edit_code.getText().toString();
				String s2 = edit_pwd.getText().toString();
				if (!s.equals("") && !s2.equals("")) {
					if (checkBinding(s, s2)) {
						progressDialog = initProgressDialog();
						uploadBinding(s, s2);

					}

				} else {
					Toast.makeText(InputWindowActivity.this,
							R.string.device_bind_null, Toast.LENGTH_SHORT)
							.show();
				}

			}
		});
		can.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ok.setBackgroundColor(getResources().getColor(
						android.R.color.transparent));
				can.setBackgroundColor(getResources().getColor(R.color.blue_ui));
				can.setTextColor(getResources().getColor(R.color.white));
				ok.setTextColor(getResources().getColor(R.color.blue_ui));
				InputWindowActivity.this.finish();
			}
		});

		super.onCreate(savedInstanceState);
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
				// app选项中 0-》手机，1-》air，2-》flame，3-》ant
				Map<String, String> device = new HashMap<String, String>();
				device.put("session", share.getString("session_id", ""));
				device.put("devicetype", devicetype + "");
				device.put("deviceid", id);
				device.put("devicepwd", code);
				try {
					String result = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/user/setUserDevice", device);
					JSONObject js = new JSONObject(result); 
					if (js.getString("status").equals("0")) {// 成功
						Message m = new Message();
						m.obj = id;
						m.what = 0;
						m.arg1 = devicetype;
						// 保存
						hand.sendMessage(m);
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

	public ProgressDialog initProgressDialog() {
		ProgressDialog progressDialog = ProgressDialog.show(this,
				getString(R.string.data_wait),
				getString(R.string.data_getting), true);
		progressDialog.setCancelable(true);

		LayoutInflater inflater = LayoutInflater.from(this
				.getApplicationContext());
		View v = inflater.inflate(R.layout.loading, null);// 寰楀埌鍔犺浇view
		TextView tvMsg = (TextView) v.findViewById(R.id.tvTitle);

		if (tvMsg != null) {
			tvMsg.setText(getString(R.string.data_getting));
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
				
				if(from!=null){
					startActivity(new Intent(InputWindowActivity.this,SlideMainActivity.class));
				}
				InputWindowActivity.this.finish();
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
				Toast.makeText(InputWindowActivity.this,
						getString(R.string.device_error), 1).show();

				break;
			case 2:
				if (progressDialog != null) {
					progressDialog.dismiss();
				}
				Toast.makeText(InputWindowActivity.this,
						getString(R.string.network_erro), 1).show();
				break;
			}
			super.handleMessage(msg);
		}

	};
}

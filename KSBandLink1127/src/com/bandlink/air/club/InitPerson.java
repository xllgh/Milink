package com.bandlink.air.club;

import java.util.HashMap;
import java.util.Map;

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
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bandlink.air.R;
import com.bandlink.air.SlideMainActivity;
import com.bandlink.air.SportsDevice;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.view.HeightRuler;
import com.bandlink.air.view.NoRegisterDialog;
import com.bandlink.air.view.WeightRuler;

public class InitPerson extends LovefitActivity implements OnClickListener {

	private TextView etHeight, etWeight;
	private Dbutils db;
	private double weight, reweight;
	private int height, reheight;
	private Button finish;
	private int tag1 = 1;
	private SharedPreferences share;
	private NoRegisterDialog dialog;
	private int ismember;
	private ProgressDialog progressdialog;
	private Context mContext;
	private TextView birth;
	private int flag = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.personalsetting);
		ActionbarSettings actionbar = new ActionbarSettings(this, null, null);
		actionbar.setTopLeftIcon(R.drawable.none);
		actionbar.setTitle(R.string.finishpersonaldata);
		share = getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		flag = getIntent().getIntExtra("flag", -1);
		if (android.os.Build.VERSION.SDK_INT >= 11) {
			mContext = new ContextThemeWrapper(this,
					android.R.style.Theme_Holo_Light);
		} else {
			mContext = this;
		}
		db = new Dbutils(share.getInt("UID", -1), this);

		
		// _id,uid,nickname,ismember,sex,height,weight,contact
		ismember = share.getInt("ISMEMBER", 0);
		initViews();
	}

	private void initViews() {
		finish = (Button) findViewById(R.id.finish);
		
		birth = (TextView) findViewById(R.id.birth);
		etHeight = (TextView) findViewById(R.id.et_height);
		etWeight = (TextView) findViewById(R.id.et_weight);
		
		etHeight.setOnClickListener(this);
		etWeight.setOnClickListener(this);
		birth.setOnClickListener(this);
		int year = 1990;
		int month = 1;
		birth.setText(year + getString(R.string.year) + (month)
				+ getString(R.string.month));
		height = 170;
		weight = 60.0;
		etHeight.setText(height + "");
		etWeight.setText(weight + "");
		if (share.getInt("ISMEMBER", 0) == 0) {
			LinearLayout l1 = (LinearLayout) findViewById(R.id.linear1);
			LinearLayout l2 = (LinearLayout) findViewById(R.id.linear2);
			LinearLayout l3 = (LinearLayout) findViewById(R.id.linear3);

			l1.setVisibility(View.GONE);
			l2.setVisibility(View.GONE);
			l3.setVisibility(View.GONE);
			finish.setOnClickListener(this);
			finish.setVisibility(View.VISIBLE);
//			dialog = new NoRegisterDialog(this, R.string.no_register,
//					R.string.no_register_content);
//			dialog.show();
		}else{
			finish.setText(R.string.next);
			
			finish.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					startActivity(new Intent(InitPerson.this,SportsDevice.class));
				}
			});
		}

	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.et_height:
			Intent intent5 = new Intent();
			intent5.setClass(InitPerson.this, HeightRuler.class);
			intent5.putExtra("height", height);
			intent5.putExtra("uheight", etHeight.getText().toString());
			this.startActivityForResult(intent5, 5);

			break;
		case R.id.et_weight:
			Intent intent4 = new Intent();
			intent4.setClass(InitPerson.this, WeightRuler.class);
			intent4.putExtra("startEdit1", weight);
			intent4.putExtra("title", getString(R.string.weight_ruler_title3));
			intent4.putExtra("start_w", etWeight.getText().toString());
			this.startActivityForResult(intent4, 4);
			break;
		case R.id.finish:
			if (flag == 0) {
				reheight = Integer.valueOf(etHeight.getText().toString());
				reweight = Double.valueOf(etWeight.getText().toString());
				db.UpdateUserInfo("", tag1, reheight, reweight);

				InitPerson.this.finish();
				break;
			}

			progressdialog = initProgressDialog();
			reheight = Integer.valueOf(etHeight.getText().toString());
			reweight = Double.valueOf(etWeight.getText().toString());
			upLoadUserInfo();

			break;
		case R.id.birth:
			int year = share.getInt("year", 1990);
			int month = share.getInt("month", 1);
			final DatePicker date = new DatePicker(mContext);
			date.init(year, month - 1, 1, null);
			((ViewGroup) date.getChildAt(0)).getChildAt(1).setVisibility(
					View.GONE);
			// 隐藏 viewid 16908947
			String lan = getResources().getConfiguration().locale
					.getISO3Language();
			String country = getResources().getConfiguration().locale
					.getCountry();
			//
			int hide = 2;
			if (lan.contains("zh")) {
				hide = 2;
			} else if (lan.contains("en")) {
				if (country.contains("US")) {
					hide = 1;
				} else {
					hide = 0;
				}
			}
			((ViewGroup) ((ViewGroup) date.getChildAt(0)).getChildAt(0))
					.getChildAt(hide).setVisibility(View.GONE);

			AlertDialog.Builder ab = new AlertDialog.Builder(mContext);
			ab.setView(date);
			ab.setTitle(R.string.selectbrith);
			ab.setPositiveButton(R.string.finish,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub

							share.edit().putInt("year", date.getYear())
									.commit();
							share.edit().putInt("month", date.getMonth() + 1)
									.commit();
							birth.setText(date.getYear()
									+ getString(R.string.year)
									+ (date.getMonth() + 1)
									+ getString(R.string.month));
							dialog.dismiss();
						}
					});
			ab.create().show();
			break;
		default:
			break;
		}
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

	private void upLoadUserInfo() {

		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Map<String, String> up = new HashMap<String, String>();
				up.put("session", share.getString("session_id", ""));
				up.put("weight", reweight + "");
				up.put("height", reheight + "");
				try {
					String result = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/user/setUserInfo", up);
					JSONObject ob = new JSONObject(result);
					if (ob.getInt("status") == 0) {
						myHandler.obtainMessage(2).sendToTarget();
					} else {
						myHandler.obtainMessage(0).sendToTarget();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	Handler myHandler = new Handler() {
		@Override
		public void dispatchMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case 0:
				if (progressdialog != null && progressdialog.isShowing())
					progressdialog.dismiss();
				Toast.makeText(InitPerson.this, getString(R.string.err),
						Toast.LENGTH_SHORT).show();
				break;
			case 1:
				break;
			case 2:
				if (progressdialog != null && progressdialog.isShowing())
					progressdialog.dismiss();
				db.UpdateUserInfo("", tag1, reheight, reweight);
				Intent intent = new Intent();
				startActivity(intent.setClass(InitPerson.this,
						SlideMainActivity.class));
				InitPerson.this.finish();
				break;

			default:
				break;
			}
			super.dispatchMessage(msg);
		}

	};

	private OnClickListener lsl = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			InitPerson.this.finish();
		}
	};

	public void getUserProfiel() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Map<String, String> targetargs = new HashMap<String, String>();
					targetargs
							.put("session", share.getString("session_id", ""));
					String userInfo = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/user/getUserInfo", targetargs);

					JSONObject InfoStatus = new JSONObject(userInfo);
					if (InfoStatus.getInt("status") == 0) {
						JSONObject Info = new JSONObject(new JSONObject(
								userInfo).get("content").toString());
						db.UpdateUserInfo(Info.getString("nickname"),
								Info.getInt("sex"), Info.getDouble("height"),
								Info.getDouble("weight"));
					}

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					Message m = new Message();
					m.what = 0;
					myHandler.sendMessage(m);
				}

			}
		}).start();

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub

		super.onResume();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case 5:
			try {
				String h = data.getStringExtra("uheight");
				reheight = Integer.parseInt(h);
				etHeight.setText(h);
			} catch (Exception e) {

			}
			break;
		case 4:
			try {
				String w = data.getStringExtra("startEdit");
				reweight = Double.parseDouble(w);
				etWeight.setText(w);
			} catch (Exception e) {

			}

			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub
		
	}

}
package com.bandlink.air;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.MApplication;
import com.bandlink.air.util.SharePreUtils;
import com.milink.android.lovewalk.bluetooth.service.StepService;

public class SportsDevice extends LovefitActivity {
	private ListView listview;

	private BaseAdapter mAdapter;
	private LayoutInflater inflater;
	private ArrayList<HashMap<String, Object>> list;
	private int deviceType;
	private Dbutils db;
	private SharedPreferences share;
	private String from;

	// private List<ActivityManager.RunningServiceInfo> serviceList;
	// private boolean isOpenStep;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mall);
		ActionbarSettings actionbar = new ActionbarSettings(this,
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						SportsDevice.this.finish();
					}
				}, null);
		from = getIntent().getStringExtra("from");
		actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
		actionbar.setTitle(R.string.setting_device_sportsdevice);
		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		listLayout();
		db = new Dbutils(SharePreUtils.getInstance(this).getUid(), this);
		deviceType = db.getUserDeivceType();
		share = getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		// serviceList = activityManager.getRunningServices(100);
		// isOpenStep =
		// serviceIsStart("com.milink.android.lovewalk.bluetooth.service.StepService");
	}

	// public boolean serviceIsStart(String serviceClassName) {
	// for (int i = 0; i < serviceList.size(); i++) {
	// MyLog.e("service", serviceList.get(i).service.getClassName());
	// if (serviceClassName.equals(serviceList.get(i).service
	// .getClassName())) {
	//
	// return true;
	// }
	// }
	// return false;
	// }

	public void listLayout() {
		listview = (ListView) findViewById(R.id.set_listView1);
		list = new ArrayList<HashMap<String, Object>>();

		HashMap<String, Object> map = new HashMap<String, Object>();
		map = new HashMap<String, Object>();
		map.put("proImage", R.drawable.airs);
		map.put("proName", getString(R.string.setting_device_band));
		map.put("proContent",
				getString(R.string.setting_device_lovefitAir_info));
		list.add(map);

		map = new HashMap<String, Object>();
		map.put("proImage", R.drawable.ic_launcher);
		map.put("proName", getString(R.string.soft_step));
		map.put("proContent", getString(R.string.soft_step_describe));
		list.add(map);
		map = new HashMap<String, Object>();
		map.put("proImage", R.drawable.setting_flame);
		map.put("proName", getString(R.string.setting_device_lovefitflame));
		map.put("proContent",
				getString(R.string.setting_device_lovefitflame_info));
		list.add(map);

		map = new HashMap<String, Object>();
		map.put("proImage", R.drawable.setting_ant);
		map.put("proName", getString(R.string.setting_device_lovefitAnt));
		map.put("proContent",
				getString(R.string.setting_device_lovefitAnt_info));
		list.add(map);

		mAdapter = new MyAdapter();
		listview.setAdapter(mAdapter);

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		// MobclickAgent.onPause(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// MobclickAgent.onResume(this);
	}

	public void setDeviceType() {
		new Thread() {
			public void run() {
				String urlStr = HttpUtlis.BASE_URL + "/user/setUserDeviceType";
				String w2 = share.getString("session_id", "");
				Map<String, String> map = new LinkedHashMap<String, String>();
				map.put("session", w2);
				map.put("devicetype", 2 + "");
				try {
					String result = HttpUtlis.getRequest(urlStr, map);
					JSONObject json = new JSONObject(result);
					if (json.getInt("status") == 0) {
						Dbutils db = new Dbutils(SportsDevice.this);
						db.setDeviceSpType(2);
						db.setDeviceSpID("");
						Intent intent = new Intent("milinkStartService");
						sendBroadcast(intent);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					// 没有网也能启用
					Dbutils db = new Dbutils(SportsDevice.this);
					db.setDeviceSpType(2);
					db.setDeviceSpID("");
					Intent intent = new Intent("milinkStartService");
					sendBroadcast(intent);
				}
				super.run();
			}

		}.start();
	}

	public class MyAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			final int mposition = position;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.listview_item_mall,
						null);
				holder = new ViewHolder();
				convertView.setTag(holder);
				holder.name = (TextView) convertView.findViewById(R.id.proname);
				holder.content = (TextView) convertView
						.findViewById(R.id.procontent);
				holder.image = (ImageView) convertView.findViewById(R.id.img);
				holder.btn = (Button) convertView.findViewById(R.id.open);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.name.setText((String) list.get(position).get("proName"));
			holder.content.setText((String) list.get(position)
					.get("proContent"));
			holder.image.setImageResource((Integer) list.get(position).get(
					"proImage"));
			String dname = list.get(mposition).get("proName").toString();
			if (deviceType == 2 && dname.equals(getString(R.string.soft_step))) {
				if (StepService.isRun) {
					holder.btn.setText(R.string.softstep_close);
				} else {
					holder.btn.setText(R.string.softstep_open);
				}
			} else {
				holder.btn.setText(R.string.set_select);
			}
			// holder.btn.setText(R.string.set_select);
			holder.btn.setOnClickListener(new Button.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					String devicename = list.get(mposition).get("proName")
							.toString();
					if (devicename
							.equals(getString(R.string.setting_device_band))) {
						// 蓝牙相关，需要扫描设备
						if (!getPackageManager().hasSystemFeature(
								PackageManager.FEATURE_BLUETOOTH_LE)) {
							Toast.makeText(SportsDevice.this,
									R.string.ble_not_supported_air,
									Toast.LENGTH_LONG).show();
							return;
						}
						Intent i = new Intent(SportsDevice.this,
								DeviceSettingBindingBLE.class);
						if (from != null) {
							i.putExtra("from", "init");
						}
						i.putExtra("name", devicename);

						startActivity(i);
					} else if (devicename.equals(getString(R.string.soft_step))) {
						// isOpenStep =
						// serviceIsStart("com.milink.android.lovewalk.bluetooth.service.StepService");
						if (StepService.isRun) {
							Intent intent2 = new Intent("MilinkConfig");
							intent2.putExtra("command", 2);
							sendBroadcast(intent2);
							Intent intents = new Intent(SportsDevice.this,
									StepService.class);
							stopService(intents);
						} else {
							db.setDeviceSpType(2);
							setDeviceType();
							if (from != null) {
								for (Activity ac : MApplication.getInstance()
										.getList()) {
									try {
										ac.finish();
									} catch (Exception e) {

									}
								}
								startActivity(new Intent(SportsDevice.this,
										SlideMainActivity.class));
							}
						}

					} else {
						Intent i = new Intent(SportsDevice.this,
								DeviceSettingBinding.class);
						i.putExtra("name", devicename);
						if (from != null) {
							i.putExtra("from", "init");
						}
						startActivity(i);
					}

					SportsDevice.this.finish();

				}
			});

			return convertView;
		}

		private final class ViewHolder {
			TextView name;
			TextView content;
			ImageView image;
			Button btn;
		}

	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub

	}

}

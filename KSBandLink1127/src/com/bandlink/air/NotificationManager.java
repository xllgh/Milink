package com.bandlink.air;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.bandlink.air.ble.AirNotificationListener;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.ViewHolder;

public class NotificationManager extends LovefitActivity {

	private ArrayList<HashMap<String, String>> data;
	private ListView listview;
	private Dbutils dbUtils;
	private MSimpleAdapter adapter;
	private ActionbarSettings actionBar;
	private ArrayList<String> enableList;
	private ArrayList<HashMap<String, String>> temp = new ArrayList<HashMap<String, String>>();
	ArrayList<HashMap<String, String>> sysList = new ArrayList<HashMap<String, String>>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setContentView(R.layout.notification_manager);
		findViewById(R.id.go).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(
						"android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
				startActivity(intent);
			}
		});
		listview = (ListView) findViewById(R.id.listview);
		dbUtils = new Dbutils(SharePreUtils.getInstance(this).getUid(), this);
		enableList = dbUtils.getEnableList();
		actionBar = new ActionbarSettings(this, new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				NotificationManager.this.finish();
			}
		}, new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

			}
		});
		actionBar.setTitle(R.string.app_notification_title);
		actionBar.setTopLeftIcon(R.drawable.ic_top_arrow);
		data = new ArrayList<HashMap<String, String>>();
		new Thread(new Runnable() {
			public void run() {

				List<PackageInfo> packages = getPackageManager()
						.getInstalledPackages(0);

				System.out.println(new Date().getTime());

				for (int i = 0; i < packages.size(); i++) {

					PackageInfo packageInfo = packages.get(i);
					HashMap<String, String> application = new HashMap<String, String>();
					if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
						if (enableList.contains(packageInfo.packageName)) {
							application.put("disable", "1");
						} else {
							application.put("disable", "0");
						}

						application.put("appName", packageInfo.applicationInfo
								.loadLabel(getPackageManager()).toString());
						application.put("packageName", packageInfo.packageName);
						sysList.add(application);
					} else if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
						if (enableList.contains(packageInfo.packageName)) {
							application.put("disable", "1");
						} else {
							application.put("disable", "0");
						}
						application.put("appName", packageInfo.applicationInfo
								.loadLabel(getPackageManager()).toString());
						application.put("packageName", packageInfo.packageName);
						temp.add(application);
					}

					// packageInfo.applicationInfo.loadIcon(getPackageManager());

				}
				runOnUiThread(new Runnable() {
					public void run() {
						data.clear();

						HashMap<String, String> aa = new HashMap<String, String>();
						aa.put("appName", "常规应用");
						data.add(aa);
						data.addAll(temp);

						HashMap<String, String> application = new HashMap<String, String>();
						application.put("appName", "内置应用");
						data.add(application);
						data.addAll(sysList);
						adapter.notifyDataSetChanged();
					}
				});

			}
		}).start();
		System.out.println(new Date().getTime());

		adapter = new MSimpleAdapter(this, data, R.layout.list_manager,
				new String[] { "appName", "packageName" }, new int[] {
						R.id.text1, R.id.text2 });
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if (data.get(position).containsKey("disable")) {
					String dis = data.get(position).get("disable");
					if (!"1".equals(dis)) {
						// showMyDialog(position);
						dbUtils.saveNotification(
								data.get(position).get("appName"),
								data.get(position).get("packageName"), "1");
						data.get(position).put("disable", "1");
					} else {
						dbUtils.saveNotification(
								data.get(position).get("appName"),
								data.get(position).get("packageName"), "0");
						data.get(position).put("disable", "0");

					}
					adapter.notifyDataSetChanged();
					listview.postInvalidate();
				}

			}
		});
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		sendBroadcast(new Intent(
				AirNotificationListener.ACTION_AIR_ENABLELIST_CHANGED));
		super.onPause();
	}

	private void showMyDialog(final int position) {
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle(R.string.warning);
		ab.setMessage("确认添加到过滤列表？");
		ab.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dbUtils.saveNotification(
								data.get(position).get("appName"),
								data.get(position).get("packageName"), "1");
						handler.sendEmptyMessage(position);
					}
				});
		ab.setNegativeButton(R.string.cancel, null);
		ab.create().show();
	}

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what >= 0) {
				data.get(msg.what).put("disable", "1");
				adapter.notifyDataSetChanged();
				listview.postInvalidate();
			}
			super.handleMessage(msg);
		};
	};

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub

	}

	class MSimpleAdapter extends SimpleAdapter {
		private Context context;

		public MSimpleAdapter(Context context,
				List<? extends Map<String, ?>> data, int resource,
				String[] from, int[] to) {

			super(context, data, resource, from, to);
			this.context = context;
			// TODO Auto-generated constructor stub
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return super.getItem(position);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if (convertView == null) {
				convertView = LayoutInflater.from(context).inflate(
						R.layout.list_manager, null);

			}
			final HashMap<String, String> map = (HashMap<String, String>) this
					.getItem(position);
			ImageView icon = (ImageView) ViewHolder.get(convertView, R.id.icon);
			CheckBox disable = (CheckBox) ViewHolder.get(convertView,
					R.id.disable);
			TextView appn = (TextView) ViewHolder.get(convertView, R.id.text1);

			TextView packetn = (TextView) ViewHolder.get(convertView,
					R.id.text2);
			appn.setText(map.get("appName"));
			if (TextUtils.isEmpty(map.get("packageName"))) {
				icon.setVisibility(View.GONE);
				appn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources()
						.getDimensionPixelSize(R.dimen.text_large));
				disable.setVisibility(View.GONE);
				packetn.setVisibility(View.GONE);
			} else {
				icon.setVisibility(View.VISIBLE);

				appn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources()
						.getDimensionPixelSize(R.dimen.text_middle_s));
				disable.setVisibility(View.VISIBLE);
				packetn.setVisibility(View.VISIBLE);
			}
			packetn.setText(map.get("packageName"));
			try {

				PackageInfo info = this.context.getPackageManager()
						.getPackageInfo(map.get("packageName"), 0);
				icon.setImageDrawable(info.applicationInfo
						.loadIcon(getPackageManager()));
				if (map.containsKey("disable")) {
					disable.setVisibility(View.VISIBLE);
					if ("0".equals(map.get("disable"))) {
						disable.setChecked(false);
						System.out.println(false + map.get("appName"));
					} else {
						disable.setChecked(true);
						System.out.println(true + map.get("appName"));
					}
				} else {
					disable.setVisibility(View.GONE);
				}

				// disable.setOnCheckedChangeListener(new
				// OnCheckedChangeListener() {
				//
				// @Override
				// public void onCheckedChanged(CompoundButton buttonView,
				// boolean isChecked) {
				// // TODO Auto-generated method stub
				// if (isChecked) {
				// dbUtils.saveNotification(map.get("appName"),
				// map.get("packageName"), "0");
				// } else {
				// dbUtils.saveNotification(map.get("appName"),
				// map.get("packageName"), "1");
				// }
				// }
				// });
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return convertView;
		}
	}

}

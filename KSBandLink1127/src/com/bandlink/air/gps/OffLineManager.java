package com.bandlink.air.gps;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.bandlink.air.R;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.LovefitActivity;

public class OffLineManager extends LovefitActivity implements
		MKOfflineMapListener {
	// 本地已添加的下载项
	private ArrayList<MKOLUpdateElement> localMapList = null;
	private ArrayList<City> localData;
	private MKOfflineMap mOffline = null;
	// 省份级别
	private ArrayList<String> groupData;
	// 包括组，及其子项
	private ArrayList<ArrayList<City>> allData;
	private ExpandableListView eList;
	private ActionbarSettings actionbar;
	private CityAdapter cAdapter;
	private boolean fristIn = true;

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		Context c = this;
		if (Build.VERSION.SDK_INT >= 11) {
			c = new ContextThemeWrapper(this, android.R.style.Theme_Holo_Light);
		}
		if (!isWifi(this) && fristIn) {
			fristIn = false;
			AlertDialog.Builder ab = new AlertDialog.Builder(c);
			ab.setTitle(getString(R.string.warning));
			ab.setMessage(getString(R.string.nowifi));
			ab.setNegativeButton(R.string.isee,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub

						}
					});
			ab.show();
		}
		super.onWindowFocusChanged(hasFocus);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		mOffline = new MKOfflineMap();
		MapView m = new MapView(OffLineManager.this);
		mOffline.init(this);
		setContentView(R.layout.offline_manager);
		actionbar = new ActionbarSettings(this, new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				OffLineManager.this.finish();
			}
		}, null);
		actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
		actionbar.setTitle(R.string.offline_map);
		// actionbar.setTopRightIcon(R.drawable.download);
		initView();
	}

	private void initView() {
		// cidView = (TextView) findViewById(R.id.cityid);
		// cityNameView = (EditText) findViewById(R.id.city);
		// stateView = (TextView) findViewById(R.id.state);
		eList = (ExpandableListView) findViewById(R.id.itemlist);
		// ListView hotCityList = (ListView) findViewById(R.id.hotcitylist);
		ArrayList<City> hotCities = new ArrayList<City>();
		groupData = new ArrayList<String>();
		allData = new ArrayList<ArrayList<City>>();

		// 获取已下过的离线地图信息
		localMapList = mOffline.getAllUpdateInfo();
		if (localMapList != null) {
			localData = new ArrayList<City>();
			for (MKOLUpdateElement m : localMapList) {
				City c = new City(m.cityName, formatDataSize(m.serversize),
						m.cityID);
				c.setProgress(m.ratio);

				c.setDownLoading(1);

				localData.add(c);
			}
		}
		// 获取热门城市列表
		ArrayList<MKOLSearchRecord> records1 = mOffline.getHotCityList();

		if (records1 != null) {
			for (MKOLSearchRecord r : records1) {
				City c = new City(r.cityName, formatDataSize(r.size), r.cityID);
				if (localData != null && localData.contains(c)) {
					// 如果该城市已下载
					hotCities.add(localData.get(localData.indexOf(c)));
				} else {
					hotCities.add(c);
				}

			}
		}

		// 获取所有支持离线地图的城市
		ArrayList<String> allCities = new ArrayList<String>();
		ArrayList<MKOLSearchRecord> records2 = mOffline.getOfflineCityList();
		if (records2 != null) {
			for (MKOLSearchRecord r : records2) {
				allCities.add(r.cityName + "(" + r.cityID + ")" + "   --"
						+ this.formatDataSize(r.size));
				if (!compareCity(r.cityName)) {
					// 常规的省份
					groupData.add(r.cityName);
					ArrayList<City> tempCity = new ArrayList<City>();
					for (MKOLSearchRecord city : r.childCities) {

						City c = new City(city.cityName,
								formatDataSize(city.size), city.cityID);
						if (localData != null && localData.contains(c)) {
							// 如果该城市已下载
							tempCity.add(localData.get(localData.indexOf(c)));
						} else {
							tempCity.add(c);
						}
					}
					allData.add(tempCity);
				} else {
					// 如全国概览包，天津上海等直辖市，特别行政区。
					City c = new City(r.cityName, formatDataSize(r.size),
							r.cityID);
					if (!hotCities.contains(c)) {
						if (localData != null && localData.contains(c)) {
							// 如果该城市已下载
							hotCities.add(localData.get(localData.indexOf(c)));
						} else {
							hotCities.add(c);
						}
					}

				}
			}
		}
		allData.add(hotCities);
		groupData.add(getString(R.string.offline_hotcity));
		ArrayList<String> s = new ArrayList<String>();
		s.add(groupData.get(groupData.size() - 1));
		s.addAll(groupData.subList(0, groupData.size() - 1));
		ArrayList<ArrayList<City>> li = new ArrayList<ArrayList<City>>();
		li.add(allData.get(allData.size() - 1));
		li.addAll(allData.subList(0, allData.size() - 1));
		groupData.clear();
		allCities.clear();
		groupData = s;
		allData = li;
		cAdapter = new CityAdapter(groupData, allData, this);
		eList.setAdapter(cAdapter);
		// eList.setOnChildClickListener(new OnChildClickListener() {
		//
		// @Override
		// public boolean onChildClick(ExpandableListView parent, View v,
		// int groupPosition, int childPosition, long id) {
		// // TODO Auto-generated method stub
		// mOffline.start(allData.get(groupPosition).get(childPosition).code);
		// Toast.makeText(
		// getApplicationContext(),
		// "开始下载"
		// + allData.get(groupPosition).get(childPosition).name,
		// Toast.LENGTH_SHORT).show();
		// actionbar.setTopRightIcon(R.anim.offline_download);
		// return true;
		// }
		// });

		// ListView localMapListView = (ListView)
		// findViewById(R.id.localmaplist);
		// lAdapter = new LocalMapAdapter();
		// localMapListView.setAdapter(lAdapter);

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

	}

	int temp;

	@Override
	public void onGetOfflineMapState(int type, int state) {
		// TODO Auto-generated method stub
		switch (type) {
		case MKOfflineMap.TYPE_DOWNLOAD_UPDATE:
			MKOLUpdateElement update = mOffline.getUpdateInfo(state);
			// 处理下载进度更新提示
			if (update != null) {
				if (temp != update.ratio && update.ratio % 2 == 0) {
					temp = update.ratio;
					if (update.ratio == 100) {
						Toast.makeText(
								getApplicationContext(),
								getString(R.string.upgrade_success) + ":"
										+ update.cityName, Toast.LENGTH_SHORT)
								.show();
						// actionbar.setTopRightIcon(R.drawable.download);
						NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
								this)
								.setWhen(System.currentTimeMillis())
								.setSmallIcon(R.drawable.ic_launcher)
								.setContentTitle(
										getString(R.string.offline_down_ok))
								.setContentText(update.cityName)
								.setAutoCancel(true);
						NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
						// mId allows you to update the notification later on.
						mNotificationManager.notify(111, mBuilder.build());

					}
					City c = new City(update.cityName,
							formatDataSize(update.serversize), update.cityID);
					for (ArrayList<City> a : allData) {
						for (City cc : a) {
							if (cc.equals(c)) {
								int i = a.indexOf(cc);
								int j = allData.indexOf(a);
								cc.setProgress(update.ratio);
								a.set(i, cc);
								allData.set(j, a);
								cAdapter.notifyDataSetChanged();
							}
						}
					}
				}
			}
			City c = new City(update.cityName,
					formatDataSize(update.serversize), update.cityID);
			for (ArrayList<City> a : allData) {
				for (City cc : a) {
					if (cc.equals(c)) {
						int i = a.indexOf(cc);
						int j = allData.indexOf(a);
						cc.setProgress(update.ratio);
						a.set(i, cc);
						allData.set(j, a);
						CityAdapter cA = new CityAdapter(groupData, allData,
								this);
						eList.setAdapter(cA);
						eList.expandGroup(j);
						return;
					}
				}
			}

			break;
		case MKOfflineMap.TYPE_NEW_OFFLINE:
			// 有新离线地图安装

			break;
		case MKOfflineMap.TYPE_VER_UPDATE:
			// 版本更新提示
			// MKOLUpdateElement e = mOffline.getUpdateInfo(state);
			break;
		}
	}

	public String formatDataSize(int size) {
		String ret = "";
		if (size < (1024 * 1024)) {
			ret = String.format("%dK", size / 1024);
		} else {
			ret = String.format("%.1fM", size / (1024 * 1024.0));
		}
		return ret;
	}

	/***
	 * 离线包城市类
	 * 
	 * @author Kevin
	 *
	 */
	class City {
		private String name; // 城市名
		private String size; // 离线包大小
		private int code; // 城市编码
		private int progress = -1; // 下载进度
		// 下载状态 0是未下载 1是暂停 2是正在
		private int DownLoading = 0;

		public int getProgress() {
			return progress;
		}

		public int getDownLoading() {
			return DownLoading;
		}

		public void setDownLoading(int downLoading) {
			DownLoading = downLoading;
		}

		public City(String name, String size, int code) {
			super();
			this.name = name;
			this.size = size;
			this.code = code;
		}

		public void setProgress(int progress) {
			this.progress = progress;
		}

		@Override
		public boolean equals(Object o) {
			// TODO Auto-generated method stub
			if (o instanceof City) {
				if (this.code == ((City) o).code) {
					return true;
				} else {
					return false;
				}
			} else if (o instanceof MKOLSearchRecord) {
				if (this.code == ((MKOLSearchRecord) o).cityID) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}

	}

	/***
	 * 传入的城市名称是否是特殊的
	 * 
	 * @param name
	 * @return
	 */
	boolean compareCity(String name) {
		String[] str = { "北京", "天津", "上海", "澳门", "重庆", "香港", "全国" };
		for (String s : str) {
			if (name.contains(s)) {
				return true;
			}
		}
		return false;
	}

	class CityAdapter extends BaseExpandableListAdapter {
		private ArrayList<String> group;
		private ArrayList<ArrayList<City>> all;
		private Context context;

		@Override
		public void notifyDataSetChanged() {
			// TODO Auto-generated method stub
			all = allData;
			super.notifyDataSetChanged();
		}

		public CityAdapter(ArrayList<String> group,
				ArrayList<ArrayList<City>> all, Context context) {
			super();
			this.group = group;
			this.all = all;
			this.context = context;
			if (Build.VERSION.SDK_INT >= 11) {
				this.context = new ContextThemeWrapper(context,
						android.R.style.Theme_Holo_Light);
			}
		}

		@Override
		public void registerDataSetObserver(DataSetObserver observer) {
			// TODO Auto-generated method stub
			super.registerDataSetObserver(observer);
		}

		@Override
		public void unregisterDataSetObserver(DataSetObserver observer) {
			// TODO Auto-generated method stub

		}

		@Override
		public int getGroupCount() {
			// TODO Auto-generated method stub
			return group.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			// TODO Auto-generated method stub
			return all.get(groupPosition).size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			// TODO Auto-generated method stub
			return all.get(groupPosition);
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return all.get(groupPosition).get(childPosition);
		}

		@Override
		public long getGroupId(int groupPosition) {
			// TODO Auto-generated method stub
			return groupPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return childPosition;
		}

		@Override
		public boolean hasStableIds() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			convertView = LayoutInflater.from(context).inflate(
					R.layout.menu_group, null);
			TextView name = (TextView) convertView.findViewById(R.id.name);
			LinearLayout l = (LinearLayout) convertView.findViewById(R.id.l1);
			ImageView img = (ImageView) convertView.findViewById(R.id.guide);
			l.setVisibility(View.GONE);
			name.setText(group.get(groupPosition));
			if (isExpanded) {
				img.setBackgroundResource(R.drawable.list_arrow_down);

			} else {
				img.setBackgroundResource(R.drawable.list_arrow);
			}
			return convertView;
		}

		@Override
		public View getChildView(final int groupPosition,
				final int childPosition, boolean isLastChild, View convertView,
				ViewGroup parent) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.offline_child, null);
			TextView name = (TextView) convertView.findViewById(R.id.name);
			ImageView ico = (ImageView) convertView.findViewById(R.id.icon);
			TextView size = (TextView) convertView.findViewById(R.id.size);
			final TextView percent = (TextView) convertView
					.findViewById(R.id.pro);
			final int p = all.get(groupPosition).get(childPosition)
					.getProgress();
			if (p == 100) {
				percent.setTextColor(Color.parseColor("#14c775"));
				percent.setText(R.string.finish);
				ico.setImageResource(R.drawable.complete);
			} else if (p > 0) {

				percent.setText(p + "%");
			}
			name.setText(all.get(groupPosition).get(childPosition).name);
			size.setText(all.get(groupPosition).get(childPosition).size);
			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (p == -1) {
						mOffline.start(allData.get(groupPosition).get(
								childPosition).code);
						Toast.makeText(
								getApplicationContext(),
								getString(R.string.offline_down_start)
										+ ":"
										+ allData.get(groupPosition).get(
												childPosition).name,
								Toast.LENGTH_SHORT).show();
						// actionbar.setTopRightIcon(R.anim.offline_download);
						percent.setTextColor(Color.parseColor("#14c775"));
						percent.setText(R.string.offline_down_added);
					} else if (p >= 0 && p < 100) {
						Toast.makeText(
								getApplicationContext(),
								getString(R.string.offline_down_pause)
										+ ":"
										+ allData.get(groupPosition).get(
												childPosition).name,
								Toast.LENGTH_SHORT).show();
						mOffline.pause(allData.get(groupPosition).get(
								childPosition).code);
						allData.get(groupPosition).get(childPosition)
								.setProgress(-1);
						// percent.setTextColor(Color.parseColor("#14c775"));
						// percent.setText(R.string.pause);
						notifyDataSetChanged();

					} else {
						Toast.makeText(
								getApplicationContext(),
								getString(R.string.offline_down_delete)
										+ ":"
										+ allData.get(groupPosition).get(
												childPosition).name,
								Toast.LENGTH_SHORT).show();
					}
				}
			});
			convertView.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					// TODO Auto-generated method stub

					AlertDialog.Builder ab = new AlertDialog.Builder(context);
					ab.setTitle(R.string.pleaseselect);
					ab.setMessage('\n'
							+ allData.get(groupPosition).get(childPosition).name
							+ '\t'
							+ ""
							+ '\t'
							+ allData.get(groupPosition).get(childPosition).size
							+ '\n');
					ab.setNegativeButton(R.string.delete,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									mOffline.remove(allData.get(groupPosition)
											.get(childPosition).code);
									allData.get(groupPosition)
											.get(childPosition).setProgress(-1);
									notifyDataSetChanged();
								}
							});
					ab.setPositiveButton(R.string.offline_down_restart,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									mOffline.remove(allData.get(groupPosition)
											.get(childPosition).code);
									mOffline.start(allData.get(groupPosition)
											.get(childPosition).code);
								}
							});
					ab.show();
					return true;
				}
			});
			return convertView;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public boolean areAllItemsEnabled() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isEmpty() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void onGroupExpanded(int groupPosition) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onGroupCollapsed(int groupPosition) {
			// TODO Auto-generated method stub

		}

		@Override
		public long getCombinedChildId(long groupId, long childId) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public long getCombinedGroupId(long groupId) {
			// TODO Auto-generated method stub
			return 0;
		}

	}

	public static boolean isWifi(Context mContext) {
		ConnectivityManager connectivityManager = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo != null
				&& activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
			return true;
		}
		return false;
	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub

	}
}

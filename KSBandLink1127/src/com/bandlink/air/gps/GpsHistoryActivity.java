package com.bandlink.air.gps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.bandlink.air.MilinkApplication;
import com.bandlink.air.R;
import com.bandlink.air.ble.Converter;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.SharePreUtils;

public class GpsHistoryActivity extends LovefitActivity {
	private SharedPreferences share;
	private SimpleAdapter saItem;
	private ListView listview;
	private ArrayList<HashMap<String, Object>> historyList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setContentView(R.layout.activity_gpshistory);
		ActionbarSettings actionbar = new ActionbarSettings(this,
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub

						GpsHistoryActivity.this.finish();

					}
				}, null);
		actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
		actionbar.setTitle(R.string.gps_sport_history);
		share = getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		history();
		super.onCreate(savedInstanceState);
	}

	public void history() {
		// setContentView(R.layout.settingsinfo);

		historyList = new ArrayList<HashMap<String, Object>>();
		listview = (ListView) findViewById(R.id.btlistView);
		getData();
		saItem = new MSimpleAdapter(this, historyList,
				R.layout.listview_item_gps_history, new String[] {
						"distancestring", "durancestring", "timestring", "num",
						"date" }, new int[] { R.id.distance, R.id.durance,
						R.id.gpsStart, R.id.iid, R.id.date });

		listview.setAdapter(saItem);

		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					final int position, long arg3) {

				Intent intent = new Intent();
				intent.setClass(GpsHistoryActivity.this,
						GPSHistoryDetailActivity.class);
				Integer iid = (Integer) (historyList.get(position).get("iid"));
				// Integer steps = (Integer) (historyList.get(position)
				// .get("stepsNum"));
				// Double calorie = (Double) (historyList.get(position)
				// .get("calorie"));
				// Double distance = (Double) (historyList.get(position)
				// .get("distance"));
				// Integer durance = (Integer) (historyList.get(position)
				// .get("durance"));
				intent.putExtra("iid", iid);
				// intent.putExtra("steps", steps);
				// intent.putExtra("calorie", calorie);
				// intent.putExtra("distance", distance);
				// intent.putExtra("durance", durance);
				// intent.putExtra("timestring",(String)historyList.get(position).get("timestring"));

				startActivity(intent);
			}
		});
		listview.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					final int arg2, long arg3) {
				// TODO Auto-generated method stub

				AlertDialog.Builder ab = new AlertDialog.Builder(
						Build.VERSION.SDK_INT >= 11 ? new ContextThemeWrapper(
								GpsHistoryActivity.this,
								android.R.style.Theme_Holo_Light)
								: GpsHistoryActivity.this);
				ab.setTitle(R.string.warning);
				ab.setMessage(R.string.del_yes_or_no);
				ab.setNegativeButton(R.string.can, null);
				ab.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								db.delGPSTrack(historyList.get(arg2)
										.get("timestring").toString());
								getData();
								saItem.notifyDataSetChanged();
							}
						});
				ab.show();
				return true;
			}
		});

	}

	Dbutils db;
	String falg = "";

	private void getData() {
		historyList.clear();
		db = new Dbutils(share.getInt("UID", -1), this);
		Cursor cursor = db.getGPSTrackCursor();
		int i = 1;
		if (cursor != null) {
			while (cursor.moveToNext()) {
				Integer iid = cursor.getInt(0);
				String time = cursor.getString(2);
				Integer steps = cursor.getInt(3);
				Double calorie = cursor.getDouble(4);
				Double distance = cursor.getDouble(5);
				if (distance <= 100) {
					continue;
				}
				Integer durance = cursor.getInt(6);
				HashMap<String, Object> map = new HashMap<String, Object>();
				String distanceString = String.format("%.2f", distance / 1000);
				String durationString = Converter.durationToString(this,
						durance);

				String sss = String.format("%1$s/%2$s", distanceString,
						durationString);
				map.put("image", R.drawable.ic_launcher);
				map.put("totalstring", sss);
				String stepsString = steps.toString();
				map.put("steps", stepsString);
				map.put("iid", iid);
				map.put("distancestring", distanceString);
				map.put("durancestring", durationString);
				map.put("timestring", time.substring(11, time.length()));
				String date = time.substring(0, 11);
				if (!falg.equals(date)) {
					map.put("date", date);
					falg = date;
				} else {
					map.put("date", "");
				}

				map.put("stepsNum", steps);
				map.put("calorie", calorie);
				map.put("distance", distance);
				map.put("durance", durance);
				map.put("num", i);
				i++;
				historyList.add(map);

			}

		}

	}

	class MSimpleAdapter extends SimpleAdapter {

		public MSimpleAdapter(Context context,
				List<? extends Map<String, ?>> data, int resource,
				String[] from, int[] to) {
			super(context, data, resource, from, to);
			// TODO Auto-generated constructor stub
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View view = super.getView(position, convertView, parent);
			HashMap<String, String> map = (HashMap<String, String>) getItem(position);
			TextView dis = (TextView) view.findViewById(R.id.distance);
			TextView durance = (TextView) view.findViewById(R.id.durance);
			TextView gpsStart = (TextView) view.findViewById(R.id.gpsStart);
			TextView date = (TextView) view.findViewById(R.id.date);
			if (!map.containsKey("date") || map.get("date").length() == 0) {
				date.setVisibility(View.GONE);
			} else {
				date.setVisibility(View.VISIBLE);
			}
			date.setTypeface(MilinkApplication.NumberFace);
			dis.setTypeface(MilinkApplication.NumberFace);
			durance.setTypeface(MilinkApplication.NumberFace);
			gpsStart.setTypeface(MilinkApplication.NumberFace);
			return view;
		}

	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub

	}
}

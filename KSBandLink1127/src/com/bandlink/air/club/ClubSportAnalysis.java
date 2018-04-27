package com.bandlink.air.club;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bandlink.air.R;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.MyDate;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Util;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ClubSportAnalysis extends LovefitActivity implements
		View.OnClickListener {

	private Button date1, date2;
	private ImageView search;
	private String clubid, session;
	private ActionbarSettings action;
	private SharedPreferences share;
	private ListView listview;
	private ClubRankAdapter adapter;
	private String startTime, endTime, uid;
	private boolean btnShow = false;
	private ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
	private View foot;

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setContentView(R.layout.activity_club_sport);
		foot = LayoutInflater.from(
				com.bandlink.air.util.Util.getThemeContext(this))
				.inflate(R.layout.xlistview_footer, null);
		endTime = startTime = MyDate.getYesterDay();
		share = getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		session = share.getString("session_id", "-1");
		uid = share.getInt("UID", -1) + "";
		date1 = (Button) findViewById(R.id.date1);
		listview = (ListView) findViewById(R.id.listview);
		date2 = (Button) findViewById(R.id.date2);
		// date2.setText(endTime);
		// date1.setText(startTime);
		search = (ImageView) findViewById(R.id.search);
		date1.setOnClickListener(this);
		date2.setOnClickListener(this);
		search.setOnClickListener(this);
		clubid = getIntent().getStringExtra("id");
		action = new ActionbarSettings(this, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ClubSportAnalysis.this.finish();
			}
		}, null);
		action.setTitle(R.string.club_sport);
		action.setTopLeftIcon(R.drawable.ic_top_arrow);
		adapter = new ClubRankAdapter(data, 100);
		listview.setAdapter(adapter);
		listview.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				if (data == null) {
					return;
				}
				if (data.size() == (visibleItemCount + firstVisibleItem)
						&& data.size() > 0) {
					if (!btnShow) {
						btnShow = true;
						// loadmore
						int size = data.size();
						if (size / 10 == 0) {
							Toast.makeText(ClubSportAnalysis.this,
									getString(R.string.nomore),
									Toast.LENGTH_SHORT).show();
						} else {
							dialog = Util.initProgressDialog(
									ClubSportAnalysis.this, true,
									getString(R.string.data_wait), null);
							new Thread(new Runnable() {
								public void run() {
									try {
										data.addAll(getRank(clubid + "", uid,
												startTime, endTime,
												data.size() / 10));
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									handler.obtainMessage(1).sendToTarget();
								}
							}).start();

						}
					}
				} else {
					if (btnShow) {
						btnShow = false;

					}

				}
				return;
			}
		});
		super.onCreate(savedInstanceState);
	}

	private ProgressDialog dialog;
	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				if (msg.obj instanceof JSONArray) {
					JSONArray arr = (JSONArray) msg.obj;
					try {
						showGroup(arr);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				break;
			case 1:
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
				if (data != null && data.size() > 0) {
					adapter.setMaxValue(Integer
							.valueOf(data.get(0).get("step")));
					adapter.notifyDataSetChanged();
				}

				break;
			}
		};
	};

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.date1:
			// new Thread(new Runnable() {
			// public void run() {
			// try {
			// JSONArray arr = getGroup(clubid, session);
			// handler.obtainMessage(0, arr).sendToTarget();
			// } catch (Exception e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// }
			// }).start();
			showTimesPicker(date1);
			break;
		case R.id.date2:
			showTimesPicker(date2);
			break;
		case R.id.search:
			dialog = Util.initProgressDialog(
					ClubSportAnalysis.this, true,
					getString(R.string.data_wait), null);
			new Thread(new Runnable() {
				public void run() {
					try {

						data.clear();
						data.addAll(getRank(clubid + "", uid, startTime,
								endTime, 0));
						handler.obtainMessage(1).sendToTarget();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();
			break;
		}
	}

	public static JSONArray getGroup(String id, String session)
			throws Exception {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("clubid", id);
		params.put("session", session);
		String res = HttpUtlis.getRequest(HttpUtlis.BASE_URL
				+ "/data/getClubGroup", params);
		JSONObject obj = new JSONObject(res);
		if (obj.getInt("status") == 0) {
			return obj.getJSONArray("content");
		}
		return null;
	}

	public static ArrayList<HashMap<String, String>> getRank(String clubid,
			String uid, String start, String end, int index) throws Exception {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("clubid", clubid);
		params.put("uid", uid);
		params.put("begin", start);
		params.put("end", end);
		params.put("sindex", index + "");
		String res = HttpUtlis.getRequest(HttpUtlis.BASE_URL
				+ "/data/getClubSoprt", params);
		JSONObject obj = new JSONObject(res);
		if (obj.getInt("status") == 0) {
			ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
			JSONArray arr = obj.getJSONArray("content");
			for (int i = 0; i < arr.length(); i++) {
				JSONObject o = arr.getJSONObject(i);
				HashMap<String, String> row = new HashMap<String, String>();
				row.put("uid", o.getString("uid"));
				row.put("gid", o.getString("gid"));
				row.put("step", o.getString("step"));
				row.put("name", o.getString("name"));
				data.add(row);
			}
			return data;
		}
		return null;
	}

	private String[] date1Str;

	void showGroup(final JSONArray arr) throws JSONException {

		date1Str = new String[arr.length() + 1];
		date1Str[0] = getString(R.string.defaultgroup);
		for (int i = 1; i < arr.length() + 1; i++) {
			date1Str[i] = arr.getJSONObject(i - 1).getString("date1name");

		}
		AlertDialog.Builder ab = new AlertDialog.Builder(
				Util.getThemeContext(this));
		ab.setItems(date1Str, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, final int which) {
				// TODO Auto-generated method stub
				date1.setText(date1Str[which]);
			}
		});
		ab.setNegativeButton(R.string.can, null);
		ab.create().show();
	}

	void showTimesPicker(final Button btn) {
		Context context = Util.getThemeContext(this);
		AlertDialog.Builder ab = new AlertDialog.Builder(context);
		ab.setTitle(R.string.select_times);
		final DatePicker datePicker1 = new DatePicker(context);
		Calendar now = Calendar.getInstance();
		now.add(Calendar.DAY_OF_YEAR, -1);
		datePicker1.init(now.get(Calendar.YEAR), now.get(Calendar.MONTH),
				now.get(Calendar.DAY_OF_MONTH), null);
		now.add(Calendar.DAY_OF_YEAR, +1);
		datePicker1.setMaxDate(now.getTimeInMillis());
		// now.add(Calendar.MONTH, -3);
		// datePicker1.setMinDate(now.getTimeInMillis());
		datePicker1.setCalendarViewShown(false);

		LinearLayout linear = new LinearLayout(context);
		linear.setOrientation(LinearLayout.VERTICAL);
		linear.addView(datePicker1);
		ab.setView(linear);
		ab.setPositiveButton(R.string.ok, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if (btn.getId() == R.id.date1) {
					startTime = datePicker1.getYear() + "-"
							+ getTran((datePicker1.getMonth() + 1)) + "-"
							+ getTran(datePicker1.getDayOfMonth());
					btn.setText(startTime);
				} else if (btn.getId() == R.id.date2) {
					endTime = datePicker1.getYear() + "-"
							+ getTran((datePicker1.getMonth() + 1)) + "-"
							+ getTran(datePicker1.getDayOfMonth());
					btn.setText(endTime);
				}

			}
		});
		ab.create().show();

	}

	String getTran(int x) {
		if (x < 10) {
			return "0" + x;
		} else {
			return "" + x;
		}
	}

	class ClubRankAdapter extends BaseAdapter {

		private ArrayList<HashMap<String, String>> data;
		private int max;

		public void setMaxValue(int max) {
			this.max = max;
		}

		public ClubRankAdapter(ArrayList<HashMap<String, String>> data, int step) {
			// TODO Auto-generated constructor stub
			this.data = data;
			max = step;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return data.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return data.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			CViewHolder vhd;
			if (convertView == null) {
				vhd = new CViewHolder();
				convertView = LayoutInflater.from(ClubSportAnalysis.this)
						.inflate(R.layout.item_club_rank, null);
				vhd.rank = (ImageView) convertView.findViewById(R.id.rank_img);
				vhd.photo = (BorderImageView) convertView
						.findViewById(R.id.photo);
				vhd.name = (TextView) convertView.findViewById(R.id.name);
				vhd.step = (TextView) convertView.findViewById(R.id.step);
				vhd.bar = (ProgressBar) convertView.findViewById(R.id.progress);
				convertView.setTag(vhd);
			} else {
				vhd = (CViewHolder) convertView.getTag();
			}
			HashMap<String, String> row = data.get(position);
			ImageLoader.getInstance()
					.displayImage(
							HttpUtlis.AVATAR_URL
									+ HttpUtlis.getAvatarUrl(row.get("uid")),
							vhd.photo);
			vhd.name.setText(row.get("name"));
			vhd.step.setText(row.get("step"));
			vhd.bar.setMax(max);
			vhd.bar.setProgress(Integer.valueOf(row.get("step")));
			if (date1.getTag() != null
					&& !row.get("gid").equals(date1.getTag())) {
				convertView.setVisibility(View.GONE);
			} else {
				convertView.setVisibility(View.VISIBLE);
			}
			return convertView;
		}

	}

	static class CViewHolder {
		public ImageView rank;
		public BorderImageView photo;
		public TextView name, step;
		public ProgressBar bar;
	}
}

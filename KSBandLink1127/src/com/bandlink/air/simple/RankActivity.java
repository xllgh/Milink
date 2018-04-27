package com.bandlink.air.simple;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bandlink.air.MilinkApplication;
import com.bandlink.air.NewFriendActivity;
import com.bandlink.air.R;
import com.bandlink.air.club.BorderImageView;
import com.bandlink.air.jpush.Jpush;
import com.bandlink.air.simple.ptr.PtrDefaultHandler;
import com.bandlink.air.simple.ptr.PtrFrameLayout;
import com.bandlink.air.simple.ptr.PtrHandler;
import com.bandlink.air.simple.ptr.StoreHouseHeader;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.MainInterface;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.UserHashMap;
import com.nostra13.universalimageloader.core.ImageLoader;

public class RankActivity extends Fragment implements OnFinished {

	private ListView list;
	private Dbutils dbutils;
	SimpleAdapter adapter;
	List<UserHashMap<String, String>> listData;
	int current_type = 4;
	ActionbarSettings actionbar;
	PtrFrameLayout ptrFrameLayout;
	SharedPreferences share;
	Dbutils db;
	private boolean btnShow = true;
	// 0 好友排名 1 全站排名
	private int RankMode = 0;

	public void getUserJid(final String uid, final String msg) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					String result = HttpUtlis.queryStringForGet(HttpUtlis.BASE_URL + "/Dongtai/getJpushId/uid/" + uid);
					JSONObject json = new JSONObject(result);
					String jid = json.getString("content");
					sendJpush(jid, msg);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}).start();
	}

	public void sendJpush(String jpushid, String msg) {
		if (jpushid != null && !jpushid.equals("")) {

			Jpush.sendJpushMessage(msg, jpushid, null, getActivity());
		}
	}

	private View foot;
	private View mView;
	MainInterface interf;

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		interf = (MainInterface) activity;
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		mView = inflater.inflate(R.layout.allrank, null);
		mView.findViewById(R.id.fbtn).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent().setClass(getActivity(), NewFriendActivity.class));
			}
		});
		foot = LayoutInflater.from(com.bandlink.air.util.Util.getThemeContext(getActivity()))
				.inflate(R.layout.xlistview_footer, null);
		share = getActivity().getSharedPreferences(SharePreUtils.APP_ACTION, Context.MODE_PRIVATE);
		db = new Dbutils(getActivity());
		ptrFrameLayout = (PtrFrameLayout) mView.findViewById(R.id.refresh);

		actionbar = new ActionbarSettings(mView, new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				current_type++;
				if (current_type > 4) {
					current_type = 1;
				}
				switch (current_type) {
				case 1:
					actionbar.setTitle(R.string.dayrank);
					actionbar.setTopRightIcon(R.drawable.dayrank);
					break;
				case 2:
					actionbar.setTitle(R.string.weekrank);
					actionbar.setTopRightIcon(R.drawable.weekrank);
					break;
				case 3:
					actionbar.setTitle(R.string.monthrank);
					actionbar.setTopRightIcon(R.drawable.monthrank);
					break;
				case 4:
					actionbar.setTitle(getString(R.string.todayrank));
					actionbar.setTopRightIcon(R.drawable.todayrank);
					break;
				}
				if (listData != null) {
					listData.clear();
					listData.addAll(dbutils.getRankDataByPage(current_type, 0));
					adapter.notifyDataSetChanged();
				} else {
					listData = dbutils.getRankDataByPage(current_type, 0);
					adapter = new SimpleAdapter(getActivity(), listData);
					list.setAdapter(adapter);
				}
			}
		}, interf);

		actionbar.setTopLeftIcon(R.drawable.ic_top_left);
		actionbar.setTopRightIcon(R.drawable.todayrank);
		list = (ListView) mView.findViewById(R.id.ranklist);

		StoreHouseHeader header = new StoreHouseHeader(getActivity());
		header.setPadding(0, 25, 0, 25);
		header.initWithString("Loading...");
		header.setBackgroundColor(Color.parseColor("#f2f4f1"));
		header.setTextColor(cur_color);
		ptrFrameLayout.setDurationToCloseHeader(1500);
		ptrFrameLayout.setHeaderView(header);
		ptrFrameLayout.addPtrUIHandler(header);

		ptrFrameLayout.setPtrHandler(new PtrHandler() {
			@Override
			public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
				return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
			}

			@Override
			public void onRefreshBegin(PtrFrameLayout frame) {
				handler.sendEmptyMessage(1);
			}
		});
		dbutils = new Dbutils(getActivity());
		// 4表示今天，1表示昨天，2表示周，3月
		listData = new ArrayList<UserHashMap<String, String>>();
		actionbar.setTitle(getString(R.string.todayrank));
		adapter = new SimpleAdapter(getActivity(), listData);
		list.setAdapter(adapter);
		list.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				if (listData == null) {
					return;
				}
				if (listData.size() == (visibleItemCount + firstVisibleItem) && listData.size() > 0) {
					if (!btnShow) {
						btnShow = true;
						// loadmore
						int size = listData.size();
						if (size / 10 == 0) {
							Toast.makeText(getActivity(), getString(R.string.nomore), Toast.LENGTH_SHORT).show();
						} else {
							try {
								list.addFooterView(foot);
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							RankActivity.this.listData.clear();
							listData.addAll(dbutils.getRankDataByPage(current_type, size / 10));
							adapter.notifyDataSetChanged();
							try {
								list.removeFooterView(foot);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
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
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				Object obj = parent.getAdapter().getItem(position);
				if (obj instanceof UserHashMap) {
					UserHashMap<String, String> map = (UserHashMap<String, String>) obj;
					String uid = map.get("uid");
					String name = map.get("user");
					// showDialog();
				}
			}
		});
		init();
		return mView;
	}

	void init() {
		listData.addAll(dbutils.getRankDataByPage(current_type, 0));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

		// 如果需要更新数据
		try {
			// true每次进来都要刷新
			if (true || Integer.valueOf(sdf.format(new Date())) > Integer.valueOf(share.getString("rankcheck", "0"))) {
				ptrFrameLayout.postDelayed(new Runnable() {
					@Override
					public void run() {
						ptrFrameLayout.autoRefresh(true);
					}
				}, 150);
				new Thread(new RunnableDayRank(share, db)).start();
				new Thread(new RunnableweekRank(share, db)).start();
				new Thread(new RunnableMonthRank(share, db)).start();
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			ptrFrameLayout.postDelayed(new Runnable() {
				@Override
				public void run() {
					ptrFrameLayout.autoRefresh(true);
				}
			}, 150);
			new Thread(new RunnableDayRank(share, db)).start();
			new Thread(new RunnableweekRank(share, db)).start();
			new Thread(new RunnableMonthRank(share, db)).start();
			e.printStackTrace();
		} catch (Exception e2) {
			e2.printStackTrace();
		}
	}

	int i = 0;

	class SimpleAdapter extends BaseAdapter {
		Context context;
		List<UserHashMap<String, String>> listData;

		// HashMap<Integer, SoftReference<View>> map;
		public SimpleAdapter(Context context, List<UserHashMap<String, String>> listData) {
			super();
			this.context = context;
			this.listData = listData;
			// map = new HashMap<Integer, SoftReference<View>>();
		}

		@Override
		public void notifyDataSetChanged() {
			// TODO Auto-generated method stub.
			// map.clear();
			super.notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return listData.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return listData.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub

			final ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = getActivity().getLayoutInflater().inflate(R.layout.simple_rank_item, null);
				holder.ico = (BorderImageView) convertView.findViewById(R.id.photo);
				holder.name = (TextView) convertView.findViewById(R.id.name);

				holder.step = (TextView) convertView.findViewById(R.id.steps);
				holder.rank = (TextView) convertView.findViewById(R.id.rank);
				holder.rank.setTypeface(MilinkApplication.NumberFace);
				holder.unit = (TextView) convertView.findViewById(R.id.unit);
				convertView.setTag(holder);
				// map.put(position, new SoftReference<View>(convertView));
			} else {
				// convertView = map.get(position).get();
				holder = (ViewHolder) convertView.getTag();
			}
			/*
			 * if (convertView.getMeasuredHeight() * listData.size() > context
			 * .getResources().getDisplayMetrics().heightPixels && position ==
			 * listData.size() - 1 && listData.size() % 10 == 0) {
			 * holder.more.setVisibility(View.VISIBLE);
			 * holder.more.setOnClickListener(new OnClickListener() {
			 * 
			 * @Override public void onClick(View v) {
			 * 
			 * 
			 * } }); } else
			 */ {
			}
			ImageLoader.getInstance().displayImage(listData.get(position).get("url"), holder.ico,
					MilinkApplication.getListOptions());
			holder.step.setText(listData.get(position).get("step") + "");
			holder.name.setText(listData.get(position).get("user").startsWith("temp_") ? getString(R.string.tourist)
					: (listData.get(position).get("user")) + "");
			switch (position) {
			case 0:
				holder.rank.setText(position + 1 + "");
				holder.unit.setText("st");
				// rank.setBackgroundResource(R.drawable.fst);
				break;
			case 1:
				holder.rank.setText(position + 1 + "");
				holder.unit.setText("nd");
				// rank.setBackgroundResource(R.drawable.sec);
				break;
			case 2:
				holder.rank.setText(position + 1 + "");
				holder.unit.setText("rd");
				// rank.setBackgroundResource(R.drawable.thi);
				break;
			default:
				holder.rank.setText(position + 1 + "");
				holder.unit.setText("");
				break;
			}

			return convertView;
		}

	}

	public class ViewHolder {
		TextView name, step, rank, unit;
		BorderImageView ico;
	}

	List<HashMap<String, String>> subList(List<HashMap<String, String>> lis, int from, int to) {
		try {
			return lis.subList(from, to);
		} catch (Exception e) {
			try {
				return lis.subList(from, lis.size() - 1);
			} catch (Exception e1) {
				return lis;
			}
		}

	}

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case 0:

				// current_type = 4;
				listData = dbutils.getRankDataByPage(current_type, 0);
				adapter = new SimpleAdapter(getActivity(), listData);
				list.setAdapter(adapter);
				break;
			case 10:
				if (msg.what == 10) {
					Toast.makeText(getActivity(), getString(R.string.network_erro), Toast.LENGTH_SHORT).show();
					ptrFrameLayout.refreshComplete();
				}
				break;
			case 1:

				// 4表示今天，1表示昨天，2表示周，3月
				Runnable runnable = null;
				switch (current_type) {
				case 1:
					runnable = new RunnableDayRank(share, db);
					((RunnableDayRank) runnable).setOnFinished(RankActivity.this);
					break;
				case 2:
					runnable = new RunnableweekRank(share, db);
					((RunnableweekRank) runnable).setOnFinished(RankActivity.this);
					break;
				case 3:
					runnable = new RunnableMonthRank(share, db);
					((RunnableMonthRank) runnable).setOnFinished(RankActivity.this);
					break;
				case 4:
					runnable = new RunnableToDayRank(share, db);
					((RunnableToDayRank) runnable).setOnFinished(RankActivity.this);
					break;
				}
				if (runnable != null) {
					new Thread(runnable).start();
				}
				break;
			}

			super.handleMessage(msg);
		}

	};

	int cur_color = 0x5f93ef;

	@Override
	public void Finished(int type, int index) {
		// TODO Auto-generated method stub
		ptrFrameLayout.refreshComplete();
		handler.sendEmptyMessage(0);
	}

	@Override
	public void Error(int type) {
		// TODO Auto-generated method stub
		ptrFrameLayout.refreshComplete();
	}

}
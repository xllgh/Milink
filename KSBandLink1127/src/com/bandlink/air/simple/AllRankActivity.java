package com.bandlink.air.simple;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bandlink.air.FriendFragment;
import com.bandlink.air.MenuFragment;
import com.bandlink.air.MilinkApplication;
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
import com.bandlink.air.util.MainInterface;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.UserHashMap;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class AllRankActivity extends Fragment implements OnFinished {
	private ListView list;
	private Dbutils dbutils;
	SimpleAdapter adapter;
	List<UserHashMap<String, String>> listData;
	int current_type = 4;
	ActionbarSettings actionbar;
	PtrFrameLayout ptrFrameLayout;
	SharedPreferences share;
	Dbutils db;
	Button fbutton;
	// 0 好友排名 1 全站排名
	private int RankMode = 0;
	private View mView;
	private boolean btnShow = false;
	private Context con;
	BorderImageView photo;
	TextView rank, name;
	View foot, head;
	MainInterface inf;

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		inf = (MainInterface) activity;
		super.onAttach(activity);
	}

	public static String transformNick(String nick) {
		if (nick.startsWith("1") && nick.length() == 11) {
			String temp = nick.substring(3, 7);
			nick = nick.replace(temp, "****");
		} else if (nick.contains("PA_")) {
			nick = nick.replace("PA_", "");
			if (nick.startsWith("1") && nick.length() == 11) {
				String temp = nick.substring(3, 7);
				nick = nick.replace(temp, "****");
			}

		}
		return nick;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mView = inflater.inflate(R.layout.rank, null);

		foot = LayoutInflater.from(
				com.bandlink.air.util.Util.getThemeContext(getActivity()))
				.inflate(R.layout.xlistview_footer, null);
		head = LayoutInflater.from(
				com.bandlink.air.util.Util.getThemeContext(getActivity()))
				.inflate(R.layout.simple_rank_item, null);
		photo = (BorderImageView) head.findViewById(R.id.photo);
		rank = (TextView) head.findViewById(R.id.rank);
		name = (TextView) head.findViewById(R.id.name);
		name.setText("我的排名");
		share = getActivity().getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		db = new Dbutils(getActivity());
		ImageLoader.getInstance().displayImage(
				HttpUtlis.AVATAR_URL
						+ MenuFragment.getAvatarUrl(share.getInt("UID", -1)
								+ "", "big"), photo,
				MilinkApplication.getListOptions());
		con = com.bandlink.air.util.Util.getThemeContext(getActivity());
		ptrFrameLayout = (PtrFrameLayout) mView.findViewById(R.id.refresh);
		fbutton = (Button) mView.findViewById(R.id.fbtn);
		fbutton.setVisibility(View.GONE);
		fbutton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(getActivity(), RankActivity.class);
				startActivity(i);
				getActivity().overridePendingTransition(R.anim.hold,
						R.anim.fade);
			}
		});
		actionbar = new ActionbarSettings(mView,  new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				current_type++;
				if (current_type > 4) {
					current_type = 1;
				}
				switch (current_type) {
				case 1:
					actionbar.setTitle(R.string.dayrank_all);
					actionbar.setTopRightIcon(R.drawable.dayrank);
					break;
				case 2:
					actionbar.setTitle(R.string.weekrank_all);
					actionbar.setTopRightIcon(R.drawable.weekrank);
					break;
				case 3:
					actionbar.setTitle(R.string.monthrank_all);
					actionbar.setTopRightIcon(R.drawable.monthrank);
					break;
				case 4:
					actionbar.setTitle(getString(R.string.todayrank_all));
					actionbar.setTopRightIcon(R.drawable.todayrank);
					break;
				}
				if (listData != null) {
					listData.clear();
					listData.addAll(dbutils.getRankDataByPage(
							current_type + 10, 0));
					adapter.notifyDataSetChanged();
				} else {
					listData = dbutils.getRankDataByPage(current_type + 10, 0);
					adapter = new SimpleAdapter(getActivity(), listData);
					list.setAdapter(adapter);
				}
			}
		}, inf);
		 
		//actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
		actionbar.setTopRightIcon(R.drawable.todayrank);
		list = (ListView) mView.findViewById(R.id.ranklist);

		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Object obj = parent.getAdapter().getItem(position);
				if (obj instanceof UserHashMap) {
					UserHashMap<String, String> map = (UserHashMap<String, String>) obj;
					String uid = map.get("uid");
					String name = map.get("user");
					try {
						addFriend(name, uid);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		});
		list.setClickable(false);
		list.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				if (listData == null) {
					return;
				}
				if (listData.size() == (visibleItemCount + firstVisibleItem)
						&& listData.size() > 0) {
					if (!btnShow) {
						btnShow = true;
						// loadmore
						int size = listData.size();
						if (size / 10 == 0) {
							Toast.makeText(getActivity(),
									getString(R.string.nomore),
									Toast.LENGTH_SHORT).show();
						} else {
							try {
								list.addFooterView(foot);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							ArrayList<UserHashMap<String, String>> temp = dbutils
									.getAllRankDataByPage(current_type + 10,
											size);
							if (temp.size() > AllRankActivity.this.listData
									.size()) {
								AllRankActivity.this.listData.clear();
								listData.addAll(temp);
								adapter.notifyDataSetChanged();
							} else {

								handler.obtainMessage(2, size, 0)
										.sendToTarget();
							}

						}
					}
				} else {
					if (btnShow) {
						btnShow = false;

					}

				}
				return;
				// if (firstVisibleItem <=2) {
				// if(!btnShow){
				// btnShow = true;
				// fbutton.clearAnimation();
				// fbutton.startAnimation(AnimationUtils.loadAnimation(
				// getActivity(), R.anim.totop));
				// }
				//
				// }
				// if(firstVisibleItem > 2){
				// if(btnShow){
				// btnShow = false;
				// fbutton.clearAnimation();
				// fbutton.startAnimation(AnimationUtils.loadAnimation(
				// getActivity(), R.anim.todown));
				// }
				//
				// }
			}
		});
		StoreHouseHeader header = new StoreHouseHeader(getActivity());
		header.setPadding(0, 25, 0, 25);
		header.setBackgroundColor(Color.parseColor("#f2f4f1"));
		header.initWithString("Loading...");
		header.setTextColor(cur_color);
		ptrFrameLayout.setDurationToCloseHeader(1500);
		ptrFrameLayout.setHeaderView(header);
		ptrFrameLayout.addPtrUIHandler(header);
		ptrFrameLayout.setPtrHandler(new PtrHandler() {
			@Override
			public boolean checkCanDoRefresh(PtrFrameLayout frame,
					View content, View header) {
				return PtrDefaultHandler.checkContentCanBePulledDown(frame,
						content, header);
			}

			@Override
			public void onRefreshBegin(PtrFrameLayout frame) {
				handler.sendEmptyMessage(1);
			}
		});
		dbutils = new Dbutils(getActivity());
		// 4表示今天，1表示昨天，2表示周，3月
		listData = dbutils.getRankDataByPage(current_type + 10, 0);
		actionbar.setTitle(getString(R.string.todayrank_all));
		adapter = new SimpleAdapter(getActivity(), listData);
		list.setAdapter(adapter);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

		// 如果需要更新数据
		try {
			if (Integer.valueOf(sdf.format(new Date())) > Integer.valueOf(share
					.getString("allrankcheck", "0")) || true) {
				ptrFrameLayout.postDelayed(new Runnable() {
					@Override
					public void run() {
						ptrFrameLayout.autoRefresh(true);
					}
				}, 150);
				new Thread(new RunnableRankAll(share, db, 1, 0)).start();
				new Thread(new RunnableRankAll(share, db, 2, 0)).start();
				new Thread(new RunnableRankAll(share, db, 3, 0)).start();
				new Thread(new RunnableRankAll(share, db, 4, 0)).start();
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			ptrFrameLayout.postDelayed(new Runnable() {
				@Override
				public void run() {
					ptrFrameLayout.autoRefresh(true);
				}
			}, 150);
			new Thread(new RunnableRankAll(share, db, 1, 0)).start();
			new Thread(new RunnableRankAll(share, db, 2, 0)).start();
			new Thread(new RunnableRankAll(share, db, 3, 0)).start();
			new Thread(new RunnableRankAll(share, db, 4, 0)).start();
			e.printStackTrace();
		} catch (Exception e2) {
			e2.printStackTrace();
		}

		return mView;
	}

	ProgressDialog progressDialog;

	void sendRequest(final String uid, final String notes) {
		progressDialog = com.bandlink.air.util.Util.initProgressDialog(
				getActivity(), false, getString(R.string.data_wait), null);
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Message msg = handler.obtainMessage();

				Map<String, String> map = new LinkedHashMap<String, String>();
				map.put("session", share.getString("session_id", ""));
				map.put("fuid", uid + "");
				map.put("note", notes);

				try {
					String result = HttpUtlis.getRequest(
							HttpUtlis.FRIEND_ADDFRIEND, map);
					if (result != null) {
						JSONObject json;
						json = new JSONObject(result);
						int s = Integer.valueOf(json.getString("status"));
						msg.what = 4;
						if (s == 0) {

							msg.arg1 = R.string.add_friend_ok;
							handler.sendMessage(msg);
						} else if (s == 1) {

							msg.arg1 = R.string.no_param;
							handler.sendMessage(msg);
						} else if (s == 2) {

							msg.arg1 = R.string.not_user;
							handler.sendMessage(msg);
						} else if (s == 3) {

							msg.arg1 = R.string.not_add_own;
							handler.sendMessage(msg);
						} else if (s == 4) {

							msg.arg1 = R.string.ID_not_exist;
							handler.sendMessage(msg);
						} else if (s == 5) {

							msg.arg1 = R.string.you_have_friend;
							handler.sendMessage(msg);
						} else if (s == 6) {

							msg.arg1 = R.string.wait_agree;
							handler.sendMessage(msg);
						} else if (s == 7) {

							msg.arg1 = R.string.wait_agree;
							msg.obj = json.getString("content");
							handler.sendMessage(msg);
							String jpushid = (String) msg.obj;
							if (jpushid != null && !jpushid.equals("")) {
								Jpush jpush = new Jpush(getActivity());
								jpush.sendFriendMessage(notes, jpushid);
							}
						}
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					if (progressDialog != null && progressDialog.isShowing()) {
						progressDialog.dismiss();
					}
					e.printStackTrace();
				}
			}

		}).start();
	}

	void addFriend(String str, final String uid) {

		AlertDialog.Builder ab = new AlertDialog.Builder(con);
		ab.setTitle(getString(R.string.addnewfriend));
		final EditText e = new EditText(con);
		e.setHint(String.format(getString(R.string.add_info)));
		e.setHintTextColor(getResources().getColor(R.color.transparent_black3));
		LinearLayout l = new LinearLayout(con);
		// l.setBackgroundColor(getResources().getColor(R.color.transparent_black3));
		l.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		e.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		l.setPadding(20, 30, 20, 10);
		l.addView(e);
		ab.setView(l);

		ab.setNegativeButton(R.string.can, null);
		ab.setPositiveButton(R.string.send,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						String np = getString(R.string.friend_add);
						if (e.getText() != null
								&& e.getText().toString().length() > 0) {
							np = e.getText().toString();
						}

						sendRequest(uid, np);

					}
				});
		ab.show();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	boolean onpause = true;

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		onpause = true;
		super.onPause();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		onpause = false;
		super.onResume();
	}

	class SimpleAdapter extends BaseAdapter {
		Context context;
		List<UserHashMap<String, String>> listData;
		// HashMap<Integer, SoftReference<View>> map;
		ImageLoader iLoader;

		public SimpleAdapter(Context context,
				List<UserHashMap<String, String>> listData) {
			super();
			this.context = context;
			this.listData = listData;
			iLoader = ImageLoader.getInstance();
			// map = new HashMap<Integer, SoftReference<View>>();
		}

		@Override
		public void notifyDataSetChanged() {
			// TODO Auto-generated method stub
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
				convertView = getActivity().getLayoutInflater().inflate(
						R.layout.simple_rank_item, null);
				holder.ico = (BorderImageView) convertView
						.findViewById(R.id.photo);
				holder.name = (TextView) convertView.findViewById(R.id.name);

				holder.step = (TextView) convertView.findViewById(R.id.steps);
				holder.rank = (TextView) convertView.findViewById(R.id.rank);
				holder.rank.setTypeface(MilinkApplication.NumberFace);
				holder.unit = (TextView) convertView.findViewById(R.id.unit);
				convertView.setTag(holder);
			} else {
				// convertView = map.get(position).get();
				holder = (ViewHolder) convertView.getTag();
			}
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
			/*
			 * if (position == listData.size() - 1 && listData.size() % 10 == 0)
			 * { holder.more.setVisibility(View.VISIBLE);
			 * holder.more.setOnClickListener(new OnClickListener() {
			 * 
			 * @Override public void onClick(View v) {
			 * holder.more.setIndeterminateProgressMode(true);
			 * 
			 * 
			 * } }); holder.more.setProgress(0); } else
			 */{

			}

			ImageLoader.getInstance().displayImage(
					listData.get(position).get("url"), holder.ico,
					MilinkApplication.getListOptions(),
					new ImageLoadingListener() {

						@Override
						public void onLoadingStarted(String arg0, View arg1) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onLoadingFailed(String arg0, View arg1,
								FailReason arg2) {
							// TODO Auto-generated method stub
							if (onpause) {
								return;
							}
							try {
								boolean o = ImageLoader
										.getInstance()
										.getDiskCache()
										.save(arg0,
												BitmapFactory.decodeResource(
														getResources(),
														R.drawable.avatar));
								// System.out.println(o + "==" +
								// arg2.getCause());
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

						@Override
						public void onLoadingComplete(String arg0, View arg1,
								Bitmap arg2) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onLoadingCancelled(final String arg0,
								View arg1) {
							// TODO Auto-generated method stub
						}
					});

			holder.step.setText(listData.get(position).get("step") + "");
			holder.name.setText(listData.get(position).get("user")
					.startsWith("temp_") ? getString(R.string.tourist)
					: (listData.get(position).get("user")) + "");

			return convertView;
		}
	}

	public static class ViewHolder {
		TextView name, step, rank, unit;
		BorderImageView ico;
	}

	List<HashMap<String, String>> subList(List<HashMap<String, String>> lis,
			int from, int to) {
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
				listData = dbutils.getRankDataByPage(current_type + 10, 0);
				adapter = new SimpleAdapter(getActivity(), listData);
				list.setAdapter(adapter);
				break;
			case 10:
				if (msg.what == 10) {
					Toast.makeText(getActivity().getApplicationContext(),
							getString(R.string.network_erro),
							Toast.LENGTH_SHORT).show();
					ptrFrameLayout.refreshComplete();
				}
				break;
			case 1:

				// 4表示今天，1表示昨天，2表示周，3月
				Runnable runnable = null;

				runnable = new RunnableRankAll(share, db, current_type, 0);
				((RunnableRankAll) runnable)
						.setOnFinished(AllRankActivity.this);
				if (FriendFragment.isNetworkConnected(getActivity())) {
					dbutils.DeleTeRank(current_type + 10);
				}
				if (runnable != null) {
					new Thread(runnable).start();
				}
				break;
			case 2:

				// 4表示今天，1表示昨天，2表示周，3月
				Runnable runnable2 = null;

				runnable2 = new RunnableRankAll(share, db, current_type,
						msg.arg1);
				((RunnableRankAll) runnable2)
						.setOnFinished(AllRankActivity.this);

				if (runnable2 != null) {
					new Thread(runnable2).start();
				}
				break;
			case 3:
				listData.clear();
				listData.addAll(dbutils.getAllRankDataByPage(current_type + 10,
						msg.arg1));
				adapter.notifyDataSetChanged();
				break;
			case 4:
				if (progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				Toast.makeText(getActivity(), getString(msg.arg1),
						Toast.LENGTH_SHORT).show();
				break;
			}

			super.handleMessage(msg);
		}

	};

	int cur_color = 0x5f93ef;

	@Override
	public void Finished(int type, int index) {
		// TODO Auto-generated method stub
		System.out.println("getcallback-"
				+ com.bandlink.air.util.Util
						.getTimeMMStringFormat("yyyy-MM-dd HH:mm:ss:SSS"));
		if (index < 1) {
			handler.sendEmptyMessage(0);
			ptrFrameLayout.refreshComplete();
		} else {
			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						list.removeFooterView(foot);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			handler.obtainMessage(3, index, 0).sendToTarget();
			ptrFrameLayout.refreshComplete();
		}

	}

	@Override
	public void Error(int type) {
		// TODO Auto-generated method stub
		ptrFrameLayout.refreshComplete();
	}
}
package com.bandlink.air.friend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bandlink.air.Friendpage;
import com.bandlink.air.NewFriendActivity;
import com.bandlink.air.R;
import com.bandlink.air.friend.BaesExpandableAdapter.OnListListener;
import com.bandlink.air.friend.SideBar.OnTouchingLetterChangedListener;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Util;
import com.bandlink.air.view.NoRegisterDialog;
import com.bandlink.air.view.SwipeLayout;

public class BaseActivity extends LovefitActivity implements OnRefreshListener {
	private ExpandableListView expand_ctv;
	private SideBar bar;
	private CharacterParser characterParser;
	private List<String> parentList = new ArrayList<String>();
	private Map<String, List<User>> childList = new HashMap<String, List<User>>();
	private List<User> childItem = new ArrayList<User>();
	private Button btn_addfriend;
	SharedPreferences share;
	PopupWindow p;
	private NoRegisterDialog d;
	private Dbutils db;
	private String result, str = "[", nickname, photo;
	private int fuid, gid, suid;
	ProgressDialog dialog;
	ConnectivityManager cm;
	private SwipeLayout mSwipeLayout;
	Context mContext;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		share = getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		if (android.os.Build.VERSION.SDK_INT >= 11) {
			mContext = new ContextThemeWrapper(this,
					android.R.style.Theme_Holo_Light);
		} else {
			mContext = this;
		}
		suid = share.getInt("UID", suid);
		db = new Dbutils(-1, this);
		if (!isNetworkConnected()) {
			Toast.makeText(this, getString(R.string.connectexception),
					Toast.LENGTH_SHORT).show();
		} else if (share.getInt("ISMEMBER", 0) == 0) {
			new Thread(new ListThread()).start();
			d = new NoRegisterDialog(BaseActivity.this, R.string.no_register,
					R.string.no_register_content);
			d.show();
		} else {
			dialog =  Util.initProgressDialog(BaseActivity.this, true, getString(R.string.data_wait), null);
			new Thread(new ListThread()).start();
		}
		setContentView(R.layout.expandable_ctv);
		ActionbarSettings actionbar = new ActionbarSettings(this, back, null);
		actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
		// actionbar.setTopRightIcon(R.drawable.ic_top_more);
		actionbar.setTitle(R.string.btnfriend);
		expand_ctv = (ExpandableListView) findViewById(R.id.expand_ctv);
		btn_addfriend = (Button) findViewById(R.id.addfriend);

		btn_addfriend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!isNetworkConnected()) {
					Toast.makeText(BaseActivity.this,
							getString(R.string.connectexception),
							Toast.LENGTH_SHORT).show();
				} else if (share.getInt("ISMEMBER", 0) == 0) {
					d = new NoRegisterDialog(BaseActivity.this,
							R.string.no_register, R.string.no_register_content);
					d.show();
				} else {
					startActivity(new Intent(BaseActivity.this,
							NewFriendActivity.class));
				}
			}
		});
		bar = (SideBar) findViewById(R.id.sidebar);
		TextView t = (TextView) findViewById(R.id.dialog);
		bar.setTextView(t);
		bar.setBackgroundResource(R.color.transparent);
		bar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

			@Override
			public void onTouchingLetterChanged(String s) {
				// TODO Auto-generated method stub
				if (parentList.contains(s)) {
					expand_ctv.setSelectedGroup(parentList.indexOf(s));
				} else if (s.equals("#")) {
					expand_ctv.setSelectedGroup(0);
				} else if (s.equals("Z")) {
					expand_ctv.setSelectedGroup(expand_ctv
							.getExpandableListAdapter().getGroupCount());
				}
			}
		});
		characterParser = CharacterParser.getInstance();

		// 子类点击事件

		expand_ctv.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				Toast.makeText(
						BaseActivity.this,
						childList.get(parentList.get(groupPosition)).get(
								childPosition).name, 1000).show();
				return true;
			}
		});

		// 父类点击事件

		expand_ctv.setOnGroupClickListener(new OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {
				return true;
			}
		});

		// 设置点击背景色

		expand_ctv.setSelector(android.R.color.transparent);
		mSwipeLayout = (SwipeLayout) findViewById(R.id.slayout);
		mSwipeLayout.setViewGroup(expand_ctv);
		mSwipeLayout.setOnRefreshListener(this);
		mSwipeLayout.setColorScheme(R.color.holo_blue_bright,
				R.color.holo_green_light, R.color.holo_orange_light,
				R.color.holo_red_light);
	}

	public boolean isNetworkConnected() {
		try {
			cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		NetworkInfo ni = cm.getActiveNetworkInfo();
		return ni != null && ni.isConnectedOrConnecting();
	}

 

	public String getAvatarUrl(String uid) {
		String size = "middle";
		int u = Math.abs(Integer.parseInt(uid));
		uid = String.format("%09d", u);
		String dir1 = uid.substring(0, 3);
		String dir2 = uid.substring(3, 5);
		String dir3 = uid.substring(5, 7);
		String typeadd = "";
		String url = dir1 + "/" + dir2 + "/" + dir3 + "/" + uid.substring(7)
				+ typeadd + "_avatar_" + size + ".jpg";
		return url;
	}

	class ListThread implements Runnable {
		@Override
		public void run() {
			// //http://192.168.9.33/home/user/getUserFriend/session/14037120006
			Message msg = handler.obtainMessage();
			Map<String, String> map = new LinkedHashMap<String, String>();
			map.put("session", share.getString("session_id", ""));
			try {
				result = HttpUtlis.getRequest(HttpUtlis.GETUSERFRIEND, map);
				msg.what = 10;
				handler.sendMessage(msg);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
				e.printStackTrace();
			}

		}
	}

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				onRefresh();
				Toast.makeText(BaseActivity.this, R.string.delete_ok_refresh, 1)
						.show();
				break;
			case 1:
				Toast.makeText(BaseActivity.this, R.string.no_param, 1).show();
				break;
			case 2:
				Toast.makeText(BaseActivity.this, R.string.not_user, 1).show();
				break;
			case 3:
				Toast.makeText(BaseActivity.this, R.string.not_add_own, 1)
						.show();
				break;
			case 4:
				Toast.makeText(BaseActivity.this, R.string.ID_not_exist, 1)
						.show();
				break;
			case 10:
				User[] user = getUser();
				init(user);
				BaesExpandableAdapter adapter = new BaesExpandableAdapter(
						BaseActivity.this, parentList, childList);
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
				if (mSwipeLayout != null) {
					mSwipeLayout.setRefreshing(false);
				}
				adapter.setOnListListener(new OnListListener() {

					@Override
					public void onListItemClick(String name, int uid,
							int indexInList, final View vv, User us) {

						final int fuid = uid;
						final String name1 = name;

						String[] Items = { getString(R.string.deletefriend),
								getString(R.string.visite) };
						AlertDialog.Builder ab1 = new AlertDialog.Builder(
								mContext);
						ab1.setTitle(name1);
						ab1.setItems(Items,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface d,
											int which) {
										// TODO Auto-generated method stub
										if (which == 0) {
											dialog =  Util.initProgressDialog(BaseActivity.this, true, getString(R.string.data_wait), null);
											new Thread(new Runnable() {

												@Override
												public void run() {
													// TODO Auto-generated
													// method stub

													Message msg = handler
															.obtainMessage();
													Map<String, String> map = new LinkedHashMap<String, String>();
													map.put("session",
															share.getString(
																	"session_id",
																	""));
													map.put("fuid", "" + fuid);
													try {
														result = HttpUtlis
																.getRequest(
																		HttpUtlis.DELETEFRIEND,
																		map);
														if (result != null) {
															JSONObject json;
															json = new JSONObject(
																	result);
															int s = Integer
																	.valueOf(json
																			.getString("status"));
															if (s == 0) {
																msg.what = 0;
																handler.sendMessage(msg);
															} else if (s == 1) {
																msg.what = 1;
																handler.sendMessage(msg);
															} else if (s == 2) {
																msg.what = 2;
																handler.sendMessage(msg);
															} else if (s == 3) {
																msg.what = 3;
																handler.sendMessage(msg);
															} else if (s == 4) {
																msg.what = 4;
																handler.sendMessage(msg);
															}

															if (dialog != null
																	&& dialog
																			.isShowing()) {
																dialog.dismiss();
															}
														}

													} catch (Exception e) {
														// TODO Auto-generated
														// catch block
														e.printStackTrace();
														if (dialog != null
																&& dialog
																		.isShowing()) {
															dialog.dismiss();
														}
													}
												}

											}).start();
										} else if (which == 1) {
											Intent page = new Intent();
											page.putExtra("uid", fuid + "");
											page.putExtra("name", name1);
											page.setClass(BaseActivity.this,
													Friendpage.class);
											startActivity(page);
										}

									}
								});
						ab1.setNegativeButton(getString(R.string.close),
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface d1,
											int which) {
										// TODO Auto-generated method stub
										d1.dismiss();
									}
								});
						ab1.create().show();

					}

					@Override
					public void onListItemLongClick(String name, int uid,
							int indexInList, View v) {
						// TODO Auto-generated method stub

					}
				});
				expand_ctv.setAdapter(adapter);
				int groupCount = expand_ctv.getCount();
				for (int i = 0; i < groupCount; i++) {
					expand_ctv.expandGroup(i);
				}
				break;
			case 11:
				Toast.makeText(BaseActivity.this, "error", 1).show();
				break;
			}
		}
	};

	public User[] getUser() {

		try {
			if (result != null) {
				db.DeleteFriendList();
				JSONObject json;
				JSONArray arr;
				json = new JSONObject(result);
				arr = json.getJSONArray("content");
				User[] user = new User[arr.length()];
				for (int i = 0; i < arr.length(); i++) {
					JSONObject temp = (JSONObject) arr.get(i);
					gid = Integer.parseInt(temp.getString("gid"));
					fuid = Integer.parseInt(temp.getString("fuid"));
					nickname = temp.getString("nickname");
					photo = "http://www.lovefit.com/ucenter/data/avatar/"
							+ getAvatarUrl(String.valueOf(fuid));
					user[i] = new User(fuid, nickname, photo);
					db.InitFriendList(gid, nickname, gid, photo);
				}
				return user;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void init(User[] str) {
		parentList.clear();
		childList.clear();
		for (int i = 0; i < 27; i++) {
			String parent = (char) (((64 + i) == 64) ? 35 : (64 + i)) + "";

			childItem = new ArrayList<User>();
			for (User name : str) {
				if (characterParser.getSelling(name.name).substring(0, 1)
						.toUpperCase().matches("[A-Z]")) {
					if (characterParser.getSelling(name.name).substring(0, 1)
							.toUpperCase().equals(parent)) {
						childItem.add(name);
					}
				} else {
					if (i == 0) {
						childItem.add(name);
					}
				}
			}
			if (childItem.size() > 0) {
				parentList.add(parent);
			}
			childList.put(parent, childItem);
		}
	}

	private OnClickListener back = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			BaseActivity.this.finish();
		}
	};

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		if (!isNetworkConnected()) {
			Toast.makeText(this, getString(R.string.connectexception),
					Toast.LENGTH_SHORT).show();
		} else {
			dialog =  Util.initProgressDialog(BaseActivity.this, true, getString(R.string.data_wait), null);
			new Thread(new ListThread()).start();
		}

	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub
		
	}

}

package com.bandlink.air.friend;

import java.io.File;
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
import com.bandlink.air.R;
import com.bandlink.air.friend.BaesExpandableAdapter.OnListListener;
import com.bandlink.air.friend.SideBar.OnTouchingLetterChangedListener;
import com.bandlink.air.jpush.Jpush;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Util;
import com.bandlink.air.view.NoRegisterDialog;
import com.bandlink.air.view.SwipeLayout;
import com.nostra13.universalimageloader.utils.StorageUtils;

public class FansActivity extends LovefitActivity implements OnRefreshListener {
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
	private String result, str = "[", nickname, photo;
	private int fuid, suid;
	ProgressDialog dialog;
	ConnectivityManager cm;
	private SwipeLayout mSwipeLayout;
	Context mContext;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		share = getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		suid = share.getInt("UID", suid);
		if (android.os.Build.VERSION.SDK_INT >= 11) {
			mContext = new ContextThemeWrapper(this,
					android.R.style.Theme_Holo_Light);
		} else {
			mContext = this;
		}
		if (!isNetworkConnected()) {
			Toast.makeText(this, getString(R.string.connectexception),
					Toast.LENGTH_SHORT).show();
		} else if (share.getInt("ISMEMBER", 0) == 0) {
			new Thread(new ListThread()).start();
			d = new NoRegisterDialog(FansActivity.this, R.string.no_register,
					R.string.no_register_content);
			d.show();
		} else {
			dialog = Util.initProgressDialog(this, true, getString(R.string.data_wait), null);
			new Thread(new ListThread()).start();
		}
		File cacheDir = StorageUtils.getOwnCacheDirectory(this,
				"UniversalImageLoader/Cache");
		 
		setContentView(R.layout.expandable_ctv);
		ActionbarSettings actionbar = new ActionbarSettings(this, back, null);
		actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
		// actionbar.setTopRightIcon(R.drawable.ic_top_more);
		actionbar.setTitle(R.string.myfans);
		expand_ctv = (ExpandableListView) findViewById(R.id.expand_ctv);
		btn_addfriend = (Button) findViewById(R.id.addfriend);
		btn_addfriend.setVisibility(View.GONE);
		bar = (SideBar) findViewById(R.id.sidebar);
		TextView t = (TextView) findViewById(R.id.dialog);
		bar.setTextView(t);
		bar.setBackgroundResource(R.color.transparent);
		bar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

			@Override
			public void onTouchingLetterChanged(String s) {
				// TODO Auto-generated method stub
				if (childList != null & childList.size() != 0) {
					expand_ctv.setSelectedGroup(((int) s.charAt(0) - 64) < 0 ? 0
							: (int) s.charAt(0) - 64);
				}
			}
		});
		characterParser = CharacterParser.getInstance();

		expand_ctv.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				Toast.makeText(
						FansActivity.this,
						childList.get(parentList.get(groupPosition)).get(
								childPosition).name, 1000).show();
				return true;
			}
		});
		// 锟斤拷锟斤拷锟斤拷锟铰硷拷
		expand_ctv.setOnGroupClickListener(new OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {
				return true;
			}
		});
		// 锟斤拷锟矫碉拷锟斤拷锟斤拷锟缴�
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

			Message msg = handler.obtainMessage();
			Map<String, String> map = new LinkedHashMap<String, String>();
			map.put("uid", suid + "");
			try {
				result = HttpUtlis.getRequest(HttpUtlis.FRIEND_GETFOLLOWERLIST,
						map);
				if (result != null) {
					JSONObject json;
					json = new JSONObject(result);
					int s = Integer.valueOf(json.getString("status"));
					if (s == 0) {
						msg.what = 0;
						handler.sendMessage(msg);
					} else if (s == 1) {
						msg.what = 1;
						handler.sendMessage(msg);
					} else if (s == 2) {
						msg.what = 2;
						handler.sendMessage(msg);
					}
				}
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				User[] user = getUser();
				init(user);
				BaesExpandableAdapter adapter = new BaesExpandableAdapter(
						FansActivity.this, parentList, childList);
				if (mSwipeLayout != null) {
					mSwipeLayout.setRefreshing(false);
				}
				adapter.setOnListListener(new OnListListener() {
					@Override
					public void onListItemClick(String name, int uid,
							int indexInList, View vv, User us) {
						final String name1 = name;
						final int fuid = uid;
						String[] Items = { getString(R.string.a_friend),
								getString(R.string.add_guanzhu),
								getString(R.string.visite) };
						AlertDialog.Builder ab1 = new AlertDialog.Builder(
								mContext);
						ab1.setTitle(name);
						ab1.setItems(Items,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface d,
											int which) {
										// TODO Auto-generated method stub
										if (which == 0) {
											dialog =  Util.initProgressDialog(FansActivity.this, true, getString(R.string.data_wait), null);
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
													map.put("fuid", fuid + "");
													try {
														result = HttpUtlis
																.getRequest(
																		HttpUtlis.FRIEND_ADDFRIEND,
																		map);

														if (result != null) {
															JSONObject json;
															json = new JSONObject(
																	result);
															int s = Integer
																	.valueOf(json
																			.getString("status"));
															if (s == 0) {
																msg.obj = json
																		.getString("content");
																msg.what = 8;
																handler.sendMessage(msg);
															} else if (s == 1) {
																msg.what = 9;
																handler.sendMessage(msg);
															} else if (s == 2) {
																msg.what = 10;
																handler.sendMessage(msg);
															} else if (s == 3) {
																msg.what = 11;
																handler.sendMessage(msg);
															} else if (s == 4) {
																msg.what = 12;
																handler.sendMessage(msg);
															} else if (s == 5) {
																msg.what = 13;
																handler.sendMessage(msg);
															} else if (s == 6) {
																msg.what = 14;
																handler.sendMessage(msg);
															}
														}
														if (dialog != null
																&& dialog
																		.isShowing()) {
															dialog.dismiss();
														}
													} catch (Exception e) {
														// TODO Auto-generated
														// catch block
														e.printStackTrace();
													}
												}

											}).start();
										} else if (which == 1) {
											dialog =  Util.initProgressDialog(FansActivity.this, true, getString(R.string.data_wait), null);
											new Thread(new Runnable() {
												@Override
												public void run() {
													// TODO Auto-generated
													// method stub

													Message msg = handler
															.obtainMessage();
													Map<String, String> map = new LinkedHashMap<String, String>();
													map.put("uid", suid + "");
													map.put("fid", "" + fuid);
													try {
														result = HttpUtlis
																.getRequest(
																		HttpUtlis.FRIEND_GUANZHU,
																		map);
														if (result != null) {
															JSONObject json;
															json = new JSONObject(
																	result);
															int s = Integer
																	.valueOf(json
																			.getString("status"));
															if (s == 0) {
																msg.what = 3;
																handler.sendMessage(msg);
															} else if (s == 1) {
																msg.what = 4;
																handler.sendMessage(msg);
															} else if (s == 2) {
																msg.what = 5;
																handler.sendMessage(msg);
															} else if (s == 3) {
																msg.what = 6;
																handler.sendMessage(msg);
															} else if (s == 4) {
																msg.what = 7;
																handler.sendMessage(msg);
															}
														}
														if (dialog != null
																&& dialog
																		.isShowing()) {
															dialog.dismiss();
														}

													} catch (Exception e) {
														// TODO Auto-generated
														// catch block
														e.printStackTrace();
														msg.what = 15;
														handler.sendMessage(msg);
													}

												}

											}).start();
										} else if (which == 2) {
											Intent page = new Intent();
											page.putExtra("uid", fuid + "");
											page.putExtra("name", name1);
											page.setClass(FansActivity.this,
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

			case 1:
				Toast.makeText(FansActivity.this, R.string.plogin, 1).show();
				break;
			case 2:
				Toast.makeText(FansActivity.this, R.string.no_fans, 1).show();
				break;
			case 3:
				Toast.makeText(FansActivity.this, R.string.guanzhu_ok, 1)
						.show();
				break;
			case 4:
				Toast.makeText(FansActivity.this, R.string.no_param, 1).show();
				break;

			case 5:
				Toast.makeText(FansActivity.this, R.string.not_own, 1).show();

				break;
			case 6:
				Toast.makeText(FansActivity.this, R.string.user_no_exist, 1)
						.show();
				break;
			case 7:
				Toast.makeText(FansActivity.this, R.string.have_guanzhu, 1)
						.show();
				break;
			case 8:
				String jpushid = (String) msg.obj;
				if (jpushid != null && !jpushid.equals("")) {
					Jpush jpush = new Jpush(FansActivity.this);
					String notes = getString(R.string.friend_add);
					jpush.sendFriendMessage(notes, jpushid);
				}
				Toast.makeText(FansActivity.this, "ok", 1).show();
				break;
			case 9:
				Toast.makeText(FansActivity.this, R.string.no_param, 1).show();
				break;
			case 10:
				Toast.makeText(FansActivity.this, R.string.not_user, 1).show();
				break;
			case 11:
				Toast.makeText(FansActivity.this, R.string.not_add_own, 1)
						.show();
				break;
			case 12:
				Toast.makeText(FansActivity.this, R.string.ID_not_exist, 1)
						.show();
				break;
			case 13:
				Toast.makeText(FansActivity.this, R.string.you_have_friend, 1)
						.show();
				break;
			case 14:
				Toast.makeText(FansActivity.this, R.string.wait_agree, 1)
						.show();
				break;
			case 15:
				Toast.makeText(FansActivity.this, "error", 1).show();
				break;
			}
		}
	};

	public User[] getUser() {

		try {
			if (result != null) {

				JSONObject json;
				JSONArray arr;
				json = new JSONObject(result);
				arr = json.getJSONArray("content");
				User[] user = new User[arr.length()];
				for (int i = 0; i < arr.length(); i++) {
					JSONObject temp = (JSONObject) arr.get(i);
					fuid = Integer.parseInt(temp.getString("uid"));
					nickname = temp.getString("name");
					if (nickname == null || nickname.equals("")
							|| nickname.equals("null")) {
						nickname = getString(R.string.tourist);
					}
					photo = "http://www.lovefit.com/ucenter/data/avatar/"
							+ getAvatarUrl(String.valueOf(fuid));
					user[i] = new User(fuid, nickname, photo);
					str += "{'name':'" + nickname + "','uid':'" + fuid
							+ "','photo':'" + photo + "'},";
				}
				str = str.substring(0, str.length() - 1) + "]";
				return user;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/***
	 * 锟皆诧拷锟斤拷user[] 锟斤拷锟�锟斤拷锟矫伙拷锟斤拷锟斤拷锟斤拷锟侥革拷锟斤拷锟�
	 * 
	 * @param str
	 *            user锟斤拷锟斤拷
	 */
	public void init(User[] str) {
		parentList.clear();
		childList.clear();
		for (int i = 0; i < 27; i++) {
			String parent = (char) (((64 + i) == 64) ? 35 : (64 + i)) + "";

			childItem = new ArrayList<User>();
			System.out.println(str.toString() + "......" + str.length);
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
			FansActivity.this.finish();
		}
	};

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		if (!isNetworkConnected()) {
			Toast.makeText(this, getString(R.string.connectexception),
					Toast.LENGTH_SHORT).show();
		} else {
			dialog =  Util.initProgressDialog(this, true, getString(R.string.data_wait), null);
			new Thread(new ListThread()).start();
		}

		if (mSwipeLayout != null) {
			mSwipeLayout.setRefreshing(false);
		}
	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub
		
	}

}

package com.bandlink.air.club;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.bandlink.air.R;
import com.bandlink.air.friend.BaesExpandableAdapter;
import com.bandlink.air.friend.BaesExpandableAdapter.OnListListener;
import com.bandlink.air.friend.CharacterParser;
import com.bandlink.air.friend.SideBar;
import com.bandlink.air.friend.SideBar.OnTouchingLetterChangedListener;
import com.bandlink.air.friend.User;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.DbContract.ClubGroup;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.view.SwipeLayout;

@SuppressLint("InlinedApi")
public class ClubMemberManager extends LovefitActivity implements
		OnRefreshListener {
	private ExpandableListView expand_ctv;
	private SideBar bar;
	private CharacterParser characterParser;
	private List<String> parentList = new ArrayList<String>();
	private Map<String, List<User>> childList = new HashMap<String, List<User>>();
	private List<User> childItem = new ArrayList<User>();
	private Button btn_addfriend, selectgroup;
	private SharedPreferences share; 
	private Dbutils db;
	private String clubid, session_id;
	private int fuid, suid; 
	private SwipeLayout sLayout;
	private String currentGroup = "";
	private ProgressDialog dialog;
	private ArrayList<Integer> height = new ArrayList<Integer>();
	private ArrayList<HashMap<String, String>> allGroup = new ArrayList<HashMap<String, String>>();
	ArrayList<HashMap<String, String>> group;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		share = getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		suid = share.getInt("UID", suid);
		session_id = share.getString("session_id", "");
		db = new Dbutils(suid, this);
		clubid = getIntent().getStringExtra("clubid");
		if (clubid == null) {
			this.finish();
		}
		setContentView(R.layout.expandable_ctv);
		ActionbarSettings actionbar = new ActionbarSettings(this, back, null);
		actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
		// actionbar.setTopRightIcon(R.drawable.ic_top_more);
		actionbar.setTitle(R.string.manageclube);
		expand_ctv = (ExpandableListView) findViewById(R.id.expand_ctv);
		btn_addfriend = (Button) findViewById(R.id.addfriend);
		btn_addfriend.setVisibility(View.GONE);
		selectgroup = (Button) findViewById(R.id.select);
		selectgroup.setVisibility(View.VISIBLE);
		characterParser = CharacterParser.getInstance();
		sLayout = (SwipeLayout) findViewById(R.id.slayout);
		// scrollView = (ScrollView) findViewById(R.id.scrollView);
		sLayout.setViewGroup(expand_ctv);
		sLayout.setOnRefreshListener(this);
		sLayout.setColorScheme(R.color.holo_blue_bright,
				R.color.holo_green_light, R.color.holo_orange_light,
				R.color.holo_red_light);
		// 获得所有分组
		getAllGroup();
		// 所有成员
		getMembers("");
		selectgroup.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (group == null || group.size() < 1) {
					group = new ArrayList<HashMap<String, String>>();
					HashMap<String, String> map = new HashMap<String, String>();
					map.put(ClubGroup.CLUBID, clubid);
					map.put(ClubGroup.NAME, getString(R.string.defaultgroup));
					map.put(ClubGroup.GROUPID, "0");
					group.add(map);
				} else if (group != null
						&& !group.get(group.size() - 1).get("groupid")
								.equals("0")) {
					HashMap<String, String> map = new HashMap<String, String>();
					map.put(ClubGroup.CLUBID, clubid);
					map.put(ClubGroup.NAME, getString(R.string.defaultgroup));
					map.put(ClubGroup.GROUPID, "0");
					group.add(map);

				}

				if (android.os.Build.VERSION.SDK_INT >= 11) {
					AlertDialog.Builder alert;

					alert = new AlertDialog.Builder(new ContextThemeWrapper(
							ClubMemberManager.this,
							android.R.style.Theme_Holo_Light));

					alert.setTitle(R.string.selectgroup);
					alert.setAdapter(new ClubGroupAdapter(
							ClubMemberManager.this, group),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									String id = "";
									try {
										id = group.get(arg1).get(
												ClubGroup.GROUPID);
									} catch (Exception e) {

									}
									if (id.equals("0")) {
										currentGroup = "";
										selectgroup
												.setText(getString(R.string.defaultgroup));
									} else {
										currentGroup = id;
										selectgroup.setText(group.get(arg1)
												.get(ClubGroup.NAME));
									}
									getMembers(currentGroup);

								}
							});
					alert.create().show();
				}/* else {
					pop = loadGroup(group);

					if (pop != null) {
						pop.showAtLocation(v.getRootView(), Gravity.CENTER, 0,
								0);
					} else {
						selectgroup.setText(getString(R.string.defaultgroup));
					}
				}*/
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
				// MyLog.w("Tests", s);

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
		expand_ctv.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				Toast.makeText(
						ClubMemberManager.this,
						childList.get(parentList.get(groupPosition)).get(
								childPosition).name, 1000).show();
				return true;
			}
		});

		expand_ctv.setOnGroupClickListener(new OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {
				return true;
			}
		});
		expand_ctv.setSelector(android.R.color.transparent);
	}

/*	@SuppressWarnings("unchecked")
	private PopupWindow loadGroup(ArrayList<HashMap<String, String>> list) {
		View fa = getLayoutInflater().inflate(R.layout.pop_club_group, null);
		pop = new PopupWindow(fa, LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT, true);
		pop.setOutsideTouchable(true);
		pop.setBackgroundDrawable(new BitmapDrawable());
		ListView listview = (ListView) fa.findViewById(R.id.list);
		listview.setAdapter(new ClubGroupAdapter(this, list));
		TextView title = (TextView) fa.findViewById(R.id.name);
		title.setText(getString(R.string.selectgroup));
		fa.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (pop != null && pop.isShowing()) {
					pop.dismiss();
				}
			}
		});
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				TextView t = (TextView) ((ViewGroup) view).getChildAt(0);
				if (t.getTag() == null) {
					currentGroup = "";
				} else {
					currentGroup = t.getTag().toString();
				}
				getMembers(currentGroup);
				selectgroup.setText(t.getText());
				if (pop != null && pop.isShowing()) {
					pop.dismiss();
				}
			}
		});
		return pop;
	}*/

	public Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if (dialog != null && dialog.isShowing()) {
				dialog.dismiss();
			}
			if (sLayout != null && sLayout.isRefreshing()) {
				sLayout.setRefreshing(false);
			}
			switch (msg.what) {
			case 0:
				// 请求分组后
				group = db.getGroupInfo(clubid);
				HashMap<String, String> map = new HashMap<String, String>();
				map.put(ClubGroup.CLUBID, clubid);
				map.put(ClubGroup.NAME, getString(R.string.defaultgroup));
				map.put(ClubGroup.GROUPID, "0");
				group.add(map);
				allGroup.clear();
				allGroup = group;

				break;
			case 1:
				// 请求成员后
				User[] users;
				try {
					users = db.getMemberReturnUser(clubid, msg.obj.toString());
				} catch (Exception e) {
					users = db.getMemberReturnUser(clubid, "");
				}
				init(users);

				break;
			case 2:
				// 数据初始化完成
				BaesExpandableAdapter adapter = new BaesExpandableAdapter(
						ClubMemberManager.this, parentList, childList);
				expand_ctv.setAdapter(adapter);
				// setListViewHeightBasedOnChildren(expand_ctv);
				int groupCount = expand_ctv.getCount();
				for (int i = 0; i < groupCount; i++) {
					expand_ctv.expandGroup(i); 
				}
				adapter.setOnListListener(new OnListListener() {

					@Override
					public void onListItemLongClick(String name, int uid,
							int indexInList, View v) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onListItemClick(String name, int uid,
							int indexInList, View v, User user) {
						final int id = uid;
						final User tempuser = user;
						final View view = v;
						Context context = ClubMemberManager.this;

						context = new ContextThemeWrapper(
								ClubMemberManager.this,
								android.R.style.Theme_Holo_Light);
						AlertDialog.Builder ab = new AlertDialog.Builder(
								context);
						ab.setTitle(name);
						ab.setItems(new String[] { getString(R.string.delete),
								getString(R.string.moveto),
								getString(R.string.set_master) },
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										switch (which) {
										case 0:
											if (id == suid) {
												Toast.makeText(
														ClubMemberManager.this,
														getString(R.string.deleteself),
														Toast.LENGTH_SHORT)
														.show();
												return;
											}
											deleteMember(id);
											childItem.remove(tempuser);
											view.setVisibility(View.GONE);
											db.deleteMemberFromDB(clubid, id
													+ "");

											break;
										case 1:
											if (allGroup
													.get(allGroup.size() - 1)
													.get("groupid").equals("0")) {
												allGroup.remove(allGroup.size() - 1);
											}
											if (allGroup.size() <= 0) {
												Toast.makeText(
														ClubMemberManager.this,
														getString(R.string.nogroup),
														Toast.LENGTH_SHORT)
														.show();
												return;
											}
											AlertDialog.Builder alert;
											if (android.os.Build.VERSION.SDK_INT >= 11) {
												alert = new AlertDialog.Builder(
														new ContextThemeWrapper(
																ClubMemberManager.this,
																android.R.style.Theme_Holo_Light));
											} else {
												alert = new AlertDialog.Builder(
														ClubMemberManager.this);
											}
											alert.setTitle(R.string.moveto);
											alert.setAdapter(
													new ClubGroupAdapter(
															ClubMemberManager.this,
															allGroup),
													new DialogInterface.OnClickListener() {

														@Override
														public void onClick(
																DialogInterface arg0,
																int arg1) {
															moveMemberTo(
																	id,
																	allGroup.get(
																			arg1)
																			.get("groupid"));
															arg0.dismiss();
														}
													});
											alert.create().show();
											break;
										case 2:
											Toast.makeText(
													ClubMemberManager.this,
													getString(R.string.opensoon),
													Toast.LENGTH_SHORT).show();
											break;
										}
									}
								});
						ab.create().show();

					}
				});
				break;
			case 3:
				Toast.makeText(ClubMemberManager.this,
						getString(R.string.deletesuccess), Toast.LENGTH_SHORT)
						.show(); 
				break;
			case 4:
				Toast.makeText(ClubMemberManager.this,
						getString(R.string.deleteerror), Toast.LENGTH_SHORT)
						.show();
				break;
			case 5:
				Toast.makeText(ClubMemberManager.this,
						getString(R.string.deleteerror), Toast.LENGTH_SHORT)
						.show();
				break;
			case 6:
				// moveto
				Toast.makeText(ClubMemberManager.this,
						getString(R.string.movetosuccess), Toast.LENGTH_SHORT)
						.show();
				break;
			}
			super.handleMessage(msg);
		}

	};

	private void moveMemberTo(final int uid, final String gid) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("session", session_id);
				map.put("clubid", clubid);
				map.put("uids", uid + "");
				map.put("groupid", gid);
				try {
					String result = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/data/transferGroup", map);
					JSONObject js = new JSONObject(result);
					if (js.getString("status").equals("0")) {
						handler.obtainMessage(6).sendToTarget();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}).start();
	}

	private void deleteMember(final int uid) {

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("session", session_id);
				map.put("clubid", clubid);
				map.put("uids", uid + "");
				try {
					String result = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/data/batchDeleteClubMembers", map);
					JSONObject js = new JSONObject(result);
					if (js.getString("status").equals("0")) {
						handler.sendEmptyMessage(3);
					} else {
						handler.sendEmptyMessage(4);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					handler.sendEmptyMessage(5);
				}
			}

		}).start();
	}

	// 获得分组
	private void getAllGroup() {
		group = db.getGroupInfo(clubid);

		HashMap<String, String> de = new HashMap<String, String>();
		de.put(ClubGroup.CLUBID, clubid);
		de.put(ClubGroup.NAME, getString(R.string.defaultgroup));
		de.put(ClubGroup.GROUPID, "0");
		group.add(de);
		allGroup.clear();
		allGroup = group;

		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Map<String, String> map = new HashMap<String, String>();
				map.put("session", session_id);
				map.put("clubid", clubid);
				String resultJoin;
				try {
					resultJoin = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/data/getClubGroup", map);
					JSONArray json = new JSONObject(resultJoin)
							.getJSONArray("content");
					for (int i = 0; i < json.length(); i++) {
						JSONObject jobj = json.getJSONObject(i);
						db.UpdateGroup(jobj.getString("clubid"),
								jobj.getString("groupname"),
								jobj.getString("id"));

					}
					handler.sendEmptyMessage(0);
					// getGroupFromDB(clubId);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					// getGroupFromDB(clubId);
				}
			}
		}).start();

	}

	public void RefreshGroup() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Map<String, String> map = new HashMap<String, String>();
				map.put("session", session_id);
				map.put("clubid", clubid);
				String resultJoin;
				try {
					resultJoin = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/data/getClubGroup", map);
					JSONArray json = new JSONObject(resultJoin)
							.getJSONArray("content");
					for (int i = 0; i < json.length(); i++) {
						JSONObject jobj = json.getJSONObject(i);
						db.UpdateGroup(jobj.getString("clubid"),
								jobj.getString("groupname"),
								jobj.getString("id"));

					}
					handler.sendEmptyMessage(0);
					// getGroupFromDB(clubId);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					// getGroupFromDB(clubId);
				}
			}
		}).start();
	}

	public ProgressDialog initProgressDialog() {
		ProgressDialog progressDialog = ProgressDialog.show(this,
				getString(R.string.data_wait),
				getString(R.string.data_getting), true);
		progressDialog.setCancelable(true);

		LayoutInflater inflater = LayoutInflater.from(this
				.getApplicationContext());
		View v = inflater.inflate(R.layout.loading, null);
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
						dialog.dismiss();
						dialog = null;
					}
				});

		return progressDialog;
	}

	/***
	 * 获得成员
	 * 
	 * @param id
	 *            部门id
	 */
	private void getMembers(final String id) {
		dialog = initProgressDialog();
		User[] users = db.getMemberReturnUser(clubid, id);
		if (users.length > 0) {
			init(users);
		} else {
			new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					Map<String, String> map = new HashMap<String, String>();
					map.put("session", session_id);
					map.put("clubid", clubid);
					String resultJoin;
					try {
						resultJoin = HttpUtlis.getRequest(HttpUtlis.BASE_URL
								+ "/data/getClubMember", map);
						JSONArray json = new JSONObject(resultJoin)
								.getJSONArray("content");
						for (int i = 0; i < json.length(); i++) {
							JSONObject jobj = json.getJSONObject(i);
							db.UpdateMemberInfo(
									clubid,
									jobj.getString("gid"),
									jobj.getString("nickname"),
									jobj.getString("uid"),
									HttpUtlis.AVATAR_URL
											+ HttpUtlis.getAvatarUrl(jobj
													.getString("uid")),
									jobj.getString("isadmin"));
						}
						Message msg = new Message();
						msg.what = 1;
						msg.obj = id;
						handler.sendMessage(msg);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();

					}

				}

			}).start();
		}
	}

	public void RefreshMemberInfo(final String id) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Map<String, String> map = new HashMap<String, String>();
				map.put("session", session_id);
				map.put("clubid", clubid);
				String resultJoin;
				try {
					resultJoin = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/data/getClubMember", map);
					JSONArray json = new JSONObject(resultJoin)
							.getJSONArray("content");
					for (int i = 0; i < json.length(); i++) {
						JSONObject jobj = json.getJSONObject(i);
						db.UpdateMemberInfo(
								clubid,
								jobj.getString("gid"),
								jobj.getString("nickname"),
								jobj.getString("uid"),
								HttpUtlis.AVATAR_URL
										+ HttpUtlis.getAvatarUrl(jobj
												.getString("uid")),
								jobj.getString("isadmin"));
					}
					Message msg = new Message();
					msg.what = 1;
					msg.obj = id;
					handler.sendMessage(msg);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Toast.makeText(ClubMemberManager.this,
							getString(R.string.network_erro),
							Toast.LENGTH_SHORT).show();
					User[] users = db.getMemberReturnUser(clubid, id);
					if (users.length > 0) {
						init(users);
					} else {

					}
					e.printStackTrace();

				}

			}

		}).start();
	}

	/***
	 * 生成适配器所需数据
	 * 
	 * @param str
	 *            用户信息数组
	 */
	public void init(User[] str) {
		parentList.clear();
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
		handler.sendEmptyMessage(2);
	}

	private OnClickListener back = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			ClubMemberManager.this.finish();
		}
	};

	/***
	 * 分组适配器
	 * 
	 * @author Kevin
	 * 
	 */
	public class ClubGroupAdapter extends BaseAdapter {
		Context context;
		ArrayList<HashMap<String, String>> list;

		public ClubGroupAdapter(Context context,
				ArrayList<HashMap<String, String>> list) {
			super();
			this.context = context;
			this.list = list;

		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub

			convertView = LayoutInflater.from(context).inflate(
					R.layout.item_club_group, null);
			TextView name = (TextView) convertView.findViewById(R.id.name);
			name.setText(list.get(position).get(ClubGroup.NAME));
			name.setTag(list.get(position).get(ClubGroup.GROUPID));
			return convertView;
		}
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		// 获得所有分组
		RefreshGroup();
		// 所有成员
		RefreshMemberInfo(currentGroup);

	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub

	}
}

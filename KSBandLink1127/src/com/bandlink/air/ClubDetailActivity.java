package com.bandlink.air;

//汉子未检查修改
import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.bandlink.air.Js.ClubeDetailJsInterface;
import com.bandlink.air.bluetooth.protocol.ComposePacket;
import com.bandlink.air.club.ClubMemberManager;
import com.bandlink.air.club.ClubSportAnalysis;
import com.bandlink.air.satellite.SatelliteMenu;
import com.bandlink.air.satellite.SatelliteMenu.SateliteClickedListener;
import com.bandlink.air.satellite.SatelliteMenuItem;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.DbContract.ClubDetail;
import com.bandlink.air.util.DbContract.ClubGroup;
import com.bandlink.air.util.DbContract.ClubMember;
import com.bandlink.air.util.DbContract.ClubRank;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.MApplication;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.UserHashMap;
import com.bandlink.air.util.Util;
import com.bandlink.air.util.WebViewHelper;
import com.bandlink.air.view.NoRegisterDialog;
import com.bandlink.air.view.SwipeLayout;
import com.polites.android.GestureImageView;

public class ClubDetailActivity extends LovefitActivity implements
		OnRefreshListener {

	private WebViewHelper help;
	public String clubId; // id of club
	private PopupWindow po;
	private WebView web;
	boolean isInit = true;
	private JSONObject thisDetail;
	private ClubeDetailJsInterface ci;
	private PopupWindow pop;
	private SharedPreferences share;
	private int admin_uid = -1;
	private JSONObject baseData;
	private SwipeLayout mSwipeLayout;
	public int currentTab = 0;
	private int ismember = 0;
	private NoRegisterDialog d;
	private int muid;
	private String session_id;
	private Dbutils db;
	private String mname;
	private SatelliteMenu menu;
	private LinearLayout frame;
	float ds;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		setContentView(R.layout.friend_frame);
		ds = getResources().getDisplayMetrics().density;
		clubId = getIntent().getStringExtra("id");
		share = getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		ActionbarSettings actionbar = new ActionbarSettings(this, lsr,
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						isMember();
						showOperate();
					}
				});
		actionbar.setTopRightIcon(R.drawable.ic_top_more);
		actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
		actionbar.setTitle(R.string.ab_clubdetail);

		web = (WebView) findViewById(R.id.web);
		if (Locale.getDefault().getLanguage().equals("zh")) {

			help = new WebViewHelper(this, web,
					"file:///android_asset/sell/3.html");
		}else if(Locale.getDefault().getLanguage().equals("zh-TW")){
			help = new WebViewHelper(this, web,
					"file:///android_asset/sell/3-tw.html");
		} else {
			help = new WebViewHelper(this, web,
					"file:///android_asset/sell/3-en.html");
		}
		ci = new ClubeDetailJsInterface(this);
		help.addJavascriptInterface(ci, "ClubDetail");
		help.initWebview();
		help.setBgTransparent();
		mSwipeLayout = (SwipeLayout) findViewById(R.id.id_swipe_ly);
		mSwipeLayout.setViewGroup(web);
		mSwipeLayout.setOnRefreshListener(this);
		mSwipeLayout.setColorScheme(R.color.holo_blue_bright,
				R.color.holo_green_light, R.color.holo_orange_light,
				R.color.holo_red_light);
		ismember = share.getInt("ISMEMBER", 0);
		muid = share.getInt("UID", -1);
		session_id = share.getString("session_id", "-1");
		mname = share.getString("NICKNAME", "lovefit");
		db = new Dbutils(muid, this);
		menu = (SatelliteMenu) findViewById(R.id.menu);
		menu.setSatelliteDistance((int) (100 * ds));
		menu.setExpandDuration((int) (100 * ds));
		menu.setCloseItemsOnClick(true);
		menu.setMainImage(getResources().getDrawable(R.drawable.edit));
		menu.setTotalSpacingDegree(90);
		List<SatelliteMenuItem> items = new ArrayList<SatelliteMenuItem>();
		items.add(new SatelliteMenuItem(3, R.drawable.fr_mess));
		items.add(new SatelliteMenuItem(0, R.drawable.ppp));
		items.add(new SatelliteMenuItem(2, R.drawable.fr_pic));
		items.add(new SatelliteMenuItem(0, R.drawable.ppp));
		items.add(new SatelliteMenuItem(1, R.drawable.fr_crt));
		menu.addItems(items);
		frame = (LinearLayout) findViewById(R.id.frame);
		frame.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (frame.getVisibility() == View.VISIBLE) {
					menu.onClick();
				}
			}
		});
		menu.setOnItemClickedListener(new SateliteClickedListener() {

			public void eventOccured(int id) {

				switch (id - 1) {
				case 2:
					frame.setVisibility(View.GONE);
					Intent ts = new Intent();
					ts.putExtra("tid", 2);
					ts.putExtra("from", clubId);
					ts.setClass(ClubDetailActivity.this,
							ShareEditActivity.class);
					startActivityForResult(ts, 2);
					break;
				case 1:

					frame.setVisibility(View.GONE);
					Intent sa = new Intent();
					sa.putExtra("tid", 1);
					sa.putExtra("from", clubId);
					sa.setClass(ClubDetailActivity.this,
							ShareEditActivity.class);
					startActivityForResult(sa, 1);
					break;
				case 0:

					frame.setVisibility(View.GONE);
					Intent s = new Intent();
					s.putExtra("tid", 0);
					s.putExtra("from", clubId);
					s.setClass(ClubDetailActivity.this, ShareEditActivity.class);
					startActivityForResult(s, 0);
					break;
				}
			}

			@Override
			public void mainImgClick() {

				if (menu.getIsExpand()) {
					frame.setVisibility(View.VISIBLE);
					frame.setBackgroundResource(R.color.tran_black);
					;
				} else {
					frame.setVisibility(View.GONE);
				}

			}
		});
		getClubFeed(1);
		super.onCreate(savedInstanceState);
	}

	public String clubName;

	/***
	 * 请求并存储俱乐部基本数据
	 */
	public void pushBaseData() {
		// getDetailFromDb(clubId);
		getBaseFromNet();
	}

	public void getBaseFromNet() {

		new Thread(new Runnable() {
			@Override
			public void run() {

				Map<String, String> map = new HashMap<String, String>();
				map.put("session", session_id);
				map.put("clubid", clubId);
				try {
					String result = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/data/getClubDetail", map);
					JSONObject json = new JSONObject(result);
					JSONObject jobj = json.getJSONObject("content");
					baseData = jobj;
					admin_uid = jobj.getInt("admin_uid");
					clubName = jobj.getString("name");
					db.UpdateClubDetail(
							jobj.getString("id"),
							jobj.getString("name"),
							jobj.getString("membernum"),
							jobj.getString("ispublic"),
							jobj.getString("provicne"),
							jobj.getString("city"),
							jobj.getString("district"),
							jobj.getString("intro"),
							jobj.getString("admin_username"),
							jobj.getString("admin_uid"),
							jobj.getString("groupnum"),
							HttpUtlis.CLUBLOGO_URL
									+ HttpUtlis.getLogoUrl(jobj.getString("id")));

					getDetailFromDb(clubId);

				} catch (Exception e) {

					getDetailFromDb(clubId);
					e.printStackTrace();
				}
			}
		}).start();
	}

	/***
	 * 获取db存储的俱乐部详情
	 * 
	 * @param id
	 *            查询的clubid
	 */
	public void getDetailFromDb(String id) {
		ArrayList<HashMap<String, String>> alist = db.getClubDetailFromDb(id);
		if (alist.size() < 1) {
			getBaseFromNet();
			return;
		}
		JSONObject js = new JSONObject();
		try {
			js.accumulate("id", alist.get(0).get(ClubDetail.CLUBID));
			js.accumulate("name", alist.get(0).get(ClubDetail.NAME));
			js.accumulate("membernum", alist.get(0).get(ClubDetail.MEMBERNUM));
			js.accumulate("ispublic", alist.get(0).get(ClubDetail.ISPUBLIC));
			js.accumulate("provicne", alist.get(0).get(ClubDetail.PROVINCE));
			js.accumulate("city", alist.get(0).get(ClubDetail.CITY));
			js.accumulate("district", alist.get(0).get(ClubDetail.DISTRICT));
			js.accumulate("admin_uid", alist.get(0).get(ClubDetail.ADMIN_UID));
			js.accumulate("admin_username",
					alist.get(0).get(ClubDetail.ADMIN_NAME));
			js.accumulate("groupnum", alist.get(0).get(ClubDetail.GROUPNUM));
			js.accumulate("intro", alist.get(0).get(ClubDetail.INTRO));
			js.accumulate("logo", alist.get(0).get(ClubDetail.LOGO));
			js.accumulate("ismyclub", db.isMyClub(clubId));

		} catch (JSONException e1) {

			e1.printStackTrace();
		}
		if (js.length() < 1)
			return;
		thisDetail = js;
		Message msg = new Message();
		msg.what = 0;
		msg.obj = js.toString();
		handlerDetail.sendMessage(msg);
	}

	/***
	 * 成员页，成员操作，加关注加好友
	 * 
	 * @param uid
	 *            对方uid
	 * @param name
	 *            对方用户名
	 */
	public void showOperate(final String uid, final String name) {
		String[] items = { getString(R.string.addfriend),
				getString(R.string.add_guanzhu), getString(R.string.visite) };
		AlertDialog.Builder ab = new AlertDialog.Builder(
				ClubDetailActivity.this);
		ab.setTitle(name);
		ab.setItems(items, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				switch (which) {
				case 0:
					if (!isMember()) {
						return;
					}
					if (uid.equals("" + muid)) {
						handlerDetail.obtainMessage(16,
								getString(R.string.not_add_own)).sendToTarget();
						return;
					}
					// 操作
					addFriend(uid);
					break;
				case 1:
					if (!isMember()) {
						return;
					}
					if (uid.equals("" + muid)) {
						handlerDetail.obtainMessage(16,
								getString(R.string.not_own)).sendToTarget();
						return;
					}

					// 操作
					addGz(uid);
					break;
				case 2:
					Intent page = new Intent(ClubDetailActivity.this,
							Friendpage.class);
					page.putExtra("uid", uid);
					page.putExtra("name", name);
					startActivity(page);
					break;
				}
				dialog.dismiss();
			}
		});
		ab.setNegativeButton(R.string.can, null);
		ab.create().show();
	}

	public void addFriend(final String uid) {
		new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					HashMap<String, String> map = new HashMap<String, String>();
					map.put("session", session_id);
					map.put("fuid", uid + "");
					map.put("note",
							getResources().getString(
									R.string.defaultclub_addfriend_note)
									+ thisDetail.getString("name"));

					String result = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/user/addFriend", map);
					JSONObject js = new JSONObject(result);
					String msg = "";
					if (js.getString("status").equals("0")
							|| js.getString("status").equals("7")) {
						msg = getString(R.string.add_friend_ok);

					} else if (js.getString("status").equals("3")) {
						msg = getString(R.string.not_add_own);

					} else if (js.getString("status").equals("4")) {
						msg = getString(R.string.err);

					} else if (js.getString("status").equals("5")) {
						msg = getString(R.string.you_have_friend);

					} else if (js.getString("status").equals("6")) {
						msg = getString(R.string.requestsended);

					}
					handlerDetail.obtainMessage(16, msg).sendToTarget();
				} catch (Exception e) {
					if (e.toString().contains("ConnectException")
							|| e.toString().contains("UnknownHostException")) {
						handlerDetail.obtainMessage(13).sendToTarget();
					}
					e.printStackTrace();
				}

			}

		}).start();
	}

	/***
	 * 添加关注
	 * 
	 * @param uid
	 *            对方uid
	 */
	private void addGz(final String uid) {

		new Thread(new Runnable() {

			@Override
			public void run() {

				HashMap<String, String> map = new HashMap<String, String>();
				map.put("uid", muid + "");
				map.put("fid", uid + "");
				try {
					String result = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/Guanzhu/doFollow", map);
					JSONObject js = new JSONObject(result);
					String msg = "";
					if (js.getString("status").equals("0")) {
						// 关注成功
						msg = getString(R.string.guanzhu_ok);

					} else if (js.getString("status").equals("1")) {
						// 关注失败
						msg = getString(R.string.user_no_exist);

					} else if (js.getString("status").equals("2")) {
						// 不能关注自己
						msg = getString(R.string.not_own);
					} else if (js.getString("status").equals("3")) {
						// 关注失败
						msg = getString(R.string.user_no_exist);
					} else if (js.getString("status").equals("4")) {
						// 已关注
						msg = getString(R.string.have_guanzhu);
					}
					handlerDetail.obtainMessage(16, msg).sendToTarget();
				} catch (Exception e) {
					if (e.toString().contains("ConnectException")
							|| e.toString().contains("UnknownHostException")) {
						handlerDetail.obtainMessage(13).sendToTarget();
					}
					e.printStackTrace();
				}
			}
		}).start();
	}

	// 获得俱乐部竞赛
	public void getAllMatch(final int all_index) {
		if (FriendFragment.isNetworkConnected(this)) {
			getAllMatchFromNet(all_index);
		} else {
			loadClubMatch(all_index);
		}
	}

	// 获得俱乐部竞赛
	public void getAllMatchFromNet(final int all_index) {
		dialog = Util.initProgressDialog(this, true,
				getString(R.string.data_wait), null);
		new Thread() {
			@Override
			public void run() {

				try {
					Map<String, String> matchargs = new HashMap<String, String>();
					matchargs.put("session", share.getString("session_id", ""));
					matchargs.put("index", all_index + "");
					matchargs.put("num", 5 + "");
					matchargs.put("clubid", clubId);
					String mymatch = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/data/getClubMatch", matchargs);
					JSONObject json = new JSONObject(mymatch);
					if (json.getInt("status") == 0) {
						JSONArray content = json.getJSONArray("content");
						db.saveClubMatch(content, clubId);
						db.saveAllMatch(content);
						if (json.getJSONArray("content").length() < 1) {
							handlerDetail.obtainMessage(16,
									getString(R.string.join_match_erro5))
									.sendToTarget();
							return;
						}

					}
					// 加载数据库

					loadClubMatch(all_index);

				} catch (Exception e) {

					if (e.toString().contains("ConnectException")
							|| e.toString().contains("UnknownHostException")) {
						handlerDetail.obtainMessage(13).sendToTarget();
					}
					loadClubMatch(all_index);
				}

			}
		}.start();

	}

	// 加载竞赛
	public boolean loadClubMatch(int all_index) {

		Dbutils db = new Dbutils(share.getInt("UID", -1), this);
		JSONArray json = db.getClubMatch(all_index, 5, clubId);
		if (json.length() <= 0) {
			return false;
		}
		JSONObject obj = new JSONObject();
		try {
			obj.put("myctx", json);
		} catch (JSONException e) {

			e.printStackTrace();
		}

		if (all_index > 0) {
			// 追加
			handlerDetail.obtainMessage(14, obj).sendToTarget();
		} else {
			// 填充
			handlerDetail.obtainMessage(15, obj).sendToTarget();
		}
		return true;
	}

	public void shareFeed(final String feedid) {
		this.registerReceiver(re, new IntentFilter(
				"com.milink.android.lovewalk.comment"));
		Intent i = new Intent(this, WindowCommentActivity.class);
		Bundle b = new Bundle();
		b.putString("feed_id", feedid);
		i.putExtras(b);
		startActivityForResult(i, 20);

	}

	// 获得俱乐部排名，先加载数据库
	public void getAllRank() {
		dialog = Util.initProgressDialog(this, true,
				getString(R.string.data_wait), null);
		if (!FriendFragment.isNetworkConnected(this)) {
			JSONArray arr = getRankFromDB(clubId, null, 0);
			if (arr.length() > 0) {
				handlerDetail.obtainMessage(5, arr.toString()).sendToTarget();
				return;
			}

		}
		db.delRank();
		new Thread(new Runnable() {

			@Override
			public void run() {

				Map<String, String> map = new HashMap<String, String>();
				map.put("session", session_id);
				map.put("clubid", clubId);
				String resultJoin;
				JSONArray arr = new JSONArray();
				SQLiteDatabase datab = db.beginTransaction();
				try {
					resultJoin = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/data/getClubRankDay", map);
					JSONArray json = new JSONObject(resultJoin).getJSONObject(
							"content").getJSONArray("data");

					for (int i = 0; i < json.length(); i++) {
						JSONObject jobj = json.getJSONObject(i);
						db.UpdateClubRank(
								jobj.getString("step"),
								jobj.getString("name"),
								jobj.getString("gid"),
								jobj.getString("time"),
								clubId,
								HttpUtlis.AVATAR_URL
										+ HttpUtlis.getAvatarUrl(jobj
												.getString("uid").replace(
														"small", "midle")),
								jobj.getString("uid"));
					}
					datab.setTransactionSuccessful();

				} catch (ConnectException e) {
					handlerDetail.obtainMessage(13).sendToTarget();
					e.printStackTrace();

				} catch (UnknownHostException e) {
					handlerDetail.obtainMessage(13).sendToTarget();
				} catch (Exception e) {
					// TODO: handle exception
				} finally {
					datab.endTransaction();
				}

				arr = getRankFromDB(clubId, null, 0);
				if (arr.toString().length() > 0) {
					handlerDetail.obtainMessage(5, arr.toString())
							.sendToTarget();
				} else {
					handlerDetail.obtainMessage(16, getString(R.string.nomore))
							.sendToTarget();
				}

			}

		}).start();
	}

	// 获得俱乐部排名，先加载数据库
	public void reFreshAllRank() {
		isBack = false;
		// help.loadUrls(new String[] { "emptyRank()" });
		db.delRank();
		dialog = Util.initProgressDialog(this, true,
				getString(R.string.data_wait), null);
		new Thread(new Runnable() {

			@Override
			public void run() {

				Map<String, String> map = new HashMap<String, String>();
				map.put("session", session_id);
				map.put("clubid", clubId);
				String resultJoin;
				JSONArray arr = new JSONArray();
				try {
					resultJoin = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/data/getClubRankDay", map);
					JSONArray json = new JSONObject(resultJoin).getJSONObject(
							"content").getJSONArray("data");
					for (int i = 0; i < json.length(); i++) {
						JSONObject jobj = json.getJSONObject(i);
						db.UpdateClubRank(
								jobj.getString("step"),
								jobj.getString("name"),
								jobj.getString("gid"),
								jobj.getString("time"),
								clubId,
								HttpUtlis.AVATAR_URL
										+ HttpUtlis.getAvatarUrl(jobj
												.getString("uid").replace(
														"small", "midle")),
								jobj.getString("uid"));
					}
					arr = getRankFromDB(clubId, null, 0);
					isBack = true;

				} catch (Exception e) {
					if (e.toString().contains("ConnectException")
							|| e.toString().contains("UnknownHostException")) {
						handlerDetail.obtainMessage(13).sendToTarget();
					}
					e.printStackTrace();
					arr = getRankFromDB(clubId, null, 0);
				}
				if (arr.toString().length() > 0) {
					handlerDetail.obtainMessage(5, arr.toString())
							.sendToTarget();
				} else {
					handlerDetail.obtainMessage(16, getString(R.string.nomore))
							.sendToTarget();
				}

			}

		}).start();
	}

	/***
	 * 获取db存储的俱乐部排名
	 * 
	 * @param id
	 *            查询的clubid
	 * @return
	 */
	public JSONArray getRankFromDB(String id, String gid, int page) {
		ArrayList<UserHashMap<String, String>> alist;
		if (gid != null && !gid.equals("0")) {
			alist = db.getGroupRank(id, gid, page);
		} else {
			alist = db.getClubRank(id, page);
		}

		JSONArray arr = new JSONArray();
		for (int i = 0; i < alist.size(); i++) {
			JSONObject js = new JSONObject();
			try {
				js.accumulate("muid", muid);
				js.accumulate("uid", alist.get(i).get(ClubRank.UID));
				js.accumulate("name", alist.get(i).get(ClubRank.USERNAME));
				js.accumulate("step", alist.get(i).get(ClubRank.STEP));
				js.accumulate("time", alist.get(i).get(ClubRank.TIME));
				js.accumulate("gid", alist.get(i).get(ClubRank.GID));
				js.accumulate("photo", alist.get(i).get(ClubRank.PHOTO));
				js.accumulate("page", page);
				try {
					double t = Double.parseDouble(alist.get(i).get(
							ClubRank.STEP))
							/ db.getClubStep1st(clubId);
					js.accumulate("per", t * 100);
				} catch (Exception e) {
					js.accumulate("per", 0);
				}

				arr.put(i, js);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		if (page > 0) {
			handlerDetail.obtainMessage(18, arr).sendToTarget();
		}
		return arr;
	}

	public void getAllGroup() {

		if (FriendFragment.isNetworkConnected(this)) {
			getGroupFromNet();
		} else {
			getGroupFromDB(clubId);
		}

	}

	public void getGroupFromNet() {

		new Thread(new Runnable() {
			@Override
			public void run() {

				Map<String, String> map = new HashMap<String, String>();
				map.put("session", session_id);
				map.put("clubid", clubId);
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
					getGroupFromDB(clubId);
				} catch (Exception e) {
					if (e.toString().contains("ConnectException")
							|| e.toString().contains("UnknownHostException")) {
						handlerDetail.obtainMessage(13).sendToTarget();
					}
					e.printStackTrace();
					getGroupFromDB(clubId);
				}
			}
		}).start();
	}

	// 获得数据库存储的分组信息
	public boolean getGroupFromDB(String clubId) {
		ArrayList<HashMap<String, String>> alist = db.getGroupInfo(clubId);
		if (alist == null || alist.size() < 1) {
			return false;
		}
		JSONArray arr = new JSONArray();
		for (HashMap<String, String> map : alist) {
			JSONObject js = new JSONObject();
			try {
				js.accumulate("gid", map.get(ClubGroup.GROUPID));
				js.accumulate("clubid", map.get(ClubGroup.CLUBID));
				js.accumulate("groupname", map.get(ClubGroup.NAME));
				arr.put(js);
			} catch (JSONException e) {

				e.printStackTrace();
			}
		}
		Message msg = new Message();
		msg.what = 6;
		msg.obj = arr.toString();
		handlerDetail.sendMessage(msg);
		return true;
	}

	public void getMember(final String deparid, final int page) {

		if (FriendFragment.isNetworkConnected(this)) {
			getMemberFromNet(deparid, page);
		} else {
			getMemberFromDB(clubId, deparid, page);
		}

	}

	public void getMemberFromNet(final String deparid, final int page) {
		dialog = Util.initProgressDialog(this, true,
				getString(R.string.data_wait), null);
		new Thread(new Runnable() {

			@Override
			public void run() {

				Map<String, String> map = new HashMap<String, String>();
				map.put("session", session_id);
				map.put("clubid", clubId);
				String resultJoin;
				SQLiteDatabase database = db.beginTransaction();
				try {
					resultJoin = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/data/getClubMember", map);

					JSONArray json = new JSONObject(resultJoin)
							.getJSONArray("content");

					for (int i = 0; i < json.length(); i++) {
						JSONObject jobj = json.getJSONObject(i);
						jobj.accumulate(
								"photo",
								HttpUtlis.AVATAR_URL
										+ HttpUtlis.getAvatarUrl(jobj
												.getString("uid").replace(
														"small", "midle")));
						jobj.accumulate("username", jobj.getString("nickname"));
						jobj.accumulate("departname", db.getGroupNameById(
								jobj.getString("gid"), clubId));
						db.UpdateMemberInfo(
								clubId,
								jobj.getString("gid"),
								jobj.getString("nickname"),
								jobj.getString("uid"),
								HttpUtlis.AVATAR_URL
										+ HttpUtlis.getAvatarUrl(jobj
												.getString("uid").replace(
														"small", "midle")),
								jobj.getString("isadmin"));
					}
					db.setTransactionSuccessful(database);
					Message msg = new Message();
					msg.what = 7;
					if (page > 0) {
						msg.what = 17;
					}
					msg.obj = json.toString();
					handlerDetail.sendMessage(msg);
					// getMemberFromDB(clubId, deparid, page);
				} catch (Exception e) {

					e.printStackTrace();
					getMemberFromDB(clubId, deparid, page);
				} finally {
					db.endTransaction(database);
				}

			}

		}).start();
	}

	// 负责接收，评论，分享传回的内容
	BroadcastReceiver re = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			final Intent b = intent;
			if (b.getExtras().getString("to_uid") == null) {
				new Thread(new Runnable() {

					@Override
					public void run() {

						try {
							Bundle bun = b.getExtras();
							String content = bun.getString("content");
							content = content.replace(" ", "&nbsp;");
							String feed_id = bun.getString("feed_id");
							HashMap<String, String> map = new HashMap<String, String>();
							map.put("uid", muid + "");
							map.put("feed_id", feed_id);
							map.put("content", content);
							String s = HttpUtlis.getRequest(HttpUtlis.BASE_URL
									+ "/Share/shareToFeed", map);
							JSONObject js = new JSONObject(s);
							if (js.getString("status").equals("0")) {
								Message msg = new Message();
								msg.obj = bun;
								msg.what = 12;
								handlerDetail.sendMessage(msg);
							} else {
								handlerDetail.sendEmptyMessage(13);
							}
						} catch (Exception e) {
							if (e.toString().contains("ConnectException")
									|| e.toString().contains(
											"UnknownHostException")) {
								handlerDetail.obtainMessage(13).sendToTarget();
							}
							e.printStackTrace();
						}
					}
				}).start();
			} else {
				new Thread(new Runnable() {

					@Override
					public void run() {

						try {
							Bundle bun = b.getExtras();
							String feed_id = bun.getString("feed_id");
							String to_uid = bun.getString("to_uid");
							String content = bun.getString("content");
							HashMap<String, String> map = new HashMap<String, String>();
							map.put("uid", muid + "");
							map.put("feed_id", feed_id);
							map.put("to_uid", to_uid);
							map.put("content", content);
							String s = HttpUtlis.getRequest(HttpUtlis.BASE_URL
									+ "/Comment/addComment", map);
							JSONObject js = new JSONObject(s);
							if (js.getString("status").equals("0")) {
								Message msg = new Message();
								msg.obj = bun;
								msg.what = 10;
								handlerDetail.sendMessage(msg);
							} else {
								handlerDetail.sendEmptyMessage(13);
							}

						} catch (Exception e) {
							if (e.toString().contains("ConnectException")
									|| e.toString().contains(
											"UnknownHostException")) {
								handlerDetail.obtainMessage(13).sendToTarget();
							}
							e.printStackTrace(System.err);
						}
					}
				}).start();
			}
		}
	};

	// 点赞
	public void clickGood(final String feed_id) {
		if (share.getInt("ISMEMBER", 0) == 0) {
			d = new NoRegisterDialog(this, R.string.no_register,
					R.string.no_register_content);
			d.show();
		} else {
			int feed = Integer.valueOf(feed_id);
			db = new Dbutils(this);
			int bj = db.getDig(feed_id);
			if (bj == 0) {
				help.loadUrls(new String[] { "zan(" + feed_id + ")" });
				dig(feed);
			} else {
				Toast.makeText(this, getString(R.string.had_zan),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	public void dig(final int feed) {
		new Thread(new Runnable() {
			@Override
			public void run() {

				HashMap<String, String> map = new HashMap<String, String>();
				map.put("feed_id", feed + "");
				map.put("uid", muid + "");
				try {
					String result = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/Like/addLike", map);
					JSONObject json = new JSONObject(result);
					if (json.getInt("status") == 0) {
						db.InsertDig(muid, feed + "", 1);
						handlerDetail.obtainMessage(100, feed).sendToTarget();
						String count = json.getString("content");
						JSONObject con = db.getClubFeedById(feed + "");
						con.remove("digg_count");
						con.put("digg_count", count);
						db.UpdateContent(feed + "", con.toString());
					} else {
						db.InsertDig(muid, feed + "", 1);
						handlerDetail.obtainMessage(16,
								getString(R.string.had_zan)).sendToTarget();
					}

				} catch (Exception e) {
					if (e.toString().contains("ConnectException")
							|| e.toString().contains("UnknownHostException")) {
						handlerDetail.obtainMessage(13).sendToTarget();
					}
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void getJsMatchDetail(String matchid) {
		Intent intent = new Intent(ClubDetailActivity.this,
				MatchDetailActivity.class);
		intent.putExtra("id", Integer.parseInt(matchid));
		intent.putExtra("from", 2);
		startActivity(intent);
	}

	public void joinMatch(final String matchid) {
		new Thread() {
			@Override
			public void run() {

				try {
					Map<String, String> matchargs = new HashMap<String, String>();
					matchargs.put("session", share.getString("session_id", ""));
					matchargs.put("matchid", matchid + "");
					String mymatch = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/data/joinMatch", matchargs);
					JSONObject json = new JSONObject(mymatch);
					int status = json.getInt("status");
					String msg = "";
					if (status == 0) {
						msg = getString(R.string.join_match_success);
					} else if (status == 3) {
						msg = getString(R.string.join_match_erro1);
					} else if (status == 4) {
						msg = getString(R.string.join_match_erro2);
					} else if (status == 5) {
						msg = getString(R.string.join_match_erro3);
					} else if (status == 6) {
						msg = getString(R.string.join_match_erro4);
					} else if (status == 7) {
						msg = getString(R.string.join_match_erro5);
					}
					handlerDetail.obtainMessage(16, msg).sendToTarget();
				} catch (Exception e) {

					if (e.toString().contains("ConnectException")
							|| e.toString().contains("UnknownHostException")) {
						handlerDetail.obtainMessage(13).sendToTarget();
					}
					handlerDetail.obtainMessage(2).sendToTarget();
					e.printStackTrace(System.err);
				}

			}
		}.start();

	}

	public void showWarning() {

		if (d != null && d.isShowing()) {
			d.dismiss();
		}
		d = new NoRegisterDialog(this, R.string.no_register,
				R.string.no_register_content);
		d.show();
	}

	public boolean isMember() {
		if (ismember == 1)
			return true;
		else
			showWarning();
		return false;
	}

	public void jqJsMatch(String ismember, final String mid) {
		if (share.getInt("ISMEMBER", 0) == 0) {
			NoRegisterDialog d = new NoRegisterDialog(this,
					R.string.no_register, R.string.no_register_content);
			d.show();
		} else {
			int ismeb = Integer.parseInt(ismember);

			if (ismeb == 1) {
				handlerDetail.obtainMessage(16,
						getString(R.string.join_match_erro3)).sendToTarget();
			} else {
				joinMatch(mid);
			}
		}

	}

	// 评论
	public void addComment(String feed_id, String to_uid) {
		this.registerReceiver(re, new IntentFilter(
				"com.milink.android.lovewalk.comment"));
		Intent i = new Intent(this, WindowCommentActivity.class);
		Bundle b = new Bundle();
		b.putString("feed_id", feed_id);
		b.putString("to_uid", to_uid);
		i.putExtras(b);
		startActivityForResult(i, 20);
	}

	public boolean getMemberFromDB(String clubId, String deparid, int page) {

		ArrayList<HashMap<String, String>> alist = db.getMemberInfo(clubId,
				deparid, page);
		// 仅当没有选择分组且数据库没有时才从网上去取
		if ((deparid == null || deparid.length() <= 0 || deparid.equals("0"))
				&& alist.size() <= 0) {
			return false;
		}
		JSONArray arr = new JSONArray();
		for (HashMap<String, String> map : alist) {
			JSONObject j = new JSONObject();
			try {
				j.accumulate("username", map.get(ClubMember.USERNAME));
				j.accumulate("uid", map.get(ClubMember.USERID));
				j.accumulate("gid", map.get(ClubMember.GID));
				j.accumulate("photo", map.get(ClubMember.PHOTO));
				j.accumulate("departname", map.get(ClubMember.DEPARTNAME));
			} catch (JSONException e) {

				e.printStackTrace();
			}
			arr.put(j);
		}
		Message msg = new Message();
		msg.what = 7;
		if (page > 0) {
			msg.what = 17;
		}
		msg.obj = arr.toString();
		handlerDetail.sendMessage(msg);
		return true;
	}

	ProgressDialog dialog;

	/***
	 * 获得指定club的指定页的动态
	 * 
	 * @param index
	 *            第index的动态
	 * @param id
	 *            指定的clubid
	 */
	public void getClubFeed(final int index) {

		// size of a at least 17 "{["f……":[]]}"
		if (!FriendFragment.isNetworkConnected(this)) {
			String a = db.getClubFeed(index, clubId);
			handlerDetail.obtainMessage(8, -1, 0, a).sendToTarget();

		} else {
			dialog = Util.initProgressDialog(this, true,
					getString(R.string.data_wait), null);
			getFeedFromNet(index);
		}
	}

	public void getFeedFromNet(final int index) {

		new Thread(new Runnable() {

			@Override
			public void run() {

				HashMap<String, String> feedMap = new HashMap<String, String>();
				feedMap.put("page", index + "");
				feedMap.put("clubid", clubId);
				String resultJoin;
				String array;
				try {
					resultJoin = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/Dongtai/clubDongTai", feedMap);
					JSONArray arr = new JSONObject(resultJoin).getJSONObject(
							"content").getJSONArray("friend_dym");

					for (int i = 0; i < arr.length(); i++) {

						JSONObject jobj = arr.getJSONObject(i);

						db.SaveClubFeed(clubId, jobj.getString("feed_id"),
								jobj.getString("uid"), jobj.toString(),
								jobj.getString("time"));
					}
					array = db.getClubFeed(index, clubId);
				} catch (Exception e) {
					if (e.toString().contains("ConnectException")
							|| e.toString().contains("UnknownHostException")) {
						handlerDetail.obtainMessage(13).sendToTarget();
					}
					array = db.getClubFeed(index, clubId);
				}
				Message msg = new Message();
				msg.obj = array;
				if (index < 2) {
					msg.what = 8;
				} else {
					msg.what = 11;
				}
				handlerDetail.sendMessage(msg);

			}

		}).start();
	}

	/***
	 * 显示大图
	 * 
	 * @param url
	 *            图片url
	 */
	public void getBigImg(Drawable d) {

		View view = LayoutInflater.from(this)
				.inflate(R.layout.pop_bigimg, null);
		final PopupWindow popwindow = new PopupWindow(view,
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true);
		popwindow.setOutsideTouchable(true);
		popwindow.setBackgroundDrawable(new BitmapDrawable());
		GestureImageView iv = (GestureImageView) view.findViewById(R.id.img);
		iv.setImageDrawable(d);
		popwindow.showAtLocation(web.getRootView(), Gravity.CENTER, 0, 0);
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				popwindow.dismiss();
			}
		});
	}

	Handler imageHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			if (msg.what == 0) {
				getBigImg((Drawable) msg.obj);
			}
			super.handleMessage(msg);
		}

	};

	public void showBigImg(final String path) {

		new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					String s = path;
					if (path.contains("small")) {
						s = s.replace("small", "middle");
					}
					URL url = new URL(s);
					Drawable d = BitmapDrawable.createFromStream(
							url.openStream(), "");
					// 下面是为了在logo链接失效时触发空指针异常来加载默认图片
					d.getLevel();
					imageHandler.obtainMessage(0, d).sendToTarget();
				} catch (Exception e) {
					MyLog.e("Exception", e.toString());
					if (e.toString().contains("ConnectException")
							|| e.toString().contains("UnknownHostException")) {
						handlerDetail.obtainMessage(13).sendToTarget();
					} else {
						try {
							Drawable d = BitmapDrawable.createFromStream(
									getAssets().open(
											"sell/images/clubmode/model1.jpg"),
									"");
							imageHandler.obtainMessage(0, d).sendToTarget();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
					e.printStackTrace();
				}

			}

		}).start();
	}

	private void showMsg(String str) {
		Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
	}

	/***
	 * 加入/退出俱乐部
	 * 
	 * @param clubid
	 *            目标俱乐部id
	 * @param isIn
	 *            是否是我所在的俱乐部
	 */
	public void joinClub(final String clubid, final boolean isIn) {
		if (!isMember()) {
			return;
		}
		if (isIn) {

			AlertDialog.Builder ab = new AlertDialog.Builder(
					ClubDetailActivity.this);

			ab.setTitle(R.string.warning);
			int res = 0;
			// 如果接口写好 请去除false
			if (admin_uid == muid && false) {
				ab.setMessage(R.string.admin_exit_ensure);
				res = R.string.appiont;
			} else {
				ab.setMessage(R.string.exit_ensure);
				res = R.string.ok;
			}

			ab.setNegativeButton(R.string.can,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

							dialog.dismiss();
						}
					});
			ab.setPositiveButton(res, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dia, int which) {
					dia.dismiss();
					if (admin_uid == muid && false) {
						Intent i = new Intent(ClubDetailActivity.this,
								ClubMemberManager.class);
						i.putExtra("clubid", clubId);
						startActivity(i);
						return;
					}

					new Thread(new Runnable() {

						@Override
						public void run() {

							Map<String, String> map = new HashMap<String, String>();
							map.put("session", session_id);
							map.put("clubid", clubid);
							try {

								String resultQuit = HttpUtlis.getRequest(
										HttpUtlis.BASE_URL + "/data/quitClub",
										map);
								String re = new JSONObject(resultQuit)
										.getString("status");
								if (re.equals("0") || re.equals("3")) {
									// success
									handlerDetail.sendEmptyMessage(1);
								} else {
									// failed
									handlerDetail.sendEmptyMessage(2);
								}
							} catch (Exception e) {

							}
						}
					}).start();
				}
			});
			ab.create().show();
		} else {

			new Thread(new Runnable() {
				@Override
				public void run() {

					try {
						Map<String, String> map = new HashMap<String, String>();
						map.put("session", session_id);
						map.put("clubid", clubid);

						String resultJoin = HttpUtlis.getRequest(
								HttpUtlis.BASE_URL + "/data/joinClub", map);
						String re = new JSONObject(resultJoin)
								.getString("status");
						if (re.equals("0")) {
							// success
							handlerDetail.sendEmptyMessage(4);
						} else if (re.equals("3")) {
							// wait check
							handlerDetail.obtainMessage(16,
									getString(R.string.requestsended))
									.sendToTarget();
						} else if (re.equals("4")) {
							// failed
							handlerDetail.obtainMessage(16,
									getString(R.string.alreadyin))
									.sendToTarget();
						}

					} catch (Exception e) {

						if (e.toString().contains("ConnectException")
								|| e.toString()
										.contains("UnknownHostException")) {
							handlerDetail.obtainMessage(13).sendToTarget();
						}
						e.printStackTrace();
					}
				}
			}).start();
		}

	}

	private Handler handlerDetail = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (mSwipeLayout != null && mSwipeLayout.isRefreshing()) {
				mSwipeLayout.setRefreshing(false);
			}

			switch (msg.what) {
			case 0:// 加载俱乐部详情信息

				help.loadUrls(new String[] { "showClubBase("
						+ msg.obj.toString() + ")" });
				break;
			case 1:// 退出俱乐部成功

				showMsg(getString(R.string.quit_ok));
				db.quitClub(clubId);
				if (admin_uid == muid) {
					db.delClubFromDb(clubId);
				}
				ClubDetailActivity.this.finish();
				break;
			case 4:// 加入俱乐部成功

				showMsg(getString(R.string.join_ok));
				try {
					db.UpdateMyClub(clubId, thisDetail.getString("name"),
							thisDetail.getString("membernum"),
							thisDetail.getString("ispublic"),
							thisDetail.getString("provicne"),
							thisDetail.getString("city"),
							thisDetail.getString("district"),
							thisDetail.getString("logo"));
				} catch (JSONException e) {

					e.printStackTrace();
				}
				Refresh();
				break;
			case 5:
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
				// 俱乐部排名
				help.loadUrls(new String[] { "showAllRank({\"friend\":"
						+ msg.obj.toString() + "})" });
				break;
			case 6:

				// 俱乐部部门
				help.loadUrls(new String[] { "showGroup({\"fenzulist\":"
						+ msg.obj.toString() + "})" });
				break;
			case 7:
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
				// 俱乐部成员
				help.loadUrls(new String[] { "showMembers({\"mateList\":"
						+ msg.obj.toString() + "})" });
				break;
			case 8:
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
				// 俱乐部动态
				help.loadUrls(new String[] { "clubFeed(" + msg.obj.toString()
						+ ")" });
				break;

			case 10:

				// 俱乐部评论1
				Bundle bun = (Bundle) msg.obj;
				String c = bun.getString("content");
				String f = bun.getString("feed_id") + "";
				help.loadUrls(new String[] { "comAppend('" + mname + "','" + c
						+ "','" + f + "')" });
				unregisterReceiver(re);
				break;
			case 11:
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
				// 更多俱乐部动态
				help.loadUrls(new String[] { "appendFeed(" + msg.obj.toString()
						+ ")" });
				break;
			case 12:
				// 分享后
				getClubFeed(1);
				unregisterReceiver(re);
				break;
			case 13:
				// 网络异常

				showMsg(getString(R.string.error_network));
				break;
			case 14:
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
				help.loadUrls(new String[] { "appandMatch("
						+ msg.obj.toString() + ")" });

				break;
			case 15:
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
				help.loadUrls(new String[] { "showMatch(" + msg.obj.toString()
						+ ")" });
				System.out.println(msg.obj.toString());
				break;
			case 16:
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
				// Toast显示结果
				showMsg(msg.obj.toString());
				break;
			case 17:
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
				if (msg.obj != null && msg.obj.toString().length() > 0) {
					// 俱乐部成员
					help.loadUrls(new String[] { "appendMembers({\"mateList\":"
							+ msg.obj.toString() + "})" });
				} else {
					showMsg(getString(R.string.nomore));
					// page 置为0
					help.loadUrls(new String[] { "setPage()" });
				}

				break;
			case 18:
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
				// 俱乐部排名
				if (msg.obj != null && msg.obj.toString().length() > 0) {
					help.loadUrls(new String[] { "appendAllRank({\"friend\":"
							+ msg.obj.toString() + "})" });
				} else {
					showMsg(getString(R.string.nomore));
					// page 置为0
					help.loadUrls(new String[] { "setPage()" });
				}

				break;

			case 100:
				// help.loadUrls(new String[] { "zan(" + msg.obj.toString() +
				// ")" });
				break;

			}
			super.handleMessage(msg);
		}

	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == 20) {
			if (data != null) {
				Bundle b = data.getExtras();
				Intent i = new Intent("com.milink.android.lovewalk.comment");
				i.putExtras(b);
				sendBroadcast(i);
			}
		} else if (resultCode == 0) {// 发布动态 签名
			getFeedFromNet(1);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	String[] items;

	// actionbar右侧按钮
	private void showOperate() {

		if (admin_uid != share.getInt("UID", -1) || admin_uid < 1) {
			items = new String[] { getString(R.string.create_club),
					getString(R.string.codeofclub) };

		} else {
			items = new String[] { getString(R.string.create_club),
					getString(R.string.updateclub),
					getString(R.string.manageclube),
					getString(R.string.codeofclub) /*
													 * ,getString(R.string.
													 * club_sport)
													 */};
		}

		AlertDialog.Builder ab = new AlertDialog.Builder(
				ClubDetailActivity.this);
		ab.setTitle(R.string.set_select);
		ab.setItems(items, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if (items.length == 2 && which == 1) {
					which = 3;
				}
				switch (which) {
				case 0:
					if (!isMember()) {
						return;
					}
					startActivity(new Intent(ClubDetailActivity.this,
							CreateClubActivity.class));
					MApplication.getInstance().addActivity(
							ClubDetailActivity.this);
					break;
				case 1:

					Intent i = new Intent(ClubDetailActivity.this,
							CreateClubActivity.class);
					i.putExtra("base", baseData.toString());
					i.putExtra("clubid", clubId);
					startActivity(i);

					break;
				case 2:

					Intent i2 = new Intent(ClubDetailActivity.this,
							ClubMemberManager.class);
					i2.putExtra("clubid", clubId);
					startActivity(i2);

					break;
				case 3:
					try {
						JSONObject jscode = new JSONObject();
						jscode.accumulate("clubid", clubId);
						jscode.accumulate("clubname", ComposePacket.string2Unicode(clubName));
						Bitmap bitmap = ScanFriendFragment.CreateTwoDCode(
								jscode.toString(), ClubDetailActivity.this);
						showCodeImage(bitmap);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case 4:
					Intent i3 = new Intent(ClubDetailActivity.this,
							ClubSportAnalysis.class);
					i3.putExtra("id", clubId);
					startActivity(i3);
					break;
				}
			}
		});
		ab.setNegativeButton(R.string.can, null);
		ab.create().show();
	}

	

	void showCodeImage(Bitmap bitmap) {
		AlertDialog.Builder ab = new AlertDialog.Builder(
				ClubDetailActivity.this);
		ImageView imv = new ImageView(ClubDetailActivity.this);
		imv.setImageBitmap(bitmap);
		ab.setView(imv);
		ab.setTitle(R.string.scan_code_join_club);
		ab.setNegativeButton(R.string.close, null);
		ab.create().show();
	}

	@Override
	protected void onResume() {
		getBaseFromNet();
		if (ismember == 0) {
			ismember = share.getInt("ISMEMBER", 0);
			if (ismember == 1) {
				muid = share.getInt("UID", -1);
				session_id = share.getString("session_id", "-1");
				mname = share.getString("NICKNAME", "lovefit");
				db = new Dbutils(muid, this);
				if (currentTab == 0) {
					getClubFeed(1);
				} else if (currentTab == 1) {
					getAllMatch(0);
				} else if (currentTab == 2) {
					getAllGroup();
					getMember("", 0);
				} else if (currentTab == 3) {
					reFreshAllRank();
				}
			}

		} else {
			ismember = share.getInt("ISMEMBER", 0);
			muid = share.getInt("UID", -1);
			session_id = share.getString("session_id", "-1");
			mname = share.getString("NICKNAME", "lovefit");
			db = new Dbutils(muid, this);
			if (currentTab == 2) {
				getAllGroup();
				getMember("", 0);
			} else if (currentTab == 3) {
				getAllRank();
			}
		}

		super.onResume();
	}

	@Override
	public void onDestroy() {

		if (po != null) {
			po.dismiss();
		}
		super.onDestroy();
	}

	@Override
	protected void onStop() {

		if (imageHandler != null) {
			imageHandler.removeCallbacksAndMessages(null);
		}
		if (handlerDetail != null) {
			handlerDetail.removeCallbacksAndMessages(null);
		}
		super.onStop();
	}

	private OnClickListener lsr = new OnClickListener() {

		@Override
		public void onClick(View v) {

			ClubDetailActivity.this.finish();
		}
	};
	boolean isBack = true;

	private void Refresh() {
		ismember = share.getInt("ISMEMBER", 0);
		muid = share.getInt("UID", -1);
		session_id = share.getString("session_id", "-1");
		mname = share.getString("NICKNAME", "lovefit");
		db = new Dbutils(muid, this);
		help.loadUrls(new String[] { "setSelect()" });
		getBaseFromNet();

		if (currentTab == 0) {
			getFeedFromNet(1);
		} else if (currentTab == 1) {
			getAllMatchFromNet(0);
		} else if (currentTab == 2) {
			getGroupFromNet();
			getMemberFromNet("", 0);
		} else if (currentTab == 3 && isBack) {
			reFreshAllRank();
			help.loadUrls(new String[] { "setPage()" });
		}
		// isMember();
	}

	public void showCreate() {
		help.loadUrls(new String[] { "showCreate()" });
	}

	public void hideCreate() {
		help.loadUrls(new String[] { "hideCreate()" });
	}

	public void createMatch() {
		if (isMember()) {
			startActivity(new Intent(this, CreateMatchActivity.class));
		}

	}

	public void showGroupRank(String gid, int page) {
		JSONArray jarr = getRankFromDB(clubId, gid, page);
		handlerDetail.obtainMessage(5, jarr.toString()).sendToTarget();
	}

	@Override
	public void onRefresh() {
		HttpUtlis.clearCacheFolder(this.getCacheDir(),
				System.currentTimeMillis());
		help.clearCache();
		Refresh();
	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub

	}
}

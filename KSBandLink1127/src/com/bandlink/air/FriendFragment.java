package com.bandlink.air;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.http.util.EncodingUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.bandlink.air.Js.FriendInterface;
import com.bandlink.air.ble.LogUtil;
import com.bandlink.air.friend.BaseActivity;
import com.bandlink.air.friend.FansActivity;
import com.bandlink.air.friend.GuanzhuActivity;
import com.bandlink.air.friend.friendInviteActivity;
import com.bandlink.air.gps.OffLineManager;
import com.bandlink.air.jpush.Jpush;
import com.bandlink.air.satellite.SatelliteMenu;
import com.bandlink.air.satellite.SatelliteMenu.SateliteClickedListener;
import com.bandlink.air.satellite.SatelliteMenuItem;
import com.bandlink.air.simple.AllRankActivity;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Util;
import com.bandlink.air.util.WebViewHelper;
import com.bandlink.air.view.NoRegisterDialog;
import com.bandlink.air.view.SwipeLayout;
import com.polites.android.GestureImageView;
import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;

public class FriendFragment extends LovefitActivity implements OnRefreshListener {
 
	WebView web;
	WebViewHelper help;
	PopupWindow p;
	EditText et;
	String toresult;
	SharedPreferences share;
	String result, session_id, user;
	private Dbutils db;
	String name, time, updatetime, content;
	int gid, uid, step, per, num, svalue, suid, to_uid;
	int maxStep = 0;
	Context context;
	boolean tag = false, isfresh = true, remore = false;
	String str = "[";
	String mystr, url, addCommentInfo, refeed_id;
	String pname, pmessage, pphoto, JSON;
	int pdongtaiNum, pfriendnum, pfansNum, pguanzhuNum, paddfriendnum, t_feed;
	int q_feed_id, q_digg_count, q_comment_all_count, feed_id, resvalue;
	private BroadcastReceiver broad;
	private NoRegisterDialog d;
	ProgressDialog dialog;
	int j = 1, currentTab = 0;

	private SwipeLayout mSwipeLayout;
	private FriendInterface js;
	public static final String ACTION_SENDRESULT = "com.air.send.result";
	public static final String ACTION_SENDMSG = "com.air.send.message";
	float ds;
	Context mContext;
	// 首先在您的Activity中添加如下成员变量
	final UMSocialService mController = UMServiceFactory
			.getUMSocialService("com.umeng.share");

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		FriendFragment.this.unregisterReceiver(sendResult);
		super.onDestroy();
	}

	BroadcastReceiver sendResult = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if (action.equals(ACTION_SENDRESULT)) {
				js.dataToLoadList();
			} else if (action.equals(ACTION_SENDMSG)) {
				js.PersonalInfo();
			}
		}
	}; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setContentView(R.layout.friend_frame);
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_SENDMSG);
		filter.addAction(ACTION_SENDRESULT);
		FriendFragment.this.registerReceiver(sendResult, filter); 

		mContext = Util.getThemeContext(FriendFragment.this);
		ds = FriendFragment.this.getResources().getDisplayMetrics().density;
		share = FriendFragment.this
				.getSharedPreferences(
						SharePreUtils.APP_ACTION,
						Context.MODE_PRIVATE);
		suid = share.getInt("UID", suid);
		if (share.getInt("ISMEMBER", 0) == 0) {
			d = new NoRegisterDialog(FriendFragment.this, R.string.no_register,
					R.string.no_register_content);
			d.show();
		}
		time = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		getInitRank();
		// ActionbarSettings actionbar = new ActionbarSettings(contentView, lsr,
		// minterface);
		// actionbar.setTopRightIcon(R.drawable.more);
		ActionbarSettings actionbar = new ActionbarSettings(this, new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				FriendFragment.this.finish();
			}
		}, lsr);
		actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
		actionbar.setTitle(R.string.user_area);
		actionbar.setRightVisible(false);
		web = (WebView)findViewById(R.id.web);
		web.requestFocus();
		web.getSettings().setDefaultTextEncodingName("utf-8");
		web.addJavascriptInterface(new FriendInterface(this), "jsonObj");
		js = new FriendInterface(this);
		if (Locale.getDefault().getLanguage().equals("zh")) {
			help = new WebViewHelper(FriendFragment.this, web,
					"file:///android_asset/sell/2.html");
		}else if(Locale.getDefault().getLanguage().equals("zh-TW")){
			help = new WebViewHelper(FriendFragment.this, web,
					"file:///android_asset/sell/2-tw.html");
		} else {
			help = new WebViewHelper(FriendFragment.this, web,
					"file:///android_asset/sell/2-en.html");

		}
		help.initWebview();
		help.setBgTransparent();

		final SatelliteMenu menu = (SatelliteMenu) findViewById(R.id.menu);
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
		final LinearLayout frame = (LinearLayout) findViewById(R.id.frame);
		frame.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (frame.getVisibility() == View.VISIBLE) {
					menu.onClick();
				}
			}
		});
		menu.setOnItemClickedListener(new SateliteClickedListener() {

			public void eventOccured(int id) {
				menu.close();
				switch (id - 1) {
				case 2:
					frame.setVisibility(View.GONE);
					Intent ts = new Intent();
					ts.putExtra("tid", 2);
					showDialog = true;
					ts.setClass(FriendFragment.this, ShareEditActivity.class);
					// startActivityForResult(ts, 2);
					startActivity(ts);
					break;
				case 1:
					frame.setVisibility(View.GONE);
					Intent sa = new Intent();
					sa.putExtra("tid", 1);
					showDialog = true;
					sa.setClass(FriendFragment.this, ShareEditActivity.class);
					// startActivityForResult(sa, 1);
					startActivity(sa);
					break;
				case 0:
					frame.setVisibility(View.GONE);
					Intent s = new Intent();
					s.putExtra("tid", 0);
					showDialog = true;
					s.setClass(FriendFragment.this, ShareEditActivity.class);
					// startActivityForResult(s, 0);
					startActivity(s);
					break;
				}
			}

			@Override
			public void mainImgClick() {
				// TODO Auto-generated method stub
				if (menu.getIsExpand()) {
					frame.setVisibility(View.VISIBLE);
					frame.setBackgroundDrawable(getResources().getDrawable(
							R.color.tran_black));
				} else {
					frame.setVisibility(View.GONE);
				}

			}
		});
		dataToLoadList();
		//js.PersonalInfo();
		mSwipeLayout = (SwipeLayout) findViewById(R.id.id_swipe_ly);
		mSwipeLayout.setViewGroup(web);
		mSwipeLayout.setOnRefreshListener(this);
		mSwipeLayout.setColorScheme(R.color.holo_blue_bright,
				R.color.holo_green_light, R.color.holo_orange_light,
				R.color.holo_red_light);
		super.onCreate(savedInstanceState);
	}

	boolean showDialog = false;
	private OnClickListener lsr = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (share.getInt("ISMEMBER", 0) == 0) {
				d = new NoRegisterDialog(FriendFragment.this, R.string.no_register,
						R.string.no_register_content);
				d.show();
			} else {
				startActivity(new Intent().setClass(FriendFragment.this,
						NewFriendActivity.class));
			}
		}
	};

	private void getInitRank() {
		// TODO Auto-generated method stub
		user = share.getString("USERNAME", "lovefit");
		db = new Dbutils(suid, FriendFragment.this);
	}

	public static boolean isNetworkConnected(Context c) {
		try {
			ConnectivityManager cm = (ConnectivityManager) c.getSystemService(
					Context.CONNECTIVITY_SERVICE);
			NetworkInfo ni = cm.getActiveNetworkInfo();
			return ni != null && ni.isConnectedOrConnecting();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
	}

	public void writeFileData(String fileName, String message) {
		try {
			File file = new File(FriendFragment.this.getFilesDir() + "/json.txt");
			if (!isNetworkConnected(FriendFragment.this)) {
			} else {
				if (file.exists()) {
					file.delete();
				}
			}

			FileOutputStream fout = FriendFragment.this.openFileOutput(fileName,
					FriendFragment.this.MODE_PRIVATE);
			byte[] bytes = message.getBytes();
			fout.write(bytes);
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String readFileData(String fileName) {
		String res = "";
		try {
			FileInputStream fin = FriendFragment.this.openFileInput(fileName);
			int length = fin.available();
			byte[] buffer = new byte[length];
			fin.read(buffer);
			res = EncodingUtils.getString(buffer, "UTF-8");
			fin.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		// if (resultCode == 0) {
		// js.PersonalInfo();
		// js.dataToLoadList();
		// } else
		if (resultCode == 1) {

		} else if (resultCode == 8) {
			addCommentInfo = data.getStringExtra("commentinfo");
			IntentFilter myIntentFilter = new IntentFilter();
			myIntentFilter.addAction("com.broadCast.Flag");
			FriendFragment.this.registerReceiver(broad, myIntentFilter);
			Intent mIntent = new Intent("com.broadCast.Flag");
			mIntent.putExtra("addCommentInfo", addCommentInfo);
			FriendFragment.this.sendBroadcast(mIntent);
		} else if (resultCode == 9) {
			addCommentInfo = data.getStringExtra("commentinfo");
			IntentFilter myIntentFilter = new IntentFilter();
			myIntentFilter.addAction("com.broadCast");
			FriendFragment.this.registerReceiver(broad, myIntentFilter);
			Intent mIntent = new Intent("com.broadCast");
			mIntent.putExtra("addCommentInfo", addCommentInfo);
			// mIntent.putExtra("num", value)
			FriendFragment.this.sendBroadcast(mIntent);

		}
	}

	public void showOperate(final String uid, final String name) {
		String[] Items = { getString(R.string.a_friend),
				getString(R.string.add_guanzhu), getString(R.string.visite) };
		AlertDialog.Builder ab1 = new AlertDialog.Builder(mContext);
		ab1.setTitle(name);
		ab1.setItems(Items, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface d, int which) {
				// TODO Auto-generated method stub
				if (which == 0) {
					dialog = Util.initProgressDialog(FriendFragment.this, true,
							getString(R.string.data_wait), null);
					new Thread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							Message msg = handler.obtainMessage();
							Map<String, String> map = new LinkedHashMap<String, String>();
							map.put("session",
									share.getString("session_id", ""));
							map.put("fuid", uid);
							try {
								result = HttpUtlis.getRequest(
										HttpUtlis.FRIEND_ADDFRIEND, map);
								if (result != null) {
									JSONObject json;
									json = new JSONObject(result);
									int s = Integer.valueOf(json
											.getString("status"));
									if (s == 0) {
										msg.what = 24;
										handler.sendMessage(msg);
									} else if (s == 1) {
										msg.what = 13;
										handler.sendMessage(msg);
									} else if (s == 2) {
										msg.what = 14;
										handler.sendMessage(msg);
									} else if (s == 3) {
										msg.what = 15;
										handler.sendMessage(msg);
									} else if (s == 4) {
										msg.what = 16;
										handler.sendMessage(msg);
									} else if (s == 5) {
										msg.what = 17;
										handler.sendMessage(msg);
									} else if (s == 6) {
										msg.what = 18;
										handler.sendMessage(msg);
									} else if (s == 7) {
										msg.what = 18;
										msg.arg1 = R.string.wait_agree;
										msg.obj = json.getString("content");
										handler.sendMessage(msg);
										String jpushid = json
												.getString("content");
										if (jpushid != null
												&& !jpushid.equals("")) {
											Jpush jpush = new Jpush(
													FriendFragment.this);
											jpush.sendFriendMessage(
													getString(R.string.friend_add),
													jpushid);
										}
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

					}).start();
				} else if (which == 1) {
					dialog = Util.initProgressDialog(FriendFragment.this, true,
							getString(R.string.data_wait), null);
					new Thread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub

							Message msg = handler.obtainMessage();
							Map<String, String> map = new LinkedHashMap<String, String>();
							map.put("uid", share.getInt("UID", -1) + "");
							map.put("fid", uid);

							try {
								result = HttpUtlis.getRequest(
										HttpUtlis.FRIEND_GUANZHU, map);
								if (result != null) {
									JSONObject json;
									json = new JSONObject(result);
									int s = Integer.valueOf(json
											.getString("status"));
									if (s == 0) {
										msg.what = 19;
										handler.sendMessage(msg);
									} else if (s == 1) {
										msg.what = 20;
										handler.sendMessage(msg);
									} else if (s == 2) {
										msg.what = 21;
										handler.sendMessage(msg);
									} else if (s == 3) {
										msg.what = 22;
										handler.sendMessage(msg);
									} else if (s == 4) {
										msg.what = 23;
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
					}).start();
				} else if (which == 2) {
					Intent page = new Intent();
					page.putExtra("uid", uid);
					page.putExtra("name", name);
					page.setClass(FriendFragment.this, Friendpage.class);
					startActivity(page);
				}

			}
		});
		ab1.setNegativeButton(getString(R.string.close),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				});
		ab1.create().show();

	}

	public String getAvatarUrl(String uid) {
		String size = "small";
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
 

	public void diggClick(String feed_id,String uid) {
		if (share.getInt("ISMEMBER", 0) == 0) {
			d = new NoRegisterDialog(FriendFragment.this, R.string.no_register,
					R.string.no_register_content);
			d.show();
		} else {
			final int feed = Integer.valueOf(feed_id);
			refeed_id = feed_id;
			db = new Dbutils(FriendFragment.this);
			int bj = db.getDig(feed_id);
			if (bj == 0) {
				help.loadUrls(new String[] { "zan(" + refeed_id + ")" });
				dig(feed,uid);
			} else {
				Toast.makeText(FriendFragment.this, getString(R.string.had_zan),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	// 分享
	public void showShare(String feedid, String content, String[] urlImage) {
		if (share.getInt("ISMEMBER", 0) == 0) {
			d = new NoRegisterDialog(FriendFragment.this, R.string.no_register,
					R.string.no_register_content);
			d.show();
		} else {
			refeed_id = feedid;
			Intent i = new Intent();
			i.putExtra("num", 1);
			i.setClass(FriendFragment.this, CommenetInfo.class);
			startActivityForResult(i, 9);
			broad = new BroadcastReceiver() {
				public void onReceive(Context context, Intent intent) {
					Message msg = handler.obtainMessage();
					String action = intent.getAction();
					if (action.equals("com.broadCast")) {
						msg.what = 100;
						handler.sendMessage(msg);
					}
					context.unregisterReceiver(broad);
				}
			};
		}
	}

	public void dig(final int feed,final String uid) {
		db.InsertDig(suid, feed+"", 1);
		new Thread(new Runnable() {
			public void run() {
				// TODO Auto-generated method stub
				Message msg = handler.obtainMessage();
				Map<String, String> map = new HashMap<String, String>();
				map.put("feed_id", feed + "");
				map.put("uid", suid + "");
				try {
					result = HttpUtlis.getRequest(HttpUtlis.FRIEND_ZAN, map);
					if (result != null) {
						JSONObject json;
						json = new JSONObject(result);
						if (json.getInt("status") == 0) {
							// db.InsertDig(suid, feed, 1);
							msg.what = 10;
							handler.sendMessage(msg);
							getUserJid(uid+"", "["+share.getString("NICKNAME", "")+"]赞了你",FriendFragment.this);
						} else {
							// db.InsertDig(suid, feed, 1);
							msg.what = 11;
							handler.sendMessage(msg);
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}
	public static  void getUserJid(  String uid,  String msg,  Context context){
	 
				try {
					String result = HttpUtlis.queryStringForGet(HttpUtlis.BASE_URL+"/Dongtai/getJpushId/uid/"+uid);
					JSONObject json = new JSONObject(result);
					String jid = json.getString("content");
					sendJpush(jid, msg,context);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
	}
	public static void sendJpush(String jpushid,String msg,Context context){
		if (jpushid != null
				&& !jpushid.equals("")) {
			 
			Jpush.sendJpushMessage(
					msg,
					jpushid,null,context);
		}
	}
	public void FriendInfo() {
		Intent intent = new Intent();
		intent.setClass(FriendFragment.this, BaseActivity.class);
		share.edit().putString("resh", "resh").commit();
		FriendFragment.this.startActivity(intent);
	}

	public void MyGuanzhu() {
		Intent t = new Intent();
		t.setClass(FriendFragment.this, GuanzhuActivity.class);
		share.edit().putString("resh", "resh").commit();
		FriendFragment.this.startActivity(t);
	}

	public void Fans() {
		Intent intent = new Intent();
		intent.setClass(FriendFragment.this, FansActivity.class);
		share.edit().putString("resh", "resh").commit();
		FriendFragment.this.startActivity(intent);
	}

	public void friendInvite() {
		Intent intent = new Intent();
		intent.setClass(FriendFragment.this, friendInviteActivity.class);
		FriendFragment.this.startActivity(intent);
	}

	public void MyMainPage() {
		Intent page = new Intent();
		page.putExtra("uid", share.getInt("UID", -1) + "");
		page.putExtra("name", share.getString("NICKNAME", ""));
		System.out.println(share.getString("NICKNAME", ""));
		page.setClass(FriendFragment.this, Friendpage.class);
		startActivity(page);
	}

	// 获得评论详细信息
	public void AddCommentInfo(String feed_id, String c_uid) {
		if (share.getInt("ISMEMBER", 0) == 0) {
			d = new NoRegisterDialog(FriendFragment.this, R.string.no_register,
					R.string.no_register_content);
			d.show();
		} else {
			Intent i = new Intent();
			i.putExtra("num", 0);
			i.setClass(FriendFragment.this, CommenetInfo.class);
			startActivityForResult(i, 8);
			final int feed = Integer.valueOf(feed_id);
			final String _uid = c_uid;
			broad = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					String action = intent.getAction();
					if (action.equals("com.broadCast.Flag")) {
						if (addCommentInfo != null) {
							try {
								new Thread(new Runnable() {
									public void run() {
										// TODO Auto-generated method stub
										Message msg = handler.obtainMessage();
										Map<String, String> map = new HashMap<String, String>();
										map.put("feed_id", feed + "");
										map.put("uid", suid + "");
										map.put("to_uid", _uid);
										map.put("comment_id", "0");
										map.put("content", addCommentInfo);
										Object[] profile = db.getUserProfile();
										String name = share.getString(
												"NICKNAME", "");
										try {
											name = profile[2].toString();
											if (name.equals("")) {
												name = share.getString(
														"NICKNAME", "");
											}
										} catch (Exception e) {

										}
										mystr = "{'name':'" + name
												+ "','content':'"
												+ addCommentInfo + "'}";
										t_feed = feed;
										msg.what = 3;
										handler.sendMessage(msg);
										try {
											result = HttpUtlis
													.getRequest(
															HttpUtlis.FRIEND_ADDCOMMENT,
															map);
											if (result != null) {
												db.DeleteComment(feed);
												JSONObject json, obj;
												JSONArray arr;
												json = new JSONObject(result);
												obj = json
														.getJSONObject("content");
												arr = obj
														.getJSONArray("result");
												for (int i = 0; i < arr
														.length(); i++) {
													JSONObject temp = (JSONObject) arr
															.get(i);
													name = temp
															.getString("name");
													to_uid = Integer.valueOf(temp
															.getString("to_uid"));
													content = temp
															.getString("content");
													db.SaveComment(
															suid,
															name,
															content,
															to_uid,
															feed,
															temp.getString("ctime"));
												}
												getUserJid(to_uid+"", "["+share.getString(
														"NICKNAME", "")+"]"+"："+addCommentInfo,FriendFragment.this);
											}
										} catch (Exception e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								}).start();
							} catch (Exception e) {
								ArrayList<HashMap<String, String>> co = db
										.getComment(feed);
								for (Map<String, String> map : co) {
									str += "{'name':'" + map.get("name")
											+ "','content':'"
											+ map.get("content") + "'},";
								}
								help.loadUrls(new String[] { "comAppend("
										+ feed + ","
										+ str.substring(0, str.length() - 1)
										+ "]" + ")" });
							}
						}
					}
					context.unregisterReceiver(broad);
				}

			};
		}
	}

	// 调用js方法来加载好友动态
	public void dataToLoadList() {
		currentTab = 0;
		if (!isNetworkConnected(FriendFragment.this)) {
			help.loadUrls(new String[] { "reLoadList("
					+ readFileData("json.txt") + ")" });
		} else {
			help.loadUrls(new String[] { "reLoadList("
					+ readFileData("json.txt") + ")" });
			if(dialog!=null && dialog.isShowing()){
				dialog.dismiss();
			} 
			dialog = Util.initProgressDialog(FriendFragment.this, true,
					getString(R.string.data_wait), null);
			LogUtil.e("TEST", ""+Util.getTimeMMStringFormat("HH:mm:ss:SSS")+"-刷新开始等待框");
			new Thread(new TrendsListThread()).start();
		}
	}

	public void clickMoreTrend() {

		if (!isNetworkConnected(FriendFragment.this)) {
			Toast.makeText(FriendFragment.this, getString(R.string.network_erro),
					Toast.LENGTH_LONG).show();
		} else {
			dialog = Util.initProgressDialog(FriendFragment.this, true,
					getString(R.string.data_wait), null);
			new Thread(new TrendsListThread()).start();
			j++;
		}
	}

	class TrendsListThread implements Runnable {

		@Override
		public void run() {
			// http://192.168.9.33/home/Dongtai/getData/uid/6/page/1
			Map<String, String> map = new LinkedHashMap<String, String>();
			map.put("uid", suid + "");
			map.put("page", "" + j);
			Message msg = handler.obtainMessage();
			try {
				LogUtil.e("TEST", ""+Util.getTimeMMStringFormat("HH:mm:ss:SSS")+"-开始请求动态");
				result = HttpUtlis.getRequest(HttpUtlis.FRIEND_GETDATA, map);
				LogUtil.e("TEST", ""+Util.getTimeMMStringFormat("HH:mm:ss:SSS")+"-动态请求返回");
				if (mSwipeLayout != null) {
					mSwipeLayout.setRefreshing(false);
				}
				// result = result.substring(39, result.length() - 1);
				JSONArray arr = null;
				try {
					arr = new JSONObject(result).getJSONObject("content")
							.getJSONArray("friend_dym");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (arr != null && arr.length() > 0) {
					for (int i = 0; i < arr.length(); i++) {
						JSONObject obj = arr.getJSONObject(i);
						obj.accumulate("wifi",
								OffLineManager.isWifi(mContext) ? "1" : "1");
						if(obj.optString("name")!=null){
							obj.put("name", AllRankActivity.transformNick(obj.optString("name")));
						}
					}
					writeFileData("json.txt",
							"{\"friend_dym\":" + arr.toString() + "}");
					msg.what = 6;
					handler.sendMessage(msg);

					// if (j == 1 && result.length() < 20) {
					// if (dialog != null && dialog.isShowing()) {
					// dialog.dismiss();
					// }
					// if (mSwipeLayout != null) {
					// mSwipeLayout.setRefreshing(false);
					// }
					// } else if (result.length() < 20 && j != 1) {
					//
					// msg.what = 00;
					// handler.sendMessage(msg);
					// } else {
					// writeFileData("json.txt", result);
					// msg.what = 6;
					// handler.sendMessage(msg);
					// }
				} else {
					msg.what = 4;
					handler.sendMessage(msg);
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
			}

		}
	}

	public void getBigImg(Drawable d) {

		View view = LayoutInflater.from(FriendFragment.this).inflate(
				R.layout.pop_bigimg, null);
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
			if (dialog != null && dialog.isShowing()) {
				dialog.dismiss();
			}
			if (msg.what == 0) {
				getBigImg((Drawable) msg.obj);
			}
			super.handleMessage(msg);
		}

	};

	public void showBigImg(final String path) {
		dialog = Util.initProgressDialog(FriendFragment.this, true,
				getString(R.string.data_wait), null);
		new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					URL url = new URL(path);
					Drawable d = BitmapDrawable.createFromStream(
							url.openStream(), "");
					imageHandler.obtainMessage(0, d).sendToTarget();
				} catch (Exception e) {
					handler.obtainMessage(25).sendToTarget();
					if (dialog != null && dialog.isShowing()) {
						dialog.dismiss();
					}
				}

			}

		}).start();
	}

	// 加载个人信息
	public void PersonalInfo() {
		Object[] info = db.getPersonalInfo(suid);
		if (!isNetworkConnected(FriendFragment.this) && info != null && info.length >= 10) {
			pname = (String) info[2];
			pmessage = (String) info[3];
			if (pmessage == null && pmessage.length() < 1) {
				pmessage = getResources().getString(R.string.no_setting_mes);
			}
			pphoto = (String) info[4];
			pdongtaiNum = (Integer) info[5];
			pfriendnum = (Integer) info[6];
			pfansNum = (Integer) info[7];
			pguanzhuNum = (Integer) info[8];
			paddfriendnum = (Integer) info[9];
			JSON = "{ 'name': '" + pname + "','message': '" + pmessage
					+ "','avatar_middle': '" + pphoto + "','dongtaiNum': '"
					+ pdongtaiNum + "','friendnum': '" + pfriendnum
					+ "','fansNum': '" + pfansNum + "','guanzhuNum': '"
					+ pguanzhuNum + "','addfriendnum': '" + paddfriendnum
					+ "'}";
			help.loadUrls(new String[] { "showPersonalInfo(" + JSON + ")" });
		} else {
			new Thread(new getPersonalInfoThread()).start();
		}

	}

	class getPersonalInfoThread implements Runnable {

		@Override
		public void run() {
			Map<String, String> map = new LinkedHashMap<String, String>();
			map.put("uid", String.valueOf(suid));
			Message msg = handler.obtainMessage();
			try {
				result = HttpUtlis.getRequest(HttpUtlis.FRIEND_MYINFO, map);
				if (result != null) {
					JSONObject json, obj;
					json = new JSONObject(result);
					obj = json.getJSONObject("content");
					if (share.getInt("ISMEMBER", 0) == 0) {
						pname = getString(R.string.tourist);
					} else {
						pname = obj.getString("name");
					}
					pmessage = obj.getString("message");
					if (share.getInt("ISMEMBER", 0) == 0) {
						pmessage = getString(R.string.no_setting_mes);
					}
					pphoto = obj.getString("avatar_middle");
					pdongtaiNum = Integer.parseInt(obj.getString("dongtaiNum"));
					pfriendnum = Integer.parseInt(obj.getString("friendnum"));
					pfansNum = Integer.parseInt(obj.getString("fansNum"));
					pguanzhuNum = Integer.parseInt(obj.getString("guanzhuNum"));
					paddfriendnum = Integer.parseInt(obj
							.getString("addfriendnum"));

					JSON = "{'distance': '" + String.format("%.1f", (float)(Integer.valueOf(obj.getString("distance"))/1000f)) +" ','name': '" + pname + "','message': '" + pmessage
							+ "','avatar_middle': '" + pphoto
							+ "','dongtaiNum': '" + pdongtaiNum
							+ "','friendnum': '" + pfriendnum
							+ "','fansNum': '" + pfansNum + "','guanzhuNum': '"
							+ pguanzhuNum + "','addfriendnum': '"
							+ paddfriendnum + "'}";
					db.InitFriendInfo(suid, pname, pmessage, pphoto,
							pdongtaiNum, pfriendnum, pfansNum, pguanzhuNum,
							paddfriendnum);
					msg.what = 5;
					handler.sendMessage(msg);

				} else {
					msg.what = 4;
					handler.sendMessage(msg);
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public void refresh(String v) {
		int sd = Integer.valueOf(v);
		if (sd == 1) {
			db.DeleTeRank(1);
			resvalue = 1;
			isfresh = false;
			remore = true;
			// dialog = Util.initProgressDialog(FriendFragment.this, true,
			// getString(R.string.data_wait), null);
			// new Thread(new getdayRankThread()).start();
		} else if (sd == 2) {
			db.DeleTeRank(2);
			resvalue = 2;
			isfresh = false;
			remore = true;
			// dialog = Util.initProgressDialog(FriendFragment.this, true,
			// getString(R.string.data_wait), null);
			// new Thread(new getweekRankThread()).start();
		} else if (sd == 3) {
			db.DeleTeRank(3);
			resvalue = 3;
			isfresh = false;
			remore = true;
			// dialog = Util.initProgressDialog(FriendFragment.this, true,
			// getString(R.string.data_wait), null);
			// new Thread(new getmonthRankThread()).start();
		}

	}

	public void getMyRank(final String value) {
		ArrayList<HashMap<String, String>> dayrank = db.getRankData(Integer
				.valueOf(value));
		if (dayrank != null && dayrank.size() > 0) {
			maxStep = Integer.valueOf(dayrank.get(0).get("step"));
			updatetime = dayrank.get(0).get("updatetime");
			int i = 1;
			for (Map<String, String> map : dayrank) {
				if (maxStep == 0) {
					per = 0;
				} else {
					per = (int) (Double.parseDouble(map.get("step"))
							/ ((double) maxStep) * 100);
				}
				url = "http://www.lovefit.com/ucenter/data/avatar/"
						+ getAvatarUrl(map.get("uid"));
				if (Integer.valueOf(map.get("uid")) == suid) {
					num = i;
					url = "http://www.lovefit.com/ucenter/data/avatar/"
							+ getAvatarUrl(map.get("uid"));
					mystr = "{'photo':'" + url + "','per':'" + per
							+ "','uid':' " + map.get("uid") + "','now_pos':'"
							+ num + "','st':' " + map.get("step") + "'},";
					help.loadUrls(new String[] { "reMyRank("
							+ mystr.substring(0, mystr.length() - 1) + ","
							+ value + ")" });
				}
				i++;
			}
		}
	}

	public void getRankType(String value) {
		svalue = Integer.valueOf(value);
		if (svalue == 1) {
			resvalue = svalue;
			String str = "";
			ArrayList<HashMap<String, String>> dayrank = db.getRankData(1, 0);
			if (dayrank != null && dayrank.size() > 0) {
				maxStep = Integer.valueOf(dayrank.get(0).get("step"));
				for (Map<String, String> map : dayrank) {
					if (maxStep == 0) {
						per = 0;
					} else {
						per = (int) (Double.parseDouble(map.get("step"))
								/ ((double) maxStep) * 100);
					}
					url = "http://www.lovefit.com/ucenter/data/avatar/"
							+ getAvatarUrl(map.get("uid"));
					str += "{'photo':'" + url + "','showname':'"
							+ map.get("user") + "','per':'" + per + "','st': '"
							+ map.get("step") + "','uid': '" + map.get("uid")
							+ "','page':'" + 0 + "'},";
				}
				help.loadUrls(new String[] { "reLoadRank(" + "{'friend':["
						+ str.substring(0, str.length() - 1) + "]}," + value
						+ ")" });
			}

		} else if (svalue == 2) {
			resvalue = svalue;
			String str = "";
			ArrayList<HashMap<String, String>> dayrank = db.getRankData(2, 0);
			if (dayrank != null && dayrank.size() > 0) {
				maxStep = Integer.valueOf(dayrank.get(0).get("step"));
				for (Map<String, String> map : dayrank) {
					url = "http://www.lovefit.com/ucenter/data/avatar/"
							+ getAvatarUrl(map.get("uid"));
					if (maxStep == 0) {
						per = 0;
					} else {
						per = (int) (Double.parseDouble(map.get("step"))
								/ ((double) maxStep) * 100);
					}
					str += "{'photo':'" + url + "','showname':'"
							+ map.get("user") + "','per':'" + per + "','st': '"
							+ map.get("step") + "','uid': '" + map.get("uid")
							+ "','page':'" + 0 + "'},";
				}
				help.loadUrls(new String[] { "reLoadRank(" + "{'friend':["
						+ str.substring(0, str.length() - 1) + "]}," + value
						+ ")" });
			}
		} else if (svalue == 3) {
			resvalue = svalue;
			String str = "";
			ArrayList<HashMap<String, String>> dayrank = db.getRankData(3, 0);
			if (dayrank != null && dayrank.size() > 0) {
				maxStep = Integer.valueOf(dayrank.get(0).get("step"));
				for (Map<String, String> map : dayrank) {
					url = "http://www.lovefit.com/ucenter/data/avatar/"
							+ getAvatarUrl(map.get("uid"));
					if (maxStep == 0) {
						per = 0;
					} else {
						per = (int) (Double.parseDouble(map.get("step"))
								/ ((double) maxStep) * 100);
					}
					str += "{'photo':'" + url + "','showname':'"
							+ map.get("user") + "','per':'" + per + "','st': '"
							+ map.get("step") + "','uid': '" + map.get("uid")
							+ "','page':'" + 0 + "'},";
				}
				help.loadUrls(new String[] { "reLoadRank(" + "{'friend':["
						+ str.substring(0, str.length() - 1) + "]}," + value
						+ ")" });
			}

		}
		if (mSwipeLayout != null) {
			mSwipeLayout.setRefreshing(false);
		}

		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
		}

	}

	public void getRank(String value, final String rankPage) {
		currentTab = 0;
		if (share.getInt("ISMEMBER", 0) != 0) {
			svalue = Integer.valueOf(value);
			if (!isNetworkConnected(FriendFragment.this)) {
				getRankType(value);
			} else {
				if (svalue == 1) {
					db.DeleTeRank(1);
					resvalue = 1;
					isfresh = false;
					// dialog = Util.initProgressDialog(FriendFragment.this, true,
					// getString(R.string.data_wait), null);
					// new Thread(new getdayRankThread()).start();
				} else if (svalue == 2) {

					resvalue = 2;
					getRankType(2 + "");
					isfresh = true;
				} else if (svalue == 3) {
					resvalue = 3;
					getRankType(3 + "");
					isfresh = true;
				}
			}
		}
	}

	class getdayRankThread implements Runnable {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			String w2 = share.getString("session_id", session_id);
			Map<String, String> map = new LinkedHashMap<String, String>();
			map.put("session", w2);
			Message msg = handler.obtainMessage();
			try {
				result = HttpUtlis.getRequest(
						HttpUtlis.FRIEND_GETFRIENDRANKDAY, map);
				if (result != null) {
					JSONObject json;
					JSONArray arr;
					json = new JSONObject(result);
					arr = json.getJSONArray("content");
					for (int i = 0; i < arr.length(); i++) {
						JSONObject temp = (JSONObject) arr.get(i);
						uid = Integer.parseInt(temp.getString("uid"));
						gid = Integer.parseInt(temp.getString("gid"));
						step = Integer.parseInt(temp.getString("step"));
						name = temp.getString("name");
						updatetime = temp.getString("time");
						url = "http://www.lovefit.com/ucenter/data/avatar/"
								+ getAvatarUrl(String.valueOf(uid));
						db.SaveRankData(1, uid, name, step, gid, updatetime,
								url);
					}
					if (svalue == 1) {
						msg.what = 1;
						handler.sendMessage(msg);
					} else if (resvalue == 1) {
						msg.what = 1;
						handler.sendMessage(msg);
					} else {
						msg.what = 4;
						handler.sendMessage(msg);
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
			}

		}
	}

	class getweekRankThread implements Runnable {

		@Override
		public void run() {
			svalue = 2;
			// TODO Auto-generated method stub
			String w2 = share.getString("session_id", session_id);
			Map<String, String> map = new LinkedHashMap<String, String>();
			map.put("session", w2);
			Message msg = handler.obtainMessage();
			try {
				result = HttpUtlis.getRequest(
						HttpUtlis.FRIEND_GETFRIENDRANKWEEK, map);
				if (result != null) {
					JSONObject json;
					JSONArray arr;
					json = new JSONObject(result);
					arr = json.getJSONArray("content");
					for (int i = 0; i < arr.length(); i++) {
						JSONObject temp = (JSONObject) arr.get(i);
						uid = Integer.parseInt(temp.getString("uid"));
						gid = Integer.parseInt(temp.getString("gid"));
						step = Integer.parseInt(temp.getString("step"));
						name = temp.getString("name");
						updatetime = temp.getString("time");
						url = "http://www.lovefit.com/ucenter/data/avatar/"
								+ getAvatarUrl(String.valueOf(uid));
						db.SaveRankData(svalue, uid, name, step, gid,
								updatetime, url);
					}
					if (svalue == 2) {
						isfresh = true;
						if (resvalue == 2) {
							getRankType(String.valueOf(resvalue));
						} else {
							msg.what = 2;
							handler.sendMessage(msg);
						}
					} else {
						msg.what = 4;
						handler.sendMessage(msg);
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public void setCurrentTab(int index) {
		currentTab = index;
	}

	class getmonthRankThread implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			svalue = 3;
			String w2 = share.getString("session_id", session_id);
			Map<String, String> map = new LinkedHashMap<String, String>();
			map.put("session", w2);
			Message msg = handler.obtainMessage();
			try {
				result = HttpUtlis.getRequest(
						HttpUtlis.FRIEND_GETFRIENDRANKMONTH, map);
				if (result != null) {
					JSONObject json;
					JSONArray arr;
					json = new JSONObject(result);
					arr = json.getJSONArray("content");
					for (int i = 0; i < arr.length(); i++) {
						JSONObject temp = (JSONObject) arr.get(i);
						uid = Integer.parseInt(temp.getString("uid"));
						gid = Integer.parseInt(temp.getString("gid"));
						step = Integer.parseInt(temp.getString("step"));
						name = temp.getString("name");
						updatetime = temp.getString("time");
						url = "http://www.lovefit.com/ucenter/data/avatar/"
								+ getAvatarUrl(String.valueOf(uid));
						db.SaveRankData(svalue, uid, name, step, gid,
								updatetime, url);
					}

					if (svalue == 3) {
						isfresh = true;
						if (resvalue == 3) {
							getRankType(String.valueOf(resvalue));
						}
					} else {
						msg.what = 4;
						handler.sendMessage(msg);
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public void clickMoreRank(String vi, String rankPage) {
		if (remore) {
			rankPage = 1 + "";
			remore = false;
		}
		String str = "";
		ArrayList<HashMap<String, String>> dayrank = db.getRankData(
				Integer.valueOf(vi), Integer.valueOf(rankPage));
		if (dayrank != null && dayrank.size() > 0) {
			maxStep = Integer.valueOf(dayrank.get(0).get("step"));
			for (Map<String, String> map : dayrank) {
				if (maxStep == 0) {
					per = 0;
				} else {
					per = (int) (Double.parseDouble(map.get("step"))
							/ ((double) maxStep) * 100);
				}
				url = "http://www.lovefit.com/ucenter/data/avatar/"
						+ getAvatarUrl(map.get("uid"));
				str += "{'photo':'" + url + "','showname':'" + map.get("user")
						+ "','per':'" + per + "','st': '" + map.get("step")
						+ "','uid': '" + map.get("uid") + "','page':'"
						+ rankPage + "'},";
			}
			help.loadUrls(new String[] { "RankMore(" + "{'friend':["
					+ str.substring(0, str.length() - 1) + "]})" });
		}

	}

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				getMyRank(1 + "");
				getRankType(1 + "");
				isfresh = true;
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
				db.DeleTeRank(2);
				// new Thread(new getweekRankThread()).start();

				break;
			case 00:
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
				if (mSwipeLayout != null) {
					mSwipeLayout.setRefreshing(false);
				}
				Toast.makeText(FriendFragment.this, R.string.nomore,
						Toast.LENGTH_SHORT).show();
				break;
			case 2:
				db.DeleTeRank(3);
				// new Thread(new getmonthRankThread()).start();
				break;
			case 3:
				help.loadUrls(new String[] { "comAppend(" + t_feed + ","
						+ mystr + ")" });
				break;
			case 4:
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
				if (mSwipeLayout != null) {
					mSwipeLayout.setRefreshing(false);
				}
				break;
			case 5:
				help.loadUrls(new String[] { "showPersonalInfo(" + JSON + ")" });
				break;
			case 6:
				LogUtil.e("TEST", ""+Util.getTimeMMStringFormat("HH:mm:ss:SSS")+"-开始加载动态");
				if (j == 1) {
					help.loadUrls(new String[] { "reLoadList("
							+ readFileData("json.txt") + ")" });
				} else if (j > 1) {
					help.loadUrls(new String[] { "treadMore("
							+ readFileData("json.txt") + ")" });
				} 
				if (dialog != null && dialog.isShowing()) {
					LogUtil.e("TEST", ""+Util.getTimeMMStringFormat("HH:mm:ss:SSS")+"-取消等待框");
					dialog.dismiss();
				} 
				if (mSwipeLayout != null) {
					mSwipeLayout.setRefreshing(false);
				}

				break;
			case 7:
				if(dialog!=null && dialog.isShowing()){
					dialog.dismiss();
				} 
				dialog = Util.initProgressDialog(FriendFragment.this, true,
						getString(R.string.data_wait), null);
				LogUtil.e("TEST", ""+Util.getTimeMMStringFormat("HH:mm:ss:SSS")+"-显示等待框");
				new Thread(new TrendsListThread()).start();
				break;
			case 10:
				// Toast.makeText(FriendFragment.this, getString(R.string.zan_ok),
				// Toast.LENGTH_SHORT).show();
				// help.loadUrls(new String[] { "zan(" + refeed_id + ")" });
				break;
			case 11:
				Toast.makeText(FriendFragment.this, getString(R.string.had_zan),
						Toast.LENGTH_SHORT).show();
				break;
			case 12:
				Toast.makeText(FriendFragment.this, R.string.ignore_ok,
						Toast.LENGTH_SHORT).show();
				break;
			case 13:
				Toast.makeText(FriendFragment.this, R.string.no_param,
						Toast.LENGTH_SHORT).show();
				break;
			case 14:
				Toast.makeText(FriendFragment.this, R.string.not_user,
						Toast.LENGTH_SHORT).show();
				break;
			case 15:
				Toast.makeText(FriendFragment.this, R.string.not_add_own,
						Toast.LENGTH_SHORT).show();
				break;
			case 16:
				Toast.makeText(FriendFragment.this, R.string.ID_not_exist,
						Toast.LENGTH_SHORT).show();
				break;
			case 17:
				Toast.makeText(FriendFragment.this, R.string.you_have_friend,
						Toast.LENGTH_SHORT).show();
				break;
			case 18:
				Toast.makeText(FriendFragment.this, R.string.wait_agree,
						Toast.LENGTH_SHORT).show();
				break;
			case 19:
				Toast.makeText(FriendFragment.this, R.string.guanzhu_ok,
						Toast.LENGTH_SHORT).show();
				break;
			case 20:
				Toast.makeText(FriendFragment.this, R.string.no_param,
						Toast.LENGTH_SHORT).show();
				break;
			case 21:
				Toast.makeText(FriendFragment.this, R.string.not_own,
						Toast.LENGTH_SHORT).show();
				break;
			case 22:
				Toast.makeText(FriendFragment.this, R.string.user_no_exist,
						Toast.LENGTH_SHORT).show();
				break;
			case 23:
				Toast.makeText(FriendFragment.this, R.string.have_guanzhu,
						Toast.LENGTH_SHORT).show();
				break;
			case 24:
				Toast.makeText(FriendFragment.this, R.string.add_friend_ok,
						Toast.LENGTH_SHORT).show();
				break;
			case 25:
				Toast.makeText(FriendFragment.this, R.string.connectexception,
						Toast.LENGTH_SHORT).show();
				break;
			case 99:
				// js.PersonalInfo();
				// js.dataToLoadList();
				break;
			case 100:
				new Thread(new Runnable() {
					@Override
					public void run() {
						Message msg = handler.obtainMessage();
						Map<String, String> map = new LinkedHashMap<String, String>();
						map.put("feed_id", refeed_id);
						map.put("content",
								addCommentInfo.replace(" ", "&nbsp;"));
						map.put("uid", suid + "");
						try {
							result = HttpUtlis.getRequest(
									HttpUtlis.FRIEND_SHARE, map);
							msg.what = 99;
							handler.sendMessage(msg);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}).start();

				break;
			}
		}
	};

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		suid = share.getInt("UID", suid);
		if (!isNetworkConnected(FriendFragment.this)) {
			if (mSwipeLayout != null) {
				mSwipeLayout.setRefreshing(false);
			}
			Toast.makeText(FriendFragment.this, getString(R.string.error_network),
					Toast.LENGTH_LONG).show();
		} else if (share.getInt("ISMEMBER", 0) != 0) {
			web.clearCache(true);

		//	js.PersonalInfo();
			js.dataToLoadList();

		} else {
			if (mSwipeLayout != null) {
				mSwipeLayout.setRefreshing(false);
			}

		}
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(showDialog){ 
			dialog = Util.initProgressDialog(FriendFragment.this, true, getString(R.string.wait), null);
			showDialog = false;
		}
		MobclickAgent.onPageStart("FriendFragment"); // 统计页面
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPageEnd("FriendFragment"); // 统计页面
	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub
		
	}

}

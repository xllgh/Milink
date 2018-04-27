package com.bandlink.air;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.bandlink.air.Js.Friend;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Util;
import com.bandlink.air.util.WebViewHelper;
import com.polites.android.GestureImageView;

public class Friendpage extends LovefitActivity {

	WebView web;
	WebViewHelper help;
	EditText et;
	PopupWindow p;
	SharedPreferences share;
	String result, session_id, user;
	private Dbutils db;
	String name, time, updatetime, content;
	int gid, uid, step, per, num, svalue, suid, to_uid;
	int maxStep = 0;
	Context context;
	boolean tag = false;
	String str = "[";
	String mystr, url, addCommentInfo, refeed_id;
	String pname, pmessage, pphoto, JSON;
	int pdongtaiNum, pfriendnum, pfansNum, pguanzhuNum, paddfriendnum, t_feed;
	int q_feed_id, q_digg_count, q_comment_all_count, feed_id;
	private BroadcastReceiver broad;
	ProgressDialog dialog;
	int j = 1;
	float ds;
	private String puid;
	ConnectivityManager cm;
	Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friendpage);

		mContext = Util.getThemeContext(this);
		Intent i = getIntent();
		puid = i.getExtras().getString("uid");
		MyLog.e("uid", puid);
		ActionbarSettings actionbar = new ActionbarSettings(this, back, null);
		actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
		actionbar.setTitle(i.getExtras().getString("name")
				+ getString(R.string.mainpage));
		web = (WebView) findViewById(R.id.web);
		web.requestFocus();
		web.getSettings().setDefaultTextEncodingName("utf-8");
		web.addJavascriptInterface(new Friend(this), "page");
		if (Locale.getDefault().getLanguage().equals("zh")) {

			help = new WebViewHelper(this, web,
					"file:///android_asset/sell/friendpage.html");
		}else if(Locale.getDefault().getLanguage().equals(" zh_TW")){
			help = new WebViewHelper(this, web,
					"file:///android_asset/sell/friendpage-tw.html");
			
		}else {
			help = new WebViewHelper(this, web,
					"file:///android_asset/sell/friendpage-en.html");
		}
		help.initWebview();
		help.setBgTransparent();

		share = getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		suid = share.getInt("UID", suid);
		if (!isNetworkConnected()) {
			Toast.makeText(Friendpage.this, getString(R.string.network_erro),
					Toast.LENGTH_LONG).show();
		}

		context = Util.getThemeContext(this);

	}

	private OnClickListener back = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Friendpage.this.finish();
		}
	};

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
					dialog = Util.initProgressDialog(Friendpage.this, true,
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
					dialog = Util.initProgressDialog(Friendpage.this, true,
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
					page.setClass(Friendpage.this, Friendpage.class);
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

	public void writeFileData(String fileName, String message) {
		try {
			File file = new File(getFilesDir() + "/json1.txt");
			if (file.exists()) {
				file.delete();
			}
			FileOutputStream fout = openFileOutput(fileName, MODE_PRIVATE);
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
			FileInputStream fin = openFileInput(fileName);
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
		if (resultCode == 8) {
			addCommentInfo = data.getStringExtra("commentinfo");
			IntentFilter myIntentFilter = new IntentFilter();
			myIntentFilter.addAction("com.broadCast.Flag");
			registerReceiver(broad, myIntentFilter);
			Intent mIntent = new Intent("com.broadCast.Flag");
			mIntent.putExtra("addCommentInfo", addCommentInfo);
			sendBroadcast(mIntent);
		} else if (resultCode == 9) {
			addCommentInfo = data.getStringExtra("commentinfo");
			IntentFilter myIntentFilter = new IntentFilter();
			myIntentFilter.addAction("com.broadCast");
			registerReceiver(broad, myIntentFilter);
			Intent mIntent = new Intent("com.broadCast");
			mIntent.putExtra("addCommentInfo", addCommentInfo);
			sendBroadcast(mIntent);

		}
	}

	public String getAvatarUrl(String uid) {
		String size = "big";
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

	public void diggClick(String feed_id) {

		final int feed = Integer.valueOf(feed_id);
		refeed_id = feed_id;
		db = new Dbutils(this);
		int bj = db.getDig(feed_id);
		if (bj == 0) {
			help.loadUrls(new String[] { "zan(" + refeed_id + ")" });
			dig(feed);
		} else {
			Toast.makeText(this, getString(R.string.had_zan),
					Toast.LENGTH_SHORT).show();
		}

	}

	public void dig(final int feed) {
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
							db.InsertDig(suid, feed+"", 1);
							msg.what = 10;
							handler.sendMessage(msg);
						} else {
							db.InsertDig(suid, feed+"", 1);
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

	// 分享
	public void showShare(String feedid, String content, String[] urlImage) {
		refeed_id = feedid;
		Intent i = new Intent();
		i.putExtra("num", 1);
		i.setClass(this, CommenetInfo.class);
		startActivityForResult(i, 9);
		broad = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				Message msg = handler.obtainMessage();
				String action = intent.getAction();
				if (action.equals("com.broadCast")) {
					System.out.println("广播收到的值" + addCommentInfo);
					msg.what = 100;
					handler.sendMessage(msg);
				}
				context.unregisterReceiver(broad);
			}
		};

	}

	// 获得评论详细信息
	public void AddCommentInfo(String feed_id, String c_uid) {

		Intent i = new Intent();
		i.putExtra("num", 0);
		i.setClass(this, CommenetInfo.class);
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
									mystr = "{'name':'"
											+ share.getString("NICKNAME", "")
											+ "','content':'" + addCommentInfo
											+ "'}";
									t_feed = feed;
									msg.what = 3;
									handler.sendMessage(msg);
									try {
										result = HttpUtlis.getRequest(
												HttpUtlis.FRIEND_ADDCOMMENT,
												map);
										if (result != null) {
											db.DeleteComment(feed);
											JSONObject json, obj;
											JSONArray arr;
											json = new JSONObject(result);
											obj = json.getJSONObject("content");
											arr = obj.getJSONArray("result");
											for (int i = 0; i < arr.length(); i++) {
												JSONObject temp = (JSONObject) arr
														.get(i);
												name = temp.getString("name");
												to_uid = Integer.valueOf(temp
														.getString("to_uid"));
												content = temp
														.getString("content");
												db.SaveComment(suid, name,
														content, to_uid, feed,
														temp.getString("ctime"));
											}

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
										+ "','content':'" + map.get("content")
										+ "'},";
							}
							help.loadUrls(new String[] { "comAppend(" + feed
									+ "," + str.substring(0, str.length() - 1)
									+ "]" + ")" });
						}
					}
				}
				context.unregisterReceiver(broad);
			}

		};

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

	// 调用js方法来加载好友动态
	public void dataToLoadList() {
		if (!isNetworkConnected()) {
			help.loadUrls(new String[] { "reLoadList("
					+ readFileData("json1.txt") + ")" });
		} else {
			dialog = Util.initProgressDialog(Friendpage.this, true,
					getString(R.string.data_wait), null);
			new Thread(new TrendsListThread()).start();
		}
	}

	public void deleteFeed(String uid, String feed_id) {
		final String feed = feed_id;
		final String deluid = uid;
		MyLog.e("uid", uid + "");
		AlertDialog.Builder aDailog = new AlertDialog.Builder(context);
		aDailog.setCancelable(false);
		aDailog.setTitle(R.string.del_yes_or_no);
		aDailog.setMessage(R.string.delete_feed);
		aDailog.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// http://192.168.9.33/home/Dongtai/doEditFeed/type/delFeed/feedId/23/uid/6
						// http://192.168.9.33/home/Dongtai/doEditFeed/type/delFeed/feedId/23
						new Thread(new Runnable() {

							@Override
							public void run() {
								Map<String, String> map = new LinkedHashMap<String, String>();
								map.put("feedId", feed);
								map.put("uid", deluid);
								try {
									result = HttpUtlis.getRequest(
											HttpUtlis.DELETEFEED, map);
									System.out.println(result);
									if (result != null) {
										JSONObject json = new JSONObject(result);
										int sa = json.getInt("status");
										if (sa == 0) {
											handler.obtainMessage(2, feed)
													.sendToTarget();
										} else {
											Toast.makeText(Friendpage.this,
													R.string.feed_not_eaist,
													Toast.LENGTH_SHORT).show();
										}
									}
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

						}).start();

					}
				});

		aDailog.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.cancel();
					}
				});
		aDailog.show();
	}

	public void clickMoreTrend() {

		try {
			dialog = Util.initProgressDialog(Friendpage.this, true,
					getString(R.string.data_wait), null);
			new Thread(new TrendsListThread()).start();
			j++;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(this, getString(R.string.network_erro),
					Toast.LENGTH_LONG).show();
		}
	}

	class TrendsListThread implements Runnable {

		@Override
		public void run() {
			// http://192.168.9.33/home/Dongtai/dongTaiUid/uid/6/page/1/myuid/7
			Map<String, String> map = new LinkedHashMap<String, String>();
			map.put("uid", puid);
			map.put("myuid", "" + suid);
			map.put("page", "" + j);
			Message msg = handler.obtainMessage();
			try {
				result = HttpUtlis.getRequest(HttpUtlis.GETUSERMAINPAGE, map);
				result = result.substring(39, result.length() - 1);
				if (result != null) {
					if (j == 1 && result.length() < 20) {
						if (dialog != null && dialog.isShowing()) {
							dialog.dismiss();
						}
					} else if (result.length() < 20 && j != 1) {

						msg.what = 00;
						handler.sendMessage(msg);
					} else {
						writeFileData("json1.txt", result);
						msg.what = 6;
						handler.sendMessage(msg);
					}
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
			if (dialog != null && dialog.isShowing()) {
				dialog.dismiss();
			}
			if (msg.what == 0) {
				getBigImg((Drawable) msg.obj);
			}
			super.handleMessage(msg);
		}

	};

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		if (imageHandler != null) {
			imageHandler.removeCallbacksAndMessages(null);
		}
		if (handler != null) {
			handler.removeCallbacksAndMessages(null);
		}
		super.onStop();
	}

	public void showBigImg(final String path) {
		dialog = Util.initProgressDialog(this, true,
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
					if (dialog != null && dialog.isShowing()) {
						dialog.dismiss();
					}
					Toast.makeText(Friendpage.this,
							getString(R.string.error_network),
							Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}

			}

		}).start();
	}

	// 加载个人信息
	public void PersonalInfo() {
		new Thread(new getPersonalInfoThread()).start();
	}

	class getPersonalInfoThread implements Runnable {

		@Override
		public void run() {
			Map<String, String> map = new LinkedHashMap<String, String>();
			map.put("uid", puid);
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
					pphoto = obj.getString("avatar_middle");
					pdongtaiNum = Integer.parseInt(obj.getString("dongtaiNum"));
					pfriendnum = Integer.parseInt(obj.getString("friendnum"));
					pfansNum = Integer.parseInt(obj.getString("fansNum"));
					pguanzhuNum = Integer.parseInt(obj.getString("guanzhuNum"));
					paddfriendnum = Integer.parseInt(obj
							.getString("addfriendnum"));
					JSON = "{ 'name': '" + pname + "','message': '" + pmessage
							+ "','avatar_middle': '" + pphoto
							+ "','dongtaiNum': '" + pdongtaiNum
							+ "','friendnum': '" + pfriendnum
							+ "','fansNum': '" + pfansNum + "','guanzhuNum': '"
							+ pguanzhuNum + "','addfriendnum': '"
							+ paddfriendnum + "'}";
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

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 00:
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
				Toast.makeText(Friendpage.this, R.string.nomore, 1).show();
				break;
			case 2:
				help.loadUrls(new String[] { "hideDelFeed("
						+ msg.obj.toString() + ")" });
				break;
			case 3:
				help.loadUrls(new String[] { "comAppend(" + t_feed + ","
						+ mystr + ")" });
				break;
			case 4:
				Toast.makeText(Friendpage.this, "error...", 1).show();
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
				break;
			case 5:
				help.loadUrls(new String[] { "showPersonalInfo(" + JSON + ")" });
				break;
			case 6:
				if (j == 1) {
					help.loadUrls(new String[] { "reLoadList("
							+ readFileData("json1.txt") + ")" });
				} else if (j > 1) {
					help.loadUrls(new String[] { "treadMore("
							+ readFileData("json1.txt") + ")" });
				}

				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}

				break;
			case 7:
				dialog = Util.initProgressDialog(Friendpage.this, true,
						getString(R.string.data_wait), null);
				new Thread(new TrendsListThread()).start();
				break;
			case 10:
				Toast.makeText(Friendpage.this, getString(R.string.zan_ok),
						Toast.LENGTH_SHORT).show();
				help.loadUrls(new String[] { "zan(" + refeed_id + ")" });
				break;
			case 11:
				Toast.makeText(Friendpage.this, getString(R.string.had_zan),
						Toast.LENGTH_SHORT).show();
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
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub

	}
}

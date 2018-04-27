package com.bandlink.air;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.bandlink.air.Js.MatchDetailJsInterface;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.AsynImagesLoader;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Util;
import com.bandlink.air.util.WebViewHelper;
import com.bandlink.air.view.NoRegisterDialog;
import com.bandlink.air.view.SwipeLayout;
import com.polites.android.GestureImageView;

public class MatchDetailActivity extends LovefitActivity implements
		SwipeRefreshLayout.OnRefreshListener {
	private int matchid, matchfrom;
	WebView web;
	WebViewHelper help;
	SharedPreferences share;
	private ProgressDialog progressDialog;
	public static int tab = 1;
	private SwipeLayout mSwipeLayout;
	AsynImagesLoader imageLoader;
	Context mContext;
	private Dbutils db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.frg_frame);

		mContext = Util.getThemeContext(this);
		share = getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		db = new Dbutils(share.getInt("UID", -1), this);
		matchid = getIntent().getIntExtra("id", 0);
		matchfrom = getIntent().getIntExtra("from", 3);
		imageLoader = AsynImagesLoader.getInstance(this, true);
		ActionbarSettings actionbar = new ActionbarSettings(this,
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						MatchDetailActivity.this.finish();
					}
				}, new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub

					}
				});
		// actionbar.setTopRightIcon(R.drawable.more);
		actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
		actionbar.setTitle(R.string.ab_racedetail);

		web = (WebView) findViewById(R.id.web);
		if (Locale.getDefault().getLanguage().equals("zh")) {

			help = new WebViewHelper(this, web,
					"file:///android_asset/sell/4.html");
		}else if(Locale.getDefault().getLanguage().equals("zh-TW")){
			help = new WebViewHelper(this, web,
					"file:///android_asset/sell/4-tw.html");
		} else {
			help = new WebViewHelper(this, web,
					"file:///android_asset/sell/4-en.html");
		}
		help.initWebview();
		help.setBgTransparent();
		help.addJavascriptInterface(new MatchDetailJsInterface(this, web),
				"MatchDetail");
		if (share.getInt("ISMEMBER", 0) == 0) {
			NoRegisterDialog d = new NoRegisterDialog(this,
					R.string.no_register, R.string.no_register_content);
			d.show();
		}
		mSwipeLayout = (SwipeLayout) findViewById(R.id.id_swipe_ly);
		mSwipeLayout.setViewGroup(web);
		mSwipeLayout.setOnRefreshListener(this);
		mSwipeLayout.setColorScheme(R.color.holo_blue_bright,
				R.color.holo_green_light, R.color.holo_orange_light,
				R.color.holo_red_light);
		InitMatchDetail(matchid);

	}

	public void InitMatchDetail(int matchid) {

		JSONObject arr = db.getMatchDetail(matchid, matchfrom);
		help.loadUrls(new String[] { "getMatchDetail(" + arr + ")" });
		if (arr.length() > 0) {
			progressDialog = Util.initProgressDialog(this, true,getString(R.string.data_wait), null);
			getMatchRank();
			getMatchMembers();
			getMatchRules();
		} else {
			getMatchDetailFromNet();

		}

	}

	public void getMatchDetailFromNet() {
		progressDialog = Util.initProgressDialog(this, true,getString(R.string.data_wait), null);
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Map<String, String> matchargs = new HashMap<String, String>();
				matchargs.put("mid", matchid + "");
				try {
					String mymatch = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/data/getMatchById", matchargs);
					JSONObject js = new JSONObject(mymatch).getJSONArray(
							"content").getJSONObject(0);
					if (js != null && js.length() > 1) {
						js = transfrom(js);
						handler.obtainMessage(100, js).sendToTarget();
					} else {
						handler.obtainMessage(5).sendToTarget();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					handler.obtainMessage(5).sendToTarget();
				}
			}
		}).start();
	}

	public JSONObject transfrom(JSONObject js) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat();
			Long today = (sdf.parse(sdf.format(new Date())).getTime()) / 1000;
			int status = js.getInt("status");
			String startTime = js.getString("start_time");
			String endTime = js.getString("end_time");
			Long s_time = Long.valueOf(startTime);
			Long e_time = Long.valueOf(endTime);
			int delay = 0;
			delay = Math.round((s_time - today) / (3600 * 24f));
			if (delay < 0) {
				delay = Math.round((e_time - today) / (3600 * 24f));
				status = 1;
				if (delay < 0) {
					delay = 0;
					status = 2;
				}
			}
			if (js.getString("poster").length() == 0) {
				js.put("logo", "images/racepic_150.png");
			} else {
				js.put("logo", js.getString("poster"));
			}

			js.put("delay", delay);
			String flag = "";
			if (status == 0) {
				flag = getResources().getString(R.string.gpsstart);
			} else {
				flag = getResources().getString(R.string.over);
			}
			js.put("flag", flag);
			js.put("status", status);
			String target = "日均步数";
			switch (js.getInt(target)) {
			case 0:
				target = "日均步数";
				break;
			case 1:
				target = "总步数";
				break;
			}
			js.put("target", target);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return js;

	}

	public void getMatchMembers() {

		new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Map<String, String> matchargs = new HashMap<String, String>();
					matchargs.put("session", share.getString("session_id", ""));
					matchargs.put("matchid", matchid + "");
					String mymatch = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/data/getMatchMember", matchargs);
					JSONObject json = new JSONObject(mymatch);
					if (json.getInt("status") == 0) {
						JSONArray content = json.getJSONArray("content");
						db.SaveMatchMembers(content, matchid);
						handler.obtainMessage(3).sendToTarget();
					} else {
						handler.obtainMessage(1).sendToTarget();
					}
					refreshHandler.sendEmptyMessage(0);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					if (e.toString().contains("ConnectException")) {
						handler.obtainMessage(4).sendToTarget();
					}
					handler.obtainMessage(1).sendToTarget();
					refreshHandler.sendEmptyMessage(0);
					e.printStackTrace(System.err);
				}

			}
		}.start();

	}

	public void getMatchRules() {
		new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Map<String, String> matchargs = new HashMap<String, String>();
					matchargs.put("session", share.getString("session_id", ""));
					matchargs.put("matchid", matchid + "");
					String mymatch = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/data/getMatchRules", matchargs);
					JSONObject json = new JSONObject(mymatch);
					if (json.getInt("status") == 0) {
						JSONArray content = json.getJSONArray("content");
						db.SaveMatchRules(content, matchid);
						handler.obtainMessage(2).sendToTarget();
					} else {
						handler.obtainMessage(1).sendToTarget();
					}
					refreshHandler.sendEmptyMessage(0);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					if (e.toString().contains("ConnectException")) {
						handler.obtainMessage(4).sendToTarget();
					}
					handler.obtainMessage(1).sendToTarget();
					refreshHandler.sendEmptyMessage(0);
					e.printStackTrace(System.err);
				}

			}
		}.start();

	}

	public void getMatchRank() {
		new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Map<String, String> matchargs = new HashMap<String, String>();
					matchargs.put("session", share.getString("session_id", ""));
					matchargs.put("matchid", matchid + "");
					String mymatch = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/data/getMatchRank", matchargs);
					JSONObject json = new JSONObject(mymatch);
					if (json.getInt("status") == 0) {
						JSONArray content = json.getJSONArray("content");
						db.SaveMatchRank(content, matchid);
						handler.obtainMessage(0).sendToTarget();
					} else {
						handler.obtainMessage(1).sendToTarget();
					}

					refreshHandler.sendEmptyMessage(0);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					if (e.toString().contains("ConnectException")) {
						handler.obtainMessage(4).sendToTarget();
					}
					handler.obtainMessage(1).sendToTarget();
					refreshHandler.sendEmptyMessage(0);
					e.printStackTrace(System.err);
				}

			}
		}.start();

	}

	public boolean isNetworkConnected() {
		ConnectivityManager cm = null;
		try {
			cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		NetworkInfo ni = cm.getActiveNetworkInfo();
		return ni != null && ni.isConnectedOrConnecting();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		refreshHandler.removeCallbacksAndMessages(null);
		handler.removeCallbacksAndMessages(null);
		operatehandler.removeCallbacksAndMessages(null);
		joinHandler.removeCallbacksAndMessages(null);
		quitHandler.removeCallbacksAndMessages(null);
		imageHandler.removeCallbacksAndMessages(null);
		super.onDestroy();
	}

	private Handler refreshHandler = new Handler() {
		public void dispatchMessage(Message msg) {
			switch (msg.what) {
			case 0:
				mSwipeLayout.setRefreshing(false);
				break;

			default:
				break;
			}

		}

	};
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				if (progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				loadMatchRank();
				break;
			case 1:
				if (progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}

				break;
			case 2:
				if (progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				loadMatchRules();
				break;

			case 3:
				if (progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				loadMatchMembers();
				break;
			case 4:
				Toast.makeText(MatchDetailActivity.this,
						R.string.error_network, Toast.LENGTH_SHORT).show();
				break;
			case 100:
				if (progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				help.loadUrls(new String[] { "getMatchDetail("
						+ msg.obj.toString() + ")" });
				getMatchRank();
				getMatchMembers();
				getMatchRules();
				break;
			case 5:
				Toast.makeText(MatchDetailActivity.this,
						R.string.join_match_erro5, Toast.LENGTH_SHORT).show();
				MatchDetailActivity.this.finish();
				break;

			}
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
					dialog = Util.initProgressDialog(MatchDetailActivity.this, true,getString(R.string.data_wait), null);
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
								String result = HttpUtlis.getRequest(
										HttpUtlis.BASE_URL + "/user/addFriend",
										map);
								if (result != null) {
									JSONObject json;
									json = new JSONObject(result);
									int s = Integer.valueOf(json
											.getString("status"));
									MyLog.e("ss", s + "..." + result);
									if (s == 0) {
										msg.what = 0;
										operatehandler.sendMessage(msg);
									} else if (s == 1) {
										msg.what = 1;
										operatehandler.sendMessage(msg);
									} else if (s == 2) {
										msg.what = 2;
										operatehandler.sendMessage(msg);
									} else if (s == 3) {
										msg.what = 3;
										operatehandler.sendMessage(msg);
									} else if (s == 4) {
										msg.what = 4;
										operatehandler.sendMessage(msg);
									} else if (s == 5) {
										msg.what = 5;
										operatehandler.sendMessage(msg);
									} else if (s == 6) {
										msg.what = 6;
										operatehandler.sendMessage(msg);
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
					dialog = Util.initProgressDialog(MatchDetailActivity.this, true,getString(R.string.data_wait), null);
					new Thread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub

							Message msg = handler.obtainMessage();
							Map<String, String> map = new LinkedHashMap<String, String>();
							map.put("uid", share.getInt("UID", -1) + "");
							map.put("fid", uid);

							try {
								String result = HttpUtlis.getRequest(
										HttpUtlis.BASE_URL
												+ "/Guanzhu/doFollow", map);
								System.out.println("ggg" + result);
								if (result != null) {
									JSONObject json;
									json = new JSONObject(result);
									int s = Integer.valueOf(json
											.getString("status"));
									MyLog.e("ss", s + "..." + result);
									if (s == 0) {
										msg.what = 7;
										operatehandler.sendMessage(msg);
									} else if (s == 1) {
										msg.what = 8;
										operatehandler.sendMessage(msg);
									} else if (s == 2) {
										msg.what = 9;
										operatehandler.sendMessage(msg);
									} else if (s == 3) {
										msg.what = 10;
										operatehandler.sendMessage(msg);
									} else if (s == 4) {
										msg.what = 11;
										operatehandler.sendMessage(msg);
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
					page.setClass(MatchDetailActivity.this, Friendpage.class);
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

	Handler operatehandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				Toast.makeText(MatchDetailActivity.this,
						R.string.add_friend_ok, 1).show();
				break;
			case 1:
				Toast.makeText(MatchDetailActivity.this,
						getString(R.string.no_param), 1).show();
				break;
			case 2:
				Toast.makeText(MatchDetailActivity.this,
						getString(R.string.not_user), 1).show();
				break;
			case 3:
				Toast.makeText(MatchDetailActivity.this,
						getString(R.string.not_add_own), 1).show();
				break;
			case 4:
				Toast.makeText(MatchDetailActivity.this,
						getString(R.string.ID_not_exist), 1).show();
				break;
			case 5:
				Toast.makeText(MatchDetailActivity.this,
						getString(R.string.you_have_friend), 1).show();
				break;
			case 6:
				Toast.makeText(MatchDetailActivity.this,
						getString(R.string.wait_agree), 1).show();
				break;
			case 7:
				Toast.makeText(MatchDetailActivity.this,
						getString(R.string.guanzhu_ok), 1).show();
				break;
			case 8:
				Toast.makeText(MatchDetailActivity.this,
						getString(R.string.no_param), 1).show();
				break;
			case 9:
				Toast.makeText(MatchDetailActivity.this,
						getString(R.string.not_own), 1).show();
				break;
			case 10:
				Toast.makeText(MatchDetailActivity.this,
						getString(R.string.user_no_exist), 1).show();
				break;
			case 11:
				Toast.makeText(MatchDetailActivity.this,
						getString(R.string.have_guanzhu), 1).show();
				break;

			}
		}
	};

	public void loadMatchMembers() {
		JSONObject obj = new JSONObject();
		try {
			JSONArray json = db.getMatchMembers(matchid);
			obj = new JSONObject();
			obj.put("mateList", json);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		help.loadUrls(new String[] { "loadMatchMembers(" + obj + ")" });
	}

	public void loadMatchRules() {
		JSONObject json = db.getMatchRules(matchid);
		help.loadUrls(new String[] { "loadMatchRules(" + json + ")" });
	}

	public void loadMatchRank() {
		JSONObject obj = new JSONObject();
		try {
			JSONArray json = db.getMatchRank(matchid);
			obj = new JSONObject();
			obj.put("scorelist", json);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		help.loadUrls(new String[] { "getMatchRank(" + obj + ")" });
	}

	public void joinMatch() {

		progressDialog = Util.initProgressDialog(this, true,getString(R.string.data_wait), null);
		new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Map<String, String> matchargs = new HashMap<String, String>();
					matchargs.put("session", share.getString("session_id", ""));
					matchargs.put("matchid", matchid + "");
					String mymatch = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/data/joinMatch", matchargs);
					JSONObject json = new JSONObject(mymatch);
					int status = json.getInt("status");
					joinHandler.obtainMessage(status).sendToTarget();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					if (e.toString().contains("ConnectException")) {
						handler.obtainMessage(4).sendToTarget();
					}
					joinHandler.obtainMessage(1).sendToTarget();
					e.printStackTrace(System.err);
				}

			}
		}.start();

	}

	public void quitMatch() {
		progressDialog = Util.initProgressDialog(this, true,getString(R.string.data_wait), null);
		new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Map<String, String> matchargs = new HashMap<String, String>();
					matchargs.put("session", share.getString("session_id", ""));
					matchargs.put("matchid", matchid + "");
					String mymatch = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/data/quitMatch", matchargs);
					JSONObject json = new JSONObject(mymatch);
					int status = json.getInt("status");
					quitHandler.obtainMessage(status).sendToTarget();
					db.quitMacth(matchid + "", matchfrom);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					if (e.toString().contains("ConnectException")) {
						handler.obtainMessage(4).sendToTarget();
					}
					quitHandler.obtainMessage(8).sendToTarget();
					e.printStackTrace(System.err);
				}

			}
		}.start();

	}

	private Handler joinHandler = new Handler() {

		public void dispatchMessage(Message msg) {
			if (progressDialog != null && progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
			switch (msg.what) {
			case 0:
				Toast.makeText(MatchDetailActivity.this,
						R.string.join_match_success, Toast.LENGTH_SHORT).show();
				db.addMyMatch(matchid, matchfrom);
				updateMatchDetail(1);
				break;
			case 3:
				Toast.makeText(MatchDetailActivity.this,
						R.string.join_match_erro1, Toast.LENGTH_SHORT).show();
				break;
			case 4:
				Toast.makeText(MatchDetailActivity.this,
						R.string.join_match_erro2, Toast.LENGTH_SHORT).show();
				break;
			case 5:
				Toast.makeText(MatchDetailActivity.this,
						R.string.join_match_erro3, Toast.LENGTH_SHORT).show();
				break;
			case 6:
				Toast.makeText(MatchDetailActivity.this,
						R.string.join_match_erro4, Toast.LENGTH_SHORT).show();
				break;
			case 7:
				Toast.makeText(MatchDetailActivity.this,
						R.string.join_match_erro5, Toast.LENGTH_SHORT).show();
				break;
			default:
				Toast.makeText(MatchDetailActivity.this,
						R.string.join_match_erro6, Toast.LENGTH_SHORT).show();
				break;
			}

		}

	};

	private void updateMatchDetail(int from) {
		MatchFragment.REFRESH_FLAG = true;
		matchfrom = from;

		JSONObject arr = db.getMatchDetail(matchid, matchfrom);
		if (arr.length() > 0) {
			help.loadUrls(new String[] { "getMatchDetail(" + arr + ")" });
			if (tab == 0) {
				progressDialog = Util.initProgressDialog(this, true,getString(R.string.data_wait), null);
				getMatchRank();
			} else if (tab == 1) {
				progressDialog = Util.initProgressDialog(this, true,getString(R.string.data_wait), null);
				getMatchMembers();
			}

		} else {
			Toast.makeText(this, R.string.join_match_erro5, Toast.LENGTH_SHORT)
					.show();
			this.finish();
		}
	}

	private Handler quitHandler = new Handler() {
		public void dispatchMessage(Message msg) {
			if (progressDialog != null && progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
			switch (msg.what) {
			case 0:
				Toast.makeText(MatchDetailActivity.this,
						R.string.quit_match_success, Toast.LENGTH_SHORT).show();
				db.removeMyMatch(matchid);
				updateMatchDetail(2);
				break;
			case 3:
				Toast.makeText(MatchDetailActivity.this,
						R.string.quit_match_erro1, Toast.LENGTH_SHORT).show();
				break;
			case 4:
				Toast.makeText(MatchDetailActivity.this,
						R.string.quit_match_erro2, Toast.LENGTH_SHORT).show();
				break;
			case 5:
				Toast.makeText(MatchDetailActivity.this,
						R.string.quit_match_erro3, Toast.LENGTH_SHORT).show();
				break;
			default:
				Toast.makeText(MatchDetailActivity.this,
						R.string.quit_match_erro4, Toast.LENGTH_SHORT).show();
				break;
			}

		}

	};

	ProgressDialog dialog;

	public void showBigImg(final String urlstr) {
		String a = urlstr.substring(urlstr.lastIndexOf(".") + 1,
				urlstr.length());
		List<String> types = Arrays.asList(new String[] { "jpg", "jpeg", "png",
				"pjpeg", "/gif", "/bmp", "/x-png" });
		if (types.contains(a)) {
			dialog = Util.initProgressDialog(this, true,getString(R.string.data_wait), null);
			new Thread(new Runnable() {

				public void run() {
					try {
						URL url = new URL(urlstr);
						Drawable d = BitmapDrawable.createFromStream(
								url.openStream(), "");
						// 下面是为了在logo链接失效时触发空指针异常来加载默认图片
						d.getLevel();
						imageHandler.obtainMessage(0, d).sendToTarget();
					} catch (Exception e) {
						if (e.toString().contains("ConnectException")) {
							imageHandler.obtainMessage(1).sendToTarget();
						} else {
							try {
								Drawable d = BitmapDrawable
										.createFromStream(
												getAssets()
														.open("sell/images/clubmode/model1.jpg"),
												"");
								imageHandler.obtainMessage(0, d).sendToTarget();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
						e.printStackTrace();
					}

				}
			}).start();
		}

	}

	Handler imageHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (dialog != null && dialog.isShowing()) {
				dialog.dismiss();
			}
			if (msg.what == 0) {
				getBigImg((Drawable) msg.obj);
			} else if (msg.what == 1) {
				Toast.makeText(MatchDetailActivity.this,
						getString(R.string.network_erro), Toast.LENGTH_SHORT)
						.show();

			}
			super.handleMessage(msg);
		}

	};

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

	// public class MatchDetailJsInterface {
	// Context context;
	//
	// public MatchDetailJsInterface(Context context) {
	// super();
	// this.context = context;
	// }
	//
	// public void getCurrentTab(String tab) {
	// MatchDetailActivity.tab = Integer.parseInt(tab);
	// }
	//
	// public void showBigImg(final String url) {
	// String a = url.substring(url.lastIndexOf(".") + 1, url.length());
	// List<String> types = Arrays.asList(new String[] { "jpg", "jpeg",
	// "png", "pjpeg", "/gif", "/bmp", "/x-png" });
	// if (types.contains(a)) {
	// runOnUiThread(new Runnable() {
	//
	// public void run() {
	// // Code that interact with UI
	// View view = LayoutInflater.from(context).inflate(
	// R.layout.pop_bigimg, null);
	// final PopupWindow popwindow = new PopupWindow(view,
	// LayoutParams.MATCH_PARENT,
	// LayoutParams.MATCH_PARENT, true);
	// popwindow.setOutsideTouchable(true);
	// popwindow.setBackgroundDrawable(new BitmapDrawable());
	// // ImageLoader.getInstance().displayImage(url, iv);
	// popwindow.showAtLocation(web.getRootView(),
	// Gravity.CENTER, 0, 0);
	// ImageView iv = (ImageView) popwindow.getContentView()
	// .findViewById(R.id.img);
	// imageLoader.DisplayImageMatch(url, iv);
	// iv.setOnClickListener(new OnClickListener() {
	//
	// @Override
	// public void onClick(View v) {
	// // TODO Auto-generated method stub
	// if (popwindow != null && popwindow.isShowing()) {
	// popwindow.dismiss();
	// }
	// }
	// });
	// view.setOnClickListener(new OnClickListener() {
	//
	// @Override
	// public void onClick(View v) {
	// // TODO Auto-generated method stub
	// if (popwindow != null && popwindow.isShowing()) {
	// popwindow.dismiss();
	// }
	// }
	// });
	// }
	// });
	// }
	//
	// }
	//
	// public void jqJsMatch(String ismember) {
	// if (share.getInt("ISMEMBER", 0) == 0) {
	// NoRegisterDialog d = new NoRegisterDialog(context,
	// R.string.no_register, R.string.no_register_content);
	// d.show();
	// } else {
	// int ismeb = Integer.parseInt(ismember);
	//
	// if (ismeb == 1) {
	// AlertDialog alertDialog = new AlertDialog.Builder(context)
	// .setTitle(
	// context.getString(R.string.quit_match_ensure))
	// .setIcon(android.R.drawable.ic_dialog_info)
	// .setPositiveButton("确定",
	// new DialogInterface.OnClickListener() {
	//
	// @Override
	// public void onClick(
	// DialogInterface dialog,
	// int which) {
	// // TODO Auto-generated method stub
	// quitMatch();
	// }
	// })
	// .setNegativeButton("取消",
	// new DialogInterface.OnClickListener() {
	//
	// @Override
	// public void onClick(
	// DialogInterface dialog,
	// int which) {
	// // TODO Auto-generated method stub
	// }
	// }).show();
	// } else {
	// joinMatch();
	// }
	// }
	//
	// }
	//
	// }

	@Override
	public void onRefresh() {

		// TODO Auto-generated method stub
		if (isNetworkConnected()) {
			web.clearCache(true);
			if (tab == 0) {
				getMatchRank();
			} else if (tab == 1) {
				getMatchMembers();
			} else {
				getMatchRules();
			}
		} else {
			mSwipeLayout.setRefreshing(false);
			if (tab == 0) {
				loadMatchRank();
			} else if (tab == 1) {
				loadMatchMembers();
			} else {
				loadMatchRules();
			}

		}

	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub

	}
}
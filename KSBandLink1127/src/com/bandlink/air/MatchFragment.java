package com.bandlink.air;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bandlink.air.Js.MatchInterface;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.MainInterface;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Util;
import com.bandlink.air.util.WebViewHelper;
import com.bandlink.air.view.NoRegisterDialog;
import com.bandlink.air.view.SwipeLayout;
import com.umeng.analytics.MobclickAgent;

public class MatchFragment extends Fragment implements
		SwipeRefreshLayout.OnRefreshListener {
	private MainInterface minterface;
	WebViewHelper help;
	private SharedPreferences share;
	ProgressDialog progressDialog;
	private int all_index = 0;// 起始index
	private final int all_num = 10;// 每次取的条数

	private int my_index = 0;
	private final int my_num = 10;

	private int ismember;
	private WebView web;
	private SwipeLayout mSwipeLayout;
	public static int tab = 0;
	public static boolean REFRESH_FLAG = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View contentView = inflater.inflate(R.layout.frg_frame, null);
		ActionbarSettings actionbar = new ActionbarSettings(contentView, lsr,
				minterface);
		actionbar.setTopRightIcon(R.drawable.ic_top_more);
		actionbar.setTitle(R.string.ab_race);
		web = (WebView) contentView.findViewById(R.id.web);
		if (Locale.getDefault().getLanguage().equals("zh")) {

			help = new WebViewHelper(getActivity(), web,
					"file:///android_asset/sell/5.html");
		} else if (Locale.getDefault().getLanguage().equals("zh-TW")) {
			help = new WebViewHelper(getActivity(), web,
					"file:///android_asset/sell/5-tw.html");
		} else {
			help = new WebViewHelper(getActivity(), web,
					"file:///android_asset/sell/5-en.html");
		}
		help.addJavascriptInterface(new MatchInterface(this,
				this.getActivity(), web), "Match");
		help.initWebview();
		help.setBgTransparent();
		share = getActivity().getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		if (share.getInt("ISMEMBER", 0) == 0) {
			NoRegisterDialog d = new NoRegisterDialog(getActivity(),
					R.string.no_register, R.string.no_register_content);
			d.show();
		}

		mSwipeLayout = (SwipeLayout) contentView.findViewById(R.id.id_swipe_ly);
		mSwipeLayout.setViewGroup(web);
		mSwipeLayout.setOnRefreshListener(this);
		mSwipeLayout.setColorScheme(R.color.holo_blue_bright,
				R.color.holo_green_light, R.color.holo_orange_light,
				R.color.holo_red_light);

		getAllMatch();
		getMyMatch();
		loadAllMatch();
		loadMyMatch();
		return contentView;
	}

	public void getMyMatch() {
		// progressDialog =
		// Util.initProgressDialog(Util.getThemeContext(getActivity()), true,
		// getString(R.string.data_wait), null);
		new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Map<String, String> matchargs = new HashMap<String, String>();
					matchargs.put("session", share.getString("session_id", ""));
					matchargs.put("index", my_index + "");
					matchargs.put("num", my_num + "");
					String mymatch = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/data/getMyMatch", matchargs);
					JSONObject json = new JSONObject(mymatch);
					if (json.getInt("status") == 0) {
						JSONArray content = json.getJSONArray("content");
						Dbutils db = new Dbutils(share.getInt("UID", -1),
								getActivity());
						db.saveMyMatch(content);
						handler.obtainMessage(0, content).sendToTarget();
					} else {
						handler.obtainMessage(1, 0).sendToTarget();
					}
					if (tab == 1)
						refreshHandler.sendEmptyMessage(0);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					if (e.toString().contains("ConnectException")) {
						handler.obtainMessage(6).sendToTarget();
					}
					handler.obtainMessage(1, 0).sendToTarget();
					if (tab == 1)
						refreshHandler.sendEmptyMessage(0);
					e.printStackTrace(System.err);
				}

			}
		}.start();

	}

	public void getAllMatch() {
		progressDialog = Util.initProgressDialog(getActivity(), true,
				getString(R.string.data_wait), null);
		new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Map<String, String> matchargs = new HashMap<String, String>();
					matchargs.put("session", share.getString("session_id", ""));
					matchargs.put("index", all_index + "");
					matchargs.put("num", all_num + "");
					String mymatch = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/data/getAllMatch", matchargs);
					JSONObject json = new JSONObject(mymatch);
					if (json.getInt("status") == 0) {
						JSONArray content = json.getJSONArray("content");
						Dbutils db = new Dbutils(share.getInt("UID", -1),
								getActivity());
						db.saveAllMatch(content);
						handler.obtainMessage(2, content).sendToTarget();
					} else {
						handler.obtainMessage(1, 1).sendToTarget();
					}
					if (tab == 0)
						refreshHandler.sendEmptyMessage(0);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					if (e.toString().contains("ConnectException")) {
						handler.obtainMessage(6).sendToTarget();
					}
					handler.obtainMessage(1, 1).sendToTarget();
					if (tab == 0)
						refreshHandler.sendEmptyMessage(0);
					e.printStackTrace(System.err);
				}

			}
		}.start();

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

	public void searchMatch(final String search) {
		progressDialog = Util.initProgressDialog(getActivity(), true,
				getString(R.string.data_wait), null);
		new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Map<String, String> matchargs = new HashMap<String, String>();
					matchargs.put("session", share.getString("session_id", ""));
					matchargs.put("search", search);
					String mymatch = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/data/searchMatch", matchargs);
					JSONObject json = new JSONObject(mymatch);
					if (json.getInt("status") == 0) {
						JSONArray content = json.getJSONArray("content");
						Dbutils db = new Dbutils(share.getInt("UID", -1),
								getActivity());
						db.saveSearchMatch(content);
						handler.obtainMessage(4).sendToTarget();
					} else {
						handler.obtainMessage(5).sendToTarget();
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					if (e.toString().contains("ConnectException")) {
						handler.obtainMessage(6).sendToTarget();
					} else {
						handler.obtainMessage(5).sendToTarget();
					}
					e.printStackTrace(System.err);
				}

			}
		}.start();

	}

	public void searchMyMatch(final String search) {
		// progressDialog =
		// Util.initProgressDialog(Util.getThemeContext(getActivity()), true,
		// getString(R.string.data_wait), null);
		// Dbutils db = new Dbutils(share.getInt("UID", -1), getActivity());
		// JSONArray json = db.searchMyMatch(search);
		// JSONObject obj = new JSONObject();
		// try {
		// obj.put("ctxsearch", json);
		// } catch (JSONException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// if (progressDialog != null && progressDialog.isShowing()) {
		// progressDialog.dismiss();
		// }
		//
		// help.loadUrls(new String[] { "loadSearchMatch(" + obj + ")" });

		progressDialog = Util.initProgressDialog(getActivity(), true,
				getString(R.string.data_wait), null);
		new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Map<String, String> matchargs = new HashMap<String, String>();
					matchargs.put("session", share.getString("session_id", ""));
					matchargs.put("search", search);
					String mymatch = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/data/searchMyMatch", matchargs);
					JSONObject json = new JSONObject(mymatch);
					if (json.getInt("status") == 0) {
						JSONArray content = json.getJSONArray("content");
						Dbutils db = new Dbutils(share.getInt("UID", -1),
								getActivity());
						db.saveSearchMatch(content);
						handler.obtainMessage(4).sendToTarget();
					} else {
						handler.obtainMessage(5).sendToTarget();
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block

					if (e.toString().contains("ConnectException")) {
						handler.obtainMessage(6).sendToTarget();
					} else {
						handler.obtainMessage(5).sendToTarget();
					}

					e.printStackTrace(System.err);
				}

			}
		}.start();

	}

	// public JSONArray analyzeMatch(JSONArray arr) throws Exception {
	// JSONArray result = new JSONArray();
	// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	// Long today = (sdf.parse(sdf.format(new Date())).getTime()) / 1000;
	// for (int i = 0; i < arr.length(); i++) {
	// JSONObject obj = (JSONObject) arr.get(i);
	// int status = Integer.parseInt(obj.getString("status"));
	// String startTime = obj.getString("start_time");
	// String endTime = obj.getString("end_time");
	// Long s_time = Long.valueOf(startTime);
	// Long e_time = Long.valueOf(endTime);
	// String s = sdf.format(new Date(s_time * 1000L));
	// String e = sdf.format(new Date(e_time * 1000L));
	// int delay = 0;
	// if (status == 0) {// 未开始
	// delay = Math.round((s_time - today) / (3600 * 24f));
	// } else if (status == 1) {// 正在进行
	// delay = Math.round((e_time - today) / (3600 * 24f));
	// }
	// JSONObject ob = new JSONObject();
	// if (obj.getString("poster").equals("")) {
	// ob.put("logo", "images/club/racepic_150.png");
	// } else {
	// ob.put("logo", obj.getString("poster"));
	// }
	// ob.put("id", obj.getInt("id"));
	// ob.put("uid", obj.getString("uid"));
	// ob.put("username", obj.getString("username"));
	// ob.put("title", obj.getString("title"));
	// ob.put("membernum", obj.getString("membernum"));
	// ob.put("start_time", startTime);
	// ob.put("end_time", endTime);
	// ob.put("delay", delay);
	// ob.put("status", obj.getString("status"));
	// ob.put("ispublic", obj.getString("is_public"));
	// ob.put("target", obj.getString("target"));
	// result.put(ob);
	// }
	//
	// return result;
	//
	// }

	boolean moreFlag = true;

	public void getMoreMatch() {
		progressDialog = Util.initProgressDialog(getActivity(), true,
				getString(R.string.data_wait), null);
		new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				moreFlag = false;
				try {
					all_index += all_num;
					Map<String, String> matchargs = new HashMap<String, String>();
					matchargs.put("session", share.getString("session_id", ""));
					matchargs.put("index", all_index + "");
					matchargs.put("num", all_num + "");
					String mymatch = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/data/getAllMatch", matchargs);
					JSONObject json = new JSONObject(mymatch);
					if (json.getInt("status") == 0) {
						JSONArray content = json.getJSONArray("content");
						Dbutils db = new Dbutils(share.getInt("UID", -1),
								getActivity());
						db.saveMoreAllMatch(content);
						handler.obtainMessage(3, content).sendToTarget();
					} else {
						handler.obtainMessage(3).sendToTarget();
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					handler.obtainMessage(3).sendToTarget();
					e.printStackTrace(System.err);
				}

			}
		}.start();

	}

	boolean mymoreFlag = true;

	public void getMyMoreMatch() {
		progressDialog = Util.initProgressDialog(getActivity(), true,
				getString(R.string.data_wait), null);
		new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mymoreFlag = false;
				try {
					my_index += my_num;
					Map<String, String> matchargs = new HashMap<String, String>();
					matchargs.put("session", share.getString("session_id", ""));
					matchargs.put("index", my_index + "");
					matchargs.put("num", my_num + "");
					String mymatch = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/data/getMyMatch", matchargs);
					JSONObject json = new JSONObject(mymatch);
					if (json.getInt("status") == 0) {
						JSONArray content = json.getJSONArray("content");
						Dbutils db = new Dbutils(share.getInt("UID", -1),
								getActivity());
						db.saveMoreMyMatch(content);
						handler.obtainMessage(7, content).sendToTarget();
					} else {
						handler.obtainMessage(7).sendToTarget();
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					handler.obtainMessage(7).sendToTarget();
					e.printStackTrace(System.err);
				}

			}
		}.start();

	}

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				moreFlag = true;
				loadMyMatch();
				break;
			case 1:
				int from = (Integer) msg.obj;
				if (from == 0) {
					loadMyMatch();
				} else if (from == 1) {
					loadAllMatch();
				}
				break;
			case 2:
				loadAllMatch();
				break;
			case 3:
				moreFlag = true;
				loadMoreMatch((JSONArray) msg.obj);
				break;
			case 4:
				loadSearchMatch();
				break;
			case 5:
				if (progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				Toast.makeText(getActivity(), R.string.getdata_erro,
						Toast.LENGTH_SHORT).show();
				break;
			case 6:
				Toast.makeText(getActivity(), R.string.error_network,
						Toast.LENGTH_SHORT).show();
			case 7:
				mymoreFlag = true;
				loadMyMoreMatch();
				break;
			}
		}
	};

	public void loadMyMatch() {
		Dbutils db = new Dbutils(share.getInt("UID", -1), getActivity());
		JSONArray json = db.getMyMatch(my_index, my_num);
		JSONObject obj = new JSONObject();
		try {
			obj.put("myctx", json);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
		help.loadUrls(new String[] { "loadMyMatchRefresh(" + obj + ")" });

	}

	public void loadAllMatch() {
		Dbutils db = new Dbutils(share.getInt("UID", -1), getActivity());
		JSONArray json = db.getAllMatch(all_index, all_num);
		JSONObject obj = new JSONObject();
		try {
			obj.put("ctxcenter", json);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
		help.loadUrls(new String[] { "loadMatchAllRefresh(" + obj + ")" });
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

	public void loadMoreMatch(JSONArray js) {
		// Dbutils db = new Dbutils(share.getInt("UID", -1), getActivity());
		// JSONArray json = db.getAllMatch(all_index, all_num);
		for (int i = 0; i < js.length(); i++) {
			try {
				transfrom(js.getJSONObject(i));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		JSONObject obj = new JSONObject();
		try {
			obj.put("ctxcenter", js);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
		help.loadUrls(new String[] { "loadMatchAllMore(" + obj + ")" });
	}

	public void loadMyMoreMatch() {
		Dbutils db = new Dbutils(share.getInt("UID", -1), getActivity());
		JSONArray json = db.getMyMatch(my_index, my_num);
		JSONObject obj = new JSONObject();
		try {
			obj.put("myctx", json);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
		help.loadUrls(new String[] { "loadMatchMyMore(" + obj + ")" });
	}

	public void loadSearchMatch() {
		Dbutils db = new Dbutils(share.getInt("UID", -1), getActivity());
		JSONArray json = db.getSearchMatch(0, 0);
		JSONObject obj = new JSONObject();
		try {
			obj.put("ctxsearch", json);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
		help.loadUrls(new String[] { "loadSearchMatch(" + obj + ")" });

	}

	public void GotoMatchDetail(final int id, final int from) {

		Runnable runnable = new Runnable() {
			public void run() {
				// your code here
				Intent intent = new Intent(getActivity(),
						MatchDetailActivity.class);
				intent.putExtra("id", id);
				intent.putExtra("from", from);
				startActivity(intent);
			}
		};
		getActivity().runOnUiThread(runnable);
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		if (REFRESH_FLAG) {
			loadAllMatch();
			loadMyMatch();
			REFRESH_FLAG = false;
		}
		MobclickAgent.onPageStart("MatchFragment"); // 统计页面
		super.onResume();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		MobclickAgent.onPageEnd("MatchFragment"); // 统计页面
		super.onPause();
	}

	public void setrefresh(final boolean isenable) {
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				mSwipeLayout.setEnabled(isenable);
			}
		});

	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		minterface = (MainInterface) activity;
		super.onAttach(activity);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		handler.removeCallbacksAndMessages(null);
		refreshHandler.removeCallbacksAndMessages(null);
		super.onDestroy();
	}

	// actionbar右侧按钮
	private PopupWindow initPopupWindow() {
		final PopupWindow pop = new PopupWindow(getActivity()
				.getLayoutInflater().inflate(R.layout.pop_club_operate, null),
				LayoutParams.WRAP_CONTENT, getActivity().getResources()
						.getDimensionPixelSize(R.dimen.icon_large_x), true);
		Button create = (Button) pop.getContentView().findViewById(
				R.id.add_guanzhu);
		create.setText(R.string.ab_createrace);
		Button update = (Button) pop.getContentView().findViewById(
				R.id.a_friend);
		Button invite = (Button) pop.getContentView().findViewById(R.id.invite);
		Button manager = (Button) pop.getContentView()
				.findViewById(R.id.ignore);
		TextView line1 = (TextView) pop.getContentView().findViewById(R.id.l1);
		TextView line2 = (TextView) pop.getContentView().findViewById(R.id.t2);
		line1.setVisibility(View.GONE);
		TextView line3 = (TextView) pop.getContentView().findViewById(R.id.t3);
		line3.setVisibility(View.GONE);
		line2.setVisibility(View.GONE);
		update.setVisibility(View.GONE);
		invite.setVisibility(View.GONE);
		manager.setVisibility(View.GONE);
		create.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				pop.dismiss();
				if (share.getInt("ISMEMBER", 0) == 0) {
					NoRegisterDialog d = new NoRegisterDialog(getActivity(),
							R.string.no_register, R.string.no_register_content);
					d.show();
					return;
				}

				startActivity(new Intent(getActivity(),
						CreateMatchActivity.class));
			}
		});

		pop.setBackgroundDrawable(new BitmapDrawable());
		pop.setOutsideTouchable(true);
		pop.setTouchable(true);
		return pop;
	}

	private OnClickListener lsr = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Context c = Util.getThemeContext(getActivity());
			AlertDialog.Builder ab = new AlertDialog.Builder(c);
			ab.setTitle(R.string.set_select);
			ab.setItems(new String[] { getString(R.string.ab_createrace) },
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							if (share.getInt("ISMEMBER", 0) == 0) {
								NoRegisterDialog d = new NoRegisterDialog(
										getActivity(), R.string.no_register,
										R.string.no_register_content);
								d.show();
								return;
							}

							startActivity(new Intent(getActivity(),
									CreateMatchActivity.class));
						}
					});
			ab.setNegativeButton(R.string.can, null);
			ab.create().show();

		}
	};

	// public class MatchInterface {
	// Context context;
	//
	// public MatchInterface(Context context) {
	// super();
	// this.context = context;
	// }
	//
	// /***
	// * 显示大图
	// *
	// * @param url
	// * 图片url
	// */
	// public void getBigImg(Drawable d) {
	//
	// View view = LayoutInflater.from(context).inflate(
	// R.layout.pop_bigimg, null);
	// final PopupWindow popwindow = new PopupWindow(view,
	// LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true);
	// popwindow.setOutsideTouchable(true);
	// popwindow.setBackgroundDrawable(new BitmapDrawable());
	//
	// GestureImageView iv = (GestureImageView) view
	// .findViewById(R.id.img);
	// iv.setImageDrawable(d);
	// popwindow.showAtLocation(web.getRootView(), Gravity.CENTER, 0, 0);
	// view.setOnClickListener(new OnClickListener() {
	//
	// @Override
	// public void onClick(View v) {
	// // TODO Auto-generated method stub
	// popwindow.dismiss();
	// }
	// });
	// }
	//
	// public Handler imageHandler = new Handler() {
	//
	// @Override
	// public void handleMessage(Message msg) {
	// if (progressDialog != null && progressDialog.isShowing()) {
	// progressDialog.dismiss();
	// }
	// if (msg.what == 0) {
	// getBigImg((Drawable) msg.obj);
	// }
	// super.handleMessage(msg);
	// }
	//
	// };
	//
	// public void showBigImg(final String path) {
	// progressDialog =
	// Util.initProgressDialog(Util.getThemeContext(getActivity()), true,
	// getString(R.string.data_wait), null);
	// new Thread(new Runnable() {
	//
	// @Override
	// public void run() {
	// // TODO Auto-generated method stub
	// try {
	// URL url = new URL(path);
	// Drawable d = BitmapDrawable.createFromStream(
	// url.openStream(), "");
	// imageHandler.obtainMessage(0, d).sendToTarget();
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// }
	//
	// }).start();
	// }
	//
	// // public void showMatchDetail(String matchid) {
	// // MatchDetailFragment f = new MatchDetailFragment();
	// // Bundle d = new Bundle();
	// // d.putString("matchid", matchid);
	// // f.setArguments(d);
	// // getFragmentManager().beginTransaction().replace(R.id.main_frame, f)
	// // .commit();
	// // }
	//
	// public void getJsMyMatch() {
	// getMyMatch();
	// }
	//
	// public void getJsAllMatch() {
	// getAllMatch();
	//
	// }
	//
	// public void getJsMoreMatch() {
	// all_index += 2;
	// all_num = 2;
	// getMoreMatch();
	// }
	//
	// public void searchJsMatch(String search, String tab) {
	// int t = Integer.parseInt(tab);
	// if (t == 1) {
	// searchMyMatch(search);
	// } else {
	// searchMatch(search);
	// }
	//
	// }
	//
	// public void getJsMatchDetail(String matchid, String matchfrom) {
	// Intent intent = new Intent(getActivity(), MatchDetailActivity.class);
	// intent.putExtra("id", Integer.parseInt(matchid));
	// intent.putExtra("from", Integer.parseInt(matchfrom));
	// startActivity(intent);
	// }
	//
	// public void getCurrentTab(String tab) {
	// MatchFragment.tab = Integer.parseInt(tab);
	// }
	//
	// }
	public boolean isNetworkConnected() {
		ConnectivityManager cm = null;
		try {
			cm = (ConnectivityManager) getActivity().getSystemService(
					Context.CONNECTIVITY_SERVICE);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		NetworkInfo ni = cm.getActiveNetworkInfo();
		return ni != null && ni.isConnectedOrConnecting();
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		if (isNetworkConnected()) {
			web.clearCache(true);
			if (tab == 0) {
				all_index = 0;
				getAllMatch();
			} else if (tab == 1) {
				my_index = 0;
				getMyMatch();
			}

		} else {
			mSwipeLayout.setRefreshing(false);
			if (tab == 0) {
				all_index = 0;
				loadAllMatch();
			} else if (tab == 1) {
				my_index = 0;
				loadMyMatch();
			}

		}

	}

}

package com.bandlink.air;

//汉子未修改
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.bandlink.air.Js.ClubeJsInterface;
import com.bandlink.air.club.ChargeClubJoin;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.DbContract.ClubInfo;
import com.bandlink.air.util.DbContract.MyClub;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.MainInterface;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Util;
import com.bandlink.air.util.WebViewHelper;
import com.bandlink.air.view.NoRegisterDialog;
import com.bandlink.air.view.SwipeLayout;
import com.umeng.analytics.MobclickAgent;

public class ClubFragment extends Fragment implements
		SwipeRefreshLayout.OnRefreshListener {
	private MainInterface minterface;
	private WebViewHelper help;
	private ProgressDialog dialog;
	private ClubeJsInterface clubjs;
	private WebView web;
	private SwipeLayout mSwipeLayout;
	public int currentTab = 0;
	private View contentView;
	private int ismember;
	private SharedPreferences share;
	private NoRegisterDialog d;
	public String session_id;
	private Dbutils db;
	private boolean isMInit = true;
	private JSONArray joinList;
	public static final String CLUB_SCAN_ACTION = "com.milink.scanclub";
	BroadcastReceiver reveiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals(CLUB_SCAN_ACTION)) {
				getActivity().unregisterReceiver(reveiver);
				String content = intent.getStringExtra("content");

				try {
					JSONObject js = new JSONObject(content);
					showJoin(js);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};

	void showJoin(final JSONObject js) {
		AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
		ab.setTitle(R.string.warning);
		ab.setMessage(String.format(getString(R.string.join_club),
				decodeUnicode(js.optString("clubname", ""))));
		ab.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						try {
							join(js.getString("clubid"), session_id);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
		ab.setNegativeButton(R.string.cancel, null);
		ab.create().show();

	}

	public static String decodeUnicode(String theString) {

		char aChar;

		int len = theString.length();

		StringBuffer outBuffer = new StringBuffer(len);

		for (int x = 0; x < len;) {

			aChar = theString.charAt(x++);

			if (aChar == '\\') {

				aChar = theString.charAt(x++);

				if (aChar == 'u') {

					// Read the xxxx

					int value = 0;

					for (int i = 0; i < 4; i++) {

						aChar = theString.charAt(x++);

						switch (aChar) {

						case '0':

						case '1':

						case '2':

						case '3':

						case '4':

						case '5':

						case '6':
						case '7':
						case '8':
						case '9':
							value = (value << 4) + aChar - '0';
							break;
						case 'a':
						case 'b':
						case 'c':
						case 'd':
						case 'e':
						case 'f':
							value = (value << 4) + 10 + aChar - 'a';
							break;
						case 'A':
						case 'B':
						case 'C':
						case 'D':
						case 'E':
						case 'F':
							value = (value << 4) + 10 + aChar - 'A';
							break;
						default:
							throw new IllegalArgumentException(
									"Malformed   \\uxxxx   encoding.");
						}

					}
					outBuffer.append((char) value);
				} else {
					if (aChar == 't')
						aChar = '\t';
					else if (aChar == 'r')
						aChar = '\r';

					else if (aChar == 'n')

						aChar = '\n';

					else if (aChar == 'f')

						aChar = '\f';

					outBuffer.append(aChar);

				}

			} else

				outBuffer.append(aChar);

		}

		return outBuffer.toString();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		contentView = inflater.inflate(R.layout.frg_frame, null);

		share = getActivity().getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);

		ActionbarSettings actionbar = new ActionbarSettings(contentView, lsr,
				minterface);
		actionbar.setTopRightIcon(R.drawable.ic_top_more);
		actionbar.setTitle(R.string.ab_club);
		web = (WebView) contentView.findViewById(R.id.web);
		web.requestFocus();
		web.getSettings().setDefaultTextEncodingName("utf-8");

		if (Locale.getDefault().getLanguage().equals("zh")) {

			help = new WebViewHelper(getActivity(), web,
					"file:///android_asset/sell/1.html");
		}else if(Locale.getDefault().getLanguage().equals("zh-TW")){
			help = new WebViewHelper(getActivity(), web,
					"file:///android_asset/sell/1-tw.html");
		} else {
			help = new WebViewHelper(getActivity(), web,
					"file:///android_asset/sell/1-en.html");
		}
		clubjs = new ClubeJsInterface(this);
		help.addJavascriptInterface(clubjs, "Club");
		help.initWebview();
		help.setBgTransparent();

		mSwipeLayout = (SwipeLayout) contentView.findViewById(R.id.id_swipe_ly);
		mSwipeLayout.setViewGroup(web);
		mSwipeLayout.setOnRefreshListener(this);
		mSwipeLayout.setColorScheme(R.color.holo_blue_bright,
				R.color.holo_green_light, R.color.holo_orange_light,
				R.color.holo_red_light);
		handData.sendEmptyMessageDelayed(6, 1000);
		return contentView;
	}

	@Override
	public void onResume() {
		curPickerIndex = new int[3];
		share = getActivity().getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		db = new Dbutils(share.getInt("UID", -1), getActivity());
		ismember = share.getInt("ISMEMBER", 0);
		session_id = share.getString("session_id", "-1");

		if (currentTab == 0) {
			getClub(0);
		} else if (currentTab == 1) {
			clubjs.getMyClubs(0, "");
			getClubJoinNum();
		}
		MobclickAgent.onPageStart("ClubFragment");
		super.onResume();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		MobclickAgent.onPageEnd("ClubFragment");
		super.onPause();
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
		}
		if (handData != null) {
			handData.removeCallbacksAndMessages(null);
		}
		super.onStop();
	}

	// actionbar右侧按钮
	private void createOperate() {
		Context context = Util.getThemeContext(getActivity());
		AlertDialog.Builder ab = new AlertDialog.Builder(context);
		ab.setTitle(R.string.set_select);
		ab.setItems(new String[] { getString(R.string.create_club),
				getString(R.string.scan_add_friend) },
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if (!isMember()) {
							return;
						}
						switch (which) {
						case 0:
							startActivity(new Intent(getActivity(),
									CreateClubActivity.class));
							break;
						case 1:
							IntentFilter inf = new IntentFilter();
							inf.addAction(CLUB_SCAN_ACTION);
							getActivity().registerReceiver(reveiver, inf);
							startActivity(new Intent(getActivity(),
									DeviceBindScanActivity.class));
							break;
						}
					}
				});
		ab.setNegativeButton(R.string.can, null);
		ab.show();
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		minterface = (MainInterface) activity;
		super.onAttach(activity);
	}

	private OnClickListener lsr = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			createOperate();

		}
	};

	Handler handData = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub

			if (mSwipeLayout != null) {
				mSwipeLayout.setRefreshing(false);
			}
			switch (msg.what) {
			case 0:// load all club
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
				help.loadUrls(new String[] {
						"showAllClubs({" + "\"cistc\":" + msg.obj.toString()
								+ "})", "getClubContent()" });

				break;
			case 1:

				getClubJoinNum();
				help.loadUrls(new String[] { "showMyClubs({" + "\"cistc\":"
						+ msg.obj.toString() + "})" });
				break;
			case 2:
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
				help.loadUrls(new String[] { "appendClubHall({" + "\"cistc\":"
						+ msg.obj.toString() + "})" });
				break;
			case 3:
				help.loadUrls(new String[] { "appendMyClubs({" + "\"cistc\":"
						+ msg.obj.toString() + "})" });
				break;
			case 4:

				String re = msg.obj.toString();
				if (re.equals("0")) {
					// success

				} else if (re.equals("3")) {
					// wait check
					showMsg(getString(R.string.requestsended));

				} else if (re.equals("4")) {
					// alreday in
					showMsg(getString(R.string.alreadyin));

				} else {
					// failed
					handData.sendEmptyMessage(5);
				}

				break;
			case 5:
				showMsg(getString(R.string.error_network));
				break;
			case 6:
				isMember();
				break;
			case 7:
				// 获得请求数，显示审核入口
				help.loadUrls(new String[] { "displayJoinNum("
						+ (Integer) msg.obj + ")" });
				break;
			case 8:
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
				showMsg(getString(R.string.nomore));
				break;
			case 9:
				help.loadUrls(new String[] { "displayJoinNum(" + 0 + ")" });
				break;
			case 10:
				Toast.makeText(getActivity(), "俱乐部不对外开放，", Toast.LENGTH_SHORT)
						.show();
				break;
			}
			super.handleMessage(msg);
		}

	};

	public void showWarning() {
		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
		}
		if (d != null && d.isShowing()) {
			d.dismiss();
		}
		d = new NoRegisterDialog(getActivity(), R.string.no_register,
				R.string.no_register_content);
		d.show();
	}

	// 获得俱乐部请求列表
	public void getClubJoinNum() {
		// TODO Auto-generated method stub
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("session", session_id);
				try {
					String result = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/data/getMyClubVerifyMember", params);
					JSONObject js = new JSONObject(result);

					if (js.getInt("status") == 0) {
						joinList = js.getJSONArray("content");
						if (joinList.length() > 0) {
							handData.obtainMessage(7, joinList.length())
									.sendToTarget();
						} else {
							handData.obtainMessage(9, 0).sendToTarget();
						}
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void setCurrentTab(int index) {
		currentTab = index;
	}

	public boolean isMember() {
		if (ismember == 1)
			return true;
		else
			showWarning();
		return false;
	}

	public void getClub(int index) {
		currentTab = 0;
		if (FriendFragment.isNetworkConnected(getActivity())) {
			getFromNet(0, "", "");
		} else {
			getFromDb(0);
		}
	}

	public void getFromNet(final int index, final String area, final String name) {
		try {
			if (dialog == null || !dialog.isShowing()) {
				dialog = Util.initProgressDialog(getActivity(), true,
						getString(R.string.data_wait), null);

			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (area.length() < 1 && name.length() < 1) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Map<String, String> allClub = new HashMap<String, String>();
					allClub.put("session", session_id);
					allClub.put("index", (index * 10) + "");
					allClub.put("num", "10");
					try {
						String result = HttpUtlis.getRequest(HttpUtlis.BASE_URL
								+ "/data/getAllClub", allClub);
						JSONArray json = new JSONObject(result)
								.getJSONArray("content");
						if (json.length() <= 0) {
							handData.obtainMessage(8).sendToTarget();
						}
						for (int i = 0; i < json.length(); i++) {
							JSONObject jobj = json.getJSONObject(i);
							String lo = HttpUtlis.CLUBLOGO_URL
									+ HttpUtlis
											.getLogoUrl(jobj.getString("id"));

							db.UpdateClubInfo(jobj.getString("id"), jobj
									.getString("name"), jobj
									.getString("membernum"), jobj
									.getString("ispublic"), jobj
									.getString("provicne"), jobj
									.getString("city"), jobj
									.getString("district"),
									new SimpleDateFormat("yyyyMMddHHmmssSSS")
											.format(new Date()), lo);
							jobj.accumulate("logo", lo);
						}

						if (index > 0) {
							Message msg = new Message();
							msg.what = 2;
							msg.obj = json.toString();
							handData.sendMessage(msg);
						} else {
							Message msg = new Message();
							msg.what = 0;
							msg.obj = json.toString();
							handData.sendMessage(msg);
						}

					} catch (Exception e) {
						// TODO Auto-generated catch block
						// getFromDb(index);
						e.printStackTrace();
					}
				}
			}).start();
		} else {
			new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					Map<String, String> allClub = new HashMap<String, String>();
					allClub.put("session", session_id);

					if (area.length() > 1) {
						String[] args = area.split("_");
						allClub.put("province", args[0]);
						allClub.put("city", args[1]);
						allClub.put("district", args[2]);
					}
					if (name.length() > 0) {
						allClub.put("search", name);
					}
					try {
						String result = HttpUtlis.getRequest(HttpUtlis.BASE_URL
								+ "/data/searchClub", allClub);
						JSONArray json = new JSONObject(result)
								.getJSONArray("content");
						for (int i = 0; i < json.length(); i++) {
							JSONObject jobj = json.getJSONObject(i);
							jobj.accumulate(
									"logo",
									HttpUtlis.CLUBLOGO_URL
											+ HttpUtlis.getLogoUrl(jobj
													.getString("id")));
						}
						Message msg = new Message();
						msg.what = 0;
						msg.obj = json.toString();
						handData.sendMessage(msg);
					} catch (Exception e) {
						handData.sendEmptyMessage(5);
					}
				}
			}).start();
		}

	}

	// 请求我的俱乐部 name参数可无
	public void getMyClubs(final int page, final String name) {
		currentTab = 1;

		if (isMInit) {
			getMyClubFromDb(page);
			isMInit = false;
		}
		if (name.length() < 1) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Map<String, String> my = new HashMap<String, String>();
					my.put("session", session_id);
					try {
						String result = HttpUtlis.getRequest(HttpUtlis.BASE_URL
								+ "/data/getMyClub", my);
						JSONArray json = new JSONObject(result)
								.getJSONArray("content");
						if (mSwipeLayout != null) {
							mSwipeLayout.setRefreshing(false);
						}
						if (json.length() <= 0) {
							handData.obtainMessage(8).sendToTarget();
						}
						for (int i = 0; i < json.length(); i++) {
							JSONObject jobj = json.getJSONObject(i);
							db.UpdateMyClub(
									jobj.getString("id"),
									jobj.getString("name"),
									jobj.getString("membernum"),
									jobj.getString("ispublic"),
									jobj.getString("provicne"),
									jobj.getString("city"),
									jobj.getString("district"),
									HttpUtlis.CLUBLOGO_URL
											+ HttpUtlis.getLogoUrl(jobj
													.getString("id")));
						}

						getMyClubFromDb(page);

					} catch (Exception e) {
						// TODO Auto-generated catch block

						getMyClubFromDb(page);

						e.printStackTrace();
					}
				}

			}).start();
		} else {
			new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					ArrayList<HashMap<String, String>> alist = db
							.searchMyClub(name);
					JSONArray arr = new JSONArray();
					for (HashMap<String, String> map : alist) {
						JSONObject js = new JSONObject();
						try {
							js.accumulate("id", map.get(ClubInfo.CLUBID));
							js.accumulate("name", map.get(ClubInfo.NAME));
							js.accumulate("membernum",
									map.get(ClubInfo.MEMBERNUM));
							js.accumulate("ispublic",
									map.get(ClubInfo.ISPUBLIC));
							js.accumulate("provicne",
									map.get(ClubInfo.PROVINCE));
							js.accumulate("city", map.get(ClubInfo.CITY));

							js.accumulate("district",
									map.get(ClubInfo.DISTRICT));

							js.accumulate("logo", map.get(ClubInfo.LOGO));
							arr.put(js);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					Message msg = new Message();
					msg.what = 1;
					msg.obj = arr.toString();
					handData.sendMessage(msg);
				}
			}).start();
		}
	}

	// 从db获得我的俱乐部
	public void getMyClubFromDb(int page) {
		ArrayList<HashMap<String, String>> alist = db.getMyClubFromDb(page);
		JSONArray arr = new JSONArray();
		for (int i = 0; i < alist.size(); i++) {
			JSONObject js = new JSONObject();
			try {
				js.accumulate("id", alist.get(i).get(MyClub.CLUBID));
				js.accumulate("name", alist.get(i).get(MyClub.NAME));
				js.accumulate("membernum", alist.get(i).get(MyClub.MEMBERNUM));
				js.accumulate("ispublic", alist.get(i).get(MyClub.ISPUBLIC));
				js.accumulate("province", alist.get(i).get(MyClub.PROVINCE));
				js.accumulate("city", alist.get(i).get(MyClub.CITY));
				js.accumulate("district", alist.get(i).get(MyClub.DISTRICT));
				js.accumulate("logo", alist.get(i).get(MyClub.LOGO));
				arr.put(i, js);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		if (page < 1) {
			Message msg = new Message();
			msg.what = 1;
			msg.obj = arr.toString();
			handData.sendMessage(msg);
		} else {
			Message msg = new Message();
			msg.what = 3;
			msg.obj = arr.toString();
			handData.sendMessage(msg);
		}
	}

	public StringBuffer getFromAssets(String fileName) {
		try {
			InputStreamReader inputReader = new InputStreamReader(
					getResources().getAssets().open(fileName));
			BufferedReader bufReader = new BufferedReader(inputReader);
			String line = "";
			StringBuffer Result = new StringBuffer();
			while ((line = bufReader.readLine()) != null)
				Result.append(line);
			return Result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	JSONArray pro;
	JSONArray cityarr;

	public void showCityPicker(final String name, final int index) {
		final Context context = Util.getThemeContext(getActivity());
		View view = LayoutInflater.from(context).inflate(R.layout.citypicker,
				null);
		AlertDialog.Builder ab = new AlertDialog.Builder(context);
		final Spinner province = (Spinner) view.findViewById(R.id.province);
		final Spinner city = (Spinner) view.findViewById(R.id.city);
		final Spinner des = (Spinner) view.findViewById(R.id.des);
		String cityList = getFromAssets("sell/script/city.min.js").toString();
		ArrayList<String> proData = new ArrayList<String>();

		try {
			pro = new JSONObject(cityList).getJSONArray("citylist");
			for (int i = 0; i < pro.length(); i++) {
				proData.add(pro.getJSONObject(i).getString("p"));
			}
			province.setAdapter(new ArrayAdapter<String>(context,
					android.R.layout.simple_list_item_1, android.R.id.text1,
					proData));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (curPickerIndex[0] != 0) {
			province.setSelection(curPickerIndex[0]);
		} else {
			province.setSelection(1);
		}

		province.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				try {
					cityarr = pro.getJSONObject(position).getJSONArray("c");
					ArrayList<String> cityData = new ArrayList<String>();
					for (int i = 0; i < cityarr.length(); i++) {

						cityData.add(cityarr.getJSONObject(i).getString("n"));

					}
					city.setEnabled(true);
					city.setAdapter(new ArrayAdapter<String>(context,
							android.R.layout.simple_list_item_1,
							android.R.id.text1, cityData));
					if (curPickerIndex[1] != 0) {
						city.setSelection(curPickerIndex[1]);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					city.setEnabled(false);
					des.setEnabled(false);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}
		});
		city.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				try {
					JSONArray desarr = cityarr.getJSONObject(position)
							.getJSONArray("a");
					ArrayList<String> desData = new ArrayList<String>();
					for (int i = 0; i < desarr.length(); i++) {

						desData.add(desarr.getJSONObject(i).getString("s"));

					}
					des.setEnabled(true);
					des.setAdapter(new ArrayAdapter<String>(context,
							android.R.layout.simple_list_item_1,
							android.R.id.text1, desData));
					if (curPickerIndex[2] != 0) {
						des.setSelection(curPickerIndex[2]);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					des.setEnabled(false);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}
		});
		ab.setTitle(R.string.select);
		ab.setView(view);

		ab.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						curArgs = "";
						String[] args = new String[3];
						if (province.isEnabled()
								&& !province.getSelectedItem().toString()
										.equals("全部")) {
							args[0] = province.getSelectedItem().toString();

							curPickerIndex[0] = province
									.getSelectedItemPosition();
							if (city.isEnabled()
									&& city.getSelectedItem().toString()
											.length() > 0) {
								args[1] = city.getSelectedItem().toString();
								curPickerIndex[1] = city
										.getSelectedItemPosition();

								if (des.isEnabled()
										&& des.getSelectedItem().toString()
												.length() > 0) {
									args[2] = des.getSelectedItem().toString();

									curPickerIndex[2] = des
											.getSelectedItemPosition();
								}
							}
						}
						if (args[0] == null) {
							curArgs = "";
						} else {
							if (args[1] == null) {
								curArgs = args[0] + "_" + args[0] + "_"
										+ args[0];
							} else {
								if (args[2] == null) {
									curArgs = args[0] + "_" + args[0] + "_"
											+ args[1];
								} else {
									curArgs = args[0] + "_" + args[1] + "_"
											+ args[2];
								}
							}
						}
						getFromNet(0, curArgs, name);

					}
				});
		ab.setNegativeButton(R.string.can, null);
		ab.create().show();
	}

	int[] curPickerIndex;
	public String curArgs;

	// 从db获得俱乐部列表
	public void getFromDb(int index) {
		ArrayList<HashMap<String, String>> alist = db.getClubFromDb(index);
		if (alist == null || alist.size() < 1) {
			getFromNet(index, "", "");
			return;
		}
		JSONArray arr = new JSONArray();
		for (int i = 0; i < alist.size(); i++) {
			JSONObject js = new JSONObject();
			try {
				js.accumulate("id", alist.get(i).get(ClubInfo.CLUBID));
				js.accumulate("name", alist.get(i).get(ClubInfo.NAME));
				js.accumulate("membernum", alist.get(i).get(ClubInfo.MEMBERNUM));
				js.accumulate("ispublic", alist.get(i).get(ClubInfo.ISPUBLIC));
				js.accumulate("province", alist.get(i).get(ClubInfo.PROVINCE));
				js.accumulate("city", alist.get(i).get(ClubInfo.CITY));
				js.accumulate("district", alist.get(i).get(ClubInfo.DISTRICT));
				js.accumulate("page", alist.get(i).get(ClubInfo.INDEX));
				js.accumulate("logo", alist.get(i).get(ClubInfo.LOGO));
				arr.put(i, js);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		if (index > 0) {
			Message msg = new Message();
			msg.what = 2;
			msg.obj = arr.toString();
			handData.sendMessage(msg);
		} else {
			Message msg = new Message();
			msg.what = 0;
			msg.obj = arr.toString();
			handData.sendMessage(msg);
		}
		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
		}
		// getMyClubs("", true);
	}

	public void showMsg(String str) {
		Toast.makeText(getActivity(), str, Toast.LENGTH_SHORT).show();
	}

	/***
	 * 查看指定id的club信息
	 * 
	 * @param id
	 */
	public void toClubeDetail(String id, String isopen, String isMember) {

		if ("1".equals(isopen)) {
			if (isMember == null || isMember.length() <= 0) {
				// 不允许进
				handData.sendEmptyMessage(10);
				return;
			}
		}
		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
		}

		Intent i = new Intent(getActivity(), ClubDetailActivity.class);
		Bundle d = new Bundle();
		d.putString("id", id);
		i.putExtras(d);
		startActivity(i);
	}

	public void join(final String clubid, final String session_id) {
		if (!isMember()) {
			return;
		}
		try {
			if (dialog == null || !dialog.isShowing()) {
				dialog = Util.initProgressDialog(getActivity(), true,
						getString(R.string.data_wait), null);
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Map<String, String> map = new HashMap<String, String>();
					map.put("session", session_id);
					map.put("clubid", clubid);

					String resultJoin = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/data/joinClub", map);
					String re = new JSONObject(resultJoin).getString("status");
					if (dialog != null && dialog.isShowing()) {
						dialog.dismiss();
					}
					if (re.equals("0")) {
						// success
						db.UpdateMyClub(clubid, "", "", "", "", "", "", "");
						toClubeDetail(clubid, "0", "0");
					}
					Message msg = new Message();
					msg.obj = re;
					msg.what = 4;
					handData.sendMessage(msg);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					handData.sendEmptyMessage(5);
					e.printStackTrace();
				}
			}
		}).start();
	}

	@Override
	public void onRefresh() {
		curArgs = "";
		curPickerIndex = new int[3];
		// 清除缓存
		HttpUtlis.clearCacheFolder(getActivity().getCacheDir(),
				System.currentTimeMillis());
		help.clearCache();
		if (currentTab == 0) {
			try {
				if (dialog == null || !dialog.isShowing()) {
					dialog = Util.initProgressDialog(getActivity(), true,
							getString(R.string.data_wait), null);
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			clubjs.getFromNet(0, "", "");
		} else if (currentTab == 1) {
			clubjs.getMyClubs(0, "");
		}

	}

	public void toChargeClub() {
		// TODO Auto-generated method stub
		Intent i = new Intent(getActivity(), ChargeClubJoin.class);
		if (joinList != null && joinList.length() > 0) {
			i.putExtra("list", joinList.toString());
		}

		startActivity(i);
	}

}

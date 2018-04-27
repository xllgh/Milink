package com.bandlink.air;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.bandlink.air.club.BorderImageView;
import com.bandlink.air.pingan.PinganRegisterActivity;
import com.bandlink.air.pingan.PinganScorePanel;
import com.bandlink.air.util.AsynImagesLoader;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.MainInterface;
import com.bandlink.air.util.SharePreUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

@SuppressLint("NewApi")
public class MenuFragment extends Fragment implements onModeClick {
	private MainInterface mCallbacks;
	private View mView;
	private LinearLayout ach, fri, clu, rac, tip, set, fit;
	private LinearLayout[] linear;
	private RelativeLayout r1;// 顶部 头像 昵称 积分
	private BorderImageView user_photo;
	private TextView user_name, user_score;
	private View Lview;

	public SharedPreferences preferences;
	private Dbutils db;
	private AsynImagesLoader imageLoader;
	private int ismember;
	private int vCode = -1, aCode = -1;
	private SharedPreferences per;
	// 10-22 使用可展listview实现菜单
	private ListView listMenu;
	private SharePreUtils spUtil;

	public MenuFragment() {
		super();

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		MyLog.v("oncreate", "menu");
		super.onCreate(savedInstanceState);

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setHasOptionsMenu(true);
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		mCallbacks = (MainInterface) activity;
	}

	MenuSimpleAdapter md;
	private LinearLayout mode;
	ArrayList<HashMap<String, Object>> data;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		preferences = getActivity().getSharedPreferences(SharePreUtils.APP_ACTION, Context.MODE_MULTI_PROCESS);
		spUtil = SharePreUtils.getInstance(getActivity());
		mView = (View) inflater.inflate(R.layout.drawer2, container, false);

		imageLoader = AsynImagesLoader.getInstance(getActivity(), false);
		user_photo = (BorderImageView) mView.findViewById(R.id.user_photo);
		user_name = (TextView) mView.findViewById(R.id.user_name);
		mode = (LinearLayout) mView.findViewById(R.id.mode);
		// expand.setVisibility(View.GONE);
		mode.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mCallbacks != null) {
					mCallbacks.onNavigationDrawerImageClick(-4);
				}
			}
		});
		user_name.setText(preferences.getString("USERNAME", getString(R.string.tourist)));
		user_score = (TextView) mView.findViewById(R.id.user_score);
		if (preferences.getBoolean("ispa", false)) {
			user_score.setVisibility(View.VISIBLE);
			getPAScore(preferences.getString("vnumber", null));
		} else {
			user_score.setVisibility(View.GONE);
		}
		user_score.setText(String.format(getString(R.string.ping_), preferences.getString("SCORE", "0")));
		user_score.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(getActivity(), PinganScorePanel.class);
				startActivity(i);
			}
		});
		r1 = (RelativeLayout) mView.findViewById(R.id.r1);
		r1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mCallbacks.onNavigationDrawerImageClick(-1);
			}
		});
		listMenu = (ListView) mView.findViewById(R.id.lists);
		data = new ArrayList<HashMap<String, Object>>();
		md = new MenuSimpleAdapter(getActivity(), data, R.layout.menu_group, new String[] { "name", "icon" },
				new int[] { R.id.name, R.id.icon });
		listMenu.setAdapter(md);
		listMenu.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
		listMenu.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				// TextView text = (TextView)view.findViewById(R.id.name);
				mCallbacks.onNavigationDrawerImageClick((int) view.getTag());
			}
		});

		db = new Dbutils(preferences.getInt("UID", -1), getActivity());
		updateUserInfo();
		getLastestVersion();
		//
		if (db.getUserDeivceType() == 5 && Build.VERSION.SDK_INT >= 18) {
			per = getActivity().getSharedPreferences("air", Context.MODE_MULTI_PROCESS);
			String ver = per.getString("soft_version", null);
			if (ver != null) {
				getLastestVersion(ver);
			}
		}

		return mView;
	}

	public void getLastestVersion(final String ver) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				String result = HttpUtlis.queryStringForPost(HttpUtlis.UPDATE_AIR);

				try {
					result = new String(result.getBytes("iso8859-1"), "utf-8");
					JSONObject js = new JSONObject(result);
					String newVersion = null;
					String newVersionText = null;
					if (ver.toUpperCase().contains("AIRIII")) {
						newVersion = js.getString("verNameIII");
						newVersionText = js.getString("textIII");
					} else if (ver.toUpperCase().contains("AIRII")) {
						newVersion = js.getString("verNameII");
						newVersionText = js.getString("textII");
					} else if (ver.toUpperCase().contains("AIR")) {
						newVersion = js.getString("verName");
						newVersionText = js.getString("text");
					}
					if (HttpUtlis.checkAir(ver, newVersion) > 0) {
						hand.obtainMessage(1, newVersionText).sendToTarget();
					} else {
						hand.obtainMessage(2).sendToTarget();
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}).start();
	}

	Handler hand = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				// app有更新r.d
				try {
					JSONObject json = (JSONObject) msg.obj;
					PackageInfo info = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(),
							0);

					if (msg.arg1 > info.versionCode) {
						preferences.edit().putBoolean("appupdate", true).commit();
						preferences.edit().putString("appupdate_context", json.getString("text")).commit();
						preferences.edit().putString("appupdate_url", json.getString("url")).commit();
					} else {
						preferences.edit().putBoolean("appupdate", false).commit();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				break;
			case 1:
				// air有更新
				preferences.edit().putBoolean("airupdate", true).commit();
				preferences.edit().putString("airupdate_context", msg.obj.toString()).commit();
				break;
			case 2:
				// air最新
				preferences.edit().putBoolean("airupdate", false).commit();
				break;
			case 8:
				// 计算总分
				int score = db.getPinganScore();
				preferences.edit().putString("SCORE", score + "").commit();
				user_score.setText(String.format(getString(R.string.ping_), score) + "");
				break;
			}
		};
	};

	@Override
	public void onPause() {
		// TODO Auto-generated method stub

		super.onPause();
	}

	public void getLastestVersion() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					String result = HttpUtlis.queryStringForPost(HttpUtlis.UPDATE_URL);
					result = new String(result.getBytes("iso8859-1"), "utf-8");
					JSONArray j = new JSONArray(result);
					JSONObject json = j.getJSONObject(0);

					int code = json.getInt("verCode");

					hand.obtainMessage(0, code, 0, json).sendToTarget();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}).start();
	}

	public void updateUserInfo() {
		if (preferences.getString("USERNAME", null) != null || preferences.getString("PASSWORD", null) != null
				|| preferences.getInt("UID", -1) > 0) {
			db = new Dbutils(preferences.getInt("UID", -1), getActivity());
			Object[] profile = db.getUserProfile();
			if (profile != null) {
				user_name.setText((CharSequence) profile[2]);
				ismember = (Integer) profile[3];
				if (ismember > 0) {
					user_name.setText((CharSequence) profile[2]);
					ImageLoader.getInstance().displayImage(
							HttpUtlis.AVATAR_URL + getAvatarUrl(preferences.getInt("UID", -1) + "", "big"), user_photo,
							MilinkApplication.getListOptions());

				} else {
					user_name.setText(R.string.tourist);
					user_photo.setImageResource(R.drawable.avatar);
				}
			} else {
				user_name.setText(R.string.tourist);
				user_photo.setImageResource(R.drawable.avatar);
			}
		}
	}

	/**
	 * 加载根据配置侧菜单
	 * 
	 * @param curmode
	 *            体温模式、运动模式
	 * @param isKs
	 *            0 表示根据配置加载，1表示不是快刷，2表示是快刷
	 */
	void loadMenu(int curmode, int isKs) {
		data.clear();
		String[] temp = getResources().getStringArray(R.array.menu);
		int[] icons = { R.drawable.ic_home, R.drawable.clock2, R.drawable.shadow2, R.drawable.sleep_icon,
				R.drawable.ic_club, R.drawable.heartrate_dark, R.drawable.running, R.drawable.ic_card,
				R.drawable.ic_device, R.drawable.ic_setting };
		boolean isKuaiShua = false;
		if (isKs == 0) {
			isKuaiShua = preferences.getBoolean("isKs", false);
		} else if (isKs == 2) {
			isKuaiShua = true;
		} else {
			isKuaiShua = false;
		}

		for (int i = 0; i < temp.length; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("name", temp[i]);
			if (i < icons.length && !isKuaiShua && icons[i] == R.drawable.ic_card) {
				continue;
			}
			if (i < icons.length
					&& (icons[i] == R.drawable.ic_card || icons[i] == R.drawable.ic_club || icons[i] == R.drawable.race
							|| icons[i] == R.drawable.ic_device  )) {
				continue;
			}
			if (icons.length > i) {
				map.put("icon", icons[i]);
			}

			if (curmode != 3) {

				data.add(map);
			} else {

				if (i < icons.length && icons[i] != R.drawable.ic_device) {
					data.add(map);
				}

			}
		}
		md.notifyDataSetChanged();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		// updateUserInfo();
		loadMenu(spUtil.getMainMode(), 0);
		super.onResume();
	}

	public int cur_group_pos = 0;// 当前组的选中
	public int cur_child_pos = -1;

	class MenuSimpleAdapter extends SimpleAdapter {

		public MenuSimpleAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from,
				int[] to) {
			super(context, data, resource, from, to);
			// TODO Auto-generated constructor stub
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			HashMap<String, Object> map = (HashMap<String, Object>) getItem(position);

			return map.containsKey("icon") ? (int) map.get("icon") : -5;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			convertView = super.getView(position, convertView, parent);
			convertView.setTag((int) this.getItemId(position));
			if (position == 2 || position == 5) {
				View v = convertView.findViewById(R.id.line);
				if (v != null) {
					v.setVisibility(View.VISIBLE);
				}
			} else {
				View v = convertView.findViewById(R.id.line);
				if (v != null) {
					v.setVisibility(View.GONE);
				}
			}
			return convertView;
		}

	}

	public void getPAScore(final String number) {

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					// String url
					// =PinganRegisterActivity.PINGAN_BASE_URL+"/open/appsvr/health/partner/bind/device/0330000?access_token="+preferences.getString("pingan_token",
					// "");
					// String ur2 = "&partyNo=010387774232"
					// +"&partnerCode=0330000&partnerMemberNo=PA_13917607092" ;
					// //String res = HttpUtlis.doHttpPut(url+ur2,null);
					//
					// //System.out.println(res);
					// HashMap<String, String> params = new HashMap<String,
					// String>();
					// params.put("access_token",
					// preferences.getString("pingan_token", ""));
					// params.put("partyNo", "010387774232");
					// params.put("partnerCode", "0330000");
					// params.put("partnerMemberNo", "PA_13917607092");
					// String res = HttpUtlis
					// .getRequestForPost(PinganRegisterActivity.
					// PINGAN_BASE_URL
					// +
					// "/open/appsvr/health/partner/bind/account/0330000?access_token="
					// + preferences.getString(
					// "pingan_token",
					// ""), params);

					String result = HttpUtlis.getRequest(PinganRegisterActivity.PINGAN_BASE_URL
							+ "/open/appsvr/health/member/points/partner/0330000/?partyNo=" + number + "&access_token="
							+ preferences.getString("pingan_token", ""), null);
					if (result != null && result.contains("\"returnCode\":\"00\"")) {
						JSONObject obj;
						try {
							obj = new JSONObject(result);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							// e.printStackTrace();
							result = result.replace("\"data\":\"{\"", "\"data\":{\"");
							result = result.replace("\"}\"}", "\"}}");
							obj = new JSONObject(result);
						}
						JSONArray arr = obj.getJSONObject("data").getJSONArray("pointDetails");
						// "pointDate": "20140911",
						// "individualPoints": "0",
						// "rejectionReason": "有效运动未达到10000步的要求"
						for (int i = 0; i < arr.length(); i++) {
							JSONObject o = arr.getJSONObject(i);
							db.savePinganScore(o.getString("individualPoints"), o.getString("pointDate"),
									o.has("rejectionReason") ? o.getString("rejectionReason") : "");
						}
						hand.obtainMessage(8).sendToTarget();
					} else if (result != null && result.contains("access")) {
						String[] token = PinganRegisterActivity.getAccessToken();
						preferences.edit().putString("pingan_token", token[0]).commit();
						getPAScore(number);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	public static String getAvatarUrl(String uid, String s) {
		String size = s;
		int u = Math.abs(Integer.parseInt(uid));
		uid = String.format("%09d", u);
		String dir1 = uid.substring(0, 3);
		String dir2 = uid.substring(3, 5);
		String dir3 = uid.substring(5, 7);
		String typeadd = "";
		String url = dir1 + "/" + dir2 + "/" + dir3 + "/" + uid.substring(7) + typeadd + "_avatar_" + size + ".jpg";
		return url;
	}

	public class GroupViewHodler {
		TextView gname;
		ImageView gicon;
	}

	@Override
	public void onModeClick(int curmode) {
		// TODO Auto-generated method stub
		loadMenu(curmode, 0);
	}

}

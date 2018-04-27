package com.bandlink.air;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.DbContract.ClubGroup;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Util;

public class CreateClubActivity extends LovefitActivity implements
		OnCheckedChangeListener, OnClickListener {

	private Gallery gallery;
	private ImageView img;
	private String[] resIds;
	private RadioGroup logo_group, permission_group;
	private File sdcardTempFile;
	private EditText et_name, et_text, et_default;
	private LinearLayout group;
	private Button btn_save, btn_add;
	private HashMap<String, String> createMap;
	private SharedPreferences share;
	private int index = 1;
	private Spinner spinnerPro, spinnerCity, spinnerDis;
	private String cityList;
	private ArrayList<String> province = new ArrayList<String>();
	private ArrayList<String> cit; // 临时存放城市名称，在每次省份点击时刷新
	private ArrayList<String> dis;// 临时存放区名称，在每次城市点击时刷新
	private JSONArray city;
	private ProgressDialog dialog;
	private JSONObject jobj;// 俱乐部基本信息
	private String session_id;
	private Dbutils db;
	private int uid;
	private String clubid;
	private ArrayList<String> delIds = new ArrayList<String>();
	private ScrollView scrollview;
	private Context themeContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		setContentView(R.layout.createclub);
		try {
			resIds = getResources().getAssets().list("sell/images/clubmode");
		} catch (IOException e) {

			e.printStackTrace();
		}
		 
			themeContext = Util.getThemeContext(this); 
		scrollview = (ScrollView) findViewById(R.id.scrollview);
		share = getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		session_id = share.getString("session_id", "");
		uid = share.getInt("UID", -1);
		db = new Dbutils(uid, this);
		ActionbarSettings actionbar = new ActionbarSettings(this,
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						CreateClubActivity.this.finish();
					}
				}, null);
		// actionbar.setTopRightIcon(R.drawable.more);
		actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
		// actionbar.setTopRightIcon(R.drawable.ic_share);
		actionbar.setTitle(R.string.create_club);
		String basedata = getIntent().getStringExtra("base");
		clubid = getIntent().getStringExtra("clubid");
		if (basedata != null) {
			actionbar.setTitle(R.string.edit_club);
		}
		initViews(basedata);
		gallery.setAdapter(new ImageAdapter(this, resIds,
				"sell/images/clubmode/"));
		gallery.setSpacing(20);
		gallery.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				try {
					img.setBackgroundDrawable(BitmapDrawable
							.createFromStream(
									getResources().getAssets().open(
											"sell/images/clubmode/"
													+ resIds[position]),
									resIds[position]));
					index = position + 1;
				} catch (IOException e) {

					img.setBackgroundResource(R.drawable.no_media);
					e.printStackTrace();
				}
			}
		});
		logo_group.setOnCheckedChangeListener(this);
		super.onCreate(savedInstanceState);
	}

	/***
	 * 获得分组信息
	 * 
	 * @param freshnow
	 *            是否直接从网上获取数据
	 */
	private void getAllGroup(boolean freshnow) {
		ArrayList<HashMap<String, String>> group = db.getGroupInfo(clubid);

		if (group.size() > 0 && !freshnow) {
			Message msg = new Message();
			msg.obj = group;
			msg.what = 3;
			createHandler.sendMessage(msg);
		} else {
			new Thread(new Runnable() {
				@Override
				public void run() {

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
						createHandler.sendEmptyMessage(3);
						// getGroupFromDB(clubId);
					} catch (Exception e) {

						e.printStackTrace();
						// getGroupFromDB(clubId);
					}
				}
			}).start();
		}
	}

	/***
	 * 根据给定数据初始化控件，如果Data为空，默认为空的值
	 * 
	 * @param 给定数据格式是json的字符串形式
	 *            ，为俱乐部基本信息
	 */
	private void initViews(String data) {
		gallery = (Gallery) findViewById(R.id.gallery1);
		MarginLayoutParams mlp = (MarginLayoutParams) gallery.getLayoutParams();
		int offset = (getResources().getDisplayMetrics().widthPixels / 2);
		mlp.setMargins(-offset, mlp.topMargin, mlp.rightMargin,
				mlp.bottomMargin);
		img = (ImageView) findViewById(R.id.img_mode);
		logo_group = (RadioGroup) findViewById(R.id.logo_group);
		permission_group = (RadioGroup) findViewById(R.id.permission_group);
		et_name = (EditText) findViewById(R.id.et_clubname);
		et_text = (EditText) findViewById(R.id.et_clubtext);
		group = (LinearLayout) findViewById(R.id.linear_group);
		btn_add = (Button) findViewById(R.id.btn_add);
		btn_save = (Button) findViewById(R.id.btn_save);
		et_default = (EditText) findViewById(R.id.et_default);
		spinnerPro = (Spinner) findViewById(R.id.spinnerPro);

		spinnerCity = (Spinner) findViewById(R.id.spinnerCity);
		spinnerDis = (Spinner) findViewById(R.id.spinnerDis);
		try {
			img.setBackgroundDrawable(BitmapDrawable.createFromStream(
					getResources().getAssets().open(
							"sell/images/clubmode/" + resIds[0]), resIds[0]));
		} catch (IOException e) {

			img.setBackgroundResource(R.drawable.no_media);
			e.printStackTrace();
		}
		jobj = new JSONObject();
		if (data != null) {
			getAllGroup(true);
			try {
				jobj = new JSONObject(data);
				btn_save.setText(getString(R.string.save));
				et_name.setText(jobj.getString("name"));
				et_text.setText(jobj.getString("intro"));
				if (!jobj.getString("ispublic").equals("0")) {
					permission_group.check(R.id.radio_permission);
				}
				new Thread(new Runnable() {

					@Override
					public void run() {

						try {
							URL url = new URL(HttpUtlis.CLUBLOGO_URL
									+ HttpUtlis
											.getLogoUrl(jobj.getString("id"))
											.replace("small", "middle"));
							Drawable d = BitmapDrawable.createFromStream(
									url.openStream(), "");
							Message msg = new Message();
							msg.what = 1;
							msg.obj = d;
							createHandler.sendMessage(msg);
						} catch (MalformedURLException e) {

							e.printStackTrace();
						} catch (JSONException e) {

							e.printStackTrace();
						} catch (IOException e) {

						}

					}

				}).start();
			} catch (JSONException e) {

				e.printStackTrace();
			}

		}
		et_name.setOnClickListener(this);
		btn_add.setOnClickListener(this);
		btn_save.setOnClickListener(this);
		et_default.setOnClickListener(this);
		et_text.setOnClickListener(this);
		// 读取city.min.js城市信息，解析json实现省市区三级联动
		cityList = getFromAssets("sell/script/city.min.js").toString();
		try {
			final JSONArray pro = new JSONObject(cityList)
					.getJSONArray("citylist");
			for (int i = 0; i < pro.length(); i++) {
				province.add(pro.getJSONObject(i).getString("p"));
			}
			ArrayAdapter<String> a = new ArrayAdapter<String>(themeContext,
					R.layout.spinner_item, province);
			a.setDropDownViewResource(R.layout.spinner_list_city);
			spinnerPro.setAdapter(a);
			if (jobj.length() > 0) {
				spinnerPro.setSelection(province.indexOf(jobj
						.getString("provicne")));
			}

			spinnerPro.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view,
						int position, long id) {

					cit = new ArrayList<String>();
					city = new JSONArray();
					try {
						city = pro.getJSONObject(position).getJSONArray("c");
						for (int i = 0; i < city.length(); i++) {

							cit.add(city.getJSONObject(i).getString("n"));

						}
					} catch (JSONException e) {
						e.printStackTrace();
						cit.clear();
						cit.add(getString(R.string.without));

					}

					ArrayAdapter<String> c = new ArrayAdapter<String>(
							CreateClubActivity.this, R.layout.spinner_item, cit);
					c.setDropDownViewResource(R.layout.spinner_list_city);
					spinnerCity.setAdapter(c);
					try {
						if (jobj.length() > 0) {
							spinnerCity.setSelection(cit.indexOf(jobj
									.getString("city")));
						}
					} catch (Exception e) {
					}
					spinnerCity
							.setOnItemSelectedListener(new OnItemSelectedListener() {

								@Override
								public void onItemSelected(
										AdapterView<?> parent, View view,
										int position, long id) {

									dis = new ArrayList<String>();
									JSONArray disarr;
									try {
										disarr = city.getJSONObject(position)
												.getJSONArray("a");
										for (int i = 0; i < disarr.length(); i++) {
											dis.add(disarr.getJSONObject(i)
													.getString("s"));
										}

									} catch (JSONException e) {

										e.printStackTrace();
										e.printStackTrace();
										dis.clear();
										dis.add(getString(R.string.without));
									}
									ArrayAdapter<String> d = new ArrayAdapter<String>(
											CreateClubActivity.this,
											R.layout.spinner_item, dis);
									d.setDropDownViewResource(R.layout.spinner_list_city);
									spinnerDis.setAdapter(d);
									try {
										if (jobj.length() > 0) {
											spinnerDis.setSelection(dis.indexOf(jobj
													.getString("district")));
										}
									} catch (Exception e) {
									}

								}

								@Override
								public void onNothingSelected(
										AdapterView<?> parent) {

								}
							});
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {

				}
			});
		} catch (JSONException e1) {

			e1.printStackTrace();
		}

	}

	// loading
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

					}
				});

		return progressDialog;
	}

	private void showMsg(String str) {
		Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
	}

	public Handler createHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			if (dialog != null && dialog.isShowing()) {
				dialog.dismiss();
			}
			switch (msg.what) {
			case 0:
				if (jobj.length() > 0) {
					Intent i = new Intent(CreateClubActivity.this,
							ClubDetailActivity.class);
					Bundle d = new Bundle();
					d.putString("id", clubid);
					i.putExtras(d);
					startActivity(i);

					CreateClubActivity.this.finish();

				} else {
					String cid = "0";
					try {
						cid = ((JSONObject) msg.obj).getString("clubid");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					db.UpdateMyClub(cid, createMap.get("name"), "1",
							createMap.get("ispublic"),
							createMap.get("province"), createMap.get("city"),
							createMap.get("district"), HttpUtlis.CLUBLOGO_URL
									+ HttpUtlis.getLogoUrl(cid));
					System.out.println(db.isMyClub(cid));
					Intent i = new Intent(CreateClubActivity.this,
							CreateClubInvite.class);
					i.putExtra("content", ((JSONObject) msg.obj).toString());
					startActivity(i);
					CreateClubActivity.this.finish();
				}
				break;
			case 1:

				img.setBackgroundDrawable((Drawable) msg.obj);

				break;
			case 2:// connect sever faild
				showMsg(getString(R.string.error_network));
				break;
			case 4:
				showMsg(getString(R.string.err));
				break;
			case 3:// 俱乐部已有分组
				ArrayList<HashMap<String, String>> groupList = new ArrayList<HashMap<String, String>>();
				if (msg.obj != null) {
					groupList
							.addAll((ArrayList<HashMap<String, String>>) msg.obj);
				} else {
					groupList.addAll(db.getGroupInfo(clubid));
				}
				if (group.getChildCount() > 0) {
					group.removeAllViews();
				}

				for (HashMap<String, String> map : groupList) {
					if (map.equals(groupList.get(0))) {
						et_default.setTag(map.get(ClubGroup.GROUPID));
						et_default.setText(map.get(ClubGroup.NAME).toString());
						continue;
					}
					if (group.getChildCount() > 24) {
						btn_add.setTextColor(getResources().getColor(
								R.color.white_0_8));
						btn_add.setEnabled(false);
					} else {
						btn_add.setTextColor(getResources().getColor(
								R.color.white));
						btn_add.setEnabled(true);
					}

					View child = LayoutInflater.from(CreateClubActivity.this)
							.inflate(R.layout.sub_clubcreate_group, null);
					View edit = ((ViewGroup) child).getChildAt(0);
					if (edit instanceof EditText) {
						// 如果是默认分组

						((EditText) edit).setTag(map.get(ClubGroup.GROUPID));
						((EditText) edit).setText(map.get(ClubGroup.NAME)
								.toString());

					}
					group.addView(child);
					btnBinding(group);
				}
				break;
			}
			super.handleMessage(msg);
		}

	};

	// 创建/编辑俱乐部
	private void createClubNow(final String keyurl) {
		new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					String s = HttpUtlis.postClubCreate(HttpUtlis.BASE_URL
							+ keyurl + share.getString("session_id", ""),
							createMap,
							((BitmapDrawable) img.getBackground()).getBitmap());

					JSONObject result = new JSONObject(s);
					if (result.getString("status").equals("0")) {
						if (jobj.length() > 1) {
							createHandler.obtainMessage(0, clubid)
									.sendToTarget();
							return;
						}
						Message msg = new Message();
						msg.obj = result.getJSONObject("content");
						msg.what = 0;
						if (sdcardTempFile != null) {
							sdcardTempFile.deleteOnExit();
						}

						createHandler.sendMessage(msg);
					} else {

						createHandler.sendEmptyMessage(4);
					}

				} catch (Exception e) {

					createHandler.sendEmptyMessage(2);
					e.printStackTrace();
				}
			}
		}).start();
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

	/***
	 * 递归绑定ViewGroup内的Button事件
	 * 
	 * @param ViewGroup
	 */
	private void btnBinding(ViewGroup vg) {
		for (int i = 0; i < vg.getChildCount(); i++) {
			if (vg.getChildAt(i) instanceof LinearLayout) {
				btnBinding((LinearLayout) vg.getChildAt(i));
			} else if (vg.getChildAt(i) instanceof Button) {
				((Button) vg.getChildAt(i))
						.setOnClickListener(CreateClubActivity.this);
			}
		}
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.btn_add:
			if (group.getChildCount() > 18) {
				btn_add.setTextColor(getResources().getColor(R.color.white_0_8));
				break;
			}
			btn_add.setTextColor(getResources().getColor(R.color.white));
			btn_add.setEnabled(true);
			View child = LayoutInflater.from(CreateClubActivity.this).inflate(
					R.layout.sub_clubcreate_group, null);
			group.addView(child);
			btnBinding(group);
			break;
		case R.id.btn_save:

			createMap = new HashMap<String, String>();

			if (checkClubName(et_name.getText().toString(), 36)) {
				createMap.put("name", et_name.getText().toString());
			} else {
				et_name.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.corner_border_red));
				et_name.requestFocusFromTouch();
				scrollview.scrollTo(0, 0);
				break;
			}
			if (spinnerCity.getSelectedItem().toString().length() < 1
					|| spinnerCity.getSelectedItem().toString().equals("无")) {
				createMap.put("province", spinnerPro.getSelectedItem()
						.toString());
				createMap.put("city", spinnerPro.getSelectedItem().toString());
				createMap.put("district", spinnerPro.getSelectedItem()
						.toString());
			} else if (spinnerDis.getSelectedItem().toString().length() < 1
					|| spinnerDis.getSelectedItem().toString().equals("无")) {
				createMap.put("province", spinnerPro.getSelectedItem()
						.toString());
				createMap.put("city", spinnerPro.getSelectedItem().toString());
				createMap.put("district", spinnerCity.getSelectedItem()
						.toString());
			} else {
				createMap.put("province", spinnerPro.getSelectedItem()
						.toString());
				createMap.put("city", spinnerCity.getSelectedItem().toString());
				createMap.put("district", spinnerDis.getSelectedItem()
						.toString());
			}
			if (checkClubName(et_text.getText().toString(), 90)) {
				createMap.put("intro", et_text.getText().toString());
			} else {
				et_text.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.corner_border_red));
				et_text.requestFocusFromTouch();
				break;
			}
			JSONArray arr = new JSONArray();
			String defaultname = et_default.getText().toString();

			// 如果默认分组空
			if (defaultname.length() < 1) {
				defaultname = getString(R.string.default_group);
				et_default.setText(defaultname);
				Toast.makeText(this, getString(R.string.update_defgroup),
						Toast.LENGTH_SHORT).show();
				break;
			}
			JSONObject j = new JSONObject();
			try {
				j.accumulate("name", et_default.getText().toString());
				if (et_default.getTag() != null) {
					j.accumulate("gid", et_default.getTag().toString());
				}

			} catch (JSONException e) {

				e.printStackTrace();
			}
			arr.put(j);

			getValue(group, arr);
			if (arr.length() > 0) {
				createMap.put("group", arr.toString());
			}
			if (permission_group.getCheckedRadioButtonId() == R.id.radio_permission) {
				createMap.put("ispublic", "1");
			}else if(permission_group.getCheckedRadioButtonId() == R.id.radio_permission_noaccess){
				createMap.put("ispublic", "2");
			} else {
				createMap.put("ispublic", "0");
			}
			if (jobj.length() > 0) {
				// 更新俱乐部
				String gids = "";
				if (delIds.size() > 0) {
					db.deleteGroup(clubid);
				}
				for (String s : delIds) {
					gids += s + ",";
				}
				try {
					gids = gids.substring(0, gids.length() - 1);
					createMap.put("delgids", gids);
				} catch (Exception e) {

				}
				createClubNow("/data/editClub/clubid/" + clubid + "/session/");
			} else {
				createClubNow("/data/createClub/session/");
			}

			dialog = initProgressDialog();
			dialog.show();
			break;
		case R.id.et_clubname:
			et_name.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.edit_border_selector));
			break;
		case R.id.et_clubtext:
			et_text.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.edit_border_selector));
			break;
		default:

			try {
				group.removeViewAt(group.indexOfChild((View) v.getParent()));
				if (jobj.length() > 1) {
					Object tag = ((ViewGroup) v.getParent()).getChildAt(0)
							.getTag();
					if (tag != null && !delIds.contains(tag.toString())) {
						delIds.add(tag.toString());
					}
				}
			} catch (Exception e) {

			}

			if (group.getChildCount() <= 8) {
				btn_add.setTextColor(getResources().getColor(R.color.white));
			}
			break;

		}
	}

	/***
	 * 获得ViewGroup内所有非空Edittext值
	 * 
	 * @param vg
	 *            ViewGroup
	 * @param arr
	 *            存到JSONArray
	 * @return 返回JSONArray
	 */
	private JSONArray getValue(ViewGroup vg, JSONArray arr) {
		for (int i = 0; i < vg.getChildCount(); i++) {
			if (vg.getChildAt(i) instanceof LinearLayout) {
				getValue((LinearLayout) vg.getChildAt(i), arr);
			} else if (vg.getChildAt(i) instanceof EditText) {
				if (((EditText) vg.getChildAt(i)).getText().toString().length() > 0) {
					JSONObject j = new JSONObject();
					try {
						j.accumulate("name", ((EditText) vg.getChildAt(i))
								.getText().toString());
						Object tag = ((EditText) vg.getChildAt(i)).getTag();
						if (tag != null) {
							j.accumulate("gid", tag);
						}

					} catch (JSONException e) {

						e.printStackTrace();
					}
					arr.put(j);
				}
			}
		}
		return arr;
	}

	/***
	 * 判断输入字符串长度是否大于指定长度，一个汉字算两个字符
	 * 
	 * @param name
	 *            字符串
	 * @param length
	 *            指定长度
	 * @return 小于等于指定长度为true
	 */
	public static boolean checkClubName(String name, int length) {
		name = name.trim();
		if (name.contains("&") && name.contains(";")) {
			return false;
		}
		if (name.length() < 1) {
			return false;
		}
		int len = 0;
		for (int i = 0; i < name.length(); i++) {
			if (isChinese(name.charAt(i))) {
				len += 3;
			} else {
				len++;
			}
			if (len > length) {
				return false;
			}
		}
		return true;

	}

	/***
	 * 判断是否是汉字
	 * 
	 * @param c
	 *            字符
	 * @return 是true
	 */
	public static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}
		return false;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
		case 1:
			startPhotoZoom(Uri.fromFile(sdcardTempFile));
			break;
		case 2:
			if (data != null)
				startPhotoZoom(data.getData());
			break;
		case 3:
			System.out.println(sdcardTempFile.getPath());
			if (data == null) {
				break;
			}
			Bundle bundle = data.getExtras();
			if (bundle != null) {
				Bitmap p = bundle.getParcelable("data");
				if (p != null) {
					img.setBackgroundDrawable(new BitmapDrawable(p));
				} else {
					if (!sdcardTempFile.exists()) {
						FileOutputStream out;
						try {
							out = new FileOutputStream(sdcardTempFile);
							p.compress(Bitmap.CompressFormat.JPEG, 100, out);
							out.flush();
							out.close();
							img.setBackgroundDrawable(new BitmapDrawable(p));
						} catch (Exception e) {
							e.printStackTrace();
							img.setBackgroundDrawable(BitmapDrawable
									.createFromPath(sdcardTempFile.getPath()));
						}

					}
				}
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void startPhotoZoom(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 3);
		intent.putExtra("aspectY", 3);
		intent.putExtra("outputX", 300);
		intent.putExtra("outputY", 300);
		intent.putExtra("return-data", true);
		intent.putExtra("scale", true);
		intent.putExtra("scaleUpIfNeeded", true);
		startActivityForResult(intent, 3);
	}

	public void getPhoto() {
		File f = new File(Environment.getExternalStorageDirectory().getPath()
				+ HttpUtlis.TEMP_Folder);
		if (!f.exists()) {
			f.mkdirs();
		}
		sdcardTempFile = new File(Environment.getExternalStorageDirectory()
				.getPath() + HttpUtlis.TEMP_Folder, "tmp_pic_"
				+ SystemClock.currentThreadTimeMillis() + ".jpg");
		final CharSequence[] items = { getString(R.string.chose_image),
				getString(R.string.chose_photo) };
		AlertDialog dlg = new AlertDialog.Builder(themeContext)
				.setTitle(getString(R.string.pic))
				.setItems(items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						if (item == 1) {
							Intent intent = new Intent(
									MediaStore.ACTION_IMAGE_CAPTURE);
							intent.putExtra(MediaStore.EXTRA_OUTPUT,
									Uri.fromFile(sdcardTempFile));
							startActivityForResult(intent, 1);

						} else {
							Intent intent = new Intent(Intent.ACTION_PICK, null);
							intent.setDataAndType(
									MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
									"image/*");
							startActivityForResult(intent, 2);
						}
					}
				}).create();
		dlg.show();
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {

		switch (checkedId) {
		case R.id.radio_img:
			gallery.setVisibility(View.GONE);
			getPhoto();
			break;
		case R.id.radio_mode:
			gallery.setVisibility(View.VISIBLE);
			break;
		case R.id.radio_nopermission:
			break;
		case R.id.radio_permission:
			break;
		}
	}

	// 加载模版的
	public static class ImageAdapter extends BaseAdapter {
		private Context mContext;
		String[] resIds;
		String path;

		public ImageAdapter(Context context, String[] ids, String path) {
			mContext = context;
			this.path = path;
			this.resIds = ids;
		}

		// 返回图像总数
		public int getCount() {
			return resIds.length;
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		// 返回具体位置的ImageView对象
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView = new ImageView(mContext);
			// 设置当前图像的图像（position为当前图像列表的位置）
			try {
				BitmapFactory.Options o = new BitmapFactory.Options();
				o.inSampleSize = 2;
				imageView.setImageBitmap(BitmapFactory.decodeStream(
						mContext.getResources().getAssets()
								.open(path + resIds[position]), null, o));

			} catch (IOException e) {

				imageView.setImageResource(R.drawable.no_media);
				e.printStackTrace();
			}
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			// 设置Gallery组件的背景风格
			imageView.setLayoutParams(new Gallery.LayoutParams(250, 160));
			return imageView;
		}
	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub
		
	}

}

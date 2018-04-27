package com.bandlink.air.club;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bandlink.air.MilinkApplication;
import com.bandlink.air.R;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Util;
import com.bandlink.air.view.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ChargeClubJoin extends LovefitActivity implements
		OnRefreshListener {
	private ListView example_lv_list;
	private SharedPreferences share;
	private int uid;
	private String arr;
	private String session_id;
	ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
	SListAdaper sl;
	private Context mContext;
	private Dbutils db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_chargejoin);
		share = getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		db = new Dbutils(share.getInt("UID", -1), this);
		mContext = Util.getThemeContext(this);
		ActionbarSettings actionbar = new ActionbarSettings(this,
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						ChargeClubJoin.this.finish();
					}
				}, new OnClickListener() {

					@Override
					public void onClick(View v) {
						// 批量同意
						AlertDialog.Builder ab = new AlertDialog.Builder(
								mContext);
						ab.setTitle(R.string.pleaseselect);

						ab.setAdapter(new ArrayAdapter<String>(mContext,
								R.layout.textview, new String[] {
										getString(R.string.mulit_refuse),
										getString(R.string.mulit_ok) }),
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										if (which == 0) {
											JSONArray arr = new JSONArray();

											try {
												// /data/batchIgnoreClubMembers/session
												ArrayList<HashMap<String, String>> arrs = new ArrayList<HashMap<String, String>>();
												for (HashMap<String, String> map : list) {
													if (map.get("selected")
															.equals("1")) {
														JSONObject j = new JSONObject();
														j.put("uid",
																map.get("uid"));
														j.put("clubid", map
																.get("clubid"));
														arr.put(j);
														arrs.add(map);
													}
												}
												if (arrs.size() < 1) {
													Toast.makeText(
															mContext,
															getString(R.string.havenot_select),
															Toast.LENGTH_SHORT)
															.show();

													return;
												}
												permissionAccess(
														"/data/batchIgnoreClubMembers/session/",
														arr, arrs);
											} catch (JSONException e) {
												// TODO Auto-generated catch
												// block
												e.printStackTrace();
											}
										} else {
											JSONArray arr = new JSONArray();

											try {

												ArrayList<HashMap<String, String>> arrs = new ArrayList<HashMap<String, String>>();
												for (HashMap<String, String> map : list) {
													if (map.get("selected")
															.equals("1")) {
														JSONObject j = new JSONObject();
														j.put("uid",
																map.get("uid"));
														j.put("clubid", map
																.get("clubid"));
														arr.put(j);
														arrs.add(map);
													}
												}
												if (arrs.size() < 1) {
													Toast.makeText(
															mContext,
															getString(R.string.havenot_select),
															Toast.LENGTH_SHORT)
															.show();

													return;
												}
												permissionAccess(
														"/data/approvedClub/session/",
														arr, arrs);
											} catch (JSONException e) {
												// TODO Auto-generated catch
												// block
												e.printStackTrace();
											}
										}
										dialog.dismiss();
									}
								});

						ab.create().show();

					}
				});
		actionbar.setTopRightIcon(R.drawable.complete);
		actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
		actionbar.setTitle(R.string.chargeclub);

		example_lv_list = (ListView) findViewById(R.id.example_lv_list);
		arr = getIntent().getStringExtra("list");

		if (arr == null) {
			getJoinList();
		} else {
			chargeHandler.obtainMessage(0, arr).sendToTarget();
		}
	}

	Handler chargeHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if(dialog!=null && dialog.isShowing()){
				dialog.dismiss();
			}
			switch (msg.what) {
			case 0:
				try {
					list.clear();
					JSONArray array = new JSONArray(msg.obj.toString());
					for (int i = 0; i < array.length(); i++) {
						HashMap<String, String> map = new HashMap<String, String>();
						map.put("name",
								array.getJSONObject(i).getString("nickname"));
						map.put("uid", array.getJSONObject(i).getString("uid"));
						map.put("photo",
								HttpUtlis.AVATAR_URL
										+ HttpUtlis.getAvatarUrl(array
												.getJSONObject(i).getString(
														"uid")));

						map.put("selected", "0");
						map.put("clubid",
								array.getJSONObject(i).getString("clubid"));
						String name = db.getMyClubNameById(array.getJSONObject(
								i).getString("clubid"));
						String text = getString(R.string.comefrom)
								+ getString(R.string.ab_club);
						if (name != null) {
							text = getString(R.string.comefrom) + "[" + name
									+ "]" + getString(R.string.ab_club);
						}
						map.put("text", text);
						list.add(map);
					}
					sl = new SListAdaper(ChargeClubJoin.this, list);
					example_lv_list.setAdapter(sl);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case 1: 
				sl.notifyDataSetChanged();
				Toast.makeText(ChargeClubJoin.this, msg.obj.toString(),
						Toast.LENGTH_SHORT).show();
				break;
			case 2:
				try {
					showGroup(((JSONObject) msg.obj).getJSONArray("content"),
							msg.arg1, msg.arg2);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
			super.handleMessage(msg);
		}

	};

	void showGroup(final JSONArray arr, final int uid, final int cid)
			throws JSONException {

		String[] g = new String[arr.length()];
		for (int i = 0; i < arr.length(); i++) {
			g[i] = arr.getJSONObject(i).getString("groupname");

		}
		AlertDialog.Builder ab = new AlertDialog.Builder(
				Util.getThemeContext(this));
		ab.setTitle(R.string.moveto);
		ab.setItems(g, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, final int which) {
				// TODO Auto-generated method stub
				new Thread(new Runnable() {
					public void run() {
						try {
							HashMap<String, String> map = new HashMap<String, String>();
							map.put("clubid", cid + "");
							map.put("uids", uid + "");
							map.put("groupid", arr.getJSONObject(which)
									.getString("id"));
							map.put("session", session_id);
							String res = HttpUtlis.getRequest(
									HttpUtlis.BASE_URL + "/data/transferGroup",
									map);
							JSONObject obj = new JSONObject(res);
							if (obj.getInt("status") == 0) {
								chargeHandler.obtainMessage(1,
										getString(R.string.finish))
										.sendToTarget();
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}).start();
			}
		});
		ab.setNegativeButton(R.string.can, null);
		ab.create().show();
	}
ProgressDialog dialog;
	private void permissionAccess(final String url, final JSONArray str,
			final ArrayList<HashMap<String, String>> position) {
		dialog = Util.initProgressDialog(Util.getThemeContext(this), true, getString(R.string.data_wait), null);
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					ArrayList<NameValuePair> as = new ArrayList<NameValuePair>();
					BasicNameValuePair b = new BasicNameValuePair("data", str
							.toString());
					as.add(b);

					String result = HttpUtlis.postData(HttpUtlis.BASE_URL + url
							+ session_id, str.toString());

					JSONObject js = new JSONObject(result);
					if (js.getInt("status") == 0) {
						// ok
						if (str.length() == 1) {
							HashMap<String, String> map = new HashMap<String, String>();
							map.put("session", session_id);
							map.put("clubid",
									str.getJSONObject(0).getString("clubid"));
							String group = HttpUtlis.getRequest(
									HttpUtlis.BASE_URL + "/data/getClubGroup",
									map);
							try {
								list.removeAll(position);
								JSONObject obj = new JSONObject(group);
								if (obj.getInt("status") == 0) {
									chargeHandler.obtainMessage(
											2,
											Integer.valueOf(str
													.getJSONObject(0)
													.getString("uid")),
											Integer.valueOf(str
													.getJSONObject(0)
													.getString("clubid")), obj)
											.sendToTarget();
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} else {
							list.removeAll(position);

							chargeHandler.obtainMessage(1,
									getString(R.string.finish)).sendToTarget();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void getJoinList() {
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
						JSONArray joinList = js.getJSONArray("content");
						if (joinList.length() > 0) {
							chargeHandler.obtainMessage(0, joinList.toString())
									.sendToTarget();
						}
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		uid = share.getInt("UID", -1);
		session_id = share.getString("session_id", "-1");
		super.onResume();
	}

	class SListAdaper extends BaseAdapter {
		private ArrayList<HashMap<String, String>> data;
		private Context context;

		public SListAdaper(Context context,
				ArrayList<HashMap<String, String>> data) {
			this.context = context;
			this.data = data;
		}

		@Override
		public int getCount() {
			return data.size();
		}

		@Override
		public HashMap<String, String> getItem(int position) {
			return data.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			final HashMap<String, String> item = getItem(position);
			final ViewHolder holder;
			if (convertView == null) {
				LayoutInflater li = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = li.inflate(R.layout.item_ac_chargejoin, parent,
						false);
				holder = new ViewHolder();

				holder.bAction1 = (Button) convertView
						.findViewById(R.id.example_row_b_action_1);
				holder.bAction2 = (Button) convertView
						.findViewById(R.id.example_row_b_action_2);
				holder.ivImage = (RoundedImageView) convertView
						.findViewById(R.id.example_row_iv_image);
				holder.tvTitle = (TextView) convertView
						.findViewById(R.id.example_row_tv_title);
				holder.msgText = (TextView) convertView
						.findViewById(R.id.example_row_tv_text);
				holder.check = (CheckBox) convertView
						.findViewById(R.id.checkbox);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			ImageLoader.getInstance().displayImage(
					item.get("photo").replace("small", "middle"),
					holder.ivImage,MilinkApplication.getListOptions());
			holder.tvTitle.setText(data.get(position).get("name"));
			holder.msgText.setText(data.get(position).get("text"));
			holder.check.setChecked(data.get(position).get("selected")
					.equals("1"));
			holder.check
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton arg0,
								boolean arg1) {
							// TODO Auto-generated method stub
							if (arg1) {
								data.get(position).put("selected", "1");
							} else {
								data.get(position).put("selected", "0");
							}
							System.out.println(data);
						}
					});
			holder.bAction1.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					JSONArray arr = new JSONArray();
					JSONObject j = new JSONObject();
					try {
						// /data/batchIgnoreClubMembers/session
						j.put("uid", data.get(position).get("uid"));
						j.put("clubid", data.get(position).get("clubid"));
						arr.put(j);
						ArrayList<HashMap<String, String>> arrs = new ArrayList<HashMap<String, String>>();
						arrs.add(data.get(position));
						permissionAccess(
								"/data/batchIgnoreClubMembers/session/", arr,
								arrs);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});

			holder.bAction2.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {

					JSONArray arr = new JSONArray();
					JSONObject j = new JSONObject();
					try {
						// /data/batchIgnoreClubMembers/session
						ArrayList<HashMap<String, String>> arrs = new ArrayList<HashMap<String, String>>();
						arrs.add(data.get(position));
						j.put("uid", data.get(position).get("uid"));
						j.put("clubid", data.get(position).get("clubid"));
						arr.put(j);
						permissionAccess("/data/approvedClub/session/", arr,
								arrs);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					showOp(position, data);
				}
			});
			return convertView;
		}

	}

	void showOp(final int position,
			final ArrayList<HashMap<String, String>> data) {
		String[] items = { getString(R.string.agree_club),
				getString(R.string.ignore_club), getString(R.string.selectall),
				getString(R.string.deselectall) };
		AlertDialog.Builder ab = new AlertDialog.Builder(
				Util.getThemeContext(this));
		ab.setItems(items, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				switch (which) {
				case 0:
					JSONArray arr = new JSONArray();
					JSONObject j = new JSONObject();
					try {
						// /data/batchIgnoreClubMembers/session
						ArrayList<HashMap<String, String>> arrs = new ArrayList<HashMap<String, String>>();
						arrs.add(data.get(position));
						j.put("uid", data.get(position).get("uid"));
						j.put("clubid", data.get(position).get("clubid"));
						arr.put(j);
						permissionAccess("/data/approvedClub/session/", arr,
								arrs);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case 1:
					JSONArray arr2 = new JSONArray();
					JSONObject j2 = new JSONObject();
					try {
						// /data/batchIgnoreClubMembers/session
						j2.put("uid", data.get(position).get("uid"));
						j2.put("clubid", data.get(position).get("clubid"));
						arr2.put(j2);
						ArrayList<HashMap<String, String>> arrs = new ArrayList<HashMap<String, String>>();
						arrs.add(data.get(position));
						permissionAccess(
								"/data/batchIgnoreClubMembers/session/", arr2,
								arrs);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case 2:
					for (HashMap<String, String> map : data) {
						map.put("selected", "1");
					}
					sl.notifyDataSetChanged();
					break;
				case 3:
					for (HashMap<String, String> map : data) {
						if (map.get("selected").equals("0")) {
							map.put("selected", "1");
						} else {
							map.put("selected", "0");
						}
					}
					sl.notifyDataSetChanged();
					break;
				}
			}
		});
		ab.create().show();
	}

	static class ViewHolder {
		RoundedImageView ivImage;
		TextView tvTitle;
		TextView msgText;
		Button bAction1;
		Button bAction2;
		CheckBox check;
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub

	}
}

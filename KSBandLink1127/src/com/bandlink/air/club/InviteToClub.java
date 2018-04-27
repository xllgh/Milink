package com.bandlink.air.club;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bandlink.air.R;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.view.RoundedImageView;

public class InviteToClub extends LovefitActivity {
	private SharedPreferences share;
	private String session_id;
	private ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
	private Dbutils db;
	private String clubid;
	private ListView example_lv_list;
	private SListAdaper sl;
	private ProgressDialog progress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_chargejoin);
		share = getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		session_id = share.getString("session_id", "-1");
		clubid = getIntent().getStringExtra("clubid");
		example_lv_list = (ListView) findViewById(R.id.example_lv_list);
//		example_lv_list
//				.setOffsetLeft(getResources().getDisplayMetrics().widthPixels -getResources().getDimension(R.dimen.icon_large_x)-30);
		ActionbarSettings actionbar = new ActionbarSettings(this,
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						// 左
						InviteToClub.this.finish();
					}
				}, new OnClickListener() {

					@Override
					public void onClick(View v) {
						// 右

					}
				});
		actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
		actionbar.setTopRightIcon(R.drawable.complete);
		actionbar.setTitle(R.string.invitetoclub);
	}

	@Override
	protected void onResume() {
		db = new Dbutils(share.getInt("UID", -1), this);
		getFriendList();
		super.onResume();
	}

	private ArrayList<String> getClubMember() {
		ArrayList<HashMap<String, String>> mList = db.getMemberInfo(clubid,
				null,0);
		ArrayList<String> ids = new ArrayList<String>();
		for (int i = 0; i < mList.size(); i++) {
			ids.add(mList.get(i).get("userid"));
		}
		return ids;
	}

	private void getFriendList() {
		progress = initProgressDialog();
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("session", session_id);
				try {
					String result = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/user/getUserFriend", map);
					JSONObject js = new JSONObject(result);

					if (js.getInt("status") == 0) {
						JSONArray joinList = js.getJSONArray("content");
						if (joinList.length() > 0) {
							handler.obtainMessage(0, joinList).sendToTarget();
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void inviteFriend(final String url, final JSONArray str,
			final ArrayList<HashMap<String, String>> position) {
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

						list.removeAll(position);

						handler.obtainMessage(1, getString(R.string.finish))
								.sendToTarget();

					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public ProgressDialog initProgressDialog() {
		final ProgressDialog progressDialog = ProgressDialog.show(this,
				getString(R.string.data_wait),
				getString(R.string.data_getting), true);
		// 7秒超时后关闭loading
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {

				if (progressDialog != null && progressDialog.isShowing()) {
					try {
						progressDialog.dismiss();
					} catch (Exception e) {

					}
				}
			}
		}, 7000);
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

						dialog.dismiss();
						dialog = null;
					}
				});

		return progressDialog;
	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (progress != null && progress.isShowing()) {
				progress.dismiss();
			}
			switch (msg.what) {
			case 0:
				ArrayList<String> mArray = getClubMember();
				try {
					list.clear();
					JSONArray array = new JSONArray(msg.obj.toString());
					for (int i = 0; i < array.length(); i++) {
						if (mArray.contains(array.getJSONObject(i).getString(
								"fuid"))) {
							continue;
						}
						HashMap<String, String> map = new HashMap<String, String>();
						map.put("name",
								array.getJSONObject(i).getString("nickname"));
						map.put("uid", array.getJSONObject(i).getString("fuid"));
						map.put("photo",
								HttpUtlis.AVATAR_URL
										+ HttpUtlis.getAvatarUrl(array
												.getJSONObject(i).getString(
														"fuid")));

						map.put("selected", "0");

						list.add(map);
					}
					sl = new SListAdaper(InviteToClub.this, list);
					example_lv_list.setAdapter(sl);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case 1:

				sl.notifyDataSetChanged();
				Toast.makeText(InviteToClub.this, msg.obj.toString(),
						Toast.LENGTH_SHORT).show();
				break;
			}
			super.handleMessage(msg);
		}

	};

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
			holder.bAction1.setVisibility(View.GONE);
			holder.bAction2.setText(R.string.invite_btn);
			((SwipeListView) parent).recycle(convertView, position);
			final Handler han = new Handler() {

				@Override
				public void handleMessage(Message msg) {
					// TODO Auto-generated method stub

					if (msg.obj != null) {
						Bitmap bit = (Bitmap) msg.obj;
						holder.ivImage.setImageBitmap(bit);
					} else {
						holder.ivImage.setImageResource(R.drawable.avatar);
					}
					super.handleMessage(msg);
				}

			};
			new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						URL url = new URL(item.get("photo").replace("small",
								"middle"));

						Bitmap bit = BitmapFactory.decodeStream(url
								.openStream());
						han.obtainMessage(0, bit).sendToTarget();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						han.obtainMessage(0, null).sendToTarget();
						e.printStackTrace();

					}
				}
			}).start();
			holder.tvTitle.setText(data.get(position).get("name"));
			holder.msgText.setText(data.get(position).get("text"));
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
			// holder.bAction1.setOnClickListener(new View.OnClickListener() {
			// @Override
			// public void onClick(View v) {
			// JSONArray arr = new JSONArray();
			// JSONObject j = new JSONObject();
			// try {
			// // /data/batchIgnoreClubMembers/session
			// j.put("uid", data.get(position).get("uid"));
			// j.put("clubid", data.get(position).get("clubid"));
			// arr.put(j);
			// ArrayList<HashMap<String, String>> arrs = new
			// ArrayList<HashMap<String, String>>();
			// arrs.add(data.get(position));
			// inviteFriend(
			// "/XXXXXXXXXXX/session/", arr,
			// arrs);
			// } catch (JSONException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// }
			// });

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
						inviteFriend("/data/approvedClub/session/", arr, arrs);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});

			return convertView;
		}

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
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub
		
	}

}

package com.bandlink.air.friend;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bandlink.air.Friendpage;
import com.bandlink.air.MyLog;
import com.bandlink.air.R;
import com.bandlink.air.club.BorderImageView;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Util;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.utils.StorageUtils;

public class friendInviteActivity extends LovefitActivity {
	private ListView list1;
	private List<User> list;
	String nickname, photo, notes;
	SharedPreferences share;
	User[] user;
	int suid;
	PopupWindow p;
	ProgressDialog dialog;
	Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friendinvite);
		if (android.os.Build.VERSION.SDK_INT >= 11) {
			mContext = new ContextThemeWrapper(this,
					android.R.style.Theme_Holo_Light);
		} else {
			mContext = this;
		}
		ActionbarSettings actionbar = new ActionbarSettings(this, lsr, null);
		actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
		actionbar.setTitle(R.string.friendinvite);
		share = getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		list1 = (ListView) findViewById(R.id.friend_invite_list);
		File cacheDir = StorageUtils.getOwnCacheDirectory(this,
				"UniversalImageLoader/Cache");
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				this).threadPriority(Thread.NORM_PRIORITY - 1)
				.denyCacheImageMultipleSizesInMemory()
				.memoryCache(new UsingFreqLimitedMemoryCache(1024 * 1024 * 2))
				.threadPoolSize(5).imageDecoder(new BaseImageDecoder(true))
				.discCache(new UnlimitedDiscCache(cacheDir))
				.defaultDisplayImageOptions(DisplayImageOptions.createSimple())
				.build();
		ImageLoader.getInstance().init(config);
		dialog = Util.initProgressDialog(friendInviteActivity.this, true, null, null);
		new Thread(new Runnable() {

			@Override
			public void run() {
				list = new ArrayList<User>();
				Message msg = handler.obtainMessage();
				String result;
				Map<String, String> map = new LinkedHashMap<String, String>();
				map.put("session", share.getString("session_id", ""));
				try {
					result = HttpUtlis.getRequest(HttpUtlis.GETFRIENDMESSAGE,
							map);
					if (result != null) {
						JSONObject json;
						JSONArray arr;
						json = new JSONObject(result);
						if (Integer.valueOf(json.getString("status")) == 0) {
							arr = json.getJSONArray("content");
							user = new User[arr.length()];
							for (int i = 0; i < arr.length(); i++) {
								JSONObject temp = (JSONObject) arr.get(i);
								suid = Integer.parseInt(temp
										.getString("fromid"));
								nickname = temp.getString("nickname");
								photo = "http://www.lovefit.com/ucenter/data/avatar/"
										+ getAvatarUrl(String.valueOf(suid));
								notes = temp.getString("tomsg");
								list.add(new User(suid, nickname, photo, notes));

							}
							msg.what = 1;
							handler.sendMessage(msg);
						} else {
							msg.what = 0;
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

 

	public String getAvatarUrl(String uid) {
		String size = "middle";
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

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {

			switch (msg.what) {

			case 0:

				Toast.makeText(friendInviteActivity.this, "error", 1).show();
				break;
			case 1:
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
				frienedInviteAdapter adapter = new frienedInviteAdapter(
						friendInviteActivity.this, list);
				list1.setAdapter(adapter);
				// setListViewHeightBasedOnChildren(list1);
				break;
			}
		}
	};

	public void setListViewHeightBasedOnChildren(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			// pre-condition
			return;
		}

		int totalHeight = 0;
		for (int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}
		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight
				+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		listView.setLayoutParams(params);
	}

	private OnClickListener lsr = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			friendInviteActivity.this.finish();
		}
	};

	class frienedInviteAdapter extends BaseAdapter {
		private Context context;
		private List<User> list;
		private LayoutInflater mInflater;
		int uid;
		SharedPreferences share;
		String result, notes;

		public frienedInviteAdapter(Context context, List<User> list) {
			this.context = context;
			this.list = list;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			share = context.getSharedPreferences(
					SharePreUtils.APP_ACTION,
					context.MODE_PRIVATE);
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = mInflater.inflate(R.layout.friend_invite_item, null);

			TextView add_nickname = (TextView) convertView
					.findViewById(R.id.add_nickname);
			BorderImageView add_image = (BorderImageView) convertView
					.findViewById(R.id.add_image);
			final ImageView btn_add = (ImageView) convertView.findViewById(R.id.more);
			TextView note = (TextView) convertView.findViewById(R.id.note);

			add_nickname.setText(list.get(position).name);
			note.setText(list.get(position).notes);
			ImageLoader.getInstance().displayImage(list.get(position).photo,
					add_image);
			
			btn_add.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					float rotate = btn_add.getTag()==null?0f:Float.valueOf(btn_add.getTag().toString());
					ObjectAnimator oa = ObjectAnimator.ofFloat(v, "rotationY", rotate+180f);
					btn_add.setTag(rotate+180f);
					oa.setDuration(800);
					oa.start();
					String[] Items = { getString(R.string.add_guanzhu),
							getString(R.string.agree_add),
							getString(R.string.ignore),
							getString(R.string.visite) };
					AlertDialog.Builder ab1 = new AlertDialog.Builder(mContext);
					ab1.setTitle(list.get(position).name);
					ab1.setItems(Items, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface d, int which) {
							// TODO Auto-generated method stub
							if (which == 0) {
								dialog = Util.initProgressDialog(friendInviteActivity.this, true, null, null);
								new Thread(new Runnable() {

									@Override
									public void run() {
										// TODO Auto-generated method stub

										Message msg = handler.obtainMessage();
										Map<String, String> map = new LinkedHashMap<String, String>();
										map.put("uid", share.getInt("UID", -1)
												+ "");
										map.put("fid", ""
												+ list.get(position).uid);

										try {
											result = HttpUtlis.getRequest(
													HttpUtlis.FRIEND_GUANZHU,
													map);
											if (result != null) {
												JSONObject json;
												json = new JSONObject(result);
												int s = Integer.valueOf(json
														.getString("status"));
												MyLog.e("ss", s + "..."
														+ result);
												if (s == 0) {
													msg.what = 7;
													handler.sendMessage(msg);
												} else if (s == 1) {
													msg.what = 8;
													handler.sendMessage(msg);
												} else if (s == 2) {
													msg.what = 9;
													handler.sendMessage(msg);
												} else if (s == 3) {
													msg.what = 10;
													handler.sendMessage(msg);
												} else if (s == 4) {
													msg.what = 11;
													handler.sendMessage(msg);
												}
											}
											if (dialog != null
													&& dialog.isShowing()) {
												dialog.dismiss();
											}
										} catch (Exception e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								}).start();
							} else if (which == 1) {
								dialog = Util.initProgressDialog(friendInviteActivity.this, true, null, null);
								new Thread(new Runnable() {
									@Override
									public void run() {
										// TODO Auto-generated method stub
										Message msg = handler.obtainMessage();
										Map<String, String> map = new LinkedHashMap<String, String>();
										map.put("session", share.getString(
												"session_id", ""));
										map.put("fuid", list.get(position).uid
												+ "");
										try {
											result = HttpUtlis.getRequest(
													HttpUtlis.FRIEND_ADDFRIEND,
													map);
											if (result != null) {
												JSONObject json;
												json = new JSONObject(result);
												int s = Integer.valueOf(json
														.getString("status"));
												MyLog.e("ss", s + "..."
														+ result);
												if (s == 0) {
													msg.what = 12;
													handler.sendMessage(msg);
												} else if (s == 1) {
													msg.what = 1;
													handler.sendMessage(msg);
												} else if (s == 2) {
													msg.what = 2;
													handler.sendMessage(msg);
												} else if (s == 3) {
													msg.what = 3;
													handler.sendMessage(msg);
												} else if (s == 4) {
													msg.what = 4;
													handler.sendMessage(msg);
												} else if (s == 5) {
													msg.what = 5;
													handler.sendMessage(msg);
												} else if (s == 6) {
													msg.what = 6;
													handler.sendMessage(msg);
												}
											}
											if (dialog != null
													&& dialog.isShowing()) {
												dialog.dismiss();
											}
										} catch (Exception e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}

								}).start();
							} else if (which == 2) {
								dialog = Util.initProgressDialog(friendInviteActivity.this, true, null, null);
								new Thread(new Runnable() {
									@Override
									public void run() {
										// TODO Auto-generated method stub

										Message msg = handler.obtainMessage();
										Map<String, String> map = new LinkedHashMap<String, String>();
										map.put("session", share.getString(
												"session_id", ""));
										map.put("fuid", ""
												+ list.get(position).uid);
										try {
											result = HttpUtlis
													.getRequest(
															HttpUtlis.DELETEFRIEND,
															map);
											if (result != null) {
												JSONObject json;
												json = new JSONObject(result);
												int s = Integer.valueOf(json
														.getString("status"));
												if (s == 0) {
													msg.what = 0;
													handler.sendMessage(msg);
												} else if (s == 1) {
													msg.what = 1;
													handler.sendMessage(msg);
												} else if (s == 2) {
													msg.what = 2;
													handler.sendMessage(msg);
												} else if (s == 3) {
													msg.what = 3;
													handler.sendMessage(msg);
												} else if (s == 4) {
													msg.what = 4;
													handler.sendMessage(msg);
												} else if (s == 5) {
													msg.what = 0;
													handler.sendMessage(msg);
												}
											}
											if (dialog != null
													&& dialog.isShowing()) {
												dialog.dismiss();
											}
										} catch (Exception e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}

								}).start();
							} else if (which == 3) {

								Intent page = new Intent();
								page.putExtra("uid", list.get(position).uid
										+ "");
								page.putExtra("name", list.get(position).name);
								page.setClass(friendInviteActivity.this,
										Friendpage.class);
								startActivity(page);
							}

						}
					});
					ab1.setNegativeButton(getString(R.string.close),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									dialog.dismiss();
								}
							});
					ab1.create().show();
				}
			});

			return convertView;
		}

		Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 0:
					Toast.makeText(context, R.string.ignore_ok, 1).show();
					break;
				case 1:
					Toast.makeText(context, R.string.no_param, 1).show();
					break;
				case 2:
					Toast.makeText(context, R.string.not_user, 1).show();
					break;
				case 3:
					Toast.makeText(context, R.string.not_add_own, 1).show();
					break;
				case 4:
					Toast.makeText(context, R.string.ID_not_exist, 1).show();
					break;
				case 5:
					Toast.makeText(context, R.string.you_have_friend, 1).show();
					break;
				case 6:
					Toast.makeText(context, R.string.wait_agree, 1).show();
					break;
				case 7:
					Toast.makeText(context, R.string.guanzhu_ok, 1).show();
					break;
				case 8:
					Toast.makeText(context, R.string.no_param, 1).show();
					break;
				case 9:
					Toast.makeText(context, R.string.not_own, 1).show();
					break;
				case 10:
					Toast.makeText(context, R.string.user_no_exist, 1).show();
					break;
				case 11:
					Toast.makeText(context, R.string.have_guanzhu, 1).show();
					break;
				case 12:
					Toast.makeText(context, R.string.add_friend_ok, 1).show();
					break;
				}
			}
		};

		public class ViewHolder {
			ImageView add_image;
			EditText note;
			TextView add_nickname;
			Button btn_add;
		}

	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub
		
	}
}

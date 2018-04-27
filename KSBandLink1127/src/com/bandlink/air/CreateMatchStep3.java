package com.bandlink.air;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bandlink.air.friend.User;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.CustomGallery;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.InviteList;
import com.bandlink.air.util.InviteList.PersonAdapter;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.MApplication;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.view.MGridView;
import com.nostra13.universalimageloader.core.ImageLoader;

public class CreateMatchStep3 extends LovefitActivity implements OnClickListener {
	SharedPreferences share;
	Button btninvite, btnok;
	MGridView grid;
	ArrayList<User> ali = new ArrayList<User>();
	ArrayList<User> lis;
	int[] ids = new int[0], temp;
	PersonAdapter p;
	ProgressDialog dialog;
	Bundle b;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setContentView(R.layout.creatematch_3);
		b = getIntent().getExtras();
		share = getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		btninvite = (Button) findViewById(R.id.btninvite);
		btnok = (Button) findViewById(R.id.btnok);
		btninvite.setOnClickListener(this);
		btnok.setOnClickListener(this);
		grid = (MGridView) findViewById(R.id.gridGallery);
		ActionbarSettings actionbar = new ActionbarSettings(this,
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						try {
							MApplication.getInstance().getList().remove(1);
							MApplication.getInstance().getList().remove(2);
						} catch (Exception e) {
							e.printStackTrace();
						}
						CreateMatchStep3.this.finish();
					}
				}, null);
		// actionbar.setTopRightIcon(R.drawable.more);
		actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
		// actionbar.setTopRightIcon(R.drawable.ic_share);
		actionbar.setTitle(R.string.ab_createrace);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btninvite:
			dialog = initProgressDialog();
			getAllFriend();
			break;
		case R.id.btnok:
			dialog = initProgressDialog();
			String t = "";
			// ArrayList<ImageEntity> imgs = new ArrayList<ImageEntity>();
			ArrayList<String> imgs = new ArrayList<String>();
			ArrayList<String> data = b.getStringArrayList("data");
			Bitmap logo = HttpUtlis.Bytes2Bimap(b.getByteArray("logo"));
			try {
				if (b.containsKey("imgs")) {
					imgs = b.getStringArrayList("imgs");
				}
				if (b.containsKey("text")) {
					t = b.getString("text");
				}
			} catch (Exception e) {
			}
			String s = "";
			for (int i = 0; i < ids.length; i++) {
				s += ids[i] + ",";
			}

			if (s.length() > 1)
				s = s.substring(0, s.length() - 1);
			else
				s = "";
			creatMatch(data, logo, t, imgs, s);
			dialog.show();
			btnok.setEnabled(false);
			btnok.setTextColor(getResources().getColor(R.color.white_0_8));
			break;
		}

	}

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
						// TODO Auto-generated method stub

					}
				});

		return progressDialog;
	}

	private void creatMatch(final ArrayList<String> data, final Bitmap logo,
			final String t, final ArrayList<String> bit, final String ids) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					String result = HttpUtlis.postMatchCreate(
							HttpUtlis.BASE_URL + "/data/createMatch/session/"
									+ share.getString("session_id", ""), logo,
							data, t, bit, ids);
					System.out.println(result);
					JSONObject js = new JSONObject(result);
					if (js.getInt("status") == 0) {
						h.sendEmptyMessage(2);
					} else {
						h.obtainMessage(3, js).sendToTarget();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					if (e.toString().contains("Connect")
							|| e.toString().contains("Unknow")) {
						h.sendEmptyMessage(3);
					}

					e.printStackTrace();
				}

			}

		}).start();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub

		super.onDestroy();
	}

	private ArrayList<CustomGallery> getGalleryPhotos(ArrayList<User> alist) {
		ArrayList<CustomGallery> galleryList = new ArrayList<CustomGallery>();

		for (User str : alist) {
			CustomGallery item = new CustomGallery();
			item.id = str.uid; // sdcardPath : uid
			item.sdcardPath = str.photo;
			item.name = str.name;
			galleryList.add(item);
		}
		return galleryList;
	}
 
	public void setListViewHeightBasedOnChildren(GridView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			// pre-condition
			return;
		}
		int all = listAdapter.getCount();
		int line = listView.getNumColumns();
		int rows = all / line;
		if (all % line != 0) {
			rows = rows + 1;
		}
		int totalHeight = 0;

		for (int i = 0; i < rows; i++) {
			View listItem = listAdapter.getView(0, null, listView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight;
		listView.setLayoutParams(params);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == 60 && data != null) {
			temp = data.getIntArrayExtra("ids");
			ids = join(ids, temp);
			lis = data.getParcelableArrayListExtra("user");
			ali.addAll(lis);
			p = new PersonAdapter(this, ImageLoader.getInstance(),
					getGalleryPhotos(ali));
			grid.setAdapter(p);
			setListViewHeightBasedOnChildren(grid);
			grid.setOnItemLongClickListener(new OnItemLongClickListener() {

				@Override
				public boolean onItemLongClick(AdapterView<?> parent,
						View view, int position, long id) {
					List<Integer> list = new ArrayList<Integer>();
					for (int i = 0; i < ids.length; i++) {
						list.add(ids[i]);
					}
					list.remove(position);
					Integer[] s = list.toArray(new Integer[ids.length - 1]);
					ids = new int[ids.length - 1];
					for (int j = 0; j < s.length; j++) {
						ids[j] = s[j].intValue();
					}
					ali.remove(position);
					p = new PersonAdapter(CreateMatchStep3.this, ImageLoader
							.getInstance(), getGalleryPhotos(ali));
					grid.setAdapter(p);
					h.sendEmptyMessage(1);
					return true;
				}
			});

		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/***
	 * 合并数组
	 * 
	 * @param a
	 *            数组A
	 * @param b
	 *            数组B
	 * @return 新数组
	 */
	public int[] join(int[] a, int[] b) {
		int[] ne = new int[a.length + b.length];
		for (int j = 0; j < a.length; ++j) {
			ne[j] = a[j];
		}

		for (int j = 0; j < b.length; ++j) {
			ne[a.length + j] = b[j];
		}
		return ne;
	}

	@SuppressLint("HandlerLeak")
	Handler h = new Handler() {

		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case 0:
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
				if (((ArrayList<User>) msg.obj).size() < 1) {
					Toast.makeText(CreateMatchStep3.this,
							getString(R.string.no_usertoinvalite),
							Toast.LENGTH_SHORT).show();
					break;
				}
				Intent i = new Intent(CreateMatchStep3.this, InviteList.class);
				i.putParcelableArrayListExtra("data",
						((ArrayList<User>) msg.obj));
				startActivityForResult(i, 60);
				break;
			case 1:

				break;
			case 2:
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
				btnok.setEnabled(true);
				btnok.setTextColor(getResources().getColor(R.color.white));
				for (Activity a : MApplication.getInstance().getList()) {
					a.finish();
				}
				MApplication.getInstance().clearAll();
				CreateMatchStep3.this.finish();
				break;
			case 3:
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
				btnok.setEnabled(true);
				btnok.setTextColor(getResources().getColor(R.color.white));
				Toast.makeText(CreateMatchStep3.this, msg.obj.toString(),
						Toast.LENGTH_SHORT).show();
				break;
			}
			super.handleMessage(msg);
		}
	};

	/***
	 * 判断是否在数组中
	 * 
	 * @param arr
	 *            数组
	 * @param j
	 *            元素
	 * @return
	 */
	public boolean dataIn(int[] arr, int j) {
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == j) {
				return true;
			}
		}
		return false;
	}

	public void getAllFriend() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("session", share.getString("session_id", ""));
				try {
					String result = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/user/getUserFriend", map);
					JSONArray arr = new JSONObject(result)
							.getJSONArray("content");
					ArrayList<User> users = new ArrayList<User>();
					for (int i = 0; i < arr.length(); i++) {
						JSONObject js = arr.getJSONObject(i);
						if (dataIn(ids, js.getInt("fuid"))) {
							continue;
						}
						String ss = HttpUtlis.AVATAR_URL
								+ HttpUtlis.getAvatarUrl(js.getInt("fuid") + "");
						users.add(new User(js.getInt("fuid"), js
								.getString("nickname"),
								HttpUtlis.AVATAR_URL
										+ HttpUtlis.getAvatarUrl(js
												.getInt("fuid") + "")));

					}
					Message msg = new Message();
					msg.obj = users;
					msg.what = 0;
					h.sendMessage(msg);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					h.sendEmptyMessage(3);
					e.printStackTrace();
				}
			}

		}).start();
	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub
		
	}

}

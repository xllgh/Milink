package com.bandlink.air.friend;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bandlink.air.R;
import com.bandlink.air.club.BorderImageView;
import com.bandlink.air.jpush.Jpush;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Util;
import com.nostra13.universalimageloader.core.ImageLoader;

public class AddFriendAdapter extends BaseAdapter {
	HashMap<Integer, View> lmap = new HashMap<Integer, View>();
	private Context context;
	private List<User> list;
	private LayoutInflater mInflater;
	int fuid;
	SharedPreferences share;
	String result, notes;
	ProgressDialog progressDialog;

	public AddFriendAdapter(Context context, List<User> list) {
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

	public void btn_invite(int position) {

	}

	 

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		share = context
				.getSharedPreferences(
						SharePreUtils.APP_ACTION,
						context.MODE_PRIVATE);
		final ViewHolder holder;
		if (lmap.get(position) == null) {
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = mInflater.inflate(R.layout.item_searchfriend, null);
			holder = new ViewHolder();
			holder.add_image = (BorderImageView) convertView
					.findViewById(R.id.add_image);
			holder.add_nickname = (TextView) convertView
					.findViewById(R.id.add_nickname);
			holder.btn_add = (Button) convertView.findViewById(R.id.btn_add);
			if(!Locale.getDefault().getLanguage().equals("zh"))
			{
				holder.btn_add.setText(R.string.add_dd);
			}
			holder.note = (EditText) convertView.findViewById(R.id.note);
			holder.add_nickname.setText(list.get(position).name);
			holder.note.addTextChangedListener(new TextWatcher() {
				private CharSequence temp;
				private int selectionStart;
				private int selectionEnd;
				int num = 10;

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
					// TODO Auto-generated method stub
					temp = s;
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
					// TODO Auto-generated method stub

				}

				@Override
				public void afterTextChanged(Editable s) {
					// TODO Auto-generated method stub
					notes = holder.note.getText().toString();
					int number = num - s.length();
					selectionStart = holder.note.getSelectionStart();
					selectionEnd = holder.note.getSelectionEnd();
					if (temp.length() > num) {
						s.delete(selectionStart - 1, selectionEnd);
						int tempSelection = selectionEnd;
						holder.note.setText(s);
						holder.note.setSelection(tempSelection);
					}
				}

			});
			ImageLoader.getInstance().displayImage(list.get(position).photo,
					holder.add_image);
			lmap.put(position, convertView);
			convertView.setTag(holder);
		} else {
			convertView = lmap.get(position);
			holder = (ViewHolder) convertView.getTag();
		}

		final int currentPosition = position;
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				holder.btn_add.setText(R.string.had_invite);
				holder.btn_add.setBackgroundResource(R.drawable.loading_bg);
				holder.btn_add.setClickable(false);
				holder.note.setEnabled(false);
				if (progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				switch (msg.what) {
				case 0:
					Toast.makeText(context, R.string.add_friend_ok, 1).show();
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
					Toast.makeText(context, R.string.wait_agree, 1).show();
					String jpushid = (String) msg.obj;
					if (jpushid != null && !jpushid.equals("")) {
						Jpush jpush = new Jpush(context);
						if (notes == null || notes.equals("")) {
							notes = context.getString(R.string.friend_add);
						}
						jpush.sendFriendMessage(notes, jpushid);
					}
					break;

				}
			}
		};

		holder.btn_add.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				progressDialog = Util.initProgressDialog(context, true, null, null);
				new Thread(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						Message msg = handler.obtainMessage();
						fuid = list.get(currentPosition).uid;
						Map<String, String> map = new LinkedHashMap<String, String>();
						map.put("session", share.getString("session_id", ""));
						map.put("fuid", fuid + "");
						if (holder.note.getText().toString().length() > 0) {
							map.put("note", notes);
						} else {

						}
						try {
							result = HttpUtlis.getRequest(
									HttpUtlis.FRIEND_ADDFRIEND, map);
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
									msg.what = 5;
									handler.sendMessage(msg);
								} else if (s == 6) {
									msg.what = 6;
									handler.sendMessage(msg);
								} else if (s == 7) {
									msg.what = 7;
									msg.obj = json.getString("content");
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
		});

		return convertView;
	}

	public class ViewHolder {
		BorderImageView add_image;
		EditText note;
		TextView add_nickname;
		Button btn_add;
	}

}
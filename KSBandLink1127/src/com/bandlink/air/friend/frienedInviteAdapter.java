package com.bandlink.air.friend;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bandlink.air.R;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.SharePreUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

public class frienedInviteAdapter extends BaseAdapter {
	private Context context;
	private List<User> list;
	private LayoutInflater mInflater;
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		share = context
				.getSharedPreferences(
						SharePreUtils.APP_ACTION,
						context.MODE_PRIVATE);
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.friend_invite_item, null);
			holder = new ViewHolder();
			holder.add_nickname = (TextView) convertView
					.findViewById(R.id.add_nickname);
			holder.add_image = (ImageView) convertView
					.findViewById(R.id.add_image);
			holder.btn_add = (Button) convertView.findViewById(R.id.btn_add);
			holder.note = (TextView) convertView.findViewById(R.id.note);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();

		}
		holder.add_nickname.setText(list.get(position).name);
		holder.note.setText(list.get(position).notes);

		ImageLoader.getInstance().displayImage(list.get(position).photo,
				holder.add_image);
		holder.btn_add.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				new Thread(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						Message msg = handler.obtainMessage();
						Map<String, String> map = new LinkedHashMap<String, String>();
						map.put("session", share.getString("session_id", ""));
						map.put("fuid", list.get(position).uid + "");
						map.put("note", notes);

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

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				Toast.makeText(context, "ok", Toast.LENGTH_SHORT).show();
				break;
			case 1:
				Toast.makeText(context, "no oaram", Toast.LENGTH_SHORT).show();
				break;
			case 2:
				Toast.makeText(context, "no user", Toast.LENGTH_SHORT).show();
				break;
			case 3:
				Toast.makeText(context, "the fuid is yourself",
						Toast.LENGTH_SHORT).show();
				break;
			case 4:
				Toast.makeText(context, "the fuid not exist",
						Toast.LENGTH_SHORT).show();
				break;
			case 5:
				Toast.makeText(context, "you_have_friends", Toast.LENGTH_SHORT)
						.show();
				break;
			case 6:
				Toast.makeText(context, "waiting_for_the_other_test",
						Toast.LENGTH_SHORT).show();
				break;
			case 7:
				break;

			}
		}
	};

	public class ViewHolder {
		ImageView add_image;
		TextView note;
		TextView add_nickname;
		Button btn_add;
	}

}

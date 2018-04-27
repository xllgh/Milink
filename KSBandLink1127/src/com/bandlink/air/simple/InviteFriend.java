package com.bandlink.air.simple;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.SharePreUtils;

public class InviteFriend extends LovefitActivity {

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub

	}

	private SharedPreferences sp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		sp = getSharedPreferences(SharePreUtils.APP_ACTION,
				MODE_PRIVATE);
		super.onCreate(savedInstanceState);
	}

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case 0:
				ArrayList<HashMap<String, String>> data = (ArrayList<HashMap<String, String>>)msg.obj;
				 
				break;
			}
			super.handleMessage(msg);
		}

	};

	private void getFriendList() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("session", sp.getString("session_id", ""));
				try {
					String result = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/user/getUserFriend/", params);
					JSONArray arr = new JSONObject(result)
							.getJSONArray("content");
					ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
					for (int i = 0; i < arr.length(); i++) {
						JSONObject js = arr.getJSONObject(i);
						HashMap<String, String> row = new HashMap<String, String>();
						row.put("name", js.getString("nickname"));
						row.put("uid", js.getString("fuid"));
						row.put("jid", js.getString("nickname"));
						data.add(row);
					}
					handler.obtainMessage(0, data).sendToTarget();
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
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		handler.removeCallbacksAndMessages(null);
		super.onDestroy();
	}
}

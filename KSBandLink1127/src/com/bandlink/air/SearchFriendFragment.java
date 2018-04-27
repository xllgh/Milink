package com.bandlink.air;

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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.bandlink.air.friend.AddFriendAdapter;
import com.bandlink.air.friend.User;
import com.bandlink.air.jpush.Jpush;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Util;

public class SearchFriendFragment extends Fragment implements OnClickListener {
	Button searching;
	EditText tv_input;
	SharedPreferences share;
	int suid, fuid;
	String result, nickname, photo;
	ListView search_result_list;
	User[] user;
	private List<User> list;
	ProgressDialog progressDialog;
	TextView sea;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View v = inflater.inflate(R.layout.searchfriend_frame3, null);
		con =Util.getThemeContext(getActivity());
		share = getActivity()
				.getSharedPreferences(
						SharePreUtils.APP_ACTION,
						Context.MODE_PRIVATE);
		suid = share.getInt("UID", suid);
		sea = (TextView) v.findViewById(R.id.ss);
		searching = (Button) v.findViewById(R.id.searching);
		tv_input = (EditText) v.findViewById(R.id.input);
		tv_input.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				// TODO Auto-generated method stub
				if( EditorInfo.IME_ACTION_SEARCH==actionId){
					search();
				}
				return true;
			}
		});

		search_result_list = (ListView) v.findViewById(R.id.search_result_list);
		searching.setOnClickListener(this);
		return v;
	}

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				Toast.makeText(getActivity(), "error", 1).show();
				break;
			case 1:
				if (progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				search_result_list.setFocusable(true);
				AddFriendAdapter adapter = new AddFriendAdapter(getActivity(),
						list);
				search_result_list.setAdapter(adapter);
				search_result_list.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						// TODO Auto-generated method stub
						addFriend(list.get(position).name,list.get(position).uid+"");
						//Toast.makeText(getActivity(), list.get(position).name, Toast.LENGTH_LONG).show();
					}
				});
//				((InputMethodManager) getActivity().getSystemService(
//						getActivity().INPUT_METHOD_SERVICE))
//						.hideSoftInputFromWindow(getActivity()
//								.getCurrentFocus().getWindowToken(),
//								InputMethodManager.HIDE_NOT_ALWAYS);
				
				break;
			case 3:
				if(progressDialog!=null && progressDialog.isShowing()){
					progressDialog.dismiss();
				}
				Toast.makeText(getActivity(), msg.arg1, Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};
	Context con;
	void sendRequest(final String uid,final String notes ){
		progressDialog = Util.initProgressDialog(getActivity(), false, getString(R.string.data_wait), null);
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Message msg = handler.obtainMessage();
				 
				Map<String, String> map = new LinkedHashMap<String, String>();
				map.put("session", share.getString("session_id", ""));
				map.put("fuid", uid + ""); 
				map.put("note", notes);
				 
				try {
					result = HttpUtlis.getRequest(
							HttpUtlis.FRIEND_ADDFRIEND, map);
					if (result != null) {
						JSONObject json;
						json = new JSONObject(result);
						int s = Integer.valueOf(json
								.getString("status"));
						msg.what =3;
						if (s == 0) {
							 
							msg.arg1 =R.string.add_friend_ok;
							handler.sendMessage(msg);
						} else if (s == 1) {
							 
							msg.arg1 =R.string.no_param;
							handler.sendMessage(msg);
						} else if (s == 2) {
							 
							msg.arg1 =R.string.not_user;
							handler.sendMessage(msg);
						} else if (s == 3) {
							 
							msg.arg1 =R.string.not_add_own;
							handler.sendMessage(msg);
						} else if (s == 4) {
							 
							msg.arg1 =R.string.ID_not_exist;
							handler.sendMessage(msg);
						} else if (s == 5) {
							 
							msg.arg1 =R.string.you_have_friend;
							handler.sendMessage(msg);
						} else if (s == 6) { 
							
							msg.arg1 =R.string.wait_agree;
							handler.sendMessage(msg);
						} else if (s == 7) {
						 
							msg.arg1 =R.string.wait_agree;
							msg.obj = json.getString("content");
							handler.sendMessage(msg);
							String jpushid = (String) msg.obj;
							if (jpushid != null && !jpushid.equals("")) {
								Jpush jpush = new Jpush(getActivity()); 
								jpush.sendFriendMessage(notes, jpushid);
							}
						}
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}).start();
	}
	void addFriend(String str,final String uid) {
		
		AlertDialog.Builder ab = new AlertDialog.Builder(con);
		ab.setTitle(getString(R.string.addnewfriend));
		final EditText e = new EditText(con);
		e.setHint(String.format(getString(R.string.add_info)));
		e.setHintTextColor(getResources().getColor(R.color.transparent_black3));
		LinearLayout l = new LinearLayout(con);
		//l.setBackgroundColor(getResources().getColor(R.color.transparent_black3));
		l.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		e.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		l.setPadding(20, 30, 20, 10);
		l.addView(e);
		ab.setView(l);
	
		ab.setNegativeButton(R.string.can, null);
		ab.setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				String np = getString(R.string.friend_add);
				if(	e.getText()!=null && e.getText().toString().length()>0){
					np = e.getText().toString();
				}
				
				sendRequest(uid,np);	
				 
			}
		});
		ab.show();
	}
 
void search(){
	if (tv_input.getText().toString() != null
			&& tv_input.getText().length() > 0) {
		progressDialog = Util.initProgressDialog(getActivity(), true, getString(R.string.data_wait), null);
		//sea.setVisibility(View.VISIBLE);
		new Thread(new Runnable() {
			@Override
			public void run() {
				list = new ArrayList<User>();
				// TODO Auto-generated method stub
				Message msg = handler.obtainMessage();
				Map<String, String> map = new LinkedHashMap<String, String>();
				map.put("session", share.getString("session_id", ""));
				map.put("search", tv_input.getText().toString()
						.replace(" ", ""));
				try {
					result = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/user/searchFriend", map);
					if (result != null) {
						JSONObject json;
						JSONArray arr;
						json = new JSONObject(result);
						if (Integer.valueOf(json.getString("status")) == 0) {
							arr = json.getJSONArray("content");
							user = new User[arr.length()];
							for (int i = 0; i < arr.length(); i++) {
								JSONObject temp = (JSONObject) arr
										.get(i);
								fuid = Integer.parseInt(temp
										.getString("uid"));
								nickname = temp.getString("name");
								photo = "http://www.lovefit.com/ucenter/data/avatar/"
										+ getAvatarUrl(String
												.valueOf(fuid));
								list.add(new User(fuid, nickname, photo));
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
}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.searching:
			search();
			break;
		default:
			break;
		}
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

}

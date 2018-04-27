package com.bandlink.air.simple;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.SharedPreferences;

import com.bandlink.air.ble.LogUtil;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.HttpUtlis;

public class RunnableweekRank implements Runnable {
	SharedPreferences share;
	Dbutils db;
	OnFinished mOnFinished;
	public void setOnFinished(OnFinished mOnFinished){
		this.mOnFinished =  mOnFinished;
	}
	public RunnableweekRank(SharedPreferences share, Dbutils db) {
		super();
		this.share = share;
		this.db = db;
	}
	

	@Override
	public void run() {
		// TODO Auto-generated method stub
		String w2 = share.getString("session_id", "");
		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("session", w2);
		try {
			String result = HttpUtlis.getRequest(
					HttpUtlis.FRIEND_GETFRIENDRANKWEEK, map);
			if (result != null) {
				JSONObject json;
				JSONArray arr;
				json = new JSONObject(result);
				arr = json.getJSONArray("content");
				db.DeleTeRank(2);
				for (int i = 0; i < arr.length(); i++) {
					JSONObject temp = (JSONObject) arr.get(i);
					db.SaveRankData(
							2,
							Integer.parseInt(temp.getString("uid")),
							temp.getString("name"),
							Integer.parseInt(temp.getString("step")),
							Integer.parseInt(temp.getString("gid")),
							temp.getString("time"),
							"http://www.lovefit.com/ucenter/data/avatar/"
									+ HttpUtlis.getAvatarUrl(temp
											.getString("uid")));
				}
				if(mOnFinished!=null){
					mOnFinished.Finished(2,-1);
				}
				LogUtil.e("##上周步数<---##"+new SimpleDateFormat("HH:mm:ss:SSS").format(new Date()));
//				LogUtil.e("##本月步数--->##"+new SimpleDateFormat("HH:mm:ss:SSS").format(new Date()));
//				new Thread(new RunnableMonthRank(share, db)).start();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			if(mOnFinished!=null){
				mOnFinished.Error(2);
			}
			e.printStackTrace();
		}

	}
}

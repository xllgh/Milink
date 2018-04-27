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

public class RunnableDayRank  implements Runnable {
	SharedPreferences share;
	Dbutils db;
	OnFinished mOnFinished;
	public void setOnFinished(OnFinished mOnFinished){
		this.mOnFinished =  mOnFinished;
	}
	public RunnableDayRank(SharedPreferences share, Dbutils db) {
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
					HttpUtlis.FRIEND_GETFRIENDRANKDAY, map);
			
			if (result != null) {
				JSONObject json;
				JSONArray arr;
				json = new JSONObject(result);
				arr = json.getJSONArray("content");
				db.DeleTeRank(1);
				for (int i = 0; i < arr.length(); i++) {
					JSONObject temp = (JSONObject) arr.get(i);
					db.SaveRankData(
							1,
							Integer.parseInt(temp.getString("uid")),
							temp.getString("name"),
							Integer.parseInt(temp.getString("step")),
							Integer.parseInt(temp.getString("gid")),
							temp.getString("time"),
							"http://www.lovefit.com/ucenter/data/avatar/"
									+ HttpUtlis.getAvatarUrl(String
											.valueOf(Integer.parseInt(temp.getString("uid")))));
				}
				share.edit().putString("rankcheck", new SimpleDateFormat("yyyyMMdd").format(new Date()).toString()).commit();
				if(mOnFinished!=null){
					mOnFinished.Finished(1,-1);
				}
				LogUtil.e("##昨日步数<---##"+new SimpleDateFormat("HH:mm:ss:SSS").format(new Date()));
				//LogUtil.e("##上周步数--->##"+new SimpleDateFormat("HH:mm:ss:SSS").format(new Date()));
				//new Thread(new RunnableweekRank(share, db)).start();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if(mOnFinished!=null){
				mOnFinished.Error(1);
			}

		}

	}
}
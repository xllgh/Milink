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
import com.bandlink.air.util.Util;

public class RunnableRankAll  implements Runnable {
	SharedPreferences share;
	Dbutils db;
	OnFinished mOnFinished;
	private int tag =0;
	private String url =HttpUtlis.BASE_URL+"/data/getAllRankDay";
	private int index =0;
	/***
	 *  getAllRankMonth()
		getAllRankWeek()
		getAllRankDay()
	 *  
	 */
	public void setOnFinished(OnFinished mOnFinished){
		this.mOnFinished =  mOnFinished;
	}
	public RunnableRankAll(SharedPreferences share, Dbutils db,int tag,int index) {
		super();
		this.share = share;
		this.db = db;
		this.tag = tag;
		this.index = index;
		switch(tag){
		//今日
		case 4:
			url = HttpUtlis.BASE_URL+"/data/getAllRankToDay";
			break;
		//昨日
		case 1:
			url = HttpUtlis.BASE_URL+"/data/getAllRankDay";
			break;
		//上周	
		case 2:
			url = HttpUtlis.BASE_URL+"/data/getAllRankWeek";
			break;
		//本月	
		case 3:
			url = HttpUtlis.BASE_URL+"/data/getAllRankMonth";
			break;
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("start-"+index+"--"+Util.getTimeMMStringFormat("yyyy-MM-dd HH:mm:ss:SSS")+"---"+url);
		String w2 = share.getString("session_id", "");
		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("session", w2);
		map.put("sindex", index+"");
		try {
			 
			String result = HttpUtlis.getRequest(url, map); 
			
			if (result != null) {
				JSONObject json;
				JSONArray arr;
				json = new JSONObject(result);
				arr = json.getJSONArray("content");
			//	db.DeleTeRank(10+tag);
				System.out.println("getack-"+arr.length()+"--"+Util.getTimeMMStringFormat("yyyy-MM-dd HH:mm:ss:SSS")+"---"+url);
				for (int i = 0; i < arr.length(); i++) {
					JSONObject temp = (JSONObject) arr.get(i);
					db.SaveAllRankData(
							10+tag,
							Integer.parseInt(temp.getString("uid")),
							temp.getString("name"),
							Integer.parseInt(temp.getString("step")),
							0,
							temp.getString("time"),
							"http://www.lovefit.com/ucenter/data/avatar/"
									+ HttpUtlis.getAvatarUrl(String
											.valueOf(Integer.parseInt(temp.getString("uid")))));
				}
				share.edit().putString("allrankcheck", new SimpleDateFormat("yyyyMMdd").format(new Date()).toString()).commit();
				if(mOnFinished!=null){
					mOnFinished.Finished(10+tag,index);
				} 
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if(mOnFinished!=null){
				mOnFinished.Error(10+tag);
			}

		}

	}
}
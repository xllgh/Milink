package com.bandlink.air.util;

import java.sql.DataTruncation;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import com.bandlink.air.MyLog;
import com.bandlink.air.ble.BluetoothLeService;
import com.bandlink.air.util.DbContract.AIRRECORDER;

import android.R.raw;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

public class UploadData extends Thread {
	//同步数据到平台
	private int datatype;
	private Dbutils db;
	private Context context;
	private String session;
	private SharedPreferences share;
	private int battery;
	public UploadData(int type,Context c,int battery)
	{	this.battery =battery;
		datatype = type;
		context = c;
		db = new Dbutils(c);		
		share = context.getSharedPreferences(SharePreUtils.APP_ACTION,Context.MODE_MULTI_PROCESS);
		session = share.getString("session_id", "lovefit");
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		while(true)
		{
			//获取简单数据
			List<StepData> steplist = db.getUploadStep();
			StepData  temp;
			String str;
			String url;
			for(int i= 0;i<steplist.size();i++)
			{
				temp = steplist.get(i);
				if(temp.step<=0){
					continue;
				}
				 // data/upLoadSpData/session/14071680006/step/123/calorie/5234/distance/1235/time/2012-04-15
				url = String
						.format("%s/data/upLoadSpData/session/%s/step/%d/calorie/%d/distance/%s/time/%s/type/2",
								HttpUtlis.BASE_URL, session, temp.step,
								temp.calorie, temp.distance, temp.dateString);
			//	MyLog.e("Uploaddata", "url:"+url);
				str = HttpRequest.sendGet(url);
			//	MyLog.e("Uploaddata", "urlresult"+str);
				if(str.equals("1"))
				{
					db.UpdateStepStatus(temp.id);
				}else{
					break;
				}
			}
			//获取详细数据
			StepRawData rawData;
			while(true)
			{
				//获取当前日期
				rawData= db.getUploadStepRaw(MyDate.getFileName());
				if(rawData == null)
					break;
				url = String.format("%s/data/upLoadSpRawDataJson/session/%s", HttpUtlis.BASE_URL,session);
				str = String.format("data=[{\"time\":\"%s\",\"blob\":\"%s\",\"type\":5}]", rawData.date,rawData.raw);
				//MyLog.e("Uploaddata", "url:"+url+str);
				str = HttpRequest.sendPost(url, str);
			//	MyLog.e("Uploaddata", "url raw result"+str);
				if(str.equals("1"))
				{
					db.UpdateStepRawStatus(rawData.date);
				}else{
					break;
				}
				//break;
			} 
			break;
			
		}
		//传当前
		if(db.getAirInfoNeedUpLoad(MyDate.getFileName())){
			upLoadAir(MyDate.getFileName(),battery);
		} 
		//查找数据库 有没有哪天没传的
		ArrayList<String> arr = db.getAirNotUpload();
		if(arr.size()>0){
			for(String date:arr){
				upLoadAir(date,-1);
			}
		} 
	}
	

	private void upLoadAir(String date,int battery){
		 HashMap<String,String> map= db.getAirRecorder(date);
		 HashMap<String,String> args = new HashMap<String, String>();
		 HashMap<String,String> args2 = new HashMap<String, String>();
			if(map!=null){
				map.get(AIRRECORDER.ADDRESS);				
				args.put("session", session);
				args.put("deviceid", map.get("did"));
				args.put("app", map.get(AIRRECORDER.APP_VERSION));
				args.put("rom", map.get(AIRRECORDER.AIR_VERSION));
				args.put("info",Build.MANUFACTURER+"_"+Build.MODEL);
				args.put("sys", Build.VERSION.SDK_INT+"");
				
				args2.put("session", session);
				args2.put("deviceid", map.get("did"));
				args2.put("time", date);
				args2.put("battery", battery==-1?map.get(AIRRECORDER.BATTERY):battery+"");
				args2.put("alert", db.getAirlostTimes(date)+"");
				args2.put("ble", db.getAirOnCreate(date)+"");
				args2.put("screen", map.get(AIRRECORDER.LIGHTTIME));
				args2.put("vibrate", map.get(AIRRECORDER.VIBRATOR));
//				try {
//					//String s = HttpUtlis.getRequest(HttpUtlis.BASE_URL+"/user/setUserAirDevice", args);
//					//System.out.println(s);
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				
				try {
					String s = HttpUtlis.getRequest(HttpUtlis.BASE_URL+"/user/uploadDeviceInfo", args2);
					JSONObject js= new JSONObject(s);
					if(!js.getString("status").equals("0")){
						db.AirUpLoadStatus(date, 0+"");
					}else{
						db.AirUpLoadStatus(date, 1+"");
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					db.AirUpLoadStatus(date, 0+"");
				}
				
			}
	}
	
}

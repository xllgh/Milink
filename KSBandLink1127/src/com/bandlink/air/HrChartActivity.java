package com.bandlink.air;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.bandlink.air.Js.DataChartInterface;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.Dbutils.HR;
import com.bandlink.air.util.MainInterface;
import com.bandlink.air.util.MyDate;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Util;
import com.bandlink.air.util.WebViewHelper;
import com.google.gson.Gson;

public class HrChartActivity extends Fragment implements View.OnClickListener {

	private Dbutils db;

	private WebViewHelper help;

	private SharedPreferences share;

	View mView;
	MainInterface inf;

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		inf = (MainInterface) activity;
		super.onAttach(activity);
	}

	RadioGroup radioGroup;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mView = inflater.inflate(R.layout.hr, null);
		ActionbarSettings actionbar = new ActionbarSettings(mView, null, inf);
		actionbar.setTitle("心率数据");

		WebView webView = (WebView) mView.findViewById(R.id.webview);
		help = new WebViewHelper(getActivity(), webView,
				"file:///android_asset/chart/hr.html");
		help.initWebview();
		help.setBgTransparent();
		DataChartInterface js = new DataChartInterface(getActivity(), webView);
		help.addJavascriptInterface(js, "Android");
		radioGroup = (RadioGroup) mView.findViewById(R.id.radioGroup);
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				switch(checkedId){
				case R.id.day:
					drawchart(0);
					break;
				case R.id.week:
					drawchart(1);
					break;
				case R.id.month:
					drawchart(2);
					break;
				}
			}
		});
		return mView;
	}

	/** 获得x轴显示的值 **/
	public String getXvalues() {
		ArrayList<String> x = new ArrayList<String>();
		for (int i = 0; i < 10; i++) {
			x.add(i + "a");
		}
		Gson gson = new Gson();
		String xstr = gson.toJson(x);
		return xstr;
	}

	/** 获得y轴显示的值 **/
	public String getYvalues() {
		ArrayList<Integer> y = new ArrayList<Integer>();
		for (int i = 0; i < 10; i++) {
			y.add(i);
		}
		Gson gson = new Gson();
		String ystr = gson.toJson(y);
		return ystr;
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub

	}

	public void drawchart(int type) {
		String[] xValues = {  "1", "2", "3", "4", "5", "6", "7", "8", "9",
				"10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
				"20", "21", "22", "23","24" };
		String[] yValues = {};
		String now = MyDate.getFileName();
		switch (type) {
		case 0:
			// 天
			
			ArrayList<HR> data = db.getHr(now, now);
			if (data != null && data.size() > 0) {
				yValues = data.get(0).value;
			}
			break;
		case 1:
			// 一周
			 
			ArrayList<HR> data2 = db.getHr(Util.getBeforeAfterDate(now, -6).toString(), now);
			xValues = new String[data2.size()];
			yValues = new String[data2.size()];
			for(int i = 0;i<data2.size();i++){
				HR mHR = data2.get(i);
				xValues[i] = mHR.date;
				yValues[i] = mHR.avgHr+"";
			}
			break;
		case 2:
			// 一月
			ArrayList<HR> data3 = db.getHr(Util.getBeforeAfterDate(now, -29).toString(),now );
			xValues = new String[data3.size()];
			yValues = new String[data3.size()];
			for(int i = 0;i<data3.size();i++){
				HR mHR = data3.get(i);
				xValues[i] = mHR.date;
				yValues[i] = mHR.avgHr+"";
			}
			break;
		}

		Gson gson = new Gson();
		String xstr = gson.toJson(xValues);
		// String ystr = gson.toJson(yValues);
		String[] sss = new String[] { "setData('" + xstr + "','"
				+ strArr2str(yValues, false) +"','"+type+ "')" };
		System.out.println(sss[0]);
		help.loadUrls(sss);

	}

	public String strArr2str(String[] source, boolean isstr) {
		if (source == null || source.length < 1) {
			return "";
		}
		String des = "";
		for (String str : source) {
			if (isstr) {
				des += "\"" + str + "\",";
			} else {
				des += str + ",";
			}

		}
		return "[" + des.substring(0, des.length() - 1) + "]";
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		share = getActivity().getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		db = new Dbutils(share.getInt("UID", -1), getActivity());
		drawchart(0);
		super.onResume();
	}

}
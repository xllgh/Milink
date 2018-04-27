package com.bandlink.air;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bandlink.air.Js.DataChartInterface;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.MainInterface;
import com.bandlink.air.util.MyDate;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Util;
import com.bandlink.air.util.WebViewHelper;
import com.bandlink.air.view.NoRegisterDialog;
import com.google.gson.Gson;

public class DataChartActivity extends Fragment implements
		View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
	private int type, day;
	private Button btn_s, btn_d, btn_w, btn_c, btn_b, btn_day, btn_sev,
			btn_month, btn_3month;
	private Button Lbtn, Lbtn2;
	private Button[] btn, btn2;
	private Dbutils db;
	private String nickname;
	private WebViewHelper help;
	private int t_step, t_distance, t_cal;
	private double t_weight, t_bmi;
	private double t_height;
	private TextView unit;
	private String xstr, ystr;
	private int interval;
	private String tip;
	private Object target;
	private String nowtime;
	private SharedPreferences share;
	private String threeday;
	private SwipeRefreshLayout mSwipeLayout;
	// private LinearLayout div;
	TextView tv_fast_time, tv_fast_step, tv_fast_cal, tv_fast_dis, tv_mid_time,
			tv_mid_step, tv_mid_dis, tv_mid_cal, tv_low_time, tv_low_step,
			tv_low_dis, tv_low_cal;
	LinearLayout detail;
	View mView;
	MainInterface inf;
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		inf = (MainInterface)activity;
		super.onAttach(activity);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		 
		nowtime = MyDate.getFileName();
		day = 0;
		mView =  inflater.inflate(R.layout.datachart, null);
		// getUserProfile();
		ActionbarSettings actionbar = new ActionbarSettings(mView, null , inf);
		actionbar.setTitle(R.string.ab_chart); 
		btn_s = (Button) mView.findViewById(R.id.step);
		btn_d = (Button) mView.findViewById(R.id.distance);
		btn_w = (Button) mView.findViewById(R.id.weight);
		btn_c = (Button) mView.findViewById(R.id.cal);
		btn_b = (Button) mView.findViewById(R.id.BMI);

		detail = (LinearLayout) mView.findViewById(R.id.detail);

		if (type != 0) {
			detail.setVisibility(View.GONE);
		}

		btn_day = (Button) mView.findViewById(R.id.chartday);
		btn_month = (Button) mView.findViewById(R.id.chartmonth);
		btn_3month = (Button) mView.findViewById(R.id.chartthreem);
		btn_sev = (Button) mView.findViewById(R.id.chartsev);
		tv_low_step = (TextView) mView.findViewById(R.id.lowstep);
		tv_low_time = (TextView) mView.findViewById(R.id.lowtime);
		tv_low_cal = (TextView) mView.findViewById(R.id.lowcal);
		tv_low_dis = (TextView) mView.findViewById(R.id.lowdistance);
		tv_fast_step = (TextView) mView.findViewById(R.id.faststep);
		tv_fast_time = (TextView) mView.findViewById(R.id.fasttime);
		tv_fast_cal = (TextView) mView.findViewById(R.id.fastcal);
		tv_fast_dis = (TextView) mView.findViewById(R.id.fastdistance);
		tv_mid_step = (TextView) mView.findViewById(R.id.midstep);
		tv_mid_time = (TextView) mView.findViewById(R.id.midtime);
		tv_mid_cal = (TextView) mView.findViewById(R.id.midcal);
		tv_mid_dis = (TextView) mView.findViewById(R.id.middistance);
		btn_s.setOnClickListener(this);
		btn_d.setOnClickListener(this);
		btn_w.setOnClickListener(this);
		btn_c.setOnClickListener(this);
		btn_b.setOnClickListener(this);
		btn_day.setOnClickListener(this);
		btn_sev.setOnClickListener(this);
		btn_3month.setOnClickListener(this);
		btn_month.setOnClickListener(this);

		unit = (TextView) mView.findViewById(R.id.unit);
		WebView webView = (WebView) mView.findViewById(R.id.webview);
		help = new WebViewHelper(getActivity(), webView,
				"file:///android_asset/chart/high.html");
		help.initWebview();
		help.setBgTransparent();
		DataChartInterface js = new DataChartInterface(getActivity(), webView);
		help.addJavascriptInterface(js, "Android");
		btn = new Button[] { btn_s, btn_d, btn_c, btn_w, btn_b };
		btn2 = new Button[] { btn_day, btn_sev, btn_month, btn_3month };
		// div = (LinearLayout) mView.findViewById(R.id.chart_div);
		initSelectedTab();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = Util.getBeforeAfterDate(nowtime, -30);
		threeday = simpleDateFormat.format(date);

		// Object[] target = db.getUserTarget();
		// if (target != null) {
		// // _id,uid,type,step,weightbefore,weightend,ifweight,ifsleep,ifbmi
		// t_step = (Integer) target[3];
		// t_cal = (int) (t_step * 0.75);
		// t_distance = (int) (t_step * 32);
		// t_weight = (Float) target[5];
		// t_bmi = t_weight / (t_height / 100) / (t_height / 100);
		//
		// } else {
		// t_step = 0;
		// t_cal = 0;
		// t_distance = 0;
		// t_weight = 0;
		// t_bmi = 0;
		// }
		// mSwipeLayout = (SwipeRefreshLayout) mView.findViewById(R.id.id_swipe_ly);
		//
		// mSwipeLayout.setOnRefreshListener(getActivity());
		// mSwipeLayout.setColorScheme(android.R.color.holo_blue_bright,
		// android.R.color.holo_green_light,
		// android.R.color.holo_orange_light,
		// android.R.color.holo_red_light);
		return mView;
	}

	private void getUserProfile() {
		Object[] profile = db.getUserProfile();
		nickname = (String) profile[2];
		t_height = (Double) profile[5];
	}

	 
 

	/** 根据选择初始化选中的按钮 **/
	public void initSelectedTab() {

		for (int i = 0; i < btn.length; i++) {
			btn[i].setBackgroundResource(R.drawable.whitebtn);
		}
		btn[type].setBackgroundResource(R.drawable.blackbtn);
		if (type == 3 || type == 4) {
			day = -6;
			btn_day.setVisibility(View.GONE);
			btn_sev.setBackgroundResource(R.drawable.dc_chartbottom_left);
			btn2[1].setBackgroundResource(R.drawable.chart_btn_middle_pressed);
			Lbtn2 = btn_sev;
			// div.setVisibility(View.GONE);
		} else {
			day = 0;
			btn_day.setVisibility(View.VISIBLE);
			btn_sev.setBackgroundResource(R.drawable.dc_chartbottom_middle);
			btn2[0].setBackgroundResource(R.drawable.chart_btn_middle_pressed);
			// div.setVisibility(View.VISIBLE);
			Lbtn2 = btn_day;
		}
		Lbtn = btn[type];
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub

		if (Arrays.asList(btn).contains(arg0)) {// 点击上面的tab
			if (arg0.getId() != Lbtn.getId()) {// 如果当前点击的和原先的不同
				arg0.setBackgroundResource(R.drawable.blackbtn);
				Lbtn.setBackgroundResource(R.drawable.whitebtn);
				Lbtn = (Button) arg0;
				if (Lbtn.getId() == R.id.weight || Lbtn.getId() == R.id.BMI) {// 选中体重和bmi
					if (day == 0) {// 如果是24小时则改为7天
						day = -6;
						Lbtn2 = btn_sev;
					}
					btn_sev.setBackgroundResource(R.drawable.dc_chartbottom_middle);
					switch (Math.abs(day)) {
					case 0:
						break;
					case 6:
						btn_sev.setBackgroundResource(R.drawable.chart_btn_middle_pressed);
						break;
					case 29:
						btn_month
								.setBackgroundResource(R.drawable.chart_btn_middle_pressed);
						break;
					case 89:
						btn_3month
								.setBackgroundResource(R.drawable.chart_btn_middle_pressed);
						break;
					default:
						break;
					}
					btn_day.setVisibility(View.GONE);
					// div.setVisibility(View.GONE);

				} else {// 选中步数或卡路里
					btn_sev.setBackgroundResource(R.drawable.dc_chartbottom_middle);
					btn_day.setBackgroundResource(R.drawable.dc_chartbottom_middle);
					switch (Math.abs(day)) {
					case 0:
						btn_day.setBackgroundResource(R.drawable.chart_btn_middle_pressed);
						break;
					case 6:
						btn_sev.setBackgroundResource(R.drawable.chart_btn_middle_pressed);
						break;
					case 29:
						btn_month
								.setBackgroundResource(R.drawable.chart_btn_middle_pressed);
						break;
					case 89:
						btn_3month
								.setBackgroundResource(R.drawable.chart_btn_middle_pressed);
						break;
					default:
						btn_day.setBackgroundResource(R.drawable.chart_btn_middle_pressed);
						break;
					}

					btn_day.setVisibility(View.VISIBLE);
					// div.setVisibility(View.VISIBLE);
				}

			}
			getChartData(day);
		} else {// 点击下面的tab
			if (arg0.getId() != Lbtn2.getId()) {
				switch (arg0.getId()) {
				case R.id.chartday:
					arg0.setBackgroundResource(R.drawable.chart_btn_middle_pressed);
					day = 0;
					break;
				case R.id.chartthreem:
					arg0.setBackgroundResource(R.drawable.chart_btn_middle_pressed);
					day = -89;
					break;
				case R.id.chartsev:
					if (Lbtn.getId() == R.id.weight || Lbtn.getId() == R.id.BMI) {
						arg0.setBackgroundResource(R.drawable.chart_btn_middle_pressed);
						day = -6;
						break;
					} else {
						arg0.setBackgroundResource(R.drawable.chart_btn_middle_pressed);
						day = -6;
						break;
					}
				case R.id.chartmonth:
					arg0.setBackgroundResource(R.drawable.chart_btn_middle_pressed);
					day = -29;
					break;

				}
				switch (Lbtn2.getId()) {
				case R.id.chartday:
					Lbtn2.setBackgroundResource(R.drawable.dc_chartbottom_middle);
					break;
				case R.id.chartthreem:
					Lbtn2.setBackgroundResource(R.drawable.dc_chartbottom_middle);
					break;
				case R.id.chartmonth:
					Lbtn2.setBackgroundResource(R.drawable.dc_chartbottom_middle);
					break;
				case R.id.chartsev:
					if (Lbtn.getId() == R.id.weight || Lbtn.getId() == R.id.BMI) {
						Lbtn2.setBackgroundResource(R.drawable.dc_chartbottom_middle);
						break;
					} else {
						Lbtn2.setBackgroundResource(R.drawable.dc_chartbottom_middle);
						break;
					}

				}

				Lbtn2 = (Button) arg0;
				if ((arg0.getId() == R.id.chartmonth || arg0.getId() == R.id.chartthreem)
						&& share.getInt("ISMEMBER", 0) == 0) {

					NoRegisterDialog d = new NoRegisterDialog(getActivity(),
							R.string.no_register, R.string.no_register_content);
					d.show();

				} else {
					getChartData(day);
				}
			}
		}

	}

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				unit.setText(msg.obj.toString());
			} else if (msg.what == 1) {
				detail.setVisibility(View.GONE);
				ArrayList<Integer> oo = (ArrayList<Integer>) msg.obj;
				tv_fast_time.setText(oo.get(5) / 60 + "′" + oo.get(5) % 60
						+ "″");
				tv_fast_step.setText(oo.get(2) + "");
				tv_fast_dis
						.setText(String.format("%.1f"+getString(R.string.km), oo.get(2) * 0.0006f));
				tv_fast_cal.setText(String.format("%.1f"+getString(R.string.unit_cal),
						oo.get(2) * 30f / 1000f));
				tv_mid_time
						.setText(oo.get(4) / 60 + "′" + oo.get(4) % 60 + "″");
				tv_mid_step.setText(oo.get(1) + "");
				tv_mid_dis
						.setText(String.format("%.1f"+getString(R.string.km), oo.get(1) * 0.0006f));
				tv_mid_cal.setText(String.format("%.1f"+getString(R.string.unit_cal),
						oo.get(1) * 30f / 1000f));
				tv_low_time
						.setText(oo.get(3) / 60 + "′" + oo.get(3) % 60 + "″");
				tv_low_step.setText(oo.get(0) + "");
				tv_low_dis
						.setText(String.format("%.1f"+getString(R.string.km), oo.get(0) * 0.0006f));
				tv_low_cal.setText(String.format("%.1f"+getString(R.string.unit_cal),
						oo.get(0) * 30f / 1000f));
			}
			super.handleMessage(msg);
		}

	};

	public void getChartData(int day) {
		Calendar c = Calendar.getInstance();
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
		String end = nowtime;// 结束时间是当前传过来的日期
		c.add(Calendar.DAY_OF_MONTH, day);
		String start = sf.format(Util.getBeforeAfterDate(nowtime, day));// 开始时间是根据按钮选择的时间段

		if (nowtime != null) {
			end = nowtime;
		}
		interval = 1;
		String msg = "";
		switch (Lbtn.getId()) {
		case R.id.step:
			msg = getString(R.string.unit_step) + "";
			if (day == 0) {
				msg = getString(R.string.unit_step) + "/"
						+ getString(R.string.chartinday);
				int[] step = db.getSpRawData(nowtime);
				interval = 6;
				drawchart3(step, getString(R.string.step), interval);
			} else {
				switch (Math.abs(day)) {
				case 6:
					interval = 1;
					break;
				case 29:
					interval = 5;
					break;
				case 89:
					interval = 10;
					break;
				default:
					interval = 15;
					break;
				}

				Map<String, Integer> result = db.getStepBasicData(start, end);

				drawchart(result, t_step, getString(R.string.step), interval,
						start);
				// Gson gson = new Gson();
				// xstr = gson.toJson(new int[]{fs,ms,ss});
				// help.loadUrls(new String[] { "setData2('" + xstr+ "')" });
			}
			target = t_step;
			int ss = 0,
			fs = 0,
			st = 0,
			ft = 0,
			ms = 0,
			mt = 0;
			for (int i = day; i <= 0; i++) {
				String date = Util.getBeforeAfterDate(nowtime, i).toString();
				int[] de = gets(date);
				ss += de[0];
				ms += de[1];
				fs += de[2];
				st += de[3];
				mt += de[4];
				ft += de[5];
			}
			ArrayList<Integer> obj = new ArrayList<Integer>();
			obj.add(ss);
			obj.add(ms);
			obj.add(fs);
			obj.add(st);
			obj.add(mt);
			obj.add(ft);

			handler.obtainMessage(1, obj).sendToTarget();

			break;
		case R.id.distance:
			if (day == 0) {
				msg = (getString(R.string.unit_distance) + "");
				int[] dis = db.getSpDisRawData(nowtime);
				interval = 6;
				drawchart3(dis, getString(R.string.distance), interval);
			} else {
				msg = (getString(R.string.unit_distance) + "");
				switch (Math.abs(day)) {
				case 6:
					interval = 1;
					break;
				case 29:
					interval = 5;
					break;
				case 89:
					interval = 10;
					break;
				default:
					interval = 15;
					break;
				}
				Map<String, Integer> result = db.getDisBasicData(start, end);
				drawchart(result, t_distance, getString(R.string.distance),
						interval, start);

			}
			target = t_distance;
			break;

		case R.id.cal:

			if (day == 0) {
				msg = (getString(R.string.calre) + "");
				int[] cal = db.getSpCalRawData(nowtime);
				interval = 6;
				drawchart3(cal, getString(R.string.cal), interval);
			} else {
				msg = (getString(R.string.unit_cal) + "");
				switch (Math.abs(day)) {
				case 6:
					interval = 1;
					break;
				case 29:
					interval = 5;
					break;
				case 89:
					interval = 10;
					break;
				default:
					interval = 15;
					break;
				}
				Map<String, Integer> result = db.getCalBasicData(start, end);
				drawchart(result, t_cal, getString(R.string.cal), interval,
						start);

			}

			target = t_cal;
			break;

		case R.id.weight:
			msg = (getString(R.string.unit_weight) + "");
			if (day == 0) {
				// db.getPdStepBasicData(from, to);
			} else {
				switch (Math.abs(day)) {
				case 6:
					interval = 1;
					break;
				case 29:
					interval = 5;
					break;
				case 89:
					interval = 10;
					break;
				default:
					interval = 15;
					break;
				}
				Map<String, Double> result = db.getWeightBasicData(start, end);
				drawchart2(result, t_weight, getString(R.string.weight),
						interval, start);

			}

			target = t_weight;
			break;
		case R.id.BMI:
			msg = (getString(R.string.bmi) + "");
			if (day == 0) {
				// db.getPdStepBasicData(from, to);
			} else {
				switch (Math.abs(day)) {
				case 6:
					interval = 1;
					break;
				case 29:
					interval = 5;
					break;
				case 89:
					interval = 10;
					break;
				default:
					interval = 15;
					break;
				}
				Map<String, Double> result = db.getBmiBasicData(start, end);
				drawchart2(result, t_bmi, getString(R.string.bmi), interval,
						start);

			}

			target = t_bmi;
			break;

		default:
			break;
		}
		handler.obtainMessage(0, msg).sendToTarget();

	}

	/***
	 * ss ms fs st mt ft
	 * 
	 * @param time
	 * @return
	 */
	int[] gets(String time) {
		int slowstep = 0;
		int midstep = 0;
		int faststep = 0;
		int slowtimes = 0;
		int fasttimes = 0;
		int midtimes = 0;
		HashMap<Integer, HashMap<String, ArrayList<Integer>>> datas = db
				.parser0802(time);
		for (int i = 0; i < datas.size(); i++) {
			if (datas.containsKey(i)) {
				ArrayList<Integer> step = datas.get(i).get("step");
				ArrayList<Integer> slow = datas.get(i).get("slow");
				ArrayList<Integer> fast = datas.get(i).get("fast");
				ArrayList<Integer> mid = datas.get(i).get("mid");
				for (int j = 0; j < step.size(); j++) {
					int steps = step.get(j);
					if (steps > 0) {
						int slowtime = slow.get(j);
						int fasttime = fast.get(j);
						int midtime = mid.get(j);
						int alltime = slowtime + fasttime + midtime;
						slowtimes += slowtime;
						fasttimes += fasttime;
						midtimes += midtime;
						slowstep += Math.round(steps
								* ((float) slowtime / (float) alltime));
						faststep += Math.round(steps
								* ((float) fasttime / (float) alltime));
						midstep += Math.round(steps
								* ((float) midtime / (float) alltime));
					}
				}
			}
		}
		return new int[] { slowstep, midstep, faststep, slowtimes, midtimes,
				fasttimes };
	}

	public void drawchart(Map<String, Integer> result, Object target,
			String name, int interval, String start) {
		xstr = "";
		ystr = "";
		tip = name;
		SimpleDateFormat sf = new SimpleDateFormat("MM.dd");
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		int count = Math.abs(day) + 1;
		String[] xValues = new String[count];
		String[] tValues = new String[count];
		int[] yValues = new int[count];
		Calendar c = Calendar.getInstance();
		try {
			Date s = df.parse(start);
			c.setTime(s);
			c.add(Calendar.DAY_OF_MONTH, -1);

			for (int i = day; i <= 0; i++) {
				c.add(Calendar.DAY_OF_MONTH, +1);
				yValues[i - day] = 0;
				xValues[i - day] = sf.format(c.getTime());
				tValues[i - day] = df.format(c.getTime());
			}

			for (int i = 0; i < tValues.length; i++) {

				if (result.containsKey(tValues[i])) {
					yValues[i] = (Integer) result.get(tValues[i]);
				}
			}

			Gson gson = new Gson();
			xstr = gson.toJson(xValues);
			ystr = gson.toJson(yValues);
			help.loadUrls(new String[] { "showStepChart('" + xstr + "','"
					+ ystr + "'," + target + ",'" + name + "'," + interval
					+ ")" });
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void drawchart2(Map<String, Double> result, Object target,
			String name, int interval, String start) {
		xstr = "";
		ystr = "";
		tip = name;
		SimpleDateFormat sf = new SimpleDateFormat("MM.dd");
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		int count = Math.abs(day) + 1;
		String[] xValues = new String[count];
		String[] tValues = new String[count];
		Double[] yValues = new Double[count];
		Calendar c = Calendar.getInstance();
		// c.add(Calendar.DAY_OF_MONTH, day - 1);
		try {
			Date s = df.parse(start);
			c.setTime(s);
			c.add(Calendar.DAY_OF_MONTH, -1);

			for (int i = day; i <= 0; i++) {
				c.add(Calendar.DAY_OF_MONTH, +1);
				yValues[i - day] = 0.0;
				xValues[i - day] = sf.format(c.getTime());
				tValues[i - day] = df.format(c.getTime());
			}

			for (int i = 0; i < tValues.length; i++) {
				if (result.containsKey(tValues[i])) {
					yValues[i] = (Double) result.get(tValues[i]);
				}
			}
			if (name.equals(getString(R.string.weight))) {
				for (int i = 0; i < yValues.length; i++) {
					if (yValues[i] <= 0) {
						yValues[i] = db.getWeightRecently(tValues[i]);
					}
				}
			}
			Gson gson = new Gson();
			xstr = gson.toJson(xValues);
			ystr = gson.toJson(yValues);
			help.loadUrls(new String[] { "showStepChart('" + xstr + "','"
					+ ystr + "'," + target + ",'" + name + "'," + interval
					+ ")" });
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void drawchart3(int[] step, String name, int interval) {
		xstr = "";
		ystr = "";
		tip = name;
		String[] xValues = new String[72];

		int[] s = new int[24];
		for (int i = 0; i < 24; i++) {
			xValues[i] = i + "h";
			s[i] = step[i * 3] + step[(i * 3) + 1] + step[(i * 3) + 2];
		}

		Gson gson = new Gson();
		xstr = gson.toJson(xValues);
		ystr = gson.toJson(s);
		help.loadUrls(new String[] { "showStepChart('" + xstr + "','" + ystr
				+ "'," + 10000 + ",'" + name + "'," + 2 + ")" });

	}

	public void zoomChart(View view) {
		// FragmentManager ft = getSupportFragmentManager();
		//
		// DialogFragment newFragment =
		// DataChartDialogFragment.newInstance(xstr,
		// ystr, target, tip, interval);
		// newFragment.show(ft, "chart_dialog");
	}

	void get3MonthSpData() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
					String cutTime = s.format(new Date());
					Map<String, String> args = new HashMap<String, String>();
					args.put("session", share.getString("session_id", null));
					args.put("end", nowtime);
					String begin = Util.getBeforeAfterDate(cutTime, -92)
							.toString();
					args.put("begin", begin);
					String data3Month = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/data/getstep", args);
					JSONObject content = new JSONObject(data3Month);
					if (content.getString("content").length() < 5)
						return;
					SQLiteDatabase base = db.beginTransaction();// 开始事务
					try {
						JSONArray jsonArray = content.getJSONArray("content");
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject jsobj = jsonArray.getJSONObject(i);
							db.SaveSpBasicData(jsobj.getInt("step"),
									jsobj.getInt("distance"),
									jsobj.getInt("calorie"), 0,
									jsobj.getString("time"), cutTime);

						}
						// share.edit().putString("last3monthupdate_start",
						// begin);
						// share.edit().putString("last3monthupdate_end",
						// nowtime);
						db.setTransactionSuccessful(base);
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						db.endTransaction(base);
						myHandler.sendEmptyMessage(0);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

				}
			}
		}).start();
	}

	private Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case 0:
				getChartData(day);
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		share = getActivity().getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		db = new Dbutils(share.getInt("UID", -1), getActivity());
		int deviceType = db.getUserDeivceType();
		getUserProfile();
		Object[] target = db.getUserTarget();
		if (target != null) {
			// _id,uid,type,step,weightbefore,weightend,ifweight,ifsleep,ifbmi
			t_step = (Integer) target[3];
			t_distance = (int) (t_step * 0.75);
			t_cal = (int) ((t_step * 32) / 1000.0);
			t_weight = (Double) target[5];
			t_bmi = t_weight / (t_height / 100) / (t_height / 100);

		} else {
			t_step = 0;
			t_cal = 0;
			t_distance = 0;
			t_weight = 0;
			t_bmi = 0;
		}
		getChartData(day);

		if (db.CheckSpRawData(nowtime) < 0) {
			getRawStep("2013-05-29");
		}
		if (db.checkSpBasicData(threeday) < 0) {
			get3MonthSpData();
		}

		if (deviceType == 5) {
			if (db.CheckSpRawData(nowtime) < 0) {
				getRawStep(nowtime);
			}
		}

		super.onResume();
	}

	public void refreshData() {

		// TODO Auto-generated method stub
		share = getActivity().getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		db = new Dbutils(share.getInt("UID", -1), getActivity());
		getUserProfile();
		Object[] target = db.getUserTarget();
		if (target != null) {
			// _id,uid,type,step,weightbefore,weightend,ifweight,ifsleep,ifbmi
			t_step = (Integer) target[3];
			t_cal = (int) (t_step * 0.75);
			t_distance = (int) (t_step * 32);
			t_weight = (Double) target[5];
			t_bmi = t_weight / (t_height / 100) / (t_height / 100);

		} else {
			t_step = 0;
			t_cal = 0;
			t_distance = 0;
			t_weight = 0;
			t_bmi = 0;
		}
		int deviceType = db.getUserDeivceType();
		if (deviceType == 1 || deviceType == 4) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						Map<String, String> args = new HashMap<String, String>();
						args.put("session", share.getString("session_id", null));
						args.put("day", nowtime);
						String dayraw = HttpUtlis.getRequest(HttpUtlis.BASE_URL
								+ "/data/getRawStep", args);
						JSONObject content = new JSONObject(dayraw);
						if (content.getString("content").length() < 5)
							return;
						db = new Dbutils(share.getInt("UID", -1),
								 getActivity());
						JSONArray jsonArray = content.getJSONArray("content");
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONArray item = jsonArray.getJSONArray(i);
							String t = (String) item.get(0);
							String data = (String) item.get(1);
							int id = db.CheckSpRawData(t);
							if (id > 0) {
								db.deleteSpRawData(id);
							}
							db.SaveSpRawData(t, data, 2);
						}

						SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
						String cutTime = s.format(new Date());
						Map<String, String> args1 = new HashMap<String, String>();
						args1.put("session",
								share.getString("session_id", null));
						args1.put("end", nowtime);
						String begin = Util.getBeforeAfterDate(nowtime, -92)
								.toString();
						args1.put("begin", begin);
						String data3Month = HttpUtlis.getRequest(
								HttpUtlis.BASE_URL + "/data/getstep", args1);
						content = new JSONObject(data3Month);
						// if (content.getString("content").length() < 5)
						// return;
						SQLiteDatabase base = db.beginTransaction();// 开始事务
						jsonArray = content.getJSONArray("content");
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject jsobj = jsonArray.getJSONObject(i);
							db.SaveSpBasicData(jsobj.getInt("step"),
									jsobj.getInt("distance"),
									jsobj.getInt("calorie"), 0,
									jsobj.getString("time"), cutTime);

						}
						db.setTransactionSuccessful(base);
						db.endTransaction(base);
						refreshHandler.sendEmptyMessage(0);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						refreshHandler.sendEmptyMessage(0);
						e.printStackTrace();

					}
				}
			}).start();
		} else if (deviceType == 5) {
			getRawStep(nowtime);
		} else {
			refreshHandler.sendEmptyMessage(0);
		}
	}

	public void getRawStep(final String date) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Map<String, String> args = new HashMap<String, String>();
					args.put("session", share.getString("session_id", null));
					// args.put("session","14037984006");
					args.put("day", date);
					String dayraw = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/data/getRawStep", args);
					JSONObject content = new JSONObject(dayraw);
					if (content.getString("content").length() < 20) {
						Toast.makeText(getActivity(),
								R.string.no_detail_step, Toast.LENGTH_SHORT)
								.show();
						return;
					}

					db = new Dbutils(share.getInt("UID", -1),
							getActivity());
					JSONArray jsonArray = content.getJSONArray("content");
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONArray item = jsonArray.getJSONArray(i);
						String t = (String) item.get(0);
						String data = (String) item.get(1);
						int id = db.CheckSpRawData(t);
						if (id > 0) {
							db.deleteSpRawData(id);
						}
						db.SaveSpRawData(t, data, 2);
					}
					myHandler.sendEmptyMessage(0);
				} catch (Exception e) {
					getChartData(day);
					// TODO Auto-generated catch block
					e.printStackTrace();

				}
			}
		}).start();

	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		refreshData();
	}

	private Handler refreshHandler = new Handler() {
		public void dispatchMessage(Message msg) {
			switch (msg.what) {
			case 0:
				mSwipeLayout.setRefreshing(false);
				getChartData(day);
				break;

			default:
				break;
			}

		}

	};

 

}
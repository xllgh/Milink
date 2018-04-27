package com.bandlink.air.club;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bandlink.air.MilinkApplication;
import com.bandlink.air.R;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.DbContract.SleepBoundsDetail;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.MainInterface;
import com.bandlink.air.util.MyDate;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Util;
import com.bandlink.air.view.SleepChart;
import com.bandlink.air.view.SleepChart.OnTouchDataAreaListener;
import com.nineoldandroids.animation.ObjectAnimator;

public class SleepActivity extends Fragment {

	TextView count_hour, count_min, // 睡眠总时长的 时 分
			sleep_score, deep_hour, // 睡眠质量 深度小时
			deep_min, awake_times, // 深度分钟 醒来次数
			s_hour, s_min, start_sleep, // 浅睡眠小时 浅睡眠分钟 入睡时间
			end_sleep, touchtime, lineTips; // 醒来时间
	private TextView date, black_layer;// 中间显示时间/覆盖层
	LinearLayout linear;
	Context mContext;
	Dbutils db;
	SleepChart sChart;
	SharedPreferences share, appShare;
	int timeFrom, timeTo;
	String yesterday;
	public static final int awakeStart = 4;
	public static final int awakeEnd = 11;
	// 该值一下是轻度睡眠
	public static final int lightStep = 20;

	public void centerText(String s) {
		date.setVisibility(View.VISIBLE);
		black_layer.setVisibility(View.VISIBLE);
		date.setText(s);
	}

	private Handler ctrlCenter = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			if (date == null) {
				return;
			}
			if (date.getVisibility() == View.VISIBLE) {
				date.setVisibility(View.GONE);
				black_layer.setVisibility(View.GONE);
			}
			super.handleMessage(msg);
		}

	};
	private View mView;
	ActionbarSettings actionBar;

	public View onCreateView(android.view.LayoutInflater inflater,
			android.view.ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.sleep, null);
		actionBar = new ActionbarSettings(mView, new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showPop();
			}
		}, interf);
		actionBar.setTitle(R.string.sleepdata);
		actionBar.setTopRightIcon(R.drawable.histroy);
		appShare = getActivity().getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		sChart = (SleepChart) mView.findViewById(R.id.chart);

		db = new Dbutils(appShare.getInt("UID", -1), getActivity());
		sChart.setOnTouchDataAreaListener(new OnTouchDataAreaListener() {

			@Override
			public void OnTouchDataListener(float offx, String time,
					boolean isMove) {
				// TODO Auto-generated method stub
				lineTips.setVisibility(View.VISIBLE);
				ObjectAnimator o2 = ObjectAnimator.ofFloat(lineTips, "x", offx);
				if (offx < touchtime.getPaint().measureText(time)) {
					offx = touchtime.getPaint().measureText(time) + 10;
				}
				ObjectAnimator o = ObjectAnimator.ofFloat(touchtime, "x", offx
						- touchtime.getPaint().measureText(time));
				touchtime.setText(time);

				if (isMove) {
					o.setDuration(0);
					o2.setDuration(0);
				}
				o.start();
				o2.start();

			}

			@Override
			public void OnDateCalcComplete(String f, String t, int sleepcount,
					int aweakcount, int times, int deep) {
				// TODO Auto-generated method stub

				Long second = (long) (sleepcount + deep + aweakcount);
				int hour = (int) (second / 3600);
				int min = (int) (second % 3600) / 60;

				int hour_d = (int) (deep / 3600);
				int min_d = (int) (deep % 3600) / 60;

				int hour_s = (int) ((sleepcount) / 3600);
				int min_s = (int) ((sleepcount) % 3600) / 60;
				count_hour.setTypeface(MilinkApplication.NumberFace);
				count_min.setTypeface(MilinkApplication.NumberFace);
				deep_hour.setTypeface(MilinkApplication.NumberFace);
				deep_min.setTypeface(MilinkApplication.NumberFace);
				s_hour.setTypeface(MilinkApplication.NumberFace);
				s_min.setTypeface(MilinkApplication.NumberFace);
				awake_times.setTypeface(MilinkApplication.NumberFace);
				start_sleep.setTypeface(MilinkApplication.NumberFace);
				end_sleep.setTypeface(MilinkApplication.NumberFace);
				sleep_score.setTypeface(MilinkApplication.NumberFace);
				count_hour.setText(hour + "");
				count_min.setText(min + "");
				deep_hour.setText(hour_d + "");
				deep_min.setText(min_d + "");
				s_hour.setText(hour_s + "");
				s_min.setText(min_s + "");
				awake_times.setText(times + "");
				start_sleep.setText(f);
				end_sleep.setText(t);
				if (hasdata) {
					if (sleepcount + deep + aweakcount == 0) {
						sleep_score.setText("0");
						return;
					}
					mView.findViewById(R.id.unit_score).setVisibility(
							View.VISIBLE);
					sleep_score.setText(db.getSleepDetailScore(yesterday));
				} else {
					db.saveSleepMsg(yesterday,
							SleepActivity.this.getString(R.string.notworn));
					sleep_score.setText(R.string.notworn);
					mView.findViewById(R.id.unit_score)
							.setVisibility(View.GONE);
				}

			}
		});
		  
		initView();

		 
		// 下部时间选择
		linear = (LinearLayout) mView.findViewById(R.id.date);
		return mView;
	};

	private void showPop() {
		View root = getActivity().getLayoutInflater().inflate(
				R.layout.sleepbounds, null);
		final PopupWindow pop = new PopupWindow(root,
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true);
		pop.setOutsideTouchable(false);

		final TextView r1 = (TextView) pop.getContentView().findViewById(
				R.id.t1);
		final TextView r2 = (TextView) pop.getContentView().findViewById(
				R.id.t2);
		final TextView r3 = (TextView) pop.getContentView().findViewById(
				R.id.t3);
		final TextView r4 = (TextView) pop.getContentView().findViewById(
				R.id.t4);
		ArrayList<HashMap<String, String>> detail = db
				.getBoundsRecord(yesterday);

		for (int i = 0; i < detail.size(); i++) {
			switch (i) {
			case 0:
				r1.setText(detail.get(i).get(SleepBoundsDetail.INTRO));
				break;
			case 1:
				r2.setText(detail.get(i).get(SleepBoundsDetail.INTRO));
				break;
			case 2:
				r3.setText(detail.get(i).get(SleepBoundsDetail.INTRO));
				break;
			case 3:
				r4.setText(detail.get(i).get(SleepBoundsDetail.INTRO));
				break;
			}
		}

		r1.startAnimation(AnimationUtils.loadAnimation(getActivity(),
				R.anim.toright_delay0));
		r2.startAnimation(AnimationUtils.loadAnimation(getActivity(),
				R.anim.toleft_delay200));
		r3.startAnimation(AnimationUtils.loadAnimation(getActivity(),
				R.anim.toright_delay300));
		r4.startAnimation(AnimationUtils.loadAnimation(getActivity(),
				R.anim.toleft_delay400));
		pop.showAtLocation(pos.getRootView(), Gravity.CENTER, 0, 0);
		root.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				pop.dismiss();
			}
		});
	}

	private boolean sameDay;

	public boolean isSameDay(int f, int t) {
		if (t - f > 0) {
			return true;
		} else {
			return false;
		}
	}

	public static byte[] formatStringSleep2Byte(String ss) {
		String[] data = ss.split("_");
		byte[] x = new byte[data.length];
		for (int i = 0; i < x.length; i++) {
			char[] hexChars = data[i].toCharArray();
			if (hexChars.length == 1) {
				char temp = hexChars[0];
				hexChars = new char[2];

				hexChars[0] = '0';
				hexChars[1] = temp;
			}
			try {
				x[i] = (byte) (Dbutils.charToByte(hexChars[0]) << 4 | Dbutils
						.charToByte(hexChars[1]));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return x;
	}

	boolean first_;
	int awakeIndex = 0;
	int sleepIndex = 0;
	int realFrom = 0;
	int realTo = 0;
	int te[] = new int[2];
	int fal = 0;
	String[] test = {};
	boolean hasdata = false;

	public static byte[] tranStr(String[] data) {
		byte[] x = new byte[data.length];
		for (int i = 0; i < x.length; i++) {
			char[] hexChars = data[i].toCharArray();
			if (hexChars.length == 1) {
				char temp = hexChars[0];
				hexChars = new char[2];

				hexChars[0] = '0';
				hexChars[1] = temp;
			}
			x[i] = (byte) (Dbutils.charToByte(hexChars[0]) << 4 | Dbutils
					.charToByte(hexChars[1]));
		}
		return x;
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		share = getActivity().getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		db = new Dbutils(share.getInt("UID", -1), getActivity());
		share = getActivity().getSharedPreferences("air", Context.MODE_PRIVATE);
		first_ = share.getBoolean("first_", true);
		yesterday = MyDate.getYesterDay();
		// -----------------------------------------------------
		loadSleep(yesterday);
		super.onResume();
	}

	public void loadSleep(String yesterday) {
		byte[] sData = db.getSleepDetail(yesterday);
		String s = db.getSleepDetailScore(yesterday);

		if (sData != null && sData.length == 360) {
			hasdata = sChart.setSleepData(sData);
		}
	}

	/***
	 * 根据在数组中的位置推算该位置的时间
	 * 
	 * @param index
	 *            索引
	 * @return 时间
	 */
	private String getTime(int index) {
		int hour = sameDay ? (index * 4) / 60 : (index * 4) / 60 + 12;
		int min = (index * 4) % 60;
		if (hour > 24) {
			hour -= 24;
		}
		return (hour < 10 ? "0" + hour : hour) + ":"
				+ (min < 10 ? "0" + min : min);
	}

	/***
	 * 分析醒来的时间
	 * 
	 * @param all
	 *            24小时数据
	 * @param to
	 *            参考醒来的时间点
	 * @return 得出推算的醒来点的索引
	 */
	private int endIndex(byte[] all, int to) {
		int toIndex = sameDay ? to * 15 : (to + 12) * 15;
		if (toIndex >= 360) {
			toIndex = 359;
		}
		if (all[toIndex] == -86) {
			// 那点醒来，有前一个小时到现在 第一个醒来的点
			int temps = toIndex - 15;
			if (temps < 0) {
				temps = 0;
			}
			for (int x = temps; x <= toIndex; x++) {
				if (all[x] == -86) {
					return x + 1;
				}
			}
		} else {
			int temps = toIndex - 15;
			if (temps < 0) {
				temps = 0;
			}
			for (int x = temps; x <= toIndex; x++) {
				// 由一个小时前向后找，没有就交给后面代码处理
				if (all[x] == -86) {
					return x + 1;
					// int z =x-15;
					// if(z<0){
					// z = 0;
					// }
					// for(int xs = z;xs<=toIndex;xs++){
					// if(all[xs]==-86){
					// return xs+2;
					// }
					// }
				}
			}

			int toRight = toIndex + 15;
			if (toRight >= 360) {
				toRight = 359;
			}
			for (int z = toIndex; z < toRight; z++) {
				if (all[z] == -86) {
					return z + 1;
				}/*
				 * else{ String temp = Integer.toBinaryString(all[z] ); int len
				 * = temp.length(); if (len < 8 || all[z] < 0) { temp =
				 * "00000000" + temp; temp = temp.substring(len, len + 8); } int
				 * i =0; for (int j = 0; j < 4; j++) { String s =
				 * temp.substring(j * 2, j * 2 + 2); if(s.equals("10")){ i++; }
				 * } if(i>=2){ System.out.println(i); return (z)>360?360:z; } }
				 */
			}
		}
		return (toIndex + 1) >= 360 ? 359 : (toIndex + 1);
	}

	/***
	 * 同 endIndex方法
	 * 
	 * @param all
	 * @param from
	 * @return
	 */
	private int startIndex(byte[] all, int from) {
		int indexFrom = sameDay ? from * 15 : (from - 12) * 15;
		if (indexFrom < 0) {
			indexFrom = 0;
		}
		// 该点是醒的 就有后两个小时向前走 遇到第一个醒的为入睡点
		if (all[indexFrom] == -86 || true) {
			// 那一刻是活动的 就由前一小时开始找d
			int temps = indexFrom + 30;
			if (temps >= 360) {
				temps = 359;
			}
			// int flag =0;
			for (int x = temps; x >= indexFrom; x--) {
				// int temp[]=new int[8];
				// int num=(all[x] & 0xff);
				// for (int i=0;i<8;i++){
				// if ((num & 0x80) == 0x80){
				// temp[i]=1;
				// }else{
				// temp[i]=0;
				// }
				// num<<=1;
				// }
				if (all[x] == -86) {
					return x - 1;
				}
			}
			/*
			 * for(int z=indexFrom;z<temps;z++){
			 * System.out.println("---"+all[z]); if(all[z] == -86){ return
			 * (z-2)<0?0:z-2; }else{ String temp = Integer.toBinaryString(all[z]
			 * ); int len = temp.length(); if (len < 8 || all[z] < 0) { temp =
			 * "00000000" + temp; temp = temp.substring(len, len + 8); } int
			 * i=0; for (int j = 0; j < 4; j++) { String s = temp.substring(j *
			 * 2, j * 2 + 2); if(s.equals("10")){ i++; } } if(i>=2){
			 * System.out.println(i); return (z-2)<0?0:z-2; } } }
			 */
		} else {
			// 那一点是睡眠的

			// int temps = indexFrom - 15;
			// if (temps < 0) {
			// temps = 0;
			// }
			// for (int z = indexFrom; z >= temps; z--) {
			// if (all[z] == -86) {
			// return z;
			// }/*
			// * else{ String temp = Integer.toBinaryString(all[z] ); int len
			// * = temp.length(); if (len < 8 || all[z] < 0) { temp =
			// * "00000000" + temp; temp = temp.substring(len, len + 8); } int
			// * i=0; for (int j = 0; j < 4; j++) { String s =
			// * temp.substring(j * 2, j * 2 + 2); if(s.equals("10")){ i++; }
			// * } if(i>=2){ System.out.println(i); return (z-2)<0?0:z-2; } }
			// */
			// }
		}
		return (indexFrom - 1) < 0 ? 0 : (indexFrom - 1);
	}

	private RelativeLayout pos;
	private LinearLayout pos2;

	private void initView() {
		count_hour = (TextView) mView.findViewById(R.id.count_hour);
		count_min = (TextView) mView.findViewById(R.id.count_min);
		sleep_score = (TextView) mView.findViewById(R.id.sleep_score);
		deep_hour = (TextView) mView.findViewById(R.id.deep_hour);
		deep_min = (TextView) mView.findViewById(R.id.deep_min);
		awake_times = (TextView) mView.findViewById(R.id.awake_times);
		s_hour = (TextView) mView.findViewById(R.id.s_hour);
		s_min = (TextView) mView.findViewById(R.id.s_min);
		start_sleep = (TextView) mView.findViewById(R.id.startsleep);
		end_sleep = (TextView) mView.findViewById(R.id.endsleep);
		touchtime = (TextView) mView.findViewById(R.id.touchtime);
		lineTips = (TextView) mView.findViewById(R.id.lineTips);
		date = (TextView) mView.findViewById(R.id.date1);
		black_layer = (TextView) mView.findViewById(R.id.black_layer);
		pos = (RelativeLayout) mView.findViewById(R.id.pos);
		pos2 = (LinearLayout) mView.findViewById(R.id.pos2);

	}

	boolean one = true;
	MainInterface interf;

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		if (first_) {
			one = false;
			first_ = false;
			showGuide();
			share.edit().putBoolean("first_", false).commit();
		} else {
			if (one) {
				one = false;
				handler.obtainMessage().sendToTarget();
				;
			}
		}
		interf = (MainInterface) activity;
		super.onAttach(activity);
	}

	int flag = 1;

	@SuppressLint("NewApi")
	void showGuide() {
		View root = getActivity().getLayoutInflater().inflate(
				R.layout.sleep_guide, null);
		final PopupWindow pop = new PopupWindow(root,
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true);
		pop.setOutsideTouchable(false);
		pop.showAtLocation(pos.getRootView(), Gravity.CENTER, 0, 0);
		final RelativeLayout r1 = (RelativeLayout) pop.getContentView()
				.findViewById(R.id.l1);
		final RelativeLayout r2 = (RelativeLayout) pop.getContentView()
				.findViewById(R.id.l2);
		final RelativeLayout r3 = (RelativeLayout) pop.getContentView()
				.findViewById(R.id.l3);
		pos.setBackgroundResource(R.drawable.corner_border_red);
		ObjectAnimator o1 = ObjectAnimator.ofFloat(r1, "alpha", 1);
		final ObjectAnimator o2 = ObjectAnimator.ofFloat(r2, "alpha", 1);
		final ObjectAnimator o3 = ObjectAnimator.ofFloat(r3, "alpha", 1);
		o1.start();

		root.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				switch (flag) {
				case 1:
					o2.start();
					r1.setAlpha(0);
					r1.setVisibility(View.GONE);
					pos.setBackground(new BitmapDrawable());
					pos2.setBackgroundResource(R.drawable.corner_border_red);
					flag++;
					break;
				case 2:
					o3.start();
					r2.setAlpha(0);
					flag++;
					pos2.setBackground(new BitmapDrawable());
					r3.setBackgroundResource(R.drawable.corner_border_red);
					break;
				case 3:
					pop.dismiss();
					handler.obtainMessage().sendToTarget();
					break;
				}
			}
		});
	}

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			// if(sleepIndex==0 || awakeIndex==0){
			//
			// AlertDialog.Builder ab = new
			// AlertDialog.Builder(Build.VERSION.SDK_INT<11?SleepActivity.this:new
			// ContextThemeWrapper(SleepActivity.this,
			// android.R.style.Theme_Holo_Light));
			// ab.setTitle(R.string.warning);
			// ab.setMessage(R.string.nosleepdata);
			// ab.setPositiveButton(R.string.ok, null);
			// ab.create().show();
			//
			// }
			super.handleMessage(msg);
		}

	};

	/***
	 * 转化long时间值为可读字符串
	 * 
	 * @param time
	 *            long值
	 * @param format
	 *            格式化格式
	 * @return 字符
	 */
	private String shiftTime(Long time, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(new Date(time));
	}

}

package com.bandlink.air.simple;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.TextView;

import com.bandlink.air.MenuFragment;
import com.bandlink.air.MilinkApplication;
import com.bandlink.air.R;
import com.bandlink.air.club.BorderImageView;
import com.bandlink.air.simple.ptr.PtrDefaultHandler;
import com.bandlink.air.simple.ptr.PtrFrameLayout;
import com.bandlink.air.simple.ptr.PtrHandler;
import com.bandlink.air.simple.ptr.StoreHouseHeader;
import com.bandlink.air.util.AsynImagesLoader;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.LovefitSlidingActivity.CallFragment;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.view.SleepChart;
import com.nostra13.universalimageloader.core.ImageLoader;

public class AchievementActivity extends LovefitActivity implements
		OnClickListener, CallFragment {
	private AsynImagesLoader imageloader;
	private SharedPreferences share;
	private BorderImageView photo;
	private String date;
	private Dbutils dbutils;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		share = getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		setContentView(R.layout.achievement_fragment);
		date = getIntent().getStringExtra("date");
		dbutils = new Dbutils(share.getInt("UID", -1), this);
		photo = (BorderImageView) findViewById(R.id.photo);
		findViewById(R.id.barb).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				AchievementActivity.this.finish();
				overridePendingTransition(R.anim.dismiss_fall_fall,
						R.anim.dismiss_fall);
			}
		});
		findViewById(R.id.bar).setAnimation(
				AnimationUtils.loadAnimation(this, R.anim.redown));
		ImageLoader.getInstance().displayImage(
				HttpUtlis.AVATAR_URL
						+ MenuFragment.getAvatarUrl(share.getInt("UID", -1)
								+ "", "big"), photo,
				MilinkApplication.getListOptions());
		final PtrFrameLayout ptrFrameLayout = (PtrFrameLayout) findViewById(R.id.refresh);
		StoreHouseHeader header = new StoreHouseHeader(this);
		header.setPadding(0, 25, 0, 25);
		header.initWithString(getString(R.string.app_name));
		header.setTextColor(getResources().getColor(R.color.simple_blue));
		ptrFrameLayout.setDurationToCloseHeader(1500);
		ptrFrameLayout.setHeaderView(header);
		ptrFrameLayout.addPtrUIHandler(header);
		ptrFrameLayout.setPtrHandler(new PtrHandler() {
			@Override
			public boolean checkCanDoRefresh(PtrFrameLayout frame,
					View content, View header) {
				return PtrDefaultHandler.checkContentCanBePulledDown(frame,
						content, header);
			}

			@Override
			public void onRefreshBegin(PtrFrameLayout frame) {
				ptrFrameLayout.postDelayed(new Runnable() {
					@Override
					public void run() {
						ptrFrameLayout.refreshComplete();
					}
				}, 1500);
			}
		});
		int[] detail = dbutils.getSpRawDataSleep(date);
		int fromindex = -1, toindex = 0;
		int tempStep = 0;
		int all = 0;
		boolean flag = true;
		// detail[0] =22;
		// detail[1] =11;
		// detail[2] =11;
		// detail[3] =22;
		// detail[88] =22;
		// detail[89] =110;
		// detail[90] =110;
		// detail[91] =220;
		// detail[170] =1000;
		// detail[171] =1000;
		// detail[172] =1000;
		// detail[240] =1000;
		ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
		boolean lastEmpty = false;
		int emptyStep = 0;
		for (int i = 0; i < detail.length; i++) {
			if (detail[i] == -1 && flag) {
				// continue;
			} else if (detail[i] > 200) {
				flag = false;
				if (lastEmpty) {
					lastEmpty = false;
					arrBuild(data, fromindex, toindex, emptyStep);
					toindex = 0;
					fromindex = -1;
					emptyStep = 0;
					{
						if (fromindex == -1) {
							fromindex = i;
						}
						toindex = i;
						tempStep += detail[i] == -1 ? 0 : detail[i];
						all += detail[i];
					}

				} else {
					if (fromindex == -1) {
						fromindex = i;
					}
					toindex = i;
					tempStep += detail[i] == -1 ? 0 : detail[i];
					all += detail[i];
					if (detail[i] == -1) {
						for (int j = i; j > 0; j--) {
							if (detail[j] != 0) {
								toindex = j;
								break;
							}
						}
						arrBuild(data, fromindex, toindex, tempStep);
						toindex = 0;
						fromindex = -1;
						tempStep = 0;
						flag = true;
						continue;
					}
				}
			} else {
				flag = false;
				if (tempStep != 0) {
					arrBuild(data, fromindex, toindex, tempStep);
					toindex = 0;
					fromindex = -1;
					tempStep = 0;
					// 当前索引
					{
						lastEmpty = true;
						if (fromindex == -1) {
							fromindex = i;
						}
						toindex = i;
						emptyStep += detail[i] == -1 ? 0 : detail[i];
						all += detail[i];
					}
				} else {
					lastEmpty = true;
					if (fromindex == -1) {
						fromindex = i;
					}
					toindex = i;
					emptyStep += detail[i] == -1 ? 0 : detail[i];
					all += detail[i];
					//
					if (detail[i] == -1) {
						for (int j = i; j > 0; j--) {
							toindex = j;
							if (detail[j] > 0) {
								break;
							}
						}
						lastEmpty = false;
						arrBuild(data, fromindex, toindex, emptyStep);
						toindex = 0;
						fromindex = -1;
						emptyStep = 0;
						flag = true;
						continue;
					}
				}
			}
		}

		TextView step = ((TextView) findViewById(R.id.count));
		step.setTypeface(Typeface.createFromAsset(getAssets(),
				"font/AvenirLTStd-Light.otf"));
		Object[] steprs = dbutils.getSpBasicData(date);
		String str = "0";
		if (steprs != null && steprs.length > 0) {
			str = steprs[0].toString();
		}
		step.setText(str);
		TextView aim = ((TextView) findViewById(R.id.aim));
		aim.setTypeface(Typeface.createFromAsset(getAssets(),
				"font/AvenirLTStd-Light.otf"));
		try {
			aim.setText("of " + dbutils.getUserTarget()[3].toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			aim.setText("of 10000");
		}
		ListView list = (ListView) findViewById(R.id.list);

		list.setAdapter(new TimeLineAdapter(this, data, detail));
		super.onCreate(savedInstanceState);
	}

	private void arrBuild(ArrayList<HashMap<String, Object>> src,
			int fromindex, int toindex, int step) {
		if (fromindex >= toindex && step == 0) {
			return;
		}
		HashMap<String, Object> map1 = new HashMap<String, Object>();
		String str = "";
		// run

		float speed = (step / (((float) (toindex - fromindex) * 5) == 0 ? 5
				: ((float) (toindex - fromindex) * 5)));
		if (speed < 30) {
			// 静止

			if (fromindex <= 48) {
				// 睡觉时的
				map1.put("flag",getString(R.string.ach_stay));
				map1.put("icon", R.drawable.sleep);
			} else {
				map1.put("flag", getString(R.string.ach_morestep));
				map1.put("icon", R.drawable.tar_low_dark);
			}
		} else if (speed >= 30 && speed < 90) {
			// 压马路
			map1.put("flag", String.format(getString(R.string.ach_slow), step));
			map1.put("icon", R.drawable.tar_middle_dark);
		} else if (speed >= 90 && speed < 180) {
			// 快走
			map1.put("flag", String.format(getString(R.string.ach_fast), step));
			map1.put("icon", R.drawable.tar_height_dark);
		} else {
			// 跑
			if ((toindex - fromindex) < 2) {
				// 耐力不足
				map1.put("flag", String.format(getString(R.string.ach_run), step));
				map1.put("icon", R.drawable.tar_height_light);
			} else {
				// 继续努力
				map1.put("flag", String.format(getString(R.string.ach_good), step));
				map1.put("icon", R.drawable.tar_height_light);
			}
		}

		str = "";
		map1.put("name", minToTime(fromindex * 5) + " / "
				+ minToTime((toindex == fromindex ? toindex + 1 : toindex) * 5)
				+ str);
		map1.put("detail", step);
		map1.put("date", date);
		src.add(map1);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			this.finish();
			overridePendingTransition(R.anim.dismiss_fall_fall,
					R.anim.dismiss_fall);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public String minToTime(int min) {
		String str = SleepChart.transformH(min / 60) + ":"
				+ SleepChart.transformM(min % 60);
		return str;
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void callFragment(int step, int dis, int cal, String action) {
		// TODO Auto-generated method stub

	}

	@Override
	public void makeOrder(int code) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub

	}

}

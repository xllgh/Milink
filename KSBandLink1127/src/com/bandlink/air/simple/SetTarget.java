package com.bandlink.air.simple;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bandlink.air.R;
import com.bandlink.air.SportsDevice;
import com.bandlink.air.simple.SeekCircle.OnSeekCircleChangeListener;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.MApplication;
import com.bandlink.air.util.SharePreUtils;

public class SetTarget extends Activity implements OnClickListener {
	private ActionbarSettings actionBar;
	private TextView msg, cal;
	private SeekCircle tView;
	private RadioGroup tar_steps;
	private RadioButton low, middle, height;
	private Button next;
	private int stepTar;
	private String session_id;
	private SharedPreferences share;
	private Dbutils db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setContentView(R.layout.settargat);
		share = getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		db = new Dbutils(share.getInt("UID", -1), this);
		session_id = getIntent().getStringExtra("session_id");
		actionBar = new ActionbarSettings(this, new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				SetTarget.this.finish();
			}
		}, null);
		actionBar.setTopLeftIcon(R.drawable.ic_top_arrow);
		actionBar.setTitle(R.string.target_setting);
		tar_steps = (RadioGroup) findViewById(R.id.tar_steps);
		low = (RadioButton) findViewById(R.id._low);
		middle = (RadioButton) findViewById(R.id._middle);
		height = (RadioButton) findViewById(R.id._height);
		next = (Button) findViewById(R.id.next);
		next.setOnClickListener(this);
		ColorStateList csl = (ColorStateList) getResources().getColorStateList(
				R.drawable.selector_color);
		height.setTextColor(csl);
		middle.setTextColor(csl);
		low.setTextColor(csl);
		low.setOnClickListener(this);
		middle.setOnClickListener(this);
		height.setOnClickListener(this);
		msg = (TextView) findViewById(R.id.msg);
		cal = (TextView) findViewById(R.id.cal);
		tView = (SeekCircle) findViewById(R.id.tar);
		tView.setOnSeekCircleChangeListener(new OnSeekCircleChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekCircle seekCircle) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStartTrackingTouch(SeekCircle seekCircle) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekCircle seekCircle, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				stepTar = Math.round((progress / 100f) * 20000);
				msg.setText(stepTar + "");
				cal.setText(Math.round((progress / 100f) * 20000 * 0.033333f)
						+ "");
				if (stepTar <= 5000) {
					low.setChecked(true);
				} else if (stepTar <= 10000) {
					middle.setChecked(true);
				} else if (stepTar >= 15000) {
					height.setChecked(true);
				} else if (stepTar <= 15000) {
					middle.setChecked(true);
				}
			}
		});
		tView.setProgress(50);
		MApplication.getInstance().addActivity(this);
		super.onCreate(savedInstanceState);
	}
@Override
protected void onDestroy() {
	// TODO Auto-generated method stub
	MApplication.getInstance().remove(this);
	super.onDestroy();
}
	class UploadTargetParams extends Thread {
		String key, value;

		public UploadTargetParams(String key, String value) {
			this.key = key;
			this.value = value;

		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String urlStr = HttpUtlis.BASE_URL + "/user/setUserTarget"; 
			Map<String, String> map = new LinkedHashMap<String, String>();
			map.put("session", session_id);
			map.put("key", key);
			map.put("value", value);
			try {
				JSONObject json = new JSONObject(HttpUtlis.getRequest(urlStr,
						map));
				if (json.getInt("status") == 0) {
					// myHandler.obtainMessage(1, map).sendToTarget();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			super.run();
		}

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id._height:
			tView.setProgress(75);
			break;
		case R.id._low:
			tView.setProgress(25);
			break;
		case R.id._middle:
			tView.setProgress(50);
			break;
		case R.id.next:
			// 目标
			db.setTargetStep(stepTar);
			UploadTargetParams u = new UploadTargetParams("3", stepTar + "");
			new Thread(u).start();
			Intent i = new Intent(this, SportsDevice.class);
			i.putExtra("from", "init");
			startActivity(i);
			break;
		}
	}
}

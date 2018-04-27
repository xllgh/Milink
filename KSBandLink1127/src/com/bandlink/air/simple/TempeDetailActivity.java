package com.bandlink.air.simple;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.bandlink.air.R;
import com.bandlink.air.Js.DataChartInterface;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Temperature;
import com.bandlink.air.util.Util;
import com.bandlink.air.util.WebViewHelper;
import com.google.gson.Gson;

public class TempeDetailActivity extends LovefitActivity {

	private SharePreUtils spUtil;

	private Dbutils db;

	private ActionbarSettings actionBar;

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub

	}

	String id;
	WebViewHelper help;
	int pro = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setContentView(R.layout.expand);
		actionBar = new ActionbarSettings(this, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				TempeDetailActivity.this.finish();
			}
		}, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Context context = Util
						.getThemeContext(TempeDetailActivity.this);
				AlertDialog.Builder ab = new AlertDialog.Builder(context);
				SeekBar seek = new SeekBar(context);
				seek.setProgress(pro);
				seek.setMax(100);
				seek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						// TODO Auto-generated method stub
						pro = progress;
						load((progress / 75f) + 1);

					}
				});
				ab.setView(seek);
				ab.show();
			}
		});
		actionBar.setTitle(R.string.temperature_detail);
		actionBar.setTopLeftIcon(R.drawable.ic_top_arrow);
		actionBar.setTopRightIcon(R.drawable.zoom);
		actionBar.setRightVisible(false);
		WebView webView = (WebView) findViewById(R.id.web);
		help = new WebViewHelper(this, webView,
				"file:///android_asset/chart/h2.html");
		help.initWebview();
		// help.setBgTransparent(); 
		DataChartInterface js = new DataChartInterface(this, webView);
		help.addJavascriptInterface(js, "Android");
		spUtil = SharePreUtils.getInstance(this);
		db = new Dbutils(spUtil.getUid(), this);
		id = getIntent().getStringExtra("id");
		load(1);
		super.onCreate(savedInstanceState);
	}

	void load(float inv) {
		SimpleDateFormat sd = new SimpleDateFormat("HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		int min = 0;
		int max = 0;
		if (id != null) {
			Temperature t = db.getTemperatureByid(id);
			if (t != null) {
				cal.setTimeInMillis(t.getTime());
				byte[] data = t.getData();
				String[] x = new String[data.length / 2];
				float[] y = new float[data.length / 2];
				for (int i = 0; i < data.length; i += 2) {
					y[i / 2] = ((data[i] << 8) + data[1 + i]) / 100f;
					x[i / 2] = sd
							.format(cal.getTimeInMillis() + (i / 2) * 1000);
					if (max == 0) {
						max = y[i / 2] > Math.round(y[i / 2]) ? (Math
								.round(y[i / 2]) + 1) : Math.round(y[i / 2]);
						min = Math.round(y[i / 2]);
					}
					if (y[i / 2] > max) {
						max = y[i / 2] > Math.round(y[i / 2]) ? (Math
								.round(y[i / 2]) + 1) : Math.round(y[i / 2]);
					}
					if (y[i / 2] < min) {
						min = Math.round(y[i / 2]) > y[i / 2] ? (Math
								.round(y[i / 2]) - 1) : Math.round(y[i / 2]);
					}
				}
				Gson gson = new Gson();
				String xstr = gson.toJson(x);
				String ystr = gson.toJson(y);
				help.loadUrls(new String[] { "showStepChart('" + xstr + "','"
						+ ystr + "','" + max + "','" + min + "','" + inv + "')" });
			}
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

}

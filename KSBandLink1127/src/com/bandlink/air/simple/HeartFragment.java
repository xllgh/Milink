package com.bandlink.air.simple;

import java.io.File;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bandlink.air.R;
import com.bandlink.air.ble.AirECGService;
import com.bandlink.air.simple.ecg.GraphView.GraphViewData;
import com.bandlink.air.simple.ecg.GraphViewSeries;
import com.bandlink.air.simple.ecg.LineGraphView;
import com.bandlink.air.util.LovefitActivity;

public class HeartFragment extends LovefitActivity {
	private Timer timer = new Timer();
	private TimerTask task;

	int[] xv = new int[100];
	int[] yv = new int[100];
	BroadcastReceiver ecgReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals(AirECGService.ACTION_BLE_AIRECG_DATA)) {
				byte[] ecgRawData = intent.getByteArrayExtra("data");
				// displayData("收到数据的长度："+ecgRawData.length);
				handler.obtainMessage(2, ecgRawData).sendToTarget();
			}
		}
	};
	ProgressDialog dialog;

	void startTempService() {
		// dialog =
		// ProgressDialog.show(Util.getThemeContext(HeartFragment.this),
		// null,
		// getString(R.string.scanning), true, true);
		Intent intentScale = new Intent(
				HeartFragment.this.getApplicationContext(), AirECGService.class);
		HeartFragment.this.startService(intentScale);
	}

	boolean start = true;
	long st = 0;;
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// 刷新图表
			if (msg.what == 2) {
				if (start) {
					start = false;
					st = Calendar.getInstance().getTimeInMillis();
				}
				byte[] x = (byte[]) msg.obj;
				writeValue(x);
				updateChart(x);
				long temp = (Calendar.getInstance().getTimeInMillis() - st);
				long h = temp / 3600000;
				long m = (temp % 3600000) / (60 * 1000);
				long s = (temp % 3600000) % (60 * 1000) / 1000;
				// long ss = (temp % 3600000) % (60 * 1000) % 1000;
				time.setText(h + ":" + m + ":" + s);
			}
			super.handleMessage(msg);
		}
	};
	String pathDir = Environment.getExternalStorageDirectory() + "/ecg";
	File saveFile;
	long index = 0;
	int temp = 0;

	void writeValue(byte[] bts) {
		try {
			if (!saveFile.exists()) {
				saveFile.createNewFile();
			}
			RandomAccessFile file = new RandomAccessFile(saveFile, "rw");
			file.seek(index);
			System.out.println(bts.length);
			if (temp == 512) {
				index += 512;
				file.skipBytes(512);
				temp = 0;
			} else {

				file.write(bts);
				index += bts.length;
				temp += bts.length;

			}

			file.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	TextView time;

	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.heart);
		start = true;
		// 这里的Handler实例将配合下面的Timer实例，完成定时更新图表的功能
		saveFile = new File(pathDir);
		if (!saveFile.exists()) {
			saveFile.mkdirs();
		}
		SimpleDateFormat sd = new SimpleDateFormat("yyyMMddHHmmss");
		saveFile = new File(pathDir + "/ecg" + sd.format(new Date()) + ".txt");
		IntentFilter filter = new IntentFilter();
		time = (TextView) findViewById(R.id.time);
		filter.addAction(AirECGService.ACTION_BLE_AIRECG_DATA);
		HeartFragment.this.registerReceiver(ecgReceiver, filter);
		graphInit();
		task = new TimerTask() {
			@Override
			public void run() {
				startTempService();
			}
		};

		timer.schedule(task, 4000);
		super.onCreate(savedInstanceState);
	};

	LineGraphView mGraph1;
	GraphViewSeries gvSeries1;

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		handler.removeCallbacksAndMessages(null);
		timer.cancel();
		super.onStop();
	}

	private void graphInit() {
		mGraph1 = new LineGraphView(HeartFragment.this // context
				, "" // heading
		);
		mGraph1.setViewPort(0, 512);
		mGraph1.setScalable(true);

		((LineGraphView) mGraph1).setDrawBackground(false);
		gvSeries1 = new GraphViewSeries(new GraphViewData[] {});

		mGraph1.addSeries(gvSeries1);
		LinearLayout layout = (LinearLayout) findViewById(R.id.chartborder);
		layout.addView(mGraph1);
	}

	private int graph2LastXValue = 0;

	private void updateChart(byte[] ecgRawData) {
		int ecgdata1 = 0;
		for (int i = 0; i < ecgRawData.length / 2; i++) {
			// ecgdata1 = (ecgRawData[i*4+1] & 0xff)*256 + (ecgRawData[i*4] &
			// 0xff);
			ecgdata1 = ((ecgRawData[i * 2 + 1] & 0xff) * 256 + (ecgRawData[i * 2] & 0xff));
			gvSeries1.appendData(new GraphViewData(graph2LastXValue, ecgdata1),
					true, 512);
			graph2LastXValue++;

		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		HeartFragment.this.unregisterReceiver(ecgReceiver);
		super.onDestroy();
	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub

	}
}
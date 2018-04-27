package com.bandlink.air;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bandlink.air.ble.BluetoothLeService;
import com.bandlink.air.simple.NewProgress;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.SharePreUtils;

public class FileUpdateActivity extends LovefitActivity {

	private String TAG = "FileUpdateActivity";

	private int uid = -1;
	private Dbutils dbutil;
	private SharedPreferences preferences,air;

	private Button updateNow;
	private NewProgress bar;
	private TextView cur_status, per,tishi,tips;

	private Intent intent;
	private String filename;

	// just for update
	public static byte[][] buffer1 = new byte[5000][];
	public static byte[][] buffer;
	public static byte[] byteslen = new byte[4];
	public static byte[] bytescrc = new byte[2];
	public static int allLine;
	public static int crc = 0;
	public static int crc1 = 0;
	public static int datalen = 0;

	double cur = 0;
	double all = 0;
	int step =0;
	boolean bSuccess = false;
	boolean bFirst = true;
	private String version;
	private Typeface tf ;
	Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			int i = msg.what;
			if(i==0){
				if(msg.obj!=null && msg.obj.toString().equals("1")){
					start();
					this.removeCallbacksAndMessages(null);
					return;
				}else{
					//sendToConnect();
					FileUpdateActivity.this.finish(); 
					this.removeCallbacksAndMessages(null);
					return;
				}
			}
			i-=1;
			per.setText(i+"");
			if(msg.obj!=null && msg.obj.toString().equals("1")){
				Message m = new Message();
				m.what = i;
				m.obj ="1";
				handler.sendMessageDelayed(m, 1000);
			}else{
				handler.sendEmptyMessageDelayed(i, 1000);
			} 
			super.handleMessage(msg);
		}
		
	};
	void sendToConnect(){

		Dbutils db = new Dbutils(this);
		String address = db.getBTDeivceAddress();
		if (address == null || address.length() != 17) {
			Toast.makeText(getApplicationContext(),
					getString(R.string.no_air_address), Toast.LENGTH_SHORT)
					.show();
		} else {
			Toast.makeText(getApplicationContext(),
					getString(R.string.air_setting_manual_connect),
					Toast.LENGTH_SHORT).show();
			intent = new Intent(this, BluetoothLeService.class);
			intent.putExtra("address", address);
			// 1 普通模式 2是固件升级
			intent.putExtra("command", 1);
			// 重新扫描连接
			intent.putExtra("scanflag", 4);
			startService(intent);
		}
	}
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_FW_PROGRESS.equals(action)) {
				if (bar != null) {
					cur = intent.getIntExtra("cur", 0);
					all = intent.getIntExtra("all", 0);
					bar.setProgress((float)(cur/all));
					//bar.setMax((int) all);
					if (all != 0) {
						per.setText((int) (cur / all * 100) + "");
						tips.setText("正在更新...");
						if ((cur / all * 100) >= 99.5) {
							per.setText("100");
							
							//updateNow.setClickable(false);
						}
					}
				}
			} else if (BluetoothLeService.ACTION_FW_STEPS.equals(action)) {
				// displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
				MyLog.d(TAG, "重启蓝牙");
				int i = intent.getIntExtra("step", 0);
				if(intent.getBooleanExtra("has", false)){
					updateNow.setVisibility(View.GONE);
				}
				step =i;
				String str_step = null;
				// 1.加 载文件
				// 2.重启蓝牙
				// 3.传输数据，请耐心等待，预计耗时100秒，如果进度条长时间不动，请重新开始
				// 4.升级完成，重启蓝牙
				// 5.Air更新成功，请返回
				// 11.文件加载失败
				// 12.遇到问题，正在重新开始
				switch (i) {
				case 1:
					str_step = FileUpdateActivity.this
							.getString(R.string.air_fw_step_1);
					break;
				case 2:
					str_step = FileUpdateActivity.this
							.getString(R.string.air_fw_step_2);
					break;
				case 3:
					str_step = FileUpdateActivity.this
							.getString(R.string.air_fw_step_3);
					break;
				case 4:
					str_step = FileUpdateActivity.this
							.getString(R.string.air_fw_step_4);
					break;
				case 5:
					updateNow.setVisibility(View.VISIBLE);
					//per.setVisibility(View.GONE);
					//tishi.setVisibility(View.GONE);
					updateNow.setText(R.string.air_up_success);

					str_step = FileUpdateActivity.this
							.getString(R.string.air_fw_step_5);
					bSuccess = true;
					if(air!=null)
						air.edit().putString("soft_version", version.replace(".hex", "")).commit();
					
					
					handler.obtainMessage(4).sendToTarget();
					tips.setText(getString(R.string.autoexit));	
					break;
				case 11:
					str_step = FileUpdateActivity.this
							.getString(R.string.air_fw_step_11);
					break;
				case 12:
					str_step = FileUpdateActivity.this
							.getString(R.string.air_fw_step_12);
					break;
				case 13:
					str_step = FileUpdateActivity.this
					.getString(R.string.air_fw_step_13);
					
					break;
				}

				if (str_step != null) {
					cur_status.setText(str_step);
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		setContentView(R.layout.fileupdate);
		version = getIntent().getStringExtra("version");
		tf = Typeface.createFromAsset(getAssets(), "font/AvenirLTStd-Light.otf");
		updateNow = (Button) findViewById(R.id.upgradenow);
		per = (TextView) findViewById(R.id.per);
		per.setTypeface(tf);
		tips = (TextView) findViewById(R.id.tips);
		tishi= (TextView) findViewById(R.id.tishi);
		TextView unit= (TextView) findViewById(R.id.unit);
		unit.setTypeface(MilinkApplication.NumberFace);
		cur_status = (TextView) findViewById(R.id.newfunction);
		ActionbarSettings actionbar = new ActionbarSettings(this, null, null);
		actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
		actionbar.setTitle(R.string.airupdate);
		bar = (NewProgress) findViewById(R.id.bar);
		preferences = getApplicationContext()
				.getSharedPreferences(
						SharePreUtils.APP_ACTION,
						Context.MODE_PRIVATE);
		air = getSharedPreferences("air", Context.MODE_MULTI_PROCESS);
		uid = preferences.getInt("UID", -1);
		dbutil = new Dbutils(uid, this);

		bFirst = true;
		
		if (updateNow != null) {
			updateNow.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					start();
				}
			});
		}

		intent = this.getIntent();
//		if (intent != null) {
			filename = intent.getStringExtra("filename");
//			tips.setText(R.string.updatetips);
//			handler.obtainMessage(8, "1").sendToTarget();
//		} else {
//			// 失败
//		}
	}
private void start(){

	if(!bSuccess)
	{
		updateNow.setText(getString(R.string.air_up_restart));
		if (bar != null) {
			per.setText("0");
			bar.setProgress(0);
		}

		cur_status.setText(FileUpdateActivity.this
			.getString(R.string.loadfile));
	// 启动升级服务
	String address = dbutil.getBTDeivceAddress();
	if (address != null) {
		Intent intent = new Intent(FileUpdateActivity.this,
				BluetoothLeService.class);
		intent.putExtra("address", address);
		intent.putExtra("command", 2);
		//tt
		//first time, we assume device is already connected and send parser.start(6) directlly
		
		if(bFirst)
			intent.putExtra("scanflag", 0);
		else { 
			if(cur > 0)
				intent.putExtra("scanflag", 1);
			else {
				intent.putExtra("scanflag", 2);
			}
		}
		bFirst = false;
		cur = 0;
		intent.putExtra("filename", filename);
		tips.setText(R.string.updatetips);
		startService(intent);
		MyLog.v(TAG, "Start BLE Service For Update");
	}

	}
	else {
		FileUpdateActivity.this.finish();
	}
}
	private OnClickListener lsr = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			FileUpdateActivity.this.finish();
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		
		if(step==5 || (cur / all * 100) >=100){
			//per.setVisibility(View.GONE);
			tishi.setVisibility(View.GONE);
			updateNow.setText(R.string.air_up_success);
			cur_status.setText(getString(R.string.air_fw_step_5));
			bar.setProgress((int) cur);
			bSuccess = true;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		unregisterReceiver(mGattUpdateReceiver);
		super.onDestroy();
	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_FW_STEPS);
		intentFilter.addAction(BluetoothLeService.ACTION_FW_PROGRESS);
		return intentFilter;
	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub
		
	}
}

package com.bandlink.air;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bandlink.air.ble.BluetoothLeService;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.Util;

public class AirNewExperience extends LovefitActivity implements OnClickListener,
		OnCheckedChangeListener {
 
	private SharedPreferences sharePre;
	private RelativeLayout lay_yaoyao_action, lay_open_picture_dir,
			lay_vibrate_once, lay_vibrate_liric1, lay_vibrate_always;
	private RadioButton rb_yaoyao_binding_choose1, rb_yaoyao_binding_choose2;
	private MediaScannerConnection conn;
	private int device_status = 0;
	// 即时RSSI
	private int device_rssi_c = 0;
	// 平均RSSI
	private int device_rssi_a = 0;
	private int device_battery = 0;
	private Context con;
	boolean bNotRead = true;
	RelativeLayout 	set_checkbox_device_vibrate_border;
	private CheckBox device_vibrate;
	TextView countdowntime;

	private BroadcastReceiver airReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			// TODO Auto-generated method stub

			final String action = intent.getAction();

			if (action.equals(BluetoothLeService.ACTION_AIR_RSSI_STATUS)) {
				device_status = intent.getIntExtra("status", 0);
				device_rssi_c = intent.getIntExtra("rssi_c", 0);
				device_rssi_a = intent.getIntExtra("rssi_a", 0);
				device_battery = intent.getIntExtra("device_battery", 0);
			}

			if (action
					.equals(BluetoothLeService.ACTION_BLE_CONFIG_READ_RESPONSE)) {
				bNotRead = false;
				final int type = intent.getIntExtra("type", -1);
				final byte[] ba;
				switch (type) {
				// vibrate_en
				case 5:
					ba = intent.getByteArrayExtra("value");
					boolean vibrate_en = ((ba[0] & 0xff) == 0x01);
					if (device_vibrate != null) {
						device_vibrate.setChecked(vibrate_en);
					}
					break;
				default:
					break;
				}

			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.air_new_experience);
		ActionbarSettings actionbar = new ActionbarSettings(this, lsr, null);
		actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
		actionbar.setTitle(R.string.air_new);
		con = Util.getThemeContext(this);

		inint();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	private void inint() {
		// TODO Auto-generated method stub
		rb_yaoyao_binding_choose1 = (RadioButton) findViewById(R.id.rb_yaoyao_binding_choose_picture);
		rb_yaoyao_binding_choose2 = (RadioButton) findViewById(R.id.rb_yaoyao_binding_choose_sound);
		rb_yaoyao_binding_choose1.setOnClickListener(this);
		rb_yaoyao_binding_choose2.setOnClickListener(this);

		lay_yaoyao_action = (RelativeLayout) findViewById(R.id.lay_yaoyao_action);
		lay_yaoyao_action.setOnClickListener(this);

		lay_open_picture_dir = (RelativeLayout) findViewById(R.id.lay_open_picture_dir);
		lay_open_picture_dir.setOnClickListener(this);

		lay_vibrate_once = (RelativeLayout) findViewById(R.id.lay_vibrate_once);
		lay_vibrate_once.setOnClickListener(this);

		lay_vibrate_liric1 = (RelativeLayout) findViewById(R.id.lay_vibrate_liric1);
		lay_vibrate_liric1.setOnClickListener(this);

		lay_vibrate_always = (RelativeLayout) findViewById(R.id.lay_vibrate_always);
		lay_vibrate_always.setOnClickListener(this);
		sharePre = getSharedPreferences("air", MODE_MULTI_PROCESS);	
		device_vibrate = (CheckBox) findViewById(R.id.set_checkbox_device_vibrate);
		device_vibrate.setOnClickListener(this);
		set_checkbox_device_vibrate_border = (RelativeLayout) findViewById(R.id.set_checkbox_device_vibrate_border);
		set_checkbox_device_vibrate_border.setOnClickListener(this);

		countdowntime = (TextView) findViewById(R.id.countdowntime);
		// 自动重连

		if (sharePre.getBoolean("air_vibrate", true)) {
			device_vibrate.setChecked(true);
		} else {
			device_vibrate.setChecked(false);
		}

	}

	private OnClickListener lsr = new OnClickListener() {

		@Override
		public void onClick(View v) {

			AirNewExperience.this.finish();
		}
	};

	private void openPictureDir() {
		File folder = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_DCIM).toString()
				+ File.separator + "Lovefit_AIR" + File.separator);
		if (!folder.exists()) {
			Toast.makeText(this, getString(R.string.empty_folder),
					Toast.LENGTH_LONG).show();
			return;
		}
		File[] allFiles = folder.listFiles();
		new SingleMediaScanner(this, allFiles[0]);

	}

	public class SingleMediaScanner implements MediaScannerConnectionClient {

		private MediaScannerConnection mMs;
		private File mFile;

		public SingleMediaScanner(Context context, File f) {
			mFile = f;
			mMs = new MediaScannerConnection(context, this);
			mMs.connect();
		}

		public void onMediaScannerConnected() {
			mMs.scanFile(mFile.getAbsolutePath(), null);
		}

		public void onScanCompleted(String path, Uri uri) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(uri);
			startActivity(intent);
			mMs.disconnect();
		}

	}

	int yaoyao_action = 0;

	void airYaoyao() {
		isStart = true;
		yaoyao_action = 1 - yaoyao_action;
		Intent intent = new Intent();
		intent.setAction(BluetoothLeService.ACTION_BLE_CONFIG_CMD);
		intent.putExtra("type", 12);
		intent.putExtra("param", new int[] { yaoyao_action });
		sendBroadcast(intent);
	}

	int vibrating = 0;

	void airVibrate(int type, int param) {
		Intent intent = new Intent();
		intent.setAction(BluetoothLeService.ACTION_BLE_CONFIG_CMD);
		intent.putExtra("type", 11);
		intent.putExtra("param", new int[] { type, param });
		sendBroadcast(intent);
	}

	private CompoundButton getToggleButtonFromRelativeLayout(ViewGroup group) {
		for (int i = 0; i < group.getChildCount(); i++) {
			if (group.getChildAt(i) instanceof CompoundButton) {
				return (CompoundButton) group.getChildAt(i);
			} else if (group.getChildAt(i) instanceof ViewGroup) {
				return getToggleButtonFromRelativeLayout((ViewGroup) group
						.getChildAt(i));
			}
		}
		return null;

	}

	private void showTime() {
		final String te = "";
		String[] distanceItems = getResources().getStringArray(R.array.time);
		final ArrayList<Map<String, String>> sp = new ArrayList<Map<String, String>>();
		for (String string : distanceItems) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("item", string);
			sp.add(map);
		}

		AlertDialog.Builder ab = new AlertDialog.Builder(con);
		ab.setTitle(getString(R.string.control_time));
		ab.setSingleChoiceItems(distanceItems, 0,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						countdowntime.setText(sp.get(which).get("item"));
					}

				});
		ab.setPositiveButton(R.string.countdown_time,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (countdowntime.getText().toString()
								.equals(getString(R.string.click_start))) {
							countdowntime.setText("30s");
						}
						MyCount coun = new MyCount(Long.valueOf(countdowntime
								.getText().toString().substring(0, 2)) * 1000,
								1000);
						coun.start();
					}
				});

		ab.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						countdowntime.setText(getString(R.string.click_start));
						dialog.cancel();
					}
				});
		ab.show();

	}

	boolean isStart = false;

	class MyCount extends CountDownTimer {

		public MyCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onFinish() {
			countdowntime.setText(getString(R.string.click_start));
			lay_yaoyao_action.setClickable(true);
			rb_yaoyao_binding_choose1.setClickable(true);
			rb_yaoyao_binding_choose2.setClickable(true);
			airYaoyao();
			handler.obtainMessage(2).sendToTarget();
		}

		@Override
		public void onTick(long millisUntilFinished) {
			countdowntime.setText(millisUntilFinished / 1000 + " s");
			if (!isStart) {
				airYaoyao();
			}
			handler.obtainMessage(1, millisUntilFinished / 1000).sendToTarget();
		}

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.lay_yaoyao_action:
			showTime();
			break;

		case R.id.lay_open_picture_dir:
			openPictureDir();
			break;

		case R.id.lay_vibrate_once:
			airVibrate(0, 0);
			break;

		case R.id.lay_vibrate_liric1:
			airVibrate(1, 0);

			break;

		case R.id.lay_vibrate_always:
			vibrating = 1 - vibrating;
			airVibrate(2, vibrating);
			if (vibrating == 1) {
				Toast.makeText(this, getString(R.string.vibrate_always_tips),
						Toast.LENGTH_SHORT).show();
			}
			break;

		case R.id.rb_yaoyao_binding_choose_picture:
			Toast.makeText(
					AirNewExperience.this,
					getString(R.string.air_setting_yaoyao_picture_path)
							+ " "
							+ Environment.getExternalStoragePublicDirectory(
									Environment.DIRECTORY_DCIM).toString()
							+ File.separator + "Lovefit_AIR" + File.separator,
					Toast.LENGTH_LONG).show();
			sharePre.edit().putInt("air_yaoyao_binding_choose", 1).commit();
			break;

		case R.id.rb_yaoyao_binding_choose_sound:
			sharePre.edit().putInt("air_yaoyao_binding_choose", 2).commit();
			break;

		case R.id.set_checkbox_callphone_border:
			CompoundButton toggle5 = getToggleButtonFromRelativeLayout((ViewGroup) v);
			if (toggle5 != null)
				onCheckedChanged(toggle5, !toggle5.isChecked());
			break;

		case R.id.set_checkbox_device_vibrate_border:
		case R.id.set_checkbox_device_vibrate:
			sharePre.edit()
					.putBoolean("air_vibrate", device_vibrate.isChecked())
					.commit();
			airSetVibrate(device_vibrate.isChecked());

			break;
		default:
			break;
		}
	}

	void airSetVibrate(boolean bb) {
		Intent intent = new Intent();
		intent.setAction(BluetoothLeService.ACTION_BLE_CONFIG_CMD);
		intent.putExtra("type", 5);
		intent.putExtra("param", new int[] { 0 });
		intent.putExtra("en", bb);
		sendBroadcast(intent);

	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case 1:

				if (Integer.valueOf(msg.obj.toString()) > 0) {
					lay_yaoyao_action.setClickable(false);
					if (sharePre.getInt("air_yaoyao_binding_choose", 0) == 1) {
						rb_yaoyao_binding_choose2.setClickable(false);
					} else {
						rb_yaoyao_binding_choose1.setClickable(false);
					}
					countdowntime.setText(msg.obj.toString() + " s");
				}
				break;
			case 2:
				isStart = false;
				break;
			}
		}
	};

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		switch (buttonView.getId()) {

		}
	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub
		
	}
}

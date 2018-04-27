package com.bandlink.air.simple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bandlink.air.FriendFragment;
import com.bandlink.air.R;
import com.bandlink.air.ble.BluetoothLeService;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.MainInterface;
import com.bandlink.air.util.Util;

public class CardFragment extends Fragment {

	private ActionbarSettings actionBar;
	private MainInterface interf;
	private View mView;
	private TextView cardNo, money;
	private ListView listView;
	private SimpleAdapter simpleAdapter;

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		if (activity instanceof MainInterface) {
			interf = (MainInterface) activity;

		}
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mView = inflater.inflate(R.layout.fragment_card, null);
		cardNo = (TextView) mView.findViewById(R.id.cardNo);
		money = (TextView) mView.findViewById(R.id.money);
		listView = (ListView) mView.findViewById(R.id.record);
		actionBar = new ActionbarSettings(mView, new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!mBluetoothAdapter.isEnabled()) {
					mBluetoothAdapter.enable();
				} else {
					readCard();
				}
				AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
				ab.setTitle(R.string.warning);
				ab.setMessage("使用本地升级包升级固件？");
				ab.setNegativeButton(R.string.cancel, null);
				ab.setPositiveButton(R.string.updatenow,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								 getActivity()
								 .sendBroadcast(
								 new Intent(
								 BluetoothLeService.ACTION_BLE_CARD_UPDATE));
								 showUpdateProgress();

							}
						});
				//ab.create().show();
			}
		}, interf);
		actionBar.setTitle(R.string.cardinfo);
		actionBar.setTopRightIcon(R.drawable.pingan_refresh);
		// mp.put("TransDate", nj.TransDate + "");
		// mp.put("TransValue", nj.TransValue + "");
		// mp.put("Verification", nj.Verification + "");
		// mp.put("TransTypeDescription", nj.TransTypeDescription + "");
		// mp.put("TransType", nj.TransType + "");
		// mp.put("TerminalCode", nj.TerminalCode + "");
		// mp.put("Reservation", nj.Reservation + "");
		// mp.put("CityID", nj.CityID + "");
		simpleAdapter = new SimpleAdapter(getActivity(), data,
				R.layout.item_trade, new String[] { "TransDate", "TransValue",
						"Verification" }, new int[] { R.id.text1, R.id.text2,
						R.id.text3 });
		listView.setAdapter(simpleAdapter);
		return mView;
	}

	ProgressDialog progress;

	void showUpdateProgress() {
		if (progress == null) {
			progress = new ProgressDialog(getActivity());

		}
		progress.setMax(100);
		progress.setCancelable(false);
		progress.setIndeterminate(false);
		progress.setTitle(R.string.update);
		progress.setMessage(getString(R.string.data_wait));
		progress.setProgress(0);
		progress.show();
		;

	}

	ProgressDialog dialog;
	BluetoothAdapter mBluetoothAdapter;

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		filter.addAction(BluetoothLeService.ACTION_BLE_CARD);
		filter.addAction(BluetoothLeService.ACTION_BLE_CARD_REVEIVER);
		getActivity().registerReceiver(receiver, filter);
		if (!getActivity().getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(getActivity(), R.string.ble_not_supported,
					Toast.LENGTH_SHORT).show();
			return;
		}

		// Initializes a Bluetooth adapter. For API level 18 and above, get a
		// reference to
		// BluetoothAdapter through BluetoothManager.
		final BluetoothManager bluetoothManager = (BluetoothManager) getActivity()
				.getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		// Checks if Bluetooth is supported on the device.
		if (mBluetoothAdapter == null) {
			Toast.makeText(getActivity(),
					R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT)
					.show();

			return;
		}

		if (!FriendFragment.isNetworkConnected(getActivity())) {
			Toast.makeText(getActivity(), getString(R.string.pleasechecknetwork), 1000).show();
		}

		super.onResume();
	}

	public void onPause() {
		getActivity().unregisterReceiver(receiver);
		super.onPause();
	};

	BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				String stateExtra = BluetoothAdapter.EXTRA_STATE;
				int state = intent.getIntExtra(stateExtra, -1);
				switch (state) {
				case BluetoothAdapter.STATE_TURNING_ON:
					// MyLog.d(TAG, "蓝牙开始开启");

					break;
				case BluetoothAdapter.STATE_ON:
					readCard();
					break;
				case BluetoothAdapter.STATE_TURNING_OFF:
					// MyLog.d(TAG, "蓝牙开始关闭");
					break;
				case BluetoothAdapter.STATE_OFF:
					break;
				}
			} else if (action.equals(BluetoothLeService.ACTION_BLE_CARD)) {
				String str;
				if ((str = intent.getStringExtra("card")) != null) {
					cardNo.setText(str);
				}

				if (intent.getIntExtra("money", -1) != -1) {
					money.setText(String.format("%.2f",
							intent.getIntExtra("money", 0) / 100f));
					if (dialog != null && dialog.isShowing()) {
						dialog.dismiss();
					}
				}
				if (intent.getSerializableExtra("record") != null) {
					handler.obtainMessage(0,
							intent.getSerializableExtra("record"))
							.sendToTarget();
				}
				if (intent.getStringExtra("code") != null) {
					handler.obtainMessage(1, getString(R.string.cardgetcodeerror)).sendToTarget();
				}
				if (intent.getStringExtra("off") != null) {
					// 关机
					handler.obtainMessage(1).sendToTarget();
				}
			} else if (action
					.equals(BluetoothLeService.ACTION_BLE_CARD_REVEIVER)) {
				if (intent.hasExtra("update_text")) {
					progress.setMessage(intent.getStringExtra("text"));
				}
				if (intent.hasExtra("update_int")) {
					progress.setProgress(intent.getIntExtra("update_int", 0));
				}
			}
		}
	};
	ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				data.clear();
				data.addAll((ArrayList<HashMap<String, String>>) msg.obj);
				simpleAdapter.notifyDataSetChanged();
				break;
			case 1:
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
				if (msg.obj != null) {
					Toast.makeText(getActivity(), msg.obj.toString(),
							Toast.LENGTH_LONG).show();
				}

				break;
			}
			super.handleMessage(msg);
		};
	};

	public static boolean isServiceRunning(String serviceClassName,
			Context context) {
		final ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		final List<RunningServiceInfo> services = activityManager
				.getRunningServices(Integer.MAX_VALUE);

		for (RunningServiceInfo runningServiceInfo : services) {
			if (runningServiceInfo.service.getClassName().equals(
					serviceClassName)) {
				return true;
			}
		}
		return false;
	}

	public void readCard() {

		if (isServiceRunning(BluetoothLeService.class.getName(), getActivity())) {
			dialog = Util.initProgressDialog(getActivity(), true, getString(R.string.readingcard), null);
			getActivity().sendBroadcast(
					new Intent(BluetoothLeService.ACTION_BLE_CARD_READ));
		} else {
			Toast.makeText(getActivity(), getString(R.string.notbinddevice), Toast.LENGTH_LONG)
					.show();
		}

	}

}

package com.bandlink.air.ble;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

@SuppressLint("NewApi")
public class BleScanService extends Service {
	private IBinder mIBinder = new LocalBinder();
	
	public class LocalBinder extends Binder {
		public BleScanService getService() {
			return BleScanService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mIBinder;
	}

	@SuppressLint("NewApi")
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub

		super.onCreate();
	}
	private String address;
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		if (intent!=null && intent.getBooleanExtra("order", false)) {
			address = intent.getStringExtra("address");
			if (handler != null) {
				handler.obtainMessage(0).sendToTarget();
			}
			LogUtil.e("SCAN", "#########onCreate");
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		if (handler != null) {
			handler.removeCallbacksAndMessages(null);
		}
		super.onDestroy();
	}

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			LogUtil.e("#########" + msg.what);
			Intent ble = new Intent(BleScanService.this,
					BluetoothLeService.class);
			switch (msg.what) {
			case 0:
				stopService(ble);
				this.sendEmptyMessageDelayed(1, 1000);
				break;
			case 1:
				ble.putExtra("scanflag", 1);
				ble.putExtra("address", address);
				startService(ble);
				this.sendEmptyMessageDelayed(2, 1000);
				break;
			case 2:
				//BleScanService.this.stopSelf();
				break;

			}
			super.handleMessage(msg);
		}

	};

}

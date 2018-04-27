package com.bandlink.air.ble;
import java.util.Calendar;

import android.bluetooth.BluetoothGatt;

import com.bandlink.air.MyLog;
public class ReadRssiAndData extends Thread{
	private final static String TAG = "ReadRssiAndData";
	
	private BluetoothLeService service;
	private BluetoothGatt gatt;
	private volatile int Count = 0;
	private volatile boolean isContinued;
	public volatile boolean isConnected;
	private  volatile int Count_lastrssi = 0;
	
	public ReadRssiAndData(BluetoothLeService g) {
		// TODO Auto-generated constructor stub
		gatt = g.mBluetoothGatt;
		service = g;
		isContinued = true;
	}
	
	public void saveCountLastRssi()
	{
		Count_lastrssi = Count;
	}
	
	public void StopThread()
	{
		isContinued=false;
	}
	


	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		//MyLog.d("AIRSERVICE", "Read Rssi thread is start!");
		while (isContinued) {
			if (service.mBluetoothGatt != null && isConnected) {
				service.mBluetoothGatt.readRemoteRssi();
				if(Math.abs(Count - Count_lastrssi)>=3)
				{
					service.onDeviceLost();
				}
			}
			MyLog.v(TAG, Calendar.getInstance().getTime().toLocaleString() + "  :  " + Count + "/" + Count_lastrssi);
			if(Count == 3600)
			{
				Count = 0;
				//获取数据？
				if(service!=null && service.parser!=null)
				{
					//MyLog.d("AIRSERVICE", "Read data on rssi thread!");
					service.parser.Start(5);					
				}
				
			}
			try {
				sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Count++;
		}
	}
}

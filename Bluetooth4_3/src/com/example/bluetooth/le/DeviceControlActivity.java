/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.bluetooth.le;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.bluetooth.le.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

/**
 * For a given BLE device, this Activity provides the user interface to connect,
 * display data, and display GATT services and characteristics supported by the
 * device. The Activity communicates with {@code BluetoothLeService}, which in
 * turn interacts with the Bluetooth LE API.
 */
@SuppressLint("NewApi")
public class DeviceControlActivity extends Activity {
	private final static String TAG = DeviceControlActivity.class
			.getSimpleName();

	public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
	public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

	public static final String hrUUID="0000fff2-0000-1000-8000-00805f9b34fb";
	
	private TextView mConnectionState;
	private TextView mDataField;
	private TextView numHeartRate;
	private RelativeLayout relativeLayout;
	private LinearLayout linearLayout;
	
	private Button btnhr;
	private boolean isShow;
	
	File file;
	
	private String mDeviceName;
	private String mDeviceAddress;
	private ExpandableListView mGattServicesList;
	private BluetoothLeService mBluetoothLeService;
	private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
	private boolean mConnected = false;
	private BluetoothGattCharacteristic mNotifyCharacteristic;

	private final String LIST_NAME = "NAME";
	private final String LIST_UUID = "UUID";
	
	private String ECGFileName;
	//private PrintWriter mCurrentFile;
	private FileOutputStream mCurrentFile;
	private StringBuffer buff = new StringBuffer();
	
	private static GraphView mGraph1;

	private GraphViewSeries gvSeries1;
	
	private int graph2LastXValue = 0;
	
	private ExecutorService pool;
	
	private int threadFlag = 0;
	
	private byte[] clone;

	// Code to manage Service lifecycle.
	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName,
				IBinder service) {
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
					.getService();
			if (!mBluetoothLeService.initialize()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
				finish();
			}
			// Automatically connects to the device upon successful start-up
			// initialization.
			mBluetoothLeService.connect(mDeviceAddress);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBluetoothLeService = null;
		}
	};
	

	// Handles various events fired by the Service.
	// ACTION_GATT_CONNECTED: connected to a GATT server.
	// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
	// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
	// ACTION_DATA_AVAILABLE: received data from the device. This can be a
	// result of read
	// or notification operations.
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			System.out.println("action = " + action);
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
				mConnected = true;
				updateConnectionState(R.string.connected);
				invalidateOptionsMenu();
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED
					.equals(action)) {
				mConnected = false;
				updateConnectionState(R.string.disconnected);
				invalidateOptionsMenu();
				clearUI();
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
					.equals(action)) {
				// Show all the supported services and characteristics on the
				// user interface.
				displayGattServices(mBluetoothLeService
						.getSupportedGattServices());
			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
				
//				displayData(intent
//						.getStringExtra(BluetoothLeService.EXTRA_DATA));
				
//				byte[] ecgRawData = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
//				pool.execute(new Runa(ecgRawData));
//				threadFlag++;
//				mBluetoothLeService.getRssiVal();
				
				
				//////////////////////////////////此处为接受心率数据///////////////////////
				byte[] ecgRawData = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
				
				Log.i("信息", "心率数据");
				///////////////////////////////////////////////////////////////////
				
				
				
//				displayData("收到数据的长度："+ecgRawData.length);
				dataProcess(ecgRawData);
			} else if(BluetoothLeService.ACTION_RSSI_AVAILABLE.equals(action)){
				int rssi = intent.getIntExtra("111", 0);
				displayData("RSSI = "+ rssi);				
			}
		}
	};
	
	private class Runa implements Runnable{
		private byte[] ecgRawData;
		public Runa( byte[] ecgRawData){
			this.ecgRawData = ecgRawData;
		}
		public void run(){
			
//			displayData("收到数据的长度："+ecgRawData.length);
			dataProcess(ecgRawData);
		}
		
	}

	// If a given GATT characteristic is selected, check for supported features.
	// This sample
	// demonstrates 'Read' and 'Notify' features. See
	// http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for
	// the complete
	// list of supported characteristic features.
	private final ExpandableListView.OnChildClickListener servicesListClickListner = new ExpandableListView.OnChildClickListener() {
		@Override
		public boolean onChildClick(ExpandableListView parent, View v,
				int groupPosition, int childPosition, long id) {

			if (mGattCharacteristics != null) {
				final BluetoothGattCharacteristic characteristic = mGattCharacteristics
						.get(groupPosition).get(childPosition);
				final int charaProp = characteristic.getProperties();
				final int charaPermi= characteristic.getPermissions();
				System.out.println("charaProp = " + charaProp + ",UUID = "
						+ characteristic.getUuid().toString());
				Random r = new Random();
				
				if (characteristic.getUuid().toString()
						.equals("0000fff2-0000-1000-8000-00805f9b34fb")) {
					System.out.println("enable notification");
					Toast.makeText(DeviceControlActivity.this, String.valueOf(charaProp).toString()+" "+String.valueOf(charaPermi).toString(), Toast.LENGTH_LONG).show();
					mNotifyCharacteristic = characteristic;
					mBluetoothLeService.setCharacteristicNotification(
							characteristic, true);
					showWave();
				
						
				}		
				

//				if (characteristic.getUuid().toString()
//						.equals("0000fff2-0000-1000-8000-00805f9b34fb")) {
//						int time= 0;
//						while((time=r.nextInt(9))<=0){
//							
//						}
//						
//						String data = time+","+"1,,,,,";
//						characteristic.setValue(data.getBytes());
//						mBluetoothLeService.wirteCharacteristic(characteristic);
//				}				
//				if (characteristic.getUuid().toString()
//						.equals("0000fff1-0000-1000-8000-00805f9b34fb")) {
//					int R = r.nextInt(255);
//					int G = r.nextInt(255);
//					int B = r.nextInt(255);
//					int BB = r.nextInt(100);
//					String data = R + "," + G + "," + B + "," + BB;
//					while (data.length() < 18) {
//						data += ",";
//					}
//					System.out.println(data);
//					characteristic.setValue(data.getBytes());
//					mBluetoothLeService.wirteCharacteristic(characteristic);
//				}
//				if (characteristic.getUuid().toString()
//						.equals("0000fff3-0000-1000-8000-00805f9b34fb")) {
//					int R = r.nextInt(255);
//					int G = r.nextInt(255);
//					int B = r.nextInt(255);
//					int BB = r.nextInt(100);
//					String data = R + "," + G + "," + B + "," + BB;
//					while (data.length() < 18) {
//						data += ",";
//					}
//					System.out.println("RT");
//					characteristic.setValue("RT".getBytes());
//					mBluetoothLeService.wirteCharacteristic(characteristic);
//				}
//				if (characteristic.getUuid().toString()
//						.equals("0000fff5-0000-1000-8000-00805f9b34fb")) {
//					characteristic.setValue("S".getBytes());
//					mBluetoothLeService.wirteCharacteristic(characteristic);
//					System.out.println("send S");
//				} else {
//
//					if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
//						// If there is an active notification on a
//						// characteristic, clear
//						// it first so it doesn't update the data field on the
//						// user interface.
//						if (mNotifyCharacteristic != null) {
//							mBluetoothLeService.setCharacteristicNotification(
//									mNotifyCharacteristic, false);
//							mNotifyCharacteristic = null;
//						}
//						mBluetoothLeService.readCharacteristic(characteristic);
//
//					}
//				}
//				if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
//
//					if (characteristic.getUuid().toString().equals("0000fff6-0000-1000-8000-00805f9b34fb")||characteristic.getUuid().toString().equals("0000fff4-0000-1000-8000-00805f9b34fb")) {
//						System.out.println("enable notification");
//						mNotifyCharacteristic = characteristic;
//						mBluetoothLeService.setCharacteristicNotification(
//								characteristic, true);
//						
//					}
//				}

				return true;
			}
			return false;
		}
	};

	private void clearUI() {
		mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
		mDataField.setText(R.string.no_data);
	}

	protected void showWave() {
		// TODO Auto-generated method stub
		try {
			// Create a directory if there is no SD card
			String appName = getResources().getString(R.string.app_name);
			String dirPath = Environment.getExternalStorageDirectory().toString() + "/" + appName;
			File dir = new File(dirPath);
			//String state = Environment.getExternalStorageState();
			if (!dir.exists()) {
				dir.mkdirs();
			}
			// Create CSV file - modified part. 
			// The name of the file will be yyyyMMddkkmmss [year/month/day/starting time] 
			StringBuilder fileName = new StringBuilder();
			fileName.append(DateFormat.format("yyyyMMddkkmmss", System
					.currentTimeMillis()));
			fileName.append(".txt");
			
			
			
			ECGFileName = dirPath + "/" + fileName.toString();
			
			File outputFile = new File(dirPath, fileName.toString());
	        mCurrentFile = null;
	        try{
	        	//mCurrentFile = new PrintWriter(new FileOutputStream(outputFile));
	        	mCurrentFile = new FileOutputStream(outputFile);
	        } catch (FileNotFoundException e){
	        	e.printStackTrace();
	        }
			
	        
		}	catch (Exception e) {
			Log.e(TAG, e.getMessage());			
		}
	}
	
	private void saveProcess(byte[] ecgRawData){
		Log.v(TAG,"接收的数组长度为："+ecgRawData.length);
//		Log.v(TAG,ecgRawData.toString());
		Log.v("BLETest","线程 "+threadFlag+" 处理开始");
		int ecgdata = 0;
		//mCurrentFile.write(ecgRawData);
		//mCurrentFile.flush();	
//		for (int i = 0; i < ecgRawData.length / 2; i++) {
//			ecgdata = (ecgRawData[i * 2 + 1] & 0xff) * 256 + (ecgRawData[i * 2] & 0xff);
//			buff.delete(0, buff.length());
//			buff.append(String.valueOf(ecgdata));
//			//mCurrentFile.println(buff.toString());
//			mCurrentFile.write(ecgRawData);
//			mCurrentFile.flush();
//		}
		Log.v("BLETest","线程 "+threadFlag+" 处理结束");
		
	}
	
	private void showProcess(byte[] ecgRawData){
		Log.v(TAG,"接收的数组长度为："+ecgRawData.length);
		int ecgdata1 = 0;
		for(int i=0;i<ecgRawData.length/4;i++){                    				
			ecgdata1 = ((ecgRawData[i*4+1] & 0xff)*256 + (ecgRawData[i*4] & 0xff) + (ecgRawData[i*4+3] & 0xff)*256 + (ecgRawData[i*4+2] & 0xff))/2;
			gvSeries1.appendData(new GraphViewData(graph2LastXValue, ecgdata1), true, 512);                    			
			graph2LastXValue++;
			
		}
		Log.v("BLETest","线程 "+threadFlag+" 处理结束");
		
	}
	
	private void dataProcess(byte[] ecgRawData) {
//		Log.v(TAG,"接收的数组长度为："+ecgRawData.length);
//		Log.v(TAG,ecgRawData.toString());
		Log.v("BLETest","线程 "+threadFlag+" 处理开始");
		int ecgdata = 0;
		clone=ecgRawData;
		/*try {
			mCurrentFile.write(ecgRawData);
			mCurrentFile.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		/*for (int i = 0; i < ecgRawData.length / 2; i++) {
			ecgdata = (ecgRawData[i * 2 + 1] & 0xff) * 256 + (ecgRawData[i * 2] & 0xff);
			buff.delete(0, buff.length());
			buff.append(String.valueOf(ecgdata));
			//mCurrentFile.println(buff.toString());
			//mCurrentFile.flush();
		}*/
		
		
		int ecgdata1 = 0;
		
		String str=String.valueOf(ecgRawData);
		numHeartRate.setText(str);
		
		FileOutputStream fos=null;
		
		try {
			fos=new FileOutputStream(file,true);
			fos.write(clone);
			fos.flush();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		for(int i=0;i<ecgRawData.length/2;i++){                    				
//			ecgdata1 = (ecgRawData[i*4+1] & 0xff)*256 + (ecgRawData[i*4] & 0xff);
			ecgdata1 = ((ecgRawData[i*2+1] & 0xff)*256 + (ecgRawData[i*2] & 0xff));
				
		
		
		
//		for(int i=0;i<ecgRawData.length/4;i++){                    				
//			ecgdata1 = (ecgRawData[i*4+1] & 0xff)*256 + (ecgRawData[i*4] & 0xff);
//			ecgdata1 = ((ecgRawData[i*2+1] & 0xff)*256 + (ecgRawData[i*2] & 0xff) + (ecgRawData[i*2+3] & 0xff)*256 + (ecgRawData[i*2+2] & 0xff))/2;
			
			
			//numHeartRate.setText(String.valueOf(ecgdata1).toString());
			gvSeries1.appendData(new GraphViewData(graph2LastXValue, ecgdata1), true, 512);                    			
			graph2LastXValue++;
			
		}
		
		Log.v("BLETest","线程 "+threadFlag+" 处理结束");
	}
	
	
	private File CreatFile(){
		File root=Environment.getExternalStorageDirectory();
	
		File dataStorage=new File(root,"dataStorage.txt");
		 if(!dataStorage.exists()){
		    	try {
					dataStorage.createNewFile();
					Toast.makeText(this, "file is not exit,now creating new one", Toast.LENGTH_LONG);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    	
		    }
		return dataStorage;	

	}
	
	
	
	private void graphInit(){
		mGraph1 = new LineGraphView(this // context
				, "" // heading
		);
    	mGraph1.setViewPort(0, 512);
    	mGraph1.setScalable(true);
		((LineGraphView) mGraph1).setDrawBackground(false);
		gvSeries1 = new GraphViewSeries(new GraphViewData[] {});
		mGraph1.addSeries(gvSeries1);
    	RelativeLayout layout = (RelativeLayout) findViewById(R.id.ecg_view_line);
		layout.addView(mGraph1);
	}



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gatt_services_characteristics);

		final Intent intent = getIntent();
		mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
		mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
		file=CreatFile();

		// Sets up UI references.
		((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
		
		numHeartRate=(TextView)findViewById(R.id.num);
		
		relativeLayout=(RelativeLayout)findViewById(R.id.ecg_view_line);
		
		mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
		mGattServicesList.setOnChildClickListener(servicesListClickListner);
		mConnectionState = (TextView) findViewById(R.id.connection_state);
		mDataField = (TextView) findViewById(R.id.data_value);
		btnhr=(Button)findViewById(R.id.heartRate);
		btnhr.setOnClickListener(hrlistener);
		
		graphInit();

		getActionBar().setTitle(mDeviceName);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		boolean bll = bindService(gattServiceIntent, mServiceConnection,
				
				BIND_AUTO_CREATE);
		if (bll) {
			System.out.println("---------------");
		} else {
			System.out.println("===============");
		}
		pool = Executors.newSingleThreadExecutor();
		
		btnhr.setOnClickListener(hrlistener);
		
	}
	
	OnClickListener hrlistener=new OnClickListener(){

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			
			
			List<BluetoothGattService> HRgattServices=mBluetoothLeService.getSupportedGattServices();
			
			String serviceUUID="0000fff0-0000-1000-8000-00805f9b34fb";
			
			for(int i=0;i<HRgattServices.size();i++){
				BluetoothGattService tempService=(BluetoothGattService)HRgattServices.get(i);
				if(tempService.getUuid().toString().equals(serviceUUID))
				{
					List<BluetoothGattCharacteristic> listchar=tempService.getCharacteristics();
					for(int j=0;j<listchar.size();j++){
						BluetoothGattCharacteristic tempChara=(BluetoothGattCharacteristic)listchar.get(j);
						if(tempChara.getUuid().toString().equals(hrUUID)){
							mNotifyCharacteristic = tempChara;
							mBluetoothLeService.setCharacteristicNotification(
									tempChara, true);
							showWave();
							tempChara=null;
						}
						
					}
				}
				
				tempService=null;
				
			}
			
			
			
		
		
		}
		
	};

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		if (mBluetoothLeService != null) {
			final boolean result = mBluetoothLeService.connect(mDeviceAddress);
			Log.d(TAG, "Connect request result=" + result);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mGattUpdateReceiver);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(mServiceConnection);
		mBluetoothLeService = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.gatt_services, menu);
		if (mConnected) {
			menu.findItem(R.id.menu_connect).setVisible(false);
			menu.findItem(R.id.menu_disconnect).setVisible(true);
		} else {
			menu.findItem(R.id.menu_connect).setVisible(true);
			menu.findItem(R.id.menu_disconnect).setVisible(false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_connect:
			mBluetoothLeService.connect(mDeviceAddress);
			return true;
		case R.id.menu_disconnect:
			mBluetoothLeService.disconnect();
			return true;
		case android.R.id.home:
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void updateConnectionState(final int resourceId) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mConnectionState.setText(resourceId);
			}
		});
	}

	private void displayData(String data) {
		if (data != null) {
			mDataField.setText(data);
		}
	}

	// Demonstrates how to iterate through the supported GATT
	// Services/Characteristics.
	// In this sample, we populate the data structure that is bound to the
	// ExpandableListView
	// on the UI.
	private void displayGattServices(List<BluetoothGattService> gattServices) {
		
		
		List<BluetoothGattService> HRgattServices=mBluetoothLeService.getSupportedGattServices();
		
		BluetoothGattService HRgattService=HRgattServices.get(0);
		
		
		
		
		
		if (gattServices == null)
			return;
		String uuid = null;
		String unknownServiceString = getResources().getString(
				R.string.unknown_service);
		String unknownCharaString = getResources().getString(
				R.string.unknown_characteristic);
		ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
		ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
		mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

		// Loops through available GATT Services.
		for (BluetoothGattService gattService : gattServices) {
			HashMap<String, String> currentServiceData = new HashMap<String, String>();
			uuid = gattService.getUuid().toString();
			currentServiceData.put(LIST_NAME,
					SampleGattAttributes.lookup(uuid, unknownServiceString));
			currentServiceData.put(LIST_UUID, uuid);
			gattServiceData.add(currentServiceData);

			ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
			List<BluetoothGattCharacteristic> gattCharacteristics = gattService
					.getCharacteristics();
			ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

			// Loops through available Characteristics.
			for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
				charas.add(gattCharacteristic);
				HashMap<String, String> currentCharaData = new HashMap<String, String>();
				uuid = gattCharacteristic.getUuid().toString();
				currentCharaData.put(LIST_NAME,
						SampleGattAttributes.lookup(uuid, unknownCharaString));
				currentCharaData.put(LIST_UUID, uuid);
				gattCharacteristicGroupData.add(currentCharaData);
			}
			mGattCharacteristics.add(charas);
			gattCharacteristicData.add(gattCharacteristicGroupData);
		}

		SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
				this, gattServiceData,
				android.R.layout.simple_expandable_list_item_2, new String[] {
						LIST_NAME, LIST_UUID }, new int[] { android.R.id.text1,
						android.R.id.text2 }, gattCharacteristicData,
				android.R.layout.simple_expandable_list_item_2, new String[] {
						LIST_NAME, LIST_UUID }, new int[] { android.R.id.text1,
						android.R.id.text2 });
		mGattServicesList.setVisibility(View.GONE);
		//mGattServicesList.setAdapter(gattServiceAdapter);
	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter
				.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		intentFilter.addAction(BluetoothLeService.ACTION_RSSI_AVAILABLE);
		return intentFilter;
	}
}

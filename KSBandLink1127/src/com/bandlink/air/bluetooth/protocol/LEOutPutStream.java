package com.bandlink.air.bluetooth.protocol;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import com.bandlink.air.MyLog;
import com.bandlink.air.ble.Converter;
import com.bandlink.air.ble.LogUtil;

@SuppressLint("NewApi")
public class LEOutPutStream extends OutputStream {

	private BluetoothGattDescriptor descriptor;
	private BluetoothGatt mBluetoothGatt;
	private BluetoothGattCharacteristic ccc;

	private byte[] temp;
	private int templen;
	private byte[] buf;

	public boolean bWriting = false;

	@Override
	public void write(byte[] buffer, int offset, int count) {
		// TODO Auto-generated method stub
		if (bLogFile)
			LogUtil.i(Calendar.getInstance().getTime().toLocaleString()
					+ "stream.write:  " + bWriting + "/ " + count + "/"
					+ Converter.byteArrayToHexString(buffer, 0, count));

		synchronized (datas) {
			datas.add(buffer);
		}

		if (!bWriting) {
			ContinueSend();
		}
	}
	public UUID getCharacteristicUUID(){
		return ccc.getUuid();
	}
	Timer mFindTimer = null;

	void writeba(byte[] buffer, int offset, int count) {

		if (ccc != null && mBluetoothGatt != null && buffer != null) {

			if (count > 20) {
				buf = new byte[20];
				templen = count - 20;
				System.arraycopy(buffer, 0, buf, 0, 20);
				System.arraycopy(buffer, 20, temp, 0, templen);

			} else {
				buf = new byte[count];
				templen = 0;
				System.arraycopy(buffer, 0, buf, 0, count);
			}

			bWriting = true;
			ccc.setValue(buf);
			mBluetoothGatt.writeCharacteristic(ccc);
			// MyLog.i("rrrr", "stream writeba 20: " + bWriting + "/ " +
			// buf.length + "/"+ Converter.byteArrayToHexString(buf));

			TimerTask task = new TimerTask() {
				public void run() {
					// execute the task
					bWriting = false;
					synchronized (datas) {
						datas.clear();
					}
				}
			};
			try {
				if (mFindTimer != null) {
					mFindTimer.cancel();
				}
				mFindTimer = new Timer();
				mFindTimer.schedule(task, 10000);
			} catch (Exception e) {
				// TODO: handle exception
			}

			/*
			 * new Handler().postDelayed(new Runnable() {
			 * 
			 * @Override public void run() { // TODO Auto-generated method stub
			 * 
			 * bWriting = false; MyLog.i("rrrr", "stream postdelayed: " +
			 * bWriting); } }, 10000);
			 */
			final StringBuilder stringBuilder = new StringBuilder(buf.length);
			for (byte byteChar : buf)
				stringBuilder.append(String.format("%02X ", byteChar));
			if (bLogFile) {
				LogUtil.e("Send:" + stringBuilder.toString());
			}

		}
	}

	public void ContinueSend() {
		if (templen > 0) {
			writeba(temp, 0, templen);
			MyLog.e("BLE", "Continue send");
		} else {
			byte[] ba = null;
			synchronized (datas) {
				if (datas.size() > 0) {
					ba = datas.get(0);
					datas.remove(0);
				}
			}
			if (ba != null)
				writeba(ba, 0, ba.length);
			else {
				bWriting = false;
				// MyLog.i("rrrr", "stream ContinueSend: " + bWriting);
			}

		}
	}

	ArrayList<byte[]> datas = new ArrayList<byte[]>();

	public static boolean bLogFile = false;

	public LEOutPutStream(BluetoothGatt att, BluetoothGattCharacteristic c,
			boolean blog) {
		mBluetoothGatt = att;
		ccc = c;
		temp = new byte[1024];
		bLogFile = blog;
	}

	@Override
	public void write(byte[] buffer) throws IOException {
		// TODO Auto-generated method stub
		super.write(buffer);
		if (descriptor != null && mBluetoothGatt != null && buffer != null) {
			descriptor.setValue(buffer);
			mBluetoothGatt.writeDescriptor(descriptor);
		}
	}

	@Override
	public void write(int arg0) throws IOException {
		// TODO Auto-generated method stub

	}
}

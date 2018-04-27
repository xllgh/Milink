package com.bandlink.air.util;

import android.bluetooth.BluetoothDevice;

public class AirDevice {

	public BluetoothDevice device;
	public int rssi;
	public AirDevice(BluetoothDevice d,int r)
	{
		device = d;
		rssi = r;
	}
}

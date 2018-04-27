package com.bandlink.air.bluetooth.protocol;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Calendar;

import org.apache.http.util.ByteArrayBuffer;

import com.bandlink.air.MyLog;
import com.bandlink.air.ble.Converter;
import com.bandlink.air.ble.LogUtil;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.format.Time;
import android.util.Log;

public class ComposePacket {

	long text1 = 0;
	long text2 = 1;
	long text3 = 1;
	// private Crc16 crc;
	private Context ContextRef = null;

	public ComposePacket(Context c) {
		ContextRef = c;
		// crc = new Crc16();
	}

	public byte[] ComposeLEConnect() {
		byte[] temp = new byte[19];

		temp[0] = (byte) 0xa7;
		temp[1] = (byte) 0xb8;
		temp[2] = (byte) 0x00;
		temp[3] = (byte) 0x01;

		temp[4] = (byte) ((temp.length >> 24) & 0xff);
		temp[5] = (byte) ((temp.length >> 16) & 0xff);
		temp[6] = (byte) ((temp.length >> 8) & 0xff);
		temp[7] = (byte) (temp.length & 0xff);

		temp[8] = (byte) (4);
		temp[9] = (byte) (0);

		Time time = new Time();
		time.setToNow();
		temp[10] = (byte) ((time.year >> 8) & 0xff);
		temp[11] = (byte) (time.year & 0xff);

		temp[12] = (byte) (time.month + 1);
		temp[13] = (byte) (time.monthDay);
		temp[14] = (byte) (time.hour);
		temp[15] = (byte) (time.minute);
		temp[16] = (byte) (time.second);
		// crc.AddCrc16(temp, temp.length - 2);
		MLinkCRC.crc16(temp, temp.length - 2);
		return temp;
	}

	/***
	 * 修改连接周期
	 * 
	 * @param value
	 *            1是500ms 0是60ms
	 * @return
	 */
	public byte[] ComposePacket0805(int value) {
		byte[] temp = new byte[15];

		temp[0] = (byte) 0xa7;
		temp[1] = (byte) 0xb8;
		temp[2] = (byte) 0x00;
		temp[3] = (byte) 0x01;

		temp[4] = (byte) ((temp.length >> 24) & 0xff);
		temp[5] = (byte) ((temp.length >> 16) & 0xff);
		temp[6] = (byte) ((temp.length >> 8) & 0xff);
		temp[7] = (byte) (temp.length & 0xff);

		temp[8] = (byte) (0x90);
		temp[9] = (byte) (0);
		temp[10] = (byte) (0x90);
		temp[11] = (byte) (10);

		temp[12] = (byte) (value);

		// crc.AddCrc16(temp, temp.length - 2);
		MLinkCRC.crc16(temp, temp.length - 2);
		return temp;
	}

	@Deprecated
	public byte[] ComposeLEConfig() {
		byte[] temp = new byte[44];

		temp[0] = (byte) 0xa7;
		temp[1] = (byte) 0xb8;
		temp[2] = (byte) 0x00;
		temp[3] = (byte) 0x01;

		temp[4] = (byte) ((temp.length >> 24) & 0xff);
		temp[5] = (byte) ((temp.length >> 16) & 0xff);
		temp[6] = (byte) ((temp.length >> 8) & 0xff);
		temp[7] = (byte) (temp.length & 0xff);

		temp[8] = (byte) (0x9e);
		temp[9] = (byte) (1);

		// 身高 0x9E01
		temp[10] = (byte) 0x9e;
		temp[11] = 1;
		// 体重 0x9E02
		temp[12] = (byte) 0x9e;
		temp[13] = 2;
		// 性别 0x9E03
		temp[14] = (byte) 0x9e;
		temp[15] = 3;
		// 步长 0x9E04
		temp[16] = (byte) 0x9e;
		temp[17] = 4;
		// 振动 0x9E05
		temp[18] = (byte) 0x9e;
		temp[19] = 5;
		// 实时同步开关 0x9E06
		temp[20] = (byte) 0x9e;
		temp[21] = 6;
		// 闹钟 0x9E07
		temp[22] = (byte) 0x9e;
		temp[23] = 7;
		// 年龄 0x9E08
		temp[24] = (byte) 0x9e;
		temp[25] = 8;
		// 防丢开关 0x9E10
		temp[26] = (byte) 0x9e;
		temp[27] = (byte) 0x10;
		// 防丢距离 0x9E11
		temp[28] = (byte) 0x9e;
		temp[29] = (byte) 0x11;
		// 防丢等级 0x9E12
		temp[30] = (byte) 0x9e;
		temp[31] = (byte) 0x12;
		// 响应延迟 0x9E13
		temp[32] = (byte) 0x9e;
		temp[33] = (byte) 0x13;
		// 设备名称 0x9E14
		temp[34] = (byte) 0x9e;
		temp[35] = (byte) 0x14;
		// 设备ID 0x9E15
		temp[36] = (byte) 0x9e;
		temp[37] = (byte) 0x15;
		// 软件版本号 0x9E16
		temp[38] = (byte) 0x9e;
		temp[39] = (byte) 0x16;
		// 硬件版本号 0x9E17
		temp[40] = (byte) 0x9e;
		temp[41] = (byte) 0x17;
		// BOOT版本号 0x9E18
		// temp[42] = (byte)0x9e;
		// temp[43] = (byte)0x18;
		// 生产日期 0x9E19
		// temp[44] = (byte)0x9e;
		// temp[45] = (byte)0x19;

		MLinkCRC.crc16(temp, temp.length - 2);
		return temp;
	}

	/***
	 * 关闭、开启 Air防丢，暂时睡眠期间用
	 * 
	 * @param isOn
	 * @return
	 */
	public byte[] ComposeLostOnNight(int isOn) {
		byte[] temp = new byte[16];
		temp[0] = (byte) 0xa7;
		temp[1] = (byte) 0xb8;
		temp[2] = (byte) 0x00;
		temp[3] = (byte) 0x01;

		temp[4] = (byte) ((temp.length >> 24) & 0xff);
		temp[5] = (byte) ((temp.length >> 16) & 0xff);
		temp[6] = (byte) ((temp.length >> 8) & 0xff);
		temp[7] = (byte) (temp.length & 0xff);

		temp[8] = (byte) (0x9e);
		temp[9] = (byte) (0);

		temp[10] = (byte) (0x9e);
		temp[11] = (byte) (10);

		temp[12] = (byte) (1);
		temp[13] = (byte) (isOn);
		MLinkCRC.crc16(temp, temp.length - 2);

		return temp;

	}

	public byte[] ComposeSendPower() {
		byte[] temp = new byte[14];
		temp[0] = (byte) 0xa7;
		temp[1] = (byte) 0xb8;
		temp[2] = (byte) 0x00;
		temp[3] = (byte) 0x01;

		temp[4] = (byte) ((temp.length >> 24) & 0xff);
		temp[5] = (byte) ((temp.length >> 16) & 0xff);
		temp[6] = (byte) ((temp.length >> 8) & 0xff);
		temp[7] = (byte) (temp.length & 0xff);

		temp[8] = (byte) (0x9e);
		temp[9] = (byte) (1);

		temp[10] = (byte) (0x9e);
		temp[11] = (byte) (0x09);
		MLinkCRC.crc16(temp, temp.length - 2);

		return temp;
	}

	byte[] composeConfigPacket_power(int level) {
		byte[] ba4 = new byte[] { (byte) 0xA7, (byte) 0xB8, 00, 01, 00, 00, 00,
				(byte) 0x10, (byte) 0x9e, 00, (byte) 0x9e, (byte) 0x09, 0x01,
				0x00, 0x00, 0x00 };
		byte pp = (byte) level;
		ba4[13] = pp;
		MLinkCRC.crc16(ba4, ba4.length - 2);
		return ba4;
	}

	public byte[] ComposeLEConfig2() {
		byte[] temp = new byte[38];

		temp[0] = (byte) 0xa7;
		temp[1] = (byte) 0xb8;
		temp[2] = (byte) 0x00;
		temp[3] = (byte) 0x01;

		temp[4] = (byte) ((temp.length >> 24) & 0xff);
		temp[5] = (byte) ((temp.length >> 16) & 0xff);
		temp[6] = (byte) ((temp.length >> 8) & 0xff);
		temp[7] = (byte) (temp.length & 0xff);

		temp[8] = (byte) (0x9e);
		temp[9] = (byte) (1);
		int i = 10;
		// 身高 0x9E01
		temp[i++] = (byte) 0x9e;
		temp[i++] = 1;
		// 体重 0x9E02
		temp[i++] = (byte) 0x9e;
		temp[i++] = 2;
		// 性别 0x9E03
		temp[i++] = (byte) 0x9e;
		temp[i++] = 3;
		// 步长 0x9E04
		temp[i++] = (byte) 0x9e;
		temp[i++] = 4;
		// 振动 0x9E05
		temp[i++] = (byte) 0x9e;
		temp[i++] = 5;

		// 闹钟 0x9E07
		temp[i++] = (byte) 0x9e;
		temp[i++] = 7;
		// 发射功率 0x9E09
		temp[i++] = (byte) 0x9e;
		temp[i++] = 9;
		// 运动数据 语言 抬腕
		temp[i++] = (byte) 0x9e;
		temp[i++] = (byte) 0x0d;
		// 防丢开关 0x9E10
		temp[i++] = (byte) 0x9e;
		temp[i++] = (byte) 0x10;

		// 响应延迟 0x9E13
		temp[i++] = (byte) 0x9e;
		temp[i++] = (byte) 0x13;
		// 设备名称 0x9E14
		temp[i++] = (byte) 0x9e;
		temp[i++] = (byte) 0x14;
		// did 0x9E15
		temp[i++] = (byte) 0x9e;
		temp[i++] = (byte) 0x15;
		// 软件版本号 0x9E16
		temp[i++] = (byte) 0x9e;
		temp[i++] = (byte) 0x16;

		// 硬件版本号 0x9E17
		// temp[i++] = (byte)0x9e;
		// temp[i++] = (byte)0x17;
		// BOOT版本号 0x9E18
		// temp[42] = (byte)0x9e;
		// temp[43] = (byte)0x18;
		// 生产日期 0x9E19
		// temp[44] = (byte)0x9e;
		// temp[45] = (byte)0x19;

		MLinkCRC.crc16(temp, temp.length - 2);
		return temp;
	}

	public byte[] ComposeAlarmConfig() {
		byte[] temp = new byte[14];

		temp[0] = (byte) 0xa7;
		temp[1] = (byte) 0xb8;
		temp[2] = (byte) 0x00;
		temp[3] = (byte) 0x01;

		temp[4] = (byte) ((temp.length >> 24) & 0xff);
		temp[5] = (byte) ((temp.length >> 16) & 0xff);
		temp[6] = (byte) ((temp.length >> 8) & 0xff);
		temp[7] = (byte) (temp.length & 0xff);

		temp[8] = (byte) (0x9e);
		temp[9] = (byte) (1);
		int i = 10;

		// 闹钟 0x9E07
		temp[i++] = (byte) 0x9e;
		temp[i++] = 7;

		MLinkCRC.crc16(temp, temp.length - 2);
		return temp;
	}

	public byte[] ComposeReadVersion() {
		byte[] temp = new byte[14];

		temp[0] = (byte) 0xa7;
		temp[1] = (byte) 0xb8;
		temp[2] = (byte) 0x00;
		temp[3] = (byte) 0x01;

		temp[4] = (byte) ((temp.length >> 24) & 0xff);
		temp[5] = (byte) ((temp.length >> 16) & 0xff);
		temp[6] = (byte) ((temp.length >> 8) & 0xff);
		temp[7] = (byte) (temp.length & 0xff);

		temp[8] = (byte) (0x9e);
		temp[9] = (byte) (1);
		int i = 10;
		temp[i++] = (byte) 0x9e;
		temp[i++] = 0x16;

		MLinkCRC.crc16(temp, temp.length - 2);
		return temp;
	}

	public byte[] ComposeCall() {
		byte[] temp = new byte[17];

		temp[0] = (byte) 0xa7;
		temp[1] = (byte) 0xb8;
		temp[2] = (byte) 0x00;
		temp[3] = (byte) 0x01;

		temp[4] = (byte) ((temp.length >> 24) & 0xff);
		temp[5] = (byte) ((temp.length >> 16) & 0xff);
		temp[6] = (byte) ((temp.length >> 8) & 0xff);
		temp[7] = (byte) (temp.length & 0xff);

		temp[8] = (byte) (0X90);
		temp[9] = (byte) (0);

		temp[10] = (byte) (0X90);
		temp[11] = (byte) (2);

		temp[12] = (byte) (0);
		temp[13] = (byte) (1);
		temp[14] = (byte) (0);

		// crc.AddCrc16(temp, temp.length - 2);
		MLinkCRC.crc16(temp, temp.length - 2);
		return temp;
	}

	public byte[] ComposeCall2(String name) {
		if (name.length() > 3) {
			// the max length is 3
			name = name.substring(0, 3);
		}
		int len = name.length();
		byte[] data = new byte[name.length() * 2];
		byte[] tempdata = null;

		byte[] temp = new byte[15 + 32 * len];

		temp[0] = (byte) 0xa7;
		temp[1] = (byte) 0xb8;
		temp[2] = (byte) 0x00;
		temp[3] = (byte) 0x01;

		temp[4] = (byte) ((temp.length >> 24) & 0xff);
		temp[5] = (byte) ((temp.length >> 16) & 0xff);
		temp[6] = (byte) ((temp.length >> 8) & 0xff);
		temp[7] = (byte) (temp.length & 0xff);

		temp[8] = (byte) (0X90);
		temp[9] = (byte) (0);

		temp[10] = (byte) (0X90);
		temp[11] = (byte) (1);

		temp[12] = (byte) (32 * len);

		try {
			tempdata = name.getBytes("gb2312");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (int i = 0, j = 0; i < tempdata.length; j = j + 2) {
			if ((tempdata[i] & 0xff) < 0xa0) {
				data[j] = (byte) (0x03 + 0xa0);
				data[j + 1] = (byte) ((tempdata[i] & 0xff) - 0x20 + 0xa0);
				i++;
			} else {
				if (i + 1 < tempdata.length) {
					data[j] = tempdata[i];
					data[j + 1] = tempdata[i + 1];
					i = i + 2;
				} else {
					i++;
				}
			}
		}

		StringBuilder str_gb = new StringBuilder();
		for (int i = 0; i < data.length; i++) {
			str_gb.append(String.format("%1$02X", data[i]));
		}

		MyLog.e("MILINK", "gb2312:" + str_gb.toString());
		byte[] fonbytes = null;
		switch (data.length) {
		case 2:
			text1 = ((((data[0] & 0xff) - 161)) * 94 + ((data[1] & 0xff) - 161)) * 32;
			fonbytes = getGBFont(text1, 0, 0);
			break;
		case 4:
			text1 = ((((data[0] & 0xff) - 161)) * 94 + ((data[1] & 0xff) - 161)) * 32;
			text2 = ((((data[2] & 0xff) - 161)) * 94 + ((data[3] & 0xff) - 161)) * 32;
			fonbytes = getGBFont(text1, text2, 0);
			break;
		case 6:
			text1 = ((((data[0] & 0xff) - 161)) * 94 + ((data[1] & 0xff) - 161)) * 32;
			text2 = ((((data[2] & 0xff) - 161)) * 94 + ((data[3] & 0xff) - 161)) * 32;
			text3 = ((((data[4] & 0xff) - 161)) * 94 + ((data[5] & 0xff) - 161)) * 32;
			fonbytes = getGBFont(text1, text2, text3);
			break;
		default:
			break;
		}
		if (fonbytes != null) {
			System.arraycopy(fonbytes, 0, temp, 13, fonbytes.length);
		}

		// crc.AddCrc16(temp, temp.length - 2);
		MLinkCRC.crc16(temp, temp.length - 2);

		StringBuilder str_p = new StringBuilder();
		for (int i = 0; i < temp.length; i++) {
			str_p.append(String.format("0x%1$02X,", temp[i]));
		}

		MyLog.e("MILINK", "call2:" + str_p.toString());
		return temp;
	}

	public byte[] ComposeCallWithNumber(String number) {

		byte[] temp = new byte[15 + number.length()];

		temp[0] = (byte) 0xa7;
		temp[1] = (byte) 0xb8;
		temp[2] = (byte) 0x00;
		temp[3] = (byte) 0x01;

		temp[4] = (byte) ((temp.length >> 24) & 0xff);
		temp[5] = (byte) ((temp.length >> 16) & 0xff);
		temp[6] = (byte) ((temp.length >> 8) & 0xff);
		temp[7] = (byte) (temp.length & 0xff);

		temp[8] = (byte) (0X90);
		temp[9] = (byte) (0);

		temp[10] = (byte) (0X90);
		temp[11] = (byte) (2);

		temp[12] = (byte) (number.length());

		for (int x = 0; x < number.length(); x++) {
			temp[13 + x] = (byte) number.charAt(x);
		}

		// crc.AddCrc16(temp, temp.length - 2);
		MLinkCRC.crc16(temp, temp.length - 2);

		StringBuilder str_p = new StringBuilder();
		for (int i = 0; i < temp.length; i++) {
			str_p.append(String.format("0x%1$02X,", temp[i]));
		}

		MyLog.e("MILINK", "call2:" + str_p.toString());
		return temp;
	}

	public byte[] ComposeCallEnd() {
		byte[] temp = new byte[16];

		temp[0] = (byte) 0xa7;
		temp[1] = (byte) 0xb8;
		temp[2] = (byte) 0x00;
		temp[3] = (byte) 0x01;

		temp[4] = (byte) ((temp.length >> 24) & 0xff);
		temp[5] = (byte) ((temp.length >> 16) & 0xff);
		temp[6] = (byte) ((temp.length >> 8) & 0xff);
		temp[7] = (byte) (temp.length & 0xff);

		temp[8] = (byte) (0X90);
		temp[9] = (byte) (0);

		temp[10] = (byte) (0X90);
		temp[11] = (byte) (3);

		temp[12] = (byte) (1);
		temp[13] = (byte) (0);

		// crc.AddCrc16(temp, temp.length - 2);
		MLinkCRC.crc16(temp, temp.length - 2);
		return temp;
	}

	public byte[] ComposeMSG(int value) {
		byte[] temp = new byte[16];

		temp[0] = (byte) 0xa7;
		temp[1] = (byte) 0xb8;
		temp[2] = (byte) 0x00;
		temp[3] = (byte) 0x01;

		temp[4] = (byte) ((temp.length >> 24) & 0xff);
		temp[5] = (byte) ((temp.length >> 16) & 0xff);
		temp[6] = (byte) ((temp.length >> 8) & 0xff);
		temp[7] = (byte) (temp.length & 0xff);

		temp[8] = (byte) (0x90);
		temp[9] = (byte) (0);

		temp[10] = (byte) (0X90);
		temp[11] = (byte) (5);

		temp[12] = (byte) (1);
		temp[13] = (byte) (value);

		// crc.AddCrc16(temp, temp.length - 2);
		MLinkCRC.crc16(temp, temp.length - 2);
		return temp;
	}

	public byte[] ComposeDeviceOut() {
		byte[] temp = new byte[16];

		temp[0] = (byte) 0xa7;
		temp[1] = (byte) 0xb8;
		temp[2] = (byte) 0x00;
		temp[3] = (byte) 0x01;

		temp[4] = (byte) ((temp.length >> 24) & 0xff);
		temp[5] = (byte) ((temp.length >> 16) & 0xff);
		temp[6] = (byte) ((temp.length >> 8) & 0xff);
		temp[7] = (byte) (temp.length & 0xff);

		temp[8] = (byte) (0x90);
		temp[9] = (byte) (0);

		temp[10] = (byte) (0X90);
		temp[11] = (byte) (6);

		temp[12] = (byte) (1);
		temp[13] = (byte) (1);

		// crc.AddCrc16(temp, temp.length - 2);
		MLinkCRC.crc16(temp, temp.length - 2);
		return temp;
	}

	public byte[] ComposeDeviceIn() {
		byte[] temp = new byte[16];

		temp[0] = (byte) 0xa7;
		temp[1] = (byte) 0xb8;
		temp[2] = (byte) 0x00;
		temp[3] = (byte) 0x01;

		temp[4] = (byte) ((temp.length >> 24) & 0xff);
		temp[5] = (byte) ((temp.length >> 16) & 0xff);
		temp[6] = (byte) ((temp.length >> 8) & 0xff);
		temp[7] = (byte) (temp.length & 0xff);

		temp[8] = (byte) (0x90);
		temp[9] = (byte) (0);

		temp[10] = (byte) (0X90);
		temp[11] = (byte) (7);

		temp[12] = (byte) (1);
		temp[13] = (byte) (1);

		// crc.AddCrc16(temp, temp.length - 2);
		MLinkCRC.crc16(temp, temp.length - 2);
		return temp;
	}

	public byte[] ComposeConnect() {
		byte[] temp = new byte[] { (byte) 0xa7, (byte) 0xb8, 0x00, 0x01, 0, 0,
				0, 0, 0x01, 0x00, 0x00, 0x00 };
		temp[4] = (byte) ((temp.length >> 24) & 0xff);
		temp[5] = (byte) ((temp.length >> 16) & 0xff);
		temp[6] = (byte) ((temp.length >> 8) & 0xff);
		temp[7] = (byte) (temp.length & 0xff);
		// crc.AddCrc16(temp, temp.length - 2);
		MLinkCRC.crc16(temp, temp.length - 2);
		return temp;
	}

	byte[] composeVibrateEn(boolean vibrate_en) {
		byte[] temp = new byte[16];

		temp[0] = (byte) 0xa7;
		temp[1] = (byte) 0xb8;
		temp[2] = (byte) 0x00;
		temp[3] = (byte) 0x01;

		temp[4] = (byte) ((temp.length >> 24) & 0xff);
		temp[5] = (byte) ((temp.length >> 16) & 0xff);
		temp[6] = (byte) ((temp.length >> 8) & 0xff);
		temp[7] = (byte) (temp.length & 0xff);

		temp[8] = (byte) (0x9e);
		temp[9] = (byte) (0);

		temp[10] = (byte) (0X9e);
		temp[11] = (byte) (5);

		temp[12] = (byte) (1);

		if (vibrate_en) {
			temp[13] = 1;
		} else {
			temp[13] = 0;
		}

		// crc.AddCrc16(temp, temp.length - 2);
		MLinkCRC.crc16(temp, temp.length - 2);
		return temp;

	}

	public byte[] ComposeAlarm(boolean alarm1, int a1_h, int a1_m,
			boolean alarm2, int a2_h, int a2_m, String week1, String week2) {
		byte[] temp = new byte[25];

		temp[0] = (byte) 0xa7;
		temp[1] = (byte) 0xb8;
		temp[2] = (byte) 0x00;
		temp[3] = (byte) 0x01;

		temp[4] = (byte) ((temp.length >> 24) & 0xff);
		temp[5] = (byte) ((temp.length >> 16) & 0xff);
		temp[6] = (byte) ((temp.length >> 8) & 0xff);
		temp[7] = (byte) (temp.length & 0xff);

		temp[8] = (byte) (0x9e);
		temp[9] = (byte) (0);

		temp[10] = (byte) (0X9e);
		temp[11] = (byte) (7);

		temp[12] = (byte) (6);

		if (alarm1) {
			temp[13] = 1;
		} else {
			temp[13] = 0;
		}

		temp[14] = (byte) (a1_h);
		temp[15] = (byte) (a1_m);

		if (alarm2) {
			temp[16] = 1;
		} else {
			temp[16] = 0;
		}
		temp[17] = (byte) (a2_h);
		temp[18] = (byte) (a2_m);

		temp[19] = (byte) (0x9e);
		temp[20] = (byte) (0x23);
		temp[21] = (byte) ((int) Integer.valueOf(binaryString2hexString("0"
				+ week1.replace("-", "")), 16));
		temp[22] = (byte) ((int) Integer.valueOf(binaryString2hexString("0"
				+ week2.replace("-", "")), 16));

		// crc.AddCrc16(temp, temp.length - 2);
		MLinkCRC.crc16(temp, temp.length - 2);
		return temp;
	}

	public static String binaryString2hexString(String bString) {
		if (bString == null || bString.equals("") || bString.length() % 8 != 0)
			return null;
		StringBuffer tmp = new StringBuffer();
		int iTmp = 0;
		for (int i = 0; i < bString.length(); i += 4) {
			iTmp = 0;
			for (int j = 0; j < 4; j++) {
				iTmp += Integer.parseInt(bString.substring(i + j, i + j + 1)) << (4 - j - 1);
			}
			tmp.append(Integer.toHexString(iTmp));
		}
		return tmp.toString();
	}

	/***
	 * 防丢+延时
	 * 
	 * @param isOn
	 *            1开启 0关闭
	 * @param delay
	 *            报警延时
	 * @return
	 */
	public byte[] ComposeAirSet(boolean isOn, byte delay, int languge,
			int handup, int height) {
		byte[] temp = new byte[34];
		// 报文头 4位
		temp[0] = (byte) 0xa7;
		temp[1] = (byte) 0xb8;
		temp[2] = (byte) 0x00;
		temp[3] = (byte) 0x01;
		// 长度 4位
		temp[4] = (byte) ((temp.length >> 24) & 0xff);
		temp[5] = (byte) ((temp.length >> 16) & 0xff);
		temp[6] = (byte) ((temp.length >> 8) & 0xff);
		temp[7] = (byte) (temp.length & 0xff);
		// 报文ID 2位
		temp[8] = (byte) (0x9e);
		temp[9] = (byte) (0);

		// 防丢标签 标签值
		temp[10] = (byte) (0X9e);
		temp[11] = (byte) (16);
		// 长度
		temp[12] = (byte) (1);
		if (isOn) {
			temp[13] = 1;
		} else {
			temp[13] = 0;
		}

		// 延时标签 标签值
		temp[14] = (byte) (0X9e);
		temp[15] = (byte) (0x0f);
		// 长度
		temp[16] = (byte) (1);
		if (isOn) {
			temp[17] = (byte) (0x80 + delay);
		} else {
			temp[17] = delay;
		}
		// 语言设置.抬腕显示设置
		temp[18] = (byte) (0X9e);
		temp[19] = (byte) (0x0d);
		// 长度
		temp[20] = (byte) (3);

		temp[21] = (byte) (1);

		temp[22] = (byte) (languge);

		temp[23] = (byte) (handup);
		// 身高
		temp[24] = (byte) (0X9e);
		temp[25] = (byte) (0x01);
		// 长度
		temp[26] = (byte) (1);
		temp[27] = (byte) (height);
		// 不会关闭设备振动
		temp[28] = (byte) (0X9e);
		temp[29] = (byte) (5);

		temp[30] = (byte) (1);
		temp[31] = 1;

		// 2.2.9 亮屏时写9e09 屏幕不灭
		// temp[14] = (byte) (0X9e);
		// temp[15] = (byte) (0X09);
		// temp[16] = (byte) (0X01);
		// temp[17] = (byte) (0X04);
		// crc.AddCrc16(temp, temp.length - 2);
		MLinkCRC.crc16(temp, temp.length - 2);
		return temp;
	}

	public byte[] ComposeAirDisplay(int hasStep, int languge, int handup) {
		byte[] temp = new byte[18];
		// 报文头 4位
		temp[0] = (byte) 0xa7;
		temp[1] = (byte) 0xb8;
		temp[2] = (byte) 0x00;
		temp[3] = (byte) 0x01;
		// 长度 4位
		temp[4] = (byte) ((temp.length >> 24) & 0xff);
		temp[5] = (byte) ((temp.length >> 16) & 0xff);
		temp[6] = (byte) ((temp.length >> 8) & 0xff);
		temp[7] = (byte) (temp.length & 0xff);
		// 报文ID 2位
		temp[8] = (byte) (0x9e);
		temp[9] = (byte) (0);

		// 语言设置.抬腕显示设置
		temp[10] = (byte) (0X9e);
		temp[11] = (byte) (0x0d);
		// 长度
		temp[12] = (byte) (3);

		temp[13] = (byte) (1);

		temp[14] = (byte) (languge);

		temp[15] = (byte) (handup);

		// 2.2.9 亮屏时写9e09 屏幕不灭
		// temp[14] = (byte) (0X9e);
		// temp[15] = (byte) (0X09);
		// temp[16] = (byte) (0X01);
		// temp[17] = (byte) (0X04);
		// crc.AddCrc16(temp, temp.length - 2);
		MLinkCRC.crc16(temp, temp.length - 2);
		return temp;
	}

	// 延时
	public byte[] ComposeLostDelay(byte delay) {
		byte[] temp = new byte[16];
		// 报文头 4位
		temp[0] = (byte) 0xa7;
		temp[1] = (byte) 0xb8;
		temp[2] = (byte) 0x00;
		temp[3] = (byte) 0x01;
		// 长度 4位
		temp[4] = (byte) ((temp.length >> 24) & 0xff);
		temp[5] = (byte) ((temp.length >> 16) & 0xff);
		temp[6] = (byte) ((temp.length >> 8) & 0xff);
		temp[7] = (byte) (temp.length & 0xff);
		// 报文ID 2位
		temp[8] = (byte) (0x9e);
		temp[9] = (byte) (0);

		// 延时标签 标签值
		temp[10] = (byte) (0X9e);
		temp[11] = (byte) (0x0f);
		// 长度
		temp[12] = (byte) (1);
		temp[13] = (byte) (0x80 + delay);
		// crc.AddCrc16(temp, temp.length - 2);
		MLinkCRC.crc16(temp, temp.length - 2);
		return temp;
	}

	// 防丢
	public byte[] ComposeLost(int delay) {
		byte[] temp = new byte[16];
		// 报文头 4位
		temp[0] = (byte) 0xa7;
		temp[1] = (byte) 0xb8;
		temp[2] = (byte) 0x00;
		temp[3] = (byte) 0x01;
		// 长度 4位
		temp[4] = (byte) ((temp.length >> 24) & 0xff);
		temp[5] = (byte) ((temp.length >> 16) & 0xff);
		temp[6] = (byte) ((temp.length >> 8) & 0xff);
		temp[7] = (byte) (temp.length & 0xff);
		// 报文ID 2位
		temp[8] = (byte) (0x9e);
		temp[9] = (byte) (0);

		// 延时标签 标签值
		temp[10] = (byte) (0X9e);
		temp[11] = (byte) (16);
		// 长度
		temp[12] = (byte) (1);
		temp[13] = (byte) (delay);
		// crc.AddCrc16(temp, temp.length - 2);
		MLinkCRC.crc16(temp, temp.length - 2);
		return temp;
	}

	byte[] composeConfigPacket_name(String name) {
		byte[] BA_BLE_NAME = { (byte) 0xA7, (byte) 0xB8, (byte) 0x00,
				(byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x10, (byte) 0x9e, (byte) 0x00, (byte) 0x9e,
				(byte) 0x14, (byte) 0x0b, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0xFC, (byte) 0xFD, };
		if (name.length() > 10) {
			return null;
		}
		byte[] aa = name.getBytes();

		int len = BA_BLE_NAME.length;
		byte[] ba1 = new byte[len];
		System.arraycopy(BA_BLE_NAME, 0, ba1, 0, 13);
		System.arraycopy(aa, 0, ba1, 13, aa.length);

		ba1[4] = (byte) (len / 16777216 % 256);
		ba1[5] = (byte) (len / 65536 % 256);
		ba1[6] = (byte) (len / 256 % 256);
		ba1[7] = (byte) (len % 256);

		ba1[len - 2] = (byte) 0xfc;
		ba1[len - 1] = (byte) 0xfd;
		MLinkCRC.crc16(ba1, ba1.length - 2);
		return ba1;

	}

	public byte[] ComposeConnect(byte type) {
		byte[] temp = new byte[] { (byte) 0xa7, (byte) 0xb8, 0x00, 0x01, 0, 0,
				0, 0, 0x01, 0x00,

				0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a,
				0x0b, 0x0c, 0x00, 0x00, 0x00, 0x01,

				0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 0x00, 0x00 };
		temp[9] = type;
		temp[4] = (byte) ((temp.length >> 24) & 0xff);
		temp[5] = (byte) ((temp.length >> 16) & 0xff);
		temp[6] = (byte) ((temp.length >> 8) & 0xff);
		temp[7] = (byte) (temp.length & 0xff);
		// crc.AddCrc16(temp, temp.length - 2);
		MLinkCRC.crc16(temp, temp.length - 2);
		return temp;
	}

	public byte[] ComposeDeviceInfo() {
		byte[] temp = new byte[] { (byte) 0xa7, (byte) 0xb8, 0x00, 0x01, 0, 0,
				0, 0, 0x02, 0x00, 0x00, 0x00 };
		temp[4] = (byte) ((temp.length >> 24) & 0xff);
		temp[5] = (byte) ((temp.length >> 16) & 0xff);
		temp[6] = (byte) ((temp.length >> 8) & 0xff);
		temp[7] = (byte) (temp.length & 0xff);
		// crc.AddCrc16(temp, temp.length - 2);
		MLinkCRC.crc16(temp, temp.length - 2);
		return temp;
	}

	public byte[] ComposeData(byte type) {
		byte[] temp = new byte[] { (byte) 0xa7, (byte) 0xb8, 0x00, 0x01, 0, 0,
				0, 0, 0x08, 0x00, 0x00, 0x00 };

		temp[9] = type;

		temp[4] = (byte) ((temp.length >> 24) & 0xff);
		temp[5] = (byte) ((temp.length >> 16) & 0xff);
		temp[6] = (byte) ((temp.length >> 8) & 0xff);
		temp[7] = (byte) (temp.length & 0xff);
		// crc.AddCrc16(temp, temp.length - 2);
		MLinkCRC.crc16(temp, temp.length - 2);
		return temp;
	}

	public byte[] ComposeSleepData(byte type, byte pos) {
		byte[] temp = new byte[] { (byte) 0xa7, (byte) 0xb8, 0x00, 0x01, 0, 0,
				0, 0, 0x08, 0x00, 0x00, 0x00, 0x00 };

		temp[9] = type;
		temp[10] = pos;

		temp[4] = (byte) ((temp.length >> 24) & 0xff);
		temp[5] = (byte) ((temp.length >> 16) & 0xff);
		temp[6] = (byte) ((temp.length >> 8) & 0xff);
		temp[7] = (byte) (temp.length & 0xff);
		// crc.AddCrc16(temp, temp.length - 2);
		MLinkCRC.crc16(temp, temp.length - 2);
		return temp;
	}

	public byte[] ComposePacket04(int step, int dis, int cal) {
		// A7 B8 00 01 00 00 00 XX 08 01 07 DC 01 10 0E 00 00 00 CRC1 CRC2
		Calendar c = Calendar.getInstance();
		// byte[] temp = new byte[22];
		byte[] temp = new byte[30];
		temp[0] = (byte) 0xa7;
		temp[1] = (byte) 0xb8;
		temp[2] = 0x00;
		temp[3] = 0x01;

		// 长度 4位
		temp[4] = (byte) ((temp.length >> 24) & 0xff);
		temp[5] = (byte) ((temp.length >> 16) & 0xff);
		temp[6] = (byte) ((temp.length >> 8) & 0xff);
		temp[7] = (byte) (temp.length & 0xff);

		temp[8] = 0x04;
		temp[9] = 0x00;
		// year
		temp[10] = (byte) ((c.get(Calendar.YEAR) >> 8) & 0xff);
		temp[11] = (byte) (c.get(Calendar.YEAR) & 0xff);
		// month
		temp[12] = (byte) (c.get(Calendar.MONTH) + 1);
		// day
		temp[13] = (byte) c.get(Calendar.DAY_OF_MONTH);
		// hour
		temp[14] = (byte) c.get(Calendar.HOUR_OF_DAY);
		// miniute
		temp[15] = (byte) c.get(Calendar.MINUTE);
		// second
		temp[16] = 0;
		temp[17] = (byte) c.get(Calendar.SECOND);

		temp[18] = (byte) (0x9e);
		temp[19] = (byte) (0x22);
		// 步数
		temp[20] = (byte) ((step >> 24) & 0xff);
		temp[21] = (byte) ((step >> 16) & 0xff);
		temp[22] = (byte) ((step >> 8) & 0xff);
		temp[23] = (byte) ((step) & 0xff);

		// 百卡
		temp[24] = (byte) ((cal >> 24) & 0xff);
		temp[25] = (byte) ((cal >> 16) & 0xff);
		temp[26] = (byte) ((cal >> 8) & 0xff);
		temp[27] = (byte) ((cal) & 0xff);
		// temp[18] = (byte) weight;
		// temp[19] = (byte) distance;
		// CRC
		// crc.AddCrc16(temp, temp.length - 2);
		MLinkCRC.crc16(temp, temp.length - 2);
		return temp;

	}

	public byte[] ComposePacket080501() {
		// A7 B8 00 01 00 00 00 XX 08 01 07 DC 01 10 0E 00 00 00 CRC1 CRC2
		byte[] temp = new byte[] { (byte) 0xa7, (byte) 0xb8, 0x00, 0x01, 0, 0,
				0, 0, 0x08, 0x05, 0x01, 0x00, 0x00 };

		temp[4] = (byte) ((temp.length >> 24) & 0xff);
		temp[5] = (byte) ((temp.length >> 16) & 0xff);
		temp[6] = (byte) ((temp.length >> 8) & 0xff);
		temp[7] = (byte) (temp.length & 0xff);
		// crc.AddCrc16(temp, temp.length - 2);
		MLinkCRC.crc16(temp, temp.length - 2);
		return temp;
	}

	public byte[] ComposePacket080502() {
		// A7 B8 00 01 00 00 00 XX 08 01 07 DC 01 10 0E 00 00 00 CRC1 CRC2
		byte[] temp = new byte[] { (byte) 0xa7, (byte) 0xb8, 0x00, 0x01, 0, 0,
				0, 0, 0x08, 0x05, 0x02, 0x00, 0x00 };

		temp[4] = (byte) ((temp.length >> 24) & 0xff);
		temp[5] = (byte) ((temp.length >> 16) & 0xff);
		temp[6] = (byte) ((temp.length >> 8) & 0xff);
		temp[7] = (byte) (temp.length & 0xff);
		// crc.AddCrc16(temp, temp.length - 2);
		MLinkCRC.crc16(temp, temp.length - 2);
		return temp;
	}

	public byte[] ComposePacket0806() {
		// A7 B8 00 01 00 00 00 XX 08 01 07 DC 01 10 0E 00 00 00 CRC1 CRC2
		byte[] temp = new byte[] { (byte) 0xa7, (byte) 0xb8, 0x00, 0x01, 0, 0,
				0, 0, 0x08, 0x06, 0x00, 0x00 };
		temp[4] = (byte) ((temp.length >> 24) & 0xff);
		temp[5] = (byte) ((temp.length >> 16) & 0xff);
		temp[6] = (byte) ((temp.length >> 8) & 0xff);
		temp[7] = (byte) (temp.length & 0xff);
		// crc.AddCrc16(temp, temp.length - 2);
		MLinkCRC.crc16(temp, temp.length - 2);
		return temp;
	}

	public byte[] getGBFont(long text12, long text22, long text32) {
		String fileName = "lovefit.mp3";
		String path = ContextRef.getFilesDir().toString() + "/" + fileName; // 文件名字
		byte[] buffer = null;
		try {
			File file = new File(path);
			if (!file.exists()) {
				AssetManager manager = ContextRef.getResources().getAssets();
				InputStream in = manager.open(fileName,
						AssetManager.ACCESS_RANDOM);
				int length = in.available();
				byte[] buffer1 = new byte[length];
				in.read(buffer1);
				in.close();
				try {

					FileOutputStream fout = new FileOutputStream(path);
					fout.write(buffer1);
					fout.close();
				}

				catch (Exception e) {
					e.printStackTrace();
				}
				MyLog.e("Milink", "the file is not exists");

			}
			RandomAccessFile file2 = new RandomAccessFile(path, "r");

			if (text22 == 0) {
				buffer = new byte[32];
				file2.seek(text12);
				file2.read(buffer, 0, 32);
			} else if (text32 == 0) {
				buffer = new byte[64];
				file2.seek(text12);
				file2.read(buffer, 0, 32);
				file2.seek(text22);
				file2.read(buffer, 32, 32);
			} else {
				buffer = new byte[96];
				file2.seek(text12);
				file2.read(buffer, 0, 32);
				file2.seek(text22);
				file2.read(buffer, 32, 32);
				file2.seek(text32);
				file2.read(buffer, 64, 32);
			}
			file2.close();
			MyLog.e("Milink", "the file is exists");
			StringBuilder str_gb = new StringBuilder();
			for (int i = 0; i < buffer.length; i++) {
				str_gb.append(String.format("0x%1$02X,", buffer[i]));
			}
			MyLog.e("MILINK", "fon:" + str_gb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return buffer;
	}

	public final static byte[] BA_BLE_ALARM_1 = { (byte) 0xA7, (byte) 0xB8,
			(byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x10, (byte) 0x90, (byte) 0x00, (byte) 0x90, (byte) 0x04,
			(byte) 0x01, (byte) 0x00, (byte) 0xFC, (byte) 0xFD,

	};

	// param[0] is on/off;param[1] is repeat num; param[2] is array_num,
	// param[3..] is liric array
	byte[] composeConfigPacket_vibrate(int[] param) {
		byte[] ba1;

		byte[] ba4 = new byte[] { (byte) 0x90, 0x08,
				23,
				// on/off, repeat_num, array_num
				0x00, 0x01, 0x02, 0x05, 0x06, 0x05, 0x06, 0x05, 0x06, 0x05,
				0x06, 0x05, 0x06, 0x05, 0x06, 0x05, 0x06, 0x05, 0x06, 0x05,
				0x06, 0x05, 0x06, };

		ba4[3] = (byte) param[0];
		ba4[4] = (byte) param[1];
		ba4[5] = (byte) param[2];
		for (int i = 0; i < param[2] * 2; i++) {
			ba4[6 + i] = (byte) param[3 + i];
		}

		int len = 12 + ba4.length;
		ba1 = new byte[len];
		System.arraycopy(BA_BLE_ALARM_1, 0, ba1, 0, 12);

		ba1[4] = (byte) (len / 16777216 % 256);
		ba1[5] = (byte) (len / 65536 % 256);
		ba1[6] = (byte) (len / 256 % 256);
		ba1[7] = (byte) (len % 256);

		ba1[8] = (byte) 0x90;
		ba1[9] = (byte) 0x00;

		System.arraycopy(ba4, 0, ba1, 10, ba4.length);

		ba1[len - 2] = (byte) 0xfc;
		ba1[len - 1] = (byte) 0xfd;

		MLinkCRC.crc16(ba1, ba1.length - 2);

		return ba1;
	}

	byte[] composeConfigPacket_yaoyao_state(int onoff) {
		byte[] ba1;

		byte[] ba4 = new byte[] { (byte) 0x90, 0x09, 0x01, 0x00, };

		ba4[3] = (byte) onoff;

		int len = 12 + ba4.length;
		ba1 = new byte[len];
		System.arraycopy(BA_BLE_ALARM_1, 0, ba1, 0, 12);

		ba1[4] = (byte) (len / 16777216 % 256);
		ba1[5] = (byte) (len / 65536 % 256);
		ba1[6] = (byte) (len / 256 % 256);
		ba1[7] = (byte) (len % 256);

		ba1[8] = (byte) 0x90;
		ba1[9] = (byte) 0x00;

		System.arraycopy(ba4, 0, ba1, 10, ba4.length);

		ba1[len - 2] = (byte) 0xfc;
		ba1[len - 1] = (byte) 0xfd;

		MLinkCRC.crc16(ba1, ba1.length - 2);
		return ba1;
	}

	public byte[] composeNotification(String title, String msg, boolean callIn)
			throws Exception {
		try {
			msg = msg.replace("\n", "");
			msg = msg.replace("\r", "");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int callInlen = callIn ? 2 : 0;
		byte[] temp1 = title.getBytes("unicode");
		byte[] temp2 = msg.getBytes("unicode");

		if (temp1 == null || temp1.length < 2) {
			System.out.println(temp1);
		}
		// unicode编码fffe高位在后
		if (temp1[0] == (byte) -1 && temp1[1] == (byte) -2) {
			for (int i = 0; i < temp1.length; i += 2) {
				byte x = temp1[i];
				temp1[i] = temp1[i + 1];
				temp1[i + 1] = x;
			}
		}
		if (temp2[0] == (byte) -1 && temp2[1] == (byte) -2) {
			for (int i = 0; i < temp2.length; i += 2) {
				byte x = temp2[i];
				temp2[i] = temp2[i + 1];
				temp2[i + 1] = x;
			}
		}

		// 去掉标志
		byte[] tit = new byte[temp1.length - 2];
		byte[] msgs = new byte[temp2.length - 2];

		System.arraycopy(temp1, 2, tit, 0, tit.length);
		System.arraycopy(temp2, 2, msgs, 0, msgs.length);

		int lenTitle = tit.length > 32 ? 32 : tit.length;
		int lenMsg = msgs.length > 120 ? 120 : msgs.length;
		byte[] temp = new byte[18 + lenTitle + lenMsg + callInlen];
		int i = 0;
		temp[i++] = (byte) 0xA7;
		temp[i++] = (byte) 0xB8;
		temp[i++] = (byte) 0x00;
		temp[i++] = (byte) 0x01;
		temp[i++] = (byte) ((temp.length >> 24) & 0xff);
		temp[i++] = (byte) ((temp.length >> 16) & 0xff);
		temp[i++] = (byte) ((temp.length >> 8) & 0xff);
		temp[i++] = (byte) (temp.length & 0xff);
		temp[i++] = (byte) 0x91;
		temp[i++] = (byte) 0x00;
		// TITLE
		temp[i++] = (byte) 0x90;
		temp[i++] = (byte) 0x10;
		// LEN-TITLE

		// temp[i++] = (byte) ((tit.length >> 8) & 0xff);
		temp[i++] = (byte) (lenTitle);
		System.arraycopy(tit, 0, temp, i, lenTitle);
		i += lenTitle;

		// MSG
		temp[i++] = (byte) 0x90;
		temp[i++] = (byte) 0x11;
		// LEN-MSG

		// temp[i++] = (byte) ((msgs.length >> 8) & 0xff);
		temp[i++] = (byte) (lenMsg);
		System.arraycopy(msgs, 0, temp, i, lenMsg);
		i += lenMsg;
		if (callIn) {
			temp[i++] = (byte) 0x90;
			temp[i++] = (byte) 0x01;
			i += callInlen;
		}
		MLinkCRC.crc16(temp, temp.length - 2);
		return temp;// [-89, -72, 0, 1, 0, 0, 0, 52, -112, 16, 0, 26, -1, -2,
					// 17, 98, -124, 118, 53, 117, 17, -127, 32, 0, 40, 0, 50,
					// 0, 97, 103, -80, 101, -120, 109, 111, 96, 41, 0, -112,
					// 17, 0, 8, -1, -2, 49, 114, 49, 114, 49, 114, -50, -24]
	}

	public static String string2Unicode(String string) {

		StringBuffer unicode = new StringBuffer();

		for (int i = 0; i < string.length(); i++) {

			// 取出每一个字符
			char c = string.charAt(i);

			// 转换为unicode
			unicode.append("\\u" + Integer.toHexString(c));
		}

		return unicode.toString();
	}

	byte[] composeConfigPacket_shutdown() {
		byte[] ba4 = new byte[] { (byte) 0xA7, (byte) 0xB8, 00, 01, 00, 00, 00,
				(byte) 0x0C, (byte) 0x93, 00, (byte) 0xFC, (byte) 0xFD, };
		MLinkCRC.crc16(ba4, ba4.length - 2);
		return ba4;
	}

}

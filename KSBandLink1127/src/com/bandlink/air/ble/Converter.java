package com.bandlink.air.ble;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Locale;

import android.content.Context;

/**
 * This class can be used to convert among short, int, long, byte[] and Hex
 * String;
 * 
 * @author Shengpeng
 * @contact shengpeng7@gmail.com
 * @version 1.0
 * @date 2011-10-29
 */
public final class Converter {
	public static final int SHORT = 2;
	public static final int INT = 4;
	public static final int LONG = 8;

	/**
	 * This function convert short to byte[]
	 * 
	 * @param n
	 *            the short value to be converted
	 * @return the length of byte[] is 2
	 */
	public static byte[] shortToByteArray(short n) {
		int length = SHORT;

		byte[] result = new byte[length];
		for (int i = 0; i < length; i++) {
			int offset = (length - i - 1) * 8;
			result[i] = (byte) ((n >> offset) & 0xFF);
		}

		return result;
	}

	public static byte[] hexStringToBytes(String hexString) {
		if ((hexString == null) || (hexString.equals(""))) {
			return null;
		}
		hexString = hexString.replace(" ", "").toUpperCase(Locale.ENGLISH);
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = ((byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[(pos + 1)])));
		}
		return d;
	}

	public static int getIntFromHexString(String hexString, ByteOrder byteOrder) {
		return getIntFromByteArray(hexStringToBytes(hexString), byteOrder);
	}

	public static int getIntFromByteArray(byte[] array, ByteOrder byteOrder) {
		return ByteBuffer.wrap(array).order(byteOrder).getInt();
	}

	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	/**
	 * This function convert int to byte[]
	 * 
	 * @param n
	 *            the int value to be convert
	 * @return the length of byte[] is 4
	 */
	public static byte[] intToByteArray(int n) {
		int length = INT;

		byte[] result = new byte[length];
		for (int i = 0; i < length; i++) {
			int offset = (length - i - 1) * 8;
			result[i] = (byte) ((n >> offset) & 0xFF);
		}

		return result;
	}

	/**
	 * This function convert long to byte[]
	 * 
	 * @param n
	 *            the long value to be convert
	 * @return the length of byte[] is 8
	 */
	public static byte[] longToByteArray(long n) {
		int length = LONG;

		byte[] result = new byte[length];
		for (int i = 0; i < length; i++) {
			int offset = (length - i - 1) * 8;
			result[i] = (byte) ((n >> offset) & 0xFF);
		}
		return result;
	}

	/**
	 * This function convert short to byte[]
	 * 
	 * @param n
	 *            the short value to be convert
	 * @param buf
	 *            the byte array to store the converted bytes, length = 2;
	 * @param index
	 *            the start point of offset of the buf[]
	 */
	public static void shortToByteArray(short n, byte[] buf, int index) {
		int length = SHORT;

		for (int i = 0; i < length; i++) {
			int offset = (length - i - 1) * 8;
			buf[index + i] = (byte) ((n >> offset) & 0xFF);
		}
	}

	/**
	 * This function convert int to byte[]
	 * 
	 * @param n
	 *            the int value to be convert
	 * @param buf
	 *            the byte array to store the converted bytes, length = 4;
	 * @param index
	 *            the start point of offset of the buf[]
	 */
	public static void intToByteArray(int n, byte[] buf, int index) {
		int length = INT;

		for (int i = 0; i < length; i++) {
			int offset = (length - i - 1) * 8;
			buf[index + i] = (byte) ((n >> offset) & 0xFF);
		}
	}

	/**
	 * This function convert Long to byte[];
	 * 
	 * @param n
	 *            the Long value to be convert
	 * @param buf
	 *            the byte array to store the converted bytes, length = 8;
	 * @param index
	 *            the start point of offset of the buf[]
	 */
	public static void longToByteArray(long n, byte[] buf, int index) {
		int length = LONG;

		for (int i = 0; i < length; i++) {
			int offset = (length - i - 1) * 8;
			buf[index + i] = (byte) ((n >> offset) & 0xFF);
		}
	}

	/**
	 * This function convert byte[] to short, whose length is 2;
	 * 
	 * @param buf
	 * @param index
	 * @return
	 */
	public static short byteArrayToShort(byte[] buf, int index) {
		int length = SHORT;
		short Value = 0;

		for (int i = 0; i < length; i++) {
			int offset = (length - i - 1) * 8; // 8
			Value |= (buf[index + i] & 0xFF) << offset;
		}

		return Value;
	}

	/**
	 * This function convert byte[] to int, whose length is 4;
	 * 
	 * @param buf
	 * @param index
	 * @return
	 */
	public static int byteArrayToInt(byte[] buf, int index) {
		int length = INT;
		int Value = 0;

		for (int i = 0; i < length; i++) {
			int offset = (length - i - 1) * 8;
			Value |= (buf[index + i] & 0xFF) << offset;
		}

		return Value;
	}

	/**
	 * This function convert byte[] to Long, whose length is 8;
	 * 
	 * @param buf
	 * @param index
	 * @return
	 */
	public static long byteArrayToLong(byte[] buf, int index) {
		int length = LONG;
		long Value = 0;

		for (int i = 0; i < length; i++) {
			int offset = (length - i - 1) * 8;
			Value |= (buf[index + i] & 0xFF) << offset;
		}

		return Value;
	}

	/**
	 * This function convert a byte to a Hex String
	 * 
	 * @param b
	 *            the byte to be converted
	 * @return Hex String
	 */
	public static String byteToHexString(byte b) {
		String hex = new String();

		hex = hex + Integer.toHexString(b & 0xFF);
		if (hex.length() == 1) {
			hex = '0' + hex;
		}

		return hex.toUpperCase();
	}

	/**
	 * This function convert a part of byte array to a Hex String
	 * 
	 * @param buf
	 *            the byte array to be convert
	 * @param index
	 *            from which position to convert
	 * @param length
	 *            how many bytes need be converted
	 * @return Hex String
	 */
	public static String byteArrayToHexString(byte[] buf, int index, int length) {
		String hex = new String();

		for (int i = index; i < index + length; i++) {
			hex = hex + byteToHexString(buf[i]);
		}
		if (hex.length() == 1) {
			hex = '0' + hex;
		}

		return hex;
	}

	/**
	 * This function convert a whole byte array to Hex String
	 * 
	 * @param buf
	 *            the byte array to be convert
	 * @return Hex String
	 */
	public static String byteArrayToHexString(byte[] buf) {
		String hex = new String();

		hex = byteArrayToHexString(buf, 0, buf.length);

		return hex;
	}

	// float转byte[]
	public static byte[] floatToByte(float v) {
		ByteBuffer bb = ByteBuffer.allocate(4);
		byte[] ret = new byte[4];
		FloatBuffer fb = bb.asFloatBuffer();
		fb.put(v);
		bb.get(ret);
		return ret;
	}

	// byte[]转float
	public static float byteToFloat(byte[] v) {
		ByteBuffer bb = ByteBuffer.wrap(v);
		FloatBuffer fb = bb.asFloatBuffer();
		return fb.get();
	}

	public static String distanceToString(Context context, double distance) {
		String m = ""; // context.getResources().getString(R.string.show_unit_m);
		String km = ""; // context.getResources().getString(R.string.show_unit_k)
						// +context.getResources().getString(R.string.show_unit_m);

		String ss;

		if (distance < 1000) {
			ss = String.format("%1$.1f%2$s", distance, m);
		} else {
			ss = String.format("%1$.2f%2$s", distance / 1000.0, km);
		}

		return ss;
	}

	public static String durationToString(Context context, long duration) {
		String unit_hour = "'"; // context.getResources().getString(R.string.show_unit_hour);
		String unit_minute = "'"; // context.getResources().getString(R.string.show_unit_minute);
		String unit_second = "''"; // context.getResources().getString(R.string.show_unit_second);

		String ss;

		if (duration > 3600)
			ss = String.format("%1$d%2$s%3$d%4$s%5$d%6$s", duration / 3600,
					unit_hour, (duration % 3600) / 60, unit_minute,
					duration % 60, unit_second);
		else {
			if (duration > 60)
				ss = String.format("%1$d%2$s%3$d%4$s", (duration % 3600) / 60,
						unit_minute, duration % 60, unit_second);
			else
				ss = String.format("%1$d%2$s", duration % 60, unit_second);
		}

		return ss;
	}

}

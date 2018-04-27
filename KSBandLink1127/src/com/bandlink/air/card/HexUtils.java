package com.bandlink.air.card;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;

public class HexUtils {

	/*
	 * Convert byte[] to hex string.
	 * 
	 * @param src byte[] data
	 * 
	 * @return hex string
	 */
	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString().toUpperCase();
	}

	/**
	 * Convert hex string to byte[]
	 * 
	 * @param hexString
	 *            the hex string
	 * @return byte[]
	 */
	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.replace(" ", "").toUpperCase(Locale.ENGLISH);
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}
	
	public static int getIntFromHexString(String hexString, ByteOrder byteOrder) {
		return getIntFromByteArray(hexStringToBytes(hexString), byteOrder);
	}
	
	public static int getIntFromByteArray(byte[] array, java.nio.ByteOrder byteOrder)
	{
		return java.nio.ByteBuffer.wrap(array).order(byteOrder).getInt();
	}

	/**
	 * Convert char to byte
	 * 
	 * @param c
	 *            char
	 * @return byte
	 */
	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}
	
	private HexUtils() {
	}

}

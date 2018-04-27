package com.milink.android.airscale;

import com.bandlink.air.MyLog;

public class Tools {
    private static float fatMin;
    private static float weightMin;
    
    public static void main(String[] args) {
    	buffer2ScaleData(new byte[]{0x11,(byte)0x11,(byte)0x82,0x11,0x11,0x11,0x11,0x11,0x11});
	}
    public static ScaleData buffer2ScaleData(byte[] buffer) {
        if(buffer.length < 0x8) {
            return null;
        }
        String bnyState = Integer.toBinaryString(byte2Int(buffer[0x2]));
        int len = bnyState.length();
        for(int i = 0x0; i < byte2Int(buffer[0x2]); i = i + 0x1) {
            bnyState = "0" + bnyState;
        }
        int type = Integer.valueOf(bnyState.substring(0x0, 0x1), 0x2).intValue();
        int state = Integer.valueOf(bnyState.substring(0x1, 0x8), 0x2).intValue();
        float weight = ((float)Integer.valueOf(byte2HexString(buffer[0x3]), 0x10).intValue() + (float)Integer.valueOf(byte2HexString(buffer[0x4]) + "00", 0x10).intValue()) / 10.0f;
        int resistance = Integer.valueOf(byte2HexString(buffer[0x5]), 0x10).intValue() + Integer.valueOf(byte2HexString(buffer[0x6]) + "00", 0x10).intValue();
		return new ScaleData(type, state, weight, resistance);
    }
    
    public static boolean checkBufferSum(byte[] buffer) {
        if(buffer.length != 0x8) {
            return false;
        }
        int sum = 0x0;
        for(int i = 0x0; i < 0x7; i = i + 0x1) {
            sum += byte2Int(buffer[i]);
        }
        String hexSum = Integer.toHexString(sum) + "";
        String hexSumD = hexSum.substring((hexSum.length() - 0x2));
        int sumD = Integer.valueOf(hexSumD, 0x10).intValue();
        if(sumD == byte2Int(buffer[0x7])) {
            return true;
        }
        
        return false;
    }
    
    public static int byte2Int(byte b) {
        return (b & 0xff);
    }
    
    public static String byte2HexString(byte b) {
        String hv = Integer.toHexString(byte2Int(b));
        if(hv.length() < 0x2) {
            hv = "0" + hv;
        }
        return hv;
    }
    
    public static String bytes2HexString(byte[] bytes) {
    	StringBuilder stringBuilder = new StringBuilder();
        if((bytes == null) || (bytes.length <= 0)) {
            return null;
        }
        for(int i = 0x0; i < bytes.length; i = i + 0x1) {
            stringBuilder.append(byte2HexString(bytes[i]));
        }
        return stringBuilder.toString();
    }
    
}

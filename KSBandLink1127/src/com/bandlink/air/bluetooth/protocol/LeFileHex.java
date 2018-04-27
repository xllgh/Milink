package com.bandlink.air.bluetooth.protocol;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.util.Log;

import com.bandlink.air.MyLog;
import com.bandlink.air.ble.LogUtil;
import com.bandlink.air.util.Crc16;

public class LeFileHex {
	public static byte[][] buffer1 = new byte[5000][];
	public static byte[][] buffer;
	String[] aj=new String[5000]; 
	public static byte[] byteslen = new byte[4];
	public static byte[] bytescrc = new byte[2];
	public static int allLine;
	public static int crc = 0;
	public static int crc1 = 0;
	public static int datalen = 0;
	
	public int filestatus;
	
	private Context context;
	
	public LeFileHex(Context c,String filename,boolean is303)
	{
		context = c;
		filestatus = LoadFile(filename,is303);
	}
	
	private int LoadFile(String filename,boolean is303)
	{
		String sd = context.getFilesDir().toString() + "/air/";
		String pathString = sd + filename;
		String str;
    	int length=0;
    	//pathString = LogUtil.SDcardUtil.getPath() + File.separator + "jj0.hex";
    	File file = new File(pathString);
    	
    	if(!file.exists())
    	{
    		//error file not exist
    		return 2;    		
    	}
    	
    	try {    		
    		allLine = 0;
    		datalen = 0;
			File in =new File(pathString);
			//InputStream input = context.getAssets().open("Air303.hex");
			FileInputStream  fileInputStream=new FileInputStream (in); 
			BufferedReader dr = new BufferedReader(new InputStreamReader(fileInputStream));
			
			//String as=dr.readLine();
			//as.toCharArray();
			//char[] jk=new char[100];
			//dr.read(jk);
			while ((str = dr.readLine())!= null) {				
				//MyLog.e("FW",str.length()+"");
				//aj[length] = str;
				buffer1[length]=ParserHex(str);
				//buffer1[length]=str.getBytes();
				length++;
				allLine++;
			}
			dr.close();

			//for(int i=0;i<length;i++)
			//{
				//buffer1[i]=ParserHex(aj[i]);
			//}
			MyLog.e("FW", "the length:"+length);
			
			buffer =new byte[allLine][];
			
			for(int i=0;i<allLine;i++)
			{
				buffer[i]=buffer1[i];
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			MyLog.e("FW", e.getMessage());
		}
    	
		crc1=0xffff;
		crc=0xffff;
		//E809

		for(int i=0;i<buffer.length;i++)
		{
			if (buffer[i][3] == 0) {
				datalen += buffer[i][0];
				crc1 = Crc16.ComputeCrc(crc1, buffer[i], 4, buffer[i][0]);
				crc = Crc16.crc16_compute(crc, buffer[i], 4, buffer[i][0]);
			}
		}
		byteslen[3] = (byte) ((datalen >>> 24) & 0xff);
		byteslen[2] = (byte) ((datalen >>> 16) & 0xff);
		byteslen[1] = (byte) ((datalen >>> 8) & 0xff);
		byteslen[0] = (byte) (datalen & 0xff);
		

		bytescrc[1] = (byte) ((crc >>> 8) & 0xff);
		bytescrc[0] = (byte) (crc & 0xff);
		
		MyLog.e("FW", "data length:"+datalen);
		MyLog.e("FW", "CRC16:"+ Integer.toHexString(crc));
		MyLog.e("FW", "CRC16:"+ Integer.toHexString(crc1));
		return 1;
	}
	
	private byte[] ParserHex(String str)
    {
    	int len=(str.length()-1) /2;
    	byte[] data = new byte[len];
    	//char[] aa=new char[2];
    	String ss1=str.substring(1,str.length());
    	char[] cc=ss1.toCharArray();
    	data = decodeHex(cc);
    	//data=HexString2Bytes(ss1);
    	/*
    	for(int i=0;i<len;i++)
    	{
    		//aa[0] = str.charAt(1+i*2);
    		//aa[1] = str.charAt(2+i*2);
    		//String jj = String.valueOf(aa);
    		//data[i]=(byte)Integer.parseInt(jj,16);
    		data[i]=(byte)Integer.parseInt(str.substring(1+i*2, 3+i*2), 16);
    		//data[i]=str.substring(1+i*2, 3+i*2).getBytes()[0];
    		//data[i]=(byte)Integer.parseInt("08", 16);
    		//data[i]=(byte)(0xaa);
    	}
    	*/
    	return data;
    }
	
	/**
	* 将两个ASCII字符合成一个字节；
	* 如："EF"--> 0xEF
	* @param src0 byte
	* @param src1 byte
	* @return byte
	*/
	public static byte uniteBytes(byte src0, byte src1) {
	byte _b0 = Byte.decode("0x" + new String(new byte[]{src0})).byteValue();
	_b0 = (byte)(_b0 << 4);
	byte _b1 = Byte.decode("0x" + new String(new byte[]{src1})).byteValue();
	byte ret = (byte)(_b0 ^ _b1);
	return ret;
	}

	/**
	* 将指定字符串src，以每两个字符分割转换为16进制形式
	* 如："2B44EFD9" --> byte[]{0x2B, 0x44, 0xEF, 0xD9}
	* @param src String
	* @return byte[]
	*/
	public static byte[] HexString2Bytes(String src){
		int len = src.length()/2;
	byte[] ret = new byte[len];
	byte[] tmp = src.getBytes();
	for(int i=0; i<len; i++){
	ret[i] = uniteBytes(tmp[i*2], tmp[i*2+1]);
	}
	return ret;
	} 	
	
	public static byte[] decodeHex(char[] data) {

	    int len = data.length;

	    byte[] out = new byte[len >> 1];

	    // two characters form the hex value.
	    for (int i = 0, j = 0; j < len; i++) {
	        int f = Character.digit(data[j], 16) << 4;
	        j++;
	        f = f | Character.digit(data[j], 16);
	        j++;
	        out[i] = (byte) (f & 0xFF);
	    }

	    return out;
	}	
}

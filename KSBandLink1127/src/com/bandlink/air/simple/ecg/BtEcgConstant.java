package com.bandlink.air.simple.ecg;

public class BtEcgConstant { /*盽计よ*/
	public static final int MESSAGE_SEND_TOAST = 20000;
	public static final int MESSAGE_SEND_TEXT = 20023;
	
	public final static int MESSAGE_SEND_IHR = 20001;
	public final static int MESSAGE_SEND_HR = 20002;
	public static final int MESSAGE_SEND_LF = 20003;
	public static final int MESSAGE_SEND_VL = 20004;
	public static final int MESSAGE_SEND_VTE = 20005;
	public static final int MESSAGE_SEND_VER = 20006;
	public static final int MESSAGE_SEND_TP = 20007;
	public static final int MESSAGE_SEND_TE = 20008;
	public static final int MESSAGE_SEND_SD = 20009;
	public static final int MESSAGE_SEND_RR = 20010;
	public static final int MESSAGE_SEND_HF = 20011;
	public static final int MESSAGE_SEND_HHR = 20022;
	
	//ECG Command
	public static final String BT_ECG_CMD_INITIALIZE = "INIT\r";
	public static final String BT_ECG_CMD_RESET_AND_ENTER_POWER_DOWN_MODE = "RS\r";
	public static final String BT_ECG_CMD_TOGGLE_WAVE_OUTPUT_ON = "W+\r";
	public static final String BT_ECG_CMD_TOGGLE_WAVE_OUTPUT_OFF = "W-\r";
	public static final String BT_ECG_CMD_TOGGLE_SIMULATION_ON = "S+\r";
	public static final String BT_ECG_CMD_TOGGLE_SIMULATION_OFF = "S-\r";
	public static final String BT_ECG_CMD_TOGGLE_DIGITAL_INPUT = "D+\r";
	public static final String BT_ECG_CMD_TOGGLE_ANALOG_INPUT = "D-\r";
	public static final String BT_ECG_CMD_CHANGE_GAIN_ADD = "G+\r";
	public static final String BT_ECG_CMD_CHANGE_GAIN_DEC = "G-\r";
	
	public static final double BT_ECG_WAVE_HEIGHT_SCALE_BASE_VALUE = 256.0;
	
	public static final int BT_ECG_DATA_BUFFER_SIZE = 480;
	// ECG data encoding
	public static final String BT_ECG_ASCII_TYPE = "ISO-8859-1";
	
	//ECG Transfer
	public static final String ECGDataTransfer_URL = "http://140.127.194.189:8080/ECGStore/StoreData";
//	public static final String ECGDataTransfer_URL = "http://140.127.194.104:8080/ECGStore/StoreData";
	//DATABASE
	public static enum ECGDatebase{
		SUCCESS("success"),
		NAME("name"),
		PASSWD("passwd"),
		TIME("time"),
		RAW("raw"),
		GPS("gps"),
		ACTION("action"),
		SYMPTOM("symptom"),
		SHOW("show"),
		VOICE("voice"),
		BTECGINFO("btecginfo");
		
		private String str;
		ECGDatebase(String str){
			this.str = str;
		}
		public String getText(){
			return this.str;
		}
		@Override
		public String toString() {
			return this.str;
		}
	}
}
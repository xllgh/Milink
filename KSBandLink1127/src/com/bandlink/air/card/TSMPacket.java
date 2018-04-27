package com.bandlink.air.card;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONException;
import org.json.JSONObject;

public class TSMPacket extends ISO8583Packet {
	public final static int kField_AccountID = 2;
	public final static int kField_AnsCode = 3;
	public final static int kField_UserName = 4;
	public final static int kField_TradeAmount = 5;
	public final static int kField_CardBalacnce = 6;
	public final static int kField_CardStatus = 7;
	public final static int kField_UserPhoneNumber = 8;
	public final static int kField_Date = 9;
	public final static int kField_Time = 10;
	public final static int kField_TransactionType = 12;
	public final static int kField_UserIDCardNumber = 14;
	public final static int kField_PhoneSEInfo = 15;
	public final static int kField_AppletCount = 19;
	public final static int kField_AppletInfo = 20;
	public final static int kField_ApplicationID = 21;
	public final static int kField_ApplicationDetails = 22;
	public final static int kField_PhoneInfo = 23;
	public final static int kField_SSDInfo = 24;
	public final static int kField_SessionID = 25;
	public final static int kField_Challenge = 26;
	public final static int kField_StepNo = 27;
	public final static int kField_ApduTransmission = 28;
	public final static int kField_ApplicationProvider = 29;
	public final static int kField_OTAOperationStatus = 30;
	public final static int kField_MultiApdu = 31;
	public final static int kField_EncryptionKey = 55;
	public final static int kField_JSON_1 = 56;
	public final static int kField_JSON_2 = 57;
	public final static int kField_JSON_3 = 58;
	public final static int kField_TransactionRecords = 59;
	public final static int kField_PaymentPassword = 62;
	public final static int kField_MAC = 64;
	
	public String hexOpCode, hexRedirectionCode, hexIMEI;
	private String[] _multiApdu;
	private JSONObject _jsonObject;
	
	private static final Logger logger = Logger.getLogger("TSMPacket");
	private static String key = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";
	
	public TSMPacket(byte[] packet) {
		//TODO: 此处应为13， 考虑一下如何解析报文头和报文头后，位元表前的交易类型
		super(packet, 15);
	}
	
	public static void setKey(String key) {
		TSMPacket.key = key;
	}
	
	public static TSMPacket getTSMPacket(byte[] packet, boolean encrypted) {
		byte[] plainPacket;
		if (encrypted) {
			plainPacket = decryptPacket(packet);
		} else {
			plainPacket = packet;
		}
		return new TSMPacket(plainPacket);
	}
	
	protected static byte[] decryptPacket(byte[] encryptedPacket) {
		byte[] result = null;
		try {
			Cipher des3Cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
			java.security.Key desKey = new SecretKeySpec(HexUtils.hexStringToBytes(key),"DES");
			IvParameterSpec iv = new IvParameterSpec(HexUtils.hexStringToBytes("0000000000000000"));
			des3Cipher.init(Cipher.DECRYPT_MODE, desKey, iv);
			String hexPacket = HexUtils.bytesToHexString(encryptedPacket);
			String header = hexPacket.substring(4, 26);
			String body = hexPacket.substring(26);
			String decryptedBody = HexUtils.bytesToHexString(des3Cipher.doFinal(HexUtils.hexStringToBytes(body)));
			String decryptedHexPacket = String.format("%04d%s%s", (decryptedBody.length() + header.length()) / 2, header, decryptedBody);
			result = HexUtils.hexStringToBytes(decryptedHexPacket);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}	
	
	public TSMPacket() {
		super();
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
		String datetime = dateFormatter.format(new Date());
		setField(kField_Date, datetime.substring(0, 8));
		setField(kField_Time, datetime.substring(8, 14));
		this._multiApdu = null;
	}
	
	@Override
	protected void postParse() {
		if (this.isFieldExist(kField_MultiApdu)) {
			ArrayList<String> apduList = new ArrayList<String>();
			int position = 0;
			int length = 0;
			String fieldData = getFieldWithHex(kField_MultiApdu);
			while (position < fieldData.length()) {
				length = Integer.parseInt(fieldData.substring(position, position+4));
				position += 4;
				apduList.add(fieldData.substring(position, position + length));
				position += length;
			}
			_multiApdu = apduList.toArray(new String[apduList.size()]);
		}
		
		if (isFieldExist(kField_JSON_1)) {
			String jsonString = getFieldWithUTF8Encoding(kField_JSON_1);
			if (isFieldExist(kField_JSON_2)) {
				jsonString += getFieldWithUTF8Encoding(kField_JSON_2);
				if (isFieldExist(kField_JSON_3)) {					
					jsonString += getFieldWithUTF8Encoding(kField_JSON_3);
				}
			}
			try {
				_jsonObject = new JSONObject(jsonString);
			} catch (JSONException e) {
				logger.warning("json 解析失败: " + e.getLocalizedMessage());
			}
		}
	}
	
	@Override
	public byte[] getDataPacket(){
		return HexUtils.hexStringToBytes(getHexDataPacket());
	}
	
	public byte[] getEncryptedPacket() {
		try {
			Cipher des3Cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
			java.security.Key desKey = new SecretKeySpec(HexUtils.hexStringToBytes(key),"DES");
			IvParameterSpec iv = new IvParameterSpec(HexUtils.hexStringToBytes("0000000000000000"));
			des3Cipher.init(Cipher.ENCRYPT_MODE, desKey, iv);
			String hexPacket = getHexDataPacket();
			String header = hexPacket.substring(4, 26);
			String body = hexPacket.substring(26);
			String encryptedBody = HexUtils.bytesToHexString(des3Cipher.doFinal(HexUtils.hexStringToBytes(body)));
			String encryptedHexPacket = String.format("%04d%s%s", (encryptedBody.length() + header.length()) / 2, header, encryptedBody);
			return HexUtils.hexStringToBytes(encryptedHexPacket);
			
		} catch (Exception e) {
		return null;
		}
	}
	
	public boolean isResponseNoError() {
		return getBCDFieldWithInteger(kField_AnsCode) == 0;
	}
	
	public String[] getMultiApdu() {
		return _multiApdu;
	}
	
	public JSONObject getJsonObject() {
		return _jsonObject;
	}
	
	public void setJsonObject(JSONObject obj) {
		_jsonObject = obj;
		String jsonString = obj.toString();
		if (jsonString.length() < 999) {
			setFieldWithUTF8Encoding(kField_JSON_1, jsonString);
		} else {
			assert(false);
		}
	}
	
	public void setMultiApdu(String[] value) {
		_multiApdu = value;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < value.length; i++) {
			String apduString = value[i];
			sb.append(String.format("%04d%s", apduString.length(), apduString));
		}
		setField(kField_MultiApdu, sb.toString());
	}
	
	@Override
	public String getHexDataPacket(){
		StringBuilder sb = new StringBuilder();
		sb.append(hexOpCode);
		sb.append(hexRedirectionCode);
		sb.append(hexIMEI);
		sb.append(hexOpCode);
		sb.append(super.getHexDataPacket());
		int packetLength = (int) (Math.ceil(sb.length() / 2.0));
		sb.insert(0, String.format("%04d", packetLength));
		return sb.toString();
	}
	@Override
	protected int getFieldLength(int fieldID) {
		int result = ISO8583Packet.kFIELD_LENGTH_ZERO;
		switch (fieldID) {
		case TSMPacket.kField_UserName:
		case TSMPacket.kField_AccountID:
			result = ISO8583Packet.kFIELD_LENGTH_VARIABLE_LLVAR;
			break;
		case TSMPacket.kField_ApduTransmission:
		case TSMPacket.kField_SSDInfo:
		case TSMPacket.kField_PhoneInfo:
		case TSMPacket.kField_AppletInfo:
		case TSMPacket.kField_ApplicationDetails:
		case TSMPacket.kField_TransactionRecords:	
		case TSMPacket.kField_MultiApdu:
		case TSMPacket.kField_JSON_1:
		case TSMPacket.kField_JSON_2:
		case TSMPacket.kField_JSON_3:
			result = ISO8583Packet.kFIELD_LENGTH_VARIABLE_LLLVAR;
			break;
		case TSMPacket.kField_AnsCode:
		case TSMPacket.kField_OTAOperationStatus:
		case TSMPacket.kField_CardStatus:
			result = 2;
			break;
		case kField_StepNo:
		case kField_TransactionType:
			result = 4;
			break;
		case TSMPacket.kField_Time:
		case TSMPacket.kField_ApplicationID:
		case TSMPacket.kField_AppletCount:
			result = 6;
			break;
		case TSMPacket.kField_Date:
		case TSMPacket.kField_SessionID:
		case TSMPacket.kField_Challenge:
			result = 8;
			break;
		case TSMPacket.kField_CardBalacnce:
		case TSMPacket.kField_TradeAmount:
			result = 12;
			break;
		case TSMPacket.kField_ApplicationProvider:
			result = 15;
			break;
		case TSMPacket.kField_PaymentPassword:
			result = 16;
			break;
		case TSMPacket.kField_UserIDCardNumber:
			result = 18;
			break;
		case TSMPacket.kField_EncryptionKey:
			result = 24;
			break;
		case TSMPacket.kField_PhoneSEInfo:
			result = 47;
			break;
		case TSMPacket.kField_MAC:
			result = 64;
			break;
		default:
			break;
		}
		return result;
	}
	@Override
	protected int getFieldDataType(int fieldID) {
		int result = ISO8583Packet.kFIELD_DATA_TYPE_NOT_DEFINED;
		switch (fieldID) {
		case kField_PhoneSEInfo:
		case kField_SessionID:
		case kField_Challenge:
		case kField_ApduTransmission:
		case kField_SSDInfo:
		case kField_MultiApdu:
		case kField_EncryptionKey:
			result = ISO8583Packet.kFIELD_DATA_TYPE_BYTE_ARRAY;
			break;
		case kField_AccountID:
		case kField_UserName:
		case kField_UserPhoneNumber:
		case kField_UserIDCardNumber:
		case kField_PaymentPassword:
		case kField_AppletInfo:
		case kField_ApplicationDetails:
		case kField_TransactionRecords:	
		case kField_ApplicationProvider:
		case kField_JSON_1:
		case kField_JSON_2:
		case kField_JSON_3:
			result = ISO8583Packet.kFIELD_DATA_TYPE_ASCII;
			break;
		case kField_AnsCode:
		case kField_Date:
		case kField_Time:
		case kField_StepNo:
		case kField_ApplicationID:
		case kField_OTAOperationStatus:
		case kField_AppletCount:
		case kField_TradeAmount:
		case kField_CardBalacnce:
		case kField_CardStatus:
		case kField_TransactionType:
			result = ISO8583Packet.kFIELD_DATA_TYPE_BCD;
			break;
		case kField_MAC:
			result = ISO8583Packet.kFIELD_DATA_TYPE_BINARY;
		default:
			break;
		}
		return result;
	}
}

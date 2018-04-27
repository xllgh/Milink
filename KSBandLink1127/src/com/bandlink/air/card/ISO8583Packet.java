package com.bandlink.air.card;

import java.io.UnsupportedEncodingException;

import android.util.Log;

public abstract class ISO8583Packet {
	public static int kFIELD_LENGTH_VARIABLE_LLVAR = 1002;
	public static int kFIELD_LENGTH_VARIABLE_LLLVAR = 1003;
	public static int kFIELD_LENGTH_ZERO = 0;

	public static byte kFIELD_DATA_TYPE_BINARY = 0;
	public static byte kFIELD_DATA_TYPE_ASCII = 1;
	public static byte kFIELD_DATA_TYPE_BCD = 2;
	public static byte kFIELD_DATA_TYPE_BYTE_ARRAY = 3;
	public static byte kFIELD_DATA_TYPE_NOT_DEFINED = 99;

	protected long _bitmap;
	protected String _raw_packet;
	protected int _read_offset;
	protected int _packet_start_offset;
	protected String[] _fields;

	protected abstract int getFieldLength(int fieldID);

	protected abstract int getFieldDataType(int fieldID);

	public ISO8583Packet() {
		this._fields = new String[65];
		this._bitmap = 0;
	}

	public ISO8583Packet(byte[] packet, int packet_start_offset) {
		this();
		this._raw_packet = HexUtils.bytesToHexString(packet);
		this._read_offset = packet_start_offset * 2;
		this._packet_start_offset = packet_start_offset * 2;
		if (packet_start_offset > 0) {
			this._fields[0] = this._raw_packet.substring(0,
					packet_start_offset * 2);
		}
		this._bitmap = Long.parseLong(readHexData(16), 16);
		this.check_length();
		this.parse();
		this.postParse();
	}

	protected void postParse() {

	}

	public void check_length() {
		int length = Integer.parseInt(this._raw_packet.substring(0, 4));
		if ((length + 2) * 2 > this._raw_packet.length()) {
			Log.e("ISO8583Packet", String.format(
					"Length check failed! Length Head=%d, packet_length=%d",
					length + 2, this._raw_packet.length() / 2));
		}
	}

	protected void setRawField(int fieldID, byte[] data) {
		setHexRawField(fieldID, HexUtils.bytesToHexString(data));
	}

	protected void setHexRawField(int fieldID, String hexData) {
		this._fields[fieldID] = hexData;
		this._bitmap |= ((long) 1 << (64 - fieldID));
	}

	public void setField(int fieldID, String hexStringData) {
		if (!isFieldIDVaild(fieldID)) {
			return;
		}

		hexStringData = padDataIfStaticLength(fieldID, hexStringData);
		hexStringData = addLengthIfVarLength(fieldID, hexStringData);

		setRawField(fieldID, HexUtils.hexStringToBytes(hexStringData));
	}

	public String getFieldWithHex(int fieldID) {
		if (!isFieldIDVaild(fieldID)) {
			return null;
		}
		if (!isFieldExist(fieldID)) {
			return null;
		}
		int field_length = getFieldLength(fieldID);
		int offset = field_length == kFIELD_LENGTH_VARIABLE_LLLVAR ? 4 : 2;

		return field_length < 999 ? _fields[fieldID] : _fields[fieldID]
				.substring(offset);
	}

	public int getBCDFieldWithInteger(int fieldID) {
		return Integer.parseInt(getFieldWithHex(fieldID));
	}

	public String getFieldWithUTF8Encoding(int fieldID) {
		String temp = getFieldWithHex(fieldID);
		if (temp != null) {
			try {
				return new String(HexUtils.hexStringToBytes(temp), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	public String padDataIfStaticLength(int fieldID, String hexData) {
		int field_type = getFieldDataType(fieldID);
		int field_length = getFieldLength(fieldID);
		if (field_length > 999) {
			return hexData;
		}
		int dataLength = hexData.length();
		if (field_type == kFIELD_DATA_TYPE_ASCII) {
			dataLength = dataLength / 2;
			if (dataLength < field_length) {
				int paddingLength = field_length - dataLength;
				hexData = getHexSpacePaddingString(paddingLength) + hexData;
			}
		}
		if (field_type == kFIELD_DATA_TYPE_BCD) {
			if (dataLength < field_length) {
				hexData = LPad(hexData, field_length, '0');
			}
		}

		assert (getDataLength(hexData, field_type) != field_length);

		return hexData;
	}

	public String addLengthIfVarLength(int fieldID, String hexData) {
		int field_length = getFieldLength(fieldID);
		int field_type = getFieldDataType(fieldID);

		if (field_length < 1000) {
			return hexData;
		}

		String headerFormat = field_length == kFIELD_LENGTH_VARIABLE_LLVAR ? "%02d"
				: "%04d";
		int dataLength = getDataLength(hexData, field_type);
		if (field_type == kFIELD_DATA_TYPE_BCD && dataLength % 2 != 0) {
			hexData = '0' + hexData;
		}

		return String.format(headerFormat, dataLength) + hexData;
	}

	public int getDataLength(String hexData, int field_type) {
		int length = hexData.length();
		if (field_type == kFIELD_DATA_TYPE_ASCII
				|| field_type == kFIELD_DATA_TYPE_BYTE_ARRAY) {
			return length / 2;
		}

		if (field_type == kFIELD_DATA_TYPE_BINARY) {
			return length * 4;
		}

		return length;
	}

	public void setBCDField(int fieldID, int data) {
		if (!isFieldIDVaild(fieldID)) {
			return;
		}
		setField(fieldID, String.format("%02d", data));
	}

	public void setFieldWithUTF8Encoding(int fieldID, String origStr) {
		if (!isFieldIDVaild(fieldID)) {
			return;
		}
		try {
			String str = HexUtils.bytesToHexString(origStr.getBytes("UTF-8"));
			setField(fieldID, str);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public void removeField(int fieldID) {
		this._fields[fieldID] = null;
		this._bitmap &= (long) ((long) 0 << (64 - fieldID));
	}

	public byte[] getDataPacket() {
		return HexUtils.hexStringToBytes(getHexDataPacket());
	}

	public String getHexDataPacket() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("%016X", _bitmap));
		for (int i = 1; i < 65; i++) {
			if (_fields[i] != null) {
				sb.append(_fields[i]);
			}
		}
		return sb.toString();
	}

	protected byte[] readData(int length) {
		return HexUtils.hexStringToBytes(readHexData(length * 2));
	}

	protected String readHexData(int length) {
		if (this._read_offset + length > _raw_packet.length()) {
			return "";
		}
		String result = _raw_packet.substring(_read_offset, _read_offset
				+ length);
		this._read_offset += length;
		return result;
	}

	protected boolean isFieldExist(int fieldID) {
		if (!isFieldIDVaild(fieldID)) {
			return false;
		}
		return (this._bitmap >> (64 - fieldID) & 1) == 1;
	}

	protected boolean isFieldIDVaild(int fieldID) {
		return fieldID > 0 && fieldID < 65;
	}

	protected int getLengthByByte(int fieldLength, int fieldDataType) {
		if (fieldDataType == kFIELD_DATA_TYPE_BCD) {
			return (int) Math.ceil(fieldLength / 2.0);
		}

		if (fieldDataType == kFIELD_DATA_TYPE_BINARY) {
			return (int) Math.ceil(fieldLength / 8.0);
		}

		return fieldLength;
	}

	protected void parse() {
		for (int field_id = 1; field_id <= 64; field_id++) {
			if (!isFieldExist(field_id)) {
				continue;
			}
			int field_length = getFieldLength(field_id);
			int field_data_type = getFieldDataType(field_id);
			String result = "";
			if (field_length > 999) {
				int length_of_length = field_length == kFIELD_LENGTH_VARIABLE_LLVAR ? 2
						: 4;
				String lengthHeader = readHexData(length_of_length);
				field_length = getLengthByByte(Integer.valueOf(lengthHeader),
						field_data_type);
				String hexData = readHexData(field_length * 2);
				result = lengthHeader + hexData;
			} else {
				result = readHexData(getLengthByByte(field_length,
						field_data_type) * 2);
			}

			this.setHexRawField(field_id, result);
		}
	}

	public static String RPad(String str, Integer length, char car) {
		return str
				+ String.format("%" + (length - str.length()) + "s", "")
						.replace(" ", String.valueOf(car));
	}

	public static String LPad(String str, Integer length, char car) {
		return String.format("%" + (length - str.length()) + "s", "").replace(
				" ", String.valueOf(car))
				+ str;
	}

	public static String getHexSpacePaddingString(Integer length) {
		return String.format("%" + length + "s", "").replace(" ",
				String.valueOf("20"));
	}
}

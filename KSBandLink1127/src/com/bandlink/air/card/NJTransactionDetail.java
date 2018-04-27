package com.bandlink.air.card;

import java.util.Date;

public class NJTransactionDetail {
	public String CityID;
	public String TerminalCode;
	public Date TransDate;
	public Integer TransValue;
	public String TransType;
	public String TransTypeDescription;
	public String Reservation;
	public String Verification;

	public NJTransactionDetail(String transLogRecord) {
		this.CityID = transLogRecord.substring(0, 4);
		this.TerminalCode = transLogRecord.substring(4, 12);
		this.TransType = transLogRecord.substring(20, 22).toLowerCase();
		this.TransTypeDescription = "小额消费";
		if ((this.TransType.equals("02")) || (this.TransType.equals("0b"))
				|| (this.TransType.equals("11"))
				|| (this.TransType.equals("00"))) {
			this.TransTypeDescription = "地铁";
		}
		if (this.TransType.equals("01")) {
			this.TransTypeDescription = "公交";
		}
		this.Reservation = transLogRecord.substring(28, 30);
		this.Verification = transLogRecord.substring(30, 32);

		String timeString = transLogRecord.substring(12, 20);
		String transValue = transLogRecord.substring(22, 28);

		Long timeZoneFix = Long.valueOf((this.TransType.equals("0b"))
				|| (this.TransType.equals("11"))
				|| (this.TransType.equals("00")) ? 0 : 28800);
		Long timeLong = Long
				.valueOf((Long.valueOf(timeString, 16).longValue() - timeZoneFix
						.longValue()) * 1000L);

		this.TransDate = new Date(timeLong.longValue());

		String MSBValue = transValue.substring(4, 6)
				+ transValue.substring(2, 4) + transValue.substring(0, 2);
		this.TransValue = Integer.valueOf(MSBValue, 16);
	}

	public String toString() {
		return "NJTranscationDetail : " + this.CityID + ' ' + this.TerminalCode
				+ ' ' + this.TransType + ' ' + this.TransDate.toString() + ' '
				+ this.TransValue.toString() + ' ' + this.Reservation + ' '
				+ this.Verification;
	}
}

package com.bandlink.air.util;

public class Temperature {
	byte[] data;
	float maxTempe;
	long time;
	String date;
	int id;

	public Temperature(byte[] data, float maxTempe, long time, String date,int id) {
		super();
		this.data = data;
		this.maxTempe = maxTempe;
		this.time = time;
		this.date = date;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public float getMaxTempe() {
		return maxTempe;
	}

	public void setMaxTempe(float maxTempe) {
		this.maxTempe = maxTempe;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

}

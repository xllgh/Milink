package com.bandlink.air.gps;

public class GPSPointEntity {
	private int tid;
	private double latitude;
	private Double longitude;
	private String dateTime;
	private Double speed;
	private Double accuracy;
	
	public int getTid() {
		return tid;
	}
	public void setIdd(int tid) {
		this.tid = tid;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public Double getLongitude() {
		return longitude;
	}
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
	public String getDateTime() {
		return dateTime;
	}
	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
	}
	public Double getSpeed() {
		return speed;
	}
	public void setSpeed(Double speed) {
		this.speed = speed;
	}	
	public Double getAccuracy() {
		return accuracy;
	}
	public void setAccuracy(Double accuracy) {
		this.accuracy = accuracy;
	}		
}

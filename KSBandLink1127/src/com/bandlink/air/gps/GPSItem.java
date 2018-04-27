package com.bandlink.air.gps;

import com.baidu.mapapi.model.LatLng;

public class GPSItem {
	public double speed;
	public LatLng gp;
	public float radius;

	public GPSItem(double speed, LatLng geo,float radius) {
		// TODO Auto-generated constructor stub
		gp = geo;
		this.speed = speed;
		this.radius = radius;
	}

	public float getRadius() {
		return radius;
	}

	public void setRadius(float radius) {
		this.radius = radius;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public LatLng getGp() {
		return gp;
	}

	public void setGp(LatLng gp) {
		this.gp = gp;
	}

}

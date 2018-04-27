/*
 *  Pedometer - Android App
 *  Copyright (C) 2009 Levente Bagi
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.milink.android.lovewalk.bluetooth.service;

import java.util.ArrayList;

import android.hardware.SensorListener;

/**
 * Counts steps provided by StepDetector and passes the current step count to
 * the activity.
 */
public class StepDisplayer implements SensorListener {

	private int mCount = 0;
	private int mColorie = 0;
	private int mDistance = 0;

	public void setSteps(int steps, int cal) {
		mCount = steps;
		mColorie = cal;
		mDistance = 0;
		notifyListener();
	}

	public void onStep(int step, int colorie, int distance) {
		mCount += step;
		mColorie += colorie;
		mDistance += distance;
		notifyListener();
	}

	public void reloadSettings() {
		notifyListener();
	}

	public void passValue() {
	}

	public int getSteps() {
		return mCount;
	}

	public int getCal() {
		return mColorie;
	}

	// -----------------------------------------------------
	// Listener

	public interface Listener {
		public void stepsChanged(int step, int colorie, int distance);

		public void passValue();
	}

	private ArrayList<Listener> mListeners = new ArrayList<Listener>();

	public void addListener(Listener l) {
		mListeners.add(l);
	}

	public void notifyListener() {
		for (Listener listener : mListeners) {
			listener.stepsChanged(mCount, mColorie, mDistance);
		}
	}

	@Override
	public void onSensorChanged(int sensor, float[] values) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAccuracyChanged(int sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

}

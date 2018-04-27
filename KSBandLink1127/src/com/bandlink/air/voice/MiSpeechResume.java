package com.bandlink.air.voice;

import android.content.Context;

import com.bandlink.air.R;
public class MiSpeechResume extends MiSpeech {

	public MiSpeechResume(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void prepare() {
		// TODO Auto-generated method stub
		pp3.clear();
		pp3.add(new soundtype1(R.raw.resuming_workout, 1.0f));

	}

}

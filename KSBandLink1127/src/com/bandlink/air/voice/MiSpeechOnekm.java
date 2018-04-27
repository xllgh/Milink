package com.bandlink.air.voice;

import java.util.List;

import android.content.Context;

import com.bandlink.air.R;
public class MiSpeechOnekm extends MiSpeech {

	int mkm=0;
	int mseconds=0;
	public MiSpeechOnekm(Context context,int km, int seconds) {
		super(context);
		// TODO Auto-generated constructor stub
		mkm=km;
		mseconds=seconds;
	}

	@Override
	public void prepare() {
		// TODO Auto-generated method stub
		pp3.clear();
		pp3.add(new soundtype1(R.raw.congratulations, 1.0f));
		pp3.add(new soundtype1(R.raw.youve_walked, 1.0f));

		List<soundtype1> rr=num2list(mkm);
		for(int i=0;i<rr.size();i++)
			pp3.add(rr.get(i));

		
		pp3.add(new soundtype1(R.raw.kilometers, 1.0f));
		
		pp3.add(new soundtype1(R.raw.average_pace, 1.0f));
		

		rr=num2list(mseconds);
		for(int i=0;i<rr.size();i++)
			pp3.add(rr.get(i));

		pp3.add(new soundtype1(R.raw.second, 1.0f));		
		pp3.add(new soundtype1(R.raw.per_kilometer, 1.0f));		
		
	}

}

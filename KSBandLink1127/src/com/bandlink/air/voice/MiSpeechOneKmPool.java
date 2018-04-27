package com.bandlink.air.voice;

import java.util.HashMap;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.bandlink.air.R;

public class MiSpeechOneKmPool {

	
	int mdurance=0;
	int mTotalDistance=0;
	
	
	Context cc;
	SoundPool soundPool;  
	HashMap<String, Integer> soundMap1 = new HashMap<String, Integer>();  
	HashMap<Integer, Integer> soundMap2 = new HashMap<Integer, Integer>();  
	
	public MiSpeechOneKmPool(Context context,int durance,int totalDistance) {
		cc=context;
		mdurance=durance;
		mTotalDistance = totalDistance;

		// TODO Auto-generated constructor stub
		
		soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
		
        soundMap1.put("wan", soundPool.load(context, R.raw.ten_thousand, 1));
        soundMap1.put("qian", soundPool.load(context, R.raw.thousand, 1));
        soundMap1.put("bai", soundPool.load(context, R.raw.hundred, 1));
        soundMap1.put("shi", soundPool.load(context, R.raw.ten, 1));
        
        soundMap1.put("congratulations", soundPool.load(context, R.raw.congratulations, 1));
        soundMap1.put("youve_walked", soundPool.load(context, R.raw.youve_walked, 1));
        soundMap1.put("kilometers", soundPool.load(context, R.raw.kilometers, 1));
        soundMap1.put("average_pace", soundPool.load(context, R.raw.average_pace, 1));
        soundMap1.put("per_kilometer", soundPool.load(context, R.raw.per_kilometer, 1));

        soundMap1.put("hour", soundPool.load(context, R.raw.hour, 1));
        soundMap1.put("minute", soundPool.load(context, R.raw.minute, 1));
        soundMap1.put("second", soundPool.load(context, R.raw.second, 1));
        
        
        soundMap2.put(1, soundPool.load(context, R.raw.one, 1));
        soundMap2.put(2, soundPool.load(context, R.raw.two, 1));
        soundMap2.put(3, soundPool.load(context, R.raw.three, 1));
        soundMap2.put(4, soundPool.load(context, R.raw.four, 1));
        soundMap2.put(5, soundPool.load(context, R.raw.five, 1));
        soundMap2.put(6, soundPool.load(context, R.raw.six, 1));
        soundMap2.put(7, soundPool.load(context, R.raw.seven, 1));
        soundMap2.put(8, soundPool.load(context, R.raw.eight, 1));
        soundMap2.put(9, soundPool.load(context, R.raw.nine, 1));
        soundMap2.put(0, soundPool.load(context, R.raw.zero, 1));		
        
		AudioManager mAudioManager = (AudioManager) cc.getSystemService(Context.AUDIO_SERVICE); 
		//最大音量    
		int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);    
		mAudioManager.setStreamVolume(AudioManager.STREAM_RING, maxVolume, AudioManager.FLAG_PLAY_SOUND);
        
	}

	public void play() throws InterruptedException
	{
		int dd1=260;
		int dd=350;

		
		soundPool.play(soundMap1.get("congratulations"), 1, 1, 0, 0, 1);
		Thread.sleep(1200);
		soundPool.play(soundMap1.get("youve_walked"), 1, 1, 0, 0, 1);
		Thread.sleep(1200);
		//play1(1);
		play1(mTotalDistance);
		soundPool.play(soundMap1.get("kilometers"), 1, 1, 0, 0, 1);
		Thread.sleep(dd1+500);
		soundPool.play(soundMap1.get("average_pace"), 1, 1, 0, 0, 1);
		Thread.sleep(dd1+1200);
		
		soundPool.play(soundMap1.get("per_kilometer"), 1, 1, 0, 0, 1);
		Thread.sleep(1200);		
		
		int hour = mdurance/3600;
		int minute = (mdurance%3600)/60;
		int second = mdurance%60;
		if(hour != 0)
		{
		play1(hour);
		soundPool.play(soundMap1.get("hour"), 1, 1, 0, 0, 1);
		Thread.sleep(dd1);
		}
		
		if(minute !=0)
		{
			play1(minute);
			soundPool.play(soundMap1.get("minute"), 1, 1, 0, 0, 1);
			Thread.sleep(dd1);
		}
		
		if(second !=0)
		{
			play1(second);
			soundPool.play(soundMap1.get("second"), 1, 1, 0, 0, 1);
			Thread.sleep(dd1+500);
		}
		
		if(mdurance == 0)
		{
			soundPool.play(soundMap2.get(0), 1, 1, 0, 0, 1);
			Thread.sleep(450);
			soundPool.play(soundMap1.get("second"), 1, 1, 0, 0, 1);
			Thread.sleep(dd1+500);
			
		}

		
	}
	

	public void play1(int num) throws InterruptedException
	{
		
		
		int nn=num;
		
		int dd1=260;
		int dd=350;
		
		int wan = nn/10000;
		nn %= 10000;
		int qian = nn/1000;
		nn %= 1000;
		int bai = nn/100;
		nn%=100;
		int shi = nn/10;
		nn %= 10;
		int ge = nn;
		float ff2=0.7f;

		
		//soundPool.play(soundPool.load(this, R.raw.workout_completed, 1), 1, 1, 0, 0, 1);
		//Thread.sleep(2222+200);
		boolean notplay=true;
		boolean jump1=false;
		boolean jumpbegin1=false;
		if(wan > 0)
		{
			notplay=false;
			jumpbegin1=true;
			if(wan > 10)
			{
				play1(wan);
			}
			else {			
			soundPool.play(soundMap2.get(wan), 1, 1, 0, 0, 1);
			Thread.sleep(dd1+200);
			}
			soundPool.play(soundMap1.get("wan"), ff2, ff2, 0, 0, 1);
		}
		else {
			if(jumpbegin1)
			jump1=true;
		}
		
		

		
		if(qian > 0)
		{
			notplay=false;
			jumpbegin1=true;
			if(jump1)
			{
				Thread.sleep(dd);
				soundPool.play(soundMap2.get(0), 1, 1, 0, 0, 1);			
				jump1=false;
			}
			Thread.sleep(dd);
			soundPool.play(soundMap2.get(qian), 1, 1, 0, 0, 1);
			Thread.sleep(dd1);
			soundPool.play(soundMap1.get("qian"), ff2, ff2, 0, 0, 1);
		}
		else {
			if(jumpbegin1)
			jump1=true;
			
		}
		
	
		if(bai > 0)
		{
			notplay=false;
			jumpbegin1=true;
			if(jump1)
			{
				Thread.sleep(dd);
				soundPool.play(soundMap2.get(0), 1, 1, 0, 0, 1);			
				jump1=false;
			}
			Thread.sleep(dd);
			soundPool.play(soundMap2.get(bai), 1, 1, 0, 0, 1);
			Thread.sleep(dd1+200);
			soundPool.play(soundMap1.get("bai"), ff2, ff2, 0, 0, 1);
		}
		else {
			if(jumpbegin1)
			jump1=true;
			
		}

		if(shi > 0)
		{
			notplay=false;
			jumpbegin1=true;
			if(jump1)
			{
				Thread.sleep(dd);
				soundPool.play(soundMap2.get(0), 1, 1, 0, 0, 1);			
				jump1=false;
			}
			Thread.sleep(dd);
			soundPool.play(soundMap2.get(shi), 1, 1, 0, 0, 1);
			Thread.sleep(dd1);
			soundPool.play(soundMap1.get("shi"), ff2, ff2, 0, 0, 1);
			Thread.sleep(200);
		}
		else {
			if(jumpbegin1)
			jump1=true;
			
		}


		if(ge > 0)
		{
			notplay=false;
			jumpbegin1=true;
			if(jump1)
			{
				Thread.sleep(dd);
				soundPool.play(soundMap2.get(0), 1, 1, 0, 0, 1);			
				jump1=false;
			}
			Thread.sleep(dd);
			soundPool.play(soundMap2.get(ge), 1, 1, 0, 0, 1);
			//soundPool.play(soundMap1.get("wan"), 1, 1, 0, 0, 1);
		}
		/*
		if(notplay)
		{
			Thread.sleep(dd);
			soundPool.play(soundMap2.get(0), 1, 1, 0, 0, 1);
		}*/
		Thread.sleep(dd+100);		
		
	}	
}

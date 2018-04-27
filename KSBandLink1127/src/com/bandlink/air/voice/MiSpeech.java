package com.bandlink.air.voice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

import com.bandlink.air.R;


public abstract class MiSpeech {
	Context cc=null;
	int mp_index=0;
	List<soundtype1> pp3 = new ArrayList<soundtype1>();
	private MediaPlayer mp = new MediaPlayer();
    HashMap<String, Integer> soundMap1 = new HashMap<String, Integer>();    
    HashMap<Integer, Integer> soundMap3 = new HashMap<Integer, Integer>();
	
	public MiSpeech(Context context)
	{
		cc=context;
		
		  soundMap1.put("wan", R.raw.ten_thousand);
	        soundMap1.put("qian",R.raw.thousand);
	        soundMap1.put("bai", R.raw.hundred);
	        soundMap1.put("shi", R.raw.ten);

	        soundMap3.put(1,R.raw.one);
	        soundMap3.put(2,R.raw.two);
	        soundMap3.put(3,R.raw.three);
	        soundMap3.put(4,R.raw.four);
	        soundMap3.put(5,R.raw.five);
	        soundMap3.put(6,R.raw.six);
	        soundMap3.put(7,R.raw.seven);
	        soundMap3.put(8,R.raw.eight);
	        soundMap3.put(9,R.raw.nine);
	        soundMap3.put(0,R.raw.zero);	
	}
	
	public void start()
	{
		prepare();
		mp_index=0;
		mp = MediaPlayer.create(cc, pp3.get(0).resid);
		mp.setOnCompletionListener(lis2);
		
		//音量控制,初始化定义    
		AudioManager mAudioManager = (AudioManager) cc.getSystemService(Context.AUDIO_SERVICE); 
		//最大音量    
		int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);    
		//当前音量    
		int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC); 
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, AudioManager.FLAG_PLAY_SOUND);
		mp.setVolume(pp3.get(mp_index).volume, pp3.get(mp_index).volume);
		mp.start();		
      	
	}
	
	public void stop()
	{
		/**try {
			if(mp!=null)
			{
				mp.setVolume(0,0);
				mp.stop();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}**/
	}
	
	abstract public void prepare();
	
	
	OnCompletionListener lis2= new OnCompletionListener() {
		
		public void onCompletion(MediaPlayer mp) {
			// TODO Auto-generated method stub
			if(mp_index >= pp3.size()-1)
			{
				mp.stop();
				mp.release();
			}
			else {
				
			
			try {
				mp_index++;
				mp.stop();				
				mp.release();
				mp=MediaPlayer.create(cc, pp3.get(mp_index).resid);
				mp.setVolume(pp3.get(mp_index).volume, pp3.get(mp_index).volume);
				mp.setOnCompletionListener(lis2);		
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			mp.start();
			}
		}
	};	
	
	List<soundtype1> num2list(int num)
	{
		
		List<soundtype1> rr=new ArrayList<soundtype1>();
		
		int nn=num;
		
		
		int wan = nn/10000;
		nn %= 10000;
		int qian = nn/1000;
		nn %= 1000;
		int bai = nn/100;
		nn%=100;
		int shi = nn/10;
		nn %= 10;
		int ge = nn;
		float ff1 = 1.0f;
		float ff2=0.7f;

		
		boolean allnull=true;
		boolean jump1=false;
		boolean jumpbegin1=false;
		if(wan > 0)
		{
			allnull=false;
			jumpbegin1=true;
			if(wan > 10)
			{
				List<soundtype1> rrr=num2list(wan);
				for(int i=0;i<rrr.size();i++)
					rr.add(rrr.get(i));
			}
			else
			{
				rr.add(new soundtype1(soundMap3.get(wan), ff1));
			}
			rr.add(new soundtype1(soundMap1.get("wan"), ff2));
		}
		else {
			if(jumpbegin1)
			jump1=true;
		}
		
		

		
		if(qian > 0)
		{
			allnull=false;
			jumpbegin1=true;
			if(jump1)
			{
				rr.add(new soundtype1(soundMap3.get(0), ff1));
				jump1=false;
			}
			rr.add(new soundtype1(soundMap3.get(qian), ff1));
			rr.add(new soundtype1(soundMap1.get("qian"), ff2));
		}
		else {
			if(jumpbegin1)
			jump1=true;
			
		}
		
	
		if(bai > 0)
		{
			allnull=false;
			jumpbegin1=true;
			if(jump1)
			{
				rr.add(new soundtype1(soundMap3.get(0), ff1));
				jump1=false;
			}
			rr.add(new soundtype1(soundMap3.get(bai), ff1));
			rr.add(new soundtype1(soundMap1.get("bai"), ff2));
		}
		else {
			if(jumpbegin1)
			jump1=true;
			
		}

		if(shi > 0)
		{
			allnull=false;
			jumpbegin1=true;
			if(jump1)
			{
				rr.add(new soundtype1(soundMap3.get(0), ff1));
				jump1=false;
			}
			rr.add(new soundtype1(soundMap3.get(shi), ff1));
			rr.add(new soundtype1(soundMap1.get("shi"), ff2));
		}
		else {
			if(jumpbegin1)
			jump1=true;
			
		}


		if(ge > 0)
		{
			allnull=false;
			jumpbegin1=true;
			if(jump1)
			{
				rr.add(new soundtype1(soundMap3.get(0), ff1));
				jump1=false;
			}
			rr.add(new soundtype1(soundMap3.get(ge), ff1));
		}
		
		if(allnull)
			rr.add(new soundtype1(soundMap3.get(0), ff1));
		return rr;
	}	
}

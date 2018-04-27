package com.bandlink.air.util;

import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;

import com.bandlink.air.R;
import com.bandlink.air.ble.BluetoothLeService;

public class MyMediaPlayer extends Thread {

	private MediaPlayer mMediaPlayer;
	private Context context;
	private int i;
	private boolean iscontinue;
	private boolean isAlarm;
	private String name;
	private String path;
	private SharedPreferences sharePre;
	OnRingOver over;

	public MyMediaPlayer(Context c, boolean isa) {
		context = c;
		isAlarm = isa;
		iscontinue = true;
		// // 报警铃声
		// set_warning_ring.setText(sharePre.getString("set_warning_ring","default"));
		// set_warning_ring_path =
		// sharePre.getString("set_warning_ring_path","media/internal/audio/media/1");
		if (isAlarm) {
			sharePre = context.getSharedPreferences("air",
					context.MODE_MULTI_PROCESS);
			name = sharePre.getString("set_warning_ring", "default");
			path = sharePre.getString("set_warning_ring_path", "default");
		} else {
			try {
				AudioManager audio = (AudioManager) c
						.getSystemService(Service.AUDIO_SERVICE);
				{
					audio.setStreamVolume(
							AudioManager.STREAM_MUSIC,
							audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
							0);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void setOnRingOverListener(OnRingOver o) {
		this.over = o;
	}

	public void Stop() {
		iscontinue = false;

		if (isAlarm) {
			BluetoothLeService.onLostAlarm = false;
		} else {
			BluetoothLeService.isOnFinding = false;
		}

		if (mMediaPlayer != null) {
			mMediaPlayer.reset();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		if (over != null) {
			over.onRringOver();
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		if (mMediaPlayer != null) {

			mMediaPlayer.stop();

		}
		i = 0;

		if (isAlarm) {
			if (name.equals("default")) {
				// MediaPlayer.create(context,resId)这个方式配置数据源后，就完成了初始化，所以不用prepare可以直接start了
				mMediaPlayer = MediaPlayer.create(context, R.raw.lost1);
				mMediaPlayer.start();
			} else {
				mMediaPlayer = new MediaPlayer();
				try {
					mMediaPlayer.setDataSource(path);

					mMediaPlayer.setLooping(true);
					try {
						mMediaPlayer.prepareAsync();

					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						try {
							mMediaPlayer.prepare();
							mMediaPlayer.start();
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					mMediaPlayer
							.setOnPreparedListener(new OnPreparedListener() {

								@Override
								public void onPrepared(MediaPlayer mp) {
									// TODO Auto-generated method stub
									mp.start();
								}
							});
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					// MediaPlayer.create(context,resId)这个方式配置数据源后，就完成了初始化，所以不用prepare可以直接start了
					mMediaPlayer = MediaPlayer.create(context, R.raw.lost1);
					mMediaPlayer.start();
				}
			}

		} else {
			// MediaPlayer.create(context,resId)这个方式配置数据源后，就完成了初始化，所以不用prepare可以直接start了
			mMediaPlayer = MediaPlayer.create(context, R.raw.lost1);
			mMediaPlayer.start(); 
		}

		if (isAlarm) {
			BluetoothLeService.onLostAlarm = true;
			BluetoothLeService.isOnFinding = false;
		} else {
			BluetoothLeService.onLostAlarm = false;
			BluetoothLeService.isOnFinding = true;
		}
		while (iscontinue) {
			i++;
			// 10-21 改为15秒响
			if (i == 15) {
				i = 0;
				mMediaPlayer.setLooping(false);
				break;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		BluetoothLeService.onLostAlarm = false;
		Stop();

	}

	public interface OnRingOver {
		public void onRringOver();
	}

}

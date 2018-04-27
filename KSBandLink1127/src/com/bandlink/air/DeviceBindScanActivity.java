package com.bandlink.air;

import java.io.IOException;
import java.util.Vector;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.bandlink.air.Zxing.camera.CameraManager;
import com.bandlink.air.Zxing.decoding.CaptureActivityHandler;
import com.bandlink.air.Zxing.decoding.InactivityTimer;
import com.bandlink.air.Zxing.view.ViewfinderView;
import com.bandlink.air.util.ActionbarSettings;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

public class DeviceBindScanActivity extends CaptureBaseActivity {

	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	private TextView txtResult;
	private InactivityTimer inactivityTimer;
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private static final float BEEP_VOLUME = 0.10f;
	private boolean vibrate;
	private int code;
	private int closecode;
	private boolean formfriend = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera_scan);
		ActionbarSettings actionbar = new ActionbarSettings(this,
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						DeviceBindScanActivity.this.finish();
					}
				}, null);
		formfriend = getIntent().getBooleanExtra("formfriend", false);
		actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
		actionbar.setTitle(R.string.scanning);
		CameraManager.init(getApplication());

		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		txtResult = (TextView) findViewById(R.id.txtResult);
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
		code = getIntent().getIntExtra("type", 1);
		closecode = getIntent().getIntExtra("close", 1);
	}

	@Override
	protected void onResume() {
		super.onResume();
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		decodeFormats = null;
		characterSet = null;

		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}
		initBeepSound();
		vibrate = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
			if (handler == null) {
				handler = new CaptureActivityHandler(this, decodeFormats,
						characterSet);
			}
		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;

	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();

	}

	public void handleDecode(Result obj, Bitmap barcode) {
		inactivityTimer.onActivity();
		viewfinderView.drawResultBitmap(barcode);
		playBeepSoundAndVibrate();
		txtResult.setText(obj.getBarcodeFormat().toString() + ":"
				+ obj.getText());
		System.out.println(obj.getBarcodeFormat().toString() + ":"
				+ obj.getText());
//flame  http://www.lovefit.com/move.htm?did=728815050001219&passwd=325253
		Intent ii = null;
		if (obj.getText().contains("did")) {
			ii = new Intent(this, DeviceSettingBinding.class);
			ii.putExtra("did", obj.getText());
			setResult(20, ii);
		} else if (obj.getText().contains("uid")) {
			Intent intent = new Intent("com.milink.android.lovewalk.scanfriend");
			intent.putExtra("uid", obj.getText());
			this.sendBroadcast(intent);
		} else if(obj.getText().contains("clubid")){
			Intent intent = new Intent(ClubFragment.CLUB_SCAN_ACTION);
			intent.putExtra("content", obj.getText());
			this.sendBroadcast(intent);
		}else{
			if (obj.getBarcodeFormat().toString().equals("QR_CODE")) {
				Intent intent = new Intent(
						"com.milink.android.lovewalk.scanfriend");
				intent.putExtra("uid","uid:"+ obj.getText());
				this.sendBroadcast(intent);
			} else {
				Toast.makeText(getApplicationContext(),
						getString(R.string.scan_code_error), Toast.LENGTH_SHORT)
						.show();
			}
		}

		finish();
	}

	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// so we now play on the music stream.
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(
					R.raw.beep);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(),
						file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}

	private static final long VIBRATE_DURATION = 200L;

	private void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};

	public void btnmainright(View view) {
		// Intent ii = new Intent(this,DeviceBindActivity.class);
		// startActivity(ii);

		finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			this.finish();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}

	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub
		
	}

}
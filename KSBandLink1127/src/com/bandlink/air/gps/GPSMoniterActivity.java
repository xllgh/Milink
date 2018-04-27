package com.bandlink.air.gps;

import java.util.Calendar;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.bandlink.air.GpsSportActivity;
import com.bandlink.air.R;
import com.bandlink.air.gps.MiLocationServiceOnce.UpdateUIListenerOnce;
import com.bandlink.air.util.LovefitActivity;

public class GPSMoniterActivity extends  LovefitActivity implements
		UpdateUIListenerOnce {
	private ImageView gps;
	private MiLocationServiceOnce loconceService;
	private boolean bResumed = true;
	private Bundle bundle;
	private TextView gpstext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		bundle = getIntent().getExtras();
		setContentView(R.layout.activity_gpsmoniter);
		startLocationOnce();
		gps = (ImageView) findViewById(R.id.gps);
		gpstext = (TextView) findViewById(R.id.singaltext);
//		Intent intents = new Intent(this, StepService.class);
//		startService(intents);
		super.onCreate(savedInstanceState);
	}

	private ServiceConnection connOnce = new ServiceConnection() {
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			loconceService = ((MiLocationServiceOnce.MyBinder) arg1)
					.getService();
			loconceService.setListener(GPSMoniterActivity.this);

		}

		public void onServiceDisconnected(ComponentName name) {
			loconceService = null;
		}
	};

	public void startLocationOnce() {
		try {
			Intent intent = new Intent(GPSMoniterActivity.this,
					MiLocationServiceOnce.class);
			this.getApplicationContext().bindService(intent, connOnce,
					BIND_AUTO_CREATE);
			// bindService(intent, conn, BIND_AUTO_CREATE);
			
		} catch (Exception e) {
		}

	}

	public void endLocationOnce() {
		// if(connOnce != null)
		try {
			this.getApplicationContext().unbindService(connOnce);
		} catch (Exception e) {
			// TODO: handle exception
			int ii = 10;
			ii++;
		}
		// connOnce = null;
		// unbindService(conn);
		
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		bResumed = true;
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		bResumed = false;
		super.onPause();
	}

	public void gpsrun(View view) {
		endLocationOnce();
		Intent intent = new Intent(GPSMoniterActivity.this,
				GpsSportActivity.class);
		intent.putExtras(bundle);
		startActivity(intent);
		this.finish();
	}

	public void back(View view) {
		endLocationOnce();
		this.finish();
	}
	

	private Long timelast = 0l;

	@Override
	public void onchangeUI(int index, BDLocation locData) {
		// TODO Auto-generated method stub
		float accuracy = locData.getRadius();
		long time = Calendar.getInstance().getTimeInMillis();
		long timedelta = time - timelast;
		if (timedelta > 1000 || timelast == 0) {
			timelast = time;
			String tag = (String) gps.getTag();
			if (bResumed) {
				if (0 < accuracy && accuracy <= 20) {
					if (!tag.equals("gps3")) {
						gps.setImageResource(R.drawable.gps_strength3);
						gps.setTag("gps3");
						gpstext.setText(R.string.gps_singal3);

					}
				} else if (20 < accuracy && accuracy < 70) {
					if (!tag.equals("gps2")) {
						gps.setImageResource(R.drawable.gps_strength2);
						gps.setTag("gps2");
						gpstext.setText(R.string.gps_singal2);
					}
				} else {
					if (!tag.equals("gps1")) {
						gps.setImageResource(R.drawable.gps_strength1);
						gps.setTag("gps1");
						gpstext.setText(R.string.gps_singal1);

					}
				}
			}
		}

	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub
		
	}
 

}

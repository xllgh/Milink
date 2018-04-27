package com.bandlink.air.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bandlink.air.R;
import com.bandlink.air.view.NewWheelInt.OnValueChangeListener;

public class HeightRuler extends Activity implements OnClickListener {
 
	public float densityDpi;
	public float scale;
	private int screenWidth; 
	private int result1, start;
	private TextView upload, cancel;
	EditText cur;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.height);
		Bundle bundle = getIntent().getExtras();
		cur = (EditText) findViewById(R.id.cur_w);
		final Handler h = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				if (cur != null)
					cur.setText(msg.obj + "");
				super.handleMessage(msg);
			}

		};
		NewWheelInt w = (NewWheelInt)findViewById(R.id.wheel);
		w.setValueChangeListener(new OnValueChangeListener() {
			
			@Override
			public void onValueChange(float value) {
				// TODO Auto-generated method stub
				h.obtainMessage(0, (int)value).sendToTarget();
			}
		});
		String str = bundle.getString("uheight");
		if (str != null) {
			if (str.contains(".")) {
				start = Integer
						.parseInt(str.substring(0, str.lastIndexOf(".")));
			} else {
				start = Integer.parseInt(str);
			}
		}

		DisplayMetrics dm = this.getApplicationContext().getResources()
				.getDisplayMetrics();
		screenWidth = dm.widthPixels;// 540
		densityDpi = dm.densityDpi;
		scale = dm.density;// 1.5
	 
		
	 
		w.initViewParam(start, 250, NewWheelInt.MOD_TYPE_ONE);
		h.obtainMessage(0, start).sendToTarget();
		upload = (TextView) findViewById(R.id.upload);
		cancel = (TextView) findViewById(R.id.cancel);
		upload.setOnClickListener(this);
		cancel.setOnClickListener(this);

	}

	 
 

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.upload:
			Intent intent = getIntent();
			String h = Integer.toString(result1);

			if (intent.getExtras().containsKey("uheight")) {
				Editable e = cur.getText();
				if (e == null || Double.valueOf(e.toString()) > 300
						|| Double.valueOf(e.toString()) <= 0) {
					Toast.makeText(getApplicationContext(),
							R.string.input_error, Toast.LENGTH_SHORT).show();
					break;
				}
				intent.putExtra("uheight", e.toString());
				intent.putExtra("on", "on");
				this.setResult(5, intent);
			}
			this.finish();
			break;
		case R.id.cancel:
			HeightRuler.this.finish();
		default:
			break;
		}
	}
}

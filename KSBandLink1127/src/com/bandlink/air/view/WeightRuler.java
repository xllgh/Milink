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
import com.bandlink.air.view.NewWheel.OnValueChangeListener;

public class WeightRuler extends Activity implements OnClickListener {

	public float densityDpi;
	public float scale;
	private float result1;
	private TextView upload, cancel;
	private float start;
	private String title;
	private EditText cur;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		setContentView(R.layout.upload_weight);
		Bundle bundle = getIntent().getExtras();
		cur = (EditText) findViewById(R.id.cur_w);

		String str = bundle.getString("start_w");
		title = bundle.getString("title");
		if (str != null) {
			if (str.contains(".")) {
				start = Float.parseFloat(str);
			} else {
				start = Float.parseFloat(str);
			}
		}

		DisplayMetrics dm = this.getApplicationContext().getResources()
				.getDisplayMetrics();
		densityDpi = dm.densityDpi;// 240
		scale = dm.density;// 1.5
		NewWheel wheel = (NewWheel) findViewById(R.id.wheel);

		final Handler h = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				if (cur != null)
					cur.setText(msg.obj + "");
				super.handleMessage(msg);
			}

		};

		try {
			if (getIntent().getExtras().get("pppp").equals("pp")) {
				if (start == 0) {
					wheel.initViewParam(60, 300, NewWheel.MOD_TYPE_ONE);
				} else {
					wheel.initViewParam(start, 300, NewWheel.MOD_TYPE_ONE);
					h.obtainMessage(0, start).sendToTarget();
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			wheel.initViewParam(start, 300, NewWheel.MOD_TYPE_ONE);
			h.obtainMessage(0, start).sendToTarget();
		}
		wheel.setValueChangeListener(new OnValueChangeListener() {

			@Override
			public void onValueChange(float value) {
				// TODO Auto-generated method stub
				h.obtainMessage(0, value).sendToTarget();
			}
		});
		TextView titletext = (TextView) findViewById(R.id.position);
		titletext.setText(title);
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
			String weight = Float.toString(result1);

			if (intent.getExtras().containsKey("startEdit1")) {
				Editable e = cur.getText();
				String str = "";
				if (e == null || Double.valueOf(e.toString()) > 350
						|| Double.valueOf(e.toString()) <= 0) {
					Toast.makeText(getApplicationContext(),
							R.string.input_error, Toast.LENGTH_SHORT).show();
					break;
				}
				intent.putExtra("startEdit", e.toString());
				intent.putExtra("on", "on");
				this.setResult(4, intent);
				this.finish();
			} else if (intent.getExtras().containsKey("endEdit1")) {
				intent.putExtra("endEdit", weight);
				intent.putExtra("on", "on");
				this.setResult(2, intent);
				this.finish();
			}

			break;
		case R.id.cancel:
			WeightRuler.this.finish();
		default:
			break;
		}
	}
}

interface OnWeightChanged {
	void onWeightChange(float w);

	void onWeightChange(int w);
}

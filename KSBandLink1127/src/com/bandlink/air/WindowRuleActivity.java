package com.bandlink.air;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class WindowRuleActivity extends Activity {

	EditText et;
	Button btn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pop_create);
		
		et = (EditText) findViewById(R.id.content);
		btn = (Button) findViewById(R.id.send);
	 
		try {
			String str = getIntent().getStringExtra("content");
			et.setText(str);
		} catch (Exception e) {
			e.printStackTrace();
		}
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (et.getText().toString().length() > 0) {
					Intent i = new Intent();
					i.putExtra("content", et.getText().toString());
					setResult(30, i);
					WindowRuleActivity.this.finish();
				}
			}
		});
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		this.finish();
		return super.onTouchEvent(event);
	}

}

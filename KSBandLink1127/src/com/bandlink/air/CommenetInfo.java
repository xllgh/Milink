package com.bandlink.air;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class CommenetInfo extends Activity {
	EditText et, etshare;
	Button btn, btn_share;
	LinearLayout comment, share;
	int ss;
	Intent i;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.commentinfon);
		et = (EditText) findViewById(R.id.content);
		btn = (Button) findViewById(R.id.iiii);
		comment = (LinearLayout) findViewById(R.id.comment);
		etshare = (EditText) findViewById(R.id.content1);
		btn_share = (Button) findViewById(R.id.iiii1);
		share = (LinearLayout) findViewById(R.id.share);
		i = getIntent();
		ss = i.getExtras().getInt("num");
		if (ss == 0) {
			et.setFocusable(true);
			et.setFocusableInTouchMode(true);
			et.requestFocus();
			btn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					String t = et.getText().toString();
					if (t != null && t.length() > 0) {
						i.putExtra("commentinfo", t);
						setResult(8, i);
						CommenetInfo.this.finish();
					}

				}
			});
		} else {
			comment.setVisibility(View.GONE);
			share.setVisibility(View.VISIBLE);
			etshare.setFocusable(true);
			etshare.setFocusableInTouchMode(true);
			etshare.requestFocus();
			btn_share.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					String t = etshare.getText().toString();
					if (t != null && t.length() > 0) {
						i.putExtra("commentinfo", t);
						setResult(9, i);
						CommenetInfo.this.finish();
					}

				}
			});
		}

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub

		super.onDestroy();
	}

	public boolean onTouchEvent(android.view.MotionEvent event) {
		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
		String t = et.getText().toString();
		setResult(1, i);
		CommenetInfo.this.finish();
		return true;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_UP) {
			setResult(1, i);
			CommenetInfo.this.finish();
			return true;
		}
		return super.dispatchKeyEvent(event);
	}
}

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
import android.widget.TextView;

public class WindowCommentActivity extends Activity {
	EditText et;
	Button btn;
	View v;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// Make us non-modal, so that others can receive touch events.

		setContentView(R.layout.pop_comment);
		v = getWindow().getDecorView().findViewById(android.R.id.content);
		et = (EditText) findViewById(R.id.content);
		btn = (Button) findViewById(R.id.send);
		TextView con =(TextView)findViewById(R.id.con);
		String s= getIntent().getExtras().getString("to_uid");
		if(s==null){
			con.setVisibility(View.VISIBLE);
			btn.setText(R.string.set_share);
			et.setHint(R.string.share_say_what);
		}
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String t = et.getText().toString();
				if (t != null && t.length() >= 1) {
					Intent i = getIntent();
					Bundle b = i.getExtras();
					b.putString("content", t);
					i.putExtras(b);
					String s = b.getString("content");
					setResult(20, i);
					WindowCommentActivity.this.finish();
				}

			}
		});
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub

		super.onDestroy();
	}

	public boolean onTouchEvent(android.view.MotionEvent event) {
		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
		setResult(-1, null);
		WindowCommentActivity.this.finish();
		return true;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_UP) {
			setResult(-1, null);
			WindowCommentActivity.this.finish();
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

}

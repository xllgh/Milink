package com.bandlink.air;

import java.util.Locale;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.TextView;

import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.WebViewHelper;

public class HelpActivity extends LovefitActivity {

	private TextView title;
	WebViewHelper help;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_webview);
		ActionbarSettings actionbar = new ActionbarSettings(this,
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						HelpActivity.this.finish();
					}
				}, null);
		actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);

		WebView webView = (WebView) findViewById(R.id.webView1);
		String lan = Locale.getDefault().getLanguage();
		actionbar.setTitle(R.string.set_about);
		if (lan.equals("zh")) {
			help = new WebViewHelper(this, webView,
					"file:///android_asset/help/h.htm");
		} else {
			help = new WebViewHelper(this, webView,
					"file:///android_asset/help/h-en.htm");
		}
		help.setBgTransparent();
		help.initWebview();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	public void btn_back(View view) {
		this.finish();

	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub
		
	}

}
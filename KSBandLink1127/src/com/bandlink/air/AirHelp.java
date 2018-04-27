package com.bandlink.air;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;

import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.WebViewHelper;

public class AirHelp extends LovefitActivity {
	WebViewHelper help;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.airhelp);
		ActionbarSettings actionbar = new ActionbarSettings(this, lsr, null);
		actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
		actionbar.setTitle(R.string.air_use_iv);

		WebView webView = (WebView) findViewById(R.id.webView1);
		webView.requestFocus();
		webView.getSettings().setDefaultTextEncodingName("utf-8");
		SharedPreferences sharePre = getSharedPreferences("air",
				MODE_MULTI_PROCESS);
		String url = "http://www.lovefit.com/air/help/air2help.html";

		help = new WebViewHelper(this, webView, url);
		// help.clearCache();
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

	private OnClickListener lsr = new OnClickListener() {

		@Override
		public void onClick(View v) {

			AirHelp.this.finish();
		}
	};

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub
		
	}
}

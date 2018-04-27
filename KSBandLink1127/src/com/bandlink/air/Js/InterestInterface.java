package com.bandlink.air.Js;

//import com.milink.android.lovewalk.CooperTestActivity;
import android.content.Context;
import android.content.Intent;
import android.webkit.WebView;
import android.widget.Toast;

import com.bandlink.air.GpsSportActivity;
import com.bandlink.air.R;

public class InterestInterface {
	private Context mcontext;
	private WebView webView;
	private Class[] activitys = new Class[] { GpsSportActivity.class };

	public InterestInterface(Context context, WebView webview) {
		mcontext = context;
		webView = webview;

	}

	public void LocationToActivity(String id) {
		int i = Integer.parseInt(id) - 1;
		if(i>=2){
			Toast.makeText(mcontext, mcontext.getString(R.string.opensoon), Toast.LENGTH_SHORT).show();
		}
		Intent intent = new Intent(mcontext, activitys[i]);
		intent.putExtra("flag", "fromslide");
		mcontext.startActivity(intent);
	}

}

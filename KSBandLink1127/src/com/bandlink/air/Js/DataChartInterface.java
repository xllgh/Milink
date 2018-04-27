package com.bandlink.air.Js;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebView;

@SuppressLint("NewApi")
public class DataChartInterface {
	private Context mcontext;
	private WebView webView;

	public DataChartInterface(Context context, WebView webview) {
		mcontext = context;
		webView = webview;

	}

}

package com.bandlink.air.util;

import java.util.LinkedList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.bandlink.air.MyLog;
import com.bandlink.air.R;

//所有webview 的初始化
public class WebViewHelper {
	private WebView webView;
	private Context mcontext;
	private String url;
	private LinkedList<String> jsurl;
	private boolean isComplete = false;
	
	public interface OnLoadUrl{
		public void onLoadUrl(WebView v,String url);
	}
	OnLoadUrl onLoadUrl;
	public WebViewHelper(Context con, WebView web, String u) {
		webView = web;
		mcontext = con;
		if (android.os.Build.VERSION.SDK_INT >= 11) {
			mcontext = new ContextThemeWrapper(con,
					android.R.style.Theme_Holo_Light);
		}
		url = u;
	}
	public WebViewHelper(Context con, WebView web, String u,OnLoadUrl onLoadUrl) {
		webView = web;
		mcontext = con;
		if (android.os.Build.VERSION.SDK_INT >= 11) {
			mcontext = new ContextThemeWrapper(con,
					android.R.style.Theme_Holo_Light);
		}
		url = u;
		this.onLoadUrl = onLoadUrl;
	}

	public void clearCache() {
		if (webView != null) {
			webView.clearCache(true);
		}
	}
 
	public void initWebview() {
		webView.loadUrl(url);
		WebSettings settings = webView.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setSupportZoom(true); 
		// 控制webview 自适应
		settings.setLoadWithOverviewMode(true);
		settings.setUseWideViewPort(true);
		webView.setInitialScale(1);
		//setLayerType在低版本不支持
		if (android.os.Build.VERSION.SDK_INT >= 11) {
			webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
		jsurl = new LinkedList<String>();
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				// TODO Auto-generated method stub
				isComplete = true;
				if (jsurl != null || jsurl.size() > 0) {
					for (int i = 0; i < jsurl.size(); i++) {
						if (Build.VERSION.SDK_INT >= 19) {
							// webView.evaluateJavascript(jsurl[i], null);
							webView.loadUrl("javascript:" + jsurl.get(i));
						} else {
							webView.loadUrl("javascript:" + jsurl.get(i));
						}
					}
					jsurl.clear();
				}
				super.onPageFinished(view, url);

			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// TODO Auto-generated method stub
				view.loadUrl(url);
				if(onLoadUrl!=null){
					onLoadUrl.onLoadUrl(view, url);
				}
				return true;
			}

		});
		webView.setWebChromeClient(new WebChromeClient() {

			public void onConsoleMessage(String message, int lineNumber,
					String sourceID) {
				MyLog.d("MyApplication", message + " -- From line " + lineNumber
						+ " of " + sourceID);

			}

			@Override
			public boolean onJsAlert(WebView view, String url, String message,
					final JsResult result) {
				// TODO Auto-generated method stub
				AlertDialog.Builder builder = new AlertDialog.Builder(mcontext);
				builder.setMessage(message);
				builder.setPositiveButton(R.string.alert_ok,
						new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								result.cancel();

							}
						});
				AlertDialog dialog = builder.create();
				dialog.show();
				return true;
			}

		});
	}

	// 设置webView 的背景透明
	public void setBgTransparent() {
		webView.setBackgroundColor(0);
	}

	public void addJavascriptInterface(Object object, String name) {
		webView.addJavascriptInterface(object, name);
	}

	// 刷新
	public void reload() {
		webView.reload();
	}

	public void loadUrls(String[] js) {

		if (js.length > 0) {
			for (int i = 0; i < js.length; i++) {
				if (Build.VERSION.SDK_INT >= 19) {
					// webView.evaluateJavascript(jsurl[i], null);
					if (isComplete) {
						webView.loadUrl("javascript:" + js[i]);
					} else {
						jsurl.add(js[i]);
						// TODO: handle exception
					}

				} else {

					if (isComplete) {
						webView.loadUrl("javascript:" + js[i]);
					} else {
						jsurl.add(js[i]);
					}
				}
			}
		}
	}
}

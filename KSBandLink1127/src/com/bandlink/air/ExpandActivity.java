package com.bandlink.air;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.MainInterface;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.WebViewHelper;

public class ExpandActivity extends Fragment {

	public WebView webview;

	private View mView;
	private MainInterface interf;
	public ActionbarSettings action;
	private int uid;
	ProgressBar progress;

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		interf = (MainInterface) activity;
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mView = inflater.inflate(R.layout.expand, null);
		action = new ActionbarSettings(mView, interf, this);
		progress = (ProgressBar) mView.findViewById(R.id.progress);
		action.setTitle(getResources().getStringArray(R.array.menu)[3]);
		webview = (WebView) mView.findViewById(R.id.web);
		Calendar c = Calendar.getInstance();
		int weekday = c.get(Calendar.DAY_OF_WEEK) - 1;
		if (weekday == 0) {
			weekday = 7;
		}
		SimpleDateFormat sd = new SimpleDateFormat("yyyy.MM.dd");
		String str = "";
		c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH),
				c.get(Calendar.DAY_OF_MONTH) - weekday + 1);
		str += sd.format(c.getTime()) + "-";
		c = Calendar.getInstance();
		c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH),
				c.get(Calendar.DAY_OF_MONTH) + 7 - weekday);
		str += sd.format(c.getTime());
		SharedPreferences preferences = getActivity().getSharedPreferences(
				SharePreUtils.APP_ACTION, Context.MODE_PRIVATE);
		uid = preferences.getInt("UID", -1);

		WebViewHelper help = new WebViewHelper(getActivity(), webview,
				"http://air.lovefit.com/index.php/home/expand/expand/date/"
						+ str + "/uid/" + uid, new WebViewHelper.OnLoadUrl() {

					@Override
					public void onLoadUrl(WebView v, String url) {
						// TODO Auto-generated method stub
						if (url.contains("report.html")) {
							action.setTopLeftIcon(R.drawable.ic_top_arrow);
						} else {
							action.setTopLeftIcon(R.drawable.ic_top_left);
						}
					}

				});
		help.initWebview();
		webview.setWebChromeClient(new WebChromeClient());
		webview.setWebViewClient(new MWebViewClient());
		webview.getSettings().setRenderPriority(RenderPriority.HIGH);
		
		return mView;
	}

	public class MWebViewClient extends android.webkit.WebViewClient {
		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			// TODO Auto-generated method stub
			getActivity().runOnUiThread(new Runnable() {
				public void run() {
					webview.setVisibility(View.GONE);
					TextView tx = (TextView) mView.findViewById(R.id.txt);
					tx.setText(R.string.load_error);
					tx.setVisibility(View.VISIBLE);
				}
			});
			super.onReceivedError(view, errorCode, description, failingUrl);
		}

		@Override
		public void onLoadResource(WebView view, String url) {
			// TODO Auto-generated method stub
			if (url.contains("expand/expand")) {
				action.setTopLeftIcon(R.drawable.ic_top_left);
			} else {
				action.setTopLeftIcon(R.drawable.ic_top_arrow);
			}
			super.onLoadResource(view, url);
		}
	}

	public class WebChromeClient extends android.webkit.WebChromeClient {
		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			if (newProgress == 100) {
				progress.setVisibility(View.INVISIBLE);

				// progress.setProgress(50);
			} else {
				progress.setVisibility(View.VISIBLE);
				progress.setProgress(newProgress);
			}
			super.onProgressChanged(view, newProgress);
		}

	}
}

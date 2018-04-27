package com.bandlink.air;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;

import com.bandlink.air.Js.DataChartInterface;
import com.bandlink.air.util.WebViewHelper;

public class DataChartDialogFragment extends DialogFragment {
	private static String xValues;
	private static String yValues;
	private static String tip;
	private static int interval;
    private static Object target;
	static DataChartDialogFragment newInstance(String x, String y,Object tar, String t,
			int i) {
		DataChartDialogFragment f = new DataChartDialogFragment();
		xValues = x;
		yValues = y;
		tip = t;
		interval = i;
		target=tar;
		return f;
	}
	
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.chartdialog_frame, container, false);
		Button button = (Button) v.findViewById(R.id.show);
		WebView webView = (WebView) v.findViewById(R.id.webview);
		WebSettings settings = webView.getSettings();
		WebViewHelper help = new WebViewHelper(getActivity(), webView,
				"file:///android_asset/chart/high.html");
		help.initWebview();
		help.setBgTransparent();
		DataChartInterface js = new DataChartInterface(getActivity(), webView);
		help.addJavascriptInterface(js, "Android");
		help.loadUrls(new String[] { "showStepChart('" + xValues + "','"
				+ yValues + "',"+target+",'"+tip+"',"+interval+")","zoomChart()" });
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// When button is clicked, call up to owning activity.
				FragmentTransaction tr = getActivity()
						.getSupportFragmentManager().beginTransaction();
				Fragment fragment = getActivity().getSupportFragmentManager()
						.findFragmentByTag("chart_dialog");
				tr.remove(fragment);
				tr.commit();

			}
		});

		return v;

	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		return dialog;
	}

}

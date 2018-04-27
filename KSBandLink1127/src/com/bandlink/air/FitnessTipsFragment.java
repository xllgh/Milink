	package com.bandlink.air;

	import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.bandlink.air.Js.InterestInterface;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.MainInterface;
import com.bandlink.air.util.WebViewHelper;

	public class FitnessTipsFragment extends Fragment {
		private MainInterface minterface;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			// TODO Auto-generated method stub
			View contentView = inflater.inflate(R.layout.frg_frame, null);
			ActionbarSettings actionbar = new ActionbarSettings(contentView,
					minterface);
			actionbar.setTopRightIcon(R.drawable.ic_top_more);
			actionbar.setTitle(R.string.ab_fitness);
			WebView web = (WebView) contentView.findViewById(R.id.web);
			WebViewHelper help = new WebViewHelper(getActivity(), web,"file:///android_asset/sell/2.html");
			help.initWebview();
			help.setBgTransparent();
			InterestInterface js = new InterestInterface(getActivity(), web);
			help.addJavascriptInterface(js, "Android");
			return contentView;
		}

		@Override
		public void onAttach(Activity activity) {
			// TODO Auto-generated method stub
			minterface = (MainInterface) activity;
			super.onAttach(activity);
		}

	}

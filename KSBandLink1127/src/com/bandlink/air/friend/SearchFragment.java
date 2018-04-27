package com.bandlink.air.friend;

import java.io.File;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.TabHost;

import com.bandlink.air.R;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.utils.StorageUtils;

public class SearchFragment extends Fragment implements OnCheckedChangeListener {
	TabHost tabHost;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View v = getActivity().getLayoutInflater().inflate(
				R.layout.recommendfrg, container, false);
		tabHost = (TabHost) v.findViewById(android.R.id.tabhost);
		tabHost.setup();

		tabHost.addTab(tabHost.newTabSpec("tab11").setIndicator("")
				.setContent(R.id.tab11));
		tabHost.addTab(tabHost.newTabSpec("tab22").setIndicator("")
				.setContent(R.id.tab22));
		tabHost.addTab(tabHost.newTabSpec("tab33").setIndicator("")
				.setContent(R.id.tab33));
		((RadioButton) v.findViewById(R.id.radio_button0))
				.setOnCheckedChangeListener(this);
		((RadioButton) v.findViewById(R.id.radio_button1))
				.setOnCheckedChangeListener(this);
		((RadioButton) v.findViewById(R.id.radio_button2))
				.setOnCheckedChangeListener(this);
		File cacheDir = StorageUtils.getOwnCacheDirectory(getActivity(),
				"UniversalImageLoader/Cache");
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				getActivity()).threadPriority(Thread.NORM_PRIORITY - 1)
				.denyCacheImageMultipleSizesInMemory()
				.memoryCache(new UsingFreqLimitedMemoryCache(1024 * 1024 * 2))
				.threadPoolSize(5).imageDecoder(new BaseImageDecoder(true))
				// You can pass your own memory cache implementation
				.discCache(new UnlimitedDiscCache(cacheDir))
				// You can pass your own disc cache implementation
				.defaultDisplayImageOptions(DisplayImageOptions.createSimple())
				.build();
		ImageLoader.getInstance().init(config);
		return v;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		if (isChecked) {
			switch (buttonView.getId()) {
			case R.id.radio_button0:
				this.tabHost.setCurrentTabByTag("tab11");
				break;
			case R.id.radio_button1:
				this.tabHost.setCurrentTabByTag("tab22");
				break;
			case R.id.radio_button2:
				this.tabHost.setCurrentTabByTag("tab33");
				break;
			}
		}
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		Fragment f1 = getFragmentManager().findFragmentById(R.id.tab11);
		Fragment f2 = getFragmentManager().findFragmentById(R.id.tab22);
		Fragment f3 = getFragmentManager().findFragmentById(R.id.tab33);
		if (f1 != null) {
			getFragmentManager().beginTransaction().remove(f1).commit();
		}
		if (f2 != null) {
			getFragmentManager().beginTransaction().remove(f2).commit();
		}
		if (f3 != null) {
			getFragmentManager().beginTransaction().remove(f3).commit();
		}
	}

}

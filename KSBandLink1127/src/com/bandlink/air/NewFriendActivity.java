package com.bandlink.air;

import java.io.File;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.TabHost;

import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.LovefitFragmentActivity;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.utils.StorageUtils;

public class NewFriendActivity extends LovefitFragmentActivity implements
		OnCheckedChangeListener {
	TabHost tabHost;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.newfriend_activity);
		tabHost = (TabHost) findViewById(android.R.id.tabhost);
		ActionbarSettings actionbar = new ActionbarSettings(this, back, null);
		actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
		// actionbar.setTopRightIcon(R.drawable.ic_top_more);
		actionbar.setTitle(R.string.addnewfriend);
		tabHost.setup();

		tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator("")
				.setContent(R.id.tab1));
		tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator("")
				.setContent(R.id.tab2));
		tabHost.addTab(tabHost.newTabSpec("tab3").setIndicator("")
				.setContent(R.id.tab3));
		((RadioButton) findViewById(R.id.radio_button0))
				.setOnCheckedChangeListener(this);
		((RadioButton) findViewById(R.id.radio_button1))
				.setOnCheckedChangeListener(this);
		((RadioButton) findViewById(R.id.radio_button2))
				.setOnCheckedChangeListener(this);
		File cacheDir = StorageUtils.getOwnCacheDirectory(this,
				"UniversalImageLoader/Cache");
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				this).threadPriority(Thread.NORM_PRIORITY - 1)
				.denyCacheImageMultipleSizesInMemory()
				.memoryCache(new UsingFreqLimitedMemoryCache(1024 * 1024 * 2))
				.threadPoolSize(5).imageDecoder(new BaseImageDecoder(true))
				// You can pass your own memory cache implementation
				.discCache(new UnlimitedDiscCache(cacheDir))
				// You can pass your own disc cache implementation
				.defaultDisplayImageOptions(DisplayImageOptions.createSimple())
				.build();
		ImageLoader.getInstance().init(config);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		if (isChecked) {
			switch (buttonView.getId()) {
			case R.id.radio_button0:
				this.tabHost.setCurrentTabByTag("tab1");
				break;
			case R.id.radio_button1:
				this.tabHost.setCurrentTabByTag("tab2");
				break;
			case R.id.radio_button2:
				this.tabHost.setCurrentTabByTag("tab3");
				break;
			}
		}
	}

	private OnClickListener back = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			NewFriendActivity.this.finish();
		}
	};

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub
		
	}

}

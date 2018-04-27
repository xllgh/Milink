package com.bandlink.air.util;

import java.util.ArrayList;
import java.util.Collections;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bandlink.air.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
 

public class ImagePagerActivity extends LovefitActivity {

	private static final String STATE_POSITION = "STATE_POSITION";
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options;
	ViewPager pager;
	private ArrayList<String> imag;
	private ArrayList<String> paths;
	private CheckBox checkbox;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_image_pager);

		Bundle bundle = getIntent().getExtras();
		imag = bundle.getStringArrayList("data");// 鍘熷璺緞闆嗗悎
		Collections.reverse(imag);
		paths = bundle.getStringArrayList("selected");
		checkbox = (CheckBox) findViewById(R.id.imgQueueMultiSelected);

		// 褰撳墠鏄剧ずView鐨勪綅缃�
		int pagerPosition = bundle.getInt("index", 0);

		// 濡傛灉涔嬪墠鏈変繚瀛樼敤鎴锋暟鎹�
		if (savedInstanceState != null) {
			pagerPosition = savedInstanceState.getInt(STATE_POSITION);
		}

		options = new DisplayImageOptions.Builder()
				.showImageForEmptyUri(R.drawable.no_media)
				.showImageOnFail(R.drawable.no_media)
				.resetViewBeforeLoading(true).cacheOnDisc(true)
				.imageScaleType(ImageScaleType.EXACTLY)
				.bitmapConfig(Bitmap.Config.RGB_565)
				.displayer(new FadeInBitmapDisplayer(300)).build();

		pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(new ImagePagerAdapter(imag));
		pager.setCurrentItem(pagerPosition); // 鏄剧ず褰撳墠浣嶇疆鐨刅iew
		ActionbarSettings actionbar = new ActionbarSettings(this, lsl, null);
		actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);		
		actionbar.setTitle(R.string.pic);

	}

	private View.OnClickListener lsl = new View.OnClickListener() {

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(ImagePagerActivity.this,
					ImageGalleryActivity.class);
			intent.putExtra("paths", paths);
			setResult(RESULT_OK, intent);
			ImagePagerActivity.this.finish();

		}
	};	

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// 淇濆瓨鐢ㄦ埛鏁版嵁
		outState.putInt(STATE_POSITION, pager.getCurrentItem());
	}

	private void checkSelected(int item) {
		String p = imag.get(pager.getCurrentItem());
		if (paths.contains(p)) {
			checkbox.setChecked(true);
		} else {
			checkbox.setChecked(false);
		}
	}

	public void imageSelect(View view) {
		String p = imag.get(pager.getCurrentItem());
		if (checkbox.isChecked()) {
			paths.add(p);
		} else {
			paths.add(p);
		}

	}

	private class ImagePagerAdapter extends PagerAdapter {

		private ArrayList<String> images;
		private LayoutInflater inflater;

		ImagePagerAdapter(ArrayList<String> images) {
			this.images = images;
			inflater = getLayoutInflater();
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager) container).removeView((View) object);
		}

		@Override
		public void finishUpdate(View container) {
		}

		@Override
		public int getCount() {
			return images.size();
		}

		@Override
		public Object instantiateItem(ViewGroup view, int position) {
			checkSelected(position);
			View imageLayout = inflater.inflate(R.layout.item_pager_image,
					view, false);
			ImageView imageView = (ImageView) imageLayout
					.findViewById(R.id.image);
			final ProgressBar spinner = (ProgressBar) imageLayout
					.findViewById(R.id.loading);
			
			imageLoader.displayImage("file://" + images.get(position),
					imageView, options, new SimpleImageLoadingListener() {
						@Override
						public void onLoadingStarted(String imageUri, View view) {
							spinner.setVisibility(View.VISIBLE);
						}

						@Override
						public void onLoadingFailed(String imageUri, View view,
								FailReason failReason) {
							String message = null;
							switch (failReason.getType()) { // 鑾峰彇鍥剧墖澶辫触绫诲瀷
							case IO_ERROR: // 鏂囦欢I/O閿欒
								message = "Input/Output error";
								break;
							case DECODING_ERROR: // 瑙ｇ爜閿欒
								message = "Image can't be decoded";
								break;
							case NETWORK_DENIED: // 缃戠粶寤惰繜
								message = "Downloads are denied";
								break;
							case OUT_OF_MEMORY: // 鍐呭瓨涓嶈冻
								message = "Out Of Memory error";
								break;
							case UNKNOWN: // 鍘熷洜涓嶆槑
								message = "Unknown error";
								break;
							}
							Toast.makeText(ImagePagerActivity.this, message,
									Toast.LENGTH_SHORT).show();

							spinner.setVisibility(View.GONE);
						}

						@Override
						public void onLoadingComplete(String imageUri,
								View view, Bitmap loadedImage) {
							spinner.setVisibility(View.GONE); // 涓嶆樉绀哄渾褰㈣繘搴︽潯
						}
					});

			((ViewPager) view).addView(imageLayout, 0); // 灏嗗浘鐗囧鍔犲埌ViewPager

			return imageLayout;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}

		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View container) {
		}
	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub
		
	}
}
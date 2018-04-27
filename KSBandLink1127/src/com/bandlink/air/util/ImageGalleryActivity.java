package com.bandlink.air.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.bandlink.air.R;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.nostra13.universalimageloader.utils.StorageUtils;

public class ImageGalleryActivity extends LovefitActivity {

	GridView gridGallery;
	Handler handler;
	GalleryAdapter adapter;

	ImageView imgNoMedia;

	private ImageLoader imageLoader;

	ArrayList<String> alist; // 点击的文件夹内图片路径集合
	String flag ="";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.photo_gridview);
		ActionbarSettings actionbar = new ActionbarSettings(this,
				new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						ImageGalleryActivity.this.finish();
					}
				}, rightlistener);

		actionbar.setTitle(R.string.pic);
		actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
		actionbar.setTopRightIcon(R.drawable.complete);
		Intent i = getIntent();
		alist = i.getExtras().getStringArrayList("data");
		flag = i.getExtras().getString("flag");
		initImageLoader();
		init();

	}

	private View.OnClickListener rightlistener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {

			ArrayList<CustomGallery> selected = adapter.getSelected();

			String[] allPath = new String[selected.size()];
			for (int i = 0; i < allPath.length; i++) {
				allPath[i] = selected.get(i).sdcardPath;
			}
			Intent data = new Intent(ImageGalleryActivity.this,
					ImageFolderActivity.class);
			data.putExtra("all_path", allPath);
			data.putExtra("flag", flag);
			setResult(RESULT_OK, data);
			finish();

		}
	};

	private void initImageLoader() {
		try {
			String CACHE_DIR = Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/.temp_tmp";
			new File(CACHE_DIR).mkdirs();

			File cacheDir = StorageUtils.getOwnCacheDirectory(getBaseContext(),
					CACHE_DIR);

			@SuppressWarnings("deprecation")
			DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
					.cacheInMemory().imageScaleType(ImageScaleType.EXACTLY)
					.bitmapConfig(Bitmap.Config.ALPHA_8).build();
			ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(
					getBaseContext())
					.defaultDisplayImageOptions(defaultOptions)
					.discCache(new UnlimitedDiscCache(cacheDir))
					.memoryCache(new WeakMemoryCache());

			ImageLoaderConfiguration config = builder.build();
			imageLoader = ImageLoader.getInstance();
			imageLoader.init(config);

		} catch (Exception e) {

		}
	}

	private void init() {

		handler = new Handler();
		gridGallery = (GridView) findViewById(R.id.gridGallery);
		gridGallery.setFastScrollEnabled(true);
		adapter = new GalleryAdapter(getApplicationContext(), imageLoader);
		PauseOnScrollListener listener = new PauseOnScrollListener(imageLoader,
				true, true);
		gridGallery.setOnScrollListener(listener);

		gridGallery.setOnItemClickListener(mItemMulClickListener);
		gridGallery.setAdapter(adapter);
		imgNoMedia = (ImageView) findViewById(R.id.imgNoMedia);

		new Thread() {

			@Override
			public void run() {
				Looper.prepare();
				handler.post(new Runnable() {

					@Override
					public void run() {
						adapter.addAll(getGalleryPhotos());
						checkImageStatus();
					}
				});
				Looper.loop();
			};

		}.start();

	}

	private void checkImageStatus() {
		if (adapter.isEmpty()) {
			imgNoMedia.setVisibility(View.VISIBLE);
		} else {
			imgNoMedia.setVisibility(View.GONE);
		}
	}

	AdapterView.OnItemClickListener mItemMulClickListener = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> l, View v, int position, long id) {
			Intent i = new Intent(ImageGalleryActivity.this,
					ImagePagerActivity.class);
			Bundle d = new Bundle();
			d.putInt("index", position);
			d.putStringArrayList("data", alist);
			ArrayList<CustomGallery> selected = adapter.getSelected();

			ArrayList<String> allPath = new ArrayList<String>();
			for (int j = 0; j < selected.size(); j++) {
				allPath.add(selected.get(j).sdcardPath);
			}
			d.putStringArrayList("selected", allPath);
			i.putExtras(d);
			
			startActivityForResult(i, 1);

		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == 1 && resultCode == RESULT_OK) {
			new Thread() {

				@Override
				public void run() {
					Looper.prepare();
					handler.post(new Runnable() {

						@Override
						public void run() {
							ArrayList<CustomGallery> galleryList = new ArrayList<CustomGallery>();
							 ArrayList<String> paths = data.getExtras()
									.getStringArrayList("paths");
							for (int i = 0; i < alist.size(); i++) {
								CustomGallery item = new CustomGallery();
								item.sdcardPath = alist.get(i);
								for (int j = 0; j < paths.size(); j++) {
									if(alist.get(i).equals(paths.get(j))){
										item.isSeleted=true;
									}
								}
								
								galleryList.add(item);
							}

							Collections.reverse(galleryList);

							adapter.addAll(galleryList);
							checkImageStatus();
						}
					});
					Looper.loop();
				};

			}.start();

		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private ArrayList<CustomGallery> getGalleryPhotos() {
		ArrayList<CustomGallery> galleryList = new ArrayList<CustomGallery>();

		for (String str : alist) {
			CustomGallery item = new CustomGallery();
			item.sdcardPath = str;
			galleryList.add(item);

		}

		// show newest photo at beginning of the list
		Collections.reverse(galleryList);

		return galleryList;
	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub
		
	}

}

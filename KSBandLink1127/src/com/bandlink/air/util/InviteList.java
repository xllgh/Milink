package com.bandlink.air.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bandlink.air.CreateMatchStep3;
import com.bandlink.air.R;
import com.bandlink.air.friend.User;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.nostra13.universalimageloader.utils.StorageUtils;

public class InviteList extends Activity {
	static DisplayImageOptions defaultOptions;
	GridView gridGallery;
	Handler handler;
	PersonAdapter adapter;

	ImageView imgNoMedia;

	private ImageLoader imageLoader;

	ArrayList<User> alist; // 点击的文件夹内图片路径集合
	String flag = "";

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.photo_gridview);
		ActionbarSettings actionbar = new ActionbarSettings(this,
				new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						InviteList.this.finish();
					}
				}, rightlistener);

		actionbar.setTitle(R.string.invitefriend);
		actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
		actionbar.setTopRightIcon(R.drawable.complete);
		Intent i = getIntent();
		alist = i.getParcelableArrayListExtra("data");
		flag = i.getExtras().getString("flag");
		initImageLoader();
		init();

	}

	private View.OnClickListener rightlistener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {

			ArrayList<CustomGallery> selected = adapter.getSelected();
			ArrayList<User> user = new ArrayList<User>();
			int[] allPath = new int[selected.size()];
			for (int i = 0; i < allPath.length; i++) {
				allPath[i] = selected.get(i).id;
				user.add(new User(selected.get(i).id, selected.get(i).name,
						selected.get(i).sdcardPath));
			}
			Intent data = new Intent(InviteList.this, CreateMatchStep3.class);
			data.putExtra("ids", allPath);
			data.putParcelableArrayListExtra("user", user);
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

			defaultOptions = new DisplayImageOptions.Builder().cacheInMemory()
					.imageScaleType(ImageScaleType.EXACTLY)
					.bitmapConfig(Bitmap.Config.ALPHA_8)
					.displayer(new RoundedBitmapDisplayer(145)).build();
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
		adapter = new PersonAdapter(getApplicationContext(), imageLoader);
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
			((PersonAdapter) l.getAdapter()).changeSelection(
					v.findViewById(R.id.imgQueueMultiSelected), position);
		}
	};

	private ArrayList<CustomGallery> getGalleryPhotos() {
		ArrayList<CustomGallery> galleryList = new ArrayList<CustomGallery>();

		for (User str : alist) {
			CustomGallery item = new CustomGallery();
			item.id = str.uid; // sdcardPath : uid
			item.sdcardPath = str.photo;
			item.name = str.name;
			galleryList.add(item);

		}

		// show newest photo at beginning of the list
		Collections.reverse(galleryList);

		return galleryList;
	}

	public static class PersonAdapter extends BaseAdapter {

		private LayoutInflater infalter;
		private ArrayList<CustomGallery> data = new ArrayList<CustomGallery>();
		boolean gone;
		ImageLoader imageLoader;

		public PersonAdapter(Context c, ImageLoader imageLoader) {
			infalter = (LayoutInflater) c
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			this.imageLoader = imageLoader;
			gone = false;
			// clearCache();
		}

		public PersonAdapter(Context c, ImageLoader imageLoader,
				ArrayList<CustomGallery> lis) {
			infalter = (LayoutInflater) c
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			data = lis;
			this.imageLoader = imageLoader;
			gone = true;
			// clearCache();
		}

		@Override
		public int getCount() {
			return data.size();
		}

		public ArrayList<CustomGallery> getAll() {
			return data;
		}

		@Override
		public CustomGallery getItem(int position) {
			return data.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		public void selectAll(boolean selection) {
			for (int i = 0; i < data.size(); i++) {
				data.get(i).isSeleted = selection;

			}
			notifyDataSetChanged();
		}

		public boolean isAllSelected() {
			boolean isAllSelected = true;

			for (int i = 0; i < data.size(); i++) {
				if (!data.get(i).isSeleted) {
					isAllSelected = false;
					break;
				}
			}

			return isAllSelected;
		}

		public boolean isAnySelected() {
			boolean isAnySelected = false;

			for (int i = 0; i < data.size(); i++) {
				if (data.get(i).isSeleted) {
					isAnySelected = true;
					break;
				}
			}

			return isAnySelected;
		}

		public ArrayList<CustomGallery> getSelected() {
			ArrayList<CustomGallery> dataT = new ArrayList<CustomGallery>();

			for (int i = 0; i < data.size(); i++) {
				if (data.get(i).isSeleted) {
					dataT.add(data.get(i));
				}
			}

			return dataT;
		}

		public void addAll(ArrayList<CustomGallery> files) {

			try {
				this.data.clear();
				this.data.addAll(files);

			} catch (Exception e) {
				e.printStackTrace();
			}

			notifyDataSetChanged();
		}

		public void changeSelection(View v, int position) {

			if (data.get(position).isSeleted) {
				data.get(position).isSeleted = false;
			} else {
				data.get(position).isSeleted = true;
			}
			try {
				((ViewHolder) v.getTag()).imgQueueMultiSelected
						.setSelected(data.get(position).isSeleted);
			} catch (Exception e) {
				v.setSelected(data.get(position).isSeleted);
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			final ViewHolder holder;
			if (convertView == null) {
				convertView = infalter.inflate(
						R.layout.invite_friend_list_item, null);
				holder = new ViewHolder();
				holder.imgQueue = (ImageView) convertView
						.findViewById(R.id.imgQueue);
				holder.imgQueueMultiSelected = (ImageView) convertView
						.findViewById(R.id.imgQueueMultiSelected);
				holder.name = (TextView) convertView.findViewById(R.id.name);
				holder.imgQueueMultiSelected.setVisibility(View.VISIBLE);

				convertView.setTag(holder);

			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.imgQueue.setTag(position);
			if (gone) {
				holder.imgQueueMultiSelected.setVisibility(View.GONE);
			}
			holder.name.setText(data.get(position).name);
			try {

				imageLoader.displayImage(data.get(position).sdcardPath,
						holder.imgQueue, defaultOptions);
				holder.imgQueueMultiSelected
						.setOnClickListener(new lvButtonListener(convertView,
								position, holder));
				holder.imgQueueMultiSelected
						.setSelected(data.get(position).isSeleted);

			} catch (Exception e) {
				e.printStackTrace();
			}

			return convertView;
		}

		class lvButtonListener implements OnClickListener {
			private int position;
			ViewHolder holder;
			View convertView;

			lvButtonListener(View convertView, int pos, ViewHolder holder) {
				position = pos;
				this.holder = holder;
				this.convertView = convertView;
			}

			@Override
			public void onClick(View v) {
				int vid = v.getId();
				if (vid == holder.imgQueueMultiSelected.getId()) {
					changeSelection(convertView, position);
				}
			}
		}

		public class ViewHolder {
			ImageView imgQueue;
			ImageView imgQueueMultiSelected;
			TextView name;
		}

		public void clearCache() {
			imageLoader.clearDiscCache();
			imageLoader.clearMemoryCache();
		}

		public void clear() {
			data.clear();
			notifyDataSetChanged();
		}
	}
}

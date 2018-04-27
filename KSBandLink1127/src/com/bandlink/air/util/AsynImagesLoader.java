package com.bandlink.air.util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.ImageView;

import com.bandlink.air.MyLog;
import com.bandlink.air.R;

public class AsynImagesLoader {
	MemoryCache memory = new MemoryCache();
	FileCache filecache;
	private Map<ImageView, String> imageViews = Collections
			.synchronizedMap(new HashMap<ImageView, String>());

	ExecutorService executorService;
	private Activity myactiviy;
	private boolean clearFlag;
	private static AsynImagesLoader loader;
	
	public static synchronized  AsynImagesLoader getInstance(Context context, boolean clear) {

		if (loader == null) {

			loader = new AsynImagesLoader(context, clear);

		}
		return loader;
	}
	
	private AsynImagesLoader(Context context, boolean clear) {
		filecache = new FileCache(context);
		executorService = Executors.newFixedThreadPool(5);
		myactiviy = (Activity) context;
		clearFlag = clear&&detect();
	}

	public  boolean detect() {

		ConnectivityManager manager = (ConnectivityManager) myactiviy
				.getApplicationContext().getSystemService(
						Context.CONNECTIVITY_SERVICE);

		if (manager == null) {
			return false;
		}

		NetworkInfo networkinfo = manager.getActiveNetworkInfo();

		if (networkinfo == null || !networkinfo.isAvailable()) {
			return false;
		}

		return true;
	}

	final int stub_id = R.drawable.avatar;

	public void DisplayImage(String url, ImageView imageview) {
		// url="http://192.168.9.33/ucenter/data/avatar/"+url;
		url = "http://www.lovefit.com/ucenter/data/avatar/" + url;
		imageViews.put(imageview, url);
		Bitmap bitmap = memory.get(url);
		if (bitmap != null) {
			imageview.setImageBitmap(bitmap);
		} else {
			queuePhoto(url, imageview);
		}
	}
	
	public void DisplayImageMatch(String url, ImageView imageview) {
		// url="http://192.168.9.33/ucenter/data/avatar/"+url;
		imageViews.put(imageview, url);
		Bitmap bitmap = memory.get(url);
		if (bitmap != null) {
			imageview.setImageBitmap(bitmap);
		} else {
			queuePhoto(url, imageview);
		}
	}
	
	public void DisplayImageClub(String url, ImageView imageview) {
		// url="http://192.168.9.33/ucenter/data/avatar/"+url;
		imageViews.put(imageview, url);
		Bitmap bitmap = memory.get(url);
		if (bitmap != null) {
			imageview.setImageBitmap(bitmap);
		} else {
			queuePhoto(url, imageview);
		}
	}
	
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {
		if (bitmap == null) {
			return null;
		}

		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return output;
	}

	public void queuePhoto(String url, ImageView imageview) {
		PhotoToLoad p = new PhotoToLoad(url, imageview);
		executorService.submit(new PhotoLoader(p));
	}

	private class PhotoToLoad {
		private String url;
		private ImageView imageview;

		public PhotoToLoad(String u, ImageView i) {
			url = u;
			imageview = i;
		}
	}

	class PhotoLoader implements Runnable {
		private PhotoToLoad photo;

		public PhotoLoader(PhotoToLoad p) {
			photo = p;
		}

		public void run() {
			if (imageViewReuse(photo))
				return;
			if (clearFlag) {
				filecache.clearSingleFile(photo.url);
			}
			Bitmap bitmap = getBitmap(photo.url);
			memory.put(photo.url, bitmap);
			if (imageViewReuse(photo))
				return;
			BitmapDisplayer bd = new BitmapDisplayer(bitmap, photo);
			try {
				// Activity a = (Activity) photo.imageview.getContext();
				myactiviy.runOnUiThread(bd);
			} catch (Exception e) {
				// TODO: handle exception
				MyLog.e("", e.toString());
			}

		}
	}

	private Bitmap getBitmap(String url) {
		File f = filecache.getFile(url);

		// from SD cache
		Bitmap b = decodeFile(f);
		if (b != null)
			return b;

		try {
			Bitmap bitmap = null;
			URL imageurl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) imageurl
					.openConnection();
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			conn.setInstanceFollowRedirects(true);
			InputStream in = conn.getInputStream();
			OutputStream os = new FileOutputStream(f);
			CopyStream(in, os);
			os.close();
			bitmap = decodeFile(f);
			return bitmap;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	public static void CopyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
		}
	}

	private Bitmap decodeFile(File file) {
		try {
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;

			Bitmap bitmap = BitmapFactory.decodeStream(
					new FileInputStream(file), null, o);
			final int REQUIRED_SIZE = 70;
			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 1;

			while (true) {
				if (width_tmp / 2 < REQUIRED_SIZE
						|| height_tmp / 2 < REQUIRED_SIZE)
					break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			Bitmap round = BitmapFactory.decodeStream(
					new FileInputStream(file), null, o2);
			return getRoundedCornerBitmap(round, 0f);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	// Used to display bitmap in the UI thread
	class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		PhotoToLoad photo;

		public BitmapDisplayer(Bitmap bmp, PhotoToLoad p) {
			photo = p;
			bitmap = bmp;
		}

		public void run() {
			if (imageViewReuse(photo))
				return;
			if (bitmap != null) {
				photo.imageview.setImageBitmap(bitmap);
			} else {
				photo.imageview.setImageResource(stub_id);
			}
		}
	}

	public boolean imageViewReuse(PhotoToLoad photo) {
		String tag = imageViews.get(photo.imageview);
		if (tag == null || !tag.equals(photo.url))
			return true;
		return false;
	}

	public void clearCache() {
		memory.clear();
		filecache.clear();
	}

	public void clearMemory() {
		memory.clear();
	}

}

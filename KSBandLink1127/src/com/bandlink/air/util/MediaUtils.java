package com.bandlink.air.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Toast;

import com.bandlink.air.R;


public class MediaUtils {
	private String picpath;
	private File picfile;
	private Context mcontext;
	private Activity activity;

	public MediaUtils(Context context) {
		mcontext = context;
		picfile = mcontext.getFilesDir();
		picpath = picfile.getPath() + "/";
		activity = (Activity) mcontext;
	}

	public File getPicPath(String dir, String filename) {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {

			File sdcardFile = new File(
					Environment.getExternalStorageDirectory(), dir);
			if (!sdcardFile.exists()) {
				sdcardFile.mkdirs();
			}
			picfile = new File(sdcardFile.getPath() + "/" + filename);
		} else {
			Toast.makeText(mcontext, R.string.nosd, Toast.LENGTH_LONG).show();
		}
		return picfile;
	}

	public File getFilePath(String dir, String filename) {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {

			File sdcardFile = new File(
					Environment.getExternalStorageDirectory(), dir);
			if (!sdcardFile.exists()) {
				sdcardFile.mkdirs();
			}
			picfile = new File(sdcardFile.getPath() + "/" + filename);
		} else {
			Toast.makeText(mcontext, R.string.nosd, Toast.LENGTH_LONG).show();
		}
		return picfile;
	}

	public void invokeCamera(File path, int requestCode) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(path));
		activity.startActivityForResult(intent, requestCode);
	}

	public void invokeCamera(int requestCode) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		activity.startActivityForResult(intent, requestCode);
	}

	public File startPhotoZoom(Uri uri, int sizeX, int sizeY, int requestCode) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		// 设置裁剪
		intent.putExtra("crop", "true");
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY 是裁剪图片宽高
		intent.putExtra("outputX", sizeX);
		intent.putExtra("outputY", sizeY);
		intent.putExtra("return-data", true);

		intent.putExtra(MediaStore.EXTRA_OUTPUT, getCropImage(uri));
		activity.startActivityForResult(intent, requestCode);
		return cropPath;
	}
	

	private File cropPath;

	public Uri getCropImage(Uri uri) {
		String path = uri.getPath();
		int start = path.lastIndexOf("/");
		String name = path.substring(start + 1, path.length());
		File file = getPicPath(HttpUtlis.TEMP_Folder + "avatar", "/temp_"
				+ name);
		cropPath = file;
		return Uri.fromFile(file);

	}

	public String GpsMapSnapshot(Bitmap bm) {
		File path = getFilePath(HttpUtlis.TEMP_Folder + "/gps", "gps.png");

		if (path.exists())
			path.delete();
		try {
			FileOutputStream out = new FileOutputStream(path);
			bm.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return path.getAbsolutePath();
	}

	public String layoutscreenpng(View vv, Context context) {
		Bitmap bm = getBitmapByView(vv);
		File path = getFilePath(HttpUtlis.TEMP_Folder + "/gps", "gpsresult.png");
		if (path.exists())
			path.delete();
		try {
			FileOutputStream out = new FileOutputStream(path);
			bm.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return path.getAbsolutePath();
	}

	public static Bitmap getBitmapByView(View view) {
		int h = 0;
		Bitmap bitmap = null;
		// 创建对应大小的bitmap
		bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
				Bitmap.Config.ARGB_8888);
		final Canvas canvas = new Canvas(bitmap);
		view.draw(canvas);
		// 测试输出
		return bitmap;
	}

	public static Bitmap getBitmapBySrollView(ScrollView scrollView) {
		int h = 0;
		Bitmap bitmap = null;
		// 获取listView实际高度
		for (int i = 0; i < scrollView.getChildCount(); i++) {
			h += scrollView.getChildAt(i).getHeight();
			// scrollView.getChildAt(i).setBackgroundResource(R.drawable.bg3);
		}
		// 创建对应大小的bitmap
		bitmap = Bitmap.createBitmap(scrollView.getWidth(), h,
				Bitmap.Config.ARGB_8888);
		final Canvas canvas = new Canvas(bitmap);
		scrollView.draw(canvas);
		// 测试输出
		return bitmap;
	}

}

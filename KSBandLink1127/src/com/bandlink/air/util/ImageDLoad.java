package com.bandlink.air.util;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;

public class ImageDLoad {

	public static Bitmap getImage(String url) {
		File f = new File(url);
		File localFile = new File(Environment.getExternalStorageDirectory()
				.getPath(), f.getName());
		if (localFile.exists()) {
			System.out.println("exists");
			return BitmapFactory.decodeFile(localFile.getPath());
		} else {
			Task t = new Task();
			return t.doInBackground(new String[] { url, localFile.getPath() });
		}

	}

	static class Task extends AsyncTask<String, Integer, Bitmap> {
		int downloadSize = 0;
		int fileSize = 0;

		@Override
		protected Bitmap doInBackground(String... params) {
			downloadFile(params[0], params[1]);
			if (new File(params[1]).exists()) {
				return BitmapFactory.decodeFile(params[1]);
			} else {
				return null;
			}

		}

		@Override
		protected void onPostExecute(Bitmap result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
		}

		private void downloadFile(String url, String path) {
			System.out.println(url + "\n" + path);
			try {
				URL u = new URL(url);
				HttpURLConnection conn = (HttpURLConnection) u.openConnection();
				conn.setConnectTimeout(5 * 1000);
				conn.setRequestMethod("GET");

				InputStream is = conn.getInputStream();
				fileSize = conn.getContentLength();
				if (fileSize < 1 || is == null) {
					System.out.println("error");
				} else {

					FileOutputStream fos = new FileOutputStream(path);
					byte[] bytes = new byte[1024];
					int len = -1;
					while ((len = is.read(bytes)) != -1) {
						fos.write(bytes, 0, len);
						downloadSize += len;
						System.out.println("downloading");
					}

					System.out.println("ok");
					is.close();
					fos.close();
				}
			} catch (Exception e) {

				e.printStackTrace();
			}
		}
	}
}
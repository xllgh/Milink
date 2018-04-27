package com.bandlink.air.gps;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

import com.bandlink.air.util.HttpUtlis;

public class GPSUploadThread {

	private static GPSUploadThread uniqueInstance = null;
	private static String sessionid;
	private ArrayList<GPSEntity> Tlist;
	private ArrayList<GPSPointEntity> Plist;
	private static Context mcontext;
	String uploadUrl = HttpUtlis.BASE_URL;
	private String srcPath;

	public GPSUploadThread() {

	}

	public static GPSUploadThread getInstance() {

		if (uniqueInstance == null) {

			uniqueInstance = new GPSUploadThread();

		}
		return uniqueInstance;

	}

	public synchronized void Write2DatUpload(final int id) throws Exception {
		String pp = "/storage/sdcard0/" + id + ".dat";
		DataOutputStream out = null;
		try {
			PackageInfo info;
			info = mcontext
					.getApplicationContext()
					.getPackageManager()
					.getPackageInfo(
							mcontext.getApplicationContext().getPackageName(),
							0);
			String packageNames = info.packageName;
			File destDir = new File("/data/data/" + packageNames + "/gpsUpload");
			if (!destDir.exists()) {
				destDir.mkdirs();
			}
			pp = "/data/data/" + packageNames + "/gpsUpload/" + id + ".dat";
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			out = new DataOutputStream(new BufferedOutputStream(
					new FileOutputStream(pp)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			// android flag
			GPSEntity entity = Tlist.get(0);
			out.writeInt(0);
			// iid
			out.writeInt(entity.getId());
			// steps
			out.writeInt(entity.steps);
			// distance*1000 double to int
			int distance = (int) entity.distance * 1000;
			out.writeInt(distance);
			// durance
			out.writeInt(entity.durance);
			// calorie*1000 double to int
			int calorie = (int) entity.calorie * 1000;
			out.writeInt(calorie);

			// start time
			int time = getUnixTime(entity.time);
			out.writeInt(time);
			// points number
			out.writeInt(entity.points);

			for (int i = 0; i < Plist.size(); i++) {
				// latitude*1e6 double to int
				int lati = (int) (Plist.get(i).getLatitude()*1E6);
				out.writeInt(lati);
				// lontitude*1e6 double to int
				int longti = (int) ((double) Plist.get(i).getLongitude()*1E6);
				out.writeInt(longti);

				int datetime = getUnixTime(Plist.get(i).getDateTime());
				// timestamp
				out.writeInt(datetime);

				int speed = (int) (Plist.get(i).getSpeed() * 1000);
				// speed*1000 double to int
				out.writeInt(speed);

				// accuray*1000 double to int
				int accuracy = (int) (Plist.get(i).getAccuracy() * 1000);
				out.writeInt(accuracy);

				out.writeInt(0);
				out.writeInt(0);

			}
			out.close();

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		srcPath = pp;

		int code = uploadFile();
		Intent intent = new Intent("ACTION_UPLOADGPS");
		intent.putExtra("code", code);
		intent.putExtra("id", id);
		File file = new File(srcPath);
		deleteFile(file);
		mcontext.sendBroadcast(intent);

	}

	public void deleteFile(File file) {
		if (file.exists()) { // 判断文件是否存在
			if (file.isFile()) { // 判断是否是文件
				file.delete(); // delete()方法 你应该知道 是删除的意思;
			} else if (file.isDirectory()) { // 否则如果它是一个目录
				File files[] = file.listFiles(); // 声明目录下所有的文件 files[];
				for (int i = 0; i < files.length; i++) { // 遍历目录下所有的文件
					this.deleteFile(files[i]); // 把每个文件 用这个方法进行迭代
				}
			}
			file.delete();
		} else {

		}
	}

	public int getUnixTime(String time) {
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		int timeint = 0;
		try {
			Long a = sf.parse(time).getTime() / 1000;
			timeint = a.intValue();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return timeint;
	}

	public void setPostEntity(String s, Context context,
			ArrayList<GPSEntity> t, ArrayList<GPSPointEntity> po) {
		Plist = po;
		Tlist = t;
		sessionid = s;
		mcontext = context;
	}

	private int uploadFile() {
		int code = -1;
		String end = "\r\n";
		String twoHyphens = "--";
		String boundary = "******";
		try {
			URL url = new URL(uploadUrl + "/data/upLoadGps/session/"
					+ sessionid);
			HttpURLConnection httpURLConnection = (HttpURLConnection) url
					.openConnection();
			// 设置每次传输的流大小，可以有效防止手机因为内存不足崩溃
			// 此方法用于在预先不知道内容长度时启用没有进行内部缓冲的 HTTP 请求正文的流。
			// httpURLConnection.setChunkedStreamingMode(128 * 1024);// 128K
			// 允许输入输出流
			httpURLConnection.setDoInput(true);
			httpURLConnection.setDoOutput(true);
			httpURLConnection.setUseCaches(false);
			// 使用POST方法
			httpURLConnection.setRequestMethod("POST");
			httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
			httpURLConnection.setRequestProperty("Charset", "UTF-8");
			httpURLConnection.setRequestProperty("Content-Type",
					"multipart/form-data;boundary=" + boundary);

			DataOutputStream dos = new DataOutputStream(
					httpURLConnection.getOutputStream());
			dos.writeBytes(twoHyphens + boundary + end);
			dos.writeBytes("Content-Disposition: form-data; name=\"filedata\"; filename=\""
					+ srcPath.substring(srcPath.lastIndexOf("/") + 1)
					+ "\""
					+ end);
			dos.writeBytes(end);

			FileInputStream fis = new FileInputStream(srcPath);
			byte[] buffer = new byte[8192]; // 8k
			int count = 0;
			// 读取文件
			while ((count = fis.read(buffer)) != -1) {
				dos.write(buffer, 0, count);
			}
			fis.close();

			dos.writeBytes(end);
			dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
			dos.flush();
			/*
			 * HttpURLConnection uc = httpURLConnection; int code =
			 * uc.getResponseCode(); if (code == 200) { InputStream is =
			 * uc.getInputStream(); int ch; StringBuffer b = new StringBuffer();
			 * while ((ch = is.read()) != -1) { b.append((char) ch); } String
			 * result1 = b.toString(); is.close();
			 * 
			 * }
			 */
			InputStream is = httpURLConnection.getInputStream();
			InputStreamReader isr = new InputStreamReader(is, "utf-8");
			BufferedReader br = new BufferedReader(isr);
			String result = br.readLine();
			if (result != null) {
				JSONObject object = null;
				try {
					object = (JSONObject) new JSONTokener(result).nextValue();
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					int rs = object.getInt("status");
					if (rs == 0) {
						code = rs;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			dos.close();
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return code;
	}

}

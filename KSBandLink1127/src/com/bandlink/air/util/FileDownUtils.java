package com.bandlink.air.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.bandlink.air.MyLog;

public class FileDownUtils extends Thread {
	private OnHexDownloadListener listener;
	private long fileSize;
	private long downLoadFileSize;
	private Context context;
	private String filename;
	private Handler handler;

	private String urlString;
	private String filePath;

	public FileDownUtils(Handler h, String path, String u) {
		handler = h;
		urlString = u;
		filePath = path;
	}
	public FileDownUtils(OnHexDownloadListener listener, String path, String u) {
		this.listener = listener;
		urlString = u;
		filePath = path;
	}

	@Override
	public synchronized void run() {
		// TODO Auto-generated method stub
		super.run();
		try {
			down_file(urlString, filePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void down_file(String url, String path) throws IOException {
		// 下载函数
		filename = url.substring(url.lastIndexOf("/") + 1);
		// 获取文件名
		MyLog.e("eeee", filename);
		URL myURL = new URL(url.replaceAll(" ", "%20"));
		URLConnection conn = myURL.openConnection();
		conn.connect();
		InputStream is = conn.getInputStream();
		this.fileSize = conn.getContentLength();// 根据响应获取文件大小
		if (this.fileSize <= 0)
			throw new RuntimeException("无法获知文件大小 ");
		if (is == null)
			throw new RuntimeException("stream is null");

		CheckFileDirectory(path);

		FileOutputStream fos = new FileOutputStream(path + "/" + "Air.hex");
		// 把数据存入路径+文件名
		byte buf[] = new byte[1024];
		downLoadFileSize = 0;
		sendMsg(0);
		do {
			// 循环读取
			int numread = is.read(buf);
			if (numread == -1) {
				break;
			}
			fos.write(buf, 0, numread);
			downLoadFileSize += numread;
			if (numread * 100 > fileSize) {
				sendMsg(1);// 更新进度条
			}

		} while (true);
		sendMsg(2);// 通知下载完成
		try {
			is.close();
			fos.close();
		} catch (Exception ex) {
			MyLog.e("tag", "error: " + ex.getMessage(), ex);
		}
	}

	private void CheckFileDirectory(String pathString) {
		File directory = new File(pathString);

		if (!directory.exists()) {
			if (directory.mkdirs()) {
				MyLog.v("FILE", "成功");
			} else {
				MyLog.v("FILE", "失败");
			}
		}
	}

	
	private void sendMsg(int flag) {
		if(handler!=null){
			Message msg = new Message();
			msg.what = flag;
			msg.arg1 = (int) ((int) downLoadFileSize / fileSize);
			msg.obj = filename;
			handler.sendMessage(msg);
		}
		if(listener!=null){
			if(flag ==1){
				listener.OnHexProgress(((int) downLoadFileSize / fileSize));
			}else if(flag ==2){
				listener.OnHexDownloaded(filename);
			}
		}
		
	}
	public interface OnHexDownloadListener{
		public void OnHexProgress(float pre);
		public void OnHexDownloaded(String name);
	}
}


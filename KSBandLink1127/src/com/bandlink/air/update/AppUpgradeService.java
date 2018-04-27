package com.bandlink.air.update;

import java.io.File;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.Toast;

import com.bandlink.air.R;
import com.bandlink.air.util.HttpUtlis;

public class AppUpgradeService extends Service {

	public static final int APP_VERSION_LATEST = 0;
	public static final int APP_VERSION_OLDER = 1;

	public static final int mNotificationId = 100;
	private String mDownloadUrl = null, apkname = null;
	private NotificationManager mNotificationManager = null;
	private File destDir = null;
	private File destFile = null;

	private static final int DOWNLOAD_FAIL = -1;
	private static final int DOWNLOAD_SUCCESS = 0;
	NotificationCompat.Builder mBuilder;
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DOWNLOAD_SUCCESS:
				Toast.makeText(getApplicationContext(),
						R.string.upgrade_success, Toast.LENGTH_LONG).show();
				install(destFile);
				break;
			case DOWNLOAD_FAIL:
				Toast.makeText(getApplicationContext(),
						R.string.download_faield, Toast.LENGTH_LONG).show();
				mNotificationManager.cancel(mNotificationId);
				break;
			default:
				break;
			}
		}

	};
	private DownloadUtils.DownloadListener downloadListener = new DownloadUtils.DownloadListener() {
		@Override
		public void downloading(int progress) {
			if (progress % 2 == 0) {
				Intent i = new Intent("com.milink.android.lovewalk.upgrade");
				i.putExtra("progress", progress);
				sendBroadcast(i);
				mBuilder.setProgress(100, progress, false);
				mBuilder.setContentText(progress + "%");
				mNotificationManager.notify(mNotificationId, mBuilder.build());
			}

		}

		@Override
		public void downloaded() {
			Intent i = new Intent("com.milink.android.lovewalk.upgrade");
			i.putExtra("progress", 100);
			sendBroadcast(i);
			mBuilder.setProgress(100, 100, false);
			mBuilder.setContentText(getString(R.string.upgrade_success));
			mNotificationManager.notify(mNotificationId, mBuilder.build());
			if (destFile.exists() && destFile.isFile()
					&& checkApkFile(destFile.getPath())) {
				Message msg = mHandler.obtainMessage();
				msg.what = DOWNLOAD_SUCCESS;
				mHandler.sendMessage(msg);
			}
			mNotificationManager.cancel(mNotificationId);
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mDownloadUrl = intent.getStringExtra("downloadUrl");
		
		if (mDownloadUrl == null) {
			return super.onStartCommand(intent, flags, startId);
		}
		apkname = intent.getStringExtra("apkname");
		if (apkname == null) {
			apkname = HttpUtlis.UPDATE_SAVENAME;
		}
		if (Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			destDir = new File(Environment.getExternalStorageDirectory()
					.getPath() + "/download");
			
		} else {
			return super.onStartCommand(intent, flags, startId);
		}

		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mBuilder = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(getString(R.string.upgrade_downloading))
				.setContentText("0%")
				.setWhen(SystemClock.currentThreadTimeMillis());
		Intent resultIntent = new Intent(this, UpdateActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(UpdateActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
				PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		mNotificationManager.cancel(mNotificationId);
		mNotificationManager.notify(mNotificationId, mBuilder.build());
		new AppUpgradeThread().start();
		return super.onStartCommand(intent, flags, startId);
	}

	class AppUpgradeThread extends Thread {

		@Override
		public void run() {
			if (Environment.getExternalStorageState().equals(
					android.os.Environment.MEDIA_MOUNTED)) {
				if (destDir == null) {
					destDir = new File(Environment
							.getExternalStorageDirectory().getPath()
							+ "/download");
				}
				if (destDir.exists() || destDir.mkdirs()) {
					destFile = new File(destDir.getPath() + "/" + apkname);
					if (destFile.exists() && destFile.isFile()
							&& checkApkFile(destFile.getPath())) {
						destFile.deleteOnExit();
					}

					try {
						DownloadUtils.download(mDownloadUrl, destFile, false,
								downloadListener);
					} catch (Exception e) {
						Message msg = mHandler.obtainMessage();
						msg.what = DOWNLOAD_FAIL;
						mHandler.sendMessage(msg);
						e.printStackTrace();
					}
				}
			}
			stopSelf();
		}
	}

	public boolean checkApkFile(String apkFilePath) {
		boolean result = false;
		try {
			PackageManager pManager = getPackageManager();
			PackageInfo pInfo = pManager.getPackageArchiveInfo(apkFilePath,
					PackageManager.GET_ACTIVITIES);
			if (pInfo == null) {
				result = false;
			} else {

				result = true;
			}
		} catch (Exception e) {
			result = false;
			e.printStackTrace();
		}
		return result;
	}

	public void install(File apkFile) {
		Uri uri = Uri.fromFile(apkFile);
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(uri, "application/vnd.android.package-archive");
		startActivity(intent);
	}
}
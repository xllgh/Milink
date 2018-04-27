package com.bandlink.air.update;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bandlink.air.R;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.MyDate;
import com.bandlink.air.util.SharePreUtils;

public class UpdateActivity extends LovefitActivity implements OnClickListener {
	private TextView btn_check, txt_VerNow, lastest_V, newfunction, ex_url;
	private Button btn_UpdateNow;
	private ProgressBar bar;
	private PackageInfo info;
	private String versionNow, versionLastest, versionText, downurl;
	private int vCodeNow, vCodeLastest = -1;
	private boolean canInstall = false;
	private String SAVE_PATH;
	private SharedPreferences share;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		setContentView(R.layout.checkupdate);
		share = getApplicationContext()
				.getSharedPreferences(
						SharePreUtils.APP_ACTION,
						Context.MODE_PRIVATE);
		ActionbarSettings actionbar = new ActionbarSettings(this,
				new OnClickListener() {

					@Override
					public void onClick(View v) {

						UpdateActivity.this.finish();
					}
				}, null);
		actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
		actionbar.setTitle(R.string.check_upgrade);

		versionLastest = getString(R.string.please_check);
		initViews();

		ex_url = (TextView) findViewById(R.id.ex_url);
		ex_url.setVisibility(View.GONE);
		ex_url.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Uri uri = Uri.parse("http://" + "www.lovefit.com/air/bandlink/"
						+ appname);
				// 通过Uri获得编辑框里的//地址，加上http://是为了用户输入时可以不要输入
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				// 建立Intent对象，传入uri
				startActivity(intent);
			}
		});
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {

		getLastestVersion();
		registerReceiver(updateResReceiver, new IntentFilter(
				"com.milink.android.lovewalk.upgrade"));
		super.onResume();
	}

	BroadcastReceiver updateResReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			int pro = intent.getIntExtra("progress", 0);
			bar.setVisibility(View.VISIBLE);
			// hand.obtainMessage(1, pro);
			bar.setProgress(pro);
			btn_UpdateNow.setEnabled(false);
			btn_UpdateNow.setText(getString(R.string.upgrade_downloading) + ":"
					+ '\t' + pro + "%");
			if (pro == 100) {
				unregisterReceiver(updateResReceiver);
				btn_UpdateNow.setEnabled(true);
				btn_UpdateNow.setText(getString(R.string.install));
				btn_UpdateNow.setBackgroundResource(R.drawable.corner_bg_red);
				canInstall = true;
				share.edit().putBoolean("appupdate", false).commit();
			}
		}
	};

	@Override
	protected void onDestroy() {

		unregisterReceiver(updateResReceiver);
		super.onDestroy();
	}

	private void initViews() {
		try {
			info = this.getPackageManager().getPackageInfo(
					this.getPackageName(), 0);
			versionNow = info.versionName;
			vCodeNow = info.versionCode;
		} catch (NameNotFoundException e) {

			e.printStackTrace();
		}
		newfunction = (TextView) findViewById(R.id.newfunction);
		btn_check = (TextView) findViewById(R.id.btn_checkupdate);
		txt_VerNow = (TextView) findViewById(R.id.current_V);
		lastest_V = (TextView) findViewById(R.id.lastest_V);
		btn_UpdateNow = (Button) findViewById(R.id.upgradenow);
		bar = (ProgressBar) findViewById(R.id.bar);
		btn_check.setOnClickListener(this);
		txt_VerNow.setOnClickListener(this);
		lastest_V.setOnClickListener(this);
		btn_UpdateNow.setOnClickListener(this);
		btn_UpdateNow.setEnabled(false);
		btn_UpdateNow.setTextColor(getResources().getColor(R.color.white_0_8));
		txt_VerNow.setText(txt_VerNow.getText().toString() + versionNow);
		bar.setVisibility(View.GONE);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.btn_checkupdate:
			getLastestVersion();
			break;
		case R.id.current_V:
			break;
		case R.id.lastest_V:
			break;
		case R.id.upgradenow: 

			if (new File(SAVE_PATH).exists()) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setDataAndType(Uri.fromFile(new File(SAVE_PATH)),
						"application/vnd.android.package-archive");
				startActivity(intent);
			} else {

				btn_UpdateNow.setEnabled(false);
				btn_UpdateNow.setText(getString(R.string.upgrade_downloading)
						+ ":" + '\t' + 0 + "%");
				bar.setVisibility(View.VISIBLE);
				Intent intent = new Intent(UpdateActivity.this,
						AppUpgradeService.class);

				if (downurl != null && downurl.length() > 5) {
					intent.putExtra("downloadUrl", downurl);
					intent.putExtra("apkname", appname);
				} else {
					intent.putExtra("downloadUrl", HttpUtlis.UPDATE_APK + "?z="
							+ MyDate.getFileName());
				}
				startService(intent);
			}
			break;
		}
	}

	String appname;

	public void getLastestVersion() {
		new Thread(new Runnable() {

			@Override
			public void run() {

				String result = HttpUtlis
						.queryStringForPost(HttpUtlis.UPDATE_URL);
				try {
					result = new String(result.getBytes("iso8859-1"), "utf-8");
					JSONArray j = new JSONArray(result);
					JSONObject json = j.getJSONObject(0);
					versionLastest = json.getString("verName");
					
					vCodeLastest = json.getInt("verCode");
					if (json.has("apkname")) {
						appname = json.getString("apkname");
					}

					versionText = json.getString("text");
					if (json.has("url")) {
						downurl = json.getString("url");
					}
					hand.sendEmptyMessage(0);
				} catch (Exception e) {

					e.printStackTrace();
				}
			}
		}).start();

	}

	private void checkSDVersion() {
		if (Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			File destFile = new File(SAVE_PATH);
			if (destFile.exists()) {

				if (destFile.exists() && destFile.isFile()) {
					PackageManager pManager = getPackageManager();
					PackageInfo pInfo = pManager.getPackageArchiveInfo(
							destFile.getPath(), PackageManager.GET_CONFIGURATIONS);
					if (pInfo != null) {
						if (pInfo.versionCode < vCodeLastest-1) {
							// 显示立即更新
							canInstall = false;
							btn_UpdateNow.setEnabled(true);
							btn_UpdateNow.setTextColor(getResources().getColor(
									R.color.white));

						} else {
							// 显示安装
							lastest_V.setText(getString(R.string.local_apk)
									+ pInfo.versionName);

							canInstall = true;
							btn_UpdateNow.setEnabled(true);
							btn_UpdateNow.setText(getString(R.string.install));
							btn_UpdateNow.setTextColor(getResources().getColor(
									R.color.white));
							btn_UpdateNow
									.setBackgroundResource(R.drawable.corner_bg_red);
						}
					} else {
						canInstall = false;
						btn_UpdateNow.setEnabled(true);
						btn_UpdateNow.setTextColor(getResources().getColor(
								R.color.white));
						newfunction.setText(versionText);
						lastest_V.setText(getString(R.string.lastest_v)
								+ versionLastest);
					}
				}
			} else {
				if (appname != null && appname.length() > 7) {
					ex_url.setVisibility(View.VISIBLE);
				}
				canInstall = false;
				btn_UpdateNow.setEnabled(true);
				btn_UpdateNow.setTextColor(getResources().getColor(
						R.color.white));
			}
		}
	}

	private Handler hand = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case 0:
				SAVE_PATH = Environment.getExternalStorageDirectory().getPath()
						+ "/download" + "/" + (appname == null ? HttpUtlis.UPDATE_SAVENAME
								: appname);
				newfunction.setVisibility(View.VISIBLE);
				if (vCodeNow < vCodeLastest) {
					newfunction.setText(versionText);
					lastest_V.setText(getString(R.string.lastest_v)
							+ versionLastest);
					checkSDVersion();
				} else {
					lastest_V.setText(R.string.move_new_version);
					newfunction.setVisibility(View.GONE);
				}
				break;
			case 1:
				bar.setProgress((Integer) msg.obj);

				break;
			}
			super.handleMessage(msg);
		}

	};

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub

	}

}

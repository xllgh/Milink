package com.bandlink.air;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bandlink.air.util.FileDownUtils;

public class FileDownDialog extends Activity {

	private Button btnButton;
	private Button cleButton;
	private Handler handler;
	private ProgressBar progress;
	private TextView per;

	private Intent intent;
	private String filename;
	private String httppath = "http://www.lovefit.com/air/";
	private String filepath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setTheme(R.style.MyDialogStyle);
		setContentView(R.layout.checkupdate_dialog);
		
		btnButton = (Button) findViewById(R.id.btndown);
		per = (TextView) findViewById(R.id.per);
		cleButton = (Button) findViewById(R.id.btncancle);
		progress = (ProgressBar) findViewById(R.id.bar);

		intent = this.getIntent();
		if (intent != null) {
			filename = intent.getStringExtra("filename");
		}

		filepath = this.getFilesDir().toString() + "/air";
		MyLog.e("eeeee", filepath);

		if (progress != null) {
			progress.setMax(100);
		}

		if (btnButton != null) {
			btnButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					// 下载文件
					if (filename != null) {
						FileDownUtils fileDownUtils = new FileDownUtils(
								handler, filepath, httppath + filename);
						fileDownUtils.start();
					}
					btnButton.setEnabled(false);
				}
			});
		}

		if (cleButton != null) {
			cleButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					FileDownDialog.this.finish();
				}
			});
		}

		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				switch (msg.what) {
				case 1:
					if (progress != null) {
						progress.setProgress(msg.arg1);
						per.setText(msg.arg1 + "%");
					}
					break;
				case 2:
					if (progress != null) {
						progress.setProgress(100);
					}
					if (filename != null) {
						//saveFile(readFile(filename), "Air v2.1.5.hex");
						Intent intent = new Intent(FileDownDialog.this,
								FileUpdateActivity.class);
						intent.putExtra("filename", "Air.hex");
						intent.putExtra("version", filename);
						startActivity(intent);
					}
					FileDownDialog.this.finish();
					break;
				default:
					break;
				}
			}

		};
	}

	private String readFile(String filename) {
		String reads = "";
		try {
			FileInputStream fis = this.openFileInput(filename);
			byte[] b = new byte[1024];
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while (fis.read(b) != -1) {
				baos.write(b, 0, b.length);
			}
			baos.close();
			fis.close();
			reads = baos.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return reads;
	}

	private void saveFile(String str, String filename) {
		FileOutputStream fos;
		try {
			fos = this.openFileOutput(filename, this.MODE_PRIVATE);
			fos.write(str.getBytes());
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} 
}

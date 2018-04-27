package com.bandlink.air.simple;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bandlink.air.R;
import com.bandlink.air.club.BorderImageView;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.AsynImagesLoader;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.MApplication;
import com.bandlink.air.util.MediaUtils;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Util;
import com.bandlink.air.view.HeightRuler;
import com.bandlink.air.view.NewWheel;
import com.bandlink.air.view.WeightRuler;
import com.nostra13.universalimageloader.core.ImageLoader;

public class InitUserData extends Activity implements OnClickListener {

	private TextView curHeight, curWeight, age, target;
	private NewWheel weight;
	private RadioGroup radio;
	private ImageView imageView1;
	private SharedPreferences share;
	private Button next;
	private ActionbarSettings actionBar; 
	private Context mContext;
	private RelativeLayout relaHeight, relaWeight, relaBrith;
	private Dbutils db;
	private TextView txHeight, txWeight, txBrith, txName;
	private BorderImageView photo;
	private MediaUtils mediautils;
	private File sdcardTempFile;
	private AsynImagesLoader imageloader;
	private int sexNum = 1;
	private int  height = 172, bYear = 1992, bMonth = 1;
	private String weights = "60.0";
	private String session_id;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setContentView(R.layout.simple_init_2);
		mediautils = new MediaUtils(this);
		imageloader = AsynImagesLoader.getInstance(this, false);
		sdcardTempFile = mediautils.getPicPath(
				HttpUtlis.TEMP_Folder + "avatar", " myAvatar.jpg");
		share = getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		session_id = share.getString("session_id", "");
		db = new Dbutils(share.getInt("UID", -1), this);
		actionBar = new ActionbarSettings(this, null, null);
		actionBar.setTitle(R.string.finishpersonaldata);
		next = (Button) findViewById(R.id.next);
		next.setOnClickListener(this);
		radio = (RadioGroup) findViewById(R.id.sex);
		radio.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
				// TODO Auto-generated method stub
				switch (arg1) {
				case R.id.radio_mode:

					sexNum = 1;
					break;
				case R.id.fe:

					sexNum = 2;
					break;
				}
			}
		});
		mContext = Util.getThemeContext(this);
		relaHeight = (RelativeLayout) findViewById(R.id.height_border);
		relaWeight = (RelativeLayout) findViewById(R.id.weight_border);
		relaBrith = (RelativeLayout) findViewById(R.id.brith_border);
		relaHeight.setOnClickListener(this);
		relaWeight.setOnClickListener(this);
		relaBrith.setOnClickListener(this);
		txHeight = (TextView) findViewById(R.id.height);
		txWeight = (TextView) findViewById(R.id.weight);
		txBrith = (TextView) findViewById(R.id.brith);
		txName = (TextView) findViewById(R.id.name);
		photo = (BorderImageView) findViewById(R.id.photo);
		txName.setOnClickListener(this);
		photo.setOnClickListener(this);
		MApplication.getInstance().addActivity(this);
		super.onCreate(savedInstanceState);
	}
@Override
protected void onDestroy() {
	// TODO Auto-generated method stub
	MApplication.getInstance().remove(this);
	super.onDestroy();
}
	public void showPhoto() {
		final CharSequence[] items = this.getResources().getTextArray(
				R.array.pic_from);
		AlertDialog dlg = new AlertDialog.Builder(mContext)
				.setTitle(R.string.picImage)
				.setItems(items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						if (item == 1) {

							Intent intent = new Intent(Intent.ACTION_PICK, null);
							intent.setDataAndType(
									MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
									"image/*");
							startActivityForResult(intent, 2);
						} else {
							mediautils.invokeCamera(sdcardTempFile, 1);
						}
					}
				}).create();
		dlg.show();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.height_border:
			showHeight();
			break;
		case R.id.weight_border:
			showWeight();
			break;
		case R.id.brith_border:
			showBrith();
			break;
		case R.id.name:
			showName();
			break;
		case R.id.photo:
			showPhoto();
			break;
		case R.id.next:
			if (txName.getText().toString().length() > 11) {
				Toast.makeText(InitUserData.this,
						getString(R.string.nikename_length), Toast.LENGTH_SHORT)
						.show();
				break;
			}
			// 性别
			updateSex(sexNum);
			// 昵称
			updateNick(txName.getText().toString());

			// 身高
			updateHeight(height);
			// 体重
			updateWeight(weights);
			// 生日
			updateBirth(bYear, bMonth);
			Intent i = new Intent(this, SetTarget.class);
			i.putExtra("session_id", session_id);
			startActivity(i);
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	void showName() {
		AlertDialog.Builder ab = new AlertDialog.Builder(mContext);
		ab.setTitle(getString(R.string.update_nick));

		final EditText e = new EditText(mContext);
		e.setText(txName.getText());
		e.setSingleLine();
		LinearLayout l = new LinearLayout(mContext);
		l.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		e.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		l.setPadding(20, 30, 20, 0);
		l.addView(e);
		ab.setView(l);
		ab.setNegativeButton(R.string.ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						txName.setText(e.getText().toString());
					}
				});
		ab.create().show();
	}

	void showBrith() {
		int year = share.getInt("year", 1990);
		int month = share.getInt("month", 1);

		final DatePicker date = new DatePicker(mContext);
		date.init(year, month - 1, 1, null);
		((ViewGroup) date.getChildAt(0)).getChildAt(1).setVisibility(View.GONE);
		// 隐藏
		String lan = getResources().getConfiguration().locale.getISO3Language();
		String country = getResources().getConfiguration().locale.getCountry();
		//
		int hide = 2;
		if (lan.contains("zh")) {
			hide = 2;
		} else if (lan.contains("en")) {
			if (country.contains("US")) {
				hide = 1;
			} else {
				hide = 0;
			}
		}
		((ViewGroup) ((ViewGroup) date.getChildAt(0)).getChildAt(0))
				.getChildAt(hide).setVisibility(View.GONE);

		AlertDialog.Builder ab = new AlertDialog.Builder(mContext);
		ab.setView(date);
		ab.setTitle(R.string.selectbrith);
		ab.setPositiveButton(R.string.finish,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

						share.edit().putInt("year", date.getYear()).commit();
						share.edit().putInt("month", date.getMonth() + 1)
								.commit();
						txBrith.setText(date.getYear() + "" + '\t' + ""
								+ getString(R.string.year)
								+ (date.getMonth() + 1)
								+ getString(R.string.month));
						bYear = date.getYear();
						bMonth = date.getMonth() + 1;
						dialog.dismiss();
					}
				});
		ab.create().show();
	}

	private void updateBirth(final int year, final int month) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Map<String, String> up = new HashMap<String, String>();
				up.put("session", share.getString("session_id", ""));
				up.put("year", year + "");
				up.put("month", month + "");
				try {
					String result = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/user/setUserBirthday", up);
					JSONObject ob = new JSONObject(result);
					if (ob.getInt("status") == 0) {
						db.UpdateUserBirth(year + getString(R.string.year)
								+ month + getString(R.string.month));
						// myHandler.obtainMessage(8).sendToTarget();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	void showWeight() {
		String h = "60";
		try {
			h = Double.valueOf(txWeight.getText().toString().replace("kg", ""))
					+ "";
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Intent intent4 = new Intent();
		intent4.setClass(this, WeightRuler.class);
		intent4.putExtra("startEdit1", "");
		intent4.putExtra("title", getString(R.string.weight_ruler_title3));
		intent4.putExtra("start_w", h);
		intent4.putExtra("pppp", "pp");
		startActivityForResult(intent4, 4);
	}

	void showHeight() {
		Intent intent5 = new Intent();
		intent5.setClass(this, HeightRuler.class);
		intent5.putExtra("height", 172.0);
		String h = "172";
		try {
			h = Double.valueOf(txHeight.getText().toString()
					.substring(0, txHeight.getText().length() - 2))
					+ "";
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		intent5.putExtra("uheight", h);
		this.startActivityForResult(intent5, 5);
	}

	private void updateWeight(final String reweight) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Map<String, String> up = new HashMap<String, String>();
				up.put("session", share.getString("session_id", ""));
				up.put("weight", reweight + "");
				try {
					String result = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/user/setUserWeight", up);
					JSONObject ob = new JSONObject(result);
					if (ob.getInt("status") == 0) {
						db.UpdateUserWeight(reweight + "");
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == 4 && data != null) {
			String w = data.getStringExtra("startEdit");
			double we = Double.parseDouble(w);
			txWeight.setText(String.format("%.1f", we) + "kg");
			weights = String.format("%.1f", we);

		} else if (requestCode == 5 && data != null) {
			try {
				String h = data.getStringExtra("uheight");
				// on = data.getStringExtra("on");
				txHeight.setText(h + "cm");
				height = Integer.valueOf(h);
			} catch (Exception e) {

			}
		} else if (requestCode == 1) {
			if (resultCode == RESULT_OK)
				sdcardTempFile = mediautils.startPhotoZoom(
						Uri.fromFile(sdcardTempFile), 150, 150, 3);
		} else if (requestCode == 2 && data != null) {
			if (resultCode == RESULT_OK) {
				Uri originalUri = data.getData();
				sdcardTempFile = mediautils.startPhotoZoom(originalUri, 200,
						200, 3);
			}
		} else if (requestCode == 3 && data != null) {
			if (data != null && resultCode == RESULT_OK) {
				Bundle bundle = data.getExtras();
				if (bundle != null) {
					Bitmap p = bundle.getParcelable("data");
					photo.setImageBitmap(p);
					submitAvatar();
				}
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	public void submitAvatar() {
		new Thread() {
			public void run() {
				try {
					// progressdialog = initProgressDialog();
					Bitmap bitmap = BitmapFactory.decodeFile(sdcardTempFile
							.getAbsolutePath());
					String result = HttpUtlis
							.uploadAvatar(
									HttpUtlis.UPAVATAR_URL
											+ share.getString("session_id", ""),
									bitmap);
					JSONObject avatar = new JSONObject(result);
					if (avatar.getInt("status") == 0) {
						imageloader.clearCache();
						ImageLoader.getInstance().clearDiskCache();
						// if (progressdialog != null
						// && progressdialog.isShowing())
						// progressdialog.dismiss();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}.start();

	}

	private void updateSex(final int s) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Map<String, String> up = new HashMap<String, String>();
				up.put("session", share.getString("session_id", ""));
				up.put("sex", s + "");
				try {
					String result = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/user/setUserSex", up);
					JSONObject ob = new JSONObject(result);
					if (ob.getInt("status") == 0) {
						db.UpdateSex(s);
						// myHandler.obtainMessage(8).sendToTarget();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void updateNick(final String etnick) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Map<String, String> up = new HashMap<String, String>();
				up.put("session", share.getString("session_id", ""));
				up.put("nickname", etnick);
				try {
					String result = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/user/setUserNickname", up);
					JSONObject ob = new JSONObject(result);
					if (ob.getInt("status") == 0) {
						db.UpdateUserInfo(etnick);
						// myHandler.obtainMessage(8).sendToTarget();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void updateHeight(final double reheight) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Map<String, String> up = new HashMap<String, String>();
				up.put("session", share.getString("session_id", ""));
				up.put("height", reheight + "");
				try {
					String result = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/user/setUserHeight", up);
					JSONObject ob = new JSONObject(result);
					if (ob.getInt("status") == 0) {
						db.UpdateUserHeight(reheight + "");
						// myHandler.obtainMessage(8).sendToTarget();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}
}

/**
 * vBar = (VerticalSeekBar)findViewById(R.id.progress); curHeight =
 * (TextView)findViewById(R.id.tx_h); curWeight =
 * (TextView)findViewById(R.id.tx_w); target =
 * (TextView)findViewById(R.id.target); target.setOnClickListener(new
 * OnClickListener() {
 * 
 * @Override public void onClick(View v) { // TODO Auto-generated method stub
 *           Context mContext = InitUserData.this;
 *           if(Build.VERSION.SDK_INT>=11){ mContext = new
 *           ContextThemeWrapper(mContext, android.R.style.Theme_Holo_Light); }
 *           AlertDialog.Builder ab = new AlertDialog.Builder(mContext);
 *           ab.setTitle("每日目标"); ab.setItems(strs,new
 *           DialogInterface.OnClickListener() {
 * @Override public void onClick(DialogInterface dialog, int which) { // TODO
 *           Auto-generated method stub target.setText("每天"+strs[which]); } });
 *           ab.create().show(); }; }); next = (Button)findViewById(R.id.next);
 *           next.setOnClickListener(new OnClickListener() {
 * @Override public void onClick(View v) { // TODO Auto-generated method stub
 *           InitUserData.this.finish(); startActivity(new
 *           Intent(InitUserData.this,SlideMainActivity.class)); } }); age =
 *           (TextView)findViewById(R.id.age);
 * 
 *           age.setText(share.getInt("year", 1990) + getString(R.string.year) +
 *           share.getInt("month", 1) + getString(R.string.month));
 *           age.setOnClickListener(new OnClickListener() {
 * @Override public void onClick(View v) { // TODO Auto-generated method stub
 *           int year = share.getInt("year", 1990); int month =
 *           share.getInt("month", 1); Context mContext = InitUserData.this;
 *           if(Build.VERSION.SDK_INT>=11){ mContext = new
 *           ContextThemeWrapper(mContext, android.R.style.Theme_Holo_Light); }
 *           final DatePicker date = new DatePicker(mContext); date.init(year,
 *           month - 1, 1, null); ((ViewGroup)
 *           date.getChildAt(0)).getChildAt(1).setVisibility( View.GONE); // 隐藏
 *           String lan = getResources().getConfiguration().locale
 *           .getISO3Language(); String country =
 *           getResources().getConfiguration().locale .getCountry(); // int hide
 *           = 2; if (lan.contains("zh")) { hide = 2; } else if
 *           (lan.contains("en")) { if (country.contains("US")) { hide = 1; }
 *           else { hide = 0; } } ((ViewGroup) ((ViewGroup)
 *           date.getChildAt(0)).getChildAt(0))
 *           .getChildAt(hide).setVisibility(View.GONE);
 * 
 *           AlertDialog.Builder ab = new AlertDialog.Builder(mContext);
 *           ab.setView(date); ab.setTitle(R.string.selectbrith);
 *           ab.setPositiveButton(R.string.finish, new
 *           DialogInterface.OnClickListener() {
 * @Override public void onClick(DialogInterface dialog, int which) { // TODO
 *           Auto-generated method stub
 * 
 *           share.edit().putInt("year", date.getYear()) .commit();
 *           share.edit().putInt("month", date.getMonth() + 1) .commit();
 *           age.setText(date.getYear() + getString(R.string.year) +
 *           (date.getMonth() + 1) + getString(R.string.month));
 *           //updateBirth(date.getYear(), date.getMonth() + 1);
 *           dialog.dismiss(); } }); ab.create().show(); } }); weight =
 *           (NewWheel)findViewById(R.id.weight); radio =
 *           (RadioGroup)findViewById(R.id.sex); imageView1 =
 *           (ImageView)findViewById(R.id.imageView1);
 *           radio.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 * @Override public void onCheckedChanged(RadioGroup group, int checkedId) { //
 *           TODO Auto-generated method stub switch(checkedId){ case
 *           R.id.radio_mode: imageView1.setImageResource(R.drawable.per_man);
 *           break; case R.id.fe:
 *           imageView1.setImageResource(R.drawable.per_woman); break; } } });
 *           weight.initViewParam(60.0f, 200, NewWheel.MOD_TYPE_ONE);
 *           weight.setValueChangeListener(new OnValueChangeListener() {
 * @Override public void onValueChange(float value) { // TODO Auto-generated
 *           method stub curWeight.setText(String.format("%.1f", value)+"kg"); }
 *           }); curWeight.setText(String.format("%.1f", 60.0f)+"kg");
 *           curHeight.setText(String.format("%.2f", ((50f/100f)*1.5f)+1)+"m");
 *           vBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 * @Override public void onStopTrackingTouch(SeekBar seekBar) { // TODO
 *           Auto-generated method stub
 * 
 *           }
 * @Override public void onStartTrackingTouch(SeekBar seekBar) { // TODO
 *           Auto-generated method stub
 * 
 *           }
 * @Override public void onProgressChanged(SeekBar seekBar, int progress,
 *           boolean fromUser) { // TODO Auto-generated method stub
 *           curHeight.setText(String.format("%.2f",
 *           ((progress/100f)*1.5f)+1)+"m"); } });
 **/

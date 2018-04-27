package com.bandlink.air;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.ImageFolderActivity;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.MediaUtils;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.view.NoRegisterDialog;

public class ShareEditActivity extends LovefitActivity {
	private File sdcardTempFile, sdcardTempFile2;
	private static int CAMERA = 1;
	private static int GALLERY = 2;
	private static int CROP = 3;
	LinkedList<View> pics = new LinkedList<View>();
	LinkedList<String> picpaths = new LinkedList<String>();
	LinearLayout hs, li;
	RelativeLayout elayout;
	MediaUtils mediautils;
	EditText note;
	TextView tvv, five;
	SharedPreferences share;
	String result, send, attach_id = "", tt;
	ProgressDialog dialog;
	int suid, tid, num, viewNum = 0;
	ConnectivityManager cm;
	private NoRegisterDialog d;
	private Context mContext;
	private String cID;
	ImageView add;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setContentView(R.layout.shareedit);
		mContext = this;
		mediautils = new MediaUtils(this);
		elayout = (RelativeLayout) findViewById(R.id.editlayout);
		Intent ss = getIntent();
		cID = ss.getExtras().getString("from");

		tid = ss.getExtras().getInt("tid");

		li = (LinearLayout) findViewById(R.id.limited);
		add = (ImageView) findViewById(R.id.add);
		tvv = (TextView) findViewById(R.id.romnum);
		five = (TextView) findViewById(R.id.tishi);
		hs = (LinearLayout) findViewById(R.id.hscroll);
		note = (EditText) findViewById(R.id.publish_note);
		ActionbarSettings actionbar = new ActionbarSettings(this,
				new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						// setResult(1);
						ShareEditActivity.this.finish();
					}
				}, rightlistener);
		share = getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		suid = share.getInt("UID", suid);
		if (tid == 3) {
			String s = ss.getExtras().getString("imagePath");
			add.setVisibility(View.VISIBLE);
			add.setImageBitmap(BitmapFactory.decodeFile(s));
			picpaths.add(s);
		}
		if (getIntent().getStringExtra("witch") != null) {
			note.setVisibility(View.GONE);
		}
		if (tid == 1) {
			five.setVisibility(View.VISIBLE);
			elayout.setVisibility(View.VISIBLE);
			actionbar.setTitle(R.string.image_and_text);
		} else if (tid == 0) {
			actionbar.setTitle(R.string.text);
		} else if (tid == 2) {
			note.setHint(R.string.limited);
			actionbar.setTitle(R.string.message);
			li.setVisibility(View.VISIBLE);
		} else if (tid == 3) {
			actionbar.setTitle(R.string.share);
		} else if (tid == 4) {
			actionbar.setTitle(R.string.share);
			note.setText(getString(R.string.share_content));
		}
		actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
		actionbar.setTopRightIcon(R.drawable.complete);
		if (share.getInt("ISMEMBER", 0) == 0) {
			d = new NoRegisterDialog(ShareEditActivity.this,
					R.string.no_register, R.string.no_register_content);
			d.show();
		}
		note.addTextChangedListener(new TextWatcher() {
			private CharSequence temp;
			private int selectionStart;
			private int selectionEnd;
			int num = 20;

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				temp = s;
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {

				// TODO Auto-generated method stub
				if (tid == 2) {
					int number = num - s.length();
					tvv.setText("" + number);
					selectionStart = note.getSelectionStart();
					selectionEnd = note.getSelectionEnd();
					if (temp.length() > num) {
						s.delete(selectionStart - 1, selectionEnd);
						int tempSelection = selectionEnd;
						note.setText(s);
						note.setSelection(tempSelection);// 璁剧疆鍏夋爣鍦ㄦ渶鍚?
					}
				}
			}
		});
		super.onCreate(savedInstanceState);
	}

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				Toast.makeText(ShareEditActivity.this,
						getString(R.string.errr), Toast.LENGTH_SHORT).show();
				break;
			case 1:
				Toast.makeText(ShareEditActivity.this,
						getString(R.string.plogin), Toast.LENGTH_SHORT).show();
				break;
			case 2:
				new Thread(new send()).start();
				break;
			}
		}
	};

	class setMessage implements Runnable {
		public void run() {
			// http://192.168.9.33/home/Dongtai/personalNote/uid/6/message/
			Message msg = handler.obtainMessage();
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("uid", suid + ""));
			params.add(new BasicNameValuePair("message", tt));
			try {
				send = HttpUtlis.SendFeed(HttpUtlis.PERSONALNOTE, params);
				if (result != null) {
					JSONObject json;
					json = new JSONObject(result);
					if (Integer.valueOf(json.getInt("status")) == 1) {
						msg.what = 1;
						handler.sendMessage(msg);
					} else if (Integer.valueOf(json.getInt("status")) == 2) {
						msg.what = 0;
						handler.sendMessage(msg);
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (dialog != null && dialog.isShowing()) {
				dialog.dismiss();
			}
			// setResult(0);
			ShareEditActivity.this.finish();

		}
	}

	class send implements Runnable {
		public void run() {
			// TODO Auto-generated method stub
			Message msg = handler.obtainMessage();
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("uid", suid + ""));
			params.add(new BasicNameValuePair("content", tt));
			params.add(new BasicNameValuePair("from", "11"));
			if (cID != null) {
				params.add(new BasicNameValuePair("clubid", cID));
			} else {
				params.add(new BasicNameValuePair("clubid", "0"));
			}
			if (getIntent().getStringExtra("witch") != null) {
				params.add(new BasicNameValuePair("witch", "1"));
			}
			if (tid == 1) {
				if (hs.getChildCount() > 0) {
					params.add(new BasicNameValuePair("type", "postimage"));
					params.add(new BasicNameValuePair("attach_id", attach_id));
				} else {
					msg.what = 2;
					handler.sendMessage(msg);
				}
			} else if (tid == 3) {
				params.add(new BasicNameValuePair("type", "postimage"));
				params.add(new BasicNameValuePair("attach_id", attach_id));
			} else {
				params.add(new BasicNameValuePair("type", "post"));
			}
			try {
				send = HttpUtlis.SendFeed(HttpUtlis.POSTFEED, params);
				sendBroadcast(new Intent(FriendFragment.ACTION_SENDRESULT));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (dialog != null && dialog.isShowing()) {
				dialog.dismiss();
			}
			// setResult(0);
			// ShareEditActivity.this.finish();

		}
	}

	class uploadImg implements Runnable {
		public void run() {

			String urlStr = HttpUtlis.UPLOADIMAGE + suid;
			Message msg = handler.obtainMessage();
			try {
				result = HttpUtlis.postFeedImg(ShareEditActivity.this, urlStr,
						picpaths);
				if (result != null) {
					JSONObject json;
					JSONArray arr;
					json = new JSONObject(result);
					if (Integer.valueOf(json.getInt("status")) == 0) {
						arr = json.getJSONArray("content");
						for (int i = 0; i < arr.length(); i++) {
							if (i > 0) {
								attach_id += ",";
							}
							JSONObject temp = (JSONObject) arr.get(i);
							attach_id += temp.getString("attach_id").toString();
						}
						msg.what = 2;
						handler.sendMessage(msg);
					} else {
						msg.what = 0;
						handler.sendMessage(msg);
					}

				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public boolean isNetworkConnected() {
		try {
			cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		NetworkInfo ni = cm.getActiveNetworkInfo();
		return ni != null && ni.isConnectedOrConnecting();
	}

	private View.OnClickListener rightlistener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			Message msg = handler.obtainMessage();
			tt = note.getText().toString();
			((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
					.hideSoftInputFromWindow(
							getCurrentFocus().getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);
			if (isNetworkConnected()) {
				if (share.getInt("ISMEMBER", 0) != 0) {
					if (tid == 1) {
						if (hs.getChildCount() > 0) {
							if (hs.getChildCount() > 5) {
								Toast.makeText(ShareEditActivity.this,
										getString(R.string.more_five_pic),
										Toast.LENGTH_SHORT).show();
							} else {
								// dialog = initProgressDialog();
								new Thread(new uploadImg()).start();
								ShareEditActivity.this.finish();
							}
						} else {
							msg.what = 0;
							handler.sendMessage(msg);
						}
					} else if (tid == 0) {

						if (tt != null && tt.length() > 0) {
							// dialog = initProgressDialog();
							new Thread(new send()).start();
							ShareEditActivity.this.finish();
						} 
					} else if (tid == 2) {
						// dialog = initProgressDialog();
						new Thread(new setMessage()).start();
						ShareEditActivity.this.finish();
					} else if (tid == 3) {
						// dialog = initProgressDialog();
						new Thread(new uploadImg()).start();
						ShareEditActivity.this.finish();
					} else if (tid == 4) {
						// dialog = initProgressDialog();
						new Thread(new send()).start();
						ShareEditActivity.this.finish();
					}
				} else {
					Toast.makeText(ShareEditActivity.this,
							getString(R.string.plogin), Toast.LENGTH_SHORT)
							.show();
				}
			} else {
				Toast.makeText(ShareEditActivity.this,
						getString(R.string.error_network), Toast.LENGTH_SHORT)
						.show();
			}

		}

	};

	public ProgressDialog initProgressDialog() {
		ProgressDialog progressDialog = ProgressDialog.show(this,
				getString(R.string.data_wait), getString(R.string.sendfeed),
				true);
		progressDialog.setCancelable(true);

		LayoutInflater inflater = LayoutInflater.from(this);
		View v = inflater.inflate(R.layout.loading, null);
		TextView tvMsg = (TextView) v.findViewById(R.id.tvTitle);

		if (tvMsg != null) {
			tvMsg.setText(getString(R.string.sendfeed));
		}
		progressDialog.setContentView(v);
		progressDialog
				.setOnCancelListener(new DialogInterface.OnCancelListener() {

					@Override
					public void onCancel(DialogInterface dialog) {
						// TODO Auto-generated method stub
					}
				});

		return progressDialog;
	}

	public static File output(Context context, String albumName, String filename) {
		File path = new File(context.getFilesDir().getPath() + "/" + filename);
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {

			File sdcardFile = new File(
					Environment
							.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
					albumName);
			if (!sdcardFile.exists()) {
				sdcardFile.mkdirs();
			}
			path = new File(sdcardFile.getPath() + "/" + filename);
		}
		return path;

	}

	public void showImages(View view) {
		if (hs.getChildCount() > 5) {
			Toast.makeText(ShareEditActivity.this,
					getString(R.string.del_five), Toast.LENGTH_SHORT).show();
		} else if (hs.getChildCount() == 5) {
			Toast.makeText(ShareEditActivity.this, getString(R.string.five),
					Toast.LENGTH_SHORT).show();
		} else {
			final CharSequence[] items = getResources().getTextArray(
					R.array.pic_from);
			AlertDialog.Builder dlg = new AlertDialog.Builder(mContext)
					.setTitle(R.string.picImage).setItems(items,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int item) {
									if (item == 1) {
										startActivityForResult(new Intent(
												ShareEditActivity.this,
												ImageFolderActivity.class),
												GALLERY);
									} else {
										sdcardTempFile = mediautils
												.getPicPath(
														HttpUtlis.TEMP_Folder,
														SystemClock
																.currentThreadTimeMillis()
																+ ".jpg");
										mediautils.invokeCamera(sdcardTempFile,
												CAMERA);
									}
								}
							});
			dlg.setNegativeButton(getString(R.string.close),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface d, int which) {
							// TODO Auto-generated method stub
							d.dismiss();
						}
					});
			dlg.create().show();
		}
	}

	File cropfile;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		// TODO Auto-generated method stub
		if (requestCode == GALLERY && resultCode == RESULT_OK) {
			String[] path = data.getExtras().getStringArray("all_path");
			if (path != null && path.length > 0) {
				for (String s : path) {
					if (picpaths.contains(s)) {
						continue;
					}
					final ImageView i = new ImageView(this);
					int width = dip2px(this, 48);
					int height = width;
					LinearLayout.LayoutParams params = new LayoutParams(width,
							height);
					params.rightMargin = dip2px(this, 2);
					i.setLayoutParams(params);
					Bitmap bm = getBitmapFromFile(new File(s), width, height);
					i.setImageBitmap(bm);
					pics.add(i);
					picpaths.add(s);
					i.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							AlertDialog.Builder aDailog = new AlertDialog.Builder(
									mContext);
							aDailog.setCancelable(false);
							aDailog.setTitle(R.string.del_yes_or_no);
							aDailog.setMessage(R.string.del_pic);
							aDailog.setPositiveButton(R.string.ok,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											pics.remove(i);
											picpaths.remove(hs.indexOfChild(i));
											hs.removeView(i);
											i.setVisibility(View.GONE);
										}
									});

							aDailog.setNegativeButton(R.string.cancel,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											// TODO Auto-generated method stub
											dialog.cancel();
										}
									});
							aDailog.show();

						}
					});
					hs.invalidate();

				}
				hs.removeAllViews();
				for (int j = 0; j <= pics.size() - 1; j++) {
					hs.addView(pics.get(j));
				}
			}
		} else if (requestCode == CAMERA && resultCode == RESULT_OK) {

			cropfile = mediautils.startPhotoZoom(Uri.fromFile(sdcardTempFile),
					720, 720, CROP);
			// Uri croppedImage = Uri.fromFile(sdcardTempFile);
			//
			// CropImageIntentBuilder cropImage = new
			// CropImageIntentBuilder(getResources().getDisplayMetrics().widthPixels,
			// getResources().getDisplayMetrics().widthPixels, croppedImage);
			// cropImage.setOutlineColor(0xFF03A9F4);
			// cropImage.setSourceImage(croppedImage);
			//
			// startActivityForResult(cropImage.getIntent(this), CROP);
		} else if (requestCode == CROP && resultCode == RESULT_OK) {
			if (data != null) {
				Bundle bundle = data.getExtras();
				// Intent it = new
				// Intent(ShareEditActivity.this,MovieMakerActivity.class);
				// it.putExtra("path",sdcardTempFile.getAbsolutePath());
				// startActivity(it);
				if (bundle != null) {
					Bitmap photo = bundle.getParcelable("data");
					final ImageView i = new ImageView(this);
					int width = dip2px(this, 48);
					int height = width;

					LinearLayout.LayoutParams params = new LayoutParams(width,
							height);
					params.rightMargin = dip2px(this, 2);
					i.setLayoutParams(params);
					i.setImageBitmap(photo);
					pics.add(i);
					picpaths.add(cropfile.getAbsolutePath());
					i.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							AlertDialog.Builder aDailog = new AlertDialog.Builder(
									mContext);
							aDailog.setCancelable(false);
							aDailog.setTitle(R.string.del_yes_or_no);
							aDailog.setMessage(R.string.del_pic);
							aDailog.setPositiveButton(R.string.ok,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											pics.remove(i);
											picpaths.remove(hs.indexOfChild(i));
											hs.removeView(i);
											i.setVisibility(View.GONE);
										}
									});

							aDailog.setNegativeButton(R.string.cancel,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											// TODO Auto-generated method stub
											dialog.cancel();
										}
									});
							aDailog.show();

						}
					});
					hs.removeAllViews();
					for (int j = 0; j <= pics.size() - 1; j++) {
						hs.addView(pics.get(j));
					}

				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	public Bitmap getBitmapFromFile(File dst, int width, int height) {
		if (null != dst && dst.exists()) {
			BitmapFactory.Options opts = null;
			if (width > 0 && height > 0) {
				opts = new BitmapFactory.Options(); // 璁剧疆inJustDecodeBounds涓簍rue鍚庯紝decodeFile骞朵笉鍒嗛厤绌洪棿锛屾鏃惰绠楀師濮嬪浘鐗囩殑闀垮害鍜屽搴?
				opts.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(dst.getPath(), opts);
				// 璁＄畻鍥剧墖缂╂斁姣斾緥
				final int minSideLength = Math.min(width, height);
				opts.inSampleSize = computeSampleSize(opts, minSideLength,
						width * height); // 杩欓噷涓€瀹氳灏嗗叾璁剧疆鍥瀎alse锛屽洜涓轰箣鍓嶆垜浠皢鍏惰缃垚浜唗rue
				opts.inJustDecodeBounds = false;
				opts.inInputShareable = true;
				opts.inPurgeable = true;
			}
			try {
				return BitmapFactory.decodeFile(dst.getPath(), opts);
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static int computeSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);

		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}

		return roundedSize;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;

		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
				.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
				Math.floor(w / minSideLength), Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}

		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_UP) {
			// setResult(1);
			ShareEditActivity.this.finish();
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub

	}

}

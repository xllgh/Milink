package com.bandlink.air;

//汉子未修改 
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ScrollView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bandlink.air.CreateClubActivity.ImageAdapter;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.DbContract.MyClub;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.MApplication;
import com.bandlink.air.util.SharePreUtils;

public class CreateMatchActivity extends LovefitActivity implements OnClickListener,
		OnCheckedChangeListener {
	Button timefrom, timeto, next;
	PopupWindow p;
	Calendar c;
	Gallery gallery;
	ImageView img;
	String resIds[];
	RadioGroup logo_group, join_group, standard_group, score_group;
	File sdcardTempFile;
	EditText matchName, cash;
	Button club;
	SharedPreferences share;
	Dbutils db;
	LinearLayout linear_club;
	ScrollView scroll;
	Context mContext;
	AlertDialog alertDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setContentView(R.layout.creatematch);

		if (android.os.Build.VERSION.SDK_INT >= 11) {
			mContext = new ContextThemeWrapper(this,
					android.R.style.Theme_Holo_Light);
		} else {
			mContext = this;
		}
		try {
			resIds = getResources().getAssets().list("sell/images/matchmode");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		scroll = (ScrollView) findViewById(R.id.scroll);
		share = getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		db = new Dbutils(share.getInt("UID", -1), this);
		ActionbarSettings actionbar = new ActionbarSettings(this,
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						MApplication.getInstance().clearAll();
						CreateMatchActivity.this.finish();
					}
				}, null);
		// actionbar.setTopRightIcon(R.drawable.more);
		actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
		// actionbar.setTopRightIcon(R.drawable.ic_share);
		actionbar.setTitle(R.string.ab_createrace);
		timefrom = (Button) findViewById(R.id.timefrom);
		timefrom.setOnClickListener(this);
		timeto = (Button) findViewById(R.id.timeto);
		timeto.setOnClickListener(this);
		next = (Button) findViewById(R.id.next);
		next.setOnClickListener(this);
		initViews();
		gallery.setAdapter(new ImageAdapter(this, resIds,
				"sell/images/matchmode/"));
		gallery.setSpacing(20);

		gallery.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				try {
					img.setBackgroundDrawable(BitmapDrawable.createFromStream(
							getResources().getAssets()
									.open("sell/images/matchmode/"
											+ resIds[position]),
							resIds[position]));
					// logoPath ="file:///"+"htmls/mode_img/" +
					// resIds[position];
				} catch (IOException e) {
					// TODO Auto-generated catch block
					img.setImageResource(R.drawable.no_media);
					e.printStackTrace();
				}
			}
		});

		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		MApplication.getInstance().getList().add(0, this);
		super.onResume();
	}

	ArrayList<HashMap<String, String>> al;
	public Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case 0:
				break;
			case 1:
				al = db.getMyClubFromDb(-1);
				if (al.size() < 1) {
					break;
				}
				alertDialog = initDialog();
				// club.setAdapter(new MSpinner(al, CreateMatchActivity.this));
				break;
			}
			super.handleMessage(msg);
		}

	};

	@SuppressWarnings("unchecked")
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.timefrom:

			timefrom.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.dropbox));
			timefrom.setTextColor(Color.WHITE);
			c = Calendar.getInstance();
			c.add(Calendar.DAY_OF_MONTH, 1);
			if (timefrom.getText().length() > 1) {
				try {
					String[] str = timefrom.getText().toString().split("\\.");
					c.set(Calendar.YEAR, Integer.parseInt(str[0]));
					c.set(Calendar.MONTH, Integer.parseInt(str[1]) - 1);
					c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(str[2]));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			new DatePickerDialog(mContext, new OnDateSetListener() {

				@Override
				public void onDateSet(DatePicker view, int year,
						int monthOfYear, int dayOfMonth) {
					// TODO Auto-generated method stub
					timefrom.setText(year + "." + (SlideMainActivity.turnNum(monthOfYear + 1))
							+ "." + SlideMainActivity.turnNum(dayOfMonth));
				}
			}, c.get(Calendar.YEAR), c.get(Calendar.MONTH),
					c.get(Calendar.DAY_OF_MONTH)).show();
			break;
		case R.id.timeto:
			timeto.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.dropbox));
			timeto.setTextColor(Color.WHITE);
			c = Calendar.getInstance();
			c.add(Calendar.DAY_OF_MONTH, 1);
			if (timeto.getText().length() > 1) {
				try {
					String[] str = timeto.getText().toString().split("\\.");
					c.set(Calendar.YEAR, Integer.parseInt(str[0]));
					c.set(Calendar.MONTH, Integer.parseInt(str[1]) - 1);
					c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(str[2]));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			new DatePickerDialog(mContext, new OnDateSetListener() {

				@Override
				public void onDateSet(DatePicker view, int year,
						int monthOfYear, int dayOfMonth) {
					// TODO Auto-generated method stub
					String mo = SlideMainActivity.turnNum(monthOfYear + 1);
					String da = SlideMainActivity.turnNum(dayOfMonth);
					if (timefrom.getText().length() <= 0) {
						Toast.makeText(CreateMatchActivity.this,
								getString(R.string.chose_starttime),
								Toast.LENGTH_SHORT).show();
						return;
					}
					if (Integer.parseInt(year + "" + mo + "" + da + "") < Integer
							.parseInt(timefrom.getText().toString()
									.replace(".", ""))) {
						timeto.setBackgroundDrawable(getResources()
								.getDrawable(R.drawable.corner_border_red));
						timeto.setText(year + "." + mo + "." + da);
						timeto.setTextColor(Color.RED);
					} else {
						timeto.setText(year + "." + mo + "." + da);
					}

				}
			}, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c
					.get(Calendar.DAY_OF_MONTH)).show();
			break;
		case R.id.next:
			// 检测名称
			if (matchName.getText().toString().length() < 1) {
				matchName.requestFocusFromTouch();
				matchName.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.corner_border_red));
				scroll.scrollTo(0, 0);
				break;
			}
			if (cash.getText().toString().length() < 1
					|| Integer.parseInt(cash.getText().toString()) <= 1) {
				// cash.requestFocusFromTouch();
				cash.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.corner_border_red));
				// break;
			}
			int ispublic = 0,
			score = 0,
			standard = 0;
			String clubid;
			long start = 0,
			end = 0;
			clubid = "-1";
			if (join_group.getCheckedRadioButtonId() != R.id.radio_alluser) {
				ispublic = 1;
				Object o = club.getTag();
				if (o == null) {
					ispublic = 0;

				} else {
					ispublic = 1;
					clubid = o.toString();
				}

				// clubid
				// =al.get(club.getSelectedItemPosition()).get(MyClub.CLUBID);
			}
			if (standard_group.getCheckedRadioButtonId() == R.id.radio_count) {
				standard = 1;
			} else if (standard_group.getCheckedRadioButtonId() == R.id.radio_frist) {
				standard = 2;
			}
			if (score_group.getCheckedRadioButtonId() == R.id.radio_top3) {
				score = 1;
			} else if (score_group.getCheckedRadioButtonId() == R.id.radio_top10) {
				score = 2;
			}
			if (timefrom.getText().length() < 1) {
				timefrom.setBackgroundResource(R.drawable.corner_border_red);
				break;
			}
			if (timeto.getText().length() < 1) {
				timeto.setBackgroundResource(R.drawable.corner_border_red);
				break;
			}
			int from = Integer.parseInt(timefrom.getText().toString()
					.replace(".", ""));
			int to = Integer.parseInt(timeto.getText().toString()
					.replace(".", ""));
			SimpleDateFormat s = new SimpleDateFormat("yyyyMMdd");
			String now = s.format(new Date());
			if (from <= Integer.parseInt(now)) {
				timefrom.setBackgroundResource(R.drawable.corner_border_red);
				timefrom.setTextColor(Color.RED);
				Toast.makeText(CreateMatchActivity.this,
						getString(R.string.nottoday), Toast.LENGTH_SHORT)
						.show();
				break;
			}
			if (to < from || to <= Integer.parseInt(now)) {
				timeto.setBackgroundResource(R.drawable.corner_border_red);
				timeto.setTextColor(Color.RED);
				break;
			}

			try {
				start = s.parse(from + "").getTime() / 1000;
				end = s.parse(to + "").getTime() / 1000;
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			ArrayList<String> data = new ArrayList<String>();
			data.add(0, matchName.getText().toString());
			data.add(1, cash.getText().toString());
			data.add(2, clubid + "");
			data.add(3, score + "");
			data.add(4, standard + "");
			data.add(5, end + "");
			data.add(6, ispublic + "");
			data.add(7, start + "");
			Intent i = new Intent(this, CreateMatchStep2.class);

			i.putStringArrayListExtra("data", data);
			byte[] bs = Bitmap2Bytes(((BitmapDrawable) img.getBackground())
					.getBitmap());
			i.putExtra("logo", bs);

			startActivity(i);
			break;
		case R.id.et_clubname:

			matchName.setBackgroundResource(R.drawable.edit_border_selector);

			break;
		case R.id.cash:

			cash.setBackgroundResource(R.drawable.edit_border_selector);

			break;
		case R.id.btn_add:
			if (alertDialog != null && !alertDialog.isShowing()) {
				alertDialog.setTitle(R.string.selectclub);
				alertDialog.show();
			}
			break;

		}
	}

	protected AlertDialog initDialog() {
		// TODO Auto-generated method stub
		AlertDialog.Builder ab = new AlertDialog.Builder(mContext);
		ab.setAdapter(new MSpinner(al, CreateMatchActivity.this),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						club.setText(al.get(which).get(MyClub.NAME));
						club.setTag(al.get(which).get(MyClub.CLUBID));
					}
				});
		return ab.create();
	}

	 
	/***
	 * bitmap to byte[]
	 * 
	 * @param bm
	 * @return
	 */
	public static byte[] Bitmap2Bytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	public void getMyClub() {
		new Thread(new Runnable() {

			@Override
			public void run() {

				// TODO Auto-generated method stub
				Map<String, String> my = new HashMap<String, String>();
				my.put("session", share.getString("session_id", ""));
				try {
					String result = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/data/getMyClub", my);
					JSONArray json = new JSONObject(result)
							.getJSONArray("content");
					for (int i = 0; i < json.length(); i++) {
						JSONObject jobj = json.getJSONObject(i);

						db.UpdateMyClub(
								jobj.getString("id"),
								jobj.getString("name"),
								jobj.getString("membernum"),
								jobj.getString("ispublic"),
								jobj.getString("provicne"),
								jobj.getString("city"),
								jobj.getString("district"),
								HttpUtlis.CLUBLOGO_URL
										+ HttpUtlis.getLogoUrl(jobj
												.getString("id")));
					}

					handler.sendEmptyMessage(1);

				} catch (Exception e) {
					// TODO Auto-generated catch block

					handler.sendEmptyMessage(1);

					e.printStackTrace();
				}
			}

		}).start();
	}

	private void initViews() {
		club = (Button) findViewById(R.id.btn_add);
		getMyClub();
		linear_club = (LinearLayout) findViewById(R.id.linear_club);
		matchName = (EditText) findViewById(R.id.et_clubname);
		matchName.setOnClickListener(this);
		cash = (EditText) findViewById(R.id.cash);
		cash.setOnClickListener(this);
		gallery = (Gallery) findViewById(R.id.gallery1);
		MarginLayoutParams mlp = (MarginLayoutParams) gallery.getLayoutParams();
		int offset = (getResources().getDisplayMetrics().widthPixels / 2);
		mlp.setMargins(-offset, mlp.topMargin, mlp.rightMargin,
				mlp.bottomMargin);
		img = (ImageView) findViewById(R.id.img_mode);
		logo_group = (RadioGroup) findViewById(R.id.logo_group);
		join_group = (RadioGroup) findViewById(R.id.join_group);
		join_group.setOnCheckedChangeListener(this);
		standard_group = (RadioGroup) findViewById(R.id.radio_standard);
		score_group = (RadioGroup) findViewById(R.id.radio_score);
		logo_group.setOnCheckedChangeListener(this);
		club.setOnClickListener(this);
		try {
			img.setBackgroundDrawable(BitmapDrawable.createFromStream(
					getResources().getAssets().open(
							"sell/images/matchmode/" + resIds[0]), resIds[0]));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			img.setImageResource(R.drawable.no_media);
			e.printStackTrace();
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		// TODO Auto-generated method stub
		switch (checkedId) {
		case R.id.radio_img:
			gallery.setVisibility(View.GONE);
			getPhoto();
			break;
		case R.id.radio_mode:
			gallery.setVisibility(View.VISIBLE);
			break;
		case R.id.radio_alluser:
			linear_club.setVisibility(View.GONE);

			break;
		case R.id.radio_club:
			linear_club.setVisibility(View.VISIBLE);
			break;
		}
	}

	public void getPhoto() {
		File f = new File(Environment.getExternalStorageDirectory().getPath()
				+ HttpUtlis.TEMP_Folder);
		if (!f.exists()) {
			f.mkdirs();
		}
		sdcardTempFile = new File(Environment.getExternalStorageDirectory()
				.getPath() + HttpUtlis.TEMP_Folder, "tmp_pic_"
				+ SystemClock.currentThreadTimeMillis() + ".jpg");
		final CharSequence[] items = { getString(R.string.chose_image),
				getString(R.string.chose_photo) };
		AlertDialog dlg = new AlertDialog.Builder(mContext)
				.setTitle(getString(R.string.pic))
				.setItems(items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						if (item == 1) {
							Intent intent = new Intent(
									MediaStore.ACTION_IMAGE_CAPTURE);
							intent.putExtra(MediaStore.EXTRA_OUTPUT,
									Uri.fromFile(sdcardTempFile));
							startActivityForResult(intent, 1);

						} else {
							Intent intent = new Intent(Intent.ACTION_PICK, null);
							intent.setDataAndType(
									MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
									"image/*");
							startActivityForResult(intent, 2);
						}
					}
				}).create();
		dlg.show();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		switch (requestCode) {
		case 1:
			startPhotoZoom(Uri.fromFile(sdcardTempFile));
			break;
		case 2:
			if (data != null)
				startPhotoZoom(data.getData());
			break;
		case 3:
			System.out.println(sdcardTempFile.getPath());
			if (data == null) {
				break;
			}
			Bundle bundle = data.getExtras();
			if (bundle != null) {
				Bitmap p = bundle.getParcelable("data");
				if (p != null) {
					img.setBackgroundDrawable(new BitmapDrawable(p));
				} else {
					if (!sdcardTempFile.exists()) {
						FileOutputStream out;
						try {

							out = new FileOutputStream(sdcardTempFile);
							p.compress(Bitmap.CompressFormat.JPEG, 100, out);
							out.flush();
							out.close();
						} catch (Exception e) {
							e.printStackTrace();
							img.setBackgroundDrawable(BitmapDrawable
									.createFromPath(sdcardTempFile.getPath()));
						}
					}

				}

			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void startPhotoZoom(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 4);
		intent.putExtra("aspectY", 2);
		intent.putExtra("outputX", 400);
		intent.putExtra("outputY", 200);
		intent.putExtra("return-data", true);
		intent.putExtra("scale", true);
		intent.putExtra("scaleUpIfNeeded", true);
		startActivityForResult(intent, 3);
	}

	class MSpinner extends BaseAdapter implements SpinnerAdapter {

		ArrayList<HashMap<String, String>> list;
		Context context;

		public MSpinner(ArrayList<HashMap<String, String>> list, Context context) {
			super();
			this.list = list;
			this.context = context;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return Long.parseLong(list.get(position).get(MyClub.CLUBID));
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.item_club_group, null);
			TextView name = (TextView) convertView.findViewById(R.id.name);
			name.setText(list.get(position).get(MyClub.NAME));
			return convertView;
		}

	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub
		
	}
}

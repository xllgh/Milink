package com.bandlink.air;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.bandlink.air.club.BorderImageView;
import com.bandlink.air.pingan.PinganScorePanel;
import com.bandlink.air.user.LoginActivity;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.AsynImagesLoader;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.MediaUtils;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Util;
import com.bandlink.air.view.HeightRuler;
import com.bandlink.air.view.NoRegisterDialog;
import com.bandlink.air.view.WeightRuler;
import com.nostra13.universalimageloader.core.ImageLoader;

public class PersonalSettings extends LovefitActivity implements OnClickListener,
		OnEditorActionListener {
	private File sdcardTempFile;
	private TextView etHeight, etWeight, etUsername, etName, et_email,
			et_phone, et_goal;
	private Object[] data;
	private Dbutils db;
	private double weight, reweight;
	private double height, reheight;
	private String nickname;
	private Button finish;
	private RadioGroup change;
	private BorderImageView photo;
	private RadioButton sex_male, sex_female;
	private int tag1 = 2;
	private SharedPreferences share;
	private AsynImagesLoader imageloader;
	private MediaUtils mediautils;
	private Boolean isAvatar = false;
	private NoRegisterDialog dialog;
	private int ismember;
	private ProgressDialog progressdialog;
	private Context mContext;
	private TextView birth, tv_id;
	private RelativeLayout linear_psd;
	String on = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.personalsetting);
		ActionbarSettings actionbar = new ActionbarSettings(this, lsl, null);
		actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
		actionbar.setTitle(R.string.set_person);
		share = getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		 
			mContext = Util.getThemeContext(this);

		db = new Dbutils(share.getInt("UID", -1), this);
		mediautils = new MediaUtils(this);
		sdcardTempFile = mediautils.getPicPath(
				HttpUtlis.TEMP_Folder + "avatar", " myAvatar.jpg");
		initViews();
		photo = (BorderImageView) findViewById(R.id.photo);
		imageloader = AsynImagesLoader.getInstance(this, false);
		ImageLoader.getInstance().displayImage(HttpUtlis.AVATAR_URL+
				MenuFragment.getAvatarUrl(share.getInt("UID", -1) + "","big"), photo,MilinkApplication.getListOptions());
		ismember = share.getInt("ISMEMBER", 0);
		if (ismember == 0) {
			LinearLayout l1 = (LinearLayout) findViewById(R.id.linear1);
			LinearLayout l2 = (LinearLayout) findViewById(R.id.linear2);
			LinearLayout l3 = (LinearLayout) findViewById(R.id.linear3);
			LinearLayout l4 = (LinearLayout) findViewById(R.id.linear_s);
			finish.setVisibility(View.VISIBLE);
			l1.setVisibility(View.GONE);
			l2.setVisibility(View.GONE);
			l4.setVisibility(View.GONE);
			l3.setVisibility(View.GONE);
			dialog = new NoRegisterDialog(PersonalSettings.this,
					R.string.no_register, R.string.no_register_content, true);
			dialog.show();
		}

	}

	private void initViews() {
		finish = (Button) findViewById(R.id.finish);
		if (getIntent().getStringExtra("value") != null) {
			finish.setVisibility(View.VISIBLE);
		}
		change = (RadioGroup) findViewById(R.id.change);
		sex_female = (RadioButton) findViewById(R.id.sex_female);
		sex_male = (RadioButton) findViewById(R.id.sex_male);
		etName = (TextView) findViewById(R.id.nickname);
		etName.setOnClickListener(this);
		if (Locale.getDefault().getLanguage().equals("zh")) {

		} else {
			sex_female.setGravity(Gravity.CENTER);
			sex_male.setGravity(Gravity.CENTER);
			sex_female.setPadding(50, 0, 50, 0);
			sex_male.setPadding(50, 0, 50, 0);

		}

		tv_id = (TextView) findViewById(R.id.tv_id);
		change.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				if (checkedId == R.id.sex_female) {
					sex_female.setChecked(true);
					sex_female.setTextColor(getResources().getColor(
							R.color.white));
					sex_male.setTextColor(getResources()
							.getColor(R.color.white_0_8));
					tag1 = 2;
					updateSex(tag1);
				} else if (checkedId == R.id.sex_male) {
					sex_male.setChecked(true);
					sex_male.setTextColor(getResources().getColor(R.color.white));
					sex_female.setTextColor(getResources().getColor(
							R.color.white_0_8));
					tag1 = 1;
					updateSex(tag1);
				}
			}
		});
		et_email = (TextView) findViewById(R.id.et_email);
		et_email.setOnClickListener(this);
		et_email.setOnEditorActionListener(this);
		et_phone = (TextView) findViewById(R.id.et_phone);
		et_phone.setOnClickListener(this);
		et_goal = (TextView) findViewById(R.id.et_step);
		et_goal.setOnClickListener(this);
		et_phone.setOnEditorActionListener(this);
		linear_psd = (RelativeLayout) findViewById(R.id.linear_psd);
		linear_psd.setOnClickListener(this);

		birth = (TextView) findViewById(R.id.birth);
		etHeight = (TextView) findViewById(R.id.et_height);
		etUsername = (TextView) findViewById(R.id.username);
		etWeight = (TextView) findViewById(R.id.et_weight);
		finish.setOnClickListener(this);
		etHeight.setOnClickListener(this);
		etWeight.setOnClickListener(this);
		birth.setOnClickListener(this);

		data = db.getUserProfile();

		if (data != null) {
			if (data[9] != null) {
				tv_id.setText(data[9].toString());
			} else {
				tv_id.setText("");
			}
			if (data[10] != null) {
				birth.setText(data[10].toString());
			} else {
				int year = share.getInt("year", 1990);
				int month = share.getInt("month", 1);
				birth.setText(year + getString(R.string.year) + (month)
						+ getString(R.string.month));
			}
			if (data[7] != null & (data[7]).toString().length() > 0) {
				et_phone.setText(data[7].toString());
			} else {
				et_phone.setText(getString(R.string.not_setting));
			}
			if (data[8] != null & (data[8]).toString().length() > 0) {
				et_email.setText(data[8].toString());
			} else {
				et_email.setText(getString(R.string.not_setting));

			}
			if (data[11] != null) {
				etUsername.setText(data[11].toString());
			} else {

			}
			if (on.equals("")) {
				height = (Double) data[5];
				weight = (Double) data[6];
				if ((int) (height) == 0) {
					height = 170;
				}
				if ((int) (weight) == 0) {
					weight = 60.0;
				}
				etHeight.setText((int) height
						+ getString(R.string.unit_distance_cm));
				etWeight.setText(weight + getString(R.string.unit_kg));
			}
			try {
				if ((CharSequence) data[2] == null) {
					etName.setText(getString(R.string.not_setting));

				} else {
					etName.setText((CharSequence) data[2]);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		int sex = 1;
		try {
			sex = (Integer) data[4];
		} catch (Exception e) {
			sex = 1;
		}
		if (sex == 1) {
			sex_male.setChecked(true);
			sex_male.setTextColor(getResources().getColor(R.color.white));
			sex_female.setTextColor(getResources().getColor(R.color.white_0_8));
			sex_female.setChecked(false);
			tag1 = 1;

		} else if (sex == 2) {
			sex_male.setChecked(false);
			sex_female.setChecked(true);
			sex_female.setTextColor(getResources().getColor(R.color.white));
			sex_male.setTextColor(getResources().getColor(R.color.white_0_8));
			tag1 = 2; 
		}
		Object[] target = db.getUserTarget();
		if (target != null) {
			et_goal.setText((Integer) target[3] + getString(R.string.unit_step));
		}

	}

	@SuppressLint("NewApi") public void onClick(View v) {
		etName.clearFocus();
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.linear_psd:
			AlertDialog.Builder psdDialog = new AlertDialog.Builder(mContext);
			LayoutInflater lay = LayoutInflater.from(mContext);
			View view = lay.inflate(R.layout.update_psd, null);
			psdDialog.setView(view);
			final EditText or_pwd = (EditText) view
					.findViewById(R.id.et_or_pwd);
			final EditText pwd_1 = (EditText) view.findViewById(R.id.et_pwd_1);
			final EditText pwd_2 = (EditText) view.findViewById(R.id.et_pwd_2);
			psdDialog.setTitle(R.string.update_pwd);
			psdDialog.setCancelable(false);
			psdDialog.setPositiveButton(R.string.ok,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

							String pwd = or_pwd.getText().toString();
							String pwd_new1 = pwd_1.getText().toString();
							String pwd_new2 = pwd_2.getText().toString();
							if (pwd.length() > 25 || pwd.length() < 6) {
								or_pwd.setBackgroundResource(R.drawable.corner_border_red);
								or_pwd.requestFocusFromTouch();
								Toast.makeText(mContext,
										R.string.accmanage_pwd_erro,
										Toast.LENGTH_SHORT).show();
								try {
									Field field = dialog.getClass()
											.getSuperclass()
											.getDeclaredField("mShowing");
									field.setAccessible(true);
									field.set(dialog, false);

								} catch (Exception e) {
									e.printStackTrace();
								}

								return;
							} else if (pwd_new1.length() > 25
									|| pwd_new1.length() < 6) {
								pwd_1.setBackgroundResource(R.drawable.corner_border_red);
								pwd_1.requestFocusFromTouch();
								Toast.makeText(PersonalSettings.this,
										R.string.accmanage_pwd_erro,
										Toast.LENGTH_SHORT).show();
								try {
									Field field = dialog.getClass()
											.getSuperclass()
											.getDeclaredField("mShowing");
									field.setAccessible(true);
									field.set(dialog, false);

								} catch (Exception e) {
									e.printStackTrace();
								}
								return;
							} else if (pwd_new2.length() > 25
									|| pwd_new2.length() < 6) {
								pwd_2.setBackgroundResource(R.drawable.corner_border_red);
								pwd_2.requestFocusFromTouch();
								Toast.makeText(PersonalSettings.this,
										R.string.accmanage_pwd_erro,
										Toast.LENGTH_SHORT).show();
								try {
									Field field = dialog.getClass()
											.getSuperclass()
											.getDeclaredField("mShowing");
									field.setAccessible(true);
									field.set(dialog, false);

								} catch (Exception e) {
									e.printStackTrace();
								}
								return;
							} else if (!pwd_new1.equals(pwd_new2)) {
								pwd_2.setBackgroundResource(R.drawable.corner_border_red);
								Toast.makeText(PersonalSettings.this,
										R.string.confirm_error,
										Toast.LENGTH_SHORT).show();
								pwd_2.requestFocusFromTouch();
								Toast.makeText(PersonalSettings.this,
										R.string.accmanage_pwd_erro1,
										Toast.LENGTH_SHORT).show();
								try {
									Field field = dialog.getClass()
											.getSuperclass()
											.getDeclaredField("mShowing");
									field.setAccessible(true);
									field.set(dialog, false);

								} catch (Exception e) {
									e.printStackTrace();
								}
								return;
							} else {
								progressdialog = initProgressDialog();
								editUserPwd(pwd, pwd_new1);
								try {
									Field field = dialog.getClass()
											.getSuperclass()
											.getDeclaredField("mShowing");
									field.setAccessible(true);
									field.set(dialog, true);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
					});

			psdDialog.setNegativeButton(R.string.cancel,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							try {
								Field field = dialog.getClass().getSuperclass()
										.getDeclaredField("mShowing");
								field.setAccessible(true);
								field.set(dialog, true);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
			psdDialog.create().show();
			break;
		case R.id.et_height:
			Intent intent5 = new Intent();
			intent5.setClass(PersonalSettings.this, HeightRuler.class);
			intent5.putExtra("height", height);
			intent5.putExtra("uheight", etHeight.getText().toString()
					.substring(0, etHeight.getText().toString().length() - 2));
			this.startActivityForResult(intent5, 5);

			break;
		case R.id.et_step:
			final String[] Items = getResources().getStringArray(
					R.array.distance);
			AlertDialog.Builder ab1 = new AlertDialog.Builder(mContext);
			ab1.setTitle(getString(R.string.set_day_step));
			ab1.setItems(Items, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface d, int which) {
					// TODO Auto-generated method stub
					et_goal.setText(Items[which]
							+ getString(R.string.unit_step));

					UploadTargetParams p = new UploadTargetParams("3",
							Items[which]);
					p.start();
					db.setTargetStep(Integer.parseInt(Items[which]));
				}
			});
			ab1.setNegativeButton(getString(R.string.close),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							dialog.dismiss();
						}
					});
			ab1.create().show();
			break;
		case R.id.et_weight:
			Intent intent4 = new Intent();
			intent4.setClass(PersonalSettings.this, WeightRuler.class);
			intent4.putExtra("startEdit1", weight);
			intent4.putExtra("title", getString(R.string.weight_ruler_title3));
			intent4.putExtra("start_w", etWeight.getText().toString()
					.substring(0, etWeight.getText().toString().length() - 2));
			this.startActivityForResult(intent4, 4);
			break;
		case R.id.nickname:
			AlertDialog.Builder nickDialog = new AlertDialog.Builder(mContext);
			LayoutInflater lay_nick = LayoutInflater.from(mContext);
			View vi = lay_nick.inflate(R.layout.edit, null);
			nickDialog.setView(vi);
			final EditText nick = (EditText) vi.findViewById(R.id.nick);
			nick.setFocusable(true);
			nick.setFocusableInTouchMode(true);
			nick.requestFocus();
			if (etName.getText().toString()
					.equals(getString(R.string.not_setting))) {
				nick.setText(null);
			} else {
				nick.setText(etName.getText().toString());
				nick.setSelection(etName.getText().toString().length());
			}
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
							.toggleSoftInput(0,
									InputMethodManager.HIDE_NOT_ALWAYS);
				}
			}, 100);
			nickDialog.setTitle(R.string.update_nick);
			nickDialog.setPositiveButton(R.string.ok,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog1, int which) {
							if (share.getInt("ISMEMBER", 0) == 0) {
								dialog.show();
							} else if (nick.getText().toString().length() < 2
									|| nick.getText().toString().length() > 25) {
								Toast.makeText(PersonalSettings.this,
										R.string.personal_setting_nickerro,
										Toast.LENGTH_SHORT).show();
								try {
									Field field = dialog1.getClass()
											.getSuperclass()
											.getDeclaredField("mShowing");
									field.setAccessible(true);
									field.set(dialog1, false);
								} catch (Exception e) {
									e.printStackTrace();
								}
							} else if (nick
									.getText()
									.toString()
									.contains(
											getString(R.string.lovefit_official))) {
								Toast.makeText(PersonalSettings.this,
										R.string.lovefit_official_erro,
										Toast.LENGTH_SHORT).show();
								try {
									Field field = dialog1.getClass()
											.getSuperclass()
											.getDeclaredField("mShowing");
									field.setAccessible(true);
									field.set(dialog1, false);
								} catch (Exception e) {
									e.printStackTrace();
								}
							} else {

								updateNick(nick.getText().toString());
								etName.setText(nick.getText().toString());
								try {
									Field field = dialog1.getClass()
											.getSuperclass()
											.getDeclaredField("mShowing");
									field.setAccessible(true);
									field.set(dialog1, true);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
					});

			nickDialog.setNegativeButton(R.string.cancel,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog1, int which) {
							// TODO Auto-generated method stub
							try {
								Field field = dialog1.getClass()
										.getSuperclass()
										.getDeclaredField("mShowing");
								field.setAccessible(true);
								field.set(dialog1, true);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
			nickDialog.create().show();
			break;
		case R.id.et_phone:
			AlertDialog.Builder phoneDialog = new AlertDialog.Builder(mContext);
			LayoutInflater lay_phone = LayoutInflater.from(mContext);
			View viphone = lay_phone.inflate(R.layout.edit, null);
			phoneDialog.setView(viphone);
			final EditText pnone = (EditText) viphone.findViewById(R.id.nick);
			pnone.setFocusable(true);
			pnone.setFocusableInTouchMode(true);
			pnone.requestFocus();
			if (et_phone.getText().toString()
					.equals(getString(R.string.not_setting))) {
				pnone.setText(null);
			} else {
				pnone.setText(et_phone.getText().toString());
				pnone.setSelection(et_phone.getText().toString().length());
			}

			Timer timer1 = new Timer();
			timer1.schedule(new TimerTask() {

				@Override
				public void run() {
					((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
							.toggleSoftInput(0,
									InputMethodManager.HIDE_NOT_ALWAYS);
				}
			}, 100);

			phoneDialog.setTitle(R.string.update_mobile);
			phoneDialog.setPositiveButton(R.string.ok,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog1, int which) {
							String ph = pnone.getText().toString().trim();
							if (share.getInt("ISMEMBER", 0) == 0) {
								dialog.show();
							} else if (ph != null && ph.length() > 0) {
								Pattern p = Pattern.compile("[0-9]*");
								Matcher m = p.matcher(ph);

								if ((!ph.substring(0, 1).equals("1"))
										|| (ph.length() != 11) || (!m.matches())) {
									et_phone.requestFocusFromTouch();
									Toast.makeText(PersonalSettings.this,
											R.string.accmanage_tel_erro,
											Toast.LENGTH_SHORT).show();
									try {
										Field field = dialog1.getClass()
												.getSuperclass()
												.getDeclaredField("mShowing");
										field.setAccessible(true);
										field.set(dialog1, false);
									} catch (Exception e) {
										e.printStackTrace();
									}

								} else {
									updateMobile(ph);
									et_phone.setText(ph);

									try {
										Field field = dialog1.getClass()
												.getSuperclass()
												.getDeclaredField("mShowing");
										field.setAccessible(true);
										field.set(dialog1, true);
									} catch (Exception e) {
										e.printStackTrace();
									}

								}
							}
						}
					});

			phoneDialog.setNegativeButton(R.string.cancel,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog1, int which) {
							// TODO Auto-generated method stub
							try {
								Field field = dialog1.getClass()
										.getSuperclass()
										.getDeclaredField("mShowing");
								field.setAccessible(true);
								field.set(dialog1, true);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
			phoneDialog.create().show();
			break;
		case R.id.et_email:
			AlertDialog.Builder emailDialog = new AlertDialog.Builder(mContext);
			LayoutInflater lay_email = LayoutInflater.from(mContext);
			View viemail = lay_email.inflate(R.layout.edit, null);
			emailDialog.setView(viemail);
			final EditText email = (EditText) viemail.findViewById(R.id.nick);
			email.setFocusable(true);
			email.setFocusableInTouchMode(true);
			email.requestFocus();
			if (et_email.getText().toString()
					.equals(getString(R.string.not_setting))) {
				email.setText(null);
			} else {
				email.setText(et_email.getText().toString());
				email.setSelection(et_email.getText().toString().length());
			}
			Timer timer2 = new Timer();
			timer2.schedule(new TimerTask() {

				@Override
				public void run() {
					((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
							.toggleSoftInput(0,
									InputMethodManager.HIDE_NOT_ALWAYS);
				}
			}, 100);
			emailDialog.setTitle(R.string.update_email);
			emailDialog.setPositiveButton(R.string.ok,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog1, int which) {
							String em = email.getText().toString().trim();
							if (share.getInt("ISMEMBER", 0) == 0) {
								dialog.show();
							} else if (email != null && email.length() > 0) {
								if (!isEmail(em)) {
									et_email.requestFocusFromTouch();
									Toast.makeText(PersonalSettings.this,
											R.string.accmanage_email_erro,
											Toast.LENGTH_SHORT).show();

									try {
										Field field = dialog1.getClass()
												.getSuperclass()
												.getDeclaredField("mShowing");
										field.setAccessible(true);
										field.set(dialog1, false);
									} catch (Exception e) {
										e.printStackTrace();
									}

								} else {
									updateEmail(em);
									et_email.setText(em);

									try {
										Field field = dialog1.getClass()
												.getSuperclass()
												.getDeclaredField("mShowing");
										field.setAccessible(true);
										field.set(dialog1, true);
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}
						}
					});

			emailDialog.setNegativeButton(R.string.cancel,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog1, int which) {
							// TODO Auto-generated method stub
							try {
								Field field = dialog1.getClass()
										.getSuperclass()
										.getDeclaredField("mShowing");
								field.setAccessible(true);
								field.set(dialog1, true);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
			emailDialog.create().show();
			break;
		case R.id.finish:
			if (share.getInt("ISMEMBER", 0) == 0) {
				dialog.show();
			} else if (etName.getText().toString().length() < 2
					|| etName.getText().toString().length() > 25) {
				Toast.makeText(PersonalSettings.this,
						R.string.personal_setting_nickerro, Toast.LENGTH_SHORT)
						.show();

			} else if (etName.getText().toString()
					.contains(getString(R.string.lovefit_official))) {
				Toast.makeText(PersonalSettings.this,
						R.string.lovefit_official_erro, Toast.LENGTH_SHORT)
						.show();

			} else {
				progressdialog = initProgressDialog();
				nickname = etName.getText().toString();
				reheight = Double.valueOf(etHeight
						.getText()
						.toString()
						.substring(0,
								etHeight.getText().toString().length() - 2));
				reweight = Double.valueOf(etWeight
						.getText()
						.toString()
						.substring(0,
								etWeight.getText().toString().length() - 2));
				SimpleDateFormat sformat = new SimpleDateFormat("yyyy-MM-dd");
				String time = sformat.format(new Date());
				// if (isAvatar) {
				// submitAvatar();
				// }
				// upLoadUserInfo();
				if (getIntent().getIntExtra("iti", 0) != 0) {
					startActivity(new Intent().setClass(PersonalSettings.this,
							SlideMainActivity.class));
				}
				PersonalSettings.this.finish();

			}
			break;

		case R.id.birth:
			int year = share.getInt("year", 1990);
			int month = share.getInt("month", 1);
			final DatePicker date = new DatePicker(mContext);
			date.init(year, month - 1, 1, null);
			PinganScorePanel.hidePickerDay(date);
			AlertDialog.Builder ab = new AlertDialog.Builder(mContext);
			ab.setView(date);
			ab.setTitle(R.string.selectbrith);
			ab.setPositiveButton(R.string.finish,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub

							share.edit().putInt("year", date.getYear())
									.commit();
							share.edit().putInt("month", date.getMonth() + 1)
									.commit();
							birth.setText(date.getYear()
									+ getString(R.string.year)
									+ (date.getMonth() + 1)
									+ getString(R.string.month));
							updateBirth(date.getYear(), date.getMonth() + 1);
							dialog.dismiss();
						}
					});
			ab.create().show();

			break;
		default:
			break;
		}
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

	private void updateHeight() {
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

	private void updateEmail(final String em) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Map<String, String> up = new HashMap<String, String>();
				up.put("session", share.getString("session_id", ""));
				up.put("email", em + "");
				try {
					String result = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/user/setUserEmail", up);
					JSONObject ob = new JSONObject(result);
					if (ob.getInt("status") == 0) {
						db.UpdateUserEmail(em);
						myHandler.obtainMessage(4).sendToTarget();
					} else if (ob.getInt("status") == 3) {
						myHandler.obtainMessage(6).sendToTarget();
					} else if (ob.getInt("status") == 4) {
						myHandler.obtainMessage(7).sendToTarget();
					} else {

						myHandler.obtainMessage(3).sendToTarget();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void updateMobile(final String phone) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Map<String, String> up = new HashMap<String, String>();
				up.put("session", share.getString("session_id", ""));
				up.put("mobile", phone);
				try {
					String result = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/user/setUserMobile", up);
					JSONObject ob = new JSONObject(result);
					if (ob.getInt("status") == 0) {
						db.UpdateUserContact(phone);
						myHandler.obtainMessage(4).sendToTarget();
					} else if (ob.getInt("status") == 3) {
						myHandler.obtainMessage(6).sendToTarget();
					} else if (ob.getInt("status") == 4) {
						myHandler.obtainMessage(7).sendToTarget();
					} else {

						myHandler.obtainMessage(3).sendToTarget();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
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

	private void updateWeight() {
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

	public void editUserPwd(final String oldpwd, final String newpwd) {

		new Thread() {
			public void run() {
				Map<String, String> params = new HashMap<String, String>();
				params.put("session", share.getString("session_id", ""));
				params.put("oldpwd", LoginActivity.MD5(oldpwd));
				params.put("newpwd", LoginActivity.MD5(newpwd));
				try {
					String result = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/user/editUserPassword", params);
					JSONObject ob = new JSONObject(result);
					if (ob.getInt("status") == 2) {
						myHandler.obtainMessage(2).sendToTarget();
					} else if (ob.getInt("status") == 0) {
						myHandler.obtainMessage(0).sendToTarget();
					} else {
						myHandler.obtainMessage(1).sendToTarget();
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					myHandler.obtainMessage(3).sendToTarget();
					e.printStackTrace();
				}

			}

		}.start();
	}

	public ProgressDialog initProgressDialog() {
		ProgressDialog progressDialog = ProgressDialog.show(this,
				getString(R.string.data_wait),
				getString(R.string.data_getting), true);
		progressDialog.setCancelable(true);

		LayoutInflater inflater = LayoutInflater.from(this
				.getApplicationContext());
		View v = inflater.inflate(R.layout.loading, null);// 寰楀埌鍔犺浇view
		TextView tvMsg = (TextView) v.findViewById(R.id.tvTitle);

		if (tvMsg != null) {
			tvMsg.setText(getString(R.string.data_getting));
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

	private void upLoadUserInfo() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Map<String, String> up = new HashMap<String, String>();
				up.put("session", share.getString("session_id", ""));
				up.put("nickname", nickname);
				up.put("sex", tag1 + "");
				up.put("weight", reweight + "");
				up.put("height", reheight + "");
				try {
					String result = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/user/setUserInfo", up);
					JSONObject ob = new JSONObject(result);
					if (ob.getInt("status") == 0) {
						myHandler.obtainMessage(8).sendToTarget();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}
	ProgressDialog dia;
	public void submitAvatar() {
		dia = initProgressDialog();
		new Thread() {
			public void run() {
				try {
					// progressdialog = initProgressDialog();
					Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(ImageLoader.getInstance().getDiskCache().get(HttpUtlis.AVATAR_URL+
										MenuFragment.getAvatarUrl(share.getInt("UID", -1) + "","big"))));
					String result = HttpUtlis
							.uploadAvatar(
									HttpUtlis.UPAVATAR_URL
											+ share.getString("session_id", ""),
									bitmap);
					JSONObject avatar = new JSONObject(result);
					if (avatar.getInt("status") == 0) {
						imageloader.clearCache();
						// if (progressdialog != null
						// && progressdialog.isShowing())
						// progressdialog.dismiss();
						runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								if(dia!=null && dia.isShowing()){
									dia.dismiss();
								}
								ImageLoader.getInstance().displayImage(HttpUtlis.AVATAR_URL+
										MenuFragment.getAvatarUrl(share.getInt("UID", -1) + "","big"), photo,MilinkApplication.getListOptions()); 
							}
						});
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}.start();

	}

	private OnClickListener lsl = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub

			if (getIntent().getStringExtra("value") != null) {
				startActivity(new Intent().setClass(PersonalSettings.this,
						SlideMainActivity.class));
			}

			PersonalSettings.this.finish();

		}
	};

	public void showPhoto(View view) {
		etName.clearFocus();
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

	public void getUserProfiel() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Map<String, String> targetargs = new HashMap<String, String>();
					targetargs
							.put("session", share.getString("session_id", ""));
					String userInfo = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/user/getUserInfo", targetargs);

					JSONObject InfoStatus = new JSONObject(userInfo);
					if (InfoStatus.getInt("status") == 0) {
						JSONObject Info = new JSONObject(new JSONObject(
								userInfo).get("content").toString());
						db.UpdateUserInfo(Info.getString("nickname"),
								Info.getInt("sex"), Info.getDouble("height"),
								Info.getDouble("weight"));
					}

				} catch (Exception e) {
					e.printStackTrace();
				} finally {

				}

			}
		}).start();

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		if (share.getInt("ISMEMBER", 0) != 0) {

			db = new Dbutils(share.getInt("UID", -1), this);
			initViews();
			LinearLayout l1 = (LinearLayout) findViewById(R.id.linear1);
			LinearLayout l2 = (LinearLayout) findViewById(R.id.linear2);
			LinearLayout l3 = (LinearLayout) findViewById(R.id.linear3);
			LinearLayout l4 = (LinearLayout) findViewById(R.id.linear_s);
			l4.setVisibility(View.VISIBLE);
			l1.setVisibility(View.VISIBLE);
			l2.setVisibility(View.VISIBLE);
			l3.setVisibility(View.VISIBLE);

		}
		super.onResume();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case 1:
			if (resultCode == RESULT_OK)
				sdcardTempFile = mediautils.startPhotoZoom(
						Uri.fromFile(sdcardTempFile), 150, 150, 3);
			break;
		case 2:
			if (resultCode == RESULT_OK) {
				Uri originalUri = data.getData();
				sdcardTempFile = mediautils.startPhotoZoom(originalUri, 200,
						200, 3);
			}

			break;
		case 3:
			if (data != null && resultCode == RESULT_OK) {
				Bundle bundle = data.getExtras();
				if (bundle != null) {
					Bitmap p = bundle.getParcelable("data");
					
					try {
						ImageLoader.getInstance().getDiskCache().save(HttpUtlis.AVATAR_URL+MenuFragment.getAvatarUrl(share.getInt("UID", -1) + "","big"), p);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						String path =HttpUtlis.AVATAR_URL+MenuFragment.getAvatarUrl(share.getInt("UID", -1) + "","big");
						ImageLoader.getInstance().getDiskCache().remove(path);
						ImageLoader.getInstance().getMemoryCache().remove(path);
						e.printStackTrace();
					}
					isAvatar = true;
					submitAvatar();
				}
			}

			break;
		case 5:
			try {
				String h = data.getStringExtra("uheight");
				on = data.getStringExtra("on");
				reheight = Integer.parseInt(h);
				etHeight.setText(h + getString(R.string.unit_distance_cm));
				updateHeight();
			} catch (Exception e) {

			}
			break;
		case 4:
			try {
				String w = data.getStringExtra("startEdit");
				on = data.getStringExtra("on");
				reweight = Double.parseDouble(w);
				updateWeight();
				etWeight.setText(w + getString(R.string.unit_kg));
			} catch (Exception e) {

			}

			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private Handler myHandler = new Handler() {
		public void dispatchMessage(Message msg) {
			switch (msg.what) {
			case 0:
				if (progressdialog != null && progressdialog.isShowing())
					progressdialog.dismiss();
				Toast.makeText(PersonalSettings.this,
						R.string.accmanage_pwd_suc, Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(PersonalSettings.this,
						LoginActivity.class);
				startActivity(intent);

				break;
			case 1:
				if (progressdialog != null && progressdialog.isShowing())
					progressdialog.dismiss();
				Toast.makeText(PersonalSettings.this,
						R.string.accmanage_pwdsave_erro, Toast.LENGTH_SHORT)
						.show();
				break;
			case 2:
				if (progressdialog != null && progressdialog.isShowing())
					progressdialog.dismiss();
				Toast.makeText(PersonalSettings.this,
						R.string.accmanage_pwdsave_nomatch, Toast.LENGTH_SHORT)
						.show();
				break;
			case 3:
				if (progressdialog != null && progressdialog.isShowing())
					progressdialog.dismiss();
				Toast.makeText(PersonalSettings.this, R.string.err,
						Toast.LENGTH_SHORT).show();
				break;
			case 4:
				if (progressdialog != null && progressdialog.isShowing())
					progressdialog.dismiss();
				// if (getIntent().getIntExtra("iti", 0) != 0) {
				// startActivity(new Intent().setClass(PersonalSettings.this,
				// SlideMainActivity.class));
				// }
				// PersonalSettings.this.finish();
				break;
			case 5:
				if (progressdialog != null && progressdialog.isShowing())
					progressdialog.dismiss();

				break;
			case 6:
				if (progressdialog != null && progressdialog.isShowing())
					progressdialog.dismiss();
				Toast.makeText(PersonalSettings.this,
						R.string.accmanage_mobile_exist, Toast.LENGTH_SHORT)
						.show();
				break;
			case 7:
				if (progressdialog != null && progressdialog.isShowing())
					progressdialog.dismiss();
				Toast.makeText(PersonalSettings.this,
						R.string.accmanage_email_exist, Toast.LENGTH_SHORT)
						.show();
				break;
			case 8:
				db.UpdateUserInfo(nickname, tag1, reheight, reweight);
				updateAccount();
				// PersonalSettings.this.finish();
				break;
			default:
				break;
			}
		};
	};

	private void updateAccount() {
		final String email = et_email.getText().toString().trim();
		final String phone = et_phone.getText().toString().trim();
		if (email != null && email.length() > 0) {
			if (!isEmail(email)) {
				if (progressdialog != null && progressdialog.isShowing())
					progressdialog.dismiss();
				et_email.requestFocusFromTouch();
				Toast.makeText(this, R.string.accmanage_email_erro,
						Toast.LENGTH_SHORT).show();
				return;
			}
		}
		if (phone != null && phone.length() > 0) {
			if (!phone.substring(0, 1).equals("1")) {
				if (progressdialog != null && progressdialog.isShowing())
					progressdialog.dismiss();
				et_phone.requestFocusFromTouch();
				Toast.makeText(this, R.string.accmanage_tel_erro,
						Toast.LENGTH_SHORT).show();
				return;
			}
		}
		if (email.equals("") && phone.equals("")) {
			if (progressdialog != null && progressdialog.isShowing())
				progressdialog.dismiss();
			et_email.requestFocusFromTouch();
			Toast.makeText(this, R.string.accmanage_nothing, Toast.LENGTH_SHORT)
					.show();
		} else {

			new Thread() {
				public void run() {
					Map<String, String> params = new HashMap<String, String>();
					params.put("session", share.getString("session_id", ""));
					if (!email.equals("")) {
						params.put("email", email);
					}
					if (!phone.equals("")) {
						params.put("mobile", phone);
					}
					try {
						String result = HttpUtlis.getRequest(HttpUtlis.BASE_URL
								+ "/user/setUserAccount", params);
						JSONObject ob = new JSONObject(result);
						if (ob.getInt("status") == 0) {
							Dbutils db = new Dbutils(PersonalSettings.this);
							db.UpdateUserContact(phone);
							db.UpdateUserEmail(email);
							myHandler.obtainMessage(4).sendToTarget();
						} else if (ob.getInt("status") == 3) {
							myHandler.obtainMessage(6).sendToTarget();
						} else if (ob.getInt("status") == 4) {
							myHandler.obtainMessage(7).sendToTarget();
						} else {

							myHandler.obtainMessage(3).sendToTarget();
						}

					} catch (Exception e) {
						// TODO Auto-generated catch block
						myHandler.obtainMessage(3).sendToTarget();
						e.printStackTrace();
					}
				}

			}.start();
		}
	}

	// 判断手机格式是否正确
	// public boolean isMobileNO(String mobiles) {
	// Pattern p = Pattern
	// .compile("^((13[0-9])|(17[0678])|(14[57])|(15[^4,\\D])|(18[0-9]))\\d{8}$");
	// Matcher m = p.matcher(mobiles);
	// return m.matches();
	// }

	// 判断email格式是否正确
	public boolean isEmail(String email) {
		String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
		Pattern p = Pattern.compile(str);
		Matcher m = p.matcher(email);
		return m.matches();
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.et_email:
			et_phone.requestFocusFromTouch();
			break;
		case R.id.et_phone:
			getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
			break;
		}

		return false;
	}

	class UploadTargetParams extends Thread {
		String key, value;

		public UploadTargetParams(String key, String value) {
			this.key = key;
			this.value = value;

		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String urlStr = HttpUtlis.BASE_URL + "/user/setUserTarget";
			String w2 = share.getString("session_id", "");
			Map<String, String> map = new LinkedHashMap<String, String>();
			map.put("session", w2);
			map.put("key", key);
			map.put("value", value);
			try {
				JSONObject json = new JSONObject(HttpUtlis.getRequest(urlStr,
						map));
				if (json.getInt("status") == 0) {
					// myHandler.obtainMessage(1, map).sendToTarget();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			super.run();
		}

	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub
		
	}
}
package com.bandlink.air;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bandlink.air.gps.ViewPagerAdapter;
import com.bandlink.air.simple.SimpleHomeFragment;
import com.bandlink.air.user.LoginActivity;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.MediaUtils;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Util;
import com.bandlink.air.wxapi.WXEntryActivity;
import com.umeng.socialize.bean.CustomPlatform;
import com.umeng.socialize.bean.MultiStatus;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners.MulStatusListener;
import com.umeng.socialize.controller.listener.SocializeListeners.OnSnsPlatformClickListener;
import com.umeng.socialize.controller.listener.SocializeListeners.SnsPostListener;
import com.umeng.socialize.media.QQShareContent;
import com.umeng.socialize.media.QZoneShareContent;
import com.umeng.socialize.media.SinaShareContent;
import com.umeng.socialize.media.TencentWbShareContent;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.umeng.socialize.sso.TencentWBSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.sso.UMSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;

@SuppressLint("NewApi")
public class WaterImage extends LovefitActivity implements
		SurfaceHolder.Callback, OnClickListener, OnPageChangeListener {
	// 首先在您的Activity中添加如下成员变量
	final UMSocialService mController = UMServiceFactory
			.getUMSocialService("com.umeng.share");

	private SurfaceHolder holder;
	private Camera camera;// 声明相机
	private int cameraPosition = 1;// 0代表前置摄像头，1代表后置摄像头
	Context mContext;
	MediaUtils mediautils;
	String sSteps, sDistance, sCalorie, mapath, min_s;
	int bit_w, bit_h;
	private SurfaceView mCameraPreview;
	private ImageView imgview, photoview, shareview, big, v1_imgbit, v2_imgbit;
	ProgressDialog dialog;
	private boolean isTaking = true, ischange = true; // 拍照中
	private ViewPager vp;
	private ViewPagerAdapter vpAdapter;
	private List<View> views;
	// 底部小点图片
	private ImageView[] dots;
	Bitmap bit, logo;
	// 记录当前选中位置
	private int currentIndex;
	float scale;
	RelativeLayout ff;
	Gallery gallery;
	String resIds[];

	@SuppressWarnings("unused")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		// 设置横屏
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		// 设置全屏
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.waterimage);
		// ShareSDK.initSDK(this);
		super.onCreate(savedInstanceState);
		mediautils = new MediaUtils(this);
		if (Build.VERSION.SDK_INT < 9) {
			ActionbarSettings actionbar = new ActionbarSettings(this,
					new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							WaterImage.this.finish();
						}
					}, null);
			actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
			actionbar.setTitle(R.string.share_water);
		} else {
			ActionbarSettings actionbar = new ActionbarSettings(this,
					new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							WaterImage.this.finish();
						}
					}, lsr);
			actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
			actionbar.setTitle(R.string.share_water);
			actionbar.setTopRightIcon(R.drawable.change_camera);
		}

		Intent ii = this.getIntent();
		Bundle bb = ii.getExtras();

		sSteps = bb.getString("steps");
		sDistance = bb.getString("distance");
		sCalorie = String.format("%.1f", (float) bb.getDouble("calorie"));
		mapath = bb.getString("map");
		min_s = bb.getString("min_s");
		// scale =
		// this.getApplicationContext().getResources().getDisplayMetrics().density;
		scale = 3.0f;
		// 照相预览界面
		mCameraPreview = (SurfaceView) findViewById(R.id.preview);
		holder = mCameraPreview.getHolder();// 获得句柄
		holder.addCallback(this);// 添加回调
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		imgview = (ImageView) findViewById(R.id.img);
		photoview = (ImageView) findViewById(R.id.photo);
		shareview = (ImageView) findViewById(R.id.share);
		big = (ImageView) findViewById(R.id.image);
		ff = (RelativeLayout) findViewById(R.id.ff);
		try {
			resIds = getResources().getAssets().list("sell/images/shareImage");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		gallery = (Gallery) findViewById(R.id.gallery1);
		MarginLayoutParams mlp = (MarginLayoutParams) gallery.getLayoutParams();
		int offset = (getResources().getDisplayMetrics().widthPixels / 2);
		mlp.setMargins(-offset, mlp.topMargin, mlp.rightMargin,
				mlp.bottomMargin);
		gallery.setAdapter(new ImageAdapter(this, resIds,
				"sell/images/shareImage/"));
		gallery.setSpacing(20);

		gallery.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				try {
					bit = ((BitmapDrawable) BitmapDrawable.createFromStream(
							getResources().getAssets().open(
									"sell/images/shareImage/"
											+ resIds[position]),
							resIds[position])).getBitmap();
					big.setImageBitmap(bit);
					mCameraPreview.setVisibility(View.GONE);
					isTaking = false;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		imgview.setOnClickListener(this);
		photoview.setOnClickListener(this);
		shareview.setOnClickListener(this);

		LayoutInflater inflater = LayoutInflater.from(this);

		views = new ArrayList<View>();
		// 初始化引导图片列表

		View v1 = inflater.inflate(R.layout.viewpage1, null);
		View v2 = inflater.inflate(R.layout.viewpage2, null);
		v1_imgbit = (ImageView) v1.findViewById(R.id.imgbit);
		v2_imgbit = (ImageView) v2.findViewById(R.id.imgbit);
		views.add(v1);
		views.add(v2);
		// 初始化Adapter
		vpAdapter = new ViewPagerAdapter(views, this);

		vp = (ViewPager) findViewById(R.id.viewpager);
		vp.setAdapter(vpAdapter);
		// 绑定回调
		vp.setOnPageChangeListener(this);
		initDots();
		logo = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
		Matrix m1 = new Matrix();
		m1.postScale(((float) 280 / (float) (logo.getWidth())),
				((float) 100 / (float) (logo.getHeight())));
		logo = Bitmap.createBitmap(logo, 0, 0, logo.getWidth(),
				logo.getHeight(), m1, true);

	}

	OnClickListener lsr = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			clickChangeCamera();
		}
	};

	private void initDots() {
		LinearLayout ll = (LinearLayout) findViewById(R.id.ll);

		dots = new ImageView[views.size()];

		// 循环取得小点图片
		for (int i = 0; i < views.size(); i++) {
			dots[i] = (ImageView) ll.getChildAt(i);
			dots[i].setEnabled(true);// 都设为灰色
		}

		currentIndex = 0;
		dots[currentIndex].setEnabled(false);// 设置为白色，即选中状态
	}

	private void setCurrentDot(int position) {
		if (position < 0 || position > views.size() - 1
				|| currentIndex == position) {
			return;
		}

		dots[position].setEnabled(false);
		dots[currentIndex].setEnabled(true);

		currentIndex = position;

	}

	// 当滑动状态改变时调用
	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	// 当当前页面被滑动时调用
	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	// 当新的页面被选中时调用
	@Override
	public void onPageSelected(int arg0) {
		// 设置底部小点选中状态
		setCurrentDot(arg0);
		drawPage();
	}

	public void drawPage() {
		if (currentIndex == 0) {

			Bitmap bmpTemp = Bitmap
					.createBitmap(bit_w, bit_h, Config.ARGB_8888);
			Canvas canvas = new Canvas(bmpTemp);
			canvas.setDrawFilter(new PaintFlagsDrawFilter(0,
					Paint.ANTI_ALIAS_FLAG));
			Paint paint = new Paint();
			Paint p = new Paint();
			p.setAntiAlias(true);
			p.setColor(Color.WHITE);
			p.setTextSize(14 * scale);
			paint.setAntiAlias(true);
			paint.setTypeface(MilinkApplication.NumberFace);
			paint.setColor(Color.WHITE);
			paint.setTextSize(25 * scale);
			canvas.drawBitmap(logo, bit_w / 10 * 9 - logo.getWidth(),
					bit_h / 20, paint);
			// canvas.drawText(sSteps, bit_w / 20, bit_h / 20 * 17, paint);
			// canvas.drawText(getResources().getString(R.string.step),
			// bit_w / 20, bit_h / 10 * 9, p);
			canvas.drawText(sDistance,
					(bit_w / 3f - paint.measureText(sDistance)) / 2f,
					bit_h / 20 * 17, paint);
			canvas.drawText(
					getResources().getString(R.string.distance),
					(bit_w / 3f - p.measureText(getResources().getString(
							R.string.distance))) / 2f, bit_h / 10 * 9, p);
			canvas.drawText(min_s,
					bit_w / 3f + (bit_w / 3f - paint.measureText(min_s)) / 2f,
					bit_h / 20 * 17, paint);
			canvas.drawText(
					getResources().getString(R.string.duration),
					bit_w
							/ 3f
							+ (bit_w / 3f - p.measureText(getResources()
									.getString(R.string.duration))) / 2f,
					bit_h / 10 * 9, p);
			canvas.drawText(sCalorie,
					bit_w * 2 / 3f + (bit_w / 3f - paint.measureText(sCalorie))
							/ 2f, bit_h / 20 * 17, paint);
			canvas.drawText(
					getResources().getString(R.string.unit_cal),
					bit_w
							* 2
							/ 3f
							+ (bit_w / 3f - p.measureText(getResources()
									.getString(R.string.unit_cal))) / 2f,
					bit_h / 10 * 9, p);
			canvas.save(Canvas.ALL_SAVE_FLAG);
			canvas.restore();
			v1_imgbit.setImageBitmap(bmpTemp);

		} else if (currentIndex == 1) {
			Bitmap bmpTemp = Bitmap
					.createBitmap(bit_w, bit_h, Config.ARGB_8888);
			Canvas canvas = new Canvas(bmpTemp);
			canvas.setDrawFilter(new PaintFlagsDrawFilter(0,
					Paint.ANTI_ALIAS_FLAG));
			Paint paint = new Paint();
			Paint p = new Paint();
			p.setAntiAlias(true);
			p.setColor(Color.WHITE);
			p.setTextAlign(Align.CENTER);
			p.setTextSize(26 * scale);
			paint.setAntiAlias(true);
			paint.setColor(Color.WHITE);
			paint.setTextAlign(Align.CENTER);
			paint.setTextSize(26 * scale);
			canvas.drawBitmap(logo, bit_w / 10 * 9 - logo.getWidth(),
					bit_h / 20, paint);
			if (Locale.getDefault().getLanguage().equals("zh")) {
				// canvas.drawText(getResources().getString(R.string.life_sport),
				// bit_w / 10 * 3, bit_h / 20 * 16, paint);
				// canvas.drawText(
				// getResources().getString(R.string.target_step) + sSteps
				// + getResources().getString(R.string.unit_step),
				// bit_w / 10 * 7, bit_h / 20 * 18, p);

				canvas.drawBitmap(BitmapFactory.decodeResource(getResources(),
						R.drawable.sport), 0, bit_h * 0.8f, paint);
			} else {

				// canvas.drawText(getResources().getString(R.string.life_sport),
				// bit_w / 10 * 4, bit_h / 40 * 33, paint);
				// canvas.drawText(
				// getResources().getString(R.string.target_step) + " "
				// + sSteps + " "
				// + getResources().getString(R.string.unit_step),
				// bit_w / 10 * 7, bit_h / 20 * 18, p);

				canvas.drawBitmap(BitmapFactory.decodeResource(getResources(),
						R.drawable.sport), 0, bit_h * 0.8f, paint);
			}

			canvas.save(Canvas.ALL_SAVE_FLAG);
			canvas.restore();
			v2_imgbit.setImageBitmap(bmpTemp);

		}
	}

	private Bitmap createBitmap(Bitmap src, String step, String dis,
			String time, String c) {
		Bitmap bmpTemp = Bitmap.createBitmap(bit_w, bit_h, Config.ARGB_8888);
		Canvas canvas = new Canvas(bmpTemp);
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG));
		Paint paint = new Paint();
		Paint p = new Paint();
		p.setAntiAlias(true);
		p.setColor(Color.WHITE);
		p.setTextSize(14 * scale);
		paint.setAntiAlias(true);
		paint.setColor(Color.WHITE);
		paint.setTypeface(MilinkApplication.NumberFace);
		paint.setTextSize(25 * scale);
		canvas.drawBitmap(src, 0, 0, paint);
		canvas.drawBitmap(logo, bit_w / 10 * 9 - logo.getWidth(), bit_h / 20,
				paint);
		// canvas.drawText(sSteps, bit_w / 20, bit_h / 20 * 17, paint);
		// canvas.drawText(getResources().getString(R.string.step), bit_w / 20,
		// bit_h / 10 * 9, p);
		canvas.drawText(sDistance,
				(bit_w / 3f - paint.measureText(sDistance)) / 2f,
				bit_h / 20 * 17, paint);
		canvas.drawText(
				getResources().getString(R.string.distance),
				(bit_w / 3f - p.measureText(getResources().getString(
						R.string.distance))) / 2f, bit_h / 10 * 9, p);
		canvas.drawText(min_s,
				bit_w / 3f + (bit_w / 3f - paint.measureText(min_s)) / 2f,
				bit_h / 20 * 17, paint);
		canvas.drawText(
				getResources().getString(R.string.duration),
				bit_w
						/ 3f
						+ (bit_w / 3f - p.measureText(getResources().getString(
								R.string.duration))) / 2f, bit_h / 10 * 9, p);
		canvas.drawText(sCalorie,
				bit_w * 2 / 3f + (bit_w / 3f - paint.measureText(sCalorie))
						/ 2f, bit_h / 20 * 17, paint);
		canvas.drawText(
				getResources().getString(R.string.unit_cal),
				bit_w
						* 2
						/ 3f
						+ (bit_w / 3f - p.measureText(getResources().getString(
								R.string.unit_cal))) / 2f, bit_h / 10 * 9, p);
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		return bmpTemp;
	}

	private Bitmap createBitmap1(Bitmap src) {
		Bitmap bmpTemp = Bitmap.createBitmap(bit_w, bit_h, Config.ARGB_8888);
		Canvas canvas = new Canvas(bmpTemp);
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG));
		Paint paint = new Paint();
		Paint p = new Paint();
		p.setAntiAlias(true);
		p.setColor(Color.WHITE);
		p.setTextAlign(Align.CENTER);
		p.setTextSize(26 * scale);
		paint.setAntiAlias(true);
		paint.setColor(Color.WHITE);
		paint.setTextAlign(Align.CENTER);
		paint.setTextSize(26 * scale);
		paint.setTypeface(MilinkApplication.NumberFace);
		canvas.drawBitmap(src, 0, 0, paint);
		canvas.drawBitmap(logo, bit_w / 10 * 9 - logo.getWidth(), bit_h / 20,
				paint);
		if (Locale.getDefault().getLanguage().equals("zh")) {
			// canvas.drawText(getResources().getString(R.string.life_sport),
			// bit_w / 10 * 3, bit_h / 20 * 16, paint);
			// canvas.drawText(getResources().getString(R.string.target_step)
			// + sSteps + getResources().getString(R.string.unit_step),
			// bit_w / 10 * 7, bit_h / 20 * 18, p);
			canvas.drawBitmap(BitmapFactory.decodeResource(getResources(),
					R.drawable.sport), 0, bit_h * 0.8f, paint);
		} else {
			canvas.drawText(getResources().getString(R.string.life_sport),
					bit_w / 10 * 4, bit_h / 40 * 33, paint);
			canvas.drawText(
					getResources().getString(R.string.target_step) + " "
							+ sSteps + " "
							+ getResources().getString(R.string.unit_step),
					bit_w / 10 * 7, bit_h / 20 * 18, p);
		}
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		return bmpTemp;
	}

	private File picfile;

	public File getFilePath(String dir, String filename) {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {

			File sdcardFile = new File(
					Environment.getExternalStorageDirectory(), dir);
			if (!sdcardFile.exists()) {
				sdcardFile.mkdirs();
			}
			picfile = new File(sdcardFile.getPath() + "/" + filename);
		} else {
			Toast.makeText(WaterImage.this, R.string.nosd, Toast.LENGTH_LONG)
					.show();
		}
		return picfile;
	}

	public void saveMyBitmap(Bitmap bitmap) {
		String strPath =
		// Environment.getExternalStorageDirectory().toString()
		"/milink/waterimage/";
		OutputStream fos = null;
		// File f = new File(strPath, "waterimage.jpg");
		File f = getFilePath(strPath, "waterimage.jpg");
		// if (path.exists())
		// path.delete();
		if (f.exists()) {
			f.delete();
		}
		try {
			FileOutputStream out = new FileOutputStream(f);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.flush();
			out.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void showShare(boolean silent, String platform, boolean captureView) {
		//
		// final OnekeyShare oks = new OnekeyShare();
		if (bit != null) {
			if (currentIndex == 0) {
				saveMyBitmap(createBitmap(bit, sSteps, sDistance, min_s,
						sCalorie));

			} else if (currentIndex == 1) {
				saveMyBitmap(createBitmap1(bit));
			}

		}
		// oks.setNotification(R.drawable.ic_launcher,
		// getString(R.string.app_name));
		// oks.setTitle(getString(R.string.lebu));
		// oks.setTitleUrl("http://www.lovefit.com");
		// oks.setText(getString(R.string.gps_hint4));
		// oks.setUrl("http://www.lovefit.com");
		// oks.setSite(getString(R.string.app_name));
		// oks.setSiteUrl("http://www.lovefit.com");
		// oks.setSilent(silent);
		// if (bit != null) {
		// oks.setImagePath(Environment.getExternalStorageDirectory()
		// .toString() + "/milink/waterimage/waterimage.jpg");
		// }
		// if (platform != null) {
		// oks.setPlatform(platform);
		// }
		// oks.setDialogMode();
		// // 在自动授权时可以禁用SSO方式
		// oks.disableSSOWhenAuthorize();
		// // 去除注释，则快捷分享的操作结果将通过OneKeyShareCallback回调
		// oks.setShareContentCustomizeCallback(new
		// ShareContentCustomizeCallback() {
		//
		// @Override
		// public void onShare(Platform platform, ShareParams paramsToShare) {
		// // TODO Auto-generated method stub
		//
		// }
		// });
		// Bitmap logo = BitmapFactory.decodeResource(getResources(),
		// R.drawable.logo_lovefit);
		// String label = getResources().getString(R.string.app_name);
		// OnClickListener listener = new OnClickListener() {
		// public void onClick(View v) {
		// Intent i=new Intent();
		// i.putExtra("imagePath", Environment.getExternalStorageDirectory()
		// .toString() + "/milink/waterimage/waterimage.jpg");
		// i.putExtra("tid", 3);
		// i.setClass(WaterImage.this, ShareEditActivity.class);
		// startActivity(i);
		// oks.finish();
		// }
		// };
		// oks.setCustomerLogo(logo, label, listener);
		//
		// oks.show(this);
	}

	private void setShareContent() {
		// 设置分享内容
		mController.setShareContent(getString(R.string.gps_hint4));
		if (bit != null) {
			if (currentIndex == 0) {
				saveMyBitmap(createBitmap(bit, sSteps, sDistance, min_s,
						sCalorie));

			} else if (currentIndex == 1) {
				saveMyBitmap(createBitmap1(bit));
			}

		}
		UMImage ui = new UMImage(this, Environment
				.getExternalStorageDirectory().toString()
				+ "/milink/waterimage/waterimage.jpg");
		// 设置分享图片, 参数2为图片的url地址
		if (bit != null) {
			// oks.setImagePath(Environment.getExternalStorageDirectory()
			// .toString() + "/milink/waterimage/waterimage.jpg");

			mController.setShareMedia(ui);
		}

		// 设置分享图片，参数2为本地图片的资源引用
		mController.setShareMedia(new UMImage(this, R.drawable.ic_launcher));

		CustomPlatform customPlatform = new CustomPlatform("contacts",
				getString(R.string.app_name), R.drawable.ic_launcher);
		customPlatform.mClickListener = new OnSnsPlatformClickListener() {
			@Override
			public void onClick(Context context, SocializeEntity entity,
					SnsPostListener listener) {

				shareToLovefit(Environment.getExternalStorageDirectory()
						.toString() + "/milink/waterimage/waterimage.jpg");
			}
		};
		mController.getConfig().addCustomPlatform(customPlatform);

		// QQ
		UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler(this, LoginActivity.QQ_APP_ID,
				LoginActivity.QQ_APP_KEY);
		qqSsoHandler.setTargetUrl("http://www.lovefit.com");
		QQShareContent qq = new QQShareContent();
		// 设置title
		qq.setShareContent(getString(R.string.gps_hint4));
		qq.setTargetUrl("http://www.lovefit.com");
		qq.setTitle(getString(R.string.app_name));
		qq.setShareImage(ui);
		mController.setShareMedia(qq);
		qqSsoHandler.addToSocialSDK();

		// QZONE
		QZoneSsoHandler qZoneSsoHandler = new QZoneSsoHandler(this,
				LoginActivity.QQ_APP_ID, LoginActivity.QQ_APP_KEY);
		qZoneSsoHandler.setTargetUrl("http://www.lovefit.com");
		QZoneShareContent qz = new QZoneShareContent();
		// 设置title
		qz.setShareContent(getString(R.string.gps_hint4));
		qz.setTargetUrl("http://www.lovefit.com");
		qz.setTitle(getString(R.string.app_name));
		qz.setShareImage(ui);
		mController.setShareMedia(qz);
		qZoneSsoHandler.addToSocialSDK();
		// 微信
		UMWXHandler wxHandler = new UMWXHandler(this, WXEntryActivity.AppId,
				WXEntryActivity.AppSecret);
		wxHandler.setTargetUrl("http://www.lovefit.com");
		// 设置微信好友分享内容
		WeiXinShareContent weixinContent = new WeiXinShareContent();
		// 设置title
		weixinContent.setTargetUrl("http://www.lovefit.com");
		// weixinContent.setShareContent(getString(R.string.gps_hint4));
		weixinContent.setTitle(getString(R.string.app_name));
		weixinContent.setShareImage(ui);

		mController.setShareMedia(weixinContent);
		wxHandler.addToSocialSDK();

		// 微信朋友圈
		UMWXHandler wxCircleHandler = new UMWXHandler(this,
				WXEntryActivity.AppId, WXEntryActivity.AppSecret);
		wxCircleHandler.setTargetUrl("http://www.lovefit.com");
		// 设置微信好友分享内容
		CircleShareContent cs = new CircleShareContent();
		// cs.setShareContent(getString(R.string.gps_hint4));
		// 设置title
		cs.setTargetUrl("http://www.lovefit.com");
		cs.setTitle(getString(R.string.app_name));
		cs.setShareImage(ui);

		mController.setShareMedia(cs);
		wxCircleHandler.setToCircle(true);
		wxCircleHandler.addToSocialSDK();
		// 新浪微博
		mController.getConfig().setSsoHandler(new SinaSsoHandler());
		mController.getConfig().setSinaCallbackUrl(
				"http://sns.whalecloud.com/sina2/callback");
		SinaShareContent si = new SinaShareContent(); // 设置title
		si.setTargetUrl("http://www.lovefit.com");
		si.setShareContent(getString(R.string.gps_hint4));
		si.setTitle(getString(R.string.app_name));
		si.setShareImage(ui);
		mController.setShareMedia(si);
		// 腾讯微博
		mController.getConfig().setSsoHandler(new TencentWBSsoHandler());
		TencentWbShareContent tb = new TencentWbShareContent(); // 设置title
		tb.setTargetUrl("http://www.lovefit.com");
		tb.setShareContent(getString(R.string.gps_hint4));
		tb.setTitle(getString(R.string.app_name));
		tb.setShareImage(ui);
		mController.setShareMedia(tb);
		SHARE_MEDIA[] platforms = new SHARE_MEDIA[] { SHARE_MEDIA.WEIXIN,
				SHARE_MEDIA.WEIXIN_CIRCLE, SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE,
				SHARE_MEDIA.SINA, SHARE_MEDIA.TENCENT };

		mController.postShareMulti(WaterImage.this, new MulStatusListener() {

			@Override
			public void onStart() {
			}

			@Override
			public void onComplete(MultiStatus multiStatus, int st,
					SocializeEntity entity) {
				String showText = "分享结果：" + multiStatus.toString();

			}
		}, platforms);

	}

	private void shareToLovefit(final String image) {
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle(R.string.warning);
		ab.setMessage(R.string.share);

		ab.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						SimpleHomeFragment.sendFeed(image, WaterImage.this,
								SharePreUtils.getInstance(WaterImage.this)
										.getUid(),false);
						Toast.makeText(WaterImage.this, R.string.check_later,
								Toast.LENGTH_LONG).show();
					}
				});
		ab.setNegativeButton(R.string.cancel, null);
		ab.create().show();

	}

	public void clickChangeCamera() {
		if (ischange) {
			if (!isTaking) {
				isTaking = true;
				mCameraPreview.setVisibility(View.VISIBLE);
				big.setImageBitmap(null);
			} else {
				int cameraCount = 0;
				CameraInfo cameraInfo = new CameraInfo();
				cameraCount = Camera.getNumberOfCameras();// 得到摄像头的个数

				if (Build.VERSION.SDK_INT >= 9) {
					// 切换前后摄像头
					for (int i = 0; i < cameraCount; i++) {
						Camera.getCameraInfo(i, cameraInfo);// 得到每一个摄像头的信息
						if (cameraPosition == 1) {
							// 现在是后置，变更为前置
							if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {// 代表摄像头的方位，CAMERA_FACING_FRONT前置
								camera.stopPreview();// 停掉原来摄像头的预览
								camera.release();// 释放资源
								camera = null;// 取消原来摄像头
								camera = Camera.open(i);// 打开当前选中的摄像头
								take();
								cameraPosition = 0;
								break;
							}
						} else {
							// 现在是前置， 变更为后置
							if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {// 代表摄像头的方位，CAMERA_FACING_FRONT前置
																							// CAMERA_FACING_BACK后置
								camera.stopPreview();// 停掉原来摄像头的预览
								camera.release();// 释放资源
								camera = null;// 取消原来摄像头
								camera = Camera.open(i);// 打开当前选中的摄像头
								take();
								cameraPosition = 1;
								break;
							}
						}

					}
				}
			}
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.back) {
			WaterImage.this.finish();
		} else if (v.getId() == R.id.img) {
			Intent picture = new Intent(
					Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(picture, 20);
		} else if (v.getId() == R.id.share) {
			if (bit != null) {
				// showShare(false, null, false);
				setShareContent();
				mController.openShare(this, false);
			}
		} else if (v.getId() == R.id.photo) {

			if (!isTaking) {
				isTaking = true;
				mCameraPreview.setVisibility(View.VISIBLE);
				big.setImageBitmap(null);
				ischange = true;
			} else {
				takePicture();
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 20) {
			if (resultCode == Activity.RESULT_OK && null != data) {

				Uri selectedImage = data.getData();
				String[] filePathColumns = { MediaStore.Images.Media.DATA };
				Cursor c = this.getContentResolver().query(selectedImage,
						filePathColumns, null, null, null);
				c.moveToFirst();
				int columnIndex = c.getColumnIndex(filePathColumns[0]);
				String picturePath = c.getString(columnIndex);
				c.close();
				File file = new File(picturePath);
				if (file.exists()) {
					bit = BitmapFactory.decodeFile(picturePath);
					Matrix m = new Matrix();
					double size = (new File(picturePath)).length();
					m.postScale(((float) bit_w / (float) (bit.getWidth())),
							((float) bit_h / (float) (bit.getHeight())));
					bit = Bitmap.createBitmap(bit, 0, 0, bit.getWidth(),
							bit.getHeight(), m, true);
					big.setImageBitmap(bit);
					mCameraPreview.setVisibility(View.GONE);
					isTaking = false;
				}
			}

		} else {

			/** 使用SSO授权必须添加如下代码 */
			UMSsoHandler ssoHandler = mController.getConfig().getSsoHandler(
					requestCode);
			if (ssoHandler != null) {
				ssoHandler.authorizeCallBack(requestCode, resultCode, data);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	// 停止拍照，并将拍摄的照片传入PictureCallback接口的onPictureTaken方法
	public void takePicture() {
		if (camera != null) {
			dialog = Util.initProgressDialog(this, false,
					getString(R.string.photo_ing), null);
			// 自动对焦

			camera.autoFocus(new AutoFocusCallback() {

				@Override
				public void onAutoFocus(boolean success, Camera camera) {
					// TODO Auto-generated method stub
					// 自动对焦成功后才拍摄
					if (success) {
						camera.takePicture(null, null, pictureCallback);
					} else {
						camera.takePicture(null, null, pictureCallback);
						if (dialog != null && dialog.isShowing()) {
							dialog.dismiss();
						}
					}
				}
			});

		}
	}

	public void take() {
		try {
			camera.setPreviewDisplay(holder);// 通过surfaceview显示取景画面
			// 获取照相机参数
			Camera.Parameters parameters = camera.getParameters();
			// 设置照片格式
			parameters.setPictureFormat(PixelFormat.JPEG);

			parameters.setJpegQuality(100);
			Size ss = parameters.getPictureSize();
			List<Size> jj = parameters.getSupportedPictureSizes();

			// int[] number = new int[jj.size()];
			// int temp = 0;
			// for (int i = 0; i < jj.size(); i++) {
			// number[i] = jj.get(i).width;
			// }
			// for (int i = 0; i < number.length - 1; i++)
			// for (int j = 0; j < number.length - 1 - i; j++)
			// if (number[j] > number[j + 1]) {
			// temp = number[j];
			// number[j] = number[j + 1];
			// number[j + 1] = temp;
			// }
			// for (int i = 0; i < number.length; i++)
			// WriteLog.Write(WriteLog.getSDPath() + "/" + "testLog.txt",
			// number[i] + "\n");
			// for(int i=0;i<jj.size();i++){
			// System.out.println(jj.get(i).width+"..."+jj.get(i).height);
			// WriteLog.Write(WriteLog.getSDPath() + "/" +
			// "testLog.txt",jj.get(i).width+"..."+jj.get(i).height+"\n");
			// }
			// if(cameraPosition==0){
			// parameters.setPreviewSize(1080, 960);
			// }else{
			// parameters.setPreviewSize(1280, 960);

			parameters.setPictureSize(1280, 960);
			// 设置照相机参数
			bit_w = 960;
			bit_h = 1280;
			camera.setDisplayOrientation(90);
			camera.setParameters(parameters);
			drawPage();
			// 开始拍照
			camera.startPreview();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private PictureCallback pictureCallback = new PictureCallback() {

		// 该方法用于处理拍摄后的照片数据
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			try {
				String strPath = Environment.getExternalStorageDirectory()
						.toString() + "/milink/waterimage/";
				File ff = new File(strPath);
				if (!ff.exists()) {
					ff.mkdir();
				}
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;

				if (cameraPosition == 0) {

					Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
							data.length);
					Matrix m = new Matrix();
					m.postScale(1, -1);// 上下翻转
					m.postRotate(-90);// 左右翻转
					bit = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
							bitmap.getHeight(), m, true);

				} else if (cameraPosition == 1) {

					Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
							data.length, options);

					options.inJustDecodeBounds = false;
					bitmap = BitmapFactory.decodeByteArray(data, 0,
							data.length, options);
					Matrix m = new Matrix();
					m.postRotate(90);
					bit = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
							bitmap.getHeight(), m, true);
				}

				big.setImageBitmap((bit));
				big.setScaleType(ImageView.ScaleType.FIT_XY);
				// drawPage();
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
				mCameraPreview.setVisibility(View.GONE);
				ischange = false;
			} catch (Exception e) {
			}
		}
	};

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		// 当surfaceview创建时开启相机
		if (camera == null) {

			if (photoview.isClickable()) {
				camera = Camera.open();
				take();
			}
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		// 当surfaceview关闭时，关闭预览并释放资源
		if (camera != null) {
			camera.setPreviewCallback(null);
			if (isTaking)
				camera.stopPreview();
			camera.release();
			camera = null;
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (camera != null) {
			camera.setPreviewCallback(null);
			camera.stopPreview();
			camera.release();
			camera = null;
			holder = null;
			mCameraPreview = null;
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

	}

	public static class ImageAdapter extends BaseAdapter {
		private Context mContext;
		String[] resIds;
		String path;

		public ImageAdapter(Context context, String[] ids, String path) {
			mContext = context;
			this.path = path;
			this.resIds = ids;
		}

		// 返回图像总数
		public int getCount() {
			return resIds.length;
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		// 返回具体位置的ImageView对象
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView = new ImageView(mContext);
			// 设置当前图像的图像（position为当前图像列表的位置）
			try {
				BitmapFactory.Options o = new BitmapFactory.Options();
				o.inSampleSize = 2;
				imageView.setImageBitmap(BitmapFactory.decodeStream(
						mContext.getResources().getAssets()
								.open(path + resIds[position]), null, o));

			} catch (IOException e) {

				imageView.setImageResource(R.drawable.no_media);
				e.printStackTrace();
			}
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			// 设置Gallery组件的背景风格
			imageView.setLayoutParams(new Gallery.LayoutParams(100, 100));
			return imageView;
		}
	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub

	}
}
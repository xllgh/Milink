package com.bandlink.air.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.bandlink.air.R;

public class Util {
	
	public static int dp2px(Context context,int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				context.getResources().getDisplayMetrics());
	}
	/***
	 * 创建等待对话框
	 * 
	 * @param context
	 * @param cancelable
	 *            是否可以取消
	 * @param msg
	 *            显示文字 不显示则传null
	 * @param listener
	 *            对话框取消事件
	 * @return
	 */
	public static ProgressDialog initProgressDialog(Context context,
			boolean cancelable, String msg,
			DialogInterface.OnCancelListener listener) {
		LayoutInflater inflater = LayoutInflater.from(context);

		ProgressDialog progressDialog = ProgressDialog.show(
				new ContextThemeWrapper(context,
						android.R.style.Theme_Holo_Light), null, msg, true);

		View v = inflater.inflate(R.layout.loading, null);//
		TextView tvMsg = (TextView) v.findViewById(R.id.tvTitle);
		if (tvMsg != null && msg != null) {
			tvMsg.setVisibility(View.VISIBLE);
			tvMsg.setText(msg);
		} else {
			tvMsg.setVisibility(View.GONE);
		}
		progressDialog.setContentView(v);
		progressDialog.setCancelable(cancelable);
		progressDialog.setOnCancelListener(listener);

		return progressDialog;
	}

	public static Context getThemeContext(Context context) {
		if (Build.VERSION.SDK_INT >= 21) {
			context = new ContextThemeWrapper(context,
					android.R.style.Theme_Material_Light);
		} else if (Build.VERSION.SDK_INT >= 11) {
			context = new ContextThemeWrapper(context,
					android.R.style.Theme_Holo_Light);
		}
		return context;
	}

	public static Bitmap getTransparentBitmap(Bitmap sourceImg, int number) {
		int[] argb = new int[sourceImg.getWidth() * sourceImg.getHeight()];

		sourceImg.getPixels(argb, 0, sourceImg.getWidth(), 0, 0, sourceImg

		.getWidth(), sourceImg.getHeight());// 获得图片的ARGB值

		number = number * 255 / 100;

		for (int i = 0; i < argb.length; i++) {

			argb[i] = (number << 24) | (argb[i] & 0x00FFFFFF);

		}

		sourceImg = Bitmap.createBitmap(argb, sourceImg.getWidth(), sourceImg

		.getHeight(), Config.ARGB_8888);

		return sourceImg;
	}

	public static Bitmap getZoomBitmap(String imagePath) {

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

		Bitmap bm = BitmapFactory.decodeFile(imagePath, options);

		options.inJustDecodeBounds = false;

		int be = (int) (options.outHeight / (float) 200);

		if (be == 0) {
			be = 1;
		}
		options.inSampleSize = be;

		bm = BitmapFactory.decodeFile(imagePath, options);
		return bm;
	}

	public static Drawable zoomDrawable(Drawable drawable, int w, int h) {
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();

		Bitmap oldbmp = drawableToBitmap(drawable);

		Matrix matrix = new Matrix();

		float sx = ((float) w / width);
		float sy = ((float) h / height);

		matrix.postScale(sx, sy);

		Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height,
				matrix, true);
		return new BitmapDrawable(newbmp);
	}

	public static Bitmap zoomBitmap(Bitmap bitmap, int width, int height) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float scaleWidth = ((float) width / w);
		float scaleHeight = ((float) height / h);
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
		return newbmp;
	}

	public static Bitmap drawableToBitmap(Drawable drawable) {
		// ȡ drawable �ĳ���
		int w = drawable.getIntrinsicWidth();
		int h = drawable.getIntrinsicHeight();

		Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565;

		Bitmap bitmap = Bitmap.createBitmap(w, h, config);

		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, w, h);

		drawable.draw(canvas);
		return bitmap;
	}

	public static void createImage(String filePath) {
		final int reflectionGap = 4;
		BitmapFactory.Options op = new BitmapFactory.Options();
		op.inSampleSize = 4;
		Bitmap orImage = BitmapFactory.decodeFile(filePath, op);
		int w = orImage.getWidth();
		int h = orImage.getHeight();

		Matrix ma = new Matrix();
		ma.preScale(1, -1);

		Bitmap reImage = Bitmap.createBitmap(orImage, 0, h / 2, w, h / 2, ma,
				false);
		Bitmap bit = Bitmap.createBitmap(w, (h + h / 2), Config.ARGB_8888);
		Canvas canvas = new Canvas(bit);
		canvas.drawBitmap(orImage, 0, 0, null);
		Paint defaultpaint = new Paint();
		canvas.drawRect(0, h, w, h + reflectionGap, defaultpaint);
		canvas.drawBitmap(reImage, 0, h + reflectionGap, null);
		Paint paint = new Paint();
		LinearGradient shader = new LinearGradient(0, orImage.getHeight(), 0,
				bit.getHeight() + reflectionGap, 0x70ffffff, 0x00ffffff,
				TileMode.CLAMP);
		paint.setShader(shader);
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		canvas.drawRect(0, h, w, bit.getHeight() + reflectionGap, paint);

	}

	/**
	 * 获得某个日期向前、向后推算一定天数的日期
	 * 
	 * @param datestr
	 *            原始日期 格式为yyyy-MM-dd
	 * @param day
	 *            推算天数，否数向后
	 * @return 新日期
	 */
	public static java.sql.Date getBeforeAfterDate(String datestr, int day) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		java.sql.Date olddate = null;
		try {
			df.setLenient(false);
			olddate = new java.sql.Date(df.parse(datestr).getTime());
		} catch (ParseException e) {
			throw new RuntimeException(R.string.time_change_mistake + "");
		}
		Calendar cal = new GregorianCalendar();
		cal.setTime(olddate);

		int Year = cal.get(Calendar.YEAR);
		int Month = cal.get(Calendar.MONTH);
		int Day = cal.get(Calendar.DAY_OF_MONTH);

		int NewDay = Day + day;

		cal.set(Calendar.YEAR, Year);
		cal.set(Calendar.MONTH, Month);
		cal.set(Calendar.DAY_OF_MONTH, NewDay);

		return new java.sql.Date(cal.getTimeInMillis());
	}

	public static String getTimeMMString() {
		return new SimpleDateFormat("yyyy-MM-dd HH：mm:ss:SSS")
				.format(new Date());
	}

	public static String getTimeMMStringFormat(String str) {
		return new SimpleDateFormat(str).format(new Date());
	}
}

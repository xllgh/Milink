package com.bandlink.air.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.bandlink.air.R;

public class RoundedImageView extends ImageView {
	private int mBorderThickness = 0;
	private Context mContext;
	private int mBorderColor = 0xFFFFFFFF;

	public RoundedImageView(Context context) {
		super(context);
		mContext = context;
	}

	public RoundedImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		setCustomAttributes(attrs);
	}

	public RoundedImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		setCustomAttributes(attrs);
	}

	private void setCustomAttributes(AttributeSet attrs) {
		TypedArray a = mContext.obtainStyledAttributes(attrs,
				R.styleable.roundedimageview);
		mBorderThickness = a.getDimensionPixelSize(
				R.styleable.roundedimageview_border_thickness, 0);
		mBorderColor = a.getColor(R.styleable.roundedimageview_border_color,
				mBorderColor);
	}

	@Override
	protected void onDraw(Canvas canvas) {

		Drawable drawable = getDrawable();

		if (drawable == null) {
			return;
		}

		if (getWidth() == 0 || getHeight() == 0) {
			return;
		}
		this.measure(0, 0);
		if (drawable.getClass() == NinePatchDrawable.class)
			return;
		Bitmap b = ((BitmapDrawable) drawable).getBitmap();
		Bitmap bitmap = b.copy(Bitmap.Config.ARGB_8888, true);

		int w = getWidth(), h = getHeight();

		int radius = (w < h ? w : h) / 2 - mBorderThickness;
		Bitmap roundBitmap = getCroppedBitmap(bitmap, radius);
		// roundBitmap=ImageUtils.setCircularInnerGlow(roundBitmap, 0xFFBAB399,
		// 4, 1);
		// canvas.drawBitmap(roundBitmap, w / 2 - radius, 8, null);

		final Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		paint.setDither(true);
		paint.setColor(mBorderColor);
		paint.setStrokeWidth(5);
		paint.setStyle(Paint.Style.STROKE);

		canvas.drawCircle(w / 2, h / 2, radius + mBorderThickness -5, paint);
		
		Matrix m =new Matrix();
		m.postScale(0.93f, 0.93f);
		roundBitmap = Bitmap.createBitmap(roundBitmap, 0, 0, roundBitmap.getWidth(), roundBitmap.getHeight(), m, true);
		canvas.drawBitmap(roundBitmap, w / 2 -roundBitmap.getWidth()/2, h / 2 -roundBitmap.getHeight()/2, null);
		bitmap.recycle();
		roundBitmap.recycle();
	}

	public Bitmap getBitmap(Bitmap b1, Bitmap b2) {
		if (!b1.isMutable()) {
			// 设置图片为背景为透明
			b1 = b1.copy(Bitmap.Config.ARGB_8888, true);
		}
		Paint paint = new Paint();

		Canvas canvas = new Canvas(b1);
		canvas.drawBitmap(b2, 0, 0, paint);// 叠加新图b2
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		return b1;
	}

	public static Bitmap getCroppedBitmap(Bitmap bmp, int radius) {
		Bitmap scaledSrcBmp;
		int diameter = radius * 2;
		if (bmp.getWidth() != diameter || bmp.getHeight() != diameter)
			scaledSrcBmp = Bitmap.createScaledBitmap(bmp, diameter, diameter,
					false);
		else
			scaledSrcBmp = bmp;
		Bitmap output = Bitmap.createBitmap(scaledSrcBmp.getWidth(),
				scaledSrcBmp.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, scaledSrcBmp.getWidth(),
				scaledSrcBmp.getHeight());

		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		paint.setDither(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(Color.parseColor("#BAB399"));
		canvas.drawCircle(scaledSrcBmp.getWidth() / 2,
				scaledSrcBmp.getHeight() / 2, scaledSrcBmp.getWidth() / 2,
				paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(scaledSrcBmp, rect, rect, paint);
		scaledSrcBmp.recycle();
		return output;
	}

}

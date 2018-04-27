package com.bandlink.air.club;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.bandlink.air.R;

public class BorderImageView extends ImageView {
	private int borderWidth;
	private int borderColor;
	private float radius;
	private Paint cornerPaint;

	public BorderImageView(Context context) {
		super(context);
	}

	public BorderImageView(Context context, int borderWidth, int borderColor,
			int radius) {
		super(context);
		cornerPaint = new Paint();
		cornerPaint.setAntiAlias(true);
		this.borderWidth = borderWidth;
		this.borderColor = borderColor;
		this.radius = radius;
	}

	public BorderImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initAttr(attrs);
	}

	public BorderImageView(Context context, AttributeSet attrs, int s) {
		super(context, attrs, s);
		initAttr(attrs);
	}

	private void initAttr(AttributeSet attrs) {
		if (attrs == null) {
			return;
		}
		cornerPaint = new Paint();
		cornerPaint.setAntiAlias(true);
		TypedArray styled = getContext().obtainStyledAttributes(attrs,
				R.styleable.BorderImageView);
		borderWidth = styled.getDimensionPixelOffset(
				R.styleable.BorderImageView_borderwidth, 3);
		borderColor = styled.getColor(R.styleable.BorderImageView_bordercolor,
				0xffffff);
		radius = styled.getFloat(R.styleable.BorderImageView_radius, 5);
		styled.recycle();
	}

	Bitmap btm = null;

	@Override
	public void setImageBitmap(Bitmap bm) {
		// TODO Auto-generated method stub
		// btm = bm;
		super.setImageBitmap(bm);
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
		// Bitmap b = ((BitmapDrawable) drawable).getBitmap();

		Bitmap bitmap = btm;
		if (bitmap == null) {
			bitmap = ((BitmapDrawable) drawable).getBitmap();
		}
		int w = getWidth(), h = getHeight();
		Matrix m = new Matrix();
		m.postScale(((float) w / (float) bitmap.getWidth()),
				((float) h / (float) bitmap.getHeight()));

		bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), m, true);

		Bitmap roundBitmap = getCroppedBitmap(bitmap, radius);

		final Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		paint.setDither(true);
		paint.setColor(borderColor);
		paint.setStrokeWidth(borderWidth);
		Rect rect = new Rect(0, 0, roundBitmap.getWidth(),
				roundBitmap.getHeight());
		canvas.drawRoundRect(new RectF(rect), radius, radius, paint);
		canvas.drawBitmap(roundBitmap, w / 2 - roundBitmap.getWidth() / 2, h
				/ 2 - roundBitmap.getHeight() / 2, null);
	}

	public Bitmap getCroppedBitmap(Bitmap bmp, float radius) {
		Bitmap output = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(),
				Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final Paint paint = new Paint();
		final Rect rect = new Rect(borderWidth, borderWidth, bmp.getWidth()
				- borderWidth, bmp.getHeight() - borderWidth);
		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		paint.setDither(true);
		canvas.drawARGB(0, 0, 0, 0);
		// 图片圆角为边框圆角的70%
		canvas.drawRoundRect(new RectF(rect), radius * 0.7f, radius * 0.7f,
				paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bmp, rect, rect, paint);
		return output;
	}

}

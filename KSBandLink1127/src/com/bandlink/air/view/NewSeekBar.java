package com.bandlink.air.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.SeekBar;

import com.bandlink.air.R;

public class NewSeekBar extends SeekBar {

	public NewSeekBar(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public NewSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	 
	public void setNumberThumb(int per) { 
		
		super.setThumb(getCroppedBitmap(per+""));
	}

	public Drawable getCroppedBitmap(String per) {

		Matrix m = new Matrix();
		m.postScale(0.8f, 0.8f);

		Bitmap src = BitmapFactory.decodeResource(getResources(),
				R.drawable.pos);
		int w = src.getWidth();
		int h = src.getHeight();
		Bitmap output = Bitmap.createBitmap(w, (int)(h*2.3), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG));
		final Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setTextSize(getResources().getDimension(R.dimen.text_small_s));
		paint.setColor(Color.WHITE);
		paint.setTextAlign(Align.CENTER);
		// paint.setFakeBoldText(true);
		canvas.drawBitmap(src, 0, 0, paint);
		canvas.drawText(per + "", w * 0.5f, h * 0.36f, paint);
		src =Bitmap.createBitmap(output, 0, 0, output.getWidth(),
		output.getHeight(), m, true);
		return new BitmapDrawable(getResources(),src);	
	}

}

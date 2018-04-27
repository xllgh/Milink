package com.bandlink.air.simple;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import com.bandlink.air.R;

public class BG extends View {

	private int screenH, screenW;
	private float centerX, centerY, r;
	private RectF main;
	private PaintFlagsDrawFilter filter;
	private Paint pointPaint;
	private int mode = 0;

	public BG(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		prepare(context, attrs);
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		setMeasuredDimension(widthMeasureSpec, (int) (2 * r) + 60);
	}

	private void prepare(Context context, AttributeSet attrs) {
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		screenH = dm.heightPixels;
		screenW = dm.widthPixels;
		centerX = screenW / 2;

		r = screenW
				* (0.44f - ((float) ((float) (1280f / 720f) - (float) screenH
						/ (float) screenW)) / 4);

		centerY = r + 30;
		main = new RectF(centerX - r, centerY - r, centerX + r, centerY + r);

		pointPaint = new Paint();
		pointPaint.setStyle(Paint.Style.STROKE);
		pointPaint.setColor(Color.parseColor("#55ffffff"));
		pointPaint.setDither(true);
		pointPaint.setStrokeCap(Cap.ROUND);
		pointPaint.setStrokeWidth(context.getResources().getDimension(
				R.dimen.point));
		pointPaint.setAntiAlias(true);

		filter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG);

		// this.setBackgroundResource(R.drawable.point);

	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		canvas.setDrawFilter(filter);
		if (mode == NewProgress.WEIGHTMODE || mode == NewProgress.TEMPMODE) {
			for (int i = 0; i < 83; i++) {

				canvas.drawArc(main, -90 + (i * 4) + 15, 0.05f, false,
						pointPaint);
			}
		} else {
			for (int i = 0; i < 90; i++) {
				canvas.drawArc(main, i * 4, 0.05f, false, pointPaint);
			}
		}

		super.onDraw(canvas);
	}

}

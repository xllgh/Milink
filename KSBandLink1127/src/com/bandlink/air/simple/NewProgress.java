package com.bandlink.air.simple;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import com.bandlink.air.R;
import com.bandlink.air.util.SharePreUtils;

public class NewProgress extends View {

	private int screenW;
	private int screenH;
	// 进度画笔
	private Paint progressBase;
	private RectF main;
	public float progress = 0;
	private float r;
	private PaintFlagsDrawFilter filter;
	private int centerX, centerY;
	public final static int STEPMODE = 0;
	public final static int WEIGHTMODE = 1;
	public final static int TEMPMODE = 2;
	private int mode;
	private Paint standradWeightPaint;
	private OnNewProgressDown onDown;
	private boolean touchable = true;
	private Context context;
	private SharedPreferences share;
	private float standard = 52.0f;
	private static float downDis = 240;

	public NewProgress(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		prepare(context, null);
	}

	public NewProgress(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		prepare(context, attrs);
	}

	public static float getDownDis() {
		return downDis;
	}

	public boolean isTouchable() {
		return touchable;
	}

	public void setTouchable(boolean touchable) {
		this.touchable = touchable;
	}

	public double getProgress() {
		return progress;
	}

	public void setProgress(float progress) {
		this.progress = progress;
		this.postInvalidate();
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
		if (mode == TEMPMODE) {
			standard = share.getFloat(SharePreUtils.AlarmTempe, 38.5f);
		}
		postInvalidate();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		setMeasuredDimension(widthMeasureSpec, (int) (2 * r) + 60);
	}

	private void prepare(Context context, AttributeSet attrs) {
		this.context = context;
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		screenH = dm.heightPixels;
		screenW = dm.widthPixels;
		centerX = screenW / 2;
		downDis = screenH / 5f;
		share = context
				.getSharedPreferences(
						SharePreUtils.APP_ACTION,
						Context.MODE_PRIVATE);
		if (share != null) {
			standard = share.getFloat("standard", 52.0f);
		}

		r = screenW
				* (0.44f - ((float) ((float) (1280f / 720f) - (float) screenH
						/ (float) screenW)) / 4);
		centerY = (int) (r + 30);
		main = new RectF(centerX - r, centerY - r, centerX + r, centerY + r);
		progressBase = new Paint();

		progressBase.setStyle(Paint.Style.STROKE);
		progressBase.setStrokeWidth(context.getResources().getDimension(
				R.dimen.point) * 2.5f);
		progressBase.setColor(Color.parseColor("#ffffff"));
		progressBase.setDither(true);
		filter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG);
		progressBase.setStrokeCap(Cap.ROUND);
		progressBase.setAntiAlias(true);

		standradWeightPaint = new Paint();
		standradWeightPaint.setAntiAlias(true);
		standradWeightPaint.setColor(Color.parseColor("#99ffffff"));
		standradWeightPaint.setTextSize(context.getResources().getDimension(
				R.dimen.text_middle));

		// this.setBackgroundResource(R.drawable.point);

	}

	public void setCircleColor(int color) {
		if (progressBase != null) {
			progressBase.setColor(color);
			postInvalidate();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		canvas.setDrawFilter(filter);
		if (mode == WEIGHTMODE) {
			progress = progress > 1 ? 1 : progress;
			canvas.drawArc(main, -90f + 15, (360 - 30) * progress, false,
					progressBase);
			canvas.drawText(
					String.format("%1$.1f", standard) + "KG",
					centerX
							- (standradWeightPaint.measureText(String.format(
									"%1$.1f", standard) + "KG") / 2),
					centerY
							- r
							+ context.getResources()
									.getDimension(R.dimen.point) * 2f,
					standradWeightPaint);
		} else if (mode == STEPMODE) {
			canvas.drawArc(main, -90f, 360 * progress, false, progressBase);
		} else if (mode == TEMPMODE) {
			progress = progress > 1 ? 1 : progress;
			canvas.drawArc(main, -90f + 15, (360 - 30) * progress, false,
					progressBase);

			canvas.drawText(
					context.getString(R.string.battery),
					centerX
							- (standradWeightPaint.measureText(context
									.getString(R.string.battery)) / 2),
					centerY
							- r
							+ context.getResources()
									.getDimension(R.dimen.point) * 4f,
					standradWeightPaint);
		}

		// drawBG(canvas);
		super.onDraw(canvas);
	}

	float posY, posX;
	float x, y;
	float offy;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		if (!touchable) {
			return false;
		}
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			posY = event.getY();
			posX = event.getX();
			offy = posY;
			break;
		case MotionEvent.ACTION_MOVE:
			if (offy - event.getY() > 0) {
				break;
			}
			offy = event.getY();
			if (Math.abs(event.getY() - posY) > Math.abs(event.getX() - posX)) {
				// Y轴的移动量大
				if (Math.abs(event.getY() - posY) > 30
						&& Math.abs(event.getY() - posY) < downDis) {
					if (onDown != null)
						onDown.onProgressDowning(x);
				} else if (Math.abs(event.getY() - posY) >= downDis) {
					if (onDown != null)
						onDown.onProgressAchieve(x);
				}

			} else {
				// X轴的移动量大
			}
			x = (event.getY() - posY);
			y = (event.getX() - posX);

			break;
		case MotionEvent.ACTION_UP:
			if (Math.abs(event.getY() - posY) > Math.abs(event.getX() - posX)) {
				// Y轴的移动量大
				if (x > 30 && x < downDis) {
					if (onDown != null)
						onDown.onProgressCancle(x);
				} else if (x >= downDis) {
					if (onDown != null)
						onDown.onProgressHadDown(x);
				} else if (event.getY() < main.bottom
						&& main.top < event.getY()) {
					if (onDown != null)
						onDown.onProgressClick(x, this);
				}
			} else {
				// X轴的移动量大
				if (event.getX() - posX > centerX * 1 / 3) {
					// 向右
					if (onDown != null)
						onDown.onOtherEvent(2);
				} else if (posX - event.getX() > centerX * 1 / 3) {
					// 向左
					if (onDown != null)
						onDown.onOtherEvent(1);
				} else if (event.getY() < main.bottom
						&& main.top < event.getY()) {
					if (onDown != null)
						onDown.onProgressClick(x, this);
				}
			}

			x = 0;
			break;
		}
		return true;
	}

	public void setOnNewProgressDown(OnNewProgressDown onDown) {
		this.onDown = onDown;
	}

	public interface OnNewProgressDown {
		public void onProgressDowning(float x);

		public void onProgressCancle(float x);

		public void onProgressHadDown(float x);

		public void onProgressAchieve(float x);

		public void onProgressClick(float x, View v);

		public void onOtherEvent(int d);
	}

}

package com.bandlink.air.simple.ecg;

import java.io.UnsupportedEncodingException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
//import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
  

public class SurfaceViewWave extends SurfaceView implements SurfaceHolder.Callback {

	private final static String TAG = "SurfaceViewWave";
	private final static boolean DEBUG = true;
	
	private int x_position = 0;
	private int y_position = 0;
	private int old_x_position = 0;
	private int old_y_position = getWaveQueueDefalutValue();

	// Default Value = 1
	private double heightScale = 1;

	// Default Weight/Height Value
	private int viewWidth = 300;
	private int viewHeight = 300;

	private static final int surfaceViewUpdateTime = 100;
	
	// Vertical Length
	private static final int verticalGridLength = 50;
	private static final int verticalSmallGridLength = 10;
	// Horizontal Length
	private static int horizontalGridLength = 50;
	private static int horizontalSmallGridLength = 10;
	
	private static final int borderLength = 10;
	
	private int d[];

	private SurfaceHolder mySurfaceHolder;
	private WaveQueue waveQueue = new WaveQueue(viewWidth);;
	private Bitmap mBitmap;
	private Paint wavePaint;
	private Paint backgroundGridPaint;
	private Paint backgroundGridSmallPaint;
	private Paint backgroundIndentPaint;
	private Path backgroundPaintVerticalGridPath;
	private Path backgroundPaintSmallVerticalGridPath;
	private Path backgroundPaintHorizontalGridPath;
	private Path backgroundPaintSmallHorizontalGridPath;
	private Path backgroundPaintIndentPath;

	private final Handler drawHandler = new Handler();

	public SurfaceViewWave(Context context) {
		super(context);
		initWaveDrawer();
	}

	public SurfaceViewWave(Context context, AttributeSet attrs) {
		super(context, attrs);
		initWaveDrawer();
	}

	public SurfaceViewWave(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initWaveDrawer();
	}

	public void initWaveDrawer() {

		mBitmap = Bitmap.createBitmap(getViewWidth(), getViewHeight(),
				Bitmap.Config.ARGB_8888);

		mySurfaceHolder = this.getHolder();
		mySurfaceHolder.addCallback(this);

		Canvas mCanvas = new Canvas();
		mCanvas.setBitmap(mBitmap);

		wavePaint = new Paint();
		wavePaint.setDither(true);
		wavePaint.setColor(0xFFFF0000);
		wavePaint.setStyle(Paint.Style.STROKE);
		wavePaint.setStrokeJoin(Paint.Join.ROUND);
		wavePaint.setStrokeCap(Paint.Cap.ROUND);
		wavePaint.setStrokeWidth(2);

		backgroundGridPaint = new Paint();
		backgroundGridPaint.setDither(true);
		backgroundGridPaint.setColor(0xFF2222FF);
		backgroundGridPaint.setStyle(Paint.Style.STROKE);
		backgroundGridPaint.setStrokeJoin(Paint.Join.ROUND);
		backgroundGridPaint.setStrokeCap(Paint.Cap.ROUND);
		backgroundGridPaint.setStrokeWidth(2);

		backgroundGridSmallPaint = new Paint();
		backgroundGridSmallPaint.setDither(true);
		backgroundGridSmallPaint.setColor(0x882222FF);
		backgroundGridSmallPaint.setStyle(Paint.Style.STROKE);
		backgroundGridSmallPaint.setStrokeJoin(Paint.Join.ROUND);
		backgroundGridSmallPaint.setStrokeCap(Paint.Cap.ROUND);
		backgroundGridSmallPaint.setStrokeWidth(1);

		backgroundIndentPaint = new Paint();
		backgroundIndentPaint.setDither(true);
		backgroundIndentPaint.setColor(0xFF444444);
		backgroundIndentPaint.setStyle(Paint.Style.STROKE);
		backgroundIndentPaint.setStrokeJoin(Paint.Join.ROUND);
		backgroundIndentPaint.setStrokeCap(Paint.Cap.ROUND);
		backgroundIndentPaint.setStrokeWidth(10);

	}

	private Runnable drawRunner = new Runnable() {

		@Override
		public void run() {
			doDraw();
			drawHandler.postDelayed(drawRunner, surfaceViewUpdateTime);
		}
	};

	public void addValueStr(String str) {

		try {
			// ASCII type is "ISO-8859-1"
			waveQueue.addByteData(str.getBytes(BtEcgConstant.BT_ECG_ASCII_TYPE));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

	}

	public void addValueByte(byte[] data) {
		//Log.v(TAG, "Add new Value:" + String.valueOf(data));
		waveQueue.addByteData(data);

	}

	void doDraw() {
		Canvas canvas = getHolder().lockCanvas(
				new Rect(0, 0, getViewWidth(), getViewHeight()));
		if (canvas != null) {
			canvas.drawColor(Color.BLACK);
			// draw grid line
			canvas.drawPath(backgroundPaintVerticalGridPath,
					backgroundGridPaint);
			canvas.drawPath(backgroundPaintSmallVerticalGridPath,
					backgroundGridSmallPaint);
			canvas.drawPath(backgroundPaintHorizontalGridPath,
					backgroundGridPaint);
			canvas.drawPath(backgroundPaintSmallHorizontalGridPath,
					backgroundGridSmallPaint);
			canvas.drawPath(backgroundPaintIndentPath, backgroundIndentPaint);

			int i, x;
			d = waveQueue.getAllData();
			// Log.i(TAG, String.valueOf(d.length));
			old_x_position = 0;
			// old_y_position = getWaveQueueDefalutValue();

			int indent = borderLength + 5;
			// for (i=0;i<mllValue.size();i++){
			for (i = 0; i < d.length && i < getViewWidth() - (indent << 1); i++) {

				x = x_position;
				x_position++;
				y_position = d[i];
				
				canvas.drawLine(old_x_position + indent,
						getDrawHight(old_y_position), x + indent,
						getDrawHight(y_position), wavePaint);

				old_x_position = x;
				old_y_position = y_position;
			}
			//draw ECG renew line
			canvas.drawLine(waveQueue.getFrontPosition() + indent, indent,
					waveQueue.getFrontPosition() + indent, getViewHeight()
							- indent, wavePaint);
			// 瑕忛浂 X搴ф
			x_position = 0;
			//canvas.rotate(90);
			getHolder().unlockCanvasAndPost(canvas);
		}

	}

	protected Path getBackgrundHorizontalGrid(int range, int indentLength) {
		Path grid = new Path();

		// Horizontal
		for (int i = 1; i < (getViewHeight() - (indentLength << 2) / range); i++) {
			grid.moveTo(indentLength<<1 , getViewHeight() - range * i);
			grid.lineTo(getViewWidth() - indentLength, getViewHeight() - range
					* i);
		}
		return grid;
	}

	private Path getBackgroundVerticalGrid(int range, int indentLength) {
		Path grid = new Path();
		// Vertical
		for (int i = 1; (i * range) < getViewWidth() - indentLength ; ++i) {
			grid.moveTo(range * i + indentLength, indentLength);
			grid.lineTo(range * i  + indentLength, getViewHeight() - indentLength);
		}
		return grid;
	}

	private Path getBackgrundIndentGrid(int indentLength) {
		Path grid = new Path();

		grid.moveTo(indentLength, indentLength);
		grid.lineTo(indentLength, getViewHeight() - indentLength);
		grid.lineTo(getViewWidth() - indentLength, getViewHeight()
				- indentLength);
		grid.lineTo(getViewWidth() - indentLength, indentLength);
		grid.close();

		return grid;

	}

	private int getWaveQueueDefalutValue() {
		//return getViewHeight() >> 1;
		// 112 is guest value (refance value)
		return 112;
	}

	public void setHeightScale(double h) {
		this.heightScale = h;
	}

	public double getHeightScale() {
		return this.heightScale;
	}

	private int getDrawHight(int height) {
		return getViewHeight() - (int) (height * getHeightScale());
	}

	public int getViewWidth() {
		return viewWidth;
	}

	public void setViewWidth(int viewWidth) {
		this.viewWidth = viewWidth;
	}

	public int getViewHeight() {
		return viewHeight;
	}

	public void setViewHeight(int viewHeight) {
		this.viewHeight = viewHeight;
	}

	 

	private static int getHorizontalSmallGridLength() {
		return horizontalSmallGridLength;
	}

	private static void setHorizontalSmallGridLength(int horizontalSmallGridLength) {
		SurfaceViewWave.horizontalSmallGridLength = horizontalSmallGridLength;
	}

	private static int getHorizontalGridLength() {
		return horizontalGridLength;
	}

	private static void setHorizontalGridLength(int horizontalGridLength) {
		SurfaceViewWave.horizontalGridLength = horizontalGridLength;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		drawHandler.post(drawRunner);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		setViewHeight(height);
		setViewWidth(width);

		waveQueue = new WaveQueue(getViewWidth(), getWaveQueueDefalutValue());
		//LogManager.info(TAG, DEBUG, "Height scale Value: " + String.valueOf(height / BtEcgConstant.BT_ECG_WAVE_HEIGHT_SCALE_BASE_VALUE));
		setHeightScale(height / BtEcgConstant.BT_ECG_WAVE_HEIGHT_SCALE_BASE_VALUE);
		
		setHorizontalSmallGridLength((int)(getViewHeight()/25.0));
		setHorizontalGridLength(getHorizontalSmallGridLength()*5);
		

		backgroundPaintVerticalGridPath = getBackgroundVerticalGrid(verticalGridLength, borderLength);
		backgroundPaintSmallVerticalGridPath = getBackgroundVerticalGrid(verticalSmallGridLength, borderLength);
		backgroundPaintHorizontalGridPath = getBackgrundHorizontalGrid(getHorizontalGridLength(), borderLength);
		backgroundPaintSmallHorizontalGridPath = getBackgrundHorizontalGrid(getHorizontalSmallGridLength(),
				borderLength);
		backgroundPaintIndentPath = getBackgrundIndentGrid(borderLength);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}

}
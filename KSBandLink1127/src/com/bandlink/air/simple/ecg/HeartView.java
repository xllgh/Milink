package com.bandlink.air.simple.ecg;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class HeartView extends SurfaceView implements Callback {
	DrawRun dr;
	ArrayList<Integer> dataAll;

	float cuX = 0, cuY = 0;
	Paint paint;

	public HeartView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		SurfaceHolder ho = this.getHolder();
		ho.addCallback(this);

		paint = new Paint();
		paint.setColor(Color.WHITE);

	}

	public void setHeatData(ArrayList<Integer> dataAl) {

		if (dataAll == null || dataAll.size() == 0) {
			cuY = dataAl.get(0);
		}
		this.dataAll = dataAl;
		if (dr != null && !dr.isRun) {
			dr.isRun = true;
		}
		//onMeasure(0, 0);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		dr = new DrawRun(holder);
		dr.isRun = true;
		new Thread(dr).start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		if (dr != null) {
			dr.isRun = false;
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		int w = getWidth();
		if (dataAll != null && dataAll.size()>0) {
			w = dataAll.size();
		}
		setMeasuredDimension(w, getHeight());
	}

	class DrawRun implements Runnable {

		SurfaceHolder holder;
		boolean isRun = false;

		public DrawRun(SurfaceHolder holder) {
			super();
			this.holder = holder;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (true) {
				if (isRun) {
					if (dataAll != null) {
						synchronized (holder) {
							ArrayList<Integer> tempData = dataAll;

							if (tempData.size() != cuX) {
								// canvas.drawColor(Color.BLACK);
								Canvas canvas = holder.lockCanvas(new Rect(
										(int) cuX, 0, (int) (205 + cuX),
										getHeight()));
								System.out
										.println((int) cuX + "-"
												+ (int) (205 + cuX) + "-"
												+ getHeight());
								int i = 0;
								for (int x : tempData) {
									canvas.drawLine(cuX, cuY, ++cuX, x, paint);
									cuY = x;

								}
								isRun = false;
								holder.unlockCanvasAndPost(canvas);
							}

						}
					}

				}
			}
		}

	}

}

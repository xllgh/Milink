package com.bandlink.air;

import android.graphics.Bitmap;
import android.os.Handler;
import android.view.SurfaceHolder.Callback;

import com.bandlink.air.Zxing.view.ViewfinderView;
import com.bandlink.air.util.LovefitActivity;
import com.google.zxing.Result;

public abstract class CaptureBaseActivity extends LovefitActivity implements Callback {


	public abstract Handler getHandler();

	public abstract ViewfinderView getViewfinderView();
	public abstract void drawViewfinder();
	public abstract void handleDecode(Result obj, Bitmap barcode);
}
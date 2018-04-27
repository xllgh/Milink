package com.bandlink.air;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Util;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ScanFriendFragment extends Fragment {

	SharedPreferences sp;
	Button btn_scan;
	String uid;
	String result;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		sp = getActivity().getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		View v = inflater.inflate(R.layout.newfriend_frame1, null);
		btn_scan = (Button) v.findViewById(R.id.btn_scan);
		btn_scan.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(getActivity(),
						DeviceBindScanActivity.class);
				i.putExtra("fromfriend", true);
				startActivity(i);
			}
		});
		ImageView image = (ImageView) v.findViewById(R.id.zxing);
		try {
			image.setImageBitmap(CreateTwoDCode("uid:" + sp.getInt("UID", -1),
					getActivity()));
		} catch (WriterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			Bitmap p = ImageLoader.getInstance().loadImageSync(
					HttpUtlis.AVATAR_URL
							+ MenuFragment.getAvatarUrl(sp.getInt("UID", -1)
									+ "", "big"));
			p = Util.getTransparentBitmap(p, 130);

			image.setBackgroundDrawable(new BitmapDrawable(p));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// image.setBackgroundResource(R.drawable.corner_border_white);
		IntentFilter counterActionFilter = new IntentFilter(
				"com.milink.android.lovewalk.scanfriend");

		getActivity().registerReceiver(re, counterActionFilter);

		return v;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		getActivity().unregisterReceiver(re);
		super.onDestroy();
	}

	/***
	 * ���յ�ɨ����ѽ��Ĺ㲥
	 */
	BroadcastReceiver re = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			uid = intent.getStringExtra("uid").toString().substring(4);
			new Thread(new Runnable() {

				public void run() {
					// TODO Auto-generated method stub
					String urlStr = HttpUtlis.BASE_URL + "/user/addFriendScan";
					Message msg = handler.obtainMessage();
					Map<String, String> map = new HashMap<String, String>();
					map.put("session", sp.getString("session_id", ""));
					map.put("fuid", uid);
					map.put("note", "lovefit");
					try {
						result = HttpUtlis.getRequest(urlStr, map);
						if (result != null) {
							JSONObject json;
							json = new JSONObject(result);
							int s = Integer.valueOf(json.getString("status"));
							if (s == 0) {
								msg.what = 0;
								handler.sendMessage(msg);
							} else if (s == 1) {
								msg.what = 1;
								handler.sendMessage(msg);
							} else if (s == 2) {
								msg.what = 2;
								handler.sendMessage(msg);
							} else if (s == 3) {
								msg.what = 3;
								handler.sendMessage(msg);
							} else if (s == 4) {
								msg.what = 4;
								handler.sendMessage(msg);
							}
						}

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();
		}

	};

	@SuppressLint("NewApi")
	public static Bitmap CreateTwoDCode(String content, Activity context)
			throws WriterException {

		int width1, height1;

		Display display = context.getWindowManager().getDefaultDisplay();
		if (android.os.Build.VERSION.SDK_INT >= 13) {
			Point size = new Point();
			display.getSize(size);
			width1 = size.x;
			height1 = size.y;
		} else {
			width1 = display.getWidth(); // deprecated
			height1 = display.getHeight(); // deprecated
		}
		int ss = Math.min(width1, height1) * 1 / 2;
		BitMatrix matrix = new MultiFormatWriter().encode(content,
				BarcodeFormat.QR_CODE, ss, ss);
		int width = matrix.getWidth();
		int height = matrix.getHeight();
		int[] pixels = new int[width * height];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (matrix.get(x, y)) {
					pixels[y * width + x] = 0xff000000;
				}
			}
		}

		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				btn_scan.setText(getString(R.string.add_friend_ok));
				break;
			case 1:
				Toast.makeText(getActivity(), getString(R.string.no_param), 1)
						.show();
				break;
			case 2:
				Toast.makeText(getActivity(), getString(R.string.no_user), 1)
						.show();
				break;
			case 3:
				Toast.makeText(getActivity(), getString(R.string.not_add_own),
						1).show();
				break;
			case 4:
				Toast.makeText(getActivity(), getString(R.string.ID_not_exist),
						1).show();
				break;
			}
		}
	};

}
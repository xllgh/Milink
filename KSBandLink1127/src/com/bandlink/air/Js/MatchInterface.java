package com.bandlink.air.Js;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.webkit.WebView;

import com.bandlink.air.MatchDetailActivity;
import com.bandlink.air.MatchFragment;
import com.bandlink.air.util.SharePreUtils;

public class MatchInterface {
	Context context;
	WebView web;
	ProgressDialog progressDialog;
	SharedPreferences share;
	MatchFragment match;

	public MatchInterface(MatchFragment m, Context context, WebView web) {
		super();
		this.context = context;
		this.web = web;
		this.match = m;
		share = context
				.getSharedPreferences(
						SharePreUtils.APP_ACTION,
						Context.MODE_PRIVATE);

	}

	/***
	 * 显示大图
	 * 
	 * @param url
	 *            图片url
	 */
	// public void getBigImg(Drawable d) {
	//
	// View view = LayoutInflater.from(context).inflate(R.layout.pop_bigimg,
	// null);
	// final PopupWindow popwindow = new PopupWindow(view,
	// LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true);
	// popwindow.setOutsideTouchable(true);
	// popwindow.setBackgroundDrawable(new BitmapDrawable());
	//
	// GestureImageView iv = (GestureImageView) view.findViewById(R.id.img);
	// iv.setImageDrawable(d);
	// popwindow.showAtLocation(web.getRootView(), Gravity.CENTER, 0, 0);
	// view.setOnClickListener(new OnClickListener() {
	//
	// @Override
	// public void onClick(View v) {
	// // TODO Auto-generated method stub
	// popwindow.dismiss();
	// }
	// });
	// }
	//
	// Handler imageHandler = new Handler() {
	//
	// @Override
	// public void handleMessage(Message msg) {
	// if (progressDialog != null && progressDialog.isShowing()) {
	// progressDialog.dismiss();
	// }
	// if (msg.what == 0) {
	// getBigImg((Drawable) msg.obj);
	// }
	// super.handleMessage(msg);
	// }
	//
	// };
	//
	// public ProgressDialog initProgressDialog() {
	// ProgressDialog progressDialog = ProgressDialog.show(context,
	// context.getString(R.string.data_wait),
	// context.getString(R.string.data_getting), true);
	// progressDialog.setCancelable(true);
	//
	// LayoutInflater inflater = LayoutInflater.from(context);
	// View v = inflater.inflate(R.layout.loading, null);// 寰楀埌鍔犺浇view
	// TextView tvMsg = (TextView) v.findViewById(R.id.tvTitle);
	//
	// if (tvMsg != null) {
	// tvMsg.setText(context.getString(R.string.data_getting));
	// }
	// progressDialog.setContentView(v);
	// progressDialog
	// .setOnCancelListener(new DialogInterface.OnCancelListener() {
	//
	// @Override
	// public void onCancel(DialogInterface dialog) {
	// // TODO Auto-generated method stub
	//
	// }
	// });
	//
	// return progressDialog;
	// }
	// public void showBigImg(final String path) {
	// progressDialog = initProgressDialog();
	// new Thread(new Runnable() {
	//
	// @Override
	// public void run() {
	// // TODO Auto-generated method stub
	// try {
	// URL url = new URL(path);
	// Drawable d = BitmapDrawable.createFromStream(
	// url.openStream(), "");
	// imageHandler.obtainMessage(0, d).sendToTarget();
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// }
	//
	// }).start();
	// }

	public void getJsMyMatch() {
		match.getMyMatch();
	}

	public void getJsAllMatch() {
		match.getAllMatch();

	}

	public void getJsMoreMatch() {
		match.getMoreMatch();
	}

	public void getJsMyMoreMatch() {
		match.getMyMoreMatch();
	}

	public void searchJsMatch(String search, String tab) {
		int t = Integer.parseInt(tab);
		if (t == 2) {
			match.searchMyMatch(search);
		} else {
			match.searchMatch(search);
		}

	}

	public void getJsMatchDetail(String matchid, String matchfrom) {
		Intent intent = new Intent(context, MatchDetailActivity.class);
		intent.putExtra("id", Integer.parseInt(matchid));
		intent.putExtra("from", Integer.parseInt(matchfrom));
		context.startActivity(intent);
	}

	public void getCurrentTab(String tab) {
		MatchFragment.tab = Integer.parseInt(tab);
	}

}
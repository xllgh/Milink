package com.bandlink.air.Js;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.webkit.WebView;

import com.bandlink.air.MatchDetailActivity;
import com.bandlink.air.R;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Util;
import com.bandlink.air.view.NoRegisterDialog;

public class MatchDetailJsInterface {
	Context context;
	SharedPreferences share;
	WebView web;
	MatchDetailActivity match;

	public MatchDetailJsInterface(MatchDetailActivity match, WebView web) {
		super();
		this.context = match;
		this.web = web;
		share = context
				.getSharedPreferences(
						SharePreUtils.APP_ACTION,
						Context.MODE_PRIVATE);
		this.match = match;
		 
		context =Util.getThemeContext(context); 
	}

	public void getCurrentTab(String tab) {
		MatchDetailActivity.tab = Integer.parseInt(tab); 
	}

	public void showBigImg(final String url) {
		match.showBigImg(url);
	}

	public void showOperate(final String uid, final String name) {
		match.showOperate(uid, name);
	}

	public void jqJsMatch(String ismember) {
		if (share.getInt("ISMEMBER", 0) == 0) {
			NoRegisterDialog d = new NoRegisterDialog(context,
					R.string.no_register, R.string.no_register_content);
			d.show();
		} else {
			 

			if (ismember.equals("1")) {
				AlertDialog alertDialog = new AlertDialog.Builder(context)
						.setMessage(context.getString(R.string.quit_match_ensure))
						.setTitle(context.getString(R.string.exit_ensure))
						.setPositiveButton(R.string.ok,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										match.quitMatch();
									}
								})
						.setNegativeButton(R.string.cancel,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
									}
								}).show();
			} else {
				match.joinMatch();
			}
		}

	}

}
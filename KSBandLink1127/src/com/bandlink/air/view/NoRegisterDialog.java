package com.bandlink.air.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bandlink.air.R;
import com.bandlink.air.user.LoginActivity;
import com.bandlink.air.user.Register;

public class NoRegisterDialog extends AlertDialog {

	public NoRegisterDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		// TODO Auto-generated constructor stub
	}

	public NoRegisterDialog(final Context context, int title_str,
			int content_str) {
		super(context);
		// TODO Auto-generated constructor stub
		View view = LayoutInflater.from(context).inflate(
				R.layout.noregisterdialog, null);
		ImageButton cancel = (ImageButton) view.findViewById(R.id.cancel);
		Button register = (Button) view.findViewById(R.id.register);
		Button login = (Button) view.findViewById(R.id.login);
		TextView title = (TextView) view.findViewById(R.id.title);
		TextView content = (TextView) view.findViewById(R.id.content);
		title.setText(title_str);
		content.setText(content_str);
		cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				cancel();

			}
		});

		register.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (context.toString().equals("PersonalSettings.this")) {
					dismiss();
					Intent intent = new Intent();
					intent.putExtra("per", "per");
					intent.setClass(context, Register.class);
					context.startActivity(intent);
				} else {
					dismiss();
					Intent intent = new Intent(context, Register.class);
					context.startActivity(intent);
				}
			}
		});

		login.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				dismiss();
				Intent intent = new Intent(context, LoginActivity.class);
				context.startActivity(intent);

			}
		});
		setView(view, 0, 0, 0, 0);

	}
	public NoRegisterDialog(final Context context, int title_str,
			int content_str,final boolean f) {
		super(context);
		// TODO Auto-generated constructor stub
		
		View view = LayoutInflater.from(context).inflate(
				R.layout.noregisterdialog, null);
		ImageButton cancel = (ImageButton) view.findViewById(R.id.cancel);
		Button register = (Button) view.findViewById(R.id.register);
		Button login = (Button) view.findViewById(R.id.login);
		TextView title = (TextView) view.findViewById(R.id.title);
		TextView content = (TextView) view.findViewById(R.id.content);
		title.setText(title_str);
		content.setText(content_str);
		cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				cancel();

			}
		});

		register.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (f) {
					dismiss();
					Intent intent = new Intent();
					intent.putExtra("per", "per");
					intent.setClass(context, Register.class);
					context.startActivity(intent);
				} else {
					dismiss();
					Intent intent = new Intent(context, Register.class);
					context.startActivity(intent);
				}
			}
		});

		login.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				dismiss();
				Intent intent = new Intent(context, LoginActivity.class);
				context.startActivity(intent);

			}
		});
		setView(view, 0, 0, 0, 0);

	}

}

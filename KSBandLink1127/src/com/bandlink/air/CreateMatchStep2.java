package com.bandlink.air;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.ImageFolderActivity;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.MApplication;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class CreateMatchStep2 extends LovefitActivity implements OnClickListener {

	ImageView btnimg, btntxt;
	LinearLayout linear_rules, linear_rule;
	String[] selectPath;
	DisplayImageOptions option;
	Button next;
	ArrayList<String> data;
	String ruleText;
	ArrayList<String> paths;
	byte[] b;
	private Context mcontext;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setContentView(R.layout.creatematch_2);
		paths = new ArrayList<String>();
		b = getIntent().getByteArrayExtra("logo");
		data = getIntent().getStringArrayListExtra("data");

		ActionbarSettings actionbar = new ActionbarSettings(this,
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						CreateMatchStep2.this.finish();
					}
				}, null);
		// actionbar.setTopRightIcon(R.drawable.more);
		actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
		// actionbar.setTopRightIcon(R.drawable.ic_share);
		actionbar.setTitle(R.string.ab_createrace);
		btnimg = (ImageView) findViewById(R.id.btn_image);
		next = (Button) findViewById(R.id.next);
		next.setOnClickListener(this);
		btntxt = (ImageView) findViewById(R.id.btn_text);
		linear_rules = (LinearLayout) findViewById(R.id.liner_rules);
		linear_rule = (LinearLayout) findViewById(R.id.liner_rule);
		btnimg.setOnClickListener(this);
		btntxt.setOnClickListener(this);
		option = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.no_media)
				.showImageForEmptyUri(R.drawable.no_media)
				.showImageOnFail(R.drawable.no_media)
				.resetViewBeforeLoading(false) // default
				.delayBeforeLoading(0).cacheInMemory(false) // default
				.cacheOnDisc(false) // default
				.considerExifParams(false) // default
				.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) // default
				.displayer(new FadeInBitmapDisplayer(500)) // default
				.handler(new Handler()) // default
				.build();
		MApplication.getInstance().addActivity(this);
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		MApplication.getInstance().getList().add(1, this);
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		try {
			MApplication.getInstance().getList().remove(0);
			MApplication.getInstance().getList().remove(1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_image:
			Intent i = new Intent(this, ImageFolderActivity.class);
			i.putExtra("flag", "matchrules");
			startActivityForResult(i, 50);
			break;
		case R.id.btn_text:
			startActivityForResult(new Intent(this, WindowRuleActivity.class),
					30);
			break;
		case R.id.next:
			Bundle bundle = new Bundle();

			if (linear_rules.getChildCount() > 0) {
				// img
				bundle.putStringArrayList("imgs", paths);
			}
			if (linear_rule.getChildCount() > 0) {

				ruleText = ((TextView) linear_rule.getChildAt(0)).getText()
						.toString();
				bundle.putString("text", ruleText);
			}
			if (linear_rule.getChildCount() < 1
					&& linear_rules.getChildCount() < 1) {
				Toast.makeText(this, getString(R.string.defaultrule),
						Toast.LENGTH_SHORT).show();
			}

			Intent intents = new Intent(CreateMatchStep2.this,
					CreateMatchStep3.class);
			bundle.putStringArrayList("data", data);
			bundle.putByteArray("logo", b);
			intents.putExtras(bundle);
			startActivity(intents);
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		boolean hasText = false;
		if (requestCode == 30 && data != null) {
			TextView t = new TextView(this);
			for (int v = 0; v < linear_rule.getChildCount(); v++) {
				if (linear_rule.getChildAt(v) instanceof TextView) {
					t = (TextView) linear_rule.getChildAt(v);
					hasText = true;
					break;
				}
			}

			t.setText(data.getStringExtra("content"));
			if (t.getText().length() < 1) {
				hasText = false;
				return;
			}
			t.setTextColor(Color.WHITE);
			DisplayMetrics dm = this.getApplicationContext().getResources()
					.getDisplayMetrics();
			
			t.setTextSize((int)(8*(dm.density)));
//			t.setTextSize((int)dip2px(mcontext, 16));
			t.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent i = new Intent(CreateMatchStep2.this,
							WindowRuleActivity.class);
					i.putExtra("content", ((TextView) v).getText());
					startActivityForResult(i, 30);
				}
			});
			if (!hasText) {
				linear_rule.addView(t);
			}

		} else if (requestCode == 50 && data != null) {

			selectPath = data.getExtras().getStringArray("all_path");
			for (String s : selectPath) {
				if (paths.contains(s)) {
					continue;
				}
				if (linear_rules.getChildCount() > 4) {
					break;
				}
				final ImageView i = new ImageView(this);
				i.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
						300));
				i.setPadding(0, 10, 0, 10);
				paths.add(s);
				ImageLoader.getInstance()
						.displayImage("file://" + s, i, option);
				linear_rules.addView(i);
				i.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Context c = CreateMatchStep2.this;
						if (Build.VERSION.SDK_INT >= 11) {
							c = new ContextThemeWrapper(c,
									android.R.style.Theme_Holo_Light);
						}
						AlertDialog.Builder aDailog = new AlertDialog.Builder(c);
						aDailog.setCancelable(false);
						aDailog.setTitle(R.string.del_yes_or_no);
						aDailog.setMessage(R.string.del_pic);
						aDailog.setPositiveButton(R.string.ok,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										paths.remove(linear_rules
												.indexOfChild(i));
										linear_rules.removeView(i);

									}
								});

						aDailog.setNegativeButton(R.string.cancel,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										dialog.cancel();
									}
								});
						aDailog.show();

					}
				});
				linear_rules.invalidate();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub
		
	}
}

package com.bandlink.air.friend;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.bandlink.air.R;
import com.bandlink.air.util.LovefitFragmentActivity;

public class Recommend extends LovefitFragmentActivity implements OnClickListener {
	TextView rightText;
	ImageView actionleft;
	RadioButton recLeft, recRight;
	RadioGroup change;
	LinearLayout choose;
	private JSONArray city;
	Context context;
	private String cityList;
	private ArrayList<String> province = new ArrayList<String>();
	private ArrayList<String> cit; // 临时存放城市名称，在每次省份点击时刷新
	private ArrayList<String> dis;// 临时存放区名称，在每次城市点击时刷新

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recommad);
		if (android.os.Build.VERSION.SDK_INT >= 11) {
			context = new ContextThemeWrapper(this,
					android.R.style.Theme_Holo_Light);
		} else {
			context = this;
		}
		InitViews();
	}

	private void InitViews() {
		// TODO Auto-generated method stub
		actionleft = (ImageView) findViewById(R.id.actionleft);
		actionleft.setOnClickListener(this);
		rightText = (TextView) findViewById(R.id.rightText);
		rightText.setOnClickListener(this);
		recLeft = (RadioButton) findViewById(R.id.recleft);
		recRight = (RadioButton) findViewById(R.id.recright);
		rightText.setOnClickListener(this);
		choose = (LinearLayout) findViewById(R.id.choose);
		change = (RadioGroup) findViewById(R.id.change);
		change.setOnCheckedChangeListener(onCheckedChangeListener);
		if (recLeft.isChecked()) {
			Fragment frg = new recommendFragment();
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.choose, frg).commitAllowingStateLoss();
		} else if (recRight.isChecked()) {
			Fragment frg = new SearchFragment();
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.choose, frg).commitAllowingStateLoss();
		}
	}

	private OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener() {
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			// TODO Auto-generated method stub
			switch (checkedId) {
			case R.id.recleft:
				recLeft.setChecked(true);
				recLeft.setTextColor(getResources().getColor(
						R.color.home_header));
				recRight.setChecked(false);
				recRight.setTextColor(getResources().getColor(R.color.white));
				rightText.setVisibility(View.VISIBLE);
				Fragment frg = new recommendFragment();
				getSupportFragmentManager().beginTransaction()
						.replace(R.id.choose, frg).commitAllowingStateLoss();
				break;
			case R.id.recright:
				recRight.setChecked(true);
				recRight.setTextColor(getResources().getColor(
						R.color.home_header));
				recLeft.setTextColor(getResources().getColor(R.color.white));
				rightText.setVisibility(View.GONE);
				recLeft.setChecked(false);
				Fragment frg1 = new SearchFragment();
				getSupportFragmentManager().beginTransaction()
						.replace(R.id.choose, frg1).commitAllowingStateLoss();
				break;

			default:
				break;
			}

		}
	};

	public StringBuffer getFromAssets(String fileName) {
		try {
			InputStreamReader inputReader = new InputStreamReader(
					getResources().getAssets().open(fileName));
			BufferedReader bufReader = new BufferedReader(inputReader);
			String line = "";
			StringBuffer Result = new StringBuffer();
			while ((line = bufReader.readLine()) != null)
				Result.append(line);
			return Result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	private PopupWindow initPopupWindow() {
		final PopupWindow pop = new PopupWindow(this.getLayoutInflater()
				.inflate(R.layout.query, null), LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, true);
		final Button btn_male = (Button) pop.getContentView().findViewById(
				R.id.btn_male);
		final Button btn_female = (Button) pop.getContentView().findViewById(
				R.id.btn_female);
		Button btn_ok = (Button) pop.getContentView().findViewById(R.id.btn_ok);
		final Spinner spinnerPro = (Spinner) pop.getContentView().findViewById(
				R.id.spinnerPro);
		final Spinner spinnerCity = (Spinner) pop.getContentView()
				.findViewById(R.id.spinnerCity);
		final Spinner spinnerDis = (Spinner) pop.getContentView().findViewById(
				R.id.spinnerDis);
		btn_male.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// Fragment frg = new TargetSlimFragment();
				// Bundle bundle = new Bundle();
				// bundle.putString("dit",
				// getResources().getString(R.string.personal_sex_male));
				// frg.setArguments(bundle);
				// if (Recommend.this.isFinishing()) {
				// return;
				// }
				// getSupportFragmentManager().beginTransaction()
				// .replace(R.id.choose, frg)
				// .commitAllowingStateLoss();
			

				pop.dismiss();

			}
		});

		btn_female.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				pop.dismiss();

			}
		});
		cityList = getFromAssets("sell/script/city.min.js").toString();
		try {
			final JSONArray pro = new JSONObject(cityList)
					.getJSONArray("citylist");
			for (int i = 0; i < pro.length(); i++) {
				province.add(pro.getJSONObject(i).getString("p"));
			}
			ArrayAdapter<String> a = new ArrayAdapter<String>(context,
					R.layout.spinner_item, province);
			a.setDropDownViewResource(R.layout.spinner_list_city);
			spinnerPro.setAdapter(a);
			spinnerPro.setSelection(province.indexOf(1));

			spinnerPro.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view,
						int position, long id) {

					cit = new ArrayList<String>();
					city = new JSONArray();
					try {
						city = pro.getJSONObject(position).getJSONArray("c");
						for (int i = 0; i < city.length(); i++) {

							cit.add(city.getJSONObject(i).getString("n"));

						}

					} catch (JSONException e) {
						e.printStackTrace();
						cit.clear();
						cit.add(getString(R.string.without));

					}
					spinnerCity.setVisibility(View.VISIBLE);
					ArrayAdapter<String> c = new ArrayAdapter<String>(
							Recommend.this, R.layout.spinner_item, cit);
					c.setDropDownViewResource(R.layout.spinner_list_city);
					spinnerCity.setAdapter(c);

					spinnerCity.setSelection(0);
					spinnerCity
							.setOnItemSelectedListener(new OnItemSelectedListener() {

								@Override
								public void onItemSelected(
										AdapterView<?> parent, View view,
										int position, long id) {

									dis = new ArrayList<String>();
									JSONArray disarr;
									try {
										disarr = city.getJSONObject(position)
												.getJSONArray("a");
										for (int i = 0; i < disarr.length(); i++) {
											dis.add(disarr.getJSONObject(i)
													.getString("s"));
										}

									} catch (JSONException e) {

										e.printStackTrace();
										e.printStackTrace();
										dis.clear();
										dis.add(getString(R.string.without));
									}
									spinnerDis.setVisibility(View.VISIBLE);
									ArrayAdapter<String> d = new ArrayAdapter<String>(
											Recommend.this,
											R.layout.spinner_item, dis);
									d.setDropDownViewResource(R.layout.spinner_list_city);
									spinnerDis.setAdapter(d);
									try {
										spinnerDis.setSelection(dis.indexOf(0));

									} catch (Exception e) {
									}

								}

								@Override
								public void onNothingSelected(
										AdapterView<?> parent) {

								}
							});
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {

				}
			});
			pop.dismiss();
		} catch (JSONException e1) {

			e1.printStackTrace();
		}
		btn_ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (spinnerCity.getSelectedItem().toString().length() < 1
						|| spinnerCity.getSelectedItem().toString().equals("无")) {
					System.out.println("111"
							+ spinnerPro.getSelectedItem().toString()
							+ spinnerPro.getSelectedItem().toString()
							+ spinnerPro.getSelectedItem().toString());
				} else if (spinnerDis.getSelectedItem().toString().length() < 1
						|| spinnerDis.getSelectedItem().toString().equals("无")) {
					System.out.println("222"
							+ spinnerPro.getSelectedItem().toString()
							+ spinnerPro.getSelectedItem().toString()
							+ spinnerCity.getSelectedItem().toString());

				} else {
					System.out.println("333"
							+ spinnerPro.getSelectedItem().toString()
							+ spinnerCity.getSelectedItem().toString()
							+ spinnerDis.getSelectedItem().toString());
				}
				pop.dismiss();
			}
		});
		pop.setBackgroundDrawable(new BitmapDrawable());
		pop.setOutsideTouchable(true);
		pop.setTouchable(true);
		return pop;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.actionleft:
			Recommend.this.finish();
			break;
		case R.id.rightText:
			initPopupWindow().showAsDropDown(v);
			break;
		}
	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub
		
	}
}

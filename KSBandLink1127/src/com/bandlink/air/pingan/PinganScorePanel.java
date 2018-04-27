package com.bandlink.air.pingan;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bandlink.air.DeviceSettingBinding;
import com.bandlink.air.R;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Util;
import com.bandlink.air.view.CalendarView;
import com.bandlink.air.view.CalendarView.OnItemClickListener;

public class PinganScorePanel extends LovefitActivity {

	public CalendarView calendar;
	private Dbutils db;
	private ActionbarSettings actionbar;
	private SharedPreferences share;
	private Calendar curCalendar;
	private LinearLayout helpCheck, accountCheck, deviceCheck;
	private JSONObject deviceJSN;
	private String[] mo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setContentView(R.layout.pingan_scorepanel);
		mo = getResources().getStringArray(R.array.mounth);
		helpCheck = (LinearLayout) findViewById(R.id.left1);
		helpCheck.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				AlertDialog.Builder ab = new AlertDialog.Builder(Util
						.getThemeContext(PinganScorePanel.this));
				ImageView view = new ImageView(PinganScorePanel.this);
				view.setImageResource(R.drawable.ping_intro);
				view.setBackgroundColor(Color.parseColor("#867CE7"));
				ab.setView(view);
				ab.create().show();
			}
		});
		accountCheck = (LinearLayout) findViewById(R.id.center1);
		deviceCheck = (LinearLayout) findViewById(R.id.right1);
		deviceCheck.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				getRe();

			}
		});
		accountCheck.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				getPAScore(share.getString("vnumber", ""));
			}
		});
		curCalendar = Calendar.getInstance();
		share = getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		actionbar = new ActionbarSettings(this, new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				PinganScorePanel.this.finish();
			}
		}, new OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				AlertDialog.Builder ab = new AlertDialog.Builder(
						Util.getThemeContext(PinganScorePanel.this));
				final DatePicker dp = new DatePicker(
						Util.getThemeContext(PinganScorePanel.this));
				dp.setCalendarViewShown(false);

				dp.init(curCalendar.get(Calendar.YEAR),
						curCalendar.get(Calendar.MONTH),
						curCalendar.get(Calendar.DAY_OF_MONTH),
						new OnDateChangedListener() {

							@Override
							public void onDateChanged(DatePicker view,
									int year, int monthOfYear, int dayOfMonth) {
								// TODO Auto-generated method stub
								Calendar cal = Calendar.getInstance();
								cal.set(year, monthOfYear, 1);
								curCalendar = cal;
								calendar.changeDate(cal);
							}
						});
				hidePickerDay(dp);
				ab.setView(dp);
				ab.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								Calendar cal = Calendar.getInstance();
								cal.set(dp.getYear(), dp.getMonth(), 1);
								curCalendar = cal;
								calendar.changeDate(cal);
								actionbar.setRightText(mo[dp.getMonth()]);
							}
						});
				ab.create().show();

			}
		});
		actionbar.setTopRightIcon(R.drawable.down);
		actionbar.setTitle(R.string.ping_bonds);
		actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);

		actionbar.setRightText(mo[Integer.valueOf(Util
				.getTimeMMStringFormat("MM")) - 1]);
		calendar = (CalendarView) findViewById(R.id.panel);
		db = new Dbutils(share.getInt("UID", -1), this);
		// db.SaveSpBasicData(10000, 10000, 10000, 2, "2015-05-16",
		// "2015-05-16");
		calendar.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void OnItemClick(Date date) {
				// TODO Auto-generated method stub
				int s = db.getPinganScore(new SimpleDateFormat("yyyyMMdd")
						.format(date));
				Object[] o = db.getSpBasicData(new SimpleDateFormat(
						"yyyy-MM-dd").format(date));
				if (s == -1) {
					s = 0;
				}
				String st = "0";
				if (o != null && o.length > 0) {
					st = o[0].toString();
				}
				String time = new SimpleDateFormat("yyyy-MM-dd").format(date);
				// msg.setText(time + '\t' + "积分：" + s + '\t' + "步数：" + st);
				int step = Integer.valueOf(st);
				if (step >= 10000 && s != 25) {
					showDialog(step, time);
				}

			}
		});
		getPAScore(share.getString("vnumber", ""));
		super.onCreate(savedInstanceState);
	}

	public void getPAScore(final String number) {
		dialog = Util.initProgressDialog(this, true,
				getString(R.string.data_wait), null);
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					// String url
					// =PinganRegisterActivity.PINGAN_BASE_URL+"/open/appsvr/health/partner/bind/device/0330000?access_token="+preferences.getString("pingan_token",
					// "");
					// String ur2 = "&partyNo=010387774232"
					// +"&partnerCode=0330000&partnerMemberNo=PA_13917607092" ;
					// //String res = HttpUtlis.doHttpPut(url+ur2,null);
					//
					// //System.out.println(res);
					// HashMap<String, String> params = new HashMap<String,
					// String>();
					// params.put("access_token",
					// preferences.getString("pingan_token", ""));
					// params.put("partyNo", "010387774232");
					// params.put("partnerCode", "0330000");
					// params.put("partnerMemberNo", "PA_13917607092");
					// String res = HttpUtlis
					// .getRequestForPost(PinganRegisterActivity.
					// PINGAN_BASE_URL
					// +
					// "/open/appsvr/health/partner/bind/account/0330000?access_token="
					// + preferences.getString(
					// "pingan_token",
					// ""), params);
					// HashMap<String, String> params = new HashMap<String,
					// String>();
					// params.put("access_token",
					// share.getString("pingan_token", ""));
					// params.put("partyNo", "006692044329");
					// params.put("partnerCode", "0330000");
					// params.put("partnerMemberNo", "PA_TEST_001");
					// String res = HttpUtlis
					// .getRequestForPost(PinganRegisterActivity.
					// PINGAN_BASE_URL
					// +
					// "/open/appsvr/health/partner/bind/account/0330000?access_token="
					// + share.getString(
					// "pingan_token",
					// ""), params);
					//
					//
					//
					// System.out.println(res);
					String result = HttpUtlis
							.getRequestJson(
									PinganRegisterActivity.PINGAN_BASE_URL
											+ "/open/appsvr/health/member/points/partner/0330000/?partyNo="
											+ number
											+ "&access_token="
											+ share.getString("pingan_token",
													""), null);
					if (result != null
							&& result.contains("\"returnCode\":\"00\"")) {
						JSONObject obj;
						try {
							obj = new JSONObject(result);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							// e.printStackTrace();
							result = result.replace("\"data\":\"{\"",
									"\"data\":{\"");
							result = result.replace("\"}\"}", "\"}}");
							obj = new JSONObject(result);
						}
						JSONArray arr = obj.getJSONObject("data").getJSONArray(
								"pointDetails");
						// "pointDate": "20140911",
						// "individualPoints": "0",
						// "rejectionReason": "有效运动未达到10000步的要求"
						for (int i = 0; i < arr.length(); i++) {
							JSONObject o = arr.getJSONObject(i);
							db.savePinganScore(
									o.getString("individualPoints"),
									o.getString("pointDate"),
									o.has("rejectionReason") ? o
											.getString("rejectionReason") : "");
						}
						handler.obtainMessage(8).sendToTarget();
					} else if (result != null && result.contains("access")) {
						String[] token = PinganRegisterActivity
								.getAccessToken();
						share.edit().putString("pingan_token", token[0])
								.commit();
						getPAScore(number);
					} else {
						handler.obtainMessage(
								7,
								new JSONObject(result).getJSONObject("data")
										.getString("message")).sendToTarget();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					handler.obtainMessage(4).sendToTarget();
				}
			}
		}).start();
	}

	private void getRe() {
		dialog = Util.initProgressDialog(this, true,
				getString(R.string.data_wait), null);
		// TODO Auto-generated method stub
		new Thread(new Runnable() {
			public void run() {
				try {
					String str = getPaInfo(share.getString("session_id", ""));
					deviceJSN = new JSONObject(str);
					JSONObject jsobj = deviceJSN.getJSONObject("content");
					int type = 2;
					String did = jsobj.optString("ldevice", null);
					if (did == null) {
						Object[] obj = db.getUserDeivce();
						if (obj != null && obj.length > 3) {
							did = obj[3].toString();
						}
					}
					if (did.startsWith("720")) {
						type = 1;
					} else if (did.contains(":")) {
						type = 3;
					}
					String lovefitname = jsobj.getString("name");
					String vname = (jsobj.getString("vusername").equals("null") ? share
							.getString("vnumber", "") : jsobj
							.getString("vusername"));
					String token = share.getString("pingan_token", "");
					String checkres = DeviceSettingBinding.bindDevicePa(token,
							vname, type);
					String result = null;
					if (checkres != null && checkres.contains("帐号")) {
						// 未生效
						result = "帐号绑定关系：已解绑";
					}
					// String bindres =
					// DeviceSettingBinding.bindAccountPa(token, vname,
					// lovefitname);

					if (str != null) {

						handler.obtainMessage(3, result).sendToTarget();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	ProgressDialog dialog;

	void bindAcc(final JSONObject name) {
		dialog = Util.initProgressDialog(this, true,
				getString(R.string.data_wait), null);
		new Thread(new Runnable() {
			public void run() {
				// 调平安接口
				try {
					HashMap<String, String> params = new HashMap<String, String>();
					params.put("access_token",
							share.getString("pingan_token", ""));
					params.put("partyNo", share.getString("vnumber", ""));
					params.put("partnerCode", "0330000");
					params.put("partnerMemberNo", name.getString("name"));
					String res;

					res = HttpUtlis
							.getRequestForPost(
									PinganRegisterActivity.PINGAN_BASE_URL
											+ "/open/appsvr/health/partner/bind/account/0330000?access_token="
											+ share.getString("pingan_token",
													""), params);
					if (!res.contains("\"returnCode\":\"00\"")) {
						// 同步平安失败。
						System.out.println(res);
						handler.obtainMessage(1).sendToTarget();
					} else {
						String did = "";
						if (name.has("bindldevice")) {
							did = name.getString("bindldevice");
						}
						HashMap<String, String> map = new HashMap<String, String>();
						map.put("uid", share.getInt("UID", -1) + "");
						map.put("deviceid", did);
						map.put("vnumber", share.getString("vnumber", ""));
						if (did.startsWith("7") && !did.contains(":")) {
							map.put("pwd", Computer(did));
						}
						String result = HttpUtlis.getRequest(HttpUtlis.BASE_URL
								+ "/user/setDefaultPaBind", map);
						if (result != null && result.equals("1")) {
							handler.obtainMessage(2).sendToTarget();
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}).start();
	}

	public static String Computer(String number) {
		// 7288c 1505001001
		String code = "";
		int num1;
		int num2;
		int num3;
		int num4;
		int num5;
		int num6;
		int[] num = new int[10];
		int sum = 0;
		number = number.substring(5, number.length());
		if (number.length() != 10) {
			return "error";
		}
		for (int i = 0; i < 10; i++) {
			num[i] = (int) number.charAt(i) - 48;
			sum = sum + num[i];
		}
		num1 = sum % 10;
		num2 = (sum / 10) % 10;
		if (num[9] == 0) {
			num3 = (sum % (num[9] + 12)) % 10;
			num4 = (sum / (num[9] + 12)) % 10;
		} else {
			num3 = (sum % num[9]) % 10;
			num4 = (sum / num[9]) % 10;
		}
		int temp = 0;
		if ((num[9] - num[8] - num[7]) > 0)
			temp = num[9] - num[8] - num[7];
		else
			temp = 5;
		num5 = sum % temp;
		num6 = (sum / temp) % 10;
		code = num1 + "" + num2 + "" + num3 + "" + num4 + "" + num5 + "" + num6;

		return code;
	}

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (dialog != null && dialog.isShowing()) {
				dialog.dismiss();
			}
			switch (msg.what) {
			case 0:
				Toast.makeText(PinganScorePanel.this, "完成！但不会立即生效，请后查看",
						Toast.LENGTH_SHORT).show();
				break;
			case 1:
				Toast.makeText(PinganScorePanel.this, "完成！但不会立即生效，请稍后查看",
						Toast.LENGTH_SHORT).show();
				break;
			case 2:
				Toast.makeText(PinganScorePanel.this, "完成！但不会立即生效，请稍后查看",
						Toast.LENGTH_SHORT).show();
				new Thread(new Runnable() {
					public void run() {
						try {
							String str = getPaInfo(share.getString(
									"session_id", ""));
							if (str != null) {
								deviceJSN = new JSONObject(str);
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}).start();
				break;
			case 3:
				showbinRe(msg.obj);
				break;
			case 4:
				Toast.makeText(PinganScorePanel.this, "出错啦，请稍候重试",
						Toast.LENGTH_SHORT).show();
				break;
			case 7:
				Toast.makeText(PinganScorePanel.this, msg.obj.toString(),
						Toast.LENGTH_SHORT).show();
				break;
			case 8:
				calendar.postInvalidate();
				Toast.makeText(PinganScorePanel.this, "积分已刷新",
						Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	void showbinRe(Object obj) {
		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
		}
		try {
			if (deviceJSN == null) {

				return;
			}
			final JSONObject str = deviceJSN.getJSONObject("content");
			AlertDialog.Builder ab = new AlertDialog.Builder(
					Util.getThemeContext(PinganScorePanel.this));
			String strs = "帐号绑定关系：已绑定";
			ab.setTitle("帐号绑定校验");
			if (obj != null) {
				strs = obj.toString();
				ab.setPositiveButton("重新绑定",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub

								try {
									String lovefitname = str.getString("name");
									String res = DeviceSettingBinding
											.bindAccountPa(share.getString(
													"pingan_token", ""), share
													.getString("vnumber", ""),
													lovefitname);
									if (!res.contains("\"returnCode\":\"00\"")) {
										// 同步平安失败。
										System.out.println(res);
										handler.obtainMessage(1).sendToTarget();
									} else {

									}
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						});
			}

			// String strs = "平安健行天下帐号："
			// + (str.getString("vusername").equals("null") ? share
			// .getString("vnumber", "") : str
			// .getString("vusername")) + '\n';
			// if (str.has("ldevice") &&
			// !str.getString("ldevice").equals("null")) {
			// // ok
			// strs += /* "关联积分设备：" + str.getString("ldevice") */"           &"
			// + '\n';
			// strs += "Lovefit帐号：" + str.getString("name") + '\n' + '\n';
			// strs += "绑定生效";
			// } else {
			// strs += /* "关联积分设备：" + str.getString("ldevice") */"           &"
			// + '\n';
			// strs += "Lovefit帐号：" + str.getString("name") + '\n' + '\n';
			// strs += "绑定失效";
			// ab.setPositiveButton("重新绑定",
			// new DialogInterface.OnClickListener() {
			//
			// @Override
			// public void onClick(DialogInterface dialog,
			// int which) {
			// // TODO Auto-generated method stub
			//
			// bindAcc(str);
			// }
			// });
			// }
			ab.setNegativeButton(R.string.next,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							showDeviceCheck();
						}
					});
			ab.setMessage(strs);

			ab.create().show();

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void showDeviceCheck() {
		if (deviceJSN != null) {

			JSONObject jsobj = deviceJSN.optJSONObject("content");
			if (jsobj != null) {
				String msg = null;
				String ldevice = jsobj.optString("ldevice", null);
				if (ldevice != null) {
					ldevice = ldevice.trim().toUpperCase();
				}
				String eqdevice = jsobj.optString("bindldevice", null);
				if (eqdevice != null) {
					if (eqdevice.equals("null")) {
						eqdevice = "";
					}
					eqdevice = eqdevice.trim().toUpperCase();
				}
				// 平安表有设备
				if (ldevice != null && ldevice.length() > 0) {
					if (!ldevice.contains(":") && ldevice.equals(eqdevice)) {
						// 正常
						msg = "设备绑定正常";
					} else if (ldevice.contains(":")) {
						// 绑定的是air
						msg = "蓝牙设备可正常使用(Flame/Ant需要重新绑定)";
					} else {
						// 有误
						msg = "设备绑定有误，请至【我的设备】重新绑定";
					}
				} else {
					// eqdevice 有但是 pingan没有
					if (eqdevice != null && eqdevice.length() > 0) {
						if (!eqdevice.contains(":")) {
							// 有误
							msg = "设备绑定有误，请至【我的设备】重新绑定";
						} else {
							// air
							msg = "蓝牙设备可正常使用(Flame/Ant需要重新绑定)";
						}
					} else {
						// air
						msg = "蓝牙设备可正常使用(Flame/Ant需要重新绑定)";
					}
				}
				if (msg != null) {
					AlertDialog.Builder ab = new AlertDialog.Builder(
							Util.getThemeContext(PinganScorePanel.this));
					ab.setTitle("设备绑定校验");
					ab.setMessage(msg);
					ab.setPositiveButton(R.string.close, null);
					ab.create().show();
				}
			}
		} else {
			Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT)
					.show();
		}
	}

	public static String getPaInfo(String session) throws Exception {
		String res = HttpUtlis.getRequest(HttpUtlis.BASE_URL
				+ "/user/getPaBindInfo/session/" + session, null);
		return res;
	}

	public void showDialog(final int step, final String time) {
		AlertDialog.Builder ab = new AlertDialog.Builder(
				Util.getThemeContext(this));
		ab.setTitle("同步积分");
		ab.setMessage("本地步数达标，但积分同步失败，请重新提交");
		ab.setPositiveButton(R.string.submit,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog = Util.initProgressDialog(
								Util.getThemeContext(PinganScorePanel.this),
								true, getString(R.string.data_wait), null);
						uploadSteps(step, time);
					}
				});
		ab.setNegativeButton(R.string.cancel, null);
		ab.create().show();

	}

	void uploadSteps(final int step, final String date) {

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {

					String url = String
							.format("%s/data/upLoadSpData/session/%s/step/%s/calorie/%s/distance/%s/time/%s/type/2",
									HttpUtlis.BASE_URL, share.getString(
											"session_id", "lovefit"),
									step + "", (step * 30) + "", String.format(
											"%.1f", ((float) step * 0.6f)),
									date);

					String str = HttpUtlis.getRequest(url, null);
					if (str.equals("1")) {
						handler.sendEmptyMessage(0);
					} else {
						handler.sendEmptyMessage(4);
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					handler.sendEmptyMessage(4);
				}
			}

		}).start();
	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub

	}

	/***
	 * 请求用户绑定的设备信息
	 */
	private void getDevice() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Map<String, String> map = new HashMap<String, String>();
				map.put("session", share.getString("session_id", ""));

				try {
					String result = HttpUtlis.getRequest(HttpUtlis.BASE_URL
							+ "/user/getUserDevice", map);
					JSONObject js = new JSONObject(result);
					if (js.getString("message").equals("ok")) {
						JSONObject devJson = js.getJSONObject("content");

						try {
							db.InitDeivce(Integer.parseInt(devJson
									.getString("devicetype")), db
									.getDeviceWgType(), devJson
									.getString("deviceid"), "");
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {

					}
				} catch (Exception e) {
					// TODO Auto-generated cat
					e.printStackTrace();

				}
			}
		}).start();
	}

	/**
	 * 隐藏或者显示相应的时间项
	 * 
	 * @param picker
	 *            传入一个DatePicker对象
	 */
	public static void hidePickerDay(DatePicker picker) {
		// 利用java反射技术得到picker内部的属性，并对其进行操作
		Class<? extends DatePicker> c = picker.getClass();
		try {
			// 为了简单，缩写了... fd是field_day,fm是field_month。
			Field fd = null, fm = null;
			// 在这里做判断，入过系统版本大于4.0，就让他执行这一块,不做判断在有的手机可能没法实现
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				fd = c.getDeclaredField("mDaySpinner");
				fm = c.getDeclaredField("mMonthSpinner");
			} else {
				fd = c.getDeclaredField("mDayPicker");
				fm = c.getDeclaredField("mMonthPicker");
			}
			// 对字段获取设置权限
			fd.setAccessible(true);
			fm.setAccessible(true);
			// 得到对应的控件
			View vd = (View) fd.get(picker);
			View vm = (View) fm.get(picker);
			// View vy = (View) fy.get(picker);
			// Type是3表示显示年月，隐藏日,vd可以从下面提取出来的，懒的 ...o(╯□╰)o

			vd.setVisibility(View.GONE);

		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

	}
}

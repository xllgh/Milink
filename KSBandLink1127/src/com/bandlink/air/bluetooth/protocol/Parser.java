package com.bandlink.air.bluetooth.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Cap;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.text.format.Time;
import android.util.Log;
import android.widget.RemoteViews;

import com.bandlink.air.AirPreferenceActivity;
import com.bandlink.air.MilinkApplication;
import com.bandlink.air.MyLog;
import com.bandlink.air.R;
import com.bandlink.air.SlideMainActivity;
import com.bandlink.air.ble.BluetoothLeService;
import com.bandlink.air.ble.Converter;
import com.bandlink.air.ble.LogUtil;
import com.bandlink.air.simple.SimpleHomeFragment;
import com.bandlink.air.simple.SyncQQHealth;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.FileDownUtils;
import com.bandlink.air.util.FileDownUtils.OnHexDownloadListener;
import com.bandlink.air.util.HttpRequest;
import com.bandlink.air.util.HttpUtlis;
import com.bandlink.air.util.MyDate;
import com.bandlink.air.util.NotificationUtils;
import com.bandlink.air.util.SessionUpdate;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.UploadData;
import com.bandlink.air.util.Util;
import com.bandlink.air.view.SleepChart;

public class Parser {
	private static String TAG = "Parser";

	public static final String ACTION_STEP_CHANGED = "LovefitAir.ON_STEP";

	public boolean _isFindLen;
	private byte[] packet;
	private byte[] buffer;
	private int packet_Len;
	public int buf_Len;
	private InputStream instream;
	private OutputStream outstream;
	private ComposePacket composePacket;

	private Context ContextRef = null;

	private Dbutils dbutils;

	private Time cur_time;
	private int hour;
	private int minute;
	private int second;
	private long tick;
	// 0->蓝牙 1-> 3g 2->手机 3->ble 4->ant 5->air
	private int devicetype;
	private SharedPreferences sharePre;

	// 闹钟
	int alarm1_enable = 0;
	int alarm1_h = 0;
	int alarm1_m = 0;
	int alarm2_enable = 0;
	int alarm2_h = 0;
	int alarm2_m = 0;

	// 配置状态
	// 0:read data 1:alarm 2:person info 3: lost
	int cur_state;
	private SharedPreferences preferences;
	private static Typeface tf;

	public Parser(OutputStream s, Context c) {

		_isFindLen = false;
		packet = new byte[1024];
		buffer = new byte[1024];
		packet_Len = 0;
		buf_Len = 0;
		// instream = s.getInputStream();
		// outstream = s.getOutputStream();
		outstream = s;
		ContextRef = c;
		preferences = c.getApplicationContext().getSharedPreferences(SharePreUtils.APP_ACTION, Context.MODE_PRIVATE);
		dbutils = new Dbutils(preferences.getInt("UID", -1), ContextRef);
		composePacket = new ComposePacket(c);
		cur_time = new Time();
		tf = Typeface.createFromAsset(ContextRef.getAssets(), "font/AvenirLTStd-Light.otf");
		mHandler = new Handler(c.getMainLooper()) {
			public void handleMessage(android.os.Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case 0:
					inDatatrans = false;
					break;
				}
			};
		};
		// onPacket0806(Converter.hexStringToBytes("A7B800010000019508060107DF0C08FFFFF66FFF33FFFF55FF44FF2244FFFFF66FFFFFFFFFFFFF07DF0C09FFFFFFFFFFFFFFFFFFFFFFFF4545FF55FFFFFFFFFF45FF2307DF0C0AFFFFFFFFFFFFFFFFFFFF44FF4545FFFFFFFFFFFFFFF34FFF07DF0C0BFFFFFFFFFFFFFFFFFFFFFFFF4545FFFFFFFFFFFFFFFFFF3407DF0C0CFFFFFFFFFFFFFFFFFFFFFFFF4545FFFFFFFFFFFFFFFFFF4507DF0C0DFFFFFFFFFFFFFFFFFFFFFFFF4545FFFFFFFFFFFFFFFFFF4807DF0C0EFFFFFFFFFFFFFFFFFFFFFFFF4545FFFFFFFFFFFFFFFFFF4407DF0C0FFFFFFFFFFFFFFFFFFFFFFFFF4545FFFFFFFFFFFFFFFFFF2207DF0C10FFFFFFFFFFFFFFFFFFFFFFFF4545FFFFFFFFFFFFFFFFFF3207DF0C11FFFF554240444745564448414548FF44FF3344434344434407DF0C12FFFFFFFFFFFFFFFFFFFFFFFF4545FFFFFFFFFFFFFFFFFF5507DF0C13FFFFFFFFFFFFFFFFFFFFFFFF4545FFFFFFFFFFFFFFFFFF3407DF0C14FFFFFFFFFFFFFFFFFFFFFFFF4545FFFFFFFFFFFFFFFFFF4307DF0C15FFFFFFFFFFFFFFFFFFFFFFFF4545FFFFFFFFFFFFFFFFFF230532"));
	}

	/**
	 * 是否在传输数据
	 */
	boolean inDatatrans = false;
	Handler mHandler;

	public void Start(int type) {
		devicetype = type;
		if (type == 5) {
			if (BluetoothLeService.bLogfile)
				LogUtil.e("--开始发时间同步--" + "---" + Util.getTimeMMString());
			// Send(composePacket.ComposeCall2("测试"));
			// Send(new
			// byte[]{(byte)0xA7,(byte)0xB8,(byte)0x00,(byte)0x01,00,00,00,(byte)0x0C,(byte)0x92,00,(byte)0xFC,(byte)0xFD});
			if (!inDatatrans) {
				synchronized (MilinkApplication.getInstance()) {
					inDatatrans = true;
					mHandler.sendEmptyMessageDelayed(0, 8 * 1000);
					dbutils = new Dbutils(preferences.getInt("UID", -1), ContextRef);
					int step = 0, dis = 0, cal = 0;
					try {
						Object[] st = dbutils.getSpBasicData(MyDate.getFileName());
						if (st != null && st.length > 0) {
							step = Integer.valueOf(st[0].toString());
							dis = Integer.valueOf(st[1].toString());
							cal = Integer.valueOf(st[2].toString());
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						step = 0;
					}
					Send(composePacket.ComposePacket04(step, dis, cal));
				}
			} else {

			}

		} else if (type == 6) {
			// Send(composePacket.ComposeCall2(“测试"));
			// Send(new
			// byte[]{(byte)0xA7,(byte)0xB8,(byte)0x00,(byte)0x01,00,00,00,(byte)0x0C,(byte)0x92,00,(byte)0xFC,(byte)0xFD});
			// above is crc wrong, not work since 2.2.6
			Send(new byte[] { (byte) 0xA7, (byte) 0xB8, (byte) 0x00, (byte) 0x01, 00, 00, 00, (byte) 0x0C, (byte) 0x92,
					00, (byte) 0x9D, (byte) 0x69 });
		} else {
			Send(composePacket.ComposeConnect((byte) 0x0));
		}
	}

	/*
	 * public void getAByte(int value) { buffer[buf_Len] = (byte) value; buf_Len
	 * += 1; Check(); }
	 */
	public void getArray(byte[] value, int len) {

		if (buffer != null) {
			synchronized (buffer) {
				try {
					// buf_Len 已有
					if (buf_Len >= buffer.length) {
						buf_Len = 0;
					} else {
						System.arraycopy(value, 0, buffer, buf_Len, len);
						if (BluetoothLeService.bLogfile)
							LogUtil.e("<--：" + Converter.byteArrayToHexString(value));
						buf_Len += len;
						Check();
					}
				} catch (Exception e) {
					// TODO: handle exception
					buf_Len = 0;
					e.printStackTrace();

				}
			}
		}
	}

	public byte[] SendCallIncome(String name) {
		byte[] byt = composePacket.ComposeCall2(name);
		Send(byt);
		return byt;
	}

	public byte[] ComposeCallWithNumber(String num) {
		byte[] byt = composePacket.ComposeCallWithNumber(num);
		Send(byt);
		return byt;
	}

	public void SendAirHz(int value) {
		Send(composePacket.ComposePacket0805(value));
	}

	public void SendMsgIncome(int value) {
		Send(composePacket.ComposeMSG(value));
	}

	public void SendCallEnd() {
		Send(composePacket.ComposeCallEnd());
	}

	public void SendOnDeviceOut() {
		Send(composePacket.ComposeDeviceOut());
	}

	public void SendOnDeviceIn() {
		Send(composePacket.ComposeDeviceIn());
	}

	private void Send(byte[] data, int len) throws IOException {
		outstream.write(data, 0, len);
	}

	private void Send(byte[] data) {
		try {
			outstream.write(data, 0, data.length);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void Check() {
		// MyLog.i("rrrr",
		// "check: " + Converter.byteArrayToHexString(buffer, 0, buf_Len));
		if (!_isFindLen) {
			if (buf_Len >= 8) {
				// packet_Len此包长度
				packet_Len = ((buffer[6] & 0xff) << 8) + (buffer[7] & 0xff);
				_isFindLen = true;
			}
		}

		if (_isFindLen && buf_Len >= packet_Len) {
			ParserData();
		}
	}

	private void ParserData() {
		buf_Len = 0;
		_isFindLen = false;

		System.arraycopy(buffer, 0, packet, 0, packet_Len);
		// 报文id
		// MyLog.i("rrrr", "onPacket: " +
		// Converter.byteArrayToHexString(packet));
		// LogUtil.i(Calendar.getInstance().getTime().toLocaleString()
		// + "onPacket: " + Converter.byteArrayToHexString(packet,8,2));
		switch (packet[8] & 0xff) {
		case 0x01:
			// 连接报文
			onPacket01();
			break;
		case 0x02:
			onPacket02();
			break;
		case 0x04:
			// 0x04 时间同步
			onPacket04();
			break;
		case 0x08:
			// 0x08 数据同步报文
			onPacket08();
			break;
		case 0x90:
			// 手机端向设备端发出的提醒
			onPacket90();
			break;
		case 0x9e:
			// 配置定义
			onPacket9E();
			break;
		case 0x92:
			// 关机？
			onPacket92();
		default:
			break;
		}
		// (packet, 0, packet.length);
	}

	private void onPacket01() {
		// 01 正常 0D 错误
		if (packet[9] == 0x00 || packet[9] == 0x01 || packet[9] == 0x0d) {
			Send(composePacket.ComposeDeviceInfo());
			// requestData(0);
		} else if (packet[9] == 0x03) {
			if (BluetoothLeService.bLogfile)
				MyLog.e("BlutTooth", "Disconnect");
		}
	}

	private void onPacket02() {
		dbutils = new Dbutils(preferences.getInt("UID", -1), ContextRef);
		int step = 0, dis = 0, cal = 0;
		try {
			Object[] st = dbutils.getSpBasicData(MyDate.getFileName());
			if (st != null && st.length > 0) {
				step = Integer.valueOf(st[0].toString());
				dis = Integer.valueOf(st[0].toString());
				cal = Integer.valueOf(st[0].toString());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			step = 0;
		}
		Send(composePacket.ComposePacket04(step, dis, cal));
	}

	boolean writeDelay = false;// 是否本次写了智能开关及延时

	private void onPacket04() {
		if (BluetoothLeService.bLogfile)
			MyLog.d(TAG, "时间同步");

		// 睡眠数据，分析
		// 请求睡眠数据
		// Send(composePacket.ComposeSleepData((byte) 4, (byte) 1));

		requestData();
	}

	/***
	*/
	public void SendLostDelay(int delay) {
		// 智能开关 一直打开
		Send(composePacket.ComposeLostDelay((byte) delay));
	}

	public void SendLostDelay(boolean antilost, int delay, int languge, int handup, int height) {
		// 智能开关 一直打开
		Send(composePacket.ComposeAirSet(antilost, (byte) delay, languge, handup, height));
	}

	/***
	 * 11 -27 在请求同步数据前需要发送连接包
	 * 
	 * @param x
	 */
	public void requestData() {

		Send(composePacket.ComposeData((byte) 0));

		//
	}

	private void onPacket08() {
		// if(BluetoothLeService.isdebug)
		if (BluetoothLeService.bLogfile)
			MyLog.e("Parser", "数据同步报文ID:" + packet[9]);
		if (!CheckCRC(packet)) {
			LogUtil.e("CRC__ERROR__数据同步报文ID:" + packet[9]);
			buf_Len = 0;
			return;
		}
		switch (packet[9]) {
		case 0x01:
			// 历史
			onPacket0801();
			break;
		case 0x02:
			// 详细
			onPacket0802();
			break;
		case 0x03:
			// 当前:
			onPacket0803();
			break;
		case 0x04:
			// 睡眠:
			onPacket0804();
			break;
		case 0x06:
			// 心率:
			onPacket0806(packet);
			break;
		case 0x00:
			// 断开
			onPacket0800();
			// Toast.makeText(ContextRef, "on packet good!",
			// Toast.LENGTH_SHORT).show();
			break;
		case 0x05:
			onPacket0805(packet);
			break;
		}

	}

	public static final String ACTION_GET_HEARTRATE = "com.milink.hr";

	private void onPacket0806(byte[] aa) {
		if (!CheckCRC(packet)) {
			return;
		}

		// A7B800010000000E080602482E58
		switch ((aa[10] & 0xff)) {
		case 0x02:
			if ((aa[11] & 0xff) > 0) {
				ContextRef.sendBroadcast(new Intent(ACTION_GET_HEARTRATE).putExtra("hr", (aa[11] & 0xff)));
			}

			break;
		case 0x01:
			// A7B800010000002908060107DF0C0A
			// FFFFFFFFFFFFFFFFFFFFFFFF0000FFFFFFFFFFFFFFFFFFFF0532
			int len = ((aa[6] & 0xff) << 8) + (aa[7] & 0xff) - 13;

			if (len > 24) {
				byte[] rawdata = new byte[len];
				System.arraycopy(aa, 11, rawdata, 0, len);
				for (int i = 0; i < len; i += 28) {
					String date = String.format("%1$04d-%2$02d-%3$02d",
							((aa[11 + i] & 0xff) << 8) + (aa[12 + i] & 0xff), (aa[13 + i] & 0xff), (aa[14 + i] & 0xff));
					byte[] tt = new byte[24];
					System.arraycopy(aa, 15 + i, tt, 0, 24);
					Log.e("xxxxxx", Converter.byteArrayToHexString(tt));
					dbutils.saveHr(date, tt);
				}
			}
			ContextRef.sendBroadcast(new Intent(ACTION_GET_HEARTRATE).putExtra("type", 1));
			break;
		}

	}

	public void readHr() {
		Send(composePacket.ComposePacket0806());
	}

	public static final String ACTION_STEP0805 = "com.milink.android.step0805";

	public void onPacket0805(byte[] step) {
		if (!CheckCRC(step)) {
			return;
		}
		long step_all = ((step[10] & 0xff) << 24) + ((step[11] & 0xff) << 16) + ((step[12] & 0xff) << 8)
				+ (step[13] & 0xff);
		long cal = ((step[14] & 0xff) << 24) + ((step[15] & 0xff) << 16) + ((step[16] & 0xff) << 8) + (step[17] & 0xff);
		// long distance = ((step[18] & 0xff) << 24) + ((step[19] & 0xff) << 16)
		// + ((step[20] & 0xff) << 8) + (step[21] & 0xff);
		if (BluetoothLeService.bLogfile)
			LogUtil.e("---------------------->on0805");
		dbutils.SaveSpBasicData((int) step_all, (int) (step_all * 0.6f), (int) cal * 100, devicetype,
				MyDate.getFileName(), MyDate.getFileName());
		updatenotification(ContextRef, (float) step_all / tar, (int) step_all, (int) cal * 100,
				Math.round(step_all * 0.6f), true);
		Intent in = new Intent(ACTION_STEP0805);
		in.putExtra("step", step_all);
		in.putExtra("cal", cal * 100);
		in.putExtra("dis", step_all * 6);
		ContextRef.sendBroadcast(in);
	}

	public void onPacket0802Fake(Date dd, int hour, int[] data) {
		// 详细
		// parserStep1(packet);
		byte[] packet1 = new byte[114];

		String date = (new SimpleDateFormat("yyyy-MM-dd")).format(dd);
		/*
		 * for(int i=0;i<5;i++) { packet1[i] = 0; }
		 */
		int year = dd.getYear() + 1900;
		packet1[0] = (byte) ((year >> 8) & 0xff);
		packet1[1] = (byte) (year & 0xff);
		packet1[2] = 0;
		packet1[3] = (byte) (dd.getMonth() + 1);
		packet1[4] = (byte) dd.getDate();
		packet1[5] = (byte) hour;
		for (int i = 0; i < 12; i++) {
			packet1[6 + i * 2] = (byte) (data[i] & 0xff);
			packet1[6 + i * 2 + 1] = (byte) (data[i] / 256);
		}
		Dbutils db = new Dbutils(preferences.getInt("UID", -1), ContextRef);
		db.SaveSpRawData(date, packet1, 2);

	}

	public void onPacket0803Fake(Date dd, int step, int cal, int distance) {
		// parserStep1(packet1);
		// requestData();

		int index = 10;
		// 解析计步器数据
		String time = (new SimpleDateFormat("yyyy-MM-dd")).format(dd);

		// 电量
		int battery = 99;
		// 体重
		int weight = 66;
		// 步幅
		int stride = 75;
		// 消耗卡路里100卡
		// long cal = 9988;
		cal = cal * 100;
		long step_all = step;
		long distance1 = step * stride / 100;

		cur_time.setToNow();
		hour = cur_time.hour;
		minute = cur_time.minute;
		second = cur_time.second;
		tick = (hour * 3600) + (minute * 60) + second;
		tick = tick / 2;
		// SavePD_DAY(time, step_all, cal, distance1);
		// SavePD_DAY_soft(time, step_all, cal, distance1, tick);

		// SavePD_DAY(time, step_all, cal, distance1);

		NotifyPD_BT();

		// Message msg = new Message();
		// msg.what = PedBriefActivity.BT_DATA;
		// Bundle bundle = new Bundle();
		// bundle.putString("data", strbuf.toString()); // bundle中存放数据
		// msg.setData(bundle);// msg利用Bundle传输数据
		// PedBriefActivity.bphandler.sendMessage(msg);//
		// 用activity中的handler发送消息

	}

	public void onPacket0803Fake1(Date dd, int step, int cal, int distance) {
		// parserStep1(packet1);
		// requestData();

		int index = 10;
		// 解析计步器数据
		String time = (new SimpleDateFormat("yyyy-MM-dd")).format(dd);

		// 电量
		int battery = 99;
		// 体重
		int weight = 66;
		// 步幅
		int stride = 75;
		// 消耗卡路里100卡
		// long cal = 9988;
		cal = cal * 100;
		long step_all = step;
		long distance1 = step * stride / 100;

		cur_time.setToNow();
		hour = cur_time.hour;
		minute = cur_time.minute;
		second = cur_time.second;
		tick = (hour * 3600) + (minute * 60) + second;
		tick = tick / 2;
		// SavePD_DAY(time, step_all, cal, distance1);
		// SavePD_DAY1_soft(time, step_all, cal, distance1, tick);

		// SavePD_DAY1(time, step_all, cal, distance1);
		// MyLog.v("pedLogin", "ffffff");
		NotifyPD_BT();

		// Message msg = new Message();
		// msg.what = PedBriefActivity.BT_DATA;
		// Bundle bundle = new Bundle();
		// bundle.putString("data", strbuf.toString()); // �?undle中存放数�?
		// msg.setData(bundle);// mes利用Bundle传�?数据
		// PedBriefActivity.bphandler.sendMessage(msg);//
		// 用activity中的handler发�?消息

	}

	public void readAlarm() {
		Send(composePacket.ComposeAlarmConfig());
	}

	private void onPacket0800() {
		// MyLog.d(TAG, "数据接收完成");
		// Intent intent = new Intent(SimpleHomeFragment.ACTION_START);
		// ContextRef.sendBroadcast(intent);

		SessionUpdate update = SessionUpdate.getInstance(ContextRef);
		update.checkSession();
		if (BluetoothLeService.bLogfile)
			MyLog.e("service_process----update", SessionUpdate.isUpload + "");
		if (SessionUpdate.isUpload) {
			// 上传数据
			UploadData upload = new UploadData(5, ContextRef, batteryNow);
			upload.start();
		}
		Calendar mCalendar = Calendar.getInstance();
		// 晚上9点以后不读了
		if (mCalendar.get(Calendar.HOUR_OF_DAY) <= 21) {
			// 分数大于45不读了

			try {
				if (Integer.valueOf(dbutils.getSleepDetailScore(MyDate.getYesterDay())) <= 20) {
					Send(composePacket.ComposeSleepData((byte) 4, (byte) 1));
				} else {
					after0804();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Send(composePacket.ComposeSleepData((byte) 4, (byte) 1));
				e.printStackTrace();
			}

		} else {
			after0804();
		}

		// //设备信息(没相应?暂时去掉) --》时间同步--》睡眠
		// Send(composePacket.ComposePacket04());
		// Send(composePacket.ComposeDeviceInfo());
		// 时间同步
		// Send(composePacket.ComposeLEConnect());

		// 读取设备信息

		// 读取闹钟
		cur_state = 1;
		// Send(composePacket.ComposeAlarmConfig());
		// mshen, no longer read here
		// Send(composePacket.ComposeLEConfig2());

		// try {
		// Send(composePacket.ComposeConnect((byte) 0x03));
		// MyLog.d(TAG, "发送来电");
		// Send(composePacket.ComposeCall2("阮明浩"));
		// Send(composePacket.ComposeMSG());
		// 上传数据
		// UploadData upload = new UploadData();
		// upload.start();
		// } catch (IOException e) {
		// TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

	private void onPacket0801() {
		parserStepHistory(packet);
		requestData();
	}

	private void onPacket0802() {
		if (!CheckCRC(packet)) {
			LogUtil.e("crc error 0802");
			return;
		}
		// 详细
		// parserStep1(packet);
		String date = String.format("%1$04d-%2$02d-%3$02d", ((packet[10] & 0xff) << 8) + (packet[11] & 0xff),
				(packet[13] & 0xff), (packet[14] & 0xff));
		// SavePD_BT(date, packet);
		// String date, String data, int type
		int len = ((packet[6] & 0xff) << 8) + (packet[7] & 0xff);
		len = len - 12;
		byte[] rawdata = new byte[len];
		System.arraycopy(packet, 10, rawdata, 0, len);
		dbutils.SaveSpRawData(date, rawdata, devicetype);
		// MyLog.d("StepResult", "历史数据");
		requestData();
	}

	private void onPacket0803() {
		long steps = parserStep1(packet);

		requestData();
		// NotifyPD_BT();
	}

	byte[] sleepData = new byte[360];

	private void onPacket0804() {
		switch (packet[10]) {
		case 0x01:
			System.arraycopy(packet, 11, sleepData, 0, 120);
			Send(composePacket.ComposeSleepData((byte) 4, (byte) 2));
			break;
		case 0x02:
			System.arraycopy(packet, 11, sleepData, 120, 120);
			Send(composePacket.ComposeSleepData((byte) 4, (byte) 3));
			break;
		case 0x03:
			System.arraycopy(packet, 11, sleepData, 240, 120);
			byte[] data = SleepChart.transData(sleepData);
			int[] te = SleepChart.checkSleep(data, true);
			String yesterday = MyDate.getYesterDay();
			if (te != null) {

				if (te[0] * te[1] != 0 && te[1] > te[0]) {
					ContentValues result = SleepChart.analysisSleep(te, data);

					int bbo = getBounds(te, result.getAsInteger("awake"), result.getAsInteger("activeCount"),
							result.getAsInteger("deep_time"), result.getAsInteger("light_time"));
					dbutils.saveSleepDetail(yesterday, result.getAsString("begin"), result.getAsString("end"),
							result.getAsString("activeCount"), result.getAsString("light_time"),
							result.getAsString("deep_time"), bbo + "", result.getAsString("all"), sleepData);
					uploadSleep(yesterday, result.getAsString("begin"), result.getAsString("end"),
							result.getAsString("activeCount"), result.getAsString("light_time"),
							result.getAsString("deep_time"), bbo + "", result.getAsString("all"), sleepData);
					if (!result.getAsBoolean("hasdata")) {
						dbutils.saveSleepMsg(yesterday, ContextRef.getString(R.string.notworn));
					} else {
						dbutils.saveSleepMsg(yesterday, "");
					}
				} else {
					dbutils.saveSleepMsg(yesterday, "-/-");
				}
				// 收完0804

				sharePre.edit().putBoolean("getsleep", true).commit();
			}
			after0804();

			break;
		case 0x00:
			after0804();
			break;
		}

		// requestData();
		// NotifyPD_BT();
	}

	// 写智能开关等
	private void after0804() {

		Object obj[] = dbutils.getUserProfile();
		int height = 170;
		try {
			height = Math.round(Float.valueOf(obj[5].toString()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (sharePre == null) {
			sharePre = ContextRef.getSharedPreferences("air", ContextRef.MODE_MULTI_PROCESS);

			SendLostDelay(islost == 1 ? true : false, 1, sharePre.getInt("lan", 1), sharePre.getInt("liftwrist", 1),
					height);
		} else {
			SendLostDelay(islost == 1 ? true : false, 1, sharePre.getInt("lan", 1), sharePre.getInt("liftwrist", 1),
					height);
		}
		inDatatrans = false;
	}

	public void setAirDisplay(int hasStep, int languge, int handup) {
		Send(composePacket.ComposeAirDisplay(hasStep, languge, handup));
	}

	private int getBounds(int te[], int active_time, int activeCount, int deep_time, int light_time) {
		long time = System.currentTimeMillis();
		float x = 0;
		long i = 0;
		// 总睡眠时间少于7小时 没分钟减0.24分
		if ((i = (te[1] - te[0]) * 60 - activeCount * 60 - 7 * 3600) < 0) {
			float t = (Math.abs(i) / 60) * 0.24f;
			x = t;
			if (t >= 0) {
				dbutils.saveSleepBoundsRecode(t, MyDate.getYesterDay(),
						String.format(ContextRef.getString(R.string.sleep_bounds_duration), String.format("%.1f", t)),
						time);
			}

		} else {
			dbutils.saveSleepBoundsRecode(0, MyDate.getYesterDay(),
					String.format(ContextRef.getString(R.string.sleep_bounds_duration), "0"), time);
		}
		// 醒来次数每次减5分
		if (active_time > 0) {
			float t = (active_time) * 5;
			x += t;
			if (t >= 0) {
				dbutils.saveSleepBoundsRecode(t, MyDate.getYesterDay(),
						String.format(ContextRef.getString(R.string.sleep_bounds_wake), String.format("%.1f", t)),
						time);
			}
		} else {
			dbutils.saveSleepBoundsRecode(0, MyDate.getYesterDay(),
					String.format(ContextRef.getString(R.string.sleep_bounds_wake), "0"), time);
		}
		// D=深睡眠/(深睡眠+浅睡眠), D小于30% 时： 每小于 1个百分点，减0.2分
		float z = 0;
		if ((z = (float) (deep_time) / (float) (deep_time + light_time)) < 0.3f) {
			if (z == 0) {
				z = 1 - z;
			}
			float t = (z * 100) * 0.2f;
			x += t;
			if (t >= 0) {
				dbutils.saveSleepBoundsRecode(t, MyDate.getYesterDay(),
						String.format(ContextRef.getString(R.string.sleep_bounds_deep), String.format("%.1f", t)),
						time);
			}
		}
		if (te[0] > 11 * 60) {
			float t = 0.18f * (te[0] - 11 * 60);
			if (t > 100) {
				t = 70;
			}
			if (t >= 0) {
				dbutils.saveSleepBoundsRecode(t, MyDate.getYesterDay(),
						String.format(ContextRef.getString(R.string.sleep_bounds_late), String.format("%.1f", t)),
						time);
			}

			x += t;
		}
		return Math.round((100 - x) < 0 ? 0 : (100 - x));
	}

	String session;

	String getString(byte[] data) {
		StringBuffer sb = new StringBuffer();

		for (byte bit : data) {
			sb.append(Converter.byteToHexString(bit));
			sb.append("_");
		}
		return sb.substring(0, sb.length() - 1).toString();
	}

	private void uploadSleep(final String date, final String begin, final String end, final String active,
			final String light, final String deep, final String bounds, final String count, final byte[] data) {
		// uploadSleep(yesterday, result.getAsString("begin"),
		// result.getAsString("end"),
		// result.getAsString("activeCount"),
		// result.getAsString("light_time"),
		// result.getAsString("deep_time"), bbo + "",
		// result.getAsString("all"), sleepData);
		session = preferences.getString("session_id", "lovefit");
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				JSONObject obj = new JSONObject();
				try {
					obj.accumulate("time", MyDate.getYesterDay());

					// obj.accumulate("blob", "0x"+Dbutils.getString(data));
					obj.accumulate("blob", getString(data));
					String str = MyDate.getYesterDay();
					// count
					if (Integer.valueOf(begin.split(":")[0]) > 18) {
						str = MyDate.getYesterDay();
					} else {
						str = MyDate.getFileName();
					}
					obj.accumulate("stime", str + ' ' + begin + ":00");
					obj.accumulate("etime", MyDate.getFileName() + ' ' + end + ":00");
					obj.accumulate("wake", count);
					obj.accumulate("deep", deep);
					obj.accumulate("light", light);
					obj.accumulate("total", "100");
					obj.accumulate("score", bounds);
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				String url = String.format("%s/data/upLoadSleepDataJson/session/%s", HttpUtlis.BASE_URL, session);

				try {

					String result = HttpRequest.sendPost(url, "data=" + obj.toString() + "");
					System.out.println(result);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	/***
	 * 校验CRC
	 * 
	 * @param data
	 *            收到的byte[]
	 * @return 错误返回false
	 */
	private boolean CheckCRC(byte[] data) {
		try {
			int len = ((data[4] & 0xff) << 24) + ((data[5] & 0xff) << 16) + ((data[6] & 0xff) << 8) + (data[7] & 0xff);
			// 校验crc
			byte[] temp = new byte[len];
			System.arraycopy(data, 0, temp, 0, len - 2);
			MLinkCRC.crc16(temp, temp.length - 2);
			if (temp[len - 2] != data[len - 2] || temp[len - 1] != data[len - 1]) {
				inDatatrans = false;
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			inDatatrans = false;
			e.printStackTrace();
			return false;
		}
	}

	int screen_time = 0;
	int vibrator_time = 0;
	int batteryNow = 0;
	int tar = 10000;

	private long parserStep1(byte[] step) {
		if (!CheckCRC(step)) {
			LogUtil.e("--CRC ERROR 0803--", true);
			return -1;
		}
		int index = 10;
		// 解析计步器值
		String time = String.format("%1$04d-%2$02d-%3$02d", 2000 + (step[1 + index] & 0xff), (step[2 + index] & 0xff),
				(step[3 + index] & 0xff));

		// 电量
		int battery = step[12 + index] & 0xff;
		batteryNow = battery;
		showAirLowPowerNotification(battery);
		// 更新电量信息
		BluetoothLeService.device_battery = battery;
		// 体重
		int weight = step[13 + index] & 0xff;
		// 步幅
		int stride = step[14 + index] & 0xff;

		// 消耗卡路里 100卡
		long cal = ((step[15 + index] & 0xff) << 24) + ((step[16 + index] & 0xff) << 16)
				+ ((step[17 + index] & 0xff) << 8) + (step[18 + index] & 0xff);
		cal = cal * 100;
		long step_all = ((step[19 + index] & 0xff) << 24) + ((step[20 + index] & 0xff) << 16)
				+ ((step[21 + index] & 0xff) << 8) + (step[22 + index] & 0xff);
		long distance = ((step[23 + index] & 0xff) << 24) + ((step[24 + index] & 0xff) << 16)
				+ ((step[25 + index] & 0xff) << 8) + (step[26 + index] & 0xff);
		if (BluetoothLeService.isdebug) {
			LogUtil.e("step:" + step_all + ";距离：" + distance + ";cal:" + cal + "detail:"
					+ Converter.byteArrayToHexString(step));

		}
		if (step_all > 0 && step_all < 99999) {

			upspByUid(step_all, cal, distance);
		}

		LogUtil.e("---------------------->on0803");
		updatenotification(ContextRef, (float) step_all / tar, (int) step_all, (int) cal, (int) distance, true);
		LogUtil.e("--收到0803--" + step_all, true);
		LogUtil.e("0803步数：" + step_all + "距离" + distance);
		if (sharePre == null) {
			sharePre = ContextRef.getSharedPreferences("air", ContextRef.MODE_MULTI_PROCESS);
		}
		if (ContextRef != null) {
			LogUtil.e("--0803解析完成开始广播--" + new SimpleDateFormat("yyyy-MM-dd-HH：mm：ss-SSS").format(new Date()));
			Intent intent = new Intent(ACTION_STEP_CHANGED);
			intent.putExtra("steps", step_all);
			intent.putExtra("dis", distance);
			intent.putExtra("cal", cal);
			ContextRef.sendBroadcast(intent);
			SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
			Date date;
			long times = Calendar.getInstance().getTimeInMillis() / 1000;
			try {
				date = s.parse(time);
				times = date.getTime() / 1000;
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// SyncQQHealth.SyncQQToday(ContextRef,
			// preferences.getString("qq_token", null),
			// preferences.getString("qq_openid", ""), (int) step_all,
			// (int) distance, (int) (step_all / 2.5f), times,
			// (int) (cal / 1000),false);

			// MyLog.e(TAG, "发送数据接收完成广播");
		}
		if (((step[45] & 0xff) == 0x31) && ((step[46] & 0xff) == 0x01)) {
			// 开屏
			screen_time = ((step[48] & 0xff) << 24) + ((step[49] & 0xff) << 16) + ((step[50] & 0xff) << 8)
					+ (step[51] & 0xff);
		}
		if (((step[52] & 0xff) == 0x31) && ((step[53] & 0xff) == 0x02)) {
			// 振动
			vibrator_time = ((step[55] & 0xff) << 24) + ((step[56] & 0xff) << 16) + ((step[57] & 0xff) << 8)
					+ (step[58] & 0xff);
		}
		String today = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
		String app = "";
		try {
			PackageInfo info = ContextRef.getPackageManager().getPackageInfo(ContextRef.getPackageName(), 0);
			app = info.versionName;
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			dbutils.saveAirRecord(today, dbutils.getBTDeivceAddress(), screen_time + "", vibrator_time + "",
					Build.MANUFACTURER + "_" + Build.MODEL, sharePre.getString("air_device_id", ""), app,
					sharePre.getString("soft_version", ""), BluetoothLeService.device_battery + "");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// SavePD_DAY(time, step_all, cal, distance);
		/*
		 * StringBuilder strbuf = new StringBuilder();
		 * strbuf.append("包类型：:"+step[9]); strbuf.append("当前记录信息如下:");
		 * strbuf.append("时间:" + time); strbuf.append("电量:" + battery);
		 * strbuf.append("体重" + weight); strbuf.append("步幅" + stride);
		 * strbuf.append("消耗卡路里:" + cal); strbuf.append("总步数:" + step_all);
		 * strbuf.append("总距离:" + distance);
		 */
		// Toast.makeText(ContextRef.getApplicationContext(), strbuf.toString(),
		// Toast.LENGTH_SHORT).show();
		// int step, int dis, int cal, int type, String time, String update
		//
		dbutils.SaveSpBasicData((int) step_all, (int) distance, (int) cal, devicetype, time, time);

		// MyLog.d("Parser", "步数："+step_all);
		return step_all;
	}

	private void upspByUid(final long step, final long calorie, final long distance) {
		// TODO Auto-generated method stub
		new Thread(new Runnable() {
			public void run() {
				try {
					String url = String.format(
							"%s/data/upLoadSpDataByUid/uid/%s/step/%d/calorie/%d/distance/%s/time/%s/type/2/calc/1",
							HttpUtlis.BASE_URL, SharePreUtils.getInstance(ContextRef).getUid(), step, calorie, distance,
							MyDate.getFileName());
					if (BluetoothLeService.bLogfile)
						MyLog.e("Uploaddata", "url:" + url);
					String str = HttpRequest.sendGet(url);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void parserStepHistory(byte[] step) {
		if (!CheckCRC(step)) {
			return;
		}
		int index = 10;
		// 解析计步器值
		String time = String.format("%1$04d-%2$02d-%3$02d", 2000 + (step[1 + index] & 0xff), (step[2 + index] & 0xff),
				(step[3 + index] & 0xff));

		// 电量
		int battery = step[12 + index] & 0xff;
		// 体重
		int weight = step[13 + index] & 0xff;
		// 步幅
		int stride = step[14 + index] & 0xff;

		// 消耗卡路里 100卡
		long cal = ((step[15 + index] & 0xff) << 24) + ((step[16 + index] & 0xff) << 16)
				+ ((step[17 + index] & 0xff) << 8) + (step[18 + index] & 0xff);
		cal = cal * 100;
		long step_all = ((step[19 + index] & 0xff) << 24) + ((step[20 + index] & 0xff) << 16)
				+ ((step[21 + index] & 0xff) << 8) + (step[22 + index] & 0xff);
		long distance = ((step[23 + index] & 0xff) << 24) + ((step[24 + index] & 0xff) << 16)
				+ ((step[25 + index] & 0xff) << 8) + (step[26 + index] & 0xff);
		if (BluetoothLeService.bLogfile) {
			LogUtil.e("历史step:" + step_all + ";距离：" + distance + ";cal:" + cal + "detail:"
					+ Converter.byteArrayToHexString(step));
		}
		// SavePD_DAY(time, step_all, cal, distance);
		/*
		 * StringBuilder strbuf = new StringBuilder();
		 * strbuf.append("包类型：:"+step[9]); strbuf.append("当前记录信息如下:");
		 * strbuf.append("时间:" + time); strbuf.append("电量:" + battery);
		 * strbuf.append("体重" + weight); strbuf.append("步幅" + stride);
		 * strbuf.append("消耗卡路里:" + cal); strbuf.append("总步数:" + step_all);
		 * strbuf.append("总距离:" + distance);
		 */
		// Toast.makeText(ContextRef.getApplicationContext(), strbuf.toString(),
		// Toast.LENGTH_SHORT).show();
		// int step, int dis, int cal, int type, String time, String update
		//
		dbutils.SaveSpBasicData((int) step_all, (int) distance, (int) cal, devicetype, time, time);

		// MyLog.d("Parser", "步数："+step_all);
	}

	public void showAirLowPowerNotification(int power) {

		String today = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
		if (!today.equals(preferences.getString("powertime", "")) && power < 15) {
			NotificationUtils not = new NotificationUtils(ContextRef, NotificationUtils.SportsNotfication);
			not.showNotification(ContextRef.getString(R.string.notification_power_title), "你设备的电量已经低于" + power + "%",
					SlideMainActivity.class, true);
			preferences.edit().putString("powertime", today).commit();
		}
	}

	private void onPacket90() {
		// Received data:A7 B8 00 01 00 00 00 10 90 01 90 04 01 00 24 6F

		if (packet[9] == 1) {
			// 设备发送的数据
			if ((packet[10] & 0xff) == 0x90) {
				Intent intent = new Intent(BluetoothLeService.ACTION_BLE_DEVICE_ALARM);

				switch (packet[11]) {
				case 3:
					// 来电挂断
					// 判断
					intent.putExtra("type", 3);
					ContextRef.sendBroadcast(intent);
					break;
				case 4:
					// 呼叫手机
					intent.putExtra("type", 4);
					ContextRef.sendBroadcast(intent);
					break;
				case 5:
					// 遥控
					intent.putExtra("type", 5);
					ContextRef.sendBroadcast(intent);
					break;
				}
			}
		}
	}

	private void onPacket92() {
		// MyLog.d("fileupdate", "升级命令");
		Intent intent2 = new Intent(BluetoothLeService.ACTION_BT_REBOOT);
		ContextRef.sendBroadcast(intent2);

	}

	public void closeLostOnnight(int t) {
		Send(composePacket.ComposeLostOnNight(t));
	}

	private void onPacket9E() {

		switch (packet[9]) {
		case 0:
			// 写入
			onPacket9E00();
			break;
		case 1:
			// // 读取
			// Converter.byteArrayToHexString(packet);
			// if(((packet[10] & 0xff) == 0x9e) && ((packet[11] & 0xff) ==
			// 0x09)){
			// //得到发射功率9e 09 0x 0x
			// System.out.println(packet[13]);
			// return;
			// }
			Intent intent = new Intent(SimpleHomeFragment.ACTION_GET0804);
			ContextRef.sendBroadcast(intent);
			onPacket9E01();
		}

	}

	private void onPacket9E00() {
		// 写完智能开关及延时
		if (writeDelay) {
			writeDelay = false;
			// 睡眠数据，分析
			// 请求睡眠数据
			// Send(composePacket.ComposeSleepData((byte)4,(byte)1));
		} else {
			// 防丢总开关改变之后是否要写一次智能开关？
			// Send(composePacket.ComposeAirAntiLost(sharePre.getBoolean("set_isantilost",
			// true), (byte)0x3c));
		}
		Intent intent;
		int len = ((packet[4] & 0xff) << 24) + ((packet[5] & 0xff) << 16) + ((packet[6] & 0xff) << 8)
				+ (packet[7] & 0xff);
		byte[] arr = new byte[len];
		System.arraycopy(packet, 0, arr, 0, len);
		switch (packet[11] & 0xff) {
		case 13:
			// 显示，抬腕语言等
			intent = new Intent();
			intent.setAction(BluetoothLeService.ACTION_BLE_CONFIG_READ_RESPONSE);
			intent.putExtra("write", 13);
			ContextRef.sendBroadcast(intent);
			break;
		case 16:
			// 防丢开关
			intent = new Intent();
			intent.setAction(BluetoothLeService.ACTION_BLE_CONFIG_READ_RESPONSE);
			intent.putExtra("write", 16);
			ContextRef.sendBroadcast(intent);
			break;
		case 07:
			// 闹钟
			intent = new Intent();
			intent.setAction(BluetoothLeService.ACTION_BLE_CONFIG_READ_RESPONSE);
			intent.putExtra("write", 7);
			ContextRef.sendBroadcast(intent);
			break;
		}
		if (len == 22) {
			if ((arr[11] & 0xff) == 0x10 && (arr[13] & 0xff) == 0x0f && (arr[15] & 0xff) == 0x0d
					&& (arr[17] & 0xff) == 0x01 && (arr[19] & 0xff) == 0x05) {
				// 连接后读取配置
				readConfig();
			}
		}
		// 配置写入成功
		// MyLog.e(TAG, "配置写入成功");
	}

	// 智能开关 一直打开

	private void onPacket9E01() {
		if (sharePre == null) {
			// MODE_MULTI_PROCESS is 4
			sharePre = ContextRef.getSharedPreferences("air", ContextRef.MODE_MULTI_PROCESS);
		}
		HashMap<String, Object> map = new HashMap<String, Object>();
		byte[] ba1;
		Intent intent;

		int len = (packet[7] & 0xff) + ((packet[6] & 0xff) << 8);
		int height = 0;
		int weight = 0;
		int sex = 0;
		int steplen = 0;
		int vibration = 0;
		int realsync = 0;
		alarm1_enable = 0;
		alarm1_h = 0;
		alarm1_m = 0;
		alarm2_enable = 0;
		alarm2_h = 0;
		alarm2_m = 0;
		int age = 0;
		int lost_enable = 0;
		int lost_distance = 0;
		int lost_level = 0;
		int ack_delay = 0;
		String device_name = "";
		String device_id = "";
		String hex_ver = "";
		String pcb_ver = "";
		String boot_ver = "";
		String date_product = "";
		len = len - 4;
		// MyLog.v("kkk", "on 9e");
		// 身高 0x9E01 1 byte
		for (int i = 10; i < len; i++) {
			if (((packet[i] & 0xff) == 0x9e) && ((packet[i + 1] & 0xff) == 0x01)) {
				height = packet[i + 3] & 0xff;
				break;
			}
		}
		for (int i = 10; i < len; i++) {
			if (((packet[i] & 0xff) == 0x9e) && ((packet[i + 1] & 0xff) == 0x09)) {
				int x = packet[i + 3] & 0xff;
				break;
			}
		}
		// 体重 0x9E02
		for (int i = 10; i < len; i++) {
			if (((packet[i] & 0xff) == 0x9e) && ((packet[i + 1] & 0xff) == 0x02)) {
				weight = packet[i + 3] & 0xff;
				break;
			}
		}
		// 性别 0x9E03 1：男；0：女
		for (int i = 10; i < len; i++) {
			if (((packet[i] & 0xff) == 0x9e) && ((packet[i + 1] & 0xff) == 0x03)) {
				sex = packet[i + 3] & 0xff;
				break;
			}
		}
		// 步长 0x9E04
		for (int i = 10; i < len; i++) {
			if (((packet[i] & 0xff) == 0x9e) && ((packet[i + 1] & 0xff) == 0x04)) {
				steplen = packet[i + 3] & 0xff;
				map.put("steplen", steplen);
				break;
			}
		}
		// 振动 0x9E05
		for (int i = 10; i < len; i++) {
			if (((packet[i] & 0xff) == 0x9e) && ((packet[i + 1] & 0xff) == 0x05)) {
				vibration = packet[i + 3] & 0xff;
				ba1 = new byte[1];
				System.arraycopy(packet, i + 3, ba1, 0, ba1.length);
				intent = new Intent();
				intent.setAction(BluetoothLeService.ACTION_BLE_CONFIG_READ_RESPONSE);
				intent.putExtra("type", 5);
				intent.putExtra("value", ba1);
				ContextRef.sendBroadcast(intent);
				map.put("steplen", ba1);
				break;
			}
		}
		// 实时同步开关 0x9E06
		for (int i = 10; i < len; i++) {
			if (((packet[i] & 0xff) == 0x9e) && ((packet[i + 1] & 0xff) == 0x06)) {
				realsync = packet[i + 3];
				break;
			}
		}
		// 闹钟 0x9E07
		for (int i = 10; i < len; i++) {
			if (((packet[i] & 0xff) == 0x9e) && ((packet[i + 1] & 0xff) == 0x07)) {
				alarm1_enable = packet[i + 3];
				alarm1_h = packet[i + 4];
				alarm1_m = packet[i + 5];
				alarm2_enable = packet[i + 6];
				alarm2_h = packet[i + 7];
				alarm2_m = packet[i + 8];
				if ((packet[i + 9] & 0xff) == 0x9e && (packet[i + 10]) == 0x23) {
					String w1 = transform(packet[i + 11]);
					String w2 = transform(packet[i + 12]);
					StringBuffer sb1 = new StringBuffer();
					for (int t = 1; t < w1.length(); t++) {
						sb1.append(w1.charAt(t));
						if (t != w1.length() - 1) {
							sb1.append("-");
						}
					}
					StringBuffer sb2 = new StringBuffer();
					for (int t = 1; t < w2.length(); t++) {
						sb2.append(w2.charAt(t));
						if (t != w2.length() - 1) {
							sb2.append("-");
						}
					}
					sharePre.edit().putString("week1", sb1.toString()).commit();
					sharePre.edit().putString("week2", sb2.toString()).commit();
				}
				ba1 = new byte[6];
				System.arraycopy(packet, i + 3, ba1, 0, ba1.length);
				if (ba1[0] == 1) {
					sharePre.edit().putBoolean("set_alarm1", true).commit();

				} else {
					sharePre.edit().putBoolean("set_alarm1", false).commit();
				}
				map.put("alarm1", ba1[0]);
				if (ba1[3] == 1) {
					sharePre.edit().putBoolean("set_alarm2", true).commit();
				} else {
					sharePre.edit().putBoolean("set_alarm2", false).commit();
				}
				String a1 = AirPreferenceActivity.turnNum(ba1[1]) + ":" + AirPreferenceActivity.turnNum(ba1[2]);
				String a2 = AirPreferenceActivity.turnNum(ba1[4]) + ":" + AirPreferenceActivity.turnNum(ba1[5]);
				map.put("alarm2", ba1[3]);
				map.put("alarm1v", a1);
				map.put("alarm2v", a1);

				sharePre.edit().putString("set_alarm1_time", a1).commit();
				sharePre.edit().putString("set_alarm2_time", a2).commit();
				intent = new Intent();
				intent.setAction(BluetoothLeService.ACTION_BLE_CONFIG_READ_RESPONSE);
				intent.putExtra("type", 7);
				intent.putExtra("value", ba1);
				ContextRef.sendBroadcast(intent);

				/*
				 * StringBuilder strbuf = new StringBuilder();
				 * strbuf.append("包类型：:"+packet[9]); strbuf.append("当前记录信息如下:");
				 * 
				 * strbuf.append("闹钟1:" +
				 * alarm1_enable+"-"+alarm1_h+"-"+alarm1_m);
				 * strbuf.append("闹钟2:" +
				 * alarm2_enable+"-"+alarm2_h+"-"+alarm2_m);
				 * 
				 * MyLog.e(TAG, strbuf.toString());
				 */
				break;
			}
		}
		// 年龄 0x9E08
		for (int i = 10; i < len; i++) {
			if (((packet[i] & 0xff) == 0x9e) && ((packet[i + 1] & 0xff) == 0x08)) {
				age = packet[i + 3];
				break;
			}
		}
		// 运动 - 语言 -抬腕
		for (int i = 10; i < len; i++) {
			if (((packet[i] & 0xff) == 0x9e) && ((packet[i + 1] & 0xff) == 0x0d)) {
				sharePre.edit().putInt("liftwrist", packet[i + 5]).commit();
				sharePre.edit().putInt("lan", packet[i + 4]).commit();
				map.put("hand", packet[i + 5]);
				map.put("lan", packet[i + 4]);
				break;
			}
		}
		// 防丢开关 0x9E10
		for (int i = 10; i < len; i++) {
			if (((packet[i] & 0xff) == 0x9e) && ((packet[i + 1] & 0xff) == 0x10)) {
				lost_enable = packet[i + 3];
				map.put("lost", lost_enable);
				break;
			}
		}
		// 防丢距离 0x9E11
		for (int i = 10; i < len; i++) {
			if (((packet[i] & 0xff) == 0x9e) && ((packet[i + 1] & 0xff) == 0x11)) {
				lost_distance = packet[i + 3];
				break;
			}
		}
		// 防丢等级 0x9E12
		for (int i = 10; i < len; i++) {
			if (((packet[i] & 0xff) == 0x9e) && ((packet[i + 1] & 0xff) == 0x12)) {
				lost_level = packet[i + 3];
				break;
			}
		}
		// 响应延迟 0x9E13
		for (int i = 10; i < len; i++) {
			if (((packet[i] & 0xff) == 0x9e) && ((packet[i + 1] & 0xff) == 0x13)) {
				ack_delay = (packet[i + 3] & 0xff) + ((packet[i + 4] & 0xff) << 8);
				map.put("lost_delay", ack_delay);
				break;
			}
		}
		// 设备名称 0x9E14
		for (int i = 10; i < len; i++) {
			if (((packet[i] & 0xff) == 0x9e) && ((packet[i + 1] & 0xff) == 0x14)) {
				// device_name = new String(packet, packet[i + 3], packet[i +
				// 2]);

				for (int j = 0; j < packet[i + 2]; j++) {
					if (packet[i + 3 + j] != 0) {
						device_name += (char) packet[i + 3 + j];
					} else {
						break;
					}
				}

				// MyLog.v("kkk", "on name");

				intent = new Intent();
				intent.setAction(BluetoothLeService.ACTION_BLE_CONFIG_READ_RESPONSE);
				intent.putExtra("type", 20);
				intent.putExtra("value", device_name);
				ContextRef.sendBroadcast(intent);

				break;
			}
		}
		// 设备ID 0x9E15
		for (int i = 10; i < len; i++) {
			if (((packet[i] & 0xff) == 0x9e) && ((packet[i + 1] & 0xff) == 0x15) && ((packet[i + 2] & 0xff) == 16)) {
				// device_id = new String(packet, packet[i + 3], packet[i + 2]);
				for (int j = 0; j < packet[i + 2]; j++) {
					if (packet[i + 3 + j] != 0) {
						device_id += (char) packet[i + 3 + j];
					} else {
						break;
					}
				}
				break;
			}
		}
		if (sharePre == null) {
			// MODE_MULTI_PROCESS is 4
			sharePre = ContextRef.getSharedPreferences("air", ContextRef.MODE_MULTI_PROCESS);
		}

		if (device_id.length() > 1) {
			// Air设备id
			sharePre.edit().putString("air_device_id", device_id).commit();
		}
		String today = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
		// 软件版本号 0x9E16
		for (int i = 10; i < len; i++) {
			if (((packet[i] & 0xff) == 0x9e) && ((packet[i + 1] & 0xff) == 0x16)) {
				// hex_ver = new String(packet, packet[i + 3], packet[i + 2]);
				for (int j = 0; j < packet[i + 2]; j++) {
					if (packet[i + 3 + j] != 0) {
						hex_ver += (char) packet[i + 3 + j];

					} else {
						break;
					}
				}

				if (hex_ver.length() > 1) {
					intent = new Intent();
					intent.setAction(BluetoothLeService.ACTION_BLE_CONFIG_READ_RESPONSE);
					intent.putExtra("type", 22);
					intent.putExtra("value", hex_ver);
					map.put("version", hex_ver);
					intent.putExtra("setting", map);
					ContextRef.sendBroadcast(intent);

				}
				break;
			}
		}
		if (sharePre.getBoolean("hasfw", false)) {
			// checkAirVersion(hex_ver);
		}

		// 硬件版本号 0x9E17
		for (int i = 10; i < len; i++) {
			if (((packet[i] & 0xff) == 0x9e) && ((packet[i + 1] & 0xff) == 0x17)) {
				// pcb_ver = new String(packet, packet[i + 3], packet[i + 2]);
				for (int j = 0; j < packet[i + 2]; j++) {
					if (packet[i + 3 + j] != 0) {
						pcb_ver += (char) packet[i + 3 + j];
					} else {
						break;
					}
				}
				break;
			}
		}
		// BOOT版本号 0x9E18
		for (int i = 10; i < len; i++) {
			if (((packet[i] & 0xff) == 0x9e) && ((packet[i + 1] & 0xff) == 0x18)) {
				// boot_ver = new String(packet, packet[i + 3], packet[i + 2]);
				for (int j = 0; j < packet[i + 2]; j++) {
					if (packet[i + 3 + j] != 0) {
						boot_ver += (char) packet[i + 3 + j];
					} else {
						break;
					}
				}
				break;
			}
		}
		// 生产日期 0x9E19
		for (int i = 10; i < len; i++) {
			if (((packet[i] & 0xff) == 0x9e) && ((packet[i + 1] & 0xff) == 0x19)) {
				// date_product = new String(packet, packet[i + 3], packet[i +
				// 2]);
				for (int j = 0; j < packet[i + 2]; j++) {
					if (packet[i + 3 + j] != 0) {
						date_product += (char) packet[i + 3 + j];
					} else {
						break;
					}
				}
				break;
			}
		}
		if (sharePre == null) {
			// MODE_MULTI_PROCESS is 4
			sharePre = ContextRef.getSharedPreferences("air", ContextRef.MODE_MULTI_PROCESS);
		}

		if (hex_ver.length() > 1) {
			// 软件版本
			sharePre.edit().putString("soft_version", hex_ver).commit();
		}
		/*
		 * StringBuilder strbuf = new StringBuilder();
		 * strbuf.append("包类型：:"+packet[9]); strbuf.append("当前记录信息如下:");
		 * strbuf.append("身高:" + height); strbuf.append("体重:" + weight);
		 * strbuf.append("性别" + sex); strbuf.append("步长" + steplen);
		 * strbuf.append("振动:" + vibration); //strbuf.append("实时同步开关:" +
		 * realsync); strbuf.append("闹钟1:" +
		 * alarm1_enable+"-"+alarm1_h+"-"+alarm1_m); strbuf.append("闹钟2:" +
		 * alarm2_enable+"-"+alarm2_h+"-"+alarm2_m); strbuf.append("年龄:" + age);
		 * //strbuf.append("防丢开关:" + lost_enable); //strbuf.append("防丢距离:" +
		 * lost_distance); //strbuf.append("防丢等级:" + lost_level);
		 * //strbuf.append("响应延迟:" + ack_delay); strbuf.append("设备名称:" +
		 * device_name); //strbuf.append("设备ID:" + device_id);
		 * strbuf.append("软件版本号:" + hex_ver); //strbuf.append("硬件版本号:" +
		 * pcb_ver); //strbuf.append("BOOT版本号:" + boot_ver);
		 * //strbuf.append("生产日期:" + date_product); MyLog.e("ConfigResult",
		 * strbuf.toString());
		 */
		switch (cur_state) {
		// 0:read data 1:alarm 2:person info 3: lost
		case 0:
			break;
		case 1:
			onAlarm();
			break;
		case 2:
			break;
		case 3:
			break;
		}
		String app = "";
		try {
			PackageInfo info = ContextRef.getPackageManager().getPackageInfo(ContextRef.getPackageName(), 0);
			app = info.versionName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (sharePre == null) {
			sharePre = ContextRef.getSharedPreferences("air", ContextRef.MODE_MULTI_PROCESS);
		}
		dbutils.saveAirRecord(today, dbutils.getBTDeivceAddress(), screen_time + "", vibrator_time + "",
				Build.MANUFACTURER + "_" + Build.MODEL, device_id, app, hex_ver,
				BluetoothLeService.device_battery + "");

	}

	public static String httppath = "http://www.lovefit.com/air/";

	private void checkAirVersion(final String ver) {
		// TODO Auto-generated method stub
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				String result = HttpUtlis.queryStringForPost(HttpUtlis.UPDATE_AIR);
				try {
					result = new String(result.getBytes("iso8859-1"), "utf-8");
					JSONObject js = new JSONObject(result);
					String newVersion = js.getString("verName");
					String newVersionText = js.getString("text");

					String filepath = ContextRef.getFilesDir().toString() + "/air";
					if (HttpUtlis.checkAir(ver, newVersion) > 0) {
						FileDownUtils fileDownUtils = new FileDownUtils(new OnHexDownloadListener() {

							@Override
							public void OnHexProgress(float pre) {
								// TODO Auto-generated method stub
							}

							@Override
							public void OnHexDownloaded(String name) {
								// TODO Auto-generated method stub
								if (name != null) {
									Intent intent = new Intent(BluetoothLeService.ACTION_AIR_CONTROL);
									intent.putExtra("task", 4);
									intent.putExtra("filename", "Air.hex");
									intent.putExtra("version", name);
									ContextRef.sendBroadcast(intent);
								}
							}
						}, filepath, httppath + newVersion + ".hex");
						fileDownUtils.start();
					}
					preferences.edit().putString("lastest_air", newVersion).commit();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void onAlarm() {
		// setAlarm();
		cur_state = 0;
		if (BluetoothLeService.bLogfile)
			MyLog.d(TAG, "Send Alarm packet");
	}

	private void NotifyPD_BT() {
		Intent intent2 = new Intent("com.lovefit.ui.ped.PedBriefActivity");
		ContextRef.sendBroadcast(intent2);
	}

	// mshen
	public void readConfig() {
		// MyLog.v("kkk", "readConfig");
		Send(composePacket.ComposeLEConfig2());
	}

	// 防丢+延时
	// public void setAirAntiLostsssssss(int isOn, int delay) {
	// if (isOn == 1) {
	// Send(composePacket.ComposeAirAntiLost(true, (byte) delay));
	// } else if (isOn == 0) {
	// Send(composePacket.ComposeAirAntiLost(false, (byte) delay));
	// }
	// }

	// 防丢
	public void setIsLost(int i) {
		Send(composePacket.ComposeLost(i));
	}

	public void setAlarm(int[] param, String w1, String w2) {
		/*
		 * sharePre =
		 * ContextRef.getSharedPreferences("air",ContextRef.MODE_MULTI_PROCESS);
		 * boolean alarm1= sharePre.getBoolean("set_alarm1", false); int
		 * alarm1_h = sharePre.getInt("set_alarm1_hour", 7); int alarm1_m =
		 * sharePre.getInt("set_alarm1_minute", 0);
		 * 
		 * boolean alarm2= sharePre.getBoolean("set_alarm2", false); int
		 * alarm2_h = sharePre.getInt("set_alarm2_hour", 7); int alarm2_m =
		 * sharePre.getInt("set_alarm2_minute", 0);
		 */
		// boolean isneed = false;

		// 1 表示开 0 表示关
		// alarm1_enable = 0;
		// alarm1_h = 0;
		// alarm1_m = 0;
		// alarm2_enable = 0;
		// alarm2_h = 0;
		// alarm2_m = 0;
		boolean alarm1 = (param[0] == 1);
		int alarm1_h = param[1];
		int alarm1_m = param[2];

		boolean alarm2 = (param[3] == 1);
		int alarm2_h = param[4];
		int alarm2_m = param[5];
		Send(composePacket.ComposeAlarm(alarm1, alarm1_h, alarm1_m, alarm2, alarm2_h, alarm2_m, w1, w2));

	}

	public String transform(byte x) {
		String temp = Integer.toBinaryString(x);
		int len = temp.length();
		if (len < 8 || x < 0) {
			temp = "00000000" + temp;
			temp = temp.substring(len, len + 8);
		}
		return temp;
	}

	public void setName(String name) {
		if (name != null) {
			Send(composePacket.composeConfigPacket_name(name));
		}
	}

	public void setVibrateEn(boolean vibrate_en) {
		Send(composePacket.composeVibrateEn(vibrate_en));
	}

	public void setConfigVibrateAction(int[] param) {
		// MyLog.v("kkk", "readConfig");
		Send(composePacket.composeConfigPacket_vibrate(param));
	}

	public void setConfigYaoyaoAction(int onoff) {
		// MyLog.v("kkk", "readConfig");
		Send(composePacket.composeConfigPacket_yaoyao_state(onoff));
	}

	public void setConfigShutdown() {
		// MyLog.v("kkk", "readConfig");
		Send(composePacket.composeConfigPacket_shutdown());
	}

	int islost;

	public void setLost(int i) {
		// TODO Auto-generated method stub
		islost = i;
	}

	static float no_p = 0;

	public static Notification updatenotification(Context ContextRef, float per, int step, int calorie, int dis,
			boolean connect) {
		Intent intent1 = new Intent(ContextRef, SlideMainActivity.class);
		intent1.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		int no_step, no_cal, no_dis;
		// if (step == -1) {
		// per = no_p;
		// step = no_step;
		// calorie = no_cal;
		// dis = no_dis;
		// }
		if (step <= 0) {
			Dbutils db = new Dbutils(ContextRef);
			Object[] target = db.getUserTarget();
			float step_aim = 10000;
			try {
				step_aim = Float.valueOf(target[3].toString());
				step_aim = (step_aim == 0 ? 10000 : step_aim);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Object[] obj = db.getSpBasicData(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
			if (obj == null) {
				obj = new Object[] { 0, 0, 0, 0, 0, 0, 0 };
			}
			no_p = Float.valueOf((obj[0].toString())) / step_aim;
			no_step = Integer.valueOf((obj[0].toString()));
			no_dis = Integer.valueOf(obj[1].toString());
			no_cal = Integer.valueOf(obj[2].toString());
		} else {
			no_p = per;
			no_step = step;
			no_cal = calorie;
			no_dis = dis;
		}

		// intent1.setAction(Intent.ACTION_MAIN);
		// intent1.addCategory(Intent.CATEGORY_LAUNCHER);
		// intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
		// | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(ContextRef, 0, intent1,
				PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationManager mNotificationManager = (NotificationManager) ContextRef
				.getSystemService(Context.NOTIFICATION_SERVICE);
		RemoteViews rvs = new RemoteViews(ContextRef.getPackageName(), R.layout.step_notification);
		mNotificationManager.cancel(NotificationUtils.GPSOrStepNotfication);
		if (per != -1) {
			rvs.setImageViewBitmap(R.id.icon, getCroppedBitmap(ContextRef, no_p, connect));
		} else {
			rvs.setImageViewResource(R.id.icon, R.drawable.ic_launcher);
		}
		rvs.setTextViewText(R.id.step, no_step + "");
		rvs.setTextViewText(R.id.kcal, String.format("%.1f", (no_cal / 1000f)));
		rvs.setTextViewText(R.id.dis, String.format("%.1f", no_dis / 1000f));
		// Sets an ID for the notification, so it can be updated
		NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(ContextRef)
				.setSmallIcon(R.drawable.ic_launcher).setContentTitle(null).setContentText(null)
				.setContentIntent(contentIntent).setWhen(System.currentTimeMillis()).setContent(rvs);
		Notification no = mNotifyBuilder.build();
		mNotificationManager.notify(NotificationUtils.BlueNotfication, no);

		return no;
	}

	public static Bitmap getCroppedBitmap(Context ContextRef, float progress, boolean connect) {

		Matrix m = new Matrix();

		int w = (int) (ContextRef.getResources().getDimensionPixelSize(R.dimen.notification_step));
		int h = w;
		Bitmap output = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG));
		final Paint paint = new Paint();
		paint.setTextSize(ContextRef.getResources().getDimensionPixelSize(R.dimen.text_small));
		paint.setStrokeWidth(5);
		paint.setColor(Color.parseColor("#ffffff"));
		paint.setStrokeCap(Cap.ROUND);
		paint.setStyle(Paint.Style.STROKE);
		paint.setAntiAlias(true);
		paint.setTextAlign(Align.CENTER);
		// paint.setFakeBoldText(true);

		paint.setColor(Color.parseColor("#33ffffff"));
		canvas.drawArc(new RectF(w * (0.1f), h * (0.1f), w * (0.9f), h * (0.9f)), -90f, 360, false, paint);
		paint.setStrokeWidth(8);
		if (connect) {
			paint.setColor(Color.parseColor("#ffffff"));
		} else {
			paint.setColor(Color.parseColor("#ff6000"));
		}

		canvas.drawArc(new RectF(w * (0.1f), h * (0.1f), w * (0.9f), h * (0.9f)), -90f, 360 * progress, false, paint);
		paint.setStrokeCap(Cap.SQUARE);
		Paint paint2 = new Paint();
		paint2.setTypeface(tf);
		paint2.setColor(Color.parseColor("#ffffff"));
		paint2.setStyle(Paint.Style.FILL);
		paint2.setTextSize(ContextRef.getResources().getDimension(R.dimen.text_middle));
		float f = paint2.measureText(Math.round(progress * 100) + "");
		canvas.drawText(Math.round(progress * 100) + "", (w * 0.5f) - (f * 0.5f), h * 0.6f, paint2);
		paint2.setTextSize(18);
		canvas.drawText("%", (w * 0.5f) + (f * 0.5f), h * 0.6f, paint2);
		return Bitmap.createBitmap(output, 0, 0, output.getWidth(), output.getHeight(), m, true);
	}

	public void SendNotification(String title, String msg, boolean callIn) {
		// TODO Auto-generated method stub
		try {
			Send(composePacket.composeNotification(title, msg, callIn));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
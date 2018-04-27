package com.bandlink.air.view;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

/***
 * 绘制睡眠数据，控件初始化需要调用setSleepData方法提供数据，提供绘制区域点击回调
 * 
 * @author Kevin
 * 
 */
public class SleepChart extends View {
	private int width;
	private int height;
	private byte[] data;

	private float unit;
	private OnTouchDataAreaListener listener;
	private Paint activePaint, lightPaint, aweakPaint, deepPaint, bgPaint;
	// private int from, to;
	private String starTime, endTime;
	private Context context;
	private boolean sameDay;

	public SleepChart(Context context, AttributeSet attrs) {
		super(context, attrs);
		// this.context = context;
		this.context = context;
		prepare();

	}

	public SleepChart(Context context) {
		super(context);
		// this.context = context;
		this.context = context;
		prepare();

	}

	void prepare() {
		DisplayMetrics metric = new DisplayMetrics();
		metric = getResources().getDisplayMetrics();
		width = metric.widthPixels; // 屏幕宽度（像素）
		height = metric.heightPixels; // 屏幕高度（像素）
		// density = metric.density; // 屏幕密度（0.75 / 1.0 / 1.5）
		// densityDpi = metric.densityDpi; // 屏幕密度DPI（120 / 160 / 240）
		// width = this.getWidth();
		// height = 500;
		lightPaint = new Paint();
		activePaint = new Paint();
		aweakPaint = new Paint();
		deepPaint = new Paint();
		bgPaint = new Paint();

		lightPaint.setAntiAlias(true);
		activePaint.setAntiAlias(true);
		aweakPaint.setAntiAlias(true);
		deepPaint.setAntiAlias(true);
		bgPaint.setAntiAlias(true);

		lightPaint.setColor(Color.parseColor("#13a3ff"));
		activePaint.setColor(Color.parseColor("#F5D700"));
		aweakPaint.setColor(Color.parseColor("#ff9000"));
		deepPaint.setColor(Color.parseColor("#2C599D"));
		bgPaint.setColor(Color.parseColor("#a0ffffff"));
		bgPaint.setAlpha(75);
	}

	byte[] sData;
	boolean hasdata = false;

	public boolean setSleepData(byte[] s) {
		this.sData = transData(s);
		te = checkSleep(sData,false);
		unit = (float) width / (float) (te[1] - te[0]);
		ContentValues values = analysisSleep(te, sData);
		hasdata = values.getAsBoolean("hasdata");
		return hasdata;
	}

	public static byte[] transData(byte[] s) {
		byte[] sData = new byte[s.length * 4];
		int sDatai = 0;
		for (int index = 0; index < 360; index++) {
			int num = ((s[index] + 256) % 256) & 0xff;
			for (int i = 0; i < 4; i++) {

				sData[sDatai++] = (byte) ((num >> 6) & 0x03);
				num <<= 2;
			}
		}
		return sData;
	}

	public static int[] checkSleep(byte[] minByte,boolean checkTime) {
		int[] result = new int[3];
		int[] activeByte = new int[1440];
		int[] activeByteMinus = new int[1440];
		int activeByteMinusi = 0;
		int activeBytei = 0;

		// 从晚上6点开始查找睡眠时间
		int startFindIndex = 540;
		int sleepCnt = 0;
		int beginFlag = 0, newBeginFlag = 0, endFlag = 0;
		int beginTime = 0, wakeCount = 0, endTime = 0;
		for (int i = startFindIndex; i < 1360; i++) {
			if (0x01 == minByte[i]) {
				newBeginFlag = 1;
				if (0 == beginFlag) {
					beginFlag = 1;
					beginTime = i;
				} else {
					wakeCount++;
				}
			}
			// 一次新的开始，那么查找第一个0x02作为结束
			if (newBeginFlag != 0) {
				sleepCnt++;
				if (0x02 == minByte[i]) {
					// 如果这次从开始到结束的时间小于30分钟就丢弃掉
					if (sleepCnt < 30) {
						beginFlag = 0;
					} else {
						endFlag = 1;
						endTime = i;
					}
					newBeginFlag = 0x00;
					sleepCnt = 0;
				}
			}
			if (0x02 == minByte[i]) {
				activeByte[activeBytei] = i;
				activeBytei++;
			}
		}

		beginFlag = 0;
		endFlag = 0;
		sleepCnt = 0;
		for (int i = 1; i < activeBytei; i++) {
			activeByteMinus[activeByteMinusi++] = activeByte[i]
					- activeByte[i - 1];
		}

		for (int i = 0; i < activeByteMinusi; i++) {
			// 大于2小时
			if (activeByteMinus[i] > 120) {
				boolean isRight = false;
				for (int j = activeByte[i] + 1; j < activeByte[i + 1]; j++) {
					if (minByte[j] != 0x00) {
						isRight = true;
						break;
					}
				}
				if (isRight) {
					if (beginFlag == 0) {
						beginFlag = 1;
						beginTime = activeByte[i];
						if (i != 0) {
							for (int j = i - 1; j >= 0; j--) {
								if (activeByteMinus[j] <= 50) {
									if (activeByte[j + 1] >= 600) {
										beginTime = activeByte[j + 1];
									}
									beginFlag = 2;
									break;
								} else
									sleepCnt++;
							}
						}
					}

					endTime = activeByte[i + 1];
					if (i < activeByteMinusi - 1) {

					}
					for (int j = i + 1; j < activeByteMinusi; j++) {
						if (activeByteMinus[j] <= 50) {
							endTime = activeByte[j];
							endFlag = 1;
							break;
						} else
							sleepCnt++;
					}
					if (endFlag == 1)
					{
						if(endTime <= 720)
						{
							
							beginFlag = 0;
						}
						else 
							break;
						
					}
				}
			}
		}

		if (beginFlag == 0 || endFlag == 0) {
			return null;
		}
		wakeCount = sleepCnt;
		result[0] = beginTime;
		result[1] = endTime;
		result[2] = wakeCount;
		Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		int endh = ((((result[1]) / 60) + 12) % 24);
		int endm = (((result[1]) % 60));

		// 如果醒来时间读在当前时间3分钟之后 扔掉

		// 如果相等，说明查找结束时间失败
		if(checkTime){
			
			if (result[1] != result[0]) {
				if (endh > hour) {
					return null;

				} else {
					if ((endh == hour) && (endm - minute > 1)) {
						return null;
					}
				}
			}
		}
		

		return result;

	}

	public static int[] checkSleepData(byte[] sData) {

		int[] te = new int[2];
		int beginFlag = 0, softBeginFlag = 0, newBeginFlag = 0, sleepCnt = 0, endFlag = 0;

		int aweakTimes = 0;
		int activeCount = 0;
		int light_time = 0, active_time = 0, deep_time = 0;
		endFlag = sleepCnt = newBeginFlag = beginFlag = aweakTimes = activeCount = light_time = active_time = deep_time = 0;

		for (int j = 360; j < sData.length; j++) {

			if (0x01 == sData[j]) {
				beginFlag = 1;
				te[0] = j;
				break;
			}
		}

		// 如果找到了起始标志
		// 结束时间定义，如果是当天，那么这个时间定义为当前时间
		// 如果是历史，那么定义为早上9点
		final int timeInterval = 60;
		int endTimeIndex = 1260;
		// 9点
		int endTimeFindIndex = 1260;
		int beginTimeindex = 720;
		int beginTimeFindindex = 0;
		boolean getSleepFlag = true;
		int softBegin = 0;
		// if(0 == beginFlag)
		{
			beginTimeFindindex = beginTimeindex;
			// 从24点往前找第一个不是02的索引
			while ((0x02 != sData[beginTimeFindindex])
					&& (beginTimeFindindex > 360)) {
				beginTimeFindindex--;
			}
			// 如果24前最后一个02 是在18点 或者没有找到02
			if (beginTimeFindindex == 360) {
				beginTimeFindindex = beginTimeindex;
				// 再从24点往后到4点 找最后一个02
				while ((0x02 != sData[beginTimeFindindex])
						&& (beginTimeFindindex < 960)) {
					beginTimeFindindex++;
				}
				// 如果没有找到说明 没有佩戴
				if (960 == beginTimeFindindex) {
					// 此种情况没有佩戴
				} else {
					// 找到02
					softBeginFlag = 1;
					softBegin = beginTimeFindindex;
				}
			} else {
				softBegin = beginTimeFindindex;
				beginTimeFindindex = beginTimeindex;
				// 从24点往后到4点 找最后一个02
				while ((0x02 != sData[beginTimeFindindex])
						&& (beginTimeFindindex < 960)) {
					beginTimeFindindex++;
				}

				softBeginFlag = 1;
				if (960 == beginTimeFindindex) {
					// 此种情况采用向前找到的0x02
				} else {
					// 如果新找到的位置大于 标记的1小时
					if (Math.abs(beginTimeFindindex - te[0]) > 60) {
						if (Math.abs(beginTimeindex - beginTimeFindindex) > Math
								.abs(beginTimeindex - softBegin)) {

						} else {
							// 新的离12点最近
							softBegin = beginTimeFindindex;
						}
					} else {
						// 新找到的小1小时
						softBegin = beginTimeFindindex;
					}
				}
			}
		}

		if (1 == beginFlag) {
			if (te[0] < 720) {
				if (Math.abs(beginTimeindex - te[0]) > Math.abs(beginTimeindex
						- softBegin)) {
					te[0] = softBegin;
				}
			}
		}

		te[1] = te[0];
		int j = 0;
		if ((1 == beginFlag) || (1 == softBegin)) {
			while (getSleepFlag) {
				// 遍历开始点往后的数据
				for (j = te[1] + 1; j < sData.length; j++) {
					// 如果碰到02
					if (0x02 == sData[j]) {
						// 先认为当前是醒来
						te[1] = j;
						// endTimeFindIndex = endTimeIndex;
						// 在醒来点到9点找第一个02
						while ((0x02 != sData[endTimeFindIndex])
								&& (endTimeFindIndex > te[1])) {
							endTimeFindIndex--;
						}
						// 没找到新的02
						if (endTimeFindIndex == te[1]) {
							// 标记的醒来点在8点之前
							if (Math.abs(endTimeIndex - te[1]) > timeInterval) {
								endTimeFindIndex = endTimeIndex;
								// 从开始点往11点找02
								while ((0x02 != sData[endTimeFindIndex])
										&& (endTimeFindIndex < 1380)) {
									endTimeFindIndex++;
								}
								// 不管有没有新的醒来点 认为endTimeFindIndex为醒来 （8-9）点
								te[1] = endTimeFindIndex;

								// 找到了，退出
								getSleepFlag = false;
							} else {
								// 8点之后 找到了，退出
								getSleepFlag = false;
							}
							endFlag = 1;
						} else if (endTimeFindIndex < te[1]) {
							// 如果在9点之后
							getSleepFlag = false;
							endFlag = 1;
						} else {
							// 找到了02 9点之前且 据结束索引endTimeFindIndex 超过1小时，进行下一次寻找
							if (Math.abs(endTimeFindIndex - te[1]) > timeInterval) {
								endTimeFindIndex--;
							} else {
								// 找到了，退出
								getSleepFlag = false;
								endFlag = 1;
							}
						}
						break;
					}
				}
				if (j == sData.length) {
					// 这里要判断 从起始开始后面的数据中0x00的比例，判断是否是没有佩戴
					getSleepFlag = false;
				}
			}
		}
		Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		int endh = ((((te[1]) / 60) + 12) % 24);
		int endm = (((te[1]) % 60));

		// 如果醒来时间读在当前时间3分钟之后 扔掉

		// 如果相等，说明查找结束时间失败
		if (te[1] != te[0]) {
			if (endh > hour) {
				if (hour >= 7) {
					te[1] = (hour < 12 ? hour + 12 : hour - 12) * 60 + minute;
				} else {
					te[1] = 0;
				}

			} else {
				if ((endh == hour) && (endm - minute > 3)) {
					if (hour >= 7) {
						te[1] = (hour < 12 ? hour + 12 : hour - 12) * 60
								+ minute;
					} else {
						te[1] = 0;
					}
				}
			}
		}

		return te;
	}

	public static ContentValues analysisSleep(int[] te, byte[] sData) {

		int activeCount = 0, light_time = 0, deep_time = 0;
		int lightCnt = 0;
		int lightFlag = 1;
		int isss = 0, aw = 0;
		// 一次浅睡眠的15分钟内都是浅睡眠
		int deepSleepReadyTime = 15;
		int sleepIng = 0x00;
		if ((te[0] * te[1]) != 0) {
			for (int i = te[0]; i < te[1]; i++) {
				if (lightFlag == 1) {
					lightCnt++;
					if (lightCnt > deepSleepReadyTime) {
						lightFlag = 0;
					}
				}

				if (0x01 == sData[i]) {
					sleepIng = 0x01;
					activeCount++;
					try {
						if (sData[i - 1] != 1) {
							aw++;
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				// 深睡眠
				if (0x00 == sData[i]) {
					// 记录连续静止
					isss++;
					if (lightFlag == 1) {
						// 浅
						light_time++;
					} else { // 深
						deep_time++;
					}
				} else {
					// 连续静止不超过4小时 清除计数
					if (isss < 240) {
						isss = 0;
					}
				}
				if (0x03 == sData[i]) {
					lightFlag = 1;
					lightCnt = 0x00;
					light_time++;
				}
				if (0x02 == sData[i]) {
					sleepIng = 0x00;
					lightFlag = 1;
					lightCnt = 0x00;
				}
				// sData[i] = 0;
			}

		}
		String begin = SleepChart.transformH(((te[0] / 60) + 12) % 24) + ":"
				+ SleepChart.transformM((te[0] % 60));
		String end = SleepChart.transformH((((te[1]) / 60) + 12) % 24) + ":"
				+ SleepChart.transformM(((te[1]) % 60));
		ContentValues data = new ContentValues();
		data.put("begin", begin);
		data.put("end", end);
		data.put("activeCount", ((--activeCount) < 0 ? ++activeCount
				: activeCount) * 60);
		data.put("deep_time", deep_time * 60);
		data.put("light_time", light_time * 60);
		data.put("hasdata", isss > 240f ? false : true);
		data.put("all", light_time * 60);
		data.put("awake", te[2]);
		return data;

	}

	public void setOnTouchDataAreaListener(OnTouchDataAreaListener listener) {
		this.listener = listener;
	}

	@Override
	protected void onDraw(Canvas canvas) {

		drawSleep0530(canvas);
		super.onDraw(canvas);
	}

	private void drawSleep0530(Canvas canvas) {
		Rect rs = new Rect(0, 10, width, height);
		canvas.drawRect(rs, bgPaint);
		activeCount = deep_time = active_time = light_time = 0;
		if (sData == null) {
			return;
		}
		if (!hasdata) {
			Rect rs22 = new Rect(0, 20, width, height);
			canvas.drawRect(rs22, bgPaint);
			if (listener != null) {
				listener.OnDateCalcComplete("-:-", "-:-", 0, 0, 0, 0);
			}
			return;
		}
		float width = 0;
		int lightCnt = 0;
		int lightFlag = 1;
		// 一次浅睡眠的15分钟内都是浅睡眠
		int deepSleepReadyTime = 15;
		int sleepIng = 0x00;
		for (int i = te[0]; i < te[1]; i++) {
			if (lightFlag == 1) {
				lightCnt++;
				if (lightCnt > deepSleepReadyTime) {
					lightFlag = 0;
				}
			}

			if (0x01 == sData[i]) {
				sleepIng = 0x01;
				activeCount++;
				Rect r = new Rect((int) (width), 100, (int) (unit + width),
						height);
				canvas.drawRect(r, aweakPaint);
				if (i < 1) {
					i = 1;
				}
				if (0x01 != sData[i - 1]) {
					aweakTimes++;
				}
			}

			// 深睡眠
			if (0x00 == sData[i]) {

				if (lightFlag == 1) {
					// 浅
					light_time++;
					Rect r = new Rect((int) (width), 140, (int) (unit + width),
							height);
					canvas.drawRect(r, lightPaint);
				} else { // 深
					deep_time++;
					Rect r = new Rect((int) (width), 70, (int) (unit + width),
							height);
					canvas.drawRect(r, deepPaint);
				}
			}
			if (0x03 == sData[i]) {
				lightFlag = 1;
				lightCnt = 0x00;
				light_time++;
				Rect r = new Rect((int) (width), 140, (int) (unit + width),
						height);
				canvas.drawRect(r, lightPaint);
			}
			if (0x02 == sData[i]) {
				sleepIng = 0x00;
				lightFlag = 1;
				lightCnt = 0x00;
				Rect r = new Rect((int) (width), 140, (int) (unit + width),
						height);
				canvas.drawRect(r, lightPaint);
			}
			// sData[i] = 0;
			width += unit;
		}
		if (listener != null) {
			if (te[0] >= te[1]) {
				listener.OnDateCalcComplete("-:-", "-:-", 0, 0, 0, 0);
			} else {
				activeCount -= 1;
				aweakTimes -= 1;
				if (activeCount < 0) {
					activeCount = 0;
				}
				if (aweakTimes < 0) {
					aweakTimes = 0;
				}
				listener.OnDateCalcComplete(
						transformH(((te[0] / 60) + 12) % 24) + ":"
								+ transformM((te[0] % 60)),
						transformH((((te[1]) / 60) + 12) % 24) + ":"
								+ transformM(((te[1]) % 60)), light_time * 60,
						activeCount * 60, te[2], deep_time * 60);
			}
		}
	}

	int aweakTimes = 0;
	int activeCount = 0;

	private int light_time = 0, active_time = 0, deep_time = 0;

	int deepCount = 0;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (listener == null) {
			return false;
		}
		if (event.getAction() == MotionEvent.ACTION_MOVE
				|| event.getAction() == MotionEvent.ACTION_DOWN) {
			int allMin = (int) ((te[1] - te[0]) * (event.getX() / width));
			// int hour = (int) ((from *4)/60+ ((to-from)*4*(event.getX() /
			// width))/60) +12;
			// int hour = (int) (from + ((to - from) * 60 * (event.getX() /
			// width)) / 60);

			// int min = (int) ((to-from)*4*(event.getX() / width))%60+((from
			// *4)/60+ (int)((to-from)*4*(event.getX() / width))%60);
			// drawLine(event.getX(),transform2(hour) + "." + transform2(min));
			int hour = ((te[0] + allMin) / 60) + 12;
			int min = ((te[0] + allMin) % 60);
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				listener.OnTouchDataListener(event.getX(),
						transformH(hour >= 24 ? hour - 24 : hour) + "."
								+ transformM(min), false);
			} else {
				listener.OnTouchDataListener(event.getX(),
						transformH(hour >= 24 ? hour - 24 : hour) + "."
								+ transformM(min), true);
			}

			return true;
		}
		return super.onTouchEvent(event);
	}

	public static String transformH(int t) {
		//
		if (t == 24 || t == 0) {
			return "00";
		} else if (t <= 9) {
			return "0" + t;
		} else {
			return t + "";
		}
	}

	public static String transformM(int t) {
		if (t == 0) {
			return "00";
		} else if (t <= 9) {
			return "0" + t;
		} else {
			return t + "";
		}
	}

	public interface OnTouchDataAreaListener {

		public void OnTouchDataListener(float offx, String time, boolean isMove);

		public void OnDateCalcComplete(String f, String t, int sleepcount,
				int active_time, int aweakTimes, int deep);
	}

	int[] te = new int[2];
	int beginFlag = 0, newBeginFlag = 0, sleepCnt = 0, endFlag = 0;

	boolean sp = false;

	public void setSleepDataStupid(byte[] s, int[] te) {
		sp = true;
		try {
			this.sData = new byte[s.length * 4];
			int sDatai = 0;
			for (int index = 0; index < 360; index++) {
				int num = ((s[index] + 256) % 256) & 0xff;
				for (int i = 0; i < 4; i++) {

					sData[sDatai++] = (byte) ((num >> 6) & 0x03);
					num <<= 2;
				}
			}
			this.te = te;

			unit = (float) width / (float) (te[1] - te[0]);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}

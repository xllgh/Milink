package com.bandlink.air.view;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.bandlink.air.MilinkApplication;
import com.bandlink.air.R;
import com.bandlink.air.util.Dbutils;
import com.bandlink.air.util.SharePreUtils;

/**
 * 日历控件 功能：获得点选的日期区间
 * 
 */
public class CalendarView extends View implements View.OnTouchListener {
	private final static String TAG = "anCalendar";
	private Date selectedStartDate;
	private Date selectedEndDate;
	private Date curDate; // 当前日历显示的月
	private Date today; // 今天的日期文字显示红色
	private Date downDate; // 手指按下状态时临时日期
	private Date showFirstDate, showLastDate; // 日历显示的第一个日期和最后一个日期
	private int downIndex; // 按下的格子索引
	private Calendar calendar;
	private Surface surface;
	private int[] date = new int[42]; // 日历显示数字
	private int curStartIndex, curEndIndex; // 当前显示的日历起始的索引
	private SimpleDateFormat spd;
	private Dbutils db;
	private SharedPreferences share;
	// private boolean completed = false; // 为false表示只选择了开始日期，true表示结束日期也选择了
	// 给控件设置监听事件
	private OnItemClickListener onItemClickListener;

	public CalendarView(Context context) {
		super(context);
		init();
	}

	public CalendarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	Bitmap refresh;
	Rect rect;

	private void init() {
		share = getContext()
				.getSharedPreferences(
						SharePreUtils.APP_ACTION,
						Context.MODE_PRIVATE);
		db = new Dbutils(share.getInt("UID", -1), getContext());
		spd = new SimpleDateFormat("yyyyMMdd");
		curDate = selectedStartDate = selectedEndDate = today = new Date();
		calendar = Calendar.getInstance();
		calendar.setTime(curDate);
		surface = new Surface();
		surface.density = getResources().getDisplayMetrics().density;
		setBackgroundColor(surface.bgColor);
		setOnTouchListener(this);
		refresh = BitmapFactory.decodeResource(getResources(),
				R.drawable.img_refresh);

	}

	public void setCellColor(int color) {
		if (surface != null) {
			surface.cellDownColor = color;
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		surface.width = getResources().getDisplayMetrics().widthPixels;
		surface.height = (int) ((getResources().getDisplayMetrics().heightPixels * 8) / 11);
		// if (View.MeasureSpec.getMode(widthMeasureSpec) ==
		// View.MeasureSpec.EXACTLY) {
		// surface.width = View.MeasureSpec.getSize(widthMeasureSpec);
		// }
		// if (View.MeasureSpec.getMode(heightMeasureSpec) ==
		// View.MeasureSpec.EXACTLY) {
		// surface.height = View.MeasureSpec.getSize(heightMeasureSpec);
		// }
		widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(surface.width,
				View.MeasureSpec.EXACTLY);
		heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(surface.height,
				View.MeasureSpec.EXACTLY);
		setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		Log.d(TAG, "[onLayout] changed:"
				+ (changed ? "new size" : "not change") + " left:" + left
				+ " top:" + top + " right:" + right + " bottom:" + bottom);
		if (changed) {
			surface.init();
		}
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Log.d(TAG, "onDraw");
		// 画框
		canvas.drawPath(surface.boxPath, surface.borderPaint);
		// 年月
		// String monthText = getYearAndmonth();
		// float textWidth = surface.monthPaint.measureText(monthText);
		// canvas.drawText(monthText, (surface.width - textWidth) / 2f,
		// surface.monthHeight * 3 / 4f, surface.monthPaint);
		// 上一月/下一月
		// canvas.drawPath(surface.preMonthBtnPath,
		// surface.monthChangeBtnPaint);
		// canvas.drawPath(surface.nextMonthBtnPath,
		// surface.monthChangeBtnPaint);
		// 星期
		float weekTextY = surface.monthHeight + surface.weekHeight * 3 / 4f;
		// 星期背景
		// surface.cellBgPaint.setColor(surface.textColor);
		// canvas.drawRect(surface.weekHeight, surface.width,
		// surface.weekHeight, surface.width, surface.cellBgPaint);
		for (int i = 0; i < surface.weekText.length; i++) {
			float weekTextX = i
					* surface.cellWidth
					+ (surface.cellWidth - surface.weekPaint
							.measureText(surface.weekText[i])) / 2f;
			canvas.drawText(surface.weekText[i], weekTextX, weekTextY,
					surface.weekPaint);
		}

		// 计算日期
		calculateDate();
		// 按下状态，选择状态背景色
		drawDownOrSelectedBg(canvas);
		// write date number
		// today index
		int todayIndex = -1;
		calendar.setTime(curDate);
		String curYearAndMonth = calendar.get(Calendar.YEAR) + ""
				+ calendar.get(Calendar.MONTH);
		calendar.setTime(today);
		String todayYearAndMonth = calendar.get(Calendar.YEAR) + ""
				+ calendar.get(Calendar.MONTH);
		if (curYearAndMonth.equals(todayYearAndMonth)) {
			int todayNumber = calendar.get(Calendar.DAY_OF_MONTH);
			todayIndex = curStartIndex + todayNumber - 1;
		}
		for (int i = 0; i < 42; i++) {
			int color = surface.textColor;
			if (isLastMonth(i)) {
				color = surface.borderColor;
			} else if (isNextMonth(i)) {
				color = surface.borderColor;
			}
			if (todayIndex != -1 && i == todayIndex) {
				color = surface.todayNumberColor;
			}
			// getDateByIndex(i)
			Date d = getDateByIndex(i);
			int score = db.getPinganScore(spd.format(d));

			Object[] steps = db.getSpBasicData(new SimpleDateFormat(
					"yyyy-MM-dd").format(d));
			String s = null;
			if (steps != null && steps.length > 0) {
				s = steps[0] + "";
			}
			drawCellText(canvas, i, date[i] + "", color, score, s);
		}
		super.onDraw(canvas);
	}

	private void calculateDate() {
		calendar.setTime(curDate);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		int dayInWeek = calendar.get(Calendar.DAY_OF_WEEK);
		Log.d(TAG, "day in week:" + dayInWeek);
		int monthStart = dayInWeek;
		if (monthStart == 1) {
			monthStart = 8;
		}
		monthStart -= 1; // 以日为开头-1，以星期一为开头-2
		curStartIndex = monthStart;
		date[monthStart] = 1;
		// last month
		if (monthStart > 0) {
			calendar.set(Calendar.DAY_OF_MONTH, 0);
			int dayInmonth = calendar.get(Calendar.DAY_OF_MONTH);
			for (int i = monthStart - 1; i >= 0; i--) {
				date[i] = dayInmonth;
				dayInmonth--;
			}
			calendar.set(Calendar.DAY_OF_MONTH, date[0]);
		}
		showFirstDate = calendar.getTime();
		// this month
		calendar.setTime(curDate);
		calendar.add(Calendar.MONTH, 1);
		calendar.set(Calendar.DAY_OF_MONTH, 0);
		// Log.d(TAG, "m:" + calendar.get(Calendar.MONTH) + " d:" +
		// calendar.get(Calendar.DAY_OF_MONTH));
		int monthDay = calendar.get(Calendar.DAY_OF_MONTH);
		for (int i = 1; i < monthDay; i++) {
			date[monthStart + i] = i + 1;
		}
		curEndIndex = monthStart + monthDay;
		// next month
		for (int i = monthStart + monthDay; i < 42; i++) {
			date[i] = i - (monthStart + monthDay) + 1;
		}
		if (curEndIndex < 42) {
			// 显示了下一月的
			calendar.add(Calendar.DAY_OF_MONTH, 1);
		}
		calendar.set(Calendar.DAY_OF_MONTH, date[41]);
		showLastDate = calendar.getTime();
	}

	/**
	 * 
	 * @param canvas
	 * @param index
	 * @param text
	 */
	private void drawCellText(Canvas canvas, int index, String text, int color,
			int score, String steps) {
		int x = getXByIndex(index);
		int y = getYByIndex(index);
		surface.datePaint.setColor(0x60ffffff);
		surface.datePaint.setTextSize(surface.cellHeight * 0.2f);
		surface.datePaint.setTextAlign(Paint.Align.LEFT);
		float cellY = surface.monthHeight + surface.weekHeight + (y - 1)
				* surface.cellHeight + surface.cellHeight * 3 / 4f;
		float cellX = (surface.cellWidth * (x - 1))
				+ (surface.cellWidth - surface.datePaint.measureText(text))
				/ 2f;
		canvas.drawText(text, cellX - (surface.cellWidth * 0.3f), cellY
				+ (surface.cellHeight * 0.2f), surface.datePaint);

		surface.datePaint.setColor(0xffffff);
		surface.datePaint.setTextSize(surface.cellHeight * 0.25f);

		if (steps == null) {
			steps = "0";
			float left = surface.cellWidth * (x - 1) + 0.5f
					* (surface.cellWidth - surface.stepPaint.measureText("-"));

			canvas.drawText("0", left, cellY - 0.1f * surface.cellHeight,
					surface.stepPaint);
		} else {
			float left = surface.cellWidth
					* (x - 1)
					+ 0.5f
					* (surface.cellWidth - surface.stepPaint.measureText(steps
							+ ""));

			canvas.drawText(steps + "", left,
					cellY - 0.1f * surface.cellHeight, surface.stepPaint);
		}

		surface.datePaint.setColor(Color.WHITE);
		if (score == -1) {
			float left = surface.cellWidth * (x - 1) + 0.5f
					* (surface.cellWidth - surface.datePaint.measureText("-"));
			// float left = surface.cellWidth * (x+0.5f);
			canvas.drawText("-", left, cellY - 0.4f * surface.cellHeight,
					surface.datePaint);
			// canvas.drawText("-", left , cellY-0.4f*surface.cellHeight ,
			// surface.datePaint);
		} else {
			float left = surface.cellWidth
					* (x - 1)
					+ 0.5f
					* (surface.cellWidth - surface.datePaint.measureText(score
							+ ""));
			// float left =surface.cellWidth * (x+0.5f);
			canvas.drawText(score + "", left,
					cellY - 0.4f * surface.cellHeight, surface.datePaint);
			// canvas.drawText(score+"", left , cellY-0.4f*surface.cellHeight ,
			// surface.datePaint);
		}
		if (score <= 0 && Integer.valueOf(steps) >= 10000) {
			canvas.drawBitmap(refresh, cellX + (surface.cellWidth * 0.2f),
					cellY, surface.datePaint);
			// rect = new Rect(
			// (int) (cellX + (surface.cellWidth * 0.2f)),
			// (int) (cellY + (surface.cellHeight * 0.2f)),
			// (int) (cellX + (surface.cellWidth * 0.45f)),
			// (int) (cellY + (surface.cellHeight * 0.2f) + (surface.cellWidth *
			// 0.25f)));
			// canvas.drawBitmap(refresh, null, rect, surface.datePaint);
		}
	}

	/**
	 * 
	 * @param canvas
	 * @param index
	 * @param color
	 */
	private void drawCellBg(Canvas canvas, int index, int color) {
		int x = getXByIndex(index);
		int y = getYByIndex(index);
		surface.cellBgPaint.setColor(color);
		float left = surface.cellWidth * (x - 1) + surface.borderWidth;
		float top = surface.monthHeight + surface.weekHeight + (y - 1)
				* surface.cellHeight + surface.borderWidth;
		canvas.drawRect(left, top, left + surface.cellWidth
				- surface.borderWidth, top + surface.cellHeight
				- surface.borderWidth, surface.cellBgPaint);
	}

	private void drawDownOrSelectedBg(Canvas canvas) {
		// down and not up
		if (downDate != null) {
			drawCellBg(canvas, downIndex, surface.cellDownColor);
		}
		// selected bg color
		if (!selectedEndDate.before(showFirstDate)
				&& !selectedStartDate.after(showLastDate)) {
			int[] section = new int[] { -1, -1 };
			calendar.setTime(curDate);
			calendar.add(Calendar.MONTH, -1);
			findSelectedIndex(0, curStartIndex, calendar, section);
			if (section[1] == -1) {
				calendar.setTime(curDate);
				findSelectedIndex(curStartIndex, curEndIndex, calendar, section);
			}
			if (section[1] == -1) {
				calendar.setTime(curDate);
				calendar.add(Calendar.MONTH, 1);
				findSelectedIndex(curEndIndex, 42, calendar, section);
			}
			if (section[0] == -1) {
				section[0] = 0;
			}
			if (section[1] == -1) {
				section[1] = 0 ;
			}
			for (int i = section[0]; i <= section[1]; i++) {
				drawCellBg(canvas, i, surface.cellSelectedColor);
			}
		}
	}

	private void findSelectedIndex(int startIndex, int endIndex,
			Calendar calendar, int[] section) {
		for (int i = startIndex; i < endIndex; i++) {
			calendar.set(Calendar.DAY_OF_MONTH, date[i]);
			Date temp = calendar.getTime();
			// Log.d(TAG, "temp:" + temp.toLocaleString());
			if (temp.compareTo(selectedStartDate) == 0) {
				section[0] = i;
			}
			if (temp.compareTo(selectedEndDate) == 0) {
				section[1] = i;
				return;
			}
		}
	}

	public Date getSelectedStartDate() {
		return selectedStartDate;
	}

	public Date getSelectedEndDate() {
		return selectedEndDate;
	}

	private boolean isLastMonth(int i) {
		if (i < curStartIndex) {
			return true;
		}
		return false;
	}

	private boolean isNextMonth(int i) {
		if (i >= curEndIndex) {
			return true;
		}
		return false;
	}

	private int getXByIndex(int i) {
		return i % 7 + 1; // 1 2 3 4 5 6 7
	}

	private int getYByIndex(int i) {
		return i / 7 + 1; // 1 2 3 4 5 6
	}

	// 获得当前应该显示的年月
	public String getYearAndmonth() {
		calendar.setTime(curDate);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		return year + "-" + surface.monthText[month];
	}

	// 上一月
	public String clickLeftMonth() {
		calendar.setTime(curDate);
		calendar.add(Calendar.MONTH, -1);
		curDate = calendar.getTime();
		invalidate();
		return getYearAndmonth();
	}

	public String changeDate(Calendar calendar) {

		curDate = calendar.getTime();
		invalidate();
		return getYearAndmonth();
	}

	// 下一月
	public String clickRightMonth() {
		calendar.setTime(curDate);
		calendar.add(Calendar.MONTH, 1);
		curDate = calendar.getTime();
		invalidate();
		return getYearAndmonth();
	}

	private void setSelectedDateByCoor(float x, float y) {
		// change month
		// if (y < surface.monthHeight) {
		// // pre month
		// if (x < surface.monthChangeWidth) {
		// calendar.setTime(curDate);
		// calendar.add(Calendar.MONTH, -1);
		// curDate = calendar.getTime();
		// }
		// // next month
		// else if (x > surface.width - surface.monthChangeWidth) {
		// calendar.setTime(curDate);
		// calendar.add(Calendar.MONTH, 1);
		// curDate = calendar.getTime();
		// }
		// }
		// cell click down
		if (y > surface.monthHeight + surface.weekHeight) {
			int m = (int) (Math.floor(x / surface.cellWidth) + 1);
			int n = (int) (Math
					.floor((y - (surface.monthHeight + surface.weekHeight))
							/ Float.valueOf(surface.cellHeight)) + 1);
			downIndex = (n - 1) * 7 + m - 1;
			Log.d(TAG, "downIndex:" + downIndex);
			calendar.setTime(curDate);
			if (isLastMonth(downIndex)) {
				calendar.add(Calendar.MONTH, -1);
			} else if (isNextMonth(downIndex)) {
				calendar.add(Calendar.MONTH, 1);
			}
			calendar.set(Calendar.DAY_OF_MONTH, date[downIndex]);
			downDate = calendar.getTime();
		}
		invalidate();
	}

	private Date getDateByIndex(int index) {
		calendar.setTime(curDate);
		if (isLastMonth(index)) {
			calendar.add(Calendar.MONTH, -1);
		} else if (isNextMonth(index)) {
			calendar.add(Calendar.MONTH, 1);
		}
		calendar.set(Calendar.DAY_OF_MONTH, date[index]);
		return calendar.getTime();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			try {
				setSelectedDateByCoor(event.getX(), event.getY());
			} catch (Exception e) {
				// TODO Auto-generated catch block
			}
			break;
		case MotionEvent.ACTION_UP:
			if (downDate != null) {
				// if (!completed) {
				// if (downDate.before(selectedStartDate)) {
				// selectedEndDate = selectedStartDate;
				// selectedStartDate = downDate;
				// } else {
				// selectedEndDate = downDate;
				// }
				// completed = true;
				// } else {
				// selectedStartDate = selectedEndDate = downDate;
				// completed = false;
				// }
				selectedStartDate = selectedEndDate = downDate;
				if (onItemClickListener != null) {
					onItemClickListener.OnItemClick(selectedStartDate);
				}
				// 响应监听事件

				// Log.d(TAG, "downdate:" + downDate.toLocaleString());
				// Log.d(TAG, "start:" + selectedStartDate.toLocaleString());
				// Log.d(TAG, "end:" + selectedEndDate.toLocaleString());
				downDate = null;
				invalidate();
			}
			break;
		}
		return true;
	}

	// 给控件设置监听事件
	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
		this.onItemClickListener = onItemClickListener;
	}

	// 监听接口
	public interface OnItemClickListener {
		void OnItemClick(Date date);
	}

	/**
	 * 
	 * 1. 布局尺寸 2. 文字颜色，大小 3. 当前日期的颜色，选择的日期颜色
	 */
	private class Surface {
		public float density;
		public int width; // 整个控件的宽度
		public int height; // 整个控件的高度
		public float monthHeight; // 显示月的高度
		// public float monthChangeWidth; // 上一月、下一月按钮宽度
		public float weekHeight; // 显示星期的高度
		public float cellWidth; // 日期方框宽度
		public float cellHeight; // 日期方框高度
		public float borderWidth;
		public int bgColor = Color.parseColor("#00ffffff");
		private int textColor = Color.WHITE;
		// private int textColorUnimportant = Color.parseColor("#666666");
		private int btnColor = Color.parseColor("#666666");
		private int borderColor = Color.parseColor("#88ffffff");
		public int todayNumberColor = Color.RED;
		public int cellDownColor = Color.parseColor("#00CCFFFF");
		public int cellSelectedColor = Color.parseColor("#44000000");
		public Paint borderPaint;
		public Paint monthPaint;
		public Paint weekPaint;
		public Paint datePaint, stepPaint;
		public Paint monthChangeBtnPaint;
		public Paint cellBgPaint;
		public Path boxPath; // 边框路径
		// public Path preMonthBtnPath; // 上一月按钮三角形
		// public Path nextMonthBtnPath; // 下一月按钮三角形
		public String[] weekText;
		public String[] monthText;

		public void init() {
			weekText = getResources().getStringArray(R.array.weekscore);
			monthText = getResources().getStringArray(R.array.mounth);
			float temp = height / 7f;
			monthHeight = 0;// (float) ((temp + temp * 0.3f) * 0.6);
			// monthChangeWidth = monthHeight * 1.5f;
			weekHeight = temp * 0.65f;
			cellHeight = temp;
			cellWidth = width / 7f;
			borderPaint = new Paint();
			borderPaint.setColor(borderColor);
			borderPaint.setStyle(Paint.Style.STROKE);
			borderWidth = (float) (0.5 * density);
			// Log.d(TAG, "borderwidth:" + borderWidth);
			borderWidth = borderWidth < 1 ? 1 : borderWidth;
			borderPaint.setStrokeWidth(borderWidth);
			monthPaint = new Paint();
			monthPaint.setColor(textColor);
			monthPaint.setAntiAlias(true);
			float textSize = cellHeight * 0.4f;
			Log.d(TAG, "text size:" + textSize);
			monthPaint.setTextSize(textSize);
			monthPaint.setTypeface(Typeface.DEFAULT_BOLD);
			weekPaint = new Paint();
			weekPaint.setColor(textColor);
			weekPaint.setAntiAlias(true);
			float weekTextSize = weekHeight * 0.35f;
			weekPaint.setTextSize(weekTextSize);
			weekPaint.setTypeface(Typeface.DEFAULT_BOLD);
			datePaint = new Paint();
			datePaint.setTypeface(MilinkApplication.NumberFace);
			datePaint.setColor(textColor);
			datePaint.setAntiAlias(true);
			stepPaint = new Paint(datePaint);
			stepPaint.setColor(0xb2ffffff);
			stepPaint.setTextSize(cellHeight * 0.2f);
			float cellTextSize = cellHeight * 0.1f;
			datePaint.setTextSize(cellTextSize);
			// datePaint.setTypeface(Typeface.DEFAULT_BOLD);
			boxPath = new Path();
			// boxPath.addRect(0, 0, width, height, Direction.CW);
			// boxPath.moveTo(0, monthHeight);
			// boxPath.rLineTo(width, 0);
			boxPath.moveTo(0, monthHeight + weekHeight);
			boxPath.rLineTo(width, 0);
			for (int i = 1; i < 7; i++) {
				boxPath.moveTo(0, monthHeight + weekHeight + i * cellHeight);
				boxPath.rLineTo(width, 0);
				boxPath.moveTo(i * cellWidth, monthHeight + weekHeight);
				boxPath.rLineTo(0, height - monthHeight - cellHeight);
			}
			boxPath.moveTo(6 * cellWidth, monthHeight + weekHeight);
			boxPath.rLineTo(0, height - monthHeight - cellHeight);
			// preMonthBtnPath = new Path();
			// int btnHeight = (int) (monthHeight * 0.6f);
			// preMonthBtnPath.moveTo(monthChangeWidth / 2f, monthHeight / 2f);
			// preMonthBtnPath.rLineTo(btnHeight / 2f, -btnHeight / 2f);
			// preMonthBtnPath.rLineTo(0, btnHeight);
			// preMonthBtnPath.close();
			// nextMonthBtnPath = new Path();
			// nextMonthBtnPath.moveTo(width - monthChangeWidth / 2f,
			// monthHeight / 2f);
			// nextMonthBtnPath.rLineTo(-btnHeight / 2f, -btnHeight / 2f);
			// nextMonthBtnPath.rLineTo(0, btnHeight);
			// nextMonthBtnPath.close();
			monthChangeBtnPaint = new Paint();
			monthChangeBtnPaint.setAntiAlias(true);
			monthChangeBtnPaint.setStyle(Paint.Style.FILL_AND_STROKE);
			monthChangeBtnPaint.setColor(btnColor);
			cellBgPaint = new Paint();
			cellBgPaint.setAntiAlias(true);
			cellBgPaint.setStyle(Paint.Style.FILL);
			cellBgPaint.setColor(cellSelectedColor);
		}
	}

	public int Dp2Px(Context context, float dp) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dp * scale + 0.5f);
	}
}
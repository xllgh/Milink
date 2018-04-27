/*
 * @author http://blog.csdn.net/singwhatiwanna
 */
package com.bandlink.air.gps;

import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.LinearLayout;

import com.bandlink.air.R;

/**
 * 这是个选项卡式的控件，会随着viewpager的滑动而滑动
 */
@SuppressWarnings("static-access")
public class TitleIndicator extends LinearLayout implements
		View.OnClickListener, OnFocusChangeListener {
	@SuppressWarnings("unused")
	private boolean DEBUG = false;

	@SuppressWarnings("unused")
	private static final String TAG = "TitleFlowIndicator";

	private static final float FOOTER_LINE_HEIGHT = 4.0f;

	private static final int FOOTER_COLOR = 0xFFFFC445;

	private static final float FOOTER_TRIANGLE_HEIGHT = 10;

	private int mCurrentScroll = 0;

	// 选项卡列表
	private List<TabInfo> mTabs;

	// 选项卡所依赖的viewpager
	private ViewPager mViewPager;

	private Path mPath = new Path();

	private Paint mPaintFooterLine;

	private Paint mPaintFooterTriangle;

	private float mFooterTriangleHeight;

	// 滚动条的高度
	private float mFooterLineHeight;

	// 当前选项卡的下标，从0开始
	private int mSelectedTab = 0;

	private Context mContext;

	private final int BSSEEID = 0xffff00;;

	private boolean mChangeOnClick = true;

	private int mCurrID = 0;

	// 单个选项卡的宽度
	private int mPerItemWidth = 0;

	// 表示选项卡总共有几个
	private int mTotal = 0;

	private LayoutInflater mInflater;

	/**
	 * Default constructor
	 */
	public TitleIndicator(Context context) {
		super(context);
		initDraw(FOOTER_LINE_HEIGHT, FOOTER_COLOR);
	}

	/**
	 * The contructor used with an inflater
	 * 
	 * @param context
	 * @param attrs
	 */
	public TitleIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		setFocusable(true);
		setOnFocusChangeListener(this);
		mContext = context;
		// Retrieve styles attributs
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.TitleIndicator);
		// Retrieve the colors to be used for this view and apply them.
		int footerColor = a.getColor(R.styleable.TitleIndicator_footerColor,
				FOOTER_COLOR);
		mFooterLineHeight = a
				.getDimension(R.styleable.TitleIndicator_footerLineHeight,
						FOOTER_LINE_HEIGHT);
		mFooterTriangleHeight = a.getDimension(
				R.styleable.TitleIndicator_footerTriangleHeight,
				FOOTER_TRIANGLE_HEIGHT);
		initDraw(mFooterLineHeight, footerColor);
		a.recycle();
	}

	/**
	 * Initialize draw objects
	 */
	private void initDraw(float footerLineHeight, int footerColor) {
		mPaintFooterLine = new Paint();
		mPaintFooterLine.setStyle(Paint.Style.FILL_AND_STROKE);
		mPaintFooterLine.setStrokeWidth(footerLineHeight);
		mPaintFooterLine.setColor(footerColor);
		mPaintFooterTriangle = new Paint();
		mPaintFooterTriangle.setStyle(Paint.Style.FILL_AND_STROKE);
		mPaintFooterTriangle.setColor(footerColor);
		mInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	/*
	 * @这个是核心函数，选项卡是用canvas画出来的。所有的invalidate方法均会触发onDraw
	 * 大意是这样的：当页面滚动的时候，会有一个滚动距离，然后onDraw被触发后， 就会在新位置重新画上滚动条（其实就是画线）
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// 下面是计算本次滑动的距离
		float scroll_x = 0;
		if (mTotal != 0) {
			mPerItemWidth = getWidth() / mTotal;
			int tabID = mSelectedTab;
			scroll_x = (mCurrentScroll - ((tabID) * (getWidth() + mViewPager
					.getPageMargin()))) / mTotal;
		} else {
			mPerItemWidth = getWidth();
			scroll_x = mCurrentScroll;
		}
		// 下面就是如何画线了
		Path path = mPath;
		path.rewind();
		float offset = 0;
		float left_x = mSelectedTab * mPerItemWidth + offset + scroll_x;
		float right_x = (mSelectedTab + 1) * mPerItemWidth - offset + scroll_x;
		float top_y =0;
		float bottom_y =mFooterLineHeight;

		path.moveTo(left_x, top_y + 1f);
		path.lineTo(right_x, top_y + 1f);
		path.lineTo(right_x, bottom_y + 1f);
		path.lineTo(left_x, bottom_y + 1f);
		path.close();
		canvas.drawPath(path, mPaintFooterTriangle);
	}

	// 当页面滚动的时候，重新绘制滚动条
	public void onScrolled(int h) {
		mCurrentScroll = h;
		invalidate();
	}

	// 当页面切换的时候，重新绘制滚动条
	public synchronized void onSwitched(int position) {
		if (mSelectedTab == position) {
			return;
		}
		setCurrentTab(position);
		invalidate();
	}

	// 初始化选项卡
	public void init(int startPos, List<TabInfo> tabs, ViewPager mViewPager) {
		removeAllViews();
		this.mViewPager = mViewPager;
		this.mTabs = tabs;
		this.mTotal = tabs.size();
		setCurrentTab(startPos);
		invalidate();
	}

	

	public void setDisplayedPage(int index) {
		mSelectedTab = index;
	}

	public void setChangeOnClick(boolean changeOnClick) {
		mChangeOnClick = changeOnClick;
	}

	public boolean getChangeOnClick() {
		return mChangeOnClick;
	}

	@Override
	public void onClick(View v) {
		int position = v.getId() - BSSEEID;
		setCurrentTab(position);
	}

	public int getTabCount() {
		int children = getChildCount();
		return children;
	}

	// 设置当前选项卡
	public synchronized void setCurrentTab(int index) {
		if (index < 0 || index >= getTabCount()) {
			return;
		}
		View oldTab = getChildAt(mSelectedTab);
		oldTab.setSelected(false);
		mSelectedTab = index;
		View newTab = getChildAt(mSelectedTab);
		newTab.setSelected(true);
		mViewPager.setCurrentItem(mSelectedTab);
		invalidate();
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (v == this && hasFocus && getTabCount() > 0) {
			getChildAt(mSelectedTab).requestFocus();
			return;
		}

		if (hasFocus) {
			int i = 0;
			int numTabs = getTabCount();
			while (i < numTabs) {
				if (getChildAt(i) == v) {
					setCurrentTab(i);
					break;
				}
				i++;
			}
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (mCurrentScroll == 0 && mSelectedTab != 0) {
			mCurrentScroll = (getWidth() + mViewPager.getPageMargin())
					* mSelectedTab;
		}
	}
}

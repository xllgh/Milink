package com.bandlink.air.view;

import java.lang.reflect.Field;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.GridView;

/***
 * 在api8不支持getNumColumns
 * 
 * @author Kevin
 *
 */
public class MGridView extends GridView {

	public MGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getNumColumns() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			return super.getNumColumns();
		} else {
			try {
				Field numColumns = getClass().getSuperclass().getDeclaredField(
						"mNumColumns");
				numColumns.setAccessible(true);
				return numColumns.getInt(this);
			} catch (Exception e) {
				return 1;
			}
		}
	}

}

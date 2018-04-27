package com.bandlink.air.simple;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bandlink.air.R;

public class TimeLineAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<HashMap<String, Object>> data;
	private HashMap<Integer, SoftReference<View>> views;
	private Typeface tf;
	private int[] detail;
	public TimeLineAdapter(Context context,
			ArrayList<HashMap<String, Object>> data,int[] detail) {
		super();
		this.context = context;
		this.data = data;
		this.detail = detail;
		views = new HashMap<Integer, SoftReference<View>>();
		tf = Typeface.createFromAsset(context.getAssets(), "font/AvenirLTStd-Light.otf");
	}

	@Override
	public void notifyDataSetChanged() {
		// TODO Auto-generated method stub
		views.clear();
		super.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
	 final ViewHolder viewholder;
		if (convertView==null || true) {
			if (position % 2 == 0) {
				convertView = LayoutInflater.from(context).inflate(
						R.layout.report_item_left, null);
			} else {
				convertView = LayoutInflater.from(context).inflate(
						R.layout.report_item_right, null);
			}
			viewholder = new ViewHolder();
			viewholder.cal = (TextView)convertView.findViewById(R.id.cal);
			viewholder.date = (TextView)convertView.findViewById(R.id.date);
			viewholder.detail = (TextView)convertView.findViewById(R.id.detail_rank);
			viewholder.name = (TextView)convertView.findViewById(R.id.name_rank);
			viewholder.dis = (TextView)convertView.findViewById(R.id.dis);
			viewholder.ico = (ImageView)convertView.findViewById(R.id.ic_rank);
			convertView.setTag(viewholder);
		} else {
			viewholder = (ViewHolder) convertView.getTag();
		}
		 
		final HashMap<String, Object> item = data.get(position);
		if (item.get("icon") instanceof Integer) {
			viewholder.ico.setImageResource(Integer.valueOf(item.get("icon").toString()));
		} else {
			viewholder.ico.setImageResource(R.drawable.tar_middle_dark);
		}
		viewholder.name.setTypeface(tf);
		viewholder.date.setTypeface(tf);
		viewholder.detail.setTypeface(tf);
		viewholder.cal.setTypeface(tf);
		viewholder.dis.setTypeface(tf);
//		if(item.containsKey("flag")){
//			int color = Color.parseColor("#ff6000");
//			viewholder.name.setTextColor(color);
//			viewholder.date.setTextColor(color);
//			viewholder.detail.setTextColor(color);
//			viewholder.cal.setTextColor(color);
//			viewholder.dis.setTextColor(color);
//		}else{
//			int color = Color.parseColor("#656561");
//			viewholder.name.setTextColor(color);
//			viewholder.date.setTextColor(color);
//			viewholder.detail.setTextColor(color);
//			viewholder.cal.setTextColor(color);
//			viewholder.dis.setTextColor(color);
//		}
		viewholder.name.setText(item.get("name").toString());
		viewholder.date.setText(item.get("date").toString());
		viewholder.detail.setText(item.get("detail").toString()+context.getString(R.string.unit_step));
		viewholder.cal.setText((String.format(
				"%.1f",
				Integer.valueOf(item.get("detail").toString()) * 0.03333f))
				+ "kcal");
		if(item.containsKey("flag")){
			viewholder.dis.setText((item.get("flag").toString()));
		}
		
		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (viewholder.cal.getVisibility() == View.GONE) {
					viewholder.cal.setVisibility(View.VISIBLE);
					viewholder.dis.setVisibility(View.VISIBLE);
				} else {
					viewholder.cal.setVisibility(View.GONE);
					viewholder.dis.setVisibility(View.GONE);
				}  
			}
		});
		 
		return convertView;
	}
	class ViewHolder{
		TextView name, detail,date,cal,dis;
		ImageView ico;
	}

}
package com.bandlink.air;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.SharePreUtils;

public class MallActivity extends LovefitActivity {
	private ListView listview;
	SharedPreferences share;
	private BaseAdapter mAdapter;
	private LayoutInflater inflater;
	private ArrayList<HashMap<String, Object>> list;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mall);
		ActionbarSettings actionbar = new ActionbarSettings(this,
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						MallActivity.this.finish();
					}
				}, null);
		actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
		actionbar.setTitle(R.string.ped_mall);
		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		share = getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);

		// if (share.getInt("ISMEMBER", 0) == 0) {
		// d = new NoRegisterDialog(this, R.string.no_register,
		// R.string.no_register_content);
		// d.show();
		// }
		listLayout();
	}

	public void listLayout() {
		listview = (ListView) findViewById(R.id.set_listView1);
		list = new ArrayList<HashMap<String, Object>>();

		HashMap<String, Object> map = new HashMap<String, Object>();

		map = new HashMap<String, Object>();
		map.put("proImage", R.drawable.setting_flame);
		map.put("proName", getString(R.string.pro_flame_pedometer));
		map.put("proContent", getString(R.string.pro_flame_content));
		list.add(map);

		map = new HashMap<String, Object>();
		map.put("proImage", R.drawable.setting_ant);
		map.put("proName", getString(R.string.pro_ant_pedometer));
		map.put("proContent", getString(R.string.pro_ant_content));
		list.add(map);

		map = new HashMap<String, Object>();
		map.put("proImage", R.drawable.mall_color);
		map.put("proName", getString(R.string.pro_color_Scale));
		map.put("proContent", getString(R.string.pro_color_content));
		//list.add(map);

		map = new HashMap<String, Object>();
		map.put("proImage", R.drawable.mall_magic);
		map.put("proName", getString(R.string.pro_magic_Scale));
		map.put("proContent", getString(R.string.pro_magic_content));
		list.add(map);

		mAdapter = new MyAdapter();
		listview.setAdapter(mAdapter);

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		// MobclickAgent.onPause(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// MobclickAgent.onResume(this);
	}

	public void openURL(int positon) {

		Uri content_url = Uri
				.parse("http://zkmldq.tmall.com/shop/view_shop.htm?spm=a220o.1000855.0.28.D0r2KP");
		switch (positon) {
		case 2:
			content_url = Uri
					.parse("http://detail.tmall.com/item.htm?spm=a1z10.5.w4011-3250786507.37.72R9En&id=23020464424&rn=14029a1b05fac3a1963313bcab5867ed");
			break;

		case 3:
			content_url = Uri
					.parse("http://detail.tmall.com/item.htm?spm=a1z10.1.w5003-3481694802.2.yQVuKX&id=19286762025&scene=taobao_shop");
			break;
		case 0:
			content_url = Uri
					.parse("http://detail.tmall.com/item.htm?spm=a1z10.1.w5003-3481694802.3.yQVuKX&id=19186667191&scene=taobao_shop");
			break;
		case 1:
			content_url = Uri
					.parse("http://detail.tmall.com/item.htm?spm=a1z10.1.w5003-3481694802.4.yQVuKX&id=19185651234&scene=taobao_shop");
			break;

		}
		Intent intent = new Intent(Intent.ACTION_VIEW, content_url);
		// Create and start the chooser
		Intent chooser = Intent.createChooser(intent,
				getString(R.string.chose_browse));
		startActivity(chooser);

	}

	public class MyAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			final int mposition = position;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.listview_item_mall,
						parent, false);
				holder = new ViewHolder();
				convertView.setTag(holder);
				holder.name = (TextView) convertView.findViewById(R.id.proname);
				holder.content = (TextView) convertView
						.findViewById(R.id.procontent);
				holder.image = (ImageView) convertView.findViewById(R.id.img);
				holder.btn = (Button) convertView.findViewById(R.id.open);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.name.setText((String) list.get(position).get("proName"));
			holder.content.setText((String) list.get(position)
					.get("proContent"));
			holder.image.setImageResource((Integer) list.get(position).get(
					"proImage"));

			holder.btn.setOnClickListener(new Button.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					openURL(mposition);

				}
			});

			return convertView;
		}

	}

	private static final class ViewHolder {
		TextView name;
		TextView content;
		ImageView image;
		Button btn;
	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub

	}

}

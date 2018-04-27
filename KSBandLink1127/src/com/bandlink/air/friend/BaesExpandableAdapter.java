package com.bandlink.air.friend;

import java.net.URL;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.bandlink.air.R;
import com.bandlink.air.club.BorderImageView;

public class BaesExpandableAdapter extends BaseExpandableListAdapter {

	private List<String> parentList;
	private Map<String, List<User>> childList;
	private Context context;
	private OnListListener listener;

	public BaesExpandableAdapter(Context context, List<String> parentList,
			Map<String, List<User>> childList) {
		this.context = context;
		this.childList = childList;
		this.parentList = parentList;

	}

	public void setOnListListener(OnListListener listener) {
		this.listener = listener;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return childList.get(parentList.get(groupPosition));
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		convertView = LayoutInflater.from(context).inflate(R.layout.childitem,
				null);
		FollowListView gridchild = (FollowListView) convertView
				.findViewById(R.id.GridView_toolbar);
		final List<User> value = childList.get(parentList.get(groupPosition));
		ChildGridView gridview = new ChildGridView(context, value);
		gridchild.setAdapter(gridview);
		gridchild.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				if (listener != null) {
					listener.onListItemClick(value.get(arg2).name,
							value.get(arg2).uid, arg2, arg1, value.get(arg2));
				}
			}
		});
		gridchild.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub

				if (listener != null) {
					// listener.onListItemLongClick(value.get(arg2).name,value.get(arg2).uid,
					// arg2);
				}
				return false;
			}
		});
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		// return childList.get(parentList.get(groupPosition)).size();
		return 1;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return parentList.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return parentList.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		convertView = LayoutInflater.from(context).inflate(R.layout.parentitem,
				null);
		TextView textview = (TextView) convertView
				.findViewById(R.id.text_parent);
		textview.setText(parentList.get(groupPosition));
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	class ChildGridView extends BaseAdapter {
		private Context context;
		private List<User> value;

		ChildGridView(Context context, List<User> value) {
			this.context = context;
			this.value = value;

		}

		@Override
		public int getCount() {
			return value.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			final ViewHolder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(context).inflate(
						R.layout.gradchilditem, null);
				holder = new ViewHolder();
				holder.tv = (TextView) convertView.findViewById(R.id.textchild);
				holder.iv = (BorderImageView) convertView
						.findViewById(R.id.image);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.tv.setText(value.get(position).name);
			final Handler han = new Handler() {

				@Override
				public void handleMessage(Message msg) {
					// TODO Auto-generated method stub
					if (msg.obj != null) {
						Bitmap bit = (Bitmap) msg.obj;
						holder.iv.setImageBitmap(bit);
					} else {
						holder.iv.setImageResource(R.drawable.avatar);
					}
					super.handleMessage(msg);
				}

			};
			new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						URL url = new URL(value.get(position).photo.replace(
								"small", "middle"));
						System.out.println(value.get(position).photo);
						Bitmap bit = BitmapFactory.decodeStream(url
								.openStream());
						han.obtainMessage(0, bit).sendToTarget();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						han.obtainMessage(0, null).sendToTarget();
						e.printStackTrace(); 
					}
				}
			}).start();
			return convertView;
		}
	}

	class ViewHolder {
		BorderImageView iv;
		TextView tv;
	}

	public interface OnListListener {

		/***
		 * 点击子项时回调
		 * 
		 * @param name
		 *            用户昵称
		 * @param uid
		 *            用户id
		 * @param indexInList
		 *            索引
		 * @param v
		 *            点击view
		 * @param user
		 *            user对象
		 */
		void onListItemClick(String name, int uid, int indexInList, View v,
				User user);

		/***
		 * 长按回调
		 * 
		 * @param uid
		 *            uid
		 * @param indexInList
		 *            index
		 * @param v
		 *            点击的View
		 */
		void onListItemLongClick(String name, int uid, int indexInList, View v);
	}
}

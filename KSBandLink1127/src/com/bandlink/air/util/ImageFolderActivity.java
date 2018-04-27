package com.bandlink.air.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bandlink.air.CreateMatchStep2;
import com.bandlink.air.R;
import com.bandlink.air.ShareEditActivity;

public class ImageFolderActivity extends LovefitActivity {
	LinkedList<ImageFolderInfo> list;
	private String flag = "";
	private static final String[] STORE_IMAGES = {
			MediaStore.Images.Media.DISPLAY_NAME, // imagename
			MediaStore.Images.Media.DATA, // imagepath
			MediaStore.Images.Media.LONGITUDE, MediaStore.Images.Media._ID, // imageid
			MediaStore.Images.Media.BUCKET_ID, // folder_id
			MediaStore.Images.Media.BUCKET_DISPLAY_NAME // foldername

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.item_album_gridview);
		GridView gv = (GridView) findViewById(R.id.gridview521);
		try {
			flag = getIntent().getStringExtra("flag");
		} catch (Exception e) {
			e.printStackTrace();
		}
		list = loadImageFromProvider();
		if (list == null) {
			ImageView i = new ImageView(this);
			i.setBackgroundResource(R.drawable.no_media);
			setContentView(i);
			return;
		}
		ImAdapter im = new ImAdapter(this, list);
		gv.setAdapter(im);
		gv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(ImageFolderActivity.this,
						ImageGalleryActivity.class);
				intent.putStringArrayListExtra("data",
						list.get(arg2).filePathes);
				intent.putExtra("flag", flag);
				startActivityForResult(intent, 1);
			}
		});
		ActionbarSettings actionbar = new ActionbarSettings(this,
				new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						ImageFolderActivity.this.finish();
					}
				}, null);

		actionbar.setTitle(R.string.pic);
		actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
	}

	/***
	 * 灏嗙郴缁熸彁渚涚殑鐩稿唽cursor 杞负 list 渚夸簬鎿嶄綔
	 * 
	 * @return LinkedList<ImageFolderInfo> 鐩稿唽鏂囦欢澶逛俊鎭殑闆嗗悎
	 */
	public LinkedList<ImageFolderInfo> loadImageFromProvider() {
		Cursor cursor = MediaStore.Images.Media.query(getContentResolver(),
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, STORE_IMAGES);
		List<String> listFolder = new ArrayList<String>();
		LinkedList<ImageFolderInfo> listFolderInfo = new LinkedList<ImageFolderInfo>();
		if (cursor == null || cursor.getCount() < 1) {
			return null;
		}
		cursor.moveToFirst();
		do {
			if (!listFolder.contains(cursor.getString(4))) {
				listFolder.add(cursor.getString(4));
				ImageFolderInfo i = new ImageFolderInfo();
				i.image = cursor.getString(1);

				i.path = cursor.getString(1).substring(0,
						cursor.getString(1).lastIndexOf("/"));
				i.name = cursor.getString(5);
				listFolderInfo.add(i);
			}
		} while (cursor.moveToNext());
		for (int i = 0; i < listFolder.size(); i++) {
			ArrayList<String> filePathes = new ArrayList<String>();
			cursor.moveToFirst();
			do {

				if (listFolder.get(i).equals(cursor.getString(4))) {
					filePathes.add(cursor.getString(1));

				}
			} while (cursor.moveToNext());
			listFolderInfo.get(i).filePathes = filePathes;
			listFolderInfo.get(i).pisNum = filePathes.size();

		}
		cursor.close();
		return listFolderInfo;
	}

	class ImAdapter extends BaseAdapter {

		LinkedList<ImageFolderInfo> list;
		Context context;

		public ImAdapter(Context context, LinkedList<ImageFolderInfo> list) {
			super();
			this.list = list;
			this.context = context;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if (convertView == null) {
				convertView = LayoutInflater.from(context).inflate(
						R.layout.album_gridview, null);

			}
			ImageView img = (ImageView) ViewHolder.get(convertView,
					R.id.display);
			TextView name = (TextView) ViewHolder.get(convertView,
					R.id.foldername);
			TextView num = (TextView) ViewHolder.get(convertView,
					R.id.foldernum);
			img.setImageBitmap(Util.getZoomBitmap(list.get(position).image));
			name.setText(list.get(position).name);
			num.setText(list.get(position).pisNum + "");
			return convertView;
		}

	}

	public static class ImageFolderInfo {
		public String path; // 鏂囦欢澶硅矾寰�
		public int pisNum = 0; // 鏂囦欢澶瑰唴鏁伴噺
		public ArrayList<String> filePathes = new ArrayList<String>(); // 鏂囦欢澶瑰唴鍥剧墖璺緞
		public String image; // 浣滀负鏂囦欢澶瑰浘鏍囩殑鍥剧墖璺緞
		public String name; // 鏂囦欢澶瑰悕绉�

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == 1 && resultCode == RESULT_OK) {
			String[] path = data.getExtras().getStringArray("all_path");
			String str ="";
			try {
				str = data.getExtras().getString("flag");
			} catch (Exception e) {
				str = "";
			}
			if (path.length > 0) {
				Intent intent;
				if (str!=null&&str.length() > 1) {
					intent = new Intent(ImageFolderActivity.this,
							CreateMatchStep2.class);
				} else {
					intent = new Intent(ImageFolderActivity.this,
							ShareEditActivity.class);
				}
				intent.putExtra("all_path", path);
				setResult(RESULT_OK, intent);
				ImageFolderActivity.this.finish();

			}
		}
		super.onActivityResult(requestCode, resultCode, data);

	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub
		
	}

}

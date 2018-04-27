package com.bandlink.air.util;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class ImageEntity implements Parcelable {

	private Bitmap contentPicture;

	public ImageEntity() {

	}

	public Bitmap getContentPicture() {
		return contentPicture;
	}

	public void setContentPicture(Bitmap contentPicture) {
		this.contentPicture = contentPicture;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub

		if (this.contentPicture != null) {
			dest.writeInt(1);
			this.contentPicture.writeToParcel(dest, flags);
		} else {
			dest.writeInt(0);
		}
	}

	public static final Parcelable.Creator<ImageEntity> CREATOR = new Parcelable.Creator<ImageEntity>() {
		@Override
		public ImageEntity createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			ImageEntity weiBoWidget = new ImageEntity();

			if (0 != source.readInt()) {
				weiBoWidget.contentPicture = Bitmap.CREATOR
						.createFromParcel(source);
				// 因为Bitmap实现了Parcelable接口，所以这里可以这样使用
			}
			return weiBoWidget;
		}

		@Override
		public ImageEntity[] newArray(int size) {
			// TODO Auto-generated method stub
			return new ImageEntity[size];
		}
	};
}

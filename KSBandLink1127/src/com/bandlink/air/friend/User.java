package com.bandlink.air.friend;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable{

	public String name;
	public String photo;
	public int uid;
	public String notes;
	public int gid;
	public static final Parcelable.Creator<User> CREATOR = new Creator<User>(){

		@Override
		public User createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			return new User( source.readInt(),source.readString(),source.readString() );
		}

		@Override
		public User[] newArray(int size) {
			// TODO Auto-generated method stub
			return new User[size];
		}};
/***
 * 
 * @param name 用户名
 * @param photo 头像
 * @param uid	编号
 */
	public User( int uid,String name, String photo) {
		super();
		this.name = name;
		this.photo = photo;
		this.uid = uid;
	}
	public User( int uid,String name, String photo,int gid) {
		super();
		this.name = name;
		this.photo = photo;
		this.uid = uid;
		this.gid =gid;
	}
	public User( int uid,String name, String photo,String notes) {
		super();
		this.name = name;
		this.photo = photo;
		this.uid = uid;
		this.notes=notes;
	}
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeInt(uid);
		dest.writeString(name);
		dest.writeString(photo); 
	}
	

}

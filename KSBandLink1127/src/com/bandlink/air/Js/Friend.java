package com.bandlink.air.Js;

import android.content.Context;

import com.bandlink.air.Friendpage;

public class Friend {
	private Context context;
	Friendpage friend;

	public Friend(Friendpage friend) {
		super();	
		this.friend = friend;
	}

	public void diggClick(String feed_id) {
		friend.diggClick(feed_id);
	}
		
	// 分享
	public void showShare(String feedid, String content, String[] urlImage) {
		friend.showShare(feedid, content, urlImage);
	}

		// 获得评论详细信息
	public void AddCommentInfo(String feed_id, String c_uid) {
		friend.AddCommentInfo(feed_id, c_uid);
	}

	// 调用js方法来加载好友动态
	public void dataToLoadList() {
		friend.dataToLoadList();
	}

	public void clickMoreTrend() {
		friend.clickMoreTrend();
	}
	public void showBigImg(final String path) {
		friend.showBigImg(path);
	}

	// 加载个人信息
	public void PersonalInfo() {
		friend.PersonalInfo();
	}
	public void showOperate(final String uid, final String name) {
		friend.showOperate(uid, name);
	}
	public void deleteFeed(String uid,String feed_id) {
		friend.deleteFeed(uid,feed_id);
	}

}

package com.bandlink.air.Js;

import com.bandlink.air.FriendFragment;
import com.bandlink.air.NewFriendFragment;

public class FriendInterface {

	FriendFragment friend;
	NewFriendFragment friend2;

	public FriendInterface(Object friend) {
		super();
		if(friend instanceof FriendFragment){

			this.friend = (FriendFragment)friend;
		}else if(friend instanceof NewFriendFragment){
			this.friend2 = (NewFriendFragment)friend;
		}
	}

	public void showOperate(final String uid, final String name) {
		if(friend2!=null){
			friend2.showOperate(uid, name);
		}else{
			friend.showOperate(uid, name);
		}
	
	}

	public void diggClick(String feed_id,String uid) {
		 
		if(friend2!=null){
			friend2.diggClick(feed_id,uid);
		}else{
			friend.diggClick(feed_id,uid);
		}
	}

	// 分享
	public void showShare(String feedid, String content, String[] urlImage) {
		 
		if(friend2!=null){
			friend2.showShare(feedid, content, urlImage);
		}else{
			friend.showShare(feedid, content, urlImage);
		}
	}

	public void FriendInfo() {
		if(friend2!=null){
			friend2.FriendInfo();
		}else{
			friend.FriendInfo();
		} 
	}

	public void setCurrentTab(int index) {
		 
		if(friend2!=null){
			friend2.setCurrentTab(index);
		}else{
			friend.setCurrentTab(index);
		} 
	}

	public void MyGuanzhu() {
		 
		if(friend2!=null){
			friend2.MyGuanzhu();
		}else{
			friend.MyGuanzhu();
		} 
	}

	public void Fans() {
		if(friend2!=null){
			friend2.Fans();
		}else{
			friend.Fans();
		} 
		
	}

	public void friendInvite() {
		if(friend2!=null){
			friend2.friendInvite();
		}else{
			friend.friendInvite();
		} 
		
	}

	// 获得评论详细信息
	public void AddCommentInfo(String feed_id, String c_uid) {
		
		if(friend2!=null){
			friend2.AddCommentInfo(feed_id, c_uid);
		}else{
			friend.AddCommentInfo(feed_id, c_uid);
		} 
	}

	public void MyMainPage() {
		
		if(friend2!=null){
			friend2.MyMainPage();
		}else{
			friend.MyMainPage();
		} 
	}

	// 调用js方法来加载好友动态
	public void dataToLoadList() {
		
		if(friend2!=null){
			friend2.dataToLoadList();
		}else{
			friend.dataToLoadList();
		} 
	}

	public void clickMoreTrend() {
	 
		if(friend2!=null){
			friend2.clickMoreTrend();
		}else{
			friend.clickMoreTrend();
		} 
	}

	public void clickMoreRank(String vi,String rankPage) {
		 
		if(friend2!=null){
			friend2.clickMoreRank(vi,rankPage);
		}else{
			friend.clickMoreRank(vi,rankPage);
		} 
	}

	public void showBigImg(final String path) {
		 
		if(friend2!=null){
			friend2.showBigImg(path);
		}else{
			friend.showBigImg(path);
		} 
	}
	public void openHelp(final String path) {
		
		if(friend2!=null){
			friend2.openHelp(path);
		} 
	}

	// 加载个人信息
	public void PersonalInfo() {
		if(friend2!=null){
			friend2.PersonalInfo();
		}else{
			friend.PersonalInfo();
		}  
	}

	public void refresh(String v) {
	 
		if(friend2!=null){
			friend2.refresh(v);
		}else{
			friend.refresh(v);
		}  
	}

	public void getRank(String value, final String rankPage) {
	 
		if(friend2!=null){
			friend2.getRank(value, rankPage);
		}else{
			friend.getRank(value, rankPage);
		}  
	}

	public void getMyRank(final String value) {
		 
		if(friend2!=null){
			friend2.getMyRank(value);
		}else{
			friend.getMyRank(value);
		}  
	}
}

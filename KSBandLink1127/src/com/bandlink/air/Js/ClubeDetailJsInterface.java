package com.bandlink.air.Js;

import android.content.Context;
import android.widget.Toast;

import com.bandlink.air.ClubDetailActivity;
import com.bandlink.air.R;

public class ClubeDetailJsInterface {
	Context context;
	ClubDetailActivity activity;

	public ClubeDetailJsInterface(ClubDetailActivity activity) {
		super();
		this.context = activity;
		this.activity = activity;
	}

	public void pushBaseData() {
		activity.pushBaseData();
	}

	// 获得俱乐部竞赛
	public void getAllMatch(final int all_index) {
		activity.getAllMatch(all_index);
	}

	public void shareFeed(final String feedid) {
		activity.shareFeed(feedid);
	}

	public void showBigImg(final String path) {
		activity.showBigImg(path);
	}

	public void joinClub(final String clubid, final boolean isIn) {
		activity.joinClub(clubid, isIn);
	}

	public void getAllGroup() {
		activity.getAllGroup();
	}

	// 获得俱乐部排名，先加载数据库
	public void getAllRank() {
		activity.getAllRank();
	}

	public void getMember(final String deparid, final int page) {
		activity.getMember(deparid, page);
	}

	public void getMemberFromDB(final String deparid, final int page) {
		activity.getMemberFromDB(activity.clubId,deparid, page);
	}

	public void setCurrentTab(int index) {
		activity.currentTab = index;
	}

	public void getClubFeed(final int index) {
		activity.getClubFeed(index);
	}

	// 点赞
	public void clickGood(final String feed_id) {
		activity.clickGood(feed_id);
	}

	// 评论
	public void addComment(String feed_id, String to_uid) {
		activity.addComment(feed_id, to_uid);
	}

	public void getJsMatchDetail(String matchid) {
		activity.getJsMatchDetail(matchid);
	}

	public void joinMatch(final String matchid) {
		activity.joinMatch(matchid);
	}

	public void jqJsMatch(String ismember, final String mid) {
		activity.jqJsMatch(ismember, mid);

	}

	public void showOperate(final String uid, final String name) {
		activity.showOperate(uid, name);
	}
	public void getRankFromDB(String gid,int page){
		activity.getRankFromDB(activity.clubId, gid,page);
	}

	// 获得竞赛内容，没有则提示
	public void getML(String str) {
		if (str.length() < 10) {
			Toast.makeText(context, context.getString(R.string.nomatch),
					Toast.LENGTH_SHORT).show();
			activity.showCreate();
		} else {
			activity.hideCreate();
		}
	}

	public void createNow() {
		activity.createMatch();
	}

	public void showGroupRank(String gid,int page) {
		activity.showGroupRank(gid,page);
	}
}
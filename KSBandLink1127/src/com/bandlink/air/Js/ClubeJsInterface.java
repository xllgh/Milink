package com.bandlink.air.Js;

import android.app.ProgressDialog;
import android.content.Context;

import com.bandlink.air.ClubFragment;
import com.bandlink.air.util.Dbutils;

public class ClubeJsInterface {
	Context context;

	Dbutils db;
	ProgressDialog dialog;
	boolean isInit = true;
	boolean isMInit = true;
	ClubFragment clubf;

	public ClubeJsInterface(ClubFragment clubf) {
		super();
		this.context = clubf.getActivity();
		this.clubf = clubf;

	}

	public void setCurrentTab(int index) {
		clubf.currentTab = index;
	}

	/***
	 * 
	 * 请求俱乐部信息
	 * 
	 * @param index
	 *            请求第几页
	 * @param area
	 *            俱乐部所在区域
	 * @param name
	 *            俱乐部名称
	 */
	public void getFromNet(int index, String area, String name) {
		 
			if(area.length()>0){
				clubf.showCityPicker(name, index);
			}else{
				clubf.getFromNet(index, area, name);
			}
		 
		
	}

	public void getClub(int index) {
		clubf.getClub(index);
	}

	// 请求我的俱乐部 name参数可无
	public void getMyClubs(int page, String name) {
		clubf.getMyClubs(page, name);
	}

	/***
	 * 查看指定id的club信息
	 * 
	 * @param id
	 */
	public void toClubeDetail(String id,String rec,String ismember) {
		clubf.toClubeDetail(id,rec,ismember);
	}

	public void join(String clubid) {
		clubf.join(clubid,clubf.session_id);
	}

	public boolean isMember() {
		return clubf.isMember();
	}

	public void toChargeClub() {
		clubf.toChargeClub();
	}

	public void isEmpty(String s) {
		if (s.length() < 1) {
			clubf.getFromNet(0, "", "");
		}
	}
}

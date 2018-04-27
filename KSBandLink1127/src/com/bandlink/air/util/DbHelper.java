package com.bandlink.air.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.bandlink.air.util.DbContract.AIRRECORDER;
import com.bandlink.air.util.DbContract.AirUpCheck;
import com.bandlink.air.util.DbContract.AllMatch;
import com.bandlink.air.util.DbContract.ClubDetail;
import com.bandlink.air.util.DbContract.ClubFeed;
import com.bandlink.air.util.DbContract.ClubFeedComment;
import com.bandlink.air.util.DbContract.ClubGroup;
import com.bandlink.air.util.DbContract.ClubInfo;
import com.bandlink.air.util.DbContract.ClubMatch;
import com.bandlink.air.util.DbContract.ClubMember;
import com.bandlink.air.util.DbContract.ClubRank;
import com.bandlink.air.util.DbContract.Comment;
import com.bandlink.air.util.DbContract.CooperTest;
import com.bandlink.air.util.DbContract.DOWNLOADLOG;
import com.bandlink.air.util.DbContract.Device;
import com.bandlink.air.util.DbContract.Dig;
import com.bandlink.air.util.DbContract.FriendList;
import com.bandlink.air.util.DbContract.Friendrank;
import com.bandlink.air.util.DbContract.GPSLASTPOINT;
import com.bandlink.air.util.DbContract.GPSPoints;
import com.bandlink.air.util.DbContract.GPSTrack;
import com.bandlink.air.util.DbContract.GPSUPLOAD;
import com.bandlink.air.util.DbContract.HR_RECORD;
import com.bandlink.air.util.DbContract.MatchMembers;
import com.bandlink.air.util.DbContract.MatchRank;
import com.bandlink.air.util.DbContract.MatchRules;
import com.bandlink.air.util.DbContract.MyClub;
import com.bandlink.air.util.DbContract.MyMatch;
import com.bandlink.air.util.DbContract.NANJING_CARD_ARGS;
import com.bandlink.air.util.DbContract.NOTIFICATION_LIST;
import com.bandlink.air.util.DbContract.PINGAN_SCORE;
import com.bandlink.air.util.DbContract.PdSpBasic;
import com.bandlink.air.util.DbContract.PdSpRaw;
import com.bandlink.air.util.DbContract.PdWgBasic;
import com.bandlink.air.util.DbContract.PermissionNotification;
import com.bandlink.air.util.DbContract.PersonalInfo;
import com.bandlink.air.util.DbContract.SOFTSTEP;
import com.bandlink.air.util.DbContract.SearchMatch;
import com.bandlink.air.util.DbContract.SleepAir0226;
import com.bandlink.air.util.DbContract.SleepBoundsDetail;
import com.bandlink.air.util.DbContract.SleepData;
import com.bandlink.air.util.DbContract.SleepDataDetail;
import com.bandlink.air.util.DbContract.TEMPERATURE;
import com.bandlink.air.util.DbContract.Target;
import com.bandlink.air.util.DbContract.Trends;
import com.bandlink.air.util.DbContract.User;

public class DbHelper extends SQLiteOpenHelper {
	private static final String TEXT_TYPE = " TEXT";
	private static final String INTEGER_TYPE = " INTEGER";
	private static final String BLOB_TYPE = " BLOB";
	private static final String COMMA_SEP = ",";
	private static final String REAL_TYPE = " REAL";
	public static final int DATABASE_VERSION = 28;
	public static final String DATABASE_NAME = "lovefit.db";

	/** 用户表 **/
	private static final String SQL_CREATE_USER = "CREATE TABLE IF NOT EXISTS "
			+ User.TABLE_NAME + " (" + User._ID + " INTEGER PRIMARY KEY,"
			+ User.UID + INTEGER_TYPE + COMMA_SEP + User.NICKNAME + TEXT_TYPE
			+ COMMA_SEP + User.ISMEMBER + INTEGER_TYPE + COMMA_SEP + User.SEX
			+ INTEGER_TYPE + COMMA_SEP + User.HEIGHT + REAL_TYPE + COMMA_SEP
			+ User.WEIGHT + REAL_TYPE + COMMA_SEP + User.CONTACT + TEXT_TYPE
			+ COMMA_SEP + User.EMAIL + TEXT_TYPE + COMMA_SEP + User.LOVEFITID
			+ TEXT_TYPE + COMMA_SEP + User.BIRTH + TEXT_TYPE + COMMA_SEP
			+ User.USERNAME + TEXT_TYPE + " )";

	private static final String SQL_DELETE_USER = "DROP TABLE IF EXISTS "
			+ User.TABLE_NAME;

	// 点赞表
	private static final String SQL_CREATE_DIG = "CREATE TABLE IF NOT EXISTS "
			+ Dig.TABLE_NAME + " (" + Dig._ID + " INTEGER PRIMARY KEY,"
			+ Dig.UID + INTEGER_TYPE + COMMA_SEP + Dig.feed_id + TEXT_TYPE
			+ COMMA_SEP + Dig.is_ok + INTEGER_TYPE + " )";
	// 是否有读取通知的权限
	private static final String SQL_CREATE_PERMiSSION = "CREATE TABLE IF NOT EXISTS "
			+ PermissionNotification.TABLE_NAME
			+ " ("
			+ PermissionNotification._ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT ,"
			+ PermissionNotification.ENABLE + TEXT_TYPE + " )";
	// 通知过滤
	private static final String SQL_CREATE_NOTIFICATION_LIST = "CREATE TABLE IF NOT EXISTS "
			+ NOTIFICATION_LIST.TABLE_NAME
			+ " ("
			+ NOTIFICATION_LIST._ID
			+ " INTEGER PRIMARY KEY,"
			+ NOTIFICATION_LIST.UID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ NOTIFICATION_LIST.APPNAME
			+ TEXT_TYPE
			+ COMMA_SEP
			+ NOTIFICATION_LIST.DISABLE
			+ TEXT_TYPE
			+ COMMA_SEP
			+ NOTIFICATION_LIST.PACKET + TEXT_TYPE + " )";
	private static final String SQL_CREATE_HR = "CREATE TABLE IF NOT EXISTS "
			+ HR_RECORD.TABLE_NAME
			+ " ("
			+ HR_RECORD._ID
			+ " INTEGER PRIMARY KEY,"
			+ HR_RECORD.UID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ HR_RECORD.DATA
			+ BLOB_TYPE
			+ COMMA_SEP
			+ HR_RECORD.DATE
			+ TEXT_TYPE
			+ COMMA_SEP
			+ HR_RECORD.UPLOAD + INTEGER_TYPE + " )";

	private static final String SQL_DELETE_Dig = "DROP TABLE IF EXISTS "
			+ Dig.TABLE_NAME;

	/** 设备表 **/
	private static final String SQL_CREATE_DEVICE = "CREATE TABLE IF NOT EXISTS "
			+ Device.TABLE_NAME
			+ " ("
			+ Device._ID
			+ " INTEGER PRIMARY KEY,"
			+ Device.UID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ Device.DEVICESP_TYPE
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ Device.DEVICESP_ID
			+ TEXT_TYPE
			+ COMMA_SEP
			+ Device.DEVICEWG_TYPE
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ Device.DEVICEWG_ID + TEXT_TYPE + ")";
	/** 南京公交参数 **/
	private static final String SQL_CREATE_NANJING_CARD_ARGS = "CREATE TABLE IF NOT EXISTS "
			+ NANJING_CARD_ARGS.TABLE_NAME
			+ " ("
			+ NANJING_CARD_ARGS._ID
			+ " INTEGER PRIMARY KEY,"
			+ NANJING_CARD_ARGS.UID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ NANJING_CARD_ARGS.BD01
			+ TEXT_TYPE
			+ COMMA_SEP
			+ NANJING_CARD_ARGS.BD05
			+ TEXT_TYPE
			+ COMMA_SEP
			+ NANJING_CARD_ARGS.BD0E
			+ TEXT_TYPE
			+ COMMA_SEP
			+ NANJING_CARD_ARGS.BD0F
			+ TEXT_TYPE
			+ COMMA_SEP
			+ NANJING_CARD_ARGS.HEXUID
			+ TEXT_TYPE
			+ COMMA_SEP
			+ NANJING_CARD_ARGS.MAC
			+ TEXT_TYPE
			+ COMMA_SEP
			+ NANJING_CARD_ARGS.CARD
			+ TEXT_TYPE
			+ COMMA_SEP
			+ NANJING_CARD_ARGS.CHENGEID + TEXT_TYPE + ")";
	/** 扣分记录 **/
	private static final String SQL_CREATE_SleepBounds = "CREATE TABLE IF NOT EXISTS "
			+ SleepBoundsDetail.TABLE_NAME
			+ " ("
			+ SleepBoundsDetail._ID
			+ " INTEGER PRIMARY KEY,"
			+ SleepBoundsDetail.UID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ SleepBoundsDetail.BOUNDS
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ SleepBoundsDetail.INTRO
			+ TEXT_TYPE
			+ COMMA_SEP
			+ SleepBoundsDetail.SAVETime
			+ TEXT_TYPE
			+ COMMA_SEP
			+ SleepBoundsDetail.DATE + TEXT_TYPE + ")";

	private static final String SQL_DELETE_DEVICE = "DROP TABLE IF EXISTS "
			+ Device.TABLE_NAME;

	// 朋友圈好友列表
	private static final String SQL_CREATE_FRIENDLIST = "CREATE TABLE IF NOT EXISTS "
			+ FriendList.TABLE_NAME
			+ " ("
			+ FriendList._ID
			+ " INTEGER PRIMARY KEY,"
			+ FriendList.FUID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ FriendList.NICKNAME
			+ TEXT_TYPE
			+ COMMA_SEP
			+ FriendList.GID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ FriendList.PHOTO
			+ TEXT_TYPE + ")";

	private static final String SQL_DELETE_FRIENDLIST = "DROP TABLE IF EXISTS "
			+ FriendList.TABLE_NAME;

	/** 朋友圈运动排名表 **/

	private static final String SQL_CREATE_FRIENDRANK = "CREATE TABLE IF NOT EXISTS "
			+ Friendrank.TABLE_NAME
			+ " ("
			+ Friendrank._ID
			+ " INTEGER PRIMARY KEY,"
			+ Friendrank.UID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ Friendrank.USER
			+ TEXT_TYPE
			+ COMMA_SEP
			+ Friendrank.STEP
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ Friendrank.DATE
			+ TEXT_TYPE
			+ COMMA_SEP
			+ Friendrank.GID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ Friendrank.RANK_TYPE
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ Friendrank.UPDATETIME
			+ TEXT_TYPE
			+ COMMA_SEP
			+ Friendrank.URL
			+ TEXT_TYPE + ")";

	private static final String SQL_DELETE_FRIENDRANK = "DROP TABLE IF EXISTS "
			+ Friendrank.TABLE_NAME;

	/* 获得朋友圈个人信息 */
	private static final String SQL_CREATE_PERSONALINFO = "CREATE TABLE IF NOT EXISTS "
			+ PersonalInfo.TABLE_NAME
			+ " ("
			+ PersonalInfo._ID
			+ " INTEGER PRIMARY KEY,"
			+ PersonalInfo.UID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ PersonalInfo.NAME
			+ TEXT_TYPE
			+ COMMA_SEP
			+ PersonalInfo.MESSAGE
			+ TEXT_TYPE
			+ COMMA_SEP
			+ PersonalInfo.PHOTO
			+ TEXT_TYPE
			+ COMMA_SEP
			+ PersonalInfo.DONGTAINUM
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ PersonalInfo.FRIENDNUM
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ PersonalInfo.GUANZHUNUM
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ PersonalInfo.FANSNUM
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ PersonalInfo.ADDFRIENDNUM + INTEGER_TYPE + ")";

	private static final String SQL_DELETE_PERSONALINFO = "DROP TABLE IF EXISTS "
			+ PersonalInfo.TABLE_NAME;

	// 评论表
	private static final String SQL_CREATE_COMMENT = "CREATE TABLE IF NOT EXISTS "
			+ Comment.TABLE_NAME
			+ " ("
			+ Comment._ID
			+ " INTEGER PRIMARY KEY,"
			+ Comment.UID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ Comment.NAME
			+ TEXT_TYPE
			+ COMMA_SEP
			+ Comment.CONTENT
			+ TEXT_TYPE
			+ COMMA_SEP
			+ Comment.TO_UID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ Comment.FEED_ID
			+ INTEGER_TYPE + COMMA_SEP + Comment.TIME + TEXT_TYPE + ")";

	private static final String SQL_DELETE_COMMENT = "DROP TABLE IF EXISTS "
			+ Comment.TABLE_NAME;

	/** 动态列表 **/
	private static final String SQL_CREATE_TRENDS = "CREATE TABLE IF NOT EXISTS "
			+ Trends.TABLE_NAME
			+ " ("
			+ Trends._ID
			+ " INTEGER PRIMARY KEY,"
			+ Trends.UID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ Trends.APP_ROW_ID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ Trends.FEED_ID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ Trends.TYPE
			+ TEXT_TYPE
			+ COMMA_SEP
			+ Trends.PUBLISH_TIME
			+ TEXT_TYPE
			+ COMMA_SEP
			+ Trends.DIGG_COUNT
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ Trends.COMMENT_ALL_COUNT
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ Trends.REPOST_COUNT
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ Trends.COMMENT_COUNT
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ Trends.IS_REPOST
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ Trends.IS_AUDIT
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ Trends.FEED_CONTENT
			+ TEXT_TYPE
			+ COMMA_SEP
			+ Trends.PHOTO
			+ TEXT_TYPE
			+ COMMA_SEP
			+ Trends.BIAOQING
			+ TEXT_TYPE
			+ COMMA_SEP
			+ Trends.TIME
			+ TEXT_TYPE
			+ COMMA_SEP
			+ Trends.NAME
			+ TEXT_TYPE
			+ COMMA_SEP
			+ Trends.DA_CONTENT
			+ TEXT_TYPE
			+ COMMA_SEP
			+ Trends.DA_BODY
			+ TEXT_TYPE
			+ COMMA_SEP
			+ Trends.DA_SOURCE_URL
			+ TEXT_TYPE
			+ COMMA_SEP
			+ Trends.DA_UID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ Trends.DA_APP
			+ TEXT_TYPE
			+ COMMA_SEP
			+ Trends.DA_TYPE
			+ TEXT_TYPE
			+ COMMA_SEP
			+ Trends.DA_APP_ROW_ID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ Trends.DA_APP_ROW_TABLE
			+ TEXT_TYPE
			+ COMMA_SEP
			+ Trends.DA_PUBLISH_TIME
			+ TEXT_TYPE
			+ COMMA_SEP
			+ Trends.DA_FROM
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ Trends.DA_REPOST_COUNT
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ Trends.DA_COMMENT_COUNT
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ Trends.DA_IS_DEL
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ Trends.DA_IS_REPOST
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ Trends.DA_ID_AUDIT
			+ INTEGER_TYPE
			+ ")";

	private static final String SQL_DELETE_TRENDS = "DROP TABLE IF EXISTS "
			+ Trends.TABLE_NAME;

	/** 目标表 **/

	private static final String SQL_CREATE_TARGET = "CREATE TABLE IF NOT EXISTS "
			+ Target.TABLE_NAME
			+ " ("
			+ Target._ID
			+ " INTEGER PRIMARY KEY,"
			+ Target.UID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ Target.TARGET_TYPE
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ Target.TARGET_STEP
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ Target.TARGET_WEIGHT_BEFORE
			+ REAL_TYPE
			+ COMMA_SEP
			+ Target.TARGET_WEIGHT_END
			+ REAL_TYPE
			+ COMMA_SEP
			+ Target.TARGET_WEIGHT
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ Target.TARGET_SLEEP
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ Target.TARGET_BMI + INTEGER_TYPE + ")";
	private static final String SQL_DELETE_TARGET = "DROP TABLE IF EXISTS "
			+ Target.TABLE_NAME;

	/** 运动基本数据表 **/
	private static final String SQL_CREATE_SPBASIC = "CREATE TABLE IF NOT EXISTS "
			+ PdSpBasic.TABLE_NAME
			+ " ("
			+ PdSpBasic._ID
			+ " INTEGER PRIMARY KEY,"
			+ PdSpBasic.UID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ PdSpBasic.DATE
			+ TEXT_TYPE
			+ COMMA_SEP
			+ PdSpBasic.STEP
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ PdSpBasic.DISTANCE
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ PdSpBasic.CALORIE
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ PdSpBasic.LVL
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ PdSpBasic.TYPE
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ PdSpBasic.UPLOAD
			+ INTEGER_TYPE + COMMA_SEP + PdSpBasic.UPDATETIME + TEXT_TYPE + ")";
	private static final String SQL_DELETE_SPBASIC = "DROP TABLE IF EXISTS "
			+ PdSpBasic.TABLE_NAME;

	/** 体重基本数据表 **/
	private static final String SQL_CREATE_WGBASIC = "CREATE TABLE IF NOT EXISTS "
			+ PdWgBasic.TABLE_NAME
			+ " ("
			+ PdWgBasic._ID
			+ " INTEGER PRIMARY KEY,"
			+ PdWgBasic.UID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ PdWgBasic.DATE
			+ TEXT_TYPE
			+ COMMA_SEP
			+ PdWgBasic.WEIGHT
			+ REAL_TYPE
			+ COMMA_SEP
			+ PdWgBasic.BMI
			+ REAL_TYPE
			+ COMMA_SEP
			+ PdWgBasic.FAT
			+ REAL_TYPE
			+ COMMA_SEP
			+ PdWgBasic.WATER
			+ REAL_TYPE
			+ COMMA_SEP
			+ PdWgBasic.MUSCLE
			+ REAL_TYPE
			+ COMMA_SEP
			+ PdWgBasic.BONE
			+ REAL_TYPE
			+ COMMA_SEP
			+ PdWgBasic.TYPE
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ PdWgBasic.UPDATETIME
			+ TEXT_TYPE + ")";
	private static final String SQL_DELETE_WGBASIC = "DROP TABLE IF EXISTS "
			+ PdWgBasic.TABLE_NAME;

	/** 俱乐部信息表 **/
	private static final String SQL_CREATE_CLUBINFO = "CREATE TABLE IF NOT EXISTS "
			+ ClubInfo.TABLE_NAME
			+ " ("
			+ ClubInfo._ID
			+ " INTEGER PRIMARY KEY,"
			+ ClubInfo.CLUBID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ ClubInfo.NAME
			+ TEXT_TYPE
			+ COMMA_SEP
			+ ClubInfo.MEMBERNUM
			+ TEXT_TYPE
			+ COMMA_SEP
			+ ClubInfo.ISPUBLIC
			+ REAL_TYPE
			+ COMMA_SEP
			+ ClubInfo.PROVINCE
			+ REAL_TYPE
			+ COMMA_SEP
			+ ClubInfo.CITY
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ ClubInfo.DISTRICT
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ ClubInfo.LOGO
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ ClubInfo.UID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ ClubInfo.INDEX + " BIGINT " + ")";
	private static final String SQL_DELETE_CLUBINFO = "DROP TABLE IF EXISTS "
			+ ClubInfo.TABLE_NAME;

	/*** gps轨迹表 **/
	private static final String SQL_CREATE_GPSTRACK = "CREATE TABLE IF NOT EXISTS "
			+ GPSTrack.TABLE_NAME
			+ " ("
			+ GPSTrack._ID
			+ " INTEGER PRIMARY KEY,"
			+ GPSTrack.UID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ GPSTrack.DATE
			+ TEXT_TYPE
			+ COMMA_SEP
			+ GPSTrack.STEP
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ GPSTrack.CALORIE
			+ REAL_TYPE
			+ COMMA_SEP
			+ GPSTrack.DISTANCE
			+ REAL_TYPE
			+ COMMA_SEP
			+ GPSTrack.DURATION
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ GPSTrack.POINTS
			+ TEXT_TYPE + ")";

	/*** gps轨迹点 **/

	private static final String SQL_CREATE_GPSPOINTS = "CREATE TABLE IF NOT EXISTS "
			+ GPSPoints.TABLE_NAME
			+ " ("
			+ GPSPoints._ID
			+ " INTEGER PRIMARY KEY,"
			+ GPSPoints.TID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ GPSPoints.TIME
			+ TEXT_TYPE
			+ COMMA_SEP
			+ GPSPoints.LATITUDE
			+ REAL_TYPE
			+ COMMA_SEP
			+ GPSPoints.lONGTITUDE
			+ REAL_TYPE
			+ COMMA_SEP
			+ GPSPoints.SPEED
			+ REAL_TYPE
			+ COMMA_SEP
			+ GPSPoints.ACCURACY + REAL_TYPE + ")";

	/*** 运动原始数据 ***/
	private static final String SQL_CREATE_SPRAW = "CREATE TABLE IF NOT EXISTS "
			+ PdSpRaw.TABLE_NAME
			+ " ("
			+ PdSpRaw._ID
			+ " INTEGER PRIMARY KEY,"
			+ PdSpRaw.UID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ PdSpRaw.DATE
			+ TEXT_TYPE
			+ COMMA_SEP
			+ PdSpRaw.HOUR
			+ INTEGER_TYPE
			+ " default 24"
			+ COMMA_SEP
			+ PdSpRaw.DATA
			+ BLOB_TYPE
			+ COMMA_SEP
			+ PdSpRaw.TYPE
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ PdSpRaw.UPLOAD
			+ INTEGER_TYPE
			+ COMMA_SEP + PdSpRaw.UPDATETIME + TEXT_TYPE + ")";
	private static final String SQL_DELETE_SPRAW = "DROP TABLE IF EXISTS "
			+ PdSpRaw.TABLE_NAME;

	/** 俱乐部详情表 **/
	private static final String SQL_CREATE_CLUBDETAIL = "CREATE TABLE IF NOT EXISTS "
			+ ClubDetail.TABLE_NAME
			+ " ("
			+ ClubDetail._ID
			+ " INTEGER PRIMARY KEY,"
			+ ClubDetail.CLUBID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ ClubDetail.NAME
			+ TEXT_TYPE
			+ COMMA_SEP
			+ ClubDetail.MEMBERNUM
			+ TEXT_TYPE
			+ COMMA_SEP
			+ ClubDetail.ISPUBLIC
			+ REAL_TYPE
			+ COMMA_SEP
			+ ClubDetail.PROVINCE
			+ REAL_TYPE
			+ COMMA_SEP
			+ ClubDetail.CITY
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ ClubDetail.DISTRICT
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ ClubDetail.LOGO
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ ClubDetail.ADMIN_UID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ ClubDetail.GROUPNUM
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ ClubDetail.INTRO
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ ClubDetail.UID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ ClubDetail.ADMIN_NAME
			+ TEXT_TYPE + ")";
	private static final String SQL_DELETE_CLUBDETAIL = "DROP TABLE IF EXISTS "
			+ ClubDetail.TABLE_NAME;
	/** 我的俱乐部 **/
	private static final String SQL_CREATE_MYCLUB = "CREATE TABLE IF NOT EXISTS "
			+ MyClub.TABLE_NAME
			+ " ("
			+ MyClub._ID
			+ " INTEGER PRIMARY KEY,"
			+ MyClub.CLUBID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ MyClub.NAME
			+ TEXT_TYPE
			+ COMMA_SEP
			+ MyClub.MEMBERNUM
			+ TEXT_TYPE
			+ COMMA_SEP
			+ MyClub.ISPUBLIC
			+ REAL_TYPE
			+ COMMA_SEP
			+ MyClub.PROVINCE
			+ REAL_TYPE
			+ COMMA_SEP
			+ MyClub.CITY
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ MyClub.DISTRICT
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ MyClub.UID
			+ INTEGER_TYPE + COMMA_SEP + MyClub.LOGO + INTEGER_TYPE + ")";
	private static final String SQL_DELETE_MYCLUB = "DROP TABLE IF EXISTS "
			+ MyClub.TABLE_NAME;

	/** 俱乐部班组 **/
	private static final String SQL_CREATE_CLUBGROUP = "CREATE TABLE IF NOT EXISTS "
			+ ClubGroup.TABLE_NAME
			+ " ("
			+ ClubGroup._ID
			+ " INTEGER PRIMARY KEY,"
			+ ClubGroup.CLUBID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ ClubGroup.NAME
			+ TEXT_TYPE
			+ COMMA_SEP
			+ ClubGroup.GROUPID
			+ TEXT_TYPE
			+ COMMA_SEP
			+ ClubGroup.UID
			+ INTEGER_TYPE + ")";
	private static final String SQL_DELETE_CLUBGROUP = "DROP TABLE IF EXISTS "
			+ ClubGroup.TABLE_NAME;
	/** 俱乐部排名 **/
	private static final String SQL_CREATE_CLUBRANK = "CREATE TABLE IF NOT EXISTS "
			+ ClubRank.TABLE_NAME
			+ " ("
			+ ClubRank._ID
			+ " INTEGER PRIMARY KEY,"
			+ ClubRank.CLUBID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ ClubRank.USERNAME
			+ TEXT_TYPE
			+ COMMA_SEP
			+ ClubRank.GID
			+ TEXT_TYPE
			+ COMMA_SEP
			+ ClubRank.STEP
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ ClubRank.TIME
			+ TEXT_TYPE
			+ COMMA_SEP
			+ ClubRank.PHOTO
			+ TEXT_TYPE
			+ COMMA_SEP
			+ ClubRank.RANKUID
			+ TEXT_TYPE + COMMA_SEP + ClubRank.UID + INTEGER_TYPE + ")";
	private static final String SQL_DELETE_CLUBRANK = "DROP TABLE IF EXISTS "
			+ ClubGroup.TABLE_NAME;

	/** 俱乐部成员 **/
	private static final String SQL_CREATE_CLUBMEMBER = "CREATE TABLE IF NOT EXISTS "
			+ ClubMember.TABLE_NAME
			+ " ("
			+ ClubMember._ID
			+ " INTEGER PRIMARY KEY,"
			+ ClubMember.CLUBID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ ClubMember.USERNAME
			+ TEXT_TYPE
			+ COMMA_SEP
			+ ClubMember.GID
			+ TEXT_TYPE
			+ COMMA_SEP
			+ ClubMember.USERID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ ClubMember.ADMIN
			+ TEXT_TYPE
			+ COMMA_SEP
			+ ClubMember.PHOTO
			+ TEXT_TYPE
			+ COMMA_SEP
			+ ClubMember.DEPARTNAME
			+ TEXT_TYPE
			+ COMMA_SEP
			+ ClubMember.UID
			+ INTEGER_TYPE + ")";
	private static final String SQL_DELETE_CLUBMEMBER = "DROP TABLE IF EXISTS "
			+ ClubGroup.TABLE_NAME;
	/** 俱乐部动态 **/
	private static final String SQL_CREATE_CLUBFEED = "CREATE TABLE IF NOT EXISTS "
			+ ClubFeed.TABLE_NAME
			+ " ("
			+ ClubFeed._ID
			+ " INTEGER PRIMARY KEY,"
			+ ClubFeed.CLUBID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ ClubFeed.FEEDID
			+ TEXT_TYPE
			+ COMMA_SEP
			+ ClubFeed.CONTENT
			+ TEXT_TYPE
			+ COMMA_SEP
			+ ClubFeed.USERID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ ClubFeed.TIME
			+ " BIGINT "
			+ COMMA_SEP
			+ ClubMember.UID + INTEGER_TYPE + ")";
	private static final String SQL_DELETE_CLUBFEED = "DROP TABLE IF EXISTS "
			+ ClubFeed.TABLE_NAME;
	/** 俱乐部动态评论 **/
	private static final String SQL_CREATE_CLUBFEEDCOMMENT = "CREATE TABLE IF NOT EXISTS "
			+ ClubFeedComment.TABLE_NAME
			+ " ("
			+ ClubFeedComment._ID
			+ " INTEGER PRIMARY KEY,"
			+ ClubFeedComment.CLUBID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ ClubFeedComment.FEEDID
			+ TEXT_TYPE
			+ COMMA_SEP
			+ ClubFeedComment.CONTENT
			+ TEXT_TYPE
			+ COMMA_SEP
			+ ClubFeedComment.COMMENTID
			+ TEXT_TYPE
			+ COMMA_SEP
			+ ClubFeedComment.UID + INTEGER_TYPE + ")";
	private static final String SQL_DELETE_CLUBFEEDCOMMENT = "DROP TABLE IF EXISTS "
			+ ClubFeedComment.TABLE_NAME;

	private static final String SQL_CREATE_MYMATCH = "CREATE TABLE IF NOT EXISTS "
			+ MyMatch.TABLE_NAME
			+ " ("
			+ MyMatch._ID
			+ " INTEGER PRIMARY KEY,"
			+ MyMatch.MATCH_ID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ MyMatch.LOGO
			+ TEXT_TYPE
			+ COMMA_SEP
			+ MyMatch.UID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ MyMatch.USERNAME
			+ TEXT_TYPE
			+ COMMA_SEP
			+ MyMatch.MATCH_TITLE
			+ TEXT_TYPE
			+ COMMA_SEP
			+ MyMatch.MEMBER_NUM
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ MyMatch.STARTTIME
			+ TEXT_TYPE
			+ COMMA_SEP
			+ MyMatch.ENDTIME
			+ TEXT_TYPE
			+ COMMA_SEP
			+ MyMatch.STATUS
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ MyMatch.ISPUBLIC
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ MyMatch.TARGET
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ MyMatch.ISMEMBER + INTEGER_TYPE + ")";

	private static final String SQL_DELETE_MYMATCH = "DROP TABLE IF EXISTS "
			+ MyMatch.TABLE_NAME;

	private static final String SQL_CREATE_ALLMATCH = "CREATE TABLE IF NOT EXISTS "
			+ AllMatch.TABLE_NAME
			+ " ("
			+ AllMatch._ID
			+ " INTEGER PRIMARY KEY,"
			+ AllMatch.MATCH_ID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ AllMatch.LOGO
			+ TEXT_TYPE
			+ COMMA_SEP
			+ AllMatch.UID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ AllMatch.USERNAME
			+ TEXT_TYPE
			+ COMMA_SEP
			+ AllMatch.MATCH_TITLE
			+ TEXT_TYPE
			+ COMMA_SEP
			+ AllMatch.MEMBER_NUM
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ AllMatch.STARTTIME
			+ TEXT_TYPE
			+ COMMA_SEP
			+ AllMatch.ENDTIME
			+ TEXT_TYPE
			+ COMMA_SEP
			+ AllMatch.STATUS
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ AllMatch.ISPUBLIC
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ AllMatch.TARGET
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ AllMatch.ISMEMBER
			+ INTEGER_TYPE + ")";
	private static final String SQL_DELETE_ALLMATCH = "DROP TABLE IF EXISTS "
			+ AllMatch.TABLE_NAME;
	// 俱乐部竞赛
	private static final String SQL_CREATE_CLUBMATCH = "CREATE TABLE IF NOT EXISTS  "
			+ ClubMatch.TABLE_NAME
			+ " ("
			+ AllMatch._ID
			+ " INTEGER PRIMARY KEY,"
			+ ClubMatch.MATCH_ID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ ClubMatch.LOGO
			+ TEXT_TYPE
			+ COMMA_SEP
			+ ClubMatch.UID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ ClubMatch.USERNAME
			+ TEXT_TYPE
			+ COMMA_SEP
			+ ClubMatch.MATCH_TITLE
			+ TEXT_TYPE
			+ COMMA_SEP
			+ ClubMatch.MEMBER_NUM
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ ClubMatch.STARTTIME
			+ TEXT_TYPE
			+ COMMA_SEP
			+ ClubMatch.ENDTIME
			+ TEXT_TYPE
			+ COMMA_SEP
			+ ClubMatch.STATUS
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ ClubMatch.CLUB_ID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ ClubMatch.ISPUBLIC
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ ClubMatch.TARGET
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ ClubMatch.ISMEMBER
			+ INTEGER_TYPE
			+ ")";
	private static final String SQL_DELETE_CLUBMATCH = "DROP TABLE IF EXISTS "
			+ ClubMatch.TABLE_NAME;

	private static final String SQL_CREATE_SEARCHMATCH = "CREATE TABLE IF NOT EXISTS "
			+ SearchMatch.TABLE_NAME
			+ " ("
			+ SearchMatch._ID
			+ " INTEGER PRIMARY KEY,"
			+ SearchMatch.MATCH_ID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ SearchMatch.LOGO
			+ TEXT_TYPE
			+ COMMA_SEP
			+ SearchMatch.UID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ SearchMatch.USERNAME
			+ TEXT_TYPE
			+ COMMA_SEP
			+ SearchMatch.MATCH_TITLE
			+ TEXT_TYPE
			+ COMMA_SEP
			+ SearchMatch.MEMBER_NUM
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ SearchMatch.STARTTIME
			+ TEXT_TYPE
			+ COMMA_SEP
			+ SearchMatch.ENDTIME
			+ TEXT_TYPE
			+ COMMA_SEP
			+ SearchMatch.STATUS
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ SearchMatch.ISPUBLIC
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ SearchMatch.TARGET
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ SearchMatch.ISMEMBER + INTEGER_TYPE + ")";
	private static final String SQL_DELETE_SEARCHMATCH = "DROP TABLE IF EXISTS "
			+ SearchMatch.TABLE_NAME;

	private static final String SQL_CREATE_MATCHRANK = "CREATE TABLE IF NOT EXISTS "
			+ MatchRank.TABLE_NAME
			+ " ("
			+ MatchRank._ID
			+ " INTEGER PRIMARY KEY,"
			+ MatchRank.MATCH_ID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ MatchRank.AVATAR
			+ TEXT_TYPE
			+ COMMA_SEP
			+ MatchRank.UID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ MatchRank.USERNAME
			+ TEXT_TYPE
			+ COMMA_SEP
			+ MatchRank.STEP
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ MatchRank.TIME + TEXT_TYPE + ")";

	private static final String SQL_DELETE_MATCHRANK = "DROP TABLE IF EXISTS "
			+ MatchRank.TABLE_NAME;

	private static final String SQL_CREATE_MATCHRULES = "CREATE TABLE IF NOT EXISTS "
			+ MatchRules.TABLE_NAME
			+ " ("
			+ MatchRules._ID
			+ " INTEGER PRIMARY KEY,"
			+ MatchRules.MATCH_ID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ MatchRules.MATCH_PICS
			+ TEXT_TYPE
			+ COMMA_SEP
			+ MatchRules.MATCH_CONTENTS + TEXT_TYPE + ")";

	private static final String SQL_DELETE_MATCHRULES = "DROP TABLE IF EXISTS "
			+ MatchRules.TABLE_NAME;

	private static final String SQL_CREATE_MATCHMEMBERS = "CREATE TABLE IF NOT EXISTS "
			+ MatchMembers.TABLE_NAME
			+ " ("
			+ MatchMembers._ID
			+ " INTEGER PRIMARY KEY,"
			+ MatchMembers.MATCH_ID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ MatchMembers.UID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ MatchMembers.USERNAME
			+ TEXT_TYPE
			+ COMMA_SEP
			+ MatchMembers.AVATAR + TEXT_TYPE + ")";

	private static final String SQL_DELETE_CooperTest = "DROP TABLE IF EXISTS "
			+ CooperTest.TABLE_NAME;
	private static final String SQL_CREATE_CooperTest = "CREATE TABLE IF NOT EXISTS "
			+ CooperTest.TABLE_NAME
			+ " ("
			+ CooperTest._ID
			+ " INTEGER PRIMARY KEY,"
			+ CooperTest.STEP
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ CooperTest.UID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ CooperTest.DISTANCE
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ CooperTest.SCORE
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ CooperTest.DURATION
			+ TEXT_TYPE
			+ COMMA_SEP
			+ CooperTest.TIME
			+ TEXT_TYPE
			+ COMMA_SEP
			+ CooperTest.CAL + TEXT_TYPE + ")";

	private static final String SQL_DELETE_MATCHMEMBERS = "DROP TABLE IF EXISTS "
			+ MatchMembers.TABLE_NAME;

	private static final String SQL_CREATE_SOFTSTEP = "CREATE TABLE IF NOT EXISTS "
			+ SOFTSTEP.TABLE_NAME
			+ " ("
			+ SOFTSTEP._ID
			+ " INTEGER PRIMARY KEY,"
			+ SOFTSTEP.STEP_TODAY
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ SOFTSTEP.CAL_TODAY
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ SOFTSTEP.SAVETIME + TEXT_TYPE + ")";

	private static final String SQL_DELETE_SOFTSTEP = "DROP TABLE IF EXISTS "
			+ SOFTSTEP.TABLE_NAME;

	private static final String SQL_CREATE_GPSLASTPOINT = "CREATE TABLE IF NOT EXISTS "
			+ GPSLASTPOINT.TABLE_NAME
			+ " ("
			+ GPSLASTPOINT._ID
			+ " INTEGER PRIMARY KEY,"
			+ GPSLASTPOINT.LATITUDE
			+ REAL_TYPE
			+ COMMA_SEP + GPSLASTPOINT.LONGITUDE + REAL_TYPE + ")";

	private static final String SQL_DELETE_GPSLASTPOINT = "DROP TABLE IF EXISTS "
			+ GPSLASTPOINT.TABLE_NAME;

	private static final String SQL_CREATE_GPSUPLOAD = "CREATE TABLE IF NOT EXISTS "
			+ GPSUPLOAD.TABLE_NAME
			+ " ("
			+ GPSUPLOAD._ID
			+ " INTEGER PRIMARY KEY,"
			+ GPSUPLOAD.UID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ GPSUPLOAD.IID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ GPSUPLOAD.ISUPLOAD + INTEGER_TYPE + ")";

	private static final String SQL_DELETE_GPSUPLOAD = "DROP TABLE IF EXISTS "
			+ GPSUPLOAD.TABLE_NAME;

	// 睡眠
	private static final String SQL_CREATE_SLEEPDATA = "CREATE TABLE IF NOT EXISTS "
			+ SleepData.TABLE_NAME
			+ " ("
			+ SleepData._ID
			+ " INTEGER PRIMARY KEY,"
			+ SleepData.UID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ SleepData.DATE
			+ TEXT_TYPE
			+ COMMA_SEP
			+ SleepData.SCORE
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ SleepData.TOTAL
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ SleepData.DEEP
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ SleepData.LIGHT
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ SleepData.LUCID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ SleepData.ISUPLOAD
			+ INTEGER_TYPE + " default 24 " + ")";

	private static final String SQL_DELETE_SLEEPDATA = "DROP TABLE IF EXISTS "
			+ SleepData.TABLE_NAME;
	// 下载记录
	private static final String SQL_CREATE_DOWNLOAD = "CREATE TABLE IF NOT EXISTS "
			+ DOWNLOADLOG.TABLE_NAME
			+ " ("
			+ DOWNLOADLOG._ID
			+ " INTEGER PRIMARY KEY,"
			+ DOWNLOADLOG.UID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ DOWNLOADLOG.AIMDATE
			+ TEXT_TYPE
			+ COMMA_SEP
			+ DOWNLOADLOG.NOWDATE + TEXT_TYPE + ")";
	// Air上传记录
	private static final String SQL_CREATE_AirUpCheck = "CREATE TABLE IF NOT EXISTS "
			+ AirUpCheck.TABLE_NAME
			+ " ("
			+ AirUpCheck._ID
			+ " INTEGER PRIMARY KEY,"
			+ AirUpCheck.UID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ AirUpCheck.DATE
			+ TEXT_TYPE
			+ COMMA_SEP
			+ AirUpCheck.STATUS + TEXT_TYPE + ")";

	private static final String SQL_DELETE_DOWNLOAD = "DROP TABLE IF EXISTS "
			+ DOWNLOADLOG.TABLE_NAME;
	// 睡眠详细数据
	private static final String SQL_CREATE_SLEEPDETAIL = "CREATE TABLE IF NOT EXISTS "
			+ SleepDataDetail.TABLE_NAME
			+ " ("
			+ SleepDataDetail._ID
			+ " INTEGER PRIMARY KEY,"
			+ SleepDataDetail.UID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ SleepDataDetail.TIME
			+ TEXT_TYPE
			+ COMMA_SEP
			+ SleepDataDetail.HOUR
			+ TEXT_TYPE
			+ COMMA_SEP
			+ SleepDataDetail.DATA + TEXT_TYPE + ")";
	// 平安积分
	private static final String SQL_CREATE_PINGAN_SCORE = "CREATE TABLE IF NOT EXISTS "
			+ PINGAN_SCORE.TABLE_NAME
			+ " ("
			+ PINGAN_SCORE._ID
			+ " INTEGER PRIMARY KEY,"
			+ PINGAN_SCORE.UID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ PINGAN_SCORE.BOUNDS
			+ TEXT_TYPE
			+ COMMA_SEP
			+ PINGAN_SCORE.DATE
			+ TEXT_TYPE
			+ COMMA_SEP
			+ PINGAN_SCORE.msg
			+ TEXT_TYPE + ")";
	// 体温
	private static final String SQL_CREATE_TEMPERATURE = "CREATE TABLE IF NOT EXISTS "
			+ TEMPERATURE.TABLE_NAME
			+ " ("
			+ TEMPERATURE._ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ TEMPERATURE.UID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ TEMPERATURE.TIME
			+ TEXT_TYPE
			+ COMMA_SEP
			+ TEMPERATURE.DATE
			+ TEXT_TYPE
			+ COMMA_SEP
			+ TEMPERATURE.DATA
			+ BLOB_TYPE
			+ COMMA_SEP
			+ TEMPERATURE.MAX_TEMPE
			+ TEXT_TYPE + ")";
	// 睡眠详细数据 0226
	private static final String SQL_CREATE_SLEEPDETAIL2 = "CREATE TABLE IF NOT EXISTS "
			+ SleepAir0226.TABLE_NAME
			+ " ("
			+ SleepAir0226._ID
			+ " INTEGER PRIMARY KEY,"
			+ SleepAir0226.UID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ SleepAir0226.ACTIVE
			+ TEXT_TYPE
			+ COMMA_SEP
			+ SleepAir0226.BEGIN
			+ TEXT_TYPE
			+ COMMA_SEP
			+ SleepAir0226.END
			+ TEXT_TYPE
			+ COMMA_SEP
			+ SleepAir0226.BOUNDS
			+ TEXT_TYPE
			+ COMMA_SEP
			+ SleepAir0226.COUNT
			+ TEXT_TYPE
			+ COMMA_SEP
			+ SleepAir0226.DEEP
			+ TEXT_TYPE
			+ COMMA_SEP
			+ SleepAir0226.LIGHT
			+ TEXT_TYPE
			+ COMMA_SEP
			+ SleepAir0226.DATE
			+ TEXT_TYPE
			+ COMMA_SEP
			+ SleepAir0226.MSG
			+ TEXT_TYPE
			+ COMMA_SEP
			+ SleepAir0226.DATA
			+ TEXT_TYPE + ")";

	private static final String SQL_DELETE_SLEEPDETAIL = "DROP TABLE IF EXISTS "
			+ SleepDataDetail.TABLE_NAME;
	private static final String SQL_CREATE_AIRRECORDER = "CREATE TABLE IF NOT EXISTS "
			+ AIRRECORDER.TABLE_NAME
			+ " ("
			+ AIRRECORDER._ID
			+ " INTEGER PRIMARY KEY,"
			+ AIRRECORDER.UID
			+ INTEGER_TYPE
			+ COMMA_SEP
			+ AIRRECORDER.ADDRESS
			+ TEXT_TYPE
			+ COMMA_SEP
			+ AIRRECORDER.AIR_VERSION
			+ TEXT_TYPE
			+ COMMA_SEP
			+ AIRRECORDER.APP_VERSION
			+ TEXT_TYPE
			+ COMMA_SEP
			+ AIRRECORDER.DATE
			+ TEXT_TYPE
			+ COMMA_SEP
			+ AIRRECORDER.LIGHTTIME
			+ TEXT_TYPE
			+ COMMA_SEP
			+ AIRRECORDER.MANUFACTURER
			+ TEXT_TYPE
			+ COMMA_SEP
			+ AIRRECORDER.MODE
			+ TEXT_TYPE
			+ COMMA_SEP
			+ AIRRECORDER.LostTime
			+ TEXT_TYPE
			+ COMMA_SEP
			+ AIRRECORDER.ServiceOnCreate
			+ TEXT_TYPE
			+ COMMA_SEP
			+ AIRRECORDER.BATTERY
			+ TEXT_TYPE
			+ COMMA_SEP
			+ AIRRECORDER.VIBRATOR + TEXT_TYPE + ")";

	private static final String SQL_DELETE_AIRRECORDER = "DROP TABLE IF EXISTS "
			+ AIRRECORDER.TABLE_NAME;

	private static final String SQL_AlLTER_USR = "ALTER TABLE "
			+ User.TABLE_NAME + " ADD " + User.LOVEFITID + " varchar(11)";
	private static final String SQL_AlLTER_Sleep = "ALTER TABLE "
			+ SleepAir0226.TABLE_NAME + " ADD " + SleepAir0226.MSG
			+ " varchar(50)";
	private static final String SQL_AlLTER_AirRe = "ALTER TABLE "
			+ AIRRECORDER.TABLE_NAME + " ADD " + AIRRECORDER.BATTERY
			+ " varchar(4)";
	private static final String SQL_AlLTER_SLEEP_DETAIL = "ALTER TABLE "
			+ SleepDataDetail.TABLE_NAME + " ADD " + SleepDataDetail.HOUR
			+ " varchar(4)";
	private static final String SQL_AlLTER_AirRecoder = "ALTER TABLE "
			+ AIRRECORDER.TABLE_NAME + " ADD " + AIRRECORDER.LostTime
			+ " varchar(4)";
	private static final String SQL_AlLTER_AirRecoder2 = "ALTER TABLE "
			+ AIRRECORDER.TABLE_NAME + " ADD " + AIRRECORDER.ServiceOnCreate
			+ " varchar(4)";

	private static final String SQL_AlLTER_PdWgBasic_FAT = "ALTER TABLE "
			+ PdWgBasic.TABLE_NAME + " ADD " + PdWgBasic.FAT + " real";
	private static final String SQL_AlLTER_PdWgBasic_WATER = "ALTER TABLE "
			+ PdWgBasic.TABLE_NAME + " ADD " + PdWgBasic.WATER + " real";
	private static final String SQL_AlLTER_PdWgBasic_MUSCLE = "ALTER TABLE "
			+ PdWgBasic.TABLE_NAME + " ADD " + PdWgBasic.MUSCLE + " real";
	private static final String SQL_AlLTER_PdWgBasic_BONE = "ALTER TABLE "
			+ PdWgBasic.TABLE_NAME + " ADD " + PdWgBasic.BONE + " real";
	private static final String SQL_AlLTER_USER_BIRTH = "ALTER TABLE "
			+ User.TABLE_NAME + " ADD " + User.BIRTH + " varchar(11)";
	private static final String SQL_AlLTER_USER_USERNAME = "ALTER TABLE "
			+ User.TABLE_NAME + " ADD " + User.USERNAME + " varchar(11)";

	public DbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public DbHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	private static DbHelper mInstance;

	public synchronized static DbHelper getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new DbHelper(context);
		}
		return mInstance;
	};

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(SQL_CREATE_USER);
		db.execSQL(SQL_CREATE_DEVICE);
		db.execSQL(SQL_CREATE_TARGET);
		db.execSQL(SQL_CREATE_SPBASIC);
		db.execSQL(SQL_CREATE_WGBASIC);
		db.execSQL(SQL_CREATE_FRIENDRANK);
		db.execSQL(SQL_CREATE_GPSTRACK);
		db.execSQL(SQL_CREATE_GPSPOINTS);
		db.execSQL(SQL_CREATE_SPRAW);

		db.execSQL(SQL_CREATE_CLUBINFO);
		db.execSQL(SQL_CREATE_CLUBDETAIL);
		db.execSQL(SQL_CREATE_MYCLUB);
		db.execSQL(SQL_CREATE_CLUBGROUP);
		db.execSQL(SQL_CREATE_CLUBRANK);
		db.execSQL(SQL_CREATE_CLUBMEMBER);
		db.execSQL(SQL_CREATE_CLUBFEED);
		db.execSQL(SQL_CREATE_CLUBFEEDCOMMENT);

		db.execSQL(SQL_CREATE_PERSONALINFO);
		db.execSQL(SQL_CREATE_TRENDS);
		db.execSQL(SQL_CREATE_COMMENT);
		db.execSQL(SQL_CREATE_FRIENDLIST);
		db.execSQL(SQL_CREATE_MYMATCH);
		db.execSQL(SQL_CREATE_ALLMATCH);
		db.execSQL(SQL_CREATE_MATCHRANK);
		db.execSQL(SQL_CREATE_SEARCHMATCH);
		db.execSQL(SQL_CREATE_MATCHRULES);
		db.execSQL(SQL_CREATE_MATCHMEMBERS);
		db.execSQL(SQL_CREATE_CLUBMATCH);
		db.execSQL(SQL_CREATE_CooperTest);
		db.execSQL(SQL_CREATE_SOFTSTEP);
		db.execSQL(SQL_CREATE_GPSUPLOAD);
		db.execSQL(SQL_CREATE_GPSLASTPOINT);
		// 睡眠
		db.execSQL(SQL_CREATE_SLEEPDATA);
		// 下载
		db.execSQL(SQL_CREATE_DOWNLOAD);

		// 睡眠详细
		db.execSQL(SQL_CREATE_SLEEPDETAIL);
		// air开屏统计
		db.execSQL(SQL_CREATE_AIRRECORDER);
		db.execSQL(SQL_CREATE_AirUpCheck);
		db.execSQL(SQL_CREATE_DIG);
		// 新睡眠
		db.execSQL(SQL_CREATE_SLEEPDETAIL2);
		// 睡眠扣分记录
		db.execSQL(SQL_CREATE_SleepBounds);
		// 平安积分
		db.execSQL(SQL_CREATE_PINGAN_SCORE);
		// 体温记录
		db.execSQL(SQL_CREATE_TEMPERATURE);
		// 白名单
		db.execSQL(SQL_CREATE_NOTIFICATION_LIST);
		try {
			ContentValues values  =new ContentValues();
			values.put(NOTIFICATION_LIST.UID, "");
			values.put(NOTIFICATION_LIST.APPNAME, "微信");
			values.put(NOTIFICATION_LIST.PACKET, "com.tencent.mm");
			values.put(NOTIFICATION_LIST.DISABLE, "1");
			db.insert(NOTIFICATION_LIST.TABLE_NAME, null, values);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 南京公交参数
		db.execSQL(SQL_CREATE_NANJING_CARD_ARGS);
		//通知使用权
		db.execSQL(SQL_CREATE_PERMiSSION);
		//心率
		db.execSQL(SQL_CREATE_HR);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		db.execSQL(SQL_CREATE_SOFTSTEP);
		db.execSQL(SQL_CREATE_GPSUPLOAD);
		// 睡眠
		db.execSQL(SQL_CREATE_SLEEPDATA);
		db.execSQL(SQL_CREATE_GPSLASTPOINT);
		db.execSQL(SQL_CREATE_DOWNLOAD);
		db.execSQL(SQL_CREATE_AirUpCheck);
		try {
			db.execSQL(SQL_CREATE_NOTIFICATION_LIST);
			ContentValues values  =new ContentValues();
			values.put(NOTIFICATION_LIST.UID, "");
			values.put(NOTIFICATION_LIST.APPNAME, "微信");
			values.put(NOTIFICATION_LIST.PACKET, "com.tencent.mm");
			values.put(NOTIFICATION_LIST.DISABLE, "1");
			db.insert(NOTIFICATION_LIST.TABLE_NAME, null, values);
		} catch (SQLException e6) {
			// TODO Auto-generated catch block
			e6.printStackTrace();
		}
		try {
			db.execSQL(SQL_CREATE_NANJING_CARD_ARGS);
		} catch (SQLException e6) {
			// TODO Auto-generated catch block
			e6.printStackTrace();
		}
		try {
			if (oldVersion == 6) {
				db.execSQL(SQL_AlLTER_USR);
			}
		} catch (SQLException e5) {
			// TODO Auto-generated catch block
			e5.printStackTrace();
		}

		try {

			db.execSQL(SQL_AlLTER_Sleep);
		} catch (SQLException e4) {
			// TODO Auto-generated catch block
			e4.printStackTrace();
		}

		try {
			db.execSQL(SQL_AlLTER_USER_BIRTH);
		} catch (SQLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		try {
			// 睡眠数据0226
			db.execSQL(SQL_CREATE_SLEEPDETAIL2);
		} catch (SQLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		try {
			db.execSQL(SQL_AlLTER_USER_USERNAME);
		} catch (SQLException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		try {
			// air开屏统计
			db.execSQL(SQL_CREATE_AIRRECORDER);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			// 增加一列存储电量
			db.execSQL(SQL_AlLTER_AirRe);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// 睡眠详细
		db.execSQL(SQL_CREATE_SLEEPDETAIL);

		try {
			db.execSQL(SQL_AlLTER_SLEEP_DETAIL);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			db.execSQL(SQL_AlLTER_AirRecoder);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			db.execSQL(SQL_AlLTER_AirRecoder2);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			db.execSQL(SQL_CREATE_DIG);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			db.execSQL(SQL_CREATE_SleepBounds);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			// 平安积分
			db.execSQL(SQL_CREATE_PINGAN_SCORE);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			db.execSQL(SQL_AlLTER_PdWgBasic_FAT);
			db.execSQL(SQL_AlLTER_PdWgBasic_WATER);
			db.execSQL(SQL_AlLTER_PdWgBasic_MUSCLE);
			db.execSQL(SQL_AlLTER_PdWgBasic_BONE);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			// 平体温记录
			db.execSQL(SQL_CREATE_TEMPERATURE);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			//通知使用权
			db.execSQL(SQL_CREATE_PERMiSSION);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			//心率
			db.execSQL(SQL_CREATE_HR);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);

	}

}

package com.bandlink.air.util;

import android.provider.BaseColumns;

public final class DbContract {
	public DbContract() {
	}

	// 用户信息表
	public static abstract class User implements BaseColumns {
		public static final String TABLE_NAME = "userprofile";
		public static final String UID = "uid";
		public static final String ISMEMBER = "ismember";// 是否注册过，0没有 ;1有
		public static final String NICKNAME = "nickname";
		public static final String CONTACT = "contact";
		public static final String SEX = "sex";// 性别 男1女2
		public static final String HEIGHT = "height";
		public static final String WEIGHT = "weight";
		public static final String EMAIL = "email";
		public static final String LOVEFITID = "lovefit_id";
		public static final String BIRTH = "birth";
		public static final String USERNAME = "username";
	}

	// 点赞表
	public static abstract class Dig implements BaseColumns {
		public static final String TABLE_NAME = "dig";
		public static final String UID = "uid";
		public static final String feed_id = "feed_id";
		public static final String is_ok = "is_ok";// 0表示未点赞，1表示点赞
	}

	// 朋友圈个人信息表
	public static abstract class PersonalInfo implements BaseColumns {
		public static final String TABLE_NAME = "personal_info";
		public static final String UID = "uid";
		public static final String NAME = "name";
		public static final String MESSAGE = "message";
		public static final String PHOTO = "photo";
		public static final String DONGTAINUM = "dongtainum";
		public static final String FRIENDNUM = "friendnum";
		public static final String GUANZHUNUM = "guanzhunum";
		public static final String FANSNUM = "fansnum";
		public static final String ADDFRIENDNUM = "addfriendnum";

	}

	// 朋友圈好友列表
	public static abstract class FriendList implements BaseColumns {
		public static final String TABLE_NAME = "friend";
		public static final String FUID = "fuid";
		public static final String NICKNAME = "nickname";
		public static final String GID = "gid";
		public static final String PHOTO = "photo";

	}

	// 设备表
	public static abstract class Device implements BaseColumns {
		public static final String TABLE_NAME = "deviceinfo";
		public static final String UID = "uid";
		public static final String DEVICESP_TYPE = "devicetype";// (0手机软计步1->air,2->flame,3->ant)
		public static final String DEVICEWG_TYPE = "weighttype";// (0手动输入)
		public static final String DEVICESP_ID = "did";
		public static final String DEVICEWG_ID = "wid";
	}

	// 朋友圈排名表
	public static abstract class Friendrank implements BaseColumns {
		public static final String TABLE_NAME = "frank ";
		public static final String USER = "user";
		public static final String UID = "uid";
		public static final String STEP = "step";
		public static final String DATE = "date";
		public static final String GID = "gid";
		public static final String RANK_TYPE = "ranktype";// 1表示天，2表示周，3表示月
		public static final String UPDATETIME = "updatetime";
		public static final String URL = "url";

	}

	// 运动目标表
	public static abstract class Target implements BaseColumns {
		public static final String TABLE_NAME = "usertarget ";
		public static final String UID = "uid";
		public static final String TARGET_TYPE = "appmode";// 0表示瘦身，1表示健体
		public static final String TARGET_WEIGHT_BEFORE = "bweight";
		public static final String TARGET_WEIGHT_END = "eweight";
		public static final String TARGET_STEP = "stepgoal";

		public static final String TARGET_WEIGHT = "weightmode";// 0表示不选中，1表示选中(是否选中体重)
		public static final String TARGET_BMI = "bmimode";// 0表示不选中，1表示选中(是否选中bmi)
		public static final String TARGET_SLEEP = "sleepmode";// 0表示不选中，1表示选中(是否显示睡眠)
	}

	// 运动基本数据表
	public static abstract class PdSpBasic implements BaseColumns {
		public static final String TABLE_NAME = "step";
		public static final String UID = "uid";
		public static final String DATE = "date";
		public static final String STEP = "step";
		public static final String DISTANCE = "distance";
		public static final String CALORIE = "calorie";
		public static final String LVL = "lvl";
		public static final String TYPE = "type";
		public static final String UPLOAD = "upload";
		public static final String UPDATETIME = "updatetime";

	}

	// 运动原始数据
	// sport
	public static abstract class PdSpRaw implements BaseColumns {
		public static final String TABLE_NAME = "stepraw";
		public static final String UID = "uid";
		public static final String DATE = "date";
		public static final String HOUR = "hour";
		public static final String DATA = "data";
		public static final String TYPE = "type";
		public static final String UPLOAD = "upload";
		public static final String UPDATETIME = "updatetime";

	}

	// club info
	public static abstract class ClubInfo implements BaseColumns {
		public static final String TABLE_NAME = "clubinfo";
		public static final String CLUBID = "clubid";
		public static final String NAME = "clubname";
		public static final String MEMBERNUM = "membernum";
		public static final String ISPUBLIC = "ispublic";
		public static final String PROVINCE = "province";
		public static final String CITY = "city";
		public static final String DISTRICT = "district";
		public static final String INDEX = "page";
		public static final String LOGO = "logo";
		public static final String UID = "uid";
	}

	// club detail
	public static abstract class ClubDetail implements BaseColumns {
		public static final String TABLE_NAME = "clubdetail";
		public static final String CLUBID = "clubid";
		public static final String NAME = "clubname";
		public static final String MEMBERNUM = "membernum";
		public static final String ISPUBLIC = "ispublic";
		public static final String PROVINCE = "province";
		public static final String CITY = "city";
		public static final String DISTRICT = "district";
		public static final String GROUPNUM = "groupnum";
		public static final String ADMIN_NAME = "admin_username";
		public static final String INTRO = "intro";
		public static final String ADMIN_UID = "admin_uid";
		public static final String LOGO = "logo";
		public static final String UID = "uid";
	}

	// 体重基本数据表
	public static abstract class PdWgBasic implements BaseColumns {
		public static final String TABLE_NAME = "weight";
		public static final String UID = "uid";
		public static final String DATE = "date";
		public static final String WEIGHT = "weight";
		public static final String BMI = "bmi";

		public static final String FAT = "fat";
		public static final String WATER = "water";
		public static final String MUSCLE = "muscle";
		public static final String BONE = "bone";

		public static final String UPDATETIME = "updatetime";
		public static final String TYPE = "type";
	}

	// gps Track 轨迹表
	public static abstract class GPSTrack implements BaseColumns {
		public static final String TABLE_NAME = "track";
		public static final String UID = "uid";
		public static final String DATE = "date";// 轨迹时间
		public static final String STEP = "step";// 步数
		public static final String CALORIE = "calorie";// 卡路里
		public static final String DISTANCE = "distance";// 距离
		public static final String DURATION = "duration";// 间隔时间
		public static final String POINTS = "points";// 轨迹点数
	}

	// gps Track 轨迹表
	public static abstract class GPSPoints implements BaseColumns {
		public static final String TABLE_NAME = "points";
		public static final String TID = "tid";// 对应的track的id
		public static final String TIME = "time";// 轨迹时间
		public static final String LATITUDE = "latitude";// 纬度
		public static final String lONGTITUDE = "longitude";// 经度
		public static final String SPEED = "speed";// 速度
		public static final String ACCURACY = "accuracy";// 精度
	}

	// myclub
	public static abstract class MyClub implements BaseColumns {
		public static final String TABLE_NAME = "myclub";
		public static final String CLUBID = "clubid";
		public static final String NAME = "clubname";
		public static final String MEMBERNUM = "membernum";
		public static final String ISPUBLIC = "ispublic";
		public static final String PROVINCE = "province";
		public static final String CITY = "city";
		public static final String DISTRICT = "district";
		public static final String LOGO = "logo";
		public static final String UID = "uid";

	}

	// 评论表
	public static abstract class Comment implements BaseColumns {
		public static final String TABLE_NAME = "comment";
		public static final String UID = "uid";
		public static final String TO_UID = "to_uid";
		public static final String FEED_ID = "feed_id";
		public static final String NAME = "name";
		public static final String CONTENT = "content";
		public static final String TIME = "time";
	}

	// club group
	public static abstract class ClubGroup implements BaseColumns {
		public static final String TABLE_NAME = "clubgroup";
		public static final String CLUBID = "clubid";
		public static final String NAME = "groupname";
		public static final String GROUPID = "groupid";
		public static final String UID = "uid";

	}

	// club rank
	public static abstract class ClubRank implements BaseColumns {
		public static final String TABLE_NAME = "clubrank";
		public static final String CLUBID = "clubid";
		public static final String USERNAME = "username";
		public static final String STEP = "step";
		public static final String UID = "uid";
		public static final String TIME = "time";
		public static final String GID = "gid";
		public static final String PHOTO = "photo";
		public static final String RANKUID = "rankuid";
	}

	// club member
	public static abstract class ClubMember implements BaseColumns {
		public static final String TABLE_NAME = "clubmember";
		public static final String CLUBID = "clubid";
		public static final String USERNAME = "username";
		public static final String USERID = "userid";
		public static final String UID = "uid";
		public static final String ADMIN = "isadmin";
		public static final String GID = "gid";
		public static final String PHOTO = "photo";
		public static final String DEPARTNAME = "departname";

	}

	// club feed
	public static abstract class ClubFeed implements BaseColumns {
		public static final String TABLE_NAME = "clubfeed";
		public static final String CLUBID = "clubid";
		public static final String FEEDID = "feedid";
		public static final String USERID = "userid";
		public static final String UID = "uid";
		public static final String CONTENT = "content";
		public static final String TIME = "time";
	}

	// club feed comment
	public static abstract class ClubFeedComment implements BaseColumns {
		public static final String TABLE_NAME = "clubfeedcomment";
		public static final String CLUBID = "clubid";
		public static final String FEEDID = "feedid";
		public static final String UID = "uid";
		public static final String CONTENT = "content";
		public static final String COMMENTID = "commentid";

	}

	// 好友动态数据表
	public static abstract class Trends implements BaseColumns {
		public static final String TABLE_NAME = "trends";
		public static final String UID = "uid";
		public static final String APP_ROW_ID = "app_row_id";
		public static final String FEED_ID = "feed_id";
		public static final String TYPE = "type";
		public static final String PUBLISH_TIME = "publish_time";
		public static final String DIGG_COUNT = "digg_count";
		public static final String COMMENT_ALL_COUNT = "comment_all_count";
		public static final String REPOST_COUNT = "repost_count";
		public static final String COMMENT_COUNT = "comment_count";
		public static final String IS_REPOST = "is_repost";
		public static final String IS_AUDIT = "is_audit";
		public static final String FEED_CONTENT = "feed_content";
		public static final String PHOTO = "photo";
		public static final String BIAOQING = "biaoqing";
		public static final String TIME = "time";
		public static final String NAME = "name";

		public static final String DA_CONTENT = "da_content";
		public static final String DA_BODY = "da_body";
		public static final String DA_SOURCE_URL = "da_source_url";
		public static final String DA_UID = "da_uid";
		public static final String DA_APP = "da_app";
		public static final String DA_TYPE = "da_type";
		public static final String DA_APP_ROW_ID = "da_app_row_id";
		public static final String DA_APP_ROW_TABLE = "app_row_table";
		public static final String DA_PUBLISH_TIME = "da_publish_time";
		public static final String DA_FROM = "da_from";
		public static final String DA_REPOST_COUNT = "da_repost_count";
		public static final String DA_COMMENT_COUNT = "da_comment_count";
		public static final String DA_IS_DEL = "da_is_del";
		public static final String DA_IS_REPOST = "da_is_repost";
		public static final String DA_ID_AUDIT = "da_is_audit";

	}

	// 我的match
	public static abstract class MyMatch implements BaseColumns {
		public static final String TABLE_NAME = "mymatch";
		public static final String MATCH_ID = "matchid";
		public static final String LOGO = "logo";
		public static final String UID = "uid";
		public static final String USERNAME = "username";
		public static final String MATCH_TITLE = "title";
		public static final String MEMBER_NUM = "membernum";
		public static final String STARTTIME = "starttime";
		public static final String ENDTIME = "endtime";
		public static final String STATUS = "status";
		public static final String ISPUBLIC = "ispublic";
		public static final String TARGET = "target";
		public static final String ISMEMBER = "ismember";

	}

	// 全部竞赛
	public static abstract class AllMatch implements BaseColumns {
		public static final String TABLE_NAME = "allmatch";
		public static final String MATCH_ID = "matchid";
		public static final String LOGO = "logo";
		public static final String UID = "uid";
		public static final String USERNAME = "username";
		public static final String MATCH_TITLE = "title";
		public static final String MEMBER_NUM = "membernum";
		public static final String STARTTIME = "starttime";
		public static final String ENDTIME = "endtime";
		public static final String STATUS = "status";
		public static final String ISPUBLIC = "ispublic";
		public static final String TARGET = "target";
		public static final String ISMEMBER = "ismember";

	}

	// 搜索的竞赛
	public static abstract class SearchMatch implements BaseColumns {
		public static final String TABLE_NAME = "searchmatch";
		public static final String MATCH_ID = "matchid";
		public static final String LOGO = "logo";
		public static final String UID = "uid";
		public static final String USERNAME = "username";
		public static final String MATCH_TITLE = "title";
		public static final String MEMBER_NUM = "membernum";
		public static final String STARTTIME = "starttime";
		public static final String ENDTIME = "endtime";
		public static final String STATUS = "status";
		public static final String ISPUBLIC = "ispublic";
		public static final String TARGET = "target";
		public static final String ISMEMBER = "ismember";

	}

	// 竞赛排名
	public static abstract class MatchRank implements BaseColumns {
		public static final String TABLE_NAME = "matchrank";
		public static final String MATCH_ID = "matchid";
		public static final String AVATAR = "avatar";
		public static final String UID = "uid";
		public static final String USERNAME = "username";
		public static final String STEP = "step";
		public static final String TIME = "time";
	}

	// 竞赛规则
	public static abstract class MatchRules implements BaseColumns {
		public static final String TABLE_NAME = "matchrules";
		public static final String MATCH_ID = "matchid";
		public static final String MATCH_PICS = "pics";
		public static final String MATCH_CONTENTS = "contents";
	}

	// 竞赛成员
	public static abstract class MatchMembers implements BaseColumns {
		public static final String TABLE_NAME = "matchmembers";
		public static final String MATCH_ID = "matchid";
		public static final String AVATAR = "avatar";
		public static final String UID = "uid";
		public static final String USERNAME = "username";
	}

	// 俱乐部存储的竞赛
	public static abstract class ClubMatch implements BaseColumns {
		public static final String TABLE_NAME = "clubmatch";
		public static final String MATCH_ID = "matchid";
		public static final String CLUB_ID = "clubid";
		public static final String LOGO = "logo";
		public static final String UID = "uid";
		public static final String USERNAME = "username";
		public static final String MATCH_TITLE = "title";
		public static final String MEMBER_NUM = "membernum";
		public static final String STARTTIME = "starttime";
		public static final String ENDTIME = "endtime";
		public static final String STATUS = "status";
		public static final String ISPUBLIC = "ispublic";
		public static final String TARGET = "target";
		public static final String ISMEMBER = "ismember";

	}

	// 强身健体数据
	public static abstract class CooperTest implements BaseColumns {
		public static final String TABLE_NAME = "coopertest";
		public static final String STEP = "step";
		public static final String DURATION = "duration";
		public static final String TIME = "time";
		public static final String DISTANCE = "distance";
		public static final String CAL = "calre";
		public static final String UID = "uid";
		public static final String SCORE = "score";// 0 很差 ，1差，2及格，3好，4极好

	}

	// 睡眠数据表
	public static abstract class SleepData implements BaseColumns {
		public static final String TABLE_NAME = "sleepdata";
		public static final String UID = "uid";
		public static final String DATE = "date";
		public static final String SCORE = "score";
		public static final String TOTAL = "total";
		public static final String DEEP = "deep";
		public static final String LIGHT = "light";
		public static final String LUCID = "lucid";
		public static final String ISUPLOAD = "isupload";
	}

	// 睡眠详细数据
	public static abstract class SleepDataDetail implements BaseColumns {
		public static final String TABLE_NAME = "sleepdatadetail";
		public static final String UID = "uid";
		public static final String TIME = "time";
		public static final String DATA = "data";
		public static final String HOUR = "hour";
	}

	// 记录软计步的实时数据
	public static abstract class SOFTSTEP implements BaseColumns {
		public static final String TABLE_NAME = "softstep";
		public static final String STEP_TODAY = "step_today";
		public static final String CAL_TODAY = "cal_today";
		public static final String SAVETIME = "savetime";

	}

	public static abstract class GPSUPLOAD implements BaseColumns {
		public static final String TABLE_NAME = "gpsupload";
		public static final String UID = "uid";
		public static final String ISUPLOAD = "isupload";
		public static final String IID = "iid";
	}

	public static abstract class GPSLASTPOINT implements BaseColumns {
		public static final String TABLE_NAME = "gpslastpoint";
		public static final String LATITUDE = "latitude";
		public static final String LONGITUDE = "longitude";
	}

	public static abstract class DOWNLOADLOG implements BaseColumns {
		public static final String TABLE_NAME = "downloadlog";
		public static final String UID = "uid";
		public static final String AIMDATE = "aimdate";
		public static final String NOWDATE = "nowdate";
	}

	public static abstract class AIRRECORDER implements BaseColumns {
		public static final String TABLE_NAME = "airrecorder";
		public static final String UID = "uid";// 用户id
		public static final String ADDRESS = "address";// air物理地址
		public static final String DATE = "date";// 日期
		public static final String MANUFACTURER = "manufacturer";// 手机厂商_手机型号
		public static final String MODE = "mode";// 原为手机mode，后存did
		public static final String AIR_VERSION = "airversion";// air固件版本
		public static final String APP_VERSION = "appversion";// app版本
		public static final String VIBRATOR = "vibratortime";// 振动时间（s）
		public static final String LIGHTTIME = "lighttime";// 开屏时间（s）
		public static final String LostTime = "losttimes";// 报警次数
		public static final String ServiceOnCreate = "serviceoncreate";// 服务启动次数
		public static final String BATTERY = "battery";// 存储时的air电量
	}

	public static abstract class AirUpCheck implements BaseColumns {
		public static final String TABLE_NAME = "airupcheck";
		public static final String UID = "uid";// 用户id
		public static final String DATE = "date";// 日期
		public static final String STATUS = "status";// 是否上传

	}

	public static abstract class SleepAir0226 implements BaseColumns {
		public static final String TABLE_NAME = "sleep0226";
		public static final String UID = "uid";// 用户id
		public static final String DATE = "date";// 日期
		public static final String DATA = "data";// 睡眠数据
		public static final String BEGIN = "begin";// 开始时间
		public static final String END = "end";// 结束时间
		public static final String BOUNDS = "bounds";// 得分
		public static final String LIGHT = "light";// 浅睡眠
		public static final String DEEP = "deep";// 深睡眠
		public static final String ACTIVE = "active";// 清醒时间
		public static final String COUNT = "count";// 总时长
		public static final String MSG = "msg";// 描述

	}

	// 3-19 告知用户因为什么扣分了
	public static abstract class SleepBoundsDetail implements BaseColumns {
		public static final String TABLE_NAME = "sleepboundsdetail";
		public static final String UID = "uid";// 用户id
		public static final String DATE = "date";// 日期
		public static final String INTRO = "intro";// 扣分说明
		public static final String BOUNDS = "bounds";// 扣分数量
		public static final String SAVETime = "stime";// 保存时的mm时间
	}

	// 04-28
	public static abstract class PINGAN_SCORE implements BaseColumns {
		public static final String TABLE_NAME = "pinganscore";
		public static final String UID = "uid";// 用户id
		public static final String DATE = "date";// 日期
		public static final String BOUNDS = "bounds";// 扣分数量
		public static final String msg = "msg";// 保存时的mm时间
	}

	// 0526
	public static abstract class TEMPERATURE implements BaseColumns {
		public static final String TABLE_NAME = "temperature";
		public static final String UID = "uid";// 用户id
		public static final String DATE = "date";// 日期
		public static final String TIME = "time";// 时间值
		public static final String DATA = "msg";// 保存时的mm时间
		public static final String MAX_TEMPE = "max";// 保存时的mm时间
	}

	// 0818
	public static abstract class NOTIFICATION_LIST implements BaseColumns {
		public static final String TABLE_NAME = "notification_list";
		public static final String UID = "uid";// 用户id
		public static final String APPNAME = "appname";// 程序名
		public static final String PACKET = "packetname";// 包名
		public static final String DISABLE = "disabled";// 为 1不提醒
	}

	// 0923 南京公交
	public static abstract class NANJING_CARD_ARGS implements BaseColumns {
		public static final String TABLE_NAME = "nanjingcard";
		public static final String UID = "uid";// 用户id
		public static final String MAC = "mac";// 程序名
		public static final String HEXUID = "packetname";// 包名
		public static final String BD01 = "bd01";//
		public static final String BD05 = "bd05";//
		public static final String BD0E = "bd14";//
		public static final String BD0F = "bd15";//
		public static final String CHENGEID = "chid";//
		public static final String CARD = "card";//
	}
	// 1210
	public static abstract class HR_RECORD implements BaseColumns {
		public static final String TABLE_NAME = "tb_hr";
		public static final String UID = "uid";// 用户id  
		public static final String DATA = "value";//
		public static final String DATE = "date";// 
		public static final String UPLOAD = "isupload";//
	}
	
	public static  abstract class PermissionNotification implements BaseColumns {
		public static final String TABLE_NAME = "permissionnotification"; 
		public static final String ENABLE = "enabled";// 是否有权限
	}

}

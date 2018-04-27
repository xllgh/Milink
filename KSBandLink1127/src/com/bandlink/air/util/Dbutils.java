package com.bandlink.air.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.baidu.mapapi.model.LatLng;
import com.bandlink.air.MyLog;
import com.bandlink.air.R;
import com.bandlink.air.ble.Converter;
import com.bandlink.air.gps.GPSEntity;
import com.bandlink.air.gps.GPSPointEntity;
import com.bandlink.air.simple.AllRankActivity;
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

public class Dbutils {
	private int uid;
	DbHelper dbHelper;
	Context context;
	SimpleDateFormat sdf;

	public Dbutils(int uid, Context context) {
		this.uid = uid;
		this.context = context;
		dbHelper = DbHelper.getInstance(context);
		sdf = new SimpleDateFormat("yyyy-MM-dd");
	}

	public Dbutils(Context c) {
		SharedPreferences share = c.getSharedPreferences(
				SharePreUtils.APP_ACTION, Context.MODE_PRIVATE);
		this.uid = share.getInt("UID", -1);

		this.context = c;
		dbHelper = DbHelper.getInstance(context);
		sdf = new SimpleDateFormat("yyyy-MM-dd");
	}

	public void saveNotificationPermission(boolean open) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(PermissionNotification.ENABLE, (open ? 1 : 0) + "");
		int row = database.update(PermissionNotification.TABLE_NAME, values,
				" " + PermissionNotification.ENABLE + "='"
						+ (1 - (open ? 1 : 0)) + "'", null);
		if (row < 1) {
			long x = database.insert(PermissionNotification.TABLE_NAME, null,
					values);
			System.out.println(row);
		}
	}

	public boolean hasNotificationPermission() {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.rawQuery("select * from "
				+ PermissionNotification.TABLE_NAME + " where "
				+ PermissionNotification.ENABLE + "='1'", null);
		if (cursor.moveToNext()) {
			cursor.close();
			return true;
		} else {
			cursor.close();
			return false;
		}
	}

	public void saveNanjingArgs(String hexuid, String cid, String bd01,
			String bd05, String bd0e, String bd0f, String mac, String card) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(NANJING_CARD_ARGS.BD01, bd01);
		values.put(NANJING_CARD_ARGS.BD05, bd05);
		values.put(NANJING_CARD_ARGS.BD0E, bd0e);
		values.put(NANJING_CARD_ARGS.BD0F, bd0f);
		values.put(NANJING_CARD_ARGS.HEXUID, hexuid);
		values.put(NANJING_CARD_ARGS.CHENGEID, cid);
		values.put(NANJING_CARD_ARGS.MAC, mac);
		values.put(NANJING_CARD_ARGS.CARD, card);
		int row = database.update(NANJING_CARD_ARGS.TABLE_NAME, values,
				NANJING_CARD_ARGS.MAC + " ='" + mac + "' ", null);
		if (row < 1) {
			database.insert(NANJING_CARD_ARGS.TABLE_NAME, null, values);
		}
	}

	public void removeNanjingArgs(String mac) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.delete(NANJING_CARD_ARGS.TABLE_NAME, NANJING_CARD_ARGS.MAC
				+ "='" + mac + "'", null);
	}

	public HashMap<String, String> getNanJingArgs(String mac) {
		HashMap<String, String> map = null;
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		Cursor cursor = database.rawQuery("select * from "
				+ NANJING_CARD_ARGS.TABLE_NAME + " where "
				+ NANJING_CARD_ARGS.MAC + "='" + mac + "' ", null);
		while (cursor.moveToNext()) {
			map = new HashMap<String, String>();
			map.put(NANJING_CARD_ARGS.BD01, cursor.getString(cursor
					.getColumnIndex(NANJING_CARD_ARGS.BD01)));
			map.put(NANJING_CARD_ARGS.BD05, cursor.getString(cursor
					.getColumnIndex(NANJING_CARD_ARGS.BD05)));
			map.put(NANJING_CARD_ARGS.BD0E, cursor.getString(cursor
					.getColumnIndex(NANJING_CARD_ARGS.BD0E)));
			map.put(NANJING_CARD_ARGS.BD0F, cursor.getString(cursor
					.getColumnIndex(NANJING_CARD_ARGS.BD0F)));
			map.put(NANJING_CARD_ARGS.CHENGEID, cursor.getString(cursor
					.getColumnIndex(NANJING_CARD_ARGS.CHENGEID)));
			map.put(NANJING_CARD_ARGS.HEXUID, cursor.getString(cursor
					.getColumnIndex(NANJING_CARD_ARGS.HEXUID)));
			map.put(NANJING_CARD_ARGS.CARD, cursor.getString(cursor
					.getColumnIndex(NANJING_CARD_ARGS.CARD)));
		}
		return map;
	}

	public void UpdateUid() {
		if (context != null) {
			SharedPreferences share = context.getSharedPreferences(
					SharePreUtils.APP_ACTION, Context.MODE_PRIVATE);
			this.uid = share.getInt("UID", -1);
		}
	}

	public SQLiteDatabase beginTransaction() {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.beginTransaction();// 开始事务
		return database;
	}

	public void setTransactionSuccessful(SQLiteDatabase database) {

		database.setTransactionSuccessful();
	}

	public void endTransaction(SQLiteDatabase database) {
		database.endTransaction();
		// database.close();
	}

	public void saveHr(String date, byte[] data) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		Cursor cc = database.rawQuery("select " + HR_RECORD.DATA + " from "
				+ HR_RECORD.TABLE_NAME + " where " + HR_RECORD.DATE + "='"
				+ date + "'", null);
		if (cc.moveToNext()) {
			byte[] x = cc.getBlob(0);
			byte[] temp = new byte[24];
			if (x.length == 24) {
				for (int i = 0; i < 24; i++) {
					temp[i] = (byte) (data[i] & x[i]);
				}
				data = temp;
			}
		}
		ContentValues values = new ContentValues();
		values.put(HR_RECORD.DATA, data);
		values.put(HR_RECORD.DATE, date);
		values.put(HR_RECORD.UPLOAD, 0);
		values.put(HR_RECORD.UID, uid);
		int rows = database.update(HR_RECORD.TABLE_NAME, values, HR_RECORD.DATE
				+ "=? ", new String[] { date });
		if (rows == 0) {
			database.insert(HR_RECORD.TABLE_NAME, null, values);
		}
	}

	public class HR {
		public String[] value = new String[24];
		public String date;
		public int isupload;
		public String avgHr;
	}

	public ArrayList<HR> getHr(String start, String end) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.rawQuery("select * from "
				+ HR_RECORD.TABLE_NAME + " where " + HR_RECORD.DATE
				+ " between '" + start + "' and '" + end + "' order by "+HR_RECORD.DATE+" asc", null);
		ArrayList<HR> arr = new ArrayList<Dbutils.HR>();
		while (cursor.moveToNext()) {
			HR hr = new HR();
			byte[] value = cursor
					.getBlob(cursor.getColumnIndex(HR_RECORD.DATA));
			int avg = -1;
			int count = 0;
			for (int i = 0; i < value.length; i++) {

				if (((value[i] & 0xff) == 0xff)||((value[i] & 0xff) == 0x00)) {
					hr.value[i] = "null";
				} else {
					if (avg == -1) {
						avg = (value[i] & 0xff);
					} else {
						count ++;
						avg += (value[i] & 0xff);
						//avg = Math.round(avg / 2f);
					}
					hr.value[i] = (value[i] & 0xff) + "";
				}
			}
			hr.date = cursor.getString(cursor.getColumnIndex(HR_RECORD.DATE));
			hr.isupload = cursor
					.getInt(cursor.getColumnIndex(HR_RECORD.UPLOAD));
			hr.avgHr = Math.round(avg/(float)(count+1)) + "";
			arr.add(hr);
		}
		return arr;
	}

	public void DeleteComment(int feed_id) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		try {
			if (Comment.TABLE_NAME != null) {
				database.execSQL("delete from " + Comment.TABLE_NAME
						+ " where feed_id=?", new String[] { feed_id + "" });
			}
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.e("delete Comment error", e.toString());
		}
	}

	public void SaveComment(int uid, String name, String content, int to_uid,
			int feed_id, String time) {

		SQLiteDatabase database = dbHelper.getWritableDatabase();
		try {
			database.execSQL("insert into " + Comment.TABLE_NAME
					+ "(uid,name,content,to_uid,feed_id,time)"
					+ "values( ?,?,?,?,?,?)", new Object[] { uid, name,
					content, to_uid, feed_id, time });

		} catch (Exception e) {
			// TODO: handle exception
			MyLog.e("kkkkkk", e.toString());
		}

	}

	public ArrayList<HashMap<String, String>> getComment(int feed_id) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		ArrayList<HashMap<String, String>> a;
		a = new ArrayList<HashMap<String, String>>();
		Cursor cursor = null;
		try {
			cursor = database.rawQuery("select * from " + Comment.TABLE_NAME
					+ " where feed_id=?", new String[] { feed_id + "" });

			if (cursor != null && cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					HashMap<String, String> map = new HashMap<String, String>();
					map.put("id", cursor.getInt(0) + "");
					map.put("uid", cursor.getInt(1) + "");
					map.put("name", cursor.getString(2) + "");
					map.put("content", cursor.getString(3) + "");
					map.put("to_uid", cursor.getString(4) + "");
					a.add(map);
				}
				;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return a;
	}

	public void SaveTrendsData(int uid, int feed_id, int digg_count,
			int comment_all_count, String photo) {

		SQLiteDatabase database = dbHelper.getWritableDatabase();
		try {
			database.execSQL("insert into " + Trends.TABLE_NAME
					+ "(uid, feed_id ,digg_count,comment_count ,photo)"
					+ "values( ?,?,?,?,?)", new Object[] { uid, feed_id,
					digg_count, comment_all_count, photo });

		} catch (Exception e) {
			// TODO: handle exception
			MyLog.v("kkkkkk", e.toString());
		}

	}

	public void UpdateDongTai(int digg_count, int feed_id, int uid) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.execSQL("update " + Trends.TABLE_NAME + " set "
				+ Trends.DIGG_COUNT + "=? " + " where " + Trends.UID + " =? "
				+ " and " + Trends.FEED_ID + " =?", new Object[] { digg_count,
				feed_id, uid });

	}

	// 查询点赞的数据
	public int getDig(String feed_id) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.rawQuery("select * from " + Dig.TABLE_NAME
				+ " where feed_id=?", new String[] { feed_id });
		int s = 0;
		if (cursor.moveToFirst()) {
			try {
				s = cursor.getInt(cursor.getColumnIndex("is_ok"));
				return s;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		cursor.close();
		return s;
	}

	// 插入点赞数据
	public void InsertDig(int uid, String feed_id, int isdig) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		try {
			database.execSQL("insert into " + Dig.TABLE_NAME
					+ "(uid, feed_id, is_ok)" + "values( ?,?,?)", new Object[] {
					uid, feed_id, isdig });
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.v("insert into dig", e.toString());
		}
	}

	public void DeleteFriendList() {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		try {
			if (Comment.TABLE_NAME != null) {
				database.execSQL("delete from " + Comment.TABLE_NAME);
			}
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.e("delete friendlist error", e.toString());
		}
	}

	public void InitFriendList(int fuid, String nickname, int gid, String photo) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		try {
			database.execSQL("insert into " + FriendList.TABLE_NAME
					+ "(fuid, nickname, gid, photo)" + "values( ?,?,?,?)",
					new Object[] { fuid, nickname, gid, photo });
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.v("Initfriiendlist", e.toString());
		}
	}

	// 保存朋友圈中的个人信息
	public void InitFriendInfo(int uid, String name, String message,
			String photo, int dongtainum, int friendnum, int fansnum,
			int guanzhunum, int addfriendnum) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		if (getPersonalInfo(uid) != null) {
			database.execSQL("delete from " + PersonalInfo.TABLE_NAME);
			database.execSQL("update " + PersonalInfo.TABLE_NAME + " set "
					+ PersonalInfo.NAME + "=? ," + PersonalInfo.MESSAGE
					+ "=? ," + PersonalInfo.PHOTO + "=? ,"
					+ PersonalInfo.ADDFRIENDNUM + "=? ,"
					+ PersonalInfo.DONGTAINUM + "=? ," + PersonalInfo.FRIENDNUM
					+ "=? ," + PersonalInfo.GUANZHUNUM + "=? ,"
					+ PersonalInfo.FANSNUM + "=? ," + PersonalInfo.ADDFRIENDNUM
					+ "=? " + " where " + PersonalInfo.UID + " =?",
					new Object[] { name, message, photo, dongtainum, friendnum,
							guanzhunum, fansnum, addfriendnum, uid });
		} else {
			ContentValues values = new ContentValues();
			values.put(PersonalInfo.UID, uid);
			values.put(PersonalInfo.NAME, name);
			values.put(PersonalInfo.MESSAGE, message);
			values.put(PersonalInfo.PHOTO, photo);
			values.put(PersonalInfo.DONGTAINUM, dongtainum);
			values.put(PersonalInfo.FRIENDNUM, friendnum);
			values.put(PersonalInfo.GUANZHUNUM, guanzhunum);
			values.put(PersonalInfo.FANSNUM, fansnum);
			values.put(PersonalInfo.ADDFRIENDNUM, addfriendnum);

			try {
				database.insert(PersonalInfo.TABLE_NAME, null, values);
			} catch (Exception e) {
				// TODO: handle exception
				MyLog.v("hhh", e.toString());
			}
		}
	}

	// 获得朋友圈中个人信息
	public Object[] getPersonalInfo(int uid) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.rawQuery("select * from "
				+ PersonalInfo.TABLE_NAME + " where uid=?", new String[] { uid
				+ "" });
		Object[] oj = null;
		if (cursor.moveToNext()) {
			int id = cursor.getCount() == 1 ? cursor.getInt(0) : -1;
			if (id >= 0) {
				oj = new Object[] { cursor.getInt(0), cursor.getInt(1),
						cursor.getString(2), cursor.getString(3),
						cursor.getString(4), cursor.getInt(5),
						cursor.getInt(6), cursor.getInt(7), cursor.getInt(8),
						cursor.getInt(9) };
			}
		}
		cursor.close();
		return oj;
	}

	// 更新朋友圈个人信息
	public void UpdatePersonalInfo(String name, String message, String photo,
			int dongtainum, int friendnum, int guanzhunum, int fansnum,
			int addfriendnum, int uid) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		Object[] finfo = getPersonalInfo(uid);
		int id = (Integer) finfo[0];
		if (id > 0) {
			database.execSQL("update " + PersonalInfo.TABLE_NAME + " set "
					+ PersonalInfo.NAME + "=? ," + PersonalInfo.MESSAGE
					+ "=? ," + PersonalInfo.PHOTO + "=? ,"
					+ PersonalInfo.ADDFRIENDNUM + "=? ,"
					+ PersonalInfo.DONGTAINUM + "=? ," + PersonalInfo.FRIENDNUM
					+ "=? ," + PersonalInfo.GUANZHUNUM + "=? ,"
					+ PersonalInfo.FANSNUM + "=? ," + PersonalInfo.ADDFRIENDNUM
					+ "=? " + "where " + PersonalInfo.UID + " =?",
					new Object[] { name, message, photo, dongtainum, friendnum,
							guanzhunum, fansnum, addfriendnum, uid });
		} else {
			ContentValues values = new ContentValues();
			values.put(PersonalInfo.UID, uid);
			values.put(PersonalInfo.NAME, name);
			values.put(PersonalInfo.MESSAGE, message);
			values.put(PersonalInfo.PHOTO, photo);
			values.put(PersonalInfo.DONGTAINUM, dongtainum);
			values.put(PersonalInfo.FRIENDNUM, friendnum);
			values.put(PersonalInfo.GUANZHUNUM, guanzhunum);
			values.put(PersonalInfo.FANSNUM, fansnum);
			values.put(PersonalInfo.ADDFRIENDNUM, addfriendnum);
			try {
				database.insert(PersonalInfo.TABLE_NAME, null, values);
			} catch (Exception e) {
				// TODO: handle exception
				MyLog.v("InitUser", e.toString());
			}
		}
	}

	// 更新目标设置中用户数据信息
	public void UpdateUserInfo(String name, int sex, double height,
			double weight) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		Object[] profile = getUserProfile();
		int id = (Integer) profile[0];
		if (id > 0) {
			database.execSQL("update " + User.TABLE_NAME + " set "
					+ User.NICKNAME + "=? ," + User.SEX + "=? ," + User.HEIGHT
					+ "=? ," + User.WEIGHT + "=?" + " where " + User.UID
					+ " =?", new Object[] { name, sex, height, weight, uid });
		} else {
			ContentValues values = new ContentValues();
			values.put(User.UID, uid);
			values.put(User.NICKNAME, name);
			values.put(User.SEX, sex);
			values.put(User.HEIGHT, height);
			values.put(User.WEIGHT, weight);
			try {
				database.insert(User.TABLE_NAME, null, values);
			} catch (Exception e) {
				// TODO: handle exception
				MyLog.v("InitUser", e.toString());
			}
		}
	}

	public void UpdateUserInfo(String name, int sex, double height,
			double weight, String username) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		Object[] profile = getUserProfile();
		int id = (Integer) profile[0];
		if (id > 0) {
			database.execSQL("update " + User.TABLE_NAME + " set "
					+ User.NICKNAME + "=? ," + User.SEX + "=? ," + User.HEIGHT
					+ "=? ," + User.WEIGHT + "=? ," + User.USERNAME + "=?"
					+ " where " + User.UID + " =?", new Object[] { name, sex,
					height, weight, username, uid });
		} else {
			ContentValues values = new ContentValues();
			values.put(User.UID, uid);
			values.put(User.NICKNAME, name);
			values.put(User.SEX, sex);
			values.put(User.HEIGHT, height);
			values.put(User.WEIGHT, weight);
			values.put(User.USERNAME, username);
			try {
				database.insert(User.TABLE_NAME, null, values);
			} catch (Exception e) {
				// TODO: handle exception
				MyLog.v("InitUser", e.toString());
			}
		}
	}

	public void UpdateSex(int sex) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		Object[] profile = getUserProfile();
		int id = (Integer) profile[0];
		if (id > 0) {
			database.execSQL("update " + User.TABLE_NAME + " set " + User.SEX
					+ "=? " + " where " + User.UID + " =?", new Object[] { sex,
					uid });
		} else {
			ContentValues values = new ContentValues();
			values.put(User.UID, uid);
			values.put(User.SEX, sex);
			try {
				database.insert(User.TABLE_NAME, null, values);
			} catch (Exception e) {
				// TODO: handle exception
				MyLog.v("InitUser", e.toString());
			}
		}
	}

	// 更新个人资料中用户信息
	public void UpdateUserInfo(String name) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		Object[] profile = getUserProfile();
		if (profile == null)
			return;
		int id = (Integer) profile[0];
		if (id > 0) {
			database.execSQL("update " + User.TABLE_NAME + " set "
					+ User.NICKNAME + "=? " + "where " + User.UID + " =?",
					new Object[] { name, uid });
		} else {
			ContentValues values = new ContentValues();
			values.put(User.UID, uid);
			values.put(User.NICKNAME, name);
			try {
				database.insert(User.TABLE_NAME, null, values);
			} catch (Exception e) {
				// TODO: handle exception
				MyLog.v("InitUser", e.toString());
			}
		}
	}

	public void UpdateUserHeight(String height) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		Object[] profile = getUserProfile();
		if (profile == null)
			return;
		int id = (Integer) profile[0];
		if (id > 0) {
			database.execSQL("update " + User.TABLE_NAME + " set "
					+ User.HEIGHT + "=? " + "where " + User.UID + " =?",
					new Object[] { height, uid });
		} else {
			ContentValues values = new ContentValues();
			values.put(User.UID, uid);
			values.put(User.HEIGHT, height);
			try {
				database.insert(User.TABLE_NAME, null, values);
			} catch (Exception e) {
				// TODO: handle exception
				MyLog.v("InitUser", e.toString());
			}
		}
	}

	public void UpdateUserBirth(String birth) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		Object[] profile = getUserProfile();
		if (profile == null)
			return;
		int id = (Integer) profile[0];
		if (id > 0) {
			database.execSQL("update " + User.TABLE_NAME + " set " + User.BIRTH
					+ "=? " + "where " + User.UID + " =?", new Object[] {
					birth, uid });
		} else {
			ContentValues values = new ContentValues();
			values.put(User.UID, uid);
			values.put(User.BIRTH, birth);
			try {
				database.insert(User.TABLE_NAME, null, values);
			} catch (Exception e) {
				// TODO: handle exception
				MyLog.v("InitUser", e.toString());
			}
		}
	}

	public void UpdateUserWeight(String weight) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		Object[] profile = getUserProfile();
		if (profile == null)
			return;
		int id = (Integer) profile[0];
		if (id > 0) {
			database.execSQL("update " + User.TABLE_NAME + " set "
					+ User.WEIGHT + "=? " + "where " + User.UID + " =?",
					new Object[] { weight, uid });
		} else {
			ContentValues values = new ContentValues();
			values.put(User.UID, uid);
			values.put(User.WEIGHT, weight);
			try {
				database.insert(User.TABLE_NAME, null, values);
			} catch (Exception e) {
				// TODO: handle exception
				MyLog.v("InitUser", e.toString());
			}
		}
	}

	public void UpdateUserInfo(int ismember) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();

		Object[] profile = getUserProfile();
		if (profile == null)
			return;
		int id = (Integer) profile[0];
		if (id > 0) {
			database.execSQL("update " + User.TABLE_NAME + " set "
					+ User.ISMEMBER + "=? " + "where " + User.UID + " =?",
					new Object[] { ismember, uid });
		} else {
			ContentValues values = new ContentValues();
			values.put(User.UID, uid);
			values.put(User.ISMEMBER, ismember);
			try {
				database.insert(User.TABLE_NAME, null, values);
			} catch (Exception e) {
				// TODO: handle exception
				MyLog.v("InitUser", e.toString());
			}
		}
	}

	public void UpdateUserContact(String contact) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();

		Object[] profile = getUserProfile();
		if (profile == null)
			return;
		int id = (Integer) profile[0];
		if (id > 0) {
			database.execSQL("update " + User.TABLE_NAME + " set "
					+ User.CONTACT + "=? " + "where " + User.UID + " =?",
					new Object[] { contact, uid });
		} else {
			ContentValues values = new ContentValues();
			values.put(User.UID, uid);
			values.put(User.CONTACT, contact);
			try {
				database.insert(User.TABLE_NAME, null, values);
			} catch (Exception e) {
				// TODO: handle exception
				MyLog.v("InitUser", e.toString());
			}
		}
	}

	public void UpdateUserEmail(String email) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();

		Object[] profile = getUserProfile();
		if (profile == null)
			return;
		int id = (Integer) profile[0];
		if (id > 0) {
			database.execSQL("update " + User.TABLE_NAME + " set " + User.EMAIL
					+ "=? " + "where " + User.UID + " =?", new Object[] {
					email, uid });
		} else {
			ContentValues values = new ContentValues();
			values.put(User.UID, uid);
			values.put(User.EMAIL, email);
			try {
				database.insert(User.TABLE_NAME, null, values);
			} catch (Exception e) {
				// TODO: handle exception
				MyLog.v("InitUser", e.toString());
			}
		}
	}

	public String[] getUserAccount() {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.rawQuery("select " + User.CONTACT + " , "
				+ User.EMAIL + " from " + User.TABLE_NAME + " where uid=?",
				new String[] { uid + "" });
		String[] oj = new String[2];
		if (cursor.moveToNext()) {
			oj = new String[] { cursor.getString(0), cursor.getString(1) };
		}
		cursor.close();
		return oj;

	}

	// 修改过
	public void InitUser(int uid, String nickname, int ismember, int sex,
			double height, double weight, String lovefitid, String email,
			String phone, String birth, String username) {
		Object[] profile = getUserProfile();
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		if (profile != null) {
			int id = (Integer) profile[0];
			if (id > 0) {
				database.execSQL("update " + User.TABLE_NAME + " set "
						+ User.UID + "=?," + User.NICKNAME + "=? ,"
						+ User.ISMEMBER + "=? ," + User.SEX + "=? ,"
						+ User.HEIGHT + "=? ," + User.WEIGHT + "=?,"
						+ User.LOVEFITID + "=?," + User.EMAIL + "=?,"
						+ User.CONTACT + "=?," + User.BIRTH + "=?,"
						+ User.USERNAME + "=? where " + User._ID + " =?",
						new Object[] { uid, nickname, ismember, sex, height,
								weight, lovefitid, email, phone, username, id });
			}
		} else {
			ContentValues values = new ContentValues();
			values.put(User.UID, uid);
			values.put(User.NICKNAME, nickname);
			values.put(User.ISMEMBER, ismember);
			values.put(User.SEX, sex);
			values.put(User.HEIGHT, height);
			values.put(User.WEIGHT, weight);
			values.put(User.LOVEFITID, lovefitid);
			values.put(User.EMAIL, email);
			values.put(User.CONTACT, phone);
			values.put(User.BIRTH, birth);
			values.put(User.USERNAME, username);
			try {
				database.insert(User.TABLE_NAME, null, values);
			} catch (Exception e) {
				// TODO: handle exception
				MyLog.v("InitUser", e.toString());
			}

		}
	}

	// 获得设置中的用户信息
	/***
	 * id,uid,nickanme,ismember,sex,height,weight,contact,email,lovefitid;
	 * 
	 * @return
	 */
	public Object[] getUserProfile() {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.rawQuery("select * from " + User.TABLE_NAME
				+ " where uid=?", new String[] { uid + "" });
		Object[] oj = null;
		// id,uid,nickanme,ismember,sex,height,weight,contact,email,lovefitid;
		if (cursor.moveToNext()) {
			int id = cursor.getCount() == 1 ? cursor.getInt(0) : -1;
			if (id >= 0) {

				oj = new Object[] { cursor.getInt(0), cursor.getInt(1),
						cursor.getString(2), cursor.getInt(3),
						cursor.getInt(4), cursor.getDouble(5),
						cursor.getDouble(6), cursor.getString(7),
						cursor.getString(8), cursor.getString(9),
						cursor.getString(10), cursor.getString(11) };
			}
		}
		cursor.close();
		return oj;
	}

	// 删除过期的排名数据
	public void DeleTeRank(int ranktype) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		try {
			database.execSQL("delete from " + Friendrank.TABLE_NAME + "where "
					+ Friendrank.RANK_TYPE + " =?", new Object[] { ranktype });
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.v("k444kkk", e.toString());
		}

	}

	/* 存储运动排名的相关数据 */
	public void SaveRankData(int ranktype, int uid, String user, int step,
			int gid, String updatetime, String url) {

		SQLiteDatabase database = dbHelper.getWritableDatabase();
		try {
			database.execSQL("insert into " + Friendrank.TABLE_NAME
					+ "(ranktype, uid, user,step, gid,updatetime , url)"
					+ "values( ?,?,?,?,?,?,?)", new Object[] { ranktype, uid,
					user, step, gid, updatetime, url });

		} catch (Exception e) {
			// TODO: handle exception
			MyLog.v("kkkkkk", e.toString());
		}
	}

	/* 存储运动排名的相关数据 */
	public void SaveAllRankData(int ranktype, int uid, String user, int step,
			int gid, String updatetime, String url) {

		SQLiteDatabase database = dbHelper.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(Friendrank.RANK_TYPE, ranktype);
		values.put(Friendrank.UID, uid);
		values.put(Friendrank.USER, user);
		values.put(Friendrank.STEP, step);
		values.put(Friendrank.GID, gid);
		values.put(Friendrank.UPDATETIME, updatetime);
		values.put(Friendrank.URL, url);

		try {
			int i = database.update(Friendrank.TABLE_NAME, values,
					Friendrank.RANK_TYPE + " = ? and " + Friendrank.UID
							+ " =? ", new String[] { ranktype + "", uid + "" });
			if (i == 0) {
				database.insert(Friendrank.TABLE_NAME, null, values);
			}
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.v("ClubDetail", e.toString());
		}
	}

	/* 朋友圈 运动排名查询 */
	public ArrayList<HashMap<String, String>> getRankData(int ranktype, int page) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();

		Cursor cursor = database.rawQuery("select * from "
				+ Friendrank.TABLE_NAME + " where ranktype=?" + " ORDER BY "
				+ Friendrank.STEP + " desc limit " + page * 10 + ",10;",
				new String[] { ranktype + "" });
		ArrayList<HashMap<String, String>> a = new ArrayList<HashMap<String, String>>();
		if (cursor != null && cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("id", cursor.getInt(0) + "");
				map.put("uid", cursor.getInt(1) + "");
				map.put("user", cursor.getString(2) + "");
				map.put("step", cursor.getInt(3) + "");
				map.put("updatetime", cursor.getString(7) + "");
				map.put("url", cursor.getString(8) + "");
				a.add(map);
			}
		}
		cursor.close();
		return a;
	}

	// 获得所有排名
	public ArrayList<HashMap<String, String>> getRankData(int ranktype) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		Cursor cursor = database.rawQuery("select * from "
				+ Friendrank.TABLE_NAME + " where ranktype=?" + " ORDER BY "
				+ Friendrank.STEP + " desc", new String[] { ranktype + "" });
		ArrayList<HashMap<String, String>> a = new ArrayList<HashMap<String, String>>();
		if (cursor != null && cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("id", cursor.getInt(0) + "");
				map.put("uid", cursor.getInt(1) + "");
				map.put("user", cursor.getString(2) + "");
				map.put("step", cursor.getInt(3) + "");
				map.put("updatetime", cursor.getString(7) + "");
				map.put("url", cursor.getString(8) + "");
				a.add(map);
			}
		}
		cursor.close();
		return a;
	}

	// 获得所有排名
	public ArrayList<UserHashMap<String, String>> getRankDataByPage(
			int ranktype, int index) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		Cursor cursor = database.rawQuery("select * from "
				+ Friendrank.TABLE_NAME + " where ranktype=?" + " ORDER BY "
				+ Friendrank.STEP + " desc ", new String[] { ranktype + "" });
		ArrayList<UserHashMap<String, String>> a = new ArrayList<UserHashMap<String, String>>();
		if (cursor != null && cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				UserHashMap<String, String> map = new UserHashMap<String, String>();
				map.put("id", cursor.getInt(0) + "");
				map.put("uid", cursor.getInt(1) + "");
				map.put("user",
						AllRankActivity.transformNick(cursor.getString(2) + ""));
				map.put("step", cursor.getInt(3) + "");
				map.put("updatetime", cursor.getString(7) + "");
				map.put("url", cursor.getString(8) + "");
				if (!a.contains(map)) {
					a.add(map);
					if (a.size() == (10 + (index * 10))) {
						cursor.close();
						return a;
					}
				}

			}
		}
		cursor.close();
		return a;
	}

	// 获得全站排名
	public ArrayList<UserHashMap<String, String>> getAllRankDataByPage(
			int ranktype, int index) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		Cursor cursor = database.rawQuery("select * from "
				+ Friendrank.TABLE_NAME + " where ranktype=?" + " ORDER BY "
				+ Friendrank.STEP + " desc ", new String[] { ranktype + "" });
		ArrayList<UserHashMap<String, String>> a = new ArrayList<UserHashMap<String, String>>();
		if (cursor != null && cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				UserHashMap<String, String> map = new UserHashMap<String, String>();
				map.put("id", cursor.getInt(0) + "");
				map.put("uid", cursor.getInt(1) + "");
				map.put("user",
						AllRankActivity.transformNick(cursor.getString(2) + ""));
				map.put("step", cursor.getInt(3) + "");
				map.put("updatetime", cursor.getString(7) + "");
				map.put("url", cursor.getString(8) + "");
				if (!a.contains(map)) {
					a.add(map);
					if (a.size() == (10 + index)) {
						cursor.close();
						return a;
					}
				} else {
					System.out.println("%%%>" + map);
				}

			}
		}
		cursor.close();
		return a;
	}

	/** 初始化目标表 **/
	public void InitTarget(int type, double wbef, double wend, int step,
			int ifsleep, int ifweight, int ifbmi) {
		Object[] target = getUserTarget();
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		if (target != null) {
			int id = (Integer) target[0];
			if (id > 0) {
				database.execSQL(
						"update " + Target.TABLE_NAME + " set "
								+ Target.TARGET_TYPE + "=?,"
								+ Target.TARGET_WEIGHT_BEFORE + "=? ,"
								+ Target.TARGET_WEIGHT_END + "=? ,"
								+ Target.TARGET_STEP + "=? ,"
								+ Target.TARGET_SLEEP + "=? ,"
								+ Target.TARGET_WEIGHT + "=? ,"
								+ Target.TARGET_BMI + "=?  where " + Target._ID
								+ " =?", new Object[] { type, wbef, wend, step,
								ifsleep, ifweight, ifbmi, id });
			}
		} else {
			ContentValues values = new ContentValues();
			values.put(Target.UID, uid);
			values.put(Target.TARGET_TYPE, type);
			values.put(Target.TARGET_WEIGHT_BEFORE, wbef);
			values.put(Target.TARGET_WEIGHT_END, wend);
			values.put(Target.TARGET_STEP, step);
			values.put(Target.TARGET_SLEEP, ifsleep);
			values.put(Target.TARGET_WEIGHT, ifweight);
			values.put(Target.TARGET_BMI, ifbmi);
			try {
				database.insert(Target.TABLE_NAME, null, values);
			} catch (Exception e) {
				// TODO: handle exception
				MyLog.v("initTarget", e.toString());
			}

		}
	}

	/** 获得用户目标 **/
	// //注意修改了
	public Object[] getUserTarget() {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.rawQuery("select * from " + Target.TABLE_NAME
				+ " where uid=?", new String[] { uid + "" });
		Object[] oj = null;
		if (cursor.moveToNext()) {
			int id = cursor.getCount() == 1 ? cursor.getInt(0) : -1;
			if (id >= 0) {
				// _id,uid,type,step,weightbefore,weightend,ifweight,ifsleep,ifbmi
				oj = new Object[] { cursor.getInt(0), cursor.getInt(1),
						cursor.getInt(2), cursor.getInt(3),
						cursor.getDouble(4), cursor.getDouble(5),
						cursor.getInt(6), cursor.getInt(7), cursor.getInt(8) };
			}
		}
		cursor.close();
		return oj;
	}

	/** 初始化设备表 **/

	public void InitDeivce(int stype, int wtype, String sdid, String wdid) {
		Object[] device = getUserDeivce();
		SQLiteDatabase database = dbHelper.getWritableDatabase();

		if (device != null) {
			int id = (Integer) device[0];
			// // 0->蓝牙 1-> 3g 2->手机 3->ble 4->ant 5->air
			if (id > 0) {
				if (stype == 1 || stype == 4) {
					if (sdid != null && sdid.length() == 15) {
						database.execSQL("update " + Device.TABLE_NAME
								+ " set " + Device.DEVICESP_TYPE + "=?,"
								+ Device.DEVICESP_ID + "=? ,"
								+ Device.DEVICEWG_TYPE + "=? ,"
								+ Device.DEVICEWG_ID + "=? " + " where "
								+ Device._ID + " =?", new Object[] { stype,
								sdid, wtype, wdid, id });
					} else {
						database.execSQL("update " + Device.TABLE_NAME
								+ " set " + Device.DEVICESP_TYPE + "=?,"
								+ Device.DEVICEWG_TYPE + "=? ,"
								+ Device.DEVICEWG_ID + "=? " + " where "
								+ Device._ID + " =?", new Object[] { 0, wtype,
								wdid, id });
					}
				} else {
					database.execSQL("update " + Device.TABLE_NAME + " set "
							+ Device.DEVICESP_TYPE + "=?,"
							+ Device.DEVICEWG_TYPE + "=? ,"
							+ Device.DEVICEWG_ID + "=? " + " where "
							+ Device._ID + " =?", new Object[] { stype, wtype,
							wdid, id });
				}
			}
		} else {

			ContentValues values = new ContentValues();
			values.put(Device.UID, uid);
			values.put(Device.DEVICESP_TYPE, stype);
			values.put(Device.DEVICESP_ID, sdid);
			values.put(Device.DEVICEWG_TYPE, wtype);
			values.put(Device.DEVICEWG_ID, wdid);
			try {
				database.insert(Device.TABLE_NAME, null, values);
			} catch (Exception e) {
				// TODO: handle exception
				MyLog.v("initDevice", e.toString());
			}

		}
	}

	/** 获得用户设备_id,uid,sp_type,spdid,wg_type,wgdid **/
	// 修改过
	public Object[] getUserDeivce() {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.rawQuery("select * from " + Device.TABLE_NAME
				+ " where uid=?", new String[] { uid + "" });
		Object[] oj = null;
		if (cursor.moveToNext()) {
			int id = cursor.getCount() == 1 ? cursor.getInt(0) : -1;
			if (id >= 0) {
				// _id,uid,sp_type,spdid,wg_type,wgdid
				oj = new Object[] { cursor.getInt(0), cursor.getInt(1),
						cursor.getInt(2), cursor.getString(3),
						cursor.getInt(4), cursor.getString(5), };
			}
		}
		cursor.close();
		return oj;
	}

	// get user device type
	public int getUserDeivceType() {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.rawQuery("select " + Device.DEVICESP_TYPE
				+ " from " + Device.TABLE_NAME + " where uid=?",
				new String[] { uid + "" });
		int type = -1;
		if (cursor.moveToNext()) {
			type = cursor.getCount() == 1 ? cursor.getInt(0) : -1;
		}
		cursor.close();
		return type;
	}

	// get user device type
	public String getBTDeivceAddress() {
		String address = null;
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.rawQuery("select " + Device.DEVICESP_ID
				+ " from " + Device.TABLE_NAME + " where uid=?",
				new String[] { uid + "" });
		if (cursor.moveToNext()) {
			address = cursor.getString(0);
		}
		cursor.close();
		return address;
	}

	/** 设置目标类型 **/

	public void setTargetType(int type) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		try {
			database.execSQL("update " + Target.TABLE_NAME + " set "
					+ Target.TARGET_TYPE + "=? where " + Target.UID + " =?",
					new Object[] { type, uid });
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.v("setTargetType", e.toString());
		}
	}

	/** 设置初始体重 **/
	public void setTargetWeightBef(double bef) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		try {
			database.execSQL("update " + Target.TABLE_NAME + " set "
					+ Target.TARGET_WEIGHT_BEFORE + "=? where " + Target.UID
					+ " =?", new Object[] { bef, uid });
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.v("setTargetWeightBef", e.toString());
		}
	}

	/** 设置目标体重 **/

	public void setTargetWeightEnd(double end) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		try {
			database.execSQL("update " + Target.TABLE_NAME + " set "
					+ Target.TARGET_WEIGHT_END + "=? where " + Target.UID
					+ " =?", new Object[] { end, uid });
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.v("setTargetWeightEnd", e.toString());
		}
	}

	/** 设置目标步数 **/

	public void setTargetStep(int step) {

		SQLiteDatabase database = dbHelper.getWritableDatabase();
		try {
			database.execSQL("update " + Target.TABLE_NAME + " set "
					+ Target.TARGET_STEP + "=? where " + Target.UID + " =?",
					new Object[] { step, uid });
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.v("setTargetStep", e.toString());
		}

	}

	/** 设置是否开启睡眠 **/
	public void setTargetIfSleep(int ifsleep) {

		SQLiteDatabase database = dbHelper.getWritableDatabase();
		try {
			database.execSQL("update " + Target.TABLE_NAME + " set "
					+ Target.TARGET_SLEEP + "=? where " + Target.UID + " =?",
					new Object[] { ifsleep, uid });
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.v("setTargetSleep", e.toString());
		}

	}

	/** 设置是否开启体重 **/

	public void setTargetIfWeight(int ifweight) {

		SQLiteDatabase database = dbHelper.getWritableDatabase();
		try {
			database.execSQL("update " + Target.TABLE_NAME + " set "
					+ Target.TARGET_WEIGHT + "=? where " + Target.UID + " =?",
					new Object[] { ifweight, uid });
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.v("setTargetIfWeight", e.toString());
		}

	}

	/** 设置是否开启bmi **/

	public void setTargetIfBmi(int ifbmi) {

		SQLiteDatabase database = dbHelper.getWritableDatabase();
		try {
			database.execSQL("update " + Target.TABLE_NAME + " set "
					+ Target.TARGET_BMI + "=? where " + Target.UID + " =?",
					new Object[] { ifbmi, uid });
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.v("setTargetIfBmi", e.toString());
		}

	}

	/** 设置运动设备类型 **/

	public void setDeviceSpType(int type) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		try {
			database.execSQL("update " + Device.TABLE_NAME + " set "
					+ Device.DEVICESP_TYPE + "=? where " + Device.UID + " =?",
					new Object[] { type, uid });
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.v("setDeviceSpType", e.toString());
		}
	}

	/** 设置体重设备类型 **/

	public void setDeviceWgType(int type) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		try {
			database.execSQL("update " + Device.TABLE_NAME + " set "
					+ Device.DEVICEWG_TYPE + "=? where " + Device.UID + " =?",
					new Object[] { type, uid });
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.v("setDeviceWgType", e.toString());
		}
	}

	/** 获得体重设备类型 **/

	public int getDeviceWgType() {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		try {
			Cursor cursor = database.rawQuery("select " + Device.DEVICEWG_TYPE
					+ " from " + Device.TABLE_NAME + " where " + Device.UID
					+ " =? ", new String[] { uid + "" });
			if (cursor.moveToFirst()) {
				return cursor.getInt(cursor
						.getColumnIndexOrThrow(Device.DEVICEWG_TYPE));
			} else {
				return 0;
			}
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.v("getDeviceWgType", e.toString());
			return 0;
		}
	}

	/** 设置运动设备id **/
	public void setDeviceSpID(String spdid) {
		if (spdid == null) {
			return;
		}
		if (spdid.length() > 0) {
			SQLiteDatabase database = dbHelper.getWritableDatabase();
			try {
				database.execSQL(
						"update " + Device.TABLE_NAME + " set "
								+ Device.DEVICESP_ID + "=? where " + Device.UID
								+ " =?", new Object[] { spdid, uid });
			} catch (Exception e) {
				// TODO: handle exception
				MyLog.v("setDeviceSpID", e.toString());
			}
		}

	}

	/** 设置体重设备id **/

	public void setDeviceWgID(String wgdid) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		try {
			database.execSQL("update " + Device.TABLE_NAME + " set "
					+ Device.DEVICEWG_ID + "=? where " + Device.UID + " =?",
					new Object[] { wgdid, uid });
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.v("setDeviceWgID", e.toString());
		}
	}

	/** 检查运动基本数据 **/

	public int checkSpBasicData(String time) {
		int _ID = -1;
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.rawQuery("select " + PdSpBasic._ID + " from "
				+ PdSpBasic.TABLE_NAME + " where " + PdSpBasic.UID + "=? and "
				+ PdSpBasic.DATE + "=?", new String[] { uid + "", time });
		if (cursor.moveToNext()) {
			_ID = cursor.getInt(0);
		}
		cursor.close();
		return _ID;
	}

	/** 检查体重基本数据 **/
	public int checkWgBasicData(String time) {
		int _ID = -1;
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.rawQuery("select " + PdWgBasic._ID + " from "
				+ PdWgBasic.TABLE_NAME + " where " + PdWgBasic.UID + "=? and "
				+ PdWgBasic.DATE + "=?", new String[] { uid + "", time });
		if (cursor.moveToNext()) {
			_ID = cursor.getInt(0);
		}
		cursor.close();
		return _ID;
	}

	/** 获得基本运动数据 **/

	public Object[] getSpBasicData(String time) {
		Object[] result = null;
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.rawQuery("select * from "
				+ PdSpBasic.TABLE_NAME + " where " + PdSpBasic.UID + "=? and "
				+ PdSpBasic.DATE + "=?", new String[] { uid + "", time });
		if (cursor.moveToNext()) {
			// step,distance,calorie
			result = new Object[] { cursor.getInt(3), cursor.getInt(4),
					cursor.getInt(5) };
		}
		cursor.close();
		return result;
	}

	/** 存储基本运动数据 **/
	public void SaveSpBasicData(int step, int dis, int cal, int type,
			String time, String update) {
		int _ID = checkSpBasicData(time);
		int upload = 0;
		// 0->蓝牙 1-> 3g 2->手机 3->ble 4->ant 5->air
		if (type == 5 || type == 0 || type == 2) {
			// 1 未上传 2已上传
			upload = 1;
		}

		SQLiteDatabase database = dbHelper.getWritableDatabase();
		try {
			if (_ID > 0) {
				database.execSQL("update " + PdSpBasic.TABLE_NAME + " set "
						+ PdSpBasic.STEP + "=?," + PdSpBasic.DISTANCE + "=?,"
						+ PdSpBasic.CALORIE + "=?," + PdSpBasic.TYPE + "=?,"
						+ PdSpBasic.DATE + "=?," + PdSpBasic.UPLOAD + "=?,"
						+ PdSpBasic.UPDATETIME + "=? where " + PdSpBasic._ID
						+ " =? ", new Object[] { step, dis, cal, type, time,
						upload, update, _ID });
			} else {
				ContentValues values = new ContentValues();
				values.put(PdSpBasic.UID, uid);
				values.put(PdSpBasic.STEP, step);
				values.put(PdSpBasic.DISTANCE, dis);
				values.put(PdSpBasic.CALORIE, cal);
				values.put(PdSpBasic.DATE, time);
				values.put(PdSpBasic.UPDATETIME, update);
				values.put(PdSpBasic.TYPE, type);
				values.put(PdSpBasic.UPLOAD, upload);
				database.insert(PdSpBasic.TABLE_NAME, null, values);
			}
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.v("SaveSPBasicData", e.toString());
		}
	}

	// 获取未上传的数据
	public List<StepData> getUploadStep() {
		List<StepData> list = new ArrayList<StepData>();
		StepData temp;
		SQLiteDatabase database = dbHelper.getReadableDatabase();

		String sql = String.format(
				"select %s,%s,%s,%s,%s from %s where %s=%s and %s = 1 ",
				PdSpBasic._ID, PdSpBasic.DATE, PdSpBasic.STEP,
				PdSpBasic.CALORIE, PdSpBasic.DISTANCE, PdSpBasic.TABLE_NAME,
				PdSpBasic.UID, uid, PdSpBasic.UPLOAD);

		Cursor cursor = database.rawQuery(sql, null);

		while (cursor.moveToNext()) {
			temp = new StepData();
			temp.id = cursor.getInt(0);
			temp.dateString = cursor.getString(1);
			temp.step = cursor.getInt(2);
			temp.calorie = cursor.getInt(3);
			temp.distance = cursor.getInt(4);
			list.add(temp);
		}
		cursor.close();
		return list;
	}

	public void UpdateStepStatus(int id) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		// 1 未上传 2已上传

		String sql = String.format("update %s set %s= 2 where %s = %d ",
				PdSpBasic.TABLE_NAME, PdSpBasic.UPLOAD, PdSpBasic._ID, id);
		try {
			database.execSQL(sql);
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.e("DB", e.getMessage());
		}

	}

	// 获取未上传的详细数据
	public StepRawData getUploadStepRaw(String curdate) {
		StepRawData temp = null;
		String dateString;

		SQLiteDatabase database = dbHelper.getReadableDatabase();

		String sql = String
				.format("select %s,%s,%s from %s where %s=%s and %s = 1 and %s <= '%s' limit 1",
						PdSpRaw._ID, PdSpRaw.DATE, PdSpRaw.DATA,
						PdSpRaw.TABLE_NAME, PdSpRaw.UID, uid, PdSpRaw.UPLOAD,
						PdSpRaw.DATE, curdate);

		Cursor cursor = database.rawQuery(sql, null);

		if (cursor.moveToNext()) {
			dateString = cursor.getString(1);
		} else {
			dateString = null;
		}

		cursor.close();

		if (dateString == null)
			return null;

		sql = String.format(
				"select %s,%s,%s from %s where %s=%s and %s = 1 and %s = '%s'",
				PdSpRaw._ID, PdSpRaw.DATE, PdSpRaw.DATA, PdSpRaw.TABLE_NAME,
				PdSpRaw.UID, uid, PdSpRaw.UPLOAD, PdSpRaw.DATE, dateString);

		cursor = database.rawQuery(sql, null);

		while (cursor.moveToNext()) {
			if (temp == null) {
				temp = new StepRawData();
				temp.date = cursor.getString(1);
			}
			// temp.id = cursor.getInt(0);
			temp.raw += getString(cursor.getBlob(2));
			// break;
		}
		cursor.close();

		return temp;
	}

	public void UpdateStepRawStatus(String date) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		// 1 未上传 2已上传
		String sql = String.format("update %s set `%s`= 2 where `%s` = '%s' ",
				PdSpRaw.TABLE_NAME, PdSpRaw.UPLOAD, PdSpRaw.DATE, date);
		try {
			database.execSQL(sql);
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.e("DB", e.getMessage());
		}

	}

	public static String getString(byte[] data) {

		StringBuilder str = new StringBuilder();

		// 跳过头部
		for (int i = 0; i < data.length; i++) {
			str.append(String.format("%02x", data[i]));
		}
		return str.toString();

	}

	/** 获取基本体重数据 weight,bmi,fat,water,muscle,bone **/
	//
	public Object[] getWgBasicData(String time) {
		Object[] result = null;
		SQLiteDatabase database = dbHelper.getReadableDatabase();

		Cursor cursor = database.rawQuery("select " + PdWgBasic.WEIGHT + ","
				+ PdWgBasic.BMI + "," + PdWgBasic.FAT + "," + PdWgBasic.WATER
				+ "," + PdWgBasic.MUSCLE + "," + PdWgBasic.BONE + " from "
				+ PdWgBasic.TABLE_NAME + " where " + PdWgBasic.UID + "=? and "
				+ PdWgBasic.DATE + "=?", new String[] { uid + "", time });

		if (cursor.moveToNext()) {
			// weight,bmi,fat,water,muscle,bone
			result = new Object[] { cursor.getDouble(0), cursor.getDouble(1),
					cursor.getDouble(2), cursor.getDouble(3),
					cursor.getDouble(4), cursor.getDouble(5), };
		}
		cursor.close();
		return result;
	}

	/** 保存基本体重设备 **/

	public void SaveWgBasicData(double weight, double bmi, int type,
			String time, String update) {
		int _ID = checkWgBasicData(time);
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		try {
			if (_ID > 0) {
				database.execSQL("update " + PdWgBasic.TABLE_NAME + " set "
						+ PdWgBasic.WEIGHT + "=?," + PdWgBasic.BMI + "=?,"
						+ PdWgBasic.TYPE + "=?," + PdWgBasic.DATE + "=?,"
						+ PdWgBasic.UPDATETIME + "=? where " + PdWgBasic._ID
						+ " =? ", new Object[] { weight, bmi, type, time,
						update, _ID });
			} else {
				ContentValues values = new ContentValues();
				values.put(PdWgBasic.UID, uid);
				values.put(PdWgBasic.WEIGHT, weight);
				values.put(PdWgBasic.BMI, bmi);
				values.put(PdWgBasic.DATE, time);
				values.put(PdWgBasic.UPDATETIME, update);
				values.put(PdWgBasic.TYPE, type);
				database.insert(PdWgBasic.TABLE_NAME, null, values);
			}
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.v("SaveWgBasicData", e.toString());
		}
	}

	public void SaveWgBasicDataFull(double weight, double bmi, double fat,
			double water, double muscle, double bone, int type, String time,
			String update) {
		int _ID = checkWgBasicData(time);
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		try {
			if (_ID > 0) {
				database.execSQL("update " + PdWgBasic.TABLE_NAME + " set "
						+ PdWgBasic.WEIGHT + "=?," + PdWgBasic.BMI + "=?,"
						+ PdWgBasic.FAT + "=?," + PdWgBasic.WATER + "=?,"
						+ PdWgBasic.MUSCLE + "=?," + PdWgBasic.BONE + "=?,"
						+ PdWgBasic.TYPE + "=?," + PdWgBasic.DATE + "=?,"
						+ PdWgBasic.UPDATETIME + "=? where " + PdWgBasic._ID
						+ " =? ", new Object[] { weight, bmi, fat, water,
						muscle, bone, type, time, update, _ID });
			} else {
				ContentValues values = new ContentValues();
				values.put(PdWgBasic.UID, uid);
				values.put(PdWgBasic.WEIGHT, weight);
				values.put(PdWgBasic.BMI, bmi);
				values.put(PdWgBasic.FAT, fat);
				values.put(PdWgBasic.WATER, water);
				values.put(PdWgBasic.MUSCLE, muscle);
				values.put(PdWgBasic.BONE, bone);
				values.put(PdWgBasic.DATE, time);
				values.put(PdWgBasic.UPDATETIME, update);
				values.put(PdWgBasic.TYPE, type);
				database.insert(PdWgBasic.TABLE_NAME, null, values);
			}
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.v("SaveWgBasicData", e.toString());
		}
	}

	/** 测试输入� **/
	public void testInsert() {
		// SaveSpBasicData(7000, 4000, 200000, 0, "2014-05-27", "2014-05-27");
		// SaveWgBasicData(61.8f, 24.2f, 0, "2014-05-22", "2014-05-22");
	}

	/** 获得步数基本数据 **/
	public Map<String, Integer> getStepBasicData(String from, String to) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Map<String, Integer> steps = new HashMap<String, Integer>();
		Cursor cursor = database.rawQuery("select " + PdSpBasic.STEP + ","
				+ PdSpBasic.DATE + " from " + PdSpBasic.TABLE_NAME + " where "
				+ PdSpBasic.UID + "=? and " + PdSpBasic.DATE
				+ " between ? and ? ", new String[] { uid + "", from, to });
		while (cursor.moveToNext()) {
			steps.put(cursor.getString(1), cursor.getInt(0));
		}
		cursor.close();
		return steps;

	}

	/** 获得卡路里基本数据 **/

	public Map<String, Integer> getCalBasicData(String from, String to) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Map<String, Integer> cal = new HashMap<String, Integer>();
		Cursor cursor = database.rawQuery("select " + PdSpBasic.CALORIE + ","
				+ PdSpBasic.DATE + " from " + PdSpBasic.TABLE_NAME + " where "
				+ PdSpBasic.UID + "=? and " + PdSpBasic.DATE
				+ " between ? and ? ", new String[] { uid + "", from, to });
		while (cursor.moveToNext()) {
			cal.put(cursor.getString(1),
					(int) Math.round(cursor.getInt(0) / 1000.0));
		}
		cursor.close();
		return cal;

	}

	/** 获得距离基本数据 **/
	public Map<String, Integer> getDisBasicData(String from, String to) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Map<String, Integer> dis = new HashMap<String, Integer>();
		Cursor cursor = database.rawQuery("select " + PdSpBasic.DISTANCE + ","
				+ PdSpBasic.DATE + " from " + PdSpBasic.TABLE_NAME + " where "
				+ PdSpBasic.UID + "=? and " + PdSpBasic.DATE
				+ " between ? and ? ", new String[] { uid + "", from, to });
		while (cursor.moveToNext()) {
			dis.put(cursor.getString(1), cursor.getInt(0));
		}
		cursor.close();
		return dis;

	}

	/** 获得体重基本数据 **/
	public Map<String, Double> getWeightBasicData(String from, String to) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Map<String, Double> weight = new HashMap<String, Double>();
		Cursor cursor = database.rawQuery("select " + PdWgBasic.WEIGHT + ","
				+ PdWgBasic.DATE + " from " + PdWgBasic.TABLE_NAME + " where "
				+ PdWgBasic.UID + "=? and " + PdWgBasic.DATE
				+ " between ? and ? ", new String[] { uid + "", from, to });
		while (cursor.moveToNext()) {
			weight.put(cursor.getString(1), cursor.getDouble(0));
		}
		cursor.close();
		return weight;

	}

	/** 获得最近一次的体重 **/
	public double getWeightRecentlyBasicData() {
		SQLiteDatabase database = dbHelper.getReadableDatabase();

		Cursor cursor = database.rawQuery("select " + PdWgBasic.WEIGHT + ","
				+ PdWgBasic.DATE + " from " + PdWgBasic.TABLE_NAME + " where "
				+ PdWgBasic.UID + "=?  ORDER BY " + PdWgBasic.DATE + " DESC",
				new String[] { uid + "" });
		if (cursor.moveToFirst()) {
			return cursor.getDouble(0);
		}
		cursor.close();
		return 0.0;

	}

	/** 获得某天前最近一次的体重 **/
	public double getWeightRecently(String date) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();

		Cursor cursor = database.rawQuery("select " + PdWgBasic.WEIGHT + ","
				+ PdWgBasic.DATE + " from " + PdWgBasic.TABLE_NAME + " where "
				+ PdWgBasic.UID + "=? and " + PdWgBasic.DATE + "<=? ORDER BY "
				+ PdWgBasic.DATE + " DESC", new String[] { uid + "", date });
		if (cursor.moveToFirst()) {
			return cursor.getDouble(0);
		}
		cursor.close();
		return 0.0;

	}

	/** 获得bmi基本数据 **/

	public Map<String, Double> getBmiBasicData(String from, String to) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Map<String, Double> bmi = new HashMap<String, Double>();
		Cursor cursor = database.rawQuery("select " + PdWgBasic.BMI + ","
				+ PdWgBasic.DATE + " from " + PdWgBasic.TABLE_NAME + " where "
				+ PdWgBasic.UID + "=? and " + PdWgBasic.DATE
				+ " between ? and ? ", new String[] { uid + "", from, to });
		while (cursor.moveToNext()) {
			bmi.put(cursor.getString(1), cursor.getDouble(0));
		}
		cursor.close();
		return bmi;

	}

	/** 获得体重全部基本数据 **/
	public Map<String, Double> getWeightBasicDataFull(String from, String to) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Map<String, Double> weight = new HashMap<String, Double>();
		Cursor cursor = database.rawQuery("select " + PdWgBasic.WEIGHT + ","
				+ PdWgBasic.BMI + "," + PdWgBasic.FAT + "," + PdWgBasic.WATER
				+ "," + PdWgBasic.MUSCLE + "," + PdWgBasic.BONE + ","
				+ PdWgBasic.DATE + " from " + PdWgBasic.TABLE_NAME + " where "
				+ PdWgBasic.UID + "=? and " + PdWgBasic.DATE
				+ " between ? and ? ", new String[] { uid + "", from, to });
		while (cursor.moveToNext()) {
			weight.put(cursor.getString(1), cursor.getDouble(0));
		}
		cursor.close();
		return weight;

	}

	/** 存储GPS轨迹数据 **/
	public void saveGpsTrack(GPSEntity entity) {

		SQLiteDatabase database = dbHelper.getWritableDatabase();
		if (entity == null)
			return;

		ContentValues values = new ContentValues();
		values.put(GPSTrack._ID, entity.getId());
		values.put(GPSTrack.UID, uid);
		values.put(GPSTrack.DATE, entity.time);
		values.put(GPSTrack.STEP, entity.steps);
		values.put(GPSTrack.CALORIE, entity.calorie);
		values.put(GPSTrack.DISTANCE, entity.distance);
		values.put(GPSTrack.DURATION, entity.durance);
		values.put(GPSTrack.POINTS, entity.points);
		try {
			database.insert(GPSTrack.TABLE_NAME, null, values);
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.v("InsertGpsTrack", e.toString());
		}
		// database.close();
	}

	/** 修改gpstrack的数据 **/
	public void updateGPSTrack(int id, GPSEntity entity) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.execSQL("update " + GPSTrack.TABLE_NAME + " set "
				+ GPSTrack.STEP + "=? ," + GPSTrack.CALORIE + "=? ,"
				+ GPSTrack.DISTANCE + "=? ," + GPSTrack.DURATION + "=?,"
				+ GPSTrack.POINTS + "=? " + "where " + GPSTrack._ID + " =?",
				new Object[] { entity.steps, entity.calorie, entity.distance,
						entity.durance, entity.points, id });

		MyLog.e("updategps", "update " + GPSTrack.TABLE_NAME + " set "
				+ GPSTrack.STEP + "=? ," + GPSTrack.CALORIE + "=? ,"
				+ GPSTrack.DISTANCE + "=? ," + GPSTrack.DURATION + "=?,"
				+ GPSTrack.POINTS + "=? " + "where " + GPSTrack._ID + " =?");
	}

	/** 存储GPS轨迹点数据 **/
	public void saveGpsPoints(GPSPointEntity entity) {

		SQLiteDatabase database = dbHelper.getWritableDatabase();
		if (entity == null)
			return;

		ContentValues values = new ContentValues();
		values.put(GPSPoints.TID, entity.getTid());
		values.put(GPSPoints.TIME, entity.getDateTime());
		values.put(GPSPoints.LATITUDE, entity.getLatitude());
		values.put(GPSPoints.lONGTITUDE, entity.getLongitude());
		values.put(GPSPoints.SPEED, entity.getSpeed());
		values.put(GPSPoints.ACCURACY, entity.getAccuracy());
		try {
			database.insert(GPSPoints.TABLE_NAME, null, values);
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.v("InsertGpsPoints", e.toString());
		}
		// database.close();

	}

	/** 存储运动原始数据 **/
	public void SaveSpRawData(String date, byte[] data, int type) {

		SQLiteDatabase database = dbHelper.getWritableDatabase();

		int id = CheckSpRawIsExit(date, data[5]);
		if (id == -1) {
			ContentValues values = new ContentValues();
			values.put(PdSpRaw.UID, uid);
			values.put(PdSpRaw.DATE, date);
			values.put(PdSpRaw.HOUR, data[5]);
			values.put(PdSpRaw.DATA, data);
			values.put(PdSpRaw.TYPE, type);
			values.put(PdSpRaw.UPLOAD, 1);

			try {
				database.insert(PdSpRaw.TABLE_NAME, null, values);
			} catch (Exception e) {
				// TODO: handle exception
				MyLog.v("InsertPdSpRaw", e.toString());
			}
		} else {
			// update raw data
			ContentValues values = new ContentValues();
			values.put(PdSpRaw.DATA, data);
			values.put(PdSpRaw.UPLOAD, 1);
			String[] args = { String.valueOf(id) };
			try {
				database.update(PdSpRaw.TABLE_NAME, values, PdSpRaw._ID + "=?",
						args);
			} catch (Exception e) {
				// TODO: handle exception
				MyLog.v("InsertPdSpRaw", e.toString());
			}
		}

	}

	// 检查数据是否存在
	public int CheckSpRawIsExit(String date, int hour) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		int id = -1;
		try {
			Cursor cursor = database.rawQuery("select " + PdSpRaw._ID
					+ " from " + PdSpRaw.TABLE_NAME + " where " + PdSpRaw.UID
					+ " = ? and " + PdSpRaw.DATE + "= ? and " + PdSpRaw.HOUR
					+ " =? ", new String[] { uid + "", date, hour + "" });

			if (cursor != null) {
				while (cursor.moveToNext()) {
					id = cursor.getInt(0);

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return id;
	}

	/** 存储运动原始数据 **/
	public void SaveSpRawData(String date, String data, int type) {

		SQLiteDatabase database = dbHelper.getWritableDatabase();
		try {
			String sql = String.format("insert into  " + PdSpRaw.TABLE_NAME
					+ " (" + PdSpRaw.UID + "," + PdSpRaw.DATE + ","
					+ PdSpRaw.DATA + "," + PdSpRaw.TYPE + "," + PdSpRaw.UPLOAD
					+ ") values (%1$s,'%2$s',x'%3$s',1,0)", uid, date, data,
					type);
			database.execSQL(sql);

		} catch (Exception e) {
			// TODO: handle exception
			MyLog.v("InsertPdSpRaw", e.toString());
		}
		// database.close();

	}

	public int CheckSpRawData(String date) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		int id = -1;
		Cursor cursor = null;
		try {
			cursor = database.rawQuery("select " + PdSpRaw._ID + " from "
					+ PdSpRaw.TABLE_NAME + " where " + PdSpRaw.UID
					+ " = ? and " + PdSpRaw.DATE + "=? ", new String[] {
					uid + "", date });

			if (cursor != null) {
				while (cursor.moveToNext()) {
					id = cursor.getInt(0);

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return id;

	}

	public void deleteSpRawData(int id) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		try {
			database.execSQL("delete from " + PdSpRaw.TABLE_NAME + " where "
					+ PdSpRaw._ID + " = ? ", new String[] { id + "" });

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	boolean jk1 = true;

	/***
	 * 对详细数据解析
	 * 
	 * @param time
	 * @return
	 */
	public HashMap<Integer, HashMap<String, ArrayList<Integer>>> parser0802(
			String time) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = null;
		HashMap<Integer, HashMap<String, ArrayList<Integer>>> data = new HashMap<Integer, HashMap<String, ArrayList<Integer>>>();
		try {
			cursor = database.rawQuery("select " + PdSpRaw.DATE + ","
					+ PdSpRaw.DATA + " from " + PdSpRaw.TABLE_NAME + " where "
					+ PdSpRaw.UID + " = ? and " + PdSpRaw.DATE + "=? ",
					new String[] { uid + "", time });
			if (cursor != null) {
				while (cursor.moveToNext()) {// 一天的记录条数
					if (cursor.isNull(1)) {
						cursor.close();
						return null;
					}

					byte[] temp = cursor.getBlob(1);// 一条data数据的blob
					for (int i = 0; i < temp.length; i = i + 114) {
						int hour = temp[5 + i];
						HashMap<String, ArrayList<Integer>> hourData = new HashMap<String, ArrayList<Integer>>();

						int j = 6;
						ArrayList<Integer> stepArr = new ArrayList<Integer>();
						for (int z = 0; z < 24; z += 2) {
							int step = (temp[z + j] & 0x000000ff)
									+ (temp[z + j + 1] & 0x000000ff) * 256;
							stepArr.add(step);
						}
						hourData.put("step", stepArr);

						ArrayList<Integer> midArr = new ArrayList<Integer>();
						for (int z = 60; z < 72; z++) {
							int sec_mid = (temp[j + z] & 0x000000ff) * 2;
							midArr.add(sec_mid);
						}
						hourData.put("mid", midArr);
						ArrayList<Integer> slowArr = new ArrayList<Integer>();
						for (int z = 48; z < 60; z++) {
							int sec_slow = (temp[j + z] & 0x000000ff) * 2;
							slowArr.add(sec_slow);
						}
						hourData.put("slow", slowArr);
						ArrayList<Integer> fastArr = new ArrayList<Integer>();
						for (int z = 72; z < 84; z++) {
							int sec_fast = (temp[j + z] & 0x000000ff) * 2;
							fastArr.add(sec_fast);
						}
						hourData.put("fast", fastArr);
						data.put(hour, hourData);
					}

				}

				cursor.close();

			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();

		} finally {
			if (cursor != null) {
				cursor.close();
			}

		}
		return data;
	}

	// 读取每日的详细数据
	public int[] getSpRawData(String time) {

		// 新改成24小时的柱状图
		// int step[]=new int[24];
		int step[] = new int[72];
		int hour = 0;
		int tempstep = 0;
		int tempstep1 = 0;
		int tempstep2 = 0;
		if (jk1) {
			SQLiteDatabase database = dbHelper.getReadableDatabase();
			Cursor cursor = null;
			try {
				cursor = database.rawQuery("select " + PdSpRaw.DATE + ","
						+ PdSpRaw.DATA + " from " + PdSpRaw.TABLE_NAME
						+ " where " + PdSpRaw.UID + " = ? and " + PdSpRaw.DATE
						+ "=? ", new String[] { uid + "", time });
				if (cursor != null) {
					while (cursor.moveToNext()) {// 一天的记录条数
						if (cursor.isNull(1)) {
							cursor.close();
							return step;
						}
						byte[] temp = cursor.getBlob(1);// 一条data数据的blob

						for (int i = 0; i < temp.length; i = i + 114) {// 每小时有114字节
							hour = temp[5 + i];
							hour = hour * 3;
							tempstep = 0;
							tempstep1 = 0;
							tempstep2 = 0;
							for (int j = 0; j < 4; j++) {// 每五分钟
								tempstep += (temp[j * 2 + i + 6 + 0] & 0x000000ff)
										+ (temp[j * 2 + i + 6 + 1] & 0x000000ff)
										* 256;
							}
							step[hour] = (tempstep > step[hour]) ? tempstep
									: step[hour];
							System.out.println("hour" + hour + "::"
									+ step[hour]);
							for (int j = 4; j < 8; j++) {// 每五分钟
								tempstep1 += (temp[j * 2 + i + 6 + 0] & 0x000000ff)
										+ (temp[j * 2 + i + 6 + 1] & 0x000000ff)
										* 256;
							}

							step[hour + 1] = (tempstep1 > step[hour + 1]) ? tempstep1
									: step[hour + 1];
							System.out.println("hour" + (hour + 1) + "::"
									+ step[hour + 1]);
							for (int j = 8; j < 12; j++) {// 每五分钟
								tempstep2 += (temp[j * 2 + i + 6 + 0] & 0x000000ff)
										+ (temp[j * 2 + i + 6 + 1] & 0x000000ff)
										* 256;
							}
							step[hour + 2] = (tempstep2 > step[hour + 2]) ? tempstep2
									: step[hour + 2];
							System.out.println("hour" + (hour + 2) + "::"
									+ step[hour + 2]);
						}
					}

					cursor.close();
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}

		} else {
			for (int i = 0; i < step.length; i++) {
				// step[i]= 20 + 20*i;
				/*
				 * if(i%3==0) step[i]= 200; if(i%3==1) step[i]= 600; if(i%3==2)
				 * step[i]= 1500;
				 */
				if (i < 20)
					step[i] = 200;
				else {
					if (i < 45)
						step[i] = 600;
					else {
						step[i] = 1500;
					}
				}
			}
		}

		return step;

	}

	// 读取每日的详细数据
	public int[] getSpRawDataSleep(String time) {

		// 新改成24小时的柱状图
		// int step[]=new int[24];
		int step[] = new int[72 * 4];
		for (int i = 0; i < step.length; i++) {
			step[i] = -1;
		}
		int hour = 0;
		int tempstep = 0;
		int tempstep1 = 0;
		int tempstep2 = 0;
		if (jk1) {
			SQLiteDatabase database = dbHelper.getReadableDatabase();

			try {
				Cursor cursor = database.rawQuery("select " + PdSpRaw.DATE
						+ "," + PdSpRaw.DATA + " from " + PdSpRaw.TABLE_NAME
						+ " where " + PdSpRaw.UID + " = ? and " + PdSpRaw.DATE
						+ "=? ", new String[] { uid + "", time });
				if (cursor != null) {
					while (cursor.moveToNext()) {// 一天的记录条数
						if (cursor.isNull(1)) {
							cursor.close();
							return step;
						}
						byte[] temp = cursor.getBlob(1);// 一条data数据的blob

						for (int i = 0; i < temp.length; i = i + 114) {// 每小时有114字节
							hour = temp[5 + i];
							hour = hour * 12;
							tempstep = 0;
							tempstep1 = 0;
							tempstep2 = 0;
							for (int j = 0; j < 4; j++) {// 每五分钟
								tempstep = (temp[j * 2 + i + 6 + 0] & 0x000000ff)
										+ (temp[j * 2 + i + 6 + 1] & 0x000000ff)
										* 256;
								step[hour + j] = (tempstep > step[hour + j]) ? tempstep
										: step[hour + j];
							}
							for (int j = 4; j < 8; j++) {// 每五分钟
								tempstep1 = (temp[j * 2 + i + 6 + 0] & 0x000000ff)
										+ (temp[j * 2 + i + 6 + 1] & 0x000000ff)
										* 256;
								step[hour + j] = (tempstep1 > step[hour + j]) ? tempstep1
										: step[hour + j];
							}
							for (int j = 8; j < 12; j++) {// 每五分钟
								tempstep2 = (temp[j * 2 + i + 6 + 0] & 0x000000ff)
										+ (temp[j * 2 + i + 6 + 1] & 0x000000ff)
										* 256;
								step[hour + j] = (tempstep2 > step[hour + j]) ? tempstep2
										: step[hour + j];
							}

						}
					}

					cursor.close();
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}

		} else {
			for (int i = 0; i < step.length; i++) {
				// step[i]= 20 + 20*i;
				/*
				 * if(i%3==0) step[i]= 200; if(i%3==1) step[i]= 600; if(i%3==2)
				 * step[i]= 1500;
				 */
				if (i < 20)
					step[i] = 200;
				else {
					if (i < 45)
						step[i] = 600;
					else {
						step[i] = 1500;
					}
				}
			}
		}

		return step;

	}

	// 每日卡路里
	public int[] getSpCalRawData(String time) {

		// 新改成24小时的柱状图
		// int step[]=new int[24];
		int step[] = new int[72];
		int hour = 0;
		int tempstep = 0;
		int tempstep1 = 0;
		int tempstep2 = 0;
		if (jk1) {
			SQLiteDatabase database = dbHelper.getReadableDatabase();

			try {
				Cursor cursor = database.rawQuery("select " + PdSpRaw.DATE
						+ "," + PdSpRaw.DATA + " from " + PdSpRaw.TABLE_NAME
						+ " where " + PdSpRaw.UID + " = ? and " + PdSpRaw.DATE
						+ "=? ", new String[] { uid + "", time });
				if (cursor != null) {
					while (cursor.moveToNext()) {// 一天的记录条数
						if (cursor.isNull(1)) {
							cursor.close();
							return step;
						}
						byte[] temp = cursor.getBlob(1);// 一条data数据的blob
						for (int i = 0; i < temp.length; i = i + 114) {// 每小时有114字节
							hour = temp[5 + i];
							hour = hour * 3;
							tempstep = 0;
							tempstep1 = 0;
							tempstep2 = 0;
							for (int j = 0; j < 4; j++) {// 每五分钟
								tempstep += (temp[j * 2 + i + 30 + 0] & 0x000000ff)
										+ (temp[j * 2 + i + 30 + 1] & 0x000000ff)
										* 256;
							}
							step[hour] = (tempstep > step[hour]) ? tempstep
									: step[hour];
							for (int j = 4; j < 8; j++) {// 每五分钟
								tempstep1 += (temp[j * 2 + i + 30 + 0] & 0x000000ff)
										+ (temp[j * 2 + i + 30 + 1] & 0x000000ff)
										* 256;
							}

							step[hour + 1] = (tempstep1 > step[hour + 1]) ? tempstep1
									: step[hour + 1];
							for (int j = 8; j < 12; j++) {// 每五分钟
								tempstep2 += (temp[j * 2 + i + 30 + 0] & 0x000000ff)
										+ (temp[j * 2 + i + 30 + 1] & 0x000000ff)
										* 256;
							}
							step[hour + 2] = (tempstep2 > step[hour + 2]) ? tempstep2
									: step[hour + 2];
						}
					}

					cursor.close();
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}

		} else {
			for (int i = 0; i < step.length; i++) {
				// step[i]= 20 + 20*i;
				/*
				 * if(i%3==0) step[i]= 200; if(i%3==1) step[i]= 600; if(i%3==2)
				 * step[i]= 1500;
				 */
				if (i < 20)
					step[i] = 200;
				else {
					if (i < 45)
						step[i] = 600;
					else {
						step[i] = 1500;
					}
				}
			}
		}

		return step;

	}

	public int[] getSpDisRawData(String time) {

		// 新改成24小时的柱状图
		// int step[]=new int[24];
		int step[] = new int[72];
		int hour = 0;
		int tempstep = 0;
		int tempstep1 = 0;
		int tempstep2 = 0;
		if (jk1) {
			SQLiteDatabase database = dbHelper.getReadableDatabase();

			try {
				Cursor cursor = database.rawQuery("select " + PdSpRaw.DATE
						+ "," + PdSpRaw.DATA + " from " + PdSpRaw.TABLE_NAME
						+ " where " + PdSpRaw.UID + " = ? and " + PdSpRaw.DATE
						+ "=? ", new String[] { uid + "", time });
				if (cursor != null) {
					while (cursor.moveToNext()) {// 一天的记录条数
						if (cursor.isNull(1)) {
							cursor.close();
							return step;
						}
						byte[] temp = cursor.getBlob(1);// 一条data数据的blob
						for (int i = 0; i < temp.length; i = i + 114) {// 每小时有114字节
							hour = temp[5 + i];
							hour = hour * 3;
							tempstep = 0;
							tempstep1 = 0;
							tempstep2 = 0;
							for (int j = 0; j < 4; j++) {// 每五分钟
								tempstep += (temp[j * 2 + i + 54 + 0] & 0x000000ff)
										+ (temp[j * 2 + i + 6 + 1] & 0x000000ff)
										* 256;
							}
							step[hour] = (tempstep > step[hour]) ? Math
									.round(tempstep / 100f) : step[hour];
							for (int j = 4; j < 8; j++) {// 每五分钟
								tempstep1 += (temp[j * 2 + i + 54 + 0] & 0x000000ff)
										+ (temp[j * 2 + i + 54 + 1] & 0x000000ff)
										* 256;
							}

							step[hour + 1] = (tempstep1 > step[hour + 1]) ? Math
									.round(tempstep1 / 100f) : step[hour + 1];
							for (int j = 8; j < 12; j++) {// 每五分钟
								tempstep2 += (temp[j * 2 + i + 54 + 0] & 0x000000ff)
										+ (temp[j * 2 + i + 54 + 1] & 0x000000ff)
										* 256;
							}
							step[hour + 2] = (tempstep2 > step[hour + 2]) ? Math
									.round(tempstep2 / 100f) : step[hour + 2];
						}
					}

					cursor.close();
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}

		} else {
			for (int i = 0; i < step.length; i++) {
				// step[i]= 20 + 20*i;
				/*
				 * if(i%3==0) step[i]= 200; if(i%3==1) step[i]= 600; if(i%3==2)
				 * step[i]= 1500;
				 */
				if (i < 20)
					step[i] = 200;
				else {
					if (i < 45)
						step[i] = 600;
					else {
						step[i] = 1500;
					}
				}
			}
		}

		return step;

	}

	/** 根据id号查询轨迹 **/
	public ArrayList<GPSEntity> getGPSTrackCursorById(int m_runId) {
		// TODO Auto-generated method stub
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		ArrayList<GPSEntity> list = new ArrayList<GPSEntity>();
		try {
			// id,uid,date,step,cal,dis,duration,points
			Cursor cursor = database.rawQuery("select * from "
					+ GPSTrack.TABLE_NAME + " where " + GPSTrack.UID
					+ "=? and " + GPSTrack._ID + "=?", new String[] { uid + "",
					m_runId + "" });
			while (cursor.moveToNext()) {
				GPSEntity entity = new GPSEntity();
				entity.setId(cursor.getInt(0));
				entity.time = cursor.getString(2);
				entity.steps = cursor.getInt(3);
				entity.calorie = cursor.getDouble(4);
				entity.distance = cursor.getDouble(5);
				entity.durance = cursor.getInt(6);
				entity.points = cursor.getInt(7);
				list.add(entity);
			}
			cursor.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return list;

	}

	/**
	 * 全部轨迹
	 * 
	 * @return
	 */
	public ArrayList<GPSEntity> getAllGPSTrack() {
		// TODO Auto-generated method stub
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		ArrayList<GPSEntity> list = new ArrayList<GPSEntity>();
		try {
			// id,uid,date,step,cal,dis,duration,points
			Cursor cursor = database.rawQuery("select * from "
					+ GPSTrack.TABLE_NAME + " where " + GPSTrack.UID
					+ "=? order by " + GPSTrack._ID + " DESC",
					new String[] { uid + "" });
			while (cursor.moveToNext()) {
				GPSEntity entity = new GPSEntity();
				entity.setId(cursor.getInt(0));
				entity.time = cursor.getString(2);
				entity.steps = cursor.getInt(3);
				entity.calorie = cursor.getDouble(4);
				entity.distance = cursor.getDouble(5);
				entity.durance = cursor.getInt(6);
				entity.points = cursor.getInt(7);
				list.add(entity);
			}
			cursor.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return list;

	}

	/** 根据轨迹点查询轨迹点数据 **/
	public ArrayList<GPSPointEntity> getGPSPointCursor(int m_runId) {
		// TODO Auto-generated method stub
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		ArrayList<GPSPointEntity> list = new ArrayList<GPSPointEntity>();
		try {
			// id,tid,time,lat,long,speed,acc
			Cursor cursor = database.rawQuery("select * from "
					+ GPSPoints.TABLE_NAME + " where " + GPSPoints.TID + "=?",
					new String[] { m_runId + "" });

			while (cursor.moveToNext()) {
				GPSPointEntity infoEntity = new GPSPointEntity();
				infoEntity.setIdd(cursor.getInt(1));
				infoEntity.setDateTime(cursor.getString(2));
				infoEntity.setLatitude(cursor.getDouble(3));
				infoEntity.setLongitude(cursor.getDouble(4));
				infoEntity.setSpeed(cursor.getDouble(5));
				infoEntity.setAccuracy(cursor.getDouble(6));
				list.add(infoEntity);
			}
			cursor.close();

		} catch (Exception e) {
			// TODO: handle exception\
			e.printStackTrace();
		}

		return list;
	}

	public Cursor getGPSPointCursor2(int m_runId) {
		// TODO Auto-generated method stub
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		ArrayList<GPSPointEntity> list = new ArrayList<GPSPointEntity>();
		Cursor cursor = null;
		try {
			// id,tid,time,lat,long,speed,acc
			cursor = database.rawQuery("select * from " + GPSPoints.TABLE_NAME
					+ " where " + GPSPoints.TID + "=?", new String[] { m_runId
					+ "" });

		} catch (Exception e) {
			// TODO: handle exception\
			e.printStackTrace();
		}

		return cursor;
	}

	// 更新俱乐部列表
	public void UpdateClubInfo(String clubid, String name, String membernum,
			String ispublic, String province, String city, String district,
			String page, String logo) {

		SQLiteDatabase database = dbHelper.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(ClubInfo.CLUBID, clubid);
		values.put(ClubInfo.NAME, name);
		values.put(ClubInfo.MEMBERNUM, membernum);
		values.put(ClubInfo.ISPUBLIC, ispublic);
		values.put(ClubInfo.PROVINCE, province);
		values.put(ClubInfo.CITY, city);
		values.put(ClubInfo.DISTRICT, district);
		values.put(ClubInfo.INDEX, page);
		values.put(ClubInfo.LOGO, logo);
		values.put(ClubInfo.UID, uid);

		try {
			int i = database.update(ClubInfo.TABLE_NAME, values,
					" clubid=? and uid=?", new String[] { clubid, uid + "" });
			if (i == 0) {
				database.insert(ClubInfo.TABLE_NAME, null, values);
			}
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.v("ClubInfo", e.toString());
		}

	}

	/***
	 * 查询指定位置的俱乐部信息
	 * 
	 * @param page
	 *            显示第page页 每页五个记录
	 * @return 符合条件的俱乐部
	 */
	public ArrayList<HashMap<String, String>> getClubFromDb(int page) {

		ArrayList<HashMap<String, String>> alist = new ArrayList<HashMap<String, String>>();
		SQLiteDatabase database = dbHelper.getReadableDatabase();

		Cursor cursor;

		cursor = database.rawQuery("select * from " + ClubInfo.TABLE_NAME
				+ " where uid=? ORDER BY " + ClubInfo.MEMBERNUM
				+ " DESC limit " + page * 10 + ",105;",
				new String[] { uid + "" });

		while (cursor.moveToNext()) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(ClubInfo.CLUBID,
					cursor.getString(cursor.getColumnIndex(ClubInfo.CLUBID)));
			map.put(ClubInfo.NAME,
					cursor.getString(cursor.getColumnIndex(ClubInfo.NAME)));
			map.put(ClubInfo.MEMBERNUM,
					cursor.getString(cursor.getColumnIndex(ClubInfo.MEMBERNUM)));
			map.put(ClubInfo.ISPUBLIC,
					cursor.getString(cursor.getColumnIndex(ClubInfo.ISPUBLIC)));
			map.put(ClubInfo.PROVINCE,
					cursor.getString(cursor.getColumnIndex(ClubInfo.PROVINCE)));
			map.put(ClubInfo.CITY,
					cursor.getString(cursor.getColumnIndex(ClubInfo.CITY)));
			map.put(ClubInfo.DISTRICT,
					cursor.getString(cursor.getColumnIndex(ClubInfo.DISTRICT)));
			map.put(ClubInfo.INDEX,
					cursor.getString(cursor.getColumnIndex(ClubInfo.INDEX)));
			map.put(ClubInfo.LOGO,
					cursor.getString(cursor.getColumnIndex(ClubInfo.LOGO)));
			alist.add(map);
		}
		cursor.close();
		return alist;
	}

	public void delClubFromDb(String clubid) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		database.rawQuery("delete from " + ClubInfo.TABLE_NAME
				+ " where uid=? and clubid=?",
				new String[] { "" + uid, clubid });
	}

	// 更新俱乐部详情
	public void UpdateClubDetail(String clubid, String name, String membernum,
			String ispublic, String province, String city, String district,
			String intro, String admin_username, String admin_uid,
			String groupnum, String logo) {

		SQLiteDatabase database = dbHelper.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(ClubDetail.CLUBID, clubid);
		values.put(ClubDetail.NAME, name);
		values.put(ClubDetail.MEMBERNUM, membernum);
		values.put(ClubDetail.ISPUBLIC, ispublic);
		values.put(ClubDetail.PROVINCE, province);
		values.put(ClubDetail.CITY, city);
		values.put(ClubDetail.DISTRICT, district);
		values.put(ClubDetail.ADMIN_NAME, admin_username);
		values.put(ClubDetail.ADMIN_UID, admin_uid);
		values.put(ClubDetail.INTRO, intro);
		values.put(ClubDetail.GROUPNUM, groupnum);
		values.put(ClubDetail.LOGO, logo);
		values.put(ClubDetail.UID, uid);

		try {
			int i = database.update(ClubDetail.TABLE_NAME, values, "clubid=? ",
					new String[] { clubid });
			if (i == 0) {
				database.insert(ClubDetail.TABLE_NAME, null, values);
			}
		} catch (Exception e) {
			// TODO: handle exception
			MyLog.v("ClubDetail", e.toString());
		}

	}

	/***
	 * 查询俱乐部详情
	 * 
	 * @param clubid
	 *            俱乐部id
	 * @return 俱乐部信息
	 */
	public ArrayList<HashMap<String, String>> getClubDetailFromDb(String clubid) {
		ArrayList<HashMap<String, String>> alist = new ArrayList<HashMap<String, String>>();
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.rawQuery("select * from "
				+ ClubDetail.TABLE_NAME + " where clubid =? ",
				new String[] { clubid });

		while (cursor.moveToNext()) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(ClubDetail.CLUBID,
					cursor.getString(cursor.getColumnIndex(ClubDetail.CLUBID)));
			map.put(ClubDetail.NAME,
					cursor.getString(cursor.getColumnIndex(ClubDetail.NAME)));
			map.put(ClubDetail.MEMBERNUM, cursor.getString(cursor
					.getColumnIndex(ClubDetail.MEMBERNUM)));
			map.put(ClubDetail.ISPUBLIC, cursor.getString(cursor
					.getColumnIndex(ClubDetail.ISPUBLIC)));
			map.put(ClubDetail.PROVINCE, cursor.getString(cursor
					.getColumnIndex(ClubDetail.PROVINCE)));
			map.put(ClubDetail.CITY,
					cursor.getString(cursor.getColumnIndex(ClubDetail.CITY)));
			map.put(ClubDetail.DISTRICT, cursor.getString(cursor
					.getColumnIndex(ClubDetail.DISTRICT)));
			map.put(ClubDetail.ADMIN_NAME, cursor.getString(cursor
					.getColumnIndex(ClubDetail.ADMIN_NAME)));
			map.put(ClubDetail.ADMIN_UID, cursor.getString(cursor
					.getColumnIndex(ClubDetail.ADMIN_UID)));
			map.put(ClubDetail.INTRO,
					cursor.getString(cursor.getColumnIndex(ClubDetail.INTRO)));
			map.put(ClubDetail.GROUPNUM, cursor.getString(cursor
					.getColumnIndex(ClubDetail.GROUPNUM)));
			map.put(ClubDetail.LOGO,
					cursor.getString(cursor.getColumnIndex(ClubDetail.LOGO)));
			alist.add(map);
		}
		cursor.close();
		return alist;
	}

	public String getMyClubNameById(String clubid) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		Cursor cursor = database.rawQuery("select " + MyClub.NAME + " from "
				+ MyClub.TABLE_NAME + " where " + MyClub.CLUBID + " =? ",
				new String[] { clubid });
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			String ss = cursor.getString(cursor.getColumnIndex(MyClub.NAME));
			cursor.close();
			return ss;
		} else {
			cursor.close();
			return null;
		}
	}

	// 更新我的俱乐部
	public void UpdateMyClub(String clubid, String name, String membernum,
			String ispublic, String province, String city, String district,
			String logo) {

		SQLiteDatabase database = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(MyClub.CLUBID, clubid);
		values.put(MyClub.NAME, name);
		values.put(MyClub.MEMBERNUM, membernum);
		values.put(MyClub.ISPUBLIC, ispublic);
		values.put(MyClub.PROVINCE, province);
		values.put(MyClub.CITY, city);
		values.put(MyClub.DISTRICT, district);
		values.put(MyClub.LOGO, logo);
		values.put(MyClub.UID, uid);
		try {
			int i = database.update(MyClub.TABLE_NAME, values,
					"clubid=? and uid=?", new String[] { clubid, uid + "" });
			if (i == 0)
				database.insert(MyClub.TABLE_NAME, null, values);

		} catch (Exception e) {
			// TODO: handle exception
			MyLog.v("MyClub", e.toString());
		}

	}

	// 查询我的俱乐部
	public ArrayList<HashMap<String, String>> getMyClubFromDb(int page) {
		ArrayList<HashMap<String, String>> alist = new ArrayList<HashMap<String, String>>();
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor;
		if (page == -1) {
			cursor = database.rawQuery("select * from " + MyClub.TABLE_NAME
					+ " where uid=? ORDER BY " + MyClub.CLUBID + " DESC ",
					new String[] { uid + "" });
		} else {
			cursor = database.rawQuery("select * from " + MyClub.TABLE_NAME
					+ " where uid=? ORDER BY " + MyClub.CLUBID + " DESC limit "
					+ page * 5 + ",5;", new String[] { uid + "" });
		}
		while (cursor.moveToNext()) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(MyClub.CLUBID,
					cursor.getString(cursor.getColumnIndex(MyClub.CLUBID)));
			map.put(MyClub.NAME,
					cursor.getString(cursor.getColumnIndex(MyClub.NAME)));
			map.put(MyClub.MEMBERNUM,
					cursor.getString(cursor.getColumnIndex(MyClub.MEMBERNUM)));
			map.put(MyClub.ISPUBLIC,
					cursor.getString(cursor.getColumnIndex(MyClub.ISPUBLIC)));
			map.put(MyClub.PROVINCE,
					cursor.getString(cursor.getColumnIndex(MyClub.PROVINCE)));
			map.put(MyClub.CITY,
					cursor.getString(cursor.getColumnIndex(MyClub.CITY)));
			map.put(MyClub.DISTRICT,
					cursor.getString(cursor.getColumnIndex(MyClub.DISTRICT)));

			map.put(MyClub.LOGO,
					cursor.getString(cursor.getColumnIndex(MyClub.LOGO)));
			alist.add(map);
		}
		cursor.close();
		return alist;
	}

	/***
	 * 退出俱乐部时删除相关本地数据
	 * 
	 * @param clubid
	 *            俱乐部id
	 */
	public void quitClub(String clubid) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		int t = database.delete(MyClub.TABLE_NAME, " clubid=? and uid=? ",
				new String[] { clubid, uid + "" });

		int s = database.delete(ClubMember.TABLE_NAME, " clubid=? and uid=? ",
				new String[] { clubid, uid + "" });
	}

	/***
	 * 是否是该俱乐部成员
	 * 
	 * @param clubid
	 *            俱乐部id
	 * @return 如果是返回0 否 1
	 */
	public int isMyClub(String clubid) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor c = database.rawQuery("select * from " + MyClub.TABLE_NAME
				+ " where clubid=? and uid=?",
				new String[] { clubid, uid + "" });

		if (c.getCount() > 0) {
			return 0;
		} else {
			return 1;
		}
	}

	/***
	 * 存储组信息
	 * 
	 * @param clubid
	 *            俱乐部号
	 * @param name
	 *            组名
	 * @param groupid
	 *            组号
	 */
	public void UpdateGroup(String clubid, String name, String groupid) {

		SQLiteDatabase database = dbHelper.getReadableDatabase();
		ContentValues values = new ContentValues();
		values.put(ClubGroup.CLUBID, clubid);
		values.put(ClubGroup.NAME, name);
		values.put(ClubGroup.UID, uid);
		values.put(ClubGroup.GROUPID, groupid);
		try {
			int i = database.update(ClubGroup.TABLE_NAME, values,
					"clubid=? and groupid=?", new String[] { clubid, groupid });
			if (i == 0)
				database.insert(ClubGroup.TABLE_NAME, null, values);

		} catch (Exception e) {
			// TODO: handle exception
			MyLog.v("ClubGroup", e.toString());
		}

	}

	public ArrayList<HashMap<String, String>> getGroupInfo(String clubid) {
		ArrayList<HashMap<String, String>> alist = new ArrayList<HashMap<String, String>>();
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor;

		cursor = database.rawQuery("select * from " + ClubGroup.TABLE_NAME
				+ " where clubid=? and uid=? ",
				new String[] { clubid, uid + "" });

		while (cursor.moveToNext()) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(ClubGroup.CLUBID,
					cursor.getString(cursor.getColumnIndex(ClubGroup.CLUBID)));
			map.put(ClubGroup.NAME,
					cursor.getString(cursor.getColumnIndex(ClubGroup.NAME)));
			map.put(ClubGroup.GROUPID,
					cursor.getString(cursor.getColumnIndex(ClubGroup.GROUPID)));
			alist.add(map);
		}
		cursor.close();
		return alist;
	}

	// 删除某俱乐部分组信息
	public void deleteGroup(String clubid) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		database.execSQL("delete from " + ClubGroup.TABLE_NAME + " where "
				+ ClubGroup.CLUBID + " = " + clubid + " and " + ClubGroup.UID
				+ " = " + uid);
	}

	/***
	 * 检查数据库俱乐部排名是否是最新的
	 * 
	 * @param clubid
	 *            俱乐部id
	 * @return 最新为true
	 */
	public boolean checkRank(String clubid) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor c = database.rawQuery("select * from " + ClubRank.TABLE_NAME
				+ " where clubid=? ", new String[] { clubid });
		SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
		if (c.getCount() < 1)
			return false;
		c.moveToFirst();
		if (c.getString(c.getColumnIndex(ClubRank.TIME)).equals(
				s.format(new Date()))) {
			return true;
		} else {
			delRank();
			return false;
		}
	}

	public void delRank() {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		database.execSQL("delete  from " + ClubRank.TABLE_NAME);
	}

	public void UpdateClubRank(String step, String username, String gid,
			String time, String clubid, String photo, String ranUserID) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		ContentValues values = new ContentValues();
		values.put(ClubRank.STEP, Integer.parseInt(step));
		values.put(ClubRank.CLUBID, clubid);
		values.put(ClubRank.USERNAME, username);
		values.put(ClubRank.RANKUID, ranUserID);
		values.put(ClubRank.GID, gid);
		values.put(ClubRank.TIME, time);
		values.put(ClubRank.UID, uid);
		values.put(ClubRank.PHOTO, photo);
		Cursor c = database.rawQuery("select * from " + ClubRank.TABLE_NAME
				+ " where clubid=? ", new String[] { clubid + "" });
		while (c.moveToNext()) {
			if (!c.getString(c.getColumnIndex(ClubRank.TIME)).equals(time)) {
				delRank();
				break;
			}
		}
		c.close();
		database.insert(ClubRank.TABLE_NAME, null, values);
	}

	public ArrayList<UserHashMap<String, String>> getClubRank(String clubid,
			int page) {
		ArrayList<UserHashMap<String, String>> alist = new ArrayList<UserHashMap<String, String>>();
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.query(ClubRank.TABLE_NAME, new String[] {
				ClubRank.CLUBID, ClubRank.USERNAME, ClubRank.GID,
				ClubRank.STEP, ClubRank.TIME, ClubRank.RANKUID, ClubRank.PHOTO,
				ClubRank.UID }, " clubid=? ORDER BY " + ClubRank.STEP
				+ " DESC  limit " + (page * 10) + ",10;",
				new String[] { clubid }, null, null, null);
		while (cursor.moveToNext()) {
			UserHashMap<String, String> map = new UserHashMap<String, String>();
			map.put(ClubRank.CLUBID,
					cursor.getString(cursor.getColumnIndex(ClubRank.CLUBID)));
			map.put(ClubRank.USERNAME,
					cursor.getString(cursor.getColumnIndex(ClubRank.USERNAME)));
			map.put(ClubRank.GID,
					cursor.getString(cursor.getColumnIndex(ClubRank.GID)));
			map.put(ClubRank.STEP,
					cursor.getString(cursor.getColumnIndex(ClubRank.STEP)));
			map.put(ClubRank.TIME,
					cursor.getString(cursor.getColumnIndex(ClubRank.TIME)));
			map.put(ClubRank.RANKUID,
					cursor.getString(cursor.getColumnIndex(ClubRank.UID)));
			map.put(ClubRank.PHOTO,
					cursor.getString(cursor.getColumnIndex(ClubRank.PHOTO)));
			map.put(ClubRank.UID,
					cursor.getString(cursor.getColumnIndex(ClubRank.RANKUID)));
			if (!alist.contains(map)) {
				alist.add(map);
			}
		}
		cursor.close();
		return alist;
	}

	public String getGroupNameById(String gid, String clubid) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		String dname = "";
		try {
			Cursor c = database.rawQuery("select " + ClubGroup.NAME + " from "
					+ ClubGroup.TABLE_NAME + " where " + ClubGroup.GROUPID
					+ "=? and " + ClubGroup.UID + "=? and " + ClubGroup.CLUBID
					+ "=? ", new String[] { gid, uid + "", clubid });
			c.moveToFirst();

			try {
				int index = c.getColumnIndex(ClubGroup.NAME);
				dname = c.getString(index);
			} catch (Exception e) {
				dname = context.getString(R.string.nogroup);
			}
			c.close();
		} catch (Exception e) {
			dname = context.getString(R.string.nogroup);
		}
		return dname;
	}

	public void UpdateMemberInfo(String clubid, String gid, String username,
			String userid, String photo, String isadmin) {

		SQLiteDatabase database = dbHelper.getReadableDatabase();
		try {
			Cursor c = database.rawQuery("select " + ClubGroup.NAME + " from "
					+ ClubGroup.TABLE_NAME + " where " + ClubGroup.GROUPID
					+ "=? and " + ClubGroup.UID + "=? and " + ClubGroup.CLUBID
					+ "=? ", new String[] { gid, uid + "", clubid });
			c.moveToFirst();
			String dname = "";
			try {
				int index = c.getColumnIndex(ClubGroup.NAME);

				dname = c.getString(index);
			} catch (Exception e) {
				dname = context.getString(R.string.nogroup);
			}
			c.close();
			ContentValues values = new ContentValues();
			values.put(ClubMember.CLUBID, clubid);
			values.put(ClubMember.USERNAME, username);
			values.put(ClubMember.USERID, userid);
			values.put(ClubMember.GID, gid);
			values.put(ClubMember.ADMIN, isadmin);
			values.put(ClubMember.UID, uid);
			values.put(ClubMember.PHOTO, photo);
			values.put(ClubMember.DEPARTNAME, dname);

			int i = database.update(ClubMember.TABLE_NAME, values,
					" clubid=? and userid=? ", new String[] { clubid, userid });
			if (i == 0)
				database.insert(ClubMember.TABLE_NAME, null, values);

		} catch (Exception e) {
			// TODO: handle exception
			MyLog.v("ClubGroup", e.toString());
		}

	}

	public ArrayList<HashMap<String, String>> getMemberInfo(String clubid,
			String gid, int page) {
		ArrayList<HashMap<String, String>> alist = new ArrayList<HashMap<String, String>>();
		SQLiteDatabase database = dbHelper.getReadableDatabase();

		Cursor cursor;
		if (gid == null || gid.equals("") || gid.equals("0")) {
			cursor = database.rawQuery("select * from " + ClubMember.TABLE_NAME
					+ " where clubid=? and uid=? ORDER BY " + ClubMember.USERID
					+ " DESC limit " + (page * 8) + "," + 8 + ";",
					new String[] { clubid, uid + "" });
		} else {
			cursor = database.rawQuery("select * from " + ClubMember.TABLE_NAME
					+ " where clubid=? and gid=? and uid=? ORDER BY "
					+ ClubMember.USERID + " DESC limit " + (page * 8) + "," + 8
					+ ";", new String[] { clubid, gid, uid + "" });
		}
		while (cursor.moveToNext()) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(ClubMember.CLUBID,
					cursor.getString(cursor.getColumnIndex(ClubMember.CLUBID)));
			map.put(ClubMember.USERNAME, cursor.getString(cursor
					.getColumnIndex(ClubMember.USERNAME)));
			map.put(ClubMember.GID,
					cursor.getString(cursor.getColumnIndex(ClubMember.GID)));
			map.put(ClubMember.ADMIN,
					cursor.getString(cursor.getColumnIndex(ClubMember.ADMIN)));
			map.put(ClubMember.USERID,
					cursor.getString(cursor.getColumnIndex(ClubMember.USERID)));
			map.put(ClubMember.PHOTO,
					cursor.getString(cursor.getColumnIndex(ClubMember.PHOTO)));
			map.put(ClubMember.DEPARTNAME, cursor.getString(cursor
					.getColumnIndex(ClubMember.DEPARTNAME)));
			alist.add(map);
		}
		cursor.close();
		return alist;
	}

	/***
	 * 删除数据库成员
	 * 
	 * @param clubid
	 *            所在俱乐部
	 * @param u
	 *            删除的uid
	 */
	public boolean deleteMemberFromDB(String clubid, String u) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		int rows = database.delete(ClubMember.TABLE_NAME, " "
				+ ClubMember.CLUBID + "=? and " + ClubMember.USERID + "=? and "
				+ ClubMember.UID + "=? ", new String[] { clubid, u, uid + "" });
		if (rows < 1) {
			return false;
		} else {
			return true;
		}
	}

	/***
	 * 获得俱乐部成员
	 * 
	 * @param clubid
	 *            俱乐部id
	 * @param gid
	 *            分组id
	 * @return 符合条件的user[]
	 */
	public com.bandlink.air.friend.User[] getMemberReturnUser(String clubid,
			String gid) {

		SQLiteDatabase database = dbHelper.getReadableDatabase();

		Cursor cursor;
		if (gid == null || gid.equals("") || gid.equals("0")) {
			cursor = database.rawQuery("select * from " + ClubMember.TABLE_NAME
					+ " where clubid=? ", new String[] { clubid });
		} else {
			cursor = database.rawQuery("select * from " + ClubMember.TABLE_NAME
					+ " where clubid=? and gid=? ",
					new String[] { clubid, gid });
		}
		com.bandlink.air.friend.User[] users = new com.bandlink.air.friend.User[cursor
				.getCount()];
		int t = 0;
		while (cursor.moveToNext()) {
			users[t] = new com.bandlink.air.friend.User(
					cursor.getInt(cursor.getColumnIndex(ClubMember.USERID)),
					cursor.getString(cursor.getColumnIndex(ClubMember.USERNAME)),
					cursor.getString(cursor.getColumnIndex(ClubMember.PHOTO)),
					cursor.getInt(cursor.getColumnIndex(ClubMember.GID)));

			t++;

		}
		cursor.close();
		return users;
	}

	/***
	 * 获得该俱乐部最高步数
	 * 
	 * @param clubid
	 *            俱乐部id
	 * @return 最高步数
	 */
	public int getClubStep1st(String clubid) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.rawQuery("select " + ClubRank.STEP + " from "
				+ ClubRank.TABLE_NAME + " where uid=? and clubid=? ORDER BY "
				+ ClubRank.STEP + " DESC limit 0,1;", new String[] { uid + "",
				clubid });
		cursor.moveToFirst();
		int st = -1;
		try {
			st = cursor.getInt(cursor.getColumnIndex(ClubRank.STEP));
		} catch (Exception e) {
			st = -1;
		}
		cursor.close();
		return st;
	}

	/***
	 * 模糊查询我的俱乐部
	 * 
	 * @param name
	 *            名称
	 * @return 符合的项目
	 */
	public ArrayList<HashMap<String, String>> searchMyClub(String name) {
		ArrayList<HashMap<String, String>> alist = new ArrayList<HashMap<String, String>>();
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.rawQuery("select * from " + MyClub.TABLE_NAME
				+ " where uid=? and " + MyClub.NAME + " like ? ", new String[] {
				uid + "", "%" + name + "%" });
		while (cursor.moveToNext()) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(MyClub.CLUBID,
					cursor.getString(cursor.getColumnIndex(MyClub.CLUBID)));
			map.put(MyClub.NAME,
					cursor.getString(cursor.getColumnIndex(MyClub.NAME)));
			map.put(MyClub.MEMBERNUM,
					cursor.getString(cursor.getColumnIndex(MyClub.MEMBERNUM)));
			map.put(MyClub.ISPUBLIC,
					cursor.getString(cursor.getColumnIndex(MyClub.ISPUBLIC)));
			map.put(MyClub.PROVINCE,
					cursor.getString(cursor.getColumnIndex(MyClub.PROVINCE)));
			map.put(MyClub.CITY,
					cursor.getString(cursor.getColumnIndex(MyClub.CITY)));
			map.put(MyClub.DISTRICT,
					cursor.getString(cursor.getColumnIndex(MyClub.DISTRICT)));

			map.put(MyClub.LOGO,
					cursor.getString(cursor.getColumnIndex(MyClub.LOGO)));
			alist.add(map);
		}
		cursor.close();
		return alist;
	}

	public void SaveClubFeed(String clubid, String feedid, String userid,
			String content, String time) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		try {
			ContentValues values = new ContentValues();
			values.put(ClubFeed.CLUBID, clubid);
			values.put(ClubFeed.FEEDID, feedid);
			values.put(ClubFeed.USERID, userid);
			values.put(ClubFeed.CONTENT, content);
			values.put(ClubFeed.TIME, time);
			values.put(ClubFeed.UID, uid);
			int row = database.update(ClubFeed.TABLE_NAME, values,
					" feedid=? and clubid=? ", new String[] { feedid, clubid });
			if (row == 0)
				database.insert(ClubFeed.TABLE_NAME, null, values);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void UpdateContent(String feedid, String content) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		ContentValues values = new ContentValues();
		values.put(ClubFeed.CONTENT, content);
		database.update(ClubFeed.TABLE_NAME, values, " feedid=? and uid=? ",
				new String[] { feedid, uid + "" });
		// database.close();
	}

	/***
	 * 根据请求的页数获取该页所有动态
	 * 
	 * @param page
	 *            页数
	 * @return 动态
	 */
	public String getClubFeed(int page, String clubid) {
		JSONArray arr = new JSONArray();
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		int t = ((page - 1) * 10);
		Cursor cursor = database.rawQuery("select * from "
				+ ClubFeed.TABLE_NAME + " where clubid=? ORDER BY "
				+ ClubFeed.FEEDID + " DESC limit " + t + " ,10;",
				new String[] { clubid });

		while (cursor.moveToNext()) {

			try {
				arr.put(new JSONObject(cursor.getString(cursor
						.getColumnIndex(ClubFeed.CONTENT))));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return "{\"friend_dym\":" + arr.toString() + "}";
	}

	/***
	 * 通过feed_id 获取该动态内容
	 * 
	 * @param feedid
	 * @return 该动态内容
	 */
	public JSONObject getClubFeedById(String feedid) {

		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.rawQuery("select content from "
				+ ClubFeed.TABLE_NAME + " where uid=? and feedid=? ",
				new String[] { uid + "", feedid });
		cursor.moveToFirst();
		String js;

		try {
			js = cursor.getString(cursor.getColumnIndex(ClubFeed.CONTENT));
			JSONObject jss = new JSONObject(js);
			cursor.close();
			return jss;
		} catch (Exception e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
			cursor.close();
			return null;
		}

	}

	/*** 保存俱乐部动态评论 ***/
	public void SaveClubComment(String feedid, String clubid, String content,
			String commentid) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		try {
			ContentValues values = new ContentValues();
			values.put(ClubFeedComment.CLUBID, clubid);
			values.put(ClubFeedComment.FEEDID, feedid);
			values.put(ClubFeedComment.CONTENT, content);
			values.put(ClubFeedComment.COMMENTID, commentid);
			values.put(ClubFeedComment.UID, uid);
			int row = database.update(ClubFeedComment.TABLE_NAME, values,
					" commentid=? and uid=? ", new String[] { commentid,
							uid + "" });
			if (row == 0)
				database.insert(ClubFeedComment.TABLE_NAME, null, values);
		} catch (Exception e) {

		}

	}

	/***
	 * 通过feed_id获取评论记录
	 * 
	 * @param feedid
	 *            动态id
	 * @return 评论信息
	 */
	public JSONArray getClubCommentById(String feedid) {

		JSONArray arr = new JSONArray();
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.rawQuery("select content from "
				+ ClubFeedComment.TABLE_NAME + " where uid=? and feedid=? ",
				new String[] { uid + "", feedid });
		while (cursor.moveToNext()) {
			try {
				arr.put(new JSONObject(cursor.getString(cursor
						.getColumnIndex(ClubFeedComment.CONTENT))));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		cursor.close();
		return arr;

	}

	public void saveMyMatch(JSONArray array) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.execSQL("delete from " + MyMatch.TABLE_NAME);
		try {
			database.beginTransaction(); // 手动设置开始事务
			for (int i = 0; i < array.length(); i++) {
				JSONObject jsobj = array.getJSONObject(i);
				database.execSQL(
						"insert into "
								+ MyMatch.TABLE_NAME
								+ "(matchid,logo,uid,username,title,membernum,starttime,endtime,status,ispublic,target,ismember)"
								+ "values( ?,?,?,?,?,?,?,?,?,?,?,?)",
						new Object[] { Integer.parseInt(jsobj.getString("id")),
								jsobj.getString("poster"),
								Integer.parseInt(jsobj.getString("uid")),
								jsobj.getString("username"),
								jsobj.getString("title"),
								Integer.parseInt(jsobj.getString("membernum")),
								jsobj.getString("start_time"),
								jsobj.getString("end_time"),
								Integer.parseInt(jsobj.getString("status")),
								Integer.parseInt(jsobj.getString("is_public")),
								Integer.parseInt(jsobj.getString("target")),
								Integer.parseInt(jsobj.getString("ismember")) });

			}
			database.setTransactionSuccessful(); // 设置事务处理成功，不设置会自动回滚不提交
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			database.endTransaction(); // 处理完成
		}

	}

	public JSONArray getMyMatch(int index, int num) {
		JSONArray arr = new JSONArray();
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor;
		try {
			if (index == 0 && num == 0) {
				cursor = database.rawQuery("select * from "
						+ MyMatch.TABLE_NAME
						+ " order by status asc,starttime desc", null);
			} else {
				cursor = database.rawQuery(
						"select * from " + MyMatch.TABLE_NAME
								+ " order by starttime desc limit " + index
								+ "," + num, null);
			}
			Long today = (sdf.parse(sdf.format(new Date())).getTime()) / 1000;
			while (cursor.moveToNext()) {
				int status = cursor.getInt(9);
				String startTime = cursor.getString(7);
				String endTime = cursor.getString(8);
				Long s_time = Long.valueOf(startTime);
				Long e_time = Long.valueOf(endTime);
				int delay = 0;
				delay = Math.round((s_time - today) / (3600 * 24f));
				if (delay < 0) {
					delay = Math.round((e_time - today) / (3600 * 24f));
					status = 1;
					if (delay < 0) {
						delay = 0;
						status = 2;
					}
				}
				JSONObject ob = new JSONObject();
				if (cursor.getString(2).equals("")) {
					ob.put("logo", "images/racepic_150.png");
				} else {
					ob.put("logo", cursor.getString(2));
				}
				ob.put("id", cursor.getInt(1));
				ob.put("uid", cursor.getInt(3));
				ob.put("username", cursor.getString(4));
				ob.put("title", cursor.getString(5));
				ob.put("membernum", cursor.getInt(6));
				ob.put("start_time", startTime);
				ob.put("endt_ime", endTime);
				String flag = "";
				if (status == 0) {
					if (context != null) {
						flag = context.getResources().getString(
								R.string.gpsstart);
					}

				} else {
					if (context != null) {
						flag = context.getResources().getString(R.string.over);
					}
				}
				ob.put("flag", flag);
				ob.put("delay", delay);
				ob.put("status", status);
				ob.put("ispublic", cursor.getInt(10));
				ob.put("target", cursor.getInt(11));
				ob.put("ismember", cursor.getInt(12));
				arr.put(ob);
			}
			cursor.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return arr;

	}

	public void saveAllMatch(JSONArray array) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.execSQL("delete from " + AllMatch.TABLE_NAME);
		try {
			database.beginTransaction(); // 手动设置开始事务
			for (int i = 0; i < array.length(); i++) {
				JSONObject jsobj = array.getJSONObject(i);
				database.execSQL(
						"insert into "
								+ AllMatch.TABLE_NAME
								+ "(matchid,logo,uid,username,title,membernum,starttime,endtime,status,ispublic,target,ismember)"
								+ "values( ?,?,?,?,?,?,?,?,?,?,?,?)",
						new Object[] { Integer.parseInt(jsobj.getString("id")),
								jsobj.getString("poster"),
								Integer.parseInt(jsobj.getString("uid")),
								jsobj.getString("username"),
								jsobj.getString("title"),
								Integer.parseInt(jsobj.getString("membernum")),
								jsobj.getString("start_time"),
								jsobj.getString("end_time"),
								Integer.parseInt(jsobj.getString("status")),
								Integer.parseInt(jsobj.getString("is_public")),
								Integer.parseInt(jsobj.getString("target")),
								Integer.parseInt(jsobj.getString("ismember")) });

			}
			database.setTransactionSuccessful(); // 设置事务处理成功，不设置会自动回滚不提交
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			database.endTransaction(); // 处理完成
		}

	}

	public void saveClubMatch(JSONArray array, String clubid) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.execSQL("delete from " + ClubMatch.TABLE_NAME);
		try {
			database.beginTransaction(); // 手动设置开始事务
			for (int i = 0; i < array.length(); i++) {
				JSONObject jsobj = array.getJSONObject(i);
				database.execSQL(
						"insert into "
								+ ClubMatch.TABLE_NAME
								+ "(matchid,logo,uid,username,title,membernum,starttime,endtime,status,ispublic,target,ismember,clubid)"
								+ "values( ?,?,?,?,?,?,?,?,?,?,?,?,?)",
						new Object[] { Integer.parseInt(jsobj.getString("id")),
								jsobj.getString("poster"),
								Integer.parseInt(jsobj.getString("uid")),
								jsobj.getString("username"),
								jsobj.getString("title"),
								Integer.parseInt(jsobj.getString("membernum")),
								jsobj.getString("start_time"),
								jsobj.getString("end_time"),
								Integer.parseInt(jsobj.getString("status")),
								Integer.parseInt(jsobj.getString("is_public")),
								Integer.parseInt(jsobj.getString("target")),
								Integer.parseInt(jsobj.getString("ismember")),
								clubid });

			}
			database.setTransactionSuccessful(); // 设置事务处理成功，不设置会自动回滚不提交
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			database.endTransaction(); // 处理完成
		}

	}

	public void saveMoreAllMatch(JSONArray array) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.beginTransaction(); // 手动设置开始事务
		try {
			for (int i = 0; i < array.length(); i++) {
				JSONObject jsobj = array.getJSONObject(i);
				ContentValues values = new ContentValues();
				int matchid = Integer.parseInt(jsobj.getString("id"));
				values.put(AllMatch.MATCH_ID, matchid);
				values.put(AllMatch.LOGO, jsobj.getString("poster"));
				values.put(AllMatch.UID,
						Integer.parseInt(jsobj.getString("uid")));
				values.put(AllMatch.USERNAME, jsobj.getString("username"));
				values.put(AllMatch.MATCH_TITLE, jsobj.getString("title"));
				values.put(AllMatch.MEMBER_NUM,
						Integer.parseInt(jsobj.getString("membernum")));

				values.put(AllMatch.STARTTIME, jsobj.getString("start_time"));
				values.put(AllMatch.ENDTIME, jsobj.getString("end_time"));
				values.put(AllMatch.STATUS,
						Integer.parseInt(jsobj.getString("status")));
				values.put(AllMatch.ISPUBLIC,
						Integer.parseInt(jsobj.getString("is_public")));
				values.put(AllMatch.TARGET,
						Integer.parseInt(jsobj.getString("target")));
				values.put(AllMatch.ISMEMBER,
						Integer.parseInt(jsobj.getString("ismember")));
				int rows = database.update(AllMatch.TABLE_NAME, values,
						" matchid=? and uid=? ", new String[] { matchid + "",
								uid + "" });
				if (rows < 1) {
					database.insert(AllMatch.TABLE_NAME, null, values);
				}
			}
			database.setTransactionSuccessful(); // 设置事务处理成功，不设置会自动回滚不提交
		} catch (Exception e) {

		} finally {
			database.endTransaction(); // 处理完成
		}

	}

	public void saveMoreMyMatch(JSONArray array) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.beginTransaction(); // 手动设置开始事务
		try {
			for (int i = 0; i < array.length(); i++) {
				JSONObject jsobj = array.getJSONObject(i);
				ContentValues values = new ContentValues();
				int matchid = Integer.parseInt(jsobj.getString("id"));
				values.put(MyMatch.MATCH_ID, matchid);
				values.put(MyMatch.LOGO, jsobj.getString("poster"));
				values.put(MyMatch.UID,
						Integer.parseInt(jsobj.getString("uid")));
				values.put(MyMatch.USERNAME, jsobj.getString("username"));
				values.put(MyMatch.MATCH_TITLE, jsobj.getString("title"));
				values.put(MyMatch.MEMBER_NUM,
						Integer.parseInt(jsobj.getString("membernum")));

				values.put(MyMatch.STARTTIME, jsobj.getString("start_time"));
				values.put(MyMatch.ENDTIME, jsobj.getString("end_time"));
				values.put(MyMatch.STATUS,
						Integer.parseInt(jsobj.getString("status")));
				values.put(MyMatch.ISPUBLIC,
						Integer.parseInt(jsobj.getString("is_public")));
				values.put(MyMatch.TARGET,
						Integer.parseInt(jsobj.getString("target")));
				values.put(MyMatch.ISMEMBER,
						Integer.parseInt(jsobj.getString("ismember")));
				int rows = database.update(MyMatch.TABLE_NAME, values,
						" matchid=? and uid=? ", new String[] { matchid + "",
								uid + "" });
				if (rows < 1) {
					database.insert(MyMatch.TABLE_NAME, null, values);
				}

			}
			database.setTransactionSuccessful(); // 设置事务处理成功，不设置会自动回滚不提交
		} catch (Exception e) {

		} finally {
			database.endTransaction(); // 处理完成
		}

	}

	public JSONArray getAllMatch(int index, int num) {
		JSONArray arr = new JSONArray();
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		try {
			Cursor cursor = database.rawQuery("select * from "
					+ AllMatch.TABLE_NAME + " order by status asc  ", null);
			Long today = (sdf.parse(sdf.format(new Date())).getTime()) / 1000;
			while (cursor.moveToNext()) {
				int status = cursor.getInt(9);
				String startTime = cursor.getString(7);
				String endTime = cursor.getString(8);
				Long s_time = Long.valueOf(startTime);
				Long e_time = Long.valueOf(endTime);
				int delay = 0;
				delay = Math.round((s_time - today) / (3600 * 24f));
				if (delay < 0) {
					delay = Math.round((e_time - today) / (3600 * 24f));
					status = 1;
					if (delay < 0) {
						delay = 0;
						status = 2;
					}
				}
				JSONObject ob = new JSONObject();
				if (cursor.getString(2).equals("")) {
					ob.put("logo", "images/racepic_150.png");
				} else {
					ob.put("logo", cursor.getString(2));
				}
				ob.put("id", cursor.getInt(1));
				ob.put("uid", cursor.getInt(3));
				ob.put("username", cursor.getString(4));
				ob.put("title", cursor.getString(5));
				ob.put("membernum", cursor.getInt(6));
				ob.put("start_time", startTime);
				ob.put("end_time", endTime);
				ob.put("delay", delay);
				String flag = "";
				if (status == 0) {
					flag = context.getResources().getString(R.string.gpsstart);
				} else {
					flag = context.getResources().getString(R.string.over);
				}
				ob.put("flag", flag);
				ob.put("status", status);
				ob.put("ispublic", cursor.getInt(10));
				ob.put("target", cursor.getInt(11));
				ob.put("ismember", cursor.getInt(12));
				arr.put(ob);
			}
			cursor.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return arr;

	}

	// 俱乐部相关竞赛
	public JSONArray getClubMatch(int index, int num, String clubid) {
		JSONArray arr = new JSONArray();
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		try {
			Cursor cursor = database
					.rawQuery(
							"select * from "
									+ ClubMatch.TABLE_NAME
									+ " where clubid=? order by status asc,starttime desc  limit "
									+ index * 5 + "," + num,
							new String[] { clubid });
			Long today = (sdf.parse(sdf.format(new Date())).getTime()) / 1000;
			while (cursor.moveToNext()) {
				int status = cursor.getInt(9);
				String startTime = cursor.getString(7);
				String endTime = cursor.getString(8);
				Long s_time = Long.valueOf(startTime);
				Long e_time = Long.valueOf(endTime);
				int delay = 0;
				delay = Math.round((s_time - today) / (3600 * 24f));
				if (delay < 0) {
					delay = Math.round((e_time - today) / (3600 * 24f));
					status = 1;
					if (delay < 0) {
						delay = 0;
						status = 2;
					}
				}
				JSONObject ob = new JSONObject();
				if (cursor.getString(2).equals("")) {
					ob.put("logo", "images/racepic_150.png");
				} else {
					ob.put("logo", cursor.getString(2));
				}
				ob.put("id", cursor.getInt(1));
				ob.put("uid", cursor.getInt(3));
				ob.put("username", cursor.getString(4));
				ob.put("title", cursor.getString(5));
				ob.put("membernum", cursor.getInt(6));
				ob.put("start_time", startTime);
				ob.put("end_time", endTime);
				ob.put("delay", delay);
				ob.put("status", status);
				String flag = "";
				if (status == 0) {
					flag = context.getResources().getString(R.string.gpsstart);
				} else {
					flag = context.getResources().getString(R.string.over);
				}
				ob.put("flag", flag);
				ob.put("ispublic", cursor.getInt(10));
				ob.put("target", cursor.getInt(11));
				ob.put("ismember", cursor.getInt(12));
				ob.put("flag_btn", cursor.getInt(12) == 1 ? "已加入" : "马上加入");
				arr.put(ob);
			}
			cursor.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return arr;

	}

	public void saveSearchMatch(JSONArray array) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.execSQL("delete from " + SearchMatch.TABLE_NAME);
		try {
			database.beginTransaction(); // 手动设置开始事务
			for (int i = 0; i < array.length(); i++) {
				JSONObject jsobj = array.getJSONObject(i);
				database.execSQL(
						"insert into "
								+ SearchMatch.TABLE_NAME
								+ "(matchid,logo,uid,username,title,membernum,starttime,endtime,status,ispublic,target,ismember)"
								+ "values( ?,?,?,?,?,?,?,?,?,?,?,?)",
						new Object[] { Integer.parseInt(jsobj.getString("id")),
								jsobj.getString("poster"),
								Integer.parseInt(jsobj.getString("uid")),
								jsobj.getString("username"),
								jsobj.getString("title"),
								Integer.parseInt(jsobj.getString("membernum")),
								jsobj.getString("start_time"),
								jsobj.getString("end_time"),
								Integer.parseInt(jsobj.getString("status")),
								Integer.parseInt(jsobj.getString("is_public")),
								Integer.parseInt(jsobj.getString("target")),
								Integer.parseInt(jsobj.getString("ismember")) });

			}
			database.setTransactionSuccessful(); // 设置事务处理成功，不设置会自动回滚不提交
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			database.endTransaction(); // 处理完成
		}

	}

	public JSONArray getSearchMatch(int index, int num) {
		JSONArray arr = new JSONArray();
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor;
		try {
			if (index == 0 && num == 0) {
				cursor = database.rawQuery("select * from "
						+ SearchMatch.TABLE_NAME
						+ " order by status asc,starttime desc", null);
			} else {
				cursor = database.rawQuery(
						"select * from " + SearchMatch.TABLE_NAME
								+ " order by starttime desc limit " + index
								+ "," + num, null);
			}
			Long today = (sdf.parse(sdf.format(new Date())).getTime()) / 1000;
			while (cursor.moveToNext()) {
				int status = cursor.getInt(9);
				String startTime = cursor.getString(7);
				String endTime = cursor.getString(8);
				Long s_time = Long.valueOf(startTime);
				Long e_time = Long.valueOf(endTime);
				int delay = 0;
				delay = Math.round((s_time - today) / (3600 * 24f));
				if (delay < 0) {
					delay = Math.round((e_time - today) / (3600 * 24f));
					status = 1;
					if (delay < 0) {
						delay = 0;
						status = 2;
					}
				}
				JSONObject ob = new JSONObject();
				if (cursor.getString(2).equals("")) {
					ob.put("logo", "images/racepic_150.png");
				} else {
					ob.put("logo", cursor.getString(2));
				}
				ob.put("id", cursor.getInt(1));
				ob.put("uid", cursor.getInt(3));
				ob.put("username", cursor.getString(4));
				ob.put("title", cursor.getString(5));
				ob.put("membernum", cursor.getInt(6));
				ob.put("start_time", startTime);
				ob.put("endt_ime", endTime);
				ob.put("delay", delay);
				String flag = "";
				if (status == 0) {
					flag = context.getResources().getString(R.string.gpsstart);
				} else {
					flag = context.getResources().getString(R.string.over);
				}
				ob.put("flag", flag);
				ob.put("status", status);
				ob.put("ispublic", cursor.getInt(10));
				ob.put("target", cursor.getInt(11));
				ob.put("ismember", cursor.getInt(12));
				arr.put(ob);
			}
			cursor.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return arr;

	}

	public void quitMacth(String matchid, int matchfrom) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();

		if (matchfrom == 1) {// 我的竞赛
			database.rawQuery("update " + MyMatch.TABLE_NAME
					+ " set ismember=0 where matchid=" + matchid, null);
		} else if (matchfrom == 2) {// 全部竞赛
			database.rawQuery("update " + AllMatch.TABLE_NAME
					+ " set ismember=0 where matchid=" + matchid, null);
		} else {// 搜索竞赛
			database.rawQuery("update " + SearchMatch.TABLE_NAME
					+ " set ismember=0 where matchid=" + matchid, null);
		}
	}

	public JSONObject getMatchDetail(int matchid, int matchfrom) {
		JSONObject object = new JSONObject();
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor;
		try {
			if (matchfrom == 1) {// 我的竞赛
				cursor = database.rawQuery("select * from "
						+ MyMatch.TABLE_NAME + " where matchid=" + matchid
						+ " limit 1", null);
			} else if (matchfrom == 2) {// 全部竞赛
				cursor = database.rawQuery("select * from "
						+ AllMatch.TABLE_NAME + " where matchid=" + matchid
						+ " limit 1", null);
			} else {// 搜索竞赛
				cursor = database.rawQuery("select * from "
						+ SearchMatch.TABLE_NAME + " where matchid=" + matchid
						+ " limit 1", null);
			}
			Long today = (sdf.parse(sdf.format(new Date())).getTime()) / 1000;

			while (cursor.moveToNext()) {
				object = new JSONObject();
				int status = cursor.getInt(9);
				String startTime = cursor.getString(7);
				String endTime = cursor.getString(8);
				Long s_time = Long.valueOf(startTime);
				Long e_time = Long.valueOf(endTime);
				int delay = 0;
				delay = Math.round((s_time - today) / (3600 * 24f));
				if (delay < 0) {
					delay = Math.round((e_time - today) / (3600 * 24f));
					status = 1;
					if (delay < 0) {
						delay = 0;
						status = 2;
					}
				}
				if (cursor.getString(2).equals("")) {
					object.put("logo", "images/racepic_150.png");
				} else {
					object.put("logo", cursor.getString(2));
				}
				object.put("id", cursor.getString(1));
				object.put("uid", cursor.getInt(3));
				object.put("username", cursor.getString(4));
				object.put("title", cursor.getString(5));
				object.put("membernum", cursor.getInt(6));
				object.put("start_time", startTime);
				object.put("end_time", endTime);
				object.put("delay", delay);
				String flag = "";
				if (status == 0) {
					flag = context.getResources().getString(R.string.gpsstart);
				} else {
					flag = context.getResources().getString(R.string.over);
				}
				object.put("flag", flag);
				object.put("status", status);
				object.put("ispublic", cursor.getInt(10));
				String target = "日均步数";
				switch (cursor.getInt(11)) {
				case 0:
					target = "日均步数";
					break;
				case 1:
					target = "总步数";
					break;
				}
				object.put("target", target);
				if (matchfrom == 0) {
					object.put("ismember", 1);
				} else {
					object.put("ismember", cursor.getInt(12));
				}
			}
			cursor.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return object;

	}

	public void SaveMatchRules(JSONArray array, int matchid) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		try {
			database.execSQL("delete from " + MatchRules.TABLE_NAME
					+ " where matchid=?", new String[] { matchid + "" });
			for (int i = 0; i < array.length(); i++) {
				JSONObject jsobj = array.getJSONObject(i);
				ContentValues values = new ContentValues();
				values.put(MatchRules.MATCH_ID, matchid);
				values.put(MatchRules.MATCH_PICS, jsobj.getString("rule_pic"));
				values.put(MatchRules.MATCH_CONTENTS,
						jsobj.getString("rule_content"));
				database.insert(MatchRules.TABLE_NAME, null, values);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public JSONObject getMatchRules(int matchid) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		JSONObject object = new JSONObject();
		try {
			Cursor cursor = database.rawQuery("select * from "
					+ MatchRules.TABLE_NAME + " where matchid=? limit 1",
					new String[] { matchid + "" });
			while (cursor.moveToNext()) {
				String pics = cursor.getString(2);
				String content = cursor.getString(3);
				String[] piclist;

				if (!pics.equals("")) {
					piclist = pics.split(";");
					JSONArray arr = new JSONArray();
					for (int i = 0; i < piclist.length; i++) {
						arr.put(piclist[i]);
					}
					object.put("rule_pic", arr);

				} else {
					object.put("rule_pic", new JSONArray());
				}

				object.put("rule_content", content);

			}
			cursor.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return object;
	}

	public void SaveMatchMembers(JSONArray array, int matchid) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();

		try {
			database.execSQL("delete from " + MatchMembers.TABLE_NAME
					+ " where matchid=?", new String[] { matchid + "" });
			database.beginTransaction(); // 手动设置开始事务
			for (int i = 0; i < array.length(); i++) {
				JSONObject jsobj = array.getJSONObject(i);
				ContentValues values = new ContentValues();
				values.put("matchid", matchid);
				values.put(MatchMembers.USERNAME, jsobj.getString("username"));
				values.put(MatchMembers.UID,
						Integer.parseInt(jsobj.getString("uid")));
				values.put(MatchMembers.AVATAR, HttpUtlis.AVATAR_URL
						+ getAvatarUrl(jsobj.getString("uid")));
				database.insert(MatchMembers.TABLE_NAME, null, values);
			}
			database.setTransactionSuccessful(); // 设置事务处理成功，不设置会自动回滚不提交
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			database.endTransaction(); // 处理完成
		}
	}

	public JSONArray getMatchMembers(int matchid) {

		SQLiteDatabase database = dbHelper.getWritableDatabase();
		JSONArray a = new JSONArray();
		try {
			Cursor cursor = database.rawQuery("select * from "
					+ MatchMembers.TABLE_NAME + " where matchid=? ",
					new String[] { matchid + "" });
			while (cursor.moveToNext()) {
				JSONObject object = new JSONObject();
				object.put("uid", cursor.getInt(2) + "");
				object.put("username", cursor.getString(3) + "");
				object.put("avatar", cursor.getString(4) + "");
				a.put(object);
			}
			cursor.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return a;
	}

	public void SaveMatchRank(JSONArray array, int matchid) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();

		try {
			database.execSQL("delete from " + MatchRank.TABLE_NAME
					+ " where matchid=?", new String[] { matchid + "" });
			database.beginTransaction(); // 手动设置开始事务
			for (int i = 0; i < array.length(); i++) {
				JSONObject jsobj = array.getJSONObject(i);
				ContentValues values = new ContentValues();
				values.put("matchid", matchid);
				values.put("username", jsobj.getString("name"));
				values.put("uid", Integer.parseInt(jsobj.getString("uid")));
				values.put("time", jsobj.getString("time"));
				values.put("step", Integer.parseInt(jsobj.getString("step")));
				values.put(
						"avatar",
						HttpUtlis.AVATAR_URL
								+ getAvatarUrl(jsobj.getString("uid")));
				database.insert(MatchRank.TABLE_NAME, null, values);
			}
			database.setTransactionSuccessful(); // 设置事务处理成功，不设置会自动回滚不提交
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			database.endTransaction(); // 处理完成
		}
	}

	public JSONArray getMatchRank(int matchid) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		JSONArray a = new JSONArray();
		try {
			Cursor cursor = database.rawQuery("select * from "
					+ MatchRank.TABLE_NAME + " where matchid=?" + " ORDER BY "
					+ MatchRank.STEP + " desc", new String[] { matchid + "" });
			int max = 0;
			if (cursor != null && cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					if (cursor.getPosition() == 0) {
						max = cursor.getInt(5);
					}

					JSONObject map = new JSONObject();
					if (max == 0) {
						map.put("per", 0);
					} else {
						map.put("per",
								Math.round(((cursor.getInt(5)) / ((float) max)) * 100));
					}
					map.put("uid", cursor.getInt(3) + "");
					map.put("username", cursor.getString(4) + "");
					map.put("step", cursor.getInt(5) + "");
					map.put("photo", cursor.getString(2) + "");
					map.put("Master", uid);
					a.put(map);
				}

			}
			cursor.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return a;

	}

	public JSONArray searchMyMatch(String search) {

		SQLiteDatabase database = dbHelper.getWritableDatabase();
		JSONArray arr = new JSONArray();
		try {
			Cursor cursor = null;
			cursor = database.rawQuery("select * from " + MyMatch.TABLE_NAME
					+ " where " + MyMatch.MATCH_TITLE + " like '%" + search
					+ "%' ", null);

			Long today = (sdf.parse(sdf.format(new Date())).getTime()) / 1000;

			while (cursor.moveToNext()) {
				JSONObject object = new JSONObject();
				int status = cursor.getInt(9);
				String startTime = cursor.getString(7);
				String endTime = cursor.getString(8);
				Long s_time = Long.valueOf(startTime);
				Long e_time = Long.valueOf(endTime);
				int delay = 0;
				delay = Math.round((s_time - today) / (3600 * 24f));
				if (delay < 0) {
					delay = Math.round((e_time - today) / (3600 * 24f));
					status = 1;
					if (delay < 0) {
						delay = 0;
						status = 2;
					}
				}
				if (cursor.getString(2).equals("")) {
					object.put("logo", "images/racepic_150.png");
				} else {
					object.put("logo", cursor.getString(2));
				}
				String flag = "";
				if (status == 0) {
					flag = context.getResources().getString(R.string.gpsstart);
				} else {
					flag = context.getResources().getString(R.string.over);
				}
				object.put("flag", flag);
				object.put("id", cursor.getString(1));
				object.put("uid", cursor.getInt(3));
				object.put("username", cursor.getString(4));
				object.put("title", cursor.getString(5));
				object.put("membernum", cursor.getInt(6));
				object.put("start_time", startTime);
				object.put("end_time", endTime);
				object.put("delay", delay);
				object.put("status", status);
				object.put("ispublic", cursor.getInt(10));
				String target = "日均步数";
				switch (cursor.getInt(11)) {
				case 0:
					target = "日均步数";
					break;
				case 1:
					target = "总步数";
					break;
				}
				object.put("target", target);
				object.put("ismember", 1);
				arr.put(object);
			}
			cursor.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return arr;
	}

	public void addMyMatch(int matchid, int matchfrom) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		try {
			Cursor cursor = null;
			if (matchfrom == 2) {// 全部的竞赛
				cursor = database.rawQuery("select * from "
						+ AllMatch.TABLE_NAME + " where matchid=" + matchid
						+ " limit 1", null);
			} else if (matchfrom == 3) {// search 的竞赛
				cursor = database.rawQuery("select * from "
						+ SearchMatch.TABLE_NAME + " where matchid=" + matchid
						+ " limit 1", null);
			}

			while (cursor.moveToNext()) {
				ContentValues values = new ContentValues();
				values.put(MyMatch.MATCH_ID, matchid);
				values.put(MyMatch.LOGO, cursor.getString(2));
				values.put(MyMatch.UID, cursor.getInt(3));
				values.put(MyMatch.USERNAME, cursor.getString(4));
				values.put(MyMatch.MATCH_TITLE, cursor.getString(5));
				values.put(MyMatch.MEMBER_NUM, cursor.getInt(6) + 1);
				values.put(MyMatch.STARTTIME, cursor.getString(7));
				values.put(MyMatch.ENDTIME, cursor.getString(8));
				values.put(MyMatch.STATUS, cursor.getInt(9));
				values.put(MyMatch.ISPUBLIC, cursor.getInt(10));
				values.put(MyMatch.TARGET, cursor.getInt(11));
				values.put(MyMatch.ISMEMBER, 1);
				int rows = database.update(MyMatch.TABLE_NAME, values,
						" uid=? and matchid=? ", new String[] { uid + "",
								matchid + "" });
				if (rows < 1) {
					database.insert(MyMatch.TABLE_NAME, null, values);
				}

				ContentValues v = new ContentValues();
				v.put(AllMatch.ISMEMBER, 1);
				v.put(AllMatch.MEMBER_NUM, cursor.getInt(6) + 1);
				// int r = database.update(AllMatch.TABLE_NAME, v,
				// " matchid=? ",
				// new String[] { matchid + "" });

			}
			cursor.close();

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	public void removeMyMatch(int matchid) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		try {
			Cursor cursor = database.rawQuery("select * from "
					+ AllMatch.TABLE_NAME + " where matchid=" + matchid
					+ " limit 1", null);
			if (cursor.moveToNext()) {
				database.execSQL("delete from " + MyMatch.TABLE_NAME
						+ " where matchid=?", new String[] { matchid + "" });
				if (cursor.getInt(3) == uid) {
					database.execSQL("delete from " + AllMatch.TABLE_NAME
							+ " where matchid=?", new String[] { matchid + "" });
				} else {
					ContentValues v = new ContentValues();
					v.put(AllMatch.ISMEMBER, 0);
					v.put(AllMatch.MEMBER_NUM, cursor.getInt(6) - 1);
					// int r = database.update(AllMatch.TABLE_NAME, v,
					// " matchid=? ", new String[] { matchid + "" });

				}

			} else {
				database.execSQL("delete from " + MyMatch.TABLE_NAME
						+ " where matchid=?", new String[] { matchid + "" });
			}
			cursor.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	public String getAvatarUrl(String uid) {
		String size = "middle";
		int u = Math.abs(Integer.parseInt(uid));
		uid = String.format("%09d", u);
		String dir1 = uid.substring(0, 3);
		String dir2 = uid.substring(3, 5);
		String dir3 = uid.substring(5, 7);
		String typeadd = "";
		String url = dir1 + "/" + dir2 + "/" + dir3 + "/" + uid.substring(7)
				+ typeadd + "_avatar_" + size + ".jpg";
		return url;
	}

	// 强身健体数据
	public void saveCooperResult(String time, String duration, int step,
			int cal, int distance, int score) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		try {

			ContentValues values = new ContentValues();
			values.put(CooperTest.TIME, time);
			values.put(CooperTest.DURATION, duration);
			values.put(CooperTest.DISTANCE, distance);
			values.put(CooperTest.STEP, step);
			values.put(CooperTest.CAL, cal);
			values.put(CooperTest.SCORE, score);
			values.put(CooperTest.UID, uid);
			int rows = database.update(CooperTest.TABLE_NAME, values,
					" uid=? and time=? ", new String[] { uid + "", time });
			if (rows < 1) {
				database.insert(CooperTest.TABLE_NAME, null, values);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public HashMap<String, String> getCooperResult(String time) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		Cursor cursor;
		if (time != null) {
			cursor = database.rawQuery(" select * from "
					+ CooperTest.TABLE_NAME + " where uid=? and time=? ",
					new String[] { uid + "", time });
		} else {
			cursor = database.rawQuery(" select * from "
					+ CooperTest.TABLE_NAME + " where uid=? ",
					new String[] { uid + "" });
		}
		if (cursor == null || cursor.getCount() < 1) {
			return null;
		}
		cursor.moveToFirst();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("step",
				cursor.getString(cursor.getColumnIndex(CooperTest.STEP)));
		map.put("distance",
				cursor.getString(cursor.getColumnIndex(CooperTest.DISTANCE)));
		map.put("duration",
				cursor.getString(cursor.getColumnIndex(CooperTest.DURATION)));
		map.put("time",
				cursor.getString(cursor.getColumnIndex(CooperTest.TIME)));
		map.put("cal", cursor.getString(cursor.getColumnIndex(CooperTest.CAL)));
		map.put("score",
				cursor.getString(cursor.getColumnIndex(CooperTest.SCORE)));
		cursor.close();
		return map;
	}

	public ArrayList<UserHashMap<String, String>> getGroupRank(String clubid,
			String gid, int page) {
		ArrayList<UserHashMap<String, String>> alist = new ArrayList<UserHashMap<String, String>>();
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.query(ClubRank.TABLE_NAME, new String[] {
				ClubRank.CLUBID, ClubRank.USERNAME, ClubRank.GID,
				ClubRank.STEP, ClubRank.TIME, ClubRank.RANKUID, ClubRank.PHOTO,
				ClubRank.UID }, " clubid=? and " + ClubRank.GID
				+ "=? ORDER BY " + ClubRank.STEP + " DESC limit " + (page * 10)
				+ ",10;", new String[] { clubid, gid }, null, null, null);
		while (cursor.moveToNext()) {
			UserHashMap<String, String> map = new UserHashMap<String, String>();
			map.put(ClubRank.CLUBID,
					cursor.getString(cursor.getColumnIndex(ClubRank.CLUBID)));
			map.put(ClubRank.USERNAME,
					cursor.getString(cursor.getColumnIndex(ClubRank.USERNAME)));
			map.put(ClubRank.GID,
					cursor.getString(cursor.getColumnIndex(ClubRank.GID)));
			map.put(ClubRank.STEP,
					cursor.getString(cursor.getColumnIndex(ClubRank.STEP)));
			map.put(ClubRank.TIME,
					cursor.getString(cursor.getColumnIndex(ClubRank.TIME)));
			map.put(ClubRank.RANKUID,
					cursor.getString(cursor.getColumnIndex(ClubRank.RANKUID)));
			map.put(ClubRank.PHOTO,
					cursor.getString(cursor.getColumnIndex(ClubRank.PHOTO)));
			map.put(ClubRank.UID,
					cursor.getString(cursor.getColumnIndex(ClubRank.UID)));
			if (!alist.contains(map)) {
				alist.add(map);
			}
		}
		cursor.close();
		return alist;
	}

	public void saveSoftStep(String time, int step, int cal) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		try {
			database.execSQL("delete from " + SOFTSTEP.TABLE_NAME);
			ContentValues values = new ContentValues();
			values.put(SOFTSTEP.SAVETIME, time);
			values.put(SOFTSTEP.STEP_TODAY, step);
			values.put(SOFTSTEP.CAL_TODAY, cal);
			database.insert(SOFTSTEP.TABLE_NAME, null, values);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	public void setLastPoint(double latitude, double longitude) {

		SQLiteDatabase database = dbHelper.getWritableDatabase();
		try {
			database.execSQL("delete from " + GPSLASTPOINT.TABLE_NAME);
			ContentValues values = new ContentValues();
			values.put(GPSLASTPOINT.LATITUDE, latitude);
			values.put(GPSLASTPOINT.LONGITUDE, longitude);
			database.insert(GPSLASTPOINT.TABLE_NAME, null, values);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	public LatLng getLastPoint() {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		LatLng gg = null;
		try {
			Cursor cursor = database.rawQuery("select " + GPSLASTPOINT.LATITUDE
					+ " ," + GPSLASTPOINT.LONGITUDE + " from "
					+ GPSLASTPOINT.TABLE_NAME, null);
			while (cursor.moveToNext()) {
				gg = new LatLng(cursor.getDouble(0), cursor.getDouble(1));
				break;
			}
			cursor.close();
		} catch (Exception e) {
		}
		return gg;
	}

	/***
	 * 检查睡眠数据
	 * 
	 * @param time
	 *            时间
	 * @param tablename
	 *            SleepData.TABLE_NAME | SleepDataDetail.TABLE_NAME
	 * @return
	 */
	public boolean CheckSleepData(String time, String hour, String tablename) {
		boolean isExist = false;
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		try {
			Cursor cursor;
			if (tablename.equals(SleepDataDetail.TABLE_NAME)) {
				cursor = database.rawQuery("select * from " + tablename
						+ " where " + SleepDataDetail.TIME + " =? and "
						+ SleepDataDetail.HOUR + " =? ", new String[] { time,
						hour });
			} else {
				cursor = database.rawQuery("select * from " + tablename
						+ " where " + SleepData.DATE + " =? ",
						new String[] { time });
			}

			if (cursor.moveToNext()) {
				isExist = true;
			}
			cursor.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isExist;
	}

	public void InsertSleepData(String time, int score, int total, int deep,
			int light, int lucid) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		try {
			ContentValues values = new ContentValues();
			values.put(SleepData.UID, uid + "");
			values.put(SleepData.DATE, time);
			values.put(SleepData.SCORE, score);
			values.put(SleepData.TOTAL, total);
			values.put(SleepData.DEEP, deep);
			values.put(SleepData.LIGHT, light);
			values.put(SleepData.LUCID, lucid);
			values.put(SleepData.ISUPLOAD, 1);

			int t = database.update(SleepData.TABLE_NAME, values,
					SleepData.DATE + "=? ", new String[] { time });
			if (t < 1) {
				database.insert(SleepData.TABLE_NAME, null, values);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public HashMap<String, Integer> getSleepScore(String s) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		Cursor cur = database.rawQuery("select * from " + SleepData.TABLE_NAME
				+ " where " + SleepData.DATE + " =? ", new String[] { s });
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		if (cur.moveToFirst()) {
			map.put("score", cur.getInt(cur.getColumnIndex(SleepData.SCORE)));
			map.put("deep", cur.getInt(cur.getColumnIndex(SleepData.DEEP)));
			map.put("light", cur.getInt(cur.getColumnIndex(SleepData.LIGHT)));
			map.put("aweak", cur.getInt(cur.getColumnIndex(SleepData.LUCID)));
			return map;
		}
		return null;
	}

	public byte[] getSleepDetail(String date) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		Cursor cursor = database.rawQuery("select " + SleepAir0226.DATA
				+ " from " + SleepAir0226.TABLE_NAME + " where "
				+ SleepAir0226.DATE + " =? ", new String[] { date });
		byte[] bt = null;
		if (cursor.moveToFirst()) {
			bt = cursor.getBlob(cursor.getColumnIndex(SleepAir0226.DATA));
		}
		return bt;
	}

	public void delSleepDetail(String date) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		int row = database.delete(SleepAir0226.TABLE_NAME, SleepAir0226.DATE
				+ " =? ", new String[] { date });
		System.out.println(row);
	}

	public String getSleepDetailScore(String date) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		Cursor cursor = database.rawQuery("select " + SleepAir0226.BOUNDS
				+ " from " + SleepAir0226.TABLE_NAME + " where "
				+ SleepAir0226.DATE + " =? ", new String[] { date });
		String bt = null;
		if (cursor.moveToFirst()) {
			bt = cursor.getString(cursor.getColumnIndex(SleepAir0226.BOUNDS));
		}
		return bt;
	}

	public void saveSleepDetail(String date, String begin, String end,
			String active, String light, String deep, String bounds,
			String count, byte[] data) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(SleepAir0226.UID, uid);
		values.put(SleepAir0226.DATA, data);
		values.put(SleepAir0226.DATE, date);
		values.put(SleepAir0226.DEEP, deep);
		values.put(SleepAir0226.ACTIVE, active);
		values.put(SleepAir0226.BEGIN, begin);
		values.put(SleepAir0226.BOUNDS, bounds);
		values.put(SleepAir0226.COUNT, count);
		values.put(SleepAir0226.END, end);
		values.put(SleepAir0226.LIGHT, light);
		int rows = database.update(SleepAir0226.TABLE_NAME, values, " uid =? "
				+ " and " + SleepAir0226.DATE + " =? ", new String[] {
				uid + "", date });
		if (rows < 1) {
			database.insert(SleepAir0226.TABLE_NAME, null, values);
		}
	}

	public void saveSleepMsg(String date, String msg) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(SleepAir0226.MSG, msg);

		int row = database.update(SleepAir0226.TABLE_NAME, values, " "
				+ SleepAir0226.UID + "=? and " + SleepAir0226.DATE + "=? ",
				new String[] { uid + "", date });
		// Cursor cursor = database.rawQuery("select * from "
		// + SleepAir0226.TABLE_NAME, null);
		// while (cursor.moveToNext()) {
		// System.out.println(cursor.getString(cursor
		// .getColumnIndex(SleepAir0226.DATE))
		// + "-"
		// + cursor.getString(cursor.getColumnIndex(SleepAir0226.MSG))
		// + "="
		// + cursor.getString(cursor
		// .getColumnIndex(SleepAir0226.BOUNDS)));
		// }
		// System.out.println(cursor);
		// cursor.close();

	}

	public String getSleepMsg(String date) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.rawQuery("select " + SleepAir0226.MSG
				+ " from " + SleepAir0226.TABLE_NAME + " where "
				+ SleepAir0226.DATE + "=? and uid=? ", new String[] { date,
				uid + "" });
		if (cursor.moveToFirst()) {
			String msg = cursor.getString(0);
			cursor.close();
			return msg;
		}
		cursor.close();
		return null;
	}

	public static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	public void saveSleepDetail(String date, String begin, String end,
			String active, String light, String deep, String bounds,
			String count, String[] data) {
		byte[] x = new byte[data.length];
		for (int i = 0; i < x.length; i++) {
			char[] hexChars = data[i].toCharArray();
			if (hexChars.length == 1) {
				char temp = hexChars[0];
				hexChars = new char[2];

				hexChars[0] = '0';
				hexChars[1] = temp;
			}
			x[i] = (byte) (charToByte(hexChars[0]) << 4 | charToByte(hexChars[1]));
		}
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(SleepAir0226.UID, uid);
		values.put(SleepAir0226.DATA, x);
		values.put(SleepAir0226.DATE, date);
		values.put(SleepAir0226.DEEP, deep);
		values.put(SleepAir0226.ACTIVE, active);
		values.put(SleepAir0226.BEGIN, begin);
		values.put(SleepAir0226.BOUNDS, bounds);
		values.put(SleepAir0226.COUNT, count);
		values.put(SleepAir0226.END, end);
		values.put(SleepAir0226.LIGHT, light);
		int rows = database.update(SleepAir0226.TABLE_NAME, values, " uid =? "
				+ " and " + SleepAir0226.DATE + " =? ", new String[] {
				uid + "", date });
		if (rows < 1) {
			database.insert(SleepAir0226.TABLE_NAME, null, values);
		}
	}

	@Deprecated
	public void saveSleepDetail(String date, String hour, byte[] data) {
		// 00：静止 01：睡眠 10：运动 11：保留
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(SleepDataDetail.UID, uid);
		values.put(SleepDataDetail.TIME, date);
		values.put(SleepDataDetail.HOUR, hour);
		values.put(SleepDataDetail.DATA, data);
		int rows = database
				.update(SleepDataDetail.TABLE_NAME, values, " uid =? "
						+ " and " + SleepDataDetail.TIME + " =? and hour=? ",
						new String[] { uid + "", date, hour });
		if (rows < 1) {
			database.insert(SleepDataDetail.TABLE_NAME, null, values);
		}
	}

	// 2015-01-04
	/***
	 * 根据详细数据的起睡时间得出0804的起睡索引
	 * 
	 * @param x
	 *            在0802中的索引
	 * @return 在0804中的索引
	 */
	public int calcTimeF(int x) {
		int min = (x - 144) * 5;
		int bytes = min % 4 != 0 ? (min / 4) + 1 : min / 4;
		return Math.abs(180 + bytes);
	}

	public int calcTimeT(int x) {
		int min = (x - 432) * 5;
		int bytes = min % 4 != 0 ? (min / 4) + 1 : min / 4;
		return Math.abs(360 + bytes);
	}

	public int calcTime(int x) {

		return (int) (((float) (x / 576f)) * 360);
	}

	@Deprecated
	public byte[] getSleep0211(String date) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		Cursor cursor = database.rawQuery(
				"select * from " + SleepDataDetail.TABLE_NAME + " where "
						+ SleepDataDetail.HOUR + " between ? and ? order by "
						+ SleepDataDetail.HOUR + " asc",
				new String[] {
						date.replace("-", "") + 12,
						Util.getBeforeAfterDate(date, 1).toString()
								.replace("-", "") + 12 });
		try {
			int index = cursor.getColumnIndex(SleepDataDetail.DATA);
			byte[] temp = new byte[360];
			for (int i = 0; i < 360; i++) {
				temp[i] = (byte) -1;
			}
			int z = 0;
			while (cursor.moveToNext()) {

				String hour = cursor.getString(cursor
						.getColumnIndex(SleepDataDetail.HOUR));
				System.out.println("HOUR:" + hour + "--"
						+ Converter.byteArrayToHexString(cursor.getBlob(index))
						+ "getCount：" + cursor.getCount());
				if (z == 0) {
					z = Integer.parseInt(hour.substring(hour.length() - 2,
							hour.length())) - 12;
				}
				// 如果该小时有数据有
				if (z < 24) {
					System.arraycopy(cursor.getBlob(index), 0, temp, z * 15,
							cursor.getBlob(index).length);
					z++;
				}
			}
			return temp;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	@Deprecated
	public byte[] getSleepDetail(String date, String from, String to,
			boolean sameDay) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		Cursor cursor;
		if (sameDay) {
			cursor = database.rawQuery("select * from "
					+ SleepDataDetail.TABLE_NAME + " where "
					+ SleepDataDetail.TIME + " between ? and ? order by "
					+ SleepDataDetail.HOUR + " asc", new String[] {
					Util.getBeforeAfterDate(date, -1).toString(), date });
		} else {
			cursor = database.rawQuery("select * from "
					+ SleepDataDetail.TABLE_NAME + " where "
					+ SleepDataDetail.HOUR + " between ? and ? order by "
					+ SleepDataDetail.HOUR + " asc",
					new String[] {
							date.replace("-", "") + 12,
							Util.getBeforeAfterDate(date, 1).toString()
									.replace("-", "") + 12 });
		}
		// 00：静止 01：睡眠 10：运动 11：保留
		try {
			int index = cursor.getColumnIndex(SleepDataDetail.DATA);
			byte[] temp = new byte[360];
			for (int i = 0; i < 360; i++) {
				temp[i] = (byte) -1;
			}
			boolean hasData = false;
			int z = 0;
			while (cursor.moveToNext()) {
				hasData = true;
				String hour = cursor.getString(cursor
						.getColumnIndex(SleepDataDetail.HOUR));
				System.out.println("HOUR:" + hour + "--"
						+ Converter.byteArrayToHexString(cursor.getBlob(index))
						+ "getCount：" + cursor.getCount());
				if (z == 0) {
					z = Integer.parseInt(hour.substring(hour.length() - 2,
							hour.length())) - 12;
				}
				// 如果该小时有数据有
				if (sameDay) {

					System.arraycopy(cursor.getBlob(index), 0, temp, Integer
							.parseInt(hour.substring(hour.length() - 2,
									hour.length())) * 15,
							cursor.getBlob(index).length);
				} else if (z < 24) {

					System.arraycopy(cursor.getBlob(index), 0, temp, z * 15,
							cursor.getBlob(index).length);
					z++;
				}
			}
			if (hasData) {
				for (int i = 0; i < temp.length; i++) {
					if (temp[i] != 85 && temp[i] != 0 && temp[i] != -86
							&& temp[i] != -1) {
						temp[i] = transform2(temp[i]);
					}
				}
				return temp;
			} else {
				return null;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	@Deprecated
	public byte[] getSleepDetail2(String date, int rfrom, int rto) {
		int from_min = (rfrom * 5);
		int to_min = (rto - 288) * 5;
		int formindex = (from_min / 4) - 180;
		int toindex = 180 + (to_min / 4);
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		Cursor cursor;
		{
			cursor = database.rawQuery("select * from "
					+ SleepDataDetail.TABLE_NAME + " where "
					+ SleepDataDetail.HOUR + " between ? and ? order by "
					+ SleepDataDetail.HOUR + " asc",
					new String[] {
							date.replace("-", "") + 12,
							Util.getBeforeAfterDate(date, 1).toString()
									.replace("-", "") + 12 });
		}
		// 00：静止 01：睡眠 10：运动 11：保留
		try {
			int index = cursor.getColumnIndex(SleepDataDetail.DATA);
			byte[] temp = new byte[360];
			for (int i = 0; i < 360; i++) {
				temp[i] = (byte) -1;
			}
			boolean hasData = false;
			int z = 0;
			while (cursor.moveToNext()) {
				hasData = true;
				String hour = cursor.getString(cursor
						.getColumnIndex(SleepDataDetail.HOUR));
				System.out.println("HOUR:" + hour + "--"
						+ Converter.byteArrayToHexString(cursor.getBlob(index))
						+ "getCount：" + cursor.getCount());
				if (z == 0) {
					z = Integer.parseInt(hour.substring(hour.length() - 2,
							hour.length())) - 12;
				}
				// 如果该小时有数据有
				if (z < 24) {

					System.arraycopy(cursor.getBlob(index), 0, temp, z * 15,
							cursor.getBlob(index).length);
					z++;
				}
			}
			if (hasData) {
				for (int i = 0; i < temp.length; i++) {
					if (temp[i] != 85 && temp[i] != 0 && temp[i] != -86
							&& temp[i] != -1) {
						temp[i] = transform2(temp[i]);
					}
				}
				byte[] x = new byte[toindex - formindex];
				System.arraycopy(temp, formindex, x, 0, toindex - formindex);
				String s = Converter.byteArrayToHexString(x);
				return x;
			} else {
				return null;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	private byte transform2(byte x) {

		String temp = Integer.toBinaryString(x);
		int len = temp.length();
		if (len < 8 || x < 0) {
			temp = "00000000" + temp;
			temp = temp.substring(len, len + 8);
		}
		int c_active = 0;
		int c_sleep = 0;
		int c_silence = 0;
		for (int j = 0; j < 4; j++) {
			String s = temp.substring(j * 2, j * 2 + 2);
			if (s.equals("00")) {
				c_silence++;
			} else if (s.equals("01")) {
				c_sleep++;
			} else if (s.equals("10")) {
				c_active++;
			}
		}
		if (c_active >= 3) {
			return (byte) -86;
		} else if (c_silence >= 3) {
			return (byte) 0;
		} else if (c_sleep >= 3) {
			return (byte) 85;
		} else {
			if (c_active <= 2) {
				return (byte) 85;
			} else {
				return x;
			}
		}

	}

	public List<byte[]> getRawData(String btime, String etime) {
		List<byte[]> listdata = new ArrayList<byte[]>();
		byte[] temp;

		SQLiteDatabase database = dbHelper.getReadableDatabase();

		String sql = String
				.format("select %s,%s,%s from %s where %s=%s and (%s = '%s' or %s = '%s')",
						PdSpRaw._ID, PdSpRaw.DATE, PdSpRaw.DATA,
						PdSpRaw.TABLE_NAME, PdSpRaw.UID, uid, PdSpRaw.DATA,
						btime, PdSpRaw.DATE, etime);

		Cursor cursor = database.rawQuery(sql, null);

		while (cursor.moveToNext()) {
			temp = cursor.getBlob(2);
			listdata.add(temp);
		}

		cursor.close();
		return listdata;
	}

	public Object[] getSoftStep() {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		Object[] objects = new Object[] {
				new SimpleDateFormat("yyyy-MM-dd").format(Calendar
						.getInstance().getTime()), 0, 0 };
		try {
			Cursor cursor = database.rawQuery(" select " + SOFTSTEP.SAVETIME
					+ " ," + SOFTSTEP.STEP_TODAY + ", " + SOFTSTEP.CAL_TODAY
					+ " from " + SOFTSTEP.TABLE_NAME + " limit 1", null);
			while (cursor.moveToNext()) {
				objects[0] = cursor.getString(0);
				objects[1] = cursor.getInt(1);
				objects[2] = cursor.getInt(2);
			}
			cursor.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return objects;

	}

	public ArrayList<Integer> getUpLoadGPSIds() {

		SQLiteDatabase database = dbHelper.getWritableDatabase();
		ArrayList<Integer> ids = new ArrayList<Integer>();
		try {
			Cursor cursor = database.rawQuery(" select " + GPSUPLOAD.IID
					+ " from " + GPSUPLOAD.TABLE_NAME + " where "
					+ GPSUPLOAD.ISUPLOAD + "=0 and uid=" + uid, null);
			while (cursor.moveToNext()) {
				ids.add(cursor.getInt(0));
			}
			cursor.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return ids;

	}

	public Cursor getGPSTrackCursor() {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		Cursor cursor = null;
		try {
			// String sql = "select * from " + GPSTrack.TABLE_NAME + " where "
			// + GPSTrack.POINTS + " != 0 and " + GPSTrack.UID + " = '"
			// + uid + "'" + " order by " + GPSTrack.DATE + " desc";

			String sql = "select * from " + GPSTrack.TABLE_NAME + " where "
					+ GPSTrack.UID + " = '" + uid + "'" + " order by "
					+ GPSTrack.DATE + " desc";
			cursor = database.rawQuery(sql, null);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return cursor;
	}

	public boolean delGPSTrack(String date) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		try {

			int i = database.delete(GPSTrack.TABLE_NAME, GPSTrack.UID
					+ "=? and " + GPSTrack.DATE + "=?", new String[] {
					uid + "", date });
			if (i == 0) {
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
	}

	public void insertGPSUpLoad(int iid, int isupload) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		String sql = "";
		try {
			sql = String.format("insert into  " + GPSUPLOAD.TABLE_NAME
					+ " (iid,isupload,uid) values (%1$s,%2$s,'%3$s')", iid,
					isupload, uid);
			database.execSQL(sql);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	public void UpdateGPSUpLoad(int iid, int isupload) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		String sql = "";
		try {
			sql = "update " + GPSUPLOAD.TABLE_NAME + " set isupload="
					+ isupload + " where iid=" + iid + " and uid='" + uid + "'";
			database.execSQL(sql);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	// 添加或更新下载记录
	public void downloaded(String date_now, String date_aim) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(DOWNLOADLOG.UID, uid);
		values.put(DOWNLOADLOG.AIMDATE, date_aim);
		values.put(DOWNLOADLOG.NOWDATE, date_now);
		int rows = database.update(DOWNLOADLOG.TABLE_NAME, values, " uid =? "
				+ " and " + DOWNLOADLOG.AIMDATE + " =? ", new String[] {
				uid + "", date_aim });
		if (rows <= 0) {
			long rows2 = database.insert(DOWNLOADLOG.TABLE_NAME, null, values);
			System.out.println(rows2);
		}

	}

	// 获取是否已下载过
	public boolean hasDownload(String aim_date) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		Cursor cursor = database.rawQuery("select * from "
				+ DOWNLOADLOG.TABLE_NAME + " where aimdate=? ",
				new String[] { aim_date });

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				return true;
			}

		}
		return false;
	}

	// air开屏振动统计
	public void saveAirRecord(String date, String adds, String light,
			String vibrator, String manufa, String mode, String appcode,
			String aircode, String battery) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(AIRRECORDER.UID, uid);
		values.put(AIRRECORDER.DATE, date);
		values.put(AIRRECORDER.ADDRESS, adds);
		values.put(AIRRECORDER.LIGHTTIME, light);
		values.put(AIRRECORDER.VIBRATOR, vibrator);
		values.put(AIRRECORDER.MANUFACTURER, manufa);
		values.put(AIRRECORDER.MODE, mode);
		values.put(AIRRECORDER.APP_VERSION, appcode);
		values.put(AIRRECORDER.AIR_VERSION, aircode);
		values.put(AIRRECORDER.AIR_VERSION, aircode);
		values.put(AIRRECORDER.BATTERY, battery);
		int rows = database.update(AIRRECORDER.TABLE_NAME, values,
				AIRRECORDER.DATE + " =? ", new String[] { date });
		if (rows <= 0) {
			long rows2 = database.insert(AIRRECORDER.TABLE_NAME, null, values);
			System.out.println(rows2);
		}

	}

	// notification 过滤
	public void saveNotification(String appname, String packetname,
			String disable) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(NOTIFICATION_LIST.UID, uid);
		values.put(NOTIFICATION_LIST.APPNAME, appname);
		values.put(NOTIFICATION_LIST.PACKET, packetname);
		values.put(NOTIFICATION_LIST.DISABLE, disable);
		int rows = database.update(NOTIFICATION_LIST.TABLE_NAME, values,
				NOTIFICATION_LIST.PACKET + " =? ", new String[] { packetname });
		if (rows <= 0) {
			long rows2 = database.insert(NOTIFICATION_LIST.TABLE_NAME, null,
					values);
			System.out.println(rows2);
		}

	}

	public ArrayList<String> getEnableList() {
		ArrayList<String> all = new ArrayList<String>();
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.rawQuery("select * from "
				+ NOTIFICATION_LIST.TABLE_NAME + " where "
				+ NOTIFICATION_LIST.DISABLE + "=1 ", null);
		while (cursor.moveToNext()) {
			all.add(cursor.getString(cursor
					.getColumnIndex(NOTIFICATION_LIST.PACKET)));
		}
		return all;
	}

	// 设置某天 丢失次数
	public void setAirRecorderlost(int lost, String time) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(AIRRECORDER.LostTime, lost + "");
		int rows = database.update(AIRRECORDER.TABLE_NAME, values,
				AIRRECORDER.DATE + " =? ", new String[] { time });
		if (rows <= 0) {
			long rows2 = database.insert(AIRRECORDER.TABLE_NAME, null, values);
			System.out.println(rows2);
		}
	}

	// 设置某天 服务重启次数
	public void setAirRecorderCreate(int oncreate, String time) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(AIRRECORDER.ServiceOnCreate, oncreate + "");
		int rows = database.update(AIRRECORDER.TABLE_NAME, values,
				AIRRECORDER.DATE + " =? ", new String[] { time });
		if (rows <= 0) {
			long rows2 = database.insert(AIRRECORDER.TABLE_NAME, null, values);
			System.out.println(rows2);
		}
	}

	/***
	 * 取某天丢失报警次数
	 * 
	 * @param date
	 *            日期
	 * @return 次数
	 */
	public int getAirlostTimes(String date) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		Cursor cursor = database.rawQuery("select " + AIRRECORDER.LostTime
				+ " from " + AIRRECORDER.TABLE_NAME + " where "
				+ AIRRECORDER.DATE + " =? ", new String[] { date });
		try {
			if (cursor.moveToFirst()) {
				String lost = cursor.getString(cursor
						.getColumnIndex(AIRRECORDER.LostTime));
				return Integer.parseInt(lost == null ? "0" : lost);
			} else {
				return 0;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}

	}

	/***
	 * 取某天BLE服务启动次数
	 * 
	 * @param date
	 * @return
	 */
	public int getAirOnCreate(String date) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		Cursor cursor = database.rawQuery("select "
				+ AIRRECORDER.ServiceOnCreate + " from "
				+ AIRRECORDER.TABLE_NAME + " where " + AIRRECORDER.DATE
				+ " =? ", new String[] { date });
		try {
			if (cursor.moveToFirst()) {
				String lost = cursor.getString(cursor
						.getColumnIndex(AIRRECORDER.ServiceOnCreate));
				return Integer.parseInt(lost == null ? "0" : lost);
			} else {
				return 0;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}

	}

	// 读air相关统计
	public HashMap<String, String> getAirRecorder(String time) {
		try {
			SQLiteDatabase database = dbHelper.getWritableDatabase();
			Cursor cursor = database.rawQuery("select * from "
					+ AIRRECORDER.TABLE_NAME + " where " + AIRRECORDER.DATE
					+ " =? ", new String[] { time });
			HashMap<String, String> map = new HashMap<String, String>();
			if (cursor.moveToFirst()) {
				map.put(AIRRECORDER.DATE, cursor.getString(cursor
						.getColumnIndex(AIRRECORDER.DATE)));
				map.put(AIRRECORDER.ADDRESS, cursor.getString(cursor
						.getColumnIndex(AIRRECORDER.ADDRESS)));
				map.put(AIRRECORDER.LIGHTTIME, cursor.getString(cursor
						.getColumnIndex(AIRRECORDER.LIGHTTIME)));
				map.put(AIRRECORDER.VIBRATOR, cursor.getString(cursor
						.getColumnIndex(AIRRECORDER.VIBRATOR)));
				map.put(AIRRECORDER.MANUFACTURER, cursor.getString(cursor
						.getColumnIndex(AIRRECORDER.MANUFACTURER)));
				// 原先厂商和型号是两个字段，现将did放到mode里
				map.put("did", cursor.getString(cursor
						.getColumnIndex(AIRRECORDER.MODE)));
				map.put(AIRRECORDER.APP_VERSION, cursor.getString(cursor
						.getColumnIndex(AIRRECORDER.APP_VERSION)));
				map.put(AIRRECORDER.AIR_VERSION, cursor.getString(cursor
						.getColumnIndex(AIRRECORDER.AIR_VERSION)));
				map.put(AIRRECORDER.ServiceOnCreate,
						cursor.getString(cursor
								.getColumnIndex(AIRRECORDER.ServiceOnCreate))
								+ "");
				map.put(AIRRECORDER.LostTime,
						cursor.getString(cursor
								.getColumnIndex(AIRRECORDER.LostTime)) + "");
				map.put(AIRRECORDER.BATTERY,
						cursor.getString(cursor
								.getColumnIndex(AIRRECORDER.BATTERY)) + "");
				return map;
			}
			return null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/***
	 * 改变上传状态
	 * 
	 * @param ok
	 *            1是已上传 0是未上传
	 * @return 是否更改
	 */
	public boolean AirUpLoadStatus(String time, String ok) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		Cursor cursor = database.rawQuery("select " + AirUpCheck.STATUS
				+ " from " + AirUpCheck.TABLE_NAME + " where "
				+ AirUpCheck.DATE + " =? ", new String[] { time });
		if (cursor.moveToFirst()) {
			if (cursor.getString(cursor.getColumnIndex(AirUpCheck.STATUS))
					.equals("1")) {
				return false;
			}
		}
		ContentValues values = new ContentValues();
		values.put(AirUpCheck.STATUS, ok);
		values.put(AirUpCheck.DATE, time);
		int rows = database.update(AirUpCheck.TABLE_NAME, values,
				AirUpCheck.DATE + " =? ", new String[] { time });
		if (rows <= 0) {
			long rows2 = database.insert(AirUpCheck.TABLE_NAME, null, values);
			System.out.println(rows2);
		}
		cursor.close();
		return true;
	}

	public boolean getAirInfoNeedUpLoad(String time) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.rawQuery("select " + AirUpCheck.STATUS
				+ " from " + AirUpCheck.TABLE_NAME + " where "
				+ AirUpCheck.DATE + " =? ", new String[] { time });
		if (cursor.moveToFirst()) {
			if (cursor.getString(cursor.getColumnIndex(AirUpCheck.STATUS))
					.equals("1")) {
				return false;
			}
		}
		return true;
	}

	/***
	 * 返回所有未上传air的日期
	 * 
	 * @return 返回所有未上传air的日期
	 */
	public ArrayList<String> getAirNotUpload() {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		Cursor cursor = database.rawQuery("select " + AirUpCheck.DATE
				+ " from " + AirUpCheck.TABLE_NAME + " where "
				+ AirUpCheck.STATUS + " =? ", new String[] { "0" });
		ArrayList<String> arr = new ArrayList<String>();
		while (cursor.moveToNext()) {
			arr.add(cursor.getString(cursor.getColumnIndex(AirUpCheck.DATE)));
		}
		return arr;
	}

	/***
	 * 扣分记录
	 * 
	 * @param bounds
	 *            口多少分
	 * @param date
	 *            睡眠所属日期
	 * @param intro
	 *            为什么扣分
	 * @param stime
	 *            存储的时间
	 */
	public void saveSleepBoundsRecode(float bounds, String date, String intro,
			long stime) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(SleepBoundsDetail.UID, uid);
		values.put(SleepBoundsDetail.BOUNDS, bounds);
		values.put(SleepBoundsDetail.DATE, date);
		values.put(SleepBoundsDetail.SAVETime, stime);
		values.put(SleepBoundsDetail.INTRO, intro);
		int row = 0;
		row = database.delete(SleepBoundsDetail.TABLE_NAME,
				SleepBoundsDetail.UID + "=? and " + SleepBoundsDetail.DATE
						+ "=? and " + SleepBoundsDetail.SAVETime + "!=? ",
				new String[] { uid + "", date, stime + "" });

		database.insert(SleepBoundsDetail.TABLE_NAME, null, values);
	}

	public ArrayList<HashMap<String, String>> getBoundsRecord(String date) {
		ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.rawQuery("select * from "
				+ SleepBoundsDetail.TABLE_NAME + " where "
				+ SleepBoundsDetail.UID + " =? and " + SleepBoundsDetail.DATE
				+ " =?", new String[] { uid + "", date });
		while (cursor.moveToNext()) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(SleepBoundsDetail.BOUNDS,
					""
							+ cursor.getFloat(cursor
									.getColumnIndex(SleepBoundsDetail.BOUNDS)));
			map.put(SleepBoundsDetail.INTRO, cursor.getString(cursor
					.getColumnIndex(SleepBoundsDetail.INTRO)));
			map.put(SleepBoundsDetail.DATE, cursor.getString(cursor
					.getColumnIndex(SleepBoundsDetail.DATE)));
			data.add(map);
		}

		return data;
	}

	public void savePinganScore(String bounds, String date, String msg) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(PINGAN_SCORE.UID, uid);
		values.put(PINGAN_SCORE.BOUNDS, bounds);
		values.put(PINGAN_SCORE.DATE, date);
		values.put(PINGAN_SCORE.msg, msg);
		int row = 0;
		Cursor cursor = database.rawQuery("select * from "
				+ PINGAN_SCORE.TABLE_NAME + " where " + PINGAN_SCORE.DATE
				+ "=? and uid=?", new String[] { date, uid + "" });
		if (cursor.moveToFirst()) {
			if (!bounds.equals("0")) {
				row = database.update(PINGAN_SCORE.TABLE_NAME, values, " "
						+ PINGAN_SCORE.UID + "=? and " + PINGAN_SCORE.DATE
						+ "=? ", new String[] { uid + "", date });
			}
		} else {
			row = (int) database.insert(PINGAN_SCORE.TABLE_NAME, null, values);
		}
		cursor.close();
	}

	public ArrayList<HashMap<String, String>> getPinganScore(String from,
			String to) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.rawQuery("select * from "
				+ PINGAN_SCORE.TABLE_NAME + " where uid=? and "
				+ PINGAN_SCORE.DATE + " between " + from + " and " + to,
				new String[] { uid + "", from, to });
		ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
		while (cursor.moveToNext()) {
			HashMap<String, String> row = new HashMap<String, String>();
			row.put(PINGAN_SCORE.BOUNDS, cursor.getString(cursor
					.getColumnIndex(PINGAN_SCORE.BOUNDS)));
			row.put(PINGAN_SCORE.DATE,
					cursor.getString(cursor.getColumnIndex(PINGAN_SCORE.DATE)));
			row.put(PINGAN_SCORE.msg,
					cursor.getString(cursor.getColumnIndex(PINGAN_SCORE.msg)));
			data.add(row);
		}
		cursor.close();
		return data;
	}

	public int getPinganScore() {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.rawQuery("select * from "
				+ PINGAN_SCORE.TABLE_NAME + " where uid=" + uid, null);
		int data = 0;
		while (cursor.moveToNext()) {
			data += cursor.getInt(cursor.getColumnIndex(PINGAN_SCORE.BOUNDS));
		}
		cursor.close();
		return data;
	}

	public int getPinganScore(String date) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.rawQuery("select * from "
				+ PINGAN_SCORE.TABLE_NAME + " where " + PINGAN_SCORE.DATE
				+ "=? and uid=? ", new String[] { date, uid + "" });

		if (cursor.moveToFirst()) {
			int x = cursor.getInt(cursor.getColumnIndex(PINGAN_SCORE.BOUNDS));
			cursor.close();
			return x;
		}
		cursor.close();
		return -1;
	}

	public void saveTemperature(String date, long time, float max,
			ArrayList<Byte> data) {
		byte[] bts = new byte[data.size()];
		for (int i = 0; i < data.size(); i++) {
			bts[i] = data.get(i);
		}
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(TEMPERATURE.DATA, bts);
		values.put(TEMPERATURE.DATE, date);
		values.put(TEMPERATURE.MAX_TEMPE, max);
		values.put(TEMPERATURE.TIME, time);
		values.put(TEMPERATURE.UID, uid + "");
		database.insert(TEMPERATURE.TABLE_NAME, null, values);
	}

	public ArrayList<Temperature> getTemperature(String date) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		String sql = "select * from " + TEMPERATURE.TABLE_NAME + " where uid="
				+ uid + " order by " + TEMPERATURE.TIME + " desc ";
		if (date != null) {
			sql = "select * from " + TEMPERATURE.TABLE_NAME + " where uid="
					+ uid + " and " + TEMPERATURE.DATE + "=" + date;
		}
		Cursor cursor = database.rawQuery(sql, null);
		ArrayList<Temperature> list = new ArrayList<Temperature>();
		while (cursor.moveToNext()) {
			list.add(new Temperature(cursor.getBlob(cursor
					.getColumnIndex(TEMPERATURE.DATA)), cursor.getFloat(cursor
					.getColumnIndex(TEMPERATURE.MAX_TEMPE)), cursor
					.getLong(cursor.getColumnIndex(TEMPERATURE.TIME)), cursor
					.getString(cursor.getColumnIndex(TEMPERATURE.DATE)), cursor
					.getInt(cursor.getColumnIndex(TEMPERATURE._ID))));
		}
		cursor.close();
		return list;

	}

	public Temperature getTemperatureByid(String id) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		String sql = "select * from " + TEMPERATURE.TABLE_NAME + " where uid="
				+ uid + " and " + TEMPERATURE.TIME + "=" + id;

		Cursor cursor = database.rawQuery(sql, null);
		Temperature tt = null;
		if (cursor.moveToFirst()) {
			tt = new Temperature(cursor.getBlob(cursor
					.getColumnIndex(TEMPERATURE.DATA)), cursor.getFloat(cursor
					.getColumnIndex(TEMPERATURE.MAX_TEMPE)),
					cursor.getLong(cursor.getColumnIndex(TEMPERATURE.TIME)),
					cursor.getString(cursor.getColumnIndex(TEMPERATURE.DATE)),
					cursor.getInt(cursor.getColumnIndex(TEMPERATURE._ID)));
		}
		cursor.close();
		return tt;

	}
}

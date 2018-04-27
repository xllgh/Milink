package com.bandlink.air.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class HttpUtlis {
	// 主要是imageloader产生的缓存
	public static final String CACHE_Folder = "/bandlink/cache/";
	// 临时文件如图片裁剪等
	public static final String TEMP_Folder = "/bandlink/temp/";
	public static final int OFFICIAL_ID = -100;
	private String url = "http://192.168.9.24/enc/getdata.php";
	public static final String UPDATE_AIR = "http://www.lovefit.com/air/bandlink/version.json";
	public static final String UPDATE_URL = "http://www.lovefit.com/air/bandlink/air.json";
	public static final String UPDATE_APK = "http://www.lovefit.com/air/bandlink/air.apk";
	public static final String UPDATE_SAVENAME = "bindlink.apk";
	public static final String AVATAR_URL = "http://www.lovefit.com/ucenter/data/avatar/";
	public static final String CLUBLOGO_URL = "http://air.lovefit.com/Application/data/clublogo/";

	public static final String FRIEND_MYINFO = "http://air.lovefit.com/index.php/home/Dongtai/getMyInfo";
	public static final String FRIEND_GETDATA = "http://air.lovefit.com/index.php/home/Dongtai/getData";
	public static final String FRIEND_ADDCOMMENT = "http://air.lovefit.com/index.php/home/Comment/addComment";
	public static final String FRIEND_ADDFRIEND = "http://air.lovefit.com/index.php/home/user/addFriend";
	public static final String FRIEND_GUANZHU = "http://air.lovefit.com/index.php/home/Guanzhu/doFollow";
	public static final String FRIEND_ZAN = "http://air.lovefit.com/index.php/home/Like/addLike";
	public static final String FRIEND_GETFRIENDRANKDAY = "http://air.lovefit.com/index.php/home/data/getFriendRankDay";
	public static final String FRIEND_GETFRIENDRANKWEEK = "http://air.lovefit.com/index.php/home/data/getFriendRankWeek";
	public static final String FRIEND_GETFRIENDRANKMONTH = "http://air.lovefit.com/index.php/home/data/getFriendRankMonth";
	public static final String FRIEND_SHARE = "http://air.lovefit.com/index.php/home/Share/shareToFeed";
	public static final String GETUSERFRIEND = "http://air.lovefit.com/index.php/home/user/getUserFriend";
	public static final String DELETEFRIEND = "http://air.lovefit.com/index.php/home/user/deleteFriend";
	public static final String FRIEND_GETFOLLOWERLIST = "http://air.lovefit.com/index.php/home/Guanzhu/getFollowerList";
	public static final String FRIEND_UNFOLLOW = "http://air.lovefit.com/index.php/home/Guanzhu/unFollow";
	public static final String FRIEND_GETFOLLOWINGALL = "http://air.lovefit.com/index.php/home/Guanzhu/getFollowingListAll";
	public static final String GETFRIENDMESSAGE = "http://air.lovefit.com/index.php/home/data/getFriendMessage";
	public static final String DELETEFEED = "http://air.lovefit.com/index.php/home/Dongtai/doEditFeed/type/delFeed";
	public static final String GETUSERMAINPAGE = "http://air.lovefit.com/index.php/home/Dongtai/dongTaiUid";
	public static final String PERSONALNOTE = "http://air.lovefit.com/index.php/home/Dongtai/personalNote";
	public static final String POSTFEED = "http://air.lovefit.com/index.php/home/Follow/PostFeed";
	public static final String UPLOADIMAGE = "http://air.lovefit.com/index.php/home/Attach/ajaxUpload/uid/";
	private String user;
	private String pwd;
	private String uid;

	public static final String UPAVATAR_URL = "http://www.lovefit.com/enc/uploadAvatar.php?session=";
	public static final String BASE_URL = "http://air.lovefit.com/index.php/home";
	// public static final String BASE_URL = "http://192.168.9.33/home";
	public static final String IMAGE_URL = "http://www.lovefit.com:35555/";
	public static BitmapFactory.Options options = new BitmapFactory.Options();

	public static boolean checkURL(String url) {
		boolean value = false;
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL(url)
					.openConnection();
			int code = conn.getResponseCode();
			if (code != 200) {
				value = false;
			} else {
				value = true;
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return value;
	}

	/**
	 * Delete基础请求
	 * 
	 * @param url
	 *            请求地址
	 * @return 请求成功后的结果
	 */
	public static String doDelete(String url) {

		HttpResponse response;
		if (url != null && url.length() != 0) {

			HttpDelete delete = new HttpDelete(url);

			// delete.setHeader("Accept-Encoding", "gzip, deflate");
			// delete.setHeader("Accept-Language", "zh-CN");
			delete.setHeader("Accept",
					"application/json, application/xml, text/html, text/*, image/*, */*");
			try {
				response = new DefaultHttpClient().execute(delete);
				if (response != null) {
					int statusCode = response.getStatusLine().getStatusCode();
					if (statusCode == 200 || statusCode == 403) {

						StringBuilder builder = new StringBuilder();
						// 获取响应内容
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(response.getEntity()
										.getContent()));
						for (String s = reader.readLine(); s != null; s = reader
								.readLine()) {
							builder.append(s);
						}
						return builder.toString();
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	public static String doHttpPut(String url, JSONObject jo) throws Exception {

		StringBuffer result;
		try {
			HttpClient client = new DefaultHttpClient();
			HttpPut put = new HttpPut(url);
			put.setHeader("Content-type", "application/json");
			if (jo != null) {
				StringEntity params = new StringEntity(jo.toString());
				put.setEntity(params);
			}

			HttpResponse response = client.execute(put);
			System.out.println("Response Code:"
					+ response.getStatusLine().getStatusCode());
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));

			result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			return result.toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/***
	 * 判断是否Air更新
	 * 
	 * @param oldV
	 *            当前版本 （格式：AirX v2.2.5）
	 * @param newV
	 *            服务器版本
	 * @return 是否要更新
	 */
	public static int checkAir(String cur, String newV) {
		if (cur == null || newV == null) {
			return -1;
		}
		if (cur.length() < 1 || newV.length() < 1) {
			return -1;
		}
		try {
			String oldCode = cur.toLowerCase().replace(".", "").trim();
			oldCode = oldCode.substring(oldCode.length() - 3, oldCode.length());
			String newCode = newV.toLowerCase().replace(".", "").trim();
			newCode = newCode.substring(newCode.length() - 3, newCode.length());

			if (Integer.parseInt(oldCode) < Integer.parseInt(newCode)) {
				return 1;
			} else if (Integer.parseInt(oldCode) == Integer.parseInt(newCode)) {
				return 0;
			} else {
				return -1;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}

	public static String postData(String urlStr, String list) {
		String result = null;
		String end = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		try {

			URL url = new URL(urlStr);
			HttpURLConnection uc = (HttpURLConnection) url.openConnection();
			uc.setDoInput(true);
			uc.setDoOutput(true);
			uc.setUseCaches(false);
			uc.setRequestMethod("POST");
			uc.setRequestProperty("Connection", "Keep-Alive");
			uc.setRequestProperty("Content-Type",
					"multipart/form-data;boundary=" + boundary);
			uc.setRequestProperty("Charset", "UTF-8");
			uc.setConnectTimeout(30 * 1000);
			StringBuffer urlBuilder = new StringBuffer();
			DataOutputStream ds = new DataOutputStream(uc.getOutputStream());

			urlBuilder.append("--" + boundary + "\r\n");
			urlBuilder.append("Content-Disposition: form-data; name=\""
					+ URLEncoder.encode("data", "UTF-8") + "\"" + "\r\n");
			urlBuilder.append("\r\n");
			urlBuilder.append(URLEncoder.encode(list, "UTF-8") + "\r\n");

			ds.writeBytes(urlBuilder.toString());
			ds.writeBytes(twoHyphens + boundary + end);
			ds.flush();
			int code = uc.getResponseCode();
			if (code == 200) {
				InputStream is = uc.getInputStream();
				int ch;
				StringBuffer b = new StringBuffer();
				while ((ch = is.read()) != -1) {
					b.append((char) ch);
				}
				result = b.toString();
				is.close();

			}

		} catch (java.net.ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return result;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return result;
		}
		return result;
	}

	// clear the cache before time numDays
	public static int clearCacheFolder(File dir, long numDays) {
		int deletedFiles = 0;
		if (dir != null && dir.isDirectory()) {
			try {
				for (File child : dir.listFiles()) {
					if (child.isDirectory()) {
						deletedFiles += clearCacheFolder(child, numDays);
					}
					if (child.lastModified() < numDays) {
						if (child.delete()) {
							deletedFiles++;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return deletedFiles;
	}

	/***
	 * 上传byte数组
	 * 
	 * @param paramArrayOfByte
	 * @param http
	 * @return
	 */
	public String postByteArray(byte[] paramArrayOfByte, String http) {
		ByteArrayEntity arrayEntity = new ByteArrayEntity(paramArrayOfByte);
		arrayEntity.setContentType("application/octet-stream");
		HttpPost httpPost = new HttpPost(http);
		httpPost.setEntity(arrayEntity);
		DefaultHttpClient client = new DefaultHttpClient();
		try {
			HttpResponse mHttpResponse = client.execute(httpPost);
			int result = mHttpResponse.getStatusLine().getStatusCode();
			StringBuilder builder = new StringBuilder();
			// 获取响应内容
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					mHttpResponse.getEntity().getContent()));
			for (String s = reader.readLine(); s != null; s = reader.readLine()) {
				builder.append(s);
			}
			return builder.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String postJSON(String arr, String http)
			throws UnsupportedEncodingException {
		// ByteArrayEntity arrayEntity = new ByteArrayEntity(paramArrayOfByte);
		StringEntity arrayEntity = new StringEntity(arr);
		arrayEntity.setContentType("application/octet-stream");
		HttpPost httpPost = new HttpPost(http);
		httpPost.setEntity(arrayEntity);
		DefaultHttpClient client = new DefaultHttpClient();
		try {
			HttpResponse mHttpResponse = client.execute(httpPost);
			int result = mHttpResponse.getStatusLine().getStatusCode();
			StringBuilder builder = new StringBuilder();
			// 获取响应内容
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					mHttpResponse.getEntity().getContent()));
			for (String s = reader.readLine(); s != null; s = reader.readLine()) {
				builder.append(s);
			}
			return builder.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getAvatarUrl(String uid) {
		String size = "small";
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

	/***
	 * 
	 * @param uid
	 *            clubid
	 * @return
	 */
	public static String getLogoUrl(String uid) {
		String size = "small";
		int u = Math.abs(Integer.parseInt(uid));
		uid = String.format("%09d", u);
		String dir1 = uid.substring(0, 3);
		String dir2 = uid.substring(3, 5);
		String dir3 = uid.substring(5, 7);
		String typeadd = "";
		String url = dir1 + "/" + dir2 + "/" + dir3 + "/" + uid.substring(7)
				+ typeadd + "_club_" + size + ".jpg";
		return url;
	}

	public static String uploadAvatar(String urlStr, Bitmap photoPath) {
		String newName = "image.jpg";
		String result = null;
		String end = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";

		try {
			StringBuilder urlBuilder = new StringBuilder();
			urlBuilder.append(urlStr);
			// if (params != null && params.size() != 0) {
			// urlBuilder.append("/");
			// Iterator<Entry<String, String>> iterator = params.entrySet()
			// .iterator();
			// while (iterator.hasNext()) {
			// Entry<String, String> param = iterator.next();
			// urlBuilder
			// .append(URLEncoder.encode(param.getKey(), "UTF-8"))
			// .append('/')
			// .append(URLEncoder.encode(param.getValue(), "UTF-8"));
			// if (iterator.hasNext()) {
			// urlBuilder.append('/');
			// }
			// }
			// }

			URL url = new URL(urlBuilder.toString());
			HttpURLConnection uc = (HttpURLConnection) url.openConnection();
			uc.setDoInput(true);
			uc.setDoOutput(true);

			HttpClient client = new DefaultHttpClient();

			HttpGet getMethod = new HttpGet(urlBuilder.toString());
			HttpResponse response = client.execute(getMethod);

			uc.setUseCaches(false);
			uc.setRequestMethod("POST");

			client.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);
			client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
					30000);
			uc.setRequestProperty("Connection", "Keep-Alive");
			uc.setRequestProperty("Content-Type",
					"multipart/form-data;boundary=" + boundary);
			uc.setRequestProperty("Charset", "UTF-8");
			uc.setConnectTimeout(30 * 1000);

			DataOutputStream ds = new DataOutputStream(uc.getOutputStream());
			ds.writeBytes(twoHyphens + boundary + end);
			ds.writeBytes("Content-Disposition: form-data; "
					+ "name=\"upfile\";filename=\"" + newName + "\"" + end);
			ds.writeBytes("Content-Type: " + "image/jpeg" + end);
			ds.writeBytes(end);

			StringBuilder builder = new StringBuilder();

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			photoPath.compress(Bitmap.CompressFormat.JPEG, 100, baos);

			byte[] ba = baos.toByteArray();

			// String ba1 = Base64.encodeToString(ba,
			// Base64.URL_SAFE|Base64.NO_WRAP);
			InputStream isBm = new java.io.ByteArrayInputStream(ba);

			int bufferSize = 1024;
			byte[] buffer = new byte[bufferSize];
			int length = -1;

			while ((length = isBm.read(buffer)) > 0) {

				ds.write(buffer, 0, length);
			}
			ds.writeBytes(end);
			ds.writeBytes(twoHyphens + boundary + twoHyphens + end);
			isBm.close();
			ds.flush();
			int code = uc.getResponseCode();
			if (code == 200) {
				InputStream is = uc.getInputStream();
				int ch;
				StringBuffer b = new StringBuffer();
				while ((ch = is.read()) != -1) {
					b.append((char) ch);
				}
				result = b.toString();
				is.close();

			}

		} catch (java.net.ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return result;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return result;
		}
		return result;
	}

	public static String postFeedImg(Context context, String urlStr,
			LinkedList<String> paths) {

		String newName = "image.jpg";
		String result = null;
		String end = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";

		try {

			URL url = new URL(urlStr);
			HttpURLConnection uc = (HttpURLConnection) url.openConnection();
			uc.setDoInput(true);
			uc.setDoOutput(true);
			uc.setUseCaches(false);
			uc.setRequestMethod("POST");
			uc.setRequestProperty("Connection", "Keep-Alive");
			uc.setRequestProperty("Content-Type",
					"multipart/form-data;boundary=" + boundary);
			uc.setRequestProperty("Charset", "UTF-8");
			uc.setConnectTimeout(30 * 1000);
			StringBuffer urlBuilder = new StringBuffer();
			DataOutputStream ds = new DataOutputStream(uc.getOutputStream());
			// 上传文件总长
			long size = 0l;
			for (String s : paths) {
				size += new File(s).length();
				System.out.println("图片：" + new File(s).length());
			}
			// 需要缩放的百分比，0或大于1则不用缩放
			double per = 0;
			long standard = 1 * 1024 * 1024;
			Matrix ma = new Matrix();
			if (size > standard) {
				per = (double) standard / (double) size;
				ma.postScale((float) per, (float) per);
				System.out.println("总大小" + size + "缩放比例" + per);
			}
			for (String path : paths) {

				ds.writeBytes(urlBuilder.toString());
				ds.writeBytes(twoHyphens + boundary + end);
				ds.writeBytes("Content-Disposition: form-data; " + "name=\"up"
						+ new Random().nextInt(900) + "\";filename=\""
						+ newName + "\"" + end);
				ds.writeBytes("Content-Type: " + "image/jpeg" + end);
				ds.writeBytes(end);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				Bitmap bitmap;
				try {
					bitmap = ImageLoader.getInstance().loadImageSync(
							"file://" + path);
					if (bitmap == null && path.startsWith("content")) {
						path = getImageAbsolutePath(context, Uri.parse(path));
						bitmap = ImageLoader.getInstance().loadImageSync(
								"file://" + path);
					}
					if (bitmap == null) {
						bitmap = MediaStore.Images.Media.getBitmap(
								context.getContentResolver(), Uri.parse(path));
					}
				} catch (Exception e) {
					bitmap = BitmapFactory.decodeFile(path);
				}

				if (per > 0 && per < 1) {
					bitmap = Bitmap.createBitmap(bitmap, 0, 0,
							bitmap.getWidth(), bitmap.getHeight(), ma, true);
				}
				bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
				byte[] ba = baos.toByteArray();
				InputStream isBm = new java.io.ByteArrayInputStream(ba);
				int bufferSize = 1024;
				byte[] buffer = new byte[bufferSize];
				int length = -1;
				while ((length = isBm.read(buffer)) > 0) {

					ds.write(buffer, 0, length);
				}
				ds.writeBytes(end);
				ds.writeBytes(twoHyphens + boundary + twoHyphens + end);
				isBm.close();
			}

			ds.flush();
			int code = uc.getResponseCode();
			if (code == 200) {
				InputStream is = uc.getInputStream();
				int ch;
				StringBuffer b = new StringBuffer();
				while ((ch = is.read()) != -1) {
					b.append((char) ch);
				}
				result = b.toString();
				is.close();

			}

		} catch (java.net.ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return result;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return result;
		}
		return result;
	}

	public static String getPathFromUri(Context context, String uri) {
		String selection = MediaColumns._ID + " = ?";
		String[] selectionArgs = new String[] { uri };

		Uri queryUri = Uri.parse("content://media/external/images/media");
		String fileName = getDataColumn(context, queryUri, selection,
				selectionArgs);
		return fileName;
	}

	public static String getDataColumn(Context context, Uri uri,
			String selection, String[] selectionArgs) {
		Cursor cursor = null;
		final String column = MediaColumns.DATA;
		final String[] projection = { column };
		try {
			cursor = context.getContentResolver().query(uri, projection,
					selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				final int index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	public static String SendFeed(String urlStr, ArrayList<NameValuePair> data) {

		String newName = "image.jpg";
		String result = null;
		String end = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		options.inJustDecodeBounds = true;
		options.inSampleSize = 2;

		try {

			URL url = new URL(urlStr);
			HttpURLConnection uc = (HttpURLConnection) url.openConnection();
			uc.setDoInput(true);
			uc.setDoOutput(true);

			uc.setUseCaches(false);
			uc.setRequestMethod("POST");

			uc.setRequestProperty("Connection", "Keep-Alive");
			uc.setRequestProperty("Content-Type",
					"multipart/form-data;boundary=" + boundary);
			uc.setRequestProperty("Charset", "UTF-8");
			uc.setConnectTimeout(30 * 1000);
			StringBuffer urlBuilder = new StringBuffer();
			DataOutputStream ds = new DataOutputStream(uc.getOutputStream());

			for (NameValuePair na : data) {
				urlBuilder.append("--" + boundary + "\r\n");
				urlBuilder.append("Content-Disposition: form-data; name=\""
						+ na.getName() + "\"" + "\r\n");
				urlBuilder.append("\r\n");
				urlBuilder.append(URLEncoder.encode(na.getValue(), "UTF-8")
						+ "\r\n");
			}
			// if (BluetoothLeService.bLogfile)
			// MyLog.e("----", urlBuilder.toString());
			ds.writeBytes(urlBuilder.toString());
			ds.writeBytes(twoHyphens + boundary + end);
			ds.writeBytes("Content-Disposition: form-data; "
					+ "name=\"poster\";filename=\"" + newName + "\"" + end);
			ds.writeBytes("Content-Type: " + "image/jpeg" + end);
			ds.writeBytes(end);
			ds.flush();
			int code = uc.getResponseCode();
			if (code == 200) {
				InputStream is = uc.getInputStream();
				int ch;
				StringBuffer b = new StringBuffer();
				while ((ch = is.read()) != -1) {
					b.append((char) ch);
				}
				result = b.toString();
				is.close();

			}

		} catch (java.net.ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return result;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return result;
		}
		return result;
	}

	/***
	 * create club by post
	 * 
	 * @param urlStr
	 * @param params
	 * @param photoPath
	 * @return
	 */
	public static String postClubCreate(String urlStr,
			final Map<String, String> params, Bitmap photoPath) {

		String newName = "image.jpg";
		String result = null;

		String end = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";

		try {

			URL url = new URL(urlStr);
			HttpURLConnection uc = (HttpURLConnection) url.openConnection();
			uc.setDoInput(true);
			uc.setDoOutput(true);
			uc.setUseCaches(false);
			uc.setRequestMethod("POST");
			uc.setRequestProperty("Connection", "Keep-Alive");
			uc.setRequestProperty("Content-Type",
					"multipart/form-data;boundary=" + boundary);
			uc.setRequestProperty("Charset", "UTF-8");
			uc.setConnectTimeout(30 * 1000);
			StringBuffer urlBuilder = new StringBuffer();
			DataOutputStream ds = new DataOutputStream(uc.getOutputStream());
			if (params != null && params.size() != 0) {
				Iterator<Entry<String, String>> iterator = params.entrySet()
						.iterator();
				while (iterator.hasNext()) {
					Entry<String, String> param = iterator.next();

					urlBuilder.append("--" + boundary + "\r\n");
					urlBuilder.append("Content-Disposition: form-data; name=\""
							+ URLEncoder.encode(param.getKey(), "UTF-8") + "\""
							+ "\r\n");
					urlBuilder.append("\r\n");
					urlBuilder.append(URLEncoder.encode(param.getValue(),
							"UTF-8") + "\r\n");

				}
			}

			ds.writeBytes(urlBuilder.toString());
			ds.writeBytes(twoHyphens + boundary + end);
			ds.writeBytes("Content-Disposition: form-data; "
					+ "name=\"upfile\";filename=\"" + newName + "\"" + end);
			ds.writeBytes("Content-Type: " + "image/jpeg" + end);
			ds.writeBytes(end);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			photoPath.compress(Bitmap.CompressFormat.JPEG, 100, baos);

			byte[] ba = baos.toByteArray();

			InputStream isBm = new java.io.ByteArrayInputStream(ba);

			int bufferSize = 1024;
			byte[] buffer = new byte[bufferSize];
			int length = -1;

			while ((length = isBm.read(buffer)) > 0) {

				ds.write(buffer, 0, length);
			}
			ds.writeBytes(end);
			ds.writeBytes(twoHyphens + boundary + twoHyphens + end);
			isBm.close();
			ds.flush();
			int code = uc.getResponseCode();
			if (code == 200) {
				InputStream is = uc.getInputStream();
				int ch;
				StringBuffer b = new StringBuffer();
				while ((ch = is.read()) != -1) {
					b.append((char) ch);
				}
				result = b.toString();
				is.close();

			}

		} catch (java.net.ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return result;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return result;
		}
		return result;
	}

	/***
	 * create match by post
	 * 
	 * @param urlStr
	 *            url
	 * @param logo
	 *            match's logo
	 * @param data
	 *            base data
	 * @param t
	 *            text rules
	 * @param bi
	 *            img rules
	 * @param ids
	 *            invite list
	 * @return create result
	 */
	public static String postMatchCreate(String urlStr, Bitmap logo,
			ArrayList<String> data, String t, ArrayList<String> bi, String ids) {

		String newName = "image.jpg";
		String result = null;
		String end = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		options.inJustDecodeBounds = true;
		options.inSampleSize = 1;

		try {

			URL url = new URL(urlStr);
			HttpURLConnection uc = (HttpURLConnection) url.openConnection();
			uc.setDoInput(true);
			uc.setDoOutput(true);

			uc.setUseCaches(false);
			uc.setRequestMethod("POST");

			uc.setRequestProperty("Connection", "Keep-Alive");
			uc.setRequestProperty("Content-Type",
					"multipart/form-data;boundary=" + boundary);
			uc.setRequestProperty("Charset", "UTF-8");
			uc.setConnectTimeout(30 * 1000);
			StringBuffer urlBuilder = new StringBuffer();
			DataOutputStream ds = new DataOutputStream(uc.getOutputStream());

			urlBuilder.append("--" + boundary + "\r\n");
			urlBuilder.append("Content-Disposition: form-data; name=\""
					+ URLEncoder.encode("title", "UTF-8") + "\"" + "\r\n");
			urlBuilder.append("\r\n");
			urlBuilder.append(URLEncoder.encode(data.get(0), "UTF-8") + "\r\n");

			urlBuilder.append("--" + boundary + "\r\n");
			urlBuilder.append("Content-Disposition: form-data; name=\""
					+ URLEncoder.encode("ispublic", "UTF-8") + "\"" + "\r\n");
			urlBuilder.append("\r\n");
			urlBuilder.append(URLEncoder.encode(data.get(6) + "", "UTF-8")
					+ "\r\n");

			urlBuilder.append("--" + boundary + "\r\n");
			urlBuilder.append("Content-Disposition: form-data; name=\""
					+ URLEncoder.encode("target", "UTF-8") + "\"" + "\r\n");
			urlBuilder.append("\r\n");
			urlBuilder.append(URLEncoder.encode(data.get(4) + "", "UTF-8")
					+ "\r\n");

			urlBuilder.append("--" + boundary + "\r\n");
			urlBuilder.append("Content-Disposition: form-data; name=\""
					+ URLEncoder.encode("inviter", "UTF-8") + "\"" + "\r\n");
			urlBuilder.append("\r\n");
			urlBuilder.append(URLEncoder.encode(ids + "", "UTF-8") + "\r\n");

			urlBuilder.append("--" + boundary + "\r\n");
			urlBuilder.append("Content-Disposition: form-data; name=\""
					+ URLEncoder.encode("start", "UTF-8") + "\"" + "\r\n");
			urlBuilder.append("\r\n");
			urlBuilder.append(URLEncoder.encode(data.get(7) + "", "UTF-8")
					+ "\r\n");
			if (!data.get(2).equals("-1")) {
				urlBuilder.append("--" + boundary + "\r\n");
				urlBuilder.append("Content-Disposition: form-data; name=\""
						+ URLEncoder.encode("clubid", "UTF-8") + "\"" + "\r\n");
				urlBuilder.append("\r\n");
				urlBuilder.append(URLEncoder.encode(data.get(2) + "", "UTF-8")
						+ "\r\n");
			}
			urlBuilder.append("--" + boundary + "\r\n");
			urlBuilder.append("Content-Disposition: form-data; name=\""
					+ URLEncoder.encode("end", "UTF-8") + "\"" + "\r\n");
			urlBuilder.append("\r\n");
			urlBuilder.append(URLEncoder.encode(data.get(5) + "", "UTF-8")
					+ "\r\n");
			if (t != null) {
				urlBuilder.append("--" + boundary + "\r\n");
				urlBuilder.append("Content-Disposition: form-data; name=\""
						+ URLEncoder.encode("rulecontent", "UTF-8") + "\""
						+ "\r\n");
				urlBuilder.append("\r\n");
				urlBuilder.append(URLEncoder.encode(t + "", "UTF-8") + "\r\n");
				ds.writeBytes(urlBuilder.toString());
				ds.writeBytes(twoHyphens + boundary + end);
			}
			if (bi != null) {
				// 上传文件总长
				long size = 0l;
				for (String s : bi) {
					size += new File(s).length();
					System.out.println("图片：" + new File(s).length());
				}
				// 需要缩放的百分比，0或大于1则不用缩放
				double per = 0;
				long standard = 2 * 1024 * 1024;
				Matrix ma = new Matrix();
				if (size > standard) {
					per = (double) standard / (double) size;
					ma.postScale((float) per, (float) per);
					System.out.println("总大小" + size + "缩放比例" + per);
				}
				// 缩放后的大小
				long sizeAf = 0;
				for (int i = 0; i < bi.size(); i++) {
					ds.writeBytes(twoHyphens + boundary + end);
					ds.writeBytes("Content-Disposition: form-data; "
							+ "name=\"up" + i + "\";filename=\"" + newName
							+ "\"" + end);
					ds.writeBytes("Content-Type: " + "image/jpeg" + end);
					ds.writeBytes(end);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					DisplayImageOptions option = new DisplayImageOptions.Builder()
							.imageScaleType(ImageScaleType.EXACTLY).build();
					Bitmap bitmap;
					try {
						bitmap = ImageLoader.getInstance().loadImageSync(
								"file://" + bi.get(i), option);

					} catch (Exception e) {
						bitmap = BitmapFactory.decodeFile(bi.get(i));
					}
					if (per > 0 && per < 1) {
						bitmap = Bitmap
								.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
										bitmap.getHeight(), ma, true);
					}

					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

					byte[] ba = baos.toByteArray();
					// String ba1 = Base64.encodeToString(ba,
					// Base64.URL_SAFE|Base64.NO_WRAP);
					InputStream isBm = new java.io.ByteArrayInputStream(ba);

					int bufferSize = 1024;
					byte[] buffer = new byte[bufferSize];
					int length = -1;
					while ((length = isBm.read(buffer)) > 0) {

						ds.write(buffer, 0, length);
					}
					ds.writeBytes(end);
					ds.writeBytes(twoHyphens + boundary + twoHyphens + end);
					isBm.close();
				}
			}
			ds.writeBytes(twoHyphens + boundary + end);
			ds.writeBytes("Content-Disposition: form-data; "
					+ "name=\"poster\";filename=\"" + newName + "\"" + end);
			ds.writeBytes("Content-Type: " + "image/jpeg" + end);
			ds.writeBytes(end);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			logo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
			byte[] ba = baos.toByteArray();
			// String ba1 = Base64.encodeToString(ba,
			// Base64.URL_SAFE|Base64.NO_WRAP);
			InputStream isBm = new java.io.ByteArrayInputStream(ba);

			int bufferSize = 1024;
			byte[] buffer = new byte[bufferSize];
			int length = -1;

			while ((length = isBm.read(buffer)) > 0) {

				ds.write(buffer, 0, length);
			}
			ds.writeBytes(end);
			ds.writeBytes(twoHyphens + boundary + twoHyphens + end);
			isBm.close();
			ds.flush();
			int code = uc.getResponseCode();
			if (code == 200) {
				InputStream is = uc.getInputStream();
				int ch;
				StringBuffer b = new StringBuffer();
				while ((ch = is.read()) != -1) {
					b.append((char) ch);
				}
				result = b.toString();
				is.close();

			}

		} catch (java.net.ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return result;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return result;
		}
		return result;
	}

	public static Bitmap Bytes2Bimap(byte[] b) {
		if (b.length != 0) {
			return BitmapFactory.decodeByteArray(b, 0, b.length);
		} else {
			return null;
		}
	}

	public static String getRequest(final String urlString,
			final Map<String, String> params) throws Exception {

		try {
			StringBuilder urlBuilder = new StringBuilder();
			urlBuilder.append(urlString);
			if (params != null && params.size() != 0) {
				urlBuilder.append("/");
				Iterator<Entry<String, String>> iterator = params.entrySet()
						.iterator();
				while (iterator.hasNext()) {
					Entry<String, String> param = iterator.next();
					urlBuilder
							.append(URLEncoder.encode(param.getKey(), "UTF-8"))
							.append('/')
							.append(URLEncoder.encode(param.getValue(), "UTF-8"));
					if (iterator.hasNext()) {
						urlBuilder.append('/');
					}
				}
				// MyLog.e("---->>", urlBuilder.toString());
			}
			// 创建HttpClient对象

			HttpClient client = new DefaultHttpClient();

			// 发送get请求创建HttpGet对象

			HttpGet getMethod = new HttpGet(urlBuilder.toString());
			HttpResponse response = client.execute(getMethod);
			// 获取状态码
			client.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);
			// 读取超时
			client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
					30000);
			if (response.getStatusLine().getStatusCode() == 200) {
				StringBuilder builder = new StringBuilder();
				// 获取响应内容
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(response.getEntity().getContent()));
				for (String s = reader.readLine(); s != null; s = reader
						.readLine()) {
					builder.append(s);
				}
				return builder.toString();
			}
		} catch (Exception e) {
			throw e;
		}
		return null;
	}

	public static String getRequestJson(final String urlString,
			final Map<String, String> params) throws Exception {

		try {
			StringBuilder urlBuilder = new StringBuilder();
			urlBuilder.append(urlString);
			if (params != null && params.size() != 0) {
				urlBuilder.append("/");
				Iterator<Entry<String, String>> iterator = params.entrySet()
						.iterator();
				while (iterator.hasNext()) {
					Entry<String, String> param = iterator.next();
					urlBuilder
							.append(URLEncoder.encode(param.getKey(), "UTF-8"))
							.append('/')
							.append(URLEncoder.encode(param.getValue(), "UTF-8"));
					if (iterator.hasNext()) {
						urlBuilder.append('/');
					}
				}
				// MyLog.e("---->>", urlBuilder.toString());
			}
			// 创建HttpClient对象

			HttpClient client = new DefaultHttpClient();

			// 发送get请求创建HttpGet对象

			HttpGet getMethod = new HttpGet(urlBuilder.toString());
			getMethod.setHeader("Accept", "application/json");
			HttpResponse response = client.execute(getMethod);
			// 获取状态码
			client.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);
			// 读取超时
			client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
					30000);
			if (response.getStatusLine().getStatusCode() == 200) {
				StringBuilder builder = new StringBuilder();
				// 获取响应内容
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(response.getEntity().getContent()));
				for (String s = reader.readLine(); s != null; s = reader
						.readLine()) {
					builder.append(s);
				}
				return builder.toString();
			}
		} catch (Exception e) {
			throw e;
		}
		return null;
	}

	public static HttpGet getHttpGet(String url) {
		HttpGet request = new HttpGet(url);
		return request;
	}

	// 获得post请求的对象request
	public static HttpPost getHttpPost(String url) {
		HttpPost request = new HttpPost(url);
		return request;
	}

	// 根据get请求获得响应对象response

	public static HttpResponse getHttpResponse(HttpGet request)
			throws ClientProtocolException, IOException {
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = httpClient.execute(request);
		return response;
	}

	// 初始化HttpClient，并设置超时

	public static HttpClient getHttpClient() {
		BasicHttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
		HttpConnectionParams.setSoTimeout(httpParams, 10000);
		HttpClient client = new DefaultHttpClient(httpParams);
		return client;
	}

	// 根据post请求获得响应对象response

	public static HttpResponse getHttpResponse(HttpPost request)
			throws ClientProtocolException, IOException {

		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = httpClient.execute(request);
		return response;
	}

	// 发送Get请求后，获得响应的查询结果

	public static String queryStringForGet(String url) {

		// 根据url获得HttpGet的对象
		HttpGet request = getHttpGet(url);
		String result = null;
		try {
			// 获得响应的对象
			HttpResponse response = getHttpResponse(request);
			// 判断是否请求成功
			if (response.getStatusLine().getStatusCode() == 200) {
				// 获得响应
				result = EntityUtils.toString(response.getEntity());
				return result;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();

			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return result;
		}
		return null;
	}

	// 发送Post请求，获得响应的查询结果

	public static String queryStringForPost(String url) {

		// 根据url获得HttpPost对象

		HttpPost request = getHttpPost(url);
		request.addHeader("Content-Type", "text/html");
		request.addHeader("charset", HTTP.UTF_8);
		String result = null;
		try {

			// 获得响应对象
			HttpResponse response = getHttpResponse(request);
			// 判断是否请求成功
			if (response.getStatusLine().getStatusCode() == 200) {
				result = EntityUtils.toString(response.getEntity());
				return result;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return result;
		}
		return null;
	}

	public static String getJsonContent(String url_path) {
		try {
			URL url = new URL(url_path);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();

			connection.setConnectTimeout(3000);
			connection.setRequestMethod("GET");
			connection.setDoInput(true);

			int code = connection.getResponseCode();
			if (code == 200) {

				return changeInputStream(connection.getInputStream());
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return "";
	}

	private static String changeInputStream(InputStream inputStream) {
		// TODO Auto-generated method stub
		String jsonString = "";
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		int len = 0;
		byte[] data = new byte[1024];
		try {
			while ((len = inputStream.read(data)) != -1) {
				outputStream.write(data, 0, len);
			}
			jsonString = new String(outputStream.toByteArray());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonString;
	}

	public HttpUtlis(String u, String p, String uid) {
		user = u;
		pwd = p;
		uid = u;
	}

	public HttpUtlis(String u, String p) {
		user = u;
		pwd = p;
	}

	public void getUserTarget() {
		String param = "?user=" + user + "&pwd=" + pwd + "&type=138";
		String urlString = url + param;

	}

	public void getUserDevice() {

	}

	public void upLoadSettings(Map<String, Object> param) {

	}

	public static String getRequestForPost(final String urlString,
			final Map<String, String> params) throws Exception {

		try {
			StringBuilder urlBuilder = new StringBuilder();
			urlBuilder.append(urlString);
			if (params != null && params.size() != 0) {
				urlBuilder.append("&");
				Iterator<Entry<String, String>> iterator = params.entrySet()
						.iterator();
				while (iterator.hasNext()) {
					Entry<String, String> param = iterator.next();
					urlBuilder
							.append(URLEncoder.encode(param.getKey(), "UTF-8"))
							.append('=')
							.append(URLEncoder.encode(param.getValue(), "UTF-8"));
					if (iterator.hasNext()) {
						urlBuilder.append('&');
					}
				}
			}
			// 创建HttpClient对象

			HttpClient client = new DefaultHttpClient();

			// 发送get请求创建HttpGet对象

			HttpPost getMethod = new HttpPost(urlBuilder.toString());
			HttpResponse response = client.execute(getMethod);
			// 获取状态码
			client.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);
			// 读取超时
			client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
					30000);
			if (response.getStatusLine().getStatusCode() == 200) {
				StringBuilder builder = new StringBuilder();
				// 获取响应内容
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(response.getEntity().getContent()));
				for (String s = reader.readLine(); s != null; s = reader
						.readLine()) {
					builder.append(s);
				}
				return builder.toString();
			}
		} catch (Exception e) {
			throw e;
		}
		return null;
	}

	public static String getRequestForPostJSON(final String urlString,
			final Map<String, String> params) throws Exception {

		try {
			StringBuilder urlBuilder = new StringBuilder();
			urlBuilder.append(urlString);
			if (params != null && params.size() != 0) {
				urlBuilder.append("&");
				Iterator<Entry<String, String>> iterator = params.entrySet()
						.iterator();
				while (iterator.hasNext()) {
					Entry<String, String> param = iterator.next();
					urlBuilder
							.append(URLEncoder.encode(param.getKey(), "UTF-8"))
							.append('=')
							.append(URLEncoder.encode(param.getValue(), "UTF-8"));
					if (iterator.hasNext()) {
						urlBuilder.append('&');
					}
				}
			}
			// 创建HttpClient对象

			HttpClient client = new DefaultHttpClient();

			// 发送get请求创建HttpGet对象

			HttpPost getMethod = new HttpPost(urlBuilder.toString());
			getMethod.setHeader("Accept", "application/json");
			HttpResponse response = client.execute(getMethod);
			// 获取状态码
			client.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);
			// 读取超时
			client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
					30000);
			if (response.getStatusLine().getStatusCode() == 200) {
				StringBuilder builder = new StringBuilder();
				// 获取响应内容
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(response.getEntity().getContent()));
				for (String s = reader.readLine(); s != null; s = reader
						.readLine()) {
					builder.append(s);
				}
				return builder.toString();
			}
		} catch (Exception e) {
			throw e;
		}
		return null;
	}

	public static String postData(String urlStr, HashMap<String, String> map) {

		String result = null;
		String end = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";

		try {

			URL url = new URL(urlStr);
			HttpURLConnection uc = (HttpURLConnection) url.openConnection();
			uc.setDoInput(true);
			uc.setDoOutput(true);

			uc.setUseCaches(false);
			uc.setRequestMethod("POST");

			uc.setRequestProperty("Connection", "Keep-Alive");
			uc.setRequestProperty("Content-Type",
					"multipart/form-data;boundary=" + boundary);
			uc.setRequestProperty("Charset", "UTF-8");
			uc.setConnectTimeout(30 * 1000);
			StringBuffer urlBuilder = new StringBuffer();
			DataOutputStream ds = new DataOutputStream(uc.getOutputStream());

			if (map != null && map.size() != 0) {
				urlBuilder.append("&");
				Iterator<Entry<String, String>> iterator = map.entrySet()
						.iterator();
				while (iterator.hasNext()) {
					Entry<String, String> param = iterator.next();
					urlBuilder.append("--" + boundary + "\r\n");
					urlBuilder.append("Content-Disposition: form-data; name=\""
							+ URLEncoder.encode(
									URLEncoder.encode(param.getKey(), "UTF-8"),
									"UTF-8") + "\"" + "\r\n");
					urlBuilder.append("\r\n");
					urlBuilder.append(URLEncoder.encode(
							URLEncoder.encode(param.getValue(), "UTF-8"),
							"UTF-8") + "\r\n");
				}
			}

			ds.writeBytes(urlBuilder.toString());
			ds.writeBytes(twoHyphens + boundary + end);

			ds.flush();
			int code = uc.getResponseCode();
			if (code == 200) {
				InputStream is = uc.getInputStream();
				int ch;
				StringBuffer b = new StringBuffer();
				while ((ch = is.read()) != -1) {
					b.append((char) ch);
				}
				result = b.toString();
				is.close();

			}

		} catch (java.net.ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return result;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return result;
		}
		return result;
	}

	public static String postData2(String urlStr, HashMap<String, String> map) {

		String result = null;
		String end = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";

		try {

			URL url = new URL(urlStr);
			HttpURLConnection uc = (HttpURLConnection) url.openConnection();
			uc.setDoInput(true);
			uc.setDoOutput(true);

			uc.setUseCaches(false);
			uc.setRequestMethod("POST");

			uc.setRequestProperty("Connection", "Keep-Alive");
			uc.setRequestProperty("Content-Type",
					"multipart/form-data;boundary=" + boundary);
			uc.setRequestProperty("Charset", "UTF-8");
			uc.setConnectTimeout(30 * 1000);
			StringBuffer urlBuilder = new StringBuffer();
			DataOutputStream ds = new DataOutputStream(uc.getOutputStream());

			if (map != null && map.size() != 0) {
				urlBuilder.append("&");
				Iterator<Entry<String, String>> iterator = map.entrySet()
						.iterator();
				while (iterator.hasNext()) {
					Entry<String, String> param = iterator.next();
					urlBuilder.append("--" + boundary + "\r\n");
					urlBuilder.append("Content-Disposition: form-data; name=\""
							+ URLEncoder.encode(
									URLEncoder.encode(param.getKey(), "UTF-8"),
									"UTF-8") + "\"" + "\r\n");
					urlBuilder.append("\r\n");
					urlBuilder.append(URLEncoder.encode(
							URLEncoder.encode(param.getValue(), "UTF-8"),
							"UTF-8") + "\r\n");
				}
			}

			ds.writeBytes(urlBuilder.toString());
			ds.writeBytes(twoHyphens + boundary + end);

			ds.flush();
			int code = uc.getResponseCode();
			if (code == 200) {
				InputStream is = uc.getInputStream();
				int ch;
				StringBuffer b = new StringBuffer();
				while ((ch = is.read()) != -1) {
					b.append((char) ch);
				}
				result = b.toString();
				is.close();

			}

		} catch (java.net.ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return result;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return result;
		}
		return result;
	}

	/**
	 * 根据Uri获取图片绝对路径，解决Android4.4以上版本Uri转换
	 * 
	 * @param activity
	 * @param imageUri
	 * @author yaoxing
	 * @date 2014-10-12
	 */
	public static String getImageAbsolutePath(Context context, Uri imageUri) {
		if (context == null || imageUri == null)
			return null;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT
				&& DocumentsContract.isDocumentUri(context, imageUri)) {
			if (isExternalStorageDocument(imageUri)) {
				String docId = DocumentsContract.getDocumentId(imageUri);
				String[] split = docId.split(":");
				String type = split[0];
				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/"
							+ split[1];
				}
			} else if (isDownloadsDocument(imageUri)) {
				String id = DocumentsContract.getDocumentId(imageUri);
				Uri contentUri = ContentUris.withAppendedId(
						Uri.parse("content://downloads/public_downloads"),
						Long.valueOf(id));
				return getDataColumn(context, contentUri, null, null);
			} else if (isMediaDocument(imageUri)) {
				String docId = DocumentsContract.getDocumentId(imageUri);
				String[] split = docId.split(":");
				String type = split[0];
				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}
				String selection = MediaStore.Images.Media._ID + "=?";
				String[] selectionArgs = new String[] { split[1] };
				return getDataColumn(context, contentUri, selection,
						selectionArgs);
			}
		} // MediaStore (and general)
		else if ("content".equalsIgnoreCase(imageUri.getScheme())) {
			// Return the remote address
			if (isGooglePhotosUri(imageUri))
				return imageUri.getLastPathSegment();
			return getDataColumn(context, imageUri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(imageUri.getScheme())) {
			return imageUri.getPath();
		}
		return null;
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is Google Photos.
	 */
	public static boolean isGooglePhotosUri(Uri uri) {
		return "com.google.android.apps.photos.content".equals(uri
				.getAuthority());
	}

}

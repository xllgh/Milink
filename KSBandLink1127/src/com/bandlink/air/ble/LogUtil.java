package com.bandlink.air.ble;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

import android.content.Context;
import android.os.Environment;

import com.bandlink.air.MyLog;

/**
 * 打印工具类
 * @author meimuan
 * 功能点：<p>
 * <ul>
 * 	<li>可以控制打印输出</li>
 * 	<li>可以简便打印，经过包装得到精准特定格式的日志信息</li>
 * 	<li>可以直接打印异常</li>
 * 	<li>可以定位异常信息</li>
 * 	<li>可以便捷统一修改，优化</li>
 *  <li>^^^^^^^^^^^^^^^^^^^^^^^^^^^^</li>
 *  <li>同步输出日志到本地sdcard文件</li>
 * </ul>
 * <p>
 * 需要注册权限：
 * <table>
 *  <tr>
 * 		<td>* 读写SD卡权限    </td><td>android.permission.WRITE_EXTERNAL_STORAGE</td>
 *  </tr>
 * 	<tr>
 * 		<td>? 网络权限    </td><td>android.permission.INTERNET</td>
 *  </tr>
 * </table>
 * </p>
 * <p>
 * 说明：<br/>
 * 1. 日志是针对天数级别的，自动生成的日志名称yyyyMMdd.log形式。</br>
 * 2. 如果当天的日志需要新开一个（比如：日志很大了，需要从新生成一个）
 * </p>
 */
public class LogUtil {
	/** 控制打印级别 在level级别之上才可以被打印出来 */
	//public static int LEVEL = 3;
	public static int LEVEL = 3;
	/** 打印级别为V，对应MyLog.v*/
	public final static int V = 1;
	/** 打印级别为W，对应MyLog.w*/
	public final static int W = 2;
	/** 打印级别为I，对应MyLog.i*/
	public final static int I = 3;
	/** 打印级别为D，对应MyLog.d*/
	public final static int D = 4;
	/** 打印级别为E，对应MyLog.e*/
	public final static int E = 5;
	/** 最高级别打印，强制性打印，LEVEL无法关闭。 */
	private final static int P = Integer.MAX_VALUE;
	/** 打印修饰符号*/
	private static final String _L = "[";
	private static final String _R = "]";
	/** 是否同步输出到本地日志文件 */
	private static boolean IS_SYNS = false;
	/** 打印日志保存路径*/
	private static String LOG_FILE_DIR = "";
	/** 生成一个日期文件名格式的日式格式对象*/
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd",Locale.getDefault());
	/** 是否新建一个日志文件。*/
	private static boolean IF_START_NEWLOG = true;
	/** 保存创建的文件路径 */
	private static String CURRENT_LOG_NAME = "";
	/** 针对天数级别的。如果当天已经存在一个LOG了，而使用者需要新开一个LOG，那么将计数 */
	private static int FILE_LOG_COUNT = 0;
	/** 单个日志的最大的容量,如果一个日志太大了，打开会影响效率*/
	private static int LOG_MAX_SIZE = 6 * 1024 * 1024;
	/** 检测文件目的地址是否正确 */
	private static Pattern pattern = Pattern.compile("(\\w+/)+");
	/** 设置是否同步记录信息或者异常到日志文件。*/
	public static final void setSyns(boolean flag) {
		synchronized (LogUtil.class){
			IS_SYNS = flag;
		}
	}
	/** 开启一个新的LOG */
	public static final void startNewLog() {
		IF_START_NEWLOG = true;
	}
	/**
	 * 打印信息
	 * @param message
	 */
	public static final void i(String message) {
		if (LEVEL <= I) {
			MyLog.i(getTag(message), message);
			if (IS_SYNS && MyLog.LOG) {
				LogFile.writeLog(message);
			}
		}
	}
	
	public static final void i(Exception exp) {
		if (LEVEL <= I) {
			MyLog.i(getTag(exp),getMessage(exp));
			if (IS_SYNS && MyLog.LOG) {
				LogFile.writeLog(exp);
			}
		}
	}
	
	public static final void e(String message) {
		if (LEVEL <= E) {
			MyLog.e(/*getTag(message)*/"AIRBLE", message);
			if (IS_SYNS && MyLog.LOG) {
				LogFile.writeLog(message);
			}
		}
	}
	public static final void e(String tag,String message) {
		if (LEVEL <= E) {
			MyLog.e(tag, message);
			if (IS_SYNS && MyLog.LOG) {
				LogFile.writeLog(message);
			}
		}
	}
	public static final void e(String message,boolean print) {
		if (LEVEL <= E) {
			MyLog.e(getTag(message), message);
			if (IS_SYNS && print) {
				LogFile.writeLog(message);
			}
		}
	}
	
	public static final void e(Exception exp) {
		if (LEVEL <= E) {
			MyLog.e(getTag(exp),getMessage(exp));
			if (IS_SYNS && MyLog.LOG) {
				LogFile.writeLog(exp);
			}
		}
	}
	
	public static final void w(String message) {
		if (LEVEL <= W) {
			MyLog.w(getTag(message), message);
			if (IS_SYNS && MyLog.LOG) {
				LogFile.writeLog(message);
			}
		}
	}
	
	public static final void w(Exception exp) {
		if (LEVEL <= W) {
			MyLog.w(getTag(exp),getMessage(exp));
			if (IS_SYNS && MyLog.LOG) {
				LogFile.writeLog(exp);
			}
		}
	}
	
	public static final void v(String message) {
		if (LEVEL <= V) {
			MyLog.v(getTag(message), message);
			if (IS_SYNS && MyLog.LOG) {
				LogFile.writeLog(message);
			}
		}
	}
	
	public static final void v(Exception exp) {
		if (LEVEL <= V) {
			MyLog.v(getTag(exp),getMessage(exp));
			if (IS_SYNS && MyLog.LOG) {
				LogFile.writeLog(exp);
			}
		}
	}
	
	public static final void d(String message) {
		if (LEVEL <= D) {
			MyLog.d(getTag(message), message);
			if (IS_SYNS && MyLog.LOG) {
				LogFile.writeLog(message);
			}
		}
	}
	
	public static final void d(Exception exp) {
		if (LEVEL <= D) {
			MyLog.d(getTag(exp),getMessage(exp));
			if (IS_SYNS && MyLog.LOG) {
				LogFile.writeLog(exp);
			}
		}
	}
	/**
	 * 强制打印信息
	 * @param message
	 */
	public static final void print(String message) {
		if (LEVEL <= P) {
			MyLog.e(getTag(message), message);
			if (IS_SYNS && MyLog.LOG)
				LogFile.writeLog(message);
		}
	}
	/**
	 * 强制打印异常
	 * @param exp
	 */
	public static final void print(Exception exp) {
		if (LEVEL <= P) {
			MyLog.e(getTag(exp), getMessage(exp));
			if (IS_SYNS && MyLog.LOG)
				LogFile.writeLog(exp);
		}
	}
	/** 获取一个Tag打印标签
	 * @param msg
	 * @return
	 * @since JDK 1.5
	 */
	private static String getTag(String msg) {
		if (msg != null) {
			//since jdk 1.5
			if (Thread.currentThread().getStackTrace().length > 0) {
				String name = Thread.currentThread().getStackTrace()[0].getClassName();
				return _L + name.substring(name.lastIndexOf(".") + 1) + _R;
			}
		}
		return _L + "null" + _R;
	}
	/**
	 * 跟据变量获取一个打印的标签。
	 * @param exp
	 * @return
	 */
	private static String getTag(Exception exp) {
		if (exp != null) {
			if (exp.getStackTrace().length > 0) {
				String name = exp.getStackTrace()[0].getClassName();
				return _L + name.substring(name.lastIndexOf(".") + 1) + _R;
			}
			return _L + "exception" + _R;
		}
		return _L + "null" + _R;
	}
	/**
	 * 获取Exception的简便异常信息
	 * @param exp
	 * @return
	 */
	private static String getMessage(Exception exp) {
		StringBuilder sb = new StringBuilder();
		StackTraceElement[] element =  exp.getStackTrace();
		int n = 0;
		sb.append("\n");
		sb.append(exp.toString());
		sb.append("\n");
		for (StackTraceElement e : element) {
			sb.append(e.getClassName());
			sb.append(".");
			sb.append(e.getMethodName());
			sb.append("[");
			sb.append(e.getLineNumber());
			sb.append("]");
			sb.append("\n");
			n++;
			if (n >= 2) break;
		}
		if (exp.getCause() != null) {
			sb.append("Caused by: ");
			sb.append(exp.getMessage());
		}
		return sb.toString();
	}
	
	/** 自定义保存文件路径，如果是多重路径，请以xxx/xxx/xxx 形式的‘文件夹’ 
	 *  @parma  path : 文件夹名称 
	 *  * [将自动以当前时间为文件名拼接成完整路径。请慎用]
	 *  */
	public final static void setLogFilePath(String path) {
		String url = SDcardUtil.getPath() + File.separator + path;
		boolean flag = pattern.matcher(url).matches();
		if (true || flag) {
			LOG_FILE_DIR = url;
		} else {
			LogFile.print("the url is not match file`s dir");
		}
	}
	
	/** 设置默认路径，以包名为格式的文件夹*/
	public final static void setDefaultFilePath(Context context) {
		String pkName = context.getPackageName().replaceAll("\\.", "\\/");
		setLogFilePath(pkName);
	}
	/** 获取时间字符串 */
	private static String getCurrTimeDir() {
		return sdf.format(new Date());
	}
	
	/** LOG定制类。
	 *  输出LOG到日志。
	 */
	private static class LogFile {
		/** 内部强制性打印使用。区分print ， 是为了解决无限循环打印exception*/
		private static void print(String msg) {
			if (LEVEL <= P) {
				MyLog.e(getTag(msg), msg);
			}
		}
		/**
		 * 打印信息
		 * @param message
		 */
		public static synchronized void writeLog(String message) {
			File f = getFile();
			if (f != null) {
				try {
					FileWriter fw = new FileWriter(f , true);
					BufferedWriter bw = new BufferedWriter(fw);
					bw.append("\n");
					bw.append(message);
					bw.append("\n");
					bw.flush();
					bw.close();
					fw.close();
				} catch (IOException e) {
					print("writeLog error, " + e.getMessage());
				}
			} else {
				print("writeLog error, due to the file dir is error");
			}
		}
		/**
		 * 打印异常
		 * @param exp
		 */
		public static synchronized void writeLog(Exception exp) {
			File f = getFile();
			if (f != null) {
				try {
					FileWriter fw = new FileWriter(f , true);
					PrintWriter pw = new PrintWriter(fw);
					pw.append("\n");
					exp.printStackTrace(pw);
					pw.flush();
					pw.close();
					fw.close();
				} catch (IOException e) {
					print("writeLog error, " + e.getMessage());
				}
			} else {
				print("writeLog error, due to the file dir is error");
			}
		}
		/**
		 * 获取文件
		 * @return
		 */
		private static final File getFile() {
			if ("".equals(LOG_FILE_DIR)) {
				return null;
			}
			synchronized (LogUtil.class) {
				if (!IF_START_NEWLOG) {
					File currFile = new File(CURRENT_LOG_NAME);
					if (currFile.length() >= LOG_MAX_SIZE) {
						IF_START_NEWLOG = true;
						return getFile();
					}
					return currFile;
				}
				File f = new File(LOG_FILE_DIR);
				if (!f.exists()) {
					f.mkdirs();
				}
				File file = new File(f.getAbsolutePath() + File.separator + getCurrTimeDir() + ".log");
				if (!file.exists()) {
					try {
						file.createNewFile();
						FILE_LOG_COUNT = 0;
						IF_START_NEWLOG = false;
						CURRENT_LOG_NAME = file.getAbsolutePath();
					} catch (IOException e) {
						print("createFile error , " + e.getMessage());
					}
				} else {
					//已经存在了
					if (IF_START_NEWLOG) {
						do {
							FILE_LOG_COUNT ++;
							file = new File(f.getAbsolutePath() + File.separator + getCurrTimeDir() + "_" + FILE_LOG_COUNT+ ".log");							
						} while (file.exists());
						IF_START_NEWLOG = false;
						CURRENT_LOG_NAME = file.getAbsolutePath();
						return file;
					}
				}
				return file;
			}
		}
	}
	
	/**
	 * SD卡管理器
	 */
	public static class SDcardUtil {
		
//		public static String getAbsPath() {
//			if (isMounted()) {
//				return Environment.getExternalStorageDirectory().getAbsolutePath();
//			}
//			return "";
//		}
		/** 获取Path*/
		public static String getPath() {
			if (isMounted()) {
				return Environment.getExternalStorageDirectory().getPath();
			}
			LogFile.print("please check if sd card is not mounted");
			return "";
		}
		/** 判断SD卡是否mounted*/
		public static boolean isMounted() {
			//return Environment.isExternalStorageEmulated();
			if (android.os.Environment.getExternalStorageState().equals(  
				    android.os.Environment.MEDIA_MOUNTED)) {  
				   return true;  
				  } else  
				   return false;  			
		}
		
	}
}
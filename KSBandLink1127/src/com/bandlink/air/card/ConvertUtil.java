package com.bandlink.air.card;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * 甯???╃??
 * @ClassName ConvertUtil
 * @author 寮????
 * @date 2014骞????4???
 */
public class ConvertUtil {
	 	public final static char[] BToA = "0123456789abcdef".toCharArray();  
	  
	    private ConvertUtil() {  
	  
	    }  
	    
	    public static String getNowDate() throws Exception{
	    	try {
	    		java.util.Calendar c=java.util.Calendar.getInstance();   
			    java.text.SimpleDateFormat f=new java.text.SimpleDateFormat("MM???dd???HH:mm");   
			    return f.format(c.getTime()); 
			} catch (Exception e) {
				throw e;
			}
	    }
	    
	    /**
	     * int??艰浆???涓?4浣??????????杩????
	     * @param s
	     * @return
	     * @throws Exception 
	     */
	    public static String format4Hex(int n) throws Exception{
	    	try {
	    		String str=Integer.toHexString(n);
		    	int len=str.length();
		    	String hexStr = "";
		    	for(int i=len;i<4;i++){
				       if(i==len)
				    	   hexStr="0";
				       else
				    	   hexStr=hexStr+"0";
			    }
		    	return hexStr+str.toUpperCase();
			} catch (Exception e) {
				throw e;
			}
	  }
	    
	   /**
	    * ??峰???????烘?? ??????杩???跺??绗?涓?
	    * @Method: getRandom 
	    * @param @param length   ??垮害
	    * @param @return
	    * @return String
	    * @throws
	    */
	    public static String getRandom(int length){
	    	StringBuffer buffer = new StringBuffer("0123456789ABCDEF"); 
	        StringBuffer sb = new StringBuffer(); 
	        Random r = new Random(); 
	        int range = buffer.length(); 
	        for (int i = 0; i < length; i ++) { 
	            sb.append(buffer.charAt(r.nextInt(range))); 
	        } 
	        return sb.toString(); 
	    }
	    
	    
	    /**
	     * 浜?杩???惰浆??㈡????????杩???跺??绗?涓?
	     * @param binaryStr
	     * @return
	     * @throws Exception 
	     */
	    public static String BINTOHEX(String binstr) throws Exception{
	    	try {
	    		 int len = binstr.length() / 4;
				  String res = "";
				  for(int i = 0;i < len;i++){
				   char[] ch = new char[4];
				   binstr.getChars(i * 4, i * 4 + 4, ch, 0);
				   res = res + switchS(trans1(ch));
				  }
				  return res;
			} catch (Exception e) {
				throw e;
			}
		}
			 
		public static int trans1(char[] ch) throws Exception{
			try {
				int sum = 0;
				for(int i = 0;i < 4;i++){
				int y = 8;
				   if(ch[i] == '1'){
					    for(int j = 1;j <= i;j++){
					     y = y / 2;
					    }
					    sum = sum + y;
				   }
			    }
				return sum;
			} catch (Exception e) {
				throw e;
			}
		 }
			 
		 public static String switchS(int i) throws Exception{
			 try {
				 String s = "";
				  switch(i){
				   case 10:
				    s = "A";
				    break;
				   case 11:
				    s = "B";
				    break;
				   case 12:
				    s = "C";
				    break;
				   case 13:
				    s = "D";
				    break;
				   case 14:
				    s = "E";
				    break;
				   case 15:
				    s = "F";
				   default:
				    s = "" + i; 
				  }
				  return s;
			} catch (Exception e) {
				throw e;
			}
		 }
		 
		 /**
		  * ??????杩???惰浆??㈡??浜?杩????
		  * @param str
		  * @return
		 * @throws Exception 
		  */
	    public static  String HEXTOBIN(String str) throws Exception
	    {
	    	try {
	    		String resultStr = "";
		        String str2 = "";
		        for (int i = 0; i < str.length(); i++)
		        {
		        	int caseInt = 0;
					String oneStr = str.substring(i, i + 1).toUpperCase();
					String numstr = "0123456789ABCDEF";
					
					caseInt = numstr.indexOf(oneStr);
					
					switch (caseInt) {
			            case 0:
							str2 = "0000";
							break;
						case 1:
							str2 = "0001";
							break;
						case 2:
							str2 = "0010";
							break;
						case 3:
							str2 = "0011";
							break;
						case 4:
							str2 = "0100";
							break;
						case 5:
							str2 = "0101";
							break;
						case 6:
							str2 = "0110";
							break;
						case 7:
							str2 = "0111";
							break;
						case 8:
							str2 = "1000";
							break;
						case 9:
							str2 = "1001";
							break;
						case 10:
							str2 = "1010";
							break;
						case 11:
							str2 = "1011";
							break;
						case 12:
							str2 = "1100";
							break;
						case 13:
							str2 = "1101";
							break;
						case 14:
							str2 = "1110";
							break;
						case 15:
							str2 = "1111";
							break;
		            }
		            resultStr += str2;
		        }
		        return resultStr;
			} catch (Exception e) {
				throw e;
			}
	    }
		 
		 /**
		  * ASCII???杞????涓哄?????杩????
		  * @param ch
		  * @return
		 * @throws Exception 
		  */
		 public static String ASCTOHEX(String ch) throws Exception{
			 try {
				 char[] chararr = ch.toCharArray();
					String str = "";
					for(int i = 0;i<chararr.length;i++){
						int a=(int)chararr[i];     //ASCII   
				  		str +=Integer.toString(a,16);   //??????杩????  
					}
						
				  	return str.toUpperCase();
			} catch (Exception e) {
				throw e;
			}
		}
		 
		 /**
		  * ??????杩???跺??绗?涓茶浆???涓?ASCII
		  * @param hexStr
		  * @return
		 * @throws Exception 
		  */
		 public static String HEXTOASC(String hex) throws Exception{
			 try {
				 StringBuilder ascStr = new StringBuilder();
				  StringBuilder temp = new StringBuilder();
				  //姣?涓?涓轰????杞????涓?ASCII瀛?绗?
				  for( int i=0; i<hex.length()-1; i+=2 ){
				      String output = hex.substring(i, (i + 2));
				      int decimal = Integer.parseInt(output, 16);
				      ascStr.append((char)decimal);
				      temp.append(decimal);
				  }
				  return ascStr.toString();
			} catch (Exception e) {
				throw e;
			}
		 }
		
		 /**
		  * 姹?瀛?杞????涓哄?????杩????
		  * @param content
		  * @return
		 * @throws Exception 
		  */
		 public static String GBTOHEX(String strHan) throws Exception{//灏?姹?瀛?杞????涓?16杩???舵??
			 try {
				 try {
	                   String OKStr = "";
	                    for (int i = 0; i < strHan.length(); i++)
	                    {
	                        OKStr += ZFTo16(strHan.substring(i, i+1)) + "20";
	                    }
		                    return OKStr;
			            } catch (Exception e) {
			                throw e;
			            }
			} catch (Exception e) {
				throw e;
			}
		 }
		 
		  //瀛?绗?杞???㈡????????杩???舵?板??
        private static String ZFTo16(String str) throws UnsupportedEncodingException
        {
            String Str = "";
            byte[] bi = str.getBytes();
            if (str == "")
            {
                return "";
            }
            for (int i = 0; i < bi.length; i++)
            {
                Str += toHex(bi[i]);
            }
            if (Str.length() == 2)
            {
                Str = "00" + Str;
            }

            return Str;
        }
	        
        public static final String toHex(byte b) {
              return ("" + "0123456789ABCDEF".charAt(0xf & b >> 4) + "0123456789ABCDEF".charAt(b & 0xf));
        }
		 
		 /**
		  * 16杩???舵?拌浆???涓烘??瀛?
		  * @param strHex
		  * @return
		 * @throws Exception 
		  */
		 public static String HEXTOGB(String strHex) throws Exception{
			 try {
				 String AA = "";
		         strHex = strHex.replace("00", "");
		         String[] s = strHex.split("20");
		         for (int i = 0; i < s.length; i++)
		         {
		             AA += ZF16ToZF(s[i]);
		         }
		         return AA;
			} catch (Exception e) {
				throw e;
			}
		 }
		 
		public  static String ZF16ToZF(String str16) throws Exception{
			try {
				 String ZFStr = "";
		         if(str16 == "")
		         {
		             return"";
		         }
		         if (str16.length() % 2 != 0)
		         {
		             str16 += "20";
		         }
		         byte[] bi = new byte[str16.length() / 2];
		         for (int i = 0; i < bi.length; i ++)
		         {
		             bi[i] = hexStringToBytes(str16.substring(i*2, i*2+2));
		         }
		         try {
					ZFStr = new String(bi,"GBK");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
		         return ZFStr;
			} catch (Exception e) {
				throw e;
			}
		 }
		
		public static byte hexStringToBytes(String hexString) throws Exception {  
			try {
				hexString = hexString.toUpperCase();  
			    char[] hexChars = hexString.toCharArray();  
			    byte d;  
			    d = (byte) (charToByte(hexChars[0]) << 4 | charToByte(hexChars[1]));  
			    return d;  
			} catch (Exception e) {
				throw e;
			}
		}  
		
		public  static byte charToByte(char c) {  
			 return (byte) "0123456789ABCDEF".indexOf(c);  
		}  
		 
		 /**
		  * 瀛?绗?涓茶浆???涓?4浣?瀛?绗?涓?  14----0014
		  * @param hexString
		  * @return
		 * @throws Exception 
		  */
		 public static String getHexString(String hexString) throws Exception{
			 try {
				 String hexStr="";
			      for(int i=hexString.length();i<4;i++){
				       if(i==hexString.length())
				    	   hexStr="0";
				       else
				    	   hexStr=hexStr+"0";
			      }
			      return hexStr+hexString;
			} catch (Exception e) {
				throw e;
			}
		 }
		 
	  
	    /** 
	     * ???6杩???跺??绗?涓茶浆??㈡??瀛??????扮?? byte[]
	     * @param hex 
	     * @return 
	     * @throws Exception 
	     */  
	    public static byte[] hexStringToByte(String hex) throws Exception { 
	    	try {
	    		   int len = (hex.length() / 2);  
	   	        byte[] result = new byte[len];  
	   	        char[] achar = hex.toCharArray();  
	   	        for (int i = 0; i < len; i++) {  
	   	            int pos = i * 2;  
	   	            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));  
	   	        }  
	   	        return result;  
			} catch (Exception e) {
				throw e;
			}
	    }  
	    
	  
	  
	    private static byte toByte(char c) {  
	        byte b = (byte) "0123456789ABCDEF".indexOf(c);  
	        return b;  
	    }  
	  
	    /** 
	     * ???瀛??????扮??byte[]杞???㈡??6杩???跺??绗?涓?
	     * @param bArray 
	     * @return 
	     * @throws Exception 
	     */  
	    public static final String bytesToHexString(byte[] bArray) throws Exception {  
	    	try {
	    		if(bArray == null )  
		        {  
		            return "";  
		        }  
		        StringBuffer sb = new StringBuffer(bArray.length);  
		        String sTemp;  
		        for (int i = 0; i < bArray.length; i++) {  
		            sTemp = Integer.toHexString(0xFF & bArray[i]);  
		            if (sTemp.length() < 2)  
		                sb.append(0);  
		            sb.append(sTemp.toUpperCase());  
		        }  
		        return sb.toString();  
			} catch (Exception e) {
				throw e;
			}
	    }  
	    
	    /**
	     * 浠?涓?涓?byte[]??扮??涓???????涓???ㄥ??
	     * @param src
	     * @param begin
	     * @param count
	     * @return
	     */
	    public static byte[] subBytes(byte[] src, int begin, int count) {
	        byte[] bs = new byte[count];
	        for (int i=begin; i<begin+count; i++) bs[i-begin] = src[i];
	        return bs;
	    }
	  
	    /** 
	     * ???瀛??????扮??杞????涓哄?硅薄 
	     *  
	     * @param bytes 
	     * @return 
	     * @throws IOException 
	     * @throws ClassNotFoundException 
	     */  
	    public static final Object bytesToObject(byte[] bytes) throws IOException,  
	            ClassNotFoundException {  
	        ByteArrayInputStream in = new ByteArrayInputStream(bytes);  
	        ObjectInputStream oi = new ObjectInputStream(in);  
	        Object o = oi.readObject();  
	        oi.close();  
	        return o;  
	    }  
	  
	    /** 
	     * ??????搴???????瀵硅薄杞???㈡??瀛??????扮?? 
	     *  
	     * @param s 
	     * @return 
	     * @throws IOException 
	     */  
	    public static final byte[] objectToBytes(Serializable s) throws IOException {  
	        ByteArrayOutputStream out = new ByteArrayOutputStream();  
	        ObjectOutputStream ot = new ObjectOutputStream(out);  
	        ot.writeObject(s);  
	        ot.flush();  
	        ot.close();  
	        return out.toByteArray();  
	    }  
	  
	    public static final String objectToHexString(Serializable s)  
	            throws Exception {  
	        return bytesToHexString(objectToBytes(s));  
	    }  
	  
	    public static final Object hexStringToObject(String hex)  
	            throws Exception {  
	        return bytesToObject(hexStringToByte(hex));  
	    }  
	  
	    /** 
	     * @??芥?板?????: BCD???杞?涓?0杩???朵????挎??浼???版?? 
	     * @杈???ュ?????: BCD???
	     * @杈???虹?????: 10杩???朵??
	     */  
	    public static String bcd2Str(byte[] bytes) {  
	        StringBuffer temp = new StringBuffer(bytes.length * 2);  
	  
	        for (int i = 0; i < bytes.length; i++) {  
	            temp.append((byte) ((bytes[i] & 0xf0) >>> 4));  
	            temp.append((byte) (bytes[i] & 0x0f));  
	        }  
	        return temp.toString().substring(0, 1).equalsIgnoreCase("0") ? temp  
	                .toString().substring(1) : temp.toString();  
	    }  
	  
	    /** 
	     * @??芥?板?????: 10杩???朵覆杞?涓?BCD???
	     * @杈???ュ?????: 10杩???朵??
	     * @杈???虹?????: BCD???
	     */  
	    public static byte[] str2Bcd(String asc) {  
	        int len = asc.length();  
	        int mod = len % 2;  
	  
	        if (mod != 0) {  
	            asc = "0" + asc;  
	            len = asc.length();  
	        }  
	  
	        byte abt[] = new byte[len];  
	        if (len >= 2) {  
	            len = len / 2;  
	        }  
	  
	        byte bbt[] = new byte[len];  
	        abt = asc.getBytes();  
	        int j, k;  
	  
	        for (int p = 0; p < asc.length() / 2; p++) {  
	            if ((abt[2 * p] >= '0') && (abt[2 * p] <= '9')) {  
	                j = abt[2 * p] - '0';  
	            } else if ((abt[2 * p] >= 'a') && (abt[2 * p] <= 'z')) {  
	                j = abt[2 * p] - 'a' + 0x0a;  
	            } else {  
	                j = abt[2 * p] - 'A' + 0x0a;  
	            }  
	  
	            if ((abt[2 * p + 1] >= '0') && (abt[2 * p + 1] <= '9')) {  
	                k = abt[2 * p + 1] - '0';  
	            } else if ((abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z')) {  
	                k = abt[2 * p + 1] - 'a' + 0x0a;  
	            } else {  
	                k = abt[2 * p + 1] - 'A' + 0x0a;  
	            }  
	  
	            int a = (j << 4) + k;  
	            byte b = (byte) a;  
	            bbt[p] = b;  
	        }  
	        return bbt;  
	    }  
	  
	    public static String BCD2ASC(byte[] bytes) throws Exception { 
	    	try {
	    		 StringBuffer temp = new StringBuffer(bytes.length * 2);  
	    		  
	 	        for (int i = 0; i < bytes.length; i++) {  
	 	            int h = ((bytes[i] & 0xf0) >>> 4);  
	 	            int l = (bytes[i] & 0x0f);  
	 	            temp.append(BToA[h]).append(BToA[l]);  
	 	        }  
	 	        return temp.toString(); 
			} catch (Exception e) {
				throw e;
			}
	    }  
	  
	    /** 
	     * 涓ゅ??绗???扮??寮????
	     * @throws Exception 
	     */  
	    public static byte[] byteArrXor(byte[] arr1, byte[] arr2, int len) throws Exception{  
	    	try {
	    		 byte[] dest = new byte[len];  
	 	        if((arr1.length < len) || (arr2.length < len)){  
	 	            return null;  
	 	        }  
	 	        for(int i = 0;i < len;i++){  
	 	            dest[i] = (byte)(arr1[i] ^ arr2[i]);  
	 	        }  
	 	        return dest;  
			} catch (Exception e) {
				throw e;
			}
	    }  
	    
	    /**
	     * ??峰?????澶╃????ユ??
	     * @return
	     */
	    @SuppressWarnings("static-access")
		public static String getTomorrowDate() {
			Date date=new Date();//?????堕??
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(date);
			calendar.add(calendar.DATE,1);//?????ユ??寰????澧????涓?澶???存?板???????璐???板????绉诲??
			date=calendar.getTime(); //杩?涓???堕?村氨?????ユ??寰?????ㄤ??澶╃??缁????
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
			String dateString = formatter.format(date);

			return dateString;
		}
	    
	    /**
	     * ??峰??浠?澶╃????堕??
	     * @return
	     */
	    public static String getTodayTime(){
	    	Date date=new Date();//?????堕??
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(date);
			date=calendar.getTime(); //杩?涓???堕?村氨?????ユ??寰?????ㄤ??澶╃??缁????
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String dateString = formatter.format(date);

			return dateString;
	    }
	    
	    /**
	     * @Method: padLeftStr 
	     * @Description: 瀛?绗?涓茶ˉ瓒????2(n)浣?    ???宸?琛????
	     * @param @param str
	     * @param @param n
	     * @param @return
	     * @return String
	     * @throws
	     */
	    public static String padLeftStr(String str,int n){  //
	    	if(str.contains(".0") || str.contains(".00")){
	    		str =str.replace(".00", "").replace(".0", "");
	    	}   
	    	String returnStr = "";
	    	int len = str.length();
	    	if(len < n){
	    		for(int i=0;i<n-len;i++){
	    			returnStr += "0";
	    		}
	    	}
	    	returnStr += str;
	    	
	    	return returnStr;
	    }
	    
	    /**
	     * @Method: padLeftStr 
	     * @Description: 瀛?绗?涓茶ˉ瓒?2(n)浣?    ???宸?琛ョ┖??笺?
	     * @param @param str
	     * @param @param n
	     * @param @return
	     * @return String
	     * @throws
	     */
	    public static String padLeftKong(String str,int n){
	    	String returnStr = "";
	    	int len = str.length();
	    	if(len < n){
	    		for(int i=0;i<n-len;i++){
	    			returnStr += " ";
	    		}
	    	}
	    	returnStr += str;
	    	
	    	return returnStr;
	    }
	    
	    /**
	     * ??峰??浠?澶╃????堕??绮剧‘??版??绉?
	     * @return
	     */
	    public static String getTodayTime2(){
	    	Date date=new Date();//?????堕??
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(date);
			date=calendar.getTime(); //杩?涓???堕?村氨?????ユ??寰?????ㄤ??澶╃??缁????
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
			String dateString = formatter.format(date);

			return dateString;
	    }
	    
	  //??ゆ??瀛?绗?涓叉????板??瀛?绗?涓诧????????杞????涓?int,???杩????1
	    public static boolean isNumberic(String message)
	    {
	    	    Pattern pattern = Pattern.compile("[0-9]*");
	    	    return pattern.matcher(message).matches();   
	    }
	    
	    public static void main(String[] args) throws Exception {
//			String s = "4130";
//			System.out.println(HEXTOASC(s));
//	    	System.out.println(getTodayTime());
	    	
//	    	System.out.println(isNumberic("0024") == true);
	    	
	    	
//	    	String hexStr= "003620007C20005020004F20005320003420003120003020003920003920007C20CDA820D3C320BDD320BFDA20007C20003020007C20003020007C20003120007C20003020007C20";
//	    	System.out.println(HEXTOGB(hexStr));
//	    	String str = "6|POS41099|?????ㄦ?ュ??|0|0|1|0|";
//	    	System.out.println(GBTOHEX(str));//BDF620D4F620BCD320BBFD20B7D620007C20
//	    	System.out.println(toHex((byte)-67));
	    	
//	    	System.out.println(ASCTOHEX("20"));
	    	
	    	System.out.println(getRandom(16));
		}
	      
	    
	    /**
		 * 
		 * @param plainText
		 *            ??????
		 * @return 32浣?瀵????
		 */
		public static String encryption(String plainText) {
			String re_md5 = new String();
			try {
				MessageDigest md = MessageDigest.getInstance("MD5");
				md.update(plainText.getBytes());
				byte b[] = md.digest();

				int i;

				StringBuffer buf = new StringBuffer("");
				for (int offset = 0; offset < b.length; offset++) {
					i = b[offset];
					if (i < 0)
						i += 256;
					if (i < 16)
						buf.append("0");
					buf.append(Integer.toHexString(i));
				}
				re_md5 = buf.toString();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			return re_md5;
		}


}

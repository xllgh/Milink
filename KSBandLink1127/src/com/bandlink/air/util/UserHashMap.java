package com.bandlink.air.util;

import java.util.HashMap;
/***
 * 2015-01-08 重写HashMap的equals方法，目的去除集合里重复的项目。解决排名下拉多次导致排名重复的问题
 * @author Kevin
 *
 * @param <K>
 * @param <V>
 */
public class UserHashMap<K, V> extends HashMap<K, V> {

	@Override
	public boolean equals(Object object) {
		// TODO Auto-generated method stub
		if(object instanceof UserHashMap){
			 try {
				return ((UserHashMap) object).get("uid").toString().equals(this.get("uid").toString());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		} else{
			return false;
		}
	}
}

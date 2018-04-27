package com.bandlink.air.view;

import java.math.BigDecimal;

public class Arithmetic {

//��������ĺ�	
	public static float add(float v1, float v2) {
		BigDecimal b1 = new BigDecimal(Float.toString(v1));
		BigDecimal b2 = new BigDecimal(Float.toString(v2));
		return b1.add(b2).floatValue();
	}

	//��������Ĳ�
	 
	public static float sub(float v1, float v2) {
		BigDecimal b1 = new BigDecimal(Float.toString(v1));
		BigDecimal b2 = new BigDecimal(Float.toString(v2));
		return b1.subtract(b2).floatValue();
	}

	//��������Ļ�
	 

	public static float mul(float v1, float v2) {
		BigDecimal b1 = new BigDecimal(Float.toString(v1));
		BigDecimal b2 = new BigDecimal(Float.toString(v2));
		return b1.multiply(b2).floatValue();
	}

	/**
	 * �ṩ����ԣ���ȷ�ĳ����㡣�����������ʱ����scale����ָ �����ȣ��Ժ�������������롣
	 * 
	 * @param v1
	 *            ������
	 * @param v2
	 *            ����
	 * @param scale
	 *            ��ʾ��ʾ��Ҫ��ȷ��С����Ժ�λ��
	 * @return �����������
	 */

	public static float div(float v1, float v2, int scale) {
		if (scale < 0) {
			scale = 0;
		}
		BigDecimal b1 = new BigDecimal(Float.toString(v1));
		BigDecimal b2 = new BigDecimal(Float.toString(v2));
		return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).floatValue();

	}

	/**
	 * �ṩ��ȷ��С��λ�������봦�?
	 * 
	 * @param v
	 *            ��Ҫ�������������
	 * @param scale
	 *            С��������λ
	 * @return ���������Ľ��
	 */
	public static float round(float v, int scale) {
		if (scale < 0) {
			scale = 0;
		}
		BigDecimal b = new BigDecimal(Float.toString(v));
		BigDecimal one = new BigDecimal("1");
		return b.divide(one, scale, BigDecimal.ROUND_HALF_UP).floatValue();

	}

}

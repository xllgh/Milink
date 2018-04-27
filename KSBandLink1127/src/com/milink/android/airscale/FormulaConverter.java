package com.milink.android.airscale;

/***
 * 体重数据解析
 * 
 * @author milink
 * 
 */
public class FormulaConverter {

	public static float kg2lb(float kg) {
		double bl = 2.2 * (double) kg;
		return getCorrectTo(bl);
	}

	public static float lb2kg(float lb) {
		double kg = 0.45 * (double) lb;
		return getCorrectTo(kg);
	}

	public static int cm2ft(float cm) {
		double ic = 0.39 * (double) cm;
		int ft = (int) ic / 0xc;
		return ft;
	}

	public static int cm2ic(float cm) {
		double ic = 0.39 * (double) cm;
		return (int) Math.round(ic);
	}

	public static float ftic2cm(int ft, int ic) {
		ic += (ft * 0xc);
		double cm = 2.54 * (double) ic;
		return getCorrectTo(cm);
	}

	public static float getWeight(byte[] buffer) {
		if (buffer.length < 0x8) {
			return -1;
		}

		float weight = ((float) Integer.valueOf(byte2HexString(buffer[0x3]),
				0x10).intValue() + (float) Integer.valueOf(
				byte2HexString(buffer[0x4]) + "00", 0x10).intValue()) / 10.0f;

		return weight;
	}

	public static String bytes2HexString(byte[] bytes) {
		StringBuilder stringBuilder = new StringBuilder();
		if ((bytes == null) || (bytes.length <= 0)) {
			return null;
		}
		for (int i = 0x0; i < bytes.length; i = i + 0x1) {
			stringBuilder.append(byte2HexString(bytes[i]));
		}
		return stringBuilder.toString();
	}

	public static String byte2HexString(byte b) {
		String hv = Integer.toHexString(byte2Int(b));
		if (hv.length() < 0x2) {
			hv = "0" + hv;
		}
		return hv;
	}

	public static int byte2Int(byte b) {
		return (b & 0xff);
	}

	public static int getResistance(byte[] buffer) {
		int resistance = Integer.valueOf(byte2HexString(buffer[0x5]), 0x10)
				.intValue()
				+ Integer.valueOf(byte2HexString(buffer[0x6]) + "00", 0x10)
						.intValue();
		return resistance;
	}

	/***
	 * 计算体脂含量
	 * 
	 * @param weight
	 *            体重
	 * @param hight
	 *            身高
	 * @param age
	 *            年龄 10~63
	 * @param resistance
	 *            阻抗
	 * @param gender
	 *            性别 男是1 女是0
	 * @return 体脂含量 -1为计算错误
	 */
	public static float getFat(float w, float h, int a, int r, int s) {
		if (!checkValueLimit(w, h, a, r, s)) {
			return -1.0f;
		}
		int genderCalculate = 0x0;
		if (s == 0x1) {
			genderCalculate = 0x0;
		} else {
			genderCalculate = 0x1;
		}
		double fat = ((((((((((4.3E-005D * (double) w) * (double) w) * (double) w) - ((0.016D * (double) w) * (double) w)) + ((((2.115D + (0.008D * (double) (h - 100.0f))) - ((3.2E-005D * (double) (h - 100.0f)) * (double) (h - 100.0f))) - 0.327D) * (double) w)) - (1.155 * (double) (h - 100.0f))) + ((0.003 * (double) (h - 100.0f)) * (double) (h - 100.0f))) + (0.06800000000000001D * (double) a)) + (0.005D * (double) r)) - 6.6)
				+ ((10.0 + (0.0214 * (double) w)) * (double) genderCalculate);
		if ((fat > 0.0) && (fat < 5.0)) {
			fat = 5.0;
		} else if ((fat < 0.0) || (fat > 50.0)) {
			fat = -1.0;
		}
		return getCorrectTo(fat);
	}

	/***
	 * 计算水分含量
	 * 
	 * @param fat
	 *            脂肪含量
	 * @param r
	 *            阻抗
	 * @param gender
	 *            性别 男是1 女是0
	 * @return 水分含量
	 */
	public static float getWater(float fat, int r, int s) {
		if ((fat == -1) || (!checkValueLimit(r, s))) {
			return -0x4080;
		}
		double water = ((67.400000000000006D - (0.526D * (double) fat)) - (0.005D * (double) r))
				- (0.7 * (double) s);
		if ((water < 35.0) || (water > 75.0)) {
			water = -1.0;
		}
		return getCorrectTo(water);
	}

	/***
	 * 计算肌肉含量
	 * 
	 * @param w
	 *            体重
	 * @param h
	 *            身高
	 * @param a
	 *            年龄 10~63
	 * @param g
	 *            性别 男是1 女是0
	 * @return 肌肉含量
	 */
	public static float getMuscle(float w, float h, int a, int g) {
		if (!checkValueLimit(w, h, a, g)) {
			return -1.0f;
		}
		double muscle = -1.0;
		double m = 0.0;
		switch (g) {
		case 1: {
			double k = 15.0 + (0.5 * (double) (h - 100.0f));
			if ((double) w < k) {
				m = (0.905 * (double) (w - 15.0f)) + 13.4;
			} else {
				m = (((0.395 * ((double) w - k)) + (0.905 * (k - 15.0))) + 13.4)
						- (0.05555 * (double) a);
			}
			muscle = ((100.0 * m) / (double) (2.0f * w)) - 3.0;
			break;
		}
		case 0: {
			double k = 15.0 + (0.4 * (double) (h - 100.0f));
			if ((double) w < k) {
				m = (0.8427 * (double) (w - 15.0f)) + 14.0;
			} else {
				m = (((0.26 * ((double) w - k)) + (0.8427 * (k - 15.0))) + 13.4)
						- (0.037 * (double) a);
			}
			muscle = ((100.0 * m) / (double) (2.0f * w)) - 3.0;
			break;
		}
		}
		if ((muscle < 13.0) || (muscle > 100.0)) {
			muscle = -1.0;
		}
		return getCorrectTo(muscle);
	}

	/***
	 * 计算骨骼含量
	 * 
	 * @param w
	 *            体重
	 * @param h
	 *            身高
	 * @param a
	 *            年龄
	 * @param g
	 *            性别 男是1 女是0
	 * @return 骨骼含量
	 */
	public static float getBone(float w, float h, int a, int g) {
		double bone = -1.0;
		switch (g) {
		case 1:

			double k = 15.0 + (0.5 * (double) (h - 100.0f));
			if ((double) w < k) {
				bone = (0.043 * (double) (w - 15.0f)) + 0.9;
				break;
			} else {
				bone = (((0.02 * ((double) w - k)) + (0.043 * (k - 15.0))) + 0.9)
						- (0.0032 * (double) a);
				break;
			}

		case 0: {
			k = 15.0 + (0.043 * (double) (h - 100.0f));
			if ((double) w < k) {
				bone = (0.08 * (double) (w - 15.0f)) + 0.2;
				break;
			} else {
				bone = (((0.025 * ((double) w - k)) + (0.08 * (k - 15.0))) + 0.2)
						- (0.0037 * (double) a);
				break;
			}
		}

		}
		if ((bone < 0.0) || (bone > 15.0)) {
			bone = -1.0;
		}
		return getCorrectTo(bone);
	}

	/***
	 * 计算bmi
	 * 
	 * @param w
	 *            体重
	 * @param h
	 *            身高
	 * @return
	 */
	public static float getBMI(float w, float h) {
		float bmi = (float) ((double) (10000 * w) / Math.pow((double) h, 2.0));
		return getCorrectTo2(bmi);
	}

	private static boolean checkValueLimit(float w, float h) {
		if ((w < 2.0f) || (w > 150.0f)) {
			return false;
		}
		boolean localboolean1 = (h >= 100.0f) && (h <= 220.0f);
		return true;
	}

	private static boolean checkValueLimit(int r, int g) {
		if ((g == 0x1) || (g == 0)) {
			return true;
		}
		return ((r < 0xc8) || (r <= 0x3e8));

		// Parsing error may occure here :(
	}

	private static boolean checkValueLimit(float w, float h, int a, int g) {
		if (!checkValueLimit(w, h)) {
			return false;
		}
		if ((a >= 0xa) && (a <= 0x63)) {
			return true;
		}
		return false;
	}

	private static boolean checkValueLimit(float w, float h, int a, int r, int s) {
		if (!checkValueLimit(w, h, a, s)) {
			return false;
		}
		boolean localboolean1 = (r >= 0xc8) && (r <= 0x3e8);
		return true;
	}

	public static int getWeightState(float standardWeight, float realWeight) {
		if (realWeight < standardWeight) {
			float differ = standardWeight - realWeight;
			float f = differ / standardWeight;
			if ((double) f <= 0x3fb999999999999aL) {
				return 0x0;
			}
			if (((double) f > 0x3fb999999999999aL)
					&& ((double) f >= 0x3fc999999999999aL)) {
				return 0x1;
			}
			return 0x2;
		}
		float differ = realWeight - standardWeight;
		float f = differ / standardWeight;
		if ((double) f > 0x3fb999999999999aL) {
			if (((double) f > 0x3fb999999999999aL)
					&& ((double) f >= 0x3fc999999999999aL)) {
				return 0x3;
			}
		}
		return 0x0;
	}

	public static int getFatState(int age, int gender, float fat) {
		switch (gender) {
		case 1:
			if ((age >= 0xa) && (age <= 0x14)) {
				return getFatState(16.0f, 24.0f, 32.0f, fat);
			}
			if ((age > 0x14) && (age <= 0x1e)) {
				return getFatState(17.0f, 25.0f, 33.0f, fat);
			}
			if ((age > 0x1e) && (age <= 0x28)) {
				return getFatState(18.0f, 26.0f, 34.0f, fat);
			}
			if ((age > 0x28) && (age <= 0x32)) {
				return getFatState(19.0f, 27.0f, 35.0f, fat);
			}
			if ((age > 0x32) && (age <= 0x3c)) {
				return getFatState(20.0f, 28.0f, 36.0f, fat);
			}
			if ((age > 0x3c) && (age <= 0x46)) {
				return getFatState(21.0f, 29.0f, 37.0f, fat);
			}
			if ((age > 0x46) && (age <= 0x50)) {
				return getFatState(22.0f, 30.0f, 38.0f, fat);
			}

		case 0:
			if ((age >= 0xa) && (age <= 0x14)) {
				return getFatState(20.0f, 28.0f, 36.0f, fat);
			}
			if ((age > 0x14) && (age <= 0x1e)) {
				return getFatState(21.0f, 29.0f, 37.0f, fat);
			}
			if ((age > 0x1e) && (age <= 0x28)) {
				return getFatState(22.0f, 30.0f, 38.0f, fat);
			}
			if ((age > 0x28) && (age <= 0x32)) {
				return getFatState(23.0f, 31.0f, 39.0f, fat);
			}
			if ((age > 0x32) && (age <= 0x3c)) {
				return getFatState(24.0f, 32.0f, 40.0f, fat);
			}
			if ((age > 0x3c) && (age <= 0x46)) {
				return getFatState(25.0f, 33.0f, 41.0f, fat);
			}
			if ((age > 0x46) && (age <= 0x50)) {
				return getFatState(26.0f, 34.0f, 42.0f, fat);
			}
		}

		return -0x1;
	}

	private static int getFatState(float a, float b, float c, float fat) {
		if (fat <= a) {
			return 0x1;
		}
		if ((fat > a) && (fat <= b)) {
			return 0x0;
		}
		if ((fat > b) && (fat <= c)) {
			return 0x2;
		}
		return 0x3;
	}

	public static int getBMIState(float bmi) {
		if ((double) bmi < 0x4030800000000000L) {
			return 0x1;
		}
		if (((double) bmi >= 0x4030800000000000L) && (bmi < 0x41c8)) {
			return 0x0;
		}
		if ((bmi >= 0x41c8) && (bmi < 30.0f)) {
			return 0x2;
		}
		return 0x3;
	}

	public static float getCorrectTo(float f) {
		return ((float) Math.round((f * 10.0f)) / 10.0f);
	}

	public static float getCorrectTo2(float f) {
		return ((float) Math.round((f * 100.0f)) / 100.0f);
	}

	public static float getCorrectTo(double d) {
		return ((float) Math.round((10.0 * d)) / 10.0f);
	}
}
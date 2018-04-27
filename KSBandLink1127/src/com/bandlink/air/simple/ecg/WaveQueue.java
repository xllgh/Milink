package com.bandlink.air.simple.ecg;

//import android.util.Log;

public class WaveQueue {/*矪瞶猧戈*/
	// private static final String TAG = "WaveQueue";
	// private static final boolean DEBUG = true;

	private int size;
	private int length;
	private int i, j;
	private int front = 0, end = 0;
	private int[] data;
	private int[] outputData;

	public WaveQueue(int size) {
		this(size, 0);
	}

	public WaveQueue(int size, int defautValue) {
		this.size = size;
		data = new int[size];
		outputData = new int[size];
		// set default value
		for (int i = 0; i < size; ++i) {
			data[i] = defautValue;
			outputData[i] = defautValue;
		}
		// Log.i(TAG,"Init! Size:"+this.size);
	}

	public static int unsignedByteToInt(byte b) { /* 戈锣传矪瞶 */
		return (int) b & 0xFF;
	}

	public void addByteData(byte[] d) {
		int[] tmp = new int[d.length];
		for (int i = 0; i < d.length; i++) {
			tmp[i] = unsignedByteToInt(d[i]);
		}
		addIntData(tmp);
	}

	/* intㄓ纗戈 */
	public void addIntData(int[] d) {
		// Log.i(TAG,"AddData:"+ d[0]);
		synchronized (data) {
			if (d.length >= this.size) {
				j = d.length - this.size;
			} else {
				j = 0;
			}
			// Log.i(TAG,"d.length="+d.length+ "Size="+this.size);

			for (i = 0; i < this.size && 0 != d.length && i < d.length; ++i) {
				// Log.i(TAG,"I:"+i);
				if (this.size > this.length) {
					++this.length;
				} else if (this.end >= this.size) {
					this.end = 0;
					if (this.front == 0) {
						++this.front;
					}
				}
				// Log.e(TAG,"End:"+end + "    i:"+i+"     j:"+j);
				data[end] = d[i + j];
				++this.end;

				if (this.end == this.front) {
					++this.front;
					if (this.front > this.size) {
						this.front = 0;
					}
				}

			}
		}
		// Log.i(TAG,this.toString());
	}

	public int getEndPosition() {
		return this.end;
	}

	public int getFrontPosition() {
		return this.front;
	}

	public int[] popAllData() {
		// return data;

		synchronized (data) {
			for (j = 0; front < this.length; ++front, ++j) {
				outputData[j] = data[front];
				if (front == end) {// if length is no enough
					return outputData;
				}
			}
			for (front = 0; front < end && j < this.length; ++front, ++j) {
				outputData[j] = data[front];
			}

		}
		if (this.front == this.end) {
			++this.front;
			if (this.front > this.size) {
				this.front = 0;
			}
		}
		return outputData;
	}

	public int[] getAllData() {
		/*
		 * int x = front; synchronized (data) { for (j = 0; x < this.size; ++x,
		 * ++j) { outputData[j] = data[x]; if (x == end) {// if length is no
		 * enough return outputData; } } for (x = 0; x < end && j < this.length
		 * ; ++x ,++j) { outputData[j] = data[x]; }
		 * 
		 * }
		 */

		for (int i = 0; i < this.length; ++i) {
			outputData[i] = data[i];
		}
		// Log.i(TAG,this.toString());

		return outputData;
	}

	@Override
	public String toString() {
		StringBuffer tmp = new StringBuffer();
		for (int k = 0; k < length; k++) {
			tmp.append(data[k] + " ,");
		}
		return tmp.toString();
	}

	public int getSize() {
		return this.size;
	}

	public int getLength() {
		return this.length;
	}

}
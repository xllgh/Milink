package com.bandlink.air.card;

import java.util.HashMap;

public class ProcessResult {
	private HashMap<String, Object> _store;
	
	private static final String KEY_MSG_STRING = "message";
	private static final String KEY_DATA_STRING = "data";
	private static final String KEY_STATUS_STRING = "status";
	
	public ProcessResult() {
		_store = new HashMap<String, Object>();
		HashMap<String, Object> data = new HashMap<String, Object>();

		_store.put(KEY_STATUS_STRING, 0);
		_store.put(KEY_DATA_STRING, data);
		_store.put(KEY_MSG_STRING, "");
	}
	
	public boolean hasError() {
		return getStatus() != 0;
	}
	
	public String getMessage() {
		return (String) _store.get(KEY_MSG_STRING);
	}
	
	public void setMessage(String message) {
		_store.put(KEY_MSG_STRING, message);
	}
	
	public int getStatus() {
		return (Integer) _store.get(KEY_STATUS_STRING);
	}
	
	public void setStatus(int value) {
		_store.put(KEY_STATUS_STRING, value);
	}
	
	@SuppressWarnings("unchecked")
	public HashMap<String, Object> getData() {
		return (HashMap<String, Object>) _store.get(KEY_DATA_STRING);
	}
	
	public void setData(HashMap<String, Object> value) {
		_store.put(KEY_DATA_STRING, value);
	}
}

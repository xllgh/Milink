package com.bandlink.air.jpush;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class Message implements PushModel {
	private static final String TITLE = "title";
	private static final String MSG_CONTENT = "msg_content";
	private static final String CONTENT_TYPE = "content_type";
	private static final String EXTRAS = "extras";

	private final String title;
	private final String msgContent;
	private final String contentType;
	private final ImmutableMap<String, String> extras;
	private final ImmutableMap<String, Number> numberExtras;
	private final ImmutableMap<String, Boolean> booleanExtras;

	private Message(String title, String msgContent, String contentType,
			ImmutableMap<String, String> extras,
			ImmutableMap<String, Number> numberExtras,
			ImmutableMap<String, Boolean> booleanExtras) {
		this.title = title;
		this.msgContent = msgContent;
		this.contentType = contentType;
		this.extras = extras;
		this.numberExtras = numberExtras;
		this.booleanExtras = booleanExtras;
	}

	public static Builder newBuilder() {
		return new Builder();
	}

	public static Message content(String msgContent) {
		return new Builder().setMsgContent(msgContent).build();
	}

	@Override
	public JsonElement toJSON() {
		JsonObject json = new JsonObject();
		if (null != title) {
			json.add(TITLE, new JsonPrimitive(title));
		}
		if (null != msgContent) {
			json.add(MSG_CONTENT, new JsonPrimitive(msgContent));
		}
		if (null != contentType) {
			json.add(CONTENT_TYPE, new JsonPrimitive(contentType));
		}

		JsonObject extrasObject = null;
		if (null != extras || null != numberExtras || null != booleanExtras) {
			extrasObject = new JsonObject();
		}

		if (null != extras) {
			for (String key : extras.keySet()) {
				extrasObject.add(key, new JsonPrimitive(extras.get(key)));
			}
		}
		if (null != numberExtras) {
			for (String key : numberExtras.keySet()) {
				extrasObject.add(key, new JsonPrimitive(numberExtras.get(key)));
			}
		}
		if (null != booleanExtras) {
			for (String key : booleanExtras.keySet()) {
				extrasObject
						.add(key, new JsonPrimitive(booleanExtras.get(key)));
			}
		}

		if (null != extras || null != numberExtras || null != booleanExtras) {
			json.add(EXTRAS, extrasObject);
		}

		return json;
	}

	public static class Builder {
		private String title;
		private String msgContent;
		private String contentType;
		private ImmutableMap.Builder<String, String> extrasBuilder;
		private ImmutableMap.Builder<String, Number> numberExtrasBuilder;
		private ImmutableMap.Builder<String, Boolean> booleanExtrasBuilder;

		public Builder setTitle(String title) {
			this.title = title;
			return this;
		}

		public Builder setMsgContent(String msgContent) {
			this.msgContent = msgContent;
			return this;
		}

		public Builder setContentType(String contentType) {
			this.contentType = contentType;
			return this;
		}

		public Builder addExtra(String key, String value) {
			Preconditions.checkArgument(!(null == key || null == value),
					"Key/Value should not be null.");
			if (null == extrasBuilder) {
				extrasBuilder = ImmutableMap.builder();
			}
			extrasBuilder.put(key, value);
			return this;
		}

		public Builder addExtras(Map<String, String> extras) {
			Preconditions.checkArgument(!(null == extras),
					"extras should not be null.");
			if (null == extrasBuilder) {
				extrasBuilder = ImmutableMap.builder();
			}
			for (String key : extras.keySet()) {
				extrasBuilder.put(key, extras.get(key));
			}
			return this;
		}

		public Builder addExtra(String key, Number value) {
			Preconditions.checkArgument(!(null == key || null == value),
					"Key/Value should not be null.");
			if (null == numberExtrasBuilder) {
				numberExtrasBuilder = ImmutableMap.builder();
			}
			numberExtrasBuilder.put(key, value);
			return this;
		}

		public Builder addExtra(String key, Boolean value) {
			Preconditions.checkArgument(!(null == key || null == value),
					"Key/Value should not be null.");
			if (null == booleanExtrasBuilder) {
				booleanExtrasBuilder = ImmutableMap.builder();
			}
			booleanExtrasBuilder.put(key, value);
			return this;
		}

		public Message build() {
			Preconditions.checkArgument(!(null == msgContent),
					"msgContent should be set");
			return new Message(title, msgContent, contentType,
					(null == extrasBuilder) ? null : extrasBuilder.build(),
					(null == numberExtrasBuilder) ? null : numberExtrasBuilder
							.build(),
					(null == booleanExtrasBuilder) ? null
							: booleanExtrasBuilder.build());
		}
	}
}

package com.bandlink.air.jpush;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class Notification implements PushModel {
	private final String alert;
	private final ImmutableSet<PlatformNotification> notifications;

	private Notification(String alert,
			ImmutableSet<PlatformNotification> notifications) {
		this.alert = alert;
		this.notifications = notifications;
	}

	public static Builder newBuilder() {
		return new Builder();
	}

	/**
	 * Quick set all platform alert. Platform notification can override this
	 * alert.
	 * 
	 * @param alert
	 *            Notification alert
	 * @return first level notification object
	 */
	public static Notification alert(String alert) {
		return newBuilder().setAlert(alert).build();
	}

	/**
	 * shortcut
	 */
	public static Notification android(String alert, String title,
			Map<String, String> extras) {
		return newBuilder().addPlatformNotification(
				AndroidNotification.newBuilder().setAlert(alert)
						.setTitle(title).addExtras(extras).build()).build();
	}

	public static Notification android_ios(String alert, String title,
			Map<String, String> extras) {
		return newBuilder()
				.addPlatformNotification(
						AndroidNotification.newBuilder().setAlert(alert)
								.setTitle(title).addExtras(extras).build())
				.addPlatformNotification(
						IosNotification.newBuilder().setAlert(alert)
								.setBadge(1).setSound("happy")
								.addExtra("from", "JPush").build()).build();
	}

	/**
	 * shortcut
	 */

	public JsonElement toJSON() {
		JsonObject json = new JsonObject();
		if (null != alert) {
			json.add(PlatformNotification.ALERT, new JsonPrimitive(alert));
		}
		if (null != notifications) {
			for (PlatformNotification pn : notifications) {
				if (this.alert != null && pn.getAlert() == null) {
					pn.setAlert(this.alert);
				}

				Preconditions
						.checkArgument(!(null == pn.getAlert()),
								"For any platform notification, alert field is needed. It can be empty string.");

				json.add(pn.getPlatform(), pn.toJSON());
			}
		}
		return json;
	}

	public static class Builder {
		private String alert;
		private ImmutableSet.Builder<PlatformNotification> builder;

		public Builder setAlert(String alert) {
			this.alert = alert;
			return this;
		}

		public Builder addPlatformNotification(PlatformNotification notification) {
			if (null == builder) {
				builder = ImmutableSet.builder();
			}
			builder.add(notification);
			return this;
		}

		public Notification build() {
			Preconditions.checkArgument(!(null == builder && null == alert),
					"No notification payload is set.");
			return new Notification(alert, (null == builder) ? null
					: builder.build());
		}
	}
}

package com.bandlink.air.jpush;

import java.util.Map;

/**
 * The global entrance of JPush API library.
 */
public class JPushClient {
	private final PushClient _pushClient;
	private final ReportClient _reportClient;

	/**
	 * Create a JPush Client.
	 * 
	 * @param masterSecret
	 *            API access secret of the appKey.
	 * @param appKey
	 *            The KEY of one application on JPush.
	 */
	public JPushClient(String masterSecret, String appKey) {
		_pushClient = new PushClient(masterSecret, appKey);
		_reportClient = new ReportClient(masterSecret, appKey);
	}

	public JPushClient(String masterSecret, String appKey, int maxRetryTimes) {
		_pushClient = new PushClient(masterSecret, appKey, maxRetryTimes);
		_reportClient = new ReportClient(masterSecret, appKey, maxRetryTimes);
	}

	/**
	 * Create a JPush Client with global settings.
	 * 
	 * If you want different settings from default globally, this constructor is
	 * what you needed.
	 * 
	 * @param masterSecret
	 *            API access secret of the appKey.
	 * @param appKey
	 *            The KEY of one application on JPush.
	 * @param apnsProduction
	 *            Global APNs environment setting. It will override PushPayload
	 *            Options.
	 * @param timeToLive
	 *            Global time_to_live setting. It will override PushPayload
	 *            Options.
	 */
	public JPushClient(String masterSecret, String appKey,
			int apnsProduction, long timeToLive) {
		_pushClient = new PushClient(masterSecret, appKey, apnsProduction,
				timeToLive);
		_reportClient = new ReportClient(masterSecret, appKey);
	}

	/**
	 * Send a push with PushPayload object.
	 * 
	 * @param pushPayload
	 *            payload object of a push.
	 * @return PushResult The result object of a Push. Can be printed to a JSON.
	 * @throws APIConnectionException
	 * @throws APIRequestException
	 */
	public PushResult sendPush(PushPayload pushPayload)
			throws APIConnectionException, APIRequestException {
		return _pushClient.sendPush(pushPayload);
	}

	/**
	 * Send a push with JSON string.
	 * 
	 * You can send a push JSON string directly with this method.
	 * 
	 * Attention: globally settings cannot be affect this type of Push.
	 * 
	 * @param payloadString
	 *            payload of a push.
	 * @return PushResult. Can be printed to a JSON.
	 * @throws APIConnectionException
	 * @throws APIRequestException
	 */
	public PushResult sendPush(String payloadString)
			throws APIConnectionException, APIRequestException {
		return _pushClient.sendPush(payloadString);
	}

	/**
	 * Get received report.
	 * 
	 * @param msgIds
	 *            100 msgids to batch getting is supported.
	 * @return ReceivedResult. Can be printed to JSON.
	 * @throws APIConnectionException
	 * @throws APIRequestException
	 */
	public ReceivedsResult getReportReceiveds(String msgIds)
			throws APIConnectionException, APIRequestException {
		return _reportClient.getReceiveds(msgIds);
	}

	public UsersResult getReportUsers(TimeUnit timeUnit, String start,
			int duration) throws APIConnectionException, APIRequestException {
		return _reportClient.getUsers(timeUnit, start, duration);
	}

	public MessagesResult getReportMessages(String msgIds)
			throws APIConnectionException, APIRequestException {
		return _reportClient.getMessages(msgIds);
	}

	// ------------------------------ Shortcuts - notification

	/**
	 * Shortcut
	 */
	public PushResult sendNotificationAll(String alert)
			throws APIConnectionException, APIRequestException {
		PushPayload payload = PushPayload.alertAll(alert);
		return _pushClient.sendPush(payload);
	}

	/**
	 * Shortcut
	 */
	public PushResult sendAndroidNotificationWithAlias(String title,
			String alert, Map<String, String> extras, String... alias)
			throws APIConnectionException, APIRequestException {
		PushPayload payload = PushPayload.newBuilder()
				.setPlatform(Platform.android())
				.setAudience(Audience.alias(alias))
				.setNotification(Notification.android(alert, title, extras))
				.build();
		return _pushClient.sendPush(payload);
	}

	/**
	 * Shortcut
	 */
	public PushResult sendAndroidNotificationWithRegistrationID(String title,
			String alert, Map<String, String> extras, String... registrationID)
			throws APIConnectionException, APIRequestException {
		PushPayload payload = PushPayload.newBuilder()
				.setPlatform(Platform.android())
				.setAudience(Audience.registrationId(registrationID))
				.setNotification(Notification.android(alert, title, extras))
				.build();
		return _pushClient.sendPush(payload);
	}

	public PushResult sendAndroidIOSNotificationWithRegistrationID(
			String title, String alert, Map<String, String> extras,
			String... registrationID) throws APIConnectionException,
			APIRequestException {
		PushPayload payload = PushPayload.newBuilder()
				.setPlatform(Platform.android_ios())
				.setAudience(Audience.registrationId(registrationID))
				.setNotification(Notification.android_ios(alert, title, extras))
				.build();
		return _pushClient.sendPush(payload);
	}

	public static PushPayload buildPushObject_ios_tagAnd_alertWithExtrasAndMessage(
			String title, String alert, Map<String, String> extras,
			String... registrationID) {
		return PushPayload
				.newBuilder()
				.setPlatform(Platform.android_ios())
				.setAudience(Audience.registrationId(registrationID))
				.setNotification(
						Notification
								.newBuilder()
								.addPlatformNotification(
										IosNotification.newBuilder()
												.setAlert(alert).setBadge(1)
												.setSound("happy")
												.addExtra("from", "JPush")
												.build()).build())
				.setMessage(Message.content(title))
				.setOptions(
						Options.newBuilder().setApnsProduction(0).build())
				.build();
	}

	/**
	 * Shortcut
	 */
	public PushResult sendMessageAll(String msgContent)
			throws APIConnectionException, APIRequestException {
		PushPayload payload = PushPayload.messageAll(msgContent);
		return _pushClient.sendPush(payload);
	}

	/**
	 * Shortcut
	 */
	public PushResult sendAndroidMessageWithAlias(String title,
			String msgContent, String... alias) throws APIConnectionException,
			APIRequestException {
		PushPayload payload = PushPayload
				.newBuilder()
				.setPlatform(Platform.android())
				.setAudience(Audience.alias(alias))
				.setMessage(
						Message.newBuilder().setTitle(title)
								.setMsgContent(msgContent).build()).build();
		return _pushClient.sendPush(payload);
	}

	/**
	 * Shortcut
	 */
	public PushResult sendAndroidMessageWithRegistrationID(String title,
			String msgContent, String... registrationID)
			throws APIConnectionException, APIRequestException {
		PushPayload payload = PushPayload
				.newBuilder()
				.setPlatform(Platform.android())
				.setAudience(Audience.registrationId(registrationID))
				.setMessage(
						Message.newBuilder().setTitle(title)
								.setMsgContent(msgContent).build()).build();
		return _pushClient.sendPush(payload);
	}

	/**
	 * Shortcut
	 */
	public PushResult sendIosMessageWithAlias(String title, String msgContent,
			String... alias) throws APIConnectionException, APIRequestException {
		PushPayload payload = PushPayload
				.newBuilder()
				.setPlatform(Platform.ios())
				.setAudience(Audience.alias(alias))
				.setMessage(
						Message.newBuilder().setTitle(title)
								.setMsgContent(msgContent).build()).build();
		return _pushClient.sendPush(payload);
	}

	/**
	 * Shortcut
	 */
	public PushResult sendIosMessageWithRegistrationID(String title,
			String msgContent, String... registrationID)
			throws APIConnectionException, APIRequestException {
		PushPayload payload = PushPayload
				.newBuilder()
				.setPlatform(Platform.ios())
				.setAudience(Audience.registrationId(registrationID))
				.setMessage(
						Message.newBuilder().setTitle(title)
								.setMsgContent(msgContent).build()).build();
		return _pushClient.sendPush(payload);
	}

}

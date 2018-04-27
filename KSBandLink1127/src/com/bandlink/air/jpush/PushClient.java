package com.bandlink.air.jpush;

/**
 * Entrance for sending Push.
 * 
 * For the following parameters, you can set them by instance creation. This
 * action will override setting in PushPayload Optional. * apnsProduction If not
 * present, the default is true. * timeToLive If not present, the default is
 * 86400(s) (one day).
 * 
 * Can be used directly.
 */
public class PushClient {
	public static final String HOST_NAME_SSL = "https://api.jpush.cn";
	public static final String PUSH_PATH = "/v3/push";

	private final NativeHttpClient _httpClient;

	// The API secret of the appKey. Please get it from JPush Web Portal
	private final String _masterSecret;

	// The KEY of the Application created on JPush. Please get it from JPush Web
	// Portal
	private final String _appKey;

	// If not present, true by default.
	private int _apnsProduction = 0;

	// If not present, the default value is 86400(s) (one day)
	private long _timeToLive = 60 * 60 * 24;

	private boolean _globalSettingEnabled = false;

	// Generated HTTP Basic authorization string.
	private final String _authCode;
	private String _baseUrl;

	/**
	 * Create a Push Client.
	 * 
	 * @param masterSecret
	 *            API access secret of the appKey.
	 * @param appKey
	 *            The KEY of one application on JPush.
	 */
	public PushClient(String masterSecret, String appKey) {
		this(masterSecret, appKey, IHttpClient.DEFAULT_MAX_RETRY_TIMES);
	}

	/**
	 * Create a Push Client with max retry times.
	 * 
	 * @param masterSecret
	 *            API access secret of the appKey.
	 * @param appKey
	 *            The KEY of one application on JPush.
	 * @param maxRetryTimes
	 *            max retry times
	 */
	public PushClient(String masterSecret, String appKey, int maxRetryTimes) {
		this._masterSecret = masterSecret;
		this._appKey = appKey;

		ServiceHelper.checkBasic(appKey, masterSecret);

		this._authCode = ServiceHelper.getAuthorizationBase64(_appKey,
				_masterSecret);
		this._baseUrl = HOST_NAME_SSL + PUSH_PATH;
		this._httpClient = new NativeHttpClient(maxRetryTimes);
	}

	/**
	 * Create a Push Client with global settings.
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
	public PushClient(String masterSecret, String appKey,
			int apnsProduction, long timeToLive) {
		this(masterSecret, appKey);
		this._apnsProduction = apnsProduction;
		this._timeToLive = timeToLive;
		this._globalSettingEnabled = true;
	}

	public void setBaseUrl(String baseUrl) {
		this._baseUrl = baseUrl;
	}

	public PushResult sendPush(PushPayload pushPayload)
			throws APIConnectionException, APIRequestException {
		if (_globalSettingEnabled) {
			pushPayload.resetOptionsTimeToLive(_timeToLive);
			pushPayload.resetOptionsApnsProduction(_apnsProduction);
		}

		ResponseWrapper response = _httpClient.sendPost(_baseUrl,
				pushPayload.toString(), _authCode);

		return PushResult.fromResponse(response);
	}

	public PushResult sendPush(String payloadString)
			throws APIConnectionException, APIRequestException {
		ResponseWrapper response = _httpClient.sendPost(_baseUrl,
				payloadString, _authCode);

		return PushResult.fromResponse(response);
	}

}

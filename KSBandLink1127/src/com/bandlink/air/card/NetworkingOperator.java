package com.bandlink.air.card;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.UUID;

import org.json.JSONObject;

import com.bandlink.air.ble.Converter;

import android.util.Log;

public class NetworkingOperator {
	private static NetworkingOperator _sharedOperator;
	private static final String logtag = "NetworkingOperator";

	public final static int MSG_InitCreditDone = 1;
	public final static int MSG_CreditDone = 2;
	public final static int MSG_ReportCreditResult = 3;

	public final static String MSGKEY_MESSAGE = "message";

	public final static int MSG_BaseConnectDone = 1000;
	public final static int MSG_BaseConnectFaild = 1001;
	public final static int MSG_BaseDisconnectDone = 1002;
	public final static int MSG_ExchangePacketDone = 1100;
	public final static int MSG_ExchangePacketFailed = 1101;

	public static int timeout = 45;
	public static String imei = "";
	public static boolean login = false;

	private static BigInteger g = new BigInteger("2");
	private static BigInteger bigPrime = new BigInteger(
			"FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3DC2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F83655D23DCA3AD961C62F356208552BB9ED529077096966D670C354E4ABC9804F1746C08CA237327FFFFFFFFFFFFFFFF",
			16);
	private static BigInteger random_a;

	private InetSocketAddress endpoint;
	private Socket socket;
	private OutputStream outputStream;
	private InputStream inputStream;

	public static NetworkingOperator getSharedOperator() {
		if (_sharedOperator == null) {
			_sharedOperator = new NetworkingOperator();
			UUID uuid = UUID.randomUUID();
			imei = String.format("F%015d",
					Math.abs(uuid.getMostSignificantBits())).substring(0, 16);
			Log.e(">>>>>uuid>>>>>", imei);
			_sharedOperator.setAddress(Configuration.ServerAddress,
					Configuration.ServerPort);
		}
		return _sharedOperator;
	}

	private NetworkingOperator() {

	}

	public void setAddress(String serverAddress, int port) {
		endpoint = new InetSocketAddress(serverAddress, port);
	}

	public boolean login_method2() throws IOException {
		SecureRandom random = new SecureRandom();
		byte bytes[] = new byte[64];
		random.nextBytes(bytes);
		random_a = new BigInteger(bytes);
		BigInteger ga = g.modPow(random_a, bigPrime);

		TSMPacket packet = new TSMPacket();
		packet.hexIMEI = imei;
		packet.hexOpCode = "0181";
		packet.hexRedirectionCode = "01";
		packet.setFieldWithUTF8Encoding(packet.kField_TransactionRecords,
				ga.toString());
		sendPacket(packet);
		packet = waitPacket();

		if (packet.isResponseNoError()) {
			String gbString = packet
					.getFieldWithUTF8Encoding(packet.kField_TransactionRecords);
			BigInteger gb = new BigInteger(gbString);
			BigInteger key = gb.modPow(random_a, bigPrime);
			String hexKey = key.toString(16);
			TSMPacket.setKey(hexKey.substring(0, 32));
			NetworkingOperator.login = true;
			return true;
		}
		return false;
	}

	public String queryPaymentAuth(String hexUid, String cardChanllange,
			HashMap<String, String> data) throws IOException {
		TSMPacket packet = new TSMPacket();
		packet.hexIMEI = imei;
		packet.hexOpCode = "8001";
		packet.hexRedirectionCode = "80";
		packet.setFieldWithUTF8Encoding(2, hexUid);
		packet.setField(28, cardChanllange);
		packet.setFieldWithUTF8Encoding(29, "000000000000000");
		JSONObject object = new JSONObject(data);
		packet.setJsonObject(object);

		sendPacket(packet);
		packet = waitPacket();

		String hexCommand = null;
		if (packet.isResponseNoError()) {
			hexCommand = packet.getFieldWithHex(28);
		}
		return hexCommand;
	}

	public ProcessResult queryPaymentAuth(String hexUid, String F05,
			String F15, String F14, String F01, String cardChallenge)
			throws IOException {
		ProcessResult pr = new ProcessResult();

		// 组包 获取消费密钥 8001
		TSMPacket packet = new TSMPacket();
		packet.hexIMEI = imei;
		packet.hexOpCode = "8001";
		packet.hexRedirectionCode = "80";
		packet.setFieldWithUTF8Encoding(TSMPacket.kField_AccountID, hexUid);
		packet.setField(TSMPacket.kField_ApduTransmission, cardChallenge);
		packet.setFieldWithUTF8Encoding(TSMPacket.kField_ApplicationProvider,
				"000000000000000");
		HashMap<String, String> queryJsonData = new HashMap<String, String>();
		queryJsonData.put("F05", F05);
		queryJsonData.put("F15", F15);
		queryJsonData.put("F14", F14);
		queryJsonData.put("F01", F01);
		queryJsonData.put("KA", "0");
		JSONObject obj = new JSONObject(queryJsonData);
		packet.setJsonObject(obj);

		// 发送数据
		sendPacket(packet);
		packet = waitPacket();

		pr.setStatus(packet.getBCDFieldWithInteger(TSMPacket.kField_AnsCode));
		if (packet.isResponseNoError()) {
			HashMap<String, Object> result = new HashMap<String, Object>();
			result.put("apdu",
					packet.getFieldWithHex(TSMPacket.kField_ApduTransmission));
			pr.setData(result);
		}

		return pr;
	}

	public ProcessResult getOnlineAccountBalance(String hexUid, String F05,
			String F15, String F14, String F01, String password)
			throws IOException {
		TSMPacket packet = new TSMPacket();
		packet.hexIMEI = imei;
		packet.hexOpCode = "8004";
		packet.hexRedirectionCode = "80";
		packet.setFieldWithUTF8Encoding(TSMPacket.kField_AccountID, hexUid);
		HashMap<String, String> queryJsonData = new HashMap<String, String>();
		queryJsonData.put("F05", F05);
		queryJsonData.put("F15", F15);
		queryJsonData.put("F14", F14);
		queryJsonData.put("F01", F01);
		queryJsonData.put("KA", "0");
		JSONObject obj = new JSONObject(queryJsonData);
		packet.setJsonObject(obj);
		packet.setFieldWithUTF8Encoding(TSMPacket.kField_PaymentPassword,
				password);

		// 发送数据
		sendPacket(packet);
		packet = waitPacket();

		// 解析返回包
		ProcessResult pr = new ProcessResult();
		pr.setStatus(packet.getBCDFieldWithInteger(TSMPacket.kField_AnsCode));

		if (packet.isResponseNoError()) {
			pr.getData()
					.put("accountBalance",
							packet.getBCDFieldWithInteger(TSMPacket.kField_CardBalacnce));
		}

		return pr;

	}

	private void sendPacket(TSMPacket packet) throws IOException {
		byte[] data = packet.getEncryptedPacket();
		Log.d(logtag, "发送数据: " + Converter.byteArrayToHexString(data));
		outputStream.write(data);
	}

	private byte[] waitReceive() throws IOException {
		byte[] buffer = new byte[4096];
		int readBytes = inputStream.read(buffer);
		if (readBytes <= 0) {
			Log.d(logtag, "接口未返回任何数据");
			return new byte[0];
		}
		byte[] retMsg = ConvertUtil.subBytes(buffer, 0, readBytes);
		String res = Converter.byteArrayToHexString(retMsg);
		Log.d(logtag, "接口返回数据: " + Converter.byteArrayToHexString(retMsg));
		return retMsg;
	}

	private TSMPacket waitPacket() throws IOException {
		byte[] result = waitReceive();
		if (result.length == 0) {
			NetworkingOperator.login = false;
			throw new IOException("服务器未返回任何信息，尝试重新登录.");
		}
		return TSMPacket.getTSMPacket(result, true);
	}

	public boolean openConnection() {
		try {
			socket = new Socket();
			// 关闭Nagle算法.立即发包
			socket.setTcpNoDelay(true);

			socket.connect(endpoint);
			socket.setSoTimeout(timeout * 1000);
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();
			Log.i(logtag, "连接成功");
			if (!login) {
				// return this.login();
				return this.login_method2();
			}
			// Message msg = new Message();
			// msg.what = MSG_BaseConnectDone;
			// msgHandler.sendMessage(msg);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public void shutdownConnection() {
		if (outputStream != null) {
			try {
				outputStream.close();
			} catch (IOException e) {
				Log.i("", "关闭OutputStream出现异常" + e.getMessage());
				e.printStackTrace();
			}
		}
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException e) {
				Log.i("", "关闭IntputStream出现异常" + e.getMessage());
				e.printStackTrace();
			}
		}
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				Log.i("", "关闭Socket出现异常" + e.getMessage());
				e.printStackTrace();
			}
			Log.i("", "连接断开...");
			// Message msg = new Message();
			// msg.what = MSG_BaseDisconnectDone;
			// msgHandler.sendMessage(msg);
		}
	}
}

package com.bandlink.air.wxapi;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.bandlink.air.util.HttpUtlis;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
	// IWXAPI 是第三方app和微信通信的openapi接口
	private IWXAPI api;
	public static String AppId = "wx437a579b44fb2fb2";
	public static String AppSecret = "6f771f662bfb3cc0bd4d13226427d274";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		api = WXAPIFactory.createWXAPI(this, AppId, false);
		api.handleIntent(getIntent(), this);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onReq(BaseReq arg0) {
	}

	@Override
	public void onResp(BaseResp resp) {
		String text = "";
		switch (resp.errCode) {
		case BaseResp.ErrCode.ERR_OK:
			// 分享成功
			// onShareWechat();
			text = "分享成功";
			break;
		case BaseResp.ErrCode.ERR_USER_CANCEL:
			// 分享取消
			text = "分享取消";
			break;
		case BaseResp.ErrCode.ERR_AUTH_DENIED:
			// 分享拒绝
			text = "分享失败";
			break;
		}
		Toast.makeText(getApplicationContext(),
				TextUtils.isEmpty(text) ? (TextUtils.isEmpty(resp.errStr) ? "" : resp.errStr) : text,
				Toast.LENGTH_SHORT).show();
		this.finish();
	}

	private void onShareWechat() {
		new Thread(new Runnable() {
			public void run() {
				try {
					HttpUtlis.getRequest("http://air.lovefit.com/index.php/home/expand/onUserShareToWechat/uid/93181",
							null);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}
}
package com.bandlink.air;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import com.bandlink.air.user.LoginActivity;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.wxapi.WXEntryActivity;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners.SnsPostListener;
import com.umeng.socialize.media.QQShareContent;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.WeiXinShareContent;

public class CreateClubInvite extends LovefitActivity implements
		OnClickListener {
	// private Platform plat; 
	private String clubName;
	private String clubid;
	private String inviteMsg;
	private Button finish;
	// 首先在您的Activity中添加如下成员变量
	final UMSocialService mController = UMServiceFactory
			.getUMSocialService("com.umeng.share");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.createclub_step2);
		try {
			JSONObject js = new JSONObject(getIntent()
					.getStringExtra("content"));
			clubName = js.getString("clubname");
			clubid = js.getString("clubid");
			inviteMsg = getString(R.string.create_invite) + clubName
					+ getString(R.string.create_join);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			inviteMsg = getString(R.string.findnew_recommend_mobilelist_text);
			e.printStackTrace();
		}
		ActionbarSettings actionbar = new ActionbarSettings(this,
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						CreateClubInvite.this.finish();
					}
				}, null);
		// actionbar.setTopRightIcon(R.drawable.more);
		actionbar.setTopLeftIcon(R.drawable.ic_top_arrow);
		// actionbar.setTopRightIcon(R.drawable.ic_share);
		actionbar.setTitle(R.string.create_club);
		LinearLayout sysContacts = (LinearLayout) findViewById(R.id.sysContacts);
		LinearLayout qqContacts = (LinearLayout) findViewById(R.id.qqContacts);
		LinearLayout weChatContacts = (LinearLayout) findViewById(R.id.weChatContacts);
		sysContacts.setOnClickListener(this);
		qqContacts.setOnClickListener(this);
		weChatContacts.setOnClickListener(this);
		// ShareSDK.initSDK(this);
		finish = (Button) findViewById(R.id.finish);
		finish.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(CreateClubInvite.this,
						ClubDetailActivity.class);
				Bundle d = new Bundle();
				d.putString("id", clubid);
				i.putExtras(d);
				startActivity(i);
				CreateClubInvite.this.finish();

			}
		});
		// plat = ShareSDK.getPlatform(this, QQ.NAME);
		// api = WXAPIFactory.createWXAPI(this, "wx2381a45310d78c6e");
		// plat.setPlatformActionListener(new PlatformActionListener() {
		//
		// @Override
		// public void onCancel(Platform arg0, int arg1) {
		// // TODO Auto-generated method stub
		//
		// }
		//
		// @Override
		// public void onComplete(Platform arg0, int arg1,
		// HashMap<String, Object> arg2) {
		// // TODO Auto-generated method stub
		//
		// }
		//
		// @Override
		// public void onError(Platform arg0, int arg1, Throwable arg2) {
		// // TODO Auto-generated method stub
		// Toast.makeText(getApplicationContext(), arg2.getMessage(), 1)
		// .show();
		// }
		//
		// });

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (resultCode == Activity.RESULT_OK) {
			Uri contactData = data.getData();
			ContentResolver cr = this.getContentResolver();
			Cursor c = cr.query(contactData, null, null, null, null);
			if (c.moveToFirst()) {

				Uri smsToUri = Uri.parse("smsto:"
						+ c.getString(c.getColumnIndex("data1")));
				Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
				intent.putExtra("sms_body", inviteMsg);
				startActivity(intent);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		// ShareParams sp = new ShareParams();
		switch (arg0.getId()) {
		case R.id.sysContacts:
			Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
			Intent intent = new Intent(Intent.ACTION_PICK, uri);
			startActivityForResult(intent, 0);
			break;
		case R.id.qqContacts:

			UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler(this,
					LoginActivity.QQ_APP_ID, LoginActivity.QQ_APP_KEY);
			qqSsoHandler.setTargetUrl("http://www.lovefit.com");
			QQShareContent qq = new QQShareContent();
			// 设置title
			qq.setShareContent(getString(R.string.share_content));
			qq.setTargetUrl("http://www.lovefit.com");
			UMImage resImage = new UMImage(CreateClubInvite.this,
					R.drawable.ic_launcher);
			qq.setShareImage(resImage);
			qq.setTitle(getString(R.string.app_name));
			mController.setShareMedia(qq);
			qqSsoHandler.addToSocialSDK();
			mController.postShare(CreateClubInvite.this, SHARE_MEDIA.QQ,
					new SnsPostListener() {
						@Override
						public void onStart() {
							// Toast.makeText(CreateClubInvite.this, "开始分享.",
							// Toast.LENGTH_SHORT).show();
						}

						@Override
						public void onComplete(SHARE_MEDIA platform, int eCode,
								SocializeEntity entity) {
							// if (eCode == 200) {
							// Toast.makeText(CreateClubInvite.this, "分享成功.",
							// Toast.LENGTH_SHORT).show();
							// } else {
							// String eMsg = "";
							// if (eCode == -101) {
							// eMsg = "没有授权";
							// }
							// Toast.makeText(CreateClubInvite.this,
							// "分享失败[" + eCode + "] " + eMsg,
							// Toast.LENGTH_SHORT).show();
							// }
						}
					});
			// mController.openShare(this, false);
			break;
		case R.id.weChatContacts:

			UMWXHandler wxHandler = new UMWXHandler(this, WXEntryActivity.AppId,
					WXEntryActivity.AppSecret);
			wxHandler.setTargetUrl("http://www.lovefit.com");
			// 设置微信好友分享内容
			WeiXinShareContent weixinContent = new WeiXinShareContent();
			// 设置title
			weixinContent.setTargetUrl("http://www.lovefit.com");

			weixinContent.setShareContent(getString(R.string.share_content));
			weixinContent.setTitle(getString(R.string.app_name));
			UMImage resImage1 = new UMImage(CreateClubInvite.this,
					R.drawable.ic_launcher);
			weixinContent.setShareImage(resImage1);
			mController.setShareMedia(weixinContent);
			wxHandler.addToSocialSDK();
			mController.postShare(CreateClubInvite.this, SHARE_MEDIA.WEIXIN,
					new SnsPostListener() {
						@Override
						public void onStart() {
							// Toast.makeText(CreateClubInvite.this, "开始分享.",
							// Toast.LENGTH_SHORT).show();
						}

						@Override
						public void onComplete(SHARE_MEDIA platform, int eCode,
								SocializeEntity entity) {
							// if (eCode == 200) {
							// Toast.makeText(CreateClubInvite.this, "分享成功.",
							// Toast.LENGTH_SHORT).show();
							// } else {
							// String eMsg = "";
							// if (eCode == -101) {
							// eMsg = "没有授权";
							// }
							// Toast.makeText(CreateClubInvite.this,
							// "分享失败[" + eCode + "] " + eMsg,
							// Toast.LENGTH_SHORT).show();
							// }
						}
					});
			break;
		}
	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub

	}

}

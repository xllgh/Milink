package com.bandlink.air;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bandlink.air.jpush.Platform;
import com.bandlink.air.user.LoginActivity;
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

public class InviteFriendFragment extends Fragment implements OnClickListener {
	TextView title;
	private Platform plat; 
	// 首先在您的Activity中添加如下成员变量
	final UMSocialService mController = UMServiceFactory
			.getUMSocialService("com.umeng.share");

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View v = inflater.inflate(R.layout.invitefriend_frame2, null);
		LinearLayout sysContacts = (LinearLayout) v
				.findViewById(R.id.sysContacts);
		LinearLayout qqContacts = (LinearLayout) v
				.findViewById(R.id.qqContacts);
		LinearLayout weChatContacts = (LinearLayout) v
				.findViewById(R.id.weChatContacts);
		sysContacts.setOnClickListener(this);
		qqContacts.setOnClickListener(this);
		weChatContacts.setOnClickListener(this);
		// ShareSDK.initSDK(getActivity());
		//
		// plat = ShareSDK.getPlatform(getActivity(), QQ.NAME);
		// api = WXAPIFactory.createWXAPI(getActivity(), "wx2381a45310d78c6e");
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
		//
		// }
		//
		// });
		return v;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (resultCode == Activity.RESULT_OK) {
			Uri contactData = data.getData();
			ContentResolver cr = getActivity().getContentResolver();
			Cursor c = cr.query(contactData, null, null, null, null);
			if (c.moveToFirst()) {
				MyLog.e("Tests", c.getString(c.getColumnIndex("display_name"))
						+ ";" + c.getString(c.getColumnIndex("data1")));
				Uri smsToUri = Uri.parse("smsto:"
						+ c.getString(c.getColumnIndex("data1")));
				Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
				intent.putExtra("sms_body",
						getString(R.string.findnew_recommend_mobilelist_text));
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

			UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler(getActivity(),
					LoginActivity.QQ_APP_ID,LoginActivity.QQ_APP_KEY);
			qqSsoHandler.setTargetUrl("http://www.lovefit.com");
			QQShareContent qq = new QQShareContent();
			// 设置title
			qq.setShareContent(getString(R.string.share_content));
			qq.setTargetUrl("http://www.lovefit.com");
			UMImage resImage = new UMImage(getActivity(),
					R.drawable.ic_launcher);
			qq.setShareImage(resImage);
			qq.setTitle(getString(R.string.app_name));
			mController.setShareMedia(qq);
			qqSsoHandler.addToSocialSDK();
			mController.postShare(getActivity(), SHARE_MEDIA.QQ,
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

			UMWXHandler wxHandler = new UMWXHandler(getActivity(),
					WXEntryActivity.AppId, WXEntryActivity.AppSecret);
			wxHandler.setTargetUrl("http://www.lovefit.com");
			// 设置微信好友分享内容
			WeiXinShareContent weixinContent = new WeiXinShareContent();
			// 设置title
			weixinContent.setTargetUrl("http://www.lovefit.com");

			weixinContent.setShareContent(getString(R.string.share_content));
			weixinContent.setTitle(getString(R.string.app_name));
			UMImage resImage1 = new UMImage(getActivity(),
					R.drawable.ic_launcher);
			weixinContent.setShareImage(resImage1);
			mController.setShareMedia(weixinContent);
			wxHandler.addToSocialSDK();
			mController.postShare(getActivity(), SHARE_MEDIA.WEIXIN,
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
}

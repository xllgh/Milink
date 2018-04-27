package com.bandlink.air;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bandlink.air.update.UpdateActivity;
import com.bandlink.air.user.LoginActivity;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.LovefitActivity;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.view.BadgeView;
import com.bandlink.air.wxapi.WXEntryActivity;
import com.umeng.fb.FeedbackAgent;
import com.umeng.socialize.bean.CustomPlatform;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners.OnSnsPlatformClickListener;
import com.umeng.socialize.controller.listener.SocializeListeners.SnsPostListener;
import com.umeng.socialize.media.QQShareContent;
import com.umeng.socialize.media.QZoneShareContent;
import com.umeng.socialize.media.SinaShareContent;
import com.umeng.socialize.media.TencentWbShareContent;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.umeng.socialize.sso.TencentWBSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.sso.UMSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;

@SuppressLint("NewApi")
public class SystemSetting extends LovefitActivity implements OnClickListener {

	private RelativeLayout l5, l6, l7, l8;
	private FeedbackAgent agent;
	ActionbarSettings action;
	ScrollView scroll;
	// 首先在您的Activity中添加如下成员变量
	final UMSocialService mController = UMServiceFactory
			.getUMSocialService("com.umeng.share");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setContentView(R.layout.sys_setting);

		agent = new FeedbackAgent(this);
		l5 = (RelativeLayout) findViewById(R.id.layout5);
		l6 = (RelativeLayout) findViewById(R.id.layout6);
		l7 = (RelativeLayout) findViewById(R.id.layout7);
		l8 = (RelativeLayout) findViewById(R.id.layout8);
		l5.setOnClickListener(this);
		l6.setOnClickListener(this);
		l7.setOnClickListener(this);
		l8.setOnClickListener(this);
		scroll = (ScrollView) findViewById(R.id.scroll);
		action = new ActionbarSettings(this, new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				SystemSetting.this.finish();
			}
		}, null);
		action.setTitle(R.string.system_settings);
		action.setTopLeftIcon(R.drawable.ic_top_arrow);

		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		action.setVisibility(View.INVISIBLE);
		scroll.setVisibility(View.INVISIBLE);
		super.onPause();
	}

	SharedPreferences preferences;

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		action.setVisibility(View.VISIBLE);
		scroll.setVisibility(View.VISIBLE);
		action.setRightVisible(false);
		preferences = getSharedPreferences(SharePreUtils.APP_ACTION,
				Context.MODE_PRIVATE);
		if (preferences.getBoolean("appupdate", false)) {
			final BadgeView b = new BadgeView(this);
			TextView textview = (TextView) findViewById(R.id.update);
			b.setTargetView(textview);
			b.setBadgeGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
			b.setBadgeMargin(
					(int) textview.getPaint().measureText(
							textview.getText() + "")
							/ 2
							+ (int) getResources().getDisplayMetrics().density
							* 5, 0, 0, 0);
			b.setBadgeMargin(0, 0, 0, 0);
			b.setText(1 + "");
			if (Build.VERSION.SDK_INT >= 11) {
				b.setOnDragListener(new OnDragListener() {

					@Override
					public boolean onDrag(View v, DragEvent event) {
						// TODO Auto-generated method stub
						switch (event.getAction()) {
						case DragEvent.ACTION_DRAG_ENTERED:
							v.layout(
									(int) event.getX(),
									(int) event.getY(),
									(int) event.getX()
											+ (int) b.getMeasuredWidth(),
									(int) event.getY()
											+ (int) b.getMeasuredHeight());
							break;
						case DragEvent.ACTION_DROP:
							v.setVisibility(View.GONE);
							preferences.edit().putBoolean("appupdate", false)
									.commit();
							break;
						}
						return false;
					}
				});
			}
		}
		super.onResume();
	}

	private void setShareContent() {
		// 设置分享内容
		mController.setShareContent(getString(R.string.share_content));
		// 设置分享图片，参数2为本地图片的资源引用
		mController.setShareMedia(new UMImage(this, R.drawable.ic_launcher));

		// QQ
		UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler(this, LoginActivity.QQ_APP_ID,
				LoginActivity.QQ_APP_KEY);
		qqSsoHandler.setTargetUrl("http://www.lovefit.com");
		QQShareContent qq = new QQShareContent();
		UMImage resImage1 = new UMImage(SystemSetting.this,
				R.drawable.ic_launcher);
		qq.setShareImage(resImage1);
		qq.setShareContent(getString(R.string.share_content));
		qq.setTargetUrl("http://www.lovefit.com");
		qq.setTitle(getString(R.string.app_name));
		mController.setShareMedia(qq);
		qqSsoHandler.addToSocialSDK();

		// QZONE
		QZoneSsoHandler qZoneSsoHandler = new QZoneSsoHandler(this,
				LoginActivity.QQ_APP_ID,LoginActivity.QQ_APP_KEY);
		qZoneSsoHandler.setTargetUrl("http://www.lovefit.com");
		QZoneShareContent qz = new QZoneShareContent();
		qz.setShareImage(resImage1);
		qz.setShareContent(getString(R.string.share_content));
		qz.setTargetUrl("http://www.lovefit.com");
		qz.setTitle(getString(R.string.app_name));
		mController.setShareMedia(qz);
		qZoneSsoHandler.addToSocialSDK();
		// 微信
		UMWXHandler wxHandler = new UMWXHandler(this, WXEntryActivity.AppId,
				WXEntryActivity.AppSecret);
		wxHandler.setTargetUrl("http://www.lovefit.com");
		// 设置微信好友分享内容
		WeiXinShareContent weixinContent = new WeiXinShareContent();
		weixinContent.setShareImage(resImage1);
		// 设置title
		weixinContent.setTargetUrl("http://www.lovefit.com");
		weixinContent.setShareContent(getString(R.string.share_content));
		weixinContent.setTitle(getString(R.string.app_name));
		mController.setShareMedia(weixinContent);
		wxHandler.addToSocialSDK();

		// 微信朋友圈
		UMWXHandler wxCircleHandler = new UMWXHandler(this,
				WXEntryActivity.AppId, WXEntryActivity.AppSecret);
		wxCircleHandler.setTargetUrl("http://www.lovefit.com");
		// 设置微信好友分享内容
		CircleShareContent cs = new CircleShareContent();
		cs.setShareContent(getString(R.string.share_content));
		cs.setShareImage(resImage1);
		// 设置title
		cs.setTargetUrl("http://www.lovefit.com");
		cs.setTitle(getString(R.string.app_name));
		mController.setShareMedia(cs);
		wxCircleHandler.setToCircle(true);
		wxCircleHandler.addToSocialSDK();
		// 新浪微博
		mController.getConfig().setSsoHandler(new SinaSsoHandler());
		mController.getConfig().setSinaCallbackUrl(
				"http://sns.whalecloud.com/sina2/callback");
		SinaShareContent si = new SinaShareContent(); // 设置title
		si.setTargetUrl("http://www.lovefit.com");
		si.setShareImage(resImage1);
		si.setShareContent(getString(R.string.share_content));
		si.setTitle(getString(R.string.app_name));
		mController.setShareMedia(si);
		// 腾讯微博
		mController.getConfig().setSsoHandler(new TencentWBSsoHandler());
		TencentWbShareContent tb = new TencentWbShareContent(); // 设置title
		tb.setTargetUrl("http://www.lovefit.com");
		tb.setShareImage(resImage1);
		tb.setShareContent(getString(R.string.share_content));
		tb.setTitle(getString(R.string.app_name));
		mController.setShareMedia(tb);
		mController.setShareMedia(new UMImage(this, R.drawable.ic_launcher));

		CustomPlatform customPlatform = new CustomPlatform("contacts",
				getString(R.string.app_name), R.drawable.ic_launcher);
		customPlatform.mClickListener = new OnSnsPlatformClickListener() {
			@Override
			public void onClick(Context context, SocializeEntity entity,
					SnsPostListener listener) {
				Intent i = new Intent();
				// i.putExtra("imagePath", Environment
				// .getExternalStorageDirectory().toString()
				// + "/milink/waterimage/waterimage.jpg");
				i.putExtra("tid", 4);
				i.setClass(SystemSetting.this, ShareEditActivity.class);
				startActivity(i);
			}
		};
		mController.getConfig().addCustomPlatform(customPlatform);

	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		/** 使用SSO授权必须添加如下代码 */
		UMSsoHandler ssoHandler = mController.getConfig().getSsoHandler(
				requestCode);
		if (ssoHandler != null) {
			ssoHandler.authorizeCallBack(requestCode, resultCode, data);
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		switch (v.getId()) {
		case R.id.layout5:
			Intent i = new Intent(this, UpdateActivity.class);
			startActivity(i);
			break;
		case R.id.layout6:
			Intent ii = new Intent(this, HelpActivity.class);
			startActivity(ii);
			break;
		case R.id.layout7:

			// mController.getConfig().setPlatforms(SHARE_MEDIA.WEIXIN,
			// SHARE_MEDIA.WEIXIN_CIRCLE, SHARE_MEDIA.QQ,
			// SHARE_MEDIA.QZONE, SHARE_MEDIA.SINA, SHARE_MEDIA.TENCENT);
			setShareContent();
			mController.openShare(this, false);
			// showShare(false, null, false);
			break;
		case R.id.layout8:
			agent.startFeedbackActivity();
			break;
		}
	}

	private void showShare(boolean silent, String platform, boolean captureView) {
		// final OnekeyShare oks = new OnekeyShare();
		// oks.setNotification(R.drawable.ic_launcher,
		// getString(R.string.app_name));
		// oks.setTitle(getString(R.string.lebu));
		// oks.setTitleUrl("http://www.lovefit.com");
		// oks.setText(getString(R.string.share_content));
		// oks.setUrl("http://www.lovefit.com");
		// oks.setSite(getString(R.string.app_name));
		// oks.setSiteUrl("http://www.lovefit.com");
		// oks.setSilent(silent);
		// if (platform != null) {
		// oks.setPlatform(platform);
		// }
		//
		// // 令编辑页面显示为Dialog模式
		// oks.setDialogMode();
		//
		// // 在自动授权时可以禁用SSO方式
		// oks.disableSSOWhenAuthorize();
		//
		// // 去除注释，则快捷分享的操作结果将通过OneKeyShareCallback回调
		// // oks.setCallback(new OneKeyShareCallback());
		// oks.setShareContentCustomizeCallback(new
		// ShareContentCustomizeCallback() {
		//
		// @Override
		// public void onShare(Platform platform, ShareParams paramsToShare) {
		// // TODO Auto-generated method stub
		//
		// }
		// });
		//
		// // 去除注释，演示在九宫格设置自定义的图标
		// // Bitmap logo = BitmapFactory.decodeResource(getResources(),
		// // R.drawable.logo_lovefit);
		// // String label = getResources().getString(R.string.app_name);
		// // OnClickListener listener = new OnClickListener() {
		// // public void onClick(View v) {
		// // String text = "Customer Logo -- ShareSDK " +
		// // ShareSDK.getSDKVersionName();
		// // Toast.makeText(getApplicationContext(), text,
		// Toast.LENGTH_SHORT).show();
		// // oks.finish();
		// // }
		// // };
		// // oks.setCustomerLogo(logo, label, listener);
		//
		// // 去除注释，则快捷分享九宫格中将隐藏新浪微博和腾讯微博
		// // oks.addHiddenPlatform(SohuMicroBlog.NAME);
		// // oks.addHiddenPlatform(Renren.NAME);
		// // 为EditPage设置一个背景的View
		// oks.show(this);
	}

	@Override
	public void OnColorChanged(int color) {
		// TODO Auto-generated method stub

	}
}

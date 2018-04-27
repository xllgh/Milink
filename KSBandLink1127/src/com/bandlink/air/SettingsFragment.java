package com.bandlink.air;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import cn.jpush.android.service.PushService;

import com.bandlink.air.ble.BluetoothLeService;
import com.bandlink.air.gps.OffLineManager;
import com.bandlink.air.simple.SyncQQHealth;
import com.bandlink.air.update.UpdateActivity;
import com.bandlink.air.user.LoginActivity;
import com.bandlink.air.util.ActionbarSettings;
import com.bandlink.air.util.MainInterface;
import com.bandlink.air.util.NotificationUtils;
import com.bandlink.air.util.SharePreUtils;
import com.bandlink.air.util.Util;
import com.bandlink.air.view.BadgeView;
import com.bandlink.air.wxapi.WXEntryActivity;
import com.milink.android.lovewalk.bluetooth.service.StepService;
import com.umeng.fb.FeedbackAgent;
import com.umeng.fb.model.Conversation.SyncListener;
import com.umeng.socialize.bean.CustomPlatform;
import com.umeng.socialize.bean.SHARE_MEDIA;
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
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;
import com.umeng.fb.model.DevReply;
import com.umeng.fb.model.Reply;

public class SettingsFragment extends Fragment implements View.OnClickListener {
	private MainInterface minterface;
	private RelativeLayout layout1, layout3, layout4, layout5, layout6, layout7, layout8, layout18, layout22, layout28,
			layout29;
	ActionbarSettings actionbar;
	ScrollView scroll;
	private Button logout;

	public static final int MESSAGE_BEGIN = 1;
	public static final int MESSAGE_FINISH = 2;
	public static final int MESSAGE_ERROR = 3;
	public static final int MESSAGE_NEWEST = 4;
	public static final int MESSAGE_DOWN_BEGIN = 5;
	public static final int MESSAGE_DOWN_UPDATE = 6;
	public ProgressDialog pBar;
	private SharedPreferences m_settingPre;
	private Context con;
	public static final String UPDATE_SERVER = "http://www.lovefit.com/";
	public static final String UPDATE_APKNAME = "downloadfiles/Move.apk";
	public static final String UPDATE_VERJSON = "downloadfiles/version.json";
	public static final String UPDATE_SAVENAME = "Move.apk";
	public static String UPDATE_TEXT = "";

	private FeedbackAgent agent;
	
	
	public void takePicture(View view){
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View contentView = inflater.inflate(R.layout.settings_frame, null);
		scroll = (ScrollView) contentView.findViewById(R.id.scroll);
		actionbar = new ActionbarSettings(contentView, minterface);
		m_settingPre = getActivity().getSharedPreferences(SharePreUtils.APP_ACTION, Context.MODE_PRIVATE);
		actionbar.setTitle(R.string.set);
		// ShareSDK.initSDK(getActivity());
		con = Util.getThemeContext(getActivity());
		layout1 = (RelativeLayout) contentView.findViewById(R.id.layout1);
		layout3 = (RelativeLayout) contentView.findViewById(R.id.layout3);
		layout3.setVisibility(View.VISIBLE);
		layout4 = (RelativeLayout) contentView.findViewById(R.id.layout4);
		// 健康商城
		// layout4.setVisibility(View.GONE);
		layout5 = (RelativeLayout) contentView.findViewById(R.id.layout5);
		layout6 = (RelativeLayout) contentView.findViewById(R.id.layout6);
		layout7 = (RelativeLayout) contentView.findViewById(R.id.layout7);
		layout8 = (RelativeLayout) contentView.findViewById(R.id.layout8);
		layout28 = (RelativeLayout) contentView.findViewById(R.id.layout28);
		// 问题
		layout28.setVisibility(View.GONE);
		// QQ健康
		layout29 = (RelativeLayout) contentView.findViewById(R.id.layout29);
		layout29.setVisibility(View.GONE);
		// sys
		layout18 = (RelativeLayout) contentView.findViewById(R.id.layout18);

		// 离线地图

		layout22 = (RelativeLayout) contentView.findViewById(R.id.layout22);
		// 目标设置
		// RelativeLayout layout10 = (RelativeLayout) contentView
		// .findViewById(R.id.layout10);
		logout = (Button) contentView.findViewById(R.id.logout);
		logout.setOnClickListener(this);
		layout1.setOnClickListener(this);
		layout3.setOnClickListener(this);
		layout4.setOnClickListener(this);
		layout5.setOnClickListener(this);
		layout6.setOnClickListener(this);
		layout7.setOnClickListener(this);
		layout8.setOnClickListener(this);
		layout18.setOnClickListener(this);
		layout28.setOnClickListener(this);
		layout22.setOnClickListener(this);
		layout29.setOnClickListener(this);
		// layout10.setOnClickListener(this);
		if (m_settingPre.getInt("ISMEMBER", 0) == 0) {
			logout.setText(R.string.login);
		}
		if (m_settingPre.getBoolean("appupdate", false)) {
			BadgeView backgroundShapeBadge = new BadgeView(getActivity());
			backgroundShapeBadge.setBadgeGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
			TextView t = null;
			if (layout18.getChildAt(0) instanceof TextView) {
				t = (TextView) layout18.getChildAt(0);
				backgroundShapeBadge.setBadgeCount(1);
				backgroundShapeBadge.setTargetView(layout18.getChildAt(0));
			}
		}

		// if (m_settingPre.getBoolean("airupdate", false)) {
		// BadgeView backgroundShapeBadge = new BadgeView(getActivity());
		// backgroundShapeBadge.setBadgeMargin(0, 15, 50, 0);
		// backgroundShapeBadge.setText("1", BufferType.NORMAL);
		// backgroundShapeBadge.setTargetView(layout3);
		// }

		agent = new FeedbackAgent(getActivity());

		// agent.sync();
		agent.getDefaultConversation().sync(new SyncListener() {

			@Override
			public void onSendUserReply(List<Reply> arg0) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onReceiveDevReply(List<DevReply> arg0) {
				if (arg0 != null) {
					int num = arg0.size();
					// TODO Auto-generated method stub
					if (num > 0) {
						BadgeView backgroundShapeBadge = new BadgeView(getActivity());
						backgroundShapeBadge.setText(num + "", BufferType.NORMAL);
						backgroundShapeBadge.setTargetView(layout8.getChildAt(0));
					}
				}

			}
		});

		return contentView;
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		minterface = (MainInterface) activity;
		super.onAttach(activity);
	}

	final UMSocialService mController = UMServiceFactory.getUMSocialService("com.umeng.share");

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		switch (arg0.getId()) {
		case R.id.layout1:
			intent.setClass(getActivity(), PersonalSettings.class);
			startActivity(intent);
			break;
		case R.id.layout18:
			intent.setClass(getActivity(), SystemSetting.class);
			startActivity(intent);
			break;
		case R.id.layout22:
			intent.setClass(getActivity(), OffLineManager.class);
			startActivity(intent);
			break;
		// case R.id.layout10:
		// intent.setClass(getActivity(), TargetSettingActivity.class);
		// startActivity(intent);
		// break;
		case R.id.layout3:
			intent.setClass(getActivity(), DeviceManager.class);
			startActivity(intent);

			break;
		case R.id.layout4:
			intent.setClass(getActivity(), MallActivity.class);
			startActivity(intent);
			break;
		case R.id.layout5:
			// update
			startActivity(new Intent(getActivity(), UpdateActivity.class));
			break;
		case R.id.layout6:
			intent.setClass(getActivity(), HelpActivity.class);
			startActivity(intent);
			break;
		case R.id.layout7:
			setShareContent();
			mController.openShare(getActivity(), false);
			break;
		case R.id.layout8:
			agent.startFeedbackActivity();

			break;
		case R.id.layout28:
			intent.setClass(getActivity(), FriendFragment.class);
			startActivity(intent);

			break;
		case R.id.logout:
			if (getActivity().getSharedPreferences(SharePreUtils.APP_ACTION, Context.MODE_PRIVATE).getInt("ISMEMBER",
					0) == 0) {
				Intent in = new Intent();
				in.setClass(getActivity(), LoginActivity.class);
				in.putExtra("value", "re");
				startActivity(in);
				getActivity().finish();
			} else {
				if (getActivity().getSharedPreferences(SharePreUtils.APP_ACTION, Context.MODE_PRIVATE)
						.getInt("ISMEMBER", 0) == 0) {

				} else {
					AlertDialog.Builder aDailog1 = new AlertDialog.Builder(con);
					aDailog1.setTitle(R.string.tishi);
					aDailog1.setMessage(R.string.exit_ensure);
					aDailog1.setPositiveButton(R.string.logout, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							try {
								getActivity().stopService(new Intent(getActivity(), BluetoothLeService.class));
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							try {
								getActivity().stopService(new Intent(getActivity(), PushService.class));
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							getActivity().getSharedPreferences(SharePreUtils.APP_ACTION, Activity.MODE_PRIVATE).edit()
									.clear().commit();
							Intent in = new Intent();
							in.setClass(getActivity(), LoginActivity.class);
							in.putExtra("value", "re");
							startActivity(in);
							getActivity().finish();
						}
					});

					aDailog1.setNegativeButton(R.string.completely_exit, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							try {
								getActivity().stopService(new Intent(getActivity(), BluetoothLeService.class));
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							try {
								getActivity().stopService(new Intent(getActivity(), PushService.class));
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							try {
								Intent intent = new Intent("MilinkConfig");
								intent.putExtra("command", 2);
								getActivity().sendBroadcast(intent);
								NotificationManager manager = (NotificationManager) getActivity()
										.getSystemService(Context.NOTIFICATION_SERVICE);
								manager.cancel(NotificationUtils.GPSOrStepNotfication);
								getActivity().stopService(new Intent(getActivity(), StepService.class));

							} catch (Exception e) {
								// TODO: handle exception
							}
							android.os.Process.killProcess(android.os.Process.myPid());
							dialog.cancel();
						}
					});

					aDailog1.show();
				}
			}
			break;
		case R.id.layout29:
			Intent mIntent = new Intent(getActivity(), SyncQQHealth.class);

			startActivity(mIntent);
			break;
		default:
			break;

		}

	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		actionbar.setVisibility(View.VISIBLE);
		scroll.setVisibility(View.VISIBLE);
		super.onResume();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		actionbar.setVisibility(View.INVISIBLE);
		scroll.setVisibility(View.INVISIBLE);
		super.onPause();
	}

	private void setShareContent() {
		// 设置分享内容
		mController.setShareContent(getString(R.string.share_content));
		// 设置分享图片，参数2为本地图片的资源引用
		mController.setShareMedia(new UMImage(getActivity(), R.drawable.ic_launcher));

		// QQ
		UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler(getActivity(), LoginActivity.QQ_APP_ID,
				LoginActivity.QQ_APP_KEY);
		qqSsoHandler.setTargetUrl("http://www.lovefit.com");
		QQShareContent qq = new QQShareContent();
		UMImage resImage1 = new UMImage(getActivity(), R.drawable.ic_launcher);
		qq.setShareImage(resImage1);
		qq.setShareContent(getString(R.string.share_content));
		qq.setTargetUrl("http://openbox.mobilem.360.cn/qcms/view/t/detail?t=2&sid=3154228");
		qq.setTitle(getString(R.string.app_name));
		mController.setShareMedia(qq);
		qqSsoHandler.addToSocialSDK();

		// QZONE
		QZoneSsoHandler qZoneSsoHandler = new QZoneSsoHandler(getActivity(), LoginActivity.QQ_APP_ID,
				LoginActivity.QQ_APP_KEY);
		qZoneSsoHandler.setTargetUrl("http://openbox.mobilem.360.cn/qcms/view/t/detail?t=2&sid=3154228");
		QZoneShareContent qz = new QZoneShareContent();
		qz.setShareImage(resImage1);
		qz.setShareContent(getString(R.string.share_content));
		qz.setTargetUrl("http://openbox.mobilem.360.cn/qcms/view/t/detail?t=2&sid=3154228");
		qz.setTitle(getString(R.string.app_name));
		mController.setShareMedia(qz);
		qZoneSsoHandler.addToSocialSDK();
		// 微信
		UMWXHandler wxHandler = new UMWXHandler(getActivity(), WXEntryActivity.AppId, WXEntryActivity.AppSecret);
		wxHandler.setTargetUrl("http://openbox.mobilem.360.cn/qcms/view/t/detail?t=2&sid=3154228");
		// 设置微信好友分享内容
		WeiXinShareContent weixinContent = new WeiXinShareContent();
		weixinContent.setShareImage(resImage1);
		// 设置title
		weixinContent.setTargetUrl("http://openbox.mobilem.360.cn/qcms/view/t/detail?t=2&sid=3154228");
		weixinContent.setShareContent(getString(R.string.share_content));
		weixinContent.setTitle(getString(R.string.app_name));
		mController.setShareMedia(weixinContent);
		wxHandler.addToSocialSDK();

		// 微信朋友圈
		UMWXHandler wxCircleHandler = new UMWXHandler(getActivity(), WXEntryActivity.AppId, WXEntryActivity.AppSecret);
		wxCircleHandler.setTargetUrl("http://openbox.mobilem.360.cn/qcms/view/t/detail?t=2&sid=3154228");
		// 设置微信好友分享内容
		CircleShareContent cs = new CircleShareContent();
		cs.setShareContent(getString(R.string.share_content));
		cs.setShareImage(resImage1);
		// 设置title
		cs.setTargetUrl("http://openbox.mobilem.360.cn/qcms/view/t/detail?t=2&sid=3154228");
		cs.setTitle(getString(R.string.app_name));
		mController.setShareMedia(cs);
		wxCircleHandler.setToCircle(true);
		wxCircleHandler.addToSocialSDK();
		// 新浪微博
		// mController.getConfig().setSsoHandler(new SinaSsoHandler());
		// mController.getConfig().setSinaCallbackUrl("http://sns.whalecloud.com/sina2/callback");
		// SinaShareContent si = new SinaShareContent(); // 设置title
		// si.setTargetUrl("http://openbox.mobilem.360.cn/qcms/view/t/detail?t=2&sid=3154228");
		// si.setShareImage(resImage1);
		// si.setShareContent(getString(R.string.share_content));
		// si.setTitle(getString(R.string.app_name));
		// mController.setShareMedia(si);
		mController.getConfig().removePlatform(SHARE_MEDIA.SINA);
		// 腾讯微博
		mController.getConfig().setSsoHandler(new TencentWBSsoHandler());
		TencentWbShareContent tb = new TencentWbShareContent(); // 设置title
		tb.setTargetUrl("http://openbox.mobilem.360.cn/qcms/view/t/detail?t=2&sid=3154228");
		tb.setShareImage(resImage1);
		tb.setShareContent(getString(R.string.share_content));
		tb.setTitle(getString(R.string.app_name));
		mController.setShareMedia(tb);
		mController.setShareMedia(new UMImage(getActivity(), R.drawable.ic_launcher));

		CustomPlatform customPlatform = new CustomPlatform("contacts", getString(R.string.app_name),
				R.drawable.ic_launcher);
		customPlatform.mClickListener = new OnSnsPlatformClickListener() {
			@Override
			public void onClick(Context context, SocializeEntity entity, SnsPostListener listener) {
				Intent i = new Intent();
				// i.putExtra("imagePath", Environment
				// .getExternalStorageDirectory().toString()
				// + "/milink/waterimage/waterimage.jpg");
				i.putExtra("tid", 4);
				i.setClass(getActivity(), ShareEditActivity.class);
				startActivity(i);
			}
		};
		// mController.getConfig().addCustomPlatform(customPlatform);

	}

}

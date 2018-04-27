package com.bandlink.air.util;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bandlink.air.ExpandActivity;
import com.bandlink.air.R;

public class ActionbarSettings {

	private View frView;
	private ImageView left, right;
	private TextView title,rtext;
	private MainInterface minterface;
	private Activity sactivity;
	public static int NORMAL = 1;
	public static int SETTINGS = 2;

 
	public ActionbarSettings(Activity activity, View.OnClickListener lsl,
			View.OnClickListener lsr) {
		sactivity = activity;
		rtext = (TextView) sactivity.findViewById(R.id.righttext);
		left = (ImageView) sactivity.findViewById(R.id.actionleft);
		right = (ImageView) sactivity.findViewById(R.id.actionright);
		if (lsl == null && left!=null) {
			left.setVisibility(View.GONE);
		} else if(left!=null){ 
			left.setVisibility(View.VISIBLE);
			LinearLayout ly = (LinearLayout) left.getParent();
			ly.setOnClickListener(lsl);
		}

		if (lsr == null) {
			right.setVisibility(View.GONE);
		} else {
			right.setVisibility(View.VISIBLE);
			LinearLayout ly2 = (LinearLayout) right.getParent();
			ly2.setOnClickListener(lsr);
		}

		title = (TextView) sactivity.findViewById(R.id.actiontitle);

	}

	public void setVisibility(int f) {
		if (left != null)
			left.setVisibility(f);
		if (right != null)
			right.setVisibility(f);
		if (title != null)
			title.setVisibility(f);
	}

	// Fragment actionbar 鍙宠竟鏃犵偣鍑讳簨浠讹紝宸﹁竟榛樿婊戝姩鏍�
	public ActionbarSettings(View view, MainInterface interf) {
		frView = view;
		left = (ImageView) frView.findViewById(R.id.actionleft);
		right = (ImageView) frView.findViewById(R.id.actionright);
		right.setVisibility(View.GONE);
		title = (TextView) frView.findViewById(R.id.actiontitle);
		minterface = interf;

		LinearLayout ly = (LinearLayout) left.getParent();
		ly.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				minterface.onToggle();
			}
		});

	}
	public ActionbarSettings(View view, MainInterface interf,final Fragment f) {
		frView = view;
		left = (ImageView) frView.findViewById(R.id.actionleft);
		right = (ImageView) frView.findViewById(R.id.actionright);
		right.setVisibility(View.GONE);
		title = (TextView) frView.findViewById(R.id.actiontitle);
		minterface = interf;

		LinearLayout ly = (LinearLayout) left.getParent();
		ly.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(f instanceof ExpandActivity){
					ExpandActivity x = (ExpandActivity)f;
					if(x.webview.canGoBack()){
						x.webview.goBack();
						x.action.setTopLeftIcon(R.drawable.ic_top_left);
					}else{
						minterface.onToggle();
					}
				}
			}
		});

	}

	public int getHeight() {
		if (left != null) {
			return left.getRootView().getHeight();
		} else {
			return 0;
		}
	}

	public void setRightVisible(boolean isVi) {
		if (right != null) {
			if (isVi) {
				right.setVisibility(View.VISIBLE);
			} else {
				right.setVisibility(View.GONE);
			}
		}
	}

	// Fragment actionbar锛屽乏杈归粯璁ゆ粦鍔ㄦ爮,鍙宠竟鐐瑰嚮浜嬩欢
	public ActionbarSettings(View view, View.OnClickListener lsr,
			MainInterface interf) {
		frView = view;

		left = (ImageView) frView.findViewById(R.id.actionleft);
		right = (ImageView) frView.findViewById(R.id.actionright);
		if (lsr == null) {
			right.setVisibility(View.GONE);
		} else {
			right.setVisibility(View.VISIBLE);
			LinearLayout ly2 = (LinearLayout) right.getParent();
			ly2.setOnClickListener(lsr);

		}

		title = (TextView) frView.findViewById(R.id.actiontitle);
		minterface = interf;
		LinearLayout ly = (LinearLayout) left.getParent();
		ly.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				minterface.onToggle();
			}
		});

	}

 
	public ActionbarSettings(View view, View.OnClickListener lsl,
			View.OnClickListener lsr) {
		frView = view;
		left = (ImageView) frView.findViewById(R.id.actionleft);
		right = (ImageView) frView.findViewById(R.id.actionright);
		title = (TextView) frView.findViewById(R.id.actiontitle);
		if (lsl == null) {
			left.setVisibility(View.GONE);
		} else {
			left.setVisibility(View.VISIBLE);
			LinearLayout ly = (LinearLayout) left.getParent();
			ly.setOnClickListener(lsl);
		}

		if (lsr == null) {
			right.setVisibility(View.GONE);
		} else {
			right.setVisibility(View.VISIBLE);
			LinearLayout ly2 = (LinearLayout) right.getParent();
			ly2.setOnClickListener(lsr);
		}
	}

	public void setTopLeftIcon(int rid) {
		left.setImageResource(rid);
	}

	public void setTopRightIcon(int rid) {
		right.setImageResource(rid);
	}

	public void setTitle(int rid) {
		title.setText(rid);
	}
	public void setRightText(int rid) {
		if(rtext!=null){
			rtext.setVisibility(View.VISIBLE);
			rtext.setText(rid);
		}
	}
	public void setRightText(String rid) {
		if(rtext!=null){
			rtext.setVisibility(View.VISIBLE);
			rtext.setText(rid);
		}
	}


	public void setTitle(String rid) {
		title.setText(rid);
	}

}

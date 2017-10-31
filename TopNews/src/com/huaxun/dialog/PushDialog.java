package com.huaxun.dialog;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import cn.jpush.android.api.JPushInterface;

import com.huaxun.MainActivity;
import com.huaxun.R;
import com.huaxun.news.activity.WebActivity;
import com.huaxun.news.bean.News;
import com.huaxun.utils.Util;

public class PushDialog {
	private android.app.AlertDialog ad;
	private TextView titleView;
	private TextView timeView;
	private TextView messageView;
	private TextView cancleTextView;
	private TextView chooseTextView;
	private Typeface fontFace;

	public PushDialog(final MainActivity mainAct, final News news) {
		ad = new android.app.AlertDialog.Builder(mainAct).create();
		ad.show();

		Window window = ad.getWindow();
		WindowManager m = mainAct.getWindowManager();
	    Display d = m.getDefaultDisplay(); 
        WindowManager.LayoutParams p = window.getAttributes(); 
        p.height = (int) (d.getHeight() * 0.3); 
        p.width = (int) (d.getWidth() - Util.dip2px(mainAct, 60));
        window.setAttributes(p);
		
		window.setContentView(R.layout.push_dialog);
		titleView = (TextView) window.findViewById(R.id.title);
		timeView = (TextView) window.findViewById(R.id.time);
		messageView = (TextView) window.findViewById(R.id.message);
		messageView.setMovementMethod(ScrollingMovementMethod.getInstance()); 
		
		cancleTextView = (TextView) window.findViewById(R.id.cancelID);
		chooseTextView = (TextView) window.findViewById(R.id.chooseID);

		fontFace = Typeface.createFromAsset(mainAct.getAssets(),"fonts/klz.ttf");
		titleView.setTypeface(fontFace);
		timeView.setTypeface(fontFace);
		messageView.setTypeface(fontFace);
		cancleTextView.setTypeface(fontFace);
		chooseTextView.setTypeface(fontFace);
		
		try {
			titleView.setText(news.getNewstitle());
			messageView.setText(news.summary);
			timeView.setText(getCurrentTime());
		} catch (Exception e) {
			e.printStackTrace();
		}		
		
		cancleTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		
		chooseTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent it = new Intent(mainAct, WebActivity.class);
				Bundle bundle = new Bundle();
				bundle.putSerializable("NEWS", news);
				it.putExtras(bundle);
				mainAct.startActivity(it);
				mainAct.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				dismiss();
			}
		});
		
	}
	
	private String getCurrentTime(){
		SimpleDateFormat df = new SimpleDateFormat("MM月dd日  HH:mm");
		return df.format(new Date());
	}

	public void dismiss() {
		ad.dismiss();
	}
}
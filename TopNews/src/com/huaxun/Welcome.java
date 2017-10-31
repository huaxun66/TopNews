package com.huaxun;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import cn.jpush.android.api.JPushInterface;

public class Welcome extends Activity implements OnClickListener {
	private TextView remainTime;
	private ImageView goNow;
	private ImageView guide;
    private Typeface fontFace;
	private int time = 4;
	View view;
	
	private final int updateTime = 1;
	Handler timeHandler = new Handler() {
		public void handleMessage(Message msg){
			switch (msg.what) {
			case updateTime:
				if (time >= 0){
					remainTime.setText(String.valueOf(time--));
					timeHandler.sendEmptyMessageDelayed(updateTime, 1000);
				} else {
					redirectTo();
				}
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		final Window win = getWindow();//返回当前Activity的Window对象,Window类中概括了Android窗口的基本属性和基本功能
		//隐藏状态栏
		win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题栏
		view = View.inflate(this, R.layout.welcome, null);
		guide = (ImageView) view.findViewById(R.id.guide_iv);
		remainTime = (TextView) view.findViewById(R.id.remainTime);
		goNow = (ImageView) view.findViewById(R.id.goNow);
		//从Assert文件中读取字体类型
		fontFace = Typeface.createFromAsset(this.getAssets(),"fonts/klz.ttf");
		remainTime.setTypeface(fontFace);
		goNow.setOnClickListener(this);
		setContentView(view);
		initData();
	}
	
	//本 API 用于“用户使用时长”，“活跃用户”，“用户打开次数”的统计，并上报到服务器，在 Portal 上展示给开发者
	@Override
	public void onPause() {
		super.onPause();
		JPushInterface.onPause(this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		JPushInterface.onResume(this);
	}
	
	private void initData() {
		//动画集
		final AnimationSet mAnimationSet = new AnimationSet(true);
		// 透明动画
		AlphaAnimation mAlphaAnimation = new AlphaAnimation(0.4f, 1.0f);
		mAlphaAnimation.setDuration(2000);
		// 缩放动画
		ScaleAnimation mScaleAnimation = new ScaleAnimation(1, 1.3f, 1, 1.3f, Animation.RELATIVE_TO_SELF, 0.5f,Animation.RELATIVE_TO_SELF, 0.5f);
		mScaleAnimation.setDuration(6500);
		mAnimationSet.addAnimation(mAlphaAnimation);
		mAnimationSet.addAnimation(mScaleAnimation);
		guide.startAnimation(mAnimationSet);
		mAnimationSet.setAnimationListener(new AnimationListener() {		
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				timeHandler.sendEmptyMessage(updateTime);
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
			}
		});
	}
	
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
			case R.id.goNow:
				redirectTo();
				timeHandler.removeMessages(updateTime);
				break;
		}
	}

	private void redirectTo() {
		startActivity(new Intent(getApplicationContext(), MainActivity.class));
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		finish();
	}
	
	/** 
	 * 键盘按键按下是触发该方法
	 * @param keyCode：被按下的键值即键盘码 
	 *        event：按键事件的对象
	 * @return
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==4 ){//按下“返回”按键
			android.os.Process.killProcess(android.os.Process.myPid());//让程序完全退出应用
		}
		return super.onKeyDown(keyCode, event);
	}


}

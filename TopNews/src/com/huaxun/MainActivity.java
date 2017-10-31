package com.huaxun;

import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.sharesdk.framework.ShareSDK;

import com.huaxun.app.AppApplication;
import com.huaxun.dialog.ExitDialog;
import com.huaxun.dialog.PushDialog;
import com.huaxun.fragment.ChatFragment;
import com.huaxun.fragment.LifeNewFragment;
import com.huaxun.fragment.MenuLeftFragment;
import com.huaxun.fragment.MenuRightFragment;
import com.huaxun.fragment.MusicFragment;
import com.huaxun.fragment.NewsFragment;
import com.huaxun.fragment.NewsRadioFragment;
import com.huaxun.news.activity.WebActivity;
import com.huaxun.news.bean.News;
import com.huaxun.tool.Constants;
import com.huaxun.tool.Settings;
import com.huaxun.utils.Util;
import com.huaxun.view.BottomControlPanel;
import com.huaxun.view.BottomControlPanel.BottomPanelCallback;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

public class MainActivity extends SlidingFragmentActivity implements BottomPanelCallback {
	
	private SlidingMenu slide_menu;
	public RelativeLayout root_layout;
	public static BottomControlPanel bottomPanel = null;
	public static RelativeLayout headPanel;
	/** head 头部 的中间的Text*/
	private TextView top_title;
	/** head 头部 的左侧菜单 按钮*/
	private ImageView top_left;
	/** head 头部 的右侧菜单 按钮*/
	private ImageView top_right;
	
	private HashMap<String, SoftReference<Fragment>> mCacheFragments;// 菜单项Fragment缓存
	private ArrayList<Fragment> mAllFmt;// 所有打开过的Fragment	
	private String currFragTag = "";
	
	private Settings setting;
	public String brightnessmode;
	private float brightnesslevel;
	private WindowManager.LayoutParams attributes;
	
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			finish();
		}
	};
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Util.setTranslucent(this);
		setContentView(R.layout.activity_main);
		setting = new Settings(this, false);
		brightnessmode = setting.getBrightnessMode(); //亮度模式
		attributes = getWindow().getAttributes();               //屏幕属性
		brightnesslevel=attributes.screenBrightness;            //亮度值
		if(brightnessmode.equals("0")){  //夜间模式
			switchNightMode(true);
		}
		initSlidingMenu();
		initView();		
		initUserInfo();	
		ShareSDK.initSDK(this);
		
		checkShareNews(getIntent());
		mCacheFragments = new HashMap<String, SoftReference<Fragment>>();
		mAllFmt = new ArrayList<Fragment>();
		setDefaultFirstFragment(Constants.FRAGMENT_FLAG_NEWS);
		
		mShowDialogReceiver = new ShowDialogReceiver();
		IntentFilter mShowDialogFilter = new IntentFilter("SHOW_PUSH_DIALOG");
		registerReceiver(mShowDialogReceiver, mShowDialogFilter);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		ShareSDK.stopSDK(this);
	}
	
	public void initUserInfo() {
		 if (AppApplication.mUserInfo != null) {
			// 发送广播刷新用户信息
			 new Handler().postDelayed(new Runnable(){
				@Override
				public void run() {
				     Intent refreshInfoIntent = new Intent("REFRESH_INFO");
					 refreshInfoIntent.putExtra("type", "LOGIN");
					 sendBroadcast(refreshInfoIntent); 
				}				 
			 }, 500);
		 }		 
	}
	
	// 从MyReceiver中过来的News
	private void checkShareNews(Intent intent) {
		Object obj = intent.getSerializableExtra("pushNews");
		if (obj != null && obj instanceof News) {
			Intent it = new Intent(this, WebActivity.class);
			Bundle bundle = new Bundle();
			bundle.putSerializable("NEWS", (News) obj);
			it.putExtras(bundle);
			startActivity(it);
			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);	
		}
	}
	
	public void switchNightMode(boolean flag){
		if (flag == true) {
			 attributes.screenBrightness=Settings.KEY_DARKNESS;	
			 getWindow().setAttributes(attributes); 
			 setting.setBrightnessMode("0");
		} else {
			 attributes.screenBrightness=brightnesslevel;	
			 getWindow().setAttributes(attributes); 
			 setting.setBrightnessMode("1");
		}		 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
//		menu.clear();
//		//Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		getMenuInflater().inflate(R.menu.main, menu);        //每次从音乐等其他界面点击optionmenu，重新创建，只显示亮度模式菜单
		MenuItem nightItem = menu.findItem(R.id.brightness_mode);
		brightnessmode = setting.getBrightnessMode(); //亮度模式
		nightItem.setTitle(brightnessmode.equals("0") ? "日间模式" : "夜间模式");
		return super.onPrepareOptionsMenu(menu);
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		 case R.id.brightness_mode:
			 Util.showToast(this, brightnessmode.equals("0") ? "日间模式" : "夜间模式");
			 if (brightnessmode.equals("0")) {  //夜间模式
				 switchNightMode(false);
			 } else {                           //日间模式
				 switchNightMode(true);
			 }
			 break;
		}
		return false;
	}

	
	private void initView(){
		root_layout = (RelativeLayout)findViewById(R.id.root_layout);
		headPanel = (RelativeLayout)findViewById(R.id.head_layout);
		top_left = (ImageView) findViewById(R.id.top_left);
		top_right = (ImageView) findViewById(R.id.top_right);
		top_title = (TextView) findViewById(R.id.top_title);
		top_title.setText(Constants.FRAGMENT_FLAG_NEWS);
		top_left.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if(slide_menu.isMenuShowing()){
					slide_menu.showContent();
				}else{
					slide_menu.showMenu();
				}
			}
		});
		top_right.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {
				if(slide_menu.isSecondaryMenuShowing()){
					slide_menu.showContent();
				}else{
					slide_menu.showSecondaryMenu();
				}
			}
		});
		
		bottomPanel = (BottomControlPanel)findViewById(R.id.bottom_layout);
		bottomPanel.setOnClickListener(null);
		if(bottomPanel != null){
			bottomPanel.initBottomPanel();
			bottomPanel.setBottomCallback(this);
		}
	}
	
	/**
	 * 点击bottomPanel的回调函数
	 */
	public void onBottomPanelClick(int itemId) {		
		String tag = "";
		if((itemId & Constants.BTN_FLAG_MUSIC) != 0){
			tag = Constants.FRAGMENT_FLAG_MUSIC;
		}else if((itemId & Constants.BTN_FLAG_LIFE) != 0){
			tag = Constants.FRAGMENT_FLAG_LIFE;
		}else if((itemId & Constants.BTN_FLAG_RADIO) != 0){
			tag = Constants.FRAGMENT_FLAG_RADIO;
		}else if((itemId & Constants.BTN_FLAG_NEWS) != 0){
			tag = Constants.FRAGMENT_FLAG_NEWS;
		}else if((itemId & Constants.BTN_FLAG_CHAT) != 0){
			tag = Constants.FRAGMENT_FLAG_CHAT;
		}
		setTabSelection(tag);
		top_title.setText(tag);
	}
	
	//----------------------配合栏目上下滑动时，显示或者隐藏底部导航栏 Begin------------------------//
	private boolean isAnimation = false;
	public void hideBottomPanel(){
		if (bottomPanel.getVisibility() == View.INVISIBLE)
			return;
		
		if (!isAnimation){
			isAnimation = true;			
			Animation animation = new TranslateAnimation(0f, 0f, 0f, 120f);
			animation.setDuration(500);
			
			bottomPanel.startAnimation(animation);
			animation.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {}
				
				@Override
				public void onAnimationRepeat(Animation animation) {}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					isAnimation = false;
					bottomPanel.setVisibility(View.INVISIBLE);
				}
			});
		}
	}
	
	public void showBottomPanel(){
		if (bottomPanel.getVisibility() == View.VISIBLE)
			return;
		
		if (!isAnimation){
			isAnimation = true;
			bottomPanel.setVisibility(View.VISIBLE);
			
			Animation animationShow = new TranslateAnimation(0f, 0f,100f,0f);
			animationShow.setDuration(500);
			bottomPanel.startAnimation(animationShow); 
			
			animationShow.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {}
				
				@Override
				public void onAnimationRepeat(Animation animation) {}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					isAnimation = false;
				}
			});
		}
	}
	//---------------------配合栏目上下滑动时，显示或者隐藏底部导航栏 End---------------------//
	
	/**
	 * 初始化侧边栏
	 */
	private void initSlidingMenu() {
		Fragment leftMenuFragment = new MenuLeftFragment(this);
		setBehindContentView(R.layout.left_menu_frame);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.id_left_menu_frame, leftMenuFragment).commit();
		slide_menu = getSlidingMenu();
		slide_menu.setMode(SlidingMenu.LEFT_RIGHT);
		// 设置触摸屏幕的模式
		slide_menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		slide_menu.setShadowWidthRes(R.dimen.shadow_width);
		slide_menu.setShadowDrawable(R.drawable.shadow);
		// 设置滑动菜单视图的宽度
		slide_menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
//		menu.setBehindWidth()
		// 设置渐入渐出效果的值
		slide_menu.setFadeDegree(0.35f);
		// menu.setBehindScrollScale(1.0f);
		slide_menu.setSecondaryShadowDrawable(R.drawable.shadow);
		//设置右边（二级）侧滑菜单
		slide_menu.setSecondaryMenu(R.layout.right_menu_frame);
		Fragment rightMenuFragment = new MenuRightFragment();
		getSupportFragmentManager().beginTransaction().replace(R.id.id_right_menu_frame, rightMenuFragment).commit();
	}
	
	private void setDefaultFirstFragment(String tag){
		setTabSelection(tag);
		bottomPanel.defaultBtnChecked();
	}

	private void setTabSelection(String tag) {
		if(TextUtils.equals(tag, currFragTag)){
			return;
		}
		hideAllFragments(); //隐藏所有
		FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
		trans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE); //渐变
		
		Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);  //FragmentManager获取
		
		if (fragment == null) {
			fragment = getCacheByTag(tag);	//缓存获取，其实已经没有必要，因为FragmentManager在任何情况都会帮你存储Fragment
		}
    		
		if (fragment == null) {
			fragment = createFragmentByTag(tag);   //新建
			trans.add(R.id.fragment_content, fragment, tag);
		}		
		trans.show(fragment);
		trans.commitAllowingStateLoss();
		// 加入所有Fragment列表
		addToAllList(fragment);
		// 加入缓存列表
		addToCache(tag,fragment);
		// 设置当前Fragment
		currFragTag = tag;
	}
	
	//通过Tag创建Fragment
	private Fragment createFragmentByTag (String tag) {
		Fragment fmt = null;		
		if(tag.equals(Constants.FRAGMENT_FLAG_NEWS)){
			fmt = new NewsFragment();
		}else if(tag.equals(Constants.FRAGMENT_FLAG_LIFE)){
			fmt = new LifeNewFragment();
		}else if(tag.equals(Constants.FRAGMENT_FLAG_MUSIC)){
			fmt = new MusicFragment();
		}else if(tag.equals(Constants.FRAGMENT_FLAG_RADIO)){
			fmt = new NewsRadioFragment();
		}else if(tag.equals(Constants.FRAGMENT_FLAG_CHAT)){
			fmt = new ChatFragment();
		}
		return fmt;
		
	}
	
	// 从缓存中获取指定的fragment
	private Fragment getCacheByTag(String tag) {
		if (null == mCacheFragments || mCacheFragments.size() == 0) {
			return null;
		}
		SoftReference<Fragment> cacheFragment = mCacheFragments.get(tag);
		if (null == cacheFragment || null == cacheFragment.get()) {
			return null;
		}
		return cacheFragment.get();
	}

	// 把fragment加入缓存中
	private void addToCache(String tag, Fragment fragment) {
		if (tag != null && fragment != null) {
			if (!mCacheFragments.containsKey(tag)) {
				SoftReference<Fragment> cacheFragment = new SoftReference<Fragment>(fragment);
				mCacheFragments.put(tag, cacheFragment);
			}
		}
	}
	
	// 加入所有列表
	private boolean addToAllList(Fragment fragment) {
		if (null != fragment && null != mAllFmt && !mAllFmt.contains(fragment)) {
			boolean add = mAllFmt.add(fragment);
            return add;
		}
		return false;
	}
	
	// 隐藏所有Fragment
	private void hideAllFragments() {
		if (null == mAllFmt || mAllFmt.size() < 1) {
			return;
		}
		FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
		for (Fragment fmt : mAllFmt) {
			if (null != fmt) {
				trans.hide(fmt);
			}
		}
		trans.commitAllowingStateLoss();
	}

	@Override
	protected void onStop() {
		super.onStop();
		currFragTag = "";
	}
	
	
	/******************************* 接收到推送，弹出对话框 ************************/
	// 拖拽排序完成的广播
	private ShowDialogReceiver mShowDialogReceiver;

	class ShowDialogReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (isRunningForeground()) {
				Object obj = intent.getSerializableExtra("pushNews");
				new PushDialog(MainActivity.this, (News)obj);
			}
		}
	}
	
	// 判断应用是否在前台运行
	private boolean isRunningForeground() {
		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
		String currentPackageName = cn.getPackageName();
		if (!TextUtils.isEmpty(currentPackageName) && currentPackageName.equals(getPackageName())) {
			return true;
		}
		return false;
	}

	
//	public void onActivityResult(int requestCode, int resultCode, Intent data) {
//		BaseTools.showlog("++requestCode="+requestCode);
//		BaseTools.showlog("++resultCode="+resultCode);
//		switch (requestCode) {
//		case NewsFragment.NODEREQUEST:
//			 //NewsFragment是第一级Fragment，继续分发，这里就不需要处理
//			break;
//		case NewsMediaFragment.LISTREQUEST:
//			//没有处理嵌套Fragment的情况，也就是说回调只到第一级Fragment，就没有继续分发
//			break;
//		default:
//			break;
//		}
//		super.onActivityResult(requestCode, resultCode, data);
//	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
	}
	
	public ScalCallback scalCallback;
	public interface ScalCallback {
		public void scal_media();
	}
	
	public void setScalInterface(ScalCallback scalCallback){
		this.scalCallback = scalCallback;
	}

	@Override
	public void onBackPressed() {
		if (getRequestedOrientation() == 0) {
			if (scalCallback != null){
				scalCallback.scal_media();
			}
		} else {
			if (slide_menu.isMenuShowing() || slide_menu.isSecondaryMenuShowing()) {
				slide_menu.showContent();
			} else {
				new ExitDialog(this, handler).show();
			}
		}		
	}
	
	public static int mStatusAndTitleHeight = 0;// 状态栏加topbar的高度

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (mStatusAndTitleHeight <= 0) {
			 mStatusAndTitleHeight = getStatusHeight() + Util.dip2px(this, 84);
		}
	}

	// 使用反射方式获取状态栏的高度
	private int getStatusHeight() {
		Class<?> c = null;
		Object obj = null;
		Field field = null;
		int x = 0, sbar = 0;
		try {
			c = Class.forName("com.android.internal.R$dimen");
			obj = c.newInstance();
			field = c.getField("status_bar_height");
			//反射出该对象中status_bar_height字段所对应的在R文件的id值
			//该id值由系统工具自动生成,文档描述如下:
			//The desired resource identifier, as generated by the aapt tool.
			x = Integer.parseInt(field.get(obj).toString());
			//依据id值获取到状态栏的高度,单位为像素
			sbar = getResources().getDimensionPixelSize(x);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sbar;
	}

}

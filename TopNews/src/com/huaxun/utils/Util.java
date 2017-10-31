package com.huaxun.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.Toast;

import com.huaxun.MainActivity;
import com.huaxun.R;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class Util {
	
	public static String getEastDayURL(String url){
		if (url.startsWith("http")) {
			return url;
		}
		return "http://listen.eastday.com" + url;
	}
	
	public static String getEastDayAudioURL(String url){
		return "http://listen.eastday.com/media/auto" + url;
	}
	
	public static String getTestImageURL(String url){
		return "http://222.73.244.30" + url;
	}
	
	/**
	 * dip转为 px
	 */
	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	/**
	 *  px 转为 dip
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}
	
	@SuppressLint("ResourceAsColor") 
	public static void setTranslucent(Activity activity) {
		// 4.4及以上版本开启
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			setTranslucentStatus(activity, true);
		}
		SystemBarTintManager tintManager = new SystemBarTintManager(activity);
		tintManager.setStatusBarTintEnabled(true);
		tintManager.setNavigationBarTintEnabled(true);
		// 默认颜色
		tintManager.setTintColor(Color.parseColor("#5CACEE"));
	}
	
	public static void setColorTranslucent(Activity activity, String color) {
		// 4.4及以上版本开启
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			setTranslucentStatus(activity, true);
		}
		SystemBarTintManager tintManager = new SystemBarTintManager(activity);
		tintManager.setStatusBarTintEnabled(true);
//		tintManager.setNavigationBarTintEnabled(true);
		// 自定义颜色
		tintManager.setTintColor(Color.parseColor(color));
	}
	
	@SuppressLint("ResourceAsColor") 
	public static void setDrawableTranslucent(Activity activity, int drawable) {	
		// 4.4及以上版本开启
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			setTranslucentStatus(activity, true);
		}
		SystemBarTintManager tintManager = new SystemBarTintManager(activity);
		tintManager.setStatusBarTintEnabled(true);
//		tintManager.setNavigationBarTintEnabled(true);
		// 自定义 图片
		tintManager.setStatusBarTintResource(drawable);
	}
	
    @TargetApi(19)
    public static void setTranslucentStatus(Activity activity, boolean on) {
    	// 4.4及以上版本开启
    	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
    		return;
    	}
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }
	
	//及时Toast
	private static Toast toast;
	public static void showToast(final Context context, final String content){
		new Handler().post(new Runnable(){
			@Override
			public void run() {
				if (toast != null){
					 toast.setText(content);         
					 toast.setDuration(Toast.LENGTH_SHORT);      
					 toast.show(); 
				}else{
					toast = Toast.makeText(context, content, Toast.LENGTH_SHORT); 
					toast.show();
				}
			}});
	}
	
	public static void hideToast(){
		new Handler().post(new Runnable(){
			@Override
			public void run() {
				toast.cancel();
			}});
	}

	public static int getIconTypeRes(String iconType) {
		int resId = -1, type = -1;
		try {
			type = Integer.parseInt(iconType);
		} catch (Exception e) {
		}
		switch (type) {
		case 1:// 专题
			resId = R.drawable.article_tip_special;
			break;
		case 2:// 置顶
			resId = R.drawable.article_tip_promotion;
			break;
		case 3:// 热点
			resId = R.drawable.article_tip_hot;
			break;
		case 4:// 直播
			resId = R.drawable.article_tip_live;
			break;
		case 8:// 人气
			resId = R.drawable.article_tip_rq;
			break;
		case 9:// 独家
			resId = R.drawable.article_tip_dj;
			break;
		case 10:// 策划
			resId = R.drawable.article_tip_ch;
			break;
		case 11:// 最新
			resId = R.drawable.article_tip_zx;
			break;
		case 12:// 专访
			resId = R.drawable.article_tip_zuanfang;
			break;
		case 13:// 突发
			resId = R.drawable.article_tip_tufa;
			break;
		case 14:// 原创
			resId = R.drawable.article_tip_yuanchuang;
			break;
		default:
			resId = -1;
			break;
		}
		return resId;
	}	
	
	public final static int getWindowsWidth(Activity activity) {
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm.widthPixels;
	}
	
	// 获取videoview离顶部的距离
	public static int getTopMargin(int position, ListView listView) {
		// 获取点击的view在屏幕中的坐标
		View itemView = getViewByPosition(position, listView);
		int[] location = new int[2];
		itemView.getLocationOnScreen(location);
		// view在屏幕中的y坐标-（状态栏+标题栏）=videoview离父控件的高度
		int topMargin = location[1] - MainActivity.mStatusAndTitleHeight;
		return topMargin;
	}
	
	// 获取指定位置上的item
	public static View getViewByPosition(int pos, ListView listView) {
		final int firstListItemPosition = listView.getFirstVisiblePosition();
		final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

		if (pos < firstListItemPosition || pos > lastListItemPosition) {
			return listView.getAdapter().getView(pos, null, listView);
		} else {
			final int childIndex = pos - firstListItemPosition;
			return listView.getChildAt(childIndex);
		}
	}
    
}

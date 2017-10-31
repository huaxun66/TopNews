package com.huaxun.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetUtil {
	
	public static boolean isWIFIOn(Context context){
		ConnectivityManager connManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (mWifi.isConnected()) {
			return true;
			}
		return false;
	}
	
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager cmgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cmgr != null) {
			NetworkInfo info = cmgr.getActiveNetworkInfo();
			if (info == null)
				return false; // 没有开启网络
			if (info.isConnected())
				return true; // isConnected()返回true表示当前网络连接上并且能访问数据
		}
		return false;
	}
    
}

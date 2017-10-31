package com.huaxun.tool;

import com.huaxun.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Settings {
	
	private SharedPreferences settingPreferences;
	public static final String PREFERENCE_NAME = "settings";
	
	public final String KEY_SKINID = "skin_id";
	public final String KEY_BRIGHTNESS = "brightnessmode";	//屏幕模式->1:日间模式，0:夜间模式	
	public final String KEY_FONT_SIZE = "fontsize";          //文字大小  ->0:小 1:中 2：大	
	public final String KEY_PICTURE_WALL = "picturewallmode"; //照片墙模式->1:网络照片，0:本地照片
	public final String KEY_LOCK = "lockmode";               //解锁模式->0：声纹解锁 1：九宫格密码 2：九宫格连线
	public final String KEY_AUTODOWNLOAD_NEWS = "autodownloadnewsmode";	//自动下载新闻模式->1:不下载视频, 0：下载视频
	public final String KEY_DOWNLOAD_MEDIA = "downloadmediamode";	//下载视频模式->1:通知栏提示模式, 0：断点续传模式   
	public final String KEY_DOWNLOAD_ONLY_WIFI = "downloadonlywifi"; //true:只在wifi环境下下载   false:非WIFI也可下载
	//屏幕模式->1:日间模式，0:夜间模式
	public static final String BRIGHTNESS_DAY = "1";
	public static final String BRIGHTNESS_NIGHT = "0";	
	public static final float KEY_DARKNESS = 0.1f;	//夜间模式值level
	//文字大小  ->0:小 1:中 2：大
	public static final String FONT_SIZE_LARGE = "2";
	public static final String FONT_SIZE_NORMAL = "1";
	public static final String FONT_SIZE_SMALL = "0";
	//照片墙模式->1:网络照片，0:本地照片
	public static final String PICTURE_WALL_LOCAL = "0";
	public static final String PICTURE_WALL_NETWORK = "1";
	//解锁模式->0：声纹解锁 1：九宫格密码 2：九宫格连线
	public static final String ISV_MODE = "0";
	public static final String PASSWORD_MODE = "1";
	public static final String LINE_MODE = "2";	
	//自动下载新闻模式->1:不下载视频, 0：下载视频
	public static final String AUTODOWNLOAD_CONTAIN_MEDIA = "0";
	public static final String AUTODOWNLOAD_NOT_CONTAIN_MEDIA = "1";	
	//下载视频模式->1:通知栏提示模式, 0：断点续传模式
	public static final String DOWNLOAD_MEDIA_RESUME = "0";
	public static final String DOWNLOAD_MEDIA_NOTIFICATION = "1";	
	
	/**
	 * 皮肤资源ID数组
	 */
	public static final int[] SKIN_RESOURCES = {
		R.drawable.music_bg01, R.drawable.music_bg02,
		R.drawable.music_bg03, R.drawable.music_bg04,
		R.drawable.music_bg05, R.drawable.music_bg06
	};
	
	public Settings(Context context, boolean isWrite) {
		settingPreferences = context.getSharedPreferences(PREFERENCE_NAME, isWrite ? Context.MODE_WORLD_WRITEABLE : Context.MODE_WORLD_READABLE);
	}

	/**
	 * 获取皮肤资源ID
	 * @return
	 */
	public int getCurrentSkinResId() {
		int skinIndex = settingPreferences.getInt(KEY_SKINID, 0);
		if(skinIndex >= SKIN_RESOURCES.length) {
			skinIndex = 0;
		}
		return SKIN_RESOURCES[skinIndex];
	}
	
	/**
	 * 获取皮肤Id
	 * @return
	 */
	public int getCurrentSkinId() {
		int skinIndex = settingPreferences.getInt(KEY_SKINID, 0);
		if(skinIndex >= SKIN_RESOURCES.hashCode()) {
			skinIndex = 0;
		}
		return skinIndex;
	}
	
	/**
	 * 设置皮肤资源ID
	 * @param skinIndex
	 */
	public void setCurrentSkinResId(int skinIndex) {
		Editor it = settingPreferences.edit();
		it.putInt(KEY_SKINID, skinIndex);
		it.commit();
	}
	
	public String getBrightnessMode() {
		return settingPreferences.getString(KEY_BRIGHTNESS, BRIGHTNESS_DAY);
	}

	public void setBrightnessMode(String value) {
		Editor it = settingPreferences.edit();
		it.putString(KEY_BRIGHTNESS, value);
		it.commit();
	}
	
	public String getFontSize() {
		return settingPreferences.getString(KEY_FONT_SIZE, FONT_SIZE_NORMAL);
	}

	public void setFontSize(String value) {
		Editor it = settingPreferences.edit();
		it.putString(KEY_FONT_SIZE, value);
		it.commit();
	}
	
	public String getPictureWallMode() {
		return settingPreferences.getString(KEY_PICTURE_WALL, PICTURE_WALL_NETWORK);
	}

	public void setPictureWallMode(String value) {
		Editor it = settingPreferences.edit();
		it.putString(KEY_PICTURE_WALL, value);
		it.commit();
	}
	
	public String getLockMode() {
		return settingPreferences.getString(KEY_LOCK, ISV_MODE);
	}

	public void setLockMode(String value) {
		Editor it = settingPreferences.edit();
		it.putString(KEY_LOCK, value);
		it.commit();
	}
	
	public String getAutodownloadNewsMode() {
		return settingPreferences.getString(KEY_AUTODOWNLOAD_NEWS, AUTODOWNLOAD_NOT_CONTAIN_MEDIA);
	}

	public void setAutodownloadNewsMode(String value) {
		Editor it = settingPreferences.edit();
		it.putString(KEY_AUTODOWNLOAD_NEWS, value);
		it.commit();
	}
	
	public String getDownloadMediaMode() {
		return settingPreferences.getString(KEY_DOWNLOAD_MEDIA, DOWNLOAD_MEDIA_NOTIFICATION);
	}

	public void setDownloadMediaMode(String value) {
		Editor it = settingPreferences.edit();
		it.putString(KEY_DOWNLOAD_MEDIA, value);
		it.commit();
	}
	
	public boolean getDownloadOnlyWifi() {
		return settingPreferences.getBoolean(KEY_DOWNLOAD_ONLY_WIFI, true);
	}

	public void setDownloadOnlyWifi(Boolean flag) {
		Editor it = settingPreferences.edit();
		it.putBoolean(KEY_DOWNLOAD_ONLY_WIFI, flag);
		it.commit();
	}
	
	/**
	 * 获取设置数据
	 * @param key
	 * @return
	 */
	public String getValue(String key) {
		return settingPreferences.getString(key, "1");
	}
	
	/**
	 * 设置键值
	 * @param key
	 * @param value
	 */
	public void setValue(String key, String value) {
		Editor it = settingPreferences.edit();
		it.putString(key, value);
		it.commit();
	}
}

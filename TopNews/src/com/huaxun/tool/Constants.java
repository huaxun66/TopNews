package com.huaxun.tool;

import com.huaxun.R;

import android.os.Environment;

public class Constants {
	public static String SDPath = Environment.getExternalStorageDirectory().getAbsolutePath().toString();
	public static String RootFolderName = "huaxun";
	public static String RootFolder =  SDPath + "/huaxun";
	public static String NewsFolderPath = RootFolder + "/News";
	public static String PictureFolderPath = RootFolder + "/Picture";
	public static String MediaFolderPath = RootFolder + "/Media";
	public static String MusicFolderPath = RootFolder + "/Music";
	public static String LrcFolderPath = MusicFolderPath + "/Lrc";
	public static String RadioFolderPath = RootFolder + "/Radio";
	public static String UserInfoFolderPath = RootFolder + "/UserInfo";
	public static String CacheFolderPath = RootFolder + "/Cache";
	public static String ImageFolderPath = CacheFolderPath + "/Image";
	public static String AudioFolderPath = CacheFolderPath + "/Audio";
	public static String ErrorFolderPath = RootFolder + "/Error";
	
	public static final String FILE_NMAE_USERINFO = "userinfo";// 用户信息
	public static final String SPEECH_APPID = "570a6ca6"; //科大讯飞APPID
	
	public static String DEVICE_MAC = "";// MAC地址
	public static String JPUSH_ALIAS = "";// 极光推送别名
	
	//Btn的标识	
	public static final int BTN_FLAG_NEWS = 0x01;
	public static final int BTN_FLAG_LIFE = 0x01 << 1;
	public static final int BTN_FLAG_MUSIC = 0x01 << 2;
	public static final int BTN_FLAG_RADIO = 0x01 << 3;	
	public static final int BTN_FLAG_CHAT = 0x01 << 4;
	
	//Fragment的标识
	public static final String FRAGMENT_FLAG_NEWS = "新闻";
	public static final String FRAGMENT_FLAG_LIFE = "生活";
	public static final String FRAGMENT_FLAG_MUSIC = "音乐"; 
	public static final String FRAGMENT_FLAG_RADIO = "电台"; 
	public static final String FRAGMENT_FLAG_CHAT = "聊天"; 
	public static final String FRAGMENT_FLAG_SIMPLE = "simple"; 
	

	public final static int mark_recom = 0;
	public final static int mark_hot = 1;
	public final static int mark_frist = 2;
	public final static int mark_exclusive = 3;
	public final static int mark_favor = 4;
	
	public static final int[] BG_RESOURCES = {
		R.drawable.bg_1, R.drawable.bg_2, R.drawable.bg_3, R.drawable.bg_4,
		R.drawable.bg_5, R.drawable.bg_6, R.drawable.bg_7, R.drawable.bg_8,
		R.drawable.bg_9, R.drawable.bg_10, R.drawable.bg_11, R.drawable.bg_12,
		R.drawable.bg_13
	};
	
}

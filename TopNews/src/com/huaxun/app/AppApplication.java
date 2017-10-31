package com.huaxun.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import cn.sharesdk.framework.ShareSDK;

import com.huaxun.R;
import com.huaxun.chat.faceMode.FaceConversionUtil;
import com.huaxun.menuRight.bean.UserInfo;
import com.huaxun.tool.BaseTools;
import com.huaxun.tool.Constants;
import com.huaxun.tool.VolleyTool;
import com.huaxun.utils.FileUtil;
import com.huaxun.utils.ImageUtil;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

public class AppApplication extends org.litepal.LitePalApplication {
	
	public static final String log = "huaxun";
	public static UserInfo mUserInfo;
	public static int mWidth,mHeight;
	public static String VERSION_NAME = "";// 版本信息
	
	@Override
	public void onCreate() {
		super.onCreate();
//      初始化即创建语音配置对象，只有初始化后才可以使用MSC的各项服务。建议将初始化放在程序入口处
		// 应用程序入口处调用,避免手机内存过小，杀死后台进程,造成SpeechUtility对象为null
		SpeechUtility.createUtility(this.getApplicationContext(), SpeechConstant.APPID + "=" + Constants.SPEECH_APPID);
		mUserInfo = FileUtil.GetUserInfo(this);// 获得保存的用户信息
		getDisplayScreenResolution();
		initImageLoader(this.getApplicationContext());
		initVolleyTool(this.getApplicationContext());
		initErrorLog(); //打印崩溃日志
		initAppInfos();
		initJpush();
		createDir();
		createSharePic();
		loadFaceDatas();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}
	
	public void getDisplayScreenResolution() {
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		mWidth = displayMetrics.widthPixels;
		mHeight = displayMetrics.heightPixels;
	}
	
	/**
	 * 初始化图片加载器
	 */
	public static void initImageLoader(Context context) {
		File cacheDir = new File(Constants.ImageFolderPath);
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
				.memoryCache(new WeakMemoryCache())
				.threadPriority(3)
				.denyCacheImageMultipleSizesInMemory()
				.discCacheFileNameGenerator(new Md5FileNameGenerator())
				.discCacheSize(50 * 1024 * 1024)
				.discCache(new UnlimitedDiscCache(cacheDir))
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.build();
		ImageLoader.getInstance().init(config);
	}
	
	public void initVolleyTool(Context context) {
		/**单例类,获得VolleyTool的一个实例，之后Instance就不用再被创建*/
		VolleyTool.getInstance(this);
	}
	
	private void initAppInfos() {
		Constants.DEVICE_MAC = getDeviceMac();
		Constants.JPUSH_ALIAS = !TextUtils.isEmpty(Constants.DEVICE_MAC) ? Constants.DEVICE_MAC.replace(":", "") : "";
		BaseTools.showlog("alias : " + Constants.JPUSH_ALIAS);
		try {
			VERSION_NAME = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_CONFIGURATIONS).versionName;
		} catch (Exception e) {
			VERSION_NAME = "1.0.0";
		}
	}
	
	// 获取设备mac地址
	private String getDeviceMac() {
		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		String macAddress = info.getMacAddress();
		return macAddress;
	}
	
	private void initJpush() {
		JPushInterface.setDebugMode(true); // 设置开启日志,发布时请关闭日志
		JPushInterface.init(this); // 初始化 JPush
		JPushInterface.setLatestNotificationNumber(this.getApplicationContext(), 3);
		JPushInterface.setAlias(this, Constants.JPUSH_ALIAS,new TagAliasCallback() {
				@Override
				public void gotResult(int arg0, String arg1, Set<String> arg2) {
					if (arg0 == 0) {
						BaseTools.showlog("Jpush绑定Alias成功");
					}
				}
		});
	}
	
	private void createDir() {
		if (! new File(Constants.RootFolder).exists()){
			new File(Constants.RootFolder).mkdir();
		}		
		if (! new File(Constants.NewsFolderPath).exists()){
			new File(Constants.NewsFolderPath).mkdir();
		}
		if (! new File(Constants.PictureFolderPath).exists()){
			new File(Constants.PictureFolderPath).mkdir();
		}		
		if (! new File(Constants.MediaFolderPath).exists()){
			new File(Constants.MediaFolderPath).mkdir();
		} 		
		if (! new File(Constants.MusicFolderPath).exists()){
			new File(Constants.MusicFolderPath).mkdir();
		}
		if (! new File(Constants.LrcFolderPath).exists()){
			new File(Constants.LrcFolderPath).mkdir();
		}		
		if (! new File(Constants.RadioFolderPath).exists()){
			new File(Constants.RadioFolderPath).mkdir();
		}
		if (! new File(Constants.CacheFolderPath).exists()){
			new File(Constants.CacheFolderPath).mkdir();
		}
		if (! new File(Constants.ImageFolderPath).exists()){
			new File(Constants.ImageFolderPath).mkdir();
		}
		if (! new File(Constants.AudioFolderPath).exists()){
			new File(Constants.AudioFolderPath).mkdir();
		}
		if (! new File(Constants.UserInfoFolderPath).exists()){
			new File(Constants.UserInfoFolderPath).mkdir();
		}
		if (! new File(Constants.ErrorFolderPath).exists()){
			new File(Constants.ErrorFolderPath).mkdir();
		}
	}
	
	private void createSharePic() {
		Drawable dra = getResources().getDrawable(R.drawable.icon_hx);
		Bitmap bmp = ImageUtil.drawableToBitmap(dra);
		bmp = ImageUtil.zoomBimtap(bmp, 150, 150); //压缩之后bitmap
		ImageUtil.saveImage(bmp, FileUtil.getCacheImagePath() + File.separator + "share.png", Bitmap.CompressFormat.PNG);
	}
	
	// 加载表情数据
	public void loadFaceDatas() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				FaceConversionUtil.getInstace().getFileText(AppApplication.this);
			}
		}).start();
	}
	
	/********************************** 记录崩溃日志--start **********************************/
	protected boolean isNeedCaughtExeption = true;// 是否捕获未知异常
	private PendingIntent restartIntent;
	private MyUncaughtExceptionHandler uncaughtExceptionHandler;
	public String packgeName;

	public void initErrorLog() {
		// 记录崩溃日志
		packgeName = getPackageName();
		if (isNeedCaughtExeption) {
			cauchException();
		}
	}

	// -------------------异常捕获-----捕获异常后重启系统-----------------//
	private void cauchException() {
		Intent intent = new Intent();
		// 参数1：包名，参数2：程序入口的activity
		intent.setClassName(packgeName, packgeName + ".MainActivity");
		restartIntent = PendingIntent.getActivity(getApplicationContext(), -1, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
		// 程序崩溃时触发线程
		uncaughtExceptionHandler = new MyUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
	}

	// 创建服务用于捕获崩溃异常
	private class MyUncaughtExceptionHandler implements UncaughtExceptionHandler {
		@Override
		public void uncaughtException(Thread thread, Throwable ex) {
			ex.printStackTrace();
			// 保存错误日志
			saveCatchInfo2File(ex);

			// 5秒钟后重启应用
			// AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			// mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 5000, restartIntent);

			// 关闭当前应用
			finishProgram();
		}
	};

	/**
	 * 保存错误信息到文件中
	 * 
	 * @return 返回文件名称
	 */
	private String saveCatchInfo2File(Throwable ex) {
		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		ex.printStackTrace(printWriter);
		Throwable cause = ex.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		printWriter.close();
		String sb = writer.toString();
		try {
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault());
			String time = formatter.format(new Date());
			String fileName = time + ".txt";
			System.out.println("fileName:" + fileName);
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				File dir = new File(Constants.ErrorFolderPath);
				if (!dir.exists()) {
					if (!dir.mkdirs()) {
						// 创建目录失败: 一般是因为SD卡被拔出了
						return "";
					}
				}
				FileOutputStream fos = new FileOutputStream(Constants.ErrorFolderPath + "/" + fileName);
				fos.write(sb.getBytes());
				fos.close();
				// 文件保存完了之后,在应用下次启动的时候去检查错误日志,发现新的错误日志,就发送给开发者
			}
			return fileName;
		} catch (Exception e) {
			System.out.println("an error occured while writing file..." + e.getMessage());
		}
		return null;
	}

	public void finishProgram() {
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(0);
	}

	/********************************** 记录崩溃日志--end **********************************/

	
}

package com.huaxun.news.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.huaxun.app.AppApplication;
import com.huaxun.tool.BaseTools;
import com.huaxun.utils.FileUtil;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;

public class PlayerService extends Service implements Runnable,
		MediaPlayer.OnCompletionListener {
	/* 定于一个多媒体对象 */
	public static MediaPlayer mMediaPlayer = null;
	// 是否单曲循环
	private static boolean isLoop = false;
	//开始播放
	public static final int PLAY=1;
	//暂停播放
	public static final int PAUSE=2;
	//继续播放
	public static final int REPLAY=3;
	//关闭播放器
	public static final int STOP=4;
	// 用户操作
	private int action;
	//播放URL
	private String url;
	

    Handler playHandler = new Handler() {
    	public void handleMessage (Message msg) {
    		playMusic();
    	}
    };

	@Override
	public IBinder onBind(Intent intent) {
		return null;// 这里的绑定没的用，上篇我贴出了如何将activity与service绑定的代码
	}

	@Override
	public void onCreate() {
		super.onCreate();
		if (mMediaPlayer != null) {
			mMediaPlayer.reset();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		mMediaPlayer = new MediaPlayer();
		/* 监听播放是否完成 */
		mMediaPlayer.setOnCompletionListener(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}

		System.out.println("service onDestroy");
	}
    /*启动service时执行的方法*/
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		/*得到从startService传来的动作，后是默认参数，这里是我自定义的常量*/
		action = intent.getIntExtra("action", PLAY);
		url = intent.getStringExtra("url");
		if (action == PLAY) {
			playMusic();
		} else if (action == PAUSE) {
			mMediaPlayer.pause();
		} else if (action == REPLAY) {
			mMediaPlayer.start();
		} else if (action == STOP) {
			mMediaPlayer.stop();
		}

		return super.onStartCommand(intent, flags, startId);
	}

	public void playMusic() {
		try {
			String name = getFileName(url);
			if (FileUtil.isCacheFileExist(name)) {
				/* 重置多媒体 */
				mMediaPlayer.reset();
				/* 读取mp3文件 */
				mMediaPlayer.setDataSource(FileUtil.getCachePath() + "/" + name);
				/* 准备播放 */
				mMediaPlayer.prepare();
				/* 开始播放 */
				mMediaPlayer.start();
				/* 是否单曲循环 */
				mMediaPlayer.setLooping(isLoop);	
			} else {
				new AudioDownLoadThread(url).start();
			}
		} catch (IOException e) {
		}

	}
	
	/***
	 * 获取文件名
	 * @param url
	 * @return
	 */
	private String getFileName(String url){
		if(TextUtils.isEmpty(url) || !url.contains("mp3")){
			return "error";
			}
		return url.substring(url.lastIndexOf("/")+1, url.lastIndexOf("."));
	}

	public class AudioDownLoadThread extends Thread {
		private String mp3url;
		private String mp3name;
		private boolean isContinueDownload;
		
		public AudioDownLoadThread(String url) {
			this.mp3url = url;
			this.mp3name = getFileName(url);
		}

		public void run() {
			httpDownLoad();
		}
		
		private void httpDownLoad() {
			URL url = null;
			HttpURLConnection conn = null;
			InputStream inStream = null;
			try {
				url = new URL(mp3url);
				conn = (HttpURLConnection) url.openConnection();
				conn.setDoInput(true); // 设置向程序输入数据
				conn.setRequestMethod("GET"); // 设置Get方法来请求
				conn.connect(); // 连接服务器
				// 返回200表示连接成功
				int responseCode = conn.getResponseCode();
				if (responseCode == 200) {
					isContinueDownload = true;
					inStream = conn.getInputStream();
					// 如果SD卡上cache文件不存在就创建它，如果存在就删掉它
					FileUtil.createTempCacheFile(mp3name);
					// 真正的下载过程
					File file = null;
					OutputStream output = null;
					try {
						// 在本地创建要下载的文件，然后读取数据放进去
						file = new File(FileUtil.getCachePath() + "/" + mp3name + "tmp");
						// 把创建好的文件用输出流打开，准备往里面写数据
						output = new FileOutputStream(file);
						byte[] buffer = new byte[1024];
						int num = 0;
						do {
							num = inStream.read(buffer);
							if (num <= 0) {
								isContinueDownload = false;
								playHandler.sendEmptyMessageDelayed(0, 300);
								break;
							}
							output.write(buffer, 0, num);
						} while (isContinueDownload);

						output.flush();
						output.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
					File f = new File(FileUtil.getCachePath() + "/" + mp3name);
					file.renameTo(f);
				} else {
				}
			} catch (Exception e) {
			} finally {
				// finally无论有没有异常都会执行，在这里断开连接
				conn.disconnect();
			}
	}
}
	
	// 刷新进度条
	@Override
	public void run() {
		int CurrentPosition = 0;// 设置默认进度条当前位置
		int total = mMediaPlayer.getDuration();//
		while (mMediaPlayer != null && CurrentPosition < total) {
			try {
				Thread.sleep(1000);
				if (mMediaPlayer != null) {
					CurrentPosition = mMediaPlayer.getCurrentPosition();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		/* 播放完当前歌曲，自动播放下一首 */

//		if (++TestMediaPlayer.currentListItme >= TestMediaPlayer.mMusicList
//				.size()) {
//			Toast.makeText(PlayerService.this, "已到最后一首歌曲", Toast.LENGTH_SHORT)
//					.show();
//			TestMediaPlayer.currentListItme--;
//			TestMediaPlayer.audioSeekBar.setMax(0);
//		} else {
//			playMusic();
//		}
	}
}
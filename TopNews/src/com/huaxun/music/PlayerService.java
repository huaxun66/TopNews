package com.huaxun.music;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.huaxun.R;
import com.huaxun.fragment.MusicFragment;
import com.huaxun.music.activity.MusicDetailActivity;
import com.huaxun.music.bean.Mp3Info;
import com.huaxun.tool.BaseTools;
import com.huaxun.utils.MediaUtil;

/***
 * 2015/12/14
 * @author hx
 * 音乐播放服务
 */
@SuppressLint("NewApi")
public class PlayerService extends Service {
	public static MediaPlayer mediaPlayer; // 媒体播放器对象
	private int msg;                       //播放信息
	private int currentPosition; 		// 音乐文件位置			
	private int currentTime;            //当前播放进度
	private boolean needPlay;           //改变进度后是否需要播放
	private int currentMode;            //0:随机 1：全部 2：单曲
	private int notificationId = 2;
	
	// 定义一个常量字符串，该常量用于命名Action
	private static final String ACTION_BUTTON = "ACTION_BUTTON";
	private static final String INTENT_BUTTONID_TAG = "INTENT_BUTTONID_TAG";
	
	private static final int BUTTON_PREV_ID = 1;
	private static final int BUTTON_PALY_ID = 2;
	private static final int BUTTON_NEXT_ID = 3;
	private static final int BUTTON_CLOSE_ID = 4;


	@Override
	public void onCreate() {
		super.onCreate();		
		if (mediaPlayer != null) {
			mediaPlayer.reset();
			mediaPlayer.release();
			mediaPlayer = null;
		}
		mediaPlayer = new MediaPlayer();
		//获取播放模式
		SharedPreferences sharedPreferences = getSharedPreferences("musicKey",Context.MODE_PRIVATE);
		String mode = sharedPreferences.getString("playMode", "0");
		currentMode = Integer.parseInt(mode);
		/**
		 * 设置音乐播放完成时的监听器
		 */
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				if (currentMode == 2) { // 单曲循环
					mediaPlayer.start();
				} else if (currentMode == 1) { // 全部循环
					currentPosition++;
					if(currentPosition > MusicFragment.playMp3List.size() - 1) {	//变为第一首的位置继续播放
						currentPosition = 0;
					}
					Intent sendIntent = new Intent("nowPlayingChanged");
					sendIntent.putExtra("currentPosition", currentPosition);
					// 发送广播，更新newsfragment和newsdetailfarment显示
					sendBroadcast(sendIntent);
					seek(0,true);
				} else if(currentMode == 0) {	//随机播放					
					currentPosition = getRandomIndex(MusicFragment.playMp3List.size() - 1);
					Intent sendIntent = new Intent("nowPlayingChanged");
					sendIntent.putExtra("currentPosition", currentPosition);
					// 发送广播，更新newsfragment和newsdetailfarment显示
					sendBroadcast(sendIntent);
					seek(0,true);
				}
			}
		});

		myReceiver = new MyReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("modeChange");
		registerReceiver(myReceiver, filter);
		
		notificationReceiver = new NotificationBroadcastReceiver();
	     IntentFilter intentFilter = new IntentFilter();
	     intentFilter.addAction(ACTION_BUTTON);
	     registerReceiver(notificationReceiver, intentFilter);
	}

	/**
	 * 获取随机位置
	 * @param end
	 * @return
	 */
	protected int getRandomIndex(int end) {
		int index = (int) (Math.random() * end);
		return index;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		/*得到从startService传来的动作，后是默认参数，这里是我自定义的常量*/
		try {
			msg = intent.getIntExtra("MSG", 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//播放信息
		if (msg == Constant.PlayerMsg.PLAY_MSG) {	//直接播放音乐
			currentPosition = intent.getIntExtra("position", 1);		//歌曲位置
			seek(0, true);
		} else if (msg == Constant.PlayerMsg.PAUSE_MSG) {	//暂停
			pause();	
		} else if (msg == Constant.PlayerMsg.STOP_MSG) {		//停止
			stop();
		} else if (msg == Constant.PlayerMsg.CONTINUE_MSG) {	//继续播放
			replay();	
		} else if (msg == Constant.PlayerMsg.PROGRESS_CHANGE) {	//进度更新
			currentTime = intent.getIntExtra("progress", 0);
			needPlay = intent.getBooleanExtra("needPlay", true);
			seek(currentTime, needPlay);
		}
		super.onStart(intent, startId);
	}

	/**
	 * 改变音乐进度
	 * 
	 * @param position
	 */
	private void seek(int currentTime, boolean needPlay) {
		try {
			mediaPlayer.reset();// 把各项参数恢复到初始状态
			mediaPlayer.setDataSource(MusicFragment.playMp3List.get(currentPosition).getPath());
			mediaPlayer.prepare(); // 进行缓冲
			mediaPlayer.setOnPreparedListener(new PreparedListener(currentTime, needPlay));// 注册一个监听器
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 播放音乐
	 * 
	 * @param position
	 */
	private void play() {
		try {
			mediaPlayer.reset();// 把各项参数恢复到初始状态
			mediaPlayer.setDataSource(MusicFragment.playMp3List.get(currentPosition).getPath());
			mediaPlayer.prepare(); // 进行缓冲	
			mediaPlayer.setOnPreparedListener(null);
			mediaPlayer.start(); // 开始播放
			updateNotification();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 暂停音乐
	 */
	private void pause() {
		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
		}
	}

	private void replay() {
		if (!mediaPlayer.isPlaying()) {
			mediaPlayer.start();
		}
	}

	/**
	 * 停止音乐
	 */
	private void stop() {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
		}
	}

	@Override
	public void onDestroy() {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
		}
		stopForeground(true); //取消前台进程
		unregisterReceiver(myReceiver);
		unregisterReceiver(notificationReceiver);
	}
	
	private void updateNotification() {
		/** 进程(process)的优先级(从高到低)：
		1. 前台进程 Foreground process
              1) 当前用户操作的Activity所在进程
              2) 绑定了当前用户操作的Activity的Service所在进程  
              3) 调用了startForeground()的Service  典型场景：后台播放音乐
        2. 可见进程Visible process
              1) 处于暂停状态的Activity
              2) 绑定到暂停状态的Activity的Service 
        3. 服务进程Service process  
              1)通过startService()启动的Service
        4. 后台进程Background process  
              1) 处于停止状态的Activity
        5. 空进程Empty process **/
		
		Intent intent = new Intent(this, MusicDetailActivity.class);
		intent.putExtra("currentPosition", currentPosition);
		BaseTools.showlog("services中 currentPosition="+currentPosition);
	//注意Intent的flag设置：FLAG_ACTIVITY_CLEAR_TOP: 如果activity已在当前任务中运行，在它前端的activity都会被关闭，
	//	它就成了最前端的activity。FLAG_ACTIVITY_SINGLE_TOP: 如果activity已经在最前端运行，则不需要再加载。设置这两个flag，就是让一个且唯一的一个activity（服务界面）运行在最前端。	
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	//	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		//第二个参数requestcode必须是notificationId,FLAG_UPDATE_CURRENT会更新之前PendingIntent的消息参数，保证点击时的currentPosition是最新的
		PendingIntent pendingItent = PendingIntent.getActivity(this, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		Mp3Info mp3Info = MusicFragment.playMp3List.get(currentPosition);
		RemoteViews remoteView = new RemoteViews(getPackageName(), R.layout.music_notitfication_layout);
		Bitmap bitmap = MediaUtil.getArtwork(this, mp3Info.getId(),mp3Info.getAlbumId(), true, true);// 获取专辑位图对象，为小图
		remoteView.setImageViewBitmap(R.id.icon, bitmap); // 这里显示专辑图片
		remoteView.setTextViewText(R.id.artist, mp3Info.getArtist());
		remoteView.setTextViewText(R.id.song, mp3Info.getTitle());
		if (mediaPlayer.isPlaying()) {
			remoteView.setImageViewResource(R.id.play_music, R.drawable.player_pause);
		} else {
			remoteView.setImageViewResource(R.id.play_music, R.drawable.player_play);
		}
		
		//点击的事件处理
        Intent buttonIntent = new Intent(ACTION_BUTTON);
        /* 上一首按钮 */
        buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_PREV_ID);
        //这里加了广播，所及INTENT的必须用getBroadcast方法
        PendingIntent intent_prev = PendingIntent.getBroadcast(this, 1, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setOnClickPendingIntent(R.id.previous_music, intent_prev);
        /* 播放/暂停  按钮 */
        buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_PALY_ID);
        PendingIntent intent_paly = PendingIntent.getBroadcast(this, 2, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setOnClickPendingIntent(R.id.play_music, intent_paly);
        /* 下一首 按钮  */
        buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_NEXT_ID);
        PendingIntent intent_next = PendingIntent.getBroadcast(this, 3, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setOnClickPendingIntent(R.id.next_music, intent_next);
        /* 关闭 按钮  */
        buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_CLOSE_ID);
        PendingIntent intent_close = PendingIntent.getBroadcast(this, 4, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setOnClickPendingIntent(R.id.close, intent_close);
		
		Notification notification = new Notification.Builder(this)
             .setSmallIcon(R.drawable.music)
             .setWhen(System.currentTimeMillis())
             .setTicker(mp3Info.getTitle()) 
             .setContentTitle(mp3Info.getArtist()) 
             .setContentText(mp3Info.getTitle())
             .setOngoing(true)
//           .setDefaults(Notification.DEFAULT_SOUND)
             .setContentIntent(pendingItent)
             .setContent(remoteView)
             .build();
//      NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//      manager.notify(notificationId, notification);
		startForeground(notificationId,notification); //该方法已创建通知管理器，设置为前台优先级后，点击通知不再自动取消
	}
	
	private void cancleNotification() {
		//取消系统通知 
		stopForeground(true); //取消前台进程
		NotificationManager notiManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
		notiManager.cancel(notificationId);
	}
	
	/**
	 * 
	 * 实现一个OnPrepareLister接口,当音乐准备好的时候开始播放
	 * 
	 */
	private final class PreparedListener implements OnPreparedListener {
		private int currentTime;
		private boolean needPlay;

		public PreparedListener(int currentTime, boolean needPlay) {
			this.currentTime = currentTime;
			this.needPlay = needPlay;
		}

		@Override
		public void onPrepared(MediaPlayer mp) {
			if (currentTime > 0) { // 如果音乐不是从头播放
				mediaPlayer.seekTo(currentTime);
			}
			if (needPlay == true) { // 改变进度后是否需要播放
				mediaPlayer.start();
			}
			updateNotification();
		}
	}

	
	private MyReceiver myReceiver;	//自定义广播接收器
	public class MyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("modeChange")) {
				currentMode = intent.getIntExtra("currentMode", 0);
			}
		}
	}
	
    private NotificationBroadcastReceiver notificationReceiver;
    class NotificationBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_BUTTON)) {
                int btn_id = intent.getIntExtra(INTENT_BUTTONID_TAG, 0);
                BaseTools.showlog("btn_id="+btn_id);
                switch (btn_id) {
                    case BUTTON_PREV_ID:
                    	currentPosition--;
    					if(currentPosition < 0) {	//变为最后一首的位置继续播放
    						currentPosition = MusicFragment.playMp3List.size() - 1;
    					}
    					Intent sendIntent = new Intent("nowPlayingChanged");
    					sendIntent.putExtra("currentPosition", currentPosition);
    					// 发送广播，更新newsfragment和newsdetailfarment显示
    					sendBroadcast(sendIntent);
    					seek(0,true);
                        break;
                    case BUTTON_PALY_ID:
                    	if (mediaPlayer.isPlaying()) {
                        	pause();
                        } else {
                        	replay();
                        }
                        updateNotification();
                    	break;
                    case BUTTON_NEXT_ID:
                    	currentPosition++;
    					if(currentPosition > MusicFragment.playMp3List.size() - 1) {	//变为第一首的位置继续播放
    						currentPosition = 0;
    					}
    					Intent sendIntent2 = new Intent("nowPlayingChanged");
    					sendIntent2.putExtra("currentPosition", currentPosition);
    					// 发送广播，更新newsfragment和newsdetailfarment显示
    					sendBroadcast(sendIntent2);
    					seek(0,true);
                    	break;
                    case BUTTON_CLOSE_ID:
                    	stop();
            			cancleNotification();
                    	break;
                    default:
                    	break;
                }
            }
        }
    }

}

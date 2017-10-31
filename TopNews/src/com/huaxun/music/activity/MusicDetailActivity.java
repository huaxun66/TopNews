package com.huaxun.music.activity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.huaxun.R;
import com.huaxun.db.DataDB;
import com.huaxun.fragment.MusicFragment;
import com.huaxun.music.Constant;
import com.huaxun.music.LrcGesture;
import com.huaxun.music.LrcProcess;
import com.huaxun.music.LrcView;
import com.huaxun.music.PlayerService;
import com.huaxun.music.bean.LrcContent;
import com.huaxun.music.bean.Mp3Info;
import com.huaxun.tool.BaseTools;
import com.huaxun.tool.Settings;
import com.huaxun.utils.ImageUtil;
import com.huaxun.utils.MediaUtil;
import com.huaxun.utils.Util;

public class MusicDetailActivity extends Activity {
	private Context context;
	private DataDB dataDB;
	private RelativeLayout music_detail_rl;
	private Button topBack;
	private Button musicVolume;
	private TextView musicTitle;
	private TextView musicArtist;
	private RelativeLayout ll_player_voice;
	private SeekBar sb_player_voice;		//控制音量大小
	private ImageView showLrc_IV;
	private LrcView lrcShowView;    // 自定义歌词视图
	private ImageView iv_music_ablum;
	private ImageView iv_music_ablum_reflection;
	private SeekBar music_progressBar;
	private TextView current_progress;
	private TextView final_progress;
	private Button play_mode;
	private Button play_music;
	private Button next_music;
	private Button previous_music;
	private Button play_list;
	
	private int currentPosition;
	private PopupWindow playListPopupWindow;
	private PlayListPopupAdapter playListPopupAdapter;
	private PopupWindow playModePopupWindow;
	
	private int currentMode;      //0:随机 1：全部 2：单曲
	private String[] playModeList = {"随机播放","全部循环","单曲循环"};
	private int[] playModeDrawable = {R.drawable.shuffle,R.drawable.repeat_all,R.drawable.repeat_current};
	
	private boolean showLrc = false;  //歌词是否显示
	public static boolean noLrcFlag = false; //没有Lrc歌词
	private LrcProcess mLrcProcess;	//歌词处理
	private List<LrcContent> lrcList = new ArrayList<LrcContent>(); //存放歌词列表对象
	private int index = 0;			//歌词检索值
	private int currentTime;		//当前播放进度
	private int duration;			//播放长度
	
	private AudioManager am;		//音频管理引用，提供对音频的控制
	int currentVolume;				//当前音量
	int maxVolume;					//最大音量
	
	// 音量面板显示和隐藏动画
	private Animation showVoicePanelAnimation;
	private Animation hiddenVoicePanelAnimation;
	
	/** 请求CODE */
	public final static int SKINREQUEST = 1;
	/** 调整返回的RESULTCODE1 */
	public final static int SKINRESULT = 2;
	private Settings mSetting;			//设置引用
	
	private final int updatePlayProcess = 1;
	public Handler processHandler = new Handler() {
        public void handleMessage(Message msg) {
        	super.handleMessage(msg);
        	switch (msg.what) {
        	case updatePlayProcess:
        		currentTime = PlayerService.mediaPlayer.getCurrentPosition();
        		music_progressBar.setProgress(currentTime);
        		current_progress.setText(MediaUtil.getDurationStr((int)(currentTime/1000)));
        		processHandler.sendEmptyMessageDelayed(updatePlayProcess, 1000);
        		break;
        	}
        }
	};
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Util.setTranslucentStatus(this, true);
		context = MusicDetailActivity.this;
		dataDB = DataDB.getInstance(context);
		mSetting = new Settings(this, false);
		setContentView(R.layout.music_detail_layout);
		initView();
		setViewOnclickListener();
		initPlayListPopWindow();
		initPlayModePopWindow();
		
		Bundle bundle = getIntent().getExtras();
		currentPosition = bundle.getInt("currentPosition");
		BaseTools.showlog("detailActivity中 currentPosition="+currentPosition);
		updateDetailDisplay(currentPosition);
		
		//音量调节面板显示和隐藏的动画
		showVoicePanelAnimation = AnimationUtils.loadAnimation(context, R.anim.push_up_in);
		hiddenVoicePanelAnimation = AnimationUtils.loadAnimation(context, R.anim.push_up_out);
		//获得系统音频管理服务对象
		am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
		maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		sb_player_voice.setMax(maxVolume);
		sb_player_voice.setProgress(currentVolume);		
		
		nowPlayingChangeReceiver = new NowPlayingChangeReceiver();  //当前播放的歌曲改变
		IntentFilter mFilter = new IntentFilter("nowPlayingChanged");
		context.registerReceiver(nowPlayingChangeReceiver, mFilter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		setIconEnable(menu, true);   //在>=4.0版的手机上运行时，Icon无法显示
		/*
         * add()方法的四个参数，依次是：
         * 1、组别，如果不分组的话就写Menu.NONE, 
         * 2、Id，这个很重要，Android根据这个Id来确定不同的菜单
         * 3、顺序，那个菜单现在在前面由这个参数的大小决定
         * 4、文本，菜单的显示文本
         */

        menu.add(Menu.NONE, Menu.FIRST + 1, 1, "换肤").setIcon(R.drawable.btn_menu_skin);

        // setIcon()方法为菜单设置图标，这里使用的是系统自带的图标，同学们留意一下,以android.R开头的资源是系统提供的，我们自己提供的资源是以R开头的

        menu.add(Menu.NONE, Menu.FIRST + 2, 2, "退出").setIcon(R.drawable.btn_menu_exit);
        return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		 case Menu.FIRST + 1:
			 Intent intent = new Intent(MusicDetailActivity.this,SkinSettingActivity.class);
			 startActivityForResult(intent, SKINREQUEST);
			 overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			 break;
		 case Menu.FIRST + 2:
			 finish();
	         overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
			 break;
		}
		return false;
	}
	
	//enable为true时，菜单添加图标有效，enable为false时无效。4.0系统默认无效    
	private void setIconEnable(Menu menu, boolean enable)    
	{    
	    try     
	    {    
	        Class<?> clazz = Class.forName("com.android.internal.view.menu.MenuBuilder");    
	        Method m = clazz.getDeclaredMethod("setOptionalIconsVisible", boolean.class);    
	        m.setAccessible(true);    	            
	        //MenuBuilder实现Menu接口，创建菜单时，传进来的menu其实就是MenuBuilder对象(java的多态特征)    
	        m.invoke(menu, enable);    
	            
	    } catch (Exception e)     
	    {    
	        e.printStackTrace();    
	    }    
	}    
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case SKINREQUEST:
			if(resultCode == SKINRESULT){
				music_detail_rl.setBackgroundResource(mSetting.getCurrentSkinResId());
			}
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	protected void onDestroy() {
		super.onDestroy();
		processHandler.removeMessages(updatePlayProcess);
		processHandler.removeCallbacks(mRunnable);
	}
	
	/**
	 * 从界面上根据id获取按钮
	 */
	private void initView() {
		music_detail_rl = (RelativeLayout) findViewById(R.id.music_detail_rl);
		music_detail_rl.setBackgroundResource(mSetting.getCurrentSkinResId());
		topBack = (Button) findViewById(R.id.topBack);
		musicVolume = (Button) findViewById(R.id.musicVolume);
		musicTitle = (TextView) findViewById(R.id.musicTitle);
		musicArtist = (TextView) findViewById(R.id.musicArtist);
		ll_player_voice = (RelativeLayout) findViewById(R.id.ll_player_voice);
		sb_player_voice = (SeekBar) findViewById(R.id.sb_player_voice);
		iv_music_ablum = (ImageView) findViewById(R.id.iv_music_ablum);
		iv_music_ablum_reflection = (ImageView) findViewById(R.id.iv_music_ablum_reflection);
		showLrc_IV = (ImageView) findViewById(R.id.showLrc_IV);
		lrcShowView = (LrcView) findViewById(R.id.lrcShowView);
		music_progressBar = (SeekBar) findViewById(R.id.music_progressBar);
		current_progress = (TextView) findViewById(R.id.current_progress);
		final_progress = (TextView) findViewById(R.id.final_progress);
		play_mode = (Button) findViewById(R.id.play_mode);
		play_music = (Button) findViewById(R.id.play_music);
		next_music = (Button) findViewById(R.id.next_music);
		previous_music = (Button) findViewById(R.id.previous_music);
		play_list = (Button) findViewById(R.id.play_list);
	}
	
	/**
	 * 给每一个按钮设置监听器
	 */
	private void setViewOnclickListener() {
		ViewOnclickListener ViewOnClickListener = new ViewOnclickListener();
		topBack.setOnClickListener(ViewOnClickListener);
		musicVolume.setOnClickListener(ViewOnClickListener);
		showLrc_IV.setOnClickListener(ViewOnClickListener);
		sb_player_voice.setOnSeekBarChangeListener(new SeekBarChangeListener());
		music_progressBar.setOnSeekBarChangeListener(new SeekBarChangeListener());
		play_mode.setOnClickListener(ViewOnClickListener);
		play_music.setOnClickListener(ViewOnClickListener);
		next_music.setOnClickListener(ViewOnClickListener);
		previous_music.setOnClickListener(ViewOnClickListener);
		play_list.setOnClickListener(ViewOnClickListener);
		lrcShowView.setLongClickable(true);
		lrcShowView.setOnTouchListener(new LrcGesture(this));
	}
	
	/**
	 * 更新detail里面的各项显示
	 */
	private void updateDetailDisplay(int position) {
		Mp3Info mp3Info = MusicFragment.playMp3List.get(position);
		duration = (int)(mp3Info.getDuration());
		if (PlayerService.mediaPlayer!=null) {
			currentTime = PlayerService.mediaPlayer.getCurrentPosition();
		}
		music_progressBar.setMax(duration);
		music_progressBar.setProgress(currentTime);
		final_progress.setText(MediaUtil.getDurationStr((int)(duration/1000)));
		current_progress.setText(MediaUtil.getDurationStr((int)(currentTime/1000)));
		musicTitle.setText(mp3Info.getTitle());
		musicArtist.setText(mp3Info.getArtist());
		showArtwork(mp3Info);		//显示专辑封面
		if (PlayerService.mediaPlayer!=null && PlayerService.mediaPlayer.isPlaying()) {
			play_music.setBackgroundResource(R.drawable.player_pause);
			processHandler.sendEmptyMessage(updatePlayProcess);  //更新播放进度条
		} else {
			play_music.setBackgroundResource(R.drawable.player_play);
		}
		SharedPreferences sharedPreferences = context.getSharedPreferences("musicKey",Context.MODE_PRIVATE);
		String mode = sharedPreferences.getString("playMode", "0");
		currentMode = Integer.parseInt(mode);
		play_mode.setBackgroundResource(playModeDrawable[currentMode]);   //更新播放模式
		noLrcFlag = false;
		processHandler.removeCallbacks(mRunnable);
		if (showLrc == true) {
			initLrc(mp3Info.getDisplayName());  //加载新的歌词	
		}
	}
	
	/**
	 * 显示专辑封面
	 */
	private void showArtwork(Mp3Info mp3Info) {
		Bitmap bm = MediaUtil.getArtwork(this, mp3Info.getId(), mp3Info.getAlbumId(), true, false);
		//切换播放时候专辑图片出现透明效果
		Animation albumanim = AnimationUtils.loadAnimation(MusicDetailActivity.this, R.anim.album_replace);
		//开始播放动画效果
		iv_music_ablum.startAnimation(albumanim);
		if(bm != null) {
			iv_music_ablum.setImageBitmap(bm);	//显示专辑封面图片
			iv_music_ablum_reflection.setImageBitmap(ImageUtil.createReflectionBitmapForSingle(bm));	//显示倒影
		} else {
			bm = MediaUtil.getDefaultArtwork(this, false);
			iv_music_ablum.setImageBitmap(bm);	//显示专辑封面图片
			iv_music_ablum_reflection.setImageBitmap(ImageUtil.createReflectionBitmapForSingle(bm));	//显示倒影
		}	
	}
	
	private class ViewOnclickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.topBack:
				 finish();
		         overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
				break;
			case R.id.musicVolume:
				voicePanelAnimation();
				break;	
			case R.id.play_mode:
				if (playModePopupWindow.isShowing()){
					playModePopupWindow.dismiss();
				}else{
					playModePopupWindow.showAtLocation(((Activity) context).findViewById(R.id.play_list),Gravity.BOTTOM|Gravity.LEFT, 0, Util.dip2px(context, 100));
				}
				break;	
			case R.id.play_music:
				if (PlayerService.mediaPlayer != null) {
					if (PlayerService.mediaPlayer.isPlaying()) {
						pauseMusic();
					} else {
						continueMusic();
					}
				} else {
					playMusic(currentPosition);   //service还没启动
				}
				break;
			case R.id.previous_music: // 上一首歌曲
					if (MusicFragment.playMp3List.size() > 0) {
						if (currentPosition == 0) {
							currentPosition = MusicFragment.playMp3List.size() - 1;
						} else {
							currentPosition--;
						}
						playMusic(currentPosition);
					}
				break;
			case R.id.next_music: // 下一首歌曲
				if (MusicFragment.playMp3List.size() > 0) {
					if (currentPosition == MusicFragment.playMp3List.size() - 1) {
						currentPosition = 0;
					} else {
						currentPosition++;
					}
					playMusic(currentPosition);	
				}
				break;
			case R.id.play_list:  //播放列表	
				if (playListPopupWindow.isShowing()){
					playListPopupWindow.dismiss();
				}else{
					playListPopupWindow.showAtLocation(((Activity) context).findViewById(R.id.play_list),Gravity.BOTTOM|Gravity.RIGHT, 0, Util.dip2px(context, 100));
				}
				break;
			case R.id.showLrc_IV:
				if (showLrc) {
					showLrc = false;
					showLrc_IV.setBackgroundResource(R.drawable.lrc_show);
					processHandler.removeCallbacks(mRunnable);
					lrcShowView.setIndex(-1);  //将index设为-1，不会绘出歌词
					lrcShowView.invalidate();
				} else {
					showLrc = true;
					showLrc_IV.setBackgroundResource(R.drawable.lrc_hide);
					initLrc(MusicFragment.playMp3List.get(currentPosition).getDisplayName());
				}
				break;
			}
		}
	}
	
	public void onBackPressed() {
		 finish();
         overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}
	
	private class SeekBarChangeListener implements OnSeekBarChangeListener {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			switch(seekBar.getId()) {
			case R.id.music_progressBar:
				if (fromUser==true && PlayerService.mediaPlayer!=null && PlayerService.mediaPlayer.isPlaying()) {
					Intent intent = new Intent();
					intent.putExtra("MSG", Constant.PlayerMsg.PROGRESS_CHANGE);
					intent.putExtra("progress", progress);
					intent.setClass(context, PlayerService.class);
					intent.putExtra("needPlay", true);
					context.startService(intent);
				}			
				break;
			case R.id.sb_player_voice:
				// 设置音量
				am.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
				break;
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}
	}
	
	private void playMusic(final int currentPosition) {
		if (MusicFragment.playMp3List!=null) {
			Intent intent = new Intent();
			intent.putExtra("position", currentPosition);
			intent.putExtra("MSG", Constant.PlayerMsg.PLAY_MSG);
			intent.setClass(context, PlayerService.class);
			context.startService(intent);
			//自己更新播放界面
			processHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					updateDetailDisplay(currentPosition);
				}				
			}, 500);			
			//发广播通知musicfragment当前播放歌曲改变，要刷新界面
			Intent it = new Intent("nowPlayingChanged");
			it.putExtra("currentPosition", currentPosition);
			sendBroadcast(it);
		}
	}
	
	private void pauseMusic() {
		play_music.setBackgroundResource(R.drawable.player_play);
		Intent intent = new Intent();
		intent.putExtra("MSG", Constant.PlayerMsg.PAUSE_MSG);
		intent.setClass(context, PlayerService.class);
		context.startService(intent);	
		processHandler.removeMessages(updatePlayProcess); //停止更新进度条
		processHandler.removeCallbacks(mRunnable);  //停止更新歌词进度
	}
	
	private void continueMusic() {
		play_music.setBackgroundResource(R.drawable.player_pause);
		Intent intent = new Intent();
		intent.putExtra("MSG", Constant.PlayerMsg.CONTINUE_MSG);
		intent.setClass(context, PlayerService.class);
		context.startService(intent);					
		processHandler.sendEmptyMessageDelayed(updatePlayProcess, 500);  //更新播放进度条
		processHandler.post(mRunnable);   //更新歌词进度
	}
	
	//控制显示音量控制面板的动画
	private void voicePanelAnimation() {
		if(ll_player_voice.getVisibility() == View.GONE) {
			ll_player_voice.startAnimation(showVoicePanelAnimation);
			ll_player_voice.setVisibility(View.VISIBLE);
		}
		else{
			ll_player_voice.startAnimation(hiddenVoicePanelAnimation);
			ll_player_voice.setVisibility(View.GONE);
		}
	}
	
	private void changePlayMode(int mode) {
		SharedPreferences sharedPreferences = context.getSharedPreferences("musicKey",Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString("playMode", String.valueOf(mode));
		editor.commit();
		Intent intent = new Intent("modeChange");
		intent.putExtra("currentMode", mode);		
		sendBroadcast(intent);	
	}
	
	/**
	 * 初始化歌词配置
	 */
	public void initLrc(String displayName) {
		mLrcProcess = new LrcProcess();
		//读取歌词文件
		mLrcProcess.readLRC(displayName);
		//传回处理后的歌词文件
		lrcList = mLrcProcess.getLrcList();
		lrcShowView.setmLrcList(lrcList);
		//切换带动画显示歌词
		lrcShowView.setAnimation(AnimationUtils.loadAnimation(this, R.anim.alpha_z));
		if (lrcList.size() == 0) {   //没有读取到Lrc文件
            noLrcFlag = true;
            lrcShowView.invalidate();
            return;
		}
		if (PlayerService.mediaPlayer!=null && PlayerService.mediaPlayer.isPlaying()) {
			processHandler.post(mRunnable);
		}		
	}
	
	Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			lrcShowView.setIndex(lrcIndex());
			lrcShowView.invalidate();
			processHandler.postDelayed(mRunnable, 1000);
		}
	};
	
	/**
	 * 根据时间获取歌词显示的索引值
	 * @return
	 */
	public int lrcIndex() {
		if(PlayerService.mediaPlayer!=null) {
			currentTime = PlayerService.mediaPlayer.getCurrentPosition();
			duration = PlayerService.mediaPlayer.getDuration();
		}
		BaseTools.showlog("currentTime="+currentTime);
		if(currentTime < duration) {
			for (int i = 0; i < lrcList.size(); i++) {
				if (i < lrcList.size() - 1) {
					if (currentTime < lrcList.get(i).getLrcTime() && i == 0) {
						index = i;
					}
					if (currentTime >= lrcList.get(i).getLrcTime() && currentTime < lrcList.get(i + 1).getLrcTime()) {
						index = i;
					}
				}
				if (i == lrcList.size() - 1 && currentTime >= lrcList.get(i).getLrcTime()) {
					index = i;
				}
			}
		}
		return index;
	}
	
	//上下滑动屏幕时，LRC歌词重绘，形成歌词滑动效果
	public void scrollLrc(float distanceY) {
		lrcShowView.showprogress = true;
		lrcShowView.drifty += distanceY;
		lrcShowView.invalidate();//更新视图
	}
	
	//上下滑动屏幕后手指抬起，歌曲进度调整到middlelineindex位置,切当前播放index变成middlelineindex
	public void seekMusic() {
		if (lrcShowView.mLrcList.isEmpty()) {
			return;
		}
		Intent intent = new Intent();
		intent.putExtra("MSG", Constant.PlayerMsg.PROGRESS_CHANGE);
		intent.putExtra("progress", lrcShowView.mLrcList.get(lrcShowView.middleLineIndex).getLrcTime());
		intent.setClass(context, PlayerService.class);
		if (PlayerService.mediaPlayer!=null && PlayerService.mediaPlayer.isPlaying()) {
			intent.putExtra("needPlay", true);
		} else {
			intent.putExtra("needPlay", false);
		}
		context.startService(intent);
		processHandler.postDelayed(new Runnable(){
			@Override
			public void run() {
				lrcShowView.setIndex(lrcIndex());
				lrcShowView.showprogress = false;
				lrcShowView.drifty = 0;
				lrcShowView.invalidate();//更新视图
			}}, 200);		
	}
	
	private void initPlayListPopWindow() {
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
        View view = inflater.inflate(R.layout.play_list_popupwindow_layout, null);
        TextView play_list_tv = (TextView)view.findViewById(R.id.play_list_tv);
        ImageView delete_all_iv = (ImageView)view.findViewById(R.id.delete_all_iv);
        ListView listView = (ListView)view.findViewById(R.id.listViewID);
        play_list_tv.setTextColor(Color.WHITE);
        playListPopupAdapter = new PlayListPopupAdapter();
        listView.setAdapter(playListPopupAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				currentPosition = position;
				playMusic(currentPosition);
				playListPopupWindow.dismiss();
			}
		});
        delete_all_iv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				MusicFragment.playMp3List.clear();
				dataDB.deleteAllMp3Info();   //数据库删除所有
				if(PlayerService.mediaPlayer.isPlaying()) {
					pauseMusic();
				}
				playListPopupAdapter.notifyDataSetChanged();
				//发广播通知musicfragment播放列表有改变
				Intent intent = new Intent("playListChanged");
				sendBroadcast(intent);	
				Toast.makeText(context, "清空播放列表完毕！", Toast.LENGTH_SHORT).show();
			}       	
        });
        
        playListPopupWindow = new PopupWindow(view);
        playListPopupWindow.setWidth(Util.dip2px(context, 250));
        playListPopupWindow.setHeight(Util.dip2px(context, 300));
        Drawable drawable = getResources().getDrawable(R.drawable.pop_window_bg);
        playListPopupWindow.setBackgroundDrawable(drawable);		
        playListPopupWindow.setFocusable(true);
        playListPopupWindow.setOutsideTouchable(true);
        playListPopupWindow.setAnimationStyle(R.style.playlistPopupStyle);
	}
	
	private class PlayListPopupAdapter extends BaseAdapter{		
		private LayoutInflater mInflater;
		private PlayListPopupAdapter(){
			mInflater = LayoutInflater.from(context);
		}
		
		@Override
		public int getCount() {
			return MusicFragment.playMp3List.size();
		}

		@Override
		public Object getItem(int position) {
			return MusicFragment.playMp3List.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			convertView = mInflater.inflate(R.layout.play_list_popupwindow_listitem, null);
			ImageView mImageView = (ImageView) convertView.findViewById(R.id.imageID);
			TextView mTextView = (TextView) convertView.findViewById(R.id.titleID);
			mTextView.setText((position+1)+" "+MusicFragment.playMp3List.get(position).getTitle());
			if (currentPosition == position) {
				mTextView.setTextColor(Color.BLUE);
			} else {
				mTextView.setTextColor(Color.WHITE);
			}
			mImageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Toast.makeText(context, "已将("+MusicFragment.playMp3List.get(position).getTitle()+")移除！", Toast.LENGTH_SHORT).show();
					dataDB.deleteMp3Info(MusicFragment.playMp3List.get(position).getTitle());
					MusicFragment.playMp3List.remove(position);
					if (position < currentPosition) {
						currentPosition--;          //删除位置在当前播放位置之前，播放位置减1      
					} else if (position == currentPosition) {
						if (position == MusicFragment.playMp3List.size()) {   //删除当前播放歌曲，是最后一首时，播放第一首
							currentPosition = 0;
						}
						if(PlayerService.mediaPlayer.isPlaying()) {  //删除当前播放歌曲，非最后一首时，播放下一首
							playMusic(currentPosition);            
						} 
					}
					notifyDataSetChanged();
					//发广播通知musicfragment播放列表有改变
					Intent intent = new Intent("playListChanged");
					sendBroadcast(intent);	
				}				
			});
			return convertView;
		}	
	}
	
	private void initPlayModePopWindow() {
		List<Map<String,Object>> listItems = new ArrayList<Map<String,Object>>();
		for (int i=0; i<3; i++) {
			Map<String,Object> listitem = new HashMap<String,Object>();
			listitem.put("image", playModeDrawable[i]);
			listitem.put("title", playModeList[i]);
			listItems.add(listitem);
		}
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
        View view = inflater.inflate(R.layout.play_mode_popupwindow_layout, null);
        ListView listView = (ListView)view.findViewById(R.id.listViewID);
        SimpleAdapter playModePopupAdapter = new SimpleAdapter(context,listItems,R.layout.play_mode_popupwindow_listitem,
        		new String[]{"image","title"},new int[]{R.id.imageID,R.id.titleID});
        listView.setAdapter(playModePopupAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {				
				currentMode = position;
				play_mode.setBackgroundResource(playModeDrawable[position]);
				playModePopupWindow.dismiss();
				Toast.makeText(context, playModeList[position], Toast.LENGTH_SHORT).show();
			    changePlayMode(currentMode); //发送广播通知playerservice模式改变
			}
		});
        
        playModePopupWindow = new PopupWindow(view);
        playModePopupWindow.setWidth(Util.dip2px(context, 130));
        playModePopupWindow.setHeight(Util.dip2px(context, 121));
        Drawable drawable = getResources().getDrawable(R.drawable.pop_window_bg);
        playModePopupWindow.setBackgroundDrawable(drawable);		
        playModePopupWindow.setFocusable(true);
        playModePopupWindow.setOutsideTouchable(true);
        playModePopupWindow.setAnimationStyle(R.style.playlistPopupStyle);
	}
	
	private NowPlayingChangeReceiver nowPlayingChangeReceiver;
	class NowPlayingChangeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			currentPosition = intent.getIntExtra("currentPosition", 0);
			updateDetailDisplay(currentPosition);
			playListPopupAdapter.notifyDataSetChanged();
		}
	}
}

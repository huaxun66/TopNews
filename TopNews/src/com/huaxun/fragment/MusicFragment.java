package com.huaxun.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager.LayoutParams;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.huaxun.R;
import com.huaxun.app.AppApplication;
import com.huaxun.base.BaseFragment;
import com.huaxun.db.DataDB;
import com.huaxun.dialog.LoadingDialog;
import com.huaxun.download.DownloadInfo;
import com.huaxun.download.DownloadManager;
import com.huaxun.download.DownloadService;
import com.huaxun.music.Constant;
import com.huaxun.music.PlayerService;
import com.huaxun.music.activity.MusicDetailActivity;
import com.huaxun.music.bean.Mp3Info;
import com.huaxun.music.util.OnLoadSearchFinishListener;
import com.huaxun.music.util.SearchUtils;
import com.huaxun.news.activity.DownloadListActivity;
import com.huaxun.tool.BaseTools;
import com.huaxun.tool.Options;
import com.huaxun.utils.FileUtil;
import com.huaxun.utils.HttpUtil;
import com.huaxun.utils.MediaUtil;
import com.huaxun.utils.NetUtil;
import com.huaxun.utils.Util;
import com.huaxun.view.SwipeListView.BaseSwipeListViewListener;
import com.huaxun.view.SwipeListView.SwipeListView;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.http.HttpHandler.State;
import com.lidroid.xutils.util.LogUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

public class MusicFragment extends BaseFragment {
	private Context context;
	private DataDB dataDB;
	private View view;
	private RelativeLayout musicLayout;
	private RelativeLayout play_control_layout;
	private SwipeListView music_list;
	private ImageView add_all;
	private EditText edt_search;
	private TextView bt_search;
	private ImageView download_list;
	private ImageView music_album;
	private SeekBar music_progressBar;
	private TextView music_title;
	private TextView music_artist;
	private Button previous_music;
	private Button play_music;
	private Button next_music;
	private Button play_list;
	private List<Mp3Info> mp3Infos = new ArrayList<Mp3Info>();
	public static List<Mp3Info> playMp3List = new ArrayList<Mp3Info>();
	public List<Mp3Info> searchMp3List = new ArrayList<Mp3Info>();
	private int currentPosition = 0;
	private int operatorPosition = 0;
	private MusicListAdapter musicListAdapter;
	private PopupAdapter popupAdapter;
	private SearchPopupAdapter searchPopupAdapter;
	private PopupWindow popupWindow;
	private PopupWindow searchPopupWindow;
	/** 是否在移动，由于这边是动画结束后才进行的数据更替，设置这个限制为了避免操作太频繁造成的数据错乱。 */
	private boolean isMove = false;
	private LoadingDialog loadingDialog;
	private DownloadManager downloadManager;
	
	private final int updatePlayProcess = 1;
	private final int updateMusicList =2;
	public Handler processHandler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case updatePlayProcess:
				if (PlayerService.mediaPlayer != null) {
					music_progressBar.setProgress(PlayerService.mediaPlayer.getCurrentPosition());
					processHandler.sendEmptyMessageDelayed(updatePlayProcess, 1000);
				}
				break;
			case updateMusicList:
				Mp3Info music = (Mp3Info)msg.obj;
				DownloadInfo info = downloadManager.getDownloadInfoByUrl(music.getMusicUrl());
                if (info.getState() == State.FAILURE || info.getState() == State.CANCELLED) {
                	Toast.makeText(context, "歌曲("+music.getTitle()+")下载中断!", Toast.LENGTH_SHORT).show();
                } else if (info.getState() == State.SUCCESS) {
                	Toast.makeText(context, "歌曲("+music.getTitle()+")下载完成!", Toast.LENGTH_SHORT).show();        	
                	String path = FileUtil.getMusicPath() + File.separator + music.getTitle() + ".mp3";
                	music.setPath(path);
                	music.setDisplayName(FileUtil.getFileName(path));
        			music.setSize(FileUtil.getFileSize(new File(path)));
                	MediaPlayer mp = MediaPlayer.create(context, Uri.parse(path));
        			music.setDuration(mp.getDuration());
                	mp3Infos.add(music);
                    musicListAdapter.notifyDataSetChanged();
                	//程序通过发送下面的Intent启动MediaScanner服务扫描指定的文件,将文件添加到多媒体数据库
            		context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(path))));
                } else {
                	Message msg1 = processHandler.obtainMessage();
                	msg1.obj = music;
                    msg1.what = updateMusicList;
                    processHandler.sendMessageDelayed(msg1, 1000);
                }
				break;
			}
		}
	};
	
	public void onCreate(Bundle savedInstanceState) {
		this.context = this.getActivity();
		dataDB = DataDB.getInstance(context);
		mp3Infos = MediaUtil.getMp3Infos(context); // 获取所有歌曲对象集合
		playMp3List = dataDB.selectAllMp3Info();  //获取播放列表歌曲对象集合
		//获取上次播放的位置
		SharedPreferences sharedPreferences = context.getSharedPreferences("musicKey",Context.MODE_PRIVATE);
		String position = sharedPreferences.getString("currentPosition", "0");
		currentPosition = Integer.parseInt(position);
		initPopWindow();
		initSearchPopWindow();
		downloadManager = DownloadService.getDownloadManager(context);
		
		playListChangeReceiver = new PlayListChangeReceiver();   //播放列表改变，增加或删除操作
		IntentFilter mFilter = new IntentFilter("playListChanged");
		context.registerReceiver(playListChangeReceiver, mFilter);
		
		nowPlayingChangeReceiver = new NowPlayingChangeReceiver();  //当前播放的歌曲改变
		IntentFilter mFilter2 = new IntentFilter("nowPlayingChanged");
		context.registerReceiver(nowPlayingChangeReceiver, mFilter2);
	
		// 添加来电监听事件
		TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE); // 获取系统服务
		telManager.listen(new MobliePhoneStateListener(),PhoneStateListener.LISTEN_CALL_STATE);
		
		super.onCreate(savedInstanceState);
	}

	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.music_layout, container, false);
		initView();
		setViewOnclickListener();
		loadingDialog = new LoadingDialog(context);
		updateMusicDisplay(currentPosition);  //歌曲信息显示仍然保持上次退出时状态	
//		registerForContextMenu(music_list);   //创建上下文菜单
		
		musicListAdapter = new MusicListAdapter(context);
		music_list.setAdapter(musicListAdapter);
		
//		music_list.setOnDeleteListener(new OnDeleteListener(){
//			@Override
//			public void onDelete(int index) {
//				deleteFileFromList(index);
//			}			
//		});
		
		music_list.setSwipeListViewListener(new BaseSwipeListViewListener() {
	            @Override
	            public void onOpened(int position, boolean toRight) {}
	            @Override
	            public void onClosed(int position, boolean fromRight) {}
	            @Override
	            public void onListChanged() {}
	            @Override
	            public void onMove(int position, float x) {}
	            @Override
	            public void onStartOpen(int position, int action, boolean right) {
	            	BaseTools.showlog(String.format("swipe-onStartOpen %d - action %d", position, action));
	            	operatorPosition = position;
	            }
	            @Override
	            public void onStartClose(int position, boolean right) {
	            	BaseTools.showlog(String.format("swipe-onStartClose %d", position));
	            }
	            @Override
	            public void onClickFrontView(int position) {
	                BaseTools.showlog(String.format("swipe-onClickFrontView %d", position));
	                if(isMove){
			            return;
			        }
			        final ImageView moveImageView = new ImageView(context);				
			        moveImageView.setImageResource(R.drawable.music);
			        View view1 = Util.getViewByPosition(position, music_list);
					ImageView addImage = (ImageView) view1.findViewById(R.id.add_music);
					final int[] startLocation = new int[2];
					addImage.getLocationInWindow(startLocation);
					final int[] endLocation = new int[2];
					music_album.getLocationInWindow(endLocation);
					MoveAnim(moveImageView, startLocation , endLocation, position, true);	
	            }

	            @Override
	            public void onClickBackView(int position) {
	            	BaseTools.showlog(String.format("swipe-onClickBackView %d", position));
	            }
	            @Override
	            public void onDismiss(int[] reverseSortedPositions) {
	            	BaseTools.showlog("onDismiss");
	            	deleteFileFromList(operatorPosition);
	            }
	        });
		
		music_progressBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser==true && PlayerService.mediaPlayer!=null && PlayerService.mediaPlayer.isPlaying()) {
					Intent intent = new Intent();
					intent.putExtra("MSG", Constant.PlayerMsg.PROGRESS_CHANGE);
					intent.putExtra("progress", progress);
					intent.putExtra("needPlay", true);
					intent.setClass(context, PlayerService.class);
					context.startService(intent);	
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}			
		});
		return view;
	}
	
	/** 初始化layout控件*/
	private void initView() {
		add_all = (ImageView) view.findViewById(R.id.add_all);
		edt_search = (EditText) view.findViewById(R.id.edt_search);
		bt_search = (TextView) view.findViewById(R.id.bt_search);
		download_list = (ImageView) view.findViewById(R.id.download_list);
		musicLayout =  (RelativeLayout) view.findViewById(R.id.musicLayout);
		play_control_layout = (RelativeLayout) view.findViewById(R.id.play_control_layout);
		music_list = (SwipeListView) view.findViewById(R.id.music_list);
		music_album = (ImageView) view.findViewById(R.id.music_album);
		music_progressBar = (SeekBar) view.findViewById(R.id.music_progressBar);
		music_title = (TextView) view.findViewById(R.id.music_title);
		music_artist = (TextView) view.findViewById(R.id.music_artist);
		previous_music = (Button) view.findViewById(R.id.previous_music);
		play_music = (Button) view.findViewById(R.id.play_music);
		next_music = (Button) view.findViewById(R.id.next_music);
		play_list = (Button) view.findViewById(R.id.play_list);		
	}
	
	/**
	 * 给每一个按钮设置监听器
	 */
	private void setViewOnclickListener() {
		ViewOnClickListener viewOnClickListener = new ViewOnClickListener();
		add_all.setOnClickListener(viewOnClickListener);
		bt_search.setOnClickListener(viewOnClickListener);
		download_list.setOnClickListener(viewOnClickListener);
		music_album.setOnClickListener(viewOnClickListener);
		previous_music.setOnClickListener(viewOnClickListener);
		play_music.setOnClickListener(viewOnClickListener);
		next_music.setOnClickListener(viewOnClickListener);
		play_list.setOnClickListener(viewOnClickListener);
	}
	
//	@Override
//	//onCreateContextMenu会在用户每一次长按View时被调用，而且View必须已经注册了上下文菜单
//	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
//		BaseTools.showlog("onCreateContextMenu");
//		Vibrator vibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
//		vibrator.vibrate(50); // 长按振动
//		menu.setHeaderTitle("文件操作");
//		menu.add(0, 1, Menu.NONE,"详细信息");
//		menu.add(0, 2, Menu.NONE,"从列表删除");
//		menu.add(0, 3, Menu.NONE,"永久删除");
//		super.onCreateContextMenu(menu, v, menuInfo);
//	}
//	
//	public boolean onContextItemSelected(MenuItem item) {
//	  // 得到当前被选中的item信息
//	  AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();	
//	  int position = menuInfo.position;
//	  switch(item.getItemId()) {
//	    case 1:
//	    	showMusicInfo(position);
//	        break;
//	    case 2:
//	    	deleteFileFromList(position);
//	    	break;
//	    case 3:
//	    	deleteFileForever(position);
//	    	break;
//	    default:
//	        return super.onContextItemSelected(item);
//	    }
//	    return true;
//	}
	
	//从MP3Infos中删除音乐文件
	private void deleteFileFromList(int index) {
		Toast.makeText(context, "已将("+mp3Infos.get(index).getTitle()+")文件从列表删除！", Toast.LENGTH_SHORT).show();					
		Mp3Info mp3Info = mp3Infos.get(index);
		mp3Infos.remove(mp3Info);
		if (playMp3List.contains(mp3Info)) {
			int position = playMp3List.indexOf(mp3Info);
			playMp3List.remove(mp3Info);
			dataDB.deleteMp3Info(mp3Info.getTitle());
			if (position < currentPosition) {
				currentPosition--;          //删除位置在当前播放位置之前，播放位置减1      
			} else if (position == currentPosition) {
				if (position == playMp3List.size()) {   //删除当前播放歌曲，是最后一首时，播放第一首
					currentPosition = 0;
				}
				if(PlayerService.mediaPlayer!=null && PlayerService.mediaPlayer.isPlaying()) {  //删除当前播放歌曲，非最后一首时，播放下一首
					if (playMp3List.size()==0) {
						pauseMusic();
					} else {
						playMusic(currentPosition);			
					}
				} 
			}
		}
		musicListAdapter.notifyDataSetChanged();
	}
	
	//永久删除音乐文件
	private void deleteFileForever(int index) {
		Mp3Info mp3Info = mp3Infos.get(index);
		String path = mp3Info.getPath();
		String title = mp3Info.getTitle();
		deleteFileFromList(index);	//从列表删除		
		FileUtil.deleteFile(path);  //永久删除
		//程序通过发送下面的Intent启动MediaScanner服务扫描指定的文件,将文件从多媒体数据库删除
		context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(path))));
		Toast.makeText(context, "已将("+title+")文件永久删除！", Toast.LENGTH_SHORT).show();					
	}
	
	/**
	 * 显示音乐详细信息
	 * 
	 * @param position
	 */
	private void showMusicInfo(int position) {
		Mp3Info mp3Info = mp3Infos.get(position);
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.music_info_layout, null);
		((TextView) view.findViewById(R.id.tv_song_title)).setText(mp3Info.getTitle());
		((TextView) view.findViewById(R.id.tv_song_artist)).setText(mp3Info.getArtist());
		((TextView) view.findViewById(R.id.tv_song_album)).setText(mp3Info.getAlbum());
		((TextView) view.findViewById(R.id.tv_song_filepath)).setText(mp3Info.getPath());
		((TextView) view.findViewById(R.id.tv_song_duration)).setText(MediaUtil.getDurationStr((int)(mp3Info.getDuration()/1000)));
		((TextView) view.findViewById(R.id.tv_song_format)).setText(FileUtil.getSuffix(mp3Info.getDisplayName()));
		((TextView) view.findViewById(R.id.tv_song_size)).setText(FileUtil.formatByteToMB(mp3Info.getSize()) + "MB");
		new AlertDialog.Builder(context).setTitle("歌曲详细信息:").setNeutralButton("确定", null).setView(view).create().show();
	}
	
	/**
	 * 电话监听器类
	 */
	private class MobliePhoneStateListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE: // 挂机状态
				continueMusic();
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:	//通话状态
			case TelephonyManager.CALL_STATE_RINGING:	//响铃状态
				pauseMusic();
				break;
			default:
				break;
			}
		}
	}
	
	private class ViewOnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.add_all:
				for (int i=0; i<mp3Infos.size(); i++) {
					Mp3Info mp3Info = mp3Infos.get(i);
					if (!playMp3List.contains(mp3Info)) {
						playMp3List.add(mp3Info);
						dataDB.addMp3Info(mp3Info); //添加到数据库
					}
				}
				Toast.makeText(context, "全部歌曲添加到播放列表", Toast.LENGTH_SHORT).show();
				musicListAdapter.notifyDataSetChanged();	
				break;
			case R.id.bt_search:
				if (!NetUtil.isNetworkAvailable(context)) {
					Toast.makeText(context, "请开启网络", Toast.LENGTH_SHORT).show();
					break;
				}
				final String keyword = edt_search.getText().toString();
				if (TextUtils.isEmpty(keyword)) {
					Toast.makeText(context, "请输入查询内容", Toast.LENGTH_SHORT).show();
					break;
				}
				InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
				if (imm != null) {
					imm.hideSoftInputFromWindow(edt_search.getWindowToken(), 0);
				}
				processHandler.postDelayed(new Runnable(){
					@Override
					public void run() {				
						loadingDialog.show();
						searchOnlineMusic();
					}}, 50);				
				break;
			case R.id.download_list:
				Intent intent = new Intent(context, DownloadListActivity.class);
				intent.putExtra("source", "MusicFragment");
                startActivity(intent); 
                ((Activity) context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				break;
			case R.id.music_album:
				if (playMp3List.size() > 0) {
					OpenMusicDetail(currentPosition);
				}
				break; 
			case R.id.previous_music:
				if (playMp3List.size() > 0) {
					if (currentPosition == 0) {
						currentPosition = playMp3List.size() - 1;
					} else {
						currentPosition--;
					}
					playMusic(currentPosition);
				}
				break; 
			case R.id.play_music:
				if (PlayerService.mediaPlayer != null) {
					if(PlayerService.mediaPlayer.isPlaying()) {
						pauseMusic();
					} else {
						continueMusic();
					}
				} else {
					playMusic(currentPosition);   //刚启动应用
				}						
				break; 
			case R.id.next_music:
				if (playMp3List.size() > 0) {
					if (currentPosition == playMp3List.size() - 1) {
						currentPosition = 0;
					} else {
						currentPosition++;
					}
					playMusic(currentPosition);	
				}
				break; 
			case R.id.play_list:
				if (popupWindow.isShowing()){
					popupWindow.dismiss();
				}else{
					popupWindow.showAtLocation(((Activity) context).findViewById(R.id.musicLayout),Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL,0, Util.dip2px(context, 120));
				}
				break;
			}
		}
	}
	
	
	private void searchOnlineMusic() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				SearchUtils.searchMusics(edt_search.getText().toString(), new OnLoadSearchFinishListener() {	
					@Override
					public void onLoadSucess(List<Mp3Info> musicList) {											
						searchMp3List = musicList;
						processHandler.post(new Runnable() {
							@Override
							public void run() {
								loadingDialog.dismiss();// 加载完成，取消进度条
								searchPopupWindow.showAtLocation(((Activity) context).findViewById(R.id.musicLayout),Gravity.CENTER, 0, Util.dip2px(context, 20));
							}							
						});						
					}
					@Override
					public void onLoadFail() {
						processHandler.post(new Runnable() {
							@Override
							public void run() {
								loadingDialog.dismiss();// 加载完成，取消进度条
								Toast.makeText(context ,"加载失败", Toast.LENGTH_SHORT).show();
							}							
						});						
					}
				});
			}
		}).start();		
	}
	
	private void downloadOnlineMusic(final Mp3Info music) {									
		Toast.makeText(context, "开始下载歌曲("+music.getTitle()+")", Toast.LENGTH_SHORT).show();
        String target = FileUtil.getMusicPath() + "/" + music.getTitle() + ".mp3";
        try {
            downloadManager.addNewDownload(music.getMusicUrl(),
            		music.getTitle(),
                    target,
                    true, // 如果目标文件存在，接着未完成的部分继续下载。服务器不支持RANGE时将从新下载。
                    false,// 如果从请求返回信息中获取到文件名，下载完成后自动重命名。
                    null);
        } catch (DbException e) {
            LogUtils.e(e.getMessage(), e);
        }
        
        //同时下载LRC歌词文件
        new Thread(new Runnable(){
			@Override
			public void run() {
				HttpUtil.saveFileToLocal(music.getLrcUrl(), FileUtil.getLrcPath() + "/" + music.getTitle() + ".lrc", "UTF-8");
			}      	
        }).start();
        
        //监听downloadInfo下载事件
        Message msg = new Message();
        msg.obj = music;
        msg.what = updateMusicList;
        processHandler.sendMessageDelayed(msg, 2000);
	}
	
	private void OpenMusicDetail(int currentPosition) {
		Intent intent = new Intent(context, MusicDetailActivity.class);
		intent.putExtra("currentPosition", currentPosition);
		startActivity(intent);
		((Activity) context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}	
	
	/**
	 * 获取点击的Item的对应View
	 * @param view
	 * @return
	 */
	private ImageView getView(View view) {
		view.destroyDrawingCache();
		view.setDrawingCacheEnabled(true);
		Bitmap cache = Bitmap.createBitmap(view.getDrawingCache());
		view.setDrawingCacheEnabled(false);
		ImageView iv = new ImageView(context);
		iv.setImageBitmap(cache);
		return iv;
	}
	
	private void MoveAnim(View moveView, int[] startLocation,int[] endLocation, final int position, final boolean needPlay) {
		int[] initLocation = new int[2];
		//获取传递过来的VIEW的坐标
		moveView.getLocationInWindow(initLocation);
		//得到要移动的VIEW,并放入对应的容器中
		final ViewGroup moveViewGroup = getMoveViewGroup();
		final View mMoveView = getMoveView(moveViewGroup, moveView, initLocation);
		//创建移动动画
		TranslateAnimation moveAnimation = new TranslateAnimation(
				startLocation[0], endLocation[0], startLocation[1],
				endLocation[1]);
		moveAnimation.setDuration(500L);//动画时间
		//动画配置
		AnimationSet moveAnimationSet = new AnimationSet(true);
		moveAnimationSet.setFillAfter(false);//动画效果执行完毕后，View对象不保留在终止的位置
		moveAnimationSet.addAnimation(moveAnimation);
		mMoveView.startAnimation(moveAnimationSet);
		moveAnimationSet.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				//开始移动
				isMove = true;
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				moveViewGroup.removeView(mMoveView);
				//移动结束
				isMove = false;
				Mp3Info mp3Info = mp3Infos.get(position);	
				if (needPlay==true) {
					if (playMp3List.contains(mp3Info)) {
						currentPosition = playMp3List.indexOf(mp3Info);
					} else {
						playMp3List.add(mp3Info);
						dataDB.addMp3Info(mp3Info); //添加到数据库
						currentPosition = playMp3List.size()-1;
					}
					playMusic(currentPosition);
				} else {
					if (!playMp3List.contains(mp3Info)) {
						playMp3List.add(mp3Info);
						dataDB.addMp3Info(mp3Info); //添加到数据库
					}
				}
				musicListAdapter.notifyDataSetChanged();
			}
		});
	}
	
	private void playMusic(int position) {
		if (playMp3List.size()!=0) {     //播放列表为空时不会触发播放
			play_music.setBackgroundResource(R.drawable.player_pause);
            updateMusicDisplay(position);
			Intent intent = new Intent();
			intent.putExtra("position", position);
			intent.putExtra("MSG", Constant.PlayerMsg.PLAY_MSG);
			intent.setClass(context, PlayerService.class);
			context.startService(intent);
		}
	}
	
	private void pauseMusic() {
		play_music.setBackgroundResource(R.drawable.player_play);
		Intent intent = new Intent();
		intent.putExtra("MSG", Constant.PlayerMsg.PAUSE_MSG);
		intent.setClass(context, PlayerService.class);
		context.startService(intent);	
		processHandler.removeMessages(updatePlayProcess); //停止更新进度条
	}
	
	private void continueMusic() {
		if (PlayerService.mediaPlayer!=null) {   //不然每次启动应用CALL_STATE_IDLE都会触发播放
			play_music.setBackgroundResource(R.drawable.player_pause);
			Intent intent = new Intent();
			intent.putExtra("MSG", Constant.PlayerMsg.CONTINUE_MSG);
			intent.setClass(context, PlayerService.class);
			context.startService(intent);					
			processHandler.sendEmptyMessageDelayed(updatePlayProcess, 500);  //更新播放进度条	
		}
	}
	
	private void updateMusicDisplay(int position) {
		if (playMp3List.size() > 0 && position < playMp3List.size()) {
		   Mp3Info mp3Info = playMp3List.get(position);		
		   music_progressBar.setMax((int)(mp3Info.getDuration()));
		   music_title.setText(mp3Info.getTitle());
		   music_artist.setText(mp3Info.getArtist());
		   Bitmap bitmap = MediaUtil.getArtwork(context, mp3Info.getId(),mp3Info.getAlbumId(), true, true);// 获取专辑位图对象，为小图
		   music_album.setImageBitmap(bitmap); // 这里显示专辑图片
		   processHandler.sendEmptyMessageDelayed(updatePlayProcess, 500);//更新播放进度条
		}
	}
	
	/**
	 * 获取移动的VIEW，放入对应ViewGroup布局容器
	 * @param viewGroup
	 * @param view
	 * @param initLocation
	 * @return
	 */
	private View getMoveView(ViewGroup viewGroup, View view, int[] initLocation) {
		int x = initLocation[0];
		int y = initLocation[1];
		viewGroup.addView(view);
		LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		mLayoutParams.leftMargin = x;
		mLayoutParams.topMargin = y;
		view.setLayoutParams(mLayoutParams);
		return view;
	}
	
	/**
	 * 创建移动的ITEM对应的ViewGroup布局容器
	 */
	private ViewGroup getMoveViewGroup() {
		ViewGroup moveViewGroup = (ViewGroup) (this.getActivity()).getWindow().getDecorView();
		LinearLayout moveLinearLayout = new LinearLayout(context);
		moveLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		moveViewGroup.addView(moveLinearLayout);
		return moveLinearLayout;
	}

	@Override
	public void onDestroy() {
		//记录上次播放歌曲在列表中位置
		SharedPreferences sharedPreferences = context.getSharedPreferences("musicKey",Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString("currentPosition", String.valueOf(currentPosition));
		editor.commit();
		context.unregisterReceiver(playListChangeReceiver);
		context.unregisterReceiver(nowPlayingChangeReceiver);
		processHandler.removeCallbacksAndMessages(null);
		Intent intent = new Intent();
		intent.setClass(context, PlayerService.class);
		context.stopService(intent);
		super.onDestroy();
	}
	
	private PlayListChangeReceiver playListChangeReceiver;
	class PlayListChangeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			musicListAdapter.notifyDataSetChanged();
		}
	}
	
	private NowPlayingChangeReceiver nowPlayingChangeReceiver;
	class NowPlayingChangeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			currentPosition = intent.getIntExtra("currentPosition", 0);
			updateMusicDisplay(currentPosition);
			popupAdapter.notifyDataSetChanged();
		}
	}
	
	public class MusicListAdapter extends BaseAdapter{
		private Context context;		//上下文对象引用
		private Mp3Info mp3Info;		//Mp3Info对象引用

		public MusicListAdapter(Context context) {
			this.context = context;
		}

		@Override
		public int getCount() {
			return mp3Infos.size();
		}

		@Override
		public Object getItem(int position) {
			return mp3Infos.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			if(convertView == null)
			{
				viewHolder = new ViewHolder();
				convertView = LayoutInflater.from(context).inflate(R.layout.music_list_item_layout, null);
				viewHolder.addImage = (ImageView) convertView.findViewById(R.id.add_music);
				viewHolder.musicTitle = (TextView) convertView.findViewById(R.id.music_title);
				viewHolder.musicArtist = (TextView) convertView.findViewById(R.id.music_Artist);
				viewHolder.musicDuration = (TextView) convertView.findViewById(R.id.music_duration);
				viewHolder.music_delete = (TextView) convertView.findViewById(R.id.music_delete);
				viewHolder.music_delete_forever = (TextView) convertView.findViewById(R.id.music_delete_forever);
				viewHolder.music_content = (TextView) convertView.findViewById(R.id.music_content);				
				convertView.setTag(viewHolder);		//表示给View添加一个格外的数据，
			} else {
				viewHolder = (ViewHolder)convertView.getTag();//通过getTag的方法将数据取出来
			}
			mp3Info = mp3Infos.get(position);
//			BaseTools.showlog("mp3Info="+mp3Info.toString());
           //注意，这里是引用类型，比较的是地址，为了使用contains比较值，我们需要重写Mp3Info的equals和GetHashCode方法
			if (playMp3List.contains(mp3Info)) {  
				viewHolder.addImage.setImageResource(R.drawable.add_music_select);
			} else {
				viewHolder.addImage.setImageResource(R.drawable.add_music_unselect);
			}			
			viewHolder.musicTitle.setText(mp3Info.getTitle());		//显示标题
			viewHolder.musicArtist.setText(mp3Info.getArtist());		//显示艺术家
			viewHolder.musicDuration.setText(MediaUtil.getDurationStr((int)(mp3Info.getDuration()/1000)));//显示时长			

			viewHolder.addImage.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					if (playMp3List.contains(mp3Info)) {
						return;
					}
					final ImageView moveImageView = MusicFragment.this.getView(view);
					final int[] startLocation = new int[2];
					final int[] endLocation = new int[2];
					if (moveImageView != null) {
	                	//获取动画起始点坐标
						ImageView addImage = (ImageView) view.findViewById(R.id.add_music);
						addImage.getLocationInWindow(startLocation);
					}
					play_list.getLocationInWindow(endLocation);
					MoveAnim(moveImageView, startLocation , endLocation, position, false);	
				}				
			});
			
			viewHolder.music_delete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					deleteFileFromList(position);					
					music_list.closeOpenedItems();
				}				
			});
			
			viewHolder.music_delete_forever.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					deleteFileForever(position);
					music_list.closeOpenedItems();
				}				
			});
			
			viewHolder.music_content.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					showMusicInfo(position);			
					music_list.closeOpenedItems();
				}				
			});

			return convertView;
		}
		
		public class ViewHolder {
			//所有控件对象引用
			public ImageView addImage;	//添加到播放列表图片
			public TextView musicTitle;		//音乐标题
			public TextView musicDuration;	//音乐时长
			public TextView musicArtist;	//音乐艺术家
			public TextView music_delete;     //删除音乐
			public TextView music_delete_forever; //永久删除
			public TextView music_content; //查看内容
		}
	}
	
	private void initPopWindow(){
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
        View view = inflater.inflate(R.layout.play_list_popupwindow_layout, null);
        TextView play_list_tv = (TextView)view.findViewById(R.id.play_list_tv);
        ImageView delete_all_iv = (ImageView)view.findViewById(R.id.delete_all_iv);
        ListView listView = (ListView)view.findViewById(R.id.listViewID);
        popupAdapter = new PopupAdapter();
        listView.setAdapter(popupAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				currentPosition = position;
				playMusic(currentPosition);
				popupWindow.dismiss();
			}
		});
        delete_all_iv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				playMp3List.clear();
				dataDB.deleteAllMp3Info();   //数据库删除所有
				currentPosition = 0;
				if(PlayerService.mediaPlayer!=null && PlayerService.mediaPlayer.isPlaying()) {
					pauseMusic();
				}
				popupAdapter.notifyDataSetChanged();
				musicListAdapter.notifyDataSetChanged();
				Toast.makeText(context, "清空播放列表完毕！", Toast.LENGTH_SHORT).show();
			}       	
        });
        
        popupWindow = new PopupWindow(view);
        popupWindow.setWidth(AppApplication.mWidth);
        popupWindow.setHeight(Util.dip2px(context, 250));
        Drawable drawable = getResources().getDrawable(R.drawable.bg_shq);
		popupWindow.setBackgroundDrawable(drawable);		
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setAnimationStyle(R.style.playlistPopupStyle);
	}
	
	private class PopupAdapter extends BaseAdapter{	
		private LayoutInflater mInflater;
		private PopupAdapter(){
			mInflater = LayoutInflater.from(context);
		}
		
		@Override
		public int getCount() {
			return playMp3List.size();
		}

		@Override
		public Object getItem(int position) {
			return playMp3List.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			holder = new ViewHolder();
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.play_list_popupwindow_listitem, null);
				holder.mImageView = (ImageView) convertView.findViewById(R.id.imageID);
				holder.mTextView = (TextView) convertView.findViewById(R.id.titleID);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.mTextView.setText((position+1)+" "+playMp3List.get(position).getTitle());
			if (currentPosition == position) {
				holder.mTextView.setTextColor(Color.BLUE);
			}
			holder.mImageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Toast.makeText(context, "已将("+playMp3List.get(position).getTitle()+")从播放列表移除！", Toast.LENGTH_SHORT).show();					
					dataDB.deleteMp3Info(playMp3List.get(position).getTitle());
					playMp3List.remove(position);
					if (position < currentPosition) {
						currentPosition--;          //删除位置在当前播放位置之前，播放位置减1     
					} else if (position == currentPosition) {
						if (position == playMp3List.size()) {   //删除当前播放歌曲，是最后一首时，播放第一首
							currentPosition = 0;
						}
						if(PlayerService.mediaPlayer!=null && PlayerService.mediaPlayer.isPlaying()) {  //删除当前播放歌曲，非最后一首时，播放下一首
							if (playMp3List.size()==0) {
								pauseMusic();
							} else {
								playMusic(currentPosition);			
							}
						} 
					}
					notifyDataSetChanged();
					musicListAdapter.notifyDataSetChanged();					
				}				
			});
			return convertView;
		}
		
		class ViewHolder {
			TextView mTextView;
			ImageView mImageView;
		}
		
	}
	
	//搜索在线歌曲弹出的popupwindow
	private void initSearchPopWindow(){
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
        View view = inflater.inflate(R.layout.search_popupwindow_layout, null);
        ImageView close_iv = (ImageView)view.findViewById(R.id.close_iv);
        ListView listView = (ListView)view.findViewById(R.id.listViewID);
        searchPopupAdapter = new SearchPopupAdapter();
        listView.setAdapter(searchPopupAdapter);
        
        close_iv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				searchPopupWindow.dismiss();
			}       	
        });
        
        searchPopupWindow = new PopupWindow(view);
        searchPopupWindow.setWidth(AppApplication.mWidth);
        searchPopupWindow.setHeight(AppApplication.mHeight - Util.dip2px(context, 200));
        Drawable drawable = getResources().getDrawable(R.drawable.bg_5);
        searchPopupWindow.setBackgroundDrawable(drawable);		
        searchPopupWindow.setFocusable(true);
        searchPopupWindow.setOutsideTouchable(true);
        searchPopupWindow.setAnimationStyle(R.style.AnimationPreview);
	}
	
	private class SearchPopupAdapter extends BaseAdapter{		
		private LayoutInflater mInflater;
		private SearchPopupAdapter(){
			mInflater = LayoutInflater.from(context);
		}
		
		@Override
		public int getCount() {
			return searchMp3List.size();
		}

		@Override
		public Mp3Info getItem(int position) {
			return searchMp3List.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			holder = new ViewHolder();
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.search_popupwindow_listitem, null);
				holder.iv_album = (ImageView) convertView.findViewById(R.id.iv_album);
				holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
				holder.tv_album = (TextView) convertView.findViewById(R.id.tv_album);
				holder.tv_artist = (TextView) convertView.findViewById(R.id.tv_artist);
				holder.iv_download = (ImageView) convertView.findViewById(R.id.iv_download);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			final Mp3Info music = getItem(position);
			final String target = FileUtil.getMusicPath() + "/" + music.getTitle() + ".mp3";
			holder.tv_title.setText(music.getTitle());
			holder.tv_album.setText(music.getAlbum());
			holder.tv_artist.setText(music.getArtist());
			ImageLoader.getInstance().displayImage(music.getSmallAlumUrl(), holder.iv_album, Options.getListOptions());			
			if (new File(target).exists()) {
				holder.iv_download.setBackgroundResource(R.drawable.download_pressed);
			} else {
				holder.iv_download.setBackgroundResource(R.drawable.download_normal);
			}
			
			holder.iv_download.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View view) {
					if (new File(target).exists()) {
						Toast.makeText(context, "歌曲文件已存在", Toast.LENGTH_SHORT).show();
						return;
					}
						
					if (!NetUtil.isWIFIOn(context)) {
						AlertDialog.Builder builder = new Builder(context);
						builder.setTitle("提示");
						builder.setMessage("当前使用的是数据流量，是否下载？");
						builder.setPositiveButton("确定",new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,int which) {
										view.setBackgroundResource(R.drawable.download_pressed);
										dialog.dismiss();
										downloadOnlineMusic(music);			
									}
								});
						builder.setNegativeButton("取消",new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,int which) {
										dialog.dismiss();
									}
								});
						builder.show();
					} else {
						view.setBackgroundResource(R.drawable.download_pressed);
						downloadOnlineMusic(music);
					}
				}		
			});
			return convertView;
		}
		
		class ViewHolder {
			TextView tv_title, tv_artist, tv_album;
			ImageView iv_album, iv_download;
		}
	}


}

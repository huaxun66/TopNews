package com.huaxun.news;

import com.huaxun.MainActivity;
import com.huaxun.R;
import com.huaxun.app.AppApplication;
import com.huaxun.fragment.NewsFragment;
import com.huaxun.news.bean.News;
import com.huaxun.news.fragment.NewsMediaFragment.NewsMediaAdapter;
import com.huaxun.tool.BaseTools;
import com.huaxun.utils.FileUtil;
import com.huaxun.utils.MediaUtil;
import com.huaxun.utils.NetUtil;
import com.huaxun.utils.Util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.VideoView;

@SuppressLint("NewApi")
public class MediaFrameLayout extends FrameLayout implements OnClickListener {
	private Context context;
	private Activity activity;
	private LayoutInflater mInflater;
	private FrameLayout.LayoutParams normalParams, smallParams, fullScreenParams;
	private View view;
	private VideoView videoView;
	private ImageView closeImageView;
	private RelativeLayout relativeLayoutBackGround;
	private ProgressBar progressBar;
	private RelativeLayout controlLayout;
	private ImageView playImageView;
	private ImageView fullImageView;
	public SeekBar mSeekBar;
	private TextView currentTimeTextView;
	private TextView totalTimeTextView;
	
	public boolean isSmallSize = false;
	public boolean isFullScreen = false;
	
	private ListView mListView;
	private int mediaPlayPosition;
	private int height;
	
	private final int updatePlayTime = 1;
	public Handler timeHandler = new Handler() {
        public void handleMessage(Message msg) {
        	super.handleMessage(msg);
        	switch (msg.what) {
        	case updatePlayTime:
        		currentTimeTextView.setText(MediaUtil.switchDuration(videoView.getCurrentPosition()));
        		mSeekBar.setProgress(videoView.getCurrentPosition());
        		timeHandler.sendEmptyMessageDelayed(updatePlayTime, 1000);
        		break;
        	}
        }
	};
	
	
	public MediaFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		this.activity = (Activity) context;
		mInflater = LayoutInflater.from(context);
		view = mInflater.inflate(R.layout.media_frame_layout, null);
		videoView = (VideoView) view.findViewById(R.id.mediaPlane);
		closeImageView = (ImageView)view.findViewById(R.id.mediaClose);
		relativeLayoutBackGround = (RelativeLayout) view.findViewById(R.id.mediaBackGround);
		controlLayout = (RelativeLayout)view.findViewById(R.id.mediaControlBottom);
		progressBar = (ProgressBar)view.findViewById(R.id.circleProgressBar);
		playImageView = (ImageView)view.findViewById(R.id.h_media_play);
		fullImageView = (ImageView)view.findViewById(R.id.h_media_scal);
		mSeekBar = (SeekBar) view.findViewById(R.id.h_media_seekBar);
		currentTimeTextView = (TextView) view.findViewById(R.id.h_media_time1);
		totalTimeTextView = (TextView) view.findViewById(R.id.h_media_time2);
		
		FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams) videoView.getLayoutParams();
		flp.width = LayoutParams.MATCH_PARENT;
		flp.height = LayoutParams.MATCH_PARENT;
		flp.gravity = Gravity.CENTER;
		videoView.setLayoutParams(flp);
	
		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				// TODO Auto-generated method stub
				if (fromUser) {
					videoView.seekTo(progress);
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			
		});
		
		view.setOnTouchListener(new OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				BaseTools.showlog("event="+event.getAction());
				switch (event.getAction()) {
				case MotionEvent.ACTION_MOVE: 
		            break;
		        case MotionEvent.ACTION_UP:	
		            break;
		        case MotionEvent.ACTION_DOWN:    
					if(isSmallSize) {
						mListView.setSelection(mediaPlayPosition);	
					} else {
						if (controlLayout.getVisibility()==View.VISIBLE) {
				        	BaseTools.showlog("hideControlLayout");
							hideControlLayout();
						} else {
				        	BaseTools.showlog("showControlLayout");
							showControlLayout();
						}
					}
		        }
				return false;
			}
		  });
		
//		view.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View arg0) {
//				// TODO Auto-generated method stub
//				if(isSmallSize) {
//					mListView.setSelection(mediaPlayPosition);	
//				} else {
//					if (controlLayout.getVisibility()==View.VISIBLE) {
//						hideControlLayout();
//					} else {
//						showControlLayout();
//					}
//				}
//			}		
//		});
		
		closeImageView.setOnClickListener(this);
		playImageView.setOnClickListener(this);	
		fullImageView.setOnClickListener(this);
		//布局文件其实是MediaFrame的一个子控件，用addView，而不是setContentView
		addView(view);
	}
	
	
	public void startPlay(News news, ListView mListView, int mediaPlayPosition, NewsMediaAdapter newsMediaAdapter){
		this.mListView = mListView;
		this.mediaPlayPosition = mediaPlayPosition;
		View listItem = newsMediaAdapter.getView(mediaPlayPosition, null, mListView);
		listItem.measure(0, 0);
		height = listItem.getMeasuredHeight();
		setMediaFragmentNormalSize();
		
		relativeLayoutBackGround.setVisibility(View.VISIBLE);
		progressBar.setVisibility(View.VISIBLE);
		videoView.setVisibility(View.VISIBLE);
		videoView.requestFocus();

		//如果下载完成，本地存在media文件，则从本地路径加载，否则从网络加载
		String newsName = news.getNewsurl().substring(news.getNewsurl().lastIndexOf("/"),news.getNewsurl().length());
		if(FileUtil.isMediaFileExist(newsName)) {
			videoView.setVideoPath(FileUtil.getMediaPath()+newsName);
		} else {
			if (!NetUtil.isNetworkAvailable(context)) {
				Toast.makeText(context, "请开启网络！",Toast.LENGTH_SHORT).show();
				return;
			}
			Uri uri = Uri.parse(news.getNewsurl());
			videoView.setVideoURI(uri);
		}
	
		videoView.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				relativeLayoutBackGround.setVisibility(View.GONE);
				progressBar.setVisibility(View.GONE);
				videoView.start();
				mSeekBar.setMax(videoView.getDuration());
				totalTimeTextView.setText(" / "+MediaUtil.switchDuration(videoView.getDuration()));
				timeHandler.removeMessages(updatePlayTime);
				timeHandler.sendEmptyMessage(updatePlayTime);
			}
		});
		videoView.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				timeHandler.removeMessages(updatePlayTime);
				MediaFrameLayout.this.setVisibility(View.GONE);
				videoView.setVideoURI(null);
				if (isFullScreen == true) {
					activity.setRequestedOrientation(1);
					showStatusTopBar();
					isFullScreen = false;
				}
				if (isSmallSize == true) {
					isSmallSize = false;
				}
			}
		});
	}


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {	
		case R.id.mediaClose:
            if(isFullScreen == true) {
            	activity.setRequestedOrientation(1);
            	showStatusTopBar();
            	isFullScreen = false;
            }
			videoView.stopPlayback();
			videoView.setVideoURI(null);
			MediaFrameLayout.this.setVisibility(View.GONE);
			timeHandler.removeMessages(updatePlayTime);
			break;
		case R.id.h_media_play:
			if (videoView.isPlaying()) {
				videoView.pause();
				playImageView.setImageResource(R.drawable.media_play);
			} else {
				videoView.start();
				playImageView.setImageResource(R.drawable.media_pause);
			}
			break;
		case R.id.h_media_scal:
			if (isFullScreen) {
				setMediaFragmentNormalSize();
			} else {
				setMediaFragmentFullScreen();
			}
		}
	}

	private void hideControlLayout() {
		closeImageView.setVisibility(View.INVISIBLE);
		controlLayout.setVisibility(View.INVISIBLE);
	}

	private void showControlLayout() {
		closeImageView.setVisibility(View.VISIBLE);
		controlLayout.setVisibility(View.VISIBLE);
	}
    
	public void setMediaFragmentSmallSize() {
		smallParams = new FrameLayout.LayoutParams(AppApplication.mWidth/2, height/2);
		smallParams.gravity = Gravity.RIGHT;
		smallParams.rightMargin = 20;
		view.setLayoutParams(smallParams);
		hideControlLayout();
		isSmallSize = true;
	}
	
	public void setMediaFragmentNormalSize() {
		if (isFullScreen == true) {
			activity.setRequestedOrientation(1);
			showStatusTopBar();
			fullImageView.setImageResource(R.drawable.media_full_screen);
			isFullScreen = false;
		}
		if (isSmallSize = true) {
			isSmallSize = false;
		}
		normalParams = new FrameLayout.LayoutParams(AppApplication.mWidth, height);
		view.setLayoutParams(normalParams);	
	}
	
	private void setMediaFragmentFullScreen() {
		activity.setRequestedOrientation(0);
		hideStatusTopBar();
		//因为横屏了，所以宽高对调
		fullScreenParams = new FrameLayout.LayoutParams(AppApplication.mHeight,AppApplication.mWidth);
		//fullScreenParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		fullScreenParams.gravity = Gravity.CENTER;
		view.setLayoutParams(fullScreenParams);
		MediaFrameLayout.this.setY(0);
		fullImageView.setImageResource(R.drawable.media_scal_screen);
		isFullScreen = true;
	}
	
	private void hideStatusTopBar(){
		//隐藏main_head和ll_channel
		MainActivity.headPanel.setVisibility(View.GONE);
        MainActivity.bottomPanel.setVisibility(View.GONE);     
        NewsFragment.ll_channel.setVisibility(View.GONE);
        
        //隐藏状态栏
//      WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
//		lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
//		activity.getWindow().setAttributes(lp);
//      activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);        
	}
	
	private void showStatusTopBar(){
        MainActivity.headPanel.setVisibility(View.VISIBLE);
        MainActivity.bottomPanel.setVisibility(View.VISIBLE);
        NewsFragment.ll_channel.setVisibility(View.VISIBLE);
//      WindowManager.LayoutParams attr = activity.getWindow().getAttributes();
//		attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
//		activity.getWindow().setAttributes(attr);
//      activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
	}
	
	 @Override
	 public boolean dispatchTouchEvent(MotionEvent ev) {
		//告诉父控件我自己的触摸事件自己处理，你们不要拦截 
	    getParent().requestDisallowInterceptTouchEvent(true);
	    return super.dispatchTouchEvent(ev);
	 }
	 
//	 public boolean onInterceptTouchEvent(MotionEvent ev) {
//	    return false;
//	 }

}

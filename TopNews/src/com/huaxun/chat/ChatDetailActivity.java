package com.huaxun.chat;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.huaxun.R;
import com.huaxun.chat.bean.Msg;
import com.huaxun.chat.bean.User;
import com.huaxun.chat.faceMode.ChatEmoji;
import com.huaxun.chat.faceMode.FaceConversionUtil;
import com.huaxun.chat.faceMode.FaceRelativeLayout;
import com.huaxun.chat.faceMode.FaceRelativeLayout.OnCorpusSelectedListener;
import com.huaxun.chat.util.FileTcpServer;
import com.huaxun.chat.util.Media;
import com.huaxun.chat.util.Tools;
import com.huaxun.dialog.SpeechDialog;
import com.huaxun.tool.BaseTools;
import com.huaxun.utils.ChoosePhotoAndZoomUtil;
import com.huaxun.utils.FileUtil;
import com.huaxun.utils.ImageUtil;
import com.huaxun.utils.Util;


public class ChatDetailActivity extends Activity implements OnClickListener {

	private TextView topBack,topTitle;
	private LinearLayout chat_msg_panel;
	private ScrollView chat_msg_scroll;
	private RelativeLayout rl_control;
	private ImageView switch_mode, chat_add, chat_emoji;
	private EditText chat_msg;
	private TextView chat_msg_send, chat_media_record;
	private LinearLayout ll_more, image_ll, vedio_ll, talk_ll, file_ll;
	private FaceRelativeLayout face_mode;
	
	public User friend;
	private List<Msg> mesList = null; //我和此人消息列表
	private Tools tools=null;
	private Typeface fontFace;
	
	private double sendFileSize;
	
	// 更多选项显示和隐藏动画
	private Animation showMoreAnimation;
	private Animation hiddenMoreAnimation;
	
	private AlertDialog revCallDialog=null, callDialog=null;
	private ProgressDialog proDia = null;	
	private SpeechDialog speechDialog;
	
	private ChoosePhotoAndZoomUtil choosePhotoAndZoomUtil;// 进入相册，并剪切
	private final int chooseFile = 1;//进入FileActivity选择文件 
	private final int chooseVedio = 2; 
	
	private Media media = new Media();	
	private Timer timer = new Timer();
	private int BASE = 1;
	
	private class VolumeTask extends TimerTask {
	    @Override
	    public void run() {
	        if (media.myRecorder!=null) {
	        	double ratio = (double)media.myRecorder.getMaxAmplitude() /BASE; 
	            double db = 0;// 分贝  
	            if (ratio > 1)  
	                db = 20 * Math.log10(ratio);  
	            final double DB = db;
	            
	            mHandler.post(new Runnable(){
					@Override
					public void run() {
                        if (DB == 0) {
                        	speechDialog.setImageResource(R.drawable.voice_volume_0);
                        } else if (DB > 0 && DB <= 25) {
                        	speechDialog.setImageResource(R.drawable.voice_volume_1);
                        } else if (DB > 25 && DB <= 50) {
                        	speechDialog.setImageResource(R.drawable.voice_volume_2);
                        } else if (DB > 50 && DB <= 75) {
                        	speechDialog.setImageResource(R.drawable.voice_volume_3);
                        } else if (DB > 75) {
                        	speechDialog.setImageResource(R.drawable.voice_volume_4);
                        }
					}            	
	            });         
	        }
	    }   
	}
	
	// 滚动屏幕
	private final Handler mHandler = new Handler();
    private Runnable scrollRunnable= new Runnable() {
	    @Override
	    public void run() {
            int offset = chat_msg_panel.getMeasuredHeight() - chat_msg_scroll.getHeight();//判断高度 
            if (offset > 0) {
            	chat_msg_scroll.scrollBy(0, 200);//每次滚200个单位
	        }
	    }
    };
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//初始化
		Util.setTranslucent(this);
		setContentView(R.layout.chat_detail_activity);
		Tools.State=Tools.ChatDetailActivity;//状态
		Tools.chatDetailActivity = this;
		tools = new Tools();
		choosePhotoAndZoomUtil = new ChoosePhotoAndZoomUtil(this);
		fontFace = Typeface.createFromAsset(this.getAssets(),"fonts/klz.ttf");
		showMoreAnimation = AnimationUtils.loadAnimation(this, R.anim.more_in);
		hiddenMoreAnimation = AnimationUtils.loadAnimation(this, R.anim.more_out);		
		hiddenMoreAnimation.setAnimationListener(new AnimationListener(){
			@Override
			public void onAnimationEnd(Animation animation) {
				ll_more.setVisibility(View.GONE);
			}
			@Override
			public void onAnimationRepeat(Animation animation) {}
			@Override
			public void onAnimationStart(Animation animation) {}			
		});
		
		Intent intent = getIntent();
		friend = (User)intent.getExtras().getSerializable("person");	
		
		initView();
		initData();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Tools.State=Tools.ChatFragment;
		//刷新列表	
		try {
			Tools.ChatTips(Tools.FLUSH,null);	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void initView() {
		topBack = (TextView)this.findViewById(R.id.topBack);
		topTitle = (TextView)this.findViewById(R.id.topTitle);
		chat_msg_panel = (LinearLayout)this.findViewById(R.id.chat_msg_panel);
		chat_msg_scroll = (ScrollView)this.findViewById(R.id.chat_msg_scroll);
		rl_control = (RelativeLayout)this.findViewById(R.id.rl_control);
		switch_mode = (ImageView)this.findViewById(R.id.switch_mode);
		chat_msg = (EditText)this.findViewById(R.id.chat_msg);
		chat_add = (ImageView)this.findViewById(R.id.chat_add);
		chat_emoji = (ImageView)this.findViewById(R.id.chat_emoji);
		face_mode = (FaceRelativeLayout)this.findViewById(R.id.face_mode);
		chat_msg_send = (TextView)this.findViewById(R.id.chat_msg_send);
		chat_media_record = (TextView)this.findViewById(R.id.chat_media_record);
		ll_more = (LinearLayout)this.findViewById(R.id.ll_more);
		image_ll = (LinearLayout)this.findViewById(R.id.image_ll);
		vedio_ll = (LinearLayout)this.findViewById(R.id.vedio_ll);
		talk_ll = (LinearLayout)this.findViewById(R.id.talk_ll);
		file_ll = (LinearLayout)this.findViewById(R.id.file_ll);
		
		topBack.setOnClickListener(this);
		switch_mode.setOnClickListener(this);
		chat_add.setOnClickListener(this);
		chat_emoji.setOnClickListener(this);
		chat_msg_send.setOnClickListener(this);
		image_ll.setOnClickListener(this);
		vedio_ll.setOnClickListener(this);
		talk_ll.setOnClickListener(this);
		file_ll.setOnClickListener(this);
		
//		chat_msg.setOnFocusChangeListener(new android.view.View.OnFocusChangeListener() {  
//		    @Override  
//		    public void onFocusChange(View v, boolean hasFocus) {
//		    	//edittext获取焦点检测
//		    	BaseTools.showlog("onFocusChange  hasFocus="+hasFocus);
//		        if(hasFocus) {
//		        	if (face_mode.getVisibility() == View.VISIBLE) {
//						face_mode.setVisibility(View.GONE);
//					} 
//		        }
//		    }
//		});
		
		chat_msg.addTextChangedListener(new EditTextWatcher());
		face_mode.setOnCorpusSelectedListener(new CorpusSelectedListener());
		
		chat_media_record.setOnLongClickListener(new OnLongClickListener(){
			@Override
			public boolean onLongClick(View v) {
				speechDialog = new SpeechDialog(ChatDetailActivity.this);
				speechDialog.show();
				timer = new Timer(); //Timer.cancel()不但结束当前schedule，连整个Timer的线程都会结束掉 ,所以不能再调用schedule,只能new一个新的 
				timer.schedule(new VolumeTask(), 100, 300);// 100后启动任务,以后每隔300执行一次线程
				media.startRecord();
				return false;
			}			
		});
		
		chat_media_record.setOnTouchListener(new OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_UP:
					media.stopRecord();
					speechDialog.dismiss();
					timer.cancel();
					//显示录音，并发送给对方
					sendMediaRecord(media.name);
					break;
				}
				return false;
			}		
		});
		
		proDia = new ProgressDialog(this);
		proDia.setTitle("正在发送");// 设置标题
		proDia.setMessage("文件");// 设置显示信息
		proDia.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);// 水平进度条
		proDia.setCancelable(true);// 设置是否可以通过点击Back键取消  
		proDia.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条  
		proDia.setMax(100);// 设置最大进度指
		proDia.setProgress(10);// 开始点
		
	}
	
	private void initData() {
		topTitle.setText(friend.getName());
		if(Tools.msgContainer.containsKey(friend.getIp())) {// 如果存在此人的消息缓存
			mesList = Tools.msgContainer.get(friend.getIp());
			reOrderList(mesList);
			
			for (int i=0; i<mesList.size(); i++) {
				Msg msg = mesList.get(i);				
				if (msg.isFromMyself() == true) {
					showSendMsg(msg);
				} else {
					receiveMsg(msg);
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void reOrderList(List<Msg> list) {
		Collections.sort(list, new SortByStoreTime());
	}
	
	@SuppressWarnings("rawtypes")
	class SortByStoreTime implements Comparator {
		public int compare(Object o1, Object o2) {
			Msg msg1 = (Msg) o1;
			Msg msg2 = (Msg) o2;
			if (msg1.getStoreTime() > (msg2.getStoreTime())) {  
                return 1;  
            } else if (msg2.getStoreTime() > (msg1.getStoreTime())) {  
                return -1;  
            } else {
                return 0;  
            } 
		}
	}

    private class EditTextWatcher implements TextWatcher {
		@Override
		public void afterTextChanged(Editable s) {
			if (!TextUtils.isEmpty(s)) {
				ll_more.startAnimation(hiddenMoreAnimation);
				chat_add.setVisibility(View.GONE);
				chat_msg_send.setVisibility(View.VISIBLE);
			} else {
				chat_add.setVisibility(View.VISIBLE);
				chat_msg_send.setVisibility(View.GONE);
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,int after) {
		}
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}
    	
    }
    
    private class CorpusSelectedListener implements OnCorpusSelectedListener {
    	//选择emoji后的回调
    	@Override
    	public void onCorpusSelected(ChatEmoji emoji) {
    		SpannableString spannableString = FaceConversionUtil.getInstace().addFace(ChatDetailActivity.this, emoji.getId(), emoji.getCharacter());
    		chat_msg.append(spannableString);
    	}

    	//删除emoji后的回调
    	@Override
    	public void onCorpusDeleted() {
    		int selection = chat_msg.getSelectionStart();
    		String text = chat_msg.getText().toString();
    		if (selection > 0) {
    			String text2 = text.substring(selection - 1);
    			if ("]".equals(text2)) {
    				int start = text.lastIndexOf("[");
    				int end = selection;
    				chat_msg.getText().delete(start, end);
    				return;
    			}
    			chat_msg.getText().delete(selection - 1, selection);
    		}
    	}
    }  
	
	// 按钮点击事件
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		    case R.id.topBack:
			   finish();
			   overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
			break;
			case R.id.chat_msg_send:
				//发送信息按钮
				sendMsg();
				break;
			case R.id.switch_mode:
				if (face_mode.getVisibility() == View.VISIBLE) {
					face_mode.setVisibility(View.GONE);
				} 
				chat_msg.setText("");
				if (chat_media_record.getVisibility() == View.GONE) {
					chat_media_record.setVisibility(View.VISIBLE);
					chat_msg.setVisibility(View.GONE);
					chat_emoji.setVisibility(View.GONE);
					switch_mode.setBackgroundResource(R.drawable.icon_chat_keyboard);
				} else {
					chat_media_record.setVisibility(View.GONE);
					chat_msg.setVisibility(View.VISIBLE);
					chat_emoji.setVisibility(View.VISIBLE);
					switch_mode.setBackgroundResource(R.drawable.icon_chat_voice);
				}
				break;
			case R.id.chat_emoji:
				if (ll_more.getVisibility() == View.VISIBLE) {
					ll_more.setVisibility(View.GONE);
				}
				if (face_mode.getVisibility() == View.GONE) {
					hideSoftInput();
					face_mode.setVisibility(View.VISIBLE);
				} else {
					face_mode.setVisibility(View.GONE);
				}				
				break;
			case R.id.chat_add:
				if (face_mode.getVisibility() == View.VISIBLE) {
					face_mode.setVisibility(View.GONE);
				}
				if (ll_more.getVisibility() == View.GONE) {
					ll_more.setVisibility(View.VISIBLE);
					ll_more.startAnimation(showMoreAnimation);
				} else {
					ll_more.startAnimation(hiddenMoreAnimation);					
				}
				break;
            case R.id.image_ll:
            	choosePhotoAndZoomUtil.choosePhotoInAlbum();
            	ll_more.startAnimation(hiddenMoreAnimation);
				break;
            case R.id.vedio_ll:
            	Intent intent = new Intent(Intent.ACTION_PICK, null);
        		intent.setDataAndType(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "video/*");
        		startActivityForResult(intent, chooseVedio);
            	ll_more.startAnimation(hiddenMoreAnimation);
				break;
            case R.id.talk_ll:
            	sendCall();
            	ll_more.startAnimation(hiddenMoreAnimation);
				break;
            case R.id.file_ll:
    			Intent it = new Intent(this, FileActivity.class);
    			startActivityForResult(it, 1);
            	ll_more.startAnimation(hiddenMoreAnimation);
				break;				
		}
	}
	
    // 发送文件取得路径
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		switch (resultCode) {
 		case RESULT_OK:
 			if (requestCode == chooseFile) {
 				Tools.chooseFilePath = data.getStringExtra("path");
 	 			//发送请求传送文件
 				Tools.TransportContent = Tools.TransportFile;
 	 			Msg msg=new Msg(Tools.getTimel(),Tools.me.getHeadIconPos(),Tools.me.getName(), Tools.me.getIp(), friend.getName(), friend.getIp(),Tools.CMD_FILEREQUEST, 
 	 					(new File(Tools.chooseFilePath)).getName()+":"+(new File(Tools.chooseFilePath)).length());
 	 			tools.sendMsg(msg);
 	 			showSendMsg(msg);
 			} else if (requestCode == ChoosePhotoAndZoomUtil.PHOTOZOOM) {  //进入系统相册选择图片后
 				Tools.chooseImagePath = FileUtil.uri2filePath(ChatDetailActivity.this, data.getData());
 				sendImage(Tools.chooseImagePath);
 			} else if (requestCode == chooseVedio) {  //进入系统选择视频后
 				Tools.chooseVedioPath = FileUtil.uri2filePath(ChatDetailActivity.this, data.getData());
 				sendVideo(Tools.chooseVedioPath);
 			}			
 			break;
 		}
 	}

	// 发送信息
	public void sendMsg() {
		String body = chat_msg.getText().toString();
		if(null==body || body.length()<=0){
			Toast.makeText(this,"消息不能为空", Toast.LENGTH_SHORT).show();
			return;
		}
		chat_msg.setText("");
		Msg msg=new Msg(Tools.getTimel(),Tools.me.getHeadIconPos(),Tools.me.getName(), Tools.me.getIp(), friend.getName(), friend.getIp(),Tools.CMD_SENDMSG, body);
		showSendMsg(msg);
		tools.sendMsg(msg);		
	}
	
	// 显示发送的信息
	public void showSendMsg(final Msg msg) {
		View view = getLayoutInflater().inflate(R.layout.send_msg_layout, null);		
		TextView send_msg_time = (TextView)view.findViewById(R.id.send_msg_time);		
		ImageView senduserhead = (ImageView)view.findViewById(R.id.senduserhead);
		TextView send_msg_content = (TextView)view.findViewById(R.id.send_msg_content);
		ImageView send_image = (ImageView)view.findViewById(R.id.send_image);
		ImageView vedio_play = (ImageView)view.findViewById(R.id.vedio_play);
		TextView media_time = (TextView)view.findViewById(R.id.media_time);
		send_msg_time.setTypeface(fontFace);
		send_msg_content.setTypeface(fontFace);
		media_time.setTypeface(fontFace);
		
		send_msg_time.setText(Tools.getChangeTime(msg.getPackId()));
		senduserhead.setImageResource(Tools.headIconIds[Tools.me.getHeadIconPos()]);
		if (msg.getMsgType() == Tools.CMD_SENDMSG) {
			SpannableString spannableString = FaceConversionUtil.getInstace().getExpressionString(this, (String)msg.getBody());
			send_msg_content.append(spannableString);
		} else if (msg.getMsgType() == Tools.CMD_SENDMEDIA) {
			media_time.setVisibility(View.VISIBLE);
			MediaPlayer mp = MediaPlayer.create(this, Uri.parse(media.sendpath +"/"+((String) msg.getBody()).split(":")[0]));
			int duration = mp.getDuration()/1000;//即为时长 是ms
			media_time.setText(duration+"″");
			//动态设置rightDrawable
			Drawable draw=getResources().getDrawable(R.drawable.chatfrom_voice_playing);  
			draw.setBounds(0, 0, 80, 80);  
			send_msg_content.setCompoundDrawables(null, null, draw, null);
		} else if (msg.getMsgType() == Tools.CMD_SENDIMAGE) {
			send_msg_content.setVisibility(View.GONE);
			send_image.setVisibility(View.VISIBLE);
			Bitmap bmp = ImageUtil.getBitmapFromSDcard(Tools.chooseImagePath);
			send_image.setImageBitmap(bmp);
			send_image.setTag(Tools.chooseImagePath);
		} else if (msg.getMsgType() == Tools.CMD_SENDVIDEO) {
			send_msg_content.setVisibility(View.GONE);
			send_image.setVisibility(View.VISIBLE);
			vedio_play.setVisibility(View.VISIBLE);
			send_image.setImageResource(R.drawable.bg_media);
			send_image.setTag(Tools.chooseVedioPath);
		} else if (msg.getMsgType() == Tools.CMD_FILEREQUEST) {
			send_msg_content.setVisibility(View.GONE);
			send_image.setVisibility(View.VISIBLE);
			send_image.setImageResource(R.drawable.file);
			send_image.setTag(Tools.chooseFilePath);
		} else if (msg.getMsgType() == Tools.CMD_FILEREFUSE) {
			send_msg_content.setBackgroundDrawable(null);
			send_msg_content.setTextColor(this.getResources().getColor(R.color.red));
			send_msg_content.setText("拒绝接收对方文件");
		}
		
		send_msg_content.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//传入录音文件名并播放
				if (msg.getMsgType() == Tools.CMD_SENDMEDIA) {
					media.startPlay(media.sendpath +"/"+((String) msg.getBody()).split(":")[0]);
				}			
			}
		});
		
		send_image.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String path = (String)v.getTag();
				FileActivity.openFile(ChatDetailActivity.this, new File(path));
			}
		});
		
		chat_msg_panel.addView(view);
		//更新滚动条
		mHandler.post(scrollRunnable);
	}
	
	
	// 接收信息
	private void receiveMsg(final Msg msg) {
		View view = getLayoutInflater().inflate(R.layout.received_msg_layout, null);
		TextView receive_msg_time = (TextView)view.findViewById(R.id.receive_msg_time);
		ImageView receiveuserhead=(ImageView)view.findViewById(R.id.receiveuserhead);
		TextView received_msg_content = (TextView)view.findViewById(R.id.received_msg_content);
		ImageView receive_image = (ImageView)view.findViewById(R.id.receive_image);
		ImageView vedio_play=(ImageView)view.findViewById(R.id.vedio_play);
		TextView media_time = (TextView)view.findViewById(R.id.media_time);
		receive_msg_time.setTypeface(fontFace);
		received_msg_content.setTypeface(fontFace);
		media_time.setTypeface(fontFace);
		
		receive_msg_time.setText(Tools.getChangeTime(msg.getPackId()));
		receiveuserhead.setImageResource(Tools.headIconIds[msg.getHeadIconPos()]);
		if (msg.getMsgType() == Tools.CMD_SENDMSG) {
			SpannableString spannableString = FaceConversionUtil.getInstace().getExpressionString(this, (String)msg.getBody());
			received_msg_content.append(spannableString);
		} else if (msg.getMsgType() == Tools.CMD_SENDMEDIA) {
			media_time.setVisibility(View.VISIBLE);
			MediaPlayer mp = MediaPlayer.create(this, Uri.parse(media.receivepath +"/"+((String) msg.getBody()).split(":")[0]));
			int duration = mp.getDuration()/1000;//即为时长 是ms
			media_time.setText(duration+"″");
			//动态设置rightDrawable
			Drawable draw=getResources().getDrawable(R.drawable.chatto_voice_playing);  
			draw.setBounds(0, 0, 80, 80);  
			received_msg_content.setCompoundDrawables(null, null, draw, null);  
		} else if (msg.getMsgType() == Tools.CMD_SENDIMAGE) {
			received_msg_content.setVisibility(View.GONE);
			receive_image.setVisibility(View.VISIBLE);
			Bitmap bmp = ImageUtil.getBitmapFromSDcard(Tools.fileSavePath + "/" + ((String) msg.getBody()).split(":")[0]);
			receive_image.setImageBitmap(bmp);
			receive_image.setTag(Tools.fileSavePath + "/" + ((String) msg.getBody()).split(":")[0]);
		} else if (msg.getMsgType() == Tools.CMD_SENDVIDEO) {
			received_msg_content.setVisibility(View.GONE);
			receive_image.setVisibility(View.VISIBLE);
			vedio_play.setVisibility(View.VISIBLE);
            receive_image.setImageResource(R.drawable.bg_media);
            receive_image.setTag(Tools.fileSavePath + "/" + ((String) msg.getBody()).split(":")[0]);
		} else if (msg.getMsgType() == Tools.CMD_FILEREQUEST) {
			received_msg_content.setVisibility(View.GONE);
			receive_image.setVisibility(View.VISIBLE);
            receive_image.setImageResource(R.drawable.file);
            receive_image.setTag(Tools.fileSavePath + "/" + ((String) msg.getBody()).split(":")[0]);
		} else if (msg.getMsgType() == Tools.CMD_FILEREFUSE) {
			received_msg_content.setBackgroundDrawable(null);
			received_msg_content.setTextColor(this.getResources().getColor(R.color.red));
			received_msg_content.setText("对方拒绝接收文件");
		}
		
		received_msg_content.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//传入录音文件名并播放
				if (msg.getMsgType() == Tools.CMD_SENDMEDIA) {
					media.startPlay(media.receivepath +"/"+((String) msg.getBody()).split(":")[0]);
				}			
			}
		});
		
		receive_image.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String path = (String)v.getTag();
				FileActivity.openFile(ChatDetailActivity.this, new File(path));
			}
		});
		
		chat_msg_panel.addView(view);
		mHandler.post(scrollRunnable);
	}
	
	// 发送录音
	public void sendMediaRecord(String name) {
		//通知准备发送录音
		String filePath = media.sendpath+"/"+name;
		String body = name + ":" + (new File(filePath)).length();
		Msg msg=new Msg(Tools.getTimel(),Tools.me.getHeadIconPos(),Tools.me.getName(), Tools.me.getIp(), friend.getName(), friend.getIp(),Tools.CMD_SENDMEDIA, body);
		showSendMsg(msg);
		tools.sendMsg(msg);
	}
	
	// 发送语音
	public void sendCall() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("发送方："+Tools.me.getName());
		builder.setMessage("接收方"+friend.getName());
		builder.setIcon(null);
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		callDialog = builder.show();
		callDialog.setCanceledOnTouchOutside(false);
		callDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface arg0) {
				//发送结束呼叫请求
				Msg msg=new Msg();
				msg.setSendUser(Tools.me.getName());//昵称默认为自己的机器号
				msg.setSendUserIp(Tools.me.getIp());
				msg.setReceiveUserIp(friend.getIp());
				msg.setMsgType(Tools.CMD_STOPTALK);//发送呼叫请求
				msg.setPackId(Tools.getTimel());
				tools.sendMsg(msg);
			}
		});
		//发送呼叫请求
		Msg msg=new Msg();
		msg.setSendUser(Tools.me.getName());//昵称默认为自己的机器号
		msg.setSendUserIp(Tools.me.getIp());
		msg.setReceiveUserIp(friend.getIp());
		msg.setMsgType(Tools.CMD_STARTTALK);//发送呼叫请求
		msg.setPackId(Tools.getTimel());
		tools.sendMsg(msg);
	}
	
	// 接收语音请求
	private void showReceiveCallDialog(final Msg mes) {
		if(!Tools.stoptalk){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("来自:"+mes.getSendUser());
			builder.setMessage(null);
			builder.setIcon(null);
			View vi = getLayoutInflater().inflate(R.layout.request_talk_layout, null);
			builder.setView(vi);
			revCallDialog = builder.show();
			revCallDialog.setCanceledOnTouchOutside(false);
			revCallDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface arg0) {
					//发送结束呼叫请求
					Msg msg=new Msg();
					msg.setSendUser(Tools.me.getName());//昵称默认为自己的机器号
					msg.setSendUserIp(Tools.me.getIp());
					msg.setReceiveUserIp(mes.getSendUserIp());
					msg.setMsgType(Tools.CMD_STOPTALK);
					msg.setPackId(Tools.getTimel());
					tools.sendMsg(msg);
				}
			});
			Button talkOkBtn = (Button)vi.findViewById(R.id.receive_talk_okbtn);
			talkOkBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View okBtn) {
					if(!Tools.stoptalk){//如果远程用户未关闭通话，则向对方发送同意接收通话指令
						Msg msg=new Msg();
						msg.setSendUser(Tools.me.getName());//昵称默认为自己的机器号
						msg.setSendUserIp(Tools.me.getIp());
						msg.setReceiveUserIp(mes.getSendUserIp());
						msg.setMsgType(Tools.CMD_ACCEPTTALK);
						msg.setPackId(Tools.getTimel());
						tools.sendMsg(msg);
						okBtn.setEnabled(false);
						//同意接收并开始传输语音数据
						User person=new User(mes.getSendUser(),mes.getSendUserIp());
						Tools.audio.audioSend(person);
					}
				}
			});
			Button talkCancelBtn = (Button)vi.findViewById(R.id.receive_talk_cancel);
			talkCancelBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View cancelBtn) {
					revCallDialog.dismiss();
				}
			});
		}
	}
	
	// 收到传送文件请求
	private void showReceiveFileDialog(final Msg mes) {
		final String str = mes.getBody().toString();
		new AlertDialog.Builder(this)
		.setTitle("来自:"+mes.getSendUser())
		.setMessage("是否接收文件?" + "\n文件名："+ str.split(":")[0] +"\n大小："+FileActivity.getFormatSize(Double.parseDouble(str.split(":")[1])))
		.setIcon(android.R.drawable.ic_dialog_info)
		.setPositiveButton("接受", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// 接收文件 返回提示接受 建立tcp 服务器 接收文件
				FileTcpServer ts = new FileTcpServer(mes);
				ts.start();
				Tools.TransportContent = Tools.TransportFile;
				// 发送消息 让对方开始发送文件
				Msg msg=new Msg(Tools.getTimel(), Tools.me.getHeadIconPos(), Tools.me.getName(), Tools.me.getIp(), mes.getSendUser(), mes.getSendUserIp(),Tools.CMD_FILEACCEPT, mes.getBody());
				tools.sendMsg(msg);
				//进度显示
				Tools.sendProgress = 0;
				Message m1 = new Message();
				m1.what = Tools.FILE_JINDU;
				m1.obj = mes.getBody();
				handler.sendMessage(m1);
				return;
			}
		})
		.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// 不接受 返回提示不接受
				Msg msg=new Msg(Tools.getTimel(), Tools.me.getHeadIconPos(),Tools.me.getName(), Tools.me.getIp(), mes.getSendUser(), mes.getSendUserIp(),Tools.CMD_FILEREFUSE, null);
				tools.sendMsg(msg);
				return;
			}
		}).show();
	}
	
	// 发送图片
	public void sendImage(String imagePath) {
		//通知准备发送图片
		Tools.TransportContent = Tools.TransportImage;
		String name = new File(imagePath).getName();
		String body = name + ":" + (new File(imagePath)).length();
		Msg msg=new Msg(Tools.getTimel(),Tools.me.getHeadIconPos(),Tools.me.getName(), Tools.me.getIp(), friend.getName(), friend.getIp(),Tools.CMD_SENDIMAGE, body);
		tools.sendMsg(msg);
		showSendMsg(msg);
	}
	
	// 发送图片
	public void sendVideo(String vedioPath) {
		//通知准备发送图片
		Tools.TransportContent = Tools.TransportVedio;
		String name = new File(vedioPath).getName();
		String body = name + ":" + (new File(vedioPath)).length();
		Msg msg=new Msg(Tools.getTimel(),Tools.me.getHeadIconPos(),Tools.me.getName(), Tools.me.getIp(), friend.getName(), friend.getIp(),Tools.CMD_SENDVIDEO, body);
		tools.sendMsg(msg);
		showSendMsg(msg);
	}

	// Handler
	public Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Tools.SHOW:
				Toast.makeText(ChatDetailActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
				break;
			case Tools.RECEIVEMSG:
				//接收消息
				receiveMsg((Msg)msg.obj);
				break;
			case Tools.CMD_FINISHTRANPORT:
				//完成接收录音
				receiveMsg((Msg)msg.obj);
				break;
			case Tools.CMD_STARTTALK:
				//语音请求
				showReceiveCallDialog((Msg)msg.obj);
				break;
			case Tools.CMD_STOPTALK:
				//语音结束
				if(revCallDialog!=null && revCallDialog.isShowing())
					revCallDialog.dismiss();
				if(callDialog!=null && callDialog.isShowing())
					callDialog.dismiss();
				break;
			case Tools.CMD_FILEREQUEST:
				//文件请求
				showReceiveFileDialog((Msg)msg.obj);
				break;
//			case Tools.CMD_FILEACCEPT:
//				showSendMsg((Msg)msg.obj);
//				break;
			case Tools.FILE_JINDU:
				String[] pi = ((String) msg.obj).split(":");
				sendFileSize = Double.parseDouble(pi[1]);
				proDia.setTitle("正在发送");// 设置标题
				proDia.setMessage("文件：" + pi[0] + "\n大小：" + FileActivity.getFormatSize(sendFileSize));// 设置显示信息
				proDia.onStart();
				proDia.show();
				fileProgress();
				break;
			case Tools.PROGRESS_FLUSH:			
				int i0 = (int) ((Tools.sendProgress / (sendFileSize)) * 100);
				proDia.setProgress(i0);
				break;
			case Tools.PROGRESS_COL:// 关闭进度条
				proDia.dismiss();
				break;
			}
		}
	};
	
	// 启动进度条刷新线程
	public void fileProgress() {
		new Thread() { 
			public void run() {			
				while (Tools.sendProgress != -1) {
					Message m = new Message();
					m.what = Tools.PROGRESS_FLUSH;
					handler.sendMessage(m);
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				// 关闭进度条
				Message m1 = new Message();
				m1.what = Tools.PROGRESS_COL;
				handler.sendMessage(m1);
			}
		}.start();
	}
	
	
	/**
	 * 隐藏软软键盘
	 */
	public void hideSoftInput() {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				InputMethodManager imm = (InputMethodManager) ChatDetailActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(chat_msg.getWindowToken(), 0);
			}
		});
	}
	
}

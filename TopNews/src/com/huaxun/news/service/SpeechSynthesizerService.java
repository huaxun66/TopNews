package com.huaxun.news.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;

import com.huaxun.menuLeft.activity.TtsSettings;
import com.huaxun.tool.BaseTools;
import com.huaxun.tool.Constants;
import com.huaxun.utils.FileUtil;
import com.huaxun.utils.Util;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

public class SpeechSynthesizerService extends Service {
	// 语音合成对象
	private SpeechSynthesizer mTts;
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
	public static boolean isNowPlaying = false;
	public static String content;
	//settings
	private SharedPreferences mSharedPreferences;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		//1.创建SpeechSynthesizer对象, 第二个参数：本地合成时传InitListener
		mTts = SpeechSynthesizer.createSynthesizer(this, mTtsInitListener);
		mSharedPreferences = getSharedPreferences(TtsSettings.PREFER_NAME, MODE_PRIVATE);
		set_mTts();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {	
		Bundle bundle = intent.getExtras();
		/*得到从startService传来的动作，后是默认参数，这里是我自定义的常量*/
		action = bundle.getInt("action", PLAY);
		content = bundle.getString("content");
		BaseTools.showlog("action="+action);
		if (action == PLAY) {
			isNowPlaying = true;
			startSpeakContent(content);
		} else if (action == PAUSE) {
			isNowPlaying = false;
			mTts.pauseSpeaking();
		} else if (action == REPLAY) {
			isNowPlaying = true;
			mTts.resumeSpeaking();
		} else if (action == STOP) {
			isNowPlaying = false;
			mTts.stopSpeaking();
			SpeechSynthesizerService.this.stopSelf();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	private void set_mTts() {
		// 清空参数
		mTts.setParameter(SpeechConstant.PARAMS, null);
		 //设置云端 
		mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
		// 设置在线合成发音人
		mTts.setParameter(SpeechConstant.VOICE_NAME, mSharedPreferences.getString("voicer_preference", "0"));
		//设置合成语速
		mTts.setParameter(SpeechConstant.SPEED, mSharedPreferences.getString("speed_preference", "50"));
		//设置合成音调
		mTts.setParameter(SpeechConstant.PITCH, mSharedPreferences.getString("pitch_preference", "50"));
		//设置合成音量
		mTts.setParameter(SpeechConstant.VOLUME, mSharedPreferences.getString("volume_preference", "50"));
		//设置播放器音频流类型
		mTts.setParameter(SpeechConstant.STREAM_TYPE, mSharedPreferences.getString("stream_preference", "3"));
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");
		// 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
		// 注：AUDIO_FORMAT参数语记需要更新版本才能生效
		mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
		// 如果不需要保存保存合成音频，请注释下行代码,合成的音频格式：默认pcm格式
		mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, FileUtil.getCacheAudioPath()+"/iat.wav");	
	}

	
	private void startSpeakContent(String content){
		int code = mTts.startSpeaking(content, mTtsListener);
//		/** 
//		 * 只保存音频不进行播放接口,调用此接口请注释startSpeaking接口
//		 * text:要合成的文本，uri:需要保存的音频全路径，listener:回调接口
//		*/
//		String path = Environment.getExternalStorageDirectory()+"/tts.pcm";
//		int code = mTts.synthesizeToUri(text, path, mTtsListener);
		
		if (code != ErrorCode.SUCCESS) {	
		    Util.showToast(this, "语音合成失败,错误码: " + code);	
		}
	}
	 
	
	private SynthesizerListener mTtsListener = new SynthesizerListener() {
		// 开始播放
		@Override
		public void onSpeakBegin() {
			BaseTools.showlog("onSpeakBegin");
		}

		// 停止播放
		@Override
		public void onSpeakPaused() {
			BaseTools.showlog("onSpeakPaused");
		}

		// 恢复播放回调接口
		@Override
		public void onSpeakResumed() {
			BaseTools.showlog("onSpeakResumed");
		}

		// 缓冲进度回调，percent为缓冲进度，beginPos为缓冲音频在文本中开始的位置，endPos为缓冲音频在文本中结束的位置，info为附加信息
		@Override
		public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
//			BaseTools.showlog("onBufferProgress,percent="+percent+" beginPos="+beginPos+" endPos="+endPos+" info="+info);
		}

		// 播放进度回调,percent为播放进度0-100；beginPos为播放音频在文本中开始的位置，endPos为播放音频在文本中结束的位置
		@Override
		public void onSpeakProgress(int percent, int beginPos, int endPos) {
//			BaseTools.showlog("onSpeakProgress,percent="+percent+" beginPos="+beginPos+" endPos="+endPos);
		}

		// 会话结束回调接口，没有错误时error为空
		@Override
		public void onCompleted(SpeechError error) {
			BaseTools.showlog("onCompleted,error="+error);
			if (error == null) {
				Util.showToast(SpeechSynthesizerService.this, "播放完毕");
			} else if (error != null) {
				Util.showToast(SpeechSynthesizerService.this, error.getPlainDescription(true));
			}
			isNowPlaying = false;
			content = null;
			SpeechSynthesizerService.this.stopSelf();				
		}

		@Override
		public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
			// 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
			// 若使用本地能力，会话id为null
			//	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
			//		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
			//		Log.d(TAG, "session id =" + sid);
			//	}
		}
	};
	
	
	private InitListener mTtsInitListener = new InitListener() {
		@Override
		public void onInit(int code) {
			BaseTools.showlog("InitListener init()：code = " + code);
			if (code != ErrorCode.SUCCESS) {
       		Util.showToast(SpeechSynthesizerService.this, "初始化失败,错误码："+code);
        	} else {   
        		// 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
        	}		
		}
	};
	
	@Override
	public void onDestroy() {
		BaseTools.showlog("onDestroy");
		isNowPlaying = false;
		mTts.stopSpeaking();
		mTts.destroy();// 退出时释放连接
	}
	
}
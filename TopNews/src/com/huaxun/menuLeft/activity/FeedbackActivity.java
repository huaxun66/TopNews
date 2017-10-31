package com.huaxun.menuLeft.activity;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huaxun.R;
import com.huaxun.app.AppApplication;
import com.huaxun.base.BaseActivity;
import com.huaxun.menuLeft.util.JsonParser;
import com.huaxun.news.service.SpeechSynthesizerService;
import com.huaxun.tool.BaseTools;
import com.huaxun.tool.Options;
import com.huaxun.utils.FileUtil;
import com.huaxun.utils.ImageUtil;
import com.huaxun.utils.Util;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.nostra13.universalimageloader.core.ImageLoader;

public class FeedbackActivity extends BaseActivity implements OnClickListener{
	private TextView title, back;
	private ImageView login_IV, audio_IV, play_IV;
	private TextView login_TV;
	private EditText feedback_et;
	private LinearLayout iat_control;
	private Button iat_recognize, iat_stop, iat_cancel, iat_recognize_stream, finish_btn;
	
	// 语音听写对象
	private SpeechRecognizer mIat;
	// 语音听写UI
	private RecognizerDialog mIatDialog;
	// 用HashMap存储听写结果
	private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
	private SharedPreferences mSharedPreferences;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Util.setTranslucent(this);
		setContentView(R.layout.feedback);
		title = (TextView)findViewById(R.id.topTitle);
		back = (TextView)findViewById(R.id.topBack);
		login_IV = (ImageView)findViewById(R.id.login_IV);
		login_TV = (TextView)findViewById(R.id.login_TV);
		audio_IV = (ImageView)findViewById(R.id.audio_IV);
		play_IV = (ImageView)findViewById(R.id.play_IV);
		feedback_et = (EditText)findViewById(R.id.feedback_et);
		iat_control = (LinearLayout)findViewById(R.id.iat_control);
		iat_recognize = (Button)findViewById(R.id.iat_recognize);
		iat_stop = (Button)findViewById(R.id.iat_stop);
		iat_cancel = (Button)findViewById(R.id.iat_cancel);
		iat_recognize_stream = (Button)findViewById(R.id.iat_recognize_stream);
		finish_btn = (Button)findViewById(R.id.finish_btn);
		
		back.setOnClickListener(this);
		audio_IV.setOnClickListener(this);
		play_IV.setOnClickListener(this);
		iat_recognize.setOnClickListener(this);
		iat_stop.setOnClickListener(this);
		iat_cancel.setOnClickListener(this);
		iat_recognize_stream.setOnClickListener(this);
		finish_btn.setOnClickListener(this);
		
		// 使用SpeechRecognizer对象，可根据回调消息自定义界面；
		mIat = SpeechRecognizer.createRecognizer(this, mInitListener);
		// 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
		// 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
		mIatDialog = new RecognizerDialog(this, mInitListener);
		mSharedPreferences = getSharedPreferences(IatSettings.PREFER_NAME, Activity.MODE_PRIVATE);
		
		initData();
	}
	
	private void initData() {
		title.setText("反馈");
		if (AppApplication.mUserInfo != null) {
			String usericon = AppApplication.mUserInfo.getUsericon();
			if (AppApplication.mUserInfo.getIsThirdLogin() == true) {
				ImageLoader.getInstance().displayImage(usericon, login_IV, Options.getListOptions());
			} else {
				if (new File(usericon).exists()) {
					Bitmap bmp = ImageUtil.getBitmapFromSDcard(usericon);
					login_IV.setImageBitmap(bmp);
				}
			}
			login_TV.setText(AppApplication.mUserInfo.getUsername());
		}
	}
	
	int ret = 0; // 函数调用返回值
	
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.topBack:
			finish();
			overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
			break;
		case R.id.audio_IV:
			if (iat_control.getVisibility()==View.VISIBLE) {
				audio_IV.setSelected(false);
				iat_control.setVisibility(View.GONE);
			} else {
				audio_IV.setSelected(true);
				iat_control.setVisibility(View.VISIBLE);
			}			
			break;
		case R.id.play_IV:
			Intent intent = new Intent(this, SpeechSynthesizerService.class);;
			Bundle bundle  = new Bundle();
			bundle.putString("content", feedback_et.getText().toString());
			bundle.putInt("action", SpeechSynthesizerService.PLAY);
			intent.putExtras(bundle);
			startService(intent);
		    Util.showToast(this, "开始播放");
			break;
		// 开始听写
		// 如何判断一次听写结束：OnResult isLast=true 或者 onError
		case R.id.iat_recognize:
			feedback_et.setText(null);// 清空显示内容
			mIatResults.clear();
			// 设置参数
			setParam();
			boolean isShowDialog = mSharedPreferences.getBoolean(getString(R.string.pref_key_iat_show), true);
			if (isShowDialog) {
				// 显示听写对话框
				mIatDialog.setListener(mRecognizerDialogListener);
				mIatDialog.show();
				Util.showToast(FeedbackActivity.this, "请开始说话…");
			} else {
				// 不显示听写对话框
				ret = mIat.startListening(mRecognizerListener);
				if (ret != ErrorCode.SUCCESS) {
					Util.showToast(this, "听写失败,错误码：" + ret);
				} else {
					Util.showToast(FeedbackActivity.this, "请开始说话…");
				}
			}
			break;
		case R.id.iat_stop:
			mIat.stopListening();
			Util.showToast(this, "停止听写");
			break;
		case R.id.iat_cancel:
			mIat.cancel();
			Util.showToast(this, "取消听写");
			break;
		case R.id.iat_recognize_stream:
			feedback_et.setText(null);// 清空显示内容
			mIatResults.clear();
			// 设置参数
			setParam();
			// 设置音频来源为外部文件
			mIat.setParameter(SpeechConstant.AUDIO_SOURCE, "-1");
			// 也可以像以下这样直接设置音频文件路径识别（要求设置文件在sdcard上的全路径）：
			// mIat.setParameter(SpeechConstant.AUDIO_SOURCE, "-2");
			// mIat.setParameter(SpeechConstant.ASR_SOURCE_PATH, FileUtil.getCacheAudioPath()+"/iat.wav);
			ret = mIat.startListening(mRecognizerListener);
			if (ret != ErrorCode.SUCCESS) {
				Util.showToast(this, "识别失败,错误码：" + ret);
			} else {
				byte[] audioData = FileUtil.readAssetsFile(this, "iat.wav");
				
				if (null != audioData) {
					Util.showToast(this, "开始音频流识别");
					// 一次（也可以分多次）写入音频文件数据，数据格式必须是采样率为8KHz或16KHz（本地识别只支持16K采样率，云端都支持），位长16bit，单声道的wav或者pcm
					// 写入8KHz采样的音频时，必须先调用setParameter(SpeechConstant.SAMPLE_RATE, "8000")设置正确的采样率
					// 注：当音频过长，静音部分时长超过VAD_EOS将导致静音后面部分不能识别
					mIat.writeAudio(audioData, 0, audioData.length);
					mIat.stopListening();
				} else {
					mIat.cancel();
					Util.showToast(this,"读取音频流失败");
				}
			}
			break;
		case R.id.finish_btn:
			break;
		}
	}
	
	public void setParam() {
		// 清空参数
		mIat.setParameter(SpeechConstant.PARAMS, null);

		// 设置听写引擎
		mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
		// 设置返回结果格式
		mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

		String lag = mSharedPreferences.getString("iat_language_preference","mandarin");
		if (lag.equals("en_us")) {
			// 设置语言
			mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
		} else {
			// 设置语言
			mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
			// 设置语言区域
			mIat.setParameter(SpeechConstant.ACCENT, lag);
		}

		// 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
		mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));
		
		// 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
		mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));
		
		// 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
		mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "1"));
		
		// 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
		// 注：AUDIO_FORMAT参数语记需要更新版本才能生效
		mIat.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
		mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, FileUtil.getCacheAudioPath()+"/iat.wav");
	}
    
	/**
	 * 初始化监听器。
	 */
	private InitListener mInitListener = new InitListener() {
		@Override
		public void onInit(int code) {
			BaseTools.showlog("SpeechRecognizer init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
				Util.showToast(FeedbackActivity.this, "初始化失败，错误码：" + code);
			}
		}
	};

    /**
     * 语音听写监听
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {
        // 听写结果回调接口(返回Json格式结果，用户可参见附录12.1)；
        // 一般情况下会通过onResults接口多次返回结果，完整的识别内容是多次结果的累加；
        // 关于解析Json的代码可参见MscDemo中JsonParser类；
        // isLast等于true时会话结束。
        public void onResult(RecognizerResult results, boolean isLast) {
        	BaseTools.showlog(results.getResultString());
            printResult(results);
        }

        // 会话发生错误回调接口
        public void onError(SpeechError error) {
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            if(error.getErrorCode()==10118){
            	Util.showToast(FeedbackActivity.this, "你好像没有说话哦");
            }
            Util.showToast(FeedbackActivity.this, error.getPlainDescription(true));            
        }// 获取错误码描述}

        // 开始录音
        public void onBeginOfSpeech() {
        	BaseTools.showlog("开始说话");
        	Util.showToast(FeedbackActivity.this, "开始说话");
        }

        // 结束录音
        public void onEndOfSpeech() {
        	BaseTools.showlog("说话结束");
        	Util.showToast(FeedbackActivity.this, "说话结束");
        }

        // 扩展用接口
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }

        //音量
        public void onVolumeChanged(int volume, byte[] data) {
            BaseTools.showlog("返回音频数据："+data.length);
            Util.showToast(FeedbackActivity.this, "当前正在说话，音量大小：" + volume);
        }

		public void onEvent(int arg0, int arg1, int arg2, String arg3) {
			// 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
			// 若使用本地能力，会话id为null
			//	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
			//		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
			//		Log.d(TAG, "session id =" + sid);
			//	}
		}

    };

    /**
     * 听写UI监听器
     */
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        public void onResult(RecognizerResult results, boolean isLast) {
            printResult(results);
        }

        /**
         * 识别回调错误.
         */
        public void onError(SpeechError error) {
            Util.showToast(FeedbackActivity.this, error.getPlainDescription(true));
        }

    };
    
	private void printResult(RecognizerResult results) {
		BaseTools.showlog("results.getResultString()="+results.getResultString());
		String text = JsonParser.parseIatResult(results.getResultString());
		String sn = null;
		// 读取json结果中的sn字段
		try {
			JSONObject resultJson = new JSONObject(results.getResultString());
			sn = resultJson.optString("sn");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		mIatResults.put(sn, text);

		StringBuffer resultBuffer = new StringBuffer();
		for (String key : mIatResults.keySet()) {
			resultBuffer.append(mIatResults.get(key));
		}

		feedback_et.setText(resultBuffer.toString());
		feedback_et.setSelection(feedback_et.length());
	}
	
}

package com.huaxun.menuLeft.activity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.huaxun.R;
import com.huaxun.base.BaseActivity;
import com.huaxun.tool.BaseTools;
import com.huaxun.utils.FileUtil;
import com.huaxun.utils.Util;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeakerVerifier;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechListener;
import com.iflytek.cloud.VerifierListener;
import com.iflytek.cloud.VerifierResult;

public class IsvPwdActivity extends BaseActivity implements OnClickListener {
	
	private static final int PWD_TYPE_TEXT = 1;
	// 自由说由于效果问题，暂不开放
//	private static final int PWD_TYPE_FREE = 2;
	private static final int PWD_TYPE_NUM = 3;
	// 当前声纹密码类型，1、2、3分别为文本、自由说和数字密码
	private int mPwdType = PWD_TYPE_TEXT;
	// 声纹识别对象
	private SpeakerVerifier mVerifier;
	// 声纹AuthId，用户在云平台的身份标识，也是声纹模型的标识
	// 请使用英文字母或者字母和数字的组合，勿使用中文字符
	private String mAuthId = "";
	// 文本声纹密码
	private String mTextPwd = "";
	// 数字声纹密码
	private String mNumPwd = "";
	// 数字声纹密码段，默认有5段
	private String[] mNumPwdSegs;

	private EditText mResultEditText;
	private TextView mAuthIdTextView;
	private RadioGroup mPwdTypeGroup;
	private TextView mShowPwdTextView;
	private TextView mShowMsgTextView;
	private TextView mShowRegFbkTextView;
	private AlertDialog mTextPwdSelectDialog;
	
	private Toast mToast;
	private ImageView imageToast;
	private TextView title, back;
	private SharedPreferences mSharedPreferences;
	private Handler handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Util.setTranslucent(this);
		setContentView(R.layout.activity_isv_demo);		
		initUi();
		mSharedPreferences = getSharedPreferences(IsvSettings.PREFER_NAME, Activity.MODE_PRIVATE);
		// 将设置页面的用户名作为AuthId
		mAuthId = mSharedPreferences.getString("isv_username_preference","Watson");
		mAuthIdTextView.setText(mAuthId);
		//是否是root权限，是的话可以注册，查看，删除模型
		if (mSharedPreferences.getBoolean(getString(R.string.pref_key_isv_root), false)) {	
			findViewById(R.id.root_ll).setVisibility(View.VISIBLE);
		}
		
		// 初始化SpeakerVerifier，InitListener为初始化完成后的回调接口
		mVerifier = SpeakerVerifier.createVerifier(this, new InitListener() {			
			public void onInit(int errorCode) {
				if (ErrorCode.SUCCESS == errorCode) {
					Util.showToast(IsvPwdActivity.this, "引擎初始化成功");
				} else {
					Util.showToast(IsvPwdActivity.this, "引擎初始化失败，错误码：" + errorCode);
				}
			}
		});
	}
	
	private void initUi() {
		title = (TextView)findViewById(R.id.topTitle);
		title.setText("声纹解锁");
		
		mResultEditText = (EditText) findViewById(R.id.edt_result);
		mAuthIdTextView = (TextView) findViewById(R.id.txt_authorid);
		mShowPwdTextView = (TextView) findViewById(R.id.showPwd);
		mShowMsgTextView = (TextView) findViewById(R.id.showMsg);
		mShowRegFbkTextView = (TextView) findViewById(R.id.showRegFbk);
		
		findViewById(R.id.topBack).setOnClickListener(this);
		findViewById(R.id.isv_getpassword).setOnClickListener(this);
		findViewById(R.id.isv_verify).setOnClickListener(this);
		findViewById(R.id.isv_stop_record).setOnClickListener(this);
		findViewById(R.id.isv_register).setOnClickListener(this);		
		findViewById(R.id.isv_search).setOnClickListener(this);
		findViewById(R.id.isv_delete).setOnClickListener(this);
		findViewById(R.id.isv_cancel).setOnClickListener(this);
		
		// 密码选择RadioGroup初始化
		mPwdTypeGroup = (RadioGroup) findViewById(R.id.radioGroup);
		mPwdTypeGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				initTextView();
				switch (checkedId) {
				case R.id.radioText:
					mPwdType = PWD_TYPE_TEXT;
					break;
				case R.id.radioNumber:
					mPwdType = PWD_TYPE_NUM;
					break;
				default:
					break;
				}
			}
		});

		//自定义只含图片的Toast
		View toastLayout = getLayoutInflater().inflate(R.layout.image_toast, null);
		imageToast = (ImageView) toastLayout.findViewById(R.id.imageToast);
		mToast = new Toast(this);
		mToast.setGravity(Gravity.CENTER, 0, 0);
		mToast.setDuration(Toast.LENGTH_SHORT);
		mToast.setView(toastLayout);
	}
	
	/**
	 * 初始化TextView和密码文本
	 */
	private void initTextView(){
		mTextPwd = null;
		mNumPwd = null;
		mResultEditText.setText("");
		mShowPwdTextView.setText("");
		mShowMsgTextView.setText("");
		mShowRegFbkTextView.setText("");
	}
	
	/**
	 * 设置radio的状态
	 */
	private void setRadioClickable(boolean clickable){
		// 设置RaioGroup状态为非按下状态
		mPwdTypeGroup.setPressed(false);
		findViewById(R.id.radioText).setClickable(clickable);
		findViewById(R.id.radioNumber).setClickable(clickable);
	}
	
	/**
	 * 执行模型操作
	 *  
	 * @param operation 操作命令
	 * @param listener  操作结果回调对象
	 */
	private void performModelOperation(String operation, SpeechListener listener) {
		// 清空参数
		mVerifier.setParameter(SpeechConstant.PARAMS, null);
		mVerifier.setParameter(SpeechConstant.ISV_PWDT, "" + mPwdType);
		
		if (mPwdType == PWD_TYPE_TEXT) {
			// 文本密码删除需要传入密码
			if (TextUtils.isEmpty(mTextPwd)) {
				Util.showToast(this, "请获取密码后进行操作");
				return;
			}
			mVerifier.setParameter(SpeechConstant.ISV_PWD, mTextPwd);
		} else if (mPwdType == PWD_TYPE_NUM) {
			// 数字密码删除不需要传入密码
		}
		setRadioClickable(false);
		// 设置auth_id，不能设置为空
		mVerifier.sendRequest(operation, mAuthId, listener);
	}
	
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.topBack:
			finish();
			overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
			break;
		case R.id.isv_getpassword:
			// 获取密码之前先终止之前的注册或验证过程
			mVerifier.cancel();
			initTextView();
			setRadioClickable(false);
			// 清空参数
			mVerifier.setParameter(SpeechConstant.PARAMS, null);
			mVerifier.setParameter(SpeechConstant.ISV_PWDT, "" + mPwdType);
			mVerifier.getPasswordList(mPwdListenter);
			break;
		case R.id.isv_search:
			performModelOperation("que", mModelOperationListener);
			break;
		case R.id.isv_delete:
			performModelOperation("del", mModelOperationListener);
			break;
		case R.id.isv_register:
			// 清空参数
			mVerifier.setParameter(SpeechConstant.PARAMS, null);
			mVerifier.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
			mVerifier.setParameter(SpeechConstant.ISV_AUDIO_PATH, FileUtil.getCacheAudioPath() + "/isv_register.wav");
			// 对于某些麦克风非常灵敏的机器，如nexus、samsung i9300等，建议加上以下设置对录音进行消噪处理
//			mVerify.setParameter(SpeechConstant.AUDIO_SOURCE, "" + MediaRecorder.AudioSource.VOICE_RECOGNITION);
			if (mPwdType == PWD_TYPE_TEXT) {
				// 文本密码注册需要传入密码
				if (TextUtils.isEmpty(mTextPwd)) {
					Util.showToast(this, "请获取密码后进行操作");
					return;
				}
				mVerifier.setParameter(SpeechConstant.ISV_PWD, mTextPwd);
				mShowPwdTextView.setText("请读出：" + mTextPwd);
				mShowMsgTextView.setText("训练 第" + 1 + "遍，剩余4遍");
			} else if (mPwdType == PWD_TYPE_NUM) {
				// 数字密码注册需要传入密码
				if (TextUtils.isEmpty(mNumPwd)) {
					Util.showToast(this, "请获取密码后进行操作");
					return;
				}
				mVerifier.setParameter(SpeechConstant.ISV_PWD, mNumPwd);
				mShowPwdTextView.setText("请读出：" + mNumPwd.substring(0, 8));
				mShowMsgTextView.setText("训练 第" + 1 + "遍，剩余4遍");
			}
			setRadioClickable(false);
			// 设置auth_id，不能设置为空
			mVerifier.setParameter(SpeechConstant.AUTH_ID, mAuthId);
			// 设置业务类型为注册
			mVerifier.setParameter(SpeechConstant.ISV_SST, "train");
			// 设置声纹密码类型
			mVerifier.setParameter(SpeechConstant.ISV_PWDT, "" + mPwdType);
			// 开始注册
			mVerifier.startListening(mRegisterListener);
			break;
		case R.id.isv_verify:
			// 清空提示信息
			mShowMsgTextView.setText("");
			// 清空参数
			mVerifier.setParameter(SpeechConstant.PARAMS, null);
			mVerifier.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
			mVerifier.setParameter(SpeechConstant.ISV_AUDIO_PATH, FileUtil.getCacheAudioPath() + "/isv_verify.wav");
			mVerifier = SpeakerVerifier.getVerifier();
			// 设置业务类型为验证
			mVerifier.setParameter(SpeechConstant.ISV_SST, "verify");
			// 对于某些麦克风非常灵敏的机器，如nexus、samsung i9300等，建议加上以下设置对录音进行消噪处理
//			mVerify.setParameter(SpeechConstant.AUDIO_SOURCE, "" + MediaRecorder.AudioSource.VOICE_RECOGNITION);
			
			if (mPwdType == PWD_TYPE_TEXT) {
				// 文本密码注册需要传入密码
				if (TextUtils.isEmpty(mTextPwd)) {
					Util.showToast(this, "请获取密码后进行操作");
					return;
				}
				mVerifier.setParameter(SpeechConstant.ISV_PWD, mTextPwd);
				mShowPwdTextView.setText("请读出：" + mTextPwd);
			} else if (mPwdType == PWD_TYPE_NUM) {
				// 数字密码注册需要传入密码
				String verifyPwd = mVerifier.generatePassword(8);
				mVerifier.setParameter(SpeechConstant.ISV_PWD, verifyPwd);
				mShowPwdTextView.setText("请读出：" + verifyPwd);
			}
			setRadioClickable(false);
			// 设置auth_id，不能设置为空
			mVerifier.setParameter(SpeechConstant.AUTH_ID, mAuthId);
			mVerifier.setParameter(SpeechConstant.ISV_PWDT, "" + mPwdType);
			// 开始验证
			mVerifier.startListening(mVerifyListener);
			break;
		case R.id.isv_stop_record:
			mVerifier.stopListening();
			break;
		case R.id.isv_cancel:
			setRadioClickable(true);
			mVerifier.cancel();
			initTextView();
			break;
		default:
			break;
		}
	}
	
	//获取密码的监听器
	private String[] items;
	private SpeechListener mPwdListenter = new SpeechListener() {
		@Override
		public void onEvent(int eventType, Bundle params) {}
		
		@Override
		public void onBufferReceived(byte[] buffer) {
			setRadioClickable(true);			
			String result = new String(buffer);
			BaseTools.showlog("获取密码-result="+result);
			
			switch (mPwdType) {
			case PWD_TYPE_TEXT:
				try {
					JSONObject object = new JSONObject(result);
					if (!object.has("txt_pwd")) {
						initTextView();
						return;
					}
					
					JSONArray pwdArray = object.optJSONArray("txt_pwd");
					items = new String[pwdArray.length()];
					for (int i = 0; i < pwdArray.length(); i++) {
						items[i] = pwdArray.getString(i);
					}
					mTextPwdSelectDialog = new AlertDialog.Builder(IsvPwdActivity.this).setTitle("请选择密码文本")
							.setItems(items,new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface arg0, int arg1) {
										mTextPwd = items[arg1];
										mResultEditText.setText("您的密码：\n" + mTextPwd);
									}
								}).create();
					mTextPwdSelectDialog.show();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			case PWD_TYPE_NUM:
				StringBuffer numberString = new StringBuffer();
				try {
					JSONObject object = new JSONObject(result);
					if (!object.has("num_pwd")) {
						initTextView();
						return;
					}
					
					JSONArray pwdArray = object.optJSONArray("num_pwd");
					numberString.append(pwdArray.get(0));
					for (int i = 1; i < pwdArray.length(); i++) {
						numberString.append("-" + pwdArray.get(i));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				mNumPwd = numberString.toString();
				mNumPwdSegs = mNumPwd.split("-");
				mResultEditText.setText("您的密码：\n" + mNumPwd);
				break;
			default:
				break;
			}
		}

		@Override
		public void onCompleted(SpeechError error) {
			setRadioClickable(true);			
			if (null != error && ErrorCode.SUCCESS != error.getErrorCode()) {
				Util.showToast(IsvPwdActivity.this, "获取密码失败：" + error.getErrorCode());
			}
		}
	};
	
	//查询模型，删除模型的监听器
	private SpeechListener mModelOperationListener = new SpeechListener() {
		
		@Override
		public void onEvent(int eventType, Bundle params) {}
		
		@Override
		public void onBufferReceived(byte[] buffer) {
			setRadioClickable(true);			
			String result = new String(buffer);
			BaseTools.showlog("查询模型，删除模型-result="+result);
			
			try {
				JSONObject object = new JSONObject(result);
				String cmd = object.getString("cmd");
				int ret = object.getInt("ret");
				
				if ("del".equals(cmd)) {
					if (ret == ErrorCode.SUCCESS) {
						Util.showToast(IsvPwdActivity.this, "删除成功");
						mResultEditText.setText("");
					} else if (ret == ErrorCode.MSP_ERROR_FAIL) {
						Util.showToast(IsvPwdActivity.this, "删除失败，模型不存在");
					}
				} else if ("que".equals(cmd)) {
					if (ret == ErrorCode.SUCCESS) {
						Util.showToast(IsvPwdActivity.this, "模型存在");
					} else if (ret == ErrorCode.MSP_ERROR_FAIL) {
						Util.showToast(IsvPwdActivity.this, "模型不存在");
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void onCompleted(SpeechError error) {
			setRadioClickable(true);
			
			if (null != error && ErrorCode.SUCCESS != error.getErrorCode()) {
				Util.showToast(IsvPwdActivity.this, "操作失败：" + error.getPlainDescription(true));
			}
		}
	};
	
	//注册模型的监听器
	private VerifierListener mRegisterListener = new VerifierListener() {

		@Override
		public void onVolumeChanged(int volume, byte[] data) {
			showVolumeToast(volume);
//			BaseTools.showlog("返回音频数据："+data.length);
		}

		@Override
		public void onResult(VerifierResult result) {
			BaseTools.showlog("注册模型-result="+result.toString());
			mShowMsgTextView.setText(result.source);
			
			if (result.ret == ErrorCode.SUCCESS) {
				switch (result.err) {
				case VerifierResult.MSS_ERROR_IVP_GENERAL:
					mShowMsgTextView.setText("内核异常");
					break;
				case VerifierResult.MSS_ERROR_IVP_EXTRA_RGN_SOPPORT:
					mShowRegFbkTextView.setText("训练达到最大次数");
					break;
				case VerifierResult.MSS_ERROR_IVP_TRUNCATED:
					mShowRegFbkTextView.setText("出现截幅");
					break;
				case VerifierResult.MSS_ERROR_IVP_MUCH_NOISE:
					mShowRegFbkTextView.setText("太多噪音");
					break;
				case VerifierResult.MSS_ERROR_IVP_UTTER_TOO_SHORT:
					mShowRegFbkTextView.setText("录音太短");
					break;
				case VerifierResult.MSS_ERROR_IVP_TEXT_NOT_MATCH:
					mShowRegFbkTextView.setText("训练失败，您所读的文本不一致");
					break;
				case VerifierResult.MSS_ERROR_IVP_TOO_LOW:
					mShowRegFbkTextView.setText("音量太低");
					break;
				case VerifierResult.MSS_ERROR_IVP_NO_ENOUGH_AUDIO:
					mShowMsgTextView.setText("音频长达不到自由说的要求");
				default:
					mShowRegFbkTextView.setText("");
					break;
				}
				
				if (result.suc == result.rgn) {
					setRadioClickable(true);
					mShowMsgTextView.setText("注册成功");
					
					if (PWD_TYPE_TEXT == mPwdType) {
						mResultEditText.setText("您的文本密码声纹ID：\n" + result.vid);
					} else if (PWD_TYPE_NUM == mPwdType) {
						mResultEditText.setText("您的数字密码声纹ID：\n" + result.vid);
					}					
				} else {
					int nowTimes = result.suc + 1;
					int leftTimes = result.rgn - nowTimes;
					
					if (PWD_TYPE_TEXT == mPwdType) {
						mShowPwdTextView.setText("请读出：" + mTextPwd);
					} else if (PWD_TYPE_NUM == mPwdType) {
						mShowPwdTextView.setText("请读出：" + mNumPwdSegs[nowTimes - 1]);
					}
				
					mShowMsgTextView.setText("训练 第" + nowTimes + "遍，剩余" + leftTimes + "遍");
				}
			}else {
				setRadioClickable(true);				
				mShowMsgTextView.setText("注册失败，请重新开始。");	
			}
		}
		// 保留方法，暂不用
		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle arg3) {
			// 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
			//	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
			//		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
			//		Log.d(TAG, "session id =" + sid);
			//	}
		}

		@Override
		public void onError(SpeechError error) {
			setRadioClickable(true);
			
			if (error.getErrorCode() == ErrorCode.MSP_ERROR_ALREADY_EXIST) {
				Util.showToast(IsvPwdActivity.this, "模型已存在，如需重新注册，请先删除");
			} else {
				Util.showToast(IsvPwdActivity.this, "注册失败  Code：" + error.getPlainDescription(true));
			}
		}

		@Override
		public void onEndOfSpeech() {
			Util.showToast(IsvPwdActivity.this, "结束说话");
		}

		@Override
		public void onBeginOfSpeech() {
			Util.showToast(IsvPwdActivity.this, "开始说话");
		}
	};
	
	//验证模型监听器
	private VerifierListener mVerifyListener = new VerifierListener() {

		@Override
		public void onVolumeChanged(int volume, byte[] data) {
			showVolumeToast(volume);
//			BaseTools.showlog("返回音频数据："+data.length);
		}

		@Override
		public void onResult(VerifierResult result) {
			setRadioClickable(true);
			BaseTools.showlog("验证模型-result="+result.toString());
			mShowMsgTextView.setText(result.source);
			
			if (result.ret == 0) {
				// 验证通过
				mShowMsgTextView.setText("验证通过");
				Util.showToast(IsvPwdActivity.this, "验证通过");
				handler.postDelayed(new Runnable(){
					public void run() {
						startActivity(new Intent(IsvPwdActivity.this, EncryptActivity.class));
						overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
						finish();
					}					
				}, 1000);				
				
			} else {
				// 验证不通过
				switch (result.err) {
				case VerifierResult.MSS_ERROR_IVP_GENERAL:
					mShowMsgTextView.setText("内核异常");
					break;
				case VerifierResult.MSS_ERROR_IVP_TRUNCATED:
					mShowMsgTextView.setText("出现截幅");
					break;
				case VerifierResult.MSS_ERROR_IVP_MUCH_NOISE:
					mShowMsgTextView.setText("太多噪音");
					break;
				case VerifierResult.MSS_ERROR_IVP_UTTER_TOO_SHORT:
					mShowMsgTextView.setText("录音太短");
					break;
				case VerifierResult.MSS_ERROR_IVP_TEXT_NOT_MATCH:
					mShowMsgTextView.setText("验证不通过，您所读的文本不一致");
					break;
				case VerifierResult.MSS_ERROR_IVP_TOO_LOW:
					mShowMsgTextView.setText("音量太低");
					break;
				case VerifierResult.MSS_ERROR_IVP_NO_ENOUGH_AUDIO:
					mShowMsgTextView.setText("音频长达不到自由说的要求");
					break;
				default:
					mShowMsgTextView.setText("验证不通过");
					break;
				}
			}
		}
		// 保留方法，暂不用
		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle arg3) {
			// 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
			//	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
			//		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
			//		Log.d(TAG, "session id =" + sid);
			//	}
		}

		@Override
		public void onError(SpeechError error) {
			setRadioClickable(true);
			
			switch (error.getErrorCode()) {
			case ErrorCode.MSP_ERROR_NOT_FOUND:
				mShowMsgTextView.setText("模型不存在，请先注册");
				break;
			default:
				Util.showToast(IsvPwdActivity.this, "验证错误 Code："	+ error.getPlainDescription(true));
				break;
			}
		}

		@Override
		public void onEndOfSpeech() {
			// 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
			Util.showToast(IsvPwdActivity.this, "结束说话");
		}

		@Override
		public void onBeginOfSpeech() {
			// 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
			Util.showToast(IsvPwdActivity.this, "开始说话");
		}
	};
	
	private void showVolumeToast(final int volume) {
		Util.hideToast();
		if (volume == 0) {
			imageToast.setImageResource(R.drawable.voice_volume_0);
		} else if (volume > 0 && volume < 10) {
			imageToast.setImageResource(R.drawable.voice_volume_1);
		} else if (volume >= 10 && volume < 20) {
			imageToast.setImageResource(R.drawable.voice_volume_2);
		} else if (volume >= 20 && volume < 30) {
			imageToast.setImageResource(R.drawable.voice_volume_3);
		} else if (volume >= 30) {
			imageToast.setImageResource(R.drawable.voice_volume_4);
		}
		mToast.show();
	}

}

package com.huaxun.menuRight.activity;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckedTextView;
import android.widget.TextView;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.tencent.weibo.TencentWeibo;
import cn.sharesdk.wechat.friends.Wechat;

import com.huaxun.R;
import com.huaxun.tool.BaseTools;
import com.huaxun.utils.Util;

/**
 * @author yangyu
 *	功能描述：授权和取消授权Activity，由于UI显示需要授权过的平台显示账户的名称，
 *	  因此此页面事实上展示的是“获取用户资料”和“取消授权”两个功能。
 */
public class AuthActivity extends Activity implements OnClickListener, PlatformActionListener {
	private TextView topBack,topTitle;
	//定义CheckedTextView对象
	private CheckedTextView	 sinaCt,qzoneCt,tengxunCt,wechatCt;
	private String[] platList = {SinaWeibo.NAME, TencentWeibo.NAME, QZone.NAME, Wechat.NAME};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Util.setTranslucent(this);
		setContentView(R.layout.activity_auth);
		
		initView();		
		initData();
	}

	/**
	 * 初始化组件
	 */
	private void initView(){
		//得到标题栏对象
		topBack = (TextView)this.findViewById(R.id.topBack);
		topTitle = (TextView)this.findViewById(R.id.topTitle);	
		
		//得到组件对象
		sinaCt    = (CheckedTextView)findViewById(R.id.ctvSw);
		qzoneCt   = (CheckedTextView)findViewById(R.id.ctvQz);
		tengxunCt = (CheckedTextView)findViewById(R.id.ctvTc);
		wechatCt = (CheckedTextView)findViewById(R.id.ctvWc);
	}
	
	/**
	 * 初始化数据
	 */
	private void initData(){
		topBack.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				finish();
				overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
			}
		});
		topTitle.setText("用户授权管理");
		
		//设置监听
		sinaCt.setOnClickListener(this);
		qzoneCt.setOnClickListener(this);
		tengxunCt.setOnClickListener(this);		
		wechatCt.setOnClickListener(this);	
		
		for(int i = 0; i < platList.length; i++){
			Platform plat = ShareSDK.getPlatform(this, platList[i]);
			if (!plat.isValid()) {
				continue;
			}		
			CheckedTextView ctv = getView(platList[i]);
			if (ctv != null) {
				ctv.setChecked(true);
				// 得到授权用户的用户名称
				String userName = plat.getDb().get("nickname");
				if (userName == null || userName.length() <= 0 || "null".equals(userName)) {
					// 如果平台已经授权却没有拿到帐号名称，则自动获取用户资料，以获取名称
					userName = plat.getName();
					//添加平台事件监听
					plat.setPlatformActionListener(this);
					//显示用户资料，null表示显示自己的资料
					plat.showUser(null);
				}
				ctv.setText(userName);
			}
		}
	}
	
	/**
	 * 在CheckedTextView组件中显示授权用户的名称
	 */
	private CheckedTextView getView(String name) {
		if (name == null) {
			return null;
		}		
		View v = null;
		if (SinaWeibo.NAME.equals(name)) {
			v = findViewById(R.id.ctvSw);
		} else if (TencentWeibo.NAME.equals(name)) {
			v = findViewById(R.id.ctvTc);
		} else if (QZone.NAME.equals(name)) {
			v = findViewById(R.id.ctvQz);
		}  else if (Wechat.NAME.equals(name)) {
			v = findViewById(R.id.ctvWc);
		}		
		
		if (v == null) {
			return null;
		}		
		if (! (v instanceof CheckedTextView)) {
			return null;
		}	
		return (CheckedTextView) v;
	}
	
	/**
	 * 授权和取消授权的按钮点击监听事件
	 */
	@Override
	public void onClick(View v) {				
		Platform plat = getWeibo(v.getId());
	
		CheckedTextView ctv = (CheckedTextView) v;
		if (plat == null) {
			ctv.setChecked(false);
			ctv.setText("尚未授权");
			return;
		}
		
		if (plat.isValid()) {
			plat.removeAccount();
			ctv.setChecked(false);
			ctv.setText("尚未授权");
			return;
		}
		
		plat.setPlatformActionListener(this);
		//关闭SSO授权
		if (plat.getName().equals(SinaWeibo.NAME)) {
			plat.SSOSetting(true);
		}		
		plat.showUser(null);
	}

	/**
	 * 获得授权
	 */
	private Platform getWeibo(int vid) {
		String name = null;
		switch (vid) {
		// 进入新浪微博的授权页面
		case R.id.ctvSw:
			name = SinaWeibo.NAME;
			break;
		// 进入腾讯微博的授权页面
		case R.id.ctvTc:
			name = TencentWeibo.NAME;
			break;
		// 进入QQ空间的授权页面
		case R.id.ctvQz:
			name = QZone.NAME;
			break;
		// 进入微信的授权页面	
		case R.id.ctvWc:
			name = Wechat.NAME;
			break;
		}		
		if (name != null) {
			return ShareSDK.getPlatform(this, name);
		}
		return null;
	}		

	/**
	 * 授权成功的回调
	 *  weibo - 回调的平台
	 *	action - 操作的类型
	 *	res - 请求的数据通过res返回
	 */
	@Override
	public void onComplete(Platform plat, int action,HashMap<String, Object> res) {
		BaseTools.showlog(plat.getName() + " completed at " + actionToString(action));
		Message msg = new Message();
		msg.obj = plat;
		handler.sendMessage(msg);		
	}

	/**
	 * 授权失败的回调
	 */
	@Override
	public void onError(Platform plat, int action, Throwable t) {
		t.printStackTrace();
		BaseTools.showlog(plat.getName() + " caught error at " + actionToString(action));	
	}
	
	/**
	 * 取消授权的回调
	 */
	@Override
	public void onCancel(Platform plat, int action) {
		BaseTools.showlog(plat.getName() + " canceled at " + actionToString(action));		
	}

	/** 
	 * 处理从授权页面返回的结果
	 * 
	 * 如果获取到用户的名称，则显示名称；否则如果已经授权，则显示平台名称
	 */
	/** 处理操作结果  */
	@SuppressLint("HandlerLeak")
	public Handler handler = new Handler() {
        public void handleMessage(Message msg) {
        	super.handleMessage(msg);
        	Platform plat = (Platform) msg.obj;
    		CheckedTextView ctv = getView(plat.getName());
    		if (ctv != null) {
    			ctv.setChecked(true);
    			String userName = plat.getDb().get("nickname");
    			if (userName == null || userName.length() <= 0 || "null".equals(userName)) {
    				userName = plat.getName();
    			}
    			ctv.setText(userName);
    		}
        }
	};
	
	/**
	 * 将action转换为String
	 */
	public static String actionToString(int action) {
		switch (action) {
			case Platform.ACTION_AUTHORIZING: return "ACTION_AUTHORIZING";
			case Platform.ACTION_GETTING_FRIEND_LIST: return "ACTION_GETTING_FRIEND_LIST";
			case Platform.ACTION_FOLLOWING_USER: return "ACTION_FOLLOWING_USER";
			case Platform.ACTION_SENDING_DIRECT_MESSAGE: return "ACTION_SENDING_DIRECT_MESSAGE";
			case Platform.ACTION_TIMELINE: return "ACTION_TIMELINE";
			case Platform.ACTION_USER_INFOR: return "ACTION_USER_INFOR";
			case Platform.ACTION_SHARE: return "ACTION_SHARE";
			default: {
				return "UNKNOWN";
			}
		}
	}
	
}

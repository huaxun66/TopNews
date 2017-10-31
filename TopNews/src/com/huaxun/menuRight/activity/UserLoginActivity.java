package com.huaxun.menuRight.activity;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.tencent.weibo.TencentWeibo;
import cn.sharesdk.wechat.friends.Wechat;

import com.google.gson.Gson;
import com.huaxun.R;
import com.huaxun.app.AppApplication;
import com.huaxun.menuRight.JsonUtils;
import com.huaxun.menuRight.bean.UserInfo;
import com.huaxun.tool.BaseTools;
import com.huaxun.tool.NewsUrls;
import com.huaxun.utils.FileUtil;
import com.huaxun.utils.HttpUtil;
import com.huaxun.utils.NetUtil;
import com.huaxun.utils.Util;

/**
 * @author huaxun
 * @Create 2016-03-20
 * @Module core
 * @Description 用户登入界面
 */
public class UserLoginActivity extends Activity implements OnClickListener, PlatformActionListener {
	private TextView topBack, topTitle;
	private Button loginbtn, regist_btn, search_secret_btn;
	private EditText login_account_edittext, login_secret_edittext;
	private ImageView btn_sina, btn_tencent, btn_qq;
	private TextView btn_wechat;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Util.setTranslucent(this);
		setContentView(R.layout.userlogin_activity_layout);

		topBack = (TextView) findViewById(R.id.topBack);
		topTitle = (TextView) findViewById(R.id.topTitle);
		loginbtn = (Button) findViewById(R.id.login_activity_loginbtn);
		regist_btn = (Button) findViewById(R.id.login_activity_registbtn);
		search_secret_btn = (Button) findViewById(R.id.login_activity_searchsecretbtn);
		login_account_edittext = (EditText) findViewById(R.id.login_account_edittext);
		login_secret_edittext = (EditText) findViewById(R.id.login_secret_edittext);
		btn_sina = (ImageView) findViewById(R.id.btn_sina);
		btn_tencent = (ImageView) findViewById(R.id.btn_tencent);
		btn_qq = (ImageView) findViewById(R.id.btn_qq);
		btn_wechat = (TextView) findViewById(R.id.btn_wechat);

		topTitle.setText("登录");
		topBack.setOnClickListener(this);
		loginbtn.setOnClickListener(this);
		regist_btn.setOnClickListener(this);
		search_secret_btn.setOnClickListener(this);
		btn_sina.setOnClickListener(this);
		btn_tencent.setOnClickListener(this);
		btn_qq.setOnClickListener(this);
		btn_wechat.setOnClickListener(this);

	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.topBack:
			finish();
			overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
			break;
		case R.id.login_activity_loginbtn:
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(loginbtn.getWindowToken(), 0);

			if (login_account_edittext.getText().toString().equals("")
					|| login_secret_edittext.getText().toString().equals("")) {
				Toast.makeText(this, "用户名或密码不能为空！", Toast.LENGTH_SHORT).show();
			} else {
				if (NetUtil.isNetworkAvailable(this)) {
					new UserLoginTask().execute();
				} else {
					Toast.makeText(this, "请开启网络！", Toast.LENGTH_SHORT).show();
				}
			}
			break;
		case R.id.login_activity_registbtn:
			Intent intent = new Intent(this, UserRegistActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			break;
		case R.id.login_activity_searchsecretbtn:
			
			break;
		case R.id.btn_qq:
			//QQ空间
			thirdLogIn(QZone.NAME);
			break;
		case R.id.btn_sina:
			//新浪微博
			thirdLogIn(SinaWeibo.NAME);
			break;
		case R.id.btn_tencent:
			//腾讯微博
			thirdLogIn(TencentWeibo.NAME);
			break;
		case R.id.btn_wechat:
			//微信登录
			//测试时，需要打包签名；sample测试时，用项目里面的android.keystore 密码huaxun100
			//打包签名apk,然后才能产生微信的登录
			thirdLogIn(Wechat.NAME);  //这种方式需要认证开发者(300大洋)后开放微信登录接口，不然会报错误：scope参数错误或没有scope权限			
			break;
		}
	}
	
	private void thirdLogIn(String name) {
		if (name != null) {
			Platform plat = ShareSDK.getPlatform(this, name);
			plat.setPlatformActionListener(this);
			//关闭SSO授权
			if (name.equals(SinaWeibo.NAME)) {
				plat.SSOSetting(true);
			}		
			plat.showUser(null);
		}
	}


	@Override
	public void onComplete(Platform plat, int action, HashMap<String, Object> res) {
		Message msg = new Message();
		if (plat.getName().equals(QZone.NAME)) {
			msg.what = 1;
		} else if (plat.getName().equals(SinaWeibo.NAME)) {
			msg.what = 2;
		} else if (plat.getName().equals(TencentWeibo.NAME)) {
			msg.what = 3;
		} else if (plat.getName().equals(Wechat.NAME)) {
			msg.what = 4;
		}	
		JsonUtils ju = new JsonUtils();
		JSONObject jsonObject = ju.getJSONObject(res);
		msg.obj = jsonObject;
		loginHandler.sendMessage(msg);
	}

	@Override
	public void onError(Platform plat, int action, Throwable t) {
		t.printStackTrace();
        BaseTools.showlog(plat.getName() + " caught error at " + AuthActivity.actionToString(action));
	}
	
	@Override
	public void onCancel(Platform plat, int action) {
		BaseTools.showlog(plat.getName() + " canceled at " + AuthActivity.actionToString(action));
	}
	
	
	/** 处理操作结果  */
	public Handler loginHandler = new Handler() {
        public void handleMessage(Message msg) {
        	super.handleMessage(msg);
        	JSONObject jsonObject = (JSONObject) msg.obj;
    		BaseTools.showlog("json="+jsonObject.toString());
    		String useName = "", iconURL = "", iconURL2 = "", gender = "", location = "", description = "";   		
        	try {
        		switch (msg.what) {
        		case 1: //QQ
        			useName = jsonObject.getString("nickname");
        			iconURL = jsonObject.getString("figureurl_qq_2");
        			iconURL2 = jsonObject.getString("figureurl_2");		
        			gender = jsonObject.getString("gender");
        			location = jsonObject.getString("province") + " " + jsonObject.getString("city");
        			break;
        		case 2: //新浪微博
        			useName = jsonObject.getString("name");
        			iconURL = jsonObject.getString("avatar_hd");
        			iconURL2 = jsonObject.getString("cover_image_phone");		
        			gender = jsonObject.getString("gender").equals("m") ? "男" : "女";
        			location = jsonObject.getString("location");
        			description = jsonObject.getString("description");
        			break;
        		case 3: //腾讯微博
        			useName = jsonObject.getString("nick");
        			iconURL = jsonObject.getString("head");
        			iconURL2 = jsonObject.getString("https_head");
        			gender = jsonObject.getInt("sex") == 1 ? "男" : "女";
        			location = jsonObject.getString("location");
        			description = jsonObject.getString("introduction");		
        			break;
        		case 4: //微信
        			break;
        		}
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
    		
    		UserInfo userInfo = new UserInfo(useName, iconURL, iconURL2, gender, location, description, true);
    		AppApplication.mUserInfo = userInfo;
    		FileUtil.saveUserInfo(UserLoginActivity.this, userInfo);// 保存到内部文件
    		// 发送广播刷新用户信息
    		Intent refreshInfoIntent = new Intent("REFRESH_INFO");
    		refreshInfoIntent.putExtra("type", "LOGIN");
    		sendBroadcast(refreshInfoIntent);
    		Toast.makeText(UserLoginActivity.this, "登录成功",Toast.LENGTH_SHORT).show();
    		finish();
    		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
	};


	class UserLoginTask extends AsyncTask<Void, Void, String> {
		String loginUrl = NewsUrls.USER_LOGIN;
		String downloadUrl = NewsUrls.DOWNLOAD_FILE;

		ProgressDialog progressDialog;
		UserInfo userInfo = null;

		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(UserLoginActivity.this, null,"登录中...");
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Void... arg0) {
			String result = null;
			String getIconResult = null;

			String username = login_account_edittext.getText().toString();
			String password = login_secret_edittext.getText().toString();

			Map<String, String> params = new HashMap<String, String>();
			params.put("username", username);
			params.put("password", password);

			try {
				// get方式
				result = HttpUtil.sendGetRequest(loginUrl, params, "utf-8");
				// post方式
				// result = HttpUtil.sendPostRequest(loginUrl, params, "utf-8");
				// HttpClient方式
				// result = HttpUtil.sendHttpClientWithPost(loginUrl, params,
				// "utf-8");
			} catch (Exception e) {
				e.printStackTrace();
			}

			userInfo = getUserInfoByJson(result);
			userInfo.setIsThirdLogin(false);
			if (userInfo != null && !userInfo.getUsericon().equals("null")) {
				final String dirPath = FileUtil.getUserInfoPath() + "/remoteIcon";
				final String filename = userInfo.getUsername() + "_icon.png";
				File mDir = new File(dirPath);
				if (!mDir.exists()) {
					mDir.mkdir();
				}
				if (!new File(dirPath + "/" + filename).exists()) {
					try {
						// get方式
						getIconResult = HttpUtil.downloadFileWithGet(downloadUrl, dirPath, filename, "utf-8");
						BaseTools.showlog("getIconResult=" + getIconResult);
						if (getIconResult != null && getIconResult.equals("下载成功")) {
							userInfo.setUsericon(dirPath + "/" + filename);
						} else {
							userInfo.setUsericon("null");
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					userInfo.setUsericon(dirPath + "/" + filename);
				}
			}

			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			if (progressDialog != null)
				progressDialog.dismiss();

			if (result != null && !result.equals("")) {
				if (result.equals("登录失败") || result.equals("sendGetRequest error!")) {
					Util.showToast(UserLoginActivity.this, "登录失败");
				} else {
					AppApplication.mUserInfo = userInfo;
					FileUtil.saveUserInfo(UserLoginActivity.this, userInfo);// 保存到内部文件
					// 发送广播刷新用户信息
					Intent refreshInfoIntent = new Intent("REFRESH_INFO");
					refreshInfoIntent.putExtra("type", "LOGIN");
					sendBroadcast(refreshInfoIntent);
					Toast.makeText(UserLoginActivity.this, "登录成功",Toast.LENGTH_SHORT).show();
					finish();
					overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
				}
			} else {
				Toast.makeText(UserLoginActivity.this, "登录失败",Toast.LENGTH_SHORT).show();
			}
		}
	}

	private UserInfo getUserInfoByJson(String json) {
		UserInfo data = null;
		try {
			Gson g = new Gson();
			data = g.fromJson(json, UserInfo.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}

}

package com.huaxun.menuLeft.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huaxun.R;
import com.huaxun.base.BaseActivity;
import com.huaxun.menuLeft.util.Md5Utils;
import com.huaxun.menuLeft.view.LocusPassWordView;
import com.huaxun.menuLeft.view.LocusPassWordView.OnCompleteListener;
import com.huaxun.tool.BaseTools;
import com.huaxun.utils.ImageUtil;
import com.huaxun.utils.Util;

public class LinePwdActivity  extends BaseActivity implements OnClickListener {

	private TextView title, back;
	private LinearLayout login_ll;
	private TextView login_text;
	private Button reset_pwd;
	private LocusPassWordView mPwdView;
	private SharedPreferences mSharedPreferences;
	public final static String pwdSharePreferenceKey = "PasswordKey";

	private int initState;
	private String firstTimePwd;
	
	private final int OK = 0;
	private final int FirstTime = 1;
	private final int SecondTime =2;
	private final int ResetPwd =3;
	
	private Handler handler = new Handler(); 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {	
		super.onCreate(savedInstanceState);
		Util.setTranslucent(this);
		setContentView(R.layout.line_pwd_layout);	
		initUi();
		mSharedPreferences = getSharedPreferences(pwdSharePreferenceKey, Activity.MODE_PRIVATE);
		String pwd = mSharedPreferences.getString("line_pwd","");
		if (pwd.length() == 0) {
			login_text.setText("请滑动初始化解锁密码");
			initState = FirstTime;
		} else {
			login_text.setText("请滑动解锁");
			initState = OK;
			reset_pwd.setVisibility(View.VISIBLE);
		}
		
		mPwdView.setOnCompleteListener(new OnCompleteListener() {
			@Override
			public void onComplete(String mPassword) {
				BaseTools.showlog("mPassword="+mPassword);
				String encodedPwd = Md5Utils.toMd5(mPassword, "");
				String exsitPwd = mSharedPreferences.getString("line_pwd","");
				switch (initState) {
				    case ResetPwd:
						if (encodedPwd.equals(exsitPwd)) {
							Util.showToast(LinePwdActivity.this, "初始密码正确");
							login_text.setText("请滑动重设解锁密码");
							initState = FirstTime;
						} else {
							mPwdView.markError();
							Util.showToast(LinePwdActivity.this, "初始密码错误");
						}
					    break;
					case FirstTime:
						initState = SecondTime;
						firstTimePwd = mPassword;
						Util.showToast(LinePwdActivity.this, "请再次滑动设置一遍");
						login_text.setText("请再次滑动设置一遍");
						break;
					case SecondTime:
						if (! firstTimePwd.equals(mPassword)) {
							initState = FirstTime;
							Util.showToast(LinePwdActivity.this, "两次滑动密码不一致，请重新设置");
							login_text.setText("请滑动初始化解锁密码");
						} else {
							reset_pwd.setVisibility(View.VISIBLE);
							reset_pwd.setSelected(false);
							initState = OK;
							Util.showToast(LinePwdActivity.this, "设置成功");
							login_text.setText("请滑动解锁");
							SharedPreferences.Editor editor = mSharedPreferences.edit();
							editor.putString("line_pwd", Md5Utils.toMd5(mPassword, ""));
							editor.commit();
						}
						break;
					case OK:
						if (encodedPwd.equals(exsitPwd)) {
							Util.showToast(LinePwdActivity.this, "解锁成功");
							handler.postDelayed(new Runnable(){
								public void run() {
									startActivity(new Intent(LinePwdActivity.this, EncryptActivity.class));
									overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
									finish();
								}					
							}, 1000);	
						} else {
							mPwdView.markError();
							Util.showToast(LinePwdActivity.this, "解锁失败");
							login_text.setText("请滑动解锁");
						}
						break;
					default:
						break;	
				}
			}
		});
	}
	
	private void initUi() {
		title = (TextView)findViewById(R.id.topTitle);
		back = (TextView)findViewById(R.id.topBack);
		login_ll = (LinearLayout)findViewById(R.id.login_ll);
		login_text = (TextView)findViewById(R.id.login_text);
		reset_pwd = (Button)findViewById(R.id.reset_pwd);
		mPwdView = (LocusPassWordView)findViewById(R.id.mPassWordView);
		
		title.setText("九宫格连线解锁");
		login_ll.setBackgroundResource(ImageUtil.getRadomImageResource());
		back.setOnClickListener(this);
		reset_pwd.setOnClickListener(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.topBack:
			finish();
			overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
			break;
		case R.id.reset_pwd:
			if (initState == OK) {
				reset_pwd.setSelected(true);
				initState = ResetPwd;
				login_text.setText("请输入原始密码");
			} else if (initState == ResetPwd) {
				reset_pwd.setSelected(false);
				initState = OK;
				login_text.setText("请滑动解锁");				
			}			
			break;	
		default:
			break;
		}
	}
}

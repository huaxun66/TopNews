package com.huaxun.menuLeft.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huaxun.R;
import com.huaxun.base.BaseActivity;
import com.huaxun.menuLeft.util.Md5Utils;
import com.huaxun.tool.BaseTools;
import com.huaxun.utils.Util;

public class NumericPwdActivity extends BaseActivity implements OnClickListener {

	private TextView title, back;
	private LinearLayout login_ll, input_iv;
	private TextView login_text;
    private ImageView input_iv1, input_iv2, input_iv3, input_iv4;
    private GridView input_gv;
    private GridAdapter gridAdapter;
 	
	private SharedPreferences mSharedPreferences;
	public final static String pwdSharePreferenceKey = "PasswordKey";

	private int initState;
	private String firstTimePwd;
	
	private final int OK = 0;
	private final int FirstTime = 1;
	private final int SecondTime =2;
	private final int ResetPwd =3;
	
	private Handler handler = new Handler(); 
	private String[] numList = {"1","2","3","4","5","6","7","8","9","重置","0",""};
	private StringBuilder nowString = new StringBuilder();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {	
		super.onCreate(savedInstanceState);
		Util.setTranslucent(this);
		setContentView(R.layout.numeric_pwd_layout);	
		initUi();
		gridAdapter = new GridAdapter();
		input_gv.setAdapter(gridAdapter);
		mSharedPreferences = getSharedPreferences(pwdSharePreferenceKey, Activity.MODE_PRIVATE);
		String pwd = mSharedPreferences.getString("numeric_pwd","");
		if (pwd.length() == 0) {
			login_text.setText("请初始化解锁密码");
			initState = FirstTime;
		} else {
			login_text.setText("请输入四位数密码");
			initState = OK;
		}
		
		input_gv.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				switch (position) {
				   case 9:
					   nowString.delete(0, 4);
					   changeDotState();
					   if (initState == OK) {
							initState = ResetPwd;
							login_text.setText("请输入原始密码");
						} else {
							initState = OK;
							login_text.setText("请输入四位数密码");				
						}
					   gridAdapter.notifyDataSetChanged();
					   break;
				   case 11:
					   if (nowString.length()>0) {
						   nowString.deleteCharAt(nowString.length()-1);
						   changeDotState();
					   }
					   break;
				   default:
					   nowString.append(numList[position]);
					   changeDotState();
					   if (nowString.length() == 4) {
						   onComplete(nowString.toString());
					   }
					   break;
				}				
			}			
		});
	}
	
	private void initUi() {
		title = (TextView)findViewById(R.id.topTitle);
		back = (TextView)findViewById(R.id.topBack);
		login_ll = (LinearLayout)findViewById(R.id.login_ll);
		input_iv = (LinearLayout)findViewById(R.id.input_iv);
		login_text = (TextView)findViewById(R.id.login_text);
		input_iv1 = (ImageView)findViewById(R.id.input_iv1);
		input_iv2 = (ImageView)findViewById(R.id.input_iv2);
		input_iv3 = (ImageView)findViewById(R.id.input_iv3);
		input_iv4 = (ImageView)findViewById(R.id.input_iv4);
		input_gv = (GridView)findViewById(R.id.input_gv);
		
		title.setText("九宫格数字解锁");
//		login_ll.setBackgroundResource(ImageUtil.getRadomImageResource());
		back.setOnClickListener(this);
	}
	
	private void changeDotState() {
		switch (nowString.length()) {
		case 0:
			input_iv1.setImageResource(R.drawable.password_normal);
			input_iv2.setImageResource(R.drawable.password_normal);
			input_iv3.setImageResource(R.drawable.password_normal);
			input_iv4.setImageResource(R.drawable.password_normal);
			break;
		case 1:
			input_iv1.setImageResource(R.drawable.password_check);
			input_iv2.setImageResource(R.drawable.password_normal);
			input_iv3.setImageResource(R.drawable.password_normal);
			input_iv4.setImageResource(R.drawable.password_normal);
			break;
		case 2:
			input_iv1.setImageResource(R.drawable.password_check);
			input_iv2.setImageResource(R.drawable.password_check);
			input_iv3.setImageResource(R.drawable.password_normal);
			input_iv4.setImageResource(R.drawable.password_normal);
			break;
		case 3:
			input_iv1.setImageResource(R.drawable.password_check);
			input_iv2.setImageResource(R.drawable.password_check);
			input_iv3.setImageResource(R.drawable.password_check);
			input_iv4.setImageResource(R.drawable.password_normal);
			break;
		case 4:
			input_iv1.setImageResource(R.drawable.password_check);
			input_iv2.setImageResource(R.drawable.password_check);
			input_iv3.setImageResource(R.drawable.password_check);
			input_iv4.setImageResource(R.drawable.password_check);
			break;
		default:
			break;
		}
	}
	
	public void onComplete(String mPassword) {
		BaseTools.showlog("mPassword="+mPassword);
		setAnimation();
		
		String encodedPwd = Md5Utils.toMd5(mPassword, "");
		String exsitPwd = mSharedPreferences.getString("numeric_pwd","");
		switch (initState) {
		    case ResetPwd:
				if (encodedPwd.equals(exsitPwd)) {
					Util.showToast(this, "初始密码正确");
					login_text.setText("请重设解锁密码");
					initState = FirstTime;
				} else {
					Util.showToast(this, "初始密码错误");
				}
			    break;
			case FirstTime:
				initState = SecondTime;
				firstTimePwd = mPassword;
				Util.showToast(this, "请再次输入设置一遍");
				login_text.setText("请再次输入设置一遍");
				break;
			case SecondTime:
				if (! firstTimePwd.equals(mPassword)) {
					initState = FirstTime;
					Util.showToast(this, "两次输入密码不一致，请重新设置");
					login_text.setText("请重设解锁密码");
				} else {
					initState = OK;
					Util.showToast(this, "设置成功");
					login_text.setText("请输入四位数密码");
					SharedPreferences.Editor editor = mSharedPreferences.edit();
					editor.putString("numeric_pwd", Md5Utils.toMd5(mPassword, ""));
					editor.commit();
					gridAdapter.notifyDataSetChanged();
				}
				break;
			case OK:
				if (encodedPwd.equals(exsitPwd)) {
					Util.showToast(this, "解锁成功");
					handler.postDelayed(new Runnable(){
						public void run() {
							startActivity(new Intent(NumericPwdActivity.this, EncryptActivity.class));
							overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
							finish();
						}					
					}, 1000);	
				} else {
					Util.showToast(this, "解锁失败");					
					login_text.setText("请输入四位数密码");
				}
				break;
			default:
				break;	
		}
	}
	
	private void setAnimation() {
		TranslateAnimation translateAnimation = new TranslateAnimation(Animation.ABSOLUTE, 20, Animation.ABSOLUTE, 0);
		translateAnimation.setDuration(10);
		translateAnimation.setRepeatMode(Animation.REVERSE);
		translateAnimation.setRepeatCount(5);
		translateAnimation.setAnimationListener(new AnimationListener(){
			@Override
			public void onAnimationEnd(Animation animation) {
				nowString.delete(0, 4);
				changeDotState();
			}
			@Override
			public void onAnimationRepeat(Animation animation) {}
			@Override
			public void onAnimationStart(Animation animation) {}			
		});
		input_iv.startAnimation(translateAnimation);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.topBack:
			finish();
			overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
			break;
		default:
			break;
		}
	}
	
	public class GridAdapter extends BaseAdapter {
		private TextView text_item;
		private ImageView image_item;
		
		@Override
		public int getCount() {
			return 12;
		}
		
		@Override
		public String getItem(int position) {
			return numList[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressLint("NewApi")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = LayoutInflater.from(NumericPwdActivity.this).inflate(R.layout.pwd_item, null);
			text_item = (TextView) view.findViewById(R.id.text_item);
			image_item = (ImageView) view.findViewById(R.id.image_item);
			text_item.setText(getItem(position));
			if (position == 9) {
				String pwd = mSharedPreferences.getString("numeric_pwd","");
				if (pwd.length() == 0) {
					text_item.setVisibility(View.INVISIBLE);
				} else {
					text_item.setTextSize(20);
					if (initState != OK) {
						view.setBackgroundResource(R.drawable.subscribe_item_bg);
					} else {
						view.setBackground(null);
					}
				}				
			} else if (position == 11) {
				text_item.setVisibility(View.INVISIBLE);
				image_item.setVisibility(view.VISIBLE);
			}
			return view;
		}
	}
	
}

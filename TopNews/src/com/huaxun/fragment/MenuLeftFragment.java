package com.huaxun.fragment;

import java.io.File;
import java.io.InputStream;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huaxun.MainActivity;
import com.huaxun.R;
import com.huaxun.app.AppApplication;
import com.huaxun.menuLeft.activity.FavoriteNewsActivity;
import com.huaxun.menuLeft.activity.FeedbackActivity;
import com.huaxun.menuLeft.activity.IsvPwdActivity;
import com.huaxun.menuLeft.activity.LinePwdActivity;
import com.huaxun.menuLeft.activity.NumericPwdActivity;
import com.huaxun.menuLeft.activity.PictureActivity;
import com.huaxun.menuLeft.activity.PictureWallActivity;
import com.huaxun.menuLeft.activity.SettingsActivity;
import com.huaxun.menuLeft.view.SpringbackScrollView;
import com.huaxun.menuLeft.view.SpringbackScrollView.OnHeaderRefreshListener;
import com.huaxun.tool.Options;
import com.huaxun.tool.Settings;
import com.huaxun.utils.ImageUtil;
import com.huaxun.view.CircleImageView;
import com.huaxun.view.RotateAnimation;
import com.huaxun.view.RotateAnimation.InterpolatedTimeListener;
import com.huaxun.view.SwitchButton;
import com.huaxun.weather.WeatherActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

@SuppressLint("ValidFragment") 
public class MenuLeftFragment extends Fragment implements OnClickListener, InterpolatedTimeListener {
	private View mView;
	private MainActivity mainAct;
	private ImageView mBackgroundImageView;
	private SpringbackScrollView mScrollView;
	private CircleImageView user_icon;
	private TextView user_name, user_location;
	private SwitchButton night_mode_btn;
	private TextView night_mode_text;	
	private LinearLayout weather_layout;
	private TextView weather_city;
	private TextView weather_temp;
	private TextView weather_des;
	private ImageView weather_icon_drawable;
	
	private RelativeLayout picture_btn;
	private RelativeLayout picturewall_btn;
	private RelativeLayout favorite_btn;
	private RelativeLayout encrypt_btn;
	private RelativeLayout feedback_btn;
	private RelativeLayout setting_btn;
	
	private DisplayImageOptions options;
	private Settings settings;
	
	private boolean afterRotate = false;
	
	public MenuLeftFragment(MainActivity mainAct){
		this.mainAct = mainAct;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (mView == null){
			initView(inflater, container);
		}
		options = Options.getListOptions();
		settings = new Settings(mainAct, false);
		updateWeatherDisplay();
		
		mUpdateWeatherReceiver = new UpdateWeatherReceiver();
		IntentFilter mUpdateFilter = new IntentFilter("update_weather");
		mainAct.registerReceiver(mUpdateWeatherReceiver, mUpdateFilter);
		
		// 网络改变的广播
		mRefreshUserInfoReceiver = new RefreshUserInfoReceiver();
		IntentFilter mFilter = new IntentFilter("REFRESH_INFO");
		mainAct.registerReceiver(mRefreshUserInfoReceiver, mFilter);
		return mView;
	}

	private void initView(LayoutInflater inflater, ViewGroup container)
	{
		mView = inflater.inflate(R.layout.left_drawer_fragment, container, false);
		mBackgroundImageView = (ImageView) mView.findViewById(R.id.personal_background_image);
		mScrollView = (SpringbackScrollView) mView.findViewById(R.id.personal_scrollView);
		user_icon = (CircleImageView)mView.findViewById(R.id.user_icon);
		user_name = (TextView)mView.findViewById(R.id.user_name);
		user_location = (TextView)mView.findViewById(R.id.user_location);
		night_mode_btn = (SwitchButton)mView.findViewById(R.id.night_mode_btn);		
		night_mode_text = (TextView)mView.findViewById(R.id.night_mode_text);
		weather_layout = (LinearLayout)mView.findViewById(R.id.weather_layout);		
		weather_city = (TextView)mView.findViewById(R.id.weather_city);
		weather_temp = (TextView)mView.findViewById(R.id.weather_temp);		
		weather_des = (TextView)mView.findViewById(R.id.weather_des);
		weather_icon_drawable = (ImageView) mView.findViewById(R.id.weather_icon_drawable);
		
		picture_btn = (RelativeLayout)mView.findViewById(R.id.picture_btn);
		picturewall_btn = (RelativeLayout)mView.findViewById(R.id.picturewall_btn);
		favorite_btn = (RelativeLayout)mView.findViewById(R.id.favorite_btn);
		encrypt_btn = (RelativeLayout)mView.findViewById(R.id.encrypt_btn);
		feedback_btn = (RelativeLayout)mView.findViewById(R.id.feedback_btn);
		setting_btn = (RelativeLayout)mView.findViewById(R.id.setting_btn);		
		
		user_icon.setOnClickListener(this);
		weather_layout.setOnClickListener(this);
		picture_btn.setOnClickListener(this);		
		picturewall_btn.setOnClickListener(this);		
		favorite_btn.setOnClickListener(this);
		encrypt_btn.setOnClickListener(this);
		feedback_btn.setOnClickListener(this);
		setting_btn.setOnClickListener(this);	
		
		mScrollView.setImageView(mBackgroundImageView);
		mScrollView.setOnHeaderRefreshListener(new OnHeaderRefreshListener(){
			@Override
			public void onHeaderRefresh(SpringbackScrollView view) {
				openRotateAnimation(user_icon);
			}			
		});
		
		night_mode_btn.setOnCheckedChangeListener(new OnCheckedChangeListener() {		
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					night_mode_text.setText(getResources().getString(R.string.action_night_mode));					
				}else{
					night_mode_text.setText(getResources().getString(R.string.action_day_mode));					
				}
				mainAct.switchNightMode(isChecked);
			}
		});
				
		if (mainAct.brightnessmode.equals("1")) {
			night_mode_btn.setChecked(false);
			night_mode_text.setText(getResources().getString(R.string.action_day_mode));
		} else {
			night_mode_btn.setChecked(true);
			night_mode_text.setText(getResources().getString(R.string.action_night_mode));
		}
	}
	
	public void onDestroy() {
		super.onDestroy();
		mainAct.unregisterReceiver(mUpdateWeatherReceiver);
		mainAct.unregisterReceiver(mRefreshUserInfoReceiver);
	}
	
	//更新天气的广播
	private UpdateWeatherReceiver mUpdateWeatherReceiver;
	class UpdateWeatherReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			updateWeatherDisplay();
		}
	}
	
	/**
	 * 刷新
	 */
	private RefreshUserInfoReceiver mRefreshUserInfoReceiver;

	class RefreshUserInfoReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			setUserIcon();
			String type = intent.getStringExtra("type");
			if (!TextUtils.isEmpty(type) && type.equals("LOGIN")) {
				user_name.setText(AppApplication.mUserInfo.getUsername() + "  | " + AppApplication.mUserInfo.getGender());
				user_location.setText(AppApplication.mUserInfo.getLocation());
			} else {
				user_name.setText("NickName | gender");
				user_location.setText("Location");
			}
		}
	}
	
	private void setUserIcon() {
		if (AppApplication.mUserInfo != null) {
			String usericon = AppApplication.mUserInfo.getUsericon();
			if (AppApplication.mUserInfo.getIsThirdLogin() == true) {
				ImageLoader.getInstance().displayImage(usericon, user_icon, options);					
			} else {
				if (new File(usericon).exists()) {
					Bitmap bmp = ImageUtil.getBitmapFromSDcard(usericon);
					user_icon.setImageBitmap(bmp);
				}
			}
		} else {
			user_icon.setImageResource(R.drawable.default_round_head);
		}
	}
	
	private void setUserIconAfter() {
		if (AppApplication.mUserInfo != null && AppApplication.mUserInfo.getIsThirdLogin() == true) {
			String usericon2 = AppApplication.mUserInfo.getUsericon2();
			ImageLoader.getInstance().displayImage(usericon2, user_icon, options);					
		} else {
			user_icon.setImageResource(R.drawable.default_artist_hole);
		}
	}

	
	private void updateWeatherDisplay() {
		SharedPreferences pref = getActivity().getSharedPreferences("weatherinfo", Context.MODE_PRIVATE);
		try {
			weather_city.setText(pref.getString("city","")); 
			weather_temp.setText(pref.getString("temp1",""));
			weather_des.setText(pref.getString("weather1",""));
			if (!pref.getString("img1","").isEmpty()){
				InputStream imageFile = mainAct.getAssets().open("weather/" + pref.getString("img1","") + ".png");
				weather_icon_drawable.setImageBitmap(BitmapFactory.decodeStream(imageFile));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {		
		case R.id.user_icon:
			openRotateAnimation(v);
			break;
		case R.id.weather_layout:
			startActivity(new Intent(getActivity(),WeatherActivity.class));
			getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			break;
		case R.id.picture_btn:
			startActivity(new Intent(getActivity(),PictureActivity.class));
			getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			break;
		case R.id.picturewall_btn:
			startActivity(new Intent(getActivity(),PictureWallActivity.class));
			getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			break;
		case R.id.favorite_btn:
			startActivity(new Intent(getActivity(),FavoriteNewsActivity.class));
			getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			break;
		case R.id.feedback_btn:
			startActivity(new Intent(getActivity(),FeedbackActivity.class));
			getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			break;
		case R.id.encrypt_btn:
			if (settings.getLockMode().equals(Settings.ISV_MODE)) {
				startActivity(new Intent(getActivity(),IsvPwdActivity.class));
			} else if (settings.getLockMode().equals(Settings.PASSWORD_MODE)) {
				startActivity(new Intent(getActivity(),NumericPwdActivity.class));
			} else {
				startActivity(new Intent(getActivity(),LinePwdActivity.class));
			}			
			getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			break;
		case R.id.setting_btn:
			startActivity(new Intent(getActivity(),SettingsActivity.class));
			getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			break;
		default:
			break;
		}
	}
	
	public void openRotateAnimation(View v) {
		RotateAnimation animation = new RotateAnimation();
		animation.setFillAfter(true);
		animation.setInterpolatedTimeListener(this);
		v.startAnimation(animation);		
		afterRotate = !afterRotate;
	}

	@Override
	public void interpolatedTime(float interpolatedTime) {
		// 监听到翻转进度过半时，更新图片内容，
		if (interpolatedTime > 0.5f) {
			if (afterRotate) {
				setUserIconAfter();
			} else {
				setUserIcon();
			}
		}
	}
	
	
}

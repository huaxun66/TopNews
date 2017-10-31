package com.huaxun.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationUtils;

import com.huaxun.R;
import com.huaxun.app.AppApplication;
import com.huaxun.menuLeft.Images;
import com.huaxun.menuRight.ImageCycleView;
import com.huaxun.menuRight.ImageCycleView.ImageCycleViewListener;
import com.huaxun.menuRight.activity.AuthActivity;
import com.huaxun.menuRight.activity.UserLoginActivity;
import com.huaxun.tool.BaseTools;
import com.huaxun.tool.Constants;
import com.huaxun.tool.Options;
import com.huaxun.utils.ImageUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class MenuRightFragment extends Fragment implements OnClickListener {
	private Context context;
	private View mView;
	private ImageView login_IV;
	private TextView login_TV, auth_TV, logout_TV, version;
	public ImageCycleView mImageCycleView;
	private DisplayImageOptions options;
    private final int UpdateImageCycleView = 0;
    private int pageIndex = -1; //imageCycleView到第几页
    private List<String> showList = new ArrayList<String>(); //imageCycleView当前显示的图片Url列表
    private AlphaAnimation mAlphaAnimation;
    
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
        	case UpdateImageCycleView:
        		showList.clear();
    			pageIndex++;
    			for (int i = pageIndex*5; i < pageIndex*5 + 5 && i < Images.sceneryImageUrls.length; i++) {
    				showList.add(Images.sceneryImageUrls[i]);
    				if (i == Images.sceneryImageUrls.length-1) {
    					pageIndex = -1;
    				}
    			}		
    			
    			mImageCycleView.setImageResources(showList, new ImageCycleViewListener() {
    				public void displayImage(final String imageURL, final ImageView imageView) {				
    					ImageLoader.getInstance().displayImage(imageURL, imageView, options);
    				}
    				@Override
    				public void onImageClick(int position, View view) {}
    			});
    			((View) mImageCycleView).startAnimation(mAlphaAnimation);
    			handler.sendEmptyMessageDelayed(UpdateImageCycleView, 30000);	
    			break;
    		default:
    			break;
    		};   		
		}
	};
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.context = this.getActivity();
		// 透明动画
		mAlphaAnimation = new AlphaAnimation(0.4f, 1.0f);
		mAlphaAnimation.setDuration(1000);
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (mView == null){
			initView(inflater, container);
		}
		options = Options.getListOptions();
		return mView;	
	}
	
	private void initView(LayoutInflater inflater, ViewGroup container) {
		mView = inflater.inflate(R.layout.profile_drawer_right, container, false);
		login_IV = (ImageView)mView.findViewById(R.id.login_IV);
		login_TV = (TextView)mView.findViewById(R.id.login_TV);
		auth_TV = (TextView)mView.findViewById(R.id.auth_TV);
		logout_TV = (TextView)mView.findViewById(R.id.logout_TV);
		mImageCycleView = (ImageCycleView) mView.findViewById(R.id.icv);
		version = (TextView)mView.findViewById(R.id.version);
		login_IV.setOnClickListener(this);
		login_TV.setOnClickListener(this);
		auth_TV.setOnClickListener(this);
		logout_TV.setOnClickListener(this);
		
		version.setText("当前版本：" + AppApplication.VERSION_NAME);
		// 网络改变的广播
		mRefreshUserInfoReceiver = new RefreshUserInfoReceiver();
		IntentFilter mFilter = new IntentFilter("REFRESH_INFO");
		context.registerReceiver(mRefreshUserInfoReceiver, mFilter);
		
		handler.sendEmptyMessage(UpdateImageCycleView);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		context.unregisterReceiver(mRefreshUserInfoReceiver);
		handler.removeMessages(UpdateImageCycleView);
	}
	
	/**
	 * 刷新
	 */
	private RefreshUserInfoReceiver mRefreshUserInfoReceiver;

	class RefreshUserInfoReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String type = intent.getStringExtra("type");
			if (!TextUtils.isEmpty(type) && type.equals("LOGIN")) {
				String usericon = AppApplication.mUserInfo.getUsericon();
				if (AppApplication.mUserInfo.getIsThirdLogin() == true) {
					ImageLoader.getInstance().displayImage(usericon, login_IV, options);
				} else {
					if (new File(usericon).exists()) {
						BaseTools.showlog("iconPath="+usericon);
						Bitmap bmp = ImageUtil.getBitmapFromSDcard(usericon);
						login_IV.setImageBitmap(bmp);
					}
				}
				login_TV.setText(AppApplication.mUserInfo.getUsername());
				logout_TV.setVisibility(View.VISIBLE);
			}
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.login_IV:
		case R.id.login_TV:
			if (AppApplication.mUserInfo == null) { 
				Intent intent = new Intent(context, UserLoginActivity.class);
				startActivity(intent);
				((Activity)context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}			
			break;
		case R.id.auth_TV:
			Intent it = new Intent(context, AuthActivity.class);
			startActivity(it);
			((Activity)context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);			
			break;
		case R.id.logout_TV:
			AppApplication.mUserInfo = null;
			context.deleteFile(Constants.FILE_NMAE_USERINFO);
			login_IV.setImageResource(R.drawable.default_round_head);
			login_TV.setText("立即登录");	
			logout_TV.setVisibility(View.GONE);
			 // 发送广播刷新用户信息
			 Intent refreshInfoIntent = new Intent("REFRESH_INFO");
			 refreshInfoIntent.putExtra("type", "LOGOUT");
			 context.sendBroadcast(refreshInfoIntent);			 
			break;
		default:
			break;
		}		
	}
	
}

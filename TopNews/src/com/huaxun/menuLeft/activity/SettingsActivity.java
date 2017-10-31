package com.huaxun.menuLeft.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.jpush.android.api.JPushInterface;

import com.huaxun.R;
import com.huaxun.base.BaseActivity;
import com.huaxun.tool.Settings;
import com.huaxun.utils.FileUtil;
import com.huaxun.utils.Util;

public class SettingsActivity extends BaseActivity implements OnClickListener{
	private TextView title, back;
	private TextView font_size, picturewall_mode, lock_mode, aotudownload_news, download_media_mode,
	                 cache_size;
	private LinearLayout setting_font_size, setting_picturewall_mode, setting_lock_mode, setting_aotudownload_news, 
	                     setting_download_media_mode, setting_clear, tts_setting, iat_setting, isv_setting; 
	private CheckBox download_only_wifi_checkbox, enable_jpush_checkbox;
	private Settings settings;
	
	public final int FONT_SIZE = 0;
	public final int PICTURE_WALL_MODE = 1;
	public final int LOCK_MODE = 2;
	public final int AUTODOWNLOAD_NEWS_MODE = 3;
	public final int DOWNLOAD_MEDIA_MODE = 4;
	public final int CLEAR_CACHE = 5;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Util.setTranslucent(this);
		settings = new Settings(this, false);
		setContentView(R.layout.settings);
		initView();
		initData();
	}
	
	private void initView() {
		title = (TextView)findViewById(R.id.topTitle);
		back = (TextView)findViewById(R.id.topBack);
		font_size = (TextView)findViewById(R.id.font_size);
		picturewall_mode = (TextView)findViewById(R.id.picturewall_mode);
		lock_mode = (TextView)findViewById(R.id.lock_mode);
		aotudownload_news = (TextView)findViewById(R.id.aotudownload_news);
		download_media_mode = (TextView)findViewById(R.id.download_media_mode);
		cache_size = (TextView)findViewById(R.id.cache_size);
		
		setting_font_size = (LinearLayout)findViewById(R.id.setting_font_size);
		setting_picturewall_mode = (LinearLayout)findViewById(R.id.setting_picturewall_mode);
		setting_lock_mode = (LinearLayout)findViewById(R.id.setting_lock_mode);
		setting_aotudownload_news = (LinearLayout)findViewById(R.id.setting_aotudownload_news);
		setting_download_media_mode = (LinearLayout)findViewById(R.id.setting_download_media_mode);
		setting_clear = (LinearLayout)findViewById(R.id.setting_clear);
		download_only_wifi_checkbox = (CheckBox)findViewById(R.id.download_only_wifi_checkbox);
		enable_jpush_checkbox = (CheckBox)findViewById(R.id.enable_jpush_checkbox);		
		tts_setting = (LinearLayout)findViewById(R.id.tts_setting);
		iat_setting = (LinearLayout)findViewById(R.id.iat_setting);
		isv_setting = (LinearLayout)findViewById(R.id.isv_setting);

		back.setOnClickListener(this);
		setting_font_size.setOnClickListener(this);		
		setting_picturewall_mode.setOnClickListener(this);
		setting_lock_mode.setOnClickListener(this);
		setting_aotudownload_news.setOnClickListener(this);
		setting_download_media_mode.setOnClickListener(this);
		setting_clear.setOnClickListener(this);
		tts_setting.setOnClickListener(this);
		iat_setting.setOnClickListener(this);
		isv_setting.setOnClickListener(this);
		
		download_only_wifi_checkbox.setOnCheckedChangeListener(myCheckedChangeListener);
		enable_jpush_checkbox.setOnCheckedChangeListener(myCheckedChangeListener);
	}
	
	private void initData() {
		title.setText("设置");
		//字体大小
		if (settings.getFontSize().equals(Settings.FONT_SIZE_LARGE)) {
			font_size.setText("大");
		} else if (settings.getFontSize().equals(Settings.FONT_SIZE_NORMAL)) {
			font_size.setText("中");
		} else {
			font_size.setText("小");
		}
		//照片墙模式
		if (settings.getPictureWallMode().equals(Settings.PICTURE_WALL_NETWORK)) {
			picturewall_mode.setText("网络");
		} else {
			picturewall_mode.setText("本地");
		}
		//解锁模式
		if (settings.getLockMode().equals(Settings.ISV_MODE)) {
			lock_mode.setText("声纹解锁");
		} else if (settings.getLockMode().equals(Settings.PASSWORD_MODE)) {
			lock_mode.setText("九宫格数字解锁");
		} else {
			lock_mode.setText("九宫格连线解锁");
		}
		//新闻自动下载模式
		if (settings.getAutodownloadNewsMode().equals(Settings.AUTODOWNLOAD_NOT_CONTAIN_MEDIA)) {
			aotudownload_news.setText("不包含视频");
		} else {
			aotudownload_news.setText("包含视频");
		}
		//视频下载模式
		if (settings.getDownloadMediaMode().equals(Settings.DOWNLOAD_MEDIA_NOTIFICATION)) {
			download_media_mode.setText("通知栏");
		} else {
			download_media_mode.setText("断点续传");
		}
		//缓存大小
		cache_size.setText(FileUtil.getFileOrFilesSize(FileUtil.getCachePath()));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.topBack:
			finish();
			overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
			break;
		case R.id.setting_font_size:
			showSelectDialog(3, FONT_SIZE);
			break;
		case R.id.setting_picturewall_mode:
			showSelectDialog(2, PICTURE_WALL_MODE);
			break;
		case R.id.setting_lock_mode:
			showSelectDialog(3, LOCK_MODE);
			break;
		case R.id.setting_aotudownload_news:
			showSelectDialog(2, AUTODOWNLOAD_NEWS_MODE);
			break;
		case R.id.setting_download_media_mode:
			showSelectDialog(2, DOWNLOAD_MEDIA_MODE);
			break;
		case R.id.setting_clear:
            Boolean flag = FileUtil.deleteDirectory(FileUtil.getCachePath(), false);
            cache_size.setText(FileUtil.getFileOrFilesSize(FileUtil.getCachePath()));
            if (flag == true) {
            	Util.showToast(this, "清除缓存成功");
            } else {
            	Util.showToast(this, "清除缓存失败");
            }
			break;
		case R.id.tts_setting:
			Intent intent1 = new Intent(this, TtsSettings.class);
			startActivity(intent1);
			break;
		case R.id.iat_setting:
			Intent intent2 = new Intent(this, IatSettings.class);
			startActivity(intent2);
			break;
		case R.id.isv_setting:
			Intent intent3 = new Intent(this, IsvSettings.class);
			startActivity(intent3);
			break;
		default:
			break;
		}
	}
	
	private MyCheckedChangeListener myCheckedChangeListener;
	class MyCheckedChangeListener implements OnCheckedChangeListener {		
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (buttonView == download_only_wifi_checkbox) {
				settings.setDownloadOnlyWifi(isChecked);
			} else if (buttonView == enable_jpush_checkbox) {
				if (isChecked) {
					JPushInterface.resumePush(SettingsActivity.this);
				} else {
					JPushInterface.stopPush(SettingsActivity.this);
				}
			}
		}
	}
	
	private void showSelectDialog(final int lines, final int flag){
		final Dialog dialog = new Dialog(this, R.style.WhiteDialog);
		if (lines == 3) {
			dialog.setContentView(R.layout.select_dialog_three_line);
			final TextView tvOne = (TextView) dialog.findViewById(R.id.dialog_tv_one);
			final TextView tvTwo = (TextView) dialog.findViewById(R.id.dialog_tv_two);
			final TextView tvThree = (TextView) dialog.findViewById(R.id.dialog_tv_three);
			final ImageView ivOne = (ImageView) dialog.findViewById(R.id.dialog_iv_one);
			final ImageView ivTwo = (ImageView) dialog.findViewById(R.id.dialog_iv_two);
			final ImageView ivThree = (ImageView) dialog.findViewById(R.id.dialog_iv_three);
			if (flag == FONT_SIZE) {
				tvOne.setText("大");
				tvTwo.setText("中");
				tvThree.setText("小");
				if (settings.getFontSize().equals(Settings.FONT_SIZE_LARGE)) {
					ivOne.setVisibility(View.VISIBLE);
					ivTwo.setVisibility(View.GONE);
					ivThree.setVisibility(View.GONE);
				} else if (settings.getFontSize().equals(Settings.FONT_SIZE_NORMAL)) {
					ivOne.setVisibility(View.GONE);
					ivTwo.setVisibility(View.VISIBLE);
					ivThree.setVisibility(View.GONE);
				} else if (settings.getFontSize().equals(Settings.FONT_SIZE_SMALL)) {
					ivOne.setVisibility(View.GONE);
					ivTwo.setVisibility(View.GONE);
					ivThree.setVisibility(View.VISIBLE);
				}
			} else if (flag == LOCK_MODE) {
				tvOne.setText("声纹解锁");
				tvTwo.setText("九宫格密码解锁");
				tvThree.setText("九宫格连线解锁");
				if (settings.getLockMode().equals(Settings.ISV_MODE)) {
					ivOne.setVisibility(View.VISIBLE);
					ivTwo.setVisibility(View.GONE);
					ivThree.setVisibility(View.GONE);
				} else if (settings.getLockMode().equals(Settings.PASSWORD_MODE)) {
					ivOne.setVisibility(View.GONE);
					ivTwo.setVisibility(View.VISIBLE);
					ivThree.setVisibility(View.GONE);
				} else if (settings.getLockMode().equals(Settings.LINE_MODE)) {
					ivOne.setVisibility(View.GONE);
					ivTwo.setVisibility(View.GONE);
					ivThree.setVisibility(View.VISIBLE);
				}
			}
			
			dialog.findViewById(R.id.rl_one).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ivOne.setVisibility(View.VISIBLE);
					ivTwo.setVisibility(View.GONE);
					ivThree.setVisibility(View.GONE);
					if (flag == FONT_SIZE) {
						settings.setFontSize(Settings.FONT_SIZE_LARGE);
						font_size.setText("大");
					} else if (flag == LOCK_MODE) {
						settings.setLockMode(Settings.ISV_MODE);
						lock_mode.setText("声纹解锁");
					}
					dialog.dismiss();
				}
			});
			dialog.findViewById(R.id.rl_two).setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					ivOne.setVisibility(View.GONE);
					ivTwo.setVisibility(View.VISIBLE);
					ivThree.setVisibility(View.GONE);
					if (flag == FONT_SIZE) {
						settings.setFontSize(Settings.FONT_SIZE_NORMAL);
						font_size.setText("中");
					} else if (flag == LOCK_MODE) {
						settings.setLockMode(Settings.PASSWORD_MODE);
						lock_mode.setText("九宫格密码解所");
					}				
					dialog.dismiss();
				}
			});
			dialog.findViewById(R.id.rl_three).setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					ivOne.setVisibility(View.GONE);
					ivTwo.setVisibility(View.GONE);
					ivThree.setVisibility(View.VISIBLE);
					if (flag == FONT_SIZE) {
						settings.setFontSize(Settings.FONT_SIZE_SMALL);
						font_size.setText("小");
					} else if (flag == LOCK_MODE) {
						settings.setLockMode(Settings.LINE_MODE);
						lock_mode.setText("九宫格连线解所");
					}				
					dialog.dismiss();
				}
			});
		} else if (lines == 2) {
			dialog.setContentView(R.layout.select_dialog_two_line);
			final TextView tvOne = (TextView) dialog.findViewById(R.id.dialog_tv_one);
			final TextView tvTwo = (TextView) dialog.findViewById(R.id.dialog_tv_two);
			final ImageView ivOne = (ImageView) dialog.findViewById(R.id.dialog_iv_one);
			final ImageView ivTwo = (ImageView) dialog.findViewById(R.id.dialog_iv_two);			
			if (flag == PICTURE_WALL_MODE) {
				tvOne.setText("网络");
				tvTwo.setText("本地");
				if (settings.getPictureWallMode().equals(Settings.PICTURE_WALL_NETWORK)) {
					ivOne.setVisibility(View.VISIBLE);
					ivTwo.setVisibility(View.GONE);
				} else {
					ivOne.setVisibility(View.GONE);
					ivTwo.setVisibility(View.VISIBLE);
				}
			} else if (flag == AUTODOWNLOAD_NEWS_MODE) {
				tvOne.setText("不包含视频");
				tvTwo.setText("包含视频");
				if (settings.getAutodownloadNewsMode().equals(Settings.AUTODOWNLOAD_NOT_CONTAIN_MEDIA)) {
					ivOne.setVisibility(View.VISIBLE);
					ivTwo.setVisibility(View.GONE);
				} else {
					ivOne.setVisibility(View.GONE);
					ivTwo.setVisibility(View.VISIBLE);
				}
			} else if (flag == DOWNLOAD_MEDIA_MODE) {
				tvOne.setText("通知栏");
				tvTwo.setText("断点续传");
				if (settings.getDownloadMediaMode().equals(Settings.DOWNLOAD_MEDIA_NOTIFICATION)) {
					ivOne.setVisibility(View.VISIBLE);
					ivTwo.setVisibility(View.GONE);
				} else {
					ivOne.setVisibility(View.GONE);
					ivTwo.setVisibility(View.VISIBLE);
				}
			}
			dialog.findViewById(R.id.rl_one).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ivOne.setVisibility(View.VISIBLE);
					ivTwo.setVisibility(View.GONE);
					if (flag == PICTURE_WALL_MODE) {
						settings.setPictureWallMode(Settings.PICTURE_WALL_NETWORK);
						picturewall_mode.setText("网络");
					} else if (flag == AUTODOWNLOAD_NEWS_MODE) {
						settings.setAutodownloadNewsMode(Settings.AUTODOWNLOAD_NOT_CONTAIN_MEDIA);
						aotudownload_news.setText("不包含视频");
					} else if (flag == DOWNLOAD_MEDIA_MODE) {
						settings.setDownloadMediaMode(Settings.DOWNLOAD_MEDIA_NOTIFICATION);
						download_media_mode.setText("通知栏");
					}
					dialog.dismiss();
				}
			});
			dialog.findViewById(R.id.rl_two).setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					ivOne.setVisibility(View.GONE);
					ivTwo.setVisibility(View.VISIBLE);
					if (flag == PICTURE_WALL_MODE) {
						settings.setPictureWallMode(Settings.PICTURE_WALL_LOCAL);
						picturewall_mode.setText("本地");
					} else if (flag == AUTODOWNLOAD_NEWS_MODE) {
						settings.setAutodownloadNewsMode(Settings.AUTODOWNLOAD_CONTAIN_MEDIA);
						aotudownload_news.setText("包含视频");
					} else if (flag == DOWNLOAD_MEDIA_MODE) {
						settings.setDownloadMediaMode(Settings.DOWNLOAD_MEDIA_RESUME);
						download_media_mode.setText("断点续传");
					}
					dialog.dismiss();
				}
			});		
		}
		dialog.show();
	}
}

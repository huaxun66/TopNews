package com.huaxun.music.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.huaxun.R;
import com.huaxun.tool.Settings;
import com.huaxun.utils.ImageUtil;
import com.huaxun.utils.Util;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class SkinSettingActivity extends Activity {
	private GridView gv_skin;			//网格视图
	private ImageView top_back;
	private ImageAdapter adapter;		//图片适配器
	private Settings mSetting;			//设置引用
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Util.setDrawableTranslucent(this, R.drawable.bg_media_library_topbar);
		
		setContentView(R.layout.skinsetting_layout);
		top_back = (ImageView) findViewById(R.id.ibtn_player_back_return); 
		top_back.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(SkinSettingActivity.this, MusicDetailActivity.class);
				setResult(MusicDetailActivity.SKINRESULT, intent);
				finish();
		        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
			}		
		});
		
		mSetting = new Settings(this, true);
		adapter = new ImageAdapter(mSetting.getCurrentSkinId());
		gv_skin = (GridView) findViewById(R.id.gv_skin);
		gv_skin.setAdapter(adapter);
		gv_skin.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//更新GridView
				adapter.setCurrentId(position);
				//更新背景图片
				SkinSettingActivity.this.getWindow().setBackgroundDrawableResource(Settings.SKIN_RESOURCES[position]);
				//保存数据
				mSetting.setCurrentSkinResId(position);
			}
		});
	}
	
	public void onBackPressed() {
		Intent intent = new Intent(SkinSettingActivity.this, MusicDetailActivity.class);
		setResult(MusicDetailActivity.SKINRESULT, intent);
		finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        super.onBackPressed();
	}
	
	public class ImageAdapter extends BaseAdapter{	
		private int currentId = -1;
		private int wh = 0;
				
		/**
		 * 皮肤预览资源的ID数组
		 */
		private final int[] SKIN_RESOURCES = { R.drawable.preview_bg01,
			R.drawable.preview_bg02, R.drawable.preview_bg03, R.drawable.preview_bg04,
			R.drawable.preview_bg05, R.drawable.preview_bg06 };		
		
		public ImageAdapter( int currentId) {
			this.currentId = currentId;
			WindowManager windowManager = (WindowManager) SkinSettingActivity.this.getSystemService(Context.WINDOW_SERVICE);
			Display display = windowManager.getDefaultDisplay();
			DisplayMetrics outMetrics = new DisplayMetrics();
			display.getMetrics(outMetrics);
			wh = (int) ((outMetrics.widthPixels - (outMetrics.density * 10 * 4)) / 4);
		}
		
		public void setCurrentId(int currentId) {
			this.currentId = currentId;
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return SKIN_RESOURCES.length;
		}

		@Override
		public Object getItem(int position) {
			return SKIN_RESOURCES[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView view = null;
			if(convertView == null) {
				view = new ImageView(SkinSettingActivity.this);
				view.setLayoutParams(new AbsListView.LayoutParams(wh, wh));
				view.setScaleType(ImageView.ScaleType.FIT_CENTER);
			} else {
				view = (ImageView) convertView;
			}
			// 判断是否同一款背景
			if(position == currentId) {
				view.setBackgroundDrawable(ImageUtil.createSelectedTip(SkinSettingActivity.this, SKIN_RESOURCES[position], R.drawable.skin_selected_bg_tip));
			} else {
				view.setBackgroundResource(SKIN_RESOURCES[position]);
			}
			return view;
		}
		
	}
	
}


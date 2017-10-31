package com.huaxun.radio.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.huaxun.R;
import com.huaxun.app.AppApplication;
import com.huaxun.base.BaseActivity;
import com.huaxun.dialog.ExitDialog;
import com.huaxun.fragment.NewsRadioFragment;
import com.huaxun.radio.FastBlur;
import com.huaxun.radio.bean.RadioAudioURLDetail;
import com.huaxun.radio.bean.RadioItem;
import com.huaxun.radio.provider.MusicUtils;
import com.huaxun.tool.BaseTools;
import com.huaxun.tool.Constants;
import com.huaxun.utils.Util;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class RadioDetailActivity extends BaseActivity implements OnClickListener  {
	private LinearLayout rootLayout;
	private ImageButton topBack;
	private TextView topTitle;
	private ImageButton topList;
	private ScrollView scrollView;
	private ImageView radioImageView;
	private TextView radioIntroduceTextView;
	private RadioItem radioItem;
	private LinearLayout playControlLayout;
	private SeekBar progessBar;
	private ImageView playerUp;
	private ImageView playerNext;
	private ImageView handoverButton;
	private ImageView playPauseButton;
	private TextView playerTime;
	private TextView morePinglunTextView;
	
	private Map<String, String> URLMAP = new HashMap<String, String>();
	private PopupWindow popupWindow;
	private String playTitle;
	
	private boolean isCheckResult = false;
	
	private final static int refreshPlayTime = 0;
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case refreshPlayTime:
				playerTime.setText(NewsRadioFragment.DutationTime);
				this.sendEmptyMessageDelayed(refreshPlayTime, 1000);
				break;
			}
		}
	};
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.news_radio_detail_layout);
		rootLayout = (LinearLayout) findViewById(R.id.rootLayoutID);
		scrollView = (ScrollView) findViewById(R.id.scrollviewID);
		radioImageView = (ImageView) findViewById(R.id.radioImageID);
		progessBar = (SeekBar) findViewById(R.id.player_progressBar);
		playerUp = (ImageView) findViewById(R.id.player_up);
		playerNext = (ImageView) findViewById(R.id.player_next);
		handoverButton = (ImageView) findViewById(R.id.player_handover);
		playPauseButton = (ImageView) findViewById(R.id.playPauseView);
		playerTime = (TextView) findViewById(R.id.player_time);
		playControlLayout = (LinearLayout) findViewById(R.id.bigContainer);
		morePinglunTextView = (TextView) findViewById(R.id.morePingLunID);
		radioIntroduceTextView = (TextView) findViewById(R.id.radioIntroduceID);
		topBack = (ImageButton) findViewById(R.id.topBack);
		topTitle = (TextView) findViewById(R.id.topTitle);
		topList = (ImageButton) findViewById(R.id.topList);
		topBack.setOnClickListener(this);
		topList.setOnClickListener(this);
		
		Bundle bundle = getIntent().getExtras();
		Object obj = bundle.getSerializable("tag_activity");
		String resource = bundle.getString("resource");
		if (null != obj) {
			if (obj instanceof RadioItem) {
				radioItem = (RadioItem) obj;
			}
		}
		if (resource != null) {
			isCheckResult = true;
		}
		
		String imagePath = radioItem.imgurl1;
		if (imagePath != null){
			if (imagePath.startsWith(Constants.RadioFolderPath)){
				ImageLoader.getInstance().loadImage("file://" + imagePath, new MyImageListener());
			}else{
				ImageLoader.getInstance().loadImage(Util.getTestImageURL(radioItem.imgurl1), new MyImageListener());
			}
		}else{
			ImageLoader.getInstance().loadImage(Util.getTestImageURL(radioItem.imgurl1), new MyImageListener());
		}

		if (radioItem != null){
			String radioTitle = radioItem.newstitle;
			if (radioTitle != null){
				radioIntroduceTextView.setText(radioTitle);
			}
		}
		
		LayoutParams params = radioImageView.getLayoutParams();
		params.width = AppApplication.mWidth;
		params.height = params.width / 2 + Util.dip2px(this,45f);
		radioImageView.setLayoutParams(params);
		
		playPauseButton.setOnClickListener(this);
		playerUp.setOnClickListener(this);
		playerNext.setOnClickListener(this);
		handoverButton.setOnClickListener(this);
		progessBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub			
			}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub	
				progessBar.setProgress(0);
			}
		});
		playTitle = radioItem.audiourl.get(0).title;
		initPopWindow();
		handler.sendEmptyMessage(refreshPlayTime);
	}
	
	public void onDestroy() {
		super.onDestroy();
		handler.removeMessages(refreshPlayTime);
	}
	

	public void onClick(View v) {
		InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
		switch(v.getId()){
		case R.id.playPauseView:
			try {
				if (NewsRadioFragment.mService.isPlaying()) {
					playPauseButton.setImageResource(R.drawable.player_play);
				} else {
					playPauseButton.setImageResource(R.drawable.player_pause);
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			Intent detailIntent = new Intent("RADIO_CONTROL");
			detailIntent.putExtra("control", "playorpause");
			sendBroadcast(detailIntent);
			break;
		case R.id.player_next:
			break;
		case R.id.player_up:
			break;
		case R.id.player_handover:
			//applyRotation(0, 90); //当前页面从0度旋转到90度
			break;
		case R.id.morePingLunID:
			break;
		case R.id.topList:
			if (popupWindow.isShowing()){
				popupWindow.dismiss();
			}else{
				popupWindow.showAtLocation(findViewById(R.id.rootLayoutID), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
			}
			break;
		case R.id.topBack:
			 if (isCheckResult == true) {
				Intent Intent = new Intent("RADIO_CONTROL");
				Intent.putExtra("control", "playorpause");
				sendBroadcast(Intent); 
			 }
			 finish();
	         overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
			break;
		}
	}
	
	public void onBackPressed() {
		if (isCheckResult == true) {
			Intent Intent = new Intent("RADIO_CONTROL");
			Intent.putExtra("control", "playorpause");
			sendBroadcast(Intent); 
		 }
		 finish();
         overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}
	
	class MyImageListener extends SimpleImageLoadingListener {
		
		private Bitmap bitmap = null;

		@Override
		public void onLoadingFailed(String imageUri, View view,FailReason failReason) {
			super.onLoadingFailed(imageUri, view, failReason);
			bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.radio_detail_background);
			if (bitmap != null){
				radioImageView.setImageBitmap(bitmap);
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						NewBlur(bitmap);
					}
				}, 500);
			}
		}

		public void onLoadingComplete(String imageUri, View view,final Bitmap loadedImage) {
			super.onLoadingComplete(imageUri, view, loadedImage);
			if (loadedImage == null) {
				bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.radio_detail_background);
			}else{
				bitmap = loadedImage;
			}
			
			if (bitmap != null){
				radioImageView.setImageBitmap(bitmap);
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						NewBlur(bitmap);
					}
				}, 500);
			}
		}
	}
	
	private Bitmap overlay;
	private void NewBlur(Bitmap bitmap) {
		Bitmap bkg = big(bitmap);
	    float scaleFactor = 15;  
	    float radius = 20;
	    overlay = Bitmap.createBitmap((int) (AppApplication.mWidth / scaleFactor),(int) (AppApplication.mHeight / scaleFactor),  Bitmap.Config.ARGB_8888);  
	    Canvas canvas = new Canvas(overlay);  
	    canvas.scale(1 / scaleFactor, 1 / scaleFactor);  
	    Paint paint = new Paint();  
	    paint.setFlags(Paint.FILTER_BITMAP_FLAG);  
	    canvas.drawBitmap(bkg, 0, 0, paint);  
	    overlay = FastBlur.doBlur(overlay, (int) radius, true);
	    Drawable backgroundDrawable = new BitmapDrawable(getResources(), overlay);
	    rootLayout.setBackgroundDrawable(backgroundDrawable);
	    //rootLayout.getBackground().setAlpha(100);
	    //scrollView.getBackground().setAlpha(100);
		//playControlLayout.getBackground().setAlpha(100);
	}
	
	private Bitmap big(Bitmap bitmap) {
		  float scaleX = (float)AppApplication.mWidth / (float)bitmap.getWidth();
		  float scaleY = (float)AppApplication.mHeight / (float)bitmap.getHeight();
	      Matrix matrix = new Matrix();   
	      matrix.postScale(scaleX,scaleY);
	      Bitmap resizeBmp = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);  
	      return resizeBmp;  
	 }
	
	private void initPopWindow(){
		final ArrayList<String> titleList = new ArrayList<String> ();
		ArrayList<RadioAudioURLDetail> audiourlList = radioItem.audiourl;
		if (audiourlList != null){
			for (int i = 0; i < audiourlList.size(); i++){
				titleList.add(audiourlList.get(i).title);
				URLMAP.put(audiourlList.get(i).title, audiourlList.get(i).url);
			}
		}
	
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
        View view = inflater.inflate(R.layout.radio_detail_popupwindow_layout, null);
        ListView listView = (ListView)view.findViewById(R.id.listViewID);
        final PopupAdapter popupAdapter = new PopupAdapter(titleList);
        listView.setAdapter(popupAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				playTitle = titleList.get(position);
				popupAdapter.notifyDataSetChanged();
				
				Intent playIntent = new Intent("RADIO_CONTROL");
				playIntent.putExtra("url", URLMAP.get(playTitle));
				playIntent.putExtra("control", "start");
				sendBroadcast(playIntent);
			}
		});
        
        popupWindow = new PopupWindow(view);
        popupWindow.setWidth(AppApplication.mWidth);
        popupWindow.setHeight(600);
        Drawable drawable = getResources().getDrawable(R.drawable.pop_window_bg);
//		ColorDrawable cdw = new ColorDrawable(0x000000);
		popupWindow.setBackgroundDrawable(drawable);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setAnimationStyle(R.style.popupStyle);
	}
	
	private class PopupAdapter extends BaseAdapter{		
		private ArrayList<String> titleList = new ArrayList<String> ();
		private LayoutInflater mInflater;
		private PopupAdapter(ArrayList<String> titleList){
			this.titleList = titleList;
			mInflater = LayoutInflater.from(RadioDetailActivity.this);
		}
		
		@Override
		public int getCount() {
			return titleList.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = mInflater.inflate(R.layout.radio_detail_popupwindow_listitem, null);
			ImageView mImageView = (ImageView) convertView.findViewById(R.id.imageID);
			TextView mTextView = (TextView) convertView.findViewById(R.id.titleID);
			mTextView.setText(titleList.get(position));
			if (playTitle.equals(titleList.get(position))){
				mImageView.setVisibility(View.VISIBLE);
			}else{
				mImageView.setVisibility(View.INVISIBLE);
			}
			return convertView;
		}	
	}
	
}

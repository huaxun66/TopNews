package com.huaxun.news;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.huaxun.R;
import com.huaxun.app.AppApplication;
import com.huaxun.news.fragment.NewsMediaFragment;
import com.huaxun.tool.BaseTools;


public class DownloadFloatControl{
	
	private Context context;
	public WindowManager.LayoutParams wmParams;
    private WindowManager mWindowManager;
	private LinearLayout mFloatLayout;
	public ImageView allImageView;
	private boolean whetherMove = false;
	private Vibrator vibrator;
	private Handler handler;
	private boolean isAlreadAddView = false;
	private SharedPreferences sharedPreferences;
	private SharedPreferences.Editor editor;
	private static final String LocationX = "LocationX";
	private static final String LocationY = "LocationY";
	
	public DownloadFloatControl(final Context context,final Handler handler){
		isAlreadAddView = false;
		sharedPreferences = context.getSharedPreferences("DownloadFloatControl", Context.MODE_PRIVATE);
		editor = sharedPreferences.edit();
		this.handler = handler;
		this.context = context;
		wmParams = new WindowManager.LayoutParams();
		vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
		mWindowManager = (WindowManager) context.getApplicationContext().getSystemService(context.getApplicationContext().WINDOW_SERVICE);
		wmParams.type = android.view.WindowManager.LayoutParams.TYPE_PHONE;
		wmParams.format = PixelFormat.RGBA_8888;
		wmParams.flags = android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		wmParams.gravity = Gravity.LEFT | Gravity.TOP;
		wmParams.x = sharedPreferences.getInt(LocationX, 0);
		wmParams.y = sharedPreferences.getInt(LocationY, 200);
		wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		LayoutInflater inflater = LayoutInflater.from(context.getApplicationContext());
		mFloatLayout = (LinearLayout)inflater.inflate(R.layout.download_float_layout,null);
		allImageView = (ImageView) mFloatLayout.findViewById(R.id.downloadList);
		allImageView.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				//Toast.makeText(context, "长按事件", Toast.LENGTH_SHORT).show();
				allImageView.setScaleX((float) 1.5);
				allImageView.setScaleY((float) 1.5);
				whetherMove = true;
				vibrator.vibrate(150);
				return true;
			}
		});
		
		allImageView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE){
					wmParams.x = (int) event.getRawX() - allImageView.getMeasuredWidth() / 2;
					wmParams.y = (int) event.getRawY() - allImageView.getMeasuredHeight() / 2 - 80;
					allImageView.setScaleX((float) 1.5);
					allImageView.setScaleY((float) 1.5);
				} else if (event.getAction() == MotionEvent.ACTION_UP){
					whetherMove = false;
					allImageView.setScaleX((float) 1);
					allImageView.setScaleY((float) 1);
//					if ((int) event.getRawX() > AppApplication.mWidth/2){
//						wmParams.x = AppApplication.mWidth;
//					}else{
//						wmParams.x = 0;
//					}
//					wmParams.y = (int) event.getRawY()- allImageView.getMeasuredHeight() / 2 - 80;
					
					wmParams.x = (int) event.getRawX() - allImageView.getMeasuredWidth() / 2;
					wmParams.y = (int) event.getRawY() - allImageView.getMeasuredHeight() / 2 - 80;

					editor.putInt(LocationX, wmParams.x);
					editor.putInt(LocationY, wmParams.y);
					editor.commit();
				}
				mWindowManager.updateViewLayout(mFloatLayout, wmParams);

				return false;
			}
		});

		allImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handler.sendEmptyMessage(NewsMediaFragment.gotoDownloadList);
			}
		});
	}
	
	public void addView(){
		if (! isAlreadAddView){
			mWindowManager.addView(mFloatLayout, wmParams);
			isAlreadAddView = true;
		}
	}
	
	public void removeView(){
		if (isAlreadAddView){
			if (mFloatLayout != null){
				mWindowManager.removeView(mFloatLayout);
				isAlreadAddView = false;
			}
		}
	}
}

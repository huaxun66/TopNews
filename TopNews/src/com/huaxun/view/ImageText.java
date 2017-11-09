package com.huaxun.view;

import com.huaxun.R;
import com.huaxun.tool.Constants;
import com.huaxun.utils.Util;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class ImageText extends LinearLayout{
	private Context mContext = null;
	private ImageView mImageView = null;
	private TextView mTextView = null;
	private int DEFAULT_IMAGE_WIDTH;
	private int DEFAULT_IMAGE_HEIGHT;
	private int CHECKED_COLOR = Color.rgb(29, 118, 199);
	private int UNCHECKED_COLOR = Color.GRAY;
	public ImageText(Context context) {
		super(context);
		init(context);
	}

	public ImageText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public void init(Context context) {
		mContext = context;
		DEFAULT_IMAGE_WIDTH = Util.dip2px(context, 20);
		DEFAULT_IMAGE_HEIGHT = Util.dip2px(context, 20);
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View parentView = inflater.inflate(R.layout.image_text_layout, this, true);
		mImageView = (ImageView)findViewById(R.id.image_iamge_text);
		mTextView = (TextView)findViewById(R.id.text_iamge_text);
	}
	
	public void setImage(int id){
		if(mImageView != null){
			mImageView.setImageResource(id);
			setImageSize(DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT);
		}
	}

	public void setText(String s){
		if(mTextView != null){
			mTextView.setText(s);
			mTextView.setTextColor(UNCHECKED_COLOR);
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		return true;
	}
	
	private void setImageSize(int w, int h){
		if(mImageView != null){
			ViewGroup.LayoutParams params = mImageView.getLayoutParams();
			params.width = w;
			params.height = h;
			mImageView.setLayoutParams(params);
		}
	}
	
	public void setChecked(int itemID){
		if(mTextView != null){
			mTextView.setTextColor(CHECKED_COLOR);
		}
		int checkDrawableId = -1;
		switch (itemID){
		case Constants.BTN_FLAG_NEWS:
			checkDrawableId = R.drawable.news_selected;
			break;
		case Constants.BTN_FLAG_LIFE:
			checkDrawableId = R.drawable.life_selected;
			break;
		case Constants.BTN_FLAG_MUSIC:
			checkDrawableId = R.drawable.music_selected;
			break;
		case Constants.BTN_FLAG_RADIO:
			checkDrawableId = R.drawable.radio_selected;
			break;
		case Constants.BTN_FLAG_CHAT:
			checkDrawableId = R.drawable.chat_selected;
			break;
		case Constants.BTN_FLAG_MORE:
			checkDrawableId = R.drawable.more_selected;
			break;
		default:
			break;
		}
		if(mImageView != null){
			mImageView.setImageResource(checkDrawableId);
		}
	}

}

package com.huaxun.view;

import java.util.ArrayList;
import java.util.List;

import com.huaxun.R;
import com.huaxun.tool.BaseTools;
import com.huaxun.tool.Constants;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

public class BottomControlPanel extends RelativeLayout implements View.OnClickListener {
	private Context mContext;
	private ImageText mNewsBtn = null;
	private ImageText mLifeBtn = null;
	private ImageText mMusicBtn = null;
	private ImageText mRadioBtn = null;
	private ImageText mChatBtn = null;
	private int DEFALUT_BACKGROUND_COLOR = Color.rgb(243, 243, 243);
	private BottomPanelCallback mBottomCallback = null;
	private List<ImageText> viewList = new ArrayList<ImageText>();

	public interface BottomPanelCallback{
		public void onBottomPanelClick(int itemId);
	}
	
	public void setBottomCallback(BottomPanelCallback bottomCallback){
		mBottomCallback = bottomCallback;
	}
	
	public BottomControlPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void initBottomPanel(){
		if(mNewsBtn != null){
			mNewsBtn.setImage(R.drawable.news_unselected);
			mNewsBtn.setText("新闻");
		}
		
		if(mLifeBtn != null){
			mLifeBtn.setImage(R.drawable.life_unselected);
			mLifeBtn.setText("生活");
		}
		if(mMusicBtn != null){
			mMusicBtn.setImage(R.drawable.music_unselected);
			mMusicBtn.setText("音乐");
		}
		if(mRadioBtn != null){
			mRadioBtn.setImage(R.drawable.radio_unselected);
			mRadioBtn.setText("电台");
		}
		if(mChatBtn != null){
			mChatBtn.setImage(R.drawable.chat_unselected);
			mChatBtn.setText("聊天");
		}
	}
	
	public void defaultBtnChecked(){
		if(mNewsBtn != null){
			mNewsBtn.setChecked(Constants.BTN_FLAG_NEWS);
		}
	}
	
	@Override
	protected void onFinishInflate() {
		mNewsBtn = (ImageText)findViewById(R.id.btn_news);
		mLifeBtn = (ImageText)findViewById(R.id.btn_life);
		mMusicBtn = (ImageText)findViewById(R.id.btn_music);
		mRadioBtn = (ImageText)findViewById(R.id.btn_radio);		
		mChatBtn = (ImageText)findViewById(R.id.btn_chat);
		setBackgroundColor(DEFALUT_BACKGROUND_COLOR);
		viewList.add(mNewsBtn);
		viewList.add(mLifeBtn);
		viewList.add(mMusicBtn);
		viewList.add(mRadioBtn);			
		viewList.add(mChatBtn);
		setBtnListener();
	}
	
	private void setBtnListener(){
		int num = this.getChildCount();
		for(int i = 0; i < num; i++){
			View v = getChildAt(i);
			if(v != null){
				v.setOnClickListener(this);
			}
		}
	}

	public void onClick(View v) {
		initBottomPanel();
		int index = -1;
		switch(v.getId()){
		case R.id.btn_news:
			index = Constants.BTN_FLAG_NEWS;
			mNewsBtn.setChecked(Constants.BTN_FLAG_NEWS);
			break;
		case R.id.btn_life:
			index = Constants.BTN_FLAG_LIFE;
			mLifeBtn.setChecked(Constants.BTN_FLAG_LIFE);
			break;
		case R.id.btn_music:
			index = Constants.BTN_FLAG_MUSIC;
			mMusicBtn.setChecked(Constants.BTN_FLAG_MUSIC);
			break;
		case R.id.btn_radio:
			index = Constants.BTN_FLAG_RADIO;
			mRadioBtn.setChecked(Constants.BTN_FLAG_RADIO);
			break;
		case R.id.btn_chat:
			index = Constants.BTN_FLAG_CHAT;
			mChatBtn.setChecked(Constants.BTN_FLAG_CHAT);
			break;
		default:break;
		}
		if(mBottomCallback != null){
			mBottomCallback.onBottomPanelClick(index);
		}
	}

	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		// TODO Auto-generated method stub
		super.onLayout(changed, left, top, right, bottom);
		layoutItems(left, top, right, bottom);
	}
	
	/**在这里，因为左右两个Item的paddingLeft,paddingRight已知，主要用来动态设置中间两个Item的间距
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 */
	private void layoutItems(int left, int top, int right, int bottom){
		int n = getChildCount();
		if(n == 0){
			return;
		}
		int paddingLeft = getPaddingLeft();
		int paddingRight = getPaddingRight();
		//BaseTools.showlog("paddingLeft = " + paddingLeft + " paddingRight = " + paddingRight);
		int width = right - left;
		int height = bottom - top;
		//BaseTools.showlog("width = " + width + " height = " + height);
		int allViewWidth = 0;
		for(int i = 0; i< n; i++){
			View v = getChildAt(i);
			//BaseTools.showlog("v.getWidth() = " + v.getWidth());
			allViewWidth += v.getWidth();
		}
		int blankWidth = (width - allViewWidth - paddingLeft - paddingRight) / (n - 1);
		//BaseTools.showlog("blankV = " + blankWidth );
		
		//设置中间view的左边距
		for (int i=1; i<n-1; i++) {
			LayoutParams params = (LayoutParams) viewList.get(i).getLayoutParams();
			params.leftMargin = blankWidth;
			viewList.get(i).setLayoutParams(params);
		}

	}

}

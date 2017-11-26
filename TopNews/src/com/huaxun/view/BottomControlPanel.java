package com.huaxun.view;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.huaxun.R;
import com.huaxun.more.bean.Column;
import com.huaxun.tool.Constants;
import com.huaxun.utils.ColumnUtil;

public class BottomControlPanel extends LinearLayout implements View.OnClickListener {
	private int DEFALUT_BACKGROUND_COLOR = Color.rgb(243, 243, 243);
	private BottomPanelCallback mBottomCallback = null;

	public interface BottomPanelCallback {
		public void onBottomPanelClick(int itemId);
	}
	
	public void setBottomCallback(BottomPanelCallback bottomCallback){
		mBottomCallback = bottomCallback;
	}
	
	public BottomControlPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void initBottomPanel(Context context){
		ArrayList<Column> fgColumnList = ColumnUtil.getFgColumn(context);
		fgColumnList.add(new Column(Constants.BTN_FLAG_MORE, Constants.FRAGMENT_FLAG_MORE)); //"更多"栏目添加在最后
		removeAllViews();
		for (int i = 0; i < fgColumnList.size(); i++) {
			Column column = fgColumnList.get(i);
			ImageText mBtn = new ImageText(getContext());	
			mBtn.setId(column.columnId);
			mBtn.setImage(getImageById(column.columnId));
			mBtn.setText(column.columnName);
			mBtn.setTag(column.columnName);
//			mBtn.setBackgroundResource(R.drawable.touch_bg);
			LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(0, RelativeLayout.LayoutParams.WRAP_CONTENT);
			mParams.weight = 1;
//			if (i > 0) {
//				mParams.addRule(RelativeLayout.RIGHT_OF, fgColumnList.get(i-1).columnId);
//			}
			mBtn.setLayoutParams(mParams);
			addView(mBtn);
		}
		setBtnListener();
	}
	
	public void resetAllBtns() {
		int num = this.getChildCount();
		for(int i = 0; i < num; i++){
			ImageText mBtn = (ImageText)getChildAt(i);
			mBtn.setImage(getImageById(mBtn.getId()));
			mBtn.setText((String)mBtn.getTag());
		}
	}
	
	public void btnChecked(int id) {
		ImageText button = getButtonById(id);
		if (button != null) {
			button.setChecked(id);
		}
	}
	
	private int getImageById(int id) {
		switch(id) {
		case Constants.BTN_FLAG_NEWS:
			return R.drawable.news_unselected;
		case Constants.BTN_FLAG_LIFE:
			return R.drawable.life_unselected;
		case Constants.BTN_FLAG_MUSIC:
			return R.drawable.music_unselected;
		case Constants.BTN_FLAG_RADIO:
			return R.drawable.radio_unselected;
		case Constants.BTN_FLAG_CHAT:
			return R.drawable.chat_unselected;
		case Constants.BTN_FLAG_MORE:
			return R.drawable.more_unselected;
		default:
			return -1;
		}
	}
	
	private ImageText getButtonById(int id) {
		int num = this.getChildCount();
		for(int i = 0; i < num; i++){
			View v = getChildAt(i);
			if(v.getId() == id){
				return (ImageText)v;
			}
		}
		return null;
	}
	
	@Override
	protected void onFinishInflate() {
		setBackgroundColor(DEFALUT_BACKGROUND_COLOR);
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
		resetAllBtns();
		btnChecked(v.getId());
		if(mBottomCallback != null){
			mBottomCallback.onBottomPanelClick(v.getId());
		}
	}

	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
//		layoutItems(left, top, right, bottom);
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
		for(int i = 0; i < n; i++) {
			View v = getChildAt(i);
			//BaseTools.showlog("v.getWidth() = " + v.getWidth());
			allViewWidth += v.getWidth();
		}
		int marginWidth = (width - allViewWidth - paddingLeft - paddingRight) / (2 * n);
		//BaseTools.showlog("blankV = " + blankWidth );
		
		//设置中间view的左右边距
		for (int i = 0; i < n; i++) {
			View v = getChildAt(i);
			LayoutParams params = (LayoutParams) v.getLayoutParams();
			params.leftMargin = marginWidth;
			params.rightMargin = marginWidth;
			v.setLayoutParams(params);
		}

	}

}

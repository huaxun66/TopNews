package com.huaxun.music;

import com.huaxun.R;
import com.huaxun.utils.Util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;


/**这个自定义的listview暂时没有用到，musicfragment中我们用的是swiplistview*/
public class MusicListView extends ListView implements OnTouchListener, OnGestureListener {
    
	private GestureDetector mGestureDetector;
	private OnDeleteListener onDeleteListener;
	private boolean isDeleteShown = false;
	private ViewGroup itemLayout;
	private View deleteTV;
	private int selectedItem;
	
	public interface OnDeleteListener {  	    	  
	   public abstract void onDelete(int index);  	  
	}

	public MusicListView(Context context, AttributeSet attrs){
		super(context, attrs);
		mGestureDetector = new GestureDetector(context, this);
		setOnTouchListener(this);
	}

    public void setOnDeleteListener(OnDeleteListener onDeleteListener) {  
    	this.onDeleteListener = onDeleteListener;  
    }  
	    
	@Override
	public boolean onTouch(View v, MotionEvent event) {	
		if (isDeleteShown) {
			itemLayout.removeView(deleteTV); 
			Animation anim= AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_right);
        	deleteTV.startAnimation(anim);        	
            deleteTV = null;  
            isDeleteShown = false; 
            return true;
		} else {
			return mGestureDetector.onTouchEvent(event);
		}	
	}
	
	@Override
	public boolean onDown(MotionEvent e) {
		selectedItem = pointToPosition((int)e.getX(),(int)e.getY());
		return false;
	}
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (!isDeleteShown && Math.abs(velocityX) > Math.abs(velocityY) && velocityX < 0) { 
    		itemLayout = (ViewGroup) getChildAt(selectedItem - getFirstVisiblePosition()); 
        	deleteTV = LayoutInflater.from(getContext()).inflate(R.layout.delete_item, null); 
    		deleteTV.setOnClickListener(new OnClickListener(){
    			@Override
    			public void onClick(View view) {
    				itemLayout.removeView(deleteTV);
    				deleteTV = null;
    				isDeleteShown = false;
    				onDeleteListener.onDelete(selectedItem);
    			}       		
        	});     
        	RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(Util.dip2px(getContext(), 70),itemLayout.getMeasuredHeight());    	
        	params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        	params.addRule(RelativeLayout.CENTER_VERTICAL);
        	itemLayout.addView(deleteTV, params);
        	Animation anim= AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_right);
        	deleteTV.startAnimation(anim);
        	isDeleteShown = true; 
        } 
		return true;
	}
	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

}

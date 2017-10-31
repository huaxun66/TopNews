package com.huaxun.music;

import com.huaxun.music.activity.MusicDetailActivity;
import com.huaxun.tool.BaseTools;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnTouchListener;

public class LrcGesture implements OnTouchListener, OnGestureListener{
	private MusicDetailActivity context;
	private GestureDetector mGestureDetector;

	public LrcGesture(MusicDetailActivity context){
		this.context = context;
		mGestureDetector = new GestureDetector(context, this);
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
        case MotionEvent.ACTION_MOVE: {
//            BaseTools.showlog("MotionEvent.ACTION_MOVE");
            break;
        }
        case MotionEvent.ACTION_UP: {       	
        	context.seekMusic();
//        	BaseTools.showlog("MotionEvent.ACTION_UP");
            return true;
        }
        case MotionEvent.ACTION_DOWN: {       	
//        	BaseTools.showlog("MotionEvent.ACTION_DOWN");
            break;
        }
    }
		return mGestureDetector.onTouchEvent(event);
	}
	
	@Override
	public boolean onDown(MotionEvent arg0) {
		return true;
	}
	@Override
	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
        return false;
	}
	@Override
	public void onLongPress(MotionEvent arg0) {
	}
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {		
		if (Math.abs(distanceY)>Math.abs(distanceX)) {
//			BaseTools.showlog("onScroll,distanceY="+distanceY);
			context.scrollLrc(-distanceY);
		}
		return false;
	}
	@Override
	public void onShowPress(MotionEvent arg0) {
	}
	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		return false;
	}

}

package com.huaxun.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ListView;
import com.huaxun.MainActivity;

public class BaseFragment extends Fragment {
	public MainActivity mainAct;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mainAct = (MainActivity) getActivity();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	/*
	 *  判断上滑或下滑控制底部导航显隐
	 */
	protected int lastVisibleItemPosition = 0;// 标记上次滑动位置
    protected int mScreenY = 0;
    protected View childAt;
    protected int[] location = new int[2];
	protected boolean isRefreshFooter = false;
	public void onScrollShowOrHide(int firstVisibleItem, ListView mListView){
        childAt = mListView.getChildAt(0);
        if (childAt != null){
            childAt.getLocationOnScreen(location);
        }
		
        if (firstVisibleItem > lastVisibleItemPosition) {// 上滑
        	mainAct.hideBottomPanel();
        	mScreenY = location[1];
		} else if (firstVisibleItem < lastVisibleItemPosition) {// 下滑
			mainAct.showBottomPanel();
			mScreenY = location[1];} 
		else {
            if(mScreenY > location[1] + 10){
            	mainAct.hideBottomPanel();
            }
            else if(mScreenY < location[1] - 10){
            	mainAct.showBottomPanel();
            }
            if (isRefreshFooter == false) {
            	mScreenY = location[1];
            }	                
        }
        lastVisibleItemPosition = firstVisibleItem;
	}

}


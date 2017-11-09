package com.huaxun.fragment;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager.LayoutParams;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huaxun.R;
import com.huaxun.base.BaseFragment;
import com.huaxun.more.DragGrid;
import com.huaxun.more.adapter.MoreDragAdapter;
import com.huaxun.more.adapter.MoreOtherAdapter;
import com.huaxun.more.bean.Column;
import com.huaxun.news.OtherGridView;
import com.huaxun.utils.ColumnUtil;

public class MoreFragment extends BaseFragment implements OnItemClickListener{
	private View view;
	private DragGrid foregroundGridView;
	private OtherGridView backgroundGridView;
	private MoreDragAdapter foregroundAdapter;
	private MoreOtherAdapter backgroundAdapter;
	
	private ArrayList<Column> fgColumnList = new ArrayList<>();
	private ArrayList<Column> bgColumnList = new ArrayList<>();
	
	private Handler handler = new Handler();
	/** 是否在移动，由于这边是动画结束后才进行的数据更替，设置这个限制为了避免操作太频繁造成的数据错乱  */	
	boolean isMove = false;

	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.more_layout,container, false);
		foregroundGridView = (DragGrid) view.findViewById(R.id.fgGridView);
		backgroundGridView = (OtherGridView) view.findViewById(R.id.bgGridView);
		foregroundAdapter = new MoreDragAdapter(mainAct);
		foregroundAdapter.setColumnList(fgColumnList);
		foregroundGridView.setAdapter(foregroundAdapter);
	    
	    backgroundAdapter = new MoreOtherAdapter(mainAct);
	    backgroundAdapter.setColumnList(bgColumnList);
	    backgroundGridView.setAdapter(backgroundAdapter);
	    
	    foregroundGridView.setOnItemClickListener(this);
	    backgroundGridView.setOnItemClickListener(this);
		
		initData();
		return view;
	}
	
	/** 初始化数据*/
	private void initData() {
		new Thread(new Runnable(){
			@Override
			public void run() {
				fgColumnList = ColumnUtil.getFgColumn(mainAct);	
				bgColumnList = ColumnUtil.getBgColumn(mainAct);
			    handler.post(new Runnable(){
					@Override
					public void run() {
						foregroundAdapter.setColumnList(fgColumnList);
						foregroundAdapter.notifyDataSetChanged();		
						backgroundAdapter.setColumnList(bgColumnList);
						backgroundAdapter.notifyDataSetChanged();   
					}
				});
			}			
		}).start();	
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        //如果点击的时候，之前动画还没结束，那么就让点击事件无效  
        if(isMove){
            return;  
        } 
		switch (parent.getId()){
		case R.id.fgGridView:
			//view转换成ImageView
			final ImageView moveImageView = getView(view);
            if (moveImageView != null) {
            	//获取动画起始点坐标
				TextView newTextView = (TextView) view.findViewById(R.id.text_item);
				final int[] startLocation = new int[2];
				newTextView.getLocationInWindow(startLocation);
				//获取点击的node
			    final Column column = foregroundAdapter.getItem(position);		    
			    //backgroundAdapter最后一个item不可见
			    backgroundAdapter.setVisible(false);
			    //更多频道列表最后添加点击的node
			    backgroundAdapter.addItem(column);
			    //设置我的频道要删除node的位置
			    foregroundAdapter.setRemove(position);

			    handler.postDelayed(new Runnable() {
				    public void run() {
					    try {
						    int[] endLocation = new int[2];
					    	//获取动画终点的坐标
						    backgroundGridView.getChildAt(backgroundGridView.getLastVisiblePosition()).getLocationInWindow(endLocation);
						    //动画执行
						    MoveAnim(moveImageView, startLocation , endLocation, column ,foregroundGridView);
					    } catch (Exception localException) {
					    }
			    	}
			    }, 50L);
             }
			break;
		case R.id.bgGridView:
			final ImageView moveImageView2 = getView(view);
            if (moveImageView2 != null) {            	
				TextView newTextView = (TextView) view.findViewById(R.id.text_item);
				final int[] startLocation = new int[2];
				newTextView.getLocationInWindow(startLocation);
			    final Column column = backgroundAdapter.getItem(position);		    
			    foregroundAdapter.setVisible(false);
			    foregroundAdapter.addItem(column);
			    backgroundAdapter.setRemove(position);

			    handler.postDelayed(new Runnable() {
				    public void run() {
					    try {
						    int[] endLocation = new int[2];
					    	//获取终点的坐标
						    foregroundGridView.getChildAt(foregroundGridView.getLastVisiblePosition()).getLocationInWindow(endLocation);
						    MoveAnim(moveImageView2, startLocation , endLocation, column, backgroundGridView);
					    } catch (Exception localException) {
					    }
			    	}
			    }, 50L);
             }		
			break;
		}
	}
	
	/**
	 * 获取点击的Item的对应View，
	 * @param view
	 * @return
	 */
	private ImageView getView(View view) {
		view.destroyDrawingCache();
		view.setDrawingCacheEnabled(true);
		Bitmap cache = Bitmap.createBitmap(view.getDrawingCache());
		view.setDrawingCacheEnabled(false);
		ImageView iv = new ImageView(mainAct);
		iv.setImageBitmap(cache);
		return iv;
	}
	
	/**
	 * 点击ITEM移动动画
	 * @param moveView
	 * @param startLocation
	 * @param endLocation
	 *  
	 * @param moveChannel
	 * @param clickGridView
	 */
	private void MoveAnim(View moveView, int[] startLocation,int[] endLocation, final Column node,
			final GridView clickGridView) {
		int[] initLocation = new int[2];
		//获取传递过来的VIEW的坐标
		moveView.getLocationInWindow(initLocation);
		//得到要移动的VIEW,并放入对应的容器中
		final ViewGroup moveViewGroup = getMoveViewGroup();
		final View mMoveView = getMoveView(moveViewGroup, moveView, initLocation);
		//创建移动动画
		TranslateAnimation moveAnimation = new TranslateAnimation(
				startLocation[0], endLocation[0], startLocation[1],
				endLocation[1]);
		moveAnimation.setDuration(300L);//动画时间
		//动画配置
		AnimationSet moveAnimationSet = new AnimationSet(true);
		moveAnimationSet.setFillAfter(false);//动画效果执行完毕后，View对象不保留在终止的位置
		moveAnimationSet.addAnimation(moveAnimation);
		mMoveView.startAnimation(moveAnimationSet);
		moveAnimationSet.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				//开始移动
				isMove = true;
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				moveViewGroup.removeView(mMoveView);
				if (clickGridView instanceof DragGrid) {
					//backgroundAdapter最后一个item可见
					backgroundAdapter.setVisible(true);
					backgroundAdapter.notifyDataSetChanged();
					//删除前台栏目刚刚点击的Column
					foregroundAdapter.remove();
				}else{				
					foregroundAdapter.setVisible(true);
					foregroundAdapter.notifyDataSetChanged();
					backgroundAdapter.remove();
				}
				//移动结束
				isMove = false;
				ArrayList<Column> fgColumnList = foregroundAdapter.getColumnList();
				ColumnUtil.setColumnListOrder(mainAct, fgColumnList);
				mainAct.initBottomPanel(); //刷新底部
			}
		});
	}
	
	/**
	 * 获取移动的VIEW，放入对应ViewGroup布局容器
	 * @param viewGroup
	 * @param view
	 * @param initLocation
	 * @return
	 */
	private View getMoveView(ViewGroup viewGroup, View view, int[] initLocation) {
		int x = initLocation[0];
		int y = initLocation[1];
		viewGroup.addView(view);
		LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		mLayoutParams.leftMargin = x;
		mLayoutParams.topMargin = y;
		view.setLayoutParams(mLayoutParams);
		return view;
	}
	
	/**
	 * 创建移动的ITEM对应的ViewGroup布局容器
	 */
	private ViewGroup getMoveViewGroup() {
		ViewGroup moveViewGroup = (ViewGroup) mainAct.getWindow().getDecorView();
		LinearLayout moveLinearLayout = new LinearLayout(mainAct);
		moveLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		moveViewGroup.addView(moveLinearLayout);
		return moveLinearLayout;
	}

}

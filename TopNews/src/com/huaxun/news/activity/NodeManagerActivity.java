package com.huaxun.news.activity;

import java.util.ArrayList;

import com.huaxun.MainActivity;
import com.huaxun.R;
import com.huaxun.base.BaseActivity;
import com.huaxun.db.DataDB;
import com.huaxun.fragment.NewsFragment;
import com.huaxun.news.adapter.DragAdapter;
import com.huaxun.news.adapter.OtherAdapter;
import com.huaxun.news.bean.Node;
import com.huaxun.tool.BaseTools;
import com.huaxun.utils.NodeUtil;
import com.huaxun.utils.Util;
import com.huaxun.news.DragGrid;
import com.huaxun.news.OtherGridView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager.LayoutParams;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 频道管理
 * 详情见：
 * http://blog.csdn.net/vipzjyno1/article/details/25005851
 */
public class NodeManagerActivity extends BaseActivity implements OnItemClickListener{

	private DataDB dataDB;
	/** 用户栏目的GRIDVIEW */
	private DragGrid userGridView;
	/** 其它栏目的GRIDVIEW */
	private OtherGridView otherGridView;
	/** 用户栏目对应的适配器，可以拖动 */
	DragAdapter userAdapter;
	/** 其它栏目对应的适配器 */
	OtherAdapter otherAdapter;
	/** 其它栏目列表 */
	ArrayList<Node> otherNodeList = new ArrayList<Node>();
	/** 用户栏目列表 */
	ArrayList<Node> userNodeList = new ArrayList<Node>();
	/** 是否在移动，由于这边是动画结束后才进行的数据更替，设置这个限制为了避免操作太频繁造成的数据错乱。 */	
	boolean isMove = false;
	private TextView topBack, topTitle;
 
	private Handler handler = new Handler();
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Util.setTranslucent(this);
		mRecordOrderReceiver = new RecordOrderReceiver();
		IntentFilter mFilter = new IntentFilter("record_order");
		this.registerReceiver(mRecordOrderReceiver, mFilter);
		dataDB = DataDB.getInstance(this);
		setContentView(R.layout.node_manager);
		initView();
		initData();
	}
	
	public void onDestroy() {
		super.onDestroy();
		this.unregisterReceiver(mRecordOrderReceiver);
	}
	
	/** 初始化布局*/
	private void initView() {
		userGridView = (DragGrid) findViewById(R.id.userGridView);
		otherGridView = (OtherGridView) findViewById(R.id.otherGridView);
		topBack = (TextView) findViewById(R.id.topBack);
		topTitle = (TextView) findViewById(R.id.topTitle);
		topTitle.setText("栏目管理");
		
	    userAdapter = new DragAdapter(this);
	    userAdapter.setNodeList(userNodeList);
	    userGridView.setAdapter(userAdapter);
	    
	    otherAdapter = new OtherAdapter(this);
	    otherAdapter.setNodeList(otherNodeList);
	    otherGridView.setAdapter(otherAdapter);
	    
	    //设置GRIDVIEW的ITEM的点击监听
	    otherGridView.setOnItemClickListener(this);
	    userGridView.setOnItemClickListener(this);
	    topBack.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View v) {
	        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
	        if(userAdapter.isListChanged()){
                 setResult(NewsFragment.NODERESULT1, intent);
            }else{
            	 setResult(NewsFragment.NODERESULT2, intent);	
	        }
	        finish();
	        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	    }
	  });		
	}
	
	/** 初始化数据*/
	private void initData() {
		new Thread(new Runnable(){
			@Override
			public void run() {
				userNodeList  = dataDB.getUserNodes();
			    NodeUtil.reorderNodeList(NodeManagerActivity.this,userNodeList);		       
			    otherNodeList = dataDB.getOtherNodes();		
			    handler.post(new Runnable(){
					@Override
					public void run() {
						 userAdapter.setNodeList(userNodeList);
						 userAdapter.notifyDataSetChanged();		
						 otherAdapter.setNodeList(otherNodeList);
						 otherAdapter.notifyDataSetChanged();   
					}		
				});
			}			
		}).start();	
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        //如果点击的时候，之前动画还没结束，那么就让点击事件无效  
        if(isMove){
            return;  
        } 
		switch (parent.getId()){
		case R.id.userGridView:
			//position为 0，1 的不可以进行任何操作
			if (position != 0 && position != 1) {
				//view转换成ImageView
				final ImageView moveImageView = getView(view);
                if (moveImageView != null) {
                	//获取动画起始点坐标
					TextView newTextView = (TextView) view.findViewById(R.id.text_item);
					final int[] startLocation = new int[2];
					newTextView.getLocationInWindow(startLocation);
					//获取点击的node
				    final Node node = userAdapter.getItem(position);
				    //把数据库里node的isselected状态改变，并将node添加到最后
				    node.isselected = "0";
				    dataDB.addNodeToEnd(node);
				    
				    //otherAdapter最后一个item不可见
				    otherAdapter.setVisible(false);
				    //更多频道列表最后添加点击的node
				    otherAdapter.addItem(node);
				    //设置我的频道要删除node的位置
				    userAdapter.setRemove(position);
				    //更新 我的频道 列表
				    //userAdapter.setNodeList(dataDB.getUserNodes());
				    //userAdapter.notifyDataSetChanged();
				    //更新 更多频道 列表
				    //otherAdapter.setNodeList(dataDB.getOtherNodes());
				    //otherAdapter.notifyDataSetChanged();

				    handler.postDelayed(new Runnable() {
					    public void run() {
						    try {
							    int[] endLocation = new int[2];
						    	//获取动画终点的坐标
							    otherGridView.getChildAt(otherGridView.getLastVisiblePosition()).getLocationInWindow(endLocation);
							    //动画执行
							    MoveAnim(moveImageView, startLocation , endLocation, node ,userGridView);
						    } catch (Exception localException) {
						    }
				    	}
				    }, 50L);
                 }
			}
			break;
		case R.id.otherGridView:
			final ImageView moveImageView = getView(view);
            if (moveImageView != null) {            	
				TextView newTextView = (TextView) view.findViewById(R.id.text_item);
				final int[] startLocation = new int[2];
				newTextView.getLocationInWindow(startLocation);
				
			    final Node node = otherAdapter.getItem(position);
			    node.isselected = "1";
			    dataDB.addNodeToEnd(node);
			    
			    userAdapter.setVisible(false);
			    userAdapter.addItem(node);
			    otherAdapter.setRemove(position);
//			    userAdapter.setNodeList(dataDB.getUserNodes());
//			    userAdapter.notifyDataSetChanged();
//			    
//			    otherAdapter.setNodeList(dataDB.getOtherNodes());
//			    otherAdapter.notifyDataSetChanged();

			    handler.postDelayed(new Runnable() {
				    public void run() {
					    try {
						    int[] endLocation = new int[2];
					    	//获取终点的坐标
						    userGridView.getChildAt(userGridView.getLastVisiblePosition()).getLocationInWindow(endLocation);
						    MoveAnim(moveImageView, startLocation , endLocation, node ,otherGridView);
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
		ImageView iv = new ImageView(this);
		iv.setImageBitmap(cache);
		return iv;
	}
	
	/**
	 * 点击ITEM移动动画
	 * @param moveView
	 * @param startLocation
	 * @param endLocation
	 * @param moveChannel
	 * @param clickGridView
	 */
	private void MoveAnim(View moveView, int[] startLocation,int[] endLocation, final Node node,
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
		moveAnimation.setDuration(500L);//动画时间
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
				//判断点击的是DragGrid还是OtherGridView
				if (clickGridView == userGridView) {
					//otherAdapter最后一个item可见
					otherAdapter.setVisible(true);
					otherAdapter.notifyDataSetChanged();
					//删除我的频道刚刚点击的node
					userAdapter.remove();
				}else{				
					userAdapter.setVisible(true);
					userAdapter.notifyDataSetChanged();
					otherAdapter.remove();
				}
				//移动结束
				isMove = false;
				ArrayList<Node> nodeList = userAdapter.getNodeList();
				NodeUtil.setNodeListOrder(NodeManagerActivity.this,nodeList);
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
		ViewGroup moveViewGroup = (ViewGroup) getWindow().getDecorView();
		LinearLayout moveLinearLayout = new LinearLayout(this);
		moveLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		moveViewGroup.addView(moveLinearLayout);
		return moveLinearLayout;
	}
	
	public void onBackPressed() {
		if(userAdapter.isListChanged()){
			Intent intent = new Intent(getApplicationContext(), MainActivity.class);
			setResult(NewsFragment.NODERESULT1, intent);
			finish();
		}else{
			super.onBackPressed();
		}
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}
	
	private RecordOrderReceiver mRecordOrderReceiver;
	class RecordOrderReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			ArrayList<Node> nodeList = userAdapter.getNodeList();
			NodeUtil.setNodeListOrder(NodeManagerActivity.this,nodeList);
		}
		
	}

}

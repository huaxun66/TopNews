package com.huaxun.fragment;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huaxun.R;
import com.huaxun.base.BaseFragment;
import com.huaxun.life.fragment.BlogListFragment;
import com.huaxun.life.fragment.HotListFragment;
import com.huaxun.life.fragment.LifeListFragment;
import com.huaxun.news.adapter.NewsFragmentPagerAdapter;
import com.huaxun.news.bean.Node;
import com.huaxun.utils.Util;
import com.huaxun.view.ColumnHorizontalScrollView;

public class LifeNewFragment extends BaseFragment {
	/** 自定义HorizontalScrollView */
	private View view;
	private Context context;
	private ColumnHorizontalScrollView mColumnHorizontalScrollView;
	LinearLayout mRadioGroup_content;
	LinearLayout ll_more_columns;
	RelativeLayout rl_column;
	public static LinearLayout ll_channel;
		
	private ViewPager mViewPager;
	private ImageView button_more_columns;
	/** 所有的新闻分类列表*/
	private ArrayList<Node> allNodes = new ArrayList<Node>();
	/** 用户选择的新闻分类列表*/
	private ArrayList<Node> userNodesList = new ArrayList<Node>();
	/** 当前选中的栏目*/
	private int columnSelectIndex = 0;
	/** 左阴影部分*/
	public ImageView shade_left;
	/** 右阴影部分*/
	public ImageView shade_right;
	/** 屏幕宽度 */
	private int mScreenWidth = 0;
	//首页我的频道的所有fragment集合
	private ArrayList<Fragment> fragments = new ArrayList<Fragment>();
	
	// 栏目类型
	public static final int NODE_TYPE_XW = 5;//新闻
	public static final int NODE_TYPE_YL = 21;//娱乐
	public static final int NODE_TYPE_FC = 23;//房产
	public static final int NODE_TYPE_QC = 24;//汽车
	public static final int NODE_TYPE_SQ = 27;//社区
	
	public static final int NODE_TYPE_RM = 66;//热门
	public static final int NODE_TYPE_ZX = 67;//最新
	public static final int NODE_TYPE_TJ = 68;//推荐
	
	public static final int NODE_TYPE_BLOG = 69; //博客

	
	public Handler handler = new Handler();
	
	public void onCreate(Bundle savedInstanceState) {
		this.context = this.getActivity();
		mScreenWidth = Util.getWindowsWidth(this.getActivity());		
		super.onCreate(savedInstanceState);
	}

	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		view = LayoutInflater.from(getActivity()).inflate(R.layout.news_fragment_layout, null);
		initView();
		setChannelView();
		return view;
	}
	
	/** 初始化layout控件*/
	private void initView() {
		mColumnHorizontalScrollView =  (ColumnHorizontalScrollView) view.findViewById(R.id.mColumnHorizontalScrollView);
		mRadioGroup_content = (LinearLayout) view.findViewById(R.id.mRadioGroup_content);
		ll_more_columns = (LinearLayout) view.findViewById(R.id.ll_more_columns);
		rl_column = (RelativeLayout) view.findViewById(R.id.rl_column);
		button_more_columns = (ImageView) view.findViewById(R.id.button_more_columns);
		mViewPager = (ViewPager) view.findViewById(R.id.mViewPager);
		shade_left = (ImageView) view.findViewById(R.id.shade_left);
		shade_right = (ImageView) view.findViewById(R.id.shade_right);
		ll_channel = (LinearLayout) view.findViewById(R.id.ll_channel);
	}
	
	/** 
	 *  当栏目项发生变化时候调用
	 * */
	private void setChannelView() {		
		userNodesList.add(new Node("5", "新闻"));
		userNodesList.add(new Node("21", "娱乐"));
		userNodesList.add(new Node("23", "房产"));
		userNodesList.add(new Node("24", "汽车"));
		userNodesList.add(new Node("27", "社区"));
		
		userNodesList.add(new Node("66", "热门"));
		userNodesList.add(new Node("67", "最新"));
		userNodesList.add(new Node("68", "推荐"));
		userNodesList.add(new Node("69", "博客"));
		
		initTabColumn();
		initFragment();
	}

	/** 
	 *  初始化Column栏目项
	 * */
	private void initTabColumn() {
		mRadioGroup_content.removeAllViews();
		int count =  userNodesList.size();
		mColumnHorizontalScrollView.setParam(this.getActivity(), mScreenWidth, mRadioGroup_content, shade_left, shade_right, ll_more_columns, rl_column);
		for(int i = 0; i< count; i++){
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT , LayoutParams.WRAP_CONTENT);
			params.leftMargin = Util.dip2px(context, 10);
			params.rightMargin = Util.dip2px(context, 10);
//			TextView localTextView = (TextView) mInflater.inflate(R.layout.column_radio_item, null);
			TextView columnTextView = new TextView(this.getActivity());
			columnTextView.setTextAppearance(this.getActivity(), R.style.top_category_scroll_view_item_text);
//			localTextView.setBackground(getResources().getDrawable(R.drawable.top_category_scroll_text_view_bg));
			columnTextView.setBackgroundResource(R.drawable.radio_buttong_bg);
			columnTextView.setGravity(Gravity.CENTER);
			columnTextView.setPadding(Util.dip2px(context, 2), Util.dip2px(context, 2), Util.dip2px(context, 2), Util.dip2px(context, 2));
			columnTextView.setId(i);
			columnTextView.setText(userNodesList.get(i).nodename);
			columnTextView.setTextColor(getResources().getColorStateList(R.color.top_category_scroll_text_color_day));
			if(columnSelectIndex == i){
				columnTextView.setSelected(true);
			}
			columnTextView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
			          for(int i = 0;i < mRadioGroup_content.getChildCount();i++){
				          View localView = mRadioGroup_content.getChildAt(i);
				          if (localView != v)
				        	  localView.setSelected(false);
				          else{
				        	  localView.setSelected(true);
				        	  mViewPager.setCurrentItem(i);
				          }
			          }
			          Util.showToast(context, userNodesList.get(v.getId()).nodename);			       
				}
			});

			mRadioGroup_content.addView(columnTextView, i ,params);
		}
	}
	/** 
	 *  选择的Column里面的Tab
	 * */
	private void selectTab(int tab_postion) {
		columnSelectIndex = tab_postion;
		for (int i = 0; i < mRadioGroup_content.getChildCount(); i++) {
			View checkView = mRadioGroup_content.getChildAt(tab_postion);
			int k = checkView.getMeasuredWidth();
			int l = checkView.getLeft();
			int i2 = l + k / 2 - mScreenWidth / 2;
			mColumnHorizontalScrollView.smoothScrollTo(i2, 0);
		}
		/**是否选中*/
		for (int j = 0; j <  mRadioGroup_content.getChildCount(); j++) {
			View checkView = mRadioGroup_content.getChildAt(j);
			boolean ischeck;
			if (j == tab_postion) {
				ischeck = true;
			} else {
				ischeck = false;
			}
			checkView.setSelected(ischeck);
		}
	}
	/** 
	 *  初始化Fragment
	 * */
	private void initFragment() {
		int count = userNodesList.size();
		/**
		 * 每次初始化Fragment之前必须清一下fragments，否则新加的fragment只是添加在fragments后面，
		 * 导致主界面我的频道不能正确更新
		 */
		fragments.clear();
		for (int i = 0; i < count; i++) {
			int nodeid = Integer.parseInt(userNodesList.get(i).nodeid);
			Object HotListFragment;
			switch (nodeid) {
			case NODE_TYPE_XW:// 新闻
			case NODE_TYPE_YL:// 娱乐
			case NODE_TYPE_FC:// 房产
			case NODE_TYPE_QC:// 汽车
			case NODE_TYPE_SQ:// 社区
				LifeListFragment lifeListFragment = new LifeListFragment();
				Bundle data = new Bundle();
				data.putInt("channelId", nodeid);
				data.putString("nodeName", userNodesList.get(i).nodename);
				lifeListFragment.setArguments(data);
				fragments.add(lifeListFragment);
				break;
			case NODE_TYPE_RM://热门
			case NODE_TYPE_ZX://最新
			case NODE_TYPE_TJ://推荐	
			     HotListFragment hotListFragment = new HotListFragment();
				 Bundle data1 = new Bundle();
				 data1.putInt("channelId", nodeid);
				 data1.putString("nodeName", userNodesList.get(i).nodename);
				 hotListFragment.setArguments(data1);
				 fragments.add(hotListFragment);	  
			     break;
			case NODE_TYPE_BLOG://博客	
			     BlogListFragment blogListFragment = new BlogListFragment();
				 fragments.add(blogListFragment);  
			     break;
			default:
				break;
			}
		}
		/**注意，fragment嵌套使用时需要用getChildFragmentManager,用getSupportFragmentManager会出现子fragment显示空白的问题 */
		NewsFragmentPagerAdapter mAdapetr = new NewsFragmentPagerAdapter(getChildFragmentManager(), fragments);
		//mViewPager.setOffscreenPageLimit(0); //设置预加载，v4包，默认是1
		mViewPager.setAdapter(mAdapetr);
		mViewPager.setOnPageChangeListener(pageListener);
	}
	/** 
	 *  ViewPager切换监听方法
	 * */
	public OnPageChangeListener pageListener= new OnPageChangeListener(){

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageSelected(int position) {
			// TODO Auto-generated method stub
			mViewPager.setCurrentItem(position);
			selectTab(position);
		}
	};
	
}

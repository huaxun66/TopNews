package com.huaxun.life.fragment;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huaxun.R;
import com.huaxun.base.BaseFragment;

public class BlogListFragment extends BaseFragment implements OnClickListener {
	private View view;
	private Context mContext;
	private ViewPagerAdapter pagerAdapter;
	private ArrayList<View> views = new ArrayList<View>();
	private ViewPager mViewPager;
	private TextView newsBlog;
	private TextView hotsBlog;
	
	private FreshBlogLayout mFreshBlogLayout;
	private HotsBlogLayout mHotsBlogLayout;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.mContext = this.getActivity();
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_blogshare, container, false);
		initView();
		return view;
	}
	
	/**
	 * 初始化控件
	 */
	private void initView() {
		LinearLayout linear = (LinearLayout) view.findViewById(R.id.head_filter);
		newsBlog = (TextView) linear.findViewById(R.id.news_blog);
		hotsBlog = (TextView) linear.findViewById(R.id.hots_blog);
		newsBlog.setOnClickListener(this);
		hotsBlog.setOnClickListener(this);
		mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
		
		initFilter();
		
		mFreshBlogLayout = new FreshBlogLayout(this);
		mHotsBlogLayout = new HotsBlogLayout(this);

		views.add(mFreshBlogLayout);
		views.add(mHotsBlogLayout);
		pagerAdapter = new ViewPagerAdapter();
		mViewPager.setAdapter(pagerAdapter);
		
		mFreshBlogLayout.loadFreshBlogInfo(true);
		mHotsBlogLayout.loadHotsBlogInfo(true);		
		
	}
	
	/**
	 * 初始化导航过滤器
	 */
	private void initFilter(){
		newsBlog.setTextColor(mContext.getResources().getColor(R.color.white));
		newsBlog.setBackgroundResource(R.drawable.nofinish_filter_pressed_bg);
		hotsBlog.setTextColor(Color.parseColor("#FF545A"));
		hotsBlog.setBackgroundResource(R.drawable.finished_filter_normal_bg);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.news_blog:
			handlerFilter(0);
			mViewPager.setCurrentItem(0);
			break;
		case R.id.hots_blog:
			handlerFilter(1);
			mViewPager.setCurrentItem(1);
			break;
		default:
			break;
		}
	}
	
	/**
	 * 重置过滤器
	 */
	@SuppressWarnings("unused")
	private void resetTab(){
		newsBlog.setTextColor(Color.parseColor("#FF545A"));
		newsBlog.setBackgroundResource(R.drawable.finished_filter_normal_bg);
		hotsBlog.setTextColor(Color.parseColor("#FF545A"));
		hotsBlog.setBackgroundResource(R.drawable.finished_filter_normal_bg);
	}
	
	/**
	 * 处理选中的导航过滤器
	 * @param position
	 */
	private void handlerFilter(int position){
		switch (position) {
		case 0:
			newsBlog.setTextColor(mContext.getResources().getColor(R.color.white));
			newsBlog.setBackgroundResource(R.drawable.nofinish_filter_pressed_bg);
			hotsBlog.setTextColor(Color.parseColor("#FF545A"));
			hotsBlog.setBackgroundResource(R.drawable.finished_filter_normal_bg);
			break;
		case 1:
			newsBlog.setTextColor(Color.parseColor("#FF545A"));
			newsBlog.setBackgroundResource(R.drawable.nofinish_filter_normal_bg);
			hotsBlog.setTextColor(mContext.getResources().getColor(R.color.white));
			hotsBlog.setBackgroundResource(R.drawable.finished_filter_pressed_bg);
			break;
		default:
			break;
		}
	}
	
	
	public class ViewPagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return views.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			((ViewPager) container).removeView(views.get(position));
		}

		@Override
		public Object instantiateItem(View container, int position) {
			((ViewPager) container).addView(views.get(position));
			return views.get(position);
		}
	}

}

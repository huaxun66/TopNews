package com.huaxun.news;

import java.util.ArrayList;

import com.huaxun.R;
import com.huaxun.app.AppApplication;
import com.huaxun.news.bean.News;
import com.huaxun.tool.BaseTools;
import com.huaxun.utils.Util;
import com.huaxun.view.NewsDetailViewPager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.GestureDetector.OnGestureListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

@SuppressLint("ClickableViewAccessibility")
public class ImageCycleView extends LinearLayout implements OnGestureListener {

	/**
	 * 上下文
	 */
	private Context mContext;

	/**
	 * 图片轮播视图
	 */
	private NewsDetailViewPager mAdvPager = null;

	/**
	 * 滚动图片视图适配器
	 */
	private ImageCycleAdapter mAdvAdapter;

	/**
	 * 图片轮播指示器控件
	 */
	private ViewGroup mGroup;

	/**
	 * 图片轮播指示器-个图
	 */
	private ImageView mImageView = null;

	/**
	 * 滚动图片指示器-视图列表
	 */
	private ImageView[] mImageViews = null;

	/**
	 * 图片滚动当前图片下标
	 */
	@SuppressWarnings("unused")
	private int mImageIndex = 0;

	/**
	 * 手机密度
	 */
	private float mScale;

	private GestureDetector mGestureDetector;
	
	/**
	 * @param context
	 */
	public ImageCycleView(Context context) {
		super(context);
		mGestureDetector = new GestureDetector(context, this);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public ImageCycleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mGestureDetector = new GestureDetector(context, this);
		mScale = context.getResources().getDisplayMetrics().density;

		View view = LayoutInflater.from(context).inflate(R.layout.ad_cycle_view, this);
		// 设置宽高，宽为屏幕的宽度，高为宽的一半
		RelativeLayout mrl = (RelativeLayout) view.findViewById(R.id.rl_cycle);
		android.view.ViewGroup.LayoutParams params = mrl.getLayoutParams();
		params.width = AppApplication.mWidth;
		params.height = params.width / 2 + dip2px(45f);
		mrl.setLayoutParams(params);

		mAdvPager = (NewsDetailViewPager) findViewById(R.id.adv_pager);
		mAdvPager.setOnPageChangeListener(new GuidePageChangeListener());
		mGroup = (ViewGroup) findViewById(R.id.viewGroup);
	}

	public int dip2px(float dipValue) {
		final float scale = getResources().getDisplayMetrics().densityDpi;
		return (int) (dipValue * (scale / 160) + 0.5f);
	}

	/**
	 * 装填图片数据
	 */
	public void setImageResources(ArrayList<News> newsList, boolean showPopupBtn, ImageCycleViewListener imageCycleViewListener) {
		// 清除所有子视图
		mGroup.removeAllViews();
		// 图片广告数量
		final int imageCount = newsList.size();
		mImageViews = new ImageView[imageCount];
		for (int i = 0; i < imageCount; i++) {
			mImageView = new ImageView(mContext);
			LayoutParams params = new LayoutParams(Util.dip2px(mContext, 8), Util.dip2px(mContext, 8));
			params.setMargins(Util.dip2px(mContext, 3), 0, Util.dip2px(mContext, 3), 0);
			mImageView.setLayoutParams(params);
			mImageViews[i] = mImageView;
			if (i == 0) {
				mImageViews[i].setBackgroundResource(R.drawable.banner_dian_focus);
			} else {
				mImageViews[i].setBackgroundResource(R.drawable.banner_dian_blur);
			}
			mGroup.addView(mImageViews[i]);
		}
		mAdvAdapter = new ImageCycleAdapter(mContext, newsList, showPopupBtn, imageCycleViewListener);
		mAdvPager.setAdapter(mAdvAdapter);
		if (mImageViews.length <= 1) {
			mAdvPager.setCurrentItem(0);
			mGroup.setVisibility(View.INVISIBLE);
		} else {
			mAdvPager.setCurrentItem(mImageViews.length * 100); //设置ViewPager的默认项, 设置为长度的100倍，这样子开始就能往右滑动
		}
	}

	/**
	 * 轮播图片状态监听器
	 */
	private final class GuidePageChangeListener implements OnPageChangeListener {
		@Override
		public void onPageScrollStateChanged(int state) {
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageSelected(int index) {
			int selectItem = index % mImageViews.length;
			for (int i = 0; i < mImageViews.length; i++) {
				if (i == selectItem) {
					mImageViews[i].setBackgroundResource(R.drawable.banner_dian_focus);
				} else {
					mImageViews[i].setBackgroundResource(R.drawable.banner_dian_blur);
				}
			}
		}
	}

	@SuppressLint("InflateParams")
	private class ImageCycleAdapter extends PagerAdapter {
		private LayoutInflater mInflater;

		/**
		 * 图片资源列表
		 */
		public ArrayList<News> mAdList = new ArrayList<News>();

		/**
		 * 广告图片点击监听器
		 */
		private ImageCycleViewListener mImageCycleViewListener;
		private boolean showPopupBtn;
		

		public ImageCycleAdapter(Context context, ArrayList<News> adList, boolean showPopupBtn, ImageCycleViewListener imageCycleViewListener) {
			mAdList = adList;
			mImageCycleViewListener = imageCycleViewListener;
			mInflater = LayoutInflater.from(context);
			this.showPopupBtn = showPopupBtn;
		}

		@Override
		public int getCount() {
			if (mImageViews.length <= 1) {
				return 1;
			} else {
				return Integer.MAX_VALUE;
			}
		}

		@Override
		public boolean isViewFromObject(View view, Object obj) {
			return view == obj;
		}

		@Override
		public Object instantiateItem(ViewGroup container, final int position) {
			News news = mAdList.get(position % mAdList.size());
			String imageURL = Util.getEastDayURL(news.getImgurl1());
			View view = mInflater.inflate(R.layout.image_cycle_item, null);
			ImageView imageView = (ImageView) view.findViewById(R.id.iv_cycle_icon);
			ImageView iv_play = (ImageView) view.findViewById(R.id.iv_cycle_play);
			ImageView popicon = (ImageView) view.findViewById(R.id.popicon);	
			TextView textview = (TextView) view.findViewById(R.id.tv_cycle_title);
			iv_play.setVisibility(null != news && news.getNewstype().equals("7") ? View.VISIBLE : View.GONE);
			popicon.setVisibility(showPopupBtn ? View.VISIBLE : View.GONE);
			textview.setText(news.getNewstitle());
			mImageCycleViewListener.displayImage(imageURL, imageView);
			imageView.setTag(news);
			popicon.setTag(news);
			//设置图片点击监听
			imageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mImageCycleViewListener.onImageClick(v);
				}
			});
			//设置popup点击监听
			popicon.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mImageCycleViewListener.onPopupClick(v);
				}
			});
			container.addView(view);
			return view;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
//          ((ViewPager)container).removeView(mImageViews[position % mImageViews.length]);  
		}
	}

	/**
	 * 轮播控件的监听事件
	 */
	public static interface ImageCycleViewListener {
		/**
		 * 加载图片资源
		 */
		public void displayImage(String imageURL, ImageView imageView);

		/**
		 * 单击图片事件
		 */
		public void onImageClick(View view);
		/**
		 * 单击popup事件
		 */
		public void onPopupClick(View view);
	}
	
	//以下代码处理手势
	 @Override
	 public boolean dispatchTouchEvent(MotionEvent ev) {
		mGestureDetector.onTouchEvent(ev);
	    return super.dispatchTouchEvent(ev);
	 }

	@Override
	public boolean onDown(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent arg0) {
		// TODO Auto-generated method stub		
	}

	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float distanceX, float distanceY) {
		BaseTools.showlog("onScroll");
		if (Math.abs(distanceY)>Math.abs(distanceX)) {
			getParent().requestDisallowInterceptTouchEvent(false);
			return true;
		} else {
			getParent().requestDisallowInterceptTouchEvent(true);
			return false;
		}		
	}

	@Override
	public void onShowPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

}

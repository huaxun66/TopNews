package com.huaxun.menuRight;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.huaxun.R;
import com.huaxun.app.AppApplication;
import com.huaxun.menuLeft.Images;
import com.huaxun.utils.Util;
import com.huaxun.view.NewsDetailViewPager;
import com.nostra13.universalimageloader.core.ImageLoader;

@SuppressLint("ClickableViewAccessibility")
public class ImageCycleView extends LinearLayout {

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

	/**
	 * @param context
	 */
	public ImageCycleView(Context context) {
		super(context);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public ImageCycleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mScale = context.getResources().getDisplayMetrics().density;

		View view = LayoutInflater.from(context).inflate(R.layout.ad_cycle_view_2, this);
		// 设置宽高，宽为屏幕的宽度，高为宽的一半
//		RelativeLayout mrl = (RelativeLayout) view.findViewById(R.id.rl_cycle);
//		android.view.ViewGroup.LayoutParams params = mrl.getLayoutParams();
//		params.width = AppApplication.mWidth;
//		params.height = params.width / 2 + dip2px(45f);
//		mrl.setLayoutParams(params);

		mAdvPager = (NewsDetailViewPager) findViewById(R.id.adv_pager);
		mAdvPager.setOnPageChangeListener(new GuidePageChangeListener());
		// 滚动图片右下指示器视图
		mGroup = (ViewGroup) findViewById(R.id.viewGroup);
	}

	public int dip2px(float dipValue) {
		final float scale = getResources().getDisplayMetrics().densityDpi;
		return (int) (dipValue * (scale / 160) + 0.5f);
	}

	/**
	 * 装填图片数据
	 */
	public void setImageResources(List<String> urlList, ImageCycleViewListener imageCycleViewListener) {
		// 清除所有子视图
		mGroup.removeAllViews();
		// 图片广告数量
		final int imageCount = urlList.size();
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
		mAdvAdapter = new ImageCycleAdapter(mContext, urlList, imageCycleViewListener);
		mAdvPager.setAdapter(mAdvAdapter);
		if (mImageViews.length <= 1) {
			mAdvPager.setCurrentItem(0);
			mGroup.setVisibility(View.INVISIBLE);
		} else {
			mAdvPager.setCurrentItem(mImageViews.length * 100);
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
		public List<String> urlList = new ArrayList<String>();

		/**
		 * 广告图片点击监听器
		 */
		private ImageCycleViewListener mImageCycleViewListener;

		public ImageCycleAdapter(Context context, List<String> urlList, ImageCycleViewListener imageCycleViewListener) {
			this.urlList = urlList;
			mImageCycleViewListener = imageCycleViewListener;
			mInflater = LayoutInflater.from(context);
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
			String imageURL = urlList.get(position % urlList.size());
			View view = mInflater.inflate(R.layout.image_cycle_item_2, null);
			ImageView imageView = (ImageView) view.findViewById(R.id.iv_cycle_icon);
			mImageCycleViewListener.displayImage(imageURL, imageView);
			view.setTag(imageURL);
			// 设置图片点击监听
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mImageCycleViewListener.onImageClick(position, v);
				}
			});
			container.addView(view);
			return view;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			// View view = (View) object;
			// container.removeView(view);
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
		public void onImageClick(int position, View view);
	}

}

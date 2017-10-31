  package com.huaxun.news.adapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.huaxun.R;
import com.huaxun.app.AppApplication;
import com.huaxun.db.DataDB;
import com.huaxun.db.FavoriteDB;
import com.huaxun.news.ImageCycleView;
import com.huaxun.news.ImageCycleView.ImageCycleViewListener;
import com.huaxun.news.activity.PicBrowserActivity;
import com.huaxun.news.activity.WebActivity;
import com.huaxun.news.bean.News;
import com.huaxun.news.service.SpeechSynthesizerService;
import com.huaxun.tool.Constants;
import com.huaxun.tool.Options;
import com.huaxun.tool.VolleyTool;
import com.huaxun.utils.FileUtil;
import com.huaxun.utils.Util;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class NewsListAdapter extends BaseAdapter {
	private Context context;
	private String nodename;
	private ArrayList<Object> newsList = new ArrayList<Object>();
	private LayoutInflater inflater = null;
	public static int mBigAdd;
	public static int mBigPicWidth;
	public static int mZTWidth, mZTHeight;
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options;	
	private ImageLoaderConfiguration config;
	private ImageLoadingListener animateFirstListener;
	private DataDB dataDB;
	private FavoriteDB favoriteDB;
	private Intent playNewsIntent;
	
	public static final int TYPE_CYCLE = 0;// 顶部滑动banner样式
	public static final int TYPE_NORMAL = 1;// 普通列表样式
	public static final int TYPE_SMALL = 2;// 三张小图样式
	public static final int TYPE_BIG = 3;// 一张大图样式
	public static final int TYPE_AD = 4;// 广告样式
	public static final int TYPE_ZTTJ = 5;// 专题图集样式
	
	public NewsListAdapter(Activity activity,String nodename) {
		this.context = activity;
		this.nodename = nodename;
		dataDB = DataDB.getInstance(context);
		favoriteDB = FavoriteDB.getInstance();
		playNewsIntent = new Intent(context, SpeechSynthesizerService.class);
		inflater = LayoutInflater.from(activity);
		options = Options.getListOptions();
		mBigAdd = Util.dip2px(context, 2);
		mBigPicWidth = AppApplication.mWidth - Util.dip2px(context, 35);
		mZTWidth = (AppApplication.mWidth - Util.dip2px(context, 50)) / 2;
		mZTHeight = (int) (mZTWidth / 1.5);
		
//		config = new ImageLoaderConfiguration.Builder(
//				context)
//				.threadPoolSize(3)
//				.threadPriority(Thread.NORM_PRIORITY - 2)
//				.denyCacheImageMultipleSizesInMemory()
//				.tasksProcessingOrder(QueueProcessingType.LIFO)
//				.denyCacheImageMultipleSizesInMemory()
//				.memoryCache(new WeakMemoryCache())
//				.memoryCacheSize((int) (2 * 1024 * 1024))
//				.memoryCacheSizePercentage(13)
//				.build();
//		ImageLoader.getInstance().init(config);
		
		animateFirstListener = new AnimateFirstDisplayListener();
	}

	
	public void setList(ArrayList<Object> newsList){
		this.newsList = newsList;
	}
	
	public ArrayList<Object> getList(){
		return newsList;
	}
	
	@Override
	public int getCount() {
		return newsList == null ? 0 : newsList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if (newsList != null && newsList.size() != 0) {
			return newsList.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	
	@Override
	public int getViewTypeCount() {
		return 6;
	}
	
	public int getItemViewType(int position) {
		Object obj = newsList.get(position);
		int type = -1;
		if (obj instanceof ArrayList) {
			type = TYPE_CYCLE;
		} else {
			News news = (News) obj;
			if (news.getStyle() == null){
				type = TYPE_SMALL;
			} else {
				type = Integer.parseInt(news.getStyle());
			}
			if (type == 0) {
				type = TYPE_NORMAL;
			}
		}	
		return type;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {	
		Object mObj = newsList.get(position);
		int type = getItemViewType(position);
		switch (type) {
		case TYPE_CYCLE:		
			@SuppressWarnings("unchecked")
			ArrayList<News> newsList = (ArrayList<News>) mObj;
			convertView = getCycleView(convertView, newsList, position);
			break;
		case TYPE_NORMAL:
			News news_normal = (News) mObj;
			convertView = getNormalView(convertView, news_normal, position);
			break;
		case TYPE_SMALL:
			News news_small = (News) mObj;
			convertView = getSmallView(convertView, news_small, position);
			break;
		case TYPE_BIG:
			News news_big = (News) mObj;
			convertView = getBigView(convertView, news_big, position);
			break;
		case TYPE_AD:
			News news_ad = (News) mObj;
			convertView = getAdView(convertView, news_ad, position);
			break;	
		case TYPE_ZTTJ:
			News news_zttj = (News) mObj;
			convertView = getZTTJView(convertView, news_zttj, position);
			break;
		}		
		return convertView;
	}	

	
	public View getCycleView(View convertView, final ArrayList<News> newsList, int position) {
		HolderCycle holder_cycle = null;
		if (null != convertView) {
			holder_cycle = (HolderCycle) convertView.getTag();
		} else {
			convertView = inflater.inflate(R.layout.news_list_item_01, null);
			holder_cycle = new HolderCycle(convertView);
			convertView.setTag(holder_cycle);
		}
		holder_cycle.newsList = newsList;
		holder_cycle.index = position;
		holder_cycle.mImageCycleView.setImageResources(newsList, true, new ImageCycleViewListener() {
			public void onImageClick(View imageView) {
				News news = (News) imageView.getTag();
				if (news.getNewstype().equals("0")){
					news.parentNodeName = nodename;
					news.isshare = "true";
					Intent intent = new Intent(context,WebActivity.class);
					Bundle bundle = new Bundle();
					bundle.putSerializable("NEWS", news);
					intent.putExtras(bundle);
					context.startActivity(intent);
					((Activity) context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				}else if (news.getNewstype().equals("5")){
					Intent intent = new Intent(context,PicBrowserActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("url", Util.getEastDayURL(news.getNewsurl()));
					intent.putExtras(bundle);
					context.startActivity(intent);
				}
			}
			
			public void onPopupClick(View imageView) {
				News news = (News) imageView.getTag();
				initPopWindow(news);
				int[] arrayOfInt = new int[2];
				imageView.getLocationOnScreen(arrayOfInt);
		        int x = arrayOfInt[0];
		        int y = arrayOfInt[1];
		        showPop(imageView, x , y, news);
			}

			public void displayImage(final String imageURL, final ImageView imageView) {				
//				ImageLoader.getInstance().displayImage(imageURL, imageView, options, animateFirstListener);

				com.android.volley.toolbox.ImageLoader imageLoader = VolleyTool.getInstance(context).getmImageLoader();		
				ImageListener listener = com.android.volley.toolbox.ImageLoader.getImageListener(imageView, R.drawable.small_image_holder_listpage, R.drawable.small_image_holder_listpage);
				imageLoader.get(imageURL, listener);
			}
		});
		return convertView;
	}
	
	public View getNormalView(View convertView, News news, int position) {
		HolderNormal holder_normal = null;
		if (null != convertView) {
			holder_normal = (HolderNormal) convertView.getTag();
		} else {
			convertView = inflater.inflate(R.layout.news_list_item_02, null);
			holder_normal = new HolderNormal(convertView);
			convertView.setTag(holder_normal);
		}
		
		int resId = Util.getIconTypeRes(news.getIcontype());
		if (resId == -1) {
			holder_normal.normal_mark.setVisibility(View.INVISIBLE);
		} else {
			holder_normal.normal_mark.setVisibility(View.VISIBLE);
			holder_normal.normal_mark.setImageResource(resId);
		}
		
		if (FileUtil.isNewsFileExist(news.getNewstitle())){
			holder_normal.local_tv.setVisibility(View.VISIBLE);
		}else{
			holder_normal.local_tv.setVisibility(View.INVISIBLE);
		}
		holder_normal.index = position;
		holder_normal.news = news;
		holder_normal.normal_title.setText(news.getNewstitle() + " ");
		if (dataDB.queryIdCache(news.getNewsid())) {
			// 看过此条新闻，字体为灰色
			holder_normal.normal_title.setTextColor(context.getResources().getColor(R.color.news_read_ok));
		}
		
		holder_normal.normal_time.setText(getCreateTime(news.getCreatetime()));

//		ImageLoader.getInstance().displayImage(Util.getEastDayURL(news.imgurl1), holder_normal.normal_iv, options, animateFirstListener);		
		com.android.volley.toolbox.ImageLoader imageLoader = VolleyTool.getInstance(context).getmImageLoader();		
		ImageListener listener = com.android.volley.toolbox.ImageLoader.getImageListener(holder_normal.normal_iv, R.drawable.small_image_holder_listpage, R.drawable.small_image_holder_listpage);
		imageLoader.get(Util.getEastDayURL(news.getImgurl1()), listener);
		//设置+按钮点击效果
		holder_normal.popicon.setVisibility(View.VISIBLE);
		holder_normal.popicon.setOnClickListener(new popAction(news));
		return convertView;
	}

	
	public View getSmallView(View convertView, News news, int position) {
		HolderSmall holder_small = null;
		if (null != convertView) {
			holder_small = (HolderSmall) convertView.getTag();
		} else {
			convertView = inflater.inflate(R.layout.news_list_item_03, null);
			holder_small = new HolderSmall(convertView);
			convertView.setTag(holder_small);
		}		
		
		holder_small.index = position;
		holder_small.news = news;
		holder_small.small_title.setText(news.getNewstitle() + "");
		if (dataDB.queryIdCache(news.getNewsid())) {
			// 看过此条新闻，字体为灰色
			holder_small.small_title.setTextColor(context.getResources().getColor(R.color.news_read_ok));
		}
		String imageURL1 = Util.getEastDayURL(news.getImgurl1());
		String imageURL2 = Util.getEastDayURL(news.getImgurl2());
		String imageURL3 = Util.getEastDayURL(news.getImgurl3());
//		ImageLoader.getInstance().displayImage(imageURL1, holder_small.small_iv01, options, animateFirstListener);		
//		ImageLoader.getInstance().displayImage(imageURL2, holder_small.small_iv02, options, animateFirstListener);		
//		ImageLoader.getInstance().displayImage(imageURL3, holder_small.small_iv03, options, animateFirstListener);		

		com.android.volley.toolbox.ImageLoader imageLoader = VolleyTool.getInstance(context).getmImageLoader();		
		ImageListener listener1 = com.android.volley.toolbox.ImageLoader.getImageListener(holder_small.small_iv01, R.drawable.small_image_holder_listpage, R.drawable.small_image_holder_listpage);
		imageLoader.get(imageURL1, listener1);
		ImageListener listener2 = com.android.volley.toolbox.ImageLoader.getImageListener(holder_small.small_iv02, R.drawable.small_image_holder_listpage, R.drawable.small_image_holder_listpage);
		imageLoader.get(imageURL2, listener2);
		ImageListener listener3 = com.android.volley.toolbox.ImageLoader.getImageListener(holder_small.small_iv03, R.drawable.small_image_holder_listpage, R.drawable.small_image_holder_listpage);
		imageLoader.get(imageURL3, listener3);
		
		int resId = Util.getIconTypeRes(news.getIcontype());
		if (resId == -1) {
			holder_small.small_mark.setVisibility(View.INVISIBLE);
		} else {
			holder_small.small_mark.setVisibility(View.VISIBLE);
			holder_small.small_mark.setImageResource(resId);
		}
		holder_small.small_time.setText(getCreateTime(news.getCreatetime()));
		//设置+按钮点击效果
		holder_small.popicon.setVisibility(View.VISIBLE);
		holder_small.popicon.setOnClickListener(new popAction(news));
		return convertView;
	}
	
	public View getBigView(View convertView, News news, int position) {
		HolderBig holder_big = null;
		if (null != convertView) {
			holder_big = (HolderBig) convertView.getTag();
		} else {
			convertView = inflater.inflate(R.layout.news_list_item_04, null);
			holder_big = new HolderBig(convertView);
			convertView.setTag(holder_big);
		}
		holder_big.index = position;
		holder_big.news = news;
		holder_big.big_title.setText(news.getNewstitle() + "");
		if (dataDB.queryIdCache(news.getNewsid())) {
			// 看过此条新闻，字体为灰色
			holder_big.big_title.setTextColor(context.getResources().getColor(R.color.news_read_ok));
		}
//		ImageLoader.getInstance().displayImage(Util.getEastDayURL(news.imgurl1), holder_big.big_iv, options, animateFirstListener);	

		com.android.volley.toolbox.ImageLoader imageLoader = VolleyTool.getInstance(context).getmImageLoader();		
		ImageListener listener = com.android.volley.toolbox.ImageLoader.getImageListener(holder_big.big_iv, R.drawable.small_image_holder_listpage, R.drawable.small_image_holder_listpage);
		imageLoader.get(Util.getEastDayURL(news.getImgurl1()), listener);
		
		int resId = Util.getIconTypeRes(news.getIcontype());
		if (resId == -1) {
			holder_big.big_mark.setVisibility(View.INVISIBLE);
		} else {
			holder_big.big_mark.setVisibility(View.VISIBLE);
			holder_big.big_mark.setImageResource(resId);
		}
		holder_big.big_time.setText(getCreateTime(news.getCreatetime()));
		//设置+按钮点击效果
		holder_big.popicon.setVisibility(View.VISIBLE);
		holder_big.popicon.setOnClickListener(new popAction(news));
		return convertView;
	}
	
	// 广告类型：一张长而窄的图片
	  public View getAdView(View convertView, News news, int position) {
		 HolderAd holder_ad = null;
		 if (null != convertView) {
			holder_ad = (HolderAd) convertView.getTag();
		 } else {
			convertView = inflater.inflate(R.layout.news_list_item_05, null);
			holder_ad = new HolderAd(convertView);
			convertView.setTag(holder_ad);
		 }
			holder_ad.index = position;
			holder_ad.news = news;
			// 加载图片
			ImageLoader.getInstance().displayImage(Util.getEastDayURL(news.getImgurl1()), holder_ad.ad_iv, options,
					animateFirstListener);
			return convertView;
			}
	  
		// 专题图集样式，两张图片放一行
		public View getZTTJView(View convertView, News news, int position) {
			HolderZTTJ holder_zttj = null;
			if (null != convertView) {
				holder_zttj = (HolderZTTJ) convertView.getTag();
			} else {
				convertView = inflater.inflate(R.layout.news_list_item_06, null);
				holder_zttj = new HolderZTTJ(convertView);
				convertView.setTag(holder_zttj);
			}
			holder_zttj.index = position;
			holder_zttj.news = news;
			News news2 = news.nextNews;
			// 设置标题及标题颜色
			holder_zttj.tv_zttj_01.setText(news.getNewstitle() + "");
			// 加载图片
			ImageLoader.getInstance().displayImage(Util.getEastDayURL(news.getImgurl1()), holder_zttj.iv_zttj_01,
					options, animateFirstListener);
			holder_zttj.ll_zttj_01.setTag(news);
			//holder_zttj.ll_zttj_01.setOnClickListener(listener);

			if (null == news2) {
				holder_zttj.ll_zttj_02.setVisibility(View.GONE);
			} else {
				holder_zttj.ll_zttj_02.setTag(news2);
				//holder_zttj.ll_zttj_02.setOnClickListener(listener);

				holder_zttj.ll_zttj_02.setVisibility(View.VISIBLE);
				holder_zttj.tv_zttj_02.setText(news2.getNewstitle() + "");
				// 加载图片
				ImageLoader.getInstance().displayImage(Util.getEastDayURL(news2.getImgurl1()), holder_zttj.iv_zttj_02,
						options, animateFirstListener);
			}
			return convertView;
		}	 
		
		
	public static class HolderCycle {
		public LinearLayout cycle_view;
		public ImageCycleView mImageCycleView;
		public View mLineView;
		public ImageView popicon;
		public int index;
		public ArrayList<News> newsList;

		public HolderCycle(View view) {
			mImageCycleView = (ImageCycleView) view.findViewById(R.id.news_list_icv);
			mLineView = view.findViewById(R.id.news_list_view_line);
			popicon = (ImageView) view.findViewById(R.id.popicon);
			newsList = new ArrayList<News>();
			index = -1;
		}
	}

	public static class HolderNormal {
		public News news;
		public ImageView normal_iv;
		public TextView normal_title;
		public TextView normal_time;
		public ImageView normal_mark;
		public TextView local_tv;
		public ImageView popicon;
		public int index;

		public HolderNormal(View view) {
			normal_iv = (ImageView) view.findViewById(R.id.normal_iv);
			normal_title = (TextView) view.findViewById(R.id.normal_tv_title);
			normal_time = (TextView) view.findViewById(R.id.normal_tv_time);
			normal_mark = (ImageView) view.findViewById(R.id.normal_mark);
			local_tv = (TextView) view.findViewById(R.id.localTextViewID);
			popicon = (ImageView) view.findViewById(R.id.popicon);
			index = -1;
		}
	}	
	
	public static class HolderSmall {
		public News news;
		// 3:小图
		public TextView small_title;
		public TextView small_time;
		public ImageView small_mark;
		public ImageView small_iv01;
		public ImageView small_iv02;
		public ImageView small_iv03;
		public ImageView popicon;
		public int index;

		public HolderSmall(View view) {
			// small
			small_title = (TextView) view.findViewById(R.id.small_image_tv_title);
			small_iv01 = (ImageView) view.findViewById(R.id.small_image_iv01);
			small_iv02 = (ImageView) view.findViewById(R.id.small_image_iv02);
			small_iv03 = (ImageView) view.findViewById(R.id.small_image_iv03);
			small_time = (TextView) view.findViewById(R.id.small_tv_time);
			small_mark = (ImageView) view.findViewById(R.id.small_mark);
			popicon = (ImageView) view.findViewById(R.id.popicon);
			index = -1;
		}
	}
	
	
	public static class HolderBig {
		public News news;
		// 4:大图
		public RelativeLayout big_image_rl;
		public TextView big_title;
		public ImageView big_iv;
		public TextView big_time;
		public ImageView big_mark;
		public ImageView popicon;
		public int index;

		public HolderBig(View view) {
			// big
			big_image_rl = (RelativeLayout) view.findViewById(R.id.big_image_rl);
			big_title = (TextView) view.findViewById(R.id.big_image_tv_title);
			big_iv = (ImageView) view.findViewById(R.id.big_image_iv);
			big_time = (TextView) view.findViewById(R.id.big_tv_time);
			big_mark = (ImageView) view.findViewById(R.id.big_mark);
			popicon = (ImageView) view.findViewById(R.id.popicon);
			LinearLayout.LayoutParams blp = (LinearLayout.LayoutParams) big_image_rl.getLayoutParams();
			blp.width = AppApplication.mWidth;
			blp.height = blp.width / 2 + mBigAdd;
			big_image_rl.setLayoutParams(blp);

			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) big_iv.getLayoutParams();
			lp.width = mBigPicWidth;
			lp.height = lp.width / 2;
			big_iv.setLayoutParams(lp);

			index = -1;
		}
	}
	
	public static class HolderAd {
		public News news;
		// 5:广告图片
		public ImageView ad_iv;
		public int index;

		public HolderAd(View view) {
			// 广告图片
			ad_iv = (ImageView) view.findViewById(R.id.ad_image_iv);
			index = -1;
		}
	}
	
	public static class HolderZTTJ {
		public News news;
		// 左边
		public LinearLayout ll_zttj_01;
		public ImageView iv_zttj_01;
		public TextView tv_zttj_01;

		// 右边
		public LinearLayout ll_zttj_02;
		public ImageView iv_zttj_02;
		public TextView tv_zttj_02;

		public int index;

		public HolderZTTJ(View view) {
			ll_zttj_01 = (LinearLayout) view.findViewById(R.id.ll_zttj_01);
			iv_zttj_01 = (ImageView) view.findViewById(R.id.iv_zttj_01);
			tv_zttj_01 = (TextView) view.findViewById(R.id.tv_zttj_01);
			LinearLayout.LayoutParams lpOne = (LinearLayout.LayoutParams) iv_zttj_01.getLayoutParams();
			lpOne.width = mZTWidth;
			lpOne.height = mZTHeight;
			iv_zttj_01.setLayoutParams(lpOne);

			ll_zttj_02 = (LinearLayout) view.findViewById(R.id.ll_zttj_02);
			iv_zttj_02 = (ImageView) view.findViewById(R.id.iv_zttj_02);
			tv_zttj_02 = (TextView) view.findViewById(R.id.tv_zttj_02);
			LinearLayout.LayoutParams lpTwo = (LinearLayout.LayoutParams) iv_zttj_02.getLayoutParams();
			lpTwo.width = mZTWidth;
			lpTwo.height = mZTHeight;
			iv_zttj_02.setLayoutParams(lpTwo);

			index = -1;
		}
	}
	
	private String getCreateTime(String time){
		String createTime = "";
		String month = "";
		String day = "";
		if (time == null)
			return createTime;
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date d1 = df.parse(time);
			Date nowDate = new Date(System.currentTimeMillis());
			long diff = nowDate.getTime() - d1.getTime();

			long days = diff / (1000 * 60 * 60 * 24);
			long hours = (diff - days * (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
			long minutes = (diff - days * (1000 * 60 * 60 * 24) - hours * (1000 * 60 * 60)) / (1000 * 60);
			
			if (days == 0){
				if (hours == 0){
					createTime = minutes + "分钟前";
				}else{
					createTime = hours + "小时前";
				}
			}else{
				if (time.substring(5, 6).equals("0")) {
					month = time.substring(6, 7);
				} else {
					month = time.substring(5, 7);
				}
				if (time.substring(8, 9).equals("0")) {
					day = time.substring(9, 10);
				} else {
					day = time.substring(8, 10);
				}
				createTime = month + "月" + day +"日";
			}
		} catch (Exception e) {}
		return createTime;
	}
	
	public int getAltMarkResID(int mark,boolean isfavor){
		if(isfavor){
			return R.drawable.ic_mark_favor;
		}
		switch (mark) {
		case Constants.mark_recom:
			return R.drawable.ic_mark_recommend;
		case Constants.mark_hot:
			return R.drawable.ic_mark_hot;
		case Constants.mark_frist:
			return R.drawable.ic_mark_first;
		case Constants.mark_exclusive:
			return R.drawable.ic_mark_exclusive;
		case Constants.mark_favor:
			return R.drawable.ic_mark_favor;
		default:
			break;
		}
		return -1;
	}
	
	private PopupWindow popupWindow;
	private View popView;
	private ImageView btn_pop_close;
	private TextView tv_pop_speech;
	private TextView tv_pop_favor;
	private TextView tv_pop_dislike;

	private void initPopWindow(News news) {
		if (popupWindow == null) {
			popView = inflater.inflate(R.layout.list_item_pop, null);
			popupWindow = new PopupWindow(popView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			popupWindow.setBackgroundDrawable(new ColorDrawable(0));
			popupWindow.setAnimationStyle(R.style.PopMenuAnimation);
			btn_pop_close = (ImageView) popView.findViewById(R.id.btn_pop_close);
			tv_pop_speech = (TextView) popView.findViewById(R.id.tv_pop_speech);
			tv_pop_favor = (TextView) popView.findViewById(R.id.tv_pop_favor);
			tv_pop_dislike = (TextView) popView.findViewById(R.id.tv_pop_dislike);
		}
		if (SpeechSynthesizerService.isNowPlaying) {
			if (news.getNewstitle().equals(SpeechSynthesizerService.content)) {
				tv_pop_speech.setText("暂停播放");
			} else {
			    tv_pop_speech.setText("开始播放");
			}
		} else {
			if (news.getNewstitle().equals(SpeechSynthesizerService.content)) {
			    tv_pop_speech.setText("重新播放");
			} else {
				tv_pop_speech.setText("开始播放");
			}
		}
		if (favoriteDB.isNewsFavorite(news)) {			
			Drawable drawable = context.getResources().getDrawable(R.drawable.listpage_more_like_seleted_normal);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
			tv_pop_favor.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
			tv_pop_favor.setText("取消收藏");
		} else {
			Drawable drawable = context.getResources().getDrawable(R.drawable.listpage_more_like_normal);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
			tv_pop_favor.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
			tv_pop_favor.setText("加入收藏");
		}
	}
	

	public void showPop(View parent, int x, int y, final News news) {
		popupWindow.showAtLocation(parent, 0, x, y);
		popupWindow.setFocusable(true);
		popupWindow.setOutsideTouchable(true);
		popupWindow.update();
		btn_pop_close.setOnClickListener(new OnClickListener() {
			public void onClick(View paramView) {
				popupWindow.dismiss();
			}
		});
		tv_pop_speech.setOnClickListener(new OnClickListener() {
			public void onClick(View paramView) {
				Bundle bundle  = new Bundle();
				bundle.putString("content", news.getNewstitle());				
				if (SpeechSynthesizerService.isNowPlaying) {
					if (news.getNewstitle().equals(SpeechSynthesizerService.content)) {
						bundle.putInt("action", SpeechSynthesizerService.PAUSE);
						playNewsIntent.putExtras(bundle);
						context.startService(playNewsIntent);
					    Util.showToast(context, "暂停播放");
					} else {
						bundle.putInt("action", SpeechSynthesizerService.PLAY);
						playNewsIntent.putExtras(bundle);
						context.startService(playNewsIntent);
					    Util.showToast(context, "开始播放");
					}
				} else {
					if (news.getNewstitle().equals(SpeechSynthesizerService.content)) {
						bundle.putInt("action", SpeechSynthesizerService.REPLAY);
						playNewsIntent.putExtras(bundle);
						context.startService(playNewsIntent);
					    Util.showToast(context, "重新播放");
					} else {
						bundle.putInt("action", SpeechSynthesizerService.PLAY);
						playNewsIntent.putExtras(bundle);
						context.startService(playNewsIntent);
					    Util.showToast(context, "开始播放");
					}
				}
				popupWindow.dismiss();
			}
		});
		tv_pop_favor.setOnClickListener(new OnClickListener() {
			public void onClick(View paramView) {
				if (!favoriteDB.isNewsFavorite(news)) {
					favoriteDB.saveNews(news);
					Util.showToast(context, "收藏成功");
				} else {
					favoriteDB.deleteNews(news);
					Util.showToast(context, "取消成功");
				}
				popupWindow.dismiss();
			}
		});
		tv_pop_dislike.setOnClickListener(new OnClickListener() {
			public void onClick(View paramView) {
				dataDB.addIdCache(news.getNewsid());
				notifyDataSetChanged();
				popupWindow.dismiss();
			}
		});
	}
	

	public class popAction implements OnClickListener{
		News news;
		public popAction(News news){
			this.news = news;
		}
		@Override
		public void onClick(View v) {
			initPopWindow(news);
			int[] arrayOfInt = new int[2];
			v.getLocationOnScreen(arrayOfInt);
	        int x = arrayOfInt[0];
	        int y = arrayOfInt[1];
	        showPop(v, x , y, news);
		}
	}
	
	//渐入动画，第一次加载透明度从0变为1
	class AnimateFirstDisplayListener extends SimpleImageLoadingListener {
		//synchronizedList,线程安全list
		final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());
		@Override
		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					FadeInBitmapDisplayer.animate(imageView, 500);
					displayedImages.add(imageUri);
				}
			}
		}
	}
	
}

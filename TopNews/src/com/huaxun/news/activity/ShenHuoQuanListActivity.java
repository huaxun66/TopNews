package com.huaxun.news.activity;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.huaxun.R;
import com.huaxun.app.AppApplication;
import com.huaxun.db.DataDB;
import com.huaxun.dialog.LoadingDialog;
import com.huaxun.news.ImageCycleView;
import com.huaxun.news.ImageCycleView.ImageCycleViewListener;
import com.huaxun.news.activity.ShenHuoQuanListActivity.NewsListAdapter.HolderCycle;
import com.huaxun.news.bean.News;
import com.huaxun.news.bean.NewsListData;
import com.huaxun.tool.BaseTools;
import com.huaxun.tool.Constants;
import com.huaxun.tool.Options;
import com.huaxun.tool.VolleyTool;
import com.huaxun.utils.FileUtil;
import com.huaxun.utils.Util;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class ShenHuoQuanListActivity extends Activity {
	private ArrayList<Object> newsList = new ArrayList<Object>();
	private PullToRefreshListView mPullToRefreshListView;
	private NewsListAdapter newsListAdapter;
	private DataDB dataDB;
	private String refreshURL = "";
	private String loadMoreURL = "";
	private String nodename = "";
	private ImageView detail_loading;
	private LoadingDialog loadingDialog;
	private TextView topBack,topTitle;
	
	private int pageIndex = 0;
	private boolean isRefresh = true;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Util.setTranslucent(this);
		dataDB = DataDB.getInstance(this);
		Bundle bundle = getIntent().getExtras();
		refreshURL = bundle.getString("refreshURL");
		loadMoreURL = bundle.getString("loadMoreURL");
		nodename = bundle.getString("nodename");
		
		setContentView(R.layout.shenhuoquan_list);		
		topTitle = (TextView)this.findViewById(R.id.topTitle);
		topTitle.setText("申活圈");
		topBack = (TextView)this.findViewById(R.id.topBack);
		topBack.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
				overridePendingTransition(R.anim.scale_out, 0);
			}
		});
		
		mPullToRefreshListView = (PullToRefreshListView) this.findViewById(R.id.pull_to_refresh_listview);
		detail_loading = (ImageView) this.findViewById(R.id.detail_loading);
		newsListAdapter = new NewsListAdapter(this);
		//mPullToRefreshListView.setAdapter(newsListAdapter);
		mPullToRefreshListView.getRefreshableView().setAdapter(newsListAdapter);
		mPullToRefreshListView.setMode(Mode.BOTH);
		//mPullToRefreshListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
		mPullToRefreshListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				Object obj = newsList.get(position-1);
				if (obj instanceof HolderCycle) {
					return;
				}
				News news = (News) obj;
				if (news.getNewstype().equals("0")) {
					news.parentNodeName = "申活圈";
					Intent intent = new Intent(ShenHuoQuanListActivity.this,WebActivity.class);
					Bundle bundle = new Bundle();
					bundle.putSerializable("NEWS", news);				
					intent.putExtras(bundle);
					startActivity(intent);
					overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				} else if (news.getNewstype().equals("5")) {
					Intent intent = new Intent(ShenHuoQuanListActivity.this,PicBrowserActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("url", Util.getEastDayURL(news.getNewsurl()));
					intent.putExtras(bundle);
					startActivity(intent);
				}
			}
		});
		
        mPullToRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
  	      public void onRefresh(PullToRefreshBase<ListView> refreshView) {
    	          String str = DateUtils.formatDateTime(ShenHuoQuanListActivity.this, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
    	          // 下拉刷新 业务代码
    	          if (refreshView.isHeaderShown()) {
    	        	  isRefresh = true;
    	        	  mPullToRefreshListView.getLoadingLayoutProxy().setRefreshingLabel("正在刷新");
    	        	  mPullToRefreshListView.getLoadingLayoutProxy().setPullLabel("下拉刷新");
    	        	  mPullToRefreshListView.getLoadingLayoutProxy().setReleaseLabel("释放开始刷新");
    	        	  refreshView.getLoadingLayoutProxy().setLastUpdatedLabel("最后更新时间:" + str);
    	        	  requestJSON(refreshURL);
    	         } 
    	          
    	         // 上拉加载更多 业务代码
    	         if(refreshView.isFooterShown()) {
    	        	 isRefresh = false;
    	        	 mPullToRefreshListView.getLoadingLayoutProxy().setRefreshingLabel("正在加载");
    	        	 mPullToRefreshListView.getLoadingLayoutProxy().setPullLabel("上拉加载");
    	        	 mPullToRefreshListView.getLoadingLayoutProxy().setReleaseLabel("释放开始加载");
    	             refreshView.getLoadingLayoutProxy().setLastUpdatedLabel("最后加载时间:" + str);
    	             pageIndex++;
    	             String url = loadMoreURL;
    	             if(pageIndex > 0){
    	            	url = url.substring(0, url.lastIndexOf(".")) + "p" + pageIndex + ".html";
    	             }
    	             requestJSON(url);
    	         }
    	      }
          });
		loadingDialog = new LoadingDialog(this);
		loadingDialog.show();
        requestJSON(refreshURL);
	}
	
	private void requestJSON(String URL){
		//为何传过来为空了？？？
    	BaseTools.showlog("nodename="+nodename);
    	BaseTools.showlog("URL="+URL);
		JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,URL,null,new Response.Listener<JSONObject>() {  
            @Override  
            public void onResponse(JSONObject response) {
        		if (isRefresh==true) {
        			pageIndex = 0;
                	newsList.clear();
                	addNewsList(response.toString());
                	dataDB.addToDataCache(nodename, response.toString());		
        		} else {
        			NewsListData newsListData = getNewsListDataByJson(response.toString());
        			if (newsListData == null)
        				return;
        			newsList.addAll(newsListData.newslist);
        		}
    		    detail_loading.setVisibility(View.GONE);
    		    if (loadingDialog != null && loadingDialog.isShowing()) {
    		    	loadingDialog.dismiss();
    		    }
    		    newsListAdapter.notifyDataSetChanged();
    		    stopRefresh();
            	}  
        },new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError arg0) {
				if (newsList.size() == 0){
					String jsonStr = dataDB.getFromDataCache(nodename);
					addNewsList(jsonStr);
				}
    		    detail_loading.setVisibility(View.GONE);
    		    if (loadingDialog != null && loadingDialog.isShowing()) {
    		    	loadingDialog.dismiss();
    		    }
    		    newsListAdapter.notifyDataSetChanged();
    		    stopRefresh();
    			if (!isRefresh){
    				pageIndex--;
    			}
			}       	
        });  
		VolleyTool.getInstance(this).getmRequestQueue().add(jsonObjectRequest);
	}
	
	private void stopRefresh() {
		mPullToRefreshListView.postDelayed(new Runnable() {
			@Override
			public void run() {
				mPullToRefreshListView.onRefreshComplete();
			}
		}, 2000);
	}
	
	private void addNewsList(String json) {
		if (json == null)
			return;
			NewsListData newsListData = getNewsListDataByJson(json.toString());
			if (newsListData == null)
				return;
			if (newsListData.bigimagelist != null && newsListData.bigimagelist.size() > 0) {
				newsList.add(newsListData.bigimagelist);
		     }
			if (newsListData.newslist != null) {
				newsList.addAll(newsListData.newslist);
			 }
	 }
	
	private NewsListData getNewsListDataByJson(String json) {
		NewsListData data = null;
		try {
			Gson g = new Gson();
			data = g.fromJson(json, NewsListData.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}
	
	public class NewsListAdapter extends BaseAdapter {
		private Context context;
		private LayoutInflater inflater = null;
		public int mBigAdd;
		public int mBigPicWidth;
		public int mZTWidth, mZTHeight;
		protected ImageLoader imageLoader = ImageLoader.getInstance();
		private DisplayImageOptions options;	
		private ImageLoadingListener animateFirstListener;
		
		public static final int TYPE_CYCLE = 0;// 顶部滑动banner样式
		public static final int TYPE_NORMAL = 1;// 普通列表样式
		public static final int TYPE_SMALL = 2;// 三张小图样式
		public static final int TYPE_BIG = 3;// 一张大图样式
		public static final int TYPE_AD = 4;// 广告样式
		public static final int TYPE_ZTTJ = 5;// 专题图集样式
		
		public NewsListAdapter(Activity activity) {
			this.context = activity;
			inflater = LayoutInflater.from(activity);
			options = Options.getListOptions();
			mBigAdd = Util.dip2px(context, 2);
			mBigPicWidth = AppApplication.mWidth - Util.dip2px(context, 35);
			mZTWidth = (AppApplication.mWidth - Util.dip2px(context, 50)) / 2;
			mZTHeight = (int) (mZTWidth / 1.5);
		
			animateFirstListener = new AnimateFirstDisplayListener();
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
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
					type = 2;
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
			holder_cycle.mImageCycleView.setImageResources(newsList, false, new ImageCycleViewListener() {
				public void onImageClick(View imageView) {				
					News news = (News) imageView.getTag();
					if (news.getNewstype().equals("0")){
						news.parentNodeName = "申活圈";
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

				public void displayImage(final String imageURL, final ImageView imageView) {
					ImageLoader.getInstance().displayImage(imageURL, imageView, options, animateFirstListener);
				}

				@Override
				public void onPopupClick(View view) {					
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
			
			if (isLocalHaveFile(news.getNewstitle())){
				holder_normal.isHaveFileTextView.setVisibility(View.VISIBLE);
			}else{
				holder_normal.isHaveFileTextView.setVisibility(View.INVISIBLE);
			}
			holder_normal.index = position;
			holder_normal.news = news;
			holder_normal.normal_title.setText(news.getNewstitle() + " ");
			holder_normal.normal_tv_time.setVisibility(View.GONE);
			ImageLoader.getInstance().displayImage(Util.getEastDayURL(news.getImgurl1()), holder_normal.normal_iv, options, animateFirstListener);		
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
			String imageURL1 = Util.getEastDayURL(news.getImgurl1());
			String imageURL2 = Util.getEastDayURL(news.getImgurl2());
			String imageURL3 = Util.getEastDayURL(news.getImgurl3());
			ImageLoader.getInstance().displayImage(imageURL1, holder_small.small_iv01, options, animateFirstListener);		
			ImageLoader.getInstance().displayImage(imageURL2, holder_small.small_iv02, options, animateFirstListener);		
			ImageLoader.getInstance().displayImage(imageURL3, holder_small.small_iv03, options, animateFirstListener);		
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
			ImageLoader.getInstance().displayImage(Util.getEastDayURL(news.getImgurl1()), holder_big.big_iv, options, animateFirstListener);	
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
				ImageLoader.getInstance().displayImage(Util.getEastDayURL(news.getImgurl1()), holder_ad.ad_iv, options, animateFirstListener);
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
				ImageLoader.getInstance().displayImage(Util.getEastDayURL(news.getImgurl1()), holder_zttj.iv_zttj_01, options, animateFirstListener);
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
					ImageLoader.getInstance().displayImage(Util.getEastDayURL(news2.getImgurl1()), holder_zttj.iv_zttj_02, options, animateFirstListener);
				}
				return convertView;
			}	  

		public class HolderNormal {
			public News news;
			public ImageView normal_iv;
			public TextView normal_title;
			public ImageView normal_mark;
			public TextView isHaveFileTextView;
			public TextView normal_tv_time;
			public int index;

			public HolderNormal(View view) {
				normal_iv = (ImageView) view.findViewById(R.id.normal_iv);
				normal_title = (TextView) view.findViewById(R.id.normal_tv_title);
				normal_mark = (ImageView) view.findViewById(R.id.normal_mark);
				isHaveFileTextView = (TextView) view.findViewById(R.id.localTextViewID);
				normal_tv_time = (TextView) view.findViewById(R.id.normal_tv_time);
				index = -1;
			}
		}
		
		public class HolderCycle {
			public LinearLayout cycle_view;
			public ImageCycleView mImageCycleView;
			public View mLineView;
			public int index;
			public ArrayList<News> newsList;

			public HolderCycle(View view) {
				mImageCycleView = (ImageCycleView) view.findViewById(R.id.news_list_icv);
				mLineView = view.findViewById(R.id.news_list_view_line);
				newsList = new ArrayList<News>();
				index = -1;
			}
		}
		
		
		public class HolderSmall {
			public News news;
			// 3:小图
			public TextView small_title;
			public ImageView small_iv01;
			public ImageView small_iv02;
			public ImageView small_iv03;
			public int index;

			public HolderSmall(View view) {
				// small
				small_title = (TextView) view.findViewById(R.id.small_image_tv_title);
				small_iv01 = (ImageView) view.findViewById(R.id.small_image_iv01);
				small_iv02 = (ImageView) view.findViewById(R.id.small_image_iv02);
				small_iv03 = (ImageView) view.findViewById(R.id.small_image_iv03);
				index = -1;
			}
		}
		
		
		public class HolderBig {
			public News news;
			// 4:大图
			public RelativeLayout big_image_rl;
			public TextView big_title;
			public ImageView big_iv;
			public int index;

			public HolderBig(View view) {
				// big
				big_image_rl = (RelativeLayout) view.findViewById(R.id.big_image_rl);
				big_title = (TextView) view.findViewById(R.id.big_image_tv_title);
				big_iv = (ImageView) view.findViewById(R.id.big_image_iv);

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
		
		public class HolderAd {
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
		
		public class HolderZTTJ {
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
		
		private boolean isLocalHaveFile(String title){
			String filePath = FileUtil.getNewsPath() + "/" + title;
			return new File(filePath).exists();
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
		
		class AnimateFirstDisplayListener extends SimpleImageLoadingListener {		
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
	
}

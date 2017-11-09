package com.huaxun.news.fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager.LayoutParams;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnPullEventListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.State;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.huaxun.MainActivity.ScalCallback;
import com.huaxun.R;
import com.huaxun.app.AppApplication;
import com.huaxun.base.BaseFragment;
import com.huaxun.db.DataDB;
import com.huaxun.download.DownloadManager;
import com.huaxun.download.DownloadService;
import com.huaxun.fragment.NewsFragment;
import com.huaxun.news.DownloadFloatControl;
import com.huaxun.news.MediaFrameLayout;
import com.huaxun.news.activity.DownloadListActivity;
import com.huaxun.news.bean.News;
import com.huaxun.news.bean.NewsMediaData;
import com.huaxun.tool.CharsetJsonRequest;
import com.huaxun.tool.Options;
import com.huaxun.tool.Settings;
import com.huaxun.tool.VolleyTool;
import com.huaxun.utils.FileUtil;
import com.huaxun.utils.HttpUtil;
import com.huaxun.utils.NetUtil;
import com.huaxun.utils.Util;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.util.LogUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

@SuppressLint("NewApi")
public class NewsMediaFragment extends BaseFragment{
	private Activity activity;
	private Context context;
	public DownloadFloatControl downloadFloatControl;
	private ArrayList<News> newsMediaList = new ArrayList<News>();
	private PullToRefreshListView mPullToRefreshListView;
	private RelativeLayout notify_view;
	private TextView notify_text;
	private NewsMediaAdapter newsMediaAdapter;
	private DataDB dataDB;
	private String refreshURL = "";
	private String loadMoreURL = "";
	private String nodename = "";
	private ImageView detail_loading;
	private MediaFrameLayout mediaFrameLayout;
	private int mediaPlayPosition = -1;
	
	private int pageIndex = 0;
	private int updateCount = 0;   //每次刷新更新的新闻条数
	private String previousJsonStr = ""; //上次新闻的json数据
	private int refreshMode = 0;
	private final static int FirstLoading = 0;  //首次加载
	private final static int PullToRefresh = 1; //下拉刷新
	private final static int PullToLoadingMore = 2; //上拉加载更多
	
	static final int MENU_DISABLE_SCROLL = 10;
	static final int MENU_SET_MODE = 11;
	private DownloadManager downloadManager;
	
	private Settings settings;
	
	private final int updateMediaPosition = 0;
	private final int resetMediaPosition = 1;
	public final static int gotoDownloadList = 2;
	private Handler mediaHandler = new Handler() {
		public void handleMessage(Message msg) {
        	super.handleMessage(msg);
        	switch (msg.what) {
        	case updateMediaPosition:
				/**动态更新视频播放器垂直位置*/
				// 只有在非全屏状态下才动态更新mediaFrameLayout的Y坐标
				mediaFrameLayout.setY(Util.getTopMargin(mediaPlayPosition, mPullToRefreshListView.getRefreshableView()));						
				mediaHandler.sendEmptyMessageDelayed(updateMediaPosition, 20);				
        		break;  
        	case resetMediaPosition:       		
        		View listItem = newsMediaAdapter.getView(mediaPlayPosition, null, mPullToRefreshListView.getRefreshableView());
        		listItem.measure(0, 0);
        		int height = listItem.getMeasuredHeight();
        		mediaFrameLayout.setY((mediaPlayPosition-1)*height);
        		mediaHandler.removeMessages(updateMediaPosition);
        		break;
        	case gotoDownloadList:
        		hideFloatControl();
                Intent intent = new Intent(context, DownloadListActivity.class);
                intent.putExtra("source", "NewsMediaFragment");
                startActivity(intent);
                ((Activity) context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        		break;
        	}
		}
	};
	

	public void onDestroy() {
		super.onDestroy();
		//  If null, all callbacks and messages will be removed.
		handler.removeCallbacksAndMessages(null);
		mediaHandler.removeCallbacksAndMessages(null);
		mediaFrameLayout.timeHandler.removeCallbacksAndMessages(null);
		activity.unregisterReceiver(showFloatReceiver);
	}	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		this.context = this.getActivity();
		settings = new Settings(context, false);		
		setHasOptionsMenu(true);  //在fragment中定义optionmenu
		dataDB = DataDB.getInstance(context);
		downloadFloatControl = new DownloadFloatControl(context, mediaHandler);
		downloadManager = DownloadService.getDownloadManager(context);
		
		Bundle args = getArguments();
		refreshURL = args != null ? args.getString("refreshURL") : "";
		loadMoreURL = args != null ? args.getString("loadMoreURL") : "";
		nodename = args != null ? args.getString("nodeName") : "";
		
		showFloatReceiver = new ShowFloatReceiver();
		IntentFilter mFilter = new IntentFilter("showFloat");
		activity.registerReceiver(showFloatReceiver, mFilter);
		
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onAttach(Activity activity) {
		//TODO Auto-generated method stub
		this.activity = activity;
		super.onAttach(activity);
	}

	protected boolean isVisible;	
	// 防止预加载数据
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (getUserVisibleHint()) {
			isVisible = true;
			mediaHandler.postDelayed(new Runnable(){
				@Override
				public void run() {
					if (settings.getDownloadMediaMode().equals(Settings.DOWNLOAD_MEDIA_RESUME)) {
						showFloatControl();
					}					
					refreshMode = FirstLoading;
					requestJSON(refreshURL);
					mainAct.setScalInterface(new ScalCallback(){
						public void scal_media() {
							mediaFrameLayout.setMediaFragmentNormalSize();
						}
					});
				}}, 200);
		} else {
			hideFloatControl();		
			isVisible = false;
		}
	}
	
	private void showFloatControl() {
		if (downloadFloatControl != null) {
			downloadFloatControl.addView();   //点击进入下载队列的悬浮按钮出现
		}
	}
	
	private void hideFloatControl() {
		if (downloadFloatControl != null) {
			downloadFloatControl.removeView();   //点击进入下载队列的悬浮按钮消失
		}
	}
	
	private ShowFloatReceiver showFloatReceiver;
	class ShowFloatReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			showFloatControl();
		   }
	   }

	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = LayoutInflater.from(getActivity()).inflate(
				R.layout.news_list_media_fragment, null);
		mediaFrameLayout = (MediaFrameLayout) view.findViewById(R.id.media_frame);
		mPullToRefreshListView = (PullToRefreshListView) view.findViewById(R.id.pull_to_refresh_listview);
		detail_loading = (ImageView) view.findViewById(R.id.detail_loading);
		notify_view = (RelativeLayout) view.findViewById(R.id.notify_view);
		notify_text = (TextView) view.findViewById(R.id.notify_text);
		newsMediaAdapter = new NewsMediaAdapter(activity);
		mPullToRefreshListView.setAdapter(newsMediaAdapter);
		mPullToRefreshListView.setMode(Mode.BOTH);
		//在Scroll或Fling的时候不要去加载图片
		//mPullToRefreshListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
		mPullToRefreshListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (isVisible) {
					onScrollShowOrHide(firstVisibleItem, mPullToRefreshListView.getRefreshableView());
				}
				/**动态更新视频播放器垂直位置*/
				if (mediaPlayPosition != -1 && mediaFrameLayout.getVisibility() == View.VISIBLE) {
					boolean isVisible = (mediaPlayPosition >= firstVisibleItem) && (mediaPlayPosition <= (firstVisibleItem + visibleItemCount - 1));
					if (isVisible) {
						// 只有在非全屏状态下才动态更新mediaFrameLayout的Y坐标
						if (!mediaFrameLayout.isFullScreen) {
							mediaFrameLayout.setMediaFragmentNormalSize();
							mediaFrameLayout.setY(Util.getTopMargin(mediaPlayPosition, mPullToRefreshListView.getRefreshableView()));
						}
					} else {
						// 不可见时设置为小屏且Y坐标固定
						mediaFrameLayout.setMediaFragmentSmallSize();
						mediaFrameLayout.setY(AppApplication.mHeight / 2);
					}
				}
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {				
			}

		});		
		
        mPullToRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
    	      public void onRefresh(PullToRefreshBase<ListView> refreshView) {
      	          String str = DateUtils.formatDateTime(context, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
      	          // 下拉刷新 业务代码
      	          if (refreshView.isHeaderShown()) {
      	        	  refreshMode = PullToRefresh;
      	        	  mPullToRefreshListView.getLoadingLayoutProxy().setRefreshingLabel("正在刷新...");
      	        	  mPullToRefreshListView.getLoadingLayoutProxy().setPullLabel("下拉刷新");
      	        	  mPullToRefreshListView.getLoadingLayoutProxy().setReleaseLabel("释放开始刷新");
      	        	  refreshView.getLoadingLayoutProxy().setLastUpdatedLabel("最后更新时间:" + str);
      	        	  requestJSON(refreshURL);     	        	  
      	         } 
      	          
      	         // 上拉加载更多 业务代码
      	         if(refreshView.isFooterShown()) {
      	        	 refreshMode = PullToLoadingMore;
      	        	 mPullToRefreshListView.getLoadingLayoutProxy().setRefreshingLabel("正在加载...");
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
        //用于在下拉刷新时动态调整当前播放视频的Y位置，在刷新完成后复位
		mPullToRefreshListView.setOnPullEventListener(new OnPullEventListener() {
			@Override
			public void onPullEvent(PullToRefreshBase refreshView, State state, Mode direction) {
                if (state == PullToRefreshBase.State.PULL_TO_REFRESH) {
                	if (mediaPlayPosition != -1 && mediaPlayPosition <=3 && mediaFrameLayout.getVisibility() == View.VISIBLE) {
                		mediaHandler.sendEmptyMessage(updateMediaPosition);
                	}               	
                } else if (state == PullToRefreshBase.State.RESET) {
                	if (mediaPlayPosition != -1 && mediaPlayPosition <=3 && mediaFrameLayout.getVisibility() == View.VISIBLE) {
                		mediaHandler.sendEmptyMessage(resetMediaPosition);
                	}                  	
                }              
			}			
		});
        
//		if (getUserVisibleHint()) {
//			isVisible = true;
//			refreshMode = FirstLoading;
//			requestJSON(refreshURL);
//		}
		return view;
	}

	private void requestJSON(String URL) {
		CharsetJsonRequest charsetJsonRequest = new CharsetJsonRequest(
				Request.Method.GET, URL, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						if (!isVisible){
							return;
						}
						if (refreshMode == PullToRefresh) {
							pageIndex = 0;
							//得到新闻更新数量
		                	previousJsonStr  = dataDB.getFromDataCache(nodename);
							if (response.toString().equals(previousJsonStr)) {
								updateCount = 0;
							} else {
								updateCount = getNewsUpdateCount(previousJsonStr,response.toString());
								dataDB.addToDataCache(nodename, response.toString());
								newsMediaList.clear();
								addNewsMediaList(response.toString());
								newsMediaAdapter.notifyDataSetChanged();
							}
							stopRefresh();
                         } else if (refreshMode == PullToLoadingMore) {
							addNewsMediaList(response.toString());	
							newsMediaAdapter.notifyDataSetChanged();
							stopRefresh();
						 } else if (refreshMode == FirstLoading) {
			        		pageIndex = 0;
			                dataDB.addToDataCache(nodename, response.toString());
			                addNewsMediaList(response.toString());
							newsMediaAdapter.notifyDataSetChanged();
							detail_loading.setVisibility(View.GONE);
			    		    if (NewsFragment.isAutoDownload == true) {
		        		    	downloadMediaContent();
		        		    }
			        	 }						
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						if (!isVisible){
							return;
						}
						if (newsMediaList.size() == 0) {
							String jsonStr = dataDB.getFromDataCache(nodename);
							addNewsMediaList(jsonStr);
						}
						newsMediaAdapter.notifyDataSetChanged();
						
						updateCount = 0;
						if (refreshMode == PullToRefresh) {
		    		    	stopRefresh();
		    		    } else if (refreshMode == PullToLoadingMore){
		    				pageIndex--;
		    				stopRefresh();
		    			} else if (refreshMode == FirstLoading) {
		        		    detail_loading.setVisibility(View.GONE);
		    			}					
					}
				});
		VolleyTool.getInstance(this.getActivity()).getmRequestQueue().add(charsetJsonRequest);
	}
	
	private void stopRefresh() {
		mPullToRefreshListView.postDelayed(new Runnable() {
			@Override
			public void run() {
				mPullToRefreshListView.onRefreshComplete();				
				updateNotify(updateCount);
			}
		}, 2000);
	}

	private void addNewsMediaList(String json) {
		if (json == null)
			return;
		NewsMediaData newsMediaData = getNewsMediaDataByJson(json.toString());
		if (newsMediaData == null)
			return;
		if (newsMediaData.newslist != null) {
			newsMediaList.addAll(newsMediaData.newslist);
		}
	}

	private NewsMediaData getNewsMediaDataByJson(String json) {
		NewsMediaData data = null;
		try {
			Gson g = new Gson();
			data = g.fromJson(json, NewsMediaData.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}
	
	//返回jsonStr2相对于jsonStr1更新的新闻条数
	private int getNewsUpdateCount(String jsonStr1, String jsonStr2) {
		int count = 0;
		int maxNewsId = 0;
		ArrayList<News> list1 = switchToNewsList(jsonStr1);
		ArrayList<News> list2 = switchToNewsList(jsonStr2);
		for (int i=0;i<list1.size();i++) {
			int newsId = Integer.parseInt(list1.get(i).getNewsid());
			if (newsId > maxNewsId) {
				maxNewsId = newsId;
			}
		}
		for (int j=0;j<list2.size();j++) {
			int newsId = Integer.parseInt(list2.get(j).getNewsid());
			if (newsId > maxNewsId) {
				count++;
			}
		}
		return count;
	}

	// 把json数据转换成ArrayList<News>
	private ArrayList<News> switchToNewsList(String json) {
		ArrayList<News> list = new ArrayList<News>();
		if (json != null) {
			NewsMediaData newsMediaData = getNewsMediaDataByJson(json.toString());
			if (newsMediaData != null) {
				if (newsMediaData.newslist != null) {
					list.addAll(newsMediaData.newslist);
				}
			}
		}
		return list;
	}
	
	/* 初始化通知栏目*/
	private void updateNotify(final int num) {
		new Handler().postDelayed(new Runnable() {		
			@Override
			public void run() {
				// TODO Auto-generated method stub
				notify_text.setText(String.format(context.getString(R.string.ss_pattern_update), num));
				notify_view.setVisibility(View.VISIBLE);
				new Handler().postDelayed(new Runnable() {	
					@Override
					public void run() {
						// TODO Auto-generated method stub
						notify_view.setVisibility(View.GONE);
					}
				}, 2000);
			}
		}, 500);
	}
	
	private String getMediaName(String newstitle) {
		return newstitle.length()<=18? newstitle : newstitle.substring(0,16)+"...";
	}
	
	private final int mediaDownloadOK = 1;
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
        	super.handleMessage(msg);
        	switch (msg.what) {
        	case mediaDownloadOK:
        		String newsName = (String) msg.obj;
        		Util.showToast(context, "视频："+newsName+"下载完毕");
        		break;
        	}
		}
	};

	
	//自动下载视频内容
	private void downloadMediaContent() {
		new Thread(new Runnable(){
			@Override
			public void run() {
				int mediaIndex = 0;
				while(mediaIndex < newsMediaList.size()) {
					Object obj = newsMediaList.get(mediaIndex);
					News news = (News) obj;
					if (news.getNewstype().equals("7")) {
						String newsName = getMediaName(news.getNewstitle());
						if (!FileUtil.isMediaFileExist(newsName)) {							
//							FileUtil.downloadMediaToSDcard(newsName,news.newsurl);
							HttpUtil.saveFileToLocal(news.getNewsurl(),FileUtil.getMediaPath() + "/" + newsName, "utf-8");
							Message msg = new Message();
							msg.what = mediaDownloadOK;
							msg.obj = newsName;
							handler.sendMessage(msg);
						}		
					}
					mediaIndex++;
				}	
				Intent mIntent = new Intent(NewsFragment.DownloadOK);
				mIntent.putExtra("nodeName",nodename);  
				context.sendBroadcast(mIntent);
			}			
		}).start();
	}
 

	public class NewsMediaAdapter extends BaseAdapter implements OnClickListener {
		private Context context;
		private LayoutInflater inflater = null;
		public int mBigAdd;
		public int mBigPicWidth;
		public int mZTWidth;
		public int mZTHeight;
		private DisplayImageOptions options;
		//下面五个变量是控制下载过程的
		private String downLoadMediaURL;	
		private String downLoadMediaName;
		private int downLoadMediaPosition;
		private ProgressBar progressBar;
		private FrameLayout media_fl; 
		
		
		public NewsMediaAdapter(Activity activity) {
			this.context = activity;
			inflater = LayoutInflater.from(activity);
			options = Options.getListOptions();
			mBigAdd = Util.dip2px(context, 2);
			mBigPicWidth = AppApplication.mWidth - Util.dip2px(context, 35);
			mZTWidth = (AppApplication.mWidth - Util.dip2px(context, 50)) / 2;
			mZTHeight = (int) (mZTWidth / 1.5);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return newsMediaList == null ? 0 : newsMediaList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			if (newsMediaList != null && newsMediaList.size() != 0) {
				return newsMediaList.get(position);
			}
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressLint("InflateParams")
		@Override
		public View getView(final int position, View view, ViewGroup parent) {
			ViewHolder holder = null;
			if (newsMediaList.size() <= position) {
				return null;
			}
			if (view == null) {
				view = inflater.inflate(R.layout.newsmedia_item, null);
				holder = new ViewHolder(view);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}

			News news = newsMediaList.get(position);
			holder.news = news;
			holder.position = position;
			holder.media_title.setText(news.getNewstitle() + "");
			holder.media_time.setText("时长：" + news.time);
			if (news.getCreatetime() != null) {
				if (news.getCreatetime().length() > 10) {
					holder.media_createTime.setText("上传时间 : "+ news.getCreatetime().substring(0, 10));
				}
			}
			ImageLoader.getInstance().displayImage(Util.getEastDayURL(news.getImgurl1()), holder.iv_background,options);
			//如果media在本地存在，下载按钮的地方显示视频播放按钮
//			String newsName = FileUtil.getFileName(news.newsurl);
			String newsName = getMediaName(news.getNewstitle());
			if (FileUtil.isMediaFileExist(newsName)) {
				holder.media_downfile.setImageResource(R.drawable.local_media);
			} else {
				//很奇怪，如果不加下面语句，有些item即使不存在也会显示存在的图标，至今不明
				holder.media_downfile.setImageResource(R.drawable.down_load_button);
			}
			// 点击播放视频
			holder.iv_play.setTag(holder);
			holder.iv_play.setOnClickListener(this);			
			// 下载按钮，点击事件
			holder.media_downfile.setTag(holder);
			holder.media_downfile.setOnClickListener(this);	
			return view;
		}
		
		public void onClick(View view) {
			switch (view.getId()) {
			// 点击播放视频
			case R.id.iv_play:
				ViewHolder holder = (ViewHolder) view.getTag();
				Object obj = newsMediaList.get(holder.position);
				News news = (News) obj;
				mediaFrameLayout.setVisibility(View.VISIBLE);
				//这里需要注意，PullToRefreshListView.getRefreshableView的Index为0对应的是下拉刷新菜单，所以真正的视频是从Index为1处开始
				mediaPlayPosition = holder.position+1;
				//这里的位置用mediaPlayPosition，否则播放位置总在上一个Item位置
				mediaFrameLayout.setY(Util.getTopMargin(mediaPlayPosition,mPullToRefreshListView.getRefreshableView()));
				//这里的位置用holder.position，否则取最后一个listItem出现空指针
				mediaFrameLayout.startPlay(news, mPullToRefreshListView.getRefreshableView(),holder.position, newsMediaAdapter);
				// 下载按钮，点击事件
				break;
			case R.id.media_down_or_delete:	
				final ViewHolder holder2 = (ViewHolder) view.getTag();
				News news2 = holder2.news;
				downLoadMediaURL = news2.getNewsurl();
				downLoadMediaName = getMediaName(news2.getNewstitle());
//				downLoadMediaName = FileUtil.getFileName(news2.newsurl);
				downLoadMediaPosition = holder2.position;
				progressBar = holder2.progressBar;
				media_fl = holder2.media_fl;
				// 如果media在本地存在，点击直接播放视频
				if (FileUtil.isMediaFileExist(downLoadMediaName)) {
					mediaFrameLayout.setVisibility(View.VISIBLE);
					mediaPlayPosition = holder2.position+1;
					mediaFrameLayout.setY(Util.getTopMargin(mediaPlayPosition, mPullToRefreshListView.getRefreshableView()));
					mediaFrameLayout.startPlay(news2, mPullToRefreshListView.getRefreshableView(),holder2.position, newsMediaAdapter);
				} else {
					// 如果media在本地不存在,且当前非正在下载中,点击下载视频
					if (!FileUtil.isMediaFileExist(downLoadMediaName+"tmp")) {
						if (settings.getDownloadOnlyWifi() && !NetUtil.isWIFIOn(context)) {
							Util.showToast(context, "仅WIFI下下载所有新闻");
						    return;
						}
						if (!NetUtil.isNetworkAvailable(context)) {
							Util.showToast(context, "请开启网络");
							return;
						}
						final ImageView moveImageView = getView(view);
						final int[] startLocation = new int[2];
						final int[] endLocation = new int[2];
						
						if (settings.getDownloadMediaMode().equals(Settings.DOWNLOAD_MEDIA_RESUME)) {
							endLocation[0] = downloadFloatControl.wmParams.x + downloadFloatControl.allImageView.getMeasuredWidth() / 2;
							endLocation[1] = downloadFloatControl.wmParams.y + downloadFloatControl.allImageView.getMeasuredHeight() / 2 + 80;
						} else {
							endLocation[0] = endLocation[1] = 0;
						}
						if (moveImageView != null) {
		                	//获取动画起始点坐标
							ImageView media_downfile = (ImageView) view.findViewById(R.id.media_down_or_delete);
							media_downfile.getLocationInWindow(startLocation);
						}
						if (!NetUtil.isWIFIOn(context)) {
							AlertDialog.Builder builder = new Builder(context);
							builder.setTitle("提示");
							builder.setMessage("当前使用的是数据流量，是否下载？");
							builder.setPositiveButton("确定",new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog,int which) {
											dialog.dismiss();
											//动画执行
										    MoveAnim(moveImageView, startLocation , endLocation);
											// 新启一个下载线程，把media_fl，progressBar传过去，在新线程中更新progressBar进度
											//new MediaDownLoadThread(holder2.media_fl,holder2.progressBar,downLoadMediaURL,downLoadMediaName,downLoadMediaPosition).start();
										}
									});
							builder.setNegativeButton("取消",new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog,int which) {
											dialog.dismiss();
										}
									});
							builder.show();
						} else {
							//动画执行
						    MoveAnim(moveImageView, startLocation , endLocation);						    
							//new MediaDownLoadThread(holder2.media_fl,holder2.progressBar,downLoadMediaURL,downLoadMediaName,downLoadMediaPosition).start();
						}
					}
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
			ImageView iv = new ImageView(context);
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
		private void MoveAnim(View moveView, int[] startLocation,int[] endLocation) {
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
			moveAnimation.setDuration(800L);//动画时间
			//动画配置
			AnimationSet moveAnimationSet = new AnimationSet(true);
			moveAnimationSet.setFillAfter(false);//动画效果执行完毕后，View对象不保留在终止的位置
			moveAnimationSet.addAnimation(moveAnimation);
			mMoveView.startAnimation(moveAnimationSet);
			moveAnimationSet.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {
					//开始移动
					//isMove = true;
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {
				}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					moveViewGroup.removeView(mMoveView);
					//new MediaDownLoadThread(downLoadMediaURL,downLoadMediaName,downLoadMediaPosition).start();
					if (settings.getDownloadMediaMode().equals(Settings.DOWNLOAD_MEDIA_NOTIFICATION)) {
						new MediaDownLoadThread(media_fl,progressBar,downLoadMediaURL,downLoadMediaName,downLoadMediaPosition).start();
					} else {
				        String target = FileUtil.getMediaPath() + "/" + downLoadMediaName;
				        try {
				            downloadManager.addNewDownload(downLoadMediaURL,
				            		downLoadMediaName,
				                    target,
				                    true, // 如果目标文件存在，接着未完成的部分继续下载。服务器不支持RANGE时将从新下载。
				                    false, // 如果从请求返回信息中获取到文件名，下载完成后自动重命名。
				                    null);
				        } catch (DbException e) {
				            LogUtils.e(e.getMessage(), e);
				        }
					}					
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
			ViewGroup moveViewGroup = (ViewGroup) activity.getWindow().getDecorView();
			LinearLayout moveLinearLayout = new LinearLayout(context);
			moveLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			moveViewGroup.addView(moveLinearLayout);
			return moveLinearLayout;
		}		
	}	


	public class MediaDownLoadThread extends Thread {		
		private ProgressBar progressBar;
		private FrameLayout media_fl;
		private String mediaURL = "";
		private String mediaName = "";
		private int NOTIFY_ID = 0;

		private boolean isContinueDownload = true;
		private int totalSize = 0;
		private int downloadSize = 0;
		private String notificationTitle = "";
		private int notificationPercent = 0;
		private int NOTIFY_FLAG = 16;	
		private final int updateNotification = 1;
		
		private Handler notificationHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case updateNotification:
					notificationPercent = (int)(((float)downloadSize/(float)totalSize)*100+0.5);
					if (notificationPercent<100) {
						//这里有一个问题，更新点击item进度时，会有其他item也会显示进度条并同步更新，至今不明
						media_fl.setVisibility(View.VISIBLE);
						progressBar.setMax(100);
						progressBar.setProgress(notificationPercent);
						
						notificationTitle = "视频：\""+mediaName+"\"正在下载中，请稍后！";
						NOTIFY_FLAG = 32;
		                notificationHandler.sendEmptyMessageDelayed(updateNotification, 1000);
					} else {
						media_fl.setVisibility(View.INVISIBLE);
						notificationTitle = "视频：\""+mediaName+"\"下载成功！";
						NOTIFY_FLAG = 16;
						newsMediaAdapter.notifyDataSetChanged();
					}
					//BaseTools.showlog(mediaName+"----"+NOTIFY_ID+"----"+notificationPercent);
					createNotification();	

					break;
				}
			}
		};

		
		public MediaDownLoadThread(String mediaURL,String mediaName,int mediaPosition) {
			this.mediaURL = mediaURL;
			this.mediaName = mediaName;
			//因为每个下载的视频的position是不同的，我们可以把它作为NOTIFY_ID，从而显示不同的通知
			this.NOTIFY_ID = mediaPosition;
		}
		
		public MediaDownLoadThread(FrameLayout media_fl, ProgressBar progressBar,String mediaURL,String mediaName,int mediaPosition) {
			this(mediaURL,mediaName,mediaPosition);
			this.media_fl = media_fl;
			this.progressBar = progressBar;
		}

		public void run() {
			httpDownLoad();
		}

		private void httpDownLoad() {
			URL url = null;
			HttpURLConnection conn = null;
			InputStream inStream = null;
			try {
				url = new URL(mediaURL);
				conn = (HttpURLConnection) url.openConnection();
				conn.setDoInput(true); // 设置向程序输入数据
				conn.setRequestMethod("GET"); // 设置Get方法来请求
				conn.connect(); // 连接服务器

				// 返回200表示连接成功
				int responseCode = conn.getResponseCode();
				if (responseCode == 200) {
					totalSize = conn.getContentLength();
					downloadSize = 0;
					isContinueDownload = true;
					//发送消息请求建立下载通知
	                notificationHandler.sendEmptyMessage(updateNotification);
					inStream = conn.getInputStream();
					// 如果SD卡上media文件不存在就创建它，如果存在就删掉它
					FileUtil.createTempMediaFile(mediaName);
					// 真正的下载过程
					File file = null;
					OutputStream output = null;
					try {
						// 在本地创建要下载的文件，然后读取数据放进去
						file = new File(FileUtil.getMediaPath() + "/" + mediaName + ".tmp");
						// 把创建好的文件用输出流打开，准备往里面写数据
						output = new FileOutputStream(file);
						byte[] buffer = new byte[1024];
						int num = 0;
						do {
							num = inStream.read(buffer);
							downloadSize += num;
							if (num <= 0) {
								isContinueDownload = false;
								break;
							}
							output.write(buffer, 0, num);
							
						} while (isContinueDownload);

						output.flush();
						output.close();
					} catch (Exception e) {
						e.printStackTrace();
					}

					File f = new File(FileUtil.getMediaPath() + "/" + mediaName);
					file.renameTo(f);
				} else {

				}
			} catch (Exception e) {
			} finally {
				// finally无论有没有异常都会执行，在这里断开连接
				conn.disconnect();
			}
		}
		
	      /**  
		    * 方法描述：createNotification方法 
		    */  
			@SuppressWarnings("deprecation")
	    public void createNotification() {
		    	Notification notification = new Notification(
		                R.drawable.download_pressed,//应用的图标  
		                notificationTitle,
		                System.currentTimeMillis());
//		        notification.flags = Notification.FLAG_ONGOING_EVENT;(2)   
//		        notification.flags = Notification.FLAG_AUTO_CANCEL;(16)
//		    	notification.flags = Notification.FLAG_NO_CLEAR;(32)
//		    	notification.flags = Notification.FLAG_INSISTENT;(4)
		    	notification.flags = NOTIFY_FLAG;
		         /*** 自定义  Notification 的显示****/
		        RemoteViews remoteView = new RemoteViews(context.getPackageName(),R.layout.download_notification_layout);
				remoteView.setImageViewResource(R.id.icon, R.drawable.download_pressed);
				remoteView.setTextViewText(R.id.notificationTitle , notificationTitle);
				remoteView.setTextViewText(R.id.notificationPercent , notificationPercent+"%");
				remoteView.setProgressBar(R.id.notificationProgress , 100, notificationPercent, false);
				notification.contentView = remoteView;

				NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				manager.notify(NOTIFY_ID, notification);  
		    }  
	}

	public class ViewHolder {
		public View view_loading;
		public ImageView iv_play;
		public TextView media_title;
		public TextView media_time;
		public TextView media_createTime;
		public ImageView media_downfile;
		public ImageView iv_background;
		public RelativeLayout rl_media_image;
		public FrameLayout media_fl;
		public ProgressBar progressBar;
		public News news;
		public int position;

		public ViewHolder(View convertView) {
			view_loading = convertView.findViewById(R.id.view_loading);
			iv_play = (ImageView) convertView.findViewById(R.id.iv_play);
			media_title = (TextView) convertView.findViewById(R.id.media_tile);
			media_time = (TextView) convertView.findViewById(R.id.media_time);
			media_createTime = (TextView) convertView.findViewById(R.id.media_createtime);
			media_downfile = (ImageView) convertView.findViewById(R.id.media_down_or_delete);
			iv_background = (ImageView) convertView.findViewById(R.id.iv_background);
			rl_media_image = (RelativeLayout) convertView.findViewById(R.id.rl_media_big_image);
			media_fl = (FrameLayout) convertView.findViewById(R.id.media_fl);
			progressBar = (ProgressBar) convertView.findViewById(R.id.progressbar);
		}
	}
	
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//		menu.add(0, MENU_DISABLE_SCROLL, 0, mPullToRefreshListView.isScrollingWhileRefreshingEnabled() ? "禁止刷新时滚动" : "允许刷新时滚动");
//		menu.add(0, MENU_SET_MODE, 1, mPullToRefreshListView.getMode() == Mode.BOTH ? "禁止上拉加载" : "允许上拉加载");
		return;
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {	
		menu.add(1, MENU_DISABLE_SCROLL, 0, mPullToRefreshListView.isScrollingWhileRefreshingEnabled() ? "禁止刷新时滚动" : "允许刷新时滚动");
		menu.add(1, MENU_SET_MODE, 1, mPullToRefreshListView.getMode() == Mode.BOTH ? "禁止上拉加载" : "允许上拉加载");
//		MenuItem disableItem = menu.findItem(MENU_DISABLE_SCROLL);
//		disableItem.setTitle(mPullToRefreshListView.isScrollingWhileRefreshingEnabled() ? "禁止刷新时滚动"	: "允许刷新时滚动");
//		MenuItem setModeItem = menu.findItem(MENU_SET_MODE);
//		setModeItem.setTitle(mPullToRefreshListView.getMode() == Mode.BOTH ? "禁止上拉加载" : "允许上拉加载");
		return;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_DISABLE_SCROLL:
			Util.showToast(context, mPullToRefreshListView.isScrollingWhileRefreshingEnabled() ? "禁止刷新时滚动" : "允许刷新时滚动");
			mPullToRefreshListView.setScrollingWhileRefreshingEnabled(!mPullToRefreshListView.isScrollingWhileRefreshingEnabled());
			break;
		case MENU_SET_MODE:
			Util.showToast(context, mPullToRefreshListView.getMode() == Mode.BOTH ? "禁止上拉加载" : "允许上拉加载");
			mPullToRefreshListView.setMode(mPullToRefreshListView.getMode() == Mode.BOTH ? Mode.PULL_FROM_START : Mode.BOTH);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}

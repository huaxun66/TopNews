package com.huaxun.news.fragment;

import java.util.ArrayList;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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
import com.huaxun.base.BaseFragment;
import com.huaxun.db.DataDB;
import com.huaxun.fragment.NewsFragment;
import com.huaxun.news.activity.PicBrowserActivity;
import com.huaxun.news.activity.WebActivity;
import com.huaxun.news.adapter.NewsListAdapter;
import com.huaxun.news.bean.News;
import com.huaxun.news.bean.NewsListData;
import com.huaxun.news.bean.TodayRecommendData;
import com.huaxun.news.service.SpeechSynthesizerService;
import com.huaxun.tool.BaseTools;
import com.huaxun.tool.CharsetJsonRequest;
import com.huaxun.tool.VolleyTool;
import com.huaxun.utils.FileUtil;
import com.huaxun.utils.HttpUtil;
import com.huaxun.utils.Util;

@SuppressLint("InflateParams")
public class NewsListFragment extends BaseFragment {
	private Activity activity;
	private Context context;
	private ArrayList<Object> newsList = new ArrayList<Object>();
	private PullToRefreshListView mPullToRefreshListView;
	private NewsListAdapter newsListAdapter;
	private DataDB dataDB;
	private String refreshURL = "";
	private String loadMoreURL = "";
	private String nodename = "";
	private LinearLayout detail_loading;
	private LinearLayout no_data_ll;
	private RelativeLayout notify_view;
	private TextView notify_text;
	
	private int pageIndex = 0;
	private int updateCount = 0;   //每次刷新更新的新闻条数
	private String previousJsonStr = ""; //上次新闻的json数据
	private int refreshMode = 0;
	private final static int FirstLoading = 0;  //首次加载
	private final static int PullToRefresh = 1; //下拉刷新
	private final static int PullToLoadingMore = 2; //上拉加载更多
	
	static final int MENU_DISABLE_SCROLL = 10; //注意这里区别于newsfragment，用10，11
	static final int MENU_SET_MODE = 11;
	private MenuInflater inflater;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.context = this.getActivity();
		setHasOptionsMenu(true);  //在fragment中定义optionmenu
//		inflater = this.getActivity().getMenuInflater();  //获取MenuInflater
		dataDB = DataDB.getInstance(context);
		Bundle args = getArguments();
		refreshURL = args != null ? args.getString("refreshURL") : "";
		loadMoreURL = args != null ? args.getString("loadMoreURL") : "";
		nodename = args != null ? args.getString("nodeName") : "";
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onAttach(Activity activity) {
		this.activity = activity;
		super.onAttach(activity);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Intent intent = new Intent();
		intent.setClass(context, SpeechSynthesizerService.class);
		context.stopService(intent);
	}

	private boolean isVisible;
	Handler handler = new Handler();
	// 防止预加载数据
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
			isVisible = true;
			handler.postDelayed(new Runnable(){
				@Override
				public void run() {
					refreshMode = FirstLoading;
					requestJSON(refreshURL);
				}}, 200);
		} else {
			isVisible = false;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.news_list_fragment, null);
		mPullToRefreshListView = (PullToRefreshListView) view.findViewById(R.id.pull_to_refresh_listview);
		detail_loading = (LinearLayout) view.findViewById(R.id.loading_ll);
		no_data_ll = (LinearLayout) view.findViewById(R.id.no_data_ll);
		notify_view = (RelativeLayout) view.findViewById(R.id.notify_view);
		notify_text = (TextView) view.findViewById(R.id.notify_text);
		newsListAdapter = new NewsListAdapter(activity,nodename);
		// You can also just use setListAdapter(newsListAdapter) or
		// mPullToRefreshListView.setAdapter(newsListAdapter)
		mPullToRefreshListView.getRefreshableView().setAdapter(newsListAdapter);
		mPullToRefreshListView.setMode(Mode.BOTH);
		//在Scroll或Fling的时候不要去加载图片
		//mPullToRefreshListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
		mPullToRefreshListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {}
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (isVisible) {
					onScrollShowOrHide(firstVisibleItem, mPullToRefreshListView.getRefreshableView());
				}				
			}
		});
		mPullToRefreshListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				Object obj = newsList.get(position-1);
				if (obj instanceof ArrayList) {
					return;
				}
				News news = (News) obj;
				if (news.getNewstype().equals("0")) {
					//看过新闻，newsid加入数据库
					dataDB.addIdCache(news.getNewsid());
					newsListAdapter.notifyDataSetChanged();
					news.parentNodeName = nodename;
					news.isshare = "true";
					Intent intent = new Intent(context,WebActivity.class);
					Bundle bundle = new Bundle();
					bundle.putSerializable("NEWS", news);
					intent.putExtras(bundle);
					context.startActivity(intent);
					((Activity) context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				} else if (news.getNewstype().equals("5")) {
					dataDB.addIdCache(news.getNewsid());
					newsListAdapter.notifyDataSetChanged();
					Intent intent = new Intent(context,PicBrowserActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("url", (news.getNewsurl()).startsWith("http")? news.getNewsurl():Util.getEastDayURL(news.getNewsurl()));
					intent.putExtras(bundle);
					context.startActivity(intent);
				}
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
//    	        	  Drawable imageDrawable = context.getResources().getDrawable(R.drawable.account_icon_renren);
//    	        	  mPullToRefreshListView.getLoadingLayoutProxy().setLoadingDrawable(imageDrawable);
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
    	             //今日推荐的loadmoreurl和refreshurl不同，所以加载更多应该从pageindex=0开始，其他栏目从1开始
    	             if (nodename.equals("今日推荐")){
    	            	 pageIndex-=1;
    	             }
    	             if(pageIndex > 0){
    	            	url = url.substring(0, url.lastIndexOf(".")) + "p" + pageIndex + ".html";
    	             }
    	             requestJSON(url);
    	         }
    	      }
          });
		/**
		 * Add Sound Event Listener
		 */
//		SoundPullEventListener<ListView> soundListener = new SoundPullEventListener<ListView>(context);
//		soundListener.addSoundEvent(State.PULL_TO_REFRESH, R.raw.pull_event);
//		soundListener.addSoundEvent(State.RESET, R.raw.reset_sound);
//		soundListener.addSoundEvent(State.REFRESHING, R.raw.refreshing_sound);
//		mPullToRefreshListView.setOnPullEventListener(soundListener);
        
        
//		if (getUserVisibleHint()) {
//			isVisible = true;
//			refreshMode = FirstLoading;
//			requestJSON(refreshURL);
//		}
		return view;
	}
	
	private void requestJSON(String URL){
    	BaseTools.showlog("nodename="+nodename);
    	BaseTools.showlog("URL="+URL);
    	CharsetJsonRequest charsetJsonRequest = new CharsetJsonRequest(Request.Method.GET,URL,null,new Response.Listener<JSONObject>() {  
            @Override  
            public void onResponse(JSONObject response) {
            	//BaseTools.showlog("response="+response.toString());
        		if (!isVisible){
        			return;
        		}
        		if (refreshMode == PullToRefresh) {        			
        			pageIndex = 0;        			
                	//得到新闻更新数量
                	previousJsonStr = dataDB.getFromDataCache(nodename);
                	if (response.toString().equals(previousJsonStr)) {
                		updateCount = 0;
                	} else {
                		updateCount = getNewsUpdateCount(previousJsonStr,response.toString());
                    	dataDB.addToDataCache(nodename, response.toString());
                    	newsList.clear();
                    	addNewsList(response.toString());
                    	newsListAdapter.setList(newsList);
                	}
                	stopRefresh();
        		} else if (refreshMode == PullToLoadingMore) {
        			NewsListData newsListData = getNewsListDataByJson(response.toString());
        			if (newsListData == null)
        				return;
        			newsList.addAll(newsListData.newslist);
        			newsListAdapter.setList(newsList);
        			stopRefresh();
        		} else if (refreshMode == FirstLoading) {
        			pageIndex = 0;
//        			previousJsonStr = dataDB.getFromDataCache(nodename);
//        			updateCount = getNewsUpdateCount(previousJsonStr,response.toString()); //第一次加载不显示更新条数
                	dataDB.addToDataCache(nodename, response.toString());
                	addNewsList(response.toString());
                	newsListAdapter.setList(newsList);
                	newsListAdapter.notifyDataSetChanged();
        		    detail_loading.setVisibility(View.GONE);    
        		    if (NewsFragment.isAutoDownload == true) {
        		    	downloadNewsContent();
        		    }
        		}		       		    
            }  
        },new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError arg0) {
				if (!isVisible){
					return;
				}
				if (newsList.size() == 0) {
					String jsonStr = dataDB.getFromDataCache(nodename);
					addNewsList(jsonStr);
				}				
      		    newsListAdapter.setList(newsList);
      		    
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
				newsListAdapter.notifyDataSetChanged();
				updateNotify(updateCount);
				if (newsListAdapter.getList().isEmpty()) {
					no_data_ll.setVisibility(View.VISIBLE);
				}
			}
		}, 2000);
	}
	
	private void addNewsList(String json) {
		if (json == null)
			return;
		if (nodename != null) {
		  if(nodename.equals("今日推荐")) {
			TodayRecommendData todayRecommendData = getDataByJson(json);
			if (todayRecommendData == null) {
				return;
			}
			newsList.add(todayRecommendData.bigimages);
			newsList.addAll(todayRecommendData.newslist.list);
			newsList.addAll(todayRecommendData.imageslist.list);		
			newsList.addAll(todayRecommendData.newslist2.list);
			newsList.addAll(todayRecommendData.imageslist2.list);
		 } else {
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
	   }
	}
	
	private TodayRecommendData getDataByJson(String json) {
		TodayRecommendData data = null;
		try {
			Gson g = new Gson();
			data = g.fromJson(json, TodayRecommendData.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
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

	// 把json数据转换成ArrayList<News>，注意，这时bigimages会当成多条news数据而不是ArrayList
	private ArrayList<News> switchToNewsList(String json) {
		ArrayList<News> list = new ArrayList<News>();
		if (json != null) {
			if (nodename != null) {
				if (nodename.equals("今日推荐")) {
					TodayRecommendData todayRecommendData = getDataByJson(json);
					if (todayRecommendData != null) {
						list.addAll(todayRecommendData.bigimages);
						list.addAll(todayRecommendData.newslist.list);
						list.addAll(todayRecommendData.imageslist.list);
						list.addAll(todayRecommendData.newslist2.list);
						list.addAll(todayRecommendData.imageslist2.list);
					}
				} else {
					NewsListData newsListData = getNewsListDataByJson(json.toString());
					if (newsListData != null) {
						if (newsListData.bigimagelist != null&& newsListData.bigimagelist.size() > 0) {
							list.addAll(newsListData.bigimagelist);
						}
						if (newsListData.newslist != null) {
							list.addAll(newsListData.newslist);
						}
					}
				}
			}
		}
		return list;
	}
	
	/* 初始化通知栏目*/
	private void updateNotify(final int num) {
		 notify_text.setText(String.format(context.getString(R.string.ss_pattern_update), num));
		 notify_view.setVisibility(View.VISIBLE);
		 new Handler().postDelayed(new Runnable() {
			 @Override
			 public void run() {
			     // TODO Auto-generated method stub
			     notify_view.setVisibility(View.GONE);
			 }
		 }, 1000);
	}
	
	//自动下载新闻内容
	private void downloadNewsContent() {
		new Thread(new Runnable(){
			@Override
			public void run() {
				int newsIndex = 0;
				while(newsIndex < newsList.size()) {
					Object obj = newsList.get(newsIndex);
					if (obj instanceof ArrayList) {
						newsIndex++;
						continue;
					}
					News news = (News) obj;
					if (news.getNewstype().equals("0")) {
						if (!FileUtil.isNewsFileExist(news.getNewstitle())) {
							String newsUrl = (news.getNewsurl()).startsWith("http")? news.getNewsurl():Util.getEastDayURL(news.getNewsurl());
//							FileUtil.downloadNewsToSDcard(news.newstitle,newsUrl);
						    HttpUtil.saveFileToLocal(newsUrl,FileUtil.getNewsPath() + "/" + news.getNewstitle(),"utf-8");
						}
					} else if (news.getNewstype().equals("5")) {
						
					}
					newsIndex++;
				}	
				Intent mIntent = new Intent(NewsFragment.DownloadOK);
				mIntent.putExtra("nodeName",nodename);  
				context.sendBroadcast(mIntent);
			}
			
		}).start();
	}
	
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//		menu.add(0, MENU_DISABLE_SCROLL, 0, mPullToRefreshListView.isScrollingWhileRefreshingEnabled() ? "禁止刷新时滚动" : "允许刷新时滚动");
//		menu.add(0, MENU_SET_MODE, 1, mPullToRefreshListView.getMode() == Mode.BOTH ? "禁止上拉加载" : "允许上拉加载");
		return;
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.add(0, MENU_DISABLE_SCROLL, 0, mPullToRefreshListView.isScrollingWhileRefreshingEnabled() ? "禁止刷新时滚动" : "允许刷新时滚动");
		menu.add(0, MENU_SET_MODE, 1, mPullToRefreshListView.getMode() == Mode.BOTH ? "禁止上拉加载" : "允许上拉加载");
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

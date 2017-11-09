package com.huaxun.life.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.huaxun.R;
import com.huaxun.base.BaseFragment;
import com.huaxun.db.DataDB;
import com.huaxun.life.activity.LifeNewsDetailActivity;
import com.huaxun.life.bean.LifeNews;
import com.huaxun.life.bean.LifeNewsList;
import com.huaxun.tool.NewsUrls;
import com.huaxun.tool.Options;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.nostra13.universalimageloader.core.ImageLoader;

public class LifeListFragment extends BaseFragment {
	private Context mContext;
	private PullToRefreshListView mListView;
	private LinearLayout loading_ll;
	private LinearLayout no_data_ll;
	private DataDB dataDB;
	private NewsAdapter mAdapter;
	private ArrayList<LifeNews> mList = new ArrayList<LifeNews>();
	
	private final String NEWS_PREFIX = NewsUrls.LIFE_NEWS;
	
	private int channelId; // 新闻 = 5，社区 = 27，房产 = 23,娱乐 = 21，汽车 = 24
	private String nodeName;
	private int pageIndex = 1;
	
	private LinearLayout footerView;    //最后一条listview
	
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
					loadLifeNewsInfo(true);
				}}, 200);
		} else {
			isVisible = false;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.mContext = this.getActivity();
		dataDB = DataDB.getInstance(mContext);
		Bundle args = getArguments();
		channelId = args.getInt("channelId");
		nodeName = args != null ? args.getString("nodeName") : "";
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.news_list_fragment, container, false);
		mListView = (PullToRefreshListView) view.findViewById(R.id.pull_to_refresh_listview);
		loading_ll = (LinearLayout) view.findViewById(R.id.loading_ll);
		no_data_ll = (LinearLayout) view.findViewById(R.id.no_data_ll);
		mAdapter = new NewsAdapter();
		mListView.setAdapter(mAdapter);
		mListView.setMode(Mode.BOTH);
		
		//在Scroll或Fling的时候不要去加载图片
		//mListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
		mListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {}
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (isVisible) {
					onScrollShowOrHide(firstVisibleItem, mListView.getRefreshableView());
				}				
			}
		});
		
		
		mListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
	  	        String str = DateUtils.formatDateTime(mContext, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
				if (refreshView.isHeaderShown()){
  	        	    mListView.getLoadingLayoutProxy().setRefreshingLabel("正在刷新...");
  	        	    mListView.getLoadingLayoutProxy().setPullLabel("下拉刷新");
  	        	    mListView.getLoadingLayoutProxy().setReleaseLabel("释放开始刷新");
  	        	    refreshView.getLoadingLayoutProxy().setLastUpdatedLabel("最后更新时间:" + str);
  	        	    pageIndex = 1;
					loadLifeNewsInfo(true);
				} else {
				    mListView.getLoadingLayoutProxy().setRefreshingLabel("正在加载...");
					mListView.getLoadingLayoutProxy().setPullLabel("上拉加载");
					mListView.getLoadingLayoutProxy().setReleaseLabel("释放开始加载");
   	                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel("最后加载时间:" + str);
   	                loadLifeNewsInfo(false);
				}
			}
		});
		
		mListView.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {
			@Override
			public void onLastItemVisible() {
				if (pageIndex!=0) {
					//滑动到底部自动刷新
					loadLifeNewsInfo(false);
				} else {
					//没有下一页了
					mListView.onRefreshComplete();
					if (footerView == null) {
						footerView = (LinearLayout) View.inflate(mContext, R.layout.item_pull_to_refresh_footer, null);
						TextView footer = (TextView) footerView.findViewById(R.id.footer);
						footer.setText("到底了呢-共" + mList.size() + "条新闻");
						mListView.getRefreshableView().addFooterView(footerView);  
					}
				}
			}
		});
		
		return view;
	}
	
	/**
	 * 加载生活新闻信息
	 */
	public void loadLifeNewsInfo(final boolean clean) {
		String url = NEWS_PREFIX + channelId + "&requiredPage=" + pageIndex;
		new HttpUtils().send(HttpRequest.HttpMethod.GET, url, new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				try {
					if (!isVisible){
						return;
					}
					if (pageIndex == 1) {
						dataDB.addToDataCache(nodeName, responseInfo.result);
					}				
					if (clean) {
						mList.clear();
					}
					addNewsList(responseInfo.result);				
					stopRefresh();	
				} catch(Exception e) {
					e.printStackTrace();
				}										
			}
			@Override
			public void onFailure(HttpException error, String msg) {
				if (!isVisible){
					return;
				}
				if (mList.size() == 0) {
					String jsonStr = dataDB.getFromDataCache(nodeName);
					addNewsList(jsonStr);
				}
				stopRefresh();
			}
		});
	}
	
	private void addNewsList(String json) {
		if (json == null)
			return;
		Gson gson = new Gson();
		LifeNewsList mLifeNewsList = gson.fromJson(json, LifeNewsList.class);
		if (mLifeNewsList != null) {
			ArrayList<LifeNews> list = mLifeNewsList.getList();	
			if (list != null && list.size() > 0) {
				reOrderList(list);  //重新排序，时间点近的排在前面
				mList.addAll(list);
				pageIndex++;
			} else {
				pageIndex = 0;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void reOrderList(ArrayList<LifeNews> list) {
		Collections.sort(list, new SortByNewsId());
	}
	
	@SuppressWarnings("rawtypes")
	class SortByNewsId implements Comparator {
		public int compare(Object o1, Object o2) {
			LifeNews news1 = (LifeNews) o1;
			LifeNews news2 = (LifeNews) o2;
			return news2.getNewsId().compareTo(news1.getNewsId());
		}
	}
	
	private void stopRefresh() {
		mListView.postDelayed(new Runnable() {
			@Override
			public void run() {
				mListView.onRefreshComplete();
				mAdapter.notifyDataSetChanged();
				loading_ll.setVisibility(View.GONE);
				if (mList.isEmpty()) {
					no_data_ll.setVisibility(View.VISIBLE);
				}				
			}
		}, 2000);
	}
	
	class NewsAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mList.size();
		}

		@Override
		public Object getItem(int position) {
			return mList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = View.inflate(mContext, R.layout.item_life_news_lv, null);
				holder = new ViewHolder();
				holder.imgNewsPic = (ImageView) convertView.findViewById(R.id.imgNewsPic);
				holder.title = (TextView) convertView.findViewById(R.id.tvTitle);
				holder.description = (TextView) convertView.findViewById(R.id.tvDescription);
				holder.date_text = (TextView) convertView.findViewById(R.id.date_text);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
				
			LifeNews item = mList.get(position);
			if ( !TextUtils.isEmpty(item.getImage().getSrc()) ) {
				ImageLoader.getInstance().displayImage(item.getImage().getSrc(), holder.imgNewsPic, Options.getListOptions());
			}
			holder.title.setText(item.getTitle());
			holder.description.setText(item.getContent());
			holder.date_text.setText(item.getDate());
			if (dataDB.queryIdCache(item.getNewsId())) {
				// 看过此条新闻，字体为灰色
				holder.title.setTextColor(mContext.getResources().getColor(R.color.news_read_ok));
				holder.description.setTextColor(mContext.getResources().getColor(R.color.news_read_ok));
				holder.date_text.setTextColor(mContext.getResources().getColor(R.color.news_read_ok));
			}
			
			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dataDB.addIdCache(mList.get(position).getNewsId());
					mAdapter.notifyDataSetChanged();
					Intent intent = new Intent(mContext, LifeNewsDetailActivity.class);
					Bundle bundle = new Bundle();
					bundle.putSerializable("lifeNews", mList.get(position));
					bundle.putString("nodeName", nodeName);
					intent.putExtras(bundle);
					mContext.startActivity(intent);
					getActivity().overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
				}
			});
			
			return convertView;
		}
		
	}

	class ViewHolder {
		ImageView imgNewsPic;
		TextView title;
		TextView date_text;
		TextView description;
	}

}
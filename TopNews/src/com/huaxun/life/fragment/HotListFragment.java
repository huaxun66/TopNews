package com.huaxun.life.fragment;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Xml;
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

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.huaxun.R;
import com.huaxun.base.BaseFragment;
import com.huaxun.db.DataDB;
import com.huaxun.fragment.LifeNewFragment;
import com.huaxun.life.activity.HotNewsDetailActivity;
import com.huaxun.life.bean.HotNews;
import com.huaxun.tool.NewsUrls;
import com.huaxun.tool.Options;
import com.huaxun.utils.FileUtil;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.nostra13.universalimageloader.core.ImageLoader;

public class HotListFragment extends BaseFragment {
	private Context mContext;
	private PullToRefreshListView mListView;
	private LinearLayout loading_ll;
	private LinearLayout no_data_ll;
	private DataDB dataDB;
	private NewsAdapter mAdapter;
	private ArrayList<HotNews> mList = new ArrayList<HotNews>();
	
	private int channelId;    //66热门   67最新    68推荐	
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
		if (channelId == LifeNewFragment.NODE_TYPE_RM) {
			mListView.setMode(Mode.PULL_FROM_START);  //只能下拉刷新
		}
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
		String url = "";
		if (channelId == LifeNewFragment.NODE_TYPE_RM) {
			url = NewsUrls.HOT_NEWS + "hot/40";
		} else if (channelId == LifeNewFragment.NODE_TYPE_ZX) {
			url = NewsUrls.HOT_NEWS + "recent/paged/" + pageIndex + "/30";
		} else if (channelId == LifeNewFragment.NODE_TYPE_TJ) {
			url = NewsUrls.HOT_NEWS + "recommend/paged/" + pageIndex + "/30";
		}
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
					if (channelId == LifeNewFragment.NODE_TYPE_RM) {
						mList.clear();
					}
					if (clean) {
						mList.clear();
					}
					ArrayList<HotNews> list = parseHotsNewsXml(responseInfo.result);
					if (list.size() > 0) {
						reOrderList(list);  //重新排序，时间点近的排在前面
						mList.addAll(list);
						pageIndex++;
					} else {
						pageIndex = 0;
					}					
					stopRefresh();	
				} catch(Exception e) {
					e.printStackTrace();
				}										
			}
			@Override
			public void onFailure(HttpException error, String msg) {
				try {
					if (!isVisible){
						return;
					}
					if (mList.size() == 0) {
						String jsonStr = dataDB.getFromDataCache(nodeName);
						ArrayList<HotNews> list = parseHotsNewsXml(jsonStr);
						if (list.size() > 0) {
							reOrderList(list);  //重新排序，时间点近的排在前面
							mList.addAll(list);
							pageIndex++;
						} else {
							pageIndex = 0;
						}
					}
					stopRefresh(); 
				} catch (Exception e) {
					e.printStackTrace();
				}				
			}
		});
	}
	
	
	/**
	 * 解析热门新闻 
	 * @param in
	 * @throws IOException 
	 * @throws XmlPullParserException 
	 */
	private ArrayList<HotNews> parseHotsNewsXml(String result) throws XmlPullParserException, IOException {
		ArrayList<HotNews> list = new ArrayList<HotNews>();
		
		XmlPullParser parser = Xml.newPullParser();		
		parser.setInput(new StringReader(result));
		int event = parser.getEventType();
		HotNews news = null;
		boolean flag = false;
		while (event != XmlPullParser.END_DOCUMENT) {
			switch (event) {
			case XmlPullParser.START_DOCUMENT:
				break;
			case XmlPullParser.START_TAG: // <entry>
				if ("entry".equals(parser.getName())) {
					flag = true;
					news = new HotNews();
				}
				if (flag) {
					if ("id".equals(parser.getName())) {
						String id = parser.nextText();
						news.setHotId(id);
					} else if ("title".equals(parser.getName())) {
						String title = parser.nextText();
						news.setTitle(title);
					} else if ("summary".equals(parser.getName())) {
						String summary = parser.nextText();
						news.setSummary(summary);
					} else if ("published".equals(parser.getName())) {
						String published = parser.nextText();
						news.setPublished(published);
					} else if ("updated".equals(parser.getName())) {
						String updated = parser.nextText();
						news.setUpdated(updated);
					} else if ("link".equals(parser.getName())) {
						String link = parser.getAttributeValue(1);  //获得第二个属性值
						news.setLink(link);
					} else if ("diggs".equals(parser.getName())) {
						String diggs = parser.nextText();
						news.setDiggs(diggs);
					} else if ("views".equals(parser.getName())) {
						String views = parser.nextText();
						news.setViews(views);
					} else if ("topicIcon".equals(parser.getName())) {
						String topicIcon = parser.nextText();
						news.setTopicIcon(topicIcon);
					} else if ("comments".equals(parser.getName())) {
						String comments = parser.nextText();
						news.setComments(comments);
					} else if ("sourceName".equals(parser.getName())) {
						String sourceName = parser.nextText();
						news.setSourceName(sourceName);
					}
				}
				break;
			case XmlPullParser.TEXT:
				break;
			case XmlPullParser.END_TAG:  // </entry>
				if ("entry".equals(parser.getName())) {
					list.add(news);
				}
				break;
			}
			event = parser.next();
		}		
		return list;
	}
	
	@SuppressWarnings("unchecked")
	private void reOrderList(ArrayList<HotNews> list) {
		Collections.sort(list, new SortByNewsId());
	}
	
	@SuppressWarnings("rawtypes")
	class SortByNewsId implements Comparator {
		public int compare(Object o1, Object o2) {
			HotNews news1 = (HotNews) o1;
			HotNews news2 = (HotNews) o2;
			return news2.getHotId().compareTo(news1.getHotId());
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
	
	private String getTime(String date) {
		return date.substring(5, 16).replace("T", " ");
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
				convertView = View.inflate(mContext, R.layout.news_list_item_02, null);
				holder = new ViewHolder(convertView);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
				
			HotNews item = mList.get(position);
			if (!TextUtils.isEmpty(item.getTopicIcon())) {
				ImageLoader.getInstance().displayImage(item.getTopicIcon(), holder.normal_iv, Options.getListOptions());
			}
			holder.normal_title.setText(item.getTitle());
			holder.normal_time.setText(getTime(item.getPublished()));
			if (dataDB.queryIdCache(item.getHotId())) {
				// 看过此条新闻，字体为灰色
				holder.normal_title.setTextColor(mContext.getResources().getColor(R.color.news_read_ok));
				holder.normal_time.setTextColor(mContext.getResources().getColor(R.color.news_read_ok));
			}
			int resId = -1;
			if (channelId == LifeNewFragment.NODE_TYPE_RM) {
				resId = R.drawable.article_tip_hot;
			} else if (channelId == LifeNewFragment.NODE_TYPE_ZX) {
				resId = R.drawable.article_tip_zx;
			} else if (channelId == LifeNewFragment.NODE_TYPE_TJ) {
				resId = R.drawable.article_tip_promotion;
			}
			if (resId == -1) {
				holder.normal_mark.setVisibility(View.INVISIBLE);
			} else {
				holder.normal_mark.setVisibility(View.VISIBLE);
				holder.normal_mark.setImageResource(resId);
			}
			if (FileUtil.isNewsFileExist(item.getTitle())){
				holder.local_tv.setVisibility(View.VISIBLE);
			}else{
				holder.local_tv.setVisibility(View.INVISIBLE);
			}
			
			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dataDB.addIdCache(mList.get(position).getHotId());
					mAdapter.notifyDataSetChanged();
					Intent intent = new Intent(mContext, HotNewsDetailActivity.class);
					Bundle bundle = new Bundle();
					bundle.putSerializable("HotNews", mList.get(position));
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
		public ImageView normal_iv;
		public TextView normal_title;
		public TextView normal_time;
		public ImageView normal_mark;
		public TextView local_tv;

		public ViewHolder(View view) {
			normal_iv = (ImageView) view.findViewById(R.id.normal_iv);
			normal_title = (TextView) view.findViewById(R.id.normal_tv_title);
			normal_time = (TextView) view.findViewById(R.id.normal_tv_time);
			normal_mark = (ImageView) view.findViewById(R.id.normal_mark);
			local_tv = (TextView) view.findViewById(R.id.localTextViewID);
		}
	}

}
package com.huaxun.life.fragment;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.huaxun.R;
import com.huaxun.db.DataDB;
import com.huaxun.life.activity.BlogDetailActivity;
import com.huaxun.life.bean.BlogItem;
import com.huaxun.tool.NewsUrls;
import com.huaxun.tool.Options;
import com.huaxun.view.CircleImageView;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.nostra13.universalimageloader.core.ImageLoader;


/**
 * 最新博客
 * 
 * @author zhou.ni 2015年5月17日
 */
public class FreshBlogLayout extends RelativeLayout{
	private BlogListFragment blogListFragment;
	private Context mContext;	
	private LinearLayout loading_ll;
	private LinearLayout no_data_ll;
	private DataDB dataDB;
	private PullToRefreshListView freshListView;
	
	private static final String FRESH_PATH = NewsUrls.FRESH_BLOG;
	private int pageIndex = 1;	//下一页页码
	private boolean flag = false;
	private String nodeName = "最新博客";
	
	private List<BlogItem> mList = new ArrayList<BlogItem>();
	private FreshBolgAdapter adapter;
	
	private LinearLayout footerView;    //最后一条listview
	
	
	public FreshBlogLayout(BlogListFragment blogListFragment) {
		super(blogListFragment.getActivity());
		mContext = blogListFragment.getActivity();
		this.blogListFragment = blogListFragment;
		dataDB = DataDB.getInstance(mContext);
		initView();
	}

	public FreshBlogLayout(BlogListFragment blogListFragment, AttributeSet attrs) {
		super(blogListFragment.getActivity(), attrs);
		mContext = blogListFragment.getActivity();
		this.blogListFragment = blogListFragment;
		dataDB = DataDB.getInstance(mContext);
		initView();
	}

	public FreshBlogLayout(BlogListFragment blogListFragment, AttributeSet attrs, int defStyle) {
		super(blogListFragment.getActivity(), attrs, defStyle);
		mContext = blogListFragment.getActivity();
		this.blogListFragment = blogListFragment;
		dataDB = DataDB.getInstance(mContext);
		initView();
	}
	
	/**
	 * 初始化控件
	 */
	private void initView() {
		View view = View.inflate(getContext(), R.layout.news_list_fragment, this);
		loading_ll = (LinearLayout) view.findViewById(R.id.loading_ll);
		no_data_ll = (LinearLayout) view.findViewById(R.id.no_data_ll);
		freshListView = (PullToRefreshListView) view.findViewById(R.id.pull_to_refresh_listview);
		adapter = new FreshBolgAdapter();
		freshListView.setAdapter(adapter);
		
		freshListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				BlogItem item = mList.get(position-1);
				Intent intent = new Intent(mContext, BlogDetailActivity.class);
				Bundle info = new Bundle();
				info.putSerializable("item", item);
				intent.putExtras(info);
				mContext.startActivity(intent);
				((Activity) mContext).overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
			}
		});
		
		freshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
	  	        String str = DateUtils.formatDateTime(mContext, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
				if (refreshView.isHeaderShown()){
					freshListView.getLoadingLayoutProxy().setRefreshingLabel("正在刷新...");
					freshListView.getLoadingLayoutProxy().setPullLabel("下拉刷新");
					freshListView.getLoadingLayoutProxy().setReleaseLabel("释放开始刷新");
  	        	    refreshView.getLoadingLayoutProxy().setLastUpdatedLabel("最后更新时间:" + str);
  	        	    pageIndex = 1;
  	        	    flag = false;
					loadFreshBlogInfo(true);
				} else {
					freshListView.getLoadingLayoutProxy().setRefreshingLabel("正在加载...");
					freshListView.getLoadingLayoutProxy().setPullLabel("上拉加载");
					freshListView.getLoadingLayoutProxy().setReleaseLabel("释放开始加载");
   	                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel("最后加载时间:" + str);
   	                loadFreshBlogInfo(false);
				}
			}
		});
		
		freshListView.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {
			@Override
			public void onLastItemVisible() {
				if (pageIndex != 0) {
					//滑动到底部自动刷新
					flag = false;
					loadFreshBlogInfo(false);
				} else {
					//没有下一页了
					freshListView.onRefreshComplete();
					if (footerView == null) {
						footerView = (LinearLayout) View.inflate(mContext, R.layout.item_pull_to_refresh_footer, null);
						TextView footer = (TextView) footerView.findViewById(R.id.footer);
						footer.setText("到底了呢-共" + mList.size() + "条新闻");
						freshListView.getRefreshableView().addFooterView(footerView);  
					}
				}
			}
		});
		
		//在Scroll或Fling的时候不要去加载图片
		//freshListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
		freshListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {}
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				blogListFragment.onScrollShowOrHide(firstVisibleItem, freshListView.getRefreshableView());			
			}
		});
		
	}
	
	
	/**
	 * 加载最新博客信息
	 */
	public void loadFreshBlogInfo(final boolean clean) {
		String url = FRESH_PATH + pageIndex + "/10";
		new HttpUtils().send(HttpRequest.HttpMethod.GET, url, new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				try {
					if (pageIndex == 1) {
						dataDB.addToDataCache(nodeName, responseInfo.result);
					}
					if (clean) {
						mList.clear();
					}
					ArrayList<BlogItem> list = parseFreshBolgXml(responseInfo.result);
					if (list.size() > 0) {
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
					if (mList.size() == 0) {
						String jsonStr = dataDB.getFromDataCache(nodeName);
						ArrayList<BlogItem> list = parseFreshBolgXml(jsonStr);
						if (list.size() > 0) {
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
	 * 解析最新博客
	 * @param in
	 * @throws IOException 
	 * @throws XmlPullParserException 
	 */
	private ArrayList<BlogItem> parseFreshBolgXml(String xml) throws XmlPullParserException, IOException {
		ArrayList<BlogItem> list = new ArrayList<BlogItem>();
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(new StringReader(xml));
		int event = parser.getEventType();
		BlogItem item = null;
		
		while (event != XmlPullParser.END_DOCUMENT) {
			switch (event) {
			case XmlPullParser.START_DOCUMENT:
				break;
			case XmlPullParser.START_TAG:
				if ("entry".equals(parser.getName())) {
					flag = true;
					item = new BlogItem();
				}
				if (flag) {
					if ("id".equals(parser.getName())) {
						String id = parser.nextText();
						item.setBlogId(id);
					} else if ("title".equals(parser.getName())) {
						String title = parser.nextText();
						item.setTitle(title);
					} else if ("summary".equals(parser.getName())) {
						String summary = parser.nextText();
						item.setSummary(summary);
					} else if ("published".equals(parser.getName())) {
						String published = parser.nextText();
						item.setPublished(published);
					} else if ("updated".equals(parser.getName())) {
						String updated = parser.nextText();
						item.setUpdated(updated);
					}else if("name".equals(parser.getName())){
						String name = parser.nextText();
						item.setName(name);
					}else if("uri".equals(parser.getName())){
						String uri = parser.nextText();
						item.setUri(uri);
					}else if("avatar".equals(parser.getName())){
						String avatar = parser.nextText();
						item.setAvatar(avatar);
					} else if ("link".equals(parser.getName())) {
						String href = parser.getAttributeValue(1);
						item.setHref(href);
					} else if ("diggs".equals(parser.getName())) {
						String diggs = parser.nextText();
						item.setDiggs(diggs);
					} else if ("views".equals(parser.getName())) {
						String views = parser.nextText();
						item.setViews(views);
					} else if ("comments".equals(parser.getName())) {
						String comments = parser.nextText();
						item.setComments(comments);
					}
				}
				break;
			case XmlPullParser.TEXT:
				break;
			case XmlPullParser.END_TAG:
				if ("entry".equals(parser.getName())) {
					list.add(item);
				}
				break;
			}
			event = parser.next();
		}
		return list;
	}
	
	private void stopRefresh() {
		freshListView.postDelayed(new Runnable() {
			@Override
			public void run() {
				freshListView.onRefreshComplete();
				adapter.notifyDataSetChanged();
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
	
	
	class FreshBolgAdapter extends BaseAdapter {

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
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if ( convertView == null ) {
				convertView = View.inflate(mContext, R.layout.item_fresh_bolg_lv, null);
				holder = new ViewHolder();
				holder.title = (TextView) convertView.findViewById(R.id.title);
				holder.uPic = (CircleImageView) convertView.findViewById(R.id.image);
				holder.name = (TextView) convertView.findViewById(R.id.name);
				holder.published = (TextView) convertView.findViewById(R.id.publish);
				holder.summary = (TextView) convertView.findViewById(R.id.summary);
				holder.viewsTv = (TextView) convertView.findViewById(R.id.views_tx);
				holder.diggsTv = (TextView) convertView.findViewById(R.id.diggs_tx);
				holder.commentsTv = (TextView) convertView.findViewById(R.id.comment_tx);
				holder.diggs = (LinearLayout) convertView.findViewById(R.id.hots_diggs);
				holder.views = (LinearLayout) convertView.findViewById(R.id.hots_views);
				holder.comment = (LinearLayout) convertView.findViewById(R.id.hots_comment);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			BlogItem item = mList.get(position);
			holder.title.setText(item.getTitle());
			holder.name.setText(item.getName());
			holder.summary.setText(item.getSummary());
			
			if( !TextUtils.isEmpty(item.getAvatar()) ) {
				ImageLoader.getInstance().displayImage(item.getAvatar(), holder.uPic, Options.getListOptions());
			}
			
			if ( !TextUtils.isEmpty(item.getPublished()) ) {
				holder.published.setText(getTime(item.getPublished()));
			} 
			
			if ( !TextUtils.isEmpty(item.getViews()) ) {
				holder.viewsTv.setText(item.getViews());
			} else {
				holder.viewsTv.setText("");
			}
			
			if ( !TextUtils.isEmpty(item.getComments()) ) {
				holder.commentsTv.setText(item.getComments());
			} else {
				holder.commentsTv.setText("");
			}

			if ( !TextUtils.isEmpty(item.getDiggs()) ) {
				holder.diggsTv.setText(item.getDiggs());
			} else {
				holder.diggsTv.setText("");
			}
			
//			if(position%2 == 0){
//				convertView.setBackgroundResource(R.color.white);
//			}else{
//				convertView.setBackgroundResource(R.color.freshblogsbg);
//			}
			
			return convertView;
		}

	}
	
	static class ViewHolder {
		TextView title;
		CircleImageView uPic;
		TextView summary;
		TextView published;
		TextView viewsTv;
		TextView diggsTv;
		TextView name;
		TextView commentsTv;
		TextView textViews;
		LinearLayout views;
		LinearLayout diggs;
		LinearLayout comment;
	}

	
}

package com.huaxun.life.activity;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Xml;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.huaxun.R;
import com.huaxun.db.DataDB;
import com.huaxun.life.bean.BolgComment;
import com.huaxun.tool.NewsUrls;
import com.huaxun.utils.Util;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;


/**
 * 新闻评论
 * 
 * @author zhou.ni 2015年5月18日
 */
public class NewsCommentActivity extends Activity {
	private LinearLayout loading_ll;
	private LinearLayout no_data_ll;
	private TextView topBack,topTitle;
	private PullToRefreshListView mListView;
	
	private List<BolgComment> bolgList = new ArrayList<BolgComment>();
	private BolgCommentAdapter adapter;
	private DataDB dataDB;
	
	private boolean flag = false;
	private int pageIndex = 1;	//下一页页码
	private String id;
	private boolean blogComment = false;
	private LinearLayout footerView;    //最后一条listview
	
	private static String STR_COMMENT = NewsUrls.LIFE_COMMENT;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Util.setTranslucent(this);
		setContentView(R.layout.activity_bolg_comment);
		getWindow().setBackgroundDrawable(null);
		dataDB = DataDB.getInstance(this);
		
		Intent intent = getIntent();
		id = intent.getStringExtra("id");
		blogComment = intent.getBooleanExtra("blogComment", false);
		if (blogComment == true) {
			STR_COMMENT = NewsUrls.BLOG_COMMENT;
		}
		
		initLayout();
	}

	/**
	 * 初始化控件
	 */
	private void initLayout() {
		topBack = (TextView)this.findViewById(R.id.topBack);
		topTitle = (TextView)this.findViewById(R.id.topTitle);
		loading_ll = (LinearLayout) this.findViewById(R.id.loading_ll);
		no_data_ll = (LinearLayout) this.findViewById(R.id.no_data_ll);
		mListView = (PullToRefreshListView) this.findViewById(R.id.list);
		topTitle.setText("评论");
		topBack.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				finish();
				overridePendingTransition(R.anim.scale_out, 0);
			}
		});
		adapter = new BolgCommentAdapter();
		mListView.setAdapter(adapter);
		mListView.setMode(Mode.BOTH);
		
		
		mListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				flag = false;
	  	        String str = DateUtils.formatDateTime(NewsCommentActivity.this, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
				if (refreshView.isHeaderShown()){
  	        	    mListView.getLoadingLayoutProxy().setRefreshingLabel("正在刷新...");
  	        	    mListView.getLoadingLayoutProxy().setPullLabel("下拉刷新");
  	        	    mListView.getLoadingLayoutProxy().setReleaseLabel("释放开始刷新");
  	        	    refreshView.getLoadingLayoutProxy().setLastUpdatedLabel("最后更新时间:" + str);
  	        	    pageIndex = 1;
  	        	    loadBolgCommentInfo(true);
				} else {
				    mListView.getLoadingLayoutProxy().setRefreshingLabel("正在加载...");
					mListView.getLoadingLayoutProxy().setPullLabel("上拉加载");
					mListView.getLoadingLayoutProxy().setReleaseLabel("释放开始加载");
   	                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel("最后加载时间:" + str);
   	                loadBolgCommentInfo(false);
				}
			}
		});
		
		mListView.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {
			@Override
			public void onLastItemVisible() {
				if (pageIndex!=0) {
					//滑动到底部自动刷新
					flag = false;
					loadBolgCommentInfo(false);
				} else {
					//没有下一页了
					mListView.onRefreshComplete();
					if (footerView == null) {
						footerView = (LinearLayout) View.inflate(NewsCommentActivity.this, R.layout.item_pull_to_refresh_footer, null);
						TextView footer = (TextView) footerView.findViewById(R.id.footer);
						footer.setText("到底了呢-共" + bolgList.size() + "条新闻");
						mListView.getRefreshableView().addFooterView(footerView);  
					}
				}
			}
		});
		
		loadBolgCommentInfo(true);
	}
	

	
	/**
	 * 加载评论内容
	 * @param url
	 */
	public void loadBolgCommentInfo(final boolean clean) {
		String url = STR_COMMENT + id + "/comments/" + pageIndex +"/20";
		new HttpUtils().send(HttpRequest.HttpMethod.GET, url, new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				try {
					if (pageIndex == 1) {
						dataDB.addToDataCache("comment_"+id, responseInfo.result);
					}
					if (clean) {
						bolgList.clear();
					}
					ArrayList<BolgComment> list = parseCommentXml(responseInfo.result);
					if (list.size() > 0) {
						bolgList.addAll(list);
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
					if (bolgList.size() == 0) {
						String jsonStr = dataDB.getFromDataCache("comment_"+id);
						ArrayList<BolgComment> list = parseCommentXml(jsonStr);
						if (list.size() > 0) {
							bolgList.addAll(list);
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
	
	private void stopRefresh() {
		mListView.postDelayed(new Runnable() {
			@Override
			public void run() {
				mListView.onRefreshComplete();
				adapter.notifyDataSetChanged();
				loading_ll.setVisibility(View.GONE);
				if (bolgList.isEmpty()) {
					no_data_ll.setVisibility(View.VISIBLE);
				}				
			}
		}, 2000);
	}
	
	/**
	 * 解析评论内容
	 * 
	 * @param in
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private ArrayList<BolgComment> parseCommentXml(String xml) throws XmlPullParserException, IOException {
		ArrayList<BolgComment> list = new ArrayList<BolgComment>();
		
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(new StringReader(xml));
		int event = parser.getEventType();
		BolgComment item = null;
		
		while (event != XmlPullParser.END_DOCUMENT) {
			switch (event) {
			case XmlPullParser.START_DOCUMENT:
				break;
				
			case XmlPullParser.START_TAG:
				if ("entry".equals(parser.getName())) {
					flag = true;
					item = new BolgComment();
				}
				if (flag) {
					if ("id".equals(parser.getName())) {
						String id = parser.nextText();
						item.setId(id);
					} else if ("title".equals(parser.getName())) {
						String title = parser.nextText();
						item.setTitle(title);
					} else if ("published".equals(parser.getName())) {
						String published = parser.nextText();
						item.setPublished(published);
					} else if ("updated".equals(parser.getName())) {
						String updated = parser.nextText();
						item.setUpdated(updated);
					} else if("author".equals(parser.getName())){
						
					} else if ("name".equals(parser.getName())) {
						String name = parser.nextText();
						item.setName(name);
					} else if ("uri".equals(parser.getName())) {
						String uri = parser.nextText();
						item.setUri(uri);
					} else if ("content".equals(parser.getName())) {
						String content = parser.nextText();
						item.setContent(content);
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
	
	private String getTime(String date) {
		return date.substring(5, 16).replace("T", " ");
	}
	
	public class BolgCommentAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return bolgList.size();
		}

		@Override
		public Object getItem(int position) {
			return bolgList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = View.inflate(NewsCommentActivity.this, R.layout.item_bolg_comment_lv, null);
				holder.name = (TextView) convertView.findViewById(R.id.name);
				holder.time = (TextView) convertView.findViewById(R.id.time);
				holder.content = (TextView) convertView.findViewById(R.id.content);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			BolgComment item = bolgList.get(position);
			holder.name.setText(item.getName());
			if (!TextUtils.isEmpty(item.getPublished())) {
				holder.time.setText(getTime(item.getPublished()));
			}
			holder.content.setText(Html.fromHtml(item.getContent()));
			return convertView;
		}
		
	    class ViewHolder {
			TextView name;
			TextView time;
			TextView content;
		}
		
	}
	
}

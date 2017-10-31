package com.huaxun.life.activity;

import java.io.IOException;
import java.io.StringReader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.huaxun.R;
import com.huaxun.db.FavoriteDB;
import com.huaxun.life.bean.BlogItem;
import com.huaxun.tool.BaseTools;
import com.huaxun.tool.NewsUrls;
import com.huaxun.utils.Util;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

/**
 * 博客详情
 * 
 * @author zhou.ni 2015年4月12日
 */
public class BlogDetailActivity extends Activity implements OnClickListener{
	private TextView topBack,topTitle;
	private ProgressBar progressBar;
	private LinearLayout loading_ll;
	private LinearLayout no_data_ll;
	private LinearLayout top, comment, favorite, share;
	private ImageView favorite_news;
	private WebView mWeb;	
	private BlogItem item;
	private String PATH = NewsUrls.BLOG_DETAIL;
	private FavoriteDB favoriteDB;
	private String result;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Util.setTranslucent(this);
		setContentView(R.layout.activity_news_detail);
		getWindow().setBackgroundDrawable(null);
		favoriteDB = FavoriteDB.getInstance();
		
		Intent intent = this.getIntent(); 
		item = (BlogItem) intent.getSerializableExtra("item");
		
		initView();
	}
	
	/**
	 * 初始化控件
	 */
	private void initView() {
		topBack = (TextView) findViewById(R.id.topBack);
		topTitle = (TextView) findViewById(R.id.topTitle);
		progressBar = (ProgressBar)this.findViewById(R.id.webview_progressBar);
		loading_ll = (LinearLayout) findViewById(R.id.loading_ll);
		no_data_ll = (LinearLayout) findViewById(R.id.no_data_ll);
		mWeb = (WebView) findViewById(R.id.webview);
		top = (LinearLayout) findViewById(R.id.toolbar_top);
		comment = (LinearLayout) findViewById(R.id.toolbar_comment);
		favorite = (LinearLayout) findViewById(R.id.toolbar_favorite);
		share = (LinearLayout) findViewById(R.id.toolbar_share);
		favorite_news = (ImageView) findViewById(R.id.favorite_news);
		topBack.setOnClickListener(this);
		top.setOnClickListener(this);
		comment.setOnClickListener(this);
		favorite.setOnClickListener(this);
		share.setOnClickListener(this);
		
		topTitle.setText("博客");
		if (favoriteDB.isBlogItemFavorite(item)) {
			favorite_news.setImageResource(R.drawable.ic_action_favor_on_normal);
		}
		if (item!=null) {
			loadBlogDetailInfo();
		}
		
 	}
	
	public void loadBlogDetailInfo() {
		String url = PATH + item.getBlogId();
		new HttpUtils().send(HttpRequest.HttpMethod.GET, url, new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				try {
				    result = parseDetailContentXml(responseInfo.result);
					if (result!=null && !result.isEmpty()) {
						initWebView(result);
						loading_ll.setVisibility(View.GONE);
					} else {
						loading_ll.setVisibility(View.GONE);
						no_data_ll.setVisibility(View.VISIBLE);
					}
				} catch(Exception e) {
					e.printStackTrace();
				}					
			}
			@Override
			public void onFailure(HttpException error, String msg) {
				loading_ll.setVisibility(View.GONE);
				no_data_ll.setVisibility(View.VISIBLE);
			}
		});
	}

	
	private String parseDetailContentXml(String xml) {
		String result = null;
		XmlPullParser parser = Xml.newPullParser();
		try {
			parser.setInput(new StringReader(xml));
			int event = parser.getEventType();
			while (event != XmlPullParser.END_DOCUMENT) {
				switch (event) {
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					if ("string".equals(parser.getName())){
						result = parser.nextText();
					}
					break;
				case XmlPullParser.TEXT:
					break;
				case XmlPullParser.END_TAG:
					break;
				}
				event = parser.next();
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 初始化WebView
	 * @param data
	 */
	@SuppressLint("SetJavaScriptEnabled") 
	private void initWebView(String data) {
		// 如果访问的页面中有Javascript，则webview必须设置支持Javascript
		mWeb.getSettings().setJavaScriptEnabled(true);
		mWeb.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
//		mWeb.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		// 触摸焦点起作用
		mWeb.requestFocus();
		// 取消滚动条
		mWeb.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		mWeb.setHorizontalScrollBarEnabled(false);//水平不显示
		mWeb.setVerticalScrollBarEnabled(false); //垂直不显示
		// 设置WevView要显示的网页：
//		mWeb.loadUrl(url);
		mWeb.loadDataWithBaseURL(null, data,"text/html", "UTF-8", null);
		// 设置可缩放
		mWeb.getSettings().setSupportZoom(true);
		mWeb.getSettings().setBuiltInZoomControls(true);

		mWeb.setWebViewClient(new MyWebViewClient());
		mWeb.setWebChromeClient(webChromeClient);
	}
	
	class MyWebViewClient extends WebViewClient {
		
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (!TextUtils.isEmpty(url)) {
				BaseTools.showlog("shouldOverrideUrlLoading,onPageFinished");
				mWeb.loadUrl(url);
			}
			return true;
		}
		
		@Override
		public void onPageFinished(WebView view, String url) {
			String title = view.getTitle();
			if( TextUtils.isEmpty(title) ){
				return;
			}
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			BaseTools.showlog("onReceivedError,onReceivedError");
		}
	}
	
	private WebChromeClient webChromeClient = new WebChromeClient(){
		public void onProgressChanged(WebView view, int newProgress) {
				if (null != progressBar) {
					if (newProgress == 100) {
						progressBar.setVisibility(View.GONE);
					} else {
						if (progressBar.getVisibility() == View.GONE) {
							progressBar.setVisibility(View.VISIBLE);
						}
						progressBar.setProgress(newProgress);
					}
				}
			super.onProgressChanged(view, newProgress);
		}
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.topBack:
			finish();
			overridePendingTransition(R.anim.scale_out, 0);
			break;
		case R.id.toolbar_top:
			mWeb.scrollTo(0, 0);
			break;
		case R.id.toolbar_comment:
			Intent intent = new Intent(this, NewsCommentActivity.class);
			intent.putExtra("id", item.getBlogId());
			intent.putExtra("blogComment", true);
			startActivity(intent);
			break;
		case R.id.toolbar_favorite:
			if (favoriteDB.isBlogItemFavorite(item)) {
				favoriteDB.deleteBlogItem(item);
				favorite_news.setImageResource(R.drawable.ic_action_favor);
				Util.showToast(this, "取消收藏成功");
			} else {
				favoriteDB.saveBlogItem(item);
				favorite_news.setImageResource(R.drawable.ic_action_favor_on_normal);
				Util.showToast(this, "收藏成功");
			}
			break;
		case R.id.toolbar_share:
			Intent inte = new Intent(Intent.ACTION_SEND);    
			inte.setType("image/*");
            inte.putExtra(Intent.EXTRA_SUBJECT, "Watson Share");    
            inte.putExtra(Intent.EXTRA_TEXT,  result);
            inte.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);    
            startActivity(Intent.createChooser(inte, item.getTitle()));
			break;
		default:
			break;
		}
	}

}


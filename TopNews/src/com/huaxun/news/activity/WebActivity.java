package com.huaxun.news.activity;


import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

import com.huaxun.R;
import com.huaxun.news.bean.News;
import com.huaxun.news.service.PlayerService;
import com.huaxun.tool.BaseTools;
import com.huaxun.tool.Settings;
import com.huaxun.utils.FileUtil;
import com.huaxun.utils.HttpUtil;
import com.huaxun.utils.NetUtil;
import com.huaxun.utils.Util;

public class WebActivity extends Activity {
	
	private WebView webView;
	private TextView topBack,topTitle;
	private ImageView topShare;
	private ProgressBar progressBar;
	private WebSettings webSettings;
	private String previousUrl = "";
	public static final int playButton = 1;
	public static final int stopButton = 2;
	private News news;
	private Handler handler = new Handler();
	private Settings settings;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Util.setTranslucent(this);
		setContentView(R.layout.webview);
		webView = (WebView)this.findViewById(R.id.webViewID);
		topBack = (TextView)this.findViewById(R.id.topBack);
		topTitle = (TextView)this.findViewById(R.id.topTitle);
		topShare = (ImageView)this.findViewById(R.id.topShare);
		progressBar = (ProgressBar)this.findViewById(R.id.webview_progressBar);
		settings = new Settings(this, false);
		webSettings = webView.getSettings();
		//实现与JS的交互，这样才能在webViewClient捕捉到各个action
		webSettings.setJavaScriptEnabled(true);
		webView.setWebViewClient(webViewClient);
		webView.setWebChromeClient(webChromeClient);
		
		webView.addJavascriptInterface(new Object(),"android");
		
		Bundle bundle = getIntent().getExtras();
		news =(News) bundle.getSerializable("NEWS");
		
		topTitle.setText(news.parentNodeName);
		topBack.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				finish();
				overridePendingTransition(R.anim.scale_out, 0);
			}
		});
		//isshare为true，说明从newslistfragment过来，可以分享。名医坐堂，专题，活动，申活圈等不可分享
		if (news.isshare != null && news.isshare.equals("true")) {  
			topShare.setVisibility(View.VISIBLE);
		}	
		topShare.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				webView.loadUrl("javascript:if(window.share){share();}else{window.location='action://share';}");
			}
		});
		initTextSize();
        loadUrl();
	}
	
	private void initTextSize() {
		if (settings.getFontSize().equals(Settings.FONT_SIZE_LARGE)) {
			webSettings.setTextSize(WebSettings.TextSize.LARGER);
		} else if (settings.getFontSize().equals(Settings.FONT_SIZE_NORMAL)) {
			webSettings.setTextSize(WebSettings.TextSize.NORMAL);
		} else if (settings.getFontSize().equals(Settings.FONT_SIZE_SMALL)) {
			webSettings.setTextSize(WebSettings.TextSize.SMALLER);
		}
	}
	
	private void loadUrl(){
		BaseTools.showlog("loadUrl  url=" + news.getNewsurl());
		if (NetUtil.isNetworkAvailable(this)){
			webView.loadUrl(Util.getEastDayURL(news.getNewsurl()));			
		}else{
			if (FileUtil.isNewsFileExist(news.getNewstitle())) {
				String newsContentFile = FileUtil.getNewsPath() + File.separator + news.getNewstitle();
				String content = FileUtil.ReadLocalNewsFile(newsContentFile);
//				String content = HttpUtil.readTxtFile(newsContentFile, "utf-8");
				webView.loadDataWithBaseURL("", content, "text/html", "UTF-8", "");
			}
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
	
	private WebViewClient webViewClient = new WebViewClient() {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Uri uri = Uri.parse(url);			
			//开始播放，暂停，继续播放其实传的都是这个action，需要我们自己根据url和当前播放状态来实现后续行为
			if (url.startsWith("action://play")) {
				String mp3url = uri.getQueryParameter("mp3url");
				String newsid = uri.getQueryParameter("newsid");
				mp3url = Util.getEastDayAudioURL(mp3url);
				BaseTools.showlog("mp3url="+mp3url);
				if (!mp3url.equals(previousUrl)) {
					previousUrl = mp3url;
					playAudio(PlayerService.PLAY, mp3url);
					webView.loadUrl("javascript:changebtn('" + newsid + "','" + stopButton + "')");
				} else {
					if (PlayerService.mMediaPlayer.isPlaying()) {
						playAudio(PlayerService.PAUSE, mp3url);
						webView.loadUrl("javascript:changebtn('" + newsid + "','" + playButton + "')");
					} else {
						playAudio(PlayerService.REPLAY, mp3url);
						webView.loadUrl("javascript:changebtn('" + newsid + "','" + stopButton + "')");
					}
				}
			} else if (url.startsWith("action://stopplay")) {
				String mp3url = uri.getQueryParameter("mp3url");
				playAudio(PlayerService.STOP, mp3url);
			} else if (url.startsWith("action://share")) {// 分享
				// shareTitle
				String title1 = uri.getQueryParameter("newstitle");
				String title2 = uri.getQueryParameter("title");
				BaseTools.showlog("title1="+title1+" title2="+title2);
				String title = !TextUtils.isEmpty(title1) ? title1 : title2;
				if (TextUtils.isEmpty(title)) {
					title = news.getNewstitle();
				}
				if (TextUtils.isEmpty(title)) {
					title = getResources().getString(R.string.app_name);
				}
				// shareUrl
				String url1 = uri.getQueryParameter("newsurl");
				String url2 = uri.getQueryParameter("url");
				BaseTools.showlog("url1="+url1+" url2="+url2);
				String Url = !TextUtils.isEmpty(url1) ? url1 : url2;
				if (TextUtils.isEmpty(Url)) {
					Url = news.shareurl;					
				}
				if (TextUtils.isEmpty(Url)) {
					Url = webView.getUrl();
				}
				// sharePic
				String image1 = uri.getQueryParameter("imgurl1");
				String image2 = uri.getQueryParameter("imageurl");
				BaseTools.showlog("image1="+image1+" image2="+image2);
				String pic = !TextUtils.isEmpty(image1) ? image1 : image2;
				if (TextUtils.isEmpty(pic)) {
					pic = news.getImgurl1();
				}				
				//shareText
				String text = uri.getQueryParameter("newsdescription");
				BaseTools.showlog("text="+text);
				if (TextUtils.isEmpty(text)) {
					text = title;
				}
				
				final String shareTitle = title;
				final String shareText = text;
				final String shareUrl = Util.getEastDayURL(Url);
				final String sharePic = Util.getEastDayURL(pic);
				
				new Thread(new Runnable(){
					@Override
					public void run() {
						HttpUtil.saveFileToLocal(sharePic, FileUtil.getCacheImagePath() + File.separator + shareTitle + "_share.png", "utf-8");			
						handler.post(new Runnable(){
							public void run() {
								// 分享
								String picDir;
								if (FileUtil.isCacheImageFileExist(shareTitle + "_share.png")) {
									picDir = FileUtil.getCacheImagePath() + File.separator + shareTitle + "_share.png";
								} else {
									picDir = FileUtil.getCacheImagePath() + File.separator + "share.png";
								}
								showShare(shareTitle, shareText, shareUrl, picDir);	
							}					
						});
					}}).start();
			} else if (url.startsWith("action://links")) {
				//action://links?url='+encodeURIComponent ('http://listen.eastday.com/node2/n463/n784/index784_t114.html') +
				//'&newsid='+encodeURIComponent('123456')+'&iscomment='+encodeURIComponent('1')+'&isshare='+encodeURIComponent('1')+
				//'&title='+encodeURIComponent('两会聚焦：李克强总理记者会释放八大信号')+'&imgurl1='+encodeURIComponent('http://listen.eastday.com/thumbnailimg/month_1604/20160425081020534_282_188.jpg')+ '&createtime='+encodeURIComponent('2016-04-25'));
				String linkUrl = Util.getEastDayURL(uri.getQueryParameter("url"));
				String newsid = uri.getQueryParameter("newsid");
				String iscomment = uri.getQueryParameter("iscomment");
				String isshare = uri.getQueryParameter("isshare");
				String title = uri.getQueryParameter("title");
				String imgurl1 = uri.getQueryParameter("imgurl1");
				String createtime = uri.getQueryParameter("createtime");
				
				News wapNews = new News();
				wapNews.setNewsurl(linkUrl);
				wapNews.setNewsid(newsid);
				wapNews.iscomment = iscomment;
				wapNews.isshare = isshare;
				wapNews.setNewstitle(title);
				wapNews.setImgurl1(imgurl1);
				wapNews.setCreatetime(createtime);
				wapNews.parentNodeName = news.parentNodeName;
				
				Intent intent = new Intent(WebActivity.this, WebActivity.class);
				Bundle bundle = new Bundle();
				bundle.putSerializable("NEWS", news);
				intent.putExtras(bundle);
				startActivity(intent);
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);			
			}
			return true;
		}
	};
	
	public void playAudio(int action, String url) {
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putInt("action", action);
		bundle.putString("url", url);
		intent.putExtras(bundle);
		intent.setClass(WebActivity.this, PlayerService.class);
		/* 启动service service要在AndroidManifest.xml注册如：<service></service>*/
		startService(intent);
	}
	
	private void showShare(String shareTitle, String shareText, String shareUrl, String picDir) {
		OnekeyShare oks = new OnekeyShare();
		// 关闭sso授权
		oks.disableSSOWhenAuthorize();
		// 分享时Notification的图标和文字 2.5.9以后的版本不调用此方法
		// oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
		// title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
		oks.setTitle(shareTitle);
		// titleUrl是标题的网络链接，仅在人人网和QQ空间使用
		oks.setTitleUrl(shareUrl);
		// text是分享文本，所有平台都需要这个字段
		oks.setText(shareText);
		// imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
		oks.setImagePath(picDir);//确保SDcard下面存在此张图片
		// url仅在微信（包括好友和朋友圈）中使用
		oks.setUrl(shareUrl);
		// comment是我对这条分享的评论，仅在人人网和QQ空间使用
		oks.setComment("我是测试评论文本");
		// site是分享此内容的网站名称，仅在QQ空间使用
		oks.setSite(getString(R.string.app_name));
		// siteUrl是分享此内容的网站地址，仅在QQ空间使用
		oks.setSiteUrl("http://sharesdk.cn");
		// 是否直接分享
		oks.setSilent(false);
		// 启动分享GUI
		oks.show(this);
	}

	
	public void onDestroy() {
		super.onDestroy();
		Intent intent = new Intent();
		intent.setClass(WebActivity.this, PlayerService.class);
		stopService(intent);
	}
	
	@Override
	public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.scale_out, 0);
	}
}

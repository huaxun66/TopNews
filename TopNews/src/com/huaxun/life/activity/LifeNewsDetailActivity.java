package com.huaxun.life.activity;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.huaxun.R;
import com.huaxun.db.FavoriteDB;
import com.huaxun.life.bean.LifeNews;
import com.huaxun.life.bean.LifeNewsContent;
import com.huaxun.news.service.SpeechSynthesizerService;
import com.huaxun.tool.BaseTools;
import com.huaxun.tool.NewsUrls;
import com.huaxun.tool.Options;
import com.huaxun.utils.Util;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.nostra13.universalimageloader.core.ImageLoader;


public class LifeNewsDetailActivity extends Activity implements OnClickListener{
	
	public static final int TEXT_TYPE = 0;
	public static final int IMG_TYPE = 1;
	
	private TextView topBack,topTitle;
	private LinearLayout loading_ll;
	private LinearLayout no_data_ll;
	private TextView news_title, news_time;
	private LinearLayout listen_news_ll, change_text_size_ll, favorite_news_ll, share_news_ll;
	private ImageView favorite_news;
	private ListView mListView;
	private DetailAdapter mAdapter;
	private ArrayList<LifeNewsContent> mLifeItem = new ArrayList<LifeNewsContent>();
	
	private PopupWindow popupWindow;
	private int fontsize = 17; // 字体大小
	private Intent playNewsIntent;
	private FavoriteDB favoriteDB;
	
	public static final String NEWS_DETAIL = NewsUrls.LIFE_NEWS_DETAIL;
	private LifeNews lifeNews;
	private String nodeName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Util.setTranslucent(this);
		setContentView(R.layout.activity_life_news_detail);
		
		favoriteDB = FavoriteDB.getInstance();
		playNewsIntent = new Intent(this, SpeechSynthesizerService.class);
		
		Bundle myBundle = getIntent().getExtras();
		lifeNews = (LifeNews) myBundle.getSerializable("lifeNews");
		nodeName = myBundle.getString("nodeName");
		 
		initView();
		loadLifeNewsInfo();
	}
	
	public void onDestroy() {
		super.onDestroy();
		Intent intent = new Intent();
		intent.setClass(LifeNewsDetailActivity.this, SpeechSynthesizerService.class);
		stopService(intent);
	}
	
	public void loadLifeNewsInfo() {
		String url = NEWS_DETAIL + lifeNews.getNewsId();
		new HttpUtils().send(HttpRequest.HttpMethod.GET, url, new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				try {
					ArrayList<LifeNewsContent> list = getNewsContent(responseInfo.result);
					mLifeItem.clear();
					if (list != null && list.size() > 0) {
						mLifeItem.addAll(list);
						loading_ll.setVisibility(View.GONE);
						mAdapter.notifyDataSetChanged();
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
	
	/**
	 * 解析新闻类容
	 * 
	 * @param result
	 * @return
	 */
	private ArrayList<LifeNewsContent> getNewsContent(String result) {
		ArrayList<LifeNewsContent> contents = new ArrayList<LifeNewsContent>();
		LifeNewsContent ncv = null;

		Document document = Jsoup.parse(result);
//		Elements info = document.getElementsByTag("span");  //获取标题，来源，时间
//		for (Element element : info) {
//			ncv = new LifeNewsContent();
//			ncv.setIsImg(0);
//			ncv.setContentList(element.text());
//			contents.add(ncv);
//		}
		
		Elements elements = document.getElementsByTag("p");
		Elements media = document.select("[src]");

		int i = 0;
		for (Element element : elements) {
			if (element.hasText()) {
				ncv = new LifeNewsContent();
				ncv.setIsImg(0);
				ncv.setContentList(element.text());
				contents.add(ncv);
			} else {
				if (element.hasAttr("align") && media != null && media.size() > 0 && media.size() > i) {
					Element src = media.get(i);
					if (src.tagName().equals("img")) {
						ncv = new LifeNewsContent();
						ncv.setIsImg(1);
						ncv.setContentList(src.attr("src"));
						contents.add(ncv);
					}
					i++;
				}
			}
		}
		return contents;
	}
	
	/**
	 * 初始化控件
	 */
	private void initView() {
		topBack = (TextView) findViewById(R.id.topBack);
		topBack.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				finish();
				overridePendingTransition(R.anim.scale_out, 0);
			}
		});
		topTitle = (TextView) findViewById(R.id.topTitle);
		topTitle.setText(nodeName);
		loading_ll = (LinearLayout) findViewById(R.id.loading_ll);
		no_data_ll = (LinearLayout) findViewById(R.id.no_data_ll);
		
		listen_news_ll = (LinearLayout) findViewById(R.id.listen_news_ll);
		change_text_size_ll = (LinearLayout) findViewById(R.id.change_text_size_ll);
		favorite_news_ll = (LinearLayout) findViewById(R.id.favorite_news_ll);
		share_news_ll = (LinearLayout) findViewById(R.id.share_news_ll);
		favorite_news = (ImageView) findViewById(R.id.favorite_news);
		listen_news_ll.setOnClickListener(this);
		change_text_size_ll.setOnClickListener(this);
		favorite_news_ll.setOnClickListener(this);
		share_news_ll.setOnClickListener(this);
		
		if (favoriteDB.isLifeNewsFavorite(lifeNews)) {
			favorite_news.setImageResource(R.drawable.ic_action_favor_on_normal);
		}
		
		mListView = (ListView) findViewById(R.id.listview_text);	
		mAdapter = new DetailAdapter();
		mListView.setAdapter(mAdapter);
		
		View head = View.inflate(this, R.layout.item_container_headview, null);
		news_title = (TextView) head.findViewById(R.id.news_title);
		news_time = (TextView) head.findViewById(R.id.news_time);
		news_title.setText(lifeNews.getTitle());
		news_time.setText(lifeNews.getDate());
		mListView.addHeaderView(head);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.listen_news_ll: //听新闻
            String content = getNewsContent();
			Bundle bundle  = new Bundle();		
			bundle.putString("content", content);			
			if (SpeechSynthesizerService.isNowPlaying) {
				if (content.equals(SpeechSynthesizerService.content)) {
					bundle.putInt("action", SpeechSynthesizerService.PAUSE);
					playNewsIntent.putExtras(bundle);
					startService(playNewsIntent);
				    Util.showToast(this, "暂停播放");
				} else {
					bundle.putInt("action", SpeechSynthesizerService.PLAY);
					playNewsIntent.putExtras(bundle);
					startService(playNewsIntent);
				    Util.showToast(this, "开始播放");
				}
			} else {
				if (content.equals(SpeechSynthesizerService.content)) {
					bundle.putInt("action", SpeechSynthesizerService.REPLAY);
					playNewsIntent.putExtras(bundle);
					startService(playNewsIntent);
				    Util.showToast(this, "重新播放");
				} else {
					bundle.putInt("action", SpeechSynthesizerService.PLAY);
					playNewsIntent.putExtras(bundle);
					startService(playNewsIntent);
				    Util.showToast(this, "开始播放");
				}
			}
			break;
			
		case R.id.change_text_size_ll: // 修改字体大小
			View layout = View.inflate(this, R.layout.pop_text_size, null);
			popupWindow = new PopupWindow(this);
			popupWindow.setBackgroundDrawable(new BitmapDrawable());
			popupWindow.setWidth(getWindowManager().getDefaultDisplay().getWidth());
			popupWindow.setHeight(getWindowManager().getDefaultDisplay().getHeight() / 6);
			popupWindow.setAnimationStyle(R.style.AnimationPreview);
			popupWindow.setOutsideTouchable(true);
			popupWindow.setFocusable(true);// 响应回退按钮事件
			popupWindow.setContentView(layout);

			int[] location = new int[2];
			v.getLocationOnScreen(location);
			popupWindow.showAtLocation(v.findViewById(R.id.change_text_size),
					Gravity.NO_GRAVITY, location[0], location[1] - popupWindow.getHeight());

			SeekBar fontseek = (SeekBar) layout.findViewById(R.id.settings_font);
			fontseek.setMax(20);
			fontseek.setProgress(fontsize - 10);
			fontseek.setSecondaryProgress(0);
			final TextView textFont = (TextView) layout.findViewById(R.id.fontSub);
			textFont.setText(fontsize + "");
			fontseek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					fontsize = progress + 10;
					textFont.setText("" + fontsize);
					if (mAdapter != null)
						mAdapter.notifyDataSetChanged();
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
			});
			break;
			
		case R.id.favorite_news_ll:
			if (favoriteDB.isLifeNewsFavorite(lifeNews)) {
				favoriteDB.deleteLifeNews(lifeNews);
				favorite_news.setImageResource(R.drawable.ic_action_favor);
				Util.showToast(this, "取消收藏成功");
			} else {
				favoriteDB.saveLifeNews(lifeNews);
				favorite_news.setImageResource(R.drawable.ic_action_favor_on_normal);
				Util.showToast(this, "收藏成功");
			}
			break;			
		case R.id.share_news_ll:
			Intent inte = new Intent(Intent.ACTION_SEND); 
			inte.setType("image/*");    
            inte.putExtra(Intent.EXTRA_SUBJECT, "Watson Share");    
            inte.putExtra(Intent.EXTRA_TEXT,  getNewsContent());
            inte.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);    
            startActivity(Intent.createChooser(inte, nodeName)); //系统会寻找所有activity，然后把有定义的activity形成列表提供给使用者
			break;
		}

	}
	
	private String getNewsContent() {
		StringBuilder content = new StringBuilder();
		for (int i=0; i<mLifeItem.size(); i++) {
			if (mLifeItem.get(i).getIsImg() == 0) {
				content.append(mLifeItem.get(i).getContentList());
			}
		}
		return content.toString();
	}
	
	class DetailAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mLifeItem == null ? 0 : mLifeItem.size();
		}

		@Override
		public Object getItem(int position) {
			return mLifeItem == null ? null : mLifeItem.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		public boolean isEnabled(int position) {
			return false;
		}

		public boolean areAllItemsEnabled() {
			return false;
		}

		@Override
		public int getItemViewType(int position) {
			LifeNewsContent vo = (LifeNewsContent) getItem(position);
			return vo.getIsImg();
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final LifeNewsContent item = (LifeNewsContent) getItem(position);
			int type = getItemViewType(position);// 获取当前位置对应的类别
			if (convertView == null) {
				switch (type) {
				case TEXT_TYPE:
					convertView = View.inflate(LifeNewsDetailActivity.this, R.layout.item_news_content_textview, null);
					break;
				case IMG_TYPE:
					convertView = View.inflate(LifeNewsDetailActivity.this, R.layout.item_news_content_image, null);
					break;
				}
			}
			if (item != null) {
				switch (type) {
				case TEXT_TYPE:
					// 对应设置文字内容F
					TextView tv = (TextView) convertView.findViewById(R.id.content_textView1);
					String text = item.getContentList();
					tv.setTextSize(fontsize);
					tv.setText(Html.fromHtml(text));
					tv.setMovementMethod(LinkMovementMethod.getInstance());
					break;
				case IMG_TYPE:
					// 加载图片
					ImageView iv = (ImageView) convertView.findViewById(R.id.content_imageView1);
					String url = item.getContentList();
					if (item.getContentList().startsWith("http:")) {
						url = item.getContentList();
					} else {
						url = "http://content.2500city.com" + item.getContentList();
					}
					if ( !TextUtils.isEmpty(url) ) {
						ImageLoader.getInstance().displayImage(url, iv, Options.getListOptions());
					}
					break;
				default:
					break;
				}
			}
			return convertView;
		}

	}
	
}


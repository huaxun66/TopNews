package com.huaxun.menuLeft.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huaxun.R;
import com.huaxun.app.AppApplication;
import com.huaxun.db.FavoriteDB;
import com.huaxun.dialog.LoadingDialog;
import com.huaxun.life.activity.BlogDetailActivity;
import com.huaxun.life.activity.HotNewsDetailActivity;
import com.huaxun.life.activity.LifeNewsDetailActivity;
import com.huaxun.life.bean.BlogItem;
import com.huaxun.life.bean.HotNews;
import com.huaxun.life.bean.LifeNews;
import com.huaxun.news.activity.PicBrowserActivity;
import com.huaxun.news.activity.WebActivity;
import com.huaxun.news.bean.News;
import com.huaxun.tool.BaseTools;
import com.huaxun.tool.Options;
import com.huaxun.utils.FileUtil;
import com.huaxun.utils.Util;
import com.huaxun.view.PinnedSectionListView;
import com.huaxun.view.PinnedSectionListView.PinnedSectionListAdapter;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class FavoriteNewsActivity extends Activity {
	private final String News = "News";
	private final String LifeNews = "LifeNews";
	private final String HotNews = "HotNews";
	private final String BlogItem = "BlogItem";
	
	private ArrayList<News> newsList = new ArrayList<News>();
	private ArrayList<LifeNews> lifeList = new ArrayList<LifeNews>();
	private ArrayList<HotNews> hotList = new ArrayList<HotNews>();
	private ArrayList<BlogItem> blogList = new ArrayList<BlogItem>();	
	private ArrayList<Item> pinnedList = new ArrayList<Item>();
	private PinnedSectionListView listView;
	private NewsListAdapter newsListAdapter;
	private FavoriteDB favoriteDB;
	private ImageView detail_loading;
	private LoadingDialog loadingDialog;
	private TextView topBack,topTitle;
	private Handler handler = new Handler();
	
    private static final int[] COLORS = new int[] {
        R.color.green_light, R.color.orange_light,
        R.color.blue_light, R.color.red_light };
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Util.setTranslucent(this);
		favoriteDB = FavoriteDB.getInstance();
		setContentView(R.layout.favorite_new_list);		
		topTitle = (TextView)this.findViewById(R.id.topTitle);
		topTitle.setText("收藏新闻");
		topBack = (TextView)this.findViewById(R.id.topBack);
		topBack.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				finish();
				overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
			}
		});
		
		listView = (PinnedSectionListView) this.findViewById(R.id.news_list);
		detail_loading = (ImageView) this.findViewById(R.id.detail_loading);
		newsListAdapter = new NewsListAdapter(this);
		listView.setAdapter(newsListAdapter);
		listView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
	//	listView.setShadowVisible(true); //阴影可见
		
		listView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
			  if (pinnedList.get(position).getType() == Item.SECTION) {
				  return;
			  }
              News news = pinnedList.get(position).getNews();
              if (news.source.equals(News)) {
              	if (news.getNewstype().equals("0")) {
  					news.parentNodeName = "收藏新闻";
  					Intent intent = new Intent(FavoriteNewsActivity.this,WebActivity.class);
  					Bundle bundle = new Bundle();
  					bundle.putSerializable("NEWS", news);				
  					intent.putExtras(bundle);
  					startActivity(intent);
  					overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
  				} else if (news.getNewstype().equals("5")) {
  					Intent intent = new Intent(FavoriteNewsActivity.this,PicBrowserActivity.class);
  					Bundle bundle = new Bundle();
  					bundle.putString("url", Util.getEastDayURL(news.getNewsurl()));
  					intent.putExtras(bundle);
  					startActivity(intent);
  				}
              } else if (news.source.equals(LifeNews)) {
					Intent intent = new Intent(FavoriteNewsActivity.this, LifeNewsDetailActivity.class);
					Bundle bundle = new Bundle();
					bundle.putSerializable("lifeNews", getLifeNews(news.getNewsid()));
					bundle.putString("nodeName", "收藏新闻");
					intent.putExtras(bundle);
					startActivity(intent);
					overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
				} else if (news.source.equals(HotNews)) {
					Intent intent = new Intent(FavoriteNewsActivity.this, HotNewsDetailActivity.class);
					Bundle bundle = new Bundle();
					bundle.putSerializable("HotNews", getHotNews(news.getNewsid()));
					bundle.putString("nodeName", "收藏新闻");
					intent.putExtras(bundle);
					startActivity(intent);
					overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
      		} else if (news.source.equals(BlogItem)) {
      			Intent intent = new Intent(FavoriteNewsActivity.this, BlogDetailActivity.class);
  				Bundle info = new Bundle();
  				info.putSerializable("item", getBlogItem(news.getNewsid()));
  				intent.putExtras(info);
  				startActivity(intent);
  				overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
      		}				
		   }			
		});
	
		loadingDialog = new LoadingDialog(this);
		loadingDialog.show();
        getData();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		getData();
	}
	
	private void getData() {
		new Thread(new Runnable(){
			@Override
			public void run() {
				newsList = (ArrayList<News>) favoriteDB.loadAllNews();
				lifeList = (ArrayList<LifeNews>) favoriteDB.loadAllLifeNews();
				hotList = (ArrayList<HotNews>) favoriteDB.loadAllHotNews();
				blogList = (ArrayList<BlogItem>) favoriteDB.loadAllBlogItem();
				appendNewsList();
				handler.post(new Runnable(){
					@Override
					public void run() {
						detail_loading.setVisibility(View.GONE);
					    if (loadingDialog != null && loadingDialog.isShowing()) {
					    	loadingDialog.dismiss();
					    }
					    newsListAdapter.notifyDataSetChanged();
					}			
				});		
			}			
		}).start();
	}
	
	private void appendNewsList() {
		pinnedList.clear();
		//添加新闻收藏
		if (newsList.size()>0) {
			pinnedList.add(new Item(Item.SECTION, News, null));
		}		
		for (int i=0; i<newsList.size(); i++) {
			newsList.get(i).source = News;
			pinnedList.add(new Item(Item.ITEM, News, newsList.get(i)));
		}
		//添加生活新闻收藏
		if (lifeList.size()>0) {
			pinnedList.add(new Item(Item.SECTION, LifeNews, null));	
		}
		for (int i=0; i<lifeList.size(); i++) {
			LifeNews lifeNews = lifeList.get(i);
			News news = new News(lifeNews.getNewsId(), lifeNews.getTitle(), lifeNews.getImage().getSrc(), lifeNews.getDate(), LifeNews);
			pinnedList.add(new Item(Item.ITEM, LifeNews, news));
		} 
		//添加热门新闻收藏
		if (hotList.size()>0) {
			pinnedList.add(new Item(Item.SECTION, HotNews, null));
		}
		for (int i=0; i<hotList.size(); i++) {
			HotNews hotNews = hotList.get(i);
			News news = new News(hotNews.getHotId(), hotNews.getTitle(), hotNews.getTopicIcon(), hotNews.getPublished(), HotNews);
			pinnedList.add(new Item(Item.ITEM, HotNews, news));
		}
		//添加博客收藏
		if (blogList.size()>0) {
			pinnedList.add(new Item(Item.SECTION, BlogItem, null));
		}
		for (int i=0; i<blogList.size(); i++) {
			BlogItem blogItem = blogList.get(i);
			News news = new News(blogItem.getBlogId(), blogItem.getTitle(), blogItem.getAvatar(), blogItem.getPublished(), BlogItem);
			pinnedList.add(new Item(Item.ITEM, BlogItem, news));
		}
	}
	
	private void deleteFavorite(News news) {
		if (news.source == null) {
			return;
		}
		if (news.source.equals(News)) {
			favoriteDB.deleteNews(news);
		} else if (news.source.equals(LifeNews)) {
			favoriteDB.deleteLifeNews(getLifeNews(news.getNewsid()));
		} else if (news.source.equals(HotNews)) {
			favoriteDB.deleteHotNews(getHotNews(news.getNewsid()));
		} else if (news.source.equals(BlogItem)) {
			favoriteDB.deleteBlogItem(getBlogItem(news.getNewsid()));
		}
		Util.showToast(this, "删除成功");
	}
	
	private LifeNews getLifeNews(String id) {
		for (int i=0; i<lifeList.size(); i++) {
			if (lifeList.get(i).getNewsId().equals(id)) {
				return lifeList.get(i);
			}
		}
		return null;
	}
	
	private HotNews getHotNews(String id) {
		for (int i=0; i<hotList.size(); i++) {
			if (hotList.get(i).getHotId().equals(id)) {
				return hotList.get(i);
			}
		}
		return null;
	}
	
	private BlogItem getBlogItem(String id) {
		for (int i=0; i<blogList.size(); i++) {
			if (blogList.get(i).getBlogId().equals(id)) {
				return blogList.get(i);
			}
		}
		return null;
	}	
		
	public class NewsListAdapter extends BaseAdapter implements PinnedSectionListAdapter {
		private Context context;
		private LayoutInflater inflater = null;
		public int mBigAdd;
		public int mBigPicWidth;
		public int mZTWidth, mZTHeight;
		protected ImageLoader imageLoader = ImageLoader.getInstance();
		private DisplayImageOptions options;	
		private ImageLoadingListener animateFirstListener;
		
		public static final int TYPE_NORMAL = 1;// 普通列表样式
		public static final int TYPE_SMALL = 2;// 三张小图样式
		public static final int TYPE_BIG = 3;// 一张大图样式
		
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
			return pinnedList == null ? 0 : pinnedList.size();
		}

		@Override
		public Object getItem(int position) {
			if (pinnedList != null && pinnedList.size() != 0) {
				return pinnedList.get(position);
			}
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
        @Override 
        public int getViewTypeCount() {
            return 2;
        }

        @Override 
        public int getItemViewType(int position) {
            return (int)((Item) getItem(position)).getType();
        }

        @Override
        public boolean isItemViewTypePinned(int viewType) {
            return viewType == Item.SECTION;
        }
		
		public int getNewsType(News news) {
			int type = -1;
			if (news.getStyle() == null) {
				type = TYPE_NORMAL;
			} else {
				type = Integer.parseInt(news.getStyle());
			}
			if (type == 0) {
				type = TYPE_NORMAL;
			}
			return type;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {	
			Item item = pinnedList.get(position);
			if (item.getType() == Item.SECTION) { //section
				convertView = getSectionView(convertView, item);
			} else {                               //item
				int type = getNewsType(item.getNews());
				switch (type) {
				case TYPE_NORMAL:
					convertView = getNormalView(convertView, item, position);
					break;
				case TYPE_SMALL:
					convertView = getSmallView(convertView, item, position);
					break;
				case TYPE_BIG:
					convertView = getBigView(convertView, item, position);
					break;
				}			
			}		
			return convertView;
		}
		
		public View getSectionView(View convertView, final Item item) {
			HolderSection holder_section = null;
			if (null != convertView) {
				holder_section = (HolderSection) convertView.getTag();
			} else {
				convertView = inflater.inflate(R.layout.news_section_item, null);
				holder_section = new HolderSection(convertView);
				convertView.setTag(holder_section);
			}
			if (item.getSource().equals(News)) {
				convertView.setBackgroundColor(getResources().getColor(COLORS[0]));
				holder_section.section_iv.setImageResource(R.drawable.news_selected);
				holder_section.source.setText("新闻");
			} else if (item.getSource().equals(LifeNews)) {
				convertView.setBackgroundColor(getResources().getColor(COLORS[1]));
				holder_section.section_iv.setImageResource(R.drawable.life_selected);
				holder_section.source.setText("生活");
			} else if (item.getSource().equals(HotNews)) {
				convertView.setBackgroundColor(getResources().getColor(COLORS[2]));
				holder_section.section_iv.setImageResource(R.drawable.hot_selected);
				holder_section.source.setText("热门");
			} else if (item.getSource().equals(BlogItem)) {
				convertView.setBackgroundColor(getResources().getColor(COLORS[3]));
				holder_section.section_iv.setImageResource(R.drawable.blog_selected);
				holder_section.source.setText("博客");
			}			
			
			if (item.getDeleteState()) {
				holder_section.delete.setVisibility(View.GONE);
				holder_section.finish.setVisibility(View.VISIBLE);
			} else {
				holder_section.delete.setVisibility(View.VISIBLE);
				holder_section.finish.setVisibility(View.GONE);
			}
				
			holder_section.delete.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View view) {
					for (int i=0; i<pinnedList.size(); i++) {
						if (pinnedList.get(i).getSource().equals(item.getSource())) {
							pinnedList.get(i).setDeleteState(true);
						}
					}
					newsListAdapter.notifyDataSetChanged();
				}				
			});
			
			holder_section.finish.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View view) {
					for (int i=0; i<pinnedList.size(); i++) {
						if (pinnedList.get(i).getSource().equals(item.getSource())) {
							pinnedList.get(i).setDeleteState(false);
						}
					}
					newsListAdapter.notifyDataSetChanged();
				}				
			});
			return convertView;
		}

		
		public View getNormalView(View convertView, Item item, final int position) {
			final News news = item.getNews();
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
			holder_normal.normal_time.setText(getCreateTime(news));
			ImageLoader.getInstance().displayImage(Util.getEastDayURL(news.getImgurl1()), holder_normal.normal_iv, options, animateFirstListener);		
			if (item.getDeleteState()) {
				holder_normal.back.setVisibility(View.VISIBLE);
			} else {
				holder_normal.back.setVisibility(View.GONE);
			}
			holder_normal.back.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
	                deleteFavorite(news);
	                pinnedList.remove(position);               
	                newsListAdapter.notifyDataSetChanged();
				}
				
			});
			return convertView;
		}

		
		public View getSmallView(View convertView, Item item, final int position) {
			final News news = item.getNews();
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
			
			int resId = Util.getIconTypeRes(news.getIcontype());
			if (resId == -1) {
				holder_small.small_mark.setVisibility(View.INVISIBLE);
			} else {
				holder_small.small_mark.setVisibility(View.VISIBLE);
				holder_small.small_mark.setImageResource(resId);
			}
			holder_small.small_time.setText(getCreateTime(news));
			if (item.getDeleteState()) {
				holder_small.back.setVisibility(View.VISIBLE);
			} else {
				holder_small.back.setVisibility(View.GONE);
			}
			holder_small.back.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
	                deleteFavorite(news);
	                pinnedList.remove(position);               
	                newsListAdapter.notifyDataSetChanged();
				}				
			});
			return convertView;
		}
		
		public View getBigView(View convertView, Item item, final int position) {
			final News news = item.getNews();
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
			
			int resId = Util.getIconTypeRes(news.getIcontype());
			if (resId == -1) {
				holder_big.big_mark.setVisibility(View.INVISIBLE);
			} else {
				holder_big.big_mark.setVisibility(View.VISIBLE);
				holder_big.big_mark.setImageResource(resId);
			}
			holder_big.big_time.setText(getCreateTime(news));
			if (item.getDeleteState()) {
				holder_big.back.setVisibility(View.VISIBLE);
			} else {
				holder_big.back.setVisibility(View.GONE);
			}
			holder_big.back.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
	                deleteFavorite(news);
	                pinnedList.remove(position);               
	                newsListAdapter.notifyDataSetChanged();
				}				
			});
			return convertView;
		}
		
		public class HolderSection {
			public ImageView section_iv;
			public TextView source;
			public ImageView delete;
			public TextView finish;

			public HolderSection(View view) {
				section_iv = (ImageView) view.findViewById(R.id.section_iv);
				source = (TextView) view.findViewById(R.id.source);
				delete = (ImageView) view.findViewById(R.id.delete);
				finish = (TextView) view.findViewById(R.id.finish);
			}
		}

		public class HolderNormal {
			public News news;
			public RelativeLayout back;
			public ImageView normal_iv;
			public TextView normal_title;
			public TextView normal_time;
			public ImageView normal_mark;
			public TextView isHaveFileTextView;
			public int index;

			public HolderNormal(View view) {
				back = (RelativeLayout) view.findViewById(R.id.back);
				normal_iv = (ImageView) view.findViewById(R.id.normal_iv);
				normal_title = (TextView) view.findViewById(R.id.normal_tv_title);
				normal_time = (TextView) view.findViewById(R.id.normal_tv_time);
				normal_mark = (ImageView) view.findViewById(R.id.normal_mark);
				isHaveFileTextView = (TextView) view.findViewById(R.id.localTextViewID);
				index = -1;
			}
		}
		
		
		public class HolderSmall {
			public News news;
			// 3:小图
			public RelativeLayout back;
			public TextView small_title;
			public ImageView small_iv01;
			public ImageView small_iv02;
			public ImageView small_iv03;
			public TextView small_time;
			public ImageView small_mark;
			public int index;

			public HolderSmall(View view) {
				// small
				back = (RelativeLayout) view.findViewById(R.id.back);
				small_title = (TextView) view.findViewById(R.id.small_image_tv_title);
				small_iv01 = (ImageView) view.findViewById(R.id.small_image_iv01);
				small_iv02 = (ImageView) view.findViewById(R.id.small_image_iv02);
				small_iv03 = (ImageView) view.findViewById(R.id.small_image_iv03);
				small_time = (TextView) view.findViewById(R.id.small_tv_time);
				small_mark = (ImageView) view.findViewById(R.id.small_mark);
				index = -1;
			}
		}
		
		
		public class HolderBig {
			public News news;
			// 4:大图
			public RelativeLayout back;
			public RelativeLayout big_image_rl;
			public TextView big_title;
			public ImageView big_iv;
			public TextView big_time;
			public ImageView big_mark;
			public int index;

			public HolderBig(View view) {
				// big
				back = (RelativeLayout) view.findViewById(R.id.back);
				big_image_rl = (RelativeLayout) view.findViewById(R.id.big_image_rl);
				big_title = (TextView) view.findViewById(R.id.big_image_tv_title);
				big_iv = (ImageView) view.findViewById(R.id.big_image_iv);
				big_time = (TextView) view.findViewById(R.id.big_tv_time);
				big_mark = (ImageView) view.findViewById(R.id.big_mark);
				
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
		
		private String getCreateTime(News news){
			if (news.getCreatetime() == null) {
				return "";
			}
			if (news.source.equals("News")) {
				return news.getCreatetime().substring(5, 16);
			} else if (news.source.equals("LifeNews")) {
				return news.getCreatetime();
			} else {
				return news.getCreatetime().substring(5, 16).replace("T", " ");
			}
		}
		
		private boolean isLocalHaveFile(String title){
			String filePath = FileUtil.getNewsPath() + "/" + title;
			return new File(filePath).exists();
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
	
	
	static class Item {
		
		public static final int SECTION = 0;
		public static final int ITEM = 1;	

		private final int type;
		private final String source;
		private final News news;
		private boolean deleteState = false;

		public Item(int type, String source, News news) {
		    this.type = type;
		    this.source = source;
		    this.news = news;
		}
		
		public int getType() {
			return type;
		}

		public String getSource() {
			return source;
		}
		
		public News getNews() {
			return news;
		}
		
		public void setDeleteState(Boolean flag) {
			this.deleteState = flag;
		}
		
		public boolean getDeleteState() {
			return deleteState;
		}

	}
	
}

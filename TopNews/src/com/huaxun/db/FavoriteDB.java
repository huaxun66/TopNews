package com.huaxun.db;

import java.util.List;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import com.huaxun.life.bean.BlogItem;
import com.huaxun.life.bean.HotNews;
import com.huaxun.life.bean.LifeNews;
import com.huaxun.life.bean.LifeNewsImage;
import com.huaxun.news.bean.News;
import com.huaxun.tool.BaseTools;
import com.huaxun.weather.City;
import com.huaxun.weather.County;
import com.huaxun.weather.Province;

import android.database.sqlite.SQLiteDatabase;

public class FavoriteDB {
	/**
	 * 一些基本的数据库方法封装
	 */
	private SQLiteDatabase db;

	private static FavoriteDB favoriteDB;

	public FavoriteDB() {
		db = Connector.getDatabase();//正式生成数据库
	}

	public synchronized static FavoriteDB getInstance() {
		if (favoriteDB == null) {
			favoriteDB = new FavoriteDB();
		}
		return favoriteDB;
	}
	
	/**
	 * 从数据库读取所有的News信息
	 */
	public List<News> loadAllNews() {
		List<News> list = DataSupport.findAll(News.class);
		return list;
	}
	
	/**
	 * 从数据库查詢指定News信息是否存在
	 */
	public boolean isNewsFavorite(News news) {
		List<News> newsList = DataSupport.where("newsid = ?", news.getNewsid()).find(News.class);
		if (newsList!=null && newsList.size()>0) {
			return true;
		}
		return false;
	}
	
	/**
	 * 将News实例存储到数据库
	 */
	public void saveNews(News news) {
		if (news != null) {
			news.save();
		}
	}
	
	/**
	 * 将News实例從数据库刪除
	 */
	public void deleteNews(News news) {
		if (news != null) {
			DataSupport.deleteAll(News.class, "newsid = ?", news.getNewsid());
		}
	}

	/**
	 * 从数据库读取所有的lifeNews信息,isEager=true,为激进查询
	 */
	public List<LifeNews> loadAllLifeNews() {
		List<LifeNews> list = DataSupport.findAll(LifeNews.class, true);
		return list;
	}
	
	/**
	 * 从数据库查詢指定lifeNews信息是否存在
	 */
	public boolean isLifeNewsFavorite (LifeNews lifeNews) {
		List<LifeNews> newsList = DataSupport.where("newsId = ?", lifeNews.getNewsId()).find(LifeNews.class);
		if (newsList!=null && newsList.size()>0) {
			return true;
		}
		return false;
	}
	
	/**
	 * 将lifeNews实例存储到数据库
	 */
	public void saveLifeNews(LifeNews lifeNews) {
		if (lifeNews != null) {
			lifeNews.save();
			if (lifeNews.getImage() != null) {
				saveLifeNewsImage(lifeNews.getImage());
				
			}
		}
	}
	
	/**
	 * 将lifeNews实例從数据库刪除
	 */
	public void deleteLifeNews(LifeNews lifeNews) {
		if (lifeNews != null) {
			DataSupport.deleteAll(LifeNews.class, "newsId = ?", lifeNews.getNewsId());
			if (lifeNews.getImage() != null) {
				deleteLifeNewsImage(lifeNews.getImage());
			}
		}
	}
	
	/**
	 * 将LifeNewsImage实例存储到数据库
	 */
	public void saveLifeNewsImage(LifeNewsImage lifeNewsImage) {
		if (lifeNewsImage != null) {
			lifeNewsImage.save();
		}
	}
	
	/**
	 * 将LifeNewsImage实例從数据库刪除
	 */
	public void deleteLifeNewsImage(LifeNewsImage lifeNewsImage) {
		if (lifeNewsImage != null) {
			DataSupport.deleteAll(LifeNewsImage.class, "imageId = ?", lifeNewsImage.getImageId());
		}
	}
	
	/**
	 * 从数据库读取所有的HotNews信息
	 */
	public List<HotNews> loadAllHotNews() {
		List<HotNews> list = DataSupport.findAll(HotNews.class);
		return list;
	}
	
	/**
	 * 从数据库查詢指定hotNews信息是否存在
	 */
	public boolean isHotNewsFavorite (HotNews hotNews) {	
		List<HotNews> hotList = DataSupport.where("hotId = ?", hotNews.getHotId()).find(HotNews.class);
		if (hotList!=null && hotList.size()>0) {
			return true;
		}
		return false;
	}
	
	/**
	 * 将hotNews实例存储到数据库
	 */
	public void saveHotNews(HotNews hotNews) {
		if (hotNews != null) {
			boolean flag = hotNews.save();
			BaseTools.showlog("saveHotNews-flag="+flag);
			List<HotNews> alllist = loadAllHotNews();
			BaseTools.showlog("*****alllist.size="+alllist.size()+"   contains:"+alllist.contains(hotNews));
		}
	}
	
	/**
	 * 将hotNews实例從数据库刪除
	 */
	public void deleteHotNews(HotNews hotNews) {
		if (hotNews != null) {
			DataSupport.deleteAll(HotNews.class, "hotId = ?", hotNews.getHotId());
		}
	}
	
	/**
	 * 从数据库读取所有的blogItem信息
	 */
	public List<BlogItem> loadAllBlogItem() {
		List<BlogItem> list = DataSupport.findAll(BlogItem.class);
		return list;
	}
	
	/**
	 * 从数据库查詢指定BlogItem信息是否存在
	 */
	public boolean isBlogItemFavorite(BlogItem blogItem) {
		List<BlogItem> blogList = DataSupport.where("blogId = ?", blogItem.getBlogId()).find(BlogItem.class);
		if (blogList!=null && blogList.size()>0) {
			return true;
		}
		return false;
	}
	
	/**
	 * 将blogItem实例存储到数据库
	 */
	public void saveBlogItem(BlogItem blogItem) {
		if (blogItem != null) {
			blogItem.save();
		}
	}
	
	/**
	 * 将blogItem实例從数据库刪除
	 */
	public void deleteBlogItem(BlogItem blogItem) {
		if (blogItem != null) {
			DataSupport.deleteAll(BlogItem.class, "blogId = ?", blogItem.getBlogId());
		}
	}
	

	/**
	 * 关闭数据库
	 */
	public void destroyDB() {
		if (db != null) {
			db.close();
		}
	}

}

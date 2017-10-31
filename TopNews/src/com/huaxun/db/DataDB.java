package com.huaxun.db;

import java.util.ArrayList;

import com.huaxun.life.bean.LifeNews;
import com.huaxun.life.bean.LifeNewsImage;
import com.huaxun.music.bean.Mp3Info;
import com.huaxun.news.bean.News;
import com.huaxun.news.bean.Node;
import com.huaxun.radio.bean.RadioAudioURLDetail;
import com.huaxun.radio.bean.RadioItem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

public class DataDB extends SQLiteOpenHelper{
	
	private static DataDB instance = null;

	public static DataDB getInstance(Context context) {
		if (instance == null) {
			instance = new DataDB(context, "data.db", null, 6);
		}
		return instance;
	}

	public DataDB(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// 栏目
		db.execSQL("CREATE TABLE IF NOT EXISTS news_node (id Integer primary key autoincrement,nodeid VARCHAR,"
				+ "nodename VARCHAR,nodetype VARCHAR,newstype VARCHAR,nodeimg VARCHAR,nodeimgdown VARCHAR,nodeurl VARCHAR,"
				+ "objcount VARCHAR,isshare VARCHAR,iscomment VARCHAR,languagetype VARCHAR,isshowhd VARCHAR,isselected VARCHAR)");
		//Node对应的json_str		
		db.execSQL("CREATE TABLE IF NOT EXISTS data_cache(id Integer primary key autoincrement,nodename VARCHAR,json_str VARCHAR)");
		// 手动添加的电台
		db.execSQL("CREATE TABLE IF NOT EXISTS radio_item (id Integer primary key autoincrement,newstitle VARCHAR,"
				+ "newsintroduce VARCHAR,url VARCHAR,audioiconimage VARCHAR,imgurl1 VARCHAR,attri VARCHAR)");
		// 音乐播放列表
		db.execSQL("CREATE TABLE IF NOT EXISTS play_list (id Integer primary key autoincrement,musicId VARCHAR,"
				+ "title VARCHAR,album VARCHAR,albumId VARCHAR,displayName VARCHAR,artist VARCHAR,duration VARCHAR,"
				+ "size VARCHAR,path VARCHAR,lrcTitle VARCHAR,lrcSize VARCHAR)");
		//收藏的新闻
		db.execSQL("CREATE TABLE IF NOT EXISTS news_favorite(id Integer primary key autoincrement,newsid VARCHAR,"
				+ "newstitle VARCHAR,newsurl VARCHAR,imgurl1 VARCHAR,imgurl2 VARCHAR,imgurl3 VARCHAR,"
				+ "icontype VARCHAR,iscomment VARCHAR,isshare VARCHAR,shareurl VARCHAR,"
				+ "newstype VARCHAR,moreurl VARCHAR,style VARCHAR,source VARCHAR,createtime VARCHAR)");
		//收藏的生活新闻
		db.execSQL("CREATE TABLE IF NOT EXISTS life_favorite(id Integer primary key autoincrement,newsId VARCHAR,"
				+ "title VARCHAR,content VARCHAR,videoAndriodURL VARCHAR,image VARCHAR,date VARCHAR)");
		// 看过的新闻的id
		db.execSQL("CREATE TABLE IF NOT EXISTS news_id(id Integer primary key autoincrement,newsId VARCHAR)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// 栏目
		try {
			db.execSQL("CREATE TABLE IF NOT EXISTS news_node(id Integer primary key autoincrement,nodeid VARCHAR,"
				+ "nodename VARCHAR,nodetype VARCHAR,newstype VARCHAR,nodeimg VARCHAR,nodeimgdown VARCHAR,nodeurl VARCHAR,"
				+ "objcount VARCHAR,isshare VARCHAR,iscomment VARCHAR,languagetype VARCHAR,isshowhd VARCHAR,isselected VARCHAR)");
		} catch (Exception e) {
			System.out.println("-----ALTER出错了！-----");
		}
		//Node对应的json_str
		try {
			db.execSQL("CREATE TABLE IF NOT EXISTS data_cache(id Integer primary key autoincrement,nodename VARCHAR,json_str VARCHAR)");
		} catch (Exception e) {
			System.out.println("-----ALTER出错了！-----");
		}
		//手动添加的电台
		try {
			db.execSQL("CREATE TABLE IF NOT EXISTS radio_item (id Integer primary key autoincrement,newstitle VARCHAR,"
					+ "newsintroduce VARCHAR,url VARCHAR,audioiconimage VARCHAR,imgurl1 VARCHAR,attri VARCHAR)");
		} catch (Exception e) {
			System.out.println("-----ALTER出错了！-----");
		}
		// 音乐播放列表
		try {
			db.execSQL("CREATE TABLE IF NOT EXISTS play_list (id Integer primary key autoincrement,id VARCHAR,"
					+ "title VARCHAR,album VARCHAR,albumId VARCHAR,displayName VARCHAR,artist VARCHAR,duration VARCHAR,"
					+ "size VARCHAR,path VARCHAR,lrcTitle VARCHAR,lrcSize VARCHAR)");
		} catch (Exception e) {
			System.out.println("-----ALTER出错了！-----");
		}
		// 音乐播放列表
		try {
			db.execSQL("CREATE TABLE IF NOT EXISTS news_id(id Integer primary key autoincrement,newsId VARCHAR)");
		} catch (Exception e) {
			System.out.println("-----ALTER出错了！-----");
		}
	}	
	
	/************************************** data_cache表 **************************************/  
	public void addToDataCache(String nodename, String jsonStr) {
		if (TextUtils.isEmpty(jsonStr)) {
			return;
		}
		SQLiteDatabase db = getWritableDatabase();
		try {
			db.delete("data_cache", " nodename = ? ", new String[] { nodename });

			ContentValues cv = new ContentValues();
			cv.put("nodename", nodename);
			cv.put("json_str", jsonStr);
			db.insert("data_cache", null, cv);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getFromDataCache(String nodename) {
		String jsonStr = "";
		SQLiteDatabase db = getReadableDatabase();
		Cursor mCursor = null;
		try {
			mCursor = db.query("data_cache", null, " nodename = ? ", new String[] { nodename }, null, null, null);
			if (mCursor.moveToFirst()) {
				jsonStr = mCursor.getString(mCursor.getColumnIndex("json_str"));
			}
		} finally {
			if (null != mCursor) {
				if (!mCursor.isClosed()) {
					mCursor.close();
				}
				mCursor = null;
			}
		}
		return jsonStr;
	}

	
	/************************************** news_node表 **************************************/  
	public void InitialAllNodes(ArrayList<Node> allNodes) {
		if (null == allNodes || allNodes.size() <= 0) {
			return;
		}
		SQLiteDatabase db = getWritableDatabase();
		try {
			db.delete("news_node", null, null);

			ContentValues cv = null;
			for (Node node : allNodes) {
				cv = new ContentValues();
				cv.put("nodeid", node.nodeid);
				cv.put("nodename", node.nodename);
				cv.put("nodetype", node.nodetype);
				cv.put("newstype", node.newstype);
				cv.put("nodeimg", node.nodeimg);
				cv.put("nodeimgdown", node.nodeimgdown);
				cv.put("nodeurl", node.nodeurl);
				cv.put("objcount", node.objcount);
				cv.put("languagetype", node.languagetype);
				cv.put("isshare", node.isshare);
				cv.put("iscomment", node.iscomment);
				cv.put("isshowhd", node.isshowhd);
				if ((node.nodename).equals("今日推荐")||(node.nodename).equals("视　频")||(node.nodename).equals("国　内")||(node.nodename).equals("体　育")) {
				cv.put("isselected", "1");
				} else {
				cv.put("isselected", "0");	
				}
				db.insert("news_node", null, cv);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public ArrayList<Node> getUserNodes() {
		ArrayList<Node> nodes = new ArrayList<Node>();
		SQLiteDatabase db = getReadableDatabase();
		Cursor mCursor = null;
		try {
			mCursor = db.query("news_node", null , " isselected = ? ", new String[] {"1"}, null, null, null);
			Node node = null;
			while (mCursor.moveToNext()) {
				String nodeid = mCursor.getString(mCursor.getColumnIndex("nodeid"));
				String nodename = mCursor.getString(mCursor.getColumnIndex("nodename"));
				String nodetype = mCursor.getString(mCursor.getColumnIndex("nodetype"));
				String newstype = mCursor.getString(mCursor.getColumnIndex("newstype"));
				String nodeimg = mCursor.getString(mCursor.getColumnIndex("nodeimg"));
				String nodeimgdown = mCursor.getString(mCursor.getColumnIndex("nodeimgdown"));
				String nodeurl = mCursor.getString(mCursor.getColumnIndex("nodeurl"));
				String objcount = mCursor.getString(mCursor.getColumnIndex("objcount"));
				String isshare = mCursor.getString(mCursor.getColumnIndex("isshare"));
				String iscomment = mCursor.getString(mCursor.getColumnIndex("iscomment"));
				String languagetype = mCursor.getString(mCursor.getColumnIndex("languagetype"));
				String isshowhd = mCursor.getString(mCursor.getColumnIndex("isshowhd"));
				String isselected = mCursor.getString(mCursor.getColumnIndex("isselected"));
				node = new Node(nodeid, nodename, nodetype, newstype, nodeimg, nodeimgdown, nodeurl, objcount, isshare, iscomment, languagetype, isshowhd, isselected);
				nodes.add(node);
			}
		} finally {
			if (null != mCursor) {
				if (!mCursor.isClosed()) {
					mCursor.close();
				}
				mCursor = null;
			}
		}
		return nodes;
	}
	
	public ArrayList<Node> getOtherNodes() {
		ArrayList<Node> nodes = new ArrayList<Node>();
		SQLiteDatabase db = getReadableDatabase();
		Cursor mCursor = null;
		try {
			mCursor = db.query("news_node", null , " isselected = ? ", new String[] {"0"}, null, null, null);
			Node node = null;
			while (mCursor.moveToNext()) {
				String nodeid = mCursor.getString(mCursor.getColumnIndex("nodeid"));
				String nodename = mCursor.getString(mCursor.getColumnIndex("nodename"));
				String nodetype = mCursor.getString(mCursor.getColumnIndex("nodetype"));
				String newstype = mCursor.getString(mCursor.getColumnIndex("newstype"));
				String nodeimg = mCursor.getString(mCursor.getColumnIndex("nodeimg"));
				String nodeimgdown = mCursor.getString(mCursor.getColumnIndex("nodeimgdown"));
				String nodeurl = mCursor.getString(mCursor.getColumnIndex("nodeurl"));
				String objcount = mCursor.getString(mCursor.getColumnIndex("objcount"));
				String isshare = mCursor.getString(mCursor.getColumnIndex("isshare"));
				String iscomment = mCursor.getString(mCursor.getColumnIndex("iscomment"));
				String languagetype = mCursor.getString(mCursor.getColumnIndex("languagetype"));
				String isshowhd = mCursor.getString(mCursor.getColumnIndex("isshowhd"));
				String isselected = mCursor.getString(mCursor.getColumnIndex("isselected"));
				node = new Node(nodeid, nodename, nodetype, newstype, nodeimg, nodeimgdown, nodeurl, objcount, isshare, iscomment, languagetype, isshowhd, isselected);
				nodes.add(node);
			}
		} finally {
			if (null != mCursor) {
				if (!mCursor.isClosed()) {
					mCursor.close();
				}
				mCursor = null;
			}
		}
		return nodes;
	}
	
    public void addNodeToEnd(Node node) {
		SQLiteDatabase db = getWritableDatabase();		
		try {
			db.delete("news_node", "nodename=?", new String[]{node.nodename});
			ContentValues cv = new ContentValues();
			cv.put("nodename", node.nodename);
			cv.put("nodetype", node.nodetype);
			cv.put("newstype", node.newstype);
			cv.put("nodeimg", node.nodeimg);
			cv.put("nodeimgdown", node.nodeimgdown);
			cv.put("nodeurl", node.nodeurl);
			cv.put("objcount", node.objcount);
			cv.put("languagetype", node.languagetype);
			cv.put("isshare", node.isshare);
			cv.put("iscomment", node.iscomment);
			cv.put("isshowhd", node.isshowhd);
			cv.put("isselected", node.isselected);	

			db.insert("news_node", null, cv);
		}  catch (Exception e) {
			e.printStackTrace();
		}
		return;		
	}
	/************************************** life_favorite表 **************************************/   
	/*
	 *  添加新闻收藏
	 */
	public void addToLifeFavorite(LifeNews lifeNews){
		if (lifeNews == null) {
			return;
		}
		
		SQLiteDatabase db = getWritableDatabase();
		try {
			db.delete("life_favorite", " newsId = ? ", new String[] { lifeNews.getNewsId() });

			ContentValues cv = new ContentValues();			
			cv.put("newsId", lifeNews.getNewsId());
			cv.put("title", lifeNews.getTitle());
			cv.put("content", lifeNews.getContent());
			cv.put("videoAndriodURL", lifeNews.getVideoAndriodURL());
			cv.put("image", lifeNews.getImage().getSrc());
			cv.put("date", lifeNews.getDate());
			db.insert("life_favorite", null, cv);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 *  删除新闻收藏
	 */
	public void deleteFromLifeFavorite(LifeNews lifeNews){
		SQLiteDatabase db = getWritableDatabase();
		try {
			db.delete("life_favorite", " newsId = ? ", new String[] { lifeNews.getNewsId() });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 *  检测新闻是否已经收藏
	 */
	public boolean isFavorite(LifeNews lifeNews){
		SQLiteDatabase db = getReadableDatabase();
		Cursor mCursor = null;
		try {
			mCursor = db.query("life_favorite", null, " newsId = ? ", new String[] { lifeNews.getNewsId() }, null, null, null);
			if (mCursor.moveToFirst()) {
				return true;
			}
		} finally {
			if (null != mCursor) {
				if (!mCursor.isClosed()) {
					mCursor.close();
				}
				mCursor = null;
			}
		}
		return false;
	}
	
	/*
	 *  返回新闻收藏数据
	 */
	public ArrayList<LifeNews> getLifeFavorite(){
		ArrayList<LifeNews> newsList = new ArrayList<LifeNews> ();
		SQLiteDatabase db = getReadableDatabase();
		Cursor mCursor = null;
		try {
			mCursor = db.query("life_favorite", null, null, null, null, null,null);
			if (mCursor.getCount() == 0)
				return newsList;
			
			mCursor.moveToLast();
			while (!mCursor.isBeforeFirst()) {
				String newsId = mCursor.getString(mCursor.getColumnIndex("newsId"));
				String title = mCursor.getString(mCursor.getColumnIndex("title"));
				String content = mCursor.getString(mCursor.getColumnIndex("content"));
				String videoAndriodURL = mCursor.getString(mCursor.getColumnIndex("videoAndriodURL"));
				String image = mCursor.getString(mCursor.getColumnIndex("image"));
				String date = mCursor.getString(mCursor.getColumnIndex("date"));
				LifeNewsImage lifeNewsImage = new LifeNewsImage();
				lifeNewsImage.setSrc(image);				
				LifeNews lifeNews = new LifeNews(newsId, title, content, videoAndriodURL, lifeNewsImage, date);
				newsList.add(lifeNews);
				mCursor.moveToPrevious();
			}
		} finally {			
			if (null != mCursor) {
				if (!mCursor.isClosed()) {
					mCursor.close();
				}
				mCursor = null;
			}
		}
		return newsList;
	}
	
	/************************************** news_favorite表 **************************************/   
	/*
	 *  添加新闻收藏
	 */
	public void addToUserFavorite(News news){
		if (news == null) {
			return;
		}
		
		SQLiteDatabase db = getWritableDatabase();
		try {
			db.delete("news_favorite", " newsid = ? ", new String[] { news.getNewsid() });

			ContentValues cv = new ContentValues();
			cv.put("newsid", news.getNewsid());
			cv.put("newstitle", news.getNewstitle());
			cv.put("newsurl", news.getNewsurl());
			cv.put("imgurl1", news.getImgurl1());
			cv.put("imgurl2", news.getImgurl2());
			cv.put("imgurl3", news.getImgurl3());
			cv.put("icontype", news.getIcontype());
			cv.put("iscomment", news.iscomment);
			cv.put("isshare", news.isshare);
			cv.put("shareurl", news.shareurl);
			cv.put("newstype", news.getNewstype());
			cv.put("moreurl", news.moreurl);
			cv.put("style", news.getStyle());
			cv.put("source", news.source);
			cv.put("createtime", news.getCreatetime());
			db.insert("user_favorite", null, cv);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 *  删除新闻收藏
	 */
	public void deleteFromUserFavorite(News news){
		SQLiteDatabase db = getWritableDatabase();
		try {
			db.delete("news_favorite", " newsid = ? ", new String[] { news.getNewsid() });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 *  检测新闻是否已经收藏
	 */
	public boolean isFavorite(News news){
		SQLiteDatabase db = getReadableDatabase();
		Cursor mCursor = null;
		try {
			mCursor = db.query("news_favorite", null, " newsid = ? ", new String[] { news.getNewsid() }, null, null, null);
			if (mCursor.moveToFirst()) {
				return true;
			}
		} finally {
			if (null != mCursor) {
				if (!mCursor.isClosed()) {
					mCursor.close();
				}
				mCursor = null;
			}
		}
		return false;
	}
	
	/*
	 *  返回新闻收藏数据
	 */
	public ArrayList<News> getUserFavorite(){
		ArrayList<News> newsList = new ArrayList<News> ();
		SQLiteDatabase db = getReadableDatabase();
		Cursor mCursor = null;
		try {
			mCursor = db.query("news_favorite", null, null, null, null, null,null);
			if (mCursor.getCount() == 0)
				return newsList;
			
			mCursor.moveToLast();
			while (!mCursor.isBeforeFirst()) {
				String newsid = mCursor.getString(mCursor.getColumnIndex("newsid"));
				String newstitle = mCursor.getString(mCursor.getColumnIndex("newstitle"));
				String newsurl = mCursor.getString(mCursor.getColumnIndex("newsurl"));
				String imgurl1 = mCursor.getString(mCursor.getColumnIndex("imgurl1"));
				String imgurl2 = mCursor.getString(mCursor.getColumnIndex("imgurl2"));
				String imgurl3 = mCursor.getString(mCursor.getColumnIndex("imgurl3"));
				String icontype = mCursor.getString(mCursor.getColumnIndex("icontype"));
				String iscomment = mCursor.getString(mCursor.getColumnIndex("iscomment"));
				String isshare = mCursor.getString(mCursor.getColumnIndex("isshare"));
				String shareurl = mCursor.getString(mCursor.getColumnIndex("shareurl"));
				String newstype = mCursor.getString(mCursor.getColumnIndex("newstype"));
				String moreurl = mCursor.getString(mCursor.getColumnIndex("moreurl"));
				String style = mCursor.getString(mCursor.getColumnIndex("style"));
				String source = mCursor.getString(mCursor.getColumnIndex("source"));
				String createtime = mCursor.getString(mCursor.getColumnIndex("createtime"));
				News news = new News();
				news.setDate(newsid, newstitle, newsurl, imgurl1, imgurl2, imgurl3, icontype, iscomment, isshare, shareurl, newstype, moreurl, style, source,createtime);
				newsList.add(news);
				mCursor.moveToPrevious();
			}

		} finally {			
			if (null != mCursor) {
				if (!mCursor.isClosed()) {
					mCursor.close();
				}
				mCursor = null;
			}
		}
		return newsList;
	}
	
	/************************************* 新闻ID缓存 *************************************/
	public void addIdCache(String newsId) {
		if (TextUtils.isEmpty(newsId)) {
			return;
		}
		SQLiteDatabase db = getWritableDatabase();
		try {
			ContentValues cv = new ContentValues();
			cv.put("newsId", newsId);
			db.insert("news_id", null, cv);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean queryIdCache(String newsId) {
		boolean isCached = false;
		SQLiteDatabase db = getReadableDatabase();
		Cursor mCursor = null;
		try {
			mCursor = db.query("news_id", null, " newsId = ? ", new String[] { newsId }, null, null, null);
			if (mCursor.moveToFirst()) {
				isCached = true;
			}
		} finally {
			if (null != mCursor) {
				if (!mCursor.isClosed()) {
					mCursor.close();
				}
				mCursor = null;
			}
		}
		return isCached;
	}
    
	/************************************** radio_item表 **************************************/
	public void updateRadioItem(RadioItem radioitem,String attri) {
		if (null == radioitem || TextUtils.isEmpty(radioitem.newstitle)) {
			return;
		}
		SQLiteDatabase db = getWritableDatabase();
		try {
			db.delete("radio_item", " newstitle = ? ", new String[] { radioitem.newstitle });

			ContentValues cv = new ContentValues();
			cv.put("newstitle", radioitem.newstitle);
			cv.put("newsintroduce", radioitem.newsintroduce);
			cv.put("url", radioitem.audiourl.get(0).url);
			cv.put("audioiconimage", radioitem.audioiconimage);
			cv.put("imgurl1", radioitem.imgurl1);
			cv.put("attri", attri);
			db.insert("radio_item", null, cv);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void deleteRadioItem(RadioItem radioitem,String attri) {
		if (null == radioitem || TextUtils.isEmpty(radioitem.newstitle)) {
			return;
		}
		SQLiteDatabase db = getWritableDatabase();
		try {
			db.delete("radio_item", " newstitle = ? and attri = ? ", new String[] { radioitem.newstitle, attri});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("null")
	public ArrayList<RadioItem> selectAllRadioItems(String attriInput) {
		ArrayList<RadioItem> radioitems = new ArrayList<RadioItem>();
		SQLiteDatabase db = getReadableDatabase();
		Cursor mCursor = null;
		try {
			mCursor = db.query("radio_item", null, null, null, null, null, null);
			RadioAudioURLDetail radioaudiourldetail = new RadioAudioURLDetail();
			RadioItem radioitem = new RadioItem();
			ArrayList<RadioAudioURLDetail> audiourl = new ArrayList<RadioAudioURLDetail>();
			while (mCursor.moveToNext()) {
				String newstitle = mCursor.getString(mCursor.getColumnIndex("newstitle"));
				String newsintroduce = mCursor.getString(mCursor.getColumnIndex("newsintroduce"));
				String url = mCursor.getString(mCursor.getColumnIndex("url"));
				String audioiconimage = mCursor.getString(mCursor.getColumnIndex("audioiconimage"));
				String imgurl1 = mCursor.getString(mCursor.getColumnIndex("imgurl1"));
				String attri = mCursor.getString(mCursor.getColumnIndex("attri"));

				if (attriInput.equals(attri)){
					radioaudiourldetail = new RadioAudioURLDetail(url);
					audiourl.add(radioaudiourldetail);
					radioitem = new RadioItem(newstitle, newsintroduce, audiourl, imgurl1, audioiconimage);
					radioitems.add(radioitem);
				}
			}
		} finally {
			if (null != mCursor) {
				if (!mCursor.isClosed()) {
					mCursor.close();
				}
				mCursor = null;
			}
		}
		return radioitems;
	}
	
	/************************************** Mp3Info表 **************************************/
	public ArrayList<Mp3Info> selectAllMp3Info() {
		ArrayList<Mp3Info> mp3Infos = new ArrayList<Mp3Info>();
		SQLiteDatabase db = getReadableDatabase();
		Cursor mCursor = null;
		try {
			mCursor = db.query("play_list", null, null, null, null, null, null);
			Mp3Info mp3Info = new Mp3Info();
			while (mCursor.moveToNext()) {
				long id = mCursor.getLong(mCursor.getColumnIndex("musicId"));
				String title = mCursor.getString(mCursor.getColumnIndex("title"));
				String album = mCursor.getString(mCursor.getColumnIndex("album"));
				long albumId = mCursor.getLong(mCursor.getColumnIndex("albumId"));
				String displayName = mCursor.getString(mCursor.getColumnIndex("displayName"));
				String artist = mCursor.getString(mCursor.getColumnIndex("artist"));
				long duration = mCursor.getLong(mCursor.getColumnIndex("duration"));
				long size = mCursor.getLong(mCursor.getColumnIndex("size"));
				String path = mCursor.getString(mCursor.getColumnIndex("path"));
				String lrcTitle = mCursor.getString(mCursor.getColumnIndex("lrcTitle"));
				String lrcSize = mCursor.getString(mCursor.getColumnIndex("lrcSize"));

				mp3Info = new Mp3Info(id,title,album,albumId,displayName,artist,duration,size,path,lrcTitle,lrcSize);
				mp3Infos.add(mp3Info);
			}
		} finally {
			if (null != mCursor) {
				if (!mCursor.isClosed()) {
					mCursor.close();
				}
				mCursor = null;
			}
		}
		return mp3Infos;
	}
	

	public void deleteAllMp3Info() {		
		SQLiteDatabase db = getWritableDatabase();
		try {
			db.delete("play_list", null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void addMp3Info(Mp3Info mp3Info) {
		if (null == mp3Info) {
			return;
		}
		SQLiteDatabase db = getWritableDatabase();
		try {
			ContentValues cv = new ContentValues();
			cv.put("musicId", mp3Info.getId());
			cv.put("title", mp3Info.getTitle());
			cv.put("album", mp3Info.getAlbum());
			cv.put("albumId", mp3Info.getAlbumId());
			cv.put("displayName", mp3Info.getDisplayName());
			cv.put("artist", mp3Info.getArtist());
			cv.put("duration", mp3Info.getDuration());
			cv.put("size", mp3Info.getSize());
			cv.put("path", mp3Info.getPath());
			cv.put("lrcTitle", mp3Info.getLrcTitle());
			cv.put("lrcSize", mp3Info.getLrcSize());
			db.insert("play_list", null, cv);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	public void deleteMp3Info(String title) {
		if (TextUtils.isEmpty(title)) {
			return;
		}
		SQLiteDatabase db = getWritableDatabase();
		try {
			db.delete("play_list", " title = ? ", new String[] {title});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}

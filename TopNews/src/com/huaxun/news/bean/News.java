package com.huaxun.news.bean;

import java.io.Serializable;
import java.util.List;

import org.litepal.crud.DataSupport;

import com.huaxun.music.bean.Mp3Info;

public class News extends DataSupport implements Serializable {
	private static final long serialVersionUID = 1L;
	//只有声明成private修饰符的字段才会被映射到数据库表中
	private String newsid;
	private String newstitle;
	private String newsurl;
	private String imgurl1;
	private String imgurl2;
	private String imgurl3;
	private String createtime;
	private String style;
	private String icontype;
	private String newstype;
	
	public String newstime;
	public String mp3url;
	public String sourcename;
	public String parentnodeid;
	public String iscomment;
	public String isshare;
	public String shareurl;
	public String moreurl;
	public String nodetype;
	public String nodeid;
	public String batchid;
	public String summary;
	public String showlocation;
	public String languagetype;
	public String source;
	public boolean fromPush = false;
	public boolean fromSpecial = false;
	public String parentNodeName = "";
	public String time;
	public String tagname;
	public News nextNews = null;
	
	public News(){}
	
	public News(String newsid,String newstitle, String imgurl1, String createtime, String source){
		this.newsid = newsid;
		this.newstitle = newstitle;
		this.imgurl1 = imgurl1;
		this.createtime = createtime;
		this.source = source;
	}
	
	public String getNewsid() {
		return newsid;
	}

	public void setNewsid(String newsid) {
		this.newsid = newsid;
	}

	public String getNewstitle() {
		return newstitle;
	}

	public void setNewstitle(String newstitle) {
		this.newstitle = newstitle;
	}

	public String getNewsurl() {
		return newsurl;
	}

	public void setNewsurl(String newsurl) {
		this.newsurl = newsurl;
	}

	public String getImgurl1() {
		return imgurl1;
	}

	public void setImgurl1(String imgurl1) {
		this.imgurl1 = imgurl1;
	}
	
	public String getImgurl2() {
		return imgurl2;
	}

	public void setImgurl2(String imgurl2) {
		this.imgurl2 = imgurl2;
	}
	
	public String getImgurl3() {
		return imgurl3;
	}

	public void setImgurl3(String imgurl3) {
		this.imgurl3 = imgurl3;
	}

	public String getCreatetime() {
		return createtime;
	}

	public void setCreatetime(String createtime) {
		this.createtime = createtime;
	}
	
	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}	
	
	public String getIcontype() {
		return icontype;
	}

	public void setIcontype(String icontype) {
		this.icontype = icontype;
	}
	
	public String getNewstype() {
		return newstype;
	}

	public void setNewstype(String newstype) {
		this.newstype = newstype;
	}

	public void setDate(String newsid,String newstitle, String newsurl, String imgurl1, String imgurl2, String imgurl3,String icontype, String iscomment, String isshare, String shareurl, String newstype, String moreurl, String style, String source, String createtime){
		this.newsid = newsid;
		this.newstitle = newstitle;
		this.newsurl = newsurl;
		this.imgurl1 = imgurl1;
		this.imgurl2 = imgurl2;
		this.imgurl3 = imgurl3;
		this.icontype = icontype;
		this.iscomment = iscomment;
		this.isshare = isshare;
		this.shareurl = shareurl;
		this.newstype = newstype;
		this.style = style;
		this.moreurl = moreurl;
		this.source = source;
		this.createtime = createtime;
	}
	
    //注意，这里重写GetHashCode和equals方法
    public int hashCode()
    {
        return Integer.parseInt(newsid);
    } 
       
    public boolean equals(Object object) {
      if(object == null)
         return false;
    	
      if(object == this)
         return true;

      if(object instanceof News) {
    	  News news = (News)object;
    	  //这里以newsurl作为比较标准，因为SpecialFragment，DoctorFragment等传给WebActivity的构造News只有newsurl
    	  return news.newsurl == this.newsurl;
       }
       return false;
    }
	
}

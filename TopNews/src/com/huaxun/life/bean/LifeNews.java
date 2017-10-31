package com.huaxun.life.bean;

import java.io.Serializable;

import org.litepal.crud.DataSupport;

public class LifeNews extends DataSupport implements Serializable {
	private static final long serialVersionUID = 1L;

	private String newsId;
	private String title;
	private String content;
	private String videoAndriodURL;
	private LifeNewsImage image;
	private String date;
	
	public LifeNews(String newsId, String title, String content, String videoAndriodURL, LifeNewsImage image, String date) {
		this.newsId = newsId;
		this.title = title;
		this.content = content;
		this.videoAndriodURL = videoAndriodURL;
		this.image = image;
		this.date = date;
	}

	public String getNewsId() {
		return newsId;
	}

	public void setNewsId(String newsId) {
		this.newsId = newsId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getVideoAndriodURL() {
		return videoAndriodURL;
	}

	public void setVideoAndriodURL(String videoAndriodURL) {
		this.videoAndriodURL = videoAndriodURL;
	}

	public LifeNewsImage getImage() {
		return image;
	}

	public void setImage(LifeNewsImage image) {
		this.image = image;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
}

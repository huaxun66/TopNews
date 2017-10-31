package com.huaxun.life.bean;

import java.io.Serializable;

import org.litepal.crud.DataSupport;


/**
 * 单条新闻
 * 
 * @author zhou.ni 2015年3月22日
 */
public class HotNews extends DataSupport implements Serializable {

	private static final long serialVersionUID = 1L;

	private String hotId; 			// id
	private String title; 			// 标题
	private String summary; 		// 简介
	private String published; 		// 发布时间（格林威治时间）
	private String updated; 		// 更新时间
	private String link; 			// 链接
	private String diggs; 			// 获得赞数
	private String views; 			// 赞同观点数
	private String comments; 		// 评论数
	private String topic; 			// 置顶
	private String topicIcon; 		// 置顶图标
	private String sourceName; 		// 文章来源

	public String getHotId() {
		return hotId;
	}

	public void setHotId(String hotId) {
		this.hotId = hotId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getPublished() {
		return published;
	}

	public void setPublished(String published) {
		this.published = published;
	}

	public String getUpdated() {
		return updated;
	}

	public void setUpdated(String updated) {
		this.updated = updated;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getDiggs() {
		return diggs;
	}

	public void setDiggs(String diggs) {
		this.diggs = diggs;
	}

	public String getViews() {
		return views;
	}

	public void setViews(String views) {
		this.views = views;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getTopicIcon() {
		return topicIcon;
	}

	public void setTopicIcon(String topicIcon) {
		this.topicIcon = topicIcon;
	}

	public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	@Override
	public String toString() {
		return "News [id=" + hotId + ", title=" + title + ", summary=" + summary
				+ ", published=" + published + ", updated=" + updated
				+ ", link=" + link + ", diggs=" + diggs + ", views=" + views
				+ ", comments=" + comments + ", topic=" + topic
				+ ", topicIcon=" + topicIcon + ", sourceName=" + sourceName
				+ "]";
	}
	
    //注意，这里重写HashCode和equals方法
    public int hashCode()
    {
        return Integer.parseInt(hotId);
    } 
	
	@Override
	public boolean equals(Object o) {
		if ( o instanceof HotNews ) {
			HotNews news = (HotNews) o;
			return this.hotId.equals(news.hotId);
		}
		return super.equals(o);
	}

}

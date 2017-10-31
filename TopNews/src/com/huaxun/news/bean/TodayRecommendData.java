package com.huaxun.news.bean;

import java.io.Serializable;
import java.util.ArrayList;

public class TodayRecommendData implements Serializable {

	private static final long serialVersionUID = 1L;
	public ArrayList<News> bigimages;
	public ArrayList<Node> fixnode;
	public ArrayList<Node> allnodes;
	public NewsList newslist;
	public NewsList imageslist;
	public NewsList newslist2;
	public NewsList imageslist2;
}
package com.huaxun.radio.bean;

import java.io.Serializable;
import java.util.ArrayList;

public class RadioCover implements Serializable{
	
	private static final long serialVersionUID = 1L;
	public String nodeid;
	public String nodename;
	public ArrayList<RadioItem> bigimagelist;
	public ArrayList<RadioItem> recommendlist;
	public RadioNewsList newslist1;
	public RadioNewsList newslist2;
}

package com.huaxun.radio.bean;

import java.io.Serializable;
import java.util.ArrayList;

public class RadioNewsList implements Serializable{
	
	private static final long serialVersionUID = 1L;
	public String title;
	public String moreurl;
	public ArrayList<RadioItem> list;
}

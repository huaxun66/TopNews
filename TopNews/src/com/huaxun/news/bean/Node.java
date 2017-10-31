package com.huaxun.news.bean;

import java.io.Serializable;


public class Node implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public String nodeid;
	public String nodename;
	public String nodetype;
	public String newstype;
	public String nodeimg;
	public String nodeimgdown;
	public boolean imgChanged;
	public boolean imgDownChanged;
	public String nodeurl;
	public String objcount;
	public int resultCount;
	public int fixed;
	public String newtime;// 新闻时间，申活圈中使用到
	public String hdnodeimg;
	public String hdnodeimgdown;

	public byte[] bitmap;
	public byte[] bitmap2;

	public String isshare;
	public String iscomment;
	public String languagetype;
	public String isshowhd;
	
	//被选择（我的频道）："1" 没有被选择（更多频道）："0"
	public String isselected;
	
	public Node() {
	}
	
	public Node(String nodeid, String nodename) {
		this.nodeid = nodeid;
		this.nodename = nodename;
	}

	public Node(String nodeid, String nodename, String nodetype, String newstype, String nodeimg, String nodeimgdown,
			String nodeurl, String objcount, String isshare, String iscomment, String languagetype, String isshowhd, String isselected) {
		this.nodeid = nodeid;
		this.nodename = nodename;
		this.nodetype = nodetype;
		this.newstype = newstype;
		this.nodeimg = nodeimg;
		this.nodeimgdown = nodeimgdown;
		this.nodeurl = nodeurl;
		this.objcount = objcount;
		this.isshare = isshare;
		this.iscomment = iscomment;
		this.languagetype = languagetype;
		this.isshowhd = isshowhd;
		this.isselected = isselected;
	}

	@Override
	public int hashCode() {
		return nodeid.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof Node)) {
			return false;
		}
		if (this == obj)
			return true;
		Node other = (Node) obj;
		if (this.nodename.equals(other.nodename))
			return true;
		return false;
	}

}

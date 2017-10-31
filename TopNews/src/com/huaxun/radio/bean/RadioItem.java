package com.huaxun.radio.bean;

import java.io.Serializable;
import java.util.ArrayList;

public class RadioItem implements Serializable {

	private static final long serialVersionUID = 1L;
	public String newsid = "";
	public String newstitle = "";
	public String newsintroduce = "";
	public ArrayList<RadioAudioURLDetail> audiourl = new ArrayList<RadioAudioURLDetail>();
	public String imgurl1 = "";
	public String imgurl2 = "";
	public String imgurl3 = "";
	public String audioiconimage = "";
	public String parentnodeid = "";
	public String iscomment = "";
	public String isshare = "";
	public String newstype = "";
	public String nodetype = "";
	public String style = "";
	public String shareurl = "";

	public RadioItem() {
	}

	public RadioItem(String newstitle, String newsintroduce, ArrayList<RadioAudioURLDetail> audiourl, String imgurl1,
			String audioiconimage) {
		this.newstitle = newstitle;
		this.newsintroduce = newsintroduce;
		this.audiourl = audiourl;
		this.imgurl1 = imgurl1;
		this.audioiconimage = audioiconimage;
	}
}

package com.huaxun.radio.bean;

import java.io.Serializable;

public class RadioAudioURLDetail implements Serializable {

	private static final long serialVersionUID = 1L;
	public String title = "";
	public String url = "";

	public RadioAudioURLDetail() {
	}

	public RadioAudioURLDetail(String url) {
		this.url = url;
	}
}
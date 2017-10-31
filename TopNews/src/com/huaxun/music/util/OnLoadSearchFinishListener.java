package com.huaxun.music.util;

import java.util.List;

import com.huaxun.music.bean.Mp3Info;

public interface OnLoadSearchFinishListener {
	void onLoadSucess(List<Mp3Info> musicList);

	void onLoadFail();
}

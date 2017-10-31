package com.huaxun.music;

import java.util.ArrayList;
import java.util.List;

import com.huaxun.music.activity.MusicDetailActivity;
import com.huaxun.music.bean.LrcContent;
import com.huaxun.tool.BaseTools;
import com.huaxun.utils.MediaUtil;
import com.huaxun.utils.Util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;

/**
 * 自定义绘画歌词，产生滚动效果
 * @author wwj
 *
 */
public class LrcView extends android.widget.TextView {
	private float width;		//歌词视图宽度
	private float height;		//歌词视图高度
	private Paint currentPaint;	//当前画笔对象
	private Paint notCurrentPaint;	//非当前画笔对象
	private Paint progressPaint;	//进度画笔对象
	private float textHeight = Util.dip2px(getContext(), 25);	//文本高度
	private int index = 0;		//list集合下标
	public boolean showprogress = false;//滑动时显示进度
	public float drifty = 0;    //y偏移量       drifty>0时，向下滑动,drifty<0时，向上滑动
	public int middleLineIndex = 0;
	
	public List<LrcContent> mLrcList = new ArrayList<LrcContent>();

	
	public void setmLrcList(List<LrcContent> mLrcList) {
		this.mLrcList = mLrcList;
	}

	public LrcView(Context context) {
		super(context);
		init();
	}
	public LrcView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public LrcView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		setFocusable(true);		//设置可对焦
		
		//高亮部分
		currentPaint = new Paint();
		currentPaint.setAntiAlias(true);	//设置抗锯齿，让文字美观饱满
		currentPaint.setTextAlign(Paint.Align.CENTER);//设置文本对齐方式
		currentPaint.setColor(Color.argb(210, 251, 248, 29));
		currentPaint.setTextSize(Util.dip2px(getContext(), 22));
		currentPaint.setTypeface(Typeface.SERIF);
		
		//非高亮部分
		notCurrentPaint = new Paint();
		notCurrentPaint.setAntiAlias(true);
		notCurrentPaint.setTextAlign(Paint.Align.CENTER);
		notCurrentPaint.setColor(Color.argb(140, 255, 255, 255));
		notCurrentPaint.setTextSize(Util.dip2px(getContext(), 18));
		notCurrentPaint.setTypeface(Typeface.DEFAULT);
		
		//画当前播放进度提示
		progressPaint = new Paint();
		progressPaint.setAntiAlias(true);
		progressPaint.setTextAlign(Paint.Align.LEFT);
		progressPaint.setColor(Color.RED);
		progressPaint.setTextSize(Util.dip2px(getContext(), 14));
		progressPaint.setTypeface(Typeface.SANS_SERIF);
		
	}
	
	/**
	 * 绘画歌词
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(canvas == null) {
			return;
		}	
		try {
			setText("");
			
			middleLineIndex = getMiddleLineIndex();
			if (showprogress == true) {
				canvas.drawText(MediaUtil.getDurationStr(mLrcList.get(middleLineIndex).getLrcTime()/1000), 0, height / 2, progressPaint);
				canvas.drawLine(0, height / 2, width, height / 2, progressPaint);
			}

			//画出当前播放句
			canvas.drawText(mLrcList.get(index).getLrcStr(), width / 2, height / 2 + drifty, currentPaint);
			
			float tempY = height / 2  + drifty;
			//画出本句之前的句子
			for(int i = index - 1; i >= 0; i--) {
				//向上推移
				tempY = tempY - textHeight;
				canvas.drawText(mLrcList.get(i).getLrcStr(), width / 2, tempY, notCurrentPaint);
			}
			
			tempY = height / 2  + drifty;
			//画出本句之后的句子
			for(int i = index + 1; i < mLrcList.size(); i++) {
				//往下推移
				tempY = tempY + textHeight;
				canvas.drawText(mLrcList.get(i).getLrcStr(), width / 2, tempY, notCurrentPaint);
			} 
		} catch (Exception e) {
			if (MusicDetailActivity.noLrcFlag == true) {
				canvas.drawText("木有歌词文件，赶紧去下载吧。。。", width / 2, height / 2, notCurrentPaint);
			} else {
				canvas.drawText("点击显示按钮可以看到歌词哦。。。", width / 2, height / 2, notCurrentPaint);			
			}
		}
	}
	
	private int getMiddleLineIndex() {
		int middleIndex;
		int MoveIndex = 0;
		//这里移动的行数我是按四舍五入来的，大于0.5行算1行，小于不算
		if (drifty > 0) {
			MoveIndex = (int)(drifty/textHeight+0.5);      //eg,4.3->4   4.8->5
		} else {
			MoveIndex = (int)(drifty/textHeight-0.5);      //eg,-4.3->-4   -4.8->-5
		}
        //drifty>0时，向下滑动，此时MoveIndex>0,middleIndex小于index
		//drifty<0时，向上滑动，此时MoveIndex<0,middleIndex大于index
		middleIndex = index - MoveIndex;
		if (middleIndex < 0) {
			middleIndex = 0;
		}
		if (middleIndex >= mLrcList.size()) {
			middleIndex = mLrcList.size() -1;
		}
		return middleIndex;
	}

	/**
	 * 当view大小改变的时候调用的方法
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		this.width = w;
		this.height = h; 
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
}

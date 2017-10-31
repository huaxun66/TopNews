package com.huaxun.news.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.huaxun.R;
import com.huaxun.news.bean.ImgBowseInfo;
import com.huaxun.news.bean.ImgInfo;
import com.huaxun.tool.VolleyTool;
import com.huaxun.utils.Util;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.polites.android.GestureImageView;

public class PicBrowserActivity extends Activity implements Listener<JSONObject>,ErrorListener, OnClickListener, OnPageChangeListener{

	private ImgBowseInfo imgBowse;// 图集数据
	private List<ImgInfo> imgInfos = new ArrayList<ImgInfo>();
	public LinearLayout detailLayout;
	private ViewPager viewPager;
	private TextView titletv;
	private TextView pagetv;
	private TextView detailtv;
	private DisplayImageOptions options;
	private ImageLoader imageLoader;
	private ImgAdapter imgAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Util.setColorTranslucent(this, "#000000");
		setContentView(R.layout.pic_show_list_layout);
		Bundle bundle = getIntent().getExtras();
		String URL = bundle.getString("url");
		
		detailLayout = (LinearLayout) this.findViewById(R.id.detail_layout);
		viewPager = (ViewPager) this.findViewById(R.id.pic_list_pager);
		titletv = (TextView) this.findViewById(R.id.pic_title_tv);
		pagetv = (TextView) this.findViewById(R.id.pic_page_tv);
		detailtv = (TextView) this.findViewById(R.id.pic_detail_tv);
		
		options = new DisplayImageOptions.Builder()
		.showStubImage(R.drawable.pic_empty)			
		.showImageForEmptyUri(R.drawable.pic_empty)	
		.showImageOnFail(R.drawable.pic_empty)		
		.cacheInMemory(true)						
		.cacheOnDisc(true)							
//		.displayer(new RoundedBitmapDisplayer(20))	
		.build();
		imageLoader = ImageLoader.getInstance();
		
		viewPager.setOnPageChangeListener(this);
		imgAdapter = new ImgAdapter();
		viewPager.setAdapter(imgAdapter);
		requestJSON(URL);
	}
	
	private void requestJSON(String URL){
		JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,URL,null,this,this);
		jsonObjectRequest.setTag("NewsListFragment");
		VolleyTool.getInstance(this).getmRequestQueue().add(jsonObjectRequest);
	}

	@Override
	public void onErrorResponse(VolleyError arg0) {
		
	}

	@Override
	public void onResponse(JSONObject arg0) {
		try {
			imgBowse = new Gson().fromJson(arg0.toString(), ImgBowseInfo.class);
			imgInfos = imgBowse.imagelist;
			pagetv.setText("1/" + imgInfos.size());
			detailtv.setText(imgInfos.get(0).getDesc());
			imgAdapter.notifyDataSetChanged();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private class ImgAdapter extends PagerAdapter {
		public ImgAdapter() {}

		@Override
		public int getCount() {
			return imgInfos == null ? 0 : imgInfos.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View v = LayoutInflater.from(PicBrowserActivity.this).inflate(R.layout.pic_browse_item_layout, null);
			ProgressBar progressBar = (ProgressBar) v.findViewById(R.id.load_bar);
			GestureImageView imageView = (GestureImageView) v.findViewById(R.id.load_img);
			imageLoader.displayImage(Util.getEastDayURL(imgInfos.get(position).getSrc()), imageView);
			imageView.setOnClickListener(PicBrowserActivity.this);
			v.setTag(imageView);
			container.addView(v);
			return v;
		}
	}

	@Override
	public void onClick(View v) {
		if (detailLayout.getVisibility() == View.VISIBLE){
			detailLayout.setVisibility(View.GONE);
		}else{
			detailLayout.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {}

	@Override
	public void onPageSelected(int arg0) {
		pagetv.setText((arg0 + 1) + "/" + imgInfos.size());
		detailtv.setText(imgInfos.get(arg0).getDesc());
		int count = viewPager.getChildCount();
		for (int i = 0; i < count; i++) {
			View view = viewPager.getChildAt(i);
			GestureImageView iv = (GestureImageView) view.getTag();
			iv.reset();
		}
	}
}

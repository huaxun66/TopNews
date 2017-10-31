package com.huaxun.news.fragment;

import java.util.ArrayList;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.huaxun.R;
import com.huaxun.app.AppApplication;
import com.huaxun.db.DataDB;
import com.huaxun.news.activity.WebActivity;
import com.huaxun.news.bean.News;
import com.huaxun.news.bean.Special;
import com.huaxun.news.bean.SpecialData;
import com.huaxun.tool.BaseTools;
import com.huaxun.tool.CharsetJsonRequest;
import com.huaxun.tool.Options;
import com.huaxun.tool.VolleyTool;
import com.huaxun.utils.Util;
import com.nostra13.universalimageloader.core.ImageLoader;

public class SpecialFragment extends BaseFragment {
	private Context context;
	private ArrayList<Special> specialList = new ArrayList<Special>();
	private ListView mListView;
	private SpecialAdapter specialAdapter;
	private DataDB dataDB;
	private String refreshURL = "";
	private String nodename = "";
	private ImageView detail_loading;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		this.context = this.getActivity();
		dataDB = DataDB.getInstance(context);
		Bundle args = getArguments();
		refreshURL = args != null ? args.getString("refreshURL") : "";
		nodename = args != null ? args.getString("nodeName") : "";
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	private boolean isVisible;
    Handler handler = new Handler();
	public void setUserVisibleHint(boolean isVisibleToUser) {
		if (isVisibleToUser) {
			isVisible = true;
			handler.postDelayed(new Runnable(){
				@Override
				public void run() {
					requestJSON(refreshURL);
				}
			}, 200);
	    } else {
	    	isVisible = false;
	    }
		super.setUserVisibleHint(isVisibleToUser);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.special_fragment, null);
		mListView = (ListView) view.findViewById(R.id.special_lv);
		detail_loading = (ImageView) view.findViewById(R.id.detail_loading);
		specialAdapter = new SpecialAdapter();
		mListView.setAdapter(specialAdapter);
		mListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {}
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (isVisible) {
					onScrollShowOrHide(firstVisibleItem, mListView);
				}				
			}
		});
//		if (getUserVisibleHint()) {
//			requestJSON(refreshURL);
//			}
		return view;
	}
	
	private void requestJSON(String URL){
    	BaseTools.showlog("nodename="+nodename);
    	BaseTools.showlog("URL="+refreshURL);
    	CharsetJsonRequest charsetJsonRequest = new CharsetJsonRequest(Request.Method.GET,URL,null,new Response.Listener<JSONObject>() {  
            @Override  
            public void onResponse(JSONObject response) {
//            	BaseTools.showlog(response.toString());
            	specialList.clear();
            	addNewsList(response.toString());
            	dataDB.addToDataCache(nodename, response.toString());
            	}  
        },new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError arg0) {
				if (specialList.size() == 0){
					String jsonStr = dataDB.getFromDataCache(nodename);
					addNewsList(jsonStr);
				}
			}       	
        });  
		VolleyTool.getInstance(context).getmRequestQueue().add(charsetJsonRequest);
	}	

	private void addNewsList(String json) {
		SpecialData specialData = getDataByJson(json);
    	if (specialData == null){
    		return;
    	}		
		if (specialData.speciallist != null && specialData.speciallist.size() > 0) {
			specialList.addAll(specialData.speciallist);
	     }
	    detail_loading.setVisibility(View.GONE);
	    specialAdapter.notifyDataSetChanged();
	}
	
	private SpecialData getDataByJson(String json) {
		SpecialData data = null;
		try {
			Gson g = new Gson();
			data = g.fromJson(json, SpecialData.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}
	
	private class SpecialAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return specialList.size();
		}

		@Override
		public Object getItem(int position) {
			return specialList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup arg2) {
			Holder holder = null;
			if (convertView == null) {
				holder = new Holder();
				convertView = LayoutInflater.from(getActivity()).inflate(
						R.layout.news_special_list_item, null);
				holder.specialPicIV = (ImageView) convertView.findViewById(R.id.special_pic_iv);
				holder.titleTV = (TextView) convertView.findViewById(R.id.title_tv);
				convertView.setTag(holder);
			} else {
				holder = (Holder) convertView.getTag();
			}
			setSize(holder.specialPicIV);
			Special special = specialList.get(position);
			
			ImageLoader.getInstance().displayImage(Util.getEastDayURL(special.imgurl), holder.specialPicIV,
					Options.getListOptions());
			holder.titleTV.setText(special.classname);
			holder.special = special;
			
			convertView.setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Object obj = v.getTag();
					Holder holder = (Holder) obj;
					Special special = holder.special;
					News news = new News();
					news.setNewsurl(special.classurl);
					news.parentNodeName = nodename;	
					Intent intent = new Intent(context,WebActivity.class);
					Bundle bundle = new Bundle();
					bundle.putSerializable("NEWS", news);
					intent.putExtras(bundle);
					context.startActivity(intent);
					((Activity) context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				}
				
			});

			return convertView;
		}

	}
	
	private static class Holder {
		Special special;
		ImageView specialPicIV;
		TextView titleTV;
	}
	
	private void setSize(ImageView img) {
		LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) img.getLayoutParams();
		float vHeight = 320.0f / 640.0f * AppApplication.mWidth;
		if (layoutParams == null) {
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) vHeight);
			img.setLayoutParams(layoutParams);
		} else {
			layoutParams.height = (int) vHeight;
			img.setLayoutParams(layoutParams);
		}
	}

}

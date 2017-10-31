
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
import android.widget.ListView;
import android.widget.RelativeLayout;
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
import com.huaxun.news.bean.ActivityData;
import com.huaxun.news.bean.News;
import com.huaxun.tool.BaseTools;
import com.huaxun.tool.CharsetJsonRequest;
import com.huaxun.tool.Options;
import com.huaxun.tool.VolleyTool;
import com.huaxun.utils.Util;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ActivityFragment extends BaseFragment {
private Context context;
private ArrayList<News> activitylist = new ArrayList<News>();
private ListView mListView;
private ActivityAdapter activityAdapter;
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
	// TODO Auto-generated method stub
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
	View view = LayoutInflater.from(getActivity()).inflate(R.layout.activity_fragment, null);
	mListView = (ListView) view.findViewById(R.id.activity_listview);
	detail_loading = (ImageView) view.findViewById(R.id.detail_loading);
	activityAdapter = new ActivityAdapter();
	mListView.setAdapter(activityAdapter);
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
//	if (getUserVisibleHint()) {
//		requestJSON(refreshURL);
//		}
	return view;
}

private void requestJSON(String URL){
	BaseTools.showlog("nodename="+nodename);
	BaseTools.showlog("URL="+URL);
	CharsetJsonRequest charsetJsonRequest = new CharsetJsonRequest(Request.Method.GET,URL,null,new Response.Listener<JSONObject>() {  
        @Override  
        public void onResponse(JSONObject response) {
        	activitylist.clear();
        	addNewsList(response.toString());
        	dataDB.addToDataCache(nodename, response.toString());
        	}  
    },new Response.ErrorListener() {
		@Override
		public void onErrorResponse(VolleyError arg0) {
			if (activitylist.size() == 0){
				String jsonStr = dataDB.getFromDataCache(nodename);
				addNewsList(jsonStr);
			}
		}       	
    });  
	VolleyTool.getInstance(context).getmRequestQueue().add(charsetJsonRequest);
}	

private void addNewsList(String json) {
	ActivityData activityData = getDataByJson(json);
	if (activityData == null){
		return;
	}		
	if (activityData.activitylist != null && activityData.activitylist.size() > 0) {
		activitylist.addAll(activityData.activitylist);
     }
    detail_loading.setVisibility(View.GONE);
    activityAdapter.notifyDataSetChanged();
}

private ActivityData getDataByJson(String json) {
	ActivityData data = null;
	try {
		Gson g = new Gson();
		data = g.fromJson(json, ActivityData.class);
	} catch (Exception e) {
		e.printStackTrace();
	}
	return data;
}  

private class ActivityAdapter extends BaseAdapter {

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return activitylist.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return activitylist.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		// TODO Auto-generated method stub
		Holder holder = null;
		if (convertView == null) {
			holder = new Holder();
			convertView = LayoutInflater.from(getActivity()).inflate(
					R.layout.active_item_layout, null);
			holder.activityPicIV = (ImageView) convertView.findViewById(R.id.activity_pic_iv);
			holder.titleTV = (TextView) convertView.findViewById(R.id.title_tv);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		setSize(holder.activityPicIV);
		News news = activitylist.get(position);
		
		ImageLoader.getInstance().displayImage(Util.getEastDayURL(news.getImgurl1()), holder.activityPicIV,
				Options.getListOptions());
		holder.titleTV.setText(news.getNewstitle());
		holder.news = news;
		
		convertView.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Object obj = v.getTag();
				Holder holder = (Holder) obj;
				News news = holder.news;
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
		News news;
		ImageView activityPicIV;
		TextView titleTV;
	}

private void setSize(ImageView img) {
	RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) img.getLayoutParams();
	float vHeight = 310.0f / 590.0f * AppApplication.mWidth;
	if (layoutParams == null) {
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, (int) vHeight);
		img.setLayoutParams(layoutParams);
	} else {
		layoutParams.height = (int) vHeight;
		img.setLayoutParams(layoutParams);
	}
 }

}
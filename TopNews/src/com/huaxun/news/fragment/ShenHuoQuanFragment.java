
package com.huaxun.news.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.huaxun.R;
import com.huaxun.app.AppApplication;
import com.huaxun.db.DataDB;
import com.huaxun.news.activity.ShenHuoQuanListActivity;
import com.huaxun.news.activity.WebActivity;
import com.huaxun.news.bean.News;
import com.huaxun.news.bean.Node;
import com.huaxun.news.bean.ShenHuoQuanData;
import com.huaxun.tool.BaseTools;
import com.huaxun.tool.CharsetJsonRequest;
import com.huaxun.tool.Options;
import com.huaxun.tool.VolleyTool;
import com.huaxun.utils.Util;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ShenHuoQuanFragment extends Fragment {
private Context context;
private DataDB dataDB;
private String refreshURL = "";
private String nodename = "";
private LinearLayout mContainer;

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

protected boolean isVisible;
Handler handler = new Handler();
// 防止预加载数据
public void setUserVisibleHint(boolean isVisibleToUser) {
	super.setUserVisibleHint(isVisibleToUser);
	if (getUserVisibleHint()) {
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
}

@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container,
		Bundle savedInstanceState) {
	// TODO Auto-generated method stub
	View view = LayoutInflater.from(context).inflate(R.layout.shenhuoquan_fragment, null);
	mContainer = (LinearLayout) view.findViewById(R.id.shq_ll);
	return view;
}

private void requestJSON(String URL){
	BaseTools.showlog("nodename="+nodename);
	BaseTools.showlog("URL="+URL);
	CharsetJsonRequest charsetJsonRequest = new CharsetJsonRequest(Request.Method.GET,URL,null,new Response.Listener<JSONObject>() {  
        @Override  
        public void onResponse(JSONObject response) {
    		if (!isVisible){
    			return;
    		}
        	dataDB.addToDataCache(nodename, response.toString());
        	ShenHuoQuanData data = getDataByJson(response.toString());
    		mContainer.removeAllViews();
    		addBigImage(data);
        	}  
    },new Response.ErrorListener() {
		@Override
		public void onErrorResponse(VolleyError arg0) {
			// TODO Auto-generated method stub		
    		if (!isVisible){
    			return;
    		}
			String jsonStr = dataDB.getFromDataCache(nodename);
			ShenHuoQuanData data = getDataByJson(jsonStr);
			mContainer.removeAllViews();
			addBigImage(data);
		}       	
    });  
	VolleyTool.getInstance(context).getmRequestQueue().add(charsetJsonRequest);
}	

private ShenHuoQuanData getDataByJson(String json) {
	ShenHuoQuanData data = null;
	try {
		Gson g = new Gson();
		data = g.fromJson(json, ShenHuoQuanData.class);
	} catch (Exception e) {
		e.printStackTrace();
	}
	return data;
}  

public void addBigImage(ShenHuoQuanData data) {
	if (null == data) {
		return;
	}
	LinearLayout view = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.shenhuoquan_layout, null);
	// 顶部大图+文字
	ImageView shq_iv = (ImageView) view.findViewById(R.id.shq_iv);
	TextView shq_tv = (TextView) view.findViewById(R.id.shq_tv);
	// 设置图片高度为宽度的一半
	LayoutParams iv_params = shq_iv.getLayoutParams();
	iv_params.width = AppApplication.mWidth;
	iv_params.height = AppApplication.mWidth / 2;
	shq_iv.setLayoutParams(iv_params);
	// 加载图片
	ImageLoader.getInstance().displayImage(Util.getEastDayURL(data.bannerimgurl), shq_iv,
			Options.getListOptions());
	shq_tv.setText(data.newstitle + "");
	shq_iv.setTag(data);
	shq_iv.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			ShenHuoQuanData data = (ShenHuoQuanData) v.getTag();
			News news = new News();
			news.setNewsurl(data.bannernewurl);
			news.parentNodeName = nodename;	
			Intent intent = new Intent(context,WebActivity.class);
			Bundle bundle = new Bundle();
			bundle.putSerializable("NEWS", news);
			intent.putExtras(bundle);
			context.startActivity(intent);
			((Activity) context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		}
	});
	// 热点新闻
	ArrayList<Node> nodes = data.lifenews;
	// 时间排序
	ArrayList<String> timeList = new ArrayList<String>();
	if (null != nodes && nodes.size() > 0) {
		for (Node node : nodes) {
			if (null != node) {
				timeList.add(node.newtime);
			}
		}
	}
	Collections.sort(timeList, new Comparator<String>() {
		@Override
		public int compare(String lhs, String rhs) {
			return rhs.compareTo(lhs);
		}
	});
	// 热点新闻的LinearLayout
	LinearLayout shq_ll_tvs = (LinearLayout) view.findViewById(R.id.shq_ll_tvs);
	LayoutParams ll_params = shq_ll_tvs.getLayoutParams();
	ll_params.height = LayoutParams.MATCH_PARENT;
	shq_ll_tvs.setLayoutParams(ll_params);
	shq_ll_tvs.setBackgroundResource(R.drawable.bg_shq);
	shq_ll_tvs.setPadding(0,Util.dip2px(context, 20), 0, Util.dip2px(context, 80));
	// 添加TextView
	for (Node node : nodes) {
		TextView tv = new TextView(context);
		tv.setSingleLine();
		tv.setEllipsize(TextUtils.TruncateAt.END);
		tv.setGravity(Gravity.CENTER_VERTICAL);
		tv.setText(node.nodename);
		tv.setTag(node);

		// 设置TextView的样式
		int index = timeList.indexOf(node.newtime.toString().trim());
		TempBean tempBean = setStyle(index);
		LinearLayout.LayoutParams tv_param = new LinearLayout.LayoutParams((int) (AppApplication.mWidth * tempBean.widthF),0);
		tv_param.setMargins((int) (AppApplication.mWidth * tempBean.marginF), 0, 0, 0);
		tv_param.weight = 1;
		tv.setLayoutParams(tv_param);
		tv.setTextColor(tempBean.color);
		tv.setTextSize(tempBean.size);
		// 设置点击事件
		tv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
		     	Node node = (Node) v.getTag();
		     	Intent intent = new Intent(context, ShenHuoQuanListActivity.class);
		     	Bundle bundle = new Bundle();
		     	bundle.putString("refreshURL", Util.getEastDayURL(node.nodeurl));
		     	bundle.putString("loadMoreURL", Util.getEastDayURL(node.nodeurl));
		     	bundle.putString("nodeName", node.nodename);
		     	intent.putExtras(bundle);
				context.startActivity(intent);
				((Activity) context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}
		});
		shq_ll_tvs.addView(tv);
	}
	mContainer.addView(view);
}

public TempBean setStyle(int index) {
	float widthF = 0f;
	float marginF = 0f;
	int color = 0;
	int size = 24;
	switch (index) {
	case 0:
		widthF = 0.8363f;
		marginF = 0.0909f;
		color = 0xff0056A0;
		size -= 0;
		break;
	case 1:
		widthF = 0.6685f;
		marginF = 0.3176f;
		color = 0xff00559E;
		size -= 2;
		break;
	case 2:
		widthF = 0.5336f;
		marginF = 0.1608f;
		color = 0xff0154A0;
		size -= 3;
		break;
	case 3:
		widthF = 0.7434f;
		marginF = 0.2448f;
		color = 0xff00569F;
		size -= 4;
		break;
	case 4:
		widthF = 0.4427f;
		marginF = 0.3846f;
		color = 0xff00559E;
		size -= 0;
		break;
	case 5:
		widthF = 0.6594f;
		marginF = 0.0909f;
		color = 0xff00569F;
		size -= 5;
		break;
	}
	return new TempBean(widthF, marginF, color, size);
}

class TempBean {
	float widthF = 0f;
	float marginF = 0f;
	int color = 0;
	int size = 0;

	public TempBean(float widthF, float marginF, int color, int size) {
		this.widthF = widthF;
		this.marginF = marginF;
		this.color = color;
		this.size = size;
	}
}

}
package com.huaxun.news.fragment;

import java.util.ArrayList;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
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
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.huaxun.R;
import com.huaxun.app.AppApplication;
import com.huaxun.db.DataDB;
import com.huaxun.news.service.PlayerService;
import com.huaxun.news.activity.WebActivity;
import com.huaxun.news.bean.AudioSpecial;
import com.huaxun.news.bean.DoctorData;
import com.huaxun.news.bean.News;
import com.huaxun.tool.BaseTools;
import com.huaxun.tool.CharsetJsonRequest;
import com.huaxun.tool.Options;
import com.huaxun.tool.VolleyTool;
import com.huaxun.utils.Util;
import com.nostra13.universalimageloader.core.ImageLoader;

public class DoctorFragment extends BaseFragment {
	private Context context;
	private ArrayList<AudioSpecial> audiospecialList = new ArrayList<AudioSpecial>();
	private PullToRefreshListView mPullToRefreshListView;
	private DoctorAdapter doctorAdapter;
	private DataDB dataDB;
	private String refreshURL = "";
	private String nodename = "";
	private String loadMoreURL = "";
	private ImageView detail_loading;
	private int pageIndex = 0;
	private boolean isRefresh = true;
	private String previousUrl = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		this.context = this.getActivity();
		dataDB = DataDB.getInstance(context);
		Bundle args = getArguments();
		refreshURL = args != null ? args.getString("refreshURL") : "";
		loadMoreURL = args != null ? args.getString("loadMoreURL") : "";
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
		View view = LayoutInflater.from(getActivity()).inflate(
				R.layout.doctor_fragment, null);
		mPullToRefreshListView = (PullToRefreshListView) view.findViewById(R.id.pull_to_refresh_listview);
		detail_loading = (ImageView) view.findViewById(R.id.detail_loading);
		doctorAdapter = new DoctorAdapter();
		mPullToRefreshListView.setAdapter(doctorAdapter);
		mPullToRefreshListView.setMode(Mode.BOTH);
		mPullToRefreshListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {}
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (isVisible) {
					onScrollShowOrHide(firstVisibleItem, mPullToRefreshListView.getRefreshableView());
				}				
			}
		});
		mPullToRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
	  	      public void onRefresh(PullToRefreshBase<ListView> refreshView) {
	    	          String str = DateUtils.formatDateTime(context, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
	    	          // 下拉刷新 业务代码
	    	          if (refreshView.isHeaderShown()) {
	    	        	  isRefresh = true;
	    	        	  mPullToRefreshListView.getLoadingLayoutProxy().setRefreshingLabel("正在刷新");
	    	        	  mPullToRefreshListView.getLoadingLayoutProxy().setPullLabel("下拉刷新");
	    	        	  mPullToRefreshListView.getLoadingLayoutProxy().setReleaseLabel("释放开始刷新");
	    	        	  refreshView.getLoadingLayoutProxy().setLastUpdatedLabel("最后更新时间:" + str);
	    	        	  requestJSON(refreshURL);
	    	         } 
	    	          
	    	         // 上拉加载更多 业务代码
	    	         if(refreshView.isFooterShown()) {
	    	        	 isRefresh = false;
	    	        	 mPullToRefreshListView.getLoadingLayoutProxy().setRefreshingLabel("正在加载");
	    	        	 mPullToRefreshListView.getLoadingLayoutProxy().setPullLabel("上拉加载");
	    	        	 mPullToRefreshListView.getLoadingLayoutProxy().setReleaseLabel("释放开始加载");
	    	             refreshView.getLoadingLayoutProxy().setLastUpdatedLabel("最后加载时间:" + str);
	    	             pageIndex++;
	    	             String url = loadMoreURL;
	    	             if(pageIndex > 0){
	    	            	url = url.substring(0, url.lastIndexOf(".")) + "p" + pageIndex + ".html";
	    	             }
	    	             requestJSON(url);
	    	         }
	    	      }
	          });
		return view;
	}

	private void requestJSON(String URL) {
		BaseTools.showlog("nodename=" + nodename);
		BaseTools.showlog("URL=" + URL);
		CharsetJsonRequest charsetJsonRequest = new CharsetJsonRequest(Request.Method.GET, URL, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
		        		if (!isVisible){
		        			return;
		        		}
		        		if (isRefresh==true) {
		        			pageIndex = 0;
		        			audiospecialList.clear();
		                	addNewsList(response.toString());
		                	dataDB.addToDataCache(nodename, response.toString());		
		        		} else {
		        			addNewsList(response.toString());
		        		}
		      			detail_loading.setVisibility(View.GONE);
		      			doctorAdapter.notifyDataSetChanged();
		    		    stopRefresh();
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						if (!isVisible){
							return;
						}
						if (audiospecialList.size() == 0) {
							 String jsonStr = dataDB.getFromDataCache(nodename);
							 addNewsList(jsonStr);
						}
		      			detail_loading.setVisibility(View.GONE);
		      			doctorAdapter.notifyDataSetChanged();
		    		    stopRefresh();
		    			if (!isRefresh){
		    				pageIndex--;
		    			}
					}
				});
		VolleyTool.getInstance(context).getmRequestQueue().add(charsetJsonRequest);
	}
	
	private void stopRefresh() {
		mPullToRefreshListView.postDelayed(new Runnable() {
			@Override
			public void run() {
				mPullToRefreshListView.onRefreshComplete();
			}
		}, 2000);
	}

	private void addNewsList(String json) {
		DoctorData doctorData = getDataByJson(json);
		
		if (doctorData == null) {
			return;
		}
		if (doctorData.audiospeciallist != null && doctorData.audiospeciallist.size() > 0) {			
			audiospecialList.addAll(doctorData.audiospeciallist);
		}
	}

	private DoctorData getDataByJson(String json) {
		DoctorData data = null;
		try {
			Gson g = new Gson();
			data = g.fromJson(json, DoctorData.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}

	private class DoctorAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return audiospecialList.size();
		}

		@Override
		public Object getItem(int position) {
			return audiospecialList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup arg2) {
			Holder holder = new Holder();
			convertView = LayoutInflater.from(getActivity()).inflate(R.layout.news_audio_specia_item, null);
			holder.doctorPicIV = (ImageView) convertView.findViewById(R.id.audio_specia_title_img);
			holder.titleTV = (TextView) convertView.findViewById(R.id.audio_specia_title_tv);
			holder.audioLayout = (LinearLayout) convertView.findViewById(R.id.audio_layout);
			
			holder.doctorPicIV.setTag(holder);
			setSize(holder.doctorPicIV);
			
			AudioSpecial audiospecial = audiospecialList.get(position);

			ImageLoader.getInstance().displayImage(Util.getEastDayURL(audiospecial.imgurl),
					holder.doctorPicIV, Options.getListOptions());
			holder.titleTV.setText(audiospecial.audiospecialname);
			holder.audiospecial = audiospecial;

			holder.doctorPicIV.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Object obj = v.getTag();
					Holder holder = (Holder) obj;
					AudioSpecial audiospecial = holder.audiospecial;
					News news = new News();
					news.setNewsurl(audiospecial.detailurl);
					news.parentNodeName = nodename;					
					Intent intent = new Intent(context, WebActivity.class);
					Bundle bundle = new Bundle();
					bundle.putSerializable("NEWS", news);
					intent.putExtras(bundle);
					context.startActivity(intent);
					((Activity) context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				}

			});
			
			final ArrayList<News> mp3list = audiospecial.mp3list;
			for(int i = 0; i < mp3list.size(); i++) {
//				BaseTools.showlog("newstitle=" + mp3list.get(i).newstitle);
//				BaseTools.showlog("mp3url=" + mp3list.get(i).mp3url);
				final News mp3news = mp3list.get(i);
				View audioView = View.inflate(getActivity(), R.layout.news_audio_specia_mp3_layout, null);
				final ImageView audioIcon = (ImageView) audioView.findViewById(R.id.audio_icon);
				TextView mp3Title = (TextView) audioView.findViewById(R.id.audio_title_tv);
				mp3Title.setText(mp3news.getNewstitle());
				holder.audioLayout.addView(audioView);
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.MATCH_PARENT, 2);
				ImageView line = new ImageView(getActivity());
				line.setBackgroundColor(0xffbdbdbd);
				line.setLayoutParams(layoutParams);
				holder.audioLayout.addView(line);
				audioView.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						String audioUrl = Util.getEastDayAudioURL(mp3news.mp3url);
						if (!audioUrl.equals(previousUrl)) {
							//这里有一个问题，播放新的音频后之前的音频按钮不会自动复位
						    previousUrl = audioUrl;
							playAudio(PlayerService.PLAY, audioUrl);
							audioIcon.setImageResource(R.drawable.audio_stop);
						} else {
							if (PlayerService.mMediaPlayer.isPlaying()) {
								playAudio(PlayerService.PAUSE, audioUrl);
								audioIcon.setImageResource(R.drawable.audio_icon);
							} else {
								playAudio(PlayerService.REPLAY, audioUrl);
								audioIcon.setImageResource(R.drawable.audio_stop);
							}
						}	
					}
				});
			}

			return convertView;
		}

		public void playAudio(int action, String url) {
			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putInt("action", action);
			bundle.putString("url", url);
			intent.putExtras(bundle);
			intent.setClass(context, PlayerService.class);
			context.startService(intent);
		}

	}

	private static class Holder {
		AudioSpecial audiospecial;
		ImageView doctorPicIV;
		TextView titleTV;
		LinearLayout audioLayout;
	}

	private void setSize(ImageView img) {
		LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) img.getLayoutParams();
		float vHeight = 0.5f * AppApplication.mWidth;
		if (layoutParams == null) {
			layoutParams = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT, (int) vHeight);
			img.setLayoutParams(layoutParams);
		} else {
			layoutParams.height = (int) vHeight;
			img.setLayoutParams(layoutParams);
		}
	}
	
	public void onDestroy() {
		super.onDestroy();
		Intent intent = new Intent();
		intent.setClass(context, PlayerService.class);
		context.stopService(intent);
	}

}

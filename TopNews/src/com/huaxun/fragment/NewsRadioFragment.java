package com.huaxun.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.huaxun.R;
import com.huaxun.base.BaseFragment;
import com.huaxun.db.DataDB;
import com.huaxun.news.bean.Node;
import com.huaxun.radio.ImageCycleView;
import com.huaxun.radio.ImageCycleView.ImageCycleViewListener;
import com.huaxun.radio.activity.RadioDetailActivity;
import com.huaxun.radio.activity.RadioMoreActivity;
import com.huaxun.radio.adapter.RadioGridAdapter;
import com.huaxun.radio.bean.RadioCover;
import com.huaxun.radio.bean.RadioItem;
import com.huaxun.radio.bean.RadioNewsList;
import com.huaxun.radio.provider.IMediaPlaybackService;
import com.huaxun.radio.provider.MusicUtils;
import com.huaxun.radio.provider.MusicUtils.ServiceToken;
import com.huaxun.tool.CharsetJsonRequest;
import com.huaxun.tool.NewsUrls;
import com.huaxun.tool.Options;
import com.huaxun.tool.VolleyTool;
import com.huaxun.utils.MediaUtil;
import com.huaxun.utils.radioUtil;
import com.huaxun.view.ObservableScrollView;
import com.huaxun.view.ObservableScrollView.ScrollViewListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/***
 * Radio Main Fragment
 */
@SuppressLint("NewApi")
public class NewsRadioFragment extends BaseFragment implements ServiceConnection {
	private Context context;
	private Activity activity;
	private DataDB dataDB;
	private ObservableScrollView radio_scrollview;
	private ImageCycleView mImageCycleView;
	private TextView recommendTextView;
	private TextView titleTextView1, titleTextView2, more1TextView, more2TextView;
	private RadioGridAdapter radioGridAdapter, shanghaiGridAdapter, dajiaGridAdapter;
	private GridView recommendGridView;
	private GridView shanghaiGridView;
	private GridView dajiaGridView;
	private LinearLayout shanghaiLinearLayout;
	private LinearLayout dajiaLinearLayout;
	private ArrayList<RadioItem> Radiolist1 = new ArrayList<RadioItem>();
	private ArrayList<RadioItem> Radiolist2 = new ArrayList<RadioItem>();
	private Node node;
	private View mView;
	private RadioCover data;
	private String nodeUrl = NewsUrls.NEWS_RADIO;
	private String nodename = "电 台";

	private final static int updateRadioList = 0;
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case updateRadioList:
				shanghaiGridAdapter.setList(Radiolist1);
				dajiaGridAdapter.setList(Radiolist2);
				shanghaiGridAdapter.notifyDataSetChanged();
				dajiaGridAdapter.notifyDataSetChanged();
				break;
			}
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.context = this.getActivity();
		this.activity = this.getActivity();
		dataDB = DataDB.getInstance(context);
		mToken = MusicUtils.bindToService(activity, this);
		
		modifyOrderReceiver = new ModifyOrderReceiver();
		IntentFilter mFilter = new IntentFilter("ReOrder");
		activity.registerReceiver(modifyOrderReceiver, mFilter);
		
		mControlRadioPlayReceiver = new ControlRadioPlayReceiver();
		IntentFilter mControlFilter = new IntentFilter("RADIO_CONTROL");
		activity.registerReceiver(mControlRadioPlayReceiver, mControlFilter);		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.radio_layout, null);
		
		radio_scrollview = (ObservableScrollView) mView.findViewById(R.id.radio_scrollview);
		radio_scrollview.setScrollViewListener(new ScrollViewListener(){
			@Override
			public void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldx, int oldy) {
				if (y > oldy) {
					mainAct.hideBottomPanel();
				} else {			
					mainAct.showBottomPanel();
				}				
			}			
		});
		
		recommendTextView = (TextView) mView.findViewById(R.id.recommendTitleID);
		recommendGridView = (GridView) mView.findViewById(R.id.recommendGridViewID);
		radioGridAdapter = new RadioGridAdapter(context);
		recommendGridView.setAdapter(radioGridAdapter);
		recommendGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				OpenRadioDetail(radioGridAdapter.getItem(position));
				StartRadioPlay(radioGridAdapter.getItem(position).audiourl.get(0).url);
			}
		});

		shanghaiLinearLayout = (LinearLayout) mView.findViewById(R.id.shanghailayoutID);
		shanghaiGridView = (GridView) mView.findViewById(R.id.shanghaiGridViewID);
		shanghaiGridAdapter = new RadioGridAdapter(context);
		shanghaiGridView.setAdapter(shanghaiGridAdapter);
		shanghaiGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				OpenRadioDetail(shanghaiGridAdapter.getItem(position));
				StartRadioPlay(shanghaiGridAdapter.getItem(position).audiourl.get(0).url);
			}
		});

		dajiaLinearLayout = (LinearLayout) mView.findViewById(R.id.dajialayoutID);
		dajiaGridView = (GridView) mView.findViewById(R.id.dajiaGridViewID);
		dajiaGridAdapter = new RadioGridAdapter(context);
		dajiaGridView.setAdapter(dajiaGridAdapter);
		dajiaGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				OpenRadioDetail(dajiaGridAdapter.getItem(position));
				StartRadioPlay(dajiaGridAdapter.getItem(position).audiourl.get(0).url);
			}
		});

		mImageCycleView = (ImageCycleView) mView.findViewById(R.id.iv_cycle_view);
		titleTextView1 = (TextView) mView.findViewById(R.id.title1ID);
		titleTextView2 = (TextView) mView.findViewById(R.id.title2ID);
		more1TextView = (TextView) mView.findViewById(R.id.more1ID);
		more2TextView = (TextView) mView.findViewById(R.id.more2ID);
		more1TextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (data != null) {
					if (data.newslist1 != null) {
						OpenMoreRadioDetail(data.newslist1);
					}
				}
			}
		});

		more2TextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (data != null) {
					if (data.newslist2 != null) {
						OpenMoreRadioDetail(data.newslist2);
					}
				}
			}
		});
		requestJSON(nodeUrl);
		return mView;
	}

	private void OpenRadioDetail(RadioItem radioItem) {
		Intent intent = new Intent(context,RadioDetailActivity.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable("tag_activity", radioItem);
		intent.putExtras(bundle);
		context.startActivity(intent);
		activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}
	
	
	private void StartRadioPlay(String string) {
		Intent detailIntent = new Intent("RADIO_CONTROL");
		detailIntent.putExtra("url", string);
		detailIntent.putExtra("control", "start");
		this.getActivity().sendBroadcast(detailIntent);
	}

	private void OpenMoreRadioDetail(RadioNewsList radioList) {
		Intent intent = new Intent(context,RadioMoreActivity.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable("tag_activity", radioList);
		intent.putExtras(bundle);
		context.startActivity(intent);
		activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		activity.unregisterReceiver(modifyOrderReceiver);
		activity.unregisterReceiver(mControlRadioPlayReceiver);
		MusicUtils.unbindFromService(mToken);
		mService = null;
	}

	private void requestJSON(String URL){	
		//默认UTF-8
    	CharsetJsonRequest charsetJsonRequest = new CharsetJsonRequest(Request.Method.GET,URL,null,new Response.Listener<JSONObject>() {  
            @Override  
            public void onResponse(JSONObject response) {
            	dataDB.addToDataCache(nodename, response.toString());
            	data = getDataByJson(response.toString());
        		data.newslist1.list.addAll(dataDB.selectAllRadioItems(data.newslist1.title));
        		data.newslist2.list.addAll(dataDB.selectAllRadioItems(data.newslist2.title));
        		if (data != null) {
        			addCycleView();
        			addRecommendGridView();
        		}
            }  
        },new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError arg0) {
					String jsonStr = dataDB.getFromDataCache(nodename);
					data = getDataByJson(jsonStr);
	        		if (data != null) {
	        			addCycleView();
	        			addRecommendGridView();
	        		}
			}       	
        });  
		VolleyTool.getInstance(this.getActivity()).getmRequestQueue().add(charsetJsonRequest);
	}

	private void changeRadioOrder() {
		if (data != null && shanghaiGridView != null) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					if (data != null) {
						if (data.newslist1 != null) {
							Radiolist1 = radioUtil.getRadioItemListOrder(context, data.newslist1.title, data.newslist1.list);
							data.newslist1.list = Radiolist1;
						}
					}

					if (data != null) {
						if (data.newslist2 != null) {
							Radiolist2 = radioUtil.getRadioItemListOrder(context, data.newslist2.title, data.newslist2.list);
							data.newslist2.list = Radiolist2;
						}
					}

					handler.sendEmptyMessage(updateRadioList);
				}
			}).start();
		}
	}

	// add CycleView
	private void addCycleView() {
		mImageCycleView.setImageResources(data.bigimagelist, new ImageCycleViewListener() {
			@Override
			public void onImageClick(int position, View imageView) {
				RadioItem radioItem = (RadioItem)imageView.getTag();
				OpenRadioDetail(radioItem);
				StartRadioPlay(radioItem.audiourl.get(0).url);
			}

			@Override
			public void displayImage(String imageURL, ImageView imageView) {
				ImageLoader.getInstance().displayImage(imageURL, imageView, Options.getListOptions(),animateFirstListener);
			}
		});
	}

	// add Recommend View
	private void addRecommendGridView() {
		ArrayList<RadioItem> recommendlist = data.recommendlist;
		radioGridAdapter.setList(recommendlist);
		radioGridAdapter.notifyDataSetChanged();
		if (recommendlist != null){
			if (recommendlist.size() > 0){
				//mainAct.saveDefaultRadioItem(recommendlist.get(0));
			}
		}
		changeRadioOrder();
	}

	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

	private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {
		static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());
		@Override
		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					FadeInBitmapDisplayer.animate(imageView, 2000);
					displayedImages.add(imageUri);
				}
			}
		}
	}

	public RadioCover getDataByJson(String json) {
		RadioCover data = null;
		try {
			Gson g = new Gson();
			data = g.fromJson(json, RadioCover.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}


//	public void switchMode(boolean isNight) {
//		if (null == mView) {
//			return;
//		}
//		recommendTextView.setBackgroundResource(isNight ? R.color.transparent : R.color.today_color_bg_text);
//		mView.setBackgroundResource(isNight ? R.color.night_fmt_background : R.color.fmt_background);
//		shanghaiLinearLayout.setBackgroundResource(isNight ? R.color.transparent : R.color.today_color_bg_text);
//		dajiaLinearLayout.setBackgroundResource(isNight ? R.color.transparent : R.color.today_color_bg_text);
//	}
	
	private ModifyOrderReceiver modifyOrderReceiver;
	class ModifyOrderReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
    			String listName = bundle.getString("listName");
    			ArrayList<RadioItem> radioListFromBroad = (ArrayList<RadioItem>)bundle.getSerializable("list");
    			if (listName.equals(data.newslist1.title)){
    				Radiolist1 = radioListFromBroad;
    				data.newslist1.list = Radiolist1;
    			}else{
    				Radiolist2 = radioListFromBroad;
    				data.newslist2.list = Radiolist2;
    			}
    			handler.sendEmptyMessage(updateRadioList);	
		   }
	   }
	
	    public static IMediaPlaybackService mService = null;
		private ServiceToken mToken;
		private ControlRadioPlayReceiver mControlRadioPlayReceiver;
		private String radioURL = "";
		private int duration=0;
		public static String DutationTime="00:00";
		
		Handler durationHandler = new Handler();
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				duration++;
				DutationTime = MediaUtil.getDurationStr(duration);
				durationHandler.postDelayed(this, 1000);
			}
		};
		
		class ControlRadioPlayReceiver extends BroadcastReceiver {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getStringExtra("control").equals("start")) {
					String urlGet = intent.getStringExtra("url");
					if (urlGet.equals(radioURL)) {
						try {
							if (!mService.isPlaying()) {
								mService.play();
								durationHandler.postDelayed(runnable, 1000);
							}
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						radioURL = urlGet;
						try {
							long[] list = new long[1];
							list[0] = MusicUtils.insert(activity, urlGet);
							mService.open(list, 0);
							durationHandler.removeCallbacks(runnable);
							duration=0;
							DutationTime = "00:00";
							durationHandler.postDelayed(runnable, 1000);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				} else if (intent.getStringExtra("control").equals("playorpause")) {
					try {
						if (mService.isPlaying()) {
							mService.pause();
							durationHandler.removeCallbacks(runnable);
						} else {
							mService.play();
							durationHandler.postDelayed(runnable, 1000);
						}
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = IMediaPlaybackService.Stub.asInterface(service);
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mService = null;
		}
 }

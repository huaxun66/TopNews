package com.huaxun.fragment;

import java.util.ArrayList;

import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.huaxun.R;
import com.huaxun.db.DataDB;
import com.huaxun.dialog.LoadingDialog;
import com.huaxun.news.activity.NodeManagerActivity;
import com.huaxun.news.adapter.NewsFragmentPagerAdapter;
import com.huaxun.news.bean.Node;
import com.huaxun.news.bean.TodayRecommendData;
import com.huaxun.news.fragment.ActivityFragment;
import com.huaxun.news.fragment.DoctorFragment;
import com.huaxun.news.fragment.GaoJianFragment;
import com.huaxun.news.fragment.NewsListFragment;
import com.huaxun.news.fragment.NewsMediaFragment;
import com.huaxun.news.fragment.ShenHuoQuanFragment;
import com.huaxun.news.fragment.SpecialFragment;
import com.huaxun.tool.CharsetJsonRequest;
import com.huaxun.tool.NewsUrls;
import com.huaxun.tool.Settings;
import com.huaxun.tool.VolleyTool;
import com.huaxun.utils.NetUtil;
import com.huaxun.utils.NodeUtil;
import com.huaxun.utils.Util;
import com.huaxun.view.ColumnHorizontalScrollView;
/**
 * （android高仿系列）今日头条 --新闻阅读器  
 * author:HX
 * blog : http://blog.csdn.net/vipzjyno1/
 */
public class NewsFragment extends Fragment {
	/** 自定义HorizontalScrollView */
	private View view;
	private Context context;
	private Activity activity;
	private ColumnHorizontalScrollView mColumnHorizontalScrollView;
	LinearLayout mRadioGroup_content;
	LinearLayout ll_more_columns;
	RelativeLayout rl_column;
	public static LinearLayout ll_channel;
		
	private ViewPager mViewPager;
	private ImageView button_more_columns;
	/** 所有的新闻分类列表*/
	private ArrayList<Node> allNodes=new ArrayList<Node>();
	/** 用户选择的新闻分类列表*/
	private ArrayList<Node> userNodesList=new ArrayList<Node>();
	/** 当前选中的栏目*/
	private int columnSelectIndex = 0;
	/** 左阴影部分*/
	public ImageView shade_left;
	/** 右阴影部分*/
	public ImageView shade_right;
	/** 屏幕宽度 */
	private int mScreenWidth = 0;
	//首页我的频道的所有fragment集合
	private ArrayList<Fragment> fragments = new ArrayList<Fragment>();
	private String todayRecommendURL = NewsUrls.TODAY_RECOMMEND;
	private TodayRecommendData todayRecommendData;
	private DataDB dataDB;
	/** 请求CODE */
	public final static int NODEREQUEST = 10;
	/** 调整返回的RESULTCODE1,用户频道改变 */
	public final static int NODERESULT1 = 11;
	/** 调整返回的RESULTCODE2，用户频道未改变 */
	public final static int NODERESULT2 = 12;
	/**记录上一个选择的频道的位置*/
	private int priviousPosition = 0;
	
	public static boolean isAutoDownload = false;
	private int nodeIndex;
	private static final int MENU_DOWNLOAD_ALL = 00; //注意这里区别于newslistfragment，用00，01
	private Settings settings;
	private boolean containMediaFlag = false;
	private LoadingDialog loadingDialog;
	private MenuInflater inflater;
	
	// 栏目类型
	public static final int NODE_TYPE_GJ = 0;// 稿件类型
	public static final int NODE_TYPE_LIST = 1;// 列表类型
	public static final int NODE_TYPE_ZT = 2;// 专题类型
	public static final int NODE_TYPE_ZT_DETAIL = 3;// 专题详细信息列表类型
	public static final int NODE_TYPE_TJ = 4;// 图集列表类型
	public static final int NODE_TYPE_HD = 5;// 活动列表类型
	public static final int NODE_TYPE_YP = 6;// 音频专题类型
	public static final int NODE_TYPE_SHQ = 7;// 申活圈类型
	public static final int NODE_TYPE_FPZ = 8;// 饭泡粥类型
	public static final int NODE_TYPE_JRTJ = 9;// 今日推荐类型
	public static final int NODE_TYPE_NO_PIC = 10;// 无图版
	public static final int NODE_MEDIA_PLAYER = 11;// 视频播放栏目
	
	public Handler handler = new Handler() {
        public void handleMessage(Message msg) {
        	super.handleMessage(msg);
        	switch (msg.what) {
        	case MENU_DOWNLOAD_ALL:
        		if (nodeIndex < userNodesList.size()) {
        			selectTab(nodeIndex);
    				mViewPager.setCurrentItem(nodeIndex);
        		} else {
        			isAutoDownload = false;
        			selectTab(priviousPosition);
    				mViewPager.setCurrentItem(priviousPosition);
    				Util.showToast(context, "自动下载新闻完毕");
        			loadingDialog.dismiss();
        		}
        		break;
        	}
        }
	};
	
	public void onCreate(Bundle savedInstanceState) {
		this.context = this.getActivity();
		this.activity = this.getActivity();
		setHasOptionsMenu(true);  //在fragment中定义optionmenu
		dataDB = DataDB.getInstance(context);
		settings = new Settings(context, false);
		mScreenWidth = Util.getWindowsWidth(this.getActivity());
		
		nodeDownloadOKReceiver = new NodeDownloadOKReceiver();
		IntentFilter mDownloadOKFilter = new IntentFilter(DownloadOK);
		context.registerReceiver(nodeDownloadOKReceiver, mDownloadOKFilter);
		
		super.onCreate(savedInstanceState);
	}
	
	public void onDestroy() {
		super.onDestroy();
		context.unregisterReceiver(nodeDownloadOKReceiver);
	}

	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		view = LayoutInflater.from(getActivity()).inflate(R.layout.news_fragment_layout, null);
		initView();
		if(DBContainsUserNodes()){
			setChannelView();
		} else {
			requestJSON(todayRecommendURL);
		}
		return view;
	}
	
	private boolean DBContainsUserNodes(){
		ArrayList<Node> templist = dataDB.getUserNodes();
		if(templist.size() != 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/** 初始化layout控件*/
	private void initView() {
		mColumnHorizontalScrollView =  (ColumnHorizontalScrollView) view.findViewById(R.id.mColumnHorizontalScrollView);
		mRadioGroup_content = (LinearLayout) view.findViewById(R.id.mRadioGroup_content);
		ll_more_columns = (LinearLayout) view.findViewById(R.id.ll_more_columns);
		rl_column = (RelativeLayout) view.findViewById(R.id.rl_column);
		button_more_columns = (ImageView) view.findViewById(R.id.button_more_columns);
		mViewPager = (ViewPager) view.findViewById(R.id.mViewPager);
		shade_left = (ImageView) view.findViewById(R.id.shade_left);
		shade_right = (ImageView) view.findViewById(R.id.shade_right);
		ll_channel = (LinearLayout) view.findViewById(R.id.ll_channel);
		
		button_more_columns.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, NodeManagerActivity.class);
				//使用Activity的startActivityForResult，使得MainActivity中OnActivityResult()接收到的request和分发时一致
				//((Activity)context).startActivityForResult(intent, NODEREQUEST);
				startActivityForResult(intent, NODEREQUEST);
				activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}
		});
	}
	
	/** 
	 *  当栏目项发生变化时候调用
	 * */
	private void setChannelView() {
		new Thread(new Runnable(){
			@Override
			public void run() {
				userNodesList = dataDB.getUserNodes();
				NodeUtil.reorderNodeList(context,userNodesList);
				handler.post(new Runnable(){
					@Override
					public void run() {
						initTabColumn();
						initFragment();
					}		
				});
			}			
		}).start();
	}

	/** 
	 *  初始化Column栏目项
	 * */
	private void initTabColumn() {
		mRadioGroup_content.removeAllViews();
		int count =  userNodesList.size();
		mColumnHorizontalScrollView.setParam(this.getActivity(), mScreenWidth, mRadioGroup_content, shade_left, shade_right, ll_more_columns, rl_column);
		for(int i = 0; i< count; i++){
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT , LayoutParams.WRAP_CONTENT);
			params.leftMargin = Util.dip2px(context, 10);
			params.rightMargin = Util.dip2px(context, 10);
//			TextView localTextView = (TextView) mInflater.inflate(R.layout.column_radio_item, null);
			TextView columnTextView = new TextView(this.getActivity());
			columnTextView.setTextAppearance(this.getActivity(), R.style.top_category_scroll_view_item_text);
//			localTextView.setBackground(getResources().getDrawable(R.drawable.top_category_scroll_text_view_bg));
			columnTextView.setBackgroundResource(R.drawable.radio_buttong_bg);
			columnTextView.setGravity(Gravity.CENTER);
			columnTextView.setPadding(Util.dip2px(context, 2), Util.dip2px(context, 2), Util.dip2px(context, 2), Util.dip2px(context, 2));
			columnTextView.setId(i);
			columnTextView.setText(userNodesList.get(i).nodename);
			columnTextView.setTextColor(getResources().getColorStateList(R.color.top_category_scroll_text_color_day));
			if(columnSelectIndex == i){
				columnTextView.setSelected(true);
			}
			columnTextView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
			          for(int i = 0;i < mRadioGroup_content.getChildCount();i++){
				          View localView = mRadioGroup_content.getChildAt(i);
				          if (localView != v)
				        	  localView.setSelected(false);
				          else{
				        	  localView.setSelected(true);
				        	  mViewPager.setCurrentItem(i);
				          }
			          }
			          Util.showToast(context, userNodesList.get(v.getId()).nodename);			       
				}
			});

			mRadioGroup_content.addView(columnTextView, i ,params);
		}
	}
	/** 
	 *  选择的Column里面的Tab
	 * */
	private void selectTab(int tab_postion) {
		columnSelectIndex = tab_postion;
		for (int i = 0; i < mRadioGroup_content.getChildCount(); i++) {
			View checkView = mRadioGroup_content.getChildAt(tab_postion);
			int k = checkView.getMeasuredWidth();
			int l = checkView.getLeft();
			int i2 = l + k / 2 - mScreenWidth / 2;
			mColumnHorizontalScrollView.smoothScrollTo(i2, 0);
		}
		/**是否选中*/
		for (int j = 0; j <  mRadioGroup_content.getChildCount(); j++) {
			View checkView = mRadioGroup_content.getChildAt(j);
			boolean ischeck;
			if (j == tab_postion) {
				ischeck = true;
			} else {
				ischeck = false;
			}
			checkView.setSelected(ischeck);
		}
	}
	/** 
	 *  初始化Fragment
	 * */
	private void initFragment() {
		int count =  userNodesList.size();
		/**每次初始化Fragment之前必须清一下fragments，否则新加的fragment只是添加在fragments后面，导致主界面我的频道不能正确更新*/
		fragments.clear();
		for(int i = 0; i< count;i++){
			Bundle data = new Bundle();
    		data.putString("nodeName", userNodesList.get(i).nodename);
    		/**稿件类型nodeurl以http开头 */   		
            data.putString("refreshURL", Util.getEastDayURL(userNodesList.get(i).nodeurl));
	    	if (userNodesList.get(i).nodename.equals("今日推荐")){
	    		data.putString("loadMoreURL", Util.getEastDayURL(NewsUrls.TODAY_RECOMMEND_LOADMORE));	
	    	} else {
	    		data.putString("loadMoreURL", Util.getEastDayURL(userNodesList.get(i).nodeurl));	
	    	}
			int type = Integer.parseInt(userNodesList.get(i).nodetype);
			switch (type) {
			     /**市场直击、说说吧、查查看 */
			case NODE_TYPE_GJ:
				GaoJianFragment gaoJianFragment = new GaoJianFragment();
				gaoJianFragment.setArguments(data);
				fragments.add(gaoJianFragment);
				break;
				/**国内、上海、军事、财经、娱乐、体育、社会、别人家的、ENGLISH、外媒揽要、评论、北美频道、国际、历史、汽车、日本语、东东腔 */	
			case NODE_TYPE_LIST:
				 /**视界 */
			case NODE_TYPE_TJ:
				 /**饭泡粥 */
			case NODE_TYPE_FPZ:
				 /**今日推荐 */
			case NODE_TYPE_JRTJ:
				 /**时政*/
			case NODE_TYPE_NO_PIC:
				NewsListFragment newsListFragment = new NewsListFragment();
				newsListFragment.setArguments(data);
				fragments.add(newsListFragment);
				break;
				/**专题 */
			case NODE_TYPE_ZT:
				SpecialFragment specialFragment = new SpecialFragment();
				specialFragment.setArguments(data);
				fragments.add(specialFragment);
				break;
			case NODE_TYPE_ZT_DETAIL:
				break;
				/**活动 */
			case NODE_TYPE_HD:
				ActivityFragment activiyFragment = new ActivityFragment();
				activiyFragment.setArguments(data);
				fragments.add(activiyFragment);	
				break;
				/**名医坐堂 */
			case NODE_TYPE_YP:
				DoctorFragment doctorFragment = new DoctorFragment();
				doctorFragment.setArguments(data);
				fragments.add(doctorFragment);
				break;
				/**申活圈 */
			case NODE_TYPE_SHQ:
				ShenHuoQuanFragment shenHuoQuanFragment = new ShenHuoQuanFragment();
				shenHuoQuanFragment.setArguments(data);
				fragments.add(shenHuoQuanFragment);	
				break;
				/**视频 */
			case NODE_MEDIA_PLAYER:
				NewsMediaFragment newsMediaFragment = new NewsMediaFragment();
				newsMediaFragment.setArguments(data);
				fragments.add(newsMediaFragment);
				break;
			default:
				break;
			}
		}
		/**注意，fragment嵌套使用时需要用getChildFragmentManager,用getSupportFragmentManager会出现子fragment显示空白的问题 */
		NewsFragmentPagerAdapter mAdapetr = new NewsFragmentPagerAdapter(getChildFragmentManager(), fragments);
		//mViewPager.setOffscreenPageLimit(0); //设置预加载，v4包，默认是1
		mViewPager.setAdapter(mAdapetr);
		mViewPager.setOnPageChangeListener(pageListener);
	}
	/** 
	 *  ViewPager切换监听方法
	 * */
	public OnPageChangeListener pageListener= new OnPageChangeListener(){

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageSelected(int position) {
			// TODO Auto-generated method stub
			mViewPager.setCurrentItem(position);
			selectTab(position);
			priviousPosition = position;
		}
	};
	
	private void requestJSON(String URL){
		CharsetJsonRequest charsetJsonRequest = new CharsetJsonRequest(Request.Method.GET,URL,null,new Response.Listener<JSONObject>() {  
            @Override
            public void onResponse(JSONObject response) {
        		todayRecommendData  = getDataByJson(response.toString());
        		allNodes = todayRecommendData.allnodes; 			
        		dataDB.InitialAllNodes(allNodes);
        		setChannelView();
            }  
        },new Response.ErrorListener() { 
            public void onErrorResponse(VolleyError error) {
            	Toast.makeText(context,"加载数据失败！", Toast.LENGTH_SHORT).show();
            }  
        });  
		VolleyTool.getInstance(this.getActivity()).getmRequestQueue().add(charsetJsonRequest);
	}
	
	private TodayRecommendData getDataByJson(String json) {
		TodayRecommendData data = null;
		try {
			Gson g = new Gson();
			data = g.fromJson(json, TodayRecommendData.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case NODEREQUEST:
			if(resultCode == NODERESULT1){
				setChannelView();
				/**从频道管理回退后选择到之前的位置，否则之前页面不刷新，一直是今日推荐*/
				mViewPager.setCurrentItem(priviousPosition);
				selectTab(priviousPosition);
			}
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		return;
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.add(0, MENU_DOWNLOAD_ALL, 0, "自动下载所有新闻");
		return;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_DOWNLOAD_ALL:	
				Util.showToast(context, "自动下载所有新闻");
				if (settings.getAutodownloadNewsMode().equals(Settings.AUTODOWNLOAD_CONTAIN_MEDIA)) {
					containMediaFlag = true;
				} else {
					containMediaFlag = false;
				}				
				if (settings.getDownloadOnlyWifi() && !NetUtil.isWIFIOn(context)) {
					Util.showToast(context, "仅WIFI下下载所有新闻");
				    break;
				}
				if (!NetUtil.isNetworkAvailable(context)) {
					Util.showToast(context, "请开启网络");
				    break;
				}
				if (isAutoDownload == true) {
					Util.showToast(context, "当前正在自动下载新闻");				
				} else {
					selectTab(mViewPager.getChildCount()-1);
					mViewPager.setCurrentItem(mViewPager.getChildCount()-1);
					Util.showToast(context, "开始自动下载新闻");
					nodeIndex = 0;
					isAutoDownload = true;
					handler.sendEmptyMessage(MENU_DOWNLOAD_ALL);
					loadingDialog = new LoadingDialog(context);
					loadingDialog.show();
				}			
				break;	
		}
		return super.onOptionsItemSelected(item);
	}
	
	public static final String DownloadOK = "DOWNLOADOK";
	private NodeDownloadOKReceiver nodeDownloadOKReceiver;
	class NodeDownloadOKReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {			
			String nodeName = intent.getStringExtra("nodeName");
			Util.showToast(context, "\""+nodeName+"\""+"栏目下载完毕");
			nodeIndex++;			
			if (nodeIndex < userNodesList.size() && userNodesList.get(nodeIndex).nodename.equals("视　频") && containMediaFlag == false) {				
				nodeIndex++;		
			}
			handler.sendEmptyMessage(MENU_DOWNLOAD_ALL);
		   }
	   }

}

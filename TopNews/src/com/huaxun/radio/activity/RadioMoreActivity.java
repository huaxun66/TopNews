package com.huaxun.radio.activity;

import java.util.ArrayList;
import java.util.Collections;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.huaxun.R;
import com.huaxun.app.AppApplication;
import com.huaxun.base.BaseActivity;
import com.huaxun.radio.DragGridView;
import com.huaxun.radio.adapter.DragGridBaseAdapter;
import com.huaxun.radio.bean.RadioItem;
import com.huaxun.radio.bean.RadioNewsList;
import com.huaxun.tool.BaseTools;
import com.huaxun.tool.Constants;
import com.huaxun.tool.Options;
import com.huaxun.utils.Util;
import com.huaxun.utils.radioUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

public class RadioMoreActivity extends BaseActivity implements OnClickListener {
	
	private RadioNewsList radioNewsList;
	private ImageButton topBack;
	private TextView topTitle;
	private ImageButton topAdd;
	private DragGridView mDragGridView;
	private ArrayList<RadioItem> radioList = new ArrayList<RadioItem>();
	private DragAdapter dragAdapter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.news_more_detail_layout);
		topBack = (ImageButton) findViewById(R.id.topBack);
		topTitle = (TextView) findViewById(R.id.topTitle);
		topAdd = (ImageButton) findViewById(R.id.topAdd);
		topBack.setOnClickListener(this);
		topAdd.setOnClickListener(this);
		mDragGridView = (DragGridView) findViewById(R.id.dragGridView);
		dragAdapter = new DragAdapter();
		mDragGridView.setAdapter(dragAdapter);
		mDragGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				OpenRadioDetail(dragAdapter.getItem(position));
				StartRadioPlay(dragAdapter.getItem(position).audiourl.get(0).url);
			}
		});
		
		Bundle bundle = getIntent().getExtras();
		Object obj = bundle.getSerializable("tag_activity");
		if (null != obj) {
			if (obj instanceof RadioNewsList) {
				radioNewsList = (RadioNewsList) obj;
				if (radioNewsList.list != null) {
					radioList = radioNewsList.list;
				}
			}
		}
		
		listChangeReceiver = new ListChangeReceiver();
		IntentFilter mFilter = new IntentFilter("ListChanged");
		registerReceiver(listChangeReceiver, mFilter);
		
		mOrderedReceiver = new OrderedReceiver();
		IntentFilter mOrderFilter = new IntentFilter("LIST_RE_ORDERED");
		registerReceiver(mOrderedReceiver, mOrderFilter);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(listChangeReceiver);
		unregisterReceiver(mOrderedReceiver);
	}
	
	private void OpenRadioDetail(RadioItem radioItem) {
		Intent intent = new Intent(this,RadioDetailActivity.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable("tag_activity", radioItem);
		intent.putExtras(bundle);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}
	
	
	private void StartRadioPlay(String string) {
		Intent detailIntent = new Intent("RADIO_CONTROL");
		detailIntent.putExtra("url", string);
		detailIntent.putExtra("control", "start");
		sendBroadcast(detailIntent);
	}
	
	public void onClick(View v) {
	switch (v.getId()) {
	case R.id.topAdd:
		Intent intent = new Intent(this, UserSpaceRadioManagerActivity.class);
		Bundle mBundle = new Bundle();
		mBundle.putString("radioListTitle", radioNewsList.title);
		intent.putExtras(mBundle);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		break;
	case R.id.topBack:
         finish();
         overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
		break;
	default:
		break;
	}
 }
	
	public void onBackPressed() {
		finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}


	public class DragAdapter extends BaseAdapter implements DragGridBaseAdapter {

		@Override
		public int getCount() {
			return radioList.size();
		}

		@Override
		public RadioItem getItem(int position) {
			return radioList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		public ArrayList<RadioItem> getOrderedList() {
			return radioList;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {			
			convertView = LayoutInflater.from(RadioMoreActivity.this).inflate(R.layout.news_more_detail_listitem_layout, null);
			ImageView mImageView = (ImageView) convertView.findViewById(R.id.item_image);
			TextView mTextView = (TextView) convertView.findViewById(R.id.item_text);
			RadioItem radioItem = radioList.get(position);
			mTextView.setText(radioItem.newstitle);			
			String imagePath = radioItem.audioiconimage;
			if (imagePath != null) {
				if (imagePath.startsWith(Constants.RadioFolderPath)){
					ImageLoader.getInstance().displayImage("file://" + imagePath, mImageView,Options.getRoundListOptions(R.drawable.radio_icon_default));
				}else{
					ImageLoader.getInstance().displayImage(Util.getTestImageURL(imagePath), mImageView,Options.getRoundListOptions(R.drawable.radio_icon_default));
				}
			}else{
				ImageLoader.getInstance().displayImage(Util.getTestImageURL(imagePath), mImageView,Options.getRoundListOptions(R.drawable.radio_icon_default));
			}
			return convertView;
		}

		public void reorderItems(int oldPosition, int newPosition) {
			if (oldPosition < 0 && newPosition < 0) {
				return;
			}

			RadioItem radioItem = radioList.get(oldPosition);
			if (oldPosition < newPosition) {
				for (int i = oldPosition; i < newPosition; i++) {
					Collections.swap(radioList, i, i + 1);
				}
			} else if (oldPosition > newPosition) {
				for (int i = oldPosition; i > newPosition; i--) {
					Collections.swap(radioList, i, i - 1);
				}
			}
			radioList.set(newPosition, radioItem);
		}

		public void setHideItem(int hidePosition) {
			notifyDataSetChanged();
		}

		public void removeItem(int removePosition) {
			radioList.remove(removePosition);
			notifyDataSetChanged();
		}
	}
	
	private ListChangeReceiver listChangeReceiver;
	class ListChangeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			String listName = bundle.getString("listName");
			String addOrdelete = bundle.getString("addOrdelete");
			RadioItem radioItem = (RadioItem)bundle.getSerializable("item");
			if (listName.equals(radioNewsList.title)){
				if (addOrdelete.equals("add")){
					radioList.add(0,radioItem);
				}else{
					for (int i = 0; i<radioList.size(); i++){
						if (radioList.get(i).newstitle.equals(radioItem.newstitle)){
							radioList.remove(i);
						}
					}
				}
				dragAdapter.notifyDataSetChanged();
				radioUtil.setRadioListOrder(RadioMoreActivity.this, radioNewsList.title, dragAdapter.getOrderedList());
				//添加或删除电台后发广播通知newsradiofragment更改顺序
				Intent it = new Intent("ReOrder");
				it.putExtra("listName", radioNewsList.title);
				it.putExtra("list",radioList);
				RadioMoreActivity.this.sendBroadcast(it);
			}
		}
	}

	private OrderedReceiver mOrderedReceiver;

	class OrderedReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			radioUtil.setRadioListOrder(context, radioNewsList.title, dragAdapter.getOrderedList());
			//拖拽排序后发广播通知newsradiofragment更改顺序
			Intent it = new Intent("ReOrder");
			it.putExtra("listName", radioNewsList.title);
			it.putExtra("list",radioList);
			RadioMoreActivity.this.sendBroadcast(it);
		}
	}

}

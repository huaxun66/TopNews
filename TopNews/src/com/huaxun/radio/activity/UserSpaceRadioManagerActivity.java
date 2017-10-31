package com.huaxun.radio.activity;

import java.util.ArrayList;
import java.util.Collections;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.huaxun.R;
import com.huaxun.base.BaseActivity;
import com.huaxun.db.DataDB;
import com.huaxun.radio.activity.RadioMoreActivity.ListChangeReceiver;
import com.huaxun.radio.bean.RadioAudioURLDetail;
import com.huaxun.radio.bean.RadioItem;
import com.huaxun.tool.BaseTools;
import com.huaxun.tool.Options;
import com.huaxun.utils.Util;
import com.huaxun.utils.radioUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * @author huaxun
 * @Create 2015-10-28
 * @Module core
 * @Description 用户空间 电台管理
 */
public class UserSpaceRadioManagerActivity extends BaseActivity implements OnClickListener {
	private ImageButton topBack;
	private TextView topTitle;
	private GridView mNewRadioGridView;
	private NewRadioGridAdapter mNewRadioGridAdapter;
	private ArrayList<RadioItem> newRadioList = new ArrayList<RadioItem>();
	private String radioListTitle = "";
	private DataDB dataDB;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dataDB = DataDB.getInstance(this);
		setContentView(R.layout.userspace_radio_manager);
		topBack = (ImageButton) findViewById(R.id.topBack);
		topBack.setOnClickListener(this);
		topTitle = (TextView) findViewById(R.id.topTitle);
		mNewRadioGridView = (GridView) findViewById(R.id.radioGridView);
		mNewRadioGridAdapter = new NewRadioGridAdapter();
		mNewRadioGridView.setAdapter(mNewRadioGridAdapter);
		mNewRadioGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final String icon = mNewRadioGridAdapter.getItem(position).audioiconimage;
				if (icon.equals("add_more_radio_button")) {
					AddNewRadio();
				}
			}
		});
		mNewRadioGridView.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				final String icon = mNewRadioGridAdapter.getItem(arg2).audioiconimage;
				if (!icon.equals("add_more_radio_button")) {
					final RadioItem item = mNewRadioGridAdapter.getItem(arg2);
					AlertDialog.Builder builder = new AlertDialog.Builder(UserSpaceRadioManagerActivity.this);
					builder.setTitle("警告");
					// 设置Content来显示一个信息
					builder.setMessage("确定要删除吗？");
					// 设置一个PositiveButton
					builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dataDB.deleteRadioItem(item, radioListTitle);
							setGridViewData();
							mNewRadioGridAdapter.notifyDataSetChanged();
							Intent intent = new Intent("ListChanged");
							intent.putExtra("listName", radioListTitle);
							intent.putExtra("item", item);
							intent.putExtra("addOrdelete", "delete");
							sendBroadcast(intent);
							Toast.makeText(UserSpaceRadioManagerActivity.this, "删除成功！", Toast.LENGTH_SHORT).show();
						}
					});
					// 设置一个NegativeButton
					builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
					// 显示出该对话框
					builder.show();
				}
				return true;
			}
		});
		
		Bundle bundle = getIntent().getExtras();
		radioListTitle = bundle.getString("radioListTitle");
		
		listChangeReceiver = new ListChangeReceiver();
		IntentFilter mFilter = new IntentFilter("ListChanged");
		registerReceiver(listChangeReceiver, mFilter);
			
		setGridViewData();
		//动态设置GridView的高度
		setGridViewHeight();
	}
	
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(listChangeReceiver);
	}

	// Solve the problem that GridView can not be shown totally in ScrollView
	private void setGridViewHeight() {
		if (mNewRadioGridAdapter == null) {
			return;
		}
		int totalHeight = 0;
		int itemnum = newRadioList.size();
		int linenum = 0;
		if (itemnum % 3 == 0) {
			linenum = itemnum / 3;
		} else {
			linenum = itemnum / 3 + 1;
		}
		View gridItem = mNewRadioGridAdapter.getView(0, null, mNewRadioGridView);
		gridItem.measure(0, 0);
		totalHeight = gridItem.getMeasuredHeight() * linenum;
		ViewGroup.LayoutParams params = mNewRadioGridView.getLayoutParams();
		params.height = totalHeight + (Util.dip2px(this, 15) * linenum);
		mNewRadioGridView.setLayoutParams(params);
	}

	private void setGridViewData() {
		newRadioList = dataDB.selectAllRadioItems(radioListTitle);
		Collections.reverse(newRadioList); //倒序
		ArrayList<RadioAudioURLDetail> audiourl = new ArrayList<RadioAudioURLDetail>();
		RadioItem addRadioButton = new RadioItem("新电台", "", audiourl, "", "add_more_radio_button");
		newRadioList.add(addRadioButton);
		mNewRadioGridAdapter.notifyDataSetChanged();
	}

	private void AddNewRadio() {
		Intent intent = new Intent(this,AddNewRadioActivity.class);
		Bundle mBundle = new Bundle();
		mBundle.putString("radioListTitle", radioListTitle);
		intent.putExtras(mBundle);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}
	
	public void onClick(View v) {
	switch (v.getId()) {
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
	
	private ListChangeReceiver listChangeReceiver;
	class ListChangeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			setGridViewData();
		}
	}
	
	public class NewRadioGridAdapter extends BaseAdapter {
		
		public int getCount() {
			return newRadioList.size();
		}

		@Override
		public RadioItem getItem(int position) {
			return newRadioList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = null;
			ViewHolder holder = null;
			RadioItem radioitem = newRadioList.get(position);
			if (convertView == null) {
				view = View.inflate(UserSpaceRadioManagerActivity.this, R.layout.radio_grid_item, null);
				holder = new ViewHolder();
				holder.image = (ImageView) view.findViewById(R.id.today_grid_iv_icon);
				holder.title = (TextView) view.findViewById(R.id.today_grid_tv_count);
				view.setTag(holder);
			} else {
				view = convertView;
				holder = (ViewHolder) view.getTag();
			}

			holder.title.setText(radioitem.newstitle);
			BaseTools.showlog("pos="+position+" audioiconimage="+radioitem.audioiconimage);
			if (radioitem.audioiconimage.equals("add_more_radio_button")) {
				holder.image.setImageResource(R.drawable.add_more_radio_button);
			} else {
				ImageLoader.getInstance().displayImage("file://" + radioitem.audioiconimage, holder.image,
						Options.getRoundListOptions(R.drawable.radio_icon_default));
			}
			return view;
		}

		public class ViewHolder {
			public ImageView image;
			public TextView title;
		}
	}

}

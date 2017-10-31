package com.huaxun.radio.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.huaxun.R;
import com.huaxun.app.AppApplication;
import com.huaxun.radio.bean.RadioItem;
import com.huaxun.tool.Constants;
import com.huaxun.tool.Options;
import com.huaxun.utils.Util;
import com.nostra13.universalimageloader.core.ImageLoader;

public class RadioGridAdapter extends BaseAdapter
{
	private Context mContext;
	private List<RadioItem> mList = new ArrayList<RadioItem>();

	public RadioGridAdapter(Context mContext) {
		this.mContext = mContext;
	}

	@Override
	public int getCount() {
		if (mList.size() >= 3) {
			return 3;
		} else {
			return mList.size();
		}
	}

	@Override
	public RadioItem getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view = null;
		ViewHolder holder = null;
		RadioItem node = mList.get(position);
		if (convertView == null) {
			view = View.inflate(mContext, R.layout.radio_grid_item, null);
			holder = new ViewHolder();
			holder.image = (ImageView) view.findViewById(R.id.today_grid_iv_icon);
			holder.title = (TextView) view.findViewById(R.id.today_grid_tv_count);
			view.setTag(holder);
		} else {
			view = convertView;
			holder = (ViewHolder) view.getTag();
		}

		holder.title.setText(node.newstitle);
		String imagePath = node.audioiconimage;
		if (imagePath != null) {
			if (imagePath.startsWith(Constants.RadioFolderPath)){
				ImageLoader.getInstance().displayImage("file://" + imagePath, holder.image,Options.getRoundListOptions(R.drawable.radio_icon_default));
			}else{
				ImageLoader.getInstance().displayImage(Util.getTestImageURL(node.audioiconimage), holder.image,Options.getRoundListOptions(R.drawable.radio_icon_default));
			}
		}else{
			ImageLoader.getInstance().displayImage(Util.getTestImageURL(node.audioiconimage), holder.image,Options.getRoundListOptions(R.drawable.radio_icon_default));
		}
		return view;
	}
	

	public static class ViewHolder {
		public ImageView image;
		public TextView title;
	}

	public List<RadioItem> getList() {
		return mList;
	}

	public void setList(List<RadioItem> list) {
		this.mList = list;
	}
}

package com.huaxun.more.adapter;

import java.util.List;

import com.huaxun.R;
import com.huaxun.more.bean.Column;
import com.huaxun.news.bean.Node;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MoreOtherAdapter extends BaseAdapter {
	private Context context;
	public List<Column> columnList;
	private TextView item_text;
	/** 是否可见 */
	boolean isVisible = true;
	/** 要删除的position */
	public int remove_position = -1;

	public MoreOtherAdapter(Context context){
		this.context = context;
	}
	
	public void setColumnList(List<Column> columnList){
		this.columnList = columnList;
	}

	@Override
	public int getCount() {
		return columnList == null ? 0 : columnList.size();
	}

	@Override
	public Column getItem(int position) {
		if (columnList != null && columnList.size() != 0) {
			return columnList.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = LayoutInflater.from(context).inflate(R.layout.channel_item, null);
		item_text = (TextView) view.findViewById(R.id.text_item);
		Column column = getItem(position);
		item_text.setText(column.columnName);
		if (!isVisible && (position == -1 + columnList.size())){
			item_text.setText("");
			item_text.setSelected(true);
			item_text.setEnabled(true);
		}
		if(remove_position == position){
			item_text.setText("");
		}
		return view;
	}
	
	/** 获取频道列表 */
	public List<Column> getColumnList() {
		return columnList;
	}
	
	/** 添加频道列表 */
	public void addItem(Column column) {
		columnList.add(column);
		notifyDataSetChanged();
	}

	/** 设置删除的position */
	public void setRemove(int position) {
		remove_position = position;
		notifyDataSetChanged();
	}

	/** 删除频道列表 */
	public void remove() {
		columnList.remove(remove_position);
		remove_position = -1;
		notifyDataSetChanged();
	}

	/** 获取是否可见 */
	public boolean isVisible() {
		return isVisible;
	}
	
	/** 设置是否可见 */
	public void setVisible(boolean visible) {
		isVisible = visible;
	}
}
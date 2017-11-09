package com.huaxun.more.adapter;

import java.util.ArrayList;
import java.util.List;

import com.huaxun.MainActivity;
import com.huaxun.R;
import com.huaxun.more.bean.Column;
import com.huaxun.news.bean.Node;
import com.huaxun.tool.BaseTools;
import com.huaxun.utils.ColumnUtil;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MoreDragAdapter extends BaseAdapter {
	/** TAG*/
	private final static String TAG = "DragAdapter";
	/** 是否显示底部的ITEM */
	private boolean isItemShow = false;
	private Context context;
	/** 控制的postion */
	private int holdPosition;
	/** 是否改变 */
	private boolean isChanged = false;
	/** 列表数据是否改变 */
	private boolean isListChanged = false;
	/** 是否可见 */
	boolean isVisible = true;
	/** 可以拖动的列表 */
	public ArrayList<Column> columnList;
	/** TextView 频道内容 */
	private TextView item_text;
	/** 要删除的position */
	public int remove_position = -1;

	public MoreDragAdapter(Context context) {
		this.context = context;		
	}
	
	/** 设置频道列表 */
	public void setColumnList(ArrayList<Column> columnList){
		this.columnList = columnList;
	}
	
	/** 获取频道列表 */
	public ArrayList<Column> getColumnList() {
		return columnList;
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
		if (isChanged && (position == holdPosition) && !isItemShow) {
			item_text.setText("");
			item_text.setSelected(true);
			item_text.setEnabled(true);
			isChanged = false;
		}
		//点击更多频道时，我的频道最后一个Item不可见
		if (!isVisible && (position == -1 + columnList.size())) {
			item_text.setText("");
			item_text.setSelected(true);
			item_text.setEnabled(true);
		}
		if(remove_position == position){
			item_text.setText("");
		}
		return view;
	}

	/** 添加频道列表 */
	public void addItem(Column column) {
		columnList.add(column);
		isListChanged = true;
		notifyDataSetChanged();
	}

	/** 拖动变更频道排序 */
	public void exchange(int dragPostion, int dropPostion) {
		holdPosition = dropPostion;
		Column dragItem = getItem(dragPostion);
		BaseTools.showlog("dragPostion=" + dragPostion + ";endPosition=" + dropPostion);
		if (dragPostion < dropPostion) {
			columnList.add(dropPostion + 1, dragItem);
			columnList.remove(dragPostion);
		} else {
			columnList.add(dropPostion, dragItem);
			columnList.remove(dragPostion + 1);
		}
		isChanged = true;
		isListChanged = true;
		notifyDataSetChanged();	
	}
	
	/** 拖动结束，记录，刷新底部 */
	public void exchangeDone() {
		ArrayList<Column> fgColumnList = getColumnList();
		ColumnUtil.setColumnListOrder(context, fgColumnList);
		((MainActivity)context).initBottomPanel(); //刷新底部
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
		isListChanged = true;
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
	
	/** 排序是否发生改变 */
	public boolean isListChanged() {
		return isListChanged;
	}
	
	/** 显示放下的ITEM */
	public void setShowDropItem(boolean show) {
		isItemShow = show;
	}
}
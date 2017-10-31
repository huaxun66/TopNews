package com.huaxun.news.adapter;

import java.util.ArrayList;
import java.util.List;

import com.huaxun.R;
import com.huaxun.news.bean.Node;
import com.huaxun.tool.BaseTools;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DragAdapter extends BaseAdapter {
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
	/** 可以拖动的列表（即用户选择的频道列表） */
	public ArrayList<Node> nodeList;
	/** TextView 频道内容 */
	private TextView item_text;
	/** 要删除的position */
	public int remove_position = -1;

	public DragAdapter(Context context) {
		this.context = context;		
	}
	
	/** 设置频道列表 */
	public void setNodeList(ArrayList<Node> nodeList){
		this.nodeList = nodeList;
	}
	
	/** 获取频道列表 */
	public ArrayList<Node> getNodeList() {
		return nodeList;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return nodeList == null ? 0 : nodeList.size();
	}

	@Override
	public Node getItem(int position) {
		// TODO Auto-generated method stub
		if (nodeList != null && nodeList.size() != 0) {
			return nodeList.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = LayoutInflater.from(context).inflate(R.layout.channel_item, null);
		item_text = (TextView) view.findViewById(R.id.text_item);
		Node node = getItem(position);
		item_text.setText(node.nodename);
		if ((position == 0) || (position == 1)){
//			item_text.setTextColor(context.getResources().getColor(R.color.black));
			item_text.setEnabled(false);
		}
		//
		if (isChanged && (position == holdPosition) && !isItemShow) {
			item_text.setText("");
			item_text.setSelected(true);
			item_text.setEnabled(true);
			isChanged = false;
		}
		//点击更多频道时，我的频道最后一个Item不可见
		if (!isVisible && (position == -1 + nodeList.size())) {
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
	public void addItem(Node node) {
		nodeList.add(node);
		isListChanged = true;
		notifyDataSetChanged();
	}

	/** 拖动变更频道排序 */
	public void exchange(int dragPostion, int dropPostion) {
		holdPosition = dropPostion;
		Node dragItem = getItem(dragPostion);
		BaseTools.showlog("dragPostion=" + dragPostion + ";endPosition=" + dropPostion);
		if (dragPostion < dropPostion) {
			nodeList.add(dropPostion + 1, dragItem);
			nodeList.remove(dragPostion);
		} else {
			nodeList.add(dropPostion, dragItem);
			nodeList.remove(dragPostion + 1);
		}
		isChanged = true;
		isListChanged = true;
		notifyDataSetChanged();
		//排序结束后发送广播通知记录更新后的nodelist顺序到数据库
		Intent intent = new Intent("record_order");
		context.sendBroadcast(intent);
	}

	/** 设置删除的position */
	public void setRemove(int position) {
		remove_position = position;
		notifyDataSetChanged();
	}

	/** 删除频道列表 */
	public void remove() {
		nodeList.remove(remove_position);
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
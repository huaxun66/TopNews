package com.huaxun.news.adapter;

import java.util.List;

import com.huaxun.R;
import com.huaxun.news.bean.Node;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class OtherAdapter extends BaseAdapter {
	private Context context;
	public List<Node> nodeList;
	private TextView item_text;
	/** 是否可见 */
	boolean isVisible = true;
	/** 要删除的position */
	public int remove_position = -1;

	public OtherAdapter(Context context){
		this.context = context;
	}
	
	public void setNodeList(List<Node> nodeList){
		this.nodeList = nodeList;
	}

	@Override
	public int getCount() {
		return nodeList == null ? 0 : nodeList.size();
	}

	@Override
	public Node getItem(int position) {
		if (nodeList != null && nodeList.size() != 0) {
			return nodeList.get(position);
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
		Node node = getItem(position);
		item_text.setText(node.nodename);
		if (!isVisible && (position == -1 + nodeList.size())){
			item_text.setText("");
		}
		if(remove_position == position){
			item_text.setText("");
		}
		return view;
	}
	
	/** 获取频道列表 */
	public List<Node> getNodeList() {
		return nodeList;
	}
	
	/** 添加频道列表 */
	public void addItem(Node node) {
		nodeList.add(node);
		notifyDataSetChanged();
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
		notifyDataSetChanged();
	}
	/** 设置频道列表 */
	public void setListDate(List<Node> list) {
		nodeList = list;
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
package com.huaxun.utils;

import java.util.ArrayList;

import com.huaxun.news.bean.Node;
import com.huaxun.tool.BaseTools;

import android.content.Context;
import android.content.SharedPreferences;

public class NodeUtil {

	//把列表各项nodename按顺序存储到SharedPreferences
	public static void setNodeListOrder(Context context, ArrayList<Node> nodeList) {
		if (nodeList == null)
			return;
		SharedPreferences mSharedPreferences= context.getSharedPreferences("NodeListOrder", context.MODE_PRIVATE);
		SharedPreferences.Editor editor = mSharedPreferences.edit();
		StringBuilder stringbuilder = new StringBuilder();
		if (nodeList.size() > 0) {
			for (int i=0; i<nodeList.size()-1; i++) {
				stringbuilder.append(nodeList.get(i).nodename).append(",");
			}
			stringbuilder.append(nodeList.get(nodeList.size()-1).nodename);
		}
		String nodeString = stringbuilder.toString();
//		BaseTools.showlog(nodeString);
		editor.putString("nodeString", nodeString);
		editor.commit();
	}
	
	//结合之前存储在SharedPreferences里各列表项顺序， 将nodeList重新排序
	public static void reorderNodeList (Context context, ArrayList<Node> nodeList) {
        ArrayList<Node> tempNodeList = new ArrayList<Node>();
        ArrayList<Node> backUpNodeList = new ArrayList<Node>();
        backUpNodeList.addAll(nodeList);
		SharedPreferences mSharedPreferences= context.getSharedPreferences("NodeListOrder", context.MODE_PRIVATE);
		String nodeString = mSharedPreferences.getString("nodeString", "null");
		if (nodeString.equals("null")){
			setNodeListOrder(context,nodeList);
		} else {
			String[] listOrder = nodeString.split(",");
//			for(int k =0 ;k<listOrder.length;k++)
//				BaseTools.showlog("reorderNodeName="+listOrder[k]);
			for (int i=0; i<listOrder.length; i++) {
				for (int j=0; j<nodeList.size(); j++) {
					if (listOrder[i].equals(nodeList.get(j).nodename)) {
						tempNodeList.add(nodeList.get(j));
						backUpNodeList.remove(nodeList.get(j));
					}
				}
			}
			if (backUpNodeList.size()>0) {
				for (int i=0; i<backUpNodeList.size(); i++) {
					tempNodeList.add(backUpNodeList.get(i));
				}
			}
		nodeList.clear();
		nodeList.addAll(tempNodeList);
		}
	}
}

package com.huaxun.utils;

import java.util.ArrayList;

import com.huaxun.more.bean.Column;
import com.huaxun.news.bean.Node;
import com.huaxun.tool.BaseTools;
import com.huaxun.tool.Constants;

import android.content.Context;
import android.content.SharedPreferences;

public class ColumnUtil {

	public static ArrayList<Column> getAllColumn() {
		ArrayList<Column> columnList = new ArrayList<>();
		columnList.add(new Column(Constants.BTN_FLAG_NEWS, Constants.FRAGMENT_FLAG_NEWS));
		columnList.add(new Column(Constants.BTN_FLAG_LIFE, Constants.FRAGMENT_FLAG_LIFE));
		columnList.add(new Column(Constants.BTN_FLAG_MUSIC, Constants.FRAGMENT_FLAG_MUSIC));
		columnList.add(new Column(Constants.BTN_FLAG_RADIO, Constants.FRAGMENT_FLAG_RADIO));
		columnList.add(new Column(Constants.BTN_FLAG_CHAT, Constants.FRAGMENT_FLAG_CHAT));
	    return columnList;
	}
	
	public static ArrayList<Column> getFgColumn(Context context) {
		ArrayList<Column> allColumnList = getAllColumn();
		ArrayList<Column> fgColumnList = new ArrayList<>();
		SharedPreferences mSharedPreferences= context.getSharedPreferences("ColumnListOrder", context.MODE_PRIVATE);
		String columnString = mSharedPreferences.getString("columnString", null);
		if (columnString == null) {
			return allColumnList;
		} else {
			String[] listOrder = columnString.split(",");
			for (String name : listOrder) {
				for (Column column : allColumnList) {
					if (column.columnName.equals(name)) {
						fgColumnList.add(column);
					}
				}
			}
			return fgColumnList;
		}
	}
	
	public static ArrayList<Column> getBgColumn(Context context) {	
		ArrayList<Column> bgColumnList = getAllColumn();
		bgColumnList.removeAll(getFgColumn(context));
        return bgColumnList;
	}
	
	
	//把列表各项columnName按顺序存储到SharedPreferences
	public static void setColumnListOrder(Context context, ArrayList<Column> columnList) {
		if (columnList == null)
			return;
		SharedPreferences mSharedPreferences= context.getSharedPreferences("ColumnListOrder", context.MODE_PRIVATE);
		SharedPreferences.Editor editor = mSharedPreferences.edit();
		StringBuilder stringbuilder = new StringBuilder();
		if (columnList.size() > 0) {
			for (int i = 0; i < columnList.size() - 1; i++) {
				stringbuilder.append(columnList.get(i).columnName).append(",");
			}
			stringbuilder.append(columnList.get(columnList.size()-1).columnName);
		}
		String columnString = stringbuilder.toString();
		BaseTools.showlog(columnString);
		editor.putString("columnString", columnString);
		editor.commit();
	}
	
}

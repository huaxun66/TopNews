package com.huaxun.utils;

import java.util.ArrayList;

import com.huaxun.radio.bean.RadioItem;
import com.huaxun.tool.BaseTools;

import android.content.Context;
import android.content.SharedPreferences;

public class radioUtil {
	
	public final static String radioSharePreferenceKey = "RadioKey";
	
	public static void setRadioListOrder(Context context,String radioListName, ArrayList<RadioItem> radioList){
		if (radioList == null)
			return;
		SharedPreferences sharedPreferences = context.getSharedPreferences(radioSharePreferenceKey,Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		
		StringBuilder stringBuilder = new StringBuilder();
		if (radioList.size() > 0){
			for (int i = 0; i < radioList.size(); i++){
				if (i == (radioList.size() - 1) ){
					stringBuilder.append(radioList.get(i).newstitle);
				}else{
					stringBuilder.append(radioList.get(i).newstitle).append(",");
				}
			}
			String radioString = stringBuilder.toString();
			editor.putString(radioListName, radioString);
//			BaseTools.showlog("SaveRadioString : "+radioString);
			editor.commit();
		}		
	}
	
	public static ArrayList<RadioItem> getRadioItemListOrder(Context context,String radioListName,ArrayList<RadioItem> radioListInput){
		ArrayList<RadioItem> radioList = new ArrayList<RadioItem> ();
		ArrayList<RadioItem> radioListBackUp = new ArrayList<RadioItem> ();
		radioListBackUp.addAll(radioListInput);
		SharedPreferences sharedPreferences = context.getSharedPreferences(radioSharePreferenceKey,Context.MODE_PRIVATE);
		
		String radioString = sharedPreferences.getString(radioListName, "null");
		//NewsConstants.showLog("radioListName  : "+radioListName+" getRadioItemListOrder :::   "+radioString);
		
		if (radioString.equals("null")){
			setRadioListOrder(context,radioListName,radioListBackUp);
			radioList = radioListBackUp;
		}else{
			String[] radioTitleList = radioString.split(",");
			if (radioTitleList != null){
				for (int i = 0 ; i < radioTitleList.length; i++){
					for (int j = 0; j < radioListBackUp.size(); j++){
						if (radioTitleList[i].equals(radioListBackUp.get(j).newstitle)){
							radioList.add(radioListBackUp.get(j));
							radioListBackUp.remove(j);
						}
					}
				}
			}
			
			for (int i = 0; i < radioListBackUp.size(); i++){
				radioList.add(radioListBackUp.get(i));
			}	
		}
		return radioList;
	}

}

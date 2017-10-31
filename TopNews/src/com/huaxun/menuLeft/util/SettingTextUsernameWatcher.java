package com.huaxun.menuLeft.util;

import java.util.regex.Pattern;
import android.content.Context;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Toast;

/**
 * 输入框输入范围控制
 */
public class SettingTextUsernameWatcher implements TextWatcher {
	private int editStart ;
	private int editCount ;
	private EditTextPreference mEditTextPreference;
	private Context mContext;
	
	public SettingTextUsernameWatcher(Context context,EditTextPreference e) {
		mContext = context;
		mEditTextPreference = e;
	 }
	
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		editStart = start;
		editCount = count;
	}
	
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,int after) {		
	}
	
	@Override
	public void afterTextChanged(Editable s) {
		if (TextUtils.isEmpty(s)) {
			return;
		}
		String content = s.toString();

		if (!isUsername(content)) {
			s.delete(editStart, editStart+editCount);
			mEditTextPreference.getEditText().setText(s);
			Toast.makeText(mContext, "只能使用英文字母或者字母和数字的组合哦", Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * 正则表达式-判断是否为数字
	 */
	public static boolean isUsername(String str){
	    Pattern pattern = Pattern.compile("[a-zA-Z0-9]+"); 
	    return pattern.matcher(str).matches();    
	 } 

};

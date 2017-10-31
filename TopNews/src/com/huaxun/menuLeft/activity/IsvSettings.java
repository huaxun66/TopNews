package com.huaxun.menuLeft.activity;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.huaxun.R;
import com.huaxun.menuLeft.util.SettingTextUsernameWatcher;
import com.huaxun.utils.Util;

/**
 * 声纹解锁设置界面
 */
public class IsvSettings extends PreferenceActivity implements OnPreferenceChangeListener {

	public static final String PREFER_NAME = "com.iflytek.setting";
	private EditTextPreference mUsernamePreference;
	
	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState) {
		final boolean isCustom = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		super.onCreate(savedInstanceState);
		
		if(isCustom){
			getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_bar);
		}
		TextView title_text = (TextView)findViewById(R.id.topTitle);
		title_text.setText("ISV设置");
		TextView back = (TextView)findViewById(R.id.topBack);
		back.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				finish();
				overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
			}
		});
		
		getPreferenceManager().setSharedPreferencesName(PREFER_NAME);
		addPreferencesFromResource(R.xml.isv_setting);
		
		mUsernamePreference = (EditTextPreference)findPreference("isv_username_preference");
		mUsernamePreference.getEditText().addTextChangedListener(new SettingTextUsernameWatcher(IsvSettings.this, mUsernamePreference));
	}
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		return true;
	}
}

package com.huaxun.menuLeft.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.huaxun.R;
import com.huaxun.base.BaseActivity;
import com.huaxun.utils.Util;

public class EncryptActivity extends BaseActivity implements OnClickListener{
	private TextView title, back;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Util.setTranslucent(this);
		setContentView(R.layout.encrypt_layout);
		title = (TextView)findViewById(R.id.topTitle);
		back = (TextView)findViewById(R.id.topBack);
		
		title.setText("加密文件");
		back.setOnClickListener(this);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.topBack:
			finish();
			overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
			break;
		}
	}
	
	
}

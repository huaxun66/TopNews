package com.huaxun.menuLeft.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.huaxun.R;
import com.huaxun.base.BaseActivity;
import com.huaxun.tool.BaseTools;
import com.huaxun.tool.Settings;
import com.huaxun.utils.FileUtil;
import com.huaxun.utils.Util;

public class PictureWallActivity extends BaseActivity implements OnClickListener {
	private TextView topBack, topTitle;
	private final int CLEAN_PICTURE_CACHE = 0;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Util.setTranslucent(this);
		setContentView(R.layout.picture_wall_layout);
		topBack = (TextView) findViewById(R.id.topBack);
		topBack.setOnClickListener(this);
		topTitle = (TextView) findViewById(R.id.topTitle);
		topTitle.setText("照片墙");
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		menu.add(0, CLEAN_PICTURE_CACHE, 0, "清空图片缓存");
		
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case CLEAN_PICTURE_CACHE:
			boolean flag = FileUtil.deleteDirectory(FileUtil.getPicturePath(), false);
			if (flag == true) {
				Util.showToast(getApplication(), "清空图片缓存成功");
			} else {
				Util.showToast(getApplication(), "清空图片缓存失败");
			}			
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.topBack:
			finish();
			overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
			break;
		}
	}
	
	
	
}

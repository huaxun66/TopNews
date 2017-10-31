package com.huaxun.menuLeft.activity;

import java.util.ArrayList;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.huaxun.R;
import com.huaxun.base.BaseActivity;
import com.huaxun.tool.BaseTools;
import com.huaxun.tool.BitmapHelp;
import com.huaxun.utils.ImageUtil;
import com.huaxun.utils.Util;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.bitmap.BitmapCommonUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.lidroid.xutils.bitmap.callback.DefaultBitmapLoadCallBack;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

public class PictureActivity extends BaseActivity implements OnClickListener {
	private TextView topBack, topTitle;
	private ListView imageListView;
	private ImageListAdapter imageListAdapter;
	private ArrayList<String> imgSrcList = new ArrayList<String>();

	public static BitmapUtils bitmapUtils;

	private String[] imgSites = { 
			"http://image.baidu.com/",
			"http://www.22mm.cc/", 
			"http://www.moko.cc/",
			"http://eladies.sina.com.cn/photo/", 
			"http://www.youzi4.com/" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Util.setTranslucent(this);
		setContentView(R.layout.picture_layout);
		initBitmapUtils();
		initView();
		imageListAdapter = new ImageListAdapter();
		imageListView.setAdapter(imageListAdapter);
		// 滑动时加载图片，快速滑动时不加载图片
		// imageListView.setOnScrollListener(new PauseOnScrollListener(bitmapUtils, false, true));
		for (String url : imgSites) {
			loadImgList(url);
		}
	}

	private void initBitmapUtils() {
		bitmapUtils = BitmapHelp.getBitmapUtils(this);
		bitmapUtils.configDefaultLoadingImage(R.drawable.pic_empty);
		bitmapUtils.configDefaultLoadFailedImage(R.drawable.pic_empty);
		bitmapUtils.configDefaultBitmapConfig(Config.RGB_565);

		// bitmapUtils.configMemoryCacheEnabled(false);
		// bitmapUtils.configDiskCacheEnabled(false);

		// bitmapUtils.configDefaultAutoRotation(true);

		// ScaleAnimation animation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
		// Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		// animation.setDuration(800);

		// AlphaAnimation 在一些android系统上表现不正常, 造成图片列表中加载部分图片后剩余无法加载, 目前原因不明.
		// 可以模仿下面示例里的fadeInDisplay方法实现一个颜色渐变动画。
		// AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
		// animation.setDuration(1000);
		// bitmapUtils.configDefaultImageLoadAnimation(animation);

		// 设置最大宽高, 不设置时更具控件属性自适应.
		bitmapUtils.configDefaultBitmapMaxSize(BitmapCommonUtils.getScreenSize(this).scaleDown(3));
	}

	private void initView() {
		imageListView = (ListView) findViewById(R.id.img_list);
		topBack = (TextView) findViewById(R.id.topBack);
		topBack.setOnClickListener(this);
		topTitle = (TextView) findViewById(R.id.topTitle);
		topTitle.setText("图集");
	}

	private void loadImgList(String url) {
		new HttpUtils().send(HttpRequest.HttpMethod.GET, url,new RequestCallBack<String>() {
					@Override
					public void onSuccess(ResponseInfo<String> responseInfo) {
						imgSrcList.addAll(ImageUtil.getImgSrcList(responseInfo.result));
						imageListAdapter.notifyDataSetChanged();// 通知listview更新数据
					}

					@Override
					public void onFailure(HttpException error, String msg) {
					}
				});
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

	private class ImageListAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public ImageListAdapter() {
			super();
			mInflater = LayoutInflater.from(PictureActivity.this);
		}

		@Override
		public int getCount() {
			return imgSrcList.size();
		}

		@Override
		public Object getItem(int position) {
			return imgSrcList.get(position);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(final int position, View view, ViewGroup parent) {
			ImageItemHolder holder = null;
			if (view == null) {
				view = mInflater.inflate(R.layout.bitmap_item, null);
				holder = new ImageItemHolder(view);
				view.setTag(holder);
			} else {
				holder = (ImageItemHolder) view.getTag();
			}
			holder.imgPb.setProgress(0);
			bitmapUtils.display(holder.imgItem, imgSrcList.get(position),new CustomBitmapLoadCallBack(holder));
			BaseTools.showlog("position="+position);
			// bitmapUtils.display((ImageView) view, imgSrcList.get(position),displayConfig);
			// bitmapUtils.display((ImageView) view, imgSrcList.get(position));
			
			holder.imgItem.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					Intent intent = new Intent(PictureActivity.this, SpaceImageDetailActivity.class);
					intent.putExtra("images", (ArrayList<String>) imgSrcList);
					intent.putExtra("position", position);
					int[] location = new int[2];
					view.getLocationOnScreen(location);
					intent.putExtra("locationX", location[0]);
					intent.putExtra("locationY", location[1]);

					intent.putExtra("width", view.getWidth());
					intent.putExtra("height", view.getHeight());
					startActivity(intent);
					overridePendingTransition(0, 0);
				}
			});
			
			return view;
		}
	}

	private class ImageItemHolder {
		private ImageView imgItem;
		private ProgressBar imgPb;

		public ImageItemHolder(View view) {
			imgItem = (ImageView) view.findViewById(R.id.img_item);
			imgPb = (ProgressBar) view.findViewById(R.id.img_pb);
		}
	}

	public class CustomBitmapLoadCallBack extends DefaultBitmapLoadCallBack<ImageView> {
		private final ImageItemHolder holder;

		public CustomBitmapLoadCallBack(ImageItemHolder holder) {
			this.holder = holder;
		}

		@Override
		public void onLoading(ImageView container, String uri,
				BitmapDisplayConfig config, long total, long current) {
			this.holder.imgPb.setProgress((int) (current * 100 / total));
		}

		@Override
		public void onLoadCompleted(ImageView container, String uri,
				Bitmap bitmap, BitmapDisplayConfig config, BitmapLoadFrom from) {
			fadeInDisplay(container, bitmap);
			this.holder.imgPb.setProgress(100);
		}
	}

	private static final ColorDrawable TRANSPARENT_DRAWABLE = new ColorDrawable(android.R.color.transparent);

	private void fadeInDisplay(ImageView imageView, Bitmap bitmap) {
		final TransitionDrawable transitionDrawable = new TransitionDrawable(new Drawable[] { TRANSPARENT_DRAWABLE,
						new BitmapDrawable(imageView.getResources(), bitmap) });
		imageView.setImageDrawable(transitionDrawable);
		transitionDrawable.startTransition(500);
	}

}

package com.handmark.pulltorefresh.library.internal;

import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Orientation;
import com.handmark.pulltorefresh.library.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;

public class FrameLoadingLayout extends LoadingLayout {
	
	public FrameLoadingLayout(Context context, Mode mode, Orientation scrollDirection, TypedArray attrs) {
		super(context, mode, scrollDirection, attrs);
		mHeaderImage.setImageResource(R.drawable.loading_frame_anim);
	}

	public void onLoadingDrawableSet(Drawable imageDrawable) {
	}

	protected void onPullImpl(float scaleOfLayout) {
	}

	@Override
	protected void refreshingImpl() {
	}

	@Override
	protected void resetImpl() {
	}


	@Override
	protected void pullToRefreshImpl() {
		// NO-OP
	}

	@Override
	protected void releaseToRefreshImpl() {
		// NO-OP
	}

	@Override
	protected int getDefaultDrawableResId() {
		return R.drawable.loading_frame_anim;
	}

}

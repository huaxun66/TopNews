package com.huaxun.tool;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public class VolleyTool {
	private static VolleyTool mInstance = null;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    
    private VolleyTool(Context context) {
    	mRequestQueue = Volley.newRequestQueue(context);
    	mImageLoader = new ImageLoader(mRequestQueue, new BitmapCache());
    }
    
    //单例模式
    public static VolleyTool getInstance(Context context){
        if(mInstance == null){
    		mInstance = new VolleyTool(context);
        }
        return mInstance;
    }
    
	public RequestQueue getmRequestQueue() {
		return mRequestQueue;
	}

	public ImageLoader getmImageLoader() {
		return mImageLoader;
	}

	public void release() {
		this.mImageLoader = null;
		this.mRequestQueue = null;
		mInstance = null;
	}
}

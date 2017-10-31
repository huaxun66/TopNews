package com.huaxun.news.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.huaxun.R;
import com.huaxun.app.AppApplication;
import com.huaxun.utils.FileUtil;
import com.huaxun.utils.NetUtil;
import com.huaxun.utils.Util;
import com.huaxun.view.ObservableWebView;
import com.huaxun.view.ObservableWebView.OnScrollChangedCallback;


public class GaoJianFragment extends BaseFragment {
	private Context context;
	private String refreshURL = "";
	private String nodename = "";
	private ObservableWebView webView;
	private ProgressBar progressBar;
	private WebSettings webSettings;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		this.context = this.getActivity();
		Bundle args = getArguments();
		refreshURL = args != null ? args.getString("refreshURL") : "";
		nodename = args != null ? args.getString("nodeName") : "";
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.gaojian_layout, null);
		webView = (ObservableWebView)view.findViewById(R.id.webViewID);
		progressBar = (ProgressBar)view.findViewById(R.id.webview_progressBar);
		webSettings = webView.getSettings();
		webView.setOnScrollChangedCallback(new OnScrollChangedCallback() {
			@Override
			public void onScroll(int dx, int dy) {
					if (dy > 0){
						mainAct.hideBottomPanel();
					}else{
						mainAct.showBottomPanel();
					}
				}				
		});
		webView.setWebChromeClient(webChromeClient);
		String content = "";
		String newsContentFile = FileUtil.getNewsPath() + "/" + nodename;
		if (NetUtil.isWIFIOn(context)){
			webView.loadUrl(refreshURL);
		}else{
			if (FileUtil.isNewsFileExist(nodename)){
				content = FileUtil.ReadLocalNewsFile(newsContentFile);
				webView.loadDataWithBaseURL("", content, "text/html", "UTF-8", "");
			}
		}
		return view;
	}
	
	private WebChromeClient webChromeClient = new WebChromeClient(){
		public void onProgressChanged(WebView view, int newProgress) {
				if (null != progressBar) {
					if (newProgress == 100) {
						progressBar.setVisibility(View.GONE);
					} else {
						if (progressBar.getVisibility() == View.GONE) {
							progressBar.setVisibility(View.VISIBLE);
						}
						progressBar.setProgress(newProgress);
					}
				}
			super.onProgressChanged(view, newProgress);
		}
	};
	
}

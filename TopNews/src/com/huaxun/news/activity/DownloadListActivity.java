package com.huaxun.news.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.huaxun.MainActivity;
import com.huaxun.R;
import com.huaxun.dialog.ExitDialog;
import com.huaxun.download.DownloadInfo;
import com.huaxun.download.DownloadManager;
import com.huaxun.download.DownloadService;
import com.huaxun.fragment.NewsFragment;
import com.huaxun.news.fragment.NewsMediaFragment;
import com.huaxun.radio.activity.RadioMoreActivity;
import com.huaxun.tool.BaseTools;
import com.huaxun.utils.Util;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Author: wyouflf
 * Date: 13-11-20
 * Time: 上午12:12
 */
public class DownloadListActivity extends Activity {
    private boolean fromNewsMediaFragment = false;
    private boolean fromMusicFragment = false;
    
    @ViewInject(R.id.download_list)
    private ListView downloadList;
    
    @ViewInject(R.id.topBack)
    private TextView topBack;
    
    @ViewInject(R.id.topTitle)
    private TextView topTitle;
    
    @OnClick(R.id.topBack)
    public void back(View view) {  //这种方式定义onclick事件，形参必须是View view
		finish();
		overridePendingTransition(R.anim.scale_out, 0);
		if (fromNewsMediaFragment == true) {
			Intent it = new Intent("showFloat");
			sendBroadcast(it);
		}
    }
    
	@Override
	public void onBackPressed() {
		finish();
		overridePendingTransition(R.anim.scale_out, 0); 
		if (fromNewsMediaFragment == true) {
			Intent it = new Intent("showFloat");
			sendBroadcast(it);
		}
	}

    private DownloadManager downloadManager;
    private DownloadListAdapter downloadListAdapter;

    private Context mAppContext;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Util.setTranslucent(this);
        setContentView(R.layout.download_list);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.getString("source") != null) {
        	if (bundle.getString("source").equals("NewsMediaFragment")) {
        		fromNewsMediaFragment = true;
        	} else if (bundle.getString("source").equals("MusicFragment")) {
        		fromMusicFragment = true;
        	}
        }
        	
        ViewUtils.inject(this);
        topTitle.setText("下载管理");

        mAppContext = this.getApplicationContext();

        downloadManager = DownloadService.getDownloadManager(mAppContext);

        downloadListAdapter = new DownloadListAdapter(mAppContext);
        downloadList.setAdapter(downloadListAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        downloadListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        try {
            if (downloadListAdapter != null && downloadManager != null) {
                downloadManager.backupDownloadInfoList();
            }
        } catch (DbException e) {
            LogUtils.e(e.getMessage(), e);
        }
        super.onDestroy();
    }

    private class DownloadListAdapter extends BaseAdapter {

        private final Context mContext;
        private final LayoutInflater mInflater;

        private DownloadListAdapter(Context context) {
            mContext = context;
            mInflater = LayoutInflater.from(mContext);
        }

        @Override
        public int getCount() {
            if (downloadManager == null) return 0;
            return downloadManager.getDownloadInfoListCount();
        }

        @Override
        public Object getItem(int i) {
            return downloadManager.getDownloadInfo(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @SuppressWarnings("unchecked")
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            DownloadItemViewHolder holder = null;
            DownloadInfo downloadInfo = downloadManager.getDownloadInfo(i);
            if (view == null) {
                view = mInflater.inflate(R.layout.download_item, null);
                holder = new DownloadItemViewHolder(downloadInfo);
                ViewUtils.inject(holder, view);
                view.setTag(holder);
                holder.refresh();
            } else {
                holder = (DownloadItemViewHolder) view.getTag();
                holder.update(downloadInfo);
            }

            HttpHandler<File> handler = downloadInfo.getHandler();
            if (handler != null) {
                RequestCallBack callBack = handler.getRequestCallBack();
                if (callBack instanceof DownloadManager.ManagerCallBack) {
                    DownloadManager.ManagerCallBack managerCallBack = (DownloadManager.ManagerCallBack) callBack;
                    if (managerCallBack.getBaseCallBack() == null) {
                        managerCallBack.setBaseCallBack(new DownloadRequestCallBack());
                    }
                }
                callBack.setUserTag(new WeakReference<DownloadItemViewHolder>(holder));
            }

            return view;
        }
    }

    public class DownloadItemViewHolder {
        @ViewInject(R.id.download_label)
        TextView label;
        @ViewInject(R.id.download_state)
        TextView state;
        @ViewInject(R.id.download_pb)
        ProgressBar progressBar;
        @ViewInject(R.id.download_stop_btn)
        TextView stopBtn;
        @ViewInject(R.id.download_remove_btn)
        TextView removeBtn;

        private DownloadInfo downloadInfo;

        public DownloadItemViewHolder(DownloadInfo downloadInfo) {
            this.downloadInfo = downloadInfo;
        }

        @OnClick(R.id.download_stop_btn)
        public void stop(View view) {
            HttpHandler.State state = downloadInfo.getState();
            switch (state) {
                case WAITING:
                case STARTED:
                case LOADING:
                    try {
                        downloadManager.stopDownload(downloadInfo);
                    } catch (DbException e) {
                        LogUtils.e(e.getMessage(), e);
                    }
                    break;
                case CANCELLED:
                case FAILURE:
                    try {
                        downloadManager.resumeDownload(downloadInfo, new DownloadRequestCallBack());
                    } catch (DbException e) {
                        LogUtils.e(e.getMessage(), e);
                    }
                    downloadListAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }

        @OnClick(R.id.download_remove_btn)
        public void remove(View view) {
            try {
                downloadManager.removeDownload(downloadInfo);
                downloadListAdapter.notifyDataSetChanged();
            } catch (DbException e) {
                LogUtils.e(e.getMessage(), e);
            }
        }

        public void update(DownloadInfo downloadInfo) {
            this.downloadInfo = downloadInfo;
            refresh();
        }

        public void refresh() {
            label.setText(downloadInfo.getFileName());
            state.setText(downloadInfo.getState().toString());
            if (downloadInfo.getFileLength() > 0) {
                progressBar.setProgress((int) (downloadInfo.getProgress() * 100 / downloadInfo.getFileLength()));
            } else {
                progressBar.setProgress(0);
            }

            stopBtn.setVisibility(View.VISIBLE);
            stopBtn.setText(mAppContext.getString(R.string.stop));
            HttpHandler.State state = downloadInfo.getState();
            switch (state) {
                case WAITING:
                    stopBtn.setText(mAppContext.getString(R.string.stop));
                    break;
                case STARTED:
                    stopBtn.setText(mAppContext.getString(R.string.stop));
                    break;
                case LOADING:
                    stopBtn.setText(mAppContext.getString(R.string.stop));
                    break;
                case CANCELLED:
                    stopBtn.setText(mAppContext.getString(R.string.resume));
                    break;
                case SUCCESS:
                    stopBtn.setVisibility(View.INVISIBLE);
                    break;
                case FAILURE:
                    stopBtn.setText(mAppContext.getString(R.string.retry));
                    break;
                default:
                    break;
            }
        }
    }

    private class DownloadRequestCallBack extends RequestCallBack<File> {
        @SuppressWarnings("unchecked")
        private void refreshListItem() {
            if (userTag == null) return;
            WeakReference<DownloadItemViewHolder> tag = (WeakReference<DownloadItemViewHolder>) userTag;
            DownloadItemViewHolder holder = tag.get();
            if (holder != null) {
                holder.refresh();
            }
        }

        @Override
        public void onStart() {
            refreshListItem();
        }

        @Override
        public void onLoading(long total, long current, boolean isUploading) {
            refreshListItem();
        }

        @Override
        public void onSuccess(ResponseInfo<File> responseInfo) {
            refreshListItem();
        }

        @Override
        public void onFailure(HttpException error, String msg) {
            refreshListItem();
        }

        @Override
        public void onCancelled() {
            refreshListItem();
        }
    }
}
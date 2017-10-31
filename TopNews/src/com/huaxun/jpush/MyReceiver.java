package com.huaxun.jpush;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.RemoteViews;
import cn.jpush.android.api.JPushInterface;

import com.huaxun.MainActivity;
import com.huaxun.R;
import com.huaxun.Welcome;
import com.huaxun.news.bean.News;
import com.huaxun.tool.BaseTools;

/**
 * 自定义接收器
 * 
 * 如果不定义这个 Receiver，则：
 * 1) 默认用户会打开主界面
 * 2) 接收不到自定义消息
 */
public class MyReceiver extends BroadcastReceiver {
	private static final String APP_NAME = "com.huaxun";
	private Context mContext = null;
	private ActivityManager activityManager = null;
	private int notificationId = -1;
	private News news;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		mContext = context.getApplicationContext();
        Bundle bundle = intent.getExtras();
		BaseTools.showlog("[MyReceiver] onReceive - " + intent.getAction() + ", extras: " + printBundle(bundle));
		
        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            BaseTools.showlog("[MyReceiver] 接收Registration Id : " + bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID));                       
       
        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
        	BaseTools.showlog("[MyReceiver] 接收到推送下来的自定义消息: " + bundle.getString(JPushInterface.EXTRA_MESSAGE));
        	// 自定义消息不会展示在通知栏，完全要开发者写代码去处理
        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
        	BaseTools.showlog("[MyReceiver] 接收到推送下来的通知");
        	String titleStr = bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE);
			String msgStr = bundle.getString(JPushInterface.EXTRA_ALERT);
            BaseTools.showlog("[MyReceiver] 接收到推送下来的通知的Title:" + titleStr + " Msg:" + msgStr);
            
            notificationId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
            news = getNewsFromBundle(bundle);
            
            if (isRunningApp()) {
				Intent pushDialogIntent = new Intent("SHOW_PUSH_DIALOG");
				pushDialogIntent.putExtra("pushNews", news);
				mContext.sendBroadcast(pushDialogIntent);
			}
            
    		Intent mPushIntent = null;
    		if (isRunningApp()) {
    			mPushIntent = new Intent(mContext, MainActivity.class);
    		} else {
    			mPushIntent = new Intent(mContext, Welcome.class);
    		}
    		mPushIntent.putExtra("pushNews", news);
            showNotification(mPushIntent, news);
        	
        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
        	BaseTools.showlog("[MyReceiver] 用户点击打开了系统通知");
            
        	//打开自定义的Activity
        	Intent i = new Intent(context, JPushActivity.class);
        	i.putExtras(bundle);
        	//i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
        	context.startActivity(i);
        	
        } else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
        	BaseTools.showlog("[MyReceiver] 用户收到到RICH PUSH CALLBACK: " + bundle.getString(JPushInterface.EXTRA_EXTRA));
            //在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity， 打开一个网页等..
        	
        } else if(JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
        	boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
        	BaseTools.showlog("[MyReceiver]" + intent.getAction() +" connected state change to "+connected);
        
        } else {
        	BaseTools.showlog("[MyReceiver] Unhandled intent - " + intent.getAction());
        }
	}
	
	private News getNewsFromBundle(Bundle bundle){
			String extraJson = bundle.getString(JPushInterface.EXTRA_EXTRA);
			String url = "";
			try {
				JSONObject jsonObj = new JSONObject(extraJson);
				url = jsonObj.getString("newsurl");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			String title = bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE);
			String summary = bundle.getString(JPushInterface.EXTRA_ALERT);
			// 封装News
			News news = new News();		
			news.setNewstitle(!TextUtils.isEmpty(title) ? title : "推   送");		
			news.summary = !TextUtils.isEmpty(summary) ? summary : news.getNewstitle();
			news.setNewsurl(!TextUtils.isEmpty(url) ? url : "");
//			news.fromPush = true;
			news.parentNodeName = "推   送";
            return news;
	}


	// 打印所有的 intent extra 数据
	private static String printBundle(Bundle bundle) {
		StringBuilder sb = new StringBuilder();
		for (String key : bundle.keySet()) {
			if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
				sb.append("\nkey:" + key + ", value:" + bundle.getInt(key));
			} else if (key.equals(JPushInterface.EXTRA_CONNECTION_CHANGE)){
				sb.append("\nkey:" + key + ", value:" + bundle.getBoolean(key));
			} else if (key.equals(JPushInterface.EXTRA_EXTRA)) {
				if (bundle.getString(JPushInterface.EXTRA_EXTRA).isEmpty()) {
					BaseTools.showlog("This message has no Extra data");
					continue;
				}

				try {
					JSONObject json = new JSONObject(bundle.getString(JPushInterface.EXTRA_EXTRA));
					Iterator<String> it =  json.keys();

					while (it.hasNext()) {
						String myKey = it.next().toString();
						sb.append("\nkey:" + key + ", value: [" + myKey + " - " +json.optString(myKey) + "]");
					}
				} catch (JSONException e) {
					BaseTools.showlog("Get message extra JSON error!");
				}

			} else {
				sb.append("\nkey:" + key + ", value:" + bundle.getString(key));
			}
		}
		return sb.toString();
	}
	
	private boolean isRunningApp() {
		boolean isAppRunning = false;
		activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> list = activityManager.getRunningTasks(100);
		for (RunningTaskInfo info : list) {
			if (info.topActivity.getPackageName().equals(APP_NAME) && info.baseActivity.getPackageName().equals(APP_NAME)) {
				isAppRunning = true;
				break;
			}
		}
		return isAppRunning;
	}
	
	private String getCurrentTime(){
		SimpleDateFormat df = new SimpleDateFormat("HH:mm");
		return df.format(new Date());
	}
	
	private void showNotification(Intent mPushIntent , News news){
		//自定义的通知，先取消系统通知 
		NotificationManager notiManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		notiManager.cancel(notificationId);
		
		CharSequence tickerText = news.getNewstitle();
		int icon = R.drawable.icon_hx;
		long when = System.currentTimeMillis(); 
		Notification noti = new Notification(icon, tickerText, when);
		noti.flags = Notification.FLAG_AUTO_CANCEL;
		
		mPushIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, notificationId, mPushIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		noti.contentIntent = contentIntent;

		RemoteViews remoteView = new RemoteViews(mContext.getPackageName(), R.layout.customer_notitfication_layout);
		remoteView.setImageViewResource(R.id.icon, icon);
		remoteView.setTextViewText(R.id.text, news.summary);
		remoteView.setTextViewText(R.id.title, news.getNewstitle());
		remoteView.setTextViewText(R.id.time, getCurrentTime());
		noti.contentView = remoteView;

		notiManager.notify(notificationId, noti);
	}
	
}

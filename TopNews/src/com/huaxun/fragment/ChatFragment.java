package com.huaxun.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.huaxun.R;
import com.huaxun.chat.ChatDetailActivity;
import com.huaxun.chat.FileActivity;
import com.huaxun.chat.SettingDialog;
import com.huaxun.chat.bean.Msg;
import com.huaxun.chat.bean.MsgData;
import com.huaxun.chat.bean.User;
import com.huaxun.chat.util.FileTcpServer;
import com.huaxun.chat.util.SDCardHelper;
import com.huaxun.chat.util.Tools;
import com.huaxun.tool.BaseTools;
import com.huaxun.utils.FileUtil;

public class ChatFragment extends Fragment implements OnClickListener{
	
	private View view;
	private Context context;
	private ImageView friend_list_myImg;
	private TextView friend_list_myName;
	private ListView listView;
	public List<User> friendList = new ArrayList<User>();
	private FriendListAdapter adapter;
	private Tools tools=null;
	private SettingDialog settingDialog=null;
	private AlertDialog revCallDialog=null;
	private ProgressDialog proDia = null;
	private double sendFileSize;
	
	private SharedPreferences sharedPreferences;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this.getActivity();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		MsgData msgdata = new MsgData();
		for (String filename : Tools.msgContainer.keySet()) {
			msgdata.msgList = Tools.msgContainer.get(filename);
			Gson g = new Gson();
	        String json = g.toJson(msgdata);
	        BaseTools.showlog("json="+json);
	        try {
				SDCardHelper.saveFileToSDCardCustomDir(json.getBytes("UTF-8"), FileUtil.getCachePath(), filename);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		sharedPreferences = context.getSharedPreferences(SettingDialog.chatSharePreferenceKey,Context.MODE_PRIVATE);
		view = inflater.inflate(R.layout.chat_layout,container, false);
		friend_list_myImg = (ImageView) view.findViewById(R.id.friend_list_myImg);
		friend_list_myName = (TextView) view.findViewById(R.id.friend_list_myName);
		listView = (ListView) view.findViewById(R.id.listView);
		adapter = new FriendListAdapter();
		listView.setAdapter(adapter);
		Tools.chatFragment = this;
		tools = new Tools();
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				User friend = friendList.get(position);
				//当前人的聊天提示信息清零
				friend.setUnReadMsgCount(0);
				adapter.notifyDataSetChanged();
				//转入ChatDetailActivity	
				Intent it = new Intent(context, ChatDetailActivity.class);
				it.putExtra("person", friend);
				context.startActivity(it);
			}
		});	
		
		friend_list_myImg.setOnClickListener(this);
		friend_list_myName.setOnClickListener(this);
		
		proDia = new ProgressDialog(context);
		proDia.setTitle("文件发送");// 设置标题
		proDia.setMessage("文件");// 设置显示信息
		proDia.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);// 水平进度条
		proDia.setMax(100);// 设置最大进度指
		proDia.setProgress(10);// 开始点
		
		initData();
		return view;
	}
	
	private void initData() {
		String nickeName = sharedPreferences.getString("nickeName", Build.MODEL);
		int headIconPos = sharedPreferences.getInt("headIconPos", 0);
		Tools.me = new User(nickeName, headIconPos, Tools.getLocalHostIp(), 0, System.currentTimeMillis());
		friendList.add(Tools.me);
		Tools.loadMessage(Tools.me);
		adapter.notifyDataSetChanged();
		friend_list_myImg.setImageResource(Tools.headIconIds[Tools.me.getHeadIconPos()]);
		friend_list_myName.setText(Tools.me.getName());
		
		//广播上线(包括自己)
		reBroad();
		// 开启接收端 时时更新在线列表
		tools.receiveMsg();
		// 心跳
		tools.startCheck();
		// 语音监听
		if (!Tools.audio.isAlive()) {
			Tools.audio.start();
		}		
	}

	
	public Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Tools.CMD_UPDATEINFORMATION:
				//更新自己信息
				reBroad();
				friend_list_myImg.setImageResource(Tools.headIconIds[Tools.me.getHeadIconPos()]);
				friend_list_myName.setText(Tools.me.getName());
				break;
			case Tools.FLUSH:
				adapter.notifyDataSetChanged();
				break;
			case Tools.SHOW:
				Toast.makeText(ChatFragment.this.getActivity(), (String) msg.obj, Toast.LENGTH_SHORT).show();
				break;
			case Tools.DESTROYUSER:
				User user = (User) msg.obj;
				friendList.remove(user);
				adapter.notifyDataSetChanged();
				break;
			case Tools.REFLESHCOUNT:
				String ip=msg.obj.toString();
				for (int k = 0; k < friendList.size(); k++) {
					if (friendList.get(k).getIp().equals(ip)){ // 遍历
						int count = friendList.get(k).getUnReadMsgCount();						
						friendList.get(k).setUnReadMsgCount(++count);
					}
				}
				adapter.notifyDataSetChanged();
				break;
			case Tools.CMD_STARTTALK:
				//语音请求
				showReceiveCallDialog((Msg)msg.obj);
				break;
			case Tools.CMD_STOPTALK:
				//语音结束
				if(revCallDialog!=null && revCallDialog.isShowing())
					revCallDialog.dismiss();
				break;
			case Tools.CMD_FILEREQUEST:
				//文件请求
				showReceiveFileDialog((Msg)msg.obj);
				break;
			case Tools.FILE_JINDU:
				String[] pi = ((String) msg.obj).split(":");
				sendFileSize = Double.parseDouble(pi[1]);
				proDia.setTitle("发送文件");// 设置标题
				proDia.setMessage("文件：" + pi[0] + " 大小：" + FileActivity.getFormatSize(sendFileSize));// 设置显示信息
				proDia.onStart();
				proDia.show();
				fileProgress();
				break;
			case Tools.PROGRESS_FLUSH:
				int i0 = (int) ((Tools.sendProgress / (sendFileSize)) * 100);
				proDia.setProgress(i0);
				break;
			case Tools.PROGRESS_COL:// 关闭进度条
				proDia.dismiss();
				break;
			}
		}
	};  
	
    //广播自己
	private void reBroad(){
    	//广播上线(包括自己)
		Msg msg=new Msg();
		msg.setSendUser(Tools.me.getName());//昵称默认为自己的机器号
		msg.setHeadIconPos(Tools.me.getHeadIconPos());
		msg.setSendUserIp(Tools.me.getIp());
		msg.setReceiveUserIp(Tools.getBroadCastIP());
		msg.setMsgType(Tools.CMD_ONLINE);//通知上线命令
		msg.setPackId(Tools.getTimel());
		// 发送广播通知上线
		tools.sendMsg(msg);
    }
	
	// 接收语音请求
	private void showReceiveCallDialog(final Msg mes) {
		if(!Tools.stoptalk){
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle("来自:"+mes.getSendUser());
			builder.setMessage(null);
			builder.setIcon(null);
			View vi = this.getActivity().getLayoutInflater().inflate(R.layout.request_talk_layout, null);
			builder.setView(vi);
			revCallDialog = builder.show();
			revCallDialog.setCanceledOnTouchOutside(false);
			revCallDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface arg0) {
					//发送结束呼叫请求
					Msg msg=new Msg();
					msg.setSendUser(Tools.me.getName());//昵称默认为自己的机器号
					msg.setSendUserIp(Tools.me.getIp());
					msg.setReceiveUserIp(mes.getSendUserIp());
					msg.setMsgType(Tools.CMD_STOPTALK);
					msg.setPackId(Tools.getTimel());
					tools.sendMsg(msg);
				}
			});
			Button talkOkBtn = (Button)vi.findViewById(R.id.receive_talk_okbtn);
			talkOkBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View okBtn) {
					if(!Tools.stoptalk){//如果远程用户未关闭通话，则向对方发送同意接收通话指令
						Msg msg=new Msg();
						msg.setSendUser(Tools.me.getName());//昵称默认为自己的机器号
						msg.setSendUserIp(Tools.me.getIp());
						msg.setReceiveUserIp(mes.getSendUserIp());
						msg.setMsgType(Tools.CMD_ACCEPTTALK);
						msg.setPackId(Tools.getTimel());
						tools.sendMsg(msg);
						okBtn.setEnabled(false);
						//同意接收并开始传输语音数据
						User person=new User(mes.getSendUser(),mes.getSendUserIp());
						Tools.audio.audioSend(person);
					}
				}
			});
			Button talkCancelBtn = (Button)vi.findViewById(R.id.receive_talk_cancel);
			talkCancelBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View cancelBtn) {
					revCallDialog.dismiss();
				}
			});
		}
	}
	
	// 收到传送文件请求
	private void showReceiveFileDialog(final Msg mes) {
		final String str = mes.getBody().toString();
		new AlertDialog.Builder(context)
		.setTitle("来自:"+mes.getSendUser())
		.setMessage("是否接收文件?" + "\n文件名："+ str.split(":")[0] +"\n大小："+FileActivity.getFormatSize(Double.parseDouble(str.split(":")[1])))
		.setIcon(android.R.drawable.ic_dialog_info)
		.setPositiveButton("接受", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// 接收文件 返回提示接受 建立tcp 服务器 接收文件
				FileTcpServer ts = new FileTcpServer(mes);
				ts.start();
				// 发送消息 让对方开始发送文件
				Msg msg=new Msg(Tools.getTimel(), Tools.me.getHeadIconPos(), Tools.me.getName(), Tools.me.getIp(), mes.getSendUser(), mes.getSendUserIp(),Tools.CMD_FILEACCEPT, mes.getBody());
				tools.sendMsg(msg);
				//进度显示
				Tools.sendProgress = 0;
				Message m1 = new Message();
				m1.what = Tools.FILE_JINDU;
				m1.obj = mes.getBody();
				handler.sendMessage(m1);
				return;
			}
		})
		.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// 不接受 返回提示不接受
				Msg msg=new Msg(Tools.getTimel(), Tools.me.getHeadIconPos(),Tools.me.getName(), Tools.me.getIp(), mes.getSendUser(), mes.getSendUserIp(),Tools.CMD_FILEREFUSE, null);
				tools.sendMsg(msg);
				return;
			}
		}).show();
	}
	
	// 刷新文件传送进度条
	public void fileProgress() {
		new Thread() {
			public void run() {
				while (Tools.sendProgress != -1) {
					Message m = new Message();
					m.what = Tools.PROGRESS_FLUSH;
					handler.sendMessage(m);
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				// 关闭进度条
				Message m1 = new Message();
				m1.what = Tools.PROGRESS_COL;
				handler.sendMessage(m1);
			}
		}.start();
	}
	
	// 按钮点击事件
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		    case R.id.friend_list_myImg:
			case R.id.friend_list_myName:
				if(null==settingDialog) {
					settingDialog = new SettingDialog(context);
				}	
	    		settingDialog.show();
				break; 
		}
	}
	
	public class FriendListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return friendList.size();
		}

		@Override
		public User getItem(int position) {
			return friendList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			if(convertView == null)
			{
				viewHolder = new ViewHolder();
				convertView = LayoutInflater.from(context).inflate(R.layout.friend_item, null);
				viewHolder.headicon = (ImageView) convertView.findViewById(R.id.headicon);
				viewHolder.unreadMsgCount = (TextView) convertView.findViewById(R.id.unreadMsgCount);
				viewHolder.name = (TextView) convertView.findViewById(R.id.name);
				viewHolder.ip = (TextView) convertView.findViewById(R.id.ip);
				viewHolder.LastedMsg = (TextView) convertView.findViewById(R.id.LastedMsg);
				viewHolder.LastedTime = (TextView) convertView.findViewById(R.id.LastedTime);
				convertView.setTag(viewHolder);//表示给View添加一个格外的数据，
			} else {
				viewHolder = (ViewHolder)convertView.getTag();//通过getTag的方法将数据取出来
			}
			User friend = friendList.get(position);
			viewHolder.headicon.setImageResource(Tools.headIconIds[friend.getHeadIconPos()]);
			viewHolder.ip.setText(friend.getIp());
			if (friend.getIp().equals(Tools.me.getIp())) {
				viewHolder.name.setText("自己");
			} else {
				viewHolder.name.setText(friend.getName());
			}			
			
			if (friend.getUnReadMsgCount() > 0) {
				viewHolder.unreadMsgCount.setVisibility(View.VISIBLE);
				viewHolder.unreadMsgCount.setText(String.valueOf(friend.getUnReadMsgCount()));
			} else {
				viewHolder.unreadMsgCount.setVisibility(View.GONE);
			}

			if(Tools.msgContainer.containsKey(friend.getIp())) {// 如果存在此人的消息缓存
				viewHolder.LastedMsg.setVisibility(View.VISIBLE);
				viewHolder.LastedTime.setVisibility(View.VISIBLE);
				List<Msg> mesList = Tools.msgContainer.get(friend.getIp());
				Msg msg = mesList.get(mesList.size()-1);
				viewHolder.LastedTime.setText(Tools.getTime(msg.getPackId()));
				if (msg.getMsgType() == Tools.CMD_SENDMSG) {
					viewHolder.LastedMsg.setText((String)msg.getBody());
				} else if (msg.getMsgType() == Tools.CMD_SENDMEDIA) {
					viewHolder.LastedMsg.setText("语音");
				} else if (msg.getMsgType() == Tools.CMD_SENDIMAGE) {
					viewHolder.LastedMsg.setText("图片");
				} else if (msg.getMsgType() == Tools.CMD_SENDVIDEO) {
					viewHolder.LastedMsg.setText("视频");
				} else if (msg.getMsgType() == Tools.CMD_FILEREQUEST) {
					viewHolder.LastedMsg.setText("文件");
				} else if (msg.getMsgType() == Tools.CMD_FILEREFUSE) {
					viewHolder.LastedMsg.setText("拒绝文件接受");
				} else {
					viewHolder.LastedMsg.setText("其他...");
				}								
			}
		
			return convertView;
		}
		
		public class ViewHolder {
			//所有控件对象引用
			public ImageView headicon;
			public TextView unreadMsgCount;
			public TextView name;
			public TextView ip;
			public TextView LastedMsg;
			public TextView LastedTime;
		}
	}
	

}

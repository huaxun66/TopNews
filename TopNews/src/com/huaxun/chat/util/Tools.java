package com.huaxun.chat.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.http.conn.util.InetAddressUtils;

import android.os.Message;

import com.google.gson.Gson;
import com.huaxun.R;
import com.huaxun.chat.ChatDetailActivity;
import com.huaxun.chat.bean.Msg;
import com.huaxun.chat.bean.MsgData;
import com.huaxun.chat.bean.User;
import com.huaxun.fragment.ChatFragment;
import com.huaxun.news.bean.TodayRecommendData;
import com.huaxun.tool.BaseTools;
import com.huaxun.utils.FileUtil;


public class Tools {
	
	// 协议命令
	public static final int CMD_ONLINE = 9;// 上线
	public static final int CMD_REPLYONLINE = 10;// 回应上线
	public static final int CMD_CHECK = 11;// 心跳广播
	public static final int CMD_UPDATEINFORMATION=12;//更新信息
	
	public static final int CMD_SENDMSG=13;// 发送信息
	public static final int CMD_STARTTALK=14;// 发送呼叫请求
	public static final int CMD_STOPTALK=15;// 发送结束呼叫请求
	public static final int CMD_ACCEPTTALK=16;// 发送接收呼叫请求
	public static final int CMD_SENDMEDIA=17;//发送录音文件请求
	public static final int CMD_ACCEPTMEDIA=18;//确定请求录音文件	
	public static final int CMD_FILEREQUEST=19;//请求传送文件
	public static final int CMD_FILEACCEPT=20;//接受文件请求
	public static final int CMD_FILEREFUSE=21;//拒绝文件请求
	public static final int CMD_SENDIMAGE=22;//发送图片文件请求
	public static final int CMD_ACCEPTIMAGE=23;//确定请求图片文件
	public static final int CMD_SENDVIDEO=24;//发送视频文件请求
	public static final int CMD_ACCEPTVIDEO=25;//确定请求视频文件	
	public static final int CMD_FINISHTRANPORT=30;//完成文件接收
	
	
	public static final int PORT_MESSAGE_SEND=2426;// 消息发送端口
	public static final int PORT_MESSAGE_RECEIVE=2425;// 消息接收端口
	public static final int PORT_AUDIO_RECEIVE = 5761;//语音接收端口
	public static final int PORT_MEDIA_RECEIVE=2222;// 多媒体文件接收端口
	
	public static final int SHOW=8000;//显示消息
	public static final int FLUSH=8001;//刷新界面
	public static final int ADDUSER=8002;//添加用户
	public static final int DESTROYUSER=8003;//删除用户
	public static final int RECEIVEMSG=8004;//删除消息
	public static final int REFLESHCOUNT=8005;//更新计数
	
	public static final int TransportFile = 6997;
	public static final int TransportImage = 6998;
	public static final int TransportVedio = 6999;
	public static int TransportContent = Tools.TransportFile;//显示当前传输的内容
	public static String chooseFilePath;
	public static String chooseImagePath;
	public static String chooseVedioPath;
	
	public static final int ChatFragment = 7998;
	public static final int ChatDetailActivity = 7999;
	public static int State = Tools.ChatFragment;//状态，显示当前活跃activity
	
	public static ChatFragment chatFragment=null;
	public static ChatDetailActivity chatDetailActivity=null;
	
	public static String fileSavePath = FileUtil.getCachePath();
	public static String imageSavePath = FileUtil.getCacheImagePath();
	public static double sendProgress = -1;// 每次读写文件的字节数
	public static final int FILE_JINDU=2001;//进度命令
	public static final int PROGRESS_FLUSH=2002;//更新进度
	public static final int PROGRESS_COL=2003;//关闭进度条
	
	//消息缓存
	public static Map<String,List<Msg>> msgContainer = new HashMap<String,List<Msg>>();
	public static long pretime=0;
	public static boolean stoptalk = false; //发送请求停止说话
	public static Audio audio=new Audio();  //语音监听线程
	
	public static User me=null;
	public static int[] headIconIds = {
		    R.drawable.face0,
			R.drawable.face1,
			R.drawable.face2,
			R.drawable.face3,
			R.drawable.face4,
			R.drawable.face5,
			R.drawable.face6,
			R.drawable.face7,
			R.drawable.face8,
			R.drawable.face9,
			R.drawable.face10,
			R.drawable.face11,
			R.drawable.face12,
			R.drawable.face13,
			R.drawable.face14,
			R.drawable.face15};
	
	// 发送消息
	public void sendMsg(Msg msg)
	{
		(new UdpSend(msg)).start();
	}
	// 发送消息线程
	class UdpSend extends Thread {
		Msg msg=null;
		UdpSend(Msg msg) {
			this.msg=msg;
		}

		public void run() {
			if (msg.getMsgType() == CMD_SENDMSG || msg.getMsgType() == CMD_SENDMEDIA || msg.getMsgType() == CMD_SENDIMAGE
					|| msg.getMsgType() == CMD_SENDVIDEO || msg.getMsgType() == CMD_FILEREQUEST || msg.getMsgType() == CMD_FILEREFUSE) {
				storeMessage(msg, true); //存储到缓存
			}
			
			try {			
				byte[] data = toByteArray(msg);
				DatagramSocket ds = new DatagramSocket(PORT_MESSAGE_SEND);
				DatagramPacket packet = new DatagramPacket(data, data.length,
						InetAddress.getByName(msg.getReceiveUserIp()), PORT_MESSAGE_RECEIVE);
				packet.setData(data);
				ds.send(packet);
				ds.close();
			} catch (Exception e) {
			}	
		}
	}
	
	// 接收消息
	public void receiveMsg()
	{
		new UdpReceive().start();
	}
	// 接收消息线程
	class UdpReceive extends Thread {
		Msg msg=null;
		UdpReceive() {}

		public void run() {
			//消息循环
			while(true)
			{
				try {
					DatagramSocket ds = new DatagramSocket(Tools.PORT_MESSAGE_RECEIVE);
					byte[] data = new byte[1024 * 4];
					DatagramPacket dp = new DatagramPacket(data, data.length);
					dp.setData(data);
					ds.receive(dp);
					byte[] data2 = new byte[dp.getLength()];
					System.arraycopy(data, 0, data2, 0, data2.length);// 得到接收的数据
					Msg msg = (Msg) Tools.toObject(data2);
					ds.close();
					//解析消息
					parse(msg);
				} catch (Exception e) {
				}
			}

		}
	}
	
	// 开启心跳检查
	public void startCheck()
	{
		new HeartBroadCast().start();
		new CheckUserOnline().start();
	}
	
	// 心跳广播
	class HeartBroadCast extends Thread{
		public void run()
		{
			while(true)
			{
				try {
					sleep(10000);				
				} catch (InterruptedException e) {}
				
				Msg msgBroad=new Msg();
				msgBroad.setSendUser(Tools.me.getName());
				msgBroad.setSendUserIp(Tools.me.getIp());
				msgBroad.setMsgType(Tools.CMD_CHECK);
				msgBroad.setReceiveUserIp(Tools.getBroadCastIP());
				msgBroad.setPackId(Tools.getTimel());
				// 发送消息
				sendMsg(msgBroad);
			}
		}
	}
	// 检测用户是否在线，如果超过15s说明用户已离线，则从列表中清除该用户
	class CheckUserOnline extends Thread {
		@Override
		public void run() {
			while(true) {
				for (int i = 0; i < chatFragment.friendList.size(); i++) {
					User user = chatFragment.friendList.get(i);
					long cm = System.currentTimeMillis() - user.getOnlineTime();
					
					if(cm > 15000){
						//刷新列表													
						ChatTips(Tools.DESTROYUSER, user);
					}
				}
				try {
					sleep(8000);
					//防掉线，广播
					//Tips(Tools.CONSTANTBROAD,null);
				} catch (InterruptedException e) {
				}
			}
		}
	}
	
	// 解析接收的
	public void parse(Msg msg)
	{
		switch (msg.getMsgType()) {
		case Tools.CMD_ONLINE:// 上线
			upline(msg);
			break;
		case Tools.CMD_REPLYONLINE:// 响应上线
			replyUpline(msg);
			break;
		case Tools.CMD_CHECK:// 心跳接收
			updateHeart(msg);
			break;
		case Tools.CMD_SENDMSG:// 接收到对方发送的消息
			receiveMsg(msg);
			break;
		case Tools.CMD_SENDMEDIA://对方发送录音文件请求
			if (!judgeUser(msg)) {// 如果列表无此人
				addUser(msg);// 列表添加此人
			}
			// 接收文件 返回提示接受 建立tcp 服务器 接收文件
			MediaTcpServer ts = new MediaTcpServer(msg);
			ts.start();
			// 发送消息 让对方开始发送文件
			Msg msg0=new Msg(Tools.getTimel(),Tools.me.getHeadIconPos(),Tools.me.getName(), Tools.me.getIp(), msg.getSendUser(), msg.getSendUserIp(),Tools.CMD_ACCEPTMEDIA, msg.getBody());
			sendMsg(msg0);
			break;
		case Tools.CMD_ACCEPTMEDIA:
			//对方已经确定接收,开始发送录音文件
			MediaTcpClient tc = new MediaTcpClient(msg);
			tc.start();
			break;
		case Tools.CMD_STARTTALK: // 语音请求
			Tools.stoptalk=false;
			if(Tools.State==Tools.ChatFragment) {
				ChatTips(Tools.CMD_STARTTALK, msg);
			} else if(Tools.State==Tools.ChatDetailActivity) {
				ChatDetailTips(Tools.CMD_STARTTALK,msg);
			}
			break;
		case Tools.CMD_ACCEPTTALK: // 被叫应答
			//开始通话
			if(!Tools.stoptalk){
				User person=new User(msg.getSendUser(),msg.getSendUserIp());
				Tools.audio.audioSend(person);
			}
			break;
		case Tools.CMD_STOPTALK:// 关闭请求
			Tools.stoptalk=true;
			if(Tools.State==Tools.ChatFragment) {
				ChatTips(Tools.CMD_STOPTALK,msg);
			} else if(Tools.State==Tools.ChatDetailActivity) {
				ChatDetailTips(Tools.CMD_STOPTALK,msg);
			}
			break;
		case Tools.CMD_FILEREQUEST:
			//收到传送文件请求
			if(Tools.State==Tools.ChatFragment) {
				Tools.ChatTips(Tools.CMD_FILEREQUEST, msg);
			} else if (Tools.State==Tools.ChatDetailActivity) {
				Tools.ChatDetailTips(Tools.CMD_FILEREQUEST, msg);
			}
			break;
		case Tools.CMD_FILEACCEPT:
			//收到确认接受		
			String filepath = chooseFilePath;		
			FileTcpClient tc0 = new FileTcpClient(msg, filepath);
			tc0.start();
			Tools.sendProgress=0;
			Tools.ChatDetailTips(Tools.SHOW, "开始发送文件");
			Tools.ChatDetailTips(Tools.FILE_JINDU, msg.getBody());
//			Tools.ChatDetailTips(Tools.CMD_FILEACCEPT, msg);
			break;
		case Tools.CMD_FILEREFUSE:
			//收到拒绝接受			
			Tools.ChatDetailTips(Tools.SHOW, "对方拒绝接受文件");
			receiveMsg(msg);
			break;
		case Tools.CMD_SENDIMAGE: //对方发送图片文件请求
			if (!judgeUser(msg)) {// 如果列表无此人
				addUser(msg);// 列表添加此人
			}
			// 接收文件 返回提示接受 建立tcp 服务器 接收文件
			FileTcpServer fts1 = new FileTcpServer(msg);
			fts1.start();
			// 发送消息让对方开始发送文件
			Msg msg1 = new Msg(Tools.getTimel(), Tools.me.getHeadIconPos(), Tools.me.getName(), Tools.me.getIp(), msg.getSendUser(), msg.getSendUserIp(),Tools.CMD_ACCEPTIMAGE, msg.getBody());
			sendMsg(msg1);
			//如果在detail界面，则显示接收进度
			if (Tools.State==Tools.ChatDetailActivity) {
				Tools.ChatDetailTips(Tools.SHOW, "对方发送图片");
				Tools.sendProgress=0;
				Tools.ChatDetailTips(Tools.FILE_JINDU, msg.getBody());
			}
			break;
		case Tools.CMD_ACCEPTIMAGE:
			//对方已经确定接收,开始发送图片文件
			String imagepath = chooseImagePath;
			FileTcpClient mtc1 = new FileTcpClient(msg, imagepath);
			mtc1.start();
			//显示发送进度
			Tools.sendProgress=0;
			Tools.ChatDetailTips(Tools.SHOW, "开始发送图片");
			Tools.ChatDetailTips(Tools.FILE_JINDU, msg.getBody());
			break;
		case Tools.CMD_SENDVIDEO: //对方发送视频文件请求
			if (!judgeUser(msg)) {// 如果列表无此人
				addUser(msg);// 列表添加此人
			}
			// 接收文件 返回提示接受 建立tcp 服务器 接收文件
			FileTcpServer fts2 = new FileTcpServer(msg);
			fts2.start();
			// 发送消息 让对方开始发送文件
			Msg msg2 = new Msg(Tools.getTimel(), Tools.me.getHeadIconPos(), Tools.me.getName(), Tools.me.getIp(), msg.getSendUser(), msg.getSendUserIp(),Tools.CMD_ACCEPTVIDEO, msg.getBody());
			sendMsg(msg2);
			if (Tools.State==Tools.ChatDetailActivity) {
				Tools.ChatDetailTips(Tools.SHOW, "对方发送视频");
				Tools.sendProgress=0;
				Tools.ChatDetailTips(Tools.FILE_JINDU, msg.getBody());
			}
			break;
		case Tools.CMD_ACCEPTVIDEO:
			//对方已经确定接收,开始发送视频文件
			String vediopath = chooseVedioPath;
			FileTcpClient mtc2 = new FileTcpClient(msg, vediopath);
			mtc2.start();
			Tools.sendProgress=0;
			Tools.ChatDetailTips(Tools.SHOW, "开始发送视频");
			Tools.ChatDetailTips(Tools.FILE_JINDU, msg.getBody());
			break;
		}
	}
	
	// ChatTips-Handler
	public static void ChatTips(int cmd, Object str) {
		Message m = new Message();
		m.what = cmd;
		m.obj = str;
		chatFragment.handler.sendMessage(m);
	}
	
	// ChatTips-Handler
	public static void ChatDetailTips(int cmd, Object str) {
		Message m = new Message();
		m.what = cmd;
		m.obj = str;
		chatDetailActivity.handler.sendMessage(m);
	}
	
	// 接收到上线广播
	public void upline(Msg msg){
		if (!judgeUser(msg)) {// 如果不存在
			ChatTips(Tools.SHOW, msg.getSendUser() + " 上线···");
			addUser(msg);// 添加此人
		}
		// 发送响应上线
		Msg msgsend=new Msg();
		msgsend.setSendUser(me.getName());
		msgsend.setSendUserIp(me.getIp());
		msgsend.setHeadIconPos(me.getHeadIconPos());
		msgsend.setMsgType(CMD_REPLYONLINE);
		msgsend.setReceiveUserIp(msg.getSendUserIp());
		msgsend.setPackId(Tools.getTimel());
		// 发送消息
		sendMsg(msgsend);
	}
	
	// 接收响应上线
	public void replyUpline(Msg msg){
		if (!judgeUser(msg)) {// 如果不存在
			ChatTips(Tools.SHOW,msg.getSendUser() + " 上线···");
			addUser(msg);// 添加此人
		}
	}
	
	// 判断是否有此人 更新
	public boolean judgeUser(Msg msg) {// false 表示不存在
		for (int i = 0; i < chatFragment.friendList.size(); i++) 
		{
			if (chatFragment.friendList.get(i).getIp().equals(msg.getSendUserIp())) 
			{
				// 如果存在 改名字
				if (!chatFragment.friendList.get(i).getName().equals(msg.getSendUser()))
				{
					chatFragment.friendList.get(i).setName(msg.getSendUser());// 该在线列表的名字
					//刷新列表													
					ChatTips(Tools.FLUSH,null);
				}
				if (chatFragment.friendList.get(i).getHeadIconPos()!=msg.getHeadIconPos())
				{
					chatFragment.friendList.get(i).setHeadIconPos(msg.getHeadIconPos());// 该在线列表的头像
					//刷新列表													
					ChatTips(Tools.FLUSH,null);
				}
				return true;
			}
		}
		return false;
	}
	
	// 添加在线用户
	public void addUser(Msg msg) {
		User user = new User(msg.getSendUser(), msg.getHeadIconPos(), msg.getSendUserIp(), 0, System.currentTimeMillis());
		// 在线列表加人
		chatFragment.friendList.add(user);
		//添加用户时，从缓存中读取该用户的以往聊天记录，并放入msgContainer		
		loadMessage(user);
		// 刷新列表
		ChatTips(Tools.FLUSH,null);	
	}
	
	// 接收心跳广播
	public void updateHeart(Msg msg)
	{
		for (int i = 0; i < chatFragment.friendList.size(); i++) 
		{
			if (chatFragment.friendList.get(i).getIp().equals(msg.getSendUserIp())) 
			{
				chatFragment.friendList.get(i).setOnlineTime(System.currentTimeMillis());
			}
		}
	}
	
	// 对象封装成消息
	public static byte[] toByteArray(Object obj) {
		byte[] bytes = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.flush();
			bytes = bos.toByteArray();
			oos.close();
			bos.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return bytes;
	}
	
	// 消息解析成对象
	public static Object toObject(byte[] bytes) {
		Object obj = null;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
			ObjectInputStream ois = new ObjectInputStream(bis);
			obj = ois.readObject();
			ois.close();
			bis.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		return obj;
	}
	
	// 接收消息
	public void receiveMsg(Msg msg)
	{		
		if (!judgeUser(msg)) {// 如果列表无此人
			addUser(msg);// 列表添加此人
		}
		storeMessage(msg, false); //存储到缓存
		
		//如果当前界面是ChatFragment
		if(Tools.State==Tools.ChatFragment) {
			ChatTips(Tools.SHOW, msg.getSendUser() + " 来消息啦！");
			ChatTips(Tools.REFLESHCOUNT, msg.getSendUserIp());
		}
		//如果当前界面是ChatDetailActivity
		if(Tools.State==Tools.ChatDetailActivity) {
			ChatDetailTips(Tools.RECEIVEMSG, msg);
		}
	}
	
	// 取得随机数
	public static String getRandomId()
	{
		return new Random().nextInt(9999)+"";
	}
	
	// 获取当前时间
	public static long getTimel() {
		return (new Date()).getTime();
	}
	
	// 得到广播ip, 192.168.0.255之类的格式
	public static String getBroadCastIP() {
		String ip = getLocalHostIp().substring(0,getLocalHostIp().lastIndexOf(".") + 1) + "255";
		return ip;
	}
	
	// 获取本机IP
	public static String getLocalHostIp() {
		String ipaddress = "";
		try {
			Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
			// 遍历所用的网络接口
			while (en.hasMoreElements()) {
				NetworkInterface nif = en.nextElement();// 得到每一个网络接口绑定的所有ip
				Enumeration<InetAddress> inet = nif.getInetAddresses();
				// 遍历每一个接口绑定的所有ip
				while (inet.hasMoreElements()) {
					InetAddress ip = inet.nextElement();
					if (!ip.isLoopbackAddress() && InetAddressUtils.isIPv4Address(ip.getHostAddress())) {
						return ipaddress = ip.getHostAddress();
					}
				}
			}
		} catch (SocketException e) {
			System.out.print("获取IP失败");
			e.printStackTrace();
		}
		return ipaddress;
	}
	
	// 时间转换
	public static String getChangeTime(long time) {
		//yyyy-MM-dd HH:mm:ss
		if(System.currentTimeMillis()-Tools.pretime<60000) {
			Tools.pretime=time;
			return "";
		} else {
			Tools.pretime=time;
			SimpleDateFormat sfd = new SimpleDateFormat("MM月dd日 HH点mm分");
			return sfd.format(time);
		}
	}

	public static String getTime(long time) {
		if(System.currentTimeMillis() - time > 24*60*60*1000) {
			return "一天前";
		} else {
			SimpleDateFormat sfd = new SimpleDateFormat("HH点mm分");
			return sfd.format(time);
		}
	}
	
	public static void loadMessage(User user) {
		try {
			List<Msg> msgList = new ArrayList<Msg>();
			byte[] bytes = SDCardHelper.loadFileFromSDCard(FileUtil.getCachePath() + File.separator + user.getIp());
			String json = new String(bytes,"UTF-8");
			MsgData msgData = getDataByJson(json);
			msgList.addAll(msgData.msgList);
			
			BaseTools.showlog("ip="+user.getIp());
			for (int i=0;i<msgList.size();i++) {
				BaseTools.showlog((String)msgList.get(i).getBody());
			}
			
			msgContainer.put(user.getIp(), msgList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static MsgData getDataByJson(String json) {
		MsgData data = null;
		try {
			Gson g = new Gson();
			data = g.fromJson(json, MsgData.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}
	
	public static void storeMessage(Msg msg, boolean fromMyself) {
		//这里是个特殊情况，和自己对话时，当拒绝自己发送的文件时，不存储发送的拒绝消息，只存储接收的拒绝消息，否则缓存会有两个拒绝消息
		if (msg.getMsgType() == CMD_FILEREFUSE && chatDetailActivity.friend.getIp().equals(me.getIp()) && fromMyself==true) {
			return;
		}
		//加入消息缓存，以便进入detail可以看到自己的记录
		String storeIp;
		if (fromMyself) {
			storeIp = msg.getReceiveUserIp();  //注意这里是接收方IP
		} else {
			storeIp = msg.getSendUserIp();  //注意这里是发送方IP
		}
		List<Msg> mesList = null;
		if (msgContainer.containsKey(storeIp)) {
			mesList = msgContainer.get(storeIp);
		} else {
			mesList = new ArrayList<Msg>();
		}
		// 加入缓存
		msg.setFromMyself(fromMyself);
		msg.setStoreTime(getTimel()); // 存储时间
		mesList.add(msg);
		msgContainer.put(storeIp, mesList);
	}

}

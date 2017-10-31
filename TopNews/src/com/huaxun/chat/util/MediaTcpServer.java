package com.huaxun.chat.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.huaxun.chat.bean.Msg;
public class MediaTcpServer {
	
	Msg msg=null;
	
	public MediaTcpServer(Msg msg){
		this.msg=msg;
	}

	public void start() {
		server s = new server();
		s.start();
	}

	class server extends Thread {

		public void run() {
			try {
				creatServer();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public void creatServer() throws Exception {
		ServerSocket ss = new ServerSocket(Tools.PORT_MEDIA_RECEIVE);
		Socket s = new Socket();
		s = ss.accept();
		File file = new File(new Media().receivepath + "/" + ((String) msg.getBody()).split(":")[0]);
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		BufferedInputStream is = new BufferedInputStream(s.getInputStream()); // 读进
		BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file));// 写出
		Thread.sleep(1000);
		//int n = 1;
		//long part = Tools.fileSize / 1024*5;// 分成几段
		//long surplus = Tools.fileSize % 1024*5;// 最后剩余多少段
		byte[] data = new byte[1024*5];// 每次读取的字节数
		int len= -1;
		while ((len=is.read(data))!= -1) {
			os.write(data,0,len); 
			//Tools.sendProgress+=len;//进度
		}
		//Tools.sendProgress=-1;	
		is.close();
		os.flush();
		os.close();
		s.close();
		//接收完成
		Tools.storeMessage(msg, false); //存储到缓存
		
		if(Tools.State==Tools.ChatFragment) {
			Tools.ChatTips(Tools.REFLESHCOUNT, msg.getSendUserIp());
		} else if(Tools.State==Tools.ChatDetailActivity) {
			Tools.ChatDetailTips(Tools.CMD_FINISHTRANPORT, msg);
		}
	}

	
}

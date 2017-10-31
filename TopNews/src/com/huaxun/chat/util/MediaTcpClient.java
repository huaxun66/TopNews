package com.huaxun.chat.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;

import com.huaxun.chat.bean.Msg;

public class MediaTcpClient {
	
	Msg msg = null;

	public MediaTcpClient(Msg msg) {
		this.msg = msg;
	}

	public void start() {
		Client c = new Client();
		c.start();
	}

	class Client extends Thread {

		public void run() {
			try {
				creatClient();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void creatClient() throws Exception {
		Socket s = new Socket(msg.getSendUserIp(), Tools.PORT_MEDIA_RECEIVE);
		// 读文件
		File file = new File(new Media().sendpath + "/" + ((String) msg.getBody()).split(":")[0]);
		BufferedInputStream is = new BufferedInputStream(new FileInputStream(file));
		BufferedOutputStream os =new BufferedOutputStream(s.getOutputStream());
		// 读文件
		byte[] data = new byte[1024*5];// 每次读取的字节数
		int len=-1;
		while ((len=is.read(data))!= -1) {
			os.write(data,0,len);
		}
		is.close();
		os.flush();
		os.close();
		//发送消息告诉几经上传完
	}
}

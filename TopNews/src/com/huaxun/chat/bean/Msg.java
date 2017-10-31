package com.huaxun.chat.bean;

import java.io.Serializable;

public class Msg implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private long packId;//时间戳
	private String sendUser;// 发送人 用户名
	private String sendUserIp;// 发送人ip
	private String receiveUser;// 接收人
	private String receiveUserIp;//接收人IP
	private int headIconPos;//头像
	private int MsgType;// 消息类型
	private Object body;// 主体
	private boolean fromMyself; //是否自己发出的消息
	private long storeTime; //加入消息缓存的时间
	
	public Msg (){}
	
	public Msg(long packId, int headIconPos,String sendUser, String sendUserIp, String receiveUser,
			String receiveIp, int msgType, Object body) {
		super();
		this.packId=packId;
		this.headIconPos=headIconPos;
		this.sendUser = sendUser;
		this.sendUserIp = sendUserIp;
		this.receiveUser = receiveUser;
		this.receiveUserIp = receiveIp;
		this.MsgType = msgType;
		this.body = body;
	}
	
	public long getPackId() {
		return packId;
	}
	public void setPackId(long packId) {
		this.packId = packId;
	}
	public String getReceiveUserIp() {
		return receiveUserIp;
	}
	public void setReceiveUserIp(String receiveUserIp) {
		this.receiveUserIp = receiveUserIp;
	}
	public String getSendUser() {
		return sendUser;
	}
	public void setSendUser(String sendUser) {
		this.sendUser = sendUser;
	}
	public String getSendUserIp() {
		return sendUserIp;
	}
	public void setSendUserIp(String sendUserIp) {
		this.sendUserIp = sendUserIp;
	}
	public String getReceiveUser() {
		return receiveUser;
	}
	public void setReceiveUser(String receiveUser) {
		this.receiveUser = receiveUser;
	}
	public int getMsgType() {
		return MsgType;
	}
	public void setMsgType(int msgType) {
		MsgType = msgType;
	}
	public Object getBody() {
		return body;
	}
	public void setBody(Object body) {
		this.body = body;
	}
	public boolean isFromMyself() {
		return fromMyself;
	}
	public void setFromMyself(boolean fromMyself) {
		this.fromMyself = fromMyself;
	}
	public long getStoreTime() {
		return storeTime;
	}
	public void setStoreTime(long storeTime) {
		this.storeTime = storeTime;
	}
	public int getHeadIconPos() {
		return headIconPos;
	}
	public void setHeadIconPos(int headIconPos) {
		this.headIconPos = headIconPos;
	}

}


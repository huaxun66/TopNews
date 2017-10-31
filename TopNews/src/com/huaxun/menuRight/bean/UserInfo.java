package com.huaxun.menuRight.bean;

import java.io.Serializable;

public class UserInfo implements Serializable {
	private static final long serialVersionUID = 1L;
    private String username;
    private String password;
    private String gender;
    private String email;
    private String usericon;    
    private String usericon2;
    private String location;
    private String description;
    private boolean isthirdlogin; //是否是第三方登录
    
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getUsericon() {
		return usericon;
	}
	public void setUsericon(String usericon) {
		this.usericon = usericon;
	}
	public String getUsericon2() {
		return usericon2;
	}
	public void setUsericon2(String usericon2) {
		this.usericon2 = usericon2;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public boolean getIsThirdLogin() {
		return isthirdlogin;
	}
	
	public void setIsThirdLogin(boolean isthirdlogin) {
		this.isthirdlogin = isthirdlogin;
	}
	
	public UserInfo(String username, String usericon, String usericon2, String gender, String location, String description, boolean isthirdlogin){
		this.username = username;
		this.usericon = usericon;
		this.usericon2 = usericon2;
		this.gender = gender;
		this.location = location;
		this.description = description;
		this.isthirdlogin = isthirdlogin;
	}
	
	
    
}

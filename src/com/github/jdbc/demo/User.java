package com.github.jdbc.demo;

import java.util.Date;

public class User {

	private String userName;
	private String password;
	private Date createdTime;
	private Double balance;
	
	public User(){}
	
	public User(String userName, String password, Double balance){
		this.userName = userName;
		this.password = password;
		this.balance = balance;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Date getCreatedTime() {
		return createdTime;
	}
	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}
	public Double getBalance() {
		return balance;
	}
	public void setBalance(Double balance) {
		this.balance = balance;
	}

	@Override
	public String toString() {
		return "User [userName=" + userName + ", password=" + password + ", createdTime=" + createdTime + ", balance="
				+ balance + "]";
	}
	
}

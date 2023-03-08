package com.example.ChatBotElise.model;

import java.sql.Timestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity(name = "usersDataTable")
public class User {
	
	@Override
	public String toString() {
		return "User [chatId=" + chatId + ", firstName=" + firstName + ", lastName=" + lastName + ", userName="
				+ userName + ", registeredAt=" + registeredAt + "]";
	}
	public Long getChatId() {
		return chatId;
	}
	public void setChatId(Long chatId) {
		this.chatId = chatId;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public Timestamp getRegisteredAt() {
		return registeredAt;
	}
	public void setRegisteredAt(Timestamp registeredAt) {
		this.registeredAt = registeredAt;
	}
	@Id	
	private Long chatId;	
	private String firstName;
	private String lastName;
	private String userName;
	private Timestamp registeredAt;
	
}

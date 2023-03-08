package com.example.ChatBotElise.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity(name = "adsTable")
public class Ads {
	
	@Override
	public String toString() {
		return "Ads [Id=" + Id + ", Ad=" + Ad + "]";
	}
	public Long getId() {
		return Id;
	}
	public void setId(Long id) {
		Id = id;
	}
	public String getAd() {
		return Ad;
	}
	public void setAd(String ad) {
		Ad = ad;
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)		
	private Long Id;	
	private String Ad;
}

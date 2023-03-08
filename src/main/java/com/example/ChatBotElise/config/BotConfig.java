package com.example.ChatBotElise.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("application.properties")
public class BotConfig {

    @Value("${bot.name}")
    String botName;

    @Value("${bot.token}")
    String token;
    
    @Value("${bot.owner}")
    Long ownerId;

	public String getToken() {
		return token;
	}

	public String getBotName() {		
		return botName;
	}
	
	public Long getOwnerId() {		
		return ownerId;
	}
}

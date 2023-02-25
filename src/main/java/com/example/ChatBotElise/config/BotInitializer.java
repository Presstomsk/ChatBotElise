package com.example.ChatBotElise.config;

import com.example.ChatBotElise.service.TelegramBot;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;


@Component
public class BotInitializer {

    @Autowired
    TelegramBot bot;
    
    private static Logger logger = Logger.getLogger(BotInitializer.class.getName());

    @EventListener({ContextRefreshedEvent.class})
    public void init() throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            telegramBotsApi.registerBot(bot);
            try {
				FileHandler handler = new FileHandler("logs.json");
				logger.addHandler(handler);				
			} catch (SecurityException e) {				
				logger.log(Level.SEVERE,e.getMessage());
			} catch (IOException e) {				
				logger.log(Level.SEVERE,e.getMessage());
			}
        }
        catch (TelegramApiException e) {  
        	logger.log(Level.SEVERE,e.getMessage());
        }
    }
}

package com.example.ChatBotElise.service;

import com.example.ChatBotElise.config.BotConfig;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


@Component
public class TelegramBot extends TelegramLongPollingBot{

    final BotConfig config;
    private static Logger logger = Logger.getLogger(TelegramBot.class.getName());
    private static FileHandler handler; 

    public TelegramBot(BotConfig config) {
        this.config = config;
        try {
			this.handler = new FileHandler("logs.json");
			logger.addHandler(handler);		
		} catch (SecurityException e) {				
			logger.log(Level.SEVERE,e.getMessage());
		} catch (IOException e) {				
			logger.log(Level.SEVERE,e.getMessage());
		}
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {

        if(update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();         

            switch (messageText) {
                case "/start":
                        startCommandReceived(chatId, update.getMessage().getChat().getFirstName());	
                        logger.info("Pressed start by " + update.getMessage().getChat().getFirstName());
                        break;
                default:

                        sendMessage(chatId, "Sorry, command was not recognized");

            }
        }



    }

    private void startCommandReceived(long chatId, String name){


        String answer = "Hi, " + name +", nice to meet you!";


        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend)  {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try{
            execute(message);
        }
        catch (TelegramApiException e) {       	
        	logger.log(Level.SEVERE,e.getMessage());
        }
    }
}

package com.example.ChatBotElise.service;

import com.example.ChatBotElise.config.BotConfig;
import com.example.ChatBotElise.model.User;
import com.example.ChatBotElise.model.UserRepository;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


@Component
public class TelegramBot extends TelegramLongPollingBot{

    private static final String HELP_TEXT = "This bot created to demonstrate Spring\n\n" +
                                             "You can execute commands from main menu!\n\n" +                                             
                                             "Type /start to see a welcome message\n\n" +
                                             "Type /mydata to see data stored about yourself\n\n" +
                                             "Type /help to see this message again";
	
    @Autowired
    private UserRepository userRepository;
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
        
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "get a welcome message"));
        listOfCommands.add(new BotCommand("/mydata", "get your data stored"));
        listOfCommands.add(new BotCommand("/deletedata", "delete my data"));
        listOfCommands.add(new BotCommand("/help", "info about this bot"));
        listOfCommands.add(new BotCommand("/settings", "set your preferences"));
        try {
        	this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch(TelegramApiException ex) {
        	logger.log(Level.SEVERE,ex.getMessage());
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
                	registerUser(update.getMessage());
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());	
                    logger.info("Pressed start by " + update.getMessage().getChat().getFirstName());
                    break;
                
                case "/help":
                	sendMessage(chatId, HELP_TEXT); 
                	logger.info("Pressed help by " + update.getMessage().getChat().getFirstName());   
                	break;
                default:

                    sendMessage(chatId, "Sorry, command was not recognized");

            }
        }



    }

    private void registerUser(Message message) {		
		
    	if(userRepository.findById(message.getChatId()).isEmpty()) {
    		
    		Long chatId = message.getChatId();
    		Chat chat = message.getChat();
    		
    		User user = new User();
    		user.setChatId(chatId);
    		user.setFirstName(chat.getFirstName());
    		user.setLastName(chat.getLastName());
    		user.setUserName(chat.getUserName());
    		user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));
    		
    		userRepository.save(user);
    		
    		logger.info("User saved: " + user);
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

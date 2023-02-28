package com.example.ChatBotElise.service;

import com.example.ChatBotElise.config.BotConfig;
import com.example.ChatBotElise.model.User;
import com.example.ChatBotElise.model.UserRepository;
import com.vdurmont.emoji.EmojiParser;

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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
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
                case "start":
                	registerUser(update.getMessage());
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());	
                    logger.info("Pressed start by " + update.getMessage().getChat().getFirstName());
                    break;
                case "/mydata":
                	getUserData(chatId);
                	break; 
                case "get my data":
                	getUserData(chatId);
                	break; 
                case "/deletedata":
                	deleteUserData(chatId);
                	break;
                case "delete my data":
                	deleteUserData(chatId);
                	break;
                case "/help":
                	sendMessage(chatId, HELP_TEXT); 
                	logger.info("Pressed help by " + update.getMessage().getChat().getFirstName());   
                	break;
                case "help":
                	sendMessage(chatId, HELP_TEXT); 
                	logger.info("Pressed help by " + update.getMessage().getChat().getFirstName());   
                	break;
                case "settings":
                	sendMessage(chatId, "Sorry, command was not recognized");  
                	break;
                default:

                    sendMessage(chatId, "Sorry, command was not recognized");

            }
        }



    }

    private void deleteUserData(long chatId) {
    	if(userRepository.findById(chatId).isPresent()) {
        	
    		User user = userRepository.findById(chatId).get();
    		userRepository.delete(user);
    		sendMessage(chatId, "Information about your account deleted from database!");
    		
    		logger.info("Delete user information: " + user);
    	}	
    	else {
    		sendMessage(chatId, "Database hasn't information about your account!");
    	}
	}

	private void getUserData(long chatId) {
    	if(userRepository.findById(chatId).isPresent()) {
    	
    		User user = userRepository.findById(chatId).get();
    		
    		String answer = "Information about me:\n\n" +
                    "ChatId: " + user.getChatId() + "\n\n" +                                             
                    "First Name: " + user.getFirstName() + "\n\n" +  
    		        "Last Name: " + user.getLastName() + "\n\n" + 
    		        "User Name: " + user.getUserName() + "\n\n" +
    		        "Registered At: " + user.getRegisteredAt() + "\n\n";

    		sendMessage(chatId, answer);
    		
    		logger.info("Get user information: " + user);
    	}
    	else {
    		sendMessage(chatId, "Database hasn't information about your account!");
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

        String answer = EmojiParser.parseToUnicode("Hi, " + name +", nice to meet you!" + " :blush:");
        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend)  {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("start");
        row.add("get my data");    
        row.add("delete my data");   
        row.add("help");
        row.add("settings");
        keyboardRows.add(row);
        keyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(keyboardMarkup);

        try{
            execute(message);
        }
        catch (TelegramApiException e) {       	
        	logger.log(Level.SEVERE,e.getMessage());
        }
    }
}

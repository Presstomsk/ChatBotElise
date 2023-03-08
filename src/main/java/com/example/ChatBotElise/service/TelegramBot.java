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
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
//import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
//import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


@Component
public class TelegramBot extends TelegramLongPollingBot{

    private static final String HELP_TEXT = "This bot created to demonstrate Spring\n\n" +
                                             "You can execute commands from main menu!\n\n" +                                             
                                             "Type /start to see a welcome message\n\n" +
                                             "Type /mydata to see data stored about yourself\n\n" +
                                             "Type /help to see this message again";
    private static final String YES = "YES_REGISTRATION";
    private static final String NO = "NO_REGISTRATION";
    private static final String DB_NO_RESULT = "Database hasn't information about your account!";
    private static final String TO_SEND_MSG = "Sended message to user : ";
	
    @Autowired
    private UserRepository userRepository;
    final BotConfig config;    
    private static Logger logger = Logger.getLogger(TelegramBot.class.getName());
    private static FileHandler handler; 

    public TelegramBot(BotConfig config) {
        this.config = config;       
        try {
			TelegramBot.handler = new FileHandler("logs.json");
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
            
            if (messageText.contains("/send") && config.getOwnerId() == chatId) {
            	logger.info(getText("send") + update.getMessage().getChat().getFirstName());
            	var textToSend = EmojiParser.parseToUnicode(messageText.substring(messageText.indexOf(" ")));
            	var users = userRepository.findAll();
            	for(User user : users) {
            		sendMessage(user.getChatId(), textToSend); 
            	}
            }
            else {           
            	 switch (messageText) {
                 case "/start":
                 	logger.info(getText("start") + update.getMessage().getChat().getFirstName());
                 	startCommandReceived(chatId, update.getMessage().getChat().getFirstName());	
                 	registerUser(update.getMessage());               
                     break;                
                 case "/mydata":
                 	logger.info(getText("mydata") + update.getMessage().getChat().getFirstName());
                 	getUserData(chatId);                	
                 	break;                
                 case "/deletedata":
                 	logger.info(getText("deletedata") + update.getMessage().getChat().getFirstName());
                 	deleteUserData(chatId);                	
                 	break;                
                 case "/help":
                 	logger.info(getText("help") + update.getMessage().getChat().getFirstName());
                 	sendMessage(chatId, HELP_TEXT);                 	   
                 	break;                 	
                 default:
                 	logger.info(getText("unknown command") + update.getMessage().getChat().getFirstName());
                    sendMessage(chatId, "Sorry, command was not recognized");  
                 }
            }
        }
        else if(update.hasCallbackQuery()) {
        	String callbackData = update.getCallbackQuery().getData();
        	long chatId = update.getCallbackQuery().getMessage().getChatId();   
        	Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        	Chat chat = update.getCallbackQuery().getMessage().getChat();
        	
        	if (callbackData.equals(YES)) {
        		logger.info("Pressed " + YES);         		
        		executeYesRegistrationLogic(chatId, messageId, chat);
        	}
        	else if (callbackData.equals(NO)) {
        		logger.info("Pressed " + NO);
        		executeNoRegistrationLogic(chatId, messageId, chat);
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
    		sendMessage(chatId, DB_NO_RESULT);
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
    		sendMessage(chatId, DB_NO_RESULT);
    	}
	}

	private void registerUser(Message message) {		
		
		Long chatId = message.getChatId();			
    	if(userRepository.findById(message.getChatId()).isEmpty()) {   		
    		SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(chatId));
            msg.setText("Do you want to register?");
    		
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup(); 
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();
            List<InlineKeyboardButton> row = new ArrayList<>();
            var yesButton = new InlineKeyboardButton();
            yesButton.setText("Yes");
            yesButton.setCallbackData(YES);
            var noButton = new InlineKeyboardButton();
            noButton.setText("No");
            noButton.setCallbackData(NO);
            row.add(yesButton);
            row.add(noButton);
            rows.add(row);
            markup.setKeyboard(rows);
            
            msg.setReplyMarkup(markup);  
            
            executeSendMessage(msg, msg.getText());    		
    	}    	
	}
	
	private void registration(long chatId, Chat chat){	
		
		User user = new User();
		user.setChatId(chatId);
		user.setFirstName(chat.getFirstName());
		user.setLastName(chat.getLastName());
		user.setUserName(chat.getUserName());
		user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));
		userRepository.save(user);		
		logger.info("User saved: " + user);        	
    }

	private void startCommandReceived(long chatId, String name){

        String answer = EmojiParser.parseToUnicode("Hi, " + name +", nice to meet you!" + " :blush:");
        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        executeSendMessage(message, textToSend);
    }
    
    private void executeYesRegistrationLogic(long chatId, Integer messageId, Chat chat){
    	String text = "You are registered!";        		
		EditMessageText message = new EditMessageText();
		message.setChatId(String.valueOf(chatId));
		message.setText(text);
		message.setMessageId(messageId);
		
		try{
		    registration(chatId, chat);          
        }       
		catch (Exception e) {       	
        	logger.log(Level.SEVERE,e.getMessage());
        }	
		executeSendEditMessage(message, text);
    }
    
    private void executeNoRegistrationLogic(long chatId, Integer messageId, Chat chat){
    	String text = "You canceled registration";         		
		EditMessageText message = new EditMessageText();
		message.setChatId(String.valueOf(chatId));
		message.setText(text);
		message.setMessageId(messageId);
		executeSendEditMessage(message, text);
    }
    
    private void executeSendMessage(SendMessage message, String text){
    	try{
            execute(message);
            logger.info(TO_SEND_MSG + text);
        }
        catch (TelegramApiException e) {       	
        	logger.log(Level.SEVERE,e.getMessage());
        } 
    }
    
    private void executeSendEditMessage(EditMessageText message, String text){
    	try{
            execute(message);
            logger.info(TO_SEND_MSG + text);
        }
        catch (TelegramApiException e) {       	
        	logger.log(Level.SEVERE,e.getMessage());
        }  
    }
    
    private String getText(String str){
    	return "Pressed " + str + " by ";
    }
}

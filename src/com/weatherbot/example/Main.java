package com.weatherbot.example;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;
import org.telegram.telegrambots.logging.BotsFileHandler;

import com.pengrad.telegrambot.TelegramBot;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

/**
 * This is a telegram bot to notify users on new weather warnings. Data is from a RSS provided by HK Government.
 * User could command some specific command to get info.
 * @version 1.0
 * @author Xiujun Yang
 * @date 19th Dec 2016
 */
public class Main {
    private static final String LOGTAG = "MAIN";

    public static void main(String[] args){
        BotLogger.setLevel(Level.ALL);
        BotLogger.registerLogger(new ConsoleHandler());
        try {
            BotLogger.registerLogger(new BotsFileHandler());
        } catch (IOException e) {
            BotLogger.severe(LOGTAG, e);
        }
        try {
            ApiContextInitializer.init();
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
            try{
                telegramBotsApi.registerBot(new com.weatherbot.example.WeatherHandlers());
                //BotLogger.info(LOGTAG,"Success to register!");
            } catch (TelegramApiException e) {
                BotLogger.error(LOGTAG, e);
            }
            
        } catch (Exception e) {
            BotLogger.error(LOGTAG, e);
        }  
    }
    
}

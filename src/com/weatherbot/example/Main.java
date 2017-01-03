package com.weatherbot.example;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;
import org.telegram.telegrambots.logging.BotsFileHandler;


import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

/**
 * This is a telegram bot to notify users on new weather warnings. Data is from a RSS provided by HK Government.
 * User could command some specific command to get info.
 * @version 1.1
 * @author Xiujun Yang
 * @date 23th Dec 2016
 */
public class Main {
    private static final String LOGTAG = "MAIN";

    public static void main(String[] args){
        // TO load subscriber from database first while run this application.
        MyConnection dbConn = MyConnection.getInstance();
        dbConn.init();
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
                telegramBotsApi.registerBot(WeatherHandlers.getInstance());
            } catch (TelegramApiException e) {
                e.printStackTrace();
                BotLogger.error(LOGTAG, e);
            }
            
        } catch (Exception e) {
            BotLogger.error(LOGTAG, e);
    }
        new WeatherWarningFeeder();
    }
}

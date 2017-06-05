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
 * Create TelegramAPI, create bothandler(extends TelegramLongPollingBot), registerBot.
 * There're two ways to get message from TelegramBot, First one is used by webhook, 
 * the other one is polled updates by a thread. (by GetUpdates method)
 * @version 1.1
 * @author Xiujun Yang
 * @date 23th Dec 2016
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
    
    /*
     private static TelegramBotsApi createTelegramBotsApi() throws TelegramApiException {
        TelegramBotsApi telegramBotsApi;
        if (!BuildVars.useWebHook) {
            telegramBotsApi = createLongPollingTelegramBotsApi();
        } else if (!BuildVars.pathToCertificatePublicKey.isEmpty()) {
            // Filled a path to a pem file ? looks like you're going for the self signed option then, invoke with store and pem file to supply.
            telegramBotsApi = createSelfSignedTelegramBotsApi();
            telegramBotsApi.registerBot(new weatherBotHandler());
        } else {
            // Non self signed, make sure you've added private/public and if needed intermediate to your cert-store.
            telegramBotsApi = createNoSelfSignedTelegramBotsApi();
            telegramBotsApi.registerBot(new weatherBotHandler());
        }
        return telegramBotsApi;
    }

 
    private static TelegramBotsApi createLongPollingTelegramBotsApi() {
        return new TelegramBotsApi();
    }


    private static TelegramBotsApi createSelfSignedTelegramBotsApi() throws TelegramApiException {
        return new TelegramBotsApi(BuildVars.pathToCertificateStore, BuildVars.certificateStorePassword, BuildVars.EXTERNALWEBHOOKURL, BuildVars.INTERNALWEBHOOKURL, BuildVars.pathToCertificatePublicKey);
    }

    private static TelegramBotsApi createNoSelfSignedTelegramBotsApi() throws TelegramApiException {
        return new TelegramBotsApi(BuildVars.pathToCertificateStore, BuildVars.certificateStorePassword, BuildVars.EXTERNALWEBHOOKURL, BuildVars.INTERNALWEBHOOKURL);
    }*/
}

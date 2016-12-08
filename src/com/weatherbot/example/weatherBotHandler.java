package com.weatherbot.example;

import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.logging.BotLogger;


public class weatherBotHandler extends TelegramWebhookBot{
    private static final String LOGTAG = "weatherBotHandler";
    
    @Override
    public BotApiMethod onWebhookUpdateReceived(Update update) {
        System.out.println("receive msg");
        if (update.hasMessage() && update.getMessage().hasText()) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(update.getMessage().getChatId().toString());
            sendMessage.setText("Well, all information looks like noise until you break the code.");
            return sendMessage;
        }
        BotLogger.debug(LOGTAG, "Fail");
        return null;
    }

    @Override
    public String getBotUsername() {
        return "webhooksamplebot";
    }

    @Override
    public String getBotToken() {
        return "<token>";
    }

    @Override
    public String getBotPath() {
        return "webhooksamplebot";
    }
}

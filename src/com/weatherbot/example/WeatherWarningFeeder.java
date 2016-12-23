package com.weatherbot.example;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.exceptions.TelegramApiException;


/**
 * Get Rss and send to subscriber in a period.
 * @author Xiujun Yang
 * @Date 23th Dec 2016
 */
public class WeatherWarningFeeder{
    WeatherHandlers handler = WeatherHandlers.getInstance();

    public WeatherWarningFeeder(){
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new FeederTask(),0, 108000000);//30 mins = 108000 sec
    }


    class FeederTask extends TimerTask{
    
        public void run() {
            ArrayList<Subscriber> subscriberList = handler.db.getSubscribersList();
            Iterator<Subscriber> it = subscriberList.iterator();
            while(it.hasNext()){
                Subscriber element = it.next();
                SendMessage sendmsg = new SendMessage();
                sendmsg.setChatId(element.getChatId());
                sendmsg.setText(handler.getXmlParsedInfo(BotConfig.WEATHER_WARNING_SUMMARY_EN));
                try {
                    handler.sendMessage(sendmsg);
                } catch (TelegramApiException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}
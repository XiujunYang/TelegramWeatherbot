package com.weatherbot.example;

/*import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;*/
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;


/**
 * Get Rss and send to subscriber in a period.
 * Trigger 30mins to get rss information and push to subscriber.
 * Send out warning to user who in the list.
 * @author Xiujun Yang
 * @Date 23th Dec 2016
 */
public class WeatherWarningFeeder{
    WeatherHandlers handler = WeatherHandlers.getInstance();

    public WeatherWarningFeeder(){
        Timer timer = new Timer();
        /* Program will use same thread to run FeederTask.
         * Run on different thread depended on Timer, not TimerTask */
        timer.scheduleAtFixedRate(new FeederTask(),0, 108000000);//30 mins = 108000 sec
    }

    class FeederTask extends TimerTask{
        private final String LOGTAG = "FeederTask";
        
        public void run() {
            BotLogger.debug(LOGTAG,"ThreadId:"+Thread.currentThread().getId());
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
            
            /*       
            //By HTTPGET to obtain rss content.
            URL obj;
            try {
                obj = new URL(BotConfig.WEATHER_WARNING_SUMMARY_EN);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                // optional default is GET
                con.setRequestMethod("GET");

                //add request header
                con.setRequestProperty("User-Agent", "Mozilla/5.0");

            int responseCode = con.getResponseCode();
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
       
            //print result
            System.out.println(response.toString());
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }*/
        }
    }
}
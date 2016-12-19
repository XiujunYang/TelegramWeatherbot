package com.weatherbot.example;

import com.weatherbot.example.BotConfig;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Receive bot message, and handle command to reply information by RSS.
 * @author Xiujun Yang
 * @version 1.0
 * @date 19th Dec 2016
 */
public class WeatherHandlers extends TelegramLongPollingBot {
    private static final String LOGTAG = "WEATHERHANDLERS";

    private final int NOT_A_COMMAND = -1;
    private final int COMMAND_DESCRIPTION = 0;
    private final int COMMAND_SUBSCRIBE = 1;
    private final int COMMAND_UNSUBSCRIBE = 2;
    private final int COMMAND_TELLMECURRENT = 3;
    private final int COMMAND_TELLMEWARNING = 4;
    private final int COMMAND_LANGUAGE_CHANGE_TO_EN = 5;
    private final int COMMAND_LANGUAGE_CHANGE_TO_CN = 6;
    
    static {
        MyConnection dbConn = new MyConnection();
    }

    public WeatherHandlers() {
        super();
        //startAlertTimers();
    }

    @Override
    public String getBotToken() {
        return BotConfig.WEATHER_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                Message message = update.getMessage();
                if (message.hasText() || message.hasLocation()) {
                    BotLogger.info(LOGTAG,"chatId["+message.getChatId()+"], "+"messageId["+message.getMessageId()+"] : "+message.getText());
                    handleIncomingMessage(message);
                }
            }
        } catch (Exception e) {
            BotLogger.error(LOGTAG, e);
        }
    }

    @Override
    public String getBotUsername() {
        return null;
    }

    
    /**
     * Check if it's a command.
     * @param str command by bot message
     * @return NOT_A_COMMAND,COMMAND_DESCRIPTION,COMMAND_TELLMECURRENT,COMMAND_TELLMEWARNING,
     *          COMMAND_SUBSCRIBE,COMMAND_UNSUBSCRIBE,COMMAND_LANGUAGE_CHANGE_TO_CN,COMMAND_LANGUAGE_CHANGE_TO_EN.
     */
    private int isCommand(String str){
        String command = new String(str).trim().toLowerCase();
        if(command.contentEquals("topics")) {
            return COMMAND_DESCRIPTION;
        } else if(command.contentEquals("tellme current")) {
            return COMMAND_TELLMECURRENT;
        } else if(command.contentEquals("tellme warning")) {
            return COMMAND_TELLMEWARNING;
        } else if(command.startsWith("subscribe")){
            return COMMAND_SUBSCRIBE;
        } else if(command.startsWith("unsubscribe")){
            return COMMAND_UNSUBSCRIBE;
        } else if(command.contentEquals("ÁπÅÈ?‰∏≠Ê?")){
            return COMMAND_LANGUAGE_CHANGE_TO_CN;
        } else if(command.contentEquals("English")){
            return COMMAND_LANGUAGE_CHANGE_TO_EN;
        }
        return NOT_A_COMMAND;
    }
    
    /**
     * Incoming messages handlers
     * @param message
     * @throws TelegramApiException
     */
    private void handleIncomingMessage(Message message) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.enableMarkdown(true); 
        //BotLogger.info(LOGTAG,"Jean_"+message.getFrom().toString());
        String replyStr;
        if (message.isUserMessage() && message.hasText()) {
            //BotLogger.info(LOGTAG,isCommand(message.getText().toString()));
            switch(isCommand(message.getText())) {
                case COMMAND_DESCRIPTION:
                    //command:topics
                    sendMessage.setText("current, warning");
                    BotLogger.info(LOGTAG,"COMMAND_DESCRIPTION REPLY:current, warning");
                    break;
                case COMMAND_TELLMECURRENT:
                    //command:tellme current
                    replyStr = getXmlParsedInfo(BotConfig.CURRENT_WEATHER_REPORT_EN);
                    sendMessage.setText(replyStr);
                    BotLogger.info(LOGTAG,"COMMAND_DESCRIPTION REPLY:"+replyStr);
                    break;
                case COMMAND_TELLMEWARNING:
                    //command:tellme warning
                    /* Use to choose Language before replying.
                     * https://core.telegram.org/bots/api/#keyboardbutton
                    Array<KeyboardButton> btnList = new Array<KeyboardButton>(3);
                    KeyboardButton btnEN = new KeyboardButton("English");
                    KeyboardButton btnTC = new KeyboardButton("ÁπÅÈ?‰∏≠Ê?");
                    KeyboardButton btnSC = new KeyboardButton("Á∞°È?‰∏≠Ê?");
                    btnEN.setRequestLocation(true);
                    */
                    replyStr = getXmlParsedInfo(BotConfig.WEATHER_WARNING_SUMMARY_EN);
                    sendMessage.setText(replyStr);
                    BotLogger.info(LOGTAG,"COMMAND_TELLMEWARNING REPLY:"+replyStr);
                    break;
                case COMMAND_SUBSCRIBE:
                    //command:subscribe warning
                    sendMessage.setText("COMMAND_SUBSCRIBE");
                    break;
                case COMMAND_UNSUBSCRIBE:
                    //command:unsubscribe warning
                    sendMessage.setText("COMMAND_UNSUBSCRIBE");
                    break;
                case COMMAND_LANGUAGE_CHANGE_TO_EN:
                    //command:English
                    sendMessage.setText("OK");
                    break;
                case COMMAND_LANGUAGE_CHANGE_TO_CN:
                    //command:ÁπÅÈ?‰∏≠Ê?/ÁÆÄ‰Ωì‰∏≠??Traditional Chinese/Simplified Chinese)
                    sendMessage.setText("?•È?‰∫?);
                    break;
                default:
                    sendMessage.setText("Please use Following keyword: 1.topics; 2.subscribe/unsubscribe; 3.tellme");
                    BotLogger.info(LOGTAG,"REPLY:default");
            }
            sendMessage(sendMessage);
        }
    }
    
    /**
     * Get target information by RSS.
     * @param rssUrl Here use xml type's RSS
     * @return a string reply to telegram weather bot
     */
    private String getXmlParsedInfo(String rssUrl) {
        String sourceCode = null;
        try{
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(rssUrl);
            
            NodeList nList = doc.getElementsByTagName("item");
            
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                //System.out.println("\nCurrent Element :" + nNode.getNodeName());
            
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    sourceCode = eElement.getElementsByTagName("description").item(0).getTextContent();
                    //System.out.println(eElement.getElementsByTagName("description").item(0).getTextContent());
              }
            }
            
            if(sourceCode.contains("<![CDATA["))
                sourceCode = sourceCode.replace("<![CDATA[", "");
            sourceCode = sourceCode.replaceAll("<p>|</p>", "");
            sourceCode = sourceCode.replaceAll("<br/>", "\n");
            NodeList LanguageList = doc.getElementsByTagName("language");
            Element e = (Element) LanguageList.item(0);
            sourceCode += "\n Language:"+e.getTextContent();
            return sourceCode;
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Parse row data by RSS
     * @return a string reply to telegram weather bot
     */
    private String getCurrentWeatherByParsingRawData() {
        String line=null;
        String sourceCode = null;
        boolean beginning = false;
        try{
            URL rssUrl = new URL(BotConfig.CURRENT_WEATHER_REPORT_EN);
            BufferedReader in = new BufferedReader(new InputStreamReader(rssUrl.openStream()));
            while((line = in.readLine())!=null){
                //System.out.println(line);
                line = line.trim();
                if (line.contains("<![CDATA[")){
                    beginning= true;
                }
                if (beginning == true){
                    if(line.contains("<img")){
                        continue;
                    } else if(line.contains("<table")){
                        break;
                    } else {
                        if (sourceCode == null) sourceCode = line;
                        else sourceCode = sourceCode+line;
                    }
                }
            }
            
            if(sourceCode.contains("<![CDATA["))
                sourceCode = sourceCode.replace("<![CDATA[", "");
            sourceCode = sourceCode.replaceAll("<p>|</p>", "");
            sourceCode = sourceCode.replaceAll("<br/>", "\n");
            in.close();
            return sourceCode;
        } catch(MalformedURLException ue){
            return null;
        } catch(IOException ioe){
            return null;
        }
    }
    
}
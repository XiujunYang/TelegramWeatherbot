package com.weatherbot.example;


/**
 * User/Group object.
 * @author Xiujun Yang
 * @Date 23th Dec 2016
 */
public class Subscriber {
    private long chatId;
    private String userName;
    private String firstName;
    private String lastName;
    private boolean isGroup;
    
    // For private user subscriber
    public Subscriber(long chatId, String userName, String firstName, String lastName){
        this.chatId = chatId;
        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isGroup = false;
    }
    
    // For group subscriber
    public Subscriber(long chatId){
        this.chatId = chatId;
        this.isGroup = true;
    }
    
    public long getChatId(){
        return chatId;
    }
    
    public String getUserName(){
        return userName;
    }
    
    public String getFirstName(){
        return firstName;
    }
    
    public String getLastName(){
        return lastName;
    }
    
    public boolean isGroup(){
        return isGroup;
    }
    
    @Override
    // This could overwrite ArrarList.contains() and ArrarList.containsAll()
    public boolean equals(Object obj) {
        boolean retVal = false;
        if (obj instanceof Subscriber)
            retVal = ((Subscriber) obj).getChatId() == this.chatId;
     return retVal;
  }
    
    @Override
    public String toString(){
        String returnStr = "Id["+chatId+"]; " + (isGroup?"Group-user":"Personal-user");
        if(!isGroup) returnStr += "; UserName["+userName+"]; FirstName["+firstName+"]; LastName["+lastName+"]";
        return returnStr;
    }
}

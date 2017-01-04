package com.weatherbot.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;

import org.telegram.telegrambots.logging.BotLogger;

/**
 * This is used to create table and update database by SQLLite. It could record who is subscribed.
 * @version 1.2
 * @author Xiujun Yang
 * @date 3rd Jan 2017
 */
public class MyConnection {
    private final String LOGTAG = "MyConnection";
    private final String tableName = "SubscriberList";
    
    private final String DB_COL_USERID = "USERID";
    private final String DB_COL_FIRSTNAME = "FIRSTNAME";
    private final String DB_COL_LASTNAME = "LASTNAME";
    private final String DB_COL_USERNAME = "USERNAME";
    private final String DB_COL_GROUPFLAG = "ISGROUP";
    
    private static MyConnection instance;
    private static Connection conn;
    private static Statement stmt;
    // Prevent SQL injection by Parameterized Statement
    private static PreparedStatement ps = null;
    private boolean isTableExisted;
    private State dbState = State.NONE;
    public enum State{NONE, CONNECTED, LOADED, DISCONNECTED};
    
    private ArrayList<Subscriber> subscriberList = new ArrayList<Subscriber>();
    
    public static MyConnection getInstance(){
        if (instance == null) instance = new MyConnection();
        return instance;
    }
    
    public Connection init(){
        if (instance == null) instance = getInstance();
        boolean isConn = false;
        try {
            if(conn != null) isConn = conn.isClosed();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if(isConn || conn == null) generateConnection();

        try {
            stmt = conn.createStatement();
            isTableExisted = isTableExistedCheck(tableName);
            System.out.println("isTableExisted="+isTableExisted);
            if(!isTableExisted) createTable();
            loadSubscriberFromDB();
        } catch(SQLException ex){
            ex.printStackTrace();
            colseConnection();
        }
        return conn;
    }
    
    private void generateConnection(){
        try {
            Class.forName("org.sqlite.JDBC");
            String dbURL = "jdbc:sqlite:MySQLiteDB.db";
            conn = DriverManager.getConnection(dbURL);
            if (conn != null) {
                System.out.println("Connected to the database");
                /*dm = (DatabaseMetaData) conn.getMetaData();
                System.out.println("Driver name: " + dm.getDriverName());
                System.out.println("Driver version: " + dm.getDriverVersion());
                System.out.println("Product name: " + dm.getDatabaseProductName());
                System.out.println("Product version: " + dm.getDatabaseProductVersion());*/
                dbState = State.CONNECTED;
            } else {
                System.out.println("Connection is empty.");
                colseConnection();
            }
        } catch (ClassNotFoundException| SQLException ex) {
            ex.printStackTrace();
            colseConnection();
        }
    }
    
    /**
     * Check if table already exist before creating new table.
     * @param tName Table Name
     * @return If true, table is existed; if false, table isn't existed;
     */
    private boolean isTableExistedCheck(String tName){
        boolean isConnClosed = false;
        try{
            isConnClosed = conn.isClosed();
        } catch(SQLException ex){
            ex.printStackTrace();
        }
        if (tName == null || isConnClosed || stmt == null)
        {
            return true;
        }
        try {
            stmt.executeQuery("SELECT * FROM "+tName);
        } catch(SQLException ex){
            ex.printStackTrace();
            if (ex.getErrorCode()==1 && ex.getMessage().contains("no such table: "+tableName)) return false;
        }
        //dm.getTables(null, null, tName, new String[] {"TABLE"});
        return true;
    }
    
    private void createTable() throws SQLException{
        if (stmt==null) {
            throw new SQLException("statement is null");
        }
        String tableInitSql = "CREATE TABLE " +tableName+
                " ("+DB_COL_USERID+"        INTEGER(20) NOT NULL, " + 
                    DB_COL_FIRSTNAME+"      TEXT, " + 
                    DB_COL_LASTNAME+"       TEXT, " + 
                    DB_COL_USERNAME+"       TEXT, " +
                    DB_COL_GROUPFLAG+"      INTEGER , PRIMARY KEY ("+DB_COL_USERID+")" +
                ")";
        stmt.executeUpdate(tableInitSql);
    }
    
    synchronized public boolean addSubscriber(long chatId, String userFirstName, String userLastName, String userName
            , boolean isGroup) throws SQLException{
        Iterator<Subscriber> it = subscriberList.iterator();
        while(it.hasNext()){
            Subscriber element = it.next();
            if(element.getChatId()==chatId) {
                BotLogger.info(LOGTAG, "chatId("+chatId+") is repeat");
                return false;
            }
        }
        Subscriber newSubscriber;
        if(isGroup) newSubscriber = new Subscriber(chatId);
        else newSubscriber = new Subscriber(chatId, userName, userFirstName, userLastName);
        subscriberList.add(newSubscriber);
        boolean result = updateToDB();
        if(!result) subscriberList.remove(newSubscriber);
        return result;
    }
    
    synchronized public void loadSubscriberFromDB(){
        ArrayList<Subscriber> tempList = new ArrayList<Subscriber>();
        try {
            tempList = queryDBSubscriber();
            // Clean all local data and reload from database. 
            subscriberList.clear();
            subscriberList.addAll(tempList);
            dbState = State.LOADED;
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    synchronized ArrayList<Subscriber> queryDBSubscriber() throws SQLException{
        if (stmt==null) {
            throw new SQLException("statement is null");
        }
        ArrayList<Subscriber> tempList = new ArrayList<Subscriber>();
        ResultSet rs = stmt.executeQuery("SELECT * FROM "+ tableName);
        while(rs.next()){
            long chatId = rs.getLong(DB_COL_USERID);
            String firstName = rs.getString(DB_COL_FIRSTNAME);
            String lastName = rs.getString(DB_COL_LASTNAME);
            String userName = rs.getString(DB_COL_USERNAME);
            boolean isGroup = rs.getInt(DB_COL_GROUPFLAG)== 1? true:false;
            BotLogger.info(LOGTAG, "userId:" +chatId
                    +",firstName:"+firstName+",lastName:"+lastName+",userName:"+userName
                    +", isGroup:"+ isGroup);
            Subscriber sub;
            if(isGroup) sub = new Subscriber(chatId);
            else sub = new Subscriber(chatId, userName, firstName, lastName);
            tempList.add(sub);
        }
        return tempList;
    }
    
    synchronized public boolean removeSubscriber(long chatId) throws SQLException{
        Iterator<Subscriber> it = subscriberList.iterator();
        while(it.hasNext()){
            Subscriber element = it.next();
            if(element.getChatId()==chatId) {
                subscriberList.remove(element);
                BotLogger.info(LOGTAG, "chatId["+chatId+"] unsubscribed");
                boolean result = updateToDB();
                if(!result) subscriberList.add(element);
                return result;
            }
        }
        return false;
    }
    
    synchronized boolean updateToDB() throws SQLException{
        if (conn==null) {
            throw new SQLException("conn is null");
        }
        
        ArrayList<Subscriber> temp = queryDBSubscriber();

        if(subscriberList.containsAll(temp)){
            //Data need to add in DB
            BotLogger.info(LOGTAG, "Data need to add in DB");
            Iterator<Subscriber> it = subscriberList.iterator();
            while(it.hasNext()){
                Subscriber element = it.next();
                if(! temp.contains(element)) {
                    String insertSQL = null;
                    if(element.isGroup()) {
                        insertSQL = "INSERT INTO "+ tableName + " ("+DB_COL_USERID+" ,"+DB_COL_GROUPFLAG+")"
                                + " VALUES (?,?)";
                    } else {
                        insertSQL = "INSERT INTO "+ tableName + " ("+DB_COL_USERID+", "+DB_COL_GROUPFLAG+","
                                + DB_COL_FIRSTNAME+", " + DB_COL_LASTNAME+", "+DB_COL_USERNAME+")"
                                + " VALUES (?,?,?,?,?)";
                    }
                    ps = conn.prepareStatement(insertSQL);
                    ps.setLong(1, element.getChatId());
                    ps.setInt(2, element.isGroup()?1:0);
                    if (!element.isGroup()){
                        ps.setString(3, element.getFirstName());
                        ps.setString(4, element.getLastName());
                        ps.setString(5, element.getUserName());
                    }
                    int result = ps.executeUpdate();
                    BotLogger.info(LOGTAG, "add to data: "+result);
                    return result==1;
                }
            }
        } else if(temp.containsAll(subscriberList)){
            //Data need to delete in DB
            Iterator<Subscriber> it = temp.iterator();
            while(it.hasNext()){
                Subscriber element = it.next();
                if(!subscriberList.contains(element)){
                    String deleteString = "DELETE FROM "+ tableName + " WHERE "+DB_COL_USERID+"= ?";
                    ps = conn.prepareStatement(deleteString);
                    ps.setLong(1, element.getChatId());
                    int result = ps.executeUpdate();
                    BotLogger.info(LOGTAG, "delete from data: "+result);
                    return result ==1;
                }
            }
        } else {
            // reload subscriber from db.
            loadSubscriberFromDB();
            BotLogger.debug(LOGTAG, "reload data from database");
        }
        return false;
    }
    
    public ArrayList<Subscriber> getSubscribersList(){
        return subscriberList;
    }
    
    public State getState(){
        return dbState;
    }
    
    private void colseConnection(){
        if (conn == null) return;
        try {
            ps.close();
            stmt.close();
            conn.close();
            stmt = null;
            conn = null;
            dbState = State.DISCONNECTED;
        } catch (Exception sqlex) {
            // prevent both of NullPointException and SQLException
            sqlex.printStackTrace();
        }
        System.exit(0);
    }
}

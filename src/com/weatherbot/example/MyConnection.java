package com.weatherbot.example;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This is used to create table and update database by SQLLite. It could record who is subscribed.
 * @version 1.0
 * @author Xiujun Yang
 * @date 19 Dec 2016
 */
public class MyConnection {
    private static final String databaseName = "DatabaseForSubscriber";
    private static final String tableName = "SubscriberList";    
    
    private static Connection conn= getInstance();
    private static Statement stmt;
    private static DatabaseMetaData dm;
    private static boolean isTableExisted;
    
    public static Connection getInstance(){
        if(conn == null) {
            generateConnection();
        }
        try {
            stmt = conn.createStatement();
            isTableExisted = isTableExistedCheck(tableName);
            System.out.println("isTableExisted="+isTableExisted);
            if(!isTableExisted) createTable();
        } catch(SQLException ex){
            ex.printStackTrace();
            colseConnection();
        }
        return conn;
    }
    
    private static void generateConnection(){
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
    private static boolean isTableExistedCheck(String tName){
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
            ResultSet result = stmt.executeQuery("SELECT * FROM "+tName);
        } catch(SQLException ex){
            ex.printStackTrace();
            if (ex.getErrorCode()==1 && ex.getMessage().contains("no such table: "+tableName)) return false;
        }
        //dm.getTables(null, null, tName, new String[] {"TABLE"});
        return true;
    }
    
    private static void createTable() throws SQLException{
        if (stmt==null) {
            throw new SQLException("statement is null");
        }
        String tableInitSql = "CREATE TABLE " +tableName+
                "(ID INT PRIMARY KEY      NOT NULL," +
                " USERID         VARCHAR(20) NOT NULL, " + 
                " FIRSTName      TEXT        NOT NULL, " + 
                " LASTNAME       TEXT, " + 
                " USERNAME       TEXT)"; 
        stmt.executeUpdate(tableInitSql);
    }
    
    private static void colseConnection(){
        if (conn == null) return;
        try {
            stmt.close();
            conn.close();
        } catch (SQLException sqlex) {
            sqlex.printStackTrace();
        }
        System.exit(0);
    }
}

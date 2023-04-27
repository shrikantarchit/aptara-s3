package com.aptara.filestorage.connectionpool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TimeZone;

 


/**
 * This class creates a singleton DBConnection object
 * @author Anil Tyagi
 *
 */

public final class DBConnection {
	
	private static DBConnection dbConnection = null;
	private DBConnection(){
		//prevents initialization of DBConnection object
	}
	/**
	 * Returns a singleton {@link DBConnection} object
	 * @author Anil Tyagi
	 * @return {@link DBConnection}
	 */
	public static DBConnection getInstance(){
		if(dbConnection==null){
			dbConnection = new DBConnection();
		}
		return dbConnection;
	}
	 /**
	  * Creates connection object based on the parameter dbURL value DEV, LIVE, QCT or UAT.
	  * @author Anil Tyagi
	  * @param dbURL
	  * @return {@link Connection}
	  */
	 public Connection getConnection(String dbURL) {  
		 TimeZone timeZone = TimeZone.getTimeZone("Asia/Kolkata");
	     TimeZone.setDefault(timeZone);
     	String user="", pass="", url="";
     	Connection con = null;
             try {                	
             	ResourceBundle myDbresources = ResourceBundle.getBundle("connection");
             	if(myDbresources!=null){
	             	if(dbURL.equals("DEV")){
	             		user = myDbresources.getString("usernameDEV");
	                    pass = myDbresources.getString("passwordDEV");
	                    url = myDbresources.getString("dbURLDEV");
	             	}else if(dbURL.equals("QCT")){
	             		user = myDbresources.getString("usernameQCT");
	                    pass = myDbresources.getString("passwordQCT");
	                    url = myDbresources.getString("dbURLQCT");
	             	}else if(dbURL.equals("LIVE")){
	             		user = myDbresources.getString("usernameLive");
	                    pass = myDbresources.getString("passwordLive");
	                    url = myDbresources.getString("dbURLLive");
	             	}else{
	             		user = myDbresources.getString("usernameUAT");
	                    pass = myDbresources.getString("passwordUAT");
	                    url = myDbresources.getString("dbURLUAT");
	             	}             	
             		Class.forName("oracle.jdbc.driver.OracleDriver");
	             	/*System.out.println("user : " + user);
	             	System.out.println("pass : " + pass);
	             	System.out.println("url : " + url);*/
             		con = DriverManager.getConnection(url, user, pass);
             	}else{
             		System.out.println("Connection resource not found.");
             		throw new MissingResourceException("The connection resource not available", "connection", "");
             	}
             } 
             catch(SQLException sqlex){
            	 
             	sqlex.printStackTrace();
             }
             catch (MissingResourceException e) {
            	 
             	e.printStackTrace();
             }
             catch(Exception e){
            	 
            	e.printStackTrace(); 
             }
             return con;
     }
}

package com.aptara.filestorage.connectionpool;

import java.sql.Connection;

 

/**
 * @author Anil Tyagi
 */

public class ConnectionObject {
	private static Connection con = null;
	
	/**
	 * Creates singleton instance of Connection object
	 * @return Connection
	 * @author Anil Tyagi
	 */
	 
	
	public static Connection getConnection(){
		try{
			con = DBConnection.getInstance().getConnection("DEV");
		}catch(Exception sqlex){
		 
			sqlex.printStackTrace();
		}
		return con;
	}
	
}
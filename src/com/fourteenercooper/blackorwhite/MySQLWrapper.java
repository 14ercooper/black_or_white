package com.fourteenercooper.blackorwhite;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import org.bukkit.scheduler.BukkitRunnable;

public class MySQLWrapper {
	// These are the front-facing interfaces that other parts of the plugin can use
	// Stores the information into the winsTable table
	public void storeWinner (String username, String color, String bet, String win) {
		BukkitRunnable r = new BukkitRunnable() {
		    @Override
		    public void run() {
		        try {
					connect();
			        makeQuery("INSERT INTO " + winsTable + " VALUES ('" + username + "','" + color + "'," + bet + "," + win + ");");
			        disconnect();
				} catch (ClassNotFoundException | SQLException e) {
					e.printStackTrace();
				}
		    }
		};
		r.runTaskAsynchronously(Main.main);
	}
	
	// Stores the informaion into the betsTable table
	public void storeBet (String username, String color, String bet) {
		BukkitRunnable r = new BukkitRunnable() {
		    @Override
		    public void run() {
		        try {
					connect();
			        makeQuery("INSERT INTO " + betsTable + " VALUES ('" + username + "','" + color + "'," + bet + ");");
			        disconnect();
				} catch (ClassNotFoundException | SQLException e) {
					e.printStackTrace();
				}
		    }
		};
		r.runTaskAsynchronously(Main.main);		
	}
	
	// Gets a hashmap of all placed bets
	public HashMap<String, String> getBets (String color) throws SQLException {
		HashMap<String, String> betsList = new HashMap<String, String>();
		ResultSet results = null;
    	try {
			connect();
	        results = makeQuery("SELECT * FROM " + betsTable + " WHERE COLOR = '" + color + "';");
	        disconnect();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
    	if (results != null) {
    		while (results.next()) {
    			String uname = results.getString ("username");
    			String bet = results.getString("bet");
    			betsList.put(uname, bet);
    		}
    	}
		return betsList;
	}
	
	// Resets the specified table
	public void resetTable (String tableName) {
		try {
			clearTable(tableName);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// These run all the behind-the-scenes work with the database
	// Database-related variables
	private int port;
	private String betsTable, winsTable, username, password, database, host;
	private Connection connection;
	
	// Load the database variables from the config file
	private void loadDatabaseVars () {
		host = ConfigParser.getDatabaseInfo("host");
		port = Integer.parseInt(ConfigParser.getDatabaseInfo("port"));
		database = ConfigParser.getDatabaseInfo("database");
		username = ConfigParser.getDatabaseInfo("username");
		password = ConfigParser.getDatabaseInfo("password");
		betsTable = ConfigParser.getDatabaseInfo("betsTable");
		winsTable = ConfigParser.getDatabaseInfo("winsTable");
	}
	
	// Connect to MySQL server
	private void connect () throws SQLException, ClassNotFoundException {
	    if (connection != null && !connection.isClosed()) {
	        return;
	    }
	 
	    loadDatabaseVars();
	    synchronized (this) {
	        if (connection != null && !connection.isClosed()) {
	            return;
	        } 
	        Class.forName("com.mysql.jdbc.Driver");
	        connection = DriverManager.getConnection("jdbc:mysql://" + this.host+ ":" + this.port + "/" + this.database, this.username, this.password);
	    }
	}
	
	// Disconnect from MySQL server
	private void disconnect () {
        if (isConnected()) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
	}
	
	// Is there an active connection to the database?
	private boolean isConnected () {
		return (connection == null ? false : true);
	}
	
	public void initTables () throws SQLException, ClassNotFoundException {
		connect();
		DatabaseMetaData dbm = connection.getMetaData();
		
		ResultSet tables = dbm.getTables(null, null, winsTable, null);
		if (tables.next()) {
		  // Table exists, so we do nothing
		}
		else {
		  // Table does not exist, so we create it
			makeQuery("CREATE TABLE " + winsTable + "(username varchar(255), color varchar(5), bet bigint, win bigint);");
		}
		
		tables = dbm.getTables(null, null, betsTable, null);
		if (tables.next()) {
		  // Table exists, so we do nothing
		}
		else {
		  // Table does not exist, so we create it
			makeQuery("CREATE TABLE " + betsTable + " (username varchar(255), color varchar(5), bet bigint);");
		}
		disconnect();
	}
	
	private void clearTable (String tableName) throws SQLException {
		makeQuery ("TRUNCATE TABLE " + tableName + ";");
	}
	
	private ResultSet makeQuery (String query) throws SQLException {
		if (!isConnected())
			return null;
		ResultSet results;
		Statement statement = connection.createStatement();
		results = statement.executeQuery(query);
		return results;
	}
}

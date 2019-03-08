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
					executeQuery("INSERT INTO " + winsTable + " VALUES ('" + username + "','" + color + "'," + bet + "," + win + ");");
			        disconnect();
				} catch (ClassNotFoundException | SQLException | InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
				}
		    }
		};
		r.runTaskAsynchronously(Main.main);
	}
	
	// Stores the information into the betsTable table
	public void storeBet (String username, String color, String bet) {
		BukkitRunnable r = new BukkitRunnable() {
		    @Override
		    public void run() {
		        try {
					connect();
					executeQuery("INSERT INTO " + betsTable + " VALUES ('" + username + "','" + color + "'," + bet + ");");
			        disconnect();
				} catch (ClassNotFoundException | SQLException | InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
				}
		    }
		};
		r.runTaskAsynchronously(Main.main);		
	}
	
	// Gets a hashmap of all placed bets
	public HashMap<String, String> getBets (String color) throws SQLException, InstantiationException, IllegalAccessException {
		HashMap<String, String> betsList = new HashMap<String, String>();
		ResultSet results = null;
    	try {
			connect();
	        results = makeQuery("SELECT * FROM " + betsTable + " WHERE COLOR = '" + color + "';");
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
    	while (results.next()) {
    		String uname = results.getString ("username");
    		String bet = results.getString("bet");
    		betsList.put(uname, bet);
    	}
        disconnect();
		return betsList;
	}
	
	// Resets the specified table
	public void resetTable (String tableName) {
		try {
			clearTable(tableName);
		} catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
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
		host = ConfigParser.getDatabaseInfo("ip");
		port = Integer.parseInt(ConfigParser.getDatabaseInfo("port"));
		database = ConfigParser.getDatabaseInfo("database");
		username = ConfigParser.getDatabaseInfo("username");
		password = ConfigParser.getDatabaseInfo("password");
		betsTable = ConfigParser.getDatabaseInfo("betsTable");
		winsTable = ConfigParser.getDatabaseInfo("winsTable");
	}
	
	// Connect to MySQL server
	private void connect () throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
	    if (connection != null && !connection.isClosed()) {
	        return;
	    }
	 
	    loadDatabaseVars();
	    synchronized (this) {
	        if (connection != null && !connection.isClosed()) {
	            return;
	        } 
	        Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
	        connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database + "?" +
	                                       "user=" + this.username + "&password=" + this.password);
	        // connection = DriverManager.getConnection("jdbc:mysql://" + this.host+ ":" + this.port + "/" + this.database, this.username, this.password);
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
	
	public void initTables () throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		connect();
		DatabaseMetaData dbm = connection.getMetaData();
		
		ResultSet tables = dbm.getTables(null, null, winsTable, null);
		if (tables.next()) {
		  // Table exists, so we do nothing
		}
		else {
		  // Table does not exist, so we create it
			executeQuery("CREATE TABLE " + winsTable + " (username varchar(255), color varchar(5), bet bigint, win bigint);");
		}
		
		tables = dbm.getTables(null, null, betsTable, null);
		if (tables.next()) {
		  // Table exists, so we do nothing
		}
		else {
		  // Table does not exist, so we create it
			executeQuery("CREATE TABLE " + betsTable + " (username varchar(255), color varchar(5), bet bigint);");
		}
		disconnect();
	}
	
	private void clearTable (String tableName) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		connect();
		executeQuery ("TRUNCATE TABLE " + tableName + ";");
		disconnect();
	}
	
	private boolean executeQuery (String query) throws SQLException {
		if (!isConnected())
			return false;
		boolean results;
		Statement statement = connection.createStatement();
		results = statement.execute(query);
		return results;
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

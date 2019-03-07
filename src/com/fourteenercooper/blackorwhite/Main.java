package com.fourteenercooper.blackorwhite;

import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import net.milkbowl.vault.economy.Economy;

/* 
		getServer().getPluginManager().registerEvents(new GUIInventory(), this);
		this.getCommand("ieadmin").setExecutor(new CommandProcessor());
*/

public class Main extends JavaPlugin {

	public static FileConfiguration pluginConfig; // The config file
	private static Economy econ; // This server's economy
	public static Main main; // A reference to this plugin
	public static MySQLWrapper wrapper;
	public static DrawingManager manager;
	public static BukkitScheduler scheduler;
	
	@Override
	public void onEnable () {
		main = this;
		// Handle configuration
		saveDefaultConfig();
		pluginConfig = getConfig();
		
		// Sets up the economy
		if (!setupEconomy()) {
			this.getLogger().severe("Disabled due to no Vault found. This plugin requires Vault.");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		
		// Sets up the MySQL wrapper
		wrapper = new MySQLWrapper();
		try {
			wrapper.initTables();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		
		// Registers the bet command
		this.getCommand("tx").setExecutor(new BetCommand());
		
		// Handles setting up the drawing manager
		manager = new DrawingManager();
		scheduler = Bukkit.getServer().getScheduler();
		manager.scheduleDrawing();
	}
	
	@Override
	public void onDisable () {
		
	}
	
	// Handles setting up the economy using Vault
	private boolean setupEconomy () {
		if (Bukkit.getPluginManager().getPlugin("Vault") == null)
			return false;
		
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null)
			return false;
		
		econ = rsp.getProvider();
		return econ != null;
	}
	
	// Returns the server's economy
	public static Economy getEconomy () {
		return econ;
	}
}

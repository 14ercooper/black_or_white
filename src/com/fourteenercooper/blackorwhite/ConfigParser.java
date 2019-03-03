package com.fourteenercooper.blackorwhite;

import org.bukkit.ChatColor;

public class ConfigParser {
	// Parses the lang data for the translated string
	public static String getLangData (String key) {
		String langData = "";
		try {
			langData = Main.pluginConfig.getString("lang." + key);
		} catch (Exception e) {
			// This should never be called unless the config file is messed up
			e.printStackTrace();
		}
		if (langData == null)
			return langData;
		else
			return ChatColor.translateAlternateColorCodes('&', langData);
	}
	
	// Parses the database data for the given key
	public static String getDatabaseInfo (String key) {
		String data = "";
		try {
			data = Main.pluginConfig.getString("database." + key);
		} catch (Exception e) {
			// This should never be called unless the config file is messed up
			e.printStackTrace();
		}
		return data;
	}
	
	public static float getWinReturn () {
		return Float.parseFloat(Main.pluginConfig.getString("winReturn"));
	}
	
	public static float getDrawingRate () {
		return Float.parseFloat(Main.pluginConfig.getString("drawingRate"));
	}
	
	public static boolean announceWinner () {
		return Main.pluginConfig.getBoolean("announceWin");
	}
}

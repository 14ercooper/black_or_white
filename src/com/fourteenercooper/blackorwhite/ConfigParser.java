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
}

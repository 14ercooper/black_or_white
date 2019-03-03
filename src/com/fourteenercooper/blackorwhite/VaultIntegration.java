package com.fourteenercooper.blackorwhite;

import org.bukkit.OfflinePlayer;

import net.milkbowl.vault.economy.Economy;

public class VaultIntegration {
	public static void addFunds (OfflinePlayer player, double amount) {
		Economy econ = Main.getEconomy();
		econ.depositPlayer(player, amount);
	}
	
	public static boolean removeFunds (OfflinePlayer player, double amount) {
		Economy econ = Main.getEconomy();
		double playerFunds = econ.getBalance(player);
		if (playerFunds < amount)
			return false;
		econ.withdrawPlayer(player, amount);
		return true;
	}
}

package com.fourteenercooper.blackorwhite;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import net.milkbowl.vault.economy.Economy;

public class DrawingManager {

	// This schedules future drawings, which iterative schedule future drawings. This is run originally from the main function on bootup.
	// DO NOT USE /reload with this plugin, it will break stuff
	public void scheduleDrawing () {
		
	}
	
	// This actually handles performing a drawing
	private void performDrawing () {
		
	}
	
	// This awards the players who won the bet
	@SuppressWarnings("deprecation")
	private void awardPlayers (Map<String,String> bets, String color) {
		// Variables that should only be loaded once per drawing
		double betMult = ConfigParser.getWinReturn();
		Map.Entry<String,String> largestWin = null;
		Economy econ = Main.getEconomy();
		// Loop through all players who won
		if (bets.size() == 0)
			return; // Prevents an issue if no bets were placed
		for (Map.Entry<String,String> bet : bets.entrySet()) {
			// Pull the username of the winner and the bet they placed
			String username = bet.getKey();
			double betAmt = Double.parseDouble(bet.getValue());
			// Set their bet to the largest win (if needed)
			if (largestWin != null) {
				if (betAmt > Double.parseDouble(largestWin.getValue()))
					largestWin = bet;
			} else {
				largestWin = bet;
			}
			// Broadcast to the player and give them money
			wonMoneyBroadcast(username, betAmt * betMult, color);
			econ.depositPlayer(username, betAmt * betMult);
		}
		// Broadcast to the entire world the largest payout, store it, and reset the bets MySQL database
		biggestWinBroadcast(largestWin, betMult, color);
		storeBiggestWin(largestWin, Double.parseDouble(largestWin.getValue()) * betMult, color);
		Main.wrapper.resetTable(ConfigParser.getDatabaseInfo("betsTable"));
	}
	
	/*
  		betWin: "Your bet for <color> was successful! You got <win> from your bet of <bet>!"
  		betLose: "Your bet for <color> was unsuccessful. You lost your <bet>."
  		betBroadcast: "<player> just won <win> by betting on <color> in Black or White!"
	 */
	
	// This is a personal broadcast to any player who won money
	private void wonMoneyBroadcast (String username, double bet, String color) {
		String text = ConfigParser.getLangData("betWin");
		text = text.replace("<win>", Double.toString(bet));
		text = text.replace("<color>", color);
		OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(username);
		if (player.isOnline())
			player.getPlayer().sendMessage(text);
	}
	
	// Lets players who lost money know
	private void lostMoenyBroadcast (String username, String amount, String color) {
		String text = ConfigParser.getLangData("betLose");
		
	}
	
	// This is the global broadcast for the player who won the most money
	private void biggestWinBroadcast (Map.Entry<String, String> largestWin, double betMult, String color) {
		if (largestWin != null) {
			String text = ConfigParser.getLangData("betBroadcast");
			text = text.replace("<player>", largestWin.getKey());
			text = text.replace("<color>", color);
			text = text.replace("<win>", Double.toString(Double.parseDouble(largestWin.getValue()) * betMult));
		} else {
			Bukkit.getServer().broadcastMessage(ConfigParser.getLangData("noBetsPlaced"));
		}
	}
	
	// This stores the largest win into the database
	private void storeBiggestWin (Map.Entry<String,String> bet, double win, String color) {
		if (bet != null)
			Main.wrapper.storeWinner(bet.getKey(), color, bet.getValue(), Double.toString(win));
	}
}

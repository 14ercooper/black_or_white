package com.fourteenercooper.blackorwhite;

import java.sql.SQLException;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import net.milkbowl.vault.economy.Economy;

public class DrawingManager {

	public Runnable drawingScheduler;
	
	// This schedules future drawings, which iterative schedule future drawings. This is run originally from the main function on bootup.
	// DO NOT USE /reload with this plugin, it will break stuff
	public void scheduleDrawing () {
		drawingScheduler = new Runnable () {
			@Override
			public void run () {
				try {
					performDrawing();
				} catch (SQLException | InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		};
		Main.scheduler.scheduleSyncRepeatingTask(Main.main, drawingScheduler, 10L,
				(long) (ConfigParser.getDrawingRate() * 60f * 20f));
	}
	
	// This actually handles performing a drawing
	private void performDrawing () throws SQLException, InstantiationException, IllegalAccessException {
		// Some variables for the drawing
		String winningColor, losingColor = null;
		Random r = new Random ();
		if (r.nextBoolean()) { // tai won
			winningColor = "tai";
			losingColor = "xiu";
		} else { // xiu won
			winningColor = "xiu";
			losingColor = "tai";
		}
		// Gets the bets for each color
		Map<String,String> winningBets = Main.wrapper.getBets(winningColor);
		Map<String,String> losingBets = Main.wrapper.getBets(losingColor);
		if (winningBets.size() == 0) {
			Bukkit.getServer().broadcastMessage(ConfigParser.getLangData("noBetsPlaced"));
		}
		// Loop through losing bets
		for (Map.Entry<String,String> bet : losingBets.entrySet()) {
			// Inform them they lost
			lostMoneyBroadcast (bet.getKey(), bet.getValue(), losingColor);
		}
		// Loop through winning bets
		awardPlayers (winningBets, winningColor);
		Main.wrapper.resetTable(ConfigParser.getDatabaseInfo("betsTable"));
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
			wonMoneyBroadcast(username, betAmt * betMult, color, betMult);
			econ.depositPlayer(username, betAmt * betMult);
		}
		// Broadcast to the entire world the largest payout, store it, and reset the bets MySQL database
		biggestWinBroadcast(largestWin, betMult, color);
		storeBiggestWin(largestWin, Double.parseDouble(largestWin.getValue()) * betMult, color);
		Main.wrapper.resetTable(ConfigParser.getDatabaseInfo("betsTable"));
	}
	
	// This is a personal broadcast to any player who won money
	@SuppressWarnings("deprecation")
	private void wonMoneyBroadcast (String username, double bet, String color, double betMult) {
		String text = ConfigParser.getLangData("betWin");
		text = text.replace("<win>", Double.toString(bet));
		text = text.replace("<color>", color);
		text = text.replace("<bet>", Double.toString(bet / betMult));
		OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(username);
		if (player.isOnline())
			player.getPlayer().sendMessage(text);
	}
	
	// Lets players who lost money know
	@SuppressWarnings("deprecation")
	private void lostMoneyBroadcast (String username, String amount, String color) {
		String text = ConfigParser.getLangData("betLose");
		text = text.replace("<color>", color);
		text = text.replace("<bet>", amount);
		OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(username);
		if (player.isOnline())
			player.getPlayer().sendMessage(text);
	}
	
	// This is the global broadcast for the player who won the most money
	private void biggestWinBroadcast (Map.Entry<String, String> largestWin, double betMult, String color) {
		if (largestWin != null) {
			String text = ConfigParser.getLangData("betBroadcast");
			text = text.replace("<player>", largestWin.getKey());
			text = text.replace("<color>", color);
			text = text.replace("<win>", Double.toString(Double.parseDouble(largestWin.getValue()) * betMult));
			Bukkit.getServer().broadcastMessage(text);
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

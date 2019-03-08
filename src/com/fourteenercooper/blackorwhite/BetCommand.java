package com.fourteenercooper.blackorwhite;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BetCommand implements CommandExecutor {

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// Is this a player who send the correct command?
		if (sender instanceof Player && label.equalsIgnoreCase("tx")) {
			// Did they bet on a color?
			if (args.length < 2)
				return false;
			if (args[0].equalsIgnoreCase("tai") || args[0].equalsIgnoreCase("xiu")) {
				// Did they actually bet money?
				try {
					if (Double.parseDouble(args[1]) > 0) {
						// Can they cover the bet?
						String username = sender.getName();
						if (Main.getEconomy().getBalance(username) - Double.parseDouble(args[1]) > 0) {
							// Makes sure they haven't already placed a bet for this drawing
							if (Main.wrapper.alreadyBet(username)) {
								((Player) sender).sendMessage(ConfigParser.getLangData("betAlreadyPlaced"));
								return true;
							}
							// Place the bet and take the money
							Main.getEconomy().withdrawPlayer(username, Double.parseDouble(args[1]));
							Main.wrapper.storeBet(username, args[0].toLowerCase(), args[1]);
							if (args[0].equalsIgnoreCase("tai"))
								args[0] = ConfigParser.getLangData("tai");
							else
								args[0] = ConfigParser.getLangData("xiu");
							sender.sendMessage(ConfigParser.getLangData("betPlaced").replace("<bet>", args[1]).replace("<color>", args[0]));
							return true;
						} else {
							((Player) sender).sendMessage(ConfigParser.getLangData("notEnoughFunds"));
							return true;
						}
					}
				} catch (Exception e) {
					return false; // That's not a number, silly person
				}
			}
		}
		return false;
	}
}

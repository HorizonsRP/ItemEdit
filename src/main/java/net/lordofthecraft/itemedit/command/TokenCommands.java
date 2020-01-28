package net.lordofthecraft.itemedit.command;

import co.lotc.core.command.annotate.Arg;
import co.lotc.core.command.annotate.Cmd;
import co.lotc.core.command.annotate.Range;
import co.lotc.core.util.MojangCommunicator;
import net.lordofthecraft.itemedit.ItemEdit;
import net.lordofthecraft.itemedit.sqlite.TransactionsSQL;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

public class TokenCommands extends BaseCommand {

	private TransactionsSQL transSQL;
	private static final String NO_PLAYER = ItemEdit.PREFIX + "Unable to find a player by that name.";

	TokenCommands(TransactionsSQL trans) {
		transSQL = trans;
	}

	public void invoke(CommandSender sender) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			msg(getTokenData(p, null));
		} else {
			msg(ItemEdit.PREFIX + "Console does not have rename tokens.");
		}
	}

	@Cmd(value="Check how many tokens someone else has.", permission="itemedit.mod")
	public void get(CommandSender sender,
					@Arg(value="Player Name") String playerName) {
		Player p = getPlayerByName(playerName);
		if (p != null) {
			msg(getTokenData(p, p.getName()));
		} else {
			msg(NO_PLAYER);
		}
	}

	private String getTokenData(Player p, String name) {
		transSQL.grabMonikerTokens(p);
		String output = "";

		if (name == null) {
			output = ItemEdit.PREFIX + "You currently have ";
		} else {
			output = ItemEdit.PREFIX + name + " currently has ";
		}
		output += ItemEdit.ALT_COLOR + (transSQL.getTokens(p)) + ItemEdit.PREFIX + " edit tokens,\n";

		if (transSQL.getVipTokensTotal(p) > 0) {
			output += "and " + ItemEdit.ALT_COLOR + (transSQL.getVipTokensTotal(p) - transSQL.getVIPUsedAmount(p)) + " / " + transSQL.getVipTokensTotal(p) + ItemEdit.PREFIX + " VIP tokens.\n" +
					  "VIP tokens refresh " + ItemEdit.ALT_COLOR + ItemEdit.getRefreshTime() + ItemEdit.PREFIX + " days after use.\n";
		}
		if (transSQL.getVIPUsedAmount(p) > 0) {
			if (name == null) {
				output += "Your ";
			} else {
				output += "Their ";
			}
			output += "next VIP token will refresh in " + ItemEdit.ALT_COLOR + transSQL.getDaysUntilRename(p) + ItemEdit.PREFIX + " days.";
		}

		return output;
	}

	@Cmd(value="Adds 1-15 tokens to a player.", permission="itemedit.mod")
	public void give(CommandSender sender,
					 @Arg(value="Player Name") String playerName,
					 @Arg(value="Tokens")@Range(min=1, max=15)int amount) {
		Player p = getPlayerByName(playerName);
		if (p != null) {
			ItemEdit.changeTokens(amount, p);
			msg(updateMessage(p));
		} else {
			msg(NO_PLAYER);
		}
	}

	@Cmd(value="Removes 1-15 tokens from a player.", permission="itemedit.mod")
	public void remove(CommandSender sender,
					   @Arg(value="Player Name") String playerName,
					   @Arg(value="Tokens")@Range(min=1, max=15)int amount) {
		Player p = getPlayerByName(playerName);
		if (p != null) {
			ItemEdit.changeTokens(-amount, p);
			msg(updateMessage(p));
		} else {
			msg(NO_PLAYER);
		}
	}

	@Cmd(value="Manually set the number of tokens a player has.", permission="itemedit.admin")
	public void set(CommandSender sender,
					@Arg(value="Player Name") String playerName,
					@Arg(value="Tokens")@Range(min=1)int amount) {
		Player p = getPlayerByName(playerName);
		if (p != null) {
			transSQL.addEntry(0, p, amount);
			msg(updateMessage(p));
		} else {
			msg(NO_PLAYER);
		}
	}

	private static String updateMessage(Player p) {
		return ItemEdit.PREFIX + "Successfully updated " + p.getName() + "'s token count.";
	}

	private static Player getPlayerByName(String playerName) {
		Player p = Bukkit.getPlayer(playerName);
		if (p == null) {
			try {
				p = Bukkit.getPlayer(MojangCommunicator.requestPlayerUUID(playerName));
			} catch (IOException e) {
				if (ItemEdit.DEBUGGING) {
					e.printStackTrace();
				}
			}
		}
		return p;
	}

}

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
import java.util.UUID;

public class TokenCommands extends BaseCommand {

	private TransactionsSQL transSQL;
	private static final String NO_PLAYER = ItemEdit.PREFIX + "Unable to find a player by that name.";

	TokenCommands(TransactionsSQL trans) {
		transSQL = trans;
	}

	public void invoke(CommandSender sender) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			msg(getTokenData(p.getUniqueId(), null));
		} else {
			msg(ItemEdit.PREFIX + "Console does not have rename tokens.");
		}
	}

	@Cmd(value="Check how many tokens someone else has.", permission="itemedit.mod")
	public void get(CommandSender sender,
					@Arg(value="Player Name") String playerName) {
		UUID uuid = null;
		try {
			uuid = MojangCommunicator.requestPlayerUUID(playerName);
			if (uuid != null) {
				msg(getTokenData(uuid, playerName));
			}
		} catch (IOException e) {
			if (ItemEdit.DEBUGGING) {
				e.printStackTrace();
			}
		} finally {
			if (uuid == null) {
				msg(NO_PLAYER);
			}
		}
	}

	private String getTokenData(UUID player, String name) {
		//transSQL.grabMonikerTokens(player);
		int vipTokensTotal = transSQL.getVipTokensTotal(player);
		int vipTokensUsed = transSQL.getVIPUsedAmount(player);

		String startName = "You";
		String middleName = "you";
		String posses = "have";
		String output = "";

		if (name != null) {
			startName = name;
			middleName = name;
			posses = "has";
		}
		output = ItemEdit.PREFIX + startName + " currently " + posses + " " + ItemEdit.ALT_COLOR + (transSQL.getTokens(player)) + ItemEdit.PREFIX + " edit tokens.\n";
		if (vipTokensTotal > 0) {
			output += "In addition, " + middleName + " " + posses + " " + ItemEdit.ALT_COLOR + (vipTokensTotal - vipTokensUsed) + " / " + vipTokensTotal + ItemEdit.PREFIX + " VIP tokens.\n" +
					  "VIP tokens refresh " + ItemEdit.ALT_COLOR + ItemEdit.getRefreshTime() + ItemEdit.PREFIX + " days after use.\n";
		}
		if (vipTokensUsed > 0) {
			if (name == null) {
				output += "Your ";
			} else {
				output += "Their ";
			}
			output += "next VIP token will refresh in " + ItemEdit.ALT_COLOR + transSQL.getDaysUntilRename(player) + ItemEdit.PREFIX + " days.";
		}

		return output;
	}

	@Cmd(value="Adds 1-15 tokens to a player.", permission="itemedit.mod")
	public void give(CommandSender sender,
					 @Arg(value="Player Name") String playerName,
					 @Arg(value="Tokens")@Range(min=1, max=15)int amount) {
		if (ItemEdit.changeTokens(amount, playerName)) {
			msg(updateMessage(playerName));
		} else {
			msg(NO_PLAYER);
		}
	}

	@Cmd(value="Removes 1-15 tokens from a player.", permission="itemedit.mod")
	public void remove(CommandSender sender,
					   @Arg(value="Player Name") String playerName,
					   @Arg(value="Tokens")@Range(min=1, max=15)int amount) {
		if (ItemEdit.changeTokens(-amount, playerName)) {
			msg(updateMessage(playerName));
		} else {
			msg(NO_PLAYER);
		}
	}

	@Cmd(value="Manually set the number of tokens a player has.", permission="itemedit.admin")
	public void set(CommandSender sender,
					@Arg(value="Player Name") String playerName,
					@Arg(value="Tokens")@Range(min=1)int amount) {
		if (ItemEdit.setTokens(amount, playerName)) {
			msg(updateMessage(playerName));
		} else {
			msg(NO_PLAYER);
		}
	}

	private static String updateMessage(String playerName) {
		String name = TransactionsSQL.getPlayerByName(playerName).getName();
		if (name == null) {
			name = playerName;
		}
		return ItemEdit.PREFIX + "Successfully updated " + name + "'s token count.";
	}

}

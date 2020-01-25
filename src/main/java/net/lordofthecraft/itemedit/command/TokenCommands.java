package net.lordofthecraft.itemedit.command;

import co.lotc.core.command.annotate.Arg;
import co.lotc.core.command.annotate.Cmd;
import co.lotc.core.command.annotate.Range;
import net.lordofthecraft.itemedit.ItemEdit;
import net.lordofthecraft.itemedit.sqlite.TransactionsSQL;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TokenCommands extends BaseCommand {

	private TransactionsSQL transSQL;

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
	public void get(CommandSender sender, Player p) {
		msg(getTokenData(p, p.getDisplayName()));
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
	public void give(CommandSender sender, Player p,
					 @Arg(value="Tokens")@Range(min=1, max=15)int amount) {
		transSQL.addEntry(0, p, (transSQL.getTokens(p) + amount));
		msg(ItemEdit.PREFIX + "Successfully updated " + p.getDisplayName() + "'s token count.");
	}

	@Cmd(value="Removes 1-15 tokens from a player.", permission="itemedit.mod")
	public void remove(CommandSender sender, Player p,
					 @Arg(value="Tokens")@Range(min=1, max=15)int amount) {
		int newAmount = (transSQL.getTokens(p) - amount);
		if (newAmount < 0) {
			newAmount = 0;
		}
		transSQL.addEntry(0, p, newAmount);
		msg(ItemEdit.PREFIX + "Successfully updated " + p.getDisplayName() + "'s token count.");
	}

	@Cmd(value="Manually set the number of tokens a player has.", permission="itemedit.admin")
	public void set(CommandSender sender, Player p,
					@Arg(value="Tokens")@Range(min=1)int amount) {
		transSQL.addEntry(0, p, amount);
		msg(ItemEdit.PREFIX + "Successfully updated " + p.getDisplayName() + "'s token count.");
	}

}

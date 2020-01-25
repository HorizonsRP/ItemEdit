package net.lordofthecraft.itemedit.command;

import co.lotc.core.bukkit.util.ItemUtil;
import co.lotc.core.command.annotate.Arg;
import co.lotc.core.command.annotate.Cmd;
import co.lotc.core.command.annotate.Range;
import co.lotc.core.util.MojangCommunicator;
import io.github.archemedes.customitem.CustomTag;
import net.lordofthecraft.itemedit.ItemEdit;
import net.lordofthecraft.itemedit.sqlite.TransactionsSQL;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class StaffCommands extends BaseCommand {

	private TransactionsSQL transSQL;

	StaffCommands(TransactionsSQL trans) {
		transSQL = trans;
	}

	@Cmd(value="Gets information on the given item.")
	public void iteminfo(CommandSender sender) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			ItemStack item = transSQL.getItemInHand(p);
			StringBuilder output = new StringBuilder();

			if (ItemUtil.hasCustomTag(item, MainCommands.EDITED_TAG)) {
				output.append(ItemEdit.ALT_COLOR).append("Edited By:").append(tagPlayerInfo(item, MainCommands.EDITED_TAG));
			} else if (ItemUtil.hasCustomTag(item, "renamedusers")) {
				output.append(ItemEdit.ALT_COLOR).append("Edited By:").append(tagPlayerInfo(item, "renamedusers"));
			}

			if (ItemUtil.hasCustomTag(item, MainCommands.SIGNED_TAG)) {
				output.append(ItemEdit.ALT_COLOR).append("Signed By:").append(tagPlayerInfo(item, MainCommands.SIGNED_TAG));
			} else if (ItemUtil.hasCustomTag(item, "signuser")) {
				output.append(ItemEdit.ALT_COLOR).append("Signed By:").append(tagPlayerInfo(item, "signuser"));
			}

			if (output.length() <= 0) {
				output.append("This is a legacy item created by Moniker, or it has not been edited. Please use /ci to investigate the custom tags.");
			}
			msg(ItemEdit.PREFIX + output.toString());
		}
	}

	private String tagPlayerInfo(ItemStack item, String fullTag) {
		String value = ItemUtil.getCustomTag(item, fullTag);

		String[] tags = value.replace("/", " ").split(" ");
		StringBuilder builder = new StringBuilder("\n");
		short length = (short) UUID.randomUUID().toString().length();

		for (String tag : tags) {
			value = tag;
			String[] thisTag = tag.replace(":", " ").split(" ");

			if (thisTag.length >= 1 && thisTag[0] != null && thisTag[0].length() == length) {
				value = thisTag[0];
				String date = "";
				String playerName = "";

				Player editor = Bukkit.getPlayer(UUID.fromString(thisTag[0]));
				if (editor != null) {
					playerName = editor.getName();
				} else {
					try {
						playerName = MojangCommunicator.requestCurrentUsername(UUID.fromString(thisTag[0])).replace("\"", "");
					} catch (IOException ignore) {
					}
				}

				if (thisTag.length >= 2 && thisTag[1] != null) {
					DateFormat format = new SimpleDateFormat("dd MMM yyyy HH:mm Z");
					date = format.format(new Date(Long.parseLong(thisTag[1])));
				}

				if (date.length() > 0) {
					value += "\n" + ItemEdit.ALT_COLOR + date;
				}
				if (playerName.length() > 0) {
					value += ItemEdit.PREFIX + " | " + ItemEdit.ALT_COLOR + playerName;
				}

			}
			builder.append(ItemEdit.PREFIX).append(value).append("\n");
		}
		return builder.toString();
	}

	@Cmd(value="Changes the VIP cooldown between tokens refreshing in days.", permission="itemedit.admin")
	public void setVIPCooldown(@Arg(value="Days")@Range(min=1, max=90)int days) {
		ItemEdit.get().setRefreshTime(days);
		msg(ItemEdit.PREFIX + "VIP refresh days set to " + days + ".");
	}

	@Cmd(value="Changes the width of described items in character count.", permission="itemedit.admin")
	public void setDescWidth(@Arg(value="# of letters")@Range(min=1, max=100)int characters) {
		ItemEdit.get().setMaxWidth(characters);
		msg(ItemEdit.PREFIX + "Lore and name width set to " + characters + ".");
	}

	@Cmd(value="Removes all tokens and recent edit history of a given player.", permission="itemedit.admin")
	public void purge(CommandSender sender, Player p) {
		transSQL.deleteAllFromPlayer(p);
		msg(ItemEdit.PREFIX + "Purged all edit data for " + p.getDisplayName() + " from our SQL Database. Edited items will not been affected.");
	}

}

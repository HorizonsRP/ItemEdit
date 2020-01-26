package net.lordofthecraft.itemedit.command;

import co.lotc.core.bukkit.util.ItemUtil;
import co.lotc.core.command.annotate.Arg;
import co.lotc.core.command.annotate.Cmd;
import co.lotc.core.command.annotate.Default;
import co.lotc.core.command.annotate.Flag;
import io.github.archemedes.customitem.CustomItem;
import net.lordofthecraft.itemedit.Glow;
import net.lordofthecraft.itemedit.ItemEdit;
import net.lordofthecraft.itemedit.sqlite.TransactionsSQL;
import net.lordofthecraft.soulbind.SoulbindEnchant;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;

import java.util.ArrayList;
import java.util.List;

public class MainCommands extends BaseCommand {

	// Sub command for moderators
	private static StaffCommands staffCommands;
	private static TokenCommands tokenCommands;

	// Our local /edit clear sub command.
	private static final ClearCommands CLEAR_COMMANDS = new ClearCommands();

	// Error messages
	private static final String NO_ITEM = ItemEdit.PREFIX + "Please hold an item in your main hand while editing.";
	private static final String NO_TOKENS = ItemEdit.PREFIX + "You do not have enough edit tokens to edit this item!";
	private static final String NO_NAME_SET = ItemEdit.PREFIX + "Please set a custom name before setting a custom colour.";
	private static final String COLOR_ERROR = ItemEdit.PREFIX + "You do not have permission to use this color type.";
	private static final String SIGNED_ALREADY = ItemEdit.PREFIX + "This item has already been signed!";
	private static final String SIGNED_OTHERWISE = ItemEdit.PREFIX + "You were not the one to sign this item!";
	private static final String NO_SIGNATURE_PERM = ItemEdit.PREFIX + "You do not have permission to use that signature.";
	private static final String MAX_LENGTH = ItemEdit.PREFIX + "You have reached the maximum length on that item.";
	private static String nameTooLong; // This one is based on Max Width so it's set on instance creation.

	// Application short-hands
	private static final String DESC_PREFIX = ChatColor.GRAY + "" + ChatColor.ITALIC;
	private static final Glow GLOW = new Glow(new NamespacedKey(ItemEdit.get(), ItemEdit.get().getDescription().getName()));

	// Tags set by CustomItem for identification
	public static final String EDITED_TAG = "editor-uuid";
	public static final String SIGNED_TAG = "signed-uuid";

	// Our SQL access
	public static TransactionsSQL transSQL;

	public MainCommands(TransactionsSQL trans) {
		nameTooLong = ItemEdit.PREFIX + "That name is too long! Please shorten it to " + ItemEdit.getMaxWidth() + " characters long.";
		transSQL = trans;
		staffCommands = new StaffCommands(transSQL);
		tokenCommands = new TokenCommands(transSQL);

		CustomItem.makeUnplaceable(SIGNED_TAG);
		CustomItem.makeUnplaceable("monikerrenamed");
	}

	// Per Player //
	@Cmd(value="Check how many tokens you have.", permission="itemedit.check")
	public BaseCommand tokens() {
		return tokenCommands;
	}

	@Cmd(value="Checks the prices on each edit, if enabled.", permission="itemedit.prices")
	public void prices() {
		msg(ItemEdit.PREFIX + "This feature is not currently enabled!");
	}

	// Edit Types //
	@Cmd(value="Set a custom name for an item.", permission="itemedit.name")
	@Flag(name="mod", description="Sets the name regardless of signature.", permission="itemedit.mod")
	public void name(CommandSender sender,
					 @Arg(value="Name", description="The name you wish to give the item.") String[] name) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			ItemStack item = transSQL.getItemInHand(p);
			if (item != null && item.getType() != Material.AIR) {
				if (!hasFlag("mod") && isSigned(item) && notSignedBy(item, p)) {
					msg(SIGNED_ALREADY);
					return;
				}

				int tokensUsed = transSQL.safeToChargePlayer(p);
				if (ItemUtil.hasCustomTag(item, EDITED_TAG)) {
					tokensUsed = 0;
				} else if (tokensUsed == 0) {
					msg(NO_TOKENS);
					return;
				}

				ItemMeta meta = item.getItemMeta();
				if (meta != null) {
					StringBuilder thisName = new StringBuilder();
					for (String s : name) {
						if (thisName.length() > 0) {
							thisName.append(" ").append(s);
						} else {
							thisName.append(s);
						}
					}
					validate(thisName.toString().length() <= ItemEdit.getMaxWidth(), nameTooLong);
					meta.setDisplayName(ChatColor.ITALIC + ChatColor.stripColor(thisName.toString()));
					item.setItemMeta(meta);
				}

				finalizeEdit(p, item, tokensUsed);
				return;
			}
		}
		msg(NO_ITEM);
	}

	@Cmd(value="Change the color of the name of an item's name.", permission="itemedit.color")
	@Flag(name="bold", description="Allows bypassing the colour type.", permission="itemedit.mod")
	@Flag(name="under", description="Allows bypassing the colour type.", permission="itemedit.mod")
	@Flag(name="strike", description="Allows bypassing the colour type.", permission="itemedit.mod")
	@Flag(name="magic", description="Allows bypassing the colour type.", permission="itemedit.mod")
	public void color(CommandSender sender, ChatColor color) {

		validate(color != ChatColor.BOLD, COLOR_ERROR);
		validate(color != ChatColor.ITALIC, COLOR_ERROR);
		validate(color != ChatColor.MAGIC, COLOR_ERROR);
		validate(color != ChatColor.RESET, COLOR_ERROR);
		validate(color != ChatColor.UNDERLINE, COLOR_ERROR);
		validate(color != ChatColor.STRIKETHROUGH, COLOR_ERROR);

		String colorString = "" + color;
		if (hasFlag("bold")) {
			colorString += ChatColor.BOLD;
		}
		if (hasFlag("under")) {
			colorString += ChatColor.UNDERLINE;
		}
		if (hasFlag("strike")) {
			colorString += ChatColor.STRIKETHROUGH;
		}
		if (hasFlag("magic")) {
			colorString += ChatColor.MAGIC;
		}

		if (sender instanceof Player) {
			Player p = (Player) sender;
			ItemStack item = transSQL.getItemInHand(p);
			if (item != null && item.getType() != Material.AIR) {
				if (!hasFlag("mod") && isSigned(item) && notSignedBy(item, p)) {
					msg(SIGNED_ALREADY);
					return;
				}
				ItemMeta meta = item.getItemMeta();
				if (meta != null && meta.hasDisplayName()) {

					int tokensUsed = transSQL.safeToChargePlayer(p);
					if (ItemUtil.hasCustomTag(item, EDITED_TAG)) {
						tokensUsed = 0;
					} else if (tokensUsed == 0) {
						msg(NO_TOKENS);
						return;
					}

					meta.setDisplayName(colorString + ChatColor.ITALIC + ChatColor.stripColor(meta.getDisplayName()));
					item.setItemMeta(meta);

					finalizeEdit(p, item, tokensUsed);
				} else {
					msg(NO_NAME_SET);
				}
				return;
			}
		}
		msg(NO_ITEM);
	}

	@Cmd(value="Add a custom description for an item.", permission="itemedit.desc")
	public void desc(CommandSender sender,
					 @Arg(value="Description", description="The blurb of text you wish to add to the item's lore. Use multiple times to string together more lore.") String[] desc) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			ItemStack item = transSQL.getItemInHand(p);
			if (item != null && item.getType() != Material.AIR) {
				if (isSigned(item)) {
					msg(SIGNED_ALREADY);
					return;
				}

				int maxLines = transSQL.getMaxLines(p);
				if (item.getItemMeta() != null && item.getItemMeta().getLore() != null &&
					item.getItemMeta().getLore().size() > maxLines) {
					msg(MAX_LENGTH);
					return;
				}

				int tokensUsed = transSQL.safeToChargePlayer(p);
				if (ItemUtil.hasCustomTag(item, EDITED_TAG)) {
					tokensUsed = 0;
				} else if (tokensUsed == 0) {
					msg(NO_TOKENS);
					return;
				}

				if (!applyDesc(item, desc, maxLines)) {
					msg(MAX_LENGTH);
					return;
				} else {
					finalizeEdit(p, item, tokensUsed);
				}

				return;
			}
		}
		msg(NO_ITEM);
	}

	public static boolean applyDesc(ItemStack item, String[] desc, int maxLines) {
		ItemMeta meta = item.getItemMeta();
		List<String> newDesc = null;
		if (meta != null) {
			newDesc = meta.getLore();
			if (newDesc == null) {
				newDesc = new ArrayList<>();
			}

			StringBuilder thisString = new StringBuilder();
			if (newDesc.size() > 0) {
				thisString = new StringBuilder(ChatColor.stripColor(newDesc.get(newDesc.size() - 1)));
				newDesc.remove(newDesc.size() - 1);
			}

			for (String word : desc) {
				int currentLength = 0;
				if (thisString.toString().length() > 0) {
					currentLength = thisString.toString().length() + 1;
				}
				String[] result = processWord(currentLength, ChatColor.stripColor(word));
				while (result.length > 1) {
					thisString.append(result[0]);
					newDesc.add(DESC_PREFIX + thisString.toString());

					thisString = new StringBuilder();
					result = processWord(0, result[1]);
				}
				thisString.append(result[0]);
			}
			newDesc.add(DESC_PREFIX + thisString.toString());

			if (newDesc.size() > maxLines) {
				return false;
			}
			meta.setLore(newDesc);
			item.setItemMeta(meta);
		}
		return true;
	}

	public static String[] processWord(int currentLength, String word) {
		String first = word;
		String second = null;

		if (first.length() > ItemEdit.getMaxWidth()) {
			int size = ItemEdit.getMaxWidth() - currentLength - 1;

			if (size > 0) {
				StringBuilder hyphenated = new StringBuilder();
				StringBuilder leftovers = new StringBuilder();
				for (int i = 0; i < size; i++) {
					hyphenated.append(first.toCharArray()[i]);
				}
				for (int i = size; i < first.length(); i++) {
					leftovers.append(first.toCharArray()[i]);
				}
				hyphenated.append("-");

				first = hyphenated.toString();
				second = leftovers.toString();
			} else {
				second = first;
				first = "";
			}
		} else if (currentLength + first.length() > ItemEdit.getMaxWidth()) {
			second = first;
			first = "";
		}

		if (currentLength > 0 && first.length() > 0) {
			first = " " + first;
		}

		if (second == null) {
			return new String[] { first };
		} else {
			return new String[] { first, second };
		}
	}

	@Cmd(value="Add a glowing effect to an item as if it were enchanted.", permission="itemedit.glow")
	@Flag(name="mod", description="Adds glow to the item regardless of signature.", permission="itemedit.mod")
	public void glow(CommandSender sender) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			ItemStack item = transSQL.getItemInHand(p);
			if (item != null && item.getType() != Material.AIR) {
				if (!hasFlag("mod") && isSigned(item) && notSignedBy(item, p)) {
					msg(SIGNED_ALREADY);
					return;
				}

				int tokensUsed = transSQL.safeToChargePlayer(p);
				if (ItemUtil.hasCustomTag(item, EDITED_TAG)) {
					tokensUsed = 0;
				} else if (tokensUsed == 0) {
					msg(NO_TOKENS);
					return;
				}

				ItemMeta meta = item.getItemMeta();
				if (meta != null) {
					meta.addEnchant(GLOW, 1, true);
					item.setItemMeta(meta);
				}

				finalizeEdit(p, item, tokensUsed);
				return;
			}
		}
		msg(NO_ITEM);
	}

	// Finalization & Clearing //
	@Cmd(value="Sign an item to lock in the information. Can be cleared later if needed.", permission="itemedit.sign")
	@Flag(name="mod", description="Prevents the username from being written on player approved signs.", permission="itemedit.mod")
	@Flag(name="rp", description="Allows signing with your RP name")
	public void sign(CommandSender sender,
					 @Default("ROLEPLAY") SignType type) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			validate(p.hasPermission(type.permission), NO_SIGNATURE_PERM);
			ItemStack item = transSQL.getItemInHand(p);
			if (item != null && item.getType() != Material.AIR) {
				if (isSigned(item)) {
					msg(SIGNED_ALREADY);
					return;
				}

				int tokensUsed = transSQL.safeToChargePlayer(p);
				if (tokensUsed == 0) {
					msg(NO_TOKENS);
					return;
				} else if (ItemUtil.hasCustomTag(item, EDITED_TAG)) {
					tokensUsed = 0;
				}

				ItemMeta meta = item.getItemMeta();
				if (meta != null) {
					List<String> lore = meta.getLore();
					if (lore == null) {
						lore = new ArrayList<>();
					}

					boolean roleplayName = (type.equals(SignType.ROLEPLAY) || hasFlag("rp"));

					if (type.equals(SignType.PLAYER) || type.equals(SignType.ROLEPLAY) || type.permission.startsWith(ItemEdit.PERMISSION_START + "." + ItemEdit.BONUS_SIGNATURE_PERM)) {
						lore.addAll(SignType.getSignature(p, type, roleplayName, !hasFlag("mod")));
					} else {
						lore.addAll(SignType.getSignature(p, type, roleplayName, true));
					}

					meta.setLore(lore);
					item.setItemMeta(meta);
				}

				ItemUtil.setCustomTag(item, SIGNED_TAG, p.getUniqueId().toString() + ":" + System.currentTimeMillis());
				finalizeEdit(p, item, tokensUsed);
				return;
			}
		}
		msg(NO_ITEM);
	}

	@Cmd(value="Clears edited info. Use flag -mod to override signatures.", permission="itemedit.clear")
	public BaseCommand clear() {
		return CLEAR_COMMANDS;
	}

	@Cmd(value="Moderator access to edited items.", permission="itemedit.mod")
	public BaseCommand staff() {
		return staffCommands;
	}

	// Checks if the item is signed, and if so, if it's by this player. Defaults to true if not signed.
	private static boolean isSigned(ItemStack item) {
		if (transSQL.isItemMonikerSigned(item)) {
			return true;
		} else {
			return ItemUtil.hasCustomTag(item, SIGNED_TAG);
		}
	}

	// Checks if the item was signed by the give player
	private static boolean notSignedBy(ItemStack item, Player p) {
		if (ItemUtil.hasCustomTag(item, SIGNED_TAG)) {
			return !ItemUtil.getCustomTag(item, SIGNED_TAG).equalsIgnoreCase(p.getUniqueId().toString());
		}
		return true;
	}

	// Replaces the item in hand with the given edits after applying the appropriate tags and charging the player.
	private static void finalizeEdit(Player p, ItemStack item, int tokensUsed) {
		// If tokensUsed is 0 it's a clearing and doesn't need to be logged.
		String preString = "";
		if (ItemUtil.hasCustomTag(item, EDITED_TAG)) {
			preString = ItemUtil.getCustomTag(item, EDITED_TAG) + "/";
		}
		ItemUtil.setCustomTag(item, EDITED_TAG, preString + p.getUniqueId().toString() + ":" + System.currentTimeMillis());

		// If the player used an edit token, otherwise the player had to've used a VIP token.
		if (tokensUsed > 0) {
			int tokensLeft = (transSQL.getTokens(p)) - tokensUsed;
			transSQL.addEntry(0L, p, tokensLeft);
		} else if (tokensUsed < 0) {
			transSQL.addEntry(-1L, p, 0);
		}
	}

	/////////////////////
	//// CLEAR CLASS ////
	/////////////////////

	public static class ClearCommands extends BaseCommand {

		@Cmd(value="Clear all custom lore from an item so you can start over.")
		@Flag(name="mod", description="Overrides signature lock on clearing an item.", permission="itemedit.mod")
		public void all(CommandSender sender) {
			String message = clearTypes(sender, hasFlag("mod"), false, false);
			if (message != null) {
				msg(message);
			}
		}

		@Cmd(value="Clear the description from an item.")
		@Flag(name="mod", description="Overrides signature lock on clearing an item.", permission="itemedit.mod")
		public void desc(CommandSender sender) {
			String message = clearTypes(sender, hasFlag("mod"), true, false);
			if (message != null) {
				msg(message);
			}
		}

		@Cmd(value="Clear the signature from an item.")
		@Flag(name="mod", description="Overrides signature lock on clearing an item.", permission="itemedit.mod")
		public void signature(CommandSender sender) {
			String message = clearTypes(sender, hasFlag("mod"), false, true);
			if (message != null) {
				msg(message);
			}
		}

		// Intermediary to avoid duplicate code.
		private String clearTypes(CommandSender sender, boolean mod, boolean descOnly, boolean sigOnly) {
			if (sender instanceof Player) {
				Player p = (Player) sender;
				ItemStack item = transSQL.getItemInHand(p);
				if (item != null && item.getType() != Material.AIR) {
					if (ItemUtil.hasCustomTag(item, EDITED_TAG) || transSQL.isItemMonikerSigned(item)) {
						if (!mod && isSigned(item) && notSignedBy(item, p)) {
							return SIGNED_ALREADY;
						} else {
							clearData(item, descOnly, sigOnly);
							finalizeEdit(p, item, 0);
						}
					}
					return null;
				}
			}
			return NO_ITEM;
		}

		private void clearData(ItemStack item, boolean descOnly, boolean sigOnly) {
			ItemMeta meta = item.getItemMeta();

			if (meta != null) {
				for (Enchantment enc : meta.getEnchants().keySet()) {
					if ((enc instanceof SoulbindEnchant) ||
						(!descOnly && !sigOnly && (enc instanceof Glow))) {
						meta.removeEnchant(enc);
					}
				}

				if (descOnly) {
					meta.setLore(null);
				} else if (sigOnly) {
					List<String> lore = meta.getLore();
					if (lore != null) {
						while (lore.size() > 0) {
							String lastLine = lore.get(lore.size() - 1);
							lore.remove(lore.size() - 1);
							if (lastLine.contains("Approved" + ChatColor.RESET) ||
								(lastLine.contains("Approved") && lastLine.contains(Character.toString((char) 0x2605)))) {
								break;
							}
						}
					}
					meta.setLore(lore);
				} else {
					meta.setLore(null);
					meta.setDisplayName(null);
				}

				if (ItemUtil.hasCustomTag(meta, SIGNED_TAG)) {
					ItemUtil.removeCustomTag(meta, SIGNED_TAG);
				}
				item.setItemMeta(meta);
			}
		}

	}

}

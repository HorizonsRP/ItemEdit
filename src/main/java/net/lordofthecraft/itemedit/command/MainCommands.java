package net.lordofthecraft.itemedit.command;

import co.lotc.core.bukkit.book.BookStream;
import co.lotc.core.bukkit.util.ItemUtil;
import co.lotc.core.command.annotate.Arg;
import co.lotc.core.command.annotate.Cmd;
import co.lotc.core.command.annotate.Flag;
import net.lordofthecraft.itemedit.Glow;
import net.lordofthecraft.itemedit.ItemEdit;
import net.lordofthecraft.itemedit.enums.*;
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
import java.util.UUID;

public class MainCommands extends BaseCommand {

	// Sub command for moderators
	private static StaffCommands staffCommands;

	//private static final ClearCommands CLEAR_COMMANDS = new ClearCommands();

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
	private static final String LINE_BREAK_PARSE = "#%cl:#;@";
	private static final String DESC_PREFIX = ChatColor.GRAY + "" + ChatColor.ITALIC;
	private static final Glow GLOW = new Glow(new NamespacedKey(ItemEdit.get(), ItemEdit.get().getDescription().getName()));

	public MainCommands() {
		nameTooLong = ItemEdit.PREFIX + "That name is too long! Please shorten it to " + ItemEdit.getMaxWidth() + " characters long.";
		staffCommands = new StaffCommands();
	}

	// Edit Types //
	@Cmd(value="Set a custom name for an item.", permission=ItemEdit.PERMISSION_START + ".name")
	@Flag(name="mod", description="Sets the name regardless of signature.", permission=ItemEdit.PERMISSION_START + ".mod")
	@Flag(name="staff", description="Items for staff purposes do not require tokens.", permission=ItemEdit.PERMISSION_START + ".free")
	public void name(CommandSender sender,
					 @Arg(value="Name", description="The name you wish to give the item.") String[] name) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			ItemStack item = ItemEdit.getItemInHand(p);
			if (item != null && item.getType() != Material.AIR) {
				if (!hasFlag("mod") && isSigned(item) && notSignedBy(item, p)) {
					msg(SIGNED_ALREADY);
					return;
				}
				StringBuilder thisName = new StringBuilder();
				for (String s : name) {
					if (thisName.length() > 0) {
						thisName.append(" ").append(s);
					} else {
						thisName.append(s);
					}
				}
				validate(thisName.toString().length() <= ItemEdit.getMaxWidth(), nameTooLong);
				updateDisplayName(item, thisName.toString());
				finalizeEdit(p, item);
				return;
			}
		}
		msg(NO_ITEM);
	}

	@Cmd(value="Set the rarity of the given item.", permission=ItemEdit.PERMISSION_START + ".rarity")
	public void rarity(CommandSender sender, Rarity rarity) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			ItemStack item = ItemEdit.getItemInHand(p);
			if (item != null) {
				updateTags(item, rarity, null, null, null, -1, 0);
				ItemMeta meta = item.getItemMeta();
				if (meta != null) {
					String name = ChatColor.stripColor(meta.getDisplayName());
					updateDisplayName(item, name);
				}
				return;
			}
		}
		msg(NO_ITEM);
	}

	@Cmd(value="Set the quality of the given item.", permission=ItemEdit.PERMISSION_START + ".quality")
	public void quality(CommandSender sender, Quality quality) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			ItemStack item = ItemEdit.getItemInHand(p);
			if (item != null) {
				Tags tags = new Tags(item);
				String oldColor = tags.getQuality().getColor();
				String newColor = quality.getColor();
				updateTags(item, null, quality, null, null, -1, 0);
				updateDescHighlights(item, oldColor, newColor);
				return;
			}
		}
		msg(NO_ITEM);
	}

	@Cmd(value="Set the aura of the given item.", permission=ItemEdit.PERMISSION_START + ".aura")
	public void aura(CommandSender sender, Aura aura, boolean strong) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			ItemStack item = ItemEdit.getItemInHand(p);
			if (item != null) {
				if (strong) {
					updateTags(item, null, null, aura, null, 1, 0);
				} else {
					updateTags(item, null, null, aura, null, 0, 0);
				}
				updateGlow(item, strong);
				return;
			}
		}
		msg(NO_ITEM);
	}

	@Cmd(value="Set the type of the given item.", permission=ItemEdit.PERMISSION_START + ".type")
	public void type(CommandSender sender, Type type) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			ItemStack item = ItemEdit.getItemInHand(p);
			if (item != null) {
				updateTags(item, null, null, null, type, -1, 0);
				return;
			}
		}
		msg(NO_ITEM);
	}

	@Cmd(value="Set the item ID of the given item.", permission=ItemEdit.PERMISSION_START + ".itemid")
	public void itemid(CommandSender sender, int id) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			ItemStack item = ItemEdit.getItemInHand(p);
			if (item != null) {
				updateTags(item, null, null, null, null, -1, id);
				return;
			}
		}
		msg(NO_ITEM);
	}

	@Cmd(value="Add or edit a custom description for an item.", permission=ItemEdit.PERMISSION_START + ".desc")
	public void desc(CommandSender sender) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			ItemStack item = ItemEdit.getItemInHand(p);

			if (item != null && !item.getType().equals(Material.AIR)) {
				ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
				BookMeta meta = (BookMeta) book.getItemMeta();
				if (meta != null) {
					meta.setPages(getDescAsPages(item));
				}
				book.setItemMeta(meta);

				Tags tags = new Tags(item);
				String highlight = tags.getQuality().getColor();
				BookStream stream = new BookStream(p, book, ItemEdit.PREFIX + "Edit in this book!") {
					@Override
					public void onBookClose() {
						List<String> desc = getMeta().getPages();
						completeDesc(item, desc, highlight);
					}
				};

				stream.open(p);
				return;
			}
		}
		msg(NO_ITEM);
	}

	/**
	 * Updates the colour using the given item's existing name.
	 * @param item The item to update.
	 */
	private void updateDisplayName(ItemStack item) {
		String name = null;
		if (item != null) {
			ItemMeta meta = item.getItemMeta();
			if (meta != null) {
				name = ChatColor.stripColor(meta.getDisplayName());
			}
		}

		if (name != null) {
			updateDisplayName(item, name);
		}
	}

	/**
	 * Update the display name of the given item, coloured based on the rarity of the item.
	 * If the item doesn't have any tags on it, it adds the tags then tries to set the name
	 * once again.
	 * @param item The item to name.
	 * @param name The name to use.
	 */
	private void updateDisplayName(ItemStack item, String name) {
		if (ItemUtil.hasCustomTag(item, ItemEdit.INFO_TAG)) {
			Tags tags = new Tags(item);
			ItemMeta meta = item.getItemMeta();
			if (meta != null) {
				meta.setDisplayName(tags.getRarity().getColor() + name);
				item.setItemMeta(meta);
			}
		} else {
			ItemMeta meta = item.getItemMeta();
			if (meta != null) {
				meta.setDisplayName(name);
				item.setItemMeta(meta);
			}
			updateTags(item);
		}
	}

	/**
	 * Re-applies the tags with whatever is on it, or default.
	 * @param item The item to apply basic tags too.
	 */
	private void updateTags(ItemStack item) {
		updateTags(item, null, null, null, null, -1, 0);
	}

	/**
	 * Apply a tag set to the given item. If it has no tags it gives it the default. If a description exists it will
	 * be pushed down by one line.
	 * @param item The item to tag.
	 * @param rarity (Optional) The rarity to set.
	 * @param quality (Optional) The quality to set.
	 * @param aura (Optional) The aura to set.
	 * @param type (Optional) The type to set.
	 * @param strongAura Whether the aura is strong or not. -1 = No Change, 0 = False, 1 = True
	 * @param id Arbitrary item ID for future use with professions.
	 */
	private void updateTags(ItemStack item, Rarity rarity, Quality quality, Aura aura, Type type, int strongAura, int id) {
		ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			List<String> lore = meta.getLore();
			Tags tags = new Tags(item);
			tags.setRarity(rarity);
			tags.setQuality(quality);
			tags.setAura(aura);
			tags.setType(type);
			if (strongAura > -1) {
				if (strongAura > 0) {
					tags.setStrongAura(true);
				} else {
					tags.setStrongAura(false);
				}
			}

			if (id > 0) {
				tags.setLFItemID(id);
			}

			if (lore != null) {
				if (lore.size() > 0) {
					lore.set(0, tags.formatTags());
				} else {
					lore.add(tags.formatTags());
				}
			} else {
				lore = new ArrayList<>();
				lore.add(tags.formatTags());
			}

			meta.setLore(lore);
			item.setItemMeta(meta);
			tags.applyTagToItem(item);
			updateDisplayName(item);
		}
	}

	/**
	 * Toggle glow on or off.
	 * @param item Item to glow.
	 * @param enable Whether to glow or not.
	 */
	private void updateGlow(ItemStack item, boolean enable) {
		ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			if (enable) {
				meta.addEnchant(GLOW, 1, true);
			} else {
				for (Enchantment enc : meta.getEnchants().keySet()) {
					if (enc instanceof Glow) {
						meta.removeEnchant(enc);
					}
				}
			}
			item.setItemMeta(meta);
		}
	}

	/**
	 * Convert a desc into a list of pages for an MC book.
	 * @param item The item to take the description from.
	 * @return Return a list of String wherein each string is one page.
	 */
	private List<String> getDescAsPages(ItemStack item) {
		List<String> output = new ArrayList<>();
		ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			List<String> lore = meta.getLore();
			if (lore != null) {
				int start = 0;
				int end = lore.size();
				if (ItemUtil.hasCustomTag(item, ItemEdit.INFO_TAG)) {
					start++;
				}
				if (ItemUtil.hasCustomTag(item, ItemEdit.SIGNED_TAG)) {
					end--;
				}

				StringBuilder fullStringBuilder = new StringBuilder();
				for (int i = start; i < end; i++) {
					String str = ChatColor.stripColor(lore.get(i));
					if (str.endsWith("-")) {
						fullStringBuilder.append(str.substring(0, str.length()-1));
					} else {
						fullStringBuilder.append(str).append(" ");
					}
				}

				int pages = (int) Math.ceil(fullStringBuilder.length()/255d);
				String fullString = fullStringBuilder.toString();
				for (int i = 1; i <= pages; i++) {
					int j = (i-1)*255;
					int k = i*255;

					if (fullString.length() < k) {
						k = fullString.length();
					}
					output.add(fullString.substring(j, k));
				}
			}
		}
		return output;
	}

	private void updateDescHighlights(ItemStack item, String previousColor, String newColor) {
		if (item != null) {
			ItemMeta meta = item.getItemMeta();
			if (meta != null) {
				List<String> lore = meta.getLore();
				if (lore != null) {
					int start = 0;
					int end = lore.size();
					if (ItemUtil.hasCustomTag(item, ItemEdit.INFO_TAG)) {
						start++;
					}
					if (ItemUtil.hasCustomTag(item, ItemEdit.SIGNED_TAG)) {
						end--;
					}

					for (int i = start; i < end; i++) {
						lore.set(i, lore.get(i).replace(previousColor, newColor));
					}

					meta.setLore(lore);
					item.setItemMeta(meta);
				}
			}
		}
	}

	/**
	 * Take a list of strings from an MC book and convert them into
	 * a description of the appropriate width, then apply said desc
	 * to the given item following format standards.
	 * @param item The item to describe.
	 * @param desc The list of pages as Strings.
	 */
	private void completeDesc(ItemStack item, List<String> desc, String highlight) {

		// Clear linebreaks and combine.
		StringBuilder combinedDesc = new StringBuilder();
		for (String str : desc) {
			if (combinedDesc.length() > 0) {
				combinedDesc.append(" ");
			}
			if (str.contains("\n")) {
				String[] lineBreaks = str.split("\\R", -1);
				for (int i = 0; i < lineBreaks.length; i++) {
					combinedDesc.append(ChatColor.stripColor(lineBreaks[i]));
					if (i < lineBreaks.length-1) {
						combinedDesc.append(" " + LINE_BREAK_PARSE + " ");
					}
				}
			} else {
				combinedDesc.append(ChatColor.stripColor(str));
			}
		}

		// Split into individual words and highlights.
		String[] descByWord = combinedDesc.toString().split(" ");
		for (int i = 0; i < descByWord.length; i++) {
			if (descByWord[i].startsWith("%") || descByWord[i].endsWith("%")) {

				if (descByWord[i].startsWith("%")) {
					descByWord[i] = descByWord[i].substring(1);
				}
				if (descByWord[i].endsWith("%")) {
					descByWord[i] = descByWord[i].substring(0, descByWord[i].length()-1);
				}

				descByWord[i] = (highlight + ChatColor.ITALIC + descByWord[i] + DESC_PREFIX);
			}
		}

		// Update the tags just in case, then format our description into a set of lore.
		updateTags(item);
		List<String> finalDesc = formatDesc(descByWord);

		ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			List<String> lore = meta.getLore();
			if (lore != null) {
				String tags = null;
				String approved = null;
				if (ItemUtil.hasCustomTag(item, ItemEdit.INFO_TAG) && lore.size() > 0) {
					tags = lore.get(0);
				}
				if (ItemUtil.hasCustomTag(item, ItemEdit.SIGNED_TAG) && lore.size() > 0) {
					approved = lore.get(lore.size()-1);
				}

				// Reset our arraylist and fill it in order.
				lore = new ArrayList<>();
				if (tags != null) {
					lore.add(tags);
				}
				lore.addAll(finalDesc);
				if (approved != null) {
					lore.add(approved);
				}

				meta.setLore(lore);
				item.setItemMeta(meta);
			}
		}
	}

	/**
	 * A list of individual words to be converted into a list of string
	 * with hyphenations to keep the width of the desc appropriate.
	 * @param words A list of individual words.
	 * @return The formatted list of strings.
	 */
	private static List<String> formatDesc(String[] words) {
		List<String> desc = new ArrayList<>();
		StringBuilder currentLine = new StringBuilder(DESC_PREFIX);
		int currentLength = 0;

		for (String word : words) {
			if (!word.equals(LINE_BREAK_PARSE)) {
				String[] result = processWord(currentLength, word);

				while (result.length > 1) {
					currentLine.append(result[0]);

					desc.add(currentLine.toString());
					currentLine = new StringBuilder(DESC_PREFIX);
					currentLength = 0;

					result = processWord(0, result[1]);
				}

				currentLine.append(result[0]);
				currentLength += result[0].length();
			} else {
				desc.add(currentLine.toString());
				currentLine = new StringBuilder(DESC_PREFIX);
				currentLength = 0;
			}
		}

		desc.add(currentLine.toString());
		return desc;
	}

	/**
	 * Check to see if the current word needs to be hyphenated or not.
	 * @param currentLength The current length of the lore line,
	 * @param word The word in question.
	 * @return Return either a singular word if it doesn't get cut, or return
	 * the first part to apply, and the second part to carry on to the next line.
	 */
	private static String[] processWord(int currentLength, String word) {
		String first = word;
		String second = null;

		// If we need to preface with a space, then include that as part of the calculation.
		if (currentLength > 0) {
			currentLength++;
		}

		if (first.length() > ItemEdit.getMaxWidth()) {
			// Subtract an extra 1 to make room for the hyphen character.
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

	/*
	// Finalization & Clearing //
	@Cmd(value="Sign an item to lock in the information. Can be cleared later if needed.", permission="itemedit.sign")
	@Flag(name="mod", description="Prevents the username from being written on player approved signs.", permission="itemedit.mod")
	@Flag(name="staff", description="Items for staff purposes do not require tokens.", permission="itemedit.free")
	@Flag(name="rp", description="Allows signing with your RP name")
	public void sign(CommandSender sender,
					 @Default("ROLEPLAY") Signature signature) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			validate(p.hasPermission(signature.permission), NO_SIGNATURE_PERM);
			ItemStack item = transSQL.getItemInHand(p);
			if (item != null && item.getType() != Material.AIR) {
				if (isSigned(item)) {
					msg(SIGNED_ALREADY);
					return;
				}

				// Check if it has a tag first and foremost, otherwise check how many tokens it would cost.
				int tokensUsed = transSQL.safeToChargePlayer(p);
				// If item has been edited and is does not have the paper tag (if relevent), or if
				// a staff is using the -staff flag, then the edit is free regardless.
				if ((ItemUtil.hasCustomTag(item, ItemEdit.EDITED_TAG) &&
					 (item.getType() != Material.PAPER || !ItemUtil.hasCustomTag(item, PAPER_FREEBIE))) ||
					hasFlag("staff")) {
					tokensUsed = 0;
				} else if (tokensUsed == 0) {
					msg(NO_TOKENS);
					return;
				}

				ItemMeta meta = item.getItemMeta();
				if (meta != null) {
					List<String> lore = meta.getLore();
					if (lore == null) {
						lore = new ArrayList<>();
					}

					if (signature.equals(Signature.PLAYER) || signature.permission.startsWith(ItemEdit.PERMISSION_START + "." + ItemEdit.SIGNATURE_PERM)) {
						lore.add(Signature.formatSignature(p, signature, hasFlag("rp"), !hasFlag("mod")));
					} else {
						lore.add(Signature.formatSignature(p, signature, hasFlag("rp"), true));
					}

					meta.setLore(lore);
					item.setItemMeta(meta);
				}

				ItemUtil.setCustomTag(item, SIGNED_TAG, p.getUniqueId().toString() + ":" + System.currentTimeMillis());
				finalizeEdit(p, item);
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
	}*/

	// Checks if the item is signed
	private static boolean isSigned(ItemStack item) {
		return ItemUtil.hasCustomTag(item, ItemEdit.SIGNED_TAG);
	}

	// Checks if the item was signed by the given player
	private static boolean notSignedBy(ItemStack item, Player p) {
		if (ItemUtil.hasCustomTag(item, ItemEdit.SIGNED_TAG)) {
			String uuid = ItemUtil.getCustomTag(item, ItemEdit.SIGNED_TAG).replace(":", " ").split(" ")[0];
			return !UUID.fromString(uuid).equals(p.getUniqueId());
		}
		return true;
	}

	// Replaces the item in hand with the given edits after applying the appropriate tags and charging the player.
	private static void finalizeEdit(Player p, ItemStack item) {
		String preString = "";
		if (ItemUtil.hasCustomTag(item, ItemEdit.EDITED_TAG)) {
			preString = ItemUtil.getCustomTag(item, ItemEdit.EDITED_TAG) + "/";
		}
		ItemUtil.setCustomTag(item, ItemEdit.EDITED_TAG, preString + p.getUniqueId().toString() + ":" + System.currentTimeMillis());
	}/*

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
					if (ItemUtil.hasCustomTag(item, ItemEdit.EDITED_TAG) || transSQL.isItemMonikerSigned(item)) {
						if (!mod && isSigned(item) && notSignedBy(item, p)) {
							return SIGNED_ALREADY;
						} else {
							clearData(item, descOnly, sigOnly);
							finalizeEdit(p, item, 0, false);
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

				ItemUtil.removeCustomTag(meta, SIGNED_TAG);
				item.setItemMeta(meta);
			}
		}

	}*/

}

package net.lordofthecraft.itemedit.command;

import co.lotc.core.bukkit.book.BookStream;
import co.lotc.core.bukkit.util.BookUtil;
import co.lotc.core.bukkit.util.ItemUtil;
import co.lotc.core.command.annotate.Arg;
import co.lotc.core.command.annotate.Cmd;
import co.lotc.core.command.annotate.Default;
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

public class MainCommands extends BaseCommand {

	// Sub command for moderators
	private static StaffCommands staffCommands;

	// Error messages
	private static final String NO_ITEM = ItemEdit.PREFIX + "Please hold an item in your main hand while editing.";
	private static final String NO_TOKENS = ItemEdit.PREFIX + "You do not have enough edit tokens to edit this item!";
	private static final String NO_NAME_SET = ItemEdit.PREFIX + "Please set a custom name before setting a custom colour.";
	private static final String COLOR_ERROR = ItemEdit.PREFIX + "You do not have permission to use this color type.";
	private static final String APPROVED_ALREADY = ItemEdit.PREFIX + "This item has already been signed!";
	private static final String SIGNED_OTHERWISE = ItemEdit.PREFIX + "You were not the one to sign this item!";
	private static final String NO_APPROVAL_PERM = ItemEdit.PREFIX + "You do not have permission to use that signature.";
	private static final String MAX_LENGTH = ItemEdit.PREFIX + "You have reached the maximum length on that item.";
	private static String nameTooLong; // This one is based on Max Width so it's set on instance creation.

	// Application short-hands
	private static final String DESC_PREFIX = ChatColor.GRAY + "" + ChatColor.ITALIC;
	private static final Glow GLOW = new Glow(new NamespacedKey(ItemEdit.get(), ItemEdit.get().getDescription().getName()));

	public MainCommands() {
		nameTooLong = ItemEdit.PREFIX + "That name is too long! Please shorten it to " + ItemEdit.getMaxWidth() + " characters long.";
		staffCommands = new StaffCommands();
	}

	// Edit Types //
	@Cmd(value="Set a custom name for an item.", permission=ItemEdit.PERMISSION_START + ".name")
	public void name(CommandSender sender,
					 @Arg(value="Name", description="The name you wish to give the item.") String[] name) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			ItemStack item = ItemEdit.getItemInHand(p);
			if (item != null && item.getType() != Material.AIR) {
				if (ableToEdit(p, item)) {
					removeApproval(item);
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
				} else {
					msg(APPROVED_ALREADY);
				}
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
				if (ableToEdit(p, item)) {
					removeApproval(item);
					Tags tags = Tags.getTags(item);
					updateTags(item, rarity, null, null, null, Integer.MAX_VALUE, 0);
					ItemMeta meta = item.getItemMeta();
					if (meta != null) {
						String name = ChatColor.stripColor(meta.getDisplayName());
						updateDisplayName(item, name);

						String oldColor = tags.getRarity().getRawColor() + "";
						String newColor = rarity.getRawColor() + "";
						replaceWithinDesc(item, oldColor, newColor);
					}
					finalizeEdit(p, item);
				} else {
					msg(APPROVED_ALREADY);
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
				if (ableToEdit(p, item)) {
					removeApproval(item);
					Tags tags = Tags.getTags(item);
					String oldColor = tags.getQuality().getColor();
					String newColor = quality.getColor();
					replaceWithinDesc(item, oldColor, newColor);
					updateTags(item, null, quality, null, null, Integer.MAX_VALUE, 0);
					finalizeEdit(p, item);
				} else {
					msg(APPROVED_ALREADY);
				}
				return;
			}
		}
		msg(NO_ITEM);
	}

	@Cmd(value="Set the aura of the given item.", permission=ItemEdit.PERMISSION_START + ".aura")
	public void aura(CommandSender sender, Aura aura, @Default(value="0")int auraClass) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			ItemStack item = ItemEdit.getItemInHand(p);
			if (item != null) {
				if (ableToEdit(p, item)) {
					removeApproval(item);
					updateTags(item, null, null, aura, null, auraClass, 0);
					if (auraClass > 0) {
						updateGlow(item, true);
					} else {
						updateGlow(item, false);
					}
					finalizeEdit(p, item);
				} else {
					msg(APPROVED_ALREADY);
				}
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
				if (ableToEdit(p, item)) {
					removeApproval(item);
					updateTags(item, null, null, null, type, Integer.MAX_VALUE, 0);
					finalizeEdit(p, item);
				} else {
					msg(APPROVED_ALREADY);
				}
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
				if (ableToEdit(p, item)) {
					updateTags(item, null, null, null, null, Integer.MAX_VALUE, id);
					finalizeEdit(p, item);
				} else {
					msg(APPROVED_ALREADY);
				}
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
				if (ableToEdit(p, item)) {
					removeApproval(item);
					ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
					BookMeta meta = (BookMeta) book.getItemMeta();
					if (meta != null) {
						meta.setPages(getDescAsPages(item));
					}
					book.setItemMeta(meta);

					Tags tags = Tags.getTags(item);
					String highlight = tags.getQuality().getColor();
					ChatColor bulletpoint = tags.getRarity().getRawColor();

					BookStream stream = new BookStream(p, book, ItemEdit.PREFIX + "Edit in this book!") {
						@Override
						public void onBookClose() {
							completeDesc(item, BookUtil.getPagesAsArray(getMeta().getPages()), highlight, bulletpoint);
						}
					};

					finalizeEdit(p, item);
					stream.open(p);
				} else {
					msg(APPROVED_ALREADY);
				}
				return;
			}
		}
		msg(NO_ITEM);
	}

	@Cmd(value="Approve an item to lock in the information. Can be cleared later if needed.", permission=ItemEdit.PERMISSION_START + ".approve")
	public void approve(CommandSender sender, @Default("PLAYER") Approval approval) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			validate(p.hasPermission(approval.permission + ".use"), NO_APPROVAL_PERM);
			ItemStack item = ItemEdit.getItemInHand(p);
			if (item != null && item.getType() != Material.AIR) {
				if (ableToEdit(p, item)) {
					ItemMeta meta = item.getItemMeta();
					if (meta != null) {
						List<String> lore = meta.getLore();
						if (lore != null) {
							String approvalString = approval.formatApproval(p);
							if (ItemUtil.hasCustomTag(item, ItemEdit.APPROVED_TAG)) {
								lore.set(lore.size() - 1, approvalString);
							} else {
								lore.add(approvalString);
							}
							meta.setLore(lore);
							item.setItemMeta(meta);
							ItemUtil.setCustomTag(item, ItemEdit.APPROVED_TAG, p.getUniqueId().toString() + ":" + System.currentTimeMillis() + ":" + approval.name);
						}
					}
					finalizeEdit(p, item);
				} else {
					msg(APPROVED_ALREADY);
				}
				return;
			}
		}
		msg(NO_ITEM);
	}

	@Cmd(value="Makes the item unable to be placed.", permission=ItemEdit.PERMISSION_START + ".placeable")
	public void placeable(CommandSender sender) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			ItemStack item = ItemEdit.getItemInHand(p);
			if (item != null && !item.getType().equals(Material.AIR)) {
				if (ableToEdit(p, item)) {
					if (ItemUtil.hasCustomTag(item, ItemEdit.NO_PLACEMENT_TAG)) {
						ItemUtil.removeCustomTag(item, ItemEdit.NO_PLACEMENT_TAG);
						msg(ItemEdit.PREFIX + "This item can now be placed down.");
					} else {
						ItemUtil.setCustomTag(item, ItemEdit.NO_PLACEMENT_TAG, "!");
						msg(ItemEdit.PREFIX + "This item can no longer be placed down.");
					}
				}
			}
		}
	}

	@Cmd(value="Moderator access to edited items.", permission=ItemEdit.PERMISSION_START + ".staff")
	public BaseCommand staff() {
		return staffCommands;
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
			Tags tags = Tags.getTags(item);
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
		updateTags(item, null, null, null, null, Integer.MAX_VALUE, 0);
	}

	/**
	 * Apply a tag set to the given item. If it has no tags it gives it the default. If a description exists it will
	 * be pushed down by one line.
	 * @param item The item to tag.
	 * @param rarity (Optional) The rarity to set.
	 * @param quality (Optional) The quality to set.
	 * @param aura (Optional) The aura to set.
	 * @param type (Optional) The type to set.
	 * @param auraClass Whether the aura is normal, minor, or major. 0 = Normal, -1 = Minor, 1 = Major. Integer.MAX_VALUE uses what exists or default.
	 * @param id Arbitrary item ID for future use with professions.
	 */
	private void updateTags(ItemStack item, Rarity rarity, Quality quality, Aura aura, Type type, int auraClass, int id) {
		ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			List<String> lore = meta.getLore();
			Tags tags = Tags.getTags(item);
			tags.setRarity(rarity);
			tags.setQuality(quality);
			tags.setAura(aura);
			tags.setType(type);
			tags.setAuraClass(auraClass);
			tags.setLFItemID(id);

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
	 * Removes the approval tag from a given item, if it has one.
	 * @param item The item to check.
	 */
	private void removeApproval(ItemStack item) {
		if (item != null) {
			if (ItemUtil.hasCustomTag(item, ItemEdit.APPROVED_TAG)) {
				ItemMeta meta = item.getItemMeta();
				if (meta != null) {
					List<String> lore = meta.getLore();
					if (lore != null) {
						lore.remove(lore.size()-1);
						meta.setLore(lore);
						item.setItemMeta(meta);
					}
				}
				ItemUtil.removeCustomTag(item, ItemEdit.APPROVED_TAG);
			}
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
				if (ItemUtil.hasCustomTag(item, ItemEdit.APPROVED_TAG)) {
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

				String fullString = fullStringBuilder.toString();
				fullString = fullString.replace("･", "[*1]");
				fullString = fullString.replace("•", "[*]");
				fullString = fullString.replace("●", "[*4]");
				fullString = fullString.replace("❖", "[**]");

				int pages = (int) Math.ceil(fullString.length()/255d);
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

	/**
	 * Replaces any instance of previousColor within the description with newColor. This
	 * is generally used for updating the highlight colours after the fact.
	 * @param item The item to update.
	 * @param previousColor The string to search for.
	 * @param newColor The string to replace it with.
	 */
	private void replaceWithinDesc(ItemStack item, String previousColor, String newColor) {
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
					if (ItemUtil.hasCustomTag(item, ItemEdit.APPROVED_TAG)) {
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
	private void completeDesc(ItemStack item, String[] desc, String highlight, ChatColor bulletpoint) {
		for (int i = 0; i < desc.length; i++) {
			// Remove any colour.
			desc[i] = ChatColor.stripColor(desc[i]);

			// Add Highlights
			if (desc[i].startsWith("%") || desc[i].endsWith("%")) {
				if (desc[i].startsWith("%")) {
					desc[i] = desc[i].substring(1);
				}
				if (desc[i].endsWith("%")) {
					desc[i] = desc[i].substring(0, desc[i].length()-1);
				}

				desc[i] = (highlight + ChatColor.ITALIC + desc[i] + DESC_PREFIX);
			}

			// Replace bulletpoints.
			if (desc[i].contains("[*")) {
				desc[i] = desc[i].replace("[*1]", bulletpoint + "･" + DESC_PREFIX);
				desc[i] = desc[i].replace("[*]",  bulletpoint + "•" + DESC_PREFIX);
				desc[i] = desc[i].replace("[*4]", bulletpoint + "●" + DESC_PREFIX);
				desc[i] = desc[i].replace("[**]", bulletpoint + "❖" + DESC_PREFIX);
			}
		}

		// Update the tags just in case, then format our description into a set of lore.
		updateTags(item);
		List<String> finalDesc = formatDesc(desc);

		// Grab our current data as it exists then put it back in with the description included.
		ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			List<String> lore = meta.getLore();
			if (lore != null) {
				String tags = null;
				String approved = null;
				if (ItemUtil.hasCustomTag(item, ItemEdit.INFO_TAG) && lore.size() > 0) {
					tags = lore.get(0);
				}
				if (ItemUtil.hasCustomTag(item, ItemEdit.APPROVED_TAG) && lore.size() > 0) {
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
			if (!word.equals("\n")) {
				String[] result = processWord(currentLength, word);

				while (result.length > 1) {
					currentLine.append(result[0]);

					desc.add(currentLine.toString());
					currentLine = new StringBuilder(DESC_PREFIX);
					currentLength = 0;

					result = processWord(0, result[1]);
				}

				currentLine.append(result[0]);
				currentLength += ChatColor.stripColor(result[0]).length();
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
		} else if (currentLength + ChatColor.stripColor(first).length() > ItemEdit.getMaxWidth()) {
			second = first;
			first = "";
		}

		if (currentLength > 0 && ChatColor.stripColor(first).length() > 0) {
			first = " " + first;
		}

		if (second == null) {
			return new String[] { first };
		} else {
			return new String[] { first, second };
		}
	}

	/**
	 * Returns if the player has the override edit permission for the approval.
	 * @param player The player to check for permission.
	 * @param item The item to get the approval from.
	 * @return Return true by default, return false if the player lacks the override
	 * permission node for the approval found.
	 */
	private static boolean ableToEdit(Player player, ItemStack item) {
		if (ItemUtil.hasCustomTag(item, ItemEdit.APPROVED_TAG)) {
			String[] data = ItemUtil.getCustomTag(item, ItemEdit.APPROVED_TAG).split(":");
			if (data.length > 2 && data[2] != null) {
				Approval approval = Approval.getByName(data[2]);
				return player.hasPermission(approval.permission + ".edit");
			}
		}
		return true;
	}

	/**
	 * Adds a formatted edit tag to the given item.
	 * @param player The player who's editing the item.
	 * @param item The item to apply the tag to.
	 */
	private static void finalizeEdit(Player player, ItemStack item) {
		String preString = "";
		if (ItemUtil.hasCustomTag(item, ItemEdit.EDITED_TAG)) {
			preString = ItemUtil.getCustomTag(item, ItemEdit.EDITED_TAG) + "/";
		}
		ItemUtil.setCustomTag(item, ItemEdit.EDITED_TAG, preString + player.getUniqueId().toString() + ":" + System.currentTimeMillis());
	}

}

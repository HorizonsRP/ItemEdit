package net.lordofthecraft.itemedit;

import co.lotc.core.bukkit.util.ItemUtil;
import net.lordofthecraft.itemedit.enums.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {

	private static final String DESC_PREFIX = ChatColor.GRAY + "" + ChatColor.ITALIC;
	private static final Glow GLOW = new Glow(new NamespacedKey(ItemEdit.get(), ItemEdit.get().getDescription().getName()));

	private ItemStack item;
	private Tags tags;

	//// PUBLIC ////
	/**
	 * Starts an Item Builder with a base item material.
	 * @param mat The material to use.
	 * @param amount The amount to set for the item.
	 */
	public ItemBuilder(Material mat, int amount) {
		this.item = new ItemStack(mat);
		this.item.setAmount(amount);
		this.tags = Tags.getTags(this.item);
	}

	/**
	 * Starts an Item Builder with a base item to be edited.
	 * @param base The item to start with.
	 */
	public ItemBuilder(ItemStack base) {
		this.item = base;
		this.tags = Tags.getTags(this.item);
	}

	/**
	 * If any tags have been applied to this ItemBuilder, then one must run this
	 * so the tags are fully applied to the item.
	 */
	public void applyTags() {
		Tags originalTags = Tags.getTags(item);
		tags.applyTagToItem(item);

		ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			String name = ChatColor.stripColor(meta.getDisplayName());
			updateDisplayName(item, name);

			// Check if any of our tags have been updated, and if so, update them.
			boolean tagsUpdate = false;

			// Rarity
			if (!originalTags.getRarity().equals(tags.getRarity())) {
				String oldColor = originalTags.getRarity().getRawColor() + "";
				String newColor = tags.getRarity().getRawColor() + "";
				replaceWithinDesc(item, oldColor, newColor);

				tagsUpdate = true;
			}

			// Quality
			if (!originalTags.getQuality().equals(tags.getQuality())) {
				String oldColor = originalTags.getQuality().getColor();
				String newColor = tags.getQuality().getColor();
				replaceWithinDesc(item, oldColor, newColor);

				tagsUpdate = true;
			}

			// Aura
			if (!originalTags.getAura().equals(tags.getAura()) || originalTags.getAuraClass() != tags.getAuraClass()) {
				if (tags.getAuraClass() > 0) {
					updateGlow(item, true);
				} else {
					updateGlow(item, false);
				}

				tagsUpdate = true;
			}

			// Type or ItemID
			if (!originalTags.getType().equals(tags.getType()) || originalTags.getLFItemID() != tags.getLFItemID()) {
				tagsUpdate = true;
			}

			if (tagsUpdate) {
				updateTags(item, tags);
			}
		}
	}

	/**
	 * @param name Set the name for this builder to the given string.
	 */
	public void setName(String name) {
		updateDisplayName(item, name);
	}

	/**
	 * @param rarity Set the rarity tag for this builder to the given rarity.
	 */
	public void setRarity(Rarity rarity) {
		this.tags.setRarity(rarity);
	}

	/**
	 * @param quality Set the quality tag for this builder to the given quality.
	 */
	public void setQuality(Quality quality) {
		this.tags.setQuality(quality);
	}

	/**
	 * @param aura Set the aura tag for this builder to the given aura.
	 * @param auraClass Determins if the aura is Minor (-1), Normal (0), or Major (1).
	 */
	public void setAura(Aura aura, int auraClass) {
		this.tags.setAura(aura);
		this.tags.setAuraClass(auraClass);
	}

	/**
	 * @param type Set the type tag for this builder to the given type.
	 */
	public void setType(Type type) {
		this.tags.setType(type);
	}

	/**
	 * @param id Set the LF Item ID for this builder to the given ID.
	 */
	public void setItemID(int id) {
		this.tags.setLFItemID(id);
	}

	/**
	 * Update the description of an item to the given list of words, using the highlight
	 * and bulletpoint strings to prefix their appropriate parts.
	 * @param desc A list of words with '\n' as their own individual word for line breaks.
	 * @param highlight The prefix to apply to '%' marked words (start or end).
	 * @param bulletpoint The prefix to apply to bulletpoint text ([*1], [*], [*4], [**]).
	 */
	public void setDesc(String[] desc, String highlight, ChatColor bulletpoint) {
		completeDesc(item, desc, highlight, bulletpoint);
	}

	/**
	 * Adds the approval for an item by the given player.
	 * @param player The player to use for the approval.
	 * @param approval The approval type to use.
	 */
	public void addApproval(Player player, Approval approval) {
		ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			List<String> lore = meta.getLore();
			if (lore != null) {
				String approvalString = approval.formatApproval(player);
				if (ItemUtil.hasCustomTag(item, ItemEdit.APPROVED_TAG)) {
					lore.set(lore.size() - 1, approvalString);
				} else {
					lore.add("");
					lore.add(approvalString);
				}
				meta.setLore(lore);
				item.setItemMeta(meta);
				ItemUtil.setCustomTag(item, ItemEdit.APPROVED_TAG, player.getUniqueId().toString() + ":" + System.currentTimeMillis() + ":" + approval.name);
			}
		}
	}

	/**
	 * Removes the approval tag from an item.
	 */
	public void removeApproval() {
		if (item != null) {
			if (ItemUtil.hasCustomTag(item, ItemEdit.APPROVED_TAG)) {
				ItemMeta meta = item.getItemMeta();
				if (meta != null) {
					List<String> lore = meta.getLore();
					if (lore != null) {
						lore.remove(lore.size()-1);
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
	 * @return Whether this item can currently be placed or not.
	 */
	public boolean isPlaceable() {
		return !ItemUtil.hasCustomTag(item, ItemEdit.NO_PLACEMENT_TAG);
	}

	/**
	 * @return Whether this item can be placed AFTER it has been toggled.
	 */
	public boolean togglePlaceable() {
		setPlaceable(!isPlaceable());
		return isPlaceable();
	}

	/**
	 * @param placeable Manually set whether an item can be placed using a boolean.
	 */
	public void setPlaceable(boolean placeable) {
		if (placeable) {
			ItemUtil.removeCustomTag(item, ItemEdit.NO_PLACEMENT_TAG);
		} else {
			ItemUtil.setCustomTag(item, ItemEdit.NO_PLACEMENT_TAG, "!");
		}
	}

	//// PRIVATE ////
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
		Tags tags = Tags.getTags(item);
		updateTags(item, tags);
	}

	/**
	 * Apply a tag set to the given item. If it has no tags it gives it the default. If a description exists it will
	 * be pushed down by one line.
	 * @param item The item to tag.
	 * @param tags The tags to apply.
	 */
	private void updateTags(ItemStack item, Tags tags) {
		ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			List<String> lore = meta.getLore();

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
						end -= 2;
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
					lore.add("");
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
}

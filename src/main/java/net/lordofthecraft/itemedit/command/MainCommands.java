package net.lordofthecraft.itemedit.command;

import co.lotc.core.bukkit.book.BookStream;
import co.lotc.core.bukkit.util.BookUtil;
import co.lotc.core.bukkit.util.ItemUtil;
import co.lotc.core.command.annotate.Arg;
import co.lotc.core.command.annotate.Cmd;
import co.lotc.core.command.annotate.Default;
import co.lotc.core.command.annotate.Range;
import net.lordofthecraft.itemedit.ItemBuilder;
import net.lordofthecraft.itemedit.ItemEdit;
import net.lordofthecraft.itemedit.enums.*;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
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

	public MainCommands() {
		nameTooLong = ItemEdit.PREFIX + "That name is too long! Please shorten it to " + ItemEdit.getMaxWidth() + " characters long.";
		staffCommands = new StaffCommands();
	}

	// Edit Types //
	@Cmd(value="Set a custom name for an item.", permission=ItemEdit.PERMISSION_START + ".name")
	public void name(CommandSender sender,
					 @Arg(value="Name", description="The name you wish to give the item.") String[] name) {
		if (sender instanceof Player && name != null) {
			Player p = (Player) sender;
			ItemStack item = ItemEdit.getItemInHand(p);
			if (item != null && item.getType() != Material.AIR) {
				if (ableToEdit(p, item)) {
					ItemBuilder builder = new ItemBuilder(item);
					String fullName = String.join(" ", name);
					validate(fullName.length() <= ItemEdit.getMaxWidth(), nameTooLong);
					builder.removeApproval();
					builder.setEditingPlayer(p);
					builder.setName(fullName);
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
		if (sender instanceof Player && rarity != null) {
			Player p = (Player) sender;
			ItemStack item = ItemEdit.getItemInHand(p);
			if (item != null) {
				if (ableToEdit(p, item)) {
					ItemBuilder builder = new ItemBuilder(item);
					builder.removeApproval();
					builder.setEditingPlayer(p);
					builder.setRarity(rarity);
					builder.applyTags();
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
		if (sender instanceof Player && quality != null) {
			Player p = (Player) sender;
			ItemStack item = ItemEdit.getItemInHand(p);
			if (item != null) {
				if (ableToEdit(p, item)) {
					ItemBuilder builder = new ItemBuilder(item);
					builder.removeApproval();
					builder.setEditingPlayer(p);
					builder.setQuality(quality);
					builder.applyTags();
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
					ItemBuilder builder = new ItemBuilder(item);
					builder.removeApproval();
					builder.setEditingPlayer(p);
					builder.setAura(aura, auraClass);
					builder.applyTags();
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
					ItemBuilder builder = new ItemBuilder(item);
					builder.removeApproval();
					builder.setEditingPlayer(p);
					builder.setType(type);
					builder.applyTags();
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
	public void itemid(CommandSender sender, @Range(min=1) int id) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			ItemStack item = ItemEdit.getItemInHand(p);
			if (item != null) {
				if (ableToEdit(p, item)) {
					ItemBuilder builder = new ItemBuilder(item);
					builder.setEditingPlayer(p);
					builder.setItemID(id);
					builder.applyTags();
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
					ItemBuilder builder = new ItemBuilder(item);

					ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
					BookMeta meta = (BookMeta) book.getItemMeta();
					if (meta != null) {
						meta.setPages(getDescAsPages(item));
					}
					book.setItemMeta(meta);

					builder.removeApproval();
					builder.setEditingPlayer(p);

					BookStream stream = new BookStream(p, book, ItemEdit.PREFIX + "Edit in this book! Sign with 'cancel' to cancel.") {
						@Override
						public void onBookClose() {
							builder.setDesc(BookUtil.getPagesAsArray(getMeta().getPages()));
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

	@Cmd(value="Set the style for the created by message.", permission=ItemEdit.PERMISSION_START + ".created")
	public void created(CommandSender sender, @Default("PLAYER") Approval approval) {
		if (sender instanceof Player && approval != null) {
			Player p = (Player) sender;
			validate(p.hasPermission(approval.permission + ".use"), NO_APPROVAL_PERM);
			ItemStack item = ItemEdit.getItemInHand(p);
			if (item != null && item.getType() != Material.AIR) {
				if (ableToEdit(p, item)) {
					ItemBuilder builder = new ItemBuilder(item);
					builder.removeApproval();
					builder.setEditingPlayer(p);
					builder.setEditingPlayerStyle(approval);
					builder.applyTags();
					finalizeEdit(p, item);
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
		if (sender instanceof Player && approval != null) {
			Player p = (Player) sender;
			validate(p.hasPermission(approval.permission + ".use"), NO_APPROVAL_PERM);
			ItemStack item = ItemEdit.getItemInHand(p);
			if (item != null && item.getType() != Material.AIR) {
				if (ableToEdit(p, item)) {
					ItemBuilder builder = new ItemBuilder(item);
					builder.addApproval(p, approval);
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
					ItemBuilder builder = new ItemBuilder(item);
					if (builder.togglePlaceable()) {
						msg(ItemEdit.PREFIX + "This item can now be placed down.");
					} else {
						msg(ItemEdit.PREFIX + "This item can no longer be placed down.");
					}
					return;
				}
			}
		}
		msg(NO_ITEM);
	}

	@Cmd(value="Edit the colour of a potion bottle.", permission=ItemEdit.PERMISSION_START + ".potion")
	public void potionraw(CommandSender sender,
						  @Arg(value="Red") @Range(min=0, max=255) @Default(value="0") int red,
						  @Arg(value="Green") @Range(min=0, max=255) @Default(value="96") int green,
						  @Arg(value="Blue") @Range(min=0, max=255) @Default(value="255") int blue) {
		if (sender instanceof Player) {
			potion(sender, Color.fromRGB(red, green, blue));
		}
	}

	@Cmd(value="Edit the colour of a potion bottle.", permission=ItemEdit.PERMISSION_START + ".potion")
	public void potion(CommandSender sender, Color color) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			ItemStack item = ItemEdit.getItemInHand(p);
			if (item != null && !item.getType().equals(Material.AIR)) {
				if (ableToEdit(p, item)) {
					ItemBuilder builder = new ItemBuilder(item);
					builder.setEditingPlayer(p);
					if (builder.setPotionColor(color)) {
						msg(ItemEdit.PREFIX + "By the power of SCIENCE the liquid is now a different color.");
					} else {
						msg(ItemEdit.PREFIX + "Please hold a potion-type item in your hand.");
					}
					return;
				}
			}
		}
		msg(NO_ITEM);
	}

	@Cmd(value="Toggles an item being breakable.", permission=ItemEdit.PERMISSION_START + ".unbreakable")
	public void unbreakable(CommandSender sender) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			ItemStack item = ItemEdit.getItemInHand(p);
			if (item != null && !item.getType().equals(Material.AIR)) {
				if (ableToEdit(p, item)) {
					ItemBuilder builder = new ItemBuilder(item);
					if (builder.toggleUnbreakable()) {
						msg(ItemEdit.PREFIX + "This item is now unbreakable.");
					} else {
						msg(ItemEdit.PREFIX + "This item is now breakable.");
					}
					return;
				}
			}
		}
		msg(NO_ITEM);
	}

	@Cmd(value="Moderator access to edited items.", permission=ItemEdit.PERMISSION_START + ".staff")
	public BaseCommand staff() {
		return staffCommands;
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
				if (ItemUtil.hasCustomTag(meta, ItemEdit.INFO_TAG)) {
					start++;
				}

				if (ItemUtil.hasCustomTag(meta, ItemEdit.EDITED_TAG) && end >= 2) {
					plugin.getLogger().info("Has been edited.");
					end = end - 2;
					if (ItemUtil.hasCustomTag(meta, ItemEdit.APPROVED_TAG) && end >= 1) {
						plugin.getLogger().info("Has been approved.");
						end = end - 1;
					}
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

				output = BookUtil.getPagesForString(fullStringBuilder.toString());
			}
		}
		return output;
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
		ItemUtil.setCustomTag(item, ItemEdit.MODERN_TAG, ItemEdit.ITEM_VERSION);
	}

}

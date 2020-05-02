package net.lordofthecraft.itemedit.enums;

import co.lotc.core.agnostic.Sender;
import net.lordofthecraft.itemedit.ItemEdit;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public enum Rarity {

	VCOMMON(  false, false, "Very Common", ChatColor.DARK_GRAY,   ItemEdit.PERMISSION_START + "." + ItemEdit.RARITY_PERM + ".verycommon"),
	COMMON(   false, false, "Common",      ChatColor.WHITE,       ItemEdit.PERMISSION_START + "." + ItemEdit.RARITY_PERM + ".common"),
	UNCOMMON( false, false, "Uncommon",    ChatColor.DARK_GREEN,  ItemEdit.PERMISSION_START + "." + ItemEdit.RARITY_PERM + ".uncommon"),
	RARE(     false, false, "Rare",        ChatColor.DARK_AQUA,   ItemEdit.PERMISSION_START + "." + ItemEdit.RARITY_PERM + ".rare"),
	WONDROUS( false, true,  "Wondrous",    ChatColor.DARK_BLUE,   ItemEdit.PERMISSION_START + "." + ItemEdit.RARITY_PERM + ".wondrous"),
	MYTHICAL( false, true,  "Mythical",    ChatColor.DARK_PURPLE, ItemEdit.PERMISSION_START + "." + ItemEdit.RARITY_PERM + ".mythical"),
	LEGENDARY(false, true,  "Legendary",   ChatColor.GOLD,        ItemEdit.PERMISSION_START + "." + ItemEdit.RARITY_PERM + ".legendary"),
	FABLED(   false, true,  "Fabled",      ChatColor.DARK_RED,    ItemEdit.PERMISSION_START + "." + ItemEdit.RARITY_PERM + ".fabled"),
	LASAGNA(  true,  true,  "Lasagna",     ChatColor.BLACK,       ItemEdit.PERMISSION_START + "." + ItemEdit.RARITY_PERM + ".lasagna");

	public static Rarity DEFAULT = VCOMMON;

	private boolean magic;
	private boolean bold;
	private String name;
	private ChatColor color;
	private String permission;

	Rarity(boolean magic, boolean bold, String name, ChatColor color, String permission) {
		this.magic = magic;
		this.bold = bold;
		this.name = name;
		this.color = color;
		this.permission = permission;
	}

	public String getTag() {
		return getColor() + this.name;
	}

	public String getName() {
		return this.name;
	}
	public String getPermission() {
		return this.permission;
	}
	public String getColor() {
		String output = this.color + "";
		if (this.bold) {
			output += ChatColor.BOLD;
		}
		if (this.magic) {
			output += ChatColor.MAGIC;
		}
		return output;
	}

	// STATIC //
	public static Rarity getByName(String name) {
		for (Rarity rarity : values()) {
			if (rarity.getName().equalsIgnoreCase(name)) {
				return rarity;
			}
		}
		return null;
	}

	public static List<String> getAvailable(Sender player) {
		ArrayList<String> list = new ArrayList<>();
		for (Rarity rarity : values()) {
			if (player.hasPermission(rarity.permission)) {
				list.add(rarity.name);
			}
		}
		return list;
	}

}

package net.lordofthecraft.itemedit.enums;

import org.bukkit.ChatColor;

public enum Rarity {

	VCOMMON(  false, false, "Very Common", ChatColor.DARK_GRAY),
	COMMON(   false, false, "Common",      ChatColor.WHITE),
	UNCOMMON( false, false, "Uncommon",    ChatColor.DARK_GREEN),
	RARE(     false, false, "Rare",        ChatColor.DARK_AQUA),
	WONDROUS( false, true,  "Wondrous",    ChatColor.DARK_BLUE),
	MYTHICAL( false, true,  "Mythical",    ChatColor.DARK_PURPLE),
	LEGENDARY(false, true,  "Legendary",   ChatColor.GOLD),
	FABLED(   false, true,  "Fabled",      ChatColor.DARK_RED),
	LASAGNA(  true,  true,  "Lasagna",     ChatColor.BLACK);

	private boolean magic;
	private boolean bold;
	private String name;
	private ChatColor color;

	Rarity(boolean magic, boolean bold, String name, ChatColor color) {
		this.magic = magic;
		this.bold = bold;
		this.name = name;
		this.color = color;
	}

	public String getTag() {
		String color = this.color + "";
		if (this.bold) {
			color += ChatColor.BOLD;
		}
		if (this.magic) {
			color += ChatColor.MAGIC;
		}
		return color + this.name;
	}

	public String getName() {
		return this.name;
	}

	private static Rarity getByName(String name) {
		for (Rarity rarity : values()) {
			if (rarity.getName().equalsIgnoreCase(name)) {
				return rarity;
			}
		}
		return null;
	}

}

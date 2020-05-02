package net.lordofthecraft.itemedit.enums;

import org.bukkit.ChatColor;

public enum Quality {

	NATURAL( false, "Natural",     ChatColor.GRAY),
	FAILURE( false, "Failure",     ChatColor.DARK_GRAY),
	CHEAP(   false, "Cheap",       ChatColor.WHITE),
	ADEPT(   false, "Adept",       ChatColor.GREEN),
	MODERATE(false, "Moderate",    ChatColor.AQUA),
	FINE(    false, "Fine",        ChatColor.BLUE),
	ARTISIAN(false, "Artisian",    ChatColor.LIGHT_PURPLE),
	MSTRCRFT(false, "Mastercraft", ChatColor.YELLOW),
	PERFECT( false, "Perfect",     ChatColor.RED),
	LASAGNA( true,  "Lasagna",     ChatColor.BLACK);

	private boolean magic;
	private String name;
	private ChatColor color;

	Quality(boolean magic, String name, ChatColor color) {
		this.magic = magic;
		this.name = name;
		this.color = color;
	}

	public String getTag() {
		String color = this.color + "";
		if (this.magic) {
			color += ChatColor.MAGIC;
		}
		return color + this.name;
	}

	public String getName() {
		return this.name;
	}

	private static Quality getByName(String name) {
		for (Quality quality : values()) {
			if (quality.getName().equalsIgnoreCase(name)) {
				return quality;
			}
		}
		return null;
	}

}

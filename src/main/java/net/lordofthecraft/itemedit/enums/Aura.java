package net.lordofthecraft.itemedit.enums;

import org.bukkit.ChatColor;

public enum Aura {

	MUNDANE("Mundane", ChatColor.DARK_GRAY),
	ARCANE("Arcane", ChatColor.GOLD),
	ELEMENTAL("Elemental", ChatColor.GREEN),
	DEMONIC("Demonic", ChatColor.DARK_RED),
	CELESTIAL("Celestial", ChatColor.YELLOW),
	FEYLIKE("Feylike", ChatColor.LIGHT_PURPLE),
	CHAOTIC("Chaotic", ChatColor.DARK_BLUE),
	DEIFIC("Deific", ChatColor.DARK_AQUA),
	DRACONIC("Draconic", ChatColor.RED),
	WEBBED("Webbed", ChatColor.WHITE),
	SERPENTINE("Serpentine", ChatColor.DARK_GREEN),
	VOIDAL("Voidal", ChatColor.DARK_PURPLE),
	ABYSSAL("Abyssal", ChatColor.GRAY);

	private String name;
	private ChatColor color;

	Aura(String name, ChatColor color) {
		this.name = name;
		this.color = color;
	}

	public String getTag(boolean bold) {
		return this.color + this.name;
	}

	public String getName() {
		return this.name;
	}

	private static Aura getByName(String name) {
		for (Aura aura : values()) {
			if (aura.getName().equalsIgnoreCase(name)) {
				return aura;
			}
		}
		return null;
	}

}

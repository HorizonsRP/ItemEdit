package net.lordofthecraft.itemedit.enums;

import org.bukkit.ChatColor;

public enum Type {

	FLORA("Flora"),
	FAUNA("Fauna"),
	CREATURE("Creature"),
	WOOD("Wood"),
	METAL("Metal"),
	ALCHEMY("Alchemical"),
	CLOTH("Cloth"),
	TOOL("Tool"),
	FOOD("Food"),
	GEM("Gem"),
	MISC("Miscellanea");


	private String name;

	Type(String name) {
		this.name = name;
	}

	public String getTag(boolean bold) {
		return ChatColor.DARK_GRAY + this.name;
	}

	public String getName() {
		return this.name;
	}

	private static Type getByName(String name) {
		for (Type type : values()) {
			if (type.getName().equalsIgnoreCase(name)) {
				return type;
			}
		}
		return null;
	}

}

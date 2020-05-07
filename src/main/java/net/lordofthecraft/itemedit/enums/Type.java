package net.lordofthecraft.itemedit.enums;

import co.lotc.core.agnostic.Sender;
import net.lordofthecraft.itemedit.ItemEdit;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public enum Type {

	FLORA(   "Flora",       ItemEdit.PERMISSION_START + "." + ItemEdit.TYPE_PERM + ".flora"),
	FAUNA(   "Fauna",       ItemEdit.PERMISSION_START + "." + ItemEdit.TYPE_PERM + ".fauna"),
	CREATURE("Creature",    ItemEdit.PERMISSION_START + "." + ItemEdit.TYPE_PERM + ".creature"),
	WOOD(    "Wood",        ItemEdit.PERMISSION_START + "." + ItemEdit.TYPE_PERM + ".wood"),
	METAL(   "Metal",       ItemEdit.PERMISSION_START + "." + ItemEdit.TYPE_PERM + ".metal"),
	GEM(     "Gem",         ItemEdit.PERMISSION_START + "." + ItemEdit.TYPE_PERM + ".gem"),
	ALCHEMY( "Alchemical",  ItemEdit.PERMISSION_START + "." + ItemEdit.TYPE_PERM + ".alch"),
	CLOTH(   "Cloth",       ItemEdit.PERMISSION_START + "." + ItemEdit.TYPE_PERM + ".cloth"),
	FOOD(    "Food",        ItemEdit.PERMISSION_START + "." + ItemEdit.TYPE_PERM + ".food"),
	TOOL(    "Tool",        ItemEdit.PERMISSION_START + "." + ItemEdit.TYPE_PERM + ".tool"),
	APPAREL( "Apparel",     ItemEdit.PERMISSION_START + "." + ItemEdit.TYPE_PERM + ".apparel"),
	MISC(    "Miscellanea", ItemEdit.PERMISSION_START + "." + ItemEdit.TYPE_PERM + ".misc");

	public static Type DEFAULT = MISC;

	public String name;
	public String permission;

	Type(String name, String permission) {
		this.name = name;
		this.permission = permission;
	}

	/**
	 * @return Returns the color and name together for the given Type.
	 */
	public String getTag() {
		return ChatColor.DARK_GRAY + this.name.replace('_', ' ');
	}

	/**
	 * @param name The type name to search for.
	 * @return Returns the Type object if found for the given name.
	 */
	public static Type getByName(String name) {
		for (Type type : values()) {
			if (type.name.equalsIgnoreCase(name)) {
				return type;
			}
		}
		return null;
	}

	/**
	 * @param player The player who's permissions we reference.
	 * @return Returns a list of Types based on the given player's permissions.
	 */
	public static List<String> getAvailable(Sender player) {
		ArrayList<String> list = new ArrayList<>();
		for (Type type : values()) {
			if (player.hasPermission(type.permission)) {
				list.add(type.name);
			}
		}
		return list;
	}

}

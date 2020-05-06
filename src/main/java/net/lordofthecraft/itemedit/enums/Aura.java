package net.lordofthecraft.itemedit.enums;

import co.lotc.core.agnostic.Sender;
import net.lordofthecraft.itemedit.ItemEdit;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public enum Aura {

	MUNDANE(   "Mundane",    ChatColor.DARK_GRAY,   ItemEdit.PERMISSION_START + "." + ItemEdit.AURA_PERM + ".mundane"),
	ARCANE(    "Arcane",     ChatColor.LIGHT_PURPLE,ItemEdit.PERMISSION_START + "." + ItemEdit.AURA_PERM + ".arcane"),
	ELEMENTAL( "Elemental",  ChatColor.GREEN,       ItemEdit.PERMISSION_START + "." + ItemEdit.AURA_PERM + ".elemental"),
	DEMONIC(   "Demonic",    ChatColor.DARK_RED,    ItemEdit.PERMISSION_START + "." + ItemEdit.AURA_PERM + ".demonic"),
	CELESTIAL( "Celestial",  ChatColor.GOLD,        ItemEdit.PERMISSION_START + "." + ItemEdit.AURA_PERM + ".celestial"),
	FEYLIKE(   "Feylike",    ChatColor.YELLOW,      ItemEdit.PERMISSION_START + "." + ItemEdit.AURA_PERM + ".feylike"),
	CHAOTIC(   "Chaotic",    ChatColor.DARK_BLUE,   ItemEdit.PERMISSION_START + "." + ItemEdit.AURA_PERM + ".chaotic"),
	DEIFIC(    "Deific",     ChatColor.DARK_AQUA,   ItemEdit.PERMISSION_START + "." + ItemEdit.AURA_PERM + ".deific"),
	DRACONIC(  "Draconic",   ChatColor.RED,         ItemEdit.PERMISSION_START + "." + ItemEdit.AURA_PERM + ".draconic"),
	WEBBED(    "Webbed",     ChatColor.WHITE,       ItemEdit.PERMISSION_START + "." + ItemEdit.AURA_PERM + ".webbed"),
	SERPENTINE("Serpentine", ChatColor.DARK_GREEN,  ItemEdit.PERMISSION_START + "." + ItemEdit.AURA_PERM + ".serpentine"),
	VOIDAL(    "Voidal",     ChatColor.DARK_PURPLE, ItemEdit.PERMISSION_START + "." + ItemEdit.AURA_PERM + ".voidal"),
	ABYSSAL(   "Abyssal",    ChatColor.GRAY,        ItemEdit.PERMISSION_START + "." + ItemEdit.AURA_PERM + ".abyssal");

	public static Aura DEFAULT = MUNDANE;

	public String name;
	private ChatColor color;
	public String permission;

	Aura(String name, ChatColor color, String permission) {
		this.name = name;
		this.color = color;
		this.permission = permission;
	}

	public String getTag(boolean bold) {
		String color = this.color + "";
		if (bold) {
			color += ChatColor.BOLD;
		}
		return color + this.name;
	}
	public static Aura getByName(String name) {
		for (Aura aura : values()) {
			if (aura.name.equalsIgnoreCase(name)) {
				return aura;
			}
		}
		return null;
	}

	public static List<String> getAvailable(Sender player) {
		ArrayList<String> list = new ArrayList<>();
		for (Aura aura : values()) {
			if (player.hasPermission(aura.permission)) {
				list.add(aura.name);
			}
		}
		return list;
	}

}

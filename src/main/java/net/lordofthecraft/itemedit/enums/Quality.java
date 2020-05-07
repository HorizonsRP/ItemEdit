package net.lordofthecraft.itemedit.enums;

import co.lotc.core.agnostic.Sender;
import net.lordofthecraft.itemedit.ItemEdit;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public enum Quality {

	NATURAL( false, "Natural",     ChatColor.GRAY,         ItemEdit.PERMISSION_START + "." + ItemEdit.QUALITY_PERM + ".natural"),
	FAILURE( false, "Failure",     ChatColor.DARK_GRAY,    ItemEdit.PERMISSION_START + "." + ItemEdit.QUALITY_PERM + ".failure"),
	CHEAP(   false, "Cheap",       ChatColor.WHITE,        ItemEdit.PERMISSION_START + "." + ItemEdit.QUALITY_PERM + ".cheap"),
	ADEPT(   false, "Adept",       ChatColor.GREEN,        ItemEdit.PERMISSION_START + "." + ItemEdit.QUALITY_PERM + ".adept"),
	MODERATE(false, "Moderate",    ChatColor.AQUA,         ItemEdit.PERMISSION_START + "." + ItemEdit.QUALITY_PERM + ".moderate"),
	FINE(    false, "Fine",        ChatColor.BLUE,         ItemEdit.PERMISSION_START + "." + ItemEdit.QUALITY_PERM + ".fine"),
	ARTISIAN(false, "Artisian",    ChatColor.LIGHT_PURPLE, ItemEdit.PERMISSION_START + "." + ItemEdit.QUALITY_PERM + ".artisian"),
	MSTRCRFT(false, "Mastercraft", ChatColor.YELLOW,       ItemEdit.PERMISSION_START + "." + ItemEdit.QUALITY_PERM + ".mastercraft"),
	PERFECT( false, "Perfect",     ChatColor.RED,          ItemEdit.PERMISSION_START + "." + ItemEdit.QUALITY_PERM + ".perfect"),
	LASAGNA( true,  "Lasagna",     ChatColor.BLACK,        ItemEdit.PERMISSION_START + "." + ItemEdit.QUALITY_PERM + ".lasagna");

	public static Quality DEFAULT = CHEAP;

	private boolean magic;
	public String name;
	private ChatColor color;
	public String permission;

	Quality(boolean magic, String name, ChatColor color, String permission) {
		this.magic = magic;
		this.name = name;
		this.color = color;
		this.permission = permission;
	}

	public String getTag() {
		String color = this.color + "";
		if (this.magic) {
			color += ChatColor.MAGIC;
		}
		return color + this.name;
	}
	public String getColor() {
		String output = this.color + "";
		if (this.magic) {
			output += ChatColor.MAGIC;
		}
		return output;
	}
	public ChatColor getRawColor() {
		return this.color;
	}

	public static Quality getByName(String name) {
		for (Quality quality : values()) {
			if (quality.name.equalsIgnoreCase(name)) {
				return quality;
			}
		}
		return null;
	}

	public static List<String> getAvailable(Sender player) {
		ArrayList<String> list = new ArrayList<>();
		for (Quality quality : values()) {
			if (player.hasPermission(quality.permission)) {
				list.add(quality.name);
			}
		}
		return list;
	}

}

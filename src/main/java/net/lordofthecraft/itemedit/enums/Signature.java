package net.lordofthecraft.itemedit.enums;

import co.lotc.core.agnostic.Sender;
import net.korvic.rppersonas.RPPersonas;
import net.lordofthecraft.itemedit.ItemEdit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public enum Signature {

	// STAFF AND DEFAULT SIGNATURES
	ADMIN("ADMIN", ChatColor.DARK_RED, "An Admin", "❃", ItemEdit.PERMISSION_START + ".admin"),
	MOD("MOD", ChatColor.BLUE, "A Moderator", "✲", ItemEdit.PERMISSION_START + ".mod"),
	TECH("TECH", ChatColor.DARK_AQUA, "A Technician", "❖", ItemEdit.PERMISSION_START + ".tech"),
	LORE("LORE", ChatColor.DARK_GREEN, "A Lore Herald", "✺", ItemEdit.PERMISSION_START + ".lore"),
	EVENT("EVENT", ChatColor.GREEN, "An Event Actor", "✺", ItemEdit.PERMISSION_START + ".event"),
	BUILD("BUILD", ChatColor.GOLD, "A Builder", "❂", ItemEdit.PERMISSION_START + ".build"),
	DESIGN("DESIGN", ChatColor.DARK_PURPLE, "A Designer", "❣", ItemEdit.PERMISSION_START + ".design"),
	PLAYER("PLAYER", ChatColor.GRAY, "A Player", "◎", ItemEdit.PERMISSION_START + ".use");

	// VIP SIGNATURES
	/*FLEURDELIS("FLEUR-DE-LIS", ChatColor.GRAY, "Player", "⚜", ItemEdit.PERMISSION_START + "." + ItemEdit.BONUS_SIGNATURE_PERM + ".fleurdelis"),
	SUN("SUN", ChatColor.GRAY, "Player", "☀", ItemEdit.PERMISSION_START + "." + ItemEdit.BONUS_SIGNATURE_PERM + ".sun"),
	TWINKLE("TWINKLE", ChatColor.GRAY, "Player", "✦", ItemEdit.PERMISSION_START + "." + ItemEdit.BONUS_SIGNATURE_PERM + ".twinkle"),
	MUSIC("MUSIC", ChatColor.GRAY, "Player", "♫", ItemEdit.PERMISSION_START + "." + ItemEdit.BONUS_SIGNATURE_PERM + ".music"),
	FLOWER("FLOWER", ChatColor.GRAY, "Player", "❀", ItemEdit.PERMISSION_START + "." + ItemEdit.BONUS_SIGNATURE_PERM + ".flower"),
	TRIDENT("TRIDENT", ChatColor.GRAY, "Player", "♆", ItemEdit.PERMISSION_START + "." + ItemEdit.BONUS_SIGNATURE_PERM + ".trident"),
	SNOWFLAKE("SNOWFLAKE", ChatColor.GRAY, "Player", "❄", ItemEdit.PERMISSION_START + "." + ItemEdit.BONUS_SIGNATURE_PERM + ".snowflake"),
	PLUS("PLUS", ChatColor.GRAY, "Player", "✚", ItemEdit.PERMISSION_START + "." + ItemEdit.BONUS_SIGNATURE_PERM + ".plus"),
	SKULL("SKULL", ChatColor.GRAY, "Player", "☠", ItemEdit.PERMISSION_START + "." + ItemEdit.BONUS_SIGNATURE_PERM + ".skull"),
	PEACE("PEACE", ChatColor.GRAY, "Player", "☮", ItemEdit.PERMISSION_START + "." + ItemEdit.BONUS_SIGNATURE_PERM + ".peace"),
	STAR("STAR", ChatColor.GRAY, "Player", "✪", ItemEdit.PERMISSION_START + "." + ItemEdit.BONUS_SIGNATURE_PERM + ".star"),
	CURRENCY("CURRENCY", ChatColor.GRAY, "Player", "¤", ItemEdit.PERMISSION_START + "." + ItemEdit.BONUS_SIGNATURE_PERM + ".currency"),
	CROWNS("CROWNS", ChatColor.GRAY, "Player", "♚", ItemEdit.PERMISSION_START + "." + ItemEdit.BONUS_SIGNATURE_PERM + ".crowns"),
	DIAMOND("DIAMOND", ChatColor.GRAY, "Player", "◆", ItemEdit.PERMISSION_START + "." + ItemEdit.BONUS_SIGNATURE_PERM + ".diamond"),
	ORNATE("ORNATE", ChatColor.GRAY, "Player", "❁", ItemEdit.PERMISSION_START + "." + ItemEdit.BONUS_SIGNATURE_PERM + ".ornate"),
	SNOWMAN("SNOWMAN", ChatColor.GRAY, "Player", "☃", ItemEdit.PERMISSION_START + "." + ItemEdit.BONUS_SIGNATURE_PERM + ".snowman");*/

	public final String name;
	public final ChatColor color;
	public final String aRank;
	public final String affixes;
	public final String permission;

	private static final String DGRAY_ITALIC = ChatColor.DARK_GRAY + "" + ChatColor.ITALIC;

	Signature(String name, ChatColor color, String aRank, String affixes, String permission) {
		this.name = name;
		this.color = color;
		this.aRank = aRank;
		this.affixes = ChatColor.RESET + "" + ChatColor.DARK_AQUA + affixes + ChatColor.RESET;
		this.permission = permission;
	}
	public static final Signature DEFAULT = PLAYER;

	public static String getSignature(Player p, Signature type, boolean roleplay, boolean showRealName) {
		String name = p.getName();
		if (roleplay) {
			//Grab Persona name.
			if (ItemEdit.get().getServer().getPluginManager().isPluginEnabled("RPPersonas")) {
				name = RPPersonas.get().getPersonaHandler().getLoadedPersona(p).getNickName();
			}

			// Still add this even if we fail to find any persona name.
			name += DGRAY_ITALIC + " (" + type.color + ChatColor.ITALIC + p.getName() + DGRAY_ITALIC + ")";
		}

		return DGRAY_ITALIC + "Created By " + type.aRank + " " + type.color + name;
	}

	public static Signature typeFromString(String string) {
		for (Signature type : values()) {
			if (type.name.equalsIgnoreCase(string)) {
				return type;
			}
		}

		return DEFAULT;
	}

	public static List<String> getAvailableTypes(Sender player) {
		ArrayList<String> list = new ArrayList<>();
		for (Signature type : values()) {
			if (player.hasPermission(type.permission)) {
				list.add(type.name);
			}
		}
		return list;
	}

}

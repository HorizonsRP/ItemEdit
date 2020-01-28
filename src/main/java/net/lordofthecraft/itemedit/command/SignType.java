package net.lordofthecraft.itemedit.command;

import co.lotc.core.agnostic.Sender;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.itemedit.ItemEdit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum SignType {

	// STAFF AND DEFAULT SIGNATURES
	ADMIN("ADMIN", ChatColor.DARK_RED, "Admin Approved", "❃", ItemEdit.PERMISSION_START + ".admin"),
	DEV("DEV", ChatColor.GOLD, "Developer Approved", "❖", ItemEdit.PERMISSION_START + ".dev"),
	MOD("MOD", ChatColor.BLUE, "Moderator Approved", "✲", ItemEdit.PERMISSION_START + ".mod"),
	STORY("STORY", ChatColor.GREEN, "Story Team Approved", "✺", ItemEdit.PERMISSION_START + ".st"),
	EVENT("EVENT", ChatColor.GREEN, "Story Actor Approved", "✺", ItemEdit.PERMISSION_START + ".et"),
	LORE("LORE", ChatColor.GREEN, "Story Writer Approved", "✺", ItemEdit.PERMISSION_START + ".lt"),
	WORLD("WORLD", ChatColor.DARK_AQUA, "World Team Approved", "❂", ItemEdit.PERMISSION_START + ".world"),
	COMMUNITY("COMMUNITY", ChatColor.LIGHT_PURPLE, "Community Team Approved", "❣", ItemEdit.PERMISSION_START + ".comm"),
	PLAYER("PLAYER", ChatColor.GRAY, "Player Approved", "○", ItemEdit.PERMISSION_START + ".use"),
	ROLEPLAY("ROLEPLAY", ChatColor.GRAY, "Player Approved", "◎", ItemEdit.PERMISSION_START + ".use"),

	// VIP SIGNATURES
	FLEURDELIS("FLEUR-DE-LIS", ChatColor.GRAY, "Player Approved", "⚜", ItemEdit.PERMISSION_START + "." + ItemEdit.BONUS_SIGNATURE_PERM + ".fleurdelis"),
	SUN("SUN", ChatColor.GRAY, "Player Approved", "☀", ItemEdit.PERMISSION_START + "." + ItemEdit.BONUS_SIGNATURE_PERM + ".sun"),
	TWINKLE("TWINKLE", ChatColor.GRAY, "Player Approved", "✦", ItemEdit.PERMISSION_START + "." + ItemEdit.BONUS_SIGNATURE_PERM + ".twinkle"),
	MUSIC("MUSIC", ChatColor.GRAY, "Player Approved", "♫", ItemEdit.PERMISSION_START + "." + ItemEdit.BONUS_SIGNATURE_PERM + ".music"),
	FLOWER("FLOWER", ChatColor.GRAY, "Player Approved", "❀", ItemEdit.PERMISSION_START + "." + ItemEdit.BONUS_SIGNATURE_PERM + ".flower"),
	TRIDENT("TRIDENT", ChatColor.GRAY, "Player Approved", "♆", ItemEdit.PERMISSION_START + "." + ItemEdit.BONUS_SIGNATURE_PERM + ".trident"),
	SNOWFLAKE("SNOWFLAKE", ChatColor.GRAY, "Player Approved", "❄", ItemEdit.PERMISSION_START + "." + ItemEdit.BONUS_SIGNATURE_PERM + ".snowflake"),
	PLUS("PLUS", ChatColor.GRAY, "Player Approved", "✚", ItemEdit.PERMISSION_START + "." + ItemEdit.BONUS_SIGNATURE_PERM + ".plus"),
	SKULL("SKULL", ChatColor.GRAY, "Player Approved", "☠", ItemEdit.PERMISSION_START + "." + ItemEdit.BONUS_SIGNATURE_PERM + ".skull"),
	PEACE("PEACE", ChatColor.GRAY, "Player Approved", "☮", ItemEdit.PERMISSION_START + "." + ItemEdit.BONUS_SIGNATURE_PERM + ".peace"),
	STAR("STAR", ChatColor.GRAY, "Player Approved", "✪", ItemEdit.PERMISSION_START + "." + ItemEdit.BONUS_SIGNATURE_PERM + ".star"),
	CURRENCY("CURRENCY", ChatColor.GRAY, "Player Approved", "¤", ItemEdit.PERMISSION_START + "." + ItemEdit.BONUS_SIGNATURE_PERM + ".currency"),
	CROWNS("CROWNS", ChatColor.GRAY, "Player Approved", "♚", ItemEdit.PERMISSION_START + "." + ItemEdit.BONUS_SIGNATURE_PERM + ".crowns"),
	DIAMOND("DIAMOND", ChatColor.GRAY, "Player Approved", "◆", ItemEdit.PERMISSION_START + "." + ItemEdit.BONUS_SIGNATURE_PERM + ".diamond"),
	ORNATE("ORNATE", ChatColor.GRAY, "Player Approved", "❁", ItemEdit.PERMISSION_START + "." + ItemEdit.BONUS_SIGNATURE_PERM + ".ornate"),
	SNOWMAN("SNOWMAN", ChatColor.GRAY, "Player Approved", "☃", ItemEdit.PERMISSION_START + "." + ItemEdit.BONUS_SIGNATURE_PERM + ".snowman");

	public final String name;
	public final ChatColor color;
	public final String approved;
	public final String affixes;
	public final String permission;

	SignType(String name, ChatColor color, String approved, String affixes, String permission) {
		this.name = name;
		this.color = color;
		this.approved = approved;
		this.affixes = ChatColor.RESET + "" + ChatColor.RED + affixes + ChatColor.RESET;
		this.permission = permission;
	}
	public static final SignType DEFAULT = PLAYER;

	public static List<String> getSignature(Player p, SignType type, boolean roleplay, boolean showRealName) {
		String name = p.getName();
		if (roleplay) {
			//Grab Persona name.
			if (ItemEdit.get().getServer().getPluginManager().isPluginEnabled("ArcheCore")) {
				Persona persona = ArcheCore.getPersona(p);
				name = persona.getName();
			}

			// Still add this even if we fail to find any persona name.
			name += " (" + p.getName() + ") ";
		}

		List<String> output = new ArrayList<>();
		output.add(type.affixes + type.color + type.approved + type.affixes);

		StringBuilder whoSigned = new StringBuilder();
		List<String> desc = new ArrayList<>();
		desc.add("Signed");
		desc.add("by");
		desc.addAll(Arrays.asList(name.split(" ")));

		for (int i = 0; i < desc.size(); i++) {
			String color;
			if (i > 1) {
				color = type.color + "";
			} else {
				color = ChatColor.DARK_GRAY + "";
			}

			if (i == desc.size()-1 && roleplay) {
				if (showRealName) {
					color += ChatColor.ITALIC;
				} else {
					break;
				}
			}

			int currentLength = 0;
			if (whoSigned.toString().length() > 0) {
				currentLength = whoSigned.toString().length() + 1;
			}

			String[] result = MainCommands.processWord(currentLength, desc.get(i));
			while(result.length > 1) {
				whoSigned.append(color).append(result[0]);
				output.add(whoSigned.toString());

				whoSigned = new StringBuilder();
				result = MainCommands.processWord(0, color + result[1]);
			}
			whoSigned.append(color).append(result[0]);
		}
		whoSigned.append(ChatColor.DARK_GRAY).append(".");
		output.add(whoSigned.toString());

		return output;
	}

	public static SignType typeFromString(String string) {
		for (SignType type : values()) {
			if (type.name.equalsIgnoreCase(string)) {
				return type;
			}
		}

		return DEFAULT;
	}

	public static List<String> getAvailableTypes(Sender player) {
		ArrayList<String> list = new ArrayList<>();
		for (SignType type : values()) {
			if (player.hasPermission(type.permission)) {
				list.add(type.name);
			}
		}
		return list;
	}

}

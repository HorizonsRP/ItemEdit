package net.lordofthecraft.itemedit.command;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.itemedit.ItemEdit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum SignType {
	ADMIN(ChatColor.DARK_RED, "Admin Approved", "❃", "itemedit.admin"),
	DEV(ChatColor.GOLD, "Developer Approved", "❖", "itemedit.dev"),
	MOD(ChatColor.BLUE, "Moderator Approved", "✲", "itemedit.mod"),
	STORY(ChatColor.GREEN, "Story Team Approved", "✺", "itemedit.st"),
	WORLD(ChatColor.DARK_AQUA, "World Team Approved", "❂", "itemedit.world"),
	COMMUNITY(ChatColor.LIGHT_PURPLE, "Community Team Approved", "❣", "itemedit.comm"),
	PLAYER(ChatColor.GRAY, "Player Approved", "○", "itemedit.use"),
	ROLEPLAY(ChatColor.GRAY, "Player Approved", "◎", "itemedit.use");

	public final ChatColor color;
	public final String approved;
	public final String affixes;
	public final String permission;

	SignType(ChatColor color, String approved, String affixes, String permission) {
		this.color = color;
		this.approved = approved;
		this.affixes = ChatColor.RESET + "" + ChatColor.RED + affixes + ChatColor.RESET;
		this.permission = permission;
	}
	public static final SignType DEFAULT = PLAYER;

	public static List<String> getSignature(Player p, SignType type, boolean showRealName) {
		String name = p.getDisplayName();
		if (type.equals(SignType.ROLEPLAY)) {
			if (ItemEdit.get().getServer().getPluginManager().isPluginEnabled("ArcheCore")) {
				Persona persona = ArcheCore.getPersona(p);
				name = persona.getChatName() + " (" + name + ")";
			} else {
				return getSignature(p, SignType.PLAYER, showRealName);
			}
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
			if (i == desc.size()-1 && type.equals(SignType.ROLEPLAY)) {
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

	public static String[] getTypes() {
		return new String[] {
				"ADMIN",
				"DEV",
				"MOD",
				"STORY",
				"WORLD",
				"COMMUNITY",
				"PLAYER",
				"ROLEPLAY"
		};
	}

	public static SignType typeFromString(String string) {
		if (string.equalsIgnoreCase("ADMIN")) {
			return ADMIN;
		} else if (string.equalsIgnoreCase("DEV")) {
			return DEV;
		} else if (string.equalsIgnoreCase("MOD")) {
			return MOD;
		} else if (string.equalsIgnoreCase("STORY")) {
			return STORY;
		} else if (string.equalsIgnoreCase("WORLD")) {
			return WORLD;
		} else if (string.equalsIgnoreCase("COMMUNITY")) {
			return COMMUNITY;
		} else if (string.equalsIgnoreCase("PLAYER")) {
			return PLAYER;
		} else if (string.equalsIgnoreCase("ROLEPLAY")) {
			return ROLEPLAY;
		}

		return DEFAULT;
	}

}

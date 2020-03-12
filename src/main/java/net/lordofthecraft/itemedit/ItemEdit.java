package net.lordofthecraft.itemedit;

import co.lotc.core.bukkit.command.Commands;
import net.lordofthecraft.itemedit.command.MainCommands;
import net.lordofthecraft.itemedit.command.SignType;
import net.lordofthecraft.itemedit.sqlite.TransactionsSQL;
import org.apache.commons.codec.binary.Base64;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ItemEdit extends JavaPlugin {

	public static final String PREFIX = ChatColor.AQUA + "";
	public static final String ALT_COLOR = ChatColor.GOLD + "";
	public static final String PERMISSION_START = "itemedit";
	public static final String BONUS_SIGNATURE_PERM = "signature";

	public static final boolean DEBUGGING = true;

	private static int maxWidth;
	public static int getMaxWidth() {
		return maxWidth;
	}
	private static int maxLines;
	public static int getMaxLines() {
		return maxLines;
	}
	private static int refreshTime;
	public static int getRefreshTime() {
		return refreshTime;
	}

	private static ItemEdit instance;
	public static ItemEdit get() {
		return instance;
	}
	private static TransactionsSQL transaction;
	public static TransactionsSQL getTransaction() {
		return transaction;
	}

	@Override
	public void onEnable() {
		instance = this;
		saveDefaultConfig();

		// Grab the width of each item.
		if (this.getConfig().getInt("MAX-WIDTH") > 10) {
			maxWidth = this.getConfig().getInt("MAX-WIDTH");
		} else {
			maxWidth = 50;
		}

		// Grab the max lines for descriptions.
		if (this.getConfig().getInt("MAX-LINES") > 2) {
			maxLines = this.getConfig().getInt("MAX-LINES");
		} else {
			maxLines = 15;
		}

		// Grab our refresh time for VIP tokens.
		if (this.getConfig().getInt("REFRESH-TIME-IN-DAYS") >= 0) {
			refreshTime = this.getConfig().getInt("REFRESH-TIME-IN-DAYS");
		} else {
			refreshTime = 7;
		}

		// Load the tokens we have.
		loadCurrentTokens();

		// Register our auto-complete and glow enchant.
		registerParameters();
		registerGlow();

		// Initiate commands if everything else has been successful.
		Commands.build(getCommand("edit"), () -> new MainCommands(transaction));
	}

	@Override
	public void onDisable() {
		// Plugin shutdown logic
	}

	// Auto-populate our SignTypes
	private void registerParameters() {
		Commands.defineArgumentType(SignType.class)
				.defaultName("Type")
				.completer((s,$) -> SignType.getAvailableTypes(s))
				.mapperWithSender((sender, type) -> SignType.typeFromString(type))
				.register();
	}

	// Registers our glow enchantment for /edit glow use.
	private void registerGlow() {
		try {
			Field f = Enchantment.class.getDeclaredField("acceptingNew");
			f.setAccessible(true);
			f.set(null, true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		try {
			NamespacedKey key = new NamespacedKey(this, getDescription().getName());
			// 1.12.2 Glow glow = new Glow(70);
			Glow glow = new Glow(key);
			Enchantment.registerEnchantment(glow);
		}
		catch (IllegalArgumentException ignore) {
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	// Loads the tokens and transactions we have registered for each player.
	// Also deletes any transactions from REFRESH_TIME ago.
	private void loadCurrentTokens() {
		transaction = new TransactionsSQL(this);
		transaction.load();
	}

	// Config Editors
	public void setRefreshTime(int i) {
		refreshTime = i;
		getConfig().set("REFRESH-TIME-IN-DAYS", i);
		saveConfig();
	}

	public void setMaxWidth(int i) {
		maxWidth = i;
		getConfig().set("MAX-WIDTH", i);
		saveConfig();
	}

	//// HOOKS ////

	// CHANGE
	// Changes the tokens of a player by a given amount (positive or negative).
	public static boolean changeTokens(int amount, String playerName) {
		UUID p = TransactionsSQL.getUUIDByName(playerName);
		boolean output = false;
		if (p != null) {
			output = changeTokens(amount, p);
		}
		return output;
	}
	public static boolean changeTokens(int amount, Player player) {
		return changeTokens(amount, player.getUniqueId());
	}
	public static boolean changeTokens(int amount, UUID player) {
		int newAmount = (getTokens(player) + amount);
		if (newAmount < 0) {
			newAmount = 0;
		}
		return setTokens(newAmount, player);
	}

	// SET
	// Sets a player's tokens to a value.
	public static boolean setTokens(int amount, String playerName) {
		UUID p = TransactionsSQL.getUUIDByName(playerName);
		boolean output = false;
		if (p != null) {
			output = setTokens(amount, p);
		}
		return output;
	}
	public static boolean setTokens(int amount, Player player) {
		return setTokens(amount, player.getUniqueId());
	}
	public static boolean setTokens(int amount, UUID player) {
		transaction.addEntry(0, player, amount);
		return true;
	}

	// GET
	// Provides how many tokens a player has.
	public static int getTokens(String playerName) {
		UUID p = TransactionsSQL.getUUIDByName(playerName);
		int output = Integer.MIN_VALUE;
		if (p != null) {
			output = getTokens(p);
		}
		return output;
	}
	public static int getTokens(Player p) {
		return getTokens(p.getUniqueId());
	}
	public static int getTokens(UUID p) {
		return transaction.getTokens(p);
	}

}

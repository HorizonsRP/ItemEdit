package net.lordofthecraft.itemedit;

import co.lotc.core.bukkit.command.Commands;
import net.lordofthecraft.itemedit.command.MainCommands;
import net.lordofthecraft.itemedit.command.SignType;
import net.lordofthecraft.itemedit.sqlite.TransactionsSQL;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.libs.org.apache.commons.codec.binary.Base64;
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

	public static final boolean DEBUGGING = false;
	public static final boolean LEGACY_CHECK = false;

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

		// Try to grab all Moniker Tokens
		if (LEGACY_CHECK) {
			loadLegacyTokens();
		}

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


	// TODO :: Moniker Legacy Garbage
	// Tries to run through all available profiles in Moniker to grab and store their tokens.
	private void loadLegacyTokens() {
		try (Stream<Path> paths = Files.walk(Paths.get(getDataFolder().getPath() + "/LegacyFiles"))) {
			List<Path> files = paths.filter(Files::isRegularFile).collect(Collectors.toList());
			for (Path path : files) {
				if (DEBUGGING) {
					getServer().getLogger().info(path.toString());
				}
				File playerFile = new File(path.toString());
				try {
					String monikerXML = getMonikerXML(playerFile);
					if (monikerXML != null) {
						if (DEBUGGING) {
							getServer().getLogger().info("Found Moniker data.");
						}
						int tokens = getTokensFromXML(monikerXML);
						if (tokens > 0) {
							changeTokens(tokens, UUID.fromString(playerFile.getName()));
						}
					}
				} catch (Exception ignore) {

				}

				if (!playerFile.delete()) {
					getServer().getLogger().warning("Failed to delete file at " + path.toString() + ". Consider manually deleting.");
				}
			}
		} catch (IOException e) {
			if (DEBUGGING) {
				e.printStackTrace();
			}
		}
	}

	private int getTokensFromXML(String parsed) {
		int output = 0;
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new InputSource(new StringReader(parsed)));
			doc.getDocumentElement().normalize();
			NodeList nodeList = doc.getFirstChild().getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (DEBUGGING) {
					getServer().getLogger().info(node.getNodeName());
				}
				if (node.getNodeName().equalsIgnoreCase("cachedVars")) {
					NodeList subNodeList = node.getChildNodes();
					for (int j = 0; j < subNodeList.getLength(); j++) {
						Node subNode = subNodeList.item(j);
						if (DEBUGGING) {
							getServer().getLogger().info(subNode.getNodeName());
						}
						if (subNode.getNodeName().equalsIgnoreCase("entry")) {
							NodeList subSubNodeList = subNode.getChildNodes();
							for (int k = 0; k < subSubNodeList.getLength(); k++) {
								Node subSubNode = subSubNodeList.item(k);
								if (DEBUGGING) {
									getServer().getLogger().info(subSubNode.getNodeName());
								}
								if (subSubNode.getNodeName().equalsIgnoreCase("net.minegrid.feature.moniker.MonikerProfile")) {
									NodeList finalNodeList = subSubNode.getChildNodes();
									for (int l = 0; l < finalNodeList.getLength(); l++) {
										Node finalNode = finalNodeList.item(l);
										if (DEBUGGING) {
											getServer().getLogger().info(finalNode.getNodeName());
										}
										if (finalNode.getNodeName().equalsIgnoreCase("tokens")) {
											output = Integer.parseInt(finalNode.getTextContent());
											if (DEBUGGING) {
												getServer().getLogger().info("" + output);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			if (DEBUGGING) {
				e.printStackTrace();
			}
		}
		return output;
	}

	private String getMonikerXML(File file) {
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(file);
			doc.getDocumentElement().normalize();

			NodeList nodeList = doc.getElementsByTagName("rawData");
			String base64XML = null;
			for (int i = 0; i < nodeList.getLength(); i++) {

				Node node = nodeList.item(i);
				base64XML = grabMonikerBase64(node.getChildNodes());
				if (base64XML != null) {
					break;
				}
			}

			if (base64XML != null) {
				return new String(Base64.decodeBase64(base64XML));
			}
			return null;
		} catch (Exception e) {
			if (DEBUGGING) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private String grabMonikerBase64(NodeList list) {
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				if (element.getElementsByTagName("string").item(0).getTextContent().equalsIgnoreCase("moniker")) {
					return element.getElementsByTagName("string").item(1).getTextContent();
				}
			}
		}
		return null;
	}

}

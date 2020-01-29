package net.lordofthecraft.itemedit.sqlite;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

import co.lotc.core.util.MojangCommunicator;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.Node;
import me.lucko.luckperms.api.User;
import net.lordofthecraft.itemedit.ItemEdit;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.minegrid.obelisk.api.Obelisk;

public class TransactionsSQL {

	public static Connection connection;

	private static final long DAY_IN_MS = (1000 * 60 * 60 * 24);
	private static ItemEdit plugin;
	private String SQLiteTableName = "trnsac_table";
	public String getTable() {
		return SQLiteTableName;
	}
	private String SQLiteTokensTable;
	private String dbname;

	public TransactionsSQL(ItemEdit instance){
		plugin = instance;
		dbname = instance.getConfig().getString("SQLite.Filename", "edits");
		SQLiteTokensTable = "CREATE TABLE IF NOT EXISTS " + SQLiteTableName + " (\n" +
							"    TIME NUM NOT NULL,\n" +
							"    PLAYER TEXT NOT NULL,\n" +
							"    TOKENS INT NOT NULL\n" +
							");";
	}


	// SQL creation stuff
	public Connection getSQLConnection() {
		File dataFolder = new File(plugin.getDataFolder(), dbname + ".db");
		if (!dataFolder.exists()){
			try {
				dataFolder.createNewFile();
			} catch (IOException e) {
				plugin.getLogger().log(Level.SEVERE, "File write error: " + dbname + ".db");
			}
		}
		try {
			if (connection != null && !connection.isClosed()) {
				return connection;
			}
			Class.forName("org.sqlite.JDBC");
			String locale = dataFolder.toString();
			if (ItemEdit.DEBUGGING) {
				plugin.getLogger().info("LOCALE: " + locale);
			}
			connection = DriverManager.getConnection("jdbc:sqlite:" + locale);
			return connection;
		} catch (SQLException ex) {
			if (ItemEdit.DEBUGGING) {
				plugin.getLogger().log(Level.SEVERE, "SQLite exception on initialize", ex);
			}
		} catch (ClassNotFoundException ex) {
			if (ItemEdit.DEBUGGING) {
				plugin.getLogger().log(Level.SEVERE, "You need the SQLite JBDC library. Google it. Put it in /lib folder.");
			}
		}
		return null;
	}

	public void load() {
		connection = getSQLConnection();
		try {
			Statement s = connection.createStatement();
			s.execute(SQLiteTokensTable);
			s.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		initialize();
	}

	public void initialize(){
		connection = getSQLConnection();
		try {
			String stmt;
			stmt = "SELECT * FROM " + SQLiteTableName + ";";
			PreparedStatement ps = connection.prepareStatement(stmt);
			ResultSet rs = ps.executeQuery();
			close(ps, rs);
			this.deleteOldVIPEntries();
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
		}
	}

	// Inserts or Replaces depending on what type of entry we're adding.
	// Time of 0 means it's storing the amount of edit tokens a player has.
	// Time of -1 defaults to current system time.
	// Any non-zero time is treated as a VIP edit entry.
	public void addEntry(long time, Player player, int tokens) {
		addEntry(time, player.getUniqueId(), tokens);
	}
	public void addEntry(long time, UUID player, int tokens) {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			if (time == 0) {
				conn = getSQLConnection();
				ps = conn.prepareStatement("DELETE FROM " + SQLiteTableName + " WHERE TIME=0 AND PLAYER='" + player.toString() + "';");
				ps.executeUpdate();
				ps.close();
			}

			conn = getSQLConnection();
			ps = conn.prepareStatement("INSERT INTO " + SQLiteTableName + " (TIME,PLAYER,TOKENS) VALUES(?,?,?)");
			if (time == -1) {
				ps.setLong(1, System.currentTimeMillis());
			} else {
				ps.setLong(1, time);
			}
			ps.setString(2, player.toString());
			ps.setInt(3, tokens);
			ps.executeUpdate();
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
			}
		}

		deleteOutdatedTokenEntries();
	}

	//// FETCHING ////

	// Retrieves the amount of tokens a player has, as per our database.
	public int getTokens(Player player) {
		return getTokens(player.getUniqueId());
	}
	public int getTokens(UUID uuid) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		deleteOutdatedTokenEntries();

		try {
			conn = getSQLConnection();
			String stmt;
			stmt = "SELECT * FROM " + SQLiteTableName + " WHERE TIME=0 AND PLAYER='" + uuid.toString() + "';";

			ps = conn.prepareStatement(stmt);
			rs = ps.executeQuery();

			int result = 0;
			while (rs.next()) {
				if (rs.getInt("TOKENS") > result) {
					result = rs.getInt("TOKENS");
				}
			}
			return result;
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
			}
		}
		return 0;
	}

	// Fetches how many VIP edits are stored in our database for a given player.
	public int getVIPUsedAmount(Player player) {
		return getVIPUsedAmount(player.getUniqueId());
	}
	public int getVIPUsedAmount(UUID player) {
		Connection conn = getSQLConnection();
		PreparedStatement ps = null;
		int output = 0;

		try {
			ps = conn.prepareStatement(getVIPUsedStatement(player));
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				output++;
			}
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
			}
		}

		return output;
	}

	// Fetches the days until the next rename token will refresh for the given player.
	public int getDaysUntilRename(Player player) {
		return getDaysUntilRename(player.getUniqueId());
	}
	public int getDaysUntilRename(UUID player) {
		Connection conn = getSQLConnection();
		PreparedStatement ps = null;
		long output = 0;

		try {
			ps = conn.prepareStatement(getVIPUsedStatement(player));
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				long timeDifference = System.currentTimeMillis() - rs.getLong("TIME");
				if (timeDifference > output) {
					output = timeDifference;
				}
			}
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
		}  finally {
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
			}
		}

		return (int) (ItemEdit.getRefreshTime() - (output / DAY_IN_MS));
	}

	// Builds a statement to get all VIP usages for a given UUID.
	private String getVIPUsedStatement(UUID player) {
		deleteOldVIPEntries();

		String stmt;
		stmt = "SELECT * FROM " + SQLiteTableName + " WHERE TIME>0 AND PLAYER='" + player.toString() + "';";

		return stmt;
	}

	//// REMOVAL ////

	// Removes all nonsensical entries (No need to store that someone has 0 tokens).
	public void deleteOutdatedTokenEntries() {
		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = getSQLConnection();
			String stmt;
			stmt = "DELETE FROM " + SQLiteTableName + " WHERE TIME=0 AND TOKENS=0;";
			ps = conn.prepareStatement(stmt);
			ps.executeUpdate();
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
			}
		}
	}

	// Removes all stored data on a given player.
	public void deleteAllFromPlayer(Player player) {
		deleteAllFromPlayer(player.getUniqueId());
	}
	public void deleteAllFromPlayer(UUID player) {
		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = getSQLConnection();
			String stmt;
			stmt = "DELETE FROM " + SQLiteTableName + " WHERE PLAYER='" + player.toString() + "';";
			ps = conn.prepareStatement(stmt);
			ps.executeUpdate();
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
			}
		}
	}

	// Removes all VIP entries that have elapsed the auto refresh time.
	public void deleteOldVIPEntries() {
		Connection conn = null;
		PreparedStatement ps = null;

		long limitToDelete = System.currentTimeMillis() - (ItemEdit.getRefreshTime() * DAY_IN_MS);

		try {
			conn = getSQLConnection();
			String stmt;
			stmt = "DELETE FROM " + SQLiteTableName + " WHERE TIME>0 AND TIME<" + limitToDelete + ";";

			ps = conn.prepareStatement(stmt);
			ps.executeUpdate();

		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
			}
		}
	}

	//// UTILITY ////

	// Close two statements.
	private static void close(PreparedStatement ps,ResultSet rs){
		try {
			if (ps != null)
				ps.close();
			if (rs != null)
				rs.close();
		} catch (SQLException ex) {
			Errors.close(plugin, ex);
		}
	}

	// Returns a player by their username if possible.
	public static Player getPlayerByName(String playerName) {
		Player p = Bukkit.getPlayer(playerName);
		if (p == null) {
			UUID uuid = getUUIDByName(playerName);
			if (uuid != null) {
				p = Bukkit.getPlayer(uuid);
				if (p == null) {
					p = Bukkit.getOfflinePlayer(uuid).getPlayer();
				}
			}
		}
		return p;
	}

	// Returns a player UUID by their username if possible.
	public static UUID getUUIDByName(String playerName) {
		UUID p = null;
		try {
			p = MojangCommunicator.requestPlayerUUID(playerName);
		} catch (IOException e) {
			if (ItemEdit.DEBUGGING) {
				e.printStackTrace();
			}
		}
		return p;
	}

	// Returns the highest VIP tokens permission a player has.
	public int getVipTokensTotal(Player p) {
		return getVipTokensTotal(p.getUniqueId());
	}
	public int getVipTokensTotal(UUID p) {
		return getMaxPermission(p, "vip", 0);
	}

	// Returns the highest Edit Lines permission a player has.
	public int getMaxLines(Player p) {
		return getMaxLines(p.getUniqueId());
	}
	public int getMaxLines(UUID p) {
		return getMaxPermission(p, "length", ItemEdit.getMaxLines());
	}

	// Gets the max permission value of a player.
	public int getMaxPermission(UUID player, String permission, int defaultAmount) {
		int output = defaultAmount;
		User user = LuckPerms.getApi().getUser(player);
		if (user != null) {
			String thisPermission = ItemEdit.PERMISSION_START + "." + permission;
			for (Node node : user.getAllNodes()) {
				if (node.getValue()) {
					if (node.getPermission().startsWith(thisPermission)) {
						String[] split = node.getPermission().replace('.', ' ').split(" ");
						if (split.length >= 3 && Integer.parseInt(split[2]) > output) {
							output = Integer.parseInt(split[2]);
						}
					} else if (node.getPermission().equalsIgnoreCase(ItemEdit.PERMISSION_START + ".unlimited")) {
						output = Integer.MAX_VALUE;
						break;
					}
				}
			}
		}
		return output;
	}

	// Returns what type of charge we can apply to this player (doesn't charge yet).
	public int safeToChargePlayer(Player p) {
		return safeToChargePlayer(p.getUniqueId());
	}
	public int safeToChargePlayer(UUID p) {
		// Has VIP tokens still.
		if (getVIPUsedAmount(p) < getVipTokensTotal(p)) {
			return -1;

		// Has edit tokens.
		} else if (getTokens(p) > 0) {
			return 1;
		}
		// Doesn't have ANY tokens.
		return 0;
	}

	// Grabs the item in main hand for the player.
	public ItemStack getItemInHand(Player player) {
		if (player.getInventory().getItemInMainHand().getType() != Material.AIR) {
			return player.getInventory().getItemInMainHand();
		}
		return null;
	}

	public boolean isItemMonikerSigned(ItemStack stack) {
		try {
			for (String line : Objects.requireNonNull(Objects.requireNonNull(stack.getItemMeta()).getLore()))
				if (line.contains("Approved") && line.contains(Character.toString((char) 0x2605)))
					return true;
		} catch (Exception ignored) {

		}
		return false;
	}

}
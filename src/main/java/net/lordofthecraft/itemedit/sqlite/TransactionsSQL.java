package net.lordofthecraft.itemedit.sqlite;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;

import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.Node;
import me.lucko.luckperms.api.User;
import net.lordofthecraft.itemedit.ItemEdit;
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
							"    TIME NUM PRIMARY KEY,\n" +
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

	// Save info
	public void addEntry(long time, Player player, int tokens) {
		if (time == 0 && tokens <= 0) {
			deleteTokensEntryFromPlayer(player);
		} else {
			Connection conn = null;
			PreparedStatement ps = null;
			String stmt = "INSERT";
			if (time == 0) {
				stmt += " OR REPLACE";
			}
			stmt += " INTO " + SQLiteTableName + " (TIME,PLAYER,TOKENS) VALUES(?,?,?)";

			try {
				conn = getSQLConnection();
				ps = conn.prepareStatement(stmt);
				if (time == -1) {
					ps.setLong(1, System.currentTimeMillis());
				} else {
					ps.setLong(1, time);
				}
				ps.setString(2, player.getUniqueId().toString());
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
		}
	}

	// Retrieve info
	public int getTokens(Player player) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<String> output = new ArrayList<>();

		try {
			conn = getSQLConnection();
			String stmt;
			stmt = "SELECT * FROM " + SQLiteTableName + " WHERE TIME=0 AND PLAYER='" + player.getUniqueId().toString() + "';";

			ps = conn.prepareStatement(stmt);
			rs = ps.executeQuery();

			while (rs.next()) {
				if (rs.getString("PLAYER").equalsIgnoreCase(player.getUniqueId().toString())) {
					return rs.getInt("TOKENS");
				}
			}
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

	public String getVIPUsedStatement(Player player) {
		deleteOldVIPEntries();

		String stmt;
		stmt = "SELECT * FROM " + SQLiteTableName + " WHERE TIME>0 AND PLAYER='" + player.getUniqueId().toString() + "';";

		return stmt;
	}

	public int getVIPUsedAmount(Player player) {
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

	public int getDaysUntilRename(Player player) {
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

	// Remove info
	public void deleteTokensEntryFromPlayer(Player player) {
		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = getSQLConnection();
			String stmt;
			stmt = "DELETE FROM " + SQLiteTableName + " WHERE TIME=0 AND PLAYER='" + player.getUniqueId().toString() + "';";
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

	public void deleteOldVIPEntries() {
		Connection conn = null;
		PreparedStatement ps = null;

		long limitToDelete = System.currentTimeMillis() - (ItemEdit.getRefreshTime() * DAY_IN_MS);

		try {
			conn = getSQLConnection();
			String stmt;
			stmt = "DELETE FROM " + SQLiteTableName + " WHERE TIME<" + limitToDelete + " AND TIME>0;";

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

	public void deleteAllFromPlayer(Player player) {
		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = getSQLConnection();
			String stmt;
			stmt = "DELETE FROM " + SQLiteTableName + " WHERE PLAYER='" + player.getUniqueId().toString() + "';";
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

	// Utility
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

	public int getVipTokensTotal(Player p) {
		return getMaxPermission(p, "vip");
	}

	public int getMaxLines(Player p) {
		return getMaxPermission(p, "length");
	}

	// Gets the max value for an itemedit.PERMISSION of the player
	public int getMaxPermission(Player player, String permission) {
		int output = 0;
		if (!player.hasPermission("itemedit.unlimited")) {
			User user = LuckPerms.getApi().getUser(player.getUniqueId());
			if (user != null) {
				String thisPermission = "itemedit." + permission;
				for (Node node : user.getAllNodes()) {
					if (node.getValue() && node.getPermission().startsWith(thisPermission)) {
						String[] split = node.getPermission().replace('.', ' ').split(" ");
						if (split.length >= 3 && Integer.parseInt(split[2]) > output) {
							output = Integer.parseInt(split[2]);
						}
					}
				}
			}
		} else {
			output = Integer.MAX_VALUE;
		}
		return output;
	}

	// Returns what type of charge we can apply to this player (doesn't charge yet).
	public int safeToChargePlayer(Player p) {
		grabMonikerTokens(p);
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

	// Grabs main hand first, if not exists, grabs off-hand. Result will place into main hand regardless.
	public ItemStack getItemInHand(Player player) {
		if (player.getInventory().getItemInMainHand().getType() != Material.AIR) {
			return player.getInventory().getItemInMainHand();
		}
		return null;
	}

	// Legacy TODO :: DELETE THIS GARBAGE AFTER MONIKER TRANSFER COMPLETE
	public void grabMonikerTokens(Player player) {
		if (ItemEdit.get().getServer().getPluginManager().isPluginEnabled("Foundation") || ItemEdit.get().getServer().getPluginManager().isPluginEnabled("Obelisk")) {
			if (Obelisk.getModule("Moniker").isPresent()) {
				try {
					Optional<Object> profile = Obelisk.getModule("Moniker").get().getUser(player).getVar("profile");
					if (profile.isPresent()) {
						addEntry(0, player, Integer.parseInt(profile.get().toString()) + getTokens(player)); // toString() gets a string of current tokens. A bit jank, but Obelisk be like that.
						profile.get().equals(0); // Sets current Moniker tokens to 0
					}
				} catch (NullPointerException ignored) {

				}
			}
		}
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
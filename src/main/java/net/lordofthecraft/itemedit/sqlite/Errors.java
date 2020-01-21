package net.lordofthecraft.itemedit.sqlite;

import net.lordofthecraft.itemedit.ItemEdit;

import java.util.logging.Level;

public class Errors {

	// Logging of SQL Errors
	public static void execute(ItemEdit plugin, Exception ex){
		plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
	}
	public static void close(ItemEdit plugin, Exception ex){
		plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
	}

	// Additional erros to track.
	public static String sqlConnectionExecute(){
		return "Couldn't execute MySQL statement: ";
	}
	public static String sqlConnectionClose(){
		return "Failed to close MySQL connection: ";
	}
	public static String noSQLConnection(){
		return "Unable to retreive MYSQL connection: ";
	}
	public static String noTableFound(){
		return "Database Error: No Table Found";
	}
}
package net.lordofthecraft.itemedit.command;

import co.lotc.core.command.CommandTemplate;
import net.lordofthecraft.itemedit.ItemEdit;

/**
 * Very basic extention of CommandTemplate for Tythan CMD util usage.
 */
public class BaseCommand extends CommandTemplate {
	protected final ItemEdit plugin = ItemEdit.get();
}
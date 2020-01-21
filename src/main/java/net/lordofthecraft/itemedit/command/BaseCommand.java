package net.lordofthecraft.itemedit.command;

import co.lotc.core.command.CommandTemplate;
import net.lordofthecraft.itemedit.ItemEdit;

public class BaseCommand extends CommandTemplate {
	protected final ItemEdit plugin = ItemEdit.get();
}
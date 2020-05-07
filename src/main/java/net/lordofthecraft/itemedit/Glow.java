package net.lordofthecraft.itemedit;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;

/**
 * Very basic extension of an Enchantment. Nothing special about it. It's made exclusively to add
 * a glowing effect to an item without having enchantment text.
 */
public class Glow extends Enchantment {
	public Glow(NamespacedKey key) {
		super(key);
	}

	@Override
	public String getName() {
		return "Glow";
	}

	@Override
	public int getMaxLevel() {
		return 0;
	}

	@Override
	public int getStartLevel() {
		return 0;
	}

	@Override
	public EnchantmentTarget getItemTarget() {
		return null;
	}

	@Override
	public boolean isTreasure() {
		return false;
	}

	@Override
	public boolean isCursed() {
		return false;
	}

	@Override
	public boolean conflictsWith(Enchantment other) {
		return false;
	}

	@Override
	public boolean canEnchantItem(ItemStack item) {
		return false;
	}
}

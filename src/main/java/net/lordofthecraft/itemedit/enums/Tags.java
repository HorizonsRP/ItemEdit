package net.lordofthecraft.itemedit.enums;

import co.lotc.core.bukkit.util.ItemUtil;
import net.lordofthecraft.itemedit.ItemEdit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

public class Tags {

	private final ChatColor BRACKET_COLOR = ChatColor.GRAY;
	private final String DIVIDER = BRACKET_COLOR + " | ";

	private Rarity rarity;
	private Quality quality;
	private Aura aura;
	private int auraClass = 0;
	private Type type;
	private int itemID;

	public Tags(Rarity rarity, Quality quality, Aura aura, Type type, int auraClass, int itemID) {
		this.rarity =  (rarity != null) ?   rarity : Rarity.DEFAULT;
		this.quality = (quality != null) ? quality : Quality.DEFAULT;
		this.aura =    (aura != null) ?       aura : Aura.DEFAULT;
		this.type =    (type != null) ?       type : Type.DEFAULT;

		this.auraClass = auraClass;
		this.itemID = itemID;
	}

	public Tags getTags(ItemStack item) {
		if (ItemUtil.hasCustomTag(item, ItemEdit.INFO_TAG)) {
			return getTags(ItemUtil.getCustomTag(item, ItemEdit.INFO_TAG));
		}
		return null;
	}

	public Tags getTags(String rawData) {
		return new Tags(rawData.split(":"));
	}

	private Tags(String[] data) {
		String auraName = null;
		if (data.length > 2) {
			auraName = data[2];
			if (auraName.endsWith("%")) {
				auraName = auraName.replace("%", "");
				auraClass = 1;
			} else if (auraName.endsWith("@")) {
				auraName = auraName.replace("@", "");
				auraClass = -1;
			}
		}

		rarity =   (data.length > 0) ?  Rarity.getByName(data[0]) : Rarity.DEFAULT;
		quality =  (data.length > 1) ? Quality.getByName(data[1]) : Quality.DEFAULT;
		aura =    (auraName != null) ?   Aura.getByName(auraName) : Aura.DEFAULT;
		type =     (data.length > 3) ?    Type.getByName(data[3]) : Type.DEFAULT;
		itemID =   (data.length > 4) ?  Integer.parseInt(data[4]) : Integer.MAX_VALUE;
	}

	public String toString() {
		String auraName = aura.name;
		if (auraClass > 0) {
			auraName += "%";
		} else if (auraClass < 0) {
			auraName += "@";
		}
		return rarity.name + ":" + quality.name + ":" + auraName + ":" + type.name + ":" + itemID;
	}

	// GET //
	public Rarity getRarity() {
		return rarity;
	}
	public Quality getQuality() {
		return quality;
	}
	public Aura getAura() {
		return aura;
	}
	public boolean isAuraMinor() {
		return auraClass < 0;
	}
	public boolean isAuraStrong() {
		return auraClass > 0;
	}
	public Type getType() {
		return type;
	}
	public int getLFItemID() {
		return itemID;
	}

	// SET //
	public void setRarity(Rarity rarity) {
		if (rarity != null) {
			this.rarity = rarity;
		}
	}
	public void setQuality(Quality quality) {
		if (quality != null) {
			this.quality = quality;
		}
	}
	public void setAura(Aura aura) {
		if (aura != null) {
			this.aura = aura;
		}
	}
	public void setAuraClass(int auraClass) {
		this.auraClass = auraClass;
	}
	public void setAuraMinor() {
		this.auraClass = -1;
	}
	public void setAuraNormal() {
		this.auraClass = 0;
	}
	public void setAuraStrong() {
		this.auraClass = 1;
	}
	public void setType(Type type) {
		if (type != null) {
			this.type = type;
		}
	}
	public void setLFItemID(int itemID) {
		this.itemID = itemID;
	}

	// FORMAT //
	public String formatTags() {
		String output = BRACKET_COLOR + "[";

		output += (rarity != null) ? rarity.getTag() : Rarity.DEFAULT.getTag();
		output += DIVIDER;
		output += (quality != null) ? quality.getTag() : Quality.DEFAULT.getTag();
		output += DIVIDER;
		output += (aura != null) ? aura.getTag(auraClass) : Aura.DEFAULT.getTag(auraClass);
		output += DIVIDER;
		output += (type != null) ? type.getTag() : Type.DEFAULT.getTag();

		return output + BRACKET_COLOR + "]";
	}

	public void applyTagToItem(ItemStack item) {
		ItemUtil.setCustomTag(item, ItemEdit.INFO_TAG, this.toString());
	}

}
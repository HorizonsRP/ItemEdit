package net.lordofthecraft.itemedit.enums;

import co.lotc.core.bukkit.util.ItemUtil;
import net.lordofthecraft.itemedit.ItemEdit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Tags {

	private final ChatColor BRACKET_COLOR = ChatColor.GRAY;
	private final String DIVIDER = BRACKET_COLOR + " | ";

	private Rarity rarity;
	private Quality quality;
	private Aura aura;
	private int auraClass = 0;
	private Type type;
	private int itemID;

	/**
	 * @param item The item to check for.
	 * @return A Tags object representing the same tags as the item provided.
	 */
	public static Tags getTags(ItemStack item) {
		if (ItemUtil.hasCustomTag(item, ItemEdit.INFO_TAG)) {
			return getTags(ItemUtil.getCustomTag(item, ItemEdit.INFO_TAG));
		} else {
			return getTags("");
		}
	}

	/**
	 * @param rawData The item data to parse.
	 * @return A Tags object representing the same tags as the data provided.
	 */
	public static Tags getTags(String rawData) {
		return new Tags(rawData.split(":"));
	}

	/**
	 * Private constructor for the getTags() methods.
	 * @param data The raw data for a tag split by ':'
	 */
	private Tags(String[] data) {
		// Parse aura name and class. % is Major/Bold/Glow, @ is Minor/Italic
		String auraName = null;
		if (data.length > 2) {
			auraName = data[2];
			if (auraName.endsWith("%")) {
				auraName = auraName.replace("%", "");
				this.auraClass = 1;
			} else if (auraName.endsWith("@")) {
				auraName = auraName.replace("@", "");
				this.auraClass = -1;
			}
		}

		// Verify no IndexOutOfBounds.
		this.rarity =   (data.length > 0) ?  Rarity.getByName(data[0]) : Rarity.DEFAULT;
		this.quality =  (data.length > 1) ? Quality.getByName(data[1]) : Quality.DEFAULT;
		this.aura =    (auraName != null) ?   Aura.getByName(auraName) : Aura.DEFAULT;
		this.type =     (data.length > 3) ?    Type.getByName(data[3]) : Type.DEFAULT;
		this.itemID =   (data.length > 4) ?  Integer.parseInt(data[4]) : Integer.MAX_VALUE;

		// Verify not null.
		if (this.rarity == null) {
			this.rarity = Rarity.DEFAULT;
		}
		if (this.quality == null) {
			this.quality = Quality.DEFAULT;
		}
		if (this.aura == null) {
			this.aura = Aura.DEFAULT;
		}
		if (this.type == null) {
			this.type = Type.DEFAULT;
		}
	}

	/**
	 * @return Returns the raw data of this tag set. Use getTags(rawData) to build back
	 * into a Tags object.
	 */
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
	public boolean isAuraMajor() {
		return auraClass > 0;
	}
	public int getAuraClass() {
		return auraClass;
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

	/**
	 * Sets the Aura's Class. When in doubt use setAura.. Minor(), Normal(), and Major().
	 * @param auraClass The class of this aura. -1 = Minor, 0 = Normal, 1 = Major.
	 */
	public void setAuraClass(int auraClass) {
		if (auraClass != Integer.MAX_VALUE) {
			this.auraClass = auraClass;
		}
	}
	public void setAuraMinor() {
		this.auraClass = -1;
	}
	public void setAuraNormal() {
		this.auraClass = 0;
	}
	public void setAuraMajor() {
		this.auraClass = 1;
	}
	public void setType(Type type) {
		if (type != null) {
			this.type = type;
		}
	}
	public void setLFItemID(int itemID) {
		if (itemID > 0) {
			this.itemID = itemID;
		}
	}

	/**
	 * @return Returns this set of tags as a singular formatted string.
	 * e.g. [Very Common | Cheap | Mundane | Tool] with proper color.
	 */
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

	/**
	 * Applies the Tythan ItemUtil tag to an item - NO LORE CHANGE.
	 * @param item The item to apply the tag to.
	 */
	public void applyTagToItem(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			ItemUtil.setCustomTag(meta, ItemEdit.INFO_TAG, this.toString());
			meta.setCustomModelData(itemID);
			item.setItemMeta(meta);
		}
	}

}
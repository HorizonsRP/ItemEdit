package net.lordofthecraft.itemedit;

import co.lotc.core.bukkit.util.ItemUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class PlaceListener implements Listener {

	@EventHandler (ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent e) {
		if (ItemUtil.hasCustomTag(e.getItemInHand(), ItemEdit.NO_PLACEMENT_TAG)) {
			e.setCancelled(true);
		}
	}

}

package com.songoda.epicfurnaces.listeners;

import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.EpicFurnaceInstances;
import com.songoda.epicfurnaces.database.DataManager;
import com.songoda.epicfurnaces.furnace.Furnace;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

/**
 * Created by songoda on 2/26/2017.
 */
public final class EntityListeners implements Listener, EpicFurnaceInstances {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlow(EntityExplodeEvent event) {
        final EpicFurnaces plugin = getPlugin();
        final DataManager dataManager = plugin.getDataManager();
        for (Block block : event.blockList()) {
            final Furnace furnace = FURNACE_MANAGER.getFurnace(block);
            if (furnace == null) continue;
            FURNACE_MANAGER.removeFurnace(block.getLocation());
            dataManager.deleteFurnace(furnace);
            plugin.clearHologram(furnace);
        }
    }
}
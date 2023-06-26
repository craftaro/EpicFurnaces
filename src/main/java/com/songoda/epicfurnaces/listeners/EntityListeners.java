package com.songoda.epicfurnaces.listeners;

import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.furnace.Furnace;
import com.songoda.epicfurnaces.furnace.FurnaceManager;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class EntityListeners implements Listener {
    private final EpicFurnaces plugin;
    private final FurnaceManager furnaceManager;

    public EntityListeners(EpicFurnaces plugin) {
        this.plugin = plugin;
        this.furnaceManager = plugin.getFurnaceManager();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlow(EntityExplodeEvent event) {
        for (Block block : event.blockList()) {
            Furnace furnace = this.furnaceManager.getFurnace(block);
            if (furnace == null) {
                continue;
            }

            this.furnaceManager.removeFurnace(block.getLocation());
            this.plugin.getDataManager().deleteFurnace(furnace);
            this.plugin.clearHologram(furnace);
        }
    }
}

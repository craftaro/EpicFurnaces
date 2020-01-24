package com.songoda.epicfurnaces.listeners;

import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.furnace.Furnace;
import com.songoda.epicfurnaces.furnace.levels.Level;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;

/**
 * Created by songoda on 2/26/2017.
 */
public class FurnaceListeners implements Listener {

    private final EpicFurnaces plugin;

    public FurnaceListeners(EpicFurnaces plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCook(FurnaceSmeltEvent event) {
        Block block = event.getBlock();
        if ((event.getBlock().isBlockPowered() && plugin.getConfig().getBoolean("Main.Redstone Deactivates Furnaces")) || event.getResult() == null) {
            event.setCancelled(true);
            return;
        }
        Furnace furnace = plugin.getFurnaceManager().getFurnace(block.getLocation());

        if (furnace != null)
            furnace.plus(event);
    }

    @EventHandler
    public void onFuel(FurnaceBurnEvent event) {
        Furnace furnace = plugin.getFurnaceManager().getFurnace(event.getBlock().getLocation());

        Level level = furnace != null ? furnace.getLevel() : plugin.getLevelManager().getLowestLevel();

        if (level.getFuelDuration() != 0) return;

        int num = level.getFuelDuration();
        int per = (event.getBurnTime() / 100) * num;
        event.setBurnTime(event.getBurnTime() + per);
    }
}
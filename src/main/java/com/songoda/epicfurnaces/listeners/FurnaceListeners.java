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
public final class FurnaceListeners implements Listener {

    private final EpicFurnaces plugin;

    public FurnaceListeners(EpicFurnaces plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCook(FurnaceSmeltEvent event) {
        final Block block = event.getBlock();
        if ((block.isBlockPowered() && plugin.getConfig().getBoolean("Main.Redstone Deactivates Furnaces")) || event.getResult() == null) {
            event.setCancelled(true);
            return;
        }
        final Furnace furnace = plugin.getFurnaceManager().getFurnace(block.getLocation());

        if (furnace != null)
            furnace.plus(event);
    }

    @EventHandler
    public void onFuel(FurnaceBurnEvent event) {
        final Furnace furnace = plugin.getFurnaceManager().getFurnace(event.getBlock().getLocation());
        if (furnace == null) {
            return;
        }

        final Level level = furnace.getLevel();

        if (level.getFuelDuration() != 0) return;

        final int num = level.getFuelDuration(), burnTime = event.getBurnTime();
        final int per = (burnTime / 100) * num;
        event.setBurnTime(burnTime + per);
    }
}
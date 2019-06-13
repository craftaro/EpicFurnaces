package com.songoda.epicfurnaces.listeners;

import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.furnace.Furnace;
import com.songoda.epicfurnaces.furnace.levels.Level;
import org.bukkit.Material;
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
    public void onCook(FurnaceSmeltEvent e) {
        Block b = e.getBlock();
        if ((e.getBlock().isBlockPowered() && plugin.getConfig().getBoolean("Main.Redstone Deactivates Furnaces")) || e.getResult() == null) {
            e.setCancelled(true);
            return;
        }
        Furnace furnace = plugin.getFurnaceManager().getFurnace(b.getLocation());

        if (furnace != null && e.getSource().getType() != Material.valueOf("WET_SPONGE"))
            furnace.plus(e);
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
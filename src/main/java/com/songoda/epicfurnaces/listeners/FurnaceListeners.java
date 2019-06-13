package com.songoda.epicfurnaces.listeners;

import com.songoda.epicfurnaces.EpicFurnacesPlugin;
import com.songoda.epicfurnaces.api.furnace.Furnace;
import com.songoda.epicfurnaces.api.furnace.Level;
import com.songoda.epicfurnaces.furnace.EFurnace;
import com.songoda.epicfurnaces.utils.Debugger;
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

    private final EpicFurnacesPlugin instance;

    public FurnaceListeners(EpicFurnacesPlugin instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onCook(FurnaceSmeltEvent e) {
        try {
            Block b = e.getBlock();
            if ((e.getBlock().isBlockPowered() && instance.getConfig().getBoolean("Main.Redstone Deactivates Furnaces")) || e.getResult() == null) {
                e.setCancelled(true);
                return;
            }
            Furnace furnace = instance.getFurnaceManager().getFurnace(b.getLocation());

            if (furnace != null && e.getSource().getType() != Material.WET_SPONGE)
                ((EFurnace) furnace).plus(e);
        } catch (Exception ee) {
            Debugger.runReport(ee);
        }
    }

    @EventHandler
    public void onFuel(FurnaceBurnEvent e) {
        try {
            if (e.getFuel() == null) return;
            Furnace furnace = instance.getFurnaceManager().getFurnace(e.getBlock().getLocation());

            Level level = furnace != null ? furnace.getLevel() : instance.getLevelManager().getLowestLevel();

            if (level.getFuelDuration() != 0) return;

            int num = level.getFuelDuration();
            int per = (e.getBurnTime() / 100) * num;
            e.setBurnTime(e.getBurnTime() + per);
        } catch (Exception ee) {
            Debugger.runReport(ee);
        }
    }
}
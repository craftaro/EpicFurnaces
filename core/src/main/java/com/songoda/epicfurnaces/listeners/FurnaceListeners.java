package com.songoda.epicfurnaces.listeners;

import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.furnace.FurnaceObject;
import com.songoda.epicfurnaces.furnace.Level;
import com.songoda.epicfurnaces.utils.Debugger;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;

/**
 * Created by songoda on 2/26/2017.
 */
public class FurnaceListeners implements Listener {

    private final EpicFurnaces instance;

    public FurnaceListeners(EpicFurnaces instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onCook(FurnaceSmeltEvent event) {
        try {
            Block b = event.getBlock();

            if ((event.getBlock().isBlockPowered() && instance.getConfig().getBoolean("Main.Redstone Deactivates Furnaces")) || event.getResult() == null) {
                event.setCancelled(true);
                return;
            }

            FurnaceObject furnace = instance.getFurnaceManager().getFurnace(b.getLocation());

            //TODO: wet sponge
            if (furnace != null && event.getSource().getType() != instance.getBukkitEnums().getWetSponge()) {
                furnace.plus(event);
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    @EventHandler
    public void onFuel(FurnaceBurnEvent event) {
        try {
            if (event.getFuel() == null) {
                return;
            }

            FurnaceObject furnace = instance.getFurnaceManager().getFurnace(event.getBlock().getLocation());
            Level level = furnace != null ? furnace.getLevel() : instance.getLevelManager().getLowestLevel();

            if (level.getFuelDuration() != 0) {
                return;
            }

            int num = level.getFuelDuration();
            int per = (event.getBurnTime() / 100) * num;
            event.setBurnTime(event.getBurnTime() + per);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }
}
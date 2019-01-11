package com.songoda.epicfurnaces.listeners;

import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.furnace.FurnaceObject;
import com.songoda.epicfurnaces.utils.Debugger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

/**
 * Created by songoda on 2/26/2017.
 */
public class BlockListeners implements Listener {

    private final EpicFurnaces instance;

    public BlockListeners(EpicFurnaces instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onSnowLand(BlockFormEvent event) {
        Material material = event.getNewState().getType();

        if (material != Material.SNOW && material != Material.ICE) {
            return;
        }

        for (FurnaceObject furnace : instance.getFurnaceManager().getFurnaces().values()) {
            if (furnace.getRadius(false) == null || ((org.bukkit.block.Furnace) furnace.getLocation().getBlock().getState()).getBurnTime() == 0) {
                continue;
            }

            for (Location location : furnace.getRadius(false)) {
                if (location.getX() != event.getNewState().getX() || location.getY() != event.getNewState().getY() || location.getZ() != event.getNewState().getZ()) {
                    continue;
                }

                event.setCancelled(true);
                return;
            }
        }

    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        try {
            if (event.getBlock().getType() != Material.FURNACE || !event.getItemInHand().getItemMeta().hasDisplayName()) {
                return;
            }

            ItemStack item = event.getItemInHand();
            Location location = event.getBlock().getLocation();

            if (instance.getFurnceLevel(item) != 1) {
                if (instance.getBlacklistHandler().isBlacklisted(event.getPlayer())) {
                    event.setCancelled(true);
                    return;
                }

                instance.getFurnaceManager().addFurnace(location, new FurnaceObject(location, instance.getLevelManager().getLevel(instance.getFurnceLevel(item)), null, instance.getFurnaceUses(item), 0, new ArrayList<>(), event.getPlayer().getUniqueId()));
            }

        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        try {
            if (!event.getPlayer().hasPermission("EpicFurnaces.overview") && !event.getPlayer().hasPermission("epicfurnaces.*")) {
                return;
            }

            Block block = event.getBlock();

            if (block.getType() != Material.FURNACE) {
                return;
            }

            instance.getHologramTask().despawn(block);

            if (instance.getBlacklistHandler().isBlacklisted(event.getPlayer())) {
                return;
            }

            FurnaceObject furnace = instance.getFurnaceManager().getFurnace(block);
            int level = instance.getFurnaceManager().getFurnace(block).getLevel().getLevel();

            if (level != 0) {
                event.setCancelled(true);

                ItemStack item = instance.createLeveledFurnace(level, furnace.getUses());

                event.getBlock().setType(Material.AIR);
                event.getBlock().getLocation().getWorld().dropItemNaturally(event.getBlock().getLocation(), item);
            }
            instance.getFurnaceManager().removeFurnace(block.getLocation());

        } catch (Exception ee) {
            Debugger.runReport(ee);
        }
    }
}
package com.songoda.epicfurnaces.listeners;

import com.mysql.jdbc.Buffer;
import com.songoda.arconix.api.methods.formatting.TextComponent;
import com.songoda.epicfurnaces.EpicFurnacesPlugin;
import com.songoda.epicfurnaces.api.furnace.Furnace;
import com.songoda.epicfurnaces.furnace.EFurnace;
import com.songoda.epicfurnaces.utils.Debugger;
import com.songoda.epicfurnaces.utils.Methods;
import org.bukkit.Bukkit;
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
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

/**
 * Created by songoda on 2/26/2017.
 */
public class BlockListeners implements Listener {

    private final EpicFurnacesPlugin instance;

    public BlockListeners(EpicFurnacesPlugin instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onSnowLand(BlockFormEvent event) {
        Material material = event.getNewState().getType();

        if (material != Material.SNOW && material != Material.ICE) return;

        for (Furnace furnace : instance.getFurnaceManager().getFurnaces().values()) {
            if (furnace.getRadius(false) == null || ((org.bukkit.block.Furnace)furnace.getLocation().getBlock().getState()).getBurnTime() == 0) continue;
            for (Location location : furnace.getRadius(false)) {
                if (location.getX() != event.getNewState().getX() || location.getY() != event.getNewState().getY() || location.getZ() != event.getNewState().getZ()) continue;
                event.setCancelled(true);
                return;
            }
        }

    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        try {
            if (event.getBlock().getType() != Material.FURNACE || !event.getItemInHand().getItemMeta().hasDisplayName()) return;
            ItemStack item = event.getItemInHand();

            Location location = event.getBlock().getLocation();

            if (instance.getFurnceLevel(item) != 1) {
                if (instance.getBlacklistHandler().isBlacklisted(event.getPlayer())) {
                    event.setCancelled(true);
                    return;
                }

                instance.getFurnaceManager().addFurnace(location, new EFurnace(location, instance.getLevelManager().getLevel(instance.getFurnceLevel(item)), null, instance.getFurnaceUses(item), 0, new ArrayList<>(), event.getPlayer().getUniqueId()));
            }

        } catch (Exception ee) {
            Debugger.runReport(ee);
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

            Furnace furnace = instance.getFurnaceManager().getFurnace(block);
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
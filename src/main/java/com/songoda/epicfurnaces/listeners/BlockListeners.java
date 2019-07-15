package com.songoda.epicfurnaces.listeners;

import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.furnace.Furnace;
import com.songoda.epicfurnaces.furnace.FurnaceBuilder;
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

/**
 * Created by songoda on 2/26/2017.
 */
public class BlockListeners implements Listener {

    private final EpicFurnaces plugin;

    public BlockListeners(EpicFurnaces plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSnowLand(BlockFormEvent event) {
        Material material = event.getNewState().getType();

        if (material != Material.SNOW && material != Material.ICE) return;

        for (Furnace furnace : plugin.getFurnaceManager().getFurnaces().values()) {
            if (furnace.getRadius(false) == null || ((org.bukkit.block.Furnace) furnace.getLocation().getBlock().getState()).getBurnTime() == 0)
                continue;
            for (Location location : furnace.getRadius(false)) {
                if (location.getX() != event.getNewState().getX() || location.getY() != event.getNewState().getY() || location.getZ() != event.getNewState().getZ())
                    continue;
                event.setCancelled(true);
                return;
            }
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {

        if (plugin.getBlacklistHandler().isBlacklisted(event.getPlayer())) {
            return;
        }

        if (event.getBlock().getType() != Material.FURNACE)
            return;

        ItemStack item = event.getItemInHand();

        Location location = event.getBlock().getLocation();

        Furnace furnace = event.getItemInHand().getItemMeta().hasDisplayName() && plugin.getFurnceLevel(item) != 1
                ? new FurnaceBuilder(location)
                .setLevel(plugin.getLevelManager().getLevel(plugin.getFurnceLevel(item)))
                .setUses(plugin.getFurnaceUses(item))
                .setPlacedBy(event.getPlayer().getUniqueId()).build()
                : new FurnaceBuilder(location).setPlacedBy(event.getPlayer().getUniqueId()).build();

        plugin.getFurnaceManager().addFurnace(furnace);

        if (plugin.getHologram() != null) {
            plugin.getHologram().add(furnace);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!event.getPlayer().hasPermission("EpicFurnaces.overview") && !event.getPlayer().hasPermission("epicfurnaces.*")) {
            return;
        }
        Block block = event.getBlock();
        if (!block.getType().name().contains("FURNACE")
                || plugin.getBlacklistHandler().isBlacklisted(event.getPlayer()))
            return;

        Furnace furnace = plugin.getFurnaceManager().getFurnace(block);
        int level = plugin.getFurnaceManager().getFurnace(block).getLevel().getLevel();

        if (plugin.getHologram() != null)
            plugin.getHologram().remove(furnace);

        if (level != 0) {
            event.setCancelled(true);

            ItemStack item = plugin.createLeveledFurnace(block.getType().name().contains("BURNING") ? Material.FURNACE
                    : block.getType(), level, furnace.getUses());

            event.getBlock().setType(Material.AIR);
            event.getBlock().getLocation().getWorld().dropItemNaturally(event.getBlock().getLocation(), item);
        }
        plugin.getFurnaceManager().removeFurnace(block.getLocation());
    }
}
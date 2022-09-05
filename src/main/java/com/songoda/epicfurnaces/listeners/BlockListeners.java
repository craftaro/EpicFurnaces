package com.songoda.epicfurnaces.listeners;

import com.songoda.core.hooks.HologramManager;
import com.songoda.core.utils.PlayerUtils;
import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.furnace.Furnace;
import com.songoda.epicfurnaces.furnace.FurnaceBuilder;
import com.songoda.epicfurnaces.settings.Settings;
import com.songoda.epicfurnaces.utils.GameArea;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

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

        for (Furnace furnace : plugin.getFurnaceManager().getFurnaces(GameArea.of(event.getBlock().getLocation()))) {
            List<Location> radius = furnace.getRadius(false);
            if (radius == null || ((org.bukkit.block.Furnace) furnace.getLocation().getBlock().getState()).getBurnTime() == 0)
                continue;
            for (Location location : radius) {
                if (location.getX() != event.getNewState().getX() || location.getY() != event.getNewState().getY() || location.getZ() != event.getNewState().getZ())
                    continue;
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {

        if (plugin.getBlacklistHandler().isBlacklisted(event.getPlayer().getWorld())
                || !event.getBlock().getType().name().contains("FURNACE") && !event.getBlock().getType().name().contains("SMOKER"))
            return;

        ItemStack item = event.getItemInHand();
        Player player = event.getPlayer();

        if (!plugin.isLeveledFurnace(item) && Settings.ALLOW_NORMAL_FURNACES.getBoolean()) {
            return;
        }

        if (Settings.USE_LIMIT_PERMISSION.getBoolean()) {
            int amount = 0;
            for (Furnace furnace : plugin.getFurnaceManager().getFurnaces().values()) {
                if (furnace.getPlacedBy() == null || !furnace.getPlacedBy().equals(player.getUniqueId())) continue;
                amount++;
            }
            int limit = PlayerUtils.getNumberFromPermission(player, "epicfurnaces.limit", -1);

            if (limit != -1 && amount >= limit) {
                event.setCancelled(true);
                plugin.getLocale().getMessage("event.limit.hit")
                        .processPlaceholder("limit", limit).sendPrefixedMessage(player);
                return;
            }
        }

        Location location = event.getBlock().getLocation();

        Furnace furnace = event.getItemInHand().getItemMeta().hasDisplayName() && plugin.getFurnaceLevel(item) != 1
                ? new FurnaceBuilder(location)
                .setLevel(plugin.getLevelManager().getLevel(plugin.getFurnaceLevel(item)))
                .setUses(plugin.getFurnaceUses(item))
                .setPlacedBy(event.getPlayer().getUniqueId()).build()
                : new FurnaceBuilder(location).setPlacedBy(event.getPlayer().getUniqueId()).build();

        plugin.getDataManager().createFurnace(furnace);
        plugin.getFurnaceManager().addFurnace(furnace);

        plugin.updateHolograms(Collections.singleton(furnace));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!event.getPlayer().hasPermission("EpicFurnaces.overview") && !event.getPlayer().hasPermission("epicfurnaces.*")) {
            return;
        }
        Block block = event.getBlock();
        if (!block.getType().name().contains("FURNACE") && !block.getType().name().contains("SMOKER")
                || plugin.getBlacklistHandler().isBlacklisted(event.getPlayer().getWorld()))
            return;

        Furnace furnace = plugin.getFurnaceManager().getFurnace(block);

        if (furnace == null) {
            return;
        }

        int level = plugin.getFurnaceManager().getFurnace(block).getLevel().getLevel();

        plugin.clearHologram(furnace);

        if (level != 0) {
            event.setCancelled(true);

            ItemStack item = plugin.createLeveledFurnace(block.getType().name().contains("BURNING") ? Material.FURNACE
                    : block.getType(), level, furnace.getUses());

            // By cancelling the event we destroy any chance of items dropping form the furnace. This fixes the problem.
            //furnace.dropItems(); No need to drop items. dropItemNaturally() will drop the items inside the furnance

            event.getBlock().setType(Material.AIR);
            event.getBlock().getLocation().getWorld().dropItemNaturally(event.getBlock().getLocation(), item);
        }
        plugin.getFurnaceManager().removeFurnace(block.getLocation());
        plugin.getDataManager().deleteFurnace(furnace);
    }
}

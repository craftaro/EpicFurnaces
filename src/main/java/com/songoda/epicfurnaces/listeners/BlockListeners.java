package com.songoda.epicfurnaces.listeners;

import com.songoda.core.hooks.HologramManager;
import com.songoda.core.utils.PlayerUtils;
import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.furnace.Furnace;
import com.songoda.epicfurnaces.furnace.FurnaceBuilder;
import com.songoda.epicfurnaces.furnace.FurnaceManager;
import com.songoda.epicfurnaces.settings.Settings;
import com.songoda.epicfurnaces.utils.GameArea;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Created by songoda on 2/26/2017.
 */
public final class BlockListeners implements Listener {

    private final EpicFurnaces plugin;

    public BlockListeners(EpicFurnaces plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSnowLand(BlockFormEvent event) {
        final BlockState newState = event.getNewState();
        final Material material = newState.getType();

        if (material != Material.SNOW && material != Material.ICE) return;

        for (Furnace furnace : plugin.getFurnaceManager().getFurnaces(GameArea.of(event.getBlock().getLocation()))) {
            final List<Location> radius = furnace.getRadius(false);
            if (radius == null || ((org.bukkit.block.Furnace) furnace.getLocation().getBlock().getState()).getBurnTime() == 0)
                continue;
            for (Location location : radius) {
                if (location.getX() != newState.getX() || location.getY() != newState.getY() || location.getZ() != newState.getZ())
                    continue;
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        if (plugin.getBlacklistHandler().isBlacklisted(player.getWorld())
                || !event.getBlock().getType().name().contains("FURNACE") && !event.getBlock().getType().name().contains("SMOKER"))
            return;

        final ItemStack item = event.getItemInHand();

        if (!plugin.isLeveledFurnace(item) && Settings.ALLOW_NORMAL_FURNACES.getBoolean()) {
            return;
        }
        
        final UUID playerUUID = player.getUniqueId();
        if (Settings.USE_LIMIT_PERMISSION.getBoolean()) {
            int amount = 0;
            for (Furnace furnace : plugin.getFurnaceManager().getFurnaces().values()) {
                if (furnace.getPlacedBy() == null || !furnace.getPlacedBy().equals(playerUUID)) continue;
                amount++;
            }
            final int limit = PlayerUtils.getNumberFromPermission(player, "epicfurnaces.limit", -1);

            if (limit != -1 && amount >= limit) {
                event.setCancelled(true);
                plugin.getLocale().getMessage("event.limit.hit")
                        .processPlaceholder("limit", limit).sendPrefixedMessage(player);
                return;
            }
        }
        final Location location = event.getBlock().getLocation();
        final ItemMeta itemMeta = event.getItemInHand().getItemMeta();
        final Furnace furnace = itemMeta != null && itemMeta.hasDisplayName() && plugin.getFurnaceLevel(item) != 1
                ? new FurnaceBuilder(location)
                .setLevel(plugin.getLevelManager().getLevel(plugin.getFurnaceLevel(item)))
                .setUses(plugin.getFurnaceUses(item))
                .setPlacedBy(playerUUID).build()
                : new FurnaceBuilder(location).setPlacedBy(playerUUID).build();

        plugin.getDataManager().createFurnace(furnace);
        plugin.getFurnaceManager().addFurnace(furnace);

        plugin.updateHolograms(Collections.singleton(furnace));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        final Player player = event.getPlayer();
        if (!player.hasPermission("EpicFurnaces.overview") && !player.hasPermission("epicfurnaces.*")) {
            return;
        }
        final Block block = event.getBlock();
        final Material blockType = block.getType();
        final String blockTypeName = blockType.name();
        if (!blockTypeName.contains("FURNACE") && !blockTypeName.contains("SMOKER")
                || plugin.getBlacklistHandler().isBlacklisted(player.getWorld()))
            return;
        
        final FurnaceManager furnaceManager = plugin.getFurnaceManager();
        final Furnace furnace = furnaceManager.getFurnace(block);

        if (furnace == null) {
            return;
        }

        final int level = furnaceManager.getFurnace(block).getLevel().getLevel();

        plugin.clearHologram(furnace);

        if (level != 0) {
            event.setCancelled(true);

            ItemStack item = plugin.createLeveledFurnace(blockTypeName.contains("BURNING") ? Material.FURNACE
                    : blockType, level, furnace.getUses());

            // By cancelling the event we destroy any chance of items dropping form the furnace. This fixes the problem.
            //furnace.dropItems(); No need to drop items. dropItemNaturally() will drop the items inside the furnance

            block.setType(Material.AIR);
            block.getLocation().getWorld().dropItemNaturally(block.getLocation(), item);
        }
        furnaceManager.removeFurnace(block.getLocation());
        plugin.getDataManager().deleteFurnace(furnace);
    }
}

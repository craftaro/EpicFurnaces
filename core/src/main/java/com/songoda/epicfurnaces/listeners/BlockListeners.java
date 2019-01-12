package com.songoda.epicfurnaces.listeners;

import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.objects.FurnaceObject;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
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
        if (event.getNewState().getType() != Material.SNOW && event.getNewState().getType() != Material.ICE) {
            return;
        }

        for (FurnaceObject furnace : instance.getFurnaceManager().getFurnaces().values()) {
            if (furnace.getRadius(false) == null || ((Furnace) furnace.getLocation().getBlock().getState()).getBurnTime() == 0) {
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
        if (event.getBlock().getType() != Material.FURNACE) {
            return;
        }

        ItemStack item = event.getItemInHand();
        Location location = event.getBlock().getLocation();

        if (instance.getBlacklistHandler().isBlacklisted(event.getPlayer())) {
            event.setCancelled(true);
            return;
        }

        FurnaceObject furnaceObject = new FurnaceObject(instance,
                location,
                instance.getLevelManager().getLevel(instance.getFurnaceManager().getFurnaceLevel(item)),
                null,
                instance.getFurnaceManager().getFurnaceUses(item),
                0,
                new ArrayList<>(),
                event.getPlayer().getUniqueId());

        instance.getFurnaceManager().addFurnace(location, furnaceObject);

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!event.getPlayer().hasPermission("EpicFurnaces.overview") && !event.getPlayer().hasPermission("epicfurnaces.*")) {
            return;
        }

        Block block = event.getBlock();

        if (block.getType() != Material.FURNACE && block.getType() != instance.getBukkitEnums().getMaterial("BURNING_FURNACE").getType()) {
            return;
        }

        if (instance.getBlacklistHandler().isBlacklisted(event.getPlayer())) {
            return;
        }

        FurnaceObject furnace = instance.getFurnaceManager().getFurnace(block.getLocation()).orElseGet(() -> instance.getFurnaceManager().createFurnace(block.getLocation()));
        int level = furnace.getLevel().getLevel();

        if (level != 0) {
            event.setCancelled(true);

            ItemStack item = instance.getFurnaceManager().createLeveledFurnace(level, furnace.getUses(), instance);

            event.getBlock().setType(Material.AIR);
            event.getBlock().getLocation().getWorld().dropItemNaturally(event.getBlock().getLocation(), item);
        }

        instance.getFurnaceManager().removeFurnace(block.getLocation());
    }
}
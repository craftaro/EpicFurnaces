package com.songoda.epicfurnaces.listeners;

import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.objects.FurnaceObject;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Furnace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.songoda.epicfurnaces.objects.FurnaceObject.BoostType.OVERHEAT;

/**
 * Created by songoda on 2/26/2017.
 */
public class BlockListeners implements Listener {

    private final EpicFurnaces instance;

    public BlockListeners(EpicFurnaces instance) {
        this.instance = instance;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onSnowLand(BlockFormEvent event) {
        if (event.getNewState().getType() != Material.SNOW && event.getNewState().getType() != Material.ICE) {
            return;
        }

        for (FurnaceObject furnace : instance.getFurnaceManager().getFurnaces().values()) {
            if (furnace.getRadius(OVERHEAT) == null || ((Furnace) furnace.getLocation().getBlock().getState()).getBurnTime() == 0) {
                continue;
            }

            for (Location location : furnace.getRadius(OVERHEAT)) {
                if (location.getX() != event.getNewState().getX() || location.getY() != event.getNewState().getY() || location.getZ() != event.getNewState().getZ()) {
                    continue;
                }

                event.setCancelled(true);
                return;
            }
        }

    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
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
        if (!event.getPlayer().hasPermission("epicfurnaces.overview") && !event.getPlayer().hasPermission("epicfurnaces.*")) {
            return;
        }

        if (instance.getBlacklistHandler().isBlacklisted(event.getPlayer())) {
            return;
        }

        if (event.getBlock().getType() != Material.FURNACE && !event.getBlock().getType().name().equals("BURNING_FURNACE")) {
            return;
        }

        instance.getFurnaceManager().getFurnace(event.getBlock().getLocation()).ifPresent(this::handleBreak);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockExplode(EntityExplodeEvent event) {
        Set<FurnaceObject> furnaces = event.blockList().parallelStream()
                .map(block -> instance.getFurnaceManager().getFurnace(block.getLocation()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        furnaces.forEach(this::handleBreak);
    }

    private void handleBreak(FurnaceObject furnaceObject) {
        ItemStack item = instance.getFurnaceManager().createLeveledFurnace(furnaceObject.getLevel().getLevel(), furnaceObject.getUses(), instance);
        furnaceObject.getLocation().getBlock().setType(Material.AIR);
        furnaceObject.getLocation().getWorld().dropItemNaturally(furnaceObject.getLocation().getBlock().getLocation(), item);

        instance.getFurnaceManager().removeFurnace(furnaceObject);
    }
}
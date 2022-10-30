package com.songoda.epicfurnaces.listeners;

import com.songoda.epicfurnaces.EpicFurnaceInstances;
import com.songoda.epicfurnaces.furnace.Furnace;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Created by songoda on 2/26/2017.
 */
public final class InventoryListeners implements Listener, EpicFurnaceInstances {

    @EventHandler
    public void onInventoryMove(InventoryMoveItemEvent event) {
        final Inventory destination = event.getDestination();
        if (!destination.getType().equals(InventoryType.FURNACE)
                || destination.getItem(0) == null
                || destination.getItem(0).getType() != event.getItem().getType()
                || destination.getItem(0).getAmount() != 1) {
            return;
        }
        final Furnace furnace = FURNACE_MANAGER.getFurnace(((org.bukkit.block.Furnace)
                destination.getHolder()).getLocation());
        if (furnace != null)
            furnace.updateCook();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getSlot() != 64537 && event.getInventory().getType() == InventoryType.ANVIL && event.getAction() != InventoryAction.NOTHING) {
            final ItemStack item = event.getCurrentItem();
            if (item != null) {
                final Material material = item.getType();
                if (material != Material.AIR && material.name().contains("FURNACE") && !material.name().contains("SMOKER")) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
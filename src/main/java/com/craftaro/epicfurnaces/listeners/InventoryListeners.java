package com.craftaro.epicfurnaces.listeners;

import com.craftaro.epicfurnaces.EpicFurnaces;
import com.craftaro.epicfurnaces.furnace.Furnace;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class InventoryListeners implements Listener {
    private final EpicFurnaces plugin;

    public InventoryListeners(EpicFurnaces plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryMove(InventoryMoveItemEvent event) {
        if (!event.getDestination().getType().equals(InventoryType.FURNACE)
                || event.getDestination().getItem(0) == null
                || event.getDestination().getItem(0).getType() != event.getItem().getType()
                || event.getDestination().getItem(0).getAmount() != 1) {
            return;
        }
        Furnace furnace = this.plugin.getFurnaceManager().getFurnace(((org.bukkit.block.Furnace) event.getDestination().getHolder()).getLocation());
        if (furnace != null) {
            furnace.updateCook();
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getSlot() == 64537 ||
                event.getInventory().getType() != InventoryType.ANVIL ||
                event.getAction() == InventoryAction.NOTHING ||
                event.getCurrentItem() == null ||
                event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        ItemStack item = event.getCurrentItem();
        if (item.getType().name().contains("FURNACE") && !item.getType().name().contains("SMOKER")) {
            event.setCancelled(true);
        }
    }
}

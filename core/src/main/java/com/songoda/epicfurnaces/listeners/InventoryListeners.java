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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

import static org.bukkit.event.inventory.InventoryAction.NOTHING;
import static org.bukkit.event.inventory.InventoryType.*;
import static org.bukkit.event.inventory.InventoryType.SlotType.CRAFTING;

/**
 * Created by songoda on 2/26/2017.
 */
public class InventoryListeners implements Listener {

    private final EpicFurnaces instance;

    public InventoryListeners(EpicFurnaces instance) {
        this.instance = instance;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        Map<Inventory, Location> loadedFurnaceInventories = this.instance.getFurnaceManager().getLoadedFurnaceInventories();
        Location location = loadedFurnaceInventories.get(inventory);
        if (location == null)
            return;

        location.getChunk().unload();
        loadedFurnaceInventories.remove(inventory);
    }

    @EventHandler
    public void onInventoryMove(InventoryMoveItemEvent event) {
        if (!event.getDestination().getType().equals(FURNACE)
                || event.getDestination().getItem(0) == null
                || event.getDestination().getItem(0).getType() != event.getItem().getType()
                || event.getDestination().getItem(0).getAmount() != 1) {
            return;
        }
        instance.getFurnaceManager().getFurnace(((Furnace) event.getDestination().getHolder()).getLocation()).ifPresent(FurnaceObject::updateCook);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getType().equals(FURNACE)
                && event.getInventory().getHolder() != null
                && event.getSlotType() == CRAFTING) {
            Block block = ((Furnace) event.getInventory().getHolder()).getLocation().getBlock();
            instance.getFurnaceManager().getFurnace(block.getLocation()).ifPresent(FurnaceObject::updateCook);
        }

        if (event.getSlot() == 64537 || event.getInventory().getType() != ANVIL || event.getAction() == NOTHING) {
            return;
        }

        if (event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        ItemStack item = event.getCurrentItem();

        if (item.getType() == Material.FURNACE) {
            event.setCancelled(true);
        }

    }

}
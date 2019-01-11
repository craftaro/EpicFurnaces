package com.songoda.epicfurnaces.listeners;

import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.utils.Debugger;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

/**
 * Created by songoda on 2/26/2017.
 */
public class InventoryListeners implements Listener {

    private final EpicFurnaces instance;

    public InventoryListeners(EpicFurnaces instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onInventoryMove(InventoryMoveItemEvent event) {
        if (!event.getDestination().getType().equals(InventoryType.FURNACE)
                || event.getDestination().getItem(0) == null
                || event.getDestination().getItem(0).getType() != event.getItem().getType()
                || event.getDestination().getItem(0).getAmount() != 1) {
            return;
        }
        instance.getFurnaceManager().getFurnace(((Furnace) event.getDestination().getHolder()).getLocation()).updateCook();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        try {
            if (event.getInventory().getType().equals(InventoryType.FURNACE)
                    && event.getInventory().getHolder() != null
                    && event.getSlotType() == InventoryType.SlotType.CRAFTING) {
                Block block;
                block = ((Furnace) event.getInventory().getHolder()).getLocation().getBlock();
                instance.getFurnaceManager().getFurnace(block).updateCook();
            }
            if (event.getSlot() != 64537) {
                if (event.getInventory().getType() == InventoryType.ANVIL) {
                    if (event.getAction() != InventoryAction.NOTHING) {
                        if (event.getCurrentItem().getType() != Material.AIR) {
                            ItemStack item = event.getCurrentItem();
                            if (item.getType() == Material.FURNACE) {
                                event.setCancelled(true);
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

}
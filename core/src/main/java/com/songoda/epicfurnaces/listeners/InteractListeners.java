package com.songoda.epicfurnaces.listeners;

import com.songoda.epicfurnaces.EpicFurnaces;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Created by songoda on 2/26/2017.
 */
public class InteractListeners implements Listener {

    private final EpicFurnaces instance;

    public InteractListeners(EpicFurnaces instance) {
        this.instance = instance;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClick(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null
                && instance.getBlacklistHandler().isBlacklisted(event.getPlayer())) {
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (!player.hasPermission("EpicFurnaces.overview")
                || event.getAction() != Action.LEFT_CLICK_BLOCK
                || player.isSneaking()
                || (block.getType() != Material.FURNACE && !block.getType().name().equals("BURNING_FURNACE"))
                || player.getInventory().getItemInHand().getType().name().contains("PICKAXE")) {
            return;
        }
        event.setCancelled(true);
        instance.getFurnaceManager().getFurnace(block.getLocation()).orElseGet(() -> instance.getFurnaceManager().createFurnace(block.getLocation())).openOverview(player);
    }
}
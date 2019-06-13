package com.songoda.epicfurnaces.listeners;

import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.furnace.Furnace;
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

    private final EpicFurnaces plugin;

    public InteractListeners(EpicFurnaces plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClick(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;

        if (plugin.getBlacklistHandler().isBlacklisted(event.getPlayer())) {
            return;
        }
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (!player.hasPermission("EpicFurnaces.overview")
                || event.getAction() != Action.LEFT_CLICK_BLOCK
                || player.isSneaking()
                || (!block.getType().name().contains("FURNACE"))
                || player.getInventory().getItemInHand().getType().name().contains("PICKAXE")) {
            return;
        }

        event.setCancelled(true);

        plugin.getFurnaceManager().getFurnace(block.getLocation()).overview(player);
    }
}
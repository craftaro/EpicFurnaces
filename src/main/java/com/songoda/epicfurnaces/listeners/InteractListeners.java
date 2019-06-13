package com.songoda.epicfurnaces.listeners;

import com.songoda.epicfurnaces.EpicFurnacesPlugin;
import com.songoda.epicfurnaces.furnace.EFurnace;
import com.songoda.epicfurnaces.utils.Debugger;
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

    private final EpicFurnacesPlugin instance;

    public InteractListeners(EpicFurnacesPlugin instance) {
        this.instance = instance;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClick(PlayerInteractEvent event) {
        try {
            if (event.getClickedBlock() == null) return;

            if (instance.getBlacklistHandler().isBlacklisted(event.getPlayer())) {
                return;
            }
            Player player = event.getPlayer();
            Block block = event.getClickedBlock();
            if (!player.hasPermission("EpicFurnaces.overview")
                    || !instance.canBuild(player, event.getClickedBlock().getLocation())
                    || event.getAction() != Action.LEFT_CLICK_BLOCK
                    || player.isSneaking()
                    || (block.getType() != Material.FURNACE)
                    || player.getInventory().getItemInMainHand().getType().name().contains("PICKAXE")) {
                return;
            }

            event.setCancelled(true);

            ((EFurnace) instance.getFurnaceManager().getFurnace(block.getLocation())).openOverview(player);

        } catch (Exception ee) {
            Debugger.runReport(ee);
        }
    }
}
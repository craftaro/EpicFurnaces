package com.songoda.epicfurnaces.listeners;

import com.songoda.core.gui.GuiManager;
import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.skyblock.SkyBlock;
import org.bukkit.Bukkit;
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
    private final GuiManager guiManager;

    public InteractListeners(EpicFurnaces plugin, GuiManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClick(PlayerInteractEvent event) {
        final Block block = event.getClickedBlock();
        if (block == null) return;

        if (plugin.getBlacklistHandler().isBlacklisted(block.getWorld())) {
            return;
        }
        Player player = event.getPlayer();
        if (event.getAction() != Action.LEFT_CLICK_BLOCK
                || !block.getType().name().contains("FURNACE") && !block.getType().name().contains("SMOKER")
                || player.isSneaking()
                || player.getInventory().getItemInHand().getType().name().contains("PICKAXE")
                || !player.hasPermission("EpicFurnaces.overview")) {
            return;
        }
    
        if (Bukkit.getPluginManager().isPluginEnabled("FabledSkyBlock")) {
            SkyBlock skyBlock = SkyBlock.getInstance();
        
            if (skyBlock.getWorldManager().isIslandWorld(event.getPlayer().getWorld()))
                if (!skyBlock.getPermissionManager().hasPermission(event.getPlayer(),
                        skyBlock.getIslandManager().getIslandAtLocation(event.getClickedBlock().getLocation()),
                        "EpicFurnaces"))
                    return;
        }
    
    
        event.setCancelled(true);

        plugin.getFurnaceManager().getFurnace(block.getLocation()).overview(guiManager, player);
    }
}
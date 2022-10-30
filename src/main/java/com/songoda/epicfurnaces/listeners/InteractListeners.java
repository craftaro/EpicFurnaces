package com.songoda.epicfurnaces.listeners;

import com.songoda.core.gui.GuiManager;
import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.furnace.Furnace;
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
public final class InteractListeners implements Listener {

    private final EpicFurnaces plugin;
    private final GuiManager guiManager;

    public InteractListeners(EpicFurnaces plugin, GuiManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClick(PlayerInteractEvent event) {
        final Block block = event.getClickedBlock();
        if (block == null || plugin.getBlacklistHandler().isBlacklisted(block.getWorld())) {
            return;
        }
        final Player player = event.getPlayer();
        if (event.getAction() != Action.LEFT_CLICK_BLOCK
                || !block.getType().name().contains("FURNACE") && !block.getType().name().contains("SMOKER")
                || player.isSneaking()
                || player.getInventory().getItemInHand().getType().name().contains("PICKAXE")
                || !player.hasPermission("EpicFurnaces.overview")) {
            return;
        }
    
        if (Bukkit.getPluginManager().isPluginEnabled("FabledSkyBlock")) {
            final SkyBlock skyBlock = SkyBlock.getInstance();
            if (skyBlock.getWorldManager().isIslandWorld(player.getWorld())
                    && !skyBlock.getPermissionManager().hasPermission(player, skyBlock.getIslandManager().getIslandAtLocation(event.getClickedBlock().getLocation()), "EpicFurnaces"))
                    return;
        }
    

        final Furnace furnace = plugin.getFurnaceManager().getFurnace(block.getLocation());
        if (furnace == null) {
            return;
        }

        event.setCancelled(true);

        furnace.overview(guiManager, player);
    }
}
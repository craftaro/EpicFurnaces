package com.songoda.epicfurnaces.listeners;

import com.songoda.core.gui.GuiManager;
import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.furnace.Furnace;
import com.songoda.epichoppers.EpicHoppers;
import com.songoda.epichoppers.hopper.Hopper;
import com.songoda.epichoppers.player.PlayerData;
import com.songoda.epichoppers.player.SyncType;
import com.songoda.skyblock.SkyBlock;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

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
        if (block == null) {
            return;
        }

        if (this.plugin.getBlacklistHandler().isBlacklisted(block.getWorld())) {
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

            if (skyBlock.getWorldManager().isIslandWorld(event.getPlayer().getWorld())) {
                if (!skyBlock.getPermissionManager().hasPermission(event.getPlayer(),
                        skyBlock.getIslandManager().getIslandAtLocation(event.getClickedBlock().getLocation()),
                        "EpicFurnaces")) {
                    return;
                }
            }
        }

        //EpicHoppers compatibility
        if (Bukkit.getPluginManager().isPluginEnabled("EpicHoppers")) {
            PlayerData playerData = EpicHoppers.getInstance().getPlayerDataManager().getPlayerData(player);
            if (playerData != null) {
                if (playerData.getSyncType() == SyncType.REGULAR) {
                    return;
                }
            }
        }


        Furnace furnace = plugin.getFurnaceManager().getFurnace(block.getLocation());
        if (furnace == null) {
            return;
        }

        event.setCancelled(true);

        furnace.overview(this.guiManager, player);
    }
}

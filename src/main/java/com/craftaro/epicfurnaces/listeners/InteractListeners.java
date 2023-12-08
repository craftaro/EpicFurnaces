package com.craftaro.epicfurnaces.listeners;

import com.craftaro.epicfurnaces.EpicFurnaces;
import com.craftaro.epicfurnaces.furnace.Furnace;
import com.craftaro.epichoppers.EpicHoppersApi;
import com.craftaro.epichoppers.player.PlayerData;
import com.craftaro.epichoppers.player.SyncType;
import com.craftaro.core.compatibility.CompatibleHand;
import com.craftaro.core.gui.GuiManager;
import com.craftaro.skyblock.SkyBlock;
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
                || CompatibleHand.MAIN_HAND.getItem(player).getType().name().contains("PICKAXE")
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
            PlayerData playerData = EpicHoppersApi.getApi().getPlayerDataManager().getPlayerData(player);
            if (playerData != null) {
                if (playerData.getSyncType() == SyncType.REGULAR) {
                    return;
                }
            }
        }


        Furnace furnace = this.plugin.getFurnaceManager().getFurnace(block.getLocation());
        if (furnace == null) {
            return;
        }

        event.setCancelled(true);

        furnace.overview(this.guiManager, player);
    }
}

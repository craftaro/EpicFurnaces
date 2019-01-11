package com.songoda.epicfurnaces.hook;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Represents a hooks for a protection plugin. This is used by EpicSpawners to determine
 * whether a block break should be successful or not according to the current state of
 * another plugin. For plugins providing claims with unique String IDs, see the
 * {@link ClaimableProtectionPluginHook} for a more detailed implementation.
 */
public interface ProtectionPluginHook {

    /**
     * The plugin to which this plugin hooks belongs. Must not be null
     * 
     * @return the hooking plugin
     */
    JavaPlugin getPlugin();

    /**
     * Check whether the provided player may build at the specified location
     *
     * @param player the player to check
     * @param location the location to check
     *
     * @return true if player is permitted to build, false otherwise
     */
    boolean canBuild(Player player, Location location);

    /**
     * Check whether the provided player may build at the specified block
     * 
     * @param player the player to check
     * @param block the block to check
     * 
     * @return true if player is permitted to build, false otherwise
     */
    default boolean canBuild(Player player, Block block) {
        return block != null && canBuild(player, block.getLocation());
    }

}
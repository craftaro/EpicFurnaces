package com.songoda.epicfurnaces.hooks;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldGuard6Hook implements ProtectionPluginHook {
    private final WorldGuardPlugin worldGuard;

    public WorldGuard6Hook() {
        this.worldGuard = WorldGuardPlugin.inst();
    }

    public JavaPlugin getPlugin() {
        return WorldGuardPlugin.inst();
    }

    public boolean canBuild(Player player, Location location) {
        return worldGuard.canBuild(player, location);
    }
}

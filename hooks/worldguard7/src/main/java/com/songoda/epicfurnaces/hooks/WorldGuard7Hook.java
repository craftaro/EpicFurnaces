package com.songoda.epicfurnaces.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.songoda.epicfurnaces.hook.ProtectionPluginHook;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldGuard7Hook implements ProtectionPluginHook {
    private final WorldGuard worldGuard;

    public WorldGuard7Hook() {
        this.worldGuard = WorldGuard.getInstance();
    }

    public JavaPlugin getPlugin() {
        return WorldGuardPlugin.inst();
    }

    public boolean canBuild(Player player, Location location) {
        RegionQuery q = worldGuard.getPlatform().getRegionContainer().createQuery();
        ApplicableRegionSet ars = q.getApplicableRegions(BukkitAdapter.adapt(player.getLocation()));
        return ars.testState(WorldGuardPlugin.inst().wrapPlayer(player), Flags.BUILD);
    }
}

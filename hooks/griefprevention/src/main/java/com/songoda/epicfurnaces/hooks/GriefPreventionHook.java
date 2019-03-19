package com.songoda.epicfurnaces.hooks;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class GriefPreventionHook implements ProtectionPluginHook {

    private final GriefPrevention griefPrevention;

    public GriefPreventionHook() {
        this.griefPrevention = GriefPrevention.instance;
    }

    public JavaPlugin getPlugin() {
        return griefPrevention;
    }

    public boolean canBuild(Player player, Location location) {
        Claim claim = griefPrevention.dataStore.getClaimAt(location, false, null);
        return claim != null && claim.allowBuild(player, Material.STONE) == null;
    }

}

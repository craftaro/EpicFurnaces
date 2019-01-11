package com.songoda.epicfurnaces;

import com.songoda.epicfurnaces.hook.ClaimableProtectionPluginHook;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import us.talabrek.ultimateskyblock.api.uSkyBlockAPI;

public class USkyBlockHook implements ClaimableProtectionPluginHook {

    private final uSkyBlockAPI uSkyBlockAPI;

    public USkyBlockHook() {
        this.uSkyBlockAPI = (uSkyBlockAPI) Bukkit.getPluginManager().getPlugin("USkyBlock");
    }

    public JavaPlugin getPlugin() { // uSkyBlockAPI is also an instance of JavaPlugin
        return (JavaPlugin) uSkyBlockAPI;
    }

    public boolean canBuild(Player player, Location location) {
        return uSkyBlockAPI.getIslandInfo(location).getOnlineMembers().contains(player) || uSkyBlockAPI.getIslandInfo(location).isLeader(player);
    }

    public boolean isInClaim(Location location, String id) {
        return uSkyBlockAPI.getIslandInfo(location).getLeader().equals(id);
    }

    public String getClaimID(String name) {
        return null;
    }

}

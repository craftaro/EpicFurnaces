package com.songoda.epicfurnaces.hooks;

import com.songoda.epicfurnaces.hook.ClaimableProtectionPluginHook;
import me.markeh.factionsframework.FactionsFramework;
import me.markeh.factionsframework.entities.FPlayer;
import me.markeh.factionsframework.entities.FPlayers;
import me.markeh.factionsframework.entities.Faction;
import me.markeh.factionsframework.entities.Factions;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class FactionsHook implements ClaimableProtectionPluginHook {

    private final FactionsFramework factions;

    public FactionsHook() {
        this.factions = FactionsFramework.get();
    }

    public JavaPlugin getPlugin() {
        return factions;
    }

    public boolean canBuild(Player player, Location location) {
        FPlayer fPlayer = FPlayers.getBySender(player);
        Faction faction = Factions.getFactionAt(location);

        return faction.isNone() || fPlayer.getFaction().equals(faction);
    }

    public boolean isInClaim(Location location, String id) {
        return Factions.getFactionAt(location).getId().equals(id);
    }

    public String getClaimID(String name) {
        Faction faction = Factions.getByName(name, "");
        return (faction != null) ? faction.getId() : null;
    }

}
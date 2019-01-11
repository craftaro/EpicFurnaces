package com.songoda.epicfurnaces.hooks;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.songoda.epicfurnaces.hook.ClaimableProtectionPluginHook;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TownyHook implements ClaimableProtectionPluginHook {

    private final Towny towny;

    public TownyHook() {
        this.towny = Towny.getPlugin();
    }

    public JavaPlugin getPlugin() {
        return towny;
    }

    public boolean canBuild(Player player, Location location) {
        if (TownyUniverse.isWilderness(location.getBlock()) || !TownyUniverse.getTownBlock(location).hasTown())
            return true;

        try {
            Resident resident = TownyUniverse.getDataSource().getResident(player.getName());
            return resident.hasTown() && TownyUniverse.getTownName(location).equals(resident.getTown().getName());
        } catch (NotRegisteredException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isInClaim(Location location, String id) {
        try {
            return TownyUniverse.isWilderness(location.getBlock()) && TownyUniverse.getTownBlock(location).getTown().getUID().toString().equals(id);
        } catch (NotRegisteredException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getClaimID(String name) {
        try {
            return TownyUniverse.getDataSource().getTown(name).getUID().toString();
        } catch (NotRegisteredException e) {
            e.printStackTrace();
            return null;
        }
    }

}
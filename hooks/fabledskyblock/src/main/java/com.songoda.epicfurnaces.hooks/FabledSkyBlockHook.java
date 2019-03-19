package com.songoda.epicfurnaces.hooks;

import me.goodandevil.skyblock.SkyBlock;
import me.goodandevil.skyblock.api.SkyBlockAPI;
import me.goodandevil.skyblock.api.island.Island;
import me.goodandevil.skyblock.api.island.IslandRole;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public class FabledSkyBlockHook implements ProtectionPluginHook {

    private final Set<IslandRole> allowedRoles = new HashSet<IslandRole>() {{
        add(IslandRole.COOP);
        add(IslandRole.MEMBER);
        add(IslandRole.OPERATOR);
        add(IslandRole.OWNER);
    }};

    public boolean canBuild(Player player, Location location) {
        Island island = SkyBlockAPI.getIslandManager().getIslandAtLocation(location);
        return allowedRoles.stream().anyMatch(role -> island.getPlayersWithRole(role).contains(player.getUniqueId())) ||
                island.getCoopPlayers().contains(player.getUniqueId());
    }

    @Override
    public JavaPlugin getPlugin() {
        return SkyBlock.getInstance();
    }
}

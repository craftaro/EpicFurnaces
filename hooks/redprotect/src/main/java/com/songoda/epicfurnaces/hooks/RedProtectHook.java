package com.songoda.epicfurnaces.hooks;

import br.net.fabiozumbi12.RedProtect.Bukkit.API.RedProtectAPI;
import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import com.songoda.epicfurnaces.hook.ProtectionPluginHook;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class RedProtectHook implements ProtectionPluginHook {

    private final RedProtect redProtect;

    public RedProtectHook() {
        this.redProtect = RedProtect.get();
    }

    public JavaPlugin getPlugin() {
        return redProtect;
    }

    public boolean canBuild(Player player, Location location) {
        RedProtectAPI api = redProtect.getAPI();
        Region region = api.getRegion(location);

        return region != null && region.canBuild(player);
    }

}
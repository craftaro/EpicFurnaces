package com.songoda.epicfurnaces.handlers;

import com.craftaro.core.configuration.Config;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;

public class BlacklistHandler {
    private final Config blackConfig;

    public BlacklistHandler(Plugin plugin) {
        this.blackConfig = new Config(plugin, "blacklist.yml");
        loadBlacklistFile();
    }

    public boolean isBlacklisted(World world) {
        List<String> list = this.blackConfig.getStringList("settings.blacklist");
        final String checkWorld = world.getName();
        return list.stream().anyMatch(w -> w.equalsIgnoreCase(checkWorld));
    }

    private void loadBlacklistFile() {
        this.blackConfig.addDefault("settings.blacklist", Arrays.asList("world2", "world3", "world4", "world5"));
        this.blackConfig.load();

        this.blackConfig.saveChanges();
    }

    public void reload() {
        loadBlacklistFile();
    }
}

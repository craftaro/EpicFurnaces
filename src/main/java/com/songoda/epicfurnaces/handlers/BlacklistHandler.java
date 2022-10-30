package com.songoda.epicfurnaces.handlers;

import com.songoda.core.configuration.Config;
import com.songoda.epicfurnaces.EpicFurnaces;
import org.bukkit.World;

import java.util.Arrays;
import java.util.List;

/**
 * Created by songoda on 2/25/2017.
 */
public final class BlacklistHandler {

    private final Config blackConfig = new Config(EpicFurnaces.getInstance(), "blacklist.yml");

    public BlacklistHandler() {
        loadBlacklistFile();
    }

    public boolean isBlacklisted(World world) {
        final List<String> list = blackConfig.getStringList("settings.blacklist");
        final String checkWorld = world.getName();
        return list.stream().anyMatch(w -> w.equalsIgnoreCase(checkWorld));
    }

    private void loadBlacklistFile() {
        blackConfig.addDefault("settings.blacklist", Arrays.asList("world2", "world3", "world4", "world5"));
        blackConfig.load();

        blackConfig.saveChanges();
    }

    public void reload() {
        loadBlacklistFile();
    }
}
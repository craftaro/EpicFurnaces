package com.songoda.epicfurnaces.handlers;

import com.songoda.core.configuration.Config;
import com.songoda.epicfurnaces.EpicFurnaces;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by songoda on 2/25/2017.
 */
public class BlacklistHandler {

    private Config blackConfig = new Config(EpicFurnaces.getInstance(), "blacklist.yml");

    public BlacklistHandler() {
        blackConfig.load();
        loadBlacklistFile();
    }

    public boolean isBlacklisted(Player player) {
        List<String> list = blackConfig.getStringList("settings.blacklist");
        String cWorld = player.getWorld().getName();
        for (String world : list) {
            if (cWorld.equalsIgnoreCase(world)) {
                return true;
            }
        }
        return false;
    }

    private void loadBlacklistFile() {
        List<String> list = new ArrayList<>();
        list.add("world2");
        list.add("world3");
        list.add("world4");
        list.add("world5");
        blackConfig.addDefault("settings.blacklist", list);

        blackConfig.options().copyDefaults(true);
        blackConfig.save();
    }

    public void reload() {
        blackConfig.load();
        loadBlacklistFile();
    }
}
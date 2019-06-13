package com.songoda.epicfurnaces.handlers;

import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.utils.ConfigWrapper;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by songoda on 2/25/2017.
 */
public class BlacklistHandler {

    private ConfigWrapper blackFile = new ConfigWrapper(EpicFurnaces.getInstance(), "", "blacklist.yml");

    public BlacklistHandler() {
        blackFile.createNewFile("Loading language file", "EpicFurnaces blacklist file");
        loadBlacklistFile();
    }

    public boolean isBlacklisted(Player player) {
        List<String> list = blackFile.getConfig().getStringList("settings.blacklist");
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
        blackFile.getConfig().addDefault("settings.blacklist", list);

        blackFile.getConfig().options().copyDefaults(true);
        blackFile.saveConfig();
    }

    public void reload() {
        blackFile.createNewFile("Loading blacklist file", "EpicFurnaces blacklist file");
        loadBlacklistFile();
    }
}
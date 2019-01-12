package com.songoda.epicfurnaces.handlers;

import com.songoda.epicfurnaces.EpicFurnaces;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Created by songoda on 2/25/2017.
 */
public class BlacklistHandler {

    private final EpicFurnaces instance;
    private FileConfiguration blackFile;

    public BlacklistHandler(EpicFurnaces instance) {
        this.instance = instance;
        this.blackFile = instance.getConfiguration("blacklist");
    }

    public boolean isBlacklisted(Player player) {
        boolean blacklisted = false;
        List<String> list = blackFile.getStringList("settings.blacklist");
        String cWorld = player.getWorld().getName();
        for (String world : list) {
            if (cWorld.equalsIgnoreCase(world)) {
                blacklisted = true;
            }
        }
        return blacklisted;
    }

    public void reload() {
        instance.save("blacklist");
        blackFile = instance.getConfiguration("blacklist");
    }
}
package com.songoda.epicfurnaces.handlers;

import com.songoda.arconix.api.utils.ConfigWrapper;
import com.songoda.epicfurnaces.EpicFurnacesPlugin;
import com.songoda.epicfurnaces.utils.Debugger;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by songoda on 2/25/2017.
 */
public class BlacklistHandler {

    private ConfigWrapper blackFile = new ConfigWrapper(EpicFurnacesPlugin.getInstance(), "", "blacklist.yml");

    public BlacklistHandler() {
        try {
            blackFile.createNewFile("Loading language file", "EpicFurnaces blacklist file");
            loadBlacklistFile();
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public boolean isBlacklisted(Player player) {
        boolean blacklisted = false;
        try {
            List<String> list = blackFile.getConfig().getStringList("settings.blacklist");
            String cWorld = player.getWorld().getName();
            for (String world : list) {
                if (cWorld.equalsIgnoreCase(world)) {
                    blacklisted = true;
                }
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return blacklisted;
    }

    private void loadBlacklistFile() {
        try {
            List<String> list = new ArrayList<>();
            list.add("world2");
            list.add("world3");
            list.add("world4");
            list.add("world5");
            blackFile.getConfig().addDefault("settings.blacklist", list);

            blackFile.getConfig().options().copyDefaults(true);
            blackFile.saveConfig();
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void reload() {
        try {
            blackFile.createNewFile("Loading blacklist file", "EpicFurnaces blacklist file");
            loadBlacklistFile();
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }
}
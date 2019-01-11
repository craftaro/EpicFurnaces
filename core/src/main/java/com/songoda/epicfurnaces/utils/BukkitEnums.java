package com.songoda.epicfurnaces.utils;

import com.songoda.epicfurnaces.EpicFurnaces;
import org.bukkit.Material;
import org.bukkit.Sound;

public class BukkitEnums {
    private final EpicFurnaces instance;

    public BukkitEnums(EpicFurnaces instance) {
        this.instance = instance;
    }

    public Material getWool() {
        return Material.valueOf(instance.getCurrentVersion() > 9 ? "WHITE_WOOL" : "WOOL");
    }

    public Material getWetSponge() {
        return Material.valueOf(instance.getCurrentVersion() > 9 ? "WET_SPONGE" : "SPONGE");
    }

    public Material getBurningFurnace() {
        return Material.valueOf(instance.getCurrentVersion() < 9 ? "BURNING_FURNACE" : "FURNACE");
    }

    public Sound getLevelUp() {
        return Sound.valueOf(instance.getCurrentVersion() > 9 ? "PLAYER_LEVEL_UP" : "LEVEL_UP");
    }
}

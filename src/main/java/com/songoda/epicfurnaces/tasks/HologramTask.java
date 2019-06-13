package com.songoda.epicfurnaces.tasks;

import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.furnace.Furnace;
import com.songoda.epicfurnaces.utils.ServerVersion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

public class HologramTask extends BukkitRunnable {

    private static HologramTask instance;

    private final EpicFurnaces plugin;

    private HologramTask(EpicFurnaces plugin) {
        this.plugin = plugin;
    }

    public static HologramTask startTask(EpicFurnaces plugin) {
        if (instance == null) {
            instance = new HologramTask(plugin);
            instance.runTaskTimer(plugin, 0, 10);
        }

        return instance;
    }

    @Override
    public void run() {
        for (Furnace furnace : plugin.getFurnaceManager().getFurnaces().values()) {
            plugin.getHologram().update(furnace);
        }
    }
}
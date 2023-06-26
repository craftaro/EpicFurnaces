package com.songoda.epicfurnaces.tasks;

import com.songoda.core.hooks.HologramManager;
import com.songoda.epicfurnaces.EpicFurnaces;
import org.bukkit.scheduler.BukkitRunnable;

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
        if (!HologramManager.getManager().isEnabled()) {
            return;
        }

        this.plugin.updateHolograms(this.plugin.getFurnaceManager().getFurnaces().values());
    }
}

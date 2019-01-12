package com.songoda.epicfurnaces.tasks;

import com.songoda.epicfurnaces.EpicFurnaces;
import org.bukkit.scheduler.BukkitRunnable;

public class HologramTask extends BukkitRunnable {

    private static HologramTask task;
    private final EpicFurnaces instance;

    private HologramTask(EpicFurnaces instance) {
        this.instance = instance;
    }

    public static void startTask(EpicFurnaces plugin) {
        if (task == null) {
            task = new HologramTask(plugin);
            task.runTaskTimer(plugin, 0, 20L);
        }
    }

    @Override
    public void run() {
        if (!instance.getConfig().getBoolean("Main.Furnaces Have Holograms")) {
            return;
        }

        if (instance.getFurnaceManager() == null) {
            return;
        }

        instance.getFurnaceManager().getFurnaces().values().stream()
                .filter(furnace -> furnace.getLocation() != null && furnace.getLocation().getWorld() != null && furnace.getLocation().getBlock() != null)
                .forEach(furnace -> instance.getHologramManager().updateHologram(furnace));
    }
}

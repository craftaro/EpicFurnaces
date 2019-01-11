package com.songoda.epicfurnaces.tasks;

import com.songoda.arconix.api.methods.formatting.TextComponent;
import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.furnace.FurnaceObject;
import com.songoda.epicfurnaces.utils.Debugger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class HologramTask extends BukkitRunnable {

    private static HologramTask instance;

    private final EpicFurnaces plugin;

    private HologramTask(EpicFurnaces plugin) {
        this.plugin = plugin;
    }

    public static HologramTask startTask(EpicFurnaces plugin) {
        if (instance == null) {
            instance = new HologramTask(plugin);
            instance.runTaskTimer(plugin, 0, 20);
        }

        return instance;
    }

    @Override
    public void run() {
        if (!plugin.getConfig().getBoolean("Main.Furnaces Have Holograms")) {
            return;
        }

        if (plugin.getFurnaceManager() == null) {
            return;
        }

        for (FurnaceObject furnace : plugin.getFurnaceManager().getFurnaces().values()) {
            if (furnace.getLocation() == null || furnace.getLocation().getWorld() == null || furnace.getLocation().getBlock() == null) {
                continue;
            }

            if (furnace.getLocation().getBlock().getType() != Material.FURNACE) {
                continue;
            }

            Location location = furnace.getLocation().add(0.5, 1.25, 0.5);
            addHologram(location, furnace);
        }
    }

    public void despawn(Block b) {
        Location location = b.getLocation().add(0.5, 1.25, 0.5);
        Arconix.pl().getApi().packetLibrary.getHologramManager().despawnHologram(location);
    }

    public void addHologram(Location location, FurnaceObject furnace) {
        try {
            Furnace furnaceBlock = ((Furnace) furnace.getLocation().getBlock().getState());

            int performance = (furnaceBlock.getCookTime() - furnace.getPerformanceTotal()) <= 0 ? 0 : furnace.getPerformanceTotal();
            float percent = (float) (furnaceBlock.getCookTime() - performance) / (200 - performance);
            int progressBars = (int) (6 * percent) + (percent == 0 ? 0 : 1);
            int leftOver = (6 - progressBars);

            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < progressBars; i++) {
                sb.append("&a=");
            }

            for (int i = 0; i < leftOver; i++) {
                sb.append("&c=");
            }

            ArrayList<String> list = new ArrayList<>();

            String progress = TextComponent.formatText(sb.toString());

            if (furnaceBlock.getInventory().getFuel() == null) {
                progress = plugin.getLocale().getMessage("general.hologram.outoffuel");
            }

            int inAmt = 0;
            int outAmt = 0;
            if (furnaceBlock.getInventory().getSmelting() != null) {
                inAmt = furnaceBlock.getInventory().getSmelting().getAmount();
            }
            if (furnaceBlock.getInventory().getResult() != null) {
                outAmt = furnaceBlock.getInventory().getResult().getAmount();
            }

            String stats = plugin.getLocale().getMessage("general.hologram.stats", inAmt, outAmt > 64 ? 64 : outAmt);
            list.add(progress);
            list.add(stats);

            Arconix.pl().getApi().packetLibrary.getHologramManager().spawnHolograms(location, list);

        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }
}

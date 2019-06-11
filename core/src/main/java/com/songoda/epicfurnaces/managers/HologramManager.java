package com.songoda.epicfurnaces.managers;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.objects.FurnaceObject;
import com.songoda.epicfurnaces.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Furnace;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HologramManager {
    private final Map<FurnaceObject, Hologram> hologramMap;
    private final EpicFurnaces instance;

    public HologramManager(EpicFurnaces instance) {
        this.hologramMap = new HashMap<>();
        this.instance = instance;
    }

    public void updateHologram(FurnaceObject furnaceObject) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(instance, () -> {
            if (furnaceObject.getLocation().getBlock().getType() != Material.FURNACE &&
                    !furnaceObject.getLocation().getBlock().getType().name().equals("BURNING_FURNACE")) {
                if (hologramMap.containsKey(furnaceObject)) {
                    hologramMap.remove(furnaceObject).delete();
                }
                return;
            }

            Furnace furnaceBlock = (Furnace) furnaceObject.getLocation().getBlock().getState();

            int performance = (furnaceBlock.getCookTime() - furnaceObject.getPerformanceTotal()) <= 0 ? 0 : furnaceObject.getPerformanceTotal();
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

            String progress = StringUtils.formatText(sb.toString());

            if (furnaceBlock.getInventory().getFuel() == null) {
                progress = instance.getLocale().getMessage("general.hologram.outoffuel");
            }

            int inAmt = 0;
            int outAmt = 0;

            if (furnaceBlock.getInventory().getSmelting() != null) {
                inAmt = furnaceBlock.getInventory().getSmelting().getAmount();
            }

            if (furnaceBlock.getInventory().getResult() != null) {
                outAmt = furnaceBlock.getInventory().getResult().getAmount();
            }

            String stats = instance.getLocale().getMessage("general.hologram.stats", inAmt, outAmt > 64 ? 64 : outAmt);
            Hologram hologram;

            if (!hologramMap.containsKey(furnaceObject)) {
                BlockFace direction = ((org.bukkit.material.Furnace) furnaceBlock.getData()).getFacing();
                Location location = instance.getConfig().getBoolean("Main.Hologram in front") ?
                        furnaceObject.getLocation().getBlock().getRelative(direction).getLocation().add(0.5, 0.8, 0.5) :
                        furnaceBlock.getLocation().add(0.5, 1.8, 0.5);
                hologram = HologramsAPI.createHologram(instance, location);
            } else {
                hologram = hologramMap.get(furnaceObject);
            }

            if (hologram.size() == 0) {
                hologram.clearLines();
                hologram.insertTextLine(0, progress);
                hologram.insertTextLine(1, stats);
            } else {
                ((TextLine) hologram.getLine(0)).setText(progress);
                ((TextLine) hologram.getLine(1)).setText(stats);
            }
            hologramMap.put(furnaceObject, hologram);
        });
    }

    public void remove(FurnaceObject furnaceObject) {
        Optional.ofNullable(hologramMap.remove(furnaceObject)).ifPresent(Hologram::delete);
    }


    public void clearAll() {
        hologramMap.values().forEach(Hologram::delete);
        hologramMap.clear();
    }
}

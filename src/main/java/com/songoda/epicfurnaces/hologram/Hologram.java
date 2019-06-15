package com.songoda.epicfurnaces.hologram;

import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.furnace.Furnace;
import com.songoda.epicfurnaces.utils.Methods;
import org.bukkit.Location;
import org.bukkit.block.BlockState;

import java.util.ArrayList;

public abstract class Hologram {

    protected final EpicFurnaces plugin;

    Hologram(EpicFurnaces plugin) {
        this.plugin = plugin;
    }

    public void loadHolograms() {
        if (!plugin.getConfig().getBoolean("Main.Furnaces Have Holograms")) return;

        for (Furnace furnace : plugin.getFurnaceManager().getFurnaces().values()) {
            if (furnace.getLocation() == null || furnace.getLocation().getWorld() == null)
                continue;
            if (!furnace.getLocation().getBlock().getType().name().contains("FURNACE")) continue;

            add(furnace);
        }
    }

    public void unloadHolograms() {
        for (Furnace furnace : plugin.getFurnaceManager().getFurnaces().values()) {
            if (furnace.getLocation() == null || furnace.getLocation().getWorld() == null)
                continue;
            if (!furnace.getLocation().getBlock().getType().name().contains("FURNACE")) continue;

            remove(furnace);
        }
    }

    public void add(Furnace furnace) {
        format(furnace, Action.ADD);
    }

    public void remove(Furnace furnace) {
        format(furnace, Action.REMOVE);
    }

    public void update(Furnace furnace) {
        format(furnace, Action.UPDATE);
    }

    private void format(Furnace furnace, Action action) {

        Location location = furnace.getLocation();

        if (!location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4))
            return;

        BlockState state = furnace.getLocation().getBlock().getState();

        if (!(state instanceof org.bukkit.block.Furnace)) return;

        org.bukkit.block.Furnace furnaceBlock = ((org.bukkit.block.Furnace) state);

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

        ArrayList<String> lines = new ArrayList<>();

        String progress = Methods.formatText(sb.toString());

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
        lines.add(progress);
        lines.add(stats);

        switch (action) {
            case UPDATE:
                update(location, lines);
                break;
            case ADD:
                add(location, lines);
                break;
            case REMOVE:
                remove(location);
                break;
        }
    }

    protected abstract void add(Location location, ArrayList<String> lines);

    protected abstract void remove(Location location);

    protected abstract void update(Location location, ArrayList<String> lines);

    public enum Action {

        UPDATE, ADD, REMOVE

    }

}

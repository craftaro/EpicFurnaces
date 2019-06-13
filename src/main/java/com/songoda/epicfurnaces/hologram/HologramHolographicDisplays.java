package com.songoda.epicfurnaces.hologram;

import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.songoda.epicfurnaces.EpicFurnaces;
import org.bukkit.Location;

import java.util.ArrayList;

public class HologramHolographicDisplays extends Hologram {


    public HologramHolographicDisplays(EpicFurnaces plugin) {
        super(plugin);
    }

    @Override
    public void add(Location location, ArrayList<String> lines) {
        fixLocation(location);

        com.gmail.filoghost.holographicdisplays.api.Hologram hologram = HologramsAPI.createHologram(plugin, location);
        for (String line : lines) {
            hologram.appendTextLine(line);
        }
    }

    @Override
    public void remove(Location location) {
        fixLocation(location);
        for (com.gmail.filoghost.holographicdisplays.api.Hologram hologram : HologramsAPI.getHolograms(plugin)) {
            if (hologram.getX() != location.getX()
                    || hologram.getY() != location.getY()
                    || hologram.getZ() != location.getZ()) continue;
            hologram.delete();
        }
    }

    @Override
    public void update(Location location, ArrayList<String> lines) {
        for (com.gmail.filoghost.holographicdisplays.api.Hologram hologram : HologramsAPI.getHolograms(plugin)) {
            if (hologram.getX() != location.getX()
                    || hologram.getY() != location.getY()
                    || hologram.getZ() != location.getZ()) continue;
            fixLocation(location);
            hologram.clearLines();
            for (String line : lines) {
                hologram.appendTextLine(line);
            }
            return;
        }
        add(location, lines);
    }

    private void fixLocation(Location location) {
        location.add(.5, 1.8, .5);
    }
}

package com.songoda.epicfurnaces.furnace;

import com.songoda.epicfurnaces.EpicFurnaces;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FurnaceManager {

    private final Map<Location, Furnace> registeredFurnaces = new HashMap<>();


    public Furnace addFurnace(Location location, Furnace furnace) {
        return registeredFurnaces.put(roundLocation(location), furnace);
    }

    public Furnace removeFurnace(Location location) {
        return registeredFurnaces.remove(location);
    }

    public Furnace getFurnace(Location location) {
        if (!registeredFurnaces.containsKey(location)) {
            addFurnace(location, new Furnace(location, EpicFurnaces.getInstance().getLevelManager().getLowestLevel(), null, 0, 0, new ArrayList<>(), null));
        }
        return registeredFurnaces.get(location);
    }

    public Furnace getFurnace(Block block) {
        return getFurnace(block.getLocation());
    }

    public Map<Location, Furnace> getFurnaces() {
        return Collections.unmodifiableMap(registeredFurnaces);
    }

    private Location roundLocation(Location location) {
        location = location.clone();
        location.setX(location.getBlockX());
        location.setY(location.getBlockY());
        location.setZ(location.getBlockZ());
        return location;
    }
}

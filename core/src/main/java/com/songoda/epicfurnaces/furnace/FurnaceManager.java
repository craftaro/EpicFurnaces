package com.songoda.epicfurnaces.furnace;

import com.songoda.epicfurnaces.EpicFurnaces;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FurnaceManager {

    private final Map<Location, FurnaceObject> registeredFurnaces = new HashMap<>();


    public void addFurnace(Location location, FurnaceObject furnace) {
        registeredFurnaces.put(roundLocation(location), furnace);
    }


    public FurnaceObject removeFurnace(Location location) {
        return registeredFurnaces.remove(location);
    }


    public FurnaceObject getFurnace(Location location) {
        if (!registeredFurnaces.containsKey(location)) {
            addFurnace(location, new FurnaceObject(location, EpicFurnaces.getInstance().getLevelManager().getLowestLevel(), null, 0, 0, new ArrayList<>(), null));
        }
        return registeredFurnaces.get(location);
    }


    public FurnaceObject getFurnace(Block block) {
        return getFurnace(block.getLocation());
    }


    public Map<Location, FurnaceObject> getFurnaces() {
        return Collections.unmodifiableMap(registeredFurnaces);
    }

    private Location roundLocation(Location location) {
        return location.getBlock().getLocation().clone();
    }
}

package com.songoda.epicfurnaces.furnace;

import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.api.furnace.Furnace;
import com.songoda.epicfurnaces.api.furnace.FurnaceManager;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EFurnaceManager implements FurnaceManager {

    private final Map<Location, Furnace> registeredFurnaces = new HashMap<>();

    @Override
    public void addFurnace(Location location, Furnace furnace) {
        registeredFurnaces.put(roundLocation(location), furnace);
    }

    @Override
    public Furnace removeFurnace(Location location) {
        return registeredFurnaces.remove(location);
    }

    @Override
    public Furnace getFurnace(Location location) {
        if (!registeredFurnaces.containsKey(location)) {
            addFurnace(location, new EFurnace(location, EpicFurnaces.getInstance().getLevelManager().getLowestLevel(), null, 0, 0, new ArrayList<>(), null));
        }
        return registeredFurnaces.get(location);
    }

    @Override
    public Furnace getFurnace(Block block) {
        return getFurnace(block.getLocation());
    }

    @Override
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

package com.songoda.epicfurnaces.furnace;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.songoda.epicfurnaces.utils.GameArea;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FurnaceManager {

    private final Map<Location, Furnace> registeredFurnaces = new HashMap<>();
    private final Multimap<GameArea, Furnace> tickingFurnaces = MultimapBuilder.hashKeys().hashSetValues().build();

    public Furnace addFurnace(Furnace furnace) {
        tickingFurnaces.put(GameArea.of(furnace.getLocation()), furnace);
        return registeredFurnaces.put(roundLocation(furnace.getLocation()), furnace);
    }

    public void addFurnaces(Collection<Furnace> furnaces) {
        for (Furnace furnace : furnaces) {
            addFurnace(furnace);
        }
    }

    public Furnace removeFurnace(Location location) {
        Furnace furnace = registeredFurnaces.remove(location);
        if (furnace != null) {
            tickingFurnaces.remove(GameArea.of(furnace.getLocation()), furnace);
        }
        return furnace;
    }

    public Furnace getFurnace(Location location) {
        if (!registeredFurnaces.containsKey(location)) {
            addFurnace(new FurnaceBuilder(location).build());
        }
        return registeredFurnaces.get(location);
    }

    public Collection<Furnace> getFurnaces(GameArea gameArea) {
        return tickingFurnaces.get(gameArea);
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

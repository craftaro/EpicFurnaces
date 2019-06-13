package com.songoda.epicfurnaces.furnace;

import com.songoda.epicfurnaces.api.furnace.Level;
import com.songoda.epicfurnaces.api.furnace.LevelManager;

import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class LevelManager implements LevelManager {

    private final NavigableMap<Integer, Level> registeredLevels = new TreeMap<>();

    @Override
    public void addLevel(int level, int costExperiance, int costEconomy, int performance, String reward, int fuelDuration, int overheat, int fuelShare) {
        registeredLevels.put(level, new Level(level, costExperiance, costEconomy, performance, reward, fuelDuration, overheat, fuelShare));
    }

    @Override
    public Level getLevel(int level) {
        return registeredLevels.get(level);
    }

    @Override
    public Level getLowestLevel() {
        return registeredLevels.firstEntry().getValue();
    }

    @Override
    public Level getHighestLevel() {
        return registeredLevels.lastEntry().getValue();
    }

    @Override
    public boolean isLevel(int level) {
        return registeredLevels.containsKey(level);
    }

    @Override
    public Map<Integer, Level> getLevels() {
        return Collections.unmodifiableMap(registeredLevels);
    }

    @Override
    public void clear() {
        registeredLevels.clear();
    }
}
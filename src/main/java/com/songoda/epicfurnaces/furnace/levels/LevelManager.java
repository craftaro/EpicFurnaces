package com.songoda.epicfurnaces.furnace.levels;

import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class LevelManager {

    private final NavigableMap<Integer, Level> registeredLevels = new TreeMap<>();


    public void addLevel(int level, int costExperiance, int costEconomy, int performance, String reward, int fuelDuration, int overheat, int fuelShare) {
        registeredLevels.put(level, new Level(level, costExperiance, costEconomy, performance, reward, fuelDuration, overheat, fuelShare));
    }


    public Level getLevel(int level) {
        return registeredLevels.get(level);
    }


    public Level getLowestLevel() {
        return registeredLevels.firstEntry().getValue();
    }


    public Level getHighestLevel() {
        return registeredLevels.lastEntry().getValue();
    }


    public boolean isLevel(int level) {
        return registeredLevels.containsKey(level);
    }


    public Map<Integer, Level> getLevels() {
        return Collections.unmodifiableMap(registeredLevels);
    }


    public void clear() {
        registeredLevels.clear();
    }
}
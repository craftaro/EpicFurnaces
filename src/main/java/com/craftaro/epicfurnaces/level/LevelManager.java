package com.craftaro.epicfurnaces.level;

import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;

import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class LevelManager {
    private final NavigableMap<Integer, Level> registeredLevels = new TreeMap<>();

    public void addLevel(int level, int costExperience, int costEconomy, int performance, String reward, int fuelDuration, int overheat, int fuelShare, Map<XMaterial, Integer> materials) {
        this.registeredLevels.put(level, new Level(level, costExperience, costEconomy, performance, reward, fuelDuration, overheat, fuelShare, materials));
    }

    public Level getLevel(int level) {
        return this.registeredLevels.get(level);
    }

    public Level getLowestLevel() {
        return this.registeredLevels.firstEntry().getValue();
    }


    public Level getHighestLevel() {
        return this.registeredLevels.lastEntry().getValue();
    }


    public boolean isLevel(int level) {
        return this.registeredLevels.containsKey(level);
    }


    public Map<Integer, Level> getLevels() {
        return Collections.unmodifiableMap(this.registeredLevels);
    }

    public void clear() {
        this.registeredLevels.clear();
    }
}

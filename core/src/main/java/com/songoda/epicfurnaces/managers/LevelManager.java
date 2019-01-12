package com.songoda.epicfurnaces.managers;

import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.objects.Level;

import java.util.*;

public class LevelManager {

    private final NavigableMap<Integer, Level> registeredLevels = new TreeMap<>();
    private final EpicFurnaces instance;

    public LevelManager(EpicFurnaces instance) {
        this.instance = instance;
    }


    public void addLevel(int level, int costExperience, int costEconomy, int performance, String reward, int fuelDuration, int overheat, int fuelShare) {
        registeredLevels.put(level, new Level(instance, level, costExperience, costEconomy, performance, reward, fuelDuration, overheat, fuelShare));
    }

    public void loadLevelManager() {
        clear();
        for (String levelName : instance.getConfig().getConfigurationSection("settings.levels").getKeys(false)) {
            int level = Integer.valueOf(levelName.split("-")[1]);
            int costExperience = instance.getConfig().getInt("settings.levels." + levelName + ".Cost-xp");
            int costEconomy = instance.getConfig().getInt("settings.levels." + levelName + ".Cost-eco");

            String performanceStr = instance.getConfig().getString("settings.levels." + levelName + ".Performance");
            int performance = performanceStr == null ? 0 : Integer.parseInt(performanceStr.substring(0, performanceStr.length() - 1));

            String reward = instance.getConfig().getString("settings.levels." + levelName + ".Reward");

            String fuelDurationStr = instance.getConfig().getString("settings.levels." + levelName + ".Fuel-duration");
            int fuelDuration = fuelDurationStr == null ? 0 : Integer.parseInt(fuelDurationStr.substring(0, fuelDurationStr.length() - 1));

            int overheat = instance.getConfig().getInt("settings.levels." + levelName + ".Overheat");
            int fuelShare = instance.getConfig().getInt("settings.levels." + levelName + ".Fuel-share");

            addLevel(level, costExperience, costEconomy, performance, reward, fuelDuration, overheat, fuelShare);
        }
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
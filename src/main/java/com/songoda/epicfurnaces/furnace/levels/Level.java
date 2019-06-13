package com.songoda.epicfurnaces.furnace.levels;

import com.songoda.epicfurnaces.EpicFurnaces;

import java.util.ArrayList;
import java.util.List;

public class Level {

    private int level, costExperience, costEconomy, performance, fuelDuration, overheat, fuelShare;

    private String reward;

    private List<String> description = new ArrayList<>();

    Level(int level, int costExperience, int costEconomy, int performance, String reward, int fuelDuration, int overheat, int fuelShare) {
        this.level = level;
        this.costExperience = costExperience;
        this.costEconomy = costEconomy;
        this.performance = performance;
        this.reward = reward;
        this.fuelDuration = fuelDuration;
        this.overheat = overheat;
        this.fuelShare = fuelShare;

        EpicFurnaces plugin = EpicFurnaces.getInstance();

        if (performance != 0)
            description.add(plugin.getLocale().getMessage("interface.furnace.performance", performance + "%"));

        if (reward != null)
            description.add(plugin.getLocale().getMessage("interface.furnace.reward", reward.split("%:")[0] + "%"));

        if (fuelDuration != 0)
            description.add(plugin.getLocale().getMessage("interface.furnace.fuelduration", fuelDuration + "%"));

        if (fuelShare != 0)
            description.add(plugin.getLocale().getMessage("interface.furnace.fuelshare", fuelShare));

        if (overheat != 0)
            description.add(plugin.getLocale().getMessage("interface.furnace.overheat", overheat));
    }


    public List<String> getDescription() {
        return new ArrayList<>(description);
    }


    public int getLevel() {
        return level;
    }


    public int getPerformance() {
        return performance;
    }


    public String getReward() {
        return reward;
    }


    public int getOverheat() {
        return overheat;
    }


    public int getFuelShare() {
        return fuelShare;
    }


    public int getFuelDuration() {
        return fuelDuration;
    }


    public int getCostExperience() {
        return costExperience;
    }


    public int getCostEconomy() {
        return costEconomy;
    }
}

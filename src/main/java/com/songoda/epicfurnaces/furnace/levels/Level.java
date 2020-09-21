package com.songoda.epicfurnaces.furnace.levels;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.epicfurnaces.EpicFurnaces;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Level {

    private int level, costExperience, costEconomy, performance, fuelDuration, overheat, fuelShare;

    private Map<CompatibleMaterial, Integer> materials = new LinkedHashMap<>();

    private String reward;

    private List<String> description = new ArrayList<>();

    Level(int level, int costExperience, int costEconomy, int performance, String reward, int fuelDuration, int overheat, int fuelShare, Map<CompatibleMaterial, Integer> materials) {
        this.level = level;
        this.costExperience = costExperience;
        this.costEconomy = costEconomy;
        this.performance = performance;
        this.reward = reward;
        this.fuelDuration = fuelDuration;
        this.overheat = overheat;
        this.fuelShare = fuelShare;
        this.materials = materials;

        EpicFurnaces plugin = EpicFurnaces.getInstance();

        if (performance != 0)
            description.add(plugin.getLocale().getMessage("interface.furnace.performance")
                    .processPlaceholder("amount", performance + "%").getMessage());

        if (reward != null)
            description.add(plugin.getLocale().getMessage("interface.furnace.reward")
                    .processPlaceholder("amount", reward.split("%:")[0] + "%").getMessage());

        if (fuelDuration != 0)
            description.add(plugin.getLocale().getMessage("interface.furnace.fuelduration")
                    .processPlaceholder("amount", fuelDuration + "%").getMessage());

        if (fuelShare != 0)
            description.add(plugin.getLocale().getMessage("interface.furnace.fuelshare")
                    .processPlaceholder("amount", fuelShare).getMessage());

        if (overheat != 0)
            description.add(plugin.getLocale().getMessage("interface.furnace.overheat")
                    .processPlaceholder("amount", overheat).getMessage());
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

    public Map<CompatibleMaterial, Integer> getMaterials() {
        return Collections.unmodifiableMap(materials);
    }
}

package com.songoda.epicfurnaces.furnace.levels;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.locale.Locale;
import com.songoda.epicfurnaces.EpicFurnaces;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class Level {

    private final int level, costExperience, costEconomy, performance, fuelDuration, overheat, fuelShare;
    private final Map<CompatibleMaterial, Integer> materials;
    private final String reward;
    private final List<String> description;

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

        final Locale locale = EpicFurnaces.getInstance().getLocale();
        description = new ArrayList<>();

        if (performance != 0)
            description.add(locale.getMessage("interface.furnace.performance")
                    .processPlaceholder("amount", performance + "%").getMessage());

        if (reward != null)
            description.add(locale.getMessage("interface.furnace.reward")
                    .processPlaceholder("amount", reward.split("%:")[0] + "%").getMessage());

        if (fuelDuration != 0)
            description.add(locale.getMessage("interface.furnace.fuelduration")
                    .processPlaceholder("amount", fuelDuration + "%").getMessage());

        if (fuelShare != 0)
            description.add(locale.getMessage("interface.furnace.fuelshare")
                    .processPlaceholder("amount", fuelShare).getMessage());

        if (overheat != 0)
            description.add(locale.getMessage("interface.furnace.overheat")
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

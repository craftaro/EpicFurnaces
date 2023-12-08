package com.craftaro.epicfurnaces.furnace.levels;

import com.craftaro.core.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.epicfurnaces.EpicFurnaces;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Level {
    private final int level;
    private final int costExperience;
    private final int costEconomy;
    private final int performance;
    private final int fuelDuration;
    private final int overheat;
    private final int fuelShare;

    private Map<XMaterial, Integer> materials;

    private final String reward;

    private final List<String> description = new ArrayList<>();

    Level(int level, int costExperience, int costEconomy, int performance, String reward, int fuelDuration, int overheat, int fuelShare, Map<XMaterial, Integer> materials) {
        this.level = level;
        this.costExperience = costExperience;
        this.costEconomy = costEconomy;
        this.performance = performance;
        this.reward = reward;
        this.fuelDuration = fuelDuration;
        this.overheat = overheat;
        this.fuelShare = fuelShare;
        this.materials = materials;

        EpicFurnaces plugin = EpicFurnaces.getPlugin(EpicFurnaces.class);

        if (performance != 0) {
            this.description.add(plugin.getLocale()
                    .getMessage("interface.furnace.performance")
                    .processPlaceholder("amount", performance + "%")
                    .getMessage());
        }

        if (reward != null) {
            this.description.add(plugin.getLocale()
                    .getMessage("interface.furnace.reward")
                    .processPlaceholder("amount", reward.split("%:")[0] + "%")
                    .getMessage());
        }

        if (fuelDuration != 0) {
            this.description.add(plugin.getLocale()
                    .getMessage("interface.furnace.fuelduration")
                    .processPlaceholder("amount", fuelDuration + "%")
                    .getMessage());
        }

        if (fuelShare != 0) {
            this.description.add(plugin.getLocale()
                    .getMessage("interface.furnace.fuelshare")
                    .processPlaceholder("amount", fuelShare)
                    .getMessage());
        }

        if (overheat != 0) {
            this.description.add(plugin.getLocale()
                    .getMessage("interface.furnace.overheat")
                    .processPlaceholder("amount", overheat)
                    .getMessage());
        }
    }


    public List<String> getDescription() {
        return new ArrayList<>(this.description);
    }


    public int getLevel() {
        return this.level;
    }


    public int getPerformance() {
        return this.performance;
    }


    public String getReward() {
        return this.reward;
    }


    public int getOverheat() {
        return this.overheat;
    }


    public int getFuelShare() {
        return this.fuelShare;
    }


    public int getFuelDuration() {
        return this.fuelDuration;
    }


    public int getCostExperience() {
        return this.costExperience;
    }


    public int getCostEconomy() {
        return this.costEconomy;
    }

    public Map<XMaterial, Integer> getMaterials() {
        return Collections.unmodifiableMap(this.materials);
    }
}

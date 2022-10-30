package com.songoda.epicfurnaces.boost;

import com.songoda.epicfurnaces.EpicFurnaceInstances;
import com.songoda.epicfurnaces.database.DataManager;

import java.util.*;

public final class BoostManager implements EpicFurnaceInstances {

    private final Set<BoostData> registeredBoosts = new HashSet<>();

    public void addBoostToPlayer(BoostData data) {
        this.registeredBoosts.add(data);
    }

    public void addBoosts(List<BoostData> boosts) {
        registeredBoosts.addAll(boosts);
    }

    public void removeBoostFromPlayer(BoostData data) {
        this.registeredBoosts.remove(data);
    }

    public Set<BoostData> getBoosts() {
        return Collections.unmodifiableSet(registeredBoosts);
    }

    public BoostData getBoost(UUID player) {
        if (player == null) return null;
        final DataManager dataManager = getPlugin().getDataManager();
        for (BoostData boostData : registeredBoosts) {
            if (boostData.getPlayer().equals(player) && System.currentTimeMillis() >= boostData.getEndTime()) {
                removeBoostFromPlayer(boostData);
                dataManager.deleteBoost(boostData);
            }
            return boostData;
        }
        return null;
    }

}

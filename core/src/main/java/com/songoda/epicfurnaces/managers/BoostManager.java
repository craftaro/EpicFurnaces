package com.songoda.epicfurnaces.managers;

import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.objects.BoostData;
import com.songoda.epicfurnaces.storage.StorageItem;

import java.util.*;

public class BoostManager {

    private final Set<BoostData> registeredBoosts = new HashSet<>();
    private final EpicFurnaces instance;

    public BoostManager(EpicFurnaces instance) {
        this.instance = instance;
    }

    public void addBoostToPlayer(BoostData data) {
        this.registeredBoosts.add(data);
    }

    public void removeBoostFromPlayer(BoostData data) {
        this.registeredBoosts.remove(data);
    }

    public Set<BoostData> getBoosts() {
        return Collections.unmodifiableSet(registeredBoosts);
    }

    public BoostData getBoost(UUID player) {
        if (player == null) return null;
        for (BoostData boostData : registeredBoosts) {
            if (boostData.getPlayer().toString().equals(player.toString())) {
                if (System.currentTimeMillis() >= boostData.getEndTime()) {
                    removeBoostFromPlayer(boostData);
                }
                return boostData;
            }
        }
        return null;
    }

    public void saveToFile() {
        for (BoostData boostData : getBoosts()) {
            instance.getStorage().prepareSaveItem("boosts", new StorageItem("endtime", String.valueOf(boostData.getEndTime())),
                    new StorageItem("amount", boostData.getMultiplier()),
                    new StorageItem("uuid", boostData.getPlayer().toString()));
        }
    }
}

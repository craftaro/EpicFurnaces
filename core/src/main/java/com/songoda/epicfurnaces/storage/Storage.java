package com.songoda.epicfurnaces.storage;

import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.objects.BoostData;
import com.songoda.epicfurnaces.objects.FurnaceObject;
import com.songoda.epicfurnaces.utils.ConfigWrapper;
import com.songoda.epicfurnaces.utils.Methods;

import java.util.List;

public abstract class Storage {

    protected final EpicFurnaces instance;
    protected final ConfigWrapper dataFile;

    public Storage(EpicFurnaces instance) {
        this.instance = instance;
        this.dataFile = new ConfigWrapper(instance, "", "data.yml");
        this.dataFile.createNewFile(null, "EpicFurnaces Data File");
        this.dataFile.getConfig().options().copyDefaults(true);
        this.dataFile.saveConfig();
    }

    public abstract boolean containsGroup(String group);

    public abstract List<StorageRow> getRowsByGroup(String group);

    public abstract void prepareSaveItem(String group, StorageItem... items);

    public void updateData(EpicFurnaces instance) {
        // Save game data
        for (FurnaceObject furnace : instance.getFurnaceManager().getFurnaces().values()) {
            if (furnace == null || furnace.getLocation() == null || furnace.getLocation().getWorld() == null) {
                continue;
            }
            String locationStr = Methods.serializeLocation(furnace.getLocation());

            instance.getStorage().prepareSaveItem("charged",
                    new StorageItem("location", locationStr),
                    new StorageItem("level", furnace.getLevel().getLevel()),
                    new StorageItem("uses", furnace.getUses()),
                    new StorageItem("tolevel", furnace.getToLevel()),
                    new StorageItem("nickname", furnace.getNickname()),
                    new StorageItem("accesslist", furnace.getOriginalAccessList()),
                    new StorageItem("placedby", furnace.getPlacedBy() == null ? null : furnace.getPlacedBy().toString()));
        }

        for (BoostData boostData : instance.getBoostManager().getBoosts()) {
            instance.getStorage().prepareSaveItem("boosts", new StorageItem("endtime", String.valueOf(boostData.getEndTime())),
                    new StorageItem("amount", boostData.getMultiplier()),
                    new StorageItem("uuid", boostData.getPlayer().toString()));
        }
    }

    public abstract void doSave();

    public abstract void save();

    public abstract void makeBackup();

    public abstract void closeConnection();

}

package com.songoda.epicfurnaces.storage;

import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.boost.BoostData;
import com.songoda.epicfurnaces.furnace.Furnace;
import com.songoda.epicfurnaces.utils.ConfigWrapper;
import com.songoda.epicfurnaces.utils.Methods;

import java.util.List;

public abstract class Storage {

    protected final EpicFurnaces plugin;
    protected final ConfigWrapper dataFile;

    public Storage(EpicFurnaces plugin) {
        this.plugin = plugin;
        this.dataFile = new ConfigWrapper(plugin, "", "data.yml");
        this.dataFile.createNewFile(null, "EpicFurnaces Data File");
        this.dataFile.getConfig().options().copyDefaults(true);
        this.dataFile.saveConfig();
    }

    public abstract boolean containsGroup(String group);

    public abstract List<StorageRow> getRowsByGroup(String group);

    public abstract void prepareSaveItem(String group, StorageItem... items);

    public void updateData(EpicFurnaces plugin) {
        /*
         * Dump FurnaceManager to file.
         */
        for (Furnace furnace : plugin.getFurnaceManager().getFurnaces().values()) {
            if (furnace == null
                    || furnace.getLocation() == null
                    || furnace.getLocation().getWorld() == null
                    || furnace.getLevel() == null) continue;
            String locationStr = Methods.serializeLocation(furnace.getLocation());

            prepareSaveItem("charged", new StorageItem("location", locationStr),
                    new StorageItem("level", furnace.getLevel().getLevel()),
                    new StorageItem("uses", furnace.getUses()),
                    new StorageItem("tolevel", furnace.getTolevel()),
                    new StorageItem("nickname", furnace.getNickname()),
                    new StorageItem("accesslist", furnace.getOriginalAccessList()),
                    new StorageItem("placedby", furnace.getPlacedBy() == null ? null : furnace.getPlacedBy().toString()));
        }

        /*
         * Dump BoostManager to file.
         */
        for (BoostData boostData : plugin.getBoostManager().getBoosts()) {
            prepareSaveItem("boosts", new StorageItem("endtime", String.valueOf(boostData.getEndTime())),
                    new StorageItem("amount", boostData.getMultiplier()),
                    new StorageItem("uuid", boostData.getPlayer().toString()));
        }
    }

    public abstract void doSave();

    public abstract void save();

    public abstract void makeBackup();

    public abstract void closeConnection();

}

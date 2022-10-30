package com.songoda.epicfurnaces.storage;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.configuration.Config;
import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.EpicFurnaceInstances;
import com.songoda.epicfurnaces.boost.BoostData;
import com.songoda.epicfurnaces.furnace.Furnace;
import com.songoda.epicfurnaces.utils.Methods;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class Storage implements EpicFurnaceInstances {

    protected final EpicFurnaces plugin;
    protected final Config dataFile;


    public Storage(EpicFurnaces plugin) {
        this.plugin = plugin;
        this.dataFile = new Config(plugin, "data.yml");
        this.dataFile.load();
    }

    public abstract boolean containsGroup(String group);

    public abstract List<StorageRow> getRowsByGroup(String group);

    public abstract void prepareSaveItem(String group, StorageItem... items);

    public void updateData() {
        /*
         * Dump FurnaceManager to file.
         */
        for (Furnace furnace : FURNACE_MANAGER.getFurnaces().values()) {
            if (furnace == null
                    || furnace.getLocation().getWorld() == null
                    || furnace.getLevel() == null) continue;
            String locationStr = Methods.serializeLocation(furnace.getLocation());

            List<String> toLevel = new ArrayList<>();
            for (Map.Entry<CompatibleMaterial, Integer> entry : furnace.getToLevel().entrySet())
                toLevel.add(entry.getKey().name() + ":" + entry.getValue());

            prepareSaveItem("charged", new StorageItem("location", locationStr),
                    new StorageItem("level", furnace.getLevel().getLevel()),
                    new StorageItem("uses", furnace.getUses()),
                    new StorageItem("tolevelnew", toLevel),
                    new StorageItem("nickname", furnace.getNickname()),
                    new StorageItem("accesslist", furnace.getAccessList().stream().map(UUID::toString).collect(Collectors.toList())),
                    new StorageItem("placedby", furnace.getPlacedBy() == null ? null : furnace.getPlacedBy().toString()));
        }

        /*
         * Dump BoostManager to file.
         */
        for (BoostData boostData : BOOST_MANAGER.getBoosts()) {
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

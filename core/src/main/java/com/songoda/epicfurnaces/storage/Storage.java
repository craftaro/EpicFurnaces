package com.songoda.epicfurnaces.storage;

import com.songoda.epicfurnaces.EpicFurnaces;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public abstract class Storage {

    protected final EpicFurnaces instance;
    protected final FileConfiguration dataFile;

    public Storage(EpicFurnaces instance) {
        this.instance = instance;
        this.dataFile = instance.getConfiguration("data");
    }

    public abstract boolean containsGroup(String group);

    public abstract List<StorageRow> getRowsByGroup(String group);

    public abstract void prepareSaveItem(String group, StorageItem... items);

    public abstract void doSave();

    public abstract void closeConnection();

}

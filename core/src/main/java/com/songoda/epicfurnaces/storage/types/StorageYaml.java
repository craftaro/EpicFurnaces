package com.songoda.epicfurnaces.storage.types;

import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.storage.Storage;
import com.songoda.epicfurnaces.storage.StorageItem;
import com.songoda.epicfurnaces.storage.StorageRow;
import org.apache.commons.io.FileUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class StorageYaml extends Storage {

    private static final Map<String, Object> toSave = new HashMap<>();

    public StorageYaml(EpicFurnaces instance) {
        super(instance);
    }

    @Override
    public boolean containsGroup(String group) {
        return dataFile.contains("data." + group);
    }

    @Override
    public List<StorageRow> getRowsByGroup(String group) {
        List<StorageRow> rows = new ArrayList<>();
        ConfigurationSection currentSection = dataFile.getConfigurationSection("data." + group);
        for (String key : currentSection.getKeys(false)) {

            Map<String, StorageItem> items = new HashMap<>();
            ConfigurationSection currentSection2 = dataFile.getConfigurationSection("data." + group + "." + key);
            for (String key2 : currentSection2.getKeys(false)) {
                String path = "data." + group + "." + key + "." + key2;
                items.put(key2, new StorageItem(dataFile.get(path) instanceof MemorySection
                        ? convertToInLineList(path) : dataFile.get(path)));
            }
            if (items.isEmpty()) continue;
            StorageRow row = new StorageRow(key, items);
            rows.add(row);
        }
        return rows;
    }

    private String convertToInLineList(String path) {
        StringBuilder converted = new StringBuilder();
        for (String key : dataFile.getConfigurationSection(path).getKeys(false)) {
            converted.append(key).append(":").append(dataFile.getInt(path + "." + key)).append(";");
        }
        return converted.toString();
    }

    @Override
    public void prepareSaveItem(String group, StorageItem... items) {
        for (StorageItem item : items) {
            if (item == null || item.asObject() == null) continue;
            toSave.put("data." + group + "." + items[0].asString() + "." + item.getKey(), item.asObject());
        }
    }

    @Override
    public void doSave() {
        try {
            dataFile.set("data", null); // Clear file

            File data = new File(instance.getDataFolder() + "/data.yml");
            File dataClone = new File(instance.getDataFolder() + "/data-backup-" + System.currentTimeMillis() + ".yml");
            try {
                FileUtils.copyFile(data, dataClone);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Deque<File> backups = new ArrayDeque<>();
            for (File file : Objects.requireNonNull(instance.getDataFolder().listFiles())) {
                if (file.getName().toLowerCase().contains("data-backup")) {
                    backups.add(file);
                }
            }
            if (backups.size() > 5) {
                backups.getFirst().delete();
            }

            for (Map.Entry<String, Object> entry : toSave.entrySet()) {
                dataFile.set(entry.getKey(), entry.getValue());
            }

            instance.save("data");
            toSave.clear();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void closeConnection() {
        instance.save("data");
    }
}

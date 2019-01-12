package com.songoda.epicfurnaces.managers;

import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.objects.BoostData;
import com.songoda.epicfurnaces.objects.FurnaceObject;
import com.songoda.epicfurnaces.storage.Storage;
import com.songoda.epicfurnaces.storage.StorageItem;
import com.songoda.epicfurnaces.storage.StorageRow;
import com.songoda.epicfurnaces.utils.Methods;
import com.songoda.epicfurnaces.utils.gui.ItemBuilder;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static org.bukkit.Material.FURNACE;

public class FurnaceManager {

    private final Map<Location, FurnaceObject> registeredFurnaces = new HashMap<>();
    private final EpicFurnaces instance;

    public FurnaceManager(EpicFurnaces instance) {
        this.instance = instance;
    }

    public FurnaceObject addFurnace(Location location, FurnaceObject furnace) {
        instance.getHologramManager().updateHologram(furnace);
        registeredFurnaces.put(roundLocation(location), furnace);
        return furnace;
    }

    public FurnaceObject createFurnace(Location location) {
        return addFurnace(location, new FurnaceObject(instance, location, instance.getLevelManager().getLowestLevel(), null, 0, 0, new ArrayList<>(), null));
    }

    public void removeFurnace(Location location) {
        instance.getHologramManager().remove(registeredFurnaces.remove(location));
    }

    public Optional<FurnaceObject> getFurnace(Location location) {
        return Optional.ofNullable(registeredFurnaces.get(location));
    }

    public int getFurnaceLevel(ItemStack item) {
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return 1;
        }

        if (item.getItemMeta().getDisplayName().contains(":")) {
            String[] arr = (item.getItemMeta().getDisplayName().replace("§", "")).split(":");
            return StringUtils.isNumeric(arr[0]) ? Integer.parseInt(arr[0]) : 1;
        }

        return 1;
    }

    public int getFurnaceUses(ItemStack item) {
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return 0;
        }

        if (item.getItemMeta().getDisplayName().contains(":")) {
            String[] arr = (item.getItemMeta().getDisplayName().replace("§", "")).split(":");
            return StringUtils.isNumeric(arr[1]) ? Integer.parseInt(arr[1]) : 0;
        } else {
            return 0;
        }
    }

    private Location roundLocation(Location location) {
        return location.getBlock().getLocation().clone();
    }

    public ItemStack createLeveledFurnace(int level, int uses, EpicFurnaces epicFurnaces) {
        ItemBuilder itemBuilder = new ItemBuilder(FURNACE);

        if (epicFurnaces.getConfig().getBoolean("Main.Remember Furnace Item Levels")) {
            itemBuilder.name(com.songoda.epicfurnaces.utils.StringUtils.formatText(com.songoda.epicfurnaces.utils.StringUtils.formatName(level, uses, true)));
        }

        return itemBuilder.build();
    }

    public void loadFurnaces() {
        Storage storage = instance.getStorage();
        if (storage.containsGroup("charged")) {
            for (StorageRow row : storage.getRowsByGroup("charged")) {
                Location location = Methods.deserializeLocation(row.getKey());
                if (location == null || location.getBlock() == null) {
                    return;
                }

                int level = row.get("level").asInt();
                int uses = row.get("uses").asInt();
                int toLevel = row.get("level").asInt();
                String nickname = row.get("nickname").asString();
                List<String> accessList = row.get("accesslist").asStringList();
                String placedByStr = row.get("placedBy").asString();
                UUID placedBy = placedByStr == null ? null : UUID.fromString(placedByStr);
                FurnaceObject furnace = new FurnaceObject(instance, location, instance.getLevelManager().getLevel(level), nickname, uses, toLevel, accessList, placedBy);

                addFurnace(location, furnace);
            }
        }

        if (storage.containsGroup("boosts")) {
            for (StorageRow row : storage.getRowsByGroup("boosts")) {
                if (row.getItems().get("uuid").asObject() != null) {
                    continue;
                }
                BoostData boostData = new BoostData(row.get("amount").asInt(), Long.parseLong(row.getKey()), UUID.fromString(row.get("uuid").asString()));
                instance.getBoostManager().addBoostToPlayer(boostData);
            }
        }
        getFurnaces().values().forEach(furnace -> instance.getHologramManager().updateHologram(furnace));
    }

    public void saveToFile() {
        for (FurnaceObject furnace : getFurnaces().values()) {
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
    }

    public Map<Location, FurnaceObject> getFurnaces() {
        return Collections.unmodifiableMap(registeredFurnaces);
    }
}

package com.songoda.epicfurnaces.storage;

import com.songoda.epicfurnaces.utils.Methods;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class StorageItem {
    private final Object object;
    private String key = null;

    public StorageItem(Object object) {
        this.object = object;
    }

    public StorageItem(String key, Object object) {
        this.key = key;
        this.object = object;
    }

    public StorageItem(String key, List<String> string) {
        StringBuilder object = new StringBuilder();
        for (String s : string) {
            object.append(s).append(";");
        }
        this.key = key;
        this.object = object.toString();
    }

    public StorageItem(String key, boolean type, List<Location> blocks) {
        StringBuilder object = new StringBuilder();
        for (Location location : blocks) {
            object.append(Methods.serializeLocation(location));
            object.append(";;");
        }
        this.key = key;
        this.object = object.toString();
    }

    public String getKey() {
        return this.key;
    }

    public String asString() {
        if (this.object == null) {
            return null;
        }
        return (String) this.object;
    }

    public boolean asBoolean() {
        if (this.object == null) {
            return false;
        }
        if (this.object instanceof Integer) {
            return (Integer) this.object == 1;
        }
        return (boolean) this.object;
    }

    public int asInt() {
        if (this.object == null) {
            return 0;
        }
        return (int) this.object;
    }

    public Object asObject() {
        if (this.object == null) {
            return null;
        }
        if (this.object instanceof Boolean) {
            return (Boolean) this.object ? 1 : 0;
        }
        return this.object;
    }

    public List<String> asStringList() {
        if (this.object instanceof ArrayList) {
            return new ArrayList<>();
        }
        List<String> list = new ArrayList<>();
        if (this.object == null) {
            return list;
        }
        String[] stack = ((String) this.object).split(";");
        for (String item : stack) {
            if (item.equals("")) {
                continue;
            }
            list.add(item);
        }
        return list;
    }

}

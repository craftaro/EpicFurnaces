package com.songoda.epicfurnaces.utils;

import com.craftaro.core.utils.TextUtils;
import com.songoda.epicfurnaces.EpicFurnaces;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Methods {
    public static String cleanString(String typ) {
        String type = typ.replaceAll("_", " ");
        type = ChatColor.stripColor(type.substring(0, 1).toUpperCase() + type.toLowerCase().substring(1));
        return type;
    }

    public static String formatName(int level) {
        String name = EpicFurnaces.getPlugin(EpicFurnaces.class)
                .getLocale()
                .getMessage("general.nametag.nameformat")
                .processPlaceholder("level", level)
                .getMessage();

        return TextUtils.formatText(name);
    }

    /**
     * Serializes the location specified.
     *
     * @param location The location that is to be saved.
     * @return The serialized data.
     */
    public static String serializeLocation(Location location) {
        if (location == null) {
            return "";
        }
        String w = location.getWorld().getName();
        double x = location.getBlockX();
        double y = location.getBlockY();
        double z = location.getBlockZ();
        DecimalFormat df = new DecimalFormat("####.####");
        String str = w + ":" + df.format(x) + ":" + df.format(y) + ":" + df.format(z);
        str = str.replace(".0", "").replace("/", "");
        return str;
    }

    private static final Map<String, Location> SERIALIZE_CACHE = new HashMap<>();

    /**
     * Deserializes a location from the string.
     *
     * @param str The string to parse.
     * @return The location that was serialized in the string.
     */
    public static Location unserializeLocation(String str) {
        if (str == null || str.equals("")) {
            return null;
        }
        if (SERIALIZE_CACHE.containsKey(str)) {
            return SERIALIZE_CACHE.get(str).clone();
        }
        String cacheKey = str;
        str = str.replace("y:", ":")
                .replace("z:", ":")
                .replace("w:", "")
                .replace("x:", ":")
                .replace("/", ".");
        List<String> args = Arrays.asList(str.split("\\s*:\\s*"));

        World world = Bukkit.getWorld(args.get(0));
        double x = Double.parseDouble(args.get(1));
        double y = Double.parseDouble(args.get(2));
        double z = Double.parseDouble(args.get(3));

        Location location = new Location(world, x, y, z, 0, 0);
        SERIALIZE_CACHE.put(cacheKey, location.clone());
        return location;
    }
}

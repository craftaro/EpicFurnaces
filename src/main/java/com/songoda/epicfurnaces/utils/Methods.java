package com.songoda.epicfurnaces.utils;

import com.songoda.core.compatibility.ServerVersion;
import com.songoda.epicfurnaces.EpicFurnaces;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by songoda on 2/25/2017.
 */
public class Methods {

    public static String cleanString(String typ) {
        String type = typ.replaceAll("_", " ");
        type = ChatColor.stripColor(type.substring(0, 1).toUpperCase() + type.toLowerCase().substring(1));
        return type;
    }

    public static String formatName(int level, int uses, boolean full) {
        String name = EpicFurnaces.getInstance().getLocale().getMessage("general.nametag.nameformat")
                .processPlaceholder("level", level).getMessage();

        String info = "";
        if (full) {
            info += convertToInvisibleString(level + ":" + uses + ":");
        }

        return info + formatText(name);
    }

    /**
     * Serializes the location of the block specified.
     *
     * @param b The block whose location is to be saved.
     * @return The serialized data.
     */
    public static String serializeLocation(Block b) {
        if (b == null)
            return "";
        return serializeLocation(b.getLocation());
    }

    /**
     * Serializes the location specified.
     *
     * @param location The location that is to be saved.
     * @return The serialized data.
     */
    public static String serializeLocation(Location location) {
        if (location == null)
            return "";
        String w = location.getWorld().getName();
        double x = location.getBlockX();
        double y = location.getBlockY();
        double z = location.getBlockZ();
        DecimalFormat df = new DecimalFormat("####.####");
        String str = w + ":" + df.format(x) + ":" + df.format(y) + ":" + df.format(z);
        str = str.replace(".0", "").replace("/", "");
        return str;
    }
    private static Map<String, Location> serializeCache = new HashMap<>();

    /**
     * Deserializes a location from the string.
     *
     * @param str The string to parse.
     * @return The location that was serialized in the string.
     */
    public static Location unserializeLocation(String str) {
        if (str == null || str.equals(""))
            return null;
        if (serializeCache.containsKey(str)) {
            return serializeCache.get(str).clone();
        }
        String cacheKey = str;
        str = str.replace("y:", ":").replace("z:", ":").replace("w:", "").replace("x:", ":").replace("/", ".");
        List<String> args = Arrays.asList(str.split("\\s*:\\s*"));

        World world = Bukkit.getWorld(args.get(0));
        double x = Double.parseDouble(args.get(1)), y = Double.parseDouble(args.get(2)), z = Double.parseDouble(args.get(3));
        Location location = new Location(world, x, y, z, 0, 0);
        serializeCache.put(cacheKey, location.clone());
        return location;
    }


    public static String convertToInvisibleString(String s) {
        if (s == null || s.equals(""))
            return "";
        StringBuilder hidden = new StringBuilder();
        for (char c : s.toCharArray()) hidden.append(ChatColor.COLOR_CHAR + "").append(c);
        return hidden.toString();
    }

    public static String formatText(String text) {
        if (text == null || text.equals(""))
            return "";
        return formatText(text, false);
    }

    public static String formatText(String text, boolean cap) {
        if (text == null || text.equals(""))
            return "";
        if (cap)
            text = text.substring(0, 1).toUpperCase() + text.substring(1);
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static boolean isInt(String number) {
        if (number == null || number.equals(""))
            return false;
        try {
            Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static String makeReadable(Long time) {
        if (time == null)
            return "";

        StringBuilder sb = new StringBuilder();

        long days = TimeUnit.MILLISECONDS.toDays(time);
        long hours = TimeUnit.MILLISECONDS.toHours(time) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(time));
        long minutes = TimeUnit.MILLISECONDS.toMinutes(time) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time));

        if (days != 0L)
            sb.append(" ").append(days).append("d");
        if (hours != 0L)
            sb.append(" ").append(hours).append("h");
        if (minutes != 0L)
            sb.append(" ").append(minutes).append("m");
        if (seconds != 0L)
            sb.append(" ").append(seconds).append("s");
        return sb.toString().trim();
    }


    public static long parseTime(String input) {
        long result = 0;
        StringBuilder number = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isDigit(c)) {
                number.append(c);
            } else if (Character.isLetter(c) && (number.length() > 0)) {
                result += convert(Integer.parseInt(number.toString()), c);
                number = new StringBuilder();
            }
        }
        return result;
    }

    private static long convert(long value, char unit) {
        switch (unit) {
            case 'd':
                return value * 1000 * 60 * 60 * 24;
            case 'h':
                return value * 1000 * 60 * 60;
            case 'm':
                return value * 1000 * 60;
            case 's':
                return value * 1000;
        }
        return 0;
    }

    public static String formatEconomy(double amt) {
        DecimalFormat formatter = new DecimalFormat("#,###.00");
        return formatter.format(amt);
    }

    public static String formatTitle(String text) {
        if (text == null || text.equals(""))
            return "";
        if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_9)) {
            if (text.length() > 31)
                text = text.substring(0, 29) + "...";
        }
        text = formatText(text);
        return text;
    }
}

package com.songoda.epicfurnaces.utils;

import com.songoda.epicfurnaces.EpicFurnaces;
import org.bukkit.ChatColor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class StringUtils {

    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();
    private static EpicFurnaces instance;

    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "B");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    public static void init(EpicFurnaces main) {
        instance = main;
    }

    public static String msToString(Long milli) {
        milli -= 24 * 60 * 60 * 1000;
        Date date = new Date(milli);
        boolean days = MILLISECONDS.toDays(milli) != 0;
        boolean hours = MILLISECONDS.toHours(milli) != 0;
        boolean minutes = MILLISECONDS.toMinutes(milli) != 0;

        DateFormat formatter = new SimpleDateFormat((days ? "D 'Days' " : "") + (hours ? "HH 'hours' " : "") + (minutes ? "mm 'minutes'" : "") + " ss's' ");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(date);
    }

    public static String cleanString(String string) {
        string = string.replaceAll("_", " ");

        if (string.split(" ").length == 2) {
            string = ChatColor.stripColor(string.substring(0, 1).toUpperCase() + string.toLowerCase().substring(1));
        }

        return string;
    }

    public static String formatName(int level, int uses, boolean full) {
        String name = instance.getLocale().getMessage("general.nametag.nameformat", level);
        String info = "";

        if (full) {
            info += encode(level + ":" + uses + ":");
        }

        return info + formatText(name);
    }

    public static String formatText(String string) {
        return string != null && !string.equals("") ? formatText(string, false) : "";
    }

    public static String formatText(String string, boolean cap) {
        if (string != null && !string.equals("")) {
            if (cap) {
                string = string.substring(0, 1).toUpperCase() + string.substring(1);
            }

            return ChatColor.translateAlternateColorCodes('&', string);
        } else {
            return "";
        }
    }

    private static String encode(String toEncode) {
        StringBuilder builder = new StringBuilder();

        for (char c : toEncode.toCharArray()) {
            builder.append(ChatColor.COLOR_CHAR).append(c);
        }

        return builder.toString();
    }

    private static int decode(String toDecode) {
        String decoded = toDecode.replaceAll(String.valueOf(ChatColor.COLOR_CHAR), "");
        return Integer.parseInt(decoded);
    }

    public static String formatEconomy(long value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return formatEconomy(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + formatEconomy(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

}

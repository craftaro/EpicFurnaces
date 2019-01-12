package com.songoda.epicfurnaces.utils;

import com.songoda.epicfurnaces.EpicFurnaces;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Created by songoda on 2/25/2017.
 */
public class Methods {

    private static EpicFurnaces instance;

    public static void init(EpicFurnaces main) {
        instance = main;
    }

    public static ItemStack getGlass(Boolean rainbow, int type) {
        int randomNum = 1 + (int) (Math.random() * 6.0D);
        ItemStack glass;

        if (rainbow) {
            glass = new ItemStack(instance.getBukkitEnums().getMaterial("WHITE_STAINED_GLASS_PANE").getType(), 1, (short) randomNum);
        } else {
            glass = new ItemStack(instance.getBukkitEnums().getMaterial("WHITE_STAINED_GLASS_PANE").getType(), 1, (short) type);
        }

        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName("§l");
        glass.setItemMeta(glassMeta);
        return glass;
    }

    public static ItemStack getBackgroundGlass(boolean type) {
        if (type) {
            return getGlass(false, instance.getConfig().getInt("Interfaces.Glass Type 2"));
        }

        return getGlass(false, instance.getConfig().getInt("Interfaces.Glass Type 3"));
    }

    public static String serializeLocation(Location location) {
        if (location == null) {
            return "";
        }
        return "w:" + location.getWorld().getName() + "x:" + location.getBlockX() + "y:" + location.getBlockY() + "z:" + location.getBlockZ();
    }

    public static Location deserializeLocation(String string) {
        if (string == null || string.isEmpty()) {
            return null;
        }
        string = string.replace("y:", ":").replace("z:", ":").replace("w:", "").replace("x:", ":").replace("/", ".");
        String[] args = string.split(":");
        World world = Bukkit.getWorld(args[0]);
        int x = Integer.parseInt(args[1]);
        int y = Integer.parseInt(args[2]);
        int z = Integer.parseInt(args[3]);
        return new Location(world, x, y, z, 0.0F, 0.0F);
    }

    public static ItemStack getGlass() {
        return getGlass(instance.getConfig().getBoolean("Interfaces.Replace Glass Type 1 With Rainbow Glass"), instance.getConfig().getInt("Interfaces.Glass Type 1"));
    }
}

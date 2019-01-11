package com.songoda.epicfurnaces.utils;

import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicfurnaces.EpicFurnaces;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Created by songoda on 2/25/2017.
 */
public class Methods {

    public static ItemStack getGlass() {
        try {
            EpicFurnaces plugin = EpicFurnaces.getInstance();
            return Arconix.pl().getApi().getGUI().getGlass(plugin.getConfig().getBoolean("Interfaces.Replace Glass Type 1 With Rainbow Glass"), plugin.getConfig().getInt("Interfaces.Glass Type 1"));
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return null;
    }

    public static void particles(Block b, Player p) {
        try {
            EpicFurnaces plugin = EpicFurnaces.getInstance();
            if (plugin.getConfig().getBoolean("settings.On-upgrade-particles")) {
                Location location = b.getLocation();
                location.setX(location.getX() + .5);
                location.setY(location.getY() + .5);
                location.setZ(location.getZ() + .5);
                //TODO: particles
                //p.getWorld().spawnParticle(org.bukkit.Particle.valueOf(plugin.getConfig().getString("Main.Upgrade Particle Type")), location, 200, .5, .5, .5);
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public static ItemStack getBackgroundGlass(boolean type) {
        try {
            EpicFurnaces plugin = EpicFurnaces.getInstance();
            if (type)
                return Arconix.pl().getApi().getGUI().getGlass(false, plugin.getConfig().getInt("Interfaces.Glass Type 2"));
            else
                return Arconix.pl().getApi().getGUI().getGlass(false, plugin.getConfig().getInt("Interfaces.Glass Type 3"));
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return null;
    }

    public static String cleanString(String typ) {
        try {
            String type = typ.replaceAll("_", " ");
            type = ChatColor.stripColor(type.substring(0, 1).toUpperCase() + type.toLowerCase().substring(1));
            return type;
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return null;
    }

    public static String formatName(int level, int uses, boolean full) {
        try {
            String name = EpicFurnaces.getInstance().getLocale().getMessage("general.nametag.nameformat", level);

            String info = "";
            if (full) {
                info += Arconix.pl().getApi().format().convertToInvisibleString(level + ":" + uses + ":");
            }

            return info + Arconix.pl().getApi().format().formatText(name);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return null;
    }
}

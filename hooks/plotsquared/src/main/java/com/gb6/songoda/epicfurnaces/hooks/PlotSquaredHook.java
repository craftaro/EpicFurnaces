package com.gb6.songoda.epicfurnaces.hooks;

import com.intellectualcrafters.plot.api.PlotAPI;
import com.plotsquared.bukkit.BukkitMain;
import com.songoda.epicfurnaces.hook.ProtectionPluginHook;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PlotSquaredHook implements ProtectionPluginHook {

    private final PlotAPI plotSquared;

    public PlotSquaredHook() {
        this.plotSquared = new PlotAPI();
    }

    public JavaPlugin getPlugin() { // BukkitMain? Really?
        return JavaPlugin.getPlugin(BukkitMain.class);
    }

    public boolean canBuild(Player player, Location location) {
        return plotSquared.getPlot(location) != null && plotSquared.isInPlot(player)
                && plotSquared.getPlot(location) == plotSquared.getPlot(player);
    }

}

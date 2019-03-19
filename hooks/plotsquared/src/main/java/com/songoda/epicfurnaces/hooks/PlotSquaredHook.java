package com.songoda.epicfurnaces.hooks;

import com.github.intellectualsites.plotsquared.api.PlotAPI;
import com.github.intellectualsites.plotsquared.bukkit.BukkitMain;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PlotSquaredHook implements ProtectionPluginHook {

    private final PlotAPI plotAPI;

    public PlotSquaredHook() {
        this.plotAPI = new PlotAPI();
    }

    public boolean canBuild(Player player, Location location) {
        PlotPlayer plotPlayer = PlotPlayer.wrap(player);
        Plot plot = plotPlayer.getCurrentPlot();
        return plot != null && plot.hasOwner() && plot.isAdded(player.getUniqueId());
    }

    public JavaPlugin getPlugin() { // BukkitMain? Really?
        return JavaPlugin.getPlugin(BukkitMain.class);
    }

}

package com.songoda.epicfurnaces.hooks;


import org.bukkit.Location;

public interface CraftBukkitHook {
    void broadcastParticle(Location location, String name, int amount, String... optional);
}

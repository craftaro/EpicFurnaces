package com.songoda.epicfurnaces.hook;


import org.bukkit.Location;

public interface CraftBukkitHook {
    void broadcastParticle(Location location, String name, int amount, String... optional);
}

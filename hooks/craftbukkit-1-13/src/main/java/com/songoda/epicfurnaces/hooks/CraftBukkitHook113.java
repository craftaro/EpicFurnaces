package com.songoda.epicfurnaces.hooks;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;

public class CraftBukkitHook113 implements CraftBukkitHook {
    @Override
    public void broadcastParticle(Location location, String name, int amount, String... optional) {
        if (optional.length != 0) {
            try {
                ((CraftWorld) location.getWorld()).spawnParticle(Particle.valueOf(optional[0]), location.getX(), location.getY(), location.getZ(), amount, 0, 0, 0, 0);
                return;
            } catch (Exception ignore) {
            }
        }

        try {
            location.getWorld().playEffect(location, Effect.valueOf(name), amount);
            return;
        } catch (Exception ignore) {
        }

        try {
            ((CraftWorld) location.getWorld()).spawnParticle(Particle.valueOf(name), location.getX(), location.getY(), location.getZ(), amount, 0, 0, 0, 0);
        } catch (Exception ignore) {
        }

    }
}

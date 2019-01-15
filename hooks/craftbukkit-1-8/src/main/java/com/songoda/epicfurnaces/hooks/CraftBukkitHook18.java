package com.songoda.epicfurnaces.hooks;

import com.songoda.epicfurnaces.hook.CraftBukkitHook;
import org.bukkit.Effect;
import org.bukkit.Location;

public class CraftBukkitHook18 implements CraftBukkitHook {
    @Override
    public void broadcastParticle(Location location, String name, int amount, String... optional) {
        if (Effect.getByName(name) == null) {
            return;
        }

        String[] split = name.split("_");
        String type;

        if (split.length != 1 && Effect.getByName(name.split("_")[0]) != null) {
            type = name.split("_")[0];
        } else {
            type = name;
        }

        location.getWorld().playEffect(location, Effect.getByName(type), amount);
    }
}

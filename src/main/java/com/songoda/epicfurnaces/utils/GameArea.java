package com.songoda.epicfurnaces.utils;

import com.songoda.epicfurnaces.settings.Settings;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;

public class GameArea {

    private static final int size = Math.max(16, Settings.FURNACE_AREA.getInt());

    private final String world;
    private final int x;
    private final int y;

    private GameArea(World world, int x, int y) {
        this.world = world.getName();
        this.x = x;
        this.y = y;
    }

    public static GameArea of(Location location) {
        return new GameArea(location.getWorld(), location.getBlockX() / size, location.getBlockZ() / size);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameArea gameArea = (GameArea) o;
        return x == gameArea.x &&
                y == gameArea.y &&
                world.equals(gameArea.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, y);
    }
}

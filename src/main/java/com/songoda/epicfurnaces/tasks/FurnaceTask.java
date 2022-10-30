package com.songoda.epicfurnaces.tasks;

import com.songoda.core.compatibility.CompatibleParticleHandler;
import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.EpicFurnaceInstances;
import com.songoda.epicfurnaces.furnace.Furnace;
import com.songoda.epicfurnaces.furnace.levels.Level;
import com.songoda.epicfurnaces.settings.Settings;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class FurnaceTask extends BukkitRunnable implements EpicFurnaceInstances {

    private static FurnaceTask instance;

    final HashSet<Location> toRemove = new HashSet<>();
    boolean doParticles;

    public static FurnaceTask startTask(EpicFurnaces plugin) {
        if (instance == null) {
            instance = new FurnaceTask();
            instance.runTaskTimer(plugin, 0, Settings.TICK_SPEED.getInt());
        }
        return instance;
    }

    @Override
    public void run() {
        doParticles = Settings.OVERHEAT_PARTICLES.getBoolean();
        FURNACE_MANAGER.getFurnaces().values().stream()
                .filter(Furnace::isInLoadedChunk)
                .forEach(furnace -> {
                    final Location location = furnace.getLocation();
                    final BlockState state = location.getBlock().getState();

                    if (!(state instanceof org.bukkit.block.Furnace)) {
                        toRemove.add(location);
                    } else if (((org.bukkit.block.Furnace) state).getBurnTime() != 0) {
                        final Level level = furnace.getLevel();
                        if (level.getOverheat() != 0) {
                            overheat(furnace);
                        }
                        if (level.getFuelShare() != 0) {
                            fuelshare(furnace);
                        }
                    }
                });
        if (!toRemove.isEmpty()) {
            toRemove.forEach(FURNACE_MANAGER::removeFurnace);
            toRemove.clear();
        }
    }

    private void overheat(Furnace furnace) {
        if (furnace.getRadius(true) == null || furnace.getRadiusLast(true) != furnace.getLevel().getOverheat()) {
            furnace.setRadiusLast(furnace.getLevel().getOverheat(), true);
            cache(furnace, true);
        }

        final List<Location> radius = furnace.getRadius(true);
        if(radius == null) {
            return;
        }
        for (Location location : radius) {
            final int random = ThreadLocalRandom.current().nextInt(0, 10);
            if (random != 1) continue;

            final Block block = location.getBlock();
            final Material material = block.getType();
            if (material == Material.AIR || block.getRelative(BlockFace.UP).getType() != Material.AIR) continue;

            if (material == Material.SNOW)
                block.setType(Material.AIR);
            else if (material == Material.ICE || material == Material.PACKED_ICE)
                block.setType(Material.WATER);
            else
                continue;

            if (doParticles) {
                final float xx = (float) (0 + (Math.random() * .75));
                final float yy = (float) (0 + (Math.random() * 1));
                final float zz = (float) (0 + (Math.random() * .75));
                CompatibleParticleHandler.spawnParticles(CompatibleParticleHandler.ParticleType.SMOKE_NORMAL, location, 25, xx, yy, zz, 0);
            }
        }
    }

    private void fuelshare(Furnace furnace) {
        if (furnace.getRadius(false) == null || furnace.getRadiusLast(false) != furnace.getLevel().getOverheat()) {
            furnace.setRadiusLast(furnace.getLevel().getOverheat(), false);
            cache(furnace, false);
        }

        final List<Location> radius = furnace.getRadius(false);
        if(radius == null) {
            return;
        }
        for (Location location : radius) {
            final int random = ThreadLocalRandom.current().nextInt(0, 10);
            if (random != 1) continue;

            final Block block = location.getBlock();

            if (!block.getType().name().contains("FURNACE") && !block.getType().name().contains("SMOKER")) continue;
            final Furnace furnace1 = FURNACE_MANAGER.getFurnace(block);
            if (furnace == furnace1) continue;
            final org.bukkit.block.Furnace furnaceBlock = ((org.bukkit.block.Furnace) block.getState());
            if (furnaceBlock.getBurnTime() == 0) {
                furnaceBlock.setBurnTime((short) 100);
                furnaceBlock.update();

                if (doParticles) {
                    final float xx = (float) (0 + (Math.random() * .75));
                    final float yy = (float) (0 + (Math.random() * 1));
                    final float zz = (float) (0 + (Math.random() * .75));
                    CompatibleParticleHandler.spawnParticles(CompatibleParticleHandler.ParticleType.SMOKE_NORMAL, location, 25, xx, yy, zz, 0);
                }
            }
        }
    }

    private void cache(Furnace furnace, boolean overheat) {
        final Block block = furnace.getLocation().getBlock();
        final World blockWorld = block.getWorld();
        final int radius = 3 * (overheat ? furnace.getLevel().getOverheat() : furnace.getLevel().getFuelShare());
        final int rSquared = radius * radius;
        final int bx = block.getX();
        final int by = block.getY();
        final int bz = block.getZ();

        for (int fx = -radius; fx <= radius; fx++) {
            final int fxSquared = fx * fx;
            for (int fy = -2; fy <= 1; fy++) {
                for (int fz = -radius; fz <= radius; fz++) {
                    if (fxSquared + (fz * fz) <= rSquared) {
                        final Location location = new Location(blockWorld, bx + fx, by + fy, bz + fz);
                        furnace.addToRadius(location, overheat);
                    }
                }
            }
        }
    }
}
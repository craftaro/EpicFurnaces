package com.songoda.epicfurnaces.tasks;

import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.objects.FurnaceObject;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Furnace;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class FurnaceTask extends BukkitRunnable {

    private static FurnaceTask plugin;
    private final EpicFurnaces instance;

    private FurnaceTask(EpicFurnaces plugin) {
        this.instance = plugin;
    }

    public static void startTask(EpicFurnaces plugin) {
        if (FurnaceTask.plugin == null) {
            FurnaceTask.plugin = new FurnaceTask(plugin);
            FurnaceTask.plugin.runTaskTimer(plugin, 0, plugin.getConfig().getInt("Main.Furnace Tick Speed"));
        }
    }

    @Override
    public void run() {
        for (FurnaceObject furnace : instance.getFurnaceManager().getFurnaces().values()) {
            Location furnaceLocation = furnace.getLocation();

            if (furnaceLocation == null) {
                continue;
            }

            if (furnaceLocation.getWorld() == null) {
                instance.getFurnaceManager().removeFurnace(furnaceLocation);
                continue;
            }

            int x = furnaceLocation.getBlockX() >> 4;
            int z = furnaceLocation.getBlockZ() >> 4;

            if (!furnaceLocation.getWorld().isChunkLoaded(x, z)) {
                continue;
            }

            if (furnace.getLocation().getBlock().getType() != Material.FURNACE &&
                    furnace.getLocation().getBlock().getType() != instance.getBukkitEnums().getMaterial("BURNING_FURNACE").getType()) {
                continue;
            }

            if (((Furnace) furnaceLocation.getBlock().getState()).getBurnTime() == 0) {
                continue;
            }

            if (furnace.getLevel().getOverheat() != 0) {
                overheat(furnace);
            }

            if (furnace.getLevel().getFuelShare() != 0) {
                fuelShare(furnace);
            }
        }
    }

    private void overheat(FurnaceObject furnace) {
        if (furnace.getRadius(true) == null || furnace.getRadiusLast(true) != furnace.getLevel().getOverheat()) {
            furnace.setRadiusLast(furnace.getLevel().getOverheat(), true);
            cache(furnace, true);
        }

        for (Location location : furnace.getRadius(true)) {
            int random = ThreadLocalRandom.current().nextInt(0, 10);

            if (random != 1) {
                continue;
            }

            Block block = location.getBlock();

            if (block.getType() == Material.AIR || block.getRelative(BlockFace.UP).getType() != Material.AIR) {
                continue;
            }

            broadcastParticles(location);

            if (block.getType() == Material.SNOW) {
                block.setType(Material.AIR);
                continue;
            }

            if (block.getType() == Material.ICE || block.getType() == Material.PACKED_ICE) {
                block.setType(Material.WATER);
            }
        }
    }

    private void fuelShare(FurnaceObject furnace) {
        if (furnace.getRadius(false) == null || furnace.getRadiusLast(false) != furnace.getLevel().getOverheat()) {
            furnace.setRadiusLast(furnace.getLevel().getOverheat(), false);
            cache(furnace, false);
        }

        for (Location location : furnace.getRadius(false)) {
            int random = ThreadLocalRandom.current().nextInt(0, 10);

            if (random != 1) {
                continue;
            }

            Block block = location.getBlock();

            if (block.getType() != Material.FURNACE && block.getType() != instance.getBukkitEnums().getMaterial("BURNING_FURNACE").getType()) {
                continue;
            }

            Optional<FurnaceObject> other = instance.getFurnaceManager().getFurnace(block.getLocation());

            if (other.isPresent() && furnace.equals(other.get())) {
                Furnace furnaceBlock = ((Furnace) block.getState());

                if (furnaceBlock.getBurnTime() == 0) {
                    furnaceBlock.setBurnTime((short) 100);
                    furnaceBlock.update();
                    broadcastParticles(location);
                }
            }

        }
    }

    private void cache(FurnaceObject furnace, boolean overheat) {
        Block block = furnace.getLocation().getBlock();
        int radius = 3 * (overheat ? furnace.getLevel().getOverheat() : furnace.getLevel().getFuelShare());
        int rSquared = radius * radius;
        int bx = block.getX();
        int by = block.getY();
        int bz = block.getZ();

        for (int fx = -radius; fx <= radius; fx++) {
            for (int fy = -2; fy <= 1; fy++) {
                for (int fz = -radius; fz <= radius; fz++) {
                    if ((fx * fx) + (fz * fz) <= rSquared) {
                        Location location = new Location(block.getWorld(), bx + fx, by + fy, bz + fz);
                        furnace.addToRadius(location, overheat);
                    }
                }
            }
        }
    }

    private void broadcastParticles(Location location) {
        float px = (float) (0 + (Math.random() * 1));
        float pz = (float) (0 + (Math.random() * 1));

        if (instance.getConfig().getBoolean("Main.Overheat Particles")) {
            location.getWorld().playEffect(location, instance.getBukkitEnums().getParticle("SMOKE_NORMAL"), 25);
        }
    }
}
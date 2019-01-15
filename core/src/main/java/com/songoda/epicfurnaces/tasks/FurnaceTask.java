package com.songoda.epicfurnaces.tasks;

import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.objects.FurnaceObject;
import com.songoda.epicfurnaces.objects.FurnaceObject.BoostType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Furnace;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

import static com.songoda.epicfurnaces.objects.FurnaceObject.BoostType.*;

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
        if (furnace.getRadius(OVERHEAT) == null || furnace.getRadiusLast(OVERHEAT) != furnace.getLevel().getOverheat()) {
            furnace.setRadiusLast(furnace.getLevel().getOverheat(), OVERHEAT);
            cache(furnace, OVERHEAT);
        }

        for (Location location : furnace.getRadius(OVERHEAT)) {
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
        if (furnace.getRadius(FUEL_SHARE) == null || furnace.getRadiusLast(FUEL_SHARE) != furnace.getLevel().getFuelShare()) {
            furnace.setRadiusLast(furnace.getLevel().getFuelShare(), FUEL_SHARE);
            cache(furnace, FUEL_SHARE);
        }

        for (Location location : furnace.getRadius(FUEL_SHARE)) {
            int random = ThreadLocalRandom.current().nextInt(0, 10);

            if (random != 1) {
                continue;
            }

            Block block = location.getBlock();

            if (block.getType() != Material.FURNACE && block.getType() != instance.getBukkitEnums().getMaterial("BURNING_FURNACE").getType()) {
                continue;
            }

            FurnaceObject other = instance.getFurnaceManager().getFurnace(block.getLocation()).orElseGet(() -> instance.getFurnaceManager().createFurnace(block.getLocation()));

            if (furnace.equals(other)) {
                continue;
            }

            Furnace furnaceBlock = ((Furnace) block.getState());

            if (furnaceBlock.getBurnTime() == 0) {
                furnaceBlock.setBurnTime((short) 200);
                furnaceBlock.update();
                broadcastParticles(location);
            }
        }
    }

    private void cache(FurnaceObject furnace, BoostType boostType) {
        Block block = furnace.getLocation().getBlock();
        int radius = 3 * (boostType == OVERHEAT ? furnace.getLevel().getOverheat() : furnace.getLevel().getFuelShare());
        int rSquared = radius * radius;
        int bx = block.getX();
        int by = block.getY();
        int bz = block.getZ();

        for (int fx = -radius; fx <= radius; fx++) {
            for (int fy = -2; fy <= 1; fy++) {
                for (int fz = -radius; fz <= radius; fz++) {
                    if ((fx * fx) + (fz * fz) <= rSquared) {
                        Location location = new Location(block.getWorld(), bx + fx, by + fy, bz + fz);
                        furnace.addToRadius(location, boostType);
                    }
                }
            }
        }
    }

    private void broadcastParticles(Location location) {
        if (instance.getConfig().getBoolean("Main.Overheat Particles")) {
            instance.getCraftBukkitHook().broadcastParticle(location, "SMOKE", 25, "SMOKE_NORMAL");
        }
    }
}
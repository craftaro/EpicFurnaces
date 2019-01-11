package com.songoda.epicfurnaces;

import com.songoda.epicfurnaces.hook.CraftBukkitHook;
import net.minecraft.server.v1_9_R2.TileEntityFurnace;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;

public class CraftBukkit19 implements CraftBukkitHook {
    public void setName(Block block, String string) {
        CraftWorld craftWorld = (CraftWorld) block.getWorld();
        TileEntityFurnace furnace = (TileEntityFurnace) craftWorld.getTileEntityAt(block.getX(), block.getY(), block.getZ());
        furnace.a(string);
    }

}

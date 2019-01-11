package com.songoda.epicfurnaces.hooks;

import com.songoda.epicfurnaces.hook.CraftBukkitHook;
import net.minecraft.server.v1_8_R1.TileEntityFurnace;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;

public class CraftBukkit18R1 implements CraftBukkitHook {
    @Override
    public void setName(Block block, String string) {
        CraftWorld craftWorld = (CraftWorld) block.getWorld();
        TileEntityFurnace furnace = (TileEntityFurnace) craftWorld.getTileEntityAt(block.getX(), block.getY(), block.getZ());
        furnace.a(string);
    }

}

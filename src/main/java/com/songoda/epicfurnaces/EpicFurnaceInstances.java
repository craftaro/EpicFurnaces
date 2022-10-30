package com.songoda.epicfurnaces;

import com.songoda.epicfurnaces.boost.BoostManager;
import com.songoda.epicfurnaces.furnace.FurnaceManager;
import com.songoda.epicfurnaces.furnace.levels.LevelManager;

public interface EpicFurnaceInstances {
    FurnaceManager FURNACE_MANAGER = new FurnaceManager();
    BoostManager BOOST_MANAGER = new BoostManager();
    LevelManager LEVEL_MANAGER = new LevelManager();

    default EpicFurnaces getPlugin() {
        return EpicFurnaces.getInstance();
    }
}

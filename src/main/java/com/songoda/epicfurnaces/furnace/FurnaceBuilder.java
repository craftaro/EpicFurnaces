package com.songoda.epicfurnaces.furnace;

import com.songoda.epicfurnaces.furnace.levels.Level;
import org.bukkit.Location;

import java.util.List;
import java.util.UUID;

public class FurnaceBuilder {

    //Level level, String nickname, int uses, int tolevel, List<String> accessList, UUID placedBy

    private final Furnace furnace;

    public FurnaceBuilder(Location location) {
        this.furnace = new Furnace(location);
    }

    public FurnaceBuilder setLevel(Level level) {
        this.furnace.setLevel(level);
        return this;
    }

    public FurnaceBuilder setNickname(String nickname) {
        this.furnace.setNickname(nickname);
        return this;
    }

    public FurnaceBuilder setUses(int uses) {
        this.furnace.setUses(uses);
        return this;
    }

    public FurnaceBuilder setToLevel(int toLevel) {
        this.furnace.setTolevel(toLevel);
        return this;
    }

    public FurnaceBuilder setAccessList(List<UUID> accessList) {
        for (UUID uuid : accessList)
            this.furnace.addToAccessList(uuid);
        return this;
    }

    public FurnaceBuilder setPlacedBy(UUID uuid) {
        this.furnace.setPlacedBy(uuid);
        return this;
    }

    public Furnace build() {
        return this.furnace;
    }

}

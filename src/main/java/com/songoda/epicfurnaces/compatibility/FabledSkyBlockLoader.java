package com.songoda.epicfurnaces.compatibility;

import com.songoda.skyblock.SkyBlock;
import com.songoda.skyblock.permission.BasicPermission;

import java.lang.reflect.InvocationTargetException;

public class FabledSkyBlockLoader {
    
    public FabledSkyBlockLoader() {
        SkyBlock.getInstance().getPermissionManager().registerPermission(new EpicFurnacesPermission());
        try {
            SkyBlock.getInstance().getPermissionManager().registerPermission(
                    (BasicPermission) Class.forName("com.songoda.epicfurnaces.compatibility.EpicFurnacesPermission").getDeclaredConstructor().newInstance());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}

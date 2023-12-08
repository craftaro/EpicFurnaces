package com.craftaro.epicfurnaces.compatibility;


import com.craftaro.skyblock.SkyBlock;
import com.craftaro.skyblock.permission.BasicPermission;

public class FabledSkyBlockLoader {
    public FabledSkyBlockLoader() {
        SkyBlock.getInstance().getPermissionManager().registerPermission(new EpicFurnacesPermission());

        try {
            SkyBlock.getInstance().getPermissionManager().registerPermission((BasicPermission) Class.forName("com.craftaro.epicfurnaces.compatibility.EpicFurnacesPermission").getDeclaredConstructor().newInstance());
        } catch (ReflectiveOperationException ex) {
            ex.printStackTrace();
        }
    }
}

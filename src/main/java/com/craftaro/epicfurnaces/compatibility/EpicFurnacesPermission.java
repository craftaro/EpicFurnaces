package com.craftaro.epicfurnaces.compatibility;

import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.skyblock.permission.BasicPermission;
import com.craftaro.skyblock.permission.PermissionType;

public class EpicFurnacesPermission extends BasicPermission {
    public EpicFurnacesPermission() {
        super("EpicFurnaces", XMaterial.FIRE_CHARGE, PermissionType.GENERIC);
    }
}

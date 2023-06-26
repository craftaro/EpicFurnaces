package com.songoda.epicfurnaces.compatibility;

import com.songoda.skyblock.core.compatibility.CompatibleMaterial;
import com.songoda.skyblock.permission.BasicPermission;
import com.songoda.skyblock.permission.PermissionType;

public class EpicFurnacesPermission extends BasicPermission {
    public EpicFurnacesPermission() {
        super("EpicFurnaces", CompatibleMaterial.FIRE_CHARGE, PermissionType.GENERIC);
    }
}

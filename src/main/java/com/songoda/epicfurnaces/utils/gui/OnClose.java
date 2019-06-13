package com.songoda.epicfurnaces.utils.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public interface OnClose {

    void onClose(Player player, Inventory inventory);

}

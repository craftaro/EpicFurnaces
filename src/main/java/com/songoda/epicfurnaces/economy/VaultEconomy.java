package com.songoda.epicfurnaces.economy;

import com.songoda.epicfurnaces.EpicFurnaces;
import org.bukkit.OfflinePlayer;

public class VaultEconomy implements Economy {

    private final EpicFurnaces plugin;

    private final net.milkbowl.vault.economy.Economy vault;

    public VaultEconomy(EpicFurnaces plugin) {
        this.plugin = plugin;

        this.vault = plugin.getServer().getServicesManager().
                getRegistration(net.milkbowl.vault.economy.Economy.class).getProvider();
    }

    @Override
    public boolean hasBalance(OfflinePlayer player, double cost) {
        return vault.has(player, cost);
    }

    @Override
    public boolean withdrawBalance(OfflinePlayer player, double cost) {
        return vault.withdrawPlayer(player, cost).transactionSuccess();
    }

    @Override
    public boolean deposit(OfflinePlayer player, double amount) {
        return vault.depositPlayer(player, amount).transactionSuccess();
    }
}

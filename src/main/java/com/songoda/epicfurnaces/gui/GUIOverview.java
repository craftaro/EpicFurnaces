package com.songoda.epicfurnaces.gui;

import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.boost.BoostData;
import com.songoda.epicfurnaces.furnace.Furnace;
import com.songoda.epicfurnaces.furnace.levels.Level;
import com.songoda.epicfurnaces.utils.CostType;
import com.songoda.epicfurnaces.utils.Methods;
import com.songoda.epicfurnaces.utils.gui.AbstractAnvilGUI;
import com.songoda.epicfurnaces.utils.gui.AbstractGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GUIOverview extends AbstractGUI {

    private final EpicFurnaces plugin;
    private final Furnace furnace;

    private int task;

    public GUIOverview(EpicFurnaces plugin, Furnace furnace, Player player) {
        super(player);
        this.plugin = plugin;
        this.furnace = furnace;


        init(Methods.formatName(furnace.getLevel().getLevel(), furnace.getUses(), false), 27);
        runTask();
    }

    @Override
    public void constructGUI() {
        inventory.clear();
        resetClickables();
        registerClickables();

        Level level = furnace.getLevel();
        Level nextLevel = plugin.getLevelManager().getHighestLevel().getLevel() > level.getLevel() ? plugin.getLevelManager().getLevel(level.getLevel() + 1) : null;

        int multi = plugin.getConfig().getInt("Main.Level Cost Multiplier");

        int needed = (multi * level.getLevel()) - furnace.getTolevel();

        ItemStack item = new ItemStack(Material.FURNACE, 1);

        ItemMeta itemmeta = item.getItemMeta();
        itemmeta.setDisplayName(plugin.getLocale().getMessage("interface.furnace.currentlevel", level.getLevel()));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(plugin.getLocale().getMessage("interface.furnace.smeltedx", furnace.getUses()));
        lore.addAll(level.getDescription());
        lore.add("");
        if (nextLevel == null)
            lore.add(plugin.getLocale().getMessage("interface.furnace.alreadymaxed"));
        else {
            lore.add(plugin.getLocale().getMessage("interface.furnace.level", nextLevel.getLevel()));
            lore.addAll(nextLevel.getDescription());

            if (plugin.getConfig().getBoolean("Main.Upgrade By Smelting Materials")) {
                lore.add(plugin.getLocale().getMessage("interface.furnace.tolevel", needed, Methods.cleanString(plugin.getConfig().getString("Main.Furnace Upgrade Cost"))));
            }
        }

        BoostData boostData = plugin.getBoostManager().getBoost(furnace.getPlacedBy());
        if (boostData != null) {
            String[] parts = plugin.getLocale().getMessage("interface.button.boostedstats", Integer.toString(boostData.getMultiplier()), Methods.makeReadable(boostData.getEndTime() - System.currentTimeMillis())).split("\\|");
            lore.add("");
            for (String line : parts)
                lore.add(Methods.formatText(line));
        }

        itemmeta.setLore(lore);
        item.setItemMeta(itemmeta);

        int nu = 0;
        while (nu != 27) {
            inventory.setItem(nu, Methods.getGlass());
            nu++;
        }


        ItemStack item2 = new ItemStack(Material.valueOf(plugin.getConfig().getString("Interfaces.Performance Icon")), 1);
        ItemMeta itemmeta2 = item2.getItemMeta();
        itemmeta2.setDisplayName(plugin.getLocale().getMessage("interface.furnace.performancetitle")); //greyed out until available
        ArrayList<String> lore2 = new ArrayList<>();

        String[] parts = plugin.getLocale().getMessage("interface.furnace.performanceinfo", level.getPerformance()).split("\\|");
        lore.add("");
        for (String line : parts) {
            lore2.add(Methods.formatText(line));
        }
        itemmeta2.setLore(lore2);
        item2.setItemMeta(itemmeta2);

        ItemStack item3 = new ItemStack(Material.valueOf(plugin.getConfig().getString("Interfaces.Reward Icon")), 1);
        ItemMeta itemmeta3 = item3.getItemMeta();
        itemmeta3.setDisplayName(plugin.getLocale().getMessage("interface.furnace.rewardtitle"));
        ArrayList<String> lore3 = new ArrayList<>();

        parts = plugin.getLocale().getMessage("interface.furnace.rewardinfo", level.getReward().split(":")[0].replace("%", "")).split("\\|");
        lore.add("");
        for (String line : parts) {
            lore3.add(Methods.formatText(line));
        }
        itemmeta3.setLore(lore3);
        item3.setItemMeta(itemmeta3);


        ItemStack item4 = new ItemStack(Material.valueOf(plugin.getConfig().getString("Interfaces.FuelDuration Icon")), 1);
        ItemMeta itemmeta4 = item4.getItemMeta();
        itemmeta4.setDisplayName(plugin.getLocale().getMessage("interface.furnace.fueldurationtitle"));
        ArrayList<String> lore4 = new ArrayList<>();

        parts = plugin.getLocale().getMessage("interface.furnace.fueldurationinfo", level.getFuelDuration()).split("\\|");
        lore.add("");
        for (String line : parts) {
            lore4.add(Methods.formatText(line));
        }
        itemmeta4.setLore(lore4);
        item4.setItemMeta(itemmeta4);

        ItemStack item5 = new ItemStack(Material.valueOf(plugin.getConfig().getString("Interfaces.Overheat Icon")), 1);
        ItemMeta itemmeta5 = item4.getItemMeta();
        itemmeta5.setDisplayName(plugin.getLocale().getMessage("interface.furnace.overheattitle"));
        ArrayList<String> lore5 = new ArrayList<>();

        parts = plugin.getLocale().getMessage("interface.furnace.overheatinfo", level.getOverheat() * 3).split("\\|");
        lore.add("");
        for (String line : parts) {
            lore5.add(Methods.formatText(line));
        }
        itemmeta5.setLore(lore5);
        item5.setItemMeta(itemmeta5);

        ItemStack item6 = new ItemStack(Material.valueOf(plugin.getConfig().getString("Interfaces.FuelShare Icon")), 1);
        ItemMeta itemmeta6 = item4.getItemMeta();
        itemmeta6.setDisplayName(plugin.getLocale().getMessage("interface.furnace.fuelsharetitle"));
        ArrayList<String> lore6 = new ArrayList<>();

        parts = plugin.getLocale().getMessage("interface.furnace.fuelshareinfo", level.getOverheat() * 3).split("\\|");
        lore.add("");
        for (String line : parts) {
            lore6.add(Methods.formatText(line));
        }
        itemmeta6.setLore(lore6);
        item6.setItemMeta(itemmeta6);

        ItemStack itemXP = new ItemStack(Material.valueOf(plugin.getConfig().getString("Interfaces.XP Icon")), 1);
        ItemMeta itemmetaXP = itemXP.getItemMeta();
        itemmetaXP.setDisplayName(plugin.getLocale().getMessage("interface.furnace.upgradewithxp"));
        ArrayList<String> loreXP = new ArrayList<>();
        if (nextLevel != null)
            loreXP.add(plugin.getLocale().getMessage("interface.furnace.upgradewithxplore", nextLevel.getCostExperience()));
        else
            loreXP.add(plugin.getLocale().getMessage("interface.furnace.alreadymaxed"));
        itemmetaXP.setLore(loreXP);
        itemXP.setItemMeta(itemmetaXP);

        ItemStack itemECO = new ItemStack(Material.valueOf(plugin.getConfig().getString("Interfaces.Economy Icon")), 1);
        ItemMeta itemmetaECO = itemECO.getItemMeta();
        itemmetaECO.setDisplayName(plugin.getLocale().getMessage("interface.furnace.upgradewitheconomy"));
        ArrayList<String> loreECO = new ArrayList<>();
        if (nextLevel != null)
            loreECO.add(plugin.getLocale().getMessage("interface.furnace.upgradewitheconomylore", Methods.formatEconomy(nextLevel.getCostEconomy())));
        else
            loreECO.add(plugin.getLocale().getMessage("interface.furnace.alreadymaxed"));
        itemmetaECO.setLore(loreECO);
        itemECO.setItemMeta(itemmetaECO);

        inventory.setItem(13, item);

        inventory.setItem(0, Methods.getBackgroundGlass(true));
        inventory.setItem(1, Methods.getBackgroundGlass(true));
        inventory.setItem(2, Methods.getBackgroundGlass(false));
        inventory.setItem(6, Methods.getBackgroundGlass(false));
        inventory.setItem(7, Methods.getBackgroundGlass(true));
        inventory.setItem(8, Methods.getBackgroundGlass(true));
        inventory.setItem(9, Methods.getBackgroundGlass(true));
        inventory.setItem(10, Methods.getBackgroundGlass(false));
        inventory.setItem(16, Methods.getBackgroundGlass(false));
        inventory.setItem(17, Methods.getBackgroundGlass(true));
        inventory.setItem(18, Methods.getBackgroundGlass(true));
        inventory.setItem(19, Methods.getBackgroundGlass(true));
        inventory.setItem(20, Methods.getBackgroundGlass(false));
        inventory.setItem(24, Methods.getBackgroundGlass(false));
        inventory.setItem(25, Methods.getBackgroundGlass(true));
        inventory.setItem(26, Methods.getBackgroundGlass(true));

        int num = -1;
        Map<Integer, int[]> spots = new HashMap();

        int[] s1 = {22};
        spots.put(0, s1);
        int[] s2 = {21, 23};
        spots.put(1, s2);
        int[] s3 = {21, 22, 23};
        spots.put(2, s3);
        int[] s4 = {20, 21, 23, 24};
        spots.put(3, s4);
        int[] s5 = {20, 21, 22, 23, 24};
        spots.put(4, s5);

        if (level.getPerformance() != 0) {
            num++;
        }
        if (level.getReward() != null) {
            num++;
        }
        if (level.getFuelDuration() != 0) {
            num++;
        }
        if (level.getFuelShare() != 0) {
            num++;
        }
        if (level.getOverheat() != 0) {
            num++;
        }

        int[] order = spots.get(num);

        int current = 0;

        if (level.getPerformance() != 0) {
            inventory.setItem(order[current], item2);
            current++;
        }
        if (level.getReward() != null) {
            inventory.setItem(order[current], item3);
            current++;
        }
        if (level.getFuelDuration() != 0) {
            inventory.setItem(order[current], item4);
            current++;
        }
        if (level.getFuelShare() != 0) {
            inventory.setItem(order[current], item6);
            current++;
        }
        if (level.getOverheat() != 0) {
            inventory.setItem(order[current], item5);
        }

        ItemStack hook = new ItemStack(Material.TRIPWIRE_HOOK, 1);
        ItemMeta hookmeta = hook.getItemMeta();
        hookmeta.setDisplayName(plugin.getLocale().getMessage("interface.furnace.remotefurnace"));
        ArrayList<String> lorehook = new ArrayList<>();

        String nickname = furnace.getNickname();

        parts = plugin.getLocale().getMessage("interface.furnace.remotefurnacelore", nickname == null ? "Unset" : nickname).split("\\|");

        for (String line : parts) {
            lorehook.add(Methods.formatText(line));
        }
        if (nickname != null) {
            parts = plugin.getLocale().getMessage("interface.furnace.utilize", nickname).split("\\|");
            for (String line : parts) {
                lorehook.add(Methods.formatText(line));
            }
        }

        lorehook.add("");
        lorehook.add(plugin.getLocale().getMessage("interface.furnace.remotelist"));
        for (String line : furnace.getRawAccessList()) {
            String[] halfs = line.split(":");
            String name = halfs[1];
            Player player = Bukkit.getPlayer(halfs[0]);
            if (player != null) {
                name = player.getDisplayName();
            }
            lorehook.add(Methods.formatText("&6" + name));
        }
        hookmeta.setLore(lorehook);
        hook.setItemMeta(hookmeta);

        if (plugin.getConfig().getBoolean("Main.Access Furnaces Remotely")
                && player.hasPermission("EpicFurnaces.Remote")) {
            inventory.setItem(4, hook);
            registerClickable(4, ((player1, inventory1, cursor, slot, type) -> {
                if (type == ClickType.LEFT) {

                    AbstractAnvilGUI gui = new AbstractAnvilGUI(player, anvilEvent -> {
                        for (Furnace other : plugin.getFurnaceManager().getFurnaces().values()) {
                            if (other.getNickname() == null) {
                                continue;
                            }

                            if (other.getNickname().equalsIgnoreCase(anvilEvent.getName())) {
                                player.sendMessage(plugin.getReferences().getPrefix() + plugin.getLocale().getMessage("event.remote.nicknameinuse"));
                                return;
                            }
                        }

                        furnace.setNickname(anvilEvent.getName());
                        player.sendMessage(plugin.getReferences().getPrefix() + plugin.getLocale().getMessage("event.remote.nicknamesuccess"));
                    });

                    gui.setOnClose((player2, inventory2) -> init(setTitle, inventory.getSize()));

                    ItemStack itemO = new ItemStack(Material.PAPER);
                    ItemMeta meta = itemO.getItemMeta();
                    meta.setDisplayName(furnace.getNickname() == null ? "Enter a nickname" : furnace.getNickname());
                    itemO.setItemMeta(meta);
                    gui.setSlot(AbstractAnvilGUI.AnvilSlot.INPUT_LEFT, itemO);
                    gui.open();

                    player.sendMessage(plugin.getReferences().getPrefix() + plugin.getLocale().getMessage("event.remote.enter"));


                } else if (type == ClickType.RIGHT) {
                    furnace.addToAccessList(player);
                    constructGUI();
                }
            }));
        }

        inventory.setItem(13, item);

        if (plugin.getConfig().getBoolean("Main.Upgrade With XP")
                && player.hasPermission("EpicFurnaces.Upgrade.XP")
                && level.getCostExperience() != -1) {
            inventory.setItem(11, itemXP);

            registerClickable(11, ((player, inventory, cursor, slot, type) -> {
                furnace.upgrade(player, CostType.EXPERIENCE);
                furnace.overview(player);
            }));
        }
        if (plugin.getConfig().getBoolean("Main.Upgrade With Economy")
                && player.hasPermission("EpicFurnaces.Upgrade.ECO")
                && level.getCostEconomy() != -1) {
            inventory.setItem(15, itemECO);

            registerClickable(15, ((player, inventory, cursor, slot, type) -> {
                furnace.upgrade(player, CostType.ECONOMY);
                furnace.overview(player);
            }));
        }
    }

    private void runTask() {
        task = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::constructGUI, 5L, 5L);
    }

    @Override
    protected void registerClickables() {

    }

    @Override
    protected void registerOnCloses() {
        registerOnClose(((player1, inventory1) -> Bukkit.getScheduler().cancelTask(task)));
    }
}

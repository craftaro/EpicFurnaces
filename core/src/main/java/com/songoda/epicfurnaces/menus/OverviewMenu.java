package com.songoda.epicfurnaces.menus;

import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.objects.BoostData;
import com.songoda.epicfurnaces.objects.FurnaceObject;
import com.songoda.epicfurnaces.objects.Level;
import com.songoda.epicfurnaces.utils.Methods;
import com.songoda.epicfurnaces.utils.StringUtils;
import com.songoda.epicfurnaces.utils.gui.AbstractAnvilGUI;
import com.songoda.epicfurnaces.utils.gui.FastInv;
import com.songoda.epicfurnaces.utils.gui.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.songoda.epicfurnaces.utils.gui.AbstractAnvilGUI.AnvilSlot.INPUT_LEFT;
import static org.bukkit.Material.*;

public class OverviewMenu extends FastInv {

    public OverviewMenu(EpicFurnaces instance, FurnaceObject furnace, Player accessor) {
        super(27, StringUtils.formatName(furnace.getLevel().getLevel(), 0, false));

        Level nextLevel = instance.getLevelManager().getHighestLevel().getLevel() > furnace.getLevel().getLevel() ? instance.getLevelManager().getLevel(furnace.getLevel().getLevel() + 1) : null;
        int multi = instance.getConfig().getInt("Main.Level Cost Multiplier");
        int needed = (multi * furnace.getLevel().getLevel()) - furnace.getToLevel();

        ItemBuilder currentLevel = new ItemBuilder(FURNACE);
        currentLevel.name(instance.getLocale().getMessage("interface.furnace.currentlevel", furnace.getLevel().getLevel()));
        currentLevel.lore(instance.getLocale().getMessage("interface.furnace.smeltedx", furnace.getUses()));
        currentLevel.addLore(furnace.getLevel().getDescription());
        currentLevel.addLore("");

        if (nextLevel == null)
            currentLevel.addLore(instance.getLocale().getMessage("interface.furnace.alreadymaxed"));
        else {
            currentLevel.addLore(instance.getLocale().getMessage("interface.furnace.level", nextLevel.getLevel()));
            currentLevel.addLore(nextLevel.getDescription());

            if (instance.getConfig().getBoolean("Main.Upgrade By Smelting Materials")) {
                currentLevel.addLore(instance.getLocale().getMessage("interface.furnace.tolevel", needed, StringUtils.cleanString(instance.getConfig().getString("Main.Furnace Upgrade Cost"))));
            }
        }

        BoostData boostData = instance.getBoostManager().getBoost(furnace.getPlacedBy());
        if (boostData != null) {
            String[] parts = instance.getLocale().getMessage("interface.button.boostedstats", "" + boostData.getMultiplier(), StringUtils.msToString(boostData.getEndTime() - System.currentTimeMillis())).split("\\|");
            currentLevel.addLore("");
            for (String line : parts) {
                currentLevel.addLore(StringUtils.formatText(line));
            }
        }

        fill(Methods.getGlass());

        ItemBuilder performance = new ItemBuilder(instance.getBukkitEnums().getMaterial(instance.getConfig().getString("Interfaces.Performance Icon")));
        performance.name(instance.getLocale().getMessage("interface.furnace.performancetitle"));
        String[] parts = instance.getLocale().getMessage("interface.furnace.performanceinfo", furnace.getLevel().getPerformance()).split("\\|");
        performance.lore(Arrays.stream(parts).map(StringUtils::formatText).collect(Collectors.toList()));

        ItemBuilder reward = new ItemBuilder(instance.getBukkitEnums().getMaterial(instance.getConfig().getString("Interfaces.Reward Icon")));
        reward.name(instance.getLocale().getMessage("interface.furnace.rewardtitle"));
        parts = instance.getLocale().getMessage("interface.furnace.rewardinfo", furnace.getLevel().getReward().split(":")[0].replace("%", "")).split("\\|");
        reward.lore(Arrays.stream(parts).map(StringUtils::formatText).collect(Collectors.toList()));

        ItemBuilder duration = new ItemBuilder(instance.getBukkitEnums().getMaterial(instance.getConfig().getString("Interfaces.FuelDuration Icon")));
        duration.name(instance.getLocale().getMessage("interface.furnace.fueldurationtitle"));
        parts = instance.getLocale().getMessage("interface.furnace.fueldurationinfo", furnace.getLevel().getFuelDuration()).split("\\|");
        duration.lore(Arrays.stream(parts).map(StringUtils::formatText).collect(Collectors.toList()));

        ItemBuilder overheat = new ItemBuilder(instance.getBukkitEnums().getMaterial(instance.getConfig().getString("Interfaces.Overheat Icon")));
        overheat.name(instance.getLocale().getMessage("interface.furnace.overheattitle"));
        parts = instance.getLocale().getMessage("interface.furnace.overheatinfo", furnace.getLevel().getOverheat() * 3).split("\\|");
        overheat.lore(Arrays.stream(parts).map(StringUtils::formatText).collect(Collectors.toList()));

        ItemBuilder fuelShare = new ItemBuilder(instance.getBukkitEnums().getMaterial(instance.getConfig().getString("Interfaces.FuelShare Icon")));
        fuelShare.name(instance.getLocale().getMessage("interface.furnace.fuelsharetitle"));
        parts = instance.getLocale().getMessage("interface.furnace.fuelshareinfo", furnace.getLevel().getFuelShare() * 3).split("\\|");
        fuelShare.lore(Arrays.stream(parts).map(StringUtils::formatText).collect(Collectors.toList()));

        ItemBuilder xp = new ItemBuilder(instance.getBukkitEnums().getMaterial(instance.getConfig().getString("Interfaces.XP Icon")));
        xp.name(instance.getLocale().getMessage("interface.furnace.upgradewithxp"));
        xp.lore(nextLevel == null ? instance.getLocale().getMessage("interface.furnace.alreadymaxed") :
                instance.getLocale().getMessage("interface.furnace.upgradewithxplore", furnace.getLevel().getCostExperience()));

        ItemBuilder eco = new ItemBuilder(instance.getBukkitEnums().getMaterial(instance.getConfig().getString("Interfaces.Economy Icon")));
        eco.name(instance.getLocale().getMessage("interface.furnace.upgradewitheconomy"));
        eco.lore(nextLevel == null ? instance.getLocale().getMessage("interface.furnace.alreadymaxed") :
                instance.getLocale().getMessage("interface.furnace.upgradewitheconomylore", StringUtils.formatEconomy(furnace.getLevel().getCostEconomy())));

        ItemBuilder remote = new ItemBuilder(TRIPWIRE_HOOK);
        remote.name(instance.getLocale().getMessage("interface.furnace.remotefurnace"));
        parts = instance.getLocale().getMessage("interface.furnace.remotefurnacelore", furnace.getNickname() == null ? "Unset" : furnace.getNickname()).split("\\|");
        remote.lore(Arrays.stream(parts).map(StringUtils::formatText).collect(Collectors.toList()));

        if (furnace.getNickname() != null) {
            parts = instance.getLocale().getMessage("interface.furnace.utilize", furnace.getNickname()).split("\\|");
            remote.addLore(Arrays.stream(parts).map(StringUtils::formatText).collect(Collectors.toList()));
        }

        if (!furnace.getOriginalAccessList().isEmpty()) {
            remote.addLore("");
            remote.addLore(instance.getLocale().getMessage("interface.furnace.remotelist"));
        }

        for (String line : furnace.getOriginalAccessList()) {
            String[] halfs = line.split(":");
            String name = halfs[1];
            Player player = Bukkit.getPlayer(halfs[0]);

            if (player != null) {
                name = player.getDisplayName();
            }
            remote.addLore(StringUtils.formatText("&6" + name));
        }

        addItem(new int[]{0, 1, 7, 8, 9, 17, 18, 19, 25, 26}, Methods.getBackgroundGlass(true));
        addItem(new int[]{2, 6, 10, 16, 20, 24}, Methods.getBackgroundGlass(false));

        int num = -1;
        Map<Integer, int[]> spots = new HashMap<Integer, int[]>() {{
            put(0, new int[]{22});
            put(1, new int[]{21, 23});
            put(2, new int[]{21, 22, 23});
            put(3, new int[]{20, 21, 23, 24});
            put(4, new int[]{20, 21, 22, 23, 24});
        }};

        num += furnace.getLevel().getPerformance() == 0 ? 0 : 1;
        num += (furnace.getLevel().getReward() == null ? 0 : 1);
        num += furnace.getLevel().getFuelDuration() == 0 ? 0 : 1;
        num += furnace.getLevel().getFuelShare() == 0 ? 0 : 1;
        num += furnace.getLevel().getOverheat() == 0 ? 0 : 1;

        int[] order = spots.get(num);
        int current = 0;

        if (furnace.getLevel().getPerformance() != 0) {
            addItem(order[current], performance.build());
            current++;
        }
        if (furnace.getLevel().getReward() != null) {
            addItem(order[current], reward.build());
            current++;
        }
        if (furnace.getLevel().getFuelDuration() != 0) {
            addItem(order[current], duration.build());
            current++;
        }
        if (furnace.getLevel().getFuelShare() != 0) {
            addItem(order[current], fuelShare.build());
            current++;
        }
        if (furnace.getLevel().getOverheat() != 0) {
            addItem(order[current], overheat.build());
        }

        if (instance.getConfig().getBoolean("Main.Access Furnaces Remotely") && accessor.hasPermission("EpicFurnaces.Remote")) {
            addItem(4, remote.build(), event -> {
                if (event.getClickType().isLeftClick()) {
                    event.getPlayer().sendMessage(instance.getLocale().getPrefix() + instance.getLocale().getMessage("event.remote.enter"));

                    AbstractAnvilGUI anvilGUI = new AbstractAnvilGUI(instance, event.getPlayer(), anvilEvent -> {
                        for (FurnaceObject other : instance.getFurnaceManager().getAllFurnaces().values()) {
                            if (other.getNickname() == null) {
                                continue;
                            }

                            if (other.getNickname().equalsIgnoreCase(anvilEvent.getName())) {
                                event.getPlayer().sendMessage(instance.getLocale().getPrefix() + instance.getLocale().getMessage("event.remote.nicknameinuse"));
                                furnace.openOverview(event.getPlayer());
                                return;
                            }
                        }

                        furnace.setNickname(anvilEvent.getName());
                        furnace.openOverview(event.getPlayer());
                        event.getPlayer().sendMessage(instance.getLocale().getPrefix() + instance.getLocale().getMessage("event.remote.nicknamesuccess"));
                    });

                    anvilGUI.setOnClose((closer, inv) -> furnace.openOverview(closer));
                    anvilGUI.setSlot(INPUT_LEFT, new ItemBuilder(PAPER).name("Enter a nickname").build());
                    anvilGUI.open();
                } else if (event.getClickType().isRightClick()) {
                    if (!furnace.getAccessList().contains(event.getPlayer().getUniqueId())) {
                        furnace.addToAccessList(event.getPlayer().getUniqueId().toString() + ":" + event.getPlayer().getName());
                        furnace.openOverview(event.getPlayer());
                    }
                }
            });
        }

        if (instance.getConfig().getBoolean("Main.Upgrade With XP") && accessor.hasPermission("EpicFurnaces.Upgrade.XP")) {
            addItem(11, xp.build(), event -> {
                furnace.upgrade("XP", event.getPlayer());
                furnace.openOverview(event.getPlayer());
            });
        }
        if (instance.getConfig().getBoolean("Main.Upgrade With Economy") && accessor.hasPermission("EpicFurnaces.Upgrade.ECO")) {
            addItem(15, eco.build(), event -> {
                furnace.upgrade("ECO", event.getPlayer());
                furnace.openOverview(event.getPlayer());
            });
        }

        addItem(13, currentLevel.build());

    }
}

package com.songoda.epicfurnaces.gui;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.gui.CustomizableGui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.core.input.ChatPrompt;
import com.songoda.core.utils.NumberUtils;
import com.songoda.core.utils.TextUtils;
import com.songoda.core.utils.TimeUtils;
import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.boost.BoostData;
import com.songoda.epicfurnaces.furnace.Furnace;
import com.songoda.epicfurnaces.furnace.levels.Level;
import com.songoda.epicfurnaces.settings.Settings;
import com.songoda.epicfurnaces.utils.CostType;
import com.songoda.epicfurnaces.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GUIOverview extends CustomizableGui {
    private final EpicFurnaces plugin;
    private final Furnace furnace;
    private final Player player;
    static int[][] infoIconOrder = new int[][]{{22}, {21, 23}, {21, 22, 23}, {20, 21, 23, 24}, {20, 21, 22, 23, 24}};

    private int task;

    public GUIOverview(EpicFurnaces plugin, Furnace furnace, Player player) {
        super(plugin, "overview");
        this.plugin = plugin;
        this.furnace = furnace;
        this.player = player;

        setRows(3);
        setTitle(Methods.formatName(furnace.getLevel().getLevel()));
        runTask();
        constructGUI();
        this.setOnClose(action -> Bukkit.getScheduler().cancelTask(this.task));
    }

    private void constructGUI() {
        ItemStack glass1 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_1.getMaterial());
        ItemStack glass2 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_2.getMaterial());
        ItemStack glass3 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_3.getMaterial());

        setDefaultItem(glass1);

        mirrorFill("mirrorfill_1", 0, 0, true, true, glass2);
        mirrorFill("mirrorfill_2", 0, 1, true, true, glass2);
        mirrorFill("mirrorfill_3", 0, 2, true, true, glass3);
        mirrorFill("mirrorfill_4", 1, 0, false, true, glass2);
        mirrorFill("mirrorfill_5", 1, 1, false, true, glass3);

        Level level = this.furnace.getLevel();
        Level nextLevel = this.plugin.getLevelManager().getHighestLevel().getLevel() > level.getLevel() ? this.plugin.getLevelManager().getLevel(level.getLevel() + 1) : null;

        // main furnace information icon
        setItem("information", 1, 4, GuiUtils.createButtonItem(
                CompatibleMaterial.getMaterial(this.furnace.getLocation().getBlock().getType()),
                this.plugin.getLocale().getMessage("interface.furnace.currentlevel")
                        .processPlaceholder("level", level.getLevel()).getMessage(),
                getFurnaceDescription(this.furnace, level, nextLevel)));

        // check how many info icons we have to show
        int num = -1;
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

        int current = 0;

        if (level.getPerformance() != 0) {
            setItem("performance", infoIconOrder[num][current++], GuiUtils.createButtonItem(
                    Settings.PERFORMANCE_ICON.getMaterial(CompatibleMaterial.REDSTONE),
                    this.plugin.getLocale().getMessage("interface.furnace.performancetitle").getMessage(),
                    this.plugin.getLocale().getMessage("interface.furnace.performanceinfo")
                            .processPlaceholder("amount", level.getPerformance()).getMessage().split("\\|")));
        }
        if (level.getReward() != null) {
            setItem("reward", infoIconOrder[num][current++], GuiUtils.createButtonItem(
                    Settings.REWARD_ICON.getMaterial(CompatibleMaterial.GOLDEN_APPLE),
                    this.plugin.getLocale().getMessage("interface.furnace.rewardtitle").getMessage(),
                    this.plugin.getLocale().getMessage("interface.furnace.rewardinfo")
                            .processPlaceholder("amount", level.getReward().split(":")[0].replace("%", ""))
                            .getMessage().split("\\|")));
        }
        if (level.getFuelDuration() != 0) {
            setItem("fuel", infoIconOrder[num][current++], GuiUtils.createButtonItem(
                    Settings.FUEL_DURATION_ICON.getMaterial(CompatibleMaterial.COAL),
                    this.plugin.getLocale().getMessage("interface.furnace.fueldurationtitle").getMessage(),
                    this.plugin.getLocale().getMessage("interface.furnace.fueldurationinfo")
                            .processPlaceholder("amount", level.getFuelDuration())
                            .getMessage().split("\\|")));
        }
        if (level.getFuelShare() != 0) {
            setItem("fuel_share", infoIconOrder[num][current++], GuiUtils.createButtonItem(
                    Settings.FUEL_SHARE_ICON.getMaterial(CompatibleMaterial.COAL_BLOCK),
                    this.plugin.getLocale().getMessage("interface.furnace.fuelsharetitle").getMessage(),
                    this.plugin.getLocale().getMessage("interface.furnace.fuelshareinfo")
                            .processPlaceholder("amount", level.getOverheat() * 3)
                            .getMessage().split("\\|")));
        }
        if (level.getOverheat() != 0) {
            setItem("overheat", infoIconOrder[num][current++], GuiUtils.createButtonItem(
                    Settings.OVERHEAT_ICON.getMaterial(CompatibleMaterial.FIRE_CHARGE),
                    this.plugin.getLocale().getMessage("interface.furnace.overheattitle").getMessage(),
                    this.plugin.getLocale().getMessage("interface.furnace.overheatinfo")
                            .processPlaceholder("amount", level.getOverheat() * 3)
                            .getMessage().split("\\|")));
        }

        // remote control
        if (Settings.REMOTE.getBoolean() && this.player.hasPermission("EpicFurnaces.Remote")) {
            setButton("remote", 4, GuiUtils.createButtonItem(
                            CompatibleMaterial.TRIPWIRE_HOOK,
                            this.plugin.getLocale().getMessage("interface.furnace.remotefurnace").getMessage(),
                            getFurnaceRemoteLore(this.furnace)),
                    ClickType.LEFT, (event) -> {
                        ChatPrompt.showPrompt(this.plugin, event.player, this.plugin.getLocale().getMessage("event.remote.enter").getMessage(),
                                promptEvent -> {
                                    for (Furnace other : this.plugin.getFurnaceManager().getFurnaces().values()) {
                                        if (other.getNickname() == null) {
                                            continue;
                                        }

                                        if (other.getNickname().equalsIgnoreCase(promptEvent.getMessage())) {
                                            this.plugin.getLocale().getMessage("event.remote.nicknameinuse").sendPrefixedMessage(this.player);
                                            return;
                                        }
                                    }

                                    this.plugin.getDataManager().queueFurnaceForUpdate(this.furnace);
                                    this.furnace.setNickname(promptEvent.getMessage());
                                    this.plugin.getLocale().getMessage("event.remote.nicknamesuccess").sendPrefixedMessage(this.player);
                                }).setOnClose(() -> this.guiManager.showGUI(this.player, new GUIOverview(this.plugin, this.furnace, this.player)));

                    }).setAction(4, ClickType.RIGHT, (event) -> {
                this.guiManager.showGUI(this.player, new GUIRemoteAccess(this.plugin, this.furnace, this.player));
            });
        }

        if (Settings.UPGRADE_WITH_XP.getBoolean()
                && level.getCostExperience() != -1
                && this.player.hasPermission("EpicFurnaces.Upgrade.XP")) {
            setButton("upgrade_xp", 1, 2, GuiUtils.createButtonItem(
                            Settings.XP_ICON.getMaterial(CompatibleMaterial.EXPERIENCE_BOTTLE),
                            this.plugin.getLocale().getMessage("interface.furnace.upgradewithxp").getMessage(),
                            nextLevel != null
                                    ? this.plugin.getLocale().getMessage("interface.furnace.upgradewithxplore")
                                    .processPlaceholder("cost", nextLevel.getCostExperience()).getMessage()
                                    : this.plugin.getLocale().getMessage("interface.furnace.alreadymaxed").getMessage()),
                    (event) -> {
                        this.furnace.upgrade(this.player, CostType.EXPERIENCE);
                        this.furnace.overview(this.guiManager, this.player);
                    });
        }
        if (Settings.UPGRADE_WITH_ECONOMY.getBoolean()
                && level.getCostEconomy() != -1
                && this.player.hasPermission("EpicFurnaces.Upgrade.ECO")) {
            setButton("upgrade_economy", 1, 6, GuiUtils.createButtonItem(
                            Settings.ECO_ICON.getMaterial(CompatibleMaterial.SUNFLOWER),
                            this.plugin.getLocale().getMessage("interface.furnace.upgradewitheconomy").getMessage(),
                            nextLevel != null
                                    ? this.plugin.getLocale().getMessage("interface.furnace.upgradewitheconomylore")
                                    .processPlaceholder("cost", NumberUtils.formatNumber(nextLevel.getCostEconomy())).getMessage()
                                    : this.plugin.getLocale().getMessage("interface.furnace.alreadymaxed").getMessage()),
                    (event) -> {
                        this.furnace.upgrade(this.player, CostType.ECONOMY);
                        this.furnace.overview(this.guiManager, this.player);
                    });
        }
    }

    private void runTask() {
        this.task = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, () -> {
            if (this.inventory.getViewers().size() != 0) {
                this.constructGUI();
            }
        }, 5L, 5L);
    }

    List<String> getFurnaceDescription(Furnace furnace, Level level, Level nextLevel) {
        ArrayList<String> lore = new ArrayList<>();
        lore.add(this.plugin.getLocale().getMessage("interface.furnace.smeltedx")
                .processPlaceholder("amount", furnace.getUses()).getMessage());
        lore.addAll(level.getDescription());
        lore.add("");
        if (nextLevel == null) {
            lore.add(this.plugin.getLocale().getMessage("interface.furnace.alreadymaxed").getMessage());
        } else {
            lore.add(this.plugin.getLocale().getMessage("interface.furnace.level")
                    .processPlaceholder("level", nextLevel.getLevel()).getMessage());
            lore.addAll(nextLevel.getDescription());

            if (Settings.UPGRADE_BY_SMELTING.getBoolean()) {
                lore.add(this.plugin.getLocale().getMessage("interface.furnace.itemsneeded").getMessage());
                for (Map.Entry<CompatibleMaterial, Integer> entry : level.getMaterials().entrySet()) {
                    lore.add(this.plugin.getLocale().getMessage("interface.furnace.neededitem")
                            .processPlaceholder("amount", entry.getValue() - furnace.getToLevel(entry.getKey()))
                            .processPlaceholder("type", Methods.cleanString(entry.getKey().name()))
                            .getMessage());
                }
            }
        }

        BoostData boostData = this.plugin.getBoostManager().getBoost(furnace.getPlacedBy());
        if (boostData != null) {
            lore.addAll(Arrays.asList(this.plugin.getLocale().getMessage("interface.button.boostedstats")
                    .processPlaceholder("amount", Integer.toString(boostData.getMultiplier()))
                    .processPlaceholder("time", TimeUtils.makeReadable(boostData.getEndTime() - System.currentTimeMillis()))
                    .getMessage().split("\\|")));
        }
        return lore;
    }

    List<String> getFurnaceRemoteLore(Furnace furnace) {
        String nickname = furnace.getNickname();
        ArrayList<String> loreHook = new ArrayList<>(Arrays.asList(this.plugin.getLocale().getMessage("interface.furnace.remotefurnacelore")
                .processPlaceholder("nickname", nickname == null ? "Unset" : nickname).getMessage().split("\\|")));

        if (nickname != null) {
            loreHook.addAll(Arrays.asList(this.plugin.getLocale().getMessage("interface.furnace.utilize")
                    .processPlaceholder("nickname", nickname).getMessage().split("\\|")));
        }

        loreHook.add("");
        loreHook.add(this.plugin.getLocale().getMessage("interface.furnace.remotelist").getMessage());
        for (UUID uuid : furnace.getAccessList()) {
            OfflinePlayer remotePlayer = Bukkit.getOfflinePlayer(uuid);
            loreHook.add(TextUtils.formatText("&6" + remotePlayer.getName()));
        }
        return loreHook;
    }
}

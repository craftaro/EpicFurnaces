package com.songoda.epicfurnaces.gui;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.gui.CustomizableGui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.core.input.ChatPrompt;
import com.songoda.core.locale.Locale;
import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.EpicFurnaceInstances;
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

public final class GUIOverview extends CustomizableGui implements EpicFurnaceInstances {

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
        this.setOnClose(action -> Bukkit.getScheduler().cancelTask(task));
    }

    private void constructGUI() {
        final ItemStack glass1 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_1.getMaterial());
        final ItemStack glass2 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_2.getMaterial());
        final ItemStack glass3 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_3.getMaterial());

        setDefaultItem(glass1);

        mirrorFill("mirrorfill_1", 0, 0, true, true, glass2);
        mirrorFill("mirrorfill_2", 0, 1, true, true, glass2);
        mirrorFill("mirrorfill_3", 0, 2, true, true, glass3);
        mirrorFill("mirrorfill_4", 1, 0, false, true, glass2);
        mirrorFill("mirrorfill_5", 1, 1, false, true, glass3);

        final Level level = furnace.getLevel();
        final Level nextLevel = LEVEL_MANAGER.getHighestLevel().getLevel() > level.getLevel() ? LEVEL_MANAGER.getLevel(level.getLevel() + 1) : null;

        // main furnace information icon
        setItem("information",1, 4, GuiUtils.createButtonItem(
                CompatibleMaterial.getMaterial(furnace.getLocation().getBlock().getType()),
                plugin.getLocale().getMessage("interface.furnace.currentlevel")
                        .processPlaceholder("level", level.getLevel()).getMessage(),
                getFurnaceDescription(furnace, level, nextLevel)));

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
        
        final Locale locale = plugin.getLocale();
        if (level.getPerformance() != 0) {
            setItem("performance",  infoIconOrder[num][current++], GuiUtils.createButtonItem(
                    Settings.PERFORMANCE_ICON.getMaterial(CompatibleMaterial.REDSTONE),
                    locale.getMessage("interface.furnace.performancetitle").getMessage(),
                    locale.getMessage("interface.furnace.performanceinfo")
                            .processPlaceholder("amount", level.getPerformance()).getMessage().split("\\|")));
        }
        if (level.getReward() != null) {
            setItem("reward", infoIconOrder[num][current++], GuiUtils.createButtonItem(
                    Settings.REWARD_ICON.getMaterial(CompatibleMaterial.GOLDEN_APPLE),
                    locale.getMessage("interface.furnace.rewardtitle").getMessage(),
                    locale.getMessage("interface.furnace.rewardinfo")
                            .processPlaceholder("amount", level.getReward().split(":")[0].replace("%", ""))
                            .getMessage().split("\\|")));
        }
        if (level.getFuelDuration() != 0) {
            setItem("fuel", infoIconOrder[num][current++], GuiUtils.createButtonItem(
                    Settings.FUEL_DURATION_ICON.getMaterial(CompatibleMaterial.COAL),
                    locale.getMessage("interface.furnace.fueldurationtitle").getMessage(),
                    locale.getMessage("interface.furnace.fueldurationinfo")
                            .processPlaceholder("amount", level.getFuelDuration())
                            .getMessage().split("\\|")));
        }
        if (level.getFuelShare() != 0) {
            setItem("fuel_share", infoIconOrder[num][current++], GuiUtils.createButtonItem(
                    Settings.FUEL_SHARE_ICON.getMaterial(CompatibleMaterial.COAL_BLOCK),
                    locale.getMessage("interface.furnace.fuelsharetitle").getMessage(),
                    locale.getMessage("interface.furnace.fuelshareinfo")
                            .processPlaceholder("amount", level.getOverheat() * 3)
                            .getMessage().split("\\|")));
        }
        if (level.getOverheat() != 0) {
            setItem("overheat", infoIconOrder[num][current++], GuiUtils.createButtonItem(
                    Settings.OVERHEAT_ICON.getMaterial(CompatibleMaterial.FIRE_CHARGE),
                    locale.getMessage("interface.furnace.overheattitle").getMessage(),
                    locale.getMessage("interface.furnace.overheatinfo")
                            .processPlaceholder("amount", level.getOverheat() * 3)
                            .getMessage().split("\\|")));
        }

        // remote control
        if (Settings.REMOTE.getBoolean() && player.hasPermission("EpicFurnaces.Remote")) {
            setButton("remote", 4, GuiUtils.createButtonItem(
                    CompatibleMaterial.TRIPWIRE_HOOK,
                    locale.getMessage("interface.furnace.remotefurnace").getMessage(),
                    getFurnaceRemoteLore(furnace)),
                    ClickType.LEFT, (event) -> {
                        ChatPrompt.showPrompt(plugin, event.player, locale.getMessage("event.remote.enter").getMessage(),
                                promptEvent -> {
                                    final String promptMessage = promptEvent.getMessage();
                                    for (Furnace other : FURNACE_MANAGER.getFurnaces().values()) {
                                        final String nickname = other.getNickname();
                                        if (nickname == null) {
                                            continue;
                                        }

                                        if (nickname.equalsIgnoreCase(promptMessage)) {
                                            locale.getMessage("event.remote.nicknameinuse").sendPrefixedMessage(player);
                                            return;
                                        }
                                    }

                                    plugin.getDataManager().queueFurnaceForUpdate(furnace);
                                    furnace.setNickname(promptMessage);
                                    locale.getMessage("event.remote.nicknamesuccess").sendPrefixedMessage(player);
                                }).setOnClose(() -> guiManager.showGUI(player, new GUIOverview(plugin, furnace, player)));

                    }).setAction(4, ClickType.RIGHT, (event) -> {
                guiManager.showGUI(player, new GUIRemoteAccess(furnace, player));
            });
        }

        if (Settings.UPGRADE_WITH_XP.getBoolean()
                && level.getCostExperience() != -1
                && player.hasPermission("EpicFurnaces.Upgrade.XP")) {
            setButton("upgrade_xp", 1, 2, GuiUtils.createButtonItem(
                    Settings.XP_ICON.getMaterial(CompatibleMaterial.EXPERIENCE_BOTTLE),
                    locale.getMessage("interface.furnace.upgradewithxp").getMessage(),
                    nextLevel != null
                            ? locale.getMessage("interface.furnace.upgradewithxplore")
                            .processPlaceholder("cost", nextLevel.getCostExperience()).getMessage()
                            : locale.getMessage("interface.furnace.alreadymaxed").getMessage()),
                    (event) -> {
                        furnace.upgrade(player, CostType.EXPERIENCE);
                        furnace.overview(guiManager, player);
                    });
        }
        if (Settings.UPGRADE_WITH_ECONOMY.getBoolean()
                && level.getCostEconomy() != -1
                && player.hasPermission("EpicFurnaces.Upgrade.ECO")) {
            setButton("upgrade_economy", 1, 6, GuiUtils.createButtonItem(
                    Settings.ECO_ICON.getMaterial(CompatibleMaterial.SUNFLOWER),
                    locale.getMessage("interface.furnace.upgradewitheconomy").getMessage(),
                    nextLevel != null
                            ? locale.getMessage("interface.furnace.upgradewitheconomylore")
                            .processPlaceholder("cost", Methods.formatEconomy(nextLevel.getCostEconomy())).getMessage()
                            : locale.getMessage("interface.furnace.alreadymaxed").getMessage()),
                    (event) -> {
                        furnace.upgrade(player, CostType.ECONOMY);
                        furnace.overview(guiManager, player);
                    });
        }
    }

    private void runTask() {
        task = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (inventory.getViewers().size() != 0)
                this.constructGUI();
        }, 5L, 5L);
    }

    private List<String> getFurnaceDescription(Furnace furnace, Level level, Level nextLevel) {
        final Locale locale = plugin.getLocale();
        final ArrayList<String> lore = new ArrayList<>();
        lore.add(locale.getMessage("interface.furnace.smeltedx")
                .processPlaceholder("amount", furnace.getUses()).getMessage());
        lore.addAll(level.getDescription());
        lore.add("");
        if (nextLevel == null) {
            lore.add(locale.getMessage("interface.furnace.alreadymaxed").getMessage());
        } else {
            lore.add(locale.getMessage("interface.furnace.level")
                    .processPlaceholder("level", nextLevel.getLevel()).getMessage());
            lore.addAll(nextLevel.getDescription());

            if (Settings.UPGRADE_BY_SMELTING.getBoolean()) {
                lore.add(locale.getMessage("interface.furnace.itemsneeded").getMessage());
                for (Map.Entry<CompatibleMaterial, Integer> entry : level.getMaterials().entrySet())
                    lore.add(locale.getMessage("interface.furnace.neededitem")
                            .processPlaceholder("amount", entry.getValue() - furnace.getToLevel(entry.getKey()))
                            .processPlaceholder("type", Methods.cleanString(entry.getKey().name()))
                            .getMessage());
            }
        }

        BoostData boostData = BOOST_MANAGER.getBoost(furnace.getPlacedBy());
        if (boostData != null) {
            lore.addAll(Arrays.asList(locale.getMessage("interface.button.boostedstats")
                    .processPlaceholder("amount", Integer.toString(boostData.getMultiplier()))
                    .processPlaceholder("time", Methods.makeReadable(boostData.getEndTime() - System.currentTimeMillis()))
                    .getMessage().split("\\|")));
        }
        return lore;
    }

    private List<String> getFurnaceRemoteLore(Furnace furnace) {
        final String nickname = furnace.getNickname();
        final Locale locale = plugin.getLocale();
        final ArrayList<String> lorehook = new ArrayList<>(Arrays.asList(locale.getMessage("interface.furnace.remotefurnacelore")
                .processPlaceholder("nickname", nickname == null ? "Unset" : nickname).getMessage().split("\\|")));

        if (nickname != null) {
            lorehook.addAll(Arrays.asList(locale.getMessage("interface.furnace.utilize")
                    .processPlaceholder("nickname", nickname).getMessage().split("\\|")));
        }

        lorehook.add("");
        lorehook.add(locale.getMessage("interface.furnace.remotelist").getMessage());
        for (UUID uuid : furnace.getAccessList()) {
            final OfflinePlayer remotePlayer = Bukkit.getOfflinePlayer(uuid);
            lorehook.add(Methods.formatText("&6" + remotePlayer.getName()));
        }
        return lorehook;
    }
}

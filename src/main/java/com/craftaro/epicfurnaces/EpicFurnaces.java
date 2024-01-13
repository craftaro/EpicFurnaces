package com.craftaro.epicfurnaces;

import com.craftaro.core.SongodaCore;
import com.craftaro.core.SongodaPlugin;
import com.craftaro.core.commands.CommandManager;
import com.craftaro.core.compatibility.CompatibleMaterial;
import com.craftaro.core.configuration.Config;
import com.craftaro.core.database.Data;
import com.craftaro.core.gui.GuiManager;
import com.craftaro.core.hooks.EconomyManager;
import com.craftaro.core.hooks.HologramManager;
import com.craftaro.core.hooks.ProtectionManager;
import com.craftaro.core.third_party.de.tr7zw.nbtapi.NBTItem;
import com.craftaro.core.utils.TextUtils;
import com.craftaro.epicfurnaces.boost.BoostData;
import com.craftaro.epicfurnaces.boost.BoostManager;
import com.craftaro.epicfurnaces.commands.CommandBoost;
import com.craftaro.epicfurnaces.commands.CommandGive;
import com.craftaro.epicfurnaces.commands.CommandReload;
import com.craftaro.epicfurnaces.commands.CommandRemote;
import com.craftaro.epicfurnaces.commands.CommandSettings;
import com.craftaro.epicfurnaces.compatibility.FabledSkyBlockLoader;
import com.craftaro.epicfurnaces.database.DataHelper;
import com.craftaro.epicfurnaces.database.migrations._1_InitialMigration;
import com.craftaro.epicfurnaces.furnace.Furnace;
import com.craftaro.epicfurnaces.furnace.FurnaceBuilder;
import com.craftaro.epicfurnaces.furnace.FurnaceManager;
import com.craftaro.epicfurnaces.furnace.levels.LevelManager;
import com.craftaro.epicfurnaces.handlers.BlacklistHandler;
import com.craftaro.epicfurnaces.listeners.BlockListeners;
import com.craftaro.epicfurnaces.listeners.EntityListeners;
import com.craftaro.epicfurnaces.listeners.FurnaceListeners;
import com.craftaro.epicfurnaces.listeners.InteractListeners;
import com.craftaro.epicfurnaces.listeners.InventoryListeners;
import com.craftaro.epicfurnaces.settings.Settings;
import com.craftaro.epicfurnaces.storage.Storage;
import com.craftaro.epicfurnaces.storage.StorageRow;
import com.craftaro.epicfurnaces.storage.types.StorageYaml;
import com.craftaro.epicfurnaces.tasks.FurnaceTask;
import com.craftaro.epicfurnaces.tasks.HologramTask;
import com.craftaro.epicfurnaces.utils.Methods;
import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class EpicFurnaces extends SongodaPlugin {
    private final Config furnaceRecipeFile = new Config(this, "Furnace Recipes.yml");
    private final Config levelsFile = new Config(this, "levels.yml");

    private final GuiManager guiManager = new GuiManager(this);
    private LevelManager levelManager;
    private FurnaceManager furnaceManager;
    private BoostManager boostManager;
    private CommandManager commandManager;

    private BlacklistHandler blacklistHandler;
    private DataHelper dataHelper;

    /**
     * @deprecated Use {@link #getPlugin(Class)} instead
     */
    @Deprecated
    public static EpicFurnaces getInstance() {
        return EpicFurnaces.getPlugin(EpicFurnaces.class);
    }

    @Override
    public void onPluginLoad() {
    }

    @Override
    public void onPluginDisable() {
        this.dataManager.shutdown();
        HologramManager.removeAllHolograms();
    }

    @Override
    public void onPluginEnable() {
        // Run Songoda Updater
        SongodaCore.registerPlugin(this, 22, XMaterial.FURNACE);

        // Load Economy
        EconomyManager.load();
        // Register Hologram Plugin
        HologramManager.load(this);

        // Load Protection hooks
        ProtectionManager.load(this);

        // Setup Config
        Settings.setupConfig();
        this.setLocale(Settings.LANGUGE_MODE.getString(), false);

        // Set Economy & Hologram preference
        EconomyManager.getManager().setPreferredHook(Settings.ECONOMY_PLUGIN.getString());
        HologramManager.getManager().setPreferredHook(Settings.HOLOGRAM_PLUGIN.getString());

        // Hook into FabledSkyBlock
        if (Bukkit.getPluginManager().isPluginEnabled("FabledSkyBlock")) {
            new FabledSkyBlockLoader();
        }

        // Register commands
        this.commandManager = new CommandManager(this);
        this.commandManager.addMainCommand("EpicFurnaces")
                .addSubCommands(
                        new CommandBoost(this),
                        new CommandGive(this),
                        new CommandReload(this),
                        new CommandRemote(this),
                        new CommandSettings(this, this.guiManager)
                );

        loadLevelManager();

        this.furnaceManager = new FurnaceManager();
        this.boostManager = new BoostManager(this);
        this.blacklistHandler = new BlacklistHandler(this);

        dataHelper = new DataHelper(this);
        initDatabase(Collections.singletonList(new _1_InitialMigration()));

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            // Legacy data! Yay!
            File folder = getDataFolder();
            File dataFile = new File(folder, "data.yml");

            boolean converted = false;
            if (dataFile.exists()) {
                converted = true;
                Storage storage = new StorageYaml(this);
                if (storage.containsGroup("charged")) {
                    Bukkit.getConsoleSender().sendMessage("[" + getDescription().getName() + "] " + ChatColor.RED +
                            "Conversion process starting. Do NOT turn off your server." +
                            "EpicFurnaces hasn't fully loaded yet, so make sure users don't" +
                            "interact with the plugin until the conversion process is complete.");

                    List<Furnace> furnaces = new ArrayList<>();
                    for (StorageRow row : storage.getRowsByGroup("charged")) {
                        Location location = Methods.unserializeLocation(row.getKey());
                        if (location == null) {
                            continue;
                        }

                        if (row.get("level").asInt() == 0) {
                            continue;
                        }

                        String placedByStr = row.get("placedby").asString();
                        UUID placedBy = placedByStr == null ? null : UUID.fromString(placedByStr);

                        List<String> list = row.get("accesslist").asStringList();
                        if (!list.isEmpty()) {
                            for (String uuid : new ArrayList<>(list)) {
                                if (uuid.contains(":")) {
                                    list = new ArrayList<>();
                                    break;
                                }
                            }
                        }
                        List<UUID> usableList = list.stream().map(UUID::fromString).collect(Collectors.toList());

                        Map<XMaterial, Integer> toLevel = new HashMap<>();
                        List<String> toLevelCompiled = row.get("tolevelnew").asStringList();
                        for (String line : toLevelCompiled) {
                            String[] split = line.split(":");
                            toLevel.put(CompatibleMaterial.getMaterial(split[0]).get(), Integer.parseInt(split[1]));
                        }

                        furnaces.add(new FurnaceBuilder(location)
                                .setLevel(this.levelManager.getLevel(row.get("level").asInt()))
                                .setNickname(row.get("nickname").asString())
                                .setUses(row.get("uses").asInt())
                                .setToLevel(toLevel)
                                .setAccessList(usableList)
                                .setPlacedBy(placedBy).build());
                    }
                    Set<Data> data = new HashSet<>(furnaces);
                    this.dataManager.saveBatch(data);
                }

                // Adding in Boosts
                if (storage.containsGroup("boosts")) {
                    for (StorageRow row : storage.getRowsByGroup("boosts")) {
                        if (row.get("uuid").asObject() == null) {
                            continue;
                        }

                        this.dataManager.save(new BoostData(
                                row.get("amount").asInt(),
                                Long.parseLong(row.getKey()),
                                UUID.fromString(row.get("uuid").asString())));
                    }
                }
                dataFile.delete();
            }

            final boolean finalConverted = converted;
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                if (finalConverted) {
                    Bukkit.getConsoleSender().sendMessage("[" + getDescription().getName() + "] " + ChatColor.GREEN + "Conversion complete :)");
                }

                this.dataManager.loadBatch(Furnace.class, "active_furnaces").forEach((furnace) -> {
                    this.furnaceManager.addFurnace((Furnace) furnace);
                    this.boostManager.addBoosts(this.dataManager.loadBatch(BoostData.class, "boosted_players"));
                });
            });
        });

        setupRecipes();

        // Start Tasks
        FurnaceTask.startTask(this);
        HologramTask.startTask(this);

        // Register Listeners
        this.guiManager.init();
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new BlockListeners(this), this);
        pluginManager.registerEvents(new FurnaceListeners(this), this);
        pluginManager.registerEvents(new InteractListeners(this, this.guiManager), this);
        pluginManager.registerEvents(new InventoryListeners(this), this);
        pluginManager.registerEvents(new EntityListeners(this), this);
    }

    @Override
    public void onDataLoad() {
    }

    @Override
    public void onConfigReload() {
        this.setLocale(getConfig().getString("System.Language Mode"), true);
        this.locale.reloadMessages();
        this.blacklistHandler.reload();
        loadLevelManager();
    }

    @Override
    public List<Config> getExtraConfig() {
        return Collections.singletonList(this.levelsFile);
    }

    public void clearHologram(Furnace furnace) {
        HologramManager.removeHologram(furnace.getHologramId());
    }

    public void updateHolograms(Collection<Furnace> furnaces) {
        // are holograms enabled?
        if (!Settings.HOLOGRAMS.getBoolean() || !HologramManager.getManager().isEnabled()) {
            return;
        }

        Map<String, List<String>> holograms = new HashMap<>();

        for (Furnace furnace : furnaces) {
            // don't try to load furnaces in chunks that aren't loaded
            if (!furnace.isInLoadedChunk()) {
                continue;
            }

            BlockState state = furnace.getLocation().getBlock().getState();

            // verify that this is a furnace
            if (!(state instanceof org.bukkit.block.Furnace)) {
                continue;
            }

            org.bukkit.block.Furnace furnaceBlock = ((org.bukkit.block.Furnace) state);

            int performance = (furnaceBlock.getCookTime() - furnace.getPerformanceTotal(furnaceBlock.getType())) <= 0 ? 0 : furnace.getPerformanceTotal(furnaceBlock.getType());
            float percent = (float) (furnaceBlock.getCookTime() - performance) / (200 - performance);

            int progressBars = (int) (6 * percent) + (percent == 0 ? 0 : 1);
            int leftOver = (6 - progressBars);

            String progress;

            if (furnaceBlock.getInventory().getFuel() != null) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < progressBars; i++) {
                    sb.append("&a=");
                }
                for (int i = 0; i < leftOver; i++) {
                    sb.append("&c=");
                }

                progress = TextUtils.formatText(sb.toString());
            } else {
                progress = getLocale().getMessage("general.hologram.outoffuel").getMessage();
            }

            int inAmt = 0;
            int outAmt = 0;
            if (furnaceBlock.getInventory().getSmelting() != null) {
                inAmt = furnaceBlock.getInventory().getSmelting().getAmount();
            }
            if (furnaceBlock.getInventory().getResult() != null) {
                outAmt = furnaceBlock.getInventory().getResult().getAmount();
            }

            String stats = getLocale().getMessage("general.hologram.stats")
                    .processPlaceholder("in", inAmt)
                    .processPlaceholder("out", Math.min(outAmt, 64)).getMessage();

            List<String> hologramLines = Arrays.asList(progress, stats);
            if (!HologramManager.isHologramLoaded(furnace.getHologramId())) {
                HologramManager.createHologram(furnace.getHologramId(), furnace.getLocation().add(0, .15, 0), hologramLines);
                continue;
            }

            holograms.put(furnace.getHologramId(), hologramLines);
        }

        // Update holograms
        HologramManager.bulkUpdateHolograms(holograms);
    }

    private void loadLevelManager() {
        if (!this.levelsFile.getFile().exists()) {
            this.saveResource("levels.yml", false);
        }
        this.levelsFile.load();

        // Load a plugin of LevelManager
        this.levelManager = new LevelManager();
        /*
         * Register Levels into LevelManager from configuration.
         */
        this.levelManager.clear();
        for (String levelName : this.levelsFile.getKeys(false)) {
            int level = Integer.parseInt(levelName.split("-")[1]);

            ConfigurationSection levels = this.levelsFile.getConfigurationSection(levelName);

            int costExperience = levels.getInt("Cost-xp");
            int costEconomy = levels.getInt("Cost-eco");

            String performanceStr = levels.getString("Performance");
            int performance = performanceStr == null ? 0 : Integer.parseInt(performanceStr.substring(0, performanceStr.length() - 1));

            String reward = levels.getString("Reward");

            String fuelDurationStr = levels.getString("Fuel-duration");
            int fuelDuration = fuelDurationStr == null ? 0 : Integer.parseInt(fuelDurationStr.substring(0, fuelDurationStr.length() - 1));

            int overheat = levels.getInt("Overheat");
            int fuelShare = levels.getInt("Fuel-share");

            Map<XMaterial, Integer> materials = new LinkedHashMap<>();
            if (levels.contains("Cost-item")) {
                for (String materialStr : levels.getStringList("Cost-item")) {
                    String[] materialSplit = materialStr.split(":");
                    materials.put(CompatibleMaterial.getMaterial(materialSplit[0]).get(), Integer.parseInt(materialSplit[1]));
                }
            }

            this.levelManager.addLevel(level, costExperience, costEconomy, performance, reward, fuelDuration, overheat, fuelShare, materials);
        }
    }

    private void setupRecipes() {
        File config = new File(getDataFolder(), "Furnace Recipes.yml");
        if (!config.exists()) {
            saveResource("Furnace Recipes.yml", false);
        }
        this.furnaceRecipeFile.load();

        if (Settings.CUSTOM_RECIPES.getBoolean()) {
            ConfigurationSection cs = this.furnaceRecipeFile.getConfigurationSection("Recipes");
            for (String key : cs.getKeys(false)) {
                Material item = Material.valueOf(key.toUpperCase());
                Material result = Material.valueOf(this.furnaceRecipeFile.getString("Recipes." + key.toUpperCase() + ".result"));
                int amount = this.furnaceRecipeFile.getInt("Recipes." + key.toUpperCase() + ".amount");

                getServer().addRecipe(new FurnaceRecipe(new ItemStack(result, amount), item));
            }
        }
    }

    public boolean isLeveledFurnace(ItemStack itemStack) {
        NBTItem nbtItem = new NBTItem(itemStack);

        return nbtItem.hasTag("level") && nbtItem.hasTag("uses");
    }

    public ItemStack createLeveledFurnace(Material material, int level, int uses) {
        ItemStack item = new ItemStack(material, 1);

        if (Settings.FURNACE_ITEM.getBoolean()) {
            ItemMeta itemmeta = item.getItemMeta();
            itemmeta.setDisplayName(TextUtils.formatText(Methods.formatName(level)));
            item.setItemMeta(itemmeta);
        }

        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setInteger("level", level);
        nbtItem.setInteger("uses", uses);

        return nbtItem.getItem();
    }

    public int getFurnaceLevel(ItemStack item) {
        NBTItem nbtItem = new NBTItem(item);

        if (nbtItem.hasTag("level")) {
            return nbtItem.getInteger("level");
        }

        // Legacy trash.
        if (item.getItemMeta().getDisplayName().contains(":")) {
            String[] arr = (item.getItemMeta().getDisplayName().replace("§", "")).split(":");
            return Integer.parseInt(arr[0]);
        } else {
            return 1;
        }
    }

    public int getFurnaceUses(ItemStack item) {
        NBTItem nbtItem = new NBTItem(item);

        if (nbtItem.hasTag("uses")) {
            return nbtItem.getInteger("uses");
        }

        // Legacy trash.
        if (item.getItemMeta().getDisplayName().contains(":")) {
            String[] arr = (item.getItemMeta().getDisplayName().replace("§", "")).split(":");
            return Integer.parseInt(arr[1]);
        } else {
            return 0;
        }
    }

    public Config getFurnaceRecipeFile() {
        return this.furnaceRecipeFile;
    }

    public CommandManager getCommandManager() {
        return this.commandManager;
    }

    public BoostManager getBoostManager() {
        return this.boostManager;
    }

    public BlacklistHandler getBlacklistHandler() {
        return this.blacklistHandler;
    }

    public FurnaceManager getFurnaceManager() {
        return this.furnaceManager;
    }

    public LevelManager getLevelManager() {
        return this.levelManager;
    }

    public DataHelper getDataHelper() {
        return this.dataHelper;
    }
}

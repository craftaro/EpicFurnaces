package com.songoda.epicfurnaces;

import com.songoda.core.SongodaCore;
import com.songoda.core.SongodaPlugin;
import com.songoda.core.commands.CommandManager;
import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.configuration.Config;
import com.songoda.core.gui.GuiManager;
import com.songoda.core.hooks.EconomyManager;
import com.songoda.core.hooks.HologramManager;
import com.songoda.epicfurnaces.boost.BoostData;
import com.songoda.epicfurnaces.boost.BoostManager;
import com.songoda.epicfurnaces.commands.*;
import com.songoda.epicfurnaces.compatibility.EpicFurnacesPermission;
import com.songoda.epicfurnaces.compatibility.FabledSkyBlockLoader;
import com.songoda.epicfurnaces.furnace.Furnace;
import com.songoda.epicfurnaces.furnace.FurnaceBuilder;
import com.songoda.epicfurnaces.furnace.FurnaceManager;
import com.songoda.epicfurnaces.furnace.levels.LevelManager;
import com.songoda.epicfurnaces.handlers.BlacklistHandler;
import com.songoda.epicfurnaces.listeners.*;
import com.songoda.epicfurnaces.settings.Settings;
import com.songoda.epicfurnaces.storage.Storage;
import com.songoda.epicfurnaces.storage.StorageRow;
import com.songoda.epicfurnaces.storage.types.StorageYaml;
import com.songoda.epicfurnaces.tasks.FurnaceTask;
import com.songoda.epicfurnaces.tasks.HologramTask;
import com.songoda.epicfurnaces.utils.Methods;
import com.songoda.skyblock.SkyBlock;
import com.songoda.skyblock.permission.BasicPermission;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public class EpicFurnaces extends SongodaPlugin {

    private static EpicFurnaces INSTANCE;

    private final Config furnaceRecipeFile = new Config(this, "Furnace Recipes.yml");
    private final Config levelsFile = new Config(this, "levels.yml");
    private final Config dataFile = new Config(this, "data.yml");

    private final GuiManager guiManager = new GuiManager(this);
    private LevelManager levelManager;
    private FurnaceManager furnaceManager;
    private BoostManager boostManager;
    private CommandManager commandManager;

    private BlacklistHandler blacklistHandler;

    private Storage storage;

    public static EpicFurnaces getInstance() {
        return INSTANCE;
    }

    @Override
    public void onPluginLoad() {
        INSTANCE = this;
    }

    @Override
    public void onPluginDisable() {
        saveToFile();
        this.storage.closeConnection();
        HologramManager.removeAllHolograms();
    }

    @Override
    public void onPluginEnable() {
        // Run Songoda Updater
        SongodaCore.registerPlugin(this, 22, CompatibleMaterial.FURNACE);

        // Load Economy
        EconomyManager.load();
        // Register Hologram Plugin
        HologramManager.load(this);

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
        this.commandManager.addMainCommand("ef")
                .addSubCommands(
                        new CommandBoost(this),
                        new CommandGive(this),
                        new CommandReload(this),
                        new CommandRemote(this),
                        new CommandSettings(this, guiManager)
                );

        loadLevelManager();

        this.furnaceManager = new FurnaceManager();
        this.boostManager = new BoostManager();
        this.blacklistHandler = new BlacklistHandler();

        // Load from file
        dataFile.load();
        this.storage = new StorageYaml(this);
        loadFromFile();

        setupRecipies();

        // Start Tasks
        FurnaceTask.startTask(this);
        HologramTask.startTask(this);

        // Register Listeners
        guiManager.init();
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new BlockListeners(this), this);
        pluginManager.registerEvents(new FurnaceListeners(this), this);
        pluginManager.registerEvents(new InteractListeners(this, guiManager), this);
        pluginManager.registerEvents(new InventoryListeners(this), this);
        pluginManager.registerEvents(new EntityListeners(this), this);

        // Start auto save
        int saveInterval = Settings.AUTOSAVE.getInt() * 60 * 20;
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::saveToFile, saveInterval, saveInterval);
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
        return Arrays.asList(levelsFile);
    }

    public void clearHologram(Furnace furnace) {
        HologramManager.removeHologram(furnace.getLocation().add(0, .15, 0));
    }

    public void updateHologram(Furnace furnace) {
        // are holograms enabled?
        if (!Settings.HOLOGRAMS.getBoolean() || !HologramManager.getManager().isEnabled()) return;
        // don't try to load furnaces in chunks that aren't loaded
        if (!furnace.isInLoadedChunk()) return;

        BlockState state = furnace.getLocation().getBlock().getState();

        // verify that this is a furnace
        if (!(state instanceof org.bukkit.block.Furnace)) return;

        org.bukkit.block.Furnace furnaceBlock = ((org.bukkit.block.Furnace) state);

        int performance = (furnaceBlock.getCookTime() - furnace.getPerformanceTotal(furnaceBlock.getType())) <= 0 ? 0 : furnace.getPerformanceTotal(furnaceBlock.getType());

        float percent = (float) (furnaceBlock.getCookTime() - performance) / (200 - performance);

        int progressBars = (int) (6 * percent) + (percent == 0 ? 0 : 1);
        int leftOver = (6 - progressBars);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < progressBars; i++) {
            sb.append("&a=");
        }
        for (int i = 0; i < leftOver; i++) {
            sb.append("&c=");
        }

        ArrayList<String> lines = new ArrayList<>();

        String progress = Methods.formatText(sb.toString());

        if (furnaceBlock.getInventory().getFuel() == null) {
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

        lines.add(progress);
        lines.add(stats);

        // create the hologram
        HologramManager.updateHologram(furnace.getLocation().add(0, .15, 0), lines);
    }

    private void loadFromFile() {
        /*
         * Register furnaces into FurnaceManger from configuration
         */
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (storage.containsGroup("charged")) {
                for (StorageRow row : storage.getRowsByGroup("charged")) {
                    Location location = Methods.unserializeLocation(row.getKey());
                    if (location == null) continue;

                    if (row.get("level").asInt() == 0) continue;

                    String placedByStr = row.get("placedBy").asString();
                    UUID placedBy = placedByStr == null ? null : UUID.fromString(placedByStr);

                    List<String> list = row.get("accesslist").asStringList();
                    if (!list.isEmpty()) {
                        for (String uuid : new ArrayList<>(list))
                            if (uuid.contains(":")) {
                                list = new ArrayList<>();
                                break;
                            }
                    }
                    List<UUID> usableList = list.stream().map(UUID::fromString).collect(Collectors.toList());

                    Map<CompatibleMaterial, Integer> toLevel = new HashMap<>();
                    List<String> toLevelCompiled = row.get("tolevelnew").asStringList();
                    for (String line : toLevelCompiled) {
                        String[] split = line.split(":");
                        toLevel.put(CompatibleMaterial.getMaterial(split[0]), Integer.parseInt(split[1]));
                    }

                    Furnace furnace = new FurnaceBuilder(location)
                            .setLevel(levelManager.getLevel(row.get("level").asInt()))
                            .setNickname(row.get("nickname").asString())
                            .setUses(row.get("uses").asInt())
                            .setToLevel(toLevel)
                            .setAccessList(usableList)
                            .setPlacedBy(placedBy).build();

                    furnaceManager.addFurnace(furnace);
                }
            }

            // Adding in Boosts
            if (storage.containsGroup("boosts")) {
                for (StorageRow row : storage.getRowsByGroup("boosts")) {
                    if (row.getItems().get("uuid").asObject() != null)
                        continue;

                    BoostData boostData = new BoostData(
                            row.get("amount").asInt(),
                            Long.parseLong(row.getKey()),
                            UUID.fromString(row.get("uuid").asString()));

                    this.boostManager.addBoostToPlayer(boostData);
                }
            }

            // Register Hologram Plugin
            if (Settings.HOLOGRAMS.getBoolean()) {
                for (Furnace furnace : getFurnaceManager().getFurnaces().values()) {
                    if (furnace.getLocation() == null || furnace.getLocation().getWorld() == null)
                        continue;
                }
            }

            // Save data initially so that if the person reloads again fast they don't lose all their data.
            this.saveToFile();
        }, 10);
    }

    private void loadLevelManager() {
        if (!levelsFile.getFile().exists())
            this.saveResource("levels.yml", false);
        levelsFile.load();

        // Load an plugin of LevelManager
        levelManager = new LevelManager();
        /*
         * Register Levels into LevelManager from configuration.
         */
        levelManager.clear();
        for (String levelName : levelsFile.getKeys(false)) {
            int level = Integer.parseInt(levelName.split("-")[1]);

            ConfigurationSection levels = levelsFile.getConfigurationSection(levelName);

            int costExperiance = levels.getInt("Cost-xp");
            int costEconomy = levels.getInt("Cost-eco");

            String performanceStr = levels.getString("Performance");
            int performance = performanceStr == null ? 0 : Integer.parseInt(performanceStr.substring(0, performanceStr.length() - 1));

            String reward = levels.getString("Reward");

            String fuelDurationStr = levels.getString("Fuel-duration");
            int fuelDuration = fuelDurationStr == null ? 0 : Integer.parseInt(fuelDurationStr.substring(0, fuelDurationStr.length() - 1));

            int overheat = levels.getInt("Overheat");
            int fuelShare = levels.getInt("Fuel-share");

            Map<CompatibleMaterial, Integer> materials = new LinkedHashMap<>();
            if (levels.contains("Cost-item")) {
                for (String materialStr : levels.getStringList("Cost-item")) {
                    String[] materialSplit = materialStr.split(":");
                    materials.put(CompatibleMaterial.getMaterial(materialSplit[0]), Integer.parseInt(materialSplit[1]));
                }
            }

            levelManager.addLevel(level, costExperiance, costEconomy, performance, reward, fuelDuration, overheat, fuelShare, materials);
        }
    }

    /*
     * Saves registered furnaces to file.
     */
    private void saveToFile() {
        storage.doSave();
    }

    private void setupRecipies() {
        File config = new File(getDataFolder(), "Furnace Recipes.yml");
        if (!config.exists()) {
            saveResource("Furnace Recipes.yml", false);
        }
        furnaceRecipeFile.load();

        if (Settings.CUSTOM_RECIPES.getBoolean()) {
            ConfigurationSection cs = furnaceRecipeFile.getConfigurationSection("Recipes");
            for (String key : cs.getKeys(false)) {
                Material item = Material.valueOf(key.toUpperCase());
                Material result = Material.valueOf(furnaceRecipeFile.getString("Recipes." + key.toUpperCase() + ".result"));
                int amount = furnaceRecipeFile.getInt("Recipes." + key.toUpperCase() + ".amount");

                getServer().addRecipe(new FurnaceRecipe(new ItemStack(result, amount), item));
            }
        }
    }

    public ItemStack createLeveledFurnace(Material material, int level, int uses) {
        ItemStack item = new ItemStack(material, 1);

        if (Settings.FURNACE_ITEM.getBoolean()) {
            ItemMeta itemmeta = item.getItemMeta();
            itemmeta.setDisplayName(Methods.formatText(Methods.formatName(level, uses, true)));
            item.setItemMeta(itemmeta);
        }

        return item;
    }

    public int getFurnceLevel(ItemStack item) {
        if (item.getItemMeta().getDisplayName().contains(":")) {
            String arr[] = (item.getItemMeta().getDisplayName().replace("§", "")).split(":");
            return Integer.parseInt(arr[0]);
        } else {
            return 1;
        }
    }

    public int getFurnaceUses(ItemStack item) {
        if (item.getItemMeta().getDisplayName().contains(":")) {
            String arr[] = (item.getItemMeta().getDisplayName().replace("§", "")).split(":");
            return Integer.parseInt(arr[1]);
        } else {
            return 0;
        }
    }

    public Config getFurnaceRecipeFile() {
        return furnaceRecipeFile;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public BoostManager getBoostManager() {
        return boostManager;
    }

    public BlacklistHandler getBlacklistHandler() {
        return blacklistHandler;
    }

    public FurnaceManager getFurnaceManager() {
        return furnaceManager;
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }
}
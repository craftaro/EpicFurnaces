package com.songoda.epicfurnaces;

import com.songoda.core.SongodaCore;
import com.songoda.core.SongodaPlugin;
import com.songoda.core.commands.CommandManager;
import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.configuration.Config;
import com.songoda.core.database.DataMigrationManager;
import com.songoda.core.database.DatabaseConnector;
import com.songoda.core.database.SQLiteConnector;
import com.songoda.core.gui.GuiManager;
import com.songoda.core.hooks.EconomyManager;
import com.songoda.core.hooks.HologramManager;
import com.songoda.core.hooks.ProtectionManager;
import com.songoda.core.nms.NmsManager;
import com.songoda.core.nms.nbt.NBTCore;
import com.songoda.core.nms.nbt.NBTItem;
import com.songoda.epicfurnaces.boost.BoostData;
import com.songoda.epicfurnaces.boost.BoostManager;
import com.songoda.epicfurnaces.commands.CommandBoost;
import com.songoda.epicfurnaces.commands.CommandGive;
import com.songoda.epicfurnaces.commands.CommandReload;
import com.songoda.epicfurnaces.commands.CommandRemote;
import com.songoda.epicfurnaces.commands.CommandSettings;
import com.songoda.epicfurnaces.compatibility.FabledSkyBlockLoader;
import com.songoda.epicfurnaces.database.DataManager;
import com.songoda.epicfurnaces.database.migrations._1_InitialMigration;
import com.songoda.epicfurnaces.furnace.Furnace;
import com.songoda.epicfurnaces.furnace.FurnaceBuilder;
import com.songoda.epicfurnaces.furnace.FurnaceManager;
import com.songoda.epicfurnaces.furnace.levels.LevelManager;
import com.songoda.epicfurnaces.handlers.BlacklistHandler;
import com.songoda.epicfurnaces.listeners.BlockListeners;
import com.songoda.epicfurnaces.listeners.EntityListeners;
import com.songoda.epicfurnaces.listeners.FurnaceListeners;
import com.songoda.epicfurnaces.listeners.InteractListeners;
import com.songoda.epicfurnaces.listeners.InventoryListeners;
import com.songoda.epicfurnaces.settings.Settings;
import com.songoda.epicfurnaces.storage.Storage;
import com.songoda.epicfurnaces.storage.StorageRow;
import com.songoda.epicfurnaces.storage.types.StorageYaml;
import com.songoda.epicfurnaces.tasks.FurnaceTask;
import com.songoda.epicfurnaces.tasks.HologramTask;
import com.songoda.epicfurnaces.utils.Methods;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class  EpicFurnaces extends SongodaPlugin {

    private static EpicFurnaces INSTANCE;

    private final Config furnaceRecipeFile = new Config(this, "Furnace Recipes.yml");
    private final Config levelsFile = new Config(this, "levels.yml");

    private final GuiManager guiManager = new GuiManager(this);
    private LevelManager levelManager;
    private FurnaceManager furnaceManager;
    private BoostManager boostManager;
    private CommandManager commandManager;

    private BlacklistHandler blacklistHandler;

    private DatabaseConnector databaseConnector;
    private DataManager dataManager;

    public static EpicFurnaces getInstance() {
        return INSTANCE;
    }

    @Override
    public void onPluginLoad() {
        INSTANCE = this;
    }

    @Override
    public void onPluginDisable() {
        this.databaseConnector.closeConnection();
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

        // Database stuff.
        this.databaseConnector = new SQLiteConnector(this);
        this.getLogger().info("Data handler connected using SQLite.");

        this.dataManager = new DataManager(this.databaseConnector, this);
        DataMigrationManager dataMigrationManager = new DataMigrationManager(this.databaseConnector, this.dataManager,
                new _1_InitialMigration());
        dataMigrationManager.runMigrations();

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            // Legacy data! Yay!
            File folder = getDataFolder();
            File dataFile = new File(folder, "data.yml");

            boolean converted = false;
            if (dataFile.exists()) {
                converted = true;
                Storage storage = new StorageYaml(this);
                if (storage.containsGroup("charged")) {
                    console.sendMessage("[" + getDescription().getName() + "] " + ChatColor.RED +
                            "Conversion process starting. Do NOT turn off your server." +
                            "EpicFurnaces hasn't fully loaded yet, so make sure users don't" +
                            "interact with the plugin until the conversion process is complete.");

                    List<Furnace> furnaces = new ArrayList<>();
                    for (StorageRow row : storage.getRowsByGroup("charged")) {
                        Location location = Methods.unserializeLocation(row.getKey());
                        if (location == null) continue;

                        if (row.get("level").asInt() == 0) continue;

                        String placedByStr = row.get("placedby").asString();
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

                        furnaces.add(new FurnaceBuilder(location)
                                .setLevel(levelManager.getLevel(row.get("level").asInt()))
                                .setNickname(row.get("nickname").asString())
                                .setUses(row.get("uses").asInt())
                                .setToLevel(toLevel)
                                .setAccessList(usableList)
                                .setPlacedBy(placedBy).build());
                    }
                    dataManager.createFurnaces(furnaces);
                }

                // Adding in Boosts
                if (storage.containsGroup("boosts")) {
                    for (StorageRow row : storage.getRowsByGroup("boosts")) {
                        if (row.get("uuid").asObject() == null)
                            continue;

                        dataManager.createBoost(new BoostData(
                                row.get("amount").asInt(),
                                Long.parseLong(row.getKey()),
                                UUID.fromString(row.get("uuid").asString())));
                    }
                }
                dataFile.delete();
            }

            final boolean finalConverted = converted;
            dataManager.queueAsync(() -> {
                if (finalConverted) {
                    console.sendMessage("[" + getDescription().getName() + "] " + ChatColor.GREEN + "Conversion complete :)");
                }

                this.dataManager.getFurnaces((furnaces) -> {
                    this.furnaceManager.addFurnaces(furnaces.values());
                    this.dataManager.getBoosts((boosts) -> this.boostManager.addBoosts(boosts));
                });
            }, "create");
        });

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
    }

    @Override
    public void onDataLoad() {
        // Register Hologram Plugin

        if (Settings.HOLOGRAMS.getBoolean()) {
            for (Furnace furnace : getFurnaceManager().getFurnaces().values()) {
                if (furnace.getLocation() == null || furnace.getLocation().getWorld() == null)
                    continue;
            }
        }
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
            itemmeta.setDisplayName(Methods.formatText(Methods.formatName(level)));
            item.setItemMeta(itemmeta);
        }

        NBTCore nbt = NmsManager.getNbt();
        NBTItem nbtItem = nbt.of(item);
        nbtItem.set("level", level);
        nbtItem.set("uses", uses);

        return nbtItem.finish();
    }

    public int getFurnaceLevel(ItemStack item) {
        NBTCore nbt = NmsManager.getNbt();
        NBTItem nbtItem = nbt.of(item);

        if (nbtItem.has("level"))
            return nbtItem.getNBTObject("level").asInt();

        // Legacy trash.
        if (item.getItemMeta().getDisplayName().contains(":")) {
            String arr[] = (item.getItemMeta().getDisplayName().replace("§", "")).split(":");
            return Integer.parseInt(arr[0]);
        } else {
            return 1;
        }
    }

    public int getFurnaceUses(ItemStack item) {
        NBTCore nbt = NmsManager.getNbt();
        NBTItem nbtItem = nbt.of(item);

        if (nbtItem.has("uses"))
            return nbtItem.getNBTObject("uses").asInt();

        // Legacy trash.
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

    public DatabaseConnector getDatabaseConnector() {
        return databaseConnector;
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}
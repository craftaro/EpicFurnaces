package com.songoda.epicfurnaces;

import com.songoda.epicfurnaces.boost.BoostData;
import com.songoda.epicfurnaces.boost.BoostManager;
import com.songoda.epicfurnaces.command.CommandManager;
import com.songoda.epicfurnaces.economy.Economy;
import com.songoda.epicfurnaces.economy.PlayerPointsEconomy;
import com.songoda.epicfurnaces.economy.VaultEconomy;
import com.songoda.epicfurnaces.furnace.Furnace;
import com.songoda.epicfurnaces.furnace.FurnaceBuilder;
import com.songoda.epicfurnaces.furnace.FurnaceManager;
import com.songoda.epicfurnaces.furnace.levels.LevelManager;
import com.songoda.epicfurnaces.handlers.BlacklistHandler;
import com.songoda.epicfurnaces.hologram.Hologram;
import com.songoda.epicfurnaces.hologram.HologramHolographicDisplays;
import com.songoda.epicfurnaces.listeners.BlockListeners;
import com.songoda.epicfurnaces.listeners.FurnaceListeners;
import com.songoda.epicfurnaces.listeners.InteractListeners;
import com.songoda.epicfurnaces.listeners.InventoryListeners;
import com.songoda.epicfurnaces.storage.Storage;
import com.songoda.epicfurnaces.storage.StorageRow;
import com.songoda.epicfurnaces.storage.types.StorageMysql;
import com.songoda.epicfurnaces.storage.types.StorageYaml;
import com.songoda.epicfurnaces.tasks.FurnaceTask;
import com.songoda.epicfurnaces.tasks.HologramTask;
import com.songoda.epicfurnaces.utils.ConfigWrapper;
import com.songoda.epicfurnaces.utils.Methods;
import com.songoda.epicfurnaces.utils.Metrics;
import com.songoda.epicfurnaces.utils.ServerVersion;
import com.songoda.epicfurnaces.utils.locale.Locale;
import com.songoda.epicfurnaces.utils.settings.Setting;
import com.songoda.epicfurnaces.utils.settings.SettingsManager;
import com.songoda.epicfurnaces.utils.updateModules.LocaleModule;
import com.songoda.update.Plugin;
import com.songoda.update.SongodaUpdate;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.UUID;

public class EpicFurnaces extends JavaPlugin {
    private static CommandSender console = Bukkit.getConsoleSender();
    private static EpicFurnaces INSTANCE;

    private ServerVersion serverVersion = ServerVersion.fromPackageName(Bukkit.getServer().getClass().getPackage().getName());

    private ConfigWrapper langFile = new ConfigWrapper(this, "", "lang.yml");
    private ConfigWrapper dataFile = new ConfigWrapper(this, "", "data.yml");
    private ConfigWrapper furnaceRecipeFile = new ConfigWrapper(this, "", "Furnace Recipes.yml");
    private ConfigWrapper levelsFile = new ConfigWrapper(this, "", "levels.yml");


    private SettingsManager settingsManager;
    private LevelManager levelManager;
    private FurnaceManager furnaceManager;
    private CommandManager commandManager;
    private BoostManager boostManager;
    private Hologram hologram;

    private Locale locale;
    private Economy economy;

    private BlacklistHandler blacklistHandler;

    private Storage storage;

    public static EpicFurnaces getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        console.sendMessage(Methods.formatText("&a============================="));
        console.sendMessage(Methods.formatText("&7EpicFurnaces " + this.getDescription().getVersion() + " by &5Songoda <3&7!"));
        console.sendMessage(Methods.formatText("&7Action: &aEnabling&7..."));

        this.settingsManager = new SettingsManager(this);
        this.settingsManager.setupConfig();

        dataFile.createNewFile("Loading data file", "EpicFurnaces data file");
        langFile.createNewFile("Loading language file", "EpicFurnaces language file");
        loadDataFile();

        new Locale(this, "en_US");
        this.locale = Locale.getLocale(getConfig().getString("System.Language Mode"));

        // Running Songoda Updater
        Plugin plugin = new Plugin(this, 22);
        plugin.addModule(new LocaleModule());
        SongodaUpdate.load(plugin);

        loadLevelManager();

        this.furnaceManager = new FurnaceManager();
        this.commandManager = new CommandManager(this);
        this.boostManager = new BoostManager();
        this.blacklistHandler = new BlacklistHandler();

        // Setup Economy
        if (Setting.VAULT_ECONOMY.getBoolean()
                && getServer().getPluginManager().getPlugin("Vault") != null)
            this.economy = new VaultEconomy(this);
        else if (Setting.PLAYER_POINTS_ECONOMY.getBoolean()
                && getServer().getPluginManager().getPlugin("PlayerPoints") != null)
            this.economy = new PlayerPointsEconomy(this);

        this.checkStorage();

        // Load from file
        loadFromFile();

        setupRecipies();

        // Start Tasks
        FurnaceTask.startTask(this);
        HologramTask.startTask(this);

        PluginManager pluginManager = Bukkit.getPluginManager();

        // Register Hologram Plugin
        if (Setting.HOLOGRAMS.getBoolean()
                && pluginManager.isPluginEnabled("HolographicDisplays"))
            hologram = new HologramHolographicDisplays(this);

        // Register Listeners
        pluginManager.registerEvents(new BlockListeners(this), this);
        pluginManager.registerEvents(new FurnaceListeners(this), this);
        pluginManager.registerEvents(new InteractListeners(this), this);
        pluginManager.registerEvents(new InventoryListeners(this), this);

        // Start auto save
        int saveInterval = Setting.AUTOSAVE.getInt() * 60 * 20;
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::saveToFile, saveInterval, saveInterval);

        // Start Metrics
        new Metrics(this);

        console.sendMessage(Methods.formatText("&a============================="));
    }

    public void onDisable() {
        saveToFile();
        this.storage.closeConnection();

        console.sendMessage(Methods.formatText("&a============================="));
        console.sendMessage(Methods.formatText("&7EpicFurnaces " + this.getDescription().getVersion() + " by &5Songoda <3&7!"));
        console.sendMessage(Methods.formatText("&7Action: &cDisabling&7..."));
        console.sendMessage(Methods.formatText("&a============================="));
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

                    Furnace furnace = new FurnaceBuilder(location)
                            .setLevel(levelManager.getLevel(row.get("level").asInt()))
                            .setNickname(row.get("nickname").asString())
                            .setUses(row.get("uses").asInt())
                            .setToLevel(row.get("tolevel").asInt())
                            .setAccessList(row.get("accesslist").asStringList())
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

            if (hologram != null)
                hologram.loadHolograms();

            // Save data initially so that if the person reloads again fast they don't lose all their data.
            this.saveToFile();
        }, 10);
    }

    private void loadLevelManager() {
        if (!new File(this.getDataFolder(), "levels.yml").exists())
            this.saveResource("levels.yml", false);

        // Load an plugin of LevelManager
        levelManager = new LevelManager();
        /*
         * Register Levels into LevelManager from configuration.
         */
        levelManager.clear();
        for (String levelName : levelsFile.getConfig().getKeys(false)) {
            int level = Integer.valueOf(levelName.split("-")[1]);

            ConfigurationSection levels = levelsFile.getConfig().getConfigurationSection(levelName);

            int costExperiance = levels.getInt("Cost-xp");
            int costEconomy = levels.getInt("Cost-eco");

            String performanceStr = levels.getString("Performance");
            int performance = performanceStr == null ? 0 : Integer.parseInt(performanceStr.substring(0, performanceStr.length() - 1));

            String reward = levels.getString("Reward");

            String fuelDurationStr = levels.getString("Fuel-duration");
            int fuelDuration = fuelDurationStr == null ? 0 : Integer.parseInt(fuelDurationStr.substring(0, fuelDurationStr.length() - 1));

            int overheat = levels.getInt("Overheat");
            int fuelShare = levels.getInt("Fuel-share");

            levelManager.addLevel(level, costExperiance, costEconomy, performance, reward, fuelDuration, overheat, fuelShare);
        }
    }

    private void checkStorage() {
        if (getConfig().getBoolean("Database.Activate Mysql Support")) {
            this.storage = new StorageMysql(this);
        } else {
            this.storage = new StorageYaml(this);
        }
    }

    /*
     * Saves registered furnaces to file.
     */
    private void saveToFile() {
        checkStorage();

        storage.doSave();
    }

    public void reload() {
        this.locale = Locale.getLocale(getConfig().getString("System.Language Mode"));
        this.locale.reloadMessages();
        this.settingsManager.reloadConfig();
        this.blacklistHandler.reload();
    }

    private void loadDataFile() {
        dataFile.getConfig().options().copyDefaults(true);
        dataFile.saveConfig();
    }

    private void setupRecipies() {
        File config = new File(getDataFolder(), "Furnace Recipes.yml");
        if (!config.exists()) {
            saveResource("Furnace Recipes.yml", false);
        }

        if (getConfig().getBoolean("Main.Use Custom Recipes")) {
            ConfigurationSection cs = furnaceRecipeFile.getConfig().getConfigurationSection("Recipes");
            for (String key : cs.getKeys(false)) {
                Material item = Material.valueOf(key.toUpperCase());
                Material result = Material.valueOf(furnaceRecipeFile.getConfig().getString("Recipes." + key.toUpperCase() + ".result"));
                int amount = furnaceRecipeFile.getConfig().getInt("Recipes." + key.toUpperCase() + ".amount");

                getServer().addRecipe(new FurnaceRecipe(new ItemStack(result, amount), item));
            }
        }
    }

    public ItemStack createLeveledFurnace(Material material, int level, int uses) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta itemmeta = item.getItemMeta();

        if (getConfig().getBoolean("Main.Remember Furnace Item Levels"))
            itemmeta.setDisplayName(Methods.formatText(Methods.formatName(level, uses, true)));

        item.setItemMeta(itemmeta);
        return item;
    }

    public ConfigWrapper getDataFile() {
        return dataFile;
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

    public ServerVersion getServerVersion() {
        return serverVersion;
    }

    public boolean isServerVersion(ServerVersion version) {
        return serverVersion == version;
    }

    public boolean isServerVersion(ServerVersion... versions) {
        return ArrayUtils.contains(versions, serverVersion);
    }

    public boolean isServerVersionAtLeast(ServerVersion version) {
        return serverVersion.ordinal() >= version.ordinal();
    }

    public ConfigWrapper getFurnaceRecipeFile() {
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

    public Economy getEconomy() {
        return economy;
    }

    public SettingsManager getSettingsManager() {
        return settingsManager;
    }

    public Hologram getHologram() {
        return hologram;
    }

    public Locale getLocale() {
        return locale;
    }
}